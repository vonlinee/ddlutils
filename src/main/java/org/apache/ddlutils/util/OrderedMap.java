package org.apache.ddlutils.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderedMap<K, V> extends HashMap<K, V> {

  /**
   * Internal list to hold the sequence of objects
   */
  private final List<K> orderedKeys;

  /**
   * Constructs a new empty <code>ListOrderedMap</code> that decorates
   * a <code>HashMap</code>.
   *
   * @since 3.1
   */
  public OrderedMap() {
    orderedKeys = new ArrayList<>();
  }

  //-----------------------------------------------------------------------
  @Override
  public V put(final K key, final V value) {
    if (super.containsKey(key)) {
      // re-adding doesn't change order
      return super.put(key, value);
    }
    // first add, so add to both map and list
    final V result = super.put(key, value);
    orderedKeys.add(key);
    return result;
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Puts the values contained in a supplied Map into the Map starting at
   * the specified index.
   *
   * @param index the index in the Map to start at.
   * @param map   the Map containing the entries to be added.
   * @throws IndexOutOfBoundsException if the index is out of range [0, size]
   */
  public void putAll(int index, final Map<? extends K, ? extends V> map) {
    if (index < 0 || index > orderedKeys.size()) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + orderedKeys.size());
    }
    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      final K key = entry.getKey();
      final boolean contains = containsKey(key);
      // The return value of put is null if the key did not exist OR the value was null,
      // so it cannot be used to determine whether the key was added
      put(index, entry.getKey(), entry.getValue());
      if (!contains) {
        // if no key was replaced, increment the index
        index++;
      } else {
        // otherwise put the next item after the currently inserted key
        index = indexOf(entry.getKey()) + 1;
      }
    }
  }

  @Override
  public V remove(final Object key) {
    V result = null;
    if (super.containsKey(key)) {
      result = super.remove(key);
      orderedKeys.remove(key);
    }
    return result;
  }

  @Override
  public void clear() {
    super.clear();
    orderedKeys.clear();
  }

  //-----------------------------------------------------------------------

  /**
   * Gets a view over the keys in the map.
   * <p>
   * The Collection will be ordered by object insertion into the map.
   *
   * @return the fully modifiable collection view over the keys
   */
  @Override
  public @NotNull Set<K> keySet() {
    return new KeySetView<>(this);
  }

  /**
   * Gets a view over the values in the map.
   * <p>
   * The Collection will be ordered by object insertion into the map.
   * <p>
   *
   * @return the fully modifiable collection view over the values
   */
  @Override
  public @NotNull Collection<V> values() {
    return new ValuesView<>(this);
  }

  /**
   * Gets a view over the entries in the map.
   * <p>
   * The Set will be ordered by object insertion into the map.
   *
   * @return the fully modifiable set view over the entries
   */
  @Override
  public @NotNull Set<Map.Entry<K, V>> entrySet() {
    return new EntrySetView<>(this, this.orderedKeys);
  }

  //-----------------------------------------------------------------------

  /**
   * Returns the Map as a string.
   *
   * @return the Map as a String
   */
  @Override
  public String toString() {
    if (isEmpty()) {
      return "{}";
    }
    final StringBuilder buf = new StringBuilder();
    buf.append('{');
    boolean first = true;
    for (final Map.Entry<K, V> entry : entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      if (first) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append(key == this ? "(this Map)" : key);
      buf.append('=');
      buf.append(value == this ? "(this Map)" : value);
    }
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------

  /**
   * Gets the key at the specified index.
   *
   * @param index the index to retrieve
   * @return the key at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public K get(final int index) {
    return orderedKeys.get(index);
  }

  /**
   * Gets the value at the specified index.
   *
   * @param index the index to retrieve
   * @return the key at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public V getValue(final int index) {
    return get(orderedKeys.get(index));
  }

  /**
   * Gets the index of the specified key.
   *
   * @param key the key to find the index of
   * @return the index, or -1 if not found
   */
  public int indexOf(final Object key) {
    return orderedKeys.indexOf(key);
  }

  /**
   * Sets the value at the specified index.
   *
   * @param index the index of the value to set
   * @param value the new value to set
   * @return the previous value at that index
   * @throws IndexOutOfBoundsException if the index is invalid
   * @since 3.2
   */
  public V setValue(final int index, final V value) {
    final K key = orderedKeys.get(index);
    return put(key, value);
  }

  /**
   * Puts a key-value mapping into the map at the specified index.
   * <p>
   * If the map already contains the key, then the original mapping
   * is removed and the new mapping added at the specified index.
   * The remove may change the effect of the index. The index is
   * always calculated relative to the original state of the map.
   * <p>
   * Thus, the steps are: (1) remove the existing key-value mapping,
   * then (2) insert the new key-value mapping at the position it
   * would have been inserted had the remove not occurred.
   *
   * @param index the index at which the mapping should be inserted
   * @param key   the key
   * @param value the value
   * @return the value previously mapped to the key
   * @throws IndexOutOfBoundsException if the index is out of range [0, size]
   */
  public V put(int index, final K key, final V value) {
    if (index < 0 || index > orderedKeys.size()) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + orderedKeys.size());
    }

    final Map<K, V> m = this;
    if (m.containsKey(key)) {
      final V result = m.remove(key);
      final int pos = orderedKeys.indexOf(key);
      orderedKeys.remove(pos);
      if (pos < index) {
        index--;
      }
      orderedKeys.add(index, key);
      m.put(key, value);
      return result;
    }
    orderedKeys.add(index, key);
    m.put(key, value);
    return null;
  }

  /**
   * Removes the element at the specified index.
   *
   * @param index the index of the object to remove
   * @return the removed value, or <code>null</code> if none existed
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public V remove(final int index) {
    return remove(get(index));
  }

  //-----------------------------------------------------------------------
  static class ValuesView<V> extends AbstractList<V> {
    private final OrderedMap<Object, V> parent;

    @SuppressWarnings("unchecked")
    ValuesView(final OrderedMap<?, V> parent) {
      super();
      this.parent = (OrderedMap<Object, V>) parent;
    }

    @Override
    public int size() {
      return this.parent.size();
    }

    @Override
    public boolean contains(final Object value) {
      return this.parent.containsValue(value);
    }

    @Override
    public void clear() {
      this.parent.clear();
    }

    @Override
    public @NotNull Iterator<V> iterator() {
      return new UntypedIteratorDecorator<Entry<Object, V>, V>(parent.entrySet().iterator()) {
        @Override
        public V next() {
          return getIterator().next().getValue();
        }
      };
    }

    @Override
    public V get(final int index) {
      return this.parent.getValue(index);
    }

    @Override
    public V set(final int index, final V value) {
      return this.parent.setValue(index, value);
    }

    @Override
    public V remove(final int index) {
      return this.parent.remove(index);
    }
  }

  //-----------------------------------------------------------------------
  static class KeySetView<K> extends AbstractSet<K> {
    private final OrderedMap<K, Object> parent;

    @SuppressWarnings("unchecked")
    KeySetView(final OrderedMap<K, ?> parent) {
      super();
      this.parent = (OrderedMap<K, Object>) parent;
    }

    @Override
    public int size() {
      return this.parent.size();
    }

    @Override
    public boolean contains(final Object value) {
      return this.parent.containsKey(value);
    }

    @Override
    public void clear() {
      this.parent.clear();
    }

    @Override
    public @NotNull Iterator<K> iterator() {
      return new UntypedIteratorDecorator<Map.Entry<K, Object>, K>(parent.entrySet().iterator()) {
        @Override
        public K next() {
          return getIterator().next().getKey();
        }
      };
    }
  }

  //-----------------------------------------------------------------------
  static class EntrySetView<K, V> extends AbstractSet<Map.Entry<K, V>> {
    private final OrderedMap<K, V> parent;
    private final List<K> insertOrder;
    private Set<Map.Entry<K, V>> entrySet;

    public EntrySetView(final OrderedMap<K, V> parent, final List<K> insertOrder) {
      super();
      this.parent = parent;
      this.insertOrder = insertOrder;
    }

    private Set<Map.Entry<K, V>> getEntrySet() {
      if (entrySet == null) {
        entrySet = parent.entrySet();
      }
      return entrySet;
    }

    @Override
    public int size() {
      return this.parent.size();
    }

    @Override
    public boolean isEmpty() {
      return this.parent.isEmpty();
    }

    @Override
    public boolean contains(final Object obj) {
      return getEntrySet().contains(obj);
    }

    @Override
    public boolean containsAll(final @NotNull Collection<?> coll) {
      return getEntrySet().containsAll(coll);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object obj) {
      if (!(obj instanceof Map.Entry)) {
        return false;
      }
      if (getEntrySet().contains(obj)) {
        final Object key = ((Map.Entry<K, V>) obj).getKey();
        parent.remove(key);
        return true;
      }
      return false;
    }

    @Override
    public void clear() {
      this.parent.clear();
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      return getEntrySet().equals(obj);
    }

    @Override
    public int hashCode() {
      return getEntrySet().hashCode();
    }

    @Override
    public String toString() {
      return getEntrySet().toString();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
      return new ListOrderedIterator<>(parent, insertOrder);
    }
  }

  //-----------------------------------------------------------------------
  static class ListOrderedIterator<K, V> extends UntypedIteratorDecorator<K, Map.Entry<K, V>> {
    private final OrderedMap<K, V> parent;
    private K last = null;

    ListOrderedIterator(final OrderedMap<K, V> parent, final List<K> insertOrder) {
      super(insertOrder.iterator());
      this.parent = parent;
    }

    @Override
    public Map.Entry<K, V> next() {
      last = getIterator().next();
      return new ListOrderedMapEntry<>(parent, last);
    }

    @Override
    public void remove() {
      super.remove();
      parent.remove(last);
    }
  }

  //-----------------------------------------------------------------------
  static class ListOrderedMapEntry<K, V> implements Map.Entry<K, V> {
    private final OrderedMap<K, V> parent;
    /**
     * The key
     */
    private K key;
    /**
     * The value
     */
    private V value;

    ListOrderedMapEntry(final OrderedMap<K, V> parent, final K key) {
      this.key = key;
      this.parent = parent;
    }

    @Override
    public K getKey() {
      return key;
    }

    @Override
    public V getValue() {
      return parent.get(getKey());
    }

    @Override
    public V setValue(final V value) {
      return parent.put(getKey(), value);
    }

    /**
     * Compares this <code>Map.Entry</code> with another <code>Map.Entry</code>.
     * <p>
     * Implemented per API documentation of {@link java.util.Map.Entry#equals(Object)}
     *
     * @param obj the object to compare to
     * @return true if equal key and value
     */
    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Map.Entry)) {
        return false;
      }
      final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
      return
        (getKey() == null ? other.getKey() == null : getKey().equals(other.getKey())) &&
          (getValue() == null ? other.getValue() == null : getValue().equals(other.getValue()));
    }

    /**
     * Gets a hashCode compatible with the equals' method.
     * <p>
     * Implemented per API documentation of {@link java.util.Map.Entry#hashCode()}
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
      return (getKey() == null ? 0 : getKey().hashCode()) ^
        (getValue() == null ? 0 : getValue().hashCode());
    }
  }
}
