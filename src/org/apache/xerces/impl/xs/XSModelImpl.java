/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.impl.xs.util.*;
import java.util.Vector;

/**
 * Implements XSModel:  a read-only interface that represents an XML Schema,
 * which could be components from different namespaces.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class XSModelImpl implements XSModel {

    // the max index / the max value of XSObject type
    private static final short MAX_COMP_IDX = XSTypeDefinition.SIMPLE_TYPE;
    private static final boolean[] GLOBAL_COMP = {false,    // null
                                                  true,     // attribute
                                                  true,     // element
                                                  true,     // type
                                                  false,    // attribute use
                                                  true,     // attribute group
                                                  true,     // group
                                                  false,    // model group
                                                  false,    // particle
                                                  false,    // wildcard
                                                  false,    // idc
                                                  true,     // notation
                                                  false,    // annotation
                                                  true,     // complex type
                                                  true      // simple type
                                                 };

    // number of grammars/namespaces stored here
    private int fGrammarCount;
    // all target namespaces
    private String[] fNamespaces;
    // all schema grammar objects (for each namespace)
    private SchemaGrammar[] fGrammarList;
    // a map from namespace to schema grammar
    private SymbolHash fGrammarMap;
    
    // store a certain kind of components from all namespaces
    private XSNamedMap[] fGlobalComponents;
    // store a certain kind of components from one namespace
    private XSNamedMap[][] fNSComponents;
    
    // temporary array
    private SymbolHash[] fTables;
    
   /**
    * Construct an XSModelImpl, by storing some grammars and grammars imported
    * by them to this object.
    * 
    * @param grammars   the array of schema grammars
    */
    public XSModelImpl(SchemaGrammar[] grammars) {
        // copy namespaces/grammars from the array to our arrays
        int len = grammars.length;
        fNamespaces = new String[Math.max(len, 5)];
        fGrammarList = new SchemaGrammar[Math.max(len, 5)];
        for (int i = 0; i < len; i++) {
            fNamespaces[i] = grammars[i].getTargetNamespace();
            fGrammarList[i] = grammars[i];
        }

        SchemaGrammar sg1, sg2;
        Vector gs;
        int i, j, k;
        // and recursively get all imported grammars, add them to our arrays
        for (i = 0; i < len; i++) {
            // get the grammar
            sg1 = fGrammarList[i];
            gs = sg1.getImportedGrammars();
            // for each imported grammar
            for (j = gs == null ? -1 : gs.size() - 1; j >= 0; j--) {
                sg2 = (SchemaGrammar)gs.elementAt(j);
                // check whether this grammar is already in the list
                for (k = 0; k < len; k++) {
                    if (sg2 == fGrammarList[k])
                        break;
                }
                // if it's not, add it to the list
                if (k == len) {
                    // ensure the capacity of the arrays
                    if (len == fGrammarList.length) {
                        String[] newSA = new String[len*2];
                        System.arraycopy(fNamespaces, 0, newSA, 0, len);
                        fNamespaces = newSA;
                        SchemaGrammar[] newGA = new SchemaGrammar[len*2];
                        System.arraycopy(fGrammarList, 0, newGA, 0, len);
                        fGrammarList = newGA;
                    }
                    fNamespaces[len] = sg2.getTargetNamespace();
                    fGrammarList[len] = sg2;
                    len++;
                }
            }
        }

        // establish the mapping from namespace to grammars
        fGrammarMap = new SymbolHash(len*2);
        for (i = 0; i < len; i++) {
            fGrammarMap.put(null2EmptyString(fNamespaces[i]), fGrammarList[i]);
        }
        
        fGrammarCount = len;
        fGlobalComponents = new XSNamedMap[MAX_COMP_IDX+1];
        fNSComponents = new XSNamedMap[len][MAX_COMP_IDX+1];
        
        fTables = new SymbolHash[fGrammarCount];
    }
    
    /**
     * Convenience method. Returns a list of all namespaces that belong to
     * this schema.
     * @return A list of all namespaces that belong to this schema or
     *   <code>null</code> if all components don't have a targetNamespace.
     */
    public StringList getNamespaces() {
        // REVISIT: should the type of fNamespace be StringListImpl?
        return new StringListImpl(fNamespaces, fGrammarCount);
    }

    /**
     * Convenience method. Returns a list of all [namespace schema information
     * item]s. There is one such item for each namespace (including the null
     * namespace). Enties of the returned enumeration are instances of
     * XSNamespaceItem interface.
     * @return A list of namespace items that belong to this schema.
     */
    public ObjectList getNamespaceItems() {

        // REVISIT: should the type of fGrammarList be ObjectListImpl?
        return new ObjectListImpl(fGrammarList, fGrammarCount);
    }

    /**
     * Returns a list of top-level components, i.e. element declarations,
     * attribute declarations, etc.
     * @param objectType The type of the declaration, i.e.
     *   ELEMENT_DECLARATION, ATTRIBUTE_DECLARATION, etc.
     * @return A list of top-level definition of the specified type in
     *   <code>objectType</code> or <code>null</code>.
     */
    public synchronized XSNamedMap getComponents(short objectType) {
        if (objectType <= 0 || objectType > MAX_COMP_IDX ||
            !GLOBAL_COMP[objectType]) {
            return null;
        }
        
        // get all hashtables from all namespaces for this type of components
        if (fGlobalComponents[objectType] == null) {
            for (int i = 0; i < fGrammarCount; i++) {
                switch (objectType) {
                case XSConstants.TYPE_DEFINITION:
                case XSTypeDefinition.COMPLEX_TYPE:
                case XSTypeDefinition.SIMPLE_TYPE:
                    fTables[i] = fGrammarList[i].fGlobalTypeDecls;
                    break;
                case XSConstants.ATTRIBUTE_DECLARATION:
                    fTables[i] = fGrammarList[i].fGlobalAttrDecls;
                    break;
                case XSConstants.ELEMENT_DECLARATION:
                    fTables[i] = fGrammarList[i].fGlobalElemDecls;
                    break;
                case XSConstants.ATTRIBUTE_GROUP:
                    fTables[i] = fGrammarList[i].fGlobalAttrGrpDecls;
                    break;
                case XSConstants.MODEL_GROUP_DEFINITION:
                    fTables[i] = fGrammarList[i].fGlobalGroupDecls;
                    break;
                case XSConstants.NOTATION_DECLARATION:
                    fTables[i] = fGrammarList[i].fGlobalNotationDecls;
                    break;
                }
            }
            // for complex/simple types, create a special implementation,
            // which take specific types out of the hash table
            if (objectType == XSTypeDefinition.COMPLEX_TYPE ||
                objectType == XSTypeDefinition.SIMPLE_TYPE) {
                fGlobalComponents[objectType] = new XSNamedMap4Types(fNamespaces, fTables, fGrammarCount, objectType);
            }
            else {
                fGlobalComponents[objectType] = new XSNamedMapImpl(fNamespaces, fTables, fGrammarCount);
            }
        }
        
        return fGlobalComponents[objectType];
    }

    /**
     * Convenience method. Returns a list of top-level component declarations
     * that are defined within the specified namespace, i.e. element
     * declarations, attribute declarations, etc.
     * @param objectType The type of the declaration, i.e.
     *   ELEMENT_DECLARATION, ATTRIBUTE_DECLARATION, etc.
     * @param namespace The namespace to which declaration belong or
     *   <code>null</code> (for components with no targetNamespace).
     * @return A list of top-level definition of the specified type in
     *   <code>objectType</code> and defined in the specified
     *   <code>namespace</code> or <code>null</code>.
     */
    public synchronized XSNamedMap getComponentsByNamespace(short objectType,
                                                            String namespace) {
        if (objectType <= 0 || objectType > MAX_COMP_IDX ||
            !GLOBAL_COMP[objectType]) {
            return null;
        }
        
        // try to find the grammar
        int i = 0;
        for (; i < fGrammarCount; i++) {
            if (fNamespaces[i] == namespace)
                break;
        }
        if (i == fGrammarCount)
            return null;
        
        // get the hashtable for this type of components
        if (fNSComponents[i][objectType] == null) {
            SymbolHash table = null;
            switch (objectType) {
            case XSConstants.TYPE_DEFINITION:
            case XSTypeDefinition.COMPLEX_TYPE:
            case XSTypeDefinition.SIMPLE_TYPE:
                table = fGrammarList[i].fGlobalTypeDecls;
                break;
            case XSConstants.ATTRIBUTE_DECLARATION:
                table = fGrammarList[i].fGlobalAttrDecls;
                break;
            case XSConstants.ELEMENT_DECLARATION:
                table = fGrammarList[i].fGlobalElemDecls;
                break;
            case XSConstants.ATTRIBUTE_GROUP:
                table = fGrammarList[i].fGlobalAttrGrpDecls;
                break;
            case XSConstants.MODEL_GROUP_DEFINITION:
                table = fGrammarList[i].fGlobalGroupDecls;
                break;
            case XSConstants.NOTATION_DECLARATION:
                table = fGrammarList[i].fGlobalNotationDecls;
                break;
            }
            
            // for complex/simple types, create a special implementation,
            // which take specific types out of the hash table
            if (objectType == XSTypeDefinition.COMPLEX_TYPE ||
                objectType == XSTypeDefinition.SIMPLE_TYPE) {
                fNSComponents[i][objectType] = new XSNamedMap4Types(namespace, table, objectType);
            }
            else {
                fNSComponents[i][objectType] = new XSNamedMapImpl(namespace, table);
            }
        }
        
        return fNSComponents[i][objectType];
    }

    /**
     * Convenience method. Returns a top-level simple or complex type
     * definition.
     * @param name The name of the definition.
     * @param namespace The namespace of the definition, otherwise null.
     * @return An <code>XSTypeDefinition</code> or null if such definition
     *   does not exist.
     */
    public XSTypeDefinition getTypeDefinition(String name,
                                              String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSTypeDefinition)sg.fGlobalTypeDecls.get(name);
    }

    /**
     * Convenience method. Returns a top-level attribute declaration.
     * @param name The name of the declaration.
     * @param namespace The namespace of the definition, otherwise null.
     * @return A top-level attribute declaration or null if such declaration
     *   does not exist.
     */
    public XSAttributeDeclaration getAttributeDecl(String name,
                                                   String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSAttributeDeclaration)sg.fGlobalAttrDecls.get(name);
    }

    /**
     * Convenience method. Returns a top-level element declaration.
     * @param name The name of the declaration.
     * @param namespace The namespace of the definition, otherwise null.
     * @return A top-level element declaration or null if such declaration
     *   does not exist.
     */
    public XSElementDeclaration getElementDecl(String name,
                                               String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSElementDeclaration)sg.fGlobalElemDecls.get(name);
    }

    /**
     * Convenience method. Returns a top-level attribute group definition.
     * @param name The name of the definition.
     * @param namespace The namespace of the definition, otherwise null.
     * @return A top-level attribute group definition or null if such
     *   definition does not exist.
     */
    public XSAttributeGroupDefinition getAttributeGroup(String name,
                                                        String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSAttributeGroupDefinition)sg.fGlobalAttrGrpDecls.get(name);
    }

    /**
     * Convenience method. Returns a top-level model group definition.
     *
     * @param name      The name of the definition.
     * @param namespace The namespace of the definition, otherwise null.
     * @return A top-level model group definition definition or null if such
     *         definition does not exist.
     */
    public XSModelGroupDefinition getModelGroupDefinition(String name,
                                                          String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSModelGroupDefinition)sg.fGlobalGroupDecls.get(name);
    }

    /**
     * Convenience method. Returns a top-level notation declaration.
     *
     * @param name      The name of the declaration.
     * @param namespace The namespace of the definition, otherwise null.
     * @return A top-level notation declaration or null if such declaration
     *         does not exist.
     */
    public XSNotationDeclaration getNotationDecl(String name,
                                                 String namespace) {
        SchemaGrammar sg = (SchemaGrammar)fGrammarMap.get(null2EmptyString(namespace));
        if (sg == null)
            return null;
        return (XSNotationDeclaration)sg.fGlobalNotationDecls.get(name);
    }

    /**
     *  {annotations} A set of annotations.
     */
    public XSObjectList getAnnotations() {
        // REVISIT: SCAPI: to implement
        return null;
    }

    private static final String EMPTY_STRING = "";
    private static final String null2EmptyString(String str) {
        return str == null ? EMPTY_STRING : str;
    }
    
} // class XSModelImpl
