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

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * This configuration holds information needed for revalidation 
 * of the DOM tree, ie. properties and features.
 * Note: This configuration is different from any parser
 * configuration and must not be used other than for DOM revalidation.
 * 
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class DOMValidationConfiguration extends ParserConfigurationSettings
    implements XMLParserConfiguration {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: validation. */
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
    /** Feature identifier: namespaces. */
    protected static final String NAMESPACES =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;

    protected static final String SCHEMA = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    
    protected static final String DYNAMIC_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;

    protected static final String NORMALIZE_DATA = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;

    
    
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


    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    //
    // Data
    //
    XMLDocumentHandler fDocumentHandler;

    // components (non-configurable)

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    // data

    /** Components. */
    protected Vector fComponents;


    /** Locale. */
    protected Locale fLocale;
    protected XMLErrorReporter fErrorReporter;

    //
    // Constructors
    //

    /** Default Constructor. */
    protected DOMValidationConfiguration() {
        this(null, null);
    } // <init>()

    /** 
     * Constructs a parser configuration using the specified symbol table. 
     *
     * @param symbolTable The symbol table to use.
     */
    protected DOMValidationConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /** 
     * Constructs a parser configuration using the specified symbol table
     * and parent settings.
     *
     * @param symbolTable    The symbol table to use.
     * @param parentSettings The parent settings.
     */
    protected DOMValidationConfiguration(SymbolTable symbolTable,
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
            VALIDATION,                 
            NAMESPACES,
            SCHEMA,
            DYNAMIC_VALIDATION,
            NORMALIZE_DATA
        };
        addRecognizedFeatures(recognizedFeatures);

        // set state for default features
        setFeature(VALIDATION, false);
        setFeature(SCHEMA, false);
        setFeature(DYNAMIC_VALIDATION, false);
        setFeature(NORMALIZE_DATA, true);
        setFeature(NAMESPACES, true);

        // add default recognized properties
        final String[] recognizedProperties = {
            XML_STRING,     
            SYMBOL_TABLE,
            ERROR_HANDLER,  
            ENTITY_RESOLVER,
            ERROR_REPORTER,
            ENTITY_MANAGER,
            VALIDATION_MANAGER,
            GRAMMAR_POOL
        };
        addRecognizedProperties(recognizedProperties);

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
        
        setProperty(VALIDATION_MANAGER, new ValidationManager());


        // add message formatters
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }

        if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            XSMessageFormatter xmft = new XSMessageFormatter();
            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
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

    //
    // Protected methods
    //

    /**
     * reset all components before parsing
     */
    protected void reset() throws XNIException {

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


} // class XMLParser
