/*
 * $Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */

package jaxp;

import junit.framework.*;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;
import java.net.*;

/**
 * Test JAXP 1.2 specific features
 *
 * @author Edwin Goei
 */
public class JAXP12Tests extends TestCase implements JAXPConstants {
    protected DocumentBuilderFactory dbf;
    protected DocumentBuilder db;
    protected DocumentBuilder dbn;
    protected DocumentBuilder dbnv;

    SAXParserFactory spf;
    SAXParser spn;
    SAXParser spnv;

    public JAXP12Tests(String name) {
        super(name);
    }

    private static class MyErrorHandler implements ErrorHandler {
        public void fatalError(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#fatalError() should not have been" +
                 " called: " +
                 x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
        public void error(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#error() should not have been called: " +
                 x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
        public void warning(SAXParseException x) throws SAXException {
            x.printStackTrace();
            fail("ErrorHandler#warning() should not have been called: "
                 + x.getMessage() +
                 " [line = " + x.getLineNumber() + ", systemId = " +
                 x.getSystemId() + "]");
        }
    }

    /**
     * Overrides method error() to see if it was ever called
     */
    private static class ErrorHandlerCheck extends MyErrorHandler {
        Boolean gotError = Boolean.FALSE;
        public void error(SAXParseException x) throws SAXException {
            gotError = Boolean.TRUE;
            throw x;
        }
        public Object getStatus() {
            return gotError;
        }
    };

    protected void setUp() throws Exception {
        dbf = DocumentBuilderFactory.newInstance();
        db = dbf.newDocumentBuilder();  // non-namespaceAware version
        dbf.setNamespaceAware(true);
        dbn = dbf.newDocumentBuilder(); // namespaceAware version
        dbn.setErrorHandler(new MyErrorHandler());
        dbf.setValidating(true);
        dbnv = dbf.newDocumentBuilder(); // validating version
        dbnv.setErrorHandler(new MyErrorHandler());

        spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spn = spf.newSAXParser();
        spf.setValidating(true);
        spnv = spf.newSAXParser();
    }

    /**
     * Should not cause a validation error.  Problem is that you get same
     * result if no validation is occurring at all.  See other tests that
     * checks that validation actually occurs.
     */
    public void testSaxParseXSD() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        XMLReader xr = spnv.getXMLReader();
        xr.setErrorHandler(new MyErrorHandler());
        xr.parse(new InputData("personal-schema.xml"));
    }

    /**
     * Should cause a validation error.  Checks that validation is indeed
     * occurring.  Warning: does not actually check for particular
     * validation error, but assumes any exception thrown is a validation
     * error of the type we expect.
     */
    public void testSaxParseXSD2() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        XMLReader xr = spnv.getXMLReader();

        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        xr.setErrorHandler(meh);
        try {
            xr.parse(new InputData("personal-schema-err.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }

    /**
     * Check that setting schemaSource overrides xsi: hint in instance doc
     */
    public void testSaxParseSchemaSource() throws Exception {
        spnv.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        spnv.setProperty(JAXP_SCHEMA_SOURCE, new InputData("personal.xsd"));
        XMLReader xr = spnv.getXMLReader();
        xr.setErrorHandler(new MyErrorHandler());
        xr.parse(new InputData("personal-schema-badhint.xml"));
        xr.parse(new InputData("personal-schema-nohint.xml"));
    }

    /**
     * Turn on DTD validation and expect an error b/c instance doc has no
     * doctypedecl
     */
    public void testSaxParseNoXSD() throws Exception {
        XMLReader xr = spnv.getXMLReader();

        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        xr.setErrorHandler(meh);
        try {
            xr.parse(new InputData("personal-schema.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }

    /**
     * Should not cause a validation error.  Problem is that you get same
     * result if no validation is occurring at all.  See other tests that
     * checks that validation actually occurs.
     */
    public void testDomParseXSD() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        mydb.setErrorHandler(new MyErrorHandler());
        mydb.parse(new InputData("personal-schema.xml"));
    }

    /**
     * Should cause a validation error.  Checks that validation is indeed
     * occurring.  Warning: does not actually check for particular
     * validation error, but assumes any exception thrown is a validation
     * error of the type we expect.
     */
    public void testDomParseXSD2() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        DocumentBuilder mydb = dbf.newDocumentBuilder();

        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        mydb.setErrorHandler(meh);
        try {
            mydb.parse(new InputData("personal-schema-err.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }

    /**
     * Check that setting schemaSource overrides xsi: hint in instance doc
     */
    public void testDomParseSchemaSource() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
        dbf.setAttribute(JAXP_SCHEMA_SOURCE, new InputData("personal.xsd"));
        DocumentBuilder mydb = dbf.newDocumentBuilder();
        mydb.setErrorHandler(new MyErrorHandler());
        mydb.parse(new InputData("personal-schema-badhint.xml"));
        mydb.parse(new InputData("personal-schema-nohint.xml"));
    }

    /**
     * Turn on DTD validation and expect an error b/c instance doc has no
     * doctypedecl
     */
    public void testDomParseNoXSD() throws Exception {
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        DocumentBuilder mydb = dbf.newDocumentBuilder();

        ErrorHandlerCheck meh = new ErrorHandlerCheck();
        mydb.setErrorHandler(meh);
        try {
            mydb.parse(new InputData("personal-schema.xml"));
            fail("ErrorHandler.error() should have thrown a SAXParseException");
        } catch (SAXException x) {
            assertEquals("Should have caused validation error.",
                         Boolean.TRUE, meh.getStatus());
        }
    }

    /**
     * Used to run a single test for debuggin.  Remove the "Debug" suffix.
     */
    public static Test suiteDebug() {
        TestSuite suite = new TestSuite();
        suite.addTest(new JAXP12Tests("testDomParseNoXSD"));
        return suite;
    }
}
