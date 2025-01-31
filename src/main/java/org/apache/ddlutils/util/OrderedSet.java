package org.apache.ddlutils.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

public class OrderedSet<E> implements Set<E>, List<E>, RandomAccess {

    private transient final Set<E> distinctElements;
    private final List<E> elements;

    public OrderedSet() {
        this(10);
    }

    public OrderedSet(int initialCapacity) {
        elements = new ArrayList<>(initialCapacity);
        distinctElements = new HashSet<>(initialCapacity);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return elements.toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (distinctElements.add(e)) {
            return elements.add(e);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (distinctElements.remove(o)) {
            return elements.remove(o);
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return distinctElements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        for (E element : c) {
            add(element);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        boolean res = false;
        for (E e : c) {
            if (distinctElements.add(e)) {
                res = true;
                elements.add(e);
            }
        }
        return res;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean res = false;
        for (Object o : c) {
            E element = (E) o;
            if (!distinctElements.contains(element)) {
                if (elements.remove(element)) {
                    distinctElements.remove(element);
                    res = true;
                }
            }
        }
        return res;
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    @Override
    public void sort(Comparator<? super E> c) {
        List.super.sort(c);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean res = false;
        for (Object o : c) {
            E element = (E) o;
            if (distinctElements.remove(element)) {
                if (elements.remove(element)) {
                    res = true;
                }
            }
        }
        return res;
    }

    @Override
    public void clear() {
        distinctElements.clear();
        elements.clear();
    }

    /**
     * @return Spliterator
     * @see Set#spliterator()
     */
    @Override
    public Spliterator<E> spliterator() {
        return List.super.spliterator();
    }

    @Override
    public E get(int index) {
        if (index < 0 || index > size() - 1) {
            return null;
        }
        return elements.get(index);
    }

    @Override
    public E set(int index, E element) {
        return elements.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        if (index < 0 || index >= size()) {
            return;
        }
        E _element = elements.get(index);
        if (Objects.equals(_element, element)) {
            return;
        }
        if (distinctElements.contains(element)) {
            return;
        }
        elements.set(index, element);
    }

    @Override
    public E remove(int index) {
        E removedElement = elements.remove(index);
        distinctElements.remove(removedElement);
        return removedElement;
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return elements.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return elements.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return elements.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrderedSet<?>) {
            return elements.equals(((OrderedSet<?>) obj).elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }
}
