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
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.validation.ContentModelValidator;

import java.lang.Integer;
import java.util.Vector;
import java.util.Hashtable;
/**
 * @version $Id$
 */

public class SchemaGrammar {
    
    //
    // public fields
    //
    public boolean fDeferParticleExpantion = true;
    
    
    /** Chunk shift (8). */
    private static final int CHUNK_SHIFT = 8; // 2^8 = 256

    /** Chunk size (1 << CHUNK_SHIFT). */
    private static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;

    /** Chunk mask (CHUNK_SIZE - 1). */
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;

    /** Initial chunk count (). */
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT)); // 2^10 = 1k

    /** Symbol table. */
    private SymbolTable fSymbolTable;

    /** Target namespace of grammar. */
    private String fTargetNamespace;

    // decl count: element, attribute, notation, particle, type
    private int fElementDeclCount = 0;
    private int fAttributeDeclCount = 0 ;
    private int fNotationCount = 0;
    private int fParticleCount = 0;
    private int fXSTypeCount = 0;

    /** Element declaration contents. */
    private QName fElementDeclName[][];
    private String fElementDeclTypeNS[][];
    private int fElementDeclTypeDecl[][];
    private short fElementDeclMiscFlags[][];
    private short fElementDeclBlockSet[][];
    private short fElementDeclFinalSet[][];
    private String fElementDeclDefault[][];
    private String fElementDeclSubGroupNS[][];
    private int fElementDeclSubGroupIdx[][];
    private Vector fElementDeclUnique[][];
    private Vector fElementDeclKey[][];
    private Vector fElementDeclKeyRef[][];

    // attribute declarations
    private QName fAttributeDeclName[][];
    private String fAttributeDeclTypeNS[][];
    private int fAttributeDeclType[][];
    private short fAttributeDeclConstraintType[][];
    private String fAttributeDeclDefault[][];

    // particles
    private short fParticleType[][];
    private String fParticleUri[][];
    private int fParticleValue[][];
    private String fParticleOtherUri[][];
    private int fParticleOtherValue[][];
    private int fParticleMinOccurs[][];
    private int fParticleMaxOccurs[][];

    // notations
    private String fNotationName[][];
    private String fNotationPublicId[][];
    private String fNotationSystemId[][];

    //REVISIT: as temporary solution store complexTypes/simpleTypes as objects
    // Add XML Schema datatypes 
    private QName fTypeDeclQName[][];
    private XSType fTypeDeclType[][];

    // other information

    // global decls
    SymbolHash fGlobalGroupDecls;
    SymbolHash fGlobalNotationDecls;
    SymbolHash fGlobalAttrDecls;
    SymbolHash fGlobalAttrGrpDecls;
    SymbolHash fGlobalElemDecls;
    SymbolHash fGlobalTypeDecls;

    // REVISIT: do we need the option?
    // Set if we check Unique Particle Attribution
    // This one onle takes effect when deferContentSpecExpansion is set
    private boolean checkUniqueParticleAttribution = false;
    private boolean checkingUPA = false;

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaGrammar(SymbolTable symbolTable, String targetNamespace) {
        fSymbolTable = symbolTable;
        fTargetNamespace = targetNamespace;

        // element decl
        fElementDeclName = new QName[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclTypeNS = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclTypeDecl = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclMiscFlags = new short[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclBlockSet = new short[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclFinalSet = new short[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclDefault = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclSubGroupNS = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclSubGroupIdx = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclUnique = new Vector[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclKey = new Vector[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fElementDeclKeyRef = new Vector[INITIAL_CHUNK_COUNT][CHUNK_SIZE];

        // attribute declarations
        fAttributeDeclName = new QName[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fAttributeDeclTypeNS = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fAttributeDeclType = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fAttributeDeclConstraintType = new short[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fAttributeDeclDefault = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];

        // particles
        fParticleType = new short[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleUri = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleValue = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleOtherUri = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleOtherValue = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleMinOccurs = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fParticleMaxOccurs = new int[INITIAL_CHUNK_COUNT][CHUNK_SIZE];

        // notations
        fNotationName = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fNotationPublicId = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fNotationSystemId = new String[INITIAL_CHUNK_COUNT][CHUNK_SIZE];

        //REVISIT: as temporary solution store complexTypes/simpleTypes as objects
        // Add XML Schema datatypes 
        fTypeDeclQName = new QName[INITIAL_CHUNK_COUNT][CHUNK_SIZE];
        fTypeDeclType = new XSType[INITIAL_CHUNK_COUNT][CHUNK_SIZE];

        fGlobalGroupDecls = new SymbolHash();
        fGlobalNotationDecls = new SymbolHash();
        fGlobalAttrDecls  = new SymbolHash();
        fGlobalAttrGrpDecls = new SymbolHash();
        fGlobalElemDecls = new SymbolHash();
        fGlobalTypeDecls = new SymbolHash();
    } // <init>(SymbolTable)

    private static final int BASICSET_COUNT = 29;
    private static final int FULLSET_COUNT  = 46;

    /** Constructor for schema for schemas. */
    private SchemaGrammar(SymbolTable symbolTable, boolean fullSet) {
        fSymbolTable = symbolTable;
        fTargetNamespace = SchemaSymbols.URI_SCHEMAFORSCHEMA;

        fXSTypeCount = fullSet?FULLSET_COUNT:BASICSET_COUNT;
        fTypeDeclType = new XSType[1][fXSTypeCount];
        fGlobalTypeDecls = new SymbolHash();

        try {
            int typeIndex = 0;
            XSComplexTypeDecl anyType = new XSComplexTypeDecl();
            fTypeDeclType[0][typeIndex] = anyType;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ANYTYPE, typeIndex++);
            //REVISIT: make anyType the base of anySimpleType
            //DatatypeValidator anySimpleType = new AnySimpleType(anyType, null, false);
            DatatypeValidator anySimpleType = new AnySimpleType(null, null, false);
            fTypeDeclType[0][typeIndex] = anySimpleType;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ANYSIMPLETYPE, typeIndex++);
            DatatypeValidator stringDV = new StringDatatypeValidator(anySimpleType, null, false);
            fTypeDeclType[0][typeIndex] = stringDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_STRING, typeIndex++);
            fTypeDeclType[0][typeIndex] = new BooleanDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_BOOLEAN, typeIndex++);
            DatatypeValidator decimalDV = new DecimalDatatypeValidator(anySimpleType, null, false);
            fTypeDeclType[0][typeIndex] = decimalDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DECIMAL, typeIndex++);
            fTypeDeclType[0][typeIndex] = new AnyURIDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ANYURI, typeIndex++);
            fTypeDeclType[0][typeIndex] = new Base64BinaryDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_BASE64BINARY, typeIndex++);
            fTypeDeclType[0][typeIndex] = new DurationDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DURATION, typeIndex++);
            fTypeDeclType[0][typeIndex] = new DateTimeDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DATETIME, typeIndex++);
            fTypeDeclType[0][typeIndex] = new TimeDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_TIME, typeIndex++);
            fTypeDeclType[0][typeIndex] = new DateDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DATE, typeIndex++);
            fTypeDeclType[0][typeIndex] = new YearMonthDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_YEARMONTH, typeIndex++);
            fTypeDeclType[0][typeIndex] = new YearDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_YEAR, typeIndex++);
            fTypeDeclType[0][typeIndex] = new MonthDayDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_MONTHDAY, typeIndex++);
            fTypeDeclType[0][typeIndex] = new DayDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DAY, typeIndex++);
            fTypeDeclType[0][typeIndex] = new MonthDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_MONTH, typeIndex++);
    
            Hashtable facets = new Hashtable(2);
            facets.put(SchemaSymbols.ELT_FRACTIONDIGITS, "0");
            DatatypeValidator integerDV = new DecimalDatatypeValidator(decimalDV, facets, false);
            fTypeDeclType[0][typeIndex] = integerDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_INTEGER, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "0" );
            DatatypeValidator nonPositiveDV = new DecimalDatatypeValidator(integerDV, facets, false);
            fTypeDeclType[0][typeIndex] = nonPositiveDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "-1" );
            fTypeDeclType[0][typeIndex] = new DecimalDatatypeValidator(nonPositiveDV, facets, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NEGATIVEINTEGER, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "9223372036854775807");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-9223372036854775808");
            DatatypeValidator longDV = new DecimalDatatypeValidator(integerDV, facets, false);
            fTypeDeclType[0][typeIndex] = longDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_LONG, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "2147483647");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-2147483648");
            DatatypeValidator intDV = new DecimalDatatypeValidator(longDV, facets, false);
            fTypeDeclType[0][typeIndex] = intDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_INT, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "32767");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-32768");
            DatatypeValidator shortDV = new DecimalDatatypeValidator(intDV, facets, false);
            fTypeDeclType[0][typeIndex] = shortDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_SHORT, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE , "127");
            facets.put(SchemaSymbols.ELT_MININCLUSIVE,  "-128");
            fTypeDeclType[0][typeIndex] = new DecimalDatatypeValidator(shortDV, facets, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_BYTE, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MININCLUSIVE, "0" );
            DatatypeValidator nonNegativeDV = new DecimalDatatypeValidator(integerDV, facets, false);
            fTypeDeclType[0][typeIndex] = nonNegativeDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "18446744073709551615" );
            DatatypeValidator unsignedLongDV = new DecimalDatatypeValidator(nonNegativeDV, facets, false);
            fTypeDeclType[0][typeIndex] = unsignedLongDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_UNSIGNEDLONG, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "4294967295" );
            DatatypeValidator unsignedIntDV = new DecimalDatatypeValidator(unsignedLongDV, facets, false);
            fTypeDeclType[0][typeIndex] = unsignedIntDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_UNSIGNEDINT, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "65535" );
            DatatypeValidator unsignedShortDV = new DecimalDatatypeValidator(unsignedIntDV, facets, false);
            fTypeDeclType[0][typeIndex] = unsignedShortDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_UNSIGNEDSHORT, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MAXINCLUSIVE, "255" );
            fTypeDeclType[0][typeIndex] = new DecimalDatatypeValidator(unsignedShortDV, facets, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_UNSIGNEDBYTE, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_MININCLUSIVE, "1" );
            fTypeDeclType[0][typeIndex] = new DecimalDatatypeValidator(nonNegativeDV, facets, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_POSITIVEINTEGER, typeIndex++);
    
            if (!fullSet)
                return;
    
            fTypeDeclType[0][typeIndex] = new FloatDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_FLOAT, typeIndex++);
            fTypeDeclType[0][typeIndex] = new DoubleDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_DOUBLE, typeIndex++);
            fTypeDeclType[0][typeIndex] = new HexBinaryDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_HEXBINARY, typeIndex++);
            fTypeDeclType[0][typeIndex] = new NOTATIONDatatypeValidator(anySimpleType, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NOTATION, typeIndex++);
            
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_REPLACE);
            DatatypeValidator normalizedDV = new StringDatatypeValidator(stringDV, facets, false);
            fTypeDeclType[0][typeIndex] = normalizedDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NORMALIZEDSTRING, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            DatatypeValidator tokenDV = new StringDatatypeValidator(normalizedDV, facets, false);
            fTypeDeclType[0][typeIndex] = tokenDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_TOKEN, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            //REVISIT: won't run: regexparser, locale, resource bundle
            //facets.put(SchemaSymbols.ELT_PATTERN , "([a-zA-Z]{2}|[iI]-[a-zA-Z]+|[xX]-[a-zA-Z]+)(-[a-zA-Z]+)*");
            fTypeDeclType[0][typeIndex] = new StringDatatypeValidator(tokenDV, facets, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_LANGUAGE, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NAME);
            DatatypeValidator nameDV = new StringDatatypeValidator(tokenDV, facets, false);
            fTypeDeclType[0][typeIndex] = nameDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NAME, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NCNAME);
            DatatypeValidator ncnameDV = new StringDatatypeValidator(nameDV, facets, false);
            fTypeDeclType[0][typeIndex] = ncnameDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NCNAME, typeIndex++);
            DatatypeValidator qnameDV = new QNameDatatypeValidator(anySimpleType, null, false);
            ((QNameDatatypeValidator)qnameDV).setNCNameValidator(ncnameDV);
            fTypeDeclType[0][typeIndex] = qnameDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_QNAME, typeIndex++);
            fTypeDeclType[0][typeIndex] = new IDDatatypeValidator(ncnameDV, null, false);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ID, typeIndex++);
            DatatypeValidator idrefDV = new IDREFDatatypeValidator(ncnameDV, null, false);
            fTypeDeclType[0][typeIndex] = idrefDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_IDREF, typeIndex++);
            fTypeDeclType[0][typeIndex] = new ListDatatypeValidator(idrefDV, null, true);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_IDREFS, typeIndex++);
            //REVISIT: entity validators
            //DatatypeValidator entityDV = new ENTITYDatatypeValidator(ncnameDV, null, false);
            DatatypeValidator entityDV = new StringDatatypeValidator(ncnameDV, null, false);
            fTypeDeclType[0][typeIndex] = entityDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ENTITY, typeIndex++);
            //REVISIT: entity validators
            //fTypeDeclType[0][typeIndex] = new ListDatatypeValidator(entityDV, null, true);
            fTypeDeclType[0][typeIndex] = new StringDatatypeValidator(entityDV, null, true);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_ENTITIES, typeIndex++);
            facets.clear();
            facets.put(SchemaSymbols.ELT_WHITESPACE, SchemaSymbols.ATTVAL_COLLAPSE);
            facets.put(AbstractStringValidator.FACET_SPECIAL_TOKEN, AbstractStringValidator.SPECIAL_TOKEN_NMTOKEN);
            DatatypeValidator nmtokenDV = new StringDatatypeValidator(tokenDV, facets, false);
            fTypeDeclType[0][typeIndex] = nmtokenDV;
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NMTOKEN, typeIndex++);
            fTypeDeclType[0][typeIndex] = new ListDatatypeValidator(nmtokenDV, null, true);
            fGlobalTypeDecls.put(SchemaSymbols.ATTVAL_NMTOKENS, typeIndex++);
        } catch (InvalidDatatypeFacetException idf) {
            // should never reach here
            System.err.println("internal error: creating schema datatypes");
        }
    } // <init>(SymbolTable)

    /** Returns the symbol table. */
    public SymbolTable getSymbolTable() {
        return fSymbolTable;
    } // getSymbolTable():SymbolTable

    /** Returns this grammar's target namespace. */
    public String getTargetNamespace() {
        return fTargetNamespace;
    } // getTargetNamespace():String
    
    /**
     * addElementDecl
     * 
     * @param element
     * @param isGlobal
     * 
     * @return index
     */
    public int addElementDecl(XSElementDecl element, boolean isGlobal) {
        //REVISIT
        //ensureCapacityElement();
        int elementIndex = fElementDeclCount++;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        fElementDeclName[chunk][index] = new QName(element.fQName);
        fElementDeclTypeNS[chunk][index] = element.fTypeNS;
        fElementDeclTypeDecl[chunk][index] = element.fTypeIdx;
        fElementDeclMiscFlags[chunk][index] = element.fElementMiscFlags;
        fElementDeclBlockSet[chunk][index] = element.fBlock;
        fElementDeclFinalSet[chunk][index] = element.fFinal;
        fElementDeclDefault[chunk][index] = element.fDefault;
        fElementDeclSubGroupNS[chunk][index] = element.fSubGroupNS;
        fElementDeclSubGroupIdx[chunk][index] = element.fSubGroupIdx;
        //REVISIT: other fields
        
        if (isGlobal)
            fGlobalElemDecls.put(element.fQName.localpart, elementIndex);
        
        return elementIndex;
    }

    /**
     * 
     * @param type       particle type
     * @param value      particle left child
     * @param otherValue particle right child
     * @return 
     */
    public int addParticle (short type, int value, int otherValue){
        fParticleCount++;
        int chunk = fParticleCount >> CHUNK_SHIFT;
        int index = fParticleCount & CHUNK_MASK;
        //
        //Implement
        //ensureParticleCapacity(chunk);
        fParticleType[chunk][index] = type;
        fParticleUri[chunk][index] = null;
        fParticleValue[chunk][index] = value;
        fParticleOtherUri[chunk][index] = null;
        fParticleOtherValue[chunk][index] = otherValue;
        fParticleMinOccurs[chunk][index] = 1;
        fParticleMaxOccurs[chunk][index] = 1;
        return fParticleCount;
    }

    /**
     * getElementIndex
     * 
     * @param elementName
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public int getElementIndex(String elementName) {
        return fGlobalElemDecls.get(elementName);
    } // getElementDecl(int,XSElementDecl):XSElementDecl


    /**
     * getElementDecl
     * 
     * @param elementDeclIndex 
     * @param elementDecl The values of this structure are set by this call.
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public XSElementDecl getElementDecl(int elementDeclIndex, 
                                        XSElementDecl elementDecl) {

        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {
            return null;
        }

        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        elementDecl.fQName.setValues(fElementDeclName[chunk][index]);
        elementDecl.fTypeNS = fElementDeclTypeNS[chunk][index];
        elementDecl.fTypeIdx = fElementDeclTypeDecl[chunk][index];
        elementDecl.fElementMiscFlags = fElementDeclMiscFlags[chunk][index];
        elementDecl.fBlock = fElementDeclBlockSet[chunk][index];
        elementDecl.fFinal = fElementDeclFinalSet[chunk][index];
        elementDecl.fDefault = fElementDeclDefault[chunk][index];
        elementDecl.fSubGroupNS = fElementDeclSubGroupNS[chunk][index];
        elementDecl.fSubGroupIdx = fElementDeclSubGroupIdx[chunk][index];

        // REVISIT: 
        // add code

        return elementDecl;
    } // getElementDecl(int,XSElementDecl):XSElementDecl


    /**
     * getElementDecl
     * 
     * @param elementName
     * @param elementDecl The values of this structure are set by this call.
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public XSElementDecl getElementDecl(String elementName, 
                                        XSElementDecl elementDecl) {

        int elementIndex = getElementIndex(elementName);
        return getElementDecl(elementIndex, elementDecl);
    } // getElementDecl(int,XSElementDecl):XSElementDecl


    /**
     * addAttributeDecl
     * 
     * @param attribute
     * @param isGlobal
     * 
     * @return index
     */
    public int addAttributeDecl(XSAttributeDecl attribute, boolean isGlobal) {
        //REVISIT
        //ensureCapacityAttribute();
        int attributeIndex = fAttributeDeclCount++;
        int chunk = attributeIndex >> CHUNK_SHIFT;
        int index = attributeIndex & CHUNK_MASK;
        fAttributeDeclName[chunk][index] = new QName(attribute.fQName);
        fAttributeDeclTypeNS[chunk][index] = attribute.fTypeNS;
        fAttributeDeclType[chunk][index] = attribute.fTypeIdx;
        fAttributeDeclDefault[chunk][index] = attribute.fDefaultValue;
        fAttributeDeclConstraintType[chunk][index] = attribute.fConstraintType;
        //REVISIT: other fields
        
        if (isGlobal)
            fGlobalAttrDecls.put(attribute.fQName.localpart, attributeIndex);
        
        return attributeIndex;
    }

    /**
     * getAttributeIndex
     * 
     * @param attributeName
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public int getAttributeIndex(String attributeName) {
        return fGlobalAttrDecls.get(attributeName);
    } // getAttributeIndex(String):int


    /**
     * getAttributeDecl
     * 
     * @param attributeDeclIndex 
     * @param attributeDecl The values of this structure are set by this call.
     * 
     * @return REVISIT: previously if failed returned false
     */
    public XSAttributeDecl getAttributeDecl(int attributeDeclIndex, XSAttributeDecl attributeDecl) {
        if (attributeDeclIndex < 0 || attributeDeclIndex >= fAttributeDeclCount) {
            return null;
        }
        
        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex & CHUNK_MASK;

        attributeDecl.fQName.setValues(fAttributeDeclName[chunk][index]);
        //REVISIT: add code

        return attributeDecl;
    } // getAttributeDecl

    /**
     * getAttributeDecl
     * 
     * @param attributeName 
     * @param attributeDecl The values of this structure are set by this call.
     * 
     * @return REVISIT: previously if failed returned false
     */
    public XSAttributeDecl getAttributeDecl(String attributeName, XSAttributeDecl attributeDecl) {
        int attributeDeclIndex = getAttributeIndex(attributeName);
        return getAttributeDecl(attributeDeclIndex, attributeDecl);
    } // getAttributeDecl

    /**
     * getAttributeGroupIndex
     * 
     * @param attrGroupName
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public int getAttributeGroupIndex(String attrGroupName) {
        return fGlobalAttrGrpDecls.get(attrGroupName);
    } // getAttributeGroupIndex(String):int


    /**
     * getGroupIndex
     * 
     * @param GroupName
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public int getGroupIndex(String GroupName) {
        return fGlobalGroupDecls.get(GroupName);
    } // getGroupIndex(String):int


    /**
     * getNotationIndex
     * 
     * @param notationName
     * 
     * @return REVISIT: previously if failed returned false 
     */
    public int getNotationIndex(String notationName) {
        return fGlobalNotationDecls.get(notationName);
    } // getNotationIndex(String):int


    /**
     * getNotationDecl
     * 
     * @param notationDeclIndex 
     * @param notationDecl 
     * 
     * @return 
     */
    /*public XSNotationDecl getNotationDecl(int notationDeclIndex, XSNotationDecl notationDecl) {
        if (notationDeclIndex < 0 || notationDeclIndex >= fNotationCount) {
            return false;
        }
        int chunk = notationDeclIndex >> CHUNK_SHIFT;
        int index = notationDeclIndex & CHUNK_MASK;

        notationDecl.setValues(fNotationName[chunk][index], 
                               fNotationPublicId[chunk][index],
                               fNotationSystemId[chunk][index]);

        return true;

    } */
    // getNotationDecl


    /**
     * getParticleDecl
     * 
     * @param particleIndex 
     * @param particle
     * 
     * @return REVISIT: previously if failed returned false
     */
    public XSParticleDecl getParticleDecl(int particleIndex, XSParticleDecl particle) {
        if (particleIndex < 0 || particleIndex >= fParticleCount )
            return null;

        int chunk = particleIndex >> CHUNK_SHIFT;
        int index = particleIndex & CHUNK_MASK;

        particle.type = fParticleType[chunk][index];

        // REVISIT: 
        // add code
        
        return particle;
    }

    /**
     * Set min/max values
     * 
     * @param particleIndex
     * @param min
     * @param max
     */
    public void setParticleMinMax (int particleIndex, int min, int max){        
        if (particleIndex < 0 || particleIndex >= fParticleCount )
            return;
        int chunk = particleIndex >> CHUNK_SHIFT;
        int index = particleIndex & CHUNK_MASK;
        fParticleMinOccurs[chunk][index] = min;
        fParticleMaxOccurs[chunk][index] = max;
    }
    /**
     * addTypeDecl
     * 
     * @param name
     * @param type
     * 
     * @return index
     */
    public int addTypeDecl(XSType type, boolean isGlobal) {
        //REVISIT
        //ensureCapacityType();
        int typeIndex = fXSTypeCount++;
        int chunk = typeIndex >> CHUNK_SHIFT;
        int index = typeIndex & CHUNK_MASK;
        fTypeDeclType[chunk][index] = type;

        if (isGlobal) {
            fGlobalTypeDecls.put(type.getXSTypeName(), typeIndex);
        }
        
        return typeIndex;
    }

    /**
     * getTypeIndex
     * 
     * @param typeName
     * 
     * @return REVISIT: previously if failed returned false
     */
    public int getTypeIndex(String typeName) {
        return fGlobalTypeDecls.get(typeName);
    }

    /**
     * getTypeDecl
     * 
     * @param typeIndex 
     * 
     * @return REVISIT: previously if failed returned false
     */
    public XSType getTypeDecl(int typeIndex) {
        if (typeIndex < 0 || typeIndex >= fXSTypeCount )
            return null;

        int chunk = typeIndex >> CHUNK_SHIFT;
        int index = typeIndex & CHUNK_MASK;

        return fTypeDeclType[chunk][index];
    }

    /**
     * getTypeDecl
     * 
     * @param typeName
     * 
     * @return REVISIT: previously if failed returned false
     */
    public XSType getTypeDecl(String typeName) {
        int typeIndex = getTypeIndex(typeName);
        return getTypeDecl(typeIndex);
    }

    static SchemaGrammar SG_SchemaNS = new SchemaGrammar(null, true);
    static SchemaGrammar SG_SchemaBasicSet = new SchemaGrammar(null, false);
    
} // class SchemaGrammar
