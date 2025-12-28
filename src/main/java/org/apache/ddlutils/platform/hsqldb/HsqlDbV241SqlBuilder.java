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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;

import java.io.IOException;
import java.sql.JDBCType;

/**
 * The SQL Builder for the HsqlDb database V2.4.1.
 */
class HsqlDbV241SqlBuilder extends HsqlDbBuilder {

  public HsqlDbV241SqlBuilder(Platform platform) {
    super(platform);
  }

  @Override
  protected String getSqlType(Column column, String nativeType) {
    String sqlType = super.getSqlType(column, nativeType);
    if (JDBCType.VARBINARY.getName().equals(sqlType)) {
      // in Hsqldb 2.4.1:
      // if length is not specified explicitly, it will throw exception:
      // length must be specified in type definition: VARBINARY
      // when read back from database, the length will be 2147483647 (Integer.MAX_VALUE)
      sqlType = sqlType + "(" + Integer.MAX_VALUE + ")";
    }
    return sqlType;
  }

  /**
   * <blockquote><pre>
   * # in hsqldb 1.10.8.1
   * SELECT SUBSTR(CAST(123242 AS VARCHAR(20)), null) FROM INFORMATION_SCHEMA.columns;
   * # in hsqldb 2.4.1
   * SELECT SUBSTR(CAST(123242 AS VARCHAR(20))) FROM INFORMATION_SCHEMA.columns;
   * </pre></blockquote>
   */
  @Override
  protected void writeCastExpressionWithSubString(Column sourceColumn, Column targetColumn) throws IOException {
    if (targetColumn.getSize() == null) {
      print("SUBSTR(");
      writeColumnCastExpression(sourceColumn, targetColumn);
      print(",1)");
    } else {
      super.writeCastExpressionWithSubString(sourceColumn, targetColumn);
    }
  }
}
