package org.apache.ddlutils.util;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public final class JdbcUtils {

  private JdbcUtils() {
  }

  /**
   * Creates a database connection from a DataSource.
   *
   * <p>
   * This method attempts to create a database connection from the provided DataSource.
   * If a username is provided (not null), it will use the username and password to establish the connection.
   * If username is null, it will attempt to create a connection without authentication credentials.
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   * BasicDataSource dataSource = new BasicDataSource();
   * dataSource.setUrl("jdbc:h2:~/test");
   *
   * // With username and password
   * try {
   *     Connection conn = JdbcUtils.getConnection(dataSource, "sa", "");
   *     // Use the connection
   *     conn.close();
   * } catch (SQLException e) {
   *     e.printStackTrace();
   * }
   *
   * // Without username and password (using DataSource defaults)
   * try {
   *     Connection conn = JdbcUtils.getConnection(dataSource, null, null);
   *     // Use the connection
   *     conn.close();
   * } catch (SQLException e) {
   *     e.printStackTrace();
   * }
   * </pre>
   *
   * @param dataSource The DataSource to get the connection from
   * @param username The database username (can be null)
   * @param password The database password (can be null)
   * @return A database connection
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection(DataSource dataSource, String username, String password) throws SQLException {
    Connection connection;
    if (username != null) {
      connection = dataSource.getConnection(username, password);
    } else {
      connection = dataSource.getConnection();
    }
    return connection;
  }

  /**
   * Creates a database connection using the provided properties.
   *
   * <p>
   * This method expects the properties object to contain the following keys:
   * </p>
   * <ul>
   * <li><code>url</code> - The database URL</li>
   * <li><code>username</code> - The database username (optional)</li>
   * <li><code>password</code> - The database password (optional)</li>
   * </ul>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   * Properties props = new Properties();
   * props.setProperty("url", "jdbc:h2:~/test");
   * props.setProperty("username", "sa");
   * props.setProperty("password", "");
   *
   * try {
   *     Connection conn = JdbcUtils.getConnection(props);
   *     // Use the connection
   *     conn.close();
   * } catch (SQLException e) {
   *     e.printStackTrace();
   * }
   * </pre>
   *
   * @param properties The properties containing connection information
   * @return A database connection
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection(Properties properties) throws SQLException {
    String url = properties.getProperty("url");
    String username = properties.getProperty("username");
    String password = properties.getProperty("password");
    return getConnection(url, username, password);
  }

  /**
   * Creates a database connection using the provided URL, username, and password.
   *
   * <p>
   * This method establishes a connection to a database using standard JDBC connection parameters.
   * If username and password are null, the method will attempt to connect without authentication.
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   * try {
   *     Connection conn = JdbcUtils.getConnection("jdbc:h2:~/test", "sa", "");
   *     // Use the connection
   *     conn.close();
   * } catch (SQLException e) {
   *     e.printStackTrace();
   * }
   * </pre>
   *
   * @param url The database URL (e.g., jdbc:h2:~/test)
   * @param username The database username (can be null)
   * @param password The database password (can be null)
   * @return A database connection
   * @throws SQLException if a database access error occurs
   */
  public static Connection getConnection(String url, String username, String password) throws SQLException {
    return DriverManager.getConnection(url, username, password);
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

  public static ResultSet executeQuery(Connection connection, String sql) throws SQLException {
    Statement statement = connection.createStatement();
    return statement.executeQuery(sql);
  }

  public static ResultSet executeQuery(Connection connection, String sql, Object... parameters) throws SQLException {
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    for (int i = 0; i < parameters.length; ++i) {
      preparedStatement.setObject(i + 1, parameters[i]);
    }
    return preparedStatement.executeQuery();
  }

  public static String queryForSingleStringValue(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getString(1);
      }
    }
  }

  public static int queryForSingleIntValue(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  public static long queryForSingleLongValue(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getLong(1);
      }
    }
  }
}
