package org.apache.ddlutils.platform;

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

import org.apache.ddlutils.TestBase;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test the base SqlBuilder class.
 *
 * @version $Revision: $
 */
public class TestSqlBuilder extends TestBase {
  /**
   * Tests the {@link SqlBuilder#getUpdateSql(Table, Map, boolean)} method.
   */
  @Test
  public void testUpdateSql1() {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='id' autoIncrement='true' type='INTEGER' primaryKey='true'/>\n" +
        "    <column name='name' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>";

    TestPlatform platform = new TestPlatform();
    SqlBuilder sqlBuilder = platform.getSqlBuilder();
    Database database = parseDatabaseFromString(modelXml);
    Map<String, Object> map = new HashMap<>();

    map.put("name", "ddlutils");
    map.put("id", 0);

    platform.setDelimitedIdentifierModeOn(true);

    String sql = sqlBuilder.getUpdateSql(database.getTable(0), map, false);

    Assert.assertEquals("UPDATE \"TestTable\" SET \"name\" = 'ddlutils' WHERE \"id\" = '0'",
      sql);
  }

  /**
   * Tests the {@link SqlBuilder#getUpdateSql(Table, Map, Map, boolean)} method.
   */
  @Test
  public void testUpdateSql2() {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='id' autoIncrement='true' type='INTEGER' primaryKey='true'/>\n" +
        "    <column name='name' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>";

    TestPlatform platform = new TestPlatform();
    SqlBuilder sqlBuilder = platform.getSqlBuilder();
    Database database = parseDatabaseFromString(modelXml);
    Map<String, Object> oldMap = new HashMap<>();
    Map<String, Object> newMap = new HashMap<>();

    oldMap.put("id", 0);

    newMap.put("name", "ddlutils");
    newMap.put("id", 1);

    platform.setDelimitedIdentifierModeOn(true);

    String sql = sqlBuilder.getUpdateSql(database.getTable(0), oldMap, newMap, false);

    Assert.assertEquals("UPDATE \"TestTable\" SET \"id\" = '1', \"name\" = 'ddlutils' WHERE \"id\" = '0'",
      sql);
  }
}
