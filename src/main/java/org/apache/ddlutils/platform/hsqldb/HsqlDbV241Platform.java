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
package org.apache.ddlutils.platform.hsqldb;

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.*;
import org.apache.ddlutils.util.StringUtilsExt;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * hsqldb v2.4.1
 */
public class HsqlDbV241Platform extends HsqlDbPlatform {

  public HsqlDbV241Platform() {
    super();

    PlatformInfo info = getPlatformInfo();

    final int targetJdbcType = info.getTargetJdbcType(JDBCType.BLOB.getVendorTypeNumber());
    if (targetJdbcType == Types.LONGVARBINARY) {
      getPlatformInfo().addNativeTypeMapping(Types.BLOB, "VARBINARY", Types.VARBINARY);
    }
    info.addNativeTypeMapping(Types.REAL, "DOUBLE", Types.DOUBLE);
    info.addNativeTypeMapping(Types.LONGVARBINARY, JDBCType.VARBINARY.name(), Types.VARBINARY);

    info.setIdentityColumnAutomaticallyRequired(false);

    info.addEquivalentOnDeleteActions(CascadeAction.RESTRICT, CascadeAction.NONE);

    setSqlBuilder(new HsqlDbV241SqlBuilder(this));
    setModelReader(new HsqlDbV241ModelReader(this));
  }

  /**
   * in Hsqldb 2.4.1, the table created with sql:
   * <blockquote><pre>
   * CREATE TABLE "roundtrip2"
   * (
   *     "pk" INTEGER NOT NULL,
   *     "avalue1" CHAR,
   *     "avalue2" CHAR(8) DEFAULT 'text' NOT NULL,
   *     PRIMARY KEY ("pk")
   * );
   * </pre></blockquote><p>
   * will be converted to:
   * <blockquote><pre>
   * CREATE TABLE PUBLIC.PUBLIC."roundtrip2" (
   * 	"pk" INTEGER NOT NULL,
   * 	"avalue1" CHARACTER(1),
   * 	"avalue2" CHARACTER(8) DEFAULT 'text    ' NOT NULL,
   * 	CONSTRAINT SYS_PK_10161 PRIMARY KEY ("pk")
   * );
   *
   * </pre></blockquote><p>
   * <p>
   * CHAR(4) will be appended with spaces to make it 8 characters long.
   */
  @Override
  protected Object getColumnObjectFromResultSet(ResultSet resultSet, Column column) throws SQLException {
    Object value = super.getColumnObjectFromResultSet(resultSet, column);
    if (value != null && ModelUtils.isCharColumnWithDefaultValue(column)) {
      // TODO Is this the right way to do it ?
      // for existed default values, we need to convert it to the expected value of the column.
      String charValue = value.toString();
      String expectedDefaultValue = StringUtilsExt.rightPad(column.getDefaultValue(), column.getSizeAsInt());
      if (Objects.equals(charValue, expectedDefaultValue)) {
        value = StringUtilsExt.rightTrim(charValue);
      }
    }
    return value;
  }

  @Override
  public boolean isDefaultValueMatched(Column column, Column anotherColumn) {
    if (ModelUtils.isCharColumnWithDefaultValue(column)) {
      Object parsedDefaultValue = column.getParsedDefaultValue();
      Object anotherParsedDefaultValue = anotherColumn.getParsedDefaultValue();
      if (parsedDefaultValue == null || anotherParsedDefaultValue == null) {
        return false;
      }
      return Objects.equals(StringUtilsExt.rightTrim(String.valueOf(parsedDefaultValue)),
        StringUtilsExt.rightTrim(String.valueOf(anotherParsedDefaultValue)));
    }
    return super.isDefaultValueMatched(column, anotherColumn);
  }

  @Override
  public Database adjustModel(Database sourceModel) {
    Database database = super.adjustModel(sourceModel);
    for (Table table : database.getTables()) {
      for (Column column : table.getColumns()) {
        if (column.isPrimaryKey() && !column.isRequired()) {
          column.setRequired(true);
        }
      }
    }
    return database;
  }
}
