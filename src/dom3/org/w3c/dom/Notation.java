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
 * This interface represents a notation declared in the DTD. A notation either 
 * declares, by name, the format of an unparsed entity (see section 4.7 of 
 * the XML 1.0 specification ), or is used for formal declaration of 
 * processing instruction targets (see section 2.6 of the XML 1.0 
 * specification ). The <code>nodeName</code> attribute inherited from 
 * <code>Node</code> is set to the declared name of the notation.
 * <p>The DOM Level 1 does not support editing <code>Notation</code> nodes; 
 * they are therefore readonly.
 * <p>A <code>Notation</code> node does not have any parent.adds a 
 * namespaceURI for notations?No. 1- notations are attached to a 
 * <code>DocumentType</code>. 2- what would be the key for notations in 
 * namednodemap? 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Notation extends Node {
    /**
     * The public identifier of this notation. If the public identifier was 
     * not specified, this is <code>null</code>.
     * <br> This attribute represents the property [public identifier] defined 
     * by the Notation Information Item in . 
     */
    public String getPublicId();

    /**
     * The system identifier of this notation. If the system identifier was 
     * not specified, this is <code>null</code>. This may be an absolute URI 
     * or not.
     * <br> This attribute represents the property [system identifier] defined 
     * by the Notation Information Item in . 
     */
    public String getSystemId();

}
