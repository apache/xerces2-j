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

/**
 * Algorithm used to hash char arrays (strings).
 *
 * This class was created after it was discovered that parsing some
 * documents was unexpectedly slow due to many different strings
 * hashing to the same 32-bit value using the java.lang.String hash
 * algorithm.
 *
 * The trick seems to be the shift of the top eight bits of the hashcode
 * back down to the bottom to keep them from being rolled out.
 *
 * @version
 */
public final class StringHasher {
    /**
     * generate a hashcode for a String
     *
     * @param str the String to hash
     * @param strLength the length of the String to hash
     * @return hashcode for the String
     */
    public static int hashString(String str, int strLength) {
        int hashcode = 0;
        for (int i = 0; i < strLength; i++) {
            int top = hashcode >> 24;
            hashcode += ((hashcode * 37) + top + ((int)str.charAt(i)));
        }
        hashcode = (hashcode & 0x7fffffff);
        return (hashcode == 0) ? 1 : hashcode;
    }
    /**
     * generate a hashcode for a character array
     *
     * @param chars the array to hash
     * @param offset the offset to start hashing
     * @param length the length of characters to hash
     * @return hashcode for the character array
     */
    public static int hashChars(char[] chars, int offset, int length) {
        int hashcode = 0;
        for (int i = 0; i < length; i++) {
            int top = hashcode >> 24;
            hashcode += ((hashcode * 37) + top + ((int)(chars[offset++] & 0xFFFF)));
        }
        hashcode = (hashcode & 0x7fffffff);
        return (hashcode == 0) ? 1 : hashcode;
    }
    /**
     * generate partially completed character hashcode.
     * this is mean to be iterated over individual characters in order to generate
     * a full hash value
     * @see #finishHash(int)
     *
     * @param hashcode a partially completed character hashcode
     * @param ch the character to hash
     * @return a partially completed character hashcode
     */
    public static int hashChar(int hashcode, int ch) {
        int top = hashcode >> 24;
        hashcode += ((hashcode * 37) + top + ch);
        return hashcode;
    }
    /**
     * finish hashing a partically completed character hashcode
     * @see #hashChar(int,int)
     * 
     * @param hashcode a partially completed character hashcode
     * @return a character hashcode
     */
    public static int finishHash(int hashcode) {
        hashcode = (hashcode & 0x7fffffff);
        return (hashcode == 0) ? 1 : hashcode;
    }
}
