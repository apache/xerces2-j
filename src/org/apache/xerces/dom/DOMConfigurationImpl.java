/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  
 * All rights reserved.
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.dom;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.apache.xerces.dom3.DOMConfiguration;
import org.apache.xerces.dom3.DOMErrorHandler;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.DOMResourceResolverWrapper;
import org.apache.xerces.util.DOMErrorHandlerWrapper;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.util.ObjectFactory;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.ls.DOMResourceResolver;



/**
 * Xerces implementation of DOMConfiguration that maintains a table of recognized parameters.
 *
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class DOMConfigurationImpl extends ParserConfigurationSettings
    implements XMLParserConfiguration, DOMConfiguration {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: validation. */
    protected static final String XERCES_VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
    /** Feature identifier: namespaces. */
    protected static final String XERCES_NAMESPACES =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    protected static final String SCHEMA = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    
    protected static final String DYNAMIC_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;

    protected static final String NORMALIZE_DATA = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
        
    /** sending psvi in the pipeline */
    protected static final String SEND_PSVI = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;

    
    // property identifiers

    /** Property identifier: entity manager. */
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: xml string. */
    protected static final String XML_STRING = 
        Constants.SAX_PROPERTY_PREFIX + Constants.XML_STRING_PROPERTY;

    /** Property identifier: symbol table. */
    protected static final String SYMBOL_TABLE = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property id: Grammar pool*/
    protected static final String GRAMMAR_POOL = 
    Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Property identifier: entity resolver. */
    protected static final String ENTITY_RESOLVER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    
    /** Property identifier: JAXP schema language / DOM schema-type. */
    protected static final String JAXP_SCHEMA_LANGUAGE =
    Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE;

    /** Property identifier: JAXP schema source/ DOM schema-location. */
    protected static final String JAXP_SCHEMA_SOURCE =
    Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE;

    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    //
    // Data
    //
    XMLDocumentHandler fDocumentHandler;
    
    /** Normalization features*/
    protected short features = 0;
    
    protected final static short NAMESPACES          = 0x1<<0;
    protected final static short DTNORMALIZATION     = 0x1<<1;
    protected final static short ENTITIES            = 0x1<<2;
    protected final static short CDATA               = 0x1<<3;
    protected final static short SPLITCDATA          = 0x1<<4;
    protected final static short COMMENTS            = 0x1<<5;
    protected final static short VALIDATE            = 0x1<<6;
    protected final static short PSVI                = 0x1<<7;

    // components

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    /** Components. */
    protected Vector fComponents;

    protected ValidationManager fValidationManager;

    /** Locale. */
    protected Locale fLocale;
    
    /** Error reporter */
    protected XMLErrorReporter fErrorReporter;
    
    protected final DOMErrorHandlerWrapper fErrorHandlerWrapper = 
                new DOMErrorHandlerWrapper();


    //
    // Constructors
    //

    /** Default Constructor. */
    protected DOMConfigurationImpl() {
        this(null, null);
    } // <init>()

    /** 
     * Constructs a parser configuration using the specified symbol table. 
     *
     * @param symbolTable The symbol table to use.
     */
    protected DOMConfigurationImpl(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /** 
     * Constructs a parser configuration using the specified symbol table
     * and parent settings.
     *
     * @param symbolTable    The symbol table to use.
     * @param parentSettings The parent settings.
     */
    protected DOMConfigurationImpl(SymbolTable symbolTable,
                                    XMLComponentManager parentSettings) {
        super(parentSettings);

        // create storage for recognized features and properties
        fRecognizedFeatures = new Vector();
        fRecognizedProperties = new Vector();

        // create table for features and properties
        fFeatures = new Hashtable();
        fProperties = new Hashtable();

        // add default recognized features
        final String[] recognizedFeatures = {
            XERCES_VALIDATION,                 
            XERCES_NAMESPACES,
            SCHEMA,
            DYNAMIC_VALIDATION,
            NORMALIZE_DATA,
            SEND_PSVI,
        };
        addRecognizedFeatures(recognizedFeatures);

        // set state for default features
        setFeature(XERCES_VALIDATION, false);
        setFeature(SCHEMA, false);
        setFeature(DYNAMIC_VALIDATION, false);
        setFeature(NORMALIZE_DATA, false);
        setFeature(XERCES_NAMESPACES, true);
        setFeature(SEND_PSVI, true);

        // add default recognized properties
        final String[] recognizedProperties = {
            XML_STRING,     
            SYMBOL_TABLE,
            ERROR_HANDLER,  
            ENTITY_RESOLVER,
            ERROR_REPORTER,
            ENTITY_MANAGER,
            VALIDATION_MANAGER,
            GRAMMAR_POOL,
            JAXP_SCHEMA_SOURCE,
            JAXP_SCHEMA_LANGUAGE
        };
        addRecognizedProperties(recognizedProperties);

        // set default values for normalization features        
        features |= NAMESPACES;
        features |= ENTITIES;
        features |= COMMENTS;
        features |= CDATA;
        features |= SPLITCDATA;

        if (symbolTable == null) {
            symbolTable = new SymbolTable();
        }
        fSymbolTable = symbolTable;

        fComponents = new Vector();

        setProperty(SYMBOL_TABLE, fSymbolTable);
        fErrorReporter = new XMLErrorReporter();
        setProperty(ERROR_REPORTER, fErrorReporter);
        addComponent(fErrorReporter);

        XMLEntityManager manager =  new XMLEntityManager();
        setProperty(ENTITY_MANAGER, manager);
        addComponent(manager);
        
        fValidationManager = createValidationManager();
        setProperty(VALIDATION_MANAGER, fValidationManager);


        // add message formatters
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }

        // REVISIT: try to include XML Schema formatter.
        //          This is a hack to allow DTD configuration to be build.
        //          
        if (fErrorReporter.getMessageFormatter("http://www.w3.org/TR/xml-schema-1") == null) {
            MessageFormatter xmft = null;
            try {            
               xmft = (MessageFormatter)(
                    ObjectFactory.newInstance("org.apache.xerces.impl.xs.XSMessageFormatter", 
                    ObjectFactory.findClassLoader(), true));
            } catch (Exception exception){
            }

             if (xmft !=  null) {  
                 fErrorReporter.putMessageFormatter("http://www.w3.org/TR/xml-schema-1", xmft);
             }
        }


        // set locale
        try {
            setLocale(Locale.getDefault());
        }
        catch (XNIException e) {
            // do nothing
            // REVISIT: What is the right thing to do? -Ac
        }


    } // <init>(SymbolTable)


    //
    // XMLParserConfiguration methods
    //

    /**
     * Parse an XML document.
     * <p>
     * The parser can use this method to instruct this configuration
     * to begin parsing an XML document from any valid input source
     * (a character stream, a byte stream, or a URI).
     * <p>
     * Parsers may not invoke this method while a parse is in progress.
     * Once a parse is complete, the parser may then parse another XML
     * document.
     * <p>
     * This method is synchronous: it will not return until parsing
     * has ended.  If a client application wants to terminate 
     * parsing early, it should throw an exception.
     *
     * @param source The input source for the top-level of the
     *               XML document.
     *
     * @exception XNIException Any XNI exception, possibly wrapping 
     *                         another exception.
     * @exception IOException  An IO exception from the parser, possibly
     *                         from a byte stream or character stream
     *                         supplied by the parser.
     */
    public void parse(XMLInputSource inputSource) 
        throws XNIException, IOException{
        // no-op
    }

    /**
     * Sets the document handler on the last component in the pipeline
     * to receive information about the document.
     * 
     * @param documentHandler   The document handler.
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    /** Returns the registered document handler. */
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    } // getDocumentHandler():XMLDocumentHandler

    /**
     * Sets the DTD handler.
     * 
     * @param dtdHandler The DTD handler.
     */
    public void setDTDHandler(XMLDTDHandler dtdHandler) {
        //no-op
    } // setDTDHandler(XMLDTDHandler)

    /** Returns the registered DTD handler. */
    public XMLDTDHandler getDTDHandler() {
        return null;
    } // getDTDHandler():XMLDTDHandler

    /**
     * Sets the DTD content model handler.
     * 
     * @param handler The DTD content model handler.
     */
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        //no-op

    } // setDTDContentModelHandler(XMLDTDContentModelHandler)

    /** Returns the registered DTD content model handler. */
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return null;
    } // getDTDContentModelHandler():XMLDTDContentModelHandler

    /**
     * Sets the resolver used to resolve external entities. The EntityResolver
     * interface supports resolution of public and system identifiers.
     *
     * @param resolver The new entity resolver. Passing a null value will
     *                 uninstall the currently installed resolver.
     */
    public void setEntityResolver(XMLEntityResolver resolver) {
        if (resolver !=null) {
            fProperties.put(ENTITY_RESOLVER, resolver);
        }
    } // setEntityResolver(XMLEntityResolver)

    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none
     *         has been registered.
     * @see #setEntityResolver
     */
    public XMLEntityResolver getEntityResolver() {
        return (XMLEntityResolver)fProperties.get(ENTITY_RESOLVER);
    } // getEntityResolver():XMLEntityResolver

    /**
     * Allow an application to register an error event handler.
     *
     * <p>If the application does not register an error handler, all
     * error events reported by the SAX parser will be silently
     * ignored; however, normal processing may not continue.  It is
     * highly recommended that all SAX applications implement an
     * error handler to avoid unexpected bugs.</p>
     *
     * <p>Applications may register a new or different handler in the
     * middle of a parse, and the SAX parser must begin using the new
     * handler immediately.</p>
     *
     * @param errorHandler The error handler.
     * @exception java.lang.NullPointerException If the handler 
     *            argument is null.
     * @see #getErrorHandler
     */
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        if (errorHandler != null) {        
            fProperties.put(ERROR_HANDLER, errorHandler);
        }
    } // setErrorHandler(XMLErrorHandler)

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public XMLErrorHandler getErrorHandler() {
        // REVISIT: Should this be a property?
        return (XMLErrorHandler)fProperties.get(ERROR_HANDLER);
    } // getErrorHandler():XMLErrorHandler

    /**
     * Set the state of a feature.
     *
     * Set the state of any feature in a SAX2 parser.  The parser
     * might not recognize the feature, and if it does recognize
     * it, it might not be able to fulfill the request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state The requested state of the feature (true or false).
     *
     * @exception org.apache.xerces.xni.parser.XMLConfigurationException If the
     *            requested feature is not known.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {

        // save state if noone "objects"
        super.setFeature(featureId, state);

    } // setFeature(String,boolean)

    /**
     * setProperty
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {

        // store value if noone "objects"
        super.setProperty(propertyId, value);

    } // setProperty(String,Object)

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) throws XNIException {
        fLocale = locale;
        fErrorReporter.setLocale(locale);

    } // setLocale(Locale)

    /** Returns the locale. */
    public Locale getLocale() {
        return fLocale;
    } // getLocale():Locale

    /**
     * DOM Level 3 WD - Experimental.
     * setParameter
     */
    public void setParameter(String name, Object value) throws DOMException {

        // REVISIT: Recognizes DOM L3 default features only.
        //          Does not yet recognize Xerces features.
		if(value instanceof Boolean){
	   		boolean state = ((Boolean)value).booleanValue();

            if (name.equals(Constants.DOM_COMMENTS)) {
                features = (short) (state ? features | COMMENTS : features & ~COMMENTS);

            }
            else if (name.equals(Constants.DOM_DATATYPE_NORMALIZATION)) {
                setFeature(NORMALIZE_DATA, state);
                features =
                    (short) (state ? features | DTNORMALIZATION : features & ~DTNORMALIZATION);

            }
            else if (name.equals(Constants.DOM_NAMESPACES)) {
                features = (short) (state ? features | NAMESPACES : features & ~NAMESPACES);
            }
            else if (name.equals(Constants.DOM_CDATA_SECTIONS)) {
                features = (short) (state ? features | CDATA : features & ~CDATA);
            }
            else if (name.equals(Constants.DOM_ENTITIES)) {
                features = (short) (state ? features | ENTITIES : features & ~ENTITIES);

            }
            else if (name.equals(Constants.DOM_SPLIT_CDATA)) {
                features = (short) (state ? features | SPLITCDATA : features & ~SPLITCDATA);

            }
            else if (name.equals(Constants.DOM_VALIDATE)) {
                features = (short) (state ? features | VALIDATE : features & ~VALIDATE);

            }
            else if (name.equals(Constants.DOM_INFOSET)
                    || name.equals(Constants.DOM_NORMALIZE_CHARACTERS)
                    || name.equals(Constants.DOM_CANONICAL_FORM)
                    || name.equals(Constants.DOM_VALIDATE_IF_SCHEMA)
                    //REVISIT: we need to support true value
                    || name.equals(Constants.DOM_WELLFORMED)) {
                if (state) { // true is not supported
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
            }
            else if (name.equals(Constants.DOM_NAMESPACE_DECLARATIONS)) {
                if (!state) { // false is not supported
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                   throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }

            }
            else if (name.equals(SEND_PSVI) ){
                // REVISIT: turning augmentation of PSVI is not support,
                // because in this case we won't be able to retrieve element
                // default value.
                if (!state) { // false is not supported
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
            }
            else if (name.equals(Constants.DOM_PSVI)){
                  features = (short) (state ? features | PSVI : features & ~PSVI);
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
        else { // set properties
            if (name.equals(Constants.DOM_ERROR_HANDLER)) {
                if (value instanceof DOMErrorHandler) {
                    fErrorHandlerWrapper.setErrorHandler((DOMErrorHandler)value);
                    setErrorHandler(fErrorHandlerWrapper);                    
                }
            
                else {
                    // REVISIT: type mismatch
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
            }       
            else if (name.equals(Constants.DOM_ENTITY_RESOLVER)) {
                if (value instanceof DOMResourceResolver) {
                    try {
                        setEntityResolver(new DOMResourceResolverWrapper((DOMResourceResolver) value));
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    // REVISIT: type mismatch
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }

            }
            else if (name.equals(Constants.DOM_SCHEMA_LOCATION)) {
                if (value instanceof String) {
                    try {
                        String schemaType = (String) getProperty(
                        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE);
                        if (schemaType == Constants.NS_XMLSCHEMA) {
                            // map DOM schema-location to JAXP schemaSource property
                            setProperty(
                                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE,
                                value);
                        }
                        else {
                            // schemaType must not be null.
                            // REVISIT: allow pre-parsing DTD grammars
                            String msg =
                                DOMMessageFormatter.formatMessage(
                                    DOMMessageFormatter.DOM_DOMAIN,
                                    "FEATURE_NOT_SUPPORTED",
                                    new Object[] { name });
                            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                        }

                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    // REVISIT: type mismatch
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }

            }
            else if (name.equals(Constants.DOM_SCHEMA_TYPE)) {
                // REVISIT: should null value be supported?
                if (value instanceof String) {
                    try {
                        if (value.equals(Constants.NS_XMLSCHEMA)) {
                            // REVISIT: when add support to DTD validation
                            setProperty(
                                Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE,
                                Constants.NS_XMLSCHEMA);
                        }
                        else if (value.equals(Constants.NS_DTD)) {                            
                            // REVISIT: revalidation against DTDs is not supported
                             String msg = DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                        }
                    }
                    catch (XMLConfigurationException e) {}
                }
                else {
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }

            }
            else if (name.equals(SYMBOL_TABLE)){
                // Xerces Symbol Table
                if (value instanceof SymbolTable){
                    setProperty(SYMBOL_TABLE, value);
                }
                else {
                    // REVISIT: type mismatch
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
            }
            else if (name.equals (GRAMMAR_POOL)){
                if (value instanceof XMLGrammarPool){
                    setProperty(GRAMMAR_POOL, value);
                }
                else {
                    // REVISIT: type mismatch
                    String msg =
                        DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
                }
                
            }
            else {
                // REVISIT: check if this is a boolean parameter -- type mismatch should be thrown.       
                //parameter is not recognized
                String msg =
                    DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "FEATURE_NOT_FOUND",
                        new Object[] { name });
                throw new DOMException(DOMException.NOT_FOUND_ERR, msg);
            }
        }

    }


    /**
     * DOM Level 3 WD - Experimental.
     * getParameter
     */
	public Object getParameter(String name) throws DOMException {

		// REVISIT: Recognizes DOM L3 default features only.
		//          Does not yet recognize Xerces features.

		if (name.equals(Constants.DOM_COMMENTS)) {
			return ((features & COMMENTS) != 0) ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_NAMESPACES)) {
			return (features & NAMESPACES) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_DATATYPE_NORMALIZATION)) {
			// REVISIT: datatype-normalization only takes effect if validation is on
			return (features & DTNORMALIZATION) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_CDATA_SECTIONS)) {
			return (features & CDATA) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_ENTITIES)) {
			return (features & ENTITIES) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_SPLIT_CDATA)) {
			return (features & SPLITCDATA) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_VALIDATE)) {
			return (features & VALIDATE) != 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		else if (name.equals(Constants.DOM_INFOSET)
				|| name.equals(Constants.DOM_NORMALIZE_CHARACTERS)
				|| name.equals(Constants.DOM_CANONICAL_FORM)
				|| name.equals(Constants.DOM_VALIDATE_IF_SCHEMA)
                //REVISIT: currently its set to false
                || name.equals(Constants.DOM_WELLFORMED)
                ) {
			return Boolean.FALSE;
		}
        else if (name.equals(SEND_PSVI)) {
            return Boolean.TRUE;

        }
        else if (name.equals(Constants.DOM_PSVI)) {
            return (features & PSVI) != 0 ? Boolean.TRUE : Boolean.FALSE;
        }
		else if (
			name.equals(Constants.DOM_NAMESPACE_DECLARATIONS)
				|| name.equals(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT)) {
			return Boolean.TRUE;    
		}        
		else if (name.equals(Constants.DOM_ERROR_HANDLER)) {
            return fErrorHandlerWrapper.getErrorHandler();
		}
		else if (name.equals(Constants.DOM_ENTITY_RESOLVER)) {
			XMLEntityResolver entityResolver = getEntityResolver();
			if (entityResolver != null && entityResolver instanceof DOMResourceResolverWrapper) {
				return ((DOMResourceResolverWrapper) entityResolver).getEntityResolver();
			}
			return null;
		}
		else if (name.equals(Constants.DOM_SCHEMA_TYPE)) {
			return getProperty(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE);
		}
		else if (name.equals(Constants.DOM_SCHEMA_LOCATION)) {
			return getProperty(Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE);
		}
        else if (name.equals(SYMBOL_TABLE)){
            return getProperty(SYMBOL_TABLE);
        }
        else if (name.equals(GRAMMAR_POOL)){
            return getProperty(GRAMMAR_POOL);
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

    /**
     * DOM Level 3 WD - Experimental.
     * canSetParameter
     */
	public boolean canSetParameter(String name, Object state) {
        //features whose parameter value can be set either 'true' or 'false'
		if (name.equals(Constants.DOM_COMMENTS)
			|| name.equals(Constants.DOM_DATATYPE_NORMALIZATION)
			|| name.equals(Constants.DOM_CDATA_SECTIONS)
			|| name.equals(Constants.DOM_ENTITIES)
			|| name.equals(Constants.DOM_SPLIT_CDATA)
			|| name.equals(Constants.DOM_NAMESPACES)
			|| name.equals(Constants.DOM_VALIDATE)) {
			return (state instanceof Boolean) ? true : false;
		}//features whose parameter value can not be set to 'true'
		else if (
			name.equals(Constants.DOM_INFOSET)
				|| name.equals(Constants.DOM_NORMALIZE_CHARACTERS)
				|| name.equals(Constants.DOM_CANONICAL_FORM)
				|| name.equals(Constants.DOM_VALIDATE_IF_SCHEMA)                
                //REVISIT: we need to support true value
                || name.equals(Constants.DOM_WELLFORMED)
                ) {
			if (state instanceof Boolean) {
				return (state.equals(Boolean.TRUE)) ? false : true;
			}
			return false;
		}//features whose parameter value can not be set to 'true'
		else if ( name.equals(Constants.DOM_NAMESPACE_DECLARATIONS)
				|| name.equals(Constants.DOM_WHITESPACE_IN_ELEMENT_CONTENT)
                || name.equals(SEND_PSVI)
                //Xerces has no way to avoid well formed ness checks
                || name.equals(Constants.DOM_WELLFORMED)
                ) {
			if (state instanceof Boolean) {
				return (state.equals(Boolean.TRUE)) ? true : false;
			}
			return false;

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

    //
    // Protected methods
    //

    /**
     * reset all components before parsing
     */
    protected void reset() throws XNIException {

        if (fValidationManager != null)
            fValidationManager.reset();

        int count = fComponents.size();
        for (int i = 0; i < count; i++) {
            XMLComponent c = (XMLComponent) fComponents.elementAt(i);
            c.reset(this);
        }

    } // reset()

    /**
     * Check a property. If the property is known and supported, this method
     * simply returns. Otherwise, the appropriate exception is thrown.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @exception org.apache.xerces.xni.parser.XMLConfigurationException If the
     *            requested feature is not known or supported.
     */
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {

        // special cases
        if (propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)) {
            String property =
                propertyId.substring(Constants.SAX_PROPERTY_PREFIX.length());
            //
            // http://xml.org/sax/properties/xml-string
            // Value type: String
            // Access: read-only
            //   Get the literal string of characters associated with the
            //   current event.  If the parser recognises and supports this
            //   property but is not currently parsing text, it should return
            //   null (this is a good way to check for availability before the
            //   parse begins).
            //
            if (property.equals(Constants.XML_STRING_PROPERTY)) {
                // REVISIT - we should probably ask xml-dev for a precise
                // definition of what this is actually supposed to return, and
                // in exactly which circumstances.
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, propertyId);
            }
        }

        // check property
        super.checkProperty(propertyId);

    } // checkProperty(String)


    protected void addComponent(XMLComponent component) {

        // don't add a component more than once
        if (fComponents.contains(component)) {
            return;
        }
        fComponents.addElement(component);

        // register component's recognized features
        String[] recognizedFeatures = component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);

        // register component's recognized properties
        String[] recognizedProperties = component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);

    } // addComponent(XMLComponent)

    protected ValidationManager createValidationManager(){
        return new ValidationManager();
    }

} // class XMLParser
