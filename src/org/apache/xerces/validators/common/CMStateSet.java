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

import org.apache.xerces.utils.ImplementationMessages;

/**
 * This class is a very simple bitset class. The DFA content model code needs
 * to support a bit set, but the java BitSet class is way, way overkill. Our
 * bitset never needs to be expanded after creation, hash itself, etc...
 *
 * Since the vast majority of content models will never require more than 64
 * bits, and since allocation of anything in Java is expensive, this class
 * provides a hybrid implementation that uses two ints for instances that use
 * 64 bits or fewer. It has a byte array reference member which will only be
 * used if more than 64 bits are required.
 *
 * Note that the code that uses this class will never perform operations
 * on sets of different sizes, so that check does not have to be made here.
 *
 * @version
 */
class CMStateSet
{
    // -------------------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------------------
    CMStateSet(int bitCount) throws CMException
    {
        // Store the required bit count and insure its legal
        fBitCount = bitCount;
        if (fBitCount < 0)
            throw new CMException(ImplementationMessages.VAL_CMSI);

        //
        //  See if we need to allocate the byte array or whether we can live
        //  within the 64 bit high performance scheme.
        //
        if (fBitCount > 64)
        {
            fByteCount = fBitCount / 8;
            if (fBitCount % 8 != 0)
                fByteCount++;
            fByteArray = new byte[fByteCount];
        }

        // Init all the bits to zero
        zeroBits();
    }


    // -------------------------------------------------------------------
    //  Public inherited methods
    // -------------------------------------------------------------------
    public String toString()
    {
        StringBuffer strRet = new StringBuffer();
        try
        {
            strRet.append("{");
            for (int index = 0; index < fBitCount; index++)
            {
                if (getBit(index))
                    strRet.append(" " + index);
            }
            strRet.append(" }");
        }

        catch(CMException exToCatch)
        {
            //
            //  We know this won't happen but we have to catch it to avoid it
            //  having to be in our 'throws' list.
            //
        }
        return strRet.toString();
    }


    // -------------------------------------------------------------------
    //  Package final methods
    // -------------------------------------------------------------------
    final void intersection(CMStateSet setToAnd)
    {
        if (fBitCount < 65)
        {
            fBits1 &= setToAnd.fBits1;
            fBits2 &= setToAnd.fBits2;
        }
         else
        {
            for (int index = fByteCount - 1; index >= 0; index--)
                fByteArray[index] &= setToAnd.fByteArray[index];
        }
    }

    final boolean getBit(int bitToGet) throws CMException
    {
        if (bitToGet >= fBitCount)
            throw new CMException(ImplementationMessages.VAL_CMSI);

        if (fBitCount < 65)
        {
            final int mask = (0x1 << (bitToGet % 32));
            if (bitToGet < 32)
                return (fBits1 & mask) != 0;
            else
                return (fBits2 & mask) != 0;
        }
         else
        {
            // Create the mask and byte values
            final byte mask = (byte)(0x1 << (bitToGet % 8));
            final int ofs = bitToGet >> 3;

            // And access the right bit and byte
            return ((fByteArray[ofs] & mask) != 0);
        }
    }

    final boolean isEmpty()
    {
        if (fBitCount < 65)
        {
            return ((fBits1 == 0) && (fBits2 == 0));
        }
         else
        {
            for (int index = fByteCount - 1; index >= 0; index--)
            {
                if (fByteArray[index] != 0)
                    return false;
            }
        }
        return true;
    }

    final boolean isSameSet(CMStateSet setToCompare)
    {
        if (fBitCount != setToCompare.fBitCount)
            return false;

        if (fBitCount < 65)
        {
            return ((fBits1 == setToCompare.fBits1)
            &&      (fBits2 == setToCompare.fBits2));
        }

        for (int index = fByteCount - 1; index >= 0; index--)
        {
            if (fByteArray[index] != setToCompare.fByteArray[index])
                return false;
        }
        return true;
    }

    final void union(CMStateSet setToOr)
    {
        if (fBitCount < 65)
        {
            fBits1 |= setToOr.fBits1;
            fBits2 |= setToOr.fBits2;
        }
         else
        {
            for (int index = fByteCount - 1; index >= 0; index--)
                fByteArray[index] |= setToOr.fByteArray[index];
        }
    }

    final void setBit(int bitToSet) throws CMException
    {
        if (bitToSet >= fBitCount)
            throw new CMException(ImplementationMessages.VAL_CMSI);

        if (fBitCount < 65)
        {
            final int mask = (0x1 << (bitToSet % 32));
            if (bitToSet < 32)
            {
                fBits1 &= ~mask;
                fBits1 |= mask;
            }
             else
            {
                fBits2 &= ~mask;
                fBits2 |= mask;
            }
        }
         else
        {
            // Create the mask and byte values
            final byte mask = (byte)(0x1 << (bitToSet % 8));
            final int ofs = bitToSet >> 3;

            // And access the right bit and byte
            fByteArray[ofs] &= ~mask;
            fByteArray[ofs] |= mask;
        }
    }

    final void setTo(CMStateSet srcSet) throws CMException
    {
        // They have to be the same size
        if (fBitCount != srcSet.fBitCount)
            throw new CMException(ImplementationMessages.VAL_CMSI);
        
        if (fBitCount < 65)
        {
            fBits1 = srcSet.fBits1;
            fBits2 = srcSet.fBits2;
        }
         else
        {
            for (int index = fByteCount - 1; index >= 0; index--)
                fByteArray[index] = srcSet.fByteArray[index];
        }
    }

    final void zeroBits()
    {
        if (fBitCount < 65)
        {
            fBits1 = 0;
            fBits2 = 0;
        }
         else
        {
            for (int index = fByteCount - 1; index >= 0; index--)
                fByteArray[index] = 0;
        }
    }


    // -------------------------------------------------------------------
    //  Private data members
    //
    //  fBitCount
    //      The count of bits that the outside world wants to support,
    //      so its the max bit index plus one.
    //
    //  fByteCount
    //      If the bit count is > 64, then we use the fByteArray member to
    //      store the bits, and this indicates its size in bytes. Otherwise
    //      its value is meaningless.
    //
    //  fBits1
    //  fBits2
    //      When the bit count is < 64 (very common), these hold the bits.
    //      Otherwise, the fByteArray member holds htem.
    // -------------------------------------------------------------------
    int         fBitCount;
    int         fByteCount;
    int         fBits1;
    int         fBits2;
    byte[]      fByteArray;
};
