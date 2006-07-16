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
            SAXXMLStreamReaderImpl sxsReader = new SAXXMLStreamReaderImpl(xr, is, this);
            if(is == null || xr == null)
                throw new XMLStreamException(
                "Can only create StAX reader for a SAXSource if Reader and InputStream exposed by getSource()");
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
        return null;
    }

    public XMLEventReader createXMLEventReader(String systemId, Reader reader)
            throws XMLStreamException {
        return null;
    }

    public XMLEventReader createXMLEventReader(XMLStreamReader reader)
            throws XMLStreamException {
        return null;
    }
    
    public XMLEventReader createXMLEventReader(Source source)
            throws XMLStreamException {
        return null;
    }

    public XMLEventReader createXMLEventReader(InputStream stream)
            throws XMLStreamException {
        return null;
    }
    
    public XMLEventReader createXMLEventReader(InputStream stream,
            String encoding) throws XMLStreamException {
        return null;
    }

    public XMLEventReader createXMLEventReader(String systemId,
            InputStream stream) throws XMLStreamException {
        return null;
    }

    public XMLStreamReader createFilteredReader(XMLStreamReader reader,
            StreamFilter filter) throws XMLStreamException {
        return null;
    }
    
    public XMLEventReader createFilteredReader(XMLEventReader reader,
            EventFilter filter) throws XMLStreamException {
        return null;
    }

    public XMLResolver getXMLResolver() {
        return null;
    }
    
    public void setXMLResolver(XMLResolver resolver) {
    }
    
    public XMLReporter getXMLReporter() {
        return null;
    }

    public void setXMLReporter(XMLReporter reporter) {
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

    public void setEventAllocator(XMLEventAllocator allocator) {
    }

    public XMLEventAllocator getEventAllocator() {
        return null;
    }
}
