package org.apache.ddlutils.platform;

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

import org.apache.ddlutils.TestPlatformBase;
import org.junit.Test;

/**
 * Tests the SapDB platform.
 *
 * @version $Revision: 231110 $
 */
public class TestSapDbPlatform extends TestPlatformBase {
  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDatabaseName() {
    return BuiltinDriverType.SAPDB.getName();
  }

  /**
   * Tests the column types.
   */
  @Test
  public void testColumnTypes() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"coltype\" CASCADE;\n" +
        "CREATE TABLE \"coltype\"\n" +
        "(\n" +
        "    \"COL_ARRAY\"           LONG BYTE,\n" +
        "    \"COL_BIGINT\"          FIXED(38,0),\n" +
        "    \"COL_BINARY\"          CHAR(254) BYTE,\n" +
        "    \"COL_BIT\"             BOOLEAN,\n" +
        "    \"COL_BLOB\"            LONG BYTE,\n" +
        "    \"COL_BOOLEAN\"         BOOLEAN,\n" +
        "    \"COL_CHAR\"            CHAR(15),\n" +
        "    \"COL_CLOB\"            LONG,\n" +
        "    \"COL_DATALINK\"        LONG BYTE,\n" +
        "    \"COL_DATE\"            DATE,\n" +
        "    \"COL_DECIMAL\"         FIXED(15,3),\n" +
        "    \"COL_DECIMAL_NOSCALE\" FIXED(15,0),\n" +
        "    \"COL_DISTINCT\"        LONG BYTE,\n" +
        "    \"COL_DOUBLE\"          FLOAT(38),\n" +
        "    \"COL_FLOAT\"           FLOAT(38),\n" +
        "    \"COL_INTEGER\"         INTEGER,\n" +
        "    \"COL_JAVA_OBJECT\"     LONG BYTE,\n" +
        "    \"COL_LONGVARBINARY\"   LONG BYTE,\n" +
        "    \"COL_LONGVARCHAR\"     LONG,\n" +
        "    \"COL_NULL\"            LONG BYTE,\n" +
        "    \"COL_NUMERIC\"         FIXED(15,0),\n" +
        "    \"COL_OTHER\"           LONG BYTE,\n" +
        "    \"COL_REAL\"            FLOAT(16),\n" +
        "    \"COL_REF\"             LONG BYTE,\n" +
        "    \"COL_SMALLINT\"        SMALLINT,\n" +
        "    \"COL_STRUCT\"          LONG BYTE,\n" +
        "    \"COL_TIME\"            TIME,\n" +
        "    \"COL_TIMESTAMP\"       TIMESTAMP,\n" +
        "    \"COL_TINYINT\"         SMALLINT,\n" +
        "    \"COL_VARBINARY\"       VARCHAR(15) BYTE,\n" +
        "    \"COL_VARCHAR\"         VARCHAR(15)\n" +
        ");\n",
      getColumnTestDatabaseCreationSql());
  }

  /**
   * Tests the column constraints.
   */
  @Test
  public void testColumnConstraints() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"constraints\" CASCADE;\n" +
        "CREATE TABLE \"constraints\"\n" +
        "(\n" +
        "    \"COL_PK\"               VARCHAR(32),\n" +
        "    \"COL_PK_AUTO_INCR\"     INTEGER DEFAULT SERIAL(1),\n" +
        "    \"COL_NOT_NULL\"         CHAR(100) BYTE NOT NULL,\n" +
        "    \"COL_NOT_NULL_DEFAULT\" FLOAT(38) DEFAULT -2.0 NOT NULL,\n" +
        "    \"COL_DEFAULT\"          CHAR(4) DEFAULT 'test',\n" +
        "    \"COL_AUTO_INCR\"        FIXED(38,0) DEFAULT SERIAL(1),\n" +
        "    PRIMARY KEY (\"COL_PK\", \"COL_PK_AUTO_INCR\")\n" +
        ");\n",
      getConstraintTestDatabaseCreationSql());
  }

  /**
   * Tests the table constraints.
   */
  @Test
  public void testTableConstraints() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "ALTER TABLE \"table3\" DROP FOREIGN KEY \"testfk\";\n" +
        "ALTER TABLE \"table2\" DROP FOREIGN KEY \"table2_FK_COL_FK_COL_FK_2_table1\";\n" +
        "DROP TABLE \"table3\" CASCADE;\n" +
        "DROP TABLE \"table2\" CASCADE;\n" +
        "DROP TABLE \"table1\" CASCADE;\n" +
        "CREATE TABLE \"table1\"\n" +
        "(\n" +
        "    \"COL_PK_1\"    VARCHAR(32) NOT NULL,\n" +
        "    \"COL_PK_2\"    INTEGER,\n" +
        "    \"COL_INDEX_1\" CHAR(100) BYTE NOT NULL,\n" +
        "    \"COL_INDEX_2\" FLOAT(38) NOT NULL,\n" +
        "    \"COL_INDEX_3\" CHAR(4),\n" +
        "    PRIMARY KEY (\"COL_PK_1\", \"COL_PK_2\")\n" +
        ");\n" +
        "CREATE INDEX \"testindex1\" ON \"table1\" (\"COL_INDEX_2\");\n" +
        "CREATE UNIQUE INDEX \"testindex2\" ON \"table1\" (\"COL_INDEX_3\", \"COL_INDEX_1\");\n" +
        "CREATE TABLE \"table2\"\n" +
        "(\n" +
        "    \"COL_PK\"   INTEGER,\n" +
        "    \"COL_FK_1\" INTEGER,\n" +
        "    \"COL_FK_2\" VARCHAR(32) NOT NULL,\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n" +
        "CREATE TABLE \"table3\"\n" +
        "(\n" +
        "    \"COL_PK\" VARCHAR(16),\n" +
        "    \"COL_FK\" INTEGER NOT NULL,\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n" +
        "ALTER TABLE \"table2\" ADD CONSTRAINT \"table2_FK_COL_FK_COL_FK_2_table1\" FOREIGN KEY(\"COL_FK_1\", \"COL_FK_2\") REFERENCES \"table1\" (\"COL_PK_2\", \"COL_PK_1\");\n" +
        "ALTER TABLE \"table3\" ADD CONSTRAINT \"testfk\" FOREIGN KEY(\"COL_FK\") REFERENCES \"table2\" (\"COL_PK\");\n",
      getTableConstraintTestDatabaseCreationSql());
  }

  /**
   * Tests the proper escaping of character sequences where Cloudscape requires it.
   */
  @Test
  public void testCharacterEscaping() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"escapedcharacters\" CASCADE;\n" +
        "CREATE TABLE \"escapedcharacters\"\n" +
        "(\n" +
        "    \"COL_PK\"   INTEGER,\n" +
        "    \"COL_TEXT\" VARCHAR(128) DEFAULT '''',\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n",
      getCharEscapingTestDatabaseCreationSql());
  }
}
