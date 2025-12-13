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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * The SQL Builder for the Mckoi database.
 *
 * @version $Revision$
 */
public class MckoiBuilder extends SqlBuilder {
  /**
   * Creates a new builder instance.
   *
   * @param platform The platform this builder belongs to
   */
  public MckoiBuilder(Platform platform) {
    super(platform);
    // we need to handle the backslash first otherwise the other
    // already escaped sequence would be affected
    addEscapedCharSequence("\\", "\\\\");
    addEscapedCharSequence("'", "\\'");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createTable(Database database, Table table, Map<String, Object> parameters) throws IOException {
    // we use sequences instead of the UNIQUEKEY function because this way
    // we can read their values back
    Column[] columns = table.getAutoIncrementColumns();

    for (Column column : columns) {
      createAutoIncrementSequence(table, column);
    }

    super.createTable(database, table, parameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropTable(Table table) throws IOException {
    print("DROP TABLE IF EXISTS ");
    printIdentifier(getTableName(table));
    printEndOfStatement();

    Column[] columns = table.getAutoIncrementColumns();

    for (Column column : columns) {
      dropAutoIncrementSequence(table, column);
    }
  }

  /**
   * Creates the sequence necessary for the auto-increment of the given column.
   *
   * @param table  The table
   * @param column The column
   */
  protected void createAutoIncrementSequence(Table table,
                                             Column column) throws IOException {
    print("CREATE SEQUENCE ");
    printIdentifier(getConstraintName("seq",
      table,
      column.getName(),
      null));
    printEndOfStatement();
  }

  /**
   * Drops the sequence used for the auto-increment of the given column.
   *
   * @param table  The table
   * @param column The column
   */
  protected void dropAutoIncrementSequence(Table table,
                                           Column column) throws IOException {
    print("DROP SEQUENCE ");
    printIdentifier(getConstraintName("seq",
      table,
      column.getName(),
      null));
    printEndOfStatement();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void writeColumnDefaultValue(Table table, Column column) throws IOException {
    if (column.isAutoIncrement()) {
      // we start at value 1 to avoid issues with jdbc
      print("NEXTVAL('");
      print(getConstraintName("seq", table, column.getName(), null));
      print("')");
    } else {
      super.writeColumnDefaultValue(table, column);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSelectLastIdentityValues(Table table) {
    Column[] columns = table.getAutoIncrementColumns();

    if (columns.length > 0) {
      StringBuilder result = new StringBuilder();

      result.append("SELECT ");
      for (int idx = 0; idx < columns.length; idx++) {
        if (idx > 0) {
          result.append(",");
        }
        result.append("CURRVAL('");
        result.append(getConstraintName("seq", table, columns[idx].getName(), null));
        result.append("')");
      }
      return result.toString();
    } else {
      return null;
    }
  }

  /**
   * Writes the SQL to recreate a table.
   *
   * @param model      The database model
   * @param table      The table to recreate
   * @param parameters The table creation parameters
   */
  protected void writeRecreateTableStmt(Database model, Table table, Map<String, Object> parameters) throws IOException {
    print("ALTER ");
    super.createTable(model, table, parameters);
  }
}
