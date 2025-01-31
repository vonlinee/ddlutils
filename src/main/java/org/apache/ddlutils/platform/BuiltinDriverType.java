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
   * A subprotocol used by the DB2 network driver.
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

  /**
   * The old Sybase jdbc driver.
   */
  SYBASE_OLD("Sybase", "com.sybase.jdbc.SybDriver", "sybase:Tds"),

  /**
   * The sub protocol used by the jTDS Sybase driver.
   */
  JTDS_SYBASE("Sybase", "org.hsqldb.jdbcDriver", "jtds:sybase"),

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
