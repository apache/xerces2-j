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
import org.apache.xerces.impl.v2.identity.IdentityConstraint;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SymbolHash;

import java.util.Hashtable;

/**
 * This class is to hold all schema component declaration that are declared
 * within one namespace.
 *
 * @author Sandy Gao, IBM
 * @author Elena Litani, IBM
 *
 * @version $Id$
 */

public class SchemaGrammar {

    /** Symbol table. */
    private SymbolTable fSymbolTable;

    // the target namespace of grammar
    private String fTargetNamespace;

    // global decls: map from decl name to decl object
    SymbolHash fGlobalAttrDecls;
    SymbolHash fGlobalAttrGrpDecls;
    SymbolHash fGlobalElemDecls;
    SymbolHash fGlobalGroupDecls;
    SymbolHash fGlobalNotationDecls;
    SymbolHash fGlobalTypeDecls;
    SymbolHash fGlobalIDConstraintDecls;

    //
    // Constructors
    //

    /**
     * Default constructor.
     *
     * @param symbolTable
     * @param targetNamespace
     */
    public SchemaGrammar(SymbolTable symbolTable, String targetNamespace) {
        fSymbolTable = symbolTable;
        fTargetNamespace = targetNamespace;

        // REVISIT: do we know the numbers of the following global decls
        // when creating this grammar? If so, we can pass the numbers in,
        // and use that number to initialize the following hashtables.
        fGlobalAttrDecls  = new SymbolHash();
        fGlobalAttrGrpDecls = new SymbolHash();
        fGlobalElemDecls = new SymbolHash();
        fGlobalGroupDecls = new SymbolHash();
        fGlobalNotationDecls = new SymbolHash();
        fGlobalTypeDecls = new SymbolHash();
        fGlobalIDConstraintDecls = new SymbolHash();
    } // <init>(SymbolTable, String)

    // number of built-in XSTypes we need to create for base and full
    // datatype set
    private static final int BASICSET_COUNT = 29;
    private static final int FULLSET_COUNT  = 46;

    /**
     * register one global type
     * REVISIT: remove when we use new DV design
     */
    private final void addGlobalTypeDecl(String name, XSTypeDecl decl) {
        fGlobalTypeDecls.put(name, decl);
    }

    /**
     * Special constructor to create the grammar for the schema namespace
     *
     * @param symbolTable
     * @param fullSet
     */
    private SchemaGrammar(SymbolTable symbolTable, boolean fullSet) {
        fSymbolTable = symbolTable;
        fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;

        // set the size of type SymbolHash to double the number of types need
        // to be created. which should be the most effecient number.
        fGlobalTypeDecls = new SymbolHash((fullSet?FULLSET_COUNT:BASICSET_COUNT)*2);

        try {
            // REVISIT: use XSSimpleTypeDecl instead
            XSComplexTypeDecl anyType = new XSComplexTypeDecl();
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYTYPE, anyType);
            //REVISIT: make anyType the base of anySimpleType
            //DatatypeValidator anySimpleType = new AnySimpleType(anyType, null, false);
            DatatypeValidator anySimpleType = new AnySimpleType(null, null, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYSIMPLETYPE, anySimpleType);
            DatatypeValidator stringDV = new StringDatatypeValidator(anySimpleType, null, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_STRING, stringDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_BOOLEAN, new BooleanDatatypeValidator(anySimpleType, null, false));
            DatatypeValidator decimalDV = new DecimalDatatypeValidator(anySimpleType, null, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DECIMAL, decimalDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYURI, new AnyURIDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_BASE64BINARY, new Base64BinaryDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DURATION, new DurationDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DATETIME, new DateTimeDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_TIME, new TimeDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DATE, new DateDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_YEARMONTH, new YearMonthDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_YEAR, new YearDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_MONTHDAY, new MonthDayDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DAY, new DayDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_MONTH, new MonthDatatypeValidator(anySimpleType, null, false));

            Hashtable facets = new Hashtable(2);
            facets.put(SchemaSymbols.ELT_FRACTIONDIGITS, "0");
            DatatypeValidator integerDV = new DecimalDatatypeValidator(decimalDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_INTEGER, integerDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "0" );
            DatatypeValidator nonPositiveDV = new DecimalDatatypeValidator(integerDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, nonPositiveDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "-1" );
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NEGATIVEINTEGER, new DecimalDatatypeValidator(nonPositiveDV, facets, false));
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
            DatatypeValidator longDV = new DecimalDatatypeValidator(integerDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_LONG, longDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
            DatatypeValidator intDV = new DecimalDatatypeValidator(longDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_INT, intDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
            DatatypeValidator shortDV = new DecimalDatatypeValidator(intDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_SHORT, shortDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_BYTE, new DecimalDatatypeValidator(shortDV, facets, false));
            facets.clear();
            facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
            DatatypeValidator nonNegativeDV = new DecimalDatatypeValidator(integerDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, nonNegativeDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
            DatatypeValidator unsignedLongDV = new DecimalDatatypeValidator(nonNegativeDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDLONG, unsignedLongDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
            DatatypeValidator unsignedIntDV = new DecimalDatatypeValidator(unsignedLongDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDINT, unsignedIntDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
            DatatypeValidator unsignedShortDV = new DecimalDatatypeValidator(unsignedIntDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDSHORT, unsignedShortDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDBYTE, new DecimalDatatypeValidator(unsignedShortDV, facets, false));
            facets.clear();
            facets.put(SchemaSymbols.ELT_MININCLUSIVE, "1" );
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_POSITIVEINTEGER, new DecimalDatatypeValidator(nonNegativeDV, facets, false));

            if (!fullSet)
                return;

            addGlobalTypeDecl(SchemaSymbols.ATTVAL_FLOAT, new FloatDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_DOUBLE, new DoubleDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_HEXBINARY, new HexBinaryDatatypeValidator(anySimpleType, null, false));
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NOTATION, new NOTATIONDatatypeValidator(anySimpleType, null, false));

            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_REPLACE);
            DatatypeValidator normalizedDV = new StringDatatypeValidator(stringDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NORMALIZEDSTRING, normalizedDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            DatatypeValidator tokenDV = new StringDatatypeValidator(normalizedDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_TOKEN, tokenDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            //REVISIT: won't run: regexparser, locale, resource bundle
            //facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*");
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_LANGUAGE, new StringDatatypeValidator(tokenDV, facets, false));
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NAME);
            DatatypeValidator nameDV = new StringDatatypeValidator(tokenDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NAME, nameDV);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NCNAME);
            DatatypeValidator ncnameDV = new StringDatatypeValidator(nameDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NCNAME, ncnameDV);
            DatatypeValidator qnameDV = new QNameDatatypeValidator(anySimpleType, null, false);
            ((QNameDatatypeValidator)qnameDV).setNCNameValidator(ncnameDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME, qnameDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ID, new IDDatatypeValidator(ncnameDV, null, false));
            DatatypeValidator idrefDV = new IDREFDatatypeValidator(ncnameDV, null, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_IDREF, idrefDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_IDREFS, new ListDatatypeValidator(idrefDV, null, true));
            //REVISIT: entity validators
            //DatatypeValidator entityDV = new ENTITYDatatypeValidator(ncnameDV, null, false);
            DatatypeValidator entityDV = new StringDatatypeValidator(ncnameDV, null, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ENTITY, entityDV);
            //REVISIT: entity validators
            //fTypeDeclType[0][typeIndex] = new ListDatatypeValidator(entityDV, null, true);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_ENTITIES, new ListDatatypeValidator(entityDV, null, true));
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NMTOKEN);
            DatatypeValidator nmtokenDV = new StringDatatypeValidator(tokenDV, facets, false);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NMTOKEN, nmtokenDV);
            addGlobalTypeDecl(SchemaSymbols.ATTVAL_NMTOKENS, new ListDatatypeValidator(nmtokenDV, null, true));
        } catch (InvalidDatatypeFacetException idf) {
            // should never reach here
            // REVISIT: report internal error
        }
    } // <init>(SymbolTable, boolean)

    /**
     * Returns this grammar's target namespace.
     */
    public final String getTargetNamespace() {
        return fTargetNamespace;
    } // getTargetNamespace():String

    /**
     * register one global attribute
     */
    public final void addGlobalAttributeDecl(XSAttributeDecl decl) {
        fGlobalAttrDecls.put(decl.fName, decl);
    }

    /**
     * register one global attribute group
     */
    public final void addGlobalAttributeGroupDecl(XSAttributeGroupDecl decl) {
        fGlobalAttrGrpDecls.put(decl.fName, decl);
    }

    /**
     * register one global element
     */
    public final void addGlobalElementDecl(XSElementDecl decl) {
        fGlobalElemDecls.put(decl.fName, decl);
    }

    /**
     * register one global group
     */
    public final void addGlobalGroupDecl(XSGroupDecl decl) {
        fGlobalGroupDecls.put(decl.fName, decl);
    }

    /**
     * register one global notation
     */
    public final void addGlobalNotationDecl(XSNotationDecl decl) {
        fGlobalNotationDecls.put(decl.fName, decl);
    }

    /**
     * register one global type
     */
    public final void addGlobalTypeDecl(XSTypeDecl decl) {
        fGlobalTypeDecls.put(decl.getXSTypeName(), decl);
    }

    /**
     * register one identity constraint
     */
    public final void addIDConstraintDecl(XSElementDecl elmDecl, IdentityConstraint decl) {
        elmDecl.addIDConstaint(decl);
        fGlobalIDConstraintDecls.put(decl.getIdentityConstraintName(), decl);
    }

    /**
     * get one global attribute
     */
    public final XSAttributeDecl getGlobalAttributeDecl(String declName) {
        return (XSAttributeDecl)fGlobalAttrDecls.get(declName);
    }

    /**
     * get one global attribute group
     */
    public final XSAttributeGroupDecl getGlobalAttributeGroupDecl(String declName) {
        return (XSAttributeGroupDecl)fGlobalAttrGrpDecls.get(declName);
    }

    /**
     * get one global element
     */
    public final XSElementDecl getGlobalElementDecl(String declName) {
        return (XSElementDecl)fGlobalElemDecls.get(declName);
    }

    /**
     * get one global group
     */
    public final XSGroupDecl getGlobalGroupDecl(String declName) {
        return (XSGroupDecl)fGlobalGroupDecls.get(declName);
    }

    /**
     * get one global notation
     */
    public final XSNotationDecl getNotationDecl(String declName) {
        return (XSNotationDecl)fGlobalNotationDecls.get(declName);
    }

    /**
     * get one global type
     */
    public final XSTypeDecl getGlobalTypeDecl(String declName) {
        return (XSTypeDecl)fGlobalTypeDecls.get(declName);
    }

    /**
     * get one identity constraint
     */
    public final IdentityConstraint getIDConstraintDecl(String declName) {
        return (IdentityConstraint)fGlobalIDConstraintDecls.get(declName);
    }

    /**
     * add one complex type decl: for later constraint checking
     */
    final void addComplexTypeDecl(XSComplexTypeDecl decl) {
        //REVISIT: implement: array, resize, store
    }

    /**
     * get all complex type decls: for later constraint checking
     */
    final XSComplexTypeDecl[] getAllComplexTypeDecls() {
        //REVISIT: return the internal array
        //         ? how to return the size of the array
        //         we can use the same approach as attribute group:
        //         having a finish method, which resize the array
        //         so that there is no empty entry in the array
        return null;
    }

    // the grammars to hold built-in types
    final static SchemaGrammar SG_SchemaNS = new SchemaGrammar(null, true);
    final static SchemaGrammar SG_SchemaBasicSet = new SchemaGrammar(null, false);

} // class SchemaGrammar
