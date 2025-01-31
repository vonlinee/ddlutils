package org.apache.ddlutils.platform.mckoi;

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
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.ColumnDefinitionChange;
import org.apache.ddlutils.alteration.RecreateTableChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultTableDefinitionChangesPredicate;
import org.apache.ddlutils.platform.PlatformImplBase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * The Mckoi database platform implementation.
 *
 * @version $Revision: 231306 $
 */
public class MckoiPlatform extends PlatformImplBase {

  /**
   * Creates a new platform instance.
   */
  public MckoiPlatform() {
    PlatformInfo info = getPlatformInfo();

    info.setIndicesSupported(false);
    info.setIndicesEmbedded(true);
    info.setDefaultValueUsedForIdentitySpec(true);
    info.setAutoCommitModeForLastIdentityValueReading(false);

    info.addNativeTypeMapping(Types.ARRAY, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.BIT, "BOOLEAN", Types.BOOLEAN);
    info.addNativeTypeMapping(Types.DATALINK, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.DISTINCT, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.FLOAT, "DOUBLE", Types.DOUBLE);
    info.addNativeTypeMapping(Types.NULL, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.OTHER, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.REF, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.STRUCT, "BLOB", Types.BLOB);

    info.setDefaultSize(Types.CHAR, 1024);
    info.setDefaultSize(Types.VARCHAR, 1024);
    info.setDefaultSize(Types.BINARY, 1024);
    info.setDefaultSize(Types.VARBINARY, 1024);

    setSqlBuilder(new MckoiBuilder(this));
    setModelReader(new MckoiModelReader(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return BuiltinDriverType.McKoi.getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createDatabase(String jdbcDriverClassName, String connectionUrl, String username, String password, Map<String, Object> parameters) throws DatabaseOperationException, UnsupportedOperationException {
    // For McKoi, you create databases by simply appending "?create=true" to the connection url
    if (BuiltinDriverType.McKoi.getDriverClassName().equals(jdbcDriverClassName)) {
      StringBuilder creationUrl = new StringBuilder();
      Connection connection = null;

      creationUrl.append(connectionUrl);
      // TODO: It might be safer to parse the URN and check whether there is already a parameter there
      //       (in which case e'd have to use '&' instead)
      creationUrl.append("?create=true");
      if ((parameters != null) && !parameters.isEmpty()) {
        for (Map.Entry<String, Object> stringObjectEntry : parameters.entrySet()) {

          // no need to specify create twice (and create=false wouldn't help anyway)
          if (!"create".equalsIgnoreCase(stringObjectEntry.getKey())) {
            creationUrl.append("&");
            creationUrl.append(stringObjectEntry.getKey());
            creationUrl.append("=");
            if (stringObjectEntry.getValue() != null) {
              creationUrl.append(stringObjectEntry.getValue());
            }
          }
        }
      }
      if (getLog().isDebugEnabled()) {
        getLog().debug("About to create database using this URL: " + creationUrl);
      }
      try {
        Class.forName(jdbcDriverClassName);

        connection = DriverManager.getConnection(creationUrl.toString(), username, password);
        logWarnings(connection);
      } catch (Exception ex) {
        throw new DatabaseOperationException("Error while trying to create a database", ex);
      } finally {
        if (connection != null) {
          try {
            connection.close();
          } catch (SQLException ignored) {
          }
        }
      }
    } else {
      throw new UnsupportedOperationException("Unable to create a McKoi database via the driver " + jdbcDriverClassName);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate() {
    return new DefaultTableDefinitionChangesPredicate() {
      @Override
      public boolean areSupported(Table intermediateTable, List<TableChange> changes) {
        // McKoi has this nice ALTER CREATE TABLE statement which saves us a lot of work
        // Thus, we reject all table level changes and instead redefine the handling of the
        // RecreateTableChange
        return false;
      }
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processChange(Database currentModel, CreationParameters params, RecreateTableChange change) throws IOException {
    // McKoi has this nice ALTER CREATE TABLE statement which saves us a lot of work
    // We only have to handle auto-increment changes manually
    MckoiBuilder sqlBuilder = (MckoiBuilder) getSqlBuilder();
    Table changedTable = findChangedTable(currentModel, change);

    for (TableChange tableChange : change.getOriginalChanges()) {
      if (tableChange instanceof ColumnDefinitionChange) {
        ColumnDefinitionChange colChange = (ColumnDefinitionChange) tableChange;
        Column origColumn = changedTable.findColumn(colChange.getChangedColumn(), isDelimitedIdentifierModeOn());
        Column newColumn = colChange.getNewColumn();

        if (!origColumn.isAutoIncrement() && newColumn.isAutoIncrement()) {
          sqlBuilder.createAutoIncrementSequence(changedTable, origColumn);
        }
      } else if (tableChange instanceof AddColumnChange) {
        AddColumnChange addColumnChange = (AddColumnChange) tableChange;
        if (addColumnChange.getNewColumn().isAutoIncrement()) {
          sqlBuilder.createAutoIncrementSequence(changedTable, addColumnChange.getNewColumn());
        }
      }
    }

    Map<String, Object> parameters = (params == null ? null : params.getParametersFor(changedTable));

    sqlBuilder.writeRecreateTableStmt(currentModel, change.getTargetTable(), parameters);

    // we have to defer removal of the sequences until they are no longer used
    for (TableChange tableChange : change.getOriginalChanges()) {
      if (tableChange instanceof ColumnDefinitionChange) {
        ColumnDefinitionChange colChange = (ColumnDefinitionChange) tableChange;
        Column origColumn = changedTable.findColumn(colChange.getChangedColumn(), isDelimitedIdentifierModeOn());
        Column newColumn = colChange.getNewColumn();

        if (origColumn.isAutoIncrement() && !newColumn.isAutoIncrement()) {
          sqlBuilder.dropAutoIncrementSequence(changedTable, origColumn);
        }
      } else if (tableChange instanceof RemoveColumnChange) {
        RemoveColumnChange removeColumnChange = (RemoveColumnChange) tableChange;
        Column removedColumn = changedTable.findColumn(removeColumnChange.getChangedColumn(), isDelimitedIdentifierModeOn());

        if (removedColumn.isAutoIncrement()) {
          sqlBuilder.dropAutoIncrementSequence(changedTable, removedColumn);
        }
      }
    }
  }
}
