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
 * The <code>Document</code> interface represents the entire HTML or XML 
 * document. Conceptually, it is the root of the document tree, and provides 
 * the primary access to the document's data.
 * <p>Since elements, text nodes, comments, processing instructions, etc. 
 * cannot exist outside the context of a <code>Document</code>, the 
 * <code>Document</code> interface also contains the factory methods needed 
 * to create these objects. The <code>Node</code> objects created have a 
 * <code>ownerDocument</code> attribute which associates them with the 
 * <code>Document</code> within whose context they were created.
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Document extends Node {
    /**
     * The Document Type Declaration (see <code>DocumentType</code>) 
     * associated with this document. For HTML documents as well as XML 
     * documents without a document type declaration this returns 
     * <code>null</code>.
     * <br>This provides direct access to the <code>DocumentType</code> node, 
     * child node of this <code>Document</code>. This node can be set at 
     * document creation time and later changed through the use of child 
     * nodes manipulation methods, such as <code>insertBefore</code>, or 
     * <code>replaceChild</code>. Note, however, that while some 
     * implementations may instantiate different types of 
     * <code>Document</code> objects supporting additional features than the 
     * "Core", such as "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , based on the <code>DocumentType</code> specified at creation time, 
     * changing it afterwards is very unlikely to result in a change of the 
     * features supported.
     * @version DOM Level 3
     */
    public DocumentType getDoctype();

    /**
     * The <code>DOMImplementation</code> object that handles this document. A 
     * DOM application may use objects from multiple implementations.
     */
    public DOMImplementation getImplementation();

    /**
     * This is a convenience attribute that allows direct access to the child 
     * node that is the document element of the document.
     * <br> This attribute represents the property [document element] defined 
     * in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     */
    public Element getDocumentElement();

    /**
     * Creates an element of the type specified. Note that the instance 
     * returned implements the <code>Element</code> interface, so attributes 
     * can be specified directly on the returned object.
     * <br>In addition, if there are known attributes with default values, 
     * <code>Attr</code> nodes representing them are automatically created 
     * and attached to the element.
     * <br>To create an element with a qualified name and namespace URI, use 
     * the <code>createElementNS</code> method.
     * @param tagName The name of the element type to instantiate. For XML, 
     *   this is case-sensitive, otherwise it depends on the case-sentivity 
     *   of the markup language in use. In that case, the name is mapped to 
     *   the canonical form of that markup by the DOM implementation.
     * @return A new <code>Element</code> object with the 
     *   <code>nodeName</code> attribute set to <code>tagName</code>, and 
     *   <code>localName</code>, <code>prefix</code>, and 
     *   <code>namespaceURI</code> set to <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     */
    public Element createElement(String tagName)
                                 throws DOMException;

    /**
     * Creates an empty <code>DocumentFragment</code> object.
     * @return A new <code>DocumentFragment</code>.
     */
    public DocumentFragment createDocumentFragment();

    /**
     * Creates a <code>Text</code> node given the specified string.
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     */
    public Text createTextNode(String data);

    /**
     * Creates a <code>Comment</code> node given the specified string.
     * @param data The data for the node.
     * @return The new <code>Comment</code> object.
     */
    public Comment createComment(String data);

    /**
     * Creates a <code>CDATASection</code> node whose value is the specified 
     * string.
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public CDATASection createCDATASection(String data)
                                           throws DOMException;

    /**
     * Creates a <code>ProcessingInstruction</code> node given the specified 
     * name and data strings.
     * @param target The target part of the processing instruction.
     * @param data The data for the node.
     * @return The new <code>ProcessingInstruction</code> object.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified target contains an 
     *   illegal character.
     *   <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public ProcessingInstruction createProcessingInstruction(String target, 
                                                             String data)
                                                             throws DOMException;

    /**
     * Creates an <code>Attr</code> of the given name. Note that the 
     * <code>Attr</code> instance can then be set on an <code>Element</code> 
     * using the <code>setAttributeNode</code> method. 
     * <br>To create an attribute with a qualified name and namespace URI, use 
     * the <code>createAttributeNS</code> method.
     * @param name The name of the attribute.
     * @return A new <code>Attr</code> object with the <code>nodeName</code> 
     *   attribute set to <code>name</code>, and <code>localName</code>, 
     *   <code>prefix</code>, and <code>namespaceURI</code> set to 
     *   <code>null</code>. The value of the attribute is the empty string.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     */
    public Attr createAttribute(String name)
                                throws DOMException;

    /**
     * Creates an <code>EntityReference</code> object. In addition, if the 
     * referenced entity is known, the child list of the 
     * <code>EntityReference</code> node is made the same as that of the 
     * corresponding <code>Entity</code> node.
     * <p ><b>Note:</b> If any descendant of the <code>Entity</code> node has 
     * an unbound namespace prefix, the corresponding descendant of the 
     * created <code>EntityReference</code> node is also unbound; (its 
     * <code>namespaceURI</code> is <code>null</code>). The DOM Level 2 and 
     * 3 do not support any mechanism to resolve namespace prefixes in this 
     * case.
     * @param name The name of the entity to reference.
     * @return The new <code>EntityReference</code> object.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an 
     *   illegal character.
     *   <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     */
    public EntityReference createEntityReference(String name)
                                                 throws DOMException;

    /**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> in 
     * document order with a given tag name and are contained in the 
     * document.
     * @param tagname The name of the tag to match on. The special value "*" 
     *   matches all tags. For XML, this is case-sensitive, otherwise it 
     *   depends on the case-sentivity of the markup language in use.
     * @return A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code>.
     */
    public NodeList getElementsByTagName(String tagname);

    /**
     * Imports a node from another document to this document. The returned 
     * node has no parent; (<code>parentNode</code> is <code>null</code>). 
     * The source node is not altered or removed from the original document; 
     * this method creates a new copy of the source node.
     * <br>For all nodes, importing a node creates a node object owned by the 
     * importing document, with attribute values identical to the source 
     * node's <code>nodeName</code> and <code>nodeType</code>, plus the 
     * attributes related to namespaces (<code>prefix</code>, 
     * <code>localName</code>, and <code>namespaceURI</code>). As in the 
     * <code>cloneNode</code> operation, the source node is not altered. 
     * User data associated to the imported node is not carried over. 
     * However, if any <code>UserDataHandlers</code> has been specified 
     * along with the associated data these handlers will be called with the 
     * appropriate parameters before this method returns.
     * <br>Additional information is copied as appropriate to the 
     * <code>nodeType</code>, attempting to mirror the behavior expected if 
     * a fragment of XML or HTML source was copied from one document to 
     * another, recognizing that the two documents may have different DTDs 
     * in the XML case. The following list describes the specifics for each 
     * type of node. 
     * <dl>
     * <dt>ATTRIBUTE_NODE</dt>
     * <dd>The <code>ownerElement</code> attribute 
     * is set to <code>null</code> and the <code>specified</code> flag is 
     * set to <code>true</code> on the generated <code>Attr</code>. The 
     * descendants of the source <code>Attr</code> are recursively imported 
     * and the resulting nodes reassembled to form the corresponding subtree.
     * Note that the <code>deep</code> parameter has no effect on 
     * <code>Attr</code> nodes; they always carry their children with them 
     * when imported.</dd>
     * <dt>DOCUMENT_FRAGMENT_NODE</dt>
     * <dd>If the <code>deep</code> option 
     * was set to <code>true</code>, the descendants of the source 
     * <code>DocumentFragment</code> are recursively imported and the 
     * resulting nodes reassembled under the imported 
     * <code>DocumentFragment</code> to form the corresponding subtree. 
     * Otherwise, this simply generates an empty 
     * <code>DocumentFragment</code>.</dd>
     * <dt>DOCUMENT_NODE</dt>
     * <dd><code>Document</code> 
     * nodes cannot be imported.</dd>
     * <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd><code>DocumentType</code> 
     * nodes cannot be imported.</dd>
     * <dt>ELEMENT_NODE</dt>
     * <dd><em>Specified</em> attribute nodes of the source element are imported, and the generated 
     * <code>Attr</code> nodes are attached to the generated 
     * <code>Element</code>. Default attributes are <em>not</em> copied, though if the document being imported into defines default 
     * attributes for this element name, those are assigned. If the 
     * <code>importNode</code> <code>deep</code> parameter was set to 
     * <code>true</code>, the descendants of the source element are 
     * recursively imported and the resulting nodes reassembled to form the 
     * corresponding subtree.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd><code>Entity</code> nodes can be 
     * imported, however in the current release of the DOM the 
     * <code>DocumentType</code> is readonly. Ability to add these imported 
     * nodes to a <code>DocumentType</code> will be considered for addition 
     * to a future release of the DOM.On import, the <code>publicId</code>, 
     * <code>systemId</code>, and <code>notationName</code> attributes are 
     * copied. If a <code>deep</code> import is requested, the descendants 
     * of the the source <code>Entity</code> are recursively imported and 
     * the resulting nodes reassembled to form the corresponding subtree.</dd>
     * <dt>
     * ENTITY_REFERENCE_NODE</dt>
     * <dd>Only the <code>EntityReference</code> itself is 
     * copied, even if a <code>deep</code> import is requested, since the 
     * source and destination documents might have defined the entity 
     * differently. If the document being imported into provides a 
     * definition for this entity name, its value is assigned.</dd>
     * <dt>NOTATION_NODE</dt>
     * <dd>
     * <code>Notation</code> nodes can be imported, however in the current 
     * release of the DOM the <code>DocumentType</code> is readonly. Ability 
     * to add these imported nodes to a <code>DocumentType</code> will be 
     * considered for addition to a future release of the DOM.On import, the 
     * <code>publicId</code> and <code>systemId</code> attributes are copied.
     * Note that the <code>deep</code> parameter has no effect on this type 
     * of nodes since they cannot have any children.</dd>
     * <dt>
     * PROCESSING_INSTRUCTION_NODE</dt>
     * <dd>The imported node copies its 
     * <code>target</code> and <code>data</code> values from those of the 
     * source node.Note that the <code>deep</code> parameter has no effect 
     * on this type of nodes since they cannot have any children.</dd>
     * <dt>TEXT_NODE, 
     * CDATA_SECTION_NODE, COMMENT_NODE</dt>
     * <dd>These three types of nodes inheriting 
     * from <code>CharacterData</code> copy their <code>data</code> and 
     * <code>length</code> attributes from those of the source node.Note 
     * that the <code>deep</code> parameter has no effect on these types of 
     * nodes since they cannot have any children.</dd>
     * </dl> 
     * @param importedNode The node to import.
     * @param deep If <code>true</code>, recursively import the subtree under 
     *   the specified node; if <code>false</code>, import only the node 
     *   itself, as explained above. This has no effect on nodes that cannot 
     *   have any children, and on <code>Attr</code>, and 
     *   <code>EntityReference</code> nodes.
     * @return The imported node that belongs to this <code>Document</code>.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the type of node being imported is not 
     *   supported.
     *   <br>INVALID_CHARACTER_ERR: Raised if one the imported names contain 
     *   an illegal character. This may happen when importing an XML 1.1 [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>] element 
     *   into an XML 1.0 document, for instance.
     * @since DOM Level 2
     */
    public Node importNode(Node importedNode, 
                           boolean deep)
                           throws DOMException;

    /**
     * Creates an element of the given qualified name and namespace URI.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the 
     * namespaceURI parameter for methods if they wish to have no namespace.
     * @param namespaceURI The namespace URI of the element to create.
     * @param qualifiedName The qualified name of the element type to 
     *   instantiate.
     * @return A new <code>Element</code> object with the following 
     *   attributes:
     * <table border='1'>
     * <tr>
     * <th>Attribute</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.namespaceURI</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>namespaceURI</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.prefix</code></td>
     * <td valign='top' rowspan='1' colspan='1'>prefix, extracted 
     *   from <code>qualifiedName</code>, or <code>null</code> if there is 
     *   no prefix</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.localName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>local name, extracted from 
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Element.tagName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * </table>
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified 
     *   <code>qualifiedName</code> contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a 
     *   malformed qualified name, if the <code>qualifiedName</code> has a 
     *   prefix and the <code>namespaceURI</code> is <code>null</code>, or 
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and 
     *   the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *   http://www.w3.org/XML/1998/namespace</a>" [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     *   , or if the <code>qualifiedName</code> or its prefix is "xmlns" and 
     *   the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>", or if the <code>namespaceURI</code> is "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>" and neither the <code>qualifiedName</code> nor its prefix is "xmlns".
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not 
     *   support the <code>"XML"</code> feature, since namespaces were 
     *   defined by XML.
     * @since DOM Level 2
     */
    public Element createElementNS(String namespaceURI, 
                                   String qualifiedName)
                                   throws DOMException;

    /**
     * Creates an attribute of the given qualified name and namespace URI.
     * <br>Per [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     * , applications must use the value <code>null</code> as the 
     * <code>namespaceURI</code> parameter for methods if they wish to have 
     * no namespace.
     * @param namespaceURI The namespace URI of the attribute to create.
     * @param qualifiedName The qualified name of the attribute to 
     *   instantiate.
     * @return A new <code>Attr</code> object with the following attributes:
     * <table border='1'>
     * <tr>
     * <th>
     *   Attribute</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>qualifiedName</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>Node.namespaceURI</code></td>
     * <td valign='top' rowspan='1' colspan='1'><code>namespaceURI</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>Node.prefix</code></td>
     * <td valign='top' rowspan='1' colspan='1'>prefix, extracted from 
     *   <code>qualifiedName</code>, or <code>null</code> if there is no 
     *   prefix</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.localName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>local name, extracted from 
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Attr.name</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeValue</code></td>
     * <td valign='top' rowspan='1' colspan='1'>the empty 
     *   string</td>
     * </tr>
     * </table>
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified 
     *   <code>qualifiedName</code> contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a 
     *   malformed qualified name, if the <code>qualifiedName</code> has a 
     *   prefix and the <code>namespaceURI</code> is <code>null</code>, if 
     *   the <code>qualifiedName</code> has a prefix that is "xml" and the 
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *   http://www.w3.org/XML/1998/namespace</a>", if the <code>qualifiedName</code> or its prefix is "xmlns" and the 
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>", or if the <code>namespaceURI</code> is "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>" and neither the <code>qualifiedName</code> nor its prefix is "xmlns".
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not 
     *   support the <code>"XML"</code> feature, since namespaces were 
     *   defined by XML.
     * @since DOM Level 2
     */
    public Attr createAttributeNS(String namespaceURI, 
                                  String qualifiedName)
                                  throws DOMException;

    /**
     * Returns a <code>NodeList</code> of all the <code>Elements</code> with a 
     * given local name and namespace URI in document order.
     * @param namespaceURI The namespace URI of the elements to match on. The 
     *   special value <code>"*"</code> matches all namespaces.
     * @param localName The local name of the elements to match on. The 
     *   special value "*" matches all local names.
     * @return A new <code>NodeList</code> object containing all the matched 
     *   <code>Elements</code>.
     * @since DOM Level 2
     */
    public NodeList getElementsByTagNameNS(String namespaceURI, 
                                           String localName);

    /**
     * Returns the <code>Element</code> that has an ID attribute with the 
     * given value. If no such element exists, this returns <code>null</code>
     * . If more than one element has an ID attribute with that value, what 
     * is returned is undefined. 
     * <br>The DOM implementation needs to have information that says which 
     * attributes are of type ID. This information can come from validating 
     * the document against a grammar or from the use of the 
     * <code>setIdAttribute</code> method and its siblings on 
     * <code>Element</code>. To query whether an attribute is of type ID see 
     * <code>isId</code> on <code>Attr</code>. 
     * <p ><b>Note:</b> Attributes with the name "ID" or "id" are not of type 
     * ID unless so defined. 
     * @param elementId The unique <code>id</code> value for an element.
     * @return The matching element or <code>null</code> if there is none.
     * @since DOM Level 2
     */
    public Element getElementById(String elementId);

    /**
     * An attribute specifying the actual encoding of this document. This is 
     * <code>null</code> otherwise.
     * <br> This attribute represents the property [character encoding scheme] 
     * defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 3
     */
    public String getActualEncoding();
    /**
     * An attribute specifying the actual encoding of this document. This is 
     * <code>null</code> otherwise.
     * <br> This attribute represents the property [character encoding scheme] 
     * defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 3
     */
    public void setActualEncoding(String actualEncoding);

    /**
     * An attribute specifying, as part of the XML declaration, the encoding 
     * of this document. This is <code>null</code> when unspecified.
     * @since DOM Level 3
     */
    public String getEncoding();
    /**
     * An attribute specifying, as part of the XML declaration, the encoding 
     * of this document. This is <code>null</code> when unspecified.
     * @since DOM Level 3
     */
    public void setEncoding(String encoding);

    /**
     * An attribute specifying, as part of the XML declaration, whether this 
     * document is standalone.
     * <br> This attribute represents the property [standalone] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 3
     */
    public boolean getStandalone();
    /**
     * An attribute specifying, as part of the XML declaration, whether this 
     * document is standalone.
     * <br> This attribute represents the property [standalone] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 3
     */
    public void setStandalone(boolean standalone);

    /**
     * An attribute specifying, as part of the XML declaration, the version 
     * number of this document. This is <code>null</code> when unspecified.
     * <br> This attribute represents the property [version] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 3
     */
    public String getVersion();
    /**
     * An attribute specifying, as part of the XML declaration, the version 
     * number of this document. This is <code>null</code> when unspecified.
     * <br> This attribute represents the property [version] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the version is set to a value that is 
     *   not supported by this <code>Document</code>.
     * @since DOM Level 3
     */
    public void setVersion(String version)
                                  throws DOMException;

    /**
     * An attribute specifying whether error checking is enforced or not. When 
     * set to <code>false</code>, the implementation is free to not test 
     * every possible error case normally defined on DOM operations, and not 
     * raise any <code>DOMException</code>. In case of error, the behavior 
     * is undefined. This attribute is <code>true</code> by default.
     * @since DOM Level 3
     */
    public boolean getStrictErrorChecking();
    /**
     * An attribute specifying whether error checking is enforced or not. When 
     * set to <code>false</code>, the implementation is free to not test 
     * every possible error case normally defined on DOM operations, and not 
     * raise any <code>DOMException</code>. In case of error, the behavior 
     * is undefined. This attribute is <code>true</code> by default.
     * @since DOM Level 3
     */
    public void setStrictErrorChecking(boolean strictErrorChecking);

    /**
     * The location of the document or <code>null</code> if undefined.
     * <br>Beware that when the <code>Document</code> supports the feature 
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over 
     * this attribute.
     * @since DOM Level 3
     */
    public String getDocumentURI();
    /**
     * The location of the document or <code>null</code> if undefined.
     * <br>Beware that when the <code>Document</code> supports the feature 
     * "HTML" [<a href='http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109'>DOM Level 2 HTML</a>]
     * , the href attribute of the HTML BASE element takes precedence over 
     * this attribute.
     * @since DOM Level 3
     */
    public void setDocumentURI(String documentURI);

    /**
     * Changes the <code>ownerDocument</code> of a node, its children, as well 
     * as the attached attribute nodes if there are any. If the node has a 
     * parent it is first removed from its parent child list. This 
     * effectively allows moving a subtree from one document to another. The 
     * following list describes the specifics for each type of node. 
     * <dl>
     * <dt>
     * ATTRIBUTE_NODE</dt>
     * <dd>The <code>ownerElement</code> attribute is set to 
     * <code>null</code> and the <code>specified</code> flag is set to 
     * <code>true</code> on the adopted <code>Attr</code>. The descendants 
     * of the source <code>Attr</code> are recursively adopted.</dd>
     * <dt>
     * DOCUMENT_FRAGMENT_NODE</dt>
     * <dd>The descendants of the source node are 
     * recursively adopted.</dd>
     * <dt>DOCUMENT_NODE</dt>
     * <dd><code>Document</code> nodes cannot 
     * be adopted.</dd>
     * <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd><code>DocumentType</code> nodes cannot 
     * be adopted.</dd>
     * <dt>ELEMENT_NODE</dt>
     * <dd><em>Specified</em> attribute nodes of the source element are adopted, and the generated 
     * <code>Attr</code> nodes. Default attributes are discarded, though if 
     * the document being adopted into defines default attributes for this 
     * element name, those are assigned. The descendants of the source 
     * element are recursively adopted.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd><code>Entity</code> nodes 
     * cannot be adopted.</dd>
     * <dt>ENTITY_REFERENCE_NODE</dt>
     * <dd>Only the 
     * <code>EntityReference</code> node itself is adopted, the descendants 
     * are discarded, since the source and destination documents might have 
     * defined the entity differently. If the document being imported into 
     * provides a definition for this entity name, its value is assigned.</dd>
     * <dt>
     * NOTATION_NODE</dt>
     * <dd><code>Notation</code> nodes cannot be adopted.</dd>
     * <dt>
     * PROCESSING_INSTRUCTION_NODE, TEXT_NODE, CDATA_SECTION_NODE, 
     * COMMENT_NODE</dt>
     * <dd>These nodes can all be adopted. No specifics.</dd>
     * </dl> Should this 
     * method simply return null when it fails? How "exceptional" is failure 
     * for this method?Stick with raising exceptions only in exceptional 
     * circumstances, return null on failure (F2F 19 Jun 2000).Can an entity 
     * node really be adopted?No, neither can Notation nodes (Telcon 13 Dec 
     * 2000).Does this affect keys and hashCode's of the adopted subtree 
     * nodes?If so, what about readonly-ness of key and hashCode?if not, 
     * would appendChild affect keys/hashCodes or would it generate 
     * exceptions if key's are duplicate?Both keys and hashcodes have been 
     * dropped.
     * @param source The node to move into this document.
     * @return The adopted node, or <code>null</code> if this operation 
     *   fails, such as when the source node comes from a different 
     *   implementation.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the source node is of type 
     *   <code>DOCUMENT</code>, <code>DOCUMENT_TYPE</code>.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised when the source node is 
     *   readonly.
     * @since DOM Level 3
     */
    public Node adoptNode(Node source)
                          throws DOMException;

    /**
     *  The configuration used when <code>Document.normalizeDocument</code> is 
     * invoked. 
     * @since DOM Level 3
     */
    public DOMConfiguration getConfig();

    /**
     * This method acts as if the document was going through a save and load 
     * cycle, putting the document in a "normal" form. The actual result 
     * depends on the features being set and governing what operations 
     * actually take place. See <code>DOMConfiguration</code> for details.
     * <br>Noticeably this method normalizes <code>Text</code> nodes, makes 
     * the document "namespace wellformed", according to the algorithm 
     * described in , by adding missing namespace declaration attributes and 
     * adding or changing namespace prefixes, updates the replacement tree 
     * of <code>EntityReference</code> nodes, normalizes attribute values, 
     * etc.
     * <br>Mutation events, when supported, are generated to reflect the 
     * changes occuring on the document.
     * <br>See  for details on how namespace declaration attributes and 
     * prefixes are normalized.Any other name? Joe proposes 
     * normalizeNamespaces.normalizeDocument. (F2F 26 Sep 2001)How specific 
     * should this be? Should we not even specify that this should be done 
     * by walking down the tree?Very. See above.What does this do on 
     * attribute nodes?Doesn't do anything (F2F 1 Aug 2000).How does it work 
     * with entity reference subtree which may be broken?This doesn't affect 
     * entity references which are not visited in this operation (F2F 1 Aug 
     * 2000).Should this really be on Node?Yes, but this only works on 
     * Document, Element, and DocumentFragment. On other types it is a 
     * no-op. (F2F 1 Aug 2000).No. Now that it does much more than simply 
     * fixing namespaces it only makes sense on Document (F2F 26 Sep 2001).
     * What happens with read-only nodes?What/how errors should be reported? 
     * Are there any?Through the error reporter.Should this be optional?No.
     * What happens with regard to mutation events?Mutation events are fired 
     * as expected. (F2F 28 Feb 2002).
     * @since DOM Level 3
     */
    public void normalizeDocument();

    /**
     * Rename an existing node of type <code>ELEMENT_NODE</code> or 
     * <code>ATTRIBUTE_NODE</code>.
     * <br>When possible this simply changes the name of the given node, 
     * otherwise this creates a new node with the specified name and 
     * replaces the existing node with the new node as described below.
     * <br>If simply changing the name of the given node is not possible, the 
     * following operations are performed: a new node is created, any 
     * registered event listener is registered on the new node, any user 
     * data attached to the old node is removed from that node, the old node 
     * is removed from its parent if it has one, the children are moved to 
     * the new node, if the renamed node is an <code>Element</code> its 
     * attributes are moved to the new node, the new node is inserted at the 
     * position the old node used to have in its parent's child nodes list 
     * if it has one, the user data that was attached to the old node is 
     * attached to the new node.
     * <br>When the node being renamed is an <code>Element</code> only the 
     * specified attributes are moved, default attributes originated from 
     * the DTD are updated according to the new element name. In addition, 
     * the implementation may update default attributes from other schemas. 
     * Applications should use normalizeDocument() to guarantee these 
     * attributes are up-to-date.
     * <br>When the node being renamed is an <code>Attr</code> that is 
     * attached to an <code>Element</code>, the node is first removed from 
     * the <code>Element</code> attributes map. Then, once renamed, either 
     * by modifying the existing node or creating a new one as described 
     * above, it is put back.
     * <br>In addition,
     * <ul>
     * <li> a user data event <code>NODE_RENAMED</code> is fired, 
     * </li>
     * <li> 
     * when the implementation supports the feature "MutationEvents", each 
     * mutation operation involved in this method fires the appropriate 
     * event, and in the end the event <code>DOMElementNameChanged</code> or 
     * <code>DOMAttributeNameChanged</code> is fired. 
     * </li>
     * </ul>Should this throw a 
     * HIERARCHY_REQUEST_ERR?No. (F2F 28 Feb 2002).
     * @param n The node to rename.
     * @param namespaceURI The new namespace URI.
     * @param qualifiedName The new qualified name.
     * @return The renamed node. This is either the specified node or the new 
     *   node that was created to replace the specified node.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised when the type of the specified node is 
     *   neither <code>ELEMENT_NODE</code> nor <code>ATTRIBUTE_NODE</code>, 
     *   or if the implementation does not support the renaming of the 
     *   document element.
     *   <br>WRONG_DOCUMENT_ERR: Raised when the specified node was created 
     *   from a different document than this document.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is a 
     *   malformed qualified name, if the <code>qualifiedName</code> has a 
     *   prefix and the <code>namespaceURI</code> is <code>null</code>, or 
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and 
     *   the <code>namespaceURI</code> is different from "<a href='http://www.w3.org/XML/1998/namespace'>
     *   http://www.w3.org/XML/1998/namespace</a>" [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]
     *   . Also raised, when the node being renamed is an attribute, if the 
     *   <code>qualifiedName</code>, or its prefix, is "xmlns" and the 
     *   <code>namespaceURI</code> is different from "<a href='http://www.w3.org/2000/xmlns/'>http://www.w3.org/2000/xmlns/</a>".
     * @since DOM Level 3
     */
    public Node renameNode(Node n, 
                           String namespaceURI, 
                           String qualifiedName)
                           throws DOMException;

}
