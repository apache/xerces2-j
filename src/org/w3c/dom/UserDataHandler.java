/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.w3c.dom;

/**
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * <p>
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * <p>
 * When associating an object to a key on a node using <code>setUserData</code>
 *  the application can provide a handler that gets called when the node the 
 * object is associated to is being cloned or imported. This can be used by 
 * the application to implement various behaviors regarding the data it 
 * associates to the DOM nodes. This interface defines that handler. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface UserDataHandler {
    // OperationType
    /**
     * The node is cloned.
     */
    public static final short NODE_CLONED               = 1;
    /**
     * The node is imported.
     */
    public static final short NODE_IMPORTED             = 2;
    /**
     * The node is deleted.
     */
    public static final short NODE_DELETED              = 3;
    /**
     * The node is renamed.
     */
    public static final short NODE_RENAMED              = 4;

    /**
     * This method is called whenever the node for which this handler is 
     * registered is imported or cloned.
     * @param operation Specifies the type of operation that is being 
     *   performed on the node.
     * @param key Specifies the key for which this handler is being called. 
     * @param data Specifies the data for which this handler is being called. 
     * @param src Specifies the node being cloned, imported, or renamed. This 
     *   is <code>null</code> when the node is being deleted.
     * @param dst Specifies the node newly created if any, or 
     *   <code>null</code>.
     */
    public void handle(short operation, 
                       String key, 
                       Object data, 
                       Node src, 
                       Node dst);

}
