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
package org.apache.xerces.domx.events;

import org.w3c.dom.Node;

/**
 * The <code>MutationEvent</code> interface provides specific contextual  
 * information associated with Mutation events. 
 * @since DOM Level 2
 */
public interface MutationEvent extends Event {
  /**
   *  <code>attrName</code> indicates the changed attr in the attrModified 
   * event. 
   */
  public String             getAttrName();  
  /**
   *  <code>newValue</code> indicates the new value of text nodes and 
   * attributes in attrModified and charDataModified events. 
   */
  public String             getNewValue();  
  /**
   *  <code>prevValue</code> indicates the previous value of text nodes and 
   * attributes in attrModified and charDataModified events. 
   */
  public String             getPrevValue();  
  /**
   *  <code>relatedNode</code> is used to identify a secondary node related to 
   * a mutation event. For example, if a mutation event is dispatched to a 
   * node indicating that its parent has changed, the <code>relatedNode</code>
   *  is the changed parent.  If an event is instead dispatch to a subtree 
   * indicating a node was changed within it, the <code>relatedNode</code> is 
   * the changed node. 
   */
  public Node               getRelatedNode();  
  /**
   * 
   * @param typeArg Specifies the event type.
   * @param canBubbleArg Specifies whether or not the event can bubble.
   * @param cancelableArg Specifies whether or not the event's default  action 
   *   can be prevent.
   * @param relatedNodeArg Specifies the <code>Event</code>'s related Node
   * @param prevValueArg Specifies the <code>Event</code>'s 
   *   <code>prevValue</code> property
   * @param newValueArg Specifies the <code>Event</code>'s 
   *   <code>newValue</code> property
   * @param attrNameArg Specifies the <code>Event</code>'s 
   *   <code>attrName</code> property
   */
  public void               initMutationEvent(String typeArg, 
											  boolean canBubbleArg, 
											  boolean cancelableArg, 
											  Node relatedNodeArg, 
											  String prevValueArg, 
											  String newValueArg, 
											  String attrNameArg);  
}
