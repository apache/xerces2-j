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

package org.apache.xerces.impl.validation.datatypes.eTypes.xml;



import org.apache.xerces.impl.validation.datatypes.eTypes.Models.AbstractProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.Property;

import java.util.Enumeration;
import java.util.Vector;

/**
 * valid if each parent XModelIF has an object of this type
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class XMLRequired implements Property {
   //XModelIF xm;
   public boolean valid = false;
/**
 * accumulate method comment.
 */
   public boolean accumulate(java.lang.Object dt) {
      return false;
   }
   public Object clone()  {
      Object obj = null;
      try {
         obj = super.clone();
      } catch (CloneNotSupportedException e) {
         ;
      }
      return obj;
   }
/**
 * getName method comment.
 */
   public java.lang.String getName() {
      return null;
   }
/**
 * 
 * @return com.ibm.DDbEv2.InterfacesEvents.XModelIF
 */
   //public final XModelIF getXm() {
   // return xm;
   // }
/**
 * 
 * @return boolean
 */
   public final boolean isValid() {
      return valid;
   }
/**
 * merge method comment.
 */
   public void merge() {}
   /** XML property merge is not called
   */
   public void merge( Property scratchProp ){
      //Assert . isTrue(false, "This should not be called " );;
   }
/**
 * 
 * @param newValid boolean
 */
   public final void setValid(boolean newValid) {
      valid = newValid;
   }
/**
 * 
 * @param newXm com.ibm.DDbEv2.InterfacesEvents.XModelIF
 */

//   public final void setXm(XModelIF newXm) {
   //    xm = newXm;
   // }
   public Property twin(){
      XMLFixed obj = null;
      try {
         obj = (XMLFixed) super.clone();
      } catch (CloneNotSupportedException e) {
         ;
      }
      /*
      obj.xm = xm;
      obj.xm = null;
      obj.valid = false;
      */
      return obj;
   }        
/**
 * If getFixed(), returns true if all non-null instances associated with xm are equal,
   If getRequired() returns true if all instances associated with xm are non-null
 */
   public boolean validate(java.lang.Object dt) {
      //Enumeration e  = getXm() . getAllInstances();
      //Vector v = Perl . e2v( e );
      Vector  v = new Vector();
      Object instance0, x = null;
      setValid( true );
      /*
      while ( e . hasMoreElements() ) {
         if ( e . nextElement() == null ) {
            setValid( false );
            break;
         }
      }
      */
      return isValid();
   }
}
