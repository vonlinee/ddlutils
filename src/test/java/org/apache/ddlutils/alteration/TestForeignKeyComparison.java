package org.apache.ddlutils.alteration;

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

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Types;
import java.util.List;

/**
 * Tests the model comparison.
 * <p>
 * TODO: need tests with foreign key without a name
 *
 * @version $Revision: $
 */
public class TestForeignKeyComparison extends TestComparisonBase {

  /**
   * Tests the addition of a single-column foreign key.
   */
  @Test
  public void testAddColumnAndForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK' foreign='COLPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(2, changes.size());

    AddColumnChange colChange = (AddColumnChange) changes.get(0);
    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(1);

    Assert.assertEquals("TableA", colChange.getChangedTable());
    assertColumn("COLFK", Types.INTEGER, null, null, false, false, false,
      colChange.getNewColumn());
    Assert.assertEquals("ColPK", colChange.getPreviousColumn());
    Assert.assertNull(colChange.getNextColumn());

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"COLFK"}, new String[]{"ColPK"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a single-column foreign key.
   */
  @Test
  public void testAddColumnAndForeignKeyToIt() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK' type='INTEGER'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='Col' type='INTEGER'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK' foreign='COLPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='Col' type='INTEGER'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    AddColumnChange colChange = (AddColumnChange) changes.get(0);
    AddPrimaryKeyChange pkChange = (AddPrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableB", colChange.getChangedTable());
    assertColumn("COLPK", Types.INTEGER, null, null, false, true, false,
      colChange.getNewColumn());
    Assert.assertNull(colChange.getPreviousColumn());
    Assert.assertEquals("Col", colChange.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(1, pkChange.getPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK", pkChange.getPrimaryKeyColumns()[0]);

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"COLFK"}, new String[]{"COLPK"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a multi-column foreign key.
   */
  @Test
  public void testAddColumnsAndForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(4, changes.size());

    AddColumnChange colChange1 = (AddColumnChange) changes.get(0);
    AddColumnChange colChange2 = (AddColumnChange) changes.get(1);
    AddColumnChange colChange3 = (AddColumnChange) changes.get(2);
    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(3);

    Assert.assertEquals("TableA", colChange1.getChangedTable());
    assertColumn("COLFK1", Types.INTEGER, null, null, false, false, false,
      colChange1.getNewColumn());
    Assert.assertEquals("ColPK", colChange1.getPreviousColumn());
    Assert.assertNull(colChange1.getNextColumn());

    Assert.assertEquals("TableA", colChange2.getChangedTable());
    assertColumn("COLFK2", Types.DOUBLE, null, null, false, false, false,
      colChange2.getNewColumn());
    Assert.assertEquals("COLFK1", colChange2.getPreviousColumn());
    Assert.assertNull(colChange2.getNextColumn());

    Assert.assertEquals("TableA", colChange3.getChangedTable());
    assertColumn("COLFK3", Types.VARCHAR, "32", null, false, false, false,
      colChange3.getNewColumn());
    Assert.assertEquals("COLFK2", colChange3.getPreviousColumn());
    Assert.assertNull(colChange3.getNextColumn());

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"COLFK2", "COLFK1", "COLFK3"}, new String[]{"ColPK1", "ColPK2", "ColPK3"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a multi-column foreign key.
   */
  @Test
  public void testAddColumnsAndForeignKeyToThem() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(4, changes.size());

    AddColumnChange colChange1 = (AddColumnChange) changes.get(0);
    AddColumnChange colChange2 = (AddColumnChange) changes.get(1);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(2);
    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(3);

    Assert.assertEquals("TableB", colChange1.getChangedTable());
    assertColumn("COLPK1", Types.DOUBLE, null, null, false, true, false,
      colChange1.getNewColumn());
    Assert.assertNull(colChange1.getPreviousColumn());
    Assert.assertEquals("ColPK3", colChange1.getNextColumn());

    Assert.assertEquals("TableB", colChange2.getChangedTable());
    assertColumn("COLPK2", Types.INTEGER, null, null, false, true, false,
      colChange2.getNewColumn());
    Assert.assertEquals("COLPK1", colChange2.getPreviousColumn());
    Assert.assertEquals("ColPK3", colChange2.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("ColPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1", "ColFK3"}, new String[]{"COLPK1", "COLPK2", "ColPK3"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a multi-column foreign key.
   */
  @Test
  public void testAddColumnsAndForeignKeyBetweenThem() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(6, changes.size());

    AddColumnChange colChange1 = (AddColumnChange) changes.get(0);
    AddColumnChange colChange2 = (AddColumnChange) changes.get(1);
    AddColumnChange colChange3 = (AddColumnChange) changes.get(2);
    AddColumnChange colChange4 = (AddColumnChange) changes.get(3);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(4);
    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(5);

    Assert.assertEquals("TableA", colChange1.getChangedTable());
    assertColumn("COLFK1", Types.INTEGER, null, null, false, false, false,
      colChange1.getNewColumn());
    Assert.assertEquals("ColPK", colChange1.getPreviousColumn());
    Assert.assertEquals("ColFK2", colChange1.getNextColumn());

    Assert.assertEquals("TableA", colChange2.getChangedTable());
    assertColumn("COLFK3", Types.VARCHAR, "32", null, false, false, false,
      colChange2.getNewColumn());
    Assert.assertEquals("ColFK2", colChange2.getPreviousColumn());
    Assert.assertNull(colChange2.getNextColumn());

    Assert.assertEquals("TableB", colChange3.getChangedTable());
    assertColumn("COLPK1", Types.DOUBLE, null, null, false, true, false,
      colChange3.getNewColumn());
    Assert.assertNull(colChange3.getPreviousColumn());
    Assert.assertEquals("ColPK3", colChange3.getNextColumn());

    Assert.assertEquals("TableB", colChange4.getChangedTable());
    assertColumn("COLPK2", Types.INTEGER, null, null, false, true, false,
      colChange4.getNewColumn());
    Assert.assertEquals("COLPK1", colChange4.getPreviousColumn());
    Assert.assertEquals("ColPK3", colChange4.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("ColPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "COLFK1", "COLFK3"}, new String[]{"COLPK1", "COLPK2", "ColPK3"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a single reference foreign key.
   */
  @Test
  public void testAddSingleReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK' type='INTEGER'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK' foreign='COLPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1, changes.size());

    AddForeignKeyChange change = (AddForeignKeyChange) changes.get(0);

    Assert.assertEquals("TableA", change.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK"}, new String[]{"ColPK"},
      change.getNewForeignKey());
  }

  /**
   * Tests the addition of a multi-reference foreign key.
   */
  @Test
  public void testAddMultiReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1, changes.size());

    AddForeignKeyChange fkChange = (AddForeignKeyChange) changes.get(0);

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1", "ColFK3"}, new String[]{"ColPK1", "ColPK2", "COLPK3"},
      fkChange.getNewForeignKey());
  }

  /**
   * Tests the addition of a column to a multi-reference foreign key.
   */
  @Test
  public void testAddLocalColumnToMultiReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='ColPK2'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(4, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    AddColumnChange colChange = (AddColumnChange) changes.get(1);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(2);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(3);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableA", colChange.getChangedTable());
    assertColumn("COLFK2", Types.DOUBLE, null, null, false, false, false,
      colChange.getNewColumn());
    Assert.assertEquals("ColFK1", colChange.getPreviousColumn());
    Assert.assertEquals("ColFK3", colChange.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"COLFK2", "ColFK1", "ColFK3"}, new String[]{"ColPK1", "ColPK2", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests the addition of a column to a multi-reference foreign key.
   */
  @Test
  public void testAddForeignColumnToMultiReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='ColPK1'/>\n" +
      "      <reference local='ColFK1' foreign='ColPK2'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(4, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    AddColumnChange colChange = (AddColumnChange) changes.get(1);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(2);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(3);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", colChange.getChangedTable());
    assertColumn("COLPK3", Types.VARCHAR, "32", null, false, true, false,
      colChange.getNewColumn());
    Assert.assertEquals("ColPK2", colChange.getPreviousColumn());
    Assert.assertNull(colChange.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1", "ColFK3"}, new String[]{"ColPK1", "ColPK2", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests the addition of columns to a single-reference foreign key.
   */
  @Test
  public void testAddColumnsToSingleReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='ColPK1'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(7, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    AddColumnChange colChange1 = (AddColumnChange) changes.get(1);
    AddColumnChange colChange2 = (AddColumnChange) changes.get(2);
    AddColumnChange colChange3 = (AddColumnChange) changes.get(3);
    AddColumnChange colChange4 = (AddColumnChange) changes.get(4);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(5);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(6);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableA", colChange1.getChangedTable());
    assertColumn("COLFK1", Types.INTEGER, null, null, false, false, false,
      colChange1.getNewColumn());
    Assert.assertEquals("ColPK", colChange1.getPreviousColumn());
    Assert.assertEquals("ColFK2", colChange1.getNextColumn());

    Assert.assertEquals("TableA", colChange2.getChangedTable());
    assertColumn("COLFK3", Types.VARCHAR, "32", null, false, false, false,
      colChange2.getNewColumn());
    Assert.assertEquals("ColFK2", colChange2.getPreviousColumn());
    Assert.assertNull(colChange2.getNextColumn());

    Assert.assertEquals("TableB", colChange3.getChangedTable());
    assertColumn("COLPK2", Types.INTEGER, null, null, false, true, false,
      colChange3.getNewColumn());
    Assert.assertEquals("ColPK1", colChange3.getPreviousColumn());
    Assert.assertNull(colChange3.getNextColumn());

    Assert.assertEquals("TableB", colChange4.getChangedTable());
    assertColumn("COLPK3", Types.VARCHAR, "32", null, false, true, false,
      colChange4.getNewColumn());
    Assert.assertEquals("COLPK2", colChange4.getPreviousColumn());
    Assert.assertNull(colChange4.getNextColumn());

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "COLFK1", "COLFK3"}, new String[]{"ColPK1", "COLPK2", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests the addition of a reference to a multi-reference foreign key.
   */
  @Test
  public void testAddReferenceToMultiReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1", "ColFK3"}, new String[]{"COLPK1", "COLPK2", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests the addition of references to a single-reference foreign key.
   */
  @Test
  public void testAddReferencesToSingleReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1", "ColFK3"}, new String[]{"COLPK1", "COLPK2", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests that the order of the references in a foreign key is not important.
   */
  @Test
  public void testForeignKeyReferenceOrder() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='ColPK1'/>\n" +
      "      <reference local='ColFK2' foreign='ColPK2'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='ColPK2'/>\n" +
      "      <reference local='ColFK1' foreign='ColPK1'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertTrue(changes.isEmpty());
  }

  /**
   * Tests adding a reference to a foreign key and changing the order of references.
   */
  @Test
  public void testAddReferenceToForeignKeyAndChangeOrder() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(3, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("COLPK3", pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK1", "ColFK2", "ColFK3"}, new String[]{"COLPK2", "COLPK1", "COLPK3"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests removing a reference from a foreign key.
   */
  @Test
  public void testRemoveReferenceFromForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='COLPK2'/>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(2, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK1", "ColFK2"}, new String[]{"COLPK2", "COLPK1"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests removing a reference from a foreign key and changing the order of references.
   */
  @Test
  public void testRemoveReferenceFromForeignKeyAndChangeOrder() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='COLPK2'/>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK2' foreign='COLPK1'/>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(3, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(2);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableB", pkChange.getChangedTable());
    Assert.assertEquals(2, pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("COLPK1", pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("COLPK2", pkChange.getNewPrimaryKeyColumns()[1]);

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK2", "ColFK1"}, new String[]{"COLPK1", "COLPK2"},
      fkChange2.getNewForeignKey());
  }

  // TODO: drop column  from reference PK

  /**
   * Tests dropping columns used in a foreign key.
   */
  @Test
  public void testDropColumnsFromForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='COLPK2'/>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TABLEB'>\n" +
      "      <reference local='COLFK1' foreign='COLPK2'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(5, changes.size());

    RemoveForeignKeyChange fkChange1 = (RemoveForeignKeyChange) changes.get(0);
    RemoveColumnChange colChange1 = (RemoveColumnChange) changes.get(1);
    RemoveColumnChange colChange2 = (RemoveColumnChange) changes.get(2);
    RemoveColumnChange colChange3 = (RemoveColumnChange) changes.get(3);
    AddForeignKeyChange fkChange2 = (AddForeignKeyChange) changes.get(4);

    Assert.assertEquals("TableA", fkChange1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange1.findChangedForeignKey(model1, false));

    Assert.assertEquals("TableA", colChange1.getChangedTable());
    Assert.assertEquals("ColFK3", colChange1.getChangedColumn());

    Assert.assertEquals("TableB", colChange2.getChangedTable());
    Assert.assertEquals("COLPK1", colChange2.getChangedColumn());

    Assert.assertEquals("TableB", colChange3.getChangedTable());
    Assert.assertEquals("COLPK3", colChange3.getChangedColumn());

    Assert.assertEquals("TableA", fkChange2.getChangedTable());
    assertForeignKey("TESTFK", "TableB", new String[]{"ColFK1"}, new String[]{"COLPK2"},
      fkChange2.getNewForeignKey());
  }

  /**
   * Tests the removal of a foreign key.
   */
  @Test
  public void testDropSingleReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableA'>\n" +
      "      <reference local='ColFK' foreign='ColPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK' type='INTEGER'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1, changes.size());

    RemoveForeignKeyChange change = (RemoveForeignKeyChange) changes.get(0);

    Assert.assertEquals("TableB", change.getChangedTable());
    Assert.assertEquals(model1.findTable("TableB").getForeignKey(0), change.findChangedForeignKey(model1, false));
  }


  /**
   * Tests dropping a multi-reference foreign key.
   */
  @Test
  public void testDropMultiReferenceForeignKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='DOUBLE'/>\n" +
      "    <column name='ColFK3' type='VARCHAR' size='32'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='COLPK2'/>\n" +
      "      <reference local='ColFK2' foreign='COLPK1'/>\n" +
      "      <reference local='ColFK3' foreign='COLPK3'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TABLEA'>\n" +
      "    <column name='COLPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLFK1' type='INTEGER'/>\n" +
      "    <column name='COLFK2' type='DOUBLE'/>\n" +
      "    <column name='COLFK3' type='VARCHAR' size='32'/>\n" +
      "  </table>\n" +
      "  <table name='TABLEB'>\n" +
      "    <column name='COLPK1' type='DOUBLE' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='COLPK3' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1, changes.size());

    RemoveForeignKeyChange fkChange = (RemoveForeignKeyChange) changes.get(0);

    Assert.assertEquals("TableA", fkChange.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), fkChange.findChangedForeignKey(model1, false));
  }

  /**
   * Tests the addition and removal of a foreign key.
   */
  @Test
  public void testAddAndDropForeignKey1() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableA'>\n" +
      "      <reference local='ColFK' foreign='ColPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK' type='INTEGER'/>\n" +
      "    <foreign-key name='TESTFK' foreignTable='TableA'>\n" +
      "      <reference local='ColFK' foreign='ColPK'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(2, changes.size());

    RemoveForeignKeyChange change1 = (RemoveForeignKeyChange) changes.get(0);
    AddForeignKeyChange change2 = (AddForeignKeyChange) changes.get(1);

    Assert.assertEquals("TableB", change1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableB").getForeignKey(0), change1.findChangedForeignKey(model1, true));

    Assert.assertEquals("TableB", change2.getChangedTable());
    assertForeignKey("TESTFK", "TableA", new String[]{"ColFK"}, new String[]{"ColPK"},
      change2.getNewForeignKey());
  }

  /**
   * Tests the recreation of a foreign key because of a change of the references.
   */
  @Test
  public void testAddAndDropForeignKey2() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='ColPK1'/>\n" +
      "      <reference local='ColFK2' foreign='ColPK2'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
      "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
      "  <table name='TableA'>\n" +
      "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColFK1' type='INTEGER'/>\n" +
      "    <column name='ColFK2' type='INTEGER'/>\n" +
      "    <foreign-key name='TestFK' foreignTable='TableB'>\n" +
      "      <reference local='ColFK1' foreign='ColPK2'/>\n" +
      "      <reference local='ColFK2' foreign='ColPK1'/>\n" +
      "    </foreign-key>\n" +
      "  </table>\n" +
      "  <table name='TableB'>\n" +
      "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
      "  </table>\n" +
      "</database>";

    Database model1 = DatabaseIO.parseString(MODEL1);
    Database model2 = DatabaseIO.parseString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(2, changes.size());

    RemoveForeignKeyChange change1 = (RemoveForeignKeyChange) changes.get(0);
    AddForeignKeyChange change2 = (AddForeignKeyChange) changes.get(1);

    Assert.assertEquals("TableA", change1.getChangedTable());
    Assert.assertEquals(model1.findTable("TableA").getForeignKey(0), change1.findChangedForeignKey(model1, true));

    Assert.assertEquals("TableA", change2.getChangedTable());
    assertForeignKey("TestFK", "TableB", new String[]{"ColFK1", "ColFK2"}, new String[]{"ColPK2", "ColPK1"},
      change2.getNewForeignKey());
  }

  // TODO: foreign key change to different table
  // TODO: foreign key change to different columns (?)
}
