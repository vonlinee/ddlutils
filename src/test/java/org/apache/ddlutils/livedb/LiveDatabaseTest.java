package org.apache.ddlutils.livedb;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.platform.mysql.MySql8Platform;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;

public class LiveDatabaseTest {

  @Test
  public void testCurrentSchemaQuery() throws Exception {
    DataSource dataSource = TestAgainstLiveDatabaseBase.getLiveDataSource("/jdbc.mysql8.properties");
    Platform platform = PlatformFactory.createNewPlatformInstance(MySql8Platform.DATABASENAME);
    platform.setDataSource(dataSource);
    Assert.assertTrue(platform instanceof MySql8Platform);
    String currentSchema = platform.currentSchemaName();
    Assert.assertEquals("ddlutils", currentSchema);
  }
}
