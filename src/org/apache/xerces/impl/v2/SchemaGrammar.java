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


import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.impl.v1.datatype.DatatypeValidator;
import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.validation.ContentModelValidator;


import java.lang.Integer;
import java.util.Hashtable;
import java.util.Vector;
/**
 * @version $Id$
 */
public class SchemaGrammar {

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

    // element, attribute, notation decl count
    private int fElementDeclCount = 0;
    private int fAttributeDeclCount = 0 ;
    private int fNotationCount = 0;

    // content spec count
    private int fContentSpecCount = 0;


    /** Element declaration name. */
    private QName fElementDeclName[][] = new QName[INITIAL_CHUNK_COUNT][];

    /** 
     * Element declaration default value. This value is used when
     * the element is of simple type.
     */
    private String fElementDeclDefaultValue[][] = new String[INITIAL_CHUNK_COUNT][];

    /** 
     * Element declaration default type. This value is used when
     * the element is of simple type.
     */
    private short   fElementDeclDefaultType[][] = new short[INITIAL_CHUNK_COUNT][];

    /** 
     * Element declaration datatype validator. This value is used when
     * the element is of simple type. 
     */

    //REVISIT: should DatatypeValidator implement XSType
    private DatatypeValidator fElementDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];

    /** 
     * Element declaration content spec index. This index value is used
     * to refer to the content spec information tables.
     */
    private int fElementDeclContentSpecIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private String fElementDeclSubGroupAffFullName[][] = new String[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclSubGroupQNames[][] = new Vector[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclAllSubGroupQNamesBlock[][] = new Vector[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclAllSubGroupQNames[][] = new Vector[INITIAL_CHUNK_COUNT][];
    private int fElementDeclBlockSet[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclFinalSet[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclMiscFlags[][] = new int[INITIAL_CHUNK_COUNT][];

    /** 
     * Element declaration content model validator. This validator is
     * constructed from the content spec nodes.
     */
    private ContentModelValidator fElementDeclContentModelValidator[][] = new ContentModelValidator[INITIAL_CHUNK_COUNT][];

    // attribute declarations

    /** Attribute declaration name. */
    private QName fAttributeDeclName[][] = new QName[INITIAL_CHUNK_COUNT][];

    /** 
     * Attribute declaration type.
     * @see XSAttributeDecl
     */
    private short fAttributeDeclType[][] = new short[INITIAL_CHUNK_COUNT][];
    private short fAttributeDeclDefaultType[][] = new short[INITIAL_CHUNK_COUNT][];
    private String fAttributeDeclDefaultValue[][] = new String[INITIAL_CHUNK_COUNT][];
    private int fAttributeDeclNextAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    //REVISIT: should DatatypeValidator implement XSType
    private DatatypeValidator fAttributeDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];

    // content specs

    // here saves the content spec binary trees for element decls, 
    // each element with a content model will hold a pointer which is 
    // the index of the head node of the content spec tree. 

    private short fContentSpecType[][] = new short[INITIAL_CHUNK_COUNT][];
    private Object fContentSpecValue[][] = new Object[INITIAL_CHUNK_COUNT][];
    private Object fContentSpecOtherValue[][] = new Object[INITIAL_CHUNK_COUNT][];

    // additional content spec tables
    // used if deferContentSpecExansion is enabled
    private int fContentSpecMinOccurs[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fContentSpecMaxOccurs[][] = new int[INITIAL_CHUNK_COUNT][];
    // store the original uri
    private int fContentSpecOrgUri[][] = new int[INITIAL_CHUNK_COUNT][];

    // notations

    private String fNotationName[][] = new String[INITIAL_CHUNK_COUNT][];
    private String[][] fNotationPublicId = new String[INITIAL_CHUNK_COUNT][];
    private String[][] fNotationSystemId = new String[INITIAL_CHUNK_COUNT][];


    // REVISIT:
    // complex type declarations
    // add attributes to complex type decls

    private Hashtable fComplexTypeRegistry = null;
    // REVISIT:
    // simple type declarations
    //

    // other information

    // global decls
    Hashtable topLevelGroupDecls = new Hashtable();
    Hashtable topLevelNotationDecls = new Hashtable();
    Hashtable topLevelAttrDecls  = new Hashtable();
    Hashtable topLevelAttrGrpDecls = new Hashtable();
    Hashtable topLevelElemDecls = new Hashtable();
    Hashtable topLevelTypeDecls = new Hashtable();

    // UPA checking

    // Set if we defer min/max expansion for content trees.   This is required if we
    // are doing particle derivation checking for schema.
    private boolean deferContentSpecExpansion = false;

    // Set if we check Unique Particle Attribution
    // This one onle takes effect when deferContentSpecExpansion is set
    private boolean checkUniqueParticleAttribution = false;
    private boolean checkingUPA = false;

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaGrammar(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
    } // <init>(SymbolTable)


} // class SchemaGrammar
