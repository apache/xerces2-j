/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  
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
 * originally based on software copyright (c) 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.parsers;

import java.io.IOException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XML11DTDScannerImpl;
import org.apache.xerces.impl.XML11DocumentScannerImpl;
import org.apache.xerces.impl.XML11NamespaceBinder;
import org.apache.xerces.impl.XML11NSDocumentScannerImpl;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLVersionDetector;

import org.apache.xerces.impl.dtd.XML11DTDProcessor;
import org.apache.xerces.impl.dtd.XML11DTDValidator;
import org.apache.xerces.impl.dtd.XML11NSDTDValidator;

import org.apache.xerces.impl.dv.DTDDVFactory;

import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;

import org.apache.xerces.util.SymbolTable;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;

/**
 * This class is the configuration used to parse XML 1.1 documents.
 * It extends the StandardParserConfiguration by making
 * use of a special scanner which detects the version of the document
 * being scanned and modifies the pipeline to employ
 * scanners optimal for the document being scanned.
 *
 * @author Neil Graham, IBM
 * @author Michael Glavassevich, IBM
 *
 * @version $Id$
 */
public class XML11Configuration extends IntegratedParserConfiguration {

    //
    // Constants
    //
    protected final static String XML11_DATATYPE_VALIDATOR_FACTORY =
        "org.apache.xerces.impl.dv.dtd.XML11DTDDVFactoryImpl";

    // 
    // Data
    //

    protected XMLVersionDetector fVersionDetector = new XMLVersionDetector();

    /** The XML 1.1 document scanner that does namespace binding. **/
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner = null;

    /** The XML 1.1 document scanner that does not do namespace binding. **/
    protected XML11DocumentScannerImpl fXML11DocScanner = null;

    /** The XML 1.1 DTD scanner. **/
    protected XML11DTDScannerImpl fXML11DTDScanner = null;

    /** The XML 1.1 DTD validator that does namespace binding. **/
    protected XML11NSDTDValidator fXML11NSDTDValidator = null;

    /** The XML 1.1 DTD validator that does not do namespace binding. **/
    protected XML11DTDValidator fXML11DTDValidator = null;

    /** The XML 1.1 DTD processor. **/
    protected XML11DTDProcessor fXML11DTDProcessor = null;

    /** The XML 1.1 namespace binder. **/
    protected XML11NamespaceBinder fXML11NamespaceBinder = null;

    /** The XML 1.1 datatype factory. **/
    protected DTDDVFactory fXML11DatatypeFactory = null;

    //
    // Constructors
    //

    /** Default constructor. */
    public XML11Configuration() {
        this(null, null, null);
    } // <init>()

    /** 
     * Constructs a parser configuration using the specified symbol table. 
     *
     * @param symbolTable The symbol table to use.
     */
    public XML11Configuration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a parser configuration using the specified symbol table and
     * grammar pool.
     * <p>
     * <strong>REVISIT:</strong> 
     * Grammar pool will be updated when the new validation engine is
     * implemented.
     *
     * @param symbolTable The symbol table to use.
     * @param grammarPool The grammar pool to use.
     */
    public XML11Configuration(SymbolTable symbolTable, 
                              XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } // <init>(SymbolTable,XMLGrammarPool)

    /**
     * Constructs a parser configuration using the specified symbol table,
     * grammar pool, and parent settings.
     * <p>
     * <strong>REVISIT:</strong> 
     * Grammar pool will be updated when the new validation engine is
     * implemented.
     *
     * @param symbolTable    The symbol table to use.
     * @param grammarPool    The grammar pool to use.
     * @param parentSettings The parent settings.
     */
    public XML11Configuration(SymbolTable symbolTable, 
                              XMLGrammarPool grammarPool, 
                              XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
    } // <init>(SymbolTable,XMLGrammarPool)

    //
    // Public methods
    //
    public boolean parse(boolean complete) throws XNIException, IOException {

        //
        // reset and configure pipeline and set InputSource.
        if (fInputSource != null) {
            try {
                fVersionDetector.reset(this);
                reset();

                short version = fVersionDetector.determineDocVersion(fInputSource);
                if (version == Constants.XML_VERSION_1_1) {
                    // XML 1.1 pipeline
                    configureXML11Pipeline();
                }
                // resets and sets the pipeline.
                fVersionDetector.startDocumentParsing((XMLEntityHandler) fScanner, version);
                fInputSource = null;
            } catch (XNIException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (IOException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (RuntimeException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }

        try {
            return fScanner.scanDocument(complete);
        } catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }

    } // parse(boolean):boolean

    /**
     *  Configures the XML 1.1 pipeline. 
     *  Note: this method also resets the new XML11 components.
     */
    protected void configureXML11Pipeline() {

        // create datatype factory
        if (fXML11DatatypeFactory == null) {
            fXML11DatatypeFactory = DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
        }
        setProperty(DATATYPE_VALIDATOR_FACTORY, fXML11DatatypeFactory);

        // setup XML 1.1 DTD pipeline
        if (fXML11DTDScanner == null) {
            fXML11DTDScanner = new XML11DTDScannerImpl();
        }
        if (fXML11DTDProcessor == null) {
            fXML11DTDProcessor = new XML11DTDProcessor();
        }
        fProperties.put(DTD_SCANNER, fXML11DTDScanner);
        fProperties.put(DTD_PROCESSOR, fXML11DTDProcessor);

        fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fXML11DTDProcessor);
        }

        fXML11DTDScanner.setDTDContentModelHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDContentModelSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.setDTDContentModelSource(fXML11DTDProcessor);
        }

        // setup XML 1.1 document pipeline
        if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            // REVISIT: Do we still need the namespace binder in the
            // configuration? It doesn't appear that any other
            // component uses it. - mrglavas
            if (fXML11NamespaceBinder == null) {
                fXML11NamespaceBinder = new XML11NamespaceBinder();
            }
            fProperties.put(NAMESPACE_BINDER, fXML11NamespaceBinder);

            if (fXML11NSDocScanner == null) {
                fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
            }
            fScanner = fXML11NSDocScanner;
            fProperties.put(DOCUMENT_SCANNER, fXML11NSDocScanner);

            if (fXML11NSDTDValidator == null) {
                fXML11NSDTDValidator = new XML11NSDTDValidator();
            }
            fProperties.put(DTD_VALIDATOR, fXML11NSDTDValidator);

            fXML11NSDocScanner.setDTDValidator(fXML11NSDTDValidator);
            fXML11NSDocScanner.setDocumentHandler(fXML11NSDTDValidator);

            fXML11NSDTDValidator.setDocumentSource(fXML11NSDocScanner);
            fXML11NSDTDValidator.setDocumentHandler(fDocumentHandler);

            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fXML11NSDTDValidator);
            }
            fLastComponent = fXML11NSDTDValidator;

            // Reset namespace pipeline components.
            fXML11NSDocScanner.reset(this);
            fXML11NSDTDValidator.reset(this);
            fXML11NamespaceBinder.reset(this);
            
        } else {
            if (fXML11DocScanner == null) {
                fXML11DocScanner = new XML11DocumentScannerImpl();
            }
            fScanner = fXML11DocScanner;
            fProperties.put(DOCUMENT_SCANNER, fXML11DocScanner);

            if (fXML11DTDValidator == null) {
                fXML11DTDValidator = new XML11DTDValidator();
            }
            fProperties.put(DTD_VALIDATOR, fXML11DTDValidator);

            fXML11DocScanner.setDocumentHandler(fXML11DTDValidator);
            
            fXML11DTDValidator.setDocumentSource(fXML11DocScanner);
            fXML11DTDValidator.setDocumentHandler(fDocumentHandler);
            
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fXML11DTDValidator);
            }
            fLastComponent = fXML11DTDValidator;
            
            // Reset no namespace pipeline components.
            fXML11DocScanner.reset(this);
            fXML11DTDValidator.reset(this);
        }
        
        // Reset DTD pipeline components.
        fXML11DTDScanner.reset(this);
        fXML11DTDProcessor.reset(this);

        // setup document pipeline
        if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            // If schema validator was not in the pipeline insert it.
            if (fSchemaValidator == null) {
                fSchemaValidator = new XMLSchemaValidator();

                // add schema component
                fProperties.put(SCHEMA_VALIDATOR, fSchemaValidator);
                addComponent(fSchemaValidator);
                // add schema message formatter
                if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
                }
            }

            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent = fSchemaValidator;
        }

    } // configurePipeline()

} // class XML11Configuration
