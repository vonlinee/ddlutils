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

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.task.Parameter;
import org.apache.ddlutils.task.TableSpecificParameter;

import java.util.Properties;

/**
 * Base type for database commands that use creation parameters.
 *
 * @version $Revision: 289996 $
 * @ant.type ignore="true"
 */
public abstract class DatabaseCommandWithCreationParameters extends DatabaseCommand {

  public DatabaseCommandWithCreationParameters(Properties properties) {
    super(properties);
  }

  /**
   * Adds a parameter which is a name-value pair.
   *
   * @param param The parameter
   */
  public void addConfiguredParameter(TableSpecificParameter param) {
    _parameters.add(param);
  }

  /**
   * Filters the parameters for the given model and platform.
   *
   * @param model           The database model
   * @param platformName    The name of the platform
   * @param isCaseSensitive Whether case is relevant when comparing names of tables
   * @return The filtered parameters
   */
  protected CreationParameters getFilteredParameters(Database model, String platformName, boolean isCaseSensitive) {
    CreationParameters parameters = new CreationParameters();

    for (Parameter parameter : _parameters) {
      TableSpecificParameter param = (TableSpecificParameter) parameter;

      if (param.isForPlatform(platformName)) {
        for (int idx = 0; idx < model.getTableCount(); idx++) {
          Table table = model.getTable(idx);

          if (param.isForTable(table, isCaseSensitive)) {
            parameters.addParameter(table, param.getName(), param.getValue());
          }
        }
      }
    }
    return parameters;
  }
}
