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

import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;

/**
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
public class ComplexTest extends AbstractTestCase {
    
    private static final String DOC1 = "<?xml version='1.0' encoding='UTF-8'?>" +
    		"<!DOCTYPE root [" +
    		"<!ENTITY a '<r/>0<s/>'>" +
    		"<!ENTITY b '1&a;<t/>&a;2'>" +
    		"<!ENTITY c '&b;'>" +
    		"]><root>&c;3<i/>&c;</root>";
    
    private static final String DOC2 = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<!DOCTYPE root [" +
            "<!ENTITY a '<child/>'>" +
            "<!ENTITY b '<!-- comment -->&a;<![CDATA[text]]>'>" +
            "<!ENTITY c '&b;'>" +
            "]><root>&c;</root>";
    
    public void testGetFirstChild1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getFirstElementChild();
        assertEquals("r", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getFirstElementChild();
        assertNull(e);
    }
    
    public void testGetFirstChild2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getFirstElementChild();
        assertEquals("child", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getFirstElementChild();
        assertNull(e);
    }
    
    public void testGetLastChild1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getLastElementChild();
        assertEquals("s", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getLastElementChild();
        assertNull(e);
    }
    
    public void testGetLastChild2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getLastElementChild();
        assertEquals("child", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getLastElementChild();
        assertNull(e);
    }
    
    public void testGetNextElementSibling1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getFirstElementChild();
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("s", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("t", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("r", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("s", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertEquals("i", e.getNodeName());
    }
    
    public void testGetNextElementSibling2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getFirstElementChild();
        et = toElementTraversal(e);
        e = et.getNextElementSibling();
        assertNull(e);
    }
    
    public void testGetPreviousElementSibling1() {
        ElementTraversal et = parse(DOC1);
        Element e = et.getLastElementChild();
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("r", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("t", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("s", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("r", e.getNodeName());
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertEquals("i", e.getNodeName());
    }
    
    public void testGetPreviousElementSibling2() {
        ElementTraversal et = parse(DOC2);
        Element e = et.getLastElementChild();
        et = toElementTraversal(e);
        e = et.getPreviousElementSibling();
        assertNull(e);
    }
    
    public void testChildElementCount1() {
        ElementTraversal et = parse(DOC1);
        assertEquals(11, et.getChildElementCount());
    }
    
    public void testChildElementCount2() {
        ElementTraversal et = parse(DOC2);
        assertEquals(1, et.getChildElementCount());
    }
}
