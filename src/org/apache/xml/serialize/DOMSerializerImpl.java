/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */



package org.apache.xml.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.xerces.dom.DOMErrorImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.dom3.DOMConfiguration;
import org.apache.xerces.dom3.DOMError;
import org.apache.xerces.dom3.DOMErrorHandler;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMSerializer;
import org.w3c.dom.ls.DOMSerializerFilter;
import org.w3c.dom.ls.DOMOutput;


/**
 * Implemenatation of DOM Level 3 org.w3c.ls.DOMSerializer  by delegating serialization
 * calls to <CODE>XMLSerializer</CODE>.
 * DOMSerializer provides an API for serializing (writing) a DOM document out in an
 * XML document. The XML data is written to an output stream.
 * During serialization of XML data, namespace fixup is done when possible as
 * defined in DOM Level 3 Core, Appendix B.
 *
 * @author Elena Litani, IBM
 * @author Gopal Sharma, Sun Microsystems
 * @author Arun Yadav, Sun Microsystems
 * @version $Id$
 */
public class DOMSerializerImpl implements DOMSerializer, DOMConfiguration {

    // data
    private String fEncoding;

    // serializer
    private XMLSerializer serializer;

    // XML 1.1 serializer
    private XML11Serializer xml11Serializer;

    /**
     * Constructs a new DOMSerializer.
     * The constructor turns on the namespace support in <code>XMLSerializer</code> and
     * initializes the following fields: fNSBinder, fLocalNSBinder, fSymbolTable,
     * fEmptySymbol, fXmlSymbol, fXmlnsSymbol, fNamespaceCounter, fFeatures.
     */
    public DOMSerializerImpl() {
        serializer = new XMLSerializer();
        initSerializer(serializer);
    }



    //
    // DOMSerializer methods
    //

    public DOMConfiguration getConfig(){
        return this;
    }

    /** DOM L3-EXPERIMENTAL:
     * Setter for boolean and object parameters
     */
	public void setParameter(String name, Object value) throws DOMException {
		if (serializer.fFeatures.containsKey(name)) {
			// This is a feature
			if (value instanceof Boolean){
				boolean state = ((Boolean)value).booleanValue();
				if (name.equals(Constants.DOM_XMLDECL)){
					serializer._format.setOmitXMLDeclaration(!state);
					serializer.fFeatures.put(name, value);
				}
				else if (name.equals(Constants.DOM_NAMESPACES)){
					serializer.fNamespaces = state;
					serializer.fFeatures.put(name, value);
				}
				else if (name.equals(Constants.DOM_SPLIT_CDATA)
				|| name.equals(Constants.DOM_DISCARD_DEFAULT_CONTENT)){
					// both values supported
					serializer.fFeatures.put(name, value);
				}
				else if (name.equals(Constants.DOM_CANONICAL_FORM)
					|| name.equals(Constants.DOM_VALIDATE_IF_SCHEMA)
					|| name.equals(Constants.DOM_VALIDATE)
					|| name.equals(Constants.DOM_CHECK_CHAR_NORMALIZATION)
					|| name.equals(Constants.DOM_DATATYPE_NORMALIZATION)
					|| name.equals(Constants.DOM_FORMAT_PRETTY_PRINT)
					|| name.equals(Constants.DOM_NORMALIZE_CHARACTERS)
					// REVISIT: these must be supported
					|| name.equals(Constants.DOM_WELLFORMED)) {
					// true is not supported
					if (state){
						String msg = DOMMessageFormatter.formatMessage(
								DOMMessageFormatter.DOM_DOMAIN,
								"FEATURE_NOT_SUPPORTED",
								new Object[] { name });
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
					}
				}
				else if (name.equals(Constants.DOM_INFOSET)
						|| name.equals(Constants.DOM_NAMESPACE_DECLARATIONS)
						|| name.equals(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT)
						|| name.equals(Constants.DOM_IGNORE_CHAR_DENORMALIZATION)
						// REVISIT: these must be supported
						|| name.equals(Constants.DOM_ENTITIES)
						|| name.equals(Constants.DOM_CDATA_SECTIONS)
						|| name.equals(Constants.DOM_COMMENTS)) {
					// false is not supported
					if (!state){
						String msg = DOMMessageFormatter.formatMessage(
								DOMMessageFormatter.DOM_DOMAIN,
								"FEATURE_NOT_SUPPORTED",
								new Object[] { name });
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
					}
				}
				else {
					String msg = DOMMessageFormatter.formatMessage(
							DOMMessageFormatter.DOM_DOMAIN,
							"FEATURE_NOT_FOUND",
							new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
				}
			}
			else {

			 // REVISIT: modify error exception to TYPE_MISMATCH
 			String msg = DOMMessageFormatter.formatMessage(
			 DOMMessageFormatter.DOM_DOMAIN,
			 "FEATURE_NOT_SUPPORTED",
			 new Object[] { name });
			throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
		}
	}
	else if (name.equals(Constants.DOM_ERROR_HANDLER)) {
			if (value instanceof DOMErrorHandler) {
				serializer.fDOMErrorHandler = (DOMErrorHandler) value;
			}
			else {
				// REVISIT: modify error exception to TYPE_MISMATCH
                String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
               throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
			}
		}
		else if (name.equals(Constants.DOM_ENTITY_RESOLVER)
				|| name.equals(Constants.DOM_SCHEMA_LOCATION)
				|| name.equals(Constants.DOM_SCHEMA_TYPE)) {
                String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
               throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
		}
		else {
                String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_FOUND",
                            new Object[] { name });
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
        }
	}

    /** DOM L3-EXPERIMENTAL:
     * Check if parameter can be set
     */
	public boolean canSetParameter(String name, Object state) {
		if (state instanceof Boolean){
			boolean value = ((Boolean)state).booleanValue();
			if (name.equals(Constants.DOM_NAMESPACES)
			|| name.equals(Constants.DOM_SPLIT_CDATA)
			|| name.equals(Constants.DOM_DISCARD_DEFAULT_CONTENT)
			|| name.equals(Constants.DOM_XMLDECL)){
	            // both values supported
				return true;
			}
			else if (name.equals(Constants.DOM_CANONICAL_FORM)
			    || name.equals(Constants.DOM_VALIDATE_IF_SCHEMA)
			    || name.equals(Constants.DOM_VALIDATE)
			    || name.equals(Constants.DOM_CHECK_CHAR_NORMALIZATION)
			    || name.equals(Constants.DOM_DATATYPE_NORMALIZATION)
			    || name.equals(Constants.DOM_FORMAT_PRETTY_PRINT)
			    || name.equals(Constants.DOM_NORMALIZE_CHARACTERS)
			    // REVISIT: these must be supported
			    || name.equals(Constants.DOM_WELLFORMED)) {
				// true is not supported
				return !value;
			}
			else if (name.equals(Constants.DOM_INFOSET)
					|| name.equals(Constants.DOM_NAMESPACE_DECLARATIONS)
			        || name.equals(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT)
			        || name.equals(Constants.DOM_IGNORE_CHAR_DENORMALIZATION)
			        // REVISIT: these must be supported
			        || name.equals(Constants.DOM_ENTITIES)
					|| name.equals(Constants.DOM_CDATA_SECTIONS)
					|| name.equals(Constants.DOM_COMMENTS)) {
				// false is not supported
				return value;
			        }
		}
		else if (name.equals(Constants.DOM_ERROR_HANDLER)){
			return true;
		}
	    return false;
    }

    /** DOM L3-EXPERIMENTAL:
     * Getter for boolean and object parameters
     */
	public Object getParameter(String name) throws DOMException {
		Object state = serializer.fFeatures.get(name);
		if (state == null) {
			if (name.equals(Constants.DOM_ERROR_HANDLER)) {
				return serializer.fDOMErrorHandler;
			}
			else if (name.equals(Constants.DOM_ENTITY_RESOLVER)
					|| name.equals(Constants.DOM_SCHEMA_LOCATION)
					|| name.equals(Constants.DOM_SCHEMA_TYPE)) {
				String msg =
					DOMMessageFormatter.formatMessage(
						DOMMessageFormatter.DOM_DOMAIN,
						"FEATURE_NOT_SUPPORTED",
						new Object[] { name });
				throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
			}
			else {
				String msg =
					DOMMessageFormatter.formatMessage(
						DOMMessageFormatter.DOM_DOMAIN,
						"FEATURE_NOT_FOUND",
						new Object[] { name });
				throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
			}
		}

		return ((Boolean) state);
	}

    /**
     * DOM L3 EXPERIMENTAL:
     *  The character encoding in which the output will be written.
     * <br> The encoding to use when writing is determined as follows: If the
     * encoding attribute has been set, that value will be used.If the
     * encoding attribute is <code>null</code> or empty, but the item to be
     * written includes an encoding declaration, that value will be used.If
     * neither of the above provides an encoding name, a default encoding of
     * "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public String getEncoding() {
        return fEncoding;
    }

    /**
     * DOM L3 EXPERIMENTAL:
     *  The character encoding in which the output will be written.
     * <br> The encoding to use when writing is determined as follows: If the
     * encoding attribute has been set, that value will be used.If the
     * encoding attribute is <code>null</code> or empty, but the item to be
     * written includes an encoding declaration, that value will be used.If
     * neither of the above provides an encoding name, a default encoding of
     * "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public void setEncoding(String encoding) {
        serializer._format.setEncoding(encoding);
        fEncoding = serializer._format.getEncoding();
    }

    /**
     * DOM L3 EXPERIMENTAL:
     * Write out the specified node as described above in the description of
     * <code>DOMSerializer</code>. Writing a Document or Entity node produces a
     * serialized form that is well formed XML. Writing other node types
     * produces a fragment of text in a form that is not fully defined by
     * this document, but that should be useful to a human for debugging or
     * diagnostic purposes.
     * @param destination The destination for the data to be written.
     * @param wnode The <code>Document</code> or <code>Entity</code> node to
     *   be written. For other node types, something sensible should be
     *   written, but the exact serialized form is not specified.
     * @return  Returns <code>true</code> if <code>node</code> was
     *   successfully serialized and <code>false</code> in case a failure
     *   occured and the failure wasn't canceled by the error handler.
     * @exception none
     */
    public boolean writeNode(java.io.OutputStream destination,
                             Node wnode) {
        // determine which serializer to use:
        Document doc = (wnode.getNodeType()== Node.DOCUMENT_NODE)?(Document)wnode:wnode.getOwnerDocument();
        Method getVersion = null;
        XMLSerializer ser = null;
        String ver = null;
        // this should run under JDK 1.1.8...
        try {
            getVersion = doc.getClass().getMethod("getVersion", new Class[]{});
            if(getVersion != null ) {
                ver = (String)getVersion.invoke(doc, null);
            }
        } catch (Exception e) {
            // no way to test the version...
            // ignore the exception
        }
        if(ver != null && ver.equals("1.1")) {
            if(xml11Serializer == null) {
                xml11Serializer = new XML11Serializer();
                initSerializer(xml11Serializer);
            }
            // copy setting from "main" serializer to XML 1.1 serializer
            copySettings(serializer, xml11Serializer);
            ser = xml11Serializer;
        } else {
            ser = serializer;
        }
        checkAllFeatures(ser);
        try {
            ser.reset();
            ser.setOutputByteStream(destination);
            if (wnode == null)
                return false;
            else if (wnode.getNodeType() == Node.DOCUMENT_NODE)
                ser.serialize((Document)wnode);
            else if (wnode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
                ser.serialize((DocumentFragment)wnode);
            else if (wnode.getNodeType() == Node.ELEMENT_NODE)
                ser.serialize((Element)wnode);
            else
                return false;
        } catch (Exception e) {
            if (ser.fDOMErrorHandler != null) {
                  DOMErrorImpl error = new DOMErrorImpl();
                  error.fException = e;
                  error.fMessage = e.getMessage();
                  error.fSeverity = DOMError.SEVERITY_ERROR;
                  ser.fDOMErrorHandler.handleError(error);

            }
        }
        return true;
    }

    /**
     * DOM L3 EXPERIMENTAL:
     *  Serialize the specified node as described above in the description of
     * <code>DOMSerializer</code>. The result of serializing the node is
     * returned as a string. Writing a Document or Entity node produces a
     * serialized form that is well formed XML. Writing other node types
     * produces a fragment of text in a form that is not fully defined by
     * this document, but that should be useful to a human for debugging or
     * diagnostic purposes.
     * @param wnode  The node to be written.
     * @return  Returns the serialized data, or <code>null</code> in case a
     *   failure occured and the failure wasn't canceled by the error
     *   handler.
     * @exception DOMException
     *    DOMSTRING_SIZE_ERR: The resulting string is too long to fit in a
     *   <code>DOMString</code>.
     */
    public String writeToString(Node wnode)
    throws DOMException {
        // determine which serializer to use:
        Document doc = (wnode.getNodeType() == Node.DOCUMENT_NODE)?(Document)wnode:wnode.getOwnerDocument();
        Method getVersion = null;
        XMLSerializer ser = null;
        String ver = null;
        // this should run under JDK 1.1.8...
        try {
            getVersion = doc.getClass().getMethod("getVersion", new Class[]{});
            if(getVersion != null ) {
                ver = (String)getVersion.invoke(doc, null);
            }
        } catch (Exception e) {
            // no way to test the version...
            // ignore the exception
        }
        if(ver != null && ver.equals("1.1")) {
            if(xml11Serializer == null) {
                xml11Serializer = new XML11Serializer();
                initSerializer(xml11Serializer);
            }
            // copy setting from "main" serializer to XML 1.1 serializer
            copySettings(serializer, xml11Serializer);
            ser = xml11Serializer;
        } else {
            ser = serializer;
        }
        checkAllFeatures(ser);
        StringWriter destination = new StringWriter();
        try {
            ser.reset();
            ser.setOutputCharStream(destination);
            if (wnode == null)
                return null;
            else if (wnode.getNodeType() == Node.DOCUMENT_NODE)
                ser.serialize((Document)wnode);
            else if (wnode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
                ser.serialize((DocumentFragment)wnode);
            else if (wnode.getNodeType() == Node.ELEMENT_NODE)
                ser.serialize((Element)wnode);
            else
                return null;
        } catch (IOException ioe) {
	        String msg = DOMMessageFormatter.formatMessage(
			    DOMMessageFormatter.DOM_DOMAIN,
				"STRING_TOO_LONG",
				new Object[] { ioe.getMessage()});
            throw new DOMException(DOMException.DOMSTRING_SIZE_ERR,msg);
        }
        return destination.toString();
    }

    /**
     * DOM L3 EXPERIMENTAL:
     * The end-of-line sequence of characters to be used in the XML being
     * written out. The only permitted values are these:
     * <dl>
     * <dt><code>null</code></dt>
     * <dd>
     * Use a default end-of-line sequence. DOM implementations should choose
     * the default to match the usual convention for text files in the
     * environment being used. Implementations must choose a default
     * sequence that matches one of those allowed by  2.11 "End-of-Line
     * Handling". </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The
     * carriage-return and line-feed characters (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed
     * character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public void setNewLine(String newLine) {
        serializer._format.setLineSeparator(newLine);
    }


    /**
     * DOM L3 EXPERIMENTAL:
     * The end-of-line sequence of characters to be used in the XML being
     * written out. The only permitted values are these:
     * <dl>
     * <dt><code>null</code></dt>
     * <dd>
     * Use a default end-of-line sequence. DOM implementations should choose
     * the default to match the usual convention for text files in the
     * environment being used. Implementations must choose a default
     * sequence that matches one of those allowed by  2.11 "End-of-Line
     * Handling". </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The
     * carriage-return and line-feed characters (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed
     * character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public String getNewLine() {
        return serializer._format.getLineSeparator();
    }


    /**
     *  When the application provides a filter, the serializer will call out
     * to the filter before serializing each Node. Attribute nodes are never
     * passed to the filter. The filter implementation can choose to remove
     * the node from the stream or to terminate the serialization early.
     */
    public DOMSerializerFilter getFilter(){
        return null;
    }
    /**
     *  When the application provides a filter, the serializer will call out
     * to the filter before serializing each Node. Attribute nodes are never
     * passed to the filter. The filter implementation can choose to remove
     * the node from the stream or to terminate the serialization early.
     */
    public void setFilter(DOMSerializerFilter filter){
        serializer.fDOMFilter = filter;
    }


    private void checkAllFeatures(XMLSerializer ser) {
        if (getParameter(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT) == Boolean.TRUE)
            ser._format.setPreserveSpace(true);
        else
            ser._format.setPreserveSpace(false);
    }

    // this initializes a newly-created serializer
    private void initSerializer(XMLSerializer ser) {
        ser.fNamespaces = true;
        ser.fNSBinder = new NamespaceSupport();
        ser.fLocalNSBinder = new NamespaceSupport();
        ser.fSymbolTable = new SymbolTable();
        ser.fFeatures = new Hashtable();
        ser.fFeatures.put(Constants.DOM_NAMESPACES, Boolean.TRUE);
        ser.fFeatures.put(Constants.DOM_NORMALIZE_CHARACTERS, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_VALIDATE_IF_SCHEMA, Boolean.FALSE);
        ser.fFeatures.put(Constants.DOM_VALIDATE, Boolean.FALSE);
        ser.fFeatures.put(Constants.DOM_ENTITIES, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_SPLIT_CDATA, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_CDATA_SECTIONS, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_COMMENTS, Boolean.TRUE);
        ser.fFeatures.put(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT, Boolean.TRUE);
        ser.fFeatures.put(Constants.DOM_DISCARD_DEFAULT_CONTENT, Boolean.TRUE);
        ser.fFeatures.put(Constants.DOM_CANONICAL_FORM, Boolean.FALSE);
        ser.fFeatures.put(Constants.DOM_FORMAT_PRETTY_PRINT, Boolean.FALSE);
        ser.fFeatures.put(Constants.DOM_XMLDECL, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_CHECK_CHAR_NORMALIZATION, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_DATATYPE_NORMALIZATION, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_NORMALIZE_CHARACTERS, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_WELLFORMED, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_INFOSET, Boolean.FALSE);
		ser.fFeatures.put(Constants.DOM_NAMESPACE_DECLARATIONS, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT, Boolean.TRUE);
		ser.fFeatures.put(Constants.DOM_IGNORE_CHAR_DENORMALIZATION, Boolean.TRUE);
    }

    // copies all settings that could have been modified
    // by calls to DOMSerializer methods from one serializer to another.
    // IMPORTANT:  if new methods are implemented or more settings of
    // the serializer are made alterable, this must be
    // reflected in this method!
    private void copySettings(XMLSerializer src, XMLSerializer dest) {
        dest._format.setOmitXMLDeclaration(src._format.getOmitXMLDeclaration());
        dest.fNamespaces = src.fNamespaces;
        dest.fDOMErrorHandler = src.fDOMErrorHandler;
        dest._format.setEncoding(src._format.getEncoding());
        dest._format.setLineSeparator(src._format.getLineSeparator());
        dest.fDOMFilter = src.fDOMFilter;
        // and copy over all the entries in fFeatures:
        Enumeration keys = src.fFeatures.keys();
        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object val = src.fFeatures.get(key);
            dest.fFeatures.put(key,val);
        }
    }//copysettings

    /**
      *  Serialize the specified node as described above in the general
      * description of the <code>DOMSerializer</code> interface. The output
      * is written to the supplied <code>DOMOutput</code>.
      * <br> When writing to a <code>DOMOutput</code>, the encoding is found by
      * looking at the encoding information that is reachable through the
      * <code>DOMOutput</code> and the item to be written (or its owner
      * document) in this order:
      * <ol>
      * <li> <code>DOMOutput.encoding</code>,
      * </li>
      * <li>
      * <code>Document.actualEncoding</code>,
      * </li>
      * <li>
      * <code>Document.xmlEncoding</code>.
      * </li>
      * </ol>
      * <br> If no encoding is reachable through the above properties, a
      * default encoding of "UTF-8" will be used.
      * <br> If the specified encoding is not supported an
      * "unsupported-encoding" error is raised.
      * <br> If no output is specified in the <code>DOMOutput</code>, a
      * "no-output-specified" error is raised.
      * @param node  The node to serialize.
      * @param destination The destination for the serialized DOM.
      * @return  Returns <code>true</code> if <code>node</code> was
      *   successfully serialized and <code>false</code> in case the node
      *   couldn't be serialized.
      */
    public boolean write(Node node,
                         DOMOutput destination){
      // Commented for time being 'coz uding DOM L3 Core fn
      //Method getVersion = null;
      //XMLSerializer ser = null;
      //String ver = null;
      //OutputStream fOutputStream = null;
      //String fEncoding = null;

      //Document fDocument = (node.getNodeType()== Node.DOCUMENT_NODE)?(Document)node:node.getOwnerDocument();
      // this should run under JDK 1.1.8...
      //try {
      //getVersion = fDocument.getClass().getMethod("getVersion", new Class[]{});
      //if(getVersion != null ) {
      //    ver = (String)getVersion.invoke(fDocument, null);
      //}
      //} catch (Exception e) {
        // no way to test the version...
        // ignore the exception
      //}
      // determine which serializer to use:
      //if(ver != null && ver.equals("1.1")) {
       // if(xml11Serializer == null) {
        //  xml11Serializer = new XML11Serializer();
         // initSerializer(xml11Serializer);
        //}
       // copy setting from "main" serializer to XML 1.1 serializer
      //copySettings(serializer, xml11Serializer);
      //ser = xml11Serializer;
      //} else {
      //ser = serializer;
      //}
      //checkAllFeatures(ser);

      //Writer fWriter = destination.getCharacterStream();

      //if(fWriter==null){
      //fOutputStream = destination.getByteStream();
      //if( fOutputStream ==null){
       //   if (ser.fDOMErrorHandler != null) {
       //     DOMErrorImpl error = new DOMErrorImpl();
        //    error.fMessage = "no-output-specified";
        //    error.fSeverity = DOMError.SEVERITY_FATAL_ERROR;
        //    ser.fDOMErrorHandler.handleError(error);
        //   }
       //   return false;
     // }
      //if( (fEncoding = destination.getEncoding())== null){
      //    fDocument = node.getOwnerDocument();
      //    if( (fEncoding = fDocument.getActualEncoding()) == null)
      //        if((fEncoding = fDocument.getXmlEncoding()) == null)
      //           fEncoding = "UTF-8";
     // }
     // }
     // try {
     //   ser.reset();
      //  if(fWriter!=null)
      //    ser.setOutputCharStream(fWriter);
      //  else if ( fOutputStream !=null){
      //    serializer._format.setEncoding(fEncoding);
      //    ser.setOutputByteStream(fOutputStream);
      //  }
      //  if (node == null)
      //    return false;
      //  else if (node.getNodeType() == Node.DOCUMENT_NODE)
      //    ser.serialize((Document)node);
      //  else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
      //    ser.serialize((DocumentFragment)node);
      //  else if (node.getNodeType() == Node.ELEMENT_NODE)
      //    ser.serialize((Element)node);
      //  else
      //    return false;
      //} catch (Exception e) {
      //if (ser.fDOMErrorHandler != null) {
      //      DOMErrorImpl error = new DOMErrorImpl();
      //      error.fException = e;
      //      error.fMessage = e.getMessage();
      //      error.fSeverity = DOMError.SEVERITY_ERROR;
      //      ser.fDOMErrorHandler.handleError(error);

      //}
      //}
        return false;
    } //write

    /**
      *  Serialize the specified node as described above in the general
      * description of the <code>DOMSerializer</code> interface. The output
      * is written to the supplied URI.
      * <br> When writing to a URI, the encoding is found by looking at the
      * encoding information that is reachable through the item to be written
      * (or its owner document) in this order:
      * <ol>
      * <li>
      * <code>Document.actualEncoding</code>,
      * </li>
      * <li>
      * <code>Document.xmlEncoding</code>.
      * </li>
      * </ol>
      * <br> If no encoding is reachable through the above properties, a
      * default encoding of "UTF-8" will be used.
      * <br> If the specified encoding is not supported an
      * "unsupported-encoding" error is raised.
      * @param node  The node to serialize.
      * @param URI The URI to write to.
      * @return  Returns <code>true</code> if <code>node</code> was
      *   successfully serialized and <code>false</code> in case the node
      *   couldn't be serialized.
      */
    public boolean writeURI(Node node,
                            String URI){
      // determine which serializer to use:
      // commented using DOM L3 Core fn
      // Method getVersion = null;
      // XMLSerializer ser = null;
      // String ver = null;
      // String fEncoding = null;

      // Document fDocument = (node.getNodeType()== Node.DOCUMENT_NODE)?(Document)node:node.getOwnerDocument();
      // this should run under JDK 1.1.8...
      // try {
      //  getVersion = fDocument.getClass().getMethod("getVersion", new Class[]{});
      //  if(getVersion != null ) {
      //    ver = (String)getVersion.invoke(fDocument, null);
      //  }
      //} catch (Exception e) {
        // no way to test the version...
        // ignore the exception
      //}
     // if(ver != null && ver.equals("1.1")) {
       // if(xml11Serializer == null) {
       //   xml11Serializer = new XML11Serializer();
       //   initSerializer(xml11Serializer);
       // }
        // copy setting from "main" serializer to XML 1.1 serializer
      //  copySettings(serializer, xml11Serializer);
      //  ser = xml11Serializer;
      //} else {
      //  ser = serializer;
      //}
     // checkAllFeatures(ser);
     // if((fEncoding = fDocument.getActualEncoding())==null)
     //   if((fEncoding = fDocument.getXmlEncoding())==null)
     //     fEncoding = "UTF-8";

     // try {
      //  ser.reset();
      //  FileOutputStream fileOut = new FileOutputStream(new File(URI));
      //  OutputStreamWriter fWriter = new OutputStreamWriter(fileOut, fEncoding);
      //  if(fWriter!=null)
      //    ser.setOutputCharStream(fWriter);
      //  else
      //    return false;

     //   if (node == null)
     //     return false;
     //   else if (node.getNodeType() == Node.DOCUMENT_NODE)
     //     ser.serialize((Document)node);
     //   else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
     //     ser.serialize((DocumentFragment)node);
     //   else if (node.getNodeType() == Node.ELEMENT_NODE)
     //     ser.serialize((Element)node);
     //   else
     //     return false;
     // } catch (Exception e) {
     //   if (ser.fDOMErrorHandler != null) {
     //     DOMErrorImpl error = new DOMErrorImpl();
     //     error.fException = e;
     //     error.fMessage = e.getMessage();
     //     error.fSeverity = DOMError.SEVERITY_ERROR;
     //     ser.fDOMErrorHandler.handleError(error);

     //   }
     //  }
             return false;
    } //writeURI

}//DOMSerializerImpl




