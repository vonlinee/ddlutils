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

package org.apache.ddlutils.platform.mysql;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Attributes;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;

import java.io.IOException;
import java.util.Map;

/**
 * The SQL Builder for MySQL version 8 and above.
 */
public class MySql8Builder extends MySqlBuilder {
  /**
   * Creates a new builder instance.
   *
   * @param platform The platform this builder belongs to
   */
  public MySql8Builder(Platform platform) {
    super(platform);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void copyData(Table sourceTable, Table targetTable) throws IOException {
    print("SET sql_mode=''");
    printEndOfStatement();
    super.copyData(sourceTable, targetTable);
  }

  @Override
  public void createTables(Database database, CreationParameters params, boolean dropTables) throws IOException {
    params.addParameter(null, "customLayout", "true");
    // put drop table SQL close to create table SQL
    super.createTables(database, params, false);
  }

  @Override
  public void dropTable(Table table) throws IOException {
    print("DROP TABLE IF EXISTS ");
    printIdentifier(getTableName(table));
    // remove the new blank line between `DROP TABLE...` AND `CREATE TABLE...`
    printlnSqlCommandDelimiter();
  }

  @Override
  protected void writeTableAlterStmt(Table table) throws IOException {
    print("ALTER TABLE ");
    // do not create next line
    print(getDelimitedIdentifier(getTableName(table)));
    printIndent();
  }

  @Override
  public void createTable(Database database, Table table, Map<String, Object> parameters) throws IOException {
    if ("true".equals(parameters.get("customLayout"))) {
      parameters.remove("customLayout");
      dropForeignKeys(table);
      dropTable(table);
    }
    Attributes attributes = table.getAttributes();

    parameters.put("ENGINE", attributes.get(Attributes.ENGINE));
    parameters.put("AUTO_INCREMENT", attributes.get(Attributes.AUTO_INCREMENT));
    parameters.put("DEFAULT CHARSET", attributes.get(Attributes.CHARSET));
    parameters.put("COLLATE", attributes.get(Attributes.TABLE_COLLATION));

    super.createTable(database, table, parameters);
  }
}
