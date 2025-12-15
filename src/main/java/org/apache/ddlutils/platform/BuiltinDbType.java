package org.apache.ddlutils.platform;

import java.util.Properties;

public enum BuiltinDbType implements DbType {

  // MySQL
  MySQL5("MySQL5", "com.mysql.jdbc.Driver", DbTypeFamily.MySQL, "jdbc:mysql://%s:%d/%s"),
  MySQL8("MySQL8", "com.mysql.cj.jdbc.Driver", DbTypeFamily.MySQL, "jdbc:mysql://%s:%d/%s"),

  // PostgreSQL
  PostgreSQL("PostgreSql", "org.postgresql.Driver", DbTypeFamily.PostgreSQL, "jdbc:postgresql://%s:%d/%s"),
  ;

  private final String name;
  private final String driverClassName;
  private final String subProtocol;
  private final String connectionUrlPattern;
  private final Family family;

  BuiltinDbType(String name, String driverClassName, Family family, String connectionUrlPattern) {
    this.name = name;
    this.driverClassName = driverClassName;
    this.subProtocol = family.getSubProtocol();
    this.family = family;
    this.connectionUrlPattern = connectionUrlPattern;
  }

  BuiltinDbType(String name, String driverClassName, String subProtocol, String connectionUrlPattern, Family family) {
    this.name = name;
    this.driverClassName = driverClassName;
    this.subProtocol = subProtocol;
    this.connectionUrlPattern = connectionUrlPattern;
    this.family = family;
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

  @Override
  public String getConnectionUrlPattern() {
    return connectionUrlPattern;
  }

  @Override
  public String getConnectionUrl(String url, String user, String password, Properties properties) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean isKindOf(Family family) {
    return false;
  }

  @Override
  public Family getFamily() {
    return family;
  }
}
