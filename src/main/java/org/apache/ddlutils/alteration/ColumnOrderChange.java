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

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents the change of the order of the columns of a table.
 *
 * @version $Revision: $
 */
public class ColumnOrderChange extends TableChangeImplBase {
  /**
   * The map containing the new positions keyed by the source columns.
   */
  private final Map<String, Integer> _newPositions;

  /**
   * Creates a new change object.
   *
   * @param tableName    The name of the table whose primary key is to be changed
   * @param newPositions The map containing the new positions keyed by the source column names
   */
  public ColumnOrderChange(String tableName, Map<String, Integer> newPositions) {
    super(tableName);
    _newPositions = newPositions;
  }

  /**
   * Returns the new position of the given source column.
   *
   * @param sourceColumnName The column's name
   * @param caseSensitive    Whether case of the column name matters
   * @return The new position or -1 if no position is marked for the column
   */
  public int getNewPosition(String sourceColumnName, boolean caseSensitive) {
    Integer newPos = null;

    if (caseSensitive) {
      newPos = _newPositions.get(sourceColumnName);
    } else {
      for (Map.Entry<String, Integer> entry : _newPositions.entrySet()) {
        if (sourceColumnName.equalsIgnoreCase(entry.getKey())) {
          newPos = entry.getValue();
          break;
        }
      }
    }

    return newPos == null ? -1 : newPos;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void apply(Database database, boolean caseSensitive) {
    Table table = findChangedTable(database, caseSensitive);
    ArrayList<Column> newColumns = new ArrayList<>();

    for (int idx = 0; idx < table.getColumnCount(); idx++) {
      newColumns.add(table.getColumn(idx));
    }
    for (int idx = 0; idx < table.getColumnCount(); idx++) {
      Column column = table.getColumn(idx);
      int newPos = getNewPosition(column.getName(), caseSensitive);

      if (newPos >= 0) {
        newColumns.set(newPos, column);
      }
    }
    table.removeAllColumns();
    table.addColumns(newColumns);
  }
}
