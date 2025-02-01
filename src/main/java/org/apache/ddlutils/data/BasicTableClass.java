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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * <p>Minimal implementation of the <code>DynaClass</code> interface.  Can be
 * used as a convenience base class for more sophisticated implementations.</p> *
 * <p><strong>IMPLEMENTATION NOTE</strong> - The <code>DynaBean</code>
 * implementation class supplied to our constructor MUST have a one-argument
 * constructor of its own that accepts a <code>DynaClass</code>.  This is
 * used to associate the DynaBean instance with this DynaClass.</p>
 */
public class BasicTableClass implements TableClass, Serializable {

  /**
   * The method signature of the constructor we will use to create
   * new DynaBean instances.
   */
  protected static Class<?>[] constructorTypes = {TableClass.class};

  /**
   * The constructor of the <code>dynaBeanClass</code> that we will use
   * for creating new instances.
   */
  protected transient Constructor<?> constructor;

  /**
   * The argument values to be passed to the constructors we will use
   * to create new DynaBean instances.
   */
  protected Object[] constructorValues = {this};

  /**
   * The <code>DynaBean</code> implementation class we will use for
   * creating new instances.
   */
  protected Class<?> dynaBeanClass = BasicRowObject.class;

  /**
   * The "name" of this DynaBean class.
   */
  protected String name = this.getClass().getName();

  /**
   * The set of dynamic properties that are part of this DynaClass.
   */
  protected ColumnProperty[] properties = {};

  /**
   * The set of dynamic properties that are part of this DynaClass,
   * keyed by the property name.  Individual descriptor instances will
   * be the same instances as those in the <code>properties</code> list.
   */
  protected HashMap<String, ColumnProperty> propertiesMap = new HashMap<>();

  /**
   * Construct a new BasicDynaClass with default parameters.
   */
  public BasicTableClass() {
    this(null, null, null);
  }

  /**
   * Construct a new BasicDynaClass with the specified parameters.
   *
   * @param name          Name of this DynaBean class
   * @param dynaBeanClass The implementation class for new instances
   */
  public BasicTableClass(final String name, final Class<?> dynaBeanClass) {
    this(name, dynaBeanClass, null);
  }

  /**
   * Construct a new BasicDynaClass with the specified parameters.
   *
   * @param name          Name of this DynaBean class
   * @param dynaBeanClass The implementation class for new intances
   * @param properties    Property descriptors for the supported properties
   */
  public BasicTableClass(final String name, Class<?> dynaBeanClass,
                         final ColumnProperty[] properties) {
    if (name != null) {
      this.name = name;
    }
    if (dynaBeanClass == null) {
      dynaBeanClass = BasicRowObject.class;
    }
    setDynaBeanClass(dynaBeanClass);
    if (properties != null) {
      setProperties(properties);
    }
  }

  /**
   * Return the Class object we will use to create new instances in the
   * <code>newInstance()</code> method.  This Class <strong>MUST</strong>
   * implement the <code>DynaBean</code> interface.
   *
   * @return The class of the {@link RowObject}
   */
  public Class<?> getDynaBeanClass() {
    return this.dynaBeanClass;
  }

  /**
   * <p>Return an array of <code>PropertyDescriptors</code> for the properties
   * currently defined in this DynaClass.  If no properties are defined, a
   * zero-length array will be returned.</p>
   *
   * <p><strong>FIXME</strong> - Should we really be implementing
   * <code>getBeanInfo()</code> instead, which returns property descriptors
   * and a bunch of other stuff?</p>
   *
   * @return the array of properties for this DynaClass
   */
  @Override
  public ColumnProperty[] getDynaProperties() {
    return properties;
  }

  /**
   * Return a property descriptor for the specified property, if it exists;
   * otherwise, return <code>null</code>.
   *
   * @param name Name of the dynamic property for which a descriptor
   *             is requested
   * @return The descriptor for the specified property
   * @throws IllegalArgumentException if no property name is specified
   */
  @Override
  public ColumnProperty getDynaProperty(final String name) {
    if (name == null) {
      throw new IllegalArgumentException
        ("No property name specified");
    }
    return propertiesMap.get(name);
  }

  /**
   * Return the name of this DynaClass (analogous to the
   * <code>getName()</code> method of <code>java.lang.Class</code>), which
   * allows the same <code>DynaClass</code> implementation class to support
   * different dynamic classes, with different sets of properties.
   *
   * @return the name of the DynaClass
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Instantiate and return a new DynaBean instance, associated
   * with this DynaClass.
   *
   * @return A new <code>DynaBean</code> instance
   * @throws IllegalAccessException if the Class or the appropriate
   *                                constructor is not accessible
   * @throws InstantiationException if this Class represents an abstract
   *                                class, an array class, a primitive type, or void; or if instantiation
   *                                fails for some other reason
   */
  @Override
  public RowObject newInstance()
    throws IllegalAccessException, InstantiationException {
    try {
      // find the constructor after a deserialization (if needed) again
      if (constructor == null) {
        setDynaBeanClass(this.dynaBeanClass);
      }
      // Invoke the constructor to create a new bean instance
      return (RowObject) constructor.newInstance(constructorValues);
    } catch (final InvocationTargetException e) {
      throw new InstantiationException
        (e.getTargetException().getMessage());
    }
  }

  /**
   * Set the Class object we will use to create new instances in the
   * <code>newInstance()</code> method.  This Class <strong>MUST</strong>
   * implement the <code>DynaBean</code> interface.
   *
   * @param dynaBeanClass The new Class object
   * @throws IllegalArgumentException if the specified Class does not
   *                                  implement the <code>DynaBean</code> interface
   */
  protected void setDynaBeanClass(final Class<?> dynaBeanClass) {
    // Validate the argument type specified
    if (dynaBeanClass.isInterface()) {
      throw new IllegalArgumentException
        ("Class " + dynaBeanClass.getName() +
          " is an interface, not a class");
    }
    if (!RowObject.class.isAssignableFrom(dynaBeanClass)) {
      throw new IllegalArgumentException
        ("Class " + dynaBeanClass.getName() +
          " does not implement DynaBean");
    }
    // Identify the Constructor we will use in newInstance()
    try {
      this.constructor = dynaBeanClass.getConstructor(constructorTypes);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException
        ("Class " + dynaBeanClass.getName() +
          " does not have an appropriate constructor");
    }
    this.dynaBeanClass = dynaBeanClass;
  }

  /**
   * Set the list of dynamic properties supported by this DynaClass.
   *
   * @param properties List of dynamic properties to be supported
   */
  protected void setProperties(final ColumnProperty[] properties) {
    this.properties = properties;
    propertiesMap.clear();
    for (final ColumnProperty columnProperty : properties) {
      propertiesMap.put(columnProperty.getName(), columnProperty);
    }
  }

}
