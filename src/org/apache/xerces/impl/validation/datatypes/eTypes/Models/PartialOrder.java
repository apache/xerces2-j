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

package org.apache.xerces.impl.validation.datatypes.eTypes.Models;

import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.*;

import java.lang.CloneNotSupportedException;

/**
 * If the argument is not an instance of the implementing class,
 * the result is false for both gt and lt, and null for those functions
 * which return an object
 * <p>
 * To implement a concrete partial order, define the compareTo(PO_IF) method
 * of com.ibm.eTypes.Interfaces.PO_IF
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public abstract class PartialOrder extends AbstractProperty implements PO_IF {
		/** default has no bound.  Bound is a PO_IF  */
		final public static int MAX = AbstractProperty.classNumberSubProperties + PO_IF.MAX;
		/** default has no bound.  Bound is a PO_IF */
		final public static int MIN = AbstractProperty.classNumberSubProperties + PO_IF.MIN;
		/** default is that intervals are closed, i.e. endpoints are included.  */
		final public static int CLOSED_ABOVE = AbstractProperty.classNumberSubProperties + PO_IF.CLOSED_ABOVE;
		/** default is that intervals are closed, i.e. endpoints are included.  */
		final public static int CLOSED_BELOW = AbstractProperty.classNumberSubProperties + PO_IF.CLOSED_BELOW;
		final public static int classNumberSubProperties = 
				AbstractProperty.classNumberSubProperties + PO_IF.classNumberSubProperties;
		public PartialOrder() {
				this( PartialOrder.class , "Partial Order" );
		}
		/**
		 * Constructs a partial order with underlying representation jc
		 * and name str
		 */
		public PartialOrder(Class jc, String str ) {
				super( jc , str );
		}
		public abstract Integer compareTo(Object other);
		final public Object                                  getBound(PO_IF.AboveBelow ab, Integer type ){
				int boundId = ( ab == PO_IF.above ) ? MAX : MIN;
				return getSubProp( boundId , type); // subProp[ boundId ];
		}
		/** Default is closed */
		final public boolean                                getClosed(PO_IF.AboveBelow ab ){
				int boundId = ( ab == PO_IF.above ) ? CLOSED_ABOVE : CLOSED_BELOW;
				//				return ( subProp[ boundId ] == null ) ? true : ((Boolean)subProp[ boundId ]).booleanValue();
				return ( getSubProp( boundId  , Property.constraint ) == null ) ? true  :
						((Boolean)getSubProp( boundId , Property.constraint)).booleanValue();
		}
		public final Object glb(Object other) {
				Integer ii = compareTo(other);
				if (ii == null) {
						return null;
				}
				int i = ii.intValue();
				if (i <= 0) {
						return this;
				}
				return other;
		}
		public final boolean gt(Object other) {
				Integer ii = compareTo(other);
				if (ii == null) {
						return false;
				}
				int i = ii.intValue();
				if (i > 0) {
						return true;
				}
				return false;
		}
		/** Convenience method for getBound( ab , Property.constraint ); */
		final public boolean                              isBounded(PO_IF.AboveBelow ab){
				boolean x = getBound( ab , Property.constraint ) != null;
				return x;
		}
		/** Convenience method for getClosed( ab , Property.constraint ); */
		final public boolean                              isClosed(PO_IF.AboveBelow ab){
				boolean x = getClosed( ab );
				return x;
		}
		public final boolean lt(Object other) {
				Integer ii = compareTo(other);
				if (ii == null) {
						return false;
				}
				int i = ii.intValue();
				if (i < 0) {
						return true;
				}
				return false;
		}
		public final Object lub(Object other) {
				Integer ii = compareTo(other);
				if (ii == null) {
						return null;
				}
				int i = ii.intValue();
				if (i >= 0) {
						return this;
				}
				return other;
		}
		/** this is the accumulator, scratch is the result of checking an instance. */
		public void merge(Property scratch){
				merge( this , (PO_IF)scratch , Property.classNumberSubProperties );
		}
		static public void merge(PartialOrder accumulator, PO_IF scratchProp, int offset){
				Integer mm;
				AbstractProperty scratch = (AbstractProperty) scratchProp ;
				if ( accumulator . getSubProp( MAX , Property.accumulate ) == null ){
						accumulator . setSubProp( scratch . getSubProp( MAX , Property.instance), MAX , Property.accumulate);
				}
				else {
						mm = ((PO_IF) accumulator . getSubProp( MAX , Property.accumulate)) .
								compareTo( (PO_IF) scratch . getSubProp( MAX , Property.instance ) );
						if ( mm != null && mm . intValue() < 0 ){
								accumulator . setSubProp( scratch . getSubProp( MAX , Property.instance ) , MAX , Property.accumulate );
						}
				}
				if ( accumulator . getSubProp( MIN , Property.accumulate) == null ){
						accumulator . setSubProp( scratch . getSubProp( MIN , Property.instance ), MIN , Property.accumulate);
				}
				else {
						mm = ((PO_IF) accumulator . getSubProp( MIN, Property.accumulate )) . 
								compareTo( (PO_IF) scratch . getSubProp( MIN , Property.instance ) );
						if ( mm != null && mm . intValue() > 0 ){
								accumulator . setSubProp( scratch . getSubProp( MIN  , Property.instance) , MIN , Property.accumulate);
						}
				}
		}
		/** Note that setting o == null, clears bound */
		public void setBound(PO_IF.AboveBelow ab, Object o, Integer type){
				int boundId = ( ab == PO_IF.above ) ? MAX : MIN;
				setSubProp( o , boundId , type );//				subProp[ boundId ] = o;
		}
		public void                             setClosed(PO_IF.AboveBelow ab, boolean b){
				int boundId = ( ab == PO_IF.above ) ? CLOSED_ABOVE : CLOSED_BELOW;
				setSubProp( new Boolean( b ) , boundId , Property.constraint );//				subProp[ boundId ] = new Boolean( b );
		}
}
