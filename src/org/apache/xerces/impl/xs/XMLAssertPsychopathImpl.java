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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.PSVIAttrNSImpl;
import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.assertion.XMLAssertAdapter;
import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.SchemaTypeValueFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An implementation of the XPath interface, for XML Schema 1.1 'assertions'
 * evaluation. This class interfaces with the PsychoPath XPath 2.0 engine.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @author Ken Cai, IBM
 * 
 * @version $Id$
 */
public class XMLAssertPsychopathImpl extends XMLAssertAdapter {

    // class variable declarations
    DynamicContext fDynamicContext;
    XSModel fSchema = null;
    AbstractPsychoPathImpl abstrPsychopathImpl = null;

    // the DOM root of assertions tree
    Document assertDocument = null;

    // an element to track construction of assertion DOM tree. This object
    // changes as per the XNI document events.
    Element currentAssertDomNode = null;

    // a stack holding the DOM roots for assertions evaluation
    Stack assertRootStack = null;

    // a stack parallel to 'assertRootStack' storing all assertions for a
    // single XDM tree.
    Stack assertListStack = null;

    // XMLSchemaValidator reference. set from the XMLSchemaValidator object
    // itself.
    XMLSchemaValidator validator = null;
    
    // parameters to pass to PsychoPath engine (like, the namespace bindings) 
    Map assertParams = null;

    
    /*
     * Class constructor
     */
    public XMLAssertPsychopathImpl(Map assertParams) {        
        // initializing the class variables.        
        // we use a PSVI enabled DOM implementation, to be able to have typed
        // XDM nodes.
        this.assertDocument = new PSVIDocumentImpl();        
        this.assertRootStack = new Stack();
        this.assertListStack = new Stack();
        this.assertParams = assertParams;        
    }
    

    /*
     * Initialize the PsychoPath XPath processor
     */
    private void initXPathProcessor() throws Exception {
        
        validator = (XMLSchemaValidator) getProperty
                        ("http://apache.org/xml/properties/assert/validator");        
        abstrPsychopathImpl = new AbstractPsychoPathImpl();
        fDynamicContext = abstrPsychopathImpl.initDynamicContext(
                                                    fSchema,
                                                    assertDocument,
                                                    assertParams);
    } // initXPathProcessor
    

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#startElement
     *      (org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, 
     *       org.apache.xerces.xni.Augmentations)
     */
    public void startElement(QName element, XMLAttributes attributes,
                                              Augmentations augs) {
        
        if (currentAssertDomNode == null) {
           currentAssertDomNode = new PSVIElementNSImpl((CoreDocumentImpl)
                                assertDocument, element.uri, element.rawname);
           assertDocument.appendChild(currentAssertDomNode);
        } else {
            Element elem = new PSVIElementNSImpl((CoreDocumentImpl)
                                assertDocument, element.uri, element.rawname);
            currentAssertDomNode.appendChild(elem);
            currentAssertDomNode = elem;
        }

        // add attributes to the element
        for (int attIndex = 0; attIndex < attributes.getLength(); attIndex++) {
            String attrUri = attributes.getURI(attIndex);
            String attQName = attributes.getQName(attIndex);
            String attValue = attributes.getValue(attIndex);
            
            PSVIAttrNSImpl attrNode = new PSVIAttrNSImpl((PSVIDocumentImpl)
                                          assertDocument, attrUri, attQName);
            attrNode.setNodeValue(attValue);
            
            // set PSVI information for the attribute
            Augmentations attrAugs = attributes.getAugmentations(attIndex);
            AttributePSVImpl attrPSVI = (AttributePSVImpl) attrAugs.
                                         getItem(Constants.ATTRIBUTE_PSVI);
            attrNode.setPSVI(attrPSVI);
            
            currentAssertDomNode.setAttributeNode(attrNode);
        }

        List assertionList = (List) augs.getItem("ASSERT");
        // if we have assertions applicable to this element, store the element
        // reference and the assertions on it, on the runtime stacks.
        if (assertionList != null) {
            assertRootStack.push(currentAssertDomNode);
            assertListStack.push(assertionList);
        }
        
    } // startElement
    

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#endElement(org.apache.xerces.xni.QName, 
     *      org.apache.xerces.xni.Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws Exception {
        
        if (currentAssertDomNode != null) {
            // set PSVI information on the element
            ElementPSVI elemPSVI = (ElementPSVI) augs.getItem(
                                                 Constants.ELEMENT_PSVI);
            ((PSVIElementNSImpl)currentAssertDomNode).setPSVI(elemPSVI);
       
            // itemType for xs:list
            XSSimpleTypeDefinition itemType = null;
            
            // memberTypes for xs:union
            XSObjectList memberTypes = null;
            
            if (elemPSVI.getTypeDefinition().getTypeCategory() == 
                                                XSTypeDefinition.SIMPLE_TYPE) {
                XSSimpleTypeDefinition simpleTypeDefn = (XSSimpleTypeDefinition) 
                                                elemPSVI.getTypeDefinition();
                itemType = simpleTypeDefn.getItemType();
                if (itemType == null) {
                    memberTypes = simpleTypeDefn.getMemberTypes();    
                }
            }
            
            if (!assertRootStack.empty() && (currentAssertDomNode == 
                                             assertRootStack.peek())) {               
                 // get XSModel                
                 fSchema =  ((PSVIElementNSImpl) currentAssertDomNode).
                                                 getSchemaInformation();
                 
                 // pop the stack, to go one level up
                 assertRootStack.pop();
                 // get assertions, and go one level up
                 List assertions = (List) assertListStack.pop(); 
                 Boolean atomicValueValidity = (Boolean) augs.getItem
                                                 ("ATOMIC_VALUE_VALIDITY");
                 if (atomicValueValidity.booleanValue()) {
                    // depending on simple content's validity status from
                    // XMLSchemaValidator, process XML schema assertions.
                    processAllAssertionsOnElement(element, itemType, 
                                               memberTypes, assertions);
                 }
            }

            if (currentAssertDomNode.getParentNode() instanceof Element) {
                currentAssertDomNode = (Element)currentAssertDomNode.
                                                        getParentNode();
            }
        }
        
    } // endElement
    

    /*
     * Method to evaluate all of assertions for an element tree.
     */
    private void processAllAssertionsOnElement(
                                    QName element,
                                    XSSimpleTypeDefinition itemType,
                                    XSObjectList memberTypes,
                                    List assertions)
                                    throws Exception {
         
         // initialize the XPath engine
         initXPathProcessor();
         
         // determine value of variable, $value
         String value = "";
         NodeList childList = currentAssertDomNode.getChildNodes();         
         int textChildCount = 0;
         // there could be adjacent text nodes. merge them to get the value.
         for (int childNodeIndex = 0; childNodeIndex < childList.getLength();
                                                       childNodeIndex++) {
             Node node = childList.item(childNodeIndex);
             if (node.getNodeType() == Node.TEXT_NODE) {
                 textChildCount++;
                 value = value + node.getNodeValue();
             }
         }
         
         if (textChildCount != childList.getLength()) {
            value = null;  
         }

         // evaluate assertions
         if (assertions instanceof XSObjectList) {
            // assertions from a complex type definition             
            if (value != null) {
               // complex type with simple content
               setValueOf$value(value, null, null);
            } else {
               // complex type with complex content                
               // $value should be, the XPath2 "empty sequence" ... TO DO 
            }
            XSObjectList assertList = (XSObjectList) assertions;
            for (int i = 0; i < assertList.size(); i++) {
               XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);               
               boolean xpathContextExists = false;
               if (assertImpl.getType() == XSConstants.ASSERTION) {
                  // not an assertion facet
                  xpathContextExists = true;   
               }
               // check if this is an assertion, from the attribute
               if (assertImpl.getAttrValue() != null) {
                  // reassign value (the attribute's value) to variable
                  // $value.
                  value = assertImpl.getAttrValue();
                  setValueOf$value(value, null, 
                                   assertImpl.getTypeDefinition());
               }
               
               AssertionError assertError = evaluateAssertion(element,
                                                assertImpl, value,
                                                xpathContextExists,
                                                false);
               if (assertError != null) {
                   reportError(assertError);    
               }
            }
         }
         else if (assertions instanceof Vector) {
            // assertions from a simple type definition           
            Vector assertList = (Vector) assertions;
            
            for (int i = 0; i < assertList.size(); i++) {
                XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);
                if (itemType != null) {
                   // evaluating assertions for simpleType -> list
                    
                   // tokenize the list value by the longest sequence of
                   // white-spaces.
                   String[] values = value.split("\\s+");
                   
                   // evaluate assertion on all list items
                   for (int valIdx = 0; valIdx < values.length; valIdx++) {
                      setValueOf$value(values[valIdx], itemType, null);
                      AssertionError assertError = evaluateAssertion(element, 
                                                                assertImpl, 
                                                                values[valIdx], 
                                                                false,
                                                                true);
                      if (assertError != null) {
                          reportError(assertError);    
                      }
                   }
                }
                else if (memberTypes != null && memberTypes.getLength() == 0) {
                    // evaluating assertions for simpleType -> restriction
                    setValueOf$value(value, null, null);
                    AssertionError assertError = evaluateAssertion(element, 
                                                                assertImpl,
                                                                value, 
                                                                false,
                                                                false);
                    if (assertError != null) {
                        reportError(assertError);    
                    }    
                }                
            }
            
            // check for assertions on union member types.
            // (there's a special handling for union types for assertions,
            // since validation success on any one of the union member types,
            // results in the overall validity of the value in the context)
            if (memberTypes != null && memberTypes.getLength() > 0) {                 
                 boolean isValidationFailedForUnion = 
                                                  isValidationFailedForUnion
                                                                 (memberTypes,
                                                                  element,
                                                                  value);
            
                if (isValidationFailedForUnion) {
                     // none of the member types of union (the assertions in
                     // them) can successfully validate an atomic value. this
                     // results in an overall validation failure. report an
                     // error message.
                     validator.reportSchemaError("cvc-assertion.union.3.13.4.1", 
                                  new Object[] { element.rawname, value } );   
                }
            }            
         }
         
    } // processAllAssertionsOnElement
    
    
    /*
     * Determine if validation should fail due to assertions evaluation for
     * 'simpleType -> union' member types.
     */
    private boolean isValidationFailedForUnion(XSObjectList memberTypes, 
                                              QName element, 
                                              String value) {
        
        boolean validationFailedForUnion = true;
        
        for (int memberTypeIdx = 0; memberTypeIdx < memberTypes.getLength();
                                                         memberTypeIdx++) {
            XSSimpleTypeDefinition memType = (XSSimpleTypeDefinition) 
                                           memberTypes.item(memberTypeIdx);
            if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(memType.
                                                           getNamespace()) &&
                                            simpleTypeHasAsserts(memType)) {
                XSObjectList memberTypeFacets = memType.getMultiValueFacets();
                for (int memberTypeFacetIdx = 0; memberTypeFacetIdx < 
                                                       memberTypeFacets.getLength(); 
                                                       memberTypeFacetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) memberTypeFacets.
                                                         item(memberTypeFacetIdx);
                    if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                        Vector assertFacets = facet.getAsserts();
                        int assertsSucceeded = 0;
                        for (Iterator iter = assertFacets.iterator(); iter.hasNext(); ) {
                            XSAssertImpl assertImpl = (XSAssertImpl) iter.next();
                            try {
                               setValueOf$value(value, memType, null);
                               AssertionError assertError = evaluateAssertion(element, 
                                                                           assertImpl,
                                                                                value, 
                                                                        false, false);
                               if (assertError == null) {
                                   assertsSucceeded++;  
                               }
                            }
                            catch(Exception ex) {
                               // An exception may occur if for example, a typed
                               // value cannot be constructed by PsychoPath engine
                               // for a given string value (say a value '5' was 
                               // attempted to be formed as a typed value xs:date).
                               // This is a known scenario for an exception here.
                               // Exceptions may occur due to other un-observed
                               // scenarios.
                                
                               // it's useful to report warning ... TO DO
                            }
                        }
                        if (assertsSucceeded == assertFacets.size()) {
                            // all assertions on a 'union member type' have 
                            // evaluated to 'true', therefore validation with
                            // union has succeeded wrt assertions. return
                            // a boolean value 'false' from this method.
                            return false;  
                        }
                    }
                }
            }
        }
        
        return validationFailedForUnion;
        
    } // isValidationFailedForUnion
    
    
    /*
     * Check if a simple type has assertion facets.
     */
    private boolean simpleTypeHasAsserts(XSSimpleTypeDefinition simpleType) {
        
        boolean hasAssertions = false;
        
        XSObjectList simpleTypeFacets = simpleType.getMultiValueFacets();
        
        for (int facetIdx = 0; facetIdx < simpleTypeFacets.getLength(); 
                                                        facetIdx++) {
            XSMultiValueFacet facet = (XSMultiValueFacet) simpleTypeFacets.
                                                 item(facetIdx);
            if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT && 
                                            facet.getAsserts().size() > 0) {
                hasAssertions = true;
                break;
            }
        }
        
        return hasAssertions;

    } // simpleTypeHasAsserts
    

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#characters
     *      (org.apache.xerces.xni.XMLString)
     */
    public void characters(XMLString text) {
        
        // add a child text node to the assertions, DOM tree
        if (currentAssertDomNode != null) {
            currentAssertDomNode.appendChild(assertDocument.createTextNode(new 
                                   String(text.ch, text.offset, text.length)));
        }
        
    }
    

    /*
     * Method to evaluate an assertion for the element.
     */
    private AssertionError evaluateAssertion(QName element,
                                   XSAssertImpl assertImpl,
                                   String value,
                                   boolean xPathContextExists,
                                   boolean isList) {
        
        AssertionError assertionError = null;
        
        try {  
            XPath xp = assertImpl.getCompiledXPath();
            
            boolean result;            
            if ((value == null) ||
                (xPathContextExists == true)) {
                result = abstrPsychopathImpl.evaluatePsychoPathExpr(xp,
                                 assertImpl.getXPathDefaultNamespace(),
                                 currentAssertDomNode);  
            } 
            else {
                // XPath context is "undefined"
                result = abstrPsychopathImpl.evaluatePsychoPathExpr(xp,
                                 assertImpl.getXPathDefaultNamespace(),
                                 null); 
            }
            
            if (!result) {
               // assertion evaluation is false
               assertionError = new AssertionError("cvc-assertion.3.13.4.1", 
                                                   element, assertImpl, 
                                                   value, isList); 
            }
        }
        catch (DynamicError ex) {
            if (ex.code().equals("XPDY0002")) {
               // ref: http://www.w3.org/TR/xpath20/#eval_context
               assertionError = new AssertionError("cvc-assertion.4.3.15.3", 
                                                   element, assertImpl,
                                                   value, isList);
            }
            else {
               assertionError = new AssertionError("cvc-assertion.3.13.4.1", 
                                                   element, assertImpl,
                                                   value, isList);
            }
        }
        catch (StaticError ex) {
            assertionError = new AssertionError("cvc-assertion.3.13.4.1", 
                                                 element, assertImpl,
                                                 value, isList);
        }
        catch(Exception ex) {
            assertionError = new AssertionError("cvc-assertion.3.13.4.1", 
                                                element, assertImpl,
                                                value, isList);   
        }
        
        return assertionError;
        
    } // evaluateAssertion
    
    
    /*
     * Assign value to the XPath2 "dynamic context" variable, $value.
     */
    private void setValueOf$value(String value, 
                                  XSSimpleTypeDefinition listOrUnionType, 
                                  XSTypeDefinition attrType) throws Exception {
        
        // XML Schema type for variable $value
        String xsdTypeName = "";
        
        if (listOrUnionType != null) {
            xsdTypeName = getXSDtypeOf$Value(listOrUnionType);    
        }
        else {
           if (attrType != null) {
              // is value of an attribute
              xsdTypeName = getXSDtypeOf$Value(attrType);  
           }
           else {
              // is "simple type" value of an element
              PSVIElementNSImpl currentAssertPSVINode = (PSVIElementNSImpl)
                                                   currentAssertDomNode;
              XSTypeDefinition typeDef = currentAssertPSVINode.getTypeDefinition();
              if (typeDef instanceof XSComplexTypeDefinition) {
                  XSComplexTypeDefinition cmplxTypeDef = (XSComplexTypeDefinition)
                                                                       typeDef;
                  if (cmplxTypeDef.getSimpleType() != null) {
                     xsdTypeName = getXSDtypeOf$Value(cmplxTypeDef.getSimpleType());   
                  }
              }
              else {
                 xsdTypeName = getXSDtypeOf$Value(currentAssertPSVINode.
                                               getTypeDefinition());
              }
           }
        }
        
        // determine the 'schema type' representation used within PsychoPath XPath 
        // engine, corresponding to an XML Schema language type.
        Object psychoPathType = SchemaTypeValueFactory.newSchemaTypeValue
                                                         (xsdTypeName, value);
        
        fDynamicContext.set_variable(
               new org.eclipse.wst.xml.xpath2.processor.internal.types.QName(
                       "value"), (AnyAtomicType) psychoPathType);
        
    } // setValueOf$value
    
    
    /*
       Find the built in XSD type for XPath2 variable, $value. This function
       recursively searches the XSD type hierarchy navigating up the base
       types, to find the needed built-in type.
    */
    private String getXSDtypeOf$Value(XSTypeDefinition elementType) {
      
      if (Constants.NS_XMLSCHEMA.equals(elementType.getNamespace())) {        
         return elementType.getName();    
      }
      else {
         return getXSDtypeOf$Value(elementType.getBaseType()); 
      }
      
    } // getXSDtypeOf$Value
    
    
    /*
     * Method to report error messages.
     */
    private void reportError(AssertionError assertError) {
        
        String key = assertError.getErrorCode();
        QName element = assertError.getElement();
        XSAssertImpl assertImpl = assertError.getAssertion();
        boolean isList = assertError.isList();
        
        XSTypeDefinition typeDef = assertImpl.getTypeDefinition();        
        String typeString = "";
        
        if (typeDef != null) {
            typeString = (typeDef.getName() != null) ? typeDef.getName() :
                                                      "#anonymous";   
        }
        else {
            typeString = "#anonymous"; 
        }
        
        String elemErrorAnnotation = element.rawname;
        if (assertImpl.getAttrName() != null) {
            elemErrorAnnotation = element.rawname + " (attribute => " +
                                              assertImpl.getAttrName()+ ")";    
        }                
        
        String listAssertErrMessage = "";        
        if (isList) {
           listAssertErrMessage =  "Assertion failed for an xs:list member value '" + 
                                                   assertError.getValue() + 
                                                   "'.";
        }
            
        String message = assertImpl.getMessage();
        if (message != null) {
           if (!message.endsWith(".")) {
              message = message + ".";    
           }
           if (key.equals("cvc-assertion.4.3.15.3")) {
              message = "Assertion failure (undefined context). " + message;   
           }
           else {
              message = "Assertion failure. " + message; 
           }
           validator.reportSchemaError("cvc-assertion.failure", 
                               new Object[] { message, listAssertErrMessage } );    
        }
        else {
           validator.reportSchemaError(key, new Object[] { elemErrorAnnotation,
                               assertImpl.getTest().getXPath().toString(),
                               typeString, listAssertErrMessage} );
        }
        
    } // reportError
    
    
    /*
     * An object to store assertion error details.
     */
    class AssertionError {
        
        // instance variables        
        String errorCode = null;
        QName element = null;
        XSAssertImpl assertImpl = null;
        String value = null;
        // does this error concerns simpleType -> list
        boolean isList;
        
        // class constructor
        public AssertionError(String errorCode, QName element, 
                              XSAssertImpl assertImpl, String value,
                              boolean isList) {
           this.errorCode = errorCode;
           this.element = element;
           this.assertImpl = assertImpl;
           this.value = value;
           this.isList = isList;
        }
        
        public String getErrorCode() {
           return errorCode;    
        }
        
        public QName getElement() {
           return element;       
        }
        
        public XSAssertImpl getAssertion() {
           return assertImpl;    
        }
        
        public String getValue() {
           return value; 
        }
        
        public boolean isList() {
           return isList;    
        }
        
    } // class AssertionError
    
} // class XMLAssertPsychopathImpl
