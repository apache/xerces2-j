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

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.dv.xs.*;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
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
    public String fTargetNamespace;

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
    protected SchemaGrammar(SymbolTable symbolTable, boolean fullSet) {
        fSymbolTable = symbolTable;
        fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;

        // REVISIT: for default datatype creation an error reporter is null.
        // In case we ever see an error, Runtime exception will be thrown
        // since this is an implementation failure.

        fGlobalAttrDecls  = new SymbolHash(1);
        fGlobalAttrGrpDecls = new SymbolHash(1);
        fGlobalElemDecls = new SymbolHash(1);
        fGlobalGroupDecls = new SymbolHash(1);
        fGlobalNotationDecls = new SymbolHash(1);
        fGlobalIDConstraintDecls = new SymbolHash(1);

        // set the size of type SymbolHash to double the number of types need
        // to be created. which should be the most effecient number.
        fGlobalTypeDecls = new SymbolHash((fullSet?FULLSET_COUNT:BASICSET_COUNT)*2);

        // REVISIT: use the newly designed interfaces

        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYTYPE, fAnyType);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYSIMPLETYPE, fAnySimpleType);
        DatatypeValidator stringDV = new StringDatatypeValidator(fAnySimpleType, null, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_STRING, stringDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_BOOLEAN, new BooleanDatatypeValidator(fAnySimpleType, null, false, null));
        DatatypeValidator decimalDV = new DecimalDatatypeValidator(fAnySimpleType, null, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DECIMAL, decimalDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYURI, new AnyURIDatatypeValidator(fAnySimpleType, null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_BASE64BINARY, new Base64BinaryDatatypeValidator(fAnySimpleType, null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DURATION, new DurationDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DATETIME, new DateTimeDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_TIME, new TimeDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DATE, new DateDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_YEARMONTH, new YearMonthDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_YEAR, new YearDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_MONTHDAY, new MonthDayDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DAY, new DayDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_MONTH, new MonthDatatypeValidator(fAnySimpleType,  null, false, null));

        Hashtable facets = new Hashtable(2);
        facets.put(SchemaSymbols.ELT_FRACTIONDIGITS, "0");
        DatatypeValidator integerDV = new DecimalDatatypeValidator(decimalDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_INTEGER, integerDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "0" );
        DatatypeValidator nonPositiveDV = new DecimalDatatypeValidator(integerDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, nonPositiveDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "-1" );
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NEGATIVEINTEGER, new DecimalDatatypeValidator(nonPositiveDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
        DatatypeValidator longDV = new DecimalDatatypeValidator(integerDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_LONG, longDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
        DatatypeValidator intDV = new DecimalDatatypeValidator(longDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_INT, intDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
        DatatypeValidator shortDV = new DecimalDatatypeValidator(intDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_SHORT, shortDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
        facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_BYTE, new DecimalDatatypeValidator(shortDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
        DatatypeValidator nonNegativeDV = new DecimalDatatypeValidator(integerDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, nonNegativeDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
        DatatypeValidator unsignedLongDV = new DecimalDatatypeValidator(nonNegativeDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDLONG, unsignedLongDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
        DatatypeValidator unsignedIntDV = new DecimalDatatypeValidator(unsignedLongDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDINT, unsignedIntDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
        DatatypeValidator unsignedShortDV = new DecimalDatatypeValidator(unsignedIntDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDSHORT, unsignedShortDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_UNSIGNEDBYTE, new DecimalDatatypeValidator(unsignedShortDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_MININCLUSIVE, "1" );
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_POSITIVEINTEGER, new DecimalDatatypeValidator(nonNegativeDV, facets, false, null));

        if (!fullSet)
            return;

        addGlobalTypeDecl(SchemaSymbols.ATTVAL_FLOAT, new FloatDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_DOUBLE, new DoubleDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_HEXBINARY, new HexBinaryDatatypeValidator(fAnySimpleType,  null, false, null));
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NOTATION, new NOTATIONDatatypeValidator(fAnySimpleType,  null, false, null));

        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_REPLACE);
        DatatypeValidator normalizedDV = new StringDatatypeValidator(stringDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NORMALIZEDSTRING, normalizedDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
        DatatypeValidator tokenDV = new StringDatatypeValidator(normalizedDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_TOKEN, tokenDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
        facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*");
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_LANGUAGE, new StringDatatypeValidator(tokenDV, facets, false, null));
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NAME);
        DatatypeValidator nameDV = new StringDatatypeValidator(tokenDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NAME, nameDV);
        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NCNAME);
        DatatypeValidator ncnameDV = new StringDatatypeValidator(nameDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NCNAME, ncnameDV);
        DatatypeValidator qnameDV = new QNameDatatypeValidator(fAnySimpleType,  null, false, null);
        ((QNameDatatypeValidator)qnameDV).setNCNameValidator(ncnameDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME, qnameDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ID, new IDDatatypeValidator(ncnameDV,  null, false, null));
        DatatypeValidator idrefDV = new IDREFDatatypeValidator(ncnameDV,  null, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_IDREF, idrefDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_IDREFS, new ListDatatypeValidator(idrefDV, null, true, null));
        DatatypeValidator entityDV = new EntityDatatypeValidator(ncnameDV, null, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ENTITY, entityDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_ENTITIES, new ListDatatypeValidator(entityDV, null, true, null));

        facets.clear();
        facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
        facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NMTOKEN);
        DatatypeValidator nmtokenDV = new StringDatatypeValidator(tokenDV, facets, false, null);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NMTOKEN, nmtokenDV);
        addGlobalTypeDecl(SchemaSymbols.ATTVAL_NMTOKENS, new ListDatatypeValidator(nmtokenDV, null, true, null));

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
        return(XSAttributeDecl)fGlobalAttrDecls.get(declName);
    }

    /**
     * get one global attribute group
     */
    public final XSAttributeGroupDecl getGlobalAttributeGroupDecl(String declName) {
        return(XSAttributeGroupDecl)fGlobalAttrGrpDecls.get(declName);
    }

    /**
     * get one global element
     */
    public final XSElementDecl getGlobalElementDecl(String declName) {
        return(XSElementDecl)fGlobalElemDecls.get(declName);
    }

    /**
     * get one global group
     */
    public final XSGroupDecl getGlobalGroupDecl(String declName) {
        return(XSGroupDecl)fGlobalGroupDecls.get(declName);
    }

    /**
     * get one global notation
     */
    public final XSNotationDecl getNotationDecl(String declName) {
        return(XSNotationDecl)fGlobalNotationDecls.get(declName);
    }

    /**
     * get one global type
     */
    public final XSTypeDecl getGlobalTypeDecl(String declName) {
        return(XSTypeDecl)fGlobalTypeDecls.get(declName);
    }

    /**
     * get one identity constraint
     */
    public final IdentityConstraint getIDConstraintDecl(String declName) {
        return(IdentityConstraint)fGlobalIDConstraintDecls.get(declName);
    }

    // array to store complex type decls
    private static final int INITIAL_SIZE = 16;
    private int fCTCount = 0;
    private XSComplexTypeDecl[] fComplexTypeDecls = new XSComplexTypeDecl[INITIAL_SIZE];

    /**
     * add one complex type decl: for later constraint checking
     */
    final void addComplexTypeDecl(XSComplexTypeDecl decl) {
        if (fCTCount == fComplexTypeDecls.length)
            fComplexTypeDecls = resize(fComplexTypeDecls, fCTCount*2);
        fComplexTypeDecls[fCTCount++] = decl;
    }

    /**
     * get all complex type decls: for later constraint checking
     */
    final XSComplexTypeDecl[] getAllComplexTypeDecls() {
        if (fCTCount < fComplexTypeDecls.length)
            fComplexTypeDecls = resize(fComplexTypeDecls, fCTCount);
        return fComplexTypeDecls;
    }

    // anyType and anySimpleType: because there are so many places where
    // we need direct access to these two types
    public final static XSComplexTypeDecl fAnyType = new XSComplexTypeDecl();
    public final static DatatypeValidator fAnySimpleType = new AnySimpleType();
    static {
        fAnyType.fName = SchemaSymbols.ATTVAL_ANYTYPE;
        fAnyType.fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;
        fAnyType.fBaseType = fAnyType;
        fAnyType.fDerivedBy = SchemaSymbols.RESTRICTION;
        fAnyType.fContentType = XSComplexTypeDecl.CONTENTTYPE_MIXED;
        XSWildcardDecl wildcard = new XSWildcardDecl();
        XSParticleDecl particle = new XSParticleDecl();
        particle.fMinOccurs = 0;
        particle.fMaxOccurs = SchemaSymbols.OCCURRENCE_UNBOUNDED;
        particle.fType = XSParticleDecl.PARTICLE_WILDCARD;
        particle.fValue = wildcard;
        fAnyType.fParticle = particle;
        fAnyType.fAttrGrp.fAttributeWC = wildcard;

        AnySimpleType astype = (AnySimpleType)fAnySimpleType;
        astype.fLocalName = SchemaSymbols.ATTVAL_ANYSIMPLETYPE;
        // REVISIT: set target namespace
        // REVISIT: set fAnyType as the base of fAnySimpleType
    }

    // the grammars to hold built-in types
    public final static SchemaGrammar SG_SchemaNS = new SchemaGrammar(null, true);
    public final static SchemaGrammar SG_SchemaBasicSet = new SchemaGrammar(null, false);

    static final XSComplexTypeDecl[] resize(XSComplexTypeDecl[] oldArray, int newSize) {
        XSComplexTypeDecl[] newArray = new XSComplexTypeDecl[newSize];
        System.arraycopy(oldArray, 0, newArray, 0, Math.min(oldArray.length, newSize));
        return newArray;
    }

} // class SchemaGrammar
