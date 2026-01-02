package org.apache.ddlutils.platform;

import java.util.Properties;

/**
 * distinguish between different database types by driver class name
 */
public interface DbType {

  String getName();

  String getDriverClassName();

  default String getSubProtocol() {
    return getFamily().getSubProtocol();
  }

  String getConnectionUrlPattern();

  String getConnectionUrl(String url, String user, String password, Properties properties);

  boolean isKindOf(Family family);

  Family getFamily();

  /**
   * Represents a database family.
   *
   * @see DbType
   */
  interface Family {

    /**
     * @return family name
     * @see DbType#getName()
     */
    String getName();

    /**
     * @return default tcp port to listen if it is server mode, 0 ~ 65536
     */
    int getDefaultPort();

    /**
     * @return sub protocol
     */
    String getSubProtocol();
  }

  /**
   * unknown database family
   */
  Family UNKNOWN = new Family() {
    @Override
    public String getName() {
      return "Unknown";
    }

    @Override
    public int getDefaultPort() {
      return -1;
    }

    @Override
    public String getSubProtocol() {
      return "Unknown";
    }
  };
}
