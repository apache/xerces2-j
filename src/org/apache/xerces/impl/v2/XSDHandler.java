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

package org.apache.xerces.impl.v2;

import org.apache.xerces.impl.v2.datatypes.*;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLManipulator;
//import GrammarResolver;???

import org.w3c.dom.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.Hashtable;
import java.util.Vector;

// The purpose of this class is to co-ordinate the construction of a
// grammar object corresponding to a schema.  To do this, it must be
// prepared to parse several schema documents (for instance if the
// schema document originally referred to contains <include> or
// <redefined> information items).  If any of the schemas imports a
// schema, other grammars may be constructed as a side-effect.

// @author Neil Graham, IBM
// @version $ID$

class XSDHandler {

    // data

    // different sorts of declarations; should make lookup and
    // traverser calling more efficient/less bulky.
    public final static int ATTRIBUTE_TYPE = 1;
    public final static int ATTRIBUTEGROUP_TYPE = 2;
    public final static int COMPLEXTYPE_TYPE = 4;
    public final static int ELEMENT_TYPE = 8;
    public final static int GROUP_TYPE = 16;
    public final static int IDENTITYCONSTRAINT_TYPE = 32;
    public final static int NOTATION_TYPE = 64;
    public final static int SIMPLETYPE_TYPE = 128;

    // These tables correspond to the symbol spaces defined in the
    // spec.
    // They are keyed with a QName (that is, String("URI,localpart) and
    // their values are nodes corresponding to the given name's decl.
    // By asking the node for its ownerDocument and looking in
    // XSDocumentInfoRegistry we can easily get the corresponding
    // XSDocumentInfo object.
    private Hashtable fUnparsedAttributeRegistry = new Hashtable();
    private Hashtable fUnparsedAttributeGroupRegistry = new Hashtable();
    private Hashtable fUnparsedElementRegistry = new Hashtable();
    private Hashtable fUnparsedGroupRegistry = new Hashtable();
    private Hashtable fUnparsedIdentityConstraintRegistry = new Hashtable();
    private Hashtable fUnparsedNotationRegistry = new Hashtable();
    private Hashtable fUnparsedTypeRegistry = new Hashtable();

    // this is keyed with a documentNode (or the schemaRoot nodes
    // contained in the XSDocumentInfo objects) and its value is the
    // XSDocumentInfo object corresponding to that document.
    // Basically, the function of this registry is to be a link
    // between the nodes we fetch from calls to the fUnparsed*
    // arrays and the XSDocumentInfos they live in.
    private Hashtable fXSDocumentInfoRegistry = new Hashtable();

    // this hashtable is keyed on by XSDocumentInfo objects.  Its values
    // are Vectors containing the XSDocumentInfo objects <include>d,
    // <import>ed or <redefine>d by the key XSDocumentInfo.
    private Hashtable fDependencyMap = new Hashtable();

    // the presence of an XSDocumentInfo object in this vector means it has
    // been completely traversed; needed to handle mutual <include>s
    // etc.
    private Vector fTraversed = new Vector();

    // the primary XSDocumentInfo we were called to parse
    private XSDocumentInfo fRoot = null;

    // the XMLErrorReporter
    private XMLErrorReporter fErrorReporter;

    // the XMLEntityResolver
    private XMLEntityResolver fEntityResolver;

    // the symbol table
    private SymbolTable fSymbolTable;

    // the GrammarResolver
    private GrammarResolver fGrammarResolver;

    // REVISIT:  old kind of DatatypeValidator...
    private DatatypeValidatorFactoryImpl fDatatypeRegistry = null;

    //************ Traversers **********
    XSDAttributeGroupTraverser fAttributeGroupTraverser;
    XSDAttributeTraverser fAttributeTraverser;
    XSDComplexTypeTraverser fComplexTypeTraverser;
    XSDElementTraverser fElementTraverser;
    XSDGroupTraverser fGroupTraverser;
    XSDNotationTraverser fNotationTraverser;
    XSDSimpleTypeTraverser fSimpleTypeTraverser;
    XSDWildcardTraverser fWildCardTraverser;

    // Constructors

    // it should be possible to use the same XSDHandler to parse
    // multiple schema documents; this will allow one to be
    // constructed.
    XSDHandler (GrammarResolver gResolver,
            XMLErrorReporter errorReporter,
            XMLEntityResolver entityResolver,
            SymbolTable symbolTable) {
        fEntityResolver = entityResolver;
        fErrorReporter = errorReporter;
        fGrammarResolver = gResolver;
        fSymbolTable = symbolTable;
        fDatatypeRegistry =
        (DatatypeValidatorFactoryImpl)fGrammarResolver.getDatatypeRegistry();
        createTraversers();
    } // end constructor

    // This method initiates the parse of a schema.  It will likely be
    // called from the Validator and it will make the
    // resulting grammar available; it returns a reference to this object just
    // in case.  An ErrorHandler, EntityResolver, GrammarPool and SymbolTable must
    // already have been set; the last thing this method does is reset
    // this object (i.e., clean the registries, etc.).
    SchemaGrammar parseSchema(String schemaNamespace,
            String schemaHint) {

        // first phase:  construct trees.
        Document schemaRoot = getSchema(schemaNamespace, schemaHint);
        fRoot = constructTrees(schemaRoot);

        // second phase:  fill global registries.
        buildGlobalNameRegistries();

        // third phase:  call traversers
        traverseSchemas();

        // fourth phase:  handle Keyrefs
        resolveKeyRefs();

        // fifth phase:  handle derivation constraint checking
        // and UPA

        // reset all traversers and SchemaHandler
        reset();

        // and return.
        return fGrammarResolver.get(fRoot.fTargetNamespace);
    } // end parseSchema

    // may wish to have setter methods for ErrorHandler,
    // EntityResolver...

    // This method does several things:
    // It constructs an instance of an XSDocumentInfo object using the
    // schemaRoot node.  Then, for each <include>,
    // <redefine>, and <import> children, it attempts to resolve the
    // requested schema document, initiates a DOM parse, and calls
    // itself recursively on that document's root.  It also records in
    // the DependencyMap object what XSDocumentInfo objects its XSDocumentInfo
    // depends on.
    protected XSDocumentInfo constructTrees(Document schemaRoot) {
        XSDocumentInfo currSchemaInfo = new
            XSDocumentInfo(schemaRoot);
        Vector dependencies = new Vector();
        Element rootNode = XMLManipulator.getRoot(schemaRoot);
        String schemaNamespace;
        String schemaHint;
        Document newSchemaRoot = null;
        for (Element child =
                XMLManipulator.getFirstChildElement(rootNode);
                child != null;
                child = XMLManipulator.getNextSiblingElement(child)) {
            String localName = XMLManipulator.getLocalName(child);
            if(localName.equals(SchemaSymbols.ELT_ANNOTATION))
                continue;
            else if(localName.equals(SchemaSymbols.ELT_IMPORT)) {
                // have to handle some validation here too!
                // call XSAttributeChecker to fill in attrs
                newSchemaRoot = getSchema(schemaNamespace, schemaHint);
            } else if ((localName.equals(SchemaSymbols.ELT_INCLUDE)) ||
                    (localName.equals(SchemaSymbols.ELT_REDEFINE))) {
                // validation for redefine/include will be the same here; just
                // make sure TNS is right (don't care about redef contents
                // yet).
                newSchemaRoot = getSchema(schemaNamespace, schemaHint);
            } else {
                // no more possibility of schema references in well-formed
                // schema...
                break;
            }
            XSDocumentInfo newSchemaInfo = constructTrees(newSchemaRoot);
            if (localName.equals(SchemaSymbols.ELT_REDEFINE)) {
                // must record which schema we're redefining so that we can
                // rename the right things later!
                fRedefine2XSDMap.put(child, newSchemaInfo);
            }
            dependencies.addElement(newSchemaInfo);
            newSchemaRoot = null;
        }
        fDependencyMap.put(currSchemaInfo, dependencies);
    } // end constructTrees

    // This method builds registries for all globally-referenceable
    // names.  A registry will be built for each symbol space defined
    // by the spec.  It is also this method's job to rename redefined
    // components, and to record which components redefine others (so
    // that implicit redefinitions of groups and attributeGroups can be handled).
    protected void buildGlobalNameRegistries() {
    } // end buildGlobalNameRegistries

    // this should only need to be called once during the construction
    // of this object; it creates the traversers that will be used to
    // construct schemaGrammars.
    protected void createTraversers() {
    } // createTraversers

    // Beginning at the first schema processing was requested for
    // (fRoot), this method
    // examines each child (global schema information item) of each
    // schema document (and of each <redefine> element)
    // corresponding to an XSDocumentInfo object.  If the
    // readOnly field on that node has not been set, it calls an
    // appropriate traverser to traverse it.  Once all global decls in
    // an XSDocumentInfo object have been traversed, it marks that object
    // as traversed (or hidden) in order to avoid infinite loops.  It completes
    // when it has visited all XSDocumentInfo objects in the
    // DependencyMap and marked them as traversed.
    protected void traverseSchemas() {
    } // end traverseSchemas

    // since it is forbidden for traversers to talk to each other
    // directly, this provides a generic means for a traverser to call
    // for the traversal of some declaration.  An XSDocumentInfo is
    // required because the XSDocumentInfo that the traverser is traversing
    // may bear no relation to the one the handler is operating on.
    // This method will:
    // 1.  See if a global definition matching declToTraverse exists;
    // 2. if so, determine if there is a path from currSchema to the
    // schema document where declToTraverse lives (i.e., do a lookup
    // in DependencyMap);
    // 3. depending on declType (which will be relevant to step 1 as
    // well), call the appropriate traverser with the appropriate
    // XSDocumentInfo object.
    // This method returns whatever the traverser it called returned;
    // since this will depend on the type of the traverser in
    // question, this needs to be an Object.
    protected Object callTraverser(XSDocumentInfo currSchema,
            int declType,
            QName declToTraverse) {
    } // end callTraverser

    // Since ID constraints can occur in local elements, unless we
    // wish to completely traverse all our DOM trees looking for ID
    // constraints while we're building our global name registries,
    // which seems terribly inefficient, we need to resolve keyrefs
    // after all parsing is complete.  This we can simply do by running through
    // fIdentityConstraintRegistry and calling traverseKeyRef on all
    // of the KeyRef nodes.  This unfortunately removes this knowledge
    // from the elementTraverser class (which must ignore keyrefs),
    // but there seems to be no efficient way around this...
    protected void resolveKeyRefs() {
    } // end resolveKeyRefs

    // this method resets this object, and should also probably be
    // used to call reset methods on the traversers.
    protected void reset() {
    } // reset

    private Document getSchema(String schemaNamespace,
            String schemaHint) {
        // contents of this method will depend on the system we adopt for entity resolution--i.e., XMLEntityHandler, EntityHandler, etc.
        XMLInputSource schemaSource = fEntityResolver.resolveEntity(schemaNamespace, schemaHint, null);
        if(schemaSource != null) {
            DOMParser schemaParser = new DOMParser(fSymbolTable);
            // set ErrorHandler and EntityResolver (doesn't seem that
            // XMLErrorHandler or XMLEntityResolver will work with
            // standard DOMParser...

            // set appropriate features
            schemaParser.parse(schemaSource);
            return schemaParser.getDocument();
        }
    } // getSchema(String, String):  Document

    // initialize all the traversers.
    private void createTraversers() {
        fAttributeChecker = new
            XSDAttributeChecker(fDatatypeRegistry, fErrorReporter);
        fAttributeGroupTraverser = new
            XSDAttributeGroupTraverser(this, fErrorReporter);
        fAttributeTraverser = new XSDAttributeTraverser(this,
            fErrorReporter);
        fComplexTypeTraverser = new XSDComplexTypeTraverser(this,
            fErrorReporter);
        fElementTraverser = new SDElementTraverser(this,
            fErrorReporter);
        fGroupTraverser = new XSDGroupTraverser(this,
            fErrorReporter);
        fNotationTraverser = new XSDNotationTraverser(this,
            fErrorReporter);
        fSimpleTypeTraverser = new XSDSimpleTypeTraverser(this,
            fErrorReporter);
        fWildCardTraverser = new XSDWildcardTraverser(this,
            fErrorReporter);
    } // createTraversers()

    // this method clears all the global structs of this object
    // (except those passed in via the constructor).
    void reset() {
        fUnparsedAttributeRegistry.clear();
        fUnparsedAttributeGroupRegistry.clear();
        fUnparsedElementRegistry.clear();
        fUnparsedGroupRegistry.clear();
        fUnparsedIdentityConstraintRegistry.clear();
        fUnparsedNotationRegistry.clear();
        fUnparsedTypeRegistry.clear();

        fXSDocumentInfoRegistry.clear();
        fDependencyMap.clear();
        fTraversed.removeAllElements();
        fRoot = null;

    fDatatypeRegistry = null;

        // reset traversers
        fAttributeGroupTraverser.reset();
        fAttributeTraverser.reset();
        fComplexTypeTraverser.reset();
        fElementTraverser.reset();
        fGroupTraverser.reset();
        fNotationTraverser.reset();
        fSimpleTypeTraverser.reset();
        fWildCardTraverser.reset();

    } // reset

} // XSDHandler
