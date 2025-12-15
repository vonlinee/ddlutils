package org.apache.ddlutils.platform;

enum DbTypeFamily implements DbType.Family {

  // MySQL
  MySQL("MySQL", "mysql", 3306),
  MySQL5("MySQL5", MySQL.subProtocol, MySQL.defaultPort),
  MySQL8("MySQL8", MySQL.subProtocol, MySQL.defaultPort),

  // PostgreSQL
  PostgreSQL("PostgreSQL", "postgresql", 5432),
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
