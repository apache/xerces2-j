/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.util;

import org.apache.xerces.xni.QName;

/**
 * This class is an unsynchronized hash table primary used for String
 * to int mapping.
 * <p>
 * The hash code uses the same algorithm as SymbolTable class.
 * 
 * @author Elena Litani
 * @version $Id$
 */
public class SymbolHash {

    //
    // Constants
    //

    /** Default table size. */
    protected int fTableSize = 101;

    //
    // Data
    //

    /** Buckets. */
    protected Entry[] fBuckets; 


    //
    // Constructors
    //

    /** Constructs a key table. */
    public SymbolHash() {
        fBuckets = new Entry[fTableSize];
    }


    public SymbolHash(int size) {
        fTableSize = size;
        fBuckets = new Entry[fTableSize];
    }

    //
    // Public methods
    //

    /**
     * Adds the specified key to the key table and returns a
     * reference to the unique key. If the key already exists, 
     * the previous key reference is returned instead, in order
     * guarantee that key references remain unique.
     * 
     * @param key The new key.
     */
    public String put (String key, int value ) {

        int bucket = hash(key) % fTableSize;
        Entry entry = search (key, bucket);

        if (entry !=null) {
            return entry.key;
        }
        // create new entry
        entry = new Entry(key, value, fBuckets[bucket]);
        fBuckets[bucket] = entry;
        return key;

    } 

    /**
     * Adds the specified key to the key table and returns a
     * reference to the unique key. If the key already exists, 
     * the previous key reference is returned instead, in order
     * guarantee that key references remain unique.
     * 
     * @param qName The QName which is the key
     * @param value 
     */
    public String put(QName qName, int value) {

        String key = qName.uri.concat(qName.localpart);

        // search for identical key
        int bucket = hash(key) % fTableSize;
        Entry entry = search (key, bucket);

        if (entry !=null) {
            return entry.key;
        }
        // add new entry
        entry = new Entry(key, value,fBuckets[bucket]);
        fBuckets[bucket] = entry;
        return entry.key;

    } 

    public int get (String key){

        int bucket = hash(key) % fTableSize;
        Entry entry = search (key, bucket);
        if (entry !=null) {
            return entry.value;
        }
        return -1;
    }

    public int get (QName qName){

        String key = qName.uri.concat(qName.localpart);
        
        int bucket = hash(key) % fTableSize;
        Entry entry = search (key, bucket);
        
        if (entry !=null) {
            return entry.value;
        }
        return -1;
    }

    protected Entry search (String key, int bucket){
        // search for identical key
        int length = key.length();
        OUTER: for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (length == entry.characters.length) {
                for (int i = 0; i < length; i++) {
                    if (key.charAt(i) != entry.characters[i]) {
                        continue OUTER;
                    }
                }
                return entry;
            }
        }
        return null;
    }
    /**
     * Returns a hashcode value for the specified key. The value
     * returned by this method must be identical to the value returned
     * by the <code>hash(char[],int,int)</code> method when called
     * with the character array that comprises the key string.
     * 
     * @param key The key to hash.
     */
    public int hash(String key) {

        int code = 0;
        int length = key.length();
        for (int i = 0; i < length; i++) {
            code = code * 37 + key.charAt(i);
        }
        return code & 0x7FFFFFF;

    } // hash(String):int




    //
    // Classes
    //

    /**
     * This class is a key table entry. Each entry acts as a node
     * in a linked list.
     */
    protected static final class Entry {
	/**
        * key is a name or QName 
        */
	public String key;

	public int value;
	
        /** 
         * key characters. This information is duplicated here for
         * comparison performance.
         */
        public char[] characters;

        /** The next entry. */
        public Entry next;


	public Entry(String key, int value, Entry next) {
	    this.key = key;
	    this.value = value;
	    this.next = next;
            characters = new char[key.length()];
            key.getChars(0, characters.length, characters, 0);
	}
    }

} // class SymbolHash

