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

package org.apache.xerces.validators.schema;

import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.common.Grammar;
import org.apache.xerces.validators.common.XMLContentModel;
import org.apache.xerces.validators.common.InsertableElementsInfo;
import org.apache.xerces.validators.datatype.DatatypeValidator;
import org.apache.xerces.validators.datatype.InvalidDatatypeValueException;
import org.apache.xerces.utils.QName;

/**
 * DatatypeContentModel provides a content model that knows
 * how to check content against datatypes
 * @version $Id:
 */
public class DatatypeContentModel implements XMLContentModel
{
    TraverseSchema.DatatypeValidatorRegistry fDatatypeRegistry = null;
    Grammar fGrammar = null;
    StringPool fStringPool = null;
    int fChild = -1;

    // -----------------------------------------------------------------------
    //  Constructors
    // -----------------------------------------------------------------------

    /**
     */
    public DatatypeContentModel(  TraverseSchema.DatatypeValidatorRegistry reg,
                                  Grammar grammar,
                                  StringPool stringPool,
                                  int childIndex)
    {
        fDatatypeRegistry = reg;
        fGrammar = grammar;
        fStringPool = stringPool;
        fChild = childIndex;
    }


    // -----------------------------------------------------------------------
    //  Public, inherited methods
    // -----------------------------------------------------------------------
    
    /**
     * Check that the specified content is valid according to this
     * content model. This method can also be called to do 'what if' 
     * testing of content models just to see if they would be valid.
     * <p>
     * A value of -1 in the children array indicates a PCDATA node. All other 
     * indexes will be positive and represent child elements. The count can be
     * zero, since some elements have the EMPTY content model and that must be 
     * confirmed.
     *
     * @param childCount The number of entries in the <code>children</code> array.
     * @param children The children of this element.  Each integer is an index within
     *                 the <code>StringPool</code> of the child element name.  An index
     *                 of -1 is used to indicate an occurrence of non-whitespace character
     *                 data.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then the specified children are valid but additional
     *         content is required to reach a valid ending state.
     *
     * @exception Exception Thrown on error.
     */
    public int validateContent(int childCount, QName[] children) throws Exception
    {
        boolean DEBUG_DATATYPES = false;
/*
        if (DEBUG_DATATYPES) {
            System.out.println("Checking content of datatype");
            String strTmp = fStringPool.toString(elementTypeIndex);
            int contentSpecIndex = fElementDeclPool.getContentSpec(elementIndex);
            XMLContentSpec csn = new XMLContentSpec();
            fElementDeclPool.getContentSpecNode(contentSpecIndex, csn);
            String contentSpecString = fStringPool.toString(csn.value);
            System.out.println
            (
                "Name: "
                + strTmp
                + ", Count: "
                + childCount
                + ", ContentSpec: "
                + contentSpecString
            );
            for (int index = 0; index < childCount && index < 10; index++) {
                if (index == 0) System.out.print("  (");
                String childName = (children[index] == -1) ? "#PCDATA" : fStringPool.toString(children[index]);
                if (index + 1 == childCount)
                    System.out.println(childName + ")");
                else if (index + 1 == 10)
                    System.out.println(childName + ",...)");
                else
                    System.out.print(childName + ",");
            }
        }
*/
        try { // REVISIT - integrate w/ error handling
            XMLContentSpec cs = new XMLContentSpec();
	    fGrammar.getContentSpec(fChild,cs);
            String type = fStringPool.toString(cs.value);
            DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
            if (v != null) 
                v.validate(fStringPool.toString(children[0].localpart));
            else
                System.out.println("No validator for datatype "+type);
        } catch (InvalidDatatypeValueException idve) {
            throw idve;
            // System.out.println("Incorrect datatype: "+idve.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Internal error in datatype validation");
        }
        return -1;
/*
        //
        //  According to the type of operation, we do the correct type of
        //  content check.
        //
        switch(fOp)
        {
            case XMLContentSpec.CONTENTSPECNODE_LEAF :
                // If there is not a child, then report an error at index 0
                if (childCount == 0)
                    return 0;

                // If the 0th child is not the right kind, report an error at 0
                if (children[0] != fFirstChild)
                    return 0;

                // If more than one child, report an error at index 1
                if (childCount > 1)
                    return 1;
                break;

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE :
                //
                //  If there is one child, make sure its the right type. If not,
                //  then its an error at index 0.
                //
                if ((childCount == 1) && (children[0] != fFirstChild))
                    return 0;

                //
                //  If the child count is greater than one, then obviously
                //  bad, so report an error at index 1.
                //
                if (childCount > 1)
                    return 1;
                break;

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE :
                //
                //  If the child count is zero, that's fine. If its more than
                //  zero, then make sure that all children are of the element
                //  type that we stored. If not, report the index of the first
                //  failed one.
                //
                if (childCount > 0)
                {
                    for (int index = 0; index < childCount; index++)
                    {
                        if (children[index] != fFirstChild)
                            return index;
                    }
                }
                break;

            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE :
                //
                //  If the child count is zero, that's an error so report
                //  an error at index 0.
                //
                if (childCount == 0)
                    return 0;

                //
                //  Otherwise we have to check them all to make sure that they
                //  are of the correct child type. If not, then report the index
                //  of the first one that is not.
                //
                for (int index = 0; index < childCount; index++)
                {
                    if (children[index] != fFirstChild)
                        return index;
                }
                break;

            case XMLContentSpec.CONTENTSPECNODE_CHOICE :
                //
                //  There must be one and only one child, so if the element count
                //  is zero, return an error at index 0.
                //
                if (childCount == 0)
                    return 0;

                // If the zeroth element isn't one of our choices, error at 0
                if ((children[0] != fFirstChild) && (children[0] != fSecondChild))
                    return 0;

                // If there is more than one element, then an error at 1
                if (childCount > 1)
                    return 1;
                break;

            case XMLContentSpec.CONTENTSPECNODE_SEQ :
                //
                //  There must be two children and they must be the two values
                //  we stored, in the stored order.
                //
                if (childCount == 2) {
                    if (children[0] != fFirstChild)
                        return 0;

                    if (children[1] != fSecondChild)
                        return 1;
                }
                else {
                    if (childCount > 2) {
                        return 2;
                    }

                    return childCount;
                }

                break;

            default :
                throw new CMException(ErrorCode.VAL_CST);
        }

        // We survived, so return success status
        return -1;
*/
    }

    /**
     * Returns information about which elements can be placed at a particular point
     * in the passed element's content model.
     * <p>
     * Note that the incoming content model to test must be valid at least up to
     * the insertion point. If not, then -1 will be returned and the info object
     * will not have been filled in.
     * <p>
     * If, on return, the info.isValidEOC flag is set, then the 'insert after'
     * element is a valid end of content. In other words, nothing needs to be
     * inserted after it to make the parent element's content model valid.
     *
     * @param fullyValid Only return elements that can be inserted and still
     *                   maintain the validity of subsequent elements past the
     *                   insertion point (if any).  If the insertion point is at
     *                   the end, and this is true, then only elements that can
     *                   be legal final states will be returned.
     * @param info An object that contains the required input data for the method,
     *             and which will contain the output information if successful.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed before the insertion point. If the value 
     *         returned is equal to the number of children, then the specified
     *         children are valid but additional content is required to reach a
     *         valid ending state.
     *
     * @see InsertableElementsInfo
     */
    public int whatCanGoHere(boolean                    fullyValid
                            , InsertableElementsInfo    info) throws Exception
    {
        return -1;
/*
        //
        //  For this one, having the empty slot at the insertion point is 
        //  a problem. So lets compress the array down. We know that it has
        //  to have at least the empty slot at the insertion point.
        //
        for (int index = info.insertAt; index < info.childCount; index++)
            info.curChildren[index] = info.curChildren[index+1];
        info.childCount--;
        
        //
        //  Check the validity of the existing contents. If this is less than
        //  the insert at point, then return failure index right now
        //
        final int failedIndex = validateContent(info.childCount, info.curChildren);
        if ((failedIndex != -1) && (failedIndex < info.insertAt))
            return failedIndex;

        // Set any stuff we can know right off the bat for all cases
        info.canHoldPCData = false;

        // See how many children we can possibly report
        if ((fOp == XMLContentSpec.CONTENTSPECNODE_LEAF)
        ||  (fOp == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE)
        ||  (fOp == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE)
        ||  (fOp == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE))
        {
            info.resultsCount = 1;
        }
         else if ((fOp == XMLContentSpec.CONTENTSPECNODE_CHOICE)
              ||  (fOp == XMLContentSpec.CONTENTSPECNODE_SEQ))
        {
            info.resultsCount = 2;
        }
         else
        {
            throw new CMException(ErrorCode.VAL_CST);
        }

        //
        //  If the outgoing arrays are too small or null, create new ones. These
        //  have to be at least the size of the results count.
        //
        if ((info.results == null) || (info.results.length < info.resultsCount))
            info.results = new boolean[info.resultsCount];

        if ((info.possibleChildren == null)
        ||  (info.possibleChildren.length < info.resultsCount))
        {
            info.possibleChildren = new int[info.resultsCount];
        }

        //
        //  Fill in the possible children array, and set all of the associated
        //  results entries to defaults of false.
        //
        info.possibleChildren[0] = fFirstChild;
        info.results[0] = false;
        if (info.resultsCount == 2)
        {
            info.possibleChildren[1] = fSecondChild;
            info.results[1] = false;
        }

        //
        //  Set some defaults so that it does not have to be done redundantly
        //  below in each case.
        //
        info.isValidEOC = false;

        //
        //  Now, for each spec type, lets do the grunt work required. Each of
        //  them is pretty simple, its just making sure of corner cases.
        //
        //  We know its valid up to the insert point at least and we know that
        //  the insert point is never past the number of children, so this releaves
        //  a lot of checking below.
        //
        switch(fOp)
        {
            case XMLContentSpec.CONTENTSPECNODE_LEAF :
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE :
                //
                //  If there are no current children, then insert at has to be
                //  zero, so we can have the one leaf element inserted here.
                //
                if (info.childCount == 0)
                {
                    info.results[0] = true;
                }
                 else if (info.childCount > 0)
                {
                    //
                    //  If the child count is greater than zero, then inserting
                    //  anything cannot be fully valid. But, if not fully valid
                    //  checking, it is ok as long as inserting at zero.
                    //
                    if (!fullyValid && (info.insertAt == 0))
                        info.results[0] = true;
                }

                if (fOp == XMLContentSpec.CONTENTSPECNODE_LEAF)
                {
                    // If the insert point is 1, then EOC is valid there
                    if (info.insertAt == 0)
                        info.isValidEOC = true;
                }
                 else
                {
                    // Its zero or one, so EOC is valid in either case
                    info.isValidEOC = true;
                }
                break;

            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE :
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE :
                //
                //  The one child is always possible to insert, regardless of
                //  where. The fully valid flag never comes into play since it
                //  cannot become invalid by inserting any number of new
                //  instances of the one element.
                //
                info.results[0] = true;

                //
                //  Its zero/one or more, so EOC is valid in either case but only
                //  after the 0th index for one or more.
                //
                if ((fOp == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE)
                ||  (info.insertAt > 0))
                {
                    info.isValidEOC = true;
                }
                break;

            case XMLContentSpec.CONTENTSPECNODE_CHOICE :
                //
                //  If the insert point is zero, then either of the two children
                //  can be inserted, unless fully valid is set and there are
                //  already any children.
                //
                if (info.insertAt == 0)
                {
                    if (!fullyValid && (info.childCount == 0))
                    {
                        info.results[0] = true;
                        info.results[1] = true;
                    }
                }

                // EOC is only valid at the end
                if (info.insertAt == 1)
                    info.isValidEOC = true;
                break;

            case XMLContentSpec.CONTENTSPECNODE_SEQ :
                //
                //  If the insert at is 0, then the first one valid. Else its
                //  the second one.
                //
                if (info.insertAt == 0)
                {
                    //
                    //  If fully valid check, then if there are two children,
                    //  it cannot be valid. If there is one child, it must be
                    //  equal to the second child of the pattern since it will
                    //  get pushed up (which means it was a pattern like (x|x)
                    //  which is kinda wierd.)
                    //
                    if (fullyValid)
                    {
                        if (info.childCount == 1)
                            info.results[0] = info.curChildren[0] == fSecondChild;
                    }
                     else
                    {
                        info.results[0] = true;
                    }
                }
                 else if (info.insertAt == 1)
                {
                    // If fully valid, then there cannot be two existing children
                    if (!fullyValid || (info.childCount == 1))
                        info.results[1] = true;
                }

                // EOC is only valid at the end
                if (info.insertAt == 2)
                    info.isValidEOC = true;
                break;

            default :
                throw new CMException(ErrorCode.VAL_CST);
        }

        // We survived, so return success status
        return -1;
  */
    }
  };
