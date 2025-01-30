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

import org.apache.ddlutils.util.StringUtils;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Base class for indexes.
 *
 * @version $Revision: $
 */
abstract class IndexImplBase implements Index {
  /**
   * The name of the index.
   */
  protected String _name;
  /**
   * The columns making up the index.
   */
  protected ArrayList<IndexColumn> _columns = new ArrayList<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return _name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setName(String name) {
    _name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getColumnCount() {
    return _columns.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IndexColumn getColumn(int idx) {
    return _columns.get(idx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IndexColumn[] getColumns() {
    return _columns.toArray(new IndexColumn[0]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasColumn(Column column) {
    for (int idx = 0; idx < _columns.size(); idx++) {
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
    for (int idx = 0; idx < _columns.size(); idx++) {
      IndexColumn curColumn = getColumn(idx);

      if (StringUtils.equals(columnName, curColumn.getName(), caseSensitive)) {
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
      for (int idx = 0; idx < _columns.size(); idx++) {
        IndexColumn curColumn = getColumn(idx);

        if (curColumn.getOrdinalPosition() > column.getOrdinalPosition()) {
          _columns.add(idx, column);
          return;
        }
      }
      _columns.add(column);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeColumn(IndexColumn column) {
    _columns.remove(column);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeColumn(int idx) {
    _columns.remove(idx);
  }
}
