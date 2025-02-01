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
import org.apache.ddlutils.task.Parameter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * The sub-task for creating the target database. Note that this is only supported on some database
 * platforms. See the database support documentation for details on which platforms support this.<br/>
 * This sub-task does not require schema files. Therefore, the <code>fileset</code> sub elements and
 * the <code>schemaFile</code> attribute of the enclosing task can be omitted.
 *
 * @version $Revision: 231306 $
 * @ant.task name="createDatabase"
 */
public class CreateDatabaseCommand extends DatabaseCommand {

  public CreateDatabaseCommand(Properties properties) {
    super(properties);
  }

  /**
   * Adds a parameter which is a name-value pair.
   *
   * @param param The parameter
   */
  public void addConfiguredParameter(Parameter param) {
    _parameters.add(param);
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
    Properties properties = task.getProperties();

    String url = properties.getProperty("url");
    String driverClassName = properties.getProperty("driverClassName");

    Platform platform = getPlatform(url, driverClassName);
    try {
      createPlatformDatabase(platform);
    } catch (UnsupportedOperationException ex) {
      throw new CommandExecuteException("Database platform " + platform.getName() + " does not support database creation " +
        "via JDBC or there was an error while creating it.",
        ex);
    } catch (Exception ex) {
      handleException(ex, ex.getMessage());
    }
  }
}
