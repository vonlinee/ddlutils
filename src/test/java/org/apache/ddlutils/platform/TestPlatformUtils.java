package org.apache.ddlutils.platform;

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

import junit.framework.TestCase;
import org.apache.ddlutils.PlatformUtils;

/**
 * Tests the {@link org.apache.ddlutils.PlatformUtils} class.
 *
 * @version $Revision: 279421 $
 */
public class TestPlatformUtils extends TestCase {
  /**
   * The tested platform utils object.
   */
  private PlatformUtils _platformUtils;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {
    _platformUtils = new PlatformUtils();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    _platformUtils = null;
  }

  /**
   * Tests the determination of the Axion platform via its JDBC driver.
   */
  public void testAxionDriver() {
    assertEquals(BuiltinDriverType.Axion.getName(),
      _platformUtils.determineDatabaseType("org.axiondb.jdbc.AxionDriver", null));
  }

  /**
   * Tests the determination of the Axion platform via JDBC connection urls.
   */
  public void testAxionUrl() {
    assertEquals(BuiltinDriverType.Axion.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:axiondb:testdb"));
    assertEquals(BuiltinDriverType.Axion.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:axiondb:testdb:/tmp/testdbdir"));
  }

  /**
   * Tests the determination of the Db2 platform via its JDBC drivers.
   */
  public void testDb2Driver() {
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType("com.ibm.db2.jcc.DB2Driver", null));
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType("COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver", null));
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType("COM.ibm.db2.jdbc.app.DB2Driver", null));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType("com.ddtek.jdbc.db2.DB2Driver", null));
    // i-net
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType("com.inet.drda.DRDADriver", null));
  }

  /**
   * Tests the determination of the Db2 platform via JDBC connection urls.
   */
  public void testDb2Url() {
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:db2://sysmvs1.stl.ibm.com:5021/san_jose"));
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:db2os390://sysmvs1.stl.ibm.com:5021/san_jose"));
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:db2os390sqlj://sysmvs1.stl.ibm.com:5021/san_jose"));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:datadirect:db2://server1:50000;DatabaseName=jdbc;User=test;Password=secret"));
    // i-net
    assertEquals(BuiltinDriverType.Db2.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetdb2://server1:50000"));
  }

  /**
   * Tests the determination of the Cloudscape platform via JDBC connection urls.
   */
  public void testCloudscapeUrl() {
    assertEquals(BuiltinDriverType.Cloudscape1.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:db2j:net:database"));
    assertEquals(BuiltinDriverType.Cloudscape1.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:cloudscape:net:database"));
  }

  /**
   * Tests the determination of the Derby platform via its JDBC drivers.
   */
  public void testDerbyDriver() {
    assertEquals(BuiltinDriverType.Derby.getName(),
      _platformUtils.determineDatabaseType("org.apache.derby.jdbc.ClientDriver", null));
    assertEquals(BuiltinDriverType.Derby.getName(),
      _platformUtils.determineDatabaseType("org.apache.derby.jdbc.EmbeddedDriver", null));
  }

  /**
   * Tests the determination of the Derby platform via JDBC connection urls.
   */
  public void testDerbyUrl() {
    assertEquals(BuiltinDriverType.Derby.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:derby:sample"));
  }

  /**
   * Tests the determination of the Firebird platform via its JDBC driver.
   */
  public void testFirebirdDriver() {
    assertEquals(BuiltinDriverType.Firebird.getName(),
      _platformUtils.determineDatabaseType("org.firebirdsql.jdbc.FBDriver", null));
  }

  /**
   * Tests the determination of the Firebird platform via JDBC connection urls.
   */
  public void testFirebirdUrl() {
    assertEquals(BuiltinDriverType.Firebird.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql://localhost:8080/path/to/db.fdb"));
    assertEquals(BuiltinDriverType.Firebird.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:native:localhost/8080:/path/to/db.fdb"));
    assertEquals(BuiltinDriverType.Firebird.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:local://localhost:8080:/path/to/db.fdb"));
    assertEquals(BuiltinDriverType.Firebird.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:firebirdsql:embedded:localhost/8080:/path/to/db.fdb"));
  }

  /**
   * Tests the determination of the HsqlDb platform via its JDBC driver.
   */
  public void testHsqldbDriver() {
    assertEquals(BuiltinDriverType.HSQL.getName(),
      _platformUtils.determineDatabaseType("org.hsqldb.jdbcDriver", null));
  }

  /**
   * Tests the determination of the HsqlDb platform via JDBC connection urls.
   */
  public void testHsqldbUrl() {
    assertEquals(BuiltinDriverType.HSQL.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:hsqldb:/opt/db/testdb"));
  }

  /**
   * Tests the determination of the Interbase platform via its JDBC driver.
   */
  public void testInterbaseDriver() {
    assertEquals(BuiltinDriverType.Interbase.getName(),
      _platformUtils.determineDatabaseType("interbase.interclient.Driver", null));
  }

  /**
   * Tests the determination of the Interbase platform via JDBC connection urls.
   */
  public void testInterbaseUrl() {
    assertEquals(BuiltinDriverType.Interbase.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:interbase://localhost/e:/testbed/database/employee.gdb"));
  }

  /**
   * Tests the determination of the McKoi platform via its JDBC driver.
   */
  public void testMckoiDriver() {
    assertEquals(BuiltinDriverType.McKoi.getName(),
      _platformUtils.determineDatabaseType("com.mckoi.JDBCDriver", null));
  }

  /**
   * Tests the determination of the McKoi platform via JDBC connection urls.
   */
  public void testMckoiUrl() {
    assertEquals(BuiltinDriverType.McKoi.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:mckoi:local://./db.conf"));
    assertEquals(BuiltinDriverType.McKoi.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:mckoi://db.myhost.org/"));
  }

  /**
   * Tests the determination of the Microsoft Sql Server platform via its JDBC drivers.
   */
  public void testMsSqlDriver() {
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType("com.microsoft.jdbc.sqlserver.SQLServerDriver", null));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType("com.ddtek.jdbc.sqlserver.SQLServerDriver", null));
    // JNetDirect JSQLConnect
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType("com.jnetdirect.jsql.JSQLDriver", null));
    // i-net
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType("com.inet.tds.TdsDriver", null));
  }

  /**
   * Tests the determination of the Microsoft Sql Server platform via JDBC connection urls.
   */
  public void testMsSqlUrl() {
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:microsoft:sqlserver://localhost:1433"));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:datadirect:sqlserver://server1:1433;User=test;Password=secret"));
    // JNetDirect JSQLConnect
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:JSQLConnect://localhost/database=master/user=sa/sqlVersion=6"));
    // i-net
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetdae:210.1.164.19:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetdae6:[2002:d201:a413::d201:a413]:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetdae7:localHost:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetdae7a://MyServer/pipe/sql/query"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae:210.1.164.19:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae6:[2002:d201:a413::d201:a413]:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae7:localHost:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetdae7a://MyServer/pipe/sql/query"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae:210.1.164.19:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae6:[2002:d201:a413::d201:a413]:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae7:localHost:1433"));
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetdae7a://MyServer/pipe/sql/query"));
    // jTDS
    assertEquals(BuiltinDriverType.SQLServer.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:jtds:sqlserver://localhost:8080/test"));
  }

  /**
   * Tests the determination of the MySql platform via its JDBC drivers.
   */
  public void testMySqlDriver() {
    assertEquals(BuiltinDriverType.MySql.getName(),
      _platformUtils.determineDatabaseType("com.mysql.jdbc.Driver", null));
    assertEquals(BuiltinDriverType.MySql.getName(),
      _platformUtils.determineDatabaseType("org.gjt.mm.mysql.Driver", null));
  }

  /**
   * Tests the determination of the MySql platform via JDBC connection urls.
   */
  public void testMySqlUrl() {
    assertEquals(BuiltinDriverType.MySql.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:mysql://localhost:1234/test"));
  }

  /**
   * Tests the determination of the Oracle 8 platform via its JDBC drivers.
   */
  public void testOracleDriver() {
    assertEquals(BuiltinDriverType.Oracle8.getName(),
      _platformUtils.determineDatabaseType("oracle.jdbc.driver.OracleDriver", null));
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType("oracle.jdbc.dnlddriver.OracleDriver", null));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.DATADIRECT_ORACLE.getName(),
      _platformUtils.determineDatabaseType("com.ddtek.jdbc.oracle.OracleDriver", null));
    // i-net
    assertEquals(BuiltinDriverType.INET_ORACLE.getName(),
      _platformUtils.determineDatabaseType("com.inet.ora.OraDriver", null));
  }

  /**
   * Tests the determination of the Oracle 8 platform via JDBC connection urls.
   */
  public void testOracleUrl() {
    assertEquals(BuiltinDriverType.Oracle8.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:oracle:thin:@myhost:1521:orcl"));
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:oracle:oci8:@(description=(address=(host=myhost)(protocol=tcp)(port=1521))(connect_data=(sid=orcl)))"));
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:oracle:dnldthin:@myhost:1521:orcl"));
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:oracle:dnldthin:@myhost:1521:orcl"));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:datadirect:oracle://server3:1521;ServiceName=ORCL;User=test;Password=secret"));
    // i-net
    assertEquals(BuiltinDriverType.Oracle8_THIN_OLD.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetora:www.inetsoftware.de:1521:orcl?traceLevel=2"));
  }

  /**
   * Tests the determination of the PostgreSql platform via its JDBC driver.
   */
  public void testPostgreSqlDriver() {
    assertEquals(BuiltinDriverType.PostgreSql.getName(),
      _platformUtils.determineDatabaseType("org.postgresql.Driver", null));
  }

  /**
   * Tests the determination of the PostgreSql platform via JDBC connection urls.
   */
  public void testPostgreSqlUrl() {
    assertEquals(BuiltinDriverType.PostgreSql.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:postgresql://localhost:1234/test"));
    assertEquals(BuiltinDriverType.PostgreSql.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:postgresql://[::1]:5740/accounting"));
  }

  /**
   * Tests the determination of the SapDb platform via its JDBC driver.
   */
  public void testSapDbDriver() {
    assertEquals(BuiltinDriverType.Sapdb.getName(),
      _platformUtils.determineDatabaseType("com.sap.dbtech.jdbc.DriverSapDB", null));
  }

  /**
   * Tests the determination of the SapDb platform via JDBC connection urls.
   */
  public void testSapDbUrl() {
    assertEquals(BuiltinDriverType.Sapdb.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:sapdb://servermachine:9876/TST"));
  }

  /**
   * Tests the determination of the Sybase platform via its JDBC drivers.
   */
  public void testSybaseDriver() {
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType("com.sybase.jdbc.SybDriver", null));
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType("com.sybase.jdbc2.jdbc.SybDriver", null));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType("com.ddtek.jdbc.sybase.SybaseDriver", null));
    // i-net
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType("com.inet.syb.SybDriver", null));
  }

  /**
   * Tests the determination of the Sybase platform via JDBC connection urls.
   */
  public void testSybaseUrl() {
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:sybase:Tds:xyz:3767orjdbc:sybase:Tds:130.214.90.27:3767"));
    // DataDirect Connect
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:datadirect:sybase://server2:5000;User=test;Password=secret"));
    // i-net
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetsyb:www.inetsoftware.de:3333"));
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:inetsyb:www.inetsoftware.de:3333"));
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:inetpool:jdbc:inetsyb:www.inetsoftware.de:3333"));
    // jTDS
    assertEquals(BuiltinDriverType.SYBASE.getName(),
      _platformUtils.determineDatabaseType(null, "jdbc:jtds:sybase://localhost:8080/test"));
  }
}
