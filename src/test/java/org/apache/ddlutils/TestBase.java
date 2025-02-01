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
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.util.DatabaseTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.StringReader;

/**
 * Base class for DdlUtils tests.
 *
 * @version $Revision: $
 */
public abstract class TestBase {
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

  @Before
  public void beforeEach() throws Exception {
    this.setUp();
  }

  @After
  public void afterEach() throws Exception {
    this.tearDown();
  }

  /**
   * Parses the database defined in the given XML definition.
   *
   * @param dbDef The database XML definition
   * @return The database model
   */
  protected Database parseDatabaseFromString(String dbDef) {
    DatabaseIO dbIO = new DatabaseIO();

    dbIO.setUseInternalDtd(true);
    dbIO.setValidateXml(true);
    return dbIO.read(new StringReader(dbDef));
  }

  /**
   * Compares the two strings but ignores any whitespace differences. It also
   * recognizes special delimiter chars.
   *
   * @param expected The expected string
   * @param actual   The actual string
   */
  protected void assertEqualsIgnoringWhitespaces(String expected, String actual) {
    String processedExpected = compressWhitespaces(expected);
    String processedActual = compressWhitespaces(actual);
    Assert.assertEquals(processedExpected, processedActual);
  }

  /**
   * Compresses the whitespaces in the given string to a single space. Also
   * recognizes special delimiter chars and removes whitespaces before them.
   *
   * @param original The original string
   * @return The resulting string
   */
  protected String compressWhitespaces(String original) {
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

  protected void setUp() throws Exception {

  }

  protected void tearDown() throws Exception {

  }

  public void setName(String name) {

  }

  protected final String readFileToString(String file) {
    return DatabaseTestHelper.readString(getClass(), file);
  }
}
