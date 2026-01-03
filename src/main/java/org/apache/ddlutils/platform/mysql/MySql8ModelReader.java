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
package org.apache.ddlutils.platform.mysql;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Attributes;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;
import org.apache.ddlutils.sql.SqlUtils;
import org.apache.ddlutils.util.CollectionUtils;
import org.apache.ddlutils.util.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Reads a database model from a MySql 8 database.
 */
public class MySql8ModelReader extends MySqlModelReader {
  /**
   * Creates a new model reader for MySql 8 databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public MySql8ModelReader(Platform platform) {
    super(platform);
  }

  @Override
  protected String getCatalogToUse(Connection connection, String name, String catalog, String databaseName, String schema, String[] tableTypes) {
    if (name != null) {
      return name;
    }
    if (schema != null) {
      return schema;
    }
    return super.getCatalogToUse(connection, null, catalog, databaseName, null, tableTypes);
  }

  @Override
  protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Column column = super.readColumn(metaData, values);

    // make sure the default value is null when an empty is returned.
    if ("".equals(column.getDefaultValue())) {
      column.setDefaultValue(null);
    }
    return column;
  }

  @Override
  protected String getDetermineAutoIncrementFromResultSetMetaDataSql(Table table, Column[] columnsToCheck) {
    boolean resetSchema = false;
    if (table != null) {
      if (table.getSchema() == null) {
        // MySQL doesn't return the schema name in the result set, so we have to get it from the table name
        // In MySQL, schema is the same as the database/catalog
        table.setSchema(table.getCatalog());
        resetSchema = true;
      }
    }
    String sql = super.getDetermineAutoIncrementFromResultSetMetaDataSql(table, columnsToCheck);
    if (resetSchema) {
      // reset schema to null
      table.setSchema(null);
    }
    return sql;
  }

  @Override
  public Database getDatabase(Connection connection, String name, String catalog, String schema, String[] tableTypes) throws SQLException {
    Database database = super.getDatabase(connection, name, catalog, schema, tableTypes);
    if (database.getTableCount() > 0) {

      final Set<String> tableNames = CollectionUtils.toSet(database.getTables(), Table::getName);
      final String sql = String.format("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME IN %s",
        database.getName(),
        SqlUtils.getInSqlFragment(tableNames)
      );

      Map<String, Object> characterValue = JdbcUtils.queryForMap(connection, "show variables like 'character_set_database'");
      final String charset = (String) characterValue.get("value");
      List<Map<String, Object>> list = JdbcUtils.queryForMapList(connection, sql);
      Map<String, Map<String, Object>> tableNameMap = CollectionUtils.toMap(list, map -> (String) map.get("TABLE_NAME"));
      for (Table table : database.getTables()) {
        Map<String, Object> tableInfoMap = tableNameMap.get(table.getName());

        Attributes attributes = table.getAttributes();
        attributes.set("engine", tableInfoMap.get("engine"));
        attributes.set("table_rows", tableInfoMap.get("table_rows"));
        attributes.set("table_collation", tableInfoMap.get("table_collation"));
        attributes.set("create_time", tableInfoMap.get("create_time"));
        attributes.set("auto_increment", tableInfoMap.get("auto_increment"));
        attributes.set("row_format", tableInfoMap.get("row_format"));
        if (charset != null) {
          attributes.set("charset", charset);
        }
      }
    }
    return database;
  }
}
