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

import javax.xml.stream.XMLStreamConstants;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Hua Lei
 * 
 * @version $Id$
 */
final class StAXSAXHandler extends DefaultHandler {
    
    private AsyncSAXParser asp;
    private SAXXMLStreamReaderImpl reader;
    
    public StAXSAXHandler(AsyncSAXParser asp, SAXXMLStreamReaderImpl reader) {
        this.asp = asp;
        this.reader = reader;  
    }
    
    public void characters(char[] ch, int start, int length) {
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.CHARACTERS);
                
                asp.setRunningFlag(false);
                asp.setCharacters(ch, start, length);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public void endDocument(){
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.END_DOCUMENT);
                
                asp.setRunningFlag(false);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public synchronized void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName){
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.END_ELEMENT);
                
                asp.setRunningFlag(false);
                
                NamespaceContextImpl nci = (NamespaceContextImpl)reader.getNamespaceContext();
                nci.onEndElement();
                
                asp.setElementName(qName);
                asp.notify();
            }
        }
        catch(Exception e){}
    }
    
    public synchronized void ignorableWhitespace(char[] ch, int start, int length){
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.SPACE);
                
                asp.setRunningFlag(false);
                asp.setCharacters(ch, start, length);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public synchronized void notationDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId){
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.NOTATION_DECLARATION);
                
                asp.setRunningFlag(false);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public synchronized void processingInstruction(java.lang.String target, java.lang.String data){
        
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.PROCESSING_INSTRUCTION);
                
                asp.setPI(data, target);
                asp.setRunningFlag(false);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public synchronized void startDocument() {
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.START_DOCUMENT);
                
                asp.setRunningFlag(false);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    public synchronized void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes){
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                
                reader.setCurType(XMLStreamConstants.START_ELEMENT);
                
                asp.setRunningFlag(false);
                
                reader.initialElementAttrs(attributes);
                
                NamespaceContextImpl nci = (NamespaceContextImpl)reader.getNamespaceContext();
                nci.onStartElement();
                asp.setElementName(qName);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    
    public synchronized void unparsedEntityDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, java.lang.String notationName){
        System.out.println("ENTITY_DECLARATION");
        try {
            synchronized (asp) {
                while (asp.getRunningFlag() == false)
                    asp.wait();
                reader.setCurType(XMLStreamConstants.ENTITY_DECLARATION);
                asp.setRunningFlag(false);
                asp.notify();
            }
        }
        catch(Exception e) {}
    }
    
    // Need to be realized
    /*
     public synchronized void startPrefixMapping(java.lang.String prefix, java.lang.String uri){
     System.out.println("startPrefixMapping");
     asp.setRunningFlag(false);
     }
     
     public synchronized void endPrefixMapping(java.lang.String prefix){
     System.out.println("endPrefixMapping");
     asp.setRunningFlag(false);
     }
     
     public synchronized void skippedEntity(java.lang.String name){
     System.out.println("skippedEntity");
     asp.setRunningFlag(false);
     }
     */
}
