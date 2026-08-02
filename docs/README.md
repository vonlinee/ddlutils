# Documentation

Generate the project website from the repository root:

```shell
mvn site
```

The Maven site lifecycle prepares the Sphinx sources under
`target/generated-site/sphinx`, generates the Ant task reference from the
`@ant.*` tags in `src/main/java/org/apache/ddlutils/task`, and writes the HTML
site to:

```text
target/site
```

The generated Ant task reference source is:

```text
target/generated-site/sphinx/generated/ant-task-reference.rst
```

Do not edit generated files under `target`. Update the Java source comments or
`src/site/scripts/generate-ant-task-reference.py` instead.

## Preview

After running `mvn site`, open the generated site directly:

```text
target/site/index.html
```

Or serve it locally with Python:

```shell
python -m http.server 8000 -d target/site
```

Then visit:

```text
http://localhost:8000/
```

The same preview server can be started through Maven:

```shell
mvn -Pserve-site validate
```

The Maven preview profile serves `target/site` on port `8000` by default. Use
another port when needed:

```shell
mvn -Pserve-site validate -Dsite.preview.port=9000
```

If your environment uses the Windows Python launcher, either run:

```shell
py -m http.server 8000 -d target/site
```

or pass the executable to Maven:

```shell
mvn -Pserve-site validate -Dpython.executable=py
```

# Test

```shell
# running test with debug mode
mvn -Dmaven.surefire.debug test
```

