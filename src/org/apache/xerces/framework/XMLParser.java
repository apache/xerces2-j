/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.framework;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.readers.XMLDeclRecognizer;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.readers.XMLEntityReaderFactory;
import org.apache.xerces.utils.ChunkyCharArray;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLMessageProvider;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.utils.ImplementationMessages;
import org.apache.xerces.validators.common.GrammarResolver;
import org.apache.xerces.validators.common.GrammarResolverImpl;
import org.apache.xerces.validators.common.XMLValidator;
import org.apache.xerces.validators.datatype.DatatypeMessageProvider;
import org.apache.xerces.validators.datatype.DatatypeValidatorFactoryImpl;
import org.apache.xerces.validators.schema.SchemaMessageProvider;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

/**
 * This is the base class of all standard parsers.
 *
 * @version $Id$
 */
public abstract class XMLParser 
    implements XMLErrorReporter, XMLDocumentHandler.DTDHandler {

    //
    // Constants
    //

    // protected

    /** SAX2 features prefix (http://xml.org/sax/features/). */
    protected static final String SAX2_FEATURES_PREFIX = "http://xml.org/sax/features/";

    /** SAX2 properties prefix (http://xml.org/sax/properties/). */
    protected static final String SAX2_PROPERTIES_PREFIX = "http://xml.org/sax/properties/";

    /** Xerces features prefix (http://apache.org/xml/features/). */
    protected static final String XERCES_FEATURES_PREFIX = "http://apache.org/xml/features/";

    /** Xerces properties prefix (http://apache.org/xml/properties/). */
    protected static final String XERCES_PROPERTIES_PREFIX = "http://apache.org/xml/properties/";

    // private

    /** Features recognized by this parser. */
    private static final String RECOGNIZED_FEATURES[] = {
        // SAX2 core
        "http://xml.org/sax/features/validation",
        "http://xml.org/sax/features/external-general-entities",
        "http://xml.org/sax/features/external-parameter-entities",
        "http://xml.org/sax/features/namespaces",
        // Xerces
        "http://apache.org/xml/features/validation/schema",
        "http://apache.org/xml/features/validation/dynamic",
        "http://apache.org/xml/features/validation/default-attribute-values",
        "http://apache.org/xml/features/validation/validate-content-models",
        "http://apache.org/xml/features/validation/validate-datatypes",
        "http://apache.org/xml/features/validation/warn-on-duplicate-attdef",
        "http://apache.org/xml/features/validation/warn-on-undeclared-elemdef",
        "http://apache.org/xml/features/allow-java-encodings",
        "http://apache.org/xml/features/continue-after-fatal-error",
        "http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
        "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    };

    /** Properties recognized by this parser. */
    private static final String RECOGNIZED_PROPERTIES[] = {
        // SAX2 core
        "http://xml.org/sax/properties/xml-string",
        // Xerces
    };

    // debugging

    /** Set to true and recompile to print exception stack trace. */
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;

    //
    // Data
    //

    protected GrammarResolver fGrammarResolver = null;

    // state

    protected boolean fParseInProgress = false;
    private boolean fNeedReset = false;

    // features

    /** Continue after fatal error. */
    private boolean fContinueAfterFatalError = false;

    // properties

    /** Error handler. */
    private ErrorHandler fErrorHandler = null;

    // other

    private Locale fLocale = null;

    // error information

    private static XMLMessageProvider fgXMLMessages = new XMLMessages();
    private static XMLMessageProvider fgImplementationMessages = new ImplementationMessages();
    private static XMLMessageProvider fgSchemaMessages = new SchemaMessageProvider();
    private static XMLMessageProvider fgDatatypeMessages= new DatatypeMessageProvider();

    //
    //
    //
    protected StringPool fStringPool = null;
    protected XMLErrorReporter fErrorReporter = null;
    protected DefaultEntityHandler fEntityHandler = null;
    protected XMLDocumentScanner fScanner = null;
    protected XMLValidator fValidator = null;

    //
    // Constructors
    //

    /**
     * Constructor
     */
    protected XMLParser() {
        this(new StringPool());
    }

    protected XMLParser(StringPool stringPool) {
        fStringPool = stringPool;
        fErrorReporter = this;
        fEntityHandler = new DefaultEntityHandler(fStringPool, fErrorReporter);
        fScanner = new XMLDocumentScanner(fStringPool, fErrorReporter, fEntityHandler, new ChunkyCharArray(fStringPool));
        fValidator = new XMLValidator(fStringPool, fErrorReporter, fEntityHandler, fScanner);
        fGrammarResolver = new GrammarResolverImpl();
        fScanner.setGrammarResolver(fGrammarResolver);
        fValidator.setGrammarResolver(fGrammarResolver);
        try {
            //JR-defect 48 fix - turn on Namespaces
            setNamespaces(true);
        }
        catch (Exception e) {
            // ignore
        }
    }

    /**
     * Set char data processing preference and handlers.
     */
    protected void initHandlers(boolean sendCharDataAsCharArray,
                                XMLDocumentHandler docHandler,
                                XMLDocumentHandler.DTDHandler dtdHandler)
    {
        fValidator.initHandlers(sendCharDataAsCharArray, docHandler, dtdHandler);
        fScanner.setDTDHandler(this);
    }

    //
    // Public methods
    //

    // features and properties

    /**
     * Returns a list of features that this parser recognizes.
     * This method will never return null; if no features are
     * recognized, this method will return a zero length array.
     *
     * @see #isFeatureRecognized
     * @see #setFeature
     * @see #getFeature
     */
    public String[] getFeaturesRecognized() {
        return RECOGNIZED_FEATURES;
    }

    /**
     * Returns true if the specified feature is recognized.
     *
     * @see #getFeaturesRecognized
     * @see #setFeature
     * @see #getFeature
     */
    public boolean isFeatureRecognized(String featureId) {
        String[] recognizedFeatures = getFeaturesRecognized();
        for (int i = 0; i < recognizedFeatures.length; i++) {
            if (featureId.equals(recognizedFeatures[i]))
                return true;
        }
        return false;
    }

    /**
     * Returns a list of properties that this parser recognizes.
     * This method will never return null; if no properties are
     * recognized, this method will return a zero length array.
     *
     * @see #isPropertyRecognized
     * @see #setProperty
     * @see #getProperty
     */
    public String[] getPropertiesRecognized() {
        return RECOGNIZED_PROPERTIES;
    }

    /**
     * Returns true if the specified property is recognized.
     *
     * @see #getPropertiesRecognized
     * @see #setProperty
     * @see #getProperty
     */
    public boolean isPropertyRecognized(String propertyId) {
        String[] recognizedProperties = getPropertiesRecognized();
        for (int i = 0; i < recognizedProperties.length; i++) {
            if (propertyId.equals(recognizedProperties[i]))
                return true;
        }
        return false;
    }

    // initialization

    /**
     * Setup for application-driven parsing.
     *
     * @param source the input source to be parsed.
     * @see #parseSome
     */
    public boolean parseSomeSetup(InputSource source) throws Exception {
        if (fNeedReset)
            resetOrCopy();
        fParseInProgress = true;
        fNeedReset = true;
        return fEntityHandler.startReadingFromDocument(source);
    }

    /**
     * Application-driven parsing.
     *
     * @see #parseSomeSetup
     */
    public boolean parseSome() throws Exception {
        if (!fScanner.parseSome(false)) {
            fParseInProgress = false;
            return false;
        }
        return true;
    }

    // resetting

    /** Reset parser instance so that it can be reused. */
    public void reset() throws Exception {
        fGrammarResolver.clearGrammarResolver();
        fStringPool.reset();
        fEntityHandler.reset(fStringPool);
        fScanner.reset(fStringPool, new ChunkyCharArray(fStringPool));
        fValidator.reset(fStringPool);
        fNeedReset = false;
    }

    // properties (the normal kind)

    /**
     * return the locator being used by the parser
     *
     * @return the parser's active locator
     */
    public final Locator getLocator() {
        return fEntityHandler;
    }

    
    /**
     * return the locale               
     *
     * @return the locale
     */
    public final Locale getfLocale() {
        return fLocale; 
    }

    /**
     * return the XML Messages object               
     *
     * @return the parser's messages object
     */
    public final XMLMessageProvider getfgXMLMessages() {
        return fgXMLMessages; 
    }

    /**
     * return the Implementation Messages object               
     *
     * @return the parser's implementation messages
     */
    public final XMLMessageProvider getfgImplementationMessages() {
        return fgImplementationMessages;
    }

    /**
     * return the Schema Messages object               
     *
     * @return the parser's schema messages
     */
    public final XMLMessageProvider getfgSchemaMessages() {
        return fgSchemaMessages;
    }

    /**
     * return the Datatypes Messages object               
     *
     * @return the parser's datatypes messages
     */
    public final XMLMessageProvider getfgDatatypeMessages() {
        return fgDatatypeMessages;
    }

    /**
     * Set the reader factory.
     */
    public void setReaderFactory(XMLEntityReaderFactory readerFactory) {
        fEntityHandler.setReaderFactory(readerFactory);
    }

    /**
     * Adds a recognizer.
     *
     * @param recognizer The XML recognizer to add.
     */
    public void addRecognizer(XMLDeclRecognizer recognizer) {
        fEntityHandler.addRecognizer(recognizer);
    }

    //
    // Protected methods
    //

    // SAX2 core features

    /**
     * Sets whether the parser validates.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/validation
     * </pre>
     *
     * @param validate True to validate; false to not validate.
     *
     * @see #getValidation
     * @see #setFeature
     */
    protected void setValidation(boolean validate) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            throw new SAXNotSupportedException("PAR004 Cannot setFeature(http://xml.org/sax/features/validation): parse is in progress.\n"+
                                               "http://xml.org/sax/features/validation");
        }
        try {
            // REVISIT: [Q] Should the scanner tell the validator that
            //              validation is on? -Ac
            fScanner.setValidationEnabled(validate);
            fValidator.setValidationEnabled(validate);
        }
        catch (Exception ex) {
            throw new SAXNotSupportedException(ex.getMessage());
        }
    }

    /**
     * Returns true if validation is turned on.
     *
     * @see #setValidation
     */
    protected boolean getValidation() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getValidationEnabled();
    }

    /**
     * <b>Note: Currently, this parser always expands external general
     * entities.</b> Setting this feature to false will throw a
     * SAXNotSupportedException.
     * <p>
     * Sets whether external general entities are expanded.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/external-general-entities
     * </pre>
     *
     * @param expand True to expand external general entities; false
     *               to not expand.
     *
     * @see #getExternalGeneralEntities
     * @see #setFeature
     */
    protected void setExternalGeneralEntities(boolean expand)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            throw new SAXNotSupportedException("PAR004 Cannot setFeature(http://xml.org/sax/features/external-general-entities): parse is in progress.\n"+
                                               "http://xml.org/sax/features/external-general-entities");
        }
        if (!expand) {
            throw new SAXNotSupportedException("http://xml.org/sax/features/external-general-entities");
        }
    }

    /**
     * <b>Note: This feature is always true.</b>
     * <p>
     * Returns true if external general entities are expanded.
     *
     * @see #setExternalGeneralEntities
     */
    protected boolean getExternalGeneralEntities() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return true;
    }

    /**
     * <b>Note: Currently, this parser always expands external parameter
     * entities.</b> Setting this feature to false will throw a
     * SAXNotSupportedException.
     * <p>
     * Sets whether external parameter entities are expanded.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/external-parameter-entities
     * </pre>
     *
     * @param expand True to expand external parameter entities; false
     *               to not expand.
     *
     * @see #getExternalParameterEntities
     * @see #setFeature
     */
    protected void setExternalParameterEntities(boolean expand)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            throw new SAXNotSupportedException("PAR004 Cannot setFeature(http://xml.org/sax/features/external-general-entities): parse is in progress.\n"+
                                               "http://xml.org/sax/features/external-general-entities");
        }
        if (!expand) {
            throw new SAXNotSupportedException("http://xml.org/sax/features/external-parameter-entities");
        }
    }

    /**
     * <b>Note: This feature is always true.</b>
     * <p>
     * Returns true if external parameter entities are expanded.
     *
     * @see #setExternalParameterEntities
     */
    protected boolean getExternalParameterEntities() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return true;
    }

    /**
     * Sets whether the parser preprocesses namespaces.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/namespaces
     * <pre>
     *
     * @param process True to process namespaces; false to not process.
     *
     * @see #getNamespaces
     * @see #setFeature
     */
    protected void setNamespaces(boolean process) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            throw new SAXNotSupportedException("PAR004 Cannot setFeature(http://xml.org/sax/features/namespaces): parse is in progress.\n"+
                                               "http://xml.org/sax/features/namespaces");
        }
        fScanner.setNamespacesEnabled(process);
        // REVISIT: [Q] Should the scanner tell the validator that namespace
        //              processing is on? -Ac
        fValidator.setNamespacesEnabled(process);
    }

    /**
     * Returns true if the parser preprocesses namespaces.
     *
     * @see #setNamespaces
     */
    protected boolean getNamespaces() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getNamespacesEnabled();
    }

    // Xerces features

    /**
     * Allows the user to turn Schema support on/off.
     * <p>
     * This method is equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/validation/schema
     * </pre>
     *
     * @param schema True to turn on Schema support; false to turn it off.
     *
     * @see #getValidationSchema
     * @see #setFeature
     */
    protected void setValidationSchema(boolean schema) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            // REVISIT: Localize message
            throw new SAXNotSupportedException("http://apache.org/xml/features/validation/schema: parse is in progress");
        }
        fValidator.setSchemaValidationEnabled(schema);
    }

    /**
     * Returns true if Schema support is turned on.
     *
     * @see #setValidationSchema
     */
    protected boolean getValidationSchema() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getSchemaValidationEnabled();
    }

    /**
     * Allows the parser to validate a document only when it contains a
     * grammar. Validation is turned on/off based on each document
     * instance, automatically.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/validation/dynamic
     * </pre>
     *
     * @param dynamic True to dynamically validate documents; false to
     *                validate based on the validation feature.
     *
     * @see #getValidationDynamic
     * @see #setFeature
     */
    protected void setValidationDynamic(boolean dynamic) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            // REVISIT: Localize message
            throw new SAXNotSupportedException("http://apache.org/xml/features/validation/dynamic: parse is in progress");
        }
        try {
            fValidator.setDynamicValidationEnabled(dynamic);
        }
        catch (Exception ex) {
            throw new SAXNotSupportedException(ex.getMessage());
        }
    }

    /**
     * Returns true if validation is based on whether a document
     * contains a grammar.
     *
     * @see #setValidationDynamic
     */
    protected boolean getValidationDynamic() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getDynamicValidationEnabled();
    }

    /**
     *
     */
    protected void setNormalizeAttributeValues(boolean normalize) {
        fValidator.setNormalizeAttributeValues(normalize);
    }

    /**
     * Allows the parser to have the choice to load DTD grammar when 
     * validation is off.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/nonvalidating/load-dtd-grammar
     * </pre>
     *
     * @param loadDTDGrammar True to turn on the feature; false to
     *                turn off the feature.
     *
     * @see #getLoadDTDGrammar
     * @see #setFeature
     */
    protected void setLoadDTDGrammar(boolean loadDTDGrammar) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            // REVISIT: Localize message
            throw new SAXNotSupportedException("http://apache.org/xml/features/nonvalidating/load-dtd-grammar: parse is in progress");
        }
        try {
            fValidator.setLoadDTDGrammar(loadDTDGrammar);
        }
        catch (Exception ex) {
            throw new SAXNotSupportedException(ex.getMessage());
        }
    }

    /**
     * Returns true if load DTD grammar is turned on in the XMLValiator.
     *
     * @see #setLoadDTDGrammar
     */
    protected boolean getLoadDTDGrammar() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getLoadDTDGrammar();
    }

    /**
     * Allows the parser to have the choice to load the external DTD when 
     * validation is off.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/nonvalidating/load-external-dtd
     * </pre>
     *
     * @param loadExternalDTD True to turn on the feature; false to
     *                turn off the feature.
     *
     * @see #getLoadExternalDTD
     * @see #setFeature
     */
    protected void setLoadExternalDTD(boolean loadExternalDTD) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (fParseInProgress) {
            // REVISIT: Localize message
            throw new SAXNotSupportedException("http://apache.org/xml/features/nonvalidating/load-external-dtd: parse is in progress");
        }
        try {
            fScanner.setLoadExternalDTD(loadExternalDTD);
        }
        catch (Exception ex) {
            throw new SAXNotSupportedException(ex.getMessage());
        }
    }

    /**
     * Returns true if loading of the external DTD is on.
     *
     * @see #setLoadExternalDTD
     */
    protected boolean getLoadExternalDTD() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fScanner.getLoadExternalDTD();
    }

    /**
     * Sets whether an error is emitted when an attribute is redefined
     * in the grammar.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/validation/warn-on-duplicate-attdef
     * </pre>
     *
     * @param warn True to warn; false to not warn.
     *
     * @see #getValidationWarnOnDuplicateAttdef
     * @see #setFeature
     */
    protected void setValidationWarnOnDuplicateAttdef(boolean warn)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fValidator.setWarningOnDuplicateAttDef(warn);
    }

    /**
     * Returns true if an error is emitted when an attribute is redefined
     * in the grammar.
     *
     * @see #setValidationWarnOnDuplicateAttdef
     */
    protected boolean getValidationWarnOnDuplicateAttdef()
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getWarningOnDuplicateAttDef();
    }

    /**
     * Sets whether the parser emits an error when an element's content
     * model references an element by name that is not declared in the
     * grammar.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/validation/warn-on-undeclared-elemdef
     * </pre>
     *
     * @param warn True to warn; false to not warn.
     *
     * @see #getValidationWarnOnUndeclaredElemdef
     * @see #setFeature
     */
    protected void setValidationWarnOnUndeclaredElemdef(boolean warn)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fValidator.setWarningOnUndeclaredElements(warn);
    }

    /**
     * Returns true if the parser emits an error when an undeclared
     * element is referenced in the grammar.
     *
     * @see #setValidationWarnOnUndeclaredElemdef
     */
    protected boolean getValidationWarnOnUndeclaredElemdef()
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fValidator.getWarningOnUndeclaredElements();
    }

    /**
     * Allows the use of Java encoding names in the XMLDecl and TextDecl
     * lines in an XML document.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/allow-java-encodings
     * </pre>
     *
     * @param allow True to allow Java encoding names; false to disallow.
     *
     * @see #getAllowJavaEncodings
     * @see #setFeature
     */
    protected void setAllowJavaEncodings(boolean allow) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fEntityHandler.setAllowJavaEncodings(allow);
    }

    /**
     * Returns true if Java encoding names are allowed in the XML document.
     *
     * @see #setAllowJavaEncodings
     */
    protected boolean getAllowJavaEncodings() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fEntityHandler.getAllowJavaEncodings();
    }

    /**
     * Allows the parser to continue after a fatal error. Normally, a
     * fatal error would stop the parse.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://apache.org/xml/features/continue-after-fatal-error
     * </pre>
     *
     * @param continueAfterFatalError True to continue; false to stop on
     *                                fatal error.
     *
     * @see #getContinueAfterFatalError
     * @see #setFeature
     */
    protected void setContinueAfterFatalError(boolean continueAfterFatalError)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fContinueAfterFatalError = continueAfterFatalError;
    }

    /**
     * Returns true if the parser continues after a fatal error.
     *
     * @see #setContinueAfterFatalError
     */
    protected boolean getContinueAfterFatalError() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fContinueAfterFatalError;
    }

    // SAX2 core properties

    /**
     * Set the separator to be used between the URI part of a name and the
     * local part of a name when namespace processing is being performed
     * (see the http://xml.org/sax/features/namespaces feature).  By default,
     * the separator is a single space.
     * <p>
     * This property may not be set while a parse is in progress (throws a
     * SAXNotSupportedException).
     * <p>
     * This method is the equivalent to the property:
     * <pre>
     * http://xml.org/sax/properties/namespace-sep
     * </pre>
     *
     * @param separator The new namespace separator.
     *
     * @see #getNamespaceSep
     * @see #setProperty
     */
    /***
    protected void setNamespaceSep(String separator) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        // REVISIT: Ask someone what it could possibly hurt to allow
        //          the application to change this in mid-parse.
        if (fParseInProgress) {
            throw new SAXNotSupportedException("http://xml.org/sax/properties/namespace-sep: parse is in progress");
        }
        fNamespaceSep = separator;
    }
    /***/

    /**
     * Returns the namespace separator.
     *
     * @see #setNamespaceSep
     */
    /***
    protected String getNamespaceSep() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fNamespaceSep;
    }
    /***/

    /**
     * This method is the equivalent to the property:
     * <pre>
     * http://xml.org/sax/properties/xml-string
     * </pre>
     *
     * @see #getProperty
     */
    protected String getXMLString() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotSupportedException("http://xml.org/sax/properties/xml-string");
    }

    // resetting

    /**
     * Reset or copy parser
     * Allows parser instance reuse
     */
    protected void resetOrCopy() throws Exception {
        fStringPool = new StringPool();
        fEntityHandler.reset(fStringPool);
        fScanner.reset(fStringPool, new ChunkyCharArray(fStringPool));
        fValidator.resetOrCopy(fStringPool);
        fNeedReset = false;
        fGrammarResolver = new GrammarResolverImpl();
        fGrammarResolver.clearGrammarResolver();
        fScanner.setGrammarResolver(fGrammarResolver);
        fValidator.setGrammarResolver(fGrammarResolver);
    }

    //
    // Parser/XMLReader methods
    //
    // NOTE: This class does *not* implement the org.xml.sax.Parser
    //       interface but it does share some common methods. -Ac

    // handlers

    /**
     * Sets the resolver used to resolve external entities. The EntityResolver
     * interface supports resolution of public and system identifiers.
     *
     * @param resolver The new entity resolver. Passing a null value will
     *                 uninstall the currently installed resolver.
     */
    public void setEntityResolver(EntityResolver resolver) {
        fEntityHandler.setEntityResolver(resolver);
    }

    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none
     *         has been registered.
     * @see #setEntityResolver
     */
    public EntityResolver getEntityResolver() {
        return fEntityHandler.getEntityResolver();
    }

    /**
     * Sets the error handler.
     *
     * @param handler The new error handler.
     */
    public void setErrorHandler(ErrorHandler handler) {
        fErrorHandler = handler;
    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none
     *         has been registered.
     * @see #setErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return fErrorHandler;
    }

    // parsing

    /**
     * Parses the specified input source.
     *
     * @param source The input source.
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(InputSource source)
        throws SAXException, IOException {

        if (fParseInProgress) {
            throw new org.xml.sax.SAXException("FWK005 parse may not be called while parsing."); // REVISIT - need to add new error message
        }

        try {
            if (parseSomeSetup(source)) {
                fScanner.parseSome(true);
            }
        } catch (org.xml.sax.SAXException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new org.xml.sax.SAXException(ex);
        }
        finally {
            fParseInProgress = false;
        }

    } // parse(InputSource)

    /**
     * Parses the input source specified by the given system identifier.
     * <p>
     * This method is <em>almost</em> equivalent to the following:
     * <pre>
     *     parse(new InputSource(systemId));
     * </pre>
     * The only difference is that this method will attempt to close
     * the stream that was opened.
     *
     * @param source The input source.
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void parse(String systemId)
        throws SAXException, IOException {

        InputSource source = new InputSource(systemId);
        try {
            parse(source);
        }
        finally {
            // NOTE: Changed code to attempt to close the stream
            //       even after parsing failure. -Ac
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

    } // parse(String)

    // locale

    /**
     * Set the locale to use for messages.
     *
     * @param locale The locale object to use for localization of messages.
     *
     * @exception SAXException An exception thrown if the parser does not
     *                         support the specified locale.
     *
     * @see org.xml.sax.Parser
     */
    public void setLocale(Locale locale) throws SAXException {

        if (fParseInProgress) {
            throw new org.xml.sax.SAXException("FWK006 setLocale may not be called while parsing"); // REVISIT - need to add new error message
        }

        fLocale = locale;
        fgXMLMessages.setLocale(locale);
        fgImplementationMessages.setLocale(locale);

    } // setLocale(Locale)

    //
    // XMLErrorReporter methods
    //

    /**
     * Report an error.
     *
     * @param locator Location of error.
     * @param errorDomain The error domain.
     * @param majorCode The major code of the error.
     * @param minorCode The minor code of the error.
     * @param args Arguments for replacement text.
     * @param errorType The type of the error.
     *
     * @exception Exception Thrown on error.
     *
     * @see XMLErrorReporter#ERRORTYPE_WARNING
     * @see XMLErrorReporter#ERRORTYPE_FATAL_ERROR
     */
    public void reportError(Locator locator, String errorDomain,
                            int majorCode, int minorCode, Object args[],
                            int errorType) throws Exception {

        // create the appropriate message
        SAXParseException spe;
        if (errorDomain.equals(XMLMessages.XML_DOMAIN)) {
            spe = new SAXParseException(fgXMLMessages.createMessage(fLocale, majorCode, minorCode, args), locator);
        }
        else if (errorDomain.equals(XMLMessages.XMLNS_DOMAIN)) {
            spe = new SAXParseException(fgXMLMessages.createMessage(fLocale, majorCode, minorCode, args), locator);
        }
        else if (errorDomain.equals(ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN)) {
            spe = new SAXParseException(fgImplementationMessages.createMessage(fLocale, majorCode, minorCode, args), locator);
        } else if (errorDomain.equals(SchemaMessageProvider.SCHEMA_DOMAIN)) {
            spe = new SAXParseException(fgSchemaMessages.createMessage(fLocale, majorCode, minorCode, args), locator);
        } else if (errorDomain.equals(DatatypeMessageProvider.DATATYPE_DOMAIN)) {
            spe = new SAXParseException(fgDatatypeMessages.createMessage(fLocale, majorCode, minorCode, args), locator);
        } else {
            throw new RuntimeException("FWK007 Unknown error domain \"" + errorDomain + "\"."+"\n"+errorDomain);
        }

        // default error handling
        if (fErrorHandler == null) {
            if (errorType == XMLErrorReporter.ERRORTYPE_FATAL_ERROR &&
                !fContinueAfterFatalError) {
                throw spe;
            }
            return;
        }

        // make appropriate callback
        if (errorType == XMLErrorReporter.ERRORTYPE_WARNING) {
            fErrorHandler.warning(spe);
        }
        else if (errorType == XMLErrorReporter.ERRORTYPE_FATAL_ERROR) {
            fErrorHandler.fatalError(spe);
            if (!fContinueAfterFatalError) {
                Object[] fatalArgs = { spe.getMessage() };
                throw new SAXException(fgImplementationMessages.createMessage(fLocale, ImplementationMessages.FATAL_ERROR, 0, fatalArgs));
            }
        }
        else {
            fErrorHandler.error(spe);
        }

    } // reportError(Locator,String,int,int,Object[],int)

    //
    // XMLReader methods
    //

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

        //
        // SAX2 Features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            String feature = featureId.substring(SAX2_FEATURES_PREFIX.length());
            //
            // http://xml.org/sax/features/validation
            //   Validate (true) or don't validate (false).
            //
            if (feature.equals("validation")) {
                setValidation(state);
                return;
            }
            //
            // http://xml.org/sax/features/external-general-entities
            //   Expand external general entities (true) or don't expand (false).
            //
            if (feature.equals("external-general-entities")) {
                setExternalGeneralEntities(state);
                return;
            }
            //
            // http://xml.org/sax/features/external-parameter-entities
            //   Expand external parameter entities (true) or don't expand (false).
            //
            if (feature.equals("external-parameter-entities")) {
                setExternalParameterEntities(state);
                return;
            }
            //
            // http://xml.org/sax/features/namespaces
            //   Preprocess namespaces (true) or don't preprocess (false).  See also
            //   the http://xml.org/sax/properties/namespace-sep property.
            //
            if (feature.equals("namespaces")) {
                setNamespaces(state);
                return;
            }
            //
            // Not recognized
            //
        }

        //
        // Xerces Features
        //

        else if (featureId.startsWith(XERCES_FEATURES_PREFIX)) {
            String feature = featureId.substring(XERCES_FEATURES_PREFIX.length());
            //
            // http://apache.org/xml/features/validation/schema
            //   Lets the user turn Schema validation support on/off.
            //
            if (feature.equals("validation/schema")) {
                setValidationSchema(state);
                return;
            }
            //
            // http://apache.org/xml/features/validation/dynamic
            //   Allows the parser to validate a document only when it
            //   contains a grammar. Validation is turned on/off based
            //   on each document instance, automatically.
            //
            if (feature.equals("validation/dynamic")) {
                setValidationDynamic(state);
                return;
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals("validation/default-attribute-values")) {
                // REVISIT
                throw new SAXNotSupportedException(featureId);
            }
            //
            // http://apache.org/xml/features/validation/normalize-attribute-values
            //
            if (feature.equals("validation/normalize-attribute-values")) {
                setNormalizeAttributeValues(state);
            }
            //
            // http://apache.org/xml/features/validation/validate-content-models
            //
            if (feature.equals("validation/validate-content-models")) {
                // REVISIT
                throw new SAXNotSupportedException(featureId);
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-dtd-grammar
            //
            if (feature.equals("nonvalidating/load-dtd-grammar")) {
                setLoadDTDGrammar(state);
                return;
            }
            //
            // http://apache.org/xml/features/validation/nonvalidating/load-external-dtd
            //
            if (feature.equals("nonvalidating/load-external-dtd")) {
                setLoadExternalDTD(state);
                return;
            }

            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals("validation/validate-datatypes")) {
                // REVISIT
                throw new SAXNotSupportedException(featureId);
            }
            //
            // http://apache.org/xml/features/validation/warn-on-duplicate-attdef
            //   Emits an error when an attribute is redefined.
            //
            if (feature.equals("validation/warn-on-duplicate-attdef")) {
                setValidationWarnOnDuplicateAttdef(state);
                return;
            }
            //
            // http://apache.org/xml/features/validation/warn-on-undeclared-elemdef
            //   Emits an error when an element's content model
            //   references an element, by name, that is not declared
            //   in the grammar.
            //
            if (feature.equals("validation/warn-on-undeclared-elemdef")) {
                setValidationWarnOnUndeclaredElemdef(state);
                return;
            }
            //
            // http://apache.org/xml/features/allow-java-encodings
            //   Allows the use of Java encoding names in the XML
            //   and TextDecl lines.
            //
            if (feature.equals("allow-java-encodings")) {
                setAllowJavaEncodings(state);
                return;
            }
            //
            // http://apache.org/xml/features/continue-after-fatal-error
            //   Allows the parser to continue after a fatal error.
            //   Normally, a fatal error would stop the parse.
            //
            if (feature.equals("continue-after-fatal-error")) {
                setContinueAfterFatalError(state);
                return;
            }
            //
            // Not recognized
            //
        }

        //
        // Not recognized
        //

        throw new SAXNotRecognizedException(featureId);

    } // setFeature(String,boolean)

    /**
     * Query the state of a feature.
     *
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public boolean getFeature(String featureId) 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 Features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            String feature = featureId.substring(SAX2_FEATURES_PREFIX.length());
            //
            // http://xml.org/sax/features/validation
            //   Validate (true) or don't validate (false).
            //
            if (feature.equals("validation")) {
                return getValidation();
            }
            //
            // http://xml.org/sax/features/external-general-entities
            //   Expand external general entities (true) or don't expand (false).
            //
            if (feature.equals("external-general-entities")) {
                return getExternalGeneralEntities();
            }
            //
            // http://xml.org/sax/features/external-parameter-entities
            //   Expand external parameter entities (true) or don't expand (false).
            //
            if (feature.equals("external-parameter-entities")) {
                return getExternalParameterEntities();
            }
            //
            // http://xml.org/sax/features/namespaces
            //   Preprocess namespaces (true) or don't preprocess (false).  See also
            //   the http://xml.org/sax/properties/namespace-sep property.
            //
            if (feature.equals("namespaces")) {
                return getNamespaces();
            }
            //
            // Not recognized
            //
        }

        //
        // Xerces Features
        //

        else if (featureId.startsWith(XERCES_FEATURES_PREFIX)) {
            String feature = featureId.substring(XERCES_FEATURES_PREFIX.length());
            //
            // http://apache.org/xml/features/validation/schema
            //   Lets the user turn Schema validation support on/off.
            //
            if (feature.equals("validation/schema")) {
                return getValidationSchema();
            }
            //
            // http://apache.org/xml/features/validation/dynamic
            //   Allows the parser to validate a document only when it
            //   contains a grammar. Validation is turned on/off based
            //   on each document instance, automatically.
            //
            if (feature.equals("validation/dynamic")) {
                return getValidationDynamic();
            }
            //
            // http://apache.org/xml/features/validation/default-attribute-values
            //
            if (feature.equals("validation/default-attribute-values")) {
                // REVISIT
                throw new SAXNotRecognizedException(featureId);
            }
            //
            // http://apache.org/xml/features/validation/validate-content-models
            //
            if (feature.equals("validation/validate-content-models")) {
                // REVISIT
                throw new SAXNotRecognizedException(featureId);
            }
            //
            // http://apache.org/xml/features/nonvalidating/load-dtd-grammar
            //
            if (feature.equals("nonvalidating/load-dtd-grammar")) {
                return getLoadDTDGrammar();
            }
            //
            // http://apache.org/xml/features/nonvalidating/load-external-dtd
            //
            if (feature.equals("nonvalidating/load-external-dtd")) {
                return getLoadExternalDTD();
            }
            //
            // http://apache.org/xml/features/validation/validate-datatypes
            //
            if (feature.equals("validation/validate-datatypes")) {
                // REVISIT
                throw new SAXNotRecognizedException(featureId);
            }
            //
            // http://apache.org/xml/features/validation/warn-on-duplicate-attdef
            //   Emits an error when an attribute is redefined.
            //
            if (feature.equals("validation/warn-on-duplicate-attdef")) {
                return getValidationWarnOnDuplicateAttdef();
            }
            //
            // http://apache.org/xml/features/validation/warn-on-undeclared-elemdef
            //   Emits an error when an element's content model
            //   references an element, by name, that is not declared
            //   in the grammar.
            //
            if (feature.equals("validation/warn-on-undeclared-elemdef")) {
                return getValidationWarnOnUndeclaredElemdef();
            }
            //
            // http://apache.org/xml/features/allow-java-encodings
            //   Allows the use of Java encoding names in the XML
            //   and TextDecl lines.
            //
            if (feature.equals("allow-java-encodings")) {
                return getAllowJavaEncodings();
            }
            //
            // http://apache.org/xml/features/continue-after-fatal-error
            //   Allows the parser to continue after a fatal error.
            //   Normally, a fatal error would stop the parse.
            //
            if (feature.equals("continue-after-fatal-error")) {
                return getContinueAfterFatalError();
            }
            //
            // Not recognized
            //
        }

        //
        // Not recognized
        //

        throw new SAXNotRecognizedException(featureId);

    } // getFeature(String):boolean

    /**
     * Set the value of a property.
     *
     * Set the value of any property in a SAX2 parser.  The parser
     * might not recognize the property, and if it does recognize
     * it, it might not support the requested value.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @param Object The value to which the property is being set.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 Properties
        //

        if (propertyId.startsWith(SAX2_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(SAX2_PROPERTIES_PREFIX.length());
            //
            // http://xml.org/sax/properties/namespace-sep
            // Value type: String
            // Access: read/write, pre-parse only
            //   Set the separator to be used between the URI part of a name and the
            //   local part of a name when namespace processing is being performed
            //   (see the http://xml.org/sax/features/namespaces feature).  By
            //   default, the separator is a single space.  This property may not be
            //   set while a parse is in progress (throws a SAXNotSupportedException).
            //
            /***
            if (property.equals("namespace-sep")) {
                try {
                    setNamespaceSep((String)value);
                }
                catch (ClassCastException e) {
                    throw new SAXNotSupportedException(propertyId);
                }
                return;
            }
            /***/
            
            //
            // http://xml.org/sax/properties/xml-string
            // Value type: String
            // Access: read-only
            //   Get the literal string of characters associated with the current
            //   event.  If the parser recognises and supports this property but is
            //   not currently parsing text, it should return null (this is a good
            //   way to check for availability before the parse begins).
            //
            if (property.equals("xml-string")) {
                // REVISIT - we should probably ask xml-dev for a precise definition
                // of what this is actually supposed to return, and in exactly which
                // circumstances.
                throw new SAXNotSupportedException(propertyId);
            }
            //
            // Not recognized
            //
        }

        //
        // Xerces Properties
        //

        /*
        else if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            //
            // No properties defined yet that are common to all parsers.
            //
        }
        */

        //
        // Not recognized
        //

        throw new SAXNotRecognizedException(propertyId);

    } // setProperty(String,Object)

    /**
     * Query the value of a property.
     *
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     * @see org.xml.sax.XMLReader#getProperty
     */
    public Object getProperty(String propertyId) 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 Properties
        //

        if (propertyId.startsWith(SAX2_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(SAX2_PROPERTIES_PREFIX.length());
            //
            // http://xml.org/sax/properties/namespace-sep
            // Value type: String
            // Access: read/write, pre-parse only
            //   Set the separator to be used between the URI part of a name and the
            //   local part of a name when namespace processing is being performed
            //   (see the http://xml.org/sax/features/namespaces feature).  By
            //   default, the separator is a single space.  This property may not be
            //   set while a parse is in progress (throws a SAXNotSupportedException).
            //
            /***
            if (property.equals("namespace-sep")) {
                return getNamespaceSep();
            }
            /***/
            //
            // http://xml.org/sax/properties/xml-string
            // Value type: String
            // Access: read-only
            //   Get the literal string of characters associated with the current
            //   event.  If the parser recognises and supports this property but is
            //   not currently parsing text, it should return null (this is a good
            //   way to check for availability before the parse begins).
            //
            if (property.equals("xml-string")) {
                return getXMLString();
            }
            //
            // Not recognized
            //
        }

        //
        // Xerces Properties
        //

        /*
        else if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            //
            // No properties defined yet that are common to all parsers.
            //
        }
        */

        //
        // Not recognized
        //

        throw new SAXNotRecognizedException(propertyId);

    } // getProperty(String):Object

}
