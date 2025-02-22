package org.apache.ddlutils.util;

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

/**
 * Represents a pair of objects.
 *
 * @version $Revision: $
 */
public class Pair<K, V> {
  /**
   * The first object.
   */
  private final K _firstObj;
  /**
   * The first object.
   */
  private final V _secondObj;

  /**
   * Creates a pair object.
   *
   * @param firstObj  The first object
   * @param secondObj The second object
   */
  public Pair(K firstObj, V secondObj) {
    _firstObj = firstObj;
    _secondObj = secondObj;
  }

  /**
   * Returns the first object of the pair.
   *
   * @return The first object
   */
  public K getFirst() {
    return _firstObj;
  }

  /**
   * Returns the second object of the pair.
   *
   * @return The second object
   */
  public V getSecond() {
    return _secondObj;
  }
}
