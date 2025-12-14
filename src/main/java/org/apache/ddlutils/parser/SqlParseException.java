package org.apache.ddlutils.parser;

public class SqlParseException extends RuntimeException {

  private static final long serialVersionUID = -1885017748859097001L;

  private final String sql;

  public SqlParseException(String sql, String message) {
    super(message);
    this.sql = sql;
  }

  public SqlParseException(String sql, String message, Throwable cause) {
    super(message, cause);
    this.sql = sql;
  }
}
