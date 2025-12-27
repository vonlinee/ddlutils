package org.apache.ddlutils.platform.hsqldb;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ddlutils.util.JdbcUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
          Assertions.assertTrue(throwable.getMessage().contains("length must be specified in type definition: VARCHAR"));
        }
      }
    }
  }

  @Test
  public void shouldCheckExplicitNotNullDeclaration() throws Exception {
    try (Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa", "")) {
      try (Statement statement = connection.createStatement()) {
        try {
          statement.execute("CREATE TABLE ROUNDTRIP\n" +
                            "(\n" +
                            "    AVALUE INTEGER,\n" +
                            "    pk1 INTEGER NOT NULL,\n" +
                            "    pk2 VARCHAR(32) NOT NULL,\n" +
                            "    pk3 DOUBLE DEFAULT 2,\n" +
                            "    PRIMARY KEY (pk1, pk2, pk3)\n" +
                            ");");
          ResultSet rs = connection.getMetaData().getColumns(null, null,
            "ROUNDTRIP", null);

          CaseInsensitiveMap<String, String> values = new CaseInsensitiveMap<>();
          while (rs.next()) {
            String isNullable = rs.getString("IS_NULLABLE");
            String columnName = rs.getString("COLUMN_NAME");
            values.put(columnName, isNullable);
            System.out.println(columnName + " " + isNullable);
          }
          Assertions.assertEquals("YES", values.get("AVALUE"));
          Assertions.assertEquals("NO", values.get("PK1"));
          Assertions.assertEquals("NO", values.get("PK2"));

          // in hsqldb 1.8.0.4, it's NO, in hsqldb 2.4.1, it's YES
          Assertions.assertEquals("YES", values.get("PK3"));
        } catch (Throwable throwable) {
          Assertions.assertTrue(throwable.getMessage().contains("length must be specified in type definition: VARCHAR"));
        }
      }
    }
  }

}
