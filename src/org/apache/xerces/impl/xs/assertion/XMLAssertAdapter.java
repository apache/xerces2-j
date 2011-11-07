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

import java.util.Hashtable;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;

/**
 * A convenience implementation of the assertions interface. All compliant assertions
 * processors (using a specific XPath 2.0 engine) should extend this class.
 * 
 * @xerces.internal
 * 
 * @author: Mukul Gandhi, IBM
 * @version $Id$
 */
public class XMLAssertAdapter extends XSAssertionXPath2ValueImpl implements XMLAssertHandler {
    
    // this hashtable contains any implementation specific properties
    private Hashtable properties = null;

    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws Exception {
        // TODO Auto-generated method stub

    }
    
    public void endElement(QName element, Augmentations augs) throws Exception {
        // TODO Auto-generated method stub

    }
    
    public void characters(XMLString text) {
        // TODO Auto-generated method stub

    }
    
    public void comment(XMLString text) {
        // TODO Auto-generated method stub
        
    }
    
    public void processingInstruction(String target, XMLString data) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Allows the user to set specific properties on the underlying implementation.
     * @param name    name of property
     * @param value   null means to remove property
     */
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        // this handles removal of properties
        if (value == null) {
          if (properties != null) {
             properties.remove(name);
          }
          // Unrecognized properties do not cause an exception
          return;
        }
        
        // create Hashtable if none existed before
        if (properties == null) {
            properties = new Hashtable();
        }
        
        properties.put(name, value);
    }
    
    
    /**
     * Allows the user to retrieve specific properties on the underlying 
     * implementation.
     */
    public Object getProperty(String name) throws IllegalArgumentException {
        // See if it's in the properties Hashtable
        if (properties != null) {
            Object val = properties.get(name);
            if (val != null) {
              return val;
            }
            else {
              throw new IllegalArgumentException("the property "+name+" is not set. can't find it's value");
            }
        }
        
        // unreach
        return null;
    }

} // class XMLAssertAdapter
