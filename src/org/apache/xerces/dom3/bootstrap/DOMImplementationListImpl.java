/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */


/**
 * This class holds a list of DOMImplementations.
 *
 * @since DOM Level 3
 */

package org.apache.xerces.dom3.bootstrap;

import java.util.Vector;

import org.apache.xerces.dom3.DOMImplementationList;
import org.w3c.dom.DOMImplementation;

public class DOMImplementationListImpl
             implements DOMImplementationList { 
    
    private Vector sources;

    /* 
     * Construct an empty list of DOMImplementations
     * @return  an initialized instance of DOMImplementationRegistry
     */ 
    public DOMImplementationListImpl()
    {
        sources = new Vector();    
    }

    /**
     *  Returns the <code>index</code>th item in the collection. If 
     * <code>index</code> is greater than or equal to the number of 
     * <code>DOMImplementation</code>s in the list, this returns 
     * <code>null</code>. 
     * @param index Index into the collection.
     * @return  The <code>DOMImplementation</code> at the <code>index</code>
     *   th position in the <code>DOMImplementationList</code>, or 
     *   <code>null</code> if that is not a valid index. 
     */
    public DOMImplementation item(int index)
    {
       try {
           return (DOMImplementation) sources.elementAt(index);
       } catch (ArrayIndexOutOfBoundsException e) {
           return null;
       }
    }

    /**
     * The number of <code>DOMImplementation</code>s in the list. The range 
     * of valid child node indices is 0 to <code>length-1</code> inclusive. 
     */
    public int getLength() {
        return sources.size();
    }

    /**
     * Add a <code>DOMImplementation</code> in the list.
     */
    public void add(DOMImplementation domImpl) {
        sources.add(domImpl);
    }
}
  