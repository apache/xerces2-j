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

package org.apache.xerces.impl.validation;

import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Stores grammars in a pool associated to a specific key. This grammar pool
 * implementation stores two types of grammars: those keyed by the root element
 * name, and those keyed by the grammar's target namespace.
 *
 * This is for now, a very simple default implementation of the GrammarPool
 * interface.  As we move forward, this will become more function-rich and
 * robust.
 *
 * @author Jeffrey Rodriguez, IBM
 * @author Andy Clark, IBM
 * @author Neil Graham, IBM
 *
 * @version $Id$
 */
public class XMLGrammarPoolImpl implements XMLGrammarPool {

    //
    // Data
    //

    /** Grammars associated with element root name. */
    protected Hashtable fGrammars = new Hashtable();

    /** Grammars associated with namespaces. */
    protected Hashtable fGrammarsNS = new Hashtable();
    protected Grammar fNoNSGrammar = null;

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLGrammarPoolImpl() {
    } // <init>()

    // XMLGrammarPool methods
    // REVISIT:  implement these!
    public Grammar [] retrieveInitialGrammarSet (String grammarType) {
        return null;
    } // retrieveInitialGrammarSet (String): Grammar[]

    public void cacheGrammars(String grammarType, Grammar[] grammars) {
    } // cacheGrammars(String, Grammar[]);

    public Grammar retrieveGrammar(XMLGrammarDescription desc) {
        return null;
    } // retrieveGrammar(Grammar):  XMLGrammarDescription

    //
    // Public methods
    //

    /**
     * Puts the specified grammar into the grammar pool and associate it to
     * a root element name.
     * 
     * @param rootElement Root element name.
     * @param grammar     The grammar.
     */
    public void putGrammar(String rootElement, Grammar grammar) {
        fGrammars.put(rootElement, grammar);
    } // putGrammar(String,Grammar)

    /**
     * Puts the specified grammar into the grammar pool and associate it to
     * a target namespace.
     *
     * @param namespace The grammar namespace.
     * @param grammar   The grammar.
     */
    public void putGrammarNS(String namespace, Grammar grammar) {
        if(namespace != null) {
            fGrammarsNS.put(namespace, grammar);
        } else {
            fNoNSGrammar = grammar;
        }
    } // putGrammarNS(String,Grammar)

    /**
     * Returns the grammar associated to the specified root element name.
     * 
     * @param rootElement Root element name.
     */
    public Grammar getGrammar(String rootElement) {
        return (Grammar)fGrammars.get(rootElement);
    } // getGrammar(String):Grammar

    /**
     * Returns the grammar associated to the specified target namespace.
     * 
     * @param namespace Target namespace.
     */
    public Grammar getGrammarNS(String namespace) {
        return ((namespace == null)?
            fNoNSGrammar :
            (Grammar)fGrammarsNS.get(namespace));
    } // getGrammarNS(String):Grammar

    /**
     * Removes the grammar associated to the specified root elememt name from the
     * grammar pool and returns the removed grammar.
     * 
     * @param rootElement Root element name.
     */
    public Grammar removeGrammar(String rootElement) {
        if (fGrammars.contains(rootElement)) {
            return (Grammar)fGrammars.remove(rootElement);
        }
        return null;
    } // removeGrammar(String):Grammar

    /**
     * Removes the grammar associated to the specified namespace from the
     * grammar pool and returns the removed grammar.
     * 
     * @param namespace Target namespace.
     */
    public Grammar removeGrammarNS(String namespace) {
        if(namespace == null) {
            Grammar tempGrammar = fNoNSGrammar;
            fNoNSGrammar = null;
            return tempGrammar;
        } else if (fGrammarsNS.contains(namespace)) {
            return (Grammar)fGrammarsNS.remove(namespace);
        }
        return null;
    } // removeGrammarNS(String):Grammar

    /**
     * Returns true if the grammar pool contains a grammar associated
     * to the specified root element name.
     *
     * @param rootElement Root element name.
     */
    public boolean containsGrammar(String rootElement) {
        return fGrammars.containsKey(rootElement);
    } // containsGrammar(String):boolean

    /**
     * Returns true if the grammar pool contains a grammar associated
     * to the specified target namespace.
     *
     * @param namespace Target namespace.
     */
    public boolean containsGrammarNS(String namespace) {
        return fGrammarsNS.containsKey(namespace);
    } // containsGrammarNS(String):boolean

    public Grammar [] getGrammars() {
        int grammarSize = fGrammars.size() ;
        Grammar [] toReturn = new Grammar[grammarSize];
        int pos = 0;
        Enumeration grammars = fGrammars.elements();
        while (grammars.hasMoreElements()) {
            toReturn[pos++] = (Grammar)grammars.nextElement();
        }
        return toReturn;
    } // getGrammars()

    /**
     * Returns all grammars associated with namespaces.
     * 
     */
    public Grammar [] getGrammarsNS() {
        int grammarSize = fGrammarsNS.size() + ((fNoNSGrammar == null) ? 0 : 1);
        Grammar [] toReturn = new Grammar[grammarSize];
        int pos = 0;
        Enumeration grammarsNS = fGrammarsNS.elements();
        while (grammarsNS.hasMoreElements()) {
            toReturn[pos++] = (Grammar)grammarsNS.nextElement();
        }
        if(pos < grammarSize) 
            toReturn[pos++] = fNoNSGrammar;
        return toReturn; 
    } // getGrammarsNS()
} // class GrammarPool
