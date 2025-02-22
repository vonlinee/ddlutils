package org.apache.ddlutils.task.command;

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
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.task.DatabaseTask;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Sub-task for dropping the target database. Note that this is only supported on some database
 * platforms. See the database support documentation for details on which platforms support this.<br/>
 * This sub-task does not require schema files. Therefore, the <code>fileset</code> sub elements and
 * the <code>schemaFile</code> attribute of the enclosing task can be omitted.
 *
 * @version $Revision: 289996 $
 * @ant.task name="dropDatabase"
 */
public class DropDatabaseCommand extends DatabaseCommand {

  public DropDatabaseCommand(Properties properties) {
    super(properties);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRequiringModel() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(DatabaseTask task, Database model) throws CommandExecuteException {
    DataSource dataSource = getDataSource();

    if (dataSource == null) {
      throw new CommandExecuteException("No database specified.");
    }

    Platform platform = getPlatform();

    try {
      createPlatformDatabase(platform);
    } catch (UnsupportedOperationException ex) {
      throw new CommandExecuteException("Database platform " + platform.getName() + " does not support database dropping via JDBC",
        ex);
    } catch (Exception ex) {
      handleException(ex, ex.getMessage());
    }
  }
}
