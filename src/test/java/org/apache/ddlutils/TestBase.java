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
}
