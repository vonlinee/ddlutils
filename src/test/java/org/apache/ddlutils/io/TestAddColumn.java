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
import org.apache.ddlutils.data.DynaBean;
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests database alterations that add columns.
 *
 * @version $Revision: $
 */
public class TestAddColumn extends TestAgainstLiveDatabaseBase {

  /**
   * Tests the addition of a column.
   */
  @Test
  public void testAddColumn() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the addition of an auto-increment column.
   */
  @Test
  public void testAddAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    // we need special catering for Sybase which does not support identity for INTEGER columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml;

    if (isSybase) {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "avalue");
    } else {
      Object avalue = beans.get(0).get("avalue");

      Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
    }
  }

  /**
   * Tests the addition of another auto-increment column.
   */
  public void testAddSecondAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isMultipleIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
      "  <table name='roundtrip'>\n" +
      "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='avalue1' type='INTEGER' autoIncrement='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
      "  <table name='roundtrip'>\n" +
      "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='avalue1' type='INTEGER' autoIncrement='true'/>\n" +
      "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
      "  </table>\n" +
      "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue1 = beans.get(0).get("avalue1");
    Object avalue2 = beans.get(0).get("avalue2");

    Assert.assertTrue((avalue1 == null) || new Integer(1).equals(avalue1));
    Assert.assertTrue((avalue2 == null) || new Integer(1).equals(avalue2));
  }

  /**
   * Tests the addition of a column that is set to NOT NULL.
   */
  public void testAddRequiredColumn() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' default='2' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(new BigDecimal(2), beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a column with a default value.
   */
  public void testAddColumnWithDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Double(2).equals(avalue));
  }

  /**
   * Tests the addition of a required auto-increment column.
   */
  public void testAddRequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    // we need special catering for Sybase which does not support identity for INTEGER columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml;

    if (isSybase) {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "avalue");
    } else {
      Object avalue = beans.get(0).get("avalue");

      Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
    }
  }

  /**
   * Tests the addition of a column with a default value.
   */
  public void testAddRequiredColumnWithDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='CHAR' size='8' default='sometext' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || "sometext".equals(avalue));
  }

  /**
   * Tests the addition of several columns at the end of the table.
   */
  public void testAddMultipleColumns() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n" +
        "    <column name='avalue4' type='VARCHAR' size='16'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "test", 3});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue3 = beans.get(0).get("avalue3");

    assertEquals("test", beans.get(0), "avalue1");
    assertEquals(3, beans.get(0), "avalue2");
    Assert.assertTrue((avalue3 == null) || new Double(1.0).equals(avalue3));
    assertEquals(null, beans.get(0), "avalue4");
  }

  /**
   * Tests the addition of a primary key and a column.
   */
  public void testAddPKAndColumn() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      // Mckoi uses null to initialize the new pk column
      assertEquals(null, beans.get(0), "pk");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of a primary key and an autoincrement column.
   */
  public void testAddPKAndAutoIncrementColumn() {
    // we need special catering for Sybase which does not support identity for INTEGER columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml;

    if (isSybase) {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "avalue");
    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "pk");
    } else {
      assertEquals(1, beans.get(0), "pk");
    }
  }

  /**
   * Tests the addition of a primary key and multiple columns.
   */
  public void testAddPKAndMultipleColumns() {
    if (getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' default='2'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(null, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(2.0, beans.get(0), "pk3");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of a primary key and multiple required columns.
   */
  public void testAddPKAndMultipleRequiredColumns() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true' default='2'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(null, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(2.0, beans.get(0), "pk3");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of a primary key and multiple columns.
   */
  public void testAddPKAndMultipleColumnsInclAutoIncrement() {
    if (!getPlatformInfo().isMixingIdentityAndNormalPrimaryKeyColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(1, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(null, beans.get(0), "pk3");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of a column to a primary key.
   */
  public void testAddColumnIntoPK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(1, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(2, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of an autoincrement column into the primary key.
   */
  public void testAddAutoIncrementColumnIntoPK() {
    if (!getPlatformInfo().isMixingIdentityAndNormalPrimaryKeyColumnsSupported()) {
      return;
    }

    // we need special catering for Sybase which does not support identity for INTEGER columns
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml;

    if (isSybase) {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{-1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertEquals(-1, beans.get(0).get("pk1"));
    Assert.assertEquals(2, beans.get(0).get("avalue"));
    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "pk2");
    } else {
      assertEquals(1, beans.get(0), "pk2");
    }
  }

  /**
   * Tests the addition of multiple columns into the primary key.
   */
  public void testAddMultipleColumnsIntoPK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(1, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(null, beans.get(0), "pk3");
      assertEquals(null, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of multiple columns into the primary key which has an auto increment column.
   */
  public void testAddMultipleColumnsIntoPKWithAutoIncrement() {
    if (!getPlatformInfo().isMixingIdentityAndNormalPrimaryKeyColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{null, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(1, beans.get(0), "pk1");
      assertEquals(null, beans.get(0), "pk2");
      assertEquals(null, beans.get(0), "pk3");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of multiple columns including one with auto increment into the primary key.
   */
  public void testAddMultipleColumnsInclAutoIncrementIntoPK() {
    if (!getPlatformInfo().isMixingIdentityAndNormalPrimaryKeyColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    if (BuiltinDriverType.MCKOI.getName().equals(getPlatform().getName())) {
      assertEquals(1, beans.get(0), "pk1");
      assertEquals("text", beans.get(0), "pk2");
      assertEquals(null, beans.get(0), "pk3");
      assertEquals(1, beans.get(0), "avalue");
    } else {
      Assert.assertTrue(beans.isEmpty());
    }
  }

  /**
   * Tests the addition of a non-unique index and a column.
   */
  public void testAddNonUniqueIndexAndColumn() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a non-unique index and an auto increment column.
   */
  public void testAddNonUniqueIndexAndAutoIncrementColumn() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
  }

  /**
   * Tests the addition of a non-unique index and a required column.
   */
  public void testAddNonUniqueIndexAndRequiredColumn() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a non-unique index and a column with a default value.
   */
  public void testAddNonUniqueIndexAndColumnWithDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Double(2).equals(avalue));
  }

  /**
   * Tests the addition of a non-unique index and a required auto increment column.
   */
  public void testAddNonUniqueIndexAndRequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a non-unique index and a required column with a default value.
   */
  public void testAddNonUniqueIndexAndRequiredColumnWithDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='CHAR' size='8' required='true' default='sometext'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals("sometext", beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a non-unique index and several columns.
   */
  public void testAddNonUniqueIndexAndMultipleColumns() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }


  /**
   * Tests the addition of a unique index and a column.
   */
  public void testAddUniqueIndexAndColumn() {
    // TODO
    if (!getPlatformInfo().isIndicesSupported() ||
      BuiltinDriverType.INTERBASE.getName().equals(getPlatform().getName())) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a unique index and an auto increment column.
   */
  public void testAddUniqueIndexAndAutoIncrementColumn() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
  }

  /**
   * Tests the addition of a unique index and a required column.
   */
  public void testAddUniqueIndexAndRequiredColumn() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a unique index and a column with a default value.
   */
  public void testAddUniqueIndexAndColumnWithDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Double(2).equals(avalue));
  }

  /**
   * Tests the addition of a unique index and a required auto increment column.
   */
  public void testAddUniqueIndexAndRequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a unique index and a required column with a default value.
   */
  public void testAddUniqueIndexAndRequiredColumnWithDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='CHAR' size='8' required='true' default='sometext'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals("sometext", beans.get(0), "avalue");
  }

  /**
   * Tests the addition of a unique index and several columns.
   */
  public void testAddUniqueIndexAndMultipleColumns() {
    // TODO
    if (!getPlatformInfo().isIndicesSupported() ||
      BuiltinDriverType.INTERBASE.getName().equals(getPlatform().getName())) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a unique index and several required columns.
   */
  public void testAddUniqueIndexAndMultipleRequiredColumns() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true' default='1'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a column into a non-unique index.
   */
  public void testAddColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(null, beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of an auto increment column into a non-unique index.
   */
  public void testAddAutoIncrementColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Integer(1).equals(avalue2));
  }

  /**
   * Tests the addition of a required column into a non-unique index.
   */
  public void testAddRequiredColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a column with a default value into a non-unique index.
   */
  public void testAddColumnWithDefaultIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Double(2).equals(avalue2));
  }

  /**
   * Tests the addition of a required auto increment column into a non-unique index.
   */
  public void testAddRequiredAutoIncrementColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of a required column with a default value into a non-unique index.
   */
  public void testAddRequiredColumnWithDefaultIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='8' default='sometext' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals("sometext", beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of multiple columns into a non-unique index.
   */
  public void testAddMultipleColumnsIntoNonUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue3' type='DOUBLE' required='true'/>\n" +
        "    <index name='test'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "      <index-column name='avalue3'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a column into a unique index.
   */
  public void testAddColumnIntoUniqueIndex() {
    // TODO
    if (!getPlatformInfo().isIndicesSupported() ||
      BuiltinDriverType.INTERBASE.getName().equals(getPlatform().getName())) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(null, beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of a auto increment column into a unique index.
   */
  public void testAddAutoIncrementColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Integer(1).equals(avalue2));
  }

  /**
   * Tests the addition of a required column into a unique index.
   */
  public void testAddRequiredColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a column with a default value into a unique index.
   */
  public void testAddColumnWithDefaultIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Double(2).equals(avalue2));
  }

  /**
   * Tests the addition of a required auto increment column into a unique index.
   */
  public void testAddRequiredAutoIncrementColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of a required column with a default value into a unique index.
   */
  public void testAddRequiredColumnWithDefaultIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='CHAR' size='8' default='sometext' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals("sometext", beans.get(0), "avalue2");
  }

  /**
   * Tests the addition of multiple columns into a unique index.
   */
  public void testAddMultipleColumnsIntoUniqueIndex() {
    // TODO
    if (!getPlatformInfo().isIndicesSupported() ||
      BuiltinDriverType.INTERBASE.getName().equals(getPlatform().getName())) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue3' type='DOUBLE' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "      <unique-column name='avalue3'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of multiple required columns into a unique index.
   */
  public void testAddMultipleRequiredColumnsIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' required='true' default='3'/>\n" +
        "    <column name='avalue3' type='DOUBLE' required='true'/>\n" +
        "    <unique name='test'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "      <unique-column name='avalue3'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the addition of a foreign key and its local column.
   */
  public void testAddFKAndLocalColumn() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"text"});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals("text", beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(null, beans2.get(0), "avalue");
  }

  /**
   * Tests the addition of a foreign key and its local auto increment column.
   */
  public void testAddFKAndLocalAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");
    Object avalue = beans2.get(0).get("avalue");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
  }

  /**
   * Tests the addition of a foreign key and its local required column.
   */
  public void testAddFKAndLocalRequiredColumn() {
    // TODO
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName())) {
      // MySql does not allow adding a required column to a fk without a default value
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal(1)});
    insertRow("roundtrip2", new Object[]{2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal(1), beans1.get(0), "pk");
    Assert.assertTrue(beans2.isEmpty());
  }

  /**
   * Tests the addition of a foreign key and its local column with a default value.
   */
  public void testAddFKAndLocalColumnWithDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE' default='1'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1.0});
    insertRow("roundtrip2", new Object[]{2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");
    Object avalue = beans2.get(0).get("avalue");

    assertEquals(1.0, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    Assert.assertTrue((avalue == null) || new Double(1).equals(avalue));
  }

  /**
   * Tests the addition of a foreign key and its local required auto increment column.
   */
  public void testAddFKAndLocalRequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue");
  }

  /**
   * Tests the addition of a foreign key and its local required column with a default value.
   */
  public void testAddFKAndLocalRequiredColumnWithDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='8' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='8' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='CHAR' size='8' required='true' default='moretext'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"moretext"});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals("moretext", beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals("moretext", beans2.get(0), "avalue");
  }

  /**
   * Tests the addition of a foreign key and its local columns.
   */
  public void testAddFKAndMultipleLocalColumns() {
    // TODO
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName())) {
      // MySql does not allow adding a required column to a fk without a default value
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='avalue2' type='DOUBLE' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1, 2.0});
    insertRow("roundtrip2", new Object[]{3});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(2.0, beans1.get(0), "pk2");
    Assert.assertTrue(beans2.isEmpty());
  }

  /**
   * Tests the addition of a foreign key and its foreign column.
   */
  public void testAddFKAndForeignColumn() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);
    // no point trying this with data in the db as it will only cause a constraint violation
    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }

  /**
   * Tests the addition of a foreign key and its foreign auto increment column.
   */
  public void testAddFKAndForeignAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2});
    insertRow("roundtrip2", new Object[]{1, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue");
  }

  /**
   * Tests the addition of a foreign key and its foreign auto increment column.
   */
  public void testAddFKAndForeignColumnWithDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true' default='1'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2});
    insertRow("roundtrip2", new Object[]{1, 1.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1.0, beans1.get(0), "pk");
    assertEquals(2, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(1.0, beans2.get(0), "avalue");
  }

  /**
   * Tests the addition of a foreign key and its multiple foreign columns.
   */
  public void testAddFKAndMultipleForeignColumns() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true' default='1'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);
    // no point trying this with data in the db as it will only cause a constraint violation
    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }

  /**
   * Tests the addition of local and foreign column into a foreign key.
   */
  public void testAddColumnsIntoFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);
    // no point trying this with data in the db as it will only cause a constraint violation
    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }

  /**
   * Tests the addition of local and foreign auto increment columns into a foreign key.
   */
  public void testAddAutoIncrementColumnIntoFK() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");
    Object avalue2 = beans2.get(0).get("avalue2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(1, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Integer(1).equals(avalue2));
  }

  /**
   * Tests the addition of local and foreign required columns into a foreign key.
   */
  public void testAddRequiredColumnsIntoFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);
    // no point trying this with data in the db as it will only cause a constraint violation
    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }

  /**
   * Tests the addition of local and foreign columns with default values into a foreign key.
   */
  public void testAddColumnsWithDefaultsIntoFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true' default='2'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");
    Object avalue2 = beans2.get(0).get("avalue2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(2.0, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    Assert.assertTrue((avalue2 == null) || new Double(2).equals(avalue2));
  }

  /**
   * Tests the addition of local and foreign required auto increment columns into a foreign key.
   */
  public void testAddRequiredAutoIncrementColumnIntoFK() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(1, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    assertEquals(1, beans2.get(0), "avalue2");
  }

  /**
   * Tests the addition of local and foreign required columns with default values into a foreign key.
   */
  public void testAddRequiredColumnsWithDefaultsIntoFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='CHAR' size='8' primaryKey='true' required='true' default='sometext'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='8' required='true' default='sometext'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    List<DynaBean> beans1 = getRows("roundtrip1");
    List<DynaBean> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals("sometext", beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    assertEquals("sometext", beans2.get(0), "avalue2");
  }

  /**
   * Tests the addition of multiple local and foreign columns into a foreign key.
   */
  public void testAddMultipleColumnsIntoFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' default='1'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' default='1'/>\n" +
        "    <column name='avalue3' type='DOUBLE' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue3' foreign='pk3'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);
    // no point trying this with data in the db as it will only cause a constraint violation
    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));
  }
}
