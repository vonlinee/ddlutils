package org.apache.ddlutils.platform;

import java.util.Properties;

public interface DriverType {

  String getName();

  String getDriverClassName();

  String getSubProtocol();

  int getDefaultPort();

  String getConnectionUrl(String host, Integer port, String database, String username, String password, Properties properties);
}
