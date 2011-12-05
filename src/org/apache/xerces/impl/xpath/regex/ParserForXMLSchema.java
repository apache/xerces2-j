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

package org.apache.xerces.impl.xpath.regex;

import java.util.Locale;

import org.apache.xerces.impl.Constants;

/**
 * A regular expression parser for the XML Schema.
 * 
 * @xerces.internal
 *
 * @author TAMURA Kent &lt;kent@trl.ibm.co.jp&gt;
 * @version $Id$
 */
class ParserForXMLSchema extends RegexParser {

    private RangeTokenMap xmlMap = null;
    private short xmlVersion = Constants.XML_VERSION_1_0;

    public ParserForXMLSchema() {
        //this.setLocale(Locale.getDefault());
    }
    public ParserForXMLSchema(Locale locale) {
        super(locale);
    }
    public ParserForXMLSchema(Locale locale, short datatypeXMLVersion) {
        super(locale);
        xmlVersion = datatypeXMLVersion;
    }

    Token processCaret() throws ParseException {
        this.next();
        return Token.createChar('^');
    }
    Token processDollar() throws ParseException {
        this.next();
        return Token.createChar('$');
     }
    Token processLookahead() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processNegativelookahead() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processLookbehind() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processNegativelookbehind() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_A() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_Z() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_z() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_b() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_B() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_lt() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_gt() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processStar(Token tok) throws ParseException {
        this.next();
        return Token.createClosure(tok);
    }
    Token processPlus(Token tok) throws ParseException {
        // X+ -> XX*
        this.next();
        return Token.createConcat(tok, Token.createClosure(tok));
    }
    Token processQuestion(Token tok) throws ParseException {
        // X? -> X|
        this.next();
        Token par = Token.createUnion();
        par.addChild(tok);
        par.addChild(Token.createEmpty());
        return par;
    }
    boolean checkQuestion(int off) {
        return false;
    }
    Token processParen() throws ParseException {
        this.next();
        Token tok = Token.createParen(this.parseRegex(), 0);
        if (this.read() != T_RPAREN)  throw ex("parser.factor.1", this.offset-1);
        this.next();                            // Skips ')'
        return tok;
    }
    Token processParen2() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processCondition() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processModifiers() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processIndependent() throws ParseException {
        throw ex("parser.process.1", this.offset);
    }
    Token processBacksolidus_c() throws ParseException {
        this.next();
        return this.getTokenForShorthand('c');
    }
    Token processBacksolidus_C() throws ParseException {
        this.next();
        return this.getTokenForShorthand('C');
    }
    Token processBacksolidus_i() throws ParseException {
        this.next();
        return this.getTokenForShorthand('i');
    }
    Token processBacksolidus_I() throws ParseException {
        this.next();
        return this.getTokenForShorthand('I');
    }
    Token processBacksolidus_g() throws ParseException {
        throw this.ex("parser.process.1", this.offset-2);
    }
    Token processBacksolidus_X() throws ParseException {
        throw ex("parser.process.1", this.offset-2);
    }
    Token processBackreference() throws ParseException {
        throw ex("parser.process.1", this.offset-4);
    }

    int processCIinCharacterClass(RangeToken tok, int c) {
        tok.mergeRanges(this.getTokenForShorthand(c));
        return -1;
    }


    /**
     * Parses a character-class-expression, not a character-class-escape.
     *
     * c-c-expression   ::= '[' c-group ']'
     * c-group          ::= positive-c-group | negative-c-group | c-c-subtraction
     * positive-c-group ::= (c-range | c-c-escape)+
     * negative-c-group ::= '^' positive-c-group
     * c-c-subtraction  ::= (positive-c-group | negative-c-group) subtraction
     * subtraction      ::= '-' c-c-expression
     * c-range          ::= single-range | from-to-range
     * single-range     ::= multi-c-escape | category-c-escape | block-c-escape | &lt;any XML char&gt;
     * cc-normal-c      ::= &lt;any character except [, ], \&gt;
     * from-to-range    ::= cc-normal-c '-' cc-normal-c
     *
     * @param useNrage Ignored.
     * @return This returns no NrageToken.
     */
    protected RangeToken parseCharacterClass(boolean useNrange) throws ParseException {
        boolean hyphen11 = isSet(RegularExpression.HYPHEN_IN_SCHEMA_11);
        this.setContext(S_INBRACKETS);
        this.next();                            // '['
        boolean nrange = false;
        boolean wasDecoded = false;     		// used to detect if the last - was escaped.
        RangeToken base = null;
        RangeToken tok;
        if (this.read() == T_CHAR && this.chardata == '^') {
            nrange = true;
            this.next();                        // '^'
            base = Token.createRange();
            base.addRange(0, Token.UTF16_MAX);
            tok = Token.createRange();
        } else {
            tok = Token.createRange();
        }
        int type;
        boolean firstloop = true;
        while ((type = this.read()) != T_EOF) { // Don't use 'cotinue' for this loop.
        	
        	wasDecoded = false;
            // single-range | from-to-range | subtraction
            if (type == T_CHAR && this.chardata == ']' && !firstloop) {
                if (nrange) {
                    base.subtractRanges(tok);
                    tok = base;
                }
                break;
            }
            int c = this.chardata;
            boolean end = false;
            if (type == T_BACKSOLIDUS) {
                switch (c) {
                  case 'd':  case 'D':
                  case 'w':  case 'W':
                  case 's':  case 'S':
                    tok.mergeRanges(this.getTokenForShorthand(c));
                    end = true;
                    break;

                  case 'i':  case 'I':
                  case 'c':  case 'C':
                    c = this.processCIinCharacterClass(tok, c);
                    if (c < 0)  end = true;
                    break;
                    
                  case 'p':
                  case 'P':
                    int pstart = this.offset;
                    RangeToken tok2 = this.processBacksolidus_pP(c);
                    if (tok2 == null) {
                        if (this.isSet(RegularExpression.ALLOW_UNRECOGNIZED_BLOCK_NAME)) {
                            tok2 = Token.token_all;
                        }
                        else {
                            throw this.ex("parser.atom.5", pstart);
                        }
                    }
                    tok.mergeRanges(tok2);
                    end = true;
                    break;
                   
                 case '-':
                 	c = this.decodeEscaped();
                 	wasDecoded = true;
                 	break;

                  default:
                    c = this.decodeEscaped();
                } // \ + c
            } // backsolidus
            else if (type == T_XMLSCHEMA_CC_SUBTRACTION && !firstloop) {
                                                // Subraction
                if (nrange) {
                    base.subtractRanges(tok);
                    tok = base;
                }
                RangeToken range2 = this.parseCharacterClass(false);
                tok.subtractRanges(range2);
                if (this.read() != T_CHAR || this.chardata != ']')
                    throw this.ex("parser.cc.5", this.offset);
                break;                          // Exit this loop
            }
            this.next();
            if (!end) {                         // if not shorthands...
                if (type == T_CHAR) {
                    if (c == '[')  throw this.ex("parser.cc.6", this.offset-2);
                    if (c == ']')  throw this.ex("parser.cc.7", this.offset-2);
                    if (!hyphen11 && c == '-' && this.chardata != ']' && !firstloop)  throw this.ex("parser.cc.8", this.offset-2);	// if regex = '[-]' then invalid
                }
                if (this.read() != T_CHAR || this.chardata != '-' || !hyphen11 && c == '-' && !wasDecoded && firstloop) { // Here is no '-'.
                    if (!this.isSet(RegularExpression.IGNORE_CASE) || c > 0xffff) {
                        tok.addRange(c, c);
                    }
                    else {
                        addCaseInsensitiveChar(tok, c);
                    }
                } else {                        // Found '-'
                                                // Is this '-' is a from-to token??
                    this.next(); // Skips '-'
                    if ((type = this.read()) == T_EOF)  throw this.ex("parser.cc.2", this.offset);
                                                // c '-' ']' -> '-' is a single-range.
                    if(type == T_CHAR && this.chardata == ']' ||
                       hyphen11 && type == T_XMLSCHEMA_CC_SUBTRACTION) {				// if - is at the last position of the group
                        if (!this.isSet(RegularExpression.IGNORE_CASE) || c > 0xffff) {
                    	    tok.addRange(c, c);
                        }
                        else {
                            addCaseInsensitiveChar(tok, c);
                        }
                    	tok.addRange('-', '-');
                    }
                    else if (type == T_XMLSCHEMA_CC_SUBTRACTION) {
                        throw this.ex("parser.cc.8", this.offset-1);
                    } else {
                        if (hyphen11 && c == '-' && !wasDecoded) {
                            throw this.ex("parser.cc.4", this.offset-2);
                        }
                    	
                        int rangeend = this.chardata;
                        if (type == T_CHAR) {
                            if (rangeend == '[')  throw this.ex("parser.cc.6", this.offset-1);
                            if (rangeend == ']')  throw this.ex("parser.cc.7", this.offset-1);
                            if (rangeend == '-')  throw this.ex("parser.cc.8", this.offset-2);
                        }
                        else if (type == T_BACKSOLIDUS)
                            rangeend = this.decodeEscaped();
                        this.next();

                        if (c > rangeend)  throw this.ex("parser.ope.3", this.offset-1);
                        if (!this.isSet(RegularExpression.IGNORE_CASE) ||
                                (c > 0xffff && rangeend > 0xffff)) {
                            tok.addRange(c, rangeend);
                        }
                        else {
                            addCaseInsensitiveCharRange(tok, c, rangeend);
                        }
                    }
                }
            }
            firstloop = false;
        }
        if (this.read() == T_EOF)
            throw this.ex("parser.cc.2", this.offset);
        tok.sortRanges();
        tok.compactRanges();
        //tok.dumpRanges();
        this.setContext(S_NORMAL);
        this.next();                    // Skips ']'

        return tok;
    }

    protected RangeToken parseSetOperations() throws ParseException {
        throw this.ex("parser.process.1", this.offset);
    }
 
    Token getTokenForShorthand(int ch) {
        if (xmlMap == null) {
            xmlMap = RangeTokenMapFactory.getXMLTokenMap(xmlVersion);
        }
        switch (ch) {
          case 'd':
            return xmlMap.get("xml:isDigit", true);
          case 'D':
            return xmlMap.get("xml:isDigit", false);
          case 'w':
            return xmlMap.get("xml:isWord", true);
          case 'W':
            return xmlMap.get("xml:isWord", false);
          case 's':
            return xmlMap.get("xml:isSpace", true);
          case 'S':
            return xmlMap.get("xml:isSpace", false);
          case 'c':
            return xmlMap.get("xml:isNameChar", true);
          case 'C':
            return xmlMap.get("xml:isNameChar", false);
          case 'i':
            return xmlMap.get("xml:isInitialNameChar", true);
          case 'I':
            return xmlMap.get("xml:isInitialNameChar", false);
          default:
            throw new RuntimeException("Internal Error: shorthands: \\u"+Integer.toString(ch, 16));
        }
    }
    int decodeEscaped() throws ParseException {
        if (this.read() != T_BACKSOLIDUS)  throw ex("parser.next.1", this.offset-1);
        int c = this.chardata;
        switch (c) {
          case 'n':  c = '\n';  break; // LINE FEED U+000A
          case 'r':  c = '\r';  break; // CRRIAGE RETURN U+000D
          case 't':  c = '\t';  break; // HORIZONTAL TABULATION U+0009
          case '\\':
          case '|':
          case '.':
          case '^':
          case '-':
          case '?':
          case '*':
          case '+':
          case '{':
          case '}':
          case '(':
          case ')':
          case '[':
          case ']':
            break; // return actucal char
          default:
            throw ex("parser.process.1", this.offset-2);
        }
        return c;
    }
}
