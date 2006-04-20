/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
 

package org.apache.jdo.tck.pc.inheritance;

/** */
public class TopNonPersistD extends TopNonPersistC { // not persistent
    
    public boolean booleanD;
    
    public TopNonPersistD() {
        booleanD = false;
    }
    
    public TopNonPersistD (int int1, double doubleB, int intB, char charC, boolean booleanVal) {
        super(int1, doubleB, intB,charC);
        booleanD = booleanVal;
    }
}