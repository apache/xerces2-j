/*
 * Copyright (c) 2001 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.apache.xerces.dom3.ls;

import org.w3c.dom.events.Event;

/**
 * This interface represents a progress event object that notifies the 
 * application about progress as a document is parsed. This event is 
 * optional and the rate at which this event is fired is implementation 
 * dependent.
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-ASLS-20011025'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 */
public interface LSProgressEvent extends Event {
    /**
     * The input source that is being parsed.
     */
    public DOMInputSource getInputSource();

    /**
     * The current position in the input source, including all external 
     * entities and other resources that have been read.
     */
    public int getPosition();

    /**
     * The total size of the document including all external resources, this 
     * number might change as a document is being parsed if references to 
     * more external resources are seen.
     */
    public int getTotalSize();

}
