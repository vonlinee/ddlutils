package org.apache.ddlutils.task;

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

import org.apache.ddlutils.data.RowObject;
import org.apache.ddlutils.data.TableClass;
import org.apache.ddlutils.io.DataReader;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.task.command.WriteDataToFileCommand;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests the writeDataToFile sub-task.
 *
 * @version $Revision: $
 */
public class TestWriteDataToFileCommand extends TestTaskBase {

  /**
   * Adds the writeDataToFile sub-task to the given task, executes it, and checks its output.
   *
   * @param task         The task
   * @param expectedData A map table name -> list of dyna beans sorted by the pk value that is expected
   */
  private void runTask(DatabaseToDdlTask task, Map<String, List<RowObject>> expectedData) throws IOException {
    WriteDataToFileCommand subTask = new WriteDataToFileCommand(task.getProperties());
    File tmpFile = File.createTempFile("data", ".xml");

    try {
      subTask.setOutputFile(tmpFile);
      task.addWriteDataToFile(subTask);
      task.setModelName("roundtriptest");
      task.execute();

      DataReader dataReader = new DataReader();
      final Map<String, List<RowObject>> readData = new HashMap<>();

      dataReader.setModel(getAdjustedModel());
      dataReader.setSink(bean -> {
        String key = ((TableClass) bean.getTableClass()).getTableName();
        List<RowObject> beans = readData.computeIfAbsent(key, k -> new ArrayList<>());
        beans.add(bean);
      });
      dataReader.read(tmpFile);

      Assert.assertEquals("Not the same tables in the expected and actual data", expectedData.keySet(), readData.keySet());
    } catch (TaskException e) {
      throw new RuntimeException(e);
    } finally {
      if (!tmpFile.delete()) {
        getLog().warn("Could not delete temporary file " + tmpFile.getAbsolutePath());
      }
    }
  }

  /**
   * Tests the task against an empty database.
   */
  @Test
  public void testEmptyDatabase() throws IOException {
    runTask(getDatabaseToDdlTaskInstance(), new HashMap<>());
  }

  /**
   * Tests against a simple model.
   */
  @Test
  public void testSimpleModel() throws IOException {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='roundtrip'>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    List<RowObject> beans = new ArrayList<>();

    beans.add(insertRow("roundtrip", new Object[]{"test1", 1}));
    beans.add(insertRow("roundtrip", new Object[]{"test2", null}));

    Map<String, List<RowObject>> expected = new HashMap<>();

    expected.put("roundtrip", beans);
    runTask(getDatabaseToDdlTaskInstance(), expected);
  }

  /**
   * Tests against a model with multiple tables and foreign keys.
   */
  @Test
  public void testComplexModel() throws IOException {
    final String modelXml =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='roundtriptest'>\n" +
        "  <table name='Roundtrip_1'>\n" +
        "    <column name='pk' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='Roundtrip_3'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='Roundtrip_2'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='INTEGER'/>\n" +
        "    <foreign-key foreignTable='Roundtrip_3'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='Roundtrip_3'>\n" +
        "    <column name='pk' type='INTEGER' primaryKey='true' required='true'/>\n" +
        "    <column name='avalue' type='VARCHAR' size='32'/>\n" +
        "    <foreign-key foreignTable='Roundtrip_1'>\n" +
        "      <reference local='avalue' foreign='pk'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    createDatabase(modelXml);

    List<RowObject> beans1 = new ArrayList<>();
    List<RowObject> beans2 = new ArrayList<>();
    List<RowObject> beans3 = new ArrayList<>();

    beans1.add(insertRow("Roundtrip_1", new Object[]{"test1", null}));
    beans2.add(insertRow("Roundtrip_2", new Object[]{3, null}));
    beans3.add(insertRow("Roundtrip_3", new Object[]{1, "test1"}));
    beans2.add(insertRow("Roundtrip_2", new Object[]{2, 1}));
    beans1.add(insertRow("Roundtrip_1", new Object[]{"test2", 1}));
    beans3.add(insertRow("Roundtrip_3", new Object[]{3, null}));
    beans3.add(insertRow("Roundtrip_3", new Object[]{4, "test2"}));
    beans1.add(insertRow("Roundtrip_1", new Object[]{"test3", 3}));
    beans3.add(insertRow("Roundtrip_3", new Object[]{2, "test3"}));
    beans2.add(insertRow("Roundtrip_2", new Object[]{1, 2}));

    Map<String, List<RowObject>> expected = new HashMap<>();

    expected.put("Roundtrip_1", beans1);
    expected.put("Roundtrip_2", beans2);
    expected.put("Roundtrip_3", beans3);
    runTask(getDatabaseToDdlTaskInstance(), expected);
  }

}
