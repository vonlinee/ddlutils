package org.apache.ddlutils.model;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for DDLUTILS-6.
 *
 * @version $Revision: 289996 $
 */
public class TestArrayAccessAtTable {
  /**
   * The tested table.
   */
  private Table _testedTable;
  /**
   * The first tested column.
   */
  private Column _column1;
  /**
   * The second tested column.
   */
  private Column _column2;
  /**
   * The tested unique index.
   */
  private UniqueIndex _uniqueIndex;
  /**
   * The tested non-unique index.
   */
  private NonUniqueIndex _nonUniqueIndex;

  /**
   * {@inheritDoc}
   */
  @Before
  public void setUp() {
    _testedTable = new Table();

    _column1 = new Column();
    _column1.setName("column1");
    _column1.setPrimaryKey(true);

    _column2 = new Column();
    _column2.setName("column2");

    _testedTable.addColumn(_column1);
    _testedTable.addColumn(_column2);

    _uniqueIndex = new UniqueIndex();
    _testedTable.addIndex(_uniqueIndex);

    _nonUniqueIndex = new NonUniqueIndex();
    _testedTable.addIndex(_nonUniqueIndex);
  }

  /**
   * Tests that the primary key columns are correctly extracted.
   */
  @Test
  public void testGetPrimaryKeyColumns() {
    Column[] primaryKeyColumns = _testedTable.getPrimaryKeyColumns();

    Assert.assertEquals(1, primaryKeyColumns.length);
    Assert.assertSame(_column1, primaryKeyColumns[0]);
  }

  /**
   * Tests that the columns are correctly extracted.
   */
  @Test
  public void testGetColumns() {
    Column[] columns = _testedTable.getColumns();

    Assert.assertEquals(2, columns.length);
    Assert.assertSame(_column1, columns[0]);
    Assert.assertSame(_column2, columns[1]);
  }

  /**
   * Tests that the non-unique indices are correctly extracted.
   */
  @Test
  public void testGetNonUniqueIndices() {
    Index[] nonUniqueIndices = _testedTable.getNonUniqueIndices();

    Assert.assertEquals(1, nonUniqueIndices.length);
    Assert.assertSame(_nonUniqueIndex, nonUniqueIndices[0]);
  }

  /**
   * Tests that the unique indices are correctly extracted.
   */
  @Test
  public void testGetUniqueIndices() {
    Index[] uniqueIndices = _testedTable.getUniqueIndices();

    Assert.assertEquals(1, uniqueIndices.length);
    Assert.assertSame(_uniqueIndex, uniqueIndices[0]);
  }
}
