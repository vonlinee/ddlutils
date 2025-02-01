package org.apache.ddlutils.task;

import java.util.Properties;

public abstract class Task {

  /**
   * Message priority of &quot;error&quot;.
   */
  public static final int MSG_ERR = 0;
  /**
   * Message priority of &quot;warning&quot;.
   */
  public static final int MSG_WARN = 1;
  /**
   * Message priority of &quot;information&quot;.
   */
  public static final int MSG_INFO = 2;
  /**
   * Message priority of &quot;verbose&quot;.
   */
  public static final int MSG_VERBOSE = 3;
  /**
   * Message priority of &quot;debug&quot;.
   */
  public static final int MSG_DEBUG = 4;


  private final Properties properties = new Properties();

  public final void log(String message, int level) {

  }

  public final void setProperty(String name, String value) {
    this.properties.setProperty(name, value);
  }

  public final String getProperty(String name, String defaultValue) {
    return properties.getProperty(name, defaultValue);
  }

  public final String getProperty(String name) {
    return properties.getProperty(name);
  }

  public abstract void execute() throws TaskException;
}
