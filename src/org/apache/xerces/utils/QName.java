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

package org.apache.xerces.utils;

/** 
 * QName structure useful for gathering the parts of a qualified name.
 *
 * @author Andy Clark
 * @version $Id$
 */
public class QName {

    //
    // Constants
    //

    /** 
     * Compile to true to help find places where a URI is being set
     * to the value -1 when it should be StringPool.EMPTY_STRING (0).
     */
    private static final boolean FIND_URI_IS_MINUS_ONE = false;

    //
    // Data
    //

    /** Prefix. */
    public int prefix;

    /** Local part of qname. */
    public int localpart;

    /** Fully concatenated name. */
    public int rawname;

    /** URI bound to prefix. */
    public int uri;

    //
    // Constructors
    //

    /** Default constructor. */
    public QName() {
        clear();
    }

    /** Constructs a specified qname. */
    public QName(int prefix, int localpart, int rawname) {
        setValues(prefix, localpart, rawname, StringPool.EMPTY_STRING);
    }

    /** Constructs a specified qname. */
    public QName(int prefix, int localpart, int rawname, int uri) {
        setValues(prefix, localpart, rawname, uri);
    }

    /** Copy constructor. */
    public QName(QName qname) {
        setValues(qname);
    }

    //
    // Public methods
    //

    /** Sets the values of the qualified name. */
    public void setValues(QName qname) {
        if (FIND_URI_IS_MINUS_ONE) {
            if (qname.uri == -1) {
                try { 
                    throw new Exception("uri value is -1 instead of StringPool.EMPTY_STRING (0)"); 
                }
                catch (Exception e) { 
                    e.printStackTrace(System.err); 
                }
            }
        }
        prefix = qname.prefix;
        localpart = qname.localpart;
        rawname = qname.rawname;
        uri = qname.uri;
    }

    /** Sets the values of the qualified name. */
    public void setValues(int prefix, int localpart, int rawname) {
        setValues(prefix, localpart, rawname, StringPool.EMPTY_STRING);
    }

    /** Sets the values of the qualified name. */
    public void setValues(int prefix, int localpart, int rawname, int uri) {
        if (FIND_URI_IS_MINUS_ONE) {
            if (uri == -1) {
                try { 
                    throw new Exception("uri value is -1 instead of StringPool.EMPTY_STRING (0)"); 
                }
                catch (Exception e) { 
                    e.printStackTrace(System.err); 
                }
            }
        }
        this.prefix = prefix;
        this.localpart = localpart;
        this.rawname = rawname;
        this.uri = uri;
    }

    /** Clears all of the values. */
    public void clear() {
        prefix = -1;
        localpart = -1;
        rawname = -1;
        uri = StringPool.EMPTY_STRING;
    }

    //
    // Object methods
    //

    /** Returns true if the two objects are equal. */
    public boolean equals(Object object) {
        if (object != null && object instanceof QName) {
            QName qname = (QName)object;
            if (uri == StringPool.EMPTY_STRING) {
                return rawname == qname.rawname;
            }
            return localpart == qname.localpart &&
                   uri == qname.uri;
        }
        return false;
    }

    /** Returns a hash code value. */
    public int hashCode() {
        return (localpart << 16) | uri;
    }

    /** Returns a string representation of this object. */
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("prefix: ");
        str.append(prefix);
        str.append(", ");
        str.append("localpart: ");
        str.append(localpart);
        str.append(", ");
        str.append("rawname: ");
        str.append(rawname);
        str.append(", ");
        str.append("uri: ");
        str.append(uri);
        return str.toString();
    }

    /** Returns a string representation of this object. */
    public String toString(StringPool stringPool) {
        StringBuffer str = new StringBuffer();
        str.append("prefix: ");
        str.append(String.valueOf(stringPool.toString(prefix)));
        str.append(", ");
        str.append("localpart: ");
        str.append(String.valueOf(stringPool.toString(localpart)));
        str.append(", ");
        str.append("rawname: ");
        str.append(String.valueOf(stringPool.toString(rawname)));
        str.append(", ");
        str.append("uri: ");
        str.append(String.valueOf(stringPool.toString(uri)));
        return str.toString();
    }

} // class QName
