/*
 * Copyright (c) 1999 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See W3C License http://www.w3.org/Consortium/Legal/ for more
 * details.
 */

package org.w3c.dom.events;

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
    public static final short   CAPTURING_PHASE           = 1;
    public static final short   AT_TARGET                 = 2;
    public static final short   BUBBLING_PHASE            = 3;

    /**
     * The <code>type</code> property represents the event name as a string 
     * property. The string must be an XML name.
     */
    public String       getType();
    /**
     * The <code>target</code> property indicates the <code>EventTarget</code> 
     * to which the event  was originally dispatched. 
     */
    public EventTarget  getTarget();
    /**
     * The <code>currentNode</code> property indicates the <code>Node</code> 
     * whose <code>EventListeners</code> are currently being processed.  This 
     * is particularly useful during capturing and bubbling. 
     */
    public Node         getCurrentNode();
    /**
     * The <code>eventPhase</code> property indicates which phase of event 
     * flow is currently  being evaluated. 
     */
    public short        getEventPhase();
    /**
     * The <code>bubbles</code> property indicates whether or not an event is 
     * a bubbling event.  If the event can bubble the value is true, else the 
     * value is false. 
     */
    public boolean      getBubbles();
    /**
     * The <code>cancelable</code> property indicates whether or not an event 
     * can have its default action prevented.  If the default action can be 
     * prevented the value is true, else the value is false. 
     */
    public boolean      getCancelable();
    /**
     * The <code>stopPropagation</code> method is used prevent further 
     * propagation of an event during event flow. If this method is called by 
     * any <code>EventListener</code> the event will cease propagating 
     * through the tree.  The event will complete dispatch to all listeners 
     * on the current <code>EventTarget</code> before event flow stops.  This 
     * method may be used during any stage of event flow.
     */
    public void         stopPropagation();
    /**
     * If an event is cancelable, the <code>preventDefault</code> method is 
     * used to signify that the event is to be canceled, meaning any default 
     * action normally taken by the implementation as a result of the event 
     * will not occur.  If, during any stage of event flow, the 
     * <code>preventDefault</code> method is called the event is canceled. 
     * Any default action associated with the event will not occur.  Calling 
     * this method for a non-cancelable event has no effect.  Once 
     * <code>preventDefault</code> has been called it will remain in effect 
     * throughout the remainder of the event's propagation.  This method may 
     * be used during any stage of event flow. 
     */
    public void         preventDefault();
    /**
     * The <code>initEvent</code> method is used to initialize the value of an 
     * <code>Event</code> created through the <code>DocumentEvent</code> 
     * interface.  This method may only be called before the 
     * <code>Event</code> has been dispatched via the 
     * <code>dispatchEvent</code> method, though it may be called multiple 
     * times during that phase if necessary.  If called multiple times the 
     * final invocation takes precedence.  If called from a subclass of 
     * <code>Event</code> interface only the values specified in the 
     * <code>initEvent</code> method are modified, all other properties are 
     * left unchanged.
     * @param eventTypeArg Specifies the event type.  This type may be any 
     *   event type currently defined in this specification or a new event 
     *   type.. The string must be an XML name. Any new event type must not 
     *   begin with any upper, lower, or mixed case version  of the string 
     *   "DOM".  This prefix is reserved for future DOM event sets.
     * @param canBubbleArg Specifies whether or not the event can bubble.
     * @param cancelableArg Specifies whether or not the event's default  
     *   action can be prevented.
     */
    public void         initEvent(String eventTypeArg, 
                                  boolean canBubbleArg, 
                                  boolean cancelableArg);
}

