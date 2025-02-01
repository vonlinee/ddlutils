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

/**
 * <p>Utility methods for converting String scalar values to objects of the
 * specified Class, String arrays to arrays of the specified Class.</p>
 *
 * <p>For more details, see <code>ConvertUtilsBean</code> which provides the
 * implementations for these methods.</p>
 */
public class ConvertUtils {

  /**
   * <p>Convert the value to an object of the specified class (if
   * possible).</p>
   *
   * @param value      Value to be converted (maybe null)
   * @param targetType Class of the value to be converted to (must not be null)
   * @return The converted value
   * @throws ConversionException if thrown by an underlying Converter
   */
  public static Object convert(final Object value, final Class<?> targetType) {
    return getInstance().convert(value, targetType);
  }

  static final ConversionService service = new DefaultConversionService();

  private static ConversionService getInstance() {
    return service;
  }

  /**
   * <p>Convert the specified value to an object of the specified class (if
   * possible).  Otherwise, return a String representation of the value.</p>
   *
   * <p>For more details see <code>ConvertUtilsBean</code>.</p>
   *
   * @param value Value to be converted (maybe null)
   * @param clazz Java class to be converted to (must not be null)
   * @return The converted value
   */
  public static Object convert(final String value, final Class<?> clazz) {
    return getInstance().convert(value, clazz);
  }

  /**
   * <p>Convert an array of specified values to an array of objects of the
   * specified class (if possible).</p>
   *
   * <p>For more details see <code>ConvertUtilsBean</code>.</p>
   *
   * @param values Array of values to be converted
   * @param clazz  Java array or element class to be converted to (must not be null)
   * @return The converted value
   */
  public static Object convert(final String[] values, final Class<?> clazz) {
    return getInstance().convert(values, clazz);
  }

  /**
   * Change primitive Class types to the associated wrapper class. This is
   * useful for concrete converter implementations which typically treat
   * primitive types like their corresponding wrapper types.
   *
   * @param <T>  The type to be checked.
   * @param type The class type to check.
   * @return The converted type.
   * @since 1.9
   */
  // All type casts are safe because the TYPE members of the wrapper types
  // return their own class.
  @SuppressWarnings("unchecked")
  public static <T> Class<T> primitiveToWrapper(final Class<T> type) {
    if (type == null || !type.isPrimitive()) {
      return type;
    }
    if (type == Integer.TYPE) {
      return (Class<T>) Integer.class;
    }
    if (type == Double.TYPE) {
      return (Class<T>) Double.class;
    }
    if (type == Long.TYPE) {
      return (Class<T>) Long.class;
    }
    if (type == Boolean.TYPE) {
      return (Class<T>) Boolean.class;
    }
    if (type == Float.TYPE) {
      return (Class<T>) Float.class;
    }
    if (type == Short.TYPE) {
      return (Class<T>) Short.class;
    }
    if (type == Byte.TYPE) {
      return (Class<T>) Byte.class;
    }
    if (type == Character.TYPE) {
      return (Class<T>) Character.class;
    }
    return type;
  }
}
