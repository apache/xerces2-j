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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.xerces.stax.events.AttributeImpl;
import org.apache.xerces.stax.events.CharactersImpl;
import org.apache.xerces.stax.events.CommentImpl;
import org.apache.xerces.stax.events.DTDImpl;
import org.apache.xerces.stax.events.EndDocumentImpl;
import org.apache.xerces.stax.events.EndElementImpl;
import org.apache.xerces.stax.events.EntityReferenceImpl;
import org.apache.xerces.stax.events.NamespaceImpl;
import org.apache.xerces.stax.events.ProcessingInstructionImpl;
import org.apache.xerces.stax.events.StartDocumentImpl;
import org.apache.xerces.stax.events.StartElementImpl;

/**
 * Default implementation of the event allocator interface.
 * 
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public class DefaultEventAllocator implements XMLEventAllocator {

    /**
     * @see javax.xml.stream.util.XMLEventAllocator#newInstance()
     */
    public XMLEventAllocator newInstance() {
        //This object is not stateful - we can just return the same instance each time.
        return this;
    }

    /**
     * @see javax.xml.stream.util.XMLEventAllocator#allocate(javax.xml.stream.XMLStreamReader)
     */
    public XMLEvent allocate(XMLStreamReader reader) throws XMLStreamException {
        int eventType = reader.getEventType(); 
        final Location location = reader.getLocation();
        switch (eventType) {
            case XMLStreamConstants.ATTRIBUTE:
                //This can only happen for cases where just attributes are returned by 
                //a query. I'm assuming that in these cases the stream reader reports each attribute
                //in turn at index 0. 
                return makeAttribute(0, reader);
            case XMLStreamConstants.CDATA:
                 return new CharactersImpl(reader.getText(), false, true, false, location);
            case XMLStreamConstants.CHARACTERS:
                return new CharactersImpl(reader.getText(), false, false, false, location);
            case XMLStreamConstants.COMMENT:
                return new CommentImpl(reader.getText(), location);
            case XMLStreamConstants.DTD:
                return new DTDImpl(reader.getText(), location);
            case XMLStreamConstants.END_DOCUMENT:
                return new EndDocumentImpl(location);
            case XMLStreamConstants.END_ELEMENT:
                return new EndElementImpl(reader.getName(), location);
            case XMLStreamConstants.ENTITY_REFERENCE:
                //TODO: Get the EntityDeclaration.
                return new EntityReferenceImpl(null, location);
            case XMLStreamConstants.NAMESPACE:
                //This can only happen for cases where just namespaces are returned by 
                //a query. I'm assuming that in these cases the stream reader reports each namespace
                //in turn at index 0. 
                final String namespacePrefix = reader.getNamespacePrefix(0);
                return new NamespaceImpl(namespacePrefix, reader.getNamespaceURI(), location);
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                return new ProcessingInstructionImpl(reader.getPITarget(), reader.getPIData(), location);
            case XMLStreamConstants.SPACE:
                //TODO: Ignorable Whitespace
                return new CharactersImpl(reader.getText(), true, false, false, location);
            case XMLStreamConstants.START_DOCUMENT:
                return new StartDocumentImpl(reader.getEncoding(), reader.getCharacterEncodingScheme() != null, reader.isStandalone(), reader.standaloneSet(), reader.getVersion(), location);
            case XMLStreamConstants.START_ELEMENT:
                return makeStartElement(reader);
            default:
                throw new IllegalStateException("Unknown eventType: '" + eventType + "'.");
        }
    }

    /**
     * @see javax.xml.stream.util.XMLEventAllocator#allocate(javax.xml.stream.XMLStreamReader, javax.xml.stream.util.XMLEventConsumer)
     */
    public void allocate(XMLStreamReader reader, XMLEventConsumer consumer)
            throws XMLStreamException {
        consumer.add(allocate(reader));
    }

    /**
     * @return
     */
    private static StartElement makeStartElement(XMLStreamReader streamReader) {
        if (!streamReader.isStartElement()) {
            throw new IllegalStateException("makeStartElement must only be called when in the start element state.");
        }
        
        StartElementImpl startElement = new StartElementImpl(streamReader.getName(), streamReader.getNamespaceContext(), streamReader.getLocation());
        for(int i = 0; i < streamReader.getAttributeCount(); i++) {
            startElement.addAttribute(makeAttribute(i, streamReader));
        }
        for(int i = 0; i < streamReader.getNamespaceCount(); i++) {
            startElement.addNamespace(makeNamespace(i, streamReader));
        }
        return startElement;
    }
    
    /**
     * @param i
     * @return
     */
    private static Namespace makeNamespace(int i, XMLStreamReader streamReader) {
        String prefix = streamReader.getNamespacePrefix(i);
        String namespace = streamReader.getNamespaceURI(i);
        return new NamespaceImpl(prefix, namespace, streamReader.getLocation());
    }

    /**
     * @param i Index of attribute to make in the current list of attributes.
     * @param streamReader TODO
     * @return An attribute factoried from the detail of the attribute at index i
     * given the current state of the reader.
     */
    private static Attribute makeAttribute(final int i, XMLStreamReader streamReader) {
        String prefix = streamReader.getAttributePrefix(i);
        final QName name = new QName(streamReader.getAttributeNamespace(i), streamReader.getAttributeLocalName(i), prefix == null ? "" : prefix); 
        return new AttributeImpl(name, streamReader.getAttributeValue(i), streamReader.getAttributeType(i), streamReader.isAttributeSpecified(i), streamReader.getLocation());
    }
    
}
