package org.apache.ddlutils.util;

import org.junit.Test;

public class OrderedMapTest {

  @Test
  public void test() {

    OrderedMap<Integer, String> map = new OrderedMap<>();

    map.put(1, "111");
    map.put(3, "53");
    map.put(5, "555");
    map.put(2, "222");

    map.clear();

  }
}
