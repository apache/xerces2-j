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
 * <code>DocumentTraversal</code> contains methods that create iterators and 
 * tree-walkers to traverse a node and its children in document order (depth 
 * first, pre-order traversal, which is equivalent to the order in which the 
 * start tags occur in the text representation of the document).
 * @since DOM Level 2
 */
public interface DocumentTraversal {
    /**
     * Create a new NodeIterator over the subtree rooted at the specified node.
     * @param root The node which will be iterated together with its children. 
     *   The iterator is initially positioned just before this node. The 
     *   whatToShow flags and the filter, if any, are not considered when 
     *   setting this position.
     * @param whatToShow This flag specifies which node types may appear in the
     *    logical view of the tree presented by the iterator. See the 
     *   description of iterator for the set of possible values.These flags 
     *   can be combined using <code>OR</code>.
     * @param filter The Filter to be used with this TreeWalker, or null to 
     *   indicate no filter.
     * @param entityReferenceExpansion The value of this flag determines 
     *   whether entity reference nodes are expanded.
     * @return The newly created <code>NodeIterator</code>.
     */
    public NodeIterator createNodeIterator(Node root, 
                                           int whatToShow, 
                                           NodeFilter filter, 
                                           boolean entityReferenceExpansion);
    /**
     * Create a new TreeWalker over the subtree rooted at the specified node.
     * @param root The node which will serve as the root for the 
     *   <code>TreeWalker</code>. The whatToShow flags and the NodeFilter are 
     *   not considered when setting this value; any node type will be 
     *   accepted as the root. The currentNode of the TreeWalker is 
     *   initialized to this node, whether or not it is visible.  The root 
     *   functions as a stopping point for traversal methods that look upward 
     *   in the document structure, such as parentNode and nextNode. The root 
     *   must not be null.
     * @param whatToShow This flag specifies which node types may appear in the
     *    logical view of the tree presented by the iterator. See the 
     *   description of TreeWalker for the set of possible values.These flags 
     *   can be combined using <code>OR</code>.
     * @param filter The Filter to be used with this TreeWalker, or null to 
     *   indicate no filter.
     * @param entityReferenceExpansion The value of this flag determines 
     *   whether entity reference nodes are expanded.
     * @return The newly created <code>TreeWalker</code>.
     * @exception DOMException
     *    Raises the exception NOT_SUPPORTED_ERR if the specified root node 
     *   is null.
     */
    public TreeWalker   createTreeWalker(Node root, 
                                         int whatToShow, 
                                         NodeFilter filter, 
                                         boolean entityReferenceExpansion)
                                         throws DOMException;
}

