package org.apache.ddlutils.livedb;

import org.apache.commons.beanutils.DynaBean;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.platform.mysql.MySql8Platform;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

public class LiveDatabaseTest {

  @Test
  public void testCurrentSchemaQuery() throws Exception {
    DataSource dataSource = TestAgainstLiveDatabaseBase.getLiveDataSource("/jdbc.mysql8.properties");
    Platform platform = PlatformFactory.createNewPlatformInstance(MySql8Platform.DATABASENAME);
    platform.setDataSource(dataSource);
    Assertions.assertInstanceOf(MySql8Platform.class, platform);
    String currentSchema = platform.currentSchemaName();
    Assertions.assertEquals("ddlutils", currentSchema);
  }

  // @Test
  public void exportDatabaseAsSql() throws Exception {
    DataSource dataSource = TestAgainstLiveDatabaseBase.getLiveDataSource("/jdbc.mysql8.properties");
    Platform platform = PlatformFactory.createNewPlatformInstance(MySql8Platform.DATABASENAME);
    platform.setDataSource(dataSource);
    Database db = platform.readModelFromDatabase("sakila");

    final SqlBuilder sqlBuilder = platform.getSqlBuilder();
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("test.sql")))) {
      platform.getSqlBuilder().setWriter(out);
      for (Table table : db.getTables()) {

        sqlBuilder.appendCommentLine("=================================================");
        sqlBuilder.appendCommentLine("Structure of Table: " + platform.getQualifiedName(table));
        sqlBuilder.appendCommentLine("==================================================");
        sqlBuilder.createTable(db, table, new HashMap<>());

        final String sql = "SELECT * FROM " + platform.getQualifiedName(table);
        Iterator<DynaBean> iterator = platform.query(db, sql);
        boolean first = true;
        while (iterator.hasNext()) {
          if (first) {
            sqlBuilder.appendCommentLine("=================================================");
            sqlBuilder.appendCommentLine("Data of table: " + platform.getQualifiedName(table));
            sqlBuilder.appendCommentLine("==================================================");
            sqlBuilder.nextLine();
            first = false;
          }
          DynaBean bean = iterator.next();
          String insertSql = platform.getInsertSql(db, bean) + ";";
          sqlBuilder.appendLine(insertSql);
        }
        sqlBuilder.flush();
      }
    }
  }
}
