/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.xerces.jaxp;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import java.util.Hashtable;

import org.apache.xerces.parsers.DOMParser;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Id$
 */
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {
    /** These are DocumentBuilderFactory attributes not DOM attributes */
    private Hashtable attributes;

    /**
     * Creates a new instance of a {@link javax.xml.parsers.DocumentBuilder}
     * using the currently configured parameters.
     */
    public DocumentBuilder newDocumentBuilder()
        throws ParserConfigurationException 
    {
        try {
            return new DocumentBuilderImpl(this, attributes);
        } catch (SAXException se) {
            // Handles both SAXNotSupportedException, SAXNotRecognizedException
            throw new ParserConfigurationException(se.getMessage());
        }
    }

    /**
     * Allows the user to set specific attributes on the underlying 
     * implementation.
     * @param name    name of attribute
     * @param value   null means to remove attribute
     */
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException
    {
        // This handles removal of attributes
        if (value == null) {
            if (attributes != null) {
                attributes.remove(name);
            }
            // Unrecognized attributes do not cause an exception
            return;
        }
        
        // This is ugly.  We have to collect the attributes and then
        // later create a DocumentBuilderImpl to verify the attributes.

        // Create Hashtable if none existed before
        if (attributes == null) {
            attributes = new Hashtable();
        }

        attributes.put(name, value);

        // Test the attribute name by possibly throwing an exception
        try {
            new DocumentBuilderImpl(this, attributes);
        } catch (Exception e) {
            attributes.remove(name);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying 
     * implementation.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException
    {
        // See if it's in the attributes Hashtable
        if (attributes != null) {
            Object val = attributes.get(name);
            if (val != null) {
                return val;
            }
        }

        DOMParser domParser = null;
        try {
            // We create a dummy DocumentBuilderImpl in case the attribute
            // name is not one that is in the attributes hashtable.
            domParser =
                new DocumentBuilderImpl(this, attributes).getDOMParser();
            return domParser.getProperty(name);
        } catch (SAXException se1) {
            // assert(name is not recognized or not supported), try feature
            try {
                boolean result = domParser.getFeature(name);
                // Must have been a feature
                return result ? Boolean.TRUE : Boolean.FALSE;
            } catch (SAXException se2) {
                // Not a property or a feature
                throw new IllegalArgumentException(se1.getMessage());
            }
        }
    }
}
