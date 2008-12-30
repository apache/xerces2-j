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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public class XMLEventReaderImpl implements XMLEventReader {

    private final XMLStreamReader fReader;

    /**
     * There is an asymmetry between XMLStreamReader and XMLEventReader.
     * The event reader starts in a non-state, prior to the START_DOCUMENT, like
     * a normal iterator. The stream reader starts already in the START_DOCUMENT
     * state. So we need to treat the first event slightly differently (in effect
     * like a peek) 
     */
    private boolean fPreStart = true;
    
    /**
     * If a peek has been done, this event will
     * be non-null, and subsequent peeks should
     * return it
     */
    private XMLEvent fPeekedEvent = null;
    
    /**
     * If a peek has been done, this will be the 
     * event we were on before the peek. This is important
     * for methods that are sensitive to "current" state, like
     * getElementText
     */
    private XMLEvent fPrePeekEvent = null;

    /**
     * The event allocator to use to make events out of the stream. 
     */
    private final XMLEventAllocator fAllocator;

    /**
     * Construct based on XMLStreamReader. 
     */
    public XMLEventReaderImpl(final XMLEventAllocator allocator, final XMLStreamReader reader) {
        fAllocator = allocator;
        fReader = reader;
    }

    /**
     * @see javax.xml.stream.XMLEventReader#nextEvent()
     */
    public XMLEvent nextEvent() throws XMLStreamException {
        populatePreStartEvent();
        XMLEvent next = null;
        if (fPeekedEvent != null) {
            next = fPeekedEvent;
            clearEventCache();
        }
        else {
            fReader.next();
            next = makeEvent();
        }
        return next;
    }

    /**
     * @see javax.xml.stream.XMLEventReader#hasNext()
     */
    public boolean hasNext() {
        if (fPeekedEvent != null || fPreStart) {
            return true;
        }
        
        try {    
            return fReader.hasNext();
        }
        catch (XMLStreamException e) {
            //Assuming that the only meaningful thing
            //to do here is to return false. 
        }
        return false;
    }

    /**
     * @see javax.xml.stream.XMLEventReader#peek()
     */
    public XMLEvent peek() throws XMLStreamException {
        populatePreStartEvent();
        if (fPeekedEvent == null) {
            try {
                if(fReader.hasNext()){
                    fReader.next();
                    fPeekedEvent = makeEvent();
                }
            }
            catch(NoSuchElementException e) {
                //Just leave it as null, which is the desired behaviour
            }
            
        }
        return fPeekedEvent; 
    }

    /**
     * @throws XMLStreamException
     */
    private void populatePreStartEvent() throws XMLStreamException {
        if (fPreStart) {
            fPeekedEvent = makeEvent();
            
            //Not strictly meaningful, but simplifies code elsewhere.
            fPrePeekEvent = fPeekedEvent;
            fPreStart = false;
        }
    }

    /**
     * @see javax.xml.stream.XMLEventReader#getElementText()
     */
    public String getElementText() throws XMLStreamException {
        fPreStart = false;
        if (fPeekedEvent == null) {
            return fReader.getElementText();
        }
        else if (!fPrePeekEvent.isStartElement()) {
            throw new XMLStreamException("getElementText called when not on a START_ELEMENT event", fPrePeekEvent.getLocation());
        }
        else if (!(fPeekedEvent.isCharacters() || fPeekedEvent.isProcessingInstruction() || fPeekedEvent instanceof Comment)) {
            throw new XMLStreamException("Unexpected content encountered while trying to getElementText", fPeekedEvent.getLocation());
        }
        else {
            StringBuffer buffer = new StringBuffer();
            if (fPeekedEvent.isCharacters()) {
                buffer.append(((Characters) fPeekedEvent).getData());
            }
            clearEventCache();
            for (XMLEvent event = nextEvent(); !event.isEndElement(); event = nextEvent()){
                if (event.isCharacters()) {
                    buffer.append(((Characters) fPeekedEvent).getData());
                }
                else if (!(event.isProcessingInstruction() || event.isProcessingInstruction() || event instanceof Comment)) {
                    throw new XMLStreamException("Unexpected content encountered while trying to getElementText", fPeekedEvent.getLocation());
                }
            }
            return buffer.toString();
        }
    }

    /**
     * @see javax.xml.stream.XMLEventReader#nextTag()
     */
    public XMLEvent nextTag() throws XMLStreamException {
        populatePreStartEvent();
        if (fPeekedEvent != null) {
            if (!(fPeekedEvent.isStartElement() 
                    || fPeekedEvent.isEndElement()
                    || fPeekedEvent.isStartDocument()
                    || (fPeekedEvent.isCharacters() && ((Characters)fPeekedEvent).isWhiteSpace()))) {
                throw new XMLStreamException("nextTag when next event was not an element event.", fReader.getLocation());
            }
            //nextTag moves to the next startElement or endElement; if we've already peeked
            //one of these there's no need to advance to the nextTag now.
            else if(!fPeekedEvent.isStartElement() && !fPeekedEvent.isEndElement()) {
                fReader.nextTag();
            }
            clearEventCache();
        }
        else {
            fReader.nextTag();
        }
        return makeEvent();
    }

    /**
     * @see javax.xml.stream.XMLEventReader#getProperty(java.lang.String)
     */
    public Object getProperty(String name) throws IllegalArgumentException {
        return fReader.getProperty(name);
    }

    /**
     * @see javax.xml.stream.XMLEventReader#close()
     */
    public void close() throws XMLStreamException {
        fReader.close();
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
        
        try {
            return nextEvent();
        }
        catch (XMLStreamException ex) {
            //Not sure what else to do in this case...
            throw new IllegalStateException(ex.toString());
        }
        
    }

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException("XMLEventReader does not support remove()");
    }

    /**
     * @return A new event object for the current event in the stream.
     */
    private XMLEvent makeEvent() throws XMLStreamException {
        return fAllocator.allocate(fReader);
    }
    
    /**
     * Clears the cached next event used for peek();
     */
    private void clearEventCache() {
        fPeekedEvent = null;
        fPrePeekEvent = null;
    }
    
}
