/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
 */

package org.w3c.dom.traversal;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * NodeIterators are used to step through a set of nodes, e.g. the set of 
 * nodes in a NodeList, the document subtree governed by a particular node, 
 * the results of a query, or any other set of nodes. The set of nodes to be 
 * iterated is determined by the implementation of the NodeIterator. DOM 
 * Level 2 specifies a single NodeIterator implementation for document-order 
 * traversal of a document subtree. Instances of these iterators are created 
 * by calling DocumentTraversal.createNodeIterator().
 * @since DOM Level 2
 */
public interface NodeIterator {
    /**
     * This attribute determines which node types are presented via the 
     * iterator. The available set of constants is defined in the Filters 
     * interface.
     */
    public int          getWhatToShow();
    /**
     * The filter used to screen nodes.
     */
    public NodeFilter   getFilter();
    /**
     *  The value of this flag determines whether the children of entity 
     * reference nodes are visible to the iterator. If false, they will be 
     * skipped over.
     * <br> To produce a view of the document that has entity references 
     * expanded and does not expose the entity reference node itself, use the 
     * whatToShow flags to hide the entity reference node and set 
     * expandEntityReferences to true when creating the iterator. To produce 
     * a view of the document that has entity reference nodes but no entity 
     * expansion, use the whatToShow flags to show the entity reference node 
     * and set expandEntityReferences to false.
     */
    public boolean      getExpandEntityReferences();
    /**
     * Returns the next node in the set and advances the position of the 
     * iterator in the set. After a NodeIterator is created, the first call 
     * to nextNode() returns the first node in the set.
     * @return The next <code>Node</code> in the set being iterated over, or 
     *   <code>null</code> if there are no more members in that set.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if this method is called after the 
     *   <code>detach</code> method was invoked.
     */
    public Node         nextNode()
                                 throws DOMException;
    /**
     * Returns the previous node in the set and moves the position of the 
     * iterator backwards in the set.
     * @return The previous <code>Node</code> in the set being iterated over, 
     *   or <code>null</code> if there are no more members in that set. 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if this method is called after the 
     *   <code>detach</code> method was invoked.
     */
    public Node         previousNode()
                                     throws DOMException;
    /**
     * Detaches the iterator from the set which it iterated over, releasing 
     * any computational resources and placing the iterator in the INVALID 
     * state. After <code>detach</code> has been invoked, calls to 
     * <code>nextNode</code> or <code>previousNode</code> will raise the 
     * exception INVALID_STATE_ERR.
     */
    public void         detach();
}

