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

package schema.override;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.apache.xerces.impl.xs.traversers.override.DOMOverrideImpl;
import org.apache.xerces.impl.xs.traversers.override.OverrideTransformationManager;
import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class OverrideHandlerTest extends BaseOverrideTest {
    
    Hashtable fId2DocMap ;
    OverrideTransformationManager fOverrideHandler;
    LinkedList fDependencyList ; 
    String fDirectedGraph;
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(OverrideHandlerTest.class);
    }
    
    public OverrideHandlerTest(){
        fId2DocMap = createId2DocMapScenario_1();        
        
    }

    protected void setUp(){
        fDependencyList = new LinkedList();
        fDirectedGraph = "";
        XSHandlerDelegate schemaHandler = new XSHandlerDelegate(Constants.SCHEMA_VERSION_1_1, null);
        fOverrideHandler = new OverrideTransformationManager(schemaHandler, new DOMOverrideImpl(schemaHandler));
    }
    
    
    public void testHasGlobalDecerations(){
        String[] testSchemas = {"schemaA.xsd","schemaA_1.xsd"};
        OverrideTransformationManager overrideHandler = new OverrideTransformationManager(null, null);
        
        Document dom1 = parseXmlDom(getResourceURL(testSchemas[0]));
        assertFalse(overrideHandler.hasGlobalDecl(dom1.getDocumentElement()));
        
        dom1 = parseXmlDom(getResourceURL(testSchemas[1]));
        assertTrue(overrideHandler.hasGlobalDecl(dom1.getDocumentElement()));
        
    }
    
    public void testHasCompositionalDecerations(){
        String[] testSchemas = {"schemaA.xsd","schemaA_1.xsd","schemaA_2.xsd"};
        OverrideTransformationManager overrideHandler = new OverrideTransformationManager(null, null);
        
        Document dom1 = parseXmlDom(getResourceURL(testSchemas[0]));
        assertTrue(overrideHandler.hasCompositionalDecl(dom1.getDocumentElement()));
        
        dom1 = parseXmlDom(getResourceURL(testSchemas[1]));
        assertTrue(overrideHandler.hasCompositionalDecl(dom1.getDocumentElement()));
        
        dom1 = parseXmlDom(getResourceURL(testSchemas[2]));
        assertFalse(overrideHandler.hasCompositionalDecl(dom1.getDocumentElement()));
        
    }
    
    public void testDependencyNoSchemaRepeats(){
        TestNode root = createDependancyMap1();
        String id = "A.xsd";
        Element domSchemaRoot = (Element) fId2DocMap.get(id); 
        fDirectedGraph = id;
        fOverrideHandler.checkSchemaRoot(id, null,domSchemaRoot);        
        performDependencyChecking(root,domSchemaRoot);
        System.out.println(fDirectedGraph);
       
    }
    
    public void testDependencySchemaRepeats(){
        fId2DocMap = createId2DocMapScenario_2(); 
        TestNode root = createDependancyMap2();
        String id = "A.xsd";
        Element domSchemaRoot = (Element) fId2DocMap.get(id); 
        fDirectedGraph = id;
        fOverrideHandler.checkSchemaRoot(id, null,domSchemaRoot);        
        performDependencyChecking(root,domSchemaRoot);
        System.out.println(fDirectedGraph);
       
    }
    
    public void testDependencySchemaRepeats_Collision(){
        fId2DocMap = createId2DocMapScenario_3(); 
        TestNode root = createDependancyMap3();
        String id = "A.xsd";
        Element domSchemaRoot = (Element) fId2DocMap.get(id); 
        fDirectedGraph = id;
        fOverrideHandler.checkSchemaRoot(id, null,domSchemaRoot);        
        performDependencyChecking(root,domSchemaRoot);
        System.out.println(fDirectedGraph);
       
    }
    
    public void testDependencySchemaRepeats_Cycles(){
        fId2DocMap = createId2DocMapScenario_4(); 
        TestNode root = createDependancyMap4();
        String id = "A.xsd";
        Element domSchemaRoot = (Element) fId2DocMap.get(id); 
        fDirectedGraph = id;
        fOverrideHandler.checkSchemaRoot(id, null,domSchemaRoot);        
        performDependencyChecking(root,domSchemaRoot);
        System.out.println(fDirectedGraph);
       
    }
    
    public void testDependencySchemaRepeats_Collision_2(){
        fId2DocMap = createId2DocMapScenario_5(); 
        TestNode root = createDependancyMap5();
        String id = "A.xsd";
        Element domSchemaRoot = (Element) fId2DocMap.get(id); 
        fDirectedGraph = id;
        fOverrideHandler.checkSchemaRoot(id, null,domSchemaRoot);        
        performDependencyChecking(root,domSchemaRoot);
        System.out.println(fDirectedGraph);
       
    }
    //"A inc B inc C";
    public TestNode createDependancyMap1(){
        TestNode parent = new TestNode();
        parent.expectedState = OverrideTransformationManager.STATE_INCLUDE;
        
        TestNode child = new TestNode();        
        child.expectedState = OverrideTransformationManager.STATE_INCLUDE;        
        parent.addChildNode(child);
        
        TestNode desc = new TestNode();        
        desc.expectedState = OverrideTransformationManager.STATE_INCLUDE;       
        child.addChildNode(desc);
        
        fillDependencyList(parent);
        return parent;
    }
    
    public TestNode createDependancyMap2(){
        TestNode parent = new TestNode();
        parent.expectedState = OverrideTransformationManager.STATE_INCLUDE;
        
        TestNode child = new TestNode();        
        child.expectedState = OverrideTransformationManager.STATE_INCLUDE;        
        parent.addChildNode(child);
        
        TestNode desc = new TestNode();        
        desc.expectedState = OverrideTransformationManager.STATE_INCLUDE;       
        child.addChildNode(desc);
        
        TestNode desc2 = new TestNode();        
        desc2.expectedState = OverrideTransformationManager.STATE_DUPLICATE;       
        desc.addChildNode(desc2);
        
        fillDependencyList(parent);
        return parent;
    }
    
    public TestNode createDependancyMap3(){
        TestNode parent = new TestNode();
        parent.expectedState = OverrideTransformationManager.STATE_INCLUDE;
        
        TestNode child = new TestNode();        
        child.expectedState = OverrideTransformationManager.STATE_INCLUDE;        
        parent.addChildNode(child);
        
        TestNode desc = new TestNode();        
        desc.expectedState = OverrideTransformationManager.STATE_INCLUDE;       
        child.addChildNode(desc);
        
        TestNode desc2 = new TestNode();        
        desc2.expectedState = OverrideTransformationManager.STATE_COLLISION;       
        desc.addChildNode(desc2);
        
        fillDependencyList(parent);
        return parent;
    }
    
    public TestNode createDependancyMap4(){
        //A
        TestNode parent = new TestNode();
        parent.expectedState = OverrideTransformationManager.STATE_INCLUDE;
        //-->B
        TestNode child = new TestNode();        
        child.expectedState = OverrideTransformationManager.STATE_INCLUDE;        
        parent.addChildNode(child);
        //-->C
        TestNode desc = new TestNode();        
        desc.expectedState = OverrideTransformationManager.STATE_INCLUDE;       
        child.addChildNode(desc);
        //-->A'
        TestNode desc2 = new TestNode();        
        desc2.expectedState = OverrideTransformationManager.STATE_CONTINUE;       
        desc.addChildNode(desc2);
        //-->B'
        TestNode desc3 = new TestNode();        
        desc3.expectedState = OverrideTransformationManager.STATE_CONTINUE;       
        desc2.addChildNode(desc3);
        //-->C'
        TestNode desc4 = new TestNode();        
        desc4.expectedState = OverrideTransformationManager.STATE_DUPLICATE;       
        desc3.addChildNode(desc4);
        
        fillDependencyList(parent);
        return parent;
    }
    
    public TestNode createDependancyMap5(){
        //A
        TestNode parent = new TestNode();
        parent.expectedState = OverrideTransformationManager.STATE_INCLUDE;
        //-->B
        TestNode child = new TestNode();        
        child.expectedState = OverrideTransformationManager.STATE_INCLUDE;        
        parent.addChildNode(child);
        //-->C
        TestNode desc = new TestNode();        
        desc.expectedState = OverrideTransformationManager.STATE_INCLUDE;       
        child.addChildNode(desc);
        //-->A'
        TestNode desc2 = new TestNode();        
        desc2.expectedState = OverrideTransformationManager.STATE_CONTINUE;       
        desc.addChildNode(desc2);
        //-->B'
        TestNode desc3 = new TestNode();        
        desc3.expectedState = OverrideTransformationManager.STATE_COLLISION;       
        desc2.addChildNode(desc3);
        
        
        fillDependencyList(parent);
        return parent;
    }
    
    private void fillDependencyList(TestNode parent){
        if(parent!=null){
            fDependencyList.add(parent);
            Iterator children = parent.getChildren();
            
            while(children.hasNext()){
                fillDependencyList((TestNode)children.next());
            }
        }        
    }
   
    public void print(int state){
        if(state == OverrideTransformationManager.STATE_DUPLICATE){
            System.out.println("CYCLE");
        }
        else if(state == OverrideTransformationManager.STATE_CONTINUE){
            System.out.println("CONTINUE");
        }
        else if(state == OverrideTransformationManager.STATE_COLLISION){
            System.out.println("COLLISION");
        }
        else{
            System.out.println("INCLUDE");
        }
    }
    
    public void performDependencyChecking(TestNode tNode,Element root){        
        assertEquals(tNode.expectedState, fOverrideHandler.getCurrentState()); 
        print(tNode.expectedState);
        fDependencyList.removeFirst();
        if(root == null){
            return;
        }
        
        for (Element child = DOMUtil.getFirstChildElement(root);
        child != null;
        child = DOMUtil.getNextSiblingElement(child)){             
            
            String localName = getLocalName(child);
            
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION)){
                continue;
            }
            else if(localName.equals(SchemaSymbols.ELT_INCLUDE)){   
                String id = DOMUtil.getAttrValue(child, SchemaSymbols.ATT_SCHEMALOCATION);
                System.out.println("<include> id : " + id);
                Element domNode = (Element) fId2DocMap.get(id); 
                Element target = (Element) fOverrideHandler.transform(id, null, domNode);                  
                fDirectedGraph+= "--inc-->" + id;
                performDependencyChecking((TestNode)fDependencyList.getFirst(), target);
                if(fOverrideHandler.getCurrentState() == OverrideTransformationManager.STATE_DUPLICATE ||
                            fOverrideHandler.getCurrentState() == OverrideTransformationManager.STATE_COLLISION)
                   fDirectedGraph+= "\n";                                                  
            }
            else if(localName.equals(SchemaSymbols.ELT_REDEFINE)){
                
            }
            else if(localName.equals(SchemaSymbols.ELT_OVERRIDE)){
                String id = DOMUtil.getAttrValue(child, SchemaSymbols.ATT_SCHEMALOCATION);
                System.out.println("<override> id : " + id);
                Element domNode = (Element) fId2DocMap.get(id); 
                Element overridden = (Element)fOverrideHandler.transform(id, child, domNode);                                        
                fDirectedGraph+= "--ov-->" + id;
                performDependencyChecking((TestNode)fDependencyList.getFirst(), overridden);
                if(fOverrideHandler.getCurrentState() == OverrideTransformationManager.STATE_DUPLICATE || 
                            fOverrideHandler.getCurrentState() == OverrideTransformationManager.STATE_COLLISION)
                   fDirectedGraph+= "\n";                                       
            }
        }           
    }
    
    public Hashtable createId2DocMapScenario_1(){
        Hashtable map = new Hashtable();
        map.put("A.xsd",parseXmlDom(getResourceURL("scenario_1/A.xsd")).getDocumentElement());
        map.put("B.xsd",parseXmlDom(getResourceURL("scenario_1/B.xsd")).getDocumentElement());
        map.put("C.xsd",parseXmlDom(getResourceURL("scenario_1/C.xsd")).getDocumentElement());
        
        return map;
    }
    
    public Hashtable createId2DocMapScenario_2(){
        Hashtable map = new Hashtable();
        map.put("A.xsd",parseXmlDom(getResourceURL("scenario_2/A.xsd")).getDocumentElement());
        map.put("B.xsd",parseXmlDom(getResourceURL("scenario_2/B.xsd")).getDocumentElement());
        map.put("C.xsd",parseXmlDom(getResourceURL("scenario_2/C.xsd")).getDocumentElement());
        
        return map;
    }
    
    public Hashtable createId2DocMapScenario_3(){
        Hashtable map = new Hashtable();
        map.put("A.xsd",parseXmlDom(getResourceURL("scenario_3/A.xsd")).getDocumentElement());
        map.put("B.xsd",parseXmlDom(getResourceURL("scenario_3/B.xsd")).getDocumentElement());
        map.put("C.xsd",parseXmlDom(getResourceURL("scenario_3/C.xsd")).getDocumentElement());
        
        return map;
    }
    
    public Hashtable createId2DocMapScenario_4(){
        Hashtable map = new Hashtable();
        map.put("A.xsd",parseXmlDom(getResourceURL("scenario_4/A.xsd")).getDocumentElement());
        map.put("B.xsd",parseXmlDom(getResourceURL("scenario_4/B.xsd")).getDocumentElement());
        map.put("C.xsd",parseXmlDom(getResourceURL("scenario_4/C.xsd")).getDocumentElement());
        
        return map;
    }
    
    public Hashtable createId2DocMapScenario_5(){
        Hashtable map = new Hashtable();
        map.put("A.xsd",parseXmlDom(getResourceURL("scenario_5/A.xsd")).getDocumentElement());
        map.put("B.xsd",parseXmlDom(getResourceURL("scenario_5/B.xsd")).getDocumentElement());
        map.put("C.xsd",parseXmlDom(getResourceURL("scenario_5/C.xsd")).getDocumentElement());
        
        return map;
    }
    
    static class TestNode{        
        ArrayList children = new ArrayList();                            
        int expectedState;
        
        public TestNode(){                 
        }       
                
        public void addChildNode(TestNode child){            
            children.add(child);
        }              
        
        public Iterator getChildren(){
            return this.children.iterator();
        }        
        
    }
}
