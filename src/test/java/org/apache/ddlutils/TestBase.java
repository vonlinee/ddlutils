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

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.util.StringUtilsExt;

/**
 * Base class for DdlUtils tests.
 *
 * @version $Revision: $
 */
public abstract class TestBase extends TestCase {
  /**
   * The log for the tests.
   */
  private final Log _log = LogFactory.getLog(getClass());

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
    assertEquals(processedExpected, processedActual);
  }


  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertTrue(String message, boolean condition) {
    Assert.assertTrue(message, condition);
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError.
   */
  @SuppressWarnings("deprecation")
  public static void assertTrue(boolean condition) {
    Assert.assertTrue(condition);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertFalse(String message, boolean condition) {
    Assert.assertFalse(message, condition);
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError.
   */
  @SuppressWarnings("deprecation")
  public static void assertFalse(boolean condition) {
    Assert.assertFalse(condition);
  }

  /**
   * Fails a test with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void fail(String message) {
    Assert.fail(message);
  }

  /**
   * Fails a test with no message.
   */
  @SuppressWarnings("deprecation")
  public static void fail() {
    Assert.fail();
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, Object expected, Object actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(Object expected, Object actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two Strings are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, String expected, String actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two Strings are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String expected, String actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, double expected, double actual, double delta) {
    Assert.assertEquals(message, expected, actual, delta);
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(double expected, double actual, double delta) {
    Assert.assertEquals(expected, actual, delta);
  }

  /**
   * Asserts that two floats are equal concerning a positive delta. If they
   * are not an AssertionFailedError is thrown with the given message. If the
   * expected value is infinity then the delta value is ignored.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, float expected, float actual, float delta) {
    Assert.assertEquals(message, expected, actual, delta);
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(float expected, float actual, float delta) {
    Assert.assertEquals(expected, actual, delta);
  }

  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, long expected, long actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two longs are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(long expected, long actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, boolean expected, boolean actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two booleans are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(boolean expected, boolean actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, byte expected, byte actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two bytes are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(byte expected, byte actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, char expected, char actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two chars are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(char expected, char actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, short expected, short actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two shorts are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(short expected, short actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(String message, int expected, int actual) {
    Assert.assertEquals(message, expected, actual);
  }

  /**
   * Asserts that two ints are equal.
   */
  @SuppressWarnings("deprecation")
  public static void assertEquals(int expected, int actual) {
    Assert.assertEquals(expected, actual);
  }

  /**
   * Asserts that an object isn't null.
   */
  @SuppressWarnings("deprecation")
  public static void assertNotNull(Object object) {
    Assert.assertNotNull(object);
  }

  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertNotNull(String message, Object object) {
    Assert.assertNotNull(message, object);
  }

  /**
   * Asserts that an object is null. If it isn't an {@link AssertionError} is
   * thrown.
   * Message contains: Expected: <null> but was: object
   *
   * @param object Object to check or <code>null</code>
   */
  @SuppressWarnings("deprecation")
  public static void assertNull(Object object) {
    Assert.assertNull(object);
  }

  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertNull(String message, Object object) {
    Assert.assertNull(message, object);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertSame(String message, Object expected, Object actual) {
    Assert.assertSame(message, expected, actual);
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  @SuppressWarnings("deprecation")
  public static void assertSame(Object expected, Object actual) {
    Assert.assertSame(expected, actual);
  }

  /**
   * Asserts that two objects do not refer to the same object. If they do
   * refer to the same object an AssertionFailedError is thrown with the
   * given message.
   */
  @SuppressWarnings("deprecation")
  public static void assertNotSame(String message, Object expected, Object actual) {
    Assert.assertNotSame(message, expected, actual);
  }

  /**
   * Asserts that two objects do not refer to the same object. If they do
   * refer to the same object an AssertionFailedError is thrown.
   */
  @SuppressWarnings("deprecation")
  public static void assertNotSame(Object expected, Object actual) {
    Assert.assertNotSame(expected, actual);
  }

  @SuppressWarnings("deprecation")
  public static void failSame(String message) {
    Assert.failSame(message);
  }

  @SuppressWarnings("deprecation")
  public static void failNotSame(String message, Object expected, Object actual) {
    Assert.failNotSame(message, expected, actual);
  }

  @SuppressWarnings("deprecation")
  public static void failNotEquals(String message, Object expected, Object actual) {
    Assert.failNotEquals(message, expected, actual);
  }

  @SuppressWarnings("deprecation")
  public static String format(String message, Object expected, Object actual) {
    return Assert.format(message, expected, actual);
  }
}
