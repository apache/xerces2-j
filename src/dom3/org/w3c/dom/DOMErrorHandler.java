/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.dom;

/** 
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * 
 * <code>DOMErrorHandler</code> is a callback interface that the DOM 
 * implementation can call when reporting errors that happens while 
 * processing XML data, or when doing some other processing (e.g. validating 
 * a document).
 * <p>The application that is using the DOM implementation is expected to 
 * implement this interface.How does one register an error handler in the 
 * core? Passed as an argument to super-duper-normalize or registered on the 
 * DOMImplementation?Document interface has an attribute errorHandler.
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
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
