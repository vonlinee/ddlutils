package org.apache.ddlutils.livedb;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.platform.BuiltinDbType;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.JdbcUtils;
import org.apache.ddlutils.util.ScriptRunner;
import org.apache.ddlutils.util.ScriptSqlReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

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

  /**
   * this should only be executed manually at local in IDE.
   *
   * @throws Exception if any error occurs
   */
  @Test
  @Disabled
  public void executeSqlScript() throws Exception {
    try (Connection connection = JdbcUtils.getConnection("jdbc:mysql://localhost:3306/ddlutils",
      "root", "123456")) {
      File schemaFile = new File(new File("").getAbsoluteFile(), "scripts/mysql/sakila-db/sakila-schema.sql");
      ScriptRunner.runScript(connection, schemaFile);

      File dataFile = new File(new File("").getAbsoluteFile(), "scripts/mysql/sakila-db/sakila-data.sql");
      ScriptRunner.runScript(connection, dataFile);
    }
  }

  /**
   * this should only be executed manually at local in IDE. just for test personally.
   *
   * @throws Exception if any error occurs
   */
  @Test
  @Disabled
  public void handleSqlScript() throws Exception {
    File schemaFile = new File(new File("").getAbsoluteFile(), "scripts/mysql/sakila-db/sakila-schema.sql");
    List<String> statements = new ArrayList<>();
    new ScriptSqlReader() {
      @Override
      protected void handleStatement(String command, LineNumberReader reader) {
        statements.add(command);
      }
    }.read(new FileReader(schemaFile));

    for (String statement : statements) {
      System.out.println();
      System.out.println(statement);
    }
  }
}
