/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */


/**
 * This class holds the list of registered DOMImplementations. The contents 
 * of the registry are drawn from the System Property 
 * <code>org.w3c.dom.DOMImplementationSourceList</code>, which must contain a 
 * white-space delimited sequence of the names of classes implementing 
 * <code>DOMImplementationSource</code>.
 * Applications may also register DOMImplementationSource
 * implementations by using a method on this class. They may then
 * query instances of the registry for implementations supporting
 * specific features.
 *
 * <p>Example:</p>
 * <pre class='example'>
 * // get an instance of the DOMImplementation registry
 * DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
 * // get a DOM implementation the Level 3 XML module
 * DOMImplementation domImpl = registry.getDOMImplementation("XML 3.0");
 * </pre>
 * <p>This provides an application with an implementation-independent 
 * starting point.</p>
 *
 * @see DOMImplementation
 * @see DOMImplementationSource
 * @since DOM Level 3
 */

package org.apache.xerces.dom3.bootstrap;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.dom3.DOMImplementationSource;
import org.apache.xerces.dom3.DOMImplementationList;
import org.w3c.dom.DOMImplementation;

public class DOMImplementationRegistry { 

    // The system property to specify the DOMImplementationSource class names.
    public final static String PROPERTY =
        "org.w3c.dom.DOMImplementationSourceList";

    private Vector _sources;

    // deny construction by other classes
    private DOMImplementationRegistry() {
    }

    // deny construction by other classes
    private DOMImplementationRegistry(Vector srcs) {
        _sources = srcs;
    }


    /* 
     * This method queries the System property
     * <code>org.w3c.dom.DOMImplementationSourceList</code>. If it is
     * able to read and parse the property, it attempts to instantiate
     * classes according to each space-delimited substring. Any
     * exceptions it encounters are thrown to the application. An application
     * must call this method before using the class.
     * @return  an initialized instance of DOMImplementationRegistry
     */ 
    public static DOMImplementationRegistry newInstance() 		
            throws ClassNotFoundException, InstantiationException, 
            IllegalAccessException
    {
        Vector _sources = new Vector();    

        // fetch system property:
        String p = System.getProperty(PROPERTY);
        if (p != null) {
            StringTokenizer st = new StringTokenizer(p);
            while (st.hasMoreTokens()) {
                String sourceName = st.nextToken();
                // Use context class loader, falling back to Class.forName
                // if and only if this fails...
                Object source = getClass(sourceName).newInstance();
                _sources.add(source);
            }
        }
        return new DOMImplementationRegistry(_sources);
    }


    /**
     * Return the first registered implementation that has the desired
     * features, or null if none is found.
     *
     * @param features A string that specifies which features are required.
     *                 This is a space separated list in which each feature is
     *                 specified by its name optionally followed by a space
     *                 and a version number.
     *                 This is something like: "XML 1.0 Traversal +Events 2.0"
     * @return An implementation that has the desired features, or
     *   <code>null</code> if this source has none.
     */
    public DOMImplementation getDOMImplementation(String features)
            throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException
    {
	int    size  = _sources.size();
        String name  = null;
        for (int i = 0; i < size; i++) {
            DOMImplementationSource source =
                (DOMImplementationSource) _sources.get(i);

            DOMImplementation impl = source.getDOMImplementation(features);
            if (impl != null) {
                return impl;
            }	    
        }
        return null;
    }

    /**
     * Return the list of all registered implementation that support the desired
     * features.
     *
     * @param features A string that specifies which features are required.
     *                 This is a space separated list in which each feature is
     *                 specified by its name optionally followed by a space
     *                 and a version number.
     *                 This is something like: "XML 1.0 Traversal +Events 2.0"
     * @return A list of DOMImplementations that support the desired features.
     */
    public DOMImplementationList getDOMImplementationList(String features)
            throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, ClassCastException
    {
	int    size  = _sources.size();
        DOMImplementationListImpl list = new DOMImplementationListImpl();
        String name = null;
        for (int i = 0; i < size; i++) {
            DOMImplementationSource source =
                (DOMImplementationSource) _sources.get(i);

            DOMImplementationList impls =
                 source.getDOMImplementationList(features);
            for (int j = 0; j < impls.getLength(); j++) {
                list.add(impls.item(j));
            }
        }
        return list;
    }

    /**
     * Register an implementation.
     */
    public void addSource(DOMImplementationSource s)
            throws ClassNotFoundException,
            InstantiationException, IllegalAccessException
    {
        _sources.add(s);
    }

    private static Class getClass (String className)
                throws ClassNotFoundException, IllegalAccessException,
                InstantiationException {
        Method m = null;
        ClassLoader cl = null;

        try {
            m = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // Assume that we are running JDK 1.1, use the current ClassLoader
            cl = DOMImplementationRegistry.class.getClassLoader();
        }

        if (cl == null ) {
            try {
                cl = (ClassLoader) m.invoke(Thread.currentThread(), null);
            } catch (IllegalAccessException e) {
                // assert(false)
                throw new UnknownError(e.getMessage());
            } catch (InvocationTargetException e) {
                // assert(e.getTargetException() instanceof SecurityException)
                throw new UnknownError(e.getMessage());
            }
        }
        if (cl == null) { 
            // fall back to Class.forName
            return Class.forName(className);
        }
        try { 
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            return Class.forName(className);
        }
    }
}
  