package org.apache.ddlutils.platform;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

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
  AXION("Axion", "org.axiondb.jdbc.AxionDriver", "axiondb"),

  /**
   * A sub-protocol used by the DB2 network driver.
   */
  CLOUDSCAPE1("Cloudscape", "org.hsqldb.jdbcDriver", "db2j:net"),

  /**
   * A sub-protocol used by the DB2 network driver.
   */
  CLOUDSCAPE2("Cloudscape", "org.hsqldb.jdbcDriver", "cloudscape:net"),

  /**
   * The sub-protocol used by the standard DB2 driver.
   */
  DB2("DB2", "com.ibm.db2.jcc.DB2Driver", "db2"),

  /**
   * DB2v8
   */
  DB2V8("DB2v8", "com.ibm.db2.jcc.DB2Driver", "db2"),

  /**
   * Older name for the jdbc driver.
   */
  DB2_OLD1("DB2", "COM.ibm.db2.jdbc.app.DB2Driver", "db2"),

  /**
   * Older name for the jdbc driver.
   */
  DB2_OLD2("DB2", "COM.ibm.db2os390.sqlj.jdbc.DB2SQLJDriver", "db2"),

  /**
   * An alternative sub protocol used by the standard DB2 driver on OS/390.
   */
  DB2_OS390_1("DB2", "com.ibm.db2.jcc.DB2Driver", "db2os390"),

  /**
   * An alternative sub protocol used by the standard DB2 driver on OS/390.
   */
  DB2_OS390_2("DB2", "com.ibm.db2.jcc.DB2Driver", "db2os390sqlj"),

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
  DB2_JTOPEN("DB2", "com.ibm.as400.access.AS400JDBCDriver", "as400"),

  /**
   * The standard Sybase jdbc driver.
   */
  SYBASE("Sybase", "com.sybase.jdbc2.jdbc.SybDriver", "sybase:Tds"),

  /**
   * SybaseASE15
   */
  SYBASE_ASE15("SybaseASE15", "com.sybase.jdbc2.jdbc.SybDriver", "sybase:Tds"),

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
  DERBY("Derby", "org.apache.derby.jdbc.ClientDriver", "derby"),

  /**
   * The sub protocol used by the derby embed drivers.
   */
  DERBY_EMBEDDED("Derby", "org.apache.derby.jdbc.EmbeddedDriver", "derby"),

  /**
   * The sub protocol used by the standard SapDB/MaxDB driver.
   */
  SAPDB("SapDB", "com.sap.dbtech.jdbc.DriverSapDB", "sapdb"),

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
  INET_SQLSERVER7_POOLED2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae7"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7_POOLED1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae7"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7A_POOLED2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae7a"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER7A_POOLED1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae7a"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER6_POOLED2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae6"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER6_POOLED1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae6"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER_POOLED2("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:jdbc:inetdae"),

  /**
   * A sub-protocol used by the pooled i-net SQLServer driver.
   */
  INET_SQLSERVER_POOLED1("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "inetpool:inetdae"),

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
  FIREBIRD("Firebird", "org.firebirdsql.jdbc.FBDriver", "firebirdsql"),

  /**
   * The interbase jdbc driver.
   */
  INTERBASE("Interbase", "interbase.interclient.Driver", "interbase"),

  /**
   * The sub-protocol internally returned by the newer SQL Server 2005 driver.
   */
  SQLSERVER2005_NEW("MsSql", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqljdbc"),

  /**
   * The sub-protocol recommended for the newer SQL Server 2005 driver.
   */
  SQLSERVER2005_NEW1("MsSql", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"),

  /**
   * The standard SQLServer jdbc driver.
   */
  SQLSERVER("MsSql", "com.microsoft.jdbc.sqlserver.SQLServerDriver", "microsoft:sqlserver"),

  /**
   * The standard MariaDB jdbc driver.
   */
  MARIADB("MariaDB", "org.mariadb.jdbc.Driver", "mariadb"),

  /**
   * The standard MySQL jdbc driver.
   */
  MYSQL("MySQL", "com.mysql.jdbc.Driver", "mysql", 3306) {
    @Override
    public String getConnectionUrl(String host, Integer port, String database, String username, String password, @Nullable Properties properties) {
      String url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
      if (properties != null) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
          sb.append(entry.getKey()).append("=").append(entry.getValue());
          sb.append("&");
        }
        if (sb.length() > 0) {
          url = url + "?" + sb;
        }
      }
      return url;
    }
  },

  /**
   * The standard MySQL jdbc driver above mysql 5.0.
   */
  MYSQL5X("MySQL5", "com.mysql.jdbc.Driver", "mysql", MYSQL.defaultPort) {
    @Override
    public String getConnectionUrl(String host, Integer port, String database, String username, String password, @Nullable Properties properties) {
      return MYSQL.getConnectionUrl(host, port, database, username, password, properties);
    }
  },

  /**
   * The standard MySQL jdbc driver above mysql 8.x.
   */
  MYSQL8X("MySQL8", "com.mysql.cj.jdbc.Driver", "mysql", MYSQL.defaultPort) {
    @Override
    public String getConnectionUrl(String host, Integer port, String database, String username, String password, @Nullable Properties properties) {
      return MYSQL.getConnectionUrl(host, port, database, username, password, properties);
    }
  },

  /**
   * The old MySQL jdbc driver.
   */
  MYSQL_OLD("MySQL", "org.gjt.mm.mysql.Driver", "mysql", MYSQL.defaultPort),

  /**
   * The standard sub-protocol used by the standard Oracle driver.
   */
  ORACLE("Oracle", "oracle.jdbc.driver.OracleDriver", "oracle:thin"),

  /**
   * The thin sub-protocol used by the standard Oracle driver.
   */
  ORACLE8("Oracle8", "oracle.jdbc.driver.OracleDriver", "oracle:thin"),

  /**
   * The old thin sub-protocol used by the standard Oracle driver.
   */
  ORACLE8_THIN_OLD("Oracle", "oracle.jdbc.dnlddriver.OracleDriver", "oracle:dnldthin"),

  /**
   * The thin sub-protocol used by the standard Oracle driver.
   */
  ORACLE8_OCI8("Oracle", ORACLE8_THIN_OLD.driverClassName, "oracle:oci8"),

  /**
   * Oracle 9
   */
  ORACLE9("Oracle9", ORACLE8_THIN_OLD.driverClassName, "oracle:thin"),

  /**
   * Oracle 10
   */
  ORACLE10("Oracle10", ORACLE8_THIN_OLD.driverClassName, "oracle:thin"),

  /**
   * McKoi DB
   */
  MCKOI("McKoi", "com.mckoi.JDBCDriver", "mckoi"),

  /**
   * The sub-protocol used by the standard PostgreSQL driver.
   */
  POSTGRE_SQL("PostgreSql", "org.postgresql.Driver", "postgresql"),

  /**
   * Max/Sap DB
   */
  MAX_DB("MaxDB", "", ""),

  /**
   * 人大金仓/电科金仓
   * <a href="https://www.kingbase.com.cn/">...</a>
   */
  KINGBASE_ES("KingBase ES", "com.kingbase8.jdbc.Driver", "kingbase8"),

  /**
   * 南大Gbase8s
   * <a href="https://www.gbase.cn/product/gbase-8s">...</a>
   */
  GBASE8S("南大Gbase 8s", "com.gbasedbt.jdbc.Driver", "gbasedbt-sqli", 9088) {
    @Override
    public String getConnectionUrl(String host, @Nullable Integer port, String database, String username, String
      password, Properties properties) {
      // 数据库实例的名称
      String instance = properties.getProperty("GBASEDBTSERVER", "");
      return String.format("jdbc:gbasedbt-sqli://{%s}:%s/%s:GBASEDBTSERVER=%s", host, port, database, instance);
    }
  },

  H2("H2", "org.h2.Driver", "h2"),

  SQLITE("SQLite", "org.sqlite.JDBC", "sqlite"),

  DM("达梦", "dm.jdbc.driver.DmDriver", "dm"),

  GAUSSDB("GaussDB", "com.gaussdb.jdbc.Driver", "gaussdb"),

  CLICK_HOUSE("ClickHouse", "com.clickhouse.jdbc.ClickHouseDriver", "clickhouse"),

  ;

  final String name;
  final String driverClassName;
  final String subProtocol;
  final int defaultPort;

  BuiltinDriverType(String name, String driverClassName, String subProtocol) {
    this(name, driverClassName, subProtocol, -1);
  }

  BuiltinDriverType(String name, String driverClassName, String subProtocol, int defaultPort) {
    this.name = name;
    this.driverClassName = driverClassName;
    this.subProtocol = subProtocol;
    this.defaultPort = defaultPort;
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
  public int getDefaultPort() {
    return defaultPort;
  }

  @Override
  public String getConnectionUrl(String host, Integer port, String database, String username, String password, @Nullable Properties properties) {
    return "jdbc://" + getSubProtocol() + "//" + host + ":" + port;
  }

  public static BuiltinDriverType findByDriverClassName(String driverClassName) {
    for (BuiltinDriverType type : values()) {
      if (type.driverClassName.equals(driverClassName)) {
        return type;
      }
    }
    return null;
  }

  public static Optional<BuiltinDriverType> findByName(String name) {
    for (BuiltinDriverType type : values()) {
      if (type.name().equalsIgnoreCase(name)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }
}
