/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package dom.rename;

import java.io.PrintWriter;

import org.w3c.dom.*;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dom.util.Assertion;
import dom.ParserWrapper;

/**
 * A simple program to test Document.getElementById() and the management
 * of ID attributes. Originally based on dom.Counter.
 * This test takes as input input.xml file
 *
 * @author Andy Clark, IBM
 * @author Arnaud  Le Hors, IBM
 *
 * @version $Id$
 */
public class Test implements UserDataHandler {

    //
    // Constants
    //

    // feature ids

    protected static final String NAMESPACES_FEATURE_ID =
        "http://xml.org/sax/features/namespaces";

    protected static final String VALIDATION_FEATURE_ID =
        "http://xml.org/sax/features/validation";

    protected static final String SCHEMA_VALIDATION_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema";

    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema-full-checking";

    protected static final String DEFERRED_DOM_FEATURE_ID =
        "http://apache.org/xml/features/dom/defer-node-expansion";

    // default settings

    protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";

    protected static final boolean DEFAULT_NAMESPACES = true;

    protected static final boolean DEFAULT_VALIDATION = false;

    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;

    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    // Xerces specific feature
    protected static final boolean DEFAULT_DEFERRED_DOM = true;

    //
    // Public methods
    //

    /** Performs the actual test. */
    public void test(Document doc) {

        System.out.println("DOM rename Test...");

	// getting the first "email" element
	NodeList elements = doc.getElementsByTagName("email");
	Element child = (Element) elements.item(0);
	Assertion.verify(child != null);
	Assertion.equals(child.getNodeName(), "email");

	// default must be there
	Attr at = child.getAttributeNode("defaultEmailAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultEmailValue");
	Assertion.verify(at.getSpecified() == false);

	// attach some data
	child.setUserData("mydata", "yo", this);
	Assertion.equals((String) child.getUserData("mydata"), "yo");

	// renaming an element without a url
	Element newChild = (Element) doc.renameNode(child, null, "url");

	Assertion.equals(newChild.getNodeName(), "url");
	Assertion.verify(newChild.getNamespaceURI() == null);

	// old default must no longer be there
	Assertion.verify(newChild.hasAttribute("defaultEmailAttr") == false);
	Assertion.verify(at.getSpecified() == true);

	// new default must be there
	at = newChild.getAttributeNode("defaultUrlAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultUrlValue");
	Assertion.verify(at.getSpecified() == false);

	// data must still be there
	Assertion.equals((String) newChild.getUserData("mydata"), "yo");
	// and handler must have been called if new node was created
	if (newChild != child) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == child);
	    Assertion.verify(lastDestination == newChild);
	    resetHandlerData();
	}

	// renaming an element with a url
	Element newChild2 = (Element) doc.renameNode(newChild, "ns1", "foo");

	Assertion.equals(newChild2.getNodeName(), "foo");
	Assertion.equals(newChild2.getNamespaceURI(), "ns1");
	Assertion.verify(newChild2.hasAttribute("defaultUrlAttr") == false);
	// data must still be there
	Assertion.equals((String) newChild2.getUserData("mydata"), "yo");
	// and handler must have been called if new node was created
	if (newChild2 != newChild) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == newChild);
	    Assertion.verify(lastDestination == newChild2);
	    resetHandlerData();
	}

	// getting the second "email" element
	child = (Element) elements.item(1);
	Assertion.verify(child != null);
	Assertion.equals(child.getNodeName(), "email");

	// default must be there
	at = child.getAttributeNode("defaultEmailAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultEmailValue");
	Assertion.verify(at.getSpecified() == false);

	// attach some data
	at.setUserData("mydata", "yo", this);
	Assertion.equals((String) at.getUserData("mydata"), "yo");

	// renaming an attribute without a url
	Attr newAt = (Attr) doc.renameNode(at, null, "foo");
	Assertion.verify(newAt != null);
	Assertion.equals(newAt.getNodeName(), "foo");
	Assertion.equals(newAt.getNamespaceURI(), null);
	Assertion.equals(newAt.getValue(), "defaultEmailValue");
	Assertion.verify(newAt.getSpecified() == true);
	Assertion.verify(child.hasAttribute("foo") == true);
	// default must be back
	Assertion.verify(child.hasAttribute("defaultEmailAttr") == true);
	// data must still be there
	Assertion.equals((String) newAt.getUserData("mydata"), "yo");
	// and handler must have been called if new node was created
	if (newAt != at) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == at);
	    Assertion.verify(lastDestination == newAt);
	    resetHandlerData();
	}

	// renaming an attribute with a url
	Attr newAt2 = (Attr) doc.renameNode(newAt, "ns1", "bar");
	Assertion.verify(newAt2 != null);
	Assertion.equals(newAt2.getNodeName(), "bar");
	Assertion.equals(newAt2.getNamespaceURI(), "ns1");
	Assertion.equals(newAt2.getValue(), "defaultEmailValue");
	Assertion.verify(newAt2.getSpecified() == true);
	Assertion.verify(child.hasAttributeNS("ns1", "bar") == true);
	// data must still be there
	Assertion.equals((String) newAt2.getUserData("mydata"), "yo");
	// and handler must have been called if new node was created
	if (newAt2 != newAt) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == newAt);
	    Assertion.verify(lastDestination == newAt2);
	    resetHandlerData();
	}


        System.out.println("done.");

    } // test(Document)

    // UserDataHandler related data
    short lastOperation = -1;
    String lastKey;
    Object lastData;
    Node lastSource;
    Node lastDestination;

    void resetHandlerData() {
	lastOperation = -1;
	lastKey = null;
	lastData = null;
	lastSource = null;
	lastDestination = null;
    }

    /**
     * This method is called whenever the node for which this handler is 
     * registered is imported, cloned, or renamed.
     * @param operation Specifies the type of operation that is being 
     *   performed on the node.
     * @param key Specifies the key for which this handler is being called. 
     * @param data Specifies the data for which this handler is being called. 
     * @param src Specifies the node being cloned, imported, or renamed. This 
     *   is <code>null</code> when the node is being deleted.
     * @param dst Specifies the node newly created if any, or 
     *   <code>null</code>.
     */
    public void handle(short operation, String key, Object data,
		       Node src, Node dst) {
	lastOperation = operation;
	lastKey = key;
	lastData = data;
	lastSource = src;
	lastDestination = dst;
    }

    //
    // MAIN
    //

    /** Main program entry point. */
    public static void main(String argv[]) {

        // is there anything to do?
        /*if (argv.length == 0) {
            printUsage();
            System.exit(1);
        } */

        
        // variables
        Test test = new Test();
        ParserWrapper parser = null;
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean validation = DEFAULT_VALIDATION;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        boolean deferredDom = DEFAULT_DEFERRED_DOM;
        
        String inputfile="tests/dom/rename/input.xml";
        
        // process arguments
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (option.equals("p")) {
                    // get parser name
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -p"
                                           + " option.");
                    }
                    String parserName = argv[i];

                    // create parser
                    try {
                        parser = (ParserWrapper)
                            Class.forName(parserName).newInstance();
                    }
                    catch (Exception e) {
                        parser = null;
                        System.err.println("error: Unable to instantiate "
                                           + "parser (" + parserName + ")");
                    }
                    continue;
                }
                if (option.equalsIgnoreCase("n")) {
                    namespaces = option.equals("n");
                    continue;
                }
                if (option.equalsIgnoreCase("v")) {
                    validation = option.equals("v");
                    continue;
                }
                if (option.equalsIgnoreCase("s")) {
                    schemaValidation = option.equals("s");
                    continue;
                }
                if (option.equalsIgnoreCase("f")) {
                    schemaFullChecking = option.equals("f");
                    continue;
                }
                if (option.equalsIgnoreCase("d")) {
                    deferredDom = option.equals("d");
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
            }
        }

            // use default parser?
            if (parser == null) {

                // create parser
                try {
                    parser = (ParserWrapper)
                        Class.forName(DEFAULT_PARSER_NAME).newInstance();
                }
                catch (Exception e) {
                    System.err.println("error: Unable to instantiate parser ("
                                       + DEFAULT_PARSER_NAME + ")");
                    System.exit(1);
                }
            }

            // set parser features
            try {
                parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + NAMESPACES_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(VALIDATION_FEATURE_ID, validation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + VALIDATION_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID,
                                  schemaValidation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + SCHEMA_VALIDATION_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID,
                                  schemaFullChecking);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + SCHEMA_FULL_CHECKING_FEATURE_ID + ")");
            }

            if (parser instanceof dom.wrappers.Xerces) {
                try {
                    parser.setFeature(DEFERRED_DOM_FEATURE_ID,
                                      deferredDom);
                }
                catch (SAXException e) {
                    System.err.println("warning: Parser does not support " +
                                       "feature (" +
                                       DEFERRED_DOM_FEATURE_ID + ")");
                }
            }

	    Document document = null;
            // parse file
            try {
                document = parser.parse(inputfile);
            }
            catch (SAXParseException e) {
                // ignore
            }
            catch (Exception e) {
                System.err.println("error: Parse error occurred - " +
                                   e.getMessage());
                Exception se = e;
                if (e instanceof SAXException) {
                    se = ((SAXException)e).getException();
                }
                if (se != null)
                  se.printStackTrace(System.err);
                else
                  e.printStackTrace(System.err);

		return;
            }

	    test.test(document);
        

    } // main(String[])

    //
    // Private static methods
    //

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java dom.ids.Test (options) " +
                           "...data/personal.xml");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -p name    Select parser by name.");
        System.err.println("  -d  | -D   Turn on/off (Xerces) deferred DOM.");
        System.err.println("  -n  | -N   Turn on/off namespace processing.");
        System.err.println("  -v  | -V   Turn on/off validation.");
        System.err.println("  -s  | -S   Turn on/off Schema validation " +
                           "support.");
        System.err.println("             NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F   Turn on/off Schema full checking.");
        System.err.println("             NOTE: Requires use of -s and not " +
                           "supported by all parsers.");
        System.err.println("  -h         This help screen.");
        System.err.println();

        System.err.println("defaults:");
        System.err.println("  Parser:     " + DEFAULT_PARSER_NAME);
        System.err.println("  Xerces Deferred DOM: " +
                           (DEFAULT_DEFERRED_DOM ? "on" : "off"));
        System.err.println("  Namespaces: " +
                           (DEFAULT_NAMESPACES ? "on" : "off"));
        System.err.println("  Validation: " +
                           (DEFAULT_VALIDATION ? "on" : "off"));
        System.err.println("  Schema:     " +
                           (DEFAULT_SCHEMA_VALIDATION ? "on" : "off"));
        System.err.println("  Schema full checking:     " +
                           (DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off"));

    } // printUsage()

} // class Test
