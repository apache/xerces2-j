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
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.jaxp.validation.ReadOnlyGrammarPool;
import org.apache.xerces.jaxp.validation.Util;
import org.apache.xerces.util.DOMEntityResolverWrapper;
import org.apache.xerces.util.DOMInputSource;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.SAXInputSource;
import org.apache.xerces.util.SAXMessageFormatter;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.Node;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.apache.xerces.util.SecurityManager;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * {@link SchemaFactory} for XML Schema.
 *
 * @author Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 * @version $Id$
 */
public class XMLSchemaFactory extends SchemaFactory {
    
    // property identifiers
    
    /** Feature identifier: schema full checking. */
    private static final String SCHEMA_FULL_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
    
    /** Property identifier: grammar pool. */
    private static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    
    /** Property identifier: SecurityManager. */
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    
    private final XMLSchemaLoader loader = new XMLSchemaLoader();
    private static XSMessageFormatter messageFormatter = new XSMessageFormatter();
    /**
     * User-specified ErrorHandler. can be null.
     */
    private ErrorHandler errorHandler;
    
    private LSResourceResolver resourceResolver;
    
    private SAXParseException lastException;
    
    private final SecurityManager secureProcessing ;
    
    private boolean enableSP;
    
    public XMLSchemaFactory() {
        secureProcessing = new SecurityManager();
        // intercept error report and remember the last thrown exception.
        loader.setErrorHandler(new ErrorHandlerWrapper(new ErrorHandler() {
            public void warning(SAXParseException exception) throws SAXException {
                if( errorHandler!=null )    errorHandler.warning(exception);
            }
            
            public void error(SAXParseException exception) throws SAXException {
                lastException = exception;
                if( errorHandler!=null )    errorHandler.error(exception);
                else    throw exception;
            }
            
            public void fatalError(SAXParseException exception) throws SAXException {
                lastException = exception;
                if( errorHandler!=null )    errorHandler.fatalError(exception);
                else    throw exception;
            }
        }));
    }
    
    
    /**
     * <p>Is specified schema supported by this <code>SchemaFactory</code>?</p>
     *
     * @param schemaLanguage Specifies the schema language which the returned <code>SchemaFactory</code> will understand.
     *    <code>schemaLanguage</code> must specify a <a href="#schemaLanguage">valid</a> schema language.
     *
     * @return <code>true</code> if <code>SchemaFactory</code> supports <code>schemaLanguage</code>, else <code>false</code>.
     *
     * @throws NullPointerException If <code>schemaLanguage</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>schemaLanguage.length() == 0</code>
     *   or <code>schemaLanguage</code> does not specify a <a href="#schemaLanguage">valid</a> schema language.
     */
    public boolean isSchemaLanguageSupported(String schemaLanguage) {
        
        if (schemaLanguage == null) {
            throw new NullPointerException(
            messageFormatter.formatMessage(Locale.getDefault(),
            "SchemaLanguageSupportedErrorWhenNull",
            new Object [] {this.getClass().getName()}));
        }
        
        if (schemaLanguage.length() == 0) {
            throw new IllegalArgumentException(
            messageFormatter.formatMessage(Locale.getDefault(),
            "SchemaLanguageSupportedErrorWhenLength",
            new Object [] {this.getClass().getName()}));
        }
        
        // understand W3C Schema and RELAX NG
        if (schemaLanguage.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        || schemaLanguage.equals(XMLConstants.RELAXNG_NS_URI)) {
            return true;
        }
        
        // don't know how to validate anything else
        return false;
    }
    
    public LSResourceResolver getResourceResolver() {
        return resourceResolver;
    }
    
    public void setResourceResolver(LSResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        loader.setEntityResolver(new DOMEntityResolverWrapper(resourceResolver));
    }
    
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    
    
    public Schema newSchema( Source[] schemas ) throws SAXException {
        
        lastException = null;
        
        // this will let the loader store parsed Grammars into the pool.
        XMLGrammarPool pool = new XMLGrammarPoolImpl();
        loader.setProperty(XMLGRAMMAR_POOL, pool);
        loader.setFeature(SCHEMA_FULL_CHECKING, true);
        if(enableSP)
            loader.setProperty(SECURITY_MANAGER, secureProcessing);
        else
            loader.setProperty(SECURITY_MANAGER, null);
        
        XMLInputSource[] xmlInputSources = new XMLInputSource[schemas.length];
        InputStream inputStream;
        Reader reader;
        for( int i=0; i<schemas.length; i++ ) {
            Source source = schemas[i];
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                String publicId = streamSource.getPublicId();
                String systemId = streamSource.getSystemId();
                inputStream = streamSource.getInputStream();
                reader = streamSource.getReader();
                xmlInputSources[i] = new XMLInputSource(publicId, systemId, null);
                xmlInputSources[i].setByteStream(inputStream);
                xmlInputSources[i].setCharacterStream(reader);
            }
            else if (source instanceof SAXSource) {
                SAXSource saxSource = (SAXSource) source;
                InputSource inputSource = saxSource.getInputSource();
                if (inputSource == null) {
                    throw new SAXException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                            "SAXSourceNullInputSource", null));
                }
                xmlInputSources[i] = new SAXInputSource(saxSource.getXMLReader(), inputSource);
            }
            else if (source instanceof DOMSource) {
                DOMSource domSource = (DOMSource) source;
                Node node = domSource.getNode();
                String systemID = domSource.getSystemId();          
                xmlInputSources[i] = new DOMInputSource(node, systemID);
            }
            else if (source == null) {
                throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                        "SchemaSourceArrayMemberNull", null));
            }
            else {
                throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                        "SchemaFactorySourceUnrecognized", 
                        new Object [] {source.getClass().getName()}));
            }
            
            try {
                loader.loadGrammar(xmlInputSources);
            } catch (XNIException e) {
                // this should have been reported to users already.
                throw Util.toSAXException(e);
            } catch (IOException e) {
                // this hasn't been reported, so do so now.
                SAXParseException se = new SAXParseException(e.getMessage(),null,e);
                errorHandler.error(se);
                throw se; // and we must throw it.
            }
        }
        
        // if any error had been reported, throw it.
        if( lastException!=null )
            throw lastException;
        
        // make sure no further grammars are added by making it read-only.
        return new XMLSchema(new ReadOnlyGrammarPool(pool));
    }
    
    public Schema newSchema() throws SAXException {
        // use a pool that uses the system id as the equality source.
        return new XMLSchema(new XMLGrammarPoolImpl() {
            public boolean equals(XMLGrammarDescription desc1, XMLGrammarDescription desc2) {
                String sid1 = desc1.getExpandedSystemId();
                String sid2 = desc2.getExpandedSystemId();
                if( sid1!=null && sid2!=null )
                    return sid1.equals(sid2);
                if( sid1==null && sid2==null )
                    return true;
                return false;
            }
            public int hashCode(XMLGrammarDescription desc) {
                String s = desc.getExpandedSystemId();
                if(s!=null)     return s.hashCode();
                return 0;
            }
        });
    }
    
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name==null) throw new NullPointerException(SAXMessageFormatter.formatMessage(Locale.getDefault(),
        "nullparameter",new Object[] {"setFeature(String,boolean)"}));
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)){
            enableSP = value;
        }else throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(Locale.getDefault(),
        "feature-not-supported", new Object [] {name}));
        
    }
    
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(name==null) throw new NullPointerException(SAXMessageFormatter.formatMessage(Locale.getDefault(),
        "nullparameter",new Object[] {"getFeature(String)"}));
        if(name.equals(XMLConstants.FEATURE_SECURE_PROCESSING))
            return enableSP;
        else throw new SAXNotRecognizedException(SAXMessageFormatter.formatMessage(Locale.getDefault(),
        "feature-not-supported", new Object [] {name}));
    }
}
