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

package org.apache.jdo.tck.query.jdoql;

import java.util.Collection;
import java.util.HashSet;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.jdo.tck.pc.company.CompanyModelReader;
import org.apache.jdo.tck.pc.company.Department;
import org.apache.jdo.tck.pc.company.Employee;
import org.apache.jdo.tck.query.QueryTest;
import org.apache.jdo.tck.util.BatchTestRunner;

/**
 *<B>Title:</B> Cast Query Operator
 *<BR>
 *<B>Keywords:</B> query
 *<BR>
 *<B>Assertion ID:</B> A14.6.2-38.
 *<BR>
 *<B>Assertion Description: </B>
The cast operator can be used for type conversions on classes.

 */

public class Cast extends QueryTest {

    /** */
    private static final String ASSERTION_FAILED = 
        "Assertion A14.6.2-38 (Cast) failed: ";
    
    /**
     * The <code>main</code> is called when the class
     * is directly executed from the command line.
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        BatchTestRunner.run(Cast.class);
    }
    
    /** */
    public void test() {
        pm = getPM();
        
        try {
            // read test data
            CompanyModelReader reader = loadCompanyModel(pm, COMPANY_TESTDATA);
            runTest(pm, reader);
        }
        finally {
            cleanupCompanyModel(pm);
            pm.close();
            pm = null;
        }
    }
    
    /** */
    void runTest(PersistenceManager pm, CompanyModelReader reader) {
        Query q;
        Object result;
        Collection expected;
        
        Transaction tx = pm.currentTransaction();
        tx.begin();
                
        q = pm.newQuery(Employee.class);
        q.declareImports("import org.apache.jdo.tck.pc.company.FullTimeEmployee");
        q.setFilter("((FullTimeEmployee)this).salary > 15000.0");
        result = q.execute();
        expected = new HashSet();
        expected.add(reader.getFullTimeEmployee("emp1"));
        expected.add(reader.getFullTimeEmployee("emp5"));
        checkQueryResultWithoutOrder(ASSERTION_FAILED, result, expected);
        
        q = pm.newQuery(Department.class);
        q.declareVariables("Employee e");
        q.declareImports("import org.apache.jdo.tck.pc.company.FullTimeEmployee");
        q.setFilter("employees.contains(e) && ((FullTimeEmployee)e).salary > 15000.0");
        result = q.execute();
        expected = new HashSet();
        expected.add(reader.getDepartment("dept1"));
        expected.add(reader.getDepartment("dept2"));
        checkQueryResultWithoutOrder(ASSERTION_FAILED, result, expected);
        
        tx.commit();
    }
}