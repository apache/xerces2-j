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
 *  The <code>TypeInfo</code> interface represent a type referenced from 
 * <code>Element</code> or <code>Attr</code> nodes, specified in the schemas 
 * associated with the document. The type is a pair of a namespace URI and 
 * name properties, and depends on the document's schema.  should you be 
 * able to return <code>null</code> on <code>name</code>? for anonymous 
 * type? for undeclared elements/attributes? Can schemaType be 
 * <code>null</code>? 
 * <p> If the document's schema is an XML DTD [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>], the values are computed as 
 * follows:  If this type is referenced from an <code>Attr</code> node, 
 * <code>namespace</code> is <code>null</code> and <code>name</code> 
 * represents the [attribute type] property in the [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]. If there is no 
 * declaration for the attribute, <code>name</code> is <code>null</code>.  
 * Unlike for XML Schema, the name contain the declared type, and does not 
 * relate to "validity".  If this type is referenced from an 
 * <code>Element</code> node, the <code>namespace</code> and 
 * <code>name</code> are <code>null</code>. 
 * <p> 
 * If the document's schema is an XML Schema [XML Schema Part 1], the
 * values are computed as follows (for definitions see Post-Schema
 * Validation infoset):
 * <p>If the [validity] property exists AND is "invalid" or "notKnown":
 * the {target namespace} and {name} properties of the declared type 
 * if available, otherwise null.
 * <p>Note: At the time of writing, the XML Schema specification does not 
 * require exposing the declared type. Thus, DOM implementations
 * might choose not to provide type information if validity is not valid.
 * <p>If the [validity] property exists and is "valid" the name and namespace
 * computed as follows:
 * <p>a) If [member type definition] exists, then expose the 
 * {target namespace} and {name} properties of the 
 * [member type definition] property;
 * <p>b) If the [member type definition namespace] and the 
 * [member type definition name] exist, then expose these properties.
 * <p>c) If the [type definition] property exists, then expose the {target
 * namespace} and {name} properties of the [type definition] property;
 * <p>d) If the [type definition namespace] and the [type definition name]
 * exist, then expose these properties.
 * 
 * <p> Note: At the time of writing, the XML Schema specification does not
 * define how to expose annonimous types. If future specifications
 * define how to expose annonimous types, DOM implementations can expose
 * annonimous types via "name" and "namespace" parameters.
 * 
 * Other schema languages are outside the scope of the 
 * W3C and therefore should define how to represent their type systems using 
 * <code>TypeInfo</code>. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20021022'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
 */
public interface TypeInfo {
    /**
     *  The name of a type declared for the associated element or attribute, 
     * or <code>null</code> if unknown. Implementations may also use 
     * <code>null</code> to represent XML Schema anonymous types. "name" 
     * seems too generic and may conflict. shoud we rename it?
     */
    public String getTypeName();

    /**
     *  The namespace of the type declared for the associated element or 
     * attribute or <code>null</code> if the element does not have 
     * declaration or if no namespace information is available. 
     * Implementations may also use <code>null</code> to represent XML 
     * Schema anonymous types. "namespace" seems too generic and may 
     * conflict. shoud we rename it?
     */
    public String getTypeNamespace();

}
