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

import javax.jdo.annotations.*;
import org.apache.jdo.tck.pc.company.IMedicalInsurance;
import org.apache.jdo.tck.util.EqualityHelper;

/**
 * This class represents a dental insurance carrier selection for a particular <code>Employee</code>
 * .
 */
@PersistenceCapable
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "DATASTORE_IDENTITY")
public class PCDSMedicalInsurance extends PCDSInsurance implements IMedicalInsurance {

  private static final long serialVersionUID = 1L;

  @NotPersistent() private String _planType; // possible values: "PPO", "EPO", "NPO"

  /**
   * This is the JDO-required no-args constructor. The TCK relies on this constructor for testing
   * PersistenceManager.newInstance(PCClass).
   */
  public PCDSMedicalInsurance() {}

  /**
   * Construct a <code>PCDSMedicalInsurance</code> instance.
   *
   * @param insid The insurance instance identifier.
   * @param carrier The insurance carrier.
   * @param planType The planType.
   */
  public PCDSMedicalInsurance(long insid, String carrier, String planType) {
    super(insid, carrier);
    this._planType = planType;
  }

  /**
   * Construct a <code>PCDSMedicalInsurance</code> instance.
   *
   * @param insid The insurance instance identifier.
   * @param carrier The insurance carrier.
   * @param employee The employee associated with this insurance.
   * @param planType The planType.
   */
  public PCDSMedicalInsurance(long insid, String carrier, PCDSEmployee employee, String planType) {
    super(insid, carrier, employee);
    this._planType = planType;
  }

  /**
   * Get the insurance planType.
   *
   * @return The insurance planType.
   */
  @Column(name = "PLANTYPE")
  public String getPlanType() {
    return _planType;
  }

  /**
   * Set the insurance planType.
   *
   * @param planType The insurance planType.
   */
  public void setPlanType(String planType) {
    this._planType = planType;
  }

  /**
   * Returns a String representation of a <code>PCDSMedicalInsurance</code> object.
   *
   * @return a String representation of a <code>PCDSMedicalInsurance</code> object.
   */
  @Override
  public String toString() {
    return "FCMedicalInsurance(" + getFieldRepr() + ")";
  }

  /**
   * Returns a String representation of the non-relationship fields.
   *
   * @return a String representation of the non-relationship fields.
   */
  @Override
  protected String getFieldRepr() {
    StringBuilder rc = new StringBuilder();
    rc.append(super.getFieldRepr());
    rc.append(", planType ").append(_planType);
    return rc.toString();
  }

  /**
   * Returns <code>true</code> if all the fields of this instance are deep equal to the coresponding
   * fields of the other Object.
   *
   * @param other the object with which to compare.
   * @param helper EqualityHelper to keep track of instances that have already been processed.
   * @return <code>true</code> if all the fields are deep equal; <code>false</code> otherwise.
   * @throws ClassCastException if the specified instances' type prevents it from being compared to
   *     this instance.
   */
  @Override
  public boolean deepCompareFields(Object other, EqualityHelper helper) {
    PCDSMedicalInsurance otherIns = (PCDSMedicalInsurance) other;
    String where = "FCMedicalInsurance<" + getInsid() + ">";
    return super.deepCompareFields(otherIns, helper)
        & helper.equals(_planType, otherIns.getPlanType(), where + ".planType");
  }
}
