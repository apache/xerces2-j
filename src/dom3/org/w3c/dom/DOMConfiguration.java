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

package org.w3c.dom;

/**
 * DOM Level 3 WD Experimental:
 *  The <code>DOMConfiguration</code> interface represents the configuration 
 * of a document and maintains a table of recognized parameters. Using the 
 * configuration, it is possible to change 
 * <code>Document.normalizeDocument</code> behavior, such as replacing the 
 * <code>CDATASection</code> nodes with <code>Text</code> nodes or 
 * specifying the type of the schema that must be used when the validation 
 * of the <code>Document</code> is requested. <code>DOMConfiguration</code> 
 * objects are also used in [<a href='http://www.w3.org/TR/DOM-Level-3-LS'>DOM Level 3 Load and Save</a>] in the <code>DOMBuilder</code> and 
 * <code>DOMWriter</code> interfaces. 
 * <p> The <code>DOMConfiguration</code> distinguish two types of parameters: 
 * <code>boolean</code> (boolean parameters) and <code>DOMUserData</code> 
 * (parameters). The names used by the <code>DOMConfiguration</code> object 
 * are defined throughout the DOM Level 3 specifications. Names are 
 * case-insensitives. To avoid possible conflicts, as a convention, names 
 * referring to boolean parameters and parameters defined outside the DOM 
 * specification should be made unique. Names are recommended to follow the 
 * XML name production rule but it is not enforced by the DOM 
 * implementation. DOM Level 3 Core Implementations are required to 
 * recognize all boolean parameters and parameters defined in this 
 * specification. Each boolean parameter state or parameter value may then 
 * be supported or not by the implementation. Refer to their definition to 
 * know if a state or a value must be supported or not.  Parameters are 
 * similar to features and properties used in SAX2 [<a href='http://www.saxproject.org/'>SAX</a>].  Can we rename boolean 
 * parameters to "flags"?  Are boolean parameters and parameters within the 
 * same scope for uniqueness? Which exception should be raised by 
 * <code>setBooleanParameter("error-handler", true)</code>? 
 * <p> The following list of parameters defined in the DOM: 
 * <dl>
 * <dt>
 * <code>"error-handler"</code></dt>
 * <dd>[required] A <code>DOMErrorHandler</code> 
 * object. If an error is encountered in the document, the implementation 
 * will call back the <code>DOMErrorHandler</code> registered using this 
 * parameter.  When called, <code>DOMError.relatedData</code> will contain 
 * the closest node to where the error occured. If the implementation is 
 * unable to determine the node where the error occurs, 
 * <code>DOMError.relatedData</code> will contain the <code>Document</code> 
 * node. Mutations to the document from within an error handler will result 
 * in implementation dependent behaviour.  Should we say non "readonly" 
 * operations are implementation dependent instead?  Removed: "or 
 * re-invoking a validation operation". </dd>
 * <dt><code>"schema-type"</code></dt>
 * <dd>[optional] 
 * A <code>DOMString</code> object containing an absolute URI and 
 * representing the type of the schema language used to validate a document 
 * against. Note that no lexical checking is done on the absolute URI.  If 
 * this parameter is not set, a default value may be provided by the 
 * implementation, based on the schema languages supported and on the schema 
 * language used at load time.  For XML Schema [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>], applications must use the 
 * value <code>"http://www.w3.org/2001/XMLSchema"</code>. For XML DTD [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>], 
 * applications must use the value 
 * <code>"http://www.w3.org/TR/REC-xml"</code>. Other schema languages are 
 * outside the scope of the W3C and therefore should recommend an absolute 
 * URI in order to use this method. </dd>
 * <dt><code>"schema-location"</code></dt>
 * <dd>[optional] 
 * A <code>DOMString</code> object containing a list of URIs, separated by 
 * white spaces (characters matching the nonterminal production S defined in 
 * section 2.3 [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>]), that represents the schemas against which validation 
 * should occur. The types of schemas referenced in this list must match the 
 * type specified with <code>schema-type</code>, otherwise the behaviour of 
 * an implementation is undefined. If the schema type is XML Schema [<a href='http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/'>XML Schema Part 1</a>], only 
 * one of the XML Schemas in the list can be with no namespace.  If 
 * validation occurs against a namespace aware schema, i.e. XML Schema, and 
 * the targetNamespace of a schema (specified using this property) matches 
 * the targetNamespace of a schema occurring in the instance document, i.e 
 * in schemaLocation attribute, the schema specified by the user using this 
 * property will be used (i.e., in XML Schema the <code>schemaLocation</code>
 *  attribute in the instance document or on the <code>import</code> element 
 * will be effectively ignored).  It is illegal to set the schema-location 
 * parameter if the schema-type parameter value is not set. It is strongly 
 * recommended that <code>DOMInputSource.baseURI</code> will be set, so that 
 * an implementation can successfully resolve any external entities 
 * referenced. </dd>
 * </dl>
 * <p> The following list of boolean parameters (features) defined in the DOM: 
 * <dl>
 * <dt>
 * <code>"canonical-form"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional]Canonicalize the 
 * document according to the rules specified in [<a href='http://www.w3.org/TR/2001/REC-xml-c14n-20010315'>Canonical XML</a>]. Note that this is limited 
 * to what can be represented in the DOM. In particular, there is no way to 
 * specify the order of the attributes in the DOM. What happen to other 
 * features? are they ignored? if yes, how do you know if a feature is 
 * ignored? </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default)Do not canonicalize the 
 * document.</dd>
 * </dl></dd>
 * <dt><code>"cdata-sections"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (default
 * )Keep <code>CDATASection</code> nodes in the document.Name does not work 
 * really well in this case. ALH suggests renaming this to "cdata-sections". 
 * It works for both load and save.Renamed as suggested. (Telcon 27 Jan 
 * 2002).</dd>
 * <dt><code>false</code></dt>
 * <dd>[required]Transform <code>CDATASection</code> 
 * nodes in the document into <code>Text</code> nodes. The new 
 * <code>Text</code> node is then combined with any adjacent 
 * <code>Text</code> node.</dd>
 * </dl></dd>
 * <dt><code>"comments"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] 
 * (default)Keep <code>Comment</code> nodes in the document.</dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required]Discard <code>Comment</code> nodes in the 
 * Document.</dd>
 * </dl></dd>
 * <dt><code>"datatype-normalization"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required]
 * Let the validation process do its datatype normalization that is defined 
 * in the used schema language. Note that this does not affect the DTD 
 * normalization operation which always takes place, in accordance to [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>].We 
 * should define "datatype normalization".DTD normalization always apply 
 * because it's part of XML 1.0. Clarify the spec. (Telcon 27 Jan 2002).</dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required] (default)Disable datatype normalization. The 
 * XML 1.0 attribute value normalization always occurs though.</dd>
 * </dl></dd>
 * <dt>
 * <code>"discard-default-content"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (default
 * )Use whatever information available to the implementation (i.e. XML 
 * schema, DTD, the <code>specified</code> flag on <code>Attr</code> nodes, 
 * and so on) to decide what attributes and content should be discarded or 
 * not. Note that the <code>specified</code> flag on <code>Attr</code> nodes 
 * in itself is not always reliable, it is only reliable when it is set to 
 * <code>false</code> since the only case where it can be set to 
 * <code>false</code> is if the attribute was created by the implementation. 
 * The default content won't be removed if an implementation does not have 
 * any information available.How does exactly work? What's the comment about 
 * level 1 implementations?Remove "Level 1" (Telcon 16 Jan 2002).</dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required]Keep all attributes and all content.</dd>
 * </dl></dd>
 * <dt>
 * <code>"entities"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required]Keep 
 * <code>EntityReference</code> and <code>Entity</code> nodes in the 
 * document.How does that interact with expand-entity-references? ALH 
 * suggests consolidating the two to a single feature called 
 * "entity-references" that is used both for load and save.Consolidate both 
 * features into a single feature called 'entities'. (Telcon 27 Jan 2002).</dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[required] (default)Remove all 
 * <code>EntityReference</code> and <code>Entity</code> nodes from the 
 * document, putting the entity expansions directly in their place. 
 * <code>Text</code> nodes are into "normal" form. Only 
 * <code>EntityReference</code> nodes to non-defined entities are kept in 
 * the document.</dd>
 * </dl></dd>
 * <dt><code>"infoset"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required]Only keep 
 * in the document the information defined in the XML Information Set [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>].This 
 * forces the following features to <code>false</code>: 
 * <code>namespace-declarations</code>, <code>validate-if-schema</code>, 
 * <code>entities</code>, <code>datatype-normalization</code>, 
 * <code>cdata-sections</code>.This forces the following features to 
 * <code>true</code>: <code>whitespace-in-element-content</code>, 
 * <code>comments</code>, <code>namespaces</code>.Other features are not 
 * changed unless explicity specified in the description of the features. 
 * Note that querying this feature with <code>getFeature</code> returns 
 * <code>true</code> only if the individual features specified above are 
 * appropriately set.Name doesn't work well here. ALH suggests renaming this 
 * to limit-to-infoset or match-infoset, something like that.Renamed 
 * 'infoset' (Telcon 27 Jan 2002).</dd>
 * <dt><code>false</code></dt>
 * <dd>Setting 
 * <code>infoset</code> to <code>false</code> has no effect.Shouldn't we 
 * change this to setting the relevant options back to their default value?
 * No, this is more like a convenience function, it's better to keep it 
 * simple. (F2F 28 Feb 2002).</dd>
 * </dl></dd>
 * <dt><code>"namespaces"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[
 * required] (default) Perform the namespace processing as defined in [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>]. </dd>
 * <dt>
 * <code>false</code></dt>
 * <dd>[optional] Do not perform the namespace processing. </dd>
 * </dl></dd>
 * <dt>
 * <code>"namespace-declarations"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] (default)
 * Include namespace declaration attributes, specified or defaulted from the 
 * schema or the DTD, in the document. See also the section Declaring 
 * Namespaces in [<a href='http://www.w3.org/TR/1999/REC-xml-names-19990114/'>XML Namespaces</a>].</dd>
 * <dt><code>false</code></dt>
 * <dd>[required]Discard all namespace 
 * declaration attributes. The Namespace prefixes are retained even if this 
 * feature is set to <code>false</code>.</dd>
 * </dl></dd>
 * <dt><code>"normalize-characters"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[optional]Perform the W3C Text Normalization of the 
 * characters [<a href='http://www.w3.org/TR/2002/WD-charmod-20020430'>CharModel</a>] in the document. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default)Do not 
 * perform character normalization.</dd>
 * </dl></dd>
 * <dt><code>"split-cdata-sections"</code></dt>
 * <dd>
 * <dl>
 * <dt>
 * <code>true</code></dt>
 * <dd>[required] (default)Split CDATA sections containing the 
 * CDATA section termination marker ']]&gt;'. When a CDATA section is split 
 * a warning is issued.</dd>
 * <dt><code>false</code></dt>
 * <dd>[required]Signal an error if a 
 * <code>CDATASection</code> contains an unrepresentable character.</dd>
 * </dl></dd>
 * <dt>
 * <code>"validate"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional] Require the validation 
 * against a schema (i.e. XML schema, DTD, any other type or representation 
 * of schema) of the document as it is being normalized as defined by [<a href='http://www.w3.org/TR/2000/REC-xml-20001006'>XML 1.0</a>]. If 
 * validation errors are found, or no schema was found, the error handler is 
 * notified. Note also that no datatype normalization (i.e. non-XML 1.0 
 * normalization) is done according to the schema used unless the feature 
 * <code>datatype-normalization</code> is <code>true</code>.  
 * <code>validate-if-schema</code> and <code>validate</code> are mutually 
 * exclusive, setting one of them to <code>true</code> will set the other 
 * one to <code>false</code>. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default) Only 
 * XML 1.0 non-validating processing must be done. Note that validation 
 * might still happen if <code>validate-if-schema</code> is <code>true</code>
 * . </dd>
 * </dl></dd>
 * <dt><code>"validate-if-schema"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[optional]Enable 
 * validation only if a declaration for the document element can be found 
 * (independently of where it is found, i.e. XML schema, DTD, or any other 
 * type or representation of schema). If validation errors are found, the 
 * error handler is notified. Note also that no datatype normalization (i.e. 
 * non-XML 1.0 normalization) is done according to the schema used unless 
 * the feature <code>datatype-normalization</code> is <code>true</code>. 
 * <code>validate-if-schema</code> and <code>validate</code> are mutually 
 * exclusive, setting one of them to <code>true</code> will set the other 
 * one to <code>false</code>. </dd>
 * <dt><code>false</code></dt>
 * <dd>[required] (default)No 
 * validation should be performed if the document has a schema. Note that 
 * validation must still happen if <code>validate</code> is <code>true</code>
 * . </dd>
 * </dl></dd>
 * <dt><code>"whitespace-in-element-content"</code></dt>
 * <dd>
 * <dl>
 * <dt><code>true</code></dt>
 * <dd>[required] 
 * (default)Keep all white spaces in the document. How does this feature 
 * interact with <code>"validate"</code> and 
 * <code>Text.isWhitespaceInElementContent</code>. issue no longer relevant 
 * (f2f october 2002).</dd>
 * <dt><code>false</code></dt>
 * <dd>[optional]Discard white space in 
 * element content while normalizing. The implementation is expected to use 
 * the <code>isWhitespaceInElementContent</code> flag on <code>Text</code> 
 * nodes to determine if a text node should be written out or not.</dd>
 * </dl></dd>
 * </dl>
 * <p> The resolutions of entities is done using <code>Document.baseURI</code>
 * . However, when the features "LS-Load" or "LS-Save" defined in [<a href='http://www.w3.org/TR/DOM-Level-3-LS'>DOM Level 3 Load and Save</a>] are 
 * supported by the DOM implementation, the parameter 
 * <code>"entity-resolver"</code> can also be used on 
 * <code>DOMConfiguration</code> objects attached to <code>Document</code> 
 * nodes. If this parameter is set, <code>Document.normalizeDocument</code> 
 * will invoke the entity resolver instead of using 
 * <code>Document.baseURI</code>. 
 * <p>See also the <a href='http://www.w3.org/TR/2002/WD-DOM-Level-3-Core-20021022'>Document Object Model (DOM) Level 3 Core Specification</a>.
 * @since DOM Level 3
 */
public interface DOMConfiguration {
    /**
     * Set the value of a parameter.
     * @param name The name of the parameter to set.
     * @param value  The new value or <code>null</code> if the user wishes to 
     *   unset the parameter. While the type of the value parameter is 
     *   defined as <code>DOMUserData</code>, the object type must match the 
     *   type defined by the definition of the parameter. For example, if 
     *   the parameter is <code>"error-handler"</code>, the value must be of 
     *   type <code>DOMErrorHandler</code>.  Should we allow implementations 
     *   to raise exception if the type does not match? INVALID_ACCESS_ERR 
     *   seems the closest exception code... 
     * @exception DOMException
     *    NOT_SUPPORTED_ERR: Raised when the parameter name is recognized but 
     *   the requested value cannot be set. 
     *   <br> NOT_FOUND_ERR: Raised when the parameter name is not recognized. 
     */
    public void setParameter(String name, 
                             Object value)
                             throws DOMException;

    /**
     *  Return the value of a parameter if known. 
     * @param name  The name of the parameter. 
     * @return  The current object associated with the specified parameter or 
     *   <code>null</code> if no object has been associated or if the 
     *   parameter is not supported.  "by a DOM application" prevents a DOM 
     *   implementation to return its default behavior (such as the default 
     *   "schema-type") if any. 
     * @exception DOMException
     *    NOT_FOUND_ERR: Raised when the parameter name is not recognized. 
     */
    public Object getParameter(String name)
                               throws DOMException;

    /**
     * Check if setting a parameter to a specific value is supported.
     * @param name The name of the parameter to check.
     * @param value  An object. if <code>null</code>, the returned value is 
     *   <code>true</code>. 
     * @return  <code>true</code> if the parameter could be successfully set 
     *   to the specified value, or <code>false</code> if the parameter is 
     *   not recognized or the requested value is not supported. This does 
     *   not change the current value of the parameter itself. 
     */
    public boolean canSetParameter(String name, 
                                   Object value);

}
