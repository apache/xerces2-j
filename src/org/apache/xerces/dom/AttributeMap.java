/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

package org.apache.xerces.dom;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.dom.*;

import org.apache.xerces.dom.events.MutationEventImpl;
import org.w3c.dom.events.*;

/**
 * AttributeMap inherits from NamedNodeMapImpl and extends it to deal with the
 * specifics of storing attributes. These are:
 * <ul>
 *  <li>managing ownership of attribute nodes
 *  <li>managing default attributes
 *  <li>firing mutation events
 * </ul>
 *
 * @version $Id$
 */
public class AttributeMap extends NamedNodeMapImpl {

    final static boolean DEBUG = false;

    //
    // Constructors
    //

    /** Constructs a named node map. */
    protected AttributeMap(ElementImpl ownerNode, NamedNodeMapImpl defaults) {
        super(ownerNode);
        if (defaults != null) {
            // initialize map with the defaults
            cloneContent(defaults);
            if (nodes != null) {
                hasDefaults(true);
            }
        }
    }

    /**
     * Adds an attribute using its nodeName attribute.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is
     *      returned, otherwise null is returned. 
     * @param arg 
     *      An Attr node to store in this map.
     * @exception org.w3c.dom.DOMException The exception description.
     */
    public Node setNamedItem(Node arg)
        throws DOMException {

    	if (isReadOnly()) {
            throw
                new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "DOM001 Modification not allowed");
        }
    	if(arg.getOwnerDocument() != ownerNode.ownerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                                       "DOM005 Wrong document");
        }
        if (arg.getNodeType() != arg.ATTRIBUTE_NODE) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                                   "DOM006 Hierarchy request error");
        }

        NodeImpl argn = (NodeImpl)arg;

    	if (argn.isOwned()) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,
                                       "DOM009 Attribute already in use");
        }

        // set owner
        argn.ownerNode = ownerNode;
        argn.isOwned(true);

   	int i = findNamePoint(arg.getNodeName(),0);
    	NodeImpl previous = null;
    	if (i >= 0) {
            previous = (NodeImpl) nodes.elementAt(i);
            nodes.setElementAt(arg,i);
            previous.ownerNode = ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
    	} else {
            i = -1 - i; // Insert point (may be end of list)
            if (null == nodes) {
                nodes = new Vector(5, 10);
            }
            nodes.insertElementAt(arg, i);
        }

        if (NodeImpl.MUTATIONEVENTS &&
            ownerNode.ownerDocument().mutationEvents) {
            // MUTATION POST-EVENTS:
            ownerNode.dispatchAggregateEvents(
                (AttrImpl)arg,
                previous==null ? null : previous.getNodeValue(),
                previous==null ?
                           MutationEvent.ADDITION : MutationEvent.MODIFICATION
                );
        }

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!argn.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;

    } // setNamedItem(Node):Node

    /**
     * Adds an attribute using its namespaceURI and localName.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is
     *      returned, otherwise null is returned. 
     * @param arg A node to store in a named node map.
     */
    public Node setNamedItemNS(Node arg)
        throws DOMException {

    	if (isReadOnly()) {
            throw
                new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "DOM001 Modification not allowed");
        }
    
    	if(arg.getOwnerDocument() != ownerNode.ownerDocument()) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
                                       "DOM005 Wrong document");
        }

        if (arg.getNodeType() != arg.ATTRIBUTE_NODE) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                                   "DOM006 Hierarchy request error");
        }
        NodeImpl argn = (NodeImpl)arg;
    	if (argn.isOwned()) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR,
                                       "DOM009 Attribute already in use");
        }

        // set owner
        argn.ownerNode = ownerNode;
        argn.isOwned(true);

    	int i = findNamePoint(argn.getNamespaceURI(), argn.getLocalName());
    	NodeImpl previous = null;
    	if (i >= 0) {
            previous = (NodeImpl) nodes.elementAt(i);
            nodes.setElementAt(arg,i);
            previous.ownerNode = ownerNode.ownerDocument();
            previous.isOwned(false);
            // make sure it won't be mistaken with defaults in case it's reused
            previous.isSpecified(true);
    	} else {
    	    // If we can't find by namespaceURI, localName, then we find by
    	    // nodeName so we know where to insert.
    	    i = findNamePoint(arg.getNodeName(),0);
            if (i >=0) {
                previous = (NodeImpl) nodes.elementAt(i);
                nodes.insertElementAt(arg,i);
            } else {
                i = -1 - i; // Insert point (may be end of list)
                if (null == nodes) {
                    nodes = new Vector(5, 10);
                }
                nodes.insertElementAt(arg, i);
            }
        }
        //    	changed(true);

    	// Only NamedNodeMaps containing attributes (those which are
    	// bound to an element) need report MutationEvents
        if (NodeImpl.MUTATIONEVENTS
            && ownerNode.ownerDocument().mutationEvents)
        {
            // MUTATION POST-EVENTS:
            ownerNode.dispatchAggregateEvents(
                (AttrImpl)arg,
                previous==null ? null : previous.getNodeValue(),
                previous==null ?
                           MutationEvent.ADDITION : MutationEvent.MODIFICATION
                );
        }

        // If the new attribute is not normalized,
        // the owning element is inherently not normalized.
        if (!argn.isNormalized()) {
            ownerNode.isNormalized(false);
        }
        return previous;

    } // setNamedItemNS(Node):Node
   
    /**
     * Removes an attribute specified by name.
     * @param name
     *      The name of a node to remove. If the
     *      removed attribute is known to have a default value, an
     *      attribute immediately appears containing the default value
     *      as well as the corresponding namespace URI, local name,
     *      and prefix when applicable.
     * @return The node removed from the map if a node with such a name exists.
     * @throws              NOT_FOUND_ERR: Raised if there is no node named
     *                      name in the map.
     */
    /***/
    public Node removeNamedItem(String name)
        throws DOMException {
        return internalRemoveNamedItem(name, true);
    }

    /**
     * Same as removeNamedItem except that it simply returns null if the
     * specified name is not found.
     */
    Node safeRemoveNamedItem(String name) {
        return internalRemoveNamedItem(name, false);
    }

    /**
     * Internal removeNamedItem method allowing to specify whether an exception
     * must be thrown if the specified name is not found.
     */
    final protected Node internalRemoveNamedItem(String name, boolean raiseEx){
    	if (isReadOnly()) {
            throw
                new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "DOM001 Modification not allowed");
        }
    	int i = findNamePoint(name,0);
    	if (i < 0) {
            if (raiseEx) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                                           "DOM008 Not found");
            } else {
                return null;
            }
        }

        LCount lc=null;
        String oldvalue="";
        AttrImpl enclosingAttribute=null;
        if (NodeImpl.MUTATIONEVENTS
            && ownerNode.ownerDocument().mutationEvents)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
               enclosingAttribute=(AttrImpl)(nodes.elementAt(i));
               oldvalue=enclosingAttribute.getNodeValue();
            }
        } // End mutation preprocessing

        NodeImpl n = (NodeImpl)nodes.elementAt(i);
        // If there's a default, add it instead
        if (hasDefaults()) {
            NamedNodeMapImpl defaults =
                ((ElementImpl) ownerNode).getDefaultAttributes();
            Node d;
            if (defaults != null && (d = defaults.getNamedItem(name)) != null
                && findNamePoint(name, i+1) < 0) {
            
                NodeImpl clone = (NodeImpl)d.cloneNode(true);
                clone.ownerNode = ownerNode;
                clone.isOwned(true);
                clone.isSpecified(false);
                nodes.setElementAt(clone, i);
            } else {
                nodes.removeElementAt(i);
            }
        } else {
            nodes.removeElementAt(i);
        }

        //        changed(true);

        // remove owning element
        n.ownerNode = ownerNode.ownerDocument();
        n.isOwned(false);
        // make sure it won't be mistaken with defaults in case it's reused
        n.isSpecified(true);

        // We can't use the standard dispatchAggregate, since it assumes
        // that the Attr is still attached to an owner. This code is
        // similar but dispatches to the previous owner, "element".
        if(NodeImpl.MUTATIONEVENTS && ownerNode.ownerDocument().mutationEvents)
        {
    	    // If we have to send DOMAttrModified (determined earlier),
            // do so.
            if(lc.captures+lc.bubbles+lc.defaults>0) {
                MutationEventImpl me= new MutationEventImpl();
                me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,
                                     true, false,
                                     null, n.getNodeValue(),
				     null, name, MutationEvent.REMOVAL);
                ownerNode.dispatchEvent(me);
            }

            // We can hand off to process DOMSubtreeModified, though.
            // Note that only the Element needs to be informed; the
            // Attr's subtree has not been changed by this operation.
            ownerNode.dispatchAggregateEvents(null,null,(short)0);
        }

        return n;

    } // removeNamedItem(String):Node
    
    /**
     * Introduced in DOM Level 2. <p>
     * Removes an attribute specified by local name and namespace URI.
     * @param namespaceURI
     *                      The namespace URI of the node to remove.
     *                      When it is null or an empty string, this
     *                      method behaves like removeNamedItem.
     * @param               The local name of the node to remove. If the
     *                      removed attribute is known to have a default
     *                      value, an attribute immediately appears
     *                      containing the default value.
     * @return Node         The node removed from the map if a node with such
     *                      a local name and namespace URI exists.
     * @throws              NOT_FOUND_ERR: Raised if there is no node named
     *                      name in the map.
     */
    public Node removeNamedItemNS(String namespaceURI, String name)
        throws DOMException {
        return internalRemoveNamedItemNS(namespaceURI, name, true);
    }

    /**
     * Same as removeNamedItem except that it simply returns null if the
     * specified local name and namespace URI is not found.
     */
    Node safeRemoveNamedItemNS(String namespaceURI, String name) {
        return internalRemoveNamedItemNS(namespaceURI, name, false);
    }

    /**
     * Internal removeNamedItemNS method allowing to specify whether an
     * exception must be thrown if the specified local name and namespace URI
     * is not found.
     */
    final protected Node internalRemoveNamedItemNS(String namespaceURI,
                                                   String name,
                                                   boolean raiseEx) {
    	if (isReadOnly()) {
            throw
                new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                     "DOM001 Modification not allowed");
        }
    	int i = findNamePoint(namespaceURI, name);
    	if (i < 0) {
            if (raiseEx) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                                           "DOM008 Not found");
            } else {
                return null;
            }
        }

        LCount lc=null;
        String oldvalue="";
        AttrImpl enclosingAttribute=null;
        if (NodeImpl.MUTATIONEVENTS
            && ownerNode.ownerDocument().mutationEvents)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
               enclosingAttribute=(AttrImpl)(nodes.elementAt(i));
               oldvalue=enclosingAttribute.getNodeValue();
            }
        } // End mutation preprocessing

        NodeImpl n = (NodeImpl)nodes.elementAt(i);
        // If there's a default, add it instead
        String nodeName = n.getNodeName();
        if (hasDefaults()) {
            NamedNodeMapImpl defaults =
                ((ElementImpl) ownerNode).getDefaultAttributes();
            Node d;
            if (defaults != null
                && (d = defaults.getNamedItem(nodeName)) != null)
                {
                    int j = findNamePoint(nodeName,0);
                    if (j>=0 && findNamePoint(nodeName, j+1) < 0) {
                        NodeImpl clone = (NodeImpl)d.cloneNode(true);
                        clone.ownerNode = ownerNode;
                        clone.isOwned(true);
                        clone.isSpecified(false);
                        nodes.setElementAt(clone, i);
                    } else {
                        nodes.removeElementAt(i);
                    }
                } else {
                    nodes.removeElementAt(i);
                }
        } else {
            nodes.removeElementAt(i);
        }

        //        changed(true);

        // Need to remove references to an Attr's owner before the
        // MutationEvents fire.
        n.ownerNode = ownerNode.ownerDocument();
        n.isOwned(false);
        // make sure it won't be mistaken with defaults in case it's reused
        n.isSpecified(true);

        // We can't use the standard dispatchAggregate, since it assumes
        // that the Attr is still attached to an owner. This code is
        // similar but dispatches to the previous owner, "element".
        if(NodeImpl.MUTATIONEVENTS && ownerNode.ownerDocument().mutationEvents)
        {
    	    // If we have to send DOMAttrModified (determined earlier),
            // do so.
            if(lc.captures+lc.bubbles+lc.defaults>0) {
                MutationEventImpl me= new MutationEventImpl();
                me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,
                                     true, false, null, n.getNodeValue(),
				     null, name, MutationEvent.REMOVAL);
                ownerNode.dispatchEvent(me);
            }

            // We can hand off to process DOMSubtreeModified, though.
            // Note that only the Element needs to be informed; the
            // Attr's subtree has not been changed by this operation.
            ownerNode.dispatchAggregateEvents(null,null,(short)0);
        }
        return n;

    } // removeNamedItem(String):Node

    //
    // Public methods
    //

    /**
     * Cloning a NamedNodeMap is a DEEP OPERATION; it always clones
     * all the nodes contained in the map.
     */
     
    public NamedNodeMapImpl cloneMap(NodeImpl ownerNode) {
    	AttributeMap newmap =
            new AttributeMap((ElementImpl) ownerNode, null);
        newmap.hasDefaults(hasDefaults());
        newmap.cloneContent(this);
    	return newmap;
    } // cloneMap():AttributeMap

    /**
     * Override parent's method to set the ownerNode correctly
     */
    protected void cloneContent(NamedNodeMapImpl srcmap) {
    	if (srcmap.nodes != null) {
            if (nodes == null) {
                nodes = new Vector(srcmap.nodes.size());
            }
            else {
                nodes.setSize(srcmap.nodes.size());
            }
            for (int i = 0; i < srcmap.nodes.size(); ++i) {
                NodeImpl n = (NodeImpl) srcmap.nodes.elementAt(i);
                NodeImpl clone = (NodeImpl) n.cloneNode(true);
                clone.isSpecified(n.isSpecified());
                nodes.insertElementAt(clone, i);
                clone.ownerNode = ownerNode;
                clone.isOwned(true);
            }
        }
    } // cloneContent():AttributeMap


    /**
     * Get this AttributeMap in sync with the given "defaults" map.
     * @param defaults The default attributes map to sync with.
     */
    protected void reconcileDefaults(NamedNodeMapImpl defaults) {
        
        // remove any existing default
        int nsize = (nodes != null) ? nodes.size() : 0;
        for (int i = nsize - 1; i >= 0; i--) {
            AttrImpl attr = (AttrImpl) nodes.elementAt(i);
            if (!attr.isSpecified()) {
                // remove owning element
                attr.ownerNode = ownerNode.ownerDocument();
                attr.isOwned(false);
                // make sure it won't be mistaken in case it's reused
                attr.isSpecified(true);
                nodes.removeElementAt(i);
            }
        }
        // add the new defaults
    	if (defaults == null) {
            return;
        }
        if (nodes == null || nodes.size() == 0) {
            cloneContent(defaults);
        }
        else {
            int dsize = defaults.nodes.size();
            for (int n = 0; n < dsize; n++) {
                AttrImpl d = (AttrImpl) defaults.nodes.elementAt(n);
                int i = findNamePoint(d.getNodeName(), 0);
                if (i < 0) {
                    NodeImpl clone = (NodeImpl) d.cloneNode(true);
                    clone.ownerNode = ownerNode;
                    clone.isOwned(true);
                    clone.isSpecified(false);
                    nodes.setElementAt(clone, i);
                }
            }
        }

    } // reconcileDefaults()

} // class AttributeMap
