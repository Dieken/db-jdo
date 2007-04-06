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

/*
 * JDOHelper.java
 *
 */
 
package javax.jdo;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.net.URL;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;

import javax.jdo.spi.I18NHelper;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.JDOImplHelper.StateInterrogationBooleanReturn;
import javax.jdo.spi.JDOImplHelper.StateInterrogationObjectReturn;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateInterrogation;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.rmi.PortableRemoteObject;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;


/**
 * This class can be used by a JDO-aware application to call the JDO behavior
 * of <code>PersistenceCapable</code> instances without declaring them to be
 * <code>PersistenceCapable</code>.
 * <P>It is also used to acquire a <code>PersistenceManagerFactory</code> via 
 * various methods.
 * <P>This helper class defines static methods that allow a JDO-aware
 * application to examine the runtime state of instances.  For example,
 * an application can discover whether the instance is persistent, 
 * transactional, dirty, new, deleted, or detached; and to get its associated
 * <code>PersistenceManager</code> if it has one.
 * 
 * @version 2.1
 */
public class JDOHelper extends Object implements Constants {

    /**
     * A mapping from jdoconfig.xsd element attributes to PMF properties.
     */
    static final Map ATTRIBUTE_PROPERTY_XREF
        = createAttributePropertyXref();

    /**
     * The standard XML schema type.
     */
    protected static final String XSD_TYPE
        = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * The JAXP schema language property.
     */
    protected static final String SCHEMA_LANGUAGE_PROP
        = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    /**
     * External schema location property.
     */
    protected static final String SCHEMA_LOCATION_PROP
        = "http://apache.org/xml/properties/schema/external-schemaLocation";

    /** The Internationalization message helper.
     */
    private final static I18NHelper msg = 
        I18NHelper.getInstance ("javax.jdo.Bundle"); //NOI18N

    /**
     * Creates a map from jdoconfig.xsd element attributes to PMF properties.
     * @return An unmodifiable Map of jdoconfig.xsd element attributes to PMF
     * properties.
     */
    static Map createAttributePropertyXref() {
        Map xref = new HashMap();

        xref.put(
            PMF_ATTRIBUTE_CLASS,
            PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_DRIVER_NAME,
            PROPERTY_CONNECTION_DRIVER_NAME);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_FACTORY_NAME,
            PROPERTY_CONNECTION_FACTORY_NAME);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_FACTORY2_NAME,
            PROPERTY_CONNECTION_FACTORY2_NAME);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_PASSWORD,
            PROPERTY_CONNECTION_PASSWORD);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_URL,
            PROPERTY_CONNECTION_URL);
        xref.put(
            PMF_ATTRIBUTE_CONNECTION_USER_NAME,
            PROPERTY_CONNECTION_USER_NAME);
        xref.put(
            PMF_ATTRIBUTE_IGNORE_CACHE,
            PROPERTY_IGNORE_CACHE);
        xref.put(
            PMF_ATTRIBUTE_MAPPING,
            PROPERTY_MAPPING);
        xref.put(
            PMF_ATTRIBUTE_MULTITHREADED,
            PROPERTY_MULTITHREADED);
        xref.put(
            PMF_ATTRIBUTE_NONTRANSACTIONAL_READ,
            PROPERTY_NONTRANSACTIONAL_READ);
        xref.put(
            PMF_ATTRIBUTE_NONTRANSACTIONAL_WRITE,
            PROPERTY_NONTRANSACTIONAL_WRITE);
        xref.put(
            PMF_ATTRIBUTE_OPTIMISTIC,
            PROPERTY_OPTIMISTIC);
        xref.put(
            PMF_ATTRIBUTE_PERSISTENCE_UNIT_NAME,
            PROPERTY_PERSISTENCE_UNIT_NAME);
        xref.put(
            PMF_ATTRIBUTE_RESTORE_VALUES,
            PROPERTY_RESTORE_VALUES);
        xref.put(
            PMF_ATTRIBUTE_RETAIN_VALUES,
            PROPERTY_RETAIN_VALUES);
        xref.put(
            PMF_ATTRIBUTE_DETACH_ALL_ON_COMMIT,
            PROPERTY_DETACH_ALL_ON_COMMIT);
        xref.put(
            PMF_ATTRIBUTE_ServerTimeZoneID,
            PROPERTY_SERVER_TIMEZONE_ID);

        return Collections.unmodifiableMap(xref);
    }

    /** The JDOImplHelper instance used for handling non-binary-compatible
     *  implementations.
     */
    private static JDOImplHelper implHelper = (JDOImplHelper)
        AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return JDOImplHelper.getInstance();
                }
            }
        );

    /** The singleton instance of JDOHelper.
     * @since 2.1
     */
    private static JDOHelper instance = new JDOHelper();

    /**
     * Return the singleton instance of JDOHelper. This instance is 
     * thread-safe.
     * @since 2.1
     * @return the thread-safe singleton JDOHelper
     */
    public static JDOHelper getInstance() {
        return instance;
    }

    /** Some applications might prefer to use instance
     * methods instead of static methods.
     * @since 2.1
     */
    public JDOHelper() {}

    /** The stateless instance used for handling non-binary-compatible
    *  implementations of getPersistenceManager.
    */
    static StateInterrogationObjectReturn getPersistenceManager =
        new StateInterrogationObjectReturn() {
            public Object get(Object pc, StateInterrogation si) {
                return si.getPersistenceManager(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of getObjectId.
    */
    static StateInterrogationObjectReturn getObjectId =
        new StateInterrogationObjectReturn() {
            public Object get(Object pc, StateInterrogation si) {
                return si.getObjectId(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of getTransactionalObjectId.
    */
    static StateInterrogationObjectReturn getTransactionalObjectId =
        new StateInterrogationObjectReturn() {
            public Object get(Object pc, StateInterrogation si) {
                return si.getTransactionalObjectId(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of getVersion.
    */
    static StateInterrogationObjectReturn getVersion =
        new StateInterrogationObjectReturn() {
            public Object get(Object pc, StateInterrogation si) {
                return si.getVersion(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isPersistent.
    */
    static StateInterrogationBooleanReturn isPersistent =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isPersistent(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isTransactional.
    */
    static StateInterrogationBooleanReturn isTransactional =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isTransactional(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isDirty.
    */
    static StateInterrogationBooleanReturn isDirty =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isDirty(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isNew.
    */
    static StateInterrogationBooleanReturn isNew =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isNew(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isDeleted.
    */
    static StateInterrogationBooleanReturn isDeleted =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isDeleted(pc);
            }
        };

   /** The stateless instance used for handling non-binary-compatible
    *  implementations of isDetached.
    */
    static StateInterrogationBooleanReturn isDetached =
        new StateInterrogationBooleanReturn() {
            public Boolean is(Object pc, StateInterrogation si) {
                return si.isDetached(pc);
            }
        };

    /** Return the associated <code>PersistenceManager</code> if there is one.
     * Transactional and persistent instances return the associated
     * <code>PersistenceManager</code>.  
     *
     * <P>Transient non-transactional instances and instances of classes 
     * that do not implement <code>PersistenceCapable</code> return 
     * <code>null</code>.
     * @see PersistenceCapable#jdoGetPersistenceManager()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return the <code>PersistenceManager</code> associated with the parameter
     * instance.
     */
     public static PersistenceManager getPersistenceManager(Object pc) {
        if (pc instanceof PersistenceCapable) {
            return ((PersistenceCapable)pc).jdoGetPersistenceManager();
        } else {
            return (PersistenceManager)
                implHelper.nonBinaryCompatibleGet(pc, getPersistenceManager);
        }
      }
    
    /** Explicitly mark the parameter instance and field dirty.
     * Normally, <code>PersistenceCapable</code> classes are able to detect 
     * changes made to their fields.  However, if a reference to an array is 
     * given to a method outside the class, and the array is modified, then the
     * persistent instance is not aware of the change.  This API allows the
     * application to notify the instance that a change was made to a field.
     *
     * <P>Transient instances and instances of classes 
     * that do not implement <code>PersistenceCapable</code> ignore this method.
     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
     * @param pc the <code>PersistenceCapable</code> instance.
     * @param fieldName the name of the field to be marked dirty.
     */
    public static void makeDirty(Object pc, String fieldName) {
        if (pc instanceof PersistenceCapable) {
            ((PersistenceCapable)pc).jdoMakeDirty(fieldName);
        } else {
             implHelper.nonBinaryCompatibleMakeDirty(pc, fieldName);
        }
    }
    
    /** Return a copy of the JDO identity associated with the parameter 
     * instance.
     *
     * <P>Persistent instances of <code>PersistenceCapable</code> classes have a
     * JDO identity managed by the <code>PersistenceManager</code>.  This method
     * returns a copy of the ObjectId that represents the JDO identity.  
     * 
     * <P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>null</code>.
     *
     * <P>The ObjectId may be serialized
     * and later restored, and used with a <code>PersistenceManager</code> from 
     * the same JDO implementation to locate a persistent instance with the same
     * data store identity.
     *
     * <P>If the JDO identity is managed by the application, then the ObjectId 
     * may be used with a <code>PersistenceManager</code> from any JDO 
     * implementation that supports the <code>PersistenceCapable</code> class.
     *
     * <P>If the JDO identity is not managed by the application or the data 
     * store, then the ObjectId returned is only valid within the current 
     * transaction.
     *<P>
     * @see PersistenceManager#getObjectId(Object pc)
     * @see PersistenceCapable#jdoGetObjectId()
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @param pc the PersistenceCapable instance.
     * @return a copy of the ObjectId of the parameter instance as of the 
     * beginning of the transaction.
     */
    public static Object getObjectId(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoGetObjectId();
        } else {
            return implHelper.nonBinaryCompatibleGet(pc, getObjectId);
        }
    }

    /** Get object ids for a collection of instances. For each instance
     * in the parameter, the getObjectId method is called. This method
     * returns one identity instance for each element 
     * in the parameter. The order of iteration of the returned
     * Collection exactly matches the order of iteration of the
     * parameter Collection.
     * @param pcs the persistence-capable instances
     * @return the object ids of the parameters
     * @see #getObjectId(Object pc)
     * @see #getObjectIds(Object[] pcs)
     * @since 2.0
     */
    public static Collection getObjectIds(Collection pcs) {
        ArrayList result = new ArrayList();
        for (Iterator it = pcs.iterator(); it.hasNext();) {
            result.add(getObjectId(it.next()));
        }
        return result;
    }

    /** Get object ids for an array of instances. For each instance
     * in the parameter, the getObjectId method is called. This method
     * returns one identity instance for each element 
     * in the parameter. The order of instances of the returned
     * array exactly matches the order of instances of the
     * parameter array.
     * @param pcs the persistence-capable instances
     * @return the object ids of the parameters
     * @see #getObjectId(Object pc)
     * @see #getObjectIds(Collection pcs)
     * @since 2.0
     */
    public static Object[] getObjectIds(Object[] pcs) {
        Object[] result = new Object[pcs.length];
        for (int i = 0; i < pcs.length; ++i) {
            result[i] = getObjectId(pcs[i]);
        }
        return result;
    }

    /** Return a copy of the JDO identity associated with the parameter 
     * instance.
     *
     * @see PersistenceCapable#jdoGetTransactionalObjectId()
     * @see PersistenceManager#getObjectById(Object oid, boolean validate)
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return a copy of the ObjectId of the parameter instance as modified in 
     * this transaction.
     */
    public static Object getTransactionalObjectId(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoGetTransactionalObjectId();
        } else {
            return implHelper.nonBinaryCompatibleGet(
                pc, getTransactionalObjectId);
        }
    }
    
    /**
     * Return the version of the instance.
     * @since 2.0
     * @param pc the instance
     * @return the version of the instance
     */
    public static Object getVersion (Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoGetVersion();
        } else {
            return implHelper.nonBinaryCompatibleGet(pc, getVersion);
        }
    }
    /** Tests whether the parameter instance is dirty.
     *
     * Instances that have been modified, deleted, or newly 
     * made persistent in the current transaction return <code>true</code>.
     *
     *<P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>false</code>.
     *<P>
     * @see javax.jdo.spi.StateManager#makeDirty(PersistenceCapable pc, 
     * String fieldName)
     * @see PersistenceCapable#jdoIsDirty()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance has been modified in 
     * the current transaction.
     */
    public static boolean isDirty(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsDirty();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isDirty);
        }
    }

    /** Tests whether the parameter instance is transactional.
     *
     * Instances whose state is associated with the current transaction 
     * return true. 
     *
     *<P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>false</code>.
     * @see PersistenceCapable#jdoIsTransactional()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance is transactional.
     */
    public static boolean isTransactional(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsTransactional();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isTransactional);
        }
    }

    /** Tests whether the parameter instance is persistent.
     *
     * Instances that represent persistent objects in the data store 
     * return <code>true</code>. 
     *
     *<P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>false</code>.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @see PersistenceCapable#jdoIsPersistent()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance is persistent.
     */
    public static boolean isPersistent(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsPersistent();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isPersistent);
        }
    }

    /** Tests whether the parameter instance has been newly made persistent.
     *
     * Instances that have been made persistent in the current transaction 
     * return <code>true</code>.
     *
     *<P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>false</code>.
     *<P>
     * @see PersistenceManager#makePersistent(Object pc)
     * @see PersistenceCapable#jdoIsNew()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance was made persistent
     * in the current transaction.
     */
    public static boolean isNew(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsNew();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isNew);
        }
    }

    /** Tests whether the parameter instance has been deleted.
     *
     * Instances that have been deleted in the current transaction return 
     * <code>true</code>.
     *
     *<P>Transient instances and instances of classes that do not implement 
     * <code>PersistenceCapable</code> return <code>false</code>.
     *<P>
     * @see PersistenceManager#deletePersistent(Object pc)
     * @see PersistenceCapable#jdoIsDeleted()
     * @param pc the <code>PersistenceCapable</code> instance.
     * @return <code>true</code> if the parameter instance was deleted
     * in the current transaction.
     */
    public static boolean isDeleted(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsDeleted();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isDeleted);
        }
    }
    
    /**
     * Tests whether the parameter instance has been detached.
     * 
     * Instances that have been detached return true.
     * 
     * <P>Transient instances return false.
     * <P>
     * @see PersistenceCapable#jdoIsDetached()
     * @return <code>true</code> if this instance is detached.
     * @since 2.0
     * @param pc the instance
     */
    public static boolean isDetached(Object pc) {
      if (pc instanceof PersistenceCapable) {
          return ((PersistenceCapable)pc).jdoIsDetached();
        } else {
            return implHelper.nonBinaryCompatibleIs(pc, isDetached);
        }
    }
    
    /** Get the anonymous <code>PersistenceManagerFactory</code> configured via the standard
     * configuration file resource "META-INF/jdoconfig.xml", using the current thread's context class loader
     * to locate the configuration file resource(s).
     * @return the anonymous <code>PersistenceManagerFactory</code>.
     * @since 2.1
     * @see #getPersistenceManagerFactory(String,ClassLoader)
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory() {
        ClassLoader cl = getContextClassLoader();
        return getPersistenceManagerFactory ((String)null, cl);
    }

    /** Get a <code>PersistenceManagerFactory</code> based on a <code>Properties</code>
     * instance, using the current thread's context class loader to locate the
     * <code>PersistenceManagerFactory</code> class.
     * @return the <code>PersistenceManagerFactory</code>.
     * @param props a <code>Properties</code> instance with properties of the
     * <code>PersistenceManagerFactory</code>.
     * @see #getPersistenceManagerFactory(java.util.Map,ClassLoader)
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
            (Map props) {
        ClassLoader cl = getContextClassLoader();
        return getPersistenceManagerFactory (props, cl);
    }

    /**
     * Get a <code>PersistenceManagerFactory</code> based on a 
     * <code>Map</code> instance and a class loader.
     * The following are standard key values:
     * <BR><code>"javax.jdo.PersistenceManagerFactoryClass"
     * <BR>"javax.jdo.option.Optimistic",
     * <BR>"javax.jdo.option.RetainValues",
     * <BR>"javax.jdo.option.RestoreValues",
     * <BR>"javax.jdo.option.IgnoreCache",
     * <BR>"javax.jdo.option.NontransactionalRead",
     * <BR>"javax.jdo.option.NontransactionalWrite",
     * <BR>"javax.jdo.option.Multithreaded",
     * <BR>"javax.jdo.option.ConnectionUserName",
     * <BR>"javax.jdo.option.ConnectionPassword",
     * <BR>"javax.jdo.option.ConnectionURL",
     * <BR>"javax.jdo.option.ConnectionFactoryName",
     * <BR>"javax.jdo.option.ConnectionFactory2Name",
     * <BR>"javax.jdo.option.Mapping",
     * <BR>"javax.jdo.mapping.Catalog",
     * <BR>"javax.jdo.mapping.Schema",
     * <BR>"javax.jdo.option.PersistenceUnitName".
     * </code>
     * and properties of the form
     * <BR><code>javax.jdo.option.InstanceLifecycleListener.{listenerClass}={pcClasses}</code>
     * where <code>{listenerClass}</code> is the fully qualified name of a
     * class that implements
     * {@link javax.jdo.listener.InstanceLifecycleListener}, and
     * <code>{pcClasses}</code> is an optional comma- or whitespace-delimited
     * list of persistence-capable classes to be observed; the absence of a
     * value for a property of this form means that instances of all
     * persistence-capable classes will be observed by an instance of the given
     * listener class.
     * <P>JDO implementations
     * are permitted to define key values of their own.  Any key values not
     * recognized by the implementation must be ignored.  Key values that are
     * recognized but not supported by an implementation must result in a
     * <code>JDOFatalUserException</code> thrown by the method.
     * <P>The returned <code>PersistenceManagerFactory</code> is not 
     * configurable (the <code>set<I>XXX</I></code> methods will throw an 
     * exception).
     * <P>JDO implementations might manage a map of instantiated
     * <code>PersistenceManagerFactory</code> instances based on specified 
     * property key values, and return a previously instantiated 
     * <code>PersistenceManagerFactory</code> instance.  In this case, the 
     * properties of the returned instance must exactly match the requested 
     * properties.
     * @return the <code>PersistenceManagerFactory</code>.
     * @param props a <code>Properties</code> instance with properties of the 
     * <code>PersistenceManagerFactory</code>.
     * @param cl the class loader to use to load the 
     * <code>PersistenceManagerFactory</code> class
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
            (Map props, ClassLoader cl) {
        String pmfClassName = (String) props.get (
            PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS); //NOI18N
        if (pmfClassName == null) {
            throw new JDOFatalUserException(msg.msg(
                "EXC_GetPMFNoClassNameProperty")); // NOI18N
        }
        try {
            Class pmfClass = cl.loadClass (pmfClassName);
            Method pmfMethod = pmfClass.getMethod(
                "getPersistenceManagerFactory", //NOI18N
                    new Class[] {Map.class});
            return (PersistenceManagerFactory) pmfMethod.invoke (
                null, new Object[] {props});
        } catch (ClassNotFoundException cnfe) {
            throw new JDOFatalUserException (msg.msg(
                "EXC_GetPMFClassNotFound", pmfClassName), cnfe); //NOI18N
        } catch (IllegalAccessException iae) {
            throw new JDOFatalUserException (msg.msg(
                "EXC_GetPMFIllegalAccess", pmfClassName), iae); //NOI18N
        } catch (NoSuchMethodException nsme) {
            throw new JDOFatalInternalException (msg.msg(
                "EXC_GetPMFNoSuchMethod"), nsme); //NOI18N
        } catch (InvocationTargetException ite) {
            Throwable nested = ite.getTargetException();
            if  (nested instanceof JDOException) {
                throw (JDOException)nested;
            } else throw new JDOFatalInternalException (msg.msg(
                "EXC_GetPMFUnexpectedException"), ite); //NOI18N
        } catch (NullPointerException e) {
            throw new JDOFatalInternalException (msg.msg(
                "EXC_GetPMFNullPointerException", pmfClassName), e); //NOI18N
        } catch (ClassCastException e) {
            throw new JDOFatalInternalException (msg.msg(
                "EXC_GetPMFClassCastException", pmfClassName), e); //NOI18N
        } catch (Exception e) {
            throw new JDOFatalInternalException (msg.msg(
                "EXC_GetPMFUnexpectedException"), e); //NOI18N
        }
    }
    
    /**
     * Returns a named {@link PersistenceManagerFactory} with the given persistence unit name or,
     * if not found, a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the resource at
     * <code>name</code>. This method is equivalent to
     * invoking {@link
     * #getPersistenceManagerFactory(String,ClassLoader)} with
     * <code>Thread.currentThread().getContextClassLoader()</code> as
     * the <code>loader</code> argument.
     * If multiple persistence units with the name given are found, a {@link JDOFatalUserException} is thrown.
     * @since 2.0
     * @param name the persistence unit name or resource containing the Properties
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (String name) {
        return getPersistenceManagerFactory (name,
            getContextClassLoader());
    }

    /**
     * Returns a named {@link PersistenceManagerFactory} with the given persistence unit name or,
     * if not found, a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the resource at
     * <code>name</code>.  Loads the resource via
     * <code>loader</code>, and creates a {@link
     * PersistenceManagerFactory} with <code>loader</code>. Any
     * <code>IOException</code>s thrown during resource loading will
     * be wrapped in a {@link JDOFatalUserException}.
     * If multiple persistence units with the name given are found, a {@link JDOFatalUserException} is thrown.
     * @since 2.0
     * @param name the persistence unit name or resource containing the Properties
     * @param loader the class loader to use to load both the name and
     * the <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (String name, ClassLoader loader) {
        return getPersistenceManagerFactory(name, loader, loader);
    }
        
    /**
     * Returns a named {@link PersistenceManagerFactory} with the given
     * persistence unit name or,
     * if not found, a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the resource at
     * <code>name</code>.  Loads the Properties via
     * <code>resourceLoader</code>, and creates a {@link
     * PersistenceManagerFactory} with <code>pmfLoader</code>. Any
     * exceptions thrown during resource loading will
     * be wrapped in a {@link JDOFatalUserException}.
     * If multiple persistence units with the requested name are found, a
     * {@link JDOFatalUserException} is thrown.
     * @since 2.0
     * @param name the persistence unit name or resource containing the Properties
     * @param resourceLoader the class loader to use to load the name
     * @param pmfLoader the class loader to use to load the 
     * <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (String name, ClassLoader resourceLoader, ClassLoader pmfLoader) {

        if (resourceLoader == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullPropsLoader")); //NOI18N
        if (pmfLoader == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullPMFLoader")); //NOI18N

        InputStream in = null;
        if (name != null) { // then try to load resources from properties file
            Properties props = new Properties ();
            try {
                in = resourceLoader.getResourceAsStream (name);
                if (in != null) {
                    // then some kind of resource was found by the given name;
                    // assume that it's a properties file and proceed as usual
                    props.load (in);
                    return getPersistenceManagerFactory (props, pmfLoader);
                }
            }
            catch (IOException ioe) {
                throw new JDOFatalUserException (msg.msg (
                    "EXC_GetPMFIOExceptionRsrc", name), ioe); //NOI18N
            }
            finally {
                if (in != null)
                    try {
                        in.close ();
                    } catch (IOException ioe) { }
            }
        }
        // JDO 2.1:  else name was null or no resource found by given name;
        // assume name represents name of PU

        PersistenceManagerFactory pmf = getPersistenceUnit(
            name == null ? "" : name.trim(),
            resourceLoader,
            pmfLoader
        );
        if (pmf != null) {
            return pmf;
        }

        // else no PU found
        throw new JDOFatalUserException (msg.msg (
            "EXC_NoPMFConfigurableViaPropertiesOrXML",
            name,
            resourceLoader)); //NOI18N
    }

    /** Find and return<BR><ul>
     * <li>a {@link PersistenceManagerFactory} with the given name,</li>
     * <li>a JPA <code>EntityManagerFactory</code> cast to a
     * {@link PersistenceManagerFactory}, or <li>
     * <li>null if not found.</li>
     * If name is null
     * or blank, this method attempts to return the anonymous
     * {@link PersistenceManagerFactory}.  If multiple persistence units with
     * the given name are found (including the anonymous persistence unit),
     * this method will throw {@link JDOFatalUserException}.
     *
     * @param name The persistence unit name, or null, the empty string or a
     * string only containing whitespace characters for the anonymous
     * persistence unit.
     * @param resourceLoader The ClassLoader used to load the standard JDO
     * configuration file(s) given in the constant
     * {@link Constants#JDOCONFIG_RESOURCE_NAME}.
     * @param pmfLoader The loader used to load the
     * {@link PersistenceManagerFactory} implementation class.
     * @return A {@link PersistenceManagerFactory} corresponding to the
     * persistence unit name if found, or null if not found.
     * @throws JDOFatalUserException if multiple persistence units are found
     * with the given name, or any other is encountered.
     * @since 2.1
     */
    public static PersistenceManagerFactory getPersistenceUnit(
            String name,
            ClassLoader resourceLoader,
            ClassLoader pmfLoader
    ) {
        Map properties = getPersistenceUnitProperties(
                name, resourceLoader, JDOCONFIG_RESOURCE_NAME);

        if (properties != null) { // found requested JDO persistence unit props
            return getPersistenceManagerFactory(properties, pmfLoader);
        }

        // else try to return PMF from JPA EMF
        if ("".equals(name)) { // no such thing as an anonymous JPA EMF
            return null;
        }

        // else try to return PMF from named JPA EMF
        return getPMFFromEMF(name, pmfLoader);
    }

    protected static Map getPersistenceUnitProperties(
            String name
    ) {
        return getPersistenceUnitProperties(
            name, getContextClassLoader(), JDOCONFIG_RESOURCE_NAME);
    }

    protected static Map getPersistenceUnitProperties(
            String name,
            ClassLoader resourceLoader
    ) {
        return getPersistenceUnitProperties(
            name, resourceLoader, JDOCONFIG_RESOURCE_NAME);
    }

    /**
     * Find and return the named {@link PersistenceManagerFactory}, or null if
     * not found.  If name is null, return the anonymous
     * {@link PersistenceManagerFactory}.  If multiple persistence units with
     * the given name are found (including anonymous), throw
     * {@link JDOFatalUserException}.
     * This method is here only to facilitate testing; the parameter
     * "jdoconfigResourceName" in public usage should always have the value
     * given in the constant <code>JDOCONFIG_RESOURCE_NAME</code>.
     *
     * @param name The persistence unit name, or null or blank for the
     * anonymous persistence unit.
     * @param resourceLoader The ClassLoader used to load the standard JDO
     * configuration file.
     * @param jdoconfigResourceName The name of the configuration file to read.
     * In public usage, this should always be the value of
     * {@link Constants#JDOCONFIG_RESOURCE_NAME}.
     * @return The named PersistenceManagerFactory properties if found, null if
     * not.
     * @since 2.1
     * @throws JDOFatalUserException if multiple persistence units are found
     * with the given name, or any other exception is encountered.
     */
    protected static Map getPersistenceUnitProperties(
            String name,
            ClassLoader resourceLoader,
            String jdoconfigResourceName
    ) {
        /* JDO 2.1:
        Attempt to find & return named persistence unit here.
        If name == null or name == "", then we're looking for the default PMF.

        If we can't find it, this method returns null.
        */
        name = name == null ? "" : name.trim(); // for use as key in Maps
        
        // key is PU name, value is Map of PU properties
        Map/*<String,Map>*/ propertiesByNameInAllConfigs
                = new HashMap/*<String,Map>*/();
        try {
            URL firstFoundConfigURL = null;

            // get all JDO configurations
            Enumeration resources =
                resourceLoader.getResources(jdoconfigResourceName);

            if (resources.hasMoreElements()) {
                ArrayList processedResources = new ArrayList();

                // get ready to parse XML
                DocumentBuilderFactory factory = null;
                // TODO:  ensure DBF is initialized properly
                factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringComments(true);
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                factory.setIgnoringElementContentWhitespace(true);
                factory.setExpandEntityReferences(true);

                do {
                    URL currentConfigURL = (URL) resources.nextElement();
                    if (processedResources.contains(currentConfigURL)) {
                        continue;
                    }
                    else {
                        processedResources.add(currentConfigURL);
                    }
                    
                    Map/*<String,Map>*/ propertiesByNameInCurrentConfig =
                        readPersistenceUnitProperties(
                            currentConfigURL, name, factory);

                    // try to detect duplicate requested PU
                    if (propertiesByNameInCurrentConfig.containsKey(name)) {
                        // possible dup -- check for it
                        if (firstFoundConfigURL == null) {
                            firstFoundConfigURL = currentConfigURL;
                        }
                        
                        if (propertiesByNameInAllConfigs.containsKey(name))
                            throw new JDOFatalUserException (msg.msg(
                                "EXC_DuplicateRequestedPersistenceUnitFoundInDifferentConfigs",
                                "".equals(name)
                                        ? "(anonymous)"
                                        : name,
                                firstFoundConfigURL.toExternalForm(),
                                currentConfigURL.toExternalForm())); //NOI18N
                    }
                    // no dups -- add found PUs to all PUs and keep going
                    propertiesByNameInAllConfigs
                        .putAll(propertiesByNameInCurrentConfig);
                } while (resources.hasMoreElements());
            }
        }
        catch (FactoryConfigurationError e) {
            throw new JDOFatalUserException(
                msg.msg("ERR_NoDocumentBuilderFactory"), e);
        }
        catch (IOException ioe) {
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFIOExceptionRsrc", name), ioe); //NOI18N
        }

        // done with reading all config resources;
        // return what we found, which may very well be null
        return (Map) propertiesByNameInAllConfigs.get(name);
    }


    /**
     * Convenience method for retrieving the JPA persistence unit by the given
     * name.  This method is equivalent to
     * calling {@link #getPMFFromEMF(String,ClassLoader)} with the context ClassLoader.
     * @param name The persistence unit name.
     * @see #getPMFFromEMF(String,ClassLoader)
     * @return The named EMF cast as a PMF.
     */
    protected static PersistenceManagerFactory getPMFFromEMF(String name) {
        return getPMFFromEMF(name, getContextClassLoader());
    }

    /**
     * Attempts to locate a <code>javax.persistence.EntityManagerFactory</code>
     * via the <code>javax.persistence.Persistence</code> method
     * <code>createEntityManagerFactory(String)</code>
     * and casts it to a {@link PersistenceManagerFactory}.  It is a user error
     * if his chosen JPA vendor's <code>EntityManagerFactory</code>
     * implementation class does not also implement
     * {@link PersistenceManagerFactory}.
     * @param name The persistence unit name.
     * @param loader The classloader used to attempt loading of the class
     * <code>javax.persistence.Persistence</code>
     * and <code>javax.persistence.PersistenceException</code>.
     * @return The <code>EntityManagerFactory</code>, cast as a
     * <code>PersistenceManagerFactory</code> for the given
     * persistence unit name, or <code>null</code> if any of the following
     * conditions are true.
     *  <ul>
     *    <li>The attempt to load <code>javax.persistence.Persistence</code> or
     *          <code>javax.persistence.Persistence</code> fails for any
     *        reason.</li>
     *    <li>The named JPA persistence unit is not found.</li>
     *  </ul>
     * @throws JDOFatalUserException This method will throw
     * a {@link JDOFatalUserException}
     * with the exception that caused the error
     * if any of the following conditions are true.
     * <ul>
     *  <li>The <code>javax.persistence.Persistence</code> method
     *      <code>createEntityManagerFactory(String)</code>
     *      cannot be invoked.</li>
     *  <li>The <code>javax.persistence.EntityManagerFactory</code> cannot be
     *      cast to a
     *      <code>{@link PersistenceManagerFactory}</code>.</li>
     * </ul>
     */
    protected static PersistenceManagerFactory getPMFFromEMF(
        String name, ClassLoader loader
    ) {
        /*
            This implementation uses reflection to try to get an EMF so that
            javax.jdo, a Java SE API, does not introduce an unnecessary
            dependency on a Java EE API, namely the
            javax.persistence.Persistence class, while still
            being compatible with JPA.
         */

        // First, get required classes & methods
        Class persistenceClass = null;
        Class persistenceExceptionClass = null;
        Method createEntityManagerFactoryMethod = null;
        try {
            persistenceClass = Class.forName(
                "javax.persistence.Persistence",
                true,
                loader);

            createEntityManagerFactoryMethod = persistenceClass.getMethod(
                    "createEntityManagerFactory",
                    new Class[] { String.class });

            persistenceExceptionClass =
                Class.forName(
                    "javax.persistence.PersistenceException",
                    true,
                    loader);
        }
        catch (Exception x) {
            // may happen -- if it does, javax.persistence.Persistence or
            // requisites not available
            return null;
        }

        // Now, try to invoke createEntityManagerFactory method
        Object entityManagerFactory = null;
        Throwable t = null;
        try {
            entityManagerFactory =
                createEntityManagerFactoryMethod.invoke(
                    persistenceClass, new Object[] { name });
        }
        catch (RuntimeException x) {
            if (persistenceExceptionClass.isAssignableFrom(x.getClass())) {
                // named persistence unit not found
                return null;
            }
            // else something else went wrong
            t = x;
        }
        catch (Exception x) {
            t = x;
        }
        if (t != null) { // something went wrong -- throw
            throw new JDOFatalUserException(
                msg.msg("EXC_UnableToInvokeCreateEMFMethod"), t);
        }

        // Last, try to cast to PMF & return
        try {
            return (PersistenceManagerFactory) entityManagerFactory;
        }
        catch (ClassCastException x) { // EMF impl doesn't also implement PMF
            throw new JDOFatalUserException(
                msg.msg(
                    "EXC_UnableToCastEMFToPMF",
                    entityManagerFactory.getClass().getName()),
                x);
        }
    }

    /** Reads JDO configuration file, creates a Map for each
     * persistence-manager-factory, then returns the map.
     * @param url URL of a JDO configuration file compliant with
     * javax/jdo/jdoconfig.xsd.
     * @param requestedPersistenceUnitName The name of the requested
     * persistence unit (allows for fail-fast).
     * @param factory The <code>DocumentBuilderFactory</code> to use for XML
     * parsing.
     * @return a Map<String,Map> holding persistence unit configurations; for
     * the anonymous persistence unit, the
     * value of the String key is the empty string, "".
     */
    protected static Map/*<String,Map>*/ readPersistenceUnitProperties(
        URL url,
        String requestedPersistenceUnitName,
        DocumentBuilderFactory factory
    ) {
        requestedPersistenceUnitName = requestedPersistenceUnitName == null
            ? ""
            : requestedPersistenceUnitName.trim();

        Map propertiesByName = new HashMap();
        InputStream in = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            in = url.openStream();
            Document doc = builder.parse(in);

            Element root = doc.getDocumentElement();
            if (root == null) {
                throw new JDOFatalUserException(
                    msg.msg("EXC_InvalidJDOConfigNoRoot", url.toExternalForm())
                );
            }

            // TODO:  prefer using namespace-aware APIs
            NodeList pmfs = root.getElementsByTagName(
                ELEMENT_PERSISTENCE_MANAGER_FACTORY);

            for(int i = 0; i < pmfs.getLength(); i++) {
                Node pmfElement = pmfs.item(i);

                Properties pmfPropertiesFromAttributes
                    = readPropertiesFromPMFElementAttributes(pmfElement);

                Properties pmfPropertiesFromElements
                    = readPropertiesFromPMFSubelements(pmfElement, url);

                // for informative error handling, get PU name (or names) now
                String puNameFromAtts =
                    pmfPropertiesFromAttributes.getProperty(
                        PROPERTY_PERSISTENCE_UNIT_NAME);
                String puNameFromElem =
                    pmfPropertiesFromElements.getProperty(
                        PROPERTY_PERSISTENCE_UNIT_NAME);

                String puName = null;
                if (nullOrBlank(puNameFromAtts)) {
                    // no PU name attribute given
                    if (!nullOrBlank(puNameFromElem)) {
                        // PU name element was given
                        puName = puNameFromElem;
                    }
                    else  {
                        // PU name not given at all, means the "anonymous" PU
                        puName = "";
                    }
                }
                else {
                    // PU name given in an attribute
                    if (!nullOrBlank(puNameFromElem)) {
                        // exception -- PU name given as both att & elem
                        throw new JDOFatalUserException(
                            msg.msg(
                                "EXC_DuplicatePersistenceUnitNamePropertyFoundWithinUnitConfig",
                                puNameFromAtts,
                                puNameFromElem,
                                url.toExternalForm()));
                    }
                    puName = puNameFromAtts;
                }
                puName = puName == null ? "" : puName.trim();

                // check for duplicate properties among atts & elems
                if (requestedPersistenceUnitName.equals(puName)) {
                    Iterator it =
                        pmfPropertiesFromAttributes.keySet().iterator();
                    while (it.hasNext()) {
                        String property = (String) it.next();
                        if (pmfPropertiesFromElements.contains(property)) {
                            throw new JDOFatalUserException(
                                msg.msg(
                                    "EXC_DuplicatePropertyFound",
                                    property,
                                    puName,
                                    url.toExternalForm()));
                        }
                    }
                }
                
                // at this point, we're guaranteed not to have duplicate
                // properties -- merge them
                Properties pmfProps = new Properties();
                pmfProps.putAll(pmfPropertiesFromAttributes);
                pmfProps.putAll(pmfPropertiesFromElements);

                // check for duplicate requested PU name
                if (puName.equals(requestedPersistenceUnitName)
                    && propertiesByName.containsKey(puName)) {

                    throw new JDOFatalUserException(msg.msg(
                        "EXC_DuplicateRequestedPersistenceUnitFoundInSameConfig",
                        puName,
                        url.toExternalForm()));
                }
                propertiesByName.put(puName, pmfProps);
            }
            return propertiesByName;
        }
        catch (IOException ioe) {
            throw new JDOFatalUserException(
                msg.msg("EXC_GetPMFIOExceptionRsrc", url.toString()),
                ioe); //NOI18N
        }
        catch (ParserConfigurationException e) {
            throw new JDOFatalInternalException(
                msg.msg("EXC_ParserConfigException"),
                e);
        }
        catch (SAXParseException e) {
            throw new JDOFatalUserException(
                msg.msg(
                    "EXC_SAXParseException",
                    url.toExternalForm(),
                    new Integer(e.getLineNumber()),
                    new Integer(e.getColumnNumber())),
                e);
        }
        catch (SAXException e) {
            throw new JDOFatalUserException(
                msg.msg("EXC_SAXException", url.toExternalForm()),
                e);
        }
        catch (JDOException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new JDOFatalUserException(
                msg.msg("EXC_SAXException", url.toExternalForm()),
                e);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) { /* gulp */ }
            }
        }
    }

    protected static Properties readPropertiesFromPMFElementAttributes(
        Node pmfElement
    ) {
        Properties p = new Properties();
        NamedNodeMap attributes = pmfElement.getAttributes();
        if (attributes == null) {
            return p;
        }

        for(int i = 0; i < attributes.getLength(); i++) {
            Node att = attributes.item(i);
            String attName = att.getNodeName();
            String attValue = att.getNodeValue().trim();

            String jdoPropertyName =
                (String) ATTRIBUTE_PROPERTY_XREF.get(attName);

            p.put(
                jdoPropertyName != null
                    ? jdoPropertyName
                    : attName,
                attValue);
        }

        return p;
    }

    protected static Properties readPropertiesFromPMFSubelements(
        Node pmfElement, URL url)
    {
        Properties p = new Properties();
        NodeList elements = pmfElement.getChildNodes();
        if (elements == null) {
            return p;
        }
        for(int i = 0; i < elements.getLength(); i++) {
            Node element = elements.item(i);
            if (element.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            
            String elementName = element.getNodeName();
            NamedNodeMap attributes = element.getAttributes();
            if (ELEMENT_PROPERTY.equalsIgnoreCase(elementName)) {
                // <property name="..." value="..."/>

                // get the "name" attribute's value (required)
                Node nameAtt = attributes.getNamedItem(PROPERTY_ATTRIBUTE_NAME);
                if (nameAtt == null) {
                    throw new JDOFatalUserException(
                        msg.msg("EXC_PropertyElementHasNoNameAttribute", url));
                }
                String name = nameAtt.getNodeValue().trim();
                if ("".equals(name)) {
                    throw new JDOFatalUserException(
                        msg.msg(
                            "EXC_PropertyElementNameAttributeHasNoValue",
                            name,
                            url));
                }
                // The next call allows users to use either the
                // <persistence-manager-factory> attribute names or the
                // "javax.jdo" property names in <property> element "name"
                // attributes.  Handy-dandy.
                String jdoPropertyName =
                    (String) ATTRIBUTE_PROPERTY_XREF.get(name);
                
                String propertyName = jdoPropertyName != null
                        ? jdoPropertyName
                        : name;

                if (p.containsKey(propertyName)) {
                    throw new JDOFatalUserException(
                        msg.msg(
                            "EXC_DuplicatePropertyNameGivenInPropertyElement",
                            propertyName,
                            url));
                }

                // get the "value" attribute's value (optional)
                Node valueAtt = attributes.getNamedItem(
                    PROPERTY_ATTRIBUTE_VALUE);
                String value = valueAtt == null
                    ? null
                    : valueAtt.getNodeValue().trim();

                p.put(propertyName, value);
            }
            else if (ELEMENT_INSTANCE_LIFECYCLE_LISTENER.equals(elementName)) {
                // <instance-lifecycle-listener listener="..." classes="..."/>

                // get the "listener" attribute's value
                Node listenerAtt = attributes.getNamedItem(
                    INSTANCE_LIFECYCLE_LISTENER_ATTRIBUTE_LISTENER);
                if (listenerAtt == null) {
                    throw new JDOFatalUserException(
                        msg.msg(
                            "EXC_MissingListenerAttribute",
                            url));
                }
                String listener = listenerAtt.getNodeValue().trim();
                if ("".equals(listener)) {
                    throw new JDOFatalUserException(
                        msg.msg(
                            "EXC_MissingListenerAttributeValue",
                            url));
                }

                // listener properties are of the form
                // "javax.jdo.option.InstanceLifecycleListener." + listener
                listener =
                    PROPERTY_PREFIX_INSTANCE_LIFECYCLE_LISTENER + listener;

                // get the "classes" attribute's value (optional)
                Node classesAtt = attributes.getNamedItem(
                    INSTANCE_LIFECYCLE_LISTENER_ATTRIBUTE_CLASSES);
                String value = classesAtt == null
                    ? null
                    : classesAtt.getNodeValue().trim();

                p.put(listener,  value);
            }
        }
        return p;
    }

    protected static boolean nullOrBlank(String s) {
        return s == null || "".equals(s.trim());
    }
    
    /**
     * Returns a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the file at
     * <code>propsFile</code>. This method is equivalent to
     * invoking {@link
     * #getPersistenceManagerFactory(File,ClassLoader)} with
     * <code>Thread.currentThread().getContextClassLoader()</code> as
     * the <code>loader</code> argument.
     * @since 2.0
     * @param propsFile the file containing the Properties
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (File propsFile) {
        return getPersistenceManagerFactory (propsFile,
            getContextClassLoader());
    }

    /**
     * Returns a {@link PersistenceManagerFactory} configured based
     * on the properties stored in the file at
     * <code>propsFile</code>. Creates a {@link
     * PersistenceManagerFactory} with <code>loader</code>. Any
     * <code>IOException</code>s or
     * <code>FileNotFoundException</code>s thrown during resource
     * loading will be wrapped in a {@link JDOFatalUserException}.
     * @since 2.0
     * @param propsFile the file containing the Properties
     * @param loader the class loader to use to load the 
     * <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (File propsFile, ClassLoader loader) {
        if (propsFile == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullFile")); //NOI18N
        if (loader == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullLoader")); //NOI18N
        Properties props = new Properties ();
        InputStream in = null;
        try {
            in = new FileInputStream (propsFile);
            props.load (in);
        } catch (FileNotFoundException fnfe) {
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNoFile", propsFile, loader), fnfe); //NOI18N
        } catch (IOException ioe) {
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFIOExceptionFile", propsFile), ioe); //NOI18N
        } finally {
            if (in != null)
                try { 
                    in.close (); 
                } catch (IOException ioe) { }
        }
        return getPersistenceManagerFactory (props, loader);
    }

    /**
     * Returns a {@link PersistenceManagerFactory} at the JNDI
     * location specified by <code>jndiLocation</code> in the context
     * <code>context</code>. If <code>context</code> is
     * <code>null</code>, <code>new InitialContext()</code> will be
     * used. This method is equivalent to invoking {@link
     * #getPersistenceManagerFactory(String,Context,ClassLoader)}
     * with <code>Thread.currentThread().getContextClassLoader()</code> as
     * the <code>loader</code> argument.
     * @since 2.0
     * @param jndiLocation the JNDI location containing the 
     * PersistenceManagerFactory
     * @param context the context in which to find the named
     * PersistenceManagerFactory
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (String jndiLocation, Context context) {
        return getPersistenceManagerFactory (jndiLocation, context,
            getContextClassLoader());
    }


    /**
     * Returns a {@link PersistenceManagerFactory} at the JNDI
     * location specified by <code>jndiLocation</code> in the context
     * <code>context</code>. If <code>context</code> is
     * <code>null</code>, <code>new InitialContext()</code> will be
     * used. Creates a {@link PersistenceManagerFactory} with
     * <code>loader</code>. Any <code>NamingException</code>s thrown
     * will be wrapped in a {@link JDOFatalUserException}.
     * @since 2.0
     * @param jndiLocation the JNDI location containing the 
     * PersistenceManagerFactory
     * @param context the context in which to find the named 
     * PersistenceManagerFactory
     * @param loader the class loader to use to load the 
     * <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (String jndiLocation, Context context, ClassLoader loader) {
        if (jndiLocation == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullJndiLoc")); //NOI18N
        if (loader == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullLoader")); //NOI18N
        try {
            if (context == null)
                context = new InitialContext ();

            Object o = context.lookup (jndiLocation);
            return (PersistenceManagerFactory) PortableRemoteObject.narrow
                (o, PersistenceManagerFactory.class);
        } catch (NamingException ne) {
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNamingException", jndiLocation, loader), ne); //NOI18N
        }
    }
    
    /**
     * Returns a {@link PersistenceManagerFactory} configured based
     * on the Properties stored in the input stream at
     * <code>stream</code>. This method is equivalent to
     * invoking {@link
     * #getPersistenceManagerFactory(InputStream,ClassLoader)} with
     * <code>Thread.currentThread().getContextClassLoader()</code> as
     * the <code>loader</code> argument.
     * @since 2.0
     * @param stream the stream containing the Properties
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (InputStream stream) {
        return getPersistenceManagerFactory (stream,
            getContextClassLoader());
    }

    /**
     * Returns a {@link PersistenceManagerFactory} configured based
     * on the Properties stored in the input stream at
     * <code>stream</code>. Creates a {@link
     * PersistenceManagerFactory} with <code>loader</code>. Any
     * <code>IOException</code>s thrown during resource
     * loading will be wrapped in a {@link JDOFatalUserException}.
     * @since 2.0
     * @param stream the stream containing the Properties
     * @param loader the class loader to use to load the 
     * <code>PersistenceManagerFactory</code> class
     * @return the PersistenceManagerFactory
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory
        (InputStream stream, ClassLoader loader) {
        if (stream == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullStream")); //NOI18N
        if (loader == null)
            throw new JDOFatalUserException (msg.msg (
                "EXC_GetPMFNullLoader")); //NOI18N
        Properties props = new Properties ();
        try {
            props.load (stream);
        } catch (IOException ioe) {
            throw new JDOFatalUserException
                (msg.msg ("EXC_GetPMFIOExceptionStream"), ioe); //NOI18N
        }
        return getPersistenceManagerFactory (props, loader);
    }

    /** Get the context class loader associated with the current thread. 
     * This is done in a doPrivileged block because it is a secure method.
     * @return the current thread's context class loader.
     * @since 2.0
     */
    private static ClassLoader getContextClassLoader() {
        return (ClassLoader)AccessController.doPrivileged(
            new PrivilegedAction () {
                public Object run () {
                    return Thread.currentThread().getContextClassLoader();
                }
            }
        );
    }
}
