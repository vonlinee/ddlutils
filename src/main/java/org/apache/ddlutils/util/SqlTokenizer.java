package org.apache.ddlutils.util;

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

/**
 * A statement tokenizer for SQL strings that splits only at delimiters that
 * are at the end of a line or the end of the SQL (row mode).
 * <p>
 * TODO: Add awareness of strings, so that semicolons within strings are not parsed
 *
 * @version $Revision: $
 */
public class SqlTokenizer {
  /**
   * The SQL to tokenize.
   */
  private final String sql;
  /**
   * The index of the last character in the string.
   */
  private final int lastCharIdx;
  /**
   * The last delimiter position in the string.
   */
  private int lastDelimiterPos = -1;
  /**
   * The next delimiter position in the string.
   */
  private int nextDelimiterPos = -1;
  /**
   * Whether there are no more tokens.
   */
  private boolean finished;

  /**
   * Creates a new SQL tokenizer.
   *
   * @param sql The sql text
   */
  public SqlTokenizer(String sql) {
    this.sql = sql;
    lastCharIdx = sql.length() - 1;
  }

  /**
   * Determines whether there are more statements.
   *
   * @return <code>true</code> if there are more statements
   */
  public boolean hasMoreStatements() {
    if (finished) {
      return false;
    } else {
      if (nextDelimiterPos <= lastDelimiterPos) {
        nextDelimiterPos = sql.indexOf(';', lastDelimiterPos + 1);
        while ((nextDelimiterPos >= 0) && (nextDelimiterPos < lastCharIdx)) {
          char nextChar = sql.charAt(nextDelimiterPos + 1);

          if ((nextChar == '\r') || (nextChar == '\n')) {
            break;
          }
          nextDelimiterPos = sql.indexOf(';', nextDelimiterPos + 1);
        }
      }
      return (nextDelimiterPos >= 0) || (lastDelimiterPos < lastCharIdx);
    }
  }

  /**
   * Returns the next statement.
   *
   * @return The statement
   */
  public String getNextStatement() {
    String result = null;

    if (hasMoreStatements()) {
      if (nextDelimiterPos >= 0) {
        result = sql.substring(lastDelimiterPos + 1, nextDelimiterPos);
        lastDelimiterPos = nextDelimiterPos;
      } else {
        result = sql.substring(lastDelimiterPos + 1);
        finished = true;
      }
    }
    return result;
  }
}
