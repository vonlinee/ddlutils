package org.apache.ddlutils.parser;

public interface SqlParser {

  SelectSqlParseResult parseSelectSql(String sql) throws SqlParseException;

  InsertIntoTableParseResult parseInsertSql(String sql) throws SqlParseException;

  AlterTableSqlParseResult parseAlterTableSql(String sql) throws SqlParseException;

  CreateIndexSqlParseResult parseCreateIndexSql(String sql) throws SqlParseException;
}
