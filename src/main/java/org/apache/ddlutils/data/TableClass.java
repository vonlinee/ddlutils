package org.apache.ddlutils.data;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ddlutils.model.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SqlDynaClass is a DynaClass which is associated with a persistent
 * Table in a Database.
 *
 * @version $Revision$
 */
public class TableClass {

  /**
   * The table for which this dyna class is defined.
   */
  private Table _table;
  /**
   * The primary key dyna properties.
   */
  private ColumnProperty[] _primaryKeyProperties;
  /**
   * The non-primary key dyna properties.
   */
  private ColumnProperty[] _nonPrimaryKeyProperties;

  /**
   * Creates a new dyna class instance for the given table that has the given properties.
   *
   * @param table      The table
   * @param properties The dyna properties
   */
  public TableClass(Table table, ColumnProperty[] properties) {
    this(table.getName(), RowObject.class, properties);
    _table = table;
  }

  public TableClass(final String name,
                    final ColumnProperty[] properties) {
    this(name, RowObject.class, properties);
    this._table = null;
  }

  /**
   * Factory method for creating and initializing a new dyna class instance
   * for the given table.
   *
   * @param table The table
   * @return The dyna class for the table
   */
  public static TableClass newInstance(Table table) {
    List<ColumnProperty> properties = new ArrayList<>();

    for (int idx = 0; idx < table.getColumnCount(); idx++) {
      properties.add(new ColumnProperty(table.getColumn(idx)));
    }

    ColumnProperty[] array = new ColumnProperty[properties.size()];

    properties.toArray(array);
    return new TableClass(table, array);
  }

  /**
   * Returns the table for which this dyna class is defined.
   *
   * @return The table
   */
  public Table getTable() {
    return _table;
  }

  // Helper methods
  //-------------------------------------------------------------------------

  /**
   * Returns the table name for which this dyna class is defined.
   *
   * @return The table name
   */
  public String getTableName() {
    return getTable().getName();
  }

  /**
   * Returns the properties of this dyna class.
   *
   * @return The properties
   */
  public ColumnProperty[] getSqlDynaProperties() {
    ColumnProperty[] props = getProperties();
    ColumnProperty[] result = new ColumnProperty[props.length];
    System.arraycopy(props, 0, result, 0, props.length);
    return result;
  }

  /**
   * Returns the properties for the primary keys of the corresponding table.
   *
   * @return The properties
   */
  public ColumnProperty[] getPrimaryKeyProperties() {
    if (_primaryKeyProperties == null) {
      initPrimaryKeys();
    }

    ColumnProperty[] result = new ColumnProperty[_primaryKeyProperties.length];

    System.arraycopy(_primaryKeyProperties, 0, result, 0, _primaryKeyProperties.length);
    return result;
  }

  /**
   * Returns the properties for the non-primary keys of the corresponding table.
   *
   * @return The properties
   */
  public ColumnProperty[] getNonPrimaryKeyProperties() {
    if (_nonPrimaryKeyProperties == null) {
      initPrimaryKeys();
    }

    ColumnProperty[] result = new ColumnProperty[_nonPrimaryKeyProperties.length];

    System.arraycopy(_nonPrimaryKeyProperties, 0, result, 0, _nonPrimaryKeyProperties.length);
    return result;
  }

  // Implementation methods
  //-------------------------------------------------------------------------

  /**
   * Initializes the primary key and non-primary key property arrays.
   */
  protected void initPrimaryKeys() {
    List<ColumnProperty> pkProps = new ArrayList<>();
    List<ColumnProperty> nonPkProps = new ArrayList<>();
    ColumnProperty[] properties = getProperties();

    for (ColumnProperty property : properties) {
      if (property != null) {
        if (property.isPrimaryKey()) {
          pkProps.add(property);
        } else {
          nonPkProps.add(property);
        }
      }
    }
    _primaryKeyProperties = pkProps.toArray(new ColumnProperty[0]);
    _nonPrimaryKeyProperties = nonPkProps.toArray(new ColumnProperty[0]);
  }


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
  protected Class<?> dynaBeanClass = RowObject.class;

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
  public TableClass() {
    this(null, null, null);
  }

  /**
   * Construct a new BasicDynaClass with the specified parameters.
   *
   * @param name          Name of this DynaBean class
   * @param dynaBeanClass The implementation class for new instances
   */
  public TableClass(String name, Class<?> dynaBeanClass) {
    this(name, dynaBeanClass, null);
  }

  /**
   * Construct a new BasicDynaClass with the specified parameters.
   *
   * @param name          Name of this DynaBean class
   * @param dynaBeanClass The implementation class for new instances
   * @param properties    Property descriptors for the supported properties
   */
  public TableClass(final String name, Class<?> dynaBeanClass,
                    final ColumnProperty[] properties) {
    if (name != null) {
      this.name = name;
    }
    if (dynaBeanClass == null) {
      dynaBeanClass = RowObject.class;
    }
    setTableClass(dynaBeanClass);
    if (properties != null) {
      setProperties(properties);
    }
  }

  /**
   * Set the Class object we will use to create new instances in the
   * <code>newInstance()</code> method.  This Class <strong>MUST</strong>
   * implement the <code>DynaBean</code> interface.
   *
   * @param tableClass The new Class object
   * @throws IllegalArgumentException if the specified Class does not implement the <code>DynaBean</code> interface
   */
  protected void setTableClass(final Class<?> tableClass) {
    // Validate the argument type specified
    if (tableClass.isInterface()) {
      throw new IllegalArgumentException
        ("Class " + tableClass.getName() +
          " is an interface, not a class");
    }
    if (!RowObject.class.isAssignableFrom(tableClass)) {
      throw new IllegalArgumentException
        ("Class " + tableClass.getName() +
          " does not implement DynaBean");
    }
    // Identify the Constructor we will use in newInstance()
    try {
      this.constructor = tableClass.getConstructor(constructorTypes);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException
        ("Class " + tableClass.getName() +
          " does not have an appropriate constructor");
    }
    this.dynaBeanClass = tableClass;
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
  public ColumnProperty[] getProperties() {
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
  public ColumnProperty getProperty(final String name) {
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
  public RowObject newInstance()
    throws IllegalAccessException, InstantiationException {
    try {
      // find the constructor after a deserialization (if needed) again
      if (constructor == null) {
        setTableClass(this.dynaBeanClass);
      }
      // Invoke the constructor to create a new bean instance
      return (RowObject) constructor.newInstance(constructorValues);
    } catch (final InvocationTargetException e) {
      throw new InstantiationException
        (e.getTargetException().getMessage());
    }
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
