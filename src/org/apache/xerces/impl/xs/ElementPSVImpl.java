/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000,2001 The Apache Software Foundation.
 * All rights reserved.
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

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSNotationDecl;
import org.apache.xerces.xni.psvi.ElementPSVI;



/**
 * Element PSV infoset augmentations implementation.
 * The following information will be available at the startElement call:
 * name, namespace, type, notation, validation context
 *
 * The following information will be available at the endElement call:
 * nil, specified, normalized value, member type, validity, error codes,
 * default(?)
 *
 * @author Elena Litani IBM
 */
public class ElementPSVImpl implements ElementPSVI {

    /** element declaration */
    protected XSElementDecl fDeclaration = null;

    /** type of element, could be xsi:type */
    protected XSTypeDecl fTypeDecl = null;

    /** true if clause 3.2 of Element Locally Valid (Element) (3.3.4) 
      * is satisfied, otherwise false 
      */
    protected boolean fNil = false;

    /** false if the element value was provided by the schema; true otherwise. 
     */
    protected boolean fSpecified = true;

    /** schema normalized value property */
    protected String fNormalizedValue = null;

    /** http://www.w3.org/TR/xmlschema-1/#e-notation*/
    protected XSNotationDecl fNotation = null;

    /** member type definition against which element was validated */
    protected XSSimpleType fMemberType = null;

    /** validation attempted: none, partial, full */
    protected short fValidationAttempted = ElementPSVI.NO_VALIDATION;

    /** validity: valid, invalid, unknown */
    protected short fValidity = ElementPSVI.UNKNOWN_VALIDITY;

    /** error codes */
    protected String[] fErrorCodes = null;

    /** validation context: could be QName or XPath expression*/
    protected String fValidationContext = null;


    //
    // ElementPSVI methods
    //

    /**
     * [member type definition anonymous]
     * @ see http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_anonymous
     * @return true if the {name} of the actual member type definition is absent,
     *         otherwise false.
     */
    public boolean  isMemberTypeAnonymous() {
        return (fMemberType !=null)? fMemberType.isAnonymous():false;
    }


    /**
     * [member type definition name]
     * @see http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_name
     * @return The {name} of the actual member type definition, if it is not absent.
     *         If it is absent, schema processors may, but need not, provide a
     *         value unique to the definition.
     */
    public String   getMemberTypeName() {
        return (fMemberType !=null)? fMemberType.getTypeName():null;
    }

    /**
     * [member type definition namespace]
     * @see http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_namespace
     * @return The {target namespace} of the actual member type definition.
     */
    public String   getMemberTypeNamespace() {
        return (fMemberType !=null)? fMemberType.getTargetNamespace():null;
    }

    /**
     * [schema default]
     *
     * @return The canonical lexical representation of the declaration's {value constraint} value.
     * @see http://www.w3.org/TR/xmlschema-1/#e-schema_default
     */
    public String   schemaDefault() {
        Object dValue = null;
        if( fDeclaration !=null ) {
            dValue = fDeclaration.fDefault;
        }
        return(dValue != null)?dValue.toString():null;
    }

    /**
     * [schema normalized value]
     *
     *
     * @see http://www.w3.org/TR/xmlschema-1/#e-schema_normalized_value
     * @return
     */
    public String schemaNormalizedValue() {
        return fNormalizedValue;
    }

    /**
     * [schema specified]
     * 
     * @return if return is true - schema, otherwise - infoset
     * @see http://www.w3.org/TR/xmlschema-1/#e-schema_specified
     */
    public boolean schemaSpecified() {
        return fSpecified;
    }


    /**
     * [type definition anonymous]
     * @see http://www.w3.org/TR/xmlschema-1/#e-type_definition_anonymous
     * @return true if the {name} of the type definition is absent, otherwise false.
     */
    public boolean isTypeAnonymous() {
        return (fTypeDecl !=null)? fTypeDecl.isAnonymous():false;
    }

    /**
     * [type definition name]
     * @see http://www.w3.org/TR/xmlschema-1/#e-type_definition_name
     * @return The {name} of the type definition, if it is not absent.
     *         If it is absent, schema processors may, but need not,
     *         provide a value unique to the definition.
     */
    public String getTypeName() {
        return (fTypeDecl !=null)? fTypeDecl.getTypeName():null;
    }

    /**
     * [type definition namespace]
     * @see http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_namespace
     * @return The {target namespace} of the type definition.
     */
    public String getTypeNamespace() {
        return (fTypeDecl !=null)? fTypeDecl.getTargetNamespace():null;
    }

    /**
     * [type definition type]
     *
     *  @see http://www.w3.org/TR/xmlschema-1/#a-type_definition_type
     *  @see http://www.w3.org/TR/xmlschema-1/#e-type_definition_type
     *  @return simple or complex, depending on the type definition.
     */
    public short getTypeDefinitionType() {
        return (fTypeDecl !=null)? fTypeDecl.getXSType():XSTypeDecl.COMPLEX_TYPE;
    }

    /**
     * Determines the extent to which the document has been validated
     *
     * @return return the [validation attempted] property. The possible values are
     *         NO_VALIDATION, PARTIAL_VALIDATION and FULL_VALIDATION
     */
    public short getValidationAttempted() {
        return fValidationAttempted;
    }

    /**
     * Determine the validity of the node with respect
     * to the validation being attempted
     *
     * @return return the [validity] property. Possible values are:
     *         UNKNOWN_VALIDITY, INVALID_VALIDITY, VALID_VALIDITY
     */
    public short getValidity() {
        return fValidity;
    }

    /**
     * A list of error codes generated from validation attempts.
     * Need to find all the possible subclause reports that need reporting
     *
     * @return Array of error codes
     */
    public String[] getErrorCodes() {
        return fErrorCodes;

    }


    // This is the only information we can provide in a pipeline.
    public String getValidationContext() {
        return fValidationContext;
    }

    /**
     * [nil]
     * @see http://www.w3.org/TR/xmlschema-1/#e-nil
     * @return true if clause 3.2 of Element Locally Valid (Element) (3.3.4) above is satisfied, otherwise false
     */
    public boolean isNil() {
        return fNil;
    }

    /**
     * [notation public]
     * @see http://www.w3.org/TR/xmlschema-1/#e-notation_public
     * @see http://www.w3.org/TR/xmlschema-1/#e-notation
     * @return The value of the {public identifier} of that notation declaration.
     */
    public String getNotationPublicId() {
        return (fNotation!=null)?fNotation.fPublicId:null;
    }

    /**
     * [notation system]
     *
     * @see http://www.w3.org/TR/xmlschema-1/#e-notation_system
     * @return The value of the {system identifier} of that notation declaration.
     */
    public String getNotationSystemId() {
        return (fNotation!=null)?fNotation.fSystemId:null;
    }

    /**
     * [schema namespace]
     * @see http://www.w3.org/TR/xmlschema-1/#nsi-schema_namespace
     * @see http://www.w3.org/TR/xmlschema-1/#e-schema_information
     * @return A namespace name or absent.
     */
    public String getSchemaNamespace() {
        // REVISIT: should we create component for schema-information item?
        return (fDeclaration !=null)? fDeclaration.fTargetNamespace:null;
    }


    /**
     * Reset() should be called in validator startElement(..) method.
     */
    public void reset() {
        fDeclaration = null;
        fTypeDecl = null;
        fNil = false;
        fSpecified = true;
        fNotation = null;
        fMemberType = null;
        fValidationAttempted = ElementPSVI.NO_VALIDATION;
        fValidity = ElementPSVI.UNKNOWN_VALIDITY;
        fErrorCodes = null;
        fValidationContext = null;
    }

}
