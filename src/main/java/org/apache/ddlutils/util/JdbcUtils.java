package org.apache.ddlutils.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcUtils {

  public static void closeSilently(AutoCloseable closeable) {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (Exception ignore) {
    }
  }

  public static void closeSilently(Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.close();
    } catch (Exception ignore) {
    }
  }

  public static void closeSilently(Statement statement) {
    if (statement == null) {
      return;
    }
    try {
      statement.close();
    } catch (Exception ignore) {
    }
  }

  public static void closeSilently(ResultSet resultSet) {
    if (resultSet == null) {
      return;
    }
    try {
      resultSet.close();
    } catch (Exception ignore) {
    }
  }
}
