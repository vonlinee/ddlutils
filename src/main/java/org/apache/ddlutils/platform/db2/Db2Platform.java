package org.apache.ddlutils.platform.db2;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.PrimaryKeyChange;
import org.apache.ddlutils.alteration.RemoveColumnChange;
import org.apache.ddlutils.alteration.RemovePrimaryKeyChange;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.CascadeActionEnum;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.apache.ddlutils.platform.CreationParameters;
import org.apache.ddlutils.platform.DefaultTableDefinitionChangesPredicate;
import org.apache.ddlutils.platform.PlatformImplBase;
import org.apache.ddlutils.util.StringUtils;

import java.io.IOException;
import java.sql.Types;

/**
 * The DB2 platform implementation.
 *
 * @version $Revision: 231306 $
 */
public class Db2Platform extends PlatformImplBase {

  /**
   * Creates a new platform instance.
   */
  public Db2Platform() {
    PlatformInfo info = getPlatformInfo();

    info.setMaxIdentifierLength(18);
    info.setIdentityColumnAutomaticallyRequired(true);
    info.setPrimaryKeyColumnsHaveToBeRequired(true);
    info.setMultipleIdentityColumnsSupported(false);
    info.setSupportedOnUpdateActions(new CascadeActionEnum[]{CascadeActionEnum.RESTRICT, CascadeActionEnum.NONE});
    info.setSupportedOnDeleteActions(new CascadeActionEnum[]{CascadeActionEnum.RESTRICT, CascadeActionEnum.CASCADE, CascadeActionEnum.SET_NULL, CascadeActionEnum.NONE});

    // the BINARY types are also handled by Db2Builder.getSqlType(Column)
    info.addNativeTypeMapping(Types.ARRAY, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.BINARY, "CHAR {0} FOR BIT DATA");
    info.addNativeTypeMapping(Types.BIT, "SMALLINT", Types.SMALLINT);
    info.addNativeTypeMapping(Types.BOOLEAN, "SMALLINT", Types.SMALLINT);
    info.addNativeTypeMapping(Types.FLOAT, "DOUBLE", Types.DOUBLE);
    info.addNativeTypeMapping(Types.JAVA_OBJECT, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.LONGVARBINARY, "LONG VARCHAR FOR BIT DATA");
    info.addNativeTypeMapping(Types.LONGVARCHAR, "LONG VARCHAR");
    info.addNativeTypeMapping(Types.NULL, "LONG VARCHAR FOR BIT DATA", Types.LONGVARBINARY);
    info.addNativeTypeMapping(Types.NUMERIC, "DECIMAL", Types.DECIMAL);
    info.addNativeTypeMapping(Types.OTHER, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.STRUCT, "BLOB", Types.BLOB);
    info.addNativeTypeMapping(Types.TINYINT, "SMALLINT", Types.SMALLINT);
    info.addNativeTypeMapping(Types.VARBINARY, "VARCHAR {0} FOR BIT DATA");

    info.setDefaultSize(Types.CHAR, 254);
    info.setDefaultSize(Types.VARCHAR, 254);
    info.setDefaultSize(Types.BINARY, 254);
    info.setDefaultSize(Types.VARBINARY, 254);

    setSqlBuilder(new Db2Builder(this));
    setModelReader(new Db2ModelReader(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return BuiltinDriverType.DB2.getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ModelComparator getModelComparator() {
    ModelComparator comparator = super.getModelComparator();

    comparator.setCanDropPrimaryKeyColumns(false);
    comparator.setGeneratePrimaryKeyChanges(false);
    return comparator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected TableDefinitionChangesPredicate getTableDefinitionChangesPredicate() {
    return new DefaultTableDefinitionChangesPredicate() {
      @Override
      protected boolean isSupported(Table intermediateTable, TableChange change) {
        if ((change instanceof RemoveColumnChange) ||
          (change instanceof PrimaryKeyChange) ||
          (change instanceof RemovePrimaryKeyChange)) {
          return true;
        } else if (change instanceof AddColumnChange) {
          AddColumnChange addColumnChange = (AddColumnChange) change;
          // DB2 cannot add IDENTITY columns, and required columns need a default value
          return (addColumnChange.getNextColumn() == null) &&
            !addColumnChange.getNewColumn().isAutoIncrement() &&
            (!addColumnChange.getNewColumn().isRequired() ||
              !StringUtils.isEmpty(addColumnChange.getNewColumn().getDefaultValue()));
        } else {
          return false;
        }
      }
    };
  }

  /**
   * Processes a change representing the addition of a column.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RemoveColumnChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);
    Column removedColumn = changedTable.findColumn(change.getChangedColumn(), isDelimitedIdentifierModeOn());

    ((Db2Builder) getSqlBuilder()).dropColumn(changedTable, removedColumn);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes the removal of a primary key from a table.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            RemovePrimaryKeyChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);

    ((Db2Builder) getSqlBuilder()).dropPrimaryKey(changedTable);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }

  /**
   * Processes the change of the primary key of a table.
   *
   * @param currentModel The current database schema
   * @param params       The parameters used in the creation of new tables. Note that for existing
   *                     tables, the parameters won't be applied
   * @param change       The change object
   */
  public void processChange(Database currentModel,
                            CreationParameters params,
                            PrimaryKeyChange change) throws IOException {
    Table changedTable = findChangedTable(currentModel, change);
    String[] newPKColumnNames = change.getNewPrimaryKeyColumns();
    Column[] newPKColumns = new Column[newPKColumnNames.length];

    for (int colIdx = 0; colIdx < newPKColumnNames.length; colIdx++) {
      newPKColumns[colIdx] = changedTable.findColumn(newPKColumnNames[colIdx], isDelimitedIdentifierModeOn());
    }
    ((Db2Builder) getSqlBuilder()).dropPrimaryKey(changedTable);
    getSqlBuilder().createPrimaryKey(changedTable, newPKColumns);
    change.apply(currentModel, isDelimitedIdentifierModeOn());
  }
}
