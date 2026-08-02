package org.apache.ddlutils.model;

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

import org.apache.ddlutils.util.StringUtilsExt;

import java.util.ArrayList;

/**
 * Base class for indexes.
 *
 * @version $Revision: $
 */
abstract class IndexImplBase implements Index {
  /**
   * The name of the index.
   */
  protected String name;
  /**
   * The columns making up the index.
   */
  protected ArrayList<IndexColumn> columns = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getColumnCount() {
    return columns.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IndexColumn getColumn(int idx) {
    return columns.get(idx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IndexColumn[] getColumns() {
    return columns.toArray(new IndexColumn[0]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasColumn(Column column) {
    for (int idx = 0; idx < columns.size(); idx++) {
      IndexColumn curColumn = getColumn(idx);

      if (column.equals(curColumn.getColumn())) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasColumn(String columnName, boolean caseSensitive) {
    for (int idx = 0; idx < columns.size(); idx++) {
      IndexColumn curColumn = getColumn(idx);

      if (StringUtilsExt.equals(columnName, curColumn.getName(), caseSensitive)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addColumn(IndexColumn column) {
    if (column != null) {
      for (int idx = 0; idx < columns.size(); idx++) {
        IndexColumn curColumn = getColumn(idx);

        if (curColumn.getOrdinalPosition() > column.getOrdinalPosition()) {
          columns.add(idx, column);
          return;
        }
      }
      columns.add(column);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeColumn(IndexColumn column) {
    columns.remove(column);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeColumn(int idx) {
    columns.remove(idx);
  }

  protected boolean equalsTo(Index otherIndex) {
    boolean checkName = name != null && !name.isEmpty() &&
                        otherIndex.getName() != null && !otherIndex.getName().isEmpty();

    if ((!checkName || name.equalsIgnoreCase(otherIndex.getName())) &&
        getColumnCount() == otherIndex.getColumnCount()) {
      for (int idx = 0; idx < getColumnCount(); idx++) {
        if (!getColumn(idx).equalsIgnoreCase(otherIndex.getColumn(idx))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
