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
 *  The <code>TypeInfo</code> interface represent a type referenced from 
 * <code>Element</code> or <code>Attr</code> nodes, specified in the schemas 
 * associated with the document. The type is a pair of a namespace URI and 
 * name properties, and depends on the document's schema.  should you be 
 * able to return <code>null</code> on <code>typeName</code>? for anonymous 
 * type? for undeclared elements/attributes? Can schemaType be 
 * <code>null</code>?  schemaTypeInfo can never be null [f2f October 2002]. 
 * <p> If the document's schema is an XML DTD [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>], the values 
 * are computed as follows: 
 * <ul>
 * <li> If this type is referenced from an 
 * <code>Attr</code> node, <code>typeNamespace</code> is <code>null</code> 
 * and <code>typeName</code> represents the [attribute type] property in the [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
 * . If there is no declaration for the attribute, <code>typeName</code> is 
 * <code>null</code>.  Unlike for XML Schema, the name contain the declared 
 * type, and does not relate to "validity". Resolved using Elena's proposal.
 * </li>
 * <li> 
 * If this type is referenced from an <code>Element</code> node, the 
 * <code>typeNamespace</code> and <code>typeName</code> are <code>null</code>
 * . 
 * </li>
 * </ul>
 * <p> If the document's schema is an XML Schema [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>]
 * , the values are computed as follows using the post-schema-validation 
 * infoset contributions (also called PSVI contributions): 
 * <ul>
 * <li> If the 
 * [validity] property exists AND is <em>"invalid"</em> or <em>"notKnown"</em>: the {target namespace} and {name} properties of the declared type if 
 * available, otherwise <code>null</code>. 
 * <p ><b>Note:</b>  At the time of writing, the XML Schema specification does 
 * not require exposing the declared type. Thus, DOM implementations might 
 * choose not to provide type information if validity is not valid. 
 * </li>
 * <li> If the 
 * [validity] property exists and is <em>"valid"</em>: 
 * <ol>
 * <li> If [member type definition] exists, then expose the {target namespace} 
 * and {name} properties of the [member type definition] property; 
 * </li>
 * <li> If the 
 * [member type definition namespace] and the [member type definition name] 
 * exist, then expose these properties. 
 * </li>
 * <li> If the [type definition] property 
 * exists, then expose the {target namespace} and {name} properties of the 
 * [type definition] property; 
 * </li>
 * <li> If the [type definition namespace] and the 
 * [type definition name] exist, then expose these properties. 
 * </li>
 * </ol>
 * </li>
 * </ul>
 * <p ><b>Note:</b>  At the time of writing, the XML Schema specification does 
 * not define how to expose anonymous types. If future specifications define 
 * how to expose anonymous types, DOM implementations can expose anonymous 
 * types via <code>typeName</code> and <code>typeNamespace</code> 
 * parameters. 
 * <p ><b>Note:</b>  Other schema languages are outside the scope of the W3C 
 * and therefore should define how to represent their type systems using 
 * <code>TypeInfo</code>. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
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
