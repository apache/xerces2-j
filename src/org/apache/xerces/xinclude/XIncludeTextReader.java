/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2003, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.xinclude;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * @author Peter McCracken, IBM
 * @author Arun Yadav, Sun Microsystem
 */
public class XIncludeTextReader {

    private Reader fReader;
    private XIncludeHandler fHandler;
    private XMLInputSource fSource;
    private boolean fCheckBOM = false;

    /**
     * Construct the XIncludeReader using the XMLInputSource and XIncludeHandler.
     *
     * @param source The XMLInputSource to use.
     * @param handler The XIncludeHandler to use.
     */
    public XIncludeTextReader(XMLInputSource source, XIncludeHandler handler)
        throws IOException {
        fHandler = handler;
        fSource = source;
    }

    /**
     * Return the Reader for given XMLInputSource.
     *
     * @param source The XMLInputSource to use.
     */
    protected Reader getReader(XMLInputSource source) throws IOException {
        if (source.getCharacterStream() != null) {
            return fReader = source.getCharacterStream();
        }
        else {
            InputStream stream = null;

            String encoding = source.getEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            if (source.getByteStream() != null) {
                stream = source.getByteStream();
            }
            else {
                URL url =
                    new URL(
                        new URL(source.getBaseSystemId()),
                        source.getSystemId());
                // TODO: use this to ensure that rewinding is supported
                //stream = new XMLEntityManager.RewindableInputStream(url.openStream());
                stream = url.openStream();
                URLConnection urlCon = url.openConnection();

                String charset = urlCon.getContentEncoding();
                String contentType = urlCon.getContentType();

                /**  The encoding of such a resource is determined by:
                    1 external encoding information, if available, otherwise
                         -- the most common type of external information is the "charset" parameter of a MIME package
                    2 if the media type of the resource is text/xml, application/xml, or matches the conventions text/*+xml or application/*+xml as described in XML Media Types [IETF RFC 3023], the encoding is recognized as specified in XML 1.0, otherwise
                    3 the value of the encoding attribute if one exists, otherwise
                    4 UTF-8.
                 **/
                if (charset != null) {
                    encoding = charset;
                }
                /* RFC2376 section 3.1: If  text/xml entity is received with
                charset parameter omitted, then MIME processors and XML processors
                MUST use the default charset value of "us-ascii". */
                else if (contentType.equals("text/xml")) {
                    encoding = "US-ASCII";
                }
                /* RFC2376 section 3.1: If  text/xml entity is received with
                charset parameter omitted, no information is provided about the
                charset by the MIME Content-type header. Conforming  XML processors
                MUST follow the requirements in section 4.3.3 of [REC-XML].
                */
                else if (contentType.equals("application/xml")) {
                    encoding = getEncodingName(stream);

                }
                else if (contentType.endsWith("+xml")) {
                    System.out.println("Not suppported");
                    // TODO: Error messages need to be defined.
                }
                //else 3  or 4.
            }
            return fReader = new InputStreamReader(stream, encoding);
        }
    }

    protected String getEncodingName(InputStream stream) throws IOException {
        final byte[] b4 = new byte[4];
        int count = 0;
        stream.mark(4);
        for (; count < 4; count++) {
            b4[count] = (byte)stream.read();
        }
        Object[] encodinginfo = getEncodingName(b4, count);

        // REVISIT: what to do if encodinginfo[1] == null
        //          Currently, this isn't a problem, since getEncodingName() will never
        //          return a null value, but the javadocs say it's possible.
        if (encodinginfo[1] != null
            && !((Boolean)encodinginfo[1]).booleanValue()) {
            stream.reset();
            fCheckBOM = false;
        }
        else {
            fCheckBOM = true;
        }
        return (String)encodinginfo[0];
    }

    /**
     * REVISIT: This code is take from org.apache.xerces.impl.XMLEntityManager.
     *          Is there any way we can share the code, without having it implemented twice?
     *          I think we should make it public and static in XMLEntityManager. --PJM
     * 
     * Returns the IANA encoding name that is auto-detected from
     * the bytes specified, with the endian-ness of that encoding where appropriate.
     *
     * @param b4    The first four bytes of the input.
     * @param count The number of bytes actually read.
     * @return a 2-element array:  the first element, an IANA-encoding string,
     *  the second element a Boolean which is true iff the document is big endian, false
     *  if it's little-endian, and null if the distinction isn't relevant.
     */
    protected Object[] getEncodingName(byte[] b4, int count) {

        if (count < 2) {
            return new Object[] { "UTF-8", new Boolean(false)};
        }

        // UTF-16, with BOM
        int b0 = b4[0] & 0xFF;
        int b1 = b4[1] & 0xFF;
        if (b0 == 0xFE && b1 == 0xFF) {
            // UTF-16, big-endian
            return new Object[] { "UTF-16BE", new Boolean(true)};
        }
        if (b0 == 0xFF && b1 == 0xFE) {
            // UTF-16, little-endian
            return new Object[] { "UTF-16LE", new Boolean(true)};
        }

        // default to UTF-8 if we don't have enough bytes to make a
        // good determination of the encoding
        if (count < 3) {
            return new Object[] { "UTF-8", new Boolean(false)};
        }

        // UTF-8 with a BOM
        int b2 = b4[2] & 0xFF;
        if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
            return new Object[] { "UTF-8", new Boolean(true)};
        }

        // default to UTF-8 if we don't have enough bytes to make a
        // good determination of the encoding
        if (count < 4) {
            return new Object[] { "UTF-8", new Boolean(false)};
        }

        // other encodings
        int b3 = b4[3] & 0xFF;
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x00 && b3 == 0x3C) {
            // UCS-4, big endian (1234), with BOM
            return new Object[] { "ISO-10646-UCS-4", new Boolean(true)};
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x00 && b3 == 0x00) {
            // UCS-4, little endian (4321), with BOM
            return new Object[] { "ISO-10646-UCS-4", new Boolean(true)};
        }
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x3C && b3 == 0x00) {
            // UCS-4, unusual octet order (2143), with BOM
            // REVISIT: What should this be?
            return new Object[] { "ISO-10646-UCS-4", new Boolean(true)};
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x00) {
            // UCS-4, unusual octect order (3412), with BOM
            // REVISIT: What should this be?
            return new Object[] { "ISO-10646-UCS-4", new Boolean(true)};
        }
        if (b0 == 0x00 && b1 == 0x3C && b2 == 0x00 && b3 == 0x3F) {
            // UTF-16, big-endian, no BOM
            // (or could turn out to be UCS-2...
            // REVISIT: What should this be?
            return new Object[] { "UTF-16BE", new Boolean(false)};
        }
        if (b0 == 0x3C && b1 == 0x00 && b2 == 0x3F && b3 == 0x00) {
            // UTF-16, little-endian, no BOM
            // (or could turn out to be UCS-2...
            return new Object[] { "UTF-16LE", new Boolean(false)};
        }
        if (b0 == 0x4C && b1 == 0x6F && b2 == 0xA7 && b3 == 0x94) {
            // EBCDIC, no BOM
            // a la xerces1, return CP037 instead of EBCDIC here
            return new Object[] { "CP037", new Boolean(false)};
        }
        if (b0 == 0x00 && b1 == 0x00 && b2 == 0x00 && b3 == 0x3C) {
            // UCS-4, no BOM
            //
            return new Object[] { "UCS-4", new Boolean(false)};
        }

        // default encoding
        return new Object[] { "UTF-8", new Boolean(false)};

    } // getEncodingName(byte[],int):Object[]

    public void parse() throws IOException {
        XMLStringBuffer buffer = new XMLStringBuffer();
        fReader = getReader(fSource);
        if (fReader.ready()) {
            // deal with byte order mark, see spec 4.3
            if (!fCheckBOM) {
                int bom = fReader.read();
                // only include the character if it isn't the byte-order mark
                if (bom != '\uFEFF') {
                    buffer.append((char)bom);
                }
            }

            // REVISIT: We might want to consider sending the character events in chunks
            //          instead of reading them all.  This would be a space hog for large
            //          text includes.
            while (fReader.ready()) {
                buffer.append((char)fReader.read());
            }
            if (fHandler != null) {
                fHandler.characters(
                    buffer,
                    fHandler.modifyAugmentations(null, true));
            }
        }
    }

    public void close() throws IOException {
        if (fReader != null) {
            fReader.close();
        }
    }
}