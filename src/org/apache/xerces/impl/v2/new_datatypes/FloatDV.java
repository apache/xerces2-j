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

package org.apache.xerces.impl.v2.new_datatypes;

//internal imports
import org.apache.xerces.impl.v2.XSSimpleTypeDecl;

//java import

/**
 * @version $Id$
 */
public class FloatDV extends AbstractNumericDV{
    // for most DV classes, this is the same as the DV_?? value defined
    // in XSSimpleTypeDecl that's corresponding to that class. But for
    // ID/IDREF/ENTITY, the privitivaDV is DV_STRING.
    public short getPrimitiveDV(){
        return XSSimpleTypeDecl.DV_FLOAT;
    }

    //convert a String to Float form, we have to take care of cases specified in spec like INF, -INF and NaN
    public Object getCompiledValue(String content) throws InvalidDatatypeValueException{

        Float compiledValue = null;

        try{
            compiledValue = fValueOf(content);
        }catch(Exception ex){
        String msg = getErrorString(
                                           DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.NOT_FLOAT ],
                                           new Object[] { "'" + content +"'"});
        throw new InvalidDatatypeValueException(msg);
        }
        return compiledValue;
    }//getCompiledValue()


    // Float equals method takes care of cases specified for Float in schema spec.
    public boolean isEqual(Object value1, Object value2){
        if(value1 instanceof Float && value2 instanceof Float)
            return ((Float)value1).equals((Float)value2);
        else //REVISIT: should we throw error for not getting right object or to be taken care in XSSimpleTypeDecl.
            return false;
    }//isEqual()

    // the following methods might not be supported by every DV.
    // but XSSimpleTypeDecl should know which type supports which methods,
    // and it's an *internal* error if a method is called on a DV that
    // doesn't support it.


    // Float compareTo method takes care of cases specified for Float in schema spec.
    public int compare(Object value1, Object value2){
	      if(value1 instanceof Float && value2 instanceof Float)
            return ((Float)value1).compareTo((Float)value2)  ;
        else
            return -1;
    }//compare()

    //takes care of special values positive, negative infinity and Not a Number as per the spec.
    private static Float fValueOf(String s) throws NumberFormatException {
        Float f=null;
        try {
            f = Float.valueOf(s);
        }
        catch ( NumberFormatException nfe ) {
            if ( s.equals("INF") ) {
                f = new Float(Float.POSITIVE_INFINITY);
            }
            else if ( s.equals("-INF") ) {
                f = new Float (Float.NEGATIVE_INFINITY);
            }
            else if ( s.equals("NaN" ) ) {
                f = new Float (Float.NaN);
            }
            else {
                throw nfe;
            }
        }
        return f;
    }



} // class FloatDV
