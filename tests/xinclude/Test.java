/*
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
package xinclude;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.xerces.parsers.XIncludeParserConfiguration;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import xni.Writer;

/**
 * Tests for XInclude implementation.
 * Use -f option to see the error message log
 * @author Peter McCracken, IBM
 */
public class Test implements XMLErrorHandler {
    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID =
        "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID =
        "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema-full-checking";

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER =
        "http://apache.org/xml/properties/internal/error-handler";

    protected static PrintWriter log = null;
    protected static boolean useLog = false;
    protected static String logFile = null;

    // this array contains whether the test number NN (contained in file testNN.xml)
    // is meant to be a pass or fail test
    // true means the test should pass
    private static boolean[] TEST_RESULTS = new boolean[] {
        // one value for each test
        true, true, true, true, true, true, false, true, false, true, // 10
        false, false, false, false, true, true, true, false, true, true, // 20
        true, false, true, false, false, false, true, true, false, true, // 30
        true, false, true, };

    public static void main(String[] args) {
        XMLParserConfiguration parserConfig = new XIncludeParserConfiguration();
        parserConfig.setFeature(NAMESPACES_FEATURE_ID, true);
        parserConfig.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        parserConfig.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);

        Writer writer = null;
        int firstTest = 0;

        if (args.length > 0) {
            if (args[0].equals("-f")) {
                useLog = true;
                if (args.length > 1) {
                    try {
                        logFile = args[1];
                        log = new PrintWriter(new FileOutputStream(logFile));
                        firstTest = 2;
                    }
                    catch (IOException e) {
                        System.err.println(
                            "Error initializing XInclude tests.");
                        System.err.println(
                            "Couldn't initialize log file: " + logFile);
                        e.printStackTrace(System.err);
                        printUsage();
                        System.err.println();
                        System.err.println("XInclude testing aborted");
                        System.exit(1);
                    }
                }
                else {
                    printUsage();
                    System.exit(1);
                }
            }
        }

        try {
            // We'll just ignore the output, for now.
            // Later, we when output relative URIs instead of absolute ones,
            // we can actually compare output to a set of expected outputs.
            OutputStream output = new OutputStream() {
                public void write(int b) throws IOException {
                }
            };
            writer = new Writer(parserConfig);
            writer.setOutput(output, "UTF8");
            parserConfig.setProperty(ERROR_HANDLER, new Test());
        }
        catch (Exception e) {
            System.err.println("Error initializing XInclude tests");
            if (useLog) {
                e.printStackTrace(log);
            }
            printDetailsMessage();
            System.err.println("XInclude testing aborted");
            System.exit(1);
        }

        int totalFailures = 0;
        int totalTests = 0;
        if (firstTest >= args.length) {
            for (int i = firstTest; i < TEST_RESULTS.length; i++) {
                totalTests++;
                if (!runTest(i + 1, writer)) {
                    totalFailures++;
                }
            }
        }
        else {
            for (int i = firstTest; i < args.length; i++) {
                int testnum = Integer.parseInt(args[i]);
                totalTests++;
                if (!runTest(testnum, writer)) {
                    totalFailures++;
                }
            }
        }
        
        if (log != null) {
            log.close();
        }
        
        if (totalFailures == 0) {
            System.out.println("All XInclude Tests Passed");
        }
        else {
            System.err.println(
                "Total failures for XInclude: "
                    + totalFailures
                    + "/"
                    + totalTests);
            printDetailsMessage();
            System.exit(1);
        }
    }

    private static boolean runTest(int testnum, Writer writer) {
        String testname = "tests/xinclude/tests/test";
        if (testnum < 10) {
            testname += "0" + testnum;
        }
        else {
            testname += testnum;
        }
        testname += ".xml";

        try {
            writer.parse(new XMLInputSource(null, testname, null));
            if (TEST_RESULTS[testnum - 1]) {
                if (useLog) {
                    log.println("PASS: " + testname);
                }
                return true;
            }
            else {
                if (useLog) {
                    log.println("FAIL: " + testname);
                }
                return false;
            }
        }
        catch (XNIException e) {
            if (TEST_RESULTS[testnum - 1]) {
                if (useLog) {
                    log.println("FAIL: " + testname);
                }
                return false;
            }
            else {
                if (useLog) {
                    log.println("PASS: " + testname);
                }
                return true;
            }
        }
        catch (IOException e) {
            if (useLog) {
                log.println("Unexpected IO problem: " + e);
                log.println("FAIL: " + testname);
            }
            return false;
        }
    }
    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void error(String domain, String key, XMLParseException exception)
        throws XNIException {
        printError("Error", exception);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void fatalError(
        String domain,
        String key,
        XMLParseException exception)
        throws XNIException {
        printError("Fatal Error", exception);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void warning(String domain, String key, XMLParseException exception)
        throws XNIException {
        printError("Warning", exception);
    }

    private static void printUsage() {
        System.out.println("java xinclude.Test [-f file] [TESTS]");
        System.out.println(
            "  -f file : specifies a log file to print detailed error messages to.");
        System.out.println(
            "            If this option is absent, the messages will not be output.");
        System.out.println(
            "    TESTS : a space separated list of tests to run, specified by test number.");
        System.out.println(
            "          : If this is absent, all tests will be run.");
    }

    private static void printDetailsMessage() {
        if (useLog) {
            System.err.println("See file " + logFile + " for details");
        }
        else {
            System.err.println("Re-run with -f option to get details.");
        }
    }

    /** Prints the error message. */
    protected void printError(String type, XMLParseException ex) {
        if (!useLog)
            return;
        log.print("[");
        log.print(type);
        log.print("] ");
        String systemId = ex.getExpandedSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            log.print(systemId);
        }
        log.print(':');
        log.print(ex.getLineNumber());
        log.print(':');
        log.print(ex.getColumnNumber());
        log.print(": ");
        log.print(ex.getMessage());
        log.println();
        log.flush();
    } // printError(String,XMLParseException)
}
