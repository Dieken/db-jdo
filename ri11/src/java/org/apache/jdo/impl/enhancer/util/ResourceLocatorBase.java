/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.apache.jdo.impl.enhancer.util;

import java.io.PrintWriter;



/**
 * Base class for resource locators.
 */
abstract class ResourceLocatorBase
    extends Support
    implements ResourceLocator
{
    // misc
    protected final PrintWriter out;
    protected final boolean verbose;

    /**
     * Creates an instance.
     */
    public ResourceLocatorBase(PrintWriter out,
                               boolean verbose)
    {
        affirm(out != null);
        this.out = out;
        this.verbose = verbose;
    }

    /**
     *  Prints out a verbose message.
     *
     *  @param msg the message
     */
    public void printMessage(String msg)
    {
        if (verbose) {
            out.println(getI18N("enhancer.message", msg));
        }
    }
}