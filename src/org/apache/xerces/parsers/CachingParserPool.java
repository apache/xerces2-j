/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.parsers;

import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.util.XMLGrammarPoolImpl;

import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.SynchronizedSymbolTable;

/**
 * A parser pool that enables caching of grammars. The caching parser
 * pool is constructed with a specific symbol table and grammar pool
 * that has already been populated with the grammars used by the
 * application.
 * <p>
 * Once the caching parser pool is constructed, specific parser
 * instances are created by calling the appropriate factory method
 * on the parser pool.
 * <p>
 * <strong>Note:</strong> There is a performance penalty for using
 * a caching parser pool due to thread safety. Access to the symbol 
 * table and grammar pool must be synchronized to ensure the safe
 * operation of the symbol table and grammar pool.
 * <p>
 * <strong>Note:</strong> If performance is critical, then another
 * mechanism needs to be used instead of the caching parser pool.
 * One approach would be to create parser instances that do not
 * share these structures. Instead, each instance would get its
 * own copy to use while parsing. This avoids the synchronization
 * overhead at the expense of more memory and the time required
 * to copy the structures for each new parser instance. And even
 * when a parser instance is re-used, there is a potential for a
 * memory leak due to new symbols being added to the symbol table
 * over time. In other words, always take caution to make sure
 * that your application is thread-safe and avoids leaking memory.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class CachingParserPool {

    //
    // Constants
    //

    /** Default shadow symbol table (false). */
    public static final boolean DEFAULT_SHADOW_SYMBOL_TABLE = false;

    /** Default shadow grammar pool (false). */
    public static final boolean DEFAULT_SHADOW_GRAMMAR_POOL = false;

    //
    // Data
    //

    /** 
     * Symbol table. The symbol table that the caching parser pool is
     * constructed with is automatically wrapped in a synchronized
     * version for thread-safety.
     */
    protected SymbolTable fSynchronizedSymbolTable;

    /** 
     * Grammar pool. The grammar pool that the caching parser pool is
     * constructed with is automatically wrapped in a synchronized
     * version for thread-safety.
     */
    protected XMLGrammarPool fSynchronizedGrammarPool;

    /** 
     * Shadow the symbol table for new parser instances. If true,
     * new parser instances use shadow copies of the main symbol
     * table and are not allowed to add new symbols to the main
     * symbol table. New symbols are added to the shadow symbol
     * table and are local to the parser instance.
     */
    protected boolean fShadowSymbolTable = DEFAULT_SHADOW_SYMBOL_TABLE;

    /** 
     * Shadow the grammar pool for new parser instances. If true,
     * new parser instances use shadow copies of the main grammar
     * pool and are not allowed to add new grammars to the main
     * grammar pool. New grammars are added to the shadow grammar
     * pool and are local to the parser instance.
     */
    protected boolean fShadowGrammarPool = DEFAULT_SHADOW_GRAMMAR_POOL;

    //
    // Constructors
    //

    /** Default constructor. */
    public CachingParserPool() {
        this(new SymbolTable(), new XMLGrammarPoolImpl());
    } // <init>()

    /**
     * Constructs a caching parser pool with the specified symbol table
     * and grammar pool.
     * 
     * @param symbolTable The symbol table.
     * @param grammarPool The grammar pool.
     */
    public CachingParserPool(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        fSynchronizedSymbolTable = new SynchronizedSymbolTable(symbolTable);
        fSynchronizedGrammarPool = new SynchronizedGrammarPool(grammarPool);
    } // <init>(SymbolTable,XMLGrammarPool)

    //
    // Public methods
    //

    /** Returns the symbol table. */
    public SymbolTable getSymbolTable() {
        return fSynchronizedSymbolTable;
    } // getSymbolTable():SymbolTable

    /** Returns the grammar pool. */
    public XMLGrammarPool getXMLGrammarPool() {
        return fSynchronizedGrammarPool;
    } // getXMLGrammarPool():XMLGrammarPool

    // setters and getters

    /** 
     * Sets whether new parser instance receive shadow copies of the
     * main symbol table.
     *
     * @param shadow If true, new parser instances use shadow copies 
     *               of the main symbol table and are not allowed to
     *               add new symbols to the main symbol table. New
     *               symbols are added to the shadow symbol table and
     *               are local to the parser instance. If false, new
     *               parser instances are allowed to add new symbols
     *               to the main symbol table.
     */
    public void setShadowSymbolTable(boolean shadow) {
        fShadowSymbolTable = shadow;
    } // setShadowSymbolTable(boolean)

    // factory methods

    /** Creates a new DOM parser. */
    public DOMParser createDOMParser() {
        SymbolTable symbolTable = fShadowSymbolTable
                                ? new ShadowedSymbolTable(fSynchronizedSymbolTable)
                                : fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool = fShadowGrammarPool
                                ? new ShadowedGrammarPool(fSynchronizedGrammarPool)
                                : fSynchronizedGrammarPool;
        return new DOMParser(symbolTable, grammarPool);
    } // createDOMParser():DOMParser

    /** Creates a new SAX parser. */
    public SAXParser createSAXParser() {
        SymbolTable symbolTable = fShadowSymbolTable
                                ? new ShadowedSymbolTable(fSynchronizedSymbolTable)
                                : fSynchronizedSymbolTable;
        XMLGrammarPool grammarPool = fShadowGrammarPool
                                ? new ShadowedGrammarPool(fSynchronizedGrammarPool)
                                : fSynchronizedGrammarPool;
        return new SAXParser(symbolTable, grammarPool);
    } // createSAXParser():SAXParser

    //
    // Classes
    //

    /**
     * Shadowed symbol table.
     *
     * @author Andy Clark, IBM
     */
    public static final class ShadowedSymbolTable
        extends SymbolTable {
        
        //
        // Data
        //

        /** Main symbol table. */
        protected SymbolTable fSymbolTable;

        //
        // Constructors
        //

        /** Constructs a shadow of the specified symbol table. */
        public ShadowedSymbolTable(SymbolTable symbolTable) {
            fSymbolTable = symbolTable;
        } // <init>(SymbolTable)

        //
        // SymbolTable methods
        //
        
        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists, 
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         * 
         * @param symbol The new symbol.
         */
        public String addSymbol(String symbol) {

            if (fSymbolTable.containsSymbol(symbol)) {
                return fSymbolTable.addSymbol(symbol);
            }
            return super.addSymbol(symbol);

        } // addSymbol(String)

        /**
         * Adds the specified symbol to the symbol table and returns a
         * reference to the unique symbol. If the symbol already exists, 
         * the previous symbol reference is returned instead, in order
         * guarantee that symbol references remain unique.
         * 
         * @param buffer The buffer containing the new symbol.
         * @param offset The offset into the buffer of the new symbol.
         * @param length The length of the new symbol in the buffer.
         */
        public String addSymbol(char[] buffer, int offset, int length) {

            if (fSymbolTable.containsSymbol(buffer, offset, length)) {
                return fSymbolTable.addSymbol(buffer, offset, length);
            }
            return super.addSymbol(buffer, offset, length);

        } // addSymbol(char[],int,int):String

        /**
         * Returns a hashcode value for the specified symbol. The value
         * returned by this method must be identical to the value returned
         * by the <code>hash(char[],int,int)</code> method when called
         * with the character array that comprises the symbol string.
         * 
         * @param symbol The symbol to hash.
         */
        public int hash(String symbol) {
            return fSymbolTable.hash(symbol);
        } // hash(String):int

        /**
         * Returns a hashcode value for the specified symbol information. 
         * The value returned by this method must be identical to the value
         * returned by the <code>hash(String)</code> method when called
         * with the string object created from the symbol information.
         * 
         * @param buffer The character buffer containing the symbol.
         * @param offset The offset into the character buffer of the start
         *               of the symbol.
         * @param length The length of the symbol.
         */
        public int hash(char[] buffer, int offset, int length) {
            return fSymbolTable.hash(buffer, offset, length);
        } // hash(char[],int,int):int

    } // class ShadowedSymbolTable

    /**
     * Synchronized grammar pool.
     *
     * @author Andy Clark, IBM
     */
    public static final class SynchronizedGrammarPool
        implements XMLGrammarPool {

        //
        // Data
        //

        /** Main grammar pool. */
        private XMLGrammarPool fGrammarPool;

        //
        // Constructors
        //

        /** Constructs a synchronized grammar pool. */
        public SynchronizedGrammarPool(XMLGrammarPool grammarPool) {
            fGrammarPool = grammarPool;
        } // <init>(XMLGrammarPool)

        //
        // GrammarPool methods
        //

        // retrieve the initial set of grammars for the validator
        // to work with.
        // REVISIT:  does this need to be synchronized since it's just reading?
        // @param grammarType type of the grammars to be retrieved.
        // @return the initial grammar set the validator may place in its "bucket"
        public Grammar [] retrieveInitialGrammarSet(String grammarType ) {
            synchronized (fGrammarPool) {
                return fGrammarPool.retrieveInitialGrammarSet(grammarType);
            }
        } // retrieveInitialGrammarSet(String):  Grammar[]

        // retrieve a particular grammar.
        // REVISIT:  does this need to be synchronized since it's just reading?
        // @param gDesc description of the grammar to be retrieved
        // @return Grammar corresponding to gDesc, or null if none exists.
        public Grammar retrieveGrammar(XMLGrammarDescription gDesc) {
            synchronized (fGrammarPool) {
                return fGrammarPool.retrieveGrammar(gDesc);
            }
        } // retrieveGrammar(XMLGrammarDesc):  Grammar

        // give the grammarPool the option of caching these grammars.
        // This certainly must be synchronized.
        // @param grammarType The type of the grammars to be cached.
        // @param grammars the Grammars that may be cached (unordered, Grammars previously
        //  given to the validator may be included).
        public void cacheGrammars(String grammarType, Grammar[] grammars) {
            synchronized (fGrammarPool) {
                fGrammarPool.cacheGrammars(grammarType, grammars);
            }
        } // cacheGrammars(String, Grammar[]);

        /** lock the grammar pool */
        public void lockPool() {
            synchronized (fGrammarPool) {
                fGrammarPool.lockPool();
            }
        } // lockPool()

        /** clear the grammar pool */
        public void clear() {
            synchronized (fGrammarPool) {
                fGrammarPool.clear();
            }
        } // lockPool()

        /** unlock the grammar pool */
        public void unlockPool() {
            synchronized (fGrammarPool) {
                fGrammarPool.unlockPool();
            }
        } // unlockPool()

        /***
         * Methods corresponding to original (pre Xerces2.0.0final)
         * grammarPool have been commented out.
         */
        /**
         * Puts the specified grammar into the grammar pool.
         * 
         * @param key Key to associate with grammar.
         * @param grammar Grammar object.
         */
        /******
        public void putGrammar(String key, Grammar grammar) {
            synchronized (fGrammarPool) {
                fGrammarPool.putGrammar(key, grammar);
            }
        } // putGrammar(String,Grammar)
        *******/

        /**
         * Returns the grammar associated to the specified key.
         * 
         * @param key The key of the grammar.
         */
        /**********
        public Grammar getGrammar(String key) {
            synchronized (fGrammarPool) {
                return fGrammarPool.getGrammar(key);
            }
        } // getGrammar(String):Grammar
        ***********/

        /**
         * Removes the grammar associated to the specified key from the
         * grammar pool and returns the removed grammar.
         * 
         * @param key The key of the grammar.
         */
        /**********
        public Grammar removeGrammar(String key) {
            synchronized (fGrammarPool) {
                return fGrammarPool.removeGrammar(key);
            }
        } // removeGrammar(String):Grammar
        ******/

        /**
         * Returns true if the grammar pool contains a grammar associated
         * to the specified key.
         *
         * @param key The key of the grammar.
         */
        /**********
        public boolean containsGrammar(String key) {
            synchronized (fGrammarPool) {
                return fGrammarPool.containsGrammar(key);
            }
        } // containsGrammar(String):boolean
        ********/

    } // class SynchronizedGrammarPool

    /**
     * Shadowed grammar pool.
     * This class is predicated on the existence of a concrete implementation;
     * so using our own doesn't seem to bad an idea.
     *
     * @author Andy Clark, IBM
     * @author Neil Graham, IBM
     */
    public static final class ShadowedGrammarPool
        extends XMLGrammarPoolImpl {

        //
        // Data
        //

        /** Main grammar pool. */
        private XMLGrammarPool fGrammarPool;

        //
        // Constructors
        //

        /** Constructs a shadowed grammar pool. */
        public ShadowedGrammarPool(XMLGrammarPool grammarPool) {
            fGrammarPool = grammarPool;
        } // <init>(GrammarPool)

        //
        // GrammarPool methods
        //

        /**
         * Retrieve the initial set of grammars for the validator to work with.
         * REVISIT:  does this need to be synchronized since it's just reading?
         * 
         * @param grammarType Type of the grammars to be retrieved.
         * @return            The initial grammar set the validator may place in its "bucket"
         */
        public Grammar [] retrieveInitialGrammarSet(String grammarType ) {
            Grammar [] grammars = super.retrieveInitialGrammarSet(grammarType);
            if (grammars != null) return grammars;
            return fGrammarPool.retrieveInitialGrammarSet(grammarType);
        } // retrieveInitialGrammarSet(String):  Grammar[]

        /**
         * Retrieve a particular grammar.
         * REVISIT:  does this need to be synchronized since it's just reading?
         *
         * @param gDesc Description of the grammar to be retrieved
         * @return      Grammar corresponding to gDesc, or null if none exists.
         */
        public Grammar retrieveGrammar(XMLGrammarDescription gDesc) {
            Grammar g = super.retrieveGrammar(gDesc);
            if(g != null) return g;
            return fGrammarPool.retrieveGrammar(gDesc);
        } // retrieveGrammar(XMLGrammarDesc):  Grammar

        /** 
         * Give the grammarPool the option of caching these grammars.
         * This certainly must be synchronized.
         * 
         * @param grammarType The type of the grammars to be cached.
         * @param grammars    The Grammars that may be cached (unordered, Grammars previously
         *  		      given to the validator may be included).
         */
        public void cacheGrammars(String grammarType, Grammar[] grammars) { 
           // better give both grammars a shot...
           super.cacheGrammars(grammarType, grammars);
           fGrammarPool.cacheGrammars(grammarType, grammars);
        } // cacheGrammars(grammarType, Grammar[]);

        /**
         * Returns the grammar associated to the specified description.
         * 
         * @param desc The description of the grammar.
         */
        public Grammar getGrammar(XMLGrammarDescription desc) {

            if (super.containsGrammar(desc)) {
                return super.getGrammar(desc);
            }
            return null;

        } // getGrammar(XMLGrammarDescription):Grammar

        /**
         * Returns true if the grammar pool contains a grammar associated
         * to the specified description.
         *
         * @param desc The description of the grammar.
         */
        public boolean containsGrammar(XMLGrammarDescription desc) {
            return super.containsGrammar(desc);
        } // containsGrammar(XMLGrammarDescription):boolean

    } // class ShadowedGrammarPool

} // class CachingParserPool
