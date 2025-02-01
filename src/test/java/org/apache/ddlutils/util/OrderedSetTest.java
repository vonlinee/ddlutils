package org.apache.ddlutils.util;

import org.junit.Test;

public class OrderedSetTest {

  @Test
  public void test() {

    OrderedSet<Integer> set = new OrderedSet<>();

    set.add(1);
    set.add(4);
    set.add(2);
    set.add(5);
    set.add(2);
    set.add(3);

    System.out.println(set);

  }
}
