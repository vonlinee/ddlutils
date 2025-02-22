package org.apache.ddlutils;

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
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.ddlutils.util.DatabaseTestHelper;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Base class for builder tests.
 *
 * @version $Revision$
 */
public abstract class TestPlatformBase extends TestBase {
  /**
   * The tested platform.
   */
  private Platform _platform;
  /**
   * The writer that the builder of the platform writes to.
   */
  private StringWriter _writer;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setUp() throws Exception {
    _writer = new StringWriter();
    _platform = PlatformFactory.createNewPlatformInstance(getDatabaseName());

    if (_platform == null) {
      throw new NullPointerException();
    }

    _platform.getSqlBuilder().setWriter(_writer);
    if (_platform.getPlatformInfo().isDelimitedIdentifiersSupported()) {
      _platform.setDelimitedIdentifierModeOn(true);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    _platform = null;
    _writer = null;
  }

  /**
   * Returns the tested platform.
   *
   * @return The platform
   */
  protected Platform getPlatform() {
    return _platform;
  }

  /**
   * Returns the info object of the tested platform.
   *
   * @return The platform info object
   */
  protected PlatformInfo getPlatformInfo() {
    return getPlatform().getPlatformInfo();
  }

  /**
   * Returns the SQL builder of the tested platform.
   *
   * @return The builder object
   */
  protected SqlBuilder getSqlBuilder() {
    return getPlatform().getSqlBuilder();
  }

  /**
   * Returns the builder output so far.
   *
   * @return The output
   */
  protected String getBuilderOutput() {
    return _writer.toString();
  }

  /**
   * Returns the name of the tested database.
   *
   * @return The database name
   */
  protected abstract String getDatabaseName();

  /**
   * Returns the database creation SQL for the given database schema.
   *
   * @param schema The database schema XML
   * @return The SQL
   */
  protected String getDatabaseCreationSql(String schema) throws IOException {
    Database testDb = parseDatabaseFromString(schema);

    // we're turning the comment creation off to make testing easier
    getPlatform().setSqlCommentsOn(false);
    getPlatform().getSqlBuilder().createTables(testDb);
    return getBuilderOutput();
  }

  /**
   * Returns the SQL to create the test database for the column tests.
   *
   * @return The SQL
   */
  protected String getColumnTestDatabaseCreationSql() throws IOException {
    final String schema =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='datatypetest'>\n" +
        "  <table name='coltype'>\n" +
        "    <column name='COL_ARRAY'           type='ARRAY'/>\n" +
        "    <column name='COL_BIGINT'          type='BIGINT'/>\n" +
        "    <column name='COL_BINARY'          type='BINARY'/>\n" +
        "    <column name='COL_BIT'             type='BIT'/>\n" +
        "    <column name='COL_BLOB'            type='BLOB'/>\n" +
        "    <column name='COL_BOOLEAN'         type='BOOLEAN'/>\n" +
        "    <column name='COL_CHAR'            type='CHAR' size='15'/>\n" +
        "    <column name='COL_CLOB'            type='CLOB'/>\n" +
        "    <column name='COL_DATALINK'        type='DATALINK'/>\n" +
        "    <column name='COL_DATE'            type='DATE'/>\n" +
        "    <column name='COL_DECIMAL'         type='DECIMAL' size='15,3'/>\n" +
        "    <column name='COL_DECIMAL_NOSCALE' type='DECIMAL' size='15'/>\n" +
        "    <column name='COL_DISTINCT'        type='DISTINCT'/>\n" +
        "    <column name='COL_DOUBLE'          type='DOUBLE'/>\n" +
        "    <column name='COL_FLOAT'           type='FLOAT'/>\n" +
        "    <column name='COL_INTEGER'         type='INTEGER'/>\n" +
        "    <column name='COL_JAVA_OBJECT'     type='JAVA_OBJECT'/>\n" +
        "    <column name='COL_LONGVARBINARY'   type='LONGVARBINARY'/>\n" +
        "    <column name='COL_LONGVARCHAR'     type='LONGVARCHAR'/>\n" +
        "    <column name='COL_NULL'            type='NULL'/>\n" +
        "    <column name='COL_NUMERIC'         type='NUMERIC' size='15' />\n" +
        "    <column name='COL_OTHER'           type='OTHER'/>\n" +
        "    <column name='COL_REAL'            type='REAL'/>\n" +
        "    <column name='COL_REF'             type='REF'/>\n" +
        "    <column name='COL_SMALLINT'        type='SMALLINT' size='5'/>\n" +
        "    <column name='COL_STRUCT'          type='STRUCT'/>\n" +
        "    <column name='COL_TIME'            type='TIME'/>\n" +
        "    <column name='COL_TIMESTAMP'       type='TIMESTAMP'/>\n" +
        "    <column name='COL_TINYINT'         type='TINYINT'/>\n" +
        "    <column name='COL_VARBINARY'       type='VARBINARY' size='15'/>\n" +
        "    <column name='COL_VARCHAR'         type='VARCHAR' size='15'/>\n" +
        "  </table>\n" +
        "</database>";

    return getDatabaseCreationSql(schema);
  }

  /**
   * Returns the SQL to create the test database for the constraint tests.
   *
   * @return The SQL
   */
  protected String getConstraintTestDatabaseCreationSql() throws IOException {
    final String schema =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='columnconstraintstest'>\n" +
        "  <table name='constraints'>\n" +
        "    <column name='COL_PK' type='VARCHAR' size='32' primaryKey='true'/>\n" +
        "    <column name='COL_PK_AUTO_INCR' type='INTEGER' primaryKey='true' autoIncrement='true'/>\n" +
        "    <column name='COL_NOT_NULL' type='BINARY' size='100' required='true'/>\n" +
        "    <column name='COL_NOT_NULL_DEFAULT' type='DOUBLE' required='true' default='-2.0'/>\n" +
        "    <column name='COL_DEFAULT' type='CHAR' size='4' default='test'/>\n" +
        "    <column name='COL_AUTO_INCR' type='BIGINT' autoIncrement='true'/>\n" +
        "  </table>\n" +
        "</database>";

    return getDatabaseCreationSql(schema);
  }

  /**
   * Returns the SQL to create the test database for the table-level constraint tests.
   *
   * @return The SQL
   */
  protected String getTableConstraintTestDatabaseCreationSql() throws IOException {
    final String schema =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='tableconstraintstest'>\n" +
        "  <table name='table1'>\n" +
        "    <column name='COL_PK_1' type='VARCHAR' size='32' primaryKey='true' required='true'/>\n" +
        "    <column name='COL_PK_2' type='INTEGER' primaryKey='true'/>\n" +
        "    <column name='COL_INDEX_1' type='BINARY' size='100' required='true'/>\n" +
        "    <column name='COL_INDEX_2' type='DOUBLE' required='true'/>\n" +
        "    <column name='COL_INDEX_3' type='CHAR' size='4'/>\n" +
        "    <index name='testindex1'>\n" +
        "      <index-column name='COL_INDEX_2'/>\n" +
        "    </index>\n" +
        "    <unique name='testindex2'>\n" +
        "      <unique-column name='COL_INDEX_3'/>\n" +
        "      <unique-column name='COL_INDEX_1'/>\n" +
        "    </unique>\n" +
        "  </table>\n" +
        "  <table name='table2'>\n" +
        "    <column name='COL_PK' type='INTEGER' primaryKey='true'/>\n" +
        "    <column name='COL_FK_1' type='INTEGER'/>\n" +
        "    <column name='COL_FK_2' type='VARCHAR' size='32' required='true'/>\n" +
        "    <foreign-key foreignTable='table1'>\n" +
        "      <reference local='COL_FK_1' foreign='COL_PK_2'/>\n" +
        "      <reference local='COL_FK_2' foreign='COL_PK_1'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "  <table name='table3'>\n" +
        "    <column name='COL_PK' type='VARCHAR' size='16' primaryKey='true'/>\n" +
        "    <column name='COL_FK' type='INTEGER' required='true'/>\n" +
        "    <foreign-key name='testfk' foreignTable='table2'>\n" +
        "      <reference local='COL_FK' foreign='COL_PK'/>\n" +
        "    </foreign-key>\n" +
        "  </table>\n" +
        "</database>";

    return getDatabaseCreationSql(schema);
  }

  /**
   * Returns the SQL to create the test database for testing character escaping.
   *
   * @return The SQL
   */
  protected String getCharEscapingTestDatabaseCreationSql() throws IOException {
    final String schema =
      "<?xml version='1.0' encoding='ISO-8859-1'?>\n" +
        "<database xmlns='" + DatabaseIO.DDLUTILS_NAMESPACE + "' name='escapetest'>\n" +
        "  <table name='escapedcharacters'>\n" +
        "    <column name='COL_PK' type='INTEGER' primaryKey='true'/>\n" +
        "    <column name='COL_TEXT' type='VARCHAR' size='128' default='&#39;'/>\n" +
        "  </table>\n" +
        "</database>";

    return getDatabaseCreationSql(schema);
  }

  /**
   * 读取指定类相同包名下的指定名称的文件文本内容
   *
   * @param filename specified filename
   * @return 文本内容
   */
  public final String readString(String filename) {
    return DatabaseTestHelper.readString(getClass(), filename);
  }
}
