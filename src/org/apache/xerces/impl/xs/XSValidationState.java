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

import org.apache.xerces.impl.validation.ConfigurableValidationState;

/**
 * @xerces.internal 
 * 
 * @version $Id$
 */
final class XSValidationState extends ConfigurableValidationState {
    
    private IDContext fIDContext;

    public XSValidationState() {
        super();
    }

    public void addId(String name) {
        if (fIDContext == null) {
            super.addId(name);
        }
        else if (fIdIdrefChecking) {
            fIDContext.add(name);
        }
    }

    public boolean isIdDeclared(String name) {
        if (fIDContext == null) {
            return super.isIdDeclared(name);
        }
        return (fIdIdrefChecking) ? fIDContext.isDeclared(name) : false; 
    }

    void setIDContext(IDContext idContext) {
        fIDContext = idContext;
    }
    
    protected boolean containsID(String name) {
        return (fIDContext == null) ? super.containsID(name) : fIDContext.containsID(name);
    }
}
