/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000,2001 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.v1.dtd;

import org.apache.xerces.impl.v1.Grammar;
import org.apache.xerces.impl.v1.XMLAttributeDecl;
import org.apache.xerces.impl.v1.XMLContentSpec;
import org.apache.xerces.impl.v1.XMLElementDecl;
import org.apache.xerces.impl.v1.datatype.DatatypeValidatorFactoryImpl;
import org.apache.xerces.impl.v1.schema.XUtil;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.QName;

/**
 * A DTD grammar. This class is an EventHandler to receive callbacks from
 * the XMLDTDScanner. When the callbacks are received, the grammar structures
 * are directly populated from the callback information.
 * <p>
 * In addition to being a recipient of scanner callbacks, the DTD grammar
 * class can act as a pass-through filter for the DTD events. This is useful
 * for parsers that must expose the DTD information to the application. (e.g.
 * SAX2 DeclHandler callbacks.)
 *
 * @author Andy Clark
 * @version $Id$
 */
public class DTDGrammar
    extends Grammar {

    // REVISIT: The grammar access currently implemented in this grammar
    //          instance is a draft implementation and should be revisited
    //          in order to design a proper DTD for the this grammar
    //          representation. -Ac

    //
    // Constants
    //

    /** Chunk shift. */
    private static final int CHUNK_SHIFT = 8; // 2^8 = 256

    /** Chunk size. */
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);

    /** Chunk mask. */
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;

    /** Initial chunk count. */
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT)); // 2^10 = 1k

    //
    // Data
    //

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    // "compiled" information structures

    /** Element declaration. */
    private final XMLElementDecl fElementDecl = new XMLElementDecl();

    /** Attribute declaration. */
    private final XMLAttributeDecl fAttributeDecl = new XMLAttributeDecl();

    /** Content spec node. */
    private final XMLContentSpec fContentSpec = new XMLContentSpec();

    private final QName fRootElementQName = new QName();

    // pass-through

    /** flag if the elementDecl is External. */
    private int fElementDeclIsExternal[][] = new int[INITIAL_CHUNK_COUNT][];
    /** Mapping for element declarations. */
    private int fElementDeclMap[][] = new int[INITIAL_CHUNK_COUNT][];

    /** flag if the AttributeDecl is External. */
    private int fAttributeDeclIsExternal[][] = new int[INITIAL_CHUNK_COUNT][];
    /** Mapping for attribute declarations. */
    private int fAttributeDeclMap[][] = new int[INITIAL_CHUNK_COUNT][];

    /** Mapping for content spec nodes. */
    private int fContentSpecMap[][] = new int[INITIAL_CHUNK_COUNT][];

    // temp vars

    private final QName fQName = new QName();

    private String fEmptySymbol;

    //
    // Constructors
    //

    public DTDGrammar(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
        fEmptySymbol = fSymbolTable.addSymbol("");
    } // <init>(SymbolTable)

    //
    // Public methods
    //

    /**
     * Add an element declaration (forward reference)
     *
     * @param handle to the name of the element being declared
     * @return handle to the element whose declaration was added
     * @exception java.lang.Exception
     */
    public int addElementDecl(QName elementDecl) throws Exception {

        // create element decl
        int elementDeclIndex = createElementDecl();

        // set element decl values
        fElementDecl.clear();
        fElementDecl.name.setValues(elementDecl);
        setElementDecl(elementDeclIndex, fElementDecl);

        // return index
        return elementDeclIndex;

    } // addElementDecl(QName):int

    /**
     * Add an element declaration
     *
     * @param handle to the name of the element being declared
     * @param contentSpecType handle to the type name of the content spec
     * @param ContentSpec handle to the content spec node for the contentSpecType
     * @return handle to the element declaration that was added 
     * @exception java.lang.Exception
     */
    public int addElementDecl(QName elementDecl, 
                              int contentSpecType, 
                              int contentSpec,
                              boolean isExternal) throws Exception {

        // create element decl
        int elementDeclIndex = createElementDecl();

        // set element decl values
        fElementDecl.clear();
        fElementDecl.name.setValues(elementDecl);
        fElementDecl.type = contentSpecType;
        fElementDecl.contentSpecIndex = contentSpec;
        setElementDecl(elementDeclIndex, fElementDecl);
        
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex & CHUNK_MASK;
        ensureElementDeclCapacity(chunk);
        fElementDeclIsExternal[chunk][index] = isExternal? 1 : 0;

        // return index
        return elementDeclIndex;

    } // addElementDecl(QName,int,int):int

    protected void putElementNameMapping(QName name, int scope,
                                         int elementDeclIndex) {
        fQName.uri = fEmptySymbol;
        fQName.localpart = name.rawname;
        super.putElementNameMapping(fQName, scope, elementDeclIndex);
    }

    public int getElementDeclIndex(QName element, int scopeIndex) {
        return super.getElementDeclIndex(element.rawname, -1);
    }

    public void setElementDeclDTD(int elementDeclIndex, XMLElementDecl elementDecl) {
        super.setElementDecl(elementDeclIndex, elementDecl);
    }

    private XMLContentSpec fTempContentSpec = new XMLContentSpec();

    public void setElementDeclIsExternal(int elementDeclIndex, boolean  isExternal) {
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex & CHUNK_MASK;
        ensureElementDeclCapacity(chunk);
        fElementDeclIsExternal[chunk][index] = isExternal? 1 : 0;
    }

    // getters for isExternals 
    public boolean getElementDeclIsExternal(int elementDeclIndex) {
        if (elementDeclIndex < 0) {
            return false;
        }
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex & CHUNK_MASK;
        return (fElementDeclIsExternal[chunk][index] != 0);
    }

    public boolean getAttributeDeclIsExternal(int attributeDeclIndex) {
        if (attributeDeclIndex < 0) {
            return false;
        }
        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex & CHUNK_MASK;
        return (fAttributeDeclIsExternal[chunk][index] != 0);
    }

    public boolean getRootElementQName(QName root) {
        if (fRootElementQName.rawname == null) {
            return false;
        }
        root.setValues(fRootElementQName);
        return true;
    }

    /**
     * Add an attribute definition
     *
     * @param handle to the element whose attribute is being declared
     * @param attName StringPool handle to the attribute name being declared
     * @param attType type of the attribute
     * @param enumeration StringPool handle of the attribute's enumeration list (if any)
     * @param attDefaultType an integer value denoting the DefaultDecl value
     * @param attDefaultValue StringPool handle of this attribute's default value
     * @return handle to the attribute definition
     * @exception java.lang.Exception
     */
    public int addAttDef(QName elementDecl, QName attributeDecl, 
                         int attType, boolean attList, int enumeration, 
                         int attDefaultType, String attDefaultValue, boolean isExternal) 
        throws Exception {

        // create attribute decl
        int attributeDeclIndex = createAttributeDecl();

        /***
        // find the dataTypeValidator associcated with this attType
        String attTypeString = "";
        switch (attType) {
        case XMLAttributeDecl.TYPE_CDATA:
            attTypeString = "string";
        case XMLAttributeDecl.TYPE_ENTITY:
            attTypeString = "ENTITY";;
        case XMLAttributeDecl.TYPE_ENUMERATION:
            attTypeString = "ENUMERATION";;
        case XMLAttributeDecl.TYPE_ID:
            attTypeString = "ID";;
        case XMLAttributeDecl.TYPE_IDREF:
            attTypeString = "IDREF";;
        case XMLAttributeDecl.TYPE_NMTOKEN:
            attTypeString = "NMTOKEN";;
        case XMLAttributeDecl.TYPE_NOTATION:
            attTypeString = "NOTATION";;
        default:
            ;
        }
        /***/

        // set attribute decl values
        fAttributeDecl.clear();
        fAttributeDecl.name.setValues(attributeDecl);
        fAttributeDecl.type = attType;
        fAttributeDecl.list = attList;
        fAttributeDecl.enumeration = enumeration;
        /***
        fAttributeDecl.datatypeValidator = 
            DatatypeValidatorFactoryImpl.getDatatypeRegistry().getDatatypeValidator(attTypeString);
        ****/
        fAttributeDecl.defaultType = attDefaultType;
        fAttributeDecl.defaultValue = attDefaultValue;

        int elementDeclIndex = getElementDeclIndex(elementDecl, -1);
        setAttributeDecl(elementDeclIndex, attributeDeclIndex, fAttributeDecl);

        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex & CHUNK_MASK;
        ensureAttributeDeclCapacity(chunk);
        fAttributeDeclIsExternal[chunk][index] = isExternal ?  1 : 0;

        // return index
        return attributeDeclIndex;

    } // addAttDef(QName,QName,int,int,int,int):int

    /**
     * create an XMLContentSpec for a leaf
     *
     * @param nameIndex StringPool handle to the name (Element) for the node
     * @return handle to the newly create XMLContentSpec
     * @exception java.lang.Exception
     */
    public int addUniqueLeafNode(QName name) throws Exception {

        // create content spec node
        int contentSpecIndex = createContentSpec();

        // set content spec node values
        fContentSpec.setValues(name);
        setContentSpec(contentSpecIndex, fContentSpec);

        // return index 
        return contentSpecIndex;

    } // addUniqueLeafNode(int):int

    /**
     * Create an XMLContentSpec for a single non-leaf
     * 
     * @param nodeType the type of XMLContentSpec to create - from XMLContentSpec.CONTENTSPECNODE_*
     * @param nodeValue handle to an XMLContentSpec
     * @return handle to the newly create XMLContentSpec
     * @exception java.lang.Exception
     */
    public int addContentSpecNode(int nodeType, 
                                  int nodeValue) throws Exception {

        // create content spec node
        int contentSpecIndex = createContentSpec();

        // set content spec node values
        fContentSpec.setValues(nodeType, nodeValue, -1);
        setContentSpec(contentSpecIndex, fContentSpec);

        // return index 
        return contentSpecIndex;

    } // addContentSpecNode(int,int):int

    /**
     * Create an XMLContentSpec for a two child leaf
     *
     * @param nodeType the type of XMLContentSpec to create - from XMLContentSpec.CONTENTSPECNODE_*
     * @param leftNodeIndex handle to an XMLContentSpec
     * @param rightNodeIndex handle to an XMLContentSpec
     * @return handle to the newly create XMLContentSpec
     * @exception java.lang.Exception
     */
    public int addContentSpecNode(int nodeType, 
                                  int leftNodeIndex, 
                                  int rightNodeIndex) throws Exception {

        // create content spec node
        int contentSpecIndex = createContentSpec();

        // set content spec node values
        fContentSpec.setValues(nodeType, 
                               leftNodeIndex, rightNodeIndex);
        setContentSpec(contentSpecIndex, fContentSpec);

        // return index 
        return contentSpecIndex;

    } // addContentSpecNode(int,int,int):int

    /**
     * Create a string representation of an XMLContentSpec tree
     * 
     * @param handle to an XMLContentSpec
     * @return String representation of the content spec tree
     * @exception java.lang.Exception
     */
    public String getContentSpecNodeAsString(int nodeIndex) throws Exception {
        return XMLContentSpec.toString(this, nodeIndex);
    }

    /**
     * Start the scope of an entity declaration.
     *
     * @return <code>true</code> on success; otherwise
     *         <code>false</code> if the entity declaration is recursive.
     * @exception java.lang.Exception
     */
    public boolean startEntityDecl(boolean isPE, int entityName) 
        throws Exception {
        return true;
    } // startEntityDecl(boolean,int):boolean

    /**
     * End the scope of an entity declaration.
     * @exception java.lang.Exception
     */
    public void endEntityDecl() throws Exception {
    } // endEntityDecl()

    /**
     * Add a declaration for an internal parameter entity
     *
     * @param name StringPool handle of the parameter entity name
     * @param value StringPool handle of the parameter entity value
     * @return handle to the parameter entity declaration
     * @exception java.lang.Exception
     */
    public int addInternalPEDecl(int name, int value) throws Exception {
        return -1;
    } // addInternalPEDecl(int,int):int

    /**
     * Add a declaration for an external parameter entity
     *
     * @param name StringPool handle of the parameter entity name
     * @param publicId StringPool handle of the publicId
     * @param systemId StringPool handle of the systemId
     * @return handle to the parameter entity declaration
     * @exception java.lang.Exception
     */
    public int addExternalPEDecl(int name, 
                                 int publicId, 
                                 int systemId) throws Exception {
        return -1;
    } // addExternalPEDecl(int,int,int):int

    /**
     * Add a declaration for an internal entity
     *
     * @param name StringPool handle of the entity name
     * @param value StringPool handle of the entity value
     * @return handle to the entity declaration
     * @exception java.lang.Exception
     */
    public int addInternalEntityDecl(int name, int value) throws Exception {
        return -1;
    } // addInternalEntityDecl(int,int):int

    /**
     * Add a declaration for an entity
     *
     * @param name StringPool handle of the entity name
     * @param publicId StringPool handle of the publicId
     * @param systemId StringPool handle of the systemId
     * @return handle to the entity declaration
     * @exception java.lang.Exception
     */
    public int addExternalEntityDecl(int name, 
                                     int publicId, 
                                     int systemId) throws Exception {
        return -1;
    } // addExternalEntityDecl(int,int,int):int

    /**
     * Add a declaration for an unparsed entity
     *
     * @param name StringPool handle of the entity name
     * @param publicId StringPool handle of the publicId
     * @param systemId StringPool handle of the systemId
     * @param notationName StringPool handle of the notationName
     * @return handle to the entity declaration
     * @exception java.lang.Exception
     */
    public int addUnparsedEntityDecl(int name, 
                                     int publicId, int systemId, 
                                     int notationName) throws Exception {
        return -1;
    } // addUnparsedEntityDecl(int,int,int,int):int

    /**
     * Called when the scanner start scanning an enumeration
     * @return StringPool handle to a string list that will hold the enumeration names
     * @exception java.lang.Exception
     */
    public int startEnumeration() throws Exception {
        // create enumeration list in string pool
        return -1;
    } // startEnumeration():int

    /**
     * Add a name to an enumeration
     * @param enumIndex StringPool handle to the string list for the enumeration
     * @param elementType handle to the element that owns the attribute with the enumeration
     * @param attrName StringPool handle to the name of the attribut with the enumeration
     * @param nameIndex StringPool handle to the name to be added to the enumeration
     * @param isNotationType true if the enumeration is an enumeration of NOTATION names
     * @exception java.lang.Exception
     */
    public void addNameToEnumeration(int enumIndex, 
                                     int elementType, int attrName, 
                                     int nameIndex, 
                                     boolean isNotationType) throws Exception {
        // add enumeration value to list
    } // addNameToEnumeration(int,int,int,int,boolean)

    /**
     * Finish processing an enumeration
     *
     * @param enumIndex handle to the string list which holds the enumeration to be finshed.
     * @exception java.lang.Exception
     */
    public void endEnumeration(int enumIndex) throws Exception {
        //finish the enumeration stringlist int the fStringPool
    } // endEnumeration(int)

    /**
     * Add a declaration for a notation
     *
     * @param notationName
     * @param publicId
     * @param systemId
     * @return handle to the notation declaration
     * @exception java.lang.Exception
     */
    public int addNotationDecl(int notationName, 
                               int publicId, int systemId) throws Exception {
        return -1;
    } // addNotationdecl(int,int,int):int

    /**
     * Called when a comment has been scanned
     *
     * @param data StringPool handle of the comment text
     * @exception java.lang.Exception
     */
    public void callComment(int data) throws Exception {
    }

    /**
     * Called when a processing instruction has been scanned
     * @param piTarget StringPool handle of the PI target
     * @param piData StringPool handle of the PI data
     * @exception java.lang.Exception
     */
    public void callProcessingInstruction(int piTarget, int piData) 
        throws Exception {
    } // callProcessingInstruction(int,int)

    // deprecated -- removed from DOM Level 2

    /**
     * Supports DOM Level 2 internalSubset additions.
     * Called when the internal subset is completely scanned.
     */
    public void internalSubset(int internalSubset) throws Exception {
    }

    protected boolean isDTD() {
        return true;
    }

    //
    // Private methods
    //

    // ensure capacity

    /** Ensures storage for element declaration mappings. */
    private boolean ensureElementDeclCapacity(int chunk) {
        try {
            return fElementDeclMap[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            fElementDeclMap = resize(fElementDeclMap, 
                                     fElementDeclMap.length * 2);
            fElementDeclIsExternal = resize(fElementDeclIsExternal, 
                                     fElementDeclIsExternal.length * 2);
        } catch (NullPointerException ex) {
            // ignore
        }
        fElementDeclMap[chunk] = new int[CHUNK_SIZE];
        fElementDeclIsExternal[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    /** Ensures storage for attribute declaration mappings. */
    private boolean ensureAttributeDeclCapacity(int chunk) {
        try {
            return fAttributeDeclMap[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            fAttributeDeclMap = resize(fAttributeDeclMap, 
                                       fAttributeDeclMap.length * 2);
            fAttributeDeclIsExternal = resize(fAttributeDeclIsExternal, 
                                       fAttributeDeclIsExternal.length * 2);
        } catch (NullPointerException ex) {
            // ignore
        }
        fAttributeDeclMap[chunk] = new int[CHUNK_SIZE];
        fAttributeDeclIsExternal[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    /** Ensures storage for content spec mappings. */
    private boolean ensureContentSpecCapacity(int chunk) {
        try {
            return fContentSpecMap[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            fContentSpecMap = resize(fContentSpecMap, 
                                     fContentSpecMap.length * 2);
        } catch (NullPointerException ex) {
            // ignore
        }
        fContentSpecMap[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    // resize initial chunk

    /** Resizes chunked integer arrays. */
    private int[][] resize(int array[][], int newsize) {
        int newarray[][] = new int[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

} // class DTDGrammar
