/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.v1.msg;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;

/**
 * XMLMessages provides error messages for the XML 1.0 Recommendation and for 
 * the Namespaces Recommendation
 *
 */
public class XML implements XMLMessageProvider {
    /**
     * The domain of messages concerning the XML 1.0 specification.
     */
    public static final String XML_DOMAIN = "http://www.w3.org/TR/1998/REC-xml-19980210";
    public static final String XMLNS_DOMAIN = "http://www.w3.org/TR/1999/REC-xml-names-19990114";

    /**
     *
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
    /**
     *
     */
    public Locale getLocale() {
        return fLocale;
    }

    /**
     * Creates a message from the specified key and replacement
     * arguments, localized to the given locale.
     *
     * @param locale    The requested locale of the message to be
     *                  created.
     * @param key       The key for the message text.
     * @param args      The arguments to be used as replacement text
     *                  in the message created.
     */
    public String createMessage(Locale locale, int majorCode, int minorCode, Object args[]) {
        boolean throwex = false;
        if (fResourceBundle == null || locale != fLocale) {
            if (locale != null)
                fResourceBundle = ListResourceBundle.getBundle("org.apache.xerces.msg.XMLMessages", locale);
            if (fResourceBundle == null)
                fResourceBundle = ListResourceBundle.getBundle("org.apache.xerces.msg.XMLMessages");
        }
        if (majorCode < 0 || majorCode >= fgMessageKeys.length - 1) {
            majorCode = MSG_BAD_MAJORCODE;
            throwex = true;
        }
        String msgKey = fgMessageKeys[majorCode];
        String msg = fResourceBundle.getString(msgKey);
        if (args != null) {
            try {
                msg = java.text.MessageFormat.format(msg, args);
            } catch (Exception e) {
                msg = fResourceBundle.getString(fgMessageKeys[MSG_FORMAT_FAILURE]);
                msg += " " + fResourceBundle.getString(msgKey);
            }
        }
/*
        if (minorCode > 0 && minorCode < fgConstraints.length - 1) {
            Constraint c = fgConstraints[minorCode];
            String prefix = "{ " + c.sections;
            if (c.productions != null && c.productions.length() != 0)
                prefix = prefix + " " + c.productions;
            msg = prefix + " } " + msg;
        }
 */
        if (throwex) {
            throw new RuntimeException(msg);
        }
        return msg;
    }
    //
    //
    //
    private Locale fLocale = null;
    private ResourceBundle fResourceBundle = null;
    //
    //
    //
    public static final int
        MSG_BAD_MAJORCODE = 0,              //  majorCode parameter to createMessage was out of bounds
        MSG_FORMAT_FAILURE = 1,             //  exception thrown during messageFormat call
        MSG_LESSTHAN_IN_ATTVALUE = 2,       //  '<' found in attribute value
        MSG_ROOT_ELEMENT_TYPE = 3,          //  document root element type does not match the doctypedecl Name
        MSG_IDREFS_INVALID = 4,             //  attributes of type IDREFS must match the Names production
        MSG_NMTOKENS_INVALID = 5,           //  attributes of type NMTOKENS must match the Nmtokens production
        MSG_RESERVED_PITARGET = 6,          //  PITarget matching [Xx][Mm][Ll] is reserved
        MSG_SPACE_REQUIRED_IN_PI = 7,       //  white space is required between PITarget and data
        MSG_INVALID_CHAR_IN_PI = 8,         //  invalid character found in PI data
        MSG_DASH_DASH_IN_COMMENT = 9,       //  the string "--" in not allowed in comments
        MSG_INVALID_CHAR_IN_COMMENT = 10,   //  invalid character found in comment
        MSG_INVALID_CHARREF = 11,           //  invalid value for character reference
        MSG_INVALID_CHAR_IN_ATTVALUE = 12,  //  invalid character found in attribute value
        MSG_QUOTE_REQUIRED_IN_ATTVALUE = 13, //  attribute value was not a quoted string
        MSG_NAME_REQUIRED_IN_REFERENCE = 14, //  a Name did not follow the '&' in the entity reference
        MSG_SEMICOLON_REQUIRED_IN_REFERENCE = 15, //  a ';' did not follow the Name in the entity reference
        MSG_DIGIT_REQUIRED_IN_CHARREF = 16,  //  a decimal representation did not follow the "&#" in the character reference
        MSG_HEXDIGIT_REQUIRED_IN_CHARREF = 17, //  a hexadecimal representation did not follow the "&#x" in the character reference
        MSG_SEMICOLON_REQUIRED_IN_CHARREF = 18, //  the ';' delimiter was not found in the character reference
        MSG_QUOTE_REQUIRED_IN_SYSTEMID = 19, //  system identifier was not a quoted string
        MSG_INVALID_CHAR_IN_SYSTEMID = 20,  //  invalid character found in system identifier
        MSG_QUOTE_REQUIRED_IN_PUBLICID = 21, //  public identifier was not a quoted string
        MSG_INVALID_CHAR_IN_PUBLICID = 22,  //  invalid character found in public identifier
        MSG_INCLUDESECT_UNTERMINATED = 23,  //  includeSect must end with "]]>"
        MSG_IGNORESECT_UNTERMINATED = 24,   //  ignoreSect must end with "]]>"
        MSG_INVALID_CHAR_IN_IGNORESECT = 25, //  invalid character found in ignoreSect
        MSG_ELEMENT_UNTERMINATED = 26,      //  element type not followed by attributes, ">" or "/>"
        MSG_EQ_REQUIRED_IN_ATTRIBUTE = 27,   //  attribute name not followed by "="
        MSG_ATTRIBUTE_NOT_UNIQUE = 28,      //  specified attributes must be unique within the element tag
        MSG_ENCODINGDECL_REQUIRED = 29,      //  the "encoding" declaration is required in a text declaration
        MSG_VERSIONINFO_REQUIRED = 30,       //  the "version" is required in the XML declaration
        MSG_EQ_REQUIRED_IN_XMLDECL = 31,     //  the '=' character must follow \"{0}\" in the XML declaration
        MSG_EQ_REQUIRED_IN_TEXTDECL = 32,    //  the '=' character must follow \"{0}\" in the text declaration
        MSG_QUOTE_REQUIRED_IN_XMLDECL = 33,  //  quote missing in the XML declaration
        MSG_QUOTE_REQUIRED_IN_TEXTDECL = 34, //  quote missing in the text declaration
        MSG_INVALID_CHAR_IN_XMLDECL = 35,   //  invalid character found in the XML declaration
        MSG_INVALID_CHAR_IN_TEXTDECL = 36,  //  invalid character found in the text declaration
        MSG_VERSIONINFO_INVALID = 37,       //  Invalid XML version format, \"{0}\"
        MSG_VERSION_NOT_SUPPORTED = 38,     //  unsupported XML version
        MSG_SPACE_REQUIRED_IN_TEXTDECL = 39, //  white space required between version and encoding
        MSG_ENCODINGDECL_INVALID = 40,      //  invalid encoding name
        MSG_SDDECL_INVALID = 41,            //  invalid \"standalone\" attribute value. Must be "yes" or "no"
        MSG_XMLDECL_UNTERMINATED = 42,      //  "\"?>\" expected.
        MSG_TEXTDECL_UNTERMINATED = 43,     //  "\"?>\" expected.
        MSG_INVALID_CHAR_IN_INTERNAL_SUBSET = 44, //  invalid character found in the internal subset of the dtd
        MSG_INVALID_CHAR_IN_EXTERNAL_SUBSET = 45, //  invalid character found in the external subset of the dtd
        MSG_INVALID_CHAR_IN_ENTITYVALUE = 46, //  invalid character found in entity value
        MSG_MIXED_CONTENT_UNTERMINATED = 47, //  mixed content model with element types must end with ")*"
        MSG_NAME_REQUIRED_IN_PEREFERENCE = 48, //  a Name did not follow the '&' in the parameter entity reference
        MSG_SEMICOLON_REQUIRED_IN_PEREFERENCE = 49, //  a ';' did not follow the Name in the parameter entity reference
        MSG_EXTERNALID_REQUIRED = 50,        //  an external id did not begin with SYSTEM or PUBLIC
        MSG_PEREFERENCE_WITHIN_MARKUP = 51, //  a PEReference is not allowed within markup in the internal subset of the DTD
        MSG_INVALID_CHAR_IN_PROLOG = 52,    //  invalid character found in prolog
        MSG_INVALID_CHAR_IN_MISC = 53,      //  invalid character found in trailing misc
        MSG_INVALID_CHAR_IN_CDSECT = 54,    //  invalid character found in cdata section
        MSG_INVALID_CHAR_IN_CONTENT = 55,   //  invalid character found in content
        MSG_ETAG_REQUIRED = 56,              //  end of input before end of element content
        MSG_ETAG_UNTERMINATED = 57,         //  the end-tag did not end with a '>'
        MSG_ATTRIBUTE_NAME_REQUIRED_IN_ATTDEF = 58, //  attribute name expected
        MSG_ATTTYPE_REQUIRED_IN_ATTDEF = 59, //  attribute type expected
        MSG_PUBIDCHAR_ILLEGAL = 60,        //  illegal character found in public identifier
        MSG_ENCODING_NOT_SUPPORTED = 61,   //  encoding is not supported
        MSG_ENTITY_NOT_DECLARED = 62,      //  entity was not declared
        MSG_REFERENCE_TO_UNPARSED_ENTITY = 63,
        MSG_REFERENCE_TO_EXTERNAL_ENTITY = 64,
        MSG_XML_LANG_INVALID = 65,         //  xml:lang attribute value does not match LanguageID production
        MSG_CDSECT_UNTERMINATED = 66,      //  CDATA sections must end with "]]>"
        MSG_DUPLICATE_TYPE_IN_MIXED_CONTENT = 67,
        MSG_ELEMENT_ENTITY_MISMATCH = 68,
        MSG_ID_DEFAULT_TYPE_INVALID = 69,
        MSG_ENCODING_REQUIRED = 70,
        MSG_RECURSIVE_REFERENCE = 71,
        MSG_RECURSIVE_PEREFERENCE = 72,
        MSG_IMPROPER_DECLARATION_NESTING = 73, // "Parameter entity replacement text must include declarations or proper pairs of ''<'' and ''>''. (entity: \"%{0};\")"
        MSG_IMPROPER_GROUP_NESTING = 74,    // "Parameter entity replacement text must include proper pairs of parentheses in content model, \"%{0};\"."
        MSG_ID_INVALID = 75,
        MSG_ID_NOT_UNIQUE = 76,
        MSG_IDREF_INVALID = 77,
        MSG_NMTOKEN_INVALID = 78,
        MSG_ENTITY_INVALID = 79,
        MSG_ENTITIES_INVALID = 80,
        MSG_ELEMENT_WITH_ID_REQUIRED = 81,
        MSG_ATTRIBUTE_NOT_DECLARED = 82,
        MSG_ELEMENT_NOT_DECLARED = 83,
        MSG_AVAILABLE1 = 84,
        MSG_DUPLICATE_ATTDEF = 85,
        MSG_MORE_THAN_ONE_ID_ATTRIBUTE = 86,
        MSG_CONTENT_INVALID = 87,
        MSG_CONTENT_INCOMPLETE = 88,
        MSG_ELEMENT_ALREADY_DECLARED = 89,
        MSG_ATTRIBUTE_VALUE_NOT_IN_LIST = 90,
        MSG_AVAILABLE2 = 91,
        MSG_UNDECLARED_ELEMENT_IN_CONTENTSPEC = 92,
        MSG_FIXED_ATTVALUE_INVALID = 93,
        MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED = 94,
        MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED = 95,
        MSG_AVAILABLE3 = 96,
        MSG_AVAILABLE4 = 97,
        MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN = 98,
        MSG_AVAILABLE5 = 99,
        MSG_SYSTEMID_UNTERMINATED = 100,
        MSG_PUBLICID_UNTERMINATED = 101,
        MSG_EXTERNAL_ENTITY_NOT_PERMITTED = 102,
        MSG_AVAILABLE6 = 103,
        MSG_XMLDECL_MUST_BE_FIRST = 104,
        MSG_TEXTDECL_MUST_BE_FIRST = 105,
        MSG_ELEMENTDECL_UNTERMINATED = 106,
        MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL = 107,
        MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL = 108,
        MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL = 109,
        MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL = 110,
        MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL = 111,
        MSG_ENTITYDECL_UNTERMINATED = 112,
        MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL = 113,
        MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL = 114,
        MSG_NAME_REQUIRED_IN_NOTATIONTYPE = 115,
        MSG_NMTOKEN_REQUIRED_IN_ENUMERATION = 116,
        MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE = 117,
        MSG_NOTATIONTYPE_UNTERMINATED = 118,
        MSG_ENUMERATION_UNTERMINATED = 119,
        MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL = 120,
        MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE = 121,
        MSG_NOTATIONDECL_UNTERMINATED = 122,
        MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE = 123,
        MSG_CDEND_IN_CONTENT = 124,
        MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL = 125,
        MSG_TWO_COLONS_IN_QNAME = 126,
        MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT = 127,
        MSG_MARKUP_NOT_RECOGNIZED_IN_MISC = 128,
        MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG = 129,
        MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE = 130,
        MSG_PITARGET_REQUIRED = 131,
        MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE = 132,
        MSG_URI_FRAGMENT_IN_SYSTEMID = 133,
        MSG_ROOT_ELEMENT_REQUIRED = 134,
        MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL = 135,
        MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE = 136,
        MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL = 137,
        MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF = 138,
        MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF = 139,
        MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF = 140,
        MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL = 141,
        MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL = 142,
        MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE = 143,
        MSG_XML_SPACE_DECLARATION_ILLEGAL = 144,
        MSG_CLOSE_PAREN_REQUIRED_IN_MIXED = 145,
        MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL = 146,
        MSG_DOCTYPEDECL_UNTERMINATED = 147,
        MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL = 148,
        MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT = 149,
        MSG_MARKUP_NOT_RECOGNIZED_IN_DTD = 150,
        MSG_ATTRIBUTE_VALUE_UNTERMINATED = 151,
        MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN = 152,
        MSG_ROOT_ELEMENT_TYPE_REQUIRED = 153,
        MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID = 154,
        MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL = 155,
        MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL = 156,
        MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL = 157,
        MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID = 158,
        MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL = 159,
        MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID = 160,
        MSG_REFERENCE_NOT_IN_ONE_ENTITY = 161,
        MSG_COMMENT_NOT_IN_ONE_ENTITY = 162,
        MSG_COMMENT_UNTERMINATED = 163,
        MSG_PI_UNTERMINATED = 164,
        MSG_PI_NOT_IN_ONE_ENTITY = 165,
        MSG_REFERENCE_UNTERMINATED = 166,
        MSG_PREFIX_DECLARED = 167,
        MSG_ATT_DEFAULT_INVALID = 168,
        MSG_GENERIC_SCHEMA_ERROR = 169,
        MSG_DTD_SCHEMA_ERROR = 170,

        // ...
        MSG_MAX_CODE = 200;

    private static final String[] fgMessageKeys = {
        "BadMajorCode",                 //   0, "The majorCode parameter to createMessage was out of bounds."
        "FormatFailed",                 //   1, "An internal error occurred while formatting the following message:"
        "LessthanInAttValue",           //   2, "The attribute value must not contain the '<' character."
        "RootElementTypeMustMatchDoctypedecl", //   3, "The document root element type \"{1}\" must match the document type declaration name \"{0}\"."
        "IDREFSInvalid",                //   4, "Attribute value \"{1}\" of type IDREFS must be one or more names."
        "NMTOKENSInvalid",              //   5, "Attribute value \"{1}\" of type NMTOKENS must contain one or more name tokens."
        "ReservedPITarget",             //   6, "The processing instruction target matching \"[xX][mM][lL]\" is not allowed."
        "SpaceRequiredInPI",            //   7, "White space is required between the processing instruction target and data."
        "InvalidCharInPI",              //   8, "An invalid XML character (Unicode: 0x{0}) was found in the processing instruction."
        "DashDashInComment",            //   9, "The string \"--\" is not permitted within comments."
        "InvalidCharInComment",         //  10, "An invalid XML character (Unicode: 0x{0}) was found in the comment."
        "InvalidCharRef",               //  11, "Character reference \"&#{0}\" is an invalid XML character."
        "InvalidCharInAttValue",        //  12, "An invalid XML character (Unicode: 0x{0}) was found in the attribute value."
        "QuoteRequiredInAttValue",      //  13, "The attribute value must begin with either a single or double quote character."
        "NameRequiredInReference",      //  14, "The entity name must immediately follow the '&' in the entity reference."
        "SemicolonRequiredInReference", //  15, "The entity reference must end with the ';' delimiter."
        "DigitRequiredInCharRef",       //  16, "A decimal representation must immediately follow the \"&#\" in the character reference."
        "HexdigitRequiredInCharRef",    //  17, "A hexadecimal representation must immediately follow the \"&#x\" in the character reference."
        "SemicolonRequiredInCharRef",   //  18, "The character reference must end with the ';' delimiter."
        "QuoteRequiredInSystemID",      //  19, "The system identifier must begin with either a single or double quote character."
        "InvalidCharInSystemID",        //  20, "An invalid XML character (Unicode: 0x{0}) was found in the system identifier."
        "QuoteRequiredInPublicID",      //  21, "The public identifier must begin with either a single or double quote character."
        "InvalidCharInPublicID",        //  22, "An invalid XML character (Unicode: 0x{0}) was found in the public identifier."
        "IncludeSectUnterminated",      //  23, "The included conditional section must end with \"]]>\"."
        "IgnoreSectUnterminated",       //  24, "The excluded conditional section must end with \"]]>\"."
        "InvalidCharInIgnoreSect",      //  25, "An invalid XML character (Unicode: 0x{0}) was found in the excluded conditional section."
        "ElementUnterminated",          //  26, "Element type \"{0}\" must be followed by either attribute specifications, \">\" or \"/>\"."
        "EqRequiredInAttribute",        //  27, "Attribute name \"{1}\" must be followed by the '=' character."
        "AttributeNotUnique",           //  28, "Attribute \"{1}\" was already specified for element \"{0}\"."
        "EncodingDeclRequired",         //  29, "The encoding declaration is required within a text declaration."
        "VersionInfoRequired",          //  30, "The version is required within an XML declaration."
        "EqRequiredInXMLDecl",          //  31, "The '=' character must follow \"{0}\" in the XML declaration."
        "EqRequiredInTextDecl",         //  32, "The '=' character must follow \"{0}\" in the text declaration."
        "QuoteRequiredInXMLDecl",       //  33, "The value following \"{0}\" in an XML declaration must be a quoted string."
        "QuoteRequiredInTextDecl",      //  34, "The value following \"{0}\" in a text declaration must be a quoted string."
        "InvalidCharInXMLDecl",         //  35, "An invalid XML character (Unicode: 0x{0}) was found in the XML declaration."
        "InvalidCharInTextDecl",        //  36, "An invalid XML character (Unicode: 0x{0}) was found in the text declaration."
        "VersionInfoInvalid",           //  37, "Invalid version \"{0}\"."
        "VersionNotSupported",          //  38, "XML version \"{0}\" is not supported."
        "SpaceRequiredInTextDecl",      //  39, "White space is required between the version and the encoding declaration."
        "EncodingDeclInvalid",          //  40, "Invalid encoding name \"{0}\"."
        "SDDeclInvalid",                //  41, "The standalone document declaration value must be \"yes\" or \"no\", not \"{0}\"."
        "XMLDeclUnterminated",          //  42, "The XML declaration must end with \"?>\"."
        "TextDeclUnterminated",         //  43, "The text declaration must end with \"?>\"."
        "InvalidCharInInternalSubset",  //  44, "An invalid XML character (Unicode: 0x{0}) was found in the internal subset of the DTD."
        "InvalidCharInExternalSubset",  //  45, "An invalid XML character (Unicode: 0x{0}) was found in the external subset of the DTD."
        "InvalidCharInEntityValue",     //  46, "An invalid XML character (Unicode: 0x{0}) was found in the literal entity value."
        "MixedContentUnterminated",     //  47, "The mixed content model \"(0}\" must end with \")*\"."
        "NameRequiredInPEReference",    //  48, "The entity name must immediately follow the '%' in the parameter entity reference."
        "SemicolonRequiredInPEReference", //  49, "The parameter entity reference must end with the ';' delimiter."
        "ExternalIDRequired",           //  50, "The external entity declaration must begin with either \"SYSTEM\" or \"PUBLIC\"."
        "PEReferenceWithinMarkup",      //  51, "The parameter entity reference \"%(0);\" cannot occur within markup in the internal subset of the DTD."
        "InvalidCharInProlog",          //  52, "An invalid XML character (Unicode: 0x{0}) was found in the prolog of the document."
        "InvalidCharInMisc",            //  53, "An invalid XML character (Unicode: 0x{0}) was found in markup after the end of the element content."
        "InvalidCharInCDSect",          //  54, "An invalid XML character (Unicode: 0x{0}) was found in the CDATA section."
        "InvalidCharInContent",         //  55, "An invalid XML character (Unicode: 0x{0}) was found in the element content of the document."
        "ETagRequired",                 //  56, "The element type \"{0}\" must be terminated by the matching end-tag \"</{0}>\"."
        "ETagUnterminated",             //  57, "The end-tag for element type \"{0}\" must end with a ''>'' delimiter."
        "AttNameRequiredInAttDef",      //  58, "The attribute name must be specified in the attribute-list declaration for element \"{0}\"."
        "AttTypeRequiredInAttDef",      //  59, "The attribute type must be specified in the attribute-list declaration of attribute \"{1}\" for element \"{0}\"."
        "PubidCharIllegal",             //  60, "The character (Unicode: 0x{0}) is not permitted in the public identifier."
        "EncodingNotSupported",         //  61, "The encoding \"{0}\" is not supported."
        "EntityNotDeclared",            //  62, "The entity \"{0}\" was referenced, but not declared."
        "ReferenceToUnparsedEntity",    //  63, "The unparsed entity reference \"&{0};\" is not permitted."
        "ReferenceToExternalEntity",    //  64, "The external entity reference \"&{0};\" is not permitted in an attribute value."
        "XMLLangInvalid",               //  65, "The xml:lang attribute value \"{0}\" is an invalid language identifier."
        "CDSectUnterminated",           //  66, "The CDATA section must end with \"]]>\"."
        "DuplicateTypeInMixedContent",  //  67, "The element type \"{0}\" was already specified in this content model."
        "ElementEntityMismatch",        //  68, "The element \"{0}\" must start and end within the same entity."
        "IDDefaultTypeInvalid",         //  69, "The ID attribute \"{0}\" must have a declared default of \"#IMPLIED\" or \"#REQUIRED\"."
        "EncodingRequired",             //  70, "A parsed entity not encoded in either UTF-8 or UTF-16 must contain an encoding declaration."
        "RecursiveReference",           //  71, "Recursive reference \"&{0};\". (Reference path: {1})"
        "RecursivePEReference",         //  72, "Recursive reference \"%{0};\". (Reference path: {1})"
        "ImproperDeclarationNesting",   //  73, "The replacement text of parameter entity \"(0)\" must include properly nested declarations."
        "ImproperGroupNesting",         //  74, "The replacement text of parameter entity \"(0)\" must include properly nested pairs of parentheses in content model."
        "IDInvalid",                    //  75, "Attribute value \"{1}\" of type ID must be a name."
        "IDNotUnique",                  //  76, "Attribute value \"(1}\" of type ID must be unique within the document."
        "IDREFInvalid",                 //  77, "Attribute value \"(1}\" of type IDREF must be a name."
        "NMTOKENInvalid",               //  78, "Attribute value \"(1}\" of type NMTOKEN must be a name token."
        "ENTITYInvalid",                //  79, "Attribute value \"(1}\" of type ENTITY must be the name of an unparsed entity."
        "ENTITIESInvalid",              //  80, "Attribute value \"(1}\" of type ENTITIES must be the names of one or more unparsed entities."
        "MSG_ELEMENT_WITH_ID_REQUIRED", //  81, "MSG_ELEMENT_WITH_ID_REQUIRED"
        "MSG_ATTRIBUTE_NOT_DECLARED",   //  82, "MSG_ATTRIBUTE_NOT_DECLARED"
        "MSG_ELEMENT_NOT_DECLARED",     //  83, "MSG_ELEMENT_NOT_DECLARED"
        "MSG_AVAILABLE1",               //  84,
        "MSG_DUPLICATE_ATTDEF",         //  85, "MSG_DUPLICATE_ATTDEF"
        "MSG_MORE_THAN_ONE_ID_ATTRIBUTE", //  86, "MSG_MORE_THAN_ONE_ID_ATTRIBUTE"
        "MSG_CONTENT_INVALID",          //  87, "MSG_CONTENT_INVALID"
        "MSG_CONTENT_INCOMPLETE",       //  88, "MSG_CONTENT_INCOMPLETE"
        "MSG_ELEMENT_ALREADY_DECLARED", //  89, "MSG_ELEMENT_ALREADY_DECLARED"
        "MSG_ATTRIBUTE_VALUE_NOT_IN_LIST", //  90, "MSG_ATTRIBUTE_VALUE_NOT_IN_LIST"
        "MSG_AVAILABLE2",               //  91,
        "UndeclaredElementInContentSpec", //   92, "The content model of element \"{0}\" refers to the undeclared element \"{1}\"."
        "MSG_FIXED_ATTVALUE_INVALID",   //  93, "MSG_FIXED_ATTVALUE_INVALID"
        "MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED", //  94, "MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED"
        "MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED", //  95, "MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED"
        "MSG_AVAILABLE3",               //  96,
        "MSG_AVAILABLE4",               //  97,
        "MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", //  98, "MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN"
        "MSG_AVAILABLE5",               //  99,
        "SystemIDUnterminated",         // 100, "MSG_SYSTEMID_UNTERMINATED"
        "PublicIDUnterminated",         // 101, "MSG_PUBLICID_UNTERMINATED"
        "MSG_EXTERNAL_ENTITY_NOT_PERMITTED", // 102, "MSG_EXTERNAL_ENTITY_NOT_PERMITTED"
        "MSG_AVAILABLE6",               // 103,
        "XMLDeclMustBeFirst",           // 104, "MSG_XMLDECL_MUST_BE_FIRST"
        "TextDeclMustBeFirst",          // 105, "MSG_TEXTDECL_MUST_BE_FIRST"
        "ElementDeclUnterminated",      // 106, "MSG_ELEMENTDECL_UNTERMINATED"
        "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL",     // 107, "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL"
        "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL",     // 108, "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL"
        "MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL",     // 109, "MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL"
        "MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL",     // 110, "MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL"
        "MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL",     // 111, "MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL"
        "EntityDeclUnterminated",       // 112, "MSG_ENTITYDECL_UNTERMINATED"
        "MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL",     // 113, "MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL"
        "MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL",     // 114, "MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL"
        "MSG_NAME_REQUIRED_IN_NOTATIONTYPE",     // 115, "MSG_NAME_REQUIRED_IN_NOTATIONTYPE"
        "MSG_NMTOKEN_REQUIRED_IN_ENUMERATION",     // 116, "MSG_NMTOKEN_REQUIRED_IN_ENUMERATION"
        "MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE",     // 117, "MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE"
        "NotationTypeUnterminated",     // 118, "MSG_NOTATIONTYPE_UNTERMINATED"
        "EnumerationUnterminated",      // 119, "MSG_ENUMERATION_UNTERMINATED"
        "MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL", // 120, "MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL"
        "MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE", // 121, "MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE"
        "NotationDeclUnterminated",     // 122, "MSG_NOTATIONDECL_UNTERMINATED"
        "MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE",     // 123, "MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE"
        "CDEndInContent",               // 124, "MSG_CDEND_IN_CONTENT"
        "MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL",     // 125, "MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL"
        "TwoColonsInQName",             // 126, ""
        "MarkupNotRecognizedInContent", // 127, "MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT"
        "MarkupNotRecognizedInMisc",    // 128, "MSG_MARKUP_NOT_RECOGNIZED_IN_MISC"
        "MarkupNotRecognizedInProlog",  // 129, "MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG"
        "MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE",     // 130, "MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE"
        "PITargetRequired",             // 131, "MSG_PITARGET_REQUIRED"
        "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE",     // 132, "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE"
        "MSG_URI_FRAGMENT_IN_SYSTEMID", // 133, "MSG_URI_FRAGMENT_IN_SYSTEMID"
        "RootElementRequired",          // 134, "MSG_ROOT_ELEMENT_REQUIRED"
        "MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL",     // 135, "MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL"
        "MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE",     // 136, "MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE"
        "MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL",     // 137, "MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL"
        "MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF",     // 138, "MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF"
        "MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF",     // 139, "MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF"
        "MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF",     // 140, "MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF"
        "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL",     // 141, "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL"
        "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL",     // 142, "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL"
        "MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE",     // 143, "MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE"
        "MSG_XML_SPACE_DECLARATION_ILLEGAL",     // 144, "MSG_XML_SPACE_DECLARATION_ILLEGAL"
        "MSG_CLOSE_PAREN_REQUIRED_IN_MIXED",     // 145, "MSG_CLOSE_PAREN_REQUIRED_IN_MIXED"
        "MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL",     // 146, "MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL"
        "DoctypedeclUnterminated", // 147, "MSG_DOCTYPEDECL_UNTERMINATED"
        "MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL",     // 148, "MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL"
        "MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT",     // 149, "MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT"
        "MSG_MARKUP_NOT_RECOGNIZED_IN_DTD",     // 150, "MSG_MARKUP_NOT_RECOGNIZED_IN_DTD"
        "AttributeValueUnterminated",   // 151, "MSG_ATTRIBUTE_VALUE_UNTERMINATED"
        "MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN",     // 152, "MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN"
        "MSG_ROOT_ELEMENT_TYPE_REQUIRED",     // 153, "MSG_ROOT_ELEMENT_TYPE_REQUIRED"
        "MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID",     // 154, "MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID"
        "MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL",     // 155, "MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL"
        "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL",     // 156, "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL"
        "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL",     // 157, "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL"
        "MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID",     // 158, "MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID"
        "MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL",     // 159, "MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL"
        "MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID",     // 160, "MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID"
        "ReferenceNotInOneEntity",      // 161, "MSG_REFERENCE_NOT_IN_ONE_ENTITY"
        "CommentNotInOneEntity",        // 162, "MSG_COMMENT_MUST_BEGIN_AND_END_IN_THE_SAME_ENTITY"
        "CommentUnterminated",          // 163, "MSG_COMMENT_UNTERMINATED"
        "PIUnterminated",               // 164, "MSG_PI_UNTERMINATED"
        "PINotInOneEntity",             // 165, "MSG_PI_MUST_BEGIN_AND_END_IN_THE_SAME_ENTITY"
        "ReferenceUnterminated",        // 166, "MSG_REFERENCE_UNTERMINATED"
        "PrefixDeclared",               // 167, "MSG_PREFIX_DECLARED"
        "MSG_ATT_DEFAULT_INVALID",      // 168, "MSG_ATT_DEFAULT_INVALID"
        "MSG_GENERIC_SCHEMA_ERROR",     // 169  "MSG_GENERIC_SCHEMA_ERROR"
        "MSG_DTD_SCHEMA_ERROR",         // 170
        // ...
        ""                              //
    };

    public static final int
        VC_ROOT_ELEMENT_TYPE = 1,           // 2.8 VC: Root Element Type
        VC_IDREF = 2,                       // 3.3.1 VC: IDREF
        VC_NAME_TOKEN = 3,                  // 3.3.1 VC: Name Token
        P17_RESERVED_PITARGET = 4,          // 2.6 [17] PITarget matching [Xx][Mm][Ll] is reserved
        P16_WHITESPACE_REQUIRED = 5,        // 2.6 [16] White space required between PITarget and data
        P16_INVALID_CHARACTER = 6,          // 2.6 [16] Invalid character in PI data
        P15_DASH_DASH = 7,                  // 2.5 [15] The string "--" must not occur within comments
        P15_INVALID_CHARACTER = 8,          // 2.5 [15] Invalid character in comment
        WFC_LEGAL_CHARACTER = 9,            // 4.1 [66] WFC: Legal Character
        P10_INVALID_CHARACTER = 10,         // 2.3 [10] Invalid character in AttValue
        WFC_NO_LESSTHAN_IN_ATTVALUE = 11,   // 3.1 [41] WFC: No < in Attribute Values
        P10_QUOTE_REQUIRED = 12,             // 2.3 [10] No quote delimiter in AttValue
        P68_NAME_REQUIRED = 13,              // 4.1 [68] Name missing in EntityRef
        P68_SEMICOLON_REQUIRED = 14,         // 4.1 [68] An EntityRef must end with a ';'
        P66_DIGIT_REQUIRED = 15,             // 4.1 [66] A "&#" CharRef must contain at least one decimal digit
        P66_HEXDIGIT_REQUIRED = 16,          // 4.1 [66] A "&#x" CharRef must contain at least one hexadecimal digit
        P66_SEMICOLON_REQUIRED = 17,         // 4.1 [66] A CharRef must end with a ';'
        P11_QUOTE_REQUIRED = 18,             // 2.3 [11] No quote delimiter in SystemLiteral
        P11_INVALID_CHARACTER = 19,         // 2.3 [11] Invalid character in SystemLiteral
        P12_QUOTE_REQUIRED = 20,             // 2.3 [12] No quote delimiter in PubidLiteral
        P12_INVALID_CHARACTER = 21,         // 2.3 [12] Invalid character in PubidLiteral
        P62_UNTERMINATED = 22,              // 3.4 [62] Included conditional sections must end with "]]>"
        P63_UNTERMINATED = 23,              // 3.4 [63] Excluded conditional sections must end with "]]>"
        P65_INVALID_CHARACTER = 24,         // 3.4 [64] Invalid character in excluded conditional section
        P40_UNTERMINATED = 25,              // 3.1 [40] Element type must be followed by attributes, ">" or "/>"
        P41_EQ_REQUIRED = 26,                // 3.1 [41] Attribute name must be followed by an '=' character
        WFC_UNIQUE_ATT_SPEC = 27,           // 3.1 [40] [44] Attribute must only appear once
        P77_ENCODINGDECL_REQUIRED = 28,      // 4.3.1 [77] The encoding declaration is not optional
        P23_VERSIONINFO_REQUIRED = 29,       // 2.8 [23] The version is not optional
        P24_EQ_REQUIRED = 30,                // 2.8 [24] An '=' is required after the version
        P32_EQ_REQUIRED = 31,                // 2.9 [32] An '=' is required in a standalone document declaration
        P80_EQ_REQUIRED = 32,                // 4.3.3 [80] An '=' is required in an encoding declaration
        P24_QUOTE_REQUIRED = 33,             // 2.8 [24] The version is a quoted string
        P32_QUOTE_REQUIRED = 34,             // 2.9 [32] The standalone document declaration value is a quoted string
        P80_QUOTE_REQUIRED = 35,             // 4.3.3 [80] The encoding name value is a quoted string
        P26_INVALID_CHARACTER = 36,         // 2.8 [26] The version contained an invalid XML character
        P32_INVALID_CHARACTER = 37,         // 2.9 [32] The standalone document declaration value contained an invalid XML character
        P81_INVALID_CHARACTER = 38,         // 4.3.3 [81] The encoding name value contained an invalid XML character
        P26_INVALID_VALUE = 39,             // 2.8 [26] The version was not in the correct format
        P26_NOT_SUPPORTED = 40,             // 2.8 [26] The version specified is not supported
        P80_WHITESPACE_REQUIRED = 41,       // 4.3.3 [80] Whitespace required between version and encoding
        P81_INVALID_VALUE = 42,             // 4.3.3 [81] The encoding name value was not in the correct format
        P32_INVALID_VALUE = 43,             // 2.9 [32] The standalone document declaration value was not "yes" or "no"
        P23_UNTERMINATED = 44,              // 2.8 [23] An XML declaration must end with "?>"
        P77_UNTERMINATED = 45,              // 4.3.1 [77] A text declaration must end with "?>"
        P28_INVALID_CHARACTER = 46,         // 2.8 [28] Invalid character in the internal subset of the DTD
        P30_INVALID_CHARACTER = 47,         // 2.8 [30] Invalid character in the external subset or an external entity within DTD
        P9_INVALID_CHARACTER = 48,          // 2.3 [9] Invalid character in EntityValue
        P51_UNTERMINATED = 49,              // 3.2.2 [51] Mixed content model with element types must end with ")*"
        P69_NAME_REQUIRED = 50,              // 4.1 [69] Name missing in PEReference
        P69_SEMICOLON_REQUIRED = 51,         // 4.1 [69] An PEReference must end with a ';'
        P75_INVALID = 52,                   // 4.2.2 [75] An ExternalId must begin with either "SYSTEM" or "PUBLIC"
        WFC_PES_IN_INTERNAL_SUBSET = 53,    // 2.8 [28] PEReferences in the internal subset cannot occur within markup declarations
        P22_INVALID_CHARACTER = 54,         // 2.8 [22] Invalid character in prolog
        P27_INVALID_CHARACTER = 55,         // 2.8 [27] Invalid character in Misc
        P20_INVALID_CHARACTER = 56,         // 2.7 [20] Invalid character in CDSect
        P43_INVALID_CHARACTER = 57,         // 3.1 [43] Invalid character in content
        P39_UNTERMINATED = 58,              // 3 [39] Element type must be followed by attributes, ">" or "/>"
        P42_UNTERMINATED = 59,              // 3.1 [42] end-tag must end with '>'
        P81_NOT_SUPPORTED = 60,             // 4.3.3 [81] The encoding is not supported
        WFC_ENTITY_DECLARED = 61,           // 4.1 [68] The entity was referenced, but not declared
        VC_ENTITY_DECLARED = 62,            // 4.1 [68] The entity was referenced, but not declared
        WFC_PARSED_ENTITY = 63,             // 4.1 [68] An unparsed entity was referenced
        WFC_NO_EXTERNAL_ENTITY_REFERENCES = 64, // 3.1 [42] reference to external entity in AttValue
        P33_INVALID = 65,                   // 2.12 [33] xml:lang attribute value must match LanguageID production
        P18_UNTERMINATED = 66,              // 2.7 [18] CDATA sections must end with "]]>"
        VC_NO_DUPLICATE_TYPES = 67,         // 3.2.2 [51] The same type must not appear more than once in a mixed content declaration
        P78_NOT_WELLFORMED = 68,            // 4.3.2 [78] 
        VC_ID_ATTRIBUTE_DEFAULT = 69,       // 3.3.1 [54] 
        P53_NAME_REQUIRED = 70,              // 3.3 [53] 
        P53_ATTTYPE_REQUIRED = 71,           // 3.3 [53] 
        P81_REQUIRED = 72,                  // 4.3.3 [81] 
        WFC_NO_RECURSION = 73,              // 4.1 [68] 
        VC_PROPER_DECLARATION_PE_NESTING = 74, // 2.8 [29] 
        VC_PROPER_GROUP_PE_NESTING = 75,    // 3.2.1 [47] 
        VC_ID = 76,                         // 3.3.1 [56] 
        VC_ENTITY_NAME = 77,                // 3.3.1 [56] 
        VC_ATTRIBUTE_VALUE_TYPE = 78,       // 3.1 [41] 
        VC_ELEMENT_VALID = 79,              // 3 [39] 
        VC_STANDALONE_DOCUMENT_DECLARATION = 80, // 2.9 [32] 
        VC_ONE_ID_PER_ELEMENT_TYPE = 81,    // 3.3.1 [56] 
        VC_UNIQUE_ELEMENT_TYPE_DECLARATION = 82, // 3.2 [45] 
        P45_UNDECLARED_ELEMENT_IN_CONTENTSPEC = 83, // 3.2 [45] 

        VC_NOTATION_ATTRIBUTES = 84,
        P53_DUPLICATE = 85,
        VC_ENUMERATION = 86,
        VC_FIXED_ATTRIBUTE_DEFAULT = 87,
        VC_REQUIRED_ATTRIBUTE = 88,
        VC_NOTATION_DECLARED = 89,
        P58_NAME_REQUIRED = 90,
        P58_UNTERMINATED = 91,
        P59_NMTOKEN_REQUIRED = 92,
        P59_UNTERMINATED = 93,
        P70_SPACE = 94,
        P70_REQUIRED_NAME = 95,
        P70_REQUIRED_SPACE = 96,
        P71_UNTERMINATED = 97,
        P72_SPACE = 98,
        P72_UNTERMINATED = 99,
        P76_REQUIRED = 100,
        P82_NAME_REQUIRED = 101,
        P82_SPACE_REQUIRED = 102,
        P82_UNTERMINATED = 103,
        P14_INVALID = 104,
        P16_PITARGET_REQUIRED = 105,
        P16_REQUIRED = 106,
        P1_ELEMENT_REQUIRED = 107,
        P22_NOT_RECOGNIZED = 108,
        P27_NOT_RECOGNIZED = 109,
        P43_NOT_RECOGNIZED = 110,
        P52_ELEMENT_TYPE_REQUIRED = 111,
        P52_SPACE_REQUIRED = 112,
        P53_SPACE_REQUIRED = 113,
        P58_OPEN_PAREN_REQUIRED = 114,
        P58_SPACE_REQUIRED = 115,
        P60_SPACE_REQUIRED = 116,
        S2_10_DECLARATION_ILLEGAL = 117,
        P39_ELEMENT_TYPE_REQUIRED = 118,
        P28_ROOT_ELEMENT_TYPE_REQUIRED = 119,
        P28_SPACE_REQUIRED = 120,
        P28_UNTERMINATED = 121,
        P29_NOT_RECOGNIZED = 122,
        P45_CONTENTSPEC_REQUIRED = 123,
        P45_ELEMENT_TYPE_REQUIRED = 124,
        P45_SPACE_REQUIRED = 125,
        P45_UNTERMINATED = 126,
        P47_CLOSE_PAREN_REQUIRED = 127,
        P47_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED = 128,
        P51_CLOSE_PAREN_REQUIRED = 129,
        P51_ELEMENT_TYPE_REQUIRED = 130,
        P75_SPACE_REQUIRED = 131,
        P76_SPACE_REQUIRED = 132,
        P15_UNTERMINATED = 133,
        P16_UNTERMINATED = 134,
        P67_UNTERMINATED = 135,
        P10_UNTERMINATED = 136,
        P22_XMLDECL_MUST_BE_FIRST = 137,
        P30_TEXTDECL_MUST_BE_FIRST = 138,
        P5_INVALID_CHARACTER = 139,
        P11_UNTERMINATED = 140,
        P12_UNTERMINATED = 141,
        P11_URI_FRAGMENT = 142,
        VC_ONE_NOTATION_PER_ELEMENT_TYPE = 143,
        NC_PREFIX_DECLARED = 144,
        VC_ATTRIBUTE_DEFAULT_LEGAL = 145,
        SCHEMA_GENERIC_ERROR = 146,
        
        // ...
        CONSTRAINT_MAX_CODE = 200;

/*
    private static final Constraint[] fgConstraints = {
        new Constraint(null, null, null, null),
    // VC_ROOT_ELEMENT_TYPE = 1
        new Constraint("2.8", "", "2.8 VC: Root Element Type",
                       "Validity Constraint: Root Element Type\n" +
                       "The Name in the document type declaration must match the element type of the\n" +
                       "root element."),
    // VC_IDREF = 2
        new Constraint("3.3.1 2.3", "[56] [6]", "3.3.1 VC: IDREF",
                       "Validity Constraint: IDREF\n" +
                       "[56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'\n" +
                       "Values of type IDREF must match the Name production, and values of type\n" +
                       "IDREFS must match Names; each Name must match the value of an ID attribute\n" +
                       "on some element in the XML document; i.e. IDREF values must match the value\n" +
                       "of some ID attribute."),
    // VC_NAME_TOKEN = 3
        new Constraint("3.3.1 2.3", "[56] [8]", "3.3.1 VC: Name Token",
                       "Validity Constraint: Name Token\n" +
                       "Values of type NMTOKEN must match the Nmtoken production; values of type\n" +
                       "NMTOKENS must match Nmtokens."),
    // P17_RESERVED_PITARGET = 4
        new Constraint("2.6", "[17]", "2.6 [17] PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))",
                       "[17] PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))\n" +
                       "The target names \"XML\", \"xml\", and so on are reserved for standardization\n" +
                       "in this or future versions of this specification."),
    // P16_WHITESPACE_REQUIRED = 5
        new Constraint("2.6 2.3", "[16] [3]", "2.6 [16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'",
                       "[16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'\n" +
                       "[3] S ::= (#x20 | #x9 | #xD | #xA)+\n" +
                       "White space is required between the PITarget and any additional characters\n" +
                       "that are to be passed through to the application."),
    // P16_INVALID_CHARACTER = 6
        new Constraint("2.6 2.2", "[16] [2]", "2.6 [2] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'",
                       "[16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'\n" +
                       "[2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]\n" +
                       "Processing instruction data is required to contain legal XML characters."),
    // P15_DASH_DASH = 7
        new Constraint("2.5", "[15]", "2.5 [15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'",
                       "[15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'\n" +
                       "For compatibility, the string \"--\" (double-hyphen) must not occur within comments."),
    // P15_INVALID_CHARACTER = 8
        new Constraint("2.5 2.2", "[15] [2]", "2.5 [2] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'",
                       "[15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'\n" +
                       "[2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]\n" +
                       "Comments are required to contain legal XML characters."),
    // WFC_LEGAL_CHARACTER = 9
        new Constraint("4.1", "[66] [2]", "4.1 WFC: Legal Character",
                       "Well-Formedness Constraint: Legal Character\n" +
                       "[66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'\n" +
                       "[2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]\n" +
                       "Characters referred to using character references must match the production\n" +
                       "for Char."),
    // P10_INVALID_CHARACTER = 10
        new Constraint("2.3", "[10]", "2.3 [10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"",
                       "[10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"\n" +
                       "Attribute values are required to contain legal XML characters."),
    // WFC_NO_LESSTHAN_IN_ATTVALUE = 11
        new Constraint("3.1 2.3", "[41] [10]", "3.1 WFC: No < in Attribute Values",
                       "Well-Formedness Constraint: No < in Attribute Values\n" +
                       "[41] Attribute ::= Name Eq AttValue\n" +
                       "[10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"\n" +
                       "The replacement text of any entity referred to directly or indirectly in an\n" +
                       "attribute value (other than \"&lt;\") must not contain a <."),
    // P10_QUOTE_REQUIRED = 12
        new Constraint("2.3", "[10]", "2.3 [10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"",
                       "[10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"\n" +
                       "Attribute values are specified using quoted strings."),
    // P68_NAME_REQUIRED = 13
        new Constraint("4.1", "[68]", "4.1 [68] EntityRef ::= '&' Name ';'",
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "The '&' delimiter must be followed by a valid Name in an entity reference."),
    // P68_SEMICOLON_REQUIRED = 14
        new Constraint("4.1", "[68]", "4.1 [68] EntityRef ::= '&' Name ';'",
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "An entity reference must end with a ';' delimiter."),
    // P66_DIGIT_REQUIRED = 15
        new Constraint("4.1", "[66]", "4.1 [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'",
                       "[66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'\n" +
                       "If the character reference begins with \"&#\", not \"&#x\", the digits up\n" +
                       "to the terminating ; provide a decimal representation of the character's\n" +
                       "code point in ISO/IEC 10646."),
    // P66_HEXDIGIT_REQUIRED = 16
        new Constraint("4.1", "[66]", "4.1 [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'",
                       "[66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'\n" +
                       "If the character reference begins with \"&#x\", the digits and letters up to\n" +
                       "the terminating ; provide a hexadecimal representation of the character's\n" +
                       "code point in ISO/IEC 10646."),
    // P66_SEMICOLON_REQUIRED = 17
        new Constraint("4.1", "[66]", "4.1 [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'",
                       "[66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'\n" +
                       "A character reference must end with a ';' delimiter."),
    // P11_QUOTE_REQUIRED = 18
        new Constraint("2.3", "[11]", "2.3 [11] SystemLiteral ::= ('\"' [^\"]* '\"') | (\"'\" [^']* \"'\")",
                       "[11] SystemLiteral ::= ('\"' [^\"]* '\"') | (\"'\" [^']* \"'\")\n" +
                       "System identifiers are specified using quoted strings."),
    // P11_INVALID_CHARACTER = 19
        new Constraint("2.3", "[11]", "2.3 [11] SystemLiteral ::= ('\"' [^\"]* '\"') | (\"'\" [^']* \"'\")",
                       "[11] SystemLiteral ::= ('\"' [^\"]* '\"') | (\"'\" [^']* \"'\")\n" +
                       "System identifiers are required to contain legal XML characters."),
    // P12_QUOTE_REQUIRED = 20
        new Constraint("2.3", "[12]", "2.3 [12] PubidLiteral ::= '\"' PubidChar* '\"' | \"'\" (PubidChar - \"'\")* \"'\"",
                       "[12] PubidLiteral ::= '\"' PubidChar* '\"' | \"'\" (PubidChar - \"'\")* \"'\"\n" +
                       "Public identifiers are specified using quoted strings."),
    // P12_INVALID_CHARACTER = 21
        new Constraint("2.3", "[13]", "2.3 [13] PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]",
                       "[13] PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]\n" +
                       "Public identifiers must consist of PubidChar characters."),
    // P62_UNTERMINATED = 22
        new Constraint("3.4", "[62]", "3.4 [62] includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'",
                       "[62] includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'\n" +
                       "Included conditional section must be terminated by \"]]>\"."),
    // P63_UNTERMINATED = 23
        new Constraint("3.4", "[63]", "3.4 [63] ignoreSect ::= '<![' S? 'IGNORE' S? '[' ignoreSectContents* ']]>'",
                       "[63] ignoreSect ::= '<![' S? 'IGNORE' S? '[' ignoreSectContents* ']]>'\n" +
                       "Excluded conditional sections must be terminated by \"]]>\"."),
    // P65_INVALID_CHARACTER = 24
        new Constraint("3.4", "[65]", "3.4 [65] Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)",
                       "[65] Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)\n" +
                       "Excluded conditional sections are required to contain legal XML characters."),
    // P40_UNTERMINATED = 25
        new Constraint("3.1", "[40]", "3.1 [40] STag ::= '<' Name (S Attribute)* S? '>'",
                       "[40] STag ::= '<' Name (S Attribute)* S? '>'\n" +
                       "[39] element ::= EmptyElemTag | STag content ETag\n" +
                       "[44] EmptyElemTag ::= '<' Name (S Attribute)* S? '/>'\n" +
                       "Element type name must be followed by Attribute, \">\" or \"/>\"."),
    // P41_EQ_REQUIRED = 26
        new Constraint("3.1", "[41]", "3.1 [41] Attribute ::= Name Eq AttValue",
                       "[41] Attribute ::= Name Eq AttValue\n" +
                       "[25] Eq ::= S? '=' S?\n" +
                       "Attribute name must be followed by an '=' character."),
    // WFC_UNIQUE_ATT_SPEC = 27
        new Constraint("3.1", "[40] [44]", "3.1 WFC: Unique Att Spec",
                       "Well-Formedness Constraint: Unique Att Spec\n" +
                       "[40] STag ::= '<' Name (S Attribute)* S? '>'\n" +
                       "[44] EmptyElemTag ::= '<' Name (S Attribute)* S? '/>'\n" +
                       "No attribute name may appear more than once in the same start-tag or\n" +
                       "empty-element tag."),
    // P77_ENCODINGDECL_REQUIRED = 28
        new Constraint("4.3.1", "[77]", "4.3.1 [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'",
                       "[77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'\n" +
                       "The encoding declaration is required in a text declaration."),
    // P23_VERSIONINFO_REQUIRED = 29
        new Constraint("2.8", "[23]", "2.8 [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'",
                       "[23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'\n" +
                       "The version is required in an XML declaration."),
    // P24_EQ_REQUIRED = 30
        new Constraint("2.8", "[24]", "2.8 [24] VersionInfo ::= S 'version' Eq (\"'\" VersionNum \"'\" | '\"' VersionNum '\"')",
                       "[24] VersionInfo ::= S 'version' Eq (\"'\" VersionNum \"'\" | '\"' VersionNum '\"')\n" +
                       "[25] Eq ::= S? '=' S?\n" +
                       "The '=' character must follow \"version\" in VersionInfo."),
    // P32_EQ_REQUIRED = 31
        new Constraint("2.9", "[32]", "2.9 [32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')",
                       "[32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')\n" +
                       "[25] Eq ::= S? '=' S?\n" +
                       "The '=' character must follow \"standalone\" in SDDecl."),
    // P80_EQ_REQUIRED = 32
        new Constraint("4.3.3", "[80]", "4.3.3 [80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")",
                       "[80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")\n" +
                       "[25] Eq ::= S? '=' S?\n" +
                       "The '=' character must follow \"encoding\" in EncodingDecl."),
    // P24_QUOTE_REQUIRED = 33
        new Constraint("2.8", "[24]", "2.8 [24] VersionInfo ::= S 'version' Eq (\"'\" VersionNum \"'\" | '\"' VersionNum '\"')",
                       "[24] VersionInfo ::= S 'version' Eq (\"'\" VersionNum \"'\" | '\"' VersionNum '\"')\n" +
                       "The version is specified using a quoted string."),
    // P32_QUOTE_REQUIRED = 34
        new Constraint("2.9", "[32]", "2.9 [32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')",
                       "[32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')\n" +
                       "The standalone document declaration value is specified using a quoted string."),
    // P80_QUOTE_REQUIRED = 35
        new Constraint("4.3.3", "[80]", "4.3.3 [80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")",
                       "[80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")\n" +
                       "The encoding name value is specified using a quoted string."),
    // P26_INVALID_CHARACTER = 36
        new Constraint("2.8", "[26]", "2.8 [26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+",
                       "[26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+\n" +
                       "The version is required to contain legal XML characters.."),
    // P32_INVALID_CHARACTER = 37
        new Constraint("2.9", "[32]", "2.9 [32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')",
                       "[32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')\n" +
                       "The standalone document declaration value is required to contain legal XML characters."),
    // P81_INVALID_CHARACTER = 38
        new Constraint("4.3.3", "[81]", "4.3.3 [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*",
                       "[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*\n" +
                       "The encoding name value is required to contain legal XML characters."),
    // P26_INVALID_VALUE = 39
        new Constraint("2.8", "[26]", "2.8 [26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+",
                       "[26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+\n" +
                       "The version value must match the production for VersionNum."),
    // P26_NOT_SUPPORTED = 40
        new Constraint("2.8", "[26]", "2.8 [26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+",
                       "[26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')+\n" +
                       "Processors may signal an error if they receive documents labeled with versions\n" +
                       "they do not support."),
    // P80_WHITESPACE_REQUIRED = 41
        new Constraint("4.3.3", "[80]", "4.3.3 [80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")",
                       "[80] EncodingDecl ::= S 'encoding' Eq ('\"' EncName '\"' | \"'\" EncName \"'\")\n" +
                       "Whitespace is required between the version and the encoding declaration."),
    // P81_INVALID_VALUE = 42
        new Constraint("4.3.3", "[81]", "4.3.3 [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*",
                       "[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*\n" +
                       "The encoding name value must match the production for EncName."),
    // P32_INVALID_VALUE = 43
        new Constraint("2.9", "[32]", "2.9 [32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')",
                       "[32] SDDecl ::= S 'standalone' Eq (\"'\" ('yes' | 'no') \"'\" | '\"' ('yes' | 'no') '\"')\n" +
                       "The standalone document declaration must have a value of \"yes\" or \"no\"."),
    // P23_UNTERMINATED = 44
        new Constraint("2.8", "[23]", "2.8 [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'",
                       "[23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'\n" +
                       "The XML declaration must be terminated by \"?>\"."),
    // P77_UNTERMINATED = 45
        new Constraint("4.3.1", "[77]", "4.3.1 [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'",
                       "[77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'\n" +
                       "The text declaration must be terminated by \"?>\"."),
    // P28_INVALID_CHARACTER = 46
        new Constraint("2.8", "[28]", "2.8 [28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S? ('[' (markupdecl | PEReference | S)* ']' S?)? '>'",
                       "[28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S? ('[' (markupdecl | PEReference | S)* ']' S?)? '>'\n" +
                       "The internal subset of the DTD is required to contain legal XML characters."),
    // P30_INVALID_CHARACTER = 47
        new Constraint("2.8", "[30]", "2.8 [30] extSubset ::= TextDecl? extSubsetDecl",
                       "[30] extSubset ::= TextDecl? extSubsetDecl\n" +
                       "[31] extSubsetDecl ::= ( markupdecl | conditionalSect | PEReference | S )*\n" +
                       "External entities in the DTD are required to contain legal XML characters."),
    // P9_INVALID_CHARACTER = 48
        new Constraint("2.3", "[9]", "2.3 [9] EntityValue ::= '\"' ([^%&\"] | PEReference | Reference)* '\"' | \"'\" ([^%&'] | PEReference | Reference)* \"'\"",
                       "[9] EntityValue ::= '\"' ([^%&\"] | PEReference | Reference)* '\"' | \"'\" ([^%&'] | PEReference | Reference)* \"'\"\n" +
                       "An entity value is required to contain legal XML characters."),
    // P51_UNTERMINATED = 49
        new Constraint("3.2.2", "[51]", "3.2.2 [51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'",
                       "[51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'\n" +
                       "A mixed content model with child element types must be terminated by \")*\"."),
    // P69_NAME_REQUIRED = 50
        new Constraint("4.1", "[69]", "4.1 [69] PEReference ::= '%' Name ';'",
                       "[69] PEReference ::= '%' Name ';'\n" +
                       "The '&' delimiter must be followed by a valid Name in a parameter entity reference."),
    // P69_SEMICOLON_REQUIRED = 51
        new Constraint("4.1", "[69]", "4.1 [69] PEReference ::= '%' Name ';'",
                       "[69] PEReference ::= '%' Name ';'\n" +
                       "A parameter entity reference must end with a ';' delimiter."),
    // P75_INVALID = 52
        new Constraint("4.2.2", "[75]", "4.2.2 [75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S PubidLiteral S SystemLiteral",
                       "[75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S PubidLiteral S SystemLiteral\n" +
                       "An external entity declaration must begin with either \"SYSTEM\" or \"PUBLIC\"."),
    // WFC_PES_IN_INTERNAL_SUBSET = 53
        new Constraint("2.8", "[40] [44]", "2.8 WFC: PEs in Internal Subset",
                       "Well-Formedness Constraint: PEs in Internal Subset\n" +
                       "[28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S? ('[' (markupdecl | PEReference | S)* ']' S?)? '>'\n" +
                       "In the internal DTD subset, parameter-entity references can occur only where\n" +
                       "markup declarations can occur, not within markup declarations."),
    // P22_INVALID_CHARACTER = 54
        new Constraint("2.8", "[22]", "2.8 [22] prolog ::= XMLDecl? Misc* (doctypedecl Misc*)?",
                       "[22] prolog ::= XMLDecl? Misc* (doctypedecl Misc*)?\n" +
                       "The prolog is required to contain legal XML characters."),
    // P27_INVALID_CHARACTER = 55
        new Constraint("2.8", "[27]", "2.8 [27] Misc ::= Comment | PI |  S",
                       "[27] Misc ::= Comment | PI |  S\n" +
                       "The markup after the end of the element content is required to contain legal XML characters."),
    // P20_INVALID_CHARACTER = 56
        new Constraint("2.7", "[20]", "2.7 [20] CData ::= (Char* - (Char* ']]>' Char*))",
                       "[20] CData ::= (Char* - (Char* ']]>' Char*))\n" +
                       "CDATA sections are required to contain legal XML characters."),
    // P43_INVALID_CHARACTER = 57
        new Constraint("3.1", "[43]", "3.1 [43] content ::= (element | CharData | Reference | CDSect | PI | Comment)*",
                       "[43] content ::= (element | CharData | Reference | CDSect | PI | Comment)*\n" +
                       "The content of elements is required to contain legal XML characters."),
    // P39_UNTERMINATED = 58
        new Constraint("3", "[39]", "3 [39] element ::= EmptyElemTag | STag content ETag",
                       "[39] element ::= EmptyElemTag | STag content ETag\n" +
                       "The end of every element that begins with a start-tag must be marked by an\n" +
                       "end-tag containing a name that echoes the element's type as given in the\n" +
                       "start-tag."),
    // P42_UNTERMINATED = 59
        new Constraint("3.1", "[42]", "3.1 [42] ETag ::= '</' Name S? '>'",
                       "[42] ETag ::= '</' Name S? '>'\n" +
                       "An end-tag must be terminated by a '>' delimiter."),
    // P81_NOT_SUPPORTED = 60
        new Constraint("4.3.3", "[81]", "4.3.3 [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*",
                       "[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*\n" +
                       "It is a fatal error when an XML processor encounters an entity with an\n" +
                       "encoding that it is unable to process.\n"),
    // WFC_ENTITY_DECLARED = 61
        new Constraint("4.1", "[68]", "4.1 WFC: Entity Declared",
                       "Well-Formedness Constraint: Entity Declared\n" +
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "In a document without any DTD, a document with only an internal DTD subset\n" +
                       "which contains no parameter entity references, or a document with\n" +
                       "\"standalone='yes'\", the Name given in the entity reference must match that\n" +
                       "in an entity declaration, except that well-formed documents need not declare\n" +
                       "any of the following entities: amp, lt, gt, apos, quot. The declaration of a\n" +
                       "parameter entity must precede any reference to it. Similarly, the\n" +
                       "declaration of a general entity must precede any reference to it which\n" +
                       "appears in a default value in an attribute-list declaration. Note that if\n" +
                       "entities are declared in the external subset or in external parameter\n" +
                       "entities, a non-validating processor is not obligated to read and process\n" +
                       "their declarations; for such documents, the rule that an entity must be\n" +
                       "declared is a well-formedness constraint only if standalone='yes'."),
    // VC_ENTITY_DECLARED = 62
        new Constraint("4.1", "[68]", "4.1 VC: Entity Declared",
                       "Validity Constraint: Entity Declared\n" +
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "In a document with an external subset or external parameter entities with\n" +
                       "\"standalone='no'\", the Name given in the entity reference must match that in\n" +
                       "an entity declaration. For interoperability, valid documents should declare\n" +
                       "the entities amp, lt, gt, apos, quot, in the form specified in\n" +
                       "\"4.6 Predefined Entities\". The declaration of a parameter entity must\n" +
                       "precede any reference to it. Similarly, the declaration of a general entity\n" +
                       "must precede any reference to it which appears in a default value in an\n" +
                       "attribute-list declaration."),
    // WFC_PARSED_ENTITY = 63
        new Constraint("4.1", "[68]", "4.1 WFC: Parsed Entity",
                       "Well-Formedness Constraint: Parsed Entity\n" +
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "An entity reference must not contain the name of an unparsed entity.\n" +
                       "Unparsed entities may be referred to only in attribute values declared to be\n" +
                       "of type ENTITY or ENTITIES."),
    // WFC_NO_EXTERNAL_ENTITY_REFERENCES = 64
        new Constraint("3.1 2.3", "[41] [10]", "4.1 WFC: No External Entity References",
                       "Well-Formedness Constraint: No External Entity References\n" +
                       "[41] Attribute ::= Name Eq AttValue\n" +
                       "[10] AttValue ::= '\"' ([^<&\"] | Reference)* '\"' | \"'\" ([^<&'] | Reference)* \"'\"\n" +
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "Attribute values cannot contain direct or indirect entity references to\n" +
                       "external entities."),
    // P33_INVALID = 65
        new Constraint("2.12", "[33]", "2.12 [33] LanguageID ::= Langcode ('-' Subcode)*",
                       "[33] LanguageID ::= Langcode ('-' Subcode)*\n" +
                       "[34] Langcode ::= ISO639Code |  IanaCode |  UserCode\n" +
                       "[35] ISO639Code ::= ([a-z] | [A-Z]) ([a-z] | [A-Z])\n" +
                       "[36] IanaCode ::= ('i' | 'I') '-' ([a-z] | [A-Z])+\n" +
                       "[37] UserCode ::= ('x' | 'X') '-' ([a-z] | [A-Z])+\n" +
                       "[38] Subcode ::= ([a-z] | [A-Z])+\n" +
                       "An xml:lang attribute value must match the LanguageID production."),
    // P18_UNTERMINATED = 66
        new Constraint("2.7", "[18]", "2.7 [18] CDSect ::= CDStart CData CDEnd",
                       "[18] CDSect ::= CDStart CData CDEnd\n" +
                       "[19] CDStart ::= '<![CDATA['\n" +
                       "[20] CData ::= (Char* - (Char* ']]>' Char*))\n" +
                       "[21] CDEnd ::= ']]>'\n" +
                       "CDATA sections must be terminated by \"]]>\"."),
    // VC_NO_DUPLICATE_TYPES = 67
        new Constraint("3.2.2", "[51]", "3.2.2 VC: No Duplicate Types",
                       "Validity Constraint: No Duplicate Types\n" +
                       "[51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'\n" +
                       "The same name must not appear more than once in a single mixed-content\n" +
                       "declaration."),
    // P78_NOT_WELLFORMED = 68
        new Constraint("4.3.2", "[78]", "4.3.2 [78] extParsedEnt ::= TextDecl? content",
                       "[78] extParsedEnt ::= TextDecl? content\n" +
                       "An internal general parsed entity is well-formed if its replacement text\n" +
                       "matches the production labeled content.\n\n" +
                       "A consequence of well-formedness in entities is that the logical and\n" +
                       "physical structures in an XML document are properly nested; no start-tag,\n" +
                       "end-tag, empty-element tag, element, comment, processing instruction,\n" +
                       "character reference, or entity reference can begin in one entity and end in\n" +
                       "another."),
    // VC_ID_ATTRIBUTE_DEFAULT = 69
        new Constraint("3.3.1 3.3", "[54] [53]", "3.3.1 VC: ID Attribute Default",
                       "Validity Constraint: ID Attribute Default\n" +
                       "[53] AttDef ::= S Name S AttType S DefaultDecl\n" +
                       "[54] AttType ::= StringType | TokenizedType | EnumeratedType\n" +
                       "[60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED' | (('#FIXED' S)? AttValue)\n" +
                       "An ID attribute must have a declared default of #IMPLIED or #REQUIRED."),
    // P53_NAME_REQUIRED = 70
        new Constraint("3.3", "[53]", "3.3 [53] AttDef ::= S Name S AttType S DefaultDecl",
                       "[52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'\n" +
                       "[53] AttDef ::= S Name S AttType S DefaultDecl\n" +
                       "In an attribute-list declaration, the Name in the AttDef rule is the name\n" +
                       "of the attribute."),
    // P53_ATTTYPE_REQUIRED = 71
        new Constraint("3.3", "[53]", "3.3 [53] AttDef ::= S Name S AttType S DefaultDecl",
                       "[52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'\n" +
                       "[53] AttDef ::= S Name S AttType S DefaultDecl\n" +
                       "."),
    // P81_REQUIRED = 72
        new Constraint("4.3.3", "[81]", "4.3.3 [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*",
                       "[81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*\n" +
                       "Parsed entities which are stored in an encoding other than UTF-8 or\n" +
                       "UTF-16 must begin with an XML declaration or a text declaration that\n" +
                       "contains an encoding declaration."),
    // WFC_NO_RECURSION = 73
        new Constraint("4.1", "[68]", "4.1 WFC: No Recursion",
                       "Well-Formedness Constraint: No Recursion\n" +
                       "[68] EntityRef ::= '&' Name ';'\n" +
                       "[69] PEReference ::= '%' Name ';'\n" +
                       "A parsed entity must not contain a recursive reference to itself, either\n" +
                       "directly or indirectly."),
    // VC_PROPER_DECLARATION_PE_NESTING = 74
        new Constraint("2.8", "[29]", "2.8 VC: Proper Declaration/PE Nesting",
                       "Validity Constraint: Proper Declaration/PE Nesting\n" +
                       "[29] markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment\n" +
                       "Parameter-entity replacement text must be properly nested with markup\n" +
                       "declarations. That is to say, if either the first character or the last\n" +
                       "character of a markup declaration (markupdecl above) is contained in the\n" +
                       "replacement text for a parameter-entity reference, both must be contained in\n" +
                       "the same replacement text."),
    // VC_PROPER_GROUP_PE_NESTING = 75
        new Constraint("3.2.1", "[47]", "3.2.1 VC: Proper Group/PE Nesting",
                       "Validity Constraint: Proper Group/PE Nesting\n" +
                       "[47] children ::= (choice | seq) ('?' | '*' | '+')?\n" +
                       "[48] cp ::= (Name | choice | seq) ('?' | '*' | '+')?\n" +
                       "[49] choice ::= '(' S? cp ( S? '|' S? cp )* S? ')'\n" +
                       "[50] seq ::= '(' S? cp ( S? ',' S? cp )* S? ')'\n" +
                       "[51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'\n" +
                       "Parameter-entity replacement text must be properly nested with parenthetized\n" +
                       "groups. That is to say, if either of the opening or closing parentheses in a\n" +
                       "choice, seq, or Mixed construct is contained in the replacement text for a\n" +
                       "parameter entity, both must be contained in the same replacement text."),
    // VC_ID = 76
        new Constraint("3.3.1", "[56]", "3.3.1 VC: ID",
                       "Validity Constraint: ID\n" +
                       "[56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'\n" +
                       "Values of type ID must match the Name production. A name must not appear\n" +
                       "more than once in an XML document as a value of this type; i.e., ID values\n" +
                       "must uniquely identify the elements which bear them."),
    // VC_ENTITY_NAME = 77
        new Constraint("3.3.1", "[56]", "3.3.1 VC: Entity Name",
                       "Validity Constraint: Entity Name\n" +
                       "[56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'\n" +
                       "Values of type ENTITY must match the Name production, values of type\n" +
                       "ENTITIES must match Names; each Name must match the name of an unparsed\n" +
                       "entity declared in the DTD."),
    // VC_ATTRIBUTE_VALUE_TYPE = 78
        new Constraint("3.1", "[41]", "3.1 VC: Attribute Value Type",
                       "Validity Constraint: Attribute Value Type\n" +
                       "[41] Attribute ::= Name Eq AttValue\n" +
                       "The attribute must have been declared; the value must be of the type\n" +
                       "declared for it."),
    // VC_ELEMENT_VALID = 79
        new Constraint("3", "[39]", "3 VC: Element Valid",
                       "Validity Constraint: Element Valid\n" +
                       "[39] element ::= EmptyElemTag | STag content ETag\n" +
                       "An element is valid if there is a declaration matching elementdecl where the\n" +
                       "Name matches the element type, and one of the following holds:\n\n" +
                       "  1. The declaration matches EMPTY and the element has no content.\n" +
                       "  2. The declaration matches children and the sequence of child elements\n" +
                       "     belongs to the language generated by the regular expression in the\n" +
                       "     content model, with optional white space (characters matching the\n" +
                       "     nonterminal S) between each pair of child elements.\n" +
                       "  3. The declaration matches Mixed and the content consists of character\n" +
                       "     data and child elements whose types match names in the content model.\n" +
                       "  4. The declaration matches ANY, and the types of any child elements have\n" +
                       "     been declared."),
    // VC_STANDALONE_DOCUMENT_DECLARATION = 80
        new Constraint("2.9", "[32]", "2.9 VC: Standalone Document Declaration",
                       "Validity Constraint: Standalone Document Declaration\n" +
                       "[32] SDDecl ::= S 'standalone' Eq ((\"'\" ('yes' | 'no') \"'\") | ('\"' ('yes' | 'no') '\"'))\n" +
                       "The standalone document declaration must have the value \"no\" if any external\n" +
                       "markup declarations contain declarations of:\n\n" +
                       "   * attributes with default values, if elements to which these attributes\n" +
                       "     apply appear in the document without specifications of values for these\n" +
                       "     attributes, or\n" +
                       "   * entities (other than amp, lt, gt, apos, quot), if references to those\n" +
                       "     entities appear in the document, or\n" +
                       "   * attributes with values subject to normalization, where the attribute\n" +
                       "     appears in the document with a value which will change as a result of\n" +
                       "     normalization, or\n" +
                       "   * element types with element content, if white space occurs directly\n" +
                       "     within any instance of those types."),
    // VC_ONE_ID_PER_ELEMENT_TYPE = 81
        new Constraint("3.3.1", "[56]", "3.3.1 VC: One ID per Element Type",
                       "Validity Constraint: One ID per Element Type\n" +
                       "[56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'\n" +
                       "No element type may have more than one ID attribute specified."),
    // VC_UNIQUE_ELEMENT_TYPE_DECLARATION = 82
        new Constraint("3.2", "[45]", "3.2 VC: Unique Element Type Declaration",
                       "Validity Constraint: Unique Element Type Declaration\n" +
                       "[45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'\n" +
                       "No element type may be declared more than once."),
    // P45_UNDECLARED_ELEMENT_IN_CONTENTSPEC = 83
        new Constraint("3.2", "[45]", "3.2 [45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'",
                       "[45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'\n" +
                       "At user option, an XML processor may issue a warning when a declaration\n" +
                       "mentions an element type for which no declaration is provided, but this\n" +
                       "is not an error."),

    // VC_NOTATION_ATTRIBUTES = 84
        new Constraint("?.?", "[??]", "", ""),
    // P53_DUPLICATE = 85
        new Constraint("?.?", "[??]", "", ""),
    // VC_ENUMERATION = 86
        new Constraint("?.?", "[??]", "", ""),
    // VC_FIXED_ATTRIBUTE_DEFAULT = 87
        new Constraint("?.?", "[??]", "", ""),
    // VC_REQUIRED_ATTRIBUTE = 88
        new Constraint("?.?", "[??]", "", ""),
    // VC_NOTATION_DECLARED = 89
        new Constraint("?.?", "[??]", "", ""),
    // P58_NAME_REQUIRED = 90
        new Constraint("?.?", "[??]", "", ""),
    // P58_UNTERMINATED = 91
        new Constraint("?.?", "[??]", "", ""),
    // P59_NMTOKEN_REQUIRED = 92
        new Constraint("?.?", "[??]", "", ""),
    // P59_UNTERMINATED = 93
        new Constraint("?.?", "[??]", "", ""),
    // P70_SPACE = 94
        new Constraint("?.?", "[??]", "", ""),
    // P70_REQUIRED_NAME = 95
        new Constraint("?.?", "[??]", "", ""),
    // P70_REQUIRED_SPACE = 96
        new Constraint("?.?", "[??]", "", ""),
    // P71_UNTERMINATED = 97
        new Constraint("?.?", "[??]", "", ""),
    // P72_SPACE = 98
        new Constraint("?.?", "[??]", "", ""),
    // P72_UNTERMINATED = 99
        new Constraint("?.?", "[??]", "", ""),
    // P76_REQUIRED = 100
        new Constraint("?.?", "[??]", "", ""),
    // P82_NAME_REQUIRED = 101
        new Constraint("?.?", "[??]", "", ""),
    // P82_SPACE_REQUIRED = 102
        new Constraint("?.?", "[??]", "", ""),
    // P82_UNTERMINATED = 103
        new Constraint("?.?", "[??]", "", ""),
    // P14_INVALID = 104
        new Constraint("?.?", "[??]", "", ""),
    // P16_PITARGET_REQUIRED = 105
        new Constraint("?.?", "[??]", "", ""),
    // P16_REQUIRED = 106
        new Constraint("?.?", "[??]", "", ""),
    // P1_ELEMENT_REQUIRED = 107
        new Constraint("?.?", "[??]", "", ""),
    // P22_NOT_RECOGNIZED = 108
        new Constraint("?.?", "[??]", "", ""),
    // P27_NOT_RECOGNIZED = 109
        new Constraint("?.?", "[??]", "", ""),
    // P43_NOT_RECOGNIZED = 110
        new Constraint("?.?", "[??]", "", ""),
    // P52_ELEMENT_TYPE_REQUIRED = 111
        new Constraint("?.?", "[??]", "", ""),
    // P52_SPACE_REQUIRED = 112
        new Constraint("?.?", "[??]", "", ""),
    // P53_SPACE_REQUIRED = 113
        new Constraint("?.?", "[??]", "", ""),
    // P58_OPEN_PAREN_REQUIRED = 114
        new Constraint("?.?", "[??]", "", ""),
    // P58_SPACE_REQUIRED = 115
        new Constraint("?.?", "[??]", "", ""),
    // P60_SPACE_REQUIRED = 116
        new Constraint("?.?", "[??]", "", ""),
    // S2_10_DECLARATION_ILLEGAL = 117
        new Constraint("?.?", "[??]", "", ""),
    // P39_ELEMENT_TYPE_REQUIRED = 118
        new Constraint("?.?", "[??]", "", ""),
    // P28_ROOT_ELEMENT_TYPE_REQUIRED = 119
        new Constraint("?.?", "[??]", "", ""),
    // P28_SPACE_REQUIRED = 120
        new Constraint("?.?", "[??]", "", ""),
    // P28_UNTERMINATED = 121
        new Constraint("?.?", "[??]", "", ""),
    // P29_NOT_RECOGNIZED = 122
        new Constraint("?.?", "[??]", "", ""),
    // P45_CONTENTSPEC_REQUIRED = 123
        new Constraint("?.?", "[??]", "", ""),
    // P45_ELEMENT_TYPE_REQUIRED = 124
        new Constraint("?.?", "[??]", "", ""),
    // P45_SPACE_REQUIRED = 125
        new Constraint("?.?", "[??]", "", ""),
    // P45_UNTERMINATED = 126
        new Constraint("?.?", "[??]", "", ""),
    // P47_CLOSE_PAREN_REQUIRED = 127
        new Constraint("?.?", "[??]", "", ""),
    // P47_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED = 128
        new Constraint("?.?", "[??]", "", ""),
    // P51_CLOSE_PAREN_REQUIRED = 129
        new Constraint("?.?", "[??]", "", ""),
    // P51_ELEMENT_TYPE_REQUIRED = 130
        new Constraint("?.?", "[??]", "", ""),
    // P75_SPACE_REQUIRED = 131
        new Constraint("?.?", "[??]", "", ""),
    // P76_SPACE_REQUIRED = 132
        new Constraint("?.?", "[??]", "", ""),
    // P15_UNTERMINATED = 133
        new Constraint("?.?", "[??]", "", ""),
    // P16_UNTERMINATED = 134
        new Constraint("?.?", "[??]", "", ""),
    // P67_UNTERMINATED = 135
        new Constraint("?.?", "[??]", "", ""),
    // P10_UNTERMINATED = 136
        new Constraint("?.?", "[??]", "", ""),
    // P22_XMLDECL_MUST_BE_FIRST = 137
        new Constraint("?.?", "[??]", "", ""),
    // P30_TEXTDECL_MUST_BE_FIRST = 138
        new Constraint("?.?", "[??]", "", ""),
    // P5_INVALID_CHARACTER = 139
        new Constraint("?.?", "[??]", "", ""),
    // P11_UNTERMINATED = 140
        new Constraint("?.?", "[??]", "", ""),
    // P12_UNTERMINATED = 141
        new Constraint("?.?", "[??]", "", ""),
    // P11_URI_FRAGMENT = 142
        new Constraint("?.?", "[??]", "", ""),
    // VC_ONE_NOTATION_PER_ELEMENT_TYPE = 143
        new Constraint("3.3.1", "[58]", "VC: One Notation per Element Type",
                       "Validity Constraint: One Notation per Element Type\n" +
                       "[58] NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'\n" +
                       "No element type may have more than one NOTATION attribute specified."),
    // NC_PREFIX_DECLARED = 144
        new Constraint("4.", "", "NC: Prefix Declared", ""),

        // ...
        new Constraint(null, null, null, null)
    };
 */
}

/*
class Constraint {
    String sections;
    String productions;
    String shortDesc;
    String longDesc;
    Constraint(String sections, String productions, String shortDesc, String longDesc) {
        this.sections = sections;
        this.productions = productions;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }
}
 */
