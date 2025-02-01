package org.apache.ddlutils.data;

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

import java.util.ArrayList;
import java.util.List;

/**
 * SqlDynaClass is a DynaClass which is associated with a persistent
 * Table in a Database.
 *
 * @version $Revision$
 */
public class SqlTableClass extends BasicTableClass {

  /**
   * The table for which this dyna class is defined.
   */
  private final Table _table;
  /**
   * The primary key dyna properties.
   */
  private SqlColumnProperty[] _primaryKeyProperties;
  /**
   * The non-primary key dyna properties.
   */
  private SqlColumnProperty[] _nonPrimaryKeyProperties;

  /**
   * Creates a new dyna class instance for the given table that has the given properties.
   *
   * @param table      The table
   * @param properties The dyna properties
   */
  public SqlTableClass(Table table, SqlColumnProperty[] properties) {
    super(table.getName(), SqlRowObject.class, properties);
    _table = table;
  }

  /**
   * Factory method for creating and initializing a new dyna class instance
   * for the given table.
   *
   * @param table The table
   * @return The dyna class for the table
   */
  public static SqlTableClass newInstance(Table table) {
    List<SqlColumnProperty> properties = new ArrayList<>();

    for (int idx = 0; idx < table.getColumnCount(); idx++) {
      properties.add(new SqlColumnProperty(table.getColumn(idx)));
    }

    SqlColumnProperty[] array = new SqlColumnProperty[properties.size()];

    properties.toArray(array);
    return new SqlTableClass(table, array);
  }

  /**
   * Returns the table for which this dyna class is defined.
   *
   * @return The table
   */
  public Table getTable() {
    return _table;
  }

  // Helper methods
  //-------------------------------------------------------------------------

  /**
   * Returns the table name for which this dyna class is defined.
   *
   * @return The table name
   */
  public String getTableName() {
    return getTable().getName();
  }

  /**
   * Returns the properties of this dyna class.
   *
   * @return The properties
   */
  public SqlColumnProperty[] getSqlDynaProperties() {
    ColumnProperty[] props = getDynaProperties();
    SqlColumnProperty[] result = new SqlColumnProperty[props.length];
    System.arraycopy(props, 0, result, 0, props.length);
    return result;
  }

  /**
   * Returns the properties for the primary keys of the corresponding table.
   *
   * @return The properties
   */
  public SqlColumnProperty[] getPrimaryKeyProperties() {
    if (_primaryKeyProperties == null) {
      initPrimaryKeys();
    }

    SqlColumnProperty[] result = new SqlColumnProperty[_primaryKeyProperties.length];

    System.arraycopy(_primaryKeyProperties, 0, result, 0, _primaryKeyProperties.length);
    return result;
  }

  /**
   * Returns the properties for the non-primary keys of the corresponding table.
   *
   * @return The properties
   */
  public SqlColumnProperty[] getNonPrimaryKeyProperties() {
    if (_nonPrimaryKeyProperties == null) {
      initPrimaryKeys();
    }

    SqlColumnProperty[] result = new SqlColumnProperty[_nonPrimaryKeyProperties.length];

    System.arraycopy(_nonPrimaryKeyProperties, 0, result, 0, _nonPrimaryKeyProperties.length);
    return result;
  }

  // Implementation methods
  //-------------------------------------------------------------------------

  /**
   * Initializes the primary key and non-primary key property arrays.
   */
  protected void initPrimaryKeys() {
    List<SqlColumnProperty> pkProps = new ArrayList<>();
    List<SqlColumnProperty> nonPkProps = new ArrayList<>();
    ColumnProperty[] properties = getDynaProperties();

    for (ColumnProperty property : properties) {
      if (property instanceof SqlColumnProperty) {
        SqlColumnProperty sqlProperty = (SqlColumnProperty) property;
        if (sqlProperty.isPrimaryKey()) {
          pkProps.add(sqlProperty);
        } else {
          nonPkProps.add(sqlProperty);
        }
      }
    }
    _primaryKeyProperties = pkProps.toArray(new SqlColumnProperty[0]);
    _nonPrimaryKeyProperties = nonPkProps.toArray(new SqlColumnProperty[0]);
  }
}
