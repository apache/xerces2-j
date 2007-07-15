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
        while (streamFilter != null && !streamFilter.accept(this))
        {
            next();
        }
        
        return next();
    }
    
    /**
     * Returns true if there are more parsing events and false if there are no
     * more events. This method will return false if the current state of the
     * XMLStreamReader is END_DOCUMENT
     * 
     * @return true if there are more events, false otherwise
     * @throws XMLStreamException
     *             if there is a fatal error detecting the next state
     */
    public boolean hasNext() throws XMLStreamException {
        boolean hasNext = hasNext();
      
        while (streamFilter != null && !streamFilter.accept(this))
        {
            next();
            hasNext = hasNext();
        }

        return hasNext;
    }
}
