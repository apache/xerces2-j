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

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * <p>Implementation of XMLInputFactory.</p>
 * 
 * @author Hua Lei
 * 
 * @version $Id$
 */
public class XMLInputFactoryImpl extends XMLInputFactory {
    
    ConfigurationContext config = new ConfigurationContext();
    
    DefaultEventAllocator fAllocator = new DefaultEventAllocator();
    
    public XMLInputFactoryImpl() {}
    
    public XMLStreamReader createXMLStreamReader(Reader reader)
    throws XMLStreamException {
        return null;
    }
    
    public XMLStreamReader createXMLStreamReader(Source source)
    throws XMLStreamException {
        if (source instanceof SAXSource) {
            SAXSource ss = (SAXSource) source;
            InputSource is = ss.getInputSource();
            XMLReader xr = ss.getXMLReader();
            if(is == null || xr == null)
                throw new XMLStreamException(
                "Can only create StAX reader for a SAXSource if Reader and InputStream exposed by getSource()");
            
            SAXXMLStreamReaderImpl sxsReader = new SAXXMLStreamReaderImpl(xr, is, this);
            return sxsReader;
        }
        if (source instanceof DOMSource) {
            DOMSource ds = (DOMSource)source;
            Node n = ds.getNode();
            DOMXMLStreamReaderImpl dxsReader = new DOMXMLStreamReaderImpl(n, this);
            return dxsReader;
        }
        throw new UnsupportedOperationException(
                "XMLInputFactory.createXMLStreamReader("
                + source.getClass().getName() + ") not yet implemented");
    }
    
    public XMLStreamReader createXMLStreamReader(InputStream stream)
    throws XMLStreamException {
        return null;
    }
    
    public XMLStreamReader createXMLStreamReader(InputStream stream,
            String encoding) throws XMLStreamException {
        return null;
    }
    
    public XMLStreamReader createXMLStreamReader(String systemId,
            InputStream stream) throws XMLStreamException {
        return null;
    }
    
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader)
    throws XMLStreamException {
        return null;
    }
    
    public XMLEventReader createXMLEventReader(Reader reader)
            throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(reader));
    }
    
    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
            throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(systemId, reader));
    }
    
    public XMLEventReader createXMLEventReader(XMLStreamReader reader)
            throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), reader);
    }
    
    public XMLEventReader createXMLEventReader(Source source)
            throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(source));
    }
    
    public XMLEventReader createXMLEventReader(InputStream stream)
            throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(stream));
    }
    
    public XMLEventReader createXMLEventReader(InputStream stream,
            String encoding) throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(stream, encoding));
    }
    
    public XMLEventReader createXMLEventReader(String systemId,
            InputStream stream) throws XMLStreamException {
        return new XMLEventReaderImpl(createEventAllocator(), createXMLStreamReader(systemId, stream));
    }
    
    
    
    public XMLStreamReader createFilteredReader(XMLStreamReader reader,
            StreamFilter filter) throws XMLStreamException {
        return null;
    }
    
    public XMLEventReader createFilteredReader(XMLEventReader reader,
            EventFilter filter) throws XMLStreamException {
        return null;
    }
    
    /**
     * The resolver that will be set on any XMLStreamReader or XMLEventReader created by this factory instance.
     */
    public XMLResolver getXMLResolver() {
        return config.getXMLResolver();
    }
    
    /**
     * The resolver that will be set on any XMLStreamReader or XMLEventReader created by this factory instance.
     * @param resolver the resolver to use to resolve references
     */
    public void  setXMLResolver(XMLResolver resolver) {
        config.setXMLResolver(resolver);
    }
    
    /**
     * The reporter that will be set on any XMLStreamReader or XMLEventReader created by this factory instance.
     */
    public XMLReporter getXMLReporter() {
        return config.getXMLReporter();
    }
    
    /**
     * The reporter that will be set on any XMLStreamReader or XMLEventReader created by this factory instance.
     * @param reporter the resolver to use to report non fatal errors
     */
    public void setXMLReporter(XMLReporter reporter) {
        config.setXMLReporter(reporter);
    }
    
    
    /**
     * Set a user defined event allocator for events
     * @param allocator the user defined allocator
     */
    public void setEventAllocator(XMLEventAllocator allocator) { 
        config.setEventAllocator(allocator);
    }
    
    /**
     * Gets the allocator used by streams created with this factory
     */
    public XMLEventAllocator getEventAllocator() {
        return config.getEventAllocator();
    }
    
    /**
     * Specifies that the stream produced by this code will append all adjacent text nodes. 
     */  
    public void setCoalescing(boolean coalescing){
        config.setCoalescing(coalescing);
    }
    
    /**
     * Indicates whether or not the factory is configured to produced streams that coalesce adjacent text nodes.
     */
    public boolean isCoalescing(){
        return config.isCoalescing();
    }
    
    public void setProperty(String name, Object value) throws IllegalArgumentException {
        // TODO - cwitt : check against supported feature list
        config.setProperty(name,value);
    }
    
    public Object getProperty(String name) throws IllegalArgumentException {
        return config.getProperty(name);
    }
    
    public boolean isPropertySupported(String name) {
        return config.isPropertySupported(name);
    }
    
    /**
     * @return Either the user supplied event allocator, or a default one.
     */
    private XMLEventAllocator createEventAllocator() {
        XMLEventAllocator userAlloc = config.getEventAllocator();
        return userAlloc == null ? fAllocator.newInstance() : userAlloc.newInstance();
    }
}
