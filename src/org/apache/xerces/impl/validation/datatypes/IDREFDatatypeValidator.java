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
//import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
//import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;
import org.apache.xerces.util.XMLChar;
import java.util.NoSuchElementException;
/**
 * <P>IDREFDatatypeValidator - represents the IDREFS
 * attribute type from XML 1.0 recommendation. The
 * Value Space of IDREF is the set of all strings
 * that match the NCName production and have been
 * used in an XML Document as the value of an element 
 * or attribute of Type ID. The Lexical space of
 * IDREF is the set of strings that match the NCName
 * production.</P>
 * <P>The Value space of IDREF is scoped to a specific
 * instance document</P>
 * <P>This datatatype checks the following constraint:
 * An IDREF must match the value of an ID in the XML
 * document in which it occurs.
 * </P>
 * The following snippet shows typical use of the
 * the IDDatatype:</P>
 * <CODE>
 * <PRE>
 *       DatatypeValidator idRefData = tstRegistry.getDatatypeValidator("IDREF" );
 *       if( idRefData != null ){
 *          IDREFDatatypeValidator refData = (IDREFDatatypeValidator) idRefData;
 *          refData.initialize( ((IDDatatypeValidator) idData).getTableIds());
 *          try {
 *             refData.validate( "a1", null );
 *             refData.validate( "a2", null );
 *             //refData.validate( "a3", null );//Should throw exception at validate()
 *             refData.validate();
 *          } catch( Exception ex ){
 *             ex.printStackTrace();
 *          }
 *       }
 *       </PRE>
 * </CODE>
 * 
 * @author Jeffrey Rodriguez-
 * @version $Id$
 * @see org.apache.xerces.impl.validation.datatypes.IDDatatypeValidator
 * @see org.apache.xerces.impl.validation.datatypes.AbstractDatatypeValidator
 * @see org.apache.xerces.impl.validation.DatatypeValidator
 */
public class IDREFDatatypeValidator extends AbstractDatatypeValidator
implements StatefullDatatypeValidator{
   private DatatypeValidator fBaseValidator    = null;
   private Hashtable              fTableOfId   = null; //This is pass to us through the state object
   private Hashtable              fTableIDRefs = null;
   private Object                   fNullValue = null;
   private Locale            fLocale           = null;
   private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();


   public IDREFDatatypeValidator () throws InvalidDatatypeFacetException {
      this( null, null, false ); // Native, No Facets defined, Restriction
   }

   public IDREFDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                                   boolean derivedByList ) throws InvalidDatatypeFacetException { 

      setBasetype( base ); // Set base type 

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
   public void validate(String content, Object state ) throws InvalidDatatypeValueException{
      //Pass content as a String
      if (!XMLChar.isValidName(content)) {//Check if is valid key
         InvalidDatatypeValueException error = new InvalidDatatypeValueException( content );//Need Message
         error.setKeyIntoReporter( "IDREFInvalid" );
         throw error;//Need Message
      }
      addIdRef( content, state);// We are storing IDs 
   }

   /**
    * <P>This method is unique to IDREFDatatypeValidator</P>
    * <P>Validator should call this method at the EndDocument
    * call to start IDREF constraint validation. This validation
    * rule checks IDREF values accumulated in internal
    * table against read table passed to IDREF validator
    * at instantiation time.</P>
    * <P>Caveats -
    * <LI>
    * Do not call this validator method until
    * you are sure that all ID values have been found since
    * this method contains a live reference to an internal
    * ID table which the ID validator could still be
    * updating.</LI>
    * <LI>Do not call this method before the initialize method
    * since the initialize method will set the reference
    * to ID table used by this method to validate the
    * IDREFs.</LI></P>
    * 
    * @exception InvalidDatatypeValueException
    */
   public void validate() throws InvalidDatatypeValueException{
      checkIdRefs();
   }


   /**
    * <P>This method is unique to IDREFDatatypeValidator</P>
    * <P>This method initializes the internal reference
    * to the ID table of ID's and IDREF internal table
    * of IDREFs.</P>
    * <P>This method should be called before the valid()
    * method</P>
    * 
    * @param tableOfIDs
    */
   public void initialize( Object tableOfIDs ){
      //System.out.println("IDREF datatype initialized" );
         
      if ( this.fTableIDRefs != null) {
         this.fTableIDRefs.clear();
      } else {
        this.fTableIDRefs = new Hashtable();
      }
      fTableOfId = (Hashtable) tableOfIDs; //set reference to table of Ids.
   }



   /**
    * REVISIT
    * Compares two Datatype for order
    * 
    * @param o1
    * @param o2
    * @return 
    */
   public int compare( String content1, String content2){
      return -1;
   }


   public Hashtable getFacets(){
      return null;
   }
   /**
      * Returns a copy of this object.
      */
   public Object clone() throws CloneNotSupportedException {
      throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
   }

   /**
     * Name of base type as a string.
     * A Native datatype has the string "native"  as its
     * base type.
     * 
     * @param base   the validator for this type's base type
     */
   private void setBasetype(DatatypeValidator base){
      fBaseValidator = base;
   }

   /** addId. */
   private void addIdRef(String content, Object state) {
      if ( fTableOfId != null &&  fTableOfId.containsKey( content ) ) {
         return;
      }
      if ( fTableIDRefs == null ) {
         fTableIDRefs = new Hashtable();
      } else if ( fTableIDRefs.containsKey( content ) ) {
         return;
      }
      if ( this.fNullValue == null ) {
         fNullValue = new Object();
      }
      try {
         this.fTableIDRefs.put( content, fNullValue ); 
      } catch ( OutOfMemoryError ex ) {
         System.out.println( "Out of Memory: Hashtable of ID's has " + this.fTableIDRefs.size() + " Elements" );
         ex.printStackTrace();
      }
   } // addId(int):boolean


   /**
    * <P>Private method used to check the IDREF valid
    * ID constraint</P>
    * 
    * @exception InvalidDatatypeValueException
    */
   private void checkIdRefs() throws InvalidDatatypeValueException {

      if ( fTableIDRefs == null)
         return;

      Enumeration en = this.fTableIDRefs.keys();

      while (en.hasMoreElements()) {
         String key = (String)en.nextElement();
         if ( this.fTableOfId == null || ! this.fTableOfId.containsKey(key)) {

            InvalidDatatypeValueException error =  new
                                                   InvalidDatatypeValueException( key );
            error.setKeyIntoReporter("MSG_ELEMENT_WITH_ID_REQUIRED" );
            throw error;
         }
      }

   } // checkIdRefs()


   /**
   * set the locate to be used for error messages
   */
   public void setLocale(Locale locale) {
      fLocale = locale;
   }

   /**
    * A no-op method in this validator
    */
   public Object getInternalStateInformation(){
       return null;
   }

   private String getErrorString(int major, int minor, Object args[]) {
      //return fMessageProvider.createMessage(fLocale, major, minor, args);
      return fMessageProvider.formatMessage( fLocale, null, null);
   }


}

