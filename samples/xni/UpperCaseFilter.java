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

package xni;

import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;
 
/**
 * This sample demonstrates how to create a filter for the document
 * "streaming" information set that turns element names into upper
 * case.
 * <p>
 * <strong>Note:</strong> This sample does not contain a 
 * <code>main</code> method and cannot be run. It is only for
 * demonstration purposes.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class UpperCaseFilter
    extends PassThroughFilter {
    
    //
    // Data
    //
    
    /** 
     * Temporary QName structure used by the <code>toUpperCase</code>
     * method. It should not be used anywhere else.
     *
     * @see #toUpperCase
     */
    private final QName fQName = new QName();

    //
    // XMLDocumentHandler methods
    //
    
    /**
     * The start of an element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        super.startElement(toUpperCase(element), attributes, augs);
    } // startElement(QName,XMLAttributes)
    
    /**
     * An empty element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        super.emptyElement(toUpperCase(element), attributes, augs);
    } // emptyElement(QName,XMLAttributes)
    
    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endElement(QName element, Augmentations augs)
        throws XNIException {
        super.endElement(toUpperCase(element), augs);
    } // endElement(QName)
    
    //
    // Protected methods
    //
    
    /**
     * This method upper-cases the prefix, localpart, and rawname
     * fields in the specified QName and returns a different
     * QName object containing the upper-cased string values.
     *
     * @param qname The QName to upper-case.
     */
    protected QName toUpperCase(QName qname) {
        String prefix = qname.prefix != null
                      ? qname.prefix.toUpperCase() : null;
        String localpart = qname.localpart != null
                         ? qname.localpart.toUpperCase() : null;
        String rawname = qname.rawname != null
                       ? qname.rawname.toUpperCase() : null;
        String uri = qname.uri;
        fQName.setValues(prefix, localpart, rawname, uri);
        return fQName;
    } // toUpperCase(QName):QName

} // class UpperCaseFilter
