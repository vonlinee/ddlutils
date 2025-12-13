package org.apache.ddlutils.util;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JdbcUtils {

  private JdbcUtils() {
  }

  public static List<Map<String, Object>> toMapList(ResultSet rs) throws SQLException {
    List<Map<String, Object>> rows = new ArrayList<>();
    while (rs.next()) {
      rows.add(readColumnValues(rs));
    }
    return rows;
  }


  /**
   * Convert a <code>ResultSet</code> row into a <code>Map</code>.
   *
   * <p>
   * This implementation returns a <code>Map</code> with case-insensitive column names as keys. Calls to
   * <code>map.get("COL")</code> and <code>map.get("col")</code> return the same value. Furthermore, this implementation
   * will return an ordered map, that preserves the ordering of the columns in the ResultSet, so that iterating over
   * the entry set of the returned map will return the first column of the ResultSet, then the second and so forth.
   * </p>
   *
   * @param rs ResultSet that supplies the map data
   * @return the newly created Map
   * @throws SQLException if a database access error occurs
   */
  public static Map<String, Object> readColumnValues(ResultSet rs) throws SQLException {
    Map<String, Object> result = new CaseInsensitiveMap<>();
    ResultSetMetaData rsmd = rs.getMetaData();
    final int cols = rsmd.getColumnCount();
    for (int i = 1; i <= cols; i++) {
      String columnName = rsmd.getColumnLabel(i);
      if (null == columnName || columnName.isEmpty()) {
        columnName = rsmd.getColumnName(i);
      }
      result.put(columnName, rs.getObject(i));
    }
    return result;
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
