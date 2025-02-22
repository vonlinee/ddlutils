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

import org.apache.ddlutils.DatabaseOperationException;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddForeignKeyChange;
import org.apache.ddlutils.alteration.AddIndexChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.AddTableChange;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.alteration.ColumnOrderChange;
import org.apache.ddlutils.alteration.ForeignKeyChange;
import org.apache.ddlutils.alteration.IndexChange;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RecreateTableChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemoveForeignKeyChange;
import org.apache.ddlutils.alteration.RemoveIndexChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveTableChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.data.RowObject;
import org.apache.ddlutils.data.TableClass;
import org.apache.ddlutils.data.ColumnProperty;
import org.apache.ddlutils.model.CloneHelper;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.ModelException;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.util.JdbcSupport;
import org.apache.ddlutils.util.Log;
import org.apache.ddlutils.util.LogFactory;
import org.apache.ddlutils.util.SqlTokenizer;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class for platform implementations.
 *
 * @version $Revision: 231110 $
 */
public abstract class PlatformImplBase extends JdbcSupport implements Platform {
  /**
   * The default name for models read from the database, if no name as given.
   */
  protected static final String MODEL_DEFAULT_NAME = "default";

  /**
   * The log for this platform.
   */
  private final Log _log = LogFactory.getLog(getClass());

  /**
   * The platform info.
   */
  private final PlatformInfo _info = new PlatformInfo();
  /**
   * The sql builder for this platform.
   */
  private SqlBuilder _builder;
  /**
   * The model reader for this platform.
   */
  private JdbcModelReader _modelReader;
  /**
   * Whether script mode is on.
   */
  private boolean _scriptModeOn;
  /**
   * Whether SQL comments are generated or not.
   */
  private boolean _sqlCommentsOn = true;
  /**
   * Whether delimited identifiers are used or not.
   */
  private boolean _delimitedIdentifierModeOn;
  /**
   * Whether identity override is enabled.
   */
  private boolean _identityOverrideOn;
  /**
   * Whether read foreign keys shall be sorted alphabetically.
   */
  private boolean _foreignKeysSorted;
  /**
   * Whether to use the default ON UPDATE action if the specified one is unsupported.
   */
  private boolean _useDefaultOnUpdateActionIfUnsupported = true;
  /**
   * Whether to use the default ON DELETE action if the specified one is unsupported.
   */
  private boolean _useDefaultOnDeleteActionIfUnsupported = true;

  /**
   * {@inheritDoc}
   */
  @Override
  public SqlBuilder getSqlBuilder() {
    return _builder;
  }

  /**
   * Sets the sql builder for this platform.
   *
   * @param builder The sql builder
   */
  protected void setSqlBuilder(SqlBuilder builder) {
    _builder = builder;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JdbcModelReader getModelReader() {
    if (_modelReader == null) {
      _modelReader = new JdbcModelReader(this);
    }
    return _modelReader;
  }

  /**
   * Sets the model reader for this platform.
   *
   * @param modelReader The model reader
   */
  protected void setModelReader(JdbcModelReader modelReader) {
    _modelReader = modelReader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PlatformInfo getPlatformInfo() {
    return _info;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isScriptModeOn() {
    return _scriptModeOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setScriptModeOn(boolean scriptModeOn) {
    _scriptModeOn = scriptModeOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSqlCommentsOn() {
    return _sqlCommentsOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setSqlCommentsOn(boolean sqlCommentsOn) {
    if (!getPlatformInfo().isSqlCommentsSupported() && sqlCommentsOn) {
      throw new DdlUtilsException("Platform " + getName() + " does not support SQL comments");
    }
    _sqlCommentsOn = sqlCommentsOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDelimitedIdentifierModeOn() {
    return _delimitedIdentifierModeOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDelimitedIdentifierModeOn(boolean delimitedIdentifierModeOn) {
    if (!getPlatformInfo().isDelimitedIdentifiersSupported() && delimitedIdentifierModeOn) {
      throw new DdlUtilsException("Platform " + getName() + " does not support delimited identifier");
    }
    _delimitedIdentifierModeOn = delimitedIdentifierModeOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isIdentityOverrideOn() {
    return _identityOverrideOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setIdentityOverrideOn(boolean identityOverrideOn) {
    _identityOverrideOn = identityOverrideOn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isForeignKeysSorted() {
    return _foreignKeysSorted;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setForeignKeysSorted(boolean foreignKeysSorted) {
    _foreignKeysSorted = foreignKeysSorted;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDefaultOnUpdateActionUsedIfUnsupported() {
    return _useDefaultOnUpdateActionIfUnsupported;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDefaultOnUpdateActionUsedIfUnsupported(boolean useDefault) {
    _useDefaultOnUpdateActionIfUnsupported = useDefault;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDefaultOnDeleteActionUsedIfUnsupported() {
    return _useDefaultOnDeleteActionIfUnsupported;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDefaultOnDeleteActionUsedIfUnsupported(boolean useDefault) {
    _useDefaultOnDeleteActionIfUnsupported = useDefault;
  }

  /**
   * Returns the log for this platform.
   *
   * @return The log
   */
  protected Log getLog() {
    return _log;
  }

  /**
   * Logs any warnings associated to the given connection. Note that the connection needs
   * to be open for this.
   *
   * @param connection The open connection
   */
  protected void logWarnings(Connection connection) throws SQLException {
    SQLWarning warning = connection.getWarnings();

    while (warning != null) {
      getLog().warn(warning.getLocalizedMessage(), warning.getCause());
      warning = warning.getNextWarning();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int evaluateBatch(String sql, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      return evaluateBatch(connection, sql, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int evaluateBatch(Connection connection, String sql, boolean continueOnError) throws DatabaseOperationException {
    Statement statement = null;
    int errors = 0;
    int commandCount = 0;

    // we tokenize the SQL along the delimiters, and we also make sure that only delimiters
    // at the end of a line or the end of the string are used (row mode)
    try {
      statement = connection.createStatement();

      SqlTokenizer tokenizer = new SqlTokenizer(sql);

      while (tokenizer.hasMoreStatements()) {
        String command = tokenizer.getNextStatement();

        // ignore whitespace
        command = command.trim();
        if (command.isEmpty()) {
          continue;
        }

        commandCount++;

        if (_log.isDebugEnabled()) {
          _log.debug("About to execute SQL " + command);
        }
        try {
          int results = statement.executeUpdate(command);

          if (_log.isDebugEnabled()) {
            _log.debug("After execution, " + results + " row(s) have been changed");
          }
        } catch (SQLException ex) {
          if (continueOnError) {
            // Since the user decided to ignore this error, we log the error
            // on level warn, and the exception itself on level debug
            _log.warn("SQL Command " + command + " failed with: " + ex.getMessage());
            if (_log.isDebugEnabled()) {
              _log.debug(ex);
            }
            errors++;
          } else {
            throw new DatabaseOperationException("Error while executing SQL " + command, ex);
          }
        }

        // let's display any warnings
        SQLWarning warning = connection.getWarnings();

        while (warning != null) {
          _log.warn(warning.toString());
          warning = warning.getNextWarning();
        }
        connection.clearWarnings();
      }
      _log.info("Executed " + commandCount + " SQL command(s) with " + errors + " error(s)");
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while executing SQL", ex);
    } finally {
      closeStatement(statement);
    }

    return errors;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdownDatabase() throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      shutdownDatabase(connection);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdownDatabase(Connection connection) throws DatabaseOperationException {
    // Per default do nothing as most databases don't need this
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map<String, Object> parameters) throws DatabaseOperationException, UnsupportedOperationException {
    throw new UnsupportedOperationException("Database creation is not supported for the database platform " + getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password) throws DatabaseOperationException, UnsupportedOperationException {
    throw new UnsupportedOperationException("Database deletion is not supported for the database platform " + getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTables(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    createModel(model, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTables(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    createModel(model, params, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTables(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    createModel(connection, model, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTables(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    createModel(connection, model, params, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCreateTablesSql(Database model, boolean dropTablesFirst, boolean continueOnError) {
    return getCreateModelSql(model, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCreateTablesSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) {
    return getCreateModelSql(model, params, dropTablesFirst, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createModel(Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      createModel(connection, model, dropTablesFirst, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createModel(Connection connection, Database model, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    String sql = getCreateModelSql(model, dropTablesFirst, continueOnError);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createModel(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      createModel(connection, model, params, dropTablesFirst, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createModel(Connection connection, Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) throws DatabaseOperationException {
    String sql = getCreateModelSql(model, params, dropTablesFirst, continueOnError);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCreateModelSql(Database model, boolean dropTablesFirst, boolean continueOnError) {
    String sql = null;

    try {
      StringWriter buffer = new StringWriter();

      getSqlBuilder().setWriter(buffer);
      getSqlBuilder().createTables(model, dropTablesFirst);
      sql = buffer.toString();
    } catch (IOException e) {
      // won't happen because we're using a string writer
    }
    return sql;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCreateModelSql(Database model, CreationParameters params, boolean dropTablesFirst, boolean continueOnError) {
    String sql = null;

    try {
      StringWriter buffer = new StringWriter();

      getSqlBuilder().setWriter(buffer);
      getSqlBuilder().createTables(model, params, dropTablesFirst);
      sql = buffer.toString();
    } catch (IOException e) {
      // won't happen because we're using a string writer
    }
    return sql;
  }

  /**
   * Returns the model comparator to be used for this platform. This method is intended
   * to be redefined by platforms that need to customize the model reader.
   *
   * @return The model comparator
   */
  protected ModelComparator getModelComparator() {
    return new ModelComparator(getPlatformInfo(),
      getTableDefinitionChangesPredicate(),
      isDelimitedIdentifierModeOn());
  }

  /**
   * Returns the predicate that defines which changes are supported by the platform.
   *
   * @return The predicate
   */
  protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate() {
    return new DefaultTableDefinitionChangesPredicate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ModelChange> getChanges(Database currentModel, Database desiredModel) {
    List<ModelChange> changes = getModelComparator().compare(currentModel, desiredModel);

    return sortChanges(changes);
  }

  /**
   * Sorts the changes so that they can be executed by the database. E.g. tables need to be created before
   * they can be referenced by foreign keys, indexes should be dropped before a table is dropped etc.
   *
   * @param changes The original changes
   * @return The sorted changes - this can be the original list object or a new one
   */
  protected List<ModelChange> sortChanges(List<ModelChange> changes) {
    final Map<Class<?>, Integer> typeOrder = new HashMap<>();

    typeOrder.put(RemoveForeignKeyChange.class, 0);
    typeOrder.put(RemoveIndexChange.class, 1);
    typeOrder.put(RemoveTableChange.class, 2);
    typeOrder.put(RecreateTableChange.class, 3);
    typeOrder.put(RemovePrimaryKeyChange.class, 3);
    typeOrder.put(RemoveColumnChange.class, 4);
    typeOrder.put(ColumnDefinitionChange.class, 5);
    typeOrder.put(ColumnOrderChange.class, 5);
    typeOrder.put(AddColumnChange.class, 5);
    typeOrder.put(PrimaryKeyChange.class, 5);
    typeOrder.put(AddPrimaryKeyChange.class, 6);
    typeOrder.put(AddTableChange.class, 7);
    typeOrder.put(AddIndexChange.class, 8);
    typeOrder.put(AddForeignKeyChange.class, 9);

    changes.sort((objA, objB) -> {
      Integer orderValueA = typeOrder.get(objA.getClass());
      Integer orderValueB = typeOrder.get(objB.getClass());

      if (orderValueA == null) {
        return (orderValueB == null ? 0 : 1);
      } else if (orderValueB == null) {
        return -1;
      } else {
        return orderValueA.compareTo(orderValueB);
      }
    });
    return changes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

      alterModel(currentModel, desiredModel, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

      alterModel(currentModel, desiredModel, params, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(String catalog, String schema, String[] tableTypes, Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

      alterModel(currentModel, desiredModel, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(String catalog, String schema, String[] tableTypes, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

      alterModel(currentModel, desiredModel, params, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Connection connection, Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

    alterModel(currentModel, desiredModel, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Connection connection, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

    alterModel(currentModel, desiredModel, params, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Connection connection, String catalog, String schema, String[] tableTypes, Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

    alterModel(currentModel, desiredModel, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterTables(Connection connection, String catalog, String schema, String[] tableTypes, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

    alterModel(currentModel, desiredModel, params, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Database desiredModel) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

      return getAlterModelSql(currentModel, desiredModel);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Database desiredModel, CreationParameters params) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

      return getAlterModelSql(currentModel, desiredModel, params);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(String catalog, String schema, String[] tableTypes, Database desiredModel) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

      return getAlterModelSql(currentModel, desiredModel);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(String catalog, String schema, String[] tableTypes, Database desiredModel, CreationParameters params) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

      return getAlterModelSql(currentModel, desiredModel, params);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Connection connection, Database desiredModel) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

    return getAlterModelSql(currentModel, desiredModel);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Connection connection, Database desiredModel, CreationParameters params) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName());

    return getAlterModelSql(currentModel, desiredModel, params);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Connection connection, String catalog, String schema, String[] tableTypes, Database desiredModel) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

    return getAlterModelSql(currentModel, desiredModel);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterTablesSql(Connection connection, String catalog, String schema, String[] tableTypes, Database desiredModel, CreationParameters params) throws DatabaseOperationException {
    Database currentModel = readModelFromDatabase(connection, desiredModel.getName(), catalog, schema, tableTypes);

    return getAlterModelSql(currentModel, desiredModel, params);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterModelSql(Database currentModel, Database desiredModel) throws DatabaseOperationException {
    return getAlterModelSql(currentModel, desiredModel, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getAlterModelSql(Database currentModel, Database desiredModel, CreationParameters params) throws DatabaseOperationException {
    List<ModelChange> changes = getChanges(currentModel, desiredModel);
    String sql = null;

    try {
      StringWriter buffer = new StringWriter();

      getSqlBuilder().setWriter(buffer);
      processChanges(currentModel, changes, params);
      sql = buffer.toString();
    } catch (IOException ex) {
      // won't happen because we're using a string writer
    }
    return sql;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterModel(Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      alterModel(connection, currentModel, desiredModel, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterModel(Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      alterModel(connection, currentModel, desiredModel, params, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterModel(Connection connection, Database currentModel, Database desiredModel, boolean continueOnError) throws DatabaseOperationException {
    String sql = getAlterModelSql(currentModel, desiredModel);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void alterModel(Connection connection, Database currentModel, Database desiredModel, CreationParameters params, boolean continueOnError) throws DatabaseOperationException {
    String sql = getAlterModelSql(currentModel, desiredModel, params);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropTable(Connection connection, Database model, Table table, boolean continueOnError) throws DatabaseOperationException {
    String sql = getDropTableSql(model, table, continueOnError);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropTable(Database model, Table table, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      dropTable(connection, model, table, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDropTableSql(Database model, Table table, boolean continueOnError) {
    String sql = null;

    try {
      StringWriter buffer = new StringWriter();

      getSqlBuilder().setWriter(buffer);
      getSqlBuilder().dropTable(model, table);
      sql = buffer.toString();
    } catch (IOException e) {
      // won't happen because we're using a string writer
    }
    return sql;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropTables(Database model, boolean continueOnError) throws DatabaseOperationException {
    dropModel(model, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropTables(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException {
    dropModel(connection, model, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDropTablesSql(Database model, boolean continueOnError) {
    return getDropModelSql(model);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropModel(Database model, boolean continueOnError) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      dropModel(connection, model, continueOnError);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropModel(Connection connection, Database model, boolean continueOnError) throws DatabaseOperationException {
    String sql = getDropModelSql(model);

    evaluateBatch(connection, sql, continueOnError);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDropModelSql(Database model) {
    String sql = null;

    try {
      StringWriter buffer = new StringWriter();

      getSqlBuilder().setWriter(buffer);
      getSqlBuilder().dropTables(model);
      sql = buffer.toString();
    } catch (IOException e) {
      // won't happen because we're using a string writer
    }
    return sql;
  }

  /**
   * Processes the given changes in the specified order. Basically, this method finds the
   * appropriate handler method (one of the <code>processChange</code> methods) defined in
   * the concrete sql builder for each change, and invokes it.
   *
   * @param model   The database model; this object is not going to be changed by this method
   * @param changes The changes
   * @param params  The parameters used in the creation of new tables. Note that for existing
   *                tables, the parameters won't be applied
   * @return The changed database model
   */
  protected Database processChanges(Database model,
                                    Collection<ModelChange> changes,
                                    CreationParameters params) throws IOException, DdlUtilsException {
    Database currentModel = new CloneHelper().clone(model);

    for (ModelChange change : changes) {
      invokeChangeHandler(currentModel, params, change);
    }
    return currentModel;
  }

  /**
   * Invokes the change handler (one of the <code>processChange</code> methods) for the given
   * change object.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  private void invokeChangeHandler(Database currentModel,
                                   CreationParameters params,
                                   ModelChange change) throws IOException {
    Class<?> curClass = getClass();

    // find the handler for the change
    while ((curClass != null) && !Object.class.equals(curClass)) {
      try {
        Method method = null;

        try {
          method = curClass.getDeclaredMethod("processChange",
            Database.class,
            CreationParameters.class,
            change.getClass());
        } catch (NoSuchMethodException ex) {
          // we actually expect this one
        }

        if (method != null) {
          method.invoke(this, currentModel, params, change);
          return;
        } else {
          curClass = curClass.getSuperclass();
        }
      } catch (InvocationTargetException ex) {
        if (ex.getTargetException() instanceof IOException) {
          throw (IOException) ex.getTargetException();
        } else {
          throw new DdlUtilsException(ex.getTargetException());
        }
      } catch (Exception ex) {
        throw new DdlUtilsException(ex);
      }
    }
    throw new DdlUtilsException("No handler for change of type " + change.getClass().getName() + " defined");
  }

  /**
   * Finds the table changed by the change object in the given model.
   *
   * @param currentModel The model to find the table in
   * @param change       The table change
   * @return The table
   * @throws ModelException If the table could not be found
   */
  protected Table findChangedTable(Database currentModel, TableChange change) throws ModelException {
    Table table = currentModel.findTable(change.getChangedTable(),
      getPlatformInfo().isDelimitedIdentifiersSupported());

    if (table == null) {
      throw new ModelException("Could not find table " + change.getChangedTable() + " in the given model");
    } else {
      return table;
    }
  }

  /**
   * Finds the index changed by the change object in the given model.
   *
   * @param currentModel The model to find the index in
   * @param change       The index change
   * @return The index
   * @throws ModelException If the index could not be found
   */
  protected Index findChangedIndex(Database currentModel, IndexChange change) throws ModelException {
    Index index = change.findChangedIndex(currentModel,
      getPlatformInfo().isDelimitedIdentifiersSupported());

    if (index == null) {
      throw new ModelException("Could not find the index to change in table " + change.getChangedTable() + " in the given model");
    } else {
      return index;
    }
  }

  /**
   * Finds the foreign key changed by the change object in the given model.
   *
   * @param currentModel The model to find the foreign key in
   * @param change       The foreign key change
   * @return The foreign key
   * @throws ModelException If the foreign key could not be found
   */
  protected ForeignKey findChangedForeignKey(Database currentModel, ForeignKeyChange change) throws ModelException {
    ForeignKey fk = change.findChangedForeignKey(currentModel,
      getPlatformInfo().isDelimitedIdentifiersSupported());

    if (fk == null) {
      throw new ModelException("Could not find the foreign key to change in table " + change.getChangedTable() + " in the given model");
    } else {
      return fk;
    }
  }

  /**
   * Processes a change representing the addition of a table.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            AddTableChange change) throws IOException {
    getSqlBuilder().createTable(currentModel,
      change.getNewTable(),
      params == null ? null : params.getParametersFor(change.getNewTable()));
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the removal of a table.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RemoveTableChange change) throws IOException, ModelException {
    Table changedTable = findChangedTable(currentModel, change);

    getSqlBuilder().dropTable(changedTable);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the addition of a foreign key.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            AddForeignKeyChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);

    getSqlBuilder().createForeignKey(currentModel,
      changedTable,
      change.getNewForeignKey());
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the removal of a foreign key.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RemoveForeignKeyChange change) throws IOException, ModelException {
    Table changedTable = findChangedTable(currentModel, change);
    ForeignKey changedFk = findChangedForeignKey(currentModel, change);

    getSqlBuilder().dropForeignKey(changedTable, changedFk);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the addition of an index.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            AddIndexChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);

    getSqlBuilder().createIndex(changedTable, change.getNewIndex());
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the removal of an index.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RemoveIndexChange change) throws IOException, ModelException {
    Table changedTable = findChangedTable(currentModel, change);
    Index changedIndex = findChangedIndex(currentModel, change);

    getSqlBuilder().dropIndex(changedTable, changedIndex);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the addition of a column.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            AddColumnChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);

    getSqlBuilder().addColumn(currentModel, changedTable, change.getNewColumn());
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the addition of a primary key.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            AddPrimaryKeyChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);
    String[] pkColumnNames = change.getPrimaryKeyColumns();
    Column[] pkColumns = new Column[pkColumnNames.length];

    for (int colIdx = 0; colIdx < pkColumns.length; colIdx++) {
      pkColumns[colIdx] = changedTable.findColumn(pkColumnNames[colIdx], isDelimitedIdentifierModeOn());
    }
    getSqlBuilder().createPrimaryKey(changedTable, pkColumns);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes a change representing the recreation of a table.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RecreateTableChange change) throws IOException {
    // we can only copy the data if no required columns without default value and
    // non-autoincrement have been added
    boolean canMigrateData = true;

    for (Iterator<TableChange> it = change.getOriginalChanges().iterator(); canMigrateData && it.hasNext(); ) {
      TableChange curChange = it.next();

      if (curChange instanceof AddColumnChange) {
        AddColumnChange addColumnChange = (AddColumnChange) curChange;
        if (addColumnChange.getNewColumn().isRequired() &&
          !addColumnChange.getNewColumn().isAutoIncrement() &&
          (addColumnChange.getNewColumn().getDefaultValue() == null)) {
          _log.warn("Data cannot be retained in table " + change.getChangedTable() +
            " because of the addition of the required column " + addColumnChange.getNewColumn().getName());
          canMigrateData = false;
        }
      }
    }

    Table changedTable = findChangedTable(currentModel, change);
    Table targetTable = change.getTargetTable();
    Map<String, Object> parameters = (params == null ? null : params.getParametersFor(targetTable));

    if (canMigrateData) {
      Table tempTable = getTemporaryTableFor(targetTable);

      getSqlBuilder().createTemporaryTable(currentModel, tempTable, parameters);
      getSqlBuilder().copyData(changedTable, tempTable);
      // Note that we don't drop the indices here because the DROP TABLE will take care of that
      // Likewise, foreign keys have already been dropped as necessary
      getSqlBuilder().dropTable(changedTable);
      getSqlBuilder().createTable(currentModel, targetTable, parameters);
      getSqlBuilder().copyData(tempTable, targetTable);
      getSqlBuilder().dropTemporaryTable(currentModel, tempTable);
    } else {
      getSqlBuilder().dropTable(changedTable);
      getSqlBuilder().createTable(currentModel, targetTable, parameters);
    }

    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Creates a temporary table object that corresponds to the given table.
   * Database-specific implementations may redefine this method if e.g. the
   * database directly supports temporary tables. The default implementation
   * simply appends an underscore to the table name and uses that as the
   * table name.
   *
   * @param targetTable The target table
   * @return The temporary table
   */
  protected Table getTemporaryTableFor(Table targetTable) {
    CloneHelper cloneHelper = new CloneHelper();
    Table table = new Table();

    table.setCatalog(targetTable.getCatalog());
    table.setSchema(targetTable.getSchema());
    table.setName(targetTable.getName() + "_");
    table.setType(targetTable.getType());
    for (int idx = 0; idx < targetTable.getColumnCount(); idx++) {
      // TODO: clone PK status ?
      table.addColumn(cloneHelper.clone(targetTable.getColumn(idx), true));
    }

    return table;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<RowObject> query(Database model, String sql) throws DatabaseOperationException {
    return query(model, sql, (Table[]) null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<RowObject> query(Database model, String sql, Collection<Object> parameters) throws DatabaseOperationException {
    return query(model, sql, parameters, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<RowObject> query(Database model, String sql, Table[] queryHints) throws DatabaseOperationException {
    Connection connection = borrowConnection();
    Statement statement = null;
    ResultSet resultSet;
    Iterator<RowObject> answer = null;

    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(sql);
      answer = createResultSetIterator(model, resultSet, queryHints);
      return answer;
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while performing a query", ex);
    } finally {
      // if any exceptions are thrown, close things down
      // otherwise we're leaving it open for the iterator
      if (answer == null) {
        closeStatement(statement);
        returnConnection(connection);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<RowObject> query(Database model, String sql, Collection<Object> parameters, Table[] queryHints) throws DatabaseOperationException {
    Connection connection = borrowConnection();
    PreparedStatement statement = null;
    ResultSet resultSet;
    Iterator<RowObject> answer = null;

    try {
      statement = connection.prepareStatement(sql);

      int paramIdx = 1;

      for (Iterator<Object> iterator = parameters.iterator(); iterator.hasNext(); paramIdx++) {
        Object arg = iterator.next();

        if (arg instanceof BigDecimal) {
          // to avoid scale problems because setObject assumes a scale of 0
          statement.setBigDecimal(paramIdx, (BigDecimal) arg);
        } else {
          statement.setObject(paramIdx, arg);
        }
      }
      resultSet = statement.executeQuery();
      answer = createResultSetIterator(model, resultSet, queryHints);
      return answer;
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while performing a query", ex);
    } finally {
      // if any exceptions are thrown, close things down
      // otherwise we're leaving it open for the iterator
      if (answer == null) {
        closeStatement(statement);
        returnConnection(connection);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql) throws DatabaseOperationException {
    return fetch(model, sql, (Table[]) null, 0, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Table[] queryHints) throws DatabaseOperationException {
    return fetch(model, sql, queryHints, 0, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, int start, int end) throws DatabaseOperationException {
    return fetch(model, sql, (Table[]) null, start, end);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Table[] queryHints, int start, int end) throws DatabaseOperationException {
    Connection connection = borrowConnection();
    Statement statement = null;
    ResultSet resultSet;
    List<RowObject> result = new ArrayList<>();

    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(sql);

      int rowIdx = 0;

      for (ModelBasedResultSetIterator it = createResultSetIterator(model, resultSet, queryHints); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++) {
        if (rowIdx >= start) {
          result.add(it.next());
        } else {
          it.advance();
        }
      }
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while fetching data from the database", ex);
    } finally {
      // the iterator should return the connection automatically
      // so this is usually not necessary (but just in case)
      closeStatement(statement);
      returnConnection(connection);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Collection<Object> parameters) throws DatabaseOperationException {
    return fetch(model, sql, parameters, null, 0, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Collection<Object> parameters, int start, int end) throws DatabaseOperationException {
    return fetch(model, sql, parameters, null, start, end);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Collection<Object> parameters, Table[] queryHints) throws DatabaseOperationException {
    return fetch(model, sql, parameters, queryHints, 0, -1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<RowObject> fetch(Database model, String sql, Collection<Object> parameters, Table[] queryHints, int start, int end) throws DatabaseOperationException {
    Connection connection = borrowConnection();
    PreparedStatement statement = null;
    ResultSet resultSet;
    List<RowObject> result = new ArrayList<>();

    try {
      statement = connection.prepareStatement(sql);

      int paramIdx = 1;

      for (Iterator<Object> iterator = parameters.iterator(); iterator.hasNext(); paramIdx++) {
        Object arg = iterator.next();

        if (arg instanceof BigDecimal) {
          // to avoid scale problems because setObject assumes a scale of 0
          statement.setBigDecimal(paramIdx, (BigDecimal) arg);
        } else {
          statement.setObject(paramIdx, arg);
        }
      }
      resultSet = statement.executeQuery();

      int rowIdx = 0;

      for (ModelBasedResultSetIterator it = createResultSetIterator(model, resultSet, queryHints); ((end < 0) || (rowIdx <= end)) && it.hasNext(); rowIdx++) {
        if (rowIdx >= start) {
          result.add(it.next());
        } else {
          it.advance();
        }
      }
    } catch (SQLException ex) {
      // any other exception comes from the iterator which closes the resources automatically
      closeStatement(statement);
      returnConnection(connection);
      throw new DatabaseOperationException("Error while fetching data from the database", ex);
    }
    return result;
  }

  /**
   * Creates the SQL for inserting an object of the given type. If a concrete bean is given,
   * then a concrete insert statement is created, otherwise an insert statement usable in a
   * prepared statement is build.
   *
   * @param model      The database model
   * @param dynaClass  The type
   * @param properties The properties to write
   * @param bean       Optionally the concrete bean to insert
   * @return The SQL required to insert an instance of the class
   */
  protected String createInsertSql(Database model, TableClass dynaClass, ColumnProperty[] properties, RowObject bean) {
    Table table = model.findTable(dynaClass.getTableName());
    HashMap<String, Object> columnValues = toColumnValues(properties, bean);

    return _builder.getInsertSql(table, columnValues, bean == null);
  }

  /**
   * Creates the SQL for querying for the id generated by the last insert of an object of the given type.
   *
   * @param model     The database model
   * @param dynaClass The type
   * @return The SQL required for querying for the id, or <code>null</code> if the database does not
   * support this
   */
  protected String createSelectLastInsertIdSql(Database model, TableClass dynaClass) {
    Table table = model.findTable(dynaClass.getTableName());

    return _builder.getSelectLastIdentityValues(table);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getInsertSql(Database model, RowObject rowObject) {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] properties = dynaClass.getSqlDynaProperties();

    if (properties.length == 0) {
      _log.info("Cannot insert instances of type " + dynaClass + " because it has no properties");
      return null;
    }

    return createInsertSql(model, dynaClass, properties, rowObject);
  }

  /**
   * Returns all properties where the column is not non-autoincrement and for which the bean
   * either has a value or the column hasn't got a default value, for the given dyna class.
   *
   * @param model     The database model
   * @param dynaClass The dyna class
   * @param bean      The bean
   * @return The properties
   */
  private ColumnProperty[] getPropertiesForInsertion(Database model, TableClass dynaClass, final RowObject bean) {
    ColumnProperty[] properties = dynaClass.getSqlDynaProperties();
    return Arrays.stream(properties).filter(prop -> {
      if (bean.get(prop.getName()) != null) {
        // we ignore properties for which a value is present in the bean
        // only if they are identity and identity override is off or
        // the platform does not allow the override of the auto-increment
        // specification
        return !prop.getColumn().isAutoIncrement() ||
          (isIdentityOverrideOn() && getPlatformInfo().isIdentityOverrideAllowed());
      } else {
        // we also return properties without a value in the bean
        // if they ain't auto-increment and don't have a default value
        // in this case, a NULL is inserted
        return !prop.getColumn().isAutoIncrement() &&
          (prop.getColumn().getDefaultValue() == null);
      }
    }).toArray(ColumnProperty[]::new);
  }

  /**
   * Returns all identity properties whose value were defined by the database and which
   * now need to be read back from the DB.
   *
   * @param model     The database model
   * @param dynaClass The dyna class
   * @param bean      The bean
   * @return The columns
   */
  private Column[] getRelevantIdentityColumns(Database model, TableClass dynaClass, final RowObject bean) {
    ColumnProperty[] properties = dynaClass.getSqlDynaProperties();

    return Arrays.stream(properties).filter(input -> {
        // we only want those identity columns that were really specified by the DB
        // if the platform allows specification of values for identity columns
        // in INSERT/UPDATE statements, then we need to filter the corresponding
        // columns out
        return input.getColumn().isAutoIncrement() &&
          (!isIdentityOverrideOn() || !getPlatformInfo().isIdentityOverrideAllowed() || (bean.get(input.getName()) == null));
      })
      .map(ColumnProperty::getColumn)
      .toArray(Column[]::new);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(Connection connection, Database model, RowObject rowObject) throws DatabaseOperationException {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] properties = getPropertiesForInsertion(model, dynaClass, rowObject);
    Column[] autoIncrementColumns = getRelevantIdentityColumns(model, dynaClass, rowObject);

    if ((properties.length == 0) && (autoIncrementColumns.length == 0)) {
      _log.warn("Cannot insert instances of type " + dynaClass + " because it has no usable properties");
      return;
    }

    String insertSql = createInsertSql(model, dynaClass, properties, null);
    String queryIdentitySql = null;

    if (_log.isDebugEnabled()) {
      _log.debug("About to execute SQL: " + insertSql);
    }

    if (autoIncrementColumns.length > 0) {
      if (!getPlatformInfo().isLastIdentityValueReadable()) {
        _log.warn("The database does not support querying for auto-generated column values");
      } else {
        queryIdentitySql = createSelectLastInsertIdSql(model, dynaClass);
      }
    }

    boolean autoCommitMode = false;
    PreparedStatement statement = null;

    try {
      if (!getPlatformInfo().isAutoCommitModeForLastIdentityValueReading()) {
        autoCommitMode = connection.getAutoCommit();
        connection.setAutoCommit(false);
      }

      beforeInsert(connection, dynaClass.getTable());

      statement = connection.prepareStatement(insertSql);

      for (int idx = 0; idx < properties.length; idx++) {
        setObject(statement, idx + 1, rowObject, properties[idx]);
      }

      int count = statement.executeUpdate();

      afterInsert(connection, dynaClass.getTable());

      if (count != 1) {
        _log.warn("Attempted to insert a single row " + rowObject +
          " in table " + dynaClass.getTableName() +
          " but changed " + count + " row(s)");
      }
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while inserting into the database: " + ex.getMessage(), ex);
    } finally {
      closeStatement(statement);
    }
    if (queryIdentitySql != null) {
      Statement queryStmt;
      ResultSet lastInsertedIds = null;

      try {
        if (getPlatformInfo().isAutoCommitModeForLastIdentityValueReading()) {
          // we'll commit the statement(s) if no auto-commit is enabled because
          // otherwise it is possible that the auto increment hasn't happened yet
          // (the db didn't actually perform the insert yet so no triggering of
          // sequences did occur)
          if (!connection.getAutoCommit()) {
            connection.commit();
          }
        }

        queryStmt = connection.createStatement();
        lastInsertedIds = queryStmt.executeQuery(queryIdentitySql);

        lastInsertedIds.next();

        for (int idx = 0; idx < autoIncrementColumns.length; idx++) {
          // we're using the index rather than the name because we cannot know how
          // the SQL statement looks like; rather we assume that we get the values
          // back in the same order as the auto increment columns
          Object value = getObjectFromResultSet(lastInsertedIds, autoIncrementColumns[idx], idx + 1);
          rowObject.set(autoIncrementColumns[idx].getName(), value);
        }
      } catch (SQLException ex) {
        throw new DatabaseOperationException("Error while retrieving the identity column value(s) from the database", ex);
      } finally {
        if (lastInsertedIds != null) {
          try {
            lastInsertedIds.close();
          } catch (SQLException ex) {
            // we ignore this one
          }
        }
        closeStatement(statement);
      }
    }
    if (!getPlatformInfo().isAutoCommitModeForLastIdentityValueReading()) {
      try {
        // we need to do a manual commit now
        connection.commit();
        connection.setAutoCommit(autoCommitMode);
      } catch (SQLException ex) {
        throw new DatabaseOperationException(ex);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(Database model, RowObject rowObject) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      insert(connection, model, rowObject);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(Connection connection, Database model, Collection<RowObject> rowObjects) throws DatabaseOperationException {
    TableClass dynaClass = null;
    ColumnProperty[] properties = null;
    PreparedStatement statement = null;
    int addedStmts = 0;
    boolean identityWarningPrinted = false;

    for (RowObject rowObject : rowObjects) {
      TableClass curDynaClass = model.getTableClassFor(rowObject);

      if (curDynaClass != dynaClass) {
        if (dynaClass != null) {
          executeBatch(statement, addedStmts, dynaClass.getTable());
          addedStmts = 0;
        }

        dynaClass = curDynaClass;
        properties = getPropertiesForInsertion(model, curDynaClass, rowObject);

        if (properties.length == 0) {
          _log.warn("Cannot insert instances of type " + dynaClass + " because it has no usable properties");
          continue;
        }
        if (!identityWarningPrinted &&
          (getRelevantIdentityColumns(model, curDynaClass, rowObject).length > 0)) {
          _log.warn("Updating the bean properties corresponding to auto-increment columns is not supported in batch mode");
          identityWarningPrinted = true;
        }

        String insertSql = createInsertSql(model, dynaClass, properties, null);

        if (_log.isDebugEnabled()) {
          _log.debug("Starting new batch with SQL: " + insertSql);
        }
        try {
          statement = connection.prepareStatement(insertSql);
        } catch (SQLException ex) {
          throw new DatabaseOperationException("Error while preparing insert statement", ex);
        }
      }
      try {
        for (int idx = 0; idx < properties.length; idx++) {
          setObject(statement, idx + 1, rowObject, properties[idx]);
        }
        statement.addBatch();
        addedStmts++;
      } catch (SQLException ex) {
        throw new DatabaseOperationException("Error while adding batch insert", ex);
      }
    }
    if (dynaClass != null) {
      executeBatch(statement, addedStmts, dynaClass.getTable());
    }
  }

  /**
   * Performs the batch for the given statement, and checks that the specified amount of rows has been changed.
   *
   * @param statement The prepared statement
   * @param numRows   The number of rows that should change
   * @param table     The changed table
   */
  private void executeBatch(PreparedStatement statement, int numRows, Table table) throws DatabaseOperationException {
    if (statement != null) {
      try {
        Connection connection = statement.getConnection();

        beforeInsert(connection, table);

        int[] results = statement.executeBatch();

        closeStatement(statement);
        afterInsert(connection, table);

        boolean hasSum = true;
        int sum = 0;

        for (int idx = 0; (results != null) && (idx < results.length); idx++) {
          if (results[idx] < 0) {
            hasSum = false;
            if (results[idx] == Statement.EXECUTE_FAILED) {
              _log.warn("The batch insertion of row " + idx + " into table " + table.getName() + " failed but the driver is able to continue processing");
            } else if (results[idx] != Statement.SUCCESS_NO_INFO) {
              _log.warn("The batch insertion of row " + idx + " into table " + table.getName() + " returned an undefined status value " + results[idx]);
            }
          } else {
            sum += results[idx];
          }
        }
        if (hasSum && (sum != numRows)) {
          _log.warn("Attempted to insert " + numRows + " rows into table " + table.getName() + " but changed " + sum + " rows");
        }
      } catch (SQLException ex) {
        if (ex instanceof BatchUpdateException) {
          SQLException sqlEx = ex.getNextException();

          throw new DatabaseOperationException("Error while inserting into the database", sqlEx);
        } else {
          throw new DatabaseOperationException("Error while inserting into the database", ex);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void insert(Database model, Collection<RowObject> rowObjects) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      insert(connection, model, rowObjects);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * Allows platforms to issue statements directly before rows are inserted into
   * the specified table.
   *
   * @param connection The connection used for the insertion
   * @param table      The table that the rows are inserted into
   */
  protected void beforeInsert(Connection connection, Table table) throws SQLException {
  }

  /**
   * Allows platforms to issue statements directly after rows have been inserted into
   * the specified table.
   *
   * @param connection The connection used for the insertion
   * @param table      The table that the rows have been inserted into
   */
  protected void afterInsert(Connection connection, Table table) throws SQLException {
  }

  /**
   * Creates the SQL for updating an object of the given type. If a concrete bean is given,
   * then a concrete update statement is created, otherwise an update statement usable in a
   * prepared statement is build.
   *
   * @param model       The database model
   * @param dynaClass   The type
   * @param primaryKeys The primary keys
   * @param properties  The properties to write
   * @param bean        Optionally the concrete bean to update
   * @return The SQL required to update the instance
   */
  protected String createUpdateSql(Database model, TableClass dynaClass, ColumnProperty[] primaryKeys, ColumnProperty[] properties, RowObject bean) {
    Table table = model.findTable(dynaClass.getTableName());
    HashMap<String, Object> columnValues = toColumnValues(properties, bean);

    columnValues.putAll(toColumnValues(primaryKeys, bean));

    return _builder.getUpdateSql(table, columnValues, bean == null);
  }

  /**
   * Creates the SQL for updating an object of the given type. If a concrete bean is given,
   * then a concrete update statement is created, otherwise an update statement usable in a
   * prepared statement is build.
   *
   * @param model       The database model
   * @param dynaClass   The type
   * @param primaryKeys The primary keys
   * @param properties  The properties to write
   * @param oldBean     Contains column values to identify the rows to update (i.e. for the WHERE clause)
   * @param newBean     Contains the new column values to write
   * @return The SQL required to update the instance
   */
  protected String createUpdateSql(Database model, TableClass dynaClass, ColumnProperty[] primaryKeys, ColumnProperty[] properties, RowObject oldBean, RowObject newBean) {
    Table table = model.findTable(dynaClass.getTableName());
    HashMap<String, Object> oldColumnValues = toColumnValues(primaryKeys, oldBean);
    HashMap<String, Object> newColumnValues = toColumnValues(properties, newBean);

    if (primaryKeys.length == 0) {
      _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
      return null;
    } else {
      return _builder.getUpdateSql(table, oldColumnValues, newColumnValues, newBean == null);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUpdateSql(Database model, RowObject rowObject) {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();
    ColumnProperty[] nonPrimaryKeys = dynaClass.getNonPrimaryKeyProperties();

    if (primaryKeys.length == 0) {
      _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
      return null;
    } else {
      return createUpdateSql(model, dynaClass, primaryKeys, nonPrimaryKeys, rowObject);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getUpdateSql(Database model, RowObject oldRowObject, RowObject newRowObject) {
    TableClass dynaClass = model.getTableClassFor(oldRowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();
    ColumnProperty[] nonPrimaryKeys = dynaClass.getNonPrimaryKeyProperties();

    if (primaryKeys.length == 0) {
      _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
      return null;
    } else {
      return createUpdateSql(model, dynaClass, primaryKeys, nonPrimaryKeys, oldRowObject, newRowObject);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Connection connection, Database model, RowObject rowObject) throws DatabaseOperationException {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

    if (primaryKeys.length == 0) {
      _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
      return;
    }

    ColumnProperty[] properties = dynaClass.getNonPrimaryKeyProperties();
    String sql = createUpdateSql(model, dynaClass, primaryKeys, properties, null);
    PreparedStatement statement = null;

    if (_log.isDebugEnabled()) {
      _log.debug("About to execute SQL: " + sql);
    }
    try {
      beforeUpdate(connection, dynaClass.getTable());

      statement = connection.prepareStatement(sql);

      int sqlIndex = 1;

      for (ColumnProperty property : properties) {
        setObject(statement, sqlIndex++, rowObject, property);
      }
      for (ColumnProperty primaryKey : primaryKeys) {
        setObject(statement, sqlIndex++, rowObject, primaryKey);
      }

      int count = statement.executeUpdate();

      afterUpdate(connection, dynaClass.getTable());

      if (count != 1) {
        _log.warn("Attempted to insert a single row " + rowObject +
          " into table " + dynaClass.getTableName() +
          " but changed " + count + " row(s)");
      }
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while updating in the database", ex);
    } finally {
      closeStatement(statement);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Database model, RowObject rowObject) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      update(connection, model, rowObject);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Connection connection, Database model, RowObject oldRowObject, RowObject newRowObject) throws DatabaseOperationException {
    TableClass dynaClass = model.getTableClassFor(oldRowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

    if (!dynaClass.getTable().equals(model.getTableClassFor(newRowObject).getTable())) {
      throw new DatabaseOperationException("The old and new dyna beans need to be for the same table");
    }
    if (primaryKeys.length == 0) {
      _log.info("Cannot update instances of type " + dynaClass + " because it has no primary keys");
      return;
    }

    ColumnProperty[] properties = dynaClass.getSqlDynaProperties();
    String sql = createUpdateSql(model, dynaClass, primaryKeys, properties, null, null);
    PreparedStatement statement = null;

    if (_log.isDebugEnabled()) {
      _log.debug("About to execute SQL: " + sql);
    }
    try {
      beforeUpdate(connection, dynaClass.getTable());

      statement = connection.prepareStatement(sql);

      int sqlIndex = 1;

      for (ColumnProperty property : properties) {
        setObject(statement, sqlIndex++, newRowObject, property);
      }
      for (ColumnProperty primaryKey : primaryKeys) {
        setObject(statement, sqlIndex++, oldRowObject, primaryKey);
      }

      int count = statement.executeUpdate();

      afterUpdate(connection, dynaClass.getTable());

      if (count != 1) {
        _log.warn("Attempted to insert a single row " + newRowObject +
          " into table " + dynaClass.getTableName() +
          " but changed " + count + " row(s)");
      }
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while updating in the database", ex);
    } finally {
      closeStatement(statement);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(Database model, RowObject oldRowObject, RowObject newRowObject) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      update(connection, model, oldRowObject, newRowObject);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * Allows platforms to issue statements directly before rows are updated in
   * the specified table.
   *
   * @param connection The connection used for the update
   * @param table      The table that the rows are updated into
   */
  protected void beforeUpdate(Connection connection, Table table) throws SQLException {
  }

  /**
   * Allows platforms to issue statements directly after rows have been updated in
   * the specified table.
   *
   * @param connection The connection used for the update
   * @param table      The table that the rows have been updated into
   */
  protected void afterUpdate(Connection connection, Table table) throws SQLException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean exists(Database model, RowObject rowObject) {
    Connection connection = borrowConnection();

    try {
      return exists(connection, model, rowObject);
    } finally {
      returnConnection(connection);
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean exists(Connection connection, Database model, RowObject rowObject) {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

    if (primaryKeys.length == 0) {
      return false;
    }

    PreparedStatement stmt = null;

    try {
      StringBuilder sql = new StringBuilder();

      sql.append("SELECT * FROM ");
      sql.append(_builder.getDelimitedIdentifier(dynaClass.getTable().getName()));
      sql.append(" WHERE ");

      for (int idx = 0; idx < primaryKeys.length; idx++) {
        String key = primaryKeys[idx].getColumn().getName();

        if (idx > 0) {
          sql.append(" AND ");
        }
        sql.append(_builder.getDelimitedIdentifier(key));
        sql.append("=?");
      }

      stmt = connection.prepareStatement(sql.toString());

      for (int idx = 0; idx < primaryKeys.length; idx++) {
        setObject(stmt, idx + 1, rowObject, primaryKeys[idx]);
      }

      ResultSet resultSet = stmt.executeQuery();

      return resultSet.next();
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while reading from the database", ex);
    } finally {
      closeStatement(stmt);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void store(Database model, RowObject rowObject) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      store(connection, model, rowObject);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void store(Connection connection, Database model, RowObject rowObject) throws DatabaseOperationException {
    if (exists(connection, model, rowObject)) {
      update(connection, model, rowObject);
    } else {
      insert(connection, model, rowObject);
    }
  }

  /**
   * Creates the SQL for deleting an object of the given type. If a concrete bean is given,
   * then a concrete delete statement is created, otherwise a delete statement usable in a
   * prepared statement is build.
   *
   * @param model       The database model
   * @param dynaClass   The type
   * @param primaryKeys The primary keys
   * @param bean        Optionally the concrete bean to update
   * @return The SQL required to delete the instance
   */
  protected String createDeleteSql(Database model, TableClass dynaClass, ColumnProperty[] primaryKeys, RowObject bean) {
    Table table = model.findTable(dynaClass.getTableName());
    HashMap<String, Object> pkValues = toColumnValues(primaryKeys, bean);

    return _builder.getDeleteSql(table, pkValues, bean == null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDeleteSql(Database model, RowObject rowObject) {
    TableClass dynaClass = model.getTableClassFor(rowObject);
    ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

    if (primaryKeys.length == 0) {
      _log.warn("Cannot delete instances of type " + dynaClass + " because it has no primary keys");
      return null;
    } else {
      return createDeleteSql(model, dynaClass, primaryKeys, rowObject);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Database model, RowObject rowObject) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      delete(connection, model, rowObject);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Connection connection, Database model, RowObject rowObject) throws DatabaseOperationException {
    PreparedStatement statement = null;

    try {
      TableClass dynaClass = model.getTableClassFor(rowObject);
      ColumnProperty[] primaryKeys = dynaClass.getPrimaryKeyProperties();

      if (primaryKeys.length == 0) {
        _log.warn("Cannot delete instances of type " + dynaClass + " because it has no primary keys");
        return;
      }

      String sql = createDeleteSql(model, dynaClass, primaryKeys, null);

      if (_log.isDebugEnabled()) {
        _log.debug("About to execute SQL " + sql);
      }

      statement = connection.prepareStatement(sql);

      for (int idx = 0; idx < primaryKeys.length; idx++) {
        setObject(statement, idx + 1, rowObject, primaryKeys[idx]);
      }

      int count = statement.executeUpdate();

      if (count != 1) {
        _log.warn("Attempted to delete a single row " + rowObject +
          " in table " + dynaClass.getTableName() +
          " but changed " + count + " row(s).");
      }
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while deleting from the database", ex);
    } finally {
      closeStatement(statement);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Database readModelFromDatabase(String name) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      return readModelFromDatabase(connection, name);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Database readModelFromDatabase(Connection connection, String name) throws DatabaseOperationException {
    try {
      Database model = getModelReader().getDatabase(connection, name);

      postProcessModelFromDatabase(model);
      return model;
    } catch (SQLException ex) {
      throw new DatabaseOperationException(ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Database readModelFromDatabase(String name, String catalog, String schema, String[] tableTypes) throws DatabaseOperationException {
    Connection connection = borrowConnection();

    try {
      return readModelFromDatabase(connection, name, catalog, schema, tableTypes);
    } finally {
      returnConnection(connection);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Database readModelFromDatabase(Connection connection, String name, String catalog, String schema, String[] tableTypes) throws DatabaseOperationException {
    try {
      JdbcModelReader reader = getModelReader();
      Database model = reader.getDatabase(connection, name, catalog, schema, tableTypes);

      postProcessModelFromDatabase(model);
      if ((model.getName() == null) || (model.getName().isEmpty())) {
        model.setName(MODEL_DEFAULT_NAME);
      }
      return model;
    } catch (SQLException ex) {
      throw new DatabaseOperationException(ex);
    }
  }

  /**
   * Allows the platform to post process the model just read from the database.
   *
   * @param model The model
   */
  protected void postProcessModelFromDatabase(Database model) {
    // Default values for CHAR/VARCHAR/LONGVARCHAR columns have quotation marks
    // around them which we'll remove now
    for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++) {
      Table table = model.getTable(tableIdx);

      for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
        Column column = table.getColumn(columnIdx);

        if (TypeMap.isTextType(column.getTypeCode()) ||
          TypeMap.isDateTimeType(column.getTypeCode())) {
          String defaultValue = column.getDefaultValue();

          if ((defaultValue != null) && (defaultValue.length() >= 2) &&
            defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
            defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
            column.setDefaultValue(defaultValue);
          }
        }
      }
    }
  }

  /**
   * Derives the column values for the given dyna properties from the dyna bean.
   *
   * @param properties The properties
   * @param bean       The bean
   * @return The values indexed by the column names
   */
  protected HashMap<String, Object> toColumnValues(ColumnProperty[] properties, RowObject bean) {
    HashMap<String, Object> result = new HashMap<>();

    for (ColumnProperty property : properties) {
      result.put(property.getName(),
        bean == null ? null : bean.get(property.getName()));
    }
    return result;
  }

  /**
   * Sets a parameter of the prepared statement based on the type of the column of the property.
   *
   * @param statement The statement
   * @param sqlIndex  The index of the parameter to set in the statement
   * @param rowObject  The bean of which to take the value
   * @param property  The property of the bean, which also defines the corresponding column
   */
  protected void setObject(PreparedStatement statement, int sqlIndex, RowObject rowObject, ColumnProperty property) throws SQLException {
    int typeCode = property.getColumn().getTypeCode();
    Object value = rowObject.get(property.getName());

    setStatementParameterValue(statement, sqlIndex, typeCode, value);
  }

  /**
   * This is the core method to set the parameter of a prepared statement to a given value.
   * The primary purpose of this method is to call the appropriate method on the statement,
   * and to give database-specific implementations the ability to change this behavior.
   *
   * @param statement The statement
   * @param sqlIndex  The parameter index
   * @param typeCode  The JDBC type code
   * @param value     The value
   * @throws SQLException If an error occurred while setting the parameter value
   */
  protected void setStatementParameterValue(PreparedStatement statement, int sqlIndex, int typeCode, Object value) throws SQLException {
    if (value == null) {
      statement.setNull(sqlIndex, typeCode);
    } else if (value instanceof String) {
      statement.setString(sqlIndex, (String) value);
    } else if (value instanceof byte[]) {
      statement.setBytes(sqlIndex, (byte[]) value);
    } else if (value instanceof Boolean) {
      statement.setBoolean(sqlIndex, (Boolean) value);
    } else if (value instanceof Byte) {
      statement.setByte(sqlIndex, (Byte) value);
    } else if (value instanceof Short) {
      statement.setShort(sqlIndex, (Short) value);
    } else if (value instanceof Integer) {
      statement.setInt(sqlIndex, (Integer) value);
    } else if (value instanceof Long) {
      statement.setLong(sqlIndex, (Long) value);
    } else if (value instanceof BigDecimal) {
      // setObject assumes a scale of 0, so we rather use the typed setter
      statement.setBigDecimal(sqlIndex, (BigDecimal) value);
    } else if (value instanceof Float) {
      statement.setFloat(sqlIndex, (Float) value);
    } else if (value instanceof Double) {
      statement.setDouble(sqlIndex, (Double) value);
    } else {
      statement.setObject(sqlIndex, value, typeCode);
    }
  }

  /**
   * Helper method esp. for the {@link ModelBasedResultSetIterator} class that retrieves
   * the value for a column from the given result set. If a table was specified,
   * and it contains the column, then the jdbc type defined for the column is used for extracting
   * the value, otherwise the object directly retrieved from the result set is returned.<br/>
   * The method is defined here rather than in the {@link ModelBasedResultSetIterator} class
   * so that concrete platforms can modify its behavior.
   *
   * @param resultSet  The result set
   * @param columnName The name of the column
   * @param table      The table
   * @return The value
   */
  @Override
  public Object getObjectFromResultSet(ResultSet resultSet, String columnName, Table table) throws SQLException {
    Column column = (table == null ? null : table.findColumn(columnName, isDelimitedIdentifierModeOn()));
    Object value;

    if (column != null) {
      int originalJdbcType = column.getTypeCode();
      int targetJdbcType = getPlatformInfo().getTargetJdbcType(originalJdbcType);
      int jdbcType = originalJdbcType;

      // in general, we're trying to retrieve the value using the original type,
      // but sometimes we also need the target type:
      if ((originalJdbcType == Types.BLOB) && (targetJdbcType != Types.BLOB)) {
        // we should not use the Blob interface if the database doesn't map to this type
        jdbcType = targetJdbcType;
      }
      if ((originalJdbcType == Types.CLOB) && (targetJdbcType != Types.CLOB)) {
        // we should not use the Clob interface if the database doesn't map to this type
        jdbcType = targetJdbcType;
      }
      value = extractColumnValue(resultSet, columnName, 0, jdbcType);
    } else {
      value = resultSet.getObject(columnName);
    }
    return resultSet.wasNull() ? null : value;
  }

  /**
   * Helper method for retrieving the value for a column from the given result set
   * using the type code of the column.
   *
   * @param resultSet The result set
   * @param column    The column
   * @param idx       The value's index in the result set (starting from 1)
   * @return The value
   */
  @Override
  public Object getObjectFromResultSet(ResultSet resultSet, Column column, int idx) throws SQLException {
    int originalJdbcType = column.getTypeCode();
    int targetJdbcType = getPlatformInfo().getTargetJdbcType(originalJdbcType);
    int jdbcType = originalJdbcType;
    Object value;

    // in general, we're trying to retrieve the value using the original type,
    // but sometimes we also need the target type:
    if ((originalJdbcType == Types.BLOB) && (targetJdbcType != Types.BLOB)) {
      // we should not use the Blob interface if the database doesn't map to this type
      jdbcType = targetJdbcType;
    }
    if ((originalJdbcType == Types.CLOB) && (targetJdbcType != Types.CLOB)) {
      // we should not use the Clob interface if the database doesn't map to this type
      jdbcType = targetJdbcType;
    }
    value = extractColumnValue(resultSet, null, idx, jdbcType);
    return resultSet.wasNull() ? null : value;
  }

  /**
   * This is the core method to retrieve a value for a column from a result set. Its  primary
   * purpose is to call the appropriate method on the result set, and to provide an extension
   * point where database-specific implementations can change this behavior.
   *
   * @param resultSet  The result set to extract the value from
   * @param columnName The name of the column; can be <code>null</code> in which case the
   *                   <code>columnIdx</code> will be used instead
   * @param columnIdx  The index of the column's value in the result set; is only used if
   *                   <code>columnName</code> is <code>null</code>
   * @param jdbcType   The jdbc type to extract
   * @return The value
   * @throws SQLException If an error occurred while accessing the result set
   */
  protected Object extractColumnValue(ResultSet resultSet, String columnName, int columnIdx, int jdbcType) throws SQLException {
    boolean useIdx = (columnName == null);
    Object value;

    switch (jdbcType) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        value = useIdx ? resultSet.getString(columnIdx) : resultSet.getString(columnName);
        break;
      case Types.NUMERIC:
      case Types.DECIMAL:
        value = useIdx ? resultSet.getBigDecimal(columnIdx) : resultSet.getBigDecimal(columnName);
        break;
      case Types.BIT:
      case Types.BOOLEAN:
        value = useIdx ? resultSet.getBoolean(columnIdx) : resultSet.getBoolean(columnName);
        break;
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        value = useIdx ? resultSet.getInt(columnIdx) : resultSet.getInt(columnName);
        break;
      case Types.BIGINT:
        value = useIdx ? resultSet.getLong(columnIdx) : resultSet.getLong(columnName);
        break;
      case Types.REAL:
        value = useIdx ? resultSet.getFloat(columnIdx) : resultSet.getFloat(columnName);
        break;
      case Types.FLOAT:
      case Types.DOUBLE:
        value = useIdx ? resultSet.getDouble(columnIdx) : resultSet.getDouble(columnName);
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        value = useIdx ? resultSet.getBytes(columnIdx) : resultSet.getBytes(columnName);
        break;
      case Types.DATE:
        value = useIdx ? resultSet.getDate(columnIdx) : resultSet.getDate(columnName);
        break;
      case Types.TIME:
        value = useIdx ? resultSet.getTime(columnIdx) : resultSet.getTime(columnName);
        break;
      case Types.TIMESTAMP:
        value = useIdx ? resultSet.getTimestamp(columnIdx) : resultSet.getTimestamp(columnName);
        break;
      case Types.CLOB:
        Clob clob = useIdx ? resultSet.getClob(columnIdx) : resultSet.getClob(columnName);

        if (clob == null) {
          value = null;
        } else {
          long length = clob.length();

          if (length > Integer.MAX_VALUE) {
            value = clob;
          } else if (length == 0) {
            // the javadoc is not clear about whether Clob.getSubString
            // can be used with a substring length of 0
            // thus we do the safe thing and handle it ourselves
            value = "";
          } else {
            value = clob.getSubString(1L, (int) length);
          }
        }
        break;
      case Types.BLOB:
        Blob blob = useIdx ? resultSet.getBlob(columnIdx) : resultSet.getBlob(columnName);

        if (blob == null) {
          value = null;
        } else {
          long length = blob.length();

          if (length > Integer.MAX_VALUE) {
            value = blob;
          } else if (length == 0) {
            // the javadoc is not clear about whether Blob.getBytes
            // can be used with for 0 bytes to be copied
            // thus we do the safe thing and handle it ourselves
            value = new byte[0];
          } else {
            value = blob.getBytes(1L, (int) length);
          }
        }
        break;
      case Types.ARRAY:
        value = useIdx ? resultSet.getArray(columnIdx) : resultSet.getArray(columnName);
        break;
      case Types.REF:
        value = useIdx ? resultSet.getRef(columnIdx) : resultSet.getRef(columnName);
        break;
      default:
        value = useIdx ? resultSet.getObject(columnIdx) : resultSet.getObject(columnName);
        break;
    }
    return resultSet.wasNull() ? null : value;
  }


  /**
   * Creates an iterator over the given result set.
   *
   * @param model      The database model
   * @param resultSet  The result set to iterate over
   * @param queryHints The tables that were queried in the query that produced the
   *                   given result set (optional)
   * @return The iterator
   */
  protected ModelBasedResultSetIterator createResultSetIterator(Database model, ResultSet resultSet, Table[] queryHints) {
    return new ModelBasedResultSetIterator(this, model, resultSet, queryHints, true);
  }
}
