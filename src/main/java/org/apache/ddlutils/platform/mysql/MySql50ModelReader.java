package org.apache.ddlutils.platform.mysql;

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
import org.apache.ddlutils.platform.DatabaseMetaDataWrapper;

import java.sql.SQLException;
import java.util.Map;

/**
 * Reads a database model from a MySql 5 database.
 *
 * @version $Revision: $
 */
public class MySql50ModelReader extends MySqlModelReader {
  /**
   * Creates a new model reader for MySql 5 databases.
   *
   * @param platform The platform that this model reader belongs to
   */
  public MySql50ModelReader(Platform platform) {
    super(platform);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Column readColumn(DatabaseMetaDataWrapper metaData, Map<String, Object> values) throws SQLException {
    Column column = super.readColumn(metaData, values);

    // make sure the default value is null when an empty is returned.
    if ("".equals(column.getDefaultValue())) {
      column.setDefaultValue(null);
    }
    return column;
  }
}
