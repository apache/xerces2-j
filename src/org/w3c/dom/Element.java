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

package org.w3c.dom;

/**
 * By far the vast majority of objects (apart from text) that authors 
 * encounter when traversing a document are <code>Element</code> nodes.  
 * Assume the following XML document:
 * <pre>
 * &lt;elementExample id="demo"&gt;
 *   &lt;subelement1/&gt;
 *   &lt;subelement2&gt;&lt;subsubelement/&gt;&lt;/subelement2&gt;
 * &lt;/elementExample&gt;  </pre>
 *  
 * <p>When represented using DOM, the top node is a <code>Document</code> node 
 * containing an <code>Element</code> node for "elementExample" which 
 * contains two child <code>Element</code> nodes, one for "subelement1" and 
 * one for "subelement2". "subelement1" contains no child nodes.
 * <p>Elements may have attributes associated with them; since the 
 * <code>Element</code> interface inherits from <code>Node</code>, the generic
 *  <code>Node</code> interface attribute <code>attributes</code> may be used 
 * to retrieve the set of all attributes for an element.  There are methods on
 *  the <code>Element</code> interface to retrieve either an <code>Attr</code>
 *  object by name or an attribute value by name. In XML, where an attribute 
 * value may contain entity references, an <code>Attr</code> object should be 
 * retrieved to examine the possibly fairly complex sub-tree representing the 
 * attribute value. On the other hand, in HTML, where all attributes have 
 * simple string values, methods to directly access an attribute value can 
 * safely be used as a convenience.In DOM Level 2, the method 
 * <code>normalize</code> is inherited from the <code>Node</code> interface 
 * where it was moved.
 */
public interface Element extends Node {
    /**
     * The name of the element. For example, in: 
     * <pre>
     * &lt;elementExample id="demo"&gt; 
     *         ... 
     * &lt;/elementExample&gt; ,</pre>
     *  <code>tagName</code> has the 
     * value <code>"elementExample"</code>. Note that this is case-preserving 
     * in XML, as are all of the operations of the DOM. The HTML DOM returns 
     * the <code>tagName</code> of an HTML element in the canonical uppercase 
     * form, regardless of the case in the  source HTML document. 
     */
    public String       getTagName();
    /**
     * Retrieves an attribute value by name.
     * @param name The name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string if 
     *   that attribute does not have a specified or default value.
     */
    public String       getAttribute(String name);
    /**
     * Adds a new attribute. If an attribute with that name is already present 
     * in the element, its value is changed to be that of the value 
     * parameter. This value is a simple string, it is not parsed as it is 
     * being set. So any markup (such as syntax to be recognized as an entity 
     * reference) is treated as literal text, and needs to be appropriately 
     * escaped by the implementation when it is written out. In order to 
     * assign an attribute value that contains entity references, the user 
     * must create an <code>Attr</code> node plus any <code>Text</code> and 
     * <code>EntityReference</code> nodes, build the appropriate subtree, and 
     * use <code>setAttributeNode</code> to assign it as the value of an 
     * attribute.
     * <br>To set an attribute with a qualified name and namespace URI, use 
     * the <code>setAttributeNS</code> method.
     * @param name The name of the attribute to create or alter.
     * @param value Value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void         setAttribute(String name, 
                                     String value)
                                     throws DOMException;
    /**
     * Removes an attribute by name. If the removed attribute is known to have 
     * a default value, an attribute immediately appears containing the 
     * default value as well as the corresponding namespace URI, local name, 
     * and prefix when applicable.
     * <br>To remove an attribute by local name and namespace URI, use the 
     * <code>removeAttributeNS</code> method.
     * @param name The name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void         removeAttribute(String name)
                                        throws DOMException;
    /**
     * Retrieves an attribute node by name.
     * <br>To retrieve an attribute node by qualified name and namespace URI, 
     * use the <code>getAttributeNodeNS</code> method.
     * @param name The name (<code>nodeName</code>) of the attribute to 
     *   retrieve.
     * @return The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code>) or <code>null</code> if there is no such 
     *   attribute.
     */
    public Attr         getAttributeNode(String name);
    /**
     * Adds a new attribute node. If an attribute with that name (
     * <code>nodeName</code>) is already present in the element, it is 
     * replaced by the new one.
     * <br>To add a new attribute node with a qualified name and namespace URI,
     *  use the <code>setAttributeNodeNS</code> method.
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing 
     *   attribute, the replaced <code>Attr</code> node is returned, 
     *   otherwise <code>null</code> is returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from 
     *   a different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already 
     *   an attribute of another <code>Element</code> object. The DOM user 
     *   must explicitly clone <code>Attr</code> nodes to re-use them in 
     *   other elements.
     */
    public Attr         setAttributeNode(Attr newAttr)
                                         throws DOMException;
    /**
     * Removes the specified attribute node. If the removed <code>Attr</code> 
     * has a default value it is immediately replaced. The replacing 
     * attribute has the same namespace URI and local name, as well as the 
     * original prefix, when applicable.
     * @param oldAttr The <code>Attr</code> node to remove from the attribute 
     *   list.
     * @return The <code>Attr</code> node that was removed.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an 
     *   attribute of the element.
     */
    public Attr         removeAttributeNode(Attr oldAttr)
                                            throws DOMException;
    /**
     * Returns a <code>NodeList</code> of all descendant elements with a given 
     * tag name, in the order in which they would be encountered in a 
     * preorder traversal of the <code>Element</code> tree.
     * @param name The name of the tag to match on. The special value "*" 
     *   matches all tags.
     * @return A list of matching <code>Element</code> nodes.
     */
    public NodeList     getElementsByTagName(String name);
    /**
     * Retrieves an attribute value by local name and namespace URI. HTML-only 
     * DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or an empty string if 
     *   that attribute does not have a specified or default value.
     */
    public String       getAttributeNS(String namespaceURI, 
                                       String localName);
    /**
     * Adds a new attribute. If the given <code>namespaceURI</code> is 
     * <code>null</code> or an empty string and the <code>qualifiedName</code>
     *  has a prefix that is "xml", the new attribute is bound to the 
     * predefined namespace "http://www.w3.org/XML/1998/namespace" . If an 
     * attribute with the same local name and namespace URI is already 
     * present on the element, its prefix is changed to be the prefix part of 
     * the <code>qualifiedName</code>, and its value is changed to be the 
     * <code>value</code> parameter. This value is a simple string, it is not 
     * parsed as it is being set. So any markup (such as syntax to be 
     * recognized as an entity reference) is treated as literal text, and 
     * needs to be appropriately escaped by the implementation when it is 
     * written out. In order to assign an attribute value that contains 
     * entity references, the user must create an <code>Attr</code> node plus 
     * any <code>Text</code> and <code>EntityReference</code> nodes, build 
     * the appropriate subtree, and use <code>setAttributeNodeNS</code> or 
     * <code>setAttributeNode</code> to assign it as the value of an 
     * attribute.
     * <br>HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the attribute to create or 
     *   alter.
     * @param qualifiedName The qualified name of the attribute to create or 
     *   alter.
     * @param value The value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix that is 
     *   "xml" and the <code>namespaceURI</code> is neither <code>null</code> 
     *   nor an empty string nor "http://www.w3.org/XML/1998/namespace", or 
     *   if the <code>qualifiedName</code> has a prefix that is "xmlns" but 
     *   the <code>namespaceURI</code> is neither <code>null</code> nor an 
     *   empty string, or if if the <code>qualifiedName</code> has a prefix 
     *   different from "xml" and "xmlns" and the <code>namespaceURI</code> 
     *   is <code>null</code> or an empty string.
     */
    public void         setAttributeNS(String namespaceURI, 
                                       String qualifiedName, 
                                       String value)
                                       throws DOMException;
    /**
     * Removes an attribute by local name and namespace URI. If the removed 
     * attribute has a default value it is immediately replaced. The 
     * replacing attribute has the same namespace URI and local name, as well 
     * as the original prefix.
     * <br>HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the attribute to remove.
     * @param localName The local name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void         removeAttributeNS(String namespaceURI, 
                                          String localName)
                                          throws DOMException;
    /**
     * Retrieves an <code>Attr</code> node by local name and namespace URI. 
     * HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> node with the specified attribute local 
     *   name and namespace URI or <code>null</code> if there is no such 
     *   attribute.
     */
    public Attr         getAttributeNodeNS(String namespaceURI, 
                                           String localName);
    /**
     * Adds a new attribute. If an attribute with that local name and 
     * namespace URI is already present in the element, it is replaced by the 
     * new one.
     * <br>HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing 
     *   attribute with the same local name and namespace URI, the replaced 
     *   <code>Attr</code> node is returned, otherwise <code>null</code> is 
     *   returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from 
     *   a different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
     *    attribute of another <code>Element</code> object. The DOM user must 
     *   explicitly clone <code>Attr</code> nodes to re-use them in other 
     *   elements.
     */
    public Attr         setAttributeNodeNS(Attr newAttr)
                                           throws DOMException;
    /**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> with a 
     * given local name and namespace URI in the order in which they would be 
     * encountered in a preorder traversal of the <code>Document</code> tree, 
     * starting from this node.
     * <br>HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the elements to match on. The 
     *   special value "*" matches all namespaces.
     * @param localName The local name of the elements to match on. The 
     *   special value "*" matches all local names.
     * @return A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code>.
     */
    public NodeList     getElementsByTagNameNS(String namespaceURI, 
                                               String localName);
}

