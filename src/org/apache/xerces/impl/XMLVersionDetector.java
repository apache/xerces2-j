/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  
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

package org.apache.xerces.impl;

import java.io.EOFException;
import java.io.IOException;

import org.apache.xerces.impl.XMLEntityScanner;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.dtd.XML11DTDValidator;
import org.apache.xerces.impl.dtd.XMLDTDProcessor;
import org.apache.xerces.impl.dtd.XML11DTDProcessor;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDTDFilter;
import org.apache.xerces.xni.parser.XMLDTDContentModelFilter;

/**
 * This is not a full-function scanner; its sole job
 * is to figure out the version of the document we're
 * scanning, and create the correct
 * document scanner, DTD scanner and entity scanner to deal with it.  
 * Any configuration that may parse documents from different versions of XML should
 * place this object first in both the maqin and DTD pipelines, and take care
 * to reset it in that position when it initiates
 * a new parse.  
 * 
 * @author Neil Graham, IBM
 * @version $Id$
 */

public class XMLVersionDetector
    implements XMLComponent, XMLDocumentScanner, XMLDTDScanner {

    //
    // Constants
    //

    private final static char[] XML11_VERSION = new char[]{'1', '.', '1'};
    private final static char [] EXPECTED_VERSION_STRING = {'<', '?', 'x', 'm', 'l', ' ', 'v', 'e', 'r', 's', 
                    'i', 'o', 'n', '=', ' ', ' ', ' ', ' ', ' '};

    // this class doesn't do anything that would affect events
    // further down the pipeline; therefore, it doesn't
    // recognize any features.

    // property identifiers

    /** Property identifier: symbol table. */
    protected static final String SYMBOL_TABLE = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: entity manager. */
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

    /** Property identifier: DTD scanner. */
    protected static final String DTD_SCANNER_PROPERTY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;

    /** Property identifier: DTD validator. */ 
    protected static final String DTD_VALIDATOR_PROPERTY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;

    /** Property identifier: DTD processor. */ 
    protected static final String DTD_PROCESSOR_PROPERTY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_PROCESSOR_PROPERTY;

    /** Property identifier: namespace binder. */ 
    protected static final String NAMESPACE_BINDER_PROPERTY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_BINDER_PROPERTY;

    // recognized properties
    private static final String[] RECOGNIZED_PROPERTIES = {
        SYMBOL_TABLE, 
        ERROR_REPORTER, 
        ENTITY_MANAGER,
        DTD_SCANNER_PROPERTY, 
        DTD_VALIDATOR_PROPERTY, 
        DTD_PROCESSOR_PROPERTY,
        NAMESPACE_BINDER_PROPERTY,
    };
    private static final String[] PROPERTY_DEFAULTS = {null, null, null, null, null, null, null};

    //
    // Data
    //

    // properties

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;

    /** Entity manager. */
    protected XMLEntityManager fEntityManager;

    // protected data

    // the current componentManager; so that we can use it to
    // properly reset any scanners we create
    protected XMLComponentManager fComponentManager = null;

    // next guy in the main pipeline
    protected XMLDocumentHandler fDocumentHandler = null;

    // next guy in the DTD pipeline
    protected XMLDTDHandler fDTDHandler = null;

    // next guy in the DTD content model pipeline
    protected XMLDTDContentModelHandler fDTDContentModelHandler = null;

    // the scanner that will actually scan the document
    protected XMLDocumentScannerImpl fRealDocumentScanner = null;

    // the scanner that will actually scan the DTD
    protected XMLDTDScannerImpl fRealDTDScanner = null;

    // the XML 1.0 document scanner
    protected XMLDocumentScannerImpl fXML10DocScanner = null;
    
    // the XML 1.0 DTD scanner
    protected XMLDTDScannerImpl fXML10DTDScanner = null;
    
    // the XML 1.1 document scanner
    protected XML11DocumentScannerImpl fXML11DocScanner = null;
    
    // the XML 1.1 DTD scanner
    protected XML11DTDScannerImpl fXML11DTDScanner = null;

    // the XML 1.1 DTD validator
    protected XML11DTDValidator fXML11DTDValidator = null;
    
    // the XML 1.1 DTD processor
    protected XML11DTDProcessor fXML11DTDProcessor = null;
    
    // the XML 1.1 namespace binder
    protected XML11NamespaceBinder fXML11NamespaceBinder = null;
    
    // symbols

    /** Symbol: "version". */
    protected final static String fVersionSymbol = "version".intern();

    // symbol:  [xml]:
    protected static final String fXMLSymbol = "[xml]".intern();

    // temporary variables
    private XMLString fVersionNum = new XMLString();

    //
    // XMLComponent methods
    //

    /**
     * 
     * 
     * @param componentManager The component manager.
     *
     * @throws SAXException Throws exception if required features and
     *                      properties cannot be found.
     */
    public void reset(XMLComponentManager componentManager)
        throws XMLConfigurationException {

        // Xerces properties
        fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        fEntityManager = (XMLEntityManager)componentManager.getProperty(ENTITY_MANAGER);
        
        // keep a reference around so we can correctly
        // initialize new scanners we need to instantiate
        fComponentManager = componentManager;
        for(int i=14; i<EXPECTED_VERSION_STRING.length; i++ )
            EXPECTED_VERSION_STRING[i] = ' ';
    } // reset(XMLComponentManager)

    /**
     * Sets the value of a property during parsing.
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        
        // Xerces properties
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String property =
               propertyId.substring(Constants.XERCES_PROPERTY_PREFIX.length());
            if (property.equals(Constants.SYMBOL_TABLE_PROPERTY)) {
                fSymbolTable = (SymbolTable)value;
            }
            else if (property.equals(Constants.ERROR_REPORTER_PROPERTY)) {
                fErrorReporter = (XMLErrorReporter)value;
            }
            else if (property.equals(Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (XMLEntityManager)value;
            }
        }

    } // setProperty(String,Object)

    /*
     * Sets the feature of the scanner.
     */
    public void setFeature(String featureId, boolean value)
        throws XMLConfigurationException {
    }
    
    /*
     * Gets the state of the feature of the scanner.
     */
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
            
        throw new XMLConfigurationException(XMLConfigurationException.NOT_RECOGNIZED, featureId);
    }

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return null;
    } // getRecognizedFeatures():String[]

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } // getRecognizedProperties():String[]

    /** 
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     *
     * @param featureId The feature identifier.
     *
     * @since Xerces 2.2.0
     */
    public Boolean getFeatureDefault(String featureId) {
        return null;
    } // getFeatureDefault(String):Boolean

    /** 
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property. 
     *
     * @param propertyId The property identifier.
     *
     * @since Xerces 2.2.0
     */
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    //
    // XMLDocumentScanner methods
    //

    // the setSource method.  We simply do what we
    // need to to the passed-in XMLResourceIdentifier
    // to open the document, determine a provisional
    // encoding (if necessary), and determine what version
    // it is.  Then we put the right scanner into the pipeline
    // and call the *real* scanDocument
    // method on the real scanner, which takes care of
    // business (that is, actually makes callbacks,
    // throws errors, etc.)
    // We finally clean up the mess we've made of 
    // the pipeline for the benefit of any unwary configuration
    
    /**
     * Sets the input source.
     *
     * @param inputSource The input source.
     *
     * @throws IOException Thrown on i/o error.
     */
    public void setInputSource(XMLInputSource inputSource) throws IOException {
        // create the entity for the document, recover its
        // encoding so that we can make the appropriate call to
        // initialize the right scanner
        String encoding = fEntityManager.setupCurrentEntity(fXMLSymbol, inputSource, false, true);
        // do what's needed to determine the version
        reinitializePipelines(determineDocVersion());
        // pass this call on to the real scanner
        fEntityManager.setEntityHandler(fRealDocumentScanner);
        fRealDocumentScanner.startEntity(fXMLSymbol, fEntityManager.getCurrentResourceIdentifier(), encoding);
    } // setInputSource(XMLInputSource)

    /** 
     * Scans a document.
     *
     * @param complete True if the scanner should scan the document
     *                 completely, pushing all events to the registered
     *                 document handler. A value of false indicates that
     *                 that the scanner should only scan the next portion
     *                 of the document and return. A scanner instance is
     *                 permitted to completely scan a document if it does
     *                 not support this "pull" scanning model.
     *
     * @returns True if there is more to scan, false otherwise.
     */
    public boolean scanDocument(boolean complete) 
        throws IOException, XNIException {

        // send this off to the right scanner
        return fRealDocumentScanner.scanDocument(complete);

        // REVISIT:  should we bother to return pipeline to its original state

    } // scanDocument(boolean):boolean

    //
    // XMLDocumentSource methods
    //

    /**
     * setDocumentHandler
     * 
     * @param documentHandler 
     */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)


    /** Returns the document handler */
    public XMLDocumentHandler getDocumentHandler(){
        return fDocumentHandler;
    }

    //
    // XMLDTDScanner methods 
    //

    /** Sets the DTD content model handler. */
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        fDTDContentModelHandler = handler;
    } 

    /** Returns the DTD content model handler. */
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return fDTDContentModelHandler;
    } 

    /** Sets the DTD handler. */
    public void setDTDHandler(XMLDTDHandler handler) {
        fDTDHandler = handler;
    }

    /** Returns the DTD handler. */
    public XMLDTDHandler getDTDHandler() {
        return fDTDHandler;
    }

    /** 
     * Scans the internal subset of the document.
     *
     * @param complete True if the scanner should scan the document
     *                 completely, pushing all events to the registered
     *                 document handler. A value of false indicates that
     *                 that the scanner should only scan the next portion
     *                 of the document and return. A scanner instance is
     *                 permitted to completely scan a document if it does
     *                 not support this "pull" scanning model.
     * @param standalone True if the document was specified as standalone.
     *                   This value is important for verifying certain
     *                   well-formedness constraints.
     * @param hasExternalDTD True if the document has an external DTD.
     *                       This allows the scanner to properly notify
     *                       the handler of the end of the DTD in the
     *                       absence of an external subset.
     *
     * @returns True if there is more to scan, false otherwise.
     */
    public boolean scanDTDInternalSubset(boolean complete, boolean standalone,
                                         boolean hasExternalSubset)
        throws IOException, XNIException {
        // place holder
        return false;
    }

    /**
     * Scans the external subset of the document.
     *
     * @param complete True if the scanner should scan the document
     *                 completely, pushing all events to the registered
     *                 document handler. A value of false indicates that
     *                 that the scanner should only scan the next portion
     *                 of the document and return. A scanner instance is
     *                 permitted to completely scan a document if it does
     *                 not support this "pull" scanning model.
     *
     * @returns True if there is more to scan, false otherwise.
     */
    public boolean scanDTDExternalSubset(boolean complete) 
        throws IOException, XNIException {
        // no-op place holder
        return false;
    }


    private short determineDocVersion() throws IOException {
        // must assume 1.1 at this stage so that whitespace
        // handling is correct in the XML decl...
        fEntityManager.setScannerVersion(Constants.XML_VERSION_1_1);
        XMLEntityScanner scanner = fEntityManager.getEntityScanner();
        if(!scanner.skipString("<?xml"))  {
            // definitely not a well-formed 1.1 doc!
            return Constants.XML_VERSION_1_0;
        }
        if(!scanner.skipSpaces()) {
            fixupCurrentEntity(fEntityManager, EXPECTED_VERSION_STRING, 5);
            return Constants.XML_VERSION_1_0;
        }
        if(!scanner.skipString("version"))  {
            fixupCurrentEntity(fEntityManager, EXPECTED_VERSION_STRING, 6);
            return Constants.XML_VERSION_1_0;
        }
        scanner.skipSpaces();
        if(scanner.scanChar() != '=') {
            fixupCurrentEntity(fEntityManager, EXPECTED_VERSION_STRING, 13);
            return Constants.XML_VERSION_1_0;
        }
        scanner.skipSpaces();
        int quoteChar = scanner.scanChar();
        EXPECTED_VERSION_STRING[14] = (char)quoteChar;
        for (int versionPos=0; versionPos<XML11_VERSION.length; versionPos++) {
            EXPECTED_VERSION_STRING[15+versionPos] = (char)scanner.scanChar();
        } 
        // REVISIT:  should we check whether this equals quoteChar? 
        EXPECTED_VERSION_STRING[18] = (char)scanner.scanChar();
        fixupCurrentEntity(fEntityManager, EXPECTED_VERSION_STRING, 19);
        int matched = 0;
        for(; matched<XML11_VERSION.length; matched++) {
            if(EXPECTED_VERSION_STRING[15+matched] != XML11_VERSION[matched]) break;
        }
        if(matched == XML11_VERSION.length)
            return Constants.XML_VERSION_1_1;
        return Constants.XML_VERSION_1_0;

    }

    private void reinitializePipelines(short pipelineType) {
        if (pipelineType == Constants.XML_VERSION_1_1) {
            fEntityManager.setScannerVersion(Constants.XML_VERSION_1_1);
            if(fXML11DocScanner == null) {
                fXML11DocScanner = new XML11DocumentScannerImpl();
            }
            fRealDocumentScanner = fXML11DocScanner;
            if(fXML11DTDScanner == null) {
                fXML11DTDScanner = new XML11DTDScannerImpl();
            }
            fRealDTDScanner = fXML11DTDScanner;
            // is there a dtd validator in the pipeline?
            // if so, it'll be XML 1.0; need to replace
            // it...
            XMLDocumentFilter val = null;
            if((val = (XMLDocumentFilter )fComponentManager.getProperty(DTD_VALIDATOR_PROPERTY)) != null) {
                // do we need to new up a replacement? 
                if(fXML11DTDValidator == null) {
                    fXML11DTDValidator = new XML11DTDValidator();
                }
                fXML11DTDValidator.reset(fComponentManager);
                // now take val out of the picture...
                if(val.getDocumentSource() != null) {
                    val.getDocumentSource().setDocumentHandler(fXML11DTDValidator);
                    fXML11DTDValidator.setDocumentSource(val.getDocumentSource());
                }
                if(val.getDocumentHandler() != null) {
                    val.getDocumentHandler().setDocumentSource(fXML11DTDValidator);
                    fXML11DTDValidator.setDocumentHandler(val.getDocumentHandler());
                }
            }
            // is there a namespace binder in the pipeline?
            // if so, it'll be XML 1.0; need to replace
            // it... (REVISIT:  does it make more sense here just to
            // have a feature???)
            XMLDocumentFilter nsb = null;
            if((nsb = (XMLDocumentFilter )fComponentManager.getProperty(NAMESPACE_BINDER_PROPERTY)) != null) {
                // do we need to new up a replacement? 
                if(fXML11NamespaceBinder == null) {
                    fXML11NamespaceBinder = new XML11NamespaceBinder();
                }
                fXML11NamespaceBinder.reset(fComponentManager);
                // now take nsb out of the picture...
                if(nsb.getDocumentSource() != null) {
                    nsb.getDocumentSource().setDocumentHandler(fXML11NamespaceBinder);
                    fXML11NamespaceBinder.setDocumentSource(nsb.getDocumentSource());
                }
                if(nsb.getDocumentHandler() != null) {
                    nsb.getDocumentHandler().setDocumentSource(fXML11NamespaceBinder);
                    fXML11NamespaceBinder.setDocumentHandler(nsb.getDocumentHandler());
                }
            }
            // now do the same to the DTD pipeline.
            // Since a full-featured DTD processor will always 
            // be an XMLDTDFilter and an XMLDTDContentModelFilter,
            // we explicitly make this assumption here...
            XMLDTDFilter proc = null;
            if((proc = (XMLDTDFilter )fComponentManager.getProperty(DTD_PROCESSOR_PROPERTY)) != null) {
                // do we need to new up a replacement? 
                if(fXML11DTDProcessor == null) {
                    fXML11DTDProcessor = new XML11DTDProcessor();
                }
                fXML11DTDProcessor.reset(fComponentManager);
                // now take proc out of the picture...
                if(proc.getDTDSource() != null) {
                    proc.getDTDSource().setDTDHandler(fXML11DTDProcessor);
                    fXML11DTDProcessor.setDTDSource(proc.getDTDSource());
                }
                if(proc.getDTDHandler() != null) {
                    proc.getDTDHandler().setDTDSource(fXML11DTDProcessor);
                    fXML11DTDProcessor.setDTDHandler(proc.getDTDHandler());
                }
                // and last but not least:  same with DTDContentModels...
                // perhaps dangerously, we'll assume the DTD_Processor property
                // will give this to us; this may not
                // be true for some pipelines!
                XMLDTDContentModelFilter cmProc = (XMLDTDContentModelFilter)fComponentManager.getProperty(DTD_PROCESSOR_PROPERTY);
                if(cmProc.getDTDContentModelSource() != null) {
                    cmProc.getDTDContentModelSource().setDTDContentModelHandler(fXML11DTDProcessor);
                    fXML11DTDProcessor.setDTDContentModelSource(cmProc.getDTDContentModelSource());
                }
                if(cmProc.getDTDContentModelHandler() != null) {
                    cmProc.getDTDContentModelHandler().setDTDContentModelSource(fXML11DTDProcessor);
                    fXML11DTDProcessor.setDTDContentModelHandler(cmProc.getDTDContentModelHandler());
                }
            }
        } else {
            // must be a 1.0 pipeline, or it'll be a fatal error later on...
            fEntityManager.setScannerVersion(Constants.XML_VERSION_1_0);
            if(fXML10DocScanner == null) {
                fXML10DocScanner = new XMLDocumentScannerImpl();
            }
            fRealDocumentScanner = fXML10DocScanner;
            if(fXML10DTDScanner == null) {
                fXML10DTDScanner = new XMLDTDScannerImpl();
            }
            fRealDTDScanner = fXML10DTDScanner;
        }

        // set up main pipeline
        fRealDocumentScanner.reset(fComponentManager);
        fRealDocumentScanner.setDocumentHandler(fDocumentHandler);
        fDocumentHandler.setDocumentSource(fRealDocumentScanner);

        // set up DTD pipeline
        fRealDTDScanner.reset(fComponentManager);
        fRealDTDScanner.setDTDHandler(fDTDHandler);
        fDTDHandler.setDTDSource(fRealDTDScanner);
        fRealDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        fDTDContentModelHandler.setDTDContentModelSource(fRealDTDScanner);

        // and link the two (note this must be done after reset...):
        fRealDocumentScanner.setProperty(DTD_SCANNER_PROPERTY, fRealDTDScanner);
    }

    // This method prepends "length" chars from the char array,
    // from offset 0, to the manager's fCurrentEntity.ch.
    private void fixupCurrentEntity(XMLEntityManager manager, 
                char [] scannedChars, int length) {
        XMLEntityManager.ScannedEntity currentEntity = manager.getCurrentEntity();
        if(currentEntity.count-currentEntity.position+length > currentEntity.ch.length) {
            //resize array; this case is hard to imagine...
            char[] tempCh = currentEntity.ch;
            currentEntity.ch = new char[length+currentEntity.count-currentEntity.position+1];
            System.arraycopy(tempCh, 0, currentEntity.ch, 0, tempCh.length);
        }
        if(currentEntity.position < length) {
            // have to move sensitive stuff out of the way...
            System.arraycopy(currentEntity.ch, currentEntity.position, currentEntity.ch, length, currentEntity.count-currentEntity.position);
            currentEntity.count += length-currentEntity.position;
        } else {
            // have to reintroduce some whitespace so this parses:
            for(int i=length; i<currentEntity.position; i++) 
                currentEntity.ch[i]=' ';
        }
        // prepend contents...
        System.arraycopy(scannedChars, 0, currentEntity.ch, 0, length);
        currentEntity.position = 0;
        currentEntity.columnNumber = currentEntity.lineNumber = 1;
    }

} // class XMLVersionDetector

