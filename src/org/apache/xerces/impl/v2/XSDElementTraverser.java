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

import  org.apache.xerces.impl.XMLErrorReporter;
import  org.apache.xerces.util.DOMUtil;
import  org.apache.xerces.xni.QName;
import  org.w3c.dom.Element;
import java.util.Hashtable;


/**
 * The element declaration schema component traverser.
 * <element
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction | substitution))
 *   default = string
 *   final = (#all | List of (extension | restriction))
 *   fixed = string
 *   form = (qualified | unqualified)
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   name = NCName
 *   nillable = boolean : false
 *   ref = QName
 *   substitutionGroup = QName
 *   type = QName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, ((simpleType | complexType)?, (unique | key | keyref)*))
 * </element>
 *
 * @version $Id$
 */
class XSDElementTraverser extends XSDAbstractTraverser{

    protected static final QName ANY_TYPE        = new QName(null,
                                                             SchemaSymbols.ATTVAL_ANYTYPE,
                                                             SchemaSymbols.ATTVAL_ANYTYPE,
                                                             SchemaSymbols.URI_SCHEMAFORSCHEMA);
    protected static final QName ANY_SIMPLE_TYPE = new QName(null,
                                                             SchemaSymbols.ATTVAL_ANYSIMPLETYPE,
                                                             SchemaSymbols.ATTVAL_ANYTYPE,
                                                             SchemaSymbols.URI_SCHEMAFORSCHEMA);

    protected XSElementDecl fTempElementDecl = new XSElementDecl();

    //
    // REVISIT: we should be able to acces
    // SubstitutionGroupHandler
    //

    XSDElementTraverser (XSDHandler handler,
                         XMLErrorReporter errorReporter,
                         XSAttributeChecker gAttrCheck) {
        super(handler, errorReporter, gAttrCheck);
    }

    /**
     * Traverse a locally declared element (or an element reference).
     *
     * To handle the recursive cases effeciently, we delay the traversal
     * and return an empty particle node. We'll fill in this particle node
     * later after we've done with all the global declarations.
     *
     * @param  elmDecl
     * @param  schemaDoc
     * @param  grammar
     * @return the particle node index
     */
    int traverseLocal(Element elmDecl,
                      XSDocumentInfo schemaDoc,
                      SchemaGrammar grammar) {
        return -1;
    }

    /**
     * Traverse a locally declared element (or an element reference).
     *
     * This is the real traversal method. It's called after we've done with
     * all the global declarations.
     *
     * @param  particleIdx
     * @param  elmDecl
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @return the particle node index
     */
    int traverseLocal(int particleIdx,
                      Element elmDecl,
                      Hashtable attrValues,
                      XSDocumentInfo schemaDoc,
                      SchemaGrammar grammar) {
        return -1;
    }

    /**
     * Traverse a globally declared element.
     *
     * @param  elmDecl
     * @param  schemaDoc
     * @param  grammar
     * @return the element declaration index
     */
    int traverseGlobal(Element elmDecl,
                       XSDocumentInfo schemaDoc,
                       SchemaGrammar grammar) {

        // General Attribute Checking
        Hashtable attrValues = fAttrChecker.checkAttributes(elmDecl, true);

        return traverseNamedElement(elmDecl, attrValues, schemaDoc, grammar, true);
    }

    /**
     * Traverse a globally declared element.
     *
     * @param  elmDecl
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @param  isGlobal
     * @return the element declaration index
     */
    int traverseNamedElement(Element elmDecl,
                             Hashtable attrValues,
                             XSDocumentInfo schemaDoc,
                             SchemaGrammar grammar,
                             boolean isGlobal) {

        Integer abstractAtt  = (Integer) attrValues.get(SchemaSymbols.ATT_ABSTRACT);
        Integer blockAtt     = (Integer) attrValues.get(SchemaSymbols.ATT_BLOCK);
        String  defaultAtt   = (String)  attrValues.get(SchemaSymbols.ATT_DEFAULT);
        Integer finalAtt     = (Integer) attrValues.get(SchemaSymbols.ATT_FINAL);
        String  fixedAtt     = (String)  attrValues.get(SchemaSymbols.ATT_FIXED);
        Integer formAtt      = (Integer) attrValues.get(SchemaSymbols.ATT_FORM);
        String  nameAtt      = (String)  attrValues.get(SchemaSymbols.ATT_NAME);
        Integer nillableAtt  = (Integer) attrValues.get(SchemaSymbols.ATT_NILLABLE);
        QName   refAtt       = (QName)   attrValues.get(SchemaSymbols.ATT_REF);
        QName   subGroupAtt  = (QName)   attrValues.get(SchemaSymbols.ATT_SUBSTITUTIONGROUP);
        QName   typeAtt      = (QName)   attrValues.get(SchemaSymbols.ATT_TYPE);

        //REVISIT: notation datatype error checking
        //checkEnumerationRequiredNotation(nameStr, typeStr);

		if (defaultAtt != null && fixedAtt != null) {
			// REVISIT:  localize
			reportGenericSchemaError("src-element.1: an element cannot have both \"fixed\" and \"default\" present at the same time");
        }

        if (nameAtt == null) {
            // REVISIT: Localize
            if (isGlobal)
                reportGenericSchemaError("globally-declared element must have a name");
            else
                reportGenericSchemaError("src-element.2.1: a local element must have a name or a ref attribute present");
        }

        // parse out 'block', 'final', 'nillable', 'abstract'
        short blockSet = blockAtt == null ? SchemaSymbols.EMPTY_SET : blockAtt.shortValue();
        short finalSet = finalAtt == null ? SchemaSymbols.EMPTY_SET : finalAtt.shortValue();
        short elementMiscFlags = 0;
        if ((nillableAtt.intValue() & SchemaSymbols.REAL_VALUE) == SchemaSymbols.BOOLEAN_TRUE)
            elementMiscFlags |= XSElementDecl.NILLABLE;
        if ((abstractAtt.intValue() & SchemaSymbols.REAL_VALUE) == SchemaSymbols.BOOLEAN_TRUE)
            elementMiscFlags |= XSElementDecl.ABSTRACT;
        // make the property of the element's value being fixed also appear in elementMiscFlags
        if (fixedAtt != null)
            elementMiscFlags |= XSElementDecl.FIXED;

        // element has a single child element, either a datatype or a type, null if primitive
        Element child = DOMUtil.getFirstChildElement(elmDecl);

        if(child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
			traverseAnnotationDecl(child, attrValues);
            child = DOMUtil.getNextSiblingElement(child);
		}
        if(child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION))
            // REVISIT: Localize
            reportGenericSchemaError("element declarations can contain at most one annotation Element Information Item");

        int elementType = -1;

        boolean noErrorSoFar = true;
        boolean haveAnonType = false;

        // Handle Anonymous type if there is one
        if (child != null) {
            String childName = DOMUtil.getLocalName(child);

            if (childName.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                // Determine what the type name will be
                elementType = fSchemaHandler.fComplexTypeTraverser.traverse(child, schemaDoc, grammar, null);
                if (elementType == -1 ) {
                    noErrorSoFar = false;
                    // REVISIT: Localize
                    reportGenericSchemaError("traverse complexType error in element '" + nameAtt +"'");
                }
                haveAnonType = true;
            	child = DOMUtil.getNextSiblingElement(child);
            }
            else if (childName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                elementType = fSchemaHandler.fSimpleTypeTraverser.traverse(child, schemaDoc, grammar);
                if (elementType == -1) {
                    noErrorSoFar = false;
                    // REVISIT: Localize
                    reportGenericSchemaError("traverse simpleType error in element '" + nameAtt +"'");
                }
                haveAnonType = true;
            	child = DOMUtil.getNextSiblingElement(child);
            }

			// see if there's something here; it had better be key, keyref or unique.
			if (child != null)
                childName = DOMUtil.getLocalName(child);
			while ((child != null) && ((childName.equals(SchemaSymbols.ELT_KEY))
                    || (childName.equals(SchemaSymbols.ELT_KEYREF))
                    || (childName.equals(SchemaSymbols.ELT_UNIQUE)))) {
            	child = DOMUtil.getNextSiblingElement(child);
                if (child != null) {
                    childName = DOMUtil.getLocalName(child);
                }
			}
			if (child != null) {
               	// REVISIT: Localize
            	noErrorSoFar = false;
                reportGenericSchemaError("src-element.0: the content of an element information item must match (annotation?, (simpleType | complexType)?, (unique | key | keyref)*)");
			}
        }

        // handle type="" here
        if (haveAnonType && (typeAtt != null)) {
            noErrorSoFar = false;
            // REVISIT: Localize
            reportGenericSchemaError( "src-element.3: Element '"+ nameAtt +
                                      "' have both a type attribute and a annoymous type child" );
        }
        // type specified as an attribute and no child is type decl.
        else if (!haveAnonType && typeAtt != null) {
            elementType = fSchemaHandler.getGlobalDecl(schemaDoc, fSchemaHandler.TYPE_TYPE, typeAtt);
            if (elementType == -1) {
                noErrorSoFar = false;
                reportGenericSchemaError("type not found: "+typeAtt.uri+":"+typeAtt.localpart);
            }
        }

        // Handle the substitutionGroup
        String subGroupNS = null;
        int subGroupIndex = -1;
/*        Element substitutionGroupElementDecl = null;
        int substitutionGroupElementDeclIndex = -1;

        // now we need to make sure that our substitution (if any)
        // is valid, now that we have all the requisite type-related info.
        String substitutionGroupUri = null;
        String substitutionGroupLocalpart = null;
        String substitutionGroupFullName = null;
        ComplexTypeInfo substitutionGroupEltTypeInfo = null;
        DatatypeValidator substitutionGroupEltDV = null;
        SchemaGrammar subGrammar = fSchemaGrammar;
        boolean ignoreSub = false;

        if ( substitutionGroupStr.length() > 0 ) {
            if(refAtt != null)
                // REVISIT: Localize
                reportGenericSchemaError("a local element cannot have a substitutionGroup");
            substitutionGroupUri =  resolvePrefixToURI(getPrefix(substitutionGroupStr));
            substitutionGroupLocalpart = getLocalPart(substitutionGroupStr);
            substitutionGroupFullName = substitutionGroupUri+","+substitutionGroupLocalpart;

            if ( !substitutionGroupUri.equals(fTargetNSURIString) ) {
                Grammar grammar = fGrammarResolver.getGrammar(substitutionGroupUri);
                if (grammar != null && grammar instanceof SchemaGrammar) {
                    subGrammar = (SchemaGrammar) grammar;
                    substitutionGroupElementDeclIndex = subGrammar.getElementDeclIndex(fStringPool.addSymbol(substitutionGroupUri),
                                                              fStringPool.addSymbol(substitutionGroupLocalpart),
                                                              TOP_LEVEL_SCOPE);
                    if (substitutionGroupElementDeclIndex<=-1) {
                        // REVISIT:  localize
                        noErrorSoFar = false;
                        reportGenericSchemaError("couldn't find substitutionGroup " + substitutionGroupLocalpart + " referenced by element " + nameStr
                                         + " in the SchemaGrammar "+substitutionGroupUri);

                    } else {
                        substitutionGroupEltTypeInfo = getElementDeclTypeInfoFromNS(substitutionGroupUri, substitutionGroupLocalpart);
                        if (substitutionGroupEltTypeInfo == null) {
                            substitutionGroupEltDV = getElementDeclTypeValidatorFromNS(substitutionGroupUri, substitutionGroupLocalpart);
                            //if (substitutionGroupEltDV == null) {
                                //TO DO: report error here;
                                //noErrorSoFar = false;
                                //reportGenericSchemaError("Could not find type for element '" +substitutionGroupLocalpart
                                //                 + "' in schema '" + substitutionGroupUri+"'");
                            //}
                        }
                    }
                } else {
                    // REVISIT:  locallize
                    noErrorSoFar = false;
                    reportGenericSchemaError("couldn't find a schema grammar with target namespace '" + substitutionGroupUri + "' for element '" + substitutionGroupStr + "'");
                }
            }
            else {
                substitutionGroupElementDecl = getTopLevelComponentByName(SchemaSymbols.ELT_ELEMENT, substitutionGroupLocalpart);
                if (substitutionGroupElementDecl == null) {
                    substitutionGroupElementDeclIndex =
                        fSchemaGrammar.getElementDeclIndex(fTargetNSURI, getLocalPartIndex(substitutionGroupStr),TOP_LEVEL_SCOPE);
                    if ( substitutionGroupElementDeclIndex == -1) {
                        noErrorSoFar = false;
                        // REVISIT: Localize
                        reportGenericSchemaError("unable to locate substitutionGroup affiliation element "
                                                  +substitutionGroupStr
                                                  +" in element declaration "
                                                  +nameStr);
                    }
                }
                else {
                    substitutionGroupElementDeclIndex =
                        fSchemaGrammar.getElementDeclIndex(fTargetNSURI, getLocalPartIndex(substitutionGroupStr),TOP_LEVEL_SCOPE);

                    if ( substitutionGroupElementDeclIndex == -1) {
                        // check for mutual recursion!
                        if(fSubstitutionGroupRecursionRegistry.contains(fTargetNSURIString+","+substitutionGroupElementDecl.getAttribute(SchemaSymbols.ATT_NAME))) {
                            ignoreSub = true;
                        } else {
                            fSubstitutionGroupRecursionRegistry.addElement(fTargetNSURIString+","+substitutionGroupElementDecl.getAttribute(SchemaSymbols.ATT_NAME));
                            traverseElementDecl(substitutionGroupElementDecl);
                            substitutionGroupElementDeclIndex =
                                fSchemaGrammar.getElementDeclIndex(fTargetNSURI, getLocalPartIndex(substitutionGroupStr),TOP_LEVEL_SCOPE);
                            fSubstitutionGroupRecursionRegistry.removeElement((Object)fTargetNSURIString+","+substitutionGroupElementDecl.getAttribute(SchemaSymbols.ATT_NAME));
                        }
                    }
                }

                if (!ignoreSub && substitutionGroupElementDeclIndex != -1) {
                    substitutionGroupEltTypeInfo = fSchemaGrammar.getElementComplexTypeInfo( substitutionGroupElementDeclIndex );
                    if (substitutionGroupEltTypeInfo == null) {
                        fSchemaGrammar.getElementDecl(substitutionGroupElementDeclIndex, fTempElementDecl);
                        substitutionGroupEltDV = fTempElementDecl.datatypeValidator;
                        //if (substitutionGroupEltDV == null) {
                            //TO DO: report error here;
                            //noErrorSoFar = false;
                            //reportGenericSchemaError("Could not find type for element '" +substitutionGroupLocalpart
                            //                         + "' in schema '" + substitutionGroupUri+"'");
                        //}
                    }
                }
            }
            if (substitutionGroupElementDeclIndex <= -1)
                ignoreSub = true;
            if(!ignoreSub)
                checkSubstitutionGroupOK(elementDecl, substitutionGroupElementDecl, noErrorSoFar, substitutionGroupElementDeclIndex, subGrammar, typeInfo, substitutionGroupEltTypeInfo, dv, substitutionGroupEltDV);
        }

        // this element is ur-type, check its substitutionGroup affiliation.
        // if there is substitutionGroup affiliation and not type definition found for this element,
        // then grab substitutionGroup affiliation's type and give it to this element
        if ( noErrorSoFar && typeInfo == null && dv == null ) {
			typeInfo = substitutionGroupEltTypeInfo;
			dv = substitutionGroupEltDV;
        }*/

        if (elementType == -1 && noErrorSoFar) {
            elementType = fSchemaHandler.getGlobalDecl(schemaDoc, fSchemaHandler.TYPE_TYPE, ANY_TYPE);
        }

        // Now we can handle validation etc. of default and fixed attributes,
        // since we finally have all the type information.
        /*if (fixedAtt != null)
            defaultAtt = fixedAtt;
        if (defaultAtt.length() != 0) {
            // REVISIT: get type declaration
            // check whether it's complex
            // if so, check whether mixed or simple contnet
            // if mixed, check whether it's emptible
            if(typeInfo != null &&
                    (typeInfo.contentType != XMLElementDecl.TYPE_MIXED_SIMPLE &&
                     typeInfo.contentType != XMLElementDecl.TYPE_MIXED_COMPLEX &&
                    typeInfo.contentType != XMLElementDecl.TYPE_SIMPLE)) {
                // REVISIT: Localize
                reportGenericSchemaError ("e-props-correct.2.1: element " + nameStr + " has a fixed or default value and must have a mixed or simple content model");
            }
            if(typeInfo != null &&
               (typeInfo.contentType == XMLElementDecl.TYPE_MIXED_SIMPLE ||
                typeInfo.contentType == XMLElementDecl.TYPE_MIXED_COMPLEX)) {
                if (!particleEmptiable(typeInfo.contentSpecHandle))
                    reportGenericSchemaError ("e-props-correct.2.2.2: for element " + nameStr + ", the {content type} is mixed, then the {content type}'s particle must be emptiable");
            }

            // get the simple type delaration, and validate
            try {
                if(dv != null) {
                    dv.validate(defaultStr, null);
                }
            } catch (InvalidDatatypeValueException ide) {
                reportGenericSchemaError ("e-props-correct.2: invalid fixed or default value '" + defaultStr + "' in element " + nameStr);
            }
        }

        // check whether ID type has a default/fixed value
        if (defaultStr.length() != 0 &&
            dv != null && dv instanceof IDDatatypeValidator) {
            reportGenericSchemaError ("e-props-correct.4: If the {type definition} or {type definition}'s {content type} is or is derived from ID then there must not be a {value constraint} -- element " + nameStr);
        }*/

        //
        // Create element decl
        //

        String namespace = "";
        if (isGlobal) {
            namespace = schemaDoc.fTargetNamespace;
        }
        else if ((formAtt.intValue() & SchemaSymbols.REAL_VALUE) == SchemaSymbols.FORM_QUALIFIED ||
                 schemaDoc.fAreLocalElementsQualified) {
            namespace = schemaDoc.fTargetNamespace;
        }

        // add element decl to the registry
        fTempElementDecl.clear();
        fTempElementDecl.fQName.setValues(null, nameAtt, nameAtt, namespace);
        fTempElementDecl.fTypeNS = typeAtt == null ? schemaDoc.fTargetNamespace : typeAtt.uri;
        fTempElementDecl.fXSTypeDecl = elementType;
        fTempElementDecl.fElementMiscFlags = elementMiscFlags;
        fTempElementDecl.fBlock = blockSet;
        fTempElementDecl.fFinal = finalSet;
        fTempElementDecl.fDefault = defaultAtt;
        fTempElementDecl.fSubGroupNS = subGroupNS;
        fTempElementDecl.fSubGroupIdx = subGroupIndex;
        int elementIndex = grammar.addElementDecl(fTempElementDecl);

        // REVISIT: process of substitutionGroup and idendity constraint
        // substitutionGroup: double-direction
        /*if (subGroupAtt != null && !ignoreSub) {
            fSchemaHandler.addElementDeclOneSubstitutionGroupQName(substitutionGroupElementDeclIndex, eltQName, fSchemaGrammar, elementIndex);
        }

        //
        // key/keyref/unique processing
        //

        Element ic = XUtil.getFirstChildElementNS(elementDecl, IDENTITY_CONSTRAINTS);
        if (ic != null) {
            Integer elementIndexObj = new Integer(elementIndex);
            Vector identityConstraints = (Vector)fIdentityConstraints.get(elementIndexObj);
            if (identityConstraints == null) {
                identityConstraints = new Vector();
                fIdentityConstraints.put(elementIndexObj, identityConstraints);
            }
            while (ic != null) {
                if (DEBUG_IC_DATATYPES) {
                    System.out.println("<ICD>: adding ic for later traversal: "+ic);
                }
                identityConstraints.addElement(ic);
                ic = XUtil.getNextSiblingElementNS(ic, IDENTITY_CONSTRAINTS);
            }
        }*/

        return elementIndex;
    }

    //private help functions

}
