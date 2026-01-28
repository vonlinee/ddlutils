package org.apache.ddlutils.sql;

public class SqlCharsetSanitizer {

  private enum State {
    NORMAL,
    IN_SINGLE_QUOTE,
    IN_DOUBLE_QUOTE,
    IN_BACKTICK,
    IN_BLOCK_COMMENT,
    IN_LINE_COMMENT
  }

  /**
   * Removes MySQL charset introducers (e.g., _utf8mb4, _latin1) that precede string literals.
   * It uses a state machine to ensure comments and existing strings are not modified.
   */
  public static String sanitize(String sql) {
    if (sql == null || sql.isEmpty()) {
      return sql;
    }

    StringBuilder sb = new StringBuilder(sql.length());
    State state = State.NORMAL;
    int length = sql.length();

    for (int i = 0; i < length; i++) {
      char c = sql.charAt(i);
      char nextChar = (i + 1 < length) ? sql.charAt(i + 1) : '\0';

      switch (state) {
        case NORMAL:
          // 1. Check for Comments
          if (c == '#' || (c == '-' && nextChar == '-')) {
            // Note: MySQL technically requires whitespace after '--', but being loose here is safer
            state = State.IN_LINE_COMMENT;
            sb.append(c);
          } else if (c == '/' && nextChar == '*') {
            state = State.IN_BLOCK_COMMENT;
            sb.append(c);
            sb.append(nextChar);
            i++; // consume '*'
          }
          // 2. Check for Quotes
          else if (c == '\'') {
            state = State.IN_SINGLE_QUOTE;
            sb.append(c);
          } else if (c == '"') {
            state = State.IN_DOUBLE_QUOTE;
            sb.append(c);
          } else if (c == '`') {
            state = State.IN_BACKTICK;
            sb.append(c);
          }
          // 3. THE LOGIC: Check for Charset Introducer (Start with '_')
          else if (c == '_') {
            // Look ahead to see if this is a charset introducer followed by a string
            int endOfIdent = findEndOfIdentifier(sql, i);
            int afterWhitespace = skipWhitespace(sql, endOfIdent);

            // Check if the identifier ends at a valid spot and is followed by a single quote
            if (afterWhitespace < length && sql.charAt(afterWhitespace) == '\'') {
              // FOUND IT! It is a charset introducer (e.g., _utf8mb4 '...')
              // We SKIP writing the identifier and the whitespace.
              // We advance 'i' to just before the quote, so the next loop processes the quote normally.
              i = afterWhitespace - 1;
            } else {
              // False alarm (e.g., _my_column_name), just write the char
              sb.append(c);
            }
          }
          // 4. Normal character
          else {
            sb.append(c);
          }
          break;

        case IN_SINGLE_QUOTE:
          sb.append(c);
          if (c == '\\') { // Handle Escape
            if (i + 1 < length) sb.append(sql.charAt(++i));
          } else if (c == '\'') {
            // Handle standard SQL escape (doubled quote '')
            if (nextChar == '\'') {
              sb.append(nextChar);
              i++;
            } else {
              state = State.NORMAL;
            }
          }
          break;

        case IN_DOUBLE_QUOTE:
          sb.append(c);
          if (c == '\\') { // Handle Escape
            if (i + 1 < length) sb.append(sql.charAt(++i));
          } else if (c == '"') {
            if (nextChar == '"') {
              sb.append(nextChar);
              i++;
            } else {
              state = State.NORMAL;
            }
          }
          break;

        case IN_BACKTICK:
          sb.append(c);
          if (c == '`') {
            state = State.NORMAL;
          }
          break;

        case IN_LINE_COMMENT:
          sb.append(c);
          if (c == '\n') {
            state = State.NORMAL;
          }
          break;

        case IN_BLOCK_COMMENT:
          sb.append(c);
          if (c == '*' && nextChar == '/') {
            sb.append(nextChar);
            i++;
            state = State.NORMAL;
          }
          break;
      }
    }
    return sb.toString();
  }

  // Helper: Find where the identifier (starting at index start) ends
  private static int findEndOfIdentifier(String s, int start) {
    int i = start + 1; // skip the initial '_'
    while (i < s.length()) {
      char c = s.charAt(i);
      // MySQL identifiers can have letters, numbers, $, _
      if (!Character.isLetterOrDigit(c) && c != '_' && c != '$') {
        break;
      }
      i++;
    }
    return i;
  }

  // Helper: Skip spaces to find the next meaningful char
  private static int skipWhitespace(String s, int start) {
    int i = start;
    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
    return i;
  }
}
