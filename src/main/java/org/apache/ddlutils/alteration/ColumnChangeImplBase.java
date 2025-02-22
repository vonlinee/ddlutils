package org.apache.ddlutils.alteration;

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

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Base class for changes to columns.
 *
 * @version $Revision: $
 */
public abstract class ColumnChangeImplBase extends TableChangeImplBase
  implements ColumnChange {
  /**
   * The column's name.
   */
  private final String _columnName;

  /**
   * Creates a new change object.
   *
   * @param tableName  The name of the table to remove the column from
   * @param columnName The column's name
   */
  public ColumnChangeImplBase(String tableName, String columnName) {
    super(tableName);
    _columnName = columnName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getChangedColumn() {
    return _columnName;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Column findChangedColumn(Database model, boolean caseSensitive) {
    Table table = findChangedTable(model, caseSensitive);

    return table == null ? null : table.findColumn(_columnName, caseSensitive);
  }
}
