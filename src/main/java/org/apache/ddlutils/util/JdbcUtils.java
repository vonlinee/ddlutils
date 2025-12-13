package org.apache.ddlutils.util;

import org.apache.ddlutils.platform.MetaDataColumnDescriptor;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JdbcUtils {

  private JdbcUtils() {
  }

  public static Map<String, Object> readColumnValues(ResultSet resultSet, List<MetaDataColumnDescriptor> columnDescriptors) throws SQLException {
    HashMap<String, Object> values = new HashMap<>();
    for (MetaDataColumnDescriptor descriptor : columnDescriptors) {
      values.put(descriptor.getName(), descriptor.readColumn(resultSet));
    }
    return values;
  }

  /**
   * Determines whether a value for the specified column is present in the given result set.
   *
   * @param resultSet The result set
   * @return <code>true</code> if the column is present in the result set
   */
  public static boolean isColumnInResultSet(ResultSet resultSet, String columnName) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    for (int idx = 1; idx <= metaData.getColumnCount(); idx++) {
      if (columnName.equals(metaData.getColumnName(idx).toUpperCase())) {
        return true;
      }
    }
    return false;
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
