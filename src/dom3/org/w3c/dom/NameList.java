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
 *  The <code>NameList</code> interface provides the abstraction of an ordered 
 * collection of parallel pairs of name and namespace values, without 
 * defining or constraining how this collection is implemented. The items in 
 * the <code>NameList</code> are accessible via an integral index, starting 
 * from 0. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
 */
public interface NameList {
    /**
     *  Returns the <code>index</code>th name item in the collection. 
     * @param index Index into the collection.
     * @return  The <code>DOMString</code> at the <code>index</code>th 
     *   position in the <code>NameList</code>. 
     * @exception DOMException
     *    INDEX_SIZE_ERR: If <code>index</code> is greater than or equal to 
     *   the number of nodes in the list. 
     */
    public String getName(int index)
                          throws DOMException;

    /**
     *  Returns the <code>index</code>th namespaceURI item in the collection. 
     * @param index Index into the collection.
     * @return  The <code>DOMString</code> at the <code>index</code>th 
     *   position in the <code>NameList</code>. 
     * @exception DOMException
     *    INDEX_SIZE_ERR: If <code>index</code> is greater than or equal to 
     *   the number of nodes in the list. 
     */
    public String getNamespaceURI(int index)
                                  throws DOMException;

    /**
     *  The number of pairs (name and namespaceURI) in the list. The range of 
     * valid child node indices is 0 to <code>length-1</code> inclusive.
     */
    public int getLength();

}
