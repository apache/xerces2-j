/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.impl.v1.util;

/**
 * A hashtable class for use with the Schema implementation that
 * maps multiple keys to a single value.
 *
 * @author Eric Ye?, IBM
 * @author Andy Clark, IBM
 * 
 * @version $Id$
 */
public final class Hash2intTable {
    
    //
    // Constants
    //

    private static final int HASHTABLE_SIZE = 101;

    //
    // Data
    //

    private Bucket[] fHashTable = new Bucket[HASHTABLE_SIZE];

    //
    // Public methods
    //

    /** Puts a value into the hashtable. */
    public void put(String key1, String key2, int key3, int value) {

        // get bucket
        int hash = (key1.hashCode()+key2.hashCode()+key3) % HASHTABLE_SIZE;
        Bucket bucket = fHashTable[hash];
        
        // new bucket
        if (bucket == null) {
            bucket = new Bucket(key1, key2, key3, value);
            fHashTable[hash] = bucket;
            return;
        } 

        // replace existing value
        for (int i = 0; i < bucket.length; i++) {
            Entry entry = bucket.data[i];
            if (entry.key1.equals(key1) && 
                entry.key2.equals(key2) &&
                entry.key3 == key3) {
                entry.value = value;
                return;
            }
        }

        // resize bucket
        if (bucket.length == bucket.data.length) {
            Entry[] array = new Entry[bucket.length * 2];
            System.arraycopy(bucket.data, 0, array, 0, bucket.length);
            bucket.data = array;
        }

        // new entry
        Entry entry = new Entry(key1, key2, key3, value);
        bucket.data[bucket.length++] = entry;
            
    } // put(String,String,int,int)

    /** Queries the value in the hashtable. */
    public int get(String key1, String key2, int key3) {

        // get bucket
        int hash = (key1.hashCode()+key2.hashCode()+key3) % HASHTABLE_SIZE;
        Bucket bucket = fHashTable[hash];

        // find value
        if (bucket != null) {
            for (int i = 0; i < bucket.length; i++) {
                Entry entry = bucket.data[i];
                if (entry.key1.equals(key1) &&
                    entry.key2.equals(key2) &&
                    entry.key3 == key3) {
                    return entry.value;
                }
            }
        }

        // not found
        return -1;

    } // get(String,String,int):int

    //
    // Classes
    //

    public static class Bucket {
        private static final int INITIAL_BUCKET_SIZE = 4;
        public int length;
        public Entry[] data = new Entry[INITIAL_BUCKET_SIZE];
        public Bucket(String k1, String k2, int k3, int value) {
            length = 1;
            data[0] = new Entry(k1, k2, k3, value);
        }
    }

    public static class Entry {
        public String key1;
        public String key2;
        public int key3;
        public int value;
        public Entry(String k1, String k2, int k3, int v) {
            setValues(k1, k2, k3, v);
        }
        public void setValues(String k1, String k2, int k3, int v) {
            key1 = k1;
            key2 = k2;
            key3 = k3;
            value = v;
        }
    }

}  // class Hash2inTable

  












