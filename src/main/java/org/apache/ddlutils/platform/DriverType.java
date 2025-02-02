package org.apache.ddlutils.platform;

import java.util.Properties;

/**
 * Interface representing a type of database driver.
 */
public interface DriverType {

  /**
   * Gets the name of the driver type.
   *
   * @return the name of the driver (e.g., "MySQL", "PostgreSQL").
   */
  String getName();

  /**
   * Gets the fully qualified class name of the driver.
   *
   * @return the driver class name (e.g., "com.mysql.cj.jdbc.Driver").
   */
  String getDriverClassName();

  /**
   * Gets the subprotocol used by the driver.
   *
   * @return the subprotocol (e.g., "mysql").
   */
  String getSubProtocol();

  /**
   * Gets the default port for the database.
   *
   * @return the default port number (e.g., 3306 for MySQL).
   */
  int getDefaultPort();

  /**
   * Constructs a connection URL using the provided parameters.
   *
   * @param host       the host of the database server.
   * @param port       the port of the database server; if null, the default port will be used.
   * @param database   the name of the database to connect to.
   * @param username   the username for the database connection.
   * @param password   the password for the database connection.
   * @param properties additional properties for the connection.
   * @return a connection URL string.
   */
  String getConnectionUrl(String host, Integer port, String database, String username, String password, Properties properties);
}
