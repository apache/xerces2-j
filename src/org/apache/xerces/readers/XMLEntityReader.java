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

package org.apache.xerces.readers;

import org.apache.xerces.framework.XMLErrorReporter;

/**
 * This is the class used by the scanner to process the XML data.
 *
 * @see org.apache.xerces.framework.XMLParser
 * @version $Id$
 */
abstract class XMLEntityReader implements XMLEntityHandler.EntityReader {
    /*
     * Instance variables.
     */
    protected XMLEntityHandler fEntityHandler = null;
    protected XMLErrorReporter fErrorReporter = null;
    protected boolean fSendCharDataAsCharArray;
    protected XMLEntityHandler.CharDataHandler fCharDataHandler = null;
    protected boolean fInCDSect = false;
    private boolean fStillActive = true;
    /*
     * These are updated directly by the subclass implementation.
     */
    protected int fCarriageReturnCounter = 1;
    protected int fLinefeedCounter = 1;
    protected int fCharacterCounter = 1;
    protected int fCurrentOffset = 0;
    /**
     * Constructor
     */
    protected XMLEntityReader(XMLEntityHandler entityHandler, XMLErrorReporter errorReporter, boolean sendCharDataAsCharArray) {
        fEntityHandler = entityHandler;
        fErrorReporter = errorReporter;
        fSendCharDataAsCharArray = sendCharDataAsCharArray;
        fCharDataHandler = fEntityHandler.getCharDataHandler();
    }
    /**
     * Constructor
     */
    protected XMLEntityReader(XMLEntityHandler entityHandler, XMLErrorReporter errorReporter, boolean sendCharDataAsCharArray, int lineNumber, int columnNumber) {
        fEntityHandler = entityHandler;
        fErrorReporter = errorReporter;
        fSendCharDataAsCharArray = sendCharDataAsCharArray;
        fCharDataHandler = fEntityHandler.getCharDataHandler();
        fLinefeedCounter = lineNumber;
        fCharacterCounter = columnNumber;
    }
    protected void init(XMLEntityHandler entityHandler, XMLErrorReporter errorReporter, boolean sendCharDataAsCharArray, int lineNumber, int columnNumber) {
        fEntityHandler = entityHandler;
        fErrorReporter = errorReporter;
        fSendCharDataAsCharArray = sendCharDataAsCharArray;
        fCharDataHandler = fEntityHandler.getCharDataHandler();
        fLinefeedCounter = lineNumber;
        fCharacterCounter = columnNumber;
        fStillActive = true;
        fInCDSect = false;
        fCarriageReturnCounter = 1;
        fCurrentOffset = 0;
    }

    /**
     * Return the current offset within this reader.
     *
     * @return The offset.
     */
    public int currentOffset() {
        return fCurrentOffset;
    }

    /**
     * Return the line number of the current position within the document that we are processing.
     *
     * @return The current line number.
     */
    public int getLineNumber() {
        if (fLinefeedCounter > 1)
            return fLinefeedCounter;
        else
            return fCarriageReturnCounter;
    }

    /**
     * Return the column number of the current position within the document that we are processing.
     *
     * @return The current column number.
     */
    public int getColumnNumber() {
        return fCharacterCounter;
    }

    /**
     * This method is provided for scanner implementations.
     */
    public void setInCDSect(boolean inCDSect) {
        fInCDSect = inCDSect;
    }

    /**
     * This method is provided for scanner implementations.
     */
    public boolean getInCDSect() {
        return fInCDSect;
    }

    /**
     * This method is called by the reader subclasses at the end of input.
     */
    protected XMLEntityHandler.EntityReader changeReaders() throws Exception {
        XMLEntityHandler.EntityReader nextReader = null;
        if (fStillActive) {
            nextReader = fEntityHandler.changeReaders();
            fStillActive = false;
        }
        return nextReader;
    }
}
