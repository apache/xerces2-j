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
 * The <code>DOMImplementation</code> interface provides a number of methods 
 * for performing operations that are independent of any particular instance 
 * of the document object model.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMImplementation {
    /**
     * Test if the DOM implementation implements a specific feature.
     * @param feature The name of the feature to test (case-insensitive). The 
     *   values used by DOM features are defined throughout the DOM Level 3 
     *   specifications and listed in the  section. The name must be an XML 
     *   name. To avoid possible conflicts, as a convention, names referring 
     *   to features defined outside the DOM specification should be made 
     *   unique.
     * @param version This is the version number of the feature to test. In 
     *   Level 3, the string can be either "3.0", "2.0" or "1.0". If the 
     *   version is <code>null</code> or empty string, supporting any 
     *   version of the feature causes the method to return <code>true</code>
     *   .
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
    public boolean hasFeature(String feature, 
                              String version);

    /**
     * Creates an empty <code>DocumentType</code> node. Entity declarations 
     * and notations are not made available. Entity reference expansions and 
     * default attribute additions do not occur. It is expected that a 
     * future version of the DOM will provide a way for populating a 
     * <code>DocumentType</code>.
     * @param qualifiedName The qualified name of the document type to be 
     *   created.
     * @param publicId The external subset public identifier.
     * @param systemId The external subset system identifier.
     * @return A new <code>DocumentType</code> node with 
     *   <code>Node.ownerDocument</code> set to <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed.
     *   <br>NOT_SUPPORTED_ERR: May be raised by DOM implementations which do 
     *   not support the <code>"XML"</code> feature, if they choose not to 
     *   support this method. Other features introduced in the future, by 
     *   the DOM WG or in extensions defined by other groups, may also 
     *   demand support for this method; please consult the definition of 
     *   the feature to see if it requires this method. 
     * @since DOM Level 2
     */
    public DocumentType createDocumentType(String qualifiedName, 
                                           String publicId, 
                                           String systemId)
                                           throws DOMException;

    /**
     * Creates a DOM Document object of the specified type with its document 
     * element.
     * <br>Note that based on the <code>DocumentType</code> given to create 
     * the document, the implementation may instantiate specialized 
     * <code>Document</code> objects that support additional features than 
     * the "Core", such as "HTML" . On the other hand, setting the 
     * <code>DocumentType</code> after the document was created makes this 
     * very unlikely to happen. Alternatively, specialized 
     * <code>Document</code> creation methods, such as 
     * <code>createHTMLDocument</code> , can be used to obtain specific 
     * types of <code>Document</code> objects.
     * @param namespaceURI The namespace URI of the document element to 
     *   create or <code>null</code>.
     * @param qualifiedName The qualified name of the document element to be 
     *   created or <code>null</code>.
     * @param doctype The type of document to be created or <code>null</code>.
     *   When <code>doctype</code> is not <code>null</code>, its 
     *   <code>Node.ownerDocument</code> attribute is set to the document 
     *   being created.
     * @return A new <code>Document</code> object with its document element. 
     *   If the <code>NamespaceURI</code>, <code>qualifiedName</code>, and 
     *   <code>doctype</code> are <code>null</code>, the returned 
     *   <code>Document</code> is empty with no document element.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name 
     *   contains an illegal character.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is 
     *   malformed, if the <code>qualifiedName</code> has a prefix and the 
     *   <code>namespaceURI</code> is <code>null</code>, or if the 
     *   <code>qualifiedName</code> is <code>null</code> and the 
     *   <code>namespaceURI</code> is different from <code>null</code>, or 
     *   if the <code>qualifiedName</code> has a prefix that is "xml" and 
     *   the <code>namespaceURI</code> is different from "
     *   http://www.w3.org/XML/1998/namespace" , or if the DOM 
     *   implementation does not support the <code>"XML"</code> feature but 
     *   a non-null namespace URI was provided, since namespaces were 
     *   defined by XML.
     *   <br>WRONG_DOCUMENT_ERR: Raised if <code>doctype</code> has already 
     *   been used with a different document or was created from a different 
     *   implementation.
     *   <br>NOT_SUPPORTED_ERR: May be raised by DOM implementations which do 
     *   not support the "XML" feature, if they choose not to support this 
     *   method. Other features introduced in the future, by the DOM WG or 
     *   in extensions defined by other groups, may also demand support for 
     *   this method; please consult the definition of the feature to see if 
     *   it requires this method. 
     * @since DOM Level 2
     */
    public Document createDocument(String namespaceURI, 
                                   String qualifiedName, 
                                   DocumentType doctype)
                                   throws DOMException;

    /**
     * This method makes available a <code>DOMImplementation</code>'s 
     * specialized interface (see ).
     * @param feature The name of the feature requested (case-insensitive).
     * @return Returns an alternate <code>DOMImplementation</code> which 
     *   implements the specialized APIs of the specified feature, if any, 
     *   or <code>null</code> if there is no alternate 
     *   <code>DOMImplementation</code> object which implements interfaces 
     *   associated with that feature. Any alternate 
     *   <code>DOMImplementation</code> returned by this method must 
     *   delegate to the primary core <code>DOMImplementation</code> and not 
     *   return results inconsistent with the primary 
     *   <code>DOMImplementation</code>
     * @since DOM Level 3
     */
    public DOMImplementation getInterface(String feature);

}
