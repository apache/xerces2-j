/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.dv;

/**
 * Any atomic simple type will implement this interface. It inherits the methods
 * of generic <code>XSSimpleType</code> interface.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public interface XSAtomicSimpleType extends XSSimpleType {

    /**
     * Constant defined for the primitive built-in simple tpyes.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes'>
     * XML Schema Part 2: Datatypes </a>
     */
    /** "string" type */
    public static final short PRIMITIVE_STRING        = 1;
    /** "boolean" type */
    public static final short PRIMITIVE_BOOLEAN       = 2;
    /** "decimal" type */
    public static final short PRIMITIVE_DECIMAL       = 3;
    /** "float" type */
    public static final short PRIMITIVE_FLOAT         = 4;
    /** "double" type */
    public static final short PRIMITIVE_DOUBLE        = 5;
    /** "duration" type */
    public static final short PRIMITIVE_DURATION      = 6;
    /** "dataTime" type */
    public static final short PRIMITIVE_DATETIME      = 7;
    /** "time" type */
    public static final short PRIMITIVE_TIME          = 8;
    /** "date" type */
    public static final short PRIMITIVE_DATE          = 9;
    /** "gYearMonth" type */
    public static final short PRIMITIVE_GYEARMONTH    = 10;
    /** "gYear" type */
    public static final short PRIMITIVE_GYEAR         = 11;
    /** "gMonthDay" type */
    public static final short PRIMITIVE_GMONTHDAY     = 12;
    /** "gDay" type */
    public static final short PRIMITIVE_GDAY          = 13;
    /** "gMonth" type */
    public static final short PRIMITIVE_GMONTH        = 14;
    /** "hexBinary" type */
    public static final short PRIMITIVE_HEXBINARY     = 15;
    /** "base64Binary" type */
    public static final short PRIMITIVE_BASE64BINARY  = 16;
    /** "anyURI" type */
    public static final short PRIMITIVE_ANYURI        = 17;
    /** "QName" type */
    public static final short PRIMITIVE_QNAME         = 18;
    /** "NOTATION" type */
    public static final short PRIMITIVE_NOTATION      = 19;

    /**
     * return an ID representing the built-in primitive base type.
     * REVISIT: This method is (currently) for internal use only.
     *          the constants returned from this method are not finalized yet.
     *          the names and values might change in the further.
     *
     * @return   an ID representing the built-in primitive base type
     */
    public short getPrimitiveKind();

    /**
     * return the built-in primitive base type
     *
     * @return   the built-in primitive base type
     */
    public XSSimpleType getPrimitiveType();
}
