
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
package org.apache.xerces.validators.schema;

import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.common.GrammarResolver;

import org.xml.sax.SAXException;

import java.lang.ClassCastException;
/* 
 * @author Eric Ye
 *         
 * @see    
 *
 * @version $Id$
 */

public class EquivClassComparator {

    // constants
    private final int TOP_LEVEL_SCOPE = -1;

    // private data members
    private StringPool fStringPool = null;
    private GrammarResolver fGrammarResolver = null;

    // constructors
    private EquivClassComparator(){
        // can never be instantiated without passing in a GrammarResolver.
    }
    public  EquivClassComparator(GrammarResolver grammarResolver, StringPool stringPool){
        fGrammarResolver = grammarResolver;
        fStringPool = stringPool;
    }

    //public methods
    public boolean isEquivalentTo(QName aElement, QName examplar) throws Exception{
        if (aElement.localpart==examplar.localpart && aElement.uri==examplar.uri ) {
            return true;
        }

        if (fGrammarResolver == null || fStringPool == null) {
            throw new SAXException("Try to check equivalency by equivClass, but no GrammarResolver is defined");
        }

        int count = 16; // 16 is the limit of times for which we'll check the equivClass transitively.
        int uriIndex = aElement.uri;
        int localpartIndex = aElement.localpart;
        String uri = fStringPool.toString(aElement.uri);
        String localpart = fStringPool.toString(aElement.localpart);

        while (count >= 0) {
            if(uri==null) {
                return false;
            }
            SchemaGrammar sGrammar = null;
            try {
                sGrammar = (SchemaGrammar) fGrammarResolver.getGrammar(uri);
            }
            catch ( ClassCastException ce) {
                //since the returen Grammar is not a SchemaGrammar, bail out
                return false;
            }
            if(sGrammar == null) return false;

            int elementIndex = sGrammar.getElementDeclIndex(uriIndex, localpartIndex, TOP_LEVEL_SCOPE);
            //System.out.println("----------equivClassFullName : " + uriIndex+","+localpartIndex+","+elementIndex);
            if (elementIndex == -1) {
                return false;
            }

            String equivClassFullName = sGrammar.getElementDeclEquivClassElementFullName(elementIndex);
            //System.out.println("----------equivClassFullName : " + equivClassFullName);
            if (equivClassFullName==null) {
                return false;
            }

            int commaAt = equivClassFullName.indexOf(","); 
            uri = "";
            localpart = equivClassFullName;
            if (  commaAt >= 0  ) {
                if (commaAt > 0 ) {
                    uri = equivClassFullName.substring(0,commaAt);
                }
                localpart = equivClassFullName.substring(commaAt+1);
            }
            uriIndex = fStringPool.addSymbol(uri);
            localpartIndex = fStringPool.addSymbol(localpart);

            if (uriIndex == examplar.uri && localpartIndex == examplar.localpart) {
                return true;
            }

            count--;
        }

        return false;
    }
}
