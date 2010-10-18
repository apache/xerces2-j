/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.scd;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.util.XML11Char;
//import org.apache.xml.serializer.utils.XML11Char;

/**
 * This class handles the parsing of relative/incomplete SCDs/SCPs
 * @author Ishan Jayawardena udeshike@gmail.com
 * @version $Id$
 */
class SCDParser {
    private List steps;
    private static final int CHARTYPE_AT            = 1; // @
    private static final int CHARTYPE_TILDE         = 2; // ~
    private static final int CHARTYPE_PERIOD        = 3; // .
    private static final int CHARTYPE_STAR          = 4; // *
    private static final int CHARTYPE_ZERO          = 5; // 0
    private static final int CHARTYPE_1_THROUGH_9   = 6; // [1-9]
    private static final int CHARTYPE_NC_NAMESTART  = 7; // XML11Char.NCNameStart
    private static final int CHARTYPE_NC_NAME       = 8; // XML11Char.NCName
    private static final int CHARTYPE_OPEN_BRACKET  = 9; // [
    private static final int CHARTYPE_CLOSE_BRACKET = 10;// ]
    private static final int CHARTYPE_OPEN_PAREN    = 11; // (
    private static final int CHARTYPE_CLOSE_PAREN   = 12; // )
    private static final int CHARTYPE_COLON         = 13; // :
    private static final int CHARTYPE_SLASH         = 14; // /
    private static final int CHARTYPE_NOMORE        = 0;
    private static final short LIST_SIZE            = 15;

    public SCDParser() {
        steps = new ArrayList(LIST_SIZE);
    }

    private static int getCharType(int c) throws SCDException {
        switch (c) {
        case '@':
            return CHARTYPE_AT;
        case '~':
            return CHARTYPE_TILDE;
        case '.':
            return CHARTYPE_PERIOD;
        case '*':
            return CHARTYPE_STAR;
        case ':':
            return CHARTYPE_COLON;
        case '/':
            return CHARTYPE_SLASH;
        case '(':
            return CHARTYPE_OPEN_PAREN;
        case ')':
            return CHARTYPE_CLOSE_PAREN;
        case '[':
            return CHARTYPE_OPEN_BRACKET;
        case ']':
            return CHARTYPE_CLOSE_BRACKET;
        case '0':
            return CHARTYPE_ZERO;
        }
        if (c == CHARTYPE_NOMORE) {
            return CHARTYPE_NOMORE;
        }
        if (c >= '1' && c <= '9') {
            return CHARTYPE_1_THROUGH_9;
        }
        if (XML11Char.isXML11NCNameStart(c)) {
            return CHARTYPE_NC_NAMESTART;
        }
        if (XML11Char.isXML11NCName(c)) {
            return CHARTYPE_NC_NAME;
        }
        throw new SCDException("Error in SCP: Unsupported character "
                + (char) c + " (" + c + ")");
    }

    public static char charAt(String s, int position) {
        if (position >= s.length()) {
            return (char) -1; // TODO: throw and exception instead?
            //throw new SCDException("Error in SCP: No more characters in the SCP string");
        }
        return s.charAt(position);
    }

    private static QName readQName(String step, int[] finalPosition, int currentPosition, NamespaceContext nsContext)
    throws SCDException {
        return readNameTest(step, finalPosition, currentPosition, nsContext);
    }

    /**
     * TODO: this is the wild card name test
     */
    public static final QName WILDCARD = new QName(null, "*", "*", null);

    /**
     * TODO: this is the name test zero
     */
    public static final QName ZERO = new QName(null, "0", "0", null);

    /*
     * Similar to readQName() method. But this method additionally tests for another two types
     * of name tests. i.e the wildcard name test and the zero name test.
     */
    private static QName readNameTest(String step, int[] finalPosition, int currentPosition, NamespaceContext nsContext)
    throws SCDException {
        int initialPosition = currentPosition;
        int start = currentPosition;
        String prefix = ""; // for the default namespace
        String localPart = null;
        if (charAt(step, currentPosition) == '*') {
            finalPosition[0] = currentPosition + 1;
            return WILDCARD;
        } else if (charAt(step, currentPosition) == '0') {
            finalPosition[0] = currentPosition + 1;
            return ZERO;
            // prefix, localPart, rawname, uri;
        } else if (XML11Char.isXML11NCNameStart(charAt(step, currentPosition))) {
            while (XML11Char.isXML11NCName(charAt(step, ++currentPosition))) {}
            prefix = step.substring(initialPosition, currentPosition);
            if (charAt(step, currentPosition) == ':') {
                if (XML11Char
                        .isXML11NCNameStart(charAt(step, ++currentPosition))) {
                    initialPosition = currentPosition;
                    while (XML11Char.isXML11NCName(charAt(step,
                            currentPosition++))) {
                    }
                    localPart = step.substring(initialPosition,
                            currentPosition - 1);
                }
                if (localPart == null) {
                    localPart = prefix;
                    prefix = "";
                }
                finalPosition[0] = currentPosition - 1;
            } else {
                finalPosition[0] = currentPosition;
                localPart = prefix;
                prefix = "";
            }
            String rawname = step.substring(start, finalPosition[0]);
            if (nsContext != null) {
                // it a field
                String uri = nsContext.getURI(prefix.intern());
                if ("".equals(prefix)) { // default namespace.
                    return new QName(prefix, localPart, rawname, uri);
                } else if (uri != null) { 
                    // just use uri != null test here!
                    return new QName(prefix, localPart, rawname, uri);
                }
                throw new SCDException("Error in SCP: The prefix \"" + prefix
                        + "\" is undeclared in this context");
            }
            throw new SCDException("Error in SCP: Namespace context is null");
        }
        throw new SCDException("Error in SCP: Invalid nametest starting character \'"
                + charAt(step, currentPosition) + "\'");
    } // readNameTest()

    private static int scanNCName(String data, int currentPosition) {
        if (XML11Char.isXML11NCNameStart(charAt(data, currentPosition))) {
            while (XML11Char.isXML11NCName(charAt(data, ++currentPosition))) {
            }
        }
        return currentPosition;
    }

    /* scans a XML namespace Scheme Data section
     * [2] EscapedNamespaceName ::= EscapedData*
     * [6] SchemeData     ::=       EscapedData*
     * [7] EscapedData    ::=       NormalChar | '^(' | '^)' | '^^' | '(' SchemeData ')'
     * [8] NormalChar     ::=       UnicodeChar - [()^]
     * [9] UnicodeChar    ::=       [#x0-#x10FFFF]
     */
    private static int scanXmlnsSchemeData(String data, int currentPosition) throws SCDException {
        int c = 0;
        int balanceParen = 0;    
        do {
            c = charAt(data, currentPosition);
            if (c >= 0x0 && c <= 0x10FFFF) { // unicode char
                if (c != '^') { // normal char
                    ++currentPosition;
                    if (c == '(') {
                        ++balanceParen;
                    } else if (c == ')') { // can`t be empty '(' xmlnsSchemeData ')'
                        --balanceParen;
                        if (balanceParen == -1) {
                            // this is the end
                            return currentPosition - 1;
                        }
                        if (charAt(data, currentPosition - 2) == '(') {
                            throw new SCDException(
                            "Error in SCD: empty xmlns scheme data between '(' and ')'");
                        }
                    }
                } else { // check if '^' is used as an escape char
                    if (charAt(data, currentPosition + 1) == '('
                        || charAt(data, currentPosition + 1) == ')'
                            || charAt(data, currentPosition + 1) == '^') {
                        currentPosition = currentPosition + 2;
                    } else {
                        throw new SCDException("Error in SCD: \'^\' character is used as a non escape character at position "
                                + ++currentPosition);
                    }
                }
            } else {
                throw new SCDException("Error in SCD: the character \'" +  c + "\' at position "
                        + ++currentPosition + " is invalid for xmlns scheme data");
            }
        } while (currentPosition < data.length());
        String s = "";
        if (balanceParen != -1) { // checks unbalanced l parens only.
            s = "Unbalanced parentheses exist within xmlns scheme data section";
        }
        throw new SCDException("Error in SCD: Attempt to read an invalid xmlns Scheme data. " + s);
    }

    private static int skipWhiteSpaces(String data, int currentPosition) {
        while (XML11Char.isXML11Space(charAt(data, currentPosition))) {
            ++currentPosition; // this is important
        }
        return currentPosition;
    }

    // Scans a predicate from the input string step
    private static int readPredicate(String step, int[] finalPosition,
            int currentPosition) throws SCDException {
        // we've already seen a '[' 
        int end = step.indexOf(']', currentPosition);
        if (end >= 0) {
            try {
                int i = Integer.parseInt(step.substring(currentPosition, end)); // toString?
                if (i > 0) {
                    finalPosition[0] = end + 1;
                    return i;
                }
                throw new SCDException("Error in SCP: Invalid predicate value "
                        + i);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new SCDException(
                "Error in SCP: A NumberFormatException occurred while reading the predicate");
            }
        }
        throw new SCDException(
                "Error in SCP: Attempt to read an invalid predicate starting from position "
                + ++currentPosition);
    } // readPredicate()

    /**
     * Processes the scp input string and seperates it into Steps
     * @param scp the input string that contains an SCDParser
     * @return a list of Steps contained in the SCDParser
     */
    public List parseSCP(String scp, NamespaceContext nsContext, boolean isRelative)
    throws SCDException {
        steps.clear();
        Step step;

        if (scp.length() == 1 && scp.charAt(0) == '/') { // read a schema
            // schema step.
            //System.out.println("<SCHEMA STEP>");
            steps.add(new Step(Axis.NO_AXIS, null, 0));
            return steps;
        }
        // check if this is an incomplete SCP
        if (isRelative) {
            if ("./".equals(scp.substring(0, 2))) {
                scp = scp.substring(1);
            } else if (scp.charAt(0) != '/') {
                scp = '/' + scp;
            } else {
                throw new SCDException("Error in incomplete SCP: Invalid starting character");
            }
        }
        int stepStart = 0;
        int[] currentPosition = new int[] { 0 };
        while (currentPosition[0] < scp.length()) {
            if (charAt(scp, currentPosition[0]) == '/') {
                if (charAt(scp, currentPosition[0] + 1) == '/') {
                    if (currentPosition[0] + 1 != scp.length() - 1) {
                        steps.add(new Step(Axis.SPECIAL_COMPONENT, WILDCARD, 0));
                        stepStart = currentPosition[0] + 2;
                    } else {
                        stepStart = currentPosition[0] + 1;
                    }
                } else {
                    if (currentPosition[0] != scp.length() - 1) {
                        stepStart = currentPosition[0] + 1;
                    } else {
                        stepStart = currentPosition[0];
                    }
                }
                step = processStep(scp, currentPosition, stepStart, nsContext);
                steps.add(step);
            } else { // error: invalid scp. should start with a slash
                throw new SCDException("Error in SCP: Invalid character \'"
                        + charAt(scp, currentPosition[0]) + " \' at position"
                        + currentPosition[0]);
            }
        }
        return steps;
    }

    private static Step processStep(String step, int[] newPosition, int currentPosition, NamespaceContext nsContext)
    throws SCDException {
        short axis = -1;
        QName nameTest = null;
        int predicate = 0;

        switch (getCharType(charAt(step, currentPosition))) { // 0
        case CHARTYPE_AT: // '@'
            axis = Axis.SCHEMA_ATTRIBUTE;
            nameTest = readNameTest(step, newPosition, currentPosition + 1,
                    nsContext); // 1 handles *, 0, and QNames.
            break;
        case CHARTYPE_TILDE: // '~'
            axis = Axis.TYPE;
            nameTest = readNameTest(step, newPosition, currentPosition + 1,
                    nsContext); // 1
            break;
        case CHARTYPE_PERIOD: // '.'
            axis = Axis.CURRENT_COMPONENT;
            nameTest = WILDCARD;
            newPosition[0] = currentPosition + 1;
            break;
        case CHARTYPE_ZERO: // '0'
            axis = Axis.SCHEMA_ELEMENT; // Element without a name. This will
            // never match anything.
            nameTest = ZERO;
            newPosition[0] = currentPosition + 1;
            break;
        case CHARTYPE_STAR: // '*'
            axis = Axis.SCHEMA_ELEMENT;
            nameTest = WILDCARD;
            newPosition[0] = currentPosition + 1;
            break;
        case CHARTYPE_NC_NAMESTART: // isXML11NCNameStart()
            QName name = readQName(step, newPosition, currentPosition,
                    nsContext); // 0 handles a and a:b
            int newPos = newPosition[0];
            if (newPosition[0] == step.length()) {
                axis = Axis.SCHEMA_ELEMENT;
                nameTest = name;
            } else if (charAt(step, newPos) == ':'
                && charAt(step, newPos + 1) == ':') {
                // TODO: what to do with extension axes?
                // Could be a hashtable look up; fail if extension axis
                axis = Axis.qnameToAxis(name.rawname);
                if (axis == Axis.EXTENSION_AXIS) {
                    throw new SCDException(
                            "Error in SCP: Extension axis {"+name.rawname+"} not supported!");
                }
                nameTest = readNameTest(step, newPosition, newPos + 2,
                        nsContext);
            } else if (charAt(step, newPos) == '(') {
                throw new SCDException(
                "Error in SCP: Extension accessor not supported!");
            } else if (charAt(step, newPos) == '/') { // /abc:def/...
                axis = Axis.SCHEMA_ELEMENT;
                nameTest = name;
                return new Step(axis, nameTest, predicate);
            } else { // /abc:def[6]
                axis = Axis.SCHEMA_ELEMENT;
                nameTest = name;
            }
            break;
        default:
            throw new SCDException("Error in SCP: Invalid character \'"
                    + charAt(step, currentPosition) + "\' at position "
                    + currentPosition);
        }
        if (newPosition[0] < step.length()) {
            if (charAt(step, newPosition[0]) == '[') {
                predicate = readPredicate(step, newPosition, newPosition[0] + 1); // Also consumes right-bracket
            } else if (charAt(step, newPosition[0]) == '/') { // /a::a/a...
                return new Step(axis, nameTest, predicate);
            } else {
                throw new SCDException("Error in SCP: Unexpected character \'"
                        + charAt(step, newPosition[0]) + "\' at position "
                        + newPosition[0]);
            }
            // TODO: handle what if not?
        }
        if (charAt(step, newPosition[0]) == '/') {// /abc:def[6]/...
            return new Step(axis, nameTest, predicate);
        }
        if (newPosition[0] < step.length()) {
            throw new SCDException("Error in SCP: Unexpected character \'"
                    + step.charAt(newPosition[0]) + "\' at the end");
        }
        return new Step(axis, nameTest, predicate);
    } // processStep()

    /**
     * Creates a list of Step objects from the input relative SCD string by
     * parsing it
     * @param relativeSCD
     * @param isIncompleteSCD if the relative SCD in the first parameter an incomplete SCD
     * @return the list of Step objects
     */
    public List parseRelativeSCD(String relativeSCD, boolean isIncompleteSCD) throws SCDException {
        // xmlns(p=http://example.com/schema/po)xscd(/type::p:USAddress)
        int[] currentPosition = new int[] { 0 };
        NamespaceContext nsContext = new NamespaceSupport();
        //System.out.println("Relative SCD## " + relativeSCD);
        while (currentPosition[0] < relativeSCD.length()) {
            if ("xmlns".equals(relativeSCD.substring(currentPosition[0], currentPosition[0] + 5))) { // TODO catch string out of bound exception
                currentPosition[0] = readxmlns(relativeSCD, nsContext, currentPosition[0] + 5);
            } else if ("xscd".equals(relativeSCD.substring(currentPosition[0], currentPosition[0] + 4))) { // (/type::p:USAddress) part
                // process xscd() part
                String data = relativeSCD.substring(currentPosition[0] + 4, relativeSCD.length());
                if (charAt(data, 0) == '('
                    && charAt(data, data.length() - 1) == ')') {
                    return parseSCP(data.substring(1, data.length() - 1), nsContext, isIncompleteSCD);
                }
                throw new SCDException("Error in SCD: xscd() part is invalid at position "
                        + ++currentPosition[0]);
            } else {
                throw new SCDException("Error in SCD: Expected \'xmlns\' or \'xscd\' at position "
                        + ++currentPosition[0]);
            }
        }
        throw new SCDException("Error in SCD: Error at position "
                + ++currentPosition[0]);
    } // createSteps()

    private static int readxmlns(String data, NamespaceContext nsContext,
            int currentPosition) throws SCDException {
        if (charAt(data, currentPosition++) == '(') {
            // readNCName
            int pos = currentPosition;
            currentPosition = scanNCName(data, currentPosition);
            if (currentPosition == pos) {
                throw new SCDException(
                        "Error in SCD: Missing namespace name at position "
                        + ++currentPosition);
            }
            String name = data.substring(pos, currentPosition);
            // skip S
            currentPosition = skipWhiteSpaces(data, currentPosition);
            // read '='
            if (charAt(data, currentPosition) != '=') {
                throw new SCDException("Error in SCD: Expected a  \'=\' character at position "
                        + ++currentPosition);
            }
            // skip S
            currentPosition = skipWhiteSpaces(data, ++currentPosition);
            // read uri
            pos = currentPosition;
            currentPosition = scanXmlnsSchemeData(data, currentPosition);
            if (currentPosition == pos) {
                throw new SCDException("Error in SCD: Missing namespace value at position "
                        + ++currentPosition);
            }
            String uri = data.substring(pos, currentPosition);
            if (charAt(data, currentPosition) == ')') {
                nsContext.declarePrefix(name.intern(), uri.intern());
                return ++currentPosition;
            }
            throw new SCDException("Error in SCD: Invalid xmlns pointer part at position "
                    + ++currentPosition);
        }
        throw new SCDException("Error in SCD: Invalid xmlns pointer part at position "
                + ++currentPosition);
    } // readxmlns()

}
