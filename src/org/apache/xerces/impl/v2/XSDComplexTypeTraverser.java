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
import org.apache.xerces.xni.QName;
import  org.w3c.dom.Element;
import java.util.Hashtable;
import java.util.Stack;


/**
 * A complex type definition schema component traverser.
 *
 * <complexType
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction))
 *   final = (#all | List of (extension | restriction))
 *   id = ID
 *   mixed = boolean : false
 *   name = NCName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleContent | complexContent | ((group | all | choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?))))
 * </complexType>
 * @version $Id$
 */
class  XSDComplexTypeTraverser extends XSDAbstractParticleTraverser {


    private Stack fCurrentGroupNameStack;



    XSDComplexTypeTraverser (XSDHandler handler,
                             XMLErrorReporter errorReporter,
                             XSAttributeChecker gAttrCheck) {
        super(handler, errorReporter, gAttrCheck);
    }

    int traverseLocal(Element elmNode,
                      XSDocumentInfo schemaDoc,
                      SchemaGrammar grammar) {

        return SchemaGrammar.I_EMPTY_DECL;
    }

    int traverseGlobal (Element elmNode,
                        XSDocumentInfo schemaDoc,
                        SchemaGrammar grammar){

        return SchemaGrammar.I_EMPTY_DECL;
    }


    private int traverseComplexTypeDecl( Element complexTypeDecl, boolean forwardRef) {

        return SchemaGrammar.I_EMPTY_DECL;
    }

    private void processComplexContent(int typeNameIndex,
                                       Element complexContentChild,
                                       XSComplexTypeDecl typeInfo,
                                       QName baseName,
                                       boolean isMixed) throws Exception {

    }
    private void traverseSimpleContentDecl(int typeNameIndex,
                                           Element simpleContentDecl,
                                           XSComplexTypeDecl typeInfo) {
    }

    private void traverseComplexContentDecl(int typeNameIndex,
                                            Element complexContentDecl,
                                            XSComplexTypeDecl typeInfo,
                                            boolean mixedOnComplexTypeDecl){
    }
    // HELP FUNCTIONS:
    //
    // 1. processAttributes
    // 2. processBasetTypeInfo
    // 3. AWildCardIntersection
    // 4. parseBlockSet - should be here or in SchemaHandler??
    // 5. parseFinalSet - also used by traverseSimpleType, thus should be in SchemaHandler
    // 6. handleComplexTypeError
    // and more...

/*
    REVISIT: something done in AttriubteTraverser in Xerces1. Should be done
             here in complexTypeTraverser.
             
        // add attribute to attr decl pool in fSchemaGrammar,
        if (typeInfo != null) {

            // check that there aren't duplicate attributes
            int temp = fSchemaGrammar.getAttributeDeclIndex(typeInfo.templateElementIndex, attQName);
            if (temp > -1) {
              reportGenericSchemaError("ct-props-correct.4:  Duplicate attribute " +
              fStringPool.toString(attQName.rawname) + " in type definition");
            }

            // check that there aren't multiple attributes with type derived from ID
            if (dvIsDerivedFromID) {
               if (typeInfo.containsAttrTypeID())  {
                 reportGenericSchemaError("ct-props-correct.5: More than one attribute derived from type ID cannot appear in the same complex type definition.");
               }
               typeInfo.setContainsAttrTypeID();
            }
            fSchemaGrammar.addAttDeDecl(typeInfo.templateElementIndex,
                                        attQName, attType,
                                        dataTypeSymbol, attValueAndUseType,
                                        fStringPool.toString( attValueConstraint), dv, attIsList);
        }
*/
}
