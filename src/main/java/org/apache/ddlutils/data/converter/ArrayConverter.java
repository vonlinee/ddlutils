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
import org.apache.ddlutils.data.Converter;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Generic {@link Converter} implementation that handles conversion
 * to and from <strong>array</strong> objects.
 * <p>
 * Can be configured to either return a <em>default value</em> or throw a
 * <code>ConversionException</code> if a conversion error occurs.
 * </p>
 * <p>
 * The main features of this implementation are:
 * </p>
 * <ul>
 *     <li><strong>Element Conversion</strong> - delegates to a {@link Converter},
 *         appropriate for the type, to convert individual elements
 *         of the array. This leverages the power of existing converters
 *         without having to replicate their functionality for converting
 *         to the element type and removes the need to create a specific
 *         array type converters.</li>
 *     <li><strong>Arrays or Collections</strong> - can convert from either arrays or
 *         Collections to an array, limited only by the capability
 *         of the delegate {@link Converter}.</li>
 *     <li><strong>Delimited Lists</strong> - can Convert <strong>to</strong> and <strong>from</strong> a
 *         delimited list in String format.</li>
 *     <li><strong>Conversion to String</strong> - converts an array to a
 *         <code>String</code> in one of two ways: as a <em>delimited list</em>
 *         or by converting the first element in the array to a String - this
 *         is controlled by the {@link ArrayConverter#setOnlyFirstToString(boolean)}
 *         parameter.</li>
 *     <li><strong>Multi Dimensional Arrays</strong> - it is possible to convert a <code>String</code>
 *         to a multi-dimensional arrays, by embedding {@link ArrayConverter}
 *         within each other - see example below.</li>
 *     <li><strong>Default Value</strong>
 *         <ul>
 *             <li><strong><em>No Default</em></strong> - use the
 *                 {@link ArrayConverter#ArrayConverter(Class, Converter)}
 *                 constructor to create a converter which throws a
 *                 {@link ConversionException} if the value is missing or
 *                 invalid.</li>
 *             <li><strong><em>Default values</em></strong> - use the
 *                 {@link ArrayConverter#ArrayConverter(Class, Converter, int)}
 *                 constructor to create a converter which returns a <i>default
 *                 value</i>. The <em>defaultSize</em> parameter controls the
 *                 <em>default value</em> in the following way:
 *                 <ul>
 *                    <li><em>defaultSize &lt; 0</em> - default is <code>null</code></li>
 *                    <li><em>defaultSize = 0</em> - default is an array of length zero</li>
 *                    <li><em>defaultSize &gt; 0</em> - default is an array with a
 *                        length specified by <code>defaultSize</code> (N.B. elements
 *                        in the array will be <code>null</code>)</li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h2>Parsing Delimited Lists</h2>
 * This implementation can convert a delimited list in <code>String</code> format
 * into an array of the appropriate type. By default, it uses a comma as the delimiter
 * but the following methods can be used to configure parsing:
 * <ul>
 *     <li><code>setDelimiter(char)</code> - allows the character used as
 *         the delimiter to be configured [default is a comma].</li>
 *     <li><code>setAllowedChars(char[])</code> - adds additional characters
 *         (to the default alphabetic/numeric) to those considered to be
 *         valid token characters.
 * </ul>
 *
 * <h2>Multi Dimensional Arrays</h2>
 * It is possible to convert a <code>String</code> to multidimensional arrays by using
 * {@link ArrayConverter} as the element {@link Converter}
 * within another {@link ArrayConverter}.
 * <p>
 * For example, the following code demonstrates how to construct a {@link Converter}
 * to convert a delimited <code>String</code> into a two dimensional integer array:
 * </p>
 * <pre>
 *    // Construct an Integer Converter
 *    IntegerConverter integerConverter = new IntegerConverter();
 *
 *    // Construct an array Converter for an integer array (i.e. int[]) using
 *    // an IntegerConverter as the element converter.
 *    // N.B. Uses the default comma (i.e. ",") as the delimiter between individual numbers
 *    ArrayConverter arrayConverter = new ArrayConverter(int[].class, integerConverter);
 *
 *    // Construct a "Matrix" Converter which converts arrays of integer arrays using
 *    // the pre-ceeding ArrayConverter as the element Converter.
 *    // N.B. Uses a semi-colon (i.e. ";") as the delimiter to separate the different sets of numbers.
 *    //      Also the delimiter used by the first ArrayConverter needs to be added to the
 *    //      "allowed characters" for this one.
 *    ArrayConverter matrixConverter = new ArrayConverter(int[][].class, arrayConverter);
 *    matrixConverter.setDelimiter(';');
 *    matrixConverter.setAllowedChars(new char[] {','});
 *
 *    // Do the Conversion
 *    String matrixString = "11,12,13 ; 21,22,23 ; 31,32,33 ; 41,42,43";
 *    int[][] result = (int[][])matrixConverter.convert(int[][].class, matrixString);
 * </pre>
 *
 * @since 1.8.0
 */
public class ArrayConverter extends AbstractConverter {

  private final Class<?> defaultType;
  private final Converter elementConverter;
  private int defaultSize;
  private char delimiter = ',';
  private char[] allowedChars = {'.', '-'};
  private boolean onlyFirstToString = true;

  /**
   * Construct an <strong>array</strong> <code>Converter</code> with the specified
   * <strong>component</strong> <code>Converter</code> that throws a
   * <code>ConversionException</code> if an error occurs.
   *
   * @param defaultType      The default array type this
   *                         <code>Converter</code> handles
   * @param elementConverter Converter used to convert
   *                         individual array elements.
   */
  public ArrayConverter(final Class<?> defaultType, final Converter elementConverter) {
    if (defaultType == null) {
      throw new IllegalArgumentException("Default type is missing");
    }
    if (!defaultType.isArray()) {
      throw new IllegalArgumentException("Default type must be an array.");
    }
    if (elementConverter == null) {
      throw new IllegalArgumentException("Component Converter is missing.");
    }
    this.defaultType = defaultType;
    this.elementConverter = elementConverter;
  }

  /**
   * Construct an <strong>array</strong> <code>Converter</code> with the specified
   * <strong>component</strong> <code>Converter</code> that returns a default
   * array of the specified size (or <code>null</code>) if an error occurs.
   *
   * @param defaultType      The default array type this
   *                         <code>Converter</code> handles
   * @param elementConverter Converter used to convert
   *                         individual array elements.
   * @param defaultSize      Specifies the size of the default array value or if less
   *                         than zero indicates that a <code>null</code> default value should be used.
   */
  public ArrayConverter(final Class<?> defaultType, final Converter elementConverter, final int defaultSize) {
    this(defaultType, elementConverter);
    this.defaultSize = defaultSize;
    Object defaultValue = null;
    if (defaultSize >= 0) {
      defaultValue = Array.newInstance(defaultType.getComponentType(), defaultSize);
    }
    setDefaultValue(defaultValue);
  }

  /**
   * Returns the value unchanged.
   *
   * @param value The value to convert
   * @return The value unchanged
   */
  @Override
  protected Object convertArray(final Object value) {
    return value;
  }

  /**
   * Converts non-array values to a Collection prior
   * to being converted either to an array or a String.
   * <ul>
   *   <li>{@link Collection} values are returned unchanged</li>
   *   <li>{@link Number}, {@link Boolean}  and {@link java.util.Date}
   *       values returned as a the only element in a List.</li>
   *   <li>All other types are converted to a String and parsed
   *       as a delimited list.</li>
   * </ul>
   * <p>
   * <strong>N.B.</strong> The method is called by both the
   * {@link ArrayConverter#convertToType(Class, Object)} and
   * {@link ArrayConverter#convertToString(Object)} methods for
   * <em>non-array</em> types.
   * </p>
   *
   * @param type  The type to convert the value to
   * @param value value to be converted
   * @return Collection elements.
   */
  protected Collection<?> convertToCollection(final Class<?> type, final Object value) {
    if (value instanceof Collection) {
      return (Collection<?>) value;
    }
    if (value instanceof Number ||
      value instanceof Boolean ||
      value instanceof Date) {
      final List<Object> list = new ArrayList<>(1);
      list.add(value);
      return list;
    }

    return parseElements(type, value.toString());
  }

  /**
   * Handles conversion to a String.
   *
   * @param value The value to be converted.
   * @return the converted String value.
   * @throws Throwable if an error occurs converting to a String
   */
  @Override
  protected String convertToString(final Object value) throws Throwable {

    int size;
    Iterator<?> iterator = null;
    final Class<?> type = value.getClass();
    if (type.isArray()) {
      size = Array.getLength(value);
    } else {
      final Collection<?> collection = convertToCollection(type, value);
      size = collection.size();
      iterator = collection.iterator();
    }

    if (size == 0) {
      return (String) getDefault(String.class);
    }

    if (onlyFirstToString) {
      size = 1;
    }

    // Create a StringBuffer containing a delimited list of the values
    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buffer.append(delimiter);
      }
      Object element = iterator == null ? Array.get(value, i) : iterator.next();
      element = elementConverter.convert(String.class, element);
      if (element != null) {
        buffer.append(element);
      }
    }

    return buffer.toString();

  }

  /**
   * Handles conversion to an array of the specified type.
   *
   * @param <T>   Target type of the conversion.
   * @param type  The type to which this value should be converted.
   * @param value The input value to be converted.
   * @return The converted value.
   * @throws Throwable if an error occurs converting to the specified type
   */
  @Override
  protected <T> T convertToType(final Class<T> type, final Object value) throws Throwable {

    if (!type.isArray()) {
      throw new ConversionException(toString(getClass())
        + " cannot handle conversion to '"
        + toString(type) + "' (not an array).");
    }

    // Handle the source
    int size = 0;
    Iterator<?> iterator = null;
    if (value.getClass().isArray()) {
      size = Array.getLength(value);
    } else {
      final Collection<?> collection = convertToCollection(type, value);
      size = collection.size();
      iterator = collection.iterator();
    }

    // Allocate a new Array
    final Class<?> componentType = type.getComponentType();
    final Object newArray = Array.newInstance(componentType, size);

    // Convert and set each element in the new Array
    for (int i = 0; i < size; i++) {
      Object element = iterator == null ? Array.get(value, i) : iterator.next();
      // TODO - probably should catch conversion errors and throw
      //        new exception providing better info back to the user
      element = elementConverter.convert(componentType, element);
      Array.set(newArray, i, element);
    }

    @SuppressWarnings("unchecked") final
    // This is safe because T is an array type and newArray is an array of
    // T's component type
    T result = (T) newArray;
    return result;
  }

  /**
   * Return the default value for conversions to the specified
   * type.
   *
   * @param type Data type to which this value should be converted.
   * @return The default value for the specified type.
   */
  @Override
  protected Object getDefault(final Class<?> type) {
    if (type.equals(String.class)) {
      return null;
    }

    final Object defaultValue = super.getDefault(type);
    if (defaultValue == null) {
      return null;
    }

    if (defaultValue.getClass().equals(type)) {
      return defaultValue;
    }
    return Array.newInstance(type.getComponentType(), defaultSize);

  }

  /**
   * Return the default type this <code>Converter</code> handles.
   *
   * @return The default type this <code>Converter</code> handles.
   */
  @Override
  protected Class<?> getDefaultType() {
    return defaultType;
  }

  /**
   * <p>Parse an incoming String of the form similar to an array initializer
   * in the Java language into a <code>List</code> individual Strings
   * for each element, according to the following rules.</p>
   * <ul>
   * <li>The string is expected to be a comma-separated list of values.</li>
   * <li>The string may optionally have matching '{' and '}' delimiters
   *   around the list.</li>
   * <li>Whitespace before and after each element is stripped.</li>
   * <li>Elements in the list may be delimited by single or double quotes.
   *  Within a quoted elements, the normal Java escape sequences are valid.</li>
   * </ul>
   *
   * @param type  The type to convert the value to
   * @param value String value to be parsed
   * @return List of parsed elements.
   * @throws ConversionException  if the syntax of <code>svalue</code>
   *                              is not syntactically valid
   * @throws NullPointerException if <code>svalue</code>
   *                              is <code>null</code>
   */
  private List<String> parseElements(final Class<?> type, String value) {

    if (log().isDebugEnabled()) {
      log().debug("Parsing elements, delimiter=[" + delimiter + "], value=[" + value + "]");
    }

    // Trim any matching '{' and '}' delimiters
    value = value.trim();
    if (value.startsWith("{") && value.endsWith("}")) {
      value = value.substring(1, value.length() - 1);
    }

    try {

      // Set up a StreamTokenizer on the characters in this String
      final StreamTokenizer st = new StreamTokenizer(new StringReader(value));
      st.whitespaceChars(delimiter, delimiter); // Set the delimiters
      st.ordinaryChars('0', '9');  // Needed to turn off numeric flag
      st.wordChars('0', '9');      // Needed to make part of tokens
      for (final char allowedChar : allowedChars) {
        st.ordinaryChars(allowedChar, allowedChar);
        st.wordChars(allowedChar, allowedChar);
      }

      // Split comma-delimited tokens into a List
      List<String> list = null;
      while (true) {
        final int tokenType = st.nextToken();
        if (tokenType == StreamTokenizer.TT_WORD || tokenType > 0) {
          if (st.sval != null) {
            if (list == null) {
              list = new ArrayList<>();
            }
            list.add(st.sval);
          }
        } else if (tokenType == StreamTokenizer.TT_EOF) {
          break;
        } else {
          throw new ConversionException("Encountered token of type "
            + tokenType + " parsing elements to '" + toString(type) + ".");
        }
      }

      if (list == null) {
        list = Collections.emptyList();
      }
      if (log().isDebugEnabled()) {
        log().debug(list.size() + " elements parsed");
      }

      // Return the completed list
      return list;

    } catch (final IOException e) {

      throw new ConversionException("Error converting from String to '"
        + toString(type) + "': " + e.getMessage(), e);

    }

  }

  /**
   * Set the allowed characters to be used for parsing a delimited String.
   *
   * @param allowedChars Characters which are to be considered as part of
   *                     the tokens when parsing a delimited String [default is '.' and '-']
   */
  public void setAllowedChars(final char[] allowedChars) {
    this.allowedChars = allowedChars;
  }

  /**
   * Set the delimiter to be used for parsing a delimited String.
   *
   * @param delimiter The delimiter [default ',']
   */
  public void setDelimiter(final char delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * Indicates whether converting to a String should create
   * a delimited list or just convert the first value.
   *
   * @param onlyFirstToString <code>true</code> converts only
   *                          the first value in the array to a String, <code>false</code>
   *                          converts all values in the array into a delimited list (default
   *                          is <code>true</code>
   */
  public void setOnlyFirstToString(final boolean onlyFirstToString) {
    this.onlyFirstToString = onlyFirstToString;
  }

  /**
   * Provide a String representation of this array converter.
   *
   * @return A String representation of this array converter
   */
  @Override
  public String toString() {
    return toString(getClass()) +
      "[UseDefault=" +
      isUseDefault() +
      ", " +
      elementConverter.toString() +
      ']';
  }

}
