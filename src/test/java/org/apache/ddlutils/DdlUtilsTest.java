package org.apache.ddlutils;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.util.StringUtilsExt;
import org.junit.jupiter.api.Assertions;

/**
 * Base class for DdlUtils tests.
 *
 * @version $Revision: $
 */
public abstract class DdlUtilsTest {
  /**
   * The log for the tests.
   */
  private final Log _log = LogFactory.getLog(getClass());

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  public static void assertTrue(String message, boolean condition) {
    Assertions.assertTrue(condition, message);
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError.
   */
  public static void assertTrue(boolean condition) {
    Assertions.assertTrue(condition);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  public static void assertFalse(String message, boolean condition) {
    Assertions.assertFalse(condition, message);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError.
   */
  public static void assertFalse(boolean condition) {
    Assertions.assertFalse(condition);
  }

  /**
   * Fails a test with the given message.
   */
  public static void fail(String message) {
    Assertions.fail(message);
  }

  /**
   * Fails a test with no message.
   */
  public static void fail() {
    Assertions.fail();
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, Object expected, Object actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown.
   */
  public static void assertEquals(Object expected, Object actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two Strings are equal.
   */
  public static void assertEquals(String message, String expected, String actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two Strings are equal.
   */
  public static void assertEquals(String expected, String actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  public static void assertEquals(String message, double expected, double actual, double delta) {
    Assertions.assertEquals(expected, actual, delta, message);
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  public static void assertEquals(double expected, double actual, double delta) {
    Assertions.assertEquals(expected, actual, delta);
  }

  /**
   * Asserts that two floats are equal concerning a positive delta. If they
   * are not an AssertionFailedError is thrown with the given message. If the
   * expected value is infinity then the delta value is ignored.
   */
  public static void assertEquals(String message, float expected, float actual, float delta) {
    Assertions.assertEquals(expected, actual, delta, message);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  public static void assertEquals(float expected, float actual, float delta) {
    Assertions.assertEquals(expected, actual, delta);
  }

  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, long expected, long actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two longs are equal.
   */
  public static void assertEquals(long expected, long actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, boolean expected, boolean actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two booleans are equal.
   */
  public static void assertEquals(boolean expected, boolean actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, byte expected, byte actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two bytes are equal.
   */
  public static void assertEquals(byte expected, byte actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, char expected, char actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two chars are equal.
   */
  public static void assertEquals(char expected, char actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, short expected, short actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two shorts are equal.
   */
  public static void assertEquals(short expected, short actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertEquals(String message, int expected, int actual) {
    Assertions.assertEquals(expected, actual, message);
  }

  /**
   * Asserts that two ints are equal.
   */
  public static void assertEquals(int expected, int actual) {
    Assertions.assertEquals(expected, actual);
  }

  /**
   * Asserts that an object isn't null.
   */
  public static void assertNotNull(Object object) {
    Assertions.assertNotNull(object);
  }

  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertNotNull(String message, Object object) {
    Assertions.assertNotNull(object, message);
  }

  /**
   * Asserts that an object is null. If it isn't an {@link AssertionError} is
   * thrown.
   * Message contains: Expected: <null> but was: object
   *
   * @param object Object to check or <code>null</code>
   */
  public static void assertNull(Object object) {
    Assertions.assertNull(object);
  }

  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertNull(String message, Object object) {
    Assertions.assertNull(object, message);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  public static void assertSame(String message, Object expected, Object actual) {
    Assertions.assertSame(expected, actual, message);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  public static void assertSame(Object expected, Object actual) {
    Assertions.assertSame(expected, actual);
  }

  /**
   * Asserts that two objects do not refer to the same object. If they do
   * refer to the same object an AssertionFailedError is thrown with the
   * given message.
   */
  public static void assertNotSame(String message, Object expected, Object actual) {
    Assertions.assertNotSame(expected, actual, message);
  }

  /**
   * Asserts that two objects do not refer to the same object. If they do
   * refer to the same object an AssertionFailedError is thrown.
   */
  public static void assertNotSame(Object expected, Object actual) {
    Assertions.assertNotSame(expected, actual);
  }

  public static void failSame(String message) {
    String formatted = (message != null) ? message + " " : "";
    fail(formatted + "expected not same");
  }

  public static void failNotSame(String message, Object expected, Object actual) {
    String formatted = (message != null) ? message + " " : "";
    fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
  }

  public static void failNotEquals(String message, Object expected, Object actual) {
    fail(format(message, expected, actual));
  }

  public static String format(String message, Object expected, Object actual) {
    String formatted = "";
    if (message != null && !message.isEmpty()) {
      formatted = message + " ";
    }
    return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
  }

  /**
   * Returns the log.
   *
   * @return The log
   */
  protected Log getLog() {
    return _log;
  }

  /**
   * Compares the two strings but ignores any whitespace differences. It also
   * recognizes special delimiter chars.
   *
   * @param expected The expected string
   * @param actual   The actual string
   */
  protected void assertEqualsIgnoringWhitespaces(String expected, String actual) {
    String processedExpected = StringUtilsExt.compressWhitespaces(expected);
    String processedActual = StringUtilsExt.compressWhitespaces(actual);
    Assertions.assertEquals(processedExpected, processedActual);
  }
}
