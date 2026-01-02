package org.apache.ddlutils.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Helper class that writes XML data with or without pretty printing.
 *
 * @version $Revision: $
 */
public class PrettyPrintingXmlWriter {
  /**
   * The indentation string.
   */
  private static final String INDENT_STRING = "  ";
  /**
   * The output encoding.
   */
  private final String encoding;
  /**
   * The XML writer.
   */
  private XMLStreamWriter writer;
  /**
   * Whether we're pretty-printing.
   */
  private boolean prettyPrinting = true;

  /**
   * Creates a XML writer instance using UTF-8 encoding.
   *
   * @param output The target to write the data XML to
   */
  public PrettyPrintingXmlWriter(OutputStream output) throws DdlUtilsXMLException {
    this(output, "UTF-8");
  }

  /**
   * Creates a XML writer instance.
   *
   * @param output   The target to write the data XML to
   * @param encoding The encoding of the XML file
   */
  public PrettyPrintingXmlWriter(OutputStream output, String encoding) throws DdlUtilsXMLException {
    BufferedOutputStream bufferedOutput;

    if (output instanceof BufferedOutputStream) {
      bufferedOutput = (BufferedOutputStream) output;
    } else {
      bufferedOutput = new BufferedOutputStream(output);
    }
    if ((encoding == null) || (encoding.isEmpty())) {
      this.encoding = "UTF-8";
    } else {
      this.encoding = encoding;
    }

    try {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();

      writer = factory.createXMLStreamWriter(bufferedOutput, this.encoding);
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Creates a xml writer instance using the specified writer. Note that the writer
   * needs to be configured using the specified encoding.
   *
   * @param output   The target to write the data XML to
   * @param encoding The encoding of the writer
   */
  public PrettyPrintingXmlWriter(Writer output, String encoding) throws DdlUtilsXMLException {
    BufferedWriter bufferedWriter;

    if (output instanceof BufferedWriter) {
      bufferedWriter = (BufferedWriter) output;
    } else {
      bufferedWriter = new BufferedWriter(output);
    }
    this.encoding = encoding;
    try {
      XMLOutputFactory factory = XMLOutputFactory.newInstance();

      writer = factory.createXMLStreamWriter(bufferedWriter);
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Returnd the encoding used by this xml writer.
   *
   * @return The encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Rethrows the given exception, wrapped in a {@link DdlUtilsXMLException}. This
   * method allows subclasses to throw their own subclasses of this exception.
   *
   * @param baseEx The original exception
   * @throws DdlUtilsXMLException The wrapped exception
   */
  protected void throwException(Exception baseEx) throws DdlUtilsXMLException {
    throw new DdlUtilsXMLException(baseEx);
  }

  /**
   * Determines whether the output shall be pretty-printed.
   *
   * @return <code>true</code> if the output is pretty-printed
   */
  public boolean isPrettyPrinting() {
    return prettyPrinting;
  }

  /**
   * Specifies whether the output shall be pretty-printed.
   *
   * @param prettyPrinting <code>true</code> if the output is pretty-printed
   */
  public void setPrettyPrinting(boolean prettyPrinting) {
    this.prettyPrinting = prettyPrinting;
  }

  /**
   * Sets the default namespace.
   *
   * @param uri The namespace uri
   */
  public void setDefaultNamespace(String uri) throws DdlUtilsXMLException {
    try {
      writer.setDefaultNamespace(uri);
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Prints a newline if we're pretty-printing.
   */
  public void printlnIfPrettyPrinting() throws DdlUtilsXMLException {
    if (prettyPrinting) {
      try {
        writer.writeCharacters("\n");
      } catch (XMLStreamException ex) {
        throwException(ex);
      }
    }
  }

  /**
   * Prints the indentation if we're pretty-printing.
   *
   * @param level The indentation level
   */
  public void indentIfPrettyPrinting(int level) throws DdlUtilsXMLException {
    if (prettyPrinting) {
      try {
        for (int idx = 0; idx < level; idx++) {
          writer.writeCharacters(INDENT_STRING);
        }
      } catch (XMLStreamException ex) {
        throwException(ex);
      }
    }
  }

  /**
   * Writes the start of the XML document, i.e. the "<?xml?>" section and the start of the
   * root node.
   */
  public void writeDocumentStart() throws DdlUtilsXMLException {
    try {
      writer.writeStartDocument(encoding, "1.0");
      printlnIfPrettyPrinting();
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Writes the end of the XML document, i.e. end of the root node.
   */
  public void writeDocumentEnd() throws DdlUtilsXMLException {
    try {
      writer.writeEndDocument();
      writer.flush();
      writer.close();
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Writes a xmlns attribute to the stream.
   *
   * @param prefix       The prefix for the namespace, use <code>null</code> or an empty string for the default namespace
   * @param namespaceUri The namespace uri, can be <code>null</code>
   */
  public void writeNamespace(String prefix, String namespaceUri) throws DdlUtilsXMLException {
    try {
      if ((prefix == null) || (prefix.isEmpty())) {
        writer.writeDefaultNamespace(namespaceUri);
      } else {
        writer.writeNamespace(prefix, namespaceUri);
      }
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Writes the start of the indicated XML element.
   *
   * @param namespaceUri The namespace uri, can be <code>null</code>
   * @param localPart    The local part of the element's qname
   */
  public void writeElementStart(String namespaceUri, String localPart) throws DdlUtilsXMLException {
    try {
      if (namespaceUri == null) {
        writer.writeStartElement(localPart);
      } else {
        writer.writeStartElement(namespaceUri, localPart);
      }
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Writes the end of the current XML element.
   */
  public void writeElementEnd() throws DdlUtilsXMLException {
    try {
      writer.writeEndElement();
    } catch (XMLStreamException ex) {
      throwException(ex);
    }
  }

  /**
   * Writes an XML attribute.
   *
   * @param namespaceUri The namespace uri, can be <code>null</code>
   * @param localPart    The local part of the attribute's qname
   * @param value        The value; if <code>null</code> then no attribute is written
   */
  public void writeAttribute(String namespaceUri, String localPart, String value) throws DdlUtilsXMLException {
    if (value != null) {
      try {
        if (namespaceUri == null) {
          writer.writeAttribute(localPart, value);
        } else {
          writer.writeAttribute(namespaceUri, localPart, value);
        }
      } catch (XMLStreamException ex) {
        throwException(ex);
      }
    }
  }

  /**
   * Writes a CDATA segment.
   *
   * @param data The data to write
   */
  public void writeCData(String data) throws DdlUtilsXMLException {
    if (data != null) {
      try {
        writer.writeCData(data);
      } catch (XMLStreamException ex) {
        throwException(ex);
      }
    }
  }

  /**
   * Writes a text segment.
   *
   * @param data The data to write
   */
  public void writeCharacters(String data) throws DdlUtilsXMLException {
    if (data != null) {
      try {
        writer.writeCharacters(data);
      } catch (XMLStreamException ex) {
        throwException(ex);
      }
    }
  }
}
