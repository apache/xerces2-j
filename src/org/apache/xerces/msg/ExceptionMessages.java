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

package org.apache.xerces.msg;

import java.util.ListResourceBundle;

/**
 * This file contains error and warning messages to be localized
 * The messages are arranged in key and value tuples in a ListResourceBundle.
 *
 * @version
 */
public class ExceptionMessages extends ListResourceBundle {
    /** The list resource bundle contents. */
    public static final Object CONTENTS[][] = {
// Internal message formatter messages

	// org.apache.html

	{ "HTM001", "State error: startDocument fired twice on one builder." },
	{ "HTM002", "State error: document never started or missing document element." },
	{ "HTM003", "State error: document ended before end of document element." },
	{ "HTM004", "Argument 'tagName' is null." },
	{ "HTM005", "State error: Document.getDocumentElement returns null." },
	{ "HTM006", "State error: startElement called after end of document element." },
	{ "HTM007", "State error: endElement called with no current node." },
	{ "HTM008", "State error: mismatch in closing tag name {0}" },
	{ "HTM009", "State error: character data found outside of root element." },
	{ "HTM010", "State error: character data found outside of root element." },
	{ "HTM011", "Argument 'topLevel' is null." },
	{ "HTM012", "Argument 'index' is negative." },
	{ "HTM013", "Argument 'name' is null." },
	{ "HTM014", "Argument 'title' is null." },
	{ "HTM015", "Tag '{0}' associated with an Element class that failed to construct." },
	{ "HTM016", "Argument 'caption' is not an element of type <CAPTION>." },
	{ "HTM017", "Argument 'tHead' is not an element of type <THEAD>." },
	{ "HTM018", "Argument 'tFoot' is not an element of type <TFOOT>." },
	{ "HTM019", "OpenXML Error: Could not find class {0} implementing HTML element {1}" },

	// org.apache.xml.serialize

	{ "SER001", "Argument 'output' is null." },
	{ "SER002", "No writer supplied for serializer" },
	{ "SER003", "The resource [{0}] could not be found." },
	{ "SER004", "The resource [{0}] could not load: {1}" },
	{ "SER005", "The method '{0}' is not supported by this factory" },

	// org.apache.xerces.dom

	{ "DOM001", "Modification not allowed" },
	{ "DOM002", "Illegal character" },
	{ "DOM003", "Namespace error" },
	{ "DOM004", "Index out of bounds" },
	{ "DOM005", "Wrong document" },
	{ "DOM006", "Hierarchy request error" },
	{ "DOM007", "Not supported" },
	{ "DOM008", "Not found" },
	{ "DOM009", "Attribute already in use" },
	{ "DOM010", "Unspecified event type" },
	{ "DOM011", "Invalid state" },
	{ "DOM012", "Invalid node type" },
	{ "DOM013", "Bad boundary points" }

        // org.apache.xerces.readers

        { "RDR001", "untested 1" },
        { "RDR002", "cannot happen 7" },
        { "RDR003", "cannot happen 8" },
        { "RDR004", "cannot happen 9" }
            
        //org.apache.xerces
        
    };
    /** Returns the list resource bundle contents. */

    public Object[][] getContents() {
        return CONTENTS;
    }
}
