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

package org.apache.jdo.tck.query.jdoql.operators;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.apache.jdo.tck.pc.mylib.PrimitiveTypes;
import org.apache.jdo.tck.query.QueryTest;
import org.apache.jdo.tck.util.BatchTestRunner;

/**
 *<B>Title:</B> Binary Addition Query Operator
 *<BR>
 *<B>Keywords:</B> query
 *<BR>
 *<B>Assertion ID:</B> A14.6.2-26.
 *<BR>
 *<B>Assertion Description: </B> The binary addition operator (<code>+</code>) is supported for all types as they are defined in the Java language. This includes the following types:
<UL>
<LI><code>byte, short, int, long, char, Byte, Short Integer, Long, Character</code></LI>
<LI><code>float, double, Float, Double</code></LI>
<LI><code>BigDecimal, BigInteger</code></LI>
</UL>
The operation on object-valued fields of wrapper types (<code>Boolean, Byte, Short, Integer, Long, Float</code>, and <code>Double</code>), and numeric types (<code>BigDecimal</code> and <code>BigInteger</code>) use the wrapped values as operands.
 */

public class BinaryAddition extends QueryTest {

    /** */
    private static final String ASSERTION_FAILED = 
        "Assertion A14.6.2-26 (BinaryAddition) failed: ";
    
    /**
     * The <code>main</code> is called when the class
     * is directly executed from the command line.
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        BatchTestRunner.run(BinaryAddition.class);
    }

    /** */
    public void testPositive() {
        PersistenceManager pm = getPM();
        if (debug) logger.debug("\nExecuting test BinaryAddition() ...");

        Transaction tx = pm.currentTransaction();
        tx.begin();
        
        List<PrimitiveTypes> instance9 = pm.newQuery(
            PrimitiveTypes.class, "id == 9").executeList();
        List<PrimitiveTypes> allOddInstances = pm.newQuery(
            PrimitiveTypes.class, "booleanNull").executeList();
        
        runSimplePrimitiveTypesQuery("id + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("byteNotNull + 1 == 10",
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("shortNotNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);            
        runSimplePrimitiveTypesQuery("intNotNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);            
        runSimplePrimitiveTypesQuery("longNotNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("floatNotNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("doubleNotNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("byteNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("shortNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("intNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("longNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("floatNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("doubleNull + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("bigDecimal + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("bigInteger + 1 == 10", 
                                     pm, instance9, ASSERTION_FAILED);

        runSimplePrimitiveTypesQuery("charNull + 1 == 'P'", 
                                     pm, allOddInstances, ASSERTION_FAILED);
        runSimplePrimitiveTypesQuery("charNotNull + 1 == 'P'", 
                                     pm, allOddInstances, ASSERTION_FAILED);

        tx.commit();
    }

    /**
     * @see org.apache.jdo.tck.JDO_Test#localSetUp()
     */
    @Override
    protected void localSetUp() {
        addTearDownClass(PrimitiveTypes.class);
        loadAndPersistPrimitiveTypes(getPM());
    }
}

