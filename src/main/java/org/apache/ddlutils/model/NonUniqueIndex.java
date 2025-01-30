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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;

/**
 * Represents an index definition for a table.
 *
 * @version $Revision: 289996 $
 */
public class NonUniqueIndex extends IndexImplBase {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isUnique() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public Index getClone() throws ModelException {
    NonUniqueIndex result = new NonUniqueIndex();

    result._name = _name;
    result._columns = (ArrayList<IndexColumn>) _columns.clone();

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equalsIgnoreCase(Index other) {
    if (other instanceof NonUniqueIndex) {
      NonUniqueIndex otherIndex = (NonUniqueIndex) other;
      boolean checkName = (_name != null) && (!_name.isEmpty()) &&
        (otherIndex._name != null) && (!otherIndex._name.isEmpty());

      if ((!checkName || _name.equalsIgnoreCase(otherIndex._name)) &&
        (getColumnCount() == otherIndex.getColumnCount())) {
        for (int idx = 0; idx < getColumnCount(); idx++) {
          if (!getColumn(idx).equalsIgnoreCase(otherIndex.getColumn(idx))) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Index [name=" +
      getName() +
      "; " +
      getColumnCount() +
      " columns]";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toVerboseString() {
    StringBuilder result = new StringBuilder();
    result.append("Index [");
    result.append(getName());
    result.append("] columns:");
    for (int idx = 0; idx < getColumnCount(); idx++) {
      result.append(" ");
      result.append(getColumn(idx).toString());
    }

    return result.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NonUniqueIndex) {
      NonUniqueIndex other = (NonUniqueIndex) obj;
      return new EqualsBuilder().append(_name, other._name)
        .append(_columns, other._columns)
        .isEquals();
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(_name)
      .append(_columns)
      .toHashCode();
  }
}
