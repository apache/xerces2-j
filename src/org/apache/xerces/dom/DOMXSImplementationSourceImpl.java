/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.dom;

import org.apache.xerces.dom3.DOMImplementationList;
import org.apache.xerces.dom3.bootstrap.DOMImplementationListImpl;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.w3c.dom.DOMImplementation;

/**
 * Allows to retrieve <code>XSImplementation</code>, DOM Level 3 Core and LS implementations
 * and PSVI implementation.
 * <p>See also the <a href='http://www.w3.org/2001/10/WD-DOM-Level-3-Core-20011017'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class DOMXSImplementationSourceImpl
    extends DOMImplementationSourceImpl{

    /**
     * A method to request a DOM implementation.
     * @param features A string that specifies which features are required. 
     *   This is a space separated list in which each feature is specified 
     *   by its name optionally followed by a space and a version number. 
     *   This is something like: "XML 1.0 Traversal Events 2.0"
     * @return An implementation that has the desired features, or 
     *   <code>null</code> if this source has none.
     */
    public DOMImplementation getDOMImplementation(String features) {
        DOMImplementation impl = super.getDOMImplementation(features);
        if (impl != null){
        	return impl;
        }
		// if not try the PSVIDOMImplementation
		impl = PSVIDOMImplementationImpl.getDOMImplementation();
		if (testImpl(impl, features)) {
			return impl;
		}
		// if not try the XSImplementation
		impl = XSImplementationImpl.getDOMImplementation();
		if (testImpl(impl, features)) {
			return impl;
		}
        
        return null;
    }
    
    /**
     * DOM Level 3 Core - CR
     *
     * A method to request a list of DOM implementations that support the 
     * specified features and versions, as specified in .
     * @param features A string that specifies which features and versions 
     *   are required. This is a space separated list in which each feature 
     *   is specified by its name optionally followed by a space and a 
     *   version number. This is something like: "XML 3.0 Traversal +Events 
     *   2.0"
     * @return A list of DOM implementations that support the desired 
     *   features.
     */
    public DOMImplementationList getDOMImplementationList(String features) {
        // first check whether the CoreDOMImplementation would do
		DOMImplementationListImpl list = (DOMImplementationListImpl)super.getDOMImplementationList(features);
		DOMImplementation impl = PSVIDOMImplementationImpl.getDOMImplementation();
		if (testImpl(impl, features)) {
			list.add(impl);
		}
        
         impl = XSImplementationImpl.getDOMImplementation();
        if (testImpl(impl, features)) {
            list.add(impl);
        }
        return list;
    }

}
