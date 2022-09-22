/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jdo.tck.pc.companyAnnotatedJPA;

import javax.jdo.PersistenceManager;
import org.apache.jdo.tck.pc.company.CompanyFactoryAbstractImpl;
import org.apache.jdo.tck.pc.company.CompanyFactoryNewInstance;
import org.apache.jdo.tck.pc.company.IAddress;
import org.apache.jdo.tck.pc.company.ICompany;
import org.apache.jdo.tck.pc.company.IDentalInsurance;
import org.apache.jdo.tck.pc.company.IDepartment;
import org.apache.jdo.tck.pc.company.IFullTimeEmployee;
import org.apache.jdo.tck.pc.company.IMedicalInsurance;
import org.apache.jdo.tck.pc.company.IMeetingRoom;
import org.apache.jdo.tck.pc.company.IPartTimeEmployee;
import org.apache.jdo.tck.pc.company.IProject;

/*
 * CompanyFactoryAnnotatedPMFieldClass.java
 *
 * This class uses the PersistenceManager.newInstance method with the concrete
 * class as a parameter.
 */
public class CompanyFactoryAnnotatedJPAAppPM extends CompanyFactoryAbstractImpl
    implements CompanyFactoryNewInstance {

  /**
   * Creates a new instance of CompanyFactoryAnnotatedJPAAppPM
   *
   * @param pm the PersistenceManager
   */
  public CompanyFactoryAnnotatedJPAAppPM(PersistenceManager pm) {
    super(pm);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Class<?>[] getTearDownClasses() {
    return new Class[] {
      JPAAppDentalInsurance.class,
      JPAAppMedicalInsurance.class,
      JPAAppPartTimeEmployee.class,
      JPAAppFullTimeEmployee.class,
      JPAAppProject.class,
      JPAAppDepartment.class,
      JPAAppCompany.class,
      JPAAppAddress.class
    };
  }

  public IAddress newAddress() {
    return pm.newInstance(JPAAppAddress.class);
  }

  public IMeetingRoom newMeetingRoom() {
    return null;
  }

  public ICompany newCompany() {
    return pm.newInstance(JPAAppCompany.class);
  }

  public IDentalInsurance newDentalInsurance() {
    return pm.newInstance(JPAAppDentalInsurance.class);
  }

  public IDepartment newDepartment() {
    return pm.newInstance(JPAAppDepartment.class);
  }

  public IFullTimeEmployee newFullTimeEmployee() {
    return pm.newInstance(JPAAppFullTimeEmployee.class);
  }

  public IMedicalInsurance newMedicalInsurance() {
    return pm.newInstance(JPAAppMedicalInsurance.class);
  }

  public IPartTimeEmployee newPartTimeEmployee() {
    return pm.newInstance(JPAAppPartTimeEmployee.class);
  }

  public IProject newProject() {
    return pm.newInstance(JPAAppProject.class);
  }
}
