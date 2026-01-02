package org.apache.ddlutils.platform;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.platform.mysql.MySql8Platform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestMySql8Platform {

  @Test
  public void shouldCreateMySQL8Platform() throws Exception {
    Platform platform = PlatformFactory.createNewPlatformInstance(BuiltinDbType.MySQL8);
    Assertions.assertInstanceOf(MySql8Platform.class, platform);
  }
}
