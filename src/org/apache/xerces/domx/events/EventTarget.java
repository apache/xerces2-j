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

import org.w3c.dom.DOMException;

/**
 * The <code>EventTarget</code> interface is implemented by all 
 * <code>Node</code>s in  an implementation which supports the DOM Event 
 * Model.  The interface allows registration and removal of 
 * <code>EventListener</code>s on an <code>EventTarget</code> and dispatch of 
 * events to that <code>EventTarget</code>.
 * @since DOM Level 2
 */
public interface EventTarget {
  /**
   * This method allows the registration of event listeners on the event 
   * target.  
   * @param type The event type for which the user is registering
   * @param listener The <code>listener</code> parameter takes an interface 
   *   implemented by the user which contains the methods to be called when 
   *   the event occurs.
   * @param useCapture If true, <code>useCapture</code> indicates that the 
   *   user wishes to initiate capture.  After initiating capture, all events 
   *   of the specified type will be  dispatched to the registered 
   *   <code>EventListener</code> before being dispatched to any 
   *   <code>EventTarget</code>s beneath them in the tree.  Events which are 
   *   bubbling upward through the tree will not trigger an 
   *   <code>EventListener</code> designated to use capture.
   */
  public void               addEventListener(String type, 
											 EventListener listener, 
											 boolean useCapture); 
  /**
   * This method allows the dispatch of events into the implementations event 
   * model.  Events dispatched in this manner will have the same capturing 
   * and bubbling behavior as events dispatched directly by the 
   * implementation.  The target of the event is the <code> EventTarget</code>
   *  on which <code>dispatchEvent</code> is called. 
   * @param evt Specifies the event type, behavior, and contextual information 
   *   to be used in processing the event.
   * @return The return value of <code>dispatchEvent</code> indicates whether 
   *   any of the listeners which handled the event called 
   *   <code>preventDefault</code>.  If <code>preventDefault</code> was 
   *   called the value is false, else the value is true. 
   * @exception DOMException
   *   UNSPECIFIED_EVENT_TYPE: Raised if the <code>Event</code>'s type was 
   *   not specified before <code>dispatchEvent</code> was called.
   */
  public boolean            dispatchEvent(Event evt)
										  throws DOMException;  
  /**
   * This method allows the removal of event listeners from the event target.  
   * If an <code>EventListener</code> is removed from an 
   * <code>EventTarget</code> while it is  processing an event, it will 
   * complete its current actions but will not be triggered again during any 
   * later stages of event flow. 
   * @param type Specifies the event type of the <code>EventListener</code> 
   *   being removed. 
   * @param listener The <code>EventListener</code> parameter indicates the 
   *   <code>EventListener </code> to be removed. 
   * @param useCapture Specifies whether the <code>EventListener</code> being 
   *   removed is a capturing listener or not. 
   */
  public void               removeEventListener(String type, 
												EventListener listener, 
												boolean useCapture);
}
