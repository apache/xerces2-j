/*
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2003, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package schema;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;

/**
 * Test Schema processing
 *
 * @author Khaled Noaman, IBM
 */
public class Test extends DefaultHandler {


    // feature ids

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Namespace prefixes feature id (http://xml.org/sax/features/namespace-prefixes). */
    protected static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    /** Dynamic validation feature id (http://apache.org/xml/features/validation/dynamic). */
    protected static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";


    // property ids

    /** schema noNamespaceSchemaLocation property id (http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation). */
    protected static final String SCHEMA_NONS_LOCATION_ID = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";


    // Default data source location
    protected static final String SOURCE_LOC_ID = "./tests/schema/";


    // Default constructor
    public Test() {
    }


    // ErrorHandler methods

    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    }

    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
    }


    // Protected methods
    protected void printError(String type, SAXParseException ex) {

        System.err.print("[");
        System.err.print(type);
        System.err.print("] ");
        if (ex== null) {
            System.out.println("!!!");
        }
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

    public void testSettingNoNamespaceSchemaLocation() throws Exception {

        System.err.println("#");
        System.err.println("# Testing Setting noNamespaceSchemaLocation property");
        System.err.println("#");

        try {
            SAXParser parser = new org.apache.xerces.parsers.SAXParser();

            // Set features
            parser.setFeature(NAMESPACES_FEATURE_ID, true);
            parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, false);
            parser.setFeature(VALIDATION_FEATURE_ID, true);
            parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
            parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, false);
            parser.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, false);

            // Set properties
            parser.setProperty(SCHEMA_NONS_LOCATION_ID, "personal.xsd");

            // Set handlers
            parser.setContentHandler(this);
            parser.setErrorHandler(this);

            // parse document
            parser.parse(SOURCE_LOC_ID + "personal-schema.xml");
            System.err.println("Pass: " + SOURCE_LOC_ID + "personal-schema.xml");
            System.err.println();
        }
        catch (SAXException e) {
            System.err.println("Fail:" + e.getMessage());
        }
    }


    /** Main program entry point. */
    public static void main(String argv[]) throws Exception {

        Test test = new Test();
        test.testSettingNoNamespaceSchemaLocation();
    }
}
