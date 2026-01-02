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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Helper class containing string utility functions.
 *
 * @version $Revision: $
 */
public class StringUtilsExt {

  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final String EMPTY = "";
  private static final int PAD_LIMIT = 8192;
  public static final int INDEX_NOT_FOUND = -1;

  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  public static boolean equals(String s1, String s2) {
    return Objects.equals(s1, s2);
  }

  public static boolean equalsIgnoreCase(String s1, String s2) {
    return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
  }

  /**
   * Compares the two given strings in a case-sensitive or insensitive manner
   * depending on the <code>caseSensitive</code> parameter.
   *
   * @param strA          The first string
   * @param strB          The second string
   * @param caseSensitive Whether case matters in the comparison
   * @return <code>true</code> if the two strings are equal
   */
  public static boolean equals(String strA, String strB, boolean caseSensitive) {
    return caseSensitive ? equals(strA, strB) : equalsIgnoreCase(strA, strB);
  }

  /**
   * Compresses the whitespaces in the given string to a single space. Also
   * recognizes special delimiter chars and removes whitespaces before them.
   *
   * @param original The original string
   * @return The resulting string
   */
  public static String compressWhitespaces(String original) {
    if (isEmpty(original)) {
      return original;
    }
    StringBuilder result = new StringBuilder();
    char oldChar = ' ';
    char curChar;

    for (int idx = 0; idx < original.length(); idx++) {
      curChar = original.charAt(idx);
      if (Character.isWhitespace(curChar)) {
        if (oldChar != ' ') {
          oldChar = ' ';
          result.append(oldChar);
        }
      } else {
        if ((curChar == ',') || (curChar == ';') ||
            (curChar == '(') || (curChar == ')')) {
          if ((oldChar == ' ') && (result.length() > 0)) {
            // we're removing whitespaces before commas/semicolons
            result.setLength(result.length() - 1);
          }
        }
        if ((oldChar == ',') || (oldChar == ';')) {
          // we're adding a space after commas/semicolons if necessary
          result.append(' ');
        }
        result.append(curChar);
        oldChar = curChar;
      }
    }
    return result.toString();
  }

  public static String replace(final String text, final String searchString, final String replacement) {
    return replace(text, searchString, replacement, -1, true);
  }

  private static String replace(final String text, String searchString, final String replacement, int max, boolean ignoreCase) {
    if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
      return text;
    }
    if (ignoreCase) {
      searchString = searchString.toLowerCase();
    }
    int start = 0;
    int end = indexOf(text, searchString, start);
    if (end == INDEX_NOT_FOUND) {
      return text;
    }
    final int replLength = searchString.length();
    int increase = Math.max(replacement.length() - replLength, 0);
    increase *= max < 0 ? 16 : Math.min(max, 64);
    final StringBuilder buf = new StringBuilder(text.length() + increase);
    while (end != INDEX_NOT_FOUND) {
      buf.append(text, start, end).append(replacement);
      start = end + replLength;
      if (--max == 0) {
        break;
      }
      end = indexOf(text, searchString, start);
    }
    buf.append(text, start, text.length());
    return buf.toString();
  }

  public static boolean isNotEmpty(CharSequence cs) {
    return !isEmpty(cs);
  }

  public static boolean isNotBlank(CharSequence cs) {
    return !isBlank(cs);
  }

  public static boolean isBlank(final CharSequence cs) {
    final int strLen = length(cs);
    if (strLen == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets a CharSequence length or {@code 0} if the CharSequence is {@code null}.
   *
   * @param cs a CharSequence or {@code null}.
   * @return CharSequence length or {@code 0} if the CharSequence is {@code null}.
   * @since 2.4
   * @since 3.0 Changed signature from length(String) to length(CharSequence)
   */
  public static int length(final CharSequence cs) {
    return cs == null ? 0 : cs.length();
  }

  public static String repeat(final char repeat, final int count) {
    if (count <= 0) {
      return EMPTY;
    }
    char[] chars = new char[count];
    Arrays.fill(chars, repeat);
    return new String(chars);
  }

  public static String repeat(final String repeat, final int count) {
    // Performance tuned for 2.0 (JDK1.4)
    if (repeat == null) {
      return null;
    }
    if (count <= 0) {
      return EMPTY;
    }
    final int inputLength = repeat.length();
    if (count == 1 || inputLength == 0) {
      return repeat;
    }
    if (inputLength == 1 && count <= PAD_LIMIT) {
      return repeat(repeat.charAt(0), count);
    }
    final int outputLength = inputLength * count;
    switch (inputLength) {
      case 1:
        return repeat(repeat.charAt(0), count);
      case 2:
        final char ch0 = repeat.charAt(0);
        final char ch1 = repeat.charAt(1);
        final char[] output2 = new char[outputLength];
        for (int i = count * 2 - 2; i >= 0; i--, i--) {
          output2[i] = ch0;
          output2[i + 1] = ch1;
        }
        return new String(output2);
      default:
        final StringBuilder buf = new StringBuilder(outputLength);
        for (int i = 0; i < count; i++) {
          buf.append(repeat);
        }
        return buf.toString();
    }
  }

  public static String rightPad(final String str, final int size) {
    return rightPad(str, size, ' ');
  }

  public static String rightPad(final String str, final int size, String padStr) {
    if (str == null) {
      return null;
    }
    if (isEmpty(padStr)) {
      padStr = " ";
    }
    final int padLen = padStr.length();
    final int strLen = str.length();
    final int pads = size - strLen;
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (padLen == 1 && pads <= PAD_LIMIT) {
      return rightPad(str, size, padStr.charAt(0));
    }
    if (pads == padLen) {
      return str.concat(padStr);
    }
    if (pads < padLen) {
      return str.concat(padStr.substring(0, pads));
    }
    final char[] padding = new char[pads];
    final char[] padChars = padStr.toCharArray();
    for (int i = 0; i < pads; i++) {
      padding[i] = padChars[i % padLen];
    }
    return str.concat(new String(padding));
  }

  public static String rightPad(final String str, final int size, final char padChar) {
    if (str == null) {
      return null;
    }
    final int pads = size - str.length();
    if (pads <= 0) {
      return str; // returns original String when possible
    }
    if (pads > PAD_LIMIT) {
      return rightPad(str, size, String.valueOf(padChar));
    }
    return str.concat(repeat(padChar, pads));
  }

  /**
   * Used by the indexOf(CharSequence methods) as a green implementation of indexOf.
   *
   * @param cs         the {@link CharSequence} to be processed
   * @param searchChar the {@link CharSequence} to be searched for
   * @param start      the start index
   * @return the index where the search sequence was found, or {@code -1} if there is no such occurrence.
   */
  static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
    if (cs == null || searchChar == null) {
      return INDEX_NOT_FOUND;
    }
    if (cs instanceof String) {
      return ((String) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuilder) {
      return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
    }
    if (cs instanceof StringBuffer) {
      return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
    }
    return cs.toString().indexOf(searchChar.toString(), start);
  }

  public static String leftTrim(String charValue) {
    final int len = charValue.length();
    final char[] val = charValue.toCharArray();
    int st = 0;
    while ((st < len) && (val[st] <= ' ')) {
      st++;
    }
    return charValue.substring(st, len);
  }

  public static String rightTrim(String charValue) {
    int len = charValue.length();
    final char[] val = charValue.toCharArray();
    while (val[len - 1] <= ' ') {
      len--;
    }
    return len < val.length ? charValue.substring(0, len) : charValue;
  }

  public static String[] splitToArray(String str) {
    return splitToArray(str, ",");
  }

  public static String[] splitToArray(String str, String delim) {
    if (isEmpty(str)) {
      return EMPTY_STRING_ARRAY;
    }
    List<String> items = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(str, delim);
    while (tokenizer.hasMoreTokens()) {
      String item = tokenizer.nextToken().trim();
      if (!item.isEmpty()) {
        items.add(item);
      }
    }
    return items.toArray(new String[0]);
  }

  /**
   * Encodes the given value with Base64.
   *
   * @param value The value to encode
   * @return The encoded value
   */
  public static String base64Encode(String value) {
    return value == null ? null : new String(Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
  }

  public static String convertEncoding(String value, Charset from, Charset to) {
    return value == null ? null : new String(Base64.getEncoder().encode(value.getBytes(from)), to);
  }

  public static String toUTF8(String value) {
    return convertEncoding(value, StandardCharsets.UTF_8, StandardCharsets.UTF_8);
  }

  public static String unquoteDouble(String tableOfColumn) {
    if (StringUtilsExt.isEmpty(tableOfColumn)) {
      return tableOfColumn;
    }
    if (tableOfColumn.startsWith("\"") && tableOfColumn.endsWith("\"") && (tableOfColumn.length() > 1)) {
      tableOfColumn = tableOfColumn.substring(1, tableOfColumn.length() - 1);
    }
    return tableOfColumn;
  }
}
