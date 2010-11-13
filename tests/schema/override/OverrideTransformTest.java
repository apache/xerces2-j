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

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.traversers.override.DOMOverrideImpl;
import org.apache.xerces.impl.xs.traversers.override.OverrideTransformException;
import org.apache.xerces.impl.xs.traversers.override.OverrideTransformer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @version $Id$
 */
public class OverrideTransformTest extends BaseOverrideTest {

    String[] fOverridingDocs = {"overriding.xsd","overriding1.xsd","overriding2.xsd"};
    String[] fOverriddenDocs = {"overridden.xsd","overridden1.xsd","overridden2.xsd"};
    String[] fExpectedDocs = {"result.xsd","result1.xsd","result2.xsd"};
    OverrideTransformer fTransformer = new DOMOverrideImpl(new XSHandlerDelegate(Constants.SCHEMA_VERSION_1_1, null));
    
    protected void setUp(){
        
    }
    
    public void testNormalOverrideTransform(){       
        doOverrideTest(fOverridingDocs[0],fOverriddenDocs[0], fExpectedDocs[0]);          
    }
    
    public void testOverrideWithMerge(){
        doOverrideTest(fOverridingDocs[1],fOverriddenDocs[1], fExpectedDocs[1]); 
    }
    
    public void testOverrideWithInclude(){
        doOverrideTest(fOverridingDocs[2],fOverriddenDocs[2], fExpectedDocs[2]); 
    }
    
    public void doOverrideTest(String overridingElem,String overriddenElem,String exResultElem){
        Document dom1 = parseXmlDom(getResourceURL(overridingElem));
        NodeList list = dom1.getDocumentElement().getChildNodes();
        Node overrideElem = list.item(1);        

        Document dom2 = parseXmlDom(getResourceURL(overriddenElem));
        Node overridenElem = dom2.getDocumentElement();
        Node overridenElem2 = overridenElem;            

        
        Node result=null;
        try {
            result = fTransformer.transform((Element)overrideElem, (Element)overridenElem2);
        } catch (OverrideTransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //This is the transformed overridden schema             
        Document resultDoc = parseXmlDom(getResourceURL(exResultElem));
        Node resultElem = resultDoc.getDocumentElement();
        result.normalize();
        resultElem.normalize();
       
        assertTrue("Error performing Override Transformations",resultElem.isEqualNode(result));
    }
    
    
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(OverrideTransformTest.class);
    }
    
}
