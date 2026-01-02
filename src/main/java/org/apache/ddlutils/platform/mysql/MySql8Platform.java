package org.apache.ddlutils.platform.mysql;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.BuiltinDbType;

public class MySql8Platform extends MySql50Platform {

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
    return BuiltinDbType.MySQL8.getName();
  }
}
