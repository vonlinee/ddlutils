package org.apache.ddlutils.platform.mysql;

import org.apache.ddlutils.PlatformInfo;

public class MySql8Platform extends MySql50Platform {
  /**
   * Database name of this platform.
   */
  public static final String DATABASENAME = "MySQL8";

  public MySql8Platform() {
    super();

    PlatformInfo info = getPlatformInfo();
    info.setSyntheticDefaultValueForRequiredReturned(false);
    setSqlBuilder(new MySql8Builder(this));
    setModelReader(new MySql8ModelReader(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return DATABASENAME;
  }
}
