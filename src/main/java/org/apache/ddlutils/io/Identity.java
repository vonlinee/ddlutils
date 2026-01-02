package org.apache.ddlutils.io;

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

import org.apache.ddlutils.model.Table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Stores the identity of a database object as defined by its primary keys. Is used
 * by {@link org.apache.ddlutils.io.DataToDatabaseSink} class for inserting objects
 * in the correct order.
 *
 * @version $Revision: 289996 $
 */
public class Identity {
  /**
   * The table.
   */
  private final Table table;
  /**
   * The identity columns and their values.
   */
  private final HashMap<String, Object> columnValues = new HashMap<>();
  /**
   * The optional foreign key name whose referenced object this identity represents.
   */
  private String fkName;

  /**
   * Creates a new identity object for the given table.
   *
   * @param table The name of the table
   */
  public Identity(Table table) {
    this.table = table;
  }

  /**
   * Creates a new identity object for the given table.
   *
   * @param table  The table
   * @param fkName The name of the foreign key whose referenced object this identity represents
   */
  public Identity(Table table, String fkName) {
    this.table = table;
    this.fkName = fkName;
  }

  /**
   * Returns the table that this identity is for.
   *
   * @return The table
   */
  public Table getTable() {
    return table;
  }

  /**
   * Returns the name of the foreign key whose referenced object this identity represents. This
   * name is <code>null</code> if the identity is not for a foreign key, or if the foreign key
   * was unnamed.
   *
   * @return The foreign key name
   */
  public String getForeignKeyName() {
    return fkName;
  }

  /**
   * Specifies the value of the indicated identity columns.
   *
   * @param name  The column name
   * @param value The value for the column
   */
  public void setColumnValue(String name, Object value) {
    columnValues.put(name, value);
  }

  /**
   * Returns the value of the indicated identity columns.
   *
   * @param name The column name
   * @return The column's value
   */
  public Object getColumnValue(String name) {
    return columnValues.get(name);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Identity)) {
      return false;
    }

    Identity otherIdentity = (Identity) obj;

    if (!table.equals(otherIdentity.table)) {
      return false;
    }
    if (columnValues.size() != otherIdentity.columnValues.size()) {
      return false;
    }
    for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
      Object otherValue = otherIdentity.columnValues.get(entry.getKey());

      if (entry.getValue() == null) {
        if (otherValue != null) {
          return false;
        }
      } else {
        if (!entry.getValue().equals(otherValue)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();

    buffer.append(table.getName());
    buffer.append(":");
    for (Iterator<Map.Entry<String, Object>> it = columnValues.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Object> entry = it.next();

      buffer.append(entry.getKey());
      buffer.append("=");
      buffer.append(entry.getValue());
      if (it.hasNext()) {
        buffer.append(";");
      }
    }
    return buffer.toString();
  }
}
