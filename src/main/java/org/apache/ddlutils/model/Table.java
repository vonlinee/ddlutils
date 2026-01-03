package org.apache.ddlutils.model;

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

import org.apache.ddlutils.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents a table in the database model.
 *
 * @version $Revision$
 */
public class Table implements Serializable {
  /**
   * Unique ID for serialization purposes.
   */
  private static final long serialVersionUID = -5541154961302342608L;
  /**
   * The columns in this table.
   */
  private final ArrayList<Column> columns = new ArrayList<>();
  /**
   * The foreign keys associated to this table.
   */
  private final ArrayList<ForeignKey> foreignKeys = new ArrayList<>();
  /**
   * The indices applied to this table.
   */
  private final ArrayList<Index> indices = new ArrayList<>();

  /**
   * The attributes of this table.
   */
  private final Attributes attributes = new Attributes();

  /**
   * The catalog of this table as read from the database.
   */
  private String catalog;
  /**
   * The table's schema.
   */
  private String schema;
  /**
   * The name.
   */
  private String name;
  /**
   * A description of the table.
   */
  private String description;

  /**
   * The table's type as read from the database.
   *
   * @see TableType
   */
  private String type;

  /**
   * Returns the catalog of this table as read from the database.
   *
   * @return The catalog
   */
  public String getCatalog() {
    return catalog;
  }

  /**
   * Sets the catalog of this table.
   *
   * @param catalog The catalog
   */
  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  /**
   * Returns the schema of this table as read from the database.
   *
   * @return The schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * Sets the schema of this table.
   *
   * @param schema The schema
   */
  public void setSchema(String schema) {
    this.schema = schema;
  }

  /**
   * Returns the type of this table as read from the database.
   *
   * @return The type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the type of this table.
   *
   * @param type The type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Returns the name of the table.
   *
   * @return The name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the table.
   *
   * @param name The name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the description of the table.
   *
   * @return The description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the table.
   *
   * @param description The description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the number of columns in this table.
   *
   * @return The number of columns
   */
  public int getColumnCount() {
    return columns.size();
  }

  /**
   * Returns the column at the specified position.
   *
   * @param idx The column index
   * @return The column at this position
   */
  public Column getColumn(int idx) {
    return columns.get(idx);
  }

  /**
   * Returns the columns in this table.
   *
   * @return The columns
   */
  public Column[] getColumns() {
    return columns.toArray(new Column[0]);
  }

  /**
   * Adds the given column.
   *
   * @param column The column
   */
  public void addColumn(Column column) {
    if (column != null) {
      columns.add(column);
    }
  }

  /**
   * Adds the given column at the specified position.
   *
   * @param idx    The index where to add the column
   * @param column The column
   */
  public void addColumn(int idx, Column column) {
    if (column != null) {
      columns.add(idx, column);
    }
  }

  /**
   * Adds the column after the given previous column.
   *
   * @param previousColumn The column to add the new column after; use
   *                       <code>null</code> for adding at the beginning
   * @param column         The column
   */
  public void addColumn(Column previousColumn, Column column) {
    if (column != null) {
      if (previousColumn == null) {
        columns.add(0, column);
      } else {
        columns.add(columns.indexOf(previousColumn), column);
      }
    }
  }

  /**
   * Adds the given columns.
   *
   * @param columns The columns
   */
  public void addColumns(Collection<Column> columns) {
    for (Column column : columns) {
      addColumn(column);
    }
  }

  /**
   * Removes the given column.
   *
   * @param column The column to remove
   */
  public void removeColumn(Column column) {
    if (column != null) {
      columns.remove(column);
    }
  }

  /**
   * Removes all columns of this table. Note that this does not change
   * indexes or foreign keys, so it might leave the table object in
   * an illegal state.
   */
  public void removeAllColumns() {
    columns.clear();
  }

  /**
   * Removes the indicated column.
   *
   * @param idx The index of the column to remove
   */
  public void removeColumn(int idx) {
    columns.remove(idx);
  }

  /**
   * Returns the number of foreign keys.
   *
   * @return The number of foreign keys
   */
  public int getForeignKeyCount() {
    return foreignKeys.size();
  }

  /**
   * Returns the foreign key at the given position.
   *
   * @param idx The foreign key index
   * @return The foreign key
   */
  public ForeignKey getForeignKey(int idx) {
    return foreignKeys.get(idx);
  }

  /**
   * Returns the foreign keys of this table.
   *
   * @return The foreign keys
   */
  public ForeignKey[] getForeignKeys() {
    return foreignKeys.toArray(new ForeignKey[0]);
  }

  /**
   * Adds the given foreign key.
   *
   * @param foreignKey The foreign key
   */
  public void addForeignKey(ForeignKey foreignKey) {
    if (foreignKey != null) {
      foreignKeys.add(foreignKey);
    }
  }

  /**
   * Adds the given foreign key at the specified position.
   *
   * @param idx        The index to add the foreign key at
   * @param foreignKey The foreign key
   */
  public void addForeignKey(int idx, ForeignKey foreignKey) {
    if (foreignKey != null) {
      foreignKeys.add(idx, foreignKey);
    }
  }

  /**
   * Adds the given foreign keys.
   *
   * @param foreignKeys The foreign keys
   */
  public void addForeignKeys(Collection<ForeignKey> foreignKeys) {
    for (ForeignKey foreignKey : foreignKeys) {
      addForeignKey(foreignKey);
    }
  }

  /**
   * Removes all foreign keys.
   */
  public void removeAllForeignKeys() {
    foreignKeys.clear();
  }

  /**
   * Removes the given foreign key.
   *
   * @param foreignKey The foreign key to remove
   */
  public void removeForeignKey(ForeignKey foreignKey) {
    if (foreignKey != null) {
      foreignKeys.remove(foreignKey);
    }
  }

  /**
   * Removes the indicated foreign key.
   *
   * @param idx The index of the foreign key to remove
   */
  public void removeForeignKey(int idx) {
    foreignKeys.remove(idx);
  }

  /**
   * Returns the number of indices.
   *
   * @return The number of indices
   */
  public int getIndexCount() {
    return indices.size();
  }

  /**
   * Returns the index at the specified position.
   *
   * @param idx The position
   * @return The index
   */
  public Index getIndex(int idx) {
    return indices.get(idx);
  }

  /**
   * Adds the given index.
   *
   * @param index The index
   */
  public void addIndex(Index index) {
    if (index != null) {
      indices.add(index);
    }
  }

  /**
   * Adds the given index at the specified position.
   *
   * @param idx   The position to add the index at
   * @param index The index
   */
  public void addIndex(int idx, Index index) {
    if (index != null) {
      indices.add(idx, index);
    }
  }

  /**
   * Adds the given indices.
   *
   * @param indices The indices
   */
  public void addIndices(Collection<Index> indices) {
    for (Index index : indices) {
      addIndex(index);
    }
  }

  /**
   * Returns the indices of this table.
   *
   * @return The indices
   */
  public Index[] getIndices() {
    return indices.toArray(new Index[0]);
  }

  /**
   * Gets a list of non-unique indices on this table.
   *
   * @return The unique indices
   */
  public Index[] getNonUniqueIndices() {
    return indices.stream().filter(index -> !index.isUnique()).toArray(Index[]::new);
  }

  /**
   * Gets a list of unique indices on this table.
   *
   * @return The unique indices
   */
  public Index[] getUniqueIndices() {
    return indices.stream().filter(Index::isUnique).toArray(Index[]::new);
  }

  /**
   * Removes the given index.
   *
   * @param index The index to remove
   */
  public void removeIndex(Index index) {
    if (index != null) {
      indices.remove(index);
    }
  }

  /**
   * Removes the indicated index.
   *
   * @param idx The position of the index to remove
   */
  public void removeIndex(int idx) {
    indices.remove(idx);
  }

  // Helper methods
  //-------------------------------------------------------------------------

  /**
   * Determines whether there is at least one primary key column on this table.
   *
   * @return <code>true</code> if there are one or more primary key columns
   */
  public boolean hasPrimaryKey() {
    for (Column column : columns) {
      if (column.isPrimaryKey()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Finds the column with the specified name, using case-insensitive matching.
   * Note that this method is not called getColumn(String) to avoid introspection
   * problems.
   *
   * @param name The name of the column
   * @return The column or <code>null</code> if there is no such column
   */
  public Column findColumn(String name) {
    return findColumn(name, false);
  }

  /**
   * Finds the column with the specified name, using case-insensitive matching.
   * Note that this method is not called getColumn(String) to avoid introspection
   * problems.
   *
   * @param name          The name of the column
   * @param caseSensitive Whether case matters for the names
   * @return The column or <code>null</code> if there is no such column
   */
  public Column findColumn(String name, boolean caseSensitive) {
    for (Column column : columns) {
      if (caseSensitive) {
        if (column.getName().equals(name)) {
          return column;
        }
      } else {
        if (column.getName().equalsIgnoreCase(name)) {
          return column;
        }
      }
    }
    return null;
  }

  /**
   * Determines the index of the given column.
   *
   * @param column The column
   * @return The index or <code>-1</code> if it is no column of this table
   */
  public int getColumnIndex(Column column) {
    int idx = 0;

    for (Iterator<Column> it = columns.iterator(); it.hasNext(); idx++) {
      if (column == it.next()) {
        return idx;
      }
    }
    return -1;
  }

  /**
   * Finds the index with the specified name, using case-insensitive matching.
   * Note that this method is not called getIndex to avoid introspection
   * problems.
   *
   * @param name The name of the index
   * @return The index or <code>null</code> if there is no such index
   */
  public Index findIndex(String name) {
    return findIndex(name, false);
  }

  /**
   * Finds the index with the specified name, using case-sensitive or insensitive
   * matching depending on the <code>caseSensitive</code> parameter.
   * Note that this method is not called getIndex to avoid introspection
   * problems.
   *
   * @param name          The name of the index
   * @param caseSensitive Whether case matters for the names
   * @return The index or <code>null</code> if there is no such index
   */
  public Index findIndex(String name, boolean caseSensitive) {
    if (name == null) {
      throw new NullPointerException("The index name to search for cannot be null");
    }
    for (int idx = 0; idx < getIndexCount(); idx++) {
      Index index = getIndex(idx);

      if ((caseSensitive && name.equals(index.getName())) ||
          (!caseSensitive && name.equalsIgnoreCase(index.getName()))) {
        return index;
      }
    }
    return null;
  }

  /**
   * Finds the foreign key with the specified name, using case-insensitive matching.
   * Note that this method is not called getForeignKey to avoid introspection
   * problems.
   *
   * @param name The name of the foreign key
   * @return The foreign key or <code>null</code> if there is no such foreign key
   */
  public ForeignKey findForeignKey(String name) {
    return findForeignKey(name, false);
  }

  /**
   * Finds the foreign key with the specified name, using case-sensitive or insensitive
   * matching depending on the <code>caseSensitive</code> parameter.
   * Note that this method is not called getForeignKey to avoid introspection
   * problems.
   *
   * @param name          The name of the foreign key
   * @param caseSensitive Whether case matters for the names
   * @return The foreign key or <code>null</code> if there is no such foreign key
   */
  public ForeignKey findForeignKey(String name, boolean caseSensitive) {
    if (name == null) {
      throw new NullPointerException("The foreign key name to search for cannot be null");
    }
    for (int idx = 0; idx < getForeignKeyCount(); idx++) {
      ForeignKey foreignKey = getForeignKey(idx);

      if ((caseSensitive && name.equals(foreignKey.getName())) ||
          (!caseSensitive && name.equalsIgnoreCase(foreignKey.getName()))) {
        return foreignKey;
      }
    }
    return null;
  }

  /**
   * Finds the foreign key in this table that is equal to the supplied foreign key.
   *
   * @param key The foreign key to search for
   * @return The found foreign key
   */
  public ForeignKey findForeignKey(ForeignKey key) {
    for (int idx = 0; idx < getForeignKeyCount(); idx++) {
      ForeignKey fk = getForeignKey(idx);

      if (fk.equals(key)) {
        return fk;
      }
    }
    return null;
  }

  /**
   * Finds the foreign key in this table that is equal to the supplied foreign key.
   *
   * @param key           The foreign key to search for
   * @param caseSensitive Whether case matters for the names
   * @return The found foreign key
   */
  public ForeignKey findForeignKey(ForeignKey key, boolean caseSensitive) {
    for (int idx = 0; idx < getForeignKeyCount(); idx++) {
      ForeignKey fk = getForeignKey(idx);

      if ((caseSensitive && fk.equals(key)) ||
          (!caseSensitive && fk.equalsIgnoreCase(key))) {
        return fk;
      }
    }
    return null;
  }

  /**
   * Returns the foreign key referencing this table if it exists.
   *
   * @return The self-referencing foreign key if any
   */
  public ForeignKey getSelfReferencingForeignKey() {
    for (int idx = 0; idx < getForeignKeyCount(); idx++) {
      ForeignKey fk = getForeignKey(idx);

      if (this.equals(fk.getForeignTable())) {
        return fk;
      }
    }
    return null;
  }

  /**
   * Returns the primary key columns of this table.
   *
   * @return The primary key columns
   */
  public Column[] getPrimaryKeyColumns() {
    return columns.stream().filter(Column::isPrimaryKey).toArray(Column[]::new);
  }

  /**
   * Returns the names of the primary key columns of this table.
   *
   * @return The primary key column names
   */
  public String[] getPrimaryKeyColumnNames() {
    Column[] pkColumns = getPrimaryKeyColumns();
    String[] names = new String[pkColumns.length];

    for (int colIdx = 0; colIdx < pkColumns.length; colIdx++) {
      names[colIdx] = pkColumns[colIdx].getName();
    }

    return names;
  }

  /**
   * Returns the auto increment columns in this table. If none are found,
   * then an empty array will be returned.
   *
   * @return The auto increment columns
   */
  public Column[] getAutoIncrementColumns() {
    return columns.stream().filter(Column::isAutoIncrement).toArray(Column[]::new);
  }

  /**
   * Returns the required (not-nullable) columns in this table. If none are found,
   * then an empty array will be returned.
   *
   * @return The required columns
   */
  public Column[] getRequiredColumns() {
    return columns.stream().filter(Column::isRequired).toArray(Column[]::new);
  }

  /**
   * Sorts the foreign keys alphabetically.
   *
   * @param caseSensitive Whether case matters
   */
  public void sortForeignKeys(final boolean caseSensitive) {
    if (!foreignKeys.isEmpty()) {
      CollectionUtils.sortString(foreignKeys, ForeignKey::getName, caseSensitive);
    }
  }

  public void setAttribute(String name, Object value) {
    attributes.set(name, value);
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * Returns the attributes.
   *
   * @return The attributes
   */
  public Attributes getAttributes() {
    return attributes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Table) {
      // Note that this compares case-sensitive
      // TODO: For now we ignore catalog and schema (type should be irrelevant anyways)
      Table other = (Table) obj;
      if (!Objects.equals(name, other.name)) {
        return false;
      }
      if (!Objects.equals(columns, other.columns)) {
        return false;
      }
      if (!Objects.equals(foreignKeys, other.foreignKeys)) {
        return false;
      }
      return Objects.equals(indices, other.indices);
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    // TODO: For now we ignore catalog and schema (type should be irrelevant anyways)
    return Objects.hash(name, columns, foreignKeys, indices);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Table [name=" +
           getName() +
           "; " +
           getColumnCount() +
           " columns]";
  }

  /**
   * Returns a verbose string representation of this table.
   *
   * @return The string representation
   */
  public String toVerboseString() {
    StringBuilder result = new StringBuilder();
    result.append("Table [name=");
    result.append(getName());
    result.append("; catalog=");
    result.append(getCatalog());
    result.append("; schema=");
    result.append(getCatalog());
    result.append("; type=");
    result.append(getType());
    result.append("] columns:");
    for (int idx = 0; idx < getColumnCount(); idx++) {
      result.append(" ");
      result.append(getColumn(idx).toVerboseString());
    }
    result.append("; indices:");
    for (int idx = 0; idx < getIndexCount(); idx++) {
      result.append(" ");
      result.append(getIndex(idx).toVerboseString());
    }
    result.append("; foreign keys:");
    for (int idx = 0; idx < getForeignKeyCount(); idx++) {
      result.append(" ");
      result.append(getForeignKey(idx).toVerboseString());
    }

    return result.toString();
  }
}
