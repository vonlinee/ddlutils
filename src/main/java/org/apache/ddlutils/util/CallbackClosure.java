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

import org.apache.ddlutils.DdlUtilsException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A closure that determines a callback for the type of the object and calls it.
 * Note that inheritance is also taken into account. I.e. if the object is of
 * type B which is a subtype of A, and there is only a callback for type A,
 * then this one will be invoked. If there is however also a callback for type B,
 * then only this callback for type B will be invoked and not the one for type A.
 *
 * @version $Revision: $
 */
public class CallbackClosure<T> implements Consumer<T> {
  /**
   * The object on which the callbacks will be invoked.
   */
  private final Object callee;
  /**
   * The parameter types.
   */
  private final Class<?>[] parameterTypes;
  /**
   * The parameters.
   */
  private final Object[] parameters;
  /**
   * The position of the callback parameter type.
   */
  private int callbackTypePos = -1;
  /**
   * The cached callbacks.
   */
  private final Map<Class<?>, Method> callbacks = new HashMap<>();

  /**
   * Creates a new closure object.
   *
   * @param callee         The object on which the callbacks will be invoked
   * @param callbackName   The name of the callback method
   * @param parameterTypes The parameter types. This array has to contain one <code>null</code>
   *                       for the type of the object for which the callback is invoked.
   *                       <code>null</code> or an empty array is regarded to be the
   *                       same as an array containing a single <code>null</code>
   * @param parameters     The actual arguments. The value at the placeholder position
   *                       will be ignored. Can be <code>null</code> if no parameter types
   *                       where given
   */
  public CallbackClosure(Object callee, String callbackName, Class<?>[] parameterTypes, Object[] parameters) {
    this.callee = callee;

    if ((parameterTypes == null) || (parameterTypes.length == 0)) {
      this.parameterTypes = new Class[]{null};
      this.parameters = new Object[]{null};
      callbackTypePos = 0;
    } else {
      this.parameterTypes = new Class[parameterTypes.length];
      this.parameters = new Object[parameterTypes.length];

      for (int idx = 0; idx < parameterTypes.length; idx++) {
        if (parameterTypes[idx] == null) {
          if (callbackTypePos >= 0) {
            throw new IllegalArgumentException("The parameter types may contain null only once");
          }
          callbackTypePos = idx;
        } else {
          this.parameterTypes[idx] = parameterTypes[idx];
          this.parameters[idx] = parameters[idx];
        }
      }
      if (callbackTypePos < 0) {
        throw new IllegalArgumentException("The parameter types need to a null placeholder");
      }
    }

    Class<?> type = callee.getClass();

    // we're caching the callbacks
    do {
      Method[] methods = type.getDeclaredMethods();

      for (Method method : methods) {
        Class<?>[] paramTypes = method.getParameterTypes();

        method.setAccessible(true);
        if (method.getName().equals(callbackName) && typesMatch(paramTypes)) {
          callbacks.putIfAbsent(paramTypes[callbackTypePos], method);
        }
      }
      type = type.getSuperclass();
    }
    while ((type != null) && !type.equals(Object.class));
  }

  /**
   * Checks whether the given method parameter types match the expected ones.
   *
   * @param methodParamTypes The method parameter types
   * @return <code>true</code> if the parameter types match
   */
  private boolean typesMatch(Class<?>[] methodParamTypes) {
    if ((methodParamTypes == null) || (parameterTypes.length != methodParamTypes.length)) {
      return false;
    }
    for (int idx = 0; idx < parameterTypes.length; idx++) {
      if ((idx != callbackTypePos) && !parameterTypes[idx].equals(methodParamTypes[idx])) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(T obj) throws DdlUtilsException {
    LinkedList<Class<?>> queue = new LinkedList<>();

    queue.add(obj.getClass());
    while (!queue.isEmpty()) {
      Class<?> type = queue.removeFirst();
      Method callback = callbacks.get(type);

      if (callback != null) {
        try {
          parameters[callbackTypePos] = obj;
          callback.invoke(callee, parameters);
          return;
        } catch (InvocationTargetException ex) {
          throw new DdlUtilsException(ex.getTargetException());
        } catch (IllegalAccessException ex) {
          throw new DdlUtilsException(ex);
        }
      }
      if ((type.getSuperclass() != null) && !type.getSuperclass().equals(Object.class)) {
        queue.add(type.getSuperclass());
      }

      Class<?>[] baseInterfaces = type.getInterfaces();
      Collections.addAll(queue, baseInterfaces);
    }
  }
}
