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

import org.apache.ddlutils.alteration.AddColumnChange;
import org.apache.ddlutils.alteration.AddPrimaryKeyChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.alteration.TableChange;
import org.apache.ddlutils.alteration.TableDefinitionChangesPredicate;
import org.apache.ddlutils.model.Table;

import java.util.List;

/**
 * This is the default predicate for filtering supported table definition changes
 * in the {@link ModelComparator}. It is also useful as the base class for platform
 * specific implementations.
 *
 * @version $Revision: $
 */
public class DefaultTableDefinitionChangesPredicate implements TableDefinitionChangesPredicate {
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean areSupported(Table intermediateTable, List<TableChange> changes) {
    for (TableChange change : changes) {
      if (!isSupported(intermediateTable, change)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether the given change is supported.
   *
   * @param intermediateTable The current table to which this change would be applied
   * @param change            The table change
   * @return <code>true</code> if the change is supported
   */
  protected boolean isSupported(Table intermediateTable, TableChange change) {
    if (change instanceof AddColumnChange) {
      AddColumnChange addColumnChange = (AddColumnChange) change;
      return addColumnChange.isAtEnd() &&
        (!addColumnChange.getNewColumn().isRequired() ||
          (addColumnChange.getNewColumn().getDefaultValue() != null) ||
          addColumnChange.getNewColumn().isAutoIncrement());
    } else return change instanceof AddPrimaryKeyChange;
  }
}
