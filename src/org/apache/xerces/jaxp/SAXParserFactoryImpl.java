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

import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Id$
 */

/**
 * This is the implementation specific class for the
 * <code>javax.xml.parsers.SAXParserFactory</code>. This is the platform
 * default implementation for the platform.
 */
public class SAXParserFactoryImpl extends SAXParserFactory {
    private Hashtable features;

    /**
     * Creates a new instance of <code>SAXParser</code> using the currently
     * configured factory parameters.
     * @return javax.xml.parsers.SAXParser
     */
    public SAXParser newSAXParser()
        throws ParserConfigurationException
    {
        SAXParser saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features);
        } catch (SAXException se) {
            // Translate to ParserConfigurationException
            throw new ParserConfigurationException(se.getMessage());
        }
	return saxParserImpl;
    }

    /**
     * Common code for translating exceptions
     */
    private SAXParserImpl newSAXParserImpl()
        throws ParserConfigurationException, SAXNotRecognizedException, 
        SAXNotSupportedException
    {
        SAXParserImpl saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features);
        } catch (SAXNotSupportedException e) {
            throw e;
        } catch (SAXNotRecognizedException e) {
            throw e;
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    /**
     * Sets the particular feature in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException, 
		SAXNotSupportedException
    {
        // XXX This is ugly.  We have to collect the features and then
        // later create an XMLReader to verify the features.
        if (features == null) {
            features = new Hashtable();
        }
        features.put(name, value ? Boolean.TRUE : Boolean.FALSE);

        // Test the feature by possibly throwing SAX exceptions
        try {
            newSAXParserImpl();
        } catch (SAXNotSupportedException e) {
            features.remove(name);
            throw e;
        } catch (SAXNotRecognizedException e) {
            features.remove(name);
            throw e;
        }
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
		SAXNotSupportedException
    {
        // Check for valid name by creating a dummy XMLReader to get
        // feature value
        return newSAXParserImpl().getXMLReader().getFeature(name);
    }
}
