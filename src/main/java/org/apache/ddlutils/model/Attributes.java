package org.apache.ddlutils.model;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

public class Attributes extends CaseInsensitiveMap<String, Object> {

  public static String ENGINE = "engine";
  public static String AUTO_INCREMENT = "auto_increment";
  public static String CHARSET = "charset";
  public static String TABLE_COLLATION = "table_collation";

  public void set(String name, Object value) {
    put(name, value);
  }

  public Object getOrDefault(String name, Object def) {
    return super.getOrDefault(name, def);
  }
}
