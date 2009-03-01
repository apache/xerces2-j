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

package dom.traversal;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
public abstract class AbstractTestCase extends TestCase {
    
    private DocumentBuilder fDocumentBuilder;
    
    protected final void setUp() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            fDocumentBuilder = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            fail(pce.getMessage());
        }
    }
    
    protected final void tearDown() {
        fDocumentBuilder = null;
    }
    
    protected final ElementTraversal parse(String input) {
        try {
            Document doc = fDocumentBuilder.parse(new InputSource(new StringReader(input)));
            return toElementTraversal(doc.getDocumentElement());
        } 
        catch (SAXException se) {
            se.printStackTrace();
            fail(se.getMessage());
        } 
        catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }
        return null;
    }
    
    protected final ElementTraversal toElementTraversal(Element e) {
        assertTrue("e instanceof ElementTraversal", e == null || e instanceof ElementTraversal);
        return (ElementTraversal) e;
    }
}
