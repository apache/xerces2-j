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
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.util.IntStack;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * An XML Schema validator subcomponent handling 'type alternative' processing.
 * 
 * @xerces.internal
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @author Mukul Gandhi IBM
 * @version $Id: $
 */
public class XSDTypeAlternativeValidator {
    
    // a Vector list storing inheritable attributes
    private Vector fInheritableAttrList = new Vector();
    
    // a Stack, storing inheritable attribute count for the elements
    private IntStack fInhrAttrCountStack = new IntStack();
    
    // temporary qname
    private final QName fTempQName = new QName();
    
    
    /*
     * Class constructor
     */
    public XSDTypeAlternativeValidator() {
      // NO OP ...
    }
    
    
    /*
     * Determine the schema type applicable for an element declaration,
     * using type alternative information.
     */
    public XSTypeDefinition getCurrentType(XSElementDecl currentElemDecl, 
                                           QName element, 
                                           XMLAttributes attributes) {
        
        XSTypeDefinition currentType = null;        
        boolean typeSelected = false;
        
        XSTypeAlternativeImpl[] alternatives = currentElemDecl.
                                                   getTypeAlternatives();
        
        if (alternatives != null) {              
            // construct a list of attributes needed for CTA processing.
            // This method call adds inherited attributes as well, to the list
            // of attributes.
            XMLAttributes ctaAttributes = getAttributesForCTA(attributes);
            
            for (int i = 0; i < alternatives.length; i++) {
                Test test = alternatives[i].getTest();
                if (test != null && test.evaluateTest(element, 
                                                      ctaAttributes)) {
                    currentType = alternatives[i].getTypeDefinition();
                    typeSelected = true;
                    break;
                }
            }
            //if a type is not selected try to assign the default type
            if (!typeSelected) {
                XSTypeAlternativeImpl defType = currentElemDecl.
                                                   getDefaultTypeDefinition();
                if (defType != null) {
                    currentType = defType.getTypeDefinition();
                }
            }
        }
        
        return currentType;
        
    } // getCurrentType
    
    
    /*
     * Type alternative processing interface during the XNI event
     * 'handleStartElement' in XMLSchemaValidator. 
     */
    public void handleStartElement(XSElementDecl currentElemDecl, 
                                   XMLAttributes attributes) {
        
        fInhrAttrCountStack.push(fInheritableAttrList.size());
        
        // Find attributes among the attributes of the current element, which
        // are declared inheritable. The inheritable attributes will later be
        // used for processing 'type alternative' instruction.
        if (attributes.getLength() > 0) {
           // get inheritable attributes, only if an element has a complex
           // type (i.e, has > 0 attributes).
           saveInheritableAttributes(currentElemDecl, attributes);
        }
        
    } // handleStartElement
    
    
    /*
     * Type alternative processing interface during the XNI event
     * 'handleEndElement' in XMLSchemaValidator. 
     */
    public void handleEndElement() {
        
        // modify the Vector list 'fInheritableAttrList' and pop the stack,
        // 'fInhrAttrCountStack', to reflect inheritable attributes processing.
        if (fInhrAttrCountStack.size() > 0) {
            fInheritableAttrList.setSize(fInhrAttrCountStack.pop());
        }
        
    } // handleEndElement
    

    /*
     * Construct a list of attributes, needed for CTA processing. This includes
     * inherited attributes as well.  
     */
    private XMLAttributes getAttributesForCTA(XMLAttributes attributes) {

      // copy attributes from the original list of attributes
      XMLAttributes ctaAttributes = new XMLAttributesImpl();
      for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
         QName qName = new QName();
         attributes.getName(attrIndx, qName);
         ctaAttributes.addAttribute(qName, attributes.getType(attrIndx),
                                    attributes.getValue(attrIndx));   
      }
      
      // traverse up the XML tree, to find inherited attributes.
      // attributes only from the nearest ancestor, are added to the list.
      for (int elemIndx = fInheritableAttrList.size() - 1; elemIndx > -1; 
                                                             elemIndx--) {        
         InheritableAttribute inhAttr = (InheritableAttribute) 
                                  fInheritableAttrList.elementAt(elemIndx);
         // if an inheritable attribute doesn't already exist in the attributes
         // list, add it to the list.
         if (!attributeExists(ctaAttributes, inhAttr)) {
            String rawName = "".equals(inhAttr.getPrefix()) ? 
                                       inhAttr.getLocalName() : 
                                       inhAttr.getPrefix() + ":" + 
                                       inhAttr.getLocalName(); 
            fTempQName.setValues(inhAttr.getPrefix(), inhAttr.getLocalName(), 
                                                  rawName, inhAttr.getUri());
            ctaAttributes.addAttribute(fTempQName, inhAttr.getType(), 
                                                   inhAttr.getValue());
         }
      }
      
      return ctaAttributes;
      
    } // getAttributesForCTA
    
    
    /*
     * For the current element being handled by the Schema validator, find
     * all inheritable attributes for this element. Save these inheritable
     * attributes, in a global Vector list.
     */
    private void saveInheritableAttributes(XSElementDecl currentElemDecl,
                                           XMLAttributes attributes) {
       
       if (currentElemDecl != null && currentElemDecl.fType instanceof  
                                                         XSComplexTypeDecl) {
          XSComplexTypeDecl currentComplexType = (XSComplexTypeDecl) 
                                                        currentElemDecl.fType;
          XSObjectListImpl attributeUses = (XSObjectListImpl) 
                                      currentComplexType.getAttributeUses();           
          
          // iterate all the attributes, being passed to this method        
          for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
             String attrName = attributes.getLocalName(attrIndx);
             String attrUri = attributes.getURI(attrIndx);            
             // iterate all the attribute declarations of a complex type,
             // for the current element.
             for (int attrUsesIndx = 0; attrUsesIndx < attributeUses.getLength(); 
                                                            attrUsesIndx++) {
                XSAttributeUseImpl attrUseImpl = (XSAttributeUseImpl) 
                                            attributeUses.get(attrUsesIndx);
                XSAttributeDeclaration attrDecl = attrUseImpl.getAttrDeclaration();              
                // the current element, has an inheritable attribute
                if (attrName.equals(attrDecl.getName()) &&
                        XSTypeHelper.uriEqual(attrUri, attrDecl.getNamespace()) &&    
                      attrUseImpl.getInheritable()) {                   
                    InheritableAttribute inhrAttr = new InheritableAttribute(
                                            attributes.getLocalName(attrIndx),
                                            attributes.getPrefix(attrIndx),
                                            attributes.getURI(attrIndx),
                                            attributes.getValue(attrIndx),
                                            attributes.getType(attrIndx)) ;
                    fInheritableAttrList.add(inhrAttr);                   
               }
            }
          }          
       }
       
    } // saveInheritableAttributes
    
    
    /*
     * Check if an inheritable attribute, exists in the attributes list
     */
    private boolean attributeExists(XMLAttributes attributes, 
                                    InheritableAttribute inhAttr) {
      
      boolean attrExists = false;
      
      for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
          String localName = attributes.getLocalName(attrIndx);
          String uri = attributes.getURI(attrIndx);          
          if (localName.equals(inhAttr.getLocalName()) &&
                XSTypeHelper.uriEqual(uri, inhAttr.getUri())) {              
             attrExists = true;
             break;
          }
      }
      
      return attrExists;
      
    } // attributeExists
    
    
    /*
     * A class representing an inheritable attribute. An instance of this class
     * is used as an intermediate storage, for inheritable attribute 
     * information.
     */
    class InheritableAttribute {       
       
       String localName = "";
       String prefix = "";
       String uri = "";
       String value = "";
       String type = "";
      
       public InheritableAttribute(String localName,
                                   String prefix,
                                   String uri,
                                   String value,
                                   String type) {
         this.localName = localName;
         this.prefix = prefix;
         this.uri = uri;
         this.value = value;
         this.type = type;
       }
       
       public String getLocalName() {
          return localName;
       }
       
       public String getPrefix() {
          return prefix;
       }
       
       public String getUri() {
          return uri;
       }
       
       public String getValue() {
          return value;
       }
       
       public String getType() {
          return type; 
       }
       
    } // class, InheritableAttribute
    
} // class XSDTypeAlternativeValidator
