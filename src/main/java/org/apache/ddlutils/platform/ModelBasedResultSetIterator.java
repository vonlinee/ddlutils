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

import org.apache.commons.beanutils.*;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.dynabean.SqlDynaBean;
import org.apache.ddlutils.dynabean.SqlDynaClass;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.JdbcUtils;
import org.apache.ddlutils.util.StringUtilsExt;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This is an iterator that is specifically targeted at traversing result sets.
 * If the query is against a known table, then {@link org.apache.ddlutils.dynabean.SqlDynaBean} instances
 * are created from the rows, otherwise normal {@link org.apache.commons.beanutils.DynaBean} instances
 * are created.
 *
 * @version $Revision: 289996 $
 */
public class ModelBasedResultSetIterator implements Iterator<DynaBean> {
  /**
   * Maps column names to properties.
   */
  private final Map<String, String> columnsToProperties = new ListOrderedMap<>();
  /**
   * The platform.
   */
  private Platform platform;
  /**
   * The base result set.
   */
  private ResultSet resultSet;
  /**
   * The dyna class to use for creating beans.
   */
  private DynaClass dynaClass;
  /**
   * Whether the case of identifiers matters.
   */
  private boolean caseSensitive;
  /**
   * Maps column names to table objects as given by the query hints.
   */
  private Map<String, Table> preparedQueryHints;
  /**
   * Whether the next call to hasNext or next needs advancement.
   */
  private boolean needsAdvancing = true;
  /**
   * Whether we're already at the end of the result set.
   */
  private boolean isAtEnd = false;
  /**
   * Whether to close the statement and connection after finishing.
   */
  private boolean cleanUpAfterFinish;

  /**
   * Creates a new iterator.
   *
   * @param platform           The platform
   * @param model              The database model
   * @param resultSet          The result set
   * @param queryHints         The tables that were queried in the query that produced the given result set
   *                           (optional)
   * @param cleanUpAfterFinish Whether to close the statement and connection after finishing
   *                           the iteration, upon on exception, or when this iterator is garbage collected
   */
  public ModelBasedResultSetIterator(PlatformImplBase platform, Database model, ResultSet resultSet, Table[] queryHints, boolean cleanUpAfterFinish) throws DatabaseOperationException {
    if (resultSet != null) {
      this.platform = platform;
      this.resultSet = resultSet;
      this.cleanUpAfterFinish = cleanUpAfterFinish;
      caseSensitive = this.platform.isDelimitedIdentifierModeOn();
      preparedQueryHints = prepareQueryHints(queryHints);

      try {
        initFromMetaData(model);
      } catch (SQLException ex) {
        cleanUp();
        throw new DatabaseOperationException("Could not read the metadata of the result set", ex);
      }
    } else {
      isAtEnd = true;
    }
  }

  /**
   * Initializes this iterator from the resultset metadata.
   *
   * @param model The database model
   */
  private void initFromMetaData(Database model) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    String tableName = null;
    boolean singleKnownTable = true;

    for (int idx = 1; idx <= metaData.getColumnCount(); idx++) {
      final String columnName = metaData.getColumnName(idx);
      // jConnect might return a table name enclosed in quotes
      String tableOfColumn = StringUtilsExt.unquoteDouble(metaData.getTableName(idx));
      // the JDBC driver gave us enough metadata info
      Table table = model.findTable(tableOfColumn, caseSensitive);
      if (table == null) {
        // not enough info in the metadata of the result set, lets try the
        // user-supplied query hints
        table = preparedQueryHints.get(caseSensitive ? columnName : columnName.toLowerCase());
        tableOfColumn = (table == null ? null : table.getName());
      }
      if (tableName == null) {
        tableName = tableOfColumn;
      } else if (!tableName.equals(tableOfColumn)) {
        singleKnownTable = false;
      }

      String propName = columnName;
      if (table != null) {
        Column column = table.findColumn(columnName, caseSensitive);
        if (column != null) {
          propName = column.getName();
        }
      }
      columnsToProperties.put(columnName, propName);
    }
    if (singleKnownTable && (tableName != null)) {
      dynaClass = model.getDynaClassFor(tableName);
    } else {
      DynaProperty[] props = new DynaProperty[columnsToProperties.size()];
      int idx = 0;

      for (Iterator<String> it = columnsToProperties.values().iterator(); it.hasNext(); idx++) {
        props[idx] = new DynaProperty(it.next());
      }
      dynaClass = new BasicDynaClass("result", BasicDynaBean.class, props);
    }
  }

  /**
   * Prepares the query hints by extracting the column names and using them as keys
   * into the resulting map pointing to the corresponding table.
   *
   * @param queryHints The query hints
   * @return The column name -> table map
   */
  protected Map<String, Table> prepareQueryHints(Table[] queryHints) {
    Map<String, Table> result = new HashMap<>();

    for (int tableIdx = 0; (queryHints != null) && (tableIdx < queryHints.length); tableIdx++) {
      for (int columnIdx = 0; columnIdx < queryHints[tableIdx].getColumnCount(); columnIdx++) {
        String columnName = queryHints[tableIdx].getColumn(columnIdx).getName();
        if (!caseSensitive) {
          columnName = columnName.toLowerCase();
        }
        if (!result.containsKey(columnName)) {
          result.put(columnName, queryHints[tableIdx]);
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() throws DatabaseOperationException {
    advanceIfNecessary();
    return !isAtEnd;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DynaBean next() throws DatabaseOperationException {
    advanceIfNecessary();
    if (isAtEnd) {
      throw new NoSuchElementException("No more elements in the resultset");
    } else {
      try {
        DynaBean bean = dynaClass.newInstance();
        Table table = null;

        if (bean instanceof SqlDynaBean) {
          SqlDynaClass dynaClass = (SqlDynaClass) bean.getDynaClass();
          table = dynaClass.getTable();
        }

        for (Map.Entry<String, String> entry : columnsToProperties.entrySet()) {
          String columnName = entry.getKey();
          String propName = entry.getValue();
          Table curTable = table;

          if (curTable == null) {
            curTable = preparedQueryHints.get(caseSensitive ? columnName : columnName.toLowerCase());
          }

          Object value = platform.getObjectFromResultSet(resultSet, columnName, curTable);
          bean.set(propName, value);
        }
        needsAdvancing = true;
        return bean;
      } catch (Exception ex) {
        cleanUp();
        throw new DatabaseOperationException("Exception while reading the row from the resultset", ex);
      }
    }
  }

  /**
   * Advances the iterator without materializing the object. This is the same effect as calling
   * {@link #next()} except that no object is created and nothing is read from the result set.
   */
  public void advance() {
    advanceIfNecessary();
    if (isAtEnd) {
      throw new NoSuchElementException("No more elements in the resultset");
    } else {
      needsAdvancing = true;
    }
  }

  /**
   * Advances the result set if necessary.
   */
  private void advanceIfNecessary() throws DatabaseOperationException {
    if (needsAdvancing && !isAtEnd) {
      try {
        isAtEnd = !resultSet.next();
        needsAdvancing = false;
      } catch (SQLException ex) {
        cleanUp();
        throw new DatabaseOperationException("Could not retrieve next row from result set", ex);
      }
      if (isAtEnd) {
        cleanUp();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove() throws DatabaseOperationException {
    try {
      resultSet.deleteRow();
    } catch (SQLException ex) {
      cleanUp();
      throw new DatabaseOperationException("Failed to delete current row", ex);
    }
  }

  /**
   * Closes the resources (connection, statement, resultset).
   */
  public void cleanUp() {
    if (cleanUpAfterFinish && (resultSet != null)) {
      Connection conn = null;
      try {
        Statement stmt = resultSet.getStatement();
        conn = stmt.getConnection();
        // also closes the resultset
        platform.closeStatement(stmt);
      } catch (SQLException ex) {
        // we ignore it
      }
      platform.returnConnection(conn);
      resultSet = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void finalize() throws Throwable {
    cleanUp();
  }

  /**
   * Determines whether the connection is still open.
   *
   * @return <code>true</code> if the connection is still open
   */
  public boolean isConnectionOpen() {
    return JdbcUtils.isConnectionOpen(resultSet);
  }
}
