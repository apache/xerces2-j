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

package org.apache.xerces.xni;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;

/**
 * The XMLAttributes interface defines a collection of attributes for 
 * an element. In the parser, the document source would scan the entire
 * start element and collect the attributes. The attributes are 
 * communicated to the document handler in the startElement method.
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
public interface XMLAttributes
    extends AttributeList, Attributes {

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
    public int addAttribute(QName attrName, String attrType, String attrValue);

    /** 
     * Removes all of the attributes. This method will also remove all
     * entities associated to the attributes.
     */
    public void removeAllAttributes();

    /**
     * Removes the attribute at the specified index.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all
     * attributes following the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     */
    public void removeAttributeAt(int attrIndex);

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
                                  int entityOffset, int entityLength);

    /** 
     * Removes all of the entities for the specified attribute.
     *
     * @param attrIndex The attribute index.
     */
    public void removeAllEntitiesFor(int attrIndex);

    /**
     * Removes the specified entity of a given attribute.
     * <p>
     * <strong>Note:</strong> This operation changes the indexes of all
     * entities following the entity at the specified index.
     *
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public void removeEntityAt(int attrIndex, int entityIndex);

    /**
     * Sets the name of the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrName  The new attribute name.
     */
    public void setName(int attrIndex, QName attrName);

    /**
     * Sets the fields in the given QName structure with the values
     * of the attribute name at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrName  The attribute name structure to fill in.
     */
    public void getName(int attrIndex, QName attrName);

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
    public void setType(int attrIndex, String attrType);

    /**
     * Sets the value of the attribute at the specified index.
     * 
     * @param attrIndex The attribute index.
     * @param attrValue The new attribute value.
     */
    public void setValue(int attrIndex, String attrValue);

    /**
     * Returns the number of entities for the specified attribute.
     * 
     * @param attrIndex The attribute index.
     */
    public int getEntityCount(int attrIndex);

    /**
     * Sets the entity name.
     *
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     * @param entityName  The new entity name.
     */
    public void setEntityName(int attrIndex, int entityIndex, 
                              String entityName);

    /**
     * Returns the name of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public String getEntityName(int attrIndex, int entityIndex);

    /**
     * Sets the entity offset.
     *
     * @param attrIndex    The attribute index.
     * @param entityIndex  The entity index.
     * @param entityOffset The new entity offset.
     */
    public void setEntityOffset(int attrIndex, int entityIndex,
                                int entityOffset);

    /**
     * Returns the offset of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public int getEntityOffset(int attrIndex, int entityIndex);

    /**
     * Sets the entity length.
     *
     * @param attrIndex    The attribute index.
     * @param entityIndex  The entity index.
     * @param entityLength The new entity length.
     */
    public void setEntityLength(int attrIndex, int entityIndex,
                                int entityLength);

    /**
     * Returns the length of the entity of a given attribute.
     * 
     * @param attrIndex   The attribute index.
     * @param entityIndex The entity index.
     */
    public int getEntityLength(int attrIndex, int entityIndex);

} // interface XMLAttributes
