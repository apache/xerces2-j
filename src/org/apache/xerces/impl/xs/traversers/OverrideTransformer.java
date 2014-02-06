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
package org.apache.xerces.impl.xs.traversers;

import org.w3c.dom.Element;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public abstract class OverrideTransformer {

    // different type definitions needed to record type of OverrideElement
    // before processing is done 
    protected final static int OVERRIDE_SIMPLE_TYPE = 1 ;
    protected final static int OVERRIDE_COMPLEX_TYPE = 2 ;
    protected final static int OVERRIDE_ATTRIBUTE_GROUP = 3 ;
    protected final static int OVERRIDE_GROUP = 4 ;
    protected final static int OVERRIDE_ELEMENT = 5 ;
    protected final static int OVERRIDE_NOTATION = 6 ;
    protected final static int OVERRIDE_ATTRIBUTE = 7 ;  

    // Constructor - not accessible
    protected OverrideTransformer(){
    }

    /**
     * Any transformer Implementation should implement this method
     * given a DOM overrideElement, and overridden schema. This method
     * should give the transformed DOM element.
     *
     */
    public abstract Element transform(Element overrideElement, Element overridenSchema) throws OverrideTransformException;
}
