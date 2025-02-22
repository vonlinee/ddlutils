package org.apache.ddlutils.task;

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
import org.apache.ddlutils.task.command.CreateDatabaseCommand;
import org.apache.ddlutils.task.command.DropDatabaseCommand;
import org.apache.ddlutils.task.command.WriteDataToDatabaseCommand;
import org.apache.ddlutils.task.command.WriteDataToFileCommand;
import org.apache.ddlutils.task.command.WriteDtdToFileCommand;
import org.apache.ddlutils.task.command.WriteSchemaSqlToFileCommand;
import org.apache.ddlutils.task.command.WriteSchemaToDatabaseCommand;

import java.io.File;
import java.util.List;

/**
 * Task for performing operations on a live database. Sub-tasks e.g. create the
 * schema in the database, drop database schemas, insert data into the database,
 * create DTDs for data files, or write the SQL for creating a schema to a file.
 * <br/>
 * Example:<br/>
 * <pre>
 * &lt;taskdef classname="org.apache.ddlutils.task.DdlToDatabaseTask"
 *          name="ddlToDatabase"
 *          classpathref="project-classpath"/&gt;
 *
 * &lt;ddlToDatabase usedelimitedsqlidentifiers="true"&gt;
 *   &lt;database driverclassname="org.apache.derby.jdbc.ClientDriver"
 *             url="jdbc:derby://localhost/ddlutils"
 *             username="ddlutils"
 *             password="ddlutils"/&gt;
 *   &lt;fileset dir="."&gt;
 *     &lt;include name="*schema.xml"/&gt;
 *   &lt;/fileset&gt;
 *
 *   &lt;createdatabase failonerror="false"/&gt;
 *   &lt;writeschematodatabase alterdatabase="true"
 *                          failonerror="false"/&gt;
 *   &lt;writedatatodatabase datafile="data.xml"
 *                        usebatchmode="true"
 *                        batchsize="1000"/&gt;
 * &lt;/ddlToDatabase&gt;
 * </pre>
 * This Ant build file snippet essentially creates a database, creates tables, foreign keys
 * etc. int it and then writes data into the newly created tables.
 *
 * @version $Revision: 289996 $
 * @ant.task name="ddlToDatabase"
 */
public class DdlToDatabaseTask extends DatabaseTask {
  /**
   * A single schema file to read.
   */
  private File _singleSchemaFile = null;

  /**
   * Whether XML input files are validated against the internal or an external DTD.
   */
  private boolean _useInternalDtd = true;
  /**
   * Whether XML input files are validated at all.
   */
  private boolean _validateXml = false;

  /**
   * Specifies whether DdlUtils shall use the embedded DTD for validating the schema XML (if
   * it matches <code><a href="http://db.apache.org/torque/dtd/database.dtd">...</a></code>). This is
   * especially useful in environments where no web access is possible or desired.
   *
   * @param useInternalDtd <code>true</code> if input files are to be validated against the internal DTD
   * @ant.not-required Default is <code>true</code>.
   */
  public void setUseInternalDtd(boolean useInternalDtd) {
    _useInternalDtd = useInternalDtd;
  }

  /**
   * Specifies whether XML input files should be validated against the DTD at all.
   *
   * @param validateXml <code>true</code> if input files are to be validated
   * @ant.not-required Default is <code>false</code> meaning that the XML is not validated at all.
   */
  public void setValidateXml(boolean validateXml) {
    _validateXml = validateXml;
  }


  /**
   * Defines the single file that contains the database file. You can use this instead of embedded
   * <code>fileset</code> elements if you only have one schema file.
   *
   * @param schemaFile The schema
   * @ant.not-required Use either this or one or more embedded fileset elements.
   */
  public void setSchemaFile(File schemaFile) {
    _singleSchemaFile = schemaFile;
  }

  /**
   * Adds the "create database"-command.
   *
   * @param command The command
   */
  public void addCreateDatabase(CreateDatabaseCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "drop database"-command.
   *
   * @param command The command
   */
  public void addDropDatabase(DropDatabaseCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "write dtd to file"-command.
   *
   * @param command The command
   */
  public void addWriteDtdToFile(WriteDtdToFileCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "write schema to database"-command.
   *
   * @param command The command
   */
  public void addWriteSchemaToDatabase(WriteSchemaToDatabaseCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "write schema sql to file"-command.
   *
   * @param command The command
   */
  public void addWriteSchemaSqlToFile(WriteSchemaSqlToFileCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "write data to database"-command.
   *
   * @param command The command
   */
  public void addWriteDataToDatabase(WriteDataToDatabaseCommand command) {
    addCommand(command);
  }

  /**
   * Adds the "write data to file"-command.
   *
   * @param command The command
   */
  public void addWriteDataToFile(WriteDataToFileCommand command) {
    addCommand(command);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Database readModel() throws TaskException {
    DatabaseIO reader = new DatabaseIO();
    Database model = null;

    reader.setValidateXml(_validateXml);
    reader.setUseInternalDtd(_useInternalDtd);
    if (_singleSchemaFile != null) {
      model = readSingleSchemaFile(reader, _singleSchemaFile);
    } else {
      List<File> files = env.getFiles();

      for (File file : files) {
        Database curModel = readSingleSchemaFile(reader, file);

        if (model == null) {
          model = curModel;
        } else if (curModel != null) {
          try {
            model.mergeWith(curModel);
          } catch (IllegalArgumentException ex) {
            throw new TaskException("Could not merge with schema from file " + file + ": " + ex.getLocalizedMessage(), ex);
          }
        }
      }
    }
    return model;
  }

  /**
   * Reads a single schema file.
   *
   * @param reader     The schema reader
   * @param schemaFile The schema file
   * @return The model
   */
  private Database readSingleSchemaFile(DatabaseIO reader, File schemaFile) throws TaskException {
    Database model;
    if (!schemaFile.isFile()) {
      throw new TaskException("Path " + schemaFile.getAbsolutePath() + " does not denote a file");
    } else if (!schemaFile.canRead()) {
      throw new TaskException("Could not read schema file " + schemaFile.getAbsolutePath());
    } else {
      try {
        model = reader.read(schemaFile);
      } catch (Exception ex) {
        throw new TaskException("Could not read schema file " + schemaFile.getAbsolutePath() + ": " + ex.getLocalizedMessage(), ex);
      }
    }
    return model;
  }
}
