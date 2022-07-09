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

package org.apache.jdo.tck.query.result;

import java.util.Arrays;

import org.apache.jdo.tck.pc.company.CompanyModelReader;
import org.apache.jdo.tck.pc.company.Company;
import org.apache.jdo.tck.pc.company.QCompany;
import org.apache.jdo.tck.pc.company.QDepartment;
import org.apache.jdo.tck.pc.company.QEmployee;
import org.apache.jdo.tck.pc.company.QProject;
import org.apache.jdo.tck.query.QueryElementHolder;
import org.apache.jdo.tck.query.QueryTest;
import org.apache.jdo.tck.util.BatchTestRunner;

import javax.jdo.JDOQLTypedQuery;

/**
 *<B>Title:</B> Variable in Result.
 *<BR>
 *<B>Keywords:</B> query
 *<BR>
 *<B>Assertion ID:</B> A14.6.9-3.
 *<BR>
 *<B>Assertion Description: </B>
 * If a variable or a field of a variable is included in the result, 
 * either directly or via navigation through the variable, 
 * then the semantics of the contains clause that include the variable change. 
 * In this case, all values of the variable 
 * that satisfy the filter are included in the result.
 * Result expressions begin with either an instance of the candidate class 
 * (with an explicit or implicit "this") or an instance of a variable 
 * (using the variable name). The candidate tuples are the cartesian product 
 * of the candidate class and all variables used in the result. The result 
 * tuples are the tuples of the candidate class and all variables used 
 * in the result that satisfy the filter. 
 * The result is the collection of result expressions projected from the 
 * result tuples. 
 * 
 * This test differs from VariableInResult by extending the navigation
 * of variables. It navigates from the candidate Department class to include
 * fields in the corresponding Company. It navigates from the candidate
 * Company class to Department, Employee, and Project.
 */
public class VariableInResultMultipleNavigation extends QueryTest {

    /** */
    private static final String ASSERTION_FAILED = 
        "Assertion A14.6.9-3 (VariableInResult) failed: ";

    /** 
     * The expected results of valid queries.
     */
    private final Object company1 = getTransientCompanyModelInstance("company1");
    private final Object emp1 = getTransientCompanyModelInstance("emp1");
    private final Object emp2 = getTransientCompanyModelInstance("emp2");
    private final Object emp3 = getTransientCompanyModelInstance("emp3");
    private final Object emp4 = getTransientCompanyModelInstance("emp4");
    private final Object emp5 = getTransientCompanyModelInstance("emp5");
    private final Object proj1 = getTransientCompanyModelInstance("proj1");
    private final Object proj2 = getTransientCompanyModelInstance("proj2");
    private final Object proj3 = getTransientCompanyModelInstance("proj3");
    private final Object dept1 = getTransientCompanyModelInstance("dept1");
    private final Object dept2 = getTransientCompanyModelInstance("dept2");

    /**
     * The <code>main</code> is called when the class
     * is directly executed from the command line.
     * @param args The arguments passed to the program.
     */
    public static void main(String[] args) {
        BatchTestRunner.run(VariableInResult.class);
    }
    
    /** */
    public void testNavigationWithCompanyAndDepartmentAndEmployeeAndProject() {
        Object expected = Arrays.asList(
                new Object[] {company1, dept1, emp1, proj1},
                new Object[] {company1, dept1, emp2, proj1},
                new Object[] {company1, dept1, emp3, proj1},
                new Object[] {company1, dept1, emp2, proj2},
                new Object[] {company1, dept1, emp3, proj2},
                new Object[] {company1, dept2, emp4, proj3},
                new Object[] {company1, dept2, emp5, proj3});

        JDOQLTypedQuery<Company> query = getPM().newJDOQLTypedQuery(Company.class);
        QCompany cand = QCompany.candidate();
        QDepartment d = QDepartment.variable("d");
        QEmployee e = QEmployee.variable("e");
        QProject p = QProject.variable("p");
        query.filter(cand.name.eq("Sun Microsystems, Inc.").and(
                cand.departments.contains(d)).and(d.employees.contains(e)).and(e.projects.contains(p)));
        query.result(false, cand, d, e, p);

        QueryElementHolder holder = new QueryElementHolder(
                /*UNIQUE*/      null,
                /*RESULT*/      "this, d, e, p",
                /*INTO*/        null,
                /*FROM*/        Company.class,
                /*EXCLUDE*/     null,
                /*WHERE*/       "name == \"Sun Microsystems, Inc.\" && " +
                "departments.contains(d) && d.employees.contains(e) && e.projects.contains(p)",
                /*VARIABLES*/   "Department d; Employee e; Project p",
                /*PARAMETERS*/  null,
                /*IMPORTS*/     null,
                /*GROUP BY*/    null,
                /*ORDER BY*/    null,
                /*FROM*/        null,
                /*TO*/          null,
                /*JDOQLTyped*/  query,
                /*paramValues*/ null);

        executeAPIQuery(ASSERTION_FAILED, holder, expected);
        executeSingleStringQuery(ASSERTION_FAILED, holder, expected);
        executeJDOQLTypedQuery(ASSERTION_FAILED, holder, null, true, expected);
    }

    /** */
    public void testNavigationWithCompanyAndEmployeeAndProject() {
        Object expected = Arrays.asList(
                new Object[] {company1, emp1, proj1},
                new Object[] {company1, emp2, proj1},
                new Object[] {company1, emp3, proj1},
                new Object[] {company1, emp2, proj2},
                new Object[] {company1, emp3, proj2},
                new Object[] {company1, emp4, proj3},
                new Object[] {company1, emp5, proj3});

        JDOQLTypedQuery<Company> query = getPM().newJDOQLTypedQuery(Company.class);
        QCompany cand = QCompany.candidate();
        QDepartment d = QDepartment.variable("d");
        QEmployee e = QEmployee.variable("e");
        QProject p = QProject.variable("p");
        query.filter(cand.name.eq("Sun Microsystems, Inc.").and(
                cand.departments.contains(d)).and(d.employees.contains(e)).and(e.projects.contains(p)));
        query.result(false, cand, e, p);

        QueryElementHolder holder = new QueryElementHolder(
                /*UNIQUE*/      null,
                /*RESULT*/      "this, e, p",
                /*INTO*/        null,
                /*FROM*/        Company.class,
                /*EXCLUDE*/     null,
                /*WHERE*/       "name == \"Sun Microsystems, Inc.\" && " +
                "departments.contains(d) && d.employees.contains(e) && e.projects.contains(p)",
                /*VARIABLES*/   "Department d; Employee e; Project p",
                /*PARAMETERS*/  null,
                /*IMPORTS*/     null,
                /*GROUP BY*/    null,
                /*ORDER BY*/    null,
                /*FROM*/        null,
                /*TO*/          null,
                /*JDOQLTyped*/  query,
                /*paramValues*/ null);

        executeAPIQuery(ASSERTION_FAILED, holder, expected);
        executeSingleStringQuery(ASSERTION_FAILED, holder, expected);
        executeJDOQLTypedQuery(ASSERTION_FAILED, holder, null, true, expected);
    }

    /** */
    public void testNavigationWithDepartmentAndEmployeeAndProject() {
        Object expected = Arrays.asList(
                new Object[] {dept1, emp1, proj1},
                new Object[] {dept1, emp2, proj1},
                new Object[] {dept1, emp3, proj1},
                new Object[] {dept1, emp2, proj2},
                new Object[] {dept1, emp3, proj2},
                new Object[] {dept2, emp4, proj3},
                new Object[] {dept2, emp5, proj3});

        JDOQLTypedQuery<Company> query = getPM().newJDOQLTypedQuery(Company.class);
        QCompany cand = QCompany.candidate();
        QDepartment d = QDepartment.variable("d");
        QEmployee e = QEmployee.variable("e");
        QProject p = QProject.variable("p");
        query.filter(cand.name.eq("Sun Microsystems, Inc.").and(
                cand.departments.contains(d)).and(d.employees.contains(e)).and(e.projects.contains(p)));
        query.result(false, d, e, p);

        QueryElementHolder holder = new QueryElementHolder(
                /*UNIQUE*/      null,
                /*RESULT*/      "d, e, p",
                /*INTO*/        null,
                /*FROM*/        Company.class,
                /*EXCLUDE*/     null,
                /*WHERE*/       "name == \"Sun Microsystems, Inc.\" && " +
                "departments.contains(d) && d.employees.contains(e) && e.projects.contains(p)",
                /*VARIABLES*/   "Department d; Employee e; Project p",
                /*PARAMETERS*/  null,
                /*IMPORTS*/     null,
                /*GROUP BY*/    null,
                /*ORDER BY*/    null,
                /*FROM*/        null,
                /*TO*/          null,
                /*JDOQLTyped*/  query,
                /*paramValues*/ null);

        executeAPIQuery(ASSERTION_FAILED, holder, expected);
        executeSingleStringQuery(ASSERTION_FAILED, holder, expected);
        executeJDOQLTypedQuery(ASSERTION_FAILED, holder, null, true, expected);
    }

    /**
     * @see org.apache.jdo.tck.JDO_Test#localSetUp()
     */
    @Override
    protected void localSetUp() {
        addTearDownClass(CompanyModelReader.getTearDownClasses());
        loadAndPersistCompanyModel(getPM());
    }
}
