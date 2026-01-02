package org.apache.ddlutils.platform;

public enum DbTypeFamily implements DbType.Family {

  // MySQL
  MySQL("MySQL", "mysql", 3306),

  // PostgreSQL
  PostgreSQL("PostgreSQL", "postgresql", 5432),

  HsqlDb("HsqlDb", "hsqldb", 9001),
  ;
  private final String name;

  /**
   * the sub protocol used by the database
   */
  private final String subProtocol;

  /**
   * the default port used by the database
   */
  private final int defaultPort;

  DbTypeFamily(String name, String subProtocol, int defaultPort) {
    this.name = name;
    this.subProtocol = subProtocol;
    this.defaultPort = defaultPort;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getDefaultPort() {
    return defaultPort;
  }

  @Override
  public String getSubProtocol() {
    return subProtocol;
  }
}
