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

import java.math.BigDecimal;

import org.apache.jdo.tck.util.DeepEquality;
import org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a dental insurance carrier selection for a
 * particular <code>Employee</code>.
 */
public class DentalInsurance extends Insurance {

    private BigDecimal lifetimeOrthoBenefit;

    /** This is the JDO-required no-args constructor */
    protected DentalInsurance() {}

    /**
     * Initialize a <code>DentalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param lifetimeOrthoBenefit The lifetimeOrthoBenefit.
     */
    public DentalInsurance(long insid, String carrier, 
                           BigDecimal lifetimeOrthoBenefit) {
        super(insid, carrier);
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /**
     * Initialize a <code>DentalInsurance</code> instance.
     * @param insid The insurance instance identifier.
     * @param carrier The insurance carrier.
     * @param employee The employee associated with this insurance.
     * @param lifetimeOrthoBenefit The lifetimeOrthoBenefit.
     */
    public DentalInsurance(long insid, String carrier, Employee employee,
                           BigDecimal lifetimeOrthoBenefit) {
        super(insid, carrier, employee);
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /**
     * Get the insurance lifetimeOrthoBenefit.
     * @return The insurance lifetimeOrthoBenefit.
     */
    public BigDecimal getLifetimeOrthoBenefit() {
        return lifetimeOrthoBenefit;
    }

    /**
     * Set the insurance lifetimeOrthoBenefit.
     * @param lifetimeOrthoBenefit The insurance lifetimeOrthoBenefit.
     */
    public void setLifetimeOrthoBenefit(BigDecimal lifetimeOrthoBenefit) {
        this.lifetimeOrthoBenefit = lifetimeOrthoBenefit;
    }

    /** 
     * Returns <code>true</code> if all the fields of this instance are
     * deep equal to the coresponding fields of the specified Person.
     * @param other the object with which to compare.
     * @param helper EqualityHelper to keep track of instances that have
     * already been processed. 
     * @return <code>true</code> if all the fields are deep equal;
     * <code>false</code> otherwise.  
     * @throws ClassCastException if the specified instances' type prevents
     * it from being compared to this instance. 
     */
    public boolean deepCompareFields(DeepEquality other, 
                                     EqualityHelper helper) {
        DentalInsurance otherIns = (DentalInsurance)other;
        return super.deepCompareFields(otherIns, helper) &&
            helper.equals(lifetimeOrthoBenefit, 
                          otherIns.lifetimeOrthoBenefit);
    }
}
