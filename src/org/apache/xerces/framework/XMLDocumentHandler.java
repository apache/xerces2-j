/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.framework;

import org.apache.xerces.utils.QName;

/**
 * XMLValidator defines the interface that XMLDocumentScanner and XML
 * EntityHandler have with an object that serves as a pluggable validator.
 * This abstraction allows validators for XML grammar languages to be
 * plugged in and queried for validity checks as the scanner processes
 * a document.
 * <p>
 * The document scanner and entity handler need to ask the validator object
 * for this information because the validator object is responsible for reading
 * the grammar specification file (which contains markup declarations and entity
 * declarations)
 *
 * @version $Id$
 */

public interface XMLDocumentHandler {

    /**
     * Callback for start of document
     *
     * If the there is no version info, encoding info, or standalone info,
     * the corresponding argument will be set to -1.
     *
     * @exception java.lang.Exception
     */
    public void startDocument()  throws Exception;

    /**
     * callback for the end of document.
     *
     * @exception java.lang.Exception
     */
    public void endDocument() throws Exception;

    /**
     * Signal the XML declaration of a document
     *
     * @param version the handle in the string pool for the version number
     * @param encoding the handle in the string pool for the encoding
     * @param standalong the handle in the string pool for the standalone value
     * @exception java.lang.Exception
     */
    public void xmlDecl(int version, int encoding, int standalone) throws Exception;

    /**
     * Signal the Text declaration of an external entity.
     *
     * @exception java.lang.Exception
     */
    public void textDecl(int version, int encoding) throws Exception;

    /**
     * callback for the start of a namespace declaration scope.
     *
     * @param prefix string pool index of the namespace prefix being declared
     * @param uri string pool index of the namespace uri begin bound
     * @param java.lang.Exception
     */
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception;

    /**
     * callback for the end a namespace declaration scope.
     *
     * @param prefix string pool index of the namespace prefix being declared
     * @exception java.lang.Exception
     */
    public void endNamespaceDeclScope(int prefix) throws Exception;

    /**
     * callback for the start of element.
     *
     * @param elementType element handle for the element being scanned
     * @param attrList attrList containing the attributes of the element
     * @param attrListHandle handle into attrList.  Allows attributes to be retreived.
     * @exception java.lang.Exception
     */
    public void startElement(QName element, 
                             XMLAttrList attrList, int attrListHandle) throws Exception;

    /**
     * callback for end of element.
     *
     * @param elementType element handle for the element being scanned
     * @exception java.lang.Exception
     */
    public void endElement(QName element) throws Exception;

    /**
     * callback for start of entity reference.
     *
     * @param entityName string pool index of the entity name
     * @param entityType the XMLEntityHandler.ENTITYTYPE_* type
     * @see org.apache.xerces.readers.XMLEntityHandler
     * @param entityContext the XMLEntityHandler.ENTITYREF_* type for where
     *        the entity reference appears
     * @see org.apache.xerces.readers.XMLEntityHandler
     * @exception java.lang.Exception
     */
    public void startEntityReference(int entityName, int entityType, int entityContext) throws Exception;

    /**
     * callback for end of entity reference.
     *
     * @param entityName string pool index of the entity anem
     * @param entityType the XMLEntityHandler.ENTITYTYPE_* type
     * @see org.apache.xerces.readers.XMLEntityHandler
     * @param entityContext the XMLEntityHandler.ENTITYREF_* type for where
     *        the entity reference appears
     * @see org.apache.xerces.readers.XMLEntityHandler
     * @exception java.lang.Exception
     */
    public void endEntityReference(int entityName, int entityType, int entityContext) throws Exception;

    /**
     * callback for processing instruction.
     *
     * @param target string pool index of the PI target
     * @param data string pool index of the PI data
     * @exception java.lang.Exception
     */
    public void processingInstruction(int target, int data) throws Exception;

    /**
     * callback for comment.
     *
     * @param comment string pool index of the comment text
     * @exception java.lang.Exception
     */
    public void comment(int comment) throws Exception;

    /**
     * callback for characters (string pool form).
     *
     * @param data string pool index of the characters that were scanned
     * @exception java.lang.Exception
     */
    public void characters(int data) throws Exception;

    /**
     * callback for characters.
     *
     * @param ch character array containing the characters that were scanned
     * @param start offset in ch where scanned characters begin
     * @param length length of scanned characters in ch
     * @exception java.lang.Exception
     */
    public void characters(char ch[], int start, int length) throws Exception;

    /**
     * callback for ignorable whitespace.
     *
     * @param data string pool index of ignorable whitespace
     * @exception java.lang.Exception
     */
    public void ignorableWhitespace(int data) throws Exception;

    /**
     * callback for ignorable whitespace.
     *
     * @param ch character array containing the whitespace that was scanned
     * @param start offset in ch where scanned whitespace begins
     * @param length length of scanned whitespace in ch
     * @exception java.lang.Exception
     */
    public void ignorableWhitespace(char ch[], int start, int length) throws Exception;

    /**
     * callback for start of CDATA section.
     * this callback marks the start of a CDATA section
     *
     * @exception java.lang.Exception
     */
    public void startCDATA() throws Exception;

    /**
     * callback for end of CDATA section.
     * this callback marks the end of a CDATA section
     *
     * @exception java.lang.Exception
     */
    public void endCDATA() throws Exception;

    //
    // Additional DTD specific events.
    //
    public interface DTDHandler {
        /**
         * callback for the start of the DTD
         * This function will be called when a &lt;!DOCTYPE...&gt; declaration is
         * encountered.
         *
         * @param rootElementType element handle for the root element of the document
         * @param publicId string pool index of the DTD's public ID
         * @param systemId string pool index of the DTD's system ID
         * @exception java.lang.Exception
         */
        public void startDTD(QName rootElement, int publicId, int systemId) throws Exception;

        /**
         * Supports DOM Level 2 internalSubset additions.
         * Called when the internal subset is completely scanned.
         */
        public void internalSubset(int internalSubset) throws Exception;

        /**
         * Signal the Text declaration of an external entity.
         *
         * @exception java.lang.Exception
         */
        public void textDecl(int version, int encoding) throws Exception;

        /**
         * callback for the end of the DTD
         * This function will be called at the end of the DTD. 
         */
        public void endDTD() throws Exception;

        /**
         * callback for an element declaration. 
         *
         * @param elementType element handle of the element being declared
         * @param contentSpec contentSpec for the element being declared
         * @see org.apache.xerces.framework.XMLContentSpec
         * @exception java.lang.Exception
         */
        public void elementDecl(QName elementDecl, XMLContentSpec contentSpec) throws Exception;

        /**
         * callback for an attribute list declaration. 
         *
         * @param elementType element handle for the attribute's element
         * @param attrName string pool index of the attribute name
         * @param attType type of attribute
         * @param enumString String representing the values of the enumeration,
         *        if the attribute is of enumerated type, or null if it is not.
         * @param attDefaultType an integer value denoting the DefaultDecl value
         * @param attDefaultValue string pool index of this attribute's default value 
         *        or -1 if there is no defaultvalue 
         * @exception java.lang.Exception
         */
        public void attlistDecl(QName elementDecl, QName attributeDecl,
                                int attType, String enumString,
                                int attDefaultType,
                                int attDefaultValue) throws Exception;

        /**
         * callback for an internal parameter entity declaration.
         *
         * @param entityName string pool index of the entity name
         * @param entityValue string pool index of the entity replacement text
         * @exception java.lang.Exception
         */
        public void internalPEDecl(int entityName, int entityValue) throws Exception;

        /**
         * callback for an external parameter entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @exception java.lang.Exception
         */
        public void externalPEDecl(int entityName, int publicId, int systemId) throws Exception;

        /**
         * callback for internal general entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param entityValue string pool index of the entity replacement text
         * @exception java.lang.Exception
         */
        public void internalEntityDecl(int entityName, int entityValue) throws Exception;

        /**
         * callback for external general entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @exception java.lang.Exception
         */
        public void externalEntityDecl(int entityName, int publicId, int systemId) throws Exception;

        /**
         * callback for an unparsed entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @param notationName string pool index of the notation name.
         * @exception java.lang.Exception
         */
        public void unparsedEntityDecl(int entityName, int publicId, int systemId, int notationName) throws Exception;

        /**
         * callback for a notation declaration.
         *
         * @param notationName string pool index of the notation name
         * @param publicId string pool index of the notation's public id.
         * @param systemId string pool index of the notation's system id.
         * @exception java.lang.Exception
         */
        public void notationDecl(int notationName, int publicId, int systemId) throws Exception;

        /**
         * Callback for processing instruction in DTD.  
         *
         * @param target the string pool index of the PI's target
         * @param data the string pool index of the PI's data
         * @exception java.lang.Exception
         */
        public void processingInstruction(int targetIndex, int dataIndex) throws Exception;

        /**
         * Callback for comment in DTD.
         * 
         * @param comment the string pool index of the comment text
         * @exception java.lang.Exception
         */
        public void comment(int dataIndex) throws Exception;
    }
}
