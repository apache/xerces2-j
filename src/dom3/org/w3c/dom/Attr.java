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
 * The <code>Attr</code> interface represents an attribute in an 
 * <code>Element</code> object. Typically the allowable values for the 
 * attribute are defined in a document type definition.
 * <p><code>Attr</code> objects inherit the <code>Node</code> interface, but 
 * since they are not actually child nodes of the element they describe, the 
 * DOM does not consider them part of the document tree. Thus, the 
 * <code>Node</code> attributes <code>parentNode</code>, 
 * <code>previousSibling</code>, and <code>nextSibling</code> have a 
 * <code>null</code> value for <code>Attr</code> objects. The DOM takes the 
 * view that attributes are properties of elements rather than having a 
 * separate identity from the elements they are associated with; this should 
 * make it more efficient to implement such features as default attributes 
 * associated with all elements of a given type. Furthermore, 
 * <code>Attr</code> nodes may not be immediate children of a 
 * <code>DocumentFragment</code>. However, they can be associated with 
 * <code>Element</code> nodes contained within a 
 * <code>DocumentFragment</code>. In short, users and implementors of the 
 * DOM need to be aware that <code>Attr</code> nodes have some things in 
 * common with other objects inheriting the <code>Node</code> interface, but 
 * they also are quite distinct.
 * <p>The attribute's effective value is determined as follows: if this 
 * attribute has been explicitly assigned any value, that value is the 
 * attribute's effective value; otherwise, if there is a declaration for 
 * this attribute, and that declaration includes a default value, then that 
 * default value is the attribute's effective value; otherwise, the 
 * attribute does not exist on this element in the structure model until it 
 * has been explicitly added. Note that the <code>nodeValue</code> attribute 
 * on the <code>Attr</code> instance can also be used to retrieve the string 
 * version of the attribute's value(s).
 * <p>In XML, where the value of an attribute can contain entity references, 
 * the child nodes of the <code>Attr</code> node may be either 
 * <code>Text</code> or <code>EntityReference</code> nodes (when these are 
 * in use; see the description of <code>EntityReference</code> for 
 * discussion). Because the DOM Core is not aware of attribute types, it 
 * treats all attribute values as simple strings, even if the DTD or schema 
 * declares them as having tokenized types.
 * <p> The DOM implementation does not perform any kind of normalization. 
 * While it is expected that the <code>value</code> and 
 * <code>nodeValue</code> attributes of an <code>Attr</code> node would 
 * initially return a normalized value depending on the schema in used, this 
 * may not be the case after mutation. This is true, independently of 
 * whether the mutation is performed by setting the string value directly or 
 * by changing the <code>Attr</code> child nodes. In particular, this is 
 * true when character entity references are involved, given that they are 
 * not represented in the DOM and they impact attribute value normalization. 
 * <p ><b>Note:</b>  The property [references] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
 *  is not accessible from DOM Level 3 Core. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Attr extends Node {
    /**
     * Returns the name of this attribute.
     */
    public String getName();

    /**
     * <code>True</code> if this attribute was explicitly given a value in the 
     * instance document, <code>false</code> otherwise. If the user changes 
     * the value of this attribute node (even if it ends up having the same 
     * value as the default value) then this is set to <code>true</code>. 
     * Removing attributes for which a default value is defined in the DTD 
     * generates a new attribute with the default value and this set to 
     * <code>false</code>. The implementation may handle attributes with 
     * default values from other schemas similarly but applications should 
     * use <code>normalizeDocument()</code> to guarantee this information is 
     * up-to-date.
     * <br> This attribute is based on the property [specified] defined [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     */
    public boolean getSpecified();

    /**
     * On retrieval, the value of the attribute is returned as a string. 
     * Character and general entity references are replaced with their 
     * values. See also the method <code>getAttribute</code> on the 
     * <code>Element</code> interface.
     * <br>On setting, this creates a <code>Text</code> node with the unparsed 
     * contents of the string. I.e. any characters that an XML processor 
     * would recognize as markup are instead treated as literal text. See 
     * also the method <code>setAttribute</code> on the <code>Element</code> 
     * interface.
     * <p ><b>Note:</b>  Some specialized implementations, such as some [<a href='http://www.w3.org/TR/2001/REC-SVG-20010904'>SVG 1.0</a>] 
     * implementations, may do normalization automatically, even after 
     * mutation; in such case, the value on retrieval may differ from the 
     * value on setting. 
     * <br> The <code>value</code> may contain the normalized attribute value 
     * and represents in that case the property [normalized value] defined 
     * in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     */
    public String getValue();
    /**
     * On retrieval, the value of the attribute is returned as a string. 
     * Character and general entity references are replaced with their 
     * values. See also the method <code>getAttribute</code> on the 
     * <code>Element</code> interface.
     * <br>On setting, this creates a <code>Text</code> node with the unparsed 
     * contents of the string. I.e. any characters that an XML processor 
     * would recognize as markup are instead treated as literal text. See 
     * also the method <code>setAttribute</code> on the <code>Element</code> 
     * interface.
     * <p ><b>Note:</b>  Some specialized implementations, such as some [<a href='http://www.w3.org/TR/2001/REC-SVG-20010904'>SVG 1.0</a>] 
     * implementations, may do normalization automatically, even after 
     * mutation; in such case, the value on retrieval may differ from the 
     * value on setting. 
     * <br> The <code>value</code> may contain the normalized attribute value 
     * and represents in that case the property [normalized value] defined 
     * in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     */
    public void setValue(String value)
                        throws DOMException;

    /**
     * The <code>Element</code> node this attribute is attached to or 
     * <code>null</code> if this attribute is not in use.
     * <br> This attribute represents the property [owner element] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @since DOM Level 2
     */
    public Element getOwnerElement();

    /**
     *  The type information associated with this attribute. 
     * @since DOM Level 3
     */
    public TypeInfo getSchemaTypeInfo();

    /**
     * Returns whether this attribute is known to be of type ID or not. When 
     * it is and its value is unique, the <code>ownerElement</code> of this 
     * attribute can be retrieved using <code>Document.getElementById</code>.
     * This translates to getIsId() in Java. Is that ok? changed to be a 
     * method.  How does this relate to <code>schemaTypeInfo</code>? no 
     * relation.
     * @return  <code>true</code> if this attribute is of type ID, 
     *   <code>false</code> otherwise. 
     * @since DOM Level 3
     */
    public boolean isId();

}
