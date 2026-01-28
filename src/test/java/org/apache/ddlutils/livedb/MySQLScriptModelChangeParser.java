package org.apache.ddlutils.livedb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.drop.Drop;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.sql.SqlCharsetSanitizer;
import org.apache.ddlutils.util.ScriptSqlReader;
import org.apache.ddlutils.util.StringUtilsExt;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class MySQLScriptModelChangeParser extends ScriptSqlReader {

  private final File file;

  private final List<ModelChange> modelChanges = new ArrayList<>();

  public MySQLScriptModelChangeParser(File file) {
    this.file = file;
  }

  @Override
  protected void handleStatement(String command, LineNumberReader reader) {
    command = command.trim();

    command = SqlCharsetSanitizer.sanitize(command);
    try {
      Statement stmt = CCJSqlParserUtil.parse(command);
      if (stmt instanceof SetStatement) {
        return;
      }
      if (stmt instanceof CreateTable) {
        CreateTable createTable = (CreateTable) stmt;

      } else if (stmt instanceof Drop) {
        Drop drop = (Drop) stmt;

      } else if (stmt instanceof CreateSchema) {
        CreateSchema createSchema = (CreateSchema) stmt;
      } else {

      }
    } catch (JSQLParserException e) {
      System.out.println(command);
      throw new RuntimeException(e);
    }
  }

  public List<ModelChange> parse() throws IOException {
    try (FileReader reader = new FileReader(file)) {
      read(reader);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return modelChanges;
  }
}
