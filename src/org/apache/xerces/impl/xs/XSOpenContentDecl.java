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

package org.apache.xerces.impl.xs;

import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSOpenContent;
import org.apache.xerces.xs.XSWildcard;

/**
 * The XML representation for an openContent declaration either
 * an &lt;openContent&gt; or a &lt;defaultOpenContent&gt;
 *
 * @xerces.internal 
 *
 * @author Khaled Noaman, IBM
 * @version $Id$
 */
public class XSOpenContentDecl implements XSOpenContent {

	 // the mode of the openContent
    public short fMode = MODE_NONE;
    // the appliesToEmpty flag
    public boolean fAppliesToEmpty = false;
    // the wildcard declaration
    public XSWildcardDecl fWildcard = null;
    
    /**
     * get the string description of this wildcard
     */
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("OC[mode=");
            if (fMode == MODE_NONE) {
            	buffer.append("none,");
            }
            else if (fMode == MODE_INTERLEAVE) {
            	buffer.append("interleave,");
            }
            else {
            	buffer.append("suffix,");
            }
            buffer.append(fWildcard.toString());
            buffer.append("]");
            fDescription = buffer.toString();
        }

        return fDescription;
    }

    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.OPEN_CONTENT;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return null;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return null;
    }

	/**
	 * @see org.apache.xerces.xs.XSObject#getNamespaceItem()
	 */
	public XSNamespaceItem getNamespaceItem() {
        // REVISIT: implement
		return null;
	}

    /**
     * A mode type: none, interleave, suffix. 
     */
    public short getModeType() {
    	return fMode;
    }

    /**
     * A wildcard declaration
     */
    public XSWildcard getWildcard() {
    	return fWildcard;
    }
    
    /**
     * A flag that indicates whether a default open content is applied
     * when the content type of a complex type declaration is empty
     */
    // TODO: do we have two different implementations (i.e. XSOenContentDecl and XSDefaultOpenContentDecl)
    //       and add that method only to XSDefaultOpenContentDecl
    public boolean appliesToEmpty() {
    	return fAppliesToEmpty;
    }
}
