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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.parsers;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.validation.GrammarPool;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.util.SymbolTable;

/**
 * This is the DTD/ XML Schema parser configuration. It extends the standard
 * configuration including XML Schema Validator in the pipeline. Note: including
 * XML Schema validator in the pipeline will always trigger an assessment of 
 * instance document against schema.
 * 
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class DTDXSParserConfiguration extends StandardParserConfiguration {

    /** Property identifier: XML Schema validator. */
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;

    /** XML Schema Validator. */
    protected XMLSchemaValidator fSchemaValidator;

    //
    // Constructors
    //

    /**
     * Constructs a document parser using the default symbol table and grammar
     * pool or the ones specified by the application (through the properties).
     */
    public DTDXSParserConfiguration() {
        this(null, null);
    } // <init>()

    /**
     * Constructs a document parser using the specified symbol table.
     */
    public DTDXSParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a document parser using the specified symbol table and
     * grammar pool.
     */
    public DTDXSParserConfiguration(SymbolTable symbolTable,
                                     GrammarPool grammarPool) {
        super(symbolTable, grammarPool);

        fSchemaValidator = createSchemaValidator();
        if (fSchemaValidator != null) {
            fProperties.put(SCHEMA_VALIDATOR, fSchemaValidator);
            addComponent(fSchemaValidator);
        }

        // add schema message formatter
        if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            XSMessageFormatter xmft = new XSMessageFormatter();
            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
        }


    } // <init>(SymbolTable,GrammarPool)

    //
    // Public methods
    //

    /** Configures the pipeline. */
    protected void configurePipeline() {

        super.configurePipeline();

        if (fSchemaValidator != null) {
            fNamespaceBinder.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
        }

    } // configurePipeline()

    /** Create a Schema validator. */
    protected XMLSchemaValidator createSchemaValidator() {
        return new XMLSchemaValidator();
    } // createSchemaValidator():XMLSchemaValidator

} // class StandardParserConfiguration
