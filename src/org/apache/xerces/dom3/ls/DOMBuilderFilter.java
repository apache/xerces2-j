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

package org.apache.xerces.dom3.ls;

import org.w3c.dom.Node;

/**
 * <code>DOMBuilderFilter</code>s provide applications the ability to examine 
 * nodes as they are being constructed during a parse. As each node is 
 * examined, it may be modified or removed, or the entire parse may be 
 * terminated early. 
 * <p>At the time any of the filter methods are called by the parser, the 
 * owner Document and DOMImplementation objects exist and are accessible. 
 * The document element is never passed to the <code>DOMBuilderFilter</code> 
 * methods, i.e. it is not possible to filter out the document element.
 * <p>All validity checking while reading a document occurs on the source 
 * document as it appears on the input stream, not on the DOM document as it 
 * is built in memory. With filters, the document in memory may be a subset 
 * of the document on the stream, and its validity may have been affected by 
 * the filtering.The description of these methods is not complete
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-ASLS-20011025'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 */
public interface DOMBuilderFilter {
    /**
     * This method will be called by the parser after each <code>Element</code>
     *  start tag has been scanned, but before the remainder of the 
     * <code>Element</code> is processed. The intent is to allow the 
     * element, including any children, to be efficiently skipped. Note that 
     * only element nodes are passed to the <code>startNode</code> function.
     * <br>The element node passed to <code>startNode</code> for filtering 
     * will include all of the Element's attributes, but none of the 
     * children nodes. The Element may not yet be in place in the document 
     * being constructed (it may not have a parent node.) 
     * <br>A <code>startNode</code> filter function may access or change the 
     * attributers for the Element. Changing Namespace declarations will 
     * have no effect on namespace resolution by the parser.
     * <br>For efficiency, the Element node passed to the filter may not be 
     * the same one as is actually placed in the tree if the node is 
     * accepted. And the actual node (node object identity) may be reused 
     * during the process of reading in and filtering a document.
     * @param snode The newly encountered element. At the time this method is 
     *   called, the element is incomplete - it will have its attributes, 
     *   but no children. Should the parameter be an Element since we only 
     *   passed elements to startNode?
     * @return  <code>ACCEPT</code> if this <code>Element</code> should be 
     *   included in the DOM document being built.  <code>REJECT</code> if 
     *   the <code>Element</code> and all of its children should be 
     *   rejected.  <code>SKIP</code> if the <code>Element</code> should be 
     *   rejected. All of its children are inserted in place of the rejected 
     *   <code>Element</code> node. 
     */
    public int startNode(Node snode);

    /**
     * This method will be called by the parser at the completion of the parse 
     * of each node. The node will exist and be complete, as will all of its 
     * children, and their children, recursively. The parent node will also 
     * exist, although that node may be incomplete, as it may have 
     * additional children that have not yet been parsed. Attribute nodes 
     * are never passed to this function.
     * <br>From within this method, the new node may be freely modified - 
     * children may be added or removed, text nodes modified, etc. This node 
     * may also be removed from its parent node, which will prevent it from 
     * appearing in the final document at the completion of the parse. Aside 
     * from this one operation on the node's parent, the state of the rest 
     * of the document outside of this node is not defined, and the affect 
     * of any attempt to navigate to or modify any other part of the 
     * document is undefined. 
     * <br>For validating parsers, the checks are made on the original 
     * document, before any modification by the filter. No validity checks 
     * are made on any document modifications made by the filter. 
     * @param enode The newly constructed element. At the time this method is 
     *   called, the element is complete - it has all of its children (and 
     *   their children, recursively) and attributes, and is attached as a 
     *   child to its parent. 
     * @return  <code>ACCEPT</code> if this <code>Node</code> should be 
     *   included in the DOM document being built.  <code>REJECT</code> if 
     *   the <code>Node</code> and all of its children should be rejected. 
     */
    public int endNode(Node enode);

    /**
     *  Tells the <code>DOMBuilder</code> what types of nodes to show to the 
     * filter. See <code>NodeFilter</code> for definition of the constants. 
     * The constant <code>SHOW_ATTRIBUTE</code> is meaningless here, 
     * attribute nodes will never be passed to a 
     * <code>DOMBuilderFilter</code>. 
     */
    public int getWhatToShow();

}
