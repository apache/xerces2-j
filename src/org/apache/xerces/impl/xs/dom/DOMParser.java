/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.xs.dom;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.NonValidatingConfiguration;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XNIException;

import org.w3c.dom.Element;

/**
 * A dom parser used to parse schema documents into DOM trees
 * 
 * @author Sandy Gao, IBM
 * 
 * @version $Id$
 */
public class DOMParser extends org.apache.xerces.parsers.DOMParser {

    /** Property identifier: entity manager. */
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    
    /** Property identifier: DOM document class name. */
    protected static final String DOCUMENT_CLASS = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_CLASS_NAME_PROPERTY;
    
    /** Feature identifier: DOM Defer node expansion. */
    protected static final String DEFER_EXPANSION = 
        Constants.XERCES_FEATURE_PREFIX + Constants.DEFER_NODE_EXPANSION_FEATURE;
    
    // the locator containing line/column information
    protected XMLLocator   fLocator;
    
    // our own document implementation, which knows how to create Element
    // with line/column information
    public DocumentImpl fDocumentImpl;

    private DOMNodePool fNodePool;
    
    //
    // Constructors
    //

    /**
     * Constructs a DOM parser using the dtd/xml schema parser configuration.
     */
    public DOMParser() {
        super(new NonValidatingConfiguration(new SchemaSymbols.SchemaSymbolTable()));
        try {
            // use our own document implementation
            setProperty(DOCUMENT_CLASS, "org.apache.xerces.impl.xs.dom.DocumentImpl");
            // don't defer DOM expansion
            setFeature(DEFER_EXPANSION, false);

        }
        catch (Exception e) {
        }
    } // <init>()

    public void setPool(DOMNodePool nodePool){
        fNodePool = nodePool;
    }

    /**
     * The start of the document.
     *
     * @param locator The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs)
        throws XNIException {

        super.startDocument(locator, encoding, augs);
        // get a handle to the document created
        fDocumentImpl = (DocumentImpl)super.fDocumentImpl;
        fDocumentImpl.fNodePool=fNodePool;
        fLocator = locator;

    } // startDocument(XMLLocator,String,Augmentations)
    
    // override this method to store line/column information in Element created
    protected Element createElementNode(QName element) {
        // create an element containing line/column information
        return fDocumentImpl.createElementNS(element.uri, element.rawname,
                                             element.localpart,
                                             fLocator.getLineNumber(),
                                             fLocator.getColumnNumber());
    }

} // class DOMParser
