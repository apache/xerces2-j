/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.msg;

import java.util.ListResourceBundle;

/**
 * This file contains error and warning messages related to XML
 * The messages are arranged in key and value tuples in a ListResourceBundle.
 *
 * @version
 */
public class XMLMessages extends ListResourceBundle {
    /** The list resource bundle contents. */
    public static final Object CONTENTS[][] = {
// Internal message formatter messages
        { "BadMajorCode", "The majorCode parameter to createMessage was out of bounds." },
        { "FormatFailed", "An internal error occurred while formatting the following message:\n  " },
// Document messages
    // 2.1 Well-Formed XML Documents
        { "RootElementRequired", "The root element is required in a well-formed document." },
    // 2.2 Characters
        { "InvalidCharInCDSect", "An invalid XML character (Unicode: 0x{0}) was found in the CDATA section." },
        { "InvalidCharInContent", "An invalid XML character (Unicode: 0x{0}) was found in the element content of the document." },
        { "TwoColonsInQName", "An invalid second ':' was found in the element type or attribute name." },
        { "InvalidCharInMisc", "An invalid XML character (Unicode: 0x{0}) was found in markup after the end of the element content." },
        { "InvalidCharInProlog", "An invalid XML character (Unicode: 0x{0}) was found in the prolog of the document." },
        { "InvalidCharInXMLDecl", "An invalid XML character (Unicode: 0x{0}) was found in the XML declaration." },
    // 2.4 Character Data and Markup
        { "CDEndInContent", "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section." },
    // 2.7 CDATA Sections
        { "CDSectUnterminated", "The CDATA section must end with \"]]>\"." },
    // 2.8 Prolog and Document Type Declaration
        { "XMLDeclMustBeFirst", "The XML declaration may only appear at the very beginning of the document." },
        { "EqRequiredInXMLDecl", "The ''='' character must follow \"{0}\" in the XML declaration." },
        { "QuoteRequiredInXMLDecl",  "The value following \"{0}\" in the XML declaration must be a quoted string." },
        { "XMLDeclUnterminated", "The XML declaration must end with \"?>\"." },
        { "VersionInfoRequired", "The version is required in the XML declaration." },
        { "MarkupNotRecognizedInProlog", "The markup in the document preceding the root element must be well-formed." },
        { "MarkupNotRecognizedInMisc", "The markup in the document following the root element must be well-formed." },
    // 2.9 Standalone Document Declaration
        { "SDDeclInvalid", "The standalone document declaration value must be \"yes\" or \"no\", not \"{0}\"." },
    // 2.12 Language Identification
        { "XMLLangInvalid", "The xml:lang attribute value \"{0}\" is an invalid language identifier." },
    // 3. Logical Structures
        { "ETagRequired", "The element type \"{0}\" must be terminated by the matching end-tag \"</{0}>\"." },
    // 3.1 Start-Tags, End-Tags, and Empty-Element Tags
        { "ElementUnterminated", "Element type \"{0}\" must be followed by either attribute specifications, \">\" or \"/>\"." },
        { "EqRequiredInAttribute", "Attribute name \"{0}\" must be followed by the ''='' character." },
        { "AttributeNotUnique", "Attribute \"{1}\" was already specified for element \"{0}\"." },
        { "ETagUnterminated", "The end-tag for element type \"{0}\" must end with a ''>'' delimiter." },
        { "MarkupNotRecognizedInContent", "The content of elements must consist of well-formed character data or markup." },
    // 4.1 Character and Entity References
        { "ReferenceUnterminated", "The reference must be terminated by a ';' delimiter." },
    // 4.3.2 Well-Formed Parsed Entities
        { "ReferenceNotInOneEntity", "The reference must be entirely contained within the same parsed entity." },
        { "ElementEntityMismatch", "The element \"{0}\" must start and end within the same entity." },
// Messages common to Document and DTD
    // 2.2 Characters
        { "InvalidCharInAttValue", "An invalid XML character (Unicode: 0x{2}) was found in the value of attribute \"{1}\"." },
        { "InvalidCharInComment", "An invalid XML character (Unicode: 0x{0}) was found in the comment." },
        { "InvalidCharInPI", "An invalid XML character (Unicode: 0x{0}) was found in the processing instruction." },
        { "InvalidCharInInternalSubset", "An invalid XML character (Unicode: 0x{0}) was found in the internal subset of the DTD." },
        { "InvalidCharInTextDecl", "An invalid XML character (Unicode: 0x{0}) was found in the text declaration." },
    // 2.3 Common Syntactic Constructs
        { "QuoteRequiredInAttValue", "The value of attribute \"{1}\" must begin with either a single or double quote character." },
        { "LessthanInAttValue", "The value of attribute \"{1}\" must not contain the ''<'' character." },
        { "AttributeValueUnterminated", "The value for attribute \"{1}\" must end with the matching quote character." },
    // 2.5 Comments
        { "DashDashInComment", "The string \"--\" is not permitted within comments." },
        { "CommentUnterminated", "The comment must end with \"-->\"." },
    // 2.6 Processing Instructions
        { "PITargetRequired", "The processing instruction must begin with the name of the target." },
        { "SpaceRequiredInPI", "White space is required between the processing instruction target and data." },
        { "PIUnterminated", "The processing instruction must end with \"?>\"." },
        { "ReservedPITarget", "The processing instruction target matching \"[xX][mM][lL]\" is not allowed." },
    // 2.8 Prolog and Document Type Declaration
        { "VersionInfoInvalid", "Invalid version \"{0}\"." },
        { "VersionNotSupported", "XML version \"{0}\" is not supported." },
    // 4.1 Character and Entity References
        { "DigitRequiredInCharRef", "A decimal representation must immediately follow the \"&#\" in a character reference." },
        { "HexdigitRequiredInCharRef", "A hexadecimal representation must immediately follow the \"&#x\" in a character reference." },
        { "SemicolonRequiredInCharRef", "The character reference must end with the ';' delimiter." },
        { "InvalidCharRef", "Character reference \"&#{0}\" is an invalid XML character." },
        { "NameRequiredInReference", "The entity name must immediately follow the '&' in the entity reference." },
        { "SemicolonRequiredInReference", "The reference to entity \"{0}\" must end with the '';'' delimiter." },
    // 4.3.1 The Text Declaration
        { "TextDeclMustBeFirst", "The text declaration may only appear at the very beginning of the external parsed entity." },
        { "EqRequiredInTextDecl", "The ''='' character must follow \"{0}\" in the text declaration." },
        { "QuoteRequiredInTextDecl", "The value following \"{0}\" in the text declaration must be a quoted string." },
        { "SpaceRequiredInTextDecl", "White space is required between the version and the encoding declaration." },
        { "TextDeclUnterminated", "The text declaration must end with \"?>\"." },
        { "EncodingDeclRequired", "The encoding declaration is required in the text declaration." },
    // 4.3.2 Well-Formed Parsed Entities
        { "CommentNotInOneEntity", "The comment must be entirely contained within the same parsed entity." },
        { "PINotInOneEntity", "The processing instruction must be entirely contained within the same parsed entity." },
    // 4.3.3 Character Encoding in Entities
        { "EncodingDeclInvalid", "Invalid encoding name \"{0}\"." },
// DTD Messages
    // 2.2 Characters
        { "InvalidCharInEntityValue", "An invalid XML character (Unicode: 0x{0}) was found in the literal entity value." },
        { "InvalidCharInExternalSubset", "An invalid XML character (Unicode: 0x{0}) was found in the external subset of the DTD." },
        { "InvalidCharInIgnoreSect", "An invalid XML character (Unicode: 0x{0}) was found in the excluded conditional section." },
        { "InvalidCharInPublicID", "An invalid XML character (Unicode: 0x{0}) was found in the public identifier." },
        { "InvalidCharInSystemID", "An invalid XML character (Unicode: 0x{0}) was found in the system identifier." },
    // 2.3 Common Syntactic Constructs
        { "QuoteRequiredInSystemID", "The system identifier must begin with either a single or double quote character." },
        { "SystemIDUnterminated", "The system identifier must end with the matching quote character." },
        { "QuoteRequiredInPublicID", "The public identifier must begin with either a single or double quote character." },
        { "PublicIDUnterminated", "The public identifier must end with the matching quote character." },
        { "PubidCharIllegal", "The character (Unicode: 0x{0}) is not permitted in the public identifier." },
    // 2.8 Prolog and Document Type Declaration
        { "MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL", "White space is required after \"<!DOCTYPE\" in the document type declaration." },
        { "MSG_ROOT_ELEMENT_TYPE_REQUIRED", "The root element type must appear after \"<!DOCTYPE\" in the document type declaration." },
        { "DoctypedeclUnterminated", "The document type declaration for root element type \"{0}\" must end with ''>''." },
        { "PEReferenceWithinMarkup", "The parameter entity reference \"%{0};\" cannot occur within markup in the internal subset of the DTD." },
        { "MSG_MARKUP_NOT_RECOGNIZED_IN_DTD", "The markup declarations contained or pointed to by the document type declaration must be well-formed." },
    // 2.10 White Space Handling
        { "MSG_XML_SPACE_DECLARATION_ILLEGAL", "The attribute declaration for \"xml:space\" must be given as an enumerated type whose only possible values are \"default\" and \"preserve\"." },
    // 3.2 Element Type Declarations
        { "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL", "White space is required after \"<!ELEMENT\" in the element type declaration." },
        { "MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL", "The element type is required in the element type declaration." },
        { "MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL", "White space is required after the element type \"{0}\" in the element type declaration." },
        { "MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL", "The constraint is required after the element type \"{0}\" in the element type declaration." },
        { "ElementDeclUnterminated", "The declaration for element type \"{0}\" must end with ''>''." },
    // 3.2.1 Element Content
        { "MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN", "A ''('' character or an element type is required in the declaration of element type \"{0}\"." },
        { "MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN", "A '')'' is required in the declaration of element type \"{0}\"." },
    // 3.2.2 Mixed Content
        { "MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT", "An element type is required in the declaration of element type \"{0}\"." },
        { "MSG_CLOSE_PAREN_REQUIRED_IN_MIXED", "A '')'' is required in the declaration of element type \"{0}\"." },
        { "MixedContentUnterminated", "The mixed content model \"{0}\" must end with \")*\" when the types of child elements are constrained." },
    // 3.3 Attribute-List Declarations
        { "MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL", "White space is required after \"<!ATTLIST\" in the attribute-list declaration." },
        { "MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL", "The element type is required in the attribute-list declaration." },
        { "MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF", "White space is required before the attribute name in the attribute-list declaration for element \"{0}\"." },
        { "AttNameRequiredInAttDef", "The attribute name must be specified in the attribute-list declaration for element \"{0}\"." },
        { "MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF", "White space is required before the attribute type in the declaration of attribute \"{1}\" for element \"{0}\"." },
        { "AttTypeRequiredInAttDef", "The attribute type is required in the declaration of attribute \"{1}\" for element \"{0}\"." },
        { "MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF", "White space is required before the attribute default in the declaration of attribute \"{1}\" for element \"{0}\"." },
    // 3.3.1 Attribute Types
        { "MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE", "White space must appear after \"NOTATION\" in the \"{1}\" attribute declaration." },
        { "MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE", "The ''('' character must follow \"NOTATION\" in the \"{1}\" attribute declaration." },
        { "MSG_NAME_REQUIRED_IN_NOTATIONTYPE", "The notation name is required in the notation type list for the \"{1}\" attribute declaration." },
        { "NotationTypeUnterminated", "The notation type list must end with '')'' in the \"{1}\" attribute declaration." },
        { "MSG_NMTOKEN_REQUIRED_IN_ENUMERATION", "The name token is required in the enumerated type list for the \"{1}\" attribute declaration." },
        { "EnumerationUnterminated", "The enumerated type list must end with '')'' in the \"{1}\" attribute declaration." },
    // 3.3.2 Attribute Defaults
        { "MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL", "White space must appear after \"FIXED\" in the \"{1}\" attribute declaration." },
    // 3.4 Conditional Sections
        { "IncludeSectUnterminated", "The included conditional section must end with \"]]>\"." },
        { "IgnoreSectUnterminated", "The excluded conditional section must end with \"]]>\"." },
    // 4.1 Character and Entity References
        { "NameRequiredInPEReference", "The entity name must immediately follow the '%' in the parameter entity reference." },
        { "SemicolonRequiredInPEReference", "The parameter entity reference \"%{0};\" must end with the '';'' delimiter." },
    // 4.2 Entity Declarations
        { "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL", "White space is required after \"<!ENTITY\" in the entity declaration." },
        { "MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL", "White space is required between \"<!ENTITY\" and the '%' character in the parameter entity declaration." },
        { "MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL", "White space is required between the '%' and the entity name in the parameter entity declaration." },
        { "MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL", "The name of the entity is required in the entity declaration." },
        { "MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL", "White space is required between the entity name \"{0}\" and the definition in the entity declaration." },
        { "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL", "White space is required between \"NDATA\" and the notation name in the declaration for the entity \"{0}\"." },
        { "MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL", "The notation name is required after \"NDATA\" in the declaration for the entity \"{0}\"." },
        { "EntityDeclUnterminated", "The declaration for the entity \"{0}\" must end with ''>''." },
    // 4.2.2 External Entities
        { "ExternalIDRequired", "The external entity declaration must begin with either \"SYSTEM\" or \"PUBLIC\"." },
        { "MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID", "White space is required between \"PUBLIC\" and the public identifier." },
        { "MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID", "White space is required between the public identifier and the system identifier." },
        { "MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID", "White space is required between \"SYSTEM\" and the system identifier." },
        { "MSG_URI_FRAGMENT_IN_SYSTEMID", "The fragment identifier should not be specified as part of the system identifier \"{0}\"." },
    // 4.7 Notation Declarations
        { "MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL", "White space is required after \"<!NOTATION\" in the notation declaration." },
        { "MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL", "The name of the notation is required in the notation declaration." },
        { "MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL", "White space is required after the notation name \"{0}\" in the notation declaration." },
        { "NotationDeclUnterminated", "The declaration for the notation \"{0}\" must end with ''>''." },
// Validation messages
        { "DuplicateTypeInMixedContent", "The element type \"{0}\" was already specified in this content model." },
        { "ENTITIESInvalid", "Attribute value \"{1}\" of type ENTITIES must be the names of one or more unparsed entities." },
        { "ENTITYInvalid", "Attribute value \"{1}\" of type ENTITY must be the name of an unparsed entity." },
        { "IDDefaultTypeInvalid", "The ID attribute \"{0}\" must have a declared default of \"#IMPLIED\" or \"#REQUIRED\"." },
        { "IDInvalid", "Attribute value \"{1}\" of type ID must be a name." },
        { "IDNotUnique", "Attribute value \"{1}\" of type ID must be unique within the document." },
        { "IDREFInvalid", "Attribute value \"{1}\" of type IDREF must be a name." },
        { "IDREFSInvalid", "Attribute value \"{0}\" of type IDREFS must be one or more names." },
        { "ImproperDeclarationNesting", "The replacement text of parameter entity \"{0}\" must include properly nested declarations." },
        { "ImproperGroupNesting", "The replacement text of parameter entity \"{0}\" must include properly nested pairs of parentheses." },
        { "MSG_ATTRIBUTE_NOT_DECLARED", "Attribute \"{1}\" must be declared for element type \"{0}\"." },
        { "MSG_ATTRIBUTE_VALUE_NOT_IN_LIST", "Attribute \"{0}\" with value \"{1}\" must have a value from the list \"{2}\"." },
        { "MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE", "The value \"{1}\" of attribute \"{0}\" must not be changed by normalization (to \"{2}\") in a standalone document." },
        { "MSG_CONTENT_INCOMPLETE", "The content of element type \"{0}\" is incomplete, it must match \"{1}\"." },
        { "MSG_CONTENT_INVALID", "The content of element type \"{0}\" must match \"{1}\"." },
        { "MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED", "Attribute \"{1}\" for element type \"{0}\" has a default value and must be specified in a standalone document." },
        { "MSG_DUPLICATE_ATTDEF", "Attribute \"{1}\" is already declared for element type \"{0}\"." },
        { "MSG_ELEMENT_ALREADY_DECLARED", "Element type \"{0}\" must not be declared more than once." },
        { "MSG_ELEMENT_NOT_DECLARED", "Element type \"{0}\" must be declared." },
        { "MSG_ELEMENT_WITH_ID_REQUIRED", "An element with the identifier \"{0}\" must appear in the document." },
        { "MSG_EXTERNAL_ENTITY_NOT_PERMITTED", "The reference to external entity \"{0}\" is not permitted in a standalone document." },
        { "MSG_FIXED_ATTVALUE_INVALID", "Attribute \"{1}\" with value \"{2}\" must have a value of \"{3}\"." },
        { "MSG_MORE_THAN_ONE_ID_ATTRIBUTE", "Element type \"{0}\" already has attribute \"{1}\" of type ID, a second attribute \"{2}\" of type ID is not permitted." },
        { "MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE", "Element type \"{0}\" already has attribute \"{1}\" of type NOTATION, a second attribute \"{2}\" of type NOTATION is not permitted." },
        { "MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE", "The notation \"{2}\" must be declared when referenced in the notation type list for attribute \"{1}\"." },
        { "MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL", "The notation \"{1}\" must be declared when referenced in the unparsed entity declaration for \"{0}\"." },
        { "MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE", "The reference to entity \"{0}\" declared in an external parsed entity is not permitted in a standalone document." },
        { "MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED", "Attribute \"{1}\" is required and must be specified for element type \"{0}\"." },
        { "MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE", "White space must not occur between elements declared in an external parsed entity with element content in a standalone document." },
        { "NMTOKENInvalid", "Attribute value \"{1}\" of type NMTOKEN must be a name token." },
        { "NMTOKENSInvalid", "Attribute value \"{0}\" of type NMTOKENS must be one or more name tokens." },
        { "RootElementTypeMustMatchDoctypedecl", "Document root element \"{1}\", must match DOCTYPE root \"{0}\"." },
        { "UndeclaredElementInContentSpec", "The content model of element \"{0}\" refers to the undeclared element \"{1}\"." },
// Entity related messages
    // 3.1 Start-Tags, End-Tags, and Empty-Element Tags
        { "ReferenceToExternalEntity", "The external entity reference \"&{0};\" is not permitted in an attribute value." },
    // 4.1 Character and Entity References
        { "EntityNotDeclared", "The entity \"{0}\" was referenced, but not declared." },
        { "ReferenceToUnparsedEntity", "The unparsed entity reference \"&{0};\" is not permitted." },
        { "RecursiveReference", "Recursive reference \"&{0};\". (Reference path: {1})" },
        { "RecursivePEReference", "Recursive reference \"%{0};\". (Reference path: {1})" },
    // 4.3.3 Character Encoding in Entities
        { "EncodingNotSupported", "The encoding \"{0}\" is not supported." },
        { "EncodingRequired", "A parsed entity not encoded in either UTF-8 or UTF-16 must contain an encoding declaration." },
// Namespaces support
    // 4. Using Qualified Names
        { "PrefixDeclared", "The namespace prefix \"{0}\" was not declared." },
        { "MSG_ATT_DEFAULT_INVALID", "The defaultValue \"{1}\" of attribute \"{0}\" is not legal as for the lexical constraints of this attribute type." },
    };

    /** Returns the list resource bundle contents. */
    public Object[][] getContents() {
        return CONTENTS;
    }
}
