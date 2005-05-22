/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.jaxp.validation;

import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import org.apache.xerces.util.SAXMessageFormatter;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * <p>Implementation of Validator for W3C XML Schemas.</p>
 *
 * @author <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class ValidatorImpl extends Validator {
    
    //
    // Data
    //
    
    /** Component manager. **/
    private XMLSchemaValidatorComponentManager fComponentManager;
    
    /**
     * TODO: Need to merge in SAX.
     */
    private ValidatorHandler handler = null;
    
    /** DOM validator helper. **/
    private DOMValidatorHelper fDOMValidatorHelper;
    
    /** Stream validator helper. **/
    private StreamValidatorHelper fStreamValidatorHelper;
    
    /** Flag for tracking whether features/properties changed since last reset. */
    private boolean fConfigurationChanged = false;
    
    /** Flag for tracking whether the error handler changed since last reset. */
    private boolean fErrorHandlerChanged = false;
    
    /** Flag for tracking whether the resource resolver changed since last reset. */
    private boolean fResourceResolverChanged = false;
    
    public ValidatorImpl(XSGrammarPoolContainer grammarContainer) {
        fComponentManager = new XMLSchemaValidatorComponentManager(grammarContainer);
        setErrorHandler(null);
        setResourceResolver(null);
    }

    public void validate(Source source, Result result)
        throws SAXException, IOException {
        if (source instanceof SAXSource) {
            // TODO: Hand off to SAX validator helper.
            process((SAXSource) source, (SAXResult) result);
        }
        else if (source instanceof DOMSource) {
            // Hand off to DOM validator helper.
            if (fDOMValidatorHelper == null) {
                fDOMValidatorHelper = new DOMValidatorHelper(fComponentManager);
            }
            fDOMValidatorHelper.validate(source, result);
        }
        else if (source instanceof StreamSource) {
            // Hand off to stream validator helper.
            if (fStreamValidatorHelper == null) {
                fStreamValidatorHelper = new StreamValidatorHelper(fComponentManager);
            }
            fStreamValidatorHelper.validate(source, result);
        }
        // Source parameter cannot be null.
        else if (source == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                    "SourceParameterNull", null));
        }
        // Source parameter must be a SAXSource, DOMSource or StreamSource
        else {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                    "SourceNotAccepted", new Object [] {source.getClass().getName()}));
        }
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        fErrorHandlerChanged = (errorHandler != null);
        fComponentManager.setErrorHandler(errorHandler);
    }

    public ErrorHandler getErrorHandler() {
        return fComponentManager.getErrorHandler();
    }

    public void setResourceResolver(LSResourceResolver resourceResolver) {
        fResourceResolverChanged = (resourceResolver != null);
        fComponentManager.setResourceResolver(resourceResolver);
    }

    public LSResourceResolver getResourceResolver() {
        return fComponentManager.getResourceResolver();
    }
    
    public boolean getFeature(String name) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        try {
            return fComponentManager.getFeature(name);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            final String key = e.getType() == XMLConfigurationException.NOT_RECOGNIZED ?
                    "feature-not-recognized" : "feature-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(Locale.getDefault(), 
                    key, new Object [] {identifier}));
        }
    }
    
    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        try {
            fComponentManager.setFeature(name, value);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            final String key = e.getType() == XMLConfigurationException.NOT_RECOGNIZED ?
                    "feature-not-recognized" : "feature-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(Locale.getDefault(), 
                    key, new Object [] {identifier}));
        }
        fConfigurationChanged = true;
    }
    
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        try {
            return fComponentManager.getProperty(name);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            final String key = e.getType() == XMLConfigurationException.NOT_RECOGNIZED ?
                    "property-not-recognized" : "property-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(Locale.getDefault(), 
                    key, new Object [] {identifier}));
        }
    }
    
    public void setProperty(String name, Object object)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        try {
            fComponentManager.setProperty(name, object);
        }
        catch (XMLConfigurationException e) {
            final String identifier = e.getIdentifier();
            final String key = e.getType() == XMLConfigurationException.NOT_RECOGNIZED ?
                    "property-not-recognized" : "property-not-supported";
            throw new SAXNotRecognizedException(
                    SAXMessageFormatter.formatMessage(Locale.getDefault(), 
                    key, new Object [] {identifier}));
        }
        fConfigurationChanged = true;
    }
    
    /**
     * Parses a {@link SAXSource} potentially to a {@link SAXResult}.
     */
    private void process(SAXSource source, SAXResult result) throws IOException, SAXException {
        if( result!=null ) {
            handler.setContentHandler(result.getHandler());
        }
        
        try {
            XMLReader reader = source.getXMLReader();
            if( reader==null ) {
                // create one now
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                try {
                    reader = spf.newSAXParser().getXMLReader();
                } catch( Exception e ) {
                    // this is impossible, but better safe than sorry
                    throw new FactoryConfigurationError(e);
                }
            }
            
            reader.setErrorHandler(errorForwarder);
            reader.setEntityResolver(resolutionForwarder);
            reader.setContentHandler(handler);
            
            InputSource is = source.getInputSource();
            reader.parse(is);
        } finally {
            // release the reference to user's handler ASAP
            handler.setContentHandler(null);
        }
    }
    
    /**
     * Forwards the error to the {@link ValidatorHandler}.
     * If the {@link ValidatorHandler} doesn't have its own
     * {@link ErrorHandler}, behave draconian.
     */
    private final ErrorHandler errorForwarder = new ErrorHandler() {
        public void warning(SAXParseException exception) throws SAXException {
            ErrorHandler realHandler = handler.getErrorHandler();
            if( realHandler!=null )
                realHandler.warning(exception);
        }
        
        public void error(SAXParseException exception) throws SAXException {
            ErrorHandler realHandler = handler.getErrorHandler();
            if( realHandler!=null )
                realHandler.error(exception);
            else
                throw exception;
        }
        
        public void fatalError(SAXParseException exception) throws SAXException {
            ErrorHandler realHandler = handler.getErrorHandler();
            if( realHandler!=null )
                realHandler.fatalError(exception);
            else
                throw exception;
        }
    };
    
    /**
     * Forwards the entity resolution to the {@link ValidatorHandler}.
     * If the {@link ValidatorHandler} doesn't have its own
     * {@link DOMResourceResolver}, let the parser do the resolution.
     */
    private final EntityResolver resolutionForwarder = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            LSResourceResolver resolver = handler.getResourceResolver();
            if( resolver==null )    return null;
            
            LSInput di = resolver.resolveResource(null,null,publicId,systemId,null);
            if(di==null)    return null;
            
            InputSource r = new InputSource();
            r.setByteStream(di.getByteStream());
            r.setCharacterStream(di.getCharacterStream());
            r.setEncoding(di.getEncoding());
            r.setPublicId(di.getPublicId());
            r.setSystemId(di.getSystemId());
            return r;
        }
    };
    
    public void reset() {
        // avoid resetting features and properties if the state the validator
        // is currently in, is the same as it will be after reset.
        if (fConfigurationChanged) {
            fComponentManager.restoreInitialState();
            setErrorHandler(null);
            setResourceResolver(null);
            fConfigurationChanged = false;
            fErrorHandlerChanged = false;
            fResourceResolverChanged = false;
        }
        else {
            if (fErrorHandlerChanged) {
                setErrorHandler(null);
                fErrorHandlerChanged = false;
            }
            if (fResourceResolverChanged) {
                setResourceResolver(null);
                fResourceResolverChanged = false;
            }
        }
    }
    
} // ValidatorImpl
