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

package org.apache.jdo.tck.pc.companyAnnotatedPC;

import javax.jdo.PersistenceManager;
import org.apache.jdo.tck.pc.company.*;

/*
 * CompanyFactoryAnnotatedFCDSPM.java
 *
 * This class uses the PersistenceManager.newInstance method with the concrete
 * class as a parameter.
 */
public class CompanyFactoryAnnotatedPCDSConcrete
        implements CompanyFactoryNewInstance {
    
    PersistenceManager pm = null;

    /**
     * Creates a new instance of CompanyFactoryAnnotatedPCDSConcrete
     * @param pm the PersistenceManager
     */
    public CompanyFactoryAnnotatedPCDSConcrete(PersistenceManager pm) {
        this.pm = pm;        
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<?>[] getTearDownClasses() {
        return new Class[] {
            PCDSDentalInsurance.class,
            PCDSMedicalInsurance.class,
            PCDSPartTimeEmployee.class,
            PCDSFullTimeEmployee.class,
            PCDSProject.class,
            PCDSDepartment.class,
            PCDSCompany.class,
            PCDSAddress.class
        };
    }
    
    public IAddress newAddress() {
        return new PCDSAddress();
    }

    public IMeetingRoom newMeetingRoom() {
        return null;
    }
    
    public ICompany newCompany() {
        return new PCDSCompany();
    }
    
    public IDentalInsurance newDentalInsurance() {
        return new PCDSDentalInsurance();
    }
    
    public IDepartment newDepartment() {
        return new PCDSDepartment();
    }
    
    public IFullTimeEmployee newFullTimeEmployee() {
        return new PCDSFullTimeEmployee();
    }
    
    public IMedicalInsurance newMedicalInsurance() {
        return new PCDSMedicalInsurance();
    }
    
    public IPartTimeEmployee newPartTimeEmployee() {
        return new PCDSPartTimeEmployee();
    }
    
    public IProject newProject() {
        return new PCDSProject();
    }
}
