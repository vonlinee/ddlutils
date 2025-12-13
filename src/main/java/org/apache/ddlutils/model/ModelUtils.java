package org.apache.ddlutils.model;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.DefaultValueHelper;

import java.sql.Types;

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
}
