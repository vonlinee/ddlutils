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

import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

/**
 * Represents the addition of a column to a table.
 *
 * @version $Revision: $
 */
public class AddColumnChange extends TableChangeImplBase {
  /**
   * The new column.
   */
  private final Column _newColumn;
  /**
   * The name of the column after which the new column should be added.
   */
  private final String _previousColumnName;
  /**
   * The name of the column before which the new column should be added.
   */
  private final String _nextColumnName;

  /**
   * Creates a new change object.
   *
   * @param tableName          The name of the table to add the column to
   * @param newColumn          The new column
   * @param previousColumnName The name of the column after which the new column should be added
   * @param nextColumnName     The name of the column before which the new column should be added
   */
  public AddColumnChange(String tableName, Column newColumn, String previousColumnName, String nextColumnName) {
    super(tableName);
    _newColumn = newColumn;
    _previousColumnName = previousColumnName;
    _nextColumnName = nextColumnName;
  }

  /**
   * Returns the new column.
   *
   * @return The new column
   */
  public Column getNewColumn() {
    return _newColumn;
  }

  /**
   * Returns the name of the column after which the new column should be added.
   *
   * @return The name of the previous column
   */
  public String getPreviousColumn() {
    return _previousColumnName;
  }

  /**
   * Returns the name of the column before which the new column should be added.
   *
   * @return The name of the next column
   */
  public String getNextColumn() {
    return _nextColumnName;
  }

  /**
   * Determines whether the column is added at the end (when applied in the order
   * of creation of the changes).
   *
   * @return <code>true</code> if the column is added at the end
   */
  public boolean isAtEnd() {
    return _nextColumnName == null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void apply(Database model, boolean caseSensitive) {
    Table table = findChangedTable(model, caseSensitive);
    Column newColumn = new CloneHelper().clone(_newColumn, true);

    if (_previousColumnName != null) {
      Column prevColumn = table.findColumn(_previousColumnName, caseSensitive);
      int idx = table.getColumnIndex(prevColumn) + 1;

      table.addColumn(idx, newColumn);
    } else if (_nextColumnName != null) {
      table.addColumn(0, newColumn);
    } else {
      table.addColumn(newColumn);
    }
  }
}
