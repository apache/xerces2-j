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

import java.io.Writer;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public class XMLEventWriterImpl implements XMLEventWriter {

    private final XMLStreamWriter fStreamWriter;
    private final Writer fUnderlyingWriter;
    
    public XMLEventWriterImpl(XMLStreamWriter streamWriter) {
        this(streamWriter, null);
    }
    
    

    public XMLEventWriterImpl(XMLStreamWriter streamWriter, Writer underlyingWriter) {
        fStreamWriter = streamWriter;
        fUnderlyingWriter = underlyingWriter;
    }
    
    /**
     * @see javax.xml.stream.XMLEventWriter#flush()
     */
    public void flush() throws XMLStreamException {
        fStreamWriter.flush();
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#close()
     */
    public void close() throws XMLStreamException {
        fStreamWriter.close();
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.events.XMLEvent)
     */
    public void add(XMLEvent event) throws XMLStreamException {
        switch(event.getEventType()) {
            case XMLEvent.ATTRIBUTE:
                writeAttribute((Attribute)event);
                break;
            case XMLEvent.SPACE:
            case XMLEvent.CDATA:
            case XMLEvent.CHARACTERS:
                writeCharacters((Characters)event);
                break;
            case XMLEvent.COMMENT:
                fStreamWriter.writeComment(((Comment)event).getText());
                break;
            case XMLEvent.DTD:
                writeDTD((DTD)event);
                break;
            case XMLEvent.END_DOCUMENT:
                fStreamWriter.writeEndDocument();
                break;
            case XMLEvent.END_ELEMENT:
                fStreamWriter.writeEndElement();
                break;
            case XMLEvent.ENTITY_DECLARATION:
                //TODO: Should this do anything?
                break;
            case XMLEvent.ENTITY_REFERENCE:
                fStreamWriter.writeEntityRef(((EntityReference)event).getName());
                break;
            case XMLEvent.NAMESPACE:
                Namespace namespace = (Namespace)event;
                fStreamWriter.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
            case XMLEvent.NOTATION_DECLARATION:
                //TODO: Should this do anything?
                break;
            case XMLEvent.PROCESSING_INSTRUCTION:
                writePI((ProcessingInstruction)event);
                break;
            case XMLEvent.START_DOCUMENT:
                writeStartDoc((StartDocument)event);
                break;
            case XMLEvent.START_ELEMENT:
                writeStartElement((StartElement)event);
                break;
            default:
                if (fUnderlyingWriter != null) {
                    event.writeAsEncodedUnicode(fUnderlyingWriter);
                }
                break;
                    
        }
        
    }


    /**
     * @see javax.xml.stream.XMLEventWriter#add(javax.xml.stream.XMLEventReader)
     */
    public void add(XMLEventReader reader) throws XMLStreamException {
        while(reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) throws XMLStreamException {
        return fStreamWriter.getPrefix(uri);
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#setPrefix(java.lang.String, java.lang.String)
     */
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        fStreamWriter.setPrefix(prefix, uri);
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#setDefaultNamespace(java.lang.String)
     */
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        fStreamWriter.setDefaultNamespace(uri);
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#setNamespaceContext(javax.xml.namespace.NamespaceContext)
     */
    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException {
        fStreamWriter.setNamespaceContext(context);
    }

    /**
     * @see javax.xml.stream.XMLEventWriter#getNamespaceContext()
     */
    public NamespaceContext getNamespaceContext() {
        return fStreamWriter.getNamespaceContext();
    }

    /**
     * @param instruction
     */
    private void writePI(ProcessingInstruction pi) throws XMLStreamException {
        if (pi.getData() != null) {
            fStreamWriter.writeProcessingInstruction(pi.getTarget(), pi.getData());
        }
        else {
            fStreamWriter.writeProcessingInstruction(pi.getTarget());
        }
    }



    /**
     * @param element
     */
    private void writeStartElement(StartElement element) throws XMLStreamException {
        for (Iterator iter = element.getNamespaces(); iter.hasNext();) {
            Namespace namespace = (Namespace) iter.next();
            fStreamWriter.setPrefix(namespace.getPrefix(), namespace.getNamespaceURI());    
        }
        
        String namespaceURI = element.getName().getNamespaceURI();
        String prefix = element.getName().getPrefix();
        if (prefix == null &&  namespaceURI != null) {
            prefix = fStreamWriter.getPrefix(namespaceURI);
        }

        fStreamWriter.writeStartElement(prefix, element.getName().getLocalPart(), element.getName().getNamespaceURI());
        
        for (Iterator iter = element.getNamespaces(); iter.hasNext();) {
            Namespace namespace = (Namespace) iter.next();
            fStreamWriter.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());    
        }

        for (Iterator iter = element.getAttributes(); iter.hasNext();) {
            Attribute attr = (Attribute) iter.next();
            writeAttribute(attr);
        }
    }



    /**
     * @param document
     */
    private void writeStartDoc(StartDocument document) throws XMLStreamException {
        String version = document.getVersion();
        String encoding = document.getCharacterEncodingScheme();
        fStreamWriter.writeStartDocument(encoding, version);
    }



    /**
     * @param dtd DTD event to write.
     */
    private void writeDTD(DTD dtd) throws XMLStreamException {
        if(dtd.getDocumentTypeDeclaration() != null) {
            fStreamWriter.writeDTD(dtd.getDocumentTypeDeclaration());
        }
    }



    /**
     * @param characters
     */
    private void writeCharacters(Characters characters) throws XMLStreamException {
        if (characters.isCData()) {
            fStreamWriter.writeCData(characters.getData());
        }
        else {
            fStreamWriter.writeCharacters(characters.getData());
        }
    }



    /**
     * @param attribute
     */
    private void writeAttribute(Attribute attribute) throws XMLStreamException {
        final String namespaceURI = attribute.getName().getNamespaceURI();
        String prefix = attribute.getName().getPrefix();
        if (prefix == null && namespaceURI != null && namespaceURI.length() > 0) {
            prefix = fStreamWriter.getPrefix(namespaceURI);
        }
        if (prefix != null && prefix.length() > 0) { 
            fStreamWriter.writeAttribute(prefix, namespaceURI, attribute.getName().getLocalPart(), attribute.getValue());
        }
        else if (namespaceURI.length() > 0){
            fStreamWriter.writeAttribute(namespaceURI, attribute.getName().getLocalPart(), attribute.getValue());
        }
        else {
            fStreamWriter.writeAttribute(attribute.getName().getLocalPart(), attribute.getValue());
        }
    }
   
    
}
