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
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests database alterations that insert columns.
 *
 * @version $Revision: $
 */
public class TestInsertColumn extends TestAgainstLiveDatabaseBase {

  /**
   * Tests the insertion of a column.
   */
  public void testInsertColumn() {
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
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of an auto-increment column.
   */
  public void testInsertAutoIncrementColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "avalue");
    } else {
      Object avalue = beans.get(0).get("avalue");

      Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
    }
  }

  /**
   * Tests the insertion of a column that is set to NOT NULL.
   */
  public void testInsertRequiredColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' default='2' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal(2), beans.get(0), "avalue");
  }

  /**
   * Tests the insert of a column with a default value. Note that depending
   * on whether the database supports this via a statement, this test may fail.
   * For instance, Sql Server has a statement for this which means that the
   * existing value in column avalue won't be changed and thus the test fails.
   */
  public void testInsertColumnWithDefault() {
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
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    // we cannot be sure whether the default algorithm is used (which will apply the
    // default value even to existing columns with NULL in it) or the database supports
    // it directly (in which case it might still be NULL)
    Object avalue = beans.get(0).get("avalue");

    Assert.assertTrue((avalue == null) || new Double(2).equals(avalue));
  }

  /**
   * Tests the insertion of a required auto-increment column.
   */
  public void testInsertRequiredAutoIncrementColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='avalue' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "avalue");
    } else {
      Object avalue = beans.get(0).get("avalue");

      Assert.assertTrue((avalue == null) || new Integer(1).equals(avalue));
    }
  }

  /**
   * Tests the insertion of a column with a default value. Note that depending
   * on whether the database supports this via a statement, this test may fail.
   * For instance, Sql Server has a statement for this which means that the
   * existing value in column avalue won't be changed and thus the test fails.
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
        "    <column name='avalue' type='CHAR' size='8' default='text' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    // we cannot be sure whether the default algorithm is used (which will apply the
    // default value even to existing columns with NULL in it) or the database supports
    // it directly (in which case it might still be NULL)
    Object avalue = beans.get(0).get("avalue");

    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", avalue);
    } else {
      // TODO
      //Assert.assertTrue((avalue == null) || "text    ".equals(avalue));
      Assert.assertEquals("text    ", avalue);
    }
  }

  /**
   * Tests the addition and insertion of several columns.
   */
  public void testAddAndInsertMultipleColumns() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue3' type='DOUBLE' default='1.0'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <column name='avalue3' type='DOUBLE' default='1.0' required='true'/>\n" +
        "    <column name='avalue4' type='VARCHAR' size='16'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 3.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue1");
    assertEquals(null, beans.get(0), "avalue2");
    assertEquals(3.0, beans.get(0), "avalue3");
    assertEquals(null, beans.get(0), "avalue4");
  }

  /**
   * Tests the insertion of a primary key and a column.
   */
  public void testInsertPKAndColumn() {
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
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    Assert.assertTrue(beans.isEmpty());
  }

  /**
   * Tests the insertion of a primary key and an autoincrement column.
   */
  public void testInsertPKAndAutoIncrementColumn() {
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
        "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(1), beans.get(0), "avalue");
    } else {
      assertEquals(1, beans.get(0), "avalue");
    }
  }

  /**
   * Tests the insertion of a primary key and multiple columns.
   */
  public void testAddAndInsertPKAndMultipleColumns() {
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
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }


  /**
   * Tests the insertion of a column to a primary key.
   */
  public void testInsertColumnIntoPK() {
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
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of an autoincrement column into the primary key.
   */
  public void testInsertAutoIncrementColumnIntoPK() {
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
        "    <column name='pk2' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    } else {
      model2Xml = "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    }

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{-1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    if (isSybase) {
      assertEquals(new BigDecimal(-1), beans.get(0), "pk1");
    } else {
      assertEquals(-1, beans.get(0), "pk1");
    }
    Assert.assertEquals(2, beans.get(0).get("avalue"));
  }

  /**
   * Tests the insertion of multiple columns into the primary key.
   */
  public void testInsertMultipleColumnsIntoPK() {
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
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a non-unique index and a column.
   */
  public void testInsertNonUniqueIndexAndColumn() {
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
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a non-unique index and an auto increment column.
   */
  public void testInsertNonUniqueIndexAndAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a non-unique index and a required column.
   */
  public void testInsertNonUniqueIndexAndRequiredColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a non-unique index and a column with a default value.
   */
  public void testInsertNonUniqueIndexAndColumnWithDefault() {
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
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2.0, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a non-unique index and a required auto increment column.
   */
  public void testInsertNonUniqueIndexAndrequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a non-unique index and a required column with a default value.
   */
  public void testInsertNonUniqueIndexAndRequiredColumnWithDefault() {
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
        "    <column name='avalue' type='CHAR' size='8' required='true' default='text'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", avalue);
    } else {
      Assert.assertEquals("text    ", avalue);
    }
  }

  /**
   * Tests the insertion of a non-unique index and several columns.
   */
  public void testAddAndInsertNonUniqueIndexAndMultipleColumns() {
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
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a unique index and a column.
   */
  public void testInsertUniqueIndexAndColumn() {
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
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(null, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of an unique index and an auto increment column.
   */
  public void testInsertUniqueIndexAndAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a unique index and a required column.
   */
  public void testInsertUniqueIndexAndRequiredColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a unique index and a column with a default value.
   */
  public void testInsertUniqueIndexAndColumnWithDefault() {
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
        "    <column name='avalue' type='DOUBLE' default='2'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2.0, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a unique index and a required auto increment column.
   */
  public void testInsertUniqueIndexAndRequiredAutoIncrementColumn() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "avalue");
  }

  /**
   * Tests the insertion of a unique index and a required column with a default value.
   */
  public void testInsertUniqueIndexAndRequiredColumnWithDefault() {
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
        "    <column name='avalue' type='CHAR' size='8' required='true' default='text'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");
    Object avalue = beans.get(0).get("avalue");

    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", avalue);
    } else {
      Assert.assertEquals("text    ", avalue);
    }
  }

  /**
   * Tests the insertion of a unique index and several columns.
   */
  public void testAddAndInsertUniqueIndexAndMultipleColumns() {
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
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a column into a non-unique index.
   */
  public void testInsertColumnIntoNonUniqueIndex() {
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
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(null, beans.get(0), "avalue2");
  }

  /**
   * Tests the insert of an auto increment column into a non-unique index.
   */
  public void testInsertAutoIncrementColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required column into a non-unique index.
   */
  public void testInsertRequiredColumnIntoNonUniqueIndex() {
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
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a column with a default value into a non-unique index.
   */
  public void testInsertColumnWithDefaultIntoNonUniqueIndex() {
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
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(2.0, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required auto increment column into a non-unique index.
   */
  public void testInsertRequiredAutoIncrementColumnIntoNonUniqueIndex() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
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
        "    <column name='avalue2' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required column with a default value into a non-unique index.
   */
  public void testInsertRequiredColumnWithDefaultIntoNonUniqueIndex() {
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
        "    <column name='avalue2' type='CHAR' size='8' default='text' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", avalue2);
    } else {
      Assert.assertEquals("text    ", avalue2);
    }
  }

  /**
   * Tests the insertion of multiple columns into a non-unique index.
   */
  public void testAddAndInsertMultipleColumnsIntoNonUniqueIndex() {
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
        "    <column name='avalue2' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a column into a unique index.
   */
  public void testInsertColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(null, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of an auto increment column into an unique index.
   */
  public void testInsertAutoIncrementColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required column into a unique index.
   */
  public void testInsertRequiredColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a column with a default value into a unique index.
   */
  public void testInsertColumnWithDefaultIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(2.0, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required auto increment column into an unique index.
   */
  public void testInsertRequiredAutoIncrementColumnIntoUniqueIndex() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported() ||
      !getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='INTEGER' autoIncrement='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(1, beans.get(0), "avalue2");
  }

  /**
   * Tests the insertion of a required column with a default value into a unique index.
   */
  public void testInsertRequiredColumnWithDefaultIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='CHAR' size='8' default='text' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    List<RowObject> beans = getRows("roundtrip");
    Object avalue2 = beans.get(0).get("avalue2");

    assertEquals(2, beans.get(0), "avalue1");
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", avalue2);
    } else {
      Assert.assertEquals("text    ", avalue2);
    }
  }

  /**
   * Tests the insertion of multiple columns into a unique index.
   */
  public void testAddAndInsertMultipleColumnsIntoUniqueIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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
        "    <column name='avalue2' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
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

    Assert.assertTrue(getRows("roundtrip").isEmpty());
  }

  /**
   * Tests the insertion of a foreign key and its local column.
   */
  public void testInsertFKAndLocalColumn() {
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
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals("text", beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(null, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its local auto increment column.
   */
  public void testInsertFKAndLocalAutoIncrementColumn() {
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
        "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its local required column.
   */
  public void testInsertFKAndLocalRequiredColumn() {
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
        "    <column name='avalue' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal(1), beans1.get(0), "pk");
    Assert.assertTrue(beans2.isEmpty());
  }

  /**
   * Tests the insertion of a foreign key and its local column with a default value.
   */
  public void testInsertFKAndLocalColumnWithDefault() {
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
        "    <column name='avalue' type='DOUBLE' default='1'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1.0, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1.0, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its local required auto increment column.
   */
  public void testInsertFKAndLocalRequiredAutoIncrementColumn() {
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
        "    <column name='avalue' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its local required column with a default value.
   */
  public void testInsertFKAndLocalRequiredColumnWithDefault() {
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
        "    <column name='avalue' type='CHAR' size='8' required='true' default='text'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    Object pk1 = beans1.get(0).get("pk");
    Object avalue = beans2.get(0).get("avalue");

    assertEquals(1, beans2.get(0), "pk");
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", pk1);
      Assert.assertEquals("text", avalue);
    } else {
      // TODO
      //Assert.assertTrue((avalue == null) || "text    ".equals(avalue));
      Assert.assertEquals("text    ", pk1);
      Assert.assertEquals("text    ", avalue);
    }
  }

  /**
   * Tests the insertion of a foreign key and its local columns.
   */
  public void testAddAndInsertFKAndMultipleLocalColumns() {
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
        "    <column name='avalue1' type='INTEGER' default='1'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(2.0, beans1.get(0), "pk2");
    Assert.assertTrue(beans2.isEmpty());
  }

  /**
   * Tests the insertion of a foreign key and its foreign column.
   */
  public void testInsertFKAndForeignColumn() {
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
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
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
   * Tests the insertion of a foreign key and its foreign auto increment column.
   */
  public void testInsertFKAndForeignAutoIncrementColumn() {
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
        "    <column name='pk' type='INTEGER' primaryKey='true' autoIncrement='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk");
    assertEquals(2, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its foreign auto increment column.
   */
  public void testInsertFKAndForeignColumnWithDefault() {
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
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true' default='1'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
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

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1.0, beans1.get(0), "pk");
    assertEquals(2, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(1.0, beans2.get(0), "avalue");
  }

  /**
   * Tests the insertion of a foreign key and its multiple foreign columns.
   */
  public void testAddAndInsertFKAndMultipleForeignColumns() {
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
        "    <column name='pk2' type='DOUBLE' primaryKey='true' required='true' default='1'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
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
   * Tests the insertion of local and foreign column into a foreign key.
   */
  public void testInsertColumnsIntoFK() {
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
        "    <column name='pk2' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='32'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
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
   * Tests the insertion of local and foreign auto increment columns into a foreign key.
   */
  public void testInsertAutoIncrementColumnIntoFK() {
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
        "    <column name='pk2' type='INTEGER' primaryKey='true' autoIncrement='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(1, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    assertEquals(1, beans2.get(0), "avalue2");
  }

  /**
   * Tests the insertion of local and foreign required columns into a foreign key.
   */
  public void testInsertRequiredColumnsIntoFK() {
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
        "    <column name='pk2' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12,0' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
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
   * Tests the insertion of local and foreign columns with default values into a foreign key.
   */
  public void testInsertColumnsWithDefaultsIntoFK() {
    if (getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
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
        "    <column name='pk2' type='DOUBLE' primaryKey='true' default='2'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='DOUBLE' default='2'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(2.0, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    assertEquals(2.0, beans2.get(0), "avalue2");
  }

  /**
   * Tests the insertion of local and foreign required auto increment columns into a foreign key.
   */
  public void testInsertRequiredAutoIncrementColumnIntoFK() {
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
        "    <column name='pk2' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='INTEGER' required='true' autoIncrement='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(1, beans1.get(0), "pk2");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    assertEquals(1, beans2.get(0), "avalue2");
  }

  /**
   * Tests the insertion of local and foreign required columns with default values into a foreign key.
   */
  public void testInsertRequiredColumnsWithDefaultsIntoFK() {
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
        "    <column name='pk2' type='CHAR' size='8' primaryKey='true' required='true' default='text'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue2' type='CHAR' size='8' required='true' default='text'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue2' foreign='pk2'/>\n" +
        "      <reference local='avalue1' foreign='pk1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{2, 1});

    alterDatabase(model2Xml);

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    Object pk2 = beans1.get(0).get("pk2");
    Object avalue2 = beans2.get(0).get("avalue2");

    assertEquals(1, beans1.get(0), "pk1");
    assertEquals(2, beans2.get(0), "pk");
    assertEquals(1, beans2.get(0), "avalue1");
    if (BuiltinDriverType.MYSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MYSQL5X.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.HSQL.getName().equals(getPlatform().getName()) ||
      BuiltinDriverType.MAX_DB.getName().equals(getPlatform().getName())) {
      // Some DBs ignore that the type is CHAR(8) and trim the value
      Assert.assertEquals("text", pk2);
      Assert.assertEquals("text", avalue2);
    } else {
      Assert.assertEquals("text    ", pk2);
      Assert.assertEquals("text    ", avalue2);
    }
  }

  /**
   * Tests the insertion of multiple local and foreign columns into a foreign key.
   */
  public void testAddAndInsertMultipleColumnsIntoFK() {
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
        "    <column name='pk3' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "    <column name='pk1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='pk2' type='INTEGER' primaryKey='true' default='1' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='avalue3' type='DOUBLE' required='true'/>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='INTEGER' default='1'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='avalue3' foreign='pk3'/>\n" +
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
}
