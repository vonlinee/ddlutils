package org.apache.ddlutils.platform.hsqldb;

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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

import java.sql.SQLException;
import java.util.Map;

/**
 * Reads a database model from a HsqlDb database.
 *
 * @version $Revision: $
 */
public class HsqlDbModelReader extends JdbcModelReader {
  /**
   * Creates a new model reader for HsqlDb databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public HsqlDbModelReader(Platform platform) {
    super(platform);
    setDefaultCatalogPattern(null);
    setDefaultSchemaPattern(null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Table readTable(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Table table = super.readTable(metaData, values);

    if (table != null) {
      // For at least version 1.7.2 we have to determine the auto-increment columns
      // from a result set metadata because the database does not put this info
      // into the database metadata
      // Since Hsqldb only allows IDENTITY for primary key columns, we restrict
      // our search to those columns
      determineAutoIncrementFromResultSetMetaData(table, table.getPrimaryKeyColumns());
    }

    return table;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Column column = super.readColumn(metaData, values);

    if (TypeMap.isTextType(column.getTypeCode()) &&
      (column.getDefaultValue() != null)) {
      column.setDefaultValue(unescape(column.getDefaultValue(), "'", "''"));
    }
    return column;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isInternalForeignKeyIndex(DatabaseMetaDataWrapper metaData, Table table, ForeignKey fk, Index index) {
    String name = index.getName();

    return (name != null) && name.startsWith("SYS_IDX_");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isInternalPrimaryKeyIndex(DatabaseMetaDataWrapper metaData, Table table, Index index) {
    String name = index.getName();

    return (name != null) && (name.startsWith("SYS_PK_") || name.startsWith("SYS_IDX_"));
  }
}
