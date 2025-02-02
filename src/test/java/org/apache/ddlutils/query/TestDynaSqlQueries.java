package org.apache.ddlutils.query;

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

import org.apache.ddlutils.TestAgainstLiveDatabaseBase;
import org.apache.ddlutils.data.RowObject;
import org.apache.ddlutils.data.TableClass;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.apache.ddlutils.platform.ModelBasedResultSetIterator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the sql querying.
 *
 * @version $Revision: 289996 $
 */
public class TestDynaSqlQueries extends TestAgainstLiveDatabaseBase {

  /**
   * Helper method to wrap the given identifier in delimiters if delimited identifier mode is turned on for the test.
   *
   * @param name The identifier
   * @return The identifier, wrapped if delimited identifier mode is turned on, or as-is if not
   */
  private String asIdentifier(String name) {
    if (getPlatform().isDelimitedIdentifierModeOn()) {
      return getPlatformInfo().getDelimiterToken() + name + getPlatformInfo().getDelimiterToken();
    } else {
      return name;
    }
  }

  @Override
  protected void setUp() throws Exception {
    System.setProperty(JDBC_PROPERTIES_PROPERTY, "org/apache/ddlutils/platform/mysql/jdbc.properties.mysql50");
    super.setUp();
  }

  /**
   * Tests a simple SELECT query.
   */
  @Test
  public void testSimpleQuery() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable TheId='1' TheText='Text 1'/>\n" +
        "  <TestTable TheId='2' TheText='Text 2'/>\n" +
        "  <TestTable TheId='3' TheText='Text 3'/>" +
        "</data>");

    ModelBasedResultSetIterator it = (ModelBasedResultSetIterator) getPlatform().query(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertTrue(it.hasNext());
    // we call the method a second time to assert that the result set does not get advanced twice
    Assert.assertTrue(true);

    RowObject row = it.next();

    Assert.assertEquals(1, getPropertyValue(row, "TheId"));
    Assert.assertEquals("Text 1", getPropertyValue(row, "TheText"));

    Assert.assertTrue(it.hasNext());

    row = it.next();

    Assert.assertEquals(2, getPropertyValue(row, "TheId"));
    Assert.assertEquals("Text 2", getPropertyValue(row, "TheText"));

    Assert.assertTrue(it.hasNext());

    row = it.next();

    Assert.assertEquals(3, getPropertyValue(row, "TheId"));
    Assert.assertEquals("Text 3", getPropertyValue(row, "TheText"));

    Assert.assertFalse(it.hasNext());
    Assert.assertFalse(it.isConnectionOpen());
  }

  /**
   * Tests a simple SELECT fetch.
   */
  @Test
  public void testSimpleFetch() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable TheId='1' TheText='Text 1'/>\n" +
        "  <TestTable TheId='2' TheText='Text 2'/>\n" +
        "  <TestTable TheId='3' TheText='Text 3'/>" +
        "</data>");

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(3, beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 1", getPropertyValue(bean, "TheText"));

    bean = beans.get(1);

    Assert.assertEquals(2, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 2", getPropertyValue(bean, "TheText"));

    bean = beans.get(2);

    Assert.assertEquals(3, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 3", getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests insertion & reading of auto-increment columns.
   */
  @Test
  public void testAutoIncrement() throws Exception {
    // we need special catering for Sybase which does not support identity for INTEGER columns
    final String modelXml;

    if (BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName())) {
      modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      modelXml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(modelXml);

    // we're inserting the rows manually via beans since we do want to
    // check the back-reading of the auto-increment columns
    TableClass dynaClass = getModel().getTableClassFor("TestTable");
    RowObject bean;
    Object id1 = null;
    Object id2 = null;
    Object id3 = null;

    bean = dynaClass.newInstance();
    bean.set("TheText", "Text 1");
    getPlatform().insert(getModel(), bean);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      // we cannot know the value for sure (though it usually will be 1)
      id1 = getPropertyValue(bean, "TheId");
      Assert.assertNotNull(id1);
    }
    bean = dynaClass.newInstance();
    bean.set("TheText", "Text 2");
    getPlatform().insert(getModel(), bean);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      // we cannot know the value for sure (though it usually will be 2)
      id2 = getPropertyValue(bean, "TheId");
      Assert.assertNotNull(id2);
    }
    bean = dynaClass.newInstance();
    bean.set("TheText", "Text 3");
    getPlatform().insert(getModel(), bean);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      // we cannot know the value for sure (though it usually will be 3)
      id3 = getPropertyValue(bean, "TheId");
      Assert.assertNotNull(id3);
    }

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(3,
      beans.size());

    bean = beans.get(0);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      Assert.assertEquals(id1,
        getPropertyValue(bean, "TheId"));
    } else {
      Assert.assertNotNull(getPropertyValue(bean, "TheId"));
    }
    Assert.assertEquals("Text 1",
      getPropertyValue(bean, "TheText"));

    bean = beans.get(1);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      Assert.assertEquals(id2,
        getPropertyValue(bean, "TheId"));
    } else {
      Assert.assertNotNull(getPropertyValue(bean, "TheId"));
    }
    Assert.assertEquals("Text 2",
      getPropertyValue(bean, "TheText"));

    bean = beans.get(2);
    if (getPlatformInfo().isLastIdentityValueReadable()) {
      Assert.assertEquals(id3,
        getPropertyValue(bean, "TheId"));
    } else {
      Assert.assertNotNull(getPropertyValue(bean, "TheId"));
    }
    Assert.assertEquals("Text 3",
      getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests a more complicated SELECT query that leads to a JOIN in the database.
   */
  @Test
  public void testJoinQuery() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable1'>\n" +
        "    <column name='Id1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='Id2' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='TestTable2'>\n" +
        "    <column name='Id' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='Avalue' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable1 Id1='1'/>\n" +
        "  <TestTable1 Id1='2' Id2='3'/>\n" +
        "  <TestTable2 Id='1' Avalue='Text 1'/>\n" +
        "  <TestTable2 Id='2' Avalue='Text 2'/>\n" +
        "  <TestTable2 Id='3' Avalue='Text 3'/>" +
        "</data>");

    String sql = "SELECT " +
      asIdentifier("Id1") +
      "," +
      asIdentifier("Avalue") +
      " FROM " +
      asIdentifier("TestTable1") +
      "," +
      asIdentifier("TestTable2") +
      " WHERE " +
      asIdentifier("Id2") +
      "=" +
      asIdentifier("Id");

    ModelBasedResultSetIterator it = (ModelBasedResultSetIterator) getPlatform().query(getModel(),
      sql,
      new Table[]{getModel().getTable(0), getModel().getTable(1)});

    Assert.assertTrue(it.hasNext());

    RowObject bean = it.next();

    Assert.assertEquals(2, getPropertyValue(bean, "Id1"));
    Assert.assertEquals("Text 3", getPropertyValue(bean, "Avalue"));

    Assert.assertFalse(it.hasNext());
    Assert.assertFalse(it.isConnectionOpen());
  }

  /**
   * Tests the insert method.
   */
  @Test
  public void testInsertSingle() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    TableClass dynaClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject = new RowObject(dynaClass);

    rowObject.set("TheId", 1);
    rowObject.set("TheText", "Text 1");

    getPlatform().insert(getModel(), rowObject);

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(1, beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 1", getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests the insert method.
   */
  @Test
  public void testInsertMultiple() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    TableClass dynaClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject1 = new RowObject(dynaClass);
    RowObject rowObject2 = new RowObject(dynaClass);
    RowObject rowObject3 = new RowObject(dynaClass);

    rowObject1.set("TheId", 1);
    rowObject1.set("TheText", "Text 1");
    rowObject2.set("TheId", 2);
    rowObject2.set("TheText", "Text 2");
    rowObject3.set("TheId", 3);
    rowObject3.set("TheText", "Text 3");

    List<RowObject> rowObjects = new ArrayList<>();

    rowObjects.add(rowObject1);
    rowObjects.add(rowObject2);
    rowObjects.add(rowObject3);

    getPlatform().insert(getModel(), rowObjects);

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(3, beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 1", getPropertyValue(bean, "TheText"));

    bean = beans.get(1);

    Assert.assertEquals(2, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 2", getPropertyValue(bean, "TheText"));

    bean = beans.get(2);

    Assert.assertEquals(3, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 3", getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests the update method.
   */
  @Test
  public void testUpdate() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable TheId='1' TheText='Text 1'/>\n" +
        "</data>");

    TableClass dynaClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject = new RowObject(dynaClass);

    rowObject.set("TheId", 1);
    rowObject.set("TheText", "Text 10");

    getPlatform().update(getModel(), rowObject);

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(1,
      beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1,
      getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 10",
      getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests the exists method.
   */
  @Test
  public void testExists() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable TheId='1' TheText='Text 1'/>\n" +
        "  <TestTable TheId='3' TheText='Text 3'/>\n" +
        "</data>");

    TableClass dynaClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject1 = new RowObject(dynaClass);
    RowObject rowObject2 = new RowObject(dynaClass);
    RowObject rowObject3 = new RowObject(dynaClass);

    rowObject1.set("TheId", 1);
    rowObject1.set("TheText", "Text 1");
    rowObject2.set("TheId", 2);
    rowObject2.set("TheText", "Text 2");
    rowObject3.set("TheId", 3);
    rowObject3.set("TheText", "Text 30");

    Assert.assertTrue(getPlatform().exists(getModel(), rowObject1));
    Assert.assertFalse(getPlatform().exists(getModel(), rowObject2));
    Assert.assertTrue(getPlatform().exists(getModel(), rowObject3));
  }

  /**
   * Tests the store method.
   */
  @Test
  public void testStoreNew() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    TableClass tableClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject = new RowObject(tableClass);

    rowObject.set("TheId", 1);
    rowObject.set("TheText", "Text 1");

    getPlatform().store(getModel(), rowObject);

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(1, beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 1", getPropertyValue(bean, "TheText"));
  }

  /**
   * Tests the store method.
   */
  @Test
  public void testStoreExisting() throws Exception {
    createDatabase(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='ddlutils'>\n" +
        "  <table name='TestTable'>\n" +
        "    <column name='TheId' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='TheText' type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>");

    insertData(
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <TestTable TheId='1' TheText='Text 1'/>\n" +
        "</data>");

    TableClass tableClass = TableClass.newInstance(getModel().getTable(0));
    RowObject rowObject = new RowObject(tableClass);

    rowObject.set("TheId", 1);
    rowObject.set("TheText", "Text 10");

    getPlatform().store(getModel(), rowObject);

    List<RowObject> beans = getPlatform().fetch(getModel(),
      "SELECT * FROM " + asIdentifier("TestTable"),
      new Table[]{getModel().getTable(0)});

    Assert.assertEquals(1, beans.size());

    RowObject bean = beans.get(0);

    Assert.assertEquals(1, getPropertyValue(bean, "TheId"));
    Assert.assertEquals("Text 10", getPropertyValue(bean, "TheText"));
  }
}
