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

import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.impl.xs.XSDDescription;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Stores grammars in a pool associated to a specific key. This grammar pool
 * implementation stores two types of grammars: those keyed by the root element
 * name, and those keyed by the grammar's target namespace.
 *
 * This is the default implementation of the GrammarPool interface.  
 * As we move forward, this will become more function-rich and robust.
 *
 * @author Jeffrey Rodriguez, IBM
 * @author Andy Clark, IBM
 * @author Neil Graham, IBM
 * @author Pavani Mukthipudi, Sun Microsystems
 * @author Neeraj Bajaj, SUN Microsystems
 *
 * @version $Id$
 */
public class XMLGrammarPoolImpl implements XMLGrammarPool {

    //
    // Data
    //

    /** Grammars associated with element root name. */
    // REVISIT : Do we want to use Internal subset also to identify DTD grammars ?
    protected Hashtable fDTDGrammars = new Hashtable();

    /** Grammars associated with namespaces. */
    // REVISIT : Do we want to use some other information available with the XSDDescription 
    //		 along with the namespace to identify Schema grammars ? 
    protected Hashtable fSchemaGrammars = new Hashtable();
    protected Grammar fNoNSSchemaGrammar = null;

	private static final boolean DEBUG = true ;
	
    //
    // Constructors
    //

    /** Default constructor. */
    public XMLGrammarPoolImpl() {
    } // <init>()

    //
    // XMLGrammarPool methods
    //
    
    /* <p> Retrieve the initial known set of grammars. This method is
     * called by a validator before the validation starts. The application 
     * can provide an initial set of grammars available to the current 
     * validation attempt. </p>
     * 
     * @param grammarType The type of the grammar, from the
     *  		  <code>org.apache.xerces.xni.grammars.XMLGrammarDescription</code> 
     *  		  interface.
     * @return 		  The set of grammars the validator may put in its "bucket"
     */
    public Grammar [] retrieveInitialGrammarSet (String grammarType) {

       	if (grammarType.equals(XMLGrammarDescription.XML_DTD)) {
    	    return getDTDGrammars();
        }
        else if (grammarType.equals(XMLGrammarDescription.XML_SCHEMA)) {
    	    return getSchemaGrammars();
        }
        return null;
    } // retrieveInitialGrammarSet (String): Grammar[]

    /* <p> Return the final set of grammars that the validator ended up
     * with. This method is called after the validation finishes. The 
     * application may then choose to cache some of the returned grammars.</p>
     *
     * @param grammarType The type of the grammars being returned;
     * @param grammars 	  An array containing the set of grammars being
     *  		  returned; order is not significant.
     */
    public void cacheGrammars(String grammarType, Grammar[] grammars) {
    	for (int i = 0; i < grammars.length; i++) {
    	if(DEBUG){
    		System.out.println("CACHED GRAMMAR " + (i+1) ) ;
    		Grammar temp = grammars[i] ;
    		print(temp.getGrammarDescription());
 	}
    	    putGrammar(grammars[i]);
    	}
    } // cacheGrammars(String, Grammar[]);
    
    /* <p> This method requests that the application retrieve a grammar
     * corresponding to the given GrammarIdentifier from its cache.
     * If it cannot do so it must return null; the parser will then
     * call the EntityResolver. </p>
     * <strong>An application must not call its EntityResolver itself 
     * from this method; this may result in infinite recursions.</strong>
     * 
     * This implementation chooses to use the root element name to identify a DTD grammar
     * and the target namespace to identify a Schema grammar.
     * 
     * @param desc The description of the Grammar being requested.
     * @return     The Grammar corresponding to this description or null if
     *  	   no such Grammar is known.
     */
    public Grammar retrieveGrammar(XMLGrammarDescription desc) {
        if(DEBUG){
            System.out.println("RETRIEVING GRAMMAR FROM THE APPLICATION WITH FOLLOWING DESCRIPTION :");
            print(desc);
        }
        return getGrammar(desc);
    } // retrieveGrammar(XMLGrammarDescription):  Grammar

    //
    // Public methods
    //

    /**
     * Puts the specified grammar into the grammar pool and associates it to
     * its root element name or its target namespace.
     *
     * @param grammar The Grammar.
     */
    public void putGrammar(Grammar grammar) {
    	if (grammar.getGrammarDescription().getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
            fDTDGrammars.put(((XMLDTDDescription)grammar.getGrammarDescription()).getRootName(), grammar);
        }
        else if (grammar.getGrammarDescription().getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
            String namespace = ((XSDDescription)grammar.getGrammarDescription()).getTargetNamespace();
	    if (namespace != null) {
    	    	fSchemaGrammars.put(namespace, grammar);
	    }
	    else {
	    	fNoNSSchemaGrammar = grammar;
	    }
        }
    } // putGrammar(Grammar)

    /**
     * Returns the grammar associated to the specified grammar description.
     * Currently, the root element name is used as the key for DTD grammars 
     * and the target namespace  is used as the key for Schema grammars.
     *
     * @param desc The Grammar Description.
     */
    public Grammar getGrammar(XMLGrammarDescription desc) {
    	if (desc.getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
            Grammar grammar = (Grammar)fDTDGrammars.get(((XMLDTDDescription)desc).getRootName());
            
	    // REVISIT : As of right now, calling equals(...) doesn't serve much of the purpose. It's a double check.
	    //		 Probably it will be useful if the way two grammars are compared in equals(...) is changed.
	    if (grammar != null && equals(grammar.getGrammarDescription(), desc)) {
            	return grammar;
            }
        }
        else if (desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
            String namespace = ((XSDDescription)desc).getTargetNamespace();
            Grammar grammar = ((namespace == null)? fNoNSSchemaGrammar : (Grammar)fSchemaGrammars.get(namespace));
            
            if (grammar != null && equals(grammar.getGrammarDescription(), desc)) {
            	return grammar;
            }
        }
        return null;
    } // getGrammar(XMLGrammarDescription):Grammar

    /**
     * Removes the grammar associated to the specified grammar description from the
     * grammar pool and returns the removed grammar. Currently, the root element name 
     * is used as the key for DTD grammars and the target namespace  is used 
     * as the key for Schema grammars.
     * 
     * @param desc The Grammar Description.
     * @return     The removed grammar.
     */
    public Grammar removeGrammar(XMLGrammarDescription desc) {
    	if (containsGrammar(desc)) {
    	    if (desc.getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
    	    	return (Grammar)fDTDGrammars.remove(((XMLDTDDescription)desc).getRootName());
	    }
	    else if (desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
	    	String namespace = ((XSDDescription)desc).getTargetNamespace();
	    	if (namespace == null) {
            	   Grammar tempGrammar = fNoNSSchemaGrammar;
            	   fNoNSSchemaGrammar = null;
            	   return tempGrammar;
            	} 
            	else {
            	   return (Grammar)fSchemaGrammars.remove(namespace);
            	}
	    }
    	}
    	return null;
    } // removeGrammar(XMLGrammarDescription):Grammar

    /**
     * Returns true if the grammar pool contains a grammar associated
     * to the specified grammar description. Currently, the root element name 
     * is used as the key for DTD grammars and the target namespace  is used 
     * as the key for Schema grammars.
     *
     * @param desc The Grammar Description.
     */
    public boolean containsGrammar(XMLGrammarDescription desc) {
    	if (desc.getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
            Grammar grammar = (Grammar)fDTDGrammars.get(((XMLDTDDescription)desc).getRootName());
            if (grammar != null && equals(grammar.getGrammarDescription(), desc)) {
            	return true;
            }
        }
        else if (desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
            String namespace = ((XSDDescription)desc).getTargetNamespace();
            Grammar grammar = ((namespace == null)? fNoNSSchemaGrammar : (Grammar)fSchemaGrammars.get(namespace));
            if (grammar != null && equals(grammar.getGrammarDescription(), desc)) {
            	return true;
            }
        }
    	return false;
    } // containsGrammar(XMLGrammarDescription):boolean

    /**
     * Returns all the DTD grammars.
     * 
     * @return The set of DTD grammars in the pool
     */
    public Grammar [] getDTDGrammars() {
        int grammarSize = fDTDGrammars.size() ;
        Grammar [] toReturn = new Grammar[grammarSize];
        int pos = 0;
        Enumeration grammars = fDTDGrammars.elements();
        while (grammars.hasMoreElements()) {
            toReturn[pos++] = (Grammar)grammars.nextElement();
        }
        return toReturn;
    } // getDTDGrammars()

    /**
     * Returns all the Schema grammars.
     * 
     * @return The set of Schema grammars in the pool
     */
    public Grammar [] getSchemaGrammars() {
        int grammarSize = fSchemaGrammars.size() + ((fNoNSSchemaGrammar == null) ? 0 : 1);
        Grammar [] toReturn = new Grammar[grammarSize];
        int pos = 0;
        Enumeration grammarsNS = fSchemaGrammars.elements();
        while (grammarsNS.hasMoreElements()) {
            toReturn[pos++] = (Grammar)grammarsNS.nextElement();
            if(DEBUG){
            	System.out.println("RETRIEVING INITIAL GRAMMAR " + pos  ) ;
            	Grammar temp = toReturn[pos - 1] ;
            	print(temp.getGrammarDescription());
            }
        }
        if(pos < grammarSize){ 
            toReturn[pos++] = fNoNSSchemaGrammar;
            if(DEBUG){
            	System.out.println("RETRIEVING INITIAL GRAMMAR " + pos  ) ;
            	Grammar temp = toReturn[pos - 1] ;
            	print(temp.getGrammarDescription());
            }
            
        }
        return toReturn; 
    } // getSchemaGrammars()
    
    /**
     * This method checks whether two grammars are the same. Currently, we compare 
     * the root element names for DTD grammars and the target namespaces for Schema grammars.
     * The application can override this behaviour and add its own logic.
     *
     * @param gDesc1 The grammar description
     * @param gDesc2 The grammar description of the grammar to be compared to
     * @return       True if the grammars are equal, otherwise false
     */
    public boolean equals(XMLGrammarDescription gDesc1, XMLGrammarDescription gDesc2) {
    	if (gDesc1.getGrammarType() != gDesc2.getGrammarType()) {
    	    return false;
    	}
    	if (gDesc1.getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
    	    if (((XMLDTDDescription)gDesc1).getRootName().equals(((XMLDTDDescription)gDesc2).getRootName())) {
    	    	return true;
    	    }
    	}
    	else if (gDesc1.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
    	    String namespace1 = ((XSDDescription)gDesc1).getTargetNamespace();
    	    String namespace2 = ((XSDDescription)gDesc2).getTargetNamespace();
    	    if (namespace1 != null && namespace1.equals(namespace2)) {
    	    	return true;
    	    }
    	    else if (namespace1 == null && namespace2 == null) {
    	    	return true;
    	    }
    	}
    	return false; 
    }
    
    public int hashCode(XMLGrammarDescription desc) {
    	if (desc.getGrammarType().equals(XMLGrammarDescription.XML_DTD)) {
            return (((XMLDTDDescription)desc).getRootName()).hashCode();
        }
        else {
            String namespace = ((XSDDescription)desc).getTargetNamespace();
            return (namespace != null) ? namespace.hashCode() : 0;
        }
    }
    
    public void print(XMLGrammarDescription description){
    	if(description.getGrammarType().equals(XMLGrammarDescription.XML_DTD)){
    	
    	}
    	else if(description.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)){
    		XSDDescription schema = (XSDDescription)description ;
    		System.out.println("Context = " + schema.getContextType());
    		System.out.println("TargetNamespace = " + schema.getTargetNamespace());
    		String [] temp = schema.getLocationHints();
    		
    		for (int i = 0 ; (temp != null && i < temp.length) ; i++){
    			System.out.println("LocationHint " + i + " = "+ temp[i]);
    		}
    		    		
    		System.out.println("Triggering Component = " + schema.getTriggeringComponent());
    		System.out.println("EnclosingElementName =" + schema.getEnclosingElementName());
    	
    	}
    
    }//print
    
} // class XMLGrammarPoolImpl
