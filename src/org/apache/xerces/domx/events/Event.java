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
 * The <code>Event</code> interface is used to provide contextual information 
 * about an event to the handler processing the event.  An object which 
 * implements the <code>Event</code> interface is generally passed as the 
 * first parameter to an event handler.  More specific  context information 
 * is passed to event handlers by deriving additional interfaces from  
 * <code>Event</code> which contain information directly relating to the type 
 * of event they accompany.  These derived interfaces are also implemented by 
 * the object passed to the event listener. 
 * @since DOM Level 2
 */
public interface Event {
  // PhaseType
  public static final short           BUBBLING_PHASE       = 1;
  public static final short           CAPTURING_PHASE      = 2;
  public static final short           AT_TARGET            = 3;

  /**
   * The <code>bubbles</code> property indicates whether or not an event is a 
   * bubbling event.  If the event can bubble the value is true, else the 
   * value is false. 
   */
  public boolean            getBubbles();  
  /**
   * The <code>cancelable</code> property indicates whether or not an event 
   * can have its default action prevented.  If the default action can be 
   * prevented the value is true, else the value is false. 
   */
  public boolean            getCancelable();  
  /**
   * The <code>currentNode</code> property indicates to which <code>Node</code>
   *  the event is currently being dispatched.  This is particularly useful 
   * during capturing and bubbling. 
   */
  public Node               getCurrentNode();  
  /**
   * The <code>eventPhase</code> property indicates which phase of event flow 
   * is currently  being evaluated. 
   */
  public short              getEventPhase();  
  /**
   * The <code>target</code> property indicates the <code>EventTarget</code> 
   * to which the event  was originally dispatched. 
   */
  public EventTarget        getTarget();  
  /**
   * The <code>type</code> property represents the event name as a string 
   * property. 
   */
  public String             getType();  
  /**
   * 
   * @param eventTypeArg Specifies the event type.
   * @param canBubbleArg Specifies whether or not the event can bubble.
   * @param cancelableArg Specifies whether or not the event's default  action 
   *   can be prevented.
   */
  public void               initEvent(String eventTypeArg, 
									  boolean canBubbleArg, 
									  boolean cancelableArg);  

  // OBSOLTETE
  // public void               preventBubble();  
  // public void               preventCapture();  
  /**
    preventCapture() and preventBubble() were replaced by stopPropigation.
    */
  public void stopPropagation();
  /**
   * If an event is cancelable, the <code>preventCapture</code> method is used 
   * to signify that the event is to be canceled.  If, during any stage of 
   * event flow, the <code>preventDefault</code> method is called the event 
   * is canceled. Any default action associated with the event will not 
   * occur.  Calling this method for a non-cancelable event has no effect. 
   */
  public void               preventDefault();  
}
