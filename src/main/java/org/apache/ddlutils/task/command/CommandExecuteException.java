/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ddlutils.task.command;

import org.apache.ddlutils.task.TaskException;

/**
 * Signals an error condition during a build
 */
public class CommandExecuteException extends TaskException {

  private static final long serialVersionUID = -5419014565354664240L;

  /**
   * Constructs an exception with the given descriptive message.
   *
   * @param message A description of or information about the exception.
   *                Should not be <code>null</code>.
   */
  public CommandExecuteException(String message) {
    super(message);
  }

  /**
   * Constructs an exception with the given message and exception as
   * a root cause.
   *
   * @param message A description of or information about the exception.
   *                Should not be <code>null</code> unless a cause is specified.
   * @param cause   The exception that might have caused this one.
   *                May be <code>null</code>.
   */
  public CommandExecuteException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs an exception with the given exception as a root cause.
   *
   * @param cause The exception that might have caused this one.
   *              Should not be <code>null</code>.
   */
  public CommandExecuteException(Throwable cause) {
    super(cause);
  }

  /**
   * Returns the nested exception, if any.
   *
   * @return the nested exception, or <code>null</code> if no
   * exception is associated with this one
   * @deprecated Use {@link #getCause} instead.
   */
  public Throwable getException() {
    return getCause();
  }
}
