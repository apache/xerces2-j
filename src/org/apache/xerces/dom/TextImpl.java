/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.dom;

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Text nodes hold the non-markup, non-Entity content of
 * an Element or Attribute.
 * <P>
 * When a document is first made available to the DOM, there is only
 * one Text object for each block of adjacent plain-text. Users (ie,
 * applications) may create multiple adjacent Texts during editing --
 * see {@link org.w3c.dom.Element#normalize} for discussion.
 * <P>
 * Note that CDATASection is a subclass of Text. This is conceptually
 * valid, since they're really just two different ways of quoting
 * characters when they're written out as part of an XML stream.
 *
 * @version $Id$
 * @since  PR-DOM-Level-1-19980818.
 */
public class TextImpl 
    extends CharacterDataImpl 
    implements CharacterData, Text {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = -5294980852957403469L;
        
    //
    // Constructors
    //

    /** Default constructor */
    public TextImpl(){}

    /** Factory constructor. */
    public TextImpl(CoreDocumentImpl ownerDoc, String data) {
        super(ownerDoc, data);
    }
    
    /**
     * NON-DOM: resets node and sets specified values for the current node
     * 
     * @param ownerDoc
     * @param data
     */
    public void setValues(CoreDocumentImpl ownerDoc, String data){

        flags=0;
        nextSibling = null;
        previousSibling=null;
        setOwnerDocument(ownerDoc);
        super.data = data;
    }
    //
    // Node methods
    //

    /** 
     * A short integer indicating what type of node this is. The named
     * constants for this value are defined in the org.w3c.dom.Node interface.
     */
    public short getNodeType() {
        return Node.TEXT_NODE;
    }

    /** Returns the node name. */
    public String getNodeName() {
        return "#text";
    }

    /**
     * NON-DOM: Set whether this Text is ignorable whitespace.
     */
    public void setIgnorableWhitespace(boolean ignore) {

        if (needsSyncData()) {
            synchronizeData();
        }
        isIgnorableWhitespace(ignore);

    } // setIgnorableWhitespace(boolean)
    

    /**
     * DOM L3 Core CR - Experimental 
     * 
     * Returns whether this text node contains 
     * element content whitespace</a>, often abusively called "ignorable whitespace". 
     * The text node is determined to contain whitespace in element content 
     * during the load of the document or if validation occurs while using 
     * <code>Document.normalizeDocument()</code>.
     * @since DOM Level 3
     */
    public boolean isElementContentWhitespace() {
        // REVISIT: is this implemenation correct?
        if (needsSyncData()) {
            synchronizeData();
        }
        return internalIsIgnorableWhitespace();
    }


    /**
     * DOM Level 3 WD - Experimental.
     * Returns all text of <code>Text</code> nodes logically-adjacent text 
     * nodes to this node, concatenated in document order.
     * @since DOM Level 3
     */
    public String getWholeText(){
        
        if (needsSyncData()) {
            synchronizeData();
        }
        if (nextSibling == null) {
            return data;
        }
        if (fBufferStr == null){
            fBufferStr = new StringBuffer();
        }
        else {
            fBufferStr.setLength(0);
        }
        if (data != null && data.length() != 0) {
            fBufferStr.append(data);
        }
        getWholeText(nextSibling, fBufferStr);
        return fBufferStr.toString();
    
    }

    /**
     * Concatenates the text of all logically-adjacent text nodes
     * 
     * @param node
     * @param buffer
     * @return true - if execution was stopped because the type of node
     *         other than EntityRef, Text, CDATA is encountered, otherwise
     *         return false
     */
    private boolean getWholeText(Node node, StringBuffer buffer){
        String text;
        while (node != null) {
            short type = node.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                if (getWholeText(node.getFirstChild(), buffer)){
                    return true;
                }
            }
            else if (type == Node.TEXT_NODE || 
                     type == Node.CDATA_SECTION_NODE) {
                ((NodeImpl)node).getTextContent(buffer);
            }
            else {
                return true; 
            }

            node = node.getNextSibling();
        }
        return false;
    }

    /**
    * DOM Level 3 WD - Experimental.
    */
    public Text replaceWholeText(String content)
                                 throws DOMException{

        if (needsSyncData()) {
            synchronizeData();
        }

        // make sure we can make the replacement
        if (!canModify(nextSibling)) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }

        Node parent = this.getParentNode();
        if (content == null || content.length() == 0) {
            // remove current node
            if (parent !=null) { // check if node in the tree 
                parent.removeChild(this);
                return null;
            }
        }
        Text currentNode = null;
    	if (isReadOnly()){
            Text newNode = this.ownerDocument().createTextNode(content);
            if (parent !=null) { // check if node in the tree                
                parent.insertBefore(newNode, this);
                parent.removeChild(this);
                currentNode = newNode;
            } else {
                return newNode;
            }
        }  else {
            this.setData(content);
            currentNode = this;
        }
        Node sibling =  currentNode.getNextSibling();
        while ( sibling !=null) {
            parent.removeChild(sibling);
            sibling = currentNode.getNextSibling();
        }

        return currentNode;
    }

    /**
     * If any EntityReference to be removed has descendants
     * that are not EntityReference, Text, or CDATASection
     * nodes, the replaceWholeText method must fail before
     * performing any modification of the document, raising a
     * DOMException with the code NO_MODIFICATION_ALLOWED_ERR.
     * 
     * @param node
     * @return true - can replace text
     *         false - can't replace exception must be raised
     */
    private boolean canModify(Node node){
        while (node != null) {
            short type = node.getNodeType();
            if (type == Node.ENTITY_REFERENCE_NODE) {
                if (!canModify(node.getFirstChild())){
                    return false;
                }
            }
            else if (type != Node.TEXT_NODE && 
                     type != Node.CDATA_SECTION_NODE) {
                return false;
            }

            node = node.getNextSibling();
        }
        return true;
    }

    /**
     * NON-DOM: Returns whether this Text is ignorable whitespace.
     */
    public boolean isIgnorableWhitespace() {

        if (needsSyncData()) {
            synchronizeData();
        }
        return internalIsIgnorableWhitespace();

    } // isIgnorableWhitespace():boolean

    
    //
    // Text methods
    //

    /** 
     * Break a text node into two sibling nodes.  (Note that if the
     * current node has no parent, they won't wind up as "siblings" --
     * they'll both be orphans.)
     *
     * @param offset The offset at which to split. If offset is at the
     * end of the available data, the second node will be empty.
     *
     * @return A reference to the new node (containing data after the
     * offset point). The original node will contain data up to that
     * point.
     *
     * @throws DOMException(INDEX_SIZE_ERR) if offset is <0 or >length.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if node is read-only.
     */
    public Text splitText(int offset) 
        throws DOMException {

    	if (isReadOnly()) {
            throw new DOMException(
    		DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NO_MODIFICATION_ALLOWED_ERR", null));
        }

        if (needsSyncData()) {
            synchronizeData();
        }
    	if (offset < 0 || offset > data.length() ) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, 
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "INDEX_SIZE_ERR", null));
        }
    		
        // split text into two separate nodes
    	Text newText =
            getOwnerDocument().createTextNode(data.substring(offset));
    	setNodeValue(data.substring(0, offset));

        // insert new text node
        Node parentNode = getParentNode();
    	if (parentNode != null) {
    		parentNode.insertBefore(newText, nextSibling);
        }

    	return newText;

    } // splitText(int):Text

    
    /**
     * NON-DOM (used by DOMParser): Reset data for the node. 
     */
    public void replaceData (String value){
        data = value;
    }


    /**
     * NON-DOM (used by DOMParser: Sets data to empty string. 
     *  Returns the value the data was set to.
     */
    public String removeData (){
        String olddata=data;
        data = "";
        return olddata;
    }


} // class TextImpl
