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

import java.util.HashMap;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
final class XML11TokenMap implements RangeTokenMap {

    private HashMap ranges;
    private HashMap ranges2;
    
    private XML11TokenMap() {
        createRanges();
    }
    
    static RangeTokenMap instance() {
        return new XML11TokenMap();
    }
    
    private void createRanges() {
        ranges = new HashMap();
        ranges2 = new HashMap();

        Token tok = Token.createRange();
        REUtil.setupRange(tok, REConstants.SPACES);
        ranges.put("xml:isSpace", tok);
        ranges2.put("xml:isSpace", Token.complementRanges(tok));

        tok = Token.createRange();
        REUtil.setupRange(tok, REConstants.DIGITS_INTS);
        ranges.put("xml:isDigit", tok);
        ranges2.put("xml:isDigit", Token.complementRanges(tok));

        /*
         * \w is defined by the XML Schema specification to be:
         * [#x0000-#x10FFFF]-[\p{P}\p{Z}\p{C}] (all characters except the set of "punctuation", "separator" and "other" characters) 
         */
        tok = Token.createRange();
        tok.mergeRanges(Token.getRange("P", true));
        tok.mergeRanges(Token.getRange("Z", true));
        tok.mergeRanges(Token.getRange("C", true));
        ranges2.put("xml:isWord", tok);
        ranges.put("xml:isWord", Token.complementRanges(tok));

        tok = Token.createRange();
        REUtil.setupRange(tok, REConstants.NAMECHARS11_INTS);
        ranges.put("xml:isNameChar", tok);
        ranges2.put("xml:isNameChar", Token.complementRanges(tok));
            
        tok = Token.createRange();
        REUtil.setupRange(tok, REConstants.NAMESTARTCHARS11_INTS);
        ranges.put("xml:isInitialNameChar", tok);
        ranges2.put("xml:isInitialNameChar", Token.complementRanges(tok));
    }
    
    public RangeToken get(String name, boolean positive) {
        return (positive ? (RangeToken)ranges.get(name)
                         : (RangeToken)ranges2.get(name));
    }

    
}
