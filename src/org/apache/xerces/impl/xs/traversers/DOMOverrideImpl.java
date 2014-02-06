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
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public final class DOMOverrideImpl extends OverrideTransformer {

    // records all the override schema components and it's properties
    private final ArrayList fOverrideComponents = new ArrayList();
    private final HashMap[] fOverrideComponentsMap =  new HashMap[] {
            null, new HashMap(), new HashMap(), new HashMap(),
            new HashMap(), new HashMap(), new HashMap(), new HashMap()
    };

    // overridden schema document 
    private Document fOverridenDoc;

    // <override> schema Element
    private Element fOverrideElem;

    // indicates transformer that it has performed at least single transformation
    private boolean hasPerformedTransformations = false;

    // DOM implementation for Document creation
    private DOMImplementation fDOMImpl;
    
    // XSDHandler - error reporting
    private final XSDHandler fSchemaHandler;

    public DOMOverrideImpl(XSDHandler schemaHandler){
        fSchemaHandler = schemaHandler;
        try {
            //get a DOM registry base
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            // get a DOM implementation the Level 3 XML module
            fDOMImpl = registry.getDOMImplementation("XML 3.0");
        } catch (ClassCastException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }      

    public void clearState(){
        fOverrideComponents.clear();
        for (int i=1; i <fOverrideComponentsMap.length; i++) {
            fOverrideComponentsMap[i].clear();
        }
        fOverridenDoc = null;
        fOverrideElem = null;            
        hasPerformedTransformations = false;
    }

    /**
     * Given an override Schema Element ie:- <override> and a schema B ,this method will 
     * try to produce override transformations on schema B and generate a overridden schema version B' 
     * Or It will return a null if override transformations cannot be applied on B
     * ie:- B does not have overriden schema elements as specified in override Schema Element
     * 
     * @param overrideElement -an override Schema Element ie:- <override> 
     * @param overridenSchema -an overridden schema B   
     */
    public Element transform(Element overrideElement, Element overridenSchema)
            throws OverrideTransformException {
        fOverridenDoc = cloneOverridenSchema(overridenSchema);
        final Element overridenSchemaRoot = fOverridenDoc.getDocumentElement();

        fOverrideElem = overrideElement;
        fillOverrideElementMap(fOverrideElem);
        transform(overridenSchemaRoot,false);

        if (hasOverrideTransformations()){
            clearState();
            return overridenSchemaRoot;
        }

        clearState();
        return null;
    }

    public boolean hasOverrideTransformations(){
        return hasPerformedTransformations;
    }

    /**
     * Main <override> transform method , It will go through all global schema components of the overridden document
     * and perform override transformations as necessary. Transformations are recursively applied on <redefine> and 
     * <override> elements of  the overridden schema. This also handles deferred transformations on <include> and 
     * <override> elements.
     * 
     * @param overrideSchemaRoot -root of overridden Schema Element  
     * @param isOverrideRoot -indicates whether transformations are currently applied on a <override> element located 
     *                        in overridden schema document. useful for <override> merge transformations.  
     */
    private void transform(Element overridenSchemaRoot, boolean isOverrideRoot) {
        for (Element child = DOMUtil.getFirstChildElement(overridenSchemaRoot);
             child != null;
             child = DOMUtil.getNextSiblingElement(child)){

            final String localName = getLocalName(child);
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION)){
                continue;
            }
            else if (localName.equals(SchemaSymbols.ELT_INCLUDE)){
                //create new <Override> Element for this Document 
                final Element temp = fOverrideElem;
                //set schemaLocation to the same location as <include>
                String newSchemaLocation = child.getAttribute(SchemaSymbols.ATT_SCHEMALOCATION);
                child = performDOMOverride(overridenSchemaRoot, temp, child);
                child.setAttribute(SchemaSymbols.ATT_SCHEMALOCATION, newSchemaLocation);
                hasPerformedTransformations = true;
            }
            else if(localName.equals(SchemaSymbols.ELT_REDEFINE)){
                transform(child,false);
            }
            else if(localName.equals(SchemaSymbols.ELT_OVERRIDE)){
                transform(child,true);
                //do <override> merging
                mergeOverride(child);
            }
            else {
                final String componentName = DOMUtil.getAttrValue(child, SchemaSymbols.ATT_NAME);
                if (componentName.length() == 0) {
                    continue;
                }
                int componentType = getOverrideType(localName);
                if (componentType == 0) {
                    continue;
                }
                OverrideElement newNode = getMatchingOverrideElement(componentType, componentName);
                Element oldNode = child;
                //check if element needs to be overridden     
                if (newNode != null){
                    child = performDOMOverride(overridenSchemaRoot, newNode.originalElement, oldNode);
                    if (!child.isEqualNode(oldNode)) {
                        hasPerformedTransformations = true;
                    }

                    if (isOverrideRoot){
                        newNode.overrideCloned = true;   
                    }
                }
            }
        }
    }
    
    /**
     * Register elements of the <override> schema component for later processing 
     */
    private void fillOverrideElementMap(Element overrideElement){
        for (Element child = DOMUtil.getFirstChildElement(overrideElement);
             child != null;
             child = DOMUtil.getNextSiblingElement(child)){

            final String localName = getLocalName(child);
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION)){
                continue;
            }
            else {
                final int componentOverrideType = getOverrideType(localName);
                if (componentOverrideType != 0) {
                    addOverrideElement(componentOverrideType, child);
                }
                else {
                    fSchemaHandler.reportSchemaError("s4s-elt-must-match.1", new Object[]{"override", "(annotation | (simpleType | complexType | group | attributeGroup | element | attribute | notation))*", DOMUtil.getLocalName(child)}, child);
                }
            }            
        }
    }

    /**
     * Create a new OverrideElemnt and record it into <override> components
     */
    private void addOverrideElement(int componentType, Element elem) {
        final String cName = DOMUtil.getAttrValue(elem, SchemaSymbols.ATT_NAME);
        final HashMap cMap = fOverrideComponentsMap[componentType];
        if (cMap.get(cName) != null) {
            fSchemaHandler.reportSchemaError("sch-props-correct.2", new Object []{cName}, elem);
        }
        else {
            OverrideElement e = new OverrideElement(componentType, elem,cName);
            fOverrideComponents.add(e);
            cMap.put(cName, e);
        }
    }

    /**
     * Check for override transformations with respect to the current schema Element
     * in the overriden schema Document
     */
    private OverrideElement getMatchingOverrideElement(int componentType, String cName){
        Iterator iter = fOverrideComponents.iterator();
        while (iter.hasNext()) {
            OverrideElement oElem = (OverrideElement) iter.next();
            if (oElem.componentType == componentType && oElem.cName.equals(cName)) {
                return oElem;
            }
        }

        return null;
    }
    
    private int getOverrideType(String localName){
        if (localName.equals(SchemaSymbols.ELT_SIMPLETYPE)){
            return OVERRIDE_SIMPLE_TYPE;
        }
        else if (localName.equals(SchemaSymbols.ELT_COMPLEXTYPE )){
            return OVERRIDE_COMPLEX_TYPE;
        }
        else if (localName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP) ){
            return OVERRIDE_ATTRIBUTE_GROUP;   
        }
        else if (localName.equals(SchemaSymbols.ELT_GROUP)){
            return OVERRIDE_GROUP;           
        }
        else if (localName.equals(SchemaSymbols.ELT_ELEMENT)){
            return OVERRIDE_ELEMENT;   
        }
        else if (localName.equals(SchemaSymbols.ELT_NOTATION)){
            return OVERRIDE_NOTATION;   
        }
        else if (localName.equals(SchemaSymbols.ELT_ATTRIBUTE)){
            return OVERRIDE_ATTRIBUTE;   
        }
        
        return 0;
    }
    
    /** 
     * produce a schema B' identical to the overridden schema document B for override transformations
     */
    private Document cloneOverridenSchema(Element overridenRoot){
        final Document newDoc = fDOMImpl.createDocument(null, null, null);
        final Node newRoot = newDoc.importNode(overridenRoot, true);

        // Document URI need to be set for proper schema resolving
        newDoc.setDocumentURI(overridenRoot.getOwnerDocument().getDocumentURI());
        newDoc.appendChild(newRoot);

        return newDoc;
    }
    
    /**
     * Create a node identical to the given node
     */
    private Node getChildClone(Node child){
        return fOverridenDoc.importNode(child,true);
    }
    
    /**
     * Perform merge override transformations on <override> element 
     */
    private void mergeOverride(Element overrideElement){
        Iterator iter = fOverrideComponents.iterator();
        while (iter.hasNext()){
            OverrideElement oElem = (OverrideElement) iter.next();
            if (!oElem.overrideCloned){
                overrideElement.appendChild(getChildClone(oElem.originalElement));
                hasPerformedTransformations = true;
            }
            else {
                oElem.overrideCloned = false;
            }
        }
    }

    /**
     * Perform general override transformations on <override> element
     * replace current element with the new override element
     * 
     */
    private Element performDOMOverride(Element overridenSchemaRoot,Element overrideNode, Element oldNode) {
        Element clonedNode = (Element)getChildClone(overrideNode);
        overridenSchemaRoot.replaceChild(clonedNode,oldNode);
        return clonedNode;
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
     * Override Element class
     * 
     * Used to store override elements and properties for later processing
     */
    private static final class OverrideElement {
        final int componentType;
        final Element originalElement;
        final String cName;
        boolean overrideCloned = false; 
        
        OverrideElement(int componentType, Element elem, String cName) {
            this.componentType = componentType;
            originalElement = elem;
            this.cName = cName; 
        }
    }
}
