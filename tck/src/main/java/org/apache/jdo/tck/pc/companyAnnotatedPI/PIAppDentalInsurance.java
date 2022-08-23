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

package org.apache.jdo.tck.pc.companyAnnotatedPI;

import java.math.BigDecimal;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import org.apache.jdo.tck.pc.company.IDentalInsurance;

/**
 * This interface represents the persistent state of DentalInsurance. Javadoc was deliberately
 * omitted because it would distract from the purpose of the interface.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public interface PIAppDentalInsurance extends IDentalInsurance, PIAppInsurance {

  @Column(name = "LIFETIME_ORTHO_BENEFIT")
  BigDecimal getLifetimeOrthoBenefit();

  void setLifetimeOrthoBenefit(BigDecimal lifetimeOrthoBenefit);
}
