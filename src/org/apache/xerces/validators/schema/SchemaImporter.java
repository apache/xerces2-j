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

import java.io.IOException;
import java.util.Hashtable; // REVISIT replace
import java.util.Vector; // REVISIT replace

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.msg.SchemaMessages;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.common.XMLValidator;
import org.apache.xerces.validators.common.XMLContentModel;
import org.apache.xerces.validators.datatype.DatatypeValidator;
import org.apache.xerces.validators.datatype.InvalidDatatypeValueException;
import org.apache.xerces.validators.datatype.IllegalFacetException;
import org.apache.xerces.validators.datatype.IllegalFacetValueException;
import org.apache.xerces.validators.datatype.UnknownFacetException;
import org.apache.xerces.validators.datatype.BooleanValidator;
import org.apache.xerces.validators.datatype.IntegerValidator;
import org.apache.xerces.validators.datatype.StringValidator;
import org.apache.xerces.validators.datatype.FloatValidator;
import org.apache.xerces.validators.datatype.DoubleValidator;
import org.apache.xerces.validators.datatype.DecimalValidator;
import org.apache.xerces.validators.datatype.TimeDurationValidator;
import org.apache.xerces.validators.datatype.TimeInstantValidator;
import org.apache.xerces.validators.datatype.BinaryValidator;
import org.apache.xerces.validators.datatype.URIValidator;

/**
 * SchemaImporter is an <font color="red"><b>experimental</b></font> implementation
 * of a validator for the W3C Schema Language.  All of its implementation is subject
 * to change.
 */
public class SchemaImporter {
    //
    //
    //

	private Schema fSchema = null;

    private XMLValidator fValidator = null;
    private XMLErrorReporter fErrorReporter = null;
    private DefaultEntityHandler fEntityHandler = null;
    private StringPool fStringPool = null;
    private DatatypeValidatorRegistry fDatatypeRegistry = new DatatypeValidatorRegistry();
    private int fTypeCount = 0;
    private int fGroupCount = 0;
    private int fModelGroupCount = 0;
    private int fAttributeGroupCount = 0;
    private int fDatatypeCount = 0;
    private Hashtable fForwardRefs = new Hashtable(); // REVISIT w/ more efficient structure later
    private Hashtable fAttrGroupUses = new Hashtable();

    // constants

    private static final String ELT_COMMENT = "comment";
    private static final String ELT_DATATYPEDECL = "datatype";
    private static final String ELT_ARCHETYPEDECL = "type";
    private static final String ELT_ELEMENTDECL = "element";
    private static final String ELT_GROUPDECL = "group";
    private static final String ELT_ATTRGROUPDECL = "attributeGroup";
//    private static final String ELT_TEXTENTITYDECL = "textEntity";
//    private static final String ELT_EXTERNALENTITYDECL = "externalEntity";
//    private static final String ELT_UNPARSEDENTITYDECL = "unparsedEntity";
    private static final String ELT_NOTATIONDECL = "notation";
//    private static final String ELT_REFINES = "refines";
    private static final String ELT_RESTRICTIONS = "restrictions";
    private static final String ELT_ATTRIBUTEDECL = "attribute";
    private static final String ELT_ANNOTATION = "annotation";
    private static final String ELT_ANY = "any";
    private static final String ATT_NAME = "name";
    private static final String ATT_CONTENT = "content";
    private static final String ATT_MODEL = "model";
    private static final String ATT_ORDER = "order";
    private static final String ATT_TYPE = "type";
    private static final String ATT_DEFAULT = "default";
    private static final String ATT_FIXED = "fixed";
    private static final String ATT_COLLECTION = "collection";
    private static final String ATT_REF = "ref";
    private static final String ATT_ARCHREF = "archRef";
    private static final String ATT_SCHEMAABBREV = "schemaAbbrev";
    private static final String ATT_SCHEMANAME = "schemaName";
    private static final String ATT_MINOCCURS = "minOccurs";
    private static final String ATT_MAXOCCURS = "maxOccurs";
//    private static final String ATT_EXPORT = "export";
    private static final String ATT_SOURCE = "source";
    private static final String ATT_VALUE = "value";
    private static final String ATTVAL_ANY = "any";
    private static final String ATTVAL_MIXED = "mixed";
    private static final String ATTVAL_EMPTY = "empty";
    private static final String ATTVAL_CHOICE = "choice";
    private static final String ATTVAL_SEQ = "seq";
    private static final String ATTVAL_ALL = "all";
    private static final String ATTVAL_ELEMONLY = "elementOnly";
    private static final String ATTVAL_TEXTONLY = "textOnly";

    private Document fSchemaDocument = null;

    //
    //
    //
    public SchemaImporter(StringPool stringPool,
                          XMLErrorReporter errorReporter,
                          DefaultEntityHandler entityHandler,
                          XMLValidator validator) {
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fStringPool = stringPool;
        fValidator = validator;
        fDatatypeRegistry.initializeRegistry();
		fSchema = new Schema (fErrorReporter, fValidator);
    }
    //
    //
    //
    public void reset(StringPool stringPool) throws Exception {
        fStringPool = stringPool;
        // need to reset the datatype registry !!
        fDatatypeRegistry = new DatatypeValidatorRegistry();
        fDatatypeRegistry.initializeRegistry();
		fSchema = new Schema (fErrorReporter, fValidator);
    }
    //
    //
    //
    public Document getSchemaDocument() {
        return fSchemaDocument;
    }
    //
    //
    //
    private int getContentSpecHandleForElementType(int elementType) {
        return fValidator.getContentSpecHandle(fStringPool.getDeclaration(elementType));
    }
    private int getContentSpecTypeForElementType(int elementType) {
        return fValidator.getContentSpecType(fStringPool.getDeclaration(elementType));
    }

    // content spec node types

    /** Occurrence count: [0, n]. */
    private static final int CONTENTSPECNODE_ZERO_TO_N = XMLContentSpec.CONTENTSPECNODE_SEQ + 1;

    /** Occurrence count: [m, n]. */
    private static final int CONTENTSPECNODE_M_TO_N = CONTENTSPECNODE_ZERO_TO_N + 1;

    /** Occurrence count: [m, *). */
    private static final int CONTENTSPECNODE_M_OR_MORE = CONTENTSPECNODE_M_TO_N + 1;

    private DOMParser fSchemaParser = null;

    public void loadSchema(InputSource is) {


		SchemaParser parser = new SchemaParser (fSchema);
		try {
            parser.setEntityResolver(new Resolver());
            parser.setErrorHandler(new ErrorHandler());
            //parser.setFeature("http://xml.org/sax/features/validation", true);
			parser.parse (is);
		} 
		catch (Exception e) {
            e.printStackTrace();
			System.exit (1);
		}


        // create parser for schema
        if (fSchemaParser == null) {
            fSchemaParser = new DOMParser() {
                public void ignorableWhitespace(char ch[], int start, int length) {}
                public void ignorableWhitespace(int dataIdx) {}
            };
            fSchemaParser.setEntityResolver(new Resolver());
            fSchemaParser.setErrorHandler(new ErrorHandler());
        }

        // parser schema file
        try {

            fSchemaParser.setFeature("http://xml.org/sax/features/validation", true);
            fSchemaParser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
            fSchemaParser.parse(is);
        }
        catch (SAXException se) {
            se.getException().printStackTrace();
            System.err.println("error parsing schema file");
//            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("error parsing schema file");
//            System.exit(1);
        }
        fSchemaDocument = fSchemaParser.getDocument();
        if (fSchemaDocument == null) {
            System.err.println("error: couldn't load schema file!");
            return;
        }

        // traverse schema
        try {
            Element root = fSchemaDocument.getDocumentElement();
            traverseSchema(root);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
//            System.exit(1);
        }
    }

    private void traverseSchema(Element root) throws Exception {
        // is there anything to do?
        if (root == null) {
            return;
        }

        // run through children
        for (Element child = XUtil.getFirstChildElement(root);
             child != null;
             child = XUtil.getNextSiblingElement(child)) {
            //System.out.println("child: "+child.getNodeName()+' '+child.getAttribute(ATT_NAME));

            //
            // Element type
            //

            String name = child.getNodeName();
			if (name.equals(ELT_COMMENT)) {
				traverseComment(child);
            } else if (name.equals(ELT_DATATYPEDECL)) {
				traverseDatatypeDecl(child);
            } else if (name.equals(ELT_ARCHETYPEDECL)) {
				traverseTypeDecl(child);
			} else if (name.equals(ELT_ELEMENTDECL)) { // && child.getAttribute(ATT_REF).equals("")) {
				traverseElementDecl(child);
			} else if (name.equals(ELT_ATTRGROUPDECL)) {
				traverseAttrGroup(child);
			} else if (name.equals(ELT_GROUPDECL) && child.getAttribute(ATT_REF).equals("")) {
				traverseGroup(child);
			}

            //
            // Entities
            //
/*
            else if (name.equals(ELT_TEXTENTITYDECL) ||
                     name.equals(ELT_EXTERNALENTITYDECL) ||
                     name.equals(ELT_UNPARSEDENTITYDECL)) {

                int entityName = fStringPool.addSymbol(child.getAttribute(ATT_NAME));

                if (name.equals(ELT_TEXTENTITYDECL)) {
                    int value = fStringPool.addString(child.getFirstChild().getFirstChild().getNodeValue());
                    fEntityHandler.addInternalEntityDecl(entityName, value, true);
                }
                else {
                    int publicId = fStringPool.addString(child.getAttribute("public"));
                    int systemId = fStringPool.addString(child.getAttribute("system"));

                    if (name.equals(ELT_EXTERNALENTITYDECL)) {
                        fEntityHandler.addExternalEntityDecl(entityName, publicId, systemId, true);
                    }
                    else {
                        int notationName = fStringPool.addSymbol(child.getAttribute("notation"));
                        fEntityHandler.addUnparsedEntityDecl(entityName, publicId, systemId, notationName, true);
                    }
                }
            }
*/
            //
            // Notation
            //

            else if (name.equals(ELT_NOTATIONDECL)) {

                int notationName = fStringPool.addSymbol(child.getAttribute(ATT_NAME));
                int publicId = fStringPool.addString(child.getAttribute("public"));
                int systemId = fStringPool.addString(child.getAttribute("system"));
                fEntityHandler.addNotationDecl(notationName, publicId, systemId, true);
            }

        } // for each child node

        cleanupForwardReferences();
    } // traverseSchema(Element)

    /**
     * this method is going to be needed to handle any elementDecl derived constructs which
     * need to copy back attributes that haven't been declared yet.
     * right now that's just attributeGroups and types -- grrr.
     */
    private void cleanupForwardReferences() {
        for (java.util.Enumeration keys = fForwardRefs.keys(); keys.hasMoreElements();) {
            Object k = keys.nextElement();
            int ik = ((Integer) k).intValue();
            cleanupForwardReferencesTo(ik);
        }
    }

    private void cleanupForwardReferencesTo(int r) {
        Vector referrers = (Vector) fForwardRefs.get(new Integer(r));
        if (referrers == null) return;
//        System.out.println("referee "+r+" csnIndex= "+getContentSpec(getElement(r)));
        for (int i = 0; i < referrers.size(); i++) {
            int ref = ((Integer) referrers.elementAt(i)).intValue();
//            System.out.println("referrer "+referrers.elementAt(i)+ " csnIndex = "+getContentSpec(getElement(((Integer)referrers.elementAt(i)).intValue())));
//            System.out.println("copying from "+fStringPool.toString(r)+" to "+fStringPool.toString(ref));
            fValidator.copyAtts(r, ((Integer) referrers.elementAt(i)).intValue());
//            try {
//                fValidator.fixupDeclaredElements(ref, r);
//            } catch (Exception e) {
//                System.out.println("Error while cleaning up");
//            }
//            cleanupForwardReferencesTo(ref);
        }
    }

	private void traverseComment(Element comment) {
        return; // do nothing
	}

	private int traverseTypeDecl(Element typeDecl) throws Exception {
		String typeName = typeDecl.getAttribute(ATT_NAME);
		String content = typeDecl.getAttribute(ATT_CONTENT);
		String source = typeDecl.getAttribute(ATT_SOURCE);

		if (typeName.equals("")) { // gensym a unique name
		    typeName = "http://www.apache.org/xml/xerces/internalType"+fTypeCount++;
		}
		
		Element child = null;
        int contentSpecType = 0;
        int csnType = 0;
        int left = -2;
        int right = -2;
        Vector uses = new Vector();
		
   		// skip refinement and annotations
        child = null;
		for (child = XUtil.getFirstChildElement(typeDecl);
		     child != null && (child.getNodeName().equals(ELT_RESTRICTIONS) ||
		                       child.getNodeName().equals(ELT_ANNOTATION));
		     child = XUtil.getNextSiblingElement(child)) {
    		if (child.getNodeName().equals(ELT_RESTRICTIONS))
	    		reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
		    					  new Object [] { "Restriction" });
	    }

        // if content = textonly, source is a datatype
        if (content.equals(ATTVAL_TEXTONLY)) {
            if (fDatatypeRegistry.getValidatorFor(source) == null) // must be datatype
	    		reportSchemaError(SchemaMessageProvider.NotADatatype,
		    					  new Object [] { source }); //REVISIT check forward refs
            //handle datatypes
            contentSpecType = fStringPool.addSymbol("DATATYPE");
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
									                   fStringPool.addSymbol(source),
													   -1, false);
            
        } else {   
            contentSpecType = fStringPool.addSymbol("CHILDREN");
            csnType = XMLContentSpec.CONTENTSPECNODE_SEQ;
            boolean mixedContent = false;
            boolean elementContent = false;
            boolean textContent = false;
            left = -2;
            right = -2;
            boolean hadContent = false;
		
            if (content.equals(ATTVAL_EMPTY)) {
                contentSpecType = fStringPool.addSymbol("EMPTY");
                left = -1; // no contentSpecNode needed
            } else if (content.equals(ATTVAL_MIXED) || content.equals("")) {
                contentSpecType = fStringPool.addSymbol("MIXED");
                mixedContent = true;
                csnType = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            } else if (content.equals(ATTVAL_ELEMONLY)) {
                elementContent = true;
            } else if (content.equals(ATTVAL_TEXTONLY)) {
                textContent = true;
            }
            
            if (mixedContent) {
                // a    dd #PCDATA leaf
                left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                           -1, // -1 means "#PCDATA" is name
                                                           -1, false);
            }

            for (;
                 child != null;
                 child = XUtil.getNextSiblingElement(child)) {
                int index = -2;
                hadContent = true;
                String childName = child.getNodeName();
                if (childName.equals(ELT_ELEMENTDECL)) {
                    if (child.getAttribute(ATT_REF).equals("") && 
                        child.getAttribute(ATT_TYPE).equals("")) { // elt decl
                        if (elementContent)   //R   EVISIT: no support for nested type declarations
                            reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
                                              new Object [] { "Nesting element declarations" });
                        else
                            reportSchemaError(SchemaMessageProvider.NestedOnlyInElemOnly, null);
                    } else if (mixedContent || elementContent) {
                        if (!child.getAttribute(ATT_TYPE).equals("")) {
                            int elementNameIndex = traverseElementDecl(child);
                            index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                                        elementNameIndex,
                                                                        -1, false);
                        } else // ATT_REF != ""
                            index = traverseElementRef(child);
                    } else {
                        reportSchemaError(SchemaMessageProvider.EltRefOnlyInMixedElemOnly, null);
                    }
                } else if (childName.equals(ELT_GROUPDECL)) {
                    if (elementContent) {
                        int groupNameIndex = 0;
                        if (child.getAttribute(ATT_REF).equals("")) {
                            groupNameIndex = traverseGroup(child);
                        } else
                            groupNameIndex = traverseGroupRef(child);
                        index = getContentSpecHandleForElementType(groupNameIndex);
                    } else if (!elementContent)
                        reportSchemaError(SchemaMessageProvider.OnlyInEltContent,
                                          new Object [] { "group" });
                    else // buildAll    
                        reportSchemaError(SchemaMessageProvider.OrderIsAll,
                                          new Object [] { "group" } );
                } else if (childName.equals(ELT_ATTRIBUTEDECL) ||
                           childName.equals(ELT_ATTRGROUPDECL)) {
                    break; // attr processing is done be    low
                } else if (childName.equals(ELT_ANY)) {
                    contentSpecType = fStringPool.addSymbol("ANY");
                    left = -1;
                } else { // datatype qual   
                    if (source.equals(""))
                        reportSchemaError(SchemaMessageProvider.DatatypeWithType, null);
                    else
                        reportSchemaError(SchemaMessageProvider.DatatypeQualUnsupported,
                                          new Object [] { childName });
                }
                uses.addElement(new Integer(index));
                if (left == -2) {
                    left = index;
                } else if (right == -2) {
                    right = index;
                } else {
                    left = fValidator.addContentSpecNode(csnType, left, right, false);
                    right = index;
                }
            }
            
            if (hadContent && right != -2)
                left = fValidator.addContentSpecNode(csnType, left, right, false);
		
            if (mixedContent && hadContent) {
                // set occurrence count
                left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                           left, -1, false);
            }
        }
		// stick in ElementDeclPool as a hack
		int typeNameIndex = fStringPool.addSymbol(typeName); //REVISIT namespace clashes possible
		int typeIndex = fValidator.addElementDecl(typeNameIndex, contentSpecType, left, false);

        for (int x = 0; x < uses.size(); x++)
            addUse(typeNameIndex, (Integer)uses.elementAt(x));

		// (attribute | attrGroupRef)*
		for (;
			 child != null;
			 child = XUtil.getNextSiblingElement(child)) {
			String childName = child.getNodeName();
			if (childName.equals(ELT_ATTRIBUTEDECL)) {
				traverseAttributeDecl(child, typeIndex);
			} else if (childName.equals(ELT_ATTRGROUPDECL) && !child.getAttribute(ATT_REF).equals("")) {
    			int index = traverseAttrGroupRef(child);
    			if (getContentSpecHandleForElementType(index) == -1) {
					reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
									  new Object [] { "Forward References to attrGroup" });
    			    Vector v = null;
    			    Integer i = new Integer(index);
    			    if ((v = (Vector) fForwardRefs.get(i)) == null)
    			        v = new Vector();
                    v.addElement(new Integer(typeNameIndex));
    			    fForwardRefs.put(i,v);
                    addUse(typeNameIndex, index);
    			} else
        			fValidator.copyAtts(index, typeNameIndex);
			}
		}
		
        return typeNameIndex;
	}

	private int traverseGroup(Element groupDecl) throws Exception {
		String groupName = groupDecl.getAttribute(ATT_NAME);
		String collection = groupDecl.getAttribute(ATT_COLLECTION);
		String order = groupDecl.getAttribute(ATT_ORDER);
		
		if (groupName.equals("")) { // gensym a unique name
		    groupName = "http://www.apache.org/xml/xerces/internalGroup"+fGroupCount++;
		}
		
		Element child = XUtil.getFirstChildElement(groupDecl);
        while (child != null && child.getNodeName().equals(ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

		int contentSpecType = 0;
		int csnType = 0;
        boolean buildAll = false;
        int allChildren[] = null;
        int allChildCount = 0;
		
		if (order.equals(ATTVAL_CHOICE)) {
			csnType = XMLContentSpec.CONTENTSPECNODE_CHOICE;
			contentSpecType = fStringPool.addSymbol("CHILDREN");
		} else if (order.equals(ATTVAL_SEQ)) {
			csnType = XMLContentSpec.CONTENTSPECNODE_SEQ;
			contentSpecType = fStringPool.addSymbol("CHILDREN");
		} else if (order.equals(ATTVAL_ALL)) {
            buildAll = true;
            allChildren = new int[((org.apache.xerces.dom.NodeImpl)groupDecl).getLength()];
            allChildCount = 0;
		}
		int left = -2;
		int right = -2;
		boolean hadContent = false;
		int groupIndices[] = new int [((org.apache.xerces.dom.NodeImpl)groupDecl).getLength()];
		int numGroups = 0;

		for (;
			 child != null;
			 child = XUtil.getNextSiblingElement(child)) {
			int index = -2;
            hadContent = true;
			String childName = child.getNodeName();
			if (childName.equals(ELT_ELEMENTDECL)) {
			    if (child.getAttribute(ATT_REF).equals("") && 
                    child.getAttribute(ATT_TYPE).equals("")) { //elt decl
					reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
									  new Object [] { "Nesting element declarations" });
                } else {
                    if (!child.getAttribute(ATT_TYPE).equals("")) {
    			       int elementNameIndex = traverseElementDecl(child);
    			       index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
    			                                                   elementNameIndex,
    			                                                   -1, false);
    			       
                    } else {
                        index = traverseElementRef(child); 
                    }
                }
			} else if (childName.equals(ELT_GROUPDECL)) {
			    if (!buildAll) {
    			    int groupNameIndex = traverseGroup(child);
	    			groupIndices[numGroups++] = groupNameIndex;
		    		index = getContentSpecHandleForElementType(groupNameIndex);
		    	} else
					reportSchemaError(SchemaMessageProvider.OrderIsAll,
									  new Object [] { "group" } );
			} else {
				reportSchemaError(SchemaMessageProvider.GroupContentRestricted,
								  new Object [] { "group", childName });
			}
			if (buildAll) {
			    allChildren[allChildCount++] = index;
			} else if (left == -2) {
				left = index;
			} else if (right == -2) {
				right = index;
			} else {
   				left = fValidator.addContentSpecNode(csnType, left, right, false);
    			right = index;
   			}
		}
		if (buildAll) {
		    left = buildAllModel(allChildren,allChildCount);
		} else {
			if (hadContent && right != -2)
				left = fValidator.addContentSpecNode(csnType, left, right, false);
		}
		left = expandContentModel(left, groupDecl);
	
		// stick in ElementDeclPool as a hack
		int groupNameIndex = fStringPool.addSymbol(groupName); //REVISIT namespace clashes possible
		int groupIndex = fValidator.addElementDecl(groupNameIndex, contentSpecType, left, false);

        return groupNameIndex;
	}

	private int traverseGroupRef(Element groupRef) {
	    String name = groupRef.getAttribute(ATT_REF);
	    int index = fStringPool.addSymbol(name);
	    return index;
	}

	public int traverseDatatypeDecl(Element datatypeDecl) throws Exception {
		int newTypeName;
		
 		if (datatypeDecl.getAttribute(ATT_NAME).equals("")) {
    		String newTypeString = "http://www.apache.org/xml/xerces/internalDatatype"+fGroupCount++;
		    newTypeName = fStringPool.addSymbol(newTypeString);
	 	} else
    		newTypeName = fStringPool.addSymbol(datatypeDecl.getAttribute(ATT_NAME));
		int basetype = fStringPool.addSymbol(datatypeDecl.getAttribute(ATT_SOURCE));

		// check that base type is defined
		//REVISIT: how do we do the extension mechanism? hardwired type name?
		DatatypeValidator baseValidator = fDatatypeRegistry.getValidatorFor(datatypeDecl.getAttribute(ATT_SOURCE));
		if (baseValidator == null) {
			reportSchemaError(SchemaMessageProvider.UnknownBaseDatatype,
							  new Object [] { datatypeDecl.getAttribute(ATT_SOURCE), datatypeDecl.getAttribute(ATT_NAME) });
			return -1;
		}

		// build facet list
		int numFacets = 0;
		int numEnumerationLiterals = 0;
		Hashtable facetData = new Hashtable();
		Vector enumData = new Vector();

        // Skip annotations
		Node facet = datatypeDecl.getFirstChild();
		while (facet != null && facet.getNodeName().equals(ELT_ANNOTATION))
		    facet = facet.getNextSibling();
		while (facet != null) {
			if (facet.getNodeType() == Node.ELEMENT_NODE) {
			    Element facetElt = (Element) facet;
				numFacets++;
    			if (facetElt.getNodeName().equals(DatatypeValidator.ENUMERATION)) {
					numEnumerationLiterals++;
					enumData.addElement(facetElt.getAttribute(ATT_VALUE));
				} else {
					facetData.put(facetElt.getNodeName(),facetElt.getAttribute(ATT_VALUE));
				}
			}
			facet = facet.getNextSibling();
		}
		if (numEnumerationLiterals > 0) {
			facetData.put(DatatypeValidator.ENUMERATION, enumData);
		}

		// create & register validator for "generated" type if it doesn't exist
		try {
			DatatypeValidator newValidator = (DatatypeValidator) baseValidator.getClass().newInstance();
			if (numFacets > 0)
				newValidator.setFacets(facetData);
			fDatatypeRegistry.addValidator(fStringPool.toString(newTypeName),newValidator);
		} catch (Exception e) {
			reportSchemaError(SchemaMessageProvider.DatatypeError,
							  new Object [] { e.getMessage() });
		}
        return newTypeName;
	}

	private int traverseElementDecl(Element elementDecl) throws Exception {
		int contentSpecType      = -1;
		int contentSpecNodeIndex = -1;
        int typeNameIndex = -1;

		String name = elementDecl.getAttribute(ATT_NAME);
		String ref = elementDecl.getAttribute(ATT_REF);
		String type = elementDecl.getAttribute(ATT_TYPE);
		String minOccurs = elementDecl.getAttribute(ATT_MINOCCURS);
		String maxOccurs = elementDecl.getAttribute(ATT_MAXOCCURS);
		String dflt = elementDecl.getAttribute(ATT_DEFAULT);
		String fixed = elementDecl.getAttribute(ATT_FIXED);

        int attrCount = 0;
		if (!ref.equals("")) attrCount++;
		if (!type.equals("")) attrCount++;
		//REVISIT top level check for ref & archref
		if (attrCount > 1)
			reportSchemaError(SchemaMessageProvider.OneOfTypeRefArchRef, null);
		
		if (!ref.equals("")) {
		    if (XUtil.getFirstChildElement(elementDecl) != null)
				reportSchemaError(SchemaMessageProvider.NoContentForRef, null);
		   	int typeName = fStringPool.addSymbol(ref);
		   	contentSpecNodeIndex = getContentSpecHandleForElementType(typeName);
		   	contentSpecType = getContentSpecTypeForElementType(typeName);

		   	int elementNameIndex = fStringPool.addSymbol(name);
            int elementIndex = -1;

		   	if (contentSpecNodeIndex == -1) {
		   	    contentSpecType = XMLContentSpec.CONTENTSPECNODE_LEAF;
            	contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
	        							                                   elementNameIndex, -1, false);
                fValidator.addElementDecl(elementNameIndex, contentSpecType, contentSpecNodeIndex, true);
				reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
								  new Object [] { "Forward references to archetypes" });
    			Vector v = null;
    			Integer i = new Integer(typeName);
    			if ((v = (Vector) fForwardRefs.get(i)) == null)
    			     v = new Vector();
                v.addElement(new Integer(elementNameIndex));
    			fForwardRefs.put(i,v);
    			addUse(elementNameIndex, typeName);
		   	} else {
                fValidator.addElementDecl(elementNameIndex, contentSpecType, contentSpecNodeIndex, true);
                // copy up attribute decls from type object
                fValidator.copyAtts(typeName, elementNameIndex);
            }
		    return elementNameIndex;
		}
		
		// element has a single child element, either a datatype or a type, null if primitive
		Element content = XUtil.getFirstChildElement(elementDecl);
        while (content != null && content.getNodeName().equals(ELT_ANNOTATION))
            content = XUtil.getNextSiblingElement(content);

		if (content != null) {
			String contentName = content.getNodeName();
			if (contentName.equals(ELT_ARCHETYPEDECL)) {
				typeNameIndex = traverseTypeDecl(content);
				contentSpecNodeIndex = getContentSpecHandleForElementType(typeNameIndex);
				contentSpecType = getContentSpecTypeForElementType(typeNameIndex);
			} else if (contentName.equals(ELT_DATATYPEDECL)) {
				reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
								  new Object [] { "Nesting datatype declarations" });
				// contentSpecNodeIndex = traverseDatatypeDecl(content);
				// contentSpecType = fStringPool.addSymbol("DATATYPE");
			} else if (!type.equals("")) { // datatype
				contentSpecType = fStringPool.addSymbol("DATATYPE");

				// set content spec node index to leaf
				contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
																		   fStringPool.addSymbol(content.getAttribute(ATT_NAME)),
																		   -1, false);
				// set occurrance count
				contentSpecNodeIndex = expandContentModel(contentSpecNodeIndex, content);
			} else if (type.equals("")) { // "untyped" leaf
			    // untyped leaf element decl
	    		contentSpecType = fStringPool.addSymbol("CHILDREN");

    			// add leaf
				int leftIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
																	fStringPool.addSymbol(content.getAttribute(ATT_NAME)),
																	-1, false);

    			// set occurrence count
	    		contentSpecNodeIndex = expandContentModel(contentSpecNodeIndex, content);
			} else {
				System.out.println("unhandled case in element decl code");
			}
		} else if (!type.equals("")) { // type specified in attribute, not content
			contentSpecType = fStringPool.addSymbol("DATATYPE");

			// set content spec node index to leaf
			contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
																	   fStringPool.addSymbol(type),
																	   -1, false);
			// set occurrance count
			contentSpecNodeIndex = expandContentModel(contentSpecNodeIndex, elementDecl);
		}

		//
		// Create element decl
		//

		int elementNameIndex     = fStringPool.addSymbol(elementDecl.getAttribute(ATT_NAME));

		// add element decl to pool
		int elementIndex = fValidator.addElementDecl(elementNameIndex, contentSpecType, contentSpecNodeIndex, true);
//        System.out.println("elementIndex:"+elementIndex+" "+elementDecl.getAttribute(ATT_NAME)+" eltType:"+elementName+" contentSpecType:"+contentSpecType+
//                           " SpecNodeIndex:"+ contentSpecNodeIndex);

        // copy up attribute decls from type object
        if (typeNameIndex != -1)
            fValidator.copyAtts(typeNameIndex, elementNameIndex);

        return elementNameIndex;
	}

    private int traverseElementRef(Element elementRef) {
        String elementName = elementRef.getAttribute(ATT_REF);
        int elementTypeIndex = fStringPool.addSymbol(elementName);
        int contentSpecNodeIndex = 0;
        try {
        	contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
	    							                                   elementTypeIndex, -1, false);
            contentSpecNodeIndex = expandContentModel(contentSpecNodeIndex, elementRef);
        } catch (Exception e) {
            //REVISIT: integrate w/ error handling
            e.printStackTrace();
        }
        return contentSpecNodeIndex;
    }

	//REVISIT: elementIndex API is ugly
	private void traverseAttributeDecl(Element attrDecl, int elementIndex) throws Exception {
		// attribute name
		int attName = fStringPool.addSymbol(attrDecl.getAttribute(ATT_NAME));

		// attribute type
		int attType = -1;
		int enumeration = -1;
		String datatype = attrDecl.getAttribute(ATT_TYPE);
		if (datatype.equals("")) {
		    Element child = XUtil.getFirstChildElement(attrDecl);
		    while (child != null && !child.getNodeName().equals(ELT_DATATYPEDECL))
		        child = XUtil.getNextSiblingElement(child);
		    if (child != null && child.getNodeName().equals(ELT_DATATYPEDECL)) {
		        attType = fStringPool.addSymbol("DATATYPE");
		        enumeration = traverseDatatypeDecl(child);
		    } else 
    			attType = fStringPool.addSymbol("CDATA");
		} else {
			if (datatype.equals("string")) {
				attType = fStringPool.addSymbol("CDATA");
			} else if (datatype.equals("ID")) {
				attType = fStringPool.addSymbol("ID");
			} else if (datatype.equals("IDREF")) {
				attType = fStringPool.addSymbol("IDREF");
			} else if (datatype.equals("IDREFS")) {
				attType = fStringPool.addSymbol("IDREFS");
			} else if (datatype.equals("ENTITY")) {
				attType = fStringPool.addSymbol("ENTITY");
			} else if (datatype.equals("ENTITIES")) {
				attType = fStringPool.addSymbol("ENTITIES");
			} else if (datatype.equals("NMTOKEN")) {
				Element e = XUtil.getFirstChildElement(attrDecl, "enumeration");
				if (e == null) {
					attType = fStringPool.addSymbol("NMTOKEN");
				} else {
					attType = fStringPool.addSymbol("ENUMERATION");
					enumeration = fStringPool.startStringList();
					for (Element literal = XUtil.getFirstChildElement(e, "literal");
						 literal != null;
						 literal = XUtil.getNextSiblingElement(literal, "literal")) {
			    		int stringIndex = fStringPool.addSymbol(literal.getFirstChild().getNodeValue());
						fStringPool.addStringToList(enumeration, stringIndex);
					}
					fStringPool.finishStringList(enumeration);
				}
			} else if (datatype.equals("NMTOKENS")) {
				attType = fStringPool.addSymbol("NMTOKENS");
			} else if (datatype.equals(ELT_NOTATIONDECL)) {
				attType = fStringPool.addSymbol("NOTATION");
			} else { // REVISIT: Danger: assuming all other ATTR types are datatypes
				//REVISIT check against list of validators to ensure valid type name
				attType = fStringPool.addSymbol("DATATYPE");
				enumeration = fStringPool.addSymbol(datatype);
			}
		}

		// attribute default type
		int attDefaultType = -1;
		int attDefaultValue = -1;
		boolean required = attrDecl.getAttribute("minOccurs").equals("1");
		if (required) {
			attDefaultType = fStringPool.addSymbol("#REQUIRED");
		} else {
			String fixed = attrDecl.getAttribute(ATT_FIXED);
			if (!fixed.equals("")) {
				attDefaultType = fStringPool.addSymbol("#FIXED");
				attDefaultValue = fStringPool.addString(fixed);
			} else {
				// attribute default value
				String defaultValue = attrDecl.getAttribute(ATT_DEFAULT);
				if (!defaultValue.equals("")) {
					attDefaultType = fStringPool.addSymbol("");
					attDefaultValue = fStringPool.addString(defaultValue);
				} else {
					attDefaultType = fStringPool.addSymbol("#IMPLIED");
				}
			}
			// check default value is valid for the datatype.
			if (attType == fStringPool.addSymbol("DATATYPE") && attDefaultValue != -1) {
        		try { // REVISIT - integrate w/ error handling
                    String type = fStringPool.toString(enumeration);
                    DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
                    if (v != null)
                        v.validate(fStringPool.toString(attDefaultValue));
                    else
						reportSchemaError(SchemaMessageProvider.NoValidatorFor,
										  new Object [] { type });
                } catch (InvalidDatatypeValueException idve) {
					reportSchemaError(SchemaMessageProvider.IncorrectDefaultType,
									  new Object [] { attrDecl.getAttribute(ATT_NAME), idve.getMessage() });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Internal error in attribute datatype validation");
                }
            }
		}

		// add attribute to element decl pool
		fValidator.addAttDef(elementIndex, attName, attType, enumeration, attDefaultType, attDefaultValue, true);
	}

	private int traverseAttrGroup(Element attrGroupDecl) throws Exception {

		String attrGroupName = attrGroupDecl.getAttribute(ATT_NAME);
		
		if (attrGroupName.equals("")) { // gensym a unique name
		    attrGroupName = "http://www.apache.org/xml/xerces/internalGroup"+fGroupCount++;
		}
		
		Element child = XUtil.getFirstChildElement(attrGroupDecl);
        while (child != null && child.getNodeName().equals(ELT_ANNOTATION))
            child = XUtil.getNextSiblingElement(child);

		int groupIndices[] = new int [((org.apache.xerces.dom.NodeImpl)attrGroupDecl).getLength()];
		int numGroups = 0;

		for (;
			 child != null;
			 child = XUtil.getNextSiblingElement(child)) {
			String childName = child.getNodeName();
			if (childName.equals(ELT_ATTRGROUPDECL) && !child.getAttribute(ATT_REF).equals("")) {
			    groupIndices[numGroups++] = traverseAttrGroupRef(child);
			    if (getContentSpecHandleForElementType(groupIndices[numGroups-1]) == -1) {
					reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
									  new Object [] { "Forward reference to AttrGroup" });
			    }
			} else if (childName.equals(ELT_ATTRIBUTEDECL)) {
                continue;
   			} else {
				reportSchemaError(SchemaMessageProvider.IllegalAttContent,
								  new Object [] { childName });
   			}
		}
	
		// stick in ElementDeclPool as a hack
		int attrGroupNameIndex = fStringPool.addSymbol(attrGroupName); //REVISIT namespace clashes possible
		int attrGroupIndex = fValidator.addElementDecl(attrGroupNameIndex, 0, 0, false);
//        System.out.println("elementIndex:"+groupIndex+" "+groupName+" eltType:"+groupNameIndex+" SpecType:"+contentSpecType+
//                           " SpecNodeIndex:"+ left);

		// (attribute | attrGroupRef)*
		for (child = XUtil.getFirstChildElement(attrGroupDecl);  // start from the beginning to just do attrs
			 child != null;
			 child = XUtil.getNextSiblingElement(child)) {
			String childName = child.getNodeName();
			if (childName.equals(ELT_ATTRIBUTEDECL)) {
				traverseAttributeDecl(child, attrGroupIndex);
			} else if (childName.equals(ELT_ATTRGROUPDECL)) {
    			int index = traverseAttrGroupRef(child);
    			if (getContentSpecHandleForElementType(index) == -1) {
					reportSchemaError(SchemaMessageProvider.FeatureUnsupported,
									  new Object [] { "Forward reference to AttrGroup" });
    			    Vector v = null;
    			    Integer i = new Integer(index);
    			    if ((v = (Vector) fForwardRefs.get(i)) == null)
    			        v = new Vector();
                    v.addElement(new Integer(attrGroupNameIndex));
    			    fForwardRefs.put(i,v);
    			    addUse(attrGroupNameIndex, index);
    			} else
    			    groupIndices[numGroups++] = getContentSpecHandleForElementType(index);
			}
		}
		
        // copy up attribute decls from nested groups
		for (int i = 0; i < numGroups; i++) {
            fValidator.copyAtts(groupIndices[i], attrGroupNameIndex);
        }

        return attrGroupNameIndex;
	}

	private int traverseAttrGroupRef(Element attrGroupRef) {
	    String name = attrGroupRef.getAttribute(ATT_REF);
	    int index = fStringPool.addSymbol(name);
        return index;
	}
	
	private void addUse(int def, int use) {
        addUse(def, new Integer(use));
	}

	private void addUse(int def, Integer use) {
        Vector v = (Vector) fAttrGroupUses.get(new Integer(def));
        if (v == null) v = new Vector();
    		 v.addElement(use);
	}

    /** builds the all content model */
    private int buildAllModel(int children[], int count) throws Exception {

        // build all model
        if (count > 1) {

            // create and initialize singletons
            XMLContentSpec.Node choice = new XMLContentSpec.Node();

            choice.type = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            choice.value = -1;
            choice.otherValue = -1;

            // build all model
            sort(children, 0, count);
            int index = buildAllModel(children, 0, choice);

            return index;
        }

        if (count > 0) {
            return children[0];
        }

        return -1;
    }

    /** Builds the all model. */
    private int buildAllModel(int src[], int offset,
                              XMLContentSpec.Node choice) throws Exception {

        // swap last two places
        if (src.length - offset == 2) {
            int seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            }
            else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            swap(src, offset, offset + 1);
            seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            }
            else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            return fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
        }

        // recurse
        for (int i = offset; i < src.length - 1; i++) {
            choice.value = buildAllModel(src, offset + 1, choice);
            choice.otherValue = -1;
            sort(src, offset, src.length - offset);
            shift(src, offset, i + 1);
        }

        int choiceIndex = buildAllModel(src, offset + 1, choice);
        sort(src, offset, src.length - offset);

        return choiceIndex;

    } // buildAllModel(int[],int,ContentSpecNode,ContentSpecNode):int

    /** Creates a sequence. */
    private int createSeq(int src[]) throws Exception {

        int left = src[0];
        int right = src[1];

        for (int i = 2; i < src.length; i++) {
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                       left, right, false);
            right = src[i];
        }

        return fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                   left, right, false);

    } // createSeq(int[]):int

    /** Shifts a value into position. */
    private void shift(int src[], int pos, int offset) {

        int temp = src[offset];
        for (int i = offset; i > pos; i--) {
            src[i] = src[i - 1];
        }
        src[pos] = temp;

    } // shift(int[],int,int)

    /** Simple sort. */
    private void sort(int src[], final int offset, final int length) {

        for (int i = offset; i < offset + length - 1; i++) {
            int lowest = i;
            for (int j = i + 1; j < offset + length; j++) {
                if (src[j] < src[lowest]) {
                    lowest = j;
                }
            }
            if (lowest != i) {
                int temp = src[i];
                src[i] = src[lowest];
                src[lowest] = temp;
            }
        }

    } // sort(int[],int,int)

    /** Swaps two values. */
    private void swap(int src[], int i, int j) {

        int temp = src[i];
        src[i] = src[j];
        src[j] = temp;

    } // swap(int[],int,int)

    /***
    private void print(int indexes[], int offset, boolean mark) {

        for (int i = 0; i < indexes.length; i++) {
            System.out.print(offset==i?'.':' ');
            System.out.print(indexes[i]);
        }
        if (mark) {
            System.out.print(" *");
        }
        System.out.println();

    } // print(int[],int,boolean)
    /***/

    /** Builds the children content model. */
/*    private int buildChildrenModel(Element model, int type) throws Exception {

        // is there anything to do?
        if (model == null) {
            return -1;
        }

        // fill parent node
        int parentValue = -1;
        int parentOtherValue = -1;

        // build content model bottom-up
        int index = -1;
        for (Element child = XUtil.getFirstChildElement(model);
             child != null;
             child = XUtil.getNextSiblingElement(child)) {

            //
            // leaf
            //

            String childName = child.getNodeName();
            if (childName.equals("elementTypeRef")) {

                // add element name to symbol table
                String elementType = child.getAttribute(ATT_NAME);
                int elementTypeIndex = fStringPool.addSymbol(elementType);

                // create leaf node
                index = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_LEAF,
                                                            elementTypeIndex, -1, false);

                // set occurrence count
                index = expandContentModel(index, child);

            }

            //
            // all
            //

            else if (childName.equals(ATTVAL_ALL)) {

                index = buildAllModel(child);

            }

            //
            // choice or sequence
            //

            else {
                int childType = childName.equals(ATTVAL_CHOICE)
                              ? XMLContentSpec.CONTENTSPECNODE_CHOICE
                              : XMLContentSpec.CONTENTSPECNODE_SEQ;
                index = buildChildrenModel(child, childType);
            }

            // add to parent node
            if (parentValue == -1) {
                parentValue = index;
            }
            else if (parentOtherValue == -1) {
                parentOtherValue = index;
            }
            else {
                parentValue = fValidator.addContentSpecNode(type, parentValue, parentOtherValue, false);
                parentOtherValue = index;
            }

        } // for all children

        // set model type
        index = fValidator.addContentSpecNode(type, parentValue, parentOtherValue, false);

        // set occurrence count
        index = expandContentModel(index, model);

        // return last content spec node
        return index;

    } // buildChildrenModel(Element,int):int
*/
    private int expandContentModel(int contentSpecNodeIndex, Element element) throws Exception {

        // set occurrence count
        int occurs = getOccurrenceCount(element);
        int m = 1, n = 1;
        if (!isSimpleOccurrenceCount(occurs)) {
            try { m = Integer.parseInt(element.getAttribute(ATT_MINOCCURS)); }
            catch (NumberFormatException e) {
				reportSchemaError(SchemaMessageProvider.ValueNotInteger,
								  new Object [] { ATT_MINOCCURS });
			}
            try { n = Integer.parseInt(element.getAttribute(ATT_MAXOCCURS)); }
            catch (NumberFormatException e) {
				reportSchemaError(SchemaMessageProvider.ValueNotInteger,
								  new Object [] { ATT_MAXOCCURS });
			}
        }

		switch (occurs) {

            //
            // +
            //

            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                //System.out.println("occurs = +");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE,
                                                            contentSpecNodeIndex, -1, false);
                break;
            }

            //
            // *
            //

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: {
                //System.out.println("occurs = *");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                            contentSpecNodeIndex, -1, false);
                break;
            }

            //
            // ?
            //

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE: {
                //System.out.println("occurs = ?");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                            contentSpecNodeIndex, -1, false);
                break;
            }

            //
            // M -> *
            //

            case CONTENTSPECNODE_M_OR_MORE: {
                //System.out.println("occurs = "+m+" -> *");

                // create sequence node
                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add required number
                for (int i = 1; i < m; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                    value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                            contentSpecNodeIndex, -1, false);

                // add optional part
                if (otherValue != -1) {
                    value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                value, otherValue, false);
                }
                otherValue = contentSpecNodeIndex;

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                            value, otherValue, false);
                break;
            }

            //
            // M -> N
            //

            case CONTENTSPECNODE_M_TO_N: {
                //System.out.println("occurs = "+m+" -> "+n);

                // create sequence node
                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add required number
                for (int i = 1; i < m; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                    value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                            contentSpecNodeIndex, -1, false);

                // add optional number
                for (int i = n; i > m; i--) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                        value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                            value, otherValue, false);
                break;
            }

            //
            // 0 -> N
            //

            case CONTENTSPECNODE_ZERO_TO_N: {
                //System.out.println("occurs = 0 -> "+n);

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                            contentSpecNodeIndex, -1, false);

                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add optional number
                for (int i = 1; i < n; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                    value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                            value, otherValue, false);
                break;
            }

        } // switch

        //System.out.println("content = "+getContentSpecNodeAsString(contentSpecNodeIndex));
        return contentSpecNodeIndex;

    } // expandContentModel(int,int,int,int):int

    private boolean isSimpleOccurrenceCount(int occurs) {
        return occurs == -1 ||
               occurs == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE ||
               occurs == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE ||
               occurs == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE;
    }

    private int getOccurrenceCount(Element element) {

        String minOccur = element.getAttribute(ATT_MINOCCURS);
        String maxOccur = element.getAttribute(ATT_MAXOCCURS);

        if (minOccur.equals("0")) {
            if (maxOccur.equals("1") || maxOccur.length() == 0) {
                return XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE;
            }
            else if (maxOccur.equals("*")) {
                return XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE;
            }
            else {
                return CONTENTSPECNODE_ZERO_TO_N;
            }
        }
        else if (minOccur.equals("1") || minOccur.length() == 0) {
            if (maxOccur.equals("*")) {
                return XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE;
            }
            else if (!maxOccur.equals("1") && maxOccur.length() > 0) {
                return CONTENTSPECNODE_M_TO_N;
            }
        }
        else {
            if (maxOccur.equals("*")) {
                return CONTENTSPECNODE_M_OR_MORE;
            }
            else {
                return CONTENTSPECNODE_M_TO_N;
            }
        }

        // exactly one
        return -1;
    }

	private void reportSchemaError(int major, Object args[]) throws Exception {
//	    try {
    		fErrorReporter.reportError(fErrorReporter.getLocator(),
	    							   SchemaMessageProvider.SCHEMA_DOMAIN,
		    						   major,
			    					   SchemaMessageProvider.MSG_NONE,
				    				   args,
					    			   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
//		} catch (Exception e) {
//		    e.printStackTrace();
//		}
	}

    //
    // Classes
    //

    static class Resolver implements EntityResolver {

        private static final String SYSTEM[] = {
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000225/structures.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-2-20000225/datatypes.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000225/versionInfo.ent",
            };
        private static final String PATH[] = {
            "structures.dtd",
            "datatypes.dtd",
            "versionInfo.ent",
            };

        public InputSource resolveEntity(String publicId, String systemId)
            throws IOException {

            // looking for the schema DTDs?
            for (int i = 0; i < SYSTEM.length; i++) {
                if (systemId.equals(SYSTEM[i])) {
                    InputSource source = new InputSource(getClass().getResourceAsStream(PATH[i]));
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            }

            // use default resolution
            return null;

        } // resolveEntity(String,String):InputSource

    } // class Resolver

    static class ErrorHandler implements org.xml.sax.ErrorHandler {

        /** Warning. */
        public void warning(SAXParseException ex) {
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Error. */
        public void error(SAXParseException ex) {
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }

        /** Fatal error. */
        public void fatalError(SAXParseException ex) throws SAXException {
            System.err.println("[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
            throw ex;
        }

        //
        // Private methods
        //

        /** Returns a string of the location. */
        private String getLocationString(SAXParseException ex) {
            StringBuffer str = new StringBuffer();

            String systemId_ = ex.getSystemId();
            if (systemId_ != null) {
                int index = systemId_.lastIndexOf('/');
                if (index != -1)
                    systemId_ = systemId_.substring(index + 1);
                str.append(systemId_);
            }
            str.append(':');
            str.append(ex.getLineNumber());
            str.append(':');
            str.append(ex.getColumnNumber());

            return str.toString();

        } // getLocationString(SAXParseException):String
    }

    class DatatypeValidatorRegistry {
        Hashtable fRegistry = new Hashtable();

        String integerSubtypeTable[][] = {
            { "non-negative-integer", DatatypeValidator.MININCLUSIVE , "0"},
            { "positive-integer", DatatypeValidator.MININCLUSIVE, "1"},
            { "non-positive-integer", DatatypeValidator.MAXINCLUSIVE, "0"},
            { "negative-integer", DatatypeValidator.MAXINCLUSIVE, "-1"}
        };

        void initializeRegistry() {
            Hashtable facets = null;
            fRegistry.put("boolean", new BooleanValidator());
            DatatypeValidator integerValidator = new IntegerValidator();
            fRegistry.put("integer", integerValidator);
            fRegistry.put("string", new StringValidator());
            fRegistry.put("decimal", new DecimalValidator());
            fRegistry.put("float", new FloatValidator());
            fRegistry.put("double", new DoubleValidator());
            fRegistry.put("timeDuration", new TimeDurationValidator());
            fRegistry.put("timeInstant", new TimeInstantValidator());
            fRegistry.put("binary", new BinaryValidator());
            fRegistry.put("uri", new URIValidator());
            //REVISIT - enable the below
            //fRegistry.put("date", new DateValidator());
            //fRegistry.put("timePeriod", new TimePeriodValidator());
            //fRegistry.put("time", new TimeValidator());


            DatatypeValidator v = null;
            for (int i = 0; i < integerSubtypeTable.length; i++) {
                v = new IntegerValidator();
                facets = new Hashtable();
                facets.put(integerSubtypeTable[i][1],integerSubtypeTable[i][2]);
                v.setBasetype(integerValidator);
                try {
                    v.setFacets(facets);
                } catch (IllegalFacetException ife) {
                    System.out.println("Internal error initializing registry - Illegal facet: "+integerSubtypeTable[i][0]);
                } catch (IllegalFacetValueException ifve) {
                    System.out.println("Internal error initializing registry - Illegal facet value: "+integerSubtypeTable[i][0]);
                } catch (UnknownFacetException ufe) {
                    System.out.println("Internal error initializing registry - Unknown facet: "+integerSubtypeTable[i][0]);
                }
                fRegistry.put(integerSubtypeTable[i][0], v);
            }
        }

        DatatypeValidator getValidatorFor(String type) {
            return (DatatypeValidator) fRegistry.get(type);
        }

        void addValidator(String name, DatatypeValidator v) {
            fRegistry.put(name,v);
        }
    }

    //
    //
    //
    final class AttValidatorDATATYPE implements XMLValidator.AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            try { // REVISIT - integrate w/ error handling
                String type = fStringPool.toString(enumHandle);
                DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
                if (v != null)
                    v.validate(fStringPool.toString(attValueHandle));
                else
                    reportSchemaError(SchemaMessageProvider.NoValidatorFor,
                                        new Object [] { type });
            } catch (InvalidDatatypeValueException idve) {
                reportSchemaError(SchemaMessageProvider.IncorrectDatatype,
                                    new Object [] { idve.getMessage() });
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Internal error in attribute datatype validation");
            }
            return attValueHandle;
        }
    }
    //
    //
    //
    public XMLValidator.AttributeValidator createDatatypeAttributeValidator() {
        return new AttValidatorDATATYPE();
    }
    public XMLContentModel createDatatypeContentModel(int elementIndex) {
        return new DatatypeContentModel(fDatatypeRegistry, fValidator, fStringPool, elementIndex);
    }
}
