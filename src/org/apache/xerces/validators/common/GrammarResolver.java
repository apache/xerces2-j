/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.validators.common;
import java.util.*;
import org.apache.xerces.validators.common.Grammar;

/**
 * This class embodies the representation of a Grammar
 * pool Resolver.
 * This class is called from the validator.
 * 
 * 
 * @author Jeffrey Rodriguez
 */

public interface GrammarResolver {
    /**
     *           Hashtable structure that represents a mapping
     *           between Namespace and a Grammar
     */
    private Hashtable fGrammarRegistry    = new Hashtable();//This class keeps a hashtable of references to Grammar structures

    /**
     * initializeGrammarRegistry - Gets call to register initial
     * grammars such as DTD - Schema
     */
    private GrammarResolver() { // Part of the GrammarResolver singleton pattern
    }

     /**
     * 
     * @return           Only instance of Grammar pool ( Singleton
     *         pattern).
     */
    static public GrammarResolver instanceGrammarResolver();



    /**
     * 
     * @param nameSpaceKey
     *               Namespace key into Grammar pool
     * @return                           Grammar abstraction associated
     *         with NameSpace key.
     */
    public Grammar getGrammar( String nameSpaceKey );

    /**
     * 
     * @return             Enumeration of String key name spaces in Grammar pool
     */

    public Enumeration nameSpaceKeys();

    /**
     * 
     * @param nameSpaceKey
     *                Key to associate with Grammar
     *                abstraction
     * @param grammar Grammar abstraction
     *                used by validator.
     */
    public void putGrammar( String nameSpaceKey, Grammar grammar )

    /**
     * Removes association of Namespace key and Grammar from                         
     * Grammar pool
     * 
     * @param nameSpaceKey
     *               Name space key
     */
    public Grammar removeGrammar( String nameSpaceKey ) 



    /**
     *         Is Grammar abstraction in Grammar pool?
     * 
     * @param grammar Grammar Abstraction
     * @return true  - Yes there is at least one instance
     *         false - No
     */
    public boolean contains( Grammar grammar );

    /**
     *                Is Namespace key in Grammar pool
     * 
     * @param nameSpaceKey
     *               Namespace key
     * @return                Boolean- true - Namespace key association
     *         is in grammar pool.
     */
    public boolean containsNamesSpace( String nameSpaceKey ); 

    /**
     *         Reset internal Namespace/Grammar registry.
     */
    public void clearGrammarResolver();

    /**
     * 
     * @return         Length of grammar pool. Number of associations.
     */
    public int size(); 

}



