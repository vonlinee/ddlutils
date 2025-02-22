package org.apache.ddlutils.task;

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
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformUtils;

import javax.sql.DataSource;

/**
 * Encloses the platform configuration for the Ant tasks.
 *
 * @version $Revision: 329426 $
 * @ant.type ignore="true"
 */
public class PlatformConfiguration {
  /**
   * The type of the database.
   */
  private String _databaseType;
  /**
   * The data source to use for accessing the database.
   */
  private DataSource _dataSource;
  /**
   * Whether to use delimited SQL identifiers.
   */
  private boolean _useDelimitedSqlIdentifiers;
  /**
   * Whether read foreign keys shall be sorted.
   */
  private boolean _sortForeignKeys;
  /**
   * Whether to shut down the database after the task has finished.
   */
  private boolean _shutdownDatabase;
  /**
   * The catalog pattern.
   */
  private String _catalogPattern;

  /**
   * the driver class name
   */
  private String driverClassName;

  /**
   * The schema pattern.
   */
  private String _schemaPattern;
  /**
   * The platform object.
   */
  private Platform _platform;

  /**
   * Returns the database type.
   *
   * @return The database type
   */
  public String getDatabaseType() {
    return _databaseType;
  }

  /**
   * Sets the database type.
   *
   * @param type The database type
   */
  public void setDatabaseType(String type) {
    _databaseType = type;
  }

  /**
   * Returns the data source to use for accessing the database.
   *
   * @return The data source
   */
  public DataSource getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data source to use for accessing the database.
   *
   * @param dataSource The data source pointing to the database
   */
  public void setDataSource(DataSource dataSource) {
    _dataSource = dataSource;
  }

  /**
   * Returns the catalog pattern if any.
   *
   * @return The catalog pattern
   */
  public String getCatalogPattern() {
    return _catalogPattern;
  }

  /**
   * Sets the catalog pattern.
   *
   * @param catalogPattern The catalog pattern
   */
  public void setCatalogPattern(String catalogPattern) {
    _catalogPattern = catalogPattern;
  }

  /**
   * Returns the schema pattern if any.
   *
   * @return The schema pattern
   */
  public String getSchemaPattern() {
    return _schemaPattern;
  }

  /**
   * Sets the schema pattern.
   *
   * @param schemaPattern The schema pattern
   */
  public void setSchemaPattern(String schemaPattern) {
    _schemaPattern = schemaPattern;
  }

  /**
   * Determines whether delimited SQL identifiers shall be used (the default).
   *
   * @return <code>true</code> if delimited SQL identifiers shall be used
   */
  public boolean isUseDelimitedSqlIdentifiers() {
    return _useDelimitedSqlIdentifiers;
  }

  /**
   * Specifies whether delimited SQL identifiers shall be used.
   *
   * @param useDelimitedSqlIdentifiers <code>true</code> if delimited SQL identifiers shall be used
   */
  public void setUseDelimitedSqlIdentifiers(boolean useDelimitedSqlIdentifiers) {
    _useDelimitedSqlIdentifiers = useDelimitedSqlIdentifiers;
  }

  /**
   * Determines whether a table's foreign keys read from a live database
   * shall be sorted alphabetically. Is <code>false</code> by default.
   *
   * @return <code>true</code> if the foreign keys shall be sorted
   */
  public boolean isSortForeignKeys() {
    return _sortForeignKeys;
  }

  /**
   * Specifies whether a table's foreign keys read from a live database
   * shall be sorted alphabetically.
   *
   * @param sortForeignKeys <code>true</code> if the foreign keys shall be sorted
   */
  public void setSortForeignKeys(boolean sortForeignKeys) {
    _sortForeignKeys = sortForeignKeys;
  }

  /**
   * Determines whether the database shall be shut down after the task has finished.
   *
   * @return <code>true</code> if the database shall be shut down
   */
  public boolean isShutdownDatabase() {
    return _shutdownDatabase;
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  /**
   * Specifies whether the database shall be shut down after the task has finished.
   *
   * @param shutdownDatabase <code>true</code> if the database shall be shut down
   */
  public void setShutdownDatabase(boolean shutdownDatabase) {
    _shutdownDatabase = shutdownDatabase;
  }

  /**
   * Creates the platform for the configured database.
   *
   * @return The platform
   */
  public Platform getPlatform(String url, String driverClassName) throws RuntimeException {
    if (_platform == null) {
      if (_databaseType == null) {
        if (_dataSource == null) {
          throw new RuntimeException("No database specified.");
        }
        _databaseType = new PlatformUtils().determineDatabaseType(driverClassName, url);
        if (_databaseType == null) {
          _databaseType = new PlatformUtils().determineDatabaseType(_dataSource);
        }
      }
      try {
        _platform = PlatformFactory.createNewPlatformInstance(_databaseType);
      } catch (Exception ex) {
        throw new RuntimeException("Database type " + _databaseType + " is not supported.", ex);
      }
      if (_platform == null) {
        throw new RuntimeException("Database type " + _databaseType + " is not supported.");
      }
      _platform.setDataSource(_dataSource);
      _platform.setDelimitedIdentifierModeOn(isUseDelimitedSqlIdentifiers());
      _platform.setForeignKeysSorted(isSortForeignKeys());
    }

    return _platform;
  }
}
