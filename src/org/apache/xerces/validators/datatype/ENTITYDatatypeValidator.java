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

package org.apache.xerces.validators.datatype;

import java.util.Hashtable;
import java.util.Vector;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.schema.SchemaSymbols;


/**
 * ENTITYDatatypeValidator implements the
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
 * instance document.
 *
 * Some caveats:
 *
 * Because of the Xerces Architecture, where all
 * symbols are stored in a StringPool and Strings
 * are referenced by int then this datatype needs
 * to know about StringPool.
 * The first time that this datatype is invoked
 * we pass a message containing 2 references needed
 * by this validator:
 * - a reference to the DefaultEntityHandler  used
 * by the XMLValidator.
 * - a reference to the StringPool.
 *
 *
 * This validator extends also the XML1.0 validation
 * provided in DTD by providing "only on Schemas"
 * facet validation.
 * This validator also embodies the Derived datatype
 * ENTITIES which is an ENTITY derived by list.
 *
 * These validators can be supplied by the application writer and may be useful as
 * standalone code as well as plugins to the validator architecture.
 *
 * @author Jeffrey Rodriguez-
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 * @see org.apache.xerces.validators.datatype.DatatypeValidator
 * @see org.apache.xerces.validators.datatype.DatatypeValidatorFactoryImpl
 * @see org.apache.xerces.validators.datatype.DatatypeValidatorFactory
 * @see org.apache.xerces.validators.common.XMLValidator
 */
public class ENTITYDatatypeValidator extends StringDatatypeValidator {

    public static final  int                ENTITY_VALIDATE  = 0;

    public ENTITYDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public ENTITYDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                     boolean derivedByList  ) throws InvalidDatatypeFacetException {

        // all facets are handled in StringDatatypeValidator
        super (base, facets, derivedByList);

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        setTokenType(SPECIAL_TOKEN_ENTITY);
    }

    /**
     * return value of whiteSpace facet
     */
    public short getWSFacet(){
        return COLLAPSE;
    }

    /**
     * Checks that "content" string is valid
     * datatype.
     * If invalid a Datatype validation exception is thrown.
     *
     * @param content A string containing the content to be validated
     * @param derivedBylist
     *                Flag which is true when type
     *                is derived by list otherwise it
     *                it is derived by extension.
     *
     * @exception throws InvalidDatatypeException if the content is
     *                   invalid according to the rules for the validators
     * @exception InvalidDatatypeValueException
     * @see         org.apache.xerces.validators.datatype.InvalidDatatypeValueException
     */
    public Object validate(String content, Object state ) throws InvalidDatatypeValueException{
        // use StringDatatypeValidator to validate content against facets
        super.validate(content, state);

        StateMessageDatatype message = (StateMessageDatatype) state;
        if (message != null && message.getDatatypeState() == ENTITY_VALIDATE) {
            Object[] params = (Object[])message.getDatatypeObject();
            DefaultEntityHandler entityHandler = (DefaultEntityHandler)params[0];
            StringPool stringPool = (StringPool)params[1];

            int attValueHandle = stringPool.addSymbol( content );
            if (!entityHandler.isUnparsedEntity( attValueHandle ) ) {
                InvalidDatatypeValueException error =
                new InvalidDatatypeValueException( "ENTITY '"+ content +"' is not valid" );
                error.setMinorCode(XMLMessages.MSG_ENTITY_INVALID );
                error.setMajorCode(XMLMessages.VC_ENTITY_NAME);
                throw error;
            }
        }

        return null;
    }

    /**
     * REVISIT
     * Compares two Datatype for order
     *
     * @return
     */
    public int compare( String  content1, String content2){
        // TO BE DONE!!!
        return content1.equals(content2)?0:-1;
    }

    // Private methods start here

    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }
}
