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

package org.apache.xerces.utils;

import java.lang.*;


/**
 * This class provides encode/decode for RFC 2045 Base64 as
 * defined by RFC 2045, N. Freed and N. Borenstein.
 * RFC 2045: Multipurpose Internet Mail Extensions (MIME)
 * Part One: Format of Internet Message Bodies. Reference
 * 1996 Available at: http://www.ietf.org/rfc/rfc2045.txt
 * This class is used by XML Schema binary format validation
 *
 * @author Jeffrey Rodriguez
 * @version     $Id$
 */

public final class  Base64 {
    static private final int  BASELENGTH         = 255;
    static private final int  LOOKUPLENGTH       = 64;
    static private final int  TWENTYFOURBITGROUP = 24;
    static private final int  EIGHTBIT           = 8;
    static private final int  SIXTEENBIT         = 16;
    static private final int  SIXBIT             = 6;
    static private final int  FOURBYTE           = 4;
    static private final int  SIGN               = -128;
    static private final byte PAD                = ( byte ) '=';
    static private final boolean fDebug          = false;
    static private byte [] base64Alphabet       = new byte[BASELENGTH];
    static private byte [] lookUpBase64Alphabet = new byte[LOOKUPLENGTH];


    static {

        for (int i = 0; i<BASELENGTH; i++ ) {
            base64Alphabet[i] = -1;
        }
        for ( int i = 'Z'; i >= 'A'; i-- ) {
            base64Alphabet[i] = (byte) (i-'A');
        }
        for ( int i = 'z'; i>= 'a'; i--) {
            base64Alphabet[i] = (byte) ( i-'a' + 26);
        }

        for ( int i = '9'; i >= '0'; i--) {
            base64Alphabet[i] = (byte) (i-'0' + 52);
        }

        base64Alphabet['+']  = 62;
        base64Alphabet['/']  = 63;

        for (int i = 0; i<=25; i++ )
            lookUpBase64Alphabet[i] = (byte) ('A'+i );

        for (int i = 26,  j = 0; i<=51; i++, j++ )
            lookUpBase64Alphabet[i] = (byte) ('a'+ j );

        for (int i = 52,  j = 0; i<=61; i++, j++ )
            lookUpBase64Alphabet[i] = (byte) ('0' + j );
        lookUpBase64Alphabet[62] = (byte) '+';
        lookUpBase64Alphabet[63] = (byte) '/';

    }

    protected static boolean isWS (byte octect) {
        return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
    }

    protected static boolean isPad (byte octect) {
        return (octect == PAD);
    }

    protected static boolean isData (byte octect) {
        return (base64Alphabet[octect] != -1);
    }

    public static boolean isBase64( String isValidString ){
        return( isArrayByteBase64( isValidString.getBytes()));
    }


    public static boolean isBase64( byte octect ) {
        //shall we ignore white space? JEFF??
        return ( isWS(octect) || isPad(octect) || isData(octect));
    }


    protected static int removeWS (byte[] arrayOctect) {
        if (arrayOctect == null || arrayOctect.length == 0)
            return 0;

        int len = arrayOctect.length, i=0, j=0;
        for (i=0,j=0; i<len; i++) {
            if (!isWS(arrayOctect[i])) {
                if (i>j)
                    arrayOctect[j] = arrayOctect[i];
                j++;
            }
        }
        return j;
    }

    public static boolean isArrayByteBase64( byte[] arrayOctect ) {
        return (getDataLength(arrayOctect) > 0);
    }

    /**
     * Encodes hex octects into Base64
     *
     * @param binaryData Array containing binaryData
     * @return Encoded Base64 array
     */
    public static byte[] encode( byte[] binaryData ) {
        int      lengthDataBits    = binaryData.length*EIGHTBIT;
        int      fewerThan24bits   = lengthDataBits%TWENTYFOURBITGROUP;
        int      numberTriplets    = lengthDataBits/TWENTYFOURBITGROUP;
        byte     encodedData[]     = null;


        if ( fewerThan24bits != 0 ) //data not divisible by 24 bit
            encodedData = new byte[ (numberTriplets + 1 )*4  ];
        else // 16 or 8 bit
            encodedData = new byte[ numberTriplets*4 ];

        byte k=0, l=0, b1=0,b2=0,b3=0;

        int encodedIndex = 0;
        int dataIndex   = 0;
        int i           = 0;
        if (fDebug ) {
            System.out.println("number of triplets = " + numberTriplets );
        }
        for ( i = 0; i<numberTriplets; i++ ) {

            dataIndex = i*3;
            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex + 1];
            b3 = binaryData[dataIndex + 2];

            if (fDebug) {
                System.out.println( "b1= " + b1 +", b2= " + b2 + ", b3= " + b3 );
            }

            l  = (byte)(b2 & 0x0f);
            k  = (byte)(b1 & 0x03);

            encodedIndex = i*4;
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);

            byte val2 = ((b2 & SIGN)==0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);
            byte val3 = ((b3 & SIGN)==0)?(byte)(b3>>6):(byte)((b3)>>6^0xfc);

            encodedData[encodedIndex]   = lookUpBase64Alphabet[ val1 ];
            if (fDebug) {
                System.out.println( "val2 = " + val2 );
                System.out.println( "k4   = " + (k<<4));
                System.out.println( "vak  = " + (val2 | (k<<4)));
            }

            encodedData[encodedIndex+1] = lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex+2] = lookUpBase64Alphabet[ (l <<2 ) | val3 ];
            encodedData[encodedIndex+3] = lookUpBase64Alphabet[ b3 & 0x3f ];
        }

        // form integral number of 6-bit groups
        dataIndex    = i*3;
        encodedIndex = i*4;
        if (fewerThan24bits == EIGHTBIT ) {
            b1 = binaryData[dataIndex];
            k = (byte) ( b1 &0x03 );
            if (fDebug ) {
                System.out.println("b1=" + b1);
                System.out.println("b1<<2 = " + (b1>>2) );
            }
            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ k<<4 ];
            encodedData[encodedIndex + 2] = PAD;
            encodedData[encodedIndex + 3] = PAD;
        } else if ( fewerThan24bits == SIXTEENBIT ) {

            b1 = binaryData[dataIndex];
            b2 = binaryData[dataIndex +1 ];
            l = ( byte ) ( b2 &0x0f );
            k = ( byte ) ( b1 &0x03 );

            byte val1 = ((b1 & SIGN)==0)?(byte)(b1>>2):(byte)((b1)>>2^0xc0);
            byte val2 = ((b2 & SIGN)==0)?(byte)(b2>>4):(byte)((b2)>>4^0xf0);

            encodedData[encodedIndex]     = lookUpBase64Alphabet[ val1 ];
            encodedData[encodedIndex + 1] = lookUpBase64Alphabet[ val2 | ( k<<4 )];
            encodedData[encodedIndex + 2] = lookUpBase64Alphabet[ l<<2 ];
            encodedData[encodedIndex + 3] = PAD;
        }
        return encodedData;
    }


    /**
     * Decodes Base64 data into octects
     *
     * @param binaryData Byte array containing Base64 data
     * @return Array containind decoded data.
     */
    public static byte[] decode( byte[] base64Data ) {
        if (base64Data == null)
            return null;

        int length = base64Data.length;
        if ( length == 0 )
            return null;

        // remove all whitespaces
        byte    arrayOctect[] = new byte[length];
        System.arraycopy(base64Data,0,arrayOctect,0,length);
        length = removeWS(arrayOctect);
        if (length == 0 || length % FOURBYTE != 0)
            return null;

        // calculate the decoded value length
        int numberQuadruple    = length/FOURBYTE*3;
        if (isPad(arrayOctect[length-2]))
            numberQuadruple -= 2;
        else if (isPad(arrayOctect[length-1]))
            numberQuadruple--;

        byte     decodedData[]      = new byte[ numberQuadruple];
        byte     b1=0,b2=0,b3=0,b4=0, d1=0,d2=0,d3=0,d4=0;

        int decodedIndex    = 0;

        boolean b3isp = false, b4isp = false;
        int i = 0;

        while (i < length) {
            // the first byte must be real data
            d1 = arrayOctect[i];
            if (!isData(d1))
                return null;
            i++;
            // the second byte must be real data
            d2 = arrayOctect[i];
            if (!isData(d2))
                return null;
            i++;
            // the third byte must be real data or pad
            d3 = arrayOctect[i];
            b3isp = isPad(d3);
            if (!isData(d3) && !b3isp)
                return null;
            i++;
            if (b3isp && (d2&0xf) != 0)
                return null;
            // the forth byte must be real data or pad
            // and if the third is pad, the forth must be pad
            d4 = arrayOctect[i];
            b4isp = isPad(d4);
            if (!isData(d4) && !b4isp || b3isp && !b4isp)
                return null;
            i++;
            if (!b3isp && b4isp && (d3&0x3) != 0)
                return null;

            // if the forth byte is pad, it should be the end of the input
            if (b4isp && i < length)
                return null;

            b1 = base64Alphabet[d1];
            b2 = base64Alphabet[d2];

            if ( !b4isp ) {                   //No PAD e.g 3cQl
                b3 = base64Alphabet[d3];
                b4 = base64Alphabet[d4];

                decodedData[decodedIndex++] = (byte)(  b1 <<2 | b2>>4 ) ;
                decodedData[decodedIndex++] = (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
                decodedData[decodedIndex++] = (byte)( b3<<6 | b4 );
            } else if ( b3isp ) {             //Two PAD e.g. 3c[Pad][Pad]
                decodedData[decodedIndex++] = (byte)(  b1 <<2 | b2>>4 ) ;
            } else if ( b4isp ) {    //One PAD e.g. 3cQ[Pad]
                b3 = base64Alphabet[d3];

                decodedData[decodedIndex++] = (byte)(  b1 <<2 | b2>>4 );
                decodedData[decodedIndex++] = (byte)(((b2 & 0xf)<<4 ) |( (b3>>2) & 0xf) );
            }
        }

        return decodedData;
    }

    static public int getDataLength (byte[] base64Data) {
        if (base64Data == null)
            return 0;

        int length = base64Data.length;
        if ( length == 0 )
            return 0;

        // remove all whitespaces
        byte    arrayOctect[] = new byte[length];
        System.arraycopy(base64Data,0,arrayOctect,0,length);
        length = removeWS(arrayOctect);
        if ( length == 0 )
            return 0;
        if (length % FOURBYTE != 0)
            return -1;

        byte d2=0, d3=0;
        boolean b3isp = false, b4isp = false;
        int i = 0;

        while (i < length) {
            // the first byte must be real data
            if (!isData(arrayOctect[i]))
                return -1;
            i++;
            // the second byte must be real data
            d2 = arrayOctect[i];
            if (!isData(d2))
                return -1;
            i++;
            // the third byte must be real data or pad
            d3 = arrayOctect[i];
            b3isp = isPad(d3);
            if (!isData(d3) && !b3isp)
                return -1;
            if (b3isp && (d2&0xf) != 0)
                return -1;
            i++;
            // the forth byte must be real data or pad
            // and if the third is pad, the forth must be pad
            b4isp = isPad(arrayOctect[i]);
            if (!isData(arrayOctect[i]) && !b4isp || b3isp && !b4isp)
                return -1;
            i++;
            if (!b3isp && b4isp && (d3&0x3) != 0)
                return -1;

            // if the forth byte is pad, it should be the end of the input
            if (b4isp && i < length)
                    return -1;
        }

        // adjust number according to pad
        return length/FOURBYTE*3 - (b3isp?2:(b4isp?1:0));
    }
}
