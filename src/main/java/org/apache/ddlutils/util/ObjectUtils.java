package org.apache.ddlutils.util;

public class ObjectUtils {

  public static String[] cloneStringArray(String[] array) {
    if (array == null) {
      return null;
    }
    String[] result = new String[array.length];
    System.arraycopy(array, 0, result, 0, array.length);
    return result;
  }
}
