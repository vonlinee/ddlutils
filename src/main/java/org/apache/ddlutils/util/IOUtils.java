package org.apache.ddlutils.util;

import java.io.PrintWriter;

public class IOUtils {

  public static void printThenFlush(PrintWriter writer, Object message) {
    if (writer != null) {
      writer.print(message);
      writer.flush();
    }
  }

  public static void printlnThenFlush(PrintWriter writer, Object message) {
    if (writer != null) {
      writer.println(message);
      writer.flush();
    }
  }
}
