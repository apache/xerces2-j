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


// TODO - error messages



package org.apache.xerces.validators.schema;


import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLDocumentHandler;
import org.apache.xerces.framework.XMLParser;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.StringPool;


public class SchemaParser
    extends XMLParser
    implements XMLDocumentHandler, XMLDocumentHandler.DTDHandler {

	
	private Schema			fSchema;
	private Schema.Document	fSchemaDocument;
	private Stack			fComponentStack = new Stack ();

	private boolean			fIgnore				= false;
	private boolean			fInSimpleType		= false;


    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaParser (Schema schema) {
        initHandlers(true, this, this);
		fSchema = schema;
    }



    //
    // Public methods
    //


    //
    // XMLParser methods
    //


    /** Start document. */
    public void startDocument() throws Exception {
		System.out.println ("SchemaParser.startDocument\n");
    }

    /** End document. */
    public void endDocument() throws Exception {
		System.out.println ("SchemaParser.endDocument\n");
    }

    /** XML declaration. */
    public void xmlDecl(int versionIndex, int encodingIndex, int standaloneIndex) throws Exception {
		System.out.println ("SchemaParser.xmlDecl\n");
    }


    /**
     * Report the start of the scope of a namespace declaration.
     */
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception {
		System.out.println ("SchemaParser.startNamespaceDeclScope\n");
    }

    /**
     * Report the end of the scope of a namespace declaration.
     */
    public void endNamespaceDeclScope(int prefix) throws Exception {
		System.out.println ("SchemaParser.endNamespaceDeclScope\n");
    }
    

	//
	// The methods we care about!!!
	//

	/*
	 * DESCRIPTION:
	 *
	 * This handler is called for the start of each element.  it assumes/requires that
	 * the root of a Schema document must be a <schema> element.  The spec states that
	 * this need not be the case, but I can see no practical way to not have one in a 
	 * Schema document.  So, if fSchemaDocument is null (which indicates that we have
	 * not yet encountered the <schema> element, we won't accept any other element.
	 *
	 * Once the <schema> element has been encounteres, startSchema is called, which
	 * processes the element and created fSchemaDocument.  After this, other valid
	 * Schema elements are accepted.  Annotations are always possible.  Otherwise, if 
	 * processing children of a simpleType, all other child elements are processed as
	 * facets.  Finally, if not an annotation and if not processing children of a 
	 * simpleType, all other element types are processed.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

    public void startElement(int elementType, XMLAttrList attrList, int attrListIndex) throws Exception 
	{
		if ( fSchemaDocument == null ) {
			if ( elementType == fStringPool.addSymbol(Schema.ELT_SCHEMA) ) {
				startSchema (attrList, attrListIndex);
			} else {
				reportGenericSchemaError ( "Invalid root element" );
			}
		} else {
			if ( elementType == fStringPool.addSymbol(Schema.ELT_ANNOTATION) ) {
				startAnnotation ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_DOCUMENTATION) ) {
				startDocumentation ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_APPINFO) ) {
				startAppInfo ();
			} else if ( fInSimpleType ) {
				startFacet (elementType, attrList, attrListIndex);
			} else {
				if ( elementType == fStringPool.addSymbol(Schema.ELT_SIMPLETYPE) ) {
					startSimpleTypeDef (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_COMPLEXTYPE) ) {
					startComplexTypeDef (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_GROUP) ) {
					startGroupDef (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_CHOICE) ) {
					startChoice (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_SEQUENCE) ) {
					startSeq (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ALL) ) {
					startAll (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ATTRGROUP) ) {
					startAttrGroupDef (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ELEMENT) ) {
					startElementDecl (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ATTRIBUTE) ) { 
					startAttributeDecl (attrList, attrListIndex);
				} else if ( elementType == fStringPool.addSymbol(Schema.ELT_NOTATION) ) {
					startNotationDecl (attrList, attrListIndex);
				} else {
					reportGenericSchemaError ( "Invalid element(" + fStringPool.toString (elementType) + ")" );
				} 
			}
		}
	}


	/*
	 * DESCRIPTION:
	 *
	 * startElement should ensure that endElement will only be called once 
	 * fSchemaDocument has been created.  Annotations are always possible.
	 * Otherwise, if processing the children of a simpleType, look for either
	 * the end of the simpleType or process the element as a facet.
	 *
	 * Otherwise, process the acceptable elements.
	 *
	 * Note: endSchema must set fSchemaDocument to null again to avoid processing
	 * elements after the </schema> element.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */ 

    public void endElement(int elementType) throws Exception 
	{
	
		if ( elementType == fStringPool.addSymbol(Schema.ELT_ANNOTATION) ) {
			endAnnotation ();
		} else if ( elementType == fStringPool.addSymbol(Schema.ELT_DOCUMENTATION) ) {
			endDocumentation ();
		} else if ( elementType == fStringPool.addSymbol(Schema.ELT_APPINFO) ) {
			endAppInfo ();
		} else if ( fInSimpleType ) {
			if ( elementType == fStringPool.addSymbol(Schema.ELT_SIMPLETYPE) ) {
				endSimpleTypeDef ();
			} else {
				endFacet ();
			}
		} else {
			if ( elementType == fStringPool.addSymbol(Schema.ELT_COMPLEXTYPE) ) {
				endComplexTypeDef ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_GROUP) ) {
				endGroupDef ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_CHOICE) ) {
				endChoice ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_SEQUENCE) ) {
				endSeq ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ALL) ) {
				endAll ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ATTRGROUP) ) {
				endAttrGroupDef ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ELEMENT) ) {
				endElementDecl ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_ATTRIBUTE) ) { 
				endAttributeDecl ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_NOTATION) ) {
				endNotationDecl ();
			} else if ( elementType == fStringPool.addSymbol(Schema.ELT_SCHEMA) ) {
				endSchema ();
			} else {	
				reportGenericSchemaError ( "Invalid element (" + fStringPool.toString (elementType) + ")" );
			}
		}
    }

    /** Start entity reference. */
    public void startEntityReference(int entityName, int entityType, int entityContext) throws Exception {
		//System.out.println ("SchemaParser.startEntityReference\n");
    }

    /** End entity reference. */
    public void endEntityReference(int entityName, int entityType, int entityContext) throws Exception {
		//System.out.println ("SchemaParser.endEntityReference\n");
    }

    /** Start CDATA section. */
    public void startCDATA() throws Exception {
		System.out.println ("SchemaParser.startCDATA\n");
    }

    /** End CDATA section. */
    public void endCDATA() throws Exception {
		System.out.println ("SchemaParser.endCDATA\n");
    }

    /** Not called. */
    public void characters(int dataIndex) throws Exception {
        throw new RuntimeException("PAR017 cannot happen 5\n5");
    }

    /** Not called. */
    public void ignorableWhitespace(int dataIndex) throws Exception {
        throw new RuntimeException("PAR017 cannot happen 6\n6");
    }

    /** Processing instruction. */
	/*
    public void processingInstruction(int piTarget, int piData) throws Exception {
		System.out.println ("SchemaParser.processingInstruction\n");
    }
	*/

    /** Comment. */
	/*
    public void comment(int dataIndex) throws Exception {
    }
	*/


	// ************************
	// **** CHARACTER DATA ****
	// ************************

	/*
	 *	characters
	 *
	 *	Process the character data in an element.
	 *
	 */


	private static final int	NONE = 0;
	private static final int	DOCUMENTATION = 1;
	private static final int	APPINFO = 2;	
	private static int			fCharacterMode = 0;


	/*
	 * DESCRIPTION:
	 *
	 * Character data is only valid in the 'documentation' and 'appinfo' children
	 * of annotations.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

    public void characters (char ch[], int start, int length) throws Exception 
	{
		String string = new String (ch, start, length);

		switch (fCharacterMode) {
		case DOCUMENTATION:
			{
			Schema.Annotation annotation = (Schema.Annotation)fComponentStack.peek ();
			annotation.addDocumentation (string);
			}
			break;
		case APPINFO:
			{
			Schema.Annotation annotation = (Schema.Annotation)fComponentStack.peek ();
			annotation.addAppInfo (string);
			}
			break;
		default:
			reportGenericSchemaError ("Invalid CDATA");
		}
    }



    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length) throws Exception {
    }

	//
	// DTDHandler Methods (NOP)
	//

	public void startDTD (int rootElementType, int publicId, int systemId) throws Exception {}
    public void internalSubset(int internalSubset) throws Exception {}
    public void textDecl(int version, int encoding) throws Exception {}
    public void endDTD() throws Exception {}
    public void elementDecl(int elementType, XMLContentSpec contentSpec) throws Exception {}
    public void attlistDecl(int elementType,int attrName, int attType, String enumString, int attDefaultType, int attDefaultValue) throws Exception {}
    public void internalPEDecl(int entityName, int entityValue) throws Exception {}
    public void externalPEDecl(int entityName, int publicId, int systemId) throws Exception {}
    public void internalEntityDecl(int entityName, int entityValue) throws Exception {}
    public void externalEntityDecl(int entityName, int publicId, int systemId) throws Exception {}
    public void unparsedEntityDecl(int entityName, int publicId, int systemId, int notationName) throws Exception {}
    public void notationDecl(int notationName, int publicId, int systemId) throws Exception {}
    public void processingInstruction(int targetIndex, int dataIndex) throws Exception {}
    public void comment(int dataIndex) throws Exception {}



	//
	// SchemaParser Methods - the meat!
	//


	// ***************************
	// **** STRING CONVERTERS ****
	// ***************************


	/*
	 * DESCRIPTION
	 *
	 * These methods each convert string attribute values to the data representations
	 * used by the Schema objects.
	 *
	 * TODO:	Tweak the values for the 'set' data, e.g. final, derivationSet, blockSet.
	 *			I'm not sure if these are correct ... need to review latest spec schema.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */


	private boolean parseBoolean (String booleanString) throws Exception
	{
		if ( booleanString.equals("true") ) {
			return true;
		} else if ( booleanString.equals("false") ) {
			return false;
		} else {
			reportGenericSchemaError ("Invalid boolean value");
			return false;
		}
	}

	private int parseSimpleDerivedBy (String derivedByString) throws Exception
	{
		if ( derivedByString.equals (Schema.VAL_LIST) ) {
			return Schema.LIST;
		} else if ( derivedByString.equals (Schema.VAL_RESTRICTION) ) {
			return Schema.RESTRICTION;
		} else if ( derivedByString.equals (Schema.VAL_REPRODUCTION) ) {
			return Schema.REPRODUCTION;
		} else {
			reportGenericSchemaError ("Invalid value for 'derivedBy'");
			return -1;
		}
	}

	private int parseComplexDerivedBy (String derivedByString)  throws Exception
	{
		if ( derivedByString.equals (Schema.VAL_EXTENSION) ) {
			return Schema.EXTENSION;
		} else if ( derivedByString.equals (Schema.VAL_RESTRICTION) ) {
			return Schema.RESTRICTION;
		} else if ( derivedByString.equals (Schema.VAL_REPRODUCTION) ) {
			return Schema.REPRODUCTION;
		} else {
			reportGenericSchemaError ( "Invalid value for 'derivedBy'" );
			return -1;
		}
	}

	private int parseSimpleFinal (String finalString) throws Exception
	{
		if ( finalString.equals (Schema.VAL_POUNDALL) ) {
			return Schema.ENUMERATION+Schema.RESTRICTION+Schema.LIST+Schema.REPRODUCTION;
		} else {
			int enumerate = 0;
			int restrict = 0;
			int list = 0;
			int reproduce = 0;

			StringTokenizer t = new StringTokenizer (finalString, " ");
			while (t.hasMoreTokens()) {
				String token = t.nextToken ();

				if ( token.equals (Schema.VAL_ENUMERATION) ) {
					if ( enumerate == 0 ) {
						enumerate = Schema.ENUMERATION;
					} else {
						reportGenericSchemaError ("enumeration in set twice");
					}
				} else if ( token.equals (Schema.VAL_RESTRICTION) ) {
					if ( restrict == 0 ) {
						restrict = Schema.RESTRICTION;
					} else {
						reportGenericSchemaError ("restriction in set twice");
					}
				} else if ( token.equals (Schema.VAL_LIST) ) {
					if ( list == 0 ) {
						list = Schema.LIST;
					} else {
						reportGenericSchemaError ("list in set twice");
					}
				} else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
					if ( reproduce == 0 ) {
						reproduce = Schema.REPRODUCTION;
					} else {
						reportGenericSchemaError ("reproduction in set twice");
					}
				} else {
						reportGenericSchemaError (	"Invalid value (" + 
													finalString +
													")" );
				}
			}

			return enumerate+restrict+list+reproduce;
		}
	}

	private int parseComplexContent (String contentString)  throws Exception
	{
		if ( contentString.equals (Schema.VAL_EMPTY) ) {
			return Schema.EMPTY;
		} else if ( contentString.equals (Schema.VAL_ELEMENTONLY) ) {
			return Schema.ELEMENT_ONLY;
		} else if ( contentString.equals (Schema.VAL_TEXTONLY) ) {
			return Schema.TEXT_ONLY;
		} else if ( contentString.equals (Schema.VAL_MIXED) ) {
			return Schema.MIXED;
		} else {
			reportGenericSchemaError ( "Invalid value for content" );
			return -1;
		}
	}

	private int parseDerivationSet (String finalString)  throws Exception
	{
		if ( finalString.equals ("#all") ) {
			return Schema.EXTENSION+Schema.RESTRICTION+Schema.REPRODUCTION;
		} else {
			int extend = 0;
			int restrict = 0;
			int reproduce = 0;

			StringTokenizer t = new StringTokenizer (finalString, " ");
			while (t.hasMoreTokens()) {
				String token = t.nextToken ();

				if ( token.equals (Schema.VAL_EXTENSION) ) {
					if ( extend == 0 ) {
						extend = Schema.EXTENSION;
					} else {
						reportGenericSchemaError ( "extension already in set" );
					}
				} else if ( token.equals (Schema.VAL_RESTRICTION) ) {
					if ( restrict == 0 ) {
						restrict = Schema.RESTRICTION;
					} else {
						reportGenericSchemaError ( "restriction already in set" );
					}
				} else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
					if ( reproduce == 0 ) {
						reproduce = Schema.REPRODUCTION;
					} else {
						reportGenericSchemaError ( "reproduction already in set" );
					}
				} else {
					reportGenericSchemaError ( "Invalid final value (" + finalString + ")" );
				}
			}

			return extend+restrict+reproduce;
		}
	}

	private int parseBlockSet (String finalString)  throws Exception
	{
		if ( finalString.equals ("#all") ) {
			return Schema.EQUIVCLASS+Schema.EXTENSION+Schema.LIST+Schema.RESTRICTION+Schema.REPRODUCTION;
		} else {
			int extend = 0;
			int restrict = 0;
			int reproduce = 0;

			StringTokenizer t = new StringTokenizer (finalString, " ");
			while (t.hasMoreTokens()) {
				String token = t.nextToken ();

				if ( token.equals (Schema.VAL_EQUIVCLASS) ) {
					if ( extend == 0 ) {
						extend = Schema.EQUIVCLASS;
					} else {
						reportGenericSchemaError ( "'equivClass' already in set" );
					}
				} else if ( token.equals (Schema.VAL_EXTENSION) ) {
					if ( extend == 0 ) {
						extend = Schema.EXTENSION;
					} else {
						reportGenericSchemaError ( "extension already in set" );
					}
				} else if ( token.equals (Schema.VAL_LIST) ) {
					if ( extend == 0 ) {
						extend = Schema.LIST;
					} else {
						reportGenericSchemaError ( "'list' already in set" );
					}
				} else if ( token.equals (Schema.VAL_RESTRICTION) ) {
					if ( restrict == 0 ) {
						restrict = Schema.RESTRICTION;
					} else {
						reportGenericSchemaError ( "restriction already in set" );
					}
				} else if ( token.equals (Schema.VAL_REPRODUCTION) ) {
					if ( reproduce == 0 ) {
						reproduce = Schema.REPRODUCTION;
					} else {
						reportGenericSchemaError ( "reproduction already in set" );
					}
				} else {
					reportGenericSchemaError ( "Invalid final value (" + finalString + ")" );
				}
			}

			return extend+restrict+reproduce;
		}
	}

	// *************************
	// **** SCHEMA DOCUMENT ****
	// *************************


	private void startSchema (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		fSchemaDocument = fSchema.createDocument ();
	}

	private void endSchema () throws Exception
	{
		//
		// TODO: add namespace
		//

		// Just for testing.

		fSchema.registerDocument (null, fSchemaDocument);
		fSchemaDocument = null;
		System.out.print ("\n\n***************** SCHEMA *****************\n\n");
		System.out.print (fSchema.toString ());
		System.out.print ("\n\n***************** SCHEMA *****************\n\n");
	}


	// *********************
	// **** ANNOTATIONS ****
	// *********************

	/*
	 * DESCRIPTION:
	 *
	 * Each Schema component may have one annotation and it must be the first
	 * child element of that component.  The Schema document may have multiple
	 * annotations and they may appear anywhere.  Each annotation may have one
	 * or more <documentation> and/or <appInfo> children.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

	private void startAnnotation () throws Exception
	{
		if ( fSchemaDocument != null ) {
			Schema.Annotation annotation = fSchemaDocument.createAnnotation ();
			fComponentStack.push (annotation);
		} else {
			reportGenericSchemaError ("Invalid annotation placement");
		}
	}

	private void endAnnotation () throws Exception
	{
		if ( fSchemaDocument != null ) {
			Schema.Annotation annotation = (Schema.Annotation)fComponentStack.pop ();

			if ( fComponentStack.size() == 0 ) {
				fSchemaDocument.addAnnotation (annotation);
			} else {
				Schema.AnnotatedComponent annotated = (Schema.AnnotatedComponent)fComponentStack.peek ();
				annotated.setAnnotation (annotation);
			}
		}
	}	

	private void startDocumentation ()
	{
		fCharacterMode = DOCUMENTATION;
	}

	private void endDocumentation ()
	{
		fCharacterMode = NONE;
	}

	private void startAppInfo ()
	{
		fCharacterMode = APPINFO;
	}

	private void endAppInfo ()
	{
		fCharacterMode = NONE;
	}


	// **********************
	// **** SIMPLE TYPES **** 
	// **********************

	/*
	 * DESCRIPTION:
	 * 
	 * Create a new simple type def and push on the component stack.  
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */


	//
	// <simpleType [name={name}] [base={base}] [derivedBy={derivedBy}] [abstract={abstract}] [final={final}]>
	//

	private void startSimpleTypeDef (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		Schema.SimpleTypeDef type = fSchemaDocument.createSimpleTypeDef ();

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String value = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				if ( fComponentStack.empty() ) {
					type.setName (value);
				} else {
					reportGenericSchemaError ( "Non-global 'simpleType' cannot have name" );
				}
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_BASE) ) {
				type.setBase (value, null);
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_DERIVEDBY) ) {
				type.setDerivedBy (parseSimpleDerivedBy(value));			
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_ABSTRACT) ) {
				type.setAbstract (parseBoolean(value));			
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_FINAL) ) {
				type.setFinal (parseSimpleFinal(value));			
			} else {
				reportGenericSchemaError ("Invalid attribute for 'simpleType'");
			}
		}

		fComponentStack.push (type);
		fInSimpleType = true;
	}

	//
	// </simpleType>
	//

	/*
	 * DESCRIPTION:
	 *
	 * Pop the finished simple type off of the component stack.  If the resulting component
	 * stack is empty then register the simple type as a global type def with the document.
	 * Otherwise, the next component on the component stack should be something that can have
	 * a simple type (otherwise an error will result).
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

	private void endSimpleTypeDef () throws Exception
	{
		fInSimpleType = false;

		Schema.SimpleTypeDef type = (Schema.SimpleTypeDef)fComponentStack.pop ();

		if ( fComponentStack.size() == 0 ) {
			fSchemaDocument.registerType (type.getName(), type);
		} else {
			try {
				Schema.HasSimpleType component = (Schema.HasSimpleType)fComponentStack.peek ();
				component.setSimpleType (type);
			} catch (ClassCastException e) {
				reportGenericSchemaError ("Invalid use of 'simpleType'");
			}
		}
	}

	//
	// <{facet} value={value}>
	//

	// TODO - what about xmlns attributes?

	/*
	 * DESCRIPTION:
	 *
	 * A generic facet adding mechanism to support extended primitive types.  Basically,
	 * whenever we are in the body of a simple type def, the fInSimpleType flag is set.
	 * When this is the case, we attempt to process all non-annotations in this method.
	 * We accept any element name (the facet name) and require there to be a single
	 * 'value' attribute.  We pass the pair to the enclosing simple type def to process.
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

	private void startFacet (int elementType, XMLAttrList attrList, int attrListIndex) throws Exception
	{
		// can only have one attribute, and must be "value"

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {
			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_VALUE) ) {
				try {
					String name = fStringPool.toString (elementType);
					String value = fStringPool.toString (attrList.getAttValue(i));
					Schema.Facet facet = fSchemaDocument.createFacet (name, value);
					fComponentStack.push (facet);
				} catch (ClassCastException e) {
					reportGenericSchemaError ("Unexpected error");
				}
			} else {
				reportGenericSchemaError ("Invalid facet");
			}
		}
	}

	//
	// </{facet}>
	//

	private void endFacet ()
	{
		Schema.Facet facet = (Schema.Facet)fComponentStack.pop ();
		Schema.SimpleTypeDef type = (Schema.SimpleTypeDef)fComponentStack.peek ();
		type.addFacet (facet);
	}


	// ***********************
	// **** COMPLEX TYPES ****
	// ***********************

	//
	// <complexType [name={name}] [content={content}] [base={base}] [derivedBy={derivedBy}]
	//				[abstract={abstract}] [final={final}] [block={block}]>
	//

	private void startComplexTypeDef (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		Schema.ComplexTypeDef type = fSchemaDocument.createComplexTypeDef ();

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String value = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				if ( fComponentStack.empty() ) {
					type.setName (value);
				} else {
					reportGenericSchemaError ("Non-global complexType cannot have name");
				}
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_CONTENT) ) {
				type.setContent (parseComplexContent(value));
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_BASE) ) {
				type.setBase (value, null);
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_DERIVEDBY) ) {
				type.setDerivedBy (parseComplexDerivedBy(value));			
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_ABSTRACT) ) {
				type.setAbstract (parseBoolean(value));			
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_FINAL) ) {
				type.setFinal (parseDerivationSet(value));			
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_BLOCK) ) {
				type.setBlock (parseDerivationSet(value));			
			} else {
				reportGenericSchemaError (	"Invalid attribute (" +
											fStringPool.toString (attrList.getAttrName(i)) + 
											")" );
			}
		}

		fComponentStack.push (type);
	}

	//
	// </complexType>
	//

	/*
	 * Pop the finished complex type off of the component stack.  If the resulting component
	 * stack is empty then register the complex type as a global type def with the document.
	 * Otherwise, the next component on the component stack should be something that can have
	 * a complex type (otherwise an error will result).
	 *
	 * 3-27-00	watso	First implementation completed.
	 *
	 */

	private void endComplexTypeDef () throws Exception
	{
		Schema.ComplexTypeDef type = (Schema.ComplexTypeDef)fComponentStack.pop ();

		if ( fComponentStack.size() == 0 ) {
			fSchemaDocument.registerType (type.getName(), type);
		} else {
			try {
				Schema.HasType component = (Schema.HasType)fComponentStack.peek ();
				component.setType (type);
			} catch (ClassCastException e) {
				reportGenericSchemaError ("Invalid use of 'complexType'");
			}
		}
	}


	// ****************
	// **** GROUPS ****
	// ****************

	//
	// <group [name={name}] [ref={ref}] [minOccurs={minOccurs}] [maxOccurs={maxOccurs}]>
	//

	// TODO: represent minOccurs and maxOccurs as BigDecimal
	// TODO: use integer datatype class to parse integers

	private void startGroupDef (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		String name = null;
		String ref = null;
		String minOccurs = null;
		String maxOccurs = null;

		//
		// Collect attributes
		//

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String value = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				name = value;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_REF) ) {
				ref = value;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_MINOCCURS) ) {
				minOccurs = value;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_MAXOCCURS) ) {
				maxOccurs = value;
			} else {
				reportGenericSchemaError (		"Invalid attribute (" +
												fStringPool.toString (attrList.getAttrName(i)) +
												")" );
			}
		}

		//
		// Global group def case
		//

		if ( fComponentStack.size() == 0 ) {
			if ( name == null ) {
				//reportSchemaError (	SchemaMessageProvider.GlobalGroupMustHaveName, null );
			}
			if ( ref != null ) {
				//reportSchemaError (	SchemaMessageProvider.GlobalGroupCannotHaveRef, null );
			}
			if ( minOccurs != null ) {
				//reportSchemaError (	SchemaMessageProvider.InvalidMinOccurs, null );
			}		
			if ( maxOccurs != null ) {
				//reportSchemaError (	SchemaMessageProvider.InvalidMaxOccurs, null );
			}

			Schema.GroupDef group = fSchemaDocument.createGroupDef ();
			group.setName (name);
			fComponentStack.push (group);
		} 
		
		//
		// Embedded group particle case
		//
		
		else {

			Schema.GroupDef group = null;
			Schema.GroupParticle particle = fSchemaDocument.createGroupParticle ();

			if ( name != null ) {
				//reportSchemaError (	SchemaMessageProvider.OnlyGlobalGroupCanHaveName, null );
			}

			if ( ref == null ) {
				group = fSchemaDocument.createGroupDef ();
			}

			//
			// Set particle minOccurs and maxOccurs
			//

			if ( minOccurs == null ) {
				particle.setMinOccurs (1);
			} else {
				particle.setMinOccurs (Integer.parseInt(minOccurs));
			}

			if ( maxOccurs == null ) {
				particle.setMaxOccurs (1);
			} else if ( maxOccurs.equals (Schema.VAL_INFINITY) ) {
				particle.setMaxOccurs (Schema.INFINITY);
			} else {
				particle.setMaxOccurs (Integer.parseInt(maxOccurs));
			}

			fComponentStack.push (particle);
			fComponentStack.push (group);
		}
	}

	//
	// </group>
	//

	private void endGroupDef () throws Exception
	{
		/*
		Schema.GroupDef group = (Schema.GroupDef)fComponentStack.pop ();

		if ( group.isNamed() ) {
			fSchemaDocument.register (group);
		} else {
			Schema.ComplexTypeDef type = (Schema.ComplexTypeDef)fComponentStack.peek ();
			type.appendParticle (group);
		}
		*/
	}

	//
	// <choice>
	//

	private void startChoice (XMLAttrList attrList, int attrListIndex)
	{
		Schema.GroupDef groupDef = (Schema.GroupDef)fComponentStack.peek();
		groupDef.setOrder (Schema.CHOICE);
	}

	//
	// </choice>
	//

	private void endChoice () { /* nothing to do */ }

	//
	// <seq>
	//

	private void startSeq (XMLAttrList attrList, int attrListIndex)
	{
		Schema.GroupDef groupDef = (Schema.GroupDef)fComponentStack.peek();
		groupDef.setOrder (Schema.SEQUENCE);
	}

	//
	// </seq>
	//

	private void endSeq () { /* nothing to do */ }

	//
	// <all>
	//

	private void startAll (XMLAttrList attrList, int attrListIndex)
	{
		Schema.GroupDef groupDef = (Schema.GroupDef)fComponentStack.peek();
		groupDef.setOrder (Schema.ALL);
	}

	//
	// </all>
	//

	private void endAll () { /* nothing to do */ }



	// **************************
	// **** ATTRIBUTE GROUPS ****
	// **************************

	//
	// <attributeGroup [name=<name> | ref=<ref>]>
	//

	private void startAttrGroupDef (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		String name = null;
		String ref = null;

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String value = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				name = value;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_REF) ) {
				ref = value;
			} else {
				reportGenericSchemaError ( "InvalidAttribute" );
			}
		}

		if ( fComponentStack.size() == 0 ) {
			if ( name != null ) {
				if ( ref == null ) {
					Schema.AttributeGroupDef attrGroup = fSchemaDocument.createAttributeGroupDef ();
					fComponentStack.push (attrGroup);
				} else {
					reportGenericSchemaError ("Global def cannot have ref");
				}
			} else {
				reportGenericSchemaError ("Global def must have name");
			}
		} else {
			if ( name == null ) {
				Schema.AttributeGroupParticle particle = fSchemaDocument.createAttributeGroupParticle ();
				if ( ref != null ) {
					// TODO: namespace
					particle.setAttributeGroupRef (ref, null);
				}
			} else {
				reportGenericSchemaError ("Non-global def cannot have name");
			}
		}
	}

	//
	// </attributeGroup>
	//

	private void endAttrGroupDef () throws Exception
	{
		if ( fComponentStack.size() == 1 ) {
			Schema.AttributeGroupDef attrGroupDef = (Schema.AttributeGroupDef)fComponentStack.pop ();
			fSchemaDocument.registerAttributeGroupDef (attrGroupDef.getName(), attrGroupDef);
		} else {
			Schema.AttributeGroupParticle particle = (Schema.AttributeGroupParticle)fComponentStack.pop ();
			Schema.AParticleHolder holder = (Schema.AParticleHolder)fComponentStack.peek ();
			holder.add (particle);
		}	
	}


	// ******************
	// **** ELEMENTS ****
	// ******************

	private void startElementDecl (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		String name = null;
		String ref = null;
		String type = null;
		String equivClass = null;
		String minOccurs = null;
		String maxOccurs = null;
		String fixedValue = null;
		String defaultValue = null;
		String isNullable = null;
		String isAbstract = null;
		String finalSet = null;
		String blockSet = null;

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String attValue = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				name = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_REF) ) {
				ref = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_TYPE) ) {
				type = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_EQUIVCLASS) ) {
				equivClass = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_MINOCCURS) ) {
				minOccurs = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_MAXOCCURS) ) {
				maxOccurs = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_DEFAULT) ) {
				defaultValue = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_FIXED) ) {
				fixedValue = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NULLABLE) ) {
				isNullable = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_ABSTRACT) ) {
				isAbstract = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_FINAL) ) {
				finalSet = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_BLOCK) ) {
				blockSet = attValue;
			} else {
				reportGenericSchemaError ("Invalid attribute for 'element'");
			}
		}

		if ( fComponentStack.size() == 0 ) {
			if ( name != null && ref == null && minOccurs == null && maxOccurs == null ) {
				Schema.ElementDecl element = fSchemaDocument.createElementDecl ();
				element.setName (name);
				if ( type != null ) element.setType (type, null);
				if ( equivClass != null ) element.setEquivClass (equivClass, null);
				if ( isNullable != null ) element.setNullable (parseBoolean(isNullable));
				if ( isAbstract != null ) element.setAbstract (parseBoolean(isAbstract));
				if ( defaultValue != null ) element.setDefaultValue (defaultValue);
				if ( fixedValue != null ) element.setFixedValue (fixedValue);
				if ( finalSet != null ) element.setFinal (parseDerivationSet(finalSet));
				if ( blockSet != null ) element.setBlock (parseBlockSet(blockSet));
				fComponentStack.push (element);
			} else {
				reportGenericSchemaError ("Invalid global 'element' declaration");
			}
		} else {
			Schema.ElementParticle particle = fSchemaDocument.createElementParticle ();

			if (equivClass == null && finalSet == null ) {
				if ( ref != null ) {
					if ( name == null && type == null ) {

					} else {
						reportGenericSchemaError ("Invalid 'element' particle");
					}

				} else {
					if ( name != null ) {
						particle.setName (name);
					} else {
						reportGenericSchemaError ("Invalid 'element' particle");
					}
				}

				if ( isNullable != null ) particle.setNullable (parseBoolean(isNullable));
				if ( isAbstract != null ) particle.setAbstract (parseBoolean(isAbstract));
				if ( defaultValue != null ) particle.setDefaultValue (defaultValue);
				if ( fixedValue != null ) particle.setFixedValue (fixedValue);

			} else {
				reportGenericSchemaError ("Invalid 'element' particle");
			}

			fComponentStack.push (particle);
		}
	}

	/*
	 * Description:
	 *
	 * The other side of starting an element.  If it's a global element
	 * decl then it will be the only thing on the component stack by this 
	 * point.  In this case, remove the element decl from the component
	 * stack and register it at the global level with the schema document.
	 *
	 * Otherwise, an element particle will be on the component stack.
	 * Remove it and add it to the element particle holder (group def 
	 * or complex type def) that should be on the stack below.
	 *
	 * Errors:
	 *
	 * A class cast exception indicatates that something on the component
	 * stack was not what we exptected it to be.
	 *
	 */

	private void endElementDecl () throws Exception
	{
		try {
			if ( fComponentStack.size() == 1 ) {

				// Global element decl

				Schema.ElementDecl element = (Schema.ElementDecl)fComponentStack.pop ();
				fSchemaDocument.registerElementDecl (element.getName(), element);

			} else {

				// Nested element decl (particle)

				Schema.ElementParticle particle = (Schema.ElementParticle)fComponentStack.pop ();
				Schema.EParticleHolder holder = (Schema.EParticleHolder)fComponentStack.peek ();
				holder.add (particle);

			}
		} catch (ClassCastException e) {
			reportGenericSchemaError ("Component stack error!");
		}
	}


	// ********************
	// **** ATTRIBUTES ****
	// ********************

	/*
	 * Description:
	 *
	 * An attribute decl can either be a global attribute decl or a an attribute 
	 * particle that is contained in either a complex type def or an attribute 
	 * group def.  An attribute particle can either be an attribute decl or a 
	 * reference to a global attribute decl.
	 *
	 * If it's a global attribute decl, we will put an attribute decl on the component
	 * stack.  Subsequent element processing may lead to adding things to this, e.g.
	 * an annotation or simple type def, before it is closed.  If it's not global, we
	 * will put an attribute particle on the component stack.  Things can be added to
	 * this as well.
	 *
	 */

	private void startAttributeDecl (XMLAttrList attrList, int attrListIndex) throws Exception
	{
		String name = null;
		String ref = null;
		String type = null;
		String use = null;
		String value = null;

		for (int i=attrList.getFirstAttr(attrListIndex); i != -1; i = attrList.getNextAttr(i) ) {

			String attValue = fStringPool.toString (attrList.getAttValue(i));

			if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_NAME) ) {
				name = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_REF) ) {
				ref = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_TYPE) ) {
				type = attValue;
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_USE) ) {
				use = attValue;	
			} else if ( attrList.getAttrName(i) == fStringPool.addSymbol(Schema.ATT_VALUE) ) {
				value = attValue;	
			} else {
				reportGenericSchemaError ("Invalid attribute for 'attribute'");
			}
		}

		if ( fComponentStack.size() == 0 ) {
			if ( name != null ) {
				if ( ref == null ) {
					Schema.AttributeDecl attribute = fSchemaDocument.createAttributeDecl ();
					// TODO: namespaces
					if ( type != null ) attribute.setSimpleType (type, null);
					if ( use != null ) attribute.setUse (parseUse(use));
					if ( value != null ) attribute.setValue (value);
					fComponentStack.push (attribute);
				} else {
					reportGenericSchemaError ("Global 'attribute' declaration may not have ref");
				}
			} else {
				reportGenericSchemaError ("Global 'attribute' declaration must have name");
			}
		} else {
			Schema.AttributeParticle particle = fSchemaDocument.createAttributeParticle ();
			if ( ref != null ) particle.setAttributeRef (ref, null);
			if ( type != null ) particle.setSimpleType (type, null);
			if ( use != null ) particle.setUse (parseUse(use));
			if ( value != null ) particle.setValue (value);
			fComponentStack.push (particle);
		}
	}


	/*
	 * Description:
	 *
	 * Translate a string value of the "use" attribute to its
	 * corresponding Schema integer representation.
	 *
	 * Errors:
	 *
	 * An invalid value for the use attribute.
	 *
	 */

	private int parseUse (String useString)  throws Exception
	{
		if ( useString.equals (Schema.VAL_OPTIONAL) ) {
			return Schema.OPTIONAL;
		} else if ( useString.equals (Schema.VAL_REQUIRED) ) {
			return Schema.REQUIRED;
		} else if ( useString.equals (Schema.VAL_DEFAULT) ) {
			return Schema.DEFAULT;
		} else if ( useString.equals (Schema.VAL_FIXED) ) {
			return Schema.FIXED;
		} else if ( useString.equals (Schema.VAL_PROHIBITED) ) {
			return Schema.PROHIBITED;
		} else {
			reportGenericSchemaError ("Invalid value for 'use'" );
			return -1;
		}
	}


	/*
	 * Description:
	 *
	 * The other side of starting an attribute.  If it's a global attribute
	 * decl then it will be the only thing on the component stack by this 
	 * point.  In this case, remove the attribute decl from the component
	 * stack and register it at the global level with the schema document.
	 *
	 * Otherwise, an attribute particle will be on the component stack.
	 * Remove it and add it to the attribute particle holder (attribute 
	 * group def or complex type def) that should be on the stack below.
	 *
	 * Errors:
	 *
	 * A class cast exception indicatates that something on the component
	 * stack was not what we exptected it to be.
	 *
	 */

	private void endAttributeDecl () throws Exception
	{
		try {

			if ( fComponentStack.size() == 1 ) {

				// Global attribute decl

				Schema.AttributeDecl attribute = (Schema.AttributeDecl)fComponentStack.pop ();
				fSchemaDocument.registerAttributeDecl (attribute.getName(), attribute);

			} else {

				// Nested attribute decl (particle)

				Schema.AttributeParticle particle = (Schema.AttributeParticle)fComponentStack.pop ();
				Schema.AParticleHolder holder = (Schema.AParticleHolder)fComponentStack.peek ();
				holder.add (particle);

			}

		} catch (ClassCastException e) {
			reportGenericSchemaError ("Component stack error!");
		}
	}



	// *******************
	// **** NOTATIONS ****
	// *******************

	private void startNotationDecl (XMLAttrList attrList, int attrListIndex)
	{
	}

	private void endNotationDecl ()
	{
		//System.out.println ("</notation>\n");
	}



	// *************************
	// **** ERROR REPORTING ****
	// *************************

	private void reportSchemaError(int major, Object args[]) throws Exception {
    	reportError(	getLocator(),
						SchemaMessageProvider.SCHEMA_DOMAIN,
						major,
						SchemaMessageProvider.MSG_NONE,
						args,
						XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR );
	}

	private void reportGenericSchemaError (String error) throws Exception {
		reportSchemaError (SchemaMessageProvider.GenericError, new Object[] { error });
	}


} // class SchemaParser
