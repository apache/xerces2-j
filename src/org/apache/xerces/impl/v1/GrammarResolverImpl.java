/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.v1;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.xerces.impl.v1.Grammar;
import org.apache.xerces.impl.v1.GrammarResolver;
import org.apache.xerces.impl.v1.datatype.DatatypeValidatorFactory;
import org.apache.xerces.impl.v1.datatype.DatatypeValidatorFactoryImpl;

/**
 * This class embodies the representation of a Schema Grammar
 * pool.
 * This class is called from the validator.
 * Grammar pool maps to a set of Grammar Proxy classes.
 * 
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class GrammarResolverImpl implements GrammarResolver {

    //
    // Data
    //

    /**
     *           Hashtable structure that represents a mapping
     *           between Namespace and a Grammar
     */
    private Hashtable fGrammarRegistry    = new Hashtable();//This class keeps a hashtable of references to Grammar structures

    //optimization -el
    //private DatatypeValidatorFactoryImpl fDataTypeReg = new DatatypeValidatorFactoryImpl();
    private DatatypeValidatorFactoryImpl fDataTypeReg;
    //
    // Constructors
    //

    /** Default constructor. */
    public GrammarResolverImpl() {
    }

    //
    // GrammarResolver methods
    //

    /**
     * 
     * @param nameSpaceKey
     *               Namespace key into Grammar pool
     * @return                           Grammar abstraction associated
     *         with NameSpace key.
     */
    public Grammar getGrammar( String nameSpaceKey ) {
        return(Grammar) ( fGrammarRegistry.get( nameSpaceKey ) ); 
    }

    public DatatypeValidatorFactory getDatatypeRegistry(){
        if (fDataTypeReg == null) {   //optimization -el
            fDataTypeReg = new DatatypeValidatorFactoryImpl();
        }
        return fDataTypeReg;
    }

    /**
     * 
     * @return             Array of String key name spaces in Grammar pool
     */
    public String[] getNSKeysInPool() {
        int numberOfNSKeysInPool = fGrammarRegistry.size();
        String[] NSArray         = new String[numberOfNSKeysInPool];
        Enumeration enumOfKeys   = nameSpaceKeys();
        for (int i = 0; i<numberOfNSKeysInPool; i ++ ) {
            NSArray[i] = (String )( enumOfKeys.nextElement() );
        }
        return NSArray;
    }

    /**
     * 
     * @param nameSpaceKey
     *                Key to associate with Grammar
     *                abstraction
     * @param grammar Grammar abstraction
     *                used by validator.
     */
    public void putGrammar( String nameSpaceKey, Grammar grammar ){
        fGrammarRegistry.put( nameSpaceKey, grammar ); 
    }

    /**
     * 
     * @return         Length of grammar pool. Number of associations.
     */
    public int size() {
        return fGrammarRegistry.size();
    }

     /**
     * 
     * @return             Enumeration of String key name spaces in Grammar pool
     */

    public Enumeration nameSpaceKeys(){
        return fGrammarRegistry.keys();
    }


    /**
     * Removes association of Namespace key and Grammar from                         
     * Grammar pool
     * 
     * @param nameSpaceKey
     *               Name space key
     */
    public Grammar removeGrammar( String nameSpaceKey ) {
        if ( containsNameSpace( nameSpaceKey ) == true )
          fGrammarRegistry.remove( nameSpaceKey );
        return null;
    }



    /**
     *         Is Grammar abstraction in Grammar pool?
     * 
     * @param grammar Grammar Abstraction
     * @return true  - Yes there is at least one instance
     *         false - No
     */
    public boolean contains( Grammar grammar ){
        return fGrammarRegistry.contains( grammar );
    }

    /**
     *                Is Namespace key in Grammar pool
     * 
     * @param nameSpaceKey
     *               Namespace key
     * @return                Boolean- true - Namespace key association
     *         is in grammar pool.
     */
    public boolean containsNameSpace( String nameSpaceKey ){
        return fGrammarRegistry.containsKey( nameSpaceKey );
    }

    /**
     *         Reset internal Namespace/Grammar registry.
     */
    public void clearGrammarResolver() { 
        fGrammarRegistry.clear();
        if (fDataTypeReg != null) {     //optimization -el
             fDataTypeReg.resetRegistry();
        }
       
    }






    /* Unit Test

    static final int NGRAMMARS  = 10;


    public static void main( String args[] ) 
    {
        //static final int NGRAMMARS  = 10;
        SchemaGrammarResolver grammarPool     = SchemaGrammarResolver.instanceGrammarResolver();
        Grammar     testGrammars[]  = new Grammar[NGRAMMARS]; 
        String      testNameSpace[] = {
        "http://www.foo1.org/",
        "http://www.foo2.org/", 
        "http://www.foo3.org/", 
        "http://www.foo4.org/", 
        "http://www.foo5.org/", 
        "http://www.foo6.org/", 
        "http://www.foo7.org/", 
        "http://www.foo8.org/", 
        "http://www.foo9.org/", 
        "http://www.foox.org/" }; 

        for( int  i = 0; i< NGRAMMARS ; i++ ) {
        testGrammars[i] = new Grammar( testNameSpace[i] );
        }


        for( int i = 0; i<testGrammars.length; i++ ) {
            grammarPool.addGrammar( testNameSpace[i], testGrammars[i] );
        }
        String [] localNames = grammarPool.getNSKeysInPool();
        for( int i = 0; i<localNames.length; i++ ){
            System.out.println( "Key[" + i + "] =" + localNames[i] );
        }
        // Get a couple of Grammars.

        Grammar gramm1 = grammarPool.getGrammar( "http://www.foo2.org/" ); 
        Grammar gramm2 = grammarPool.getGrammar( "http://www.foox.org/" );

        System.out.println( "Grammar1 id = " + gramm1.whatGrammarAmI() + " It should be http://www.foo2.org/" );
        System.out.println( "Grammar1 id = " + gramm2.whatGrammarAmI() + " It should be http://www.foox.org/" );
         
        Grammar myTestGrammar = new Grammar( "testgrammar" );
        
        boolean isInPool = grammarPool.isGrammarInPool( myTestGrammar);
        System.out.println( "Grammar " + myTestGrammar.whatGrammarAmI()  + "Is in pool = " +  isInPool );
        
        grammarPool.addGrammar("myNSTest", myTestGrammar );
        isInPool = grammarPool.isGrammarInPool( myTestGrammar);
        System.out.println( "Just added Grammar " + myTestGrammar.whatGrammarAmI()  + "Is in pool = " +  isInPool );
                    
        String myNSTest = "http://www.foo.com/";
        isInPool = grammarPool.isNSInPool(myNSTest);

        System.out.println( "NS: " + myNSTest  + "Is in pool = " +  isInPool ); 
        grammarPool.addGrammar(myNSTest, new Grammar( myNSTest ));
        
        isInPool = grammarPool.isNSInPool(myNSTest);

        System.out.println( "NS: " + myNSTest  + "Is in pool = " +  isInPool ); 

        System.out.println( "Length of Grammar pool = " + grammarPool.length() );
               
        grammarPool.resetGrammarPool();
        System.out.println( "Length of Grammar pool now = " + grammarPool.length() );

        grammarPool.addGrammar("myNSTest", myTestGrammar ); // The same key
        grammarPool.addGrammar("myNSTest", myTestGrammar );
        grammarPool.addGrammar("myNSTest", myTestGrammar );
        grammarPool.addGrammar("myNSTest", myTestGrammar );
        grammarPool.addGrammar("myNSTest", myTestGrammar );

        System.out.println( "Length of Grammar pool now better not be 5 = " + grammarPool.length() );
        for( int i = 0; i<testGrammars.length; i++ ) {
        grammarPool.addGrammar( testNameSpace[i], testGrammars[i] );
        }
        grammarPool.deleteGrammarForNS( "myNSTest" );
        System.out.println( "Length of Grammar pool now better not be 5 = " + grammarPool.length() );
        localNames = grammarPool.getNSKeysInPool();
        for( int i = 0; i<localNames.length; i++ ){
                   System.out.println( "Key[" + i + "] =" + localNames[i] );
           }
    }
    */

} // class GrammarResolverImpl
