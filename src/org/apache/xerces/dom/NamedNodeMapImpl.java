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

package org.apache.xerces.dom;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.dom.*;

//import org.apache.xerces.domx.events.*;
import org.apache.xerces.dom.events.MutationEventImpl;
import org.w3c.dom.events.*;

/**
 * NamedNodeMaps represent collections of Nodes that can be accessed
 * by name. They're currently used in two modes. Attributes are
 * placed in a NamedNodeMap rather than being children of the node
 * they describe. On the other hand Entity and Notation appear both
 * in the NamedNodeMap _and_ as kids of the DocumentType, requiring
 * some additional logic so these are maintained as "live views" of
 * each other.
 * <P>
 * Only one Node may be stored per name; attempting to
 * store another will replace the previous value.
 * <P>
 * NOTE: The "primary" storage key is taken from the NodeName attribute of the
 * node. The "nodes" Vector is kept sorted by this key to speed lookup,
 * ad make it easier to reconcile with the defaults. The "secondary" 
 * storage key is the namespaceURI and localName, when accessed by 
 * DOM level 2 nodes. all nodes, even DOM Level 2 nodes are stored in
 * a single Vector sorted by the primary "nodename" key.
 * <P>
 * NOTE: item()'s integer index does _not_ imply that the named nodes
 * must be stored in an array; that's only an access method. Note too
 * that these indices are "live"; if someone changes the map's
 * contents, the indices associated with nodes may change.
 * <P>
 * As of REC-DOM-Level-1-19981001, Entities and Notations are no longer
 * shadowed as children of DocumentType.
 * <P>
 * This implementation includes support for DOM Level 2 Mutation Events.
 * If the static boolean NodeImpl.MUTATIONEVENTS is not set true, that support
 * is disabled and can be optimized out to reduce code size.
 *
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public class NamedNodeMapImpl
    implements NamedNodeMap, Serializable {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = -7039242451046758020L;
    static final boolean DEBUG = false;

    //
    // Data
    //

    /** Nodes. */
	protected Vector nodes;

    /** Owner document. */
	protected Document ownerDocument; // support for WRONG_DOCUMENT_ERR

    /**
     * Element. Only named node maps holding attributes for elements
     * have an element object. This value is here to support the
     * Attr#getElement method and MutationEvent processing
     * (DOM Level 2, 19 June 1999).
     */
    protected ElementImpl element;

    /** Default nodes. */
	protected NamedNodeMapImpl defaults;

    /** Changes. */
	protected int changes;	// Support for reconcileDefaults

    /** Last defaults changes. */
	protected int lastDefaultsChanges = -1;

    /** Read-only. */
	protected boolean readOnly = false;

    //
    // Constructors
    //

    /** Constructs a named node map. */
    protected NamedNodeMapImpl(Document ownerDoc, NamedNodeMapImpl defaults) {
	    ownerDocument = ownerDoc;
	    this.defaults = defaults;
    }

    /** Constructs a named node map. */
    protected NamedNodeMapImpl(ElementImpl element, NamedNodeMapImpl defaults) {
        this(element.getOwnerDocument(), defaults);
        this.element = element;
    }

    //
    // NamedNodeMap methods
    //

    /**
     * Report how many nodes are currently stored in this NamedNodeMap.
     * Caveat: This is a count rather than an index, so the
     * highest-numbered node at any time can be accessed via
     * item(getLength()-1).
     * <P>
     * COMPLICATION: In the case of attributes,
     * the count has to include any defaults that may be inherited, yet
     * not double-count when there is both a default and a specified
     * value. Convolving the two lists is expensive, and for GC reasons
     * we don't want to register with the DTD (it wouldn't know when to
     * release us).
     * <P>
     * One solution is to go into the change-counter domain, as we did for
     * DeepNodeList. Another is to accept the convolution, and count
     * on the fact that our implementation is a sorted list to keep the cost
     * managable... which works pretty well for getLength(), but makes
     * item() expensive.
     * <P>
     * The ideal fix would be to rearchitect to eliminate integer indexing,
     * but of course that wouldn't meet the spec!
     */
    public int getLength() {
    	reconcileDefaults();
    	return (nodes != null) ? nodes.size() : 0;
    }

    /**
     * Retrieve an item from the map by 0-based index.
     *
     * @param index Which item to retrieve. Note that indices are just an
     * enumeration of the current contents; they aren't guaranteed to be
     * stable, nor do they imply any promises about the order of the
     * NamedNodeMap's contents. In other words, DO NOT assume either that
     * index(i) will always refer to the same entry, or that there is any
     * stable ordering of entries... and be prepared for double-reporting
     * or skips as insertion and deletion occur.
     *
     * @returns the node which currenly has the specified index, or null
     * if index is greater than or equal to getLength().
     */
    public Node item(int index) {
    	reconcileDefaults();
    	return (nodes != null && index < nodes.size()) ?
                    (Node)(nodes.elementAt(index)) : null;
    }

    /**
     * Retrieve a node by name. If not explicitly defined, checks the
     * defaults before giving up.
     *
     * @param name Name of a node to look up.
     * @returns the Node (of unspecified sub-class) stored with that name,
     * or null if no value has been assigned to that name.
     */
    public Node getNamedItem(String name) {

    	int i = findNamePoint(name,0);
        return (i < 0) ? null : (Node)(nodes.elementAt(i));

    } // getNamedItem(String):Node

    /**
     * Introduced in DOM Level 2. <p>
     * Retrieves a node specified by local name and namespace URI.
     *
     * @param namespaceURI  The namespace URI of the node to retrieve.
     *                      When it is null or an empty string, this
     *                      method behaves like getNamedItem.
     * @param localName     The local name of the node to retrieve.
     * @return Node         A Node (of any type) with the specified name, or null if the specified
     *                      name did not identify any node in the map.
     */
    public Node getNamedItemNS(String namespaceURI, String localName) {

    	int i = findNamePoint(namespaceURI, localName);
        return (i < 0) ? null : (Node)(nodes.elementAt(i));

    } // getNamedItemNS(String,String):Node

    /**
     * Adds a node using its nodeName attribute.
     * As the nodeName attribute is used to derive the name which the node must be
     * stored under, multiple nodes of certain types (those that have a "special" string
     * value) cannot be stored as the names would clash. This is seen as preferable to
     * allowing nodes to be aliased.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is returned,
     *      otherwise null is returned. 
     * @param arg 
     *      A node to store in a named node map. The node will later be
     *      accessible using the value of the namespaceURI and localName
     *      attribute of the node. If a node with those namespace URI and
     *      local name is already present in the map, it is replaced by the new
     *      one.
     * @exception org.w3c.dom.DOMException The exception description.
     */
    public Node setNamedItem(Node arg)
        throws DOMException {

    	if (readOnly) {
            throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                       "DOM001 Modification not allowed");
        }
    	
    	if(arg.getOwnerDocument() != ownerDocument) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR,
                                       "DOM005 Wrong document");
        }

    	if (arg instanceof AttrImpl && ((AttrImpl)arg).owned) {
            throw new DOMExceptionImpl(DOMException.INUSE_ATTRIBUTE_ERR,
                                       "DOM009 Attribute already in use");
        }

        NodeImpl argn = (NodeImpl)arg;
    	int i = findNamePoint(arg.getNodeName(),0);
    	Node previous = null;
    	if (i >= 0) {
    		previous = (Node)nodes.elementAt(i);
            if (element != null) {
                ((NodeImpl)arg).parentNode = element;
            }
    		nodes.setElementAt(arg,i);
    	}
    	else {
    		i = -1 - i; // Insert point (may be end of list)
    		if (null == nodes) {
    			nodes = new Vector(5, 10);
            }
            if (element != null) {
                ((NodeImpl)arg).parentNode = element;
            }
    		nodes.insertElementAt(arg, i);
        }			

        // change owning element
        if (element != null) {
            ((NodeImpl)arg).parentNode = element;
        }

    	++changes;

    	// Only NamedNodeMaps containing attributes (those which are
    	// bound to an element) need report MutationEvents
        if(NodeImpl.MUTATIONEVENTS && element!=null)
        {
            // MUTATION POST-EVENTS:
            element.dispatchAggregateEvents(
                (AttrImpl)arg,
                previous==null ? null : previous.getNodeValue()
                );
        }
    	
    	return previous;

    } // setNamedItem(Node):Node

    /**
     * Adds a node using its namespaceURI and localName.
     * @see org.w3c.dom.NamedNodeMap#setNamedItem
     * @return If the new Node replaces an existing node the replaced Node is returned,
     *      otherwise null is returned. 
     * @param arg A node to store in a named node map. The node will later be
     *      accessible using the value of the namespaceURI and localName
     *      attribute of the node. If a node with those namespace URI and
     *      local name is already present in the map, it is replaced by the new
     *      one.
     */
    public Node setNamedItemNS(Node arg)
        throws DOMException {

    	if (readOnly) {
            throw new DOMExceptionImpl(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                       "DOM001 Modification not allowed");
        }
    
    	if(arg.getOwnerDocument() != ownerDocument) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR,
                                       "DOM005 Wrong document");
        }

    	if (arg instanceof AttrImpl && ((AttrImpl)arg).owned) {
            throw new DOMExceptionImpl(DOMException.INUSE_ATTRIBUTE_ERR,
                                       "DOM009 Attribute already in use");
        }

        NodeImpl argn = (NodeImpl)arg;
    	int i = findNamePoint(argn.getNamespaceURI(), argn.getLocalName());
    	Node previous = null;
    	if (i >= 0) {
    		previous = (Node)nodes.elementAt(i);
            if (element != null) {
                ((NodeImpl)arg).parentNode = element;
            }
    		nodes.setElementAt(arg,i);
    	}
    	else {
    	    // If we can't find by namespaceURI, localName, then we find by nodeName
    	    // so we know where to insert.
    	    i = findNamePoint(argn.getNodeName(),0);
    		if (i >=0) {
    		    previous = (Node)nodes.elementAt(i);
                if (element != null) {
                    ((NodeImpl)arg).parentNode = element;
                }
    		    nodes.insertElementAt(arg,i);
    		    
    		} else {
    		    i = -1 - i; // Insert point (may be end of list)
    		    if (null == nodes) {
    			    nodes = new Vector(5, 10);
                }
                if (element != null) {
                    ((NodeImpl)arg).parentNode = element;
                }
    		    nodes.insertElementAt(arg, i);
    		}
        }			

        // change owning element
        if (element != null) {
            ((NodeImpl)arg).parentNode = element;
        }

    	++changes;

    	// Only NamedNodeMaps containing attributes (those which are
    	// bound to an element) need report MutationEvents
        if(NodeImpl.MUTATIONEVENTS && element!=null)
        {
            // MUTATION POST-EVENTS:
            element.dispatchAggregateEvents(
                (AttrImpl)arg,
                previous==null ? null : previous.getNodeValue()
                );
        }
    	
    	return previous;

    } // setNamedItem(Node):Node
   
    /**
     * Removes a node specified by name.
     * @param name
     *      The name of a node to remove. When this NamedNodeMap
     *      contains the attributes attached to an element, as returned
     *      by the attributes attribute of the Node interface, if the
     *      removed attribute is known to have a default value, an
     *      attribute immediately appears containing the default value
     *      as well as the corresponding namespace URI, local name,
     *      and prefix when applicable.
     * @return The node removed from the map if a node with such a name exists.
     */
    /***/
    public Node removeNamedItem(String name)
        throws DOMException {

    	int i = findNamePoint(name,0);
    	if (i < 0) {
    		throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
    		                           "DOM008 Not found");
        }

        Node n = (Node)(nodes.elementAt(i));
        // If there's a default, add it instead
        Node d;
        if (defaults != null && (d = defaults.getNamedItem(name)) != null
            && findNamePoint(name, i+1) < 0) {
            
            NodeImpl clone = (NodeImpl)d.cloneNode(true);
            clone.parentNode = element;
            nodes.setElementAt(clone, i);
        }
        else {
            nodes.removeElementAt(i);
        }

        // remove owning element
        if (element != null) {
            ((NodeImpl)n).parentNode = null;
        }

        ++changes;
        return n;

    } // removeNamedItem(String):Node
    
    /**
     * Introduced in DOM Level 2. <p>
     * Removes a node specified by local name and namespace URI.
     * @param namespaceURI
     *                      The namespace URI of the node to remove.
     *                      When it is null or an empty string, this
     *                      method behaves like removeNamedItem.
     * @param               The local name of the node to remove. When
     *                      this NamedNodeMap contains the attributes
     *                      attached to an element, as returned by the
     *                      attributes attribute of the Node interface, if the
     *                      removed attribute is known to have a default
     *                      value, an attribute immediately appears
     *                      containing the default value.
     * @return Node         The node removed from the map if a node with such a local name and
     *                       namespace URI exists.
     * @throws              NOT_FOUND_ERR: Raised if there is no node named
     *                      name in the map.

     */
     public Node removeNamedItemNS(String namespaceURI, String name)
        throws DOMException {

    	int i = findNamePoint(namespaceURI, name);
    	if (i < 0) {
    		throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
    		                           "DOM008 Not found");
        }

        LCount lc=null;
        String oldvalue="";
        AttrImpl enclosingAttribute=null;
        if(NodeImpl.MUTATIONEVENTS && element!=null)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
               enclosingAttribute=(AttrImpl)(nodes.elementAt(i));
               oldvalue=enclosingAttribute.getNodeValue();
            }
        } // End mutation preprocessing

        Node n = (Node)(nodes.elementAt(i));
        // If there's a default, add it instead
        Node d;
        String nodeName = n.getNodeName();
        if (defaults != null && (d = defaults.getNamedItem(nodeName)) != null
        ) {
            int j = findNamePoint(nodeName,0);
            if (j>=0 && findNamePoint(nodeName, j+1) < 0) {
                NodeImpl clone = (NodeImpl)d.cloneNode(true);
                clone.parentNode = element;
                nodes.setElementAt(clone, i);
            } else {
                nodes.removeElementAt(i);
            }
        }
        else {
            nodes.removeElementAt(i);
        }

        // Need to remove references to an Attr's owner before the
        // MutationEvents fire.
        // ***** We really don't need a seperate "owned" flag; we
        // could instead test for parentNode!=null... or alternatively,
        // and perhaps better, we could replace owned with ownerElement
        // and avoid overloading parentNode as owner.
        if (element != null) {
            AttrImpl attr=(AttrImpl)n;
            attr.parentNode = null;
            attr.owned=false;
        }

        ++changes;

		// We can't use the standard dispatchAggregate, since it assumes
		// that the Attr is still attached to an owner. This code is
		// similar but dispatches to the previous owner, "element".
        if(NodeImpl.MUTATIONEVENTS && element!=null)
        {
    	    // If we have to send DOMAttrModified (determined earlier),
	        // do so.
	        if(lc.captures+lc.bubbles+lc.defaults>0)
	        {
                MutationEvent me=
                    new MutationEventImpl();
                //?????ownerDocument.createEvent("MutationEvents");
                me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED,true,false,
                   null,n.getNodeValue(),element.getAttribute(name),name);
                element.dispatchEvent(me);
            }

            // We can hand off to process DOMSubtreeModified, though.
            // Note that only the Element needs to be informed; the
            // Attr's subtree has not been changed by this operation.
            element.dispatchAggregateEvents(null,null);
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
     
     // REVIST: Currently, this does _not_ clone the docType reference.
     // Should the new docType object (if any) be a parameter, rather
     // than being set manually later on?
     // We _do_ clone the defaults reference, if any.
    public NamedNodeMapImpl cloneMap() {
        return cloneMap(null);
    }
    public NamedNodeMapImpl cloneMap(ElementImpl element) {

    	NamedNodeMapImpl newmap = null;
        if (element != null) {
            newmap = new NamedNodeMapImpl(element, defaults);
        }
        else {
            newmap = new NamedNodeMapImpl(ownerDocument, defaults);
        }
    	if (nodes != null) {
    		newmap.nodes = new Vector(nodes.size());
    		for (int i = 0; i < nodes.size(); ++i) {
                NodeImpl clone = (NodeImpl)((Node)nodes.elementAt(i)).cloneNode(true);
                newmap.setNamedItem(clone);
            }
        }
    	newmap.defaults = defaults;
        newmap.lastDefaultsChanges = lastDefaultsChanges;
    	return newmap;

    } // cloneMap():NamedNodeMapImpl

    //
    // Package methods
    //

    /**
     * Internal subroutine to allow read-only Nodes to make their contained
     * NamedNodeMaps readonly too. I expect that in fact the shallow
     * version of this operation will never be
     *
     * @param readOnly boolean true to make read-only, false to permit editing.
     * @param deep boolean true to pass this request along to the contained
     * nodes, false to only toggle the NamedNodeMap itself. I expect that
     * the shallow version of this operation will never be used, but I want
     * to design it in now, while I'm thinking about it.
     */
    void setReadOnly(boolean readOnly, boolean deep) {

    	this.readOnly = readOnly;
    	if(deep && nodes != null) {
    		Enumeration e=nodes.elements();
    		while(e.hasMoreElements()) {
    			((NodeImpl)e.nextElement()).setReadOnly(readOnly,deep);
            }
    	}

    } // setReadOnly(boolean,boolean)
    
    /**
     * Internal subroutine returns this NodeNameMap's (shallow) readOnly value.
     *
     */
    boolean getReadOnly() {
    	return readOnly;
    } // getReadOnly()
    

    //
    // Protected methods
    //

    /**
     * Subroutine: If this NamedNodeMap is backed by a "defaults" map (eg,
     * if it's being used for Attributes of an XML file validated against
     * a DTD), we need to deal with the risk that those defaults might
     * have changed. Entries may have been added, changed, or removed, and
     * if so we need to update our version of that information
     * <P>
     * Luckily, this currently applies _only_ to Attributes, which have a
     * "specified" flag that allows us to distinguish which we set manually
     * versus which were defaults... assuming that the defaults list is being
     * maintained properly, of course.
     * <P>
     * Also luckily, The NameNodeMaps are maintained as 
     * sorted lists. This should keep the cost of convolving the two lists
     * managable... not wonderful, but at least more like 2N than N**2..
     * <P>
     * Finally, to avoid doing the convolution except when there are actually
     * changes to be absorbed, I've made the Map aware of whether or not
     * its defaults Map has changed. This is not 110% reliable, but it should
     * work under normal circumstances, especially since the DTD is usually
     * relatively static information.
     * <P>
     * Note: This is NON-DOM implementation, though used to support behavior
     * that the DOM requires.
     */
    protected void reconcileDefaults() {
        
        if (DEBUG) System.out.println("reconcileDefaults:");
    	if (defaults != null && lastDefaultsChanges != defaults.changes) {

    		int n = 0;
            int d = 0;
            int nsize = (nodes != null) ? nodes.size() : 0;
            int dsize = defaults.nodes.size();

    		AttrImpl nnode = (nsize == 0) ? null : (AttrImpl) nodes.elementAt(0);
    		AttrImpl dnode = (dsize == 0) ? null : (AttrImpl) defaults.nodes.elementAt(0);

    		while (n < nsize && d < dsize) {
    			nnode = (AttrImpl) nodes.elementAt(n);
    			dnode = (AttrImpl) defaults.nodes.elementAt(d);
    			if (DEBUG) {
    			System.out.println("\n\nnnode="+nnode.getNodeName());
    			System.out.println("dnode="+dnode.getNodeName());
    			}
    			
    			
    			int test = nnode.getNodeName().compareTo(dnode.getNodeName());

                //REVIST: EACH CONDITION - HOW IT RESPONDS TO DUPLICATE KEYS!
    			// Same name and a default -- make sure same value
    			if (test == 0) {
    			    if (!nnode.getSpecified()) {
    			        if (DEBUG) System.out.println("reconcile (test==0, specified = false): clone default");
                        NodeImpl clone = (NodeImpl)dnode.cloneNode(true);
                        clone.parentNode = element;
    				    nodes.setElementAt(clone, n);
    				    // Advance over both, since names in sync
    				    ++n;
    				    ++d;
    			    }
    			    else { //REVIST: if same name, but specified, simply increment over it.
    			        System.out.println("reconcile (test==0, specified=true): just increment");
    				    ++n;
    				    ++d;
    			    }
    			}

    			// Different name, new default in table; add it
    			else if (test > 0) {
    			    if (DEBUG) System.out.println("reconcile (test>0): insert new default");
                    NodeImpl clone = (NodeImpl)dnode.cloneNode(true);
                    clone.parentNode = element;
    				nodes.insertElementAt(clone, n);
    				// Now in sync, so advance over both
    				++n;
    				++d;
    			}

    			// Different name, old default here; remove it.
    			else if (!nnode.getSpecified()) {
    			    if (DEBUG) System.out.println("reconcile(!specified): remove old default:"
    			    +nnode.getNodeName());
                    // NOTE: We don't need to null out the parent
                    //       because this is a node that we're
                    //       throwing away (not returning). -Ac
                    // REVISIT: [Q] Should we null it out anyway? -Ac
    				nodes.removeElementAt(n);
    				// n didn't advance but represents a different element
    			}

    			// Different name, specified; accept it
                else {
    			    if (DEBUG) System.out.println("reconcile: Different name else accept it.");
    				++n;
                }
        	}

    		// If we ran out of local before default, pick up defaults
            if (d < dsize) {
                if (nodes == null) nodes = new Vector();
                while (d < dsize) {
                    dnode = (AttrImpl)defaults.nodes.elementAt(d++);
                    NodeImpl clone = (NodeImpl)dnode.cloneNode(true);
                    clone.parentNode = element;
    			    if (DEBUG) System.out.println("reconcile: adding"+clone);
                    nodes.addElement(clone);
                }
            }
    		lastDefaultsChanges = defaults.changes;
    	}
    } // reconcileDefaults()


    /**
     * NON-DOM
     * set the ownerDocument of this node, and the attributes it contains
     */
    void setOwnerDocument(DocumentImpl doc) {
        ownerDocument = doc;
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                ((NodeImpl)item(i)).setOwnerDocument(doc);
            }
        }
    }

    //
    // Private methods
    //

    /**
     * Subroutine: Locate the named item, or the point at which said item
     * should be added. 
     *
     * SIDE EFFECT: Resynchronizes w/ Defaults. 
     *
     * @param name Name of a node to look up.
     *
     * @return If positive or zero, the index of the found item.
     * If negative, index of the appropriate point at which to insert
     * the item, encoded as -1-index and hence reconvertable by subtracting
     * it from -1. (Encoding because I don't want to recompare the strings
     * but don't want to burn bytes on a datatype to hold a flagged value.)
     */
    private int findNamePoint(String name, int start) {

    	reconcileDefaults();

    	// Binary search
    	int i = 0;
    	if(nodes != null) {
    		int first = start;
            int last  = nodes.size() - 1;

    		while (first <= last) {
    			i = (first + last) / 2;
    			int test = name.compareTo(((Node)(nodes.elementAt(i))).getNodeName());
    			if(test == 0) {
    				return i; // Name found
                }
    			else if (test < 0) {
    				last = i - 1;
                }
    			else {
    				first = i + 1;
                }
    		}

    		if (first > i) {
                i = first;
            }
    	}

    	return -1 - i; // not-found has to be encoded.

    } // findNamePoint(String):int

    
    /** This findNamePoint is for DOM Level 2 Namespaces.
     */
    private int findNamePoint(String namespaceURI, String name) {
        
        reconcileDefaults();
        if (nodes == null) return -1;
        if (namespaceURI == null) return -1;
        if (name == null) return -1;
        
        // This is a linear search through the same nodes Vector.
        // The Vector is sorted on the DOM Level 1 nodename.
        // The DOM Level 2 NS keys are namespaceURI and Localname, 
        // so we must linear search thru it.
        
        for (int i = 0; i < nodes.size(); i++) {
            NodeImpl a = (NodeImpl)nodes.elementAt(i);
            if (namespaceURI.equals(a.getNamespaceURI())
                && name.equals(a.getLocalName())
                ) {
                return i;
            }
        }
        return -1;
    }
    

} // class NamedNodeMapImpl
