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

/**
 * Tests for the SCD API that designate components of a schema that
 * has a target namespace. 
 * @author Ishan Jayawardena udeshike@gmail.com
 * @version $Id: $
 */
public class HasTargetNamespace extends BaseTest {

    private HasTargetNamespace(String URI) {
        super(URI);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        HasTargetNamespace test = new HasTargetNamespace("tests/scd/dd.xsd");

        // tests for endpoint.xsd which has a target namespace
        // ##
        // test resolve(String relativeSCD)
        // ##
        // test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tDeployment/model::sequence/schemaElement::dd:process/type::0/model::sequence/schemaElement::dd:active/annotation::*)");
        // test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tInvoke/model::choice/schemaElement::dd:binding/type::0/component::*)");
        // test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tInvoke/model::choice/schemaElement::dd:binding/type::0//schemaAttribute::name)");
        // test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tService/model::sequence/any::*)");
        // test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tProcessEvents/component::*/facet::enumeration)");
        test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tDeployment/model::sequence/schemaElement::dd:process/type::0/schemaElement::*[8])");

        NamespaceContext nsContext = new NamespaceSupport();
        nsContext.declarePrefix("dd", "http://www.apache.org/ode/schemas/dd/2007/03");
        // ##
        // test resolve(String incompleteSCP, NamespaceContext nsContext, XSObject currentComponent)
        // ##
        XSObject currentComponent = test.testResolveString("xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(/type::dd:tDeployment/model::sequence/schemaElement::dd:process)").item(0);
        test.testResolveStringNamespaceContextXSObject(
                "./type::0/model::sequence/schemaElement::dd:active",
                nsContext, 
                currentComponent);

        // ##
        // test resolve(String incompleteSCD, XSObject currentComponent)
        // ##
        test.testResolveStringXSObject(
                "xmlns(dd=http://www.apache.org/ode/schemas/dd/2007/03)xscd(./type::0/model::sequence/schemaElement::dd:active)",
                currentComponent);

        // ##
        // test resolve(String scp, NamespaceContext nsContext)
        // ##
        test.testResolveStringNamespaceContext(
                "/type::dd:tDeployment/model::sequence/schemaElement::dd:process/type::0/model::sequence/schemaElement::dd:active",
                nsContext);

    }

}
