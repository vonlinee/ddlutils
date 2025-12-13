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
 * Helper class containing string utility functions.
 *
 * @version $Revision: $
 */
public class StringUtilsExt extends org.apache.commons.lang3.StringUtils {
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
}
