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

import javax.xml.stream.XMLStreamConstants;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Hua Lei
 * 
 * @version $Id$
 */
final class StAXSAXHandler extends DefaultHandler {
    
    private AsyncSAXParser asp;
    private SAXXMLStreamReaderImpl reader;
    private SAXLocation loc;
    private StringBuffer buf;
    
    public StAXSAXHandler(AsyncSAXParser asp, SAXXMLStreamReaderImpl reader, SAXLocation loc) {
        this.asp = asp;
        this.reader = reader;
        this.loc = loc;
        if (reader.isCoalescing) {
            buf = new StringBuffer();
        }
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            synchronized (asp) {
                reader.setCurType(XMLStreamConstants.CHARACTERS);
                
                if (reader.isCoalescing) {
                    buf.append(ch, start, length);
                }
                else {
                    asp.setCharacters(ch, start, length);
                    
                    while (!asp.getRunningFlag()) {
                        asp.notify();
                        asp.wait();
                    }
                    
                    asp.setRunningFlag(false);
                }
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public void endDocument() throws SAXException{
        try {
            synchronized (asp) {
                checkCoalescing();
                
                reader.setCurType(XMLStreamConstants.END_DOCUMENT);
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            synchronized (asp) {
                checkCoalescing();
                
                reader.setCurType(XMLStreamConstants.END_ELEMENT);
                if (qName != null && !"".equals(qName)) {
                    asp.setElementName(qName);
                }
                else {
                    asp.setElementName(localName);
                }
                asp.setUri(uri);
                
                while (asp.getRunningFlag() == false) {
                    asp.notify();
                    asp.wait();
                }
                
                NamespaceContextImpl nci = (NamespaceContextImpl)reader.getNamespaceContext();
                nci.onEndElement();
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        try {
            synchronized (asp) {
                checkCoalescing();
                
                reader.setCurType(XMLStreamConstants.SPACE);
                asp.setCharacters(ch, start, length);
                
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }                
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void notationDecl(String name, String publicId, String systemId) throws SAXException {
        try {
            synchronized (asp) {
                reader.setCurType(XMLStreamConstants.NOTATION_DECLARATION);
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void processingInstruction(String target, String data) throws SAXException {
        try {
            reader.setCurType(XMLStreamConstants.PROCESSING_INSTRUCTION);
            asp.setPI(data, target);
            
            synchronized (asp) {
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void startDocument() throws SAXException {
        try {
            reader.setCurType(XMLStreamConstants.START_DOCUMENT);
            
            synchronized (asp) {
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    public synchronized void startElement(String uri, String localName, 
            String qName, Attributes attributes) throws SAXException {
        try {
            synchronized (asp) {
                checkCoalescing();
                
                reader.setCurType(XMLStreamConstants.START_ELEMENT);
                reader.initialElementAttrs(attributes);
                
                NamespaceContextImpl nci = (NamespaceContextImpl)reader.getNamespaceContext();
                nci.onStartElement();
                if (qName != null && !"".equals(qName)) {
                    asp.setElementName(qName);
                }
                else {
                    asp.setElementName(localName);
                }
                asp.setUri(uri);
                
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);                
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    
    public synchronized void unparsedEntityDecl(String name, String publicId, 
            String systemId, String notationName) throws SAXException {
        try {
            synchronized (asp) {
                reader.setCurType(XMLStreamConstants.ENTITY_DECLARATION);
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    /**
     * If feature http://xml.org/sax/features/namespace-prefixes is set to false,
     * namespace binding is done in this method.
     */
    public synchronized void startPrefixMapping(String prefix, String uri) {
        try {
            if (!asp.xr.getFeature("http://xml.org/sax/features/namespace-prefixes")) {
                NamespaceContextImpl nci = (NamespaceContextImpl)reader.getNamespaceContext();
                nci.addNamespace(prefix, uri);
            }
        } 
        catch (Exception e) {
            //It is said that
            //all XMLReaders are required to recognize the http://xml.org/sax/features/namespaces 
            //and the http://xml.org/sax/features/namespace-prefixes feature names
            //in SAX JavaDoc.
        }
    }
    
    public synchronized void setDocumentLocator(Locator locator) {
        loc.setLocator(locator);
    }
    
    public synchronized void skippedEntity(String name) throws SAXException{
        try {
            synchronized (asp) {
                checkCoalescing();
                
                reader.setCurType(XMLStreamConstants.ENTITY_REFERENCE);
                asp.setElementName(name);
                asp.setCharacters(null, 0, 0);
                while (!asp.getRunningFlag()) {
                    asp.notify();
                    asp.wait();
                }
                
                asp.setRunningFlag(false);
            }
        }
        catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
    }
    
    private void checkCoalescing() throws InterruptedException {
        if (reader.isCoalescing && reader.curType == XMLStreamConstants.CHARACTERS) {
            char[] chs = buf.toString().toCharArray();
            asp.setCharacters(chs, 0, chs.length);
            buf.setLength(0);
            
            while (!asp.getRunningFlag()) {
                asp.notify();
                asp.wait();
            }
            
            asp.setRunningFlag(false);
        }
    }
}
