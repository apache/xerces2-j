/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

import org.xml.sax.Locator;

/**
 * 
 * XMLValidator defines the interface that XMLDocumentScanner and XML
 * EntityHandler have with an object that serves as a pluggable validator.
 * This abstraction allows validators for XML grammar languages to be
 * plugged in and queried for validity checks as the scanner processes
 * a document.
 *
 * The document scanner and entity handler need to ask the validator object
 * for this information because the validator object is responsible for reading
 * the grammar specification file (which contains markup declarations and entity
 * declarations)
 *
 * @version
 */

public interface XMLValidator {

    //
    // Scanner/Parserinterfaces
    //  

    /**
     * Check to see if the rootElement matches the root element specified by the DOCTYPE line.
     * Signal an error to the application if it does not.
     * 
     * @param rootElementType StringPool handle for the rootElement
     * @exception java.lang.Exception
     */
    public void rootElementSpecified(int rootElementType) throws Exception;
    /**
     * add an attribute definition for an attribute name attrName to the element
     * elementType.
     *
     * @param elementType the index of the element
     * @param attrList the XMLAttrList to receive the new attribute
     * @param attrName the string pool index of the attribute name
     * @param attrNameLocator a SAX Locator (for error reporting)
     * @param attValue the string pool index of the attribute value 
     * @exception java.lang.Exception
     */
    public boolean attributeSpecified(int elementType, XMLAttrList attrList, int attrName, Locator attrNameLocator, int attValue) throws Exception;
    /**
     * a callback for the start tag of an element
     *
     * @param elementType the index of the element
     * @param attrList the XMLAttrList containing the attributes for the element
     * @return true if processing element content //REVISIT
     * @exception java.lang.Exception
     */
    public boolean startElement(int elementType, XMLAttrList attrList) throws Exception;
    /**
     * a callback for the end tag of an element
     * 
     * @param elementType the index of the element
     * @return true if processing element content //REVISIT
     * @exception java.lang.Exception
     */
    public boolean endElement(int elementType) throws Exception;
    /**
     * a callback for character data - String object version
     *
     * @param chars array containing the characters that were scanned
     * @param offset offset in chars of characters that were scanned
     * @param length length of characters that were scanned
     * @exception java.lang.Exception
     */
    public void characters(char[] chars, int offset, int length) throws Exception;
    /**
     * a callback for character data - string pool version
     *
     * @param stringIndex string pool index of the string that was scanned
     * @exception java.lang.Exception
     */
    public void characters(int stringIndex) throws Exception;
    /**
     * a callback for ignorable whitespace - String object version
     *
     * @param chars array containing the white space that was scanned
     * @param offset offset in chars of the white space that was scanned
     * @param length length of the white space that was scanned
     * @exception java.lang.Exception
     */
    public void ignorableWhitespace(char[] chars, int offset, int length) throws Exception;
    /**
     * a callback for ignorable whitespace - string pool version
     *
     * @param stringIndex - string pool index of the white space that was scanned
     * @exception java.lang.Exception
     */
    public void ignorableWhitespace(int stringIndex) throws Exception;
    //
    // Validation support for XMLEntityHandler
    //
    /**
     * return true if entityIndex's entity is external, and it satisfies
     * the requirements for an external entity reference within content
     *
     * @param entityIndex entity handle
     * @return true if the entity is external
     * @exception java.lang.Exception
     */
    public boolean externalReferenceInContent(int entityIndex) throws Exception;
    /**
     * return the string pool index of an entity
     * the entity must be allowed to appear in an attribute value
     *
     * @param entityIndex entity handle
     * @return string pool index of entity valkue
     * @exception java.lang.Exception
     */
    public int valueOfReferenceInAttValue(int entityIndex) throws Exception;
    //
    // Entity query support for XMLEntityHandler
    //
    /**
     * return a handle to an entity.  This handle can then be passed to the
     * other XMLEntityHandler methods on XMLValidator
     *
     * @param entityName string pool index of entity name
     * @return entity Handle
     * @exception java.lang.Exception
     */
    public int lookupEntity(int entityName) throws Exception;
    /**
     * return true if entityIndex's entity is unparsed 
     *
     * @param entityIndex entity handle
     * @return true if entityIndex's entity is unparsed
     * @exception java.lang.Exception
     */
    public boolean isUnparsedEntity(int entityIndex) throws Exception;
    /**
     * return true if entityIndex's entity is external
     *
     * @param entityIndex entity handle
     * @return true if entityIndex's entity is external
     * @exception java.lang.Exception
     */
    public boolean isExternalEntity(int entityIndex) throws Exception;
    /**
     * return the replacement text for entityIndex
     *
     * @param entityIndex entity handle
     * @return the string pool index of entityIndex's replacement text
     * @exception java.lang.Exception
     */
    public int getEntityValue(int entityIndex) throws Exception;
    /**
     * return the public identifer for entityIndex
     *
     * @param entityIndex entity handle
     * @return String containing entityIndex's public identifier
     * @exception java.lang.Exception
     */
    public String getPublicIdOfEntity(int entityIndex) throws Exception;
    /**
     * return the system identifier for entityIndex
     *
     * @param entityIndex entity handle
     * @return String containing entityIndex' system identifier
     * @exception java.lang.Exception
     */
    public String getSystemIdOfEntity(int entityIndex) throws Exception;
    /**
     * return a handle to a parameter entity - this handle can then
     * be passed to other XMLEntityHandler methods on XMLValidator
     * 
     * @param peName string pool index of a parameter entity name
     * @return handle to the parameter entity named by peName
     * @exception java.lang.Exception
     */
    public int lookupParameterEntity(int peName) throws Exception;
    /**
     * return true if a parameter entity is external
     *
     * @param peIndex parameter entity handle
     * @return true if peIndex's parameter entity is external
     * @exception java.lang.Exception
     */
    public boolean isExternalParameterEntity(int peIndex) throws Exception;
    /**
     * return the replacement text for a parameter entity
     *
     * @param peIndex parameter entity handle
     * @return the string pool index of the replacment text for peIndex's parameter entity
     * @exception java.lang.Exception
    */
    public int getParameterEntityValue(int peIndex) throws Exception;
    /**
     * return the public identifier for a parameter entity
     *
     * @param peIndex parameter entity handle
     * @return a String containing the public identifier for peIndex's parameter entity
     * @exception java.lang.Exception
    */
    public String getPublicIdOfParameterEntity(int peIndex) throws Exception;
    /**
     * return the system identifier for a parameter entity
     *
     * @param peIndex parameter entity handle
     * @returna String containing the system identifier for peIndex's parameter entity
     * @exception java.lang.Exception
    */
    public String getSystemIdOfParameterEntity(int peIndex) throws Exception;
    //
    // Content model support
    //
    /**
     * ContentSpec really exists to aid the parser classes in implementing access
     * to the grammar
     */
    public interface ContentSpec {
        /**
         * return this ContentSpec as a string
         *
         * @return the content spec as a string
         */
        public String toString();
        /**
         * return the type of this ContentSpec
         *
         * @return The string pool handle for the type of this ContentSpec
         */
        public int getType();
        /**
         * get this ContentSpec's handle
         *
         * @return handle for this ContentSpec
         */
        public int getHandle();
        /**
         * fill in XMLContentSpecNode node with the information for
         * ContentSpec in handle
         * 
         * @param handle ContentSpec handle
         * @param node result XMLContentSpecNode
         */
        public void getNode(int handle, XMLContentSpecNode node);
    }
    //
    // REVISIT - used only by the revalidating DOM parser...
    //
    /**
     * validate an element's content 
     *
     * @param elementHandle handle of the element to be validated
     * @param childCount the number of children to be checked
     * @param children an array containing the indices of the children to be checked
     * @return -1 if valid, otherwise the index of the child that caused the content to be invalid
     * @exception java.lang.Exception
     */
    public int checkContent(int elementHandle, int childCount, int[] children) throws Exception;
    /**
     * return elementIndex's content spec as a string
     * 
     * @param elementIndex element handle
     * @return String containing the content spec for elementIndex
     */
    public String getContentSpecAsString(int elementIndex);
}
