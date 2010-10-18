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

/**
 * Tests for the SCD API that designate components of a schema that
 * has a default namespace.
 * @version $Id$
 * @author Ishan Jayawardena udeshike@gmail.com
 */
public class HasDefaultNamespace extends BaseTest {

    private HasDefaultNamespace(String URI) {
        super(URI);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        HasDefaultNamespace test = new HasDefaultNamespace("tests/scd/po2.xsd");

        test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/schemaElement::p:purchaseOrder)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/schemaElement::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/~p:USAddress)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/~p:USAddress/component::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/~p:USAddress/schemaAttribute::country)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:USAddress/model::sequence/schemaElement::*[4])");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:PurchaseOrderType/model::sequence/any::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:PurchaseOrderType/model::sequence/schemaElement::shipTo)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/~p:PurchaseOrderType/shipTo)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:PurchaseOrderType/anyAttribute::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:Items/model::sequence/schemaElement::item/~0/model::sequence/schemaElement::p:comment)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:Items/model::sequence/schemaElement::item/~0/model::sequence/schemaElement::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:Items/model::sequence/schemaElement::item/~0/attributeUse::*)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/~p:PurchaseOrderType/schemaAttribute::orderDate)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:USAddress/model::sequence/schemaElement::city)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:Items/model::sequence/schemaElement::item/type::0/model::sequence/schemaElement::*[1])");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:Items/model::sequence/schemaElement::item/type::0/model::sequence/schemaElement::quantity/type::0)");
        // test.testResolveString("xmlns(p=http://www.example.com/schema/po)xscd(/type::p:SKU/facet::pattern)");
    }

}
