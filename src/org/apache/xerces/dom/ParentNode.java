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
 * ParentNode inherits from ChildImpl and adds the capability of having child
 * nodes. Not every node in the DOM can have children, so only nodes that can
 * should inherit from this class and pay the price for it.
 * <P>
 * ParentNode, just like NodeImpl, also implements NodeList, so it can
 * return itself in response to the getChildNodes() query. This eliminiates
 * the need for a separate ChildNodeList object. Note that this is an
 * IMPLEMENTATION DETAIL; applications should _never_ assume that
 * this identity exists. On the other hand, subclasses may need to override
 * this, in case of conflicting names. This is the case for the classes
 * HTMLSelectElementImpl and HTMLFormElementImpl of the HTML DOM.
 * <P>
 * While we have a direct reference to the first child, the last child is
 * stored as the previous sibling of the first child. First child nodes are
 * marked as being so, and getNextSibling hides this fact.
 * <P>Note: Not all parent nodes actually need to also be a child. At some
 * point we used to have ParentNode inheriting from NodeImpl and another class
 * called ChildAndParentNode that inherited from ChildNode. But due to the lack
 * of multiple inheritance a lot of code had to be duplicated which led to a
 * maintenance nightmare. At the same time only a few nodes (Document,
 * DocumentFragment, Entity, and Attribute) cannot be a child so the gain is
 * memory wasn't really worth it. The only type for which this would be the
 * case is Attribute, but we deal with there in another special way, so this is
 * not applicable.
 *
 * <p><b>WARNING</b>: Some of the code here is partially duplicated in
 * AttrImpl, be careful to keep these two classes in sync!
 *
 * @author Arnaud  Le Hors, IBM
 * @author Joe Kesselman, IBM
 * @author Andy Clark, IBM
 */
public abstract class ParentNode
    extends ChildNode {

    /** Serialization version. */
    static final long serialVersionUID = 2815829867152120872L;

    /** Owner document. */
    protected DocumentImpl ownerDocument;

    /** First child. */
    protected ChildNode firstChild = null;

    // transients

    /** Cached node list length. */
    protected transient int fCachedLength = -1;

    /** Last requested node. */
    protected transient ChildNode fCachedChild;

    /** Last requested node index. */
    protected transient int fCachedChildIndex = -1;

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
    	newnode.firstChild      = null;

        // invalidate cache for children NodeList
        newnode.fCachedChildIndex = -1;
        newnode.fCachedLength = -1;

        // Then, if deep, clone the kids too.
    	if (deep) {
            for (ChildNode child = firstChild;
                 child != null;
                 child = child.nextSibling) {
                newnode.appendChild(child.cloneNode(true));
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
	for (ChildNode child = firstChild;
	     child != null; child = child.nextSibling) {
	    child.setOwnerDocument(doc);
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
        return firstChild != null;
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
    	return firstChild;

    }   // getFirstChild():Node

    /** The last child of this Node, or null if none. */
    public Node getLastChild() {

        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return lastChild();

    } // getLastChild():Node

    final ChildNode lastChild() {
        // last child is stored as the previous sibling of first child
        return firstChild != null ? firstChild.previousSibling : null;
    }

    final void lastChild(ChildNode node) {
        // store lastChild as previous sibling of first child
        if (firstChild != null) {
            firstChild.previousSibling = node;
        }
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

        boolean errorChecking = ownerDocument.errorChecking;

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
            if (errorChecking) {
                for (Node kid = newChild.getFirstChild(); // Prescan
                     kid != null; kid = kid.getNextSibling()) {

                    if (!ownerDocument.isKidOK(this, kid)) {
                        throw new DOMException(
                                           DOMException.HIERARCHY_REQUEST_ERR, 
                                           "DOM006 Hierarchy request error");
                    }
                }
            }

            while (newChild.hasChildNodes()) {
                insertBefore(newChild.getFirstChild(), refChild);
            }
            return newChild;
        }

        if (newChild == refChild) {
            // stupid case that must be handled as a no-op triggering events...
            refChild = refChild.getNextSibling();
            removeChild(newChild);
            insertBefore(newChild, refChild);
            return newChild;
        }

        if (needsSyncChildren()) {
            synchronizeChildren();
        }

        if (errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                                       "DOM001 Modification not allowed");
            }
            if (newChild.getOwnerDocument() != ownerDocument) {
                throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, 
                                       "DOM005 Wrong document");
            }
            if (!ownerDocument.isKidOK(this, newChild)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                                       "DOM006 Hierarchy request error");
            }
            // refChild must be a child of this node (or null)
            if (refChild != null && refChild.getParentNode() != this) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,
                                       "DOM008 Not found");
            }

            // Prevent cycles in the tree
            // newChild cannot be ancestor of this Node,
            // and actually cannot be this
            boolean treeSafe = true;
            for (NodeImpl a = this; treeSafe && a != null; a = a.parentNode())
            {
                treeSafe = newChild != a;
            }
            if(!treeSafe) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, 
                                       "DOM006 Hierarchy request error");
            }
        }

        EnclosingAttr enclosingAttr=null;
        if (MUTATIONEVENTS && ownerDocument.mutationEvents
            && (mutationMask&MUTATION_AGGREGATE)!=0) {
            // MUTATION PREPROCESSING
            // No direct pre-events, but if we're within the scope 
            // of an Attr and DOMAttrModified was requested,
            // we need to preserve its previous value.
            LCount lc=LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
            if (lc.captures+lc.bubbles+lc.defaults>0) {
                enclosingAttr=getEnclosingAttr();
            }
        }

        // Convert to internal type, to avoid repeated casting
        ChildNode newInternal = (ChildNode)newChild;

        Node oldparent = newInternal.parentNode();
        if (oldparent != null) {
            oldparent.removeChild(newInternal);
        }

        // Convert to internal type, to avoid repeated casting
        ChildNode refInternal = (ChildNode)refChild;

        // Attach up
        newInternal.ownerNode = this;
        newInternal.isOwned(true);

        // Attach before and after
        // Note: firstChild.previousSibling == lastChild!!
        if (firstChild == null) {
            // this our first and only child
            firstChild = newInternal;
            newInternal.isFirstChild(true);
            newInternal.previousSibling = newInternal;
        }
        else {
            if (refInternal == null) {
                // this is an append
                ChildNode lastChild = firstChild.previousSibling;
                lastChild.nextSibling = newInternal;
                newInternal.previousSibling = lastChild;
                firstChild.previousSibling = newInternal;
            }
            else {
                // this is an insert
                if (refChild == firstChild) {
                    // at the head of the list
                    firstChild.isFirstChild(false);
                    newInternal.nextSibling = firstChild;
                    newInternal.previousSibling = firstChild.previousSibling;
                    firstChild.previousSibling = newInternal;
                    firstChild = newInternal;
                    newInternal.isFirstChild(true);
                }
                else {
                    // somewhere in the middle
                    ChildNode prev = refInternal.previousSibling;
                    newInternal.nextSibling = refInternal;
                    prev.nextSibling = newInternal;
                    refInternal.previousSibling = newInternal;
                    newInternal.previousSibling = prev;
                }
            }
        }

        changed();

        // update cached length if we have any
        if (fCachedLength != -1) {
            fCachedLength++;
        }
        if (fCachedChildIndex != -1) {
            // if we happen to insert just before the cached node, update
            // the cache to the new node to match the cached index
            if (fCachedChild == refInternal) {
                fCachedChild = newInternal;
            } else {
                // otherwise just invalidate the cache
                fCachedChildIndex = -1;
            }
        }

        if (MUTATIONEVENTS && ownerDocument.mutationEvents) {
            // MUTATION POST-EVENTS:
            // "Local" events (non-aggregated)
            if ((mutationMask&MUTATION_LOCAL) != 0) {
                // New child is told it was inserted, and where
                LCount lc = LCount.lookup(MutationEventImpl.DOM_NODE_INSERTED);
                if (lc.captures+lc.bubbles+lc.defaults>0) {
                    MutationEvent me= new MutationEventImpl();
                    me.initMutationEvent(MutationEventImpl.DOM_NODE_INSERTED,
                                         true,false,this,null,
                                          null,null,(short)0);
                    newInternal.dispatchEvent(me);
                }

                // If within the Document, tell the subtree it's been added
                // to the Doc.
                lc=LCount.lookup(
                            MutationEventImpl.DOM_NODE_INSERTED_INTO_DOCUMENT);
                if (lc.captures+lc.bubbles+lc.defaults>0) {
                    NodeImpl eventAncestor=this;
                    if (enclosingAttr!=null) 
                        eventAncestor=
                            (NodeImpl)(enclosingAttr.node.getOwnerElement());
                    if (eventAncestor!=null) { // Might have been orphan Attr
                        NodeImpl p=eventAncestor;
                        while (p!=null) {
                            eventAncestor=p; // Last non-null ancestor
                            // In this context, ancestry includes
                            // walking back from Attr to Element
                            if(p.getNodeType()==ATTRIBUTE_NODE) {
                                p=(ElementImpl)((AttrImpl)p).getOwnerElement();
                            }
                            else {
                                p=p.parentNode();
                            }
                        }
                        if(eventAncestor.getNodeType()==Node.DOCUMENT_NODE) {
                            MutationEvent me= new MutationEventImpl();
                            me.initMutationEvent(MutationEventImpl
                                              .DOM_NODE_INSERTED_INTO_DOCUMENT,
                                                 false,false,null,null,
                                                 null,null,(short)0);
                            dispatchEventToSubtree(newInternal,me);
                        }
                    }
                }
            }

            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified
            // (Common to most kinds of mutation)
            if ((mutationMask&MUTATION_AGGREGATE) != 0) {
                dispatchAggregateEvents(enclosingAttr);
            }
        }

        checkNormalizationAfterInsert(newInternal);

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

        DocumentImpl ownerDocument = ownerDocument();
        if (ownerDocument.errorChecking) {
            if (isReadOnly()) {
                throw new DOMException(
                                     DOMException.NO_MODIFICATION_ALLOWED_ERR, 
                                     "DOM001 Modification not allowed");
            }
            if (oldChild != null && oldChild.getParentNode() != this) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, 
                                       "DOM008 Not found");
            }
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
                    me.initMutationEvent(MutationEventImpl.DOM_NODE_REMOVED,
                                         true,false,this,null,
                                         null,null,(short)0);
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
                            me.initMutationEvent(MutationEventImpl
                                               .DOM_NODE_REMOVED_FROM_DOCUMENT,
                                                 false,false,
                                                 null,null,null,null,(short)0);
                            dispatchEventToSubtree(oldInternal,me);
                        }
                    }
                }
            }
        } // End mutation preprocessing

        // update cached length if we have any
        if (fCachedLength != -1) {
            fCachedLength--;
        }
        if (fCachedChildIndex != -1) {
            // if the removed node is the cached node
            // move the cache to its (soon former) previous sibling
            if (fCachedChild == oldInternal) {
                fCachedChildIndex--;
                fCachedChild = oldInternal.previousSibling();
            } else {
                // otherwise just invalidate the cache
                fCachedChildIndex = -1;
            }
        }

        // Patch linked list around oldChild
        // Note: lastChild == firstChild.previousSibling
        if (oldInternal == firstChild) {
            // removing first child
            oldInternal.isFirstChild(false);
            firstChild = oldInternal.nextSibling;
            if (firstChild != null) {
                firstChild.isFirstChild(true);
                firstChild.previousSibling = oldInternal.previousSibling;
            }
        } else {
            ChildNode prev = oldInternal.previousSibling;
            ChildNode next = oldInternal.nextSibling;
            prev.nextSibling = next;
            if (next == null) {
                // removing last child
                firstChild.previousSibling = prev;
            } else {
                // removing some other child in the middle
                next.previousSibling = prev;
            }
        }

        // Save previous sibling for normalization checking.
        ChildNode oldPreviousSibling = oldInternal.previousSibling();

        // Remove oldInternal's references to tree
        oldInternal.ownerNode       = ownerDocument;
        oldInternal.isOwned(false);
        oldInternal.nextSibling     = null;
        oldInternal.previousSibling = null;

        changed();

        if(MUTATIONEVENTS && ownerDocument.mutationEvents)
        {
            // MUTATION POST-EVENTS:
            // Subroutine: Transmit DOMAttrModified and DOMSubtreeModified,
            // if required. (Common to most kinds of mutation)
            if( (mutationMask&MUTATION_AGGREGATE) != 0)
                dispatchAggregateEvents(enclosingAttr);
        } // End mutation postprocessing

        checkNormalizationAfterRemove(oldPreviousSibling);

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
        if (newChild != oldChild) {
            internalRemoveChild(oldChild,MUTATION_LOCAL);
        }

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
     * Count the immediate children of this node.  Use to implement
     * NodeList.getLength().
     * @return int
     */
    private int nodeListGetLength() {

        if (fCachedLength == -1) { // is the cached length invalid ?
            ChildNode node;
            // start from the cached node if we have one
            if (fCachedChildIndex != -1 && fCachedChild != null) {
                fCachedLength = fCachedChildIndex;
                node = fCachedChild;
            } else {
                node = firstChild;
                fCachedLength = 0;
            }
            for (; node != null; node = node.nextSibling) {
                fCachedLength++;
            }
        }

        return fCachedLength;

    } // nodeListGetLength():int

    /**
     * NodeList method: Count the immediate children of this node
     * @return int
     */
    public int getLength() {
        return nodeListGetLength();
    }

    /**
     * Return the Nth immediate child of this node, or null if the index is
     * out of bounds.  Use to implement NodeList.item().
     * @param index int
     */
    private Node nodeListItem(int index) {
        // short way
        if (fCachedChildIndex != -1 && fCachedChild != null) {
            if (fCachedChildIndex < index) {
                while (fCachedChildIndex < index && fCachedChild != null) {
                    fCachedChildIndex++;
                    fCachedChild = fCachedChild.nextSibling;
                }
            }
            else if (fCachedChildIndex > index) {
                while (fCachedChildIndex > index && fCachedChild != null) {
                    fCachedChildIndex--;
                    fCachedChild = fCachedChild.previousSibling();
                }
            }
            return fCachedChild;
        }

        // long way
        fCachedChild = firstChild;
        for (fCachedChildIndex = 0; 
             fCachedChildIndex < index && fCachedChild != null; 
             fCachedChildIndex++) {
            fCachedChild = fCachedChild.nextSibling;
        }
        return fCachedChild;

    } // nodeListItem(int):Node

    /**
     * NodeList method: Return the Nth immediate child of this node, or
     * null if the index is out of bounds.
     * @return org.w3c.dom.Node
     * @param index int
     */
    public Node item(int index) {
        return nodeListItem(index);
    } // item(int):Node

    /**
     * Create a NodeList to access children that is use by subclass elements
     * that have methods named getLength() or item(int).  ChildAndParentNode
     * optimizes getChildNodes() by implementing NodeList itself.  However if
     * a subclass Element implements methods with the same name as the NodeList
     * methods, they will override the actually methods in this class.
     * <p>
     * To use this method, the subclass should implement getChildNodes() and
     * have it call this method.  The resulting NodeList instance maybe
     * shared and cached in a transient field, but the cached value must be
     * cleared if the node is cloned.
     */
    protected final NodeList getChildNodesUnoptimized() {
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        return new NodeList() {
                /**
                 * @see NodeList.getLength()
                 */
                public int getLength() {
                    return nodeListGetLength();
                } // getLength():int
                
                /**
                 * @see NodeList.item(int)
                 */
                public Node item(int index) {
                    return nodeListItem(index);
                } // item(int):Node
            };
    } // getChildNodesUnoptimized():NodeList

    //
    // DOM2: methods, getters, setters
    //

    /**
     * Override default behavior to call normalize() on this Node's
     * children. It is up to implementors or Node to override normalize()
     * to take action.
     */
    public void normalize() {
        // No need to normalize if already normalized.
        if (isNormalized()) {
            return;
        }
        if (needsSyncChildren()) {
            synchronizeChildren();
        }
        ChildNode kid;
        for (kid = firstChild; kid != null; kid = kid.nextSibling) {
            kid.normalize();
        }
        isNormalized(true);
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
            for (ChildNode mykid = firstChild;
                 mykid != null;
                 mykid = mykid.nextSibling) {
                if (mykid.getNodeType() != Node.ENTITY_REFERENCE_NODE) {
                    mykid.setReadOnly(readOnly,true);
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
        ChildNode first = null;
        ChildNode last = null;
        for (int index = ownerDocument.getLastChild(nodeIndex);
             index != -1;
             index = ownerDocument.getPrevSibling(index)) {

            ChildNode node = (ChildNode)ownerDocument.getNodeObject(index);
            if (last == null) {
                last = node;
            }
            else {
                first.previousSibling = node;
            }
            node.ownerNode = this;
            node.isOwned(true);
            node.nextSibling = first;
            first = node;
        }
        if (last != null) {
            firstChild = first;
            first.isFirstChild(true);
            lastChild(last);
        }

        // set mutation events flag back to its original value
        ownerDocument.mutationEvents = orig;

    } // synchronizeChildren()

    /**
     * Checks the normalized state of this node after inserting a child.
     * If the inserted child causes this node to be unnormalized, then this
     * node is flagged accordingly.
     * The conditions for changing the normalized state are:
     * <ul>
     * <li>The inserted child is a text node and one of its adjacent siblings
     * is also a text node.
     * <li>The inserted child is is itself unnormalized.
     * </ul>
     *
     * @param insertedChild the child node that was inserted into this node
     *
     * @throws NullPointerException if the inserted child is <code>null</code>
     */
    void checkNormalizationAfterInsert(ChildNode insertedChild) {
        // See if insertion caused this node to be unnormalized.
        if (insertedChild.getNodeType() == Node.TEXT_NODE) {
            ChildNode prev = insertedChild.previousSibling();
            ChildNode next = insertedChild.nextSibling;
            // If an adjacent sibling of the new child is a text node,
            // flag this node as unnormalized.
            if ((prev != null && prev.getNodeType() == Node.TEXT_NODE) ||
                (next != null && next.getNodeType() == Node.TEXT_NODE)) {
                isNormalized(false);
            }
        }
        else {
            // If the new child is not normalized,
            // then this node is inherently not normalized.
            if (!insertedChild.isNormalized()) {
                isNormalized(false);
            }
        }
    } // checkNormalizationAfterInsert(ChildNode)

    /**
     * Checks the normalized of this node after removing a child.
     * If the removed child causes this node to be unnormalized, then this
     * node is flagged accordingly.
     * The conditions for changing the normalized state are:
     * <ul>
     * <li>The removed child had two adjacent siblings that were text nodes.
     * </ul>
     *
     * @param previousSibling the previous sibling of the removed child, or
     * <code>null</code>
     */
    void checkNormalizationAfterRemove(ChildNode previousSibling) {
        // See if removal caused this node to be unnormalized.
        // If the adjacent siblings of the removed child were both text nodes,
        // flag this node as unnormalized.
        if (previousSibling != null &&
            previousSibling.getNodeType() == Node.TEXT_NODE) {

            ChildNode next = previousSibling.nextSibling;
            if (next != null && next.getNodeType() == Node.TEXT_NODE) {
                isNormalized(false);
            }
        }
    } // checkNormalizationAfterRemove(Node)

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

        // initialize transients
        fCachedLength = -1;
        fCachedChildIndex = -1;

    } // readObject(ObjectInputStream)

} // class ParentNode
