package org.apache.ddlutils.data;

public interface Converter {

  /**
   * Convert the specified input object into an output object of the
   * specified type.
   *
   * @param <T>   the desired result type
   * @param type  Data type to which this value should be converted
   * @param value The input value to be converted
   * @return The converted value
   * @throws ConversionException if conversion cannot be performed
   *                             successfully
   */
  <T> T convert(Class<T> type, Object value);
}
