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

import java.sql.DatabaseMetaData;
import java.util.Objects;

/**
 * Represents the different cascade actions for the <code>onDelete</code> and
 * <code>onUpdate</code> properties of {@link ForeignKey}.
 *
 * @version $Revision: $
 */
public enum CascadeAction {

  /**
   * The enum value for a cascade action which directs the database to apply the change to
   * the referenced table also to this table. E.g. if the referenced row is deleted, then
   * the local one will also be deleted when this value is used for the onDelete action.
   */
  CASCADE("cascade", 1),
  /**
   * The enum value for a cascade action which directs the database to set the local columns
   * referenced by the foreign key to null when the referenced row changes/is deleted.
   */
  SET_NULL("setnull", 2),
  /**
   * The enum value for a cascade action which directs the database to set the local columns
   * referenced by the foreign key to the default value when the referenced row changes/is deleted.
   */
  SET_DEFAULT("setdefault", 3),
  /**
   * The enum value for a cascade action which directs the database to restrict the change
   * changes to the referenced column. The interpretation of this is database-dependent, but it is
   * usually the same as {@link #NONE}.
   */
  RESTRICT("restrict", 4),
  /**
   * The enum value for the cascade action that directs the database to not change the local column
   * when the value of the referenced column changes, only check the foreign key constraint.
   */
  NONE("none", 5);

  /**
   * Version id for this class as relevant for serialization.
   */
  private static final long serialVersionUID = -6378050861446415790L;
  private final String name;
  private final int value;

  /**
   * Creates a new enum object.
   *
   * @param name  The textual representation
   * @param value The corresponding integer value
   */
  CascadeAction(String name, int value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the enum value that corresponds to the given textual
   * representation.
   *
   * @param defaultTextRep The textual representation
   * @return The enum value
   */
  public static CascadeAction getEnum(String defaultTextRep) {
    for (CascadeAction item : values()) {
      if (Objects.equals(item.name, defaultTextRep)) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the enum value that corresponds to the given integer
   * representation.
   *
   * @param intValue The integer value
   * @return The enum value
   */
  public static CascadeAction getEnum(int intValue) {
    for (CascadeAction item : values()) {
      if (item.value == intValue) {
        return item;
      }
    }
    return null;
  }

  public static CascadeAction valueOfCode(Short jdbcActionValue) {
    if (jdbcActionValue == null) {
      return null;

    }
    CascadeAction action = null;
    switch (jdbcActionValue) {
      case DatabaseMetaData.importedKeyCascade:
        action = CascadeAction.CASCADE;
        break;
      case DatabaseMetaData.importedKeySetNull:
        action = CascadeAction.SET_NULL;
        break;
      case DatabaseMetaData.importedKeySetDefault:
        action = CascadeAction.SET_DEFAULT;
        break;
      case DatabaseMetaData.importedKeyRestrict:
        action = CascadeAction.RESTRICT;
        break;
    }
    return action;
  }

  public String getName() {
    return name;
  }

  public int getValue() {
    return value;
  }
}
