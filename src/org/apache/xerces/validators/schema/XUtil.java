/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.schema;

import org.apache.xerces.dom.AttrImpl;
import org.apache.xerces.dom.DocumentImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Some useful utility methods.
 */
public class XUtil {

    //
    // Constructors
    //

    /** This class cannot be instantiated. */
    protected XUtil() {}

    //
    // Public static methods
    //

    /**
     * Copies the source tree into the specified place in a destination
     * tree. The source node and its children are appended as children
     * of the destination node.
     * <p>
     * <em>Note:</em> This is an iterative implementation.
     */
    public static void copyInto(Node src, Node dest) throws DOMException {

        // get node factory
        Document factory = dest.getOwnerDocument();
        boolean domimpl = factory instanceof DocumentImpl;

        // placement variables
        Node start  = src;
        Node parent = src;
        Node place  = src;

        // traverse source tree
        while (place != null) {

            // copy this node
            Node node = null;
            int  type = place.getNodeType();
            switch (type) {
                case Node.CDATA_SECTION_NODE: {
                    node = factory.createCDATASection(place.getNodeValue());
                    break;
                }
                case Node.COMMENT_NODE: {
                    node = factory.createComment(place.getNodeValue());
                    break;
                }
                case Node.ELEMENT_NODE: {
                    Element element = factory.createElement(place.getNodeName());
                    node = element;
                    NamedNodeMap attrs  = place.getAttributes();
                    int attrCount = attrs.getLength();
                    for (int i = 0; i < attrCount; i++) {
                        Attr attr = (Attr)attrs.item(i);
                        String attrName = attr.getNodeName();
                        String attrValue = attr.getNodeValue();
                        element.setAttribute(attrName, attrValue);
                        if (domimpl && !attr.getSpecified()) {
                            ((AttrImpl)element.getAttributeNode(attrName)).setSpecified(false);
                        }
                    }
                    break;
                }
                case Node.ENTITY_REFERENCE_NODE: {
                    node = factory.createEntityReference(place.getNodeName());
                    break;
                }
                case Node.PROCESSING_INSTRUCTION_NODE: {
                    node = factory.createProcessingInstruction(place.getNodeName(),
                                                               place.getNodeValue());
                    break;
                }
                case Node.TEXT_NODE: {
                    node = factory.createTextNode(place.getNodeValue());
                    break;
                }
                default: {
                    throw new IllegalArgumentException("can't copy node type, "+
                                                       type+" ("+
                                                       node.getNodeName()+')');
                }
            }
            dest.appendChild(node);

            // iterate over children
            if (place.hasChildNodes()) {
                parent = place;
                place  = place.getFirstChild();
                dest   = node;
            }

            // advance
            else {
                place = place.getNextSibling();
                while (place == null && parent != start) {
                    place  = parent.getNextSibling();
                    parent = parent.getParentNode();
                    dest   = dest.getParentNode();
                }
            }

        }

    } // copyInto(Node,Node)

    /** Finds and returns the first child element node. */
    public static Element getFirstChildElement(Node parent) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)child;
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    } // getFirstChildElement(Node):Element

    /** Finds and returns the next sibling element node. */
    public static Element getNextSiblingElement(Node node) {

        // search for node
        Node sibling = node.getNextSibling();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                return (Element)sibling;
            }
            sibling = sibling.getNextSibling();
        }

        // not found
        return null;

    } // getNextSiblingdElement(Node):Element

    /** Finds and returns the first child node with the given name. */
    public static Element getFirstChildElement(Node parent, String elemName) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(elemName)) {
                    return (Element)child;
                }
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    } // getFirstChildElement(Node,String):Element

    /** Finds and returns the next sibling node with the given name. */
    public static Element getNextSiblingElement(Node node, String elemName) {

        // search for node
        Node sibling = node.getNextSibling();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                if (sibling.getNodeName().equals(elemName)) {
                    return (Element)sibling;
                }
            }
            sibling = sibling.getNextSibling();
        }

        // not found
        return null;

    } // getNextSiblingdElement(Node,String):Element

    /** Finds and returns the first child node with the given name. */
    public static Element getFirstChildElement(Node parent, String elemNames[]) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                for (int i = 0; i < elemNames.length; i++) {
                    if (child.getNodeName().equals(elemNames[i])) {
                        return (Element)child;
                    }
                }
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    } // getFirstChildElement(Node,String[]):Element

    /** Finds and returns the next sibling node with the given name. */
    public static Element getNextSiblingElement(Node node, String elemNames[]) {

        // search for node
        Node sibling = node.getNextSibling();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                for (int i = 0; i < elemNames.length; i++) {
                    if (sibling.getNodeName().equals(elemNames[i])) {
                        return (Element)sibling;
                    }
                }
            }
            sibling = sibling.getNextSibling();
        }

        // not found
        return null;

    } // getNextSiblingdElement(Node,String[]):Element

    /**
     * Finds and returns the first child node with the given name and
     * attribute name, value pair.
     */
    public static Element getFirstChildElement(Node   parent,
                                               String elemName,
                                               String attrName,
                                               String attrValue) {

        // search for node
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)child;
                if (element.getNodeName().equals(elemName) &&
                    element.getAttribute(attrName).equals(attrValue)) {
                    return element;
                }
            }
            child = child.getNextSibling();
        }

        // not found
        return null;

    } // getFirstChildElement(Node,String,String,String):Element

    /**
     * Finds and returns the next sibling node with the given name and
     * attribute name, value pair. Since only elements have attributes,
     * the node returned will be of type Node.ELEMENT_NODE.
     */
    public static Element getNextSiblingElement(Node   node,
                                                String elemName,
                                                String attrName,
                                                String attrValue) {

        // search for node
        Node sibling = node.getNextSibling();
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)sibling;
                if (element.getNodeName().equals(elemName) &&
                    element.getAttribute(attrName).equals(attrValue)) {
                    return element;
                }
            }
            sibling = sibling.getNextSibling();
        }

        // not found
        return null;

    } // getNextSiblingElement(Node,String,String,String):Element

} // class XUtil
