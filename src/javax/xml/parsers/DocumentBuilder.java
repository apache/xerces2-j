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

package javax.xml.parsers;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

/**
 * Instances of <code>DocumentBuilder</code> provide a mechansim for
 * parsing XML documents into a DOM document tree represented by an
 * <code>org.w3c.dom.Document</code> object.
 * <br>
 * A <code>DocumentBuilder</code> instance is obtained from a
 * <code>DocumentBuilderFactory</code> by invoking its
 * <code>newDocumentBuilder()</code> method.
 * <br>
 * <b>NOTE:</b> <code>DocumentBuilder</code> uses several classes from the
 *     SAX API. This does not require that the implementor of the
 *     underlying DOM implementation use a SAX parser to parse XML content
 *     into a <code>org.w3c.dom.Document</code>. It merely requires that
 *     the implementation communicate with the application using these
 *     existing APIs.
 * <br>
 * <br>
 * <b>ATTENTION:</b> THIS IMPLEMENTATION OF THE "JAVAX.XML.PARSER" CLASSES
 *   IS NOT THE OFFICIAL REFERENCE IMPLEMENTATION OF THE JAVA SPECIFICATION
 *   REQUEST 5 FOUND AT
 *   <a href="http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html">
 *   http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html
 *   </a><br>
 *   THIS IMPLEMENTATION IS CONFORMANT TO THE "JAVA API FOR XML PARSING"
 *   SPECIFICATION VERSION 1.1 PUBLIC REVIEW 1 BY JAMES DUNCAN DAVIDSON
 *   PUBLISHED BY SUN MICROSYSTEMS ON NOV. 2, 2000 AND FOUND AT
 *   <a href="http://java.sun.com/xml">http://java.sun.com/xml</a>
 * <br>
 * <br>
 * <b>THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author <a href="pier@betaversion.org">Pierpaolo Fumagalli</a>
 * @author Copyright &copy; 2000 The Apache Software Foundation.
 * @version 1.0 CVS $Revision$ $Date$
 */
public abstract class DocumentBuilder {

    protected DocumentBuilder () {
    }

    /**
     * Parse the content of the given InputStream as an XML document
     * and return a new DOM Document object.
     *
     * @param is InputStream containing the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputStream is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public Document parse(InputStream is)
        throws SAXException, IOException
    {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        InputSource in = new InputSource(is);
        return parse(in);
    }

    /**
     * Parse the content of the given InputStream as an XML document
     * and return a new DOM Document object.
     *
     * @param is InputStream containing the content to be parsed.
     * @param systemId Provide a base for resolving relative URIs. 
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputStream is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public Document parse(InputStream is, String systemId)
        throws SAXException, IOException
    {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        InputSource in = new InputSource(is);
	in.setSystemId(systemId);
        return parse(in);
    }

    /**
     * Parse the content of the given URI as an XML document
     * and return a new DOM Document object.
     *
     * @param uri The location of the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the URI is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public Document parse(String uri)
        throws SAXException, IOException
    {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        
        InputSource in = new InputSource(uri);
        return parse(in);
    }

    /**
     * Parse the content of the given file as an XML document
     * and return a new DOM Document object.
     *
     * @param f The file containing the XML to parse
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the file is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public Document parse(File f)
       throws SAXException, IOException
    {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        String uri = "file:" + f.getAbsolutePath();
	if (File.separatorChar == '\\') {
	    uri = uri.replace('\\', '/');
	}
        InputSource in = new InputSource(uri);
        return parse(in);
    }

    /**
     * Parse the content of the given input source as an XML document
     * and return a new DOM Document object.
     *
     * @param is InputSource containing the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputSource is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public abstract Document parse(InputSource is)
        throws  SAXException, IOException;

    
    /**
     * Indicates whether or not this parser is configured to
     * understand namespaces.
     */

    public abstract boolean isNamespaceAware();

    /**
     * Indicates whether or not this parser is configured to
     * validate XML documents.
     */
    
    public abstract boolean isValidating();

    /**
     * Specify the <code>EntityResolver</code> to be used to resolve
     * entities present in the XML document to be parsed. Setting
     * this to <code>null</code> will result in the underlying
     * implementation using it's own default implementation and
     * behavior.
     */

    // XXX
    // Add that the underlying impl doesn't have to use SAX, but
    // must understand how to resolve entities from this object.
    
    public abstract void setEntityResolver(org.xml.sax.EntityResolver er);

    /**
     * Specify the <code>ErrorHandler</code> to be used to resolve
     * entities present in the XML document to be parsed. Setting
     * this to <code>null</code> will result in the underlying
     * implementation using it's own default implementation and
     * behavior.
     */

    // XXX
    // Add that the underlying impl doesn't have to use SAX, but
    // must understand how to handle errors using this object.
    
    public abstract void setErrorHandler(org.xml.sax.ErrorHandler eh);

    /**
     * Obtain a new instance of a DOM {@link org.w3c.dom.Document} object
     * to build a DOM tree with.  An alternative way to create a DOM
     * Document object is to use the
     * {@link #getDOMImplementation() getDOMImplementation}
     * method to get a DOM Level 2 DOMImplementation object and then use
     * DOM Level 2 methods on that object to create a DOM Document object.
     *
     * @return A new instance of a DOM Document object.
     */
    
    public abstract Document newDocument();

    /**
     * Obtain an instance of a {@link org.w3c.dom.DOMImplementation} object.
     *
     * @return A new instance of a <code>DOMImplementation</code>.
     */
    public abstract DOMImplementation getDOMImplementation();
}
