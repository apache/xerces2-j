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

package org.w3c.dom.ls;

/**
 *  The <code>ElementLS</code> interface provides a convenient mechanism by 
 * which the children of an element can be serialized to a string, or 
 * replaced by the result of parsing a provided string. 
 * <p> If the <code>ElementLS</code> interface is supported, the expectation 
 * is that an instance of the <code>ElementLS</code> interface can be 
 * obtained by using binding-specific casting methods on an instance of the 
 * <code>Element</code> interface, or by using the method 
 * <code>Node.getFeature</code> with parameter values 
 * <code>"ElementLS"</code> and <code>"3.0"</code> (respectively) on an 
 * <code>Element</code>, if the <code>Element</code> supports the feature 
 * <code>"Core"</code> version <code>"3.0"</code> defined in [<a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030609'>DOM Level 3 Core</a>]
 * . 
 * <p> This interface is optional. If supported, implementations must support 
 * version <code>"3.0"</code> of the feature <code>"ElementLS"</code>. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030619'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface ElementLS {
    /**
     *  The content of the element in serialized form. 
     * <br> When getting the value of this attribute, the children are 
     * serialized in document order and the serialized result is returned. 
     * This is equivalent of calling 
     * <code>DOMSerializer.writeToString()</code> on all children in 
     * document order and appending the result of the individual results to 
     * a single string that is then returned as the value of this attribute. 
     * <br> When setting the value of this attribute, all children of the 
     * element are removed, the provided string is parsed and the result of 
     * the parse operation is inserted into the element. This is equivalent 
     * of calling <code>DOMParser.parseWithContext()</code> passing in the 
     * provided string (through the input source argument), the 
     * <code>Element</code>, and the action 
     * <code>ACTION_REPLACE_CHILDREN</code>. If an error occurs while 
     * parsing the provided string, the <code>Element</code>'s owner 
     * document's error handler will be called, and the <code>Element</code> 
     * is left with no children. 
     * <br> Both setting and getting the value of this attribute assumes that 
     * the parameters in the <code>DOMConfiguration</code> object have their 
     * default values. 
     */
    public String getMarkupContent();
    /**
     *  The content of the element in serialized form. 
     * <br> When getting the value of this attribute, the children are 
     * serialized in document order and the serialized result is returned. 
     * This is equivalent of calling 
     * <code>DOMSerializer.writeToString()</code> on all children in 
     * document order and appending the result of the individual results to 
     * a single string that is then returned as the value of this attribute. 
     * <br> When setting the value of this attribute, all children of the 
     * element are removed, the provided string is parsed and the result of 
     * the parse operation is inserted into the element. This is equivalent 
     * of calling <code>DOMParser.parseWithContext()</code> passing in the 
     * provided string (through the input source argument), the 
     * <code>Element</code>, and the action 
     * <code>ACTION_REPLACE_CHILDREN</code>. If an error occurs while 
     * parsing the provided string, the <code>Element</code>'s owner 
     * document's error handler will be called, and the <code>Element</code> 
     * is left with no children. 
     * <br> Both setting and getting the value of this attribute assumes that 
     * the parameters in the <code>DOMConfiguration</code> object have their 
     * default values. 
     */
    public void setMarkupContent(String markupContent);

}
