package org.apache.ddlutils.util;

import java.text.Collator;
import java.util.*;
import java.util.function.Function;

public class CollectionUtils {

  public static boolean isNotEmpty(Collection<?> collection) {
    return collection != null && !collection.isEmpty();
  }

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  public static boolean isNotEmpty(Map<?, ?> map) {
    return map != null && !map.isEmpty();
  }

  public static boolean isEmpty(Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  public static <E> void sortString(List<E> list, Function<E, String> mapper) {
    if (isEmpty(list)) {
      return;
    }
    final Collator collator = Collator.getInstance();
    list.sort((obj1, obj2) -> {
      String string1 = mapper.apply(obj1);
      String string2 = mapper.apply(obj2);
      return collator.compare(string1.toUpperCase(), string2.toUpperCase());
    });
  }

  public static <E> void sortString(List<E> list, Function<E, String> mapper, final boolean caseSensitive) {
    if (isEmpty(list)) {
      return;
    }
    final Collator collator = Collator.getInstance();
    list.sort((obj1, obj2) -> {
      String fk1Name = mapper.apply(obj1);
      String fk2Name = mapper.apply(obj2);
      if (!caseSensitive) {
        fk1Name = (fk1Name != null ? fk1Name.toLowerCase() : null);
        fk2Name = (fk2Name != null ? fk2Name.toLowerCase() : null);
      }
      return collator.compare(fk1Name, fk2Name);
    });
  }

  public static <E, T> Set<T> toSet(Collection<E> collection, Function<E, T> mapper) {
    if (isEmpty(collection)) {
      return Collections.emptySet();
    }
    Set<T> set = new HashSet<>();
    for (E e : collection) {
      set.add(mapper.apply(e));
    }
    return set;
  }
}
