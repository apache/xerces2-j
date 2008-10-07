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

package org.apache.xerces.impl.xs;

/**
 * XML Schema 1.0 constraints
 * 
 * @xerces.internal
 *
 * @author Khaled Noaman, IBM
 *
 * @version $Id$
 *
 */
class XS11Constraints extends XSConstraints {

    public XS11Constraints() {
        super(SchemaGrammar.fAnyType); // TODO: use 1.1 specific anyType
    }

    public boolean overlapUPA(XSElementDecl element,
            XSWildcardDecl wildcard,
            SubstitutionGroupHandler sgHandler) {
        return false;
    }

    public boolean overlapUPA(XSWildcardDecl wildcard1,
            XSWildcardDecl wildcard2) {
        // if the intersection of the two wildcards is not any and
    	// and the {namespaces} of such intersection is not the empty set
        XSWildcardDecl intersect = performIntersectionWith(wildcard1, wildcard2, wildcard1.fProcessContents);
        if (intersect.fType != XSWildcardDecl.NSCONSTRAINT_ANY &&
                intersect.fNamespaceList.length != 0) {
            return true;
        }

        return false;
    }
}
