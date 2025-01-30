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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents the different cascade actions for the <code>onDelete</code> and
 * <code>onUpdate</code> properties of {@link ForeignKey}.
 *
 * @version $Revision: $
 */
public enum CascadeActionEnum {

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

  private final String name;
  private final int value;

  public String getName() {
    return name;
  }

  public int getValue() {
    return value;
  }

  /**
   * Creates a new enum object.
   *
   * @param name  The textual representation
   * @param value The corresponding integer value
   */
  CascadeActionEnum(String name, int value) {
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
  public static CascadeActionEnum getEnum(String defaultTextRep) {
    for (CascadeActionEnum item : values()) {
      if (Objects.equals(item.name, defaultTextRep)) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns a list of all enum values.
   *
   * @return The list of enum values
   */
  public static List<CascadeActionEnum> getEnumList() {
    return Arrays.asList(values());
  }

  /**
   * Returns an iterator of all enum values.
   *
   * @return The iterator
   */
  public static Iterator<CascadeActionEnum> iterator() {
    return getEnumList().iterator();
  }


}
