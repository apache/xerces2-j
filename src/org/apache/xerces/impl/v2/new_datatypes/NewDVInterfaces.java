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

package org.apache.xerces.impl.v2.new_datatypes;

/**
 * All the interfaces that need to be implemented by DV implementations,
 * and those interfaces/classes that DV implementations depend on.
 *
 * REVISIT: putting them all together in one file is temporary. after we clean
 *          up these interfaces, and the implementation, we'll make the
 *          interfaces public in separate files in the impl.dv package.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
*/

/**
 * Base datatype exception class
 */
class DatatypeException extends Exception {
    // this one is like reportGenericSchemaError: remove it later
    public DatatypeException(String msg) {
        this("general", new Object[]{msg});
    }

    // store a datatype error: error code plus the arguments
    String key;
    Object[] args;

    // report an error
    public DatatypeException(String key, Object[] args) {
        this.key = key;
        this.args = args;
    }

    public String getKey() {
        return key;
    }

    public Object[] getArgs() {
        return args;
    }
}

/**
 * Exception for facets
 */
class InvalidDatatypeFacetException extends DatatypeException {
    public InvalidDatatypeFacetException(String key, Object[] args) {
        super(key, args);
    }

    public InvalidDatatypeFacetException(String msg) {
        super(msg);
    }
}

/**
 * Exception for values
 */
class InvalidDatatypeValueException extends DatatypeException {
    public InvalidDatatypeValueException(String key, Object[] args) {
        super(key, args);
    }

    public InvalidDatatypeValueException(String msg) {
        super(msg);
    }
}

/**
 * the factory to create and return DTD types.
 */
interface DTDDVFactory {
    // return a dtd type of the given name
    public DatatypeValidator getBuiltInDV(String name);
}

/**
 * the interface that a DTD DV must implement
 */
interface DatatypeValidator {
    // validate a given string against this DV
    public Object validate(String content, ValidationContext context)
        throws InvalidDatatypeValueException;
}

/**
 * the factory to create/return built-in schema DVs and create user-defined DVs
 */
interface SchemaDVFactory {
    // get a built-in DV of the given name
    // we need to pass in anyType as the base of anySimpleType
    public XSSimpleType getBuiltInType(String name);

    // create a user-defined DV according to how it's defined:
    // <restriction> / <list> / <union>
    public XSSimpleType createTypeRestriction(String name, String targetNamespace,
                                              short finalSet, XSSimpleType base);
    public XSListSimpleType createTypeList(String name, String targetNamespace,
                                           short finalSet, XSSimpleType itemType);
    public XSUnionSimpleType createTypeUnion(String name, String targetNamespace,
                                             short finalSet, XSSimpleType[] memberTypes);
}

/**
 * represent a facet. similar to the one defined in PSVI API
 */
interface XSFacet {
    public String getName();
    public Object getValue();
    public boolean isFixed();
}

/**
 * The base interface for both complex type and simple type
 */
interface XSTypeDecl {
    public static final short COMPLEX_TYPE   = 1;
    public static final short SIMPLE_TYPE    = 2;

    public short getXSType();
    public String getXSTypeName();
    public String getXSTypeNamespace();
}

/**
 * the simple type interface, equivalent to XSSimpleTypeDefinition in PSVI API
 * it can't be derived from DatatypeValidator (for DTD), because we need to
 * derive it from the base type definition
 */
interface XSSimpleType extends XSTypeDecl, DatatypeValidator {

    // To tell which facets are fixed, and which ones are present, we use
    // bit mask of the following contants
    public static final short FACET_LENGTH         = 1<<0;
    public static final short FACET_MINLENGTH      = 1<<1;
    public static final short FACET_MAXLENGTH      = 1<<2;
    public static final short FACET_PATTERN        = 1<<3;
    public static final short FACET_ENUMERATION    = 1<<4;
    public static final short FACET_WHITESPACE     = 1<<5;
    public static final short FACET_MAXINCLUSIVE   = 1<<6;
    public static final short FACET_MAXEXCLUSIVE   = 1<<7;
    public static final short FACET_MINEXCLUSIVE   = 1<<8;
    public static final short FACET_MININCLUSIVE   = 1<<9;
    public static final short FACET_TOTALDIGITS    = 1<<10;
    public static final short FACET_FRACTIONDIGITS = 1<<11;

    // varieties
    public static final short VARIETY_ATOMIC    = 1;
    public static final short VARIETY_LIST      = 2;
    public static final short VARIETY_UNION     = 3;

    // whitespace values
    public static final short WS_PRESERVE = 0;
    public static final short WS_REPLACE  = 1;
    public static final short WS_COLLAPSE = 2;

    // order constants: for PSVI
    public static final short ORDERED_FALSE     = 1;
    public static final short ORDERED_PARTIAL   = 2;
    public static final short ORDERED_TOTAL     = 3;

    // cardinality constants: for PSVI
    public static final short CARDINALITY_FINITE             = 1;
    public static final short CARDINALITY_COUNTABLY_INFINITE = 2;

    // apply the facets
    public void applyFacets(XSFacets facets, short presentFacet, short fixedFacet)
        throws InvalidDatatypeFacetException;

    // to check whether two values are equal
    // we can't depend on Object#isEqual: for list types, we need to compare
    // the items one by one.
    public boolean isEqual(Object value1, Object value2)
        throws DatatypeException;

    // Andy also believes that a compare() method is necessary.
    // I don't see the necessity for schema (there only place where we need
    // to compare two values is to check min/maxIn/Exclusive facets, but we
    // only need a private method for this case.)
    // But Andy thinks XPATH potentially needs this compare() method.
    //public short compare(Object value1, Object value2);

    public short getFinalSet();
    public short getVariety();

    // the following methods are for PSVI. they are not implemented yet.
    //public XSFacet[] getFacets();
    // the following four are for fundamental facets
    //public short getOrderedFacet();
    //public boolean isBounded();
    //public boolean isNumeric();
    //public short getCardinalityFacet();
}

/**
 * atomic types
 */
interface XSAtomicSimpleType extends XSSimpleType {
    // return the built-in primitive type
    public XSSimpleType getPrimitiveType();
}

/**
 * list types
 */
interface XSListSimpleType extends XSSimpleType {
    // return the item type
    public XSSimpleType getItemType();
}

/**
 * union types
 */
interface XSUnionSimpleType extends XSSimpleType {
    // return the member types
    public XSSimpleType[] getMemberTypes();
}

/**
 * context for validatoin. it's useful for some types:
 *   ENTITY: check whether an entity is declared or unparsed
 *   ID    : check whether an ID value is declared
 *           declare a new ID value
 *   IDREF : declare a new IDREF value (for later checking against ID values)
 *   QName : resolve a prefix to a namespace uri
 *           get a string to the symbol table, to compare by reference
 */
interface ValidationContext {

    // entity
    public boolean isEntityDeclared (String name);
    public boolean isEntityUnparsed (String name);

    // id
    public boolean isIdDeclared (String name);
    public void    addId(String name);

    // idref
    public void addIdRef(String name);

    // get symbol from symbol table
    public String getSymbol (String symbol);

    // qname
    public String getURI(String prefix);
}

/**
 * The class for all facets to be passed to applyFacets()
 */
class XSFacets {
    int length;
    int minLength;
    int maxLength;
    short whiteSpace;
    int totalDigits;
    int fractionDigits;
    String patterh;
    String[] enumeration;
    String maxInclusive;
    String maxExclusive;
    String minInclusive;
    String minExclusive;
}
