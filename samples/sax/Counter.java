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

package sax;

import java.io.PrintWriter;

import org.xml.sax.Attributes;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.ParserFactory;

/**
 * A sample SAX2 counter. This sample program illustrates how to
 * register a SAX2 ContentHandler and receive the callbacks in
 * order to print information about the document. The output of
 * this program shows the time and count of elements, attributes,
 * ignorable whitespaces, and characters appearing in the document.
 * <p>
 * This class is useful as a "poor-man's" performance tester to
 * compare the speed and accuracy of various SAX parsers. However,
 * it is important to note that the first parse time of a parser
 * will include both VM class load time and parser initialization
 * that would not be present in subsequent parses with the same
 * file.
 * <p>
 * <strong>Note:</strong> The results produced by this program
 * should never be accepted as true performance measurements.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class Counter
    extends DefaultHandler {

    static {
        try {
            //String encoding = System.getProperty("file.encoding");
            //String userdir = org.apache.xerces.impl.XMLEntityManager.getUserDir();
            //int i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            //System.exit(1);
        }
    }

    //
    // Constants
    //

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

    // default settings

    /** Default parser name. */
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    /** Default repetition (1). */
    protected static final int DEFAULT_REPETITION = 1;

    /** Default namespaces support (true). */
    protected static final boolean DEFAULT_NAMESPACES = true;

    /** Default namespace prefixes (false). */
    protected static final boolean DEFAULT_NAMESPACE_PREFIXES = false;

    /** Default validation support (false). */
    protected static final boolean DEFAULT_VALIDATION = false;

    /** Default Schema validation support (false). */
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;

    /** Default Schema full checking support (false). */
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    /** Default dynamic validation support (false). */
    protected static final boolean DEFAULT_DYNAMIC_VALIDATION = false;

    /** Default memory usage report (false). */
    protected static final boolean DEFAULT_MEMORY_USAGE = false;

    /** Default "tagginess" report (false). */
    protected static final boolean DEFAULT_TAGGINESS = false;

    //
    // Data
    //

    /** Number of elements. */
    protected long fElements;

    /** Number of attributes. */
    protected long fAttributes;

    /** Number of characters. */
    protected long fCharacters;

    /** Number of ignorable whitespace characters. */
    protected long fIgnorableWhitespace;

    /** Number of characters of tags. */
    protected long fTagCharacters;

    /** Number of other content characters for the "tagginess" calculation. */
    protected long fOtherCharacters;

    //
    // Constructors
    //

    /** Default constructor. */
    public Counter() {
    } // <init>()

    //
    // Public methods
    //

    /** Prints the results. */
    public void printResults(PrintWriter out, String uri, long time,
                             long memory, boolean tagginess,
                             int repetition) {

        // filename.xml: 631 ms (4 elems, 0 attrs, 78 spaces, 0 chars)
        out.print(uri);
        out.print(": ");
        if (repetition == 1) {
            out.print(time);
        }
        else {
            out.print(time);
            out.print('/');
            out.print(repetition);
            out.print('=');
            out.print(time/repetition);
        }
        out.print(" ms");
        if (memory != Long.MIN_VALUE) {
            out.print(", ");
            out.print(memory);
            out.print(" bytes");
        }
        out.print(" (");
        out.print(fElements);
        out.print(" elems, ");
        out.print(fAttributes);
        out.print(" attrs, ");
        out.print(fIgnorableWhitespace);
        out.print(" spaces, ");
        out.print(fCharacters);
        out.print(" chars)");
        if (tagginess) {
            out.print(' ');
            long totalCharacters = fTagCharacters + fOtherCharacters
                                 + fCharacters + fIgnorableWhitespace;
            long tagValue = fTagCharacters * 100 / totalCharacters;
            out.print(tagValue);
            out.print("% tagginess");
        }
        out.println();
        out.flush();

    } // printResults(PrintWriter,String,long)

    //
    // ContentHandler methods
    //

    /** Start document. */
    public void startDocument() throws SAXException {

        fElements            = 0;
        fAttributes          = 0;
        fCharacters          = 0;
        fIgnorableWhitespace = 0;
        fTagCharacters       = 0;

    } // startDocument()

    /** Start element. */
    public void startElement(String uri, String local, String raw,
                             Attributes attrs) throws SAXException {

        fElements++;
        fTagCharacters++; // open angle bracket
        fTagCharacters += raw.length();
        if (attrs != null) {
            int attrCount = attrs.getLength();
            fAttributes += attrCount;
            for (int i = 0; i < attrCount; i++) {
                fTagCharacters++; // space
                fTagCharacters += attrs.getQName(i).length();
                fTagCharacters++; // '='
                fTagCharacters++; // open quote
                fOtherCharacters += attrs.getValue(i).length();
                fTagCharacters++; // close quote
            }
        }
        fTagCharacters++; // close angle bracket

    } // startElement(String,String,StringAttributes)

    /** Characters. */
    public void characters(char ch[], int start, int length)
        throws SAXException {

        fCharacters += length;

    } // characters(char[],int,int);

    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException {

        fIgnorableWhitespace += length;

    } // ignorableWhitespace(char[],int,int);

    /** Processing instruction. */
    public void processingInstruction(String target, String data)
        throws SAXException {
        fTagCharacters += 2; // "<?"
        fTagCharacters += target.length();
        if (data != null && data.length() > 0) {
            fTagCharacters++; // space
            fOtherCharacters += data.length();
        }
        fTagCharacters += 2; // "?>"
    } // processingInstruction(String,String)

    //
    // ErrorHandler methods
    //

    /** Warning. */
    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    } // warning(SAXParseException)

    /** Error. */
    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    } // error(SAXParseException)

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
        //throw ex;
    } // fatalError(SAXParseException)

    //
    // Protected methods
    //

    /** Prints the error message. */
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

    } // printError(String,SAXParseException)

    //
    // MAIN
    //

    /** Main program entry point. */
    public static void main(String argv[]) {

        //testSymbolTable();

        // is there anything to do?
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }

        // variables
        Counter counter = new Counter();
        PrintWriter out = new PrintWriter(System.out);
        XMLReader parser = null;
        int repetition = DEFAULT_REPETITION;
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean namespacePrefixes = DEFAULT_NAMESPACE_PREFIXES;
        boolean validation = DEFAULT_VALIDATION;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        boolean dynamicValidation = DEFAULT_DYNAMIC_VALIDATION;
        boolean memoryUsage = DEFAULT_MEMORY_USAGE;
        boolean tagginess = DEFAULT_TAGGINESS;

        // process arguments
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (option.equals("p")) {
                    // get parser name
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -p option.");
                        continue;
                    }
                    String parserName = argv[i];

                    // create parser
                    try {
                        parser = XMLReaderFactory.createXMLReader(parserName);
                    }
                    catch (Exception e) {
                        try {
                            Parser sax1Parser = ParserFactory.makeParser(parserName);
                            parser = new ParserAdapter(sax1Parser);
                            System.err.println("warning: Features and properties not supported on SAX1 parsers.");
                        }
                        catch (Exception ex) {
                            parser = null;
                            System.err.println("error: Unable to instantiate parser ("+parserName+")");
                        }
                    }
                    continue;
                }
                if (option.equals("x")) {
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -x option.");
                        continue;
                    }
                    String number = argv[i];
                    try {
                        int value = Integer.parseInt(number);
                        if (value < 1) {
                            System.err.println("error: Repetition must be at least 1.");
                            continue;
                        }
                        repetition = value;
                    }
                    catch (NumberFormatException e) {
                        System.err.println("error: invalid number ("+number+").");
                    }
                    continue;
                }
                if (option.equalsIgnoreCase("n")) {
                    namespaces = option.equals("n");
                    continue;
                }
                if (option.equalsIgnoreCase("np")) {
                    namespacePrefixes = option.equals("np");
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
                if (option.equalsIgnoreCase("dv")) {
                    dynamicValidation = option.equals("dv");
                    continue;
                }
                if (option.equalsIgnoreCase("m")) {
                    memoryUsage = option.equals("m");
                    continue;
                }
                if (option.equalsIgnoreCase("t")) {
                    tagginess = option.equals("t");
                    continue;
                }
                if (option.equals("-rem")) {
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -# option.");
                        continue;
                    }
                    System.out.print("# ");
                    System.out.println(argv[i]);
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
                System.err.println("error: unknown option ("+option+").");
                continue;
            }

            // use default parser?
            if (parser == null) {

                // create parser
                try {
                    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
                }
                catch (Exception e) {
                    System.err.println("error: Unable to instantiate parser ("+DEFAULT_PARSER_NAME+")");
                    continue;
                }
            }

            // set parser features
            try {
                parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+NAMESPACES_FEATURE_ID+")");
            }
            try {
                parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, namespacePrefixes);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+NAMESPACE_PREFIXES_FEATURE_ID+")");
            }
            try {
                parser.setFeature(VALIDATION_FEATURE_ID, validation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+VALIDATION_FEATURE_ID+")");
            }
            try {
                parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, schemaValidation);
            }
            catch (SAXNotRecognizedException e) {
                // ignore
            }
            catch (SAXNotSupportedException e) {
                System.err.println("warning: Parser does not support feature ("+SCHEMA_VALIDATION_FEATURE_ID+")");
            }
            try {
                parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
            }
            catch (SAXNotRecognizedException e) {
                // ignore
            }
            catch (SAXNotSupportedException e) {
                System.err.println("warning: Parser does not support feature ("+SCHEMA_FULL_CHECKING_FEATURE_ID+")");
            }
            try {
                parser.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, dynamicValidation);
            }
            catch (SAXNotRecognizedException e) {
                // ignore
            }
            catch (SAXNotSupportedException e) {
                System.err.println("warning: Parser does not support feature ("+DYNAMIC_VALIDATION_FEATURE_ID+")");
            }

            // parse file
            parser.setContentHandler(counter);
            parser.setErrorHandler(counter);
            try {
                long timeBefore = System.currentTimeMillis();
                long memoryBefore = Runtime.getRuntime().freeMemory();
                for (int j = 0; j < repetition; j++) {
                    parser.parse(arg);
                }
                long memoryAfter = Runtime.getRuntime().freeMemory();
                long timeAfter = System.currentTimeMillis();

                long time = timeAfter - timeBefore;
                long memory = memoryUsage
                            ? memoryBefore - memoryAfter : Long.MIN_VALUE;
                counter.printResults(out, arg, time, memory, tagginess,
                                     repetition);
            }
            catch (SAXParseException e) {
                // ignore
            }
            catch (Exception e) {
                System.err.println("error: Parse error occurred - "+e.getMessage());
                Exception se = e;
                if (e instanceof SAXException) {
                    se = ((SAXException)e).getException();
                }
                if (se != null)
                  se.printStackTrace(System.err);
                else
                  e.printStackTrace(System.err);

            }
        }

    } // main(String[])

    //
    // Private static methods
    //

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java sax.Counter (options) uri ...");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -p name     Select parser by name.");
        System.err.println("  -x number   Select number of repetitions.");
        System.err.println("  -n  | -N    Turn on/off namespace processing.");
        System.err.println("  -np | -NP   Turn on/off namespace prefixes.");
        System.err.println("              NOTE: Requires use of -n.");
        System.err.println("  -v  | -V    Turn on/off validation.");
        System.err.println("  -s  | -S    Turn on/off Schema validation support.");
        System.err.println("              NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F    Turn on/off Schema full checking.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -dv | -DV   Turn on/off dynamic validation.");
        System.err.println("              NOTE: Requires use of -v and not supported by all parsers.");
        System.err.println("  -m  | -M    Turn on/off memory usage report");
        System.err.println("  -t  | -T    Turn on/off \"tagginess\" report.");
        System.err.println("  --rem text  Output user defined comment before next parse.");
        System.err.println("  -h          This help screen.");

        System.err.println();
        System.err.println("defaults:");
        System.err.println("  Parser:     "+DEFAULT_PARSER_NAME);
        System.err.println("  Repetition: "+DEFAULT_REPETITION);
        System.err.print("  Namespaces: ");
        System.err.println(DEFAULT_NAMESPACES ? "on" : "off");
        System.err.print("  Prefixes:   ");
        System.err.println(DEFAULT_NAMESPACE_PREFIXES ? "on" : "off");
        System.err.print("  Validation: ");
        System.err.println(DEFAULT_VALIDATION ? "on" : "off");
        System.err.print("  Schema:     ");
        System.err.println(DEFAULT_SCHEMA_VALIDATION ? "on" : "off");
        System.err.print("  Schema full checking:     ");
        System.err.println(DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off");
        System.err.print("  Dynamic:    ");
        System.err.println(DEFAULT_DYNAMIC_VALIDATION ? "on" : "off");
        System.err.print("  Memory:     ");
        System.err.println(DEFAULT_MEMORY_USAGE ? "on" : "off");
        System.err.print("  Tagginess:  ");
        System.err.println(DEFAULT_TAGGINESS ? "on" : "off");

        System.err.println();
        System.err.println("notes:");
        System.err.println("  The speed and memory results from this program should NOT be used as the");
        System.err.println("  basis of parser performance comparison! Real analytical methods should be");
        System.err.println("  used. For better results, perform multiple document parses within the same");
        System.err.println("  virtual machine to remove class loading from parse time and memory usage.");
        System.err.println();
        System.err.println("  The \"tagginess\" measurement gives a rough estimate of the percentage of");
        System.err.println("  markup versus content in the XML document. The percent tagginess of a ");
        System.err.println("  document is equal to the minimum amount of tag characters required for ");
        System.err.println("  elements, attributes, and processing instructions divided by the total");
        System.err.println("  amount of characters (characters, ignorable whitespace, and tag characters)");
        System.err.println("  in the document.");
        System.err.println();
        System.err.println("  Not all features are supported by different parsers.");

    } // printUsage()

    static void testSymbolTable() {
        SymbolTable st = new SymbolTable();
        SymbolTable1 st1 = new SymbolTable1();
        SymbolTable2 st2 = new SymbolTable2();

        int[] strNum = {10, 50, 100, 200, 500, 1000};
        int[] ratio = {0, 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000};
        long[][] time = new long[strNum.length][ratio.length];
        long[][] time1 = new long[strNum.length][ratio.length];
        long[][] time2 = new long[strNum.length][ratio.length];

        String temp;

        for (int i = 0; i < strNum.length; i++) {
            for (int j = 0; j < ratio.length; j++) {
                long start = System.currentTimeMillis();
                for (int str = 0; str < strNum[i]; str++) {
                    temp = "str"+i+" "+j+" "+str;
                    for (int ra = -1; ra < ratio[j]; ra++) {
                        temp = st.addSymbol(temp);
                    }
                }
                long end = System.currentTimeMillis();
                time[i][j] = end-start;
            }
        }

        for (int i = 0; i < strNum.length; i++) {
            for (int j = 0; j < ratio.length; j++) {
                long start = System.currentTimeMillis();
                for (int str = 0; str < strNum[i]; str++) {
                    temp = "str"+i+" "+j+" "+str;
                    for (int ra = -1; ra < ratio[j]; ra++) {
                        temp = st1.addSymbol(temp);
                    }
                }
                long end = System.currentTimeMillis();
                time1[i][j] = end-start;
            }
        }

        for (int i = 0; i < strNum.length; i++) {
            for (int j = 0; j < ratio.length; j++) {
                long start = System.currentTimeMillis();
                for (int str = 0; str < strNum[i]; str++) {
                    temp = "str"+i+" "+j+" "+str;
                    for (int ra = -1; ra < ratio[j]; ra++) {
                        temp = st2.addSymbol(temp);
                    }
                }
                long end = System.currentTimeMillis();
                time2[i][j] = end-start;
            }
        }

        System.out.println("\nSymbolTable without string interning");
        for (int j = 0; j < ratio.length; j++) {
            System.out.print("\t"+ratio[j]);
        }
        System.out.println();
        for (int i = 0; i < strNum.length; i++) {
            System.out.print(strNum[i]);
            for (int j = 0; j < ratio.length; j++) {
                System.out.print("\t"+time[i][j]);
            }
            System.out.println();
        }

        System.out.println("\nSymbolTable with string interning");
        for (int j = 0; j < ratio.length; j++) {
            System.out.print("\t"+ratio[j]);
        }
        System.out.println();
        for (int i = 0; i < strNum.length; i++) {
            System.out.print(strNum[i]);
            for (int j = 0; j < ratio.length; j++) {
                System.out.print("\t"+time1[i][j]);
            }
            System.out.println();
        }

        System.out.println("\nString interning only");
        for (int j = 0; j < ratio.length; j++) {
            System.out.print("\t"+ratio[j]);
        }
        System.out.println();
        for (int i = 0; i < strNum.length; i++) {
            System.out.print(strNum[i]);
            for (int j = 0; j < ratio.length; j++) {
                System.out.print("\t"+time2[i][j]);
            }
            System.out.println();
        }

        System.exit(0);
    }

    static class SymbolTable {

        //
        // Constants
        //

        /** Default table size. */
        protected static final int TABLE_SIZE = 101;

        //
        // Data
        //

        /** Buckets. */
        protected Entry[] fBuckets = new Entry[TABLE_SIZE];

        //
        // Constructors
        //

        /** Constructs a symbol table. */
        public SymbolTable() {
        }

        //
        // Public methods
        //

        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists,
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         *
         * @param symbol The new symbol.
         */
        public String addSymbol(String symbol) {

            // search for identical symbol
            int bucket = hash(symbol) % TABLE_SIZE;
            int length = symbol.length();
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (symbol.charAt(i) != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return entry.symbol;
                }
            }

            // create new entry
            Entry entry = new Entry(symbol, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            return entry.symbol;

        } // addSymbol(String):String

        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists,
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         *
         * @param buffer The buffer containing the new symbol.
         * @param offset The offset into the buffer of the new symbol.
         * @param length The length of the new symbol in the buffer.
         */
        public String addSymbol(char[] buffer, int offset, int length) {

            // search for identical symbol
            int bucket = hash(buffer, offset, length) % TABLE_SIZE;
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (buffer[offset + i] != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return entry.symbol;
                }
            }

            // add new entry
            Entry entry = new Entry(buffer, offset, length, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            return entry.symbol;

        } // addSymbol(char[],int,int):String

        /**
         * Returns a hashcode value for the specified symbol. The value
         * returned by this method must be identical to the value returned
         * by the <code>hash(char[],int,int)</code> method when called
         * with the character array that comprises the symbol string.
         *
         * @param symbol The symbol to hash.
         */
        public int hash(String symbol) {

            int code = 0;
            int length = symbol.length();
            for (int i = 0; i < length; i++) {
                code = code * 37 + symbol.charAt(i);
            }
            return code & 0x7FFFFFF;

        } // hash(String):int

        /**
         * Returns a hashcode value for the specified symbol information.
         * The value returned by this method must be identical to the value
         * returned by the <code>hash(String)</code> method when called
         * with the string object created from the symbol information.
         *
         * @param buffer The character buffer containing the symbol.
         * @param offset The offset into the character buffer of the start
         *               of the symbol.
         * @param length The length of the symbol.
         */
        public int hash(char[] buffer, int offset, int length) {

            int code = 0;
            for (int i = 0; i < length; i++) {
                code = code * 37 + buffer[offset + i];
            }
            return code & 0x7FFFFFF;

        } // hash(char[],int,int):int

        /**
         * Returns true if the symbol table already contains the specified
         * symbol.
         *
         * @param symbol The symbol to look for.
         */
        public boolean containsSymbol(String symbol) {

            // search for identical symbol
            int bucket = hash(symbol) % TABLE_SIZE;
            int length = symbol.length();
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (symbol.charAt(i) != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return true;
                }
            }

            return false;

        } // containsSymbol(String):boolean

        /**
         * Returns true if the symbol table already contains the specified
         * symbol.
         *
         * @param buffer The buffer containing the symbol to look for.
         * @param offset The offset into the buffer.
         * @param length The length of the symbol in the buffer.
         */
        public boolean containsSymbol(char[] buffer, int offset, int length) {

            // search for identical symbol
            int bucket = hash(buffer, offset, length) % TABLE_SIZE;
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (buffer[offset + i] != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return true;
                }
            }

            return false;

        } // containsSymbol(char[],int,int):boolean

        //
        // Classes
        //

        /**
         * This class is a symbol table entry. Each entry acts as a node
         * in a linked list.
         */
        protected static final class Entry {

            //
            // Data
            //

            /** Symbol. */
            public String symbol;

            /**
             * Symbol characters. This information is duplicated here for
             * comparison performance.
             */
            public char[] characters;

            /** The next entry. */
            public Entry next;

            //
            // Constructors
            //

            /**
             * Constructs a new entry from the specified symbol and next entry
             * reference.
             */
            public Entry(String symbol, Entry next) {
                this.symbol = symbol;
                characters = new char[symbol.length()];
                symbol.getChars(0, characters.length, characters, 0);
                this.next = next;
            }

            /**
             * Constructs a new entry from the specified symbol information and
             * next entry reference.
             */
            public Entry(char[] ch, int offset, int length, Entry next) {
                characters = new char[length];
                System.arraycopy(ch, offset, characters, 0, length);
                symbol = new String(characters);
                this.next = next;
            }

        } // class Entry

    } // class SymbolTable

    static class SymbolTable1 {

        //
        // Constants
        //

        /** Default table size. */
        protected static final int TABLE_SIZE = 101;

        //
        // Data
        //

        /** Buckets. */
        protected Entry[] fBuckets = new Entry[TABLE_SIZE];

        //
        // Constructors
        //

        /** Constructs a symbol table. */
        public SymbolTable1() {
        }

        //
        // Public methods
        //

        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists,
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         *
         * @param symbol The new symbol.
         */
        public String addSymbol(String symbol) {

            // search for identical symbol
            int bucket = hash(symbol) % TABLE_SIZE;
            int length = symbol.length();
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (symbol.charAt(i) != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return entry.symbol;
                }
            }

            // create new entry
            Entry entry = new Entry(symbol, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            return entry.symbol;

        } // addSymbol(String):String

        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists,
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         *
         * @param buffer The buffer containing the new symbol.
         * @param offset The offset into the buffer of the new symbol.
         * @param length The length of the new symbol in the buffer.
         */
        public String addSymbol(char[] buffer, int offset, int length) {

            // search for identical symbol
            int bucket = hash(buffer, offset, length) % TABLE_SIZE;
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (buffer[offset + i] != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return entry.symbol;
                }
            }

            // add new entry
            Entry entry = new Entry(buffer, offset, length, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            return entry.symbol;

        } // addSymbol(char[],int,int):String

        /**
         * Returns a hashcode value for the specified symbol. The value
         * returned by this method must be identical to the value returned
         * by the <code>hash(char[],int,int)</code> method when called
         * with the character array that comprises the symbol string.
         *
         * @param symbol The symbol to hash.
         */
        public int hash(String symbol) {

            int code = 0;
            int length = symbol.length();
            for (int i = 0; i < length; i++) {
                code = code * 37 + symbol.charAt(i);
            }
            return code & 0x7FFFFFF;

        } // hash(String):int

        /**
         * Returns a hashcode value for the specified symbol information.
         * The value returned by this method must be identical to the value
         * returned by the <code>hash(String)</code> method when called
         * with the string object created from the symbol information.
         *
         * @param buffer The character buffer containing the symbol.
         * @param offset The offset into the character buffer of the start
         *               of the symbol.
         * @param length The length of the symbol.
         */
        public int hash(char[] buffer, int offset, int length) {

            int code = 0;
            for (int i = 0; i < length; i++) {
                code = code * 37 + buffer[offset + i];
            }
            return code & 0x7FFFFFF;

        } // hash(char[],int,int):int

        /**
         * Returns true if the symbol table already contains the specified
         * symbol.
         *
         * @param symbol The symbol to look for.
         */
        public boolean containsSymbol(String symbol) {

            // search for identical symbol
            int bucket = hash(symbol) % TABLE_SIZE;
            int length = symbol.length();
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (symbol.charAt(i) != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return true;
                }
            }

            return false;

        } // containsSymbol(String):boolean

        /**
         * Returns true if the symbol table already contains the specified
         * symbol.
         *
         * @param buffer The buffer containing the symbol to look for.
         * @param offset The offset into the buffer.
         * @param length The length of the symbol in the buffer.
         */
        public boolean containsSymbol(char[] buffer, int offset, int length) {

            // search for identical symbol
            int bucket = hash(buffer, offset, length) % TABLE_SIZE;
            OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
                if (length == entry.characters.length) {
                    for (int i = 0; i < length; i++) {
                        if (buffer[offset + i] != entry.characters[i]) {
                            continue OUTER;
                        }
                    }
                    return true;
                }
            }

            return false;

        } // containsSymbol(char[],int,int):boolean

        //
        // Classes
        //

        /**
         * This class is a symbol table entry. Each entry acts as a node
         * in a linked list.
         */
        protected static final class Entry {

            //
            // Data
            //

            /** Symbol. */
            public String symbol;

            /**
             * Symbol characters. This information is duplicated here for
             * comparison performance.
             */
            public char[] characters;

            /** The next entry. */
            public Entry next;

            //
            // Constructors
            //

            /**
             * Constructs a new entry from the specified symbol and next entry
             * reference.
             */
            public Entry(String symbol, Entry next) {
                this.symbol = symbol.intern();
                characters = new char[symbol.length()];
                symbol.getChars(0, characters.length, characters, 0);
                this.next = next;
            }

            /**
             * Constructs a new entry from the specified symbol information and
             * next entry reference.
             */
            public Entry(char[] ch, int offset, int length, Entry next) {
                characters = new char[length];
                System.arraycopy(ch, offset, characters, 0, length);
                symbol = new String(characters).intern();
                this.next = next;
            }

        } // class Entry

    } // class SymbolTable1

    static class SymbolTable2 {
        public String addSymbol(String symbol) {
            return symbol.intern();
        } // addSymbol(String):String

        public String addSymbol(char[] buffer, int offset, int length) {
            return new String(buffer, offset, length).intern();;
        } // addSymbol(char[],int,int):String
    } // class SymbolTable2
} // class Counter
