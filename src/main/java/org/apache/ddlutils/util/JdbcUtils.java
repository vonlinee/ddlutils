package org.apache.ddlutils.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class JdbcUtils {

  private JdbcUtils() {
  }

  public static void closeSilently(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public static void closeSilently(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }

  public static void closeSilently(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (Exception e) {
        // ignore
      }
    }
  }
}
