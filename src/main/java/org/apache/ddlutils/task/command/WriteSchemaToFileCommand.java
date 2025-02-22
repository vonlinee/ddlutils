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

import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.task.DatabaseTask;

import java.io.File;
import java.io.FileWriter;

/**
 * Reads the schema of the live database (as specified in the enclosing task), and writes
 * it as XML to a file.
 *
 * @version $Revision: 289996 $
 * @ant.task name="writeSchemaToFile"
 */
public class WriteSchemaToFileCommand extends Command {
  /**
   * The file to output the schema to.
   */
  private File _outputFile;

  /**
   * Specifies the name of the file to write the schema XML to.
   *
   * @param outputFile The output file
   * @ant.required
   */
  public void setOutputFile(File outputFile) {
    _outputFile = outputFile;
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
    if (_outputFile == null) {
      throw new CommandExecuteException("No output file specified");
    }
    if (_outputFile.exists() && !_outputFile.canWrite()) {
      throw new CommandExecuteException("Cannot overwrite output file " + _outputFile.getAbsolutePath());
    }

    try {
      FileWriter outputWriter = new FileWriter(_outputFile);
      DatabaseIO dbIO = new DatabaseIO();

      dbIO.write(model, outputWriter);
      outputWriter.close();
    } catch (Exception ex) {
      handleException(ex, "Failed to write to output file " + _outputFile.getAbsolutePath());
    }
  }
}
