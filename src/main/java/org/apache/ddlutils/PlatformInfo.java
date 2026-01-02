package org.apache.ddlutils;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.model.CascadeAction;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;

/**
 * Contains information about the database platform such as supported features and native type mappings.
 *
 * @version $Revision: 289996 $
 */
public class PlatformInfo {
  /**
   * The Log to which logging calls will be made.
   */
  private final Log _log = LogFactory.getLog(PlatformInfo.class);

  // properties influencing the definition of columns
  /**
   * Contains non-default mappings from jdbc to native types.
   */
  private final HashMap<Integer, String> nativeTypes = new HashMap<>();
  /**
   * Contains the jdbc types corresponding to the native types for non-default mappings.
   */
  private final HashMap<Integer, Integer> targetJdbcTypes = new HashMap<>();

  // properties influencing the specification of table constraints
  /**
   * Contains those JDBC types whose corresponding native types have a null value as the default value.
   */
  private final HashSet<Integer> typesWithNullDefault = new HashSet<>();
  /**
   * Contains those JDBC types whose corresponding native types are types that have a size on this platform.
   */
  private final HashSet<Integer> typesWithSize = new HashSet<>();
  /**
   * Contains the default sizes for those JDBC types whose corresponding native types require a size.
   */
  private final HashMap<Integer, Integer> typesDefaultSizes = new HashMap<>();
  /**
   * Contains those JDBC types whose corresponding native types are types that have precision and scale on this platform.
   */
  private final HashSet<Integer> typesWithPrecisionAndScale = new HashSet<>();
  /**
   * Contains the supported ON UPDATE actions.
   */
  private final HashSet<CascadeAction> supportedOnUpdateActions = new HashSet<>();
  /**
   * Contains the supported ON DELETE actions.
   */
  private final HashSet<CascadeAction> supportedOnDeleteActions = new HashSet<>();
  /**
   * Contains for each ON UPDATE action the list of equivalent actions.
   */
  private final HashMap<CascadeAction, Set<CascadeAction>> equivalentOnUpdateActions = new HashMap<>();
  /**
   * Contains for each ON DELETE action the list of equivalent actions.
   */
  private final HashMap<CascadeAction, Set<CascadeAction>> equivalentOnDeleteActions = new HashMap<>();
  /**
   * Whether the database requires the explicit stating of NULL as the default value.
   */
  private boolean nullAsDefaultValueRequired = false;
  /**
   * Whether default values can be defined for LONGVARCHAR/LONGVARBINARY columns.
   */
  private boolean defaultValuesForLongTypesSupported = true;

  // properties influencing the reading of models from live databases
  /**
   * Whether primary key constraints are embedded inside the creation of table statement.
   */
  private boolean primaryKeyEmbedded = true;
  /**
   * Whether primary key columns have to be required, too.
   */
  private boolean primaryKeyColumnsHaveToBeRequired = false;
  /**
   * Whether mixed identity and non-identity columns in a primary key is supported.
   */
  private boolean mixingIdentityAndNormalPrimaryKeyColumnsSupported = true;
  /**
   * Whether foreign key constraints are embedded inside the creation of table statement.
   */
  private boolean foreignKeysEmbedded = false;
  /**
   * Whether embedded foreign key constraints are explicitly named.
   */
  private boolean embeddedForeignKeysNamed = false;
  /**
   * Whether non-unique indices are supported.
   */
  private boolean indicesSupported = true;

  // other DDL/DML properties
  /**
   * Whether indices are embedded inside the creation of table statement.
   */
  private boolean indicesEmbedded = false;
  /**
   * Whether identity specification is supported for non-primary key columns.
   */
  private boolean nonPrimaryKeyIdentityColumnsSupported = true;
  /**
   * Whether multiple identity columns in the same table are supported.
   */
  private boolean multipleIdentityColumnsSupported = true;
  /**
   * Whether the auto-increment definition is done via the DEFAULT part of the column definition.
   */
  private boolean defaultValueUsedForIdentitySpec = false;
  /**
   * Whether primary key columns are automatically required.
   */
  private boolean primaryKeyColumnAutomaticallyRequired = false;
  /**
   * Whether identity columns are automatically required.
   */
  private boolean identityColumnAutomaticallyRequired = false;
  /**
   * Whether system indices (database-generated indices for primary and foreign keys) are returned when
   * reading a model from a database.
   */
  private boolean systemIndicesReturned = true;
  /**
   * Whether system indices for foreign keys are always non-unique or can be
   * unique (i.e. if a primary key column is used to establish the foreign key).
   */
  private boolean systemForeignKeyIndicesAlwaysNonUnique = false;
  /**
   * Whether the database returns a synthetic default value for non-identity required columns.
   */
  private boolean syntheticDefaultValueForRequiredReturned = false;
  /**
   * Whether the platform is able to determine auto increment status from an existing database.
   */
  private boolean identityStatusReadingSupported = true;
  /**
   * Whether comments are supported.
   */
  private boolean sqlCommentsSupported = true;
  /**
   * Whether delimited identifiers are supported or not.
   */
  private boolean delimitedIdentifiersSupported = true;
  /**
   * Whether an ALTER TABLE is needed to drop indexes.
   */
  private boolean alterTableForDropUsed = false;
  /**
   * Whether the platform allows for the explicit specification of values for identity columns in INSERT
   * and UPDATE statements.
   */
  private boolean identityOverrideAllowed = true;
  /**
   * Whether the values of identity columns can be read back from the database after insertion.
   */
  private boolean lastIdentityValueReadable = true;
  /**
   * Whether auto-commit mode for the reading of the values of identity columns after insertion
   * shall be used.
   */
  private boolean autoCommitModeForLastIdentityValueReading = true;
  /**
   * Specifies the maximum length that a table name can have for this database (-1 if there is no limit).
   */
  private int maxTableNameLength = -1;
  /**
   * Specifies the maximum length that a column name can have for this database (-1 if there is no limit).
   */
  private int maxColumnNameLength = -1;
  /**
   * Specifies the maximum length that a constraint name can have for this database (-1 if there is no limit).
   */
  private int maxConstraintNameLength = -1;
  /**
   * Specifies the maximum length that a foreign key name can have for this database (-1 if there is no limit).
   */
  private int maxForeignKeyNameLength = -1;

  /**
   * Whether the length of fixed char columns should be trimmed to the length of the column.
   */
  private boolean trimLengthFixedCharColumnValues = false;

  /**
   * The string used for delimiting SQL identifiers, e.g. table names, column names etc.
   */
  private String _delimiterToken = "\"";
  /**
   * The string used for escaping values when generating textual SQL statements.
   */
  private String _valueQuoteToken = "'";
  /**
   * The string that starts a comment.
   */
  private String _commentPrefix = "--";
  /**
   * The string that ends a comment.
   */
  private String _commentSuffix = "";
  /**
   * The text separating individual SQL commands.
   */
  private String _sqlCommandDelimiter = ";";

  /**
   * Some JDBC drivers do not allow us to perform the query without an explicit alias
   */
  private boolean allowPerformQueryWithoutExplicitAlias = false;

  /**
   * The default ON UPDATE action.
   */
  private CascadeAction _defaultOnUpdateAction = CascadeAction.NONE;
  /**
   * The default ON DELETE action.
   */
  private CascadeAction _defaultOnDeleteAction = CascadeAction.NONE;

  /**
   * Creates a new platform info object.
   */
  public PlatformInfo() {
    typesWithNullDefault.add(Types.CHAR);
    typesWithNullDefault.add(Types.VARCHAR);
    typesWithNullDefault.add(Types.LONGVARCHAR);
    typesWithNullDefault.add(Types.CLOB);
    typesWithNullDefault.add(Types.BINARY);
    typesWithNullDefault.add(Types.VARBINARY);
    typesWithNullDefault.add(Types.LONGVARBINARY);
    typesWithNullDefault.add(Types.BLOB);

    typesWithSize.add(Types.CHAR);
    typesWithSize.add(Types.VARCHAR);
    typesWithSize.add(Types.BINARY);
    typesWithSize.add(Types.VARBINARY);

    typesWithPrecisionAndScale.add(Types.DECIMAL);
    typesWithPrecisionAndScale.add(Types.NUMERIC);

    supportedOnUpdateActions.addAll(Arrays.asList(CascadeAction.values()));
    supportedOnDeleteActions.addAll(Arrays.asList(CascadeAction.values()));
  }

  // properties influencing the definition of columns

  /**
   * Determines whether a NULL needs to be explicitly stated when the column
   * has no specified default value. Default is false.
   *
   * @return <code>true</code> if NULL must be written for empty default values
   */
  public boolean isNullAsDefaultValueRequired() {
    return nullAsDefaultValueRequired;
  }

  /**
   * Specifies whether a NULL needs to be explicitly stated when the column
   * has no specified default value. Default is false.
   *
   * @param requiresNullAsDefaultValue Whether NULL must be written for empty
   *                                   default values
   */
  public void setNullAsDefaultValueRequired(boolean requiresNullAsDefaultValue) {
    nullAsDefaultValueRequired = requiresNullAsDefaultValue;
  }

  /**
   * Determines whether default values can be specified for LONGVARCHAR/LONGVARBINARY columns.
   *
   * @return <code>true</code> if default values are allowed
   */
  public boolean isDefaultValuesForLongTypesSupported() {
    return defaultValuesForLongTypesSupported;
  }

  /**
   * Specifies whether default values can be specified for LONGVARCHAR/LONGVARBINARY columns.
   *
   * @param isSupported <code>true</code> if default values are supported
   */
  public void setDefaultValuesForLongTypesSupported(boolean isSupported) {
    defaultValuesForLongTypesSupported = isSupported;
  }

  // properties influencing the specification of table constraints

  /**
   * Determines whether primary key constraints are embedded in the creation
   * table clause or as separate alter table statements. The default is
   * embedded pks.
   *
   * @return <code>true</code> if pk constraints are embedded
   */
  public boolean isPrimaryKeyEmbedded() {
    return primaryKeyEmbedded;
  }

  /**
   * Specifies whether the primary key constraints are embedded in the creation
   * table clause or as separate alter table statements.
   *
   * @param primaryKeyEmbedded Whether pk constraints are embedded
   */
  public void setPrimaryKeyEmbedded(boolean primaryKeyEmbedded) {
    this.primaryKeyEmbedded = primaryKeyEmbedded;
  }

  /**
   * Determines whether the primary key constraints have to be required, as well.
   *
   * @return <code>true</code> if pk constraints have to be required
   */
  public boolean isPrimaryKeyColumnsHaveToBeRequired() {
    return primaryKeyColumnsHaveToBeRequired;
  }

  /**
   * Specifies whether the primary key constraints have to be required, as well.
   *
   * @param primaryKeyColumnsHaveToBeRequired Whether pk constraints need to be required
   */
  public void setPrimaryKeyColumnsHaveToBeRequired(boolean primaryKeyColumnsHaveToBeRequired) {
    this.primaryKeyColumnsHaveToBeRequired = primaryKeyColumnsHaveToBeRequired;
  }

  /**
   * Determines whether primary key can contain identity and non-identity columns at the same time.
   *
   * @return <code>true</code> if identity and non-identity columns can be mixed in the pk
   */
  public boolean isMixingIdentityAndNormalPrimaryKeyColumnsSupported() {
    return mixingIdentityAndNormalPrimaryKeyColumnsSupported;
  }

  /**
   * Specifies whether primary key can contain identity and non-identity columns at the same time.
   *
   * @param mixingIdentityAndNormalPrimaryKeyColumnsSupported Whether identity and non-identity columns
   *                                                          can be mixed in the pk
   */
  public void setMixingIdentityAndNormalPrimaryKeyColumnsSupported(boolean mixingIdentityAndNormalPrimaryKeyColumnsSupported) {
    this.mixingIdentityAndNormalPrimaryKeyColumnsSupported = mixingIdentityAndNormalPrimaryKeyColumnsSupported;
  }

  /**
   * Determines whether foreign key constraints are embedded in the creation
   * table clause or as separate alter table statements. Per default,
   * foreign keys are external.
   *
   * @return <code>true</code> if fk constraints are embedded
   */
  public boolean isForeignKeysEmbedded() {
    return foreignKeysEmbedded;
  }

  /**
   * Specifies whether foreign key constraints are embedded in the creation
   * table clause or as separate alter table statements.
   *
   * @param foreignKeysEmbedded Whether fk constraints are embedded
   */
  public void setForeignKeysEmbedded(boolean foreignKeysEmbedded) {
    this.foreignKeysEmbedded = foreignKeysEmbedded;
  }

  /**
   * Returns whether embedded foreign key constraints should have a name.
   *
   * @return <code>true</code> if embedded fks have name
   */
  public boolean isEmbeddedForeignKeysNamed() {
    return embeddedForeignKeysNamed;
  }

  /**
   * Specifies whether embedded foreign key constraints should be named.
   *
   * @param embeddedForeignKeysNamed Whether embedded fks shall have a name
   */
  public void setEmbeddedForeignKeysNamed(boolean embeddedForeignKeysNamed) {
    this.embeddedForeignKeysNamed = embeddedForeignKeysNamed;
  }

  /**
   * Determines whether indices are supported.
   *
   * @return <code>true</code> if indices are supported
   */
  public boolean isIndicesSupported() {
    return indicesSupported;
  }

  /**
   * Specifies whether indices are supported.
   *
   * @param supportingIndices <code>true</code> if indices are supported
   */
  public void setIndicesSupported(boolean supportingIndices) {
    indicesSupported = supportingIndices;
  }

  /**
   * Determines whether the indices are embedded in the creation table clause
   * or as separate statements. Per default, indices are external.
   *
   * @return <code>true</code> if indices are embedded
   */
  public boolean isIndicesEmbedded() {
    return indicesEmbedded;
  }

  /**
   * Specifies whether indices are embedded in the creation table clause or
   * as separate alter table statements.
   *
   * @param indicesEmbedded Whether indices are embedded
   */
  public void setIndicesEmbedded(boolean indicesEmbedded) {
    this.indicesEmbedded = indicesEmbedded;
  }

  /**
   * Determines whether non-primary key columns can be auto-incrementing (IDENTITY columns).
   *
   * @return <code>true</code> if normal non-PK columns can be auto-incrementing
   */
  public boolean isNonPrimaryKeyIdentityColumnsSupported() {
    return nonPrimaryKeyIdentityColumnsSupported;
  }

  /**
   * Specifies whether non-primary key columns can be auto-incrementing (IDENTITY columns).
   *
   * @param supportingNonPKIdentityColumns <code>true</code> if normal non-PK columns can
   *                                       be auto-incrementing
   */
  public void setNonPrimaryKeyIdentityColumnsSupported(boolean supportingNonPKIdentityColumns) {
    nonPrimaryKeyIdentityColumnsSupported = supportingNonPKIdentityColumns;
  }

  /**
   * Determines whether multiple columns in the same table can be auto-incrementing (IDENTITY columns).
   *
   * @return <code>true</code> if multiple columns can be auto-incrementing in the same table
   */
  public boolean isMultipleIdentityColumnsSupported() {
    return multipleIdentityColumnsSupported;
  }

  /**
   * Specifies whether multiple columns in the same table can be auto-incrementing (IDENTITY columns).
   *
   * @param supportingMultipleIdentityColumns <code>true</code> if multiple columns can be auto-incrementing
   *                                          in the same table
   */
  public void setMultipleIdentityColumnsSupported(boolean supportingMultipleIdentityColumns) {
    multipleIdentityColumnsSupported = supportingMultipleIdentityColumns;
  }

  /**
   * Determines whether the auto-increment specification uses the DEFAULT value of the
   * column definition.
   *
   * @return <code>true</code> if the auto-increment spec is done via the DEFAULT value
   */
  public boolean isDefaultValueUsedForIdentitySpec() {
    return defaultValueUsedForIdentitySpec;
  }

  /**
   * Specifies whether the auto-increment specification uses the DEFAULT value of the
   * column definition.
   *
   * @param identitySpecUsesDefaultValue <code>true</code> if the auto-increment spec is
   *                                     done via the DEFAULT value
   */
  public void setDefaultValueUsedForIdentitySpec(boolean identitySpecUsesDefaultValue) {
    defaultValueUsedForIdentitySpec = identitySpecUsesDefaultValue;
  }

  // properties influencing the reading of models from live databases

  /**
   * Determines whether the database will make a primary key column automatically required.
   *
   * @return <code>true</code> if primary key columns are automatically required
   */
  public boolean isPrimaryKeyColumnAutomaticallyRequired() {
    return primaryKeyColumnAutomaticallyRequired;
  }

  /**
   * Specifies whether the database will make a primary key column automatically required.
   *
   * @param primaryKeyAutomaticallyRequired <code>true</code> if primary key columns are
   *                                        automatically required
   */
  public void setPrimaryKeyColumnAutomaticallyRequired(boolean primaryKeyAutomaticallyRequired) {
    primaryKeyColumnAutomaticallyRequired = primaryKeyAutomaticallyRequired;
  }

  /**
   * Determines whether the database will make an identity column automatically required.
   *
   * @return <code>true</code> if identity columns are automatically required
   */
  public boolean isIdentityColumnAutomaticallyRequired() {
    return identityColumnAutomaticallyRequired;
  }

  /**
   * Specifies whether the database will make a primary key column automatically required.
   *
   * @param identityAutomaticallyRequired <code>true</code> if identity columns are
   *                                      automatically required
   */
  public void setIdentityColumnAutomaticallyRequired(boolean identityAutomaticallyRequired) {
    identityColumnAutomaticallyRequired = identityAutomaticallyRequired;
  }

  /**
   * Determines whether database-generated indices for primary and foreign keys are
   * returned when reading a model from a database.
   *
   * @return <code>true</code> if system indices are read from a live database
   */
  public boolean isSystemIndicesReturned() {
    return systemIndicesReturned;
  }

  /**
   * Specifies whether database-generated indices for primary and foreign keys are
   * returned when reading a model from a database.
   *
   * @param returningSystemIndices <code>true</code> if system indices are read from
   *                               a live database
   */
  public void setSystemIndicesReturned(boolean returningSystemIndices) {
    systemIndicesReturned = returningSystemIndices;
  }

  /**
   * Determines whether system indices for foreign keys are always non-unique or can be
   * unique (i.e. if a primary key column is used to establish the foreign key).
   *
   * @return <code>true</code> if system foreign key indices are always non-unique;
   * default is <code>false</code>
   */
  public boolean isSystemForeignKeyIndicesAlwaysNonUnique() {
    return systemForeignKeyIndicesAlwaysNonUnique;
  }

  /**
   * Specifies whether system indices for foreign keys are always non-unique or can be
   * unique (i.e. if a primary key column is used to establish the foreign key).
   *
   * @param alwaysNonUnique <code>true</code> if system foreign key indices are always
   *                        non-unique
   */
  public void setSystemForeignKeyIndicesAlwaysNonUnique(boolean alwaysNonUnique) {
    systemForeignKeyIndicesAlwaysNonUnique = alwaysNonUnique;
  }

  /**
   * Determines whether the platform returns synthetic default values (e.g. 0 for numeric
   * columns etc.) for non-identity required columns when reading a model from a database.
   *
   * @return <code>true</code> if synthetic default values are returned for non-identity
   * required columns
   */
  public boolean isSyntheticDefaultValueForRequiredReturned() {
    return syntheticDefaultValueForRequiredReturned;
  }

  /**
   * Specifies whether the platform returns synthetic default values (e.g. 0 for numeric
   * columns etc.) for non-identity required columns when reading a model from a database.
   *
   * @param returningDefaultValue <code>true</code> if synthetic default values are returned for
   *                              non-identity required columns
   */
  public void setSyntheticDefaultValueForRequiredReturned(boolean returningDefaultValue) {
    syntheticDefaultValueForRequiredReturned = returningDefaultValue;
  }

  /**
   * Determines whether the platform is able to read the auto-increment status for columns
   * from an existing database.
   *
   * @return <code>true</code> if the auto-increment status can be determined from an existing
   * database
   */
  public boolean getIdentityStatusReadingSupported() {
    return identityStatusReadingSupported;
  }

  /**
   * Specifies whether the platform is able to read the auto-increment status for columns
   * from an existing database.
   *
   * @param canReadAutoIncrementStatus <code>true</code> if the auto-increment status can be
   *                                   determined from an existing database
   */
  public void setIdentityStatusReadingSupported(boolean canReadAutoIncrementStatus) {
    identityStatusReadingSupported = canReadAutoIncrementStatus;
  }

  // other ddl properties

  /**
   * Determines whether the database supports SQL comments.
   *
   * @return <code>true</code> if comments are supported
   */
  public boolean isSqlCommentsSupported() {
    return sqlCommentsSupported;
  }

  /**
   * Specifies whether SQL comments are supported by the database.
   *
   * @param commentsSupported <code>true</code> if comments are supported
   */
  public void setSqlCommentsSupported(boolean commentsSupported) {
    sqlCommentsSupported = commentsSupported;
  }

  /**
   * Determines whether delimited identifiers are supported.
   *
   * @return <code>true</code> if delimited identifiers are supported
   */
  public boolean isDelimitedIdentifiersSupported() {
    return delimitedIdentifiersSupported;
  }

  /**
   * Specifies whether delimited identifiers are supported.
   *
   * @param areSupported <code>true</code> if delimited identifiers are supported
   */
  public void setDelimitedIdentifiersSupported(boolean areSupported) {
    delimitedIdentifiersSupported = areSupported;
  }

  /**
   * Determines whether an ALTER TABLE statement shall be used for dropping indices
   * or constraints.  The default is false.
   *
   * @return <code>true</code> if ALTER TABLE is required
   */
  public boolean isAlterTableForDropUsed() {
    return alterTableForDropUsed;
  }

  /**
   * Specifies whether an ALTER TABLE statement shall be used for dropping indices
   * or constraints.
   *
   * @param useAlterTableForDrop Whether ALTER TABLE will be used
   */
  public void setAlterTableForDropUsed(boolean useAlterTableForDrop) {
    alterTableForDropUsed = useAlterTableForDrop;
  }

  /**
   * Determines whether the platform is allowing the explicit specification of values for
   * identity columns in INSERT/UPDATE statements.
   *
   * @return <code>true</code> if values for identity columns can be specified
   */
  public boolean isIdentityOverrideAllowed() {
    return identityOverrideAllowed;
  }

  /**
   * Specifies whether the platform is allowing the explicit specification of values for
   * identity columns in INSERT/UPDATE statements.
   *
   * @param identityOverrideAllowed <code>true</code> if values for identity columns can be specified
   */
  public void setIdentityOverrideAllowed(boolean identityOverrideAllowed) {
    this.identityOverrideAllowed = identityOverrideAllowed;
  }

  /**
   * Determines whether the values of identity columns can be read back from the
   * database after insertion of a row.
   *
   * @return <code>true</code> if the identity column(s) can be read back
   */
  public boolean isLastIdentityValueReadable() {
    return lastIdentityValueReadable;
  }

  /**
   * Specifies whether the values of identity columns can be read back from the
   * database after insertion of a row.
   *
   * @param lastIdentityValueReadable <code>true</code> if the identity column(s) can be read back
   */
  public void setLastIdentityValueReadable(boolean lastIdentityValueReadable) {
    this.lastIdentityValueReadable = lastIdentityValueReadable;
  }

  /**
   * Determines whether auto-commit mode for the reading of the values of identity columns
   * after insertion shall be used, i.e. whether between the insertion of the row and the
   * reading of the database-generated identity value a commit is issued.
   *
   * @return <code>true</code> if auto-commit mode is used
   */
  public boolean isAutoCommitModeForLastIdentityValueReading() {
    return autoCommitModeForLastIdentityValueReading;
  }

  /**
   * Determines whether auto-commit mode for the reading of the values of identity columns
   * after insertion shall be used, i.e. whether between the insertion of the row and the
   * reading of the database-generated identity value a commit is issued.
   *
   * @param autoCommitModeForLastIdentityValueReading <code>true</code> if auto-commit mode
   *                                                  shall be used
   */
  public void setAutoCommitModeForLastIdentityValueReading(boolean autoCommitModeForLastIdentityValueReading) {
    this.autoCommitModeForLastIdentityValueReading = autoCommitModeForLastIdentityValueReading;
  }

  /**
   * Returns the maximum number of characters that a table name can have.
   *
   * @return The number of characters, or -1 if not limited
   */
  public int getMaxTableNameLength() {
    return maxTableNameLength;
  }

  /**
   * Sets the maximum length of table names that this database allows.
   *
   * @param maxTableNameLength The maximum length, -1 if unlimited
   */
  public void setMaxTableNameLength(int maxTableNameLength) {
    this.maxTableNameLength = maxTableNameLength;
  }

  /**
   * Returns the maximum number of characters that a column name can have.
   *
   * @return The number of characters, or -1 if not limited
   */
  public int getMaxColumnNameLength() {
    return maxColumnNameLength;
  }

  /**
   * Sets the maximum length of column names that this database allows.
   *
   * @param maxColumnNameLength The maximum length, -1 if unlimited
   */
  public void setMaxColumnNameLength(int maxColumnNameLength) {
    this.maxColumnNameLength = maxColumnNameLength;
  }

  /**
   * Returns the maximum number of characters that a constraint name can have.
   *
   * @return The number of characters, or -1 if not limited
   */
  public int getMaxConstraintNameLength() {
    return maxConstraintNameLength;
  }

  /**
   * Sets the maximum length of constraint names that this database allows.
   *
   * @param maxConstraintNameLength The maximum length, -1 if unlimited
   */
  public void setMaxConstraintNameLength(int maxConstraintNameLength) {
    this.maxConstraintNameLength = maxConstraintNameLength;
  }

  /**
   * Returns the maximum number of characters that a foreign key name can have.
   *
   * @return The number of characters, or -1 if not limited
   */
  public int getMaxForeignKeyNameLength() {
    return maxForeignKeyNameLength;
  }

  /**
   * Sets the maximum length of foreign key names that this database allows.
   *
   * @param maxForeignKeyNameLength The maximum length, -1 if unlimited
   */
  public void setMaxForeignKeyNameLength(int maxForeignKeyNameLength) {
    this.maxForeignKeyNameLength = maxForeignKeyNameLength;
  }

  /**
   * Sets the maximum length of all identifiers that this database allows.
   * Use this method if the length limit is the same for all kinds of identifiers.
   *
   * @param maxIdentifierLength The maximum identifier length, -1 if unlimited
   */
  public void setMaxIdentifierLength(int maxIdentifierLength) {
    maxTableNameLength = maxIdentifierLength;
    maxColumnNameLength = maxIdentifierLength;
    maxConstraintNameLength = maxIdentifierLength;
    maxForeignKeyNameLength = maxIdentifierLength;
  }

  /**
   * Returns the text that is used to delimit identifiers (e.g. table names).
   * Per default, this is a double quotation character (").
   *
   * @return The delimiter text
   */
  public String getDelimiterToken() {
    return _delimiterToken;
  }

  /**
   * Sets the text that is used to delimit identifiers (e.g. table names).
   *
   * @param delimiterToken The delimiter text
   */
  public void setDelimiterToken(String delimiterToken) {
    _delimiterToken = delimiterToken;
  }

  /**
   * Returns the text that is used for quoting values (e.g. text) when
   * printing default values and in generates insert/update/delete statements.
   * Per default, this is a single quotation character (').
   *
   * @return The quote text
   */
  public String getValueQuoteToken() {
    return _valueQuoteToken;
  }

  /**
   * Sets the text that is used for quoting values (e.g. text) when
   * printing default values and in generates insert/update/delete statements.
   *
   * @param valueQuoteChar The new quote text
   */
  public void setValueQuoteToken(String valueQuoteChar) {
    _valueQuoteToken = valueQuoteChar;
  }

  /**
   * Returns the string that denotes the beginning of a comment.
   *
   * @return The comment prefix
   */
  public String getCommentPrefix() {
    return _commentPrefix;
  }

  /**
   * Sets the text that starts a comment.
   *
   * @param commentPrefix The new comment prefix
   */
  public void setCommentPrefix(String commentPrefix) {
    _commentPrefix = (commentPrefix == null ? "" : commentPrefix);
  }

  /**
   * Returns the string that denotes the end of a comment. Note that comments will
   * be always on their own line.
   *
   * @return The comment suffix
   */
  public String getCommentSuffix() {
    return _commentSuffix;
  }

  /**
   * Sets the text that ends a comment.
   *
   * @param commentSuffix The new comment suffix
   */
  public void setCommentSuffix(String commentSuffix) {
    _commentSuffix = (commentSuffix == null ? "" : commentSuffix);
  }

  /**
   * Returns the text separating individual SQL commands.
   *
   * @return The delimiter text
   */
  public String getSqlCommandDelimiter() {
    return _sqlCommandDelimiter;
  }

  /**
   * Sets the text separating individual SQL commands.
   *
   * @param sqlCommandDelimiter The delimiter text
   */
  public void setSqlCommandDelimiter(String sqlCommandDelimiter) {
    _sqlCommandDelimiter = sqlCommandDelimiter;
  }

  /**
   * Returns the database-native type for the given type code.
   *
   * @param typeCode The {@link java.sql.Types} type code
   * @return The native type or <code>null</code> if there isn't one defined
   */
  public String getNativeType(int typeCode) {
    return nativeTypes.get(typeCode);
  }

  /**
   * Returns the jdbc type corresponding to the native type that is used for the given
   * jdbc type. This is most often the same jdbc type, but can also be a different one.
   * For instance, if a database has no native boolean type, then the source jdbc type
   * would be <code>BIT</code> or <code>BOOLEAN</code>, and the target jdbc type might
   * be <code>TINYINT</code> or <code>SMALLINT</code>.
   *
   * @param typeCode The {@link java.sql.Types} type code
   * @return The target jdbc type
   */
  public int getTargetJdbcType(int typeCode) {
    Integer targetJdbcType = targetJdbcTypes.get(typeCode);

    return targetJdbcType == null ? typeCode : targetJdbcType;
  }

  /**
   * Adds a mapping from jdbc type to database-native type.
   *
   * @param jdbcTypeCode The jdbc type code as defined by {@link java.sql.Types}
   * @param nativeType   The native type
   */
  public void addNativeTypeMapping(int jdbcTypeCode, String nativeType) {
    nativeTypes.put(jdbcTypeCode, nativeType);
  }

  /**
   * Adds a mapping from jdbc type to database-native type.
   *
   * @param jdbcTypeCode       The jdbc type code as defined by {@link java.sql.Types}
   * @param nativeType         The native type
   * @param targetJdbcTypeCode The jdbc type code corresponding to the native type
   *                           (e.g. when reading the model from the database)
   */
  public void addNativeTypeMapping(int jdbcTypeCode, String nativeType, int targetJdbcTypeCode) {
    addNativeTypeMapping(jdbcTypeCode, nativeType);
    targetJdbcTypes.put(jdbcTypeCode, targetJdbcTypeCode);
  }

  /**
   * Adds a mapping from jdbc type to database-native type. Note that this
   * method accesses the named constant in {@link java.sql.Types} via reflection
   * and is thus safe to use under JDK 1.2/1.3 even with constants defined
   * only in later Java versions - for these, the method simply will not add
   * a mapping.
   *
   * @param jdbcTypeName The jdbc type name, one of the constants defined in
   *                     {@link java.sql.Types}
   * @param nativeType   The native type
   */
  public void addNativeTypeMapping(String jdbcTypeName, String nativeType) {
    try {
      Field constant = Types.class.getField(jdbcTypeName);

      addNativeTypeMapping(constant.getInt(null), nativeType);
    } catch (Exception ex) {
      // ignore -> won't be defined
      _log.warn("Cannot add native type mapping for undefined jdbc type " + jdbcTypeName, ex);
    }
  }

  /**
   * Adds a mapping from jdbc type to database-native type. Note that this
   * method accesses the named constant in {@link java.sql.Types} via reflection
   * and is thus safe to use under JDK 1.2/1.3 even with constants defined
   * only in later Java versions - for these, the method simply will not add
   * a mapping.
   *
   * @param jdbcTypeName       The jdbc type name, one of the constants defined
   *                           in {@link java.sql.Types}
   * @param nativeType         The native type
   * @param targetJdbcTypeName The jdbc type corresponding to the native type
   *                           (e.g. when reading the model from the database)
   */
  public void addNativeTypeMapping(String jdbcTypeName, String nativeType, String targetJdbcTypeName) {
    try {
      Field sourceType = Types.class.getField(jdbcTypeName);
      Field targetType = Types.class.getField(targetJdbcTypeName);

      addNativeTypeMapping(sourceType.getInt(null), nativeType, targetType.getInt(null));
    } catch (Exception ex) {
      // ignore -> won't be defined
      _log.warn("Cannot add native type mapping for undefined jdbc type " + jdbcTypeName + ", target jdbc type " + targetJdbcTypeName, ex);
    }
  }

  /**
   * Determines whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has a null default value on this platform.
   *
   * @param sqlTypeCode The SQL type code
   * @return <code>true</code> if the native type has a null default value
   */
  public boolean hasNullDefault(int sqlTypeCode) {
    return typesWithNullDefault.contains(sqlTypeCode);
  }

  /**
   * Specifies whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has a null default value on this platform.
   *
   * @param sqlTypeCode    The SQL type code
   * @param hasNullDefault <code>true</code> if the native type has a null default value
   */
  public void setHasNullDefault(int sqlTypeCode, boolean hasNullDefault) {
    if (hasNullDefault) {
      typesWithNullDefault.add(sqlTypeCode);
    } else {
      typesWithNullDefault.remove(sqlTypeCode);
    }
  }

  /**
   * Determines whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has a size specification on this platform.
   *
   * @param sqlTypeCode The SQL type code
   * @return <code>true</code> if the native type has a size specification
   */
  public boolean hasSize(int sqlTypeCode) {
    return typesWithSize.contains(sqlTypeCode);
  }

  /**
   * Specifies whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has a size specification on this platform.
   *
   * @param sqlTypeCode The SQL type code
   * @param hasSize     <code>true</code> if the native type has a size specification
   */
  public void setHasSize(int sqlTypeCode, boolean hasSize) {
    if (hasSize) {
      typesWithSize.add(sqlTypeCode);
    } else {
      typesWithSize.remove(sqlTypeCode);
    }
  }

  /**
   * Returns the default size value for the given type, if any.
   *
   * @param jdbcTypeCode The jdbc type code
   * @return The default size or <code>null</code> if none is defined
   */
  public Integer getDefaultSize(int jdbcTypeCode) {
    return typesDefaultSizes.get(jdbcTypeCode);
  }

  /**
   * Adds a default size for the given jdbc type.
   *
   * @param jdbcTypeCode The jdbc type code
   * @param defaultSize  The default size
   */
  public void setDefaultSize(int jdbcTypeCode, int defaultSize) {
    typesDefaultSizes.put(jdbcTypeCode, defaultSize);
  }

  /**
   * Adds a default size for the given jdbc type.
   *
   * @param jdbcTypeName The name of the jdbc type, one of the {@link Types} constants
   * @param defaultSize  The default size
   */
  public void setDefaultSize(String jdbcTypeName, int defaultSize) {
    try {
      Field constant = Types.class.getField(jdbcTypeName);

      setDefaultSize(constant.getInt(null), defaultSize);
    } catch (Exception ex) {
      // ignore -> won't be defined
      _log.warn("Cannot add default size for undefined jdbc type " + jdbcTypeName, ex);
    }
  }

  /**
   * Determines whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has precision and scale specifications on
   * this platform.
   *
   * @param sqlTypeCode The SQL type code
   * @return <code>true</code> if the native type has precision and scale specifications
   */
  public boolean hasPrecisionAndScale(int sqlTypeCode) {
    return typesWithPrecisionAndScale.contains(sqlTypeCode);
  }

  /**
   * Specifies whether the native type for the given SQL type code (one of the
   * {@link java.sql.Types} constants) has precision and scale specifications on
   * this platform.
   *
   * @param sqlTypeCode          The SQL type code
   * @param hasPrecisionAndScale <code>true</code> if the native type has precision and scale specifications
   */
  public void setHasPrecisionAndScale(int sqlTypeCode, boolean hasPrecisionAndScale) {
    if (hasPrecisionAndScale) {
      typesWithPrecisionAndScale.add(sqlTypeCode);
    } else {
      typesWithPrecisionAndScale.remove(sqlTypeCode);
    }
  }

  /**
   * Sets the actions that this platform supports for ON UPDATE.
   *
   * @param actions The actions
   */
  public void setSupportedOnUpdateActions(CascadeAction... actions) {
    supportedOnUpdateActions.clear();
    supportedOnUpdateActions.addAll(Arrays.asList(actions));
  }

  /**
   * Determines whether the given action is supported for ON UPDATE on this platform.
   *
   * @param action The action
   * @return <code>true</code> if the action is supported
   */
  public boolean isActionSupportedForOnUpdate(CascadeAction action) {
    return supportedOnUpdateActions.contains(action);
  }

  /**
   * Sets the actions that this platform supports for ON DELETE.
   *
   * @param actions The actions
   */
  public void setSupportedOnDeleteActions(CascadeAction... actions) {
    supportedOnDeleteActions.clear();
    supportedOnDeleteActions.addAll(Arrays.asList(actions));
  }

  /**
   * Determines whether the given action is supported for ON DELETE on this platform.
   *
   * @param action The action
   * @return <code>true</code> if the action is supported
   */
  public boolean isActionSupportedForOnDelete(CascadeAction action) {
    return supportedOnDeleteActions.contains(action);
  }

  /**
   * Returns the default ON UPDATE action that is used if none is specified.
   *
   * @return The default action
   */
  public CascadeAction getDefaultOnUpdateAction() {
    return _defaultOnUpdateAction;
  }

  /**
   * Sets the default ON UPDATE action that is used if none is specified.
   *
   * @param defaultOnUpdateAction The default action
   */
  public void setDefaultOnUpdateAction(CascadeAction defaultOnUpdateAction) {
    _defaultOnUpdateAction = defaultOnUpdateAction;
  }

  /**
   * Returns the default ON DELETE action that is used if none is specified.
   *
   * @return The default action
   */
  public CascadeAction getDefaultOnDeleteAction() {
    return _defaultOnDeleteAction;
  }

  /**
   * Sets the default ON DELETE action that is used if none is specified.
   *
   * @param defaultOnDeleteAction The default action
   */
  public void setDefaultOnDeleteAction(CascadeAction defaultOnDeleteAction) {
    _defaultOnDeleteAction = defaultOnDeleteAction;
  }

  /**
   * Registers the given pair of ON UPDATE actions to be equivalent. Equivalent actions will not
   * cause a foreign key to be changed/recreated when altering a database.
   *
   * @param actionA The first action
   * @param actionB The second action
   */
  public void addEquivalentOnUpdateActions(CascadeAction actionA, CascadeAction actionB) {
    if (Objects.equals(actionA, actionB)) {
      return;
    }
    equivalentOnUpdateActions.computeIfAbsent(actionA, k -> new HashSet<>()).add(actionB);
    equivalentOnUpdateActions.computeIfAbsent(actionB, k -> new HashSet<>()).add(actionA);
  }

  /**
   * Determines whether the two ON UPDATE actions are equivalent. Equivalent actions will not
   * cause a foreign key to be changed/recreated when altering a database.
   *
   * @param actionA The first action
   * @param actionB The second action
   * @return <code>true</code> if the two actions are equivalent
   */
  public boolean areEquivalentOnUpdateActions(CascadeAction actionA, CascadeAction actionB) {
    Set<CascadeAction> actionsEquivalentToActionA = equivalentOnUpdateActions.get(actionA);
    return actionsEquivalentToActionA != null && actionsEquivalentToActionA.contains(actionB);
  }

  /**
   * Registers the given pair of ON DELETE actions to be equivalent. Equivalent actions will not
   * cause a foreign key to be changed/recreated when altering a database.
   *
   * @param actionA The first action
   * @param actionB The second action
   */
  public void addEquivalentOnDeleteActions(CascadeAction actionA, CascadeAction actionB) {
    if (Objects.equals(actionA, actionB)) {
      return;
    }
    equivalentOnDeleteActions.computeIfAbsent(actionA, k -> new HashSet<>()).add(actionB);
    equivalentOnDeleteActions.computeIfAbsent(actionB, k -> new HashSet<>()).add(actionA);
  }

  /**
   * Determines whether the two ON DELETE actions are equivalent. Equivalent actions will not
   * cause a foreign key to be changed/recreated when altering a database.
   *
   * @param actionA The first action
   * @param actionB The second action
   * @return <code>true</code> if the two actions are equivalent
   */
  public boolean areEquivalentOnDeleteActions(CascadeAction actionA, CascadeAction actionB) {
    Set<CascadeAction> actionsEquivalentToActionA = equivalentOnDeleteActions.get(actionA);
    return actionsEquivalentToActionA != null && actionsEquivalentToActionA.contains(actionB);
  }

  public boolean isTrimLengthFixedCharColumnValues() {
    return trimLengthFixedCharColumnValues;
  }

  public void setTrimLengthFixedCharColumnValues(boolean trimLengthFixedCharColumnValues) {
    this.trimLengthFixedCharColumnValues = trimLengthFixedCharColumnValues;
  }

  public boolean isAllowPerformQueryWithoutExplicitAlias() {
    return allowPerformQueryWithoutExplicitAlias;
  }

  public void setAllowPerformQueryWithoutExplicitAlias(boolean allowPerformQueryWithoutExplicitAlias) {
    this.allowPerformQueryWithoutExplicitAlias = allowPerformQueryWithoutExplicitAlias;
  }
}
