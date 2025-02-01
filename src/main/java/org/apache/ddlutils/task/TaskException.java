package org.apache.ddlutils.task;

public class TaskException extends Exception {

  public TaskException(String message, Throwable cause) {
    super(message, cause);
  }

  public TaskException(String message) {
    super(message);
  }

  public TaskException(Throwable cause) {
    super(cause);
  }
}
