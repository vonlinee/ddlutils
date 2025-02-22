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
 * Represents the change of the primary key of a table.
 *
 * @version $Revision: $
 */
public class PrimaryKeyChange extends TableChangeImplBase {
  /**
   * The names of the columns making up the new primary key.
   */
  private final String[] _newPrimaryKeyColumns;

  /**
   * Creates a new change object.
   *
   * @param tableName            The name of the table whose primary key is to be changed
   * @param newPrimaryKeyColumns The names of the columns making up the new primary key
   */
  public PrimaryKeyChange(String tableName, String[] newPrimaryKeyColumns) {
    super(tableName);
    if (newPrimaryKeyColumns == null) {
      _newPrimaryKeyColumns = new String[0];
    } else {
      _newPrimaryKeyColumns = new String[newPrimaryKeyColumns.length];

      System.arraycopy(newPrimaryKeyColumns, 0, _newPrimaryKeyColumns, 0, newPrimaryKeyColumns.length);
    }
  }

  /**
   * Returns the names of the columns making up the new primary key.
   *
   * @return The column names
   */
  public String[] getNewPrimaryKeyColumns() {
    String[] result = new String[_newPrimaryKeyColumns.length];

    System.arraycopy(_newPrimaryKeyColumns, 0, result, 0, _newPrimaryKeyColumns.length);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void apply(Database model, boolean caseSensitive) {
    Table table = findChangedTable(model, caseSensitive);
    Column[] pkCols = table.getPrimaryKeyColumns();

    for (Column pkCol : pkCols) {
      pkCol.setPrimaryKey(false);
    }
    for (String newPrimaryKeyColumn : _newPrimaryKeyColumns) {
      Column column = table.findColumn(newPrimaryKeyColumn, caseSensitive);

      column.setPrimaryKey(true);
    }
  }
}
