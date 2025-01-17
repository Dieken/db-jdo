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

package org.apache.jdo.tck.pc.company;

import java.util.Set;

/**
 * This interface represents the persistent state of Department. Javadoc was deliberately omitted
 * because it would distract from the purpose of the interface.
 */
public interface PIDepartment extends IDepartment {

  long getDeptid();

  String getName();

  ICompany getCompany();

  IEmployee getEmployeeOfTheMonth();

  Set<IEmployee> getEmployees();

  Set<IEmployee> getFundedEmps();

  void setDeptid(long deptid);

  void setName(String name);

  void setCompany(ICompany company);

  void setEmployeeOfTheMonth(IEmployee employeeOfTheMonth);

  void setEmployees(Set<IEmployee> employees);

  void setFundedEmps(Set<IEmployee> employees);
}
