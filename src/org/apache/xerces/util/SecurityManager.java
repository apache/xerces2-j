/*
 * Copyright 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.util;

/**
 * This class is a container for parser settings that relate to 
 * security, or more specifically, it is intended to be used to prevent denial-of-service 
 * attacks from being launched against a system running Xerces.  
 * Any component that is aware of a denial-of-service attack that can arise
 * from its processing of a certain kind of document may query its Component Manager
 * for the property (http://apache.org/xml/properties/security-manager) 
 * whose value will be an instance of this class.  
 * If no value has been set for the property, the component should proceed in the "usual" (spec-compliant)
 * manner.  If a value has been set, then it must be the case that the component in
 * question needs to know what method of this class to query.  This class
 * will provide defaults for all known security issues, but will also provide
 * setters so that those values can be tailored by applications that care.
 *
 * @author  Neil Graham, IBM
 *
 * @version $Id$
 */
public final class SecurityManager {

    //
    // Constants
    //

    // default value for entity expansion limit
    private final static int DEFAULT_ENTITY_EXPANSION_LIMIT = 100000;
    
    //default value of number of nodes created 
    private final static int DEFAULT_MAX_OCCUR_NODE_LIMIT = 3000;

    //
    // Data
    //

    /** entity expansion limit */
    private int entityExpansionLimit;
    private int maxOccurLimit;

    // default constructor.  Establishes default values for
    // all known security holes.  
    public SecurityManager() {
        entityExpansionLimit = DEFAULT_ENTITY_EXPANSION_LIMIT;
        maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT ;
    }

    // set the number of entity expansions that the
    // parser should permit in a document
    public void setEntityExpansionLimit(int limit) {
        entityExpansionLimit = limit;
    }

    // return the number of entity expansions that the
    // parser permits in a document
    public int getEntityExpansionLimit() {
        return entityExpansionLimit;
    }
    
    //sets the limit of the number of content model nodes that may be created when building
    // a grammar for a schema that contains maxOccurs attributes with values
    // other than "unbounded"
    public void setMaxOccurNodeLimit(int limit){
        maxOccurLimit = limit ;
    }
    
    
    //sets the limit of the number of content model nodes that may be created when building
    // a grammar for a schema that contains maxOccurs attributes with values
    // other than "unbounded" 
    public int getMaxOccurNodeLimit(){
        return maxOccurLimit ;    
    }
    
} // class SecurityManager

