package org.apache.ddlutils.platform.oracle;

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
import org.apache.ddlutils.platform.BuiltinDriverType;
import org.junit.Test;

/**
 * Tests the Oracle 9 platform.
 *
 * @version $Revision: 231110 $
 */
public class TestOracle9Platform extends TestPlatformBase {
  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDatabaseName() {
    return BuiltinDriverType.ORACLE9.getName();
  }

  /**
   * Tests the column types.
   */
  @Test
  public void testColumnTypes() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"coltype\" CASCADE CONSTRAINTS;\n" +
        "CREATE TABLE \"coltype\"\n" +
        "(\n" +
        "    \"COL_ARRAY\"           BLOB,\n" +
        "    \"COL_BIGINT\"          NUMBER(38),\n" +
        "    \"COL_BINARY\"          RAW(254),\n" +
        "    \"COL_BIT\"             NUMBER(1),\n" +
        "    \"COL_BLOB\"            BLOB,\n" +
        "    \"COL_BOOLEAN\"         NUMBER(1),\n" +
        "    \"COL_CHAR\"            CHAR(15),\n" +
        "    \"COL_CLOB\"            CLOB,\n" +
        "    \"COL_DATALINK\"        BLOB,\n" +
        "    \"COL_DATE\"            DATE,\n" +
        "    \"COL_DECIMAL\"         NUMBER(15,3),\n" +
        "    \"COL_DECIMAL_NOSCALE\" NUMBER(15,0),\n" +
        "    \"COL_DISTINCT\"        BLOB,\n" +
        "    \"COL_DOUBLE\"          DOUBLE PRECISION,\n" +
        "    \"COL_FLOAT\"           FLOAT,\n" +
        "    \"COL_INTEGER\"         INTEGER,\n" +
        "    \"COL_JAVA_OBJECT\"     BLOB,\n" +
        "    \"COL_LONGVARBINARY\"   BLOB,\n" +
        "    \"COL_LONGVARCHAR\"     CLOB,\n" +
        "    \"COL_NULL\"            BLOB,\n" +
        "    \"COL_NUMERIC\"         NUMBER(15,0),\n" +
        "    \"COL_OTHER\"           BLOB,\n" +
        "    \"COL_REAL\"            REAL,\n" +
        "    \"COL_REF\"             BLOB,\n" +
        "    \"COL_SMALLINT\"        NUMBER(5),\n" +
        "    \"COL_STRUCT\"          BLOB,\n" +
        "    \"COL_TIME\"            DATE,\n" +
        "    \"COL_TIMESTAMP\"       TIMESTAMP,\n" +
        "    \"COL_TINYINT\"         NUMBER(3),\n" +
        "    \"COL_VARBINARY\"       RAW(15),\n" +
        "    \"COL_VARCHAR\"         VARCHAR2(15)\n" +
        ");\n",
      getColumnTestDatabaseCreationSql());
  }

  /**
   * Tests the column constraints.
   */
  @Test
  public void testColumnConstraints() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TRIGGER \"trg_constraints_L_PK_AUTO_INCR\";\n" +
        "DROP SEQUENCE \"seq_constraints_L_PK_AUTO_INCR\";\n" +
        "DROP TRIGGER \"trg_constraints_COL_AUTO_INCR\";\n" +
        "DROP SEQUENCE \"seq_constraints_COL_AUTO_INCR\";\n" +
        "DROP TABLE \"constraints\" CASCADE CONSTRAINTS;\n" +
        "CREATE SEQUENCE \"seq_constraints_L_PK_AUTO_INCR\";\n" +
        "CREATE SEQUENCE \"seq_constraints_COL_AUTO_INCR\";\n" +
        "CREATE TABLE \"constraints\"\n" +
        "(\n" +
        "    \"COL_PK\"               VARCHAR2(32),\n" +
        "    \"COL_PK_AUTO_INCR\"     INTEGER,\n" +
        "    \"COL_NOT_NULL\"         RAW(100) NOT NULL,\n" +
        "    \"COL_NOT_NULL_DEFAULT\" DOUBLE PRECISION DEFAULT -2.0 NOT NULL,\n" +
        "    \"COL_DEFAULT\"          CHAR(4) DEFAULT 'test',\n" +
        "    \"COL_AUTO_INCR\"        NUMBER(38),\n" +
        "    PRIMARY KEY (\"COL_PK\", \"COL_PK_AUTO_INCR\")\n" +
        ");\n" +
        "CREATE OR REPLACE TRIGGER \"trg_constraints_L_PK_AUTO_INCR\" BEFORE INSERT ON \"constraints\" FOR EACH ROW WHEN (new.\"COL_PK_AUTO_INCR\" IS NULL)\n" +
        "BEGIN SELECT \"seq_constraints_L_PK_AUTO_INCR\".nextval INTO :new.\"COL_PK_AUTO_INCR\" FROM dual; END;;\n" +
        "CREATE OR REPLACE TRIGGER \"trg_constraints_COL_AUTO_INCR\" BEFORE INSERT ON \"constraints\" FOR EACH ROW WHEN (new.\"COL_AUTO_INCR\" IS NULL)\n" +
        "BEGIN SELECT \"seq_constraints_COL_AUTO_INCR\".nextval INTO :new.\"COL_AUTO_INCR\" FROM dual; END;;\n",
      getConstraintTestDatabaseCreationSql());
  }

  /**
   * Tests the table constraints.
   */
  @Test
  public void testTableConstraints() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"table3\" CASCADE CONSTRAINTS;\n" +
        "DROP TABLE \"table2\" CASCADE CONSTRAINTS;\n" +
        "DROP TABLE \"table1\" CASCADE CONSTRAINTS;\n" +
        "CREATE TABLE \"table1\"\n" +
        "(\n" +
        "    \"COL_PK_1\"    VARCHAR2(32) NOT NULL,\n" +
        "    \"COL_PK_2\"    INTEGER,\n" +
        "    \"COL_INDEX_1\" RAW(100) NOT NULL,\n" +
        "    \"COL_INDEX_2\" DOUBLE PRECISION NOT NULL,\n" +
        "    \"COL_INDEX_3\" CHAR(4),\n" +
        "    PRIMARY KEY (\"COL_PK_1\", \"COL_PK_2\")\n" +
        ");\n" +
        "CREATE INDEX \"testindex1\" ON \"table1\" (\"COL_INDEX_2\");\n" +
        "CREATE UNIQUE INDEX \"testindex2\" ON \"table1\" (\"COL_INDEX_3\", \"COL_INDEX_1\");\n" +
        "CREATE TABLE \"table2\"\n" +
        "(\n" +
        "    \"COL_PK\"   INTEGER,\n" +
        "    \"COL_FK_1\" INTEGER,\n" +
        "    \"COL_FK_2\" VARCHAR2(32) NOT NULL,\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n" +
        "CREATE TABLE \"table3\"\n" +
        "(\n" +
        "    \"COL_PK\" VARCHAR2(16),\n" +
        "    \"COL_FK\" INTEGER NOT NULL,\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n" +
        "ALTER TABLE \"table2\" ADD CONSTRAINT \"table2_FK_COL_F_OL_FK_2_table1\" FOREIGN KEY (\"COL_FK_1\", \"COL_FK_2\") REFERENCES \"table1\" (\"COL_PK_2\", \"COL_PK_1\");\n" +
        "ALTER TABLE \"table3\" ADD CONSTRAINT \"testfk\" FOREIGN KEY (\"COL_FK\") REFERENCES \"table2\" (\"COL_PK\");\n",
      getTableConstraintTestDatabaseCreationSql());
  }

  /**
   * Tests the proper escaping of character sequences where Oracle requires it.
   */
  @Test
  public void testCharacterEscaping() throws Exception {
    assertEqualsIgnoringWhitespaces(
      "DROP TABLE \"escapedcharacters\" CASCADE CONSTRAINTS;\n" +
        "CREATE TABLE \"escapedcharacters\"\n" +
        "(\n" +
        "    \"COL_PK\"   INTEGER,\n" +
        "    \"COL_TEXT\" VARCHAR2(128) DEFAULT '''',\n" +
        "    PRIMARY KEY (\"COL_PK\")\n" +
        ");\n",
      getCharEscapingTestDatabaseCreationSql());
  }
}
