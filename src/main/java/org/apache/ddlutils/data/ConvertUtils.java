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

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;

/**
 * <p>Utility methods for converting String scalar values to objects of the
 * specified Class, String arrays to arrays of the specified Class.</p>
 *
 * <p>For more details, see <code>ConvertUtilsBean</code> which provides the
 * implementations for these methods.</p>
 *
 * @see ConvertUtilsBean
 */

public class ConvertUtils {

  /**
   * <p>Convert the specified value into a String.</p>
   *
   * <p>For more details see <code>ConvertUtilsBean</code>.</p>
   *
   * @param value Value to be converted (maybe null)
   * @return The converted String value or null if value is null
   * @see ConvertUtilsBean#convert(Object)
   */
  public static String convert(final Object value) {
    return BeanUtilsBean.getInstance().getConvertUtils().convert(value);
  }

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

  private static ConvertUtilsBean getInstance() {
    return BeanUtilsBean.getInstance().getConvertUtils();
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
   * @see ConvertUtilsBean#convert(String, Class)
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
   * @see ConvertUtilsBean#convert(String[], Class)
   */
  public static Object convert(final String[] values, final Class<?> clazz) {
    return getInstance().convert(values, clazz);
  }

  /**
   * <p>Look up and return any registered {@link Converter} for the specified
   * destination class; if there is no registered Converter, return
   * <code>null</code>.</p>
   *
   * <p>For more details see <code>ConvertUtilsBean</code>.</p>
   *
   * @param clazz Class for which to return a registered Converter
   * @return The registered {@link Converter} or <code>null</code> if not found
   * @see ConvertUtilsBean#lookup(Class)
   */
  public static Converter lookup(final Class<?> clazz) {
    return getInstance().lookup(clazz);
  }

  /**
   * Look up and return any registered {@link Converter} for the specified
   * source and destination class; if there is no registered Converter,
   * return <code>null</code>.
   *
   * @param sourceType Class of the value being converted
   * @param targetType Class of the value to be converted to
   * @return The registered {@link Converter} or <code>null</code> if not found
   */
  public static Converter lookup(final Class<?> sourceType, final Class<?> targetType) {
    return getInstance().lookup(sourceType, targetType);
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

  /**
   * <p>Register a custom {@link Converter} for the specified destination
   * <code>Class</code>, replacing any previously registered Converter.</p>
   *
   * <p>For more details see <code>ConvertUtilsBean</code>.</p>
   *
   * @param converter Converter to be registered
   * @param clazz     Destination class for conversions performed by this
   *                  Converter
   * @see ConvertUtilsBean#register(Converter, Class)
   */
  public static void register(final Converter converter, final Class<?> clazz) {
    getInstance().register(converter, clazz);
  }
}
