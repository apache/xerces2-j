/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  
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

package org.apache.xerces.parsers;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * A very basic parser configuration. This configuration class can
 * be used as a base class for custom parser configurations. The
 * basic parser configuration creates the symbol table (if not
 * specified at construction time) and manages all of the recognized
 * features and properties.
 * <p>
 * The basic parser configuration does <strong>not</strong> mandate
 * any particular pipeline configuration or the use of specific 
 * components except for the symbol table. If even this is too much
 * for a basic parser configuration, the programmer can create a new
 * configuration class that implements the 
 * <code>XMLParserConfiguration</code> interface.
 * <p>
 * Subclasses of the basic parser configuration can add their own
 * recognized features and properties by calling the
 * <code>addRecognizedFeature</code> and 
 * <code>addRecognizedProperty</code> methods, respectively.
 * <p>
 * The basic parser configuration assumes that the configuration
 * will be made up of various parser components that implement the
 * <code>XMLComponent</code> interface. If subclasses of this
 * configuration create their own components for use in the 
 * parser configuration, then each component should be added to
 * the list of components by calling the <code>addComponent</code>
 * method. The basic parser configuration will make sure to call
 * the <code>reset</code> method of each registered component
 * before parsing an instance document.
 * <p>
 * This class recognizes the following features and properties:
 * <ul>
 * <li>Features
 *  <ul>
 *   <li>http://xml.org/sax/features/validation</li>
 *   <li>http://xml.org/sax/features/namespaces</li>
 *   <li>http://xml.org/sax/features/external-general-entities</li>
 *   <li>http://xml.org/sax/features/external-parameter-entities</li>
 *  </ul>
 * <li>Properties
 *  <ul>
 *   <li>http://xml.org/sax/properties/xml-string</li>
 *   <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *   <li>http://apache.org/xml/properties/internal/error-handler</li>
 *   <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 *  </ul>
 * </ul>
 *
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public abstract class BasicParserConfiguration
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
    
    /** Feature identifier: external general entities. */
    protected static final String EXTERNAL_GENERAL_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
    
    /** Feature identifier: external parameter entities. */
    protected static final String EXTERNAL_PARAMETER_ENTITIES =
        Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
    
    // property identifiers

    /** Property identifier: xml string. */
    protected static final String XML_STRING = 
        Constants.SAX_PROPERTY_PREFIX + Constants.XML_STRING_PROPERTY;

    /** Property identifier: symbol table. */
    protected static final String SYMBOL_TABLE = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: error handler. */
    protected static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;

    /** Property identifier: entity resolver. */
    protected static final String ENTITY_RESOLVER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    //
    // Data
    //

    // components (non-configurable)

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    // data

    /** Recognized properties. */
    protected Vector fRecognizedProperties;

    /** Properties. */
    protected Hashtable fProperties;

    /** Recognized features. */
    protected Vector fRecognizedFeatures;

    /** Features. */
    protected Hashtable fFeatures;

    /** Components. */
    protected Vector fComponents;

    // handlers

    /** The document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    /** The DTD handler. */
    protected XMLDTDHandler fDTDHandler;

    /** The DTD content model handler. */
    protected XMLDTDContentModelHandler fDTDContentModelHandler;

    //
    // Constructors
    //

    /** Default Constructor. */
    protected BasicParserConfiguration() {
        this(null);
    } // <init>()

    /**
     * Constructs a document parser using the specified symbol table
     * and a default grammar pool.
     *
     */
    protected BasicParserConfiguration(SymbolTable symbolTable) {

        // create a vector to hold all the components in use
        fComponents = new Vector();

        // create storage for recognized features and properties
        fRecognizedFeatures = new Vector();
        fRecognizedProperties = new Vector();

        // create table for features and properties
        fFeatures = new Hashtable();
        fProperties = new Hashtable();

        // add default recognized features
        final String[] recognizedFeatures = {
            VALIDATION,                 NAMESPACES, 
            EXTERNAL_GENERAL_ENTITIES,  EXTERNAL_PARAMETER_ENTITIES,
        };
        addRecognizedFeatures(recognizedFeatures);

        // set state for default features
        fFeatures.put(VALIDATION, Boolean.FALSE);
        fFeatures.put(NAMESPACES, Boolean.TRUE);
        fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);

        // add default recognized properties
        final String[] recognizedProperties = {
            XML_STRING,     SYMBOL_TABLE,
            ERROR_HANDLER,  ENTITY_RESOLVER,
        };
        addRecognizedProperties(recognizedProperties);

        if (symbolTable == null) {
            if (fSymbolTable == null) {
                fSymbolTable = new SymbolTable();
                fProperties.put(SYMBOL_TABLE, fSymbolTable);
            }
        }
        else {
            fSymbolTable = symbolTable;
            fProperties.put(SYMBOL_TABLE, fSymbolTable);
        }

    } // <init>(SymbolTable)

    /** 
     * Adds a component to the parser configuration. This method will
     * also add all of the component's recognized features and properties
     * to the list of default recognized features and properties.
     *
     * @param component The component to add.
     */
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

    //
    // Public methods
    //

    /**
     * Parses the input source specified by the given system identifier.
     * <p>
     * This method is equivalent to the following:
     * <pre>
     *     parse(new InputSource(systemId));
     * </pre>
     *
     * @param source The input source.
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    /***
    public void parse(String systemId)
        throws XNIException, IOException {

        InputSource source = new InputSource(systemId);
        parse(source);
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

    } // parse(String)

    /**
     * parse
     *
     * @param inputSource
     *
     * @exception org.xml.sax.SAXException
     * @exception java.io.IOException
     */
    public abstract void parse(InputSource inputSource) 
        throws XNIException, IOException;


    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    }

    public void setDTDHandler(XMLDTDHandler handler) {
        fDTDHandler = handler;
    }

    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        fDTDContentModelHandler = handler;
    }

    /**
     * Sets the resolver used to resolve external entities. The EntityResolver
     * interface supports resolution of public and system identifiers.
     *
     * @param resolver The new entity resolver. Passing a null value will
     *                 uninstall the currently installed resolver.
     */
    public void setEntityResolver(EntityResolver resolver) {
        fProperties.put(ENTITY_RESOLVER, resolver);
    } // setEntityResolver(EntityResolver)

    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none
     *         has been registered.
     * @see #setEntityResolver
     */
    public EntityResolver getEntityResolver() {
        return (EntityResolver)fProperties.get(ENTITY_RESOLVER);
    } // getEntityResolver():EntityResolver

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
    public void setErrorHandler(ErrorHandler errorHandler) {
        fProperties.put(ERROR_HANDLER, errorHandler);
    } // setErrorHandler(ErrorHandler)

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return (ErrorHandler)fProperties.get(ERROR_HANDLER);
    } // getErrorHandler():ErrorHandler

    /**
     * Allows a parser to add parser specific features to be recognized
     * and managed by the parser configuration.
     *
     * @param featureIds An array of the additional feature identifiers 
     *                   to be recognized.
     */
    public void addRecognizedFeatures(String[] featureIds) {

        // add recognized features
        int featureIdsCount = featureIds != null ? featureIds.length : 0;
        for (int i = 0; i < featureIdsCount; i++) {
            String featureId = featureIds[i];
            if (!fRecognizedFeatures.contains(featureId)) {
                fRecognizedFeatures.addElement(featureId);
            }
        }

    } // addRecognizedFeatures(String[])

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
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        checkFeature(featureId);

        // forward to every component
        int count = fComponents.size();
        for (int i = 0; i < count; i++) {
            XMLComponent c = (XMLComponent) fComponents.elementAt(i);
            c.setFeature(featureId, state);
        }
        // then store the information
        fFeatures.put(featureId, state ? Boolean.TRUE : Boolean.FALSE);

    } // setFeature(String,boolean)

    /**
     * Allows a parser to add parser specific properties to be recognized
     * and managed by the parser configuration.
     *
     * @param propertyIds An array of the additional property identifiers 
     *                    to be recognized.
     */
    public void addRecognizedProperties(String[] propertyIds) {

        // add recognizedProperties
        int propertyIdsCount = propertyIds != null ? propertyIds.length : 0;
        for (int i = 0; i < propertyIdsCount; i++) {
            String propertyId = propertyIds[i];
            if (!fRecognizedProperties.contains(propertyId)) {
                fRecognizedProperties.addElement(propertyId);
            }
        }

    } // addRecognizedProperties(String[])

    /**
     * setProperty
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        checkProperty(propertyId);

        // forward to every component
        int count = fComponents.size();
        for (int i = 0; i < count; i++) {
            XMLComponent c = (XMLComponent) fComponents.elementAt(i);
            c.setProperty(propertyId, value);
        }
        // then store the information
        fProperties.put(propertyId, value);

    } // setProperty(String,Object)

    //
    // XMLComponentManager methods
    //

    /**
     * Returns the state of a feature.
     * 
     * @param featureId The feature identifier.
     * 
     * @throws SAXNotRecognizedException Thrown if the feature is not 
     *                                   recognized.
     * @throws SAXNotSupportedException Thrown if the feature is not
     *                                  supported.
     */
    public boolean getFeature(String featureId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        checkFeature(featureId);

        Boolean state = (Boolean) fFeatures.get(featureId);
        return state != null ? state.booleanValue() : false;

    } // getFeature(String):boolean

    /**
     * Returns the value of a property.
     * 
     * @param propertyId The property identifier.
     * 
     * @throws SAXNotRecognizedException Thrown if the feature is not 
     *                                   recognized.
     * @throws SAXNotSupportedException Thrown if the feature is not
     *                                  supported.
     */
    public Object getProperty(String propertyId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        checkProperty(propertyId);

        return fProperties.get(propertyId);

    } // getProperty(String):Object

    /*** These should be queried through the property mechanism. -Ac ***
    public Locator getLocator() {
        return fLocator;
    } // getLocator():Locator

    public SymbolTable getSymbolTable() {
        return fSymbolTable;
    } // getSymbolTable():SymbolTable

    public Hashtable getFeatureTable() {
        return fFeatures;
    }

    public Hashtable getPropertyTable() {
        return fProperties;
    }
    /***/

    //
    // Protected methods
    //

    /**
     * reset all components before parsing
     */
    protected void reset() throws SAXException {

        // reset every component
        int count = fComponents.size();
        for (int i = 0; i < count; i++) {
            XMLComponent c = (XMLComponent) fComponents.elementAt(i);
            c.reset(this);
        }

    } // reset(XMLParser)

    /**
     * Check a feature. If feature is known and supported, this method simply
     * returns. Otherwise, the appropriate exception is thrown.
     *
     * @param featureId The unique identifier (URI) of the feature.
     *
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    protected void checkFeature(String featureId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        // check feature
        if (!fRecognizedFeatures.contains(featureId)) {
            throw new SAXNotRecognizedException(featureId);
        }

    } // checkFeature(String)

    /**
     * Check a property. If the property is known and supported, this method
     * simply returns. Otherwise, the appropriate exception is thrown.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    protected void checkProperty(String propertyId)
        throws SAXNotRecognizedException, SAXNotSupportedException {

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
                throw new SAXNotSupportedException(propertyId);
            }
        }

        // check property
        if (!fRecognizedProperties.contains(propertyId)) {
            throw new SAXNotRecognizedException(propertyId);
        }

    } // checkProperty(String)

} // class XMLParser
