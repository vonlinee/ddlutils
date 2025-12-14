package org.apache.ddlutils.parser;

import java.util.regex.Pattern;

public class ScriptReader {

  protected static final String LINE_SEPARATOR = System.lineSeparator();

  protected static final String DEFAULT_DELIMITER = ";";

  protected static final Pattern DELIMITER_PATTERN = Pattern
    .compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

  protected String delimiter = DEFAULT_DELIMITER;
  protected boolean fullLineDelimiter;
  protected boolean escapeProcessing = true;

  /**
   * Sets the escape processing.
   *
   * @param escapeProcessing the new escape processing
   */
  public void setEscapeProcessing(boolean escapeProcessing) {
    this.escapeProcessing = escapeProcessing;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public void setFullLineDelimiter(boolean fullLineDelimiter) {
    this.fullLineDelimiter = fullLineDelimiter;
  }
}
