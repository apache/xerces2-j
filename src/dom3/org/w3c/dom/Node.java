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
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * <p>
 * The <code>Node</code> interface is the primary datatype for the entire 
 * Document Object Model. It represents a single node in the document tree. 
 * While all objects implementing the <code>Node</code> interface expose 
 * methods for dealing with children, not all objects implementing the 
 * <code>Node</code> interface may have children. For example, 
 * <code>Text</code> nodes may not have children, and adding children to 
 * such nodes results in a <code>DOMException</code> being raised.
 * <p>The attributes <code>nodeName</code>, <code>nodeValue</code> and 
 * <code>attributes</code> are included as a mechanism to get at node 
 * information without casting down to the specific derived interface. In 
 * cases where there is no obvious mapping of these attributes for a 
 * specific <code>nodeType</code> (e.g., <code>nodeValue</code> for an 
 * <code>Element</code> or <code>attributes</code> for a <code>Comment</code>
 * ), this returns <code>null</code>. Note that the specialized interfaces 
 * may contain additional and more convenient mechanisms to get and set the 
 * relevant information.
 * <p>The values of <code>nodeName</code>, 
 * <code>nodeValue</code>, and <code>attributes</code> vary according to the 
 * node type as follows: 
 * <table border='1'>
 * <tr>
 * <th>Interface</th>
 * <th>nodeName</th>
 * <th>nodeValue</th>
 * <th>attributes</th>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Attr</td>
 * <td valign='top' rowspan='1' colspan='1'>name of 
 * attribute</td>
 * <td valign='top' rowspan='1' colspan='1'>value of attribute</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>CDATASection</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#cdata-section"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>
 * content of the CDATA Section</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Comment</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#comment"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>content of 
 * the comment</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Document</td>
 * <td valign='top' rowspan='1' colspan='1'><code>"#document"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>DocumentFragment</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * <code>"#document-fragment"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>DocumentType</td>
 * <td valign='top' rowspan='1' colspan='1'>document type name</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Element</td>
 * <td valign='top' rowspan='1' colspan='1'>tag name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>NamedNodeMap</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Entity</td>
 * <td valign='top' rowspan='1' colspan='1'>entity name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>
 * EntityReference</td>
 * <td valign='top' rowspan='1' colspan='1'>name of entity referenced</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Notation</td>
 * <td valign='top' rowspan='1' colspan='1'>notation name</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>ProcessingInstruction</td>
 * <td valign='top' rowspan='1' colspan='1'>target</td>
 * <td valign='top' rowspan='1' colspan='1'>entire content excluding the target</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * <tr>
 * <td valign='top' rowspan='1' colspan='1'>Text</td>
 * <td valign='top' rowspan='1' colspan='1'>
 * <code>"#text"</code></td>
 * <td valign='top' rowspan='1' colspan='1'>content of the text node</td>
 * <td valign='top' rowspan='1' colspan='1'>null</td>
 * </tr>
 * </table> 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Node {
    // NodeType
    /**
     * The node is an <code>Element</code>.
     */
    public static final short ELEMENT_NODE              = 1;
    /**
     * The node is an <code>Attr</code>.
     */
    public static final short ATTRIBUTE_NODE            = 2;
    /**
     * The node is a <code>Text</code> node.
     */
    public static final short TEXT_NODE                 = 3;
    /**
     * The node is a <code>CDATASection</code>.
     */
    public static final short CDATA_SECTION_NODE        = 4;
    /**
     * The node is an <code>EntityReference</code>.
     */
    public static final short ENTITY_REFERENCE_NODE     = 5;
    /**
     * The node is an <code>Entity</code>.
     */
    public static final short ENTITY_NODE               = 6;
    /**
     * The node is a <code>ProcessingInstruction</code>.
     */
    public static final short PROCESSING_INSTRUCTION_NODE = 7;
    /**
     * The node is a <code>Comment</code>.
     */
    public static final short COMMENT_NODE              = 8;
    /**
     * The node is a <code>Document</code>.
     */
    public static final short DOCUMENT_NODE             = 9;
    /**
     * The node is a <code>DocumentType</code>.
     */
    public static final short DOCUMENT_TYPE_NODE        = 10;
    /**
     * The node is a <code>DocumentFragment</code>.
     */
    public static final short DOCUMENT_FRAGMENT_NODE    = 11;
    /**
     * The node is a <code>Notation</code>.
     */
    public static final short NOTATION_NODE             = 12;

    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName();

    /**
     * The value of this node, depending on its type; see the table above. 
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than 
     *   fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     */
    public String getNodeValue()
                              throws DOMException;
    /**
     * The value of this node, depending on its type; see the table above. 
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than 
     *   fit in a <code>DOMString</code> variable on the implementation 
     *   platform.
     */
    public void setNodeValue(String nodeValue)
                              throws DOMException;

    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType();

    /**
     * The parent of this node. All nodes, except <code>Attr</code>, 
     * <code>Document</code>, <code>DocumentFragment</code>, 
     * <code>Entity</code>, and <code>Notation</code> may have a parent. 
     * However, if a node has just been created and not yet added to the 
     * tree, or if it has been removed from the tree, this is 
     * <code>null</code>. 
     * <br> When the node is an <code>Element</code>, a 
     * <code>ProcessingInstruction</code>, an <code>EntityReference</code>, 
     * a <code>CharacterData</code>, a <code>Comment</code>, or a 
     * <code>DocumentType</code>, this attribute represents the properties 
     * [parent] defined in . 
     */
    public Node getParentNode();

    /**
     * A <code>NodeList</code> that contains all children of this node. If 
     * there are no children, this is a <code>NodeList</code> containing no 
     * nodes.
     * <br> When the node is a <code>Document</code>, or an 
     * <code>Element</code>, and if the <code>NodeList</code> does not 
     * contain <code>EntityReference</code> or <code>CDATASection</code> 
     * nodes, this attribute represents the properties [children] defined in 
     * . 
     */
    public NodeList getChildNodes();

    /**
     * The first child of this node. If there is no such node, this returns 
     * <code>null</code>.
     */
    public Node getFirstChild();

    /**
     * The last child of this node. If there is no such node, this returns 
     * <code>null</code>.
     */
    public Node getLastChild();

    /**
     * The node immediately preceding this node. If there is no such node, 
     * this returns <code>null</code>.
     */
    public Node getPreviousSibling();

    /**
     * The node immediately following this node. If there is no such node, 
     * this returns <code>null</code>.
     */
    public Node getNextSibling();

    /**
     * A <code>NamedNodeMap</code> containing the attributes of this node (if 
     * it is an <code>Element</code>) or <code>null</code> otherwise.
     * <br> If no namespace declaration appear in the attributes, this 
     * attribute represents the property [attributes] defined in . If 
     * namespace declarations appear in the attributes, this attribute 
     * combines the properties [attributes] and [namespace attributes] 
     * defined in . 
     */
    public NamedNodeMap getAttributes();

    /**
     * The <code>Document</code> object associated with this node. This is 
     * also the <code>Document</code> object used to create new nodes. When 
     * this node is a <code>Document</code> or a <code>DocumentType</code> 
     * which is not used with any <code>Document</code> yet, this is 
     * <code>null</code>.
     * @version DOM Level 2
     */
    public Document getOwnerDocument();

    /**
     * Inserts the node <code>newChild</code> before the existing child node 
     * <code>refChild</code>. If <code>refChild</code> is <code>null</code>, 
     * insert <code>newChild</code> at the end of the list of children.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object, 
     * all of its children are inserted, in the same order, before 
     * <code>refChild</code>. If the <code>newChild</code> is already in the 
     * tree, it is first removed.
     * @param newChild The node to insert.
     * @param refChild The reference node, i.e., the node before which the 
     *   new node must be inserted.
     * @return The node being inserted.
     * @exception DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not 
     *   allow children of the type of the <code>newChild</code> node, or if 
     *   the node to insert is one of this node's ancestors or this node 
     *   itself, or if this node if of type <code>Document</code> and the 
     *   DOM application attempts to insert a second 
     *   <code>DocumentType</code> or <code>Element</code> node.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created 
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or 
     *   if the parent of the node being inserted is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>refChild</code> is not a child of 
     *   this node.
     *   <br>NOT_SUPPORTED_ERR: if this node if of type <code>Document</code>, 
     *   this exception might be raised if the DOM implementation doesn't 
     *   support the insertion of a <code>DocumentType</code> or 
     *   <code>Element</code> node.
     * @version DOM Level 3
     */
    public Node insertBefore(Node newChild, 
                             Node refChild)
                             throws DOMException;

    /**
     * Replaces the child node <code>oldChild</code> with <code>newChild</code>
     *  in the list of children, and returns the <code>oldChild</code> node.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object, 
     * <code>oldChild</code> is replaced by all of the 
     * <code>DocumentFragment</code> children, which are inserted in the 
     * same order. If the <code>newChild</code> is already in the tree, it 
     * is first removed.
     * @param newChild The new node to put in the child list.
     * @param oldChild The node being replaced in the list.
     * @return The node replaced.
     * @exception DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not 
     *   allow children of the type of the <code>newChild</code> node, or if 
     *   the node to put in is one of this node's ancestors or this node 
     *   itself.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created 
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node or the parent of 
     *   the new node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of 
     *   this node.
     *   <br>NOT_SUPPORTED_ERR: if this node if of type <code>Document</code>, 
     *   this exception might be raised if the DOM implementation doesn't 
     *   support the replacement of the <code>DocumentType</code> child or 
     *   <code>Element</code> child.
     * @version DOM Level 3
     */
    public Node replaceChild(Node newChild, 
                             Node oldChild)
                             throws DOMException;

    /**
     * Removes the child node indicated by <code>oldChild</code> from the list 
     * of children, and returns it.
     * @param oldChild The node being removed.
     * @return The node removed.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of 
     *   this node.
     *   <br>NOT_SUPPORTED_ERR: if this node if of type <code>Document</code>, 
     *   this exception might be raised if the DOM implementation doesn't 
     *   support the removal of the <code>DocumentType</code> child or the 
     *   <code>Element</code> child.
     * @version DOM Level 3
     */
    public Node removeChild(Node oldChild)
                            throws DOMException;

    /**
     * Adds the node <code>newChild</code> to the end of the list of children 
     * of this node. If the <code>newChild</code> is already in the tree, it 
     * is first removed.
     * @param newChild The node to add.If it is a 
     *   <code>DocumentFragment</code> object, the entire contents of the 
     *   document fragment are moved into the child list of this node
     * @return The node added.
     * @exception DOMException
     *   HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not 
     *   allow children of the type of the <code>newChild</code> node, or if 
     *   the node to append is one of this node's ancestors or this node 
     *   itself.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created 
     *   from a different document than the one that created this node.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or 
     *   if the previous parent of the node being inserted is readonly.
     */
    public Node appendChild(Node newChild)
                            throws DOMException;

    /**
     * Returns whether this node has any children.
     * @return <code>true</code> if this node has any children, 
     *   <code>false</code> otherwise.
     */
    public boolean hasChildNodes();

    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy 
     * constructor for nodes. The duplicate node has no parent; (
     * <code>parentNode</code> is <code>null</code>.) and no user data. User 
     * data associated to the imported node is not carried over. However, if 
     * any <code>UserDataHandlers</code> has been specified along with the 
     * associated data these handlers will be called with the appropriate 
     * parameters before this method returns.
     * <br>Cloning an <code>Element</code> copies all attributes and their 
     * values, including those generated by the XML processor to represent 
     * defaulted attributes, but this method does not copy any children it 
     * contains unless it is a deep clone. This includes text contained in 
     * an the <code>Element</code> since the text is contained in a child 
     * <code>Text</code> node. Cloning an <code>Attribute</code> directly, 
     * as opposed to be cloned as part of an <code>Element</code> cloning 
     * operation, returns a specified attribute (<code>specified</code> is 
     * <code>true</code>). Cloning an <code>Attribute</code> always clones 
     * its children, since they represent its value, no matter whether this 
     * is a deep clone or not. Cloning an <code>EntityReference</code> 
     * automatically constructs its subtree if a corresponding 
     * <code>Entity</code> is available, no matter whether this is a deep 
     * clone or not. Cloning any other type of node simply returns a copy of 
     * this node.
     * <br>Note that cloning an immutable subtree results in a mutable copy, 
     * but the children of an <code>EntityReference</code> clone are readonly
     * . In addition, clones of unspecified <code>Attr</code> nodes are 
     * specified. And, cloning <code>Document</code>, 
     * <code>DocumentType</code>, <code>Entity</code>, and 
     * <code>Notation</code> nodes is implementation dependent.
     * @param deep If <code>true</code>, recursively clone the subtree under 
     *   the specified node; if <code>false</code>, clone only the node 
     *   itself (and its attributes, if it is an <code>Element</code>).
     * @return The duplicate node.
     */
    public Node cloneNode(boolean deep);

    /**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree 
     * underneath this <code>Node</code>, including attribute nodes, into a 
     * "normal" form where only structure (e.g., elements, comments, 
     * processing instructions, CDATA sections, and entity references) 
     * separates <code>Text</code> nodes, i.e., there are neither adjacent 
     * <code>Text</code> nodes nor empty <code>Text</code> nodes. This can 
     * be used to ensure that the DOM view of a document is the same as if 
     * it were saved and re-loaded, and is useful when operations (such as 
     * XPointer  lookups) that depend on a particular document tree 
     * structure are to be used.In cases where the document contains 
     * <code>CDATASections</code>, the normalize operation alone may not be 
     * sufficient, since XPointers do not differentiate between 
     * <code>Text</code> nodes and <code>CDATASection</code> nodes.
     * @version DOM Level 2
     */
    public void normalize();

    /**
     * Tests whether the DOM implementation implements a specific feature and 
     * that feature is supported by this node.
     * @param feature The name of the feature to test. This is the same name 
     *   which can be passed to the method <code>hasFeature</code> on 
     *   <code>DOMImplementation</code>.
     * @param version This is the version number of the feature to test. In 
     *   Level 2, version 1, this is the string "2.0". If the version is not 
     *   specified, supporting any version of the feature will cause the 
     *   method to return <code>true</code>.
     * @return Returns <code>true</code> if the specified feature is 
     *   supported on this node, <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean isSupported(String feature, 
                               String version);

    /**
     * The namespace URI of this node, or <code>null</code> if it is 
     * unspecified.
     * <br> When the node is <code>Element</code>, or <code>Attr</code>, this 
     * attribute represents the properties [namespace name] defined in . 
     * <br>This is not a computed value that is the result of a namespace 
     * lookup based on an examination of the namespace declarations in 
     * scope. It is merely the namespace URI given at creation time.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and 
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 
     * method, such as <code>createElement</code> from the 
     * <code>Document</code> interface, this is always <code>null</code>.Per 
     * the Namespaces in XML Specification  an attribute does not inherit 
     * its namespace from the element it is attached to. If an attribute is 
     * not explicitly given a namespace, it simply has no namespace.
     * @since DOM Level 2
     */
    public String getNamespaceURI();

    /**
     * The namespace prefix of this node, or <code>null</code> if it is 
     * unspecified.
     * <br> When the node is <code>Element</code>, or <code>Attr</code>, this 
     * attribute represents the properties [prefix] defined in . 
     * <br>Note that setting this attribute, when permitted, changes the 
     * <code>nodeName</code> attribute, which holds the qualified name, as 
     * well as the <code>tagName</code> and <code>name</code> attributes of 
     * the <code>Element</code> and <code>Attr</code> interfaces, when 
     * applicable.
     * <br>Note also that changing the prefix of an attribute that is known to 
     * have a default value, does not make a new attribute with the default 
     * value and the original prefix appear, since the 
     * <code>namespaceURI</code> and <code>localName</code> do not change.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and 
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 
     * method, such as <code>createElement</code> from the 
     * <code>Document</code> interface, this is always <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified prefix contains an 
     *   illegal character, per the XML 1.0 specification .
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is 
     *   malformed per the Namespaces in XML specification, if the 
     *   <code>namespaceURI</code> of this node is <code>null</code>, if the 
     *   specified prefix is "xml" and the <code>namespaceURI</code> of this 
     *   node is different from "http://www.w3.org/XML/1998/namespace", if 
     *   this node is an attribute and the specified prefix is "xmlns" and 
     *   the <code>namespaceURI</code> of this node is different from "
     *   http://www.w3.org/2000/xmlns/", or if this node is an attribute and 
     *   the <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public String getPrefix();
    /**
     * The namespace prefix of this node, or <code>null</code> if it is 
     * unspecified.
     * <br> When the node is <code>Element</code>, or <code>Attr</code>, this 
     * attribute represents the properties [prefix] defined in . 
     * <br>Note that setting this attribute, when permitted, changes the 
     * <code>nodeName</code> attribute, which holds the qualified name, as 
     * well as the <code>tagName</code> and <code>name</code> attributes of 
     * the <code>Element</code> and <code>Attr</code> interfaces, when 
     * applicable.
     * <br>Note also that changing the prefix of an attribute that is known to 
     * have a default value, does not make a new attribute with the default 
     * value and the original prefix appear, since the 
     * <code>namespaceURI</code> and <code>localName</code> do not change.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and 
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 
     * method, such as <code>createElement</code> from the 
     * <code>Document</code> interface, this is always <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified prefix contains an 
     *   illegal character, per the XML 1.0 specification .
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is 
     *   malformed per the Namespaces in XML specification, if the 
     *   <code>namespaceURI</code> of this node is <code>null</code>, if the 
     *   specified prefix is "xml" and the <code>namespaceURI</code> of this 
     *   node is different from "http://www.w3.org/XML/1998/namespace", if 
     *   this node is an attribute and the specified prefix is "xmlns" and 
     *   the <code>namespaceURI</code> of this node is different from "
     *   http://www.w3.org/2000/xmlns/", or if this node is an attribute and 
     *   the <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public void setPrefix(String prefix)
                               throws DOMException;

    /**
     * Returns the local part of the qualified name of this node.
     * <br> When the node is <code>Element</code>, or <code>Attr</code>, this 
     * attribute represents the properties [local name] defined in . 
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and 
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1 
     * method, such as <code>createElement</code> from the 
     * <code>Document</code> interface, this is always <code>null</code>.
     * @since DOM Level 2
     */
    public String getLocalName();

    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes, 
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributes();

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

    /**
     * Look up the prefix associated to the given namespace URI, starting from 
     * this node.
     * <br>See  for details on the algorithm used by this method.Should this 
     * be optional?No.How does the lookup work? Is it based on the prefix of 
     * the nodes, the namespace declaration attributes, or a combination of 
     * both?See .
     * @param namespaceURI The namespace URI to look for.
     * @param useDefault  Indicates if the lookup mechanism should take into 
     *   account the default namespace or not. 
     * @return Returns an associated namespace prefix if found, 
     *   <code>null</code> if none is found and <code>useDefault</code> is 
     *   false, or <code>null</code> if not found or it is the default 
     *   namespace and <code>useDefault</code> is <code>true</code>. If more 
     *   than one prefix are associated to the namespace prefix, the 
     *   returned namespace prefix is implementation dependent.
     * @since DOM Level 3
     */
    public String lookupNamespacePrefix(String namespaceURI, 
                                        boolean useDefault);

    /**
     *  This method checks if the specified <code>namespaceURI</code> is the 
     * default namespace or not. 
     * @param namespaceURI The namespace URI to look for.
     * @return  <code>true</code> if the specified <code>namespaceURI</code> 
     *   is the default namespace, <code>false</code> otherwise. 
     * @since DOM Level 3
     */
    public boolean isDefaultNamespace(String namespaceURI);

    /**
     * Look up the namespace URI associated to the given prefix, starting from 
     * this node.
     * <br>See  for details on the algorithm used by this method.Name? May 
     * need to change depending on ending of the relative namespace URI 
     * reference nightmare.No need.Should this be optional?No.How does the 
     * lookup work? Is it based on the namespaceURI of the nodes, the 
     * namespace declaration attributes, or a combination of both?See .
     * @param prefix The prefix to look for. If this parameter is 
     *   <code>null</code>, the method will return the default namespace URI 
     *   if any.
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
     * same length and contain equal nodes at the same index. Note that 
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
     * registered on the nodes.Should this be optional?No.Should the deep 
     * parameter be dropped?Yes (Telcon Apr 3, 2002).
     * @param arg The node to compare equality with.
     * @return If the nodes, and possibly subtrees are equal, 
     *   <code>true</code> otherwise <code>false</code>.
     * @since DOM Level 3
     */
    public boolean isEqualNode(Node arg);

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
     * @return Returns the <code>DOMUserData</code> previously associated to 
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
     * @return Returns the <code>DOMUserData</code> associated to the given 
     *   key on this node, or <code>null</code> if there was none.
     * @since DOM Level 3
     */
    public Object getUserData(String key);

}
