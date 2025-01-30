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

import java.util.Objects;

/**
 * Helper class containing string utility functions.
 *
 * @version $Revision: $
 */
public class StringUtils {

  /**
   * The empty String <code>""</code>.
   * @since 2.0
   */
  public static final String EMPTY = "";

  /**
   * <p>The maximum size to which the padding constant(s) can expand.</p>
   */
  private static final int PAD_LIMIT = 8192;

  /**
   * <p>An array of <code>String</code>s used for padding.</p>
   *
   * <p>Used for efficient space padding. The length of each String expands as needed.</p>
   */
  private static final String[] PADDING = new String[Character.MAX_VALUE];

  static {
    // space padding is most common, start with 64 chars
    PADDING[32] = "                                                                ";
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
    if (strA == null) {
      return strB == null;
    }
    return caseSensitive ? Objects.equals(strA, strB) : strA.equalsIgnoreCase(strB);
  }

  public static boolean equals(String str1, String str2) {
    return Objects.equals(str1, str2);
  }

  public static boolean isEmpty(String str) {
    return str == null || str.isEmpty();
  }


  /**
   * <p>Replaces all occurrences of a String within another String.</p>
   *
   * <p>A <code>null</code> reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *)        = null
   * StringUtils.replace("", *, *)          = ""
   * StringUtils.replace("any", null, *)    = "any"
   * StringUtils.replace("any", *, null)    = "any"
   * StringUtils.replace("any", "", *)      = "any"
   * StringUtils.replace("aba", "a", null)  = "aba"
   * StringUtils.replace("aba", "a", "")    = "b"
   * StringUtils.replace("aba", "a", "z")   = "zbz"
   * </pre>
   *
   * @see #replace(String text, String repl, String with, int max)
   * @param text  text to search and replace in, may be null
   * @param repl  the String to search for, may be null
   * @param with  the String to replace with, may be null
   * @return the text with any replacements processed,
   *  <code>null</code> if null String input
   */
  public static String replace(String text, String repl, String with) {
    return replace(text, repl, with, -1);
  }

  /**
   * <p>Replaces a String with another String inside a larger String,
   * for the first <code>max</code> values of the search String.</p>
   *
   * <p>A <code>null</code> reference passed to this method is a no-op.</p>
   *
   * <pre>
   * StringUtils.replace(null, *, *, *)         = null
   * StringUtils.replace("", *, *, *)           = ""
   * StringUtils.replace("any", null, *, *)     = "any"
   * StringUtils.replace("any", *, null, *)     = "any"
   * StringUtils.replace("any", "", *, *)       = "any"
   * StringUtils.replace("any", *, *, 0)        = "any"
   * StringUtils.replace("abaa", "a", null, -1) = "abaa"
   * StringUtils.replace("abaa", "a", "", -1)   = "b"
   * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
   * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
   * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
   * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
   * </pre>
   *
   * @param text  text to search and replace in, may be null
   * @param repl  the String to search for, may be null
   * @param with  the String to replace with, may be null
   * @param max  maximum number of values to replace, or <code>-1</code> if no maximum
   * @return the text with any replacements processed,
   *  <code>null</code> if null String input
   */
  public static String replace(String text, String repl, String with, int max) {
    if (text == null || isEmpty(repl) || with == null || max == 0) {
      return text;
    }

    StringBuilder buf = new StringBuilder(text.length());
    int start = 0, end = 0;
    while ((end = text.indexOf(repl, start)) != -1) {
      buf.append(text, start, end).append(with);
      start = end + repl.length();

      if (--max == 0) {
        break;
      }
    }
    buf.append(text.substring(start));
    return buf.toString();
  }

  /**
   * <p>Repeat a String <code>repeat</code> times to form a
   * new String.</p>
   *
   * <pre>
   * StringUtils.repeat(null, 2) = null
   * StringUtils.repeat("", 0)   = ""
   * StringUtils.repeat("", 2)   = ""
   * StringUtils.repeat("a", 3)  = "aaa"
   * StringUtils.repeat("ab", 2) = "abab"
   * StringUtils.repeat("a", -2) = ""
   * </pre>
   *
   * @param str  the String to repeat, may be null
   * @param repeat  number of times to repeat str, negative treated as zero
   * @return a new String consisting of the original String repeated,
   *  <code>null</code> if null String input
   */
  public static String repeat(String str, int repeat) {
    // Performance tuned for 2.0 (JDK1.4)

    if (str == null) {
      return null;
    }
    if (repeat <= 0) {
      return EMPTY;
    }
    int inputLength = str.length();
    if (repeat == 1 || inputLength == 0) {
      return str;
    }
    if (inputLength == 1 && repeat <= PAD_LIMIT) {
      return padding(repeat, str.charAt(0));
    }

    int outputLength = inputLength * repeat;
    switch (inputLength) {
      case 1 :
        char ch = str.charAt(0);
        char[] output1 = new char[outputLength];
        for (int i = repeat - 1; i >= 0; i--) {
          output1[i] = ch;
        }
        return new String(output1);
      case 2 :
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        char[] output2 = new char[outputLength];
        for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
          output2[i] = ch0;
          output2[i + 1] = ch1;
        }
        return new String(output2);
      default :
        StringBuilder buf = new StringBuilder(outputLength);
        for (int i = 0; i < repeat; i++) {
          buf.append(str);
        }
        return buf.toString();
    }
  }

  /**
   * <p>Returns padding using the specified delimiter repeated
   * to a given length.</p>
   *
   * <pre>
   * StringUtils.padding(0, 'e')  = ""
   * StringUtils.padding(3, 'e')  = "eee"
   * StringUtils.padding(-2, 'e') = IndexOutOfBoundsException
   * </pre>
   *
   * @param repeat  number of times to repeat delim
   * @param padChar  character to repeat
   * @return String with repeated character
   * @throws IndexOutOfBoundsException if <code>repeat &lt; 0</code>
   */
  private static String padding(int repeat, char padChar) {
    // be careful of synchronization in this method
    // we are assuming that get and set from an array index is atomic
    String pad = PADDING[padChar];
    if (pad == null) {
      pad = String.valueOf(padChar);
    }
    while (pad.length() < repeat) {
      pad = pad.concat(pad);
    }
    PADDING[padChar] = pad;
    return pad.substring(0, repeat);
  }
}
