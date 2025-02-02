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
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Tests changing columns, e.g. changing the data type or size.
 * <p>
 * TODO:
 * - more type conversion tests (parameterized ?)
 * - more size change tests (parameterized ?)
 *
 * @version $Revision: $
 */
public class TestChangeColumn extends TestAgainstLiveDatabaseBase {

//  - change default value (add default, remove default, change default)
//  - combined changes, e.g
//    - type & size
//    - type & auto increment status
//    - type & required
//    - size & required
//    - required & default value
//  - for each of the above: normal column, pk column, fk column (local), index column, unique index column

  /**
   * Tests the alteration of a column datatype change from integer to double.
   */
  @Test
  public void testColumnTypeIntegerToDouble() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2.0, beans.get(0), "avalue");
  }

  /**
   * Tests the alteration of a column datatype change from smallint to varchar.
   */
  @Test
  public void testColumnTypeSmallintToVarchar() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='SMALLINT'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, (short) 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean.get("avalue") instanceof String) {
      bean.set("avalue", ((String) bean.get("avalue")).trim());
    }
    assertEquals(1, beans.get(0), "pk");
    assertEquals("2", beans.get(0), "avalue");
  }

  /**
   * Tests the alteration of a pk column datatype change from integer to double.
   */
  @Test
  public void testPKColumnTypeIntegerToDouble() {
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
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{2.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(2.0, beans.get(0), "pk");
  }

  /**
   * Tests the alteration of a pk column datatype change from integer to varchar.
   */
  @Test
  public void testPKColumnTypeIntegerToVarchar() {
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
        "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean.get("pk") instanceof String) {
      bean.set("pk", ((String) bean.get("pk")).trim());
    }
    assertEquals("1", beans.get(0), "pk");
  }

  /**
   * Tests the change of the datatypes of PK and FK columns from integer to varchar.
   */
  @Test
  public void testPKAndFKColumnTypesIntegerToVarchar() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='128' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='128' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{1, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    RowObject bean1 = beans1.get(0);
    RowObject bean2 = beans2.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean1.get("pk") instanceof String) {
      bean1.set("pk", ((String) bean1.get("pk")).trim());
    }
    if (bean2.get("fk") instanceof String) {
      bean2.set("fk", ((String) bean2.get("fk")).trim());
    }
    assertEquals("1", bean1, "pk");
    assertEquals(1, bean2, "pk");
    assertEquals("1", bean2, "fk");
  }

  /**
   * Tests the alteration of the datatypes of columns of a PK and FK that
   * will be dropped.
   */
  @Test
  public void testPKAndFKColumnTypesAndDropFK() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' primaryKey='false' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' required='false'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{1});
    insertRow("roundtrip2", new Object[]{1, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    RowObject bean1 = beans1.get(0);
    RowObject bean2 = beans2.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean1.get("pk") instanceof String) {
      bean1.set("pk", ((String) bean1.get("pk")).trim());
    }
    if (bean2.get("fk") instanceof String) {
      bean2.set("fk", ((String) bean2.get("fk")).trim());
    }
    assertEquals("1", bean1, "pk");
    assertEquals(1, bean2, "pk");
    assertEquals("1", bean2, "fk");
  }

  /**
   * Tests the alteration of an indexed column datatype change from integer to double.
   */
  @Test
  public void testIndexColumnTypeIntegerToDouble() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DOUBLE'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2.0, beans.get(0), "avalue1");
    assertEquals("text", beans.get(0), "avalue2");
  }

  /**
   * Tests the alteration of an indexed column datatype change from smallint to varchar.
   */
  @Test
  public void testIndexColumnTypeSmallintToVarchar() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='SMALLINT'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='20'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, (short) 2, 3.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean.get("avalue1") instanceof String) {
      bean.set("avalue1", ((String) bean.get("avalue1")).trim());
    }
    assertEquals(1, beans.get(0), "pk");
    assertEquals("2", beans.get(0), "avalue1");
    assertEquals(3.0, beans.get(0), "avalue2");
  }

  /**
   * Tests the alteration of the datatype of an indexed column where
   * the index will be dropped.
   */
  @Test
  public void testIndexColumnTypeAndDropIndex() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='8' required='false'/>\n" +
        "    <index name='avalue_index'>\n" +
        "      <index-column name='avalue'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' required='false'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 1});
    insertRow("roundtrip", new Object[]{2, 10});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(1, beans.get(0), "avalue");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(10, beans.get(1), "avalue");
  }

  /**
   * Tests the alteration of an indexed column datatype change from integer to double.
   */
  @Test
  public void testUniqueIndexColumnTypeIntegerToDouble() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DOUBLE'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2.0, beans.get(0), "avalue1");
    assertEquals("text", beans.get(0), "avalue2");
  }

  /**
   * Tests the alteration of an indexed column datatype change from smallint to varchar.
   */
  @Test
  public void testUniqueIndexColumnTypeSmallintToVarchar() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='SMALLINT'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='20'/>\n" +
        "    <column name='avalue2' type='DOUBLE'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, (short) 2, 3.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some databases (e.g. DB2) pad the string for some reason, so we manually trim it
    if (bean.get("avalue1") instanceof String) {
      bean.set("avalue1", ((String) bean.get("avalue1")).trim());
    }
    assertEquals(1, beans.get(0), "pk");
    assertEquals("2", beans.get(0), "avalue1");
    assertEquals(3.0, beans.get(0), "avalue2");
  }

  /**
   * Tests increasing the size of a column.
   */
  @Test
  public void testColumnIncreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("test", beans.get(0), "avalue");
  }

  /**
   * Tests decreasing the size of a column.
   */
  @Test
  public void testColumnDecreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='16' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "12345678901234567890123456789012"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    Assert.assertTrue("12345678901234567890123456789012".equals(bean.get("avalue")) || "1234567890123456".equals(bean.get("avalue")));
  }

  /**
   * Tests increasing the size of a primary key column.
   */
  @Test
  public void testPKColumnIncreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='CHAR' size='20' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='CHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{"test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    Assert.assertEquals("test", ((String) bean.get("pk")).trim());
  }

  /**
   * Tests decreasing the size of a column.
   */
  @Test
  public void testPKColumnDecreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='CHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='CHAR' size='16' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{"12345678901234567890123456789012"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    Assert.assertTrue("12345678901234567890123456789012".equals(bean.get("pk")) || "1234567890123456".equals(bean.get("pk")));
  }

  /**
   * Tests increasing the sizes of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnIncreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='32' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='128' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='128' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"test"});
    insertRow("roundtrip2", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals("test", beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals("test", beans2.get(0), "fk");
  }

  /**
   * Tests decreasing the sizes of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnDecreaseSize() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='32' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='CHAR' size='32' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='16' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='CHAR' size='16' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"test"});
    insertRow("roundtrip2", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    RowObject bean1 = beans1.get(0);
    RowObject bean2 = beans2.get(0);

    Assert.assertEquals("test", ((String) bean1.get("pk")).trim());
    assertEquals(1, bean2, "pk");
    Assert.assertEquals("test", ((String) bean2.get("fk")).trim());
  }

  /**
   * Tests increasing the size of an indexed column.
   */
  @Test
  public void testIndexColumnIncreaseSize() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='40'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals("text", beans.get(0), "avalue2");
  }

  /**
   * Tests decreasing the size of an indexed column.
   */
  @Test
  public void testIndexColumnDecreaseSize() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='32'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue1");
    Assert.assertEquals("text", ((String) bean.get("avalue2")).trim());
  }

  /**
   * Tests increasing the size of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnIncreaseSize() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='16'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue1");
    Assert.assertEquals("text", ((String) bean.get("avalue2")).trim());
  }

  /**
   * Tests decreasing the size of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnDecreaseSize() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='40'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals("text", beans.get(0), "avalue2");
  }

  /**
   * Tests increasing the precision of a column.
   */
  @Test
  public void testColumnIncreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='10,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='16,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("12345.12345")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("12345.12345"), beans.get(0), "avalue");
  }

  /**
   * Tests decreasng the precision of a column.
   */
  @Test
  public void testColumnDecreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='12,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='10,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("12345.12345")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("12345.12345"), beans.get(0), "avalue");
  }

  /**
   * Tests increasing the precision of a primary key column.
   */
  @Test
  public void testPKColumnIncreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='DECIMAL' size='10,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='DECIMAL' size='16,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345.12345")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal("12345.12345"), beans.get(0), "pk");
  }

  /**
   * Tests decreasing the precision of a primary key column.
   */
  @Test
  public void testPKColumnDecreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='DECIMAL' size='12,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='DECIMAL' size='10,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345.12345")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal("12345.12345"), beans.get(0), "pk");
  }

  /**
   * Tests increasing the precision of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnIncreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='8,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='8,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='11,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='11,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal("123456.12")});
    insertRow("roundtrip2", new Object[]{1, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal("123456.12"), beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans2.get(0), "fk");
  }

  /**
   * Tests decreasing the precision of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnDecreasePrecision() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='10,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='8,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='8,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal("123456.12")});
    insertRow("roundtrip2", new Object[]{1, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal("123456.12"), beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans2.get(0), "fk");
  }

  /**
   * Tests increasing the precision of an indexed column.
   */
  @Test
  public void testIndexColumnIncreasePrecision() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='8,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='12,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("123456.12"), 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue1");
    assertEquals(2, beans.get(0), "avalue2");
  }

  /**
   * Tests decreasing the precision of an indexed column.
   */
  @Test
  public void testIndexColumnDecreasePrecision() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='10,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='8,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("123456.12"), 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue1");
    assertEquals(2, beans.get(0), "avalue2");
  }

  /**
   * Tests increasing the precision of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnIncreasePrecision() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='8,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='9,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue2");
  }

  /**
   * Tests decreasing the precision of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnDecreasePrecision() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='9,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='8,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue2");
  }

  /**
   * Tests increasing the scale of a column.
   */
  @Test
  public void testColumnIncreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='10,4' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='10,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("12345.1234")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    // Some DBs return the BigDecimal with the five digits scale, some don't
    Assert.assertTrue(bean.get("avalue").equals(new BigDecimal("12345.1234")) ||
      bean.get("avalue").equals(new BigDecimal("12345.12340")));
  }

  /**
   * Tests decreasing the scale of a column.
   */
  @Test
  public void testColumnDecreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='10,5' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DECIMAL' size='10,3' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("12345.123")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("12345.123"), beans.get(0), "avalue");
  }

  /**
   * Tests increasing the scale of a primary key column.
   */
  @Test
  public void testPKColumnIncreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,3' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345.123")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some DBs return the BigDecimal with the three digits scale, some don't
    Assert.assertTrue(bean.get("pk").equals(new BigDecimal("12345.123")) ||
      bean.get("pk").equals(new BigDecimal("12345.12300")));
  }

  /**
   * Tests decreasing the scale of a primary key column.
   */
  @Test
  public void testPKColumnDecreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,3' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345.123")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal("12345.123"), beans.get(0), "pk");
  }

  /**
   * Tests increasing the scale of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnIncreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DECIMAL' size='11,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DECIMAL' size='11,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DECIMAL' size='11,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DECIMAL' size='11,5' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal("123456.12")});
    insertRow("roundtrip2", new Object[]{1, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");
    RowObject bean1 = beans1.get(0);
    RowObject bean2 = beans2.get(0);

    // Some DBs return the BigDecimal with the three digits scale, some don't
    Assert.assertTrue(bean1.get("pk").equals(new BigDecimal("123456.12")) ||
      bean1.get("pk").equals(new BigDecimal("123456.12000")));
    assertEquals(1, beans2.get(0), "pk");
    Assert.assertTrue(bean2.get("fk").equals(new BigDecimal("123456.12")) ||
      bean2.get("fk").equals(new BigDecimal("123456.12000")));
  }

  /**
   * Tests decreasing the scale of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnDecreaseScale() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='11,5' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='11,5' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='11,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='11,2' required='false'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal("123456.12")});
    insertRow("roundtrip2", new Object[]{1, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal("123456.12"), beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans2.get(0), "fk");
  }

  /**
   * Tests increasing the scale of an indexed column.
   */
  @Test
  public void testIndexColumnIncreaseScale() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='NUMERIC' size='11,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='NUMERIC' size='11,5'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("123456.12"), 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue2");
    Assert.assertTrue(new BigDecimal("123456.12").equals(bean.get("avalue1")) || new BigDecimal("123456.12000").equals(bean.get("avalue1")));
  }

  /**
   * Tests decreasing the scale of an indexed column.
   */
  @Test
  public void testIndexColumnDecreaseScale() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='11,5'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='DECIMAL' size='11,2'/>\n" +
        "    <column name='avalue2' type='INTEGER'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("123456.12"), 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue1");
    assertEquals(2, beans.get(0), "avalue2");
  }

  /**
   * Tests increasing the scale of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnIncreaseScale() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DECIMAL' size='11,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='DECIMAL' size='11,5'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue1");
    Assert.assertTrue(new BigDecimal("123456.12").equals(bean.get("avalue2")) || new BigDecimal("123456.12000").equals(bean.get("avalue2")));
  }

  /**
   * Tests decreasing the scale of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnDecreaseScale() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='11,5'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='11,2'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, new BigDecimal("123456.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(new BigDecimal("123456.12"), beans.get(0), "avalue2");
  }

  /**
   * Tests making a column required.
   */
  @Test
  public void testColumnMakeRequired() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("test", beans.get(0), "avalue");
  }

  /**
   * Tests making a column no longer required.
   */
  @Test
  public void testColumnUnmakeRequired() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue");
  }

  /**
   * Tests making a primary key column required.
   */
  @Test
  public void testPKColumnMakeRequired() {
    if (getPlatformInfo().isPrimaryKeyColumnAutomaticallyRequired() ||
      getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,2' primaryKey='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345678.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal("12345678.12"), beans.get(0), "pk");
  }

  /**
   * Tests making a primary key column no longer required.
   */
  @Test
  public void testPKColumnUnmakeRequired() {
    if (getPlatformInfo().isPrimaryKeyColumnAutomaticallyRequired() ||
      getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,2' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='10,2' primaryKey='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{new BigDecimal("12345678.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(new BigDecimal("12345678.12"), beans.get(0), "pk");
  }

  /**
   * Tests making a FK column required.
   */
  @Test
  public void testFKColumnMakeRequired() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
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
        "    <column name='fk' type='DOUBLE' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2.0});
    insertRow("roundtrip2", new Object[]{1, 2.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2.0, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2.0, beans2.get(0), "fk");
  }

  /**
   * Tests making a FK column no longer required.
   */
  @Test
  public void testFKColumnUnmakeRequired() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DOUBLE' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
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
        "    <column name='fk' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2.0});
    insertRow("roundtrip2", new Object[]{1, 2.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2.0, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2.0, beans2.get(0), "fk");
  }

  /**
   * Tests making PK and FK columns required.
   */
  @Test
  public void testPKAndFKColumnMakeRequired() {
    if (getPlatformInfo().isPrimaryKeyColumnAutomaticallyRequired() ||
      getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='10' primaryKey='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='CHAR' size='10'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='CHAR' size='10' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='CHAR' size='10' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"1234567890"});
    insertRow("roundtrip2", new Object[]{1, "1234567890"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals("1234567890", beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals("1234567890", beans2.get(0), "fk");
  }

  /**
   * Tests making PK and FK columns no longer required.
   */
  @Test
  public void testPKAndFKColumnUnmakeRequired() {
    if (getPlatformInfo().isPrimaryKeyColumnAutomaticallyRequired() ||
      getPlatformInfo().isPrimaryKeyColumnsHaveToBeRequired()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DOUBLE' required='true'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DOUBLE'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2.0});
    insertRow("roundtrip2", new Object[]{1, 2.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2.0, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2.0, beans2.get(0), "fk");
  }

  /**
   * Tests making an indexed column required.
   */
  @Test
  public void testIndexColumnMakeRequired() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP' required='true'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // some databases (such as MySql) don't store micro-/nanoseconds
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, 2, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(time, beans.get(0), "avalue2");
  }

  /**
   * Tests making an indexed column no longer required.
   */
  @Test
  public void testIndexColumnUnmakeRequired() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='SMALLINT' required='true'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='SMALLINT'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // some databases (such as MySql) don't store micro-/nanoseconds
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, 2, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(time, beans.get(0), "avalue2");
  }

  /**
   * Tests making an indexed column required.
   */
  @Test
  public void testUniqueIndexColumnMakeRequired() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='16'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='20' required='true'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue1");
    Assert.assertEquals("text", ((String) bean.get("avalue2")).trim());
  }

  /**
   * Tests making an indexed column no longer required.
   */
  @Test
  public void testUniqueIndexColumnUnmakeRequired() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' required='true'/>\n" +
        "    <column name='avalue2' type='CHAR' size='16'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='CHAR' size='20'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2, "text"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    assertEquals(1, bean, "pk");
    assertEquals(2, bean, "avalue1");
    Assert.assertEquals("text", ((String) bean.get("avalue2")).trim());
  }

  /**
   * Tests making a column auto increment.
   */
  @Test
  public void testColumnMakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue' type='INTEGER'/>\n") +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n") +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("0") : new Integer(0);

    insertRow("roundtrip", new Object[]{1, value});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(value, beans.get(0), "avalue");
  }

  /**
   * Tests making a column no longer auto increment.
   */
  @Test
  public void testColumnUnmakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue' type='INTEGER' autoIncrement='true'/>\n") +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue' type='INTEGER'/>\n") +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    Object value = isSybase ? new BigDecimal("1") : new Integer(1);

    assertEquals(1, beans.get(0), "pk");
    assertEquals(value, beans.get(0), "avalue");
  }

  /**
   * Tests making a primary column auto increment.
   */
  @Test
  public void testPKColumnMakeAutoIncrement() {
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n") +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("5") : new Integer(5);

    insertRow("roundtrip", new Object[]{value});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(value, beans.get(0), "pk");
  }

  /**
   * Tests making a primary column no longer auto increment.
   */
  @Test
  public void testPKColumnUnmakeAutoIncrement() {
    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n") +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{null, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    Object value = isSybase ? new BigDecimal("1") : new Integer(1);

    assertEquals(value, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue");
  }

  /**
   * Tests making a FK column auto increment.
   */
  @Test
  public void testFKColumnMakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='fk' type='INTEGER'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='fk' type='INTEGER' autoIncrement='true'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("0") : new Integer(0);

    insertRow("roundtrip1", new Object[]{value});
    insertRow("roundtrip2", new Object[]{1, value});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(value, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(value, beans2.get(0), "fk");
  }

  /**
   * Tests making a FK column no longer auto increment.
   */
  @Test
  public void testFKColumnnUnmakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='fk' type='INTEGER' autoIncrement='true'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='fk' type='INTEGER'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("1") : new Integer(1);

    insertRow("roundtrip1", new Object[]{value});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(value, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(value, beans2.get(0), "fk");
  }

  /**
   * Tests making PK and FK columns auto increment.
   */
  @Test
  public void testPKAndFKColumnMakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='fk' type='INTEGER'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n") +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='fk' type='INTEGER' autoIncrement='true'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("0") : new Integer(0);

    insertRow("roundtrip1", new Object[]{value});
    insertRow("roundtrip2", new Object[]{1, value});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(value, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(value, beans2.get(0), "fk");
  }

  /**
   * Tests making PK and FK columns no longer auto increment.
   */
  @Test
  public void testPKAndFKColumnUnmakeAutoIncrement() {
    if (!getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true' autoIncrement='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true' autoIncrement='true'/>\n") +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='fk' type='INTEGER' autoIncrement='true'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        (isSybase ?
          "    <column name='pk' type='NUMERIC' size='12,0' primaryKey='true' required='true'/>\n"
          : "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n") +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='fk' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='fk' type='INTEGER'/>\n") +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{null, 2});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    Object value = isSybase ? new BigDecimal("1") : new Integer(1);
    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(value, beans1.get(0), "pk");
    assertEquals(2, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(value, beans2.get(0), "fk");
  }

  /**
   * Tests making an indexed column auto increment.
   */
  @Test
  public void testIndexColumnMakeAutoIncrement() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue1' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue1' type='INTEGER'/>\n") +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue1' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue1' type='INTEGER' autoIncrement='true'/>\n") +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("0") : new Integer(0);
    Timestamp time = new Timestamp(new Date().getTime());

    // to avoid problems with the database's time resolution
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, value, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(time, beans.get(0), "avalue2");
    assertEquals(value, beans.get(0), "avalue1");
  }

  /**
   * Tests making an indexed column no longer auto increment.
   */
  @Test
  public void testIndexColumnUnmakeAutoIncrement() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16'/>\n" +
        (isSybase ?
          "    <column name='avalue2' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n") +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16'/>\n" +
        (isSybase ?
          "    <column name='avalue2' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue2' type='INTEGER'/>\n") +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    Object value = isSybase ? new BigDecimal("1") : new Integer(1);
    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("test", beans.get(0), "avalue1");
    assertEquals(value, beans.get(0), "avalue2");
  }

  /**
   * Tests making an indexed column auto increment.
   */
  @Test
  public void testUniqueIndexColumnMakeAutoIncrement() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16'/>\n" +
        (isSybase ?
          "    <column name='avalue2' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue2' type='INTEGER'/>\n") +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16'/>\n" +
        (isSybase ?
          "    <column name='avalue2' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue2' type='INTEGER' autoIncrement='true'/>\n") +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Object value = isSybase ? new BigDecimal("0") : new Integer(0);

    insertRow("roundtrip", new Object[]{1, "test", value});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("test", beans.get(0), "avalue1");
    assertEquals(value, beans.get(0), "avalue2");
  }

  /**
   * Tests making an indexed column no longer auto increment.
   */
  @Test
  public void testUniqeIndexColumnUnmakeAutoIncrement() {
    if (!getPlatformInfo().isIndicesSupported() ||
      !getPlatformInfo().isNonPrimaryKeyIdentityColumnsSupported()) {
      return;
    }

    boolean isSybase = BuiltinDriverType.SYBASE.getName().equals(getPlatform().getName());
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue1' type='NUMERIC' size='12,0' autoIncrement='true'/>\n"
          : "    <column name='avalue1' type='INTEGER' autoIncrement='true'/>\n") +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        (isSybase ?
          "    <column name='avalue1' type='NUMERIC' size='12,0'/>\n"
          : "    <column name='avalue1' type='INTEGER'/>\n") +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // to avoid problems with the database's time resolution
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, null, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    Object value = isSybase ? new BigDecimal("1") : new Integer(1);

    assertEquals(1, beans.get(0), "pk");
    assertEquals(value, beans.get(0), "avalue1");
    assertEquals(time, beans.get(0), "avalue2");
  }

  /**
   * Tests adding a default value to a column.
   */
  @Test
  public void testColumnAddDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='DOUBLE' default='2.0'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 1.0});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(1.0, beans.get(0), "avalue");
  }

  /**
   * Tests changing the default value of a column.
   */
  @Test
  public void testColumnChangeDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' default='1'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER' default='20'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 2});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue");
  }

  /**
   * Tests removing the default value of a column.
   */
  @Test
  public void testColumnRemoveDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20' default='test'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='20'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("test", beans.get(0), "avalue");
  }

  /**
   * Tests adding a default value to a primary key column.
   */
  @Test
  public void testPKColumnAddDefault() throws ParseException {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='TIMESTAMP' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='TIMESTAMP' primaryKey='true' required='true' default='2000-01-01 00:00:00'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // some databases (such as MySql) don't store micro-/nanoseconds
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{time, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{null, 2});

    List<RowObject> beans = getRows("roundtrip", "avalue");
    Timestamp defaultTime = Timestamp.valueOf("2000-01-01 00:00:00");

    assertEquals(time, beans.get(0), "pk");
    assertEquals(1, beans.get(0), "avalue");
    assertEquals(defaultTime, beans.get(1), "pk");
    assertEquals(2, beans.get(1), "avalue");
  }

  /**
   * Tests changing the default value of a primary key column.
   */
  @Test
  public void testPKColumnChangeDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true' default='old'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='VARCHAR' size='20' primaryKey='true' required='true' default='new'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{null, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{null, 2});

    List<RowObject> beans = getRows("roundtrip", "avalue");

    assertEquals("old", beans.get(0), "pk");
    assertEquals(1, beans.get(0), "avalue");
    assertEquals("new", beans.get(1), "pk");
    assertEquals(2, beans.get(1), "avalue");
  }

  /**
   * Tests removing the default value of a primary key column.
   */
  @Test
  public void testPKColumnRemoveDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='12,2' primaryKey='true' required='true' default='2'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='NUMERIC' size='12,2' primaryKey='true' required='true' default='2.20'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{null, 1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");
    RowObject bean = beans.get(0);

    // Some DBs return the BigDecimal with the two digits scale, some don't
    Assert.assertTrue(bean.get("pk").equals(new BigDecimal("2")) || bean.get("pk").equals(new BigDecimal("2.00")));
    assertEquals(1, bean, "avalue");
  }

  /**
   * Tests adding a default value to a FK column.
   */
  @Test
  public void testFKColumnAddDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='12' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='12'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='12' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='12' default='text'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{"test"});
    insertRow("roundtrip2", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip1", new Object[]{"text"});
    insertRow("roundtrip2", new Object[]{2});

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals("test", beans1.get(0), "pk");
    assertEquals("text", beans1.get(1), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals("test", beans2.get(0), "fk");
    assertEquals(2, beans2.get(1), "pk");
    assertEquals("text", beans2.get(1), "fk");
  }

  /**
   * Tests changing the default value of a FK column.
   */
  @Test
  public void testFKColumnChangeDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='DOUBLE' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='DOUBLE' default='2.0'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
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
        "    <column name='fk' type='DOUBLE' default='3.0'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2.0});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip1", new Object[]{3.0});
    insertRow("roundtrip2", new Object[]{2});

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2.0, beans1.get(0), "pk");
    assertEquals(3.0, beans1.get(1), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2.0, beans2.get(0), "fk");
    assertEquals(2, beans2.get(1), "pk");
    assertEquals(3.0, beans2.get(1), "fk");
  }

  /**
   * Tests removing the default value of a FK column.
   */
  @Test
  public void testFKColumnRemoveDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' default='2'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
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
        "    <column name='fk' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{2});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2, beans1.get(0), "pk");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2, beans2.get(0), "fk");
  }

  /**
   * Tests adding default values to PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnAddDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='12' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='12'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='NUMERIC' size='12' primaryKey='true' required='true' default='1'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='NUMERIC' size='12' default='1'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{new BigDecimal("0"), 1});
    insertRow("roundtrip2", new Object[]{1, new BigDecimal("0")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip1", new Object[]{null, 2});
    insertRow("roundtrip2", new Object[]{2});

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(new BigDecimal("0"), beans1.get(0), "pk");
    assertEquals(1, beans1.get(0), "avalue");
    assertEquals(new BigDecimal("1"), beans1.get(1), "pk");
    assertEquals(2, beans1.get(1), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(new BigDecimal("0"), beans2.get(0), "fk");
    assertEquals(2, beans2.get(1), "pk");
    assertEquals(new BigDecimal("1"), beans2.get(1), "fk");
  }

  /**
   * Tests changing the default values of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnChangeDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' default='2'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' default='2'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true' default='3'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='INTEGER' default='3'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{null, 1});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip1", new Object[]{null, 2});
    insertRow("roundtrip2", new Object[]{2});

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals(2, beans1.get(0), "pk");
    assertEquals(1, beans1.get(0), "avalue");
    assertEquals(3, beans1.get(1), "pk");
    assertEquals(2, beans1.get(1), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals(2, beans2.get(0), "fk");
    assertEquals(2, beans2.get(1), "pk");
    assertEquals(3, beans2.get(1), "fk");
  }

  /**
   * Tests removing the default values of PK and FK columns.
   */
  @Test
  public void testPKAndFKColumnRemoveDefault() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='16' primaryKey='true' required='true' default='text'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='16' default='text'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip1'>\n" +
        "    <column name='pk' type='VARCHAR' size='16' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "  <table name='roundtrip2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='fk' type='VARCHAR' size='16'/>\n" +
        "    <foreign-key foreignTable='roundtrip1'>\n" +
        "      <reference local='fk' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip1", new Object[]{null, 1});
    insertRow("roundtrip2", new Object[]{1});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans1 = getRows("roundtrip1");
    List<RowObject> beans2 = getRows("roundtrip2");

    assertEquals("text", beans1.get(0), "pk");
    assertEquals(1, beans1.get(0), "avalue");
    assertEquals(1, beans2.get(0), "pk");
    assertEquals("text", beans2.get(0), "fk");
  }

  /**
   * Tests adding a default value to an indexed column.
   */
  @Test
  public void testIndexColumnAddDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // some databases (such as MySql) don't store micro-/nanoseconds
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, 2, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{2, null, time});

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(time, beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(3, beans.get(1), "avalue1");
    assertEquals(time, beans.get(1), "avalue2");
  }

  /**
   * Tests changing the default value of an indexed column.
   */
  @Test
  public void testIndexColumnChangeDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12' default='3'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12' default='4'/>\n" +
        "    <index name='testindex'>\n" +
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

    insertRow("roundtrip", new Object[]{2, 3});

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(new BigDecimal("3"), beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(3, beans.get(1), "avalue1");
    assertEquals(new BigDecimal("4"), beans.get(1), "avalue2");
  }

  /**
   * Tests removing the default value of an indexed column.
   */
  @Test
  public void testIndexColumnRemoveDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16' default='text'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='VARCHAR' size='16'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12'/>\n" +
        "    <index name='testindex'>\n" +
        "      <index-column name='avalue1'/>\n" +
        "      <index-column name='avalue2'/>\n" +
        "    </index>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, null, new BigDecimal("3")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{2, "test", new BigDecimal("4")});

    List<RowObject> beans = getRows("roundtrip", "pk");

    assertEquals(1, beans.get(0), "pk");
    assertEquals("text", beans.get(0), "avalue1");
    assertEquals(new BigDecimal("3"), beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals("test", beans.get(1), "avalue1");
    assertEquals(new BigDecimal("4"), beans.get(1), "avalue2");
  }

  /**
   * Tests adding a default value to an indexed column.
   */
  @Test
  public void testUniqueIndexColumnAddDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='TIMESTAMP' default='2000-01-01 00:00:00'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    Timestamp time = new Timestamp(new Date().getTime());

    // some databases (such as MySql) don't store micro-/nanoseconds
    time.setNanos(0);

    insertRow("roundtrip", new Object[]{1, 2, time});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{2, 3});

    List<RowObject> beans = getRows("roundtrip");
    Timestamp defaultTime = Timestamp.valueOf("2000-01-01 00:00:00");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(2, beans.get(0), "avalue1");
    assertEquals(time, beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(3, beans.get(1), "avalue1");
    assertEquals(defaultTime, beans.get(1), "avalue2");
  }

  /**
   * Tests changing the default value of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnChangeDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='3'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER' default='4'/>\n" +
        "    <column name='avalue2' type='NUMERIC' size='12'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, null, new BigDecimal("2")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{2, null, new BigDecimal("3")});

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(3, beans.get(0), "avalue1");
    assertEquals(new BigDecimal("2"), beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(4, beans.get(1), "avalue1");
    assertEquals(new BigDecimal("3"), beans.get(1), "avalue2");
  }

  /**
   * Tests removing the default value of an indexed column.
   */
  @Test
  public void testUniqueIndexColumnRemoveDefault() {
    if (!getPlatformInfo().isIndicesSupported()) {
      return;
    }

    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='16' default='text'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue1' type='INTEGER'/>\n" +
        "    <column name='avalue2' type='VARCHAR' size='16'/>\n" +
        "    <unique name='testindex'>\n" +
        "      <unique-column name='avalue1'/>\n" +
        "      <unique-column name='avalue2'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, 3});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    insertRow("roundtrip", new Object[]{2, 4, "test"});

    List<RowObject> beans = getRows("roundtrip");

    assertEquals(1, beans.get(0), "pk");
    assertEquals(3, beans.get(0), "avalue1");
    assertEquals("text", beans.get(0), "avalue2");
    assertEquals(2, beans.get(1), "pk");
    assertEquals(4, beans.get(1), "avalue1");
    assertEquals("test", beans.get(1), "avalue2");
  }

  /**
   * Tests the alteration of a column's datatype and size.
   */
  @Test
  public void testChangeDatatypeAndSize1() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='CHAR' size='4' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, "test"});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals("test", beans.get(0), "avalue");
  }

  /**
   * Tests the alteration of a column's datatype and size.
   */
  @Test
  public void testChangeDatatypeAndSize2() {
    final String model1Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='NUMERIC' size='10,2' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String model2Xml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='8' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(model1Xml);

    insertRow("roundtrip", new Object[]{1, new BigDecimal("12345678.12")});

    alterDatabase(model2Xml);

    assertEquals(getAdjustedModel(),
      readModelFromDatabase("roundtriptest"));

    List<RowObject> beans = getRows("roundtrip");

    assertEquals("12345678", beans.get(0), "avalue");
  }
}
