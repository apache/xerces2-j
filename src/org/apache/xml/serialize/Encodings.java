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


package org.apache.xml.serialize;


import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


/**
 * Provides information about encodings. Depends on the Java runtime
 * to provides writers for the different encodings, but can be used
 * to override encoding names and provide the last printable character
 * for each encoding.
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 */
class Encodings
{


    /**
     * The last printable character for unknown encodings.
     */
    static final int DefaultLastPrintable = 0x7F;


    /**
     * Returns a writer for the specified encoding based on
     * an output stream.
     *
     * @param output The output stream
     * @param encoding The encoding
     * @return A suitable writer
     * @throws UnsupportedEncodingException There is no convertor
     *  to support this encoding
     */
    static Writer getWriter( OutputStream output, String encoding )
        throws UnsupportedEncodingException
    {
        for ( int i = 0 ; i < _encodings.length ; ++i ) {
            if ( _encodings[ i ].name.equals( encoding ) )
                return new OutputStreamWriter( output, _encodings[ i ].javaName );
        }
        return new OutputStreamWriter( output, encoding );
    }


    /**
     * Returns the last printable character for the specified
     * encoding.
     *
     * @param encoding The encoding
     * @return The last printable character
     */
    static int getLastPrintable( String encoding )
    {
        for ( int i = 0 ; i < _encodings.length ; ++i ) {
            if ( _encodings[ i ].name.equalsIgnoreCase( encoding ) )
                return _encodings[ i ].lastPrintable;
        }
        return DefaultLastPrintable;
    }


    /**
     * Returns the last printable character for an unspecified
     * encoding.
     */
    static int getLastPrintable()
    {
        return DefaultLastPrintable;
    }


    /**
     * Holds information about a given encoding.
     */
    static final class EncodingInfo
    {
       
        /**
         * The encoding name.
         */ 
        final String name;

        /**
         * The name used by the Java convertor.
         */
        final String javaName;

        /**
         * The last printable character.
         */
        final int    lastPrintable;

        EncodingInfo( String name, String javaName, int lastPrintable )
        {
            this.name = name;
            this.javaName = javaName;
            this.lastPrintable = lastPrintable;
        }

    }


    /**
     * Constructs a list of all the supported encodings.
     */
    private static final EncodingInfo[] _encodings = new EncodingInfo[] {
        new EncodingInfo( "ASCII", "ASCII", 0x7F ),
        new EncodingInfo( "ISO-Latin-1", "ASCII", 0xFF ),
        new EncodingInfo( "ISO-8859-1", "ISO8859_1", 0xFF ),
        new EncodingInfo( "ISO-8859-2", "ISO8859_2", 0xFF ),
        new EncodingInfo( "ISO-8859-3", "ISO8859_3", 0xFF ),
        new EncodingInfo( "ISO-8859-4", "ISO8859_4", 0xFF ),
        new EncodingInfo( "ISO-8859-5", "ISO8859_5", 0xFF ),
        new EncodingInfo( "ISO-8859-6", "ISO8859_6", 0xFF ),
        new EncodingInfo( "ISO-8859-7", "ISO8859_7", 0xFF ),
        new EncodingInfo( "ISO-8859-8", "ISO8859_8", 0xFF ),
        new EncodingInfo( "ISO-8859-9", "ISO8859_9", 0xFF ),
        new EncodingInfo( "UTF-8", "UTF8", 0xFFFF ),
        new EncodingInfo( "UNICODE", "Unicode", 0xFFFF )
    };


}
