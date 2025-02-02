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
package org.apache.ddlutils.data.converter;

import org.apache.ddlutils.data.ConversionException;
import org.apache.ddlutils.data.ConvertUtils;
import org.apache.ddlutils.data.Converter;
import org.apache.ddlutils.util.Log;
import org.apache.ddlutils.util.LogFactory;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Base {@link Converter} implementation that provides the structure
 * for handling conversion <strong>to</strong> and <strong>from</strong> a specified type.
 * <p>
 * This implementation provides the basic structure for
 * converting to/from a specified type optionally using a default
 * value or throwing a {@link ConversionException} if a
 * conversion error occurs.
 * <p>
 * Implementations should provide conversion to the specified
 * type and from the specified type to a <code>String</code> value
 * by implementing the following methods:
 * <ul>
 *     <li><code>convertToString(value)</code> - convert to a String
 *        (default implementation uses the objects <code>toString()</code>
 *        method).</li>
 *     <li><code>convertToType(Class, value)</code> - convert
 *         to the specified type</li>
 * </ul>
 * <p>
 * The default value has to be compliant to the default type of this
 * converter - which is enforced by the generic type parameter. If a
 * conversion is not possible and a default value is set, the converter
 * tries to transform the default value to the requested target type.
 * If this fails, a {@code ConversionException} if thrown.
 */
public abstract class AbstractConverter implements Converter {

  /**
   * Debug logging message to indicate default value configuration
   */
  private static final String DEFAULT_CONFIG_MSG =
    "(N.B. Converters can be configured to use default values to avoid throwing exceptions)";

  /**
   * Current package name
   */
  //    getPackage() below returns null on some platforms/jvm versions during the unit tests.
//    private static final String PACKAGE = AbstractConverter.class.getPackage().getName() + ".";
  private static final String PACKAGE = "org.apache.commons.beanutils.converters.";

  /**
   * Logging for this instance.
   */
  private transient Log log;

  /**
   * Should we return the default value on conversion errors?
   */
  private boolean useDefault;

  /**
   * The default value specified to our Constructor, if any.
   */
  private Object defaultValue;

  /**
   * Construct a <em>Converter</em> that throws a
   * <code>ConversionException</code> if an error occurs.
   */
  public AbstractConverter() {
  }

  /**
   * Construct a <em>Converter</em> that returns a default
   * value if an error occurs.
   *
   * @param defaultValue The default value to be returned
   *                     if the value to be converted is missing or an error
   *                     occurs converting the value.
   */
  public AbstractConverter(final Object defaultValue) {
    setDefaultValue(defaultValue);
  }

  /**
   * Generates a standard conversion exception with a message indicating that
   * the passed in value cannot be converted to the desired target type.
   *
   * @param type  the target type
   * @param value the value to be converted
   * @return a {@code ConversionException} with a standard message
   * @since 1.9
   */
  protected ConversionException conversionException(final Class<?> type, final Object value) {
    return new ConversionException("Can't convert value '" + value
      + "' to type " + type);
  }

  /**
   * Convert the input object into an output object of the
   * specified type.
   *
   * @param <T>   the target type of the conversion
   * @param type  Data type to which this value should be converted
   * @param value The input value to be converted
   * @return The converted value.
   * @throws ConversionException if conversion cannot be performed
   *                             successfully and no default is specified.
   */
  @Override
  public <T> T convert(final Class<T> type, Object value) {

    if (type == null) {
      return convertToDefaultType(type, value);
    }

    Class<?> sourceType = value == null ? null : value.getClass();
    final Class<T> targetType = ConvertUtils.primitiveToWrapper(type);

    if (log().isDebugEnabled()) {
      log().debug("Converting"
        + (value == null ? "" : " '" + toString(sourceType) + "'")
        + " value '" + value + "' to type '" + toString(targetType) + "'");
    }

    value = convertArray(value);

    // Missing Value
    if (value == null) {
      return handleMissing(targetType);
    }

    sourceType = value.getClass();

    try {
      // Convert --> String
      if (targetType.equals(String.class)) {
        return targetType.cast(convertToString(value));

        // No conversion necessary
      }
      if (targetType.equals(sourceType)) {
        if (log().isDebugEnabled()) {
          log().debug("    No conversion required, value is already a "
            + toString(targetType));
        }
        return targetType.cast(value);

        // Convert --> Type
      }
      final Object result = convertToType(targetType, value);
      if (log().isDebugEnabled()) {
        log().debug("    Converted to " + toString(targetType) +
          " value '" + result + "'");
      }
      return targetType.cast(result);
    } catch (final Throwable t) {
      return handleError(targetType, value, t);
    }

  }

  /**
   * Return the first element from an Array (or Collection)
   * or the value unchanged if not an Array (or Collection).
   * <p>
   * N.B. This needs to be overridden for array/Collection converters.
   *
   * @param value The value to convert
   * @return The first element in an Array (or Collection)
   * or the value unchanged if not an Array (or Collection)
   */
  protected Object convertArray(final Object value) {
    if (value == null) {
      return null;
    }
    if (value.getClass().isArray()) {
      if (Array.getLength(value) > 0) {
        return Array.get(value, 0);
      }
      return null;
    }
    if (value instanceof Collection) {
      final Collection<?> collection = (Collection<?>) value;
      if (!collection.isEmpty()) {
        return collection.iterator().next();
      }
      return null;
    }
    return value;
  }

  /**
   * Performs a conversion to the default type. This method is called if we do
   * not have a target class. In this case, the T parameter is not set.
   * Therefore, we can cast to it (which is required to fulfill the contract
   * of the method signature).
   *
   * @param <T>         the type of the result object
   * @param targetClass the target class of the conversion
   * @param value       the value to be converted
   * @return the converted value
   */
  @SuppressWarnings("unchecked")
  private <T> T convertToDefaultType(final Class<T> targetClass, final Object value) {
    return (T) convert(getDefaultType(), value);
  }

  /**
   * Convert the input object into a String.
   * <p>
   * <strong>N.B.</strong>This implementation simply uses the value's
   * <code>toString()</code> method and should be overridden if a
   * more sophisticated mechanism for <em>conversion to a String</em>
   * is required.
   *
   * @param value The input value to be converted.
   * @return the converted String value.
   * @throws Throwable if an error occurs converting to a String
   */
  protected String convertToString(final Object value) throws Throwable {
    return value.toString();
  }

  /**
   * Convert the input object into an output object of the
   * specified type.
   * <p>
   * Typical implementations will provide a minimum of
   * <code>String to type</code> conversion.
   *
   * @param <T>   Target type of the conversion.
   * @param type  Data type to which this value should be converted.
   * @param value The input value to be converted.
   * @return The converted value.
   * @throws Throwable if an error occurs converting to the specified type
   */
  protected abstract <T> T convertToType(Class<T> type, Object value) throws Throwable;

  /**
   * Return the default value for conversions to the specified
   * type.
   *
   * @param type Data type to which this value should be converted.
   * @return The default value for the specified type.
   */
  protected Object getDefault(final Class<?> type) {
    if (type.equals(String.class)) {
      return null;
    }
    return defaultValue;
  }

  /**
   * Return the default type this <code>Converter</code> handles.
   *
   * @return The default type this <code>Converter</code> handles.
   */
  protected abstract Class<?> getDefaultType();

  /**
   * Handle Conversion Errors.
   * <p>
   * If a default value has been specified then it is returned
   * otherwise a ConversionException is thrown.
   *
   * @param <T>   Target type of the conversion.
   * @param type  Data type to which this value should be converted.
   * @param value The input value to be converted
   * @param cause The exception thrown by the <code>convert</code> method
   * @return The default value.
   * @throws ConversionException if no default value has been
   *                             specified for this {@link Converter}.
   */
  protected <T> T handleError(final Class<T> type, final Object value, final Throwable cause) {
    if (log().isDebugEnabled()) {
      if (cause instanceof ConversionException) {
        log().debug("    Conversion threw ConversionException: " + cause.getMessage());
      } else {
        log().debug("    Conversion threw " + cause);
      }
    }

    if (useDefault) {
      return handleMissing(type);
    }

    ConversionException cex;
    if (cause instanceof ConversionException) {
      cex = (ConversionException) cause;
      if (log().isDebugEnabled()) {
        log().debug("    Re-throwing ConversionException: " + cex.getMessage());
        log().debug("    " + DEFAULT_CONFIG_MSG);
      }
    } else {
      final String msg = "Error converting from '" + toString(value.getClass()) +
        "' to '" + toString(type) + "' " + cause.getMessage();
      cex = new ConversionException(msg, cause);
      if (log().isDebugEnabled()) {
        log().debug("    Throwing ConversionException: " + msg);
        log().debug("    " + DEFAULT_CONFIG_MSG);
      }
    }

    throw cex;

  }

  /**
   * Handle missing values.
   * <p>
   * If a default value has been specified, then it is returned (after a cast
   * to the desired target class); otherwise a ConversionException is thrown.
   *
   * @param <T>  the desired target type
   * @param type Data type to which this value should be converted.
   * @return The default value.
   * @throws ConversionException if no default value has been
   *                             specified for this {@link Converter}.
   */
  protected <T> T handleMissing(final Class<T> type) {

    if (useDefault || type.equals(String.class)) {
      Object value = getDefault(type);
      if (useDefault && value != null && !type.equals(value.getClass())) {
        try {
          value = convertToType(type, defaultValue);
        } catch (final Throwable t) {
          throw new ConversionException("Default conversion to " + toString(type)
            + " failed.", t);
        }
      }
      if (log().isDebugEnabled()) {
        log().debug("    Using default "
          + (value == null ? "" : toString(value.getClass()) + " ")
          + "value '" + defaultValue + "'");
      }
      // value is now either null or of the desired target type
      return type.cast(value);
    }

    final ConversionException cex = new ConversionException("No value specified for '" +
      toString(type) + "'");
    if (log().isDebugEnabled()) {
      log().debug("    Throwing ConversionException: " + cex.getMessage());
      log().debug("    " + DEFAULT_CONFIG_MSG);
    }
    throw cex;

  }

  /**
   * Indicates whether a default value will be returned or exception
   * thrown in the event of a conversion error.
   *
   * @return <code>true</code> if a default value will be returned for
   * conversion errors or <code>false</code> if a {@link ConversionException}
   * will be thrown.
   */
  public boolean isUseDefault() {
    return useDefault;
  }

  /**
   * Accessor method for Log instance.
   * <p>
   * The Log instance variable is transient and
   * accessing it through this method ensures it
   * is re-initialized when this instance is
   * de-serialized.
   *
   * @return The Log instance.
   */
  Log log() {
    if (log == null) {
      log = LogFactory.getLog(getClass());
    }
    return log;
  }

  /**
   * Set the default value, converting as required.
   * <p>
   * If the default value is different from the type the
   * <code>Converter</code> handles, it will be converted
   * to the handled type.
   *
   * @param defaultValue The default value to be returned
   *                     if the value to be converted is missing or an error
   *                     occurs converting the value.
   * @throws ConversionException if an error occurs converting
   *                             the default value
   */
  protected void setDefaultValue(final Object defaultValue) {
    useDefault = false;
    if (log().isDebugEnabled()) {
      log().debug("Setting default value: " + defaultValue);
    }
    if (defaultValue == null) {
      this.defaultValue = null;
    } else {
      this.defaultValue = convert(getDefaultType(), defaultValue);
    }
    useDefault = true;
  }

  /**
   * Provide a String representation of this converter.
   *
   * @return A String representation of this converter
   */
  @Override
  public String toString() {
    return toString(getClass()) + "[UseDefault=" + useDefault + "]";
  }

  /**
   * Provide a String representation of a <code>java.lang.Class</code>.
   *
   * @param type The <code>java.lang.Class</code>.
   * @return The String representation.
   */
  String toString(final Class<?> type) {
    StringBuilder typeName;
    if (type == null) {
      typeName = new StringBuilder("null");
    } else if (type.isArray()) {
      Class<?> elementType = type.getComponentType();
      int count = 1;
      while (elementType.isArray()) {
        elementType = elementType.getComponentType();
        count++;
      }
      typeName = new StringBuilder(elementType.getName());
      for (int i = 0; i < count; i++) {
        typeName.append("[]");
      }
    } else {
      typeName = new StringBuilder(type.getName());
    }
    if (typeName.toString().startsWith("java.lang.") ||
      typeName.toString().startsWith("java.util.") ||
      typeName.toString().startsWith("java.math.")) {
      typeName = new StringBuilder(typeName.substring("java.lang.".length()));
    } else if (typeName.toString().startsWith(PACKAGE)) {
      typeName = new StringBuilder(typeName.substring(PACKAGE.length()));
    }
    return typeName.toString();
  }
}
