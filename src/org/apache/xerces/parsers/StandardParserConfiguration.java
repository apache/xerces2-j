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

import java.io.IOException;
import java.util.Locale;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDocumentScanner;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLDTDScanner;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLValidator;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.DatatypeValidatorFactory;
import org.apache.xerces.impl.validation.GrammarPool;
import org.apache.xerces.impl.validation.datatypes.DatatypeValidatorFactoryImpl;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLLocator;

//import org.xml.sax.Locator;

/**
 * This is the "standard" parser configuration. It extends the basic
 * configuration with the standard set of parser components.
 * <p>
 * In addition to the features and properties recognized by the base
 * parser configuration, this class recognizes these additional 
 * features and properties:
 * <ul>
 * <li>Features
 *  <ul>
 *   <li>http://apache.org/xml/features/validation/warn-on-duplicate-attdef</li>
 *   <li>http://apache.org/xml/features/validation/warn-on-undeclared-elemdef</li>
 *   <li>http://apache.org/xml/features/allow-java-encodings</li>
 *   <li>http://apache.org/xml/features/continue-after-fatal-error</li>
 *   <li>http://apache.org/xml/features/load-external-dtd</li>
 *  </ul>
 * <li>Properties
 *  <ul>
 *   <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *   <li>http://apache.org/xml/properties/internal/entity-manager</li>
 *   <li>http://apache.org/xml/properties/internal/document-scanner</li>
 *   <li>http://apache.org/xml/properties/internal/dtd-scanner</li>
 *   <li>http://apache.org/xml/properties/internal/grammar-pool</li>
 *   <li>http://apache.org/xml/properties/internal/validator</li>
 *   <li>http://apache.org/xml/properties/internal/datatype-validator-factory</li>
 *  </ul>
 * </ul>
 *
 * @author Arnaud  Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class StandardParserConfiguration
    extends BasicParserConfiguration {

    //
    // Constants
    //

    // feature identifiers

    /** Feature identifier: warn on duplicate attribute definition. */
    protected static final String WARN_ON_DUPLICATE_ATTDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    
    /** Feature identifier: warn on undeclared element definition. */
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    
    /** Feature identifier: allow Java encodings. */
    protected static final String ALLOW_JAVA_ENCODINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    
    /** Feature identifier: continue after fatal error. */
    protected static final String CONTINUE_AFTER_FATAL_ERROR = 
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;

    /** Feature identifier: load external DTD. */
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;

    /** Feature identifier: notify built-in refereces. */
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    
    // property identifiers

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: entity manager. */
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    
    /** Property identifier: locator. */
    protected static final String LOCATOR = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCATOR_PROPERTY;
    
    /** Property identifier document scanner: */
    protected static final String DOCUMENT_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;

    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;

    /** Property identifier: grammar pool. */
    protected static final String GRAMMAR_POOL = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.GRAMMAR_POOL_PROPERTY;
    
    /** Property identifier: validator. */
    protected static final String VALIDATOR = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATOR_PROPERTY;

    /** Property identifier: datatype validator factory. */
    protected static final String DATATYPE_VALIDATOR_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;

    // debugging

    /** Set to true and recompile to print exception stack trace. */
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;

    //
    // Data
    //

    // components (non-configurable)

    /** Grammar pool. */
    protected GrammarPool fGrammarPool;

    /** Datatype validator factory. */
    protected DatatypeValidatorFactory fDatatypeValidatorFactory;

    // components (configurable)

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;

    /** Entity manager. */
    protected XMLEntityManager fEntityManager;

    /** Document scanner. */
    protected XMLDocumentScanner fScanner;

    /** DTD scanner. */
    protected XMLDTDScanner fDTDScanner;

    /** Validator. */
    protected XMLValidator fValidator;

    // state

    /** Locator */
    protected XMLLocator fLocator;

    /** 
     * True if a parse is in progress. This state is needed because
     * some features/properties cannot be set while parsing (e.g.
     * validation and namespaces).
     */
    protected boolean fParseInProgress = false;

    //
    // Constructors
    //

    /**
     * Constructs a document parser using the default symbol table and grammar
     * pool or the ones specified by the application (through the properties).
     */
    public StandardParserConfiguration() {
        this(null, null);
    } // <init>()

    /**
     * Constructs a document parser using the specified symbol table.
     */
    public StandardParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a document parser using the specified symbol table and
     * grammar pool.
     */
    public StandardParserConfiguration(SymbolTable symbolTable,
                                       GrammarPool grammarPool) {
        super(symbolTable);

        // add default recognized features
        final String[] recognizedFeatures = {
            WARN_ON_DUPLICATE_ATTDEF,   WARN_ON_UNDECLARED_ELEMDEF,
            ALLOW_JAVA_ENCODINGS,       CONTINUE_AFTER_FATAL_ERROR,
            LOAD_EXTERNAL_DTD,          NOTIFY_BUILTIN_REFS,
        };
        addRecognizedFeatures(recognizedFeatures);

        // set state for default features
        fFeatures.put(WARN_ON_DUPLICATE_ATTDEF, Boolean.FALSE);
        fFeatures.put(WARN_ON_UNDECLARED_ELEMDEF, Boolean.FALSE);
        fFeatures.put(ALLOW_JAVA_ENCODINGS, Boolean.FALSE);
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
        fFeatures.put(NOTIFY_BUILTIN_REFS, Boolean.TRUE);

        // add default recognized properties
        final String[] recognizedProperties = {
            ERROR_REPORTER,             ENTITY_MANAGER, 
            LOCATOR,                    GRAMMAR_POOL,   
            DATATYPE_VALIDATOR_FACTORY,
        };
        addRecognizedProperties(recognizedProperties);

        // create and register missing components
        if (grammarPool == null) {
            if (fGrammarPool == null) {
                fGrammarPool = new GrammarPool();
                fProperties.put(GRAMMAR_POOL, fGrammarPool);
            }
        }
        else {
            fGrammarPool = grammarPool;
            fProperties.put(GRAMMAR_POOL, fGrammarPool);
        }

        fEntityManager = createEntityManager();
        fProperties.put(ENTITY_MANAGER, fEntityManager);
        addComponent(fEntityManager);
        fLocator = (XMLLocator)fEntityManager.getEntityScanner();
        fProperties.put(LOCATOR, fLocator);

        fErrorReporter = createErrorReporter(fEntityManager.getEntityScanner());
        fProperties.put(ERROR_REPORTER, fErrorReporter);
        addComponent(fErrorReporter);

        fScanner = createDocumentScanner();
        fProperties.put(DOCUMENT_SCANNER, fScanner);
        addComponent(fScanner);

        fDTDScanner = createDTDScanner();
        if (fDTDScanner != null) {
            fProperties.put(DTD_SCANNER, fDTDScanner);
            addComponent(fDTDScanner);
        }

        fValidator = createValidator();
        if (fValidator != null) {
            fProperties.put(VALIDATOR, fValidator);
            addComponent(fValidator);
        }
        
        fDatatypeValidatorFactory = createDatatypeValidatorFactory();
        if (fDatatypeValidatorFactory != null) {
            fProperties.put(DATATYPE_VALIDATOR_FACTORY,
                            fDatatypeValidatorFactory);
        }

        // add message formatters
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }

        // set locale
        try {
            setLocale(Locale.getDefault());
        }
        catch (XNIException e) {
            // do nothing
            // REVISIT: What is the right thing to do? -Ac
        }

    } // <init>(SymbolTable,GrammarPool)

    //
    // Public methods
    //

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception XNIException Thrown if the parser does not support the
     *                         specified locale.
     */
    public void setLocale(Locale locale) throws XNIException {
        if (fErrorReporter == null) {
            if (fEntityManager == null) {
                fEntityManager = createEntityManager();
                fProperties.put(ENTITY_MANAGER, fEntityManager);
                addComponent(fEntityManager);
                fLocator = (XMLLocator)fEntityManager.getEntityScanner();
            }
            fErrorReporter =
                createErrorReporter(fEntityManager.getEntityScanner());
            fProperties.put(ERROR_REPORTER, fErrorReporter);
            addComponent(fErrorReporter);
        }
        fErrorReporter.setLocale(locale);
    } // setLocale(Locale)

    //
    // XMLParserConfiguration methods
    //

    /**
     * Parses the specified input source.
     *
     * @param source The input source.
     *
     * @exception XNIException Throws exception on XNI error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(XMLInputSource source) throws XNIException, IOException {

        if (fParseInProgress) {
            // REVISIT - need to add new error message
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress = true;

        try {
            reset();
            fEntityManager.setEntityHandler(fScanner);
            fEntityManager.startDocumentEntity(source);
            fScanner.scanDocument(true);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
        finally {
            fParseInProgress = false;
        }

    } // parse(InputSource)

    //
    // Protected methods
    //
    
    /** 
     * Reset all components before parsing. 
     *
     * @throws SAXException Thrown if an error occurs during initialization.
     */
    protected void reset() throws XNIException {

        // configure the pipeline and initialize the components
        configurePipeline();
        super.reset();

    } // reset()

    /** Configures the pipeline. */
    protected void configurePipeline() {

        // REVISIT: This should be better designed. In other words, we
        //          need to figure out what is the best way for people to
        //          re-use *most* of the standard configuration but do 
        //          things common things such as remove a component (e.g.
        //          the validator), insert a new component (e.g. XInclude), 
        //          etc... -Ac

        // setup document pipeline
        if (fValidator != null) {
            fScanner.setDocumentHandler(fValidator);
            fValidator.setDocumentHandler(fDocumentHandler);
        }
        else {
            fScanner.setDocumentHandler(fDocumentHandler);
        }

        // setup dtd pipeline
        if (fDTDScanner != null) {
            fDTDScanner.setDTDHandler(fValidator);
            fValidator.setDTDHandler(fDTDHandler);
            
            fDTDScanner.setDTDContentModelHandler(fValidator);
            fValidator.setDTDContentModelHandler(fDTDContentModelHandler);
        }

    } // configurePipeline()

    // features and properties

    /**
     * Check a feature. If feature is know and supported, this method simply
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
        throws XMLConfigurationException {

        //
        // Xerces Features
        //

        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            String feature = featureId.substring(Constants.XERCES_FEATURE_PREFIX.length());
            //
            // http://apache.org/xml/features/validation/schema
            //   Lets the user turn Schema validation support on/off.
            //
            if (feature.equals(Constants.SCHEMA_VALIDATION_FEATURE)) {
                return;
            }
            //
            // http://apache.org/xml/features/validation/dynamic
            //   Allows the parser to validate a document only when it
            //   contains a grammar. Validation is turned on/off based
            //   on each document instance, automatically.
            //
            if (feature.equals(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                // REVISIT
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                // REVISIT
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-dtd-grammar
            //
            if (feature.equals(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return;
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-external-dtd
            //
            if (feature.equals(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return;
            }

            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals(Constants.VALIDATE_DATATYPES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
        }

        //
        // Not recognized
        //

        super.checkFeature(featureId);

    } // checkFeature(String)

    /**
     * Check a property. If the property is know and supported, this method
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
        throws XMLConfigurationException {

        //
        // Xerces Properties
        //

        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String property = propertyId.substring(Constants.XERCES_PROPERTY_PREFIX.length());
            if (property.equals(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
        }

        //
        // Not recognized
        //

        super.checkProperty(propertyId);

    } // checkProperty(String)

    // factory methods

    /** Creates an entity manager. */
    protected XMLEntityManager createEntityManager() {
        return new XMLEntityManager();
    } // createEntityManager():XMLEntityManager

    /** Creates an error reporter. */
    protected XMLErrorReporter createErrorReporter(XMLLocator locator) {
        return new XMLErrorReporter(locator);
    } // createErrorReporter(XMLLocator):XMLErrorReporter

    /** Create a document scanner. */
    protected XMLDocumentScanner createDocumentScanner() {
        return new XMLDocumentScanner();
    } // createDocumentScanner():XMLDocumentScanner

    /** Create a DTD scanner. */
    protected XMLDTDScanner createDTDScanner() {
        return new XMLDTDScanner();
    } // createDTDScanner():XMLDTDScanner

    /** Create a validator. */
    protected XMLValidator createValidator() {
        return new XMLValidator();
    } // createValidator():XMLValidator

    /** Create a datatype validator factory. */
    protected DatatypeValidatorFactory createDatatypeValidatorFactory() {
        return new DatatypeValidatorFactoryImpl();
    } // createDatatypeValidatorFactory():DatatypeValidatorFactory

} // class StandardParserConfiguration
