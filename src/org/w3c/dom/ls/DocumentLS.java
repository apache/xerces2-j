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

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

/**
 *  The <code>DocumentLS</code> interface provides a mechanism by which the 
 * content of a document can be serialized, or replaced with the DOM tree 
 * produced when loading a URI, or parsing a string. 
 * <p> If the <code>DocumentLS</code> interface is supported, the expectation 
 * is that an instance of the <code>DocumentLS</code> interface can be 
 * obtained by using binding-specific casting methods on an instance of the 
 * <code>Document</code> interface, or by using the method 
 * <code>Node.getFeature</code> with parameter values 
 * <code>"DocumentLS"</code> and <code>"3.0"</code> (respectively) on an 
 * <code>Document</code>, if the <code>Document</code> supports the feature 
 * <code>"Core"</code> version <code>"3.0"</code> defined in [<a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030609'>DOM Level 3 Core</a>]
 *  
 * <p> This interface is optional. If supported, implementations must support 
 * version <code>"3.0"</code> of the feature <code>"DocumentLS"</code>. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030619'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DocumentLS {
    /**
     *  Indicates whether the method <code>DocumentLS.load()</code> should be 
     * synchronous or asynchronous. When the async attribute is set to 
     * <code>true</code> the load method returns control to the caller 
     * before the document has completed loading. The default value of this 
     * attribute is <code>true</code>. 
     */
    public boolean getAsync();
    /**
     *  Indicates whether the method <code>DocumentLS.load()</code> should be 
     * synchronous or asynchronous. When the async attribute is set to 
     * <code>true</code> the load method returns control to the caller 
     * before the document has completed loading. The default value of this 
     * attribute is <code>true</code>. 
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the implementation doesn't support the 
     *   mode the attribute is being set to.
     */
    public void setAsync(boolean async)
                          throws DOMException;

    /**
     * If the document is currently being loaded as a result of the method 
     * <code>load</code> being invoked the loading and parsing is 
     * immediately aborted. The possibly partial result of parsing the 
     * document is discarded and the document is cleared.
     */
    public void abort();

    /**
     * Replaces the content of the document with the result of parsing the 
     * given URI. Invoking this method will either block the caller or 
     * return to the caller immediately depending on the value of the async 
     * attribute. Once the document is fully loaded a "load" event (as 
     * defined in [<a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Events-20030331'>DOM Level 3 Events</a>]
     * , except that the <code>Event.targetNode</code> will be the document, 
     * not an element) will be dispatched on the document. If an error 
     * occurs, an implementation dependent "error" event will be dispatched 
     * on the document. If this method is called on a document that is 
     * currently loading, the current load is interrupted and the new URI 
     * load is initiated. 
     * <br> When invoking this method the parameters used in the 
     * <code>DOMParser</code> interface are assumed to have their default 
     * values with the exception that the parameters <code>"entities"</code>
     * , <code>"normalize-characters"</code>, 
     * <code>"check-character-normalization"</code> are set to 
     * <code>"false"</code>. 
     * <br> The result of a call to this method is the same the result of a 
     * call to <code>DOMParser.parseWithContext</code> with an input stream 
     * referencing the URI that was passed to this call, the document as the 
     * context node, and the action <code>ACTION_REPLACE_CHILDREN</code>. 
     * @param uri The URI reference for the XML file to be loaded. If this is 
     *   a relative URI, the base URI used by the implementation is 
     *   implementation dependent.
     * @return If async is set to <code>true</code> <code>load</code> returns 
     *   <code>true</code> if the document load was successfully initiated. 
     *   If an error occurred when initiating the document load, 
     *   <code>load</code> returns <code>false</code>.If async is set to 
     *   <code>false</code> <code>load</code> returns <code>true</code> if 
     *   the document was successfully loaded and parsed. If an error 
     *   occurred when either loading or parsing the URI, <code>load</code> 
     *   returns <code>false</code>.
     */
    public boolean load(String uri);

    /**
     *  Replace the content of the document with the result of parsing the 
     * input string, this method is always synchronous. This method always 
     * parses from a DOMString, which means the data is always UTF-16. All 
     * other encoding information is ignored. 
     * <br> The parameters used in the <code>DOMParser</code> interface are 
     * assumed to have their default values when invoking this method.
     * <br> The result of a call to this method is the same the result of a 
     * call to <code>DOMParser.parseWithContext</code> with an input stream 
     * containing the string passed to this call, the document as the 
     * context node, and the action <code>ACTION_REPLACE_CHILDREN</code>. 
     * @param source A string containing an XML document.
     * @return <code>true</code> if parsing the input string succeeded 
     *   without errors, otherwise <code>false</code>.
     */
    public boolean loadXML(String source);

    /**
     * Save the document or the given node and all its descendants to a string 
     * (i.e. serialize the document or node). 
     * <br>The parameters used in the <code>DOMSerializer</code> interface are 
     * assumed to have their default values when invoking this method. 
     * <br> The result of a call to this method is the same the result of a 
     * call to <code>DOMSerializer.writeToString</code> with the document as 
     * the node to write. 
     * @param node Specifies what to serialize, if this parameter is 
     *   <code>null</code> the whole document is serialized, if it's 
     *   non-null the given node is serialized.
     * @return The serialized document or <code>null</code> in case an error 
     *   occurred.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if the node passed in as the node 
     *   parameter is from an other document.
     */
    public String saveXML(Node node)
                          throws DOMException;

}
