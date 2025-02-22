package org.apache.ddlutils.platform.mysql;

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
import org.apache.ddlutils.model.Table;

import java.io.IOException;

/**
 * The SQL Builder for MySQL version 5 and above.
 *
 * @version $Revision: $
 */
public class MySql50Builder extends MySqlBuilder {
  /**
   * Creates a new builder instance.
   *
   * @param platform The platform this builder belongs to
   */
  public MySql50Builder(Platform platform) {
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
}
