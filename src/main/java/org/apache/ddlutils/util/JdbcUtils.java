package org.apache.ddlutils.util;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.ddlutils.model.TypeMap;
import org.apache.ddlutils.platform.MetaDataColumnDescriptor;
import org.apache.ddlutils.sql.SqlUtils;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
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
   * @param username   The database username (can be null)
   * @param password   The database password (can be null)
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
   * @param url      The database URL (e.g., jdbc:h2:~/test)
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

  public static Map<String, Object> toMap(ResultSet rs) throws SQLException {
    return readColumnValues(rs);
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

  public static Map<Integer, Object> readColumnValuesByIndex(ResultSet rs) throws SQLException {
    Map<Integer, Object> result = new CaseInsensitiveMap<>();
    ResultSetMetaData rsmd = rs.getMetaData();
    final int cols = rsmd.getColumnCount();
    for (int i = 1; i <= cols; i++) {
      result.put(i, rs.getObject(i));
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

  public static String queryForString(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getString(1);
      }
    }
  }

  public static int queryForInt(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  public static long queryForLong(Connection connection, String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      try (ResultSet rs = statement.executeQuery(sql)) {
        rs.next();
        return rs.getLong(1);
      }
    }
  }

  public static Map<String, Object> queryForMap(Connection connection, String sql) throws SQLException {
    try (ResultSet res = executeQuery(connection, sql)) {
      return toMap(res);
    }
  }

  public static List<Map<String, Object>> queryForMapList(Connection connection, String sql) throws SQLException {
    try (ResultSet res = executeQuery(connection, sql)) {
      return toMapList(res);
    }
  }

  public static Object getResultSetColumnValue(ResultSet resultSet, int jdbcType, String columnName, Object defaultValue) throws SQLException {
    Object result;
    try {
      switch (jdbcType) {
        case Types.BIT:
          result = resultSet.getBoolean(columnName);
          break;
        case Types.INTEGER:
          result = resultSet.getInt(columnName);
          break;
        case Types.TINYINT:
          result = resultSet.getShort(columnName);
          break;
        default:
          result = resultSet.getString(columnName);
          break;
      }
      if (resultSet.wasNull()) {
        result = null;
      }
    } catch (SQLException ex) {
      if (JdbcUtils.isColumnInResultSet(resultSet, columnName)) {
        throw ex;
      } else {
        result = defaultValue;
      }
    }
    return result;
  }

  /**
   * @param jdbcType jdbc type code
   * @return jdbc type
   * @see JDBCType
   */
  public static JDBCType getJdbcType(int jdbcType) {
    for (JDBCType type : JDBCType.values()) {
      if (type.getVendorTypeNumber() == jdbcType) {
        return type;
      }
    }
    return null;
  }

  public static String[] getCatalogs(DatabaseMetaData metaData) throws SQLException {
    List<String> list = new ArrayList<>();
    try (ResultSet rs = metaData.getCatalogs()) {
      while (rs.next()) {
        list.add(rs.getString("TABLE_CAT"));
      }
    }
    return list.toArray(new String[0]);
  }

  public static Map<String, List<String>> getSchemas(DatabaseMetaData metaData) throws SQLException {
    Map<String, List<String>> schemas = new HashMap<>();
    try (ResultSet rs = metaData.getSchemas()) {
      while (rs.next()) {
        List<String> schemasOfCatalog = schemas.computeIfAbsent(rs.getString("TABLE_CATALOG"),
          (catalog) -> new ArrayList<>());
        schemasOfCatalog.add(rs.getString("TABLE_SCHEM"));
      }
    }
    return schemas;
  }

  public static boolean isTextType(int typeCode) {
    return TypeMap.isTextType(typeCode);
  }

  public static String getInSqlFragment(Collection<?> items) {
    return SqlUtils.getInSqlFragment(items);
  }

  /**
   * Parses a string literal value according to the specified JDBC type.
   *
   * <p>
   * This method converts a string representation of a value to its appropriate Java object type
   * based on the provided JDBC type code. For example, it will convert a string to an Integer
   * when jdbcType is Types.INTEGER, or to a Date when jdbcType is Types.DATE.
   * </p>
   *
   * <p>
   * For null or empty string values, the method returns the literal value unchanged.
   * </p>
   *
   * <p>
   * Supported JDBC types include numeric types (TINYINT, SMALLINT, INTEGER, BIGINT, DECIMAL, NUMERIC,
   * REAL, DOUBLE, FLOAT), date/time types (DATE, TIME, TIMESTAMP), and boolean types (BIT, BOOLEAN).
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   * // Parse integer value
   * Object intValue = JdbcUtils.parseValue("123", Types.INTEGER);
   * System.out.println(intValue.getClass()); // class java.lang.Integer
   *
   * // Parse date value
   * Object dateValue = JdbcUtils.parseValue("2023-12-20", Types.DATE);
   * System.out.println(dateValue.getClass()); // class java.sql.Date
   *
   * // Parse boolean value
   * Object boolValue = JdbcUtils.parseValue("true", Types.BOOLEAN);
   * System.out.println(boolValue); // true
   *
   * // Parse null/empty value
   * Object nullValue = JdbcUtils.parseValue("", Types.VARCHAR);
   * System.out.println(nullValue); // ""
   * </pre>
   *
   * @param literalValue The string representation of the value to parse (can be null or empty)
   * @param jdbcType     The JDBC type code that determines how to parse the value
   * @return The parsed value as an appropriate Java object type, or the original literalValue if it is null or empty
   * @see Types
   * @see ConvertUtils
   */
  public static Object parseValue(String literalValue, int jdbcType) {
    if (literalValue == null || literalValue.isEmpty()) {
      return literalValue;
    }
    switch (jdbcType) {
      case Types.TINYINT:
      case Types.SMALLINT:
        return Short.valueOf(literalValue);
      case Types.INTEGER:
        return Integer.valueOf(literalValue);
      case Types.BIGINT:
        return Long.valueOf(literalValue);
      case Types.DECIMAL:
      case Types.NUMERIC:
        return new BigDecimal(literalValue);
      case Types.REAL:
        return Float.valueOf(literalValue);
      case Types.DOUBLE:
      case Types.FLOAT:
        return Double.valueOf(literalValue);
      case Types.DATE:
        return Date.valueOf(literalValue);
      case Types.TIME:
        return Time.valueOf(literalValue);
      case Types.TIMESTAMP:
        return Timestamp.valueOf(literalValue);
      case Types.BIT:
      case Types.BOOLEAN:
        return Boolean.parseBoolean(literalValue);
      default:
        break;
    }
    return literalValue;
  }

  public static boolean isNumericType(int jdbcType) {
    return TypeMap.isNumericType(jdbcType);
  }

  public static boolean isDateTimeType(int jdbcType) {
    return TypeMap.isDateTimeType(jdbcType);
  }

  public static boolean isTextType(JDBCType jdbcType) {
    return TypeMap.isTextType(jdbcType.getVendorTypeNumber());
  }

  public static boolean isDateTimeType(JDBCType jdbcType) {
    return TypeMap.isDateTimeType(jdbcType.getVendorTypeNumber());
  }

  public static boolean isBinaryType(JDBCType jdbcType) {
    return TypeMap.isBinaryType(jdbcType.getVendorTypeNumber());
  }

  public static boolean isBinaryType(int jdbcType) {
    return TypeMap.isBinaryType(jdbcType);
  }

  public static boolean isNumericType(JDBCType jdbcType) {
    return TypeMap.isNumericType(jdbcType.getVendorTypeNumber());
  }

  public static boolean isConnectionOpen(ResultSet resultSet) {
    if (resultSet == null) {
      return false;
    }
    try {
      Statement stmt = resultSet.getStatement();
      Connection conn = stmt.getConnection();
      return !conn.isClosed();
    } catch (SQLException ex) {
      return false;
    }
  }

  public static void setParameterValue(PreparedStatement statement, int index, int jdbcType, Object value) throws SQLException {
    if (value == null) {
      statement.setNull(index, jdbcType);
    } else if (value instanceof String) {
      statement.setString(index, (String) value);
    } else if (value instanceof byte[]) {
      statement.setBytes(index, (byte[]) value);
    } else if (value instanceof Boolean) {
      statement.setBoolean(index, (Boolean) value);
    } else if (value instanceof Byte) {
      statement.setByte(index, (Byte) value);
    } else if (value instanceof Short) {
      statement.setShort(index, (Short) value);
    } else if (value instanceof Integer) {
      statement.setInt(index, (Integer) value);
    } else if (value instanceof Long) {
      statement.setLong(index, (Long) value);
    } else if (value instanceof BigDecimal) {
      // setObject assumes a scale of 0, so we rather use the typed setter
      statement.setBigDecimal(index, (BigDecimal) value);
    } else if (value instanceof Float) {
      statement.setFloat(index, (Float) value);
    } else if (value instanceof Double) {
      statement.setDouble(index, (Double) value);
    } else {
      statement.setObject(index, value, jdbcType);
    }
  }

  public static void commit(Connection connection) throws SQLException {
    if (!connection.getAutoCommit()) {
      connection.commit();
    }
  }

  public static Object extractColumnValue(ResultSet resultSet, String columnName, int columnIdx, int jdbcType) throws SQLException {
    final boolean useIdx = (columnName == null);
    Object value;
    switch (jdbcType) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
        value = useIdx ? resultSet.getString(columnIdx) : resultSet.getString(columnName);
        break;
      case Types.NUMERIC:
      case Types.DECIMAL:
        value = useIdx ? resultSet.getBigDecimal(columnIdx) : resultSet.getBigDecimal(columnName);
        break;
      case Types.BIT:
      case Types.BOOLEAN:
        value = useIdx ? resultSet.getBoolean(columnIdx) : resultSet.getBoolean(columnName);
        break;
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        value = useIdx ? resultSet.getInt(columnIdx) : resultSet.getInt(columnName);
        break;
      case Types.BIGINT:
        value = useIdx ? resultSet.getLong(columnIdx) : resultSet.getLong(columnName);
        break;
      case Types.REAL:
        value = useIdx ? resultSet.getFloat(columnIdx) : resultSet.getFloat(columnName);
        break;
      case Types.FLOAT:
      case Types.DOUBLE:
        value = useIdx ? resultSet.getDouble(columnIdx) : resultSet.getDouble(columnName);
        break;
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        value = useIdx ? resultSet.getBytes(columnIdx) : resultSet.getBytes(columnName);
        break;
      case Types.DATE:
        value = useIdx ? resultSet.getDate(columnIdx) : resultSet.getDate(columnName);
        break;
      case Types.TIME:
        value = useIdx ? resultSet.getTime(columnIdx) : resultSet.getTime(columnName);
        break;
      case Types.TIMESTAMP:
        value = useIdx ? resultSet.getTimestamp(columnIdx) : resultSet.getTimestamp(columnName);
        break;
      case Types.CLOB:
        Clob clob = useIdx ? resultSet.getClob(columnIdx) : resultSet.getClob(columnName);

        if (clob == null) {
          value = null;
        } else {
          long length = clob.length();

          if (length > Integer.MAX_VALUE) {
            value = clob;
          } else if (length == 0) {
            // the Javadoc is not clear about whether Clob.getSubString
            // can be used with a substring length of 0
            // thus we do the safe thing and handle it ourselves
            value = "";
          } else {
            value = clob.getSubString(1L, (int) length);
          }
        }
        break;
      case Types.BLOB:
        Blob blob = useIdx ? resultSet.getBlob(columnIdx) : resultSet.getBlob(columnName);

        if (blob == null) {
          value = null;
        } else {
          long length = blob.length();

          if (length > Integer.MAX_VALUE) {
            value = blob;
          } else if (length == 0) {
            // the Javadoc is not clear about whether Blob.getBytes
            // can be used with for 0 bytes to be copied
            // thus we do the safe thing and handle it ourselves
            value = new byte[0];
          } else {
            value = blob.getBytes(1L, (int) length);
          }
        }
        break;
      case Types.ARRAY:
        value = useIdx ? resultSet.getArray(columnIdx) : resultSet.getArray(columnName);
        break;
      case Types.REF:
        value = useIdx ? resultSet.getRef(columnIdx) : resultSet.getRef(columnName);
        break;
      default:
        value = useIdx ? resultSet.getObject(columnIdx) : resultSet.getObject(columnName);
        break;
    }
    return resultSet.wasNull() ? null : value;
  }
}
