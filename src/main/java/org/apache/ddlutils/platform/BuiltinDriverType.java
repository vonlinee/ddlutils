package org.apache.ddlutils.platform;

/**
 * builtin driver type enum.
 */
public enum BuiltinDriverType implements DriverType {

  /**
   * HSQL
   */
  HSQL("HsqlDb", "org.hsqldb.jdbcDriver", "hsqldb"),

  /**
   * Axion DB
   */
  Axion("Axion", "org.axiondb.jdbc.AxionDriver", "axiondb"),

  /**
   * A sub-protocol used by the DB2 network driver.
   */
  Cloudscape1("Cloudscape", "org.hsqldb.jdbcDriver", "db2j:net"),

  /**
   * A sub-protocol used by the DB2 network driver.
   */
  Cloudscape2("Cloudscape", "org.hsqldb.jdbcDriver", "cloudscape:net"),

  /**
   * The sub-protocol used by the standard DB2 driver.
   */
  Db2("DB2", "com.ibm.db2.jcc.DB2Driver", "db2"),
  DB2v8("DB2v8", "com.ibm.db2.jcc.DB2Driver", "db2"),

  /**
   * Older name for the jdbc driver.
   */
  Db2_OLD1("DB2", "COM.ibm.db2.jdbc.app.DB2Driver", "db2"),

  /**
   * Older name for the jdbc driver.
   */
  Db2_OLD2("DB2", "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver", "db2"),

  /**
   * An alternative sub protocol used by the standard DB2 driver on OS/390.
   */
  Db2_OS390_1("DB2", "com.ibm.db2.jcc.DB2Driver", "db2os390"),

  /**
   * An alternative sub protocol used by the standard DB2 driver on OS/390.
   */
  Db2_OS390_2("DB2", "com.ibm.db2.jcc.DB2Driver", "db2os390sqlj"),

  /**
   * The DataDirect Connect DB2 jdbc driver.
   */
  DATADIRECT_DB2("DB2", "com.ddtek.jdbc.db2.DB2Driver", "datadirect:db2"),

  /**
   * The i-net DB2 jdbc driver.
   */
  INET_DB2("DB2", "com.inet.drda.DRDADriver", "inetdb2"),

  /**
   * An alternative sub protocol used by the JTOpen driver on OS/400.
   */
  Db2_JTOPEN("DB2", "com.ibm.as400.access.AS400JDBCDriver", "as400"),

  /**
   * The standard Sybase jdbc driver.
   */
  SYBASE("Sybase", "com.sybase.jdbc2.jdbc.SybDriver", "sybase:Tds"),
  SybaseASE15("SybaseASE15", "com.sybase.jdbc2.jdbc.SybDriver", "sybase:Tds"),

  /**
   * The old Sybase jdbc driver.
   */
  SYBASE_OLD("Sybase", "com.sybase.jdbc.SybDriver", "sybase:Tds"),

  /**
   * The sub protocol used by the jTDS Sybase driver.
   */
  JTDS_SYBASE("Sybase", "net.sourceforge.jtds.jdbc.Driver", "jtds:sybase"),

  /**
   * The i-net Sybase jdbc driver.
   */
  INET_SYBASE("Sybase", "com.inet.syb.SybDriver", "inetsyb"),

  /**
   * The sub-protocol used by the pooled i-net Sybase driver.
   */
  INET_SYBASE_POOLED_1("Sybase", "com.inet.syb.SybDriver", "inetpool:inetsyb"),

  /**
   * The sub-protocol used by the pooled i-net Sybase driver.
   */
  INET_SYBASE_POOLED_2("Sybase", "com.inet.syb.SybDriver", "inetpool:jdbc:inetsyb"),

  /**
   * The sub-protocol used by the DataDirect Sybase driver.
   */
  DATADIRECT_SYBASE("Sybase", "com.ddtek.jdbc.sybase.SybaseDriver", "datadirect:sybase"),

  /**
   * The sub-protocol used by the derby drivers.
   */
  Derby("Derby", "org.apache.derby.jdbc.ClientDriver", "derby"),

  /**
   * The sub protocol used by the derby embed drivers.
   */
  Derby_EMBEDDED("Derby", "org.apache.derby.jdbc.EmbeddedDriver", "derby"),

  /**
   * The sub protocol used by the standard SapDB/MaxDB driver.
   */
  Sapdb("SapDB", "com.sap.dbtech.jdbc.DriverSapDB", "sapdb"),

  // Sql Server

  /**
   * The sub-protocol used by the jTDS SQLServer driver.
   */
  JTDS_SQLSERVER("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "jtds:sqlserver"),

  /**
   * The sub-protocol used by the JNetDirect SQLServer driver.
   */
  JSQLCONNECT_SQLSERVER("MsSql", "com.jnetdirect.jsql.JSQLDriver", "JSQLConnect"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7_POOLED_2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae7"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7_POOLED_1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae7"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7A_POOLED_2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae7a"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7A_POOLED_1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae7a"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER6_POOLED_2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae6"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER6_POOLED_1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae6"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER_POOLED_2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER_POOLED_1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae"),

  /**
   * A sub-protocol used by the i-net SQLServer driver.
   */
  INET_SQLSERVER7A("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetdae7a"),

  /**
   * A sub-protocol used by the i-net SQLServer driver.
   */
  INET_SQLSERVER7("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetdae7"),

  /**
   * A sub-protocol used by the i-net SQLServer driver.
   */
  INET_SQLSERVER6("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetdae6"),

  /**
   * The sub-protocol used by the DataDirect SQLServer driver.
   */
  DATADIRECT_SQLSERVER("MsSql", "com.ddtek.jdbc.sqlserver.SQLServerDriver", "datadirect:sqlserver"),

  /**
   * A sub-protocol used by the i-net SQLServer driver.
   */
  INET_SQLSERVER("MsSql", "com.inet.tds.TdsDriver", "inetdae"),

  /**
   * The sub-protocol used by the i-net Oracle driver.
   */
  INET_ORACLE("Oracle", "com.inet.ora.OraDriver", "inetora"),

  /**
   * The sub-protocol used by the DataDirect Oracle driver.
   * The DataDirect Connect Oracle jdbc driver.
   */
  DATADIRECT_ORACLE("Oracle", "com.ddtek.jdbc.oracle.OracleDriver", "datadirect:oracle"),

  /**
   * The jTDS jdbc driver for SQLServer and Sybase.
   */
  JTDS("", "net.sourceforge.jtds.jdbc.Driver", ""),

  /**
   * The i-net pooled jdbc driver for SQLServer and Sybase.
   */
  INET_POOLED("", "com.inet.pool.PoolDriver", ""),

  /**
   * The standard Firebird jdbc driver.
   */
  Firebird("Firebird", "org.firebirdsql.jdbc.FBDriver", "firebirdsql"),

  /**
   * The interbase jdbc driver.
   */
  Interbase("Interbase", "interbase.interclient.Driver", "interbase"),

  /**
   * The sub-protocol internally returned by the newer SQL Server 2005 driver.
   */
  SQLServer2005_NEW("MsSql", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqljdbc"),

  /**
   * The sub-protocol recommended for the newer SQL Server 2005 driver.
   */
  SQLServer2005_NEW1("MsSql", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"),

  /**
   * The standard SQLServer jdbc driver.
   */
  SQLServer("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "microsoft:sqlserver"),

  /**
   * The standard MySQL jdbc driver.
   */
  MySql("MySQL", "com.mysql.jdbc.Driver", "mysql"),

  /**
   * The standard MySQL jdbc driver above mysql 5.0.
   */
  MySql50("MySQL5", "com.mysql.jdbc.Driver", "mysql"),

  /**
   * The old MySQL jdbc driver.
   */
  MySql_OLD("MySQL", "org.gjt.mm.mysql.Driver", "mysql"),

  /**
   * The standard sub-protocol used by the standard Oracle driver.
   */
  Oracle("Oracle", "oracle.jdbc.driver.OracleDriver", "oracle:thin"),

  /**
   * The thin sub-protocol used by the standard Oracle driver.
   */
  Oracle8("Oracle8", "oracle.jdbc.driver.OracleDriver", "oracle:thin"),

  /**
   * The old thin sub-protocol used by the standard Oracle driver.
   */
  Oracle8_THIN_OLD("Oracle", "oracle.jdbc.dnlddriver.OracleDriver", "oracle:dnldthin"),

  /**
   * The thin sub-protocol used by the standard Oracle driver.
   */
  Oracle8_OCI8("Oracle", "", "oracle:oci8"),
  Oracle9("Oracle9", "", ""),
  Oracle10("Oracle10", "", ""),
  McKoi("McKoi", "com.mckoi.JDBCDriver", "mckoi"),

  /**
   * The sub-protocol used by the standard PostgreSQL driver.
   */
  PostgreSql("PostgreSql", "org.postgresql.Driver", "postgresql"),
  MaxDB("MaxDB", "", ""),

  ;

  final String name;
  final String driverClassName;
  final String subProtocol;

  BuiltinDriverType(String name, String driverClassName, String subProtocol) {
    this.name = name;
    this.driverClassName = driverClassName;
    this.subProtocol = subProtocol;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDriverClassName() {
    return driverClassName;
  }

  @Override
  public String getSubProtocol() {
    return subProtocol;
  }
}
