package org.apache.ddlutils.model;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.DefaultValueHelper;
import org.apache.ddlutils.util.JdbcUtils;

import java.sql.*;
import java.util.List;

public final class ModelUtils {

  public static Database cloneDatabase(Database sourceModel) {
    return new CloneHelper().clone(sourceModel);
  }

  /**
   * Returns a copy of the given model adjusted for type changes because of the native type mappings
   * which when read back from the database will map to different types.
   *
   * @param sourceModel The source model
   * @return The adjusted model
   */
  public static Database adjustModel(Platform platform, Database sourceModel) {
    Database model = cloneDatabase(sourceModel);
    final PlatformInfo platformInfo = platform.getPlatformInfo();
    for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++) {
      Table table = model.getTable(tableIdx);
      for (int columnIdx = 0; columnIdx < table.getColumnCount(); columnIdx++) {
        Column column = table.getColumn(columnIdx);
        int origType = column.getTypeCode();
        int targetType = platformInfo.getTargetJdbcType(origType);

        // we adjust the column types if the native type would back-map to a
        // different jdbc type
        if (targetType != origType) {
          column.setTypeCode(targetType);
          // we should also adapt the default value
          if (column.getDefaultValue() != null) {
            DefaultValueHelper helper = platform.getSqlBuilder().getDefaultValueHelper();

            column.setDefaultValue(helper.convert(column.getDefaultValue(), origType, targetType));
          }
        }
        // we also promote the default size if the column has no size
        // spec of its own
        if ((column.getSize() == null) && platformInfo.hasSize(targetType)) {
          Integer defaultSize = platformInfo.getDefaultSize(targetType);

          if (defaultSize != null) {
            column.setSize(defaultSize.toString());
          }
        }
        // finally the platform might return a synthetic default value if the column
        // is a primary key column
        if (platformInfo.isSyntheticDefaultValueForRequiredReturned()) {
          setColumnDefaultValue(column);
        }
        if (column.isPrimaryKey() && platformInfo.isPrimaryKeyColumnAutomaticallyRequired()) {
          column.setRequired(true);
        }
        if (column.isAutoIncrement() && platformInfo.isIdentityColumnAutomaticallyRequired()) {
          column.setRequired(true);
        }
      }
      // we also add the default names to foreign keys that are initially unnamed
      for (ForeignKey foreignKey : table.getForeignKeys()) {
        if (foreignKey.getName() == null) {
          foreignKey.setName(platform.getSqlBuilder().getForeignKeyName(table, foreignKey));
        }
      }
    }
    return model;
  }

  private static void setColumnDefaultValue(Column column) {
    if (column.getDefaultValue() == null && column.isRequired() && !column.isAutoIncrement()) {
      switch (column.getTypeCode()) {
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
          column.setDefaultValue("0");
          break;
        case Types.REAL:
        case Types.FLOAT:
        case Types.DOUBLE:
          column.setDefaultValue("0.0");
          break;
        case Types.BIT:
          column.setDefaultValue("false");
          break;
        default:
          column.setDefaultValue("");
          break;
      }
    }
  }

  public static void sortForeignKeys(Database model, Platform platform) {
    for (int tableIdx = 0; tableIdx < model.getTableCount(); tableIdx++) {
      model.getTable(tableIdx).sortForeignKeys(platform.isDelimitedIdentifierModeOn());
    }
  }

  public static void setAutoIncrement(ResultSet rs, List<Column> columns) throws SQLException {
    ResultSetMetaData rsMetaData = rs.getMetaData();
    for (int idx = 0; idx < columns.size(); idx++) {
      if (rsMetaData.isAutoIncrement(idx + 1)) {
        columns.get(idx).setAutoIncrement(true);
      }
    }
  }

  public static void setAutoIncrement(ResultSet rs, Column... columns) throws SQLException {
    ResultSetMetaData rsMetaData = rs.getMetaData();
    for (int idx = 0; idx < columns.length; idx++) {
      if (rsMetaData.isAutoIncrement(idx + 1)) {
        columns[idx].setAutoIncrement(true);
      }
    }
  }

  public static boolean isCharColumnWithDefaultValue(Column column) {
    return JDBCType.CHAR.getVendorTypeNumber() == column.getTypeCode() && column.getDefaultValue() != null;
  }

  public static boolean isNumericColumnWithScale(Column column) {
    return Types.NUMERIC == column.getTypeCode() && column.getScale() > 0;
  }

  /**
   * Calculates the maximum textual length required to represent values of the given column.
   * <p>
   * For numeric types with scale (decimal/floating point numbers), this method adds 1 to account
   * for the decimal point. For other numeric types, it returns the column size plus scale.
   * For non-numeric types, it simply returns the column size.
   *
   * <p>for example:</p>
   * <li>For a VARCHAR(10) column: returns 10</li>
   *
   * <li> For an INTEGER(5) column: returns 5 </li>
   * <li> For a DECIMAL(8,2) column: returns 8 + 2 + 1 = 11 (8 for precision, 2 for scale, 1 for decimal point) </li>
   * <li> For a NUMERIC(10,3) column: returns 10 + 3 + 1 = 14 </li>
   *
   * @param column The column to determine the maximum textual length for
   * @return The maximum number of characters needed to represent values of this column,
   * including space for the decimal point in numeric types with scale
   */
  public static int getMaxTextualLength(Column column) {
    if (JdbcUtils.isNumericType(column.getTypeCode())) {
      if (column.getScale() > 0) {
        // with decimal point
        return column.getSizeAsInt() + 1;
      }
      return column.getSizeAsInt() + column.getScale();
    }
    return column.getSizeAsInt();
  }

  public static void applyChange(Column column, Column newColumn) {
    column.setTypeCode(newColumn.getTypeCode());
    column.setSize(newColumn.getSize());
    column.setAutoIncrement(newColumn.isAutoIncrement());
    column.setRequired(newColumn.isRequired());
    column.setDescription(newColumn.getDescription());
    column.setDefaultValue(newColumn.getDefaultValue());
  }
}
