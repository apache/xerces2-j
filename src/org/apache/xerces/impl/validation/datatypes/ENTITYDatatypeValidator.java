/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.validation.datatypes;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.xerces.impl.validation.DatatypeValidator;

import org.apache.xerces.impl.validation.XMLEntityDecl;
import org.apache.xerces.impl.validation.Grammar;
import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;


/**
 * <P>ENTITYDatatypeValidator implements the
 * DatattypeValidator interface.
 * This validator embodies the ENTITY attribute type
 * from XML1.0 recommendation.
 * The Value space of ENTITY is the set of all strings
 * that match the NCName production and have been
 * declared as an unparsed entity in a document
 * type definition.
 * The Lexical space of Entity is the set of all
 * strings that match the NCName production.
 * The value space of ENTITY is scoped to a specific
 * instance document.</P>
 * <P>This is a statefull datatype validator and it
 * needs access to a valid Grammar structure to being 
 * able to validate entities.</P>
 * 
 * @author Jeffrey Rodriguez-
 * @version $Id$
 * @see org.apache.xerces.impl.validation.Grammar
 * @see org.apache.xerces.impl.validation.grammars.DTDGrammar
 * @see org.apache.xerces.impl.validation.grammars.SchemaGrammar
 */
public class ENTITYDatatypeValidator extends AbstractDatatypeValidator 
implements StatefullDatatypeValidator {
    private DatatypeValidator        fBaseValidator    = null;
    private Grammar                  fGrammar          = null;
    private XMLEntityDecl            fEntityDecl       = new XMLEntityDecl();

    public ENTITYDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public ENTITYDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                     boolean derivedByList  ) throws InvalidDatatypeFacetException {
        setBasetype( base ); // Set base type
    }


    /**
     * <P>Checks that "content" string is valid
     * datatype.
     * If invalid a Datatype validation exception is thrown.</P>
     * <P>The following constrain is checked:
     * ENTITY values must match an unparsed entity 
     * name that is declared in the schema.</P> 
     * 
     * @param content A string containing the content to be validated
     * @param state
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see org.apache.xerces.validators.datatype.InvalidDatatypeValueException
     */
    public void validate(String content, Object state ) throws InvalidDatatypeValueException{
        int entityDeclIndex = -1;
        if (fGrammar == null) {
            InvalidDatatypeValueException error = 
            new InvalidDatatypeValueException( "ERROR: ENTITYDatatype Validator: Failed Need to call initialize method with a valid Grammar reference" );//Need Message
            throw error;
        }

        fEntityDecl.clear();//Reset Entity Decl struct

        entityDeclIndex = fGrammar.getEntityDeclIndex( content );

        if (entityDeclIndex == -1) {
            fGrammar.getEntityDecl( entityDeclIndex, fEntityDecl );
            if (fEntityDecl.notation != null) {// not unparsed entity
                InvalidDatatypeValueException error = 
                new InvalidDatatypeValueException( "ENTITY '"+ content +"' is not unparsed" );
                throw error;
            }
        } else {
            InvalidDatatypeValueException error = 
            new InvalidDatatypeValueException( "ENTITY '"+ content +"' is not valid" );
            throw error;
        }
    }

    /**
     * A no-op method in this Datatype
     */
    public void validate(){
    }



    /**
     * <P>Initializes internal Grammar reference
     * This method is unique to ENTITYDatatypeValidator.</P>
     * <P>This method should  be called before calling the
     * validate method</P>
     * 
     * @param grammar
     */
    public void initialize( Object grammar ) {
        //System.out.println("ENTITYDatatypeValidator initialized" );
        fGrammar = (Grammar) grammar;
    }

    /**
     * REVISIT
     * Compares two Datatype for order
     * 
     * @return 
     */
    public int compare( String  content1, String content2) {
        return -1;
    }

    public Hashtable getFacets() {
        return null;
    }

    // Private methods start here

    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


   /**
    * A no-op method in this validator
    */
    public Object getInternalStateInformation() {
    return null;
    }

    /**
     * 
     * @param base   the validator for this type's base type
     */
    private void setBasetype(DatatypeValidator base) {
        fBaseValidator = base;
    }



}
