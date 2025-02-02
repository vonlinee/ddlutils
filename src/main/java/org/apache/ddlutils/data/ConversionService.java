package org.apache.ddlutils.data;

public interface ConversionService {

  Object convert(Object value, Class<?> targetType);

  Object convert(String[] values, Class<?> clazz);

  void deregister(Class<?> clazz);

  Converter lookup(Class<?> clazz);

  String convert(Object value);

  void register(Converter converter, Class<?> clazz);
}
