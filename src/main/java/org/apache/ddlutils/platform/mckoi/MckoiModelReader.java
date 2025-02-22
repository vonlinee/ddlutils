package org.apache.ddlutils.platform.mckoi;

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
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;
import org.apache.ddlutils.util.Log;
import org.apache.ddlutils.util.LogFactory;
import org.apache.ddlutils.util.OrderedMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a database model from a Mckoi database.
 *
 * @version $Revision: $
 */
public class MckoiModelReader extends JdbcModelReader {
  /**
   * The log.
   */
  protected Log _log = LogFactory.getLog(MckoiModelReader.class);

  /**
   * Creates a new model reader for Mckoi databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public MckoiModelReader(Platform platform) {
    super(platform);
    setDefaultCatalogPattern(null);
    setDefaultSchemaPattern(null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Table readTable(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    // Mckoi does not currently return unique indices in the metadata, so we have to query
    // internal tables to get this info
    final String query =
      "SELECT uniqueColumns.column, uniqueColumns.seq_no, uniqueInfo.name" +
        " FROM SYS_INFO.sUSRUniqueColumns uniqueColumns, SYS_INFO.sUSRUniqueInfo uniqueInfo" +
        " WHERE uniqueColumns.un_id = uniqueInfo.id AND uniqueInfo.table = ?";
    final String queryWithSchema =
      query + " AND uniqueInfo.schema = ?";

    Table table = super.readTable(metaData, values);

    if (table != null) {
      Map<String, Index> indices = new OrderedMap<>();
      PreparedStatement stmt = null;

      try {
        stmt = getConnection().prepareStatement(table.getSchema() == null ? query : queryWithSchema);
        stmt.setString(1, table.getName());
        if (table.getSchema() != null) {
          stmt.setString(2, table.getSchema());
        }

        ResultSet resultSet = stmt.executeQuery();
        Map<String, Object> indexValues = new HashMap<>();

        indexValues.put("NON_UNIQUE", Boolean.FALSE);
        while (resultSet.next()) {
          indexValues.put("COLUMN_NAME", resultSet.getString(1));
          indexValues.put("ORDINAL_POSITION", resultSet.getShort(2));
          indexValues.put("INDEX_NAME", resultSet.getString(3));

          readIndex(metaData, indexValues, indices);
        }
      } finally {
        closeStatement(stmt);
      }

      table.addIndices(indices.values());
    }

    return table;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Column column = super.readColumn(metaData, values);

    if (column.getSize() != null) {
      if (column.getSizeAsInt() <= 0) {
        column.setSize(null);
      }
    }

    String defaultValue = column.getDefaultValue();

    if (defaultValue != null) {
      if (defaultValue.toLowerCase().startsWith("nextval('") ||
        defaultValue.toLowerCase().startsWith("uniquekey('")) {
        column.setDefaultValue(null);
        column.setAutoIncrement(true);
      } else if (TypeMap.isTextType(column.getTypeCode())) {
        column.setDefaultValue(unescape(column.getDefaultValue(), "'", "\\'"));
      }
    }
    return column;
  }
}
