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

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.impl.v2.datatypes.NOTATIONDatatypeValidator;
import org.apache.xerces.impl.v2.datatypes.UnionDatatypeValidator;
import org.apache.xerces.impl.v2.datatypes.ListDatatypeValidator;
import org.apache.xerces.impl.v2.datatypes.DatatypeValidator;
import org.apache.xerces.impl.v2.datatypes.StringDatatypeValidator;

import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import java.util.Stack;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.lang.Integer;

/**
 * The simple type definition schema component traverser.
 *
 * <simpleType
 *   final = (#all | (list | union | restriction))
 *   id = ID
 *   name = NCName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (restriction | list | union))
 * </simpleType>
 *
 * <restriction
 *   base = QName
 *   id = ID
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleType?, (minExclusive | minInclusive | maxExclusive | maxInclusive | totalDigits | fractionDigits | length | minLength | maxLength | enumeration | whiteSpace | pattern)*))
 * </restriction>
 *
 * <list
 *   id = ID
 *   itemType = QName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleType?))
 * </list>
 *
 * <union
 *   id = ID
 *   memberTypes = List of QName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleType*))
 * </union>
 * @version $Id$
 */
class XSDSimpleTypeTraverser extends XSDAbstractTraverser {


    //private data
    private Hashtable fAttributes = null;
    private Hashtable fFacetData = new Hashtable(10);
    private Stack fSimpleTypeNameStack = new Stack();
    private String fListName = "";

    private XSDocumentInfo fSchemaDoc = null;
    private SchemaGrammar fGrammar = null;
    private StringBuffer fPattern = null;
    private int fSimpleTypeAnonCount = 0;

    XSDSimpleTypeTraverser (XSDHandler handler,
                            XMLErrorReporter errorReporter,
                            XSAttributeChecker gAttrCheck) {
        super(handler, errorReporter, gAttrCheck);
    }

    //return qualified name of simpleType or empty string if error occured
    String traverseGlobal(Element elmNode,
                          XSDocumentInfo schemaDoc,
                          SchemaGrammar grammar) {
        // General Attribute Checking
        fSchemaDoc = schemaDoc;
        fGrammar = grammar;
        fAttributes = fAttrChecker.checkAttributes(elmNode,true); 
        return traverseSimpleTypeDecl (elmNode);

    }

    String traverse(Element elmNode,
                    XSDocumentInfo schemaDoc,
                    SchemaGrammar grammar) {
        fSchemaDoc = schemaDoc;
        fGrammar = grammar;
        fAttributes = fAttrChecker.checkAttributes(elmNode,false);
        return traverseSimpleTypeDecl (elmNode);
    }

    private String traverseSimpleTypeDecl(Element simpleTypeDecl) {

        String nameProperty  =  DOMUtil.getAttrValue(simpleTypeDecl, SchemaSymbols.ATT_NAME);
        String qualifiedName = nameProperty;


        //---------------------------------------------------
        // set qualified name
        //---------------------------------------------------
        if (nameProperty.length() == 0) { // anonymous simpleType
            qualifiedName =  fSchemaDoc.fTargetNamespace+","+"#S#"+(fSimpleTypeAnonCount++);
            //REVISIT:
            // add to symbol table?
        }
        else {
            // this behaviour has been changed so that we neither
            // process unqualified names as if they came from the schemaforschema namespace nor
            // fail to pick up unqualified names from schemas with no
            // targetnamespace.  - NG
            //if (fTargetNSURIString.length () != 0) {
            qualifiedName = fSchemaDoc.fTargetNamespace+","+qualifiedName;
            //}
            //REVISIT:
            // add to symbol table?

        }

        //----------------------------------------------------------------------
        //check if we have already traversed the same simpleType decl
        //----------------------------------------------------------------------
        if (fSchemaHandler.getSimpleTypeValidator(qualifiedName)!=null) {
            return resetSimpleTypeNameStack(qualifiedName);
        }
        else {
            if (fSimpleTypeNameStack.search(qualifiedName) != -1) {
                // cos-no-circular-unions && no circular definitions
                reportGenericSchemaError("cos-no-circular-unions: no circular definitions are allowed for an element '"+ nameProperty+"'");
                return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
            }
        }


        //----------------------------------------------------------
        // REVISIT!
        // update _final_ registry
        //----------------------------------------------------------
        Attr finalAttr = DOMUtil.getAttr(simpleTypeDecl, SchemaSymbols.ATT_FINAL); 
        int finalProperty = 0;

        if (finalAttr != null)
            finalProperty = parseFinalSet(DOMUtil.getValue( finalAttr));
        else
            finalProperty = parseFinalSet(null);

        // if we have a nonzero final , store it in the hash...
        if (finalProperty != 0)
            fSchemaHandler.fSimpleTypeFinalRegistry.put(qualifiedName, new Integer(finalProperty));


        // -------------------------------
        // remember name being traversed to
        // avoid circular definitions in union
        // -------------------------------
        fSimpleTypeNameStack.push(qualifiedName);


        //----------------------------------------------------------------------
        //annotation?,(list|restriction|union)
        //----------------------------------------------------------------------
        Element content = DOMUtil.getFirstChildElement(simpleTypeDecl);
        content = checkContent(simpleTypeDecl, content, false);
        if (content == null) {
            return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
        }

        // General Attribute Checking
        fAttributes = fAttrChecker.checkAttributes(content, false);

        //----------------------------------------------------------------------
        //use content.getLocalName for the cases there "xsd:" is a prefix, ei. "xsd:list"
        //----------------------------------------------------------------------
        String varietyProperty =  DOMUtil.getLocalName(content);  //content.getLocalName();
        String baseTypeQNameProperty = null;
        Vector dTValidators = null;
        int size = 0;
        StringTokenizer unionMembers = null;
        boolean list = false;
        boolean union = false;
        boolean restriction = false;
        int numOfTypes = 0; //list/restriction = 1, union = "+"

        if (varietyProperty.equals(SchemaSymbols.ELT_LIST)) { //traverse List
            baseTypeQNameProperty =  DOMUtil.getAttrValue(content,  SchemaSymbols.ATT_ITEMTYPE);//content.getAttribute( SchemaSymbols.ATT_ITEMTYPE );
            list = true;
            if (fListName.length() != 0) { // parent is <list> datatype
                reportCosListOfAtomic();
                return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
            }
            else {
                fListName = qualifiedName;
            }
        }
        else if (varietyProperty.equals(SchemaSymbols.ELT_RESTRICTION)) { //traverse Restriction
            baseTypeQNameProperty =  DOMUtil.getAttrValue(content, SchemaSymbols.ATT_BASE); 
            //content.getAttribute( SchemaSymbols.ATT_BASE );
            restriction= true;
        }
        else if (varietyProperty.equals(SchemaSymbols.ELT_UNION)) { //traverse union
            union = true;
            baseTypeQNameProperty = DOMUtil.getAttrValue(content, SchemaSymbols.ATT_MEMBERTYPES);
            //content.getAttribute( SchemaSymbols.ATT_MEMBERTYPES);
            if (baseTypeQNameProperty.length() != 0) {
                unionMembers = new StringTokenizer( baseTypeQNameProperty );
                size = unionMembers.countTokens();
            }
            else {
                size = 1; //at least one must be seen as <simpleType> decl
            }
            dTValidators = new Vector (size, 2);
        }
        else {
            //REVISIT: port SchemaMessageProvider
            //reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
            //          new Object [] { varietyProperty });
            //          return fSchemaHandler.EMPTY_STRING;
        }
        if (DOMUtil.getNextSiblingElement(content) != null) {
            // REVISIT: Localize
            reportGenericSchemaError("error in content of simpleType");
        }

        DatatypeValidator baseValidator = null;
        String simpleTypeName = null;
        if (baseTypeQNameProperty.length() == 0) {
            //---------------------------
            //must 'see' <simpleType>
            //---------------------------

            //content = {annotation?,simpleType?...}
            content = DOMUtil.getFirstChildElement(content);

            //check content (annotation?, ...)
            content = checkContent(simpleTypeDecl, content, false);
            if (content == null) {
                return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
            }
            if (DOMUtil.getLocalName(content).equals( SchemaSymbols.ELT_SIMPLETYPE )) {
                simpleTypeName = traverse(content, fSchemaDoc, fGrammar);
                if (!simpleTypeName.equals(fSchemaHandler.EMPTY_STRING)) {
                    baseValidator=fSchemaHandler.getSimpleTypeValidator(simpleTypeName); 
                    if (baseValidator !=null && union) {
                        dTValidators.addElement((DatatypeValidator)baseValidator);
                    }
                }
                if (baseValidator == null) {
                    //REVISIT:
                    // reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
                    //                      new Object [] { content.getAttribute( SchemaSymbols.ATT_BASE ),
                    //                          content.getAttribute(SchemaSymbols.ATT_NAME) });
                    return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
                }
            }
            else {
                //REVISIT
                //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                //       new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
            }
        } //end - must see simpleType?
        else {
            //-----------------------------
            //base was provided - get proper validator.
            //-----------------------------
            numOfTypes = 1;
            if (union) {
                numOfTypes= size;
            }
            //--------------------------------------------------------------------
            // this loop is also where we need to find out whether the type being used as
            // a base (or itemType or whatever) allows such things.
            //--------------------------------------------------------------------
            int baseRefContext = (restriction? SchemaSymbols.RESTRICTION:0);
            baseRefContext = baseRefContext | (union? SchemaSymbols.UNION:0);
            baseRefContext = baseRefContext | (list ? SchemaSymbols.LIST:0);
            for (int i=0; i<numOfTypes; i++) {  //find all validators
                if (union) {
                    baseTypeQNameProperty = unionMembers.nextToken();
                }
                baseValidator = findDTValidator ( simpleTypeDecl, baseTypeQNameProperty, baseRefContext);
                if (baseValidator == null) {
                    return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
                }
                // ------------------------------
                // (variety is list)cos-list-of-atomic
                // ------------------------------
                if (fListName.length() != 0) {
                    if (baseValidator instanceof ListDatatypeValidator) {
                        reportCosListOfAtomic();
                        return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
                    }
                    //-----------------------------------------------------
                    // if baseValidator is of type (union) need to look
                    // at Union validators to make sure that List is not one of them
                    //-----------------------------------------------------
                    if (isListDatatype(baseValidator)) {
                        reportCosListOfAtomic();
                        return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);

                    }

                }
                if (union) {
                    dTValidators.addElement((DatatypeValidator)baseValidator); //add validator to structure
                }
            }
        } //end - base is available


        // ------------------------------------------
        // move to next child
        // <base==empty)->[simpleType]->[facets]  OR
        // <base!=empty)->[facets]
        // ------------------------------------------
        if (baseTypeQNameProperty.length() == 0) {
            content = DOMUtil.getNextSiblingElement( content );
        }
        else {
            content = DOMUtil.getFirstChildElement(content);
        }

        // ------------------------------------------
        //get more types for union if any
        // ------------------------------------------
        if (union) {
            int index=size;
            if (baseTypeQNameProperty.length() != 0) {
                content = checkContent(simpleTypeDecl, content, true);
            }
            while (content!=null) {
                simpleTypeName = traverse(content, fSchemaDoc, fGrammar);
                baseValidator = null;
                if (!simpleTypeName.equals(fSchemaHandler.EMPTY_STRING)) {
                    baseValidator= fSchemaHandler.getSimpleTypeValidator(simpleTypeName);
                    if (baseValidator != null) {
                        if (fListName.length() != 0 && baseValidator instanceof ListDatatypeValidator) {
                            reportCosListOfAtomic();
                            return resetSimpleTypeNameStack(fSchemaHandler.EMPTY_STRING);
                        }
                        dTValidators.addElement((DatatypeValidator)baseValidator);
                    }
                }
                if (baseValidator == null) {
                    //REVISIT: 
                    //reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
                    //                  new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_BASE ),
                    //                      simpleTypeDecl.getAttribute(SchemaSymbols.ATT_NAME)});
                    return fSchemaHandler.EMPTY_STRING;
                }
                content   = DOMUtil.getNextSiblingElement( content );
            }
        } // end - traverse Union


        if (fListName.length() != 0) {
            // reset fListName, meaning that we are done with
            // traversing <list> and its itemType resolves to atomic value
            if (fListName.equals(qualifiedName)) {
                fListName = "";
            }
        }

        int numFacets=0;
        fFacetData.clear();
        if (restriction && content != null) {
            short flags = 0; // flag facets that have fixed="true"
            int numEnumerationLiterals = 0;
            Vector enumData  = new Vector();
            content = checkContent(simpleTypeDecl, content , true);
            String facet;
            while (content != null) {
                if (content.getNodeType() == Node.ELEMENT_NODE) {
                    // General Attribute Checking
                    fAttributes = fAttrChecker.checkAttributes(content, false);
                    numFacets++;
                    facet = DOMUtil.getLocalName(content);
                    if (facet.equals(SchemaSymbols.ELT_ENUMERATION)) {
                        numEnumerationLiterals++;
                        String enumVal =  DOMUtil.getAttrValue(content, SchemaSymbols.ATT_VALUE);
                        String localName;
                        if (baseValidator instanceof NOTATIONDatatypeValidator) {
                            String prefix = "";
                            String localpart = enumVal;
                            int colonptr = enumVal.indexOf(":");
                            if (colonptr > 0) {
                                prefix = enumVal.substring(0,colonptr);
                                localpart = enumVal.substring(colonptr+1);
                            }
                            String uriStr = (prefix.length() != 0)?fSchemaHandler.resolvePrefixToURI(prefix):fSchemaDoc.fTargetNamespace;
                            nameProperty=uriStr + ":" + localpart;
                            localName = (String)fSchemaHandler.fNotationRegistry.get(nameProperty);
                            if (localName == null) {

                                //REVISIT: when implementing notation!
                                //localName = traverseNotationFromAnotherSchema( localpart, uriStr);
                                if (localName == null) {
                                    reportGenericSchemaError("Notation '" + localpart +
                                                             "' not found in the grammar "+ uriStr);

                                }
                            }
                            enumVal=nameProperty;
                        }
                        enumData.addElement(enumVal);
                        checkContent(simpleTypeDecl, DOMUtil.getFirstChildElement( content ), true);
                    }
                    else if (facet.equals(SchemaSymbols.ELT_ANNOTATION) || facet.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                        //REVISIT:      
                        //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                        //new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                    }
                    else if (facet.equals(SchemaSymbols.ELT_PATTERN)) {
                        if (fPattern == null) {
                            //REVISIT: size of buffer
                            fPattern = new StringBuffer (DOMUtil.getAttrValue( content, SchemaSymbols.ATT_VALUE ));
                        }
                        else {
                            // ---------------------------------------------
                            //datatypes: 5.2.4 pattern: src-multiple-pattern
                            // ---------------------------------------------
                            fPattern.append("|");
                            fPattern.append(DOMUtil.getAttrValue(content, SchemaSymbols.ATT_VALUE ));
                            checkContent(simpleTypeDecl, DOMUtil.getFirstChildElement( content ), true);
                        }
                    }
                    else {
                        if (fFacetData.containsKey(facet))
                            //REVISIT:
                            //reportSchemaError(SchemaMessageProvider.DatatypeError,
                            //                  new Object [] {"The facet '" + facet + "' is defined more than once."} );
                            fFacetData.put(facet,content.getAttribute( SchemaSymbols.ATT_VALUE ));

                        if (content.getAttribute( SchemaSymbols.ATT_FIXED).equals(SchemaSymbols.ATTVAL_TRUE) ||
                            content.getAttribute( SchemaSymbols.ATT_FIXED).equals(SchemaSymbols.ATTVAL_TRUE_1)) {
                            // --------------------------------------------
                            // set fixed facet flags
                            // length - must remain const through derivation
                            // thus we don't care if it fixed
                            // --------------------------------------------
                            if (facet.equals(SchemaSymbols.ELT_MINLENGTH)) {
                                flags |= DatatypeValidator.FACET_MINLENGTH;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_MAXLENGTH)) {
                                flags |= DatatypeValidator.FACET_MAXLENGTH;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                                flags |= DatatypeValidator.FACET_MAXEXCLUSIVE;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                                flags |= DatatypeValidator.FACET_MAXINCLUSIVE;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                                flags |= DatatypeValidator.FACET_MINEXCLUSIVE;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                                flags |= DatatypeValidator.FACET_MININCLUSIVE;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_TOTALDIGITS)) {
                                flags |= DatatypeValidator.FACET_TOTALDIGITS;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_FRACTIONDIGITS)) {
                                flags |= DatatypeValidator.FACET_FRACTIONDIGITS;
                            }
                            else if (facet.equals(SchemaSymbols.ELT_WHITESPACE) &&
                                     baseValidator instanceof StringDatatypeValidator) {
                                flags |= DatatypeValidator.FACET_WHITESPACE;
                            }
                        }
                        checkContent(simpleTypeDecl, DOMUtil.getFirstChildElement( content ), true);
                    }
                }
                content = DOMUtil.getNextSiblingElement(content);
            }
            if (numEnumerationLiterals > 0) {
                fFacetData.put(SchemaSymbols.ELT_ENUMERATION, enumData);
            }
            if (fPattern !=null) {
                fFacetData.put(SchemaSymbols.ELT_PATTERN, fPattern.toString());
            }
            if (flags != 0) {
                fFacetData.put(DatatypeValidator.FACET_FIXED, new Short(flags));
            }
            fPattern.setLength(0);
        }


        else if (list && content!=null) {
            // report error - must not have any children!
            if (baseTypeQNameProperty.length() != 0) {
                content = checkContent(simpleTypeDecl, content, true);
                if (content!=null) {
                    //REVISIT:
                    //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                    //                  new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                }
            }
            else {
                //REVISIT
                //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                //                  new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                //REVISIT: should we return?
            }
        }
        else if (union && content!=null) {
            //report error - must not have any children!
            if (baseTypeQNameProperty.length() != 0) {
                content = checkContent(simpleTypeDecl, content, true);
                if (content!=null) {
                    //REVISIT
                    //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                    //                  new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                }
            }
            else {
                //REVISIT
                //reportSchemaError(SchemaMessageProvider.ListUnionRestrictionError,
                //                  new Object [] { simpleTypeDecl.getAttribute( SchemaSymbols.ATT_NAME )});
                //REVISIT: should we return?
            }
        }

        // ----------------------------------------------------------------------
        // create & register validator for "generated" type if it doesn't exist
        // ----------------------------------------------------------------------
        DatatypeValidator newValidator =
        fSchemaHandler.getSimpleTypeValidator( qualifiedName );

        if (newValidator == null) { // not previously registered
            if (list) {
                fSchemaHandler.createSimpleType(qualifiedName, baseValidator,
                                                fFacetData,true);
            }
            else if (restriction) {
                fSchemaHandler.createSimpleType( qualifiedName, baseValidator,
                                                 fFacetData,false);
            }
            else { //union
                fSchemaHandler.createUnionSimpleType( qualifiedName, dTValidators);
            }

        }



        return resetSimpleTypeNameStack(qualifiedName);


    }


    private void reportCosListOfAtomic () {
        reportGenericSchemaError("cos-list-of-atomic: The itemType must have a {variety} of atomic or union (in which case all the {member type definitions} must be atomic)");
        fListName="";
    }
    // @used in traverseSimpleType
    // on return we need to pop the last simpleType name from
    // the name stack
    private String resetSimpleTypeNameStack(String returnValue) {
        if (!fSimpleTypeNameStack.empty()) {
            fSimpleTypeNameStack.pop();
        }
        return returnValue;
    }

    //@param: elm - top element
    //@param: baseTypeStr - type (base/itemType/memberTypes)
    //@param: baseRefContext:  whether the caller is using this type as a base for restriction, union or list
    //return DatatypeValidator available for the baseTypeStr, null if not found or disallowed.
    // also throws an error if the base type won't allow itself to be used in this context.
    // REVISIT: can this code be re-used?
    private DatatypeValidator findDTValidator (Element elm, String baseTypeStr, int baseRefContext ) {
        String prefix = "";
        DatatypeValidator baseValidator = null;
        String localpart = baseTypeStr;
        int colonptr = baseTypeStr.indexOf(":");
        if (colonptr > 0) {
            prefix = baseTypeStr.substring(0,colonptr);
            localpart = baseTypeStr.substring(colonptr+1);
        }
        String uri = fSchemaHandler.resolvePrefixToURI(prefix);
        if (uri.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) &&
            localpart.equals("anySimpleType") &&
            baseRefContext == SchemaSymbols.RESTRICTION) {
            //REVISIT
            //reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
            //                  new Object [] { DOMUtil.getAttrValue(elm, SchemaSymbols.ATT_BASE),
            //                      DOMUtil.getAttrValue(elm, SchemaSymbols.ATT_NAME)});
            return null;
        }
        baseValidator = fSchemaHandler.getDatatypeValidator(uri, localpart);
        if (baseValidator == null) {
            Element baseTypeNode = fSchemaHandler.getTopLevelComponentByName(SchemaSymbols.ELT_SIMPLETYPE, localpart);
            if (baseTypeNode != null) {
                traverseGlobal(baseTypeNode, fSchemaDoc, fGrammar);
                baseValidator = fSchemaHandler.getDatatypeValidator(uri, localpart);
            }
        }
        Integer finalValue;
        if (baseValidator == null) {
            //REVISIT
            //reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
            //                  new Object [] { DOMUtil.getAttrValue(elm, SchemaSymbols.ATT_BASE ),
            //                      DOMUtil.getAttrValue(elm,SchemaSymbols.ATT_NAME)});
        }
        else {
            finalValue =
            ((Integer)fSchemaHandler.fSimpleTypeFinalRegistry.get(uri + "," +localpart));
            if ((finalValue != null) &&
                ((finalValue.intValue() & baseRefContext) != 0)) {
                //REVISIT:  localize
                reportGenericSchemaError("the base type " + baseTypeStr + " does not allow itself to be used as the base for a restriction and/or as a type in a list and/or union");
                return baseValidator;
            }
        }
        return baseValidator;
    }

    // find if union datatype validator has list datatype member.
    private boolean isListDatatype (DatatypeValidator validator) {
        if (validator instanceof UnionDatatypeValidator) {
            Vector temp = ((UnionDatatypeValidator)validator).getBaseValidators();
            for (int i=0;i<temp.size();i++) {
                if (temp.elementAt(i) instanceof ListDatatypeValidator) {
                    return true;
                }
                if (temp.elementAt(i) instanceof UnionDatatypeValidator) {
                    if (isListDatatype((DatatypeValidator)temp.elementAt(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
