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

import org.apache.ddlutils.data.converter.ArrayConverter;
import org.apache.ddlutils.data.converter.BigDecimalConverter;
import org.apache.ddlutils.data.converter.BigIntegerConverter;
import org.apache.ddlutils.data.converter.BooleanConverter;
import org.apache.ddlutils.data.converter.ByteConverter;
import org.apache.ddlutils.data.converter.CalendarConverter;
import org.apache.ddlutils.data.converter.CharacterConverter;
import org.apache.ddlutils.data.converter.ClassConverter;
import org.apache.ddlutils.data.converter.ConverterFacade;
import org.apache.ddlutils.data.converter.DateConverter;
import org.apache.ddlutils.data.converter.DoubleConverter;
import org.apache.ddlutils.data.converter.FileConverter;
import org.apache.ddlutils.data.converter.FloatConverter;
import org.apache.ddlutils.data.converter.IntegerConverter;
import org.apache.ddlutils.data.converter.LongConverter;
import org.apache.ddlutils.data.converter.ShortConverter;
import org.apache.ddlutils.data.converter.SqlDateConverter;
import org.apache.ddlutils.data.converter.SqlTimeConverter;
import org.apache.ddlutils.data.converter.SqlTimestampConverter;
import org.apache.ddlutils.data.converter.StringConverter;
import org.apache.ddlutils.data.converter.URLConverter;
import org.apache.ddlutils.util.WeakFastHashMap;

import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;

/**
 * <p>Utility methods for converting String scalar values to objects of the
 * specified Class, String arrays to arrays of the specified Class.  The
 * actual {@link Converter} instance to be used can be registered for each
 * possible destination Class.  Unless you override them, standard
 * {@link Converter} instances are provided for all of the following
 * destination Classes:</p>
 * <ul>
 * <li>java.lang.BigDecimal (no default value)</li>
 * <li>java.lang.BigInteger (no default value)</li>
 * <li>boolean and java.lang.Boolean (default to false)</li>
 * <li>byte and java.lang.Byte (default to zero)</li>
 * <li>char and java.lang.Character (default to a space)</li>
 * <li>java.lang.Class (no default value)</li>
 * <li>double and java.lang.Double (default to zero)</li>
 * <li>float and java.lang.Float (default to zero)</li>
 * <li>int and java.lang.Integer (default to zero)</li>
 * <li>long and java.lang.Long (default to zero)</li>
 * <li>short and java.lang.Short (default to zero)</li>
 * <li>java.lang.String (default to null)</li>
 * <li>java.io.File (no default value)</li>
 * <li>java.net.URL (no default value)</li>
 * <li>java.sql.Date (no default value)</li>
 * <li>java.sql.Time (no default value)</li>
 * <li>java.sql.Timestamp (no default value)</li>
 * </ul>
 *
 * <p>For backwards compatibility, the standard Converters for primitive
 * types (and the corresponding wrapper classes) return a defined
 * default value when a conversion error occurs.  If you prefer to have a
 * {@link ConversionException} thrown instead, replace the standard Converter
 * instances with instances created with the zero-arguments constructor.  For
 * example, to cause the Converters for integers to throw an exception on
 * conversion errors, you could do this:</p>
 * <pre>
 *   // No-args constructor gets the version that throws exceptions
 *   Converter myConverter =
 *    new org.apache.commons.beanutils.converter.IntegerConverter();
 *   ConvertUtils.register(myConverter, Integer.TYPE);    // Native type
 *   ConvertUtils.register(myConverter, Integer.class);   // Wrapper class
 * </pre>
 *
 * <p>
 * Converters generally treat null input as if it were invalid
 * input, ie they return their default value if one was specified when the
 * converter was constructed, and throw an exception otherwise. If you prefer
 * nulls to be preserved for converters that are converting to objects (not
 * primitives) then register a converter as above, passing a default value of
 * null to the converter constructor (and of course registering that converter
 * only for the .class target).
 * </p>
 *
 * <p>
 * When a converter is listed above as having no default value, then that
 * converter will throw an exception when passed null or an invalid value
 * as its input. In particular, by default the BigInteger and BigDecimal
 * converters have no default (and are therefore somewhat inconsistent
 * with the other numerical converters which all have zero as their default).
 * </p>
 *
 * <p>
 * Converters that generate <em>arrays</em> of each of the primitive types are
 * also automatically configured (including String[]). When passed null
 * or invalid input, these return an empty array (not null). See class
 * AbstractArrayConverter for the supported input formats for these converters.
 * </p>
 */
class DefaultConversionService implements ConversionService {

  private static final Integer ZERO = 0;
  private static final Character SPACE = ' ';

  /**
   * The set of {@link Converter}s that can be used to convert Strings
   * into objects of a specified Class, keyed by the destination Class.
   */
  private final WeakFastHashMap<Class<?>, Converter> converters = new WeakFastHashMap<>();

  /**
   * Construct a bean with standard converters registered
   */
  public DefaultConversionService() {
    converters.setFast(false);
    deregister();
    converters.setFast(true);
  }

  /**
   * Convert the specified value into a String.  If the specified value
   * is an array, the first element (converted to a String) will be
   * returned.  The registered {@link Converter} for the
   * <code>java.lang.String</code> class will be used, which allows
   * applications to customize Object to String conversions (the default
   * implementation simply uses toString()).
   *
   * @param value Value to be converted (maybe null)
   * @return The converted String value or null if value is null
   */
  public String convert(Object value) {
    if (value == null) {
      return null;
    }
    if (!value.getClass().isArray()) {
      final Converter converter = lookup(String.class);
      return converter.convert(String.class, value);
    }
    if (Array.getLength(value) < 1) {
      return null;
    }
    value = Array.get(value, 0);
    if (value == null) {
      return null;
    }
    final Converter converter = lookup(String.class);
    return converter.convert(String.class, value);

  }

  /**
   * Convert the value to an object of the specified class (if
   * possible). If no converter for the desired target type is registered,
   * the passed in object is returned unchanged.
   *
   * @param value      Value to be converted (maybe null)
   * @param targetType Class of the value to be converted to (must not be null)
   * @return The converted value
   * @throws ConversionException if thrown by an underlying Converter
   */
  @Override
  public Object convert(final Object value, final Class<?> targetType) {

    final Class<?> sourceType = value == null ? null : value.getClass();

    Object converted = value;
    Converter converter = lookup(sourceType, targetType);
    if (converter != null) {
      converted = converter.convert(targetType, value);
    }
    if (String.class.equals(targetType) && converted != null && !(converted instanceof String)) {

      // NOTE: For backwards compatibility, if the Converter
      //       doesn't handle  conversion-->String then
      //       use the registered String Converter
      converter = lookup(String.class);
      if (converter != null) {
        converted = converter.convert(String.class, converted);
      }

      // If the object still isn't a String, use toString() method
      if (converted != null && !(converted instanceof String)) {
        converted = converted.toString();
      }

    }
    return converted;

  }

  /**
   * Convert the specified value to an object of the specified class (if
   * possible).  Otherwise, return a String representation of the value.
   *
   * @param value Value to be converted (maybe null)
   * @param clazz Java class to be converted to (must not be null)
   * @return The converted value
   * @throws ConversionException if thrown by an underlying Converter
   */
  public Object convert(final String value, final Class<?> clazz) {
    Converter converter = lookup(clazz);
    if (converter == null) {
      converter = lookup(String.class);
    }
    return converter.convert(clazz, value);

  }

  /**
   * Convert an array of specified values to an array of objects of the
   * specified class (if possible).  If the specified Java class is itself
   * an array class, this class will be the type of the returned value.
   * Otherwise, an array will be constructed whose component type is the
   * specified class.
   *
   * @param values Array of values to be converted
   * @param clazz  Java array or element class to be converted to (must not be null)
   * @return The converted value
   * @throws ConversionException if thrown by an underlying Converter
   */
  @Override
  public Object convert(final String[] values, final Class<?> clazz) {

    Class<?> type = clazz;
    if (clazz.isArray()) {
      type = clazz.getComponentType();
    }
    Converter converter = lookup(type);
    if (converter == null) {
      converter = lookup(String.class);
    }
    final Object array = Array.newInstance(type, values.length);
    for (int i = 0; i < values.length; i++) {
      Array.set(array, i, converter.convert(type, values[i]));
    }
    return array;

  }

  /**
   * Remove all registered {@link Converter}s, and re-establish the
   * standard Converters.
   */
  public void deregister() {

    converters.clear();

    registerPrimitives(false);
    registerStandard(false, false);
    registerOther(true);
    registerArrays(false, 0);
    register(BigDecimal.class, new BigDecimalConverter());
    register(BigInteger.class, new BigIntegerConverter());
  }

  /**
   * Remove any registered {@link Converter} for the specified destination
   * <code>Class</code>.
   *
   * @param clazz Class for which to remove a registered Converter
   */
  @Override
  public void deregister(final Class<?> clazz) {
    converters.remove(clazz);
  }

  /**
   * Look up and return any registered {@link Converter} for the specified
   * destination class; if there is no registered Converter, return
   * <code>null</code>.
   *
   * @param clazz Class for which to return a registered Converter
   * @return The registered {@link Converter} or <code>null</code> if not found
   */
  @Override
  public Converter lookup(final Class<?> clazz) {
    return converters.get(clazz);
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
  public Converter lookup(final Class<?> sourceType, final Class<?> targetType) {
    if (targetType == null) {
      throw new IllegalArgumentException("Target type is missing");
    }
    if (sourceType == null) {
      return lookup(targetType);
    }

    Converter converter = null;
    // Convert --> String
    if (targetType == String.class) {
      converter = lookup(sourceType);
      if (converter == null && (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType))) {
        converter = lookup(String[].class);
      }
      if (converter == null) {
        converter = lookup(String.class);
      }
      return converter;
    }

    // Convert --> String array
    if (targetType == String[].class) {
      if (sourceType.isArray() || Collection.class.isAssignableFrom(sourceType)) {
        converter = lookup(sourceType);
      }
      if (converter == null) {
        converter = lookup(String[].class);
      }
      return converter;
    }
    return lookup(targetType);
  }

  /**
   * Register the provided converters with the specified defaults.
   *
   * @param throwException   <code>true</code> if the converters should
   *                         throw an exception when a conversion error occurs, otherwise
   *                         <code>false</code> if a default value should be used.
   * @param defaultNull      <code>true</code>if the <em>standard</em> converters
   *                         should use a default value of <code>null</code>, otherwise <code>false</code>.
   *                         N.B. This values is ignored if <code>throwException</code> is <code>true</code>
   * @param defaultArraySize The size of the default array value for array converters
   *                         (N.B. This values is ignored if <code>throwException</code> is <code>true</code>).
   *                         Specifying a value less than zero causes a <code>null</code> value to be used for
   *                         the default.
   */
  public void register(final boolean throwException, final boolean defaultNull, final int defaultArraySize) {
    registerPrimitives(throwException);
    registerStandard(throwException, defaultNull);
    registerOther(throwException);
    registerArrays(throwException, defaultArraySize);
  }

  /**
   * strictly for convenience since it has same parameter order as Map#put
   */
  private void register(final Class<?> clazz, final Converter converter) {
    register(new ConverterFacade(converter), clazz);
  }

  /**
   * Register a custom {@link Converter} for the specified destination
   * <code>Class</code>, replacing any previously registered Converter.
   *
   * @param converter Converter to be registered
   * @param clazz     Destination class for conversions performed by this
   *                  Converter
   */
  @Override
  public void register(final Converter converter, final Class<?> clazz) {
    converters.put(clazz, converter);
  }

  /**
   * Register a new ArrayConverter with the specified element delegate converter
   * that returns a default array of the specified size in the event of conversion errors.
   *
   * @param componentType      The component type of the array
   * @param componentConverter The converter to delegate to for the array elements
   * @param throwException     Whether a conversion exception should be thrown or a default
   *                           value used in the event of a conversion error
   * @param defaultArraySize   The size of the default array
   */
  private void registerArrayConverter(final Class<?> componentType, final Converter componentConverter, final boolean throwException, final int defaultArraySize) {
    final Class<?> arrayType = Array.newInstance(componentType, 0).getClass();
    Converter arrayConverter;
    if (throwException) {
      arrayConverter = new ArrayConverter(arrayType, componentConverter);
    } else {
      arrayConverter = new ArrayConverter(arrayType, componentConverter, defaultArraySize);
    }
    register(arrayType, arrayConverter);
  }

  /**
   * Register array converters.
   *
   * @param throwException   <code>true</code> if the converters should
   *                         throw an exception when a conversion error occurs, otherwise <code>
   *                         <code>false</code> if a default value should be used.
   * @param defaultArraySize The size of the default array value for array converters
   *                         (N.B. This values is ignored if <code>throwException</code> is <code>true</code>).
   *                         Specifying a value less than zero causes a <code>null<code> value to be used for
   *                         the default.
   */
  private void registerArrays(final boolean throwException, final int defaultArraySize) {

    // Primitives
    registerArrayConverter(Boolean.TYPE, new BooleanConverter(), throwException, defaultArraySize);
    registerArrayConverter(Byte.TYPE, new ByteConverter(), throwException, defaultArraySize);
    registerArrayConverter(Character.TYPE, new CharacterConverter(), throwException, defaultArraySize);
    registerArrayConverter(Double.TYPE, new DoubleConverter(), throwException, defaultArraySize);
    registerArrayConverter(Float.TYPE, new FloatConverter(), throwException, defaultArraySize);
    registerArrayConverter(Integer.TYPE, new IntegerConverter(), throwException, defaultArraySize);
    registerArrayConverter(Long.TYPE, new LongConverter(), throwException, defaultArraySize);
    registerArrayConverter(Short.TYPE, new ShortConverter(), throwException, defaultArraySize);

    // Standard
    registerArrayConverter(BigDecimal.class, new BigDecimalConverter(), throwException, defaultArraySize);
    registerArrayConverter(BigInteger.class, new BigIntegerConverter(), throwException, defaultArraySize);
    registerArrayConverter(Boolean.class, new BooleanConverter(), throwException, defaultArraySize);
    registerArrayConverter(Byte.class, new ByteConverter(), throwException, defaultArraySize);
    registerArrayConverter(Character.class, new CharacterConverter(), throwException, defaultArraySize);
    registerArrayConverter(Double.class, new DoubleConverter(), throwException, defaultArraySize);
    registerArrayConverter(Float.class, new FloatConverter(), throwException, defaultArraySize);
    registerArrayConverter(Integer.class, new IntegerConverter(), throwException, defaultArraySize);
    registerArrayConverter(Long.class, new LongConverter(), throwException, defaultArraySize);
    registerArrayConverter(Short.class, new ShortConverter(), throwException, defaultArraySize);
    registerArrayConverter(String.class, new StringConverter(), throwException, defaultArraySize);

    // Other
    registerArrayConverter(Class.class, new ClassConverter(), throwException, defaultArraySize);
    registerArrayConverter(java.util.Date.class, new DateConverter(), throwException, defaultArraySize);
    registerArrayConverter(Calendar.class, new DateConverter(), throwException, defaultArraySize);
    registerArrayConverter(File.class, new FileConverter(), throwException, defaultArraySize);
    registerArrayConverter(java.sql.Date.class, new SqlDateConverter(), throwException, defaultArraySize);
    registerArrayConverter(java.sql.Time.class, new SqlTimeConverter(), throwException, defaultArraySize);
    registerArrayConverter(Timestamp.class, new SqlTimestampConverter(), throwException, defaultArraySize);
    registerArrayConverter(URL.class, new URLConverter(), throwException, defaultArraySize);

  }

  /**
   * Register the converters for other types.
   * </p>
   * This method registers the following converters:
   * <ul>
   *     <li><code>Class.class</code> - {@link ClassConverter}</li>
   *     <li><code>java.util.Date.class</code> - {@link DateConverter}</li>
   *     <li><code>java.util.Calendar.class</code> - {@link CalendarConverter}</li>
   *     <li><code>File.class</code> - {@link FileConverter}</li>
   *     <li><code>java.sql.Date.class</code> - {@link SqlDateConverter}</li>
   *     <li><code>java.sql.Time.class</code> - {@link SqlTimeConverter}</li>
   *     <li><code>java.sql.Timestamp.class</code> - {@link SqlTimestampConverter}</li>
   *     <li><code>URL.class</code> - {@link URLConverter}</li>
   * </ul>
   *
   * @param throwException <code>true</code> if the converters should
   *                       throw an exception when a conversion error occurs, otherwise <code>
   *                       <code>false</code> if a default value should be used.
   */
  private void registerOther(final boolean throwException) {
    register(Class.class, throwException ? new ClassConverter() : new ClassConverter(null));
    register(java.util.Date.class, throwException ? new DateConverter() : new DateConverter(null));
    register(Calendar.class, throwException ? new CalendarConverter() : new CalendarConverter(null));
    register(File.class, throwException ? new FileConverter() : new FileConverter(null));
    register(java.sql.Date.class, throwException ? new SqlDateConverter() : new SqlDateConverter(null));
    register(java.sql.Time.class, throwException ? new SqlTimeConverter() : new SqlTimeConverter(null));
    register(Timestamp.class, throwException ? new SqlTimestampConverter() : new SqlTimestampConverter(null));
    register(URL.class, throwException ? new URLConverter() : new URLConverter(null));
  }

  /**
   * Register the converters for primitive types.
   * </p>
   * This method registers the following converters:
   * <ul>
   *     <li><code>Boolean.TYPE</code> - {@link BooleanConverter}</li>
   *     <li><code>Byte.TYPE</code> - {@link ByteConverter}</li>
   *     <li><code>Character.TYPE</code> - {@link CharacterConverter}</li>
   *     <li><code>Double.TYPE</code> - {@link DoubleConverter}</li>
   *     <li><code>Float.TYPE</code> - {@link FloatConverter}</li>
   *     <li><code>Integer.TYPE</code> - {@link IntegerConverter}</li>
   *     <li><code>Long.TYPE</code> - {@link LongConverter}</li>
   *     <li><code>Short.TYPE</code> - {@link ShortConverter}</li>
   * </ul>
   *
   * @param throwException <code>true</code> if the converters should
   *                       throw an exception when a conversion error occurs, otherwise <code>
   *                       <code>false</code> if a default value should be used.
   */
  private void registerPrimitives(final boolean throwException) {
    register(Boolean.TYPE, throwException ? new BooleanConverter() : new BooleanConverter(Boolean.FALSE));
    register(Byte.TYPE, throwException ? new ByteConverter() : new ByteConverter(ZERO));
    register(Character.TYPE, throwException ? new CharacterConverter() : new CharacterConverter(SPACE));
    register(Double.TYPE, throwException ? new DoubleConverter() : new DoubleConverter(ZERO));
    register(Float.TYPE, throwException ? new FloatConverter() : new FloatConverter(ZERO));
    register(Integer.TYPE, throwException ? new IntegerConverter() : new IntegerConverter(ZERO));
    register(Long.TYPE, throwException ? new LongConverter() : new LongConverter(ZERO));
    register(Short.TYPE, throwException ? new ShortConverter() : new ShortConverter(ZERO));
  }

  /**
   * Register the converters for standard types.
   * </p>
   * This method registers the following converters:
   * <ul>
   *     <li><code>BigDecimal.class</code> - {@link BigDecimalConverter}</li>
   *     <li><code>BigInteger.class</code> - {@link BigIntegerConverter}</li>
   *     <li><code>Boolean.class</code> - {@link BooleanConverter}</li>
   *     <li><code>Byte.class</code> - {@link ByteConverter}</li>
   *     <li><code>Character.class</code> - {@link CharacterConverter}</li>
   *     <li><code>Double.class</code> - {@link DoubleConverter}</li>
   *     <li><code>Float.class</code> - {@link FloatConverter}</li>
   *     <li><code>Integer.class</code> - {@link IntegerConverter}</li>
   *     <li><code>Long.class</code> - {@link LongConverter}</li>
   *     <li><code>Short.class</code> - {@link ShortConverter}</li>
   *     <li><code>String.class</code> - {@link StringConverter}</li>
   * </ul>
   *
   * @param throwException <code>true</code> if the converters should
   *                       throw an exception when a conversion error occurs, otherwise <code>
   *                       <code>false</code> if a default value should be used.
   * @param defaultNull    <code>true</code>if the <em>standard</em> converters
   *                       should use a default value of <code>null</code>, otherwise <code>false</code>.
   *                       N.B. This values is ignored if <code>throwException</code> is <code>true</code>
   */
  private void registerStandard(final boolean throwException, final boolean defaultNull) {
    final Number defaultNumber = defaultNull ? null : ZERO;
    final BigDecimal bigDecimalDefault = defaultNull ? null : new BigDecimal("0.0");
    final BigInteger bigIntDefault = defaultNull ? null : new BigInteger("0");
    final Boolean booleanDefault = defaultNull ? null : Boolean.FALSE;
    final Character charDefault = defaultNull ? null : SPACE;
    final String stringDefault = defaultNull ? null : "";

    register(BigDecimal.class, throwException ? new BigDecimalConverter() : new BigDecimalConverter(bigDecimalDefault));
    register(BigInteger.class, throwException ? new BigIntegerConverter() : new BigIntegerConverter(bigIntDefault));
    register(Boolean.class, throwException ? new BooleanConverter() : new BooleanConverter(booleanDefault));
    register(Byte.class, throwException ? new ByteConverter() : new ByteConverter(defaultNumber));
    register(Character.class, throwException ? new CharacterConverter() : new CharacterConverter(charDefault));
    register(Double.class, throwException ? new DoubleConverter() : new DoubleConverter(defaultNumber));
    register(Float.class, throwException ? new FloatConverter() : new FloatConverter(defaultNumber));
    register(Integer.class, throwException ? new IntegerConverter() : new IntegerConverter(defaultNumber));
    register(Long.class, throwException ? new LongConverter() : new LongConverter(defaultNumber));
    register(Short.class, throwException ? new ShortConverter() : new ShortConverter(defaultNumber));
    register(String.class, throwException ? new StringConverter() : new StringConverter(stringDefault));
  }
}
