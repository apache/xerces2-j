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

package org.apache.xerces.dom3;

/**
 * <code>DOMErrorHandler</code> is a callback interface that the DOM 
 * implementation can call when reporting errors that happens while 
 * processing XML data, or when doing some other processing (e.g. validating 
 * a document).
 * <p>The application that is using the DOM implementation is expected to 
 * implement this interface.How does one register an error handler in the 
 * core? Passed as an argument to super-duper-normalize or registered on the 
 * DOMImplementation?
 * <p>See also the <a href='http://www.w3.org/2001/09/WD-DOM-Level-3-Core-20010919'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMErrorHandler {
    /**
     * This method is called on the error handler when an error occures.
     * @param error The error object that describes the error, this object 
     *   may be reused by the DOM implementation across multiple calls to 
     *   the handleEvent method.
     * @return If the handleError method returns <code>true</code> the DOM 
     *   implementation should continue as if the error didn't happen when 
     *   possible, if the method returns <code>false</code> then the DOM 
     *   implementation should stop the current processing when possible.
     */
    public boolean handleError(DOMError error);

}
