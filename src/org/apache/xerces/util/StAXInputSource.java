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

package org.apache.xerces.util;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * <p>An <code>XMLInputSource</code> analogue to <code>javax.xml.transform.stax.StAXSource</code>.</p>
 * 
 * @version $Id$
 */
public final class StAXInputSource extends XMLInputSource {
    
    private XMLStreamReader fStreamReader;
    private XMLEventReader fEventReader;
    
    public StAXInputSource(XMLStreamReader source) {
        super(null, source.getLocation().getSystemId(), null);
        fStreamReader = source;
    }
    
    public StAXInputSource(XMLEventReader source) {
        super(null, getEventReaderSystemId(source), null);
        fEventReader = source;
    }
    
    public XMLStreamReader getXMLStreamReader() {
        return fStreamReader;
    }
    
    public XMLEventReader getXMLEventReader() {
        return fEventReader;
    }
    
    public void setSystemId(String systemId){
        throw new UnsupportedOperationException("Cannot set the system ID on a StAXInputSource");
    }
    
    private static String getEventReaderSystemId(XMLEventReader reader) {
        try {
            return reader.peek().getLocation().getSystemId();
        }
        catch (XMLStreamException e) {
            return null;
        }
    }
    
} // StAXInputSource
