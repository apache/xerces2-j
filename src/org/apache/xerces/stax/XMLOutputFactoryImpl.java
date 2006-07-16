/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.xerces.stax;

import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

/**
 * <p>Implementation of XMLOutputFactory.</p>
 * 
 * @version $Id: $
 */
public class XMLOutputFactoryImpl extends XMLOutputFactory {

    public XMLOutputFactoryImpl() {}
    
    public XMLStreamWriter createXMLStreamWriter(Writer stream)
            throws XMLStreamException {
        return null;
    }
    
    public XMLStreamWriter createXMLStreamWriter(OutputStream stream)
            throws XMLStreamException {
        return null;
    }
    
    public XMLStreamWriter createXMLStreamWriter(OutputStream stream,
            String encoding) throws XMLStreamException {
        return null;
    }

    public XMLStreamWriter createXMLStreamWriter(Result result)
            throws XMLStreamException {
        return null;
    }
    
    public XMLEventWriter createXMLEventWriter(Result result)
            throws XMLStreamException {
        return null;
    }
    
    public XMLEventWriter createXMLEventWriter(OutputStream stream)
            throws XMLStreamException {
        return null;
    }
    
    public XMLEventWriter createXMLEventWriter(OutputStream stream,
            String encoding) throws XMLStreamException {
        return null;
    }
    
    public XMLEventWriter createXMLEventWriter(Writer stream)
            throws XMLStreamException {
        return null;
    }

    public void setProperty(String name, Object value)
            throws IllegalArgumentException {
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }
    
    public boolean isPropertySupported(String name) {
        return false;
    }
}
