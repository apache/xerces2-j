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

import org.w3c.dom.Entity;

/**
 * The <code>Entity3</code> interface is an extension to the DOM Level 2
 * <code>Entity</code> interface containing the DOM Level 3 additions.
 * <p>See also the <a href='http://www.w3.org/2001/10/WD-DOM-Level-3-Core-20011017'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface Entity3 extends Entity {

    /**
     * An attribute specifying the actual encoding of this entity, when it is 
     * an external parsed entity. This is <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public String getActualEncoding();
    /**
     * An attribute specifying the actual encoding of this entity, when it is 
     * an external parsed entity. This is <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public void setActualEncoding(String actualEncoding);

    /**
     * An attribute specifying, as part of the text declaration, the encoding 
     * of this entity, when it is an external parsed entity. This is 
     * <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public String getEncoding();
    /**
     * An attribute specifying, as part of the text declaration, the encoding 
     * of this entity, when it is an external parsed entity. This is 
     * <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public void setEncoding(String encoding);

    /**
     * An attribute specifying, as part of the text declaration, the version 
     * number of this entity, when it is an external parsed entity. This is 
     * <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public String getVersion();
    /**
     * An attribute specifying, as part of the text declaration, the version 
     * number of this entity, when it is an external parsed entity. This is 
     * <code>null</code> otherwise.
     * @since DOM Level 3
     */
    public void setVersion(String version);

}
