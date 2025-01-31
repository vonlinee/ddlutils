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
import org.apache.ddlutils.task.Parameter;
import org.apache.ddlutils.task.PlatformConfiguration;
import org.apache.tools.ant.BuildException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base type for commands that have the database info embedded.
 *
 * @version $Revision: 289996 $
 * @ant.type ignore="true"
 */
public abstract class DatabaseCommand extends Command {

  /**
   * The additional creation parameters.
   */
  protected final ArrayList<Parameter> _parameters = new ArrayList<>();

  private final Properties properties;

  public DatabaseCommand() {
    this.properties = new Properties();
  }

  public DatabaseCommand(Properties properties) {
    this.properties = properties;
  }

  protected void createPlatformDatabase(Platform platform) {
    String driverClassName = properties.getProperty("driverClassName");
    String url = properties.getProperty("url");
    String username = properties.getProperty("username");
    String password = properties.getProperty("password");
    platform.createDatabase(driverClassName,
      url,
      username,
      password,
      getFilteredParameters(platform.getName()));
  }

  /**
   * The platform configuration.
   */
  private PlatformConfiguration _platformConf = new PlatformConfiguration();

  /**
   * Returns the database type.
   *
   * @return The database type
   */
  public String getDatabaseType() {
    return _platformConf.getDatabaseType();
  }

  /**
   * Returns the data source to use for accessing the database.
   *
   * @return The data source
   */
  public DataSource getDataSource() {
    return _platformConf.getDataSource();
  }

  /**
   * Returns the catalog pattern if any.
   *
   * @return The catalog pattern
   */
  public String getCatalogPattern() {
    return _platformConf.getCatalogPattern();
  }

  /**
   * Returns the schema pattern if any.
   *
   * @return The schema pattern
   */
  public String getSchemaPattern() {
    return _platformConf.getSchemaPattern();
  }

  /**
   * Sets the platform configuration.
   *
   * @param platformConf The platform configuration
   */
  public void setPlatformConfiguration(PlatformConfiguration platformConf) {
    _platformConf = platformConf;
  }

  /**
   * Creates the platform for the configured database.
   *
   * @return The platform
   */
  public Platform getPlatform() throws BuildException {
    return _platformConf.getPlatform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRequiringModel() {
    return true;
  }

  /**
   * Filters the parameters for the indicated platform.
   *
   * @param platformName The name of the platform
   * @return The filtered parameters
   */
  public Map<String, Object> getFilteredParameters(String platformName) {
    LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

    for (Parameter param : _parameters) {
      if (param.isForPlatform(platformName)) {
        parameters.put(param.getName(), param.getValue());
      }
    }
    return parameters;
  }
}
