package org.apache.ddlutils.io;

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

import org.apache.ddlutils.io.converters.SqlTypeConverter;
import org.apache.ddlutils.model.TypeMap;
import org.apache.tools.ant.BuildException;

/**
 * Represents the registration of a data converter for tasks that work on data files.
 *
 * @version $Revision: 289996 $
 */
public class DataConverterRegistration {
  /**
   * The converter.
   */
  private SqlTypeConverter converter;
  /**
   * The SQL type for which the converter shall be registered.
   */
  private int typeCode = Integer.MIN_VALUE;
  /**
   * The table name.
   */
  private String table;
  /**
   * The column name.
   */
  private String column;
  /**
   * Returns the converter.
   *
   * @return The converter
   */
  public SqlTypeConverter getConverter() {
    return converter;
  }

  public void setClassName(String converterClassName) throws BuildException {
    try {
      converter = (SqlTypeConverter) getClass().getClassLoader().loadClass(converterClassName).newInstance();
    } catch (Exception ex) {
      throw new BuildException(ex);
    }
  }
  /**
   * Returns the jdbc type.
   *
   * @return The jdbc type code
   */
  public int getTypeCode() {
    return typeCode;
  }
  /**
   * Sets the jdbc type.
   *
   * @param jdbcTypeName The jdbc type name
   */
  public void setJdbcType(String jdbcTypeName) throws BuildException {
    Integer typeCode = TypeMap.getJdbcTypeCode(jdbcTypeName);

    if (typeCode == null) {
      throw new BuildException("Unknown jdbc type " + jdbcTypeName);
    } else {
      this.typeCode = typeCode;
    }
  }
  /**
   * Returns the column for which this converter is defined.
   *
   * @return The column
   */
  public String getColumn() {
    return column;
  }
  /**
   * Sets the column for which this converter is defined.
   *
   * @param column The column
   */
  public void setColumn(String column) throws BuildException {
    if ((column == null) || (column.isEmpty())) {
      throw new BuildException("Please specify a non-empty column name");
    }
    this.column = column;
  }
  /**
   * Returns the table for whose column this converter is defined.
   *
   * @return The table
   */
  public String getTable() {
    return table;
  }

  public void setTable(String table) throws BuildException {
    if ((table == null) || (table.isEmpty())) {
      throw new BuildException("Please specify a non-empty table name");
    }
    this.table = table;
  }
}
