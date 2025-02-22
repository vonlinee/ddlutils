package org.apache.ddlutils.platform.cloudscape;

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

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.platform.SqlBuilder;

import java.io.IOException;

/**
 * The SQL Builder for Cloudscape.
 *
 * @version $Revision$
 */
public class CloudscapeBuilder extends SqlBuilder {
  /**
   * Creates a new builder instance.
   *
   * @param platform The plaftform this builder belongs to
   */
  public CloudscapeBuilder(Platform platform) {
    super(platform);
    addEscapedCharSequence("'", "''");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void writeColumnAutoIncrementStmt(Table table, Column column) throws IOException {
    print("GENERATED ALWAYS AS IDENTITY");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getSelectLastIdentityValues(Table table) {
    return "VALUES IDENTITY_VAL_LOCAL()";
  }
}
