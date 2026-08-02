#!/usr/bin/env python3
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import argparse
import html
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Iterable, List, Optional


JAVA_BLOCK_RE = re.compile(r"/\*\*(.*?)\*/\s*([^/\{;]+)[\{;]", re.DOTALL)
CLASS_RE = re.compile(r"(?:public\s+)?(?:abstract\s+)?class\s+(\w+)(?:\s+extends\s+(\w+))?")
METHOD_RE = re.compile(r"public\s+void\s+(set|addConfigured|add)(\w+)\s*\(([^)]*)\)")
PACKAGE_RE = re.compile(r"package\s+([\w.]+);")
ANT_NAME_RE = re.compile(r'name\s*=\s*"([^"]+)"')


@dataclass
class MemberDoc:
    name: str
    kind: str
    java_type: str
    required: bool
    note: str
    description: str


@dataclass
class AntDoc:
    ant_name: str
    class_name: str
    full_class_name: str
    kind: str
    description: str
    super_class: Optional[str] = None
    ignored: bool = False
    attributes: List[MemberDoc] = field(default_factory=list)
    nested_elements: List[MemberDoc] = field(default_factory=list)


def clean_javadoc(block: str) -> List[str]:
    lines = []
    for raw_line in block.splitlines():
        line = raw_line.strip()
        if line.startswith("*"):
            line = line[1:].strip()
        lines.append(line)
    return lines


def extract_ant_tag(lines: Iterable[str], tag: str) -> Optional[str]:
    capture = False
    chunks: List[str] = []
    marker = f"@ant.{tag}"
    for line in lines:
        if line.startswith(marker):
            capture = True
            chunks.append(line[len(marker):].strip())
            continue
        if capture:
            if line.startswith("@"):
                break
            chunks.append(line.strip())
    text = " ".join(chunk for chunk in chunks if chunk).strip()
    return normalize_text(text) if text else None


def plain_description(lines: Iterable[str]) -> str:
    chunks: List[str] = []
    for line in lines:
        if line.startswith("@"):
            break
        chunks.append(line)
    text = "\n".join(chunks)
    text = re.split(r"<br\s*/?>\s*Example\s*:?", text, flags=re.IGNORECASE)[0]
    text = re.split(r"<pre>", text, flags=re.IGNORECASE)[0]
    return normalize_text(text)


def normalize_text(text: str) -> str:
    text = html.unescape(text)
    text = re.sub(r"<code>(.*?)</code>", r"``\1``", text, flags=re.DOTALL)
    text = re.sub(r"<[^>]+>", "", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def decapitalize(value: str) -> str:
    if not value:
        return value
    if len(value) > 1 and value[1].isupper():
        return value
    return value[0].lower() + value[1:]


def method_type(arguments: str) -> str:
    first_arg = arguments.split(",", 1)[0].strip()
    parts = first_arg.split()
    return parts[0] if parts else ""


def parse_java_file(path: Path) -> Optional[AntDoc]:
    content = path.read_text(encoding="utf-8")
    package_match = PACKAGE_RE.search(content)
    package_name = package_match.group(1) if package_match else ""
    doc: Optional[AntDoc] = None

    for match in JAVA_BLOCK_RE.finditer(content):
        lines = clean_javadoc(match.group(1))
        declaration = " ".join(match.group(2).split())
        class_match = CLASS_RE.search(declaration)
        method_match = METHOD_RE.search(declaration)

        if class_match:
            class_name = class_match.group(1)
            super_class = class_match.group(2)
            task_tag = extract_ant_tag(lines, "task")
            type_tag = extract_ant_tag(lines, "type")
            ant_tag = task_tag or type_tag
            if ant_tag:
                ignored = 'ignore="true"' in ant_tag
                name_match = ANT_NAME_RE.search(ant_tag)
                ant_name = name_match.group(1) if name_match else decapitalize(class_name)
                kind = "task" if task_tag else "type"
                doc = AntDoc(
                    ant_name=ant_name,
                    class_name=class_name,
                    full_class_name=f"{package_name}.{class_name}" if package_name else class_name,
                    kind=kind,
                    description=plain_description(lines),
                    super_class=super_class,
                    ignored=ignored,
                )
            continue

        if doc and method_match:
            prefix, java_name, args = method_match.groups()
            required_note = extract_ant_tag(lines, "required")
            optional_note = extract_ant_tag(lines, "not-required")
            description = plain_description(lines)
            member = MemberDoc(
                name=decapitalize(java_name),
                kind="attribute" if prefix == "set" else "nested element",
                java_type=method_type(args),
                required=required_note is not None,
                note=required_note or optional_note or "",
                description=description,
            )
            if prefix == "set":
                doc.attributes.append(member)
            else:
                doc.nested_elements.append(member)

    return doc


def inherited_members(doc: AntDoc, all_docs: Dict[str, AntDoc], member_name: str) -> List[MemberDoc]:
    members: List[MemberDoc] = []
    parent_name = doc.super_class
    seen = set()
    while parent_name and parent_name not in seen:
        seen.add(parent_name)
        parent = all_docs.get(parent_name)
        if not parent:
            break
        members = list(getattr(parent, member_name)) + members
        parent_name = parent.super_class
    members.extend(getattr(doc, member_name))
    deduped: Dict[str, MemberDoc] = {}
    for member in members:
        deduped[member.name] = member
    return list(deduped.values())


def rst_heading(text: str, marker: str) -> List[str]:
    return [text, marker * len(text), ""]


def emit_member(member: MemberDoc) -> List[str]:
    required = "yes" if member.required else "no"
    type_suffix = f" ({member.java_type})" if member.java_type else ""
    lines = [f"``{member.name}``{type_suffix}", f"    Required: {required}."]
    if member.note:
        lines.append(f"    {member.note}")
    if member.description:
        lines.append(f"    {member.description}")
    lines.append("")
    return lines


def emit_doc_section(title: str, docs: List[AntDoc], all_docs: Dict[str, AntDoc]) -> List[str]:
    lines: List[str] = []
    lines.extend(rst_heading(title, "-"))
    for doc in docs:
        lines.extend(rst_heading(f"``{doc.ant_name}``", "~"))
        lines.append(f":Class: ``{doc.full_class_name}``")
        lines.append("")
        if doc.description:
            lines.append(doc.description)
            lines.append("")

        attributes = inherited_members(doc, all_docs, "attributes")
        nested_elements = inherited_members(doc, all_docs, "nested_elements")

        if attributes:
            lines.extend(rst_heading("Attributes", "^"))
            for attribute in attributes:
                lines.extend(emit_member(attribute))

        if nested_elements:
            lines.extend(rst_heading("Nested elements", "^"))
            for element in nested_elements:
                lines.extend(emit_member(element))
    return lines


def generate(source_root: Path, output: Path) -> None:
    task_dir = source_root / "org" / "apache" / "ddlutils" / "task"
    docs = [doc for doc in (parse_java_file(path) for path in sorted(task_dir.glob("*.java"))) if doc]
    all_docs = {doc.class_name: doc for doc in docs}
    tasks = sorted((doc for doc in docs if doc.kind == "task" and not doc.ignored), key=lambda doc: doc.ant_name.lower())
    types = sorted((doc for doc in docs if doc.kind == "type" and not doc.ignored), key=lambda doc: doc.ant_name.lower())

    lines: List[str] = [
        ".. Licensed to the Apache Software Foundation (ASF) under one",
        "   or more contributor license agreements.  See the NOTICE file",
        "   distributed with this work for additional information",
        "   regarding copyright ownership.  The ASF licenses this file",
        "   to you under the Apache License, Version 2.0 (the",
        '   "License"); you may not use this file except in compliance',
        "   with the License.  You may obtain a copy of the License at",
        "",
        "    http://www.apache.org/licenses/LICENSE-2.0",
        "",
        "   Unless required by applicable law or agreed to in writing,",
        "   software distributed under the License is distributed on an",
        '   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY',
        "   KIND, either express or implied.  See the License for the",
        "   specific language governing permissions and limitations",
        "   under the License.",
        "",
        ".. This file is generated by src/site/scripts/generate-ant-task-reference.py.",
        ".. Do not edit this file directly.",
        "",
    ]
    lines.extend(rst_heading("Generated Ant task reference", "="))
    lines.append("This page is generated from the ``@ant.*`` tags in ``src/main/java/org/apache/ddlutils/task``.")
    lines.append("")
    lines.extend(emit_doc_section("Tasks", tasks, all_docs))
    if types:
        lines.extend(emit_doc_section("Types", types, all_docs))

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate Sphinx Ant task reference from Java @ant tags.")
    parser.add_argument("--source-root", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    generate(args.source_root, args.output)


if __name__ == "__main__":
    main()
