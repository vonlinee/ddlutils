package org.apache.ddlutils.model;

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

import java.sql.JDBCType;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A class that maps SQL type names to their JDBC type ID found in
 * {@link java.sql.Types} and vice versa.
 *
 * @version $Revision$
 */
public abstract class TypeMap {

  /**
   * Maps type names to the corresponding {@link java.sql.Types} constants.
   */
  private static final HashMap<String, Integer> _typeNameToTypeCode = new HashMap<>();
  /**
   * Maps {@link java.sql.Types} type code constants to the corresponding type names.
   */
  private static final HashMap<Integer, String> _typeCodeToTypeName = new HashMap<>();
  /**
   * Contains the types per category.
   */
  private static final HashMap<JdbcTypeCategory, Set<Integer>> _typesPerCategory = new HashMap<>();

  static {
    registerJdbcType(JDBCType.ARRAY, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.BIGINT, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.BINARY, JdbcTypeCategory.BINARY);
    registerJdbcType(JDBCType.BIT, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.BLOB, JdbcTypeCategory.BINARY);
    registerJdbcType(JDBCType.BOOLEAN, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.CHAR, JdbcTypeCategory.TEXTUAL);
    registerJdbcType(JDBCType.CLOB, JdbcTypeCategory.TEXTUAL);
    registerJdbcType(JDBCType.DATALINK, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.DATE, JdbcTypeCategory.DATETIME);
    registerJdbcType(JDBCType.DECIMAL, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.DISTINCT, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.DOUBLE, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.FLOAT, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.INTEGER, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.JAVA_OBJECT, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.LONGVARBINARY, JdbcTypeCategory.BINARY);
    registerJdbcType(JDBCType.LONGVARCHAR, JdbcTypeCategory.TEXTUAL);
    registerJdbcType(JDBCType.NULL, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.NUMERIC, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.OTHER, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.REAL, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.REF, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.SMALLINT, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.STRUCT, JdbcTypeCategory.SPECIAL);
    registerJdbcType(JDBCType.TIME, JdbcTypeCategory.DATETIME);
    registerJdbcType(JDBCType.TIMESTAMP, JdbcTypeCategory.DATETIME);
    registerJdbcType(JDBCType.TINYINT, JdbcTypeCategory.NUMERIC);
    registerJdbcType(JDBCType.VARBINARY, JdbcTypeCategory.BINARY);
    registerJdbcType(JDBCType.VARCHAR, JdbcTypeCategory.TEXTUAL);

    for (JDBCType type : JDBCType.values()) {
      if (_typeCodeToTypeName.containsKey(type.getVendorTypeNumber())) {
        registerJdbcType(type, JdbcTypeCategory.OTHER);
      }
    }

    // Torque/Turbine extensions which we only support when reading from an XML schema
    _typeNameToTypeCode.put("BOOLEANINT", Types.TINYINT);
    _typeNameToTypeCode.put("BOOLEANCHAR", Types.CHAR);
  }

  /**
   * Returns all supported JDBC types.
   *
   * @return The type codes ({@link java.sql.Types} constants)
   */
  public static int[] getSupportedJdbcTypes() {
    int[] typeCodes = new int[_typeCodeToTypeName.size()];
    int idx = 0;

    for (Iterator<Integer> it = _typeCodeToTypeName.keySet().iterator(); it.hasNext(); idx++) {
      typeCodes[idx] = it.next();
    }
    return typeCodes;
  }

  /**
   * Returns the JDBC type code (one of the {@link java.sql.Types} constants) that
   * corresponds to the given JDBC type name.
   *
   * @param typeName The JDBC type name (case is ignored)
   * @return The type code or <code>null</code> if the type is unknown
   */
  public static Integer getJdbcTypeCode(String typeName) {
    return _typeNameToTypeCode.get(typeName.toUpperCase());
  }

  /**
   * Returns the JDBC type name that corresponds to the given type code
   * (one of the {@link java.sql.Types} constants).
   *
   * @param typeCode The type code
   * @return The JDBC type name (one of the constants in this class) or
   * <code>null</code> if the type is unknown
   */
  public static String getJdbcTypeName(int typeCode) {
    return _typeCodeToTypeName.get(typeCode);
  }

  /**
   * Registers a jdbc type.
   *
   * @param typeCode The type code (one of the {@link java.sql.Types} constants)
   * @param typeName The type name (case is ignored)
   * @param category The type category
   */
  protected static void registerJdbcType(int typeCode, String typeName, JdbcTypeCategory category) {
    _typeNameToTypeCode.put(typeName.toUpperCase(), typeCode);
    _typeCodeToTypeName.put(typeCode, typeName.toUpperCase());
    _typesPerCategory.computeIfAbsent(category, k -> new HashSet<>()).add(typeCode);
  }

  protected static void registerJdbcType(JDBCType jdbcType, JdbcTypeCategory category) {
    registerJdbcType(jdbcType.getVendorTypeNumber(), jdbcType.getName(), category);
  }

  /**
   * Determines whether the given jdbc type (one of the {@link java.sql.Types} constants)
   * is a numeric type.
   *
   * @param jdbcTypeCode The type code
   * @return <code>true</code> if the type is a numeric one
   */
  public static boolean isNumericType(int jdbcTypeCode) {
    Set<Integer> typesInCategory = _typesPerCategory.get(JdbcTypeCategory.NUMERIC);
    return typesInCategory != null && typesInCategory.contains(jdbcTypeCode);
  }

  /**
   * Determines whether the given jdbc type (one of the {@link java.sql.Types} constants)
   * is a date/time type.
   *
   * @param jdbcTypeCode The type code
   * @return <code>true</code> if the type is a numeric one
   */
  public static boolean isDateTimeType(int jdbcTypeCode) {
    Set<Integer> typesInCategory = _typesPerCategory.get(JdbcTypeCategory.DATETIME);
    return typesInCategory != null && typesInCategory.contains(jdbcTypeCode);
  }

  /**
   * Determines whether the given jdbc type (one of the {@link java.sql.Types} constants)
   * is a text type.
   *
   * @param jdbcTypeCode The type code
   * @return <code>true</code> if the type is a text one
   */
  public static boolean isTextType(int jdbcTypeCode) {
    Set<Integer> typesInCategory = _typesPerCategory.get(JdbcTypeCategory.TEXTUAL);
    return typesInCategory != null && typesInCategory.contains(jdbcTypeCode);
  }

  /**
   * Determines whether the given jdbc type (one of the {@link java.sql.Types} constants)
   * is a binary type.
   *
   * @param jdbcTypeCode The type code
   * @return <code>true</code> if the type is a binary one
   */
  public static boolean isBinaryType(int jdbcTypeCode) {
    Set<Integer> typesInCategory = _typesPerCategory.get(JdbcTypeCategory.BINARY);
    return typesInCategory != null && typesInCategory.contains(jdbcTypeCode);
  }

  /**
   * Determines whether the given SQL type (one of the {@link java.sql.Types} constants)
   * is a special type.
   *
   * @param jdbcTypeCode The type code
   * @return <code>true</code> if the type is a special one
   */
  public static boolean isSpecialType(int jdbcTypeCode) {
    Set<Integer> typesInCategory = _typesPerCategory.get(JdbcTypeCategory.SPECIAL);
    return typesInCategory != null && typesInCategory.contains(jdbcTypeCode);
  }
}
