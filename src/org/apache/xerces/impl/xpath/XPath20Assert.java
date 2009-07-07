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

package org.apache.xerces.impl.xpath;

import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.NamespaceContext;

/**
 * A class representing an XPath 2.0 expression for, assertions evaluation.
 *
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XPath20Assert {
    
    protected final String fExpression;
    protected final NamespaceContext fContext;
    
    public XPath20Assert(String xpath, SymbolTable symbolTable,
                         NamespaceContext context) {
        fExpression = xpath;
        fContext = context;
    }

    public String toString() {
        return fExpression;
    }
}
