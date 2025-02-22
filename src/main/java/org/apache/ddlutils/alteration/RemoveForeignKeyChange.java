package org.apache.ddlutils.alteration;

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

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;

/**
 * Represents the removal of a foreign key from a table. Note that for
 * simplicity and because it fits the model, this change actually implements
 * table change for the table that the foreign key originates.
 *
 * @version $Revision: $
 */
public class RemoveForeignKeyChange extends ForeignKeyChangeImplBase {
  /**
   * Creates a new change object.
   *
   * @param tableName  The name of the table to remove the foreign key from
   * @param foreignKey The foreign key
   */
  public RemoveForeignKeyChange(String tableName, ForeignKey foreignKey) {
    super(tableName, foreignKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void apply(Database model, boolean caseSensitive) {
    findChangedTable(model, caseSensitive).removeForeignKey(findChangedForeignKey(model, caseSensitive));
  }
}
