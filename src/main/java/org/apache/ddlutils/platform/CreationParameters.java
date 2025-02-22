package org.apache.ddlutils.platform;

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
import org.apache.ddlutils.util.OrderedMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains parameters used in the table creation. Note that the definition
 * order is retained (per table), so if a parameter should be applied before
 * some other parameter, then add it before the other one.
 *
 * @version $Revision: 331006 $
 */
public class CreationParameters {
  /**
   * The parameter maps keyed by the tables.
   */
  private final Map<String, Map<String, Object>> _parametersPerTable = new HashMap<>();

  /**
   * Returns the parameters for the given table.
   *
   * @param table The table
   * @return The parameters
   */
  public OrderedMap<String, Object> getParametersFor(Table table) {
    OrderedMap<String, Object> result = new OrderedMap<>();
    Map<String, Object> globalParams = _parametersPerTable.get(null);
    Map<String, Object> tableParams = _parametersPerTable.get(table.getName());

    if (globalParams != null) {
      result.putAll(globalParams);
    }
    if (tableParams != null) {
      result.putAll(tableParams);
    }
    return result;
  }

  /**
   * Adds a parameter.
   *
   * @param table      The table; if <code>null</code> then the parameter is for all tables
   * @param paramName  The name of the parameter
   * @param paramValue The value of the parameter
   */
  public void addParameter(Table table, String paramName, String paramValue) {
    String key = (table == null ? null : table.getName());
    Map<String, Object> params = _parametersPerTable.get(key);

    if (params == null) {
      // we're using a list ordered map to retain the order
      params = new OrderedMap<>();
      _parametersPerTable.put(key, params);
    }
    params.put(paramName, paramValue);
  }
}
