package org.apache.ddlutils.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * null is not allowed.
 *
 * @param <E>
 */
public class OrderedSet<E> extends HashSet<E> implements List<E> {

  /**
   * the stored list.
   */
  @NotNull
  private final List<E> list;

  public OrderedSet() {
    list = new ArrayList<>();
  }

  /**
   * @param index index at which to insert the first element from the
   *              specified collection
   * @param c     collection containing elements to be added to this list
   * @return whether at least add one to current set.
   * @see java.util.AbstractList#addAll(int, Collection)
   */
  @Override
  public boolean addAll(int index, @NotNull Collection<? extends E> c) {
    rangeCheckForAdd(index);
    if (c.isEmpty()) {
      return false;
    }
    boolean modified = false;
    for (E e : c) {
      if (e != null && add(e)) {
        add(index++, e);
        modified = true;
      }
    }
    return modified;
  }

  private void rangeCheckForAdd(int index) {
    if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
  }

  @Override
  public E get(int index) {
    return list.get(index);
  }

  @Override
  public E set(int index, E element) {
    if (index < 0 || index >= size()) {
      // add to last
      add(element);
      return null;
    }
    E remove = list.remove(index);
    if (super.remove(remove)) {
      return remove;
    }
    return null;
  }

  @Override
  public boolean add(E e) {
    if (!super.add(e)) {
      return false;
    }
    list.add(e);
    return true;
  }

  @Override
  public void add(int index, E element) {
    if (0 <= index && index < size()) {
      if (contains(element)) {
        return;
      }
      super.add(element);
      list.add(index, element);
    } else {
      add(element);
    }
  }

  @Override
  public E remove(int index) {
    E removedElement = list.remove(index);
    if (removedElement != null) {
      super.remove(removedElement);
    }
    return removedElement;
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @NotNull
  @Override
  public ListIterator<E> listIterator() {
    return list.listIterator();
  }

  @NotNull
  @Override
  public ListIterator<E> listIterator(int index) {
    return list.listIterator(index);
  }

  @NotNull
  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  /**
   * same method in HashSet not annotated with @NotNull in return value.
   *
   * @return Iterator
   */
  @Override
  @NotNull
  public Iterator<E> iterator() {
    return list.iterator();
  }

  @Override
  public boolean remove(Object o) {
    return super.remove(o) && list.remove(o);
  }

  /**
   * same method in HashSet not annotated with @NotNull on parameter c.
   *
   * @return Iterator
   */
  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    if (c.isEmpty()) {
      return false;
    }
    Objects.requireNonNull(c);
    boolean modified = false;

    if (size() > c.size()) {
      for (Object object : c) modified |= remove(object);
    } else {
      for (Iterator<?> i = iterator(); i.hasNext(); ) {
        if (c.contains(i.next())) {
          i.remove();
          modified = true;
        }
      }
    }
    return modified;
  }
}
