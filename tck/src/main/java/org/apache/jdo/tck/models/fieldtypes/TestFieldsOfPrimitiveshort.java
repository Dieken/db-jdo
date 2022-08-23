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

package org.apache.jdo.tck.models.fieldtypes;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.apache.jdo.tck.JDO_Test;
import org.apache.jdo.tck.pc.fieldtypes.FieldsOfPrimitiveshort;
import org.apache.jdo.tck.util.BatchTestRunner;

/**
 * <B>Title:</B> Support of field type short. <br>
 * <B>Keywords:</B> model <br>
 * <B>Assertion ID:</B> A6.4.3-3. <br>
 * <B>Assertion Description: </B> JDO implementations must support fields of the primitive type
 * <code>short</code>.
 */
public class TestFieldsOfPrimitiveshort extends JDO_Test {

  /** */
  private static final String ASSERTION_FAILED =
      "Assertion A6.4.3-3 (TestFieldsOfPrimitiveshort) failed: ";

  /**
   * The <code>main</code> is called when the class is directly executed from the command line.
   *
   * @param args The arguments passed to the program.
   */
  public static void main(String[] args) {
    BatchTestRunner.run(TestFieldsOfPrimitiveshort.class);
  }

  /**
   * @see org.apache.jdo.tck.JDO_Test#localSetUp()
   */
  @Override
  protected void localSetUp() {
    addTearDownClass(FieldsOfPrimitiveshort.class);
  }

  /** */
  public void test() {
    pm = getPM();

    runTest(pm);

    pm.close();
    pm = null;
  }

  /** */
  void runTest(PersistenceManager pm) {
    Transaction tx = pm.currentTransaction();
    int i, n;
    short value;
    tx.begin();
    FieldsOfPrimitiveshort pi = new FieldsOfPrimitiveshort();
    pi.identifier = 1;
    pm.makePersistent(pi);
    Object oid = pm.getObjectId(pi);
    n = pi.getLength();
    // Provide initial set of values
    for (i = 0, value = 10; i < n; ++i) {
      pi.set(i, value);
    }
    tx.commit();
    // cache will be flushed
    pi = null;
    System.gc();

    tx.begin();

    pi = (FieldsOfPrimitiveshort) pm.getObjectById(oid, true);
    checkValues(oid, (short) 10);

    // Provide new set of values
    for (i = 0, value = 32000; i < n; ++i) {
      pi.set(i, value);
    }
    tx.commit();
    // cache will be flushed
    pi = null;
    System.gc();

    tx.begin();
    // check new values
    checkValues(oid, (short) 32000);
    tx.commit();
  }

  /** */
  private void checkValues(Object oid, short startValue) {
    int i;
    FieldsOfPrimitiveshort pi = (FieldsOfPrimitiveshort) pm.getObjectById(oid, true);
    int n = pi.getLength();
    for (i = 0; i < n; ++i) {
      if (!FieldsOfPrimitiveshort.isPersistent[i]) continue;
      short val = pi.get(i);
      if (val != startValue) {
        fail(
            ASSERTION_FAILED,
            "Incorrect value for "
                + FieldsOfPrimitiveshort.fieldSpecs[i]
                + ", expected value "
                + startValue
                + ", value is "
                + val);
      }
    }
  }
}
