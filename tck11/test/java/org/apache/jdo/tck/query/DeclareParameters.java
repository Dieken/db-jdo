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
 
package org.apache.jdo.tck.query;


import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.jdo.tck.pc.mylib.PCPoint;
import org.apache.jdo.tck.util.BatchTestRunner;

/**
 *<B>Title:</B> Declare Parameters
 *<BR>
 *<B>Keywords:</B> query
 *<BR>
 *<B>Assertion ID:</B> A14.6-10.
 *<BR>
 *<B>Assertion Description: </B> <code>Query.declareParameters(String parameters)</code> binds the parameter statements to the query instance. This method defines the parameter types and names which will be used by a subsequent <code>execute</code> method.
 */

public class DeclareParameters extends QueryTest {

    /** */
    private static final String ASSERTION_FAILED = 
        "Assertion A14.6-10 (DeclareParameters) failed: ";
    
    /**
     * The <code>main</code> is called when the class
     * is directly executed from the command line.
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        BatchTestRunner.run(DeclareParameters.class);
    }

    /** */
    public void test() {
        pm = getPM();

        initDatabase(pm, PCPoint.class);
        runTestDeclareParameters01(pm);
        runTestDeclareParameters02(pm);
        runTestDeclareParameters03(pm);
        
        pm.close();
        pm = null;
    }

    /** */
    private void runTestDeclareParameters01(PersistenceManager pm) {
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
    
            Query query = pm.newQuery();
            query.setClass(PCPoint.class);
            query.setCandidates(pm.getExtent(PCPoint.class, false));
            query.declareParameters("Integer param");
            query.setFilter("x == param");
            Object results = query.execute(new Integer(2));
    
            // check query result
            List expected = new ArrayList();
            Object p3 = new PCPoint(2, 2);
            expected.add(p3);
            expected = getFromInserted(expected);
            printOutput(results, expected);
            checkQueryResultWithoutOrder(ASSERTION_FAILED, results, expected);
            tx.commit();
            tx = null;
        }
        
        finally {
            if ((tx != null) && tx.isActive())
                tx.rollback();
        }
    }

    /** */
    private void runTestDeclareParameters02(PersistenceManager pm) {
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            
            Query query = pm.newQuery();
            query.setClass(PCPoint.class);
            query.setCandidates(pm.getExtent(PCPoint.class, false));
            query.declareParameters("Integer param1, Integer param2");
            query.setFilter("x == param1 && y == param2");
            Object results = query.execute(new Integer(2), new Integer(2));

            // check query result
            List expected = new ArrayList();
            Object p3 = new PCPoint(2, 2);
            expected.add(p3);
            expected = getFromInserted(expected);
            printOutput(results, expected);
            checkQueryResultWithoutOrder(ASSERTION_FAILED, results, expected);
            tx.commit();
            tx = null;
        }
        finally {
            if ((tx != null) && tx.isActive())
                tx.rollback();
        }
    }

    /** */
    void runTestDeclareParameters03(PersistenceManager pm) {
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();

            Query query = pm.newQuery();
            query.setClass(PCPoint.class);
            query.setCandidates(pm.getExtent(PCPoint.class, false));
            query.declareParameters("int a, int b");
            query.setFilter("x == a && y == b");
            Object results = query.execute(new Integer(1), new Integer(1));

            // check query result
            List expected = new ArrayList();
            Object p = new PCPoint(1, 1);
            expected.add(p);
            expected = getFromInserted(expected);
            printOutput(results, expected);
            checkQueryResultWithoutOrder(ASSERTION_FAILED, results, expected);
            tx.commit();
            tx = null;
        }
        finally {
            if ((tx != null) && tx.isActive())
                tx.rollback();
        }
    }
}
