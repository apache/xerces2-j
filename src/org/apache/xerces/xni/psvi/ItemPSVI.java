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

package org.apache.xerces.xni.psvi;

import org.apache.xerces.xni.QName;
/**
 *
 *
 * @author Elena Litani, IBM
 *
 */

public interface ItemPSVI {

    /** Validity value indicating that validation has either not 
    been performed or that a strict assessment of validity could 
    not be performed  
    */
    public static final short UNKNOWN_VALIDITY               = 0;

    /** Validity value indicating that validation has been strictly
     assessed and the element in question is invalid according to the 
     rules of schema validation.
    */
    public static final short INVALID_VALIDITY               = 1;

    /** Validity value indicating that validation has been strictly 
     assessed and the element in question is valid according to the rules 
     of schema validation.
     */
    public static final short VALID_VALIDITY                 = 2;

    /** Validation status indicating that schema validation has been 
     performed and the element in question has specifically been skipped.   
     */
    public static final short NO_VALIDATION                  = 1;

    /** Validation status indicating that schema validation has been 
    performed on the element in question under the rules of lax validation.
    */
    public static final short PARTIAL_VALIDATION             = 2;

    /**  Validation status indicating that full schema validation has been 
    performed on the element.  */
    public static final short FULL_VALIDATION                = 3;

    /**
     * [member type definition anonymous]
     * @ see <a href="http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_anonymous">XML Schema Part 1: Structures [member type definition anonymous]</a>
     * @return true if the {name} of the actual member type definition is absent, 
     *         otherwise false.
     */
    public boolean isMemberTypeAnonymous();

    /**
     * [member type definition name]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_name">XML Schema Part 1: Structures [member type definition name]</a>
     * @return The {name} of the actual member type definition, if it is not absent.
     *         If it is absent, schema processors may, but need not, provide a
     *         value unique to the definition.
     */
    public String getMemberTypeName();

    /**
     * [member type definition namespace]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_namespace">XML Schema Part 1: Structures [member type definition namespace]</a>
     * @return The {target namespace} of the actual member type definition.
     */
    public String getMemberTypeNamespace();

    /**
     * [schema default]
     * 
     * @return The canonical lexical representation of the declaration's {value constraint} value.
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_default">XML Schema Part 1: Structures [schema default]</a>
     */
    public String schemaDefault();

    /**
     * [schema normalized value] 
     * 
     * 
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_normalized_value">XML Schema Part 1: Structures [schema normalized value]</a>
     * @return the normalized value of this item after validation
     */
    public String schemaNormalizedValue();

    /**
     * [schema specified] 
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-schema_specified">XML Schema Part 1: Structures [schema specified]</a>
     * @return true if return is schema, false if infoset
     */
    public boolean schemaSpecified();


    /**
     * [type definition anonymous]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-type_definition_anonymous">XML Schema Part 1: Structures [type definition anonymous]</a>
     * @return true if the {name} of the type definition is absent, otherwise false.
     */
    public boolean isTypeAnonymous();

    /**
     * [type definition name]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-type_definition_name">XML Schema Part 1: Structures [type definition name]</a>
     * @return The {name} of the type definition, if it is not absent.
     *         If it is absent, schema processors may, but need not,
     *         provide a value unique to the definition.
     */
    public String getTypeName();

    /**
     * [type definition namespace]
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-member_type_definition_namespace">XML Schema Part 1: Structures [type definition namespace]</a>
     * @return The {target namespace} of the type definition.
     */
    public String getTypeNamespace();

    /**
     * [type definition type] 
     * 
     *  @see <a href="http://www.w3.org/TR/xmlschema-1/#a-type_definition_type">XML Schema Part 1: Structures [type definition type]</a>
     *  @see <a href="http://www.w3.org/TR/xmlschema-1/#e-type_definition_type">XML Schema Part 1: Structures [type definition type]</a>
     * @return simple or complex, depending on the type definition. 
     */
    public short getTypeDefinitionType();

    /**
     * Determines the extent to which the document has been validated
     * 
     * @return return the [validation attempted] property. The possible values are 
     *         NO_VALIDATION, PARTIAL_VALIDATION and FULL_VALIDATION
     */
    public short getValidationAttempted();

    /**
     * Determine the validity of the node with respect 
     * to the validation being attempted
     * 
     * @return return the [validity] property. Possible values are: 
     *         UNKNOWN_VALIDITY, INVALID_VALIDITY, VALID_VALIDITY
     */
    public short getValidity();

    /**
     * A list of error codes generated from validation attempts. 
     * Need to find all the possible subclause reports that need reporting
     * 
     * @return Array of error codes
     */
    public String[] getErrorCodes();


    /**
     * [validation context]
     * // REVISIT: what the return type should be?
     *             Should we return QName/XPath/ or element info item..?
     * 
     * @return The nearest ancestor element information item with a [schema information] property
     *         (or this element item itself if it has such a property)
     * @see <a href="http://www.w3.org/TR/xmlschema-1/#e-validation_context">XML Schema Part 1: Structures [validation context]</a>
     */
    public String getValidationContext();

}
