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

import org.apache.xerces.impl.v2.datatypes.*;
import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.XInt;
import org.apache.xerces.util.XIntPool;
import org.w3c.dom.Element;
import java.util.Hashtable;


/**
 * The attribute declaration schema component traverser.
 *
 * <attribute
 *   default = string
 *   fixed = string
 *   form = (qualified | unqualified)
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   type = QName
 *   use = (optional | prohibited | required) : optional
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleType?))
 * </attribute>
 * 
 * @author Sandy Gao, IBM
 * 
 * @version $Id$
 */
class  XSDAttributeTraverser extends XSDAbstractTraverser {

    protected static final QName ANY_SIMPLE_TYPE = new QName(null,
                                                             SchemaSymbols.ATTVAL_ANYSIMPLETYPE,
                                                             SchemaSymbols.ATTVAL_ANYTYPE,
                                                             SchemaSymbols.URI_SCHEMAFORSCHEMA);

    protected XSAttributeDecl fTempAttributeDecl = new XSAttributeDecl();
    protected XSAttributeUse  fTempAttributeUse  = new XSAttributeUse();

    public XSDAttributeTraverser (XSDHandler handler,
                                  XMLErrorReporter errorReporter,
                                  XSAttributeChecker gAttrCheck) {
        super(handler, errorReporter, gAttrCheck);
    }

    protected int traverseLocal(Element attrDecl,
                                XSDocumentInfo schemaDoc,
                                SchemaGrammar grammar) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(attrDecl, false, schemaDoc.fNamespaceSupport);

        String defaultAtt = (String) attrValues[XSAttributeChecker.ATTIDX_DEFAULT];
        String fixedAtt   = (String) attrValues[XSAttributeChecker.ATTIDX_FIXED];
        String nameAtt    = (String) attrValues[XSAttributeChecker.ATTIDX_NAME];
        QName  refAtt     = (QName)  attrValues[XSAttributeChecker.ATTIDX_REF];
        XInt   useAtt     = (XInt)   attrValues[XSAttributeChecker.ATTIDX_USE];

        // get 'attribute declaration'
        int attrIdx = SchemaGrammar.I_EMPTY_DECL;
        if (refAtt != null) {
            attrIdx = fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.ATTRIBUTE_TYPE, refAtt);
            if (attrIdx == SchemaGrammar.I_NOT_FOUND) {
                reportGenericSchemaError("attribute not found: "+refAtt.uri+","+refAtt.localpart);
                attrIdx = SchemaGrammar.I_EMPTY_DECL;
            }

            Element child = DOMUtil.getFirstChildElement(attrDecl);
            if(child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                child = DOMUtil.getNextSiblingElement(child);
            }
    
            if (child != null) {
                reportGenericSchemaError("src-attribute.0: the content of an attribute information item with 'ref' must match (annotation?)");
            }
            
        } else {
            attrIdx = traverseNamedAttr(attrDecl, attrValues, schemaDoc, grammar, false);
            fTempAttributeDecl = fSchemaHandler.getAttributeDecl(schemaDoc.fTargetNamespace, attrIdx, fTempAttributeDecl);
        }

        // get 'value constraint'
        short consType = XSAttributeDecl.NO_CONSTRAINT;
        if (defaultAtt != null) {
            consType = XSAttributeDecl.DEFAULT_VALUE;
        } else if (fixedAtt != null) {
            consType = XSAttributeDecl.FIXED_VALUET;
            defaultAtt = fixedAtt;
            fixedAtt = null;
        }
        
        int attrUseIdx = SchemaGrammar.I_EMPTY_DECL;
        if (attrIdx != SchemaGrammar.I_EMPTY_DECL) {
            fTempAttributeUse.clear();
            fTempAttributeUse.fAttrIdx = attrIdx;
            fTempAttributeUse.fAttrName = refAtt == null ? fTempAttributeDecl.fName : refAtt.localpart;
            fTempAttributeUse.fAttrNS = refAtt == null ? schemaDoc.fTargetNamespace : refAtt.uri;
            fTempAttributeUse.fUse = useAtt.shortValue();
            fTempAttributeUse.fConstraintType = consType;
            fTempAttributeUse.fDefaultValue = defaultAtt;
            attrUseIdx = grammar.addAttributeUse(fTempAttributeUse);
        }
        
        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        //src-attribute
        
        // 1 default and fixed must not both be present. 
		if (defaultAtt != null && fixedAtt != null) {
			// REVISIT:  localize
			reportGenericSchemaError("src-attribute.1: 'default' and 'fixed' must not both be present in attribute declaration '" + nameAtt + "'");
        }

        // 2 If default and use are both present, use must have the ·actual value· optional. 
        if (consType == XSAttributeDecl.DEFAULT_VALUE &&
            useAtt != null && useAtt.intValue() != SchemaSymbols.USE_OPTIONAL) {
			reportGenericSchemaError("src-attribute.2: 'default' is present in attribute '"+nameAtt+"', so 'use' must be 'optional'");
        }
        
        // a-props-correct

        if (defaultAtt != null) {
            String namespace = refAtt == null ? schemaDoc.fTargetNamespace : refAtt.uri;
            XSAttributeDecl attr = fSchemaHandler.getAttributeDecl(namespace, attrIdx, fTempAttributeDecl);
            DatatypeValidator type = (DatatypeValidator)fSchemaHandler.getXSTypeDecl(attr.fTypeNS, attr.fTypeIdx);

            // 2 if there is a {value constraint}, the canonical lexical representation of its value must be ·valid· with respect to the {type definition} as defined in String Valid (§3.14.4). 
            if (!checkDefaultValid(defaultAtt, type, nameAtt)) {
                reportGenericSchemaError ("a-props-correct.2: invalid fixed or default value '" + defaultAtt + "' in attribute " + nameAtt);
            }

            // 3 If the {type definition} is or is derived from ID then there must not be a {value constraint}. 
            if (type instanceof IDDatatypeValidator) {
                reportGenericSchemaError ("a-props-correct.3: If the {type definition} or {type definition}'s {content type} is or is derived from ID then there must not be a {value constraint} -- attribute " + nameAtt);
            }
        }

        // create new attributeUse object
        // put in attribute decl, use, value constraint
        // register
        // return
        
        return SchemaGrammar.I_EMPTY_DECL;
    }

    protected int traverseGlobal(Element attrDecl,
                                 XSDocumentInfo schemaDoc,
                                 SchemaGrammar grammar) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(attrDecl, true, schemaDoc.fNamespaceSupport);
        int attrIdx = traverseNamedAttr(attrDecl, attrValues, schemaDoc, grammar, true);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return attrIdx;
    }

    /**
     * Traverse a globally declared attribute.
     *
     * @param  attrDecl
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @param  isGlobal
     * @return the attribute declaration index
     */
    int traverseNamedAttr(Element attrDecl,
                          Object[] attrValues,
                          XSDocumentInfo schemaDoc,
                          SchemaGrammar grammar,
                          boolean isGlobal) {

        String  defaultAtt   = (String)  attrValues[XSAttributeChecker.ATTIDX_DEFAULT];
        String  fixedAtt     = (String)  attrValues[XSAttributeChecker.ATTIDX_FIXED];
        XInt formAtt      = (XInt) attrValues[XSAttributeChecker.ATTIDX_FORM];
        String  nameAtt      = (String)  attrValues[XSAttributeChecker.ATTIDX_NAME];
        QName   typeAtt      = (QName)   attrValues[XSAttributeChecker.ATTIDX_TYPE];

        // Step 1: get declaration information
        
        // get 'target namespace'
        String namespace = XSDHandler.EMPTY_STRING;
        if (isGlobal) {
            namespace = schemaDoc.fTargetNamespace;
        }
        else if (formAtt != null) {
            if (formAtt.intValue() == SchemaSymbols.FORM_QUALIFIED)
                namespace = schemaDoc.fTargetNamespace;
        } else if (schemaDoc.fAreLocalElementsQualified) {
            namespace = schemaDoc.fTargetNamespace;
        }

        // get 'value constraint'
        // for local named attribute, value constraint is absent
        if (!isGlobal) {
            fixedAtt = defaultAtt = null;
        }
        short consType = XSAttributeDecl.NO_CONSTRAINT;
        if (fixedAtt != null) {
            consType = XSAttributeDecl.FIXED_VALUET;
            defaultAtt = fixedAtt;
        } else if (defaultAtt != null) {
            consType = XSAttributeDecl.DEFAULT_VALUE;
        }

        // get 'annotation'
        Element child = DOMUtil.getFirstChildElement(attrDecl);
        if(child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
			traverseAnnotationDecl(child, attrValues, false, schemaDoc);
            child = DOMUtil.getNextSiblingElement(child);
		}

        // get 'type definition'
        String typeNS = null;
        int attrType = SchemaGrammar.I_EMPTY_DECL;
        boolean haveAnonType = false;

        // Handle Anonymous type if there is one
        if (child != null) {
            String childName = DOMUtil.getLocalName(child);

            if (childName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                attrType = fSchemaHandler.fSimpleTypeTraverser.traverseLocal(child, schemaDoc, grammar);
                if (attrType != SchemaGrammar.I_EMPTY_DECL)
                    typeNS = schemaDoc.fTargetNamespace;
                haveAnonType = true;
            	child = DOMUtil.getNextSiblingElement(child);
            }
        }

        // Handler type attribute
        if (attrType == SchemaGrammar.I_EMPTY_DECL && typeAtt != null) {
            attrType = fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.TYPEDECL_TYPE, typeAtt);
            if (attrType != SchemaGrammar.I_NOT_FOUND) {
                reportGenericSchemaError("type not found: "+typeAtt.uri+","+typeAtt.localpart+" for element '"+nameAtt+"'");
                attrType = SchemaGrammar.I_EMPTY_DECL;
            } else {
                typeNS = typeAtt.uri;
            }
        }
        
        if (attrType == SchemaGrammar.I_EMPTY_DECL) {
            attrType = fSchemaHandler.getGlobalDecl(schemaDoc, fSchemaHandler.TYPEDECL_TYPE, ANY_SIMPLE_TYPE);
            typeNS = SchemaSymbols.URI_SCHEMAFORSCHEMA;
        }

        // Step 2: create the declaration, and register it to the grammar
        fTempAttributeDecl.clear();
        fTempAttributeDecl.fName = nameAtt;
        fTempAttributeDecl.fTargetNamespace = namespace;
        fTempAttributeDecl.fTypeNS = typeNS;
        fTempAttributeDecl.fTypeIdx = attrType;
        fTempAttributeDecl.fConstraintType = consType;
        fTempAttributeDecl.fDefaultValue = defaultAtt;
        // REVISIT: to implement
        //int attributeIndex = grammar.addAttributeDecl(fTempAttributeDecl);

        // Step 3: check against schema for schemas
        
        // required attributes
        if (nameAtt == null) {
            if (isGlobal)
                reportGenericSchemaError("src-attribute.0: 'name' must be present in a global attribute declaration");
            else
                reportGenericSchemaError("src-attribute.3.1: One of 'ref' or 'name' must be present in a local attribute declaration");
        }
        
        // element
        if (child != null) {
            reportGenericSchemaError("src-attribute.0: the content of an attribute information item must match (annotation?, (simpleType?))");
        }

        // Step 4: check 3.2.3 constraints
        
        // src-attribute

        // 1 default and fixed must not both be present. 
		if (defaultAtt != null && fixedAtt != null) {
			// REVISIT:  localize
			reportGenericSchemaError("src-attribute.1: 'default' and 'fixed' must not both be present in attribute declaration '" + nameAtt + "'");
        }

        // 2 If default and use are both present, use must have the ·actual value· optional.
        // This is checked in "traverse" method
        
        // 3 If the item's parent is not <schema>, then all of the following must be true:
        // 3.1 One of ref or name must be present, but not both. 
        // This is checked in XSAttributeChecker

        // 3.2 If ref is present, then all of <simpleType>, form and type must be absent. 
        // Attributes are checked in XSAttributeChecker, elements are checked in "traverse" method

        // 4 type and <simpleType> must not both be present. 
        if (haveAnonType && (typeAtt != null)) {
            reportGenericSchemaError( "src-attribute.3: Attribute '"+ nameAtt +
                                      "' have both a type attribute and a simpleType child" );
        }

        // Step 5: check 3.2.6 constraints
        DatatypeValidator type = (DatatypeValidator)fSchemaHandler.getXSTypeDecl(typeNS, attrType);
        // check for NOTATION type        
        checkNotationType(nameAtt, type);

        // a-props-correct

        // 2 if there is a {value constraint}, the canonical lexical representation of its value must be ·valid· with respect to the {type definition} as defined in String Valid (§3.14.4). 
        if (defaultAtt != null) {
            if (!checkDefaultValid(defaultAtt, type, nameAtt)) {
                reportGenericSchemaError ("a-props-correct.2: invalid fixed or default value '" + defaultAtt + "' in attribute " + nameAtt);
            }
        }

        // 3 If the {type definition} is or is derived from ID then there must not be a {value constraint}. 
        if (defaultAtt != null) {
            if (type instanceof IDDatatypeValidator) {
                reportGenericSchemaError ("a-props-correct.3: If the {type definition} or {type definition}'s {content type} is or is derived from ID then there must not be a {value constraint} -- attribute " + nameAtt);
            }
        }
        
        // no-xmlns

        // The {name} of an attribute declaration must not match xmlns. 
        if (nameAtt.equals(SchemaSymbols.XMLNS)) {
            reportGenericSchemaError("no-xmlns: The {name} of an attribute declaration must not match 'xmlns'");
        }

        // no-xsi

        // The {target namespace} of an attribute declaration, whether local or top-level, must not match http://www.w3.org/2001/XMLSchema-instance (unless it is one of the four built-in declarations given in the next section). 
        if (namespace.equals(SchemaSymbols.URI_XSI)) {
            reportGenericSchemaError("no-xsi: The {target namespace} of an attribute declaration must not match " + SchemaSymbols.URI_XSI);
        }

        return 0;
    }

    // return whether the constraint value is valid for the given type
    boolean checkDefaultValid(String defaultStr, DatatypeValidator dv, String referName) {

        boolean ret = true;

        try {
            dv.validate(defaultStr, null);
        } catch (InvalidDatatypeValueException ide) {
            ret = false;
        }
        
        return ret;
    }

}
