/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

import org.apache.xerces.utils.QName;

/**
 * InsertableElementsInfo is a simple 'data packet' class that is used to
 * get information into and out of the validator APIs that allow you to ask
 * what kind of elements can be inserted into a particular place in an
 * element's content model.
 * <p>
 * The parent element is not explicitly stored here, since it is a separate
 * parameter to the methods that do the query.
 * <p>
 * Since it exists purely to exchange data, it just uses simple public
 * data members.
 *
 * @version $Id$
 */
public class InsertableElementsInfo {
    
    //
    // Data
    //
    
    /**
     * One of the things that could be inserted here is a PCDATA node,
     * in addition to the element type nodes reported.
     */
    public boolean canHoldPCData;

    /**
     * The count of elements in the curChildren array. The array can be
     * larger than this (for reuse purposes), so this value indicates
     * how many elements are valid.
     * <p>
     * Note that, since the curChildren array must have an empty slot at
     * the insertion index, this value can never be zero.
     * <p>
     * Note also that this value can be changed during processing, though
     * its value on return is meaningless to the caller.
     */
    public int childCount;
    
    /**
     * The current list of children of the parent element. This may or
     * may not be the <em>real</em> list of children, since the caller can lie
     * but that's of no concern to the validator. These query APIs are
     * intended to be for 'what if' kind of work, so any list of children
     * could be passed in.
     * <p>
     * There must be an empy slot in the array at the requested insertion
     * point. That slot does not have to have any particular value, but
     * it will be used by the validator to do brute force validation in
     * some cases when a 'fully valid' check is done for valid insertable
     * elements.
     * <p>
     * Note that this array can be modified by the call, so do not expect
     * its contents to remain the same as on input.
     */
    public QName curChildren[];

    /**
     * Indicates that one of the valid things after the insert point is
     * 'end of content', which means that the element being inserted
     * after can legally be the last element.
     */
    public boolean isValidEOC;
    
    /**
     * The insertion point. The question is 'what can go here' and this
     * indicates where 'where' is. It is an offset into curChildren.
     */
    public int insertAt;
    
    /**
     * This array is filled with flags that indicate what the possible
     * insertable elements are (i.e. the list of unique elements that
     * could possibly be inserted somewhere in this type of element.)
     * Effectively this is the list of unique children in the content
     * model of the parent element.
     * <p>
     * If this array is not big enough to hold the results, or is null,
     * then it will be replaced with a new array of the correct size.
     */
    public QName possibleChildren[];
    
    /**
     * The number of elements that are valid in the possibleChildren
     * and resultsCount arrays. They can be larger than that, so there
     * must be a way to indicate how many elements are filled in with
     * value results. If they were not at least this large on input,
     * then they will be reallocated up to this size.
     */
    public int resultsCount;
    
    /**
     * This array must be at least as large as possibleChildren since
     * a flag is set in the same indexes in this array to indicate that
     * the possible child at that index in possibleChildren can be
     * inserted at the requested insertion point.
     * <p>
     * If this array is not big enough to hold the results, or is null,
     * then it will be replaced with a new array of the correct size.
     */
    public boolean results[];
    
} // class InsertableElementsInfo
