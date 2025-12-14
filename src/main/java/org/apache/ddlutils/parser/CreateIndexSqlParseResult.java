package org.apache.ddlutils.parser;

import java.util.List;

public class CreateIndexSqlParseResult {

  private String schemaName;

  private String tableName;

  private String indexName;
  private List<String> indexColumnsNames;
  private List<String> tailParameters;

  public String getIndexName() {
    return indexName;
  }

  public void setIndexName(String indexName) {
    this.indexName = indexName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<String> getTailParameters() {
    return tailParameters;
  }

  public void setTailParameters(List<String> tailParameters) {
    this.tailParameters = tailParameters;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public List<String> getIndexColumnsNames() {
    return indexColumnsNames;
  }

  public void setIndexColumnsNames(List<String> indexColumnsNames) {
    this.indexColumnsNames = indexColumnsNames;
  }
}
