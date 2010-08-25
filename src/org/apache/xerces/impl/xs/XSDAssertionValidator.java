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
import org.apache.xerces.impl.dv.XSSimpleType;
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
    
    // XMLSchemaValidator instance that acts as context for the present 
    // assertion validator subcomponent. Passed into the constructor of 
    // this object.
    XMLSchemaValidator xmlSchemaValidator = null;
    
    // assertion processor object reference
    XMLAssertHandler fAssertionProcessor = null;
    
    
    /*
     * Class constructor
     */
    public XSDAssertionValidator(XMLSchemaValidator xmlSchemaValidator) {
       this.xmlSchemaValidator = xmlSchemaValidator; 
    }

    
    /*
     * Assertions processing interface during the XNI event, 'handleCharacters'
     * in XMLSchemaValidator. 
     */
    public void characterDataHandler(XMLString text) {
        
        if (fAssertionProcessor != null) {
           fAssertionProcessor.characters(text);
        }
        
    } // characterDataHandler
    
    
    /*
     * Assertions processing interface during the XNI event, 'handleStartElement'
     * in XMLSchemaValidator.
     */
    public void handleStartElement(QName element, XMLAttributes attributes) {
       
        // get list of assertions for processing
        List assertionList = getAssertsForEvaluation(element, attributes);
        
        // instantiate the assertions processor
        if (assertionList != null && fAssertionProcessor == null) {
            // construct parameter values for the assertion processor
            NamespaceSupport xpathNamespaceContext = null;
            if (assertionList instanceof XSObjectList) {
                xpathNamespaceContext = ((XSAssertImpl)((XSObjectList)
                         assertionList).item(0)).getXPath2NamespaceContext();    
            }
            else {
                Vector assertVector = (Vector) assertionList;
                xpathNamespaceContext = ((XSAssertImpl)assertVector.
                                   get(0)).getXPath2NamespaceContext();
            }
            
            Map assertProcessorParams = new HashMap();
            assertProcessorParams.put("XPATH2_NS_CONTEXT", 
                                      xpathNamespaceContext);
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
     * Assertions processing interface during the XNI event, 'handleEndElement'
     * in XMLSchemaValidator.
     */
    public void handleEndElement(QName element, 
                                 XSElementDecl elemDecl, 
                                 XSTypeDefinition typeDef, 
                                 XSNotationDecl notation,
                                 XSGrammarBucket grammarBucket,
                                 boolean atomicValueValid) {
        
        if (fAssertionProcessor != null) {
            try {
                // create the ElementPSVImpl object, for assertions
                ElementPSVImpl assertPSVI = new ElementPSVImpl();
                assertPSVI.fDeclaration = elemDecl;
                assertPSVI.fTypeDecl = typeDef;
                assertPSVI.fNotation = notation;
                assertPSVI.fGrammars = grammarBucket.getGrammars();

                // construct the augmentations object for assertions.
                // store assertPSVI into the augmentations
                AugmentationsImpl assertAugs = new AugmentationsImpl();
                assertAugs.putItem(Constants.ELEMENT_PSVI, assertPSVI);
                assertAugs.putItem("ATOMIC_VALUE_VALIDITY", Boolean.valueOf
                                               (atomicValueValid));
                fAssertionProcessor.endElement(element, assertAugs);
            } catch (Exception ex) {
                throw new XNIException(ex.getMessage(), ex);
            }
        }
          
    } // handleEndElement
    
    
    /*
     * Accumulate a list of assertions (fetch from the underlying XSModel
     * instance) to be processed for the current context. Return the 
     * assertions list.
     */
    private List getAssertsForEvaluation(QName element,
                                         XMLAttributes attributes) {
        
       XSTypeDefinition typeDef = xmlSchemaValidator.fCurrentPSVI.
                                                         getTypeDefinition();

       List assertionList = null;
            
       if (typeDef.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
           // if element's governing type is a "complex type"
           XSObjectListImpl assertions = new XSObjectListImpl();                
           XSComplexTypeDefinition complexTypeDef = (XSComplexTypeDefinition) 
                                                                typeDef;
          
           XSObjectList complexTypeAsserts = complexTypeDef.getAssertions();
           if (complexTypeAsserts.getLength() > 0) {
              for (int i = 0; i < complexTypeAsserts.getLength(); i++) {
                 assertions.addXSObject((XSAssert)complexTypeAsserts.get(i));
              }
           }
          
           // add assertion facets, from "complexType -> simpleContent -> 
           // restriction".
           XSSimpleTypeDefinition simpleTypeDef = complexTypeDef.getSimpleType();
           if (simpleTypeDef != null) {
               XSObjectList complexTypeFacets = simpleTypeDef.getMultiValueFacets();
               for (int i = 0; i < complexTypeFacets.getLength(); i++) {
                  XSMultiValueFacet facet = (XSMultiValueFacet) 
                                                    complexTypeFacets.item(i);
                  if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                      Vector simpleContentAsserts = facet.getAsserts();
                      for (int simpleAssertIdx = 0; simpleAssertIdx < 
                                   simpleContentAsserts.size(); simpleAssertIdx++) {
                         XSAssert simpleContentAssert = (XSAssert)
                                           simpleContentAsserts.get(simpleAssertIdx);
                         assertions.addXSObject(simpleContentAssert);
                      }
                   }
                }
            }

            // there could be assertions, to be evaluated on attributes. add these
            // assertions to the list of assertions to be processed.
            for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
                Augmentations attrAugs = attributes.getAugmentations(attrIndx);
                AttributePSVImpl attrPSVI = (AttributePSVImpl)attrAugs.getItem
                                                   (Constants.ATTRIBUTE_PSVI);
                XSSimpleTypeDefinition attrType = (XSSimpleTypeDefinition)attrPSVI.
                                                          getTypeDefinition();
                if (attrType != null) {
                    XSObjectList facets = attrType.getMultiValueFacets();              
                    for (int i = 0; i < facets.getLength(); i++) {
                        XSMultiValueFacet facet = (XSMultiValueFacet) 
                                                              facets.item(i);
                        if (facet.getFacetKind() == XSSimpleTypeDefinition.
                                                               FACET_ASSERT) {
                            Vector attrAsserts = facet.getAsserts();
                            for (int j = 0; j < attrAsserts.size(); j++) {
                                XSAssertImpl attrAssert = (XSAssertImpl) 
                                                         attrAsserts.elementAt(j);
                                attrAssert.setAttrName(attributes.getLocalName
                                                                    (attrIndx));
                                attrAssert.setAttrValue(attributes.getValue
                                                                   (attrIndx));
                                assertions.addXSObject(attrAssert);    
                             }                       
                             break;
                         }
                     }
                  }
              }
              
              if (assertions.size() > 0) {
                 assertionList = assertions;             
              }
           }
           else if (typeDef.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
               // if element's governing type is a "simple type"
               XSSimpleTypeDefinition simpleTypeDef = (XSSimpleTypeDefinition) 
                                                                 typeDef;                     
            
               XSObjectListImpl facets = (XSObjectListImpl) simpleTypeDef.
                                                     getMultiValueFacets();
            
               if (facets.getLength() == 0 && simpleTypeDef.getItemType() != 
                                                              null) {
                   // facets for list -> simpleType
                   facets = (XSObjectListImpl) simpleTypeDef.getItemType().
                                                   getMultiValueFacets();    
               }
               else if (simpleTypeDef.getVariety() == XSSimpleType.
                                                         VARIETY_UNION) {
                   // assertions on a simpleType with variety union, are
                   // handled in a special way.
                   // NOTE: assertions on simpleType -> union are not 
                   // accumulated here.
                   // create a dummy assertions list (so that a non-null 
                   // assertions context is pushed on to a run-time assertions 
                   // stack).                
                   XSAssertImpl assertImpl = getFirstAssertFromUnionMemberTypes(
                                                simpleTypeDef.getMemberTypes());
                   if (assertImpl != null) {
                       // this is done to pass a correct schema NamespaceContext
                       // for union assertions evaluation. since NamespaceContext
                       // is available in an assertions object, therefore we
                       // utilize the 1st assertions on a union for this need.
                       
                       // REVISIT: if we can store the schema namespace context
                       // in a global store, then we can avoid storing it in
                       // every assertions object.
                       assertionList = new Vector();
                       assertionList.add(assertImpl);
                    }
               }
            
               // iterate 'multi value' facets and get assertions from them
               for (int facetIdx = 0; facetIdx < facets.getLength(); 
                                                       facetIdx++) {
                   XSMultiValueFacet facet = (XSMultiValueFacet) facets.
                                                          item(facetIdx);
                   if (facet.getFacetKind() == XSSimpleTypeDefinition.
                                                           FACET_ASSERT) {
                       if (assertionList == null) {
                          assertionList = new Vector();   
                       }                  
                       assertionList.addAll(facet.getAsserts());                  
                   }
               }            
          }
               
          return assertionList;
         
    } // getAssertsForEvaluation
    
    
    /*
     * Get the 1st assertion from the member types of union. Needed to get 
     * an schema 'namespace context', which is available for example, in the
     * 1st assertion in the assertions list.
     */
    private XSAssertImpl getFirstAssertFromUnionMemberTypes(XSObjectList 
                                                             memberTypes) {
        
        XSAssertImpl assertImpl = null;
        
        for (int memberTypeIdx = 0; memberTypeIdx < memberTypes.getLength();
                                                       memberTypeIdx++) {
            XSSimpleTypeDefinition memType = (XSSimpleTypeDefinition) 
                                                  memberTypes.item(memberTypeIdx);
            if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(memType.getNamespace())
                                                                            ) {
                XSObjectList memberTypeFacets = memType.getMultiValueFacets();
                for (int memberTypeFacetIdx = 0; memberTypeFacetIdx < 
                                                   memberTypeFacets.getLength(); 
                                                   memberTypeFacetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) memberTypeFacets.
                                                       item(memberTypeFacetIdx);
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
        
        String assertProcessorProp = System
                .getProperty("org.apache.xerces.assertProcessor");
        
        if (assertProcessorProp == null || assertProcessorProp.equals("")) {
            // if assertion processor is not specified via a system
            // property, initialize it to the PsychoPath XPath 2.0 processor.
            fAssertionProcessor = new XMLAssertPsychopathImpl(assertParams);
        } else {
            try {
                Class assertClass = ClassLoader.getSystemClassLoader()
                        .loadClass(assertProcessorProp);
                fAssertionProcessor = (XMLAssertHandler) 
                                             assertClass.newInstance();
            } catch (ClassNotFoundException ex) {
                throw new XNIException(ex.getMessage(), ex);
            } catch (InstantiationException ex) {
                throw new XNIException(ex.getMessage(), ex);
            } catch (IllegalAccessException ex) {
                throw new XNIException(ex.getMessage(), ex);
            }
        }
        
        fAssertionProcessor.setProperty
                    ("http://apache.org/xml/properties/assert/validator", 
                                                      xmlSchemaValidator);
        
    } // initializeAssertProcessor
    
} // class XMLAssertionValidator
