package org.apache.ddlutils.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptSqlReader {

  private static final String DEFAULT_DELIMITER = ";";

  /**
   * regex to detect delimiter.
   * ignores spaces, allows delimiter in comment, allows an equals-sign
   */
  private static final Pattern PATTERN_DELIMITER = Pattern.compile("^\\s*(--)?\\s*delimiter\\s*=?\\s*([^\\s]+)+\\s*.*$", Pattern.CASE_INSENSITIVE);

  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  public final void setDelimiter(String delimiter, boolean fullLineDelimiter) {
    this.delimiter = delimiter;
    this.fullLineDelimiter = fullLineDelimiter;
  }

  protected void handleStatement(String command, LineNumberReader reader) {
  }

  protected void handleEndOfScript() {
  }

  protected void handleError(String command, Throwable throwable) throws Exception {
  }

  protected void handleComment(String comment) {
  }

  public final void read(Reader reader) throws Exception {
    StringBuilder command = null;
    try {
      LineNumberReader lineReader = new LineNumberReader(reader);
      String line;
      while ((line = lineReader.readLine()) != null) {
        if (command == null) {
          command = new StringBuilder();
        }
        String trimmedLine = line.trim();
        final Matcher delimMatch = PATTERN_DELIMITER.matcher(trimmedLine);
        if (trimmedLine.isEmpty()
            || trimmedLine.startsWith("//")) {
          // Do nothing
        } else if (delimMatch.matches()) {
          setDelimiter(delimMatch.group(2), false);
        } else if (trimmedLine.startsWith("--")) {
          handleComment(trimmedLine);
        } else if (!fullLineDelimiter
                   && trimmedLine.endsWith(getDelimiter())
                   || fullLineDelimiter
                      && trimmedLine.equals(getDelimiter())) {
          command.append(line, 0, line.lastIndexOf(getDelimiter()));
          command.append(" ");

          if (command.length() > 0) {
            handleStatement(command.toString(), lineReader);
          }
          command = null;
        } else {
          command.append(line);
          command.append("\n");
        }
      }
      if (command != null && command.length() > 0) {
        handleStatement(command.toString(), lineReader);
      }
      handleEndOfScript();
    } catch (IOException e) {
      if (command == null) {
        handleError(null, e);
      } else {
        handleError(command.toString(), e);
      }
    }
  }

  private String getDelimiter() {
    return delimiter;
  }
}
