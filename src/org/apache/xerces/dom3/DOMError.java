/*
 * Copyright (c) 2002 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 */

package org.apache.xerces.dom3;

/**
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * <p>
 * <code>DOMError</code> is an interface that describes an error.
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020409'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface DOMError {
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * warning
     */
    public static final short SEVERITY_WARNING          = 0;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * error
     */
    public static final short SEVERITY_ERROR            = 1;
    /**
     * The severity of the error described by the <code>DOMError</code> is 
     * fatal error
     */
    public static final short SEVERITY_FATAL_ERROR      = 2;
    /**
     * The severity of the error, either <code>SEVERITY_WARNING</code>, 
     * <code>SEVERITY_ERROR</code>, or <code>SEVERITY_FATAL_ERROR</code>.
     */
    public short getSeverity();

    /**
     * An implementation specific string describing the error that occured.
     */
    public String getMessage();

    /**
     * The related platform dependent exception if any.exception is a reserved 
     * word, we need to rename it.Change to "relatedException". (F2F 26 Sep 
     * 2001)
     */
    public Object getRelatedException();

    /**
     * The location of the error.
     */
    public DOMLocator getLocation();
    
        /**
     *  The related <code>Error.type</code> dependent data if any. 
     */

    public Object getRelatedData();
        /**
     *  A <code>DOMString</code> indicating which related data is expected in 
     * <code>relatedData</code>. Users should refer to the specification of 
     * the error in order to find its <code>DOMString</code> type and 
     * <code>relatedData</code> definitions if any.  As an example, [<a href='http://www.w3.org/TR/DOM-Level-3-LS'>DOM Level 3 Load and Save</a>] does 
     * not keep the [baseURI] property defined on a Processing Instruction 
     * information item. Therefore, the <code>DOMBuilder</code> generates a 
     * <code>SEVERITY_WARNING</code> with <code>type</code> 
     * <code>"infoset-baseURI"</code> and the lost [baseURI] property 
     * represented as a <code>DOMString</code> in the 
     * <code>relatedData</code> attribute. 
     */

    public String getType();


}
