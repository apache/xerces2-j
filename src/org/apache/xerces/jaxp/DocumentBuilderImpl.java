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

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * The <code>DocumentBuilder</code> implementation for the Apache Xerces XML
 * parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version $Revision$ $Date$
 */
public class DocumentBuilderImpl extends DocumentBuilder {

    /** Wether this <code>SAXParserImpl</code> supports namespaces. */
    private boolean namespaces=false;
    /** Wether this <code>SAXParserImpl</code> supports validataion. */
    private boolean validation=false;
    /** The current Xerces SAX <code>Parser</code>. */
    private DOMParser parser=null;

    /** Deny no-argument construction. */
    private DocumentBuilderImpl() {
        super();
    }

    /**
     * Create a new <code>SAXParserFactoryImpl</code> instance.
     */
    protected DocumentBuilderImpl(boolean namespaces, boolean validation)
    throws ParserConfigurationException {
        this();
        DOMParser p=new DOMParser();
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
     * Parses the content of the given <code>InputSource</code> and returns
     * a <code>Document</code> object.
     */
    public Document parse(InputSource source)
    throws SAXException, IOException, IllegalArgumentException {
        if (source==null) throw new IllegalArgumentException();
        this.parser.parse(source);
        return(this.parser.getDocument());
    }

    /**
     * Creates an new <code>Document</code> instance from the underlying DOM
     * implementation.
     */
    public Document newDocument() {
        return(new org.apache.xerces.dom.DocumentImpl());
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
     * Specifies the <code>EntityResolver</code> to be used by this
     * <code>DocumentBuilderImpl</code>.
     */
    public void setEntityResolver(EntityResolver er) {
        this.parser.setEntityResolver(er);
    }

    /**
     * Specifies the <code>ErrorHandler</code> to be used by this
     * <code>DocumentBuilderImpl</code>.
     */
    public void setErrorHandler(ErrorHandler eh) {
        this.parser.setErrorHandler(eh);
    }
}

