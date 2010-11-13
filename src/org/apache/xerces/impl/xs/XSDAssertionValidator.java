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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.assertion.XMLAssertHandler;
import org.apache.xerces.impl.xs.assertion.XSAssert;
import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * An XML Schema validator subcomponent handling assertions processing.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi IBM
 * 
 * @version $Id$
 */
public class XSDAssertionValidator {
    
    // XMLSchemaValidator instance that acts as context for the present assertion validator subcomponent.
    // Passed into the constructor of this object.
    XMLSchemaValidator xmlSchemaValidator = null;
    
    // assertion processor object reference
    XMLAssertHandler fAssertionProcessor = null;
    
    
    /*
     * Class constructor.
     */
    public XSDAssertionValidator(XMLSchemaValidator xmlSchemaValidator) {
       this.xmlSchemaValidator = xmlSchemaValidator; 
    }

    
    /*
     * Assertions processing interface during the XNI event, 'handleCharacters' in XMLSchemaValidator. 
     */
    public void characterDataHandler(XMLString text) {
        
        if (fAssertionProcessor != null) {
           fAssertionProcessor.characters(text);
        }
        
    } // characterDataHandler
    
    
    /*
     * Assertions processing interface during the XNI event, 'handleStartElement' in XMLSchemaValidator.
     */
    public void handleStartElement(QName element, XMLAttributes attributes) {
       
        // get list of assertions for processing
        List assertionList = getAssertsForEvaluation(element, attributes);
        
        // instantiate the assertions processor
        if (assertionList != null && fAssertionProcessor == null) {
            // construct parameter values for the assertion processor
            NamespaceSupport xpathNamespaceContext = null;
            if (assertionList instanceof XSObjectList) {
                xpathNamespaceContext = ((XSAssertImpl)((XSObjectList) assertionList).item(0)).
                                                                          getXPath2NamespaceContext();    
            }
            else {
                Vector assertVector = (Vector) assertionList;
                xpathNamespaceContext = ((XSAssertImpl)assertVector.get(0)).getXPath2NamespaceContext();
            }
            
            Map assertProcessorParams = new HashMap();
            assertProcessorParams.put("XPATH2_NS_CONTEXT", xpathNamespaceContext);
            // initialize the assertions processor
            initializeAssertProcessor(assertProcessorParams);
        }
       
        // invoke the assertions processor method
        if (fAssertionProcessor != null) {
           // construct the augmentations object, for assertions
           AugmentationsImpl assertAugs = new AugmentationsImpl();
           assertAugs.putItem("ASSERT", assertionList);
           fAssertionProcessor.startElement(element, attributes, assertAugs);
        }
        
    } // handleStartElement
    
    
    /*
     * Assertions processing interface during the XNI event, 'handleEndElement' in XMLSchemaValidator.
     */
    public void handleEndElement(QName element, XSElementDecl elemDecl, 
                                 XSTypeDefinition typeDef, XSNotationDecl notation,
                                 XSGrammarBucket grammarBucket, boolean atomicValueValid) {
        
        if (fAssertionProcessor != null) {
            try {
                // create the ElementPSVImpl object, for assertions
                ElementPSVImpl assertPSVI = new ElementPSVImpl();
                assertPSVI.fDeclaration = elemDecl;
                assertPSVI.fTypeDecl = typeDef;
                assertPSVI.fNotation = notation;
                assertPSVI.fGrammars = grammarBucket.getGrammars();

                // construct the augmentations object for assertions.
                // store assertPSVI into the augmentations.
                AugmentationsImpl assertAugs = new AugmentationsImpl();
                assertAugs.putItem(Constants.ELEMENT_PSVI, assertPSVI);
                assertAugs.putItem("ATOMIC_VALUE_VALIDITY", Boolean.valueOf(atomicValueValid));
                fAssertionProcessor.endElement(element, assertAugs);
            } catch (Exception ex) {
                throw new XNIException(ex.getMessage(), ex);
            }
        }
          
    } // handleEndElement
    
    
    /*
     * Accumulate a list of assertions (fetch from the underlying XSModel instance) to be processed
     * for the current context. Return the assertions list.
     */
    private List getAssertsForEvaluation(QName element, XMLAttributes attributes) {
        
       XSTypeDefinition typeDef = xmlSchemaValidator.fCurrentPSVI.getTypeDefinition();

       List assertionList = null;
            
       if (typeDef.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
           // if element's schema type is a "complex type"               
           XSObjectListImpl complexTypeAsserts = getAssertsFromComplexType(typeDef, attributes);
           if (complexTypeAsserts.size() > 0) {
               assertionList = complexTypeAsserts;             
           }
       }
       else if (typeDef.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
           // if element's schema type is a "simple type"
           assertionList = getAssertsFromSimpleType(typeDef);            
       }
               
       return assertionList;
         
    } // getAssertsForEvaluation


    /*
     * Accumulate assertions from a complex type.
     */
    private XSObjectListImpl getAssertsFromComplexType(XSTypeDefinition typeDef, XMLAttributes attributes) {
        
        XSObjectListImpl complexTypeAsserts = new XSObjectListImpl();

        XSComplexTypeDefinition complexTypeDef = (XSComplexTypeDefinition) typeDef;

        XSObjectList primaryAssertions = complexTypeDef.getAssertions();
        if (primaryAssertions.getLength() > 0) {
            for (int i = 0; i < primaryAssertions.getLength(); i++) {
                complexTypeAsserts.addXSObject((XSAssert) primaryAssertions.get(i));
            }
        }

        // add assertion facets, from "complexType -> simpleContent -> restriction"
        XSSimpleTypeDefinition simpleContentType = complexTypeDef.getSimpleType();
        if (simpleContentType != null) {
            XSObjectList complexTypeFacets = simpleContentType.getMultiValueFacets();
            for (int i = 0; i < complexTypeFacets.getLength(); i++) {
                XSMultiValueFacet facet = (XSMultiValueFacet) complexTypeFacets.item(i);
                if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                    Vector simpleContentAsserts = facet.getAsserts();
                    for (int simpleAssertIdx = 0; simpleAssertIdx < simpleContentAsserts.size(); simpleAssertIdx++) {
                        XSAssert simpleContentAssert = (XSAssert) simpleContentAsserts.get(simpleAssertIdx);
                        complexTypeAsserts.addXSObject(simpleContentAssert);
                    }
                }
            }
        }

        // find assertions from attributes of a complex type, and add them to the parent assertions list.
        XSObjectListImpl attrAsserts = getAssertsFromAttributes(attributes);
        for (int attrAssertIdx = 0; attrAssertIdx < attrAsserts.getLength(); attrAssertIdx++) {
            complexTypeAsserts.addXSObject(attrAsserts.item(attrAssertIdx)); 
        }

        return complexTypeAsserts;
            
    } // getAssertsFromComplexType


    /*
     * Get assertions from attributes of a complex type.
     */
    private XSObjectListImpl getAssertsFromAttributes(XMLAttributes attributes) {
        
        XSObjectListImpl attrAssertList = new XSObjectListImpl();
        
        for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
            Augmentations attrAugs = attributes.getAugmentations(attrIndx);
            AttributePSVImpl attrPSVI = (AttributePSVImpl) attrAugs.getItem(Constants.ATTRIBUTE_PSVI);
            XSSimpleTypeDefinition attrType = (XSSimpleTypeDefinition) attrPSVI.getTypeDefinition();
            if (attrType != null) {
                // this accumulates assertions only for simpleType -> restriction
                XSObjectList facets = attrType.getMultiValueFacets();

                // simpleType variety is 'unknown/absent' at the moment                    
                short attrTypeVariety = XSSimpleTypeDefinition.VARIETY_ABSENT;

                if (facets.getLength() == 0 && attrType.getItemType() != null) {
                    // facets for simpleType -> list
                    attrTypeVariety = XSSimpleTypeDefinition.VARIETY_LIST;
                    facets = (XSObjectListImpl) attrType.getItemType().getMultiValueFacets();    
                }
                else if (attrType.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION) {
                    attrTypeVariety = XSSimpleTypeDefinition.VARIETY_UNION;
                    // Special handling for assertions on "simpleType -> union" cases. Adding an assertion here,
                    // for determining the XSModel NamespaceContext. This particular assertion object is not 
                    // actually evaluated. For simpleType's with variety union, assertions are later again determined 
                    // in XMLAssertPsychopathImpl, which are evaluated to determine validity of an XML instance.               
                    XSAssertImpl assertImpl = getFirstAssertFromUnionMemberTypes(attrType.getMemberTypes());
                    if (assertImpl != null) {
                        assertImpl.setTypeDefinition(attrType);
                        assertImpl.setVariety(attrTypeVariety);
                        assertImpl.setAttrName(attributes.getLocalName(attrIndx));
                        assertImpl.setAttrValue(attributes.getValue(attrIndx));
                        attrAssertList.addXSObject(assertImpl);
                    }
                }

                // iterate all the facets, for attributes having the simpleType variety "atomic | list", and
                // accumulate assertions from them.
                for (int facetIdx = 0; facetIdx < facets.getLength(); facetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) facets.item(facetIdx);
                    if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                        Vector attrAsserts = facet.getAsserts();
                        for (int j = 0; j < attrAsserts.size(); j++) {
                            XSAssertImpl attrAssert = (XSAssertImpl) attrAsserts.elementAt(j);
                            attrAssert.setAttrName(attributes.getLocalName(attrIndx));
                            attrAssert.setAttrValue(attributes.getValue(attrIndx));
                            attrAssert.setVariety(attrTypeVariety);                                
                            attrAssertList.addXSObject(attrAssert);    
                        }
                        // break from the for loop
                        break;
                    }
                }                
            }           
        }
        
        return attrAssertList;
        
    } // getAssertsFromAttributes
    
    
    /*
     * Get assertions from a simpleType.
     */
    private List getAssertsFromSimpleType(XSTypeDefinition typeDef) {
        
        List simpleTypeAsserts = null;

        XSSimpleTypeDefinition simpleTypeDef = (XSSimpleTypeDefinition) typeDef;                     

        XSObjectListImpl facets = (XSObjectListImpl) simpleTypeDef.getMultiValueFacets();

        if (facets.getLength() == 0 && simpleTypeDef.getItemType() != null) {
            // facets for simpleType -> list
            facets = (XSObjectListImpl) simpleTypeDef.getItemType().getMultiValueFacets();    
        }
        else if (simpleTypeDef.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION) {
            // Special handling for assertions on simpleType -> union cases. Adding an assertion here,
            // for determining the NamespaceContext.
            XSAssertImpl assertImpl = getFirstAssertFromUnionMemberTypes(simpleTypeDef.getMemberTypes());
            if (assertImpl != null) {
                simpleTypeAsserts = new Vector();
                simpleTypeAsserts.add(assertImpl);
            }
        }

        // iterate all the facets, having the 'simple type' variety "atomic | list", and accumulate assertions
        // from them.
        for (int facetIdx = 0; facetIdx < facets.getLength(); facetIdx++) {
            XSMultiValueFacet facet = (XSMultiValueFacet) facets.item(facetIdx);
            if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                if (simpleTypeAsserts == null) {
                    simpleTypeAsserts = new Vector();   
                }                  
                simpleTypeAsserts.addAll(facet.getAsserts());                  
            }
        }

        return simpleTypeAsserts;
        
    } // getAssertsFromSimpleType
    
    
    /*
     * Get the 1st assertion from the member types of simpleType union. Needed to get an schema
     * "namespace context", which is available for example, in the 1st assertion in the assertions
     * list.
     */
    private XSAssertImpl getFirstAssertFromUnionMemberTypes(XSObjectList memberTypes) {
        
        XSAssertImpl assertImpl = null;
        
        for (int memberTypeIdx = 0; memberTypeIdx < memberTypes.getLength(); memberTypeIdx++) {
            XSSimpleTypeDefinition memType = (XSSimpleTypeDefinition) memberTypes.item(memberTypeIdx);
            if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(memType.getNamespace())) {
                XSObjectList memberTypeFacets = memType.getMultiValueFacets();
                for (int memberTypeFacetIdx = 0; memberTypeFacetIdx < memberTypeFacets.getLength(); 
                                                                                   memberTypeFacetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) memberTypeFacets.item(memberTypeFacetIdx);
                    if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                        Vector assertFacets = facet.getAsserts();
                        assertImpl = (XSAssertImpl) assertFacets.get(0);                        
                        // return the 1st assertion that's found
                        return assertImpl;
                    }
                 }
             }
         }
        
         return assertImpl;

    } // getFirstAssertFromUnionMemberTypes
    
    
    /*
     * Method to initialize the assertions processor.
     */
    private void initializeAssertProcessor(Map assertParams) {
             
        String assertProcessorProp;
        try {
            assertProcessorProp = SecuritySupport.getSystemProperty("org.apache.xerces.assertProcessor");
        }
        catch (SecurityException se) {
            assertProcessorProp = null;
        }
        
        if (assertProcessorProp == null || assertProcessorProp.length() == 0) {
            // if assertion processor is not specified via a system property, initialize it to
            // the "PsychoPath XPath 2.0" processor.
            fAssertionProcessor = new XMLAssertPsychopathImpl(assertParams);
        } 
        else {
            try {
                ClassLoader cl = ObjectFactory.findClassLoader();
                Class assertClass = ObjectFactory.findProviderClass(assertProcessorProp, cl, true);
                fAssertionProcessor = (XMLAssertHandler) assertClass.newInstance();
            } 
            catch (ClassNotFoundException ex) {
                throw new XNIException(ex.getMessage(), ex);
            } 
            catch (InstantiationException ex) {
                throw new XNIException(ex.getMessage(), ex);
            } 
            catch (IllegalAccessException ex) {
                throw new XNIException(ex.getMessage(), ex);
            }
        }
        
        fAssertionProcessor.setProperty("http://apache.org/xml/properties/assert/validator", xmlSchemaValidator);
        
    } // initializeAssertProcessor
    
} // class XSDAssertionValidator
