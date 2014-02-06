/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xerces.impl.xs.traversers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public final class OverrideTransformationManager {

    // public static data
    public final static int STATE_INCLUDE = 1;
    public final static int STATE_CONTINUE = 2;
    public final static int STATE_DUPLICATE = 3;
    public final static int STATE_COLLISION = 4;

    // private static data
    private static String[] fGlobalComponentNames = {
        SchemaSymbols.ELT_ATTRIBUTEGROUP,
        SchemaSymbols.ELT_ATTRIBUTE,
        SchemaSymbols.ELT_COMPLEXTYPE,
        SchemaSymbols.ELT_SIMPLETYPE,
        SchemaSymbols.ELT_ELEMENT,
        SchemaSymbols.ELT_NOTATION,
        SchemaSymbols.ELT_GROUP
        };

    private static String[] fCompositeComponentNames = {
        SchemaSymbols.ELT_INCLUDE,
        SchemaSymbols.ELT_OVERRIDE,
        SchemaSymbols.ELT_REDEFINE 
        };

    // private data 
    private OverrideTransformer fOverrideTransformationHandler;
    private final HashMap fSystemId2ContextMap = new HashMap();
    private int fCurrentState = STATE_INCLUDE;
    private final XSDHandler fSchemaHandler;

    // Constructor
    public OverrideTransformationManager(XSDHandler handler, OverrideTransformer overrideHandler) {
        fSchemaHandler = handler;
        fOverrideTransformationHandler = overrideHandler;
    }

    /**
     * Reset state
     */
    public void reset() {
        fCurrentState = STATE_INCLUDE;
        if (fSystemId2ContextMap.size() != 0) {
            fSystemId2ContextMap.clear();
        }
    }

    /**
     * Perform override transformations on target schema. If transformation
     * takes place and we do not have a collision, we return the transformed
     * schema. If no transformation and no collision, we return the original
     * target schema, otherwise we return null to prevent the target schema
     * from being added to the dependency list.
     */
    public Element transform(String schemaId, Element overrideElement, Element targetSchema) {
        Element transformedSchema;
        boolean hasPerformedTransformations = false;

        // do transformations if an <override> element is parsed
        // else just an include/redefined scenario we want to check dependency only
        try {
            transformedSchema = fOverrideTransformationHandler.transform(overrideElement,targetSchema);
        }
        catch (OverrideTransformException e) {
            // NOTE: Exception is only throw when doing JAXP transformation
            //       For now, we just return the original schema
            return targetSchema;
        }

        if (transformedSchema != null) {
            hasPerformedTransformations = true;                    
        }                
        else { // if no override transformations has taken place let transformed be the original
            transformedSchema = targetSchema;
        }

        // include current schema (transformed/not) into context
        final String schemaIdString = nullToEmptyString(schemaId);
        if (checkSchemaDependencies(schemaIdString, overrideElement, transformedSchema, hasPerformedTransformations)) {
            return transformedSchema;
        }

        return null;
    }

    /**
     * Add a schema to the list of processed schemas. 
     */
    public void addSchemaRoot(String schemaId, Element schemaRoot) {
        final String schemaIdString = nullToEmptyString(schemaId);
        setDocumentMapForSystemId(schemaIdString ,
                createDocumentMap(schemaRoot,DocumentContext.IS_ORIGINAL));
    }

    /**
     * Check original schema root for possible collision
     * 
     * @param schemaId The system id of the schema
     * @param schemaRoot The root of the schema
     */
    public void checkSchemaRoot(String schemaId, Element decl, Element schemaRoot) {
        final String schemaIdString = nullToEmptyString(schemaId);

        if (includeSchemaDependencies(schemaIdString, schemaRoot, DocumentContext.IS_ORIGINAL)) {
            fCurrentState = STATE_INCLUDE;
            return;
        }

        DocumentContext dCtxt = getDocumentMapForSystemId(schemaIdString);            
        Iterator mappedRoots = dCtxt.getSchemaArray();

        // for each schema root stored in the context compare for duplicates 
        while (mappedRoots.hasNext()){
            final Element mRoot = (Element) mappedRoots.next();
            if (DocumentContext.IS_ORIGINAL == dCtxt.getSchemaState(mRoot)) {
                fCurrentState = STATE_DUPLICATE;
                return;
            }
        }

        // We have a collision - schema id was overridden
        fCurrentState = STATE_COLLISION;
        fSchemaHandler.reportSchemaError("src-override-collision.1", new Object [] {
                schemaId, DOMUtil.getLocalName(decl)}, decl);
    }

    /**
     * Return the state after immediate transformation 
     */
    public int getCurrentState(){
        return fCurrentState;
    }

    /**
     * Set <code>override</code> transformer
     * 
     * @param overrideHandler <code>override</code> transformer handler to set
     */
    public void setOverrideHandler(OverrideTransformer overrideHandler){
        fOverrideTransformationHandler = overrideHandler ;
    }

    /**
     * Tries to create a Document Map for a schemaElement
     * records different Schema Component Types within the created Document Map 
     */
    private DocumentContext createDocumentMap(Element schemaRootElement, Boolean state){
        DocumentContext dCtxt = new DocumentContext();
        dCtxt.addSchemasToArray(schemaRootElement,state);
        
        return dCtxt;
    }

    public boolean hasGlobalDecl(Element schemaRoot){
        return hasComponentsTypes(schemaRoot, fGlobalComponentNames);
    }

    public boolean hasCompositionalDecl(Element schemaRoot){
        return hasComponentsTypes(schemaRoot, fCompositeComponentNames);
    }

    /**
     * Check the dependency state of the given schema document (ie:-include it in the dependency list/not ,etc )
     * during <override>'s and <include>'s
     * There are four dependency states
     * --> INCLUDE - first time inclusion to dependency tree
     * --> CONTINUE - Ok to include into dependency tree
     * --> COLLISION - there is a name collision between the current schema and the corresponding schemas in dependency tree
     * --> DUPLICATE - Sometimes during overrides inclusion may be valid but would be repeated due to a cycle. This state indicates
     *                 Transformer/caller to stop further inclusion of the schema
     *
     * Dependencies are first checked for the global elements of the current schema with respect to the corresponding schemas of the 
     * dependency tree. If no collisions are detected it checks for any cycles else indicate continuation with inclusion of current schema
     *
     */
    private boolean checkSchemaDependencies(String schemaId, Element decl, Element schemaRoot, boolean hasTransformationsOnSchema){
        Boolean newSchemaState = (hasTransformationsOnSchema == true) 
            ? DocumentContext.IS_TRANSFORMED : DocumentContext.IS_ORIGINAL;

        // First time to see the schema, we store the information and return
        if (includeSchemaDependencies(schemaId, schemaRoot,newSchemaState)) {
            fCurrentState = STATE_INCLUDE;
            return true;
        }

        // We have seen the schema before, so either it's the same or
        // we have a collision
        boolean collisionState = false;
        DocumentContext dCtxt = getDocumentMapForSystemId(schemaId);            
        Iterator mappedRoots = dCtxt.getSchemaArray();

        // for each schema root stored in the context compare for duplicates 
        while (mappedRoots.hasNext()) {
            Element mRoot = (Element) mappedRoots.next();
            if (checkDuplicateElements(dCtxt,mRoot, schemaRoot, newSchemaState)){
                // Either the schema is original and being referenced multiple
                // times or we hit an override cycle
                fCurrentState = STATE_DUPLICATE;
                return false;
            }
            else {
                collisionState = true;
            }
        } //end of while

        // if no duplicates were found ,here state can only be COLLISON or CONTINUE 
        if (collisionState) {
            //check for any actual collison                                                     
            if (!hasGlobalDecl(schemaRoot)) {     
                dCtxt.addSchemasToArray(schemaRoot);
            }
            else {
                // We have a collision - schema id was previously included/redefined/overridden
                fCurrentState = STATE_COLLISION;
                fSchemaHandler.reportSchemaError("src-override-collision.2", new Object [] {
                        schemaId }, decl);   
                return false;
            }
        }//end of If
        
        fCurrentState = STATE_CONTINUE;
        return true;
    }

    /**
     * Include this schema into the dependency map for the respective schema ID
     */
    private boolean includeSchemaDependencies(String schemaId,Element schemaRoot, Boolean state){
        if (!isSchemaAlreadyTraversed(schemaId)){
            setDocumentMapForSystemId(schemaId,createDocumentMap(schemaRoot,state));
            return true;
        }

        return false;
    }
    
    /**
     * Check whether schema corresponding to this schema ID has already been included in the map
     */
    private boolean isSchemaAlreadyTraversed(String schemaId){        
        return (fSystemId2ContextMap.get(schemaId) != null);
    }

    /**
     * Checks for equality on two global components (with same name Attribute)
     *       true --> if two components are equal in content 
     *       false --> otherwise
     */
    private boolean checkDuplicateElements(DocumentContext dCtxt,Element contextComponent, Element newComponent, Boolean stateNewSchema){
        Boolean stateCtxtElement = dCtxt.getSchemaState(contextComponent);
        //if both schemas are transformed check #isEquals
        if (stateCtxtElement == DocumentContext.IS_TRANSFORMED && stateNewSchema == DocumentContext.IS_TRANSFORMED ){    
            return compareComponents(contextComponent,newComponent);
        }
        
        // if both schemas are original ==> compared schemas are duplicates
        // if one of the schemas is Transformed ==> then they are not duplicates hence possibility of Collision
        return (stateCtxtElement == DocumentContext.IS_ORIGINAL && stateNewSchema == DocumentContext.IS_ORIGINAL);
    }

    private boolean compareComponents(Element component1,Element component2){
        //normalize two elements before comparing
        component1.normalize();
        component2.normalize();
        //perform a deep equality comparison on two nodes
        return component1.isEqualNode(component2);
    }
    
    private DocumentContext getDocumentMapForSystemId(String systemId){        
        return (DocumentContext) fSystemId2ContextMap.get(systemId);
    }

    private void setDocumentMapForSystemId(String systemId, DocumentContext map){
        fSystemId2ContextMap.put(systemId, map);
        
    }

    private String nullToEmptyString(String input) {
        return (input == null) ? "" : input;
    }

    private boolean hasComponentsTypes(Element schemaRoot,String[] types){                
        for (Element child = DOMUtil.getFirstChildElement(schemaRoot);
                child != null;
                child = DOMUtil.getNextSiblingElement(child)){  
            String localName = getLocalName(child);
            for(int i=0;i<types.length;i++){
                if(types[i].equals(localName)){
                    return true;
                }
            }
        }
        return false;
    }   

    private String getLocalName(Node node){
        String localName = "";
        localName = DOMUtil.getLocalName(node);
        if(localName.indexOf(":") > -1){
            return localName.split(":")[1];
        }
        
        return localName;
    }

    /**
     * A context to store schema related information related to any schema corresponding to a particular 
     * schemaId location 
     * It records ,
     * --> global elements of a particular schema and
     * --> override elements list corresponding to each schema with respect to a given schemaId location
     */
    private static final class DocumentContext {     
        
        private final ArrayList fRootElementList = new ArrayList();
        private final HashMap fSchema2StateMap = new HashMap();
        
        //indicate Transformation handler that this schema root is original
        private final static Boolean IS_ORIGINAL = Boolean.TRUE;
        //indicate Transformation handler that this schema root is transformed
        private final static Boolean IS_TRANSFORMED = Boolean.FALSE;
        
        DocumentContext() {}

        void addSchemasToArray(Element schemaRoot){
            fRootElementList.add(schemaRoot);
        }
        
        void addSchemasToArray(Element schemaRoot, Boolean state){
            addSchemasToArray(schemaRoot);
            fSchema2StateMap.put(schemaRoot, state);
        }

        Boolean getSchemaState(Element schemaRoot){
            return ((Boolean) fSchema2StateMap.get(schemaRoot));
        }
        
        Iterator getSchemaArray(){
            return fRootElementList.iterator();
        }
        
        void clear() {
            fRootElementList.clear();
            fSchema2StateMap.clear();
        }
    }
}
