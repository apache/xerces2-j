/*
 *  The Apache Software License, Version 1.1
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
 4. The names "Xerces" and "Apache Software Foundation" must
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

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import org.xml.sax.helpers.*;

/** This testcase tests for following scenarios as per JAXP 1.2 specification.
 *
 * 1.Parser(SAX and DOM) should ignore SchemaLanguage property when validation
 *  feature is set to false.
 * 2.SAXParser should throw SAXNotSupportedException when SchemaSource property is
 *  set without setting SchemaLanguage property.
 * 3.DOMParser should throw IllegalArgumentException when SchemaSource property is
 *  set without setting SchemaLanguage property.
 *
 * @author k.venugopal@sun.com
 */

public class JAXPSpecTest extends DefaultHandler {
    
    /** TestCase Main.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        JAXPSpecTest jaxpTest = new JAXPSpecTest();
        
        if (args.length == 0) {
            jaxpTest.printUsage();
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("testSchemaLanguageSAX"))
                jaxpTest.testSchemaLanguageSAX();
            else if(arg.equals("testSchemaSourceSAX"))
                jaxpTest.testSchemaSourceSAX();
            else if(arg.equals("testSchemaLanguageDOM"))
                jaxpTest.testSchemaLanguageDOM();
            else if(arg.equals("testSchemaSourceDOM"))
                jaxpTest.testSchemaSourceDOM();
            else
                jaxpTest.printUsage();
        }
        
    }
    
    /** Schema Language property should be ignored if
     * validation feature is set to false
     * @throws Exception
     */
    
    public void testSchemaLanguageSAX() throws Exception{
        
        System.out.println(" Running JAXPSpecTest.testSchemaLanguageSAX ");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        SAXParser saxParser = spf.newSAXParser();
        //Schema Language Property should be ignored since validation is set to false.
        saxParser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
        saxParser.parse("tests/jaxp/data/personal-schema.xml",this);
        System.out.println(" JAXPSpecTest.testSchemaLanguageSAX Passed ");
    }
    
    /** SAXParser should throw SAXNotSupportedException when SchemaSource property is
     * set without setting SchemaLanguage property
     * @throws Exception
     */
    
    public void testSchemaSourceSAX() throws Exception{
        try{
            System.out.println(" Running JAXPSpecTest.testSchemaSourceSAX ");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            SAXParser saxParser = spf.newSAXParser();
            //Schema Language property should be set before setting schema source property.
            //setting this property should throw SAXNotSupportedException
            saxParser.setProperty(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            "tests/jaxp/data/personal-schema.xsd");
            saxParser.parse("tests/jaxp/data/personal-schema.xml",this);
        }catch(SAXNotSupportedException ne){
            System.out.println(" JAXPSpecTest.testSchemaSourceSAX Passed");
        }
    }
    
    /** Schema Language property should be ignored if
     * validation feature is set to false
     * @throws Exception  */
    
    public void testSchemaLanguageDOM() throws Exception {
        
        System.out.println(" Running JAXPSpecTest.testSchemaLanguageDOM ");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        //Schema Language Property should be ignored since validation is set to false.
        dbf.setAttribute(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        docBuilder.setErrorHandler(this);
        Document document = docBuilder.parse(
        new File("tests/jaxp/data/personal-schema.xml"));
        System.out.println(" JAXPSpecTest.testSchemaLanguageDOM Passed");
        
    }
    
    /** DOMParser should throw IllegalArgumentException when SchemaSource property is
     * set without setting SchemaLanguage property.
     * @throws Exception
     */
    
    public void testSchemaSourceDOM() throws Exception {
        try{
            System.out.println(" Running JAXPSpecTest.testSchemaSourceDOM ");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            //Schema Language property should be set before setting schema source property.
            //setting this property should throw IllegalArgumentException
            dbf.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            "tests/jaxp/data/personal-schema.xsd");
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            docBuilder.setErrorHandler(this);
            Document document = docBuilder.parse(
            "tests/jaxp/data/personal-schema.xml");
        } catch (IllegalArgumentException e) {
            System.out.println(" JAXPSpecTest.testSchemaSourceDOM Passed");
        }
        
    }
    
    
    /** Handles Warnings
     * @param ex
     * @throws SAXException
     */
    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    }
    
    /** Handles Errors.
     * @param ex
     * @throws SAXException
     */
    
    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    }
    
    /** Handles Fatal errors.
     * @param ex
     * @throws SAXException
     */
    
    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
        throw ex;
    }
    
    
    /**
     * @param type
     * @param ex
     */
    private void printError(String type, SAXParseException ex) {
        
        System.err.print("[");
        System.err.print(type);
        System.err.print("] ");
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            System.err.print(systemId);
        }
        System.err.print(':');
        System.err.print(ex.getLineNumber());
        System.err.print(':');
        System.err.print(ex.getColumnNumber());
        System.err.print(": ");
        System.err.print(ex.getMessage());
        System.err.println();
        System.err.flush();
        
    }
    
    private static void printUsage(){
        System.err.println("Usage : JAXPSpecTest testSchemaLanguageSAX testSchemaSourceSAX testSchemaLanguageDOM testSchemaSourceDOM  ... ");
    }
    
    
}

