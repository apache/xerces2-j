/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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

package dom.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import dom.ParserWrapper;

/**
 * A java serialization test. This sample program parses a
 * document, then serializes out to a file, then reloads
 * it from the file.  The intent is to have zero exceptions
 * in the process.
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @version $Id$
 */
public class Test {

    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";

    public static void main(String args[]) {

        if (args.length != 2) {
            System.exit(1);
        }

        ParserWrapper parser = null;

        try {
            parser = (ParserWrapper) Class.forName(DEFAULT_PARSER_NAME).newInstance();
        } catch (Exception e) {
            System.err.println("error: Unable to instantiate parser (" + DEFAULT_PARSER_NAME + ")");
        }

        try {
            parser.setFeature(NAMESPACES_FEATURE_ID, true);
        } catch (SAXException e) {
            System.err.println("warning: Parser does not support feature (" + NAMESPACES_FEATURE_ID + ")");
        }

        try {
            Document document = null;
            document = parser.parse(args[0]);
            serialize(document, args[1]);
            Document newDocument = deserialize(args[1]);
        } catch (Exception e) {
            System.err.println("error: Error occurred - " + e.getMessage());
            Exception se = e;
            if (e instanceof SAXException) {
                se = ((SAXException) e).getException();
            }
            if (se != null)
                se.printStackTrace(System.err);
            else
                e.printStackTrace(System.err);
        }

    }

    public static void serialize(Document document, String filename) throws Exception {
        System.out.println("Serializing parsed document");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(document);
        out.close();
    }

    public static Document deserialize(String filename) throws Exception {
        System.out.println("De-Serializing parsed document");
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        Document result = (Document) in.readObject();
        return result;
    }

}
