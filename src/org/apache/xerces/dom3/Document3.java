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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The <code>Document3</code> interface is an extension to the DOM Level 2
 * <code>Document</code> interface containing the DOM Level 3 additions.
 * <p>See also the <a href='http://www.w3.org/2001/10/WD-DOM-Level-3-Core-20011017'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Document3 extends Document {
    /**
     * An attribute specifying the actual encoding of this document. This is 
     * <code>null</code> otherwise.
     * <br> This attribute represents the property [character encoding scheme] 
     * defined in . 
     * @since DOM Level 3
     */
    public String getActualEncoding();
    /**
     * An attribute specifying the actual encoding of this document. This is 
     * <code>null</code> otherwise.
     * <br> This attribute represents the property [character encoding scheme] 
     * defined in . 
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
     * <br> This attribute represents the property [standalone] defined in . 
     * @since DOM Level 3
     */
    public boolean getStandalone();
    /**
     * An attribute specifying, as part of the XML declaration, whether this 
     * document is standalone.
     * <br> This attribute represents the property [standalone] defined in . 
     * @since DOM Level 3
     */
    public void setStandalone(boolean standalone);

    /**
     * An attribute specifying, as part of the XML declaration, the version 
     * number of this document. This is <code>null</code> when unspecified.
     * <br> This attribute represents the property [version] defined in . 
     * @since DOM Level 3
     */
    public String getVersion();
    /**
     * An attribute specifying, as part of the XML declaration, the version 
     * number of this document. This is <code>null</code> when unspecified.
     * <br> This attribute represents the property [version] defined in . 
     * @since DOM Level 3
     */
    public void setVersion(String version);

    /**
     * An attribute specifying whether errors checking is enforced or not. 
     * When set to <code>false</code>, the implementation is free to not 
     * test every possible error case normally defined on DOM operations, 
     * and not raise any <code>DOMException</code>. In case of error, the 
     * behavior is undefined. This attribute is <code>true</code> by 
     * defaults.
     * @since DOM Level 3
     */
    public boolean getStrictErrorChecking();
    /**
     * An attribute specifying whether errors checking is enforced or not. 
     * When set to <code>false</code>, the implementation is free to not 
     * test every possible error case normally defined on DOM operations, 
     * and not raise any <code>DOMException</code>. In case of error, the 
     * behavior is undefined. This attribute is <code>true</code> by 
     * defaults.
     * @since DOM Level 3
     */
    public void setStrictErrorChecking(boolean strictErrorChecking);

    /**
     * This attribute allows applications to specify a 
     * <code>DOMErrorHandler</code> to be called in the event that an error 
     * is encountered while performing an operation on a document. Note that 
     * not all methods use this mechanism, see the description of each 
     * method for details.
     * @since DOM Level 3
     */
    public DOMErrorHandler getErrorHandler();
    /**
     * This attribute allows applications to specify a 
     * <code>DOMErrorHandler</code> to be called in the event that an error 
     * is encountered while performing an operation on a document. Note that 
     * not all methods use this mechanism, see the description of each 
     * method for details.
     * @since DOM Level 3
     */
    public void setErrorHandler(DOMErrorHandler errorHandler);

    /**
     * The location of the document or <code>null</code> if undefined.
     * <br>Beware that when the <code>Document</code> supports the feature 
     * "HTML" , the href attribute of the HTML BASE element takes precedence 
     * over this attribute.
     * @since DOM Level 3
     */
    public String getDocumentURI();
    /**
     * The location of the document or <code>null</code> if undefined.
     * <br>Beware that when the <code>Document</code> supports the feature 
     * "HTML" , the href attribute of the HTML BASE element takes precedence 
     * over this attribute.
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
     * <dd>Specified attribute nodes of the source 
     * element are adopted, and the generated <code>Attr</code> nodes. 
     * Default attributes are discarded, though if the document being 
     * adopted into defines default attributes for this element name, those 
     * are assigned. The descendants of the source element are recursively 
     * adopted.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd><code>Entity</code> nodes cannot be adopted.</dd>
     * <dt>
     * ENTITY_REFERENCE_NODE</dt>
     * <dd>Only the <code>EntityReference</code> node 
     * itself is adopted, the descendants are discarded, since the source 
     * and destination documents might have defined the entity differently. 
     * If the document being imported into provides a definition for this 
     * entity name, its value is assigned.</dd>
     * <dt>NOTATION_NODE</dt>
     * <dd><code>Notation</code> 
     * nodes cannot be adopted.</dd>
     * <dt>PROCESSING_INSTRUCTION_NODE, TEXT_NODE, 
     * CDATA_SECTION_NODE, COMMENT_NODE</dt>
     * <dd>These nodes can all be adopted. No 
     * specifics.</dd>
     * </dl> Should this method simply return null when it fails? How 
     * "exceptional" is failure for this method?Stick with raising 
     * exceptions only in exceptional circumstances, return null on failure 
     * (F2F 19 Jun 2000).Can an entity node really be adopted?No, neither 
     * can Notation nodes (Telcon 13 Dec 2000).Does this affect keys and 
     * hashCode's of the adopted subtree nodes?If so, what about 
     * readonly-ness of key and hashCode?if not, would appendChild affect 
     * keys/hashCodes or would it generate exceptions if key's are duplicate?
     * Both keys and hashcodes have been dropped.
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
     * This method acts as if the document was going through a save and load 
     * cycle, putting the document in a "normal" form. The actual result 
     * depends on the features being set and governing what operations 
     * actually take place. See <code>setNormalizeFeature</code> for details.
     * <br>Noticeably this method normalizes <code>Text</code> nodes, makes 
     * the document "namespace wellformed", according to the algorithm 
     * described below in pseudo code, by adding missing namespace 
     * declaration attributes and adding or changing namespace prefixes, 
     * updates the replacement tree of <code>EntityReference</code> nodes, 
     * normalizes attribute values, etc.
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
     * What happens with regard to mutation events?
     * @since DOM Level 3
     */
    public void normalizeDocument();

    /**
     * Query whether setting a feature to a specific value is supported.
     * <br>The feature name has the same form as a DOM <code>hasFeature</code> 
     * string.
     * @param name The name of the feature to check.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @return <code>true</code> if the feature could be successfully set to 
     *   the specified value, or <code>false</code> if the feature is not 
     *   recognized or the requested value is not supported. This does not 
     *   change the current value of the feature itself.
     * @since DOM Level 3
     */
    public boolean canSetNormalizationFeature(String name, 
                                              boolean state);

    /**
     * Set the state of a feature.
     * <br>The feature name has the same form as a DOM <code>hasFeature</code> 
     * string.
     * <br>It is possible for a <code>Document</code> to recognize a feature 
     * name but to be unable to set its value.Need to specify the list of 
     * features.
     * @param name The name of the feature to set.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised when the feature name is recognized but the 
     *   requested value cannot be set.
     *   <br>NOT_FOUND_ERR: Raised when the feature name is not recognized.
     * @since DOM Level 3
     */
    public void setNormalizationFeature(String name, 
                                        boolean state)
                                        throws DOMException;

    /**
     * Look up the value of a feature.
     * <br>The feature name has the same form as a DOM <code>hasFeature</code> 
     * string
     * @param name The name of the feature to look up.
     * @return The current state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   NOT_FOUND_ERR: Raised when the feature name is not recognized.
     * @since DOM Level 3
     */
    public boolean getNormalizationFeature(String name)
                                           throws DOMException;

    /**
     * Rename an existing node. When possible this simply changes the name of 
     * the given node, otherwise this creates a new node with the specified 
     * name and replaces the existing node with the new node as described 
     * below. This only applies to nodes of type <code>ELEMENT_NODE</code> 
     * and <code>ATTRIBUTE_NODE</code>.
     * <br>When a new node is created, the following operations are performed: 
     * the new node is created, any registered event listener is registered 
     * on the new node, any user data attached to the old node is removed 
     * from that node, the old node is removed from its parent if it has 
     * one, the children are moved to the new node, if the renamed node is 
     * an <code>Element</code> its attributes are moved to the new node, the 
     * new node is inserted at the position the old node used to have in its 
     * parent's child nodes list if it has one, the user data that was 
     * attached to the old node is attach to the new node, the user data 
     * event <code>NODE_RENAMED</code> is fired.
     * <br>When the node being renamed is an <code>Attr</code> that is 
     * attached to an <code>Element</code>, the node is first removed from 
     * the <code>Element</code> attributes map. Then, once renamed, either 
     * by modifying the existing node or creating a new one as described 
     * above, it is put back.
     * <br>In addition, when the implementation supports the feature 
     * "MutationEvents", each mutation operation involved in this method 
     * fires the appropriate event, and in the end the event 
     * <code>ElementNameChanged</code> or <code>AttributeNameChanged</code> 
     * is fired.Should this throw a HIERARCHY_REQUEST_ERR?
     * @param n The node to rename.
     * @param namespaceURI The new namespaceURI.
     * @param name The new qualified name.
     * @return The renamed node. This is either the specified node or the new 
     *   node that was created to replace the specified node.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised when the type of the specified node is 
     *   neither <code>ELEMENT_NODE</code> nor <code>ATTRIBUTE_NODE</code>.
     * @since DOM Level 3
     */
    public Node renameNode(Node n, 
                           String namespaceURI, 
                           String name)
                           throws DOMException;

}
