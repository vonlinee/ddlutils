package org.apache.ddlutils.data;

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

/**
 * SqlDynaBean is a DynaBean which can be persisted as a single row in
 * a Database Table.
 *
 * @version $Revision$
 */
public class SqlRowObject extends BasicRowObject {

  /**
   * Creates a new dyna bean of the given class.
   *
   * @param tableClass The dyna class
   */
  public SqlRowObject(TableClass tableClass) {
    super(tableClass);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    TableClass type = getDynaClass();
    ColumnProperty[] props = type.getDynaProperties();

    result.append(type.getName());
    result.append(": ");
    for (int idx = 0; idx < props.length; idx++) {
      if (idx > 0) {
        result.append(", ");
      }
      result.append(props[idx].getName());
      result.append(" = ");
      result.append(get(props[idx].getName()));
    }
    return result.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SqlRowObject) {
      SqlRowObject other = (SqlRowObject) obj;
      TableClass tableClass = getDynaClass();

      if (tableClass.equals(other.getDynaClass())) {
        ColumnProperty[] props = tableClass.getDynaProperties();

        for (ColumnProperty prop : props) {
          Object value = get(prop.getName());
          Object otherValue = other.get(prop.getName());

          if (value == null) {
            if (otherValue != null) {
              return false;
            }
          } else {
            return value.equals(otherValue);
          }
        }
        return true;
      }
    }
    return false;
  }
}
