/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.util;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;

/**
 * The XMLAttributesImpl class is an implementation of the XMLAttributes
 * interface which defines a collection of attributes for an element. 
 * In the parser, the document source would scan the entire start element 
 * and collect the attributes. The attributes are communicated to the
 * document handler in the startElement method.
 * <p>
 * The attributes are read-write so that subsequent stages in the document
 * pipeline can modify the values or change the attributes that are
 * propogated to the next stage.
 *
 * @see XMLDocumentHandler#startElement
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class XMLAttributesImpl
    implements XMLAttributes {

    //
    // Data
    //

    // features

    /** Namespaces. */
    protected boolean fNamespaces = true;

    // data

    /** Attribute count. */
    protected int fLength;

    /** Attribute information. */
    protected Attribute[] fAttributes = new Attribute[4];

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLAttributesImpl() {
        for (int i = 0; i < fAttributes.length; i++) {
            fAttributes[i] = new Attribute();
        }
    } // <init>()

    //
    // Public methods
    //

    /** 
     * Sets whether namespace processing is being performed. This state
     * is needed to return the correct value from the getLocalName method.
     *
     * @param namespaces True if namespace processing is turned on.
     *
     * @see #getLocalName
     */
    public void setNamespaces(boolean namespaces) {
        fNamespaces = namespaces;
    } // setNamespaces(boolean)

    //
    // XMLAttributes methods
    //

    /**
     * Adds an attribute. 
     * <p>
     * <strong>Note:</strogn> If an attribute of the same name already
     * exists, the old values for the attribute are replaced by the new
     * values.
     * 
     * @param attrName  The attribute name.
     * @param attrType  The attribute type. The type name is determined by
     *                  the type specified for this attribute in the DTD.
     *                  For example: "CDATA", "ID", "NMTOKEN", etc. However,
     *                  attributes of type enumeration will have the type
     *                  value specified as the pipe ('|') separated list of
     *                  the enumeration values prefixed by an open 
     *                  parenthesis and suffixed by a close parenthesis.
     *                  For example: "(true|false)".
     * @param attrValue The attribute value.
     * 
     * @return Returns the attribute index.
     */
    public int addAttribute(QName name, String type, String value) {

        // find attribute; create, if necessary
        int index = name.uri != null
                  ? getIndex(name.uri, name.localpart)
                  : getIndex(name.rawname);
        if (index == -1) {
            index = fLength;
            if (fLength++ == fAttributes.length) {
                Attribute[] attributes = new Attribute[fAttributes.length + 4];
                System.arraycopy(fAttributes, 0, attributes, 0, fAttributes.length);
                for (int i = fAttributes.length; i < attributes.length; i++) {
                    attributes[i] = new Attribute();
                }
                fAttributes = attributes;
            }
        }

        // set values
        Attribute attribute = fAttributes[index];
        attribute.name.setValues(name);
        attribute.type = type;
        attribute.value = value;
        attribute.entityCount = 0;

        // return
        return index;

    } // addAttribute(QName,String,XMLString)

    /** 
     * Removes all of the attributes. This method will also remove all
     * entities associated to the attributes.
     */
    public void removeAllAttributes() {
        fLength = 0;
    } // removeAllAttributes()

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all
     * attributes following the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     */
    public void removeAttributeAt(int attrIndex) {
        if (attrIndex < fLength - 1) {
            System.arraycopy(fAttributes, attrIndex + 1, fAttributes, attrIndex, fLength - attrIndex - 1);
        }
        fLength--;
    } // removeAttributeAt(int)

    /**
     * Adds an entity to the specified attribute.
     * <p>
     * <strong>Note:</strong> This method does not replace any existing
     * entities for the specified attribute, even if an entity of the
     * same name already exists.
     * <p>
     * <strong>Note:</strong> This method does <em>not</em> ensure that 
     * the entities appear in increasing offset order. The caller is
     * required to add attribute entities in increasing offset order.
     * 
     * @param attrIndex    The attribute index.
     * @param entityName   The entity name.
     * @param entityOffset The entity offset.
     * @param entityLength The entity length
     */
    public int addAttributeEntity(int attrIndex, String entityName, 
                                  int entityOffset, int entityLength) {

        // create entity arrays, if needed
        Attribute attribute = fAttributes[attrIndex];
        if (attribute.entityName == null) {
            attribute.entityName = new String[2];
            attribute.entityOffset = new int[2];
            attribute.entityLength = new int[2];
        }

        // resize entity arrays, if needed
        if (attribute.entityCount == attribute.entityName.length) {
            String[] names = new String[attribute.entityName.length * 2];
            System.arraycopy(attribute.entityName, 0, names, 0, attribute.entityName.length);
            attribute.entityName = names;
            int[] offsets = new int[attribute.entityOffset.length * 2];
            System.arraycopy(attribute.entityOffset, 0, offsets, 0, attribute.entityOffset.length);
            attribute.entityOffset = offsets;
            int[] lengths = new int[attribute.entityLength.length * 2];
            System.arraycopy(attribute.entityLength, 0, lengths, 0, attribute.entityLength.length);
            attribute.entityLength = lengths;
        }

        // save values
        int entityIndex = attribute.entityCount++;
        attribute.entityName[entityIndex] = entityName;
        attribute.entityOffset[entityIndex] = entityOffset;
        attribute.entityLength[entityIndex] = entityLength;
        
        
        // return entity index
        return entityIndex;

    } // addAttributeEntity(int,String,int,int):int

    /** 
     * Removes all of the entities for the specified attribute.
     *
     * @param attrIndex The attribute index.
     */
    public void removeAllEntitiesFor(int attrIndex) {
        fAttributes[attrIndex].entityCount = 0;
    } // removeAllEntitiesFor(int)

    /**
     * Removes the specified entity of a given attribute.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all
     * entities following the entity at the specified index.
     *
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public void removeEntityAt(int attrIndex, int entityIndex) {
        Attribute attribute = fAttributes[attrIndex];
        if (entityIndex < attribute.entityCount - 1) {
            System.arraycopy(attribute.entityName, entityIndex + 1, attribute.entityName, entityIndex, attribute.entityCount - entityIndex - 1);
            System.arraycopy(attribute.entityOffset, entityIndex + 1, attribute.entityOffset, entityIndex, attribute.entityCount - entityIndex - 1);
            System.arraycopy(attribute.entityLength, entityIndex + 1, attribute.entityLength, entityIndex, attribute.entityCount - entityIndex - 1);
        }
        attribute.entityCount--;
    } // removeEntityAt(int,int)

    /**
     * Sets the name of the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrName  The new attribute name.
     */
    public void setName(int attrIndex, QName attrName) {
        fAttributes[attrIndex].name.setValues(attrName);
    } // setName(int,QName)

    /**
     * Sets the fields in the given QName structure with the values
     * of the attribute name at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrName  The attribute name structure to fill in.
     */
    public void getName(int attrIndex, QName attrName) {
        attrName.setValues(fAttributes[attrIndex].name);
    } // getName(int,QName)

    /**
     * Sets the type of the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrType  The attribute type. The type name is determined by
     *                  the type specified for this attribute in the DTD.
     *                  For example: "CDATA", "ID", "NMTOKEN", etc. However,
     *                  attributes of type enumeration will have the type
     *                  value specified as the pipe ('|') separated list of
     *                  the enumeration values prefixed by an open 
     *                  parenthesis and suffixed by a close parenthesis.
     *                  For example: "(true|false)".
     */
    public void setType(int attrIndex, String attrType) {
        fAttributes[attrIndex].type = attrType;
    } // setType(int,String)

    /**
     * Sets the value of the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrValue The new attribute value.
     */
    public void setValue(int attrIndex, String attrValue) {
        fAttributes[attrIndex].value = attrValue;
    } // setValue(int,String)

    /**
     * setValue
     * 
     * @param index 
     * @param value 
     */
    public void setValue(int index, XMLString value) {
        setValue(index, value != null ? value.toString() : null);
    } // setValue(int,XMLString)

    /**
     * Returns the number of entities for the specified attribute.
     * 
     * @param attrIndex The attribute index.
     */
    public int getEntityCount(int attrIndex) {
        return fAttributes[attrIndex].entityCount;
    } // getEntityCount(int):int

    /**
     * Sets the entity name.
     *
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     * @param entityName  The new entity name.
     */
    public void setEntityName(int attrIndex, int entityIndex, 
                              String entityName) {
        fAttributes[attrIndex].entityName[entityIndex] = entityName;
    } // setEntityName(int,int,String)

    /**
     * Returns the name of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public String getEntityName(int attrIndex, int entityIndex) {
        return fAttributes[attrIndex].entityName[entityIndex];
    } // getEntityName(int,int):String

    /**
     * Sets the entity offset.
     *
     * @param attrIndex    The attribute index.
     * @param entityIndex  The entity index.
     * @param entityOffset The new entity offset.
     */
    public void setEntityOffset(int attrIndex, int entityIndex,
                                int entityOffset) {
        fAttributes[attrIndex].entityOffset[entityIndex] = entityOffset;
    } // setEntityOffset(int,int,int)

    /**
     * Returns the offset of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public int getEntityOffset(int attrIndex, int entityIndex) {
        return fAttributes[attrIndex].entityOffset[entityIndex];
    } // getEntityOffset(int,int):int

    /**
     * Sets the entity length.
     *
     * @param attrIndex    The attribute index.
     * @param entityIndex  The entity index.
     * @param entityLength The new entity length.
     */
    public void setEntityLength(int attrIndex, int entityIndex,
                                int entityLength) {
        fAttributes[attrIndex].entityLength[entityIndex] = entityLength;
    } // setEntityLength(int,int,int)

    /**
     * Returns the length of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public int getEntityLength(int attrIndex, int entityIndex) {
        return fAttributes[attrIndex].entityLength[entityIndex];
    } // getEntityLength(int,int):int

    //
    // AttributeList and Attributes methods
    //

    /**
     * Return the number of attributes in the list.
     *
     * <p>Once you know the number of attributes, you can iterate
     * through the list.</p>
     *
     * @return The number of attributes in the list.
     */
    public int getLength() {
        return fLength;
    } // getLength():int

    /**
     * Look up an attribute's type by index.
     *
     * <p>The attribute type is one of the strings "CDATA", "ID",
     * "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES",
     * or "NOTATION" (always in upper case).</p>
     *
     * <p>If the parser has not read a declaration for the attribute,
     * or if the parser does not report attribute types, then it must
     * return the value "CDATA" as stated in the XML 1.0 Recommentation
     * (clause 3.3.3, "Attribute-Value Normalization").</p>
     *
     * <p>For an enumerated attribute that is not a notation, the
     * parser will report the type as "NMTOKEN".</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's type as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    public String getType(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fAttributes[index].type;
    } // getType(int):String

    /**
     * Look up an attribute's type by XML 1.0 qualified name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    public String getType(String qname) {
        int index = getIndex(qname);
        return index != -1 ? fAttributes[index].type : null;
    } // getType(String):String

    /**
     * Look up an attribute's value by index.
     *
     * <p>If the attribute value is a list of tokens (IDREFS,
     * ENTITIES, or NMTOKENS), the tokens will be concatenated
     * into a single string with each token separated by a
     * single space.</p>
     *
     * @param index The attribute index (zero-based).
     * @return The attribute's value as a string, or null if the
     *         index is out of range.
     * @see #getLength
     */
    public String getValue(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fAttributes[index].value;
    } // getValue(int):String

    /**
     * Look up an attribute's value by XML 1.0 qualified name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param qname The XML 1.0 qualified name.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list or if qualified names
     *         are not available.
     */
    public String getValue(String qname) {
        int index = getIndex(qname);
        return index != -1 ? fAttributes[index].value : null;
    } // getValue(String):String

    //
    // AttributeList methods
    //

    /**
     * Return the name of an attribute in this list (by position).
     *
     * <p>The names must be unique: the SAX parser shall not include the
     * same attribute twice.  Attributes without values (those declared
     * #IMPLIED without a value specified in the start tag) will be
     * omitted from the list.</p>
     *
     * <p>If the attribute name has a namespace prefix, the prefix
     * will still be attached.</p>
     *
     * @param i The index of the attribute in the list (starting at 0).
     * @return The name of the indexed attribute, or null
     *         if the index is out of range.
     * @see #getLength 
     */
    public String getName(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fAttributes[index].name.rawname;
    } // getName(int):String

    //
    // Attributes methods
    //

    /**
     * Look up the index of an attribute by XML 1.0 qualified name.
     *
     * @param qName The qualified (prefixed) name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex(String qName) {
        for (int i = 0; i < fLength; i++) {
            Attribute attribute = fAttributes[i];
            if (attribute.name.rawname != null &&
                attribute.name.rawname.equals(qName)) {
                return i;
            }
        }
        return -1;
    } // getIndex(String):int
    
    /**
     * Look up the index of an attribute by Namespace name.
     *
     * @param uri The Namespace URI, or the empty string if
     *        the name has no Namespace URI.
     * @param localName The attribute's local name.
     * @return The index of the attribute, or -1 if it does not
     *         appear in the list.
     */
    public int getIndex(String uri, String localPart) {
        for (int i = 0; i < fLength; i++) {
            Attribute attribute = fAttributes[i];
            if (attribute.name.uri != null &&
                attribute.name.uri.equals(uri) &&
                attribute.name.localpart != null &&
                attribute.name.localpart.equals(localPart)) {
                return i;
            }
        }
        return -1;
    } // getIndex(String,String):int

    /**
     * Look up an attribute's local name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The local name, or the empty string if Namespace
     *         processing is not being performed, or null
     *         if the index is out of range.
     * @see #getLength
     */
    public String getLocalName(int index) {
        if (!fNamespaces) {
            return "";
        }
        if (index < 0 || index >= fLength) {
            return null;
        }
        return fAttributes[index].name.localpart;
    } // getLocalName(int):String

    /**
     * Look up an attribute's XML 1.0 qualified name by index.
     *
     * @param index The attribute index (zero-based).
     * @return The XML 1.0 qualified name, or the empty string
     *         if none is available, or null if the index
     *         is out of range.
     * @see #getLength
     */
    public String getQName(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        String rawname = fAttributes[index].name.rawname;
        return rawname != null ? rawname : "";
    } // getQName(int):String

    /**
     * Look up an attribute's type by Namespace name.
     *
     * <p>See {@link #getType(int) getType(int)} for a description
     * of the possible types.</p>
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute type as a string, or null if the
     *         attribute is not in the list or if Namespace
     *         processing is not being performed.
     */
    public String getType(String uri, String localName) {
        if (!fNamespaces) {
            return null;
        }
        int index = getIndex(uri, localName);
        return index != -1 ? getType(index) : null;
    } // getType(String,String):String

    /**
     * Look up an attribute's Namespace URI by index.
     *
     * @param index The attribute index (zero-based).
     * @return The Namespace URI, or the empty string if none
     *         is available, or null if the index is out of
     *         range.
     * @see #getLength
     */
    public String getURI(int index) {
        if (index < 0 || index >= fLength) {
            return null;
        }
        String uri = fAttributes[index].name.uri;
        return uri != null ? uri : "";
    } // getURI(int):String

    /**
     * Look up an attribute's value by Namespace name.
     *
     * <p>See {@link #getValue(int) getValue(int)} for a description
     * of the possible values.</p>
     *
     * @param uri The Namespace URI, or the empty String if the
     *        name has no Namespace URI.
     * @param localName The local name of the attribute.
     * @return The attribute value as a string, or null if the
     *         attribute is not in the list.
     */
    public String getValue(String uri, String localName) {
        int index = getIndex(uri, localName);
        return index != -1 ? getValue(index) : null;
    } // getValue(String,String):String

    //
    // Classes
    //

    /**
     * Attribute information.
     *
     * @author Andy Clark, IBM
     */
    static class Attribute {
        
        //
        // Data
        //

        // basic info

        /** Name. */
        public QName name = new QName();

        /** Type. */
        public String type;

        /** Value. */
        public String value;

        // entity info

        /** Entity count. */
        public int entityCount;

        /** Entity name. */
        public String[] entityName;

        /** Entity offset. */
        public int[] entityOffset;

        /** Entity length. */
        public int[] entityLength;

    } // class Attribute

} // class XMLAttributesImpl
