package org.apache.ddlutils.platform.maxdb;

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
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.sapdb.SapDbBuilder;

import java.io.IOException;

/**
 * The SQL Builder for MaxDB.
 *
 * @version $Revision: $
 */
public class MaxDbBuilder extends SapDbBuilder {
  /**
   * Creates a new builder instance.
   *
   * @param platform The platform this builder belongs to
   */
  public MaxDbBuilder(Platform platform) {
    super(platform);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createPrimaryKey(Table table, Column[] primaryKeyColumns) throws IOException {
    if ((primaryKeyColumns.length > 0) && shouldGeneratePrimaryKeys(primaryKeyColumns)) {
      print("ALTER TABLE ");
      printlnIdentifier(getTableName(table));
      printIndent();
      print("ADD CONSTRAINT ");
      printIdentifier(getConstraintName(null, table, "PK", null));
      print(" ");
      writePrimaryKeyStmt(table, primaryKeyColumns);
      printEndOfStatement();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropForeignKey(Table table, ForeignKey foreignKey) throws IOException {
    writeTableAlterStmt(table);
    print("DROP CONSTRAINT ");
    printIdentifier(getForeignKeyName(table, foreignKey));
    printEndOfStatement();
  }
}
