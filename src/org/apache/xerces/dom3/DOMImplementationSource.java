/*
 * Copyright (c) 2001 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

//package org.w3c.dom; 
package org.apache.xerces.dom3;

import org.w3c.dom.DOMImplementation;

/**
 * This interface permits a DOM implementer to supply one or more 
 * implementations, based upon requested features. Each implemented 
 * <code>DOMImplementationSource</code> object is listed in the 
 * binding-specific list of available sources so that its 
 * <code>DOMImplementation</code> objects are made available.
 * <p>See also the <a href='http://www.w3.org/2001/10/WD-DOM-Level-3-Core-20011017'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMImplementationSource {
    /**
     * A method to request a DOM implementation.
     * @param features A string that specifies which features are required. 
     *   This is a space separated list in which each feature is specified 
     *   by its name optionally followed by a space and a version number. 
     *   This is something like: "XML 1.0 Traversal Events 2.0"
     * @return An implementation that has the desired features, or 
     *   <code>null</code> if this source has none.
     */
    public DOMImplementation getDOMImplementation(String features);

}
