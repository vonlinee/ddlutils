package org.apache.ddlutils.platform.hsqldb;

import org.apache.ddlutils.util.JdbcUtils;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;

/**
 * hsqldb 2.4.1 behaviour test
 * <a href="https://www.hsqldb.org/doc/">hsqldb</a>
 */
public class HsqlDBPlatformTest {

  @Test
  public void shouldFailWhenLengthNotSpecifiedExplicitly() throws Exception {
    try (Connection connection = JdbcUtils.getConnection("jdbc:hsqldb:mem:test", "sa", "")) {
      try (Statement statement = connection.createStatement()) {
        try {
          statement.execute("SELECT SUBSTR(CAST(\"1234\" AS VARCHAR),1,16)");
        } catch (Throwable throwable) {
          Assert.assertTrue(throwable.getMessage().contains("length must be specified in type definition: VARCHAR"));
        }
      }
    }
  }
}
