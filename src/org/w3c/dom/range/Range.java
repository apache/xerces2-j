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

package org.w3c.dom.range;

import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 */
public interface Range {
    /**
     * Node within which the range begins 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public Node         getStartContainer()
                                         throws DOMException;
    /**
     * Offset within the starting node of the range. 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public int          getStartOffset()
                                         throws DOMException;
    /**
     * Node within which the range ends 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public Node         getEndContainer()
                                         throws DOMException;
    /**
     * Offset within the ending node of the range. 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public int          getEndOffset()
                                         throws DOMException;
    /**
     * TRUE if the range is collapsed 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public boolean      getIsCollapsed()
                                         throws DOMException;
    /**
     * The common ancestor container of the range's two boundary-points.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public Node         getCommonAncestorContainer()
                                         throws DOMException;
    /**
     * Sets the attributes describing the start of therange. 
     * @param refNode The <code>refNode</code> value. Thisparameter must be 
     *   different from <code>null</code>.
     * @param offset The <code>startOffset</code> value. 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if<code>refNode</code> or an ancestor 
     *   of <code>refNode</code> is anAttr, Entity, Notation, or DocumentType 
     *   node.
     * @exception DOMException
     *   INDEX_SIZE_ERR: Raised if <code>offset</code> is negative or greater 
     *   than the number of child units in <code>refNode</code>. Child units 
     *   are 16-bit units if <code>refNode</code> is a CharacterData, Comment 
     *   or ProcessingInstruction node. Child units are Nodes in all other 
     *   cases.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public void         setStart(Node refNode, 
                                 int offset)
                                 throws RangeException, DOMException;
    /**
     * Sets the attributes describing the end of a range.
     * @param refNode The <code>refNode</code> value. Thisparameter must be 
     *   different from <code>null</code>.
     * @param offset The <code>endOffset</code> value. 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if<code>refNode</code> or an ancestor 
     *   of <code>refNode</code> is anAttr, Entity, Notation, or DocumentType 
     *   node.
     * @exception DOMException
     *   INDEX_SIZE_ERR: Raised if <code>offset</code> is negative or greater 
     *   than the number of child units in <code>refNode</code>. Child units 
     *   are 16-bit units if <code>refNode</code> is a CharacterData, Comment 
     *   or ProcessingInstruction node. Child units are Nodes in all other 
     *   cases.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public void         setEnd(Node refNode, 
                               int offset)
                               throws RangeException, DOMException;
    /**
     * Sets the start position to be before a node
     * @param refNode Range starts before <code>refNode</code> 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if an ancestorof <code>refNode</code> 
     *   is an Attr, Entity,Notation, or DocumentType node or if 
     *   <code>refNode</code> is a Document,DocumentFragment, Attr, Entity, 
     *   or Notation node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         setStartBefore(Node refNode)
                                       throws RangeException, DOMException;
    /**
     * Sets the start position to be after a node
     * @param refNode Range starts after <code>refNode</code> 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if an ancestorof <code>refNode</code> 
     *   is an Attr, Entity,Notation, or DocumentType node or if 
     *   <code>refNode</code> is a Document,DocumentFragment, Attr, Entity, 
     *   or Notation node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         setStartAfter(Node refNode)
                                      throws RangeException, DOMException;
    /**
     * Sets the end position to be before a node. 
     * @param refNode Range ends before <code>refNode</code> 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if an ancestorof <code>refNode</code> 
     *   is an Attr, Entity,Notation, or DocumentType node or if 
     *   <code>refNode</code> is a Document,DocumentFragment, Attr, Entity, 
     *   or Notation node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         setEndBefore(Node refNode)
                                     throws RangeException, DOMException;
    /**
     * Sets the end of a range to be after a node 
     * @param refNode Range ends after <code>refNode</code>. 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if an ancestorof <code>refNode</code> 
     *   is an Attr, Entity,Notation or DocumentType node or if 
     *   <code>refNode</code> is a Document,DocumentFragment, Attr, Entity, 
     *   or Notation node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         setEndAfter(Node refNode)
                                    throws RangeException, DOMException;
    /**
     * Collapse a range onto one of its boundary-points 
     * @param toStart If TRUE, collapses the Range onto its start;if FALSE, 
     *   collapses it onto its end. 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         collapse(boolean toStart)
                                 throws DOMException;
    /**
     * Select a node and its contents 
     * @param refNode The node to select. 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if an ancestorof <code>refNode</code> 
     *   is an Attr, Entity,Notation or DocumentType node or if 
     *   <code>refNode</code> is a Document,DocumentFragment, Attr, Entity, 
     *   or Notation node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         selectNode(Node refNode)
                                   throws RangeException, DOMException;
    /**
     * Select the contents within a node 
     * @param refNode Node to select from 
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if<code>refNode</code> or an ancestor 
     *   of <code>refNode</code> is anAttr, Entity, Notation or DocumentType 
     *   node.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         selectNodeContents(Node refNode)
                                           throws RangeException, DOMException;

    public static final int StartToStart = 1;
    public static final int StartToEnd   = 2;
    public static final int EndToEnd     = 3;
    public static final int EndToStart   = 4;


    /**
     * Compare the boundary-points of two ranges in a document.
     * @param how 
     * @param sourceRange 
     * @return  -1, 0 or 1 depending on whether the correspondingboundary-point
     *    of the Range is before, equal to, or after thecorresponding 
     *   boundary-point of <code>sourceRange</code>. 
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if the two Rangesare not in the same 
     *   document or document fragment.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public short        compareBoundaryPoints(int how, 
                                              Range sourceRange)
                                              throws DOMException;
    /**
     * Removes the contents of a range from the containingdocument or document 
     * fragment without returning a reference to theremoved content.  
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if anyportion of the content of 
     *   the range is read-only or anyof the nodes that contain any of the 
     *   content of the range areread-only.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public void         deleteContents()
                                       throws DOMException;
    /**
     * Moves the contents of a range from the containingdocument or document 
     * fragment to a new DocumentFragment. 
     * @return A DocumentFragment containing the extractedcontents. 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if anyportion of the content of 
     *   the range is read-only or anyof the nodes which contain any of the 
     *   content of the range are read-only.
     *   <br>HIERARCHY_REQUEST_ERR: Raised if aDocumentType node would be 
     *   extracted into the newDocumentFragment.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public DocumentFragment extractContents()
                                            throws DOMException;
    /**
     * Duplicates the contents of a range 
     * @return A DocumentFragment containing contents equivalentto those of 
     *   this range. 
     * @exception DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if aDocumentType node would be 
     *   extracted into the newDocumentFragment.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     */
    public DocumentFragment cloneContents()
                                          throws DOMException;
    /**
     * Inserts a node into the document or document fragmentat the start of 
     * the range. 
     * @param newNode The node to insert at the start of therange 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if an ancestor container of the 
     *   start of the range is read-only.
     *   <br>WRONG_DOCUMENT_ERR: Raised if<code>newNode</code> and the 
     *   container of the start of the Range were not created from the same 
     *   document.
     *   <br>HIERARCHY_REQUEST_ERR: Raised if the container of the start of 
     *   the Range is of a type that does not allow children ofthe type of 
     *   <code>newNode</code> or if <code>newNode</code> is an ancestor of the
     *   container.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     * @exception RangeException
     *   INVALID_NODE_TYPE_ERR: Raised if<code>node</code> is an Attr, 
     *   Entity, Notation,DocumentFragment, or Document node.
     */
    public void         insertNode(Node newNode)
                                   throws DOMException, RangeException;
    /**
     * Reparents the contents of the range to the given nodeand inserts the 
     * node at the position of the start of therange. 
     * @param newParent The node to surround the contents with. 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if an ancestor container of 
     *   either boundary-point of the range is read-only.
     *   <br>WRONG_DOCUMENT_ERR: Raised if<code>newParent</code> and the 
     *   container of the start of the Range were not created from the same 
     *   document.
     *   <br>HIERARCHY_REQUEST_ERR: Raised if the container of the start of 
     *   the Range is of a type that does not allow children ofthe type of 
     *   <code>newParent</code> or if <code>newParent</code> is an ancestor 
     *   of thecontaineror if <code>node</code> would end up with a child 
     *   node of a type not allowedby the type of <code>node</code>.
     *   <br>INVALID_STATE_ERR: Raised if <code>detach()</code> has already 
     *   been invoked on this object.
     * @exception RangeException
     *   BAD_BOUNDARYPOINTS_ERR: Raised if the range partially selects a 
     *   non-text node.
     *   <br>INVALID_NODE_TYPE_ERR: Raised if<code>node</code> is an Attr, 
     *   Entity, DocumentType, Notation,Document, or DocumentFragment node.
     */
    public void         surroundContents(Node newParent)
                                         throws DOMException, RangeException;
    /**
     * Produces a new range whose boundary-points are equal tothe 
     * boundary-points of the range. 
     * @return The duplicated range. 
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public Range        cloneRange()
                                   throws DOMException;
    /**
     * Returns the contents of a range as a string. 
     * @return The contents of the range.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public String       toString()
                                 throws DOMException;
    /**
     * Called to indicate that the range is no  longer in use and that the 
     * implementation may relinquish any resources associated with this 
     * range. Subsequent calls to any methods or attribute getters on this 
     * range will result in a <code>DOMException</code> being thrown with an 
     * error code of  <code>INVALID_STATE_ERR</code>.
     * @exception DOMException
     *   INVALID_STATE_ERR: Raised if <code>detach()</code> has already been 
     *   invoked on this object.
     */
    public void         detach()
                               throws DOMException;
}

