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

package org.apache.xerces.impl.v2;

import org.apache.xerces.impl.v2.datatypes.IDDatatypeValidator;

/**
 * The XML representation for an attribute group declaration
 * schema component is a global <attributeGroup> element information item
 *
 * @author Sandy Gao, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class XSAttributeGroupDecl {

    // name of the attribute group
    public String fName = null;
    // target namespace of the attribute group
    public String fTargetNamespace = null;
    // number of attribute uses included by this attribute group
    int fAttrUseNum = 0;
    // attribute uses included by this attribute group
    private static final int INITIAL_SIZE = 5;
    XSAttributeUse[] fAttributeUses = new XSAttributeUse[INITIAL_SIZE];
    // attribute wildcard included by this attribute group
    public XSWildcardDecl fAttributeWC = null;
    // whether there is an attribute use whose type is or is derived from ID.
    public String fIDAttrName = null;

    void addAttributeUse(XSAttributeUse attrUse) {
        if (fAttrUseNum == fAttributeUses.length) {
            fAttributeUses = resize(fAttributeUses, fAttrUseNum*2);
        }
        fAttributeUses[fAttrUseNum++] = attrUse;
        if (fIDAttrName == null &&
            attrUse.fAttrDecl.fType instanceof IDDatatypeValidator) {
            fIDAttrName = attrUse.fAttrDecl.fName;
        }
    }

    public XSAttributeUse getAttributeUse(String uri, String localpart) {
    	for (int i=0; i<fAttrUseNum; i++) {
    		if ( (fAttributeUses[i].fAttrDecl.fTargetNamespace == uri) &&
    		     (fAttributeUses[i].fAttrDecl.fName == localpart) )
    			return fAttributeUses[i];
    	}
    	
    	return null;
    }

    public XSAttributeUse[] getAttributeUses() {
        if (fAttrUseNum < fAttributeUses.length) {
            fAttributeUses = resize(fAttributeUses, fAttrUseNum);
        }
        return fAttributeUses;
    }

   // Check that the attributes in this group validly restrict those from a base group  
   // If an error is found, the error code is returned. 
   public String validRestrictionOf(XSAttributeGroupDecl baseGroup) {

        String errorCode = null;
        for (int i=0; i<fAttrUseNum; i++) {

           XSAttributeUse attrUse = fAttributeUses[i];
           XSAttributeDecl attrDecl = attrUse.fAttrDecl;

           // Look for a match in the base
           XSAttributeUse baseAttrUse = baseGroup.getAttributeUse(
                                    attrDecl.fTargetNamespace,attrDecl.fName);
           if (baseAttrUse != null) {
             //
             // derivation-ok-restriction.  Constraint 2.1.1
             //
             if (baseAttrUse.fUse == SchemaSymbols.USE_REQUIRED && 
                 attrUse.fUse != SchemaSymbols.USE_REQUIRED) {
               errorCode = "derivation-ok-restriction.2.1.1";
               return errorCode;
             }

             XSAttributeDecl baseAttrDecl = baseAttrUse.fAttrDecl;
             //
             // derivation-ok-restriction.  Constraint 2.1.1
             //
             if (! XSConstraints.checkSimpleDerivationOk(attrDecl.fType,
                                           baseAttrDecl.fType,
                                           baseAttrDecl.fType.getFinalSet()) ) {
             	errorCode="derivation-ok-restriction.2.1.2";
             	return errorCode;
             }

             
             //
             // derivation-ok-restriction.  Constraint 2.1.3
             //
             if (baseAttrDecl.fConstraintType == XSAttributeDecl.FIXED_VALUE && 
                 attrDecl.fConstraintType != XSAttributeDecl.FIXED_VALUE) {
               errorCode="derivation-ok-restriction.2.1.3";
               return errorCode;
             }
           }
           else {
             // No matching attribute in base - there should be a matching wildcard


             //
             // derivation-ok-restriction.  Constraint 2.2
             //
             if (baseGroup.fAttributeWC == null ||
                 (!baseGroup.fAttributeWC.allowNamespace(attrDecl.fTargetNamespace))) {

               errorCode = "derivation-ok-restriction.2.2";
               return errorCode;
             }
           }
        }               
            

        // Now, check wildcards
        //
        // derivation-ok-restriction.  Constraint 4
        //
        if (fAttributeWC != null) {
          if (baseGroup.fAttributeWC == null) {
            errorCode = "derivation-ok-restriction.4";
            return errorCode;
          }
          if (! fAttributeWC.isSubsetOf(baseGroup.fAttributeWC)) {
            errorCode="derivation-ok-restriction.4";
            return errorCode;
          }
        }
    
        return null; 

   }

    static final XSAttributeUse[] resize(XSAttributeUse[] oldArray, int newSize) {
        XSAttributeUse[] newArray = new XSAttributeUse[newSize];
        System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newSize));
        return newArray;
    }

} // class XSAttributeGroupDecl
