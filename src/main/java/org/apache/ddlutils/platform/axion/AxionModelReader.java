package org.apache.ddlutils.platform.axion;

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
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;
import org.apache.ddlutils.platform.JdbcModelReader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Reads a database model from an Axion database.
 *
 * @version $Revision: $
 */
public class AxionModelReader extends JdbcModelReader {
  /**
   * Creates a new model reader for Axion databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public AxionModelReader(Platform platform) {
    super(platform);
    setDefaultCatalogPattern(null);
    setDefaultSchemaPattern(null);
    setDefaultTablePattern("%");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Collection<String> readPrimaryKeyNames(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException {
    // Axion still does not support DatabaseMetaData#getPrimaryKeys
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Collection<ForeignKey> readForeignKeys(DatabaseMetaDataWrapper metaData, String tableName) throws SQLException {
    // Axion still does not support DatabaseMetaData#getImportedKeys or #getExportedKeys
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void removeSystemIndices(DatabaseMetaDataWrapper metaData, Table table) throws SQLException {
    // Axion's JDBC driver does not support primary key reading, so we have to filter at this level
    for (int indexIdx = 0; indexIdx < table.getIndexCount(); ) {
      Index index = table.getIndex(indexIdx);

      // also, Axion's internal indices are not unique
      if (index.getName().startsWith("SYS_")) {
        table.removeIndex(indexIdx);
      } else {
        indexIdx++;
      }
    }
  }
}
