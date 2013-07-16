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

package org.apache.xerces.impl.xs.assertion;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;

/**
 * The implementation of this interface will invoke an external XPath engine, that would evaluate the XPath 2.0 expressions
 * for XML Schema 1.1 assertions. This interface communicates with the XMLSchemaValidator and accepts context information required 
 * by the external XPath engine. If assertions evaluation would return 'true', validation would proceed further checking the
 * remaining validation constraints.
 * 
 * @xerces.internal
 * 
 * @author Ken Cai, IBM
 * @author Mukul Gandhi, IBM
 * 
 * @version $Id$
 */
public interface XMLAssertHandler extends XSAssertionXPath2Value {  

    /*
     * A callback method triggered during "startElement" method call in, XMLSchemaValidator
     * 
     * @param element
     *             XML element
     * @param attributes
     *             attributes of the element
     * @param augs
     *             Augmentations object                                    
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws Exception;
    
    /*
     * A callback method triggered during "endElement" method call in, XMLSchemaValidator
     * 
     * @param element
     *           XML element  
     * @param augs
     *           Augmentations object        
     */
    public void endElement(QName element, Augmentations augs) throws Exception;
        
    /*
     * A callback method triggered during "characters" method call in, XMLSchemaValidator
     * 
     * @param text
     *        Text data received during the call
     */
    public void characters(XMLString text);
    
    /*
     * A callback method triggered during "comment" method call in, XMLSchemaValidator
     * 
     * @param text
     *        The text in the comment
     */
    public void comment(XMLString text);
    
    /*
     * A callback method triggered during "processingInstruction" method call in, XMLSchemaValidator
     * 
     * @param target
     *        The target
     * @param data
     *        The data or null if none specified
     */
    public void processingInstruction(String target, XMLString data);

  
    /**
     * Allows the user to set specific properties on the underlying
     * implementation.
     * @param name The name of the property.
     * @param value The value of the property.
     * @exception IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the property.
     */
    public void setProperty(String name, Object value) throws IllegalArgumentException;
    
    
    /**
     * Allows the user to retrieve specific properties on the underlying
     * implementation.
     * @param name The name of the property.
     * @return value The value of the property.
     * @exception IllegalArgumentException thrown if the underlying
     * implementation doesn't recognize the property.
     */
    public abstract Object getProperty(String name) throws IllegalArgumentException;

}
