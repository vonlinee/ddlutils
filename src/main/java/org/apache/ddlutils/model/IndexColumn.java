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

import org.apache.ddlutils.util.EqualsBuilder;
import org.apache.ddlutils.util.HashCodeBuilder;

import java.io.Serializable;

/**
 * Represents a column of an index in the database model.
 *
 * @version $Revision$
 */
public class IndexColumn implements Serializable {
  /**
   * Unique ID for serialization purposes.
   */
  private static final long serialVersionUID = -5009366896427504739L;
  /**
   * The name of the column.
   */
  protected String _name;
  /**
   * The size of the column in the index.
   */
  protected String _size;
  /**
   * The position within the owning index.
   */
  private int _ordinalPosition;
  /**
   * The indexed column.
   */
  private Column _column;

  /**
   * Creates a new index column object.
   */
  public IndexColumn() {
  }

  /**
   * Creates a new index column object.
   *
   * @param column The indexed column
   */
  public IndexColumn(Column column) {
    _column = column;
    _name = column.getName();
  }

  /**
   * Creates a new index column object.
   *
   * @param columnName The name of the corresponding table column
   */
  public IndexColumn(String columnName) {
    _name = columnName;
  }

  /**
   * Returns the position within the owning index.
   *
   * @return The position
   */
  public int getOrdinalPosition() {
    return _ordinalPosition;
  }

  /**
   * Sets the position within the owning index. Please note that you should not
   * change the value once the column has been added to an index.
   *
   * @param position The position
   */
  public void setOrdinalPosition(int position) {
    _ordinalPosition = position;
  }

  /**
   * Returns the name of the column.
   *
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the column.
   *
   * @param name The name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Returns the indexed column.
   *
   * @return The column
   */
  public Column getColumn() {
    return _column;
  }

  /**
   * Sets the indexed column.
   *
   * @param column The column
   */
  public void setColumn(Column column) {
    _column = column;
    _name = (column == null ? null : column.getName());
  }

  /**
   * Returns the size of the column in the index.
   *
   * @return The size
   */
  public String getSize() {
    return _size;
  }

  /**
   * Sets the size of the column in the index.
   *
   * @param size The size
   */
  public void setSize(String size) {
    _size = size;
  }

  /**
   * Compares this index column to the given one while ignoring the case of identifiers.
   *
   * @param other The other index column
   * @return <code>true</code> if this index column is equal (ignoring case) to the given one
   */
  public boolean equalsIgnoreCase(IndexColumn other) {
    return new EqualsBuilder().append(_name.toUpperCase(), other._name.toUpperCase())
      .append(_size, other._size)
      .isEquals();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IndexColumn) {
      IndexColumn other = (IndexColumn) obj;
      return new EqualsBuilder().append(_name, other._name)
        .append(_size, other._size)
        .isEquals();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(_name)
      .append(_size)
      .toHashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Index column [name=" +
      getName() +
      "; size=" +
      getSize() +
      "]";
  }
}
