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

package org.w3c.dom.ls;

import org.w3c.dom.traversal.NodeFilter;

/**
 * DOM Level 3 WD - Experimental.
 *  <code>DOMWriterFilter</code>s provide applications the ability to examine 
 * nodes as they are being serialized. <code>DOMWriterFilter</code> lets the 
 * application decide what nodes should be serialized or not. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-ASLS-20020409'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 */
public interface DOMWriterFilter extends NodeFilter {
    /**
     *  Tells the <code>DOMWriter</code> what types of nodes to show to the 
     * filter. See <code>NodeFilter</code> for definition of the constants. 
     * The constant <code>SHOW_ATTRIBUTE</code> is meaningless here, 
     * attribute nodes will never be passed to a <code>DOMWriterFilter</code>
     * . 
     */
    public int getWhatToShow();

}
