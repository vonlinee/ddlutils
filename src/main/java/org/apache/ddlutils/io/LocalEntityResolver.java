package org.apache.ddlutils.io;

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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * An entity resolver that matches the specific database dtds to the one that comes
 * with DdlUtils, and that can handle file url's.
 *
 * @version $Revision$
 */
public class LocalEntityResolver implements EntityResolver {
  /**
   * The default DTD.
   */
  public static final String DTD_PREFIX = "http://db.apache.org/torque/dtd/database";

  /**
   * {@inheritDoc}
   */
  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
    InputSource result = null;

    if (systemId.startsWith(DTD_PREFIX)) {
      InputStream input = getClass().getResourceAsStream("/database.dtd");

      if (input != null) {
        result = new InputSource(input);
      }
    } else if (systemId.startsWith("file:")) {
      try {
        URL url = new URL(systemId);

        if ("file".equals(url.getProtocol())) {
          String path = systemId.substring("file:".length());

          if (path.startsWith("//")) {
            path = path.substring(2);
          }
          result = new InputSource(Files.newInputStream(Paths.get(path)));
        } else {
          result = new InputSource(url.openStream());
        }
      } catch (Exception ex) {
        throw new SAXException(ex);
      }
    }
    return result;
  }
}
