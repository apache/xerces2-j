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

import org.w3c.dom.Document;
import org.apache.xerces.dom3.DOMConfiguration;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

/**
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 *
 *  A interface to an object that is able to build a DOM tree from various 
 * input sources. 
 * <p> <code>DOMBuilder</code> provides an API for parsing XML documents and 
 * building the corresponding DOM document tree. A <code>DOMBuilder</code> 
 * instance is obtained by invoking the 
 * <code>DOMImplementationLS.createDOMBuilder</code> method. 
 * <p> As specified in [<a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20021022'>DOM Level 3 Core</a>]
 * , when a document is first made available via the DOMBuilder: 
 * <ul>
 * <li> there is 
 * only one <code>Text</code> node for each block of text. The 
 * <code>Text</code> nodes are in "normal" form: only structure (e.g. 
 * elements, comments, processing instructions, CDATA sections, and entity 
 * references) separates <code>Text</code> nodes, i.e., there are neither 
 * adjacent nor empty <code>Text</code> nodes. 
 * </li>
 * <li> it is expected that the 
 * <code>value</code> and <code>nodeValue</code> attributes of an 
 * <code>Attr</code> node initially return the <a href='http://www.w3.org/TR/2000/REC-xml-20001006#AVNormalize'>XML 1.0 
 * normalized value</a>. However, if the boolean parameters <code>validate-if-schema</code> and 
 * <code>datatype-normalization</code> are set to <code>true</code>, 
 * depending on the attribute normalization used, the attribute values may 
 * differ from the ones obtained by the XML 1.0 attribute normalization. If 
 * the boolean parameter <code>datatype-normalization</code> is set to 
 * <code>false</code>, the XML 1.0 attribute normalization is guaranteed to 
 * occur, and if attributes list does not contain namespace declarations, 
 * the <code>attributes</code> attribute on <code>Element</code> node 
 * represents the property [attributes] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
 * .  XML Schemas does not modify the XML attribute normalization but 
 * represents their normalized value in an other information item property: 
 * [schema normalized value]XML Schema normalization only occurs if 
 * <code>datatype-normalization</code> is set to <code>true</code>.
 * </li>
 * </ul>
 * <p> Asynchronous <code>DOMBuilder</code> objects are expected to also 
 * implement the <code>events::EventTarget</code> interface so that event 
 * listeners can be registered on asynchronous <code>DOMBuilder</code> 
 * objects. 
 * <p> Events supported by asynchronous <code>DOMBuilder</code> objects are: 
 * <ul>
 * <li><b>load</b>: The document that's being loaded is completely parsed, see the 
 * definition of <code>LSLoadEvent</code>
 * </li>
 * <li><b>progress</b>: Progress notification, see the definition of <code>LSProgressEvent</code>
 * </li>
 * </ul>
 * <p ><b>Note:</b>  All events defined in this specification use the 
 * namespace URI "<a href='http://www.w3.org/2002/DOMLS'>http://www.w3.org/2002/DOMLS</a>".  ED: State that a parse operation may fail due to security reasons (DOM 
 * LS telecon 20021202). 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030226'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMBuilder {
    /**
     *  The configuration used when a document is loaded. The values of 
     * parameters used to load a document are not passed automatically to 
     * the <code>DOMConfiguration</code> object used by the 
     * <code>Document</code> nodes. The DOM application is responsible for 
     * passing the parameters values from the <code>DOMConfiguration</code> 
     * object referenced from the <code>DOMBuilder</code> to the 
     * <code>DOMConfiguration</code> object referenced from the 
     * <code>Document</code> node. 
     * <br> In addition to the boolean parameters and parameters recognized in 
     * the Core module, the <code>DOMConfiguration</code> objects for 
     * <code>DOMBuider</code> adds the following boolean parameters: 
     * <dl>
     * <dt>
     * <code>"entity-resolver"</code></dt>
     * <dd>[<em>required</em>] A <code>DOMEntityResolver</code> object. If this parameter has been 
     * specified, each time a reference to an external entity is encountered 
     * the implementation will pass the public and system IDs to the entity 
     * resolver, which can then specify the actual source of the entity.  If 
     * this parameter is not set, the resolution of entities in the document 
     * is implementation dependent. 
     * <p ><b>Note:</b>  When the features "LS-Load" or "LS-Save" are 
     * supported, this parameter may also be supported by the 
     * <code>DOMConfiguration</code> object referenced from the 
     * <code>Document</code> node. </dd>
     * <dt><code>"certified"</code></dt>
     * <dd>
     * <dl>
     * <dt><code>true</code></dt>
     * <dd>[<em>optional</em>] Assume, when XML 1.1 is supported, that the input is certified (see 
     * section 2.13 in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]). </dd>
     * <dt>
     * <code>false</code></dt>
     * <dd>[<em>required</em>] (<em>default</em>) Don't assume that the input is certified (see section 2.13 in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]). </dd>
     * </dl></dd>
     * <dt>
     * <code>"charset-overrides-xml-encoding"</code></dt>
     * <dd>
     * <dl>
     * <dt><code>true</code></dt>
     * <dd>[<em>required</em>] (<em>default</em>) If a higher level protocol such as HTTP [<a href='http://www.ietf.org/rfc/rfc2616.txt'>IETF RFC 2616</a>] provides an 
     * indication of the character encoding of the input stream being 
     * processed, that will override any encoding specified in the XML 
     * declaration or the Text declaration (see also [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>] 4.3.3 
     * "Character Encoding in Entities"). Explicitly setting an encoding in 
     * the <code>DOMInputSource</code> overrides encodings from the 
     * protocol. </dd>
     * <dt><code>false</code></dt>
     * <dd>[<em>required</em>] Any character set encoding information from higher level protocols is 
     * ignored by the parser. </dd>
     * </dl></dd>
     * <dt><code>"supported-mediatypes-only"</code></dt>
     * <dd>
     * <dl>
     * <dt>
     * <code>true</code></dt>
     * <dd>[<em>optional</em>] Check that the media type of the parsed resource is a supported media 
     * type. If an unsupported media type is encountered, a fatal error of 
     * type <b>"unsupported-media-type"</b> will be raised. The media types defined in [<a href='http://www.ietf.org/rfc/rfc3023.txt'>IETF RFC 3023</a>] must always 
     * be accepted. </dd>
     * <dt><code>false</code></dt>
     * <dd>[<em>required</em>] (<em>default</em>) Accept any media type. </dd>
     * </dl></dd>
     * <dt><code>"unknown-characters"</code></dt>
     * <dd>
     * <dl>
     * <dt>
     * <code>true</code></dt>
     * <dd>[<em>required</em>] (<em>default</em>) If, while verifying full normalization when [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>] is 
     * supported, a processor encounters characters for which it cannot 
     * determine the normalization properties, then the processor will 
     * ignore any possible denormalizations caused by these characters.  
     * This parameter is ignored [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>]. </dd>
     * <dt>
     * <code>false</code></dt>
     * <dd>[<em>optional</em>] Report an fatal error if a character is encountered for which the 
     * processor can not determine the normalization properties. </dd>
     * </dl></dd>
     * </dl>
     */
    public DOMConfiguration getConfig();

    /**
     *  When a filter is provided, the implementation will call out to the 
     * filter as it is constructing the DOM tree structure. The filter can 
     * choose to remove elements from the document being constructed, or to 
     * terminate the parsing early. 
     * <br> The filter is invoked after the operations requested by the 
     * <code>DOMConfiguration</code> parameters have been applied. For 
     * example, if <code>"validate"</code> is set to true, the validation is 
     * done before invoking the filter. 
     */
    public DOMBuilderFilter getFilter();
    /**
     *  When a filter is provided, the implementation will call out to the 
     * filter as it is constructing the DOM tree structure. The filter can 
     * choose to remove elements from the document being constructed, or to 
     * terminate the parsing early. 
     * <br> The filter is invoked after the operations requested by the 
     * <code>DOMConfiguration</code> parameters have been applied. For 
     * example, if <code>"validate"</code> is set to true, the validation is 
     * done before invoking the filter. 
     */
    public void setFilter(DOMBuilderFilter filter);

    /**
     *  True if the <code>DOMBuider</code> is asynchronous, false if it is 
     * synchronous. 
     */
    public boolean getAsync();

    /**
     *  True if the <code>DOMBuider</code> is currently busy loading a 
     * document, otherwise false. 
     */
    public boolean getBusy();

    /**
     * Parse an XML document from a resource identified by a 
     * <code>DOMInputSource</code>.
     * @param is  The <code>DOMInputSource</code> from which the source of 
     *   the document is to be read. 
     * @return  If the <code>DOMBuilder</code> is a synchronous 
     *   <code>DOMBuilder</code>, the newly created and populated 
     *   <code>Document</code> is returned. If the <code>DOMBuilder</code> 
     *   is asynchronous, <code>null</code> is returned since the document 
     *   object may not yet be constructed when this method returns. 
     * @exception DOMException
     *    INVALID_STATE_ERR: Raised if the <code>DOMBuilder</code>'s 
     *   <code>DOMBuilder.busy</code> attribute is true. 
     */
    public Document parse(DOMInputSource is)
                          throws DOMException;

    /**
     *  Parse an XML document from a location identified by a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]. If the URI 
     * contains a fragment identifier (see section 4.1 in [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>]), the 
     * behavior is not defined by this specification, future versions of 
     * this specification may define the behavior. 
     * @param uri The location of the XML document to be read.
     * @return  If the <code>DOMBuilder</code> is a synchronous 
     *   <code>DOMBuilder</code>, the newly created and populated 
     *   <code>Document</code> is returned. If the <code>DOMBuilder</code> 
     *   is asynchronous, <code>null</code> is returned since the document 
     *   object may not yet be constructed when this method returns. 
     * @exception DOMException
     *    INVALID_STATE_ERR: Raised if the <code>DOMBuilder</code>'s 
     *   <code>DOMBuilder.busy</code> attribute is true. 
     */
    public Document parseURI(String uri)
                             throws DOMException;

    // ACTION_TYPES
    /**
     *  Append the result of the parse operation as children of the context 
     * node. For this action to work, the context node must be an 
     * <code>Element</code> or a <code>DocumentFragment</code>. 
     */
    public static final short ACTION_APPEND_AS_CHILDREN = 1;
    /**
     *  Replace all the children of the context node with the result of the 
     * parse operation. For this action to work, the context node must be an 
     * <code>Element</code> or a <code>DocumentFragment</code>. 
     */
    public static final short ACTION_REPLACE_CHILDREN   = 2;
    /**
     *  Insert the result of the parse operation as the immediately preceeding 
     * sibling of the context node. For this action to work the context 
     * node's parent must be an <code>Element</code> or a 
     * <code>DocumentFragment</code>. 
     */
    public static final short ACTION_INSERT_BEFORE      = 3;
    /**
     *  Insert the result of the parse operation as the immediately following 
     * sibling of the context node. For this action to work the context 
     * node's parent must be an <code>Element</code> or a 
     * <code>DocumentFragment</code>. 
     */
    public static final short ACTION_INSERT_AFTER       = 4;
    /**
     *  Replace the context node with the result of the parse operation. For 
     * this action to work, the context node must have a parent, and the 
     * parent must be an <code>Element</code> or a 
     * <code>DocumentFragment</code>. 
     */
    public static final short ACTION_REPLACE            = 5;

    /**
     *  Parse an XML fragment from a resource identified by a 
     * <code>DOMInputSource</code> and insert the content into an existing 
     * document at the position specified with the <code>contextNode</code> 
     * and <code>action</code> arguments. When parsing the input stream, the 
     * context node is used for resolving unbound namespace prefixes. The 
     * context node's <code>ownerDocument</code> node is used to resolve 
     * default attributes and entity references. 
     * <br> As the new data is inserted into the document, at least one 
     * mutation event is fired per immediate child (or sibling) of context 
     * node. 
     * <br> If an error occurs while parsing, the caller is notified through 
     * the error handler. 
     * @param is  The <code>DOMInputSource</code> from which the source 
     *   document is to be read. The source document must be an XML 
     *   fragment, i.e. anything except a complete XML document, a DOCTYPE 
     *   (internal subset), entity declaration(s), notation declaration(s), 
     *   or XML or text declaration(s). 
     * @param cnode  The node that is used as the context for the data that 
     *   is being parsed. This node must be a <code>Document</code> node, a 
     *   <code>DocumentFragment</code> node, or a node of a type that is 
     *   allowed as a child of an <code>Element</code> node, e.g. it can not 
     *   be an <code>Attribute</code> node. 
     * @param action This parameter describes which action should be taken 
     *   between the new set of node being inserted and the existing 
     *   children of the context node. The set of possible actions is 
     *   defined above.
     * @return  Return the node that is the result of the parse operation. If 
     *   the result is more than one top-level node, the first one is 
     *   returned. 
     * @exception DOMException
     *    NOT_SUPPORTED_ERR: Raised if the <code>DOMBuilder</code> doesn't 
     *   support this method. 
     *   <br> NO_MODIFICATION_ALLOWED_ERR: Raised if the context node is 
     *   readonly.
     *   <br> INVALID_STATE_ERR: Raised if the <code>DOMBuilder</code>'s 
     *   <code>DOMBuilder.busy</code> attribute is true. 
     */
    public Node parseWithContext(DOMInputSource is, 
                                 Node cnode, 
                                 short action)
                                 throws DOMException;

    /**
     *  Abort the loading of the document that is currently being loaded by 
     * the <code>DOMBuilder</code>. If the <code>DOMBuilder</code> is 
     * currently not busy, a call to this method does nothing. 
     */
    public void abort();

}
