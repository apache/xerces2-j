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
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.DocumentHandler;


/**
 * Interface for a DOM serializer implementation, factory for DOM and SAX
 * serializers, and static methods for serializing DOM documents.
 * <p>
 * To serialize a document using SAX events, create a compatible serializer
 * using {@link #makeSAXSerializer} and pass it around as a {@link
 * DocumentHandler}. If an I/O error occurs while serializing, it will
 * be thrown by {@link DocumentHandler#endDocument}. The SAX serializer
 * may also be used as {@link DTDHandler}, {@link DeclHandler} and
 * {@link LexicalHandler}.
 * <p>
 * To serialize a DOM document or DOM element, create a compatible
 * serializer using {@link #makeSerializer} and call it's {@link
 * #serialize(Document)} or {@link #serialize(Element)} methods.
 * Both methods would produce a full XML document, to serizlie only
 * the portion of the document use {@link OutputFormat#setOmitXMLDeclaration}
 * and specify no document type.
 * <p>
 * The convenience method {@link #serialize(Document,Writer,OutputFormat)}
 * creates a serializer and calls {@link #serizlie(Document)} on that
 * serialized.
 * <p>
 * The {@link OutputFormat} dictates what underlying serialized is used
 * to serialize the document based on the specified method. If the output
 * format or method are missing, the default is an XML serializer with
 * UTF-8 encoding and now indentation.
 * 
 *
 * @version
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see DocumentHandler
 * @see OutputFormat
 */
public abstract class Serializer
{


    /**
     * Serialized the DOM element. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param elem The element to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public abstract void serialize( Element elem )
        throws IOException;


    /**
     * Serializes the DOM document. Throws an exception only if
     * an I/O exception occured while serializing.
     *
     * @param doc The document to serialize
     * @throws IOException An I/O exception occured while
     *   serializing
     */
    public abstract void serialize( Document doc )
        throws IOException;


    /**
     * Creates a compatible serialized for the specified writer
     * and output format. If the output format is missing,
     * the default is an XML format with UTF-8 encoding.
     *
     * @param writer The writer
     * @param format The output format
     * @return A compatible serializer
     */
    public static Serializer makeSerializer( Writer writer, OutputFormat format )
    {
	BaseSerializer serializer;

	serializer = makeBaseSerializer( format );
	serializer.init( writer, format );
	return serializer;
    }


    /**
     * Creates a compatible serializer for the specified output stream
     * and output format. If the output format is missing, the default
     * is an XML format with UTF-8 encoding.
     *
     * @param output The output stream
     * @param format The output format
     * @return A compatible serializer
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static Serializer makeSerializer( OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException
    {
	BaseSerializer serializer;

	serializer = makeBaseSerializer( format );
	serializer.init( output, format );
	return serializer;
    }


    /**
     * Creates a compatible SAX serializer for the specified writer
     * and output format. If the output format is missing, the default
     * is an XML format with UTF-8 encoding.
     *
     * @param writer The writer
     * @param format The output format
     * @return A compatible SAX serializer
     */
    public static DocumentHandler makeSAXSerializer( Writer writer, OutputFormat format )
    {
	BaseSerializer serializer;

	serializer = makeBaseSerializer( format );
	serializer.init( writer, format );
	return serializer;
    }


    /**
     * Creates a compatible SAX serializer for the specified output stream
     * and output format. If the output format is missing, the default
     * is an XML format with UTF-8 encoding.
     *
     * @param output The output stream
     * @param format The output format
     * @return A compatible SAX serializer
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static DocumentHandler makeSAXSerializer( OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException
    {
	BaseSerializer serializer;

	serializer = makeBaseSerializer( format );
	serializer.init( output, format );
	return serializer;
    }


    /**
     * Convenience method serializes the specified document to
     * the writer using the specified output format.
     * <p>
     * Equivalent to calling {@link #serialize(Document)} on
     * a compatible DOM serializer.
     *
     * @param doc The document to serialize
     * @param writer The writer
     * @param format The output format
     * @throws IOException An I/O exception occured while serializing
     * @throws UnsupportedEncodingException Encoding specified
     *   in the output format is not supported
     */
    public static void serialize( Document doc, Writer writer, OutputFormat format )
        throws IOException
    {
	BaseSerializer serializer;

	if ( format == null )
	    format = new OutputFormat( doc );
	serializer = makeBaseSerializer( format );
	serializer.init( writer, format );
	serializer.serialize( doc );
    }


    /**
     * Convenience method serializes the specified document to
     * the output stream using the specified output format.
     * <p>
     * Equivalent to calling {@link #serialize(Document)} on
     * a compatible DOM serializer.
     *
     * @param doc The document to serialize
     * @param output The output stream
     * @param format The output format
     * @throws IOException An I/O exception occured while serializing
     */
    public static void serialize( Document doc, OutputStream output, OutputFormat format )
        throws UnsupportedEncodingException, IOException
    {
	BaseSerializer serializer;

	if ( format == null )
	    format = new OutputFormat( doc );
	serializer = makeBaseSerializer( format );
	serializer.init( output, format );
	serializer.serialize( doc );
    }


    private static BaseSerializer makeBaseSerializer( OutputFormat format )
    {
	BaseSerializer serializer;

	if ( format == null ) {
	    format = new OutputFormat( "xml", "UTF-8", false );
	    serializer = new XMLSerializer();
	} else {
	    if ( format.getMethod().equalsIgnoreCase( "html" ) )
		serializer = new XHTMLSerializer();
	    else
	    if ( format.getMethod().equalsIgnoreCase( "xhtml" ) )
		serializer = new HTMLSerializer();
	    /*
	    else
	    if ( format.getMethod().equalsIgnoreCase( "fop" ) )
		serializer = new FOPSerializer();
	    */
	    else
		serializer = new XMLSerializer();
	}
	return serializer;
    }


}





