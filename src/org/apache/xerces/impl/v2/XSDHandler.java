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

import org.w3c.dom.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.io.IOException;


/**
 * The purpose of this class is to co-ordinate the construction of a
 * grammar object corresponding to a schema.  To do this, it must be
 * prepared to parse several schema documents (for instance if the
 * schema document originally referred to contains <include> or
 * <redefined> information items).  If any of the schemas imports a
 * schema, other grammars may be constructed as a side-effect.
 *
 * @author Neil Graham, IBM
 * @version $Id$
 */

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

    // this string gets appended to redefined names; it's purpose is to be
    // as unlikely as possible to cause collisions.
    public final static String REDEF_IDENTIFIER = "_fn3dktizrknc9pi";

    //REVISIT: should we have this constant in symbolTable?
    public final static String EMPTY_STRING="";

    //
    //protected data that can be accessable by any traverser
    // stores <notation> decl
    protected Hashtable fNotationRegistry = new Hashtable();

    // stores "final" values of simpleTypes--no clean way to integrate this into the existing datatype validation structure...
    protected Hashtable fSimpleTypeFinalRegistry = new Hashtable();

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

    // map between <redefine> elements and the XSDocumentInfo 
    // objects that correspond to the documents being redefined.  
    private Hashtable fRedefine2XSDMap = new Hashtable();

    // the XMLErrorReporter
    private XMLErrorReporter fErrorReporter;

    // the XSAttributeChecker
    private XSAttributeChecker fAttributeChecker;

    // the XMLEntityResolver
    private XMLEntityResolver fEntityResolver;

    // the symbol table
    private SymbolTable fSymbolTable;

    // the GrammarResolver
    private XSGrammarResolver fGrammarResolver;

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
    XSDHandler (XSGrammarResolver gResolver,
                XMLErrorReporter errorReporter,
                XMLEntityResolver entityResolver,
                SymbolTable symbolTable) {
        fEntityResolver = entityResolver;
        fErrorReporter = errorReporter;
        fGrammarResolver = gResolver;
        fSymbolTable = symbolTable;
        //REVISIT: get schema grammar instead of validator factory
        //SchemaGrammar fGrammar4Schema = fGrammarResolver.getGrammar(SchemaSymbols.URI_SCHEMAFORSCHEMA);
        fDatatypeRegistry = new DatatypeValidatorFactoryImpl();
        fDatatypeRegistry.expandRegistryToFullSchemaSet();
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
        return fGrammarResolver.getGrammar(fRoot.fTargetNamespace);
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
        String schemaNamespace=EMPTY_STRING;        
        String schemaHint=null;
        Document newSchemaRoot = null;
        for (Element child =
             XMLManipulator.getFirstChildElement(rootNode);
            child != null;
            child = XMLManipulator.getNextSiblingElement(child)) {
            String localName = XMLManipulator.getLocalName(child);
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION))
                continue;
            else if (localName.equals(SchemaSymbols.ELT_IMPORT)) {
                // have to handle some validation here too!
                // call XSAttributeChecker to fill in attrs
                newSchemaRoot = getSchema(schemaNamespace, schemaHint);
            }
            else if ((localName.equals(SchemaSymbols.ELT_INCLUDE)) ||
                     (localName.equals(SchemaSymbols.ELT_REDEFINE))) {
                // validation for redefine/include will be the same here; just
                // make sure TNS is right (don't care about redef contents
                // yet).
                newSchemaRoot = getSchema(schemaNamespace, schemaHint);
            }
            else {
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
        return currSchemaInfo;
    } // end constructTrees

    // This method builds registries for all globally-referenceable
    // names.  A registry will be built for each symbol space defined
    // by the spec.  It is also this method's job to rename redefined
    // components, and to record which components redefine others (so
    // that implicit redefinitions of groups and attributeGroups can be handled).
    protected void buildGlobalNameRegistries() {
        /* Starting with fRoot, we examine each child of the schema
         * element.  Skipping all imports and includes, we record the names
         * of all other global components (and children of <redefine>).  We
         * also put <redefine> names in a registry that we look through in
         * case something needs renaming.  Once we're done with a schema we
         * set its Document node to hidden so that we don't try to traverse
         * it again; then we look to its Dependency map entry.  We keep a
         * stack of schemas that we haven't yet finished processing; this
         * is a depth-first traversal.  
         */
        Stack schemasToProcess = new Stack();
        schemasToProcess.push(fRoot);
        while(!schemasToProcess.empty()) {
            XSDocumentInfo currSchemaDoc =
                (XSDocumentInfo)schemasToProcess.pop();
            Document currDoc = currSchemaDoc.fSchemaDoc;
            if(XMLManipulator.isHidden(currDoc)) {
                // must have processed this already!
                continue;
            }
            Element currRoot = XMLManipulator.getRoot(currDoc);

            // process this schema's global decls
            boolean dependenciesCanOccur = true;
            for(Element globalComp =
                    XMLManipulator.getFirstChildElement(currRoot);
                    globalComp != null;
                    globalComp = XMLManipulator.getNextSiblingElement(globalComp)){
                // this loop makes sure the <schema> element ordering is
                // also valid.
                if(XMLManipulator.getLocalName(globalComp).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    //skip it; traverse it later
                    continue;
                } else if(XMLManipulator.getLocalName(globalComp).equals(SchemaSymbols.ELT_INCLUDE) ||
                        XMLManipulator.getLocalName(globalComp).equals(SchemaSymbols.ELT_IMPORT)) {
                    if(!dependenciesCanOccur) {
                        // REVISIT:  schema element ordreing violation
                    }
                    // we've dealt with this; mark as traversed
                    XMLManipulator.setHidden(globalComp);
                } else if(XMLManipulator.getLocalName(globalComp).equals(SchemaSymbols.ELT_REDEFINE)) {
                    if(!dependenciesCanOccur) {
                        // REVISIT:  schema element ordreing violation
                    }
                    for(Element redefineComp = XMLManipulator.getFirstChildElement(globalComp);
                            redefineComp != null;
                            redefineComp = XMLManipulator.getNextSiblingElement(redefineComp)) {
                        String lName = XMLManipulator.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME); 
                        if(lName.length() == 0) // an error we'll catch later
                            continue;
                        String qName = currSchemaDoc.fTargetNamespace +","+lName;
                        String componentType = XMLManipulator.getLocalName(globalComp);
                        if(componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                            checkForDuplicateNames(qName, fUnparsedAttributeGroupRegistry, globalComp, currSchemaDoc);
                            // the check will have changed our name;
                            String targetLName = XMLManipulator.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME); 
                            // and all we need to do is error-check+rename our kkids:
                            // REVISIT!!!
//                            renameRedefiningComponents(SchemaSymbols.ELT_ATTRIBUTEGROUP), 
//                                lName, targetLName);
                        } else if((componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) ||
                                (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE))) {
                            checkForDuplicateNames(qName, fUnparsedTypeRegistry, globalComp, currSchemaDoc);
                            // the check will have changed our name;
                            String targetLName = XMLManipulator.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME); 
                            // and all we need to do is error-check+rename our kkids:
                            // REVISIT!!!
                            if(componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
//                            renameRedefiningComponents(SchemaSymbols.ELT_COMPLEXTYPE), 
//                                lName, targetLName);
                            } else { // must be simpleType
//                            renameRedefiningComponents(SchemaSymbols.ELT_SIMPLETYPE), 
//                                lName, targetLName);
                            }
                        } else if(componentType.equals(SchemaSymbols.ELT_GROUP)) {
                            checkForDuplicateNames(qName, fUnparsedGroupRegistry, globalComp, currSchemaDoc);
                            // the check will have changed our name;
                            String targetLName = XMLManipulator.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME); 
                            // and all we need to do is error-check+rename our kkids:
                            // REVISIT!!!
//                            renameRedefiningComponents(SchemaSymbols.ELT_GROUP), 
//                                lName, targetLName);
                        } else {
                            // REVISIT:  report schema element ordering error
                        }
                    } // end march through <redefine> children
                    // and now set as traversed
                    XMLManipulator.setHidden(globalComp);
                } else {
                    dependenciesCanOccur = false;
                    String lName = XMLManipulator.getAttrValue(globalComp, SchemaSymbols.ATT_NAME); 
                    if(lName.length() == 0) // an error we'll catch later
                        continue;
                    String qName = currSchemaDoc.fTargetNamespace +","+lName;
                    String componentType = XMLManipulator.getLocalName(globalComp);
                    if(componentType.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                        checkForDuplicateNames(qName, fUnparsedAttributeRegistry, globalComp, currSchemaDoc);
                    } else if(componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                        checkForDuplicateNames(qName, fUnparsedAttributeGroupRegistry, globalComp, currSchemaDoc);
                    } else if((componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) ||
                            (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE))) {
                        checkForDuplicateNames(qName, fUnparsedTypeRegistry, globalComp, currSchemaDoc);
                    } else if(componentType.equals(SchemaSymbols.ELT_ELEMENT)) {
                        checkForDuplicateNames(qName, fUnparsedElementRegistry, globalComp, currSchemaDoc);
                    } else if(componentType.equals(SchemaSymbols.ELT_GROUP)) {
                        checkForDuplicateNames(qName, fUnparsedGroupRegistry, globalComp, currSchemaDoc);
                    } else if(componentType.equals(SchemaSymbols.ELT_NOTATION)) {
                        checkForDuplicateNames(qName, fUnparsedNotationRegistry, globalComp, currSchemaDoc);
                    } else {
                        // REVISIT:  report schema element ordering error
                    }
                }
            } // end for

            // now we're done with this one!
            XMLManipulator.setHidden(currDoc);
            // now add the schemas this guy depends on
            Vector currSchemaDepends = (Vector)fDependencyMap.get(currSchemaDoc);
            for(int i = 0; i < currSchemaDepends.size(); i++) {
                schemasToProcess.push(currSchemaDepends.elementAt(i));
            } 
        } // while 
    } // end buildGlobalNameRegistries

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
        return null;
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

    private Document getSchema(String schemaNamespace,
                               String schemaHint) {
        // contents of this method will depend on the system we adopt for entity resolution--i.e., XMLEntityHandler, EntityHandler, etc.
        XMLInputSource schemaSource=null;
        try {
            schemaSource = fEntityResolver.resolveEntity(schemaNamespace, schemaHint, null);
            if (schemaSource != null) {
                DOMParser schemaParser = new DOMParser(fSymbolTable);
                // set ErrorHandler and EntityResolver (doesn't seem that
                // XMLErrorHandler or XMLEntityResolver will work with
                // standard DOMParser...

                // set appropriate features
                schemaParser.parse(schemaSource);
                return schemaParser.getDocument();
            }

        }
        catch (IOException ex) {
            // REVISIT: report an error!
        }

        return null;
    } // getSchema(String, String):  Document

    // initialize all the traversers.
    // this should only need to be called once during the construction
    // of this object; it creates the traversers that will be used to
    // construct schemaGrammars.
    private void createTraversers() {
        fAttributeChecker = new
                            XSAttributeChecker(fDatatypeRegistry, fErrorReporter);
        fAttributeGroupTraverser = new
                                   XSDAttributeGroupTraverser(this, fErrorReporter, fAttributeChecker);
        fAttributeTraverser = new XSDAttributeTraverser(this,
                                                        fErrorReporter, fAttributeChecker);
        fComplexTypeTraverser = new XSDComplexTypeTraverser(this,
                                                            fErrorReporter, fAttributeChecker);
        fElementTraverser = new XSDElementTraverser(this,
                                                    fErrorReporter, fAttributeChecker);
        fGroupTraverser = new XSDGroupTraverser(this,
                                                fErrorReporter, fAttributeChecker);
        fNotationTraverser = new XSDNotationTraverser(this,
                                                      fErrorReporter, fAttributeChecker);
        fSimpleTypeTraverser = new XSDSimpleTypeTraverser(this,
                                                          fErrorReporter, fAttributeChecker);
        fWildCardTraverser = new XSDWildcardTraverser(this,
                                                      fErrorReporter, fAttributeChecker);
    } // createTraversers()

    // this method clears all the global structs of this object
    // (except those passed in via the constructor).
    protected void reset() {
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
        fAttributeChecker.reset();
        fAttributeGroupTraverser.reset();
        fAttributeTraverser.reset();
        fComplexTypeTraverser.reset();
        fElementTraverser.reset();
        fGroupTraverser.reset();
        fNotationTraverser.reset();
        fSimpleTypeTraverser.reset();
        fWildCardTraverser.reset();

    } // reset


    protected DatatypeValidator getDatatypeValidator(String uri, String localpart) {

        DatatypeValidator dv = null;

        if (uri.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) {
            dv = fDatatypeRegistry.getDatatypeValidator( localpart );
        }
        else {
            dv = fDatatypeRegistry.getDatatypeValidator( uri+","+localpart );
        }

        return dv;
    }

    protected  DatatypeValidator getSimpleTypeValidator (String qualifiedName){
        return fDatatypeRegistry.getDatatypeValidator(qualifiedName);
    }

    protected void createSimpleType (String qualifiedName, DatatypeValidator baseValidator,
                                Hashtable facetData, boolean isList){
        
        try {
            fDatatypeRegistry.createDatatypeValidator( qualifiedName, baseValidator,
                                                       facetData, isList);
        }
        catch (Exception e) {
            //REVISIT
            //reportSchemaError(SchemaMessageProvider.DatatypeError,new Object [] { e.getMessage()});
        }
    }
    protected void createUnionSimpleType (String qualifiedName, Vector dTValidators){
        try{            
            fDatatypeRegistry.createDatatypeValidator( qualifiedName, dTValidators);
        }
        catch (Exception e) {
            //REVISIT
            //reportSchemaError(SchemaMessageProvider.DatatypeError,new Object [] { e.getMessage()});
        }
    }
    
    /** This method makes sure that 
     * if this component is being redefined that it lives in the
     * right schema.  It then renames the component correctly.  If it
     * detects a collision--a duplicate definition--then it complains.  
     */
    private void checkForDuplicateNames(String qName,
            Hashtable registry, Element currComp, 
            XSDocumentInfo currSchema) {
        Object objElem = null;
        if((objElem = registry.get(qName)) == null) {
            // just add it in!
            registry.put(qName, currComp);
        } else {
            Element collidingElem = (Element)objElem;
            XSDocumentInfo redefinedSchema = (XSDocumentInfo)(fRedefine2XSDMap.get(XMLManipulator.getParent(collidingElem)));
            if(redefinedSchema == currSchema) { // object comp. okay here
                // now have to do some renaming...
                String newName = qName.substring(qName.lastIndexOf(','));
                currComp.setAttribute(SchemaSymbols.ATT_NAME, newName);
                // and take care of nested redefines by calling recursively:
                checkForDuplicateNames(currSchema.fTargetNamespace+","+newName, registry, currComp, currSchema);
            } else if (redefinedSchema != null) { // we're apparently redefining the wrong schema
                // REVISIT:  error that redefined element in wrong schema
            } else { // we've just got a flat-out collision
                // REVISIT:  report error for duplicate declarations
            } 
        }
    } // checkForDuplicateNames(String, Hashtable, Element, XSDocumentInfo):void
    

    //
    //!!!!!!!!!!!!!!!! IMPLEMENT the following functions !!!!!!!!!!
    //
    //REVISIT: implement namescope support!!!
    protected String resolvePrefixToURI (String prefix) {
        //String uriStr = fStringPool.toString(fNamespacesScope.getNamespaceForPrefix(fStringPool.addSymbol(prefix)));
        //if (uriStr.length() == 0 && prefix.length() > 0) {
            // REVISIT: Localize
            //reportGenericSchemaError("prefix : [" + prefix +"] cannot be resolved to a URI");
            //return "";
        //}

        return null;
    }
    //REVISIT: implement namescope support!!!
    protected Element getTopLevelComponentByName(String componentCategory, String name){
        return null;
    }



} // XSDHandler
