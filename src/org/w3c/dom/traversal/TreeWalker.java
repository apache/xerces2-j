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
 * <code>TreeWalker</code> objects are used to navigate a document tree or 
 * subtree using the view of the document defined by its 
 * <code>whatToShow</code> flags and any filters that are defined for the 
 * <code>TreeWalker</code>. Any function which performs navigation using a 
 * <code>TreeWalker</code> will automatically support any view defined by a 
 * <code>TreeWalker</code>.
 * <p>Omitting nodes from the logical view of a subtree can result in a 
 * structure that is substantially different from the same subtree in the 
 * complete, unfiltered document. Nodes that are siblings in the TreeWalker 
 * view may be children of different, widely separated nodes in the original 
 * view. For instance, consider a Filter that skips all nodes except for Text 
 * nodes and the root node of a document. In the logical view that results, 
 * all text nodes will be siblings and appear as direct children of the root 
 * node, no matter how deeply nested the structure of the original document.
 * @since DOM Level 2
 */
public interface TreeWalker {
    /**
     * This attribute determines which node types are presented via the 
     * TreeWalker. These constants are defined in the NodeFilter interface.
     */
    public int          getWhatToShow();
    /**
     * The filter used to screen nodes.
     */
    public NodeFilter   getFilter();
    /**
     * The value of this flag determines whether the children of entity 
     * reference nodes are visible to the TreeWalker. If false, they will be 
     * skipped over.
     * <br> To produce a view of the document that has entity references 
     * expanded and does not expose the entity reference node itself, use the 
     * whatToShow flags to hide the entity reference node and set 
     * expandEntityReferences to true when creating the TreeWalker. To 
     * produce a view of the document that has entity reference nodes but no 
     * entity expansion, use the whatToShow flags to show the entity 
     * reference node and set expandEntityReferences to false.
     */
    public boolean      getExpandEntityReferences();
    /**
     * The node at which the TreeWalker is currently positioned.
     * <br>The value must not be null. Alterations to the DOM tree may cause 
     * the current node to no longer be accepted by the TreeWalker's 
     * associated filter. currentNode may also be explicitly set to any node, 
     * whether or not it is within the subtree specified by the root node or 
     * would be accepted by the filter and whatToShow flags.  Further 
     * traversal occurs relative to currentNode even if it is not part of the 
     * current view by applying the filters in the requested direction (not 
     * changing currentNode where no traversal is possible). 
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the specified <code>currentNode</code> 
     *   is <code>null</code>.
     */
    public Node         getCurrentNode();
    public void         setCurrentNode(Node currentNode)
                                   throws DOMException;
    /**
     * Moves to and returns the closest visible ancestor node of the current 
     * node. If the search for parentNode attempts to step upward from the 
     * TreeWalker's root node, or if it fails to find a visible ancestor 
     * node, this method retains the current position and returns null.
     * @return The new parent node, or null if the current node has no parent 
     *   in the TreeWalker's logical view.
     */
    public Node         parentNode();
    /**
     * Moves the <code>TreeWalker</code> to the first child of the current 
     * node, and returns the new node. If the current node has no children, 
     * returns <code>null</code>, and retains the current node.
     * @return The new node, or <code>null</code> if the current node has no 
     *   children.
     */
    public Node         firstChild();
    /**
     * Moves the <code>TreeWalker</code> to the last child of the current 
     * node, and returns the new node. If the current node has no children, 
     * returns <code>null</code>, and retains the current node.
     * @return The new node, or <code>null</code> if the current node has no 
     *   children.
     */
    public Node         lastChild();
    /**
     * Moves the <code>TreeWalker</code> to the previous sibling of the 
     * current node, and returns the new node. If the current node has no 
     * previous sibling, returns <code>null</code>, and retains the current 
     * node.
     * @return The new node, or <code>null</code> if the current node has no 
     *   previous sibling.
     */
    public Node         previousSibling();
    /**
     * Moves the <code>TreeWalker</code> to the next sibling of the current 
     * node, and returns the new node. If the current node has no next 
     * sibling, returns <code>null</code>, and retains the current node.
     * @return The new node, or <code>null</code> if the current node has no 
     *   next sibling.
     */
    public Node         nextSibling();
    /**
     * Moves the <code>TreeWalker</code> to the previous visible node in 
     * document order relative to the current node, and returns the new node. 
     * If the current node has no previous node,  or if the search for 
     * previousNode attempts to step upward from  the TreeWalker's root node, 
     * returns <code>null</code>, and retains the current node. 
     * @return The new node, or <code>null</code> if the current node has no 
     *   previous node.
     */
    public Node         previousNode();
    /**
     * Moves the <code>TreeWalker</code> to the next visible node in document 
     * order relative to the current node, and returns the new node. If the 
     * current node has no next node,  or if the search for nextNode attempts 
     * to step upward from  the TreeWalker's root node, returns 
     * <code>null</code>, and retains the current node.
     * @return The new node, or <code>null</code> if the current node has no 
     *   next node.
     */
    public Node         nextNode();
}

