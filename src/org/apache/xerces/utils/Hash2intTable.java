/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
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
 * A light-weight hashtable class that takes 2 ints as key and 1 int as value
 * @version
 */

public final class Hash2intTable {
    
    
    private static final int INITIAL_BUCKET_SIZE = 4;
    private static final int HASHTABLE_SIZE = 128;
    private int[][] fHashTable = new int[HASHTABLE_SIZE][];


    public void put(int key1, int key2, int value) {
        int hash = (key1+key2) % HASHTABLE_SIZE;
        int[] bucket = fHashTable[hash];
        
        if (bucket == null) {
            bucket = new int[1 + 3*INITIAL_BUCKET_SIZE];
            bucket[0] = 1;
            bucket[1] = key1;
	    bucket[2] = key2;
	    bucket[3] = value;
            fHashTable[hash] = bucket;
        } else {
            int count = bucket[0];
            int offset = 1 + 3*count;
            if (offset == bucket.length) {
                int newSize = count + INITIAL_BUCKET_SIZE;
                int[] newBucket = new int[1 + 3*newSize];
                System.arraycopy(bucket, 0, newBucket, 0, offset);
                bucket = newBucket;
                fHashTable[hash] = bucket;
            }
            boolean found = false;
	    int j=1;
            for (int i=0; i<count; i++){
                if ( bucket[j] == key1 && bucket[j+1] == key2) {
                    bucket[j+2] = value;
                    found = true;
                    break;
                }
		j += 3;
            }
            if (! found) {
                bucket[offset++] = key1;
                bucket[offset++] = key2;
		bucket[offset]= value;
                bucket[0] = ++count;
            }
            
        }
    }

    public int get(int key1, int key2) {
        int hash = (key1+key2) % HASHTABLE_SIZE;
        int[] bucket = fHashTable[hash];

	if (bucket == null) {
	    return -1;
	}
        int count = bucket[0];

        boolean found = false;
	int j=1;
        for (int i=0; i<count; i++){
            if ( bucket[j] == key1 && bucket[j+1] == key2) {
                found = true;
                return bucket[j+2];
                }
        }
        if (! found) {
            return -1;
        }
        return -1;
    }

}  // class Hash2inTable

  












