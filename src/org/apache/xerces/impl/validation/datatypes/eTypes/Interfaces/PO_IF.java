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

package org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces;



/**
 * Methods for partial orders
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public interface PO_IF extends Property {
   /* These are here to assist in implementing classes which extend this interface. */
   final public static int MAX = 0;
   final public static int MIN = 1;
   final public static int CLOSED_ABOVE = 2;
   final public static int CLOSED_BELOW = 3;
   final public static int classNumberSubProperties = 4;
   public final static class AboveBelow {
      private String value;
      int v;
      static int next = 0;
      AboveBelow(String s){
         value = s;
         v = next++;
      }
      public int toInt(){ return v;}
      public String toString() { return value;}
   }
   public final static AboveBelow above = new AboveBelow("above");
   public final static AboveBelow below = new AboveBelow("below");

   /* *  <it>compareTo(obj) != null &amp;&amp; compareTo(obj).intValue()==0 </it> *
       public boolean                       valueEquals(Object obj);*/
   /**
    *  Returns null if <it>this &amp; other</it> are not comparable.  Otherwise Integer which is
    * <it>&lt;0</it>, <it>==0</it> , or <it>&gt;0</it> according to whether
    * <br><it>this&lt;other</it>, <it>this==other</it> , or <it>this&gt;other</it>
    * @return java.lang.Integer
    * @param other com.ibm.eTypes.Interfaces.PO_IF
    */
   public Integer                      compareTo(Object other);
   public Object                        getBound(PO_IF.AboveBelow ab, Integer type );
   /** Greatest lower bound of <it>this</it> and <it>other</it> (or null )
    */
   public Object                        glb(Object right);
   /** Returns false if the argument is not an instance of the implementing class.
    *	Returns true if
    *  <it>compareTo(right).intValue()&gt;0</it>
    */
   public boolean                       gt(Object right);
   /** If true, a largest permissible value exists.  */
   public boolean                       isBounded(AboveBelow ab);
   /** If true, a value equal to a bound may be valid.  */
   public boolean                       isClosed(AboveBelow ab);
   /** Returns false if the argument is not an instance of the implementing class.
    *	Returns true if 
    *  <it>compareTo(right).intValue()&lt;0</it>
    */
   public boolean                       lt(Object right);
   /** Least upper bound of <it>this</it> and <it>other</it> (or null )
    */
   public Object                        lub(Object other);
   /** Letting <it>o==null</it> clears the bound
    */
   void                                 setBound(AboveBelow ab, Object o, Integer type);
}
