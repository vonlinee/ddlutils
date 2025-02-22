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

import org.apache.ddlutils.DdlUtilsException;

import java.sql.Date;
import java.sql.Types;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Converts between {@link java.sql.Date} and {@link java.lang.String} using the standard
 * representation "yyyy", or "yyyy-mm", or "yyyy-mm-dd".
 *
 * @version $Revision: 289996 $
 */
public class DateConverter implements SqlTypeConverter {
  /**
   * The regular expression pattern for the parsing of ISO dates.
   */
  private final Pattern _datePattern;
  /**
   * The calendar object to convert to/from dates.
   */
  private final Calendar _calendar;

  /**
   * Creates a new date converter object.
   */
  public DateConverter() {
    try {
      _datePattern = Pattern.compile("(\\d{2,4})(?:-(\\d{2}))?(?:-(\\d{2}))?.*");
    } catch (PatternSyntaxException ex) {
      throw new DdlUtilsException(ex);
    }

    _calendar = Calendar.getInstance();
    _calendar.setLenient(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object convertFromString(String textRep, int sqlTypeCode) throws ConversionException {
    if (sqlTypeCode != Types.DATE) {
      return textRep;
    } else if (textRep != null) {
      // we're not using {@link java.sql.Date#valueOf(String)} as this method is too strict
      // it only parses the full spec "yyyy-mm-dd"
      Matcher matcher = _datePattern.matcher(textRep);
      int year = 1970;
      int month = 1;
      int day = 1;

      if (matcher.matches()) {
        int numGroups = matcher.groupCount();

        try {
          year = Integer.parseInt(matcher.group(1));
          if ((numGroups >= 2) && (matcher.group(2) != null)) {
            month = Integer.parseInt(matcher.group(2));
          }
          if ((numGroups >= 3) && (matcher.group(3) != null)) {
            day = Integer.parseInt(matcher.group(3));
          }
        } catch (NumberFormatException ex) {
          throw new ConversionException("Not a valid date : " + textRep, ex);
        }
        _calendar.clear();
        try {
          _calendar.set(year, month - 1, day);
          return new Date(_calendar.getTimeInMillis());
        } catch (IllegalArgumentException ex) {
          throw new ConversionException("Not a valid date : " + textRep, ex);
        }
      } else {
        throw new ConversionException("Not a valid date : " + textRep);
      }
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String convertToString(Object obj, int sqlTypeCode) throws ConversionException {
    String result = null;

    if (obj != null) {
      if (!(obj instanceof Date)) {
        throw new ConversionException("Expected object of type java.sql.Date, but instead received " + obj.getClass().getName());
      }
      result = obj.toString();
    }
    return result;
  }
}
