/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.dom;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// REVISIT: This is a HACK! DO NOT MODIFY THIS import.
//          It allows us to expose DOM L3 implemenation via org.w3c.dom packages
import org.apache.xerces.dom3.DOMConfiguration;
import org.w3c.dom.*;

/**
 * Our own document implementation, which knows how to create an element
 * with PSVI information.
 * 
 * @author Sandy Gao, IBM
 * 
 * @version $Id$
 */
public class PSVIDocumentImpl extends DocumentImpl {
   
    /** Serialization version. */
    static final long serialVersionUID = -8822220250676434522L;

    /**
     * Create a document.
     */
    public PSVIDocumentImpl() {
        super();
    }

    /**
     * For DOM2 support.
     * The createDocument factory method is in DOMImplementation.
     */
    public PSVIDocumentImpl(DocumentType doctype) {
        super(doctype);
    }
    
    /**
     * Retrieve information describing the abilities of this particular
     * DOM implementation. Intended to support applications that may be
     * using DOMs retrieved from several different sources, potentially
     * with different underlying representations.
     */
    public DOMImplementation getImplementation() {
        // Currently implemented as a singleton, since it's hardcoded
        // information anyway.
        return PSVIDOMImplementationImpl.getDOMImplementation();
    }

    /**
     * Create an element with PSVI information
     */
    public Element createElementNS(String namespaceURI, String qualifiedName)
        throws DOMException {
        return new PSVIElementNSImpl(this, namespaceURI, qualifiedName);
    }

    /**
     * Create an element with PSVI information
     */
    public Element createElementNS(String namespaceURI, String qualifiedName,
                                   String localpart) throws DOMException {
        return new PSVIElementNSImpl(this, namespaceURI, qualifiedName, localpart);
    }

    /**
     * Create an attribute with PSVI information
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
        throws DOMException {
        return new PSVIAttrNSImpl(this, namespaceURI, qualifiedName);
    } 
    
    /**
     * Create an attribute with PSVI information
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName,
                                  String localName) throws DOMException {
        return new PSVIAttrNSImpl(this, namespaceURI, qualifiedName, localName);
    } 
    
    /**
     * 
     * The configuration used when <code>Document.normalizeDocument</code> is 
     * invoked. 
     * @since DOM Level 3
     */
    public DOMConfiguration getDomConfig(){
        super.getDomConfig();
        return fConfiguration;
    }
    
    // REVISIT: Forbid serialization of PSVI DOM until
    // we support object serialization of grammars -- mrglavas
    
    private void writeObject(ObjectOutputStream out)
        throws IOException {
        throw new NotSerializableException(getClass().getName());
	}

    private void readObject(ObjectInputStream in) 
        throws IOException, ClassNotFoundException {
        throw new NotSerializableException(getClass().getName());
    }
    
} // class PSVIDocumentImpl
