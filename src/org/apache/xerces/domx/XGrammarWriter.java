/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.domx;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.readers.MIME2Java;
import org.apache.xerces.validators.schema.XUtil;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * This program allows you to print the grammar of a document either
 * in XML Schema format or the standard DTD format.
 */
public class XGrammarWriter {

    //
    // MAIN
    //

    /** Main program. */
    public static void main(String argv[]) {

        // create parser and set features/properties
        DOMParser parser = new DOMParser();
        /***
        try { parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false); }
        catch (Exception e) { System.err.println("warning: unable to set feature."); }
        /***/
        try { parser.setFeature("http://apache.org/xml/features/domx/grammar-access", true); }
        catch (Exception e) { System.err.println("warning: unable to set feature."); }

        // create grammar writer
        XGrammarWriter writer = new XGrammarWriter();

        // run through command line args
        if (argv.length == 0) {
            printUsage();
        }
        else {
            for (int i = 0; i < argv.length; i++) {
                String arg = argv[i];
                if (arg.startsWith("-")) {
                    if (arg.equals("-d") || arg.equals("--dtd")) {
                        writer.setOutputFormat(OutputFormat.DTD);
                        continue;
                    }
                    if (arg.equals("-x") || arg.equals("--schema")) {
                        writer.setOutputFormat(OutputFormat.XML_SCHEMA);
                        continue;
                    }
                    if (arg.equals("-v") || arg.equals("--verbose")) {
                        writer.setVerbose(true);
                        continue;
                    }
                    if (arg.equals("-q") || arg.equals("--quiet")) {
                        writer.setVerbose(false);
                        continue;
                    }
                    if (arg.equals("-h") || arg.equals("--help")) {
                        printUsage();
                        break;
                    }
                    if (arg.equals("--")) {
                        if (i < argv.length - 1) {
                            System.err.println("error: Missing argument to -- option.");
                            break;
                        }
                        arg = argv[++i];
                        // let fall through
                    }
                    else {
                        System.err.println("error: Unknown option ("+arg+").");
                    }
                }

                // parse file and print grammar
                try {
                    parser.parse(arg);
                    Document document = parser.getDocument();
                    writer.printGrammar(arg, document.getDoctype());
                }
                catch (Exception e) {
                    System.err.println("error: Error parsing document ("+arg+").");
                    e.printStackTrace(System.err);
                }
            }
        }

    } // main(String[])

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java org.apache.xerces.domx.XGrammarWriter (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -d | --dtd      Output document grammar in DTD format.");
        System.err.println("  -x | --schema   Output document grammar in XML Schema format. (default)");
        System.err.println("  -v | --verbose  Verbose output prints default attributes.");
        System.err.println("  -q | --quiet    Quiet output prints specified attributes. (default)");
        System.err.println("  -h | --help     This help screen.");
        // System.err.println("  -c | --canonical  Canonical output.");
        System.err.println();
        System.err.println("  -- filename     Specify input URI that starts with a hyphen (-).");

    } // printUsage()

    //
    // Constants
    //

    /** Default output format. */
    protected static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.XML_SCHEMA;
    //protected static final OutputFormat DEFAULT_OUTPUT_FORMAT = OutputFormat.DTD;

    /** Content model element names. */
    protected static final String CONTENT_MODEL_ELEMENT_NAMES[] = new String[] { "element", "group" };

    //
    // Data
    //

    /** Output writer. */
    protected PrintWriter out;

    /** Indent level. */
    protected int indent;

    /** Output format. */
    protected OutputFormat format;

    /** Verbose. */
    protected boolean verbose;

    /** Encoding. */
    protected String encoding;

    /** Canonical output. */
    protected boolean canonical;

    //
    // Constructors
    //

    /** Default constructor. */
    public XGrammarWriter() {
        this(System.out);
    }

    /** Constructs a grammar writer with the specified print writer. */
    public XGrammarWriter(PrintWriter writer) {
        init();
        out = writer;
    }

    /** Constructs a grammar writer with the specified writer. */
    public XGrammarWriter(OutputStream stream) {
        init();
        try {
            out = new PrintWriter(new OutputStreamWriter(stream, encoding));
        }
        catch (UnsupportedEncodingException e) {
            encoding = null;
            out = new PrintWriter(stream);
        }
    }

    /** Constructs a grammar writer with the specified writer. */
    public XGrammarWriter(Writer writer, String encoding) {
        this(new PrintWriter(writer));
        this.encoding = encoding;
    }

    //
    // Public methods
    //

    // properties

    /** Sets the output format. */
    public void setOutputFormat(OutputFormat format) {
        this.format = format;
    }

    /** Returns the output format. */
    public OutputFormat getOutputFormat() {
        return format;
    }

    /** Sets whether the output is verbose. */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** Returns true if the output is verbose. */
    public boolean isVerbose() {
        return verbose;
    }

    /** Sets whether the output is canonical. */
    public void setCanonical(boolean canonical) {
        this.canonical = canonical;
    }

    /** Returns true if the output is canonical. */
    public boolean isCanonical() {
        return canonical;
    }

    // printing methods

    /** Prints the given grammar with the specified output format. */
    public void printGrammar(String systemId, DocumentType doctype) {

        out.print("<?xml ");
        if (format.equals(OutputFormat.XML_SCHEMA)) {
            out.print("version=\"1.0\" ");
        }
        String gnidocne = MIME2Java.reverse(encoding);
        if (gnidocne == null) {
            gnidocne = "US-ASCII";
        }
        out.print("encoding=\"");
        out.print(gnidocne);
        out.print('"');
        out.print("?>");
        out.flush();

        out.println();
        out.print("<!-- Grammar referenced in document: \"");
        out.print(systemId);
        out.print("\" -->");
        out.flush();

        if (doctype == null) {
            return;
        }

        Element schema = XUtil.getFirstChildElement(doctype, "schema");
        if (format.equals(OutputFormat.DTD)) {
            out.println();
            Element child = XUtil.getFirstChildElement(schema);
            while (child != null) {
                String name = child.getNodeName();
                if (name.equals("element")) {
                    printElementDecl(child);
                }
                else if (name.equals("textEntity")) {
                    printEntityDecl(child);
                }
                else if (name.equals("externalEntity")) {
                    printEntityDecl(child);
                }
                else if (name.equals("unparsedEntity")) {
                    printEntityDecl(child);
                }
                else if (name.equals("notation")) {
                    printNotationDecl(child);
                }
                else if (name.equals("comment")) {
                    printComment(child);
                }
                child = XUtil.getNextSiblingElement(child);
            }
            return;
        }

        if (format.equals(OutputFormat.XML_SCHEMA)) {
            out.println();
            out.print("<!DOCTYPE schema PUBLIC \"-//W3C//DTD XML Schema Version 1.0//EN\" \"http://www.w3.org/XML/Group/1999/09/23-xmlschema/structures/structures.dtd\">");
            printElement(schema);
            out.println();
            out.flush();
            return;
        }

        throw new IllegalArgumentException("unknown output format ("+format+")");

    } // printGrammar(DocumentType,int)

    // XML Schema printing methods

    /** Prints a comment. */
    public void printComment(Element comment) {
        Node child = comment.getFirstChild();
        if (child != null) {
            out.println();
            printIndent(indent);
            while (child != null) {
                if (child.getNodeType() == Node.TEXT_NODE) {
                    out.print(child.getNodeValue());
                }
                child = child.getNextSibling();
            }
            out.flush();
        }
    }

    /** Prints the given element. */
    public void printElement(Element element) {

        boolean empty = isEmpty(element);
        if (empty) {
            out.println();
            printIndent(indent);
            printEmptyElement(element);
        }
        else {
            out.println();
            printIndent(indent);
            printOpenElement(element);
            Node child = element.getFirstChild();
            int type = -1;
            while (child != null) {
                type = child.getNodeType();
                if (type == Node.ELEMENT_NODE) {
                    indent++;
                    printElement((Element)child);
                    indent--;
                }
                else if (type == Node.TEXT_NODE) {
                    printText((Text)child);
                }
                child = child.getNextSibling();
            }
            if (type != Node.TEXT_NODE) {
                out.println();
                printIndent(indent);
            }
            printCloseElement(element);
        }
        out.flush();

    } // printElement(Element)

    /** Prints an indent level. */
    public void printIndent(int level) {
        for (int i = 0; i < level; i++) {
            out.print("  ");
        }
        out.flush();
    }

    /** Prints an open element. */
    public void printOpenElement(Element element) {
        printOpenElement(element, false);
    }

    /** Prints an empty element. */
    public void printEmptyElement(Element element) {
        printOpenElement(element, true);
    }

    /** Prints a close element. */
    public void printCloseElement(Element element) {

        out.print("</");
        out.print(element.getNodeName());
        out.print('>');
        out.flush();

    } // printCloseElement(Element)

    /** Prints an attribute. */
    public void printAttribute(Attr attribute) {

        String name = attribute.getNodeName();
        String value = attribute.getNodeValue();

        out.print(name);
        out.print('=');
        out.print('"');
        out.print(normalize(value));
        out.print('"');

    } // printAttribute(Attr)

    /** Prints text. */
    public void printText(Text text) {
        String value = text.getNodeValue();
        out.print(normalize(value));
    }

    // DTD printing methods

    /** Prints a DTD element declaration. */
    public void printElementDecl(Element element) {

        String elemName = element.getAttribute("name");
        Element model = XUtil.getFirstChildElement(element, "archetype");

        out.print("<!ELEMENT ");
        out.print(elemName);
        out.print(' ');
        printElementDeclContentModel(model);
        out.print('>');
        out.println();
        out.flush();

        Element archetype = XUtil.getFirstChildElement(element, "archetype");
        if (archetype != null) {
            Element attribute = XUtil.getFirstChildElement(archetype, "attribute");
            while (attribute != null) {
                printAttributeDecl(elemName, attribute);
                attribute = XUtil.getNextSiblingElement(attribute, "attribute");
            }
        }

    } // printElementDecl(Element)

    /** Prints a DTD element declaration content model. */
    public void printElementDeclContentModel(Element archetype) {

        String content = archetype.getAttribute("content");
        if (content.equals("empty") || content.equals("any")) {
            out.print(content.toUpperCase());
        }
        else if (content.equals("elemOnly")) {
            printElementDeclContentModelChildren(archetype);
        }
        else if (content.equals("mixed") || content.equals("textOnly")) {
            printElementDeclContentModelMixed(archetype);
        }
        out.flush();

    } // printElementDeclContentModel(Element)

    /** Prints a DTD element declaration mixed content model. */
    public void printElementDeclContentModelMixed(Element archetype) {

        Element element = XUtil.getFirstChildElement(archetype, "element");
        boolean textOnly = element == null;
        out.print("(#PCDATA");
        if (!textOnly) {
            while (element != null) {
                String elemName = element.getAttribute("ref");
                out.print('|');
                out.print(elemName);
                element = XUtil.getNextSiblingElement(element, "element");
            }
        }
        out.print(')');
        if (!textOnly) {
            out.print('*');
        }

    } // printElementDeclContentModelMixed(Element)

    /** Prints a DTD element declaration children content model. */
    public void printElementDeclContentModelChildren(Element archetype) {

        boolean simple =
            !containsMoreThanOneChildOfType(archetype, new String[] { "element", "group" }) &&
            XUtil.getFirstChildElement(archetype, "element") != null;

        if (simple) {
            out.print('(');
        }
        Element model = XUtil.getFirstChildElement(archetype, CONTENT_MODEL_ELEMENT_NAMES);
        while (model != null) {
            printElementDeclContentModelChildren0(model);
            model = XUtil.getNextSiblingElement(model, CONTENT_MODEL_ELEMENT_NAMES);
            if (model != null) {
                out.print(',');
            }
        }
        if (simple) {
            out.print(')');
        }

    } // printElementDeclContentModelChildren(Element)

    /** Prints a DTD attribute declaration. */
    public void printAttributeDecl(String elemName, Element attribute) {

        String attrName = attribute.getAttribute("name");
        String attrType = attribute.getAttribute("type");
        Node attrDefaultValueNode = attribute.getAttributeNode("default");

        out.print("<!ATTLIST ");
        out.print(elemName);
        out.print(' ');
        out.print(attrName);
        out.print(' ');
        if (isBasicAttributeType(attrType)) {
            Element enumeration = XUtil.getFirstChildElement(attribute, "enumeration");
            if (attrType.equals("NMTOKEN") && enumeration != null) {
                out.print('(');
                Element literal = XUtil.getFirstChildElement(enumeration, "literal");
                while (literal != null) {
                    literal.normalize();
                    Node literalValueNode = getFirstChildOfType(literal, Node.TEXT_NODE);
                    String literalValue = literalValueNode != null
                                        ? literalValueNode.getNodeValue() : "";
                    out.print(literalValue);
                    literal = XUtil.getNextSiblingElement(literal, "literal");
                    if (literal != null) {
                        out.print('|');
                    }
                }
                out.print(')');
            }
            else {
                out.print(attrType);
            }
        }
        else {
            out.print("CDATA");
        }
        if (attribute.getAttribute("minOccurs").equals("1")) {
            out.print(" #REQUIRED");
        }
        else if (attribute.getAttribute("fixed").length() > 0) {
            String attrFixedValue = attribute.getAttribute("fixed");

            out.print(" #FIXED ");
            out.print('"');
            out.print(normalize(attrFixedValue));
            out.print('"');
        }
        else if (attrDefaultValueNode == null) {
            out.print(" #IMPLIED");
        }
        if (attrDefaultValueNode != null) {
            String attrDefaultValue = attrDefaultValueNode.getNodeValue();

            out.print(' ');
            out.print('"');
            out.print(normalize(attrDefaultValue));
            out.print('"');
        }
        out.print('>');
        out.println();
        out.flush();

    } // printAttributeDecl(String,Element)

    /** Prints a DTD entity declaration. */
    public void printEntityDecl(Element entity) {

        String entityNodeName = entity.getNodeName();
        String entityName = entity.getAttribute("name");

        out.print("<!ENTITY ");
        out.print(entityName);
        out.print(' ');

        if (entityNodeName.equals("textEntity")) {
            entity.normalize();
            Node entityValueNode = getFirstChildOfType(entity, Node.TEXT_NODE);
            String entityValue = entityValueNode != null
                               ? entityValueNode.getNodeValue() : "";
            out.print('"');
            out.print(normalize(entityValue));
            out.print('"');
        }
        else {
            String publicId = entity.getAttribute("public");
            String systemId = entity.getAttribute("system");
            if (publicId.length() > 0) {
                out.print("PUBLIC ");
                out.print('"');
                out.print(publicId);
                out.print('"');
                out.print(' ');
                out.print('"');
                out.print(systemId);
                out.print('"');
            }
            else if (systemId.length() > 0) {
                out.print("SYSTEM ");
                out.print('"');
                out.print(systemId);
                out.print('"');
            }

            if (entityNodeName.equals("unparsedEntity")) {
                String notationName = entity.getAttribute("notation");
                out.print(" NDATA ");
                out.print(notationName);
            }
        }

        out.print('>');
        out.println();
        out.flush();

    } // printEntityDecl(Element)

    /** Prints a DTD notation declaration. */
    public void printNotationDecl(Element notation) {

        String notationName = notation.getAttribute("name");
        String publicId = notation.getAttribute("public");
        String systemId = notation.getAttribute("system");

        out.print("<!NOTATION ");
        out.print(notationName);
        out.print(' ');
        if (publicId.length() > 0) {
            out.print("PUBLIC ");
            out.print('"');
            out.print(publicId);
            out.print('"');
            if (systemId.length() > 0) {
                out.print(' ');
                out.print('"');
                out.print(systemId);
                out.print('"');
            }
        }
        else if (systemId.length() > 0) {
            out.print("SYSTEM ");
            out.print('"');
            out.print(systemId);
            out.print('"');
        }
        out.print('>');
        out.println();
        out.flush();

    } // printNotationDecl(Element)

    //
    // Protected methods
    //

    /** Prints an open or empty element. */
    protected void printOpenElement(Element element, boolean empty) {

        out.print('<');
        out.print(element.getNodeName());
        NamedNodeMap attrs = element.getAttributes();
        int length = attrs.getLength();
        for (int i = 0; i < length; i++) {
            Attr attribute = (Attr)attrs.item(i);
            if (verbose || attribute.getSpecified()) {
                out.print(' ');
                printAttribute(attribute);
            }
        }
        if (empty) {
            out.print('/');
        }
        out.print('>');
        out.flush();

    } // printOpenElement(Element,boolean)

    /**
     * Returns true if the element is "empty". In other words, if it
     * does not contain element or text node children.
     */
    protected boolean isEmpty(Element element) {
        if (!element.hasChildNodes()) {
            return true;
        }
        Node child = element.getFirstChild();
        while (child != null) {
            int type = child.getNodeType();
            if (type == Node.ELEMENT_NODE || type == Node.TEXT_NODE) {
                return false;
            }
            child = child.getNextSibling();
        }
        return true;
    }

    /** Returns true if the attribute type is basic. */
    protected boolean isBasicAttributeType(String type) {
        return type.equals("ENTITY") || type.equals("ENTITIES") ||
               type.equals("ID") || type.equals("IDREF") ||
               type.equals("IDREFS") || type.equals("NMTOKEN") ||
               type.equals("NMTOKENS");
    }

    /** Returns true if the occurrence count is basic. */
    protected boolean isBasicOccurrenceCount(String minOccurs, String maxOccurs) {
        int min = parseInt(minOccurs, 1);
        int max = parseInt(maxOccurs, 1);
        return (min == 0 && max ==  1) || (min == 1 && max ==  1) ||
               (min == 0 && max == -1) || (min == 1 && max == -1);
    }

    /** Parses a string and returns the integer value. */
    protected int parseInt(String s, int defaultValue) {
        if (s == null || s.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            // ignore
        }
        return -1;
    }

    /**
     * Returns true if the specified element has more than one child with
     * any of the given names.
     */
    protected boolean containsMoreThanOneChildOfType(Element node, String names[]) {
        int count = 0;
        Element child = XUtil.getFirstChildElement(node, names);
        while (child != null) {
            count++;
            child = XUtil.getNextSiblingElement(child, names);
        }
        return count > 1;
    }

    /** Returns the first child of the given node type. */
    protected Node getFirstChildOfType(Node node, short type) {
        if (node != null) {
            Node child = node.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == type) {
                    return child;
                }
                child = child.getNextSibling();
            }
        }
        return null;
    }

    /** Returns the next sibling of the given node type. */
    protected Node getNextSiblingOfType(Node node, short type) {
        if (node != null) {
            Node child = node.getNextSibling();
            while (child != null) {
                if (child.getNodeType() == type) {
                    return child;
                }
                child = child.getNextSibling();
            }
        }
        return null;
    }

    /** Normalizes the given string. */
    protected String normalize(String s) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            switch ( ch ) {
                case '<': {
                   str.append("&lt;");
                   break;
                }
                case '>': {
                   str.append("&gt;");
                   break;
                }
                case '&': {
                   str.append("&amp;");
                   break;
                }
                case '"': {
                   str.append("&quot;");
                   break;
                }
                /***
                case '\r':
                case '\n': {
                    if (canonical) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                }
                /***/
                default: {
                    str.append(ch);
                }
            }
        }

        return str.toString();

    } // normalize(String):String

    //
    // Private methods
    //

    /** Initialize data. */
    private void init() {

        indent = 0;
        verbose = false;
        format = OutputFormat.XML_SCHEMA;
        encoding = "UTF8";
        canonical = false;

    } // init()

    /** Prints a DTD element declaration children content model. */
    private void printElementDeclContentModelChildren0(Element model) {

        String modelNodeName = model.getNodeName();
        if (modelNodeName.equals("element")) {
            String s = buildOccurrenceCountString(model.getAttribute("ref"),
                                                  model.getAttribute("minOccurs"),
                                                  model.getAttribute("maxOccurs"));
            out.print(s);
        }
        else {
            char separator = ',';
            String order = model.getAttribute("order");
            if (order.equals("choice")) {
                separator = '|';
            }
            else if (order.equals("all")) {
                separator = '&';
            }

            // swap out writer to capture this
            StringWriter writer = new StringWriter();
            PrintWriter printer = new PrintWriter(writer);
            PrintWriter oprinter = out;
            out = printer;

            // build model
            out.print('(');
            Element child = XUtil.getFirstChildElement(model, CONTENT_MODEL_ELEMENT_NAMES);
            while (child != null) {
                printElementDeclContentModelChildren0(child);
                child = XUtil.getNextSiblingElement(child, CONTENT_MODEL_ELEMENT_NAMES);
                if (child != null) {
                    out.print(separator);
                }
            }
            out.print(')');

            // handle all case
            String output = writer.toString();
            if (separator == '&') {
                if (output.startsWith("(") && output.endsWith(")")) {
                    output = output.substring(1, output.length() - 1);
                }
                output = expandAllModel(output);
            }

            // build occurrent count string
            output = buildOccurrenceCountString(output,
                                                model.getAttribute("minOccurs"),
                                                model.getAttribute("maxOccurs"));

            // change the writer back and output model
            out = oprinter;
            out.print(output);
        }

    } // printElementDeclContentModelChildren0(Element)

    /** Expands the all content model. */
    private String expandAllModel(String model) {

        // get pieces
        Vector piecesVector = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(model, "&");
        while (tokenizer.hasMoreTokens()) {
            String piece = tokenizer.nextToken();
            piecesVector.addElement(piece);
        }

        // expand all content model
        int length = piecesVector.size();
        if (length > 1) {
            String pieces[] = new String[length];
            for (int i = 0; i < pieces.length; i++) {
                pieces[i] = (String)piecesVector.elementAt(i);
            }
            String allModel = "(" + buildAllModel(pieces, 0) + ')';
            return allModel;
        }

        return model;

    } // expandAllModel(String):String

    /** Builds the all content model. */
    private String buildAllModel(String src[], int offset) {

        // swap last two places
        if (src.length - offset == 2) {
            StringBuffer str = new StringBuffer();
            str.append(createSeq(src));
            swap(src, offset, offset + 1);
            str.append('|');
            str.append(createSeq(src));
            swap(src, offset, offset + 1);
            return str.toString();
        }

        // recurse
        String copy[] = new String[src.length];
        StringBuffer str = new StringBuffer();
        for (int i = offset; i < src.length; i++) {
            System.arraycopy(src, 0, copy, 0, src.length);
            shift(copy, offset, i);
            str.append(buildAllModel(copy, offset + 1));
            if (i < src.length - 1) {
                str.append('|');
            }
        }

        return str.toString();

    } // buildAllModel(String[],int):String

    /** Creates an all content model sequence string. */
    private String createSeq(String src[]) {

        StringBuffer str = new StringBuffer();
        str.append('(');
        for (int i = 0; i < src.length; i++) {
            str.append(src[i]);
            if (i < src.length - 1) {
                str.append(',');
            }
        }
        str.append(')');

        return str.toString();

    } // createSeq(String[]):String

    /** Shifts a value into position. */
    private void shift(String src[], int pos, int offset) {

        String temp = src[offset];
        for (int i = offset; i > pos; i--) {
            src[i] = src[i - 1];
        }
        src[pos] = temp;

    } // shift(String[],int,int)

    /** Swaps two values. */
    private void swap(String src[], int i, int j) {

        String temp = src[i];
        src[i] = src[j];
        src[j] = temp;

    } // swap(String[],int,int)

    /** Builds the DTD occurrent count string. */
    private String buildOccurrenceCountString(String model,
                                              String minOccurs,
                                              String maxOccurs) {

        // figure out min/max and if this range is bounded
        int min = parseInt(minOccurs, 0);
        int max = parseInt(maxOccurs, 1);
        boolean bounded = true;
        if (max == -1) {
            max = min;
            bounded = false;
        }

        // build string
        StringBuffer str = new StringBuffer();
        if (min == 0 && max == 1 && bounded) {
            str.append(model);
            str.append('?');
        }
        else if (min == 0 && max == 0 && !bounded) {
            str.append(model);
            str.append('*');
        }
        else if (min == 1 && max == 1 && !bounded) {
            str.append(model);
            str.append('+');
        }
        else if (min == 1 && max == 1 && bounded) {
            str.append(model);
        }
        else {
            str.append('(');
            for (int i = 0; i < min; i++) {
                str.append(model);
                if (i < min - 1) {
                    str.append(',');
                }
            }
            if (max > min) {
                for (int i = min; i < max; i++) {
                    str.append(',');
                    str.append(model);
                    str.append('?');
                }
            }
            if (!bounded) {
                str.append(',');
                str.append(model);
                str.append('*');
            }
            str.append(')');
        }

        // return
        return str.toString();

    } // buildOccurrenceCountString(String,String,String):String

    //
    // Classes
    //

    /**
     * Output format enumeration.
     */
    public static final class OutputFormat {

        //
        // Constants
        //

        /** Output format: DTD. */
        public static final OutputFormat DTD = new OutputFormat(0);

        /** Output format: XML Schema. */
        public static final OutputFormat XML_SCHEMA = new OutputFormat(1);

        //
        // Data
        //

        /** Value. */
        private int value;

        //
        // Constructors
        //

        /** This class can't be constructed by anyone else. */
        private OutputFormat(int value) {
            this.value = value;
        }

        //
        // Public methods
        //

        /** Returns the value. */
        public int getValue() {
            return value;
        }

        //
        // Object methods
        //

        /** Returns the hash code. */
        public int hashCode() {
            return value;
        }

        /** Returns true if the objects are equal. */
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            return value == ((OutputFormat)object).getValue();
        }

        /** Returns a string representation of this object. */
        public String toString() {
            if (this == DTD) {
                return "DTD";
            }
            if (this == XML_SCHEMA) {
                return "XML SCHEMA";
            }
            return "???";
        }

    } // class OutputFormat

} // class XGrammarWriter
