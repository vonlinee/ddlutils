package org.apache.ddlutils.platform.postgresql;

public class PgUtils {

  /**
   * Extracts the default value from a default value spec of the form
   * "-9000000000000000000::bigint".
   *
   * @param defaultValue The default value spec
   * @return The default value
   */
  public static String extractUndelimitedDefaultValue(String defaultValue) {
    if (defaultValue == null) {
      return null;
    }
    int valueEnd = defaultValue.indexOf("::");
    if (valueEnd > 0) {
      return defaultValue.substring(0, valueEnd);
    } else {
      return defaultValue;
    }
  }

  /**
   * Extracts the default value from a default value spec of the form
   * "'some value'::character varying" or "'2000-01-01'::date".
   *
   * @param defaultValue The default value spec
   * @return The default value
   */
  public static String extractDelimitedDefaultValue(String defaultValue) {
    if (defaultValue == null) {
      return null;
    }
    if (defaultValue.startsWith("'")) {
      int valueEnd = defaultValue.indexOf("'::");

      if (valueEnd > 0) {
        return defaultValue.substring("'".length(), valueEnd);
      }
    }
    return defaultValue;
  }
}
