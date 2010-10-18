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

import java.util.List;

import org.apache.xerces.impl.scd.SCDException;
import org.apache.xerces.impl.scd.SCDResolver;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;


/**
 * @version $Id$
 */
public class BaseTest implements DOMErrorHandler {
    private SCDResolver resolver;

    private BaseTest() {}
    public BaseTest(String URI) {
        resolver = new SCDResolver(createXSModel(URI));
    }

    private XSModel createXSModel(String URI) {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
            XSLoader schemaLoader = impl.createXSLoader(null);
            DOMConfiguration config = schemaLoader.getConfig();
            // create Error Handler
            DOMErrorHandler errorHandler = new BaseTest();
            // set error handler
            config.setParameter("error-handler", errorHandler);
            // set validation feature
            config.setParameter("validate", Boolean.TRUE);
            // parse document
            System.out.println("Parsing " + URI + "...");
            return schemaLoader.loadURI(URI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String)}.
     */
    public XSObjectList testResolveString(String s) {
        try {
            XSObjectList result = resolver.resolve(s);
            printComps(result);
            return result;
        } catch (SCDException e) {
            System.err.println(e.getKey());
            e.printStackTrace();
        }
        return new XSObjectListImpl();
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xni.NamespaceContext)}.
     */
    public XSObjectList testResolveStringNamespaceContext(String s, NamespaceContext nc) {
        try {
            XSObjectList result = resolver.resolve(s, nc);
            printComps(result);
            return result;
        } catch (SCDException e) {
            System.err.println(e.getKey());
            e.printStackTrace();
        }
        return new XSObjectListImpl();
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xni.NamespaceContext, org.apache.xerces.xs.XSObject)}.
     */

    public XSObjectList testResolveStringNamespaceContextXSObject(String s, NamespaceContext nc, XSObject comp) {
        try {
            XSObjectList result = resolver.resolve(s, nc, comp);
            printComps(result);
            return result;
        } catch (SCDException e) {
            System.err.println(e.getKey());
            e.printStackTrace();
        }
        return new XSObjectListImpl();
    }

    /**
     * Test method for {@link org.apache.xerces.impl.scd.SCDResolver#resolve(java.lang.String, org.apache.xerces.xs.XSObject)}.
     */
    public XSObjectList testResolveStringXSObject(String s, XSObject comp) {
        try {
            XSObjectList result = resolver.resolve(s, comp);
            printComps(result);
            return result;
        } catch (SCDException e) {
            System.err.println(e.getKey());
            e.printStackTrace();
        }
        return new XSObjectListImpl();
    }

    private static final String[] typeNames = {"", "ATTRIBUTE_DECLARATION", "ELEMENT_DECLARATION", "TYPE_DEFINITION",
        "ATTRIBUTE_USE", "ATTRIBUTE_GROUP", "MODEL_GROUP_DEFINITION", "MODEL_GROUP", "PARTICLE", "WILDCARD",
        "IDENTITY_CONSTRAINT", "NOTATION_DECLARATION", "ANNOTATION", "FACET", "MULTIVALUE_FACET"};


    public static void printComps(List comps) { 
        System.out.println("\n{"+comps.size()+"} resulting components###\n[");
        for (int i = 0; i < comps.size(); ++i) { // TODO: this fails if the type is a Particle
            System.out.println("  " + typeNames[((XSObject)comps.get(i)).getType()]
                                                + " {" + ((XSObject)comps.get(i)).getNamespace()+ ", "+ ((XSObject)comps.get(i)).getName()+"}");
        }
        System.out.println("]");
    }

    public boolean handleError(DOMError error) {
        short severity = error.getSeverity();
        if (severity == DOMError.SEVERITY_ERROR) {
            System.out.println("[SCD-Error]: " + error.getMessage());
        }

        if (severity == DOMError.SEVERITY_WARNING) {
            System.out.println("[SCD-Warning]: " + error.getMessage());
        }
        return true;
    }
}
