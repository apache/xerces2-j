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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package xni.parser;


import xni.PSVIWriter;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.util.SymbolTable;

import org.apache.xerces.xni.parser.XMLComponentManager;

/**
 * This is the DTD/ XML Schema parser configuration that includes PSVIWriter component.
 * The document will be fully assessed and will produce PSVI as required by XML Schema specification
 * configuration including XML Schema Validator in the pipeline.
 * 
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class PSVIConfiguration extends StandardParserConfiguration {


     /** PSVI Writer */
    protected PSVIWriter fPSVIWriter;
    
    //
    // Constructors
    //

    /**
     * Constructs a document parser using the default symbol table and grammar
     * pool or the ones specified by the application (through the properties).
     */
    public PSVIConfiguration() {
        this(null, null);
    } // <init>()

    /**
     * Constructs a document parser using the specified symbol table.
     *
     * @param symbolTable    The symbol table to use.
     */
    public PSVIConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    } // <init>(SymbolTable)

    /**
     * Constructs a document parser using the specified symbol table and
     * grammar pool.
     * <p>
     * <strong>REVISIT:</strong>
     * Grammar pool will be updated when the new validation engine is
     * implemented.
     *
     * @param symbolTable    The symbol table to use.
     * @param grammarPool    The grammar pool to use.
     */
    public PSVIConfiguration(SymbolTable symbolTable,
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
    public PSVIConfiguration(SymbolTable symbolTable,
                                    XMLGrammarPool grammarPool,
                                    XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);

        fPSVIWriter = createPSVIWriter();
        if (fPSVIWriter != null) {
            addComponent(fPSVIWriter);
        }

    } // <init>(SymbolTable,XMLGrammarPool)


    /** Configures the pipeline. */
    protected void configurePipeline() {

        super.configurePipeline();
        if (fSchemaValidator != null) {
            fSchemaValidator.setDocumentHandler(fPSVIWriter);
            fPSVIWriter.setDocumentHandler(fDocumentHandler);
            fPSVIWriter.setDocumentSource(fSchemaValidator);
        }

    } // configurePipeline()


    /** Create a PSVIWriter */
    protected PSVIWriter createPSVIWriter(){
        return new PSVIWriter();
    }

} // class StandardParserConfiguration
