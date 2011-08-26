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

import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;

/**
 * Lexical analyzer and parser for test XPath expressions. This parser 
 * implementation constructs a syntax tree for valid test XPath expressions
 * which can be later evaluated.
 * 
 * (Parser generated using JavaCC)
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class XPath20Parser {
    
    public static final int EOF = 0;
    public static final int KEYWORD_AND = 2;
    public static final int KEYWORD_OR = 3;
    public static final int KEYWORD_CAST = 4;
    public static final int KEYWORD_AS = 5;
    
    public static final int SYMBOL_COLON = 6;
    public static final int SYMBOL_AT = 7;
    public static final int SYMBOL_QUESTION = 8;
    public static final int OPEN_PARAN = 9;
    public static final int CLOSE_PARAN = 10;
    public static final int SYMBOL_EQ = 11;
    public static final int SYMBOL_NE = 12;
    public static final int SYMBOL_LT = 13;
    public static final int SYMBOL_GT = 14;
    public static final int SYMBOL_LE = 15;
    public static final int SYMBOL_GE = 16;
    
    public static final int NUMERIC_LITERAL = 17;
    public static final int DIGITS = 18;
    public static final int NCNAME = 19;
    public static final int NCNAME_START_CHAR = 20;
    public static final int NCNAME_CHAR = 21;
    public static final int ESCAPE_QUOTE = 22;
    public static final int ESCAPE_APOS = 23;
    public static final int STRING_LITERAL = 24;

    /** Lexical state. */
    public static final int DEFAULT = 0;

    /** Literal token values. */
    String[] tokenImage = {
      "<EOF>",
      "\" \"",
      "\"and\"",
      "\"or\"",
      "\"cast\"",
      "\"as\"",
      "\":\"",
      "\"@\"",
      "\"?\"",
      "\"(\"",
      "\")\"",
      "\"=\"",
      "\"!=\"",
      "\"<\"",
      "\">\"",
      "\"<=\"",
      "\">=\"",
      "<NUMERIC_LITERAL>",
      "<DIGITS>",
      "<NCNAME>",
      "<NCNAME_START_CHAR>",
      "<NCNAME_CHAR>",
      "\"\\\"\\\"\"",
      "\"\\\'\\\'\"",
      "<STRING_LITERAL>",
      "\"\\n\"",
    };

    protected final NamespaceContext fNsContext;

    public XPathSyntaxTreeNode parseExpression() throws XPathException {
        return Test();
    }

    private XPathSyntaxTreeNode Test() throws XPathException {
        XPathSyntaxTreeNode n;
        n = OrExpr();
        consumeToken(25);
        return n;
    }

    private XPathSyntaxTreeNode OrExpr() throws XPathException {
        XPathSyntaxTreeNode n1, n2;
        n1 = AndExpr();
        label_1: while (true) {
            switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
            case KEYWORD_OR:
                break;
                
            default:
                array1[0] = gen;
                break label_1;
            }
            consumeToken(KEYWORD_OR);
            n2 = AndExpr();
            return new ConjunctionNode(ConjunctionNode.OR, n1, n2);
        }
        return n1;
    }

    private XPathSyntaxTreeNode AndExpr() throws XPathException {
        XPathSyntaxTreeNode n1, n2;
        n1 = BooleanExpr();
        label_2: while (true) {
            switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
            case KEYWORD_AND:
                break;
                
            default:
                array1[1] = gen;
                break label_2;
            }
            
            consumeToken(KEYWORD_AND);
            n2 = BooleanExpr();
            return new ConjunctionNode(ConjunctionNode.AND, n1, n2);
        }
        return n1;
    }

    private XPathSyntaxTreeNode BooleanExpr() throws XPathException {
        XPathSyntaxTreeNode n1, n2;
        QName name;
        int comp;
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case OPEN_PARAN:
            consumeToken(OPEN_PARAN);
            n1 = OrExpr();
            consumeToken(CLOSE_PARAN);
            return n1;
            
        case NCNAME:
            name = QName();
            consumeToken(OPEN_PARAN);
            if ("not".equals(name.localpart) && "http://www.w3.org/2005/xpath-functions".equals(name.uri)) {
                n1 = OrExpr();
                consumeToken(CLOSE_PARAN);
                return new FunctionNode(name, n1);
            }
            n1 = SimpleValue();
            consumeToken(CLOSE_PARAN);
            n1 = new CastNode(n1, name);
            switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
            case SYMBOL_EQ:
            case SYMBOL_NE:
            case SYMBOL_LT:
            case SYMBOL_GT:
            case SYMBOL_LE:
            case SYMBOL_GE:
                comp = Comparator();
                n2 = ValueExpr();
                return new CompNode(comp, n1, n2);
                
            default:
                array1[2] = gen;
            }
            return n1;
        case SYMBOL_AT:
        case NUMERIC_LITERAL:
        case STRING_LITERAL:
            n1 = CastExpr();
            switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
            case SYMBOL_EQ:
            case SYMBOL_NE:
            case SYMBOL_LT:
            case SYMBOL_GT:
            case SYMBOL_LE:
            case SYMBOL_GE:
                comp = Comparator();
                n2 = CastExpr();
                return new CompNode(comp, n1, n2);
                
            default:
                array1[2] = gen;
            }
            return n1;
            
        default:
            array1[3] = gen;
            consumeToken(-1);
            throw new XPathException("c-general-xpath");
        }
    }

    private QName QName() throws XPathException {
        Token t1, t2;
        QName name;
        t1 = consumeToken(NCNAME);
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case SYMBOL_COLON:
            consumeToken(SYMBOL_COLON);
            t2 = consumeToken(NCNAME);
            // TODO: better way to intern the strings
            String prefix = t1.image.intern();
            String local = t2.image.intern();
            name = new QName(prefix, local, prefix + ':' + local, fNsContext.getURI(prefix));
            break;
        default:
            // TODO: better way to intern the strings
            local = t1.image.intern();
            name = new QName(null, local, local, null);
            array1[4] = gen;
        }
        return name;
    }

    private int Comparator() throws XPathException {
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case SYMBOL_EQ:
            consumeToken(SYMBOL_EQ);
            return CompNode.EQ;
            
        case SYMBOL_NE:
            consumeToken(SYMBOL_NE);
            return CompNode.NE;
            
        case SYMBOL_GT:
            consumeToken(SYMBOL_GT);
            return CompNode.GT;
            
        case SYMBOL_LT:
            consumeToken(SYMBOL_LT);
            return CompNode.LT;
            
        case SYMBOL_GE:
            consumeToken(SYMBOL_GE);
            return CompNode.GE;
            
        case SYMBOL_LE:
            consumeToken(SYMBOL_LE);
            return CompNode.LE;
            
        default:
            array1[5] = gen;
            consumeToken(-1);
            throw new XPathException("c-general-xpath");
        }
    }

    private XPathSyntaxTreeNode ValueExpr() throws XPathException {
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case NCNAME:
            QName name = QName();
            consumeToken(OPEN_PARAN);
            XPathSyntaxTreeNode n1 = SimpleValue();
            consumeToken(CLOSE_PARAN);
            return new CastNode(n1, name);
        case SYMBOL_AT:
        case NUMERIC_LITERAL:
        case STRING_LITERAL:
            return CastExpr();
        default:
            array1[3] = gen;
            consumeToken(-1);
            throw new XPathException("c-general-xpath");
        }
    }

    private XPathSyntaxTreeNode CastExpr() throws XPathException {
        XPathSyntaxTreeNode n;
        QName name;
        n = SimpleValue();
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case KEYWORD_CAST:
            consumeToken(KEYWORD_CAST);
            consumeToken(KEYWORD_AS);
            name = QName();
            switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
            case SYMBOL_QUESTION:
                consumeToken(SYMBOL_QUESTION);
                break;
            default:
                array1[6] = gen;
            }
            return new CastNode(n, name);
            
        default:
            array1[7] = gen;
        }
        return n;
    }

    private XPathSyntaxTreeNode SimpleValue() throws XPathException {
        XPathSyntaxTreeNode n;
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case SYMBOL_AT:
            n = AttrName();
            return n;
            
        case NUMERIC_LITERAL:
        case STRING_LITERAL:
            n = Literal();
            return n;
            
        default:
            array1[8] = gen;
            consumeToken(-1);
            throw new XPathException("c-general-xpath");
        }
    }

    private XPathSyntaxTreeNode AttrName() throws XPathException {
        consumeToken(SYMBOL_AT);
        return new AttrNode(NameTest());
    }

    private XPathSyntaxTreeNode Literal() throws XPathException {
        Token t;
        switch ((nextTokenIndex == -1) ? nextToken() : nextTokenIndex) {
        case NUMERIC_LITERAL:
            t = consumeToken(NUMERIC_LITERAL);
            return new LiteralNode(t.image, true);
            
        case STRING_LITERAL:
            t = consumeToken(STRING_LITERAL);
            return new LiteralNode(t.image.substring(1, t.image
                    .length() - 1), false);
            
        default:
            array1[9] = gen;
            consumeToken(-1);
            throw new XPathException("c-general-xpath");
        }
    }

    private QName NameTest() throws XPathException {
        QName t;
        t = QName();
        return t;
    }

    /** Generated Token Manager. */
    public XPath20ParserTokenManager tokenSource;
    SimpleCharStream inputStream;
    
    /** Current token. */
    public Token token;
    
    /** Next token. */
    public Token nextToken;
    private int nextTokenIndex;
    private int gen;
    final private int[] array1 = new int[10];
        
    /** Constructor. */
    public XPath20Parser(java.io.Reader stream, NamespaceContext nsContext) {
        fNsContext = nsContext;
        inputStream = new SimpleCharStream(stream, 1, 1);
        tokenSource = new XPath20ParserTokenManager(inputStream);
        token = new Token();
        nextTokenIndex = -1;
        gen = 0;
        for (int i = 0; i < 10; i++)
            array1[i] = -1;
    }

    private Token consumeToken(int kind) throws XPathException {
        Token oldToken;
        if ((oldToken = token).next != null) {
            token = token.next;
        }
        else {
            token = token.next = tokenSource.getNextToken();
        }
        nextTokenIndex = -1;
        if (token.kind == kind) {
            gen++;
            return token;
        }
        token = oldToken;
        throw new XPathException("c-general-xpath");
    }

    private int nextToken() throws XPathException {
        if ((nextToken = token.next) == null) {
            return (nextTokenIndex = (token.next = tokenSource.getNextToken()).kind);
        }
        else {
            return (nextTokenIndex = nextToken.kind);
        }
    }    
}

