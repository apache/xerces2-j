/* $Id$ */
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2000 The Apache Software Foundation.  All rights 
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

import org.w3c.dom.*;
import org.w3c.dom.events.*;
import org.apache.xerces.dom.*;
import org.apache.xerces.dom.events.*;

/**
 * ParentNode inherits from NodeImpl and adds the capability of having child
 * nodes. Not every node in the DOM can have children, so only nodes that can
 * should inherit from this class and pay the price for it.
 * <P>
 * ParentNode, just like NodeImpl, also implements NodeList, so it can
 * return itself in response to the getChildNodes() query. This eliminiates
 * the need for a separate ChildNodeList object. Note that this is an
 * IMPLEMENTATION DETAIL; applications should _never_ assume that
 * this identity exists.
 * <P>
 * While we have a direct reference to the first child, the last child is
 * stored as the previous sibling of the first child. First child nodes are
 * marked as being so, and getNextSibling hides this fact.
 *
 */
public abstract class ParentNode
    extends NodeImpl {

    /** Serialization version. */
    static final long serialVersionUID = 2815829867152120872L;

    /** Owner document. */
    protected DocumentImpl ownerDocument;

    protected ChildNode		children [];
    protected int		length;

    //
    // Constructors
    //

    /**
     * No public constructor; only subclasses of ParentNode should be
     * instantiated, and those normally via a Document's factory methods
     */
    protected ParentNode(DocumentImpl ownerDocument) {
        super(ownerDocument);
        this.ownerDocument = ownerDocument;
    }

    /** Constructor for serialization. */
    public ParentNode() {}

    /**
     * Called to minimize space utilization.  Affects only
     * this node; children must be individually trimmed.
     */
    public void trimToSize ()
    {
	if (length == 0)
	    children = null;
        else if (children.length != length) {
	    ChildNode	temp [] = new ChildNode [length];

            System.arraycopy (children, 0, temp, 0, length);
	    children = temp;
	}
    }

    public void reduceWaste ()
    {
	if (children == null)
	    return;

	//
	// Arbitrary -- rather than paying trimToSize() costs
	// on most elements, we routinely accept some waste but
	// do try to reduce egregious waste.  Interacts with
	// the array allocation done in appendChild.
	//
	if ((children.length - length) > 6)
            trimToSize ();
    }

    /**
     * Returns the index of the node in the list of children, such
     * that <em>item()</em> will return that child.
     *
     * @param maybeChild the node which may be a child of this one
     * @return the index of the node in the set of children, or
     *	else -1 if that node is not a child
     */
    final protected int getIndexOf(Node maybeChild)
    {
	for (int i = 0; i < length; i++)
	    if (children[i] == maybeChild)
		return i;
	return -1;
    }

    //
    // NodeList methods
    //

    /**
     * Returns a duplicate of a given node. You can consider this a
     * generic "copy constructor" for nodes. The newly returned object should
     * be completely independent of the source object's subtree, so changes
     * in one after the clone has been made will not affect the other.
     * <p>
     * Example: Cloning a Text node will copy both the node and the text it
     * contains.
     * <p>
     * Example: Cloning something that has children -- Element or Attr, for
     * example -- will _not_ clone those children unless a "deep clone"
     * has been requested. A shallow clone of an Attr node will yield an
     * empty Attr of the same name.
     * <p>
     * NOTE: Clones will always be read/write, even if the node being cloned
     * is read-only, to permit applications using only the DOM API to obtain
     * editable copies of locked portions of the tree.
     */
    public Node cloneNode(boolean deep) {
    	
    	ParentNode newnode = (ParentNode) super.cloneNode(deep);

        // set owner document
        newnode.ownerDocument = ownerDocument;

        // REVISIT: Do we need to synchronize at this point? -Ac
        if (needsSyncChildren()) {
            synchronizeChildren();
        }

    	// Need to break the association w/ original kids
        newnode.children = null;
        newnode.length = 0;

        // Then, if deep, clone the kids too.
    	if (deep) {
            for (int i = 0; i < length; i++) {
                newnode.appendChild(children[i].cloneNode(true));
            }
        }

    	return newnode;

    } // cloneNode(boolean):Node

    /**
     * Find the Document that this Node belongs to (the document in
     * whose context the Node was created). The Node may or may not
     * currently be part of that Document's actual contents.
     */
    public Document getOwnerDocument() {
        return ownerDocument;
    }

    /**
     * same as above but returns internal type and this one is not overridden
     * by DocumentImpl to return null
     */
    DocumentImpl ownerDocument() {
        return ownerDocument;
    }

    /**
     * NON-DOM
     * set the ownerDocument of this node and its children
     */
    void setOwnerDocument(DocumentImpl doc) {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ownerDocument = doc;
	for (int i = 0; i < length; i++) {
	    children[i].setOwnerDocument(doc);
	}
        ownerDocument = doc;
    }

    /**
     * Test whether this node has any children. Convenience shorthand
     * for (Node.getFirstChild()!=null)
     */
    public boolean hasChildNodes() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return length > 0;
    }

    /**
     * Obtain a NodeList enumerating all children of this node. If there
     * are none, an (initially) empty NodeList is returned.
     * <p>
     * NodeLists are "live"; as children are added/removed the NodeList
     * will immediately reflect those changes. Also, the NodeList refers
     * to the actual nodes, so changes to those nodes made via the DOM tree
     * will be reflected in the NodeList and vice versa.
     * <p>
     * In this implementation, Nodes implement the NodeList interface and
     * provide their own getChildNodes() support. Other DOMs may solve this
     * differently.
     */
    public NodeList getChildNodes() {
        // JKESS: KNOWN ISSUE HERE 

        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return this;

    } // getChildNodes():NodeList

    /** The first child of this Node, or null if none. */
    public Node getFirstChild() {

        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        if (length == 0)
            return null;
    	return children[0];

    }   // getFirstChild():Node

    /** The last child of this Node, or null if none. */
    public Node getLastChild() {

        if (needsSyncChildren()) {
            synchronizeChildren();
        }
	if (length == 0)
	    return null;
	return children[length - 1];

    } // getLastChild():Node

    final ChildNode lastChild() {
	if (length == 0)
	    return null;
	return children[length - 1];
    }

    /**
     * Move one or more node(s) to our list of children. Note that this
     * implicitly removes them from their previous parent.
     *
     * @param newChild The Node to be moved to our subtree. As a
     * convenience feature, inserting a DocumentNode will instead insert
     * all its children.
     *
     * @param refChild Current child which newChild should be placed
     * immediately before. If refChild is null, the insertion occurs
     * after all existing Nodes, like appendChild().
     *
     * @returns newChild, in its new state (relocated, or emptied in the
     * case of DocumentNode.)
     *
     * @throws DOMException(HIERARCHY_REQUEST_ERR) if newChild is of a
     * type that shouldn't be a child of this node, or if newChild is an
     * ancestor of this node.
     *
     * @throws DOMException(WRONG_DOCUMENT_ERR) if newChild has a
     * different owner document than we do.
     *
     * @throws DOMException(NOT_FOUND_ERR) if refChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
    public Node insertBefore(Node newChild, Node refChild) 
        throws DOMException {
        // Tail-call; optimizer should be able to do good things with.
        return internalInsertBefore(newChild,refChild,MUTATION_ALL);
    } // insertBefore(Node,Node):Node
     
    /** NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able
     * to control which mutation events are spawned. This version of the
     * insertBefore operation allows us to do so. It is not intended
     * for use by application programs.
     */
    Node internalInsertBefore(Node newChild, Node refChild,int mutationMask) 
        throws DOMException {

    	if (isReadOnly())
            throw new DOMExceptionImpl(
                        DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                        "DOM001 Modification not allowed");

        boolean errorChecking = ownerDocument.errorChecking;
    	if (errorChecking && newChild.getOwnerDocument() != ownerDocument) {
            throw new DOMExceptionImpl(DOMException.WRONG_DOCUMENT_ERR, 
                                       "DOM005 Wrong document");
        }

        if (needsSyncChildren()) {
            synchronizeChildren();
        }

        if (errorChecking) {
            // Prevent cycles in the tree
            boolean treeSafe = true;
            for (NodeImpl a = parentNode();
                 treeSafe && a != null;
                 a = a.parentNode()) {
                treeSafe = newChild != a;
            }
            if(!treeSafe) {
                throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, 
                                           "DOM006 Hierarchy request error");
            }

            // refChild must in fact be a child of this node (or null)
            if(refChild != null && refChild.getParentNode() != this) {
                throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR,
                                           "DOM008 Not found");
            }
        }
        
        if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
            // SLOW BUT SAFE: We could insert the whole subtree without
            // juggling so many next/previous pointers. (Wipe out the
            // parent's child-list, patch the parent pointers, set the
            // ends of the list.) But we know some subclasses have special-
            // case behavior they add to insertBefore(), so we don't risk it.
            // This approch also takes fewer bytecodes.

            // NOTE: If one of the children is not a legal child of this
            // node, throw HIERARCHY_REQUEST_ERR before _any_ of the children
            // have been transferred. (Alternative behaviors would be to
            // reparent up to the first failure point or reparent all those
            // which are acceptable to the target node, neither of which is
            // as robust. PR-DOM-0818 isn't entirely clear on which it
            // recommends?????

            // No need to check kids for right-document; if they weren't,
            // they wouldn't be kids of that DocFrag.
            for (int i = 0; i < length; i++) { // Prescan
                if (errorChecking &&
                    !ownerDocument.isKidOK(this, children[i])) {
                    throw new DOMExceptionImpl(
                                           DOMException.HIERARCHY_REQUEST_ERR, 
                                           "DOM006 Hierarchy request error");
                }
            }

            while (newChild.hasChildNodes()) {
                insertBefore(newChild.getFirstChild(), refChild);
            }
        }
        else if (errorChecking &&
                 (!(newChild instanceof ChildNode)
                  ||
                  !ownerDocument.isKidOK(this, newChild))) {
            throw new DOMExceptionImpl(DOMException.HIERARCHY_REQUEST_ERR, 
                                       "DOM006 Hierarchy request error");
        }
        else {
            // Convert to internal type, to avoid repeated casting
            ChildNode newInternal = (ChildNode)newChild;

            EnclosingAttr enclosingAttr=null;
            if(MUTATIONEVENTS && ownerDocument.mutationEvents
               && (mutationMask&MUTATION_AGGREGATE)!=0)
            {
                // MUTATION PREPROCESSING
                // No direct pre-events, but if we're within the scope 
    	        // of an Attr and DOMAttrModified was requested,
                // we need to preserve its previous value.
                LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    enclosingAttr=getEnclosingAttr();
                }
            }

            Node oldparent = newInternal.parentNode();
            if (oldparent != null) {
                oldparent.removeChild(newInternal);
            }

            if (refChild == null) { // append

                // this is the only place this vector needs allocating,
                // though it may also need to be grown in insertBefore.
                // most elements have very few children
                if (children == null)
                    children = new ChildNode [3];
                else if (children.length == length) {
                    ChildNode temp [] = new ChildNode [length * 2];
                    System.arraycopy(children, 0, temp, 0, length);
                    children = temp;
                }

                // set parent
                newInternal.ownerNode = this;
                newInternal.isOwned(true);
                newInternal.parentIndex = length;

                children [length++] = newInternal;

            } else {

                // grow array if needed
                if (children.length == length) {
                    ChildNode temp [] = new ChildNode [length * 2];
                    System.arraycopy(children, 0, temp, 0, length);
                    children = temp;
                }

                for (int i = 0; i < length; i++) {
                    if (children [i] != refChild)
                        continue;

                    // set parent
                    newInternal.ownerNode = this;
                    newInternal.isOwned(true);
                    newInternal.parentIndex = i;

                    System.arraycopy(children, i, children, i + 1, length - i);
                    children [i] = newInternal;
                    length++;
                    break;
                }
            }

            changed();

            if(MUTATIONEVENTS && ownerDocument.mutationEvents)
            {
                // MUTATION POST-EVENTS:
                // "Local" events (non-aggregated)
                if( (mutationMask&MUTATION_LOCAL) != 0)
                {
                    // New child is told it was inserted, and where
                    LCount lc =
                        LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED);
                    if(lc.captures+lc.bubbles+lc.defaults>0)
                    {
                        MutationEvent me= new MutationEventImpl();
                        //?????ownerDocument.createEvent("MutationEvents");
                        me.initMutationEvent(
                                          MutationEventImpl.DOM_NODE_INSERTED,
                                          true,false,this,null,null,null);
                        newInternal.dispatchEvent(me);
                    }

                    // If within the Document, tell the subtree it's been added
                    // to the Doc.
                    lc=LCount.lookup(
                            MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT);
                    if(lc.captures+lc.bubbles+lc.defaults>0)
                    {
                        NodeImpl eventAncestor=this;
                        if(enclosingAttr!=null) 
                            eventAncestor=
                              (NodeImpl)(enclosingAttr.node.getOwnerElement());
                        if(eventAncestor!=null) // Might have been orphan Attr
                        {
                            NodeImpl p=eventAncestor;
                            while(p!=null)
                            {
                                eventAncestor=p; // Last non-null ancestor
                                // In this context, ancestry includes
                                // walking back from Attr to Element
                                if(p.getNodeType()==ATTRIBUTE_NODE)
                                    p=(ElementImpl)
                                        ((AttrImpl)p).getOwnerElement();
                                else
                                    p=p.parentNode();
                            }
                            if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE)
                            {
                                MutationEvent me= new MutationEventImpl();
                                //??ownerDocument.createEvent("MutationEvents")
                                me.initMutationEvent(MutationEventImpl
                                              .DOM_NODE_INSERTED_INTO_DOCUMENT,
                                                     false,false,
                                                     null,null,null,null);
                                dispatchEventToSubtree(newInternal,me);
                            }
                        }
                    }
                }

                // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified
                // (Common to most kinds of mutation)
                if( (mutationMask&MUTATION_AGGREGATE) != 0)
                    dispatchAggregateEvents(enclosingAttr);
            }
        }
        return newChild;

    } // internalInsertBefore(Node,Node,int):Node

    /**
     * Remove a child from this Node. The removed child's subtree
     * remains intact so it may be re-inserted elsewhere.
     *
     * @return oldChild, in its new state (removed).
     *
     * @throws DOMException(NOT_FOUND_ERR) if oldChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
    public Node removeChild(Node oldChild) 
        throws DOMException {
        // Tail-call, should be optimizable
        return internalRemoveChild(oldChild,MUTATION_ALL);
    } // removeChild(Node) :Node
     
    /** NON-DOM INTERNAL: Within DOM actions,we sometimes need to be able
     * to control which mutation events are spawned. This version of the
     * removeChild operation allows us to do so. It is not intended
     * for use by application programs.
     */
    Node internalRemoveChild(Node oldChild,int mutationMask)
        throws DOMException {

        if (isReadOnly()) {
            throw new DOMExceptionImpl(
                DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                "DOM001 Modification not allowed");
        }
         
        if (ownerDocument.errorChecking && 
            oldChild != null && oldChild.getParentNode() != this) {
            throw new DOMExceptionImpl(DOMException.NOT_FOUND_ERR, 
                                       "DOM008 Not found");
        }

        // notify document
        ownerDocument.removedChildNode(oldChild);

        ChildNode oldInternal = (ChildNode) oldChild;

        EnclosingAttr enclosingAttr=null;
        if(MUTATIONEVENTS && ownerDocument.mutationEvents)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            // If we're within the scope of an Attr and DOMAttrModified 
            // was requested, we need to preserve its previous value for
            // that event.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                enclosingAttr=getEnclosingAttr();
            }
            
            if( (mutationMask&MUTATION_LOCAL) != 0)
            {
                // Child is told that it is about to be removed
                lc=LCount.lookup(MutationEventImpl.DOM_NODE_REMOVED);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    MutationEvent me= new MutationEventImpl();
                    //?????ownerDocument.createEvent("MutationEvents");
                    me.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED,
                                         true,false,this,null,null,null);
                    oldInternal.dispatchEvent(me);
                }
            
                // If within Document, child's subtree is informed that it's
                // losing that status
                lc=LCount.lookup(
                             MutationEventImpl.DOM_NODE_REMOVED_FROM_DOCUMENT);
                if(lc.captures+lc.bubbles+lc.defaults>0)
                {
                    NodeImpl eventAncestor=this;
                    if(enclosingAttr!=null) 
                        eventAncestor=
                            (NodeImpl) enclosingAttr.node.getOwnerElement();
                    if(eventAncestor!=null) // Might have been orphan Attr
                    {
                        for(NodeImpl p=eventAncestor.parentNode();
                            p!=null;
                            p=p.parentNode())
                        {
                            eventAncestor=p; // Last non-null ancestor
                        }
                        if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE)
                        {
                            MutationEvent me= new MutationEventImpl();
                            //?????ownerDocument.createEvent("MutationEvents");
                            me.initMutationEvent(MutationEventImpl
                                               .DOM_NODE_REMOVED_FROM_DOCUMENT,
                                                 false,false,
                                                 null,null,null,null);
                            dispatchEventToSubtree(oldInternal,me);
                        }
                    }
                }
            }
        } // End mutation preprocessing

        // Patch tree past oldChild
	for (int i = 0; i < length; i++) {
	    if (children [i] != oldInternal)
		continue;
	    if ((i + 1) != length) {
		System.arraycopy (children, i + 1, children, i,
                                  (length - 1) - i);
            }
	    length--;
	    children[length] = null;

            break;
	}

        // Remove oldInternal's references to tree
        oldInternal.ownerNode       = ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.parentIndex = -1;

        changed();

        if(MUTATIONEVENTS && ownerDocument.mutationEvents)
        {
            // MUTATION POST-EVENTS:
            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
            // if required. (Common to most kinds of mutation)
            if( (mutationMask&MUTATION_AGGREGATE) != 0)
                dispatchAggregateEvents(enclosingAttr);
        } // End mutation postprocessing

        return oldInternal;

    } // internalRemoveChild(Node,int):Node

    /**
     * Make newChild occupy the location that oldChild used to
     * have. Note that newChild will first be removed from its previous
     * parent, if any. Equivalent to inserting newChild before oldChild,
     * then removing oldChild.
     *
     * @returns oldChild, in its new state (removed).
     *
     * @throws DOMException(HIERARCHY_REQUEST_ERR) if newChild is of a
     * type that shouldn't be a child of this node, or if newChild is
     * one of our ancestors.
     *
     * @throws DOMException(WRONG_DOCUMENT_ERR) if newChild has a
     * different owner document than we do.
     *
     * @throws DOMException(NOT_FOUND_ERR) if oldChild is not a child of
     * this node.
     *
     * @throws DOMException(NO_MODIFICATION_ALLOWED_ERR) if this node is
     * read-only.
     */
    public Node replaceChild(Node newChild, Node oldChild)
        throws DOMException {
        // If Mutation Events are being generated, this operation might
        // throw aggregate events twice when modifying an Attr -- once 
        // on insertion and once on removal. DOM Level 2 does not specify 
        // this as either desirable or undesirable, but hints that
        // aggregations should be issued only once per user request.

        EnclosingAttr enclosingAttr=null;
        if(MUTATIONEVENTS && ownerDocument.mutationEvents)
        {
            // MUTATION PREPROCESSING AND PRE-EVENTS:
            // If we're within the scope of an Attr and DOMAttrModified 
            // was requested, we need to preserve its previous value for
            // that event.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if(lc.captures+lc.bubbles+lc.defaults>0)
            {
                enclosingAttr=getEnclosingAttr();
            }
        } // End mutation preprocessing

        internalInsertBefore(newChild, oldChild,MUTATION_LOCAL);
        internalRemoveChild(oldChild,MUTATION_LOCAL);

        if(MUTATIONEVENTS && ownerDocument.mutationEvents)
        {
            dispatchAggregateEvents(enclosingAttr);
        }

        return oldChild;
    }

    //
    // NodeList methods
    //

    /**
     * NodeList method: Count the immediate children of this node
     * @return int
     */
    public int getLength() {
        return length;
    } // getLength():int

    /**
     * NodeList method: Return the Nth immediate child of this node, or
     * null if the index is out of bounds.
     * @return org.w3c.dom.Node
     * @param Index int
     */
    public Node item(int index) {
	if (length == 0 || index >= length)
	    return null;
	try {
	    return children[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    return null;
	}
    } // item(int):Node

    //
    // DOM2: methods, getters, setters
    //

    /**
     * Override default behavior to call normalize() on this Node's
     * children. It is up to implementors or Node to override normalize()
     * to take action.
     */
    public void normalize() {

        Node kid;
        for (int i = 0; i < length; i++) {
            children[i].normalize();
        }
    }

    //
    // Public methods
    //

    /**
     * Override default behavior so that if deep is true, children are also
     * toggled.
     * @see Node
     * <P>
     * Note: this will not change the state of an EntityReference or its
     * children, which are always read-only.
     */
    public void setReadOnly(boolean readOnly, boolean deep) {

        super.setReadOnly(readOnly, deep);

        if (deep) {

            if (needsSyncChildren()) {
                synchronizeChildren();
            }

            // Recursively set kids
            for (int i = 0; i < length; i++) {
                if(!(children[i] instanceof EntityReference)) {
                    children[i].setReadOnly(readOnly,true);
                }
            }
        }
    } // setReadOnly(boolean,boolean)

    //
    // Protected methods
    //

    /**
     * Override this method in subclass to hook in efficient
     * internal data structure.
     */
    protected void synchronizeChildren() {
        // By default just change the flag to avoid calling this method again
        needsSyncChildren(false);
    }

    /**
     * Synchronizes the node's children with the internal structure.
     * Fluffing the children at once solves a lot of work to keep
     * the two structures in sync. The problem gets worse when
     * editing the tree -- this makes it a lot easier.
     * Even though this is only used in deferred classes this method is
     * put here so that it can be shared by all deferred classes.
     */
    protected final void synchronizeChildren(int nodeIndex) {

        // we don't want to generate any event for this so turn them off
        boolean orig = ownerDocument.mutationEvents;
        ownerDocument.mutationEvents = false;

        // no need to sync in the future
        needsSyncChildren(false);

        // create children and link them as siblings
        DeferredDocumentImpl ownerDocument =
            (DeferredDocumentImpl)this.ownerDocument;

        // first count them
        for (int index = ownerDocument.getLastChild(nodeIndex, false);
             index != -1;
             index = ownerDocument.getPrevSibling(index, false)) {
            length++;
        }

        // then fluff them up
        if (length > 0) {
            children = new ChildNode [length];

            int count = length;
            for (int index = ownerDocument.getLastChild(nodeIndex);
                 index != -1;
                 index = ownerDocument.getPrevSibling(index)) {

                ChildNode node = (ChildNode)ownerDocument.getNodeObject(index);
                node.ownerNode = this;
                node.isOwned(true);
                node.parentIndex = --count;
                children[count] = node;
            }
        }

        // set mutation events flag back to its original value
        ownerDocument.mutationEvents = orig;

    } // synchronizeChildren()

    //
    // Serialization methods
    //

    /** Serialize object. */
    private void writeObject(ObjectOutputStream out) throws IOException {

        // synchronize chilren
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        // write object
        out.defaultWriteObject();

    } // writeObject(ObjectOutputStream)

    /** Deserialize object. */
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {

        // perform default deseralization
        ois.defaultReadObject();

        // hardset synchildren - so we don't try to sync- it does not make any sense
        // to try to synchildren when we just desealize object.

        needsSyncChildren(false);

    } // readObject(ObjectInputStream)

} // class ParentNode
