/*****************************************************************************
 * Copyright (C) 2000 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.xerces.jaxp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * The <code>SAXParser</code> implementation for the Apache Xerces XML parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version $Revision$ $Date$
 */
public class SAXParserImpl extends javax.xml.parsers.SAXParser {

    /** Wether this <code>SAXParserImpl</code> supports namespaces. */
    private boolean namespaces=false;
    /** Wether this <code>SAXParserImpl</code> supports validataion. */
    private boolean validation=false;
    /** The current Xerces SAX <code>Parser</code>. */
    private Parser parser=null;

    /** Deny no-argument construction. */
    private SAXParserImpl() {
        super();
    }

    /**
     * Create a new <code>SAXParserFactoryImpl</code> instance.
     */
    protected SAXParserImpl(boolean namespaces, boolean validation)
    throws ParserConfigurationException {
        this();
        SAXParser p=new SAXParser();
        try {
            p.setFeature("http://xml.org/sax/features/namespaces",namespaces);
        } catch (SAXException e) {
            throw new ParserConfigurationException("Cannot set namespace "+
                "awareness to "+namespaces);
        }
        try {
            p.setFeature("http://xml.org/sax/features/validation",validation);
        } catch (SAXException e) {
            throw new ParserConfigurationException("Cannot set validation to "+
                validation);
        }
        this.namespaces=namespaces;
        this.validation=validation;
        this.parser=p;
    }

    /**
     * Returns the underlying <code>Parser</code> object which is wrapped by
     * this <code>SAXParserImpl</code> implementation.
     */
    public Parser getParser() {
        return(this.parser);
    }

    /**
     * Returns whether or not this parser supports XML namespaces.
     */
    public boolean isNamespaceAware() {
        return(this.namespaces);
    }

    /**
     * Returns whether or not this parser supports validating XML content.
     */
    public boolean isValidating() {
        return(this.validation);
    }
}