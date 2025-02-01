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

import org.apache.ddlutils.data.BasicRowObject;
import org.apache.ddlutils.data.BasicTableClass;
import org.apache.ddlutils.data.RowObject;
import org.apache.ddlutils.data.TableClass;
import org.apache.ddlutils.data.ColumnProperty;
import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.data.SqlRowObject;
import org.apache.ddlutils.data.SqlTableClass;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.util.OrderedMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This is an iterator that is specifically targeted at traversing result sets.
 * If the query is against a known table, then {@link SqlRowObject} instances
 * are created from the rows, otherwise normal {@link RowObject} instances
 * are created.
 *
 * @version $Revision: 289996 $
 */
public class ModelBasedResultSetIterator implements Iterator<RowObject> {
  /**
   * The platform.
   */
  private PlatformImplBase _platform;
  /**
   * The base result set.
   */
  private ResultSet _resultSet;
  /**
   * The dyna class to use for creating beans.
   */
  private TableClass _tableClass;
  /**
   * Whether the case of identifiers matters.
   */
  private boolean _caseSensitive;
  /**
   * Maps column names to table objects as given by the query hints.
   */
  private Map<String, Table> _preparedQueryHints;
  /**
   * Maps column names to properties.
   */
  private final OrderedMap<String, String> _columnsToProperties = new OrderedMap<>();
  /**
   * Whether the next call to hasNext or next needs advancement.
   */
  private boolean _needsAdvancing = true;
  /**
   * Whether we're already at the end of the result set.
   */
  private boolean _isAtEnd = false;
  /**
   * Whether to close the statement and connection after finishing.
   */
  private boolean _cleanUpAfterFinish;

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
      _platform = platform;
      _resultSet = resultSet;
      _cleanUpAfterFinish = cleanUpAfterFinish;
      _caseSensitive = _platform.isDelimitedIdentifierModeOn();
      _preparedQueryHints = prepareQueryHints(queryHints);

      try {
        initFromMetaData(model);
      } catch (SQLException ex) {
        cleanUp();
        throw new DatabaseOperationException("Could not read the metadata of the result set", ex);
      }
    } else {
      _isAtEnd = true;
    }
  }

  /**
   * Initializes this iterator from the result set metadata.
   *
   * @param model The database model
   */
  private void initFromMetaData(Database model) throws SQLException {
    ResultSetMetaData metaData = _resultSet.getMetaData();
    String tableName = null;
    boolean singleKnownTable = true;

    for (int idx = 1; idx <= metaData.getColumnCount(); idx++) {
      String columnName = metaData.getColumnName(idx);
      String tableOfColumn = metaData.getTableName(idx);
      Table table = null;

      if ((tableOfColumn != null) && (!tableOfColumn.isEmpty())) {
        // jConnect might return a table name enclosed in quotes
        if (tableOfColumn.startsWith("\"") && tableOfColumn.endsWith("\"") && (tableOfColumn.length() > 1)) {
          tableOfColumn = tableOfColumn.substring(1, tableOfColumn.length() - 1);
        }
        // the JDBC driver gave us enough metadata info
        table = model.findTable(tableOfColumn, _caseSensitive);
      }
      if (table == null) {
        // not enough info in the metadata of the result set, lets try the
        // user-supplied query hints
        table = _preparedQueryHints.get(_caseSensitive ? columnName : columnName.toLowerCase());
        tableOfColumn = (table == null ? null : table.getName());
      }
      if (tableName == null) {
        tableName = tableOfColumn;
      } else if (!tableName.equals(tableOfColumn)) {
        singleKnownTable = false;
      }

      String propName = columnName;

      if (table != null) {
        Column column = table.findColumn(columnName, _caseSensitive);

        if (column != null) {
          propName = column.getName();
        }
      }
      _columnsToProperties.put(columnName, propName);
    }
    if (singleKnownTable && (tableName != null)) {
      _tableClass = model.getDynaClassFor(tableName);
    } else {
      ColumnProperty[] props = new ColumnProperty[_columnsToProperties.size()];
      int idx = 0;

      for (Iterator<String> it = _columnsToProperties.values().iterator(); it.hasNext(); idx++) {
        props[idx] = new ColumnProperty(it.next());
      }
      _tableClass = new BasicTableClass("result", BasicRowObject.class, props);
    }
  }

  /**
   * Prepares the query hints by extracting the column names and using them as keys
   * into the resulting map pointing to the corresponding table.
   *
   * @param queryHints The query hints
   * @return The column name -> table map
   */
  private Map<String, Table> prepareQueryHints(Table[] queryHints) {
    Map<String, Table> result = new HashMap<>();

    for (int tableIdx = 0; (queryHints != null) && (tableIdx < queryHints.length); tableIdx++) {
      for (int columnIdx = 0; columnIdx < queryHints[tableIdx].getColumnCount(); columnIdx++) {
        String columnName = queryHints[tableIdx].getColumn(columnIdx).getName();

        if (!_caseSensitive) {
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
    return !_isAtEnd;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RowObject next() throws DatabaseOperationException {
    advanceIfNecessary();
    if (_isAtEnd) {
      throw new NoSuchElementException("No more elements in the result set");
    } else {
      try {
        RowObject bean = _tableClass.newInstance();
        Table table = null;

        if (bean instanceof SqlRowObject) {
          SqlTableClass dynaClass = (SqlTableClass) bean.getDynaClass();

          table = dynaClass.getTable();
        }

        for (Map.Entry<String, String> entry : _columnsToProperties.entrySet()) {
          String columnName = entry.getKey();
          String propName = entry.getValue();
          Table curTable = table;

          if (curTable == null) {
            curTable = _preparedQueryHints.get(_caseSensitive ? columnName : columnName.toLowerCase());
          }

          Object value = _platform.getObjectFromResultSet(_resultSet, columnName, curTable);

          bean.set(propName, value);
        }
        _needsAdvancing = true;
        return bean;
      } catch (Exception ex) {
        cleanUp();
        throw new DatabaseOperationException("Exception while reading the row from the result set", ex);
      }
    }
  }

  /**
   * Advances the iterator without materializing the object. This is the same effect as calling
   * {@link #next()} except that no object is created and nothing is read from the result set.
   */
  public void advance() {
    advanceIfNecessary();
    if (_isAtEnd) {
      throw new NoSuchElementException("No more elements in the result set");
    } else {
      _needsAdvancing = true;
    }
  }

  /**
   * Advances the result set if necessary.
   */
  private void advanceIfNecessary() throws DatabaseOperationException {
    if (_needsAdvancing && !_isAtEnd) {
      try {
        _isAtEnd = !_resultSet.next();
        _needsAdvancing = false;
      } catch (SQLException ex) {
        cleanUp();
        throw new DatabaseOperationException("Could not retrieve next row from result set", ex);
      }
      if (_isAtEnd) {
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
      _resultSet.deleteRow();
    } catch (SQLException ex) {
      cleanUp();
      throw new DatabaseOperationException("Failed to delete current row", ex);
    }
  }

  /**
   * Closes the resources (connection, statement, result set).
   */
  public void cleanUp() {
    if (_cleanUpAfterFinish && (_resultSet != null)) {
      Connection conn = null;
      try {
        Statement stmt = _resultSet.getStatement();

        conn = stmt.getConnection();

        // also closes the result set
        _platform.closeStatement(stmt);
      } catch (SQLException ex) {
        // we ignore it
      }
      _platform.returnConnection(conn);
      _resultSet = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void finalize() {
    cleanUp();
  }

  /**
   * Determines whether the connection is still open.
   *
   * @return <code>true</code> if the connection is still open
   */
  public boolean isConnectionOpen() {
    if (_resultSet == null) {
      return false;
    }
    try {
      Statement stmt = _resultSet.getStatement();
      Connection conn = stmt.getConnection();

      return !conn.isClosed();
    } catch (SQLException ex) {
      return false;
    }
  }
}
