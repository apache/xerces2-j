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

package org.apache.xerces.impl.v2;

/**
 * Store schema particle declaration.
 * 
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSParticleDecl {

    //
    // Constants
    //

    /** An empty particle, to be ignored. */
    public static final short PARTICLE_EMPTY     = 0;

    /** An element. */
    public static final short PARTICLE_ELEMENT   = 1;

    /** Represents <choice>. */
    public static final short PARTICLE_CHOICE    = 2;
    
    /** Represents <sequence>. */
    public static final short PARTICLE_SEQUENCE  = 3;

    /** Represents <all>. */
    public static final short PARTICLE_ALL       = 4;

    /** Wildcard namespace="##any". */
    public static final short PARTICLE_ANY       = 5;

    /** Wildcard namespace="##other". */
    public static final short PARTICLE_ANY_OTHER = 6;

    /** Wildcard namespace=list of anyURI | ##targetNamespace | ##local. */
    public static final short PARTICLE_ANY_LIST  = 7;

    /** Wildcard processContents="lax". */
    public static final short WILDCARD_LAX       = 0x10;

    /** Wildcard processContents="skip". */
    public static final short WILDCARD_SKIP      = 0x20;

    /** prcessContent is 'lax' **/
    public static final short PARTICLE_ANY_LAX        = PARTICLE_ANY       | WILDCARD_LAX;
    public static final short PARTICLE_ANY_OTHER_LAX  = PARTICLE_ANY_OTHER | WILDCARD_LAX;
    public static final short PARTICLE_ANY_LIST_LAX   = PARTICLE_ANY_LIST  | WILDCARD_LAX;

    /** processContent is 'skip' **/
    public static final short PARTICLE_ANY_SKIP       = PARTICLE_ANY       | WILDCARD_SKIP;
    public static final short PARTICLE_ANY_OTHER_SKIP = PARTICLE_ANY_OTHER | WILDCARD_SKIP;
    public static final short PARTICLE_ANY_LIST_SKIP  = PARTICLE_ANY_LIST  | WILDCARD_SKIP;

    //
    // Data
    //

    /** 
     * The particle type. 
     *
     * @see PARTICLE_LEAF
     * @see PARTICLE_CHOICE
     * @see PARTICLE_SEQ
     */
    public short type;

    /**
     * The namespace of the "left hand" value object.
     */
    public String uri;

    /**
     * The "left hand" value object of the particle.
     * leaf elementIndex, single child for unary ops, left child for binary ops.
     */
    public int value;

    /**
     * The namespace of the "right hand" value object.
     * When the type is element, then the local name of the element
     */
    public String otherUri;

    /**
     * The "right hand" value of the particle.
     * right child for binary ops
     */
    public int otherValue;

    /**
     * The mininum occurrence of this particle.
     */
    public int minOccurs;

    /**
     * The maximum occurrence of this particle
     */
    public int maxOccurs;

    //
    // Constructors
    //

    /** Default constructor. */
    public XSParticleDecl() {
        clear();
    }

    /** Constructs a particle with the specified values. */
    public XSParticleDecl(short type, String uri, int value, String otherUri,
                          int otherValue, int minOccurs, int maxOccurs) {
        setValues(type, uri, value, otherUri, otherValue, minOccurs, maxOccurs);
    }

    /** 
     * Constructs a particle from the values in the specified particle.
     */
    //REVISIT: we should never clone a particle, so we don't need this method.
    /*public XSParticleDecl(XSParticleDecl particleDecl) {
        setValues(particleDecl);
    }*/

    //
    // Public methods
    //

    /** Clears the values. */
    public void clear() {
        type = PARTICLE_EMPTY;
    }

    /** Sets the values. */
    public void setValues(short type, String uri, int value, String otherUri,
                          int otherValue, int minOccurs, int maxOccurs) {
        this.type = type;
        this.uri = uri;
        this.value = value;
        this.otherUri = otherUri;
        this.otherValue = otherValue;
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
    }
    
    /** Sets the values of the specified particle. */
    //REVISIT: we should never clone a particle, so we don't need this method.
    /*public void setValues(XSParticleDecl particle) {
        type = particle.type;
        uri = particle.uri;
        value = particle.value;
        otherUri = particle.otherUri;
        otherValue = particle.otherValue;
        minOccurs = particle.minOccurs;
        maxOccurs = particle.maxOccurs;
    }*/

    //
    // Object methods
    //

    /** Returns a hash code for this node. */
    public int hashCode() {
        return type << 24 | 
               uri.hashCode() << 20 |
               value << 16 |
               otherUri.hashCode() << 12 |
               otherValue << 8 |
               minOccurs << 4 |
               maxOccurs;
    }

    /** Returns true if the two objects are equal. */
    public boolean equals(Object object) {
        if (object != null && object instanceof XSParticleDecl) {
            XSParticleDecl particle = (XSParticleDecl)object;
            return type == particle.type &&
                   uri.equals(particle.uri) &&
                   value == particle.value &&
                   otherUri.equals(particle.otherUri) &&
                   otherValue == particle.otherValue &&
                   minOccurs == particle.minOccurs &&
                   maxOccurs == particle.maxOccurs;
        }
        return false;
    }
    
} // class XMLContentSpec
