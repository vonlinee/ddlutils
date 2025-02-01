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

import java.lang.reflect.InvocationTargetException;

/**
 * <p>Utility methods for populating JavaBeans properties via reflection.</p>
 *
 * <p>The implementations are provided by {@link BeanUtilsBean}.
 * These static utility methods use the default instance.
 * More sophisticated behaviour can be provided by using a <code>BeanUtilsBean</code> instance.</p>
 *
 * @see BeanUtilsBean
 */
public class BeanUtils {

  /**
   * <p>Copy property values from the origin bean to the destination bean
   * for all cases where the property names are the same.</p>
   *
   * <p>For more details see <code>BeanUtilsBean</code>.</p>
   *
   * @param dest Destination bean whose properties are modified
   * @param orig Origin bean whose properties are retrieved
   * @throws IllegalAccessException    if the caller does not have
   *                                   access to the property accessor method
   * @throws IllegalArgumentException  if the <code>dest</code> or
   *                                   <code>orig</code> argument is null or if the <code>dest</code>
   *                                   property type is different from the source type and the relevant
   *                                   converter has not been registered.
   * @throws InvocationTargetException if the property accessor method
   *                                   throws an exception
   * @see BeanUtilsBean#copyProperties
   */
  public static void copyProperties(final Object dest, final Object orig)
    throws IllegalAccessException, InvocationTargetException {
    BeanUtilsBean.getInstance().copyProperties(dest, orig);
  }

  /**
   * <p>Return the value of the specified property of the specified bean,
   * no matter which property reference format is used, as a String.</p>
   *
   * <p>For more details see <code>BeanUtilsBean</code>.</p>
   *
   * @param bean Bean whose property is to be extracted
   * @param name Possibly indexed and/or nested name of the property
   *             to be extracted
   * @return The property's value, converted to a String
   * @throws IllegalAccessException    if the caller does not have
   *                                   access to the property accessor method
   * @throws InvocationTargetException if the property accessor method
   *                                   throws an exception
   * @throws NoSuchMethodException     if an accessor method for this
   *                                   property cannot be found
   * @see BeanUtilsBean#getProperty
   */
  public static String getProperty(final Object bean, final String name)
    throws IllegalAccessException, InvocationTargetException,
    NoSuchMethodException {
    return BeanUtilsBean.getInstance().getProperty(bean, name);
  }

  /**
   * <p>Set the specified property value, performing type conversions as
   * required to conform to the type of the destination property.</p>
   *
   * <p>For more details see <code>BeanUtilsBean</code>.</p>
   *
   * @param bean  Bean on which setting is to be performed
   * @param name  Property name (can be nested/indexed/mapped/combo)
   * @param value Value to be set
   * @throws IllegalAccessException    if the caller does not have
   *                                   access to the property accessor method
   * @throws InvocationTargetException if the property accessor method
   *                                   throws an exception
   * @see BeanUtilsBean#setProperty
   */
  public static void setProperty(final Object bean, final String name, final Object value)
    throws IllegalAccessException, InvocationTargetException {
    BeanUtilsBean.getInstance().setProperty(bean, name, value);
  }
}
