package org.apache.ddlutils.platform.db2;

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

/**
 * The DB2 platform implementation for DB2 v8 and above.
 *
 * @version $Revision: $
 */
public class Db2v8Platform extends Db2Platform {

  /**
   * Creates a new platform instance.
   */
  public Db2v8Platform() {
    super();
    // DB2 v8 has a maximum identifier length of 128 bytes for things like table names,
    // stored procedure names etc., 30 bytes for column names and 18 bytes for foreign key names
    // Note that we optimistically assume that number of characters = number of bytes
    // If the name contains characters that are more than one byte in the database's
    // encoding, then the db will report an error anyway, but we cannot really calculate
    // the number of bytes
    getPlatformInfo().setMaxIdentifierLength(128);
    getPlatformInfo().setMaxColumnNameLength(30);
    getPlatformInfo().setMaxConstraintNameLength(18);
    getPlatformInfo().setMaxForeignKeyNameLength(18);
    setSqlBuilder(new Db2v8Builder(this));
  }
}
