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

package org.apache.xerces.xni;

import org.apache.xerces.xs.XSAttributeDeclaration;

/**
 * An object of this class stores a typed attribute information (an attribute's value and it's XSModel declaration)
 * at runtime. This class is used for example to represent inheritable attribute PSVI information.
 * 
 * @author Mukul Gandhi IBM
 * @version $Id$
 */
public class XMLAttribute {
    
    private XSAttributeDeclaration fAttrDecl = null;
    private String fAttrValue = null;
    
    // Class constructor
    public XMLAttribute(XSAttributeDeclaration attrDecl, String attrValue) {
        fAttrDecl = attrDecl;
        fAttrValue = attrValue;
    }
    
    /**
     * @return the fAttrDecl
     */
    public XSAttributeDeclaration getAttrDecl() {
        return fAttrDecl;
    }

    /**
     * @return the fAttrValue
     */
    public String getAttrValue() {
        return fAttrValue;
    }
    
} // class XMLAttribute 
