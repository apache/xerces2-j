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

//package org.w3c.dom;
package org.apache.xerces.dom3;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * The <code>Node3</code> interface is an extension to the DOM Level 2
 * <code>Node</code> interface containing the DOM Level 3 additions.
 * <p>See also the <a href='http://www.w3.org/2001/10/WD-DOM-Level-3-Core-20011017'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Node3 extends Node {
    /**
     * The absolute base URI of this node or <code>null</code> if undefined. 
     * This value is computed according to . However, when the 
     * <code>Document</code> supports the feature "HTML" , the base URI is 
     * computed using first the value of the href attribute of the HTML BASE 
     * element if any, and the value of the <code>documentURI</code> 
     * attribute from the <code>Document</code> interface otherwise.
     * <br> When the node is an <code>Element</code>, a <code>Document</code> 
     * or a a <code>ProcessingInstruction</code>, this attribute represents 
     * the properties [base URI] defined in . When the node is a 
     * <code>Notation</code>, an <code>Entity</code>, or an 
     * <code>EntityReference</code>, this attribute represents the 
     * properties [declaration base URI] in the . How will this be affected 
     * by resolution of relative namespace URIs issue?It's not.Should this 
     * only be on Document, Element, ProcessingInstruction, Entity, and 
     * Notation nodes, according to the infoset? If not, what is it equal to 
     * on other nodes? Null? An empty string? I think it should be the 
     * parent's.No.Should this be read-only and computed or and actual 
     * read-write attribute?Read-only and computed (F2F 19 Jun 2000 and 
     * teleconference 30 May 2001).If the base HTML element is not yet 
     * attached to a document, does the insert change the Document.baseURI?
     * Yes. (F2F 26 Sep 2001)
     * @since DOM Level 3
     */
    public String getBaseURI();

    // TreePosition
    /**
     * The node precedes the reference node.
     */
    public static final short TREE_POSITION_PRECEDING   = 0x01;
    /**
     * The node follows the reference node.
     */
    public static final short TREE_POSITION_FOLLOWING   = 0x02;
    /**
     * The node is an ancestor of the reference node.
     */
    public static final short TREE_POSITION_ANCESTOR    = 0x04;
    /**
     * The node is a descendant of the reference node.
     */
    public static final short TREE_POSITION_DESCENDANT  = 0x08;
    /**
     * The two nodes have an equivalent position. This is the case of two 
     * attributes that have the same <code>ownerElement</code>, and two 
     * nodes that are the same.
     */
    public static final short TREE_POSITION_EQUIVALENT  = 0x10;
    /**
     * The two nodes are the same. Two nodes that are the same have an 
     * equivalent position, though the reverse may not be true.
     */
    public static final short TREE_POSITION_SAME_NODE   = 0x20;
    /**
     * The two nodes are disconnected, they do not have any common ancestor. 
     * This is the case of two nodes that are not in the same document.
     */
    public static final short TREE_POSITION_DISCONNECTED = 0x00;

    /**
     * Compares a node with this node with regard to their position in the 
     * tree and according to the document order. This order can be extended 
     * by module that define additional types of nodes.Should this method be 
     * optional?No.Need reference for namespace nodes.No, instead avoid 
     * referencing them directly.
     * @param other The node to compare against this node.
     * @return Returns how the given node is positioned relatively to this 
     *   node.
     * @since DOM Level 3
     */
    public short compareTreePosition(Node other);

    /**
     * This attribute returns the text content of this node and its 
     * descendants. When it is defined to be null, setting it has no effect. 
     * When set, any possible children this node may have are removed and 
     * replaced by a single <code>Text</code> node containing the string 
     * this attribute is set to. On getting, no serialization is performed, 
     * the returned string does not contain any markup. No whitespace 
     * normalization is performed, the returned string does not contain the 
     * element content whitespaces . Similarly, on setting, no parsing is 
     * performed either, the input string is taken as pure textual content.
     * <br>The string returned is made of the text content of this node 
     * depending on its type, as defined below: 
     * <table border='1'>
     * <tr>
     * <th>Node type</th>
     * <th>Content</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     * ELEMENT_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE, 
     * DOCUMENT_FRAGMENT_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>concatenation of the <code>textContent</code> 
     * attribute value of every child node, excluding COMMENT_NODE and 
     * PROCESSING_INSTRUCTION_NODE nodes</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>ATTRIBUTE_NODE, TEXT_NODE, 
     * CDATA_SECTION_NODE, COMMENT_NODE, PROCESSING_INSTRUCTION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>
     * <code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>
     * null</td>
     * </tr>
     * </table> Should any whitespace normalization be performed? MS' text 
     * property doesn't but what about "ignorable whitespace"?Does not 
     * perform any whitespace normalization and ignores "ignorable 
     * whitespace".Should this be two methods instead?No. Keep it a read 
     * write attribute.What about the name? MS uses text and innerText. text 
     * conflicts with HTML DOM.Keep the current name, MS has a different 
     * name and different semantic.Should this be optional?No.Setting the 
     * text property on a Document, Document Type, or Notation node is an 
     * error for MS. How do we expose it? Exception? Which one?
     * (teleconference 23 May 2001) consistency with nodeValue. Remove 
     * Document from the list.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than 
     *   fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     * @since DOM Level 3
     */
    public String getTextContent()
                                     throws DOMException;
    /**
     * This attribute returns the text content of this node and its 
     * descendants. When it is defined to be null, setting it has no effect. 
     * When set, any possible children this node may have are removed and 
     * replaced by a single <code>Text</code> node containing the string 
     * this attribute is set to. On getting, no serialization is performed, 
     * the returned string does not contain any markup. No whitespace 
     * normalization is performed, the returned string does not contain the 
     * element content whitespaces . Similarly, on setting, no parsing is 
     * performed either, the input string is taken as pure textual content.
     * <br>The string returned is made of the text content of this node 
     * depending on its type, as defined below: 
     * <table border='1'>
     * <tr>
     * <th>Node type</th>
     * <th>Content</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     * ELEMENT_NODE, ENTITY_NODE, ENTITY_REFERENCE_NODE, 
     * DOCUMENT_FRAGMENT_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>concatenation of the <code>textContent</code> 
     * attribute value of every child node, excluding COMMENT_NODE and 
     * PROCESSING_INSTRUCTION_NODE nodes</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>ATTRIBUTE_NODE, TEXT_NODE, 
     * CDATA_SECTION_NODE, COMMENT_NODE, PROCESSING_INSTRUCTION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>
     * <code>nodeValue</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>DOCUMENT_NODE, DOCUMENT_TYPE_NODE, NOTATION_NODE</td>
     * <td valign='top' rowspan='1' colspan='1'>
     * null</td>
     * </tr>
     * </table> Should any whitespace normalization be performed? MS' text 
     * property doesn't but what about "ignorable whitespace"?Does not 
     * perform any whitespace normalization and ignores "ignorable 
     * whitespace".Should this be two methods instead?No. Keep it a read 
     * write attribute.What about the name? MS uses text and innerText. text 
     * conflicts with HTML DOM.Keep the current name, MS has a different 
     * name and different semantic.Should this be optional?No.Setting the 
     * text property on a Document, Document Type, or Notation node is an 
     * error for MS. How do we expose it? Exception? Which one?
     * (teleconference 23 May 2001) consistency with nodeValue. Remove 
     * Document from the list.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than 
     *   fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     * @since DOM Level 3
     */
    public void setTextContent(String textContent)
                                     throws DOMException;

    /**
     * Returns whether this node is the same node as the given one.
     * <br>This method provides a way to determine whether two 
     * <code>Node</code> references returned by the implementation reference 
     * the same object. When two <code>Node</code> references are references 
     * to the same object, even if through a proxy, the references may be 
     * used completely interchangeably, such that all attributes have the 
     * same values and calling the same DOM method on either reference 
     * always has exactly the same effect.Do we really want to make this 
     * different from equals?Yes, change name from isIdentical to 
     * isSameNode. (Telcon 4 Jul 2000).Is this really needed if we provide a 
     * unique key?Yes, because the key is only unique within a document. 
     * (F2F 2 Mar 2001).Definition of 'sameness' is needed.
     * @param other The node to test against.
     * @return Returns <code>true</code> if the nodes are the same, 
     *   <code>false</code> otherwise.
     * @since DOM Level 3
     */
    public boolean isSameNode(Node other);

    public String lookupNamespacePrefix(String namespaceURI, 
                                        boolean useDefault);
    /**
     * Look up the namespace URI associated to the given prefix, starting from 
     * this node.
     * <br>See  for details on the algorithm used by this method.Name? May 
     * need to change depending on ending of the relative namespace URI 
     * reference nightmare.No need.Should this be optional?No.How does the 
     * lookup work? Is it based on the namespaceURI of the nodes, the 
     * namespace declaration attributes, or a combination of both?See .
     * @param prefix The prefix to look for.
     * @return Returns the associated namespace URI or <code>null</code> if 
     *   none is found.
     * @since DOM Level 3
     */
    public String lookupNamespaceURI(String prefix);

    /**
     * Tests whether two nodes are equal.
     * <br>This method tests for equality of nodes, not sameness (i.e., 
     * whether the two nodes are references to the same object) which can be 
     * tested with <code>Node.isSameNode</code>. All nodes that are the same 
     * will also be equal, though the reverse may not be true.
     * <br>Two nodes are equal if and only if the following conditions are 
     * satisfied: The two nodes are of the same type.The following string 
     * attributes are equal: <code>nodeName</code>, <code>localName</code>, 
     * <code>namespaceURI</code>, <code>prefix</code>, <code>nodeValue</code>
     * , <code>baseURI</code>. This is: they are both <code>null</code>, or 
     * they have the same length and are character for character identical.
     * The <code>attributes</code> <code>NamedNodeMaps</code> are equal. 
     * This is: they are both <code>null</code>, or they have the same 
     * length and for each node that exists in one map there is a node that 
     * exists in the other map and is equal, although not necessarily at the 
     * same index.The <code>childNodes</code> <code>NodeLists</code> are 
     * equal. This is: they are both <code>null</code>, or they have the 
     * same length and contain equal nodes at the same index. This is true 
     * for <code>Attr</code> nodes as for any other type of node. Note that 
     * normalization can affect equality; to avoid this, nodes should be 
     * normalized before being compared. 
     * <br>For two <code>DocumentType</code> nodes to be equal, the following 
     * conditions must also be satisfied: The following string attributes 
     * are equal: <code>publicId</code>, <code>systemId</code>, 
     * <code>internalSubset</code>.The <code>entities</code> 
     * <code>NamedNodeMaps</code> are equal.The <code>notations</code> 
     * <code>NamedNodeMaps</code> are equal. 
     * <br>On the other hand, the following do not affect equality: the 
     * <code>ownerDocument</code> attribute, the <code>specified</code> 
     * attribute for <code>Attr</code> nodes, the 
     * <code>isWhitespaceInElementContent</code> attribute for 
     * <code>Text</code> nodes, as well as any user data or event listeners 
     * registered on the nodes.Should this be optional?No.
     * @param arg The node to compare equality with.
     * @param deep If <code>true</code>, recursively compare the subtrees; if 
     *   <code>false</code>, compare only the nodes themselves (and its 
     *   attributes, if it is an <code>Element</code>).
     * @return If the nodes, and possibly subtrees are equal, 
     *   <code>true</code> otherwise <code>false</code>.
     * @since DOM Level 3
     */
    public boolean isEqualNode(Node arg, 
                               boolean deep);

    /**
     * This method makes available a <code>Node</code>'s specialized interface 
     * (see ).What are the relations between Node.isSupported and 
     * Node3.getInterface?Should we rename this method (and also 
     * DOMImplementation.getInterface?)?getInterface can return a node that 
     * doesn't actually support the requested interface and will lead to a 
     * cast exception. Other solutions are returning null or throwing an 
     * exception.
     * @param feature The name of the feature requested (case-insensitive).
     * @return Returns an alternate <code>Node</code> which implements the 
     *   specialized APIs of the specified feature, if any, or 
     *   <code>null</code> if there is no alternate <code>Node</code> which 
     *   implements interfaces associated with that feature. Any alternate 
     *   <code>Node</code> returned by this method must delegate to the 
     *   primary core <code>Node</code> and not return results inconsistent 
     *   with the primary core <code>Node</code> such as <code>key</code>, 
     *   <code>attributes</code>, <code>childNodes</code>, etc.
     * @since DOM Level 3
     */
    public Node getInterface(String feature);

    /**
     * Associate an object to a key on this node. The object can later be 
     * retrieved from this node by calling <code>getUserData</code> with the 
     * same key.
     * @param key The key to associate the object to.
     * @param data The object to associate to the given key, or 
     *   <code>null</code> to remove any existing association to that key.
     * @param handler The handler to associate to that key, or 
     *   <code>null</code>.
     * @return Returns the <code>DOMObject</code> previously associated to 
     *   the given key on this node, or <code>null</code> if there was none.
     * @since DOM Level 3
     */
    public Object setUserData(String key, 
                              Object data, 
                              UserDataHandler handler);

    /**
     * Retrieves the object associated to a key on a this node. The object 
     * must first have been set to this node by calling 
     * <code>setUserData</code> with the same key.
     * @param key The key the object is associated to.
     * @return Returns the <code>DOMObject</code> associated to the given key 
     *   on this node, or <code>null</code> if there was none.
     * @since DOM Level 3
     */
    public Object getUserData(String key);

}
