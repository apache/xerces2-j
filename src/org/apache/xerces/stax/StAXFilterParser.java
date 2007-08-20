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

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

/**
 * @author Wei Duan
 * 
 * @version $Id$
 */
public class StAXFilterParser extends StreamReaderDelegate {
    
    // The stream fileter used to test whether current state is part of this stream
    private StreamFilter streamFilter = null;
       
    /**
     * The constructor for StAXFilterParser
     * 
     * @param streamReader
     * @param streamFilter
     */
    public StAXFilterParser(XMLStreamReader streamReader, StreamFilter streamFilter)
        throws XMLStreamException {
        super(streamReader);
        
        if (streamReader == null)
        {
            throw new XMLStreamException("The StreamReader parameter can't be null for StAXFilterParser");
        }
        this.streamFilter = streamFilter;
        next();
    }

    /**
     * Get next parsing event - a processor may return all contiguous character
     * data in a single chunk, or it may split it into several chunks. If the
     * property javax.xml.stream.isCoalescing is set to true element content
     * must be coalesced and only one CHARACTERS event must be returned for
     * contiguous element content or CDATA Sections. By default entity
     * references must be expanded and reported transparently to the
     * application. An exception will be thrown if an entity reference cannot be
     * expanded. If element content is empty (i.e. content is "") then no
     * CHARACTERS event will be reported.
     * 
     * @return the integer code corresponding to the current parse event
     * @throws java.lang.IllegalStateException -
     *             if this is called when hasNext() returns false
     * @throws XMLStreamException
     *             if there is an error processing the underlying XML source
     */
    public int next() throws XMLStreamException {
        while (super.hasNext())
        {
            int nextEvent = super.next();
            if (streamFilter.accept(this))
            {
                return nextEvent;
            }
        }
    
        throw new IllegalStateException("No more document to parse");
    }
    
    /**
     * Skips any white space (isWhiteSpace() returns true), COMMENT, or
     * PROCESSING_INSTRUCTION, until a START_ELEMENT or END_ELEMENT is reached.
     * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION,
     * START_ELEMENT, END_ELEMENT are encountered, an exception is thrown.
     * return eventType;
     * 
     * </pre>
     * 
     * @return the event type of the element read (START_ELEMENT or END_ELEMENT)
     * @throws XMLStreamException
     *             if the current event is not white space,
     *             PROCESSING_INSTRUCTION, START_ELEMENT or END_ELEMENT
     * @throws NoSuchElementException
     *             if this is called when hasNext() returns false
     */
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip
                // whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
                // skip whitespace
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag",
                    getLocation());
        }
        return eventType;
    }
}
