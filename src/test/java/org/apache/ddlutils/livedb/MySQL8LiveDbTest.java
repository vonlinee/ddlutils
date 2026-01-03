package org.apache.ddlutils.livedb;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.BuiltinDbType;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.SqlBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.StringWriter;

@Disabled
public class MySQL8LiveDbTest {

  @Test
  public void mysqlDatabaseToDDL() throws Exception {
    DataSource dataSource = TestAgainstLiveDatabaseBase.getLiveDataSource("/jdbc.mysql8.properties");
    Platform platform = PlatformFactory.createNewPlatformInstance(BuiltinDbType.MySQL8);
    platform.setDataSource(dataSource);

    platform.setDelimitedIdentifierModeOn(true);
    Database database = platform.readModelFromDatabase("sakila");
    SqlBuilder sqlBuilder = platform.getSqlBuilder();
    platform.setScriptModeOn(true);
    sqlBuilder.setIndent("  ");
    StringWriter script = new StringWriter();
    sqlBuilder.setWriter(script);
    sqlBuilder.createTables(database, new CreationParameters(), true);
    System.out.println(script);
  }
}
