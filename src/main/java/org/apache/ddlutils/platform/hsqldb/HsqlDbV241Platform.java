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
package org.apache.ddlutils.platform.hsqldb;

import java.sql.JDBCType;
import java.sql.Types;

/**
 * hsqldb v2.4.1
 */
public class HsqlDbV241Platform extends HsqlDbPlatform {

  public HsqlDbV241Platform() {
    super();

    final int targetJdbcType = getPlatformInfo().getTargetJdbcType(JDBCType.BLOB.getVendorTypeNumber());
    if (targetJdbcType == Types.LONGVARBINARY) {
      getPlatformInfo().addNativeTypeMapping(Types.BLOB, "VARBINARY", Types.VARBINARY);
    }

    setSqlBuilder(new HsqlDbV241SqlBuilder(this));
    setModelReader(new HsqlDbV241ModelReader(this));
  }
}
