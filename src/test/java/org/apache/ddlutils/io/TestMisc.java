package org.apache.ddlutils.io;

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
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Contains misc tests.
 *
 * @version $Revision: $
 */
public class TestMisc extends TestAgainstLiveDatabaseBase {

  /**
   * Tests the backup and restore of a table with an identity column and a foreign key to
   * it when identity override is turned on.
   */
  @Test
  public void testIdentityOverrideOn() throws Exception {
    if (!getPlatformInfo().isIdentityOverrideAllowed()) {
      // TODO: for testing these platforms, we need deleteRows
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc1'>\n" +
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='avalue' type='INTEGER' required='false'/>\n" +
          "  </table>\n" +
          "  <table name='misc2'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
          "    <column name='fk' type='NUMERIC' size='12,0' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc1'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc1'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='avalue' type='INTEGER' required='false'/>\n" +
          "  </table>\n" +
          "  <table name='misc2'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
          "    <column name='fk' type='INTEGER' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc1'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    if (isSybase) {
      insertRow("misc1", new Object[]{new BigDecimal(10), 1});
      insertRow("misc1", new Object[]{new BigDecimal(12), 2});
      insertRow("misc1", new Object[]{new BigDecimal(13), 3});
      insertRow("misc2", new Object[]{1, new BigDecimal(10)});
      insertRow("misc2", new Object[]{2, new BigDecimal(13)});
    } else {
      insertRow("misc1", new Object[]{10, 1});
      insertRow("misc1", new Object[]{12, 2});
      insertRow("misc1", new Object[]{13, 3});
      insertRow("misc2", new Object[]{1, 10});
      insertRow("misc2", new Object[]{2, 13});
    }

    StringWriter stringWriter = new StringWriter();
    DatabaseDataIO dataIO = new DatabaseDataIO();

    dataIO.writeDataToXML(getPlatform(), getModel(), stringWriter, "UTF-8");

    String dataAsXml = stringWriter.toString();
    SAXReader reader = new SAXReader();
    Document testDoc = reader.read(new InputSource(new StringReader(dataAsXml)));

    List<Node> misc1Rows = testDoc.selectNodes("//misc1");
    List<Node> misc2Rows = testDoc.selectNodes("//misc2");
    String pkColumnName = "pk";
    String fkColumnName = "fk";
    String valueColumnName = "avalue";

    if (misc1Rows.isEmpty()) {
      misc1Rows = testDoc.selectNodes("//MISC1");
      misc2Rows = testDoc.selectNodes("//MISC2");
      pkColumnName = pkColumnName.toUpperCase();
      fkColumnName = fkColumnName.toUpperCase();
      valueColumnName = valueColumnName.toUpperCase();
    }

    Assert.assertEquals(3, misc1Rows.size());
    Assert.assertEquals("10", ((Element) misc1Rows.get(0)).attributeValue(pkColumnName));
    Assert.assertEquals("1", ((Element) misc1Rows.get(0)).attributeValue(valueColumnName));
    Assert.assertEquals("12", ((Element) misc1Rows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("2", ((Element) misc1Rows.get(1)).attributeValue(valueColumnName));
    Assert.assertEquals("13", ((Element) misc1Rows.get(2)).attributeValue(pkColumnName));
    Assert.assertEquals("3", ((Element) misc1Rows.get(2)).attributeValue(valueColumnName));
    Assert.assertEquals(2, misc2Rows.size());
    Assert.assertEquals("1", ((Element) misc2Rows.get(0)).attributeValue(pkColumnName));
    Assert.assertEquals("10", ((Element) misc2Rows.get(0)).attributeValue(fkColumnName));
    Assert.assertEquals("2", ((Element) misc2Rows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("13", ((Element) misc2Rows.get(1)).attributeValue(fkColumnName));

    dropDatabase();
    createDatabase(modelXml);

    StringReader stringReader = new StringReader(dataAsXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc1");

    if (isSybase) {
      assertEquals(new BigDecimal(10), beans.get(0), "pk");
      assertEquals(new BigDecimal(12), beans.get(1), "pk");
      assertEquals(new BigDecimal(13), beans.get(2), "pk");
    } else {
      assertEquals(10, beans.get(0), "pk");
      assertEquals(12, beans.get(1), "pk");
      assertEquals(13, beans.get(2), "pk");
    }
    assertEquals(1, beans.get(0), "avalue");
    assertEquals(2, beans.get(1), "avalue");
    assertEquals(3, beans.get(2), "avalue");

    beans = getRows("misc2");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(1), "pk");
    if (isSybase) {
      assertEquals(new BigDecimal(10), beans.get(0), "fk");
      assertEquals(new BigDecimal(13), beans.get(1), "fk");
    } else {
      assertEquals(10, beans.get(0), "fk");
      assertEquals(13, beans.get(1), "fk");
    }
  }

  /**
   * Tests the backup and restore of a table with an identity column and a foreign key to
   * it when identity override is turned off.
   */
  @Test
  public void testIdentityOverrideOff() throws Exception {
    if (!getPlatformInfo().isIdentityOverrideAllowed()) {
      // TODO: for testing these platforms, we need deleteRows
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc1'>\n" +
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='avalue' type='INTEGER' required='false'/>\n" +
          "  </table>\n" +
          "  <table name='misc2'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
          "    <column name='fk' type='NUMERIC' size='12,0' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc1'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc1'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='avalue' type='INTEGER' required='false'/>\n" +
          "  </table>\n" +
          "  <table name='misc2'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
          "    <column name='fk' type='INTEGER' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc1'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    if (isSybase) {
      insertRow("misc1", new Object[]{new BigDecimal(10), 1});
      insertRow("misc1", new Object[]{new BigDecimal(12), 2});
      insertRow("misc1", new Object[]{new BigDecimal(13), 3});
      insertRow("misc2", new Object[]{1, new BigDecimal(10)});
      insertRow("misc2", new Object[]{2, new BigDecimal(13)});
    } else {
      insertRow("misc1", new Object[]{10, 1});
      insertRow("misc1", new Object[]{12, 2});
      insertRow("misc1", new Object[]{13, 3});
      insertRow("misc2", new Object[]{1, 10});
      insertRow("misc2", new Object[]{2, 13});
    }

    StringWriter stringWriter = new StringWriter();
    DatabaseDataIO dataIO = new DatabaseDataIO();

    dataIO.writeDataToXML(getPlatform(), getModel(), stringWriter, "UTF-8");

    String dataAsXml = stringWriter.toString();
    SAXReader reader = new SAXReader();
    Document testDoc = reader.read(new InputSource(new StringReader(dataAsXml)));

    List<Node> misc1Rows = testDoc.selectNodes("//misc1");
    List<Node> misc2Rows = testDoc.selectNodes("//misc2");
    String pkColumnName = "pk";
    String fkColumnName = "fk";
    String valueColumnName = "avalue";

    if (misc1Rows.isEmpty()) {
      misc1Rows = testDoc.selectNodes("//MISC1");
      misc2Rows = testDoc.selectNodes("//MISC2");
      pkColumnName = pkColumnName.toUpperCase();
      fkColumnName = fkColumnName.toUpperCase();
      valueColumnName = valueColumnName.toUpperCase();
    }

    Assert.assertEquals(3, misc1Rows.size());
    Assert.assertEquals("10", ((Element) misc1Rows.get(0)).attributeValue(pkColumnName));
    Assert.assertEquals("1", ((Element) misc1Rows.get(0)).attributeValue(valueColumnName));
    Assert.assertEquals("12", ((Element) misc1Rows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("2", ((Element) misc1Rows.get(1)).attributeValue(valueColumnName));
    Assert.assertEquals("13", ((Element) misc1Rows.get(2)).attributeValue(pkColumnName));
    Assert.assertEquals("3", ((Element) misc1Rows.get(2)).attributeValue(valueColumnName));
    Assert.assertEquals(2, misc2Rows.size());
    Assert.assertEquals("1", ((Element) misc2Rows.get(0)).attributeValue(pkColumnName));
    Assert.assertEquals("10", ((Element) misc2Rows.get(0)).attributeValue(fkColumnName));
    Assert.assertEquals("2", ((Element) misc2Rows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("13", ((Element) misc2Rows.get(1)).attributeValue(fkColumnName));

    dropDatabase();
    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(false);

    StringReader stringReader = new StringReader(dataAsXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc1");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "pk");
      assertEquals(new BigDecimal(2), beans.get(1), "pk");
      assertEquals(new BigDecimal(3), beans.get(2), "pk");
    } else {
      assertEquals(1, beans.get(0), "pk");
      assertEquals(2, beans.get(1), "pk");
      assertEquals(3, beans.get(2), "pk");
    }
    assertEquals(1, beans.get(0), "avalue");
    assertEquals(2, beans.get(1), "avalue");
    assertEquals(3, beans.get(2), "avalue");

    beans = getRows("misc2");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(1), "pk");
    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "fk");
      assertEquals(new BigDecimal(3), beans.get(1), "fk");
    } else {
      assertEquals(1, beans.get(0), "fk");
      assertEquals(3, beans.get(1), "fk");
    }
  }

  /**
   * Tests the backup and restore of a table with an identity column and a foreign key to
   * itself while identity override is off.
   */
  @Test
  public void testSelfReferenceIdentityOverrideOff() throws Exception {
    // Hsqldb does not allow rows to reference themselves
    if (BuiltinDriverType.HSQL.getName().equals(getPlatform().getName())) {
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='fk' type='NUMERIC' size='12,0' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='fk' type='INTEGER' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(false);

    if (isSybase) {
      insertRow("misc", new Object[]{new BigDecimal(1), null});
      insertRow("misc", new Object[]{new BigDecimal(2), new BigDecimal(1)});
      insertRow("misc", new Object[]{new BigDecimal(3), new BigDecimal(2)});
      insertRow("misc", new Object[]{new BigDecimal(4), new BigDecimal(4)});
    } else {
      insertRow("misc", new Object[]{1, null});
      insertRow("misc", new Object[]{2, 1});
      insertRow("misc", new Object[]{3, 2});
      insertRow("misc", new Object[]{4, 4});
    }

    StringWriter stringWriter = new StringWriter();
    DatabaseDataIO dataIO = new DatabaseDataIO();

    dataIO.writeDataToXML(getPlatform(), getModel(), stringWriter, "UTF-8");

    String dataAsXml = stringWriter.toString();
    SAXReader reader = new SAXReader();
    Document testDoc = reader.read(new InputSource(new StringReader(dataAsXml)));

    List<Node> miscRows = testDoc.selectNodes("//misc");
    String pkColumnName = "pk";
    String fkColumnName = "fk";

    if (miscRows.isEmpty()) {
      miscRows = testDoc.selectNodes("//MISC");
      pkColumnName = pkColumnName.toUpperCase();
      fkColumnName = fkColumnName.toUpperCase();
    }

    Assert.assertEquals(4, miscRows.size());
    Assert.assertEquals("1", ((Element) miscRows.get(0)).attributeValue(pkColumnName));
    Assert.assertNull(((Element) miscRows.get(0)).attributeValue(fkColumnName));
    Assert.assertEquals("2", ((Element) miscRows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("1", ((Element) miscRows.get(1)).attributeValue(fkColumnName));
    Assert.assertEquals("3", ((Element) miscRows.get(2)).attributeValue(pkColumnName));
    Assert.assertEquals("2", ((Element) miscRows.get(2)).attributeValue(fkColumnName));
    Assert.assertEquals("4", ((Element) miscRows.get(3)).attributeValue(pkColumnName));
    Assert.assertEquals("4", ((Element) miscRows.get(3)).attributeValue(fkColumnName));

    dropDatabase();
    createDatabase(modelXml);

    StringReader stringReader = new StringReader(dataAsXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "pk");
      Assert.assertNull(beans.get(0).get("fk"));
      assertEquals(new BigDecimal(2), beans.get(1), "pk");
      assertEquals(new BigDecimal(1), beans.get(1), "fk");
      assertEquals(new BigDecimal(3), beans.get(2), "pk");
      assertEquals(new BigDecimal(2), beans.get(2), "fk");
      assertEquals(new BigDecimal(4), beans.get(3), "pk");
      assertEquals(new BigDecimal(4), beans.get(3), "fk");
    } else {
      assertEquals(1, beans.get(0), "pk");
      Assert.assertNull(beans.get(0).get("fk"));
      assertEquals(2, beans.get(1), "pk");
      assertEquals(1, beans.get(1), "fk");
      assertEquals(3, beans.get(2), "pk");
      assertEquals(2, beans.get(2), "fk");
      assertEquals(4, beans.get(3), "pk");
      assertEquals(4, beans.get(3), "fk");
    }
  }

  /**
   * Tests the backup and restore of a table with an identity column and a foreign key to
   * itself while identity override is off.
   */
  @Test
  public void testSelfReferenceIdentityOverrideOn() throws Exception {
    if (!getPlatformInfo().isIdentityOverrideAllowed()) {
      // TODO: for testing these platforms, we need deleteRows
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='fk' type='NUMERIC' size='12,0' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
          "    <column name='fk' type='INTEGER' required='false'/>\n" +
          "    <foreign-key name='test' foreignTable='misc'>\n" +
          "      <reference local='fk' foreign='pk'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    if (isSybase) {
      insertRow("misc", new Object[]{new BigDecimal(10), null});
      insertRow("misc", new Object[]{new BigDecimal(11), new BigDecimal(10)});
      insertRow("misc", new Object[]{new BigDecimal(12), new BigDecimal(11)});
      insertRow("misc", new Object[]{new BigDecimal(13), new BigDecimal(13)});
    } else {
      insertRow("misc", new Object[]{10, null});
      insertRow("misc", new Object[]{11, 10});
      insertRow("misc", new Object[]{12, 11});
      insertRow("misc", new Object[]{13, 13});
    }

    StringWriter stringWriter = new StringWriter();
    DatabaseDataIO dataIO = new DatabaseDataIO();

    dataIO.writeDataToXML(getPlatform(), getModel(), stringWriter, "UTF-8");

    String dataAsXml = stringWriter.toString();
    SAXReader reader = new SAXReader();
    Document testDoc = reader.read(new InputSource(new StringReader(dataAsXml)));

    List<Node> miscRows = testDoc.selectNodes("//misc");
    String pkColumnName = "pk";
    String fkColumnName = "fk";

    if (miscRows.isEmpty()) {
      miscRows = testDoc.selectNodes("//MISC");
      pkColumnName = pkColumnName.toUpperCase();
      fkColumnName = fkColumnName.toUpperCase();
    }

    Assert.assertEquals(4, miscRows.size());
    Assert.assertEquals("10", ((Element) miscRows.get(0)).attributeValue(pkColumnName));
    Assert.assertNull(((Element) miscRows.get(0)).attributeValue(fkColumnName));
    Assert.assertEquals("11", ((Element) miscRows.get(1)).attributeValue(pkColumnName));
    Assert.assertEquals("10", ((Element) miscRows.get(1)).attributeValue(fkColumnName));
    Assert.assertEquals("12", ((Element) miscRows.get(2)).attributeValue(pkColumnName));
    Assert.assertEquals("11", ((Element) miscRows.get(2)).attributeValue(fkColumnName));
    Assert.assertEquals("13", ((Element) miscRows.get(3)).attributeValue(pkColumnName));
    Assert.assertEquals("13", ((Element) miscRows.get(3)).attributeValue(fkColumnName));

    dropDatabase();
    createDatabase(modelXml);

    StringReader stringReader = new StringReader(dataAsXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc");

    if (isSybase) {
      assertEquals(new BigDecimal(10), beans.get(0), "pk");
      Assert.assertNull(beans.get(0).get("fk"));
      assertEquals(new BigDecimal(11), beans.get(1), "pk");
      assertEquals(new BigDecimal(10), beans.get(1), "fk");
      assertEquals(new BigDecimal(12), beans.get(2), "pk");
      assertEquals(new BigDecimal(11), beans.get(2), "fk");
      assertEquals(new BigDecimal(13), beans.get(3), "pk");
      assertEquals(new BigDecimal(13), beans.get(3), "fk");
    } else {
      assertEquals(10, beans.get(0), "pk");
      Assert.assertNull(beans.get(0).get("fk"));
      assertEquals(11, beans.get(1), "pk");
      assertEquals(10, beans.get(1), "fk");
      assertEquals(12, beans.get(2), "pk");
      assertEquals(11, beans.get(2), "fk");
      assertEquals(13, beans.get(3), "pk");
      assertEquals(13, beans.get(3), "fk");
    }
  }

  /**
   * Tests the backup and restore of a self-referencing data set.
   */
  @Test
  public void testSelfReferences() throws Exception {
    if (!getPlatformInfo().isIdentityOverrideAllowed()) {
      // TODO: for testing these platforms, we need deleteRows
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='id' primaryKey='true' required='true' type='NUMERIC' size='10,0' autoIncrement='true'/>\n" +
          "    <column name='parent_id' primaryKey='false' required='false' type='NUMERIC' size='10,0' autoIncrement='false'/>\n" +
          "    <foreign-key foreignTable='misc' name='misc_parent_fk'>\n" +
          "      <reference local='parent_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='id' primaryKey='true' required='true' type='SMALLINT' size='2' autoIncrement='true'/>\n" +
          "    <column name='parent_id' primaryKey='false' required='false' type='SMALLINT' size='2' autoIncrement='false'/>\n" +
          "    <foreign-key foreignTable='misc' name='misc_parent_fk'>\n" +
          "      <reference local='parent_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    final String dataXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <misc id='4' parent_id='1'/>\n" +
        "  <misc id='7' parent_id='1'/>\n" +
        "  <misc id='3' parent_id='2'/>\n" +
        "  <misc id='5' parent_id='3'/>\n" +
        "  <misc id='8' parent_id='7'/>\n" +
        "  <misc id='9' parent_id='6'/>\n" +
        "  <misc id='10' parent_id='4'/>\n" +
        "  <misc id='1'/>\n" +
        "  <misc id='2'/>\n" +
        "  <misc id='6'/>\n" +
        "  <misc id='11'/>\n" +
        "  <misc id='12' parent_id='11'/>\n" +
        "</data>";

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    DatabaseDataIO dataIO = new DatabaseDataIO();
    StringReader stringReader = new StringReader(dataXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc", "id");

    if (isSybase) {
      Assert.assertEquals(12, beans.size());
      assertEquals(new BigDecimal(1), beans.get(0), "id");
      Assert.assertNull(beans.get(0).get("parent_id"));
      assertEquals(new BigDecimal(2), beans.get(1), "id");
      Assert.assertNull(beans.get(1).get("parent_id"));
      assertEquals(new BigDecimal(3), beans.get(2), "id");
      assertEquals(new BigDecimal(2), beans.get(2), "parent_id");
      assertEquals(new BigDecimal(4), beans.get(3), "id");
      assertEquals(new BigDecimal(1), beans.get(3), "parent_id");
      assertEquals(new BigDecimal(5), beans.get(4), "id");
      assertEquals(new BigDecimal(3), beans.get(4), "parent_id");
      assertEquals(new BigDecimal(6), beans.get(5), "id");
      Assert.assertNull(beans.get(5).get("parent_id"));
      assertEquals(new BigDecimal(7), beans.get(6), "id");
      assertEquals(new BigDecimal(1), beans.get(6), "parent_id");
      assertEquals(new BigDecimal(8), beans.get(7), "id");
      assertEquals(new BigDecimal(7), beans.get(7), "parent_id");
      assertEquals(new BigDecimal(9), beans.get(8), "id");
      assertEquals(new BigDecimal(6), beans.get(8), "parent_id");
      assertEquals(new BigDecimal(10), beans.get(9), "id");
      assertEquals(new BigDecimal(4), beans.get(9), "parent_id");
      assertEquals(new BigDecimal(11), beans.get(10), "id");
      Assert.assertNull(beans.get(10).get("parent_id"));
      assertEquals(new BigDecimal(12), beans.get(11), "id");
      assertEquals(new BigDecimal(11), beans.get(11), "parent_id");
    } else {
      Assert.assertEquals(12, beans.size());
      assertEquals(1, beans.get(0), "id");
      Assert.assertNull(beans.get(0).get("parent_id"));
      assertEquals(2, beans.get(1), "id");
      Assert.assertNull(beans.get(1).get("parent_id"));
      assertEquals(3, beans.get(2), "id");
      assertEquals(2, beans.get(2), "parent_id");
      assertEquals(4, beans.get(3), "id");
      assertEquals(1, beans.get(3), "parent_id");
      assertEquals(5, beans.get(4), "id");
      assertEquals(3, beans.get(4), "parent_id");
      assertEquals(6, beans.get(5), "id");
      Assert.assertNull(beans.get(5).get("parent_id"));
      assertEquals(7, beans.get(6), "id");
      assertEquals(1, beans.get(6), "parent_id");
      assertEquals(8, beans.get(7), "id");
      assertEquals(7, beans.get(7), "parent_id");
      assertEquals(9, beans.get(8), "id");
      assertEquals(6, beans.get(8), "parent_id");
      assertEquals(10, beans.get(9), "id");
      assertEquals(4, beans.get(9), "parent_id");
      assertEquals(11, beans.get(10), "id");
      Assert.assertNull(beans.get(10).get("parent_id"));
      assertEquals(12, beans.get(11), "id");
      assertEquals(11, beans.get(11), "parent_id");
    }
  }

  /**
   * Tests the backup and restore of a self-referencing data set (with multiple self references
   * in the same table).
   */
  @Test
  public void testMultiSelfReferences() throws Exception {
    if (!getPlatformInfo().isIdentityOverrideAllowed()) {
      // TODO: for testing these platforms, we need deleteRows
      return;
    }

    // Sybase does not like INTEGER auto-increment columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String modelXml;

    if (isSybase) {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='id' primaryKey='true' required='true' type='NUMERIC' size='6,0' autoIncrement='true'/>\n" +
          "    <column name='left_id' primaryKey='false' required='false' type='NUMERIC' size='6,0' autoIncrement='false'/>\n" +
          "    <column name='right_id' primaryKey='false' required='false' type='NUMERIC' size='6,0' autoIncrement='false'/>\n" +
          "    <foreign-key foreignTable='misc' name='misc_left_fk'>\n" +
          "      <reference local='left_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "    <foreign-key foreignTable='misc' name='misc_right_fk'>\n" +
          "      <reference local='right_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    } else {
      modelXml =
        "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
          "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
          "  <table name='misc'>\n" +
          "    <column name='id' primaryKey='true' required='true' type='SMALLINT' size='2' autoIncrement='true'/>\n" +
          "    <column name='left_id' primaryKey='false' required='false' type='SMALLINT' size='2' autoIncrement='false'/>\n" +
          "    <column name='right_id' primaryKey='false' required='false' type='SMALLINT' size='2' autoIncrement='false'/>\n" +
          "    <foreign-key foreignTable='misc' name='misc_left_fk'>\n" +
          "      <reference local='left_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "    <foreign-key foreignTable='misc' name='misc_right_fk'>\n" +
          "      <reference local='right_id' foreign='id'/>\n" +
          "    </foreign-key>\n" +
          "  </table>\n" +
          "</database>";
    }

    final String dataXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<data>\n" +
        "  <misc id='1' left_id='2' right_id='3'/>\n" +
        "  <misc id='3' left_id='2' right_id='4'/>\n" +
        "  <misc id='2' left_id='5' right_id='4'/>\n" +
        "  <misc id='5' right_id='6'/>\n" +
        "  <misc id='6'/>\n" +
        "  <misc id='4' left_id='6'/>\n" +
        "</data>";

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    DatabaseDataIO dataIO = new DatabaseDataIO();
    StringReader stringReader = new StringReader(dataXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    List<RowObject> beans = getRows("misc", "id");

    Assert.assertEquals(6, beans.size());
    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "id");
      assertEquals(new BigDecimal(2), beans.get(0), "left_id");
      assertEquals(new BigDecimal(3), beans.get(0), "right_id");
      assertEquals(new BigDecimal(2), beans.get(1), "id");
      assertEquals(new BigDecimal(5), beans.get(1), "left_id");
      assertEquals(new BigDecimal(4), beans.get(1), "right_id");
      assertEquals(new BigDecimal(3), beans.get(2), "id");
      assertEquals(new BigDecimal(2), beans.get(2), "left_id");
      assertEquals(new BigDecimal(4), beans.get(2), "right_id");
      assertEquals(new BigDecimal(4), beans.get(3), "id");
      assertEquals(new BigDecimal(6), beans.get(3), "left_id");
      assertEquals(null, beans.get(3), "right_id");
      assertEquals(new BigDecimal(5), beans.get(4), "id");
      assertEquals(null, beans.get(4), "left_id");
      assertEquals(new BigDecimal(6), beans.get(4), "right_id");
      assertEquals(new BigDecimal(6), beans.get(5), "id");
      assertEquals(null, beans.get(5), "left_id");
      assertEquals(null, beans.get(5), "right_id");
    } else {
      assertEquals(1, beans.get(0), "id");
      assertEquals(2, beans.get(0), "left_id");
      assertEquals(3, beans.get(0), "right_id");
      assertEquals(2, beans.get(1), "id");
      assertEquals(5, beans.get(1), "left_id");
      assertEquals(4, beans.get(1), "right_id");
      assertEquals(3, beans.get(2), "id");
      assertEquals(2, beans.get(2), "left_id");
      assertEquals(4, beans.get(2), "right_id");
      assertEquals(4, beans.get(3), "id");
      assertEquals(6, beans.get(3), "left_id");
      assertEquals(null, beans.get(3), "right_id");
      assertEquals(5, beans.get(4), "id");
      assertEquals(null, beans.get(4), "left_id");
      assertEquals(6, beans.get(4), "right_id");
      assertEquals(6, beans.get(5), "id");
      assertEquals(null, beans.get(5), "left_id");
      assertEquals(null, beans.get(5), "right_id");
    }
  }

  /**
   * Tests the backup and restore of several tables with complex relationships with an identity column and a foreign key to
   * itself while identity override is off.
   */
  @Test
  public void testComplexTableModel() throws Exception {
    // A: self-reference (A1->A2)
    // B: self- and foreign-reference (B1->B2|G1, B2->G2)
    // C: circular reference involving more than one table (C1->D1,C2->D2)
    // D: foreign-reference to F (D1->F1,D2)
    // E: isolated table (E1)
    // F: foreign-reference to C (F1->C2)
    // G: no references (G1, G2)

    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='A'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='AtoA' foreignTable='A'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='B'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk1' type='INTEGER' required='false'/>\n" +
        "    <column name='fk2' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='BtoB' foreignTable='B'>\n" +
        "      <reference local='fk1' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "    <foreign-key name='BtoG' foreignTable='G'>\n" +
        "      <reference local='fk2' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='C'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='CtoD' foreignTable='D'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='D'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='DtoF' foreignTable='F'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='E'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='F'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='FtoC' foreignTable='C'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='G'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    getPlatform().setIdentityOverrideOn(true);

    // this is the optimal insertion order
    insertRow("E", new Object[]{1});
    insertRow("G", new Object[]{1});
    insertRow("G", new Object[]{2});
    insertRow("A", new Object[]{2, null});
    insertRow("A", new Object[]{1, 2});
    insertRow("B", new Object[]{2, null, 2});
    insertRow("B", new Object[]{1, 2, 1});
    insertRow("D", new Object[]{2, null});
    insertRow("C", new Object[]{2, 2});
    insertRow("F", new Object[]{1, 2});
    insertRow("D", new Object[]{1, 1});
    insertRow("C", new Object[]{1, 1});

    StringWriter stringWriter = new StringWriter();
    DatabaseDataIO dataIO = new DatabaseDataIO();

    dataIO.writeDataToXML(getPlatform(), getModel(), stringWriter, "UTF-8");

    String dataAsXml = stringWriter.toString();

    // the somewhat optimized order that DdlUtils currently generates is:
    // E1, G1, G2, A2, A1, B2, B1, C2, C1, D2, D1, F1
    // note that the order per table is the insertion order above
    SAXReader reader = new SAXReader();
    Document testDoc = reader.read(new InputSource(new StringReader(dataAsXml)));
    boolean uppercase = false;
    List<Node> rows = testDoc.selectNodes("/*/*");
    String pkColumnName = "pk";

    Assert.assertEquals(12, rows.size());
    if (!"e".equals(rows.get(0).getName())) {
      Assert.assertEquals("E", rows.get(0).getName());
      uppercase = true;
    }
    if (!"pk".equals(((Element) rows.get(0)).attribute(0).getName())) {
      pkColumnName = pkColumnName.toUpperCase();
    }
    Assert.assertEquals("1", ((Element) rows.get(0)).attributeValue(pkColumnName));

    // we cannot be sure of the order in which the database returns the rows
    // per table (some return them in pk order, some in insertion order)
    // so we don't assume an order in this test
    HashSet<String> pkValues = new HashSet<>();
    HashSet<String> expectedValues = new HashSet<>(Arrays.asList("1", "2"));

    Assert.assertEquals(uppercase ? "G" : "g", rows.get(1).getName());
    Assert.assertEquals(uppercase ? "G" : "g", rows.get(2).getName());
    pkValues.add(((Element) rows.get(1)).attributeValue(pkColumnName));
    pkValues.add(((Element) rows.get(2)).attributeValue(pkColumnName));
    Assert.assertEquals(pkValues, expectedValues);

    pkValues.clear();

    Assert.assertEquals(uppercase ? "A" : "a", rows.get(3).getName());
    Assert.assertEquals(uppercase ? "A" : "a", rows.get(4).getName());
    pkValues.add(((Element) rows.get(3)).attributeValue(pkColumnName));
    pkValues.add(((Element) rows.get(4)).attributeValue(pkColumnName));
    Assert.assertEquals(pkValues, expectedValues);

    pkValues.clear();

    Assert.assertEquals(uppercase ? "B" : "b", rows.get(5).getName());
    Assert.assertEquals(uppercase ? "B" : "b", rows.get(6).getName());
    pkValues.add(((Element) rows.get(5)).attributeValue(pkColumnName));
    pkValues.add(((Element) rows.get(6)).attributeValue(pkColumnName));
    Assert.assertEquals(pkValues, expectedValues);

    pkValues.clear();

    Assert.assertEquals(uppercase ? "C" : "c", rows.get(7).getName());
    Assert.assertEquals(uppercase ? "C" : "c", rows.get(8).getName());
    pkValues.add(((Element) rows.get(7)).attributeValue(pkColumnName));
    pkValues.add(((Element) rows.get(8)).attributeValue(pkColumnName));
    Assert.assertEquals(pkValues, expectedValues);

    pkValues.clear();

    Assert.assertEquals(uppercase ? "D" : "d", rows.get(9).getName());
    Assert.assertEquals(uppercase ? "D" : "d", rows.get(10).getName());
    pkValues.add(((Element) rows.get(9)).attributeValue(pkColumnName));
    pkValues.add(((Element) rows.get(10)).attributeValue(pkColumnName));
    Assert.assertEquals(pkValues, expectedValues);

    pkValues.clear();

    Assert.assertEquals(uppercase ? "F" : "f", rows.get(11).getName());
    Assert.assertEquals("1", ((Element) rows.get(11)).attributeValue(pkColumnName));

    dropDatabase();
    createDatabase(modelXml);

    StringReader stringReader = new StringReader(dataAsXml);

    dataIO.writeDataToDatabase(getPlatform(), getModel(), new Reader[]{stringReader});

    Assert.assertEquals(2, getRows("A").size());
    Assert.assertEquals(2, getRows("B").size());
    Assert.assertEquals(2, getRows("C").size());
    Assert.assertEquals(2, getRows("D").size());
    Assert.assertEquals(1, getRows("E").size());
    Assert.assertEquals(1, getRows("F").size());
    Assert.assertEquals(2, getRows("G").size());
  }

  /**
   * Test for DDLUTILS-178.
   */
  @Test
  public void testDdlUtils178() throws Exception {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "  <database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "    <table name='ad_sequence_no'>\n" +
        "      <column name='ad_sequence_id' required='true' type='NUMERIC' size='10'/>\n" +
        "      <column name='ad_year' required='true' type='VARCHAR' size='4' default='0000'/>\n" +
        "      <column name='ad_client_id' required='true' type='NUMERIC' size='10'/>\n" +
        "      <unique name='ad_sequence_no_key'>\n" +
        "        <unique-column name='ad_sequence_id'/>\n" +
        "        <unique-column name='ad_year'/>\n" +
        "      </unique>\n" +
        "    </table>\n" +
        "</database>";

    createDatabase(modelXml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }

  /**
   * Test for DDLUTILS-179.
   */
  @Test
  public void testDdlUtils179() throws Exception {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='A'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='AtoA' foreignTable='A'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='B'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk1' type='INTEGER' required='false'/>\n" +
        "    <column name='fk2' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='BtoB' foreignTable='B'>\n" +
        "      <reference local='fk1' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "    <foreign-key name='BtoG' foreignTable='G'>\n" +
        "      <reference local='fk2' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='C'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='CtoD' foreignTable='D'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='D'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='DtoF' foreignTable='F'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='E'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='F'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key name='FtoC' foreignTable='C'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='G'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    Database readModel = readModelFromDatabase("roundtriptest");

    assertEquals(getAdjustedModel(),
      readModel);

    File tmpFile = File.createTempFile("model", ".xml");

    try {
      DatabaseIO dbIO = new DatabaseIO();
      FileOutputStream out = new FileOutputStream(tmpFile);

      dbIO.write(readModel, out);
      out.flush();
      out.close();

      assertEquals(getAdjustedModel(),
        dbIO.read(tmpFile));
    } finally {
      if (!tmpFile.delete()) {
        getLog().warn("Could not delete the temporary file " + tmpFile.getAbsolutePath());
      }
    }
  }

  /**
   * Test for DDLUTILS-214.
   */
  @Test
  public void testDdlUtils214() throws Exception {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk2' type='VARCHAR' primaryKey='true' required='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    Database readModel = readModelFromDatabase("roundtriptest");

    assertEquals(getAdjustedModel(),
      readModel);

    insertRow("roundtrip1", new Object[]{1, "foo"});
    insertRow("roundtrip1", new Object[]{2, "bar"});
    insertRow("roundtrip2", new Object[]{"foo", 1});
    insertRow("roundtrip2", new Object[]{"bar", 2});

    List<RowObject> beans1 = getRows("roundtrip1", "pk1");
    List<RowObject> beans2 = getRows("roundtrip2", "pk1");

    Assert.assertEquals(2, beans1.size());
    Assert.assertEquals(2, beans2.size());
    assertEquals(1, beans1.get(0), "pk1");
    assertEquals("foo", beans1.get(0), "pk2");
    assertEquals(2, beans1.get(1), "pk1");
    assertEquals("bar", beans1.get(1), "pk2");
    assertEquals(1, beans2.get(0), "pk1");
    assertEquals("foo", beans2.get(0), "pk2");
    assertEquals(2, beans2.get(1), "pk1");
    assertEquals("bar", beans2.get(1), "pk2");

    deleteRow("roundtrip1", new Object[]{1, "foo"});
    deleteRow("roundtrip2", new Object[]{"foo", 1});

    beans1 = getRows("roundtrip1", "pk1");
    beans2 = getRows("roundtrip2", "pk1");

    Assert.assertEquals(1, beans1.size());
    Assert.assertEquals(1, beans2.size());
    assertEquals(2, beans1.get(0), "pk1");
    assertEquals("bar", beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk1");
    assertEquals("bar", beans2.get(0), "pk2");
  }

  /**
   * Test for DDLUTILS-227.
   */
  @Test
  public void testDdlUtils227() throws Exception {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='Roundtrip'>\n" +
        "    <column name='Pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='Avalue' type='VARCHAR'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    Database readModel = readModelFromDatabase("roundtriptest");

    assertEquals(getAdjustedModel(),
      readModel);

    insertRow("Roundtrip", new Object[]{1, "foo"});

    List<RowObject> beans = getRows("Roundtrip");

    Assert.assertEquals(1, beans.size());
    assertEquals(1, beans.get(0), "Pk");
    assertEquals("foo", beans.get(0), "Avalue");

    Table table = getModel().findTable("Roundtrip", getPlatform().isDelimitedIdentifierModeOn());
    StringBuilder query = new StringBuilder();

    query.append("SELECT * FROM (SELECT * FROM ");
    if (getPlatform().isDelimitedIdentifierModeOn()) {
      query.append(getPlatformInfo().getDelimiterToken());
    }
    query.append(table.getName());
    if (getPlatform().isDelimitedIdentifierModeOn()) {
      query.append(getPlatformInfo().getDelimiterToken());
    }
    query.append(")");
    // Some JDBC drivers do not allow us to perform the query without an explicit alias
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.DERBY.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.POSTGRE_SQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.SQLSERVER.getName().equals(getPlatform().getName())) {
      query.append(" AS ");
      if (getPlatform().isDelimitedIdentifierModeOn()) {
        query.append(getPlatformInfo().getDelimiterToken());
      }
      query.append(table.getName());
      if (getPlatform().isDelimitedIdentifierModeOn()) {
        query.append(getPlatformInfo().getDelimiterToken());
      }
    }

    beans = getPlatform().fetch(getModel(), query.toString(), new Table[]{table});

    Assert.assertEquals(1, beans.size());
    assertEquals(1, beans.get(0), "Pk");
    assertEquals("foo", beans.get(0), "Avalue");
  }
}
