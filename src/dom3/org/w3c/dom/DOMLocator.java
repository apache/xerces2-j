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
 * <code>DOMLocator</code> is an interface that describes a location (e.g. 
 * where an error occured).
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
 */
public interface DOMLocator {
    /**
     * The line number this locator is pointing to, or <code>-1</code> if 
     * there is no column number available.
     */
    public int getLineNumber();

    /**
     * The column number this locator is pointing to, or <code>-1</code> if 
     * there is no column number available.
     */
    public int getColumnNumber();

    /**
     * The byte or character offset into the input source this locator is 
     * pointing to. If the input source is a file or a byte stream then this 
     * is the byte offset into that stream, but if the input source is a 
     * character media then the offset is the character offset. The value is 
     * <code>-1</code> if there is no offset available.
     */
    public int getOffset();

    /**
     * The node this locator is pointing to, or <code>null</code> if no node 
     * is available.
     */
    public Node getRelatedNode();

    /**
     * The URI this locator is pointing to, or <code>null</code> if no URI is 
     * available.
     */
    public String getUri();

}
