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

package org.apache.xerces.impl.xs.traversers;

import org.apache.xerces.impl.xpath.XPath20;
import org.apache.xerces.impl.xpath.XPathException;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.alternative.Test;
import org.apache.xerces.impl.xs.alternative.XSTypeAlternativeImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.JFlexCupParser;
import org.eclipse.wst.xml.xpath2.processor.XPathParser;
import org.eclipse.wst.xml.xpath2.processor.XPathParserException;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.w3c.dom.Element;

/**
 * The traverser implementation for XML Schema 1.1 'type alternative' component.
 * 
 * <alternative
 *    id = ID
 *    test = an XPath expression
 *    type = QName
 *    xpathDefaultNamespace = (anyURI | (##defaultNamespace | ##targetNamespace | ##local))
 *    {any attributes with non-schema namespace . . .}>
 *    Content: (annotation?, (simpleType | complexType)?)
 * </alternative>
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
class XSDTypeAlternativeTraverser extends XSDAbstractTraverser {

    XSDTypeAlternativeTraverser (XSDHandler handler,
            XSAttributeChecker attrChecker) {
        super(handler, attrChecker);
    }

    /**
     * Traverse the given alternative element and update the
     * schema grammar. Validate the content of the type alternative
     * element.
     */
    public void traverse(Element altElement, XSElementDecl element,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar) {

        Object[] attrValues = fAttrChecker.checkAttributes(altElement, false, schemaDoc);
        QName typeAtt = (QName) attrValues[XSAttributeChecker.ATTIDX_TYPE];
        String test = (String) attrValues[XSAttributeChecker.ATTIDX_XPATH];
        String xpathNS = (String) attrValues[XSAttributeChecker.ATTIDX_XPATHDEFAULTNS];

        // get 'annotation'
        Element childNode = DOMUtil.getFirstChildElement(altElement);
        XSAnnotationImpl annotation = null;
        //first child could be an annotation
        if (childNode != null && DOMUtil.getLocalName(childNode).equals(SchemaSymbols.ELT_ANNOTATION)) {
            annotation = traverseAnnotationDecl(childNode, attrValues, false, schemaDoc);
            //now move on to the next child element
            childNode = DOMUtil.getNextSiblingElement(childNode);
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(altElement);
            if (text != null) {
                annotation = traverseSyntheticAnnotation(altElement, text, attrValues, false, schemaDoc);
            }          
            //here we remain in the first child element
        }

        XSObjectList annotations = null;
        if (annotation != null) {
            annotations = new XSObjectListImpl();
            ((XSObjectListImpl)annotations).addXSObject(annotation);
        }
        else {
            //if no annotations are present add an empty list to the type alternative
            annotations = XSObjectListImpl.EMPTY_LIST;
        }

        // get 'type definition'
        XSTypeDefinition alternativeType = null;
        boolean hasAnonType = false;
        
        if (typeAtt != null) {
            alternativeType = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.TYPEDECL_TYPE, typeAtt, altElement);
        }
        
        // check whether the childNode still points to something...
        // if it does it must be an anonymous type declaration
        if (childNode != null) {
            // traverse any anonymous type declarations present
            // do not care whether the type attr is present or not
            String childName = DOMUtil.getLocalName(childNode);
            XSTypeDefinition typeDef = null;
            if (childName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                typeDef = fSchemaHandler.fComplexTypeTraverser.traverseLocal(childNode, schemaDoc, grammar);
                hasAnonType = true;
                childNode = DOMUtil.getNextSiblingElement(childNode);
            }
            else if (childName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                typeDef = fSchemaHandler.fSimpleTypeTraverser.traverseLocal(childNode, schemaDoc, grammar);
                hasAnonType = true;
                childNode = DOMUtil.getNextSiblingElement(childNode);
            }
            
            if (alternativeType == null) {
                alternativeType = typeDef;
            }
            
            // type and either <simpleType> or <complexType> are mutually exclusive.
            if (hasAnonType && (typeAtt != null)) {
                reportSchemaError("src-type-alternative.3.12.13.1", null, altElement);
            }
        }

        // if the type definition component is not present..
        // i.e. test attr value is absent and no anonymous types are defined
        if (typeAtt == null && !hasAnonType) {
            reportSchemaError("src-type-alternative.3.12.13.2", null, altElement);
        }
        
        // fall back to the element declaration's type
        if (alternativeType == null) {
            alternativeType= element.fType;
        }

        // not expecting any more children
        if (childNode != null) {
            //reportSchemaError("s4s-elt-must-match.1", new Object[] { "type alternative", "(annotation?, (simpleType|complexType)?)", childNode.getLocalName() }, altElement);
            reportSchemaError("s4s-elt-must-match.1", new Object[]{"type alternative", "(annotation?, (simpleType | complexType)?)", DOMUtil.getLocalName(childNode)}, childNode);
        }

        // create type alternative
        XSTypeAlternativeImpl typeAlternative = new XSTypeAlternativeImpl(element.fName, alternativeType, annotations);

        // now look for other optional attributes like test and xpathDefaultNamespace
        if (test != null) {
            Test testExpr = null;
            //set the test attribute value
            try {
               testExpr = new Test(new XPath20(test, fSymbolTable, schemaDoc.fNamespaceSupport),
                                       typeAlternative,
                                       schemaDoc.fNamespaceSupport);
            } 
            catch (XPathException e) {
               // fall back to full XPath 2.0 support, with PsychoPath engine
               try {
                  XPathParser xpp = new JFlexCupParser();
                  XPath xp = xpp.parse("boolean(" + test + ")");
                  testExpr = new Test(xp, typeAlternative, schemaDoc.fNamespaceSupport);
               } catch(XPathParserException ex) {
                  reportSchemaError("c-cta-xpath", new Object[] { test }, altElement);
                  //if the XPath is invalid, create a Test without an expression
                  testExpr = new Test((XPath20) null, typeAlternative,
                                       schemaDoc.fNamespaceSupport);
               }                
            }            
            typeAlternative.setTest(testExpr);
        }

        if (xpathNS != null) {
            //set the xpathDefaultNamespace attribute value
            typeAlternative.setXPathDefauleNamespace(xpathNS);
        }

        grammar.addTypeAlternative(element, typeAlternative);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
    }
}
