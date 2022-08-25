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

package org.apache.jdo.tck.pc.order;

import javax.jdo.PersistenceManager;

/*
 * OrderFactoryPMClass.java
 *
 * This class uses the PersistenceManager.newInstance method with the concrete
 * class as a parameter. 
 */
public class OrderFactoryPMClass 
        extends OrderFactoryAbstractImpl {
    
    /** */
    @SuppressWarnings("rawtypes")
    public static final Class<?>[] tearDownClasses = new Class[] {
        Order.class, OrderItem.class
    };

    /**
     * Creates a new instance of OrderFactoryPMClass
     * @param pm the PersistnceManager
     */
    public OrderFactoryPMClass(PersistenceManager pm) {
        super(pm);
    }
    
    public Order newOrder() {
        return pm.newInstance(Order.class);
    }
    
    public OrderItem newOrderItem() {
        return pm.newInstance(OrderItem.class);
    }
    
    public Class<?>[] getTearDownClasses() {
        return tearDownClasses;
    }
}
