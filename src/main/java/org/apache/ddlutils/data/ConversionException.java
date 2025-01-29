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

/**
 * <p>A <strong>ConversionException</strong> indicates that a call to
 * <code>Converter.convert()</code> has failed to complete successfully.
 *
 * @since 1.3
 */
public class ConversionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The cause, deprecated.
     *
     * @deprecated Use {@link Throwable#getCause()}}.
     */
    @Deprecated
    protected Throwable cause;

    /**
     * Constructs a new exception with the specified message.
     *
     * @param message The message describing this exception
     */
    public ConversionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and root cause.
     *
     * @param message The message describing this exception
     * @param cause The root cause of this exception
     */
    public ConversionException(final String message, final Throwable cause) {
        super(message, cause);
        this.cause = cause;
    }

    /**
     * Constructs a new exception with the specified root cause.
     *
     * @param cause The root cause of this exception
     */
    public ConversionException(final Throwable cause) {
        super(cause);
        this.cause = cause;
    }

}
