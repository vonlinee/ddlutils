package org.apache.ddlutils.sql;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlCreateView;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.util.SqlBasicVisitor;

import java.util.ArrayList;
import java.util.List;

public class CalciteParserDemo extends SqlBaseTest {

  public static SqlNode parseSql(String sql) {
    try {
      SqlParser.Config config = SqlParser.config()
        .withParserFactory(SqlDdlParserImpl.FACTORY) // <--- CRITICAL FIX
        .withLex(Lex.MYSQL);

      SqlParser parser = SqlParser.create(sql, config);
      return parser.parseStmt();
    } catch (SqlParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) {

    SqlNode sqlNode = parseSql(sql1);

    System.out.println("✅ Parsing Successful!");
    System.out.println("AST Class: " + sqlNode.getClass().getSimpleName());

    // 3. Visit
    TableNameExtractor extractor = new TableNameExtractor();
    sqlNode.accept(extractor);

    System.out.println("Tables Found: " + extractor.getTableNames());
  }

  static class TableNameExtractor extends SqlBasicVisitor<Void> {
    private final List<String> tableNames = new ArrayList<>();

    public List<String> getTableNames() {
      return tableNames;
    }

    @Override
    public Void visit(SqlCall call) {
      // Handle CREATE VIEW specifically
      if (call instanceof SqlCreateView) {
        SqlCreateView createView = (SqlCreateView) call;
        // The body of the view is the SELECT statement
        if (createView.query != null) {
          createView.query.accept(this);
        }
        return null;
      }

      // Handle SELECT
      if (call.getKind() == SqlKind.SELECT) {
        SqlSelect select = (SqlSelect) call;
        if (select.getFrom() != null) {
          select.getFrom().accept(this);
        }
        return null;
      }

      // Handle JOIN
      if (call.getKind() == SqlKind.JOIN) {
        SqlJoin join = (SqlJoin) call;
        join.getLeft().accept(this);
        join.getRight().accept(this);
        return null;
      }

      // Handle AS (Alias)
      if (call.getKind() == SqlKind.AS) {
        call.operand(0).accept(this);
        return null;
      }

      return super.visit(call);
    }

    @Override
    public Void visit(SqlIdentifier id) {
      tableNames.add(id.toString());
      return null;
    }
  }
}
