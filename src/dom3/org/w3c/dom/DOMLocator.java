/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom;

/**
 * <code>DOMLocator</code> is an interface that describes a location (e.g. 
 * where an error occured).
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMLocator {
    /**
     * The line number where the error occured, or -1 if there is no line 
     * number available.
     */
    public int getLineNumber();

    /**
     * The column number where the error occured, or -1 if there is no column 
     * number available.
     */
    public int getColumnNumber();

    /**
     * The byte or character offset into the input source, if we're parsing a 
     * file or a byte stream then this will be the byte offset into that 
     * stream, but if a character media is parsed then the offset will be 
     * the character offset. The value is <code>-1</code> if there is no 
     * offset available.
     */
    public int getOffset();

    /**
     * The DOM Node where the error occured, or null if there is no Node 
     * available.
     */
    public Node getErrorNode();

    /**
     * The URI where the error occured, or null if there is no URI available.
     */
    public String getUri();

}
