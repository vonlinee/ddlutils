package org.apache.ddlutils.task.command;

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
import org.apache.ddlutils.task.DatabaseTask;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Reads the data currently in the table in the live database (as specified by the
 * enclosing task), and writes it as XML to a file.
 *
 * @version $Revision: 289996 $
 * @ant.task name="writeDataToFile"
 */
public class WriteDataToFileCommand extends ConvertingDatabaseCommand {
  /**
   * The file to output the data to.
   */
  private File _outputFile;
  /**
   * The character encoding to use.
   */
  private String _encoding;

  /**
   * Whether DdlUtils should search for the schema of the tables. @deprecated
   */
  private boolean _determineSchema;

  public WriteDataToFileCommand(Properties properties) {
    super(properties);
  }

  /**
   * Specifies the file to write the data XML to.
   *
   * @param outputFile The output file
   * @ant.required
   */
  public void setOutputFile(File outputFile) {
    _outputFile = outputFile;
  }

  /**
   * Specifies the encoding of the XML file.
   *
   * @param encoding The encoding
   * @ant.not-required The default encoding is <code>UTF-8</code>.
   */
  public void setEncoding(String encoding) {
    _encoding = encoding;
  }

  /**
   * Specifies whether DdlUtils should try to find the schema of the tables when reading data
   * from a live database.
   *
   * @param determineSchema Whether to try to find the table's schemas
   * @deprecated Will be removed once proper schema support is in place
   */
  public void setDetermineSchema(boolean determineSchema) {
    _determineSchema = determineSchema;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRequiringModel() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(DatabaseTask task, Database model) throws CommandExecuteException {
    try {
      getDataIO().setDetermineSchema(_determineSchema);
      getDataIO().writeDataToXML(getPlatform(), model,
        Files.newOutputStream(_outputFile.toPath()), _encoding);
    } catch (Exception ex) {
      handleException(ex, ex.getMessage());
    }
  }

}
