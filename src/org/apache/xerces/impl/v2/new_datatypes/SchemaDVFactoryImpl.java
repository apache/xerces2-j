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

import java.util.Hashtable;

/**
 * the factory to create/return built-in schema DVs and create user-defined DVs
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class SchemaDVFactoryImpl implements SchemaDVFactory {

    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";

    static Hashtable fBuiltInTypes = null;
    static XSSimpleTypeDecl fAnySimpleType = null;

    // get a built-in DV of the given name
    // we need to pass in anyType as the base of anySimpleType
    public XSSimpleType getBuiltInType(String name) {
        prepareBuiltInTypes();
        return (XSSimpleType)fBuiltInTypes.get(name);
    }

    // create a user-defined DV according to how it's defined: <restriction>
    public XSSimpleType createTypeRestriction(String name, String targetNamespace,
                                              short finalSet, XSSimpleType base) {
        return new XSSimpleTypeDecl((XSSimpleTypeDecl)base, name, targetNamespace, finalSet);
    }

    // create a user-defined DV according to how it's defined: <list>
    public XSListSimpleType createTypeList(String name, String targetNamespace,
                                           short finalSet, XSSimpleType itemType) {
        prepareBuiltInTypes();
        return new XSSimpleTypeDecl(fAnySimpleType, name, targetNamespace, finalSet, (XSSimpleTypeDecl)itemType);
    }

    // create a user-defined DV according to how it's defined: <union>
    public XSUnionSimpleType createTypeUnion(String name, String targetNamespace,
                                             short finalSet, XSSimpleType[] memberTypes) {
        prepareBuiltInTypes();
        int typeNum = memberTypes.length;
        XSSimpleTypeDecl[] mtypes = new XSSimpleTypeDecl[typeNum];
        System.arraycopy(memberTypes, 0, mtypes, 0, typeNum);
        return new XSSimpleTypeDecl(fAnySimpleType, name, targetNamespace, finalSet, mtypes);
    }

    void prepareBuiltInTypes() {
        if (fBuiltInTypes == null) {
            synchronized (this.getClass()) {
                if (fBuiltInTypes == null) {
                    createBuiltInTypes();
                }
            }
        }
    }

    // create all built-in types
    // we are assumeing that fBuiltInTypes == null
    void createBuiltInTypes() {

        // all schema simple type names
        final String ANYSIMPLETYPE     = "anySimpleType";
        final String ANYURI            = "anyURI";
        final String BASE64BINARY      = "base64Binary";
        final String BOOLEAN           = "boolean";
        final String BYTE              = "byte";
        final String DATE              = "date";
        final String DATETIME          = "dateTime";
        final String DAY               = "gDay";
        final String DECIMAL           = "decimal";
        final String DOUBLE            = "double";
        final String DURATION          = "duration";
        final String ENTITY            = "ENTITY";
        final String ENTITIES          = "ENTITIES";
        final String FLOAT             = "float";
        final String HEXBINARY         = "hexBinary";
        final String ID                = "ID";
        final String IDREF             = "IDREF";
        final String IDREFS            = "IDREFS";
        final String INT               = "int";
        final String INTEGER           = "integer";
        final String LONG              = "long";
        final String NAME              = "Name";
        final String NEGATIVEINTEGER   = "negativeInteger";
        final String MONTH             = "gMonth";
        final String MONTHDAY          = "gMonthDay";
        final String NCNAME            = "NCName";
        final String NMTOKEN           = "NMTOKEN";
        final String NMTOKENS          = "NMTOKENS";
        final String NONNEGATIVEINTEGER= "nonNegativeInteger";
        final String NONPOSITIVEINTEGER= "nonPositiveInteger";
        final String NORMALIZEDSTRING  = "normalizedString";
        final String NOTATION          = "NOTATION";
        final String POSITIVEINTEGER   = "positiveInteger";
        final String QNAME             = "QName";
        final String SHORT             = "short";
        final String STRING            = "string";
        final String TIME              = "time";
        final String TOKEN             = "token";
        final String UNSIGNEDBYTE      = "unsignedByte";
        final String UNSIGNEDINT       = "unsignedInt";
        final String UNSIGNEDLONG      = "unsignedLong";
        final String UNSIGNEDSHORT     = "unsignedShort";
        final String YEAR              = "gYear";
        final String YEARMONTH         = "gYearMonth";

        final XSFacets facets = new XSFacets();

        fBuiltInTypes = new Hashtable();

        fAnySimpleType = new XSSimpleTypeDecl(null, ANYSIMPLETYPE, XSSimpleTypeDecl.DV_ANYSIMPLETYPE);
        fBuiltInTypes.put(ANYSIMPLETYPE, fAnySimpleType);
        XSSimpleTypeDecl stringDV = new XSSimpleTypeDecl(fAnySimpleType, STRING, XSSimpleTypeDecl.DV_STRING);
        fBuiltInTypes.put(STRING, stringDV);
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, STRING, XSSimpleTypeDecl.DV_BOOLEAN));
        XSSimpleTypeDecl decimalDV = new XSSimpleTypeDecl(fAnySimpleType, DECIMAL, XSSimpleTypeDecl.DV_DECIMAL);
        fBuiltInTypes.put(DECIMAL, decimalDV);
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, BOOLEAN, XSSimpleTypeDecl.DV_BOOLEAN));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, ANYURI, XSSimpleTypeDecl.DV_ANYURI));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, BASE64BINARY, XSSimpleTypeDecl.DV_BASE64BINARY));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, DURATION, XSSimpleTypeDecl.DV_DURATION));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, DATETIME, XSSimpleTypeDecl.DV_DATETIME));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, TIME, XSSimpleTypeDecl.DV_TIME));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, DATE, XSSimpleTypeDecl.DV_DATE));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, YEARMONTH, XSSimpleTypeDecl.DV_GYEARMONTH));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, YEAR, XSSimpleTypeDecl.DV_GYEAR));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, MONTHDAY, XSSimpleTypeDecl.DV_GMONTHDAY));
        fBuiltInTypes.put(BOOLEAN, new XSSimpleTypeDecl(fAnySimpleType, MONTH, XSSimpleTypeDecl.DV_GMONTH));

        facets.fractionDigits = 0;
        XSSimpleTypeDecl integerDV = new XSSimpleTypeDecl(decimalDV, INTEGER, URI_SCHEMAFORSCHEMA, (short)0);
        integerDV.applyFacets(facets, XSSimpleType.FACET_FRACTIONDIGITS, (short)0);
        fBuiltInTypes.put(INTEGER, integerDV);
        facets.maxInclusive = "0";
        XSSimpleTypeDecl nonPositiveDV = new XSSimpleTypeDecl(integerDV, NONPOSITIVEINTEGER, URI_SCHEMAFORSCHEMA, (short)0);
        nonPositiveDV.applyFacets(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0);
        fBuiltInTypes.put(NONPOSITIVEINTEGER, nonPositiveDV);
        facets.maxInclusive = "-1";
        XSSimpleTypeDecl negativeDV = new XSSimpleTypeDecl(integerDV, NEGATIVEINTEGER, URI_SCHEMAFORSCHEMA, (short)0);
        negativeDV.applyFacets(facets, XSSimpleType.FACET_MAXINCLUSIVE, (short)0);
        fBuiltInTypes.put(NEGATIVEINTEGER, negativeDV);
        /*facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
        XSSimpleTypeDecl longDV = new DecimalXSSimpleTypeDecl(integerDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.LONG, longDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
        XSSimpleTypeDecl intDV = new DecimalXSSimpleTypeDecl(longDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.INT, intDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
        XSSimpleTypeDecl shortDV = new DecimalXSSimpleTypeDecl(intDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.SHORT, shortDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
        addGlobalTypeDecl(SchemaSymbols.BYTE, new DecimalXSSimpleTypeDecl(shortDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
        XSSimpleTypeDecl nonNegativeDV = new DecimalXSSimpleTypeDecl(integerDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.NONNEGATIVEINTEGER, nonNegativeDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
        XSSimpleTypeDecl unsignedLongDV = new DecimalXSSimpleTypeDecl(nonNegativeDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.UNSIGNEDLONG, unsignedLongDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
        XSSimpleTypeDecl unsignedIntDV = new DecimalXSSimpleTypeDecl(unsignedLongDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.UNSIGNEDINT, unsignedIntDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
        XSSimpleTypeDecl unsignedShortDV = new DecimalXSSimpleTypeDecl(unsignedIntDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.UNSIGNEDSHORT, unsignedShortDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
        addGlobalTypeDecl(SchemaSymbols.UNSIGNEDBYTE, new DecimalXSSimpleTypeDecl(unsignedShortDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_MININCLUSIVE, "1" );
        addGlobalTypeDecl(SchemaSymbols.POSITIVEINTEGER, new DecimalXSSimpleTypeDecl(nonNegativeDV, facets, false, null));

        if (!fullSet)
            return;

        addGlobalTypeDecl(SchemaSymbols.FLOAT, new FloatXSSimpleTypeDecl(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.DOUBLE, new DoubleXSSimpleTypeDecl(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.HEXBINARY, new HexBinaryXSSimpleTypeDecl(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.NOTATION, new NOTATIONXSSimpleTypeDecl(fAnySimpleType,  null, false, null));

        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.REPLACE);
        XSSimpleTypeDecl normalizedDV = new StringXSSimpleTypeDecl(stringDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.NORMALIZEDSTRING, normalizedDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.COLLAPSE);
        XSSimpleTypeDecl tokenDV = new StringXSSimpleTypeDecl(normalizedDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.TOKEN, tokenDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.COLLAPSE);
        facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*");
        addGlobalTypeDecl(SchemaSymbols.LANGUAGE, new StringXSSimpleTypeDecl(tokenDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NAME);
        XSSimpleTypeDecl nameDV = new StringXSSimpleTypeDecl(tokenDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.NAME, nameDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NCNAME);
        XSSimpleTypeDecl ncnameDV = new StringXSSimpleTypeDecl(nameDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.NCNAME, ncnameDV);
        XSSimpleTypeDecl qnameDV = new QNameXSSimpleTypeDecl(fAnySimpleType,  null, false, null);
        ((QNameXSSimpleTypeDecl)qnameDV).setNCNameValidator(ncnameDV);
        addGlobalTypeDecl(SchemaSymbols.QNAME, qnameDV);
        addGlobalTypeDecl(SchemaSymbols.ID, new IDXSSimpleTypeDecl(ncnameDV,  null, false, null));
        XSSimpleTypeDecl idrefDV = new IDREFXSSimpleTypeDecl(ncnameDV,  null, false, null);
        addGlobalTypeDecl(SchemaSymbols.IDREF, idrefDV);
        addGlobalTypeDecl(SchemaSymbols.IDREFS, new ListXSSimpleTypeDecl(idrefDV, null, true, null));
        XSSimpleTypeDecl entityDV = new EntityXSSimpleTypeDecl(ncnameDV, null, false, null);
        addGlobalTypeDecl(SchemaSymbols.ENTITY, entityDV);
        addGlobalTypeDecl(SchemaSymbols.ENTITIES, new ListXSSimpleTypeDecl(entityDV, null, true, null));

        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NMTOKEN);
        XSSimpleTypeDecl nmtokenDV = new StringXSSimpleTypeDecl(tokenDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.NMTOKEN, nmtokenDV);
        addGlobalTypeDecl(SchemaSymbols.NMTOKENS, new ListXSSimpleTypeDecl(nmtokenDV, null, true, null));
*/
    }//getBuiltInSimpleType()

}//SchemaDVFactory
