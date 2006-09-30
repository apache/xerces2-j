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

package org.apache.xerces.stax;

import org.apache.xerces.xni.QName;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Hua Lei
 * 
 * @version $Id$
 */
final class AsyncSAXParser extends Thread {
    
    XMLReader xr;
    private InputSource is;
    
    // The buffer which records the Characters and Space
    private char[] charactersBuf;
    
    // The current attributes of start element. The key is element name, and the 
    // value is its attributes
    private Attributes attrs;
    
    // The current element name
    private final QName elementName = new QName();
    
    Exception ex = null;
    
    public AsyncSAXParser(XMLReader xr, InputSource is) {
        this.xr = xr;
        this.is = is;  
    }
    
    /**
     * The thread begins to parse XML source. 
     */
    public void run(){
        try {
            xr.parse(is);
        }
        catch(Exception e){
//          System.out.print("Error occurs during the SAX parsing process at ");
//          System.out.flush();
//          e.printStackTrace();
            ex = e;
//          throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Set the characters buffer for characters event
     * 
     * @param ch
     * @param start
     * @param length
     */
    public void setCharacters(char[] ch, int start, int length) {
        if(ch == null) {
            charactersBuf = new char[0];
            return;
        }
        
        charactersBuf = new char[length];
        System.arraycopy(ch, start, charactersBuf, 0, length);
    }
    
    /**
     * Get the current characters buffer
     * 
     * @return
     */
    public char[] getCharacters() {
        return charactersBuf;
    }
    
    /**
     * Set the attributes for Start element
     * 
     * @param attrs
     */
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }
    
    /**
     * Get the current attributes
     * 
     * @return
     */
    public Attributes getAttributes() {
        return attrs;
    }
    
    /**
     * Sets the element name for the current startElement/endElement event
     */
    public void setElementName(String uri, String localName, String qName) {
        elementName.setValues(null, localName, qName, uri);
    }
    
    /**
     * Gets the QName for the current element.
     */
    public String getQName() {
        return elementName.rawname;
    }
    
    /**
     * Gets the local name for the current element or
     * the entity name if the current event is an entity reference.
     */
    public String getLocalName() {
        return elementName.localpart;
    }
    
    /**
     * Gets the namespace for the current element.
     */
    public String getNamespaceURI() {
        return elementName.uri;
    }
    
    /**
     * Sets the name of the current entity reference.
     */
    public void setEntityName(String name) {
        elementName.localpart = name;
    }
    
    // Record the data and target of ProcessingInstruction
    private String piData;
    private String piTarget;
    
    public void setPI(String piData, String piTarget) {
        this.piData = piData;
        this.piTarget = piTarget;
    }
    
    public String getPIData() {
        return piData;
    }
    
    public String getPITarget() {
        return piTarget;
    }
    
}
