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

package org.apache.xerces.dom3.ls;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException;
import org.apache.xerces.dom3.DOMErrorHandler;

/**
 * A interface to an object that is able to build a DOM tree from various 
 * input sources.
 * <p><code>DOMBuilder</code> provides an API for parsing XML documents and 
 * building the corresponding DOM document tree. A <code>DOMBuilder</code> 
 * instance is obtained from the <code>DOMImplementationLS</code> interface 
 * by invoking its <code>createDOMBuilder</code>method.
 * <p> As specified in , when a document is first made available via the 
 * DOMBuilder: there is only one <code>Text</code> node for each block of 
 * text. The <code>Text</code> nodes are into "normal" form: only structure 
 * (e.g., elements, comments, processing instructions, CDATA sections, and 
 * entity references) separates <code>Text</code> nodes, i.e., there are 
 * neither adjacent <code>Text</code> nodes nor empty <code>Text</code> 
 * nodes.  it is expected that the <code>value</code> and 
 * <code>nodeValue</code> attributes of an <code>Attr</code> node initially 
 * return the XML 1.0 normalized value. However, if the features 
 * <code>validate-if-schema</code> and <code>datatype-normalization</code> 
 * are set to <code>true</code>, depending on the attribute normalization 
 * used, the attribute values may differ from the ones obtained by the XML 
 * 1.0 attribute normalization. If the feature 
 * <code>datatype-normalization</code> is not set to <code>true</code>, the 
 * XML 1.0 attribute normalization is garantee to occur, and if attributes 
 * list does not contain namespace declarations, the <code>attributes</code> 
 * attribute on <code>Element</code> node represents the property 
 * [attributes] defined in  .  XML Schemas does not modified the XML 
 * attribute normalization but represents their normalized value in an other 
 * information item property: [schema normalized value]XML Schema 
 * normalization only occurs if <code>datatype-normalization</code> is set 
 * to <code>true</code>.
 * <p> The Document Object Model Level 3 Load and Save does not provide a way 
 * to disable the namespace resolution: Namespaces are always taken into 
 * account during loading and saving operations. 
 * <p> Asynchronous <code>DOMBuilder</code> objects are expected to also 
 * implement the <code>events::EventTarget</code> interface so that event 
 * listeners can be registerd on asynchronous <code>DOMBuilder</code> 
 * objects. 
 * <p> Events supported by asynchronous <code>DOMBuilder</code> are: ls-load: 
 * The document that's being loaded is completely parsed, see the definition 
 * of <code>LSLoadEvent</code>ls-progress: Progress notification, see the 
 * definition of <code>LSProgressEvent</code>
 * <p> <code>DOMBuilder</code>s have a number of named features that can be 
 * queried or set. The name of <code>DOMBuilder</code> features must be 
 * valid XML names. Implementation specific features (extensions) should 
 * choose a implementation specific prefix to avoid name collisions. 
 * <p> Even if all features must be recognized by all implementations, being 
 * able to set a state (<code>true</code> or <code>false</code>) is not 
 * always required. The following list of recognized features indicates the 
 * definitions of each feature state, if setting the state to 
 * <code>true</code> or <code>false</code> must be supported or is optional 
 * and, which state is the default one: 
 * <dl>
 * <dt><code>"namespace-declarations"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default) include the namespace declaration 
 * attributes, specified or defaulted from the schema or the DTD, in the DOM 
 * document. See also the section Declaring Namespaces in . </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[optional] discard all namespace declaration 
 * attributes. The Namespace prefixes will be retained even if this feature 
 * is set to <code>false</code>. </dd>
 * </dl></dd>
 * <dt><code>"validation"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[
 * optional] report validation errors (setting <code>true</code> also will 
 * force the <code>external-general-entities</code> and 
 * <code>external-parameter-entities</code> features to be <code>true</code>
 * .) Also note that the <code>validate-if-schema</code> feature will alter 
 * the validation behavior when this feature is set <code>true</code>. </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required] (default) do not report validation errors. </dd>
 * </dl></dd>
 * <dt>
 * <code>"external-parameter-entities"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (
 * default)load external parameter entities. </dd>
 * <dt><code>false</code></dt>
 * <dd>[optional]do 
 * not load external parameter entities. </dd>
 * <dt>default value</dt>
 * <dd><code>true</code></dd>
 * </dl></dd>
 * <dt>
 * <code>"external-general-entities"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (
 * default) include all external general (text) entities. </dd>
 * <dt><code>false</code></dt>
 * <dd>[
 * optional]do not include external general entities. </dd>
 * </dl></dd>
 * <dt>
 * <code>"external-dtd-subset"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (default) 
 * load the external dtd and also all external parameter entities. </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[optional] do not load the dtd nor external parameter 
 * entities. </dd>
 * </dl></dd>
 * <dt><code>"validate-if-schema"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional] 
 * when both this feature and validation are <code>true</code>, enable 
 * validation only if the document being processed has a schema (i.e. XML 
 * schema, DTD, any other type of schema, note that this is unrelated to the 
 * abstract schema specification). Documents without schemas are parsed 
 * without validation. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default) the validation 
 * feature alone controls whether the document is checked for validity. 
 * Documents without a schemas are not valid. </dd>
 * </dl></dd>
 * <dt>
 * <code>"validate-against-dtd"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional] Prefere 
 * validation against the DTD over any other schema referenced in the XML 
 * file. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default) Let the parser decide what 
 * to validate against if there are references to multiple types of schemas. </dd>
 * </dl></dd>
 * <dt>
 * <code>"datatype-normalization"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] Let the 
 * (non-DTD) validation process do its datatype normalization that is 
 * defined in the used schema language.  We should define "datatype 
 * normalization". </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default) Disable datatype 
 * normalization. The XML 1.0 attribute value normalization is garantee to 
 * occur in that case. </dd>
 * </dl></dd>
 * <dt><code>"create-entity-ref-nodes"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default) Create <code>EntityReference</code> 
 * nodes in the DOM document. It will also set 
 * <code>create-entity-nodes</code> to be <code>true</code>. </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[optional] omit all <code>EntityReference</code> nodes 
 * from the DOM document, putting the entity expansions directly in their 
 * place. <code>Text</code> nodes are into "normal" form. 
 * <code>EntityReference</code> nodes to non-defined entities will still be 
 * created in the DOM document. </dd>
 * </dl></dd>
 * <dt><code>"create-entity-nodes"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default) Create <code>Entity</code> nodes in 
 * the DOM document. </dd>
 * <dt><code>false</code></dt>
 * <dd>[optional] Omit all 
 * <code>entity</code> nodes from the DOM document. It will also set 
 * <code>create-entity-ref-nodes</code> to <code>false</code>. </dd>
 * </dl></dd>
 * <dt>
 * <code>"whitespace-in-element-content"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (
 * default) Include white space characters appearing within element content 
 * (see  2.10 "White Space Handling"). </dd>
 * <dt><code>false</code></dt>
 * <dd>[optional] Omit 
 * white space characters appearing within element content. Note that white 
 * space characters within element content will only be omitted if it can be 
 * identified as such, and not all parsers may be able to do so (see  2.10 
 * "White Space Handling"). </dd>
 * </dl></dd>
 * <dt><code>"create-cdata-nodes"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default) Create <code>CDATASection</code> 
 * nodes in response to the appearance of CDATA sections in the XML 
 * document. </dd>
 * <dt><code>false</code></dt>
 * <dd>[optional] Do not create 
 * <code>CDATASection</code> nodes in the DOM document.  The content of any 
 * CDATA sections in the XML document appears in the DOM as if it had been 
 * normal (non-CDATA) content. If a CDATA section is adjacent to other 
 * content, the combined content appears in a single <code>Text</code> node, 
 * i.e. the <code>Text</code> nodes are into "normal" form. </dd>
 * </dl></dd>
 * <dt>
 * <code>"comments"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (default) Include XML 
 * comments in the DOM document. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] Discard XML 
 * comments, do not create <code>Comment</code> nodes in the DOM Document 
 * resulting from a parse. </dd>
 * </dl></dd>
 * <dt><code>"charset-overrides-xml-encoding"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default) If a higher level protocol such as 
 * HTTP  provides an indication of the character encoding of the input 
 * stream being processed, that will override any encoding specified in the 
 * XML declaration or the Text declaration (see also  4.3.3 "Character 
 * Encoding in Entities"). Explicitly setting an encoding in the 
 * <code>DOMInputSource</code> overrides encodings from the protocol. </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required] Any character set encoding information from 
 * higher level protocols is ignored by the parser. </dd>
 * </dl></dd>
 * <dt>
 * <code>"load-as-infoset"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional] Load the 
 * document and store only the information defined in the XML Information 
 * Set .  This will force the following features to <code>false</code>: 
 * <code>namespace-declarations</code>, <code>validate-if-schema</code>, 
 * <code>create-entity-ref-nodes</code>, <code>create-entity-nodes</code>, 
 * <code>create-cdata-nodes</code>.  This will force the following features 
 * to <code>true</code>: <code>datatype-normalization</code>, 
 * <code>whitespace-in-element-content</code>, <code>comments</code>, 
 * <code>charset-overrides-xml-encoding</code>.  Other features are not 
 * changed unless explicity specified in the description of the features.  
 * Note that querying this feature with <code>getFeature</code> will return 
 * <code>true</code> only if the individual features specified above are 
 * appropriately set. </dd>
 * <dt><code>false</code></dt>
 * <dd> Setting <code>load-as-infoset</code>
 *  to <code>false</code> has no effect. </dd>
 * </dl></dd>
 * <dt>
 * <code>"supported-mediatypes-only"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional] Check 
 * that the media type of the parsed resource is a supported media type and 
 * call the error handler if an unsupported media type is encountered. The 
 * media types defined in  must be accepted. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (
 * default) Don't check the media type, accept any type of data. </dd>
 * </dl></dd>
 * </dl>
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-ASLS-20011025'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 */
public interface DOMBuilder {
    /**
     * If a <code>DOMEntityResolver</code> has been specified, each time a 
     * reference to an external entity is encountered the 
     * <code>DOMBuilder</code> will pass the public and system IDs to the 
     * entity resolver, which can then specify the actual source of the 
     * entity.
     */
    public DOMEntityResolver getEntityResolver();
    /**
     * If a <code>DOMEntityResolver</code> has been specified, each time a 
     * reference to an external entity is encountered the 
     * <code>DOMBuilder</code> will pass the public and system IDs to the 
     * entity resolver, which can then specify the actual source of the 
     * entity.
     */
    public void setEntityResolver(DOMEntityResolver entityResolver);

    /**
     *  In the event that an error is encountered in the XML document being 
     * parsed, the <code>DOMDcoumentBuilder</code> will call back to the 
     * <code>errorHandler</code> with the error information. When the 
     * document loading process calls the error handler the node closest to 
     * where the error occured is passed to the error handler if the 
     * implementation, if the implementation is unable to pass the node 
     * where the error occures the document Node is passed to the error 
     * handler. Mutations to the document from within an error handler will 
     * result in implementation dependent behavour. 
     */
    public DOMErrorHandler getErrorHandler();
    /**
     *  In the event that an error is encountered in the XML document being 
     * parsed, the <code>DOMDcoumentBuilder</code> will call back to the 
     * <code>errorHandler</code> with the error information. When the 
     * document loading process calls the error handler the node closest to 
     * where the error occured is passed to the error handler if the 
     * implementation, if the implementation is unable to pass the node 
     * where the error occures the document Node is passed to the error 
     * handler. Mutations to the document from within an error handler will 
     * result in implementation dependent behavour. 
     */
    public void setErrorHandler(DOMErrorHandler errorHandler);

    /**
     *  When the application provides a filter, the parser will call out to 
     * the filter at the completion of the construction of each 
     * <code>Element</code> node. The filter implementation can choose to 
     * remove the element from the document being constructed (unless the 
     * element is the document element) or to terminate the parse early. If 
     * the document is being validated when it's loaded the validation 
     * happens before the filter is called. 
     */
    public DOMBuilderFilter getFilter();
    /**
     *  When the application provides a filter, the parser will call out to 
     * the filter at the completion of the construction of each 
     * <code>Element</code> node. The filter implementation can choose to 
     * remove the element from the document being constructed (unless the 
     * element is the document element) or to terminate the parse early. If 
     * the document is being validated when it's loaded the validation 
     * happens before the filter is called. 
     */
    public void setFilter(DOMBuilderFilter filter);

    /**
     * Set the state of a feature.
     * <br>The feature name has the same form as a DOM hasFeature string.
     * <br>It is possible for a <code>DOMBuilder</code> to recognize a feature 
     * name but to be unable to set its value.
     * @param name The feature name.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_SUPPORTED_ERR exception when the <code>DOMBuilder</code> 
     *   recognizes the feature name but cannot set the requested value. 
     *   <br>Raise a NOT_FOUND_ERR When the <code>DOMBuilder</code> does not 
     *   recognize the feature name.
     */
    public void setFeature(String name, 
                           boolean state)
                           throws DOMException;

    /**
     * Query whether setting a feature to a specific value is supported.
     * <br>The feature name has the same form as a DOM hasFeature string.
     * @param name The feature name, which is a DOM has-feature style string.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @return <code>true</code> if the feature could be successfully set to 
     *   the specified value, or <code>false</code> if the feature is not 
     *   recognized or the requested value is not supported. The value of 
     *   the feature itself is not changed.
     */
    public boolean canSetFeature(String name, 
                                 boolean state);

    /**
     * Look up the value of a feature.
     * <br>The feature name has the same form as a DOM hasFeature string
     * @param name The feature name, which is a string with DOM has-feature 
     *   syntax.
     * @return The current state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_FOUND_ERR When the <code>DOMBuilder</code> does not 
     *   recognize the feature name.
     */
    public boolean getFeature(String name)
                              throws DOMException;

    /**
     *  Parse an XML document from a location identified by an URI reference . 
     * If the URI contains a fragment identifier (see section 4.1 in ), the 
     * behavior is not defined by this specification. 
     * @param uri The location of the XML document to be read.
     * @return If the <code>DOMBuilder</code> is a synchronous 
     *   <code>DOMBuilder</code> the newly created and populated 
     *   <code>Document</code> is returned. If the <code>DOMBuilder</code> 
     *   is asynchronous then <code>null</code> is returned since the 
     *   document object is not yet parsed when this method returns.
     * @exception DOMSystemException
     *   Exceptions raised by <code>parseURI</code> originate with the 
     *   installed ErrorHandler, and thus depend on the implementation of 
     *   the <code>DOMErrorHandler</code> interfaces. The default error 
     *   handlers will raise a DOMSystemException if any form I/O or other 
     *   system error occurs during the parse, but application defined error 
     *   handlers are not required to do so. 
     */
    public Document parseURI(String uri)
                             throws Exception;

    /**
     * Parse an XML document from a resource identified by an 
     * <code>DOMInputSource</code>.
     * @param is The <code>DOMInputSource</code> from which the source 
     *   document is to be read. 
     * @return If the <code>DOMBuilder</code> is a synchronous 
     *   <code>DOMBuilder</code> the newly created and populated 
     *   <code>Document</code> is returned. If the <code>DOMBuilder</code> 
     *   is asynchronous then <code>null</code> is returned since the 
     *   document object is not yet parsed when this method returns.
     * @exception DOMSystemException
     *   Exceptions raised by <code>parse</code> originate with the installed 
     *   ErrorHandler, and thus depend on the implementation of the 
     *   <code>DOMErrorHandler</code> interfaces. The default ErrorHandlers 
     *   will raise a <code>DOMSystemException</code> if any form I/O or 
     *   other system error occurs during the parse, but application defined 
     *   ErrorHandlers are not required to do so. 
     */
    public Document parse(DOMInputSource is)
                          throws Exception;

    // ACTION_TYPES
    /**
     * Replace the context node with the result of parsing the input source. 
     * For this action to work the context node must be an 
     * <code>Element</code>, <code>Text</code>, <code>CDATASection</code>, 
     * <code>Comment</code>, <code>ProcessingInstruction</code>, or 
     * <code>EntityReference</code> node.
     */
    public static final short ACTION_REPLACE            = 1;
    /**
     * Append the result of parsing the input source to the context node. For 
     * this action to work, the context node must be an <code>Element</code>.
     */
    public static final short ACTION_APPEND             = 2;
    /**
     * Insert the result of parsing the input source after the context node. 
     * For this action to work the context nodes parent must be an 
     * <code>Element</code>.
     */
    public static final short ACTION_INSERT_AFTER       = 3;
    /**
     * Insert the result of parsing the input source before the context node. 
     * For this action to work the context nodes parent must be an 
     * <code>Element</code>.
     */
    public static final short ACTION_INSERT_BEFORE      = 4;

    /**
     *  Parse an XML document or fragment from a resource identified by an 
     * <code>DOMInputSource</code> and insert the content into an existing 
     * document at the position epcified with the <code>contextNode</code> 
     * and <code>action</code> arguments. When parsing the input stream the 
     * context node is used for resolving unbound namespace prefixes. 
     * @param is  The <code>DOMInputSource</code> from which the source 
     *   document is to be read. 
     * @param cnode  The <code>Node</code> that is used as the context for 
     *   the data that is being parsed. 
     * @param action This parameter describes which action should be taken 
     *   between the new set of node being inserted and the existing 
     *   children of the context node. The set of possible actions is 
     *   defined above. 
     * @exception DOMException
     *   HIERARCHY_REQUEST_ERR: Thrown if this action results in an invalid 
     *   hierarchy (i.e. a Document with more than one document element). 
     */
    public void parseWithContext(DOMInputSource is, 
                                 Node cnode, 
                                 short action)
                                 throws DOMException;

}
