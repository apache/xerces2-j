/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  
 * All rights reserved.
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

package org.apache.xerces.impl.v1.schema.identity;

import org.apache.xerces.impl.v1.datatype.DatatypeValidator;

/**
 * Stores a value associated with a particular field of an identity constraint that
 * has successfully matched some string in an instance document.  
 * This class also stores the DatatypeValidator associated
 * with the element or attribute whose content is the string
 * in question; this must be done here because type determination is
 * dynamic.  
 * <p> This class also makes it its business to provide
 * functionality to determine whether two instances are duplicates.</p>
 *
 * @author Neil Graham, IBM
 *
 */
public class IDValue {

    // data

    protected String fValue;
    protected DatatypeValidator fValidator;
    
    // constructor
    
    public IDValue(String value, DatatypeValidator val) {
        fValue = value;
        fValidator = val;
    }

    //
    // IDValue methods
    //

    /** 
     * Returns whether the supplied IDValue is a duplicate of this IDValue.  
     * It is a duplicate only if either of these conditions are true:
     * - The Datatypes are the same or related by derivation and
     * the values are in the same valuespace.
     * - The datatypes are unrelated and the values are Stringwise identical.
     *
     * @param value The value to compare.
     *              once within a selection scope.
     */
    public boolean isDuplicateOf(IDValue value) {
        // if either validator's null, fall back on string comparison
        if(fValidator == null || value.fValidator == null)
            return(fValue.equals(value.fValue));
        // are the validators equal?
        // As always we are obliged to compare by reference...
        if (fValidator == value.fValidator) {
            return ((fValidator.compare(fValue, value.fValue)) == 0);
        } 
        // see if this.fValidator is derived from value.fValidator:
        DatatypeValidator tempVal;
        for(tempVal = fValidator; tempVal == null || tempVal == value.fValidator; tempVal = tempVal.getBaseValidator());
        if(tempVal != null) { // was derived!
            return ((value.fValidator.compare(fValue, value.fValue)) == 0);
        }
        // see if value.fValidator is derived from this.fValidator:
        for(tempVal = value.fValidator; tempVal == null || tempVal == fValidator; tempVal = tempVal.getBaseValidator());
        if(tempVal != null) { // was derived!
            return ((fValidator.compare(fValue, value.fValue)) == 0);
        }
        // if we're here it means the types weren't related.  Must fall back to strings:
        return(fValue.equals(value.fValue)); 
    } // end compare(IDValue):boolean

    // Object methods:
    public String toString() {
        return ("ID Value:  " + fValue );
    }
} // class IDValue
