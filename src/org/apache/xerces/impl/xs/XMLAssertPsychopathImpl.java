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

import java.util.ArrayList;
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
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.impl.xs.assertion.XMLAssertAdapter;
import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.PsychoPathTypeHelper;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.SchemaTypeValueFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class implementing an XPath interface, for XML Schema 1.1 "assertions"
 * evaluation. This class interfaces with the "Eclipse/PsychoPath XPath 2.0"
 * engine for XPath expression evaluations, for XML Schema assertions.
 * 
 * The class here constructs Xerces PSVI enabled DOM trees -- "on which
 * PsychoPath XPath engine operates" (for typed XDM instance support) from
 * XNI event calls. XML Schema assertions are evaluated on these XPath tree
 * instances, in a bottom up fashion.
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
    AbstractPsychoPathImpl fAbstrPsychopathImpl = null;

    // the DOM root of assertions tree
    Document fAssertDocument = null;

    // an element to track construction of assertion DOM tree. This object
    // changes as per the XNI document events.
    Element fCurrentAssertDomNode = null;

    // a stack holding the DOM roots for assertions evaluation
    Stack fAssertRootStack = null;

    // a stack parallel to 'assertRootStack' storing all assertions for a
    // single XDM tree.
    Stack fAssertListStack = null;

    // XMLSchemaValidator reference. set from the XMLSchemaValidator object
    // itself.
    XMLSchemaValidator fValidator = null;
    
    // parameters to pass to PsychoPath engine (like, the XML namespace
    // bindings).
    Map fAssertParams = null;
    
    // an instance variable to track the name of an attribute currently
    // been processed for assertions.
    String fAttrName = null;

    
    /*
     * Class constructor.
     */
    public XMLAssertPsychopathImpl(Map assertParams) {        
        // initializing the class variables
        this.fAssertDocument = new PSVIDocumentImpl();        
        this.fAssertRootStack = new Stack();
        this.fAssertListStack = new Stack();
        this.fAssertParams = assertParams;
    }
    

    /*
     * Initialize the PsychoPath XPath processor.
     */
    private void initXPathProcessor() throws Exception {
        
        fValidator = (XMLSchemaValidator) getProperty
                        ("http://apache.org/xml/properties/assert/validator");        
        fAbstrPsychopathImpl = new AbstractPsychoPathImpl();
        fDynamicContext = fAbstrPsychopathImpl.initDynamicContext(
                                                    fSchema,
                                                    fAssertDocument,
                                                    fAssertParams);
    } // initXPathProcessor
    

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#startElement
     *      (org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, 
     *       org.apache.xerces.xni.Augmentations)
     */
    public void startElement(QName element, XMLAttributes attributes,
                                            Augmentations augs) {
        
        if (fCurrentAssertDomNode == null) {
           fCurrentAssertDomNode = new PSVIElementNSImpl((CoreDocumentImpl)
                                fAssertDocument, element.uri, element.rawname);
           fAssertDocument.appendChild(fCurrentAssertDomNode);
        } else {
            Element elem = new PSVIElementNSImpl((CoreDocumentImpl)
                                fAssertDocument, element.uri, element.rawname);
            fCurrentAssertDomNode.appendChild(elem);
            fCurrentAssertDomNode = elem;
        }

        // add attribute nodes to DOM element node
        for (int attIndex = 0; attIndex < attributes.getLength(); attIndex++) {
            String attrUri = attributes.getURI(attIndex);
            String attQName = attributes.getQName(attIndex);
            String attrLocalName = attributes.getLocalName(attIndex);
            String attValue = attributes.getValue(attIndex);             
            
            PSVIAttrNSImpl attrNode = new PSVIAttrNSImpl((PSVIDocumentImpl)
                                            fAssertDocument, attrUri,
                                            attQName, attrLocalName);
            attrNode.setNodeValue(attValue);
            
            // set PSVI information for the attribute
            Augmentations attrAugs = attributes.getAugmentations(attIndex);
            AttributePSVImpl attrPSVI = (AttributePSVImpl) attrAugs.
                                         getItem(Constants.ATTRIBUTE_PSVI);
            attrNode.setPSVI(attrPSVI);
            
            fCurrentAssertDomNode.setAttributeNode(attrNode);
        }

        List assertionList = (List) augs.getItem("ASSERT");
        
        // if we have assertions applicable to this element, store the element
        // reference and the assertions on it, on the runtime stacks.
        if (assertionList != null) {
            fAssertRootStack.push(fCurrentAssertDomNode);
            fAssertListStack.push(assertionList);
        }
        
    } // startElement
    

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#endElement(org.apache.xerces.xni.QName, 
     *      org.apache.xerces.xni.Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws 
                                                          Exception {
        
        if (fCurrentAssertDomNode != null) {
            // set PSVI information on the element
            ElementPSVI elemPSVI = (ElementPSVI) augs.getItem(
                                                 Constants.ELEMENT_PSVI);
            ((PSVIElementNSImpl) fCurrentAssertDomNode).setPSVI(elemPSVI);
       
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
            
            if (!fAssertRootStack.empty() && (fCurrentAssertDomNode == 
                                             fAssertRootStack.peek())) {               
                 // get XSModel instance                
                 fSchema =  ((PSVIElementNSImpl) fCurrentAssertDomNode).
                                                 getSchemaInformation();
                 
                 // pop the stack, to go one level up
                 fAssertRootStack.pop();
                 // get assertions, and go one level up on the stack
                 List assertions = (List) fAssertListStack.pop(); 
                 Boolean atomicValueValidity = (Boolean) augs.getItem
                                                 ("ATOMIC_VALUE_VALIDITY");
                 if (atomicValueValidity.booleanValue()) {                    
                    // depending on simple content's validity status from
                    // XMLSchemaValidator, process XML schema assertions.
                    processAllAssertionsOnElement(element, itemType, 
                                                  memberTypes, assertions,
                                                  elemPSVI);
                 }
            }

            if (fCurrentAssertDomNode.getParentNode() instanceof Element) {
                fCurrentAssertDomNode = (Element)fCurrentAssertDomNode.
                                                        getParentNode();
            }
        }
        
    } // endElement
    

    /*
     * Method to evaluate all of XML schema 1.1 assertions for an element tree.
     * This is the root method, which evaluates all XML schema assertions, in
     * a single XML instance validation episode.
     */
    private void processAllAssertionsOnElement(QName element,
                                               XSSimpleTypeDefinition itemType,
                                               XSObjectList memberTypes,
                                               List assertions, 
                                               ElementPSVI elemPSVI)
                                               throws Exception {
         
         // initialize the XPath engine
         initXPathProcessor();
         
         // determine "string value" of XPath2 context variable, $value
         String value = getStringValueOf$Value(elemPSVI);

         // evaluate assertions
         if (assertions instanceof XSObjectList) {
            // assertions from a "complex type" definition             
            evaluateAssertionsFromAComplexType(element, assertions, value);            
         }
         else if (assertions instanceof Vector) {
            // assertions from a "simple type" definition
            evaluateAssertionsFromASimpleType(element, itemType, memberTypes,
                                              assertions, value);            
         }
         
    } // processAllAssertionsOnElement


    /*
     * Determine "string value" of XPath2 context variable $value.
     */
    private String getStringValueOf$Value(ElementPSVI pElemPSVI) throws 
                                                          DOMException {
        
        int textChildCount = 0;        
        // we are only interested in text & element nodes. Store count of them
        // in this variable.
        int effectiveChildCount = 0;
        
        // there could be adjacent text nodes in a DOM tree. merge them to 
        // get the value.
        NodeList childList = fCurrentAssertDomNode.getChildNodes();
        StringBuffer textValueContents = new StringBuffer();
        for (int childNodeIndex = 0; childNodeIndex < childList.getLength();
                                                         childNodeIndex++) {
            Node node = childList.item(childNodeIndex);
            if (node.getNodeType() == Node.TEXT_NODE) {
                textChildCount++;
                effectiveChildCount++;
                textValueContents.append(node.getNodeValue());
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE) {
                effectiveChildCount++;  
            }
        }
        
        String value = "";
        
        if (textChildCount == effectiveChildCount) {
            // The DOM tree we are inspecting has simple content. Therefore
            // we can find the desired string value. 
            XSElementDeclaration elemDecl = pElemPSVI.getElementDeclaration();
            if ((elemDecl.getTypeDefinition()).derivedFrom(SchemaSymbols.
                                                          URI_SCHEMAFORSCHEMA,
                                                  SchemaSymbols.ATTVAL_STRING,
                                        XSConstants.DERIVATION_RESTRICTION)) {
                // if element's schema type is derived by restriction from
                // xs:string, white-space normalization is not needed for the
                // string value for context variable $value.
                value = textValueContents.toString();  
            }
            else {
                // white-space normalization is needed for the string value of
                // $value in case of derivation from non xs:string atomic types.
                value = (textValueContents.toString()).trim();
            }    
        }
        else {
            // The DOM tree we are inspecting, has "mixed/element only"
            // content.
            value = null; 
        }
        
        return value;
        
    } // getStringValueOf$Value


    /*
     * Evaluate assertions on a "complex type".
     */
    private void evaluateAssertionsFromAComplexType(QName element, List 
                                                    assertions, String value) 
                                                   throws Exception {
        if (value != null) {
            // complex type with simple content
            setTypedValueFor$value(value, null, null);
        } else {
            // complex type with complex content. assign an empty XPath2
            // sequence to xpath context variable $value.
            fDynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.
                                              processor.internal.types.QName(
                                                                    "value"),
                                    getXPath2ResultSequence(new ArrayList()));
        }
        
        XSObjectList assertList = (XSObjectList) assertions;
        XSObjectList attrMemberTypes = null;        
        for (int i = 0; i < assertList.size(); i++) {
            XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);               
            boolean xpathContextExists = false;
            if (assertImpl.getType() == XSConstants.ASSERTION) {
                // not an assertion facet
                xpathContextExists = true;   
            }
            // check if this is an assertion, from an attribute
            if (assertImpl.getAttrName() != null) {
                value = assertImpl.getAttrValue();
                XSSimpleTypeDefinition attrType = (XSSimpleTypeDefinition) 
                                                assertImpl.getTypeDefinition();
                attrMemberTypes = attrType.getMemberTypes();
                if (assertImpl.getVariety() == XSSimpleTypeDefinition.
                                                                VARIETY_LIST) {
                    // this assertion belongs to a type, that is an item type
                    // of a simpleType -> list.
                    // tokenize the list value by the longest sequence of
                    // white-spaces.
                    String[] values = value.split("\\s+");
                    
                    // evaluate assertion on all of list items
                    for (int valIdx = 0; valIdx < values.length; valIdx++) {
                        setValueOf$valueForAListItem(attrType, values[valIdx]);                        
                        AssertionError assertError = evaluateAssertion(element, 
                                                                    assertImpl, 
                                                                values[valIdx], 
                                                            xpathContextExists,
                                                                        true);
                        if (assertError != null) {
                            reportAssertionsError(assertError);    
                        }
                    }
                }
                else if (assertImpl.getVariety() == XSSimpleTypeDefinition.
                                                            VARIETY_ATOMIC) {
                    // evaluating assertions for simpleType -> restriction
                    setTypedValueFor$value(value, null, attrType);
                    AssertionError assertError = evaluateAssertion(element,
                                                         assertImpl, value,
                                                         xpathContextExists,
                                                                   false);
                    if (assertError != null) {
                        reportAssertionsError(assertError);    
                    }
                }                
            }
            else {
                AssertionError assertError = evaluateAssertion(element,
                                                          assertImpl, value,
                                                          xpathContextExists,
                                                             false);
                if (assertError != null) {
                    reportAssertionsError(assertError);    
                }  
            }
        }

        // evaluate assertions on simpleType -> union on an attribute
        if (attrMemberTypes != null && attrMemberTypes.getLength() > 0) {                
            boolean isValidationFailedForUnion = isValidationFailedForUnion
                                                            (attrMemberTypes,
                                                             element,
                                                             value, true);

            if (isValidationFailedForUnion) {
                // none of the member types of union (the assertions in
                // them) can successfully validate an atomic value. this
                // results in an overall validation failure. report an
                // error message.
                fValidator.reportSchemaError("cvc-assertion.attr.union." +
                                                                 "3.13.4.1", 
                                  new Object[] { element.rawname, fAttrName, 
                                                                  value } );   
            }

            fAttrName = null;            
        }
        
    } // evaluateAssertionsFromAComplexType
    
    
    /*
     * Evaluate assertions on a "simple type".
     */
    private void evaluateAssertionsFromASimpleType(QName element,
                                             XSSimpleTypeDefinition itemType,
                                             XSObjectList memberTypes,
                                             List assertions, String value)                                             
                                             throws Exception {
        
        // assertions from a simple type definition           
        Vector assertList = (Vector) assertions;
        
        for (int i = 0; i < assertList.size(); i++) {
            XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);
            if (itemType != null) {
               // evaluating assertions for simpleType -> list.                    
               // tokenize the list value by the longest sequence of
               // white-spaces.
               String[] values = value.split("\\s+");               
               
               // evaluate assertion on all of list items
               for (int valIdx = 0; valIdx < values.length; valIdx++) {
                  setValueOf$valueForAListItem(itemType, values[valIdx]);
                  AssertionError assertError = evaluateAssertion(element, 
                                                 assertImpl, values[valIdx], 
                                                 false, true);
                  if (assertError != null) {
                      reportAssertionsError(assertError);    
                  }
               }
            }
            else if (memberTypes != null && memberTypes.getLength() == 0) {
                // evaluating assertions for simpleType -> restriction
                setTypedValueFor$value(value, null, null);
                AssertionError assertError = evaluateAssertion(element, 
                                                        assertImpl, value, 
                                                        false, false);
                if (assertError != null) {
                    reportAssertionsError(assertError);    
                }    
            }                
        }

        if (memberTypes != null && memberTypes.getLength() > 0) {
             // evaluate assertions for simpleType -> union
             boolean isValidationFailedForUnion = isValidationFailedForUnion
                                                             (memberTypes,
                                                              element,
                                                              value, false);
            // only 1 error message is reported for assertion failures on
            // simpleType -> union, since it is hard (perhaps impossible?)
            // to determine statically that what all assertions can cause 
            // validation failure, when participating in an XML schema union.
            if (isValidationFailedForUnion) {
                 fValidator.reportSchemaError("cvc-assertion.union.3.13.4.1", 
                              new Object[] { element.rawname, value } );   
            }
        }
        
    } // evaluateAssertionsFromSimpleType
    
    
    /*
     * Set a typed value of XPath2 context variable $value, if an atomic
     * value on which assertion is been evaluated, is an item of a schema
     * component, xs:list.
     */
    private void setValueOf$valueForAListItem(XSSimpleTypeDefinition simpType,
                                              String value) 
                                              throws Exception {
        
        if ((simpType.getMemberTypes()).getLength() > 0) {
            // The list item's type has variety 'union'.
            XSSimpleTypeDefinition actualListItemType = 
                                        getActualListItemTypeForVarietyUnion(
                                                     simpType.getMemberTypes(),
                                                     value);
            // set a schema 'typed value' to variable $value
            setTypedValueFor$value(value, actualListItemType,
                                                 null);
        } 
        else {
           // The list item's type has variety 'atomic'.
           setTypedValueFor$value(value, simpType, null); 
        }
        
    } // setSchemaTypeOn$valueForAListItem
    
    
    /*
     * Determine if an validation must fail due to assertions evaluation for
     * 'simpleType -> union' member types.
     */
    private boolean isValidationFailedForUnion(XSObjectList memberTypes, 
                                               QName element, 
                                               String value,
                                               boolean isAttribute) {
        
        boolean validationFailedForUnion = true;
        
        for (int memberTypeIdx = 0; memberTypeIdx < memberTypes.getLength();
                                                         memberTypeIdx++) {
            XSSimpleTypeDefinition memType = (XSSimpleTypeDefinition) 
                                           memberTypes.item(memberTypeIdx);
            
            // check for assertions on types in an non-schema namespace
            if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(memType.
                                                           getNamespace()) &&
                                               simpleTypeHasAsserts(memType)) {
                XSObjectList memberTypeFacets = memType.getMultiValueFacets();
                for (int memberTypeFacetIdx = 0; memberTypeFacetIdx < 
                                                  memberTypeFacets.getLength(); 
                                                  memberTypeFacetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) 
                                   memberTypeFacets.item(memberTypeFacetIdx);
                    if (facet.getFacetKind() == XSSimpleTypeDefinition.
                                                             FACET_ASSERT) {
                        Vector assertFacets = facet.getAsserts();
                        int assertsSucceeded = 0;
                        for (Iterator iter = assertFacets.iterator(); iter.
                                                               hasNext(); ) {
                            XSAssertImpl assertImpl = (XSAssertImpl) iter.
                                                                      next();
                            try {
                               setTypedValueFor$value(value, memType, null);
                               AssertionError assertError = evaluateAssertion(
                                                                      element, 
                                                                   assertImpl,
                                                                        value, 
                                                                 false, false);
                               if (assertError == null) {
                                   assertsSucceeded++;  
                               }
                               else if (isAttribute && fAttrName == null) {
                                   fAttrName = assertImpl.getAttrName();   
                               }
                            }
                            catch(Exception ex) {
                               // An exception may occur if for example, a typed
                               // value cannot be constructed by PsychoPath engine
                               // for a given "string value" (say a value '5' was 
                               // attempted to be formed as a typed value xs:date).                               
                               // it's useful to report warning ... TO DO
                            }
                        }
                        if (assertsSucceeded == assertFacets.size()) {
                            // all assertions on a 'union' member type have 
                            // evaluated to 'true', therefore validation with
                            // union has succeeded wrt assertions.
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
        if (fCurrentAssertDomNode != null) {
            fCurrentAssertDomNode.appendChild(fAssertDocument.createTextNode(new 
                                   String(text.ch, text.offset, text.length)));
        }
        
    }
    

    /*
     * Method to evaluate an assertion instance for an XML element.
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
                result = fAbstrPsychopathImpl.evaluatePsychoPathExpr(xp,
                                 assertImpl.getXPathDefaultNamespace(),
                                 fCurrentAssertDomNode);  
            } 
            else {
                // XPath context is "undefined"
                result = fAbstrPsychopathImpl.evaluatePsychoPathExpr(xp,
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
     * Find a "schema typed value" (of kind xs:anyAtomicType*) to assign to
     * XPath2 context variable $value.
     */
    private void setTypedValueFor$value(String value, XSSimpleTypeDefinition 
                                  listOrUnionType, XSTypeDefinition attrType)
                                  throws Exception {
        
        // dummy short code initializer
        short xsdTypecode = -100;
        
        if (listOrUnionType != null) {
            xsdTypecode = getXercesXSDTypeCodeFor$Value(listOrUnionType);
            setValueOf$ValueForSTVarietyAtomic(value, xsdTypecode);
        }
        else {
           if (attrType != null) {
              // is value of an attribute
              xsdTypecode = getXercesXSDTypeCodeFor$Value(attrType);
              setValueOf$ValueForSTVarietyAtomic(value, xsdTypecode);
           }
           else {
              // is "simple type" value of an element
              PSVIElementNSImpl currentAssertPSVINode = (PSVIElementNSImpl)
                                                   fCurrentAssertDomNode;
              XSTypeDefinition typeDef = currentAssertPSVINode.
                                                      getTypeDefinition();
              if (typeDef instanceof XSComplexTypeDefinition && 
                 ((XSComplexTypeDefinition) typeDef).getSimpleType() != null) {
                  setValueOf$ValueForCTWithSimpleContent(value, 
                                            (XSComplexTypeDefinition) typeDef);
              }
              else if (typeDef instanceof XSComplexTypeDefinition && 
                      ((XSComplexTypeDefinition) typeDef).getSimpleType() == null) {
                  // assign an empty XPath2 sequence to xpath context variable
                  // $value.
                  fDynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.
                                                processor.internal.types.QName(
                                                              "value"),
                                          getXPath2ResultSequence(new ArrayList())); 
              }
              else {
                  xsdTypecode = getXercesXSDTypeCodeFor$Value(typeDef);
                  setValueOf$ValueForSTVarietyAtomic(value, xsdTypecode);
              }
           }
        }
        
    } // setTypedValueFor$value
    
    
    /*
     * Set value of XPath2 context variable $value, when variety of it's
     * schema type is simpleType -> 'atomic'.  
     */
    private void setValueOf$ValueForSTVarietyAtomic(String value, short 
                                                              xsdTypecode) {
        AnyType psychoPathType = SchemaTypeValueFactory.newSchemaTypeValue
                                                         (xsdTypecode, value);
        fDynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.
                                               processor.internal.types.QName(
                                                     "value"), (AnyAtomicType) 
                                                              psychoPathType);
    } // setValueOf$ValueForSTVarietyAtomic


    /*
     * Set value of XPath2 context variable $value, if element has a complex
     * type with simple content.
     */
    private void setValueOf$ValueForCTWithSimpleContent(String value, 
                                            XSComplexTypeDefinition typeDef) {
        
        XSComplexTypeDefinition cmplxTypeDef = (XSComplexTypeDefinition) 
                                                                      typeDef;
        XSSimpleTypeDefinition complexTypeSimplContentType = cmplxTypeDef.
                                                              getSimpleType();
        if (complexTypeSimplContentType.getVariety() == XSSimpleTypeDefinition.
                                                                VARIETY_LIST) {
            // simple content type has variety xs:list
            XSSimpleTypeDefinition listItemType = complexTypeSimplContentType.
                                                                 getItemType();
            // split the "string value" of list contents, into non white-space
            // tokens.
            String[] values = value.split("\\s+");
            
            // construct a list of atomic XDM items, to assign to XPath2
            // context variable $value.
            List xdmItemList = new ArrayList();
            if ((listItemType.getMemberTypes()).getLength() > 0) {
               // itemType of xs:list has variety 'union'. here list items may
               // have different types, which are determined below.
               for (int valIdx = 0; valIdx < values.length; valIdx++) {
                   XSSimpleTypeDefinition listItemTypeForUnion = 
                                         getActualListItemTypeForVarietyUnion
                                               (listItemType.getMemberTypes(), 
                                                              values[valIdx]);
                   xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue
                                                          (listItemTypeForUnion.
                                              getBuiltInKind(),values[valIdx]));
               }                                  
            }
            else {
               // every list item has a same type (the itemType of
               // xs:list).
               for (int valIdx = 0; valIdx < values.length; valIdx++) {
                   xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue
                                               (listItemType.getBuiltInKind(), 
                                                             values[valIdx])); 
               }                                  
            }

            // assign an XPath2 sequence to xpath context variable $value
            fDynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.
                                              processor.internal.types.QName(
                                                                    "value"),
                                        getXPath2ResultSequence(xdmItemList));
        }
        else if (complexTypeSimplContentType.getVariety() == 
                                        XSSimpleTypeDefinition.VARIETY_UNION) {
            // simple content type has variety xs:union
            XSSimpleTypeDefinition simpleContentTypeForUnion = 
                                           getActualListItemTypeForVarietyUnion
                                 (complexTypeSimplContentType.getMemberTypes(),
                                                                       value);
            fDynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.
                                      processor.internal.types.QName("value"), 
                                    SchemaTypeValueFactory.newSchemaTypeValue
                         (simpleContentTypeForUnion.getBuiltInKind(), value));
        }
        else {
            // simple content type has variety atomic
            setValueOf$ValueForSTVarietyAtomic(value, getXercesXSDTypeCodeFor$Value
                                              (cmplxTypeDef.getSimpleType()));
        }
          
    } // setValueOf$ValueForCTWithSimpleContent
    
    
    /*
       Find the built-in Xerces schema 'type code' for XPath2 variable, $value.
       This function recursively searches the XML schema type hierarchy
       navigating up the base types, to find the needed built-in type.
    */
    private short getXercesXSDTypeCodeFor$Value(XSTypeDefinition elementType) {
            
      if (Constants.NS_XMLSCHEMA.equals(elementType.getNamespace())) {
         short typeCode = -100; // dummy initializer
         
         boolean isxsd11Type = false;
         
         // the below 'if else' clauses are written to process few special
         // cases, handling few of schema types, within PsychoPath XPath
         // engine.
         if ("dayTimeDuration".equals(elementType.getName())) {
             typeCode = PsychoPathTypeHelper.DAYTIMEDURATION_DT;
             isxsd11Type = true;
         }
         else if ("yearMonthDuration".equals(elementType.getName())) {
             typeCode = PsychoPathTypeHelper.YEARMONTHDURATION_DT;
             isxsd11Type = true;
         }
         
         return (isxsd11Type) ? typeCode : ((XSSimpleTypeDefinition) 
                                            elementType).getBuiltInKind();    
      }
      else {
         return getXercesXSDTypeCodeFor$Value(elementType.getBaseType()); 
      }
      
    } // getXercesXSDTypeCodeFor$Value
    
    
    /*
     * Construct an PsychoPath XPath2 "result sequence", given a list of XDM
     * items as input.
     */
    private ResultSequence getXPath2ResultSequence(List xdmItems) {
        
        ResultSequence xpath2Seq = ResultSequenceFactory.create_new();
        
        for (Iterator iter = xdmItems.iterator(); iter.hasNext(); ) {
            xpath2Seq.add((AnyType) iter.next()); 
        }
        
        return xpath2Seq;
        
    } // getXPath2ResultSequence
    
    
    /*
     * Method to report assertions error messages.
     */
    private void reportAssertionsError(AssertionError assertError) {
        
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
              message = "Assertion failed (undefined context) for schema type '"
                         + typeString + "'. " + message;   
           }
           else {
              message = "Assertion failed for schema type '" + typeString + "'. "
                         + message; 
           }
           fValidator.reportSchemaError("cvc-assertion.failure", 
                               new Object[] { message, listAssertErrMessage } );    
        }
        else {
           fValidator.reportSchemaError(key, new Object[] { elemErrorAnnotation,
                               assertImpl.getTest().getXPath().toString(),
                               typeString, listAssertErrMessage} );
        }
        
    } // reportAssertionsError
    
    
    /*
     * Find the actual schema type of "list item" instance, if the
     * "item type" of list has variety union. 
     */
    private XSSimpleTypeDefinition getActualListItemTypeForVarietyUnion
                                                    (XSObjectList memberTypes,
                                                     String value) {

        XSSimpleTypeDefinition simpleTypeDefn = null;
        
        // Inspecting the member types of union in order, to find that which
        // schema type can successfully validate an atomic value first.
        for (int memTypeIdx = 0; memTypeIdx < memberTypes.getLength(); 
                                                            memTypeIdx++) {
           XSSimpleType memSimpleType = (XSSimpleType) memberTypes.item
                                                             (memTypeIdx);
              if (isValueValidForASimpleType(value, memSimpleType)) {
                 // no more memberTypes need to be checked
                 simpleTypeDefn = memSimpleType; 
                 break; 
              }
        }
        
        return simpleTypeDefn;
        
    } // getActualListItemTypeForVarietyUnion
    
    
    /*
     * Determine if a lexical "string value" conforms to a given schema
     * simpleType definition. Using Xerces API 'XSSimpleType.validate'
     * for this need.
     */
    private boolean isValueValidForASimpleType(String value, XSSimpleType 
                                                             simplType) {
        
        boolean isValueValid = true;
        
        try {
            // construct necessary context objects
            ValidatedInfo validatedInfo = new ValidatedInfo();
            ValidationContext validationState = new ValidationState();
            
            // attempt to validate the "string value" with a simpleType
            // instance.
            simplType.validate(value, validationState, validatedInfo);
        } 
        catch(InvalidDatatypeValueException ex){
            isValueValid = false;
        }
        
        return isValueValid;
        
    } // isValueValidForASimpleType
    
    
    /*
     * Class to store "assertion evaluation" error details.
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
                              XSAssertImpl assertImpl, 
                              String value, boolean isList) {
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
