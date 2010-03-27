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

package org.apache.xerces.impl.xs.util;

import org.apache.xerces.xs.XSTypeDefinition;

/**
 * Utility methods related to schema types.
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id: $
 */
public class XSTypeHelper {
    
    /*
     * Checks if the two schema type components are identical.
     */
    public static boolean schemaTypesIdentical(XSTypeDefinition typeDefn1,
                                               XSTypeDefinition typeDefn2) {
        boolean typesIdentical = false;
        
        String type1Ns = typeDefn1.getNamespace();
        String type1Name = typeDefn1.getName();        
        boolean nsEqual = false;           
        if ((type1Ns != null && type1Ns.equals(typeDefn2.getNamespace())) ||
                  (type1Ns == null && typeDefn2.getNamespace() == null)) {
           nsEqual = true;   
        }
        if (nsEqual == true && type1Name.equals(typeDefn2.getName())) {
           typesIdentical = true;   
        }
        
        return typesIdentical;
        
    } // end of, schemaTypesIdentical
}
