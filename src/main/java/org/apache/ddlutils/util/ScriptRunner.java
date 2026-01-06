/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.ddlutils.util;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * see: <a href="https://github.com/BenoitDuffez/ScriptRunner">...</a>
 * <p>
 * Slightly modified version of the com.ibatis.common.jdbc.ScriptRunner class
 * from the iBATIS Apache project. Only removed dependency on Resource class
 * and a constructor
 * GPSHansl, 06.08.2015: regex for delimiter, rearrange comment/delimiter detection, remove some ide warnings.
 */
public class ScriptRunner extends ScriptSqlReader {

  private static final Pattern SOURCE_COMMAND = Pattern.compile("^\\s*SOURCE\\s+(.*?)\\s*$", Pattern.CASE_INSENSITIVE);

  private final Connection connection;

  private final boolean stopOnError;
  private final boolean autoCommit;

  private PrintWriter logWriter = null;
  private PrintWriter errorLogWriter = null;


  private String userDirectory = System.getProperty("user.dir");

  /**
   * Default constructor
   */
  public ScriptRunner(Connection connection, boolean autoCommit,
                      boolean stopOnError) {
    this.connection = connection;
    this.autoCommit = autoCommit;
    this.stopOnError = stopOnError;
    File logFile = new File("create_db.log");
    File errorLogFile = new File("create_db_error.log");
    try {
      if (logFile.exists()) {
        logWriter = new PrintWriter(new FileWriter(logFile, true));
      } else {
        logWriter = new PrintWriter(new FileWriter(logFile, false));
      }
    } catch (IOException e) {
      System.err.println("Unable to access or create the db_create log");
    }
    try {
      if (errorLogFile.exists()) {
        errorLogWriter = new PrintWriter(new FileWriter(errorLogFile, true));
      } else {
        errorLogWriter = new PrintWriter(new FileWriter(errorLogFile, false));
      }
    } catch (IOException e) {
      System.err.println("Unable to access or create the db_create error log");
    }
    final String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
    println("\n-------\n" + timeStamp + "\n-------\n");
    printlnError("\n-------\n" + timeStamp + "\n-------\n");
  }

  public static void runScript(Connection connection, File file) throws IOException, SQLException {
    try (FileReader reader = new FileReader(file)) {
      new ScriptRunner(connection, true, true).runScript(reader);
    }
  }

  /**
   * Setter for logWriter property
   *
   * @param logWriter - the new value of the logWriter property
   */
  public void setLogWriter(PrintWriter logWriter) {
    this.logWriter = logWriter;
  }

  /**
   * Setter for errorLogWriter property
   *
   * @param errorLogWriter - the new value of the errorLogWriter property
   */
  public void setErrorLogWriter(PrintWriter errorLogWriter) {
    this.errorLogWriter = errorLogWriter;
  }

  /**
   * Set the current working directory.  Source commands will be relative to this.
   */
  public void setUserDirectory(String userDirectory) {
    this.userDirectory = userDirectory;
  }

  /**
   * Runs an SQL script (read in using the Reader parameter)
   *
   * @param filepath - the filepath of the script to run. May be relative to the userDirectory.
   */
  public void runScript(String filepath) throws IOException, SQLException {
    File file = new File(userDirectory, filepath);
    this.runScript(new BufferedReader(new FileReader(file)));
  }

  /**
   * Runs an SQL script (read in using the Reader parameter)
   *
   * @param reader - the source of the script
   */
  public void runScript(Reader reader) throws IOException, SQLException {
    try {
      boolean originalAutoCommit = connection.getAutoCommit();
      try {
        if (originalAutoCommit != this.autoCommit) {
          connection.setAutoCommit(this.autoCommit);
        }
        doRunScript(reader);
      } finally {
        connection.setAutoCommit(originalAutoCommit);
      }
    } catch (IOException | SQLException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error running script.  Cause: " + e, e);
    }
  }

  /**
   * Runs an SQL script (read in using the Reader parameter) using the
   * connection passed in
   *
   * @param reader - the source of the script
   * @throws SQLException if any SQL errors occur
   * @throws IOException  if there is an error reading from the Reader
   */
  private void doRunScript(Reader reader) throws IOException, SQLException {
    try {
      read(reader);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      // conn.rollback();
      flush();
    }
  }

  @Override
  protected void handleError(String command, Throwable throwable) throws IOException {
    throw new IOException(String.format("Error executing '%s': %s", command, throwable.getMessage()), throwable);
  }

  @Override
  protected void handleComment(String comment) {
    println(comment);
  }

  @Override
  protected void handleEndOfScript() {
    if (!autoCommit) {
      try {
        connection.commit();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected void handleStatement(String command, LineNumberReader reader) {
    try {
      this.execCommand(connection, command, reader);
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void execCommand(Connection conn, String command,
                           LineNumberReader lineReader) throws IOException, SQLException {
    Matcher sourceCommandMatcher = SOURCE_COMMAND.matcher(command);
    if (sourceCommandMatcher.matches()) {
      String filepath = sourceCommandMatcher.group(1);
      File file = new File(userDirectory, filepath);
      this.doRunScript(new BufferedReader(new FileReader(file)));
      return;
    }
    this.execSqlCommand(conn, command, lineReader);
  }

  private void execSqlCommand(Connection conn, String command,
                              LineNumberReader lineReader) throws SQLException {

    Statement statement = conn.createStatement();
    println(command);
    boolean hasResults = false;
    try {
      hasResults = statement.execute(command);
    } catch (SQLException e) {
      final String errText = String.format("Error executing '%s' (line %d): %s",
        command, lineReader.getLineNumber(), e.getMessage());
      printlnError(errText);
      System.err.println(errText);
      if (stopOnError) {
        throw new SQLException(errText, e);
      }
    }

    if (autoCommit && !conn.getAutoCommit()) {
      conn.commit();
    }

    ResultSet rs = statement.getResultSet();
    if (hasResults && rs != null) {
      ResultSetMetaData md = rs.getMetaData();
      int cols = md.getColumnCount();
      for (int i = 1; i <= cols; i++) {
        String name = md.getColumnLabel(i);
        print(name + "\t");
      }
      println("");
      while (rs.next()) {
        for (int i = 1; i <= cols; i++) {
          String value = rs.getString(i);
          print(value + "\t");
        }
        println("");
      }
    }

    try {
      statement.close();
    } catch (Exception e) {
      // Ignore to work around a bug in Jakarta DBCP
    }
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")

  private void print(Object o) {
    if (logWriter != null) {
      logWriter.print(o);
    }
  }

  private void println(Object o) {
    if (logWriter != null) {
      logWriter.println(o);
    }
  }

  private void printlnError(Object o) {
    if (errorLogWriter != null) {
      errorLogWriter.println(o);
    }
  }

  private void flush() {
    if (logWriter != null) {
      logWriter.flush();
    }
    if (errorLogWriter != null) {
      errorLogWriter.flush();
    }
  }
}
