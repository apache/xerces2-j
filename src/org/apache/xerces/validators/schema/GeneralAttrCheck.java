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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.schema;

import java.util.*;
import org.w3c.dom.*;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.validators.datatype.*;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.validators.common.XMLAttributeDecl;
import org.apache.xerces.validators.common.GrammarResolver;
import org.apache.xerces.validators.common.Grammar;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * @author:  Sandy Gao, IBM
 * @version 1.0
 */

public class GeneralAttrCheck {

    // used to specify in which context the element appears: global or local
    public static int ELE_CONTEXT_GLOBAL    = 0;
    public static int ELE_CONTEXT_LOCAL     = 1;

    // used to specify whether the attribute is optional,
    // and whether it has a default value
    public static int ATT_REQUIRED          = 0;
    public static int ATT_OPT_DFLT          = 1;
    public static int ATT_OPT_NODFLT        = 2;

    // the prefix to distinguish gloval vs. local; name vs. ref
    protected static String PRE_GLOBAL      = "G_";
    protected static String PRE_LOC_NAME    = "LN_";
    protected static String PRE_LOC_REF     = "LR_";

    // used to store the map from element name to attribute list
    protected static Hashtable fEleAttrsMap = new Hashtable();

    // used to store extra datatype validators
    protected static DatatypeValidator[] fExtraDVs = null;

    // used to initialize fEleAttrsMap
    // step 1: all possible data types
    // DT_??? >= 0 : validate using a validator, which is initialized staticly
    // DT_??? <  0 : validate directly, which is done in "validate()"
    protected static int dtCount             = 0;

    protected static final int DT_ANYURI           = dtCount++;
    protected static final int DT_BOOLEAN          = dtCount++;
    protected static final int DT_ID               = dtCount++;
    protected static final int DT_NONNEGINT        = dtCount++;
    protected static final int DT_QNAME            = dtCount++;
    protected static final int DT_STRING           = dtCount++;
    protected static final int DT_TOKEN            = dtCount++;
    protected static final int DT_NCNAME           = dtCount++;
    protected static final int DT_XPATH            = dtCount++;
    protected static final int DT_XPATH1           = dtCount++;

    protected static final int DT_BLOCK            = -1;
    protected static final int DT_BLOCK1           = DT_BLOCK-1;
    protected static final int DT_FINAL            = DT_BLOCK1-1;
    protected static final int DT_FINAL1           = DT_FINAL-1;
    protected static final int DT_FORM             = DT_FINAL1-1;
    protected static final int DT_MAXOCCURS        = DT_FORM-1;
    protected static final int DT_MAXOCCURS1       = DT_MAXOCCURS-1;
    protected static final int DT_MEMBERTYPES      = DT_MAXOCCURS1-1;
    protected static final int DT_MINOCCURS1       = DT_MEMBERTYPES-1;
    protected static final int DT_NAMESPACE        = DT_MINOCCURS1-1;
    protected static final int DT_PROCESSCONTENTS  = DT_NAMESPACE-1;
    protected static final int DT_PUBLIC           = DT_PROCESSCONTENTS-1;
    protected static final int DT_USE              = DT_PUBLIC-1;
    protected static final int DT_WHITESPACE       = DT_USE-1;

    static {
        fExtraDVs = new DatatypeValidator[dtCount];

        // step 2: all possible attributes for all elements
        int attCount = 0;
        int ATT_ABSTRACT_D          = attCount++;
        int ATT_ATTRIBUTE_FD_D      = attCount++;
        int ATT_BASE_R              = attCount++;
        int ATT_BASE_N              = attCount++;
        int ATT_BLOCK_N             = attCount++;
        int ATT_BLOCK1_N            = attCount++;
        int ATT_BLOCK_D_D           = attCount++;
        int ATT_DEFAULT_N           = attCount++;
        int ATT_ELEMENT_FD_D        = attCount++;
        int ATT_FINAL_N             = attCount++;
        int ATT_FINAL1_N            = attCount++;
        int ATT_FINAL_D_D           = attCount++;
        int ATT_FIXED_N             = attCount++;
        int ATT_FIXED_D             = attCount++;
        int ATT_FORM_N              = attCount++;
        int ATT_ID_N                = attCount++;
        int ATT_ITEMTYPE_N          = attCount++;
        int ATT_MAXOCCURS_D         = attCount++;
        int ATT_MAXOCCURS1_D        = attCount++;
        int ATT_MEMBER_T_N          = attCount++;
        int ATT_MINOCCURS_D         = attCount++;
        int ATT_MINOCCURS1_D        = attCount++;
        int ATT_MIXED_D             = attCount++;
        int ATT_MIXED_N             = attCount++;
        int ATT_NAME_R              = attCount++;
        int ATT_NAMESPACE_D         = attCount++;
        int ATT_NAMESPACE_N         = attCount++;
        int ATT_NILLABLE_D          = attCount++;
        int ATT_PROCESS_C_D         = attCount++;
        int ATT_PUBLIC_R            = attCount++;
        int ATT_REF_R               = attCount++;
        int ATT_REFER_R             = attCount++;
        int ATT_SCHEMA_L_R          = attCount++;
        int ATT_SCHEMA_L_N          = attCount++;
        int ATT_SOURCE_N            = attCount++;
        int ATT_SUBSTITUTION_G_N    = attCount++;
        int ATT_SYSTEM_N            = attCount++;
        int ATT_TARGET_N_N          = attCount++;
        int ATT_TYPE_N              = attCount++;
        int ATT_USE_D               = attCount++;
        int ATT_VALUE_NNI_N         = attCount++;
        int ATT_VALUE_STR_N         = attCount++;
        int ATT_VALUE_WS_N          = attCount++;
        int ATT_VERSION_N           = attCount++;
        int ATT_XPATH_R             = attCount++;
        int ATT_XPATH1_R            = attCount++;

        // step 3: store all these attributes in an array
        OneAttr[] allAttrs = new OneAttr[attCount];
        allAttrs[ATT_ABSTRACT_D]        =   new OneAttr(SchemaSymbols.ATT_ABSTRACT,
                                                        DT_BOOLEAN,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_FALSE);
        allAttrs[ATT_ATTRIBUTE_FD_D]    =   new OneAttr(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT,
                                                        DT_FORM,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_UNQUALIFIED);
        allAttrs[ATT_BASE_R]            =   new OneAttr(SchemaSymbols.ATT_BASE,
                                                        DT_QNAME,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_BASE_N]            =   new OneAttr(SchemaSymbols.ATT_BASE,
                                                        DT_QNAME,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_BLOCK_N]           =   new OneAttr(SchemaSymbols.ATT_BLOCK,
                                                        DT_BLOCK,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_BLOCK1_N]          =   new OneAttr(SchemaSymbols.ATT_BLOCK,
                                                        DT_BLOCK1,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_BLOCK_D_D]         =   new OneAttr(SchemaSymbols.ATT_BLOCKDEFAULT,
                                                        DT_BLOCK,
                                                        ATT_OPT_DFLT,
                                                        "");
        allAttrs[ATT_DEFAULT_N]         =   new OneAttr(SchemaSymbols.ATT_DEFAULT,
                                                        DT_STRING,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_ELEMENT_FD_D]      =   new OneAttr(SchemaSymbols.ATT_ELEMENTFORMDEFAULT,
                                                        DT_FORM,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_UNQUALIFIED);
        allAttrs[ATT_FINAL_N]           =   new OneAttr(SchemaSymbols.ATT_FINAL,
                                                        DT_FINAL,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_FINAL1_N]          =   new OneAttr(SchemaSymbols.ATT_FINAL,
                                                        DT_FINAL1,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_FINAL_D_D]         =   new OneAttr(SchemaSymbols.ATT_FINALDEFAULT,
                                                        DT_FINAL,
                                                        ATT_OPT_DFLT,
                                                        "");
        allAttrs[ATT_FIXED_N]           =   new OneAttr(SchemaSymbols.ATT_FIXED,
                                                        DT_STRING,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_FIXED_D]           =   new OneAttr(SchemaSymbols.ATT_FIXED,
                                                        DT_BOOLEAN,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_FALSE);
        allAttrs[ATT_FORM_N]            =   new OneAttr(SchemaSymbols.ATT_FORM,
                                                        DT_FORM,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_ID_N]              =   new OneAttr(SchemaSymbols.ATT_ID,
                                                        DT_ID,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_ITEMTYPE_N]        =   new OneAttr(SchemaSymbols.ATT_ITEMTYPE,
                                                        DT_QNAME,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_MAXOCCURS_D]       =   new OneAttr(SchemaSymbols.ATT_MAXOCCURS,
                                                        DT_MAXOCCURS,
                                                        ATT_OPT_DFLT,
                                                        "1");
        allAttrs[ATT_MAXOCCURS1_D]      =   new OneAttr(SchemaSymbols.ATT_MAXOCCURS,
                                                        DT_MAXOCCURS1,
                                                        ATT_OPT_DFLT,
                                                        "1");
        allAttrs[ATT_MEMBER_T_N]        =   new OneAttr(SchemaSymbols.ATT_MEMBERTYPES,
                                                        DT_MEMBERTYPES,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_MINOCCURS_D]       =   new OneAttr(SchemaSymbols.ATT_MINOCCURS,
                                                        DT_NONNEGINT,
                                                        ATT_OPT_DFLT,
                                                        "1");
        allAttrs[ATT_MINOCCURS1_D]      =   new OneAttr(SchemaSymbols.ATT_MINOCCURS,
                                                        DT_MINOCCURS1,
                                                        ATT_OPT_DFLT,
                                                        "1");
        allAttrs[ATT_MIXED_D]           =   new OneAttr(SchemaSymbols.ATT_MIXED,
                                                        DT_BOOLEAN,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_FALSE);
        allAttrs[ATT_MIXED_N]           =   new OneAttr(SchemaSymbols.ATT_MIXED,
                                                        DT_BOOLEAN,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_NAME_R]            =   new OneAttr(SchemaSymbols.ATT_NAME,
                                                        DT_NCNAME,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_NAMESPACE_D]       =   new OneAttr(SchemaSymbols.ATT_NAMESPACE,
                                                        DT_NAMESPACE,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_TWOPOUNDANY);
        allAttrs[ATT_NAMESPACE_N]       =   new OneAttr(SchemaSymbols.ATT_NAMESPACE,
                                                        DT_ANYURI,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_NILLABLE_D]        =   new OneAttr(SchemaSymbols.ATT_NILLABLE,
                                                        DT_BOOLEAN,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_FALSE);
        allAttrs[ATT_PROCESS_C_D]       =   new OneAttr(SchemaSymbols.ATT_PROCESSCONTENTS,
                                                        DT_PROCESSCONTENTS,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_STRICT);
        allAttrs[ATT_PUBLIC_R]          =   new OneAttr(SchemaSymbols.ATT_PUBLIC,
                                                        DT_PUBLIC,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_REF_R]             =   new OneAttr(SchemaSymbols.ATT_REF,
                                                        DT_QNAME,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_REFER_R]           =   new OneAttr(SchemaSymbols.ATT_REFER,
                                                        DT_QNAME,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_SCHEMA_L_R]        =   new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION,
                                                        DT_ANYURI,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_SCHEMA_L_N]        =   new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION,
                                                        DT_ANYURI,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_SOURCE_N]          =   new OneAttr(SchemaSymbols.ATT_SOURCE,
                                                        DT_ANYURI,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_SUBSTITUTION_G_N]  =   new OneAttr(SchemaSymbols.ATT_SUBSTITUTIONGROUP,
                                                        DT_QNAME,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_SYSTEM_N]          =   new OneAttr(SchemaSymbols.ATT_SYSTEM,
                                                        DT_ANYURI,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_TARGET_N_N]        =   new OneAttr(SchemaSymbols.ATT_TARGETNAMESPACE,
                                                        DT_ANYURI,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_TYPE_N]            =   new OneAttr(SchemaSymbols.ATT_TYPE,
                                                        DT_QNAME,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_USE_D]             =   new OneAttr(SchemaSymbols.ATT_USE,
                                                        DT_USE,
                                                        ATT_OPT_DFLT,
                                                        SchemaSymbols.ATTVAL_OPTIONAL);
        allAttrs[ATT_VALUE_NNI_N]       =   new OneAttr(SchemaSymbols.ATT_VALUE,
                                                        DT_NONNEGINT,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_VALUE_STR_N]       =   new OneAttr(SchemaSymbols.ATT_VALUE,
                                                        DT_STRING,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_VALUE_WS_N]        =   new OneAttr(SchemaSymbols.ATT_VALUE,
                                                        DT_WHITESPACE,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_VERSION_N]         =   new OneAttr(SchemaSymbols.ATT_VERSION,
                                                        DT_TOKEN,
                                                        ATT_OPT_NODFLT,
                                                        null);
        allAttrs[ATT_XPATH_R]           =   new OneAttr(SchemaSymbols.ATT_XPATH,
                                                        DT_XPATH,
                                                        ATT_REQUIRED,
                                                        null);
        allAttrs[ATT_XPATH1_R]          =   new OneAttr(SchemaSymbols.ATT_XPATH,
                                                        DT_XPATH1,
                                                        ATT_REQUIRED,
                                                        null);

        // step 4: for each element, make a list of possible attributes
        Hashtable attrList;
        Object[] attrArray;
        OneElement oneEle;

        // for element "attribute" - global
        attrList = new Hashtable();
        // default = string
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        // fixed = string
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // type = QName
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_ATTRIBUTE, oneEle);

        // for element "attribute" - local name
        attrList = new Hashtable();
        // default = string
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        // fixed = string
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        // form = (qualified | unqualified)
        attrList.put(SchemaSymbols.ATT_FORM, allAttrs[ATT_FORM_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // type = QName
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        // use = (optional | prohibited | required) : optional
        attrList.put(SchemaSymbols.ATT_USE, allAttrs[ATT_USE_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ATTRIBUTE, oneEle);

        // for element "attribute" - local ref
        attrList = new Hashtable();
        // default = string
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        // fixed = string
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // ref = QName
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        // use = (optional | prohibited | required) : optional
        attrList.put(SchemaSymbols.ATT_USE, allAttrs[ATT_USE_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_REF+SchemaSymbols.ELT_ATTRIBUTE, oneEle);

        // for element "element" - global
        attrList = new Hashtable();
        // abstract = boolean : false
        attrList.put(SchemaSymbols.ATT_ABSTRACT, allAttrs[ATT_ABSTRACT_D]);
        // block = (#all | List of (substitution | extension | restriction | list | union))
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK_N]);
        // default = string
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        // final = (#all | List of (extension | restriction))
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL_N]);
        // fixed = string
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // nillable = boolean : false
        attrList.put(SchemaSymbols.ATT_NILLABLE, allAttrs[ATT_NILLABLE_D]);
        // substitutionGroup = QName
        attrList.put(SchemaSymbols.ATT_SUBSTITUTIONGROUP, allAttrs[ATT_SUBSTITUTION_G_N]);
        // type = QName
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_ELEMENT, oneEle);

        // for element "element" - local name
        attrList = new Hashtable();
        // block = (#all | List of (substitution | extension | restriction | list | union))
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK_N]);
        // default = string
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        // fixed = string
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        // form = (qualified | unqualified)
        attrList.put(SchemaSymbols.ATT_FORM, allAttrs[ATT_FORM_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = (nonNegativeInteger | unbounded)  : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        // minOccurs = nonNegativeInteger : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // nillable = boolean : false
        attrList.put(SchemaSymbols.ATT_NILLABLE, allAttrs[ATT_NILLABLE_D]);
        // type = QName
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ELEMENT, oneEle);

        // for element "element" - local ref
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = (nonNegativeInteger | unbounded)  : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        // minOccurs = nonNegativeInteger : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        // ref = QName
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_REF+SchemaSymbols.ELT_ELEMENT, oneEle);

        // for element "complexType" - global
        attrList = new Hashtable();
        // abstract = boolean : false
        attrList.put(SchemaSymbols.ATT_ABSTRACT, allAttrs[ATT_ABSTRACT_D]);
        // block = (#all | List of (extension | restriction))
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK1_N]);
        // final = (#all | List of (extension | restriction))
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // mixed = boolean : false
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_D]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_COMPLEXTYPE, oneEle);

        // for element "complexType" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // mixed = boolean : false
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_COMPLEXTYPE, oneEle);

        // for element "simpleContent" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_SIMPLECONTENT, oneEle);

        // for element "restriction" - local name
        attrList = new Hashtable();
        // base = QName
        attrList.put(SchemaSymbols.ATT_BASE, allAttrs[ATT_BASE_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_RESTRICTION, oneEle);

        // for element "extension" - local name
        attrList = new Hashtable();
        // base = QName
        attrList.put(SchemaSymbols.ATT_BASE, allAttrs[ATT_BASE_R]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_EXTENSION, oneEle);

        // for element "attributeGroup" - local ref
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // ref = QName
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_REF+SchemaSymbols.ELT_ATTRIBUTEGROUP, oneEle);

        // for element "anyAttribute" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )  : ##any
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_D]);
        // processContents = (lax | skip | strict) : strict
        attrList.put(SchemaSymbols.ATT_PROCESSCONTENTS, allAttrs[ATT_PROCESS_C_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ANYATTRIBUTE, oneEle);

        // for element "complexContent" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // mixed = boolean
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_COMPLEXCONTENT, oneEle);

        // for element "attributeGroup" - global
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_ATTRIBUTEGROUP, oneEle);

        // for element "group" - global
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_GROUP, oneEle);

        // for element "group" - local ref
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = (nonNegativeInteger | unbounded)  : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        // minOccurs = nonNegativeInteger : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        // ref = QName
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_REF+SchemaSymbols.ELT_GROUP, oneEle);

        // for element "all" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = 1 : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS1_D]);
        // minOccurs = (0 | 1) : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS1_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ALL, oneEle);

        // for element "choice" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = (nonNegativeInteger | unbounded)  : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        // minOccurs = nonNegativeInteger : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_CHOICE, oneEle);
        // for element "sequence" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_SEQUENCE, oneEle);

        // for element "any" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // maxOccurs = (nonNegativeInteger | unbounded)  : 1
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        // minOccurs = nonNegativeInteger : 1
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        // namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )  : ##any
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_D]);
        // processContents = (lax | skip | strict) : strict
        attrList.put(SchemaSymbols.ATT_PROCESSCONTENTS, allAttrs[ATT_PROCESS_C_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ANY, oneEle);

        // for element "unique" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_UNIQUE, oneEle);
        // for element "key" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_KEY, oneEle);

        // for element "keyref" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // refer = QName
        attrList.put(SchemaSymbols.ATT_REFER, allAttrs[ATT_REFER_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_KEYREF, oneEle);

        // for element "selector" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // xpath = a subset of XPath expression
        attrList.put(SchemaSymbols.ATT_XPATH, allAttrs[ATT_XPATH_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_SELECTOR, oneEle);

        // for element "field" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // xpath = a subset of XPath expression
        attrList.put(SchemaSymbols.ATT_XPATH, allAttrs[ATT_XPATH1_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_FIELD, oneEle);

        // for element "notation" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        // public = A public identifier, per ISO 8879
        attrList.put(SchemaSymbols.ATT_PUBLIC, allAttrs[ATT_PUBLIC_R]);
        // system = anyURI
        attrList.put(SchemaSymbols.ATT_SYSTEM, allAttrs[ATT_SYSTEM_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_NOTATION, oneEle);

        // for element "annotation" - global
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_ANNOTATION, oneEle);
        // for element "annotation" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ANNOTATION, oneEle);

        // for element "appinfo" - local name
        attrList = new Hashtable();
        // source = anyURI
        attrList.put(SchemaSymbols.ATT_SOURCE, allAttrs[ATT_SOURCE_N]);
        oneEle = new OneElement (attrList, false);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_APPINFO, oneEle);

        // for element "documentation" - local name
        attrList = new Hashtable();
        // source = anyURI
        attrList.put(SchemaSymbols.ATT_SOURCE, allAttrs[ATT_SOURCE_N]);
        // xml:lang = language ???
        oneEle = new OneElement (attrList, false);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_DOCUMENTATION, oneEle);

        // for element "simpleType" - global
        attrList = new Hashtable();
        // final = (#all | (list | union | restriction))
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL1_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // name = NCName
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_SIMPLETYPE, oneEle);

        // for element "simpleType" - local name
        attrList = new Hashtable();
        // final = (#all | (list | union | restriction))
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL1_N]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_SIMPLETYPE, oneEle);

        // for element "restriction" - local name
        // already registered for complexType

        // for element "list" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // itemType = QName
        attrList.put(SchemaSymbols.ATT_ITEMTYPE, allAttrs[ATT_ITEMTYPE_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_LIST, oneEle);

        // for element "union" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // memberTypes = List of QName
        attrList.put(SchemaSymbols.ATT_MEMBERTYPES, allAttrs[ATT_MEMBER_T_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_UNION, oneEle);

        // for element "schema" - global
        attrList = new Hashtable();
        // attributeFormDefault = (qualified | unqualified) : unqualified
        attrList.put(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT, allAttrs[ATT_ATTRIBUTE_FD_D]);
        // blockDefault = (#all | List of (substitution | extension | restriction | list | union))  : ''
        attrList.put(SchemaSymbols.ATT_BLOCKDEFAULT, allAttrs[ATT_BLOCK_D_D]);
        // elementFormDefault = (qualified | unqualified) : unqualified
        attrList.put(SchemaSymbols.ATT_ELEMENTFORMDEFAULT, allAttrs[ATT_ELEMENT_FD_D]);
        // finalDefault = (#all | List of (extension | restriction))  : ''
        attrList.put(SchemaSymbols.ATT_FINALDEFAULT, allAttrs[ATT_FINAL_D_D]);
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // targetNamespace = anyURI
        attrList.put(SchemaSymbols.ATT_TARGETNAMESPACE, allAttrs[ATT_TARGET_N_N]);
        // version = token
        attrList.put(SchemaSymbols.ATT_VERSION, allAttrs[ATT_VERSION_N]);
        // xml:lang = language ???
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_SCHEMA, oneEle);

        // for element "include" - global
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // schemaLocation = anyURI
        attrList.put(SchemaSymbols.ATT_SCHEMALOCATION, allAttrs[ATT_SCHEMA_L_R]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_INCLUDE, oneEle);
        // for element "redefine" - global
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_REDEFINE, oneEle);

        // for element "import" - global
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // namespace = anyURI
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_N]);
        // schemaLocation = anyURI
        attrList.put(SchemaSymbols.ATT_SCHEMALOCATION, allAttrs[ATT_SCHEMA_L_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_GLOBAL+SchemaSymbols.ELT_IMPORT, oneEle);

        // for element "length" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // value = nonNegativeInteger
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_NNI_N]);
        // fixed = boolean : false
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_LENGTH, oneEle);
        // for element "minLength" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MINLENGTH, oneEle);
        // for element "maxLength" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MAXLENGTH, oneEle);
        // for element "totalDigits" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_TOTALDIGITS, oneEle);
        // for element "fractionDigits" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_FRACTIONDIGITS, oneEle);

        // for element "pattern" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // value = string
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_PATTERN, oneEle);

        // for element "enumeration" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // value = anySimpleType ???
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_ENUMERATION, oneEle);

        // for element "whiteSpace" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // value = preserve | replace | collapse
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_WS_N]);
        // fixed = boolean : false
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_WHITESPACE, oneEle);

        // for element "maxInclusive" - local name
        attrList = new Hashtable();
        // id = ID
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        // value = anySimpleType ???
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        // fixed = boolean : false
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        oneEle = new OneElement (attrList);
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MAXINCLUSIVE, oneEle);
        // for element "maxExclusive" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MAXEXCLUSIVE, oneEle);
        // for element "minInclusive" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MININCLUSIVE, oneEle);
        // for element "minExclusive" - local name
        fEleAttrsMap.put(PRE_LOC_NAME+SchemaSymbols.ELT_MINEXCLUSIVE, oneEle);
    }

    private Hashtable fIdDefs = new Hashtable();

    // used to store utility reference: error reproter. set via constructor.
    protected XMLErrorReporter fErrorReporter = null;

    // used to store the list of simple type validators
    protected DatatypeValidatorFactoryImpl fDatatypeRegistry;

    // used to store the mapping from processed element to attributes
    protected Hashtable fProcessedElements = new Hashtable();

    // used to store the mapping from processed element to attributes
    protected Hashtable fNonSchemaAttrs = new Hashtable();

    // constructor. Sets fDVRegistry and fErrorReproter
    private GeneralAttrCheck() {}
    public GeneralAttrCheck (XMLErrorReporter er, DatatypeValidatorFactoryImpl datatypeRegistry) {
        fErrorReporter = er;
        fDatatypeRegistry = fDatatypeRegistry;
        synchronized (getClass()) {
            if (fExtraDVs[DT_ANYURI] == null) {
                // step 5: register all datatype validators for new types
                datatypeRegistry.expandRegistryToFullSchemaSet();
                // anyURI
                fExtraDVs[DT_ANYURI] = datatypeRegistry.getDatatypeValidator("anyURI");
                // boolean
                fExtraDVs[DT_BOOLEAN] = datatypeRegistry.getDatatypeValidator("boolean");
                // ID
                fExtraDVs[DT_ID] = datatypeRegistry.getDatatypeValidator("ID");
                // nonNegtiveInteger
                fExtraDVs[DT_NONNEGINT] = datatypeRegistry.getDatatypeValidator("nonNegativeInteger");
                // QName
                fExtraDVs[DT_QNAME] = datatypeRegistry.getDatatypeValidator("QName");
                // string
                fExtraDVs[DT_STRING] = datatypeRegistry.getDatatypeValidator("string");
                // token
                fExtraDVs[DT_TOKEN] = datatypeRegistry.getDatatypeValidator("token");
                // NCName
                fExtraDVs[DT_NCNAME] = datatypeRegistry.getDatatypeValidator("NCName");
                // xpath = a subset of XPath expression
                fExtraDVs[DT_XPATH] = fExtraDVs[DT_STRING];
                // xpath = a subset of XPath expression
                fExtraDVs[DT_XPATH] = fExtraDVs[DT_STRING];
            }
        }
    }

    // check whether the specified element conforms to the attributes restriction
    // @param: element    - which element to check
    // @param: eleContext - the context in which the element is
    // @return: Hashtable - list of attributes and values
    public Hashtable checkAttributes(Element element, int eleContext) throws Exception {
        if (element == null)
            return null;

        Hashtable attrValues = (Hashtable)fProcessedElements.get(element);
        if (attrValues != null)
            return attrValues;

        // Get the proper name:
        // G_ for global;
        // LN_ for local + name;
        // LR_ for local + ref;
        String elName = element.getLocalName(), name;
        if (eleContext == ELE_CONTEXT_GLOBAL) {
            name = PRE_GLOBAL + elName;
        } else {
            if (element.getAttributeNode(SchemaSymbols.ATT_REF) == null)
                name = PRE_LOC_NAME + elName;
            else
                name = PRE_LOC_REF + elName;
        }

        // get desired attribute list of this element
        OneElement oneEle = (OneElement)fEleAttrsMap.get(name);
        if (oneEle == null) {
            reportSchemaError (SchemaMessageProvider.GenericError,
                               new Object[] {"Element '"+elName+"' cannot appear here"});
            return null;
        }

        attrValues = new Hashtable();
        Hashtable attrList = oneEle.attrList;

        // traverse all attributes
        NamedNodeMap attrs = element.getAttributes();
        Attr sattr = null;
        int i = 0;
        while ((sattr = (Attr)attrs.item(i++)) != null) {
            // get the attribute name/value
            String attrName = sattr.getName();
            String attrVal = sattr.getValue();

            // skip anything starts with x/X m/M l/L ???
            // simply put their values in the return hashtable
            if (attrName.toLowerCase().startsWith("xml")) {
                attrValues.put(attrName, new Object[] {sattr.getValue(), Boolean.FALSE});
                continue;
            }

            // for attributes with namespace prefix
            String attrURI = sattr.getNamespaceURI();
            if (attrURI != null && attrURI.length() != 0) {
                // attributes with schema namespace are not allowed
                // and not allowed on "document" and "appInfo"
                if (attrURI.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) ||
                    !oneEle.allowNonSchemaAttr) {
                    reportSchemaError (SchemaMessageProvider.GenericError,
                                       new Object[] {"Attribute '"+attrName+"' cannot appear in '"+elName+"'"});
                } else {
                    // for attributes from other namespace
                    // store them in a list, and TRY to validate them after
                    // schema traversal (because it's "lax")
                    attrValues.put(attrName,
                                   new Object[] {attrVal, Boolean.FALSE});
                    String attrRName = attrURI + "," + sattr.getLocalName();
                    Vector values = (Vector)fNonSchemaAttrs.get(attrRName);
                    if (values == null) {
                        values = new Vector();
                        values.addElement(attrVal);
                        fNonSchemaAttrs.put(attrRName, values);
                    } else {
                        values.addElement(attrVal);
                    }
                }
                continue;
            }

            // check whether this attribute is allowed
            OneAttr oneAttr = (OneAttr)attrList.get(attrName);
            if (oneAttr == null) {
                reportSchemaError (SchemaMessageProvider.GenericError,
                                   new Object[] {"Attribute '"+attrName+"' cannot appear in '"+elName+"'"});
                continue;
            }

            // check the value against the datatype
            try {
                // no checking on string needs to be done here.
                // no checking on xpath needs to be done here.
                // xpath values are validated in xpath parser
                if (oneAttr.dvIndex >= 0) {
                    if (oneAttr.dvIndex != DT_STRING &&
                        oneAttr.dvIndex != DT_XPATH &&
                        oneAttr.dvIndex != DT_XPATH1) {
                        DatatypeValidator dv = fExtraDVs[oneAttr.dvIndex];
                        if (dv instanceof IDDatatypeValidator) {
                            dv.validate( attrVal, fIdDefs );
                        } else {
                            dv.validate( attrVal, null);
                        }
                    }
                    attrValues.put(attrName, new Object[] {attrVal, Boolean.FALSE});
                } else {
                    attrVal = validate(attrName, attrVal, oneAttr.dvIndex);
                    attrValues.put(attrName, new Object[] {attrVal, Boolean.FALSE});
                }
            } catch(InvalidDatatypeValueException ide) {
                reportSchemaError (SchemaMessageProvider.GenericError,
                                   new Object[] {"Invalid attribute value '"+attrVal+"' for '"+
                                   attrName+"' in '"+ elName +"': " + ide.getLocalizedMessage()});
            }
        }

        // traverse all required attributes
        Object[] reqAttrs = oneEle.attrArray;
        for (i = 0; i < reqAttrs.length; i++) {
            OneAttr oneAttr = (OneAttr)reqAttrs[i];

            // if the attribute appreared, skip to the next one
            if (element.getAttributeNode(oneAttr.name) != null)
                continue;

            // if the attribute is required, report an error
            if (oneAttr.optdflt == ATT_REQUIRED) {
                reportSchemaError (SchemaMessageProvider.GenericError,
                                   new Object[] {"Attribute '"+oneAttr.name+"' must appear in '"+elName+"'"});
            }
            // if the attribute is optional with default value, apply it
            else if (oneAttr.optdflt == ATT_OPT_DFLT) {
                attrValues.put(oneAttr.name, new Object[] {oneAttr.dfltValue, Boolean.TRUE});
            }
        }

        fProcessedElements.put(element, attrValues);

        return attrValues;
    }

    private String validate(String attr, String value, int dvIndex) throws InvalidDatatypeValueException {
        Vector unionBase, enum;
        int choice;

        if (value == null)
            return null;

        switch (dvIndex) {
        case DT_BLOCK:
            // block = (#all | List of (substitution | extension | restriction | list | union))
            choice = 0;
            if (value.equals (SchemaSymbols.ATTVAL_POUNDALL)) {
                choice = SchemaSymbols.SUBSTITUTION|SchemaSymbols.EXTENSION|
                         SchemaSymbols.RESTRICTION|SchemaSymbols.LIST|
                         SchemaSymbols.UNION;
            } else {
                StringTokenizer t = new StringTokenizer (value, " ");
                while (t.hasMoreTokens()) {
                    String token = t.nextToken ();

                    if (token.equals (SchemaSymbols.ATTVAL_SUBSTITUTION) ) {
                        choice |= SchemaSymbols.SUBSTITUTION;
                    } else if (token.equals (SchemaSymbols.ATTVAL_EXTENSION)) {
                        choice |= SchemaSymbols.EXTENSION;
                    } else if (token.equals (SchemaSymbols.ATTVAL_RESTRICTION)) {
                        choice |= SchemaSymbols.RESTRICTION;
                    } else if ( token.equals (SchemaSymbols.ELT_LIST) ) {
                        choice |= SchemaSymbols.LIST;
                    } else if ( token.equals (SchemaSymbols.ELT_UNION) ) {
                        choice |= SchemaSymbols.RESTRICTION;
                    } else {
                        throw new InvalidDatatypeValueException();
                    }
                }
            }
//???            value = Integer.toString(choice);
            break;
        case DT_BLOCK1:
        case DT_FINAL:
            // block = (#all | List of (extension | restriction))
            // final = (#all | List of (extension | restriction))
            choice = 0;
            if (value.equals (SchemaSymbols.ATTVAL_POUNDALL)) {
                choice = SchemaSymbols.EXTENSION|SchemaSymbols.RESTRICTION;
            } else {
                StringTokenizer t = new StringTokenizer (value, " ");
                while (t.hasMoreTokens()) {
                    String token = t.nextToken ();

                    if (token.equals (SchemaSymbols.ATTVAL_EXTENSION)) {
                        choice |= SchemaSymbols.EXTENSION;
                    } else if (token.equals (SchemaSymbols.ATTVAL_RESTRICTION)) {
                        choice |= SchemaSymbols.RESTRICTION;
                    } else {
                        throw new InvalidDatatypeValueException();
                    }
                }
            }
//???            value = Integer.toString(choice);
            break;
        case DT_FINAL1:
            // final = (#all | (list | union | restriction))
            choice = 0;
            if (value.equals (SchemaSymbols.ATTVAL_POUNDALL)) {
                choice = SchemaSymbols.RESTRICTION|SchemaSymbols.LIST|
                         SchemaSymbols.UNION;
            } else if (value.equals (SchemaSymbols.ELT_LIST)) {
                choice = SchemaSymbols.LIST;
            } else if (value.equals (SchemaSymbols.ELT_UNION)) {
                choice = SchemaSymbols.UNION;
            } else if (value.equals (SchemaSymbols.ATTVAL_RESTRICTION)) {
                choice = SchemaSymbols.RESTRICTION;
            } else {
                throw new InvalidDatatypeValueException();
            }
//???            value = Integer.toString(choice);
            break;
        case DT_FORM:
            // form = (qualified | unqualified)
            if (!value.equals (SchemaSymbols.ATTVAL_QUALIFIED) &&
                !value.equals (SchemaSymbols.ATTVAL_UNQUALIFIED)) {
                throw new InvalidDatatypeValueException();
            }
            break;
        case DT_MAXOCCURS:
            // maxOccurs = (nonNegativeInteger | unbounded)
            if (!value.equals("unbounded")) {
                fExtraDVs[DT_NONNEGINT].validate(value, null);
            }
            break;
        case DT_MAXOCCURS1:
            // maxOccurs = 1
            if (!value.equals("1"))
                throw new InvalidDatatypeValueException();
            break;
        case DT_MEMBERTYPES:
            // memberTypes = List of QName
            {
                StringTokenizer t = new StringTokenizer (value, " ");
                while (t.hasMoreTokens()) {
                    String token = t.nextToken ();
                    fExtraDVs[DT_QNAME].validate(token, null);
                }
            }
            break;
        case DT_MINOCCURS1:
            // minOccurs = (0 | 1)
            if (!value.equals("0") && !value.equals("1"))
                throw new InvalidDatatypeValueException();
            break;
        case DT_NAMESPACE:
            // namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )
            if (!value.equals(SchemaSymbols.ATTVAL_TWOPOUNDANY) &&
                !value.equals(SchemaSymbols.ATTVAL_TWOPOUNDOTHER)) {
                StringTokenizer t = new StringTokenizer (value, " ");
                while (t.hasMoreTokens()) {
                    String token = t.nextToken ();
                    if (!token.equals(SchemaSymbols.ATTVAL_TWOPOUNDTARGETNS) &&
                        !token.equals(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL)) {
                        fExtraDVs[DT_ANYURI].validate(token, null);
                    }
                }
            }
            break;
        case DT_PROCESSCONTENTS:
            // processContents = (lax | skip | strict)
            if (!value.equals (SchemaSymbols.ATTVAL_SKIP) &&
                !value.equals (SchemaSymbols.ATTVAL_LAX) &&
                !value.equals (SchemaSymbols.ATTVAL_STRICT)) {
                throw new InvalidDatatypeValueException();
            }
            break;
        case DT_PUBLIC:
            // public = A public identifier, per ISO 8879 ???
            fExtraDVs[DT_TOKEN].validate(value, null);
            break;
        case DT_USE:
            // use = (optional | prohibited | required)
            if (!value.equals (SchemaSymbols.ATTVAL_OPTIONAL) &&
                !value.equals (SchemaSymbols.ATTVAL_PROHIBITED) &&
                !value.equals (SchemaSymbols.ATTVAL_REQUIRED)) {
                throw new InvalidDatatypeValueException();
            }
            break;
        case DT_WHITESPACE:
            // value = preserve | replace | collapse
            if (!value.equals (SchemaSymbols.ATT_PRESERVE) &&
                !value.equals (SchemaSymbols.ATT_REPLACE) &&
                !value.equals (SchemaSymbols.ATT_COLLAPSE)) {
                throw new InvalidDatatypeValueException();
            }
            break;
        }

        return value;
    }

    // report an error. copied from TraverseSchema
    private void reportSchemaError(int major, Object args[]) throws Exception {
        if (fErrorReporter == null) {
            System.out.println("__TraverseSchemaError__ : " + SchemaMessageProvider.fgMessageKeys[major]);
            for (int i=0; i< args.length ; i++) {
                System.out.println((String)args[i]);
            }
        }
        else {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       SchemaMessageProvider.SCHEMA_DOMAIN,
                                       major,
                                       SchemaMessageProvider.MSG_NONE,
                                       args,
                                       XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
        }
    }

    // validate attriubtes from non-schema namespaces
    public void checkNonSchemaAttributes(GrammarResolver grammarResolver) throws Exception {
        // for all attributes
        Enumeration enum = fNonSchemaAttrs.keys();
        while (enum.hasMoreElements()) {
            // get name, uri, localpart
            String attrRName = (String)enum.nextElement();
            String attrURI = attrRName.substring(0,attrRName.indexOf(','));
            String attrLocal = attrRName.substring(attrRName.indexOf(',')+1);
            // find associated grammar
            Grammar grammar = grammarResolver.getGrammar(attrURI);
            if (grammar == null || !(grammar instanceof SchemaGrammar))
                continue;
            SchemaGrammar sGrammar = (SchemaGrammar)grammar;
            // then get all top-level attributes from that grammar
            Hashtable attrRegistry = sGrammar.getAttributeDeclRegistry();
            if (attrRegistry == null)
                continue;
            // and get the datatype validator, if there is one
            XMLAttributeDecl tempAttrDecl = (XMLAttributeDecl)attrRegistry.get(attrLocal);
            if (tempAttrDecl == null)
                continue;
            DatatypeValidator dv = tempAttrDecl.datatypeValidator;
            if (dv == null)
                continue;

            // get all values appeared with this attribute name
            Vector values = (Vector)fNonSchemaAttrs.get(attrRName);
            String attrVal;
            String attrName = (String)values.get(0);
            // for each of the values
            int count = values.size();
            for (int i = 1; i < count; i++) {
                // normalize it according to the whiteSpace facet
                attrVal = normalize((String)values.get(i), dv.getWSFacet());
                try {
                    // and validate it using the DatatypeValidator
                    dv.validate(attrVal,null);
                } catch(InvalidDatatypeValueException ide) {
                    reportSchemaError (SchemaMessageProvider.GenericError,
                                       new Object[] {"Invalid attribute value '"+attrVal+"' for '"+
                                       attrName + "': " + ide.getLocalizedMessage()});
                }
            }
        }
    }

    // normalize the string according to the whiteSpace facet
    private String normalize(String content, short ws) {
        int len = content == null ? 0 : content.length();
        if (len == 0 || ws == DatatypeValidator.PRESERVE)
            return content;

        StringBuffer sb = new StringBuffer();
        if (ws == DatatypeValidator.REPLACE) {
            char ch;
            // when it's replace, just replace #x9, #xa, #xd by #x20
            for (int i = 0; i < len; i++) {
                ch = content.charAt(i);
                if (ch != 0x9 && ch != 0xa && ch != 0xd)
                    sb.append(ch);
                else
                    sb.append((char)0x20);
            }
        } else {
            char ch;
            int i;
            boolean isLeading = true;
            // when it's collapse
            for (i = 0; i < len; i++) {
                ch = content.charAt(i);
                // append real characters, so we passed leading ws
                if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20) {
                    sb.append(ch);
                    isLeading = false;
                } else {
                    // for whitespaces, we skip all following ws
                    for (; i < len-1; i++) {
                        ch = content.charAt(i+1);
                        if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20)
                            break;
                    }
                    // if it's not a leading or tailing ws, then append a space
                    if (i < len - 1 && !isLeading)
                        sb.append((char)0x20);
                }
            }
        }

        return sb.toString();
    }
}

class OneAttr {
    // name of the attribute
    public String name;
    // index of the datatype validator
    public int dvIndex;
    // whether it's optional, and has default value
    public int optdflt;
    // the default value of this attribute
    public String dfltValue;

    public OneAttr(String name, int dvIndex, int optdflt, String dfltValue) {
        this.name = name;
        this.dvIndex = dvIndex;
        this.optdflt = optdflt;
        this.dfltValue = dfltValue;
    }
}

class OneElement {
    // the list of attributes that can appear in one element
    public Hashtable attrList;
    // the array of attributes that can appear in one element
    public Object[] attrArray;
    // does this element allow attributes from non-schema namespace
    public boolean allowNonSchemaAttr;

    public OneElement (Hashtable attrList) {
        this(attrList, true);
    }

    public OneElement (Hashtable attrList, boolean allowNonSchemaAttr) {
        this.attrList = attrList;

        int count = attrList.size();
        this.attrArray = new Object[count];
        Enumeration enum = attrList.elements();
        for (int i = 0; i < count; i++)
            this.attrArray[i] = enum.nextElement();

        this.allowNonSchemaAttr = allowNonSchemaAttr;
    }
}
