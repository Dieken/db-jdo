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
 
package org.apache.jdo.tck.pc.companyAnnotatedFC;

import javax.jdo.annotations.*;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import org.apache.jdo.tck.pc.company.IAddress;

import org.apache.jdo.tck.pc.company.ICompany;
import org.apache.jdo.tck.pc.company.IDepartment;
import org.apache.jdo.tck.util.DeepEquality;
import org.apache.jdo.tck.util.EqualityHelper;
import org.apache.jdo.tck.util.JDOCustomDateEditor;

/**
 * This class represents information about a company.
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION,table="companies")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME,
        column="DISCRIMINATOR")
    public class FCAppCompany 
    implements ICompany, Serializable, Comparable<ICompany>, Comparator<ICompany>, DeepEquality {

    private static final long serialVersionUID = 1L;

    @Persistent(primaryKey="true")
    @Column(name="ID")
    private long        companyid;
    @Column(name="NAME", jdbcType="VARCHAR")
    private String      name;
    @Column(name="FOUNDEDDATE")
    private Date        founded;
    @Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
    @Embedded(nullIndicatorColumn="COUNTRY",
        members={
            @Persistent(name="addrid", columns=@Column(name="ADDRID")),
            @Persistent(name="street", columns=@Column(name="STREET")),
            @Persistent(name="city", columns=@Column(name="CITY")),
            @Persistent(name="state", columns=@Column(name="STATE")),
            @Persistent(name="zipcode", columns=@Column(name="ZIPCODE")),
            @Persistent(name="country", columns=@Column(name="COUNTRY"))
    })
    private FCAppAddress     address;
    @Persistent(persistenceModifier=PersistenceModifier.PERSISTENT,
            mappedBy="company")
    @Element(types=org.apache.jdo.tck.pc.companyAnnotatedFC.FCAppDepartment.class)
    private transient Set<IDepartment> departments = new HashSet<>();

    /** This is the JDO-required no-args constructor. The TCK relies on
     * this constructor for testing PersistenceManager.newInstance(PCClass).
     */
    public FCAppCompany() {}

    /**
     * 
     * Initialize the <code>FCAppCompany</code> instance.
     * 
     * @param companyid The company id.
     * @param name The company name.
     * @param founded The date the company was founded.
     */
    public FCAppCompany(long companyid, String name, Date founded) {
        this.companyid = companyid;
        this.name = name;
        this.founded = founded;
    }

    /** 
     * Initialize the <code>Company</code> instance.
     * @param companyid The company id.
     * @param name The company name.
     * @param founded The date the company was founded.
     * @param addr The company's address.
     */
    public FCAppCompany(long companyid, String name, Date founded, IAddress addr) {
        this(companyid, name, founded);
        this.address = (FCAppAddress)addr;
    }

    /**
     * Get the company id.
     * @return The company id.
     */
    public long getCompanyid() {
        return companyid;
    }
    
    /** 
     * Set the id associated with this object.
     * @param id the id.
     */
    public void setCompanyid(long id) {
        if (this.companyid != 0)
            throw new IllegalStateException("Id is already set.");
        this.companyid = id;
    }

    /**
     * Get the name of the company.
     * @return The name of the company.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the  name of the company.
     * @param name The value to use for the name of the company.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the date that the company was founded.
     * @return The date the company was founded.
     */
    public Date getFounded() {
        return founded;
    }

    /**
     * Set the date that the company was founded.
     * @param founded The date to set that the company was founded.
     */
    public void setFounded(Date founded) {
        this.founded = founded;
    }

    /**
     * Get the address of the company.
     * @return The primary address of the company.
     */
    public IAddress getAddress() {
        return address;
    }
    
    /**
     * Set the primary address for the company.
     * @param address The address to set for the company.
     */
    public void setAddress(IAddress address) {
        this.address = (FCAppAddress)address;
    }

    /**
     * Get the departments contained in the company.
     * 
     * @return An unmodifiable <code>Set</code> that contains all the
     * <code>FCAppDepartment</code>s of the company.
     */
    public Set<IDepartment> getDepartments() {
        return Collections.unmodifiableSet(departments);
    }

    /**
     * Add a <code>FCAppDepartment</code> instance to the company.
     * 
     * @param dept The <code>FCAppDepartment</code> instance to add.
     */
    public void addDepartment(FCAppDepartment dept) {
        departments.add(dept);
    }

    /**
     * Remove a <code>FCAppDepartment</code> instance from the company.
     * 
     * @param dept The <code>FCAppDepartment</code> instance to remove.
     */
    public void removeDepartment(FCAppDepartment dept) {
        departments.remove(dept);
    }

    /**
     * Initialize the set of <code>FCAppDepartment</code>s in the company to the 
     * parameter. 
     * 
     * @param departments The set of <code>FCAppDepartment</code>s for the
     * company.
     */
    public void setDepartments(Set<IDepartment> departments) {
        // workaround: create a new HashSet, because fostore does not
        // support LinkedHashSet
        this.departments = 
            (departments != null) ? new HashSet<>(departments) : null;
    }
    
    /**
     * Serialization support: initialize transient fields.
     * @param in stream
     * @throws IOException error during reading
     * @throws ClassNotFoundException class could not be found
     */
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        departments = new HashSet<>();
    }

    /**
     * Returns a String representation of a <code>Company</code> object.
     * @return a String representation of a <code>Company</code> object.
     */
    public String toString() {
        return "Company(" + getFieldRepr()+ ")";
    }
    
    /**
     * Returns a String representation of the non-relationship fields.
     * @return a String representation of the non-relationship fields.
     */
    protected String getFieldRepr() {
        StringBuilder rc = new StringBuilder();
        rc.append(companyid);
        rc.append(", name ").append(name);
        rc.append(", founded ").append(JDOCustomDateEditor.getDateRepr(founded));
        return rc.toString();
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
    public boolean deepCompareFields(Object other, 
                                     EqualityHelper helper) {
        FCAppCompany otherCompany = (FCAppCompany)other;
        String where = "Company<" + companyid + ">";
        return 
            helper.equals(companyid, otherCompany.getCompanyid(), where + ".companyid") &
            helper.equals(name, otherCompany.getName(), where + ".name") &
            helper.equals(founded, otherCompany.getFounded(), where + ".founded") &
            helper.deepEquals(address, otherCompany.getAddress(), where + ".address") &
            helper.deepEquals(departments, otherCompany.getDepartments(), where + ".departments");
    }
    
    /** 
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object. 
     * @param o The Object to be compared. 
     * @return a negative integer, zero, or a positive integer as this 
     * object is less than, equal to, or greater than the specified object. 
     * @throws ClassCastException - if the specified object's type prevents
     * it from being compared to this Object. 
     */
    public int compareTo(ICompany o) {
        return compareTo((FCAppCompany)o);
    }

    /** 
     * Compare two instances. This is a method in Comparator.
     */
    public int compare(ICompany o1, ICompany o2) {
        return compare((FCAppCompany)o1, (FCAppCompany)o2);
    }

    /** 
     * Compares this object with the specified Company object for
     * order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified
     * object.  
     * @param other The Company object to be compared. 
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified Company
     * object. 
     */
    public int compareTo(FCAppCompany other) {
        return compare(this, other);
    }

    /**
     * Compares its two ICompany arguments for order. Returns a negative
     * integer, zero, or a positive integer as the first argument is less
     * than, equal to, or greater than the second. 
     * @param o1 the first ICompany object to be compared. 
     * @param o2 the second ICompany object to be compared. 
     * @return a negative integer, zero, or a positive integer as the first
     * object is less than, equal to, or greater than the second object. 
     */
    public static int compare(FCAppCompany o1, FCAppCompany o2) {
        return EqualityHelper.compare(o1.getCompanyid(), o2.getCompanyid());
    }
    
    /** 
     * Indicates whether some other object is "equal to" this one.
     * @param obj the object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     * argument; <code>false</code> otherwise. 
     */
    public boolean equals(Object obj) {
        if (obj instanceof FCAppCompany) {
            return compareTo((FCAppCompany)obj) == 0;
        }
        return false;
    }
        
    /**
     * Returns a hash code value for the object. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return (int)companyid;
    }
    
    /**
     * The class to be used as the application identifier
     * for the <code>Company</code> class. It consists of both the company 
     * name and the date that the company was founded.
     */
    public static class Oid implements Serializable, Comparable<Oid> {

        private static final long serialVersionUID = 1L;

        /**
         * This field is part of the identifier and should match in name
         * and type with a field in the <code>Company</code> class.
         */
        public long companyid;

        /** The required public no-arg constructor. */
        public Oid() { }

        /**
         * Initialize the identifier.
         * @param companyid The id of the company.
         */
        public Oid(long companyid) {
            this.companyid = companyid;
        }
        
        public Oid(String s) { companyid = Long.parseLong(justTheId(s)); }

        public String toString() { return this.getClass().getName() + ": "  + companyid;}

        
        /** */
        public boolean equals(Object obj) {
            if (obj==null || !this.getClass().equals(obj.getClass())) 
                return false;
            Oid o = (Oid) obj;
            if (this.companyid != o.companyid) 
                return false;
            return true;
        }

        /** */
        public int hashCode() {
            return (int)companyid;
        }
        
        protected static String justTheId(String str) {
            return str.substring(str.indexOf(':') + 1);
        }

        /** */
        public int compareTo(Oid obj) {
            return Long.compare(companyid, obj.companyid);
        }
        
    }

}

