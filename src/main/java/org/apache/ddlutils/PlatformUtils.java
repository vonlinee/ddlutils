package org.apache.ddlutils;

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

import org.apache.ddlutils.platform.BuiltinDriverType;
import org.apache.ddlutils.util.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility functions for dealing with database platforms.
 *
 * @version $Revision: 279421 $
 */
public class PlatformUtils {

  /**
   * Maps the sub-protocol part of a jdbc connection url to a OJB platform name.
   */
  private final HashMap<String, String> jdbcSubProtocolToPlatform = new HashMap<>();
  /**
   * Maps the jdbc driver name to a OJB platform name.
   */
  private final HashMap<String, String> jdbcDriverToPlatform = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public PlatformUtils() {
    // Note that currently SapDB and MaxDB have equal sub protocols and
    // drivers, so we have no means to distinguish them
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.AXION.getSubProtocol(), BuiltinDriverType.AXION.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.CLOUDSCAPE1.getSubProtocol(), BuiltinDriverType.CLOUDSCAPE1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.CLOUDSCAPE2.getSubProtocol(), BuiltinDriverType.CLOUDSCAPE2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DB2.getSubProtocol(), BuiltinDriverType.DB2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DB2_OS390_1.getSubProtocol(), BuiltinDriverType.DB2_OS390_1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DB2_OS390_2.getSubProtocol(), BuiltinDriverType.DB2_OS390_2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DB2_JTOPEN.getSubProtocol(), BuiltinDriverType.DB2_JTOPEN.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DATADIRECT_DB2.getSubProtocol(), BuiltinDriverType.DATADIRECT_DB2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_DB2.getSubProtocol(), BuiltinDriverType.INET_DB2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DERBY.getSubProtocol(), BuiltinDriverType.DERBY.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.FIREBIRD.getSubProtocol(), BuiltinDriverType.FIREBIRD.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.HSQL.getSubProtocol(), BuiltinDriverType.HSQL.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INTERBASE.getSubProtocol(), BuiltinDriverType.INTERBASE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.SAPDB.getSubProtocol(), BuiltinDriverType.SAPDB.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.MCKOI.getSubProtocol(), BuiltinDriverType.MCKOI.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.SQLSERVER.getSubProtocol(), BuiltinDriverType.SQLSERVER.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.SQLSERVER2005_NEW1.getSubProtocol(), BuiltinDriverType.SQLSERVER2005_NEW1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.SQLSERVER2005_NEW.getSubProtocol(), BuiltinDriverType.SQLSERVER2005_NEW.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DATADIRECT_SQLSERVER.getSubProtocol(), BuiltinDriverType.DATADIRECT_SQLSERVER.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER6.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER6.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7A.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7A.getName());

    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER_POOLED1.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER_POOLED1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER6_POOLED1.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER6_POOLED1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7_POOLED1.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7_POOLED1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7A_POOLED1.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7A_POOLED1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER_POOLED2.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER_POOLED2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER6_POOLED2.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER6_POOLED2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7_POOLED2.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7_POOLED2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SQLSERVER7A_POOLED2.getSubProtocol(), BuiltinDriverType.INET_SQLSERVER7A_POOLED2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.JSQLCONNECT_SQLSERVER.getSubProtocol(), BuiltinDriverType.JSQLCONNECT_SQLSERVER.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.JTDS_SQLSERVER.getSubProtocol(), BuiltinDriverType.JTDS_SQLSERVER.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.MYSQL.getSubProtocol(), BuiltinDriverType.MYSQL.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.ORACLE8.getSubProtocol(), BuiltinDriverType.ORACLE8.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.ORACLE8_OCI8.getSubProtocol(), BuiltinDriverType.ORACLE8_OCI8.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.ORACLE8_THIN_OLD.getSubProtocol(), BuiltinDriverType.ORACLE8_THIN_OLD.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DATADIRECT_ORACLE.getSubProtocol(), BuiltinDriverType.DATADIRECT_ORACLE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_ORACLE.getSubProtocol(), BuiltinDriverType.INET_ORACLE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.POSTGRE_SQL.getSubProtocol(), BuiltinDriverType.POSTGRE_SQL.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.SYBASE.getSubProtocol(), BuiltinDriverType.SYBASE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.DATADIRECT_SYBASE.getSubProtocol(), BuiltinDriverType.DATADIRECT_SYBASE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SYBASE.getSubProtocol(), BuiltinDriverType.INET_SYBASE.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SYBASE_POOLED_1.getSubProtocol(), BuiltinDriverType.INET_SYBASE_POOLED_1.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.INET_SYBASE_POOLED_2.getSubProtocol(), BuiltinDriverType.INET_SYBASE_POOLED_2.getName());
    jdbcSubProtocolToPlatform.put(BuiltinDriverType.JTDS_SYBASE.getSubProtocol(), BuiltinDriverType.JTDS_SYBASE.getName());

    jdbcDriverToPlatform.put(BuiltinDriverType.AXION.getDriverClassName(), BuiltinDriverType.AXION.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DB2.getDriverClassName(), BuiltinDriverType.DB2.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DB2_OLD1.getDriverClassName(), BuiltinDriverType.DB2_OLD1.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DB2_OLD2.getDriverClassName(), BuiltinDriverType.DB2_OLD2.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DB2_JTOPEN.getDriverClassName(), BuiltinDriverType.DB2_JTOPEN.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DATADIRECT_DB2.getDriverClassName(), BuiltinDriverType.DATADIRECT_DB2.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.INET_DB2.getDriverClassName(), BuiltinDriverType.INET_DB2.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DERBY_EMBEDDED.getDriverClassName(), BuiltinDriverType.DERBY_EMBEDDED.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DERBY.getDriverClassName(), BuiltinDriverType.DERBY.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.FIREBIRD.getDriverClassName(), BuiltinDriverType.FIREBIRD.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.HSQL.getDriverClassName(), BuiltinDriverType.HSQL.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.INTERBASE.getDriverClassName(), BuiltinDriverType.INTERBASE.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.SAPDB.getDriverClassName(), BuiltinDriverType.SAPDB.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.MCKOI.getDriverClassName(), BuiltinDriverType.MCKOI.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.SQLSERVER.getDriverClassName(), BuiltinDriverType.SQLSERVER.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.SQLSERVER2005_NEW.getDriverClassName(), BuiltinDriverType.SQLSERVER2005_NEW.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DATADIRECT_SQLSERVER.getDriverClassName(), BuiltinDriverType.DATADIRECT_SQLSERVER.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.INET_SQLSERVER.getDriverClassName(), BuiltinDriverType.INET_SQLSERVER.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.JSQLCONNECT_SQLSERVER.getDriverClassName(), BuiltinDriverType.JSQLCONNECT_SQLSERVER.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.MYSQL.getDriverClassName(), BuiltinDriverType.MYSQL.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.MYSQL_OLD.getDriverClassName(), BuiltinDriverType.MYSQL_OLD.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.ORACLE8.getDriverClassName(), BuiltinDriverType.ORACLE8.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.ORACLE8_THIN_OLD.getDriverClassName(), BuiltinDriverType.ORACLE8_THIN_OLD.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DATADIRECT_ORACLE.getDriverClassName(), BuiltinDriverType.DATADIRECT_ORACLE.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.INET_ORACLE.getDriverClassName(), BuiltinDriverType.INET_ORACLE.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.POSTGRE_SQL.getDriverClassName(), BuiltinDriverType.POSTGRE_SQL.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.SYBASE.getDriverClassName(), BuiltinDriverType.SYBASE.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.SYBASE_OLD.getDriverClassName(), BuiltinDriverType.SYBASE_OLD.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.DATADIRECT_SYBASE.getDriverClassName(), BuiltinDriverType.DATADIRECT_SYBASE.getName());
    jdbcDriverToPlatform.put(BuiltinDriverType.INET_SYBASE.getDriverClassName(), BuiltinDriverType.INET_SYBASE.getName());
  }

  /**
   * Tries to determine the database type for the given data source. Note that this will establish
   * a connection to the database.
   *
   * @param dataSource The data source
   * @return The database type or <code>null</code> if the database type couldn't be determined
   */
  public String determineDatabaseType(DataSource dataSource) throws DatabaseOperationException {
    return determineDatabaseType(dataSource, null, null);
  }

  /**
   * Tries to determine the database type for the given data source. Note that this will establish
   * a connection to the database.
   *
   * @param dataSource The data source
   * @param username   The username to use for connecting to the database
   * @param password   The password to use for connecting to the database
   * @return The database type or <code>null</code> if the database type couldn't be determined
   */
  public String determineDatabaseType(DataSource dataSource, String username, String password) throws DatabaseOperationException {
    Connection connection = null;
    try {
      if (username != null) {
        connection = dataSource.getConnection(username, password);
      } else {
        connection = dataSource.getConnection();
      }
      DatabaseMetaData metaData = connection.getMetaData();
      return determineDatabaseType(metaData.getDriverName(), metaData.getURL());
    } catch (SQLException ex) {
      throw new DatabaseOperationException("Error while reading the database metadata: " + ex.getMessage(), ex);
    } finally {
      JdbcUtils.closeSilently(connection);
    }
  }

  /**
   * Tries to determine the database type for the given jdbc driver and connection url.
   *
   * @param driverName        The fully qualified name of the JDBC driver
   * @param jdbcConnectionUrl The connection url
   * @return The database type or <code>null</code> if the database type couldn't be determined
   */
  public String determineDatabaseType(String driverName, String jdbcConnectionUrl) {
    if (jdbcDriverToPlatform.containsKey(driverName)) {
      return jdbcDriverToPlatform.get(driverName);
    }
    if (jdbcConnectionUrl == null) {
      return null;
    }
    for (Map.Entry<String, String> stringStringEntry : jdbcSubProtocolToPlatform.entrySet()) {
      String curSubProtocol = "jdbc:" + stringStringEntry.getKey() + ":";

      if (jdbcConnectionUrl.startsWith(curSubProtocol)) {
        return stringStringEntry.getValue();
      }
    }
    return null;
  }
}
