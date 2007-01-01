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

package org.apache.jdo.tck.query.jdoql.variables;

import org.apache.jdo.tck.JDO_Test;
import org.apache.jdo.tck.pc.company.CompanyModelReader;
import org.apache.jdo.tck.pc.company.Employee;
import org.apache.jdo.tck.query.QueryElementHolder;
import org.apache.jdo.tck.query.QueryTest;
import org.apache.jdo.tck.util.BatchTestRunner;

/**
 *<B>Title:</B> Unconstrained Variables.
 *<BR>
 *<B>Keywords:</B> query
 *<BR>
 *<B>Assertion ID:</B> A14.6.5-1.
 *<BR>
 *<B>Assertion Description: </B>
 * A variable that is not constrained with an explicit contains clause 
 * is constrained by the extent of the persistence capable class 
 * (including subclasses).
 */
public class UnconstrainedVariable extends QueryTest {

    /** */
    private static final String ASSERTION_FAILED = 
        "Assertion A14.6.5-1 (UnconstrainedVariable) failed: ";
    
    /** 
     * The array of valid queries which may be executed as 
     * single string queries and as API queries.
     */
    private static final QueryElementHolder[] VALID_QUERIES = {
        new QueryElementHolder(
        /*UNIQUE*/      null,
        /*RESULT*/      null, 
        /*INTO*/        null, 
        /*FROM*/        Employee.class,
        /*EXCLUDE*/     null,
        /*WHERE*/       "this.hireDate > e.hireDate & e.personid = id",
        /*VARIABLES*/   "Employee e",
        /*PARAMETERS*/  "int id",
        /*IMPORTS*/     null,
        /*GROUP BY*/    null,
        /*ORDER BY*/    null,
        /*FROM*/        null,
        /*TO*/          null)
    };
    
    /** 
     * The expected results of valid queries.
     */
    private Object[] expectedResult = {
        getTransientCompanyModelInstancesAsList(new String[]{
                "emp2", "emp3", "emp4"})
    };
            
    /** Parameters of valid queries. */
    private Object[][] parameters = {
        {new Integer(1)},
    };
            
    /**
     * The <code>main</code> is called when the class
     * is directly executed from the command line.
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        BatchTestRunner.run(UnconstrainedVariable.class);
    }
    
    /** */
    public void testPositive() {
        if (isUnconstrainedVariablesSupported()) {
            for (int i = 0; i < VALID_QUERIES.length; i++) {
                executeAPIQuery(ASSERTION_FAILED, VALID_QUERIES[i], 
                        parameters[i], expectedResult[i]);
                executeSingleStringQuery(ASSERTION_FAILED, VALID_QUERIES[i], 
                        parameters[i], expectedResult[i]);
            }
        }
    }
    
    /**
     * @see JDO_Test#localSetUp()
     */
    protected void localSetUp() {
        addTearDownClass(CompanyModelReader.getTearDownClasses());
        loadAndPersistCompanyModel(getPM());
    }
}
