package org.apache.ddlutils.parser;

import org.junit.Test;

public class SqlParserTest {

  SqlParser sqlParser = new GithubJSqlParser();

  @Test
  public void testParseSelectSql() throws Exception {
    String sql = "SELECT t.id, t.name, SUM(o.amount) " +
                 "FROM t_user t JOIN t_order o ON t.id = o.user_id " +
                 "WHERE t.status = 1 AND o.create_time > '2025-01-01' " +
                 "GROUP BY t.id, t.name " +
                 "HAVING SUM(o.amount) > 1000";

    SelectSqlParseResult result = sqlParser.parseSelectSql(sql);
  }

  @Test
  public void testParseInsertSql() throws Exception {
    String sql = "INSERT INTO staff (staff_id, first_name, last_name, address_id, picture, email, store_id, " +
                 "active, username, password, last_update) VALUES ('2', 'Jon', 'Stephens', '4', " +
                 "NULL, 'Jon.Stephens@sakilastaff.com', '2', 'true', 'Jon', NULL, '2006-02-15 03:57:16.0')";

    InsertIntoTableParseResult result = sqlParser.parseInsertSql(sql);

    System.out.println(result);

    String sql1 = "INSERT INTO staff VALUES ('2', 'Jon', 'Stephens', '4', " +
                 "NULL, 'Jon.Stephens@sakilastaff.com', '2', 'true', 'Jon', NULL, '2006-02-15 03:57:16.0')";
    InsertIntoTableParseResult result2 = sqlParser.parseInsertSql(sql1);
    System.out.println(result2);
  }

  @Test
  public void testParseInsertSql1() throws Exception {
    String sql = "INSERT INTO staff VALUES ('2', 'Jon', 'Stephens', '4', " +
                  "NULL, 'Jon.Stephens@sakilastaff.com', '2', 'true', 'Jon', NULL, '2006-02-15 03:57:16.0')";

    InsertIntoTableParseResult result = sqlParser.parseInsertSql(sql);
    System.out.println(result);
  }

  @Test
  public void testCreateIndexSql() throws Exception {
    String sql = "CREATE INDEX idx_actor_last_name ON actor (last_name)";

    CreateIndexSqlParseResult result = sqlParser.parseCreateIndexSql(sql);
    System.out.println(result);
  }
}
