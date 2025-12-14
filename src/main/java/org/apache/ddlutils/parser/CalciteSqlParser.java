package org.apache.ddlutils.parser;

import com.google.common.collect.Sets;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.util.NlsString;
import org.apache.commons.collections4.map.ListOrderedMap;

public class CalciteSqlParser implements SqlParser {

  private static final org.apache.calcite.sql.parser.SqlParser.Config parserConfig = org.apache.calcite.sql.parser.SqlParser.config()
    .withLex(Lex.MYSQL)
    .withCaseSensitive(false)
    .withQuoting(Quoting.BACK_TICK)
    .withQuotedCasing(Casing.UNCHANGED)
    .withUnquotedCasing(Casing.UNCHANGED)
    .withConformance(SqlConformanceEnum.MYSQL_5)
    .withIdentifierMaxLength(256);

  @Override
  public SelectSqlParseResult parseSelectSql(String sql) throws SqlParseException {
    org.apache.calcite.sql.parser.SqlParser parser = org.apache.calcite.sql.parser.SqlParser.create(sql, parserConfig);
    try {
      SqlNode sqlNode = parser.parseStmt();

      System.out.println(sqlNode);

    } catch (org.apache.calcite.sql.parser.SqlParseException e) {
      throw new SqlParseException(sql, e.getMessage(), e);
    }
    return null;
  }

  protected String handleSql(String sql) {
    if (sql.endsWith(";")) {
      // not supported
      sql = sql.substring(0, sql.length() - 1);
    }
    return sql;
  }

  @Override
  public InsertIntoTableParseResult parseInsertSql(String sql) {
    sql = handleSql(sql);
    org.apache.calcite.sql.parser.SqlParser parser = org.apache.calcite.sql.parser.SqlParser.create(sql, parserConfig);
    try {
      SqlNode sqlNode = parser.parseStmt();
      if (!sqlNode.isA(Sets.newHashSet(SqlKind.INSERT))) {
        throw new SqlParseException(sql, "not insert sql");
      }
      SingleInsertTableSqlResultExtractor tableExtractor = new SingleInsertTableSqlResultExtractor();
      sqlNode.accept(tableExtractor);
      return tableExtractor.result;
    } catch (org.apache.calcite.sql.parser.SqlParseException e) {
      throw new SqlParseException(sql, e.getMessage(), e);
    }
  }

  @Override
  public AlterTableSqlParseResult parseAlterTableSql(String sql) throws SqlParseException {
    return null;
  }

  @Override
  public CreateIndexSqlParseResult parseCreateIndexSql(String sql) throws SqlParseException {
    return null;
  }

  /**
   * 自定义 SqlNode 访问器：提取查询中涉及的表名
   */
  static class SingleInsertTableSqlResultExtractor extends SqlBasicVisitor<Void> {

    InsertIntoTableParseResult result = new InsertIntoTableParseResult();

    @Override
    public Void visit(SqlCall call) {
      if (call.getKind() == SqlKind.INSERT) {
        SqlInsert insert = (SqlInsert) call;

        SqlNode targetTable = insert.getTargetTable();
        if (targetTable.isA(Sets.newHashSet(SqlKind.IDENTIFIER))) {
          SqlIdentifier identifier = (SqlIdentifier) targetTable;
          result.setTable(identifier.getSimple());
        }

        ListOrderedMap<String, Object> columnValues = new ListOrderedMap<>();
        SqlNodeList targetColumnList = insert.getTargetColumnList();
        if (targetColumnList != null) {
          for (SqlNode sqlNode : targetColumnList) {
            SqlKind kind = sqlNode.getKind();
            if (kind == SqlKind.IDENTIFIER) {
              SqlIdentifier identifier = (SqlIdentifier) sqlNode;
              columnValues.put(identifier.getSimple(), null);
            }
          }
        }
        final boolean hasColumnNames = !columnValues.isEmpty();
        SqlNode source = insert.getSource();
        if (source.isA(Sets.newHashSet(SqlKind.VALUES))) {
          SqlBasicCall values = (SqlBasicCall) source;
          for (SqlNode operand : values.getOperandList()) {
            if (operand.isA(Sets.newHashSet(SqlKind.ROW))) {
              SqlBasicCall basicCall = (SqlBasicCall) operand;
              int colIndex = 0;
              for (SqlNode sqlNode : basicCall.getOperandList()) {
                if (sqlNode.isA(Sets.newHashSet(SqlKind.LITERAL))) {
                  SqlLiteral literal = (SqlLiteral) sqlNode;
                  if (hasColumnNames) {
                    columnValues.setValue(colIndex, getSimpleLiteralValue(literal));
                  } else {
                    columnValues.put(colIndex + "", getSimpleLiteralValue(literal));
                  }
                  colIndex++;
                }
              }
            }
          }
        }
        result.setColumnValues(columnValues);
      }
      return super.visit(call);
    }
  }

  protected static Object getSimpleLiteralValue(SqlLiteral literal) {
    Object value = literal.getValue();
    if (value instanceof NlsString) {
      return ((NlsString) value).getValue();
    }
    return value;
  }
}
