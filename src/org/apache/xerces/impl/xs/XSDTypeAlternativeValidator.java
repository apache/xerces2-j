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

package org.apache.xerces.impl.xs;

import java.util.Vector;

import org.apache.xerces.impl.xs.alternative.Test;
import org.apache.xerces.impl.xs.alternative.XSTypeAlternativeImpl;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * An XML Schema validator subcomponent handling "type alternative" processing.
 * 
 * @xerces.internal
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @author Mukul Gandhi IBM
 * @version $Id$
 */
public class XSDTypeAlternativeValidator {          
    
    
    /*
     * Class constructor.
     */
    public XSDTypeAlternativeValidator() {
       // NO OP ...
    }
    
    
    /*
     * Determine the schema type applicable for an element declaration, using type alternative information.
     */
    public XSTypeDefinition getCurrentType(XSElementDecl currentElemDecl, QName element, XMLAttributes attributes, Vector inheritableAttrList) {
        
        XSTypeDefinition currentType = null;        
        
        XSTypeAlternativeImpl[] typeAlternatives = currentElemDecl.getTypeAlternatives();        
        if (typeAlternatives != null) {              
            // Construct a list of attributes needed for CTA processing. This includes inherited attributes as well.
            XMLAttributes ctaAttributes = getAttributesForCTA(attributes, inheritableAttrList);
            for (int typeAltIdx = 0; typeAltIdx < typeAlternatives.length; typeAltIdx++) {
                Test ctaTest = typeAlternatives[typeAltIdx].getTest();
                if (ctaTest != null && ctaTest.evaluateTest(element, ctaAttributes)) {
                    currentType = typeAlternatives[typeAltIdx].getTypeDefinition();
                    break;
                }
            }
            //if a type is not selected by xs:alternative components, try to assign the default type
            if (currentType == null) {
                XSTypeAlternativeImpl defType = currentElemDecl.getDefaultTypeDefinition();
                if (defType != null) {
                    currentType = defType.getTypeDefinition();
                }
            }
        }
        
        return currentType;
        
    } // getCurrentType
    

    /*
     * Construct a list of attributes, needed for CTA processing. This includes inherited attributes as well.  
     */
    private XMLAttributes getAttributesForCTA(XMLAttributes attributes, Vector inheritableAttrList) {

        XMLAttributes ctaAttributes = new XMLAttributesImpl();
        
        // copy attributes from the original list of attributes        
        for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
            QName attrQName = new QName();
            attributes.getName(attrIndx, attrQName);
            ctaAttributes.addAttribute(attrQName, attributes.getType(attrIndx), attributes.getValue(attrIndx));   
        }

        // add inherited attributes to the CTA attributes list
        for (int elemIndx = inheritableAttrList.size() - 1; elemIndx > -1; elemIndx--) {        
            AttributePSVI inhAttrPsvi = (AttributePSVI) inheritableAttrList.elementAt(elemIndx);
            XSAttributeDeclaration inhrAttrDecl = inhAttrPsvi.getAttributeDeclaration();
            // if an inherited attribute is not overridden by the current element, add it to the CTA attributes list
            if (!isInheritedAttributeOverridden(ctaAttributes, inhrAttrDecl)) {                
                QName attrQName = new QName();
                attrQName.setValues(null, inhrAttrDecl.getName(), inhrAttrDecl.getName(), inhrAttrDecl.getNamespace());                
                ctaAttributes.addAttribute(attrQName, null, inhAttrPsvi.getSchemaValue().getNormalizedValue());
            }
        }

        return ctaAttributes;
      
    } // getAttributesForCTA

    
    /*
     * Check if an inherited attribute already exists in the current attributes list.
     */
    private boolean isInheritedAttributeOverridden(XMLAttributes attributes, XSAttributeDeclaration inhrAttrDecl) {      
        boolean attrExists = false;
        for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {        
            if ((attributes.getLocalName(attrIndx)).equals(inhrAttrDecl.getName()) && XSTypeHelper.isURIEqual(attributes.getURI(attrIndx), inhrAttrDecl.getNamespace())) {              
                attrExists = true;
                break;
            }
        }
        return attrExists;      
    } // isInheritedAttributeOverridden
    
} // class XSDTypeAlternativeValidator
