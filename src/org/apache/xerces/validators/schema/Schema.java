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

package org.apache.xerces.validators.schema;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.validators.common.XMLValidator;


class Schema
{
	//
	// Schema Keywords
	//

    public static final String		ELT_SCHEMA			= "schema";
    public static final String		ELT_SIMPLETYPE		= "simpleType";
    public static final String		ELT_COMPLEXTYPE		= "complexType";
    public static final String		ELT_ELEMENT			= "element";
    public static final String		ELT_ATTRIBUTE		= "attribute";
    public static final String		ELT_GROUP			= "group";
	public static final String		ELT_CHOICE			= "choice";
	public static final String		ELT_SEQUENCE		= "seq";
	public static final String		ELT_ALL				= "all";
    public static final String		ELT_ATTRGROUP		= "attributeGroup";
    public static final String		ELT_NOTATION		= "notation";
    public static final String		ELT_ANY				= "any";
    public static final String		ELT_ANYATTRIBUTE	= "anyAttribute";
    public static final String		ELT_ANNOTATION		= "annotation";
    public static final String		ELT_DOCUMENTATION	= "documentation";
    public static final String		ELT_APPINFO			= "appinfo";

    public static final String		ELT_MINEXCLUSIVE	= "minExclusive";
    public static final String		ELT_MININCLUSIVE	= "minInclusive";
    public static final String		ELT_MAXEXCLUSIVE	= "maxExclusive";
    public static final String		ELT_MAXINCLUSIVE	= "maxInclusive";
    public static final String		ELT_PATTERN			= "pattern";
    public static final String		ELT_ENUMERATION		= "enumeration";
    public static final String		ELT_PRECISION		= "precision";
    public static final String		ELT_SCALE			= "scale";
    public static final String		ELT_LENGTH			= "length";
    public static final String		ELT_MINLENGTH		= "minLength";
    public static final String		ELT_MAXLENGTH		= "maxLength";
    public static final String		ELT_ENCODING		= "encoding";
    public static final String		ELT_PERIOD			= "period";

    public static final String		ATT_TARGETNAMESPACE	= "targetNamespace";
    public static final String		ATT_IMPORT			= "import";
    public static final String		ATT_INCLUDE			= "include";
    public static final String		ATT_NAME			= "name";
    public static final String		ATT_TYPE			= "type";
    public static final String		ATT_REF				= "ref";
    public static final String		ATT_CONTENT			= "content";
    public static final String		ATT_ABSTRACT		= "abstract";
	public static final String		ATT_NULLABLE		= "nullable";
	public static final String		ATT_DEFAULT			= "default";
    public static final String		ATT_FIXED			= "fixed";
    public static final String		ATT_FINAL			= "final";
    public static final String		ATT_BLOCK			= "block";
    public static final String		ATT_BASE			= "base";
    public static final String		ATT_DERIVEDBY		= "derivedBy";
    public static final String		ATT_MINOCCURS		= "minOccurs";
    public static final String		ATT_MAXOCCURS		= "maxOccurs";
    public static final String		ATT_EQUIVCLASS		= "equivClass";
    public static final String		ATT_VALUE			= "value";
	public static final String		ATT_USE				= "use";
	public static final String		ATT_SYSTEM			= "system";
	public static final String		ATT_PUBLIC			= "public";

    public static final String		VAL_TRUE			= "true";
    public static final String		VAL_FALSE			= "false";
    public static final String		VAL_EMPTY			= "empty";
    public static final String		VAL_ELEMENTONLY		= "elementOnly";
    public static final String		VAL_TEXTONLY		= "textOnly";
    public static final String		VAL_MIXED			= "mixed";
    public static final String		VAL_EXTENSION		= "extension";
    public static final String		VAL_RESTRICTION		= "restriction";
    public static final String		VAL_REPRODUCTION	= "reproduction";
    public static final String		VAL_ENUMERATION		= "enumeration";
    public static final String		VAL_EQUIVCLASS		= "equivClass";
    public static final String		VAL_LIST			= "list";
    public static final String		VAL_POUNDALL		= "#all";
	public static final String		VAL_INFINITY		= "*";

	public static final String		VAL_OPTIONAL		= "optional";
	public static final String		VAL_REQUIRED		= "required";
	public static final String		VAL_DEFAULT			= "default";
	public static final String		VAL_FIXED			= "fixed";
	public static final String		VAL_PROHIBITED		= "prohibited";


	//
	// Schema Representation Data Values
	//

	static public final int			EMPTY = 0;			// content models
	static public final int			ELEMENT_ONLY = 1;	// content models
	static public final int			TEXT_ONLY = 2;		// content models
	static public final int			MIXED = 3;			// content models

	public static final int			EMPTY_SET = 0;		
	public static final int			EXTENSION = 1;
	public static final int			RESTRICTION = 2;
	public static final int			REPRODUCTION = 4;
	public static final int			LIST = 8;
	public static final int			ENUMERATION = 16;
	public static final int			EQUIVCLASS = 32;

	public static final int			CHOICE = 0;			// group orders
	public static final int			SEQUENCE = 1;		// group orders
	public static final int			ALL = 2;			// group orders

	public static final int			OPTIONAL = 0;
	public static final int			REQUIRED = 1;
	public static final int			DEFAULT = 2;
	public static final int			FIXED = 3;
	public static final int			PROHIBITED = 4;

	public static final int			INFINITY = -1;		// used for maxOccurs


	//
	// Helpers for toString methods
	//

	private static final String[]	CONTENT_MAP = { 
										VAL_EMPTY, 
										VAL_ELEMENTONLY, 
										VAL_TEXTONLY, 
										VAL_MIXED
									};

	private static final String[]	ORDER_MAP = { 
										ELT_CHOICE, 
										ELT_SEQUENCE, 
										ELT_ALL 
									};

	private static final String[]	USE_MAP = { 
										VAL_OPTIONAL, 
										VAL_REQUIRED, 
										VAL_DEFAULT, 
										VAL_FIXED, 
										VAL_PROHIBITED 
									};


	private static final String[]	DERIVEDBY_MAP = {
										null,
										VAL_EXTENSION,		// 1
										VAL_RESTRICTION,	// 2
										null,
										VAL_REPRODUCTION,	// 4
										null, null, null,
										VAL_LIST,			// 8
										null, null, null, null, null, null, null,
										VAL_ENUMERATION		// 16
									};


	//
	// Schema Instance Data
	//

	private Hashtable				fDocuments = new Hashtable ();
	private Document				fDefaultDocument = null;
	private XMLValidator			fValidator = null;
	private XMLErrorReporter		fErrorReporter = null;


	//
	// Constructor
	//

	public Schema (XMLErrorReporter errorReporter, XMLValidator validator) {
		fErrorReporter = errorReporter;
		fValidator = validator;
	}


	//
	// Create a new Schema Document
	//

	public Document createDocument () {
		return new Document ();
	}


	//
	// Register a Schema document (associate it with a unique namespace)
	//

	public void registerDocument (String namespace, Document document)
	{
		if ( namespace == null ) {
			fDefaultDocument = document;
		} else {
			fDocuments.put (namespace, document);
		}
	}


	//
	// Lookup a Schema document (given it's unique namespace)
	//

	public Document lookupDocument (String namespace) {
		if ( namespace == null ) {
			return fDefaultDocument;
		} else {
			return (Document)fDocuments.get (namespace);
		}
	}

	//
	// Debugging
	//

	public String toString () 
	{
		String s = "";

		for ( Enumeration e = fDocuments.keys (); e.hasMoreElements();) {
			String namespace = (String)e.nextElement ();
			Document doc = (Document)fDocuments.get (namespace);
			s += namespace + ":\n";
			s += doc.toString () + "\n";
		}

		return s;
	}


	//****************************************************************************************
	// Schema Interfaces
	//
	//****************************************************************************************


	public interface HasSimpleType
	{
		public void setSimpleType (SimpleTypeDef type) throws Exception;
		public void setSimpleType (String name, String namespace) throws Exception;
	}


	public interface HasType extends HasSimpleType
	{
		public void setType (TypeDef type) throws Exception;
		public void setType (String name, String namespace) throws Exception;
	}



	//****************************************************************************************
	// Schema Classes
	//
	//****************************************************************************************

	//********************
	//**** Schema.Ref ****
	//********************

	public abstract class Ref
	{
		//
		// Getters
		//

		public abstract String				getName ();
		public abstract String				getNamespace ();

		public String getQName () {
			String namespace = getNamespace ();
			String qname = namespace == null? "" : namespace + ":";
			qname += getName();
			return qname;
		}

		//
		// Ref Resolution
		//

		public abstract TypeDef				resolveTypeDef ();
		public abstract GroupDef			resolveGroupDef ();
		public abstract AttributeGroupDef	resolveAttributeGroupDef ();
		public abstract ElementDecl			resolveElementDecl ();
		public abstract AttributeDecl		resolveAttributeDecl ();
		public abstract NotationDecl		resolveNotationDecl ();
	}

	//*************************
	//**** Schema.LocalRef ****
	//*************************

	public class LocalRef extends Ref
	{
		private Component		fComponent;

		//
		// Constructors
		//

		public LocalRef (Component component) {
			fComponent = component;
		}

		//
		// Getters
		//

		public String getName () {
			return fComponent.getName ();
		}

		public String getNamespace () {
			return fComponent.getNamespace ();
		}

		//
		// Ref Resolution
		//

		public TypeDef resolveTypeDef () {
			return (TypeDef)fComponent;
		}

		public GroupDef resolveGroupDef () {
			return (GroupDef)fComponent;
		}

		public AttributeGroupDef resolveAttributeGroupDef () {
			return (AttributeGroupDef)fComponent;
		}

		public ElementDecl resolveElementDecl () {
			return (ElementDecl)fComponent;
		}

		public AttributeDecl resolveAttributeDecl () {
			return (AttributeDecl)fComponent;
		}

		public NotationDecl resolveNotationDecl () {
			return (NotationDecl)fComponent;
		}

	}

	//*************************
	//**** Schema.NamedRef ****
	//*************************

	public class NamedRef extends Ref
	{
		private String			fName;
		private String			fNamespace;

		//
		// Constructors
		//

		public NamedRef (String name, String namespace) {
			fName = name;
			fNamespace = namespace;
		}

		//
		// Getters
		//

		public String getName () {
			return fName;
		}

		public String getNamespace () {
			return fNamespace;
		}

		//
		// Ref Resolution
		//

		public TypeDef resolveTypeDef () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupTypeDef (fName);
		}

		public GroupDef resolveGroupDef () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupGroupDef (fName);
		}

		public AttributeGroupDef resolveAttributeGroupDef () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupAttributeGroupDef (fName);
		}

		public ElementDecl resolveElementDecl () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupElementDecl (fName);
		}

		public AttributeDecl resolveAttributeDecl () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupAttributeDecl (fName);
		}

		public NotationDecl resolveNotationDecl () {
			Document doc = lookupDocument (fNamespace);
			return doc.lookupNotationDecl (fName);
		}
	}

	//***************************
	//**** Schema.Annotation ****
	//***************************

	public class Annotation 
	{
		private Vector			fDocumentation = null;
		private Vector			fAppInfo = null;

		public void addDocumentation (String documentation) {
			if ( fDocumentation == null ) fDocumentation = new Vector ();
			fDocumentation.addElement (documentation);
		}

		public void addAppInfo (String appInfo) {
			if ( fAppInfo == null ) fAppInfo = new Vector ();
			fAppInfo.addElement (appInfo);
		}

		public String toString () {
			String s = "<" + ELT_ANNOTATION + ">\n";

			if ( fDocumentation != null ) {
				for (int i=0; i<fDocumentation.size(); i++) {
					String doc = (String)fDocumentation.elementAt (i);
					s += "  <" + ELT_DOCUMENTATION + ">" + doc + "</" + ELT_DOCUMENTATION + ">\n";
				}
			}

			if ( fAppInfo != null ) {
				for (int i=0; i<fAppInfo.size(); i++) {
					String appInfo = (String)fAppInfo.elementAt (i);
					s += "  <" + ELT_APPINFO + ">" + appInfo + "</" + ELT_APPINFO + ">\n";
				}
			}

			s += "</" + ELT_ANNOTATION + ">\n";

			return s;
		}
	}

	//**************************
	//**** Schema.Component ****
	//**************************

	public class Component 
	{
		private Document	fDocument = null;
		private String		fName = null;

		public Component (Document doc) {
			fDocument = doc;
		}

		//
		// Setters
		//

		public void setName (String name) {
			fName = name;
		}

		//
		// Getters
		//

		public String getName () {
			return fName;
		}

		public String getQName () {
			String namespace = getNamespace ();
			String name = getName ();
			if ( namespace != null ) {
				return namespace + ":" + name;
			} else {
				return name;
			}
		}

		public Document getDocument () {
			return fDocument;
		}

		public String getNamespace () {
			return fDocument.getNamespace ();
		}
	}

	//***********************************
	//**** Schema.AnnotatedComponent ****
	//***********************************

	public class AnnotatedComponent extends Component
	{
		private Annotation		fAnnotation = null;

		public AnnotatedComponent (Document doc) {
			super (doc);
		}

		public void setAnnotation (Annotation annotation) {
			fAnnotation = annotation;
		}

		public Annotation getAnnotation () {
			return fAnnotation;
		}

		public String toString () {
			return annotationToString ();
		}

		public String annotationToString () {
			if ( fAnnotation != null ) {
				return fAnnotation.toString ();
			} else {
				return "";
			}
		}
	}

	//*************************
	//**** Schema.Document ****
	//*************************

	public class Document
	{
		// ISSUE:	Should Schemas have multiple annotations?
		//			How is order and relationship to interspersed
		//			components maintained?

		private String			fNamespace = null;
		private Vector			fAnnotations = new Vector ();
		private Hashtable		fTypeDefs = new Hashtable ();
		private Hashtable		fGroupDefs = new Hashtable ();
		private Hashtable		fAttrGroupDefs = new Hashtable ();
		private Hashtable		fElementDecls = new Hashtable ();
		private Hashtable		fAttributeDecls = new Hashtable ();
		private Hashtable		fNotationDecls = new Hashtable ();

		//
		// Getters
		//

		public String getNamespace () {
			return fNamespace;
		}

		//
		// Factory methods for Schema components
		//

		public Annotation createAnnotation () {
			return new Annotation ();
		}

		public ComplexTypeDef createComplexTypeDef () {
			return new ComplexTypeDef (this);
		}

		public SimpleTypeDef createSimpleTypeDef () {
			return new SimpleTypeDef (this);
		}

		public Facet createFacet (String name, String value) {
			return new Facet (name, value, this);
		}

		public GroupDef createGroupDef () {
			return new GroupDef (this);
		}

		public AttributeGroupDef createAttributeGroupDef () {
			return new AttributeGroupDef (this);
		}

		public ElementDecl createElementDecl () {
			return new ElementDecl (this);
		}

		public AttributeDecl createAttributeDecl () {
			return new AttributeDecl (this);
		}

		public NotationDecl createNotationDecl () {
			return new NotationDecl (this);
		}

		public AttributeGroupParticle createAttributeGroupParticle () {
			return new AttributeGroupParticle (this);
		}

		public AttributeParticle createAttributeParticle () {
			return new AttributeParticle (this);
		}

		public GroupParticle createGroupParticle () {
			return new GroupParticle (this);
		}

		public ElementParticle createElementParticle () {
			return new ElementParticle (this);
		}

		//
		// Annotations
		//

		public void addAnnotation (Annotation annotation) {
			fAnnotations.addElement (annotation);
		}


		//
		// Add Schema components to their respective symbol spaces in this
		// Schema document.
		//

		public void registerType (String name, TypeDef typeDef) {
			fTypeDefs.put (name, typeDef);
		}

		public void registerGroupDef (String name, GroupDef groupDef) {
			fGroupDefs.put (name, groupDef);
		}

		public void registerAttributeGroupDef (String name, AttributeGroupDef attrGroupDef) {
			fAttrGroupDefs.put (name, attrGroupDef);
		}

		public void registerElementDecl (String name, ElementDecl elementDecl) {
			fElementDecls.put (name, elementDecl);
		}

		public void registerAttributeDecl (String name, AttributeDecl attributeDecl) {
			fAttributeDecls.put (name, attributeDecl);
		}

		public void registerNotationDecl (String name, NotationDecl notationDecl) {
			fNotationDecls.put (name, notationDecl);
		}

		//
		// Lookup Schema Components
		//

		public TypeDef lookupTypeDef (String name) {
			return (TypeDef)fTypeDefs.get (name);
		}

		public GroupDef lookupGroupDef (String name) {
			return (GroupDef)fGroupDefs.get (name);
		}

		public AttributeGroupDef lookupAttributeGroupDef (String name) {
			return (AttributeGroupDef)fAttrGroupDefs.get (name);
		}

		public ElementDecl lookupElementDecl (String name) {
			return (ElementDecl)fElementDecls.get (name);
		}

		public AttributeDecl lookupAttributeDecl (String name) {
			return (AttributeDecl)fAttributeDecls.get (name);
		}

		public NotationDecl lookupNotationDecl (String name) {
			return (NotationDecl)fNotationDecls.get (name);
		}

		//
		// Validation
		//

		public void expandEquivClassReferences () {
			for ( Enumeration e = fElementDecls.elements (); e.hasMoreElements();) {
				ElementDecl element = (ElementDecl)e.nextElement ();
				ElementDecl equivClass = element.getEquivClass ();
				if ( equivClass != null ) {
					equivClass.addEquivElementDecl (element);
				}
			}
		}

		//
		// Debugging
		//

		public String toString () 
		{

			String s = "<" + ELT_SCHEMA;

			// Target Namespace

			if ( fNamespace != null ) {
				s += " " + ATT_TARGETNAMESPACE + "=" + fNamespace;
			}

			s += ">\n";


			// Global TypeDefs

			for ( Enumeration e = fTypeDefs.elements (); e.hasMoreElements();) {
				TypeDef t = (TypeDef)e.nextElement ();
				s += t.toString ();
			}

			// Global GroupDefs

			for ( Enumeration e = fGroupDefs.elements (); e.hasMoreElements();) {
				GroupDef g = (GroupDef)e.nextElement ();
				s += g.toString ();
			}

			// Global AttributeGroupDefs

			for ( Enumeration e = fAttrGroupDefs.elements (); e.hasMoreElements();) {
				AttributeGroupDef ag = (AttributeGroupDef)e.nextElement ();
				s += ag.toString ();
			}

			// Global ElementDecls

			for ( Enumeration e = fElementDecls.elements (); e.hasMoreElements();) {
				ElementDecl elem = (ElementDecl)e.nextElement ();
				s += elem.toString ();
			}

			// Global AttributeDecls

			for ( Enumeration e = fAttributeDecls.elements (); e.hasMoreElements();) {
				AttributeDecl attr = (AttributeDecl)e.nextElement ();
				s += attr.toString ();
			}

			// Global NotationDecls

			for ( Enumeration e = fNotationDecls.elements (); e.hasMoreElements();) {
				NotationDecl not = (NotationDecl)e.nextElement ();
				s += not.toString ();
			}
			s += "</" + ELT_SCHEMA + ">\n";

			return s;
		}

	}

	//************************
	//**** Schema.TypeDef ****
	//************************

	public abstract class TypeDef extends AnnotatedComponent
	{
		private Ref				fBaseType = null;
		private int				fDerivedBy = 0;
		private boolean			fIsAbstract = false;
		private int				fFinalSet = EMPTY_SET;

		//
		// Constructor
		//

		public TypeDef (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setBase (String name, String namespace) {
			fBaseType = new NamedRef (name, namespace);
		}

		public void setDerivedBy (int derivedBy) {
			fDerivedBy = derivedBy;
		}

		public void setAbstract (boolean isAbstract) {
			fIsAbstract = isAbstract;
		}

		public void setFinal (int finalSet) {
			fFinalSet = finalSet;
		}
	
		//
		// Getters
		//

		public TypeDef getBaseType () {
			return fBaseType.resolveTypeDef ();
		}

		public int getDerivedBy () {
			return fDerivedBy;
		}

		public boolean isDerivedBy (int derivedBy) {
			return fDerivedBy == derivedBy;
		}

		public boolean isAbstract () {
			return fIsAbstract;
		}

		public int getFinal () {
			return fFinalSet;
		}

		public boolean canExtend () {
			return (getFinal() & EXTENSION) == 0;
		} 

		public boolean canRestrict () {
			return (getFinal() & RESTRICTION) == 0;
		}

		public boolean canReproduce () {
			return (getFinal() & REPRODUCTION) == 0;
		}

		public boolean canEnumerate () {
			return (getFinal() & ENUMERATION) == 0;
		} 

		public boolean canList () {
			return (getFinal() & LIST) == 0;
		} 

		//
		// Debugging
		//

		public String toString () {
		
			// This is an abstract base class for ComplexTypeDef and SimpleTypeDef
			// so its string representation is just a portion of either of these.
			// In particular, toString produces a portion of the attribute string
			// for either.

			String s = " ";

			// base

			if ( fBaseType != null ) {
				s += " " + ATT_BASE + "=\'" + fBaseType.getQName() + "\'";
			}

			// derivedBy

			s += " " + ATT_DERIVEDBY + "=\'" + DERIVEDBY_MAP[getDerivedBy()] + "\'";

			// abstract
			
			s += " " + ATT_ABSTRACT + "=";
			s += isAbstract()? "\'" + VAL_TRUE + "\'" : "\'" + VAL_FALSE + "\'";
			
			// final

			s += " " + ATT_FINAL + "=\'";

			boolean extend = canExtend ();
			boolean restrict = canRestrict ();
			boolean reproduce = canReproduce ();
			boolean enumerate = canEnumerate ();
			boolean list = canList ();

			if ( (!extend && !restrict && !reproduce) || (!enumerate && !list && !restrict && !reproduce)) {
				s += "#all";
			} else {
				if ( !extend ) {
					s += " " + VAL_EXTENSION;
				}
				if ( !restrict ) {
					s += " " + VAL_RESTRICTION;
				}
				if ( !reproduce ) {
					s += " " + VAL_REPRODUCTION;
				}
				if ( !enumerate ) {
					s += " " + VAL_ENUMERATION;
				}
				if ( !list ) {
					s += " " + VAL_LIST;
				}
			}

			s += "\'";

			return s;
		}
	}


	//********************
	//**** INTERFACES ****
	//********************

	public interface AParticleHolder
	{
		public void add (AParticle particle);
	}

	public interface EParticleHolder
	{
		public void add (EParticle particle);
		public void setOrder (int order);
	}


	//*******************************
	//**** Schema.ComplexTypeDef ****
	//*******************************

	public class ComplexTypeDef		extends TypeDef 
									implements EParticleHolder, AParticleHolder
	{
		private int					fContent = EMPTY;
		private int					fBlockSet = EMPTY_SET;
		private AttributeGroupDef	fAttrGroupDef = null;
		private GroupDef			fGroupDef = null;

		//
		// Constructor
		//

		public ComplexTypeDef (Document doc) {
			super (doc);
			setDerivedBy (EXTENSION);
		}

		//
		// Setters
		//

		public void setContent (int content) {
			fContent = content;
		}
		
		public void setBlock (int blockSet) {
			fBlockSet = blockSet;
		}

		public void add (EParticle particle) {
			if ( fGroupDef == null ) fGroupDef = new GroupDef (getDocument());
			fGroupDef.add (particle);
		}

		public void setOrder (int order) {
			fGroupDef.setOrder (order);
		}

		public void add (AParticle particle) {
			if ( fAttrGroupDef == null ) fAttrGroupDef = new AttributeGroupDef (getDocument());
			fAttrGroupDef.add (particle);
		}
		
		//
		// Getters
		//

		public int getContent () {
			return fContent;
		}

		public boolean isContent (int content) {
			return fContent == content;
		}

		public boolean canSubstituteExtension () {
			return (fBlockSet & EXTENSION) == 0;
		} 

		public boolean canSubstituteRestriction () {
			return (fBlockSet & RESTRICTION) == 0;
		}

		public boolean canSubstituteReproduction () {
			return (fBlockSet & REPRODUCTION) == 0;
		}

		//
		// Debugging
		//

		public String toString () {

			String s = "<" + ELT_COMPLEXTYPE + " ";
			
			// name
			
			String name = getName ();
			if ( name != null ) {
				s += ATT_NAME + "='" + getName() + "'";
			}

			s += super.toString ();

			// content

			s += " " + ATT_CONTENT + "='" + CONTENT_MAP[fContent] + "'";


			// block

			s += " " + ATT_BLOCK + "='";

			boolean extend = canSubstituteExtension ();
			boolean restrict = canSubstituteRestriction ();
			boolean reproduce = canSubstituteReproduction ();

			if ( !extend && !restrict && !reproduce ) {
				s += VAL_POUNDALL;
			} else {
				if ( !extend ) {
					s += " " + VAL_EXTENSION;
				}
				if ( !restrict ) {
					s += " " + VAL_RESTRICTION;
				}
				if ( !reproduce ) {
					s += " " + VAL_REPRODUCTION;
				}
			}

			s += "'>\n";

			// annotation

			s += annotationToString ();

			// particles

			if ( fGroupDef != null ) {
				s += fGroupDef.toString ();
			}

			// attributes

			if ( fAttrGroupDef != null ) {
				s += fAttrGroupDef.toString ();
			}

			s += "</" + ELT_COMPLEXTYPE + ">\n";

			return s;
		}

	}

	//******************************
	//**** Schema.SimpleTypeDef ****
	//******************************

	public class SimpleTypeDef extends TypeDef
	{
		private Vector			fFacets = null;

		//
		// Constructor
		//

		public SimpleTypeDef (Document doc) {
			super (doc);
			setDerivedBy (RESTRICTION);
		}

		//
		// Setters
		//
						
		public void addFacet (Facet facet) {
			if ( fFacets == null ) fFacets = new Vector ();
			fFacets.addElement (facet);
		}

		//
		// Debugging
		//

		public String toString () {

			String s = "<" + ELT_SIMPLETYPE;
			
			// name
			
			String name = getName ();
			if ( name != null ) {
				s += " " + ATT_NAME + "=\'" + getName() + "\'";
			}

			s += super.toString ();

			s += ">\n";
			
			// annotations

			s += annotationToString ();

			// facets

			if ( fFacets != null ) {
				int numFacets = fFacets.size();
				for (int f=0; f<numFacets; f++) {
					Facet facet = (Facet)fFacets.elementAt (f);
					s += facet.toString ();
				}
			}

			s += "</" + ELT_SIMPLETYPE + ">\n";

			return s;
		}

	}

	//**********************
	//**** Schema.Facet ****
	//**********************

	public class Facet extends AnnotatedComponent
	{
		private String	fValue = null;

		public Facet (String name, String value, Document doc) {
			super (doc);
			setName (name);
			fValue = value;
		}

		public String getValue () {
			return fValue;
		}

		//
		// Debugging
		//

		public String toString () {
			String name = getName ();
			String s = "<" + name + " " + ATT_VALUE + "='" + fValue + "'" + ">\n";
			s += annotationToString ();
			s += "</" + name + ">\n";
			return s;
		}
	}

	//******************************
	//**** Schema.AttributeDecl ****
	//******************************

	public class AttributeDecl	extends		AnnotatedComponent
								implements	HasSimpleType
	{
		private Ref				fTypeDef = null;
		private int				fUse = OPTIONAL;
		private String			fValue = null;

		//
		// Constructor
		//

		public AttributeDecl (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setSimpleType (SimpleTypeDef typeDef) throws Exception {
			if ( fTypeDef == null ) {
				fTypeDef = new LocalRef (typeDef);
			} else {
				reportGenericSchemaError ("Type def already set");
			}
		}
		
		public void setSimpleType (String name, String namespace) throws Exception {
			if ( fTypeDef == null ) {
				fTypeDef = new NamedRef (name, namespace);
			} else {
				reportGenericSchemaError ("Type def already set");
			}
		}

		public void setUse (int use) {
			fUse = use;
		}

		public void setValue (String value) {
			fValue = value;
		}

		//
		// Getters
		//

		public SimpleTypeDef getType () {
			return (SimpleTypeDef)fTypeDef.resolveTypeDef ();
		}

		public boolean hasTypeRef () {
			return NamedRef.class.isInstance(fTypeDef);
		}

		public int getUse () {
			return fUse;
		}

		public String getValue () {
			return fValue;
		}

		public AttributeDecl createRestriction (AttributeDecl restrictions) {

			// TODO: refine what is a valid restriction

			AttributeDecl attrDecl = new AttributeDecl (getDocument());
			attrDecl.setName(getName());
			attrDecl.fTypeDef = fTypeDef;
			attrDecl.fUse = restrictions.fUse;
			attrDecl.fValue = (restrictions.fValue == null)? fValue : restrictions.fValue;
			return attrDecl;
		}

		/*
		public void collectAttributes (Hashtable attributes, boolean restrict) {
			String name = getName ();
			AttributeDecl attrDecl = (AttributeDecl)attributes.get (name);
			if ( attrDecl == null ) {
				attributes.put (name, this);
			} else {
				attributes.put (name, attrDecl.createRestriction(this));
			}
		}
		*/


		//
		// Debugging
		//

		public String toString () {

			String s = "<" + ELT_ATTRIBUTE;

			String name = getName ();
			if ( name != null ) {
				s += " " + ATT_NAME + "='" + name + "'";
			}

			if ( hasTypeRef() ) {
				s += " " + ATT_TYPE + "='" + getType().getQName() + "'";
			}

			s += " " + ATT_USE + "='" + USE_MAP[fUse] + "'";

			if ( fValue != null ) {
				s += " " + ATT_VALUE + "='" + fValue + "'";
			}

			s += ">\n";
			
			s += annotationToString();
			s += getType().toString ();
						
			s += "</" + ELT_ATTRIBUTE + ">\n";
			return s;
		}
	}

	//**********************************
	//**** Schema.AttributeGroupDef ****
	//**********************************

	public class AttributeGroupDef		extends AnnotatedComponent
										implements AParticleHolder
	{
		private Vector fParticles = new Vector ();

		//
		// Constructor
		//

		public AttributeGroupDef (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void add (AParticle particle) {
			fParticles.addElement (particle);
		}

		//
		// Getters
		//

		/*
		public void collectAttributes (Hashtable attributes, boolean restrict) {
			for (int i=0; i<fParticles.size(); i++) {
				AttributeCollector collector = (AttributeCollector)fParticles.elementAt (i);
				collector.collectAttributes (attributes, restrict);
			}
		}
		*/

		//
		// Debugging
		//

		public String toString () {
			String s = "<" + ELT_ATTRGROUP;
			String name = getName ();
			if ( name != null ) {
				s += " " + ATT_NAME + "='" + name + "'";
			}
			s += ">\n";
			s += annotationToString ();
			s += bodyToString ();
			s += "</" + ELT_ATTRGROUP + ">\n";
			return s;
		}

		public String bodyToString () {
			String s = "";
			for (int i=0; i<fParticles.size(); i++) {
				Object particle = fParticles.elementAt (i);
				s += particle.toString ();
			}
			return s;
		}
	}

	//****************************
	//**** Schema.ElementDecl ****
	//****************************

	public class ElementDecl	extends		AnnotatedComponent
								implements	HasType
	{
		private Ref				fTypeDef = null;
		private Ref				fEquivClass = null;
		private Vector			fEquivElementDecls = null;

		private boolean			fIsAbstract = false;
		private boolean			fIsNullable = false;
		private int				fFinalSet = EMPTY_SET;						// equiv class
		private int				fBlockSet = EMPTY_SET;						// substitutions
		private int				fValueConstraint = OPTIONAL;
		private String			fValue = null;

		//
		// Constructor
		//

		public ElementDecl (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setType (String name, String namespace) throws Exception {
			if ( fTypeDef == null ) {
				fTypeDef = new NamedRef (name, namespace);
			} else {
				reportGenericSchemaError ("Type def already set");
			}
		}

		public void setType (TypeDef typeDef) throws Exception {
			if ( fTypeDef == null ) {
				fTypeDef = new LocalRef (typeDef);
			} else {
				reportGenericSchemaError ("Type def already set");
			}
		}

		public void setSimpleType (SimpleTypeDef typeDef) throws Exception {
			setType (typeDef);
		}

		public void setSimpleType (String name, String namespace) throws Exception {
			setType (name, namespace);
		}

		public void setEquivClass (String name, String namespace) {
			fEquivClass = new NamedRef (name, namespace);
		}
		
		public void setAbstract (boolean isAbstract) {
			fIsAbstract = isAbstract;
		}

		public void setNullable (boolean isNullable) {
			fIsNullable = isNullable;
		}

		public void setFinal (int finalSet) {
			fFinalSet = finalSet;
		}

		public void setBlock (int blockSet) {
			fBlockSet = blockSet;
		}

		public void setDefaultValue (String defaultValue) {
			fValueConstraint = DEFAULT;
			fValue = defaultValue;
		}
		
		public void setFixedValue (String fixedValue) {
			fValueConstraint = DEFAULT;
			fValue = fixedValue;
		}

		//
		// Getters
		//

		public TypeDef getType () {
			return fTypeDef.resolveTypeDef ();
		}

		public boolean hasTypeRef () {
			return NamedRef.class.isInstance(fTypeDef);
		}

		public ElementDecl getEquivClass () {
			return fEquivClass.resolveElementDecl ();
		}

		public boolean isNullable () {
			return fIsNullable;
		}

		public boolean isAbstract () {
			return fIsAbstract;
		}

		public String getDefaultValue () {
			if ( fValueConstraint == DEFAULT ) {
				return fValue;
			} else {
				return null;
			}
		}

		public String getFixedValue () {
			if ( fValueConstraint == FIXED ) {
				return fValue;
			} else {
				return null;
			}
		}

		//
		// Validation Support
		//

		public void addEquivElementDecl (ElementDecl elementDecl) {
			fEquivElementDecls.addElement (elementDecl);
		}


		//
		// Debugging
		//

		//
		// Debugging
		//

		public String toString () {

			String s = "< " + ELT_ELEMENT;

			String name = getName ();
			if ( name != null ) {
				s += " " + ATT_NAME + "='" + name + "'";
			}

			if ( hasTypeRef() ) {
				s += " " + ATT_TYPE + "='" + getType().getQName() + "'";
			} 

			s += " " + ATT_ABSTRACT + "='" + (isAbstract()? "true'" : "false") + "'";
			s += " " + ATT_NULLABLE + "='" + (isNullable()? "true'" : "false") + "'";

			// default value

			String defaultValue = getDefaultValue ();
			if ( defaultValue != null ) {
				s += " " + ATT_DEFAULT + "='" + defaultValue + "'";
			}

			// fixed value

			String fixedValue = getFixedValue ();
			if ( fixedValue != null ) {
				s += " " + ATT_FIXED + "='" + fixedValue + "'";
			}

			// equiv class

			if ( fEquivClass != null ) {
				s += " " + ATT_EQUIVCLASS + "='" + fEquivClass.getQName () + "'";
			}

			s += annotationToString ();

			// anonymous

			if ( !hasTypeRef() ) {
				getType().toString ();
			}

			return s;

		}

	}

	//*************************
	//**** Schema.GroupDef ****
	//*************************

	public class GroupDef		extends AnnotatedComponent
								implements EParticleHolder
	{
		private int				fOrder = CHOICE;
		private Vector			fParticles = new Vector ();


		//
		// Constructor
		//

		public GroupDef (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setOrder (int order) {
			fOrder = order;
		}

		public void add (EParticle particle) {
			fParticles.addElement (particle);
		}

		//
		// Getters
		//

		public int getOrder () {
			return fOrder;
		}
		
		/*
		public void collectParticles (Hashtable particles, boolean restrict) {
			for (int i=0; i<fParticles.size(); i++) {
				ParticleCollector collector = (ParticleCollector)fParticles.elementAt (i);
				collector.collectParticles (particles, restrict);
			}
		}
		*/

		//
		// Debugging
		//

		public String toString () {
			String s = "<" + ELT_GROUP;
			String name = getName ();
			if ( name != null ) {
				s += " " + ATT_NAME + "='" + name + "'";
			}	
			s += ">\n";
			s += annotationToString ();
			s += bodyToString ();
			s += "</" + ELT_GROUP + ">\n";
			return s;
		}

		public String bodyToString () {
			String s = "<" + ORDER_MAP[fOrder] + ">\n";
			for (int i=0; i<fParticles.size(); i++) {
				Object particle = fParticles.elementAt (i);
				s += particle.toString ();
			}
			s += "</" + ORDER_MAP[fOrder] + ">\n";
			return s;
		}
	}


	//*****************************
	//**** Schema.NotationDecl ****
	//*****************************

	public class NotationDecl extends AnnotatedComponent
	{
		private String		fSystemId = null;
		private String		fPublicId = null;

		//
		// Constructors
		//

		public NotationDecl (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setSystemId (String systemId) {
			fSystemId = systemId;
		}

		public void setPublicId (String publicId) {
			fPublicId = publicId;
		}

		//
		// Getters
		//

		public String getSystemId () {
			return fSystemId;
		}

		public String getPublicId () {
			return fPublicId;
		}

		//
		// Debugging
		//

		public String toString () {
			String s = "<" + ELT_NOTATION;
			s += " " + ATT_NAME + "='" + getName() + "'";
			s += " " + ATT_SYSTEM + "='" + getSystemId() + "'";
			s += " " + ATT_PUBLIC + "='" + getPublicId() + "'";
			s += ">\n";
			s += annotationToString ();
			s += "</" + ELT_NOTATION + ">\n";
			return s;
		}

	}


	//**************************
	//**** Schema.AParticle ****
	//**************************

	public abstract class AParticle extends AnnotatedComponent
	{
		private Ref			fComponentRef = null;

		//
		// Constructors
		//

		public AParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		protected void setComponentRef (Ref componentRef) {
			fComponentRef = componentRef;
		}

		//
		// Getters
		//

		protected Ref getComponentRef () {
			return fComponentRef;
		}

		public String getName () {
			return fComponentRef.getName ();
		}

		public String getNamespace () {
			return fComponentRef.getNamespace ();
		}

		// Does this particle reference a global component?

		public boolean isRef () {
			return NamedRef.class.isInstance (fComponentRef);
		}
	}

	//***************************************
	//**** Schema.AttributeGroupParticle ****
	//***************************************

	public class AttributeGroupParticle		extends AParticle 
											implements AParticleHolder
	{
		//
		// Constructors
		//

		public AttributeGroupParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setAttributeGroupDef (AttributeGroupDef attrGroupDef) {
			setComponentRef (new LocalRef(attrGroupDef));
		}

		public void setAttributeGroupRef (String name, String namespace) {
			setComponentRef (new NamedRef(name, namespace));
		}

		public void add (AParticle particle) {
			getAttributeGroupDef().add (particle);
		}

		//
		// Getters
		//

		public AttributeGroupDef getAttributeGroupDef () {
			Ref ref = getComponentRef ();
			if ( ref == null ) {
				setAttributeGroupDef (new AttributeGroupDef(getDocument()));
				ref = getComponentRef ();
			}
			return ref.resolveAttributeGroupDef ();
		}

		//
		// Debugging
		//

		public String toString () {
			String s = "<" + ELT_ATTRGROUP;
			if ( isRef() ) {
				s += ATT_REF + "='" + getComponentRef().getQName() + "'";
			}
			s += ">\n";
			s += annotationToString ();
			if ( !isRef() ) {
				s += getAttributeGroupDef().bodyToString();
			}
			s += "</" + ELT_ATTRGROUP + ">\n";
			return s;
		}

	}

	//**********************************
	//**** Schema.AttributeParticle ****
	//**********************************

	public class AttributeParticle extends AParticle
	{
		private int				fUse = OPTIONAL;
		private String			fValue = null;

		//
		// Constructors
		//

		public AttributeParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setAttributeDecl (AttributeDecl attributeDecl) {
			setComponentRef (new LocalRef(attributeDecl));
		}

		public void setAttributeRef (String name, String namespace) {
			setComponentRef (new NamedRef(name, namespace));
		}

		public void setSimpleType (SimpleTypeDef typeDef) throws Exception {
			getAttributeDecl().setSimpleType (typeDef);
		}

		public void setSimpleType (String name, String namespace) throws Exception {
			getAttributeDecl().setSimpleType (name, namespace);
		}

		public void setUse (int use) {
			// TODO: merge use with attribute decl
			fUse = use;
		}

		public void setValue (String value) {
			// TODO: merge value with attribute decl
			fValue = value;
		}

		//
		// Getters
		//

		public AttributeDecl getAttributeDecl () {

			// Faults in AttributeDecl if not already there.

			Ref ref = getComponentRef ();
			if ( ref == null ) {
				setAttributeDecl (getDocument().createAttributeDecl());
				ref = getComponentRef ();
			}
			return ref.resolveAttributeDecl ();
		}

		public String getName () {
			return getAttributeDecl().getName ();
		}

		public boolean hasTypeRef () {
			return getAttributeDecl().hasTypeRef ();
		}

		public SimpleTypeDef getType () {
			return getAttributeDecl().getType ();
		}

		public int getUse () {
			if ( fUse == OPTIONAL ) {
				return getAttributeDecl().getUse ();
			} else {
				return fUse;
			}
		}

		public String getValue () {
			if ( fValue == null ) {
				return getAttributeDecl().getValue ();
			} else {
				return fValue;
			}
		}

		//
		// Debugging
		//

		public String toString () {

			String s = "<" + ELT_ATTRIBUTE;

			if ( isRef() ) {
				s += " " + ATT_REF + "='" + getComponentRef().getQName() + "'";
			} else {
				s += " " + ATT_NAME + "='" + getName() + "'";
				if ( hasTypeRef() ) {
					s += " " + ATT_TYPE + "='" + getType().getQName() + "'";
				}
			}

			s += " " + ATT_USE + "='" + USE_MAP[getUse()] + "'";
			
			String value = getValue ();
			if ( value != null ) {
				s += " " + ATT_VALUE + "='" + value + "'";
			}

			s += ">\n";
			s += annotationToString ();

			if ( !isRef() && !hasTypeRef() ) {
				getType().toString ();
			}

			s += "</" + ELT_ATTRIBUTE + ">\n";

			return s;
		}


	}

	//**************************
	//**** Schema.EParticle ****
	//**************************

	public class EParticle extends AnnotatedComponent
	{
		protected Ref			fComponentRef = null;
		private int				fMinOccurs = 1;
		private int				fMaxOccurs = 1;

		//
		// Constructors
		//

		public EParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		protected void setComponentRef (Ref componentRef) {
			fComponentRef = componentRef;
		}

		public void setMinOccurs (int minOccurs) {
			fMinOccurs = minOccurs;
		}

		public void setMaxOccurs (int maxOccurs) {
			fMaxOccurs = maxOccurs;
		}

		//
		// Getters
		//

		protected Ref getComponentRef () {
			return fComponentRef;
		}

		public String getName () {
			return fComponentRef.getName ();
		}

		public String getNamespace () {
			return fComponentRef.getNamespace ();
		}

		public int getMinOccurs () {
			return fMinOccurs;
		}

		public int getMaxOccurs () {
			return fMaxOccurs;
		}

		// Does this particle reference a global component?

		public boolean isRef () {
			return NamedRef.class.isInstance (fComponentRef);
		}
	}

	//******************************
	//**** Schema.GroupParticle ****
	//******************************

	public class GroupParticle	extends EParticle
								implements EParticleHolder
	{
		//
		// Constructors
		//

		public GroupParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setGroupDef (GroupDef groupDef) {
			setComponentRef (new LocalRef(groupDef));
		}

		public void setGroupRef (String name, String namespace) {
			setComponentRef (new NamedRef(name, namespace));
		}

		public void add (EParticle particle) {
			getGroupDef().add (particle);
		}

		public void setOrder (int order) {
			getGroupDef().setOrder (order);
		}

		//
		// Getters
		//

		public GroupDef getGroupDef () {
			Ref ref = getComponentRef ();
			if ( ref == null ) {
				setGroupDef (new GroupDef(getDocument()));
				ref = getComponentRef ();
			}
			return ref.resolveGroupDef ();
		}

		//
		// Debugging
		//

		public String toString () {	
			
			String s = "<" + ELT_GROUP;

			if ( isRef() ) {
				s += " " + ATT_REF + "'" + getComponentRef().getQName () + "'";
			}

			s += " " + ATT_MINOCCURS + "='" + getMinOccurs() + "'";
			s += " " + ATT_MAXOCCURS + "='" + getMaxOccurs() + "'";
			s += ">\n";

			s += annotationToString ();

			if ( !isRef() ) {
				s += getGroupDef().bodyToString ();
			}

			s += "</" + ELT_GROUP + ">\n";
			return s;
		}
	}

	//********************************
	//**** Schema.ElementParticle ****
	//********************************

	public class ElementParticle	extends		EParticle
									implements	HasType
	{
		// TODO: I currently don't use these.  I just use the values from the 
		// associated elementdecl.  Need to use and merge these later.

		private boolean			fIsAbstract = false;
		private boolean			fIsNullable = false;
		private int				fBlockSet = EMPTY_SET;	
		private String			fDefaultValue = null;
		private String			fFixedValue = null;

		//
		// Constructors
		//

		public ElementParticle (Document doc) {
			super (doc);
		}

		//
		// Setters
		//

		public void setElementDecl (ElementDecl elementDecl) {
			setComponentRef (new LocalRef(elementDecl));
		}

		public void setElementRef (String name, String namespace) {
			setComponentRef (new NamedRef(name, namespace));
		}

		public void setType (TypeDef typeDef) throws Exception {
			getElementDecl().setType (typeDef);
		}

		public void setType (String name, String namespace) throws Exception {
			getElementDecl().setType (name, namespace);
		}

		public void setSimpleType (SimpleTypeDef typeDef) throws Exception {
			setType (typeDef);
		}

		public void setSimpleType (String name, String namespace) throws Exception {
			setType (name, namespace);
		}

		public void setAbstract (boolean isAbstract) {
			fIsAbstract = isAbstract;
		}

		public void setNullable (boolean isNullable) {
			fIsNullable = isNullable;
		}

		public void setBlock (int blockSet) {
			fBlockSet = blockSet;
		}

		public void setDefaultValue (String defaultValue) {
			fDefaultValue = defaultValue;
		}

		public void setFixedValue (String fixedValue) {
			fFixedValue = fixedValue;
		}

		//
		// Getters
		//

		public ElementDecl getElementDecl () {
			Ref ref = getComponentRef ();
			if ( ref == null ) {
				setElementDecl (new ElementDecl(getDocument()));
				ref = getComponentRef ();
			}
			return ref.resolveElementDecl ();
		}

		public TypeDef getType () {
			return getElementDecl().getType ();
		}

		public boolean hasTypeRef () {
			return getElementDecl().hasTypeRef ();
		}

		public boolean isNullable () {
			// TODO:  Merge local value with element decl
			return getElementDecl().isNullable ();
		}

		public boolean isAbstract () {
			return getElementDecl().isAbstract ();
		}

		public boolean canSubstituteExtension () {
			return (fBlockSet & EXTENSION) == 0;
		} 

		public boolean canSubstituteRestriction () {
			return (fBlockSet & RESTRICTION) == 0;
		}

		public boolean canSubstituteReproduction () {
			return (fBlockSet & REPRODUCTION) == 0;
		}

		public String getDefaultValue () {
			String defaultValue = getElementDecl().getDefaultValue ();
			if ( defaultValue != null && fDefaultValue != null ) {
				return fDefaultValue;
			} else {
				return defaultValue;
			}
		}

		public String getFixedValue () {
			String fixedValue = getElementDecl().getFixedValue ();
			if ( fixedValue != null && fFixedValue != null ) {
				return fFixedValue;
			} else {
				return fixedValue;
			}
		}

		/*
		public void collectElements (Hashtable elements, boolean restrict) {
			ElementDecl elementDecl = getElementDecl ();
			String name = elementDecl.getName ();
			ElementDecl e = (ElementDecl)elements.get (name);
			if ( e == null ) {
				elements.put (name, elementDecl);
			} else {
				
			}
		}
		*/


		//
		// Debugging
		//

		public String toString () {
			String s = "<" + ELT_ELEMENT;

			if ( isRef() ) {
				s += " " + ATT_REF + "='" + getComponentRef().getQName () + "'";
			} else {
				s += " " + ATT_NAME + "='" + getName() + "'";
				if ( hasTypeRef() ) {
					s += " " + ATT_TYPE + "='" + getType().getQName() + "'";
				}
			}

			s += " " + ATT_MINOCCURS + "='" + getMinOccurs() + "'";
			s += " " + ATT_MAXOCCURS + "='" + getMaxOccurs() + "'";
			s += " " + ATT_ABSTRACT + "='" + ( isAbstract()? "true" : "false" ) + "'";
			s += " " + ATT_NULLABLE + "='" + ( isNullable()? "true" : "false" ) + "'"; 

			s += " " + ATT_BLOCK + "='";

			boolean extend = canSubstituteExtension ();
			boolean restrict = canSubstituteRestriction ();
			boolean reproduce = canSubstituteReproduction ();

			if ( !extend && !restrict && !reproduce ) {
				s += VAL_POUNDALL;
			} else {
				if ( !extend ) {
					s += " " + VAL_EXTENSION;
				}
				if ( !restrict ) {
					s += " " + VAL_RESTRICTION;
				}
				if ( !reproduce ) {
					s += " " + VAL_REPRODUCTION;
				}
			}

			s += "'";

			if ( fDefaultValue != null ) {
				s += " " + ATT_DEFAULT + "='" + fDefaultValue + "'";
			}
			if ( fFixedValue != null ) {
				s += " " + ATT_FIXED + "='" + fFixedValue + "'";
			}

			s += ">\n";
			s += annotationToString ();

			if ( !isRef() ) {
				s += getType().toString ();
			}

			s += "</" + ELT_ELEMENT + ">\n";
			return s;
		}


	}

	//
	// Inner objects can call this to report errors
	//

	private void reportSchemaError(int major, Object args[]) {
	    try {
    		fErrorReporter.reportError(fErrorReporter.getLocator(),
	    							   SchemaMessageProvider.SCHEMA_DOMAIN,
		    						   major,
			    					   SchemaMessageProvider.MSG_NONE,
				    				   args,
					    			   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}

	private void reportGenericSchemaError (String error) throws Exception {
		reportSchemaError (SchemaMessageProvider.GenericError, new Object[] { error });
	}


}