package org.apache.ddlutils.dynabean;

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

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.ddlutils.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlDynaClass is a DynaClass which is associated with a persistent
 * Table in a Database.
 *
 * @version $Revision$
 */
public class SqlDynaClass extends BasicDynaClass {
  /**
   * Unique ID for serialization purposes.
   */
  private static final long serialVersionUID = -5768155698352911245L;

  /**
   * The table for which this dyna class is defined.
   */
  private final Table table;
  /**
   * The primary key dyna properties.
   */
  private SqlDynaProperty[] primaryKeyProperties;
  /**
   * The non-primary key dyna properties.
   */
  private SqlDynaProperty[] nonPrimaryKeyProperties;

  /**
   * Creates a new dyna class instance for the given table that has the given properties.
   *
   * @param table      The table
   * @param properties The Dyna properties
   */
  public SqlDynaClass(Table table, SqlDynaProperty[] properties) {
    super(table.getName(), SqlDynaBean.class, properties);
    this.table = table;
  }

  /**
   * Factory method for creating and initializing a new dyna class instance
   * for the given table.
   *
   * @param table The table
   * @return The dyna class for the table
   */
  public static SqlDynaClass newInstance(Table table) {
    List<SqlDynaProperty> properties = new ArrayList<>();

    for (int idx = 0; idx < table.getColumnCount(); idx++) {
      properties.add(new SqlDynaProperty(table.getColumn(idx)));
    }

    SqlDynaProperty[] array = new SqlDynaProperty[properties.size()];

    properties.toArray(array);
    return new SqlDynaClass(table, array);
  }

  /**
   * Returns the table for which this dyna class is defined.
   *
   * @return The table
   */
  public Table getTable() {
    return table;
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
  public SqlDynaProperty[] getSqlDynaProperties() {
    DynaProperty[] props = getDynaProperties();
    SqlDynaProperty[] result = new SqlDynaProperty[props.length];
    System.arraycopy(props, 0, result, 0, props.length);
    return result;
  }

  /**
   * Returns the properties for the primary keys of the corresponding table.
   *
   * @return The properties
   */
  public SqlDynaProperty[] getPrimaryKeyProperties() {
    if (primaryKeyProperties == null) {
      initPrimaryKeys();
    }

    SqlDynaProperty[] result = new SqlDynaProperty[primaryKeyProperties.length];
    System.arraycopy(primaryKeyProperties, 0, result, 0, primaryKeyProperties.length);
    return result;
  }

  /**
   * Returns the properties for the non-primary keys of the corresponding table.
   *
   * @return The properties
   */
  public SqlDynaProperty[] getNonPrimaryKeyProperties() {
    if (nonPrimaryKeyProperties == null) {
      initPrimaryKeys();
    }

    SqlDynaProperty[] result = new SqlDynaProperty[nonPrimaryKeyProperties.length];
    System.arraycopy(nonPrimaryKeyProperties, 0, result, 0, nonPrimaryKeyProperties.length);
    return result;
  }

  // Implementation methods
  //-------------------------------------------------------------------------

  /**
   * Initializes the primary key and non-primary key property arrays.
   */
  protected void initPrimaryKeys() {
    List<SqlDynaProperty> pkProps = new ArrayList<>();
    List<SqlDynaProperty> nonPkProps = new ArrayList<>();
    DynaProperty[] properties = getDynaProperties();

    for (DynaProperty property : properties) {
      if (property instanceof SqlDynaProperty) {
        SqlDynaProperty sqlProperty = (SqlDynaProperty) property;

        if (sqlProperty.isPrimaryKey()) {
          pkProps.add(sqlProperty);
        } else {
          nonPkProps.add(sqlProperty);
        }
      }
    }
    primaryKeyProperties = pkProps.toArray(new SqlDynaProperty[0]);
    nonPrimaryKeyProperties = nonPkProps.toArray(new SqlDynaProperty[0]);
  }
}
