package org.apache.ddlutils.platform;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.platform.mysql.MySql8Platform;
import org.junit.Assert;
import org.junit.Test;

public class TestMySql8Platform {

  @Test
  public void shouldCreateMySQL8Platform() throws Exception {
    Platform platform = PlatformFactory.createNewPlatformInstance(MySql8Platform.DATABASENAME);
    Assert.assertTrue(platform instanceof MySql8Platform);
  }
}
