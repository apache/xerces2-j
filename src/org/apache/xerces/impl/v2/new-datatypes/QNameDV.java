/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.v2.new-datatypes;

//internal imports
import org.apache.xerces.util.XMLChar;

import java.util.Locale;
import java.text.Collator;

/**
 * @version $Id$
 */
public  class QNameDV extends AbstractStringDV{

    protected DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    protected Locale fLocale = null;


    // for most DV classes, this is the same as the DV_?? value defined
    // in XSSimpleTypeDecl that's corresponding to that class. But for
    // ID/IDREF/ENTITY, the privitivaDV is DV_STRING.

    public short getPrimitiveDV(){
        return XSSimpleTypeDecl.DV_QNAME;
    }


    // convert a string to a compiled form. for example,
    // for number types (decimal, double, float, and types derived from them),
    // get the BigDecimal, Double, Flout object.
    // for some types (string and derived), they just return the string itself
    public Object getCompiledValue(String content) throws InvalidDatatypeValueException{
        //NCNames check should be done here.
        boolean NCName = false;
        int posColon = content.indexOf(':');
        if (posColon >= 0){
            if( !XMLChar.isValidNCName(content.substring(0,posColon)) ||
                !XMLChar.isValidNCName(content.substring(posColon+1))){
                NCName = false;
            }
        }
        if(!NCName)
            throw new InvalidDatatypeValueException("Value '"+content+"' is not a valid QName");

        return content;

    }//getCompiledValue()


    // the parameters are in compiled form (from getCompiledValue)
    public boolean isEqual(Object value1, Object value2){
        return ((String)value1).equals((String)value2);
    }

    // the following methods might not be supported by every DV.
    // but XSSimpleTypeDecl should know which type supports which methods,
    // and it's an *internal* error if a method is called on a DV that
    // doesn't support it.


    public  int compare(Object value1, Object value2) {
        Locale    loc       = Locale.getDefault();
        Collator  collator  = Collator.getInstance( loc );
        return collator.compare( (String)value1 , (String)value2 );
    }



} // class QNameDVDV
