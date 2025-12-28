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

package org.apache.ddlutils.platform.hsqldb;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.sql.SqlUtils;
import org.apache.ddlutils.util.CollectionUtils;
import org.apache.ddlutils.util.JdbcUtils;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a database model from a HsqlDb database (v2.4.1).
 */
class HsqlDbV241ModelReader extends HsqlDbModelReader {
  /**
   * Creates a new model reader for HsqlDb databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public HsqlDbV241ModelReader(Platform platform) {
    super(platform);
    setDefaultCatalogPattern(null);
    setDefaultSchemaPattern(null);
  }

  @Override
  protected Table readTable(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Table table = super.readTable(metaData, values);

    final Map<String, Column> nameColumnMap = new HashMap<>();
    for (Column column : table.getColumns()) {
      if (JdbcUtils.isTextType(column.getTypeCode())) {
        nameColumnMap.put(column.getName(), column);
      }
    }
    if (!CollectionUtils.isEmpty(nameColumnMap)) {
      /**
       * in hsqldb 2.4.1: LONGVARCHAR will be mapped to VARCHAR(16777216), maybe depends on its version used.
       * but the jdbc type name is still VARCHAR if we read the data type metadata by
       * {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}
       * See: https://stackoverflow.com/questions/41157901/create-table-with-longvarchar-consistently-in-hsqldb-and-oracle
       */
      String sql = String.format("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, IS_NULLABLE, DTD_IDENTIFIER, CHARACTER_MAXIMUM_LENGTH, " +
                                 "NUMERIC_PRECISION, NUMERIC_SCALE\n" +
                                 "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                                 "WHERE TABLE_CATALOG = '%s' AND TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' " +
                                 "AND COLUMN_NAME IN %s"
        , table.getCatalog(), table.getSchema(), table.getName(), JdbcUtils.getInSqlFragment(nameColumnMap.keySet()));
      List<Map<String, Object>> list = JdbcUtils.queryForMapList(getConnection(), sql);

      Map<String, Map<String, Object>> columnTypeMap = new CaseInsensitiveMap<>();
      for (Map<String, Object> map : list) {
        columnTypeMap.put(String.valueOf(map.get("COLUMN_NAME")), map);
      }
      for (String columnName : nameColumnMap.keySet()) {
        Map<String, Object> columnInfo = columnTypeMap.get(columnName);
        if (columnInfo != null && String.valueOf(columnInfo.get("DTD_IDENTIFIER")).startsWith(JDBCType.VARCHAR.getName())) {
          Column column = nameColumnMap.get(columnName);
          if (JDBCType.VARCHAR.getName().equals(column.getType())) {
            Long characterMaximumLength = (Long) columnInfo.get("CHARACTER_MAXIMUM_LENGTH");
            if (characterMaximumLength != null && characterMaximumLength == 16777216) {
              // 16777216
              column.setType(JDBCType.LONGVARCHAR.getName());
            }
          }
        }
      }
    }
    return table;
  }

  @Override
  protected String escapeForSearch(DatabaseMetaDataWrapper metaData, String literalString) throws SQLException {
    return literalString;
  }

  @Override
  protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    final Column column = super.readColumn(metaData, values);

    if (column.getDefaultValue() != null) {
      final String defaultValue = column.getDefaultValue();
      if (column.getTypeCode() == Types.DATE) {
        // DATE'2000-01-01'
        if (defaultValue.startsWith("DATE")) {
          column.setDefaultValue(SqlUtils.unwrapStringValue(defaultValue.substring(4)));
        }
      }
      if (column.getTypeCode() == Types.TIME) {
        // TIME'11:27:03'
        if (defaultValue.startsWith("TIME")) {
          column.setDefaultValue(SqlUtils.unwrapStringValue(defaultValue.substring(4)));
        }
      }
      if (column.getTypeCode() == Types.TIMESTAMP) {
        // TIMESTAMP'1985-06-17 16:17:18.000000'
        if (defaultValue.startsWith("TIMESTAMP")) {
          column.setDefaultValue(SqlUtils.unwrapStringValue(defaultValue.substring("TIMESTAMP".length())));
        }
      }
    }
    return column;
  }
}
