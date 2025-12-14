package org.apache.ddlutils.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.Index;

import java.util.List;

public class GithubJSqlParser implements SqlParser {

  @Override
  public SelectSqlParseResult parseSelectSql(String sql) throws SqlParseException {
    return null;
  }

  @Override
  public InsertIntoTableParseResult parseInsertSql(String sql) throws SqlParseException {
    return null;
  }

  @Override
  public AlterTableSqlParseResult parseAlterTableSql(String sql) throws SqlParseException {
    try {
      Statement statement = CCJSqlParserUtil.parse(sql);
      if (statement instanceof Alter) {
        Alter alter = (Alter) statement;
      }
    } catch (JSQLParserException e) {
      throw new SqlParseException(sql, e.getMessage(), e);
    }
    return null;
  }

  @Override
  public CreateIndexSqlParseResult parseCreateIndexSql(String sql) throws SqlParseException {
    try {
      Statement stmt = CCJSqlParserUtil.parse(sql);
      CreateIndexSqlParseResult result = new CreateIndexSqlParseResult();
      if (stmt instanceof CreateIndex) {
        CreateIndex createIndex = (CreateIndex) stmt;

        Table table = createIndex.getTable();
        result.setSchemaName(table.getSchemaName());
        result.setTableName(table.getName());

        Index index = createIndex.getIndex();
        result.setIndexName(index.getName());
        result.setIndexColumnsNames(index.getColumnsNames());

        List<String> tailParameters = createIndex.getTailParameters();
        result.setTailParameters(tailParameters);
      }
      return result;
    } catch (JSQLParserException e) {
      throw new RuntimeException(e);
    }
  }
}
