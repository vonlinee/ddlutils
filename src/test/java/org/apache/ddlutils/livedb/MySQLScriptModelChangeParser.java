package org.apache.ddlutils.livedb;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.drop.Drop;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.util.ScriptSqlReader;

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
    try {
      Statement stmt = CCJSqlParserUtil.parse(command);
      System.out.println("============= " + stmt.getClass() + " ===========================");
      System.out.println(command);
      System.out.println("===================================================================");
      if (stmt instanceof SetStatement) {
        System.out.println("=========== Skip =================");
        return;
      }
      if (stmt instanceof CreateTable) {
        CreateTable createTable = (CreateTable) stmt;
        System.out.println("=========== CreateTable =================");

      } else if (stmt instanceof Drop) {
        System.out.println("=========== Drop Table =============================");
        Drop drop = (Drop) stmt;

      } else if (stmt instanceof CreateSchema) {
        CreateSchema createSchema = (CreateSchema) stmt;
        System.out.println("=========== CreateSchema =================");
      } else {
        System.out.println("=========== Skip =================");
      }
    } catch (JSQLParserException e) {
      System.err.println("======== Error ============================");
      System.err.println(command);
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
