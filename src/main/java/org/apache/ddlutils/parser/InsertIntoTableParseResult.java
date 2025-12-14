package org.apache.ddlutils.parser;

import java.util.Map;

public class InsertIntoTableParseResult {

  private String schema;
  private String table;
  private Map<String, Object> columnValues;

  public Map<String, Object> getColumnValues() {
    return columnValues;
  }

  public void setColumnValues(Map<String, Object> columnValues) {
    this.columnValues = columnValues;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getTable() {
    return table;
  }

  public void setTable(String table) {
    this.table = table;
  }

  @Override
  public String toString() {
    return "InsertIntoTableParseResult{" +
           "schema='" + schema + '\'' +
           ", table='" + table + '\'' +
           ", columnValues=" + columnValues +
           '}';
  }
}
