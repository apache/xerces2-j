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
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Implementation instances of the <code>SAXParser</code> abstract class
 * contain an implementation of the <code>org.xml.sax.Parser</code> interface
 * and enables content from a variety of sources to be parsed using the
 * contained parser.
 * <br>
 * Instances of <code>SAXParser</code> are obtained from a
 * <code>SAXParserFactory</code> by invoking its <code>newSAXParser()</code>
 * method.
 * <br>
 * <br>
 * <b>ATTENTION:</b> THIS IMPLEMENTATION OF THE "JAVAX.XML.PARSER" CLASSES
 *   IS NOT THE OFFICIAL REFERENCE IMPLEMENTATION OF THE JAVA SPECIFICATION
 *   REQUEST 5 FOUND AT
 *   <a href="http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html">
 *   http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html
 *   </a><br>
 *   THIS IMPLEMENTATION IS CONFORMANT TO THE "JAVA API FOR XML PARSING"
 *   SPECIFICATION VERSION 1.0 PUBLIC RELEASE 1 BY JAMES DUNCAN DAVIDSON
 *   PUBLISHED BY SUN MICROSYSTEMS ON FEB. 18, 2000 AND FOUND AT
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
public abstract class SAXParser {

    /**
     * Implementations should provide a protected constructor so that 
     * their factory implementation can instantiate instances of the 
     * implementation class.
     * <br>
     * Application programmers should not be able to directly construct 
     * implementation subclasses of this abstract subclass. The only way a 
     * application should be able to obtain a reference to a SAXParser 
     * implementation instance is by using the appropriate methods of the 
     * <code>SAXParserFactory</code>.
     */
    protected SAXParser() {
        super();
    }

    /**
     * Parses the contents of the given <code>InputStream</code> as an XML
     * document using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur reading the given
     *                <code>InputStream</code>.
     * @exception  IllegalArgumentException If the given
     *                <code>InputStream</code> is <b>null</b>.
     */
    public void parse(InputStream stream, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (stream==null) throw new IllegalArgumentException();
        this.parse(new InputSource(stream),base);
    }

    /**
     * Parses the content of the given URI as an XML document using the
     * specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur while reading content
     *                located by the given URI.
     * @exception IllegalArgumentException If the given URI is <b>null</b>.
     */
    public void parse(String uri, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (uri==null) throw new IllegalArgumentException();
        this.parse(new InputSource(uri),base);
    }

    /**
     * Parses the content of the given <code>File</code> as an XML document
     * using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO errors occur while reading content
     *                from the given <code>File</code>.
     * @exception IllegalArgumentException if the given <code>File</code> is
     *                <b>null</b>.
     */
    public void parse(File file, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (file==null) throw new IllegalArgumentException();
        String path = file.getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/") && file.isDirectory()) {
            path = path + "/";
        }
        java.net.URL url = new java.net.URL("file", "", path);
        this.parse(new InputSource(url.toString()),base);
    }

    /**
     * Parses the content of the given <code>InputSource</code> as an XML
     * document using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO Errors occur while reading content
     *                from the given <code>InputSource</code>.
     * @exception IllegalArgumentException if the given
     *                <code>InputSource</code> is <b>null</b>.
     */
    public void parse(InputSource source, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (source==null) throw new IllegalArgumentException();

        // Get the SAX parser instance
        Parser p=this.getParser();

        // Set the various handler instances
        if (base!=null) {
            p.setDocumentHandler(base);
            p.setDTDHandler(base);
            p.setEntityResolver(base);
            p.setErrorHandler(base);
        }

        // Parse the specified source
        p.parse(source);
    }

    /**
     * Returns the underlying <code>Parser</code> object which is wrapped by
     * this <code>SAXParser</code> implementation.
     *
     * @exception SAXException If the initialization of the underlying parser
     *                fails. <b>NOTE:</b> This Exception is specified on page
     *                21 of the specification, but later on omissed in this
     *                method documentation on page 23. Wich one is correct?
     */
    public abstract Parser getParser()
    throws SAXException;

    /**
     * Returns whether or not this parser supports XML namespaces.
     */
    public abstract boolean isNamespaceAware();

    /**
     * Returns whether or not this parser supports validating XML content.
     */
    public abstract boolean isValidating();
}
