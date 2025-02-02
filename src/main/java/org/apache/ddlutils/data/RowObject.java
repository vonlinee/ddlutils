/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ddlutils.data;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Object of row values, where the keys are the column names.
 */
public class RowObject extends HashMap<String, Object> {

  /**
   * The <code>DynaClass</code> "base class" that this DynaBean
   * is associated with.
   */
  protected TableClass tableClass;

  /**
   * The set of property values for this DynaBean, keyed by property name.
   */
  protected HashMap<String, Object> values = this;

  /**
   * Construct a new <code>DynaBean</code> associated with the specified
   * <code>DynaClass</code> instance.
   *
   * @param tableClass The DynaClass we are associated with
   */
  public RowObject(final TableClass tableClass) {
    this.tableClass = tableClass;
  }

  /**
   * Does the specified mapped property contain a value for the specified
   * key value?
   *
   * @param name Name of the property to check
   * @param key  Name of the key to check
   * @return <code>true</code> if the mapped property contains a value for
   * the specified key, otherwise <code>false</code>
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   */
  public boolean contains(final String name, final String key) {
    final Object value = values.get(name);
    Objects.requireNonNull(value, () -> "No mapped value for '" + name + "(" + key + ")'");
    if (value instanceof Map) {
      return ((Map<?, ?>) value).containsKey(key);
    }
    throw new IllegalArgumentException("Non-mapped property for '" + name + "(" + key + ")'");
  }

  /**
   * Return the value of a simple property with the specified name.
   *
   * @param name Name of the property whose value is to be retrieved
   * @return The property's value
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   */
  public Object get(final String name) {

    // Return any non-null value for the specified property
    final Object value = values.get(name);
    if (value != null) {
      return value;
    }

    // Return a null value for a non-primitive property
    final Class<?> type = getDynaProperty(name).getType();
    if (!type.isPrimitive()) {
      return null;
    }

    // Manufacture default values for primitive properties
    if (type == Boolean.TYPE) {
      return Boolean.FALSE;
    }
    if (type == Byte.TYPE) {
      return (byte) 0;
    }
    if (type == Character.TYPE) {
      return (char) 0;
    }
    if (type == Double.TYPE) {
      return 0.0;
    }
    if (type == Float.TYPE) {
      return (float) 0.0;
    }
    if (type == Integer.TYPE) {
      return 0;
    }
    if (type == Long.TYPE) {
      return 0L;
    }
    if (type == Short.TYPE) {
      return (short) 0;
    }
    return null;
  }

  /**
   * Return the value of an indexed property with the specified name.
   *
   * @param name  Name of the property whose value is to be retrieved
   * @param index Index of the value to be retrieved
   * @return The indexed property's value
   * @throws IllegalArgumentException  if there is no property
   *                                   of the specified name
   * @throws IllegalArgumentException  if the specified property
   *                                   exists, but is not indexed
   * @throws IndexOutOfBoundsException if the specified index
   *                                   is outside the range of the underlying property
   * @throws NullPointerException      if no array or List has been
   *                                   initialized for this property
   */
  public Object get(final String name, final int index) {
    final Object value = values.get(name);
    Objects.requireNonNull(value, () -> "No indexed value for '" + name + "[" + index + "]'");
    if (value.getClass().isArray()) {
      return Array.get(value, index);
    }
    if (value instanceof List) {
      return ((List<?>) value).get(index);
    }
    throw new IllegalArgumentException("Non-indexed property for '" + name + "[" + index + "]'");
  }

  /**
   * Return the value of a mapped property with the specified name,
   * or <code>null</code> if there is no value for the specified key.
   *
   * @param name Name of the property whose value is to be retrieved
   * @param key  Key of the value to be retrieved
   * @return The mapped property's value
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   * @throws IllegalArgumentException if the specified property
   *                                  exists, but is not mapped
   */
  public Object get(final String name, final String key) {
    final Object value = values.get(name);
    Objects.requireNonNull(value, () -> "No mapped value for '" + name + "(" + key + ")'");
    if (value instanceof Map) {
      return ((Map<?, ?>) value).get(key);
    }
    throw new IllegalArgumentException("Non-mapped property for '" + name + "(" + key + ")'");
  }

  /**
   * Return the <code>DynaClass</code> instance that describes the set of
   * properties available for this DynaBean.
   *
   * @return The associated DynaClass
   */
  public TableClass getTableClass() {
    return this.tableClass;
  }

  /**
   * Return the property descriptor for the specified property name.
   *
   * @param name Name of the property for which to retrieve the descriptor
   * @return The property descriptor
   * @throws IllegalArgumentException if this is not a valid property
   *                                  name for our DynaClass
   */
  protected ColumnProperty getDynaProperty(final String name) {
    final ColumnProperty descriptor = getTableClass().getProperty(name);
    if (descriptor == null) {
      throw new IllegalArgumentException
        ("Invalid property name '" + name + "'");
    }
    return descriptor;
  }

  /**
   * Is an object of the source class assignable to the destination class?
   *
   * @param target Destination class
   * @param source Source class
   * @return <code>true</code> if the source class is assignable to the
   * destination class, otherwise <code>false</code>
   */
  protected boolean isAssignable(final Class<?> target, final Class<?> source) {
    return target.isAssignableFrom(source) ||
      target == Boolean.TYPE && source == Boolean.class ||
      target == Byte.TYPE && source == Byte.class ||
      target == Character.TYPE && source == Character.class ||
      target == Double.TYPE && source == Double.class ||
      target == Float.TYPE && source == Float.class ||
      target == Integer.TYPE && source == Integer.class ||
      target == Long.TYPE && source == Long.class ||
      target == Short.TYPE && source == Short.class;
  }

  /**
   * Remove any existing value for the specified key on the
   * specified mapped property.
   *
   * @param name Name of the property for which a value is to
   *             be removed
   * @param key  Key of the value to be removed
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   */
  public void remove(final String name, final String key) {
    final Object value = values.get(name);
    Objects.requireNonNull(value, () -> "No mapped value for '" + name + "(" + key + ")'");
    if (!(value instanceof Map)) {
      throw new IllegalArgumentException("Non-mapped property for '" + name + "(" + key + ")'");
    }
    ((Map<?, ?>) value).remove(key);
  }

  /**
   * Set the value of an indexed property with the specified name.
   *
   * @param name  Name of the property whose value is to be set
   * @param index Index of the property to be set
   * @param value Value to which this property is to be set
   * @throws ConversionException       if the specified value cannot be
   *                                   converted to the type required for this property
   * @throws IllegalArgumentException  if there is no property
   *                                   of the specified name
   * @throws IllegalArgumentException  if the specified property
   *                                   exists, but is not indexed
   * @throws IndexOutOfBoundsException if the specified index
   *                                   is outside the range of the underlying property
   */
  @SuppressWarnings("unchecked")
  public void set(final String name, final int index, final Object value) {
    final Object prop = values.get(name);
    Objects.requireNonNull(prop, () -> "No indexed value for '" + name + "[" + index + "]'");
    if (prop.getClass().isArray()) {
      Array.set(prop, index, value);
    } else if (prop instanceof List) {
      try {
        // This is safe to cast because list properties are always
        // of type Object
        List<Object> list = (List<Object>) prop;
        list.set(index, value);
      } catch (final ClassCastException e) {
        throw new ConversionException(e.getMessage());
      }
    } else {
      throw new IllegalArgumentException("Non-indexed property for '" + name + "[" + index + "]'");
    }
  }

  /**
   * Set the value of a simple property with the specified name.
   *
   * @param name  Name of the property whose value is to be set
   * @param value Value to which this property is to be set
   * @throws ConversionException      if the specified value cannot be
   *                                  converted to the type required for this property
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   * @throws NullPointerException     if an attempt is made to set a
   *                                  primitive property to null
   */
  public void set(final String name, final Object value) {

    final ColumnProperty descriptor = getDynaProperty(name);
    if (value == null) {
      if (descriptor.getType().isPrimitive()) {
        throw new NullPointerException
          ("Primitive value for '" + name + "'");
      }
    } else if (!isAssignable(descriptor.getType(), value.getClass())) {
      throw new ConversionException
        ("Cannot assign value of type '" +
          value.getClass().getName() +
          "' to property '" + name + "' of type '" +
          descriptor.getType().getName() + "'");
    }
    values.put(name, value);

  }

  /**
   * Set the value of a mapped property with the specified name.
   *
   * @param name  Name of the property whose value is to be set
   * @param key   Key of the property to be set
   * @param value Value to which this property is to be set
   * @throws ConversionException      if the specified value cannot be
   *                                  converted to the type required for this property
   * @throws IllegalArgumentException if there is no property
   *                                  of the specified name
   * @throws IllegalArgumentException if the specified property
   *                                  exists, but is not mapped
   */
  @SuppressWarnings("unchecked")
  public void set(final String name, final String key, final Object value) {
    final Object prop = values.get(name);
    Objects.requireNonNull(prop, () -> "No mapped value for '" + name + "(" + key + ")'");
    if (!(prop instanceof Map)) {
      throw new IllegalArgumentException("Non-mapped property for '" + name + "(" + key + ")'");
    }
    // This is safe to cast because mapped properties are always
    // maps of types String -> Object
    Map<String, Object> map = (Map<String, Object>) prop;
    map.put(key, value);
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    TableClass type = getTableClass();
    ColumnProperty[] props = type.getProperties();

    result.append(type.getName());
    result.append(": ");
    for (int idx = 0; idx < props.length; idx++) {
      if (idx > 0) {
        result.append(", ");
      }
      result.append(props[idx].getName());
      result.append(" = ");
      result.append(get(props[idx].getName()));
    }
    return result.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RowObject) {
      RowObject other = (RowObject) obj;
      TableClass tableClass = getTableClass();

      if (tableClass.equals(other.getTableClass())) {
        ColumnProperty[] props = tableClass.getProperties();

        for (ColumnProperty prop : props) {
          Object value = get(prop.getName());
          Object otherValue = other.get(prop.getName());

          if (value == null) {
            if (otherValue != null) {
              return false;
            }
          } else {
            return value.equals(otherValue);
          }
        }
        return true;
      }
    }
    return false;
  }
}

