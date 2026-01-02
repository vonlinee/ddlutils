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
package org.apache.ddlutils.model;

/**
 * Enumeration for Table Types
 */
public enum TableType {

  /**
   * Local temporary tables
   */
  LOCAL_TEMPORARY("LOCAL TEMPORARY"),

  /**
   * System tables and views
   */
  SYSTEM_TABLE("SYSTEM TABLE"),

  /**
   * System views
   */
  SYSTEM_VIEW("SYSTEM VIEW"),

  /**
   * Tables
   */
  TABLE("TABLE", new String[]{"BASE TABLE"}),

  /**
   * Views
   */
  VIEW("VIEW"),

  /**
   * Unknown
   */
  UNKNOWN("UNKNOWN");

  private final String name;
  private final byte[] nameAsBytes;
  private final String[] synonyms;

  TableType(String tableTypeName) {
    this(tableTypeName, null);
  }

  TableType(String tableTypeName, String[] tableTypeSynonyms) {
    this.name = tableTypeName;
    this.nameAsBytes = tableTypeName.getBytes();
    this.synonyms = tableTypeSynonyms;
  }

  public String getName() {
    return this.name;
  }

  public byte[] asBytes() {
    return this.nameAsBytes;
  }

  public boolean equalsTo(String tableTypeName) {
    return this.name.equalsIgnoreCase(tableTypeName);
  }

  public static TableType getTableTypeEqualTo(String tableTypeName) {
    for (TableType tableType : TableType.values()) {
      if (tableType.equalsTo(tableTypeName)) {
        return tableType;
      }
    }
    return UNKNOWN;
  }

  public boolean compliesWith(String tableTypeName) {
    if (equalsTo(tableTypeName)) {
      return true;
    }
    if (this.synonyms != null) {
      for (String synonym : this.synonyms) {
        if (synonym.equalsIgnoreCase(tableTypeName)) {
          return true;
        }
      }
    }
    return false;
  }

  public static TableType getTableTypeCompliantWith(String tableTypeName) {
    for (TableType tableType : TableType.values()) {
      if (tableType.compliesWith(tableTypeName)) {
        return tableType;
      }
    }
    return UNKNOWN;
  }

}
