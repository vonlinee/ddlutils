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

import org.apache.ddlutils.data.ConvertUtils;

import java.math.BigDecimal;
import java.sql.Types;

/**
 * Converts between the various number types (including boolean types) and {@link java.lang.String}.
 *
 * @version $Revision: 289996 $
 */
public class NumberConverter implements SqlTypeConverter {
  /**
   * {@inheritDoc}
   */
  @Override
  public Object convertFromString(String textRep, int sqlTypeCode) throws ConversionException {
    if (textRep == null) {
      return null;
    } else {
      Class<?> targetClass = null;

      switch (sqlTypeCode) {
        case Types.BIGINT:
          targetClass = Long.class;
          break;
        case Types.BIT:
        case Types.BOOLEAN:
          targetClass = Boolean.class;
          break;
        case Types.DECIMAL:
        case Types.NUMERIC:
          targetClass = BigDecimal.class;
          break;
        case Types.DOUBLE:
        case Types.FLOAT:
          targetClass = Double.class;
          break;
        case Types.INTEGER:
          targetClass = Integer.class;
          break;
        case Types.REAL:
          targetClass = Float.class;
          break;
        case Types.SMALLINT:
        case Types.TINYINT:
          targetClass = Short.class;
          break;
      }
      return targetClass == null ? textRep : ConvertUtils.convert(textRep, targetClass);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String convertToString(Object obj, int sqlTypeCode) throws ConversionException {
    if (obj == null) {
      return null;
    } else if (sqlTypeCode == Types.BIT) {
      return (Boolean) obj ? "1" : "0";
    } else {
      return obj.toString();
    }
  }
}
