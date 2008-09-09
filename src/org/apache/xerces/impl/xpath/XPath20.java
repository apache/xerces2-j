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

package org.apache.xerces.impl.xpath;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.xs.TypeValidator;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.AttributePSVImpl;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.util.IntStack;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xni.XMLAttributes;

/**
 * Bare minimal XPath 2.0 implementation for schema
 * type alternatives
 *
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class XPath20 {

    protected final String fExpression;
    protected final SymbolTable fSymbolTable;
    protected NodeTest fSyntaxTree;
    protected final NamespaceContext fContext;

    public XPath20(String xpath, SymbolTable symbolTable,
            NamespaceContext context) throws XPathException {
        fExpression = xpath;
        fSymbolTable = symbolTable;
        fContext = context;
        fSyntaxTree = null;
        parseExpression();
    }

    /**
     * Traverse the syntax tree recursively and evaluates the tests stored in each of
     * the nodes. Finally combines the test results and returns the overall result of
     * the test expression as a boolean value.
     */
    public boolean traverseTree(QName element, XMLAttributes attributes) {
        if (fSyntaxTree == null) {
            return false;
        }
        return visitNode(fSyntaxTree.getChildren()[0], element, attributes);
    }

    private boolean visitNode(NodeTest node, QName element, XMLAttributes attributes) {
        int nodeType = node.getType();
        if (nodeType == SyntaxTreeBuilder.TEST_CONJUNCTION_AND ||
                node.getType() == SyntaxTreeBuilder.TEST_CONJUNCTION_OR) {
            NodeTest[] children = node.getChildren();
            //recursively traverse the left sub tree
            boolean result1 = visitNode(children[0], element, attributes);
            //recursively traverse the right sub tree
            boolean result2 = visitNode(children[1], element, attributes);
            //combine the results
            if (nodeType == SyntaxTreeBuilder.TEST_CONJUNCTION_AND) {
                return result1 && result2;
            }
            else {
                return result1 || result2;
            }
        }
        else {
            return node.evaluateNodeTest(element, attributes);
        }
    }

    /**
     * Parses the XPath expression and builds a tree model out of it.
     */
    private void parseExpression()
            throws XPathException {
        XPath20.Scanner scanner = new Scanner();
        XPath20.Tokens tokens = new Tokens();
        try {
            scanner.scanExpr(fExpression, 0, fExpression.length(), tokens);
            fSyntaxTree = XPath20.Parser.parse(tokens, fContext, fSymbolTable);
        }
        catch (XPathException e) {
            fSyntaxTree = null; //reset the tree
            throw e;
        }
    }

    /**
     * The Scanner implementation for XPath 2.0 expressions used in
     * the type alternatives.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class Scanner {

        /**
         * 7-bit ASCII subset
         *
         *  0   1   2   3   4   5   6   7   8   9   A   B   C   D   E   F
         *  0,  0,  0,  0,  0,  0,  0,  0,  0, HT, LF,  0,  0, CR,  0,  0,  // 0
         *  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  // 1
         * SP,  !,  ",  #,  $,  %,  &,  ',  (,  ),  *,  +,  ,,  -,  .,  /,  // 2
         *  0,  1,  2,  3,  4,  5,  6,  7,  8,  9,  :,  ;,  <,  =,  >,  ?,  // 3
         *  @,  A,  B,  C,  D,  E,  F,  G,  H,  I,  J,  K,  L,  M,  N,  O,  // 4
         *  P,  Q,  R,  S,  T,  U,  V,  W,  X,  Y,  Z,  [,  \,  ],  ^,  _,  // 5
         *  `,  a,  b,  c,  d,  e,  f,  g,  h,  i,  j,  k,  l,  m,  n,  o,  // 6
         *  p,  q,  r,  s,  t,  u,  v,  w,  x,  y,  z,  {,  |,  },  ~, DEL  // 7
         */
        private static final byte
            CHARTYPE_INVALID            =  0,   // invalid XML character
            CHARTYPE_OTHER              =  1,   // not special - one of "#%&;?\^`{}~" or DEL
            CHARTYPE_WHITESPACE         =  2,   // one of "\t\n\r " (0x09, 0x0A, 0x0D, 0x20)
            CHARTYPE_EXCLAMATION        =  3,   // '!' (0x21)
            CHARTYPE_QUOTE              =  4,   // '\"' or '\'' (0x22 and 0x27)
            CHARTYPE_DOLLAR             =  5,   // '$' (0x24)
            CHARTYPE_OPEN_PAREN         =  6,   // '(' (0x28)
            CHARTYPE_CLOSE_PAREN        =  7,   // ')' (0x29)
            CHARTYPE_STAR               =  8,   // '*' (0x2A)
            CHARTYPE_PLUS               =  9,   // '+' (0x2B)
            CHARTYPE_COMMA              = 10,   // ',' (0x2C)
            CHARTYPE_MINUS              = 11,   // '-' (0x2D)
            CHARTYPE_PERIOD             = 12,   // '.' (0x2E)
            CHARTYPE_SLASH              = 13,   // '/' (0x2F)
            CHARTYPE_DIGIT              = 14,   // '0'-'9' (0x30 to 0x39)
            CHARTYPE_COLON              = 15,   // ':' (0x3A)
            CHARTYPE_LESS               = 16,   // '<' (0x3C)
            CHARTYPE_EQUAL              = 17,   // '=' (0x3D)
            CHARTYPE_GREATER            = 18,   // '>' (0x3E)
            CHARTYPE_ATSIGN             = 19,   // '@' (0x40)
            CHARTYPE_LETTER             = 20,   // 'A'-'Z' or 'a'-'z' (0x41 to 0x5A and 0x61 to 0x7A)
            CHARTYPE_OPEN_BRACKET       = 21,   // '[' (0x5B)
            CHARTYPE_CLOSE_BRACKET      = 22,   // ']' (0x5D)
            CHARTYPE_UNDERSCORE         = 23,   // '_' (0x5F)
            CHARTYPE_UNION              = 24,   // '|' (0x7C)
            CHARTYPE_NONASCII           = 25;   // Non-ASCII Unicode codepoint (>= 0x80)

        private static final byte[] fASCIICharMap = {
            0,  0,  0,  0,  0,  0,  0,  0,  0,  2,  2,  0,  0,  2,  0,  0,
            0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
            2,  3,  4,  1,  5,  1,  1,  4,  6,  7,  8,  9, 10, 11, 12, 13,
           14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15,  1, 16, 17, 18,  1,
           19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
           20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,  1, 22,  1, 23,
            1, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
           20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,  1, 24,  1,  1,  1
        };

        // symbols
        private static final String fAndSymbol = "and".intern();
        private static final String fOrSymbol = "or".intern();
        private static final String fCastSymbol = "cast".intern();
        private static final String fAsSymbol = "as".intern();

        /**
         * Scans the given XPath expression one character at a time and break it
         * up into a series of tokens.
         */
        public void scanExpr(String data, int currentOffset, int endOffset, XPath20.Tokens tokens)
                    throws XPathException {

            int ch, nameOffset;
            String nameHandle, prefixHandle;

            while (true) {
                if (currentOffset == endOffset) {
                    break;
                }
                ch = data.charAt(currentOffset);    //read a character from the expression

                //check for whitespace(s)token
                while (ch == ' ' || ch == 0x0A || ch == 0x09 || ch == 0x0D) {
                    if (++currentOffset == endOffset) {
                        break;
                    }
                    ch = data.charAt(currentOffset);
                }

                if (currentOffset == endOffset) {
                    break;
                }

                byte chartype = (ch >= 0x80) ? CHARTYPE_NONASCII : fASCIICharMap[ch];

                switch (chartype) {

                case CHARTYPE_ATSIGN:
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_AT_SIGN);
                    if (++currentOffset == endOffset) {
                        //an expression cannot end with an '@' sign
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case CHARTYPE_EQUAL:
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_EQUAL);
                    if (++currentOffset == endOffset) {
                        //an expression cannot end with '=' sign
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case CHARTYPE_EXCLAMATION:
                    if (++currentOffset == endOffset) {
                        //an expression cannot end with '!' sign
                        throw new XPathException("c-general-xpath");
                    }
                    ch = data.charAt(currentOffset);
                    if (ch != '=') {
                        throw new XPathException("c-general-xpath");
                    }
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_NOT_EQUAL);
                    if (++currentOffset == endOffset) {
                        //an expression cannot end with '!=' sign
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case CHARTYPE_LESS:
                    //the token could be '<' or '<='.
                    //we need to read the next character to be sure
                    if (++currentOffset == endOffset) {
                        throw new XPathException("c-general-xpath");
                    }
                    ch = data.charAt(currentOffset);
                    if (ch == '=') {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_LESS_EQUAL);
                        if (++currentOffset == endOffset) {
                            throw new XPathException("c-general-xpath");
                        }
                    } else {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_LESS);
                        if (currentOffset + 1 == endOffset) {
                            throw new XPathException("c-general-xpath");
                        }
                    }
                    break;

                case CHARTYPE_GREATER:
                    //the token could be '>' or '>='
                    if (++currentOffset == endOffset) {
                        throw new XPathException("c-general-xpath");
                    }
                    ch = data.charAt(currentOffset);
                    if (ch == '=') {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_GREATER_EQUAL);
                        if (++currentOffset == endOffset) {
                            throw new XPathException("c-general-xpath");
                        }
                    } else {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_COMPARATOR_GREATER);
                        if (currentOffset + 1 == endOffset) {
                            //we have the last read character still
                            //to be processed and so we don't touch
                            //the value of currentOffset
                            throw new XPathException("c-general-xpath");
                        }
                    }
                    break;

                case CHARTYPE_QUOTE:
                    int qchar = ch;
                    if (++currentOffset == endOffset) {
                        throw new XPathException("c-general-xpath");
                    }
                    ch = data.charAt(currentOffset);
                    int litOffset = currentOffset;
                    while (ch != qchar) {
                        if (++currentOffset == endOffset) {
                            //no matching quote
                            throw new XPathException("c-general-xpath");
                        }
                        ch = data.charAt(currentOffset);
                    }

                    int litLength = currentOffset - litOffset;
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_STRING_LITERAL, data.substring
                            (litOffset, litOffset + litLength));
                    ++currentOffset;
                    break;

                case CHARTYPE_OPEN_PAREN:
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_OPEN_PAREN);
                    if (++currentOffset == endOffset) {
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case CHARTYPE_CLOSE_PAREN:
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_CLOSE_PAREN);
                    ++currentOffset;
                    break;

                case CHARTYPE_STAR:
                    //if '*' is encountered treat it as if it was a NameTest
                    if (++currentOffset == endOffset) {
                        break;
                    }
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_NAMETEST_ANY);
                    break;

                case CHARTYPE_OTHER:
                    if (ch == '?') {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_QUESTION_SIGN);
                    }
                    else {
                        throw new XPathException("c-general-xpath");
                    }

                    ++currentOffset;
                    break;

                case CHARTYPE_DIGIT:
                    nameOffset = currentOffset;
                    currentOffset = scanNumber(data, currentOffset, endOffset);
                    tokens.addToken(XPath20.Tokens.EXPRTOKEN_NUMERIC_LITERAL, data.
                            substring(nameOffset, currentOffset));
                    break;

                case CHARTYPE_LETTER:
                    nameOffset = currentOffset;
                    currentOffset = scanNCName(data, currentOffset, endOffset);
                    if (currentOffset == nameOffset) {
                        throw new XPathException("c-general-xpath");
                    }

                    //read the string of characters
                    nameHandle = data.substring(nameOffset, currentOffset);

                    //we read the next character to see whether we have hit a
                    //QName or a NameTest
                    if (currentOffset < endOffset) {
                        ch = data.charAt(currentOffset);
                    }
                    else {
                        ch = -1;
                    }
                    boolean isNameTest = false;
                    boolean isQName = false;
                    prefixHandle = XMLSymbols.EMPTY_STRING;
                    if (ch == ':') {
                        if (++currentOffset == endOffset) {
                            throw new XPathException("c-general-xpath");
                        }
                        ch = data.charAt(currentOffset);
                        if (ch == '*') {
                            //we are at a NameTest
                            if (++currentOffset < endOffset) {
                                ch = data.charAt(currentOffset);
                            }
                            isNameTest = true;
                        }
                        else {
                            prefixHandle = nameHandle;
                            nameOffset = currentOffset;
                            currentOffset = scanNCName(data, currentOffset, endOffset);
                            if (currentOffset == nameOffset) {
                                throw new XPathException("c-general-xpath");
                            }
                            if (currentOffset < endOffset) {
                                ch = data.charAt(currentOffset);
                            }
                            else {
                                ch = -1;
                            }
                            //we have more text, ie we are at a QName
                            nameHandle = data.substring(nameOffset, currentOffset);
                            isQName = true;
                        }
                    }
                    else {
                        //if nothing special was found we treat what we have
                        //read so far as a QName and continue
                        isQName = true;
                    }

                    //deal with any whitespace
                    while (ch == ' ' || ch == 0x0A || ch == 0x09 || ch == 0x0D) {
                        if (++currentOffset == endOffset) {
                            break;
                        }
                        ch = data.charAt(currentOffset);
                    }

                    if (nameHandle.equals(fAndSymbol)) {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_KEYWORD_AND);
                    }
                    else if (nameHandle.equals(fOrSymbol)) {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_KEYWORD_OR);
                    }
                    else if (nameHandle.equals(fCastSymbol)) {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_KEYWORD_CAST);
                    }
                    else if (nameHandle.equals(fAsSymbol)) {
                        tokens.addToken(XPath20.Tokens.EXPRTOKEN_KEYWORD_AS);
                    }
                    else {
                        if (isNameTest) {
                            tokens.addToken(XPath20.Tokens.EXPRTOKEN_NAMETEST_NS, nameHandle);
                        }
                        else if (isQName) {
                            if (prefixHandle !=XMLSymbols.EMPTY_STRING) {
                                nameHandle = prefixHandle + ":" +nameHandle;
                            }
                            tokens.addToken(XPath20.Tokens.EXPRTOKEN_QNAME, nameHandle);
                        }
                        else {
                            throw new XPathException("c-general-xpath");
                        }
                    }
                    break;

                case CHARTYPE_INVALID:
                case CHARTYPE_DOLLAR:
                case CHARTYPE_PLUS:
                case CHARTYPE_UNION:
                case CHARTYPE_OPEN_BRACKET:
                case CHARTYPE_CLOSE_BRACKET:
                case CHARTYPE_COLON:
                case CHARTYPE_COMMA:
                case CHARTYPE_SLASH:
                    throw new XPathException("c-general-xpath");

                default:
                    throw new XPathException("c-general-xpath");
                }
            }
        }

        /*
         * Reads a numeric literal in a test XPath expression.
         */
        private int scanNumber(String data, int currentOffset, int endOffset) throws XPathException {
            int ch = data.charAt(currentOffset);
            while (ch >= '0' && ch <= '9') {
                if (++currentOffset == endOffset) {
                    break;
                }
                ch = data.charAt(currentOffset);
            }

            if (ch == '.') {
                if (++currentOffset < endOffset) {
                    ch = data.charAt(currentOffset);
                    while (ch >= '0' && ch <= '9') {
                        if (++currentOffset == endOffset) {
                            break;
                        }
                        ch = data.charAt(currentOffset);
                    }
                }
            }

            if (ch == 'e' || ch == 'E') {
                if (++currentOffset < endOffset) {
                    ch = data.charAt(currentOffset);
                    if (ch == '+' || ch == '-') {
                        ch = data.charAt(++currentOffset);
                    }

                    if (ch < '0' || ch > '9') {
                        throw new XPathException("c-general-xpath");
                    }

                    while (ch >= '0' && ch <= '9') {
                        if (++currentOffset == endOffset) {
                            break;
                        }
                        ch = data.charAt(currentOffset);
                    }
                }
                else {
                    throw new XPathException("c-general-xpath");
                }
            }
            return currentOffset;
        }

        /*
         * Reads a NCName in a test XPath expression
         */
        private int scanNCName(String data, int currentOffset, int endOffset) {
            int ch = data.charAt(currentOffset);
            if (ch >= 0x80) {
                if (!XMLChar.isNameStart(ch)) {
                    return currentOffset;
                }
            }
            else {
                byte chartype = fASCIICharMap[ch];
                if (chartype != CHARTYPE_LETTER && chartype != CHARTYPE_UNDERSCORE) {
                    return currentOffset;
                }
            }

            while (++currentOffset < endOffset) {
                ch = data.charAt(currentOffset);
                if (ch >= 0x80) {
                    if (!XMLChar.isName(ch)) {
                        break;
                    }
                }
                else {
                    byte chartype = fASCIICharMap[ch];
                    if (chartype != CHARTYPE_LETTER && chartype != CHARTYPE_DIGIT &&
                            chartype != CHARTYPE_PERIOD && chartype != CHARTYPE_MINUS &&
                            chartype != CHARTYPE_UNDERSCORE) {
                        break;
                    }
                }
            }
            return currentOffset;
        }

    }

    /**
     * Used by the XPath20.Tokenizer to store a set of tokens. Generally
     * only the token type is stored. But in certain situations the textual
     * value of the tokens can be saved.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class Tokens {

        public static final int
            EXPRTOKEN_OPEN_PAREN                    =   0,
            EXPRTOKEN_CLOSE_PAREN                   =   1,

            EXPRTOKEN_KEYWORD_OR                    =   2,
            EXPRTOKEN_KEYWORD_AND                   =   3,
            EXPRTOKEN_KEYWORD_CAST                  =   4,
            EXPRTOKEN_KEYWORD_AS                    =   5,

            EXPRTOKEN_COMPARATOR_EQUAL              =   6,
            EXPRTOKEN_COMPARATOR_NOT_EQUAL          =   7,
            EXPRTOKEN_COMPARATOR_LESS               =   8,
            EXPRTOKEN_COMPARATOR_LESS_EQUAL         =   9,
            EXPRTOKEN_COMPARATOR_GREATER            =   10,
            EXPRTOKEN_COMPARATOR_GREATER_EQUAL      =   11,

            EXPRTOKEN_AT_SIGN                       =   12,
            EXPRTOKEN_QUESTION_SIGN                 =   13,
            EXPRTOKEN_STAR_SIGN                     =   14,
            EXPRTOKEN_COLON_SIGN                    =   15,

            EXPRTOKEN_NUMERIC_LITERAL               =   16,
            EXPRTOKEN_STRING_LITERAL                =   17,

            //[7] QName ::= PrefixedName | UnprefixedName
            EXPRTOKEN_QNAME                         =   18,
            EXPRTOKEN_ATTR_NAME                     =   19,

            //[4] NameTest ::= QName | '*' | NCName ':' '*'
            EXPRTOKEN_NAMETEST_ANY                  =   20, //'*' scenario
            EXPRTOKEN_NAMETEST_NS                   =   21; //NCName:* scenario


        private static final int INITIAL_TOKEN_COUNT = 8;

        //the token list
        private int[] fTokens = new int[INITIAL_TOKEN_COUNT];

        //stores the textual values of tokens (indexed by the corresponding index in the token list)
        private String[] fTokenNames = new String[INITIAL_TOKEN_COUNT];
        //private Hashtable<Integer, String> fTokenNames = new Hashtable<Integer, String>();
        private int fTokenCount = 0;
        private int fCurrentTokenIndex = 0;

        /**
         * Adds the specified token to the list of tokens. Only the token
         * type is saved.
         */
        public void addToken(int token) {
        	addToken(token, null);
        }
        /*public void addToken(int token) {

            if (fTokenCount == fTokens.length) {
                int[] oldArray = fTokens;
                fTokens = new int[fTokenCount << 1];
                System.arraycopy(oldArray, 0, fTokens, 0, fTokenCount);
            }
            fTokens[fTokenCount] = token;
            fTokenCount++;
        }*/

        /**
         * Adds the specified token to the list of tokens along with its
         * textual value.
         */
        public void addToken(int token, String tokenStr) {

            if (fTokenCount == fTokens.length) {
                int[] oldArray = fTokens;
                fTokens = new int[fTokenCount << 1];
                System.arraycopy(oldArray, 0, fTokens, 0, fTokenCount);

                String[] oldNames = fTokenNames;
                fTokenNames = new String[fTokenCount << 1];
                System.arraycopy(oldNames, 0, fTokenNames, 0, fTokenCount);
            }
            fTokens[fTokenCount] = token;
            fTokenNames[fTokenCount] = tokenStr;
            fTokenCount++;
        }
        /*public void addToken(int token, String tokenStr) {
            addToken(token);
            fTokenNames.put(fTokenCount - 1, tokenStr);
        }*/

        /**
         * Checks whether there are any more tokens in the list
         */
        public boolean hasNext() {
            return fCurrentTokenIndex < fTokenCount;
        }

        /**
         * Returns the next token in the token list
         */
        public int next() throws XPathException {
            if (fCurrentTokenIndex == fTokenCount) {
                throw new XPathException("c-general-xpath");
            }
            return fTokens[fCurrentTokenIndex++];
        }

        public int peek() {
            if (fCurrentTokenIndex != fTokenCount) {
                return fTokens[fCurrentTokenIndex];
            }
            else {
                return -1;
            }
        }

        /**
         * Reset the position indicator in the token list to be 0
         */
        public void rewind() {
            fCurrentTokenIndex = 0;
        }

        /**
         * Gets the textual value of a token. If the text value for
         * the specified token is not in the Map will return null.
         */
        public String getTokenValue(int tokenIndex) {
            return fTokenNames[tokenIndex];
        }
    }


    /**
     * The parser implementation for the test XPath expressions
     * used in type alternatives. Processes a set of tokens given
     * by the XPath20.Tokenizer and checks whether they expression
     * conforms to the XML Schema 1.1 Structures specification.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class Parser {

        /**
         * Parses a set of tokens to see whether the test XPath expression
         * represented by the tokens is valid. Valid tokens are passed to the
         * SyntaxTreeBuilder in order to construct the abstract syntax tree
         * representation of the expression.
         */
        public static NodeTest parse(XPath20.Tokens tokens, NamespaceContext context,
                SymbolTable symbolTable) throws XPathException {

            boolean expectingNameTest = false;
            boolean expectingQName = true;
            boolean attrAxis = false;
            boolean inCastExpr = false;

            int i = 0;
            int openBrackets = 0;
            int prevToken = -1;

            //init the content model builder
            SyntaxTreeBuilder treeBuilder = new SyntaxTreeBuilder();

            while (tokens.hasNext()) {
                int token = tokens.next();

                switch (token) {

                case XPath20.Tokens.EXPRTOKEN_AT_SIGN:
                    //we are at the beginning of an attribute name
                    check(!expectingNameTest);
                    //must be followed by a nametest
                    expectingNameTest = true;
                    expectingQName = false;
                    attrAxis = true;
                    break;

                case XPath20.Tokens.EXPRTOKEN_QNAME:
                    check(expectingNameTest || expectingQName);
                    if (prevToken == XPath20.Tokens.EXPRTOKEN_AT_SIGN) {
                        //if the '@' sign was detected previously
                        //we should let the tree builder know that we
                        //are dealing with an attribute name here
                        treeBuilder.addToken(Tokens.EXPRTOKEN_ATTR_NAME, tokens.getTokenValue(i));
                    }
                    else {
                        treeBuilder.addToken(token, tokens.getTokenValue(i));
                    }
                    expectingNameTest = false;
                    if (inCastExpr) {
                        //any explicit casts must be to built-in types
                        if (!isBuiltInType(tokens.getTokenValue(i), context, symbolTable)) {
                            throw new XPathException("c-general-xpath");
                        }

                        //if we are in a cast expression the QName can be followed by
                        //a '?'
                        token = tokens.peek();
                        if (token == XPath20.Tokens.EXPRTOKEN_QUESTION_SIGN) {
                            token = tokens.next();
                            treeBuilder.addToken(token, null);
                            i++;
                        }
                        else {
                            treeBuilder.addToken(XPath20.Tokens.EXPRTOKEN_QUESTION_SIGN, null);
                        }
                        //end of cast expression
                        inCastExpr = false;
                        expectingQName = false;
                    }
                    break;

                //handle comparator signs (=, >, <, <=, >=, !=)
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_EQUAL:
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_NOT_EQUAL:
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_GREATER:
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_GREATER_EQUAL:
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_LESS:
                case XPath20.Tokens.EXPRTOKEN_COMPARATOR_LESS_EQUAL:
                    check(!expectingNameTest && !expectingQName);
                    if (!tokens.hasNext()) {
                        //the test must not end here
                        //if it does something's wrong
                        throw new XPathException("c-general-xpath");
                    }
                    else if (prevToken == XPath20.Tokens.EXPRTOKEN_STRING_LITERAL ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_NUMERIC_LITERAL ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_QUESTION_SIGN ||
                            attrAxis) {
                        //the previous token must be a literal or a
                        //cast expression
                        treeBuilder.addToken(token, null);
                        attrAxis = false;
                    }
                    else {
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case XPath20.Tokens.EXPRTOKEN_NUMERIC_LITERAL:
                case XPath20.Tokens.EXPRTOKEN_STRING_LITERAL:
                    treeBuilder.addToken(token, tokens.getTokenValue(i));
                    expectingNameTest = false;
                    expectingQName = false;
                    break;

                case XPath20.Tokens.EXPRTOKEN_KEYWORD_CAST:
                    if (prevToken == XPath20.Tokens.EXPRTOKEN_STRING_LITERAL ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_NUMERIC_LITERAL ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_NAMETEST_NS ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_NAMETEST_ANY ||
                            prevToken == XPath20.Tokens.EXPRTOKEN_QNAME) {

                        treeBuilder.addToken(token, null);
                        token = tokens.next();
                        if (token == XPath20.Tokens.EXPRTOKEN_KEYWORD_AS) {
                            //keyword 'cast' must be followed by the keyword 'as'
                            treeBuilder.addToken(token, null);
                            //should be followed by a QName
                            expectingQName = true;
                            expectingNameTest = false;
                            inCastExpr = true;
                            i++;
                        }
                        else {
                            throw new XPathException("c-general-xpath");
                        }
                    }
                    else {
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                case XPath20.Tokens.EXPRTOKEN_KEYWORD_OR:
                case XPath20.Tokens.EXPRTOKEN_KEYWORD_AND:
                    //might be followed by a QName
                    expectingQName = true;
                    treeBuilder.addToken(token, null);
                    break;

                case XPath20.Tokens.EXPRTOKEN_OPEN_PAREN:
                    openBrackets++;
                    expectingQName = true;
                    treeBuilder.addToken(token, null);
                    break;

                case XPath20.Tokens.EXPRTOKEN_CLOSE_PAREN:
                    if (openBrackets > 0) {
                        treeBuilder.addToken(token, null);
                        expectingQName = expectingNameTest = false;
                        openBrackets--;
                    }
                    else {
                        throw new XPathException("c-general-xpath");
                    }
                    break;

                default:
                        throw new XPathException("c-general-xpath");
                }

                //'cache' the current token for the next iteration
                prevToken = token;
                i++;
            }
            if (openBrackets > 0) {
                //check for bracket inconsistency
                throw new XPathException("c-general-xpath");
            }
            return treeBuilder.markEnd();
        }

        /**
         * Checks whether a specified boolean condition is satisfied by the current
         * state of the parser. If the conditions are not satisfied throws an XPath
         * exception
         */
        private static void check( boolean b ) throws XPathException {
            if(!b)      throw new XPathException("c-general-xpath");
        }

        /**
         * Checks whether a given QName represents a valid built-in type
         */
        private static boolean isBuiltInType(String qname, NamespaceContext context,
                SymbolTable symbolTable) {
            boolean builtIn = false;
            final int colonIndex = qname.indexOf(':');
            if (colonIndex != -1) {
                final String prefix = symbolTable.addSymbol(qname.substring(0, colonIndex));
                final String uri = context.getURI(prefix);
                if (SchemaSymbols.URI_SCHEMAFORSCHEMA == uri) {
                    final String local = qname.substring(colonIndex + 1);
                    XSSimpleType type = SchemaDVFactory.getInstance().getBuiltInType(local);
                    if (type != null) {
                        builtIn = true;
                    }
                }
            }
            return builtIn;
        }
    }


    /**
     * The syntax tree builder implementation for the test XPath expressions used in
     * the type alternatives.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class SyntaxTreeBuilder {

        public static final int
            TEST_CATEGORY_ROOT                          = 0,

            TEST_CATEGORY_VALUE                         = 1,
            TEST_CATEGORY_VALUE_AS_CAST                 = 2,
            TEST_CATEGORY_ATTR                          = 3,
            TEST_CATEGORY_ATTR_AS_CAST                  = 4,

            TEST_CATEGORY_ATTR_VALUE                    = 5,
            TEST_CATEGORY_ATTR_VALUE_AS_CAST            = 6,
            TEST_CATEGORY_ATTR_AS_CAST_VALUE            = 7,
            TEST_CATEGORY_ATTR_AS_CAST_VALUE_AS_CAST    = 8,

            TEST_CATEGORY_VALUE_VALUE                   = 9,
            TEST_CATEGORY_VALUE_VALUE_AS_CAST           = 10,
            TEST_CATEGORY_VALUE_AS_CAST_VALUE           = 11,
            TEST_CATEGORY_VALUE_AS_CAST_VALUE_AS_CAST   = 12,

            TEST_CATEGORY_ATTR_ATTR                     = 13,
            TEST_CATEGORY_ATTR_ATTR_AS_CAST             = 14,
            TEST_CATEGORY_ATTR_AS_CAST_ATTR             = 15,
            TEST_CATEGORY_ATTR_AS_CAST_ATTR_AS_CAST     = 16,

            TEST_CONJUNCTION_OR         = 17,
            TEST_CONJUNCTION_AND        = 18,
            TEST_OPEN_PAREN             = 19,
            TEST_CLOSE_PAREN            = 20;

        private static final int INITIAL_TOKEN_COUNT = 8;

        private NodeTest fCurrentNode;
        private int[] fTokensBuffer = new int[INITIAL_TOKEN_COUNT];
        private String[] fTokenNames = new String[INITIAL_TOKEN_COUNT];
        private int fTokenCount = 0;
        //private ArrayList<Integer> fTokensBuffer;
        //private ArrayList<String> fTokenNames;

        private boolean fExcessBrackets;
        private IntStack fBracketStates;

        private String fLHS, fRHS;
        private int fComparator;
        private boolean fNumeric;

        public SyntaxTreeBuilder() {
            fCurrentNode = null;
            //fTokensBuffer = new ArrayList<Integer>();
            //fTokenNames = new ArrayList<String>();
            fBracketStates = new IntStack();
            fExcessBrackets = false;
            fNumeric = false;
        }

        /**
         * Processes the given token and adds it to the syntax tree
         */
        public void addToken(int token, String text) throws XPathException{
            NodeTest node = null;
            NodeTest conjunction = null;

            switch (token) {

            case Tokens.EXPRTOKEN_KEYWORD_AND:
                node = getNodeTest();
                conjunction = new NodeTest(TEST_CONJUNCTION_AND);
                if (fExcessBrackets) {
                    fExcessBrackets = false;
                }
                break;

            case Tokens.EXPRTOKEN_KEYWORD_OR:
                node = getNodeTest();
                conjunction = new NodeTest(TEST_CONJUNCTION_OR);
                if (fExcessBrackets) {
                    fExcessBrackets = false;
                }
                break;

            case Tokens.EXPRTOKEN_OPEN_PAREN:
                fBracketStates.push(fExcessBrackets ? 1 : 0);
                fExcessBrackets = true;
                break;

            case Tokens.EXPRTOKEN_CLOSE_PAREN:
                if (!fExcessBrackets) {
                    node = getNodeTest();
                }
                fExcessBrackets = fBracketStates.pop() == 1;
                break;

            default:
                {
                    if (fTokenCount == fTokensBuffer.length) {
                        int[] oldArray = fTokensBuffer;
                        fTokensBuffer = new int[fTokenCount << 1];
                        System.arraycopy(oldArray, 0, fTokensBuffer, 0, fTokenCount);

                        String[] oldNames = fTokenNames;
                        fTokenNames = new String[fTokenCount << 1];
                        System.arraycopy(oldNames, 0, fTokenNames, 0, fTokenCount);
                    }
                    fTokensBuffer[fTokenCount] = token;
                    fTokenNames[fTokenCount] = text;
                    fTokenCount++;
                    //fTokensBuffer.add(token);
                    //fTokenNames.add(text);
                }
                break;
            }
            connectToTree(conjunction, node);
        }

        /**
         * Attach the two given nodes to the syntax tree
         */
        private void connectToTree(NodeTest conjunction, NodeTest node) {
            if (fCurrentNode == null && node != null) {
                if (conjunction != null) {
                    fCurrentNode = conjunction;
                    fCurrentNode.setFirstChild(node);
                }
                else {
                    fCurrentNode = node;
                }
            }
            else if (fCurrentNode != null && !fCurrentNode.isComplete()) {
                if (node != null) {
                    if (conjunction != null) {
                        fCurrentNode.setSecondChild(conjunction);
                        conjunction.setFirstChild(node);
                        fCurrentNode = conjunction;
                    }
                    else {
                        fCurrentNode.setSecondChild(node);
                        while (fCurrentNode.getParent() != null &&
                                fCurrentNode.isComplete()) {
                            fCurrentNode = fCurrentNode.getParent();
                        }
                    }
                }
            }
            else if (fCurrentNode != null && fCurrentNode.isComplete()) {
                if (conjunction != null && node == null) {
                    conjunction.setFirstChild(fCurrentNode);
                    fCurrentNode = conjunction;
                }
            }
        }

        /**
         * Call this method to complete building the syntax tree. Once called
         * this method will create the root node of the tree and attach the tree
         * constructed so far to the tree.
         */
        public NodeTest markEnd() throws XPathException {
            connectToTree(null, getNodeTest());
            NodeTest root = new NodeTest(TEST_CATEGORY_ROOT);
            root.setFirstChild(fCurrentNode);
            return root;
        }

        private NodeTest getNodeTest() throws XPathException {
            int testType = getTestCategory();
            if (testType < 0) {
                return null;
            }
            NodeTest node = new NodeTest(testType);
            node.setLHS(fLHS);
            if (testType != TEST_CATEGORY_ATTR &&
                    testType != TEST_CATEGORY_VALUE) {
                node.setRHS(fRHS);
                node.setComparator(fComparator);
            }

            node.setNumeric(fNumeric);
            fTokenCount = 0;
            //fTokensBuffer.clear();
            //fTokenNames.clear();
            return node;
        }

        /**
         * Analyzes the content currently in the tokens buffer and tries to
         * classify the expression into one of the test categories. Looks at
         * the number of tokens in the buffer, their order and types. While
         * doing so extracts the LHS, RHS and comparator components from the
         * test expression.
         */
        private int getTestCategory() throws XPathException {
            int testCategory;
            final int size = fTokenCount;

            switch (size) {
            case 0:
            	return -1;
            case 1:
                //Only one token in the test
                if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR;
                }
                else {
                    testCategory = TEST_CATEGORY_VALUE;
                    if (fTokensBuffer[0] == Tokens.EXPRTOKEN_NUMERIC_LITERAL) {
                        fNumeric = true;
                    }
                }
                fLHS = fTokenNames[0];
                break;

            case 3:
                //3 tokens in the test
                if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[2] == Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_ATTR;
                }
                else if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[2] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_VALUE;
                }
                else if ((fTokensBuffer[0] == Tokens.EXPRTOKEN_NUMERIC_LITERAL &&
                        fTokensBuffer[2] == Tokens.EXPRTOKEN_NUMERIC_LITERAL) ||
                        (fTokensBuffer[0] == Tokens.EXPRTOKEN_STRING_LITERAL &&
                                fTokensBuffer[2] == Tokens.EXPRTOKEN_STRING_LITERAL)) {
                    testCategory = TEST_CATEGORY_VALUE_VALUE;
                }
                else {
                    throw new XPathException("c-general-xpath");
                }
                fLHS = fTokenNames[0];
                fRHS = fTokenNames[2];
                fComparator = fTokensBuffer[1];
                break;

            case 5:
                //5 tokens in the test
                if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[1] == Tokens.EXPRTOKEN_KEYWORD_CAST) {
                    testCategory = TEST_CATEGORY_ATTR_AS_CAST;
                    fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                }
                else {
                    testCategory = TEST_CATEGORY_VALUE_AS_CAST;
                    fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                }
                break;

            case 7:
                //7 tokens in the test (one explicit cast is in the test)
                if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[1] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[6] == Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_AS_CAST_ATTR;
                    fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                    fRHS = fTokenNames[6];
                    fComparator = fTokensBuffer[5];
                }
                else if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[3] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[2] == Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_ATTR_AS_CAST;
                    fLHS = fTokenNames[0];
                    fRHS = fTokenNames[2] + " cast as " + fTokenNames[5] + " ?";
                    fComparator = fTokensBuffer[1];
                }
                else if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[1] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[6] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_AS_CAST_VALUE;
                    fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                    fRHS = fTokenNames[6];
                    fComparator = fTokensBuffer[5];
                }
                else if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[3] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[2] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_VALUE_AS_CAST;
                    fLHS = fTokenNames[0];
                    fRHS = fTokenNames[2] + " cast as " + fTokenNames[5] + " ?";
                    fComparator = fTokensBuffer[1];
                }
                else if (fTokensBuffer[0] != Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[1] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[6] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_VALUE_AS_CAST_VALUE;
                    fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                    fRHS = fTokenNames[6];
                    fComparator = fTokensBuffer[5];
                }
                else if (fTokensBuffer[0] != Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[3] == Tokens.EXPRTOKEN_KEYWORD_CAST &&
                        fTokensBuffer[2] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_VALUE_VALUE_AS_CAST;
                    fLHS = fTokenNames[0];
                    fRHS = fTokenNames[2] + " cast as " + fTokenNames[5] + " ?";
                    fComparator = fTokensBuffer[1];
                }
                else {
                    throw new XPathException("c-general-xpath");
                }
                break;

            case 11:
                //11 tokens in the test (two explicit casts in the test)
                if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[6] == Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_AS_CAST_ATTR_AS_CAST;
                }
                else if (fTokensBuffer[0] == Tokens.EXPRTOKEN_ATTR_NAME &&
                        fTokensBuffer[6] != Tokens.EXPRTOKEN_ATTR_NAME) {
                    testCategory = TEST_CATEGORY_ATTR_AS_CAST_VALUE_AS_CAST;
                }
                else {
                    testCategory = TEST_CATEGORY_VALUE_AS_CAST_VALUE_AS_CAST;
                }
                fLHS = fTokenNames[0] + " cast as " + fTokenNames[3] + " ?";
                fRHS = fTokenNames[6] + " cast as " + fTokenNames[9] + " ?";
                fComparator = fTokensBuffer[5];
                break;

            default:
                throw new XPathException("c-general-xpath");

            }

            if (fTokensBuffer[0] == Tokens.EXPRTOKEN_NUMERIC_LITERAL) {
                fNumeric = true;
            }
            return testCategory;
        }
    }

    /**
     * The syntax tree for test XPath expressions is composed of NodeTests.
     * A NodeTest encapsulates all the necessary information regarding a
     * simple test expressed in XPath 2.0 language.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class NodeTest {

        private int fNodeType;
        private NodeTest fParentNode = null;
        private NodeTest[] fChildNodes = null;

        private String fLHS = null;
        private String fRHS = null;
        private int fComparator;
        private boolean fNumeric = false;

        public NodeTest(int nodeType) {
            this.fNodeType = nodeType;
            if (nodeType == SyntaxTreeBuilder.TEST_CONJUNCTION_OR ||
                    nodeType == SyntaxTreeBuilder.TEST_CONJUNCTION_AND) {
                fChildNodes = new NodeTest[2];
            }
            else if (nodeType == SyntaxTreeBuilder.TEST_CATEGORY_ROOT) {
                fChildNodes = new NodeTest[1];
            }
        }

        public int getType() {
            return fNodeType;
        }

        public NodeTest[] getChildren() {
            return fChildNodes;
        }

        public void setLHS(String text) {
            fLHS = text;
        }

        public String getLHS() {
            return fLHS;
        }

        public void setRHS(String text) {
            fRHS = text;
        }

        public String getRHS() {
            return fRHS;
        }

        public void setComparator(int comp) {
            fComparator = comp;
        }

        public NodeTest getParent() {
            return fParentNode;
        }

        private void setParent(NodeTest parentNode) {
            this.fParentNode = parentNode;
        }

        public void setFirstChild(NodeTest test1) {
            if (fChildNodes != null) {
                fChildNodes[0] = test1;
                fChildNodes[0].setParent(this);
            }
        }

        public void setSecondChild(NodeTest test2) {
            if (fChildNodes != null) {
                fChildNodes[1] = test2;
                fChildNodes[1].setParent(this);
            }
        }

        public boolean getNumeric() {
            return fNumeric;
        }

        public void setNumeric(boolean numeric) {
            fNumeric = numeric;
        }

        /**
         * Checks whether the NodeTest is complete. A NodeTest is complete if it has both
         * an LHS and an RHS.
         */
        public boolean isComplete() {
            if (fNodeType != SyntaxTreeBuilder.TEST_CONJUNCTION_AND &&
                    fNodeType != SyntaxTreeBuilder.TEST_CONJUNCTION_OR) {
                return true;
            }
            else {
                if (fChildNodes[0] != null && fChildNodes[1] != null) {
                    return true;
                }
                return false;
            }
        }

        /**
         * Evaluates the test expression stored in the node and returns a boolean
         * value.
         */
        public boolean  evaluateNodeTest(QName element, XMLAttributes attributes) {
            Object actualVal1, actualVal2;
            Augmentations aug1, aug2;
            AttributePSVImpl attr1, attr2;
            short variety;
            XSSimpleTypeDecl type1, type2;

            switch (fNodeType) {

            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_VALUE:
                aug1 = attributes.getAugmentations(fLHS);
                if (aug1 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                variety = type1.getVariety();
                if (variety == XSSimpleTypeDefinition.VARIETY_UNION) {
                    type1 = (XSSimpleTypeDecl) attr1.getMemberTypeDefinition();
                }

                actualVal1 = attr1.getActualNormalizedValue();
                try {
                    actualVal2 = type1.validate(fRHS, null, null); //convert the value to the type of the attribute
                    return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_VALUE_AS_CAST:
                aug1 = attributes.getAugmentations(fLHS);
                if (aug1 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                variety = type1.getVariety();
                if (variety == XSSimpleTypeDefinition.VARIETY_UNION) {
                    type1 = (XSSimpleTypeDecl) attr1.getMemberTypeDefinition();
                }

                actualVal1 = attr1.getActualNormalizedValue();
                type2 = getCastedType(fRHS); //get the casted type
                try {
                    //cast the value into the specified type
                    actualVal2 = type2.validate(getCastedValue(fRHS), null, null);
                    //check whether the casted type and the attribute type are comparable
                    if (DataMatcher.isComparable(
                            attr1.getActualNormalizedValueType(),
                            type2.getBuiltInKind(),
                            attr1.getItemValueTypes(),
                            null)) {

                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    return false;
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_AS_CAST_VALUE:
                aug1 = attributes.getAugmentations(getCastedValue(fLHS));
                if (aug1 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                variety = type1.getVariety();
                if (variety == XSSimpleTypeDefinition.VARIETY_UNION) {
                    type1 = (XSSimpleTypeDecl) attr1.getMemberTypeDefinition();
                }

                type2 = getCastedType(fLHS);
                try {
                    //perform the specified cast on the attribute value
                    actualVal1 = type2.validate(attributes.getValue(getCastedValue(fLHS)), null, null);
                    //convert the given value into the attribute's casted type
                    actualVal2 = type2.validate(fRHS, null, null);
                    return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type2);
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_AS_CAST_VALUE_AS_CAST:
                aug1 = attributes.getAugmentations(getCastedValue(fLHS));
                if (aug1 == null) {
                    return false;
                }

                type1 = getCastedType(fLHS);
                type2 = getCastedType(fRHS);

                //check whether the two casted types are comparable
                if (DataMatcher.isComparable(
                        type1.getBuiltInKind(),
                        type2.getBuiltInKind(),
                        null,
                        null)) {

                    try {
                        //perform the two casts
                        actualVal1 = type1.validate(attributes.getValue(getCastedValue(fLHS)), null, null);
                        actualVal2 = type2.validate(getCastedValue(fRHS), null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_ATTR:
                aug1 = attributes.getAugmentations(fLHS);
                aug2 = attributes.getAugmentations(fRHS);
                if (aug1 == null || aug2 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                attr2 = (AttributePSVImpl) aug2.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                type2 = (XSSimpleTypeDecl) attr2.getTypeDefinition();

                //check whether the two attribute types are comparable
                if (DataMatcher.isComparable(
                        attr1.getActualNormalizedValueType(),
                        attr2.getActualNormalizedValueType(),
                        attr1.getItemValueTypes(),
                        attr2.getItemValueTypes())) {

                    actualVal1 = attr1.getActualNormalizedValue();
                    actualVal2 = attr2.getActualNormalizedValue();
                    return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                }
                return false;


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_ATTR_AS_CAST:
                aug1 = attributes.getAugmentations(fLHS);
                aug2 = attributes.getAugmentations(fRHS);
                if (aug1 == null || aug2 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                type2 = getCastedType(fRHS);

                //check whether the two attribute types are comparable
                if (DataMatcher.isComparable(
                        attr1.getActualNormalizedValueType(),
                        type2.getBuiltInKind(),
                        attr1.getItemValueTypes(),
                        null)) {

                    actualVal1 = attr1.getActualNormalizedValue();
                    try {
                        //perform the cast
                        actualVal2 = type2.validate(attributes.getValue(getCastedValue(fRHS)), null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return false;


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_AS_CAST_ATTR:
                aug1 = attributes.getAugmentations(fLHS);
                aug2 = attributes.getAugmentations(fRHS);
                if (aug1 == null || aug2 == null) {
                    return false;
                }

                attr2 = (AttributePSVImpl) aug2.getItem(Constants.ATTRIBUTE_PSVI);
                type2 = (XSSimpleTypeDecl) attr2.getTypeDefinition();
                type1 = getCastedType(fLHS);

                //check whether the two attribute types are comparable
                if (DataMatcher.isComparable(
                        type1.getBuiltInKind(),
                        attr2.getActualNormalizedValueType(),
                        null,
                        attr2.getItemValueTypes())) {

                    actualVal2 = attr2.getActualNormalizedValue();
                    try {
                        //perform the cast
                        actualVal1 = type1.validate(attributes.getValue(getCastedValue(fRHS)), null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return false;


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_AS_CAST_ATTR_AS_CAST:
                aug1 = attributes.getAugmentations(fLHS);
                aug2 = attributes.getAugmentations(fRHS);
                if (aug1 == null || aug2 == null) {
                    return false;
                }

                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = getCastedType(fLHS);
                type2 = getCastedType(fRHS);

                //check whether the two casted types are comparable
                if (DataMatcher.isComparable(
                        type1.getBuiltInKind(),
                        type2.getBuiltInKind(),
                        null,
                        null)) {

                    try {
                        //perform the two casts
                        actualVal1 = type1.validate(attributes.getValue(getCastedValue(fLHS)), null, null);
                        actualVal2 = type2.validate(attributes.getValue(getCastedValue(fRHS)), null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return false;


            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE_VALUE:
                //treat both values as strings and compare
                if (!fNumeric) {
                    return DataMatcher.compareActualValues(fLHS, fRHS, fComparator,
                            (XSSimpleTypeDecl) SchemaDVFactory.getInstance().getBuiltInType("string"));
                }
                else {
                    type1 = (XSSimpleTypeDecl) SchemaDVFactory.getInstance().
                                                        getBuiltInType("decimal");
                    try {
                        actualVal1 = type1.validate(fLHS, null, null);
                        actualVal2 = type1.validate(fRHS, null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }



            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE_VALUE_AS_CAST:
                type2 = getCastedType(fRHS);
                try {
                    //convert both values to the casted type
                    actualVal1 = type2.validate(getCastedValue(fLHS), null, null);
                    actualVal2 = type2.validate(getCastedValue(fRHS), null, null);
                    return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type2);
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE_AS_CAST_VALUE:
                type1 = getCastedType(fLHS);
                try {
                    //convert both values to the casted type
                    actualVal1 = type1.validate(getCastedValue(fLHS), null, null);
                    actualVal2 = type1.validate(getCastedValue(fRHS), null, null);
                    return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE_AS_CAST_VALUE_AS_CAST:
                type1 = getCastedType(fLHS);
                type2 = getCastedType(fRHS);

                //check whether the two casted types are comparable
                if (DataMatcher.isComparable(
                        type1.getBuiltInKind(),
                        type2.getBuiltInKind(),
                        null,
                        null)) {
                    try {
                        actualVal1 = type1.validate(getCastedValue(fLHS), null, null);
                        actualVal2 = type1.validate(getCastedValue(fRHS), null, null);
                        return DataMatcher.compareActualValues(actualVal1, actualVal2, fComparator, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return false;

            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE:
                if (fNumeric) {
                    //if numeric treat as decimal
                    type1 = (XSSimpleTypeDecl) SchemaDVFactory.getInstance().getBuiltInType("decimal");
                    try {
                        //see whether the value is numerically equal to 0
                        actualVal1 = type1.validate(fLHS, null, null);
                        actualVal2 = type1.validate("0", null, null);
                        return !DataMatcher.compareActualValues(actualVal1, actualVal2,
                                Tokens.EXPRTOKEN_COMPARATOR_EQUAL, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return true;

            case SyntaxTreeBuilder.TEST_CATEGORY_VALUE_AS_CAST:
                type1 = getCastedType(fLHS);
                try {
                    //perform the cast
                    actualVal1 = type1.validate(getCastedValue(fLHS), null, null);
                    actualVal2 = type1.validate("0", null, null);
                }
                catch (InvalidDatatypeValueException e) {
                    return false;
                }

                if (actualVal1 == null) {
                    return false;
                }
                else if (type1.getNumeric()) {
                    return !DataMatcher.compareActualValues(actualVal1, actualVal2,
                            Tokens.EXPRTOKEN_COMPARATOR_EQUAL, type1);
                }


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR:
                aug1 = attributes.getAugmentations(fLHS);
                if (aug1 == null) {
                    return false;
                }
                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = (XSSimpleTypeDecl) attr1.getTypeDefinition();
                actualVal1 = attr1.getActualNormalizedValue();
                if (actualVal1 == null) {
                    return false;
                }

                if (type1.getNumeric()) {
                    try {
                        actualVal2 = type1.validate("0", null, null);
                        return !DataMatcher.compareActualValues(actualVal1, actualVal2,
                                Tokens.EXPRTOKEN_COMPARATOR_EQUAL, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return true;


            case SyntaxTreeBuilder.TEST_CATEGORY_ATTR_AS_CAST:
                aug1 = attributes.getAugmentations(getCastedValue(fLHS));
                if (aug1 == null) {
                    return false;
                }
                attr1 = (AttributePSVImpl) aug1.getItem(Constants.ATTRIBUTE_PSVI);
                type1 = getCastedType(fLHS);
                actualVal1 = attr1.getActualNormalizedValue();
                if (actualVal1 == null) {
                    return false;
                }

                if (type1.getNumeric()) {
                    try {
                        actualVal1 = type1.validate(attributes.getValue(getCastedValue(fLHS)), null, null);
                        actualVal2 = type1.validate("0", null, null);
                        return !DataMatcher.compareActualValues(actualVal1, actualVal2,
                                Tokens.EXPRTOKEN_COMPARATOR_EQUAL, type1);
                    }
                    catch (InvalidDatatypeValueException e) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private XSSimpleTypeDecl getCastedType(String castExpr) {
            int start = castExpr.indexOf(" cast as ", 0) + 9;
            int end = castExpr.indexOf(" ?", start);
            String qname = castExpr.substring(start, end);
            String local = qname;
            int colonIndex = qname.indexOf(':');
            if (colonIndex != -1) {
                local = qname.substring(colonIndex + 1);
            }
            return (XSSimpleTypeDecl) SchemaDVFactory.getInstance().getBuiltInType(local);
        }

        private String getCastedValue(String castExpr) {
            int end = castExpr.indexOf(" cast as ", 0);
            return castExpr.substring(0, end);
        }

    }

    /**
     * This class provides the necessary means to compare actual values. This
     * functionality is used during the XPath evaluation.
     *
     * @author Hiranya Jayathilaka, University of Moratuwa
     */
    private static class DataMatcher {

        public static boolean compareActualValues(Object value1, Object value2, int comparator,
                XSSimpleTypeDecl type) {

            TypeValidator typeValidator = type.getTypeValidator();
            short ordered = type.getOrdered();

            if (ordered == XSSimpleTypeDecl.ORDERED_FALSE) {
                //if the type is not ordered then only equality can be tested
                //delegate the test to the type
                if (comparator == Tokens.EXPRTOKEN_COMPARATOR_EQUAL) {
                    return type.isEqual(value1, value2);
                }
                else if (comparator == Tokens.EXPRTOKEN_COMPARATOR_NOT_EQUAL) {
                    return !type.isEqual(value1, value2);
                }
                else {
                    //only equality can be tested upon unordered types
                    return false;
                }
            }

            //if the type is ordered then the corresponding TypeValidator should
            //know how to compare the values
            switch (comparator) {

            case Tokens.EXPRTOKEN_COMPARATOR_EQUAL:
                return typeValidator.compare(value1, value2) == 0;

            case Tokens.EXPRTOKEN_COMPARATOR_NOT_EQUAL:
                return typeValidator.compare(value1, value2) != 0;

            case Tokens.EXPRTOKEN_COMPARATOR_GREATER:
                return typeValidator.compare(value1, value2) > 0;

            case Tokens.EXPRTOKEN_COMPARATOR_GREATER_EQUAL:
                return typeValidator.compare(value1, value2) >= 0;

            case Tokens.EXPRTOKEN_COMPARATOR_LESS:
                return typeValidator.compare(value1, value2) < 0;

            case Tokens.EXPRTOKEN_COMPARATOR_LESS_EQUAL:
                return typeValidator.compare(value1, value2) <= 0;
            }
            return false;
        }

        public static boolean compareLists(XSSimpleTypeDecl type) {
            return false;
        }

        /**
         * Checks whether two specified data types are comparable. The types passed
         * into this method should be defined in XSConstants as *_DT values.
         */
        public static boolean isComparable(short type1, short type2,
                ShortList typeList1, ShortList typeList2) {

            short primitiveType1 = convertToPrimitiveKind(type1);
            short primitiveType2 = convertToPrimitiveKind(type2);

            if (primitiveType1 != primitiveType2) {
                return (primitiveType1 == XSConstants.ANYSIMPLETYPE_DT && primitiveType2 == XSConstants.STRING_DT ||
                        primitiveType1 == XSConstants.STRING_DT && primitiveType2 == XSConstants.ANYSIMPLETYPE_DT);
            }
            else if (primitiveType1 == XSConstants.LIST_DT || primitiveType1 == XSConstants.LISTOFUNION_DT) {
                final int typeList1Length = typeList1 != null ? typeList1.getLength() : 0;
                final int typeList2Length = typeList2 != null ? typeList2.getLength() : 0;
                if (typeList1Length != typeList2Length) {
                    return false;
                }
                for (int i = 0; i < typeList1Length; ++i) {
                    final short primitiveItem1 = convertToPrimitiveKind(typeList1.item(i));
                    final short primitiveItem2 = convertToPrimitiveKind(typeList2.item(i));
                    if (primitiveItem1 != primitiveItem2) {
                        if (primitiveItem1 == XSConstants.ANYSIMPLETYPE_DT && primitiveItem2 == XSConstants.STRING_DT ||
                            primitiveItem1 == XSConstants.STRING_DT && primitiveItem2 == XSConstants.ANYSIMPLETYPE_DT) {
                            continue;
                        }
                        return false;
                    }
                }
            }
            return true;
        }

        private static short convertToPrimitiveKind(short valueType) {
            //Primitive data types
            if (valueType <= XSConstants.NOTATION_DT) {
                return valueType;
            }
            // Types derived from string
            if (valueType <= XSConstants.ENTITY_DT) {
                return XSConstants.STRING_DT;
            }
            // Types derived from decimal
            if (valueType <= XSConstants.POSITIVEINTEGER_DT) {
                return XSConstants.DECIMAL_DT;
            }
            // Other types
            return valueType;
        }
    }

}
