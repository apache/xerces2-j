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

package org.w3c.dom;

/**
 * The <code>DOMImplementation</code> interface provides a number of methods 
 * for performing operations that are independent of any particular instance 
 * of the document object model.
 */
public interface DOMImplementation {
    /**
     * Test if the DOM implementation implements a specific feature.
     * @param feature The string of the feature to test (case-insensitive). The
     *    legal values are defined throughout this specification. The string 
     *   must be an XML name (see also ).
     * @param version This is the version number of the feature to test. In 
     *   Level 2, this is the string "2.0". If the version is not specified, 
     *   supporting any version of the feature will cause the method to 
     *   return <code>true</code>.
     * @return <code>true</code> if the feature is implemented in the 
     *   specified version, <code>false</code> otherwise.
     */
    public boolean      hasFeature(String feature, 
                                   String version);
    /**
     * Creates an empty <code>DocumentType</code> node. Entity declarations 
     * and notations are not made available. Entity reference expansions and 
     * default attribute additions do not occur. It is expected that a future 
     * version of the DOM will provide a way for populating a 
     * <code>DocumentType</code>.
     * <br>HTML-only DOM implementations do not need to implement this method.
     * @since DOM Level 2
     * @param qualifiedName The qualified name of the document type to be 
     *   created. 
     * @param publicId The external subset public identifier.
     * @param systemId The external subset system identifier.
     * @param internalSubset The internal subset as a string. This should be 
     *   valid.
     * @return A new <code>DocumentType</code> node with 
     *   <code>Node.ownerDocument</code> set to <code>null</code>.
     */
    public DocumentType createDocumentType(String qualifiedName, 
                                           String publicId, 
                                           String systemId, 
                                           String internalSubset);
    /**
     * Creates an XML <code>Document</code> object of the specified type with 
     * its document element. HTML-only DOM implementations do not need to 
     * implement this method.
     * @since DOM Level 2
     * @param namespaceURI The namespace URI of the document element to 
     *   create, or <code>null</code>.
     * @param qualifiedName The qualified name of the document element to be 
     *   created.
     * @param doctype The type of document to be created or <code>null</code>.
     *   When <code>doctype</code> is not <code>null</code>, its 
     *   <code>Node.ownerDocument</code> attribute is set to the document 
     *   being created.
     * @return A new <code>Document</code> object.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>doctype</code> has already been 
     *   used with a different document.
     */
    public Document     createDocument(String namespaceURI, 
                                       String qualifiedName, 
                                       DocumentType doctype)
                                       throws DOMException;
}

