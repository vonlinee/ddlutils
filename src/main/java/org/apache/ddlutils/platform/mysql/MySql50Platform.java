package org.apache.ddlutils.platform.mysql;

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

import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.platform.BuiltinDriverType;

/**
 * The platform implementation for MySQL 5 and above.
 *
 * @version $Revision: 231306 $
 */
public class MySql50Platform extends MySqlPlatform {

  /**
   * Creates a new platform instance.
   */
  public MySql50Platform() {
    super();

    PlatformInfo info = getPlatformInfo();

    // MySql 5.0 returns an empty string for default values for pk columns
    // which is different from the MySql 4 behaviour
    info.setSyntheticDefaultValueForRequiredReturned(false);

    setSqlBuilder(new MySql50Builder(this));
    setModelReader(new MySql50ModelReader(this));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return BuiltinDriverType.MYSQL5X.getName();
  }
}
