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

package scd;

import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xs.XSObject;

import junit.framework.TestCase;

/**
 * @author Ishan Jayawardena udeshike@gmail.com
 * @version $Id$
 */
public class NoTargetNamespace extends TestCase {

    private BaseTest fNoTargetNamespace = new BaseTest("tests/scd/po1.xsd");

    public NoTargetNamespace(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String)}.
     */
    public void testResolveString() {
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType/model::sequence/schemaElement::shipTo)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType/attributeUse::*)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType/model::sequence/schemaElement::billTo)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/schemaElement::comment)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType/model::sequence/schemaElement::items)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::PurchaseOrderType/schemaAttribute::orderDate)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/model::sequence/schemaElement::name)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/model::sequence/schemaElement::street)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/model::sequence/schemaElement::city)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/model::sequence/schemaElement::state)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/model::sequence/schemaElement::zip)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::USAddress/schemaAttribute::country)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(//quantity)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/model::sequence/schemaElement::item/type::0)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/model::sequence/schemaElement::item/type::0/model::sequence/schemaElement::productName)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/model::sequence/schemaElement::item/type::0/model::sequence/schemaElement::quantity/type::0/facet::maxExclusive)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/model::sequence/schemaElement::item/type::0/component::partNum)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::SKU)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::SKU/facet::pattern)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/annotation::*)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/model::sequence/schemaElement::item/type::0/model::sequence/schemaElement::*[3])").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/type::Items/component::comment)").isEmpty());
        /// abbreviated SCDs
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~PurchaseOrderType)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~PurchaseOrderType/shipTo)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/comment)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~PurchaseOrderType/items)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~Items/item/~0)").isEmpty());
        // next two do not work
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~Items/item/productName)").isEmpty());
        assertEquals(false, fNoTargetNamespace.testResolveString("xscd(/~Items/item/quantity/~0)").isEmpty());
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xni.NamespaceContext)}.
     */
    public void testResolveStringNamespaceContext() {
        // define a namespace context
        NamespaceContext nsContext = new NamespaceSupport();
        nsContext.declarePrefix(null, null);
        // get a schema component
        XSObject currentComponent = fNoTargetNamespace.testResolveString("xscd(/type::Items)").item(0);
        // evaluate
        assertEquals(false, fNoTargetNamespace.testResolveStringNamespaceContextXSObject(
                "./model::sequence/schemaElement::item/type::0//schemaAttribute::partNum",
                nsContext,
                currentComponent).isEmpty());
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xni.NamespaceContext, org.apache.xerces.xs.XSObject)}.
     */
    public void testResolveStringNamespaceContextXSObject() {
        XSObject currentComponent = fNoTargetNamespace.testResolveString("xscd(/type::Items)").item(0);
        assertEquals(false, fNoTargetNamespace.testResolveStringXSObject(
                "xscd(./model::sequence/schemaElement::item/type::0//schemaAttribute::partNum)",
                currentComponent).isEmpty());
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xs.XSObject)}.
     */
    public void testResolveStringXSObject() {
        NamespaceContext nsContext = new NamespaceSupport();
        nsContext.declarePrefix(null, null);
        assertEquals(false, fNoTargetNamespace.testResolveStringNamespaceContext(
                "/type::Items/model::sequence/schemaElement::item/type::0//schemaAttribute::partNum",
                nsContext).isEmpty());
    }

}
