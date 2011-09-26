/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.xs;


/**
 * XML Schema 1.1 - ID value handling
 * 
 * @xerces.internal
 * 
 * @version $Id$
 */
final class IDContext {

    private static final int INITIAL_MATCH_SIZE = 16;

    /** Table that stores the ID depth matched for a given element depth */
    private int[] fElementIDTable = new int[INITIAL_MATCH_SIZE];

    /** ID Depth */
    private int fIDDepth = 0;

    /**
     * Current ID Scope
     * 
     * Represents the id matched to current element (attribute handling) or to
     * its parent (simple content handling)
     */
    private int fIDScope = 0;

    /** Current Element scope
     * 
     * Represents the current element depth (attribute handling) or its parent
     * (simple content handling)
     */
    private int fElementScope = -1;

    /** Element depth */
    private int fElementDepth = 0;

    // ID Mapping Table

    /** Default table size. */
    final private int fTableSize = 101;

    /** Buckets. */
    final private Entry[] fBuckets; 

    /** Number of elements. */
    private int fNum = 0;

    IDContext() {
        fBuckets = new Entry[fTableSize];
    }

    void clear() {
        if (fNum > 0) {
            for (int i=0; i<fTableSize; i++) {
                fBuckets[i] = null;
            }
        }

        fElementDepth = fIDDepth = fNum = 0;
        fElementScope = fIDScope = -1; 
    }

    void setCurrentScopeToParent() {
        if (fElementScope > 0) {
            fIDScope = fElementIDTable[--fElementScope];
        }
        else {
            fElementScope = fIDScope = -1;
        }
    }

    void popContext() {
        --fElementDepth;
    }

    void pushContext() {
        if (fElementDepth == fElementIDTable.length) {
            resizeElementDepthIDTable();
        }
        fElementScope = fElementDepth++;
        fElementIDTable[fElementScope] = fIDScope = 0;
    }

    private void resizeElementDepthIDTable() {
        final int newSize = fElementDepth << 1;
        final int[] newArray = new int[newSize];

        System.arraycopy(fElementIDTable, 0, newArray, 0, fElementDepth);
        fElementIDTable = newArray;
    }

    boolean isDeclared(String id) {
        final int idDepth = get(id);
        // did not find a match in the table
        if (idDepth == -1) {
            // root element with simple content of type ID is invalid
            return fIDScope == -1;
        }

        return idDepth != fIDScope;
    }

    boolean containsID(String name) {
        int bucket = (name.hashCode() & 0x7FFFFFFF) % fTableSize;
        for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (name.equals(entry.key))
                return true;
        }
        return false;
    }

    private int get(String key) {
        int bucket = (key.hashCode() & 0x7FFFFFFF) % fTableSize;
        for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (key.equals(entry.key))
                return entry.value;
        }
        return -1;
    }

    void add(String id) {
        final int bucket = (id.hashCode() & 0x7FFFFFFF) % fTableSize;
        Entry entry = search(id, bucket);

        if (entry == null) {
            if (fElementIDTable[fElementScope] == 0) {
                fElementIDTable[fElementScope] = fIDScope = ++fIDDepth;
            }
            entry = new Entry(id, fIDScope, fBuckets[bucket]);
            fBuckets[bucket] = entry;
            fNum++;
        }
    }

    private Entry search(String id, int bucket) {
        for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
            if (id.equals(entry.key)) {
                return entry;
            }
        }
        
        return null;
    }

    /**
     * This class is a key table entry. Each entry acts as a node
     * in a linked list. 
     */
    private static final class Entry {
        /** key/value */
        public String key;
        public int value;

        /** The next entry. */
        public Entry next;

        public Entry() {
            key = null;
            value = -1;
            next = null;
        }

        public Entry(String key, int value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }        
    } // entry
}
