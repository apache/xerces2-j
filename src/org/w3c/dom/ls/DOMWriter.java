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

package org.w3c.dom.ls;

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;
import org.apache.xerces.dom3.DOMConfiguration;


/**
 * <strong>DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time.</strong> <p>
 *  <code>DOMWriter</code> provides an API for serializing (writing) a DOM 
 * document out in an XML document. The XML data is written to an output 
 * stream, the type of which depends on the specific language bindings in 
 * use. 
 * <p> During serialization of XML data, namespace fixup is done when possible 
 * as defined in [<a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020114'>DOM Level 3 Core</a>], Appendix B. [<a href='http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113'>DOM Level 2 Core</a>] allows empty strings as a real namespace 
 * URI. If the <code>namespaceURI</code> of a <code>Node</code> is empty 
 * string, the serialization will treat them as <code>null</code>, ignoring 
 * the prefix if any. should the remark on DOM Level 2 namespace URI 
 * included in the namespace algorithm in Core instead?
 * <p> <code>DOMWriter</code> accepts any node type for serialization. For 
 * nodes of type <code>Document</code> or <code>Entity</code>, well formed 
 * XML will be created if possible. The serialized output for these node 
 * types is either as a Document or an External Entity, respectively, and is 
 * acceptable input for an XML parser. For all other types of nodes the 
 * serialized form is not specified, but should be something useful to a 
 * human for debugging or diagnostic purposes. Note: rigorously designing an 
 * external (source) form for stand-alone node types that don't already have 
 * one defined in [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>] seems a bit much to take on here. 
 * <p>Within a <code>Document</code>, <code>DocumentFragment</code>, or 
 * <code>Entity</code> being serialized, <code>Nodes</code> are processed as 
 * follows <code>Document</code> nodes are written including with the XML 
 * declaration and a DTD subset, if one exists in the DOM. Writing a 
 * <code>Document</code> node serializes the entire document.  
 * <code>Entity</code> nodes, when written directly by 
 * <code>DOMWriter.writeNode</code>, output the entity expansion but no 
 * namespace fixup is done. The resulting output will be valid as an 
 * external entity.  <code>EntityReference</code> nodes are serialized as an 
 * entity reference of the form "<code>&amp;entityName;</code>" in the 
 * output. Child nodes (the expansion) of the entity reference are ignored.  
 * CDATA sections containing content characters that can not be represented 
 * in the specified output encoding are handled according to the 
 * "split-cdata-sections" boolean parameter.  If the boolean parameter is 
 * <code>true</code>, CDATA sections are split, and the unrepresentable 
 * characters are serialized as numeric character references in ordinary 
 * content. The exact position and number of splits is not specified.  If 
 * the boolean parameter is <code>false</code>, unrepresentable characters 
 * in a CDATA section are reported as errors. The error is not recoverable - 
 * there is no mechanism for supplying alternative characters and continuing 
 * with the serialization.  <code>DocumentFragment</code> nodes are 
 * serialized by serializing the children of the document fragment in the 
 * order they appear in the document fragment.  All other node types 
 * (Element, Text, etc.) are serialized to their corresponding XML source 
 * form.  The serialization of a <code>Node</code> does not always generate 
 * a well-formed XML document, i.e. a <code>DOMBuilder</code> might through 
 * fatal errors when parsing the resulting serialization. 
 * <p> Within the character data of a document (outside of markup), any 
 * characters that cannot be represented directly are replaced with 
 * character references. Occurrences of '&lt;' and '&amp;' are replaced by 
 * the predefined entities &amp;lt; and &amp;amp;. The other predefined 
 * entities (&amp;gt, &amp;apos, and &amp;quot;) are not used; these 
 * characters can be included directly. Any character that can not be 
 * represented directly in the output character encoding is serialized as a 
 * numeric character reference. 
 * <p> Attributes not containing quotes are serialized in quotes. Attributes 
 * containing quotes but no apostrophes are serialized in apostrophes 
 * (single quotes). Attributes containing both forms of quotes are 
 * serialized in quotes, with quotes within the value represented by the 
 * predefined entity &amp;quot;. Any character that can not be represented 
 * directly in the output character encoding is serialized as a numeric 
 * character reference. 
 * <p> Within markup, but outside of attributes, any occurrence of a character 
 * that cannot be represented in the output character encoding is reported 
 * as an error. An example would be serializing the element 
 * &lt;LaCa\u00f1ada/&gt; with <code>encoding="us-ascii"</code>. 
 * <p> When requested by setting the <code>normalize-characters</code> boolean 
 * parameter on <code>DOMWriter</code>, all data to be serialized, both 
 * markup and character data, is W3C Text normalized according to the rules 
 * defined in [<a href='http://www.w3.org/TR/2002/WD-charmod-20020430'>CharModel</a>]. The W3C Text normalization process affects only the data as 
 * it is being written; it does not alter the DOM's view of the document 
 * after serialization has completed. 
 * <p> Namespaces are fixed up during serialization, the serialization process 
 * will verify that namespace declarations, namespace prefixes and the 
 * namespace URIs associated with elements and attributes are consistent. If 
 * inconsistencies are found, the serialized form of the document will be 
 * altered to remove them. The method used for doing the namespace fixup 
 * while serializing a document is the algorithm defined in Appendix B.1 
 * "Namespace normalization" of [<a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020114'>DOM Level 3 Core</a>]. previous paragraph to be defined closer 
 * here.
 * <p>Any changes made affect only the namespace prefixes and declarations 
 * appearing in the serialized data. The DOM's view of the document is not 
 * altered by the serialization operation, and does not reflect any changes 
 * made to namespace declarations or prefixes in the serialized output. 
 * <p> While serializing a document the serializer will write out 
 * non-specified values (such as attributes whose <code>specified</code> is 
 * <code>false</code>) if the <code>discard-default-content</code> boolean 
 * parameter is set to <code>true</code>. If the 
 * <code>discard-default-content</code> flag is set to <code>false</code> 
 * and a schema is used for validation, the schema will be also used to 
 * determine if a value is specified or not. If no schema is used, the 
 * <code>specified</code> flag on attribute nodes is used to determine if 
 * attribute values should be written out. 
 * <p> Ref to Core spec (1.1.9, XML namespaces, 5th paragraph) entity ref 
 * description about warning about unbound entity refs. Entity refs are 
 * always serialized as <code>&amp;foo;</code>, also mention this in the 
 * load part of this spec. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-LS-20021022'>Document Object Model (DOM) Level 3 Load and Save Specification</a>.
 */

public interface DOMWriter {

    /**
     *  The configuration used when a document is loaded. 
     * <br>In addition to the boolean parameters and parameters recognized in 
     * the Core module, the <code>DOMConfiguration</code> objects for 
     * <code>DOMWriter</code> adds, or modifies, the following boolean 
     * parameters: 
     * <dl>
     * <dt><code>"entity-resolver"</code></dt>
     * <dd> This parameter is 
     * equivalent to the <code>"entity-resolver"</code> parameter defined in 
     * <code>DOMBuilder.config</code>. </dd>
     * <dt><code>"xml-declaration"</code></dt>
     * <dd>
     * <dl>
     * <dt>
     * <code>true</code></dt>
     * <dd>[required] (default) If a <code>Document</code> Node 
     * or an <code>Entity</code> node is serialized, the XML declaration, or 
     * text declaration, should be included <code>Document.version</code> 
     * and/or an encoding is specified. </dd>
     * <dt><code>false</code></dt>
     * <dd>[required] Do not 
     * serialize the XML and text declarations. </dd>
     * </dl></dd>
     * <dt><code>"canonical-form"</code></dt>
     * <dd>
     * <dl>
     * <dt>
     * <code>true</code></dt>
     * <dd>[optional] This formatting writes the document 
     * according to the rules specified in [<a href='http://www.w3.org/TR/2001/REC-xml-c14n-20010315'>Canonical XML</a>]. Setting this boolean parameter 
     * to true will set the boolean parameter 
     * <code>"format-pretty-print"</code> to false. </dd>
     * <dt><code>false</code></dt>
     * <dd>[
     * required] (default) Do not canonicalize the output. </dd>
     * </dl></dd>
     * <dt>
     * <code>"format-pretty-print"</code></dt>
     * <dd>
     * <dl>
     * <dt><code>true</code></dt>
     * <dd>[optional] 
     * Formatting the output by adding whitespace to produce a 
     * pretty-printed, indented, human-readable form. The exact form of the 
     * transformations is not specified by this specification. Setting this 
     * boolean parameter to true will set the boolean parameter 
     * "canonical-form" to false. </dd>
     * <dt><code>false</code></dt>
     * <dd>[required] (default) 
     * Don't pretty-print the result. </dd>
     * </dl></dd>
     * <dt><code>"normalize-characters"</code></dt>
     * <dd> 
     * This boolean parameter is equivalent to the one defined by 
     * <code>DOMConfiguration</code> in [<a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20020114'>DOM Level 3 Core</a>]. Unlike in the Core, the default 
     * value for this boolean parameter is <code>true</code>. While DOM 
     * implementations are not required to implement the W3C Text 
     * Normalization defined in [<a href='http://www.w3.org/TR/2002/WD-charmod-20020430'>CharModel</a>], this boolean parameter must be activated 
     * by default if supported. </dd>
     * <dt><code>"unknown-characters"</code></dt>
     * <dd>
     * <dl>
     * <dt>
     * <code>true</code></dt>
     * <dd>[required] (default) If, while verifying full 
     * normalization when [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>] is supported, a character is encountered for 
     * which the normalization properties cannot be determined, then ignore 
     * any possible denormalizations caused by these characters. </dd>
     * <dt>
     * <code>false</code></dt>
     * <dd>[optional] Report an fatal error if a character is 
     * encountered for which the processor can not determine the 
     * normalization properties. </dd>
     * </dl></dd>
     * </dl>
     */

    public DOMConfiguration getConfig();


    /**
     *  The character encoding in which the output will be written. 
     * <br> The encoding to use when writing is determined as follows: If the 
     * encoding attribute has been set, that value will be used.If the 
     * encoding attribute is <code>null</code> or empty, but the item to be 
     * written, or the owner document of the item, specifies an encoding 
     * (i.e. the "actualEncoding" from the document) specified encoding, 
     * that value will be used.If neither of the above provides an encoding 
     * name, a default encoding of "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public String getEncoding();
    /**
     *  The character encoding in which the output will be written. 
     * <br> The encoding to use when writing is determined as follows: If the 
     * encoding attribute has been set, that value will be used.If the 
     * encoding attribute is <code>null</code> or empty, but the item to be 
     * written, or the owner document of the item, specifies an encoding 
     * (i.e. the "actualEncoding" from the document) specified encoding, 
     * that value will be used.If neither of the above provides an encoding 
     * name, a default encoding of "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public void setEncoding(String encoding);

    /**
     *  The end-of-line sequence of characters to be used in the XML being 
     * written out. Any string is supported, but these are the recommended 
     * end-of-line sequences (using other character sequences than these 
     * recommended ones can result in a document that is either not 
     * serializable or not well-formed): 
     * <dl>
     * <dt><code>null</code></dt>
     * <dd> Use a default 
     * end-of-line sequence. DOM implementations should choose the default 
     * to match the usual convention for text files in the environment being 
     * used. Implementations must choose a default sequence that matches one 
     * of those allowed by "End-of-Line Handling" (, section 2.11) if the 
     * serialized content is XML 1.0 or "End-of-Line Handling" (, section 
     * 2.11) if the serialized content is XML 1.1. </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return 
     * character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The carriage-return and line-feed characters 
     * (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public String getNewLine();   
    /**
     *  The end-of-line sequence of characters to be used in the XML being 
     * written out. Any string is supported, but these are the recommended 
     * end-of-line sequences (using other character sequences than these 
     * recommended ones can result in a document that is either not 
     * serializable or not well-formed): 
     * <dl>
     * <dt><code>null</code></dt>
     * <dd> Use a default 
     * end-of-line sequence. DOM implementations should choose the default 
     * to match the usual convention for text files in the environment being 
     * used. Implementations must choose a default sequence that matches one 
     * of those allowed by "End-of-Line Handling" (, section 2.11) if the 
     * serialized content is XML 1.0 or "End-of-Line Handling" (, section 
     * 2.11) if the serialized content is XML 1.1. </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return 
     * character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The carriage-return and line-feed characters 
     * (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public void setNewLine(String newLine);

    /**
     *  When the application provides a filter, the serializer will call out 
     * to the filter before serializing each Node. Attribute nodes are never 
     * passed to the filter. The filter implementation can choose to remove 
     * the node from the stream or to terminate the serialization early. 
     */
    public DOMWriterFilter getFilter();
    /**
     *  When the application provides a filter, the serializer will call out 
     * to the filter before serializing each Node. Attribute nodes are never 
     * passed to the filter. The filter implementation can choose to remove 
     * the node from the stream or to terminate the serialization early. 
     */
    public void setFilter(DOMWriterFilter filter);


    /**
     * Write out the specified node as described above in the description of 
     * <code>DOMWriter</code>. Writing a Document or Entity node produces a 
     * serialized form that is well formed XML, when possible (Entity nodes 
     * might not always be well formed XML in themselves). Writing other 
     * node types produces a fragment of text in a form that is not fully 
     * defined by this document, but that should be useful to a human for 
     * debugging or diagnostic purposes. 
     * <br> If the specified encoding is not supported the error handler is 
     * called and the serialization is interrupted. 
     * @param destination The destination for the data to be written.
     * @param wnode The <code>Document</code> or <code>Entity</code> node to 
     *   be written. For other node types, something sensible should be 
     *   written, but the exact serialized form is not specified.
     * @return  Returns <code>true</code> if <code>node</code> was 
     *   successfully serialized and <code>false</code> in case a failure 
     *   occured and the failure wasn't canceled by the error handler. 
     */
    public boolean writeNode(java.io.OutputStream destination, 
                             Node wnode);

    /**
     *  Serialize the specified node as described above in the description of 
     * <code>DOMWriter</code>. The result of serializing the node is 
     * returned as a DOMString (this method completely ignores all the 
     * encoding information available). Writing a Document or Entity node 
     * produces a serialized form that is well formed XML. Writing other 
     * node types produces a fragment of text in a form that is not fully 
     * defined by this document, but that should be useful to a human for 
     * debugging or diagnostic purposes. 
     * <br> Error handler is called if encoding not supported... 
     * @param wnode  The node to be written. 
     * @return  Returns the serialized data, or <code>null</code> in case a 
     *   failure occured and the failure wasn't canceled by the error 
     *   handler. 
     * @exception DOMException
     *    DOMSTRING_SIZE_ERR: Raised if the resulting string is too long to 
     *   fit in a <code>DOMString</code>. 
     */
    public String writeToString(Node wnode)
                                throws DOMException;

}
