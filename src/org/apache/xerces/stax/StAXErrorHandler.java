/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.stax;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;

/**
 * @xerces.internal
 * 
 * @author Wei Duan
 * 
 * @version $Id$
 */
public class StAXErrorHandler implements XMLErrorHandler {
    
    protected XMLReporter fXMLReporter = null;
    protected static final String ERROR = "error";
    protected static final String WARN = "warn";
    protected static final String FATAL = "fatal";

    /** Creates a new instance of StaxErrorReporter */
    public StAXErrorHandler(XMLReporter reporter) {
        fXMLReporter = reporter;
    }

    /**
     * Reports a warning. Warnings are non-fatal and can be safely ignored
     * by most applications.
     *
     * @param domain    The domain of the warning. The domain can be any
     *                  string but is suggested to be a valid URI. The
     *                  domain can be used to conveniently specify a web
     *                  site location of the relevent specification or
     *                  document pertaining to this warning.
     * @param key       The warning key. This key can be any string and
     *                  is implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop
     *                      parsing the document.
     */
    public void warning(String domain, String key, XMLParseException exception)
            throws XNIException {
        try {
            fXMLReporter.report(exception.getMessage(), WARN, null,
                    convertToLocation(exception));
        } 
        catch (XMLStreamException ex) {
            throw new XNIException(ex.getMessage(), ex);
        }
    }

    /**
     * Reports an error. Errors are non-fatal and usually signify that the
     * document is invalid with respect to its grammar(s).
     *
     * @param domain    The domain of the error. The domain can be any
     *                  string but is suggested to be a valid URI. The
     *                  domain can be used to conveniently specify a web
     *                  site location of the relevent specification or
     *                  document pertaining to this error.
     * @param key       The error key. This key can be any string and
     *                  is implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop
     *                      parsing the document.
     */
    public void error(String domain, String key, XMLParseException exception)
            throws XNIException {
        try {
            fXMLReporter.report(exception.getMessage(), ERROR, null,
                    convertToLocation(exception));
        } 
        catch (XMLStreamException ex) {
            throw new XNIException(ex.getMessage(), ex);
        }
    }

    /**
     * Report a fatal error. Fatal errors usually occur when the document
     * is not well-formed and signifies that the parser cannot continue
     * normal operation.
     * <p>
     * <strong>Note:</strong> The error handler should <em>always</em>
     * throw an <code>XNIException</code> from this method. This exception
     * can either be the same exception that is passed as a parameter to
     * the method or a new XNI exception object. If the registered error
     * handler fails to throw an exception, the continuing operation of
     * the parser is undetermined.
     *
     * @param domain    The domain of the fatal error. The domain can be 
     *                  any string but is suggested to be a valid URI. The
     *                  domain can be used to conveniently specify a web
     *                  site location of the relevent specification or
     *                  document pertaining to this fatal error.
     * @param key       The fatal error key. This key can be any string 
     *                  and is implementation dependent.
     * @param exception Exception.
     *
     * @throws XNIException Thrown to signal that the parser should stop
     *                      parsing the document.
     */
    public void fatalError(String domain, String key,
            XMLParseException exception) throws XNIException {
        try {
            fXMLReporter.report(exception.getMessage(), FATAL, null,
                    convertToLocation(exception));
        } 
        catch (XMLStreamException ex) {
            throw new XNIException(ex.getMessage(), ex);
        }
    }

    /**
     * Get the STAX location from XMLParserException
     *
     * @param exception XMLParseException. Get the XMLParserException Exception
     *
     * @return Location 
     */
    Location convertToLocation(final XMLParseException exception) {
        return new Location() {
            public int getColumnNumber() {
                return exception.getColumnNumber();
            }

            public int getLineNumber() {
                return exception.getLineNumber();
            }

            public String getPublicId() {
                return exception.getPublicId();
            }

            public String getSystemId() {
                return exception.getLiteralSystemId();
            }

            public int getCharacterOffset() {
                return exception.getCharacterOffset();
            }
        };
    }
}