/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.parsers;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.apache.xerces.dom3.DOMErrorHandler;
import org.apache.xerces.dom3.ls.DOMBuilder;
import org.apache.xerces.dom3.ls.DOMEntityResolver;
import org.apache.xerces.dom3.ls.DOMBuilderFilter;
import org.apache.xerces.dom3.ls.DOMInputSource;

import org.apache.xerces.util.DOMEntityResolverWrapper;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.DOMErrorHandlerWrapper;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;


/**
 * This is Xerces DOM Builder class. It uses the abstract DOM
 * parser with a document scanner, a dtd scanner, and a validator, as 
 * well as a grammar pool.
 *
 * @author Pavani Mukthipudi, Sun Microsystems Inc.
 * @author Elena Litani, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 */

// REVISIT: 
// 1. "load-as-infoset" should be implemented
// 2. why do we need so many fields in this class (why don't we use AbstractDomParser fields)
// 3. implementation of setFeature method (we are going via switch several times)
// 4. include-comments - I've defined a constant for DOM feature in Constants. 
//    I don't think we need to define corresponding feature for Xerces parser 
//    (with Xerces prefix).
//    We probably should define all DOM Feature constants in Constants.java
//  --el
 
public class DOMBuilderImpl
    extends AbstractDOMParser implements DOMBuilder {
    
    //
    // Constants
    //

    // feature ids

    /** Feature id: namespace declarations. */
    protected static final String NAMESPACE_DECLARATIONS =
        "namespace-declarations";
        
    /** Feature id: validation. */
    protected static final String VALIDATION =
        "validation";
        
    /** Feature id: external parameter entities. */
    protected static final String EXTERNAL_PARAMETER_ENTITIES =
        "external-parameter-entities";
        
    /** Feature id: external general entities. */
    protected static final String EXTERNAL_GENERAL_ENTITIES =
        "external-general-entities";
        
    /** Feature id: external dtd subset. */
    protected static final String EXTERNAL_DTD_SUBSET =
        "external-dtd-subset";
        
    /** Feature id: validate if schema. */
    protected static final String VALIDATE_IF_SCHEMA =
        "validate-if-schema";
        
    /** Feature id: validate against dtd. */
    protected static final String VALIDATE_AGAINST_DTD =
        "validate-against-dtd";  
        
    /** Feature id: datatype normalization. */
    protected static final String DATATYPE_NORMALIZATION =
        "datatype-normalization"; 
        
    /** Feature id: create entity ref nodes. */
    protected static final String CREATE_ENTITY_REFERENCE_NODES =
        "create-entity-ref-nodes"; 
        
    /** Feature id: create entity nodes. */
    protected static final String CREATE_ENTITY_NODES =
        "create-entity-nodes";
           
    /** Feature id: whitespace in element content. */
    protected static final String WHITESPACE_IN_ELEMENT_CONTENT =
        "whitespace-in-element-content"; 
    
    /** Feature id: comments. */
    protected static final String COMMENTS =
        "comments";  
        
    /** Feature id: charset overrides xml encoding. */
    protected static final String CHARSET_OVERRIDES_XML_ENCODING =
        "charset-overrides-xml-encoding";  
        
    /** Feature id: load as infoset. */
    protected static final String LOAD_AS_INFOSET =
        "load-as-infoset";  
        
    /** Feature id: supported mediatypes only. */
    protected static final String SUPPORTED_MEDIATYPES_ONLY =
        "supported-mediatypes-only";
        
    // SAX & Xerces feature ids

    /** Feature id: validation. */
    protected static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX+Constants.VALIDATION_FEATURE;
        
    /** Feature id: external parameter entities. */
    protected static final String EXTERNAL_PARAMETER_ENTITIES_FEATURE =
        Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
        
    /** Feature id: external general entities. */
    protected static final String EXTERNAL_GENERAL_ENTITIES_FEATURE =
        Constants.SAX_FEATURE_PREFIX+Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
        
    /** Feature id: load external dtd. */
    protected static final String LOAD_EXTERNAL_DTD_FEATURE =
        Constants.XERCES_FEATURE_PREFIX+Constants.LOAD_EXTERNAL_DTD_FEATURE;
        
    //
    // Data
    //
    
    // features

    /** Namespace declarations. */
    protected boolean fNamespaceDeclarations;
    
    /** Validate if schema */
    protected boolean fValidateIfSchema;
    
    /** Validate against DTD */
    protected boolean fValidateAgainstDTD;
    
    /** Datatype normalization */
    protected boolean fDatatypeNormalization;
    
    /** Create entity nodes */
    protected boolean fCreateEntityNodes;
        
    /** Whitespace in element content */
    protected boolean fWhitespaceInElementContent;
    
    /** Charset overrides xml encoding */
    protected boolean fCharsetOverridesXmlEncoding;
    
    /** Load as infoset */
    protected boolean fLoadAsInfoset;
    
    /** Supported mediatypes only */
    protected boolean fSupportedMediatypesOnly;
    
    //
    // Constructors
    //

    /**
     * Constructs a DOM Builder using the dtd/xml schema parser configuration.
     */
    public DOMBuilderImpl() {
        this(new DTDXSParserConfiguration());
    } // <init>

    /**
     * Constructs a DOM Builder using the specified parser configuration.
     */
    public DOMBuilderImpl(XMLParserConfiguration config) {
        super(config);
        
        // add recognized features
        final String[] domRecognizedFeatures = {
            NAMESPACE_DECLARATIONS,
            VALIDATE_IF_SCHEMA,
            VALIDATE_AGAINST_DTD,
            DATATYPE_NORMALIZATION,
            CREATE_ENTITY_NODES,
            WHITESPACE_IN_ELEMENT_CONTENT,
            CHARSET_OVERRIDES_XML_ENCODING,
            LOAD_AS_INFOSET,
            SUPPORTED_MEDIATYPES_ONLY
        };
        
        fConfiguration.addRecognizedFeatures(domRecognizedFeatures);

        // set default values
        fConfiguration.setFeature(NAMESPACE_DECLARATIONS, true);
        fConfiguration.setFeature(VALIDATE_IF_SCHEMA, false);
        fConfiguration.setFeature(VALIDATE_AGAINST_DTD, false);
        fConfiguration.setFeature(DATATYPE_NORMALIZATION, false);
        fConfiguration.setFeature(CREATE_ENTITY_NODES, true);
        fConfiguration.setFeature(WHITESPACE_IN_ELEMENT_CONTENT, true);
        fConfiguration.setFeature(CHARSET_OVERRIDES_XML_ENCODING, true);
        fConfiguration.setFeature(LOAD_AS_INFOSET, false);
        fConfiguration.setFeature(SUPPORTED_MEDIATYPES_ONLY, false);
        
    } // <init>(XMLParserConfiguration)
    
    /**
     * Constructs a DOM Builder using the specified symbol table.
     */
    public DOMBuilderImpl(SymbolTable symbolTable) {
        this(new DTDXSParserConfiguration(symbolTable));
    } // <init>(SymbolTable)


    /**
     * Constructs a DOM Builder using the specified symbol table and
     * grammar pool.
     */
    public DOMBuilderImpl(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        this(new DTDXSParserConfiguration(symbolTable, grammarPool));
    }
    
    /**
     * Resets the parser state.
     *
     * @throws SAXException Thrown on initialization error.
     */
    public void reset() {
        super.reset();
        try {
            fNamespaceDeclarations = fConfiguration.getFeature(NAMESPACE_DECLARATIONS);
            fValidateIfSchema = fConfiguration.getFeature(VALIDATE_IF_SCHEMA);
            fValidateAgainstDTD = fConfiguration.getFeature(VALIDATE_AGAINST_DTD);
            fDatatypeNormalization = fConfiguration.getFeature(DATATYPE_NORMALIZATION);
            fWhitespaceInElementContent = fConfiguration.getFeature(WHITESPACE_IN_ELEMENT_CONTENT);
            fCharsetOverridesXmlEncoding = fConfiguration.getFeature(CHARSET_OVERRIDES_XML_ENCODING);
            fLoadAsInfoset = fConfiguration.getFeature(LOAD_AS_INFOSET);
            fSupportedMediatypesOnly = fConfiguration.getFeature(SUPPORTED_MEDIATYPES_ONLY);
        }
        catch (XMLConfigurationException e) {
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, e.getMessage());
            }
            else {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, e.getMessage());
            }
        }

    } // reset() 
    
    //
    // DOMBuilder methods
    //

    /**
     * If a <code>DOMEntityResolver</code> has been specified, each time a 
     * reference to an external entity is encountered the 
     * <code>DOMBuilder</code> will pass the public and system IDs to the 
     * entity resolver, which can then specify the actual source of the 
     * entity.
     */
    public DOMEntityResolver getEntityResolver() {
        DOMEntityResolver domEntityResolver = null;
        try {
            DOMEntityResolver entityResolver = (DOMEntityResolver)fConfiguration.getProperty(ENTITY_RESOLVER);
            if (entityResolver != null && 
                entityResolver instanceof DOMEntityResolverWrapper) {
                domEntityResolver = ((DOMEntityResolverWrapper)entityResolver).getEntityResolver();
            }
        }
        catch (XMLConfigurationException e) {
            
        }
        return domEntityResolver;
    }
    
    /**
     * If a <code>DOMEntityResolver</code> has been specified, each time a 
     * reference to an external entity is encountered the 
     * <code>DOMBuilder</code> will pass the public and system IDs to the 
     * entity resolver, which can then specify the actual source of the 
     * entity.
     */
    public void setEntityResolver(DOMEntityResolver entityResolver) {
        try {
            fConfiguration.setProperty(ENTITY_RESOLVER, 
                                        new DOMEntityResolverWrapper(entityResolver));
        }
        catch (XMLConfigurationException e) {
            
        }
    }

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
    public DOMErrorHandler getErrorHandler() {
        DOMErrorHandler errorHandler = null;
        try {
            DOMErrorHandler domErrorHandler = 
                (DOMErrorHandler)fConfiguration.getProperty(ERROR_HANDLER);
            if (domErrorHandler != null && 
                domErrorHandler instanceof DOMErrorHandlerWrapper) {
                errorHandler = ((DOMErrorHandlerWrapper)domErrorHandler).getErrorHandler();
            }
        }
        catch (XMLConfigurationException e) {
            
        }
        return errorHandler;
    }
    
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
    public void setErrorHandler(DOMErrorHandler errorHandler) {
        try {
            fConfiguration.setProperty(ERROR_HANDLER, 
                                       new DOMErrorHandlerWrapper(errorHandler));
        }
        catch (XMLConfigurationException e) {
            
        }
    }

    /**
     *  When the application provides a filter, the parser will call out to 
     * the filter at the completion of the construction of each 
     * <code>Element</code> node. The filter implementation can choose to 
     * remove the element from the document being constructed (unless the 
     * element is the document element) or to terminate the parse early. If 
     * the document is being validated when it's loaded the validation 
     * happens before the filter is called. 
     */
    public DOMBuilderFilter getFilter() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported");
    }
    
    /**
     *  When the application provides a filter, the parser will call out to 
     * the filter at the completion of the construction of each 
     * <code>Element</code> node. The filter implementation can choose to 
     * remove the element from the document being constructed (unless the 
     * element is the document element) or to terminate the parse early. If 
     * the document is being validated when it's loaded the validation 
     * happens before the filter is called. 
     */
    public void setFilter(DOMBuilderFilter filter) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported");
    }

    /**
     * Set the state of a feature.
     * 
     * <br>The feature name has the same form as a DOM hasFeature string.
     * <br>It is possible for a <code>DOMBuilder</code> to recognize a feature 
     * name but to be unable to set its value.
     * 
     * @param name The feature name.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_SUPPORTED_ERR exception when the <code>DOMBuilder</code> 
     *   recognizes the feature name but cannot set the requested value. 
     *   <br>Raise a NOT_FOUND_ERR When the <code>DOMBuilder</code> does not 
     *   recognize the feature name.
     */
    public void setFeature(String name, boolean state) throws DOMException {
        try {
            if (canSetFeature(name, state)) {
                if (name.equals(VALIDATION)) {
                    fConfiguration.setFeature(VALIDATION_FEATURE, state);
                }
                else if (name.equals(EXTERNAL_PARAMETER_ENTITIES)) {
                    fConfiguration.setFeature(EXTERNAL_PARAMETER_ENTITIES_FEATURE, state);
                }
                else if (name.equals(EXTERNAL_GENERAL_ENTITIES)) {
                    fConfiguration.setFeature(EXTERNAL_GENERAL_ENTITIES_FEATURE, state);
                }
                else if (name.equals(EXTERNAL_DTD_SUBSET)) {
                    fConfiguration.setFeature(LOAD_EXTERNAL_DTD_FEATURE, state);
                }
                else if (name.equals(COMMENTS)) {
                    fConfiguration.setFeature(INCLUDE_COMMENTS_FEATURE, state);
                }
                else if(name.equals(CREATE_ENTITY_REFERENCE_NODES)) {
                    fConfiguration.setFeature(CREATE_ENTITY_REF_NODES, state);
                }
                else {
                    fConfiguration.setFeature(name, state);
                }
            }
            else {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Feature \""+name+"\" cannot be set to \""+state+"\"");
            }
        }
        catch (XMLConfigurationException e) {
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,"Feature \""+name+"\" not recognized");
            }
            else {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Feature \""+name+"\" not supported");
            }
        }
    }

    /**
     * Query whether setting a feature to a specific value is supported.
     * <br>The feature name has the same form as a DOM hasFeature string.
     * 
     * @param name The feature name, which is a DOM has-feature style string.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @return <code>true</code> if the feature could be successfully set to 
     *   the specified value, or <code>false</code> if the feature is not 
     *   recognized or the requested value is not supported. The value of 
     *   the feature itself is not changed.
     */
    public boolean canSetFeature(String name, boolean state) {
        if (name.equals(NAMESPACE_DECLARATIONS) && !state) {
            return false;
        }
        else if(name.equals(VALIDATE_IF_SCHEMA) && state) {
            return false;
        }
        else if(name.equals(VALIDATE_AGAINST_DTD) && state) {
            return false;
        }
        else if(name.equals(CREATE_ENTITY_NODES) && !state) {
            return false;
        }
        else if(name.equals(WHITESPACE_IN_ELEMENT_CONTENT) && !state) {
            return false;
        }
        else if(name.equals(LOAD_AS_INFOSET) && state) {
            return false;
        }
        else if(name.equals(SUPPORTED_MEDIATYPES_ONLY) && state) {
            return false;
        }
        
        return true;
    }

    /**
     * Look up the value of a feature.
     * <br>The feature name has the same form as a DOM hasFeature string
     * 
     * @param name The feature name, which is a string with DOM has-feature 
     *   syntax.
     * @return The current state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_FOUND_ERR When the <code>DOMBuilder</code> does not 
     *   recognize the feature name.
     */
    public boolean getFeature(String name) throws DOMException {
        try {
            if (name.equals(VALIDATION)) {
                return fConfiguration.getFeature(VALIDATION_FEATURE);
            }
            else if (name.equals(EXTERNAL_PARAMETER_ENTITIES)) {
                return fConfiguration.getFeature(EXTERNAL_PARAMETER_ENTITIES_FEATURE);
            }
            else if (name.equals(EXTERNAL_GENERAL_ENTITIES)) {
                return fConfiguration.getFeature(EXTERNAL_GENERAL_ENTITIES_FEATURE);
            }
            else if (name.equals(EXTERNAL_DTD_SUBSET)) {
                return fConfiguration.getFeature(LOAD_EXTERNAL_DTD_FEATURE);
            }
            else if (name.equals(COMMENTS)) {
                return fConfiguration.getFeature(INCLUDE_COMMENTS_FEATURE);
            }
            else if(name.equals(CREATE_ENTITY_REFERENCE_NODES)) {
                return fConfiguration.getFeature(CREATE_ENTITY_REF_NODES);
            }
            else {
                return fConfiguration.getFeature(name);
            }
        }
        catch (XMLConfigurationException e) {
            if (e.getType() == XMLConfigurationException.NOT_RECOGNIZED) {
                throw new DOMException(DOMException.NOT_FOUND_ERR,"Feature \""+name+"\" not recognized");
            }
            else {
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Feature \""+name+"\" not supported");
            }
        }
    }
    
    /**
     * Parse an XML document from a location identified by an URI reference. 
     * If the URI contains a fragment identifier (see section 4.1 in ), the 
     * behavior is not defined by this specification.
     *  
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
    public Document parseURI(String uri) throws Exception {
        XMLInputSource source = new XMLInputSource(null, uri, null);
        try {
            parse(source);
        }

        catch (XNIException e) {
            Exception ex = e.getException();
            throw ex;
        }
                
        // close stream opened by the parser
        finally {
            try {
                Reader reader = source.getCharacterStream();
                if (reader != null) {
                    reader.close();
                }
                else {
                    InputStream is = source.getByteStream();
                    if (is != null) {
                        is.close();
                    }
                }
            }
            catch (IOException e) {
                // ignore
            }
        }
        return getDocument();
    }

    /**
     * Parse an XML document from a resource identified by an 
     * <code>DOMInputSource</code>.
     * 
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
    public Document parse(DOMInputSource is) throws Exception {
        
        try {
            // need to wrap the DOMInputSource with an XMLInputSource
            XMLInputSource xmlInputSource = dom2xmlInputSource(is);
            parse(xmlInputSource);
        }
        catch (XNIException e) {
            Exception ex = e.getException();
            throw ex;
        }
                
        return getDocument();
    }
                          
    /**
     *  Parse an XML document or fragment from a resource identified by an 
     * <code>DOMInputSource</code> and insert the content into an existing 
     * document at the position epcified with the <code>contextNode</code> 
     * and <code>action</code> arguments. When parsing the input stream the 
     * context node is used for resolving unbound namespace prefixes.
     *  
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
    public void parseWithContext(DOMInputSource is, Node cnode, 
                                 short action) throws DOMException {
        // REVISIT: need to implement.
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not supported");
    }
    
    XMLInputSource dom2xmlInputSource(DOMInputSource is) {
        // need to wrap the DOMInputSource with an XMLInputSource
        XMLInputSource xis = null;
        // if there is a string data, use a StringReader
        // according to DOM, we need to treat such data as "UTF-16".
        if (is.getStringData() != null) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                                     is.getBaseURI(), new StringReader(is.getStringData()),
                                     "UTF-16");
        }
        // check whether there is a Reader
        // according to DOM, we need to treat such reader as "UTF-16".
        else if (is.getCharacterStream() != null) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                                     is.getBaseURI(), is.getCharacterStream(),
                                     "UTF-16");
        }
        // check whether there is an InputStream
        else if (is.getByteStream() != null) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                                     is.getBaseURI(), is.getByteStream(),
                                     is.getEncoding());
        }
        // otherwise, just use the public/system/base Ids
        else {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                                     is.getBaseURI());
        }
        
        return xis;
    }
            
} // class DOMBuilderImpl
