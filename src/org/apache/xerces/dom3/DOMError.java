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
 * <code>DOMError</code> is an interface that describes an error.
 * <p>See also the <a href='http://www.w3.org/2001/09/WD-DOM-Level-3-Core-20010919'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMError {
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * warning
     */
    public static final short SEVERITY_WARNING          = 0;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * error
     */
    public static final short SEVERITY_ERROR            = 1;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * fatal error
     */
    public static final short SEVERITY_FATAL_ERROR      = 2;
    /**
     * The severity of the error, either <code>SEVERITY_WARNING</code>, 
     * <code>SEVERITY_ERROR</code>, or <code>SEVERITY_FATAL_ERROR</code>.
     */
    public short getSeverity();

    /**
     * An implementation specific string describing the error that occured.
     */
    public String getMessage();

    /**
     * The byte or character offset into the input source, if we're parsing a 
     * file or a byte stream then this will be the byte offset into that 
     * stream, but if a character media is parsed then the offset will be 
     * the character offset.exception is a reserved word, we need to rename 
     * it.
     */
    public Object getException();

    /**
     * The location of the error.
     */
    public DOMLocator getLocation();

}
