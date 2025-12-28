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
package org.apache.ddlutils.sql;

public final class SqlUtils {

  private SqlUtils() {
  }

  /**
   * Unwraps a string value by removing the surrounding single quotes if present.
   * This method is useful for processing SQL string literals that are enclosed in single quotes.
   *
   * @param value the string value to unwrap, may be null
   * @return the string value with surrounding single quotes removed, or null if the input is null
   *
   * <p>Example usage:
   * <pre>
   * unwrapStringValue("'hello'")  // returns "hello"
   * unwrapStringValue("hello")    // returns "hello"
   * unwrapStringValue("'world")   // returns "world"
   * unwrapStringValue("test'")    // returns "test"
   * unwrapStringValue(null)       // returns null
   * </pre>
   */
  public static String unwrapStringValue(String value) {
    if (value == null) {
      return null;
    }
    if (value.startsWith("'")) {
      value = value.substring(1);
    }
    if (value.endsWith("'")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }

  public static String toValueLiteral(Object value) {
    if (value instanceof CharSequence) {
      return "'" + value + "'";
    } else if (value instanceof Number) {
      return value.toString();
    } else {
      return "'" + value + "'";
    }
  }
}
