package org.apache.ddlutils.io.converters;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.sql.Date;
import java.sql.Types;
import java.util.Calendar;

/**
 * Tests the {@link DateConverter}.
 *
 * @version $Revision: 1.0 $
 */
public class TestDateConverter {
  /**
   * The tested date converter.
   */
  private DateConverter _dateConverter;

  /**
   * {@inheritDoc}
   */
  @Before
  public void setUp() throws Exception {
    _dateConverter = new DateConverter();
  }

  /**
   * {@inheritDoc}
   */
  @After
  public void tearDown() throws Exception {
    _dateConverter = null;
  }

  /**
   * Tests a normal date string.
   */
  @Test
  public void testNormalConvertFromYearMonthDateString() {
    String textRep = "2005-12-19";
    Calendar cal = Calendar.getInstance();

    cal.setLenient(false);
    cal.clear();
    cal.set(2005, Calendar.DECEMBER, 19);

    Object result = _dateConverter.convertFromString(textRep, Types.DATE);

    Assertions.assertTrue(result instanceof Date);
    Assertions.assertEquals(cal.getTimeInMillis(), ((Date) result).getTime());
  }

  /**
   * Tests a date string that has no day.
   */
  @Test
  public void testNormalConvertFromYearMonthString() {
    String textRep = "2005-12";
    Calendar cal = Calendar.getInstance();

    cal.setLenient(false);
    cal.clear();
    cal.set(2005, Calendar.DECEMBER, 1);

    Object result = _dateConverter.convertFromString(textRep, Types.DATE);

    Assertions.assertTrue(result instanceof Date);
    Assertions.assertEquals(cal.getTimeInMillis(), ((Date) result).getTime());
  }

  /**
   * Tests a date string that has only a year.
   */
  @Test
  public void testNormalConvertFromYearString() {
    String textRep = "2005";
    Calendar cal = Calendar.getInstance();

    cal.clear();
    cal.set(2005, Calendar.JANUARY, 1);

    Object result = _dateConverter.convertFromString(textRep, Types.DATE);

    Assertions.assertTrue(result instanceof Date);
    Assertions.assertEquals(cal.getTimeInMillis(), ((Date) result).getTime());
  }

  /**
   * Tests a full datetime string.
   */
  @Test
  public void testNormalConvertFromFullDateTimeString() {
    String textRep = "2005-06-07 10:11:12";
    Calendar cal = Calendar.getInstance();

    cal.clear();
    cal.set(2005, Calendar.JUNE, 7);

    Object result = _dateConverter.convertFromString(textRep, Types.DATE);

    Assertions.assertTrue(result instanceof Date);
    Assertions.assertEquals(cal.getTimeInMillis(), ((Date) result).getTime());
  }

  /**
   * Tests converting with an invalid SQL type.
   */
  @Test
  public void testConvertFromStringWithInvalidSqlType() {
    String textRep = "2005-12-19";
    Object result = _dateConverter.convertFromString(textRep, Types.INTEGER);

    // Make sure that the text representation is returned since SQL type was not a DATE
    Assertions.assertNotNull(result);
    Assertions.assertEquals(textRep, result);
  }

  /**
   * Tests handling of null.
   */
  @Test
  public void testConvertFromStringWithNullTextRep() {
    Object result = _dateConverter.convertFromString(null, Types.DATE);
    Assertions.assertNull(result);
  }

  /**
   * Tests an invalid date.
   */
  @Test
  public void testConvertFromStringWithInvalidTextRep() {
    String textRep = "9999-99-99";

    try {
      _dateConverter.convertFromString(textRep, Types.DATE);
      Assert.fail("ConversionException expected");
    } catch (ConversionException ex) {
      // we expect the exception
    }
  }

  /**
   * Tests an invalid date that contains non-numbers.
   */
  @Test
  public void testConvertFromStringWithAlphaTextRep() {
    String textRep = "aaaa-bb-cc";

    try {
      _dateConverter.convertFromString(textRep, Types.DATE);
      Assert.fail("ConversionException expected");
    } catch (ConversionException ex) {
      // we expect the exception
    }
  }

  /**
   * Tests converting a normal date to a string.
   */
  @Test
  public void testNormalConvertToString() {
    Calendar cal = Calendar.getInstance();

    cal.setLenient(false);
    cal.clear();
    cal.set(2005, Calendar.DECEMBER, 19);

    Date date = new Date(cal.getTimeInMillis());
    String result = _dateConverter.convertToString(date, Types.DATE);

    Assertions.assertNotNull(result);
    Assertions.assertEquals("2005-12-19", result);
  }

  /**
   * Tests converting a null.
   */
  @Test
  public void testConvertToStringWithNullDate() {
    String result = _dateConverter.convertToString(null, Types.DATE);

    Assertions.assertNull(result);
  }

  /**
   * Tests converting a {@link java.util.Date}.
   */
  @Test
  public void testConvertToStringWithWrongType() {
    java.util.Date date = new java.util.Date();

    try {
      _dateConverter.convertToString(date, Types.DATE);
      Assert.fail("ConversionException expected");
    } catch (ConversionException expected) {
      // we expect the exception
    }
  }
}
