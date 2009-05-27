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

package org.apache.xerces.xni.parser;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;

/**
 * The implementation of this interface will invoke an external XPath engine, which
 * would evaluate the XPath 2.0 expressions, in XML Schema 1.1 assertions.
 * This interface communicates with the XMLSchemaValidator, and accepts information
 * required by the external XPath engine. If assertions evaluation returns 'true',
 * validation would proceed further. And while upon assertion failure, validation process 
 * terminates in the implementing classes if this interface.
 * 
 * @author Mukul Gandhi, IBM
 * @author Ken Cai, IBM
 * 
 * @version $Id$
 * 
 */
public interface XMLAssertHandler {  

    /*
     * A callback method triggered during startElement method call in, XMLSchemaValidator
     * 
     * @param element
     *             XML element
     * @param attributes
     *             attributes of the element
     * @param assertObject
     *             An object holding the assertions for this element (for the element's type)                                    
     */
    public void startElement(QName element, XMLAttributes attributes, Object assertObject);
    
    /*
     * A callback method triggered during endElement method call in, XMLSchemaValidator
     * 
     * @param element
     *           XML element  
     * @param augs
     *           Augmentations object, to support Schema types          
     */
    public void endElement(QName element, Augmentations augs) throws Exception;
        
    /*
     * A callback method triggered during characters method call in, XMLSchemaValidator
     * 
     * @param text
     *        Text data received during the call
     */
    public void characters(XMLString text);

  
    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.
     * @param name The name of the attribute.
     * @param value The value of the attribute.
     * @exception IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the attribute.
     */
    public void setAttribute(String name, Object value)
                throws IllegalArgumentException;
    
    
    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     * @exception IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the attribute.
     */
    public abstract Object getAttribute(String name)
                throws IllegalArgumentException;

}
