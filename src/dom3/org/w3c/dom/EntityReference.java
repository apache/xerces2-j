/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.dom;

/** 
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * 
 * <code>EntityReference</code> nodes may be used to represent an entity 
 * reference in the tree. Note that character references and references to 
 * predefined entities are considered to be expanded by the HTML or XML 
 * processor so that characters are represented by their Unicode equivalent 
 * rather than by an entity reference. Moreover, the XML processor may 
 * completely expand references to entities while building the 
 * <code>Document</code>, instead of providing <code>EntityReference</code> 
 * nodes. If it does provide such nodes, then for an 
 * <code>EntityReference</code> node that represents a reference to a known 
 * entity an <code>Entity</code> exists, and the subtree of the 
 * <code>EntityReference</code> node is a copy of the <code>Entity</code> 
 * node subtree. However, the latter may not be true when an entity contains 
 * an unbound namespace prefix. In such a case, because the namespace prefix 
 * resolution depends on where the entity reference is, the descendants of 
 * the <code>EntityReference</code> node may be bound to different namespace 
 * URIs. When an <code>EntityReference</code> node represents a reference to 
 * an unknown entity, its content is empty.
 * <p>As for <code>Entity</code> nodes, <code>EntityReference</code> nodes and 
 * all their descendants are readonly.
 * <p ><b>Note:</b>  The properties [system identifier] and [public 
 * identifier] defined by the Unexpanded Entity Reference Information Item 
 * in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
 *  are accessible through the <code>Entity</code> interface. The property 
 * [all declarations processed] is not accessible through the DOM API. 
 * <p ><b>Note:</b> <code>EntityReference</code> nodes may cause element 
 * content and attribute value normalization problems when, such as in XML 
 * 1.0 and XML Schema, the normalization is be performed after entity 
 * reference are expanded.
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface EntityReference extends Node {
}
