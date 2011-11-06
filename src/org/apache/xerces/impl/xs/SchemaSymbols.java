/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.xs;


/**
 * Collection of symbols used to parse a Schema Grammar.
 *
 * @xerces.internal 
 *
 * @author jeffrey rodriguez
 * @version $Id$
 */
public final class SchemaSymbols {

    // strings that's not added to the schema symbol table, because they
    // are not symbols in the schema document.
    // the validator can choose to add them by itself.

    // the following strings (xsi:, xsd) will be added into the
    // symbol table that comes with the parser

    // xsi attributes: in validator
    public static final String URI_XSI                       = "http://www.w3.org/2001/XMLSchema-instance".intern();
    public static final String XSI_SCHEMALOCATION            = "schemaLocation".intern();
    public static final String XSI_NONAMESPACESCHEMALOCATION = "noNamespaceSchemaLocation".intern();
    public static final String XSI_TYPE                      = "type".intern();
    public static final String XSI_NIL                       = "nil".intern();
    public static final String EMPTY_STRING                  = "".intern(); 

    // schema namespace
    public static final String URI_SCHEMAFORSCHEMA           = "http://www.w3.org/2001/XMLSchema".intern();

    // schema version namespace
    public static final String URI_SCHEMAVERSION = "http://www.w3.org/2007/XMLSchema-versioning".intern();
    
    // xerces extensions namespace
    public static final String URI_XERCES_EXTENSIONS = "http://xerces.apache.org".intern();

    // all possible schema element names
    public static final String ELT_ALL                    = "all".intern();
    public static final String ELT_ASSERT                 = "assert".intern();
    public static final String ELT_ASSERTION              = "assertion".intern();    
    public static final String ELT_ALTERNATIVE            = "alternative".intern();
    public static final String ELT_ANNOTATION             = "annotation".intern();
    public static final String ELT_ANY                    = "any".intern();
    public static final String ELT_ANYATTRIBUTE           = "anyAttribute".intern();
    public static final String ELT_APPINFO                = "appinfo".intern();
    public static final String ELT_ATTRIBUTE              = "attribute".intern();
    public static final String ELT_ATTRIBUTEGROUP         = "attributeGroup".intern();
    public static final String ELT_CHOICE                 = "choice".intern();
    public static final String ELT_COMPLEXCONTENT         = "complexContent".intern();
    public static final String ELT_COMPLEXTYPE            = "complexType".intern();
    public static final String ELT_DEFAULTOPENCONTENT     = "defaultOpenContent".intern();
    public static final String ELT_DOCUMENTATION          = "documentation".intern();
    public static final String ELT_ELEMENT                = "element".intern();
    public static final String ELT_ENUMERATION            = "enumeration".intern();
    public static final String ELT_EXPLICITTIMEZONE       = "explicitTimezone".intern();
    public static final String ELT_EXTENSION              = "extension".intern();
    public static final String ELT_FIELD                  = "field".intern();
    public static final String ELT_FRACTIONDIGITS         = "fractionDigits".intern();
    public static final String ELT_GROUP                  = "group".intern();
    public static final String ELT_IMPORT                 = "import".intern();
    public static final String ELT_INCLUDE                = "include".intern();
    public static final String ELT_KEY                    = "key".intern();
    public static final String ELT_KEYREF                 = "keyref".intern();
    public static final String ELT_LENGTH                 = "length".intern();
    public static final String ELT_LIST                   = "list".intern();
    public static final String ELT_MAXEXCLUSIVE           = "maxExclusive".intern();
    public static final String ELT_MAXINCLUSIVE           = "maxInclusive".intern();
    public static final String ELT_MAXLENGTH              = "maxLength".intern();
    public static final String ELT_MAXSCALE               = "maxScale".intern();
    public static final String ELT_MINEXCLUSIVE           = "minExclusive".intern();
    public static final String ELT_MININCLUSIVE           = "minInclusive".intern();
    public static final String ELT_MINLENGTH              = "minLength".intern();
    public static final String ELT_MINSCALE               = "minScale".intern();
    public static final String ELT_NOTATION               = "notation".intern();
    public static final String ELT_OPENCONTENT            = "openContent".intern();
    public static final String ELT_OVERRIDE               = "override".intern();
    public static final String ELT_PATTERN                = "pattern".intern();
    public static final String ELT_REDEFINE               = "redefine".intern();
    public static final String ELT_RESTRICTION            = "restriction".intern();
    public static final String ELT_SCHEMA                 = "schema".intern();
    public static final String ELT_SELECTOR               = "selector".intern();
    public static final String ELT_SEQUENCE               = "sequence".intern();
    public static final String ELT_SIMPLECONTENT          = "simpleContent".intern();
    public static final String ELT_SIMPLETYPE             = "simpleType".intern();
    public static final String ELT_TOTALDIGITS            = "totalDigits".intern();
    public static final String ELT_UNION                  = "union".intern();
    public static final String ELT_UNIQUE                 = "unique".intern();
    public static final String ELT_WHITESPACE             = "whiteSpace".intern();
 
    // all possible schema attribute names (and xml:lang defined on <schema> and <documentation>)
    public static final String ATT_ABSTRACT               = "abstract".intern();
    public static final String ATT_APPLIESTOEMPTY         = "appliesToEmpty".intern();
    public static final String ATT_ATTRIBUTEFORMDEFAULT   = "attributeFormDefault".intern();
    public static final String ATT_BASE                   = "base".intern();
    public static final String ATT_BLOCK                  = "block".intern();
    public static final String ATT_BLOCKDEFAULT           = "blockDefault".intern();
    public static final String ATT_DEFAULT                = "default".intern();
    public static final String ATT_DEFAULTATTRIBUTESAPPLY = "defaultAttributesApply".intern();
    public static final String ATT_DEFAULTATTRIBUTES      = "defaultAttributes".intern();    
    public static final String ATT_ELEMENTFORMDEFAULT     = "elementFormDefault".intern();
    public static final String ATT_FINAL                  = "final".intern();
    public static final String ATT_FINALDEFAULT           = "finalDefault".intern();
    public static final String ATT_FIXED                  = "fixed".intern();
    public static final String ATT_FORM                   = "form".intern();
    public static final String ATT_ID                     = "id".intern();
    public static final String ATT_ITEMTYPE               = "itemType".intern();
    public static final String ATT_MAXOCCURS              = "maxOccurs".intern();
    public static final String ATT_MEMBERTYPES            = "memberTypes".intern();
    public static final String ATT_MINOCCURS              = "minOccurs".intern();
    public static final String ATT_MIXED                  = "mixed".intern();
    public static final String ATT_MODE                   = "mode".intern();    
    public static final String ATT_NAME                   = "name".intern();
    public static final String ATT_NAMESPACE              = "namespace".intern();
    public static final String ATT_NILLABLE               = "nillable".intern();
    public static final String ATT_NOTNAMESPACE           = "notNamespace".intern();
    public static final String ATT_NOTQNAME               = "notQName".intern();
    public static final String ATT_PROCESSCONTENTS        = "processContents".intern();
    public static final String ATT_REF                    = "ref".intern();
    public static final String ATT_REFER                  = "refer".intern();
    public static final String ATT_SCHEMALOCATION         = "schemaLocation".intern();
    public static final String ATT_SOURCE                 = "source".intern();
    public static final String ATT_SUBSTITUTIONGROUP      = "substitutionGroup".intern();
    public static final String ATT_SYSTEM                 = "system".intern();
    public static final String ATT_PUBLIC                 = "public".intern();
    public static final String ATT_TARGETNAMESPACE        = "targetNamespace".intern();
    public static final String ATT_TEST                   = "test".intern();
    public static final String ATT_TYPE                   = "type".intern();
    public static final String ATT_USE                    = "use".intern();
    public static final String ATT_VALUE                  = "value".intern();
    public static final String ATT_VERSION                = "version".intern();
    public static final String ATT_XML_LANG               = "xml:lang".intern();
    public static final String ATT_XPATH                  = "xpath".intern();
    public static final String ATT_XPATH_DEFAULT_NS       = "xpathDefaultNamespace".intern();    
    public static final String ATT_MINVERSION             = "minVersion".intern();
    public static final String ATT_MAXVERSION             = "maxVersion".intern();
    public static final String ATT_TYPEAVAILABLE          = "typeAvailable".intern();
    public static final String ATT_TYPEUNAVAILABLE        = "typeUnavailable".intern();
    public static final String ATT_FACETAVAILABLE         = "facetAvailable".intern();
    public static final String ATT_FACETUNAVAILABLE       = "facetUnavailable".intern();
    public static final String ATT_INHERITABLE            = "inheritable".intern();
    public static final String ATT_ASSERT_MESSAGE         = "message".intern();

    // all possible schema attribute values
    public static final String ATTVAL_TWOPOUNDANY            = "##any";
    public static final String ATTVAL_TWOPOUNDDEFAULTNS      = "##defaultNamespace";
    public static final String ATTVAL_TWOPOUNDDEFINED        = "##defined";
    public static final String ATTVAL_TWOPOUNDDEFINEDSIBLING = "##definedSibling";    
    public static final String ATTVAL_TWOPOUNDLOCAL          = "##local";
    public static final String ATTVAL_TWOPOUNDOTHER          = "##other";
    public static final String ATTVAL_TWOPOUNDTARGETNS       = "##targetNamespace";    
    public static final String ATTVAL_POUNDALL               = "#all";
    public static final String ATTVAL_FALSE_0                = "0";
    public static final String ATTVAL_TRUE_1                 = "1";
    public static final String ATTVAL_ANYATOMICTYPE          = "anyAtomicType";    
    public static final String ATTVAL_ANYSIMPLETYPE          = "anySimpleType";
    public static final String ATTVAL_ANYTYPE                = "anyType";
    public static final String ATTVAL_ANYURI                 = "anyURI";
    public static final String ATTVAL_BASE64BINARY           = "base64Binary";
    public static final String ATTVAL_BOOLEAN                = "boolean";
    public static final String ATTVAL_BYTE                   = "byte";
    public static final String ATTVAL_COLLAPSE               = "collapse";
    public static final String ATTVAL_DATE                   = "date";
    public static final String ATTVAL_DATETIME               = "dateTime";
    public static final String ATTVAL_DAY                    = "gDay";
    public static final String ATTVAL_DECIMAL                = "decimal";
    public static final String ATTVAL_DOUBLE                 = "double";
    public static final String ATTVAL_DURATION               = "duration";
    public static final String ATTVAL_ENTITY                 = "ENTITY";
    public static final String ATTVAL_ENTITIES               = "ENTITIES";
    public static final String ATTVAL_EXTENSION              = "extension";
    public static final String ATTVAL_FALSE                  = "false";
    public static final String ATTVAL_FLOAT                  = "float";
    public static final String ATTVAL_HEXBINARY              = "hexBinary";
    public static final String ATTVAL_ID                     = "ID";
    public static final String ATTVAL_IDREF                  = "IDREF";
    public static final String ATTVAL_IDREFS                 = "IDREFS";
    public static final String ATTVAL_INT                    = "int";
    public static final String ATTVAL_INTEGER                = "integer";
    public static final String ATTVAL_INTERLEAVE             = "interleave";    
    public static final String ATTVAL_LANGUAGE               = "language";
    public static final String ATTVAL_LAX                    = "lax";
    public static final String ATTVAL_LIST                   = "list";
    public static final String ATTVAL_LONG                   = "long";
    public static final String ATTVAL_NAME                   = "Name";
    public static final String ATTVAL_NEGATIVEINTEGER        = "negativeInteger";
    public static final String ATTVAL_MONTH                  = "gMonth";
    public static final String ATTVAL_MONTHDAY               = "gMonthDay";
    public static final String ATTVAL_NCNAME                 = "NCName";
    public static final String ATTVAL_NMTOKEN                = "NMTOKEN";
    public static final String ATTVAL_NMTOKENS               = "NMTOKENS";
    public static final String ATTVAL_NONE                   = "none";    
    public static final String ATTVAL_NONNEGATIVEINTEGER     = "nonNegativeInteger";
    public static final String ATTVAL_NONPOSITIVEINTEGER     = "nonPositiveInteger";
    public static final String ATTVAL_NORMALIZEDSTRING       = "normalizedString";
    public static final String ATTVAL_NOTATION               = "NOTATION";
    public static final String ATTVAL_OPTIONAL               = "optional";
    public static final String ATTVAL_POSITIVEINTEGER        = "positiveInteger";
    public static final String ATTVAL_PRESERVE               = "preserve";
    public static final String ATTVAL_PROHIBITED             = "prohibited";
    public static final String ATTVAL_QNAME                  = "QName";
    public static final String ATTVAL_QUALIFIED              = "qualified";
    public static final String ATTVAL_REPLACE                = "replace";
    public static final String ATTVAL_REQUIRED               = "required";
    public static final String ATTVAL_RESTRICTION            = "restriction";
    public static final String ATTVAL_SHORT                  = "short";
    public static final String ATTVAL_SKIP                   = "skip";
    public static final String ATTVAL_STRICT                 = "strict";
    public static final String ATTVAL_STRING                 = "string";    
    public static final String ATTVAL_SUBSTITUTION           = "substitution";
    public static final String ATTVAL_SUFFIX                 = "suffix";
    public static final String ATTVAL_TIME                   = "time";
    public static final String ATTVAL_TOKEN                  = "token";
    public static final String ATTVAL_TRUE                   = "true";
    public static final String ATTVAL_UNBOUNDED              = "unbounded";
    public static final String ATTVAL_UNION                  = "union";
    public static final String ATTVAL_UNQUALIFIED            = "unqualified";
    public static final String ATTVAL_UNSIGNEDBYTE           = "unsignedByte";
    public static final String ATTVAL_UNSIGNEDINT            = "unsignedInt";
    public static final String ATTVAL_UNSIGNEDLONG           = "unsignedLong";
    public static final String ATTVAL_UNSIGNEDSHORT          = "unsignedShort";
    public static final String ATTVAL_YEAR                   = "gYear";
    public static final String ATTVAL_YEARMONTH              = "gYearMonth";
    public static final String ATTVAL_PRECISIONDECIMAL       = "precisionDecimal";
    public static final String ATTVAL_YEARMONTHDURATION      = "yearMonthDuration";
    public static final String ATTVAL_DAYTIMEDURATION        = "dayTimeDuration";
    public static final String ATTVAL_DATETIMESTAMP          = "dateTimeStamp";

    // form qualified/unqualified
    public static final short FORM_UNQUALIFIED = 0;
    public static final short FORM_QUALIFIED   = 1;

    // attribute use
    public static final short USE_OPTIONAL   = 0;
    public static final short USE_REQUIRED   = 1;
    public static final short USE_PROHIBITED = 2;
    
    // maxOccurs = "unbounded"
    public static final int OCCURRENCE_UNBOUNDED = -1;
    
    // a placeholder definition used for assertions error messages
    public static final String ASSERT_ERRORMSG_PLACEHOLDER_REGEX = "\\{\\$value\\}";
    
    // warning message string when an assert or, a CTA XPath expression (in full mode) contains tokens '/' or '//' 
    public static final String XS11_XPATHEXPR_COMPILE_WRN_MESG_1 = "Expression starts with / or //";

}
