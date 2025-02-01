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

import java.sql.Types;
import java.util.List;

/**
 * Tests the model comparison of primary keys.
 *
 * @version $Revision: $
 */
public class TestPrimaryKeyComparison extends TestComparisonBase {
  /**
   * Tests the addition of a column that is the primary key.
   */
  public void testAddPrimaryKeyColumn() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(2,
      changes.size());

    AddColumnChange colChange = (AddColumnChange) changes.get(0);
    AddPrimaryKeyChange pkChange = (AddPrimaryKeyChange) changes.get(1);

    Assert.assertEquals("TableA",
      colChange.getChangedTable());
    assertColumn("ColPK1", Types.INTEGER, null, null, false, true, false,
      colChange.getNewColumn());
    Assert.assertNull(colChange.getPreviousColumn());
    Assert.assertEquals("ColPK2",
      colChange.getNextColumn());

    Assert.assertEquals("TableA",
      pkChange.getChangedTable());
    Assert.assertEquals(1,
      pkChange.getPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK1",
      pkChange.getPrimaryKeyColumns()[0]);
  }

  /**
   * Tests the addition of a single-column primary key.
   */
  public void testMakeColumnPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    AddPrimaryKeyChange change = (AddPrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      change.getChangedTable());
    Assert.assertEquals(1,
      change.getPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK",
      change.getPrimaryKeyColumns()[0]);
  }

  /**
   * Tests the addition of a column to the primary key.
   */
  public void testAddColumnToPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    PrimaryKeyChange change = (PrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      change.getChangedTable());
    Assert.assertEquals(2,
      change.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK1",
      change.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK2",
      change.getNewPrimaryKeyColumns()[1]);
  }

  /**
   * Tests changing the order of columns in the primary key.
   */
  public void testChangeColumnOrderInPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(2,
      changes.size());

    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(0);
    ColumnOrderChange colChange = (ColumnOrderChange) changes.get(1);

    Assert.assertEquals("TableA",
      pkChange.getChangedTable());
    Assert.assertEquals(3,
      pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK2",
      pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK3",
      pkChange.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("ColPK1",
      pkChange.getNewPrimaryKeyColumns()[2]);

    Assert.assertEquals("TableA",
      colChange.getChangedTable());
    Assert.assertEquals(2,
      colChange.getNewPosition("ColPK1", true));
    Assert.assertEquals(0,
      colChange.getNewPosition("ColPK2", true));
    Assert.assertEquals(1,
      colChange.getNewPosition("ColPK3", true));
  }

  /**
   * Tests adding a column to and changing the order of columns in the primary key.
   */
  public void testAddColumnAndChangeColumnOrderInPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(4,
      changes.size());

    PrimaryKeyChange pkChange1 = (PrimaryKeyChange) changes.get(0);
    ColumnOrderChange colChange1 = (ColumnOrderChange) changes.get(1);
    AddColumnChange colChange2 = (AddColumnChange) changes.get(2);
    PrimaryKeyChange pkChange2 = (PrimaryKeyChange) changes.get(3);

    Assert.assertEquals("TableA",
      pkChange1.getChangedTable());
    Assert.assertEquals(2,
      pkChange1.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK3",
      pkChange1.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK1",
      pkChange1.getNewPrimaryKeyColumns()[1]);

    Assert.assertEquals("TableA",
      colChange1.getChangedTable());
    Assert.assertEquals(1,
      colChange1.getNewPosition("ColPK1", true));
    Assert.assertEquals(-1,
      colChange1.getNewPosition("ColPK2", true));
    Assert.assertEquals(0,
      colChange1.getNewPosition("ColPK3", true));

    Assert.assertEquals("TableA",
      colChange2.getChangedTable());
    assertColumn("ColPK2", Types.INTEGER, null, null, false, true, false,
      colChange2.getNewColumn());
    Assert.assertNull(colChange2.getPreviousColumn());
    Assert.assertEquals("ColPK3",
      colChange2.getNextColumn());

    Assert.assertEquals("TableA",
      pkChange2.getChangedTable());
    Assert.assertEquals(3,
      pkChange2.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK2",
      pkChange2.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK3",
      pkChange2.getNewPrimaryKeyColumns()[1]);
    Assert.assertEquals("ColPK1",
      pkChange2.getNewPrimaryKeyColumns()[2]);
  }

  /**
   * Tests removing a column from and changing the order of columns in the primary key.
   */
  public void testRemoveColumnAndChangeColumnOrderInPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(3,
      changes.size());

    RemoveColumnChange colChange1 = (RemoveColumnChange) changes.get(0);
    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(1);
    ColumnOrderChange colChange2 = (ColumnOrderChange) changes.get(2);
    Assert.
      assertEquals("TableA",
        colChange1.getChangedTable());
    Assert.assertEquals("ColPK2",
      colChange1.getChangedColumn());

    Assert.assertEquals("TableA",
      pkChange.getChangedTable());
    Assert.assertEquals(2,
      pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK3",
      pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK1",
      pkChange.getNewPrimaryKeyColumns()[1]);

    Assert.assertEquals("TableA",
      colChange2.getChangedTable());
    Assert.assertEquals(1,
      colChange2.getNewPosition("ColPK1", true));
    Assert.assertEquals(-1,
      colChange2.getNewPosition("ColPK2", true));
    Assert.assertEquals(0,
      colChange2.getNewPosition("ColPK3", true));
  }

  // TODO: remove, add & reorder PK columns

  /**
   * Tests the removal of a column from the primary key.
   */
  public void testMakeColumnNotPrimaryKey() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    PrimaryKeyChange change = (PrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      change.getChangedTable());
    Assert.assertEquals(1,
      change.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK2",
      change.getNewPrimaryKeyColumns()[0]);
  }


  /**
   * Tests removing the column that is the primary key.
   */
  public void testDropPrimaryKeyColumn1() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='Col' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TABLEA'>\n" +
        "    <column name='COL' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    RemoveColumnChange colChange = (RemoveColumnChange) changes.get(0);

    Assert.assertEquals("TableA",
      colChange.getChangedTable());
    Assert.assertEquals("ColPK",
      colChange.getChangedColumn());
  }

  /**
   * Tests dropping a column that is part of the primary key.
   */
  public void testDropPrimaryKeyColumn2() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(true).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    RemoveColumnChange colChange = (RemoveColumnChange) changes.get(0);

    Assert.assertEquals("TableA",
      colChange.getChangedTable());
    Assert.assertEquals("ColPK2",
      colChange.getChangedColumn());
  }

  /**
   * Tests the removal of a primary key.
   */
  public void testRemovePrimaryKey1() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    RemovePrimaryKeyChange change = (RemovePrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      change.getChangedTable());
  }

  /**
   * Tests removing a multi-column primary key.
   */
  public void testRemovePrimaryKey2() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TABLEA'>\n" +
        "    <column name='COLPK1' type='INTEGER' required='true'/>\n" +
        "    <column name='COLPK2' type='INTEGER' required='true'/>\n" +
        "    <column name='COLPK3' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    RemovePrimaryKeyChange pkChange = (RemovePrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      pkChange.getChangedTable());
  }

  /**
   * Tests changing the columns of a primary key.
   */
  public void testChangePrimaryKeyColumns() {
    final String MODEL1 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TableA'>\n" +
        "    <column name='ColPK1' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK4' type='INTEGER' required='true'/>\n" +
        "    <column name='ColPK5' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "  </table>\n" +
        "</database>";
    final String MODEL2 =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='test'>\n" +
        "  <table name='TABLEA'>\n" +
        "    <column name='ColPK1' type='INTEGER' required='true'/>\n" +
        "    <column name='ColPK2' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK3' type='INTEGER' required='true'/>\n" +
        "    <column name='ColPK4' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='ColPK5' type='INTEGER' required='true'/>\n" +
        "  </table>\n" +
        "</database>";

    Database model1 = parseDatabaseFromString(MODEL1);
    Database model2 = parseDatabaseFromString(MODEL2);
    List<ModelChange> changes = getPlatform(false).getChanges(model1, model2);

    Assert.assertEquals(1,
      changes.size());

    PrimaryKeyChange pkChange = (PrimaryKeyChange) changes.get(0);

    Assert.assertEquals("TableA",
      pkChange.getChangedTable());
    Assert.assertEquals(2,
      pkChange.getNewPrimaryKeyColumns().length);
    Assert.assertEquals("ColPK2",
      pkChange.getNewPrimaryKeyColumns()[0]);
    Assert.assertEquals("ColPK4",
      pkChange.getNewPrimaryKeyColumns()[1]);
  }
}
