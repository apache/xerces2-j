/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "Apache Software Foundation" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2001, Sun Microsystems,
 * Inc., http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

package org.apache.xerces.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * This class is duplicated for each JAXP subpackage so keep it in sync.
 * It is package private and therefore is not exposed as part of the JAXP
 * API.
 * <p>
 * This code is designed to implement the JAXP 1.1 spec pluggability
 * feature and is designed to both compile and run on JDK version 1.1 and
 * later.  The code also runs both as part of an unbundled jar file and
 * when bundled as part of the JDK.
 * <p>
 * This class was moved from the <code>javax.xml.parsers.FactoryFinder</code>
 * class and modified to be used as a general utility for creating objects 
 * dynamically.
 *
 * @version $Id$
 */
public class ObjectFactory {

    //
    // Constants
    //

    /** Set to true for debugging */
    private static final boolean DEBUG = false;

    //
    // Public static methods
    //

    /**
     * Finds the implementation Class object in the specified order.  The
     * specified order is the following:
     * <ol>
     *  <li>query the system property using <code>System.getProperty</code>
     *  <li>read <code>META-INF/services/<i>factoryId</i></code> file
     *  <li>use fallback classname
     * </ol>
     *
     * @return Class object of factory, never null
     *
     * @param factoryId             Name of the factory to find, same as
     *                              a property name
     * @param fallbackClassName     Implementation class name, if nothing else
     *                              is found.  Use null to mean no fallback.
     *
     * @exception ObjectFactory.ConfigurationError
     */
    public static Object createObject(String factoryId, String fallbackClassName)
        throws ConfigurationError {
        return createObject(factoryId, null, fallbackClassName);
    } // createObject(String,String):Object

    /**
     * Finds the implementation Class object in the specified order.  The
     * specified order is the following:
     * <ol>
     *  <li>query the system property using <code>System.getProperty</code>
     *  <li>read <code>$java.home/lib/<i>propertiesFilename</i></code> file
     *  <li>read <code>META-INF/services/<i>factoryId</i></code> file
     *  <li>use fallback classname
     * </ol>
     *
     * @return Class object of factory, never null
     *
     * @param factoryId             Name of the factory to find, same as
     *                              a property name
     * @param propertiesFilename The filename in the $java.home/lib directory
     *                           of the properties file.
     * @param fallbackClassName     Implementation class name, if nothing else
     *                              is found.  Use null to mean no fallback.
     *
     * @exception ObjectFactory.ConfigurationError
     */
    public static Object createObject(String factoryId, 
                                      String propertiesFilename,
                                      String fallbackClassName)
        throws ConfigurationError
    {
        debugPrintln("debug is on");

        ClassLoader classLoader = findClassLoader();

        // Use the system property first
        try {
            String systemProp =
                System.getProperty( factoryId );
            if( systemProp!=null) {
                debugPrintln("found system property " + systemProp);
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        }

        // try to read from $java.home/lib/xml.properties
        if (propertiesFilename != null) {
            try {
                String javah=System.getProperty( "java.home" );
                String configFile = javah + File.separator +
                    "lib" + File.separator + propertiesFilename;
                File f=new File( configFile );
                if( f.exists()) {
                    Properties props=new Properties();
                    props.load( new FileInputStream(f));
                    String factoryClassName = props.getProperty(factoryId);
                    debugPrintln("found java.home property " + factoryClassName);
                    return newInstance(factoryClassName, classLoader);
                }
            } catch(Exception ex ) {
                if( DEBUG ) ex.printStackTrace();
            }
        }

        // try to find services in CLASSPATH
        String serviceId = "META-INF/services/" + factoryId;
        try {
            InputStream is=null;
            if (classLoader == null) {
                is=ClassLoader.getSystemResourceAsStream( serviceId );
            } else {
                is=classLoader.getResourceAsStream( serviceId );
            }
        
            if( is!=null ) {
                debugPrintln("found " + serviceId);

                // Read the service provider name in UTF-8 as specified in
                // the jar spec.  Unfortunately this fails in Microsoft
                // VJ++, which does not implement the UTF-8
                // encoding. Theoretically, we should simply let it fail in
                // that case, since the JVM is obviously broken if it
                // doesn't support such a basic standard.  But since there
                // are still some users attempting to use VJ++ for
                // development, we have dropped in a fallback which makes a
                // second attempt using the platform's default encoding. In
                // VJ++ this is apparently ASCII, which is a subset of
                // UTF-8... and since the strings we'll be reading here are
                // also primarily limited to the 7-bit ASCII range (at
                // least, in English versions), this should work well
                // enough to keep us on the air until we're ready to
                // officially decommit from VJ++. [Edited comment from
                // jkesselm]
                BufferedReader rd;
                try {
                    rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                } catch (java.io.UnsupportedEncodingException e) {
                    rd = new BufferedReader(new InputStreamReader(is));
                }
        
                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                    ! "".equals(factoryClassName)) {
                    debugPrintln("loaded from services: " + factoryClassName);
                    return newInstance(factoryClassName, classLoader);
                }
            }
        } catch( Exception ex ) {
            if( DEBUG ) ex.printStackTrace();
        }

        if (fallbackClassName == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }

        debugPrintln("loaded from fallback value: " + fallbackClassName);
        return newInstance(fallbackClassName, classLoader);

    } // createObject(String,String,String):Object

    //
    // Private static methods
    //

    /** Prints a message to standard error if debugging is enabled. */
    private static void debugPrintln(String msg) {
        if (DEBUG) {
            System.err.println("JAXP: " + msg);
        }
    } // debugPrintln(String)

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use
     * the context ClassLoader.
     */           
    private static ClassLoader findClassLoader()
        throws ConfigurationError
    {
        Method m = null;

        try {
            m = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // Assume that we are running JDK 1.1, use the current ClassLoader
            debugPrintln("assuming JDK 1.1");
            return ObjectFactory.class.getClassLoader();
        }

        try {
            ClassLoader loader = (ClassLoader) m.invoke(Thread.currentThread(), null);
            return loader != null ? loader : ObjectFactory.class.getClassLoader();
        } catch (IllegalAccessException e) {
            // assert(false)
            throw new ConfigurationError("Unexpected IllegalAccessException",
                                         e);
        } catch (InvocationTargetException e) {
            // assert(e.getTargetException() instanceof SecurityException)
            throw new ConfigurationError("Unexpected InvocationTargetException",
                                         e);
        }
    } // findClassLoader():ClassLoader

    /**
     * Create an instance of a class using the specified ClassLoader
     */
    private static Object newInstance(String className,
                                      ClassLoader classLoader)
        throws ConfigurationError
    {
        try {
            if (classLoader != null) {
                try {
                      return classLoader.loadClass(className).newInstance ();
                } catch (ClassNotFoundException x) {
                      // try again
                }
            }
            return Class.forName(className).newInstance();
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    } // newInstance(String,ClassLoader):Object

    //
    // Classes
    //

    /**
     * A configuration error.
     */
    public static class ConfigurationError 
        extends Error {

        //
        // Data
        //

        /** Exception. */
        private Exception exception;

        //
        // Constructors
        //

        /**
         * Construct a new instance with the specified detail string and
         * exception.
         */
        public ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        } // <init>(String,Exception)

        //
        // Public methods
        //

        /** Returns the exception associated to this error. */
        public Exception getException() {
            return exception;
        } // getException():Exception

    } // class ConfigurationError

} // class ObjectFactory
