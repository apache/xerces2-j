/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */


package org.w3c.dom; 

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * This class holds the list of registered DOMImplementations. It is first
 * initialized based on the content of the space separated list of classnames
 * contained in the System Property "org.w3c.dom.DOMImplementationSourceList".
 *
 * <p>Subsequently, additional sources can be registered and implementations
 * can be queried based on a list of requested features.
 *
 * <p>This provides an application with an implementation independent starting
 * point.
 *
 * @see DOMImplementation
 * @see DOMImplementationSource
 */
public class DOMImplementationRegistry 
{ 

    // The system property to specify the DOMImplementationSource class names. 
    public static String PROPERTY = "org.w3c.dom.DOMImplementationSourceList";

    private static Vector sources = new Vector();
    private static boolean initialized = false;

    private static void initialize() throws ClassNotFoundException,
        InstantiationException, IllegalAccessException
    {
        initialized = true;
        String p = System.getProperty(PROPERTY);
        if (p == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(p);
        while (st.hasMoreTokens()) {
            Object source = Class.forName(st.nextToken()).newInstance();
            sources.addElement(source);
        }
    }

    /**
     * Return the first registered implementation that has the desired features,
     * or null if none is found.
     *
     * @param features A string that specifies which features are required.
     *                 This is a space separated list in which each feature is
     *                 specified by its name optionally followed by a space
     *                 and a version number.
     *                 This is something like: "XML 1.0 Traversal Events 2.0"
     * @return An implementation that has the desired features, or
     *   <code>null</code> if this source has none.
     */
    public static DOMImplementation getDOMImplementation(String features)
        throws ClassNotFoundException,
        InstantiationException, IllegalAccessException
    {
        if (!initialized) {
            initialize();
        }
        int len = sources.size(); 
        for (int i = 0; i < len; i++) {
            DOMImplementationSource source =
                (DOMImplementationSource) sources.elementAt(i);

            DOMImplementation impl = source.getDOMImplementation(features);
            if (impl != null) {
                return impl;
            }
        }
        return null;
    }

    /**
     * Register an implementation.
     */
    public static void addSource(DOMImplementationSource s)
        throws ClassNotFoundException,
        InstantiationException, IllegalAccessException
    {
        if (!initialized) {
            initialize();
        }
        sources.addElement(s);
        // update system property accordingly
        StringBuffer b = new StringBuffer(System.getProperty(PROPERTY));
        b.append(" " + s.getClass().getName());
        System.setProperty(PROPERTY, b.toString());
    }
}
  