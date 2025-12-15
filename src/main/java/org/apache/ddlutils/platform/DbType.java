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
   */
  interface Family {

    String getName();

    int getDefaultPort();

    String getSubProtocol();
  }
}
