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

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a cache of dyna class instances for a specific model, as well as
 * helper methods for dealing with these classes.
 *
 * @version $Revision: 231110 $
 */
public class TableClassCache {
  /**
   * A cache of the SqlDynaClasses per table name.
   */
  private final Map<String, TableClass> _tableClassCache = new HashMap<>();

  /**
   * Creates a new dyna bean instance for the given table.
   *
   * @param table The table
   * @return The new empty dyna bean
   */
  public RowObject createNewInstance(Table table) throws RuntimeSqlException {
    try {
      return getTableClass(table).newInstance();
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new RuntimeSqlException("Could not create a new dyna bean for table " + table.getName(), ex);
    }
  }

  /**
   * Returns the {@link TableClass} for the given table. If it does not
   * exist yet, a new one will be created based on the Table definition.
   *
   * @param table The table
   * @return The <code>SqlDynaClass</code> for the indicated table
   */
  public TableClass getTableClass(Table table) {
    TableClass answer = _tableClassCache.get(table.getName());
    if (answer == null) {
      answer = createDynaClass(table);
      _tableClassCache.put(table.getName(), answer);
    }
    return answer;
  }

  /**
   * Returns the {@link TableClass} for the given bean.
   *
   * @param rowObject The bean
   * @return The dyna bean class
   */
  public TableClass getTableClass(RowObject rowObject) throws RuntimeSqlException {
    return rowObject.getTableClass();
  }

  /**
   * Creates a new {@link TableClass} instance for the given table based on the table definition.
   *
   * @param table The table
   * @return The new dyna class
   */
  private TableClass createDynaClass(Table table) {
    return TableClass.newInstance(table);
  }
}
