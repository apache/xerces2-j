/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
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
 * originally based on software copyright (c) 1999-2000, Pierpaolo
 * Fumagalli <mailto:pier@betaversion.org>, http://www.apache.org.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.jaxp;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
     * Returns the XMLReader that is encapsulated by the implementation of
     * this class.
     */
    public XMLReader getXMLReader() throws SAXException {
        return (XMLReader)parser; // xerces implements both parser and reader.
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

    /**
     * Sets the particular property in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        throw new SAXNotRecognizedException("Feature: " + name);
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        throw new SAXNotRecognizedException("Feature: " + name);
    }

}
