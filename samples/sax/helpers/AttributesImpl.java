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

package sax.helpers;
                    
import org.xml.sax.Attributes;

/**
 * An Attributes implementation that can perform more operations
 * than the attribute list helper supplied with the standard SAX2
 * distribution.
 */
public class AttributesImpl
    implements Attributes {

    //
    // Data
    //

    /** Head node. */
    private ListNode head;

    /** Tail node. */
    private ListNode tail;

    /** Length. */
    private int length;

    //
    // Attributes methods
    //

    /** Returns the number of attributes. */
    public int getLength() {
        return length;
    }

    /** Returns the index of the specified attribute. */
    public int getIndex(String raw) {
        ListNode place = head;
        int index = 0;
        while (place != null) {
            if (place.raw.equals(raw)) {
                return index;
            }
            index++;
            place = place.next;
        }
        return -1;
    }

    /** Returns the index of the specified attribute. */
    public int getIndex(String uri, String local) {
        ListNode place = head;
        int index = 0;
        while (place != null) {
            if (place.uri.equals(uri) && place.local.equals(local)) {
                return index;
            }
            index++;
            place = place.next;
        }
        return -1;
    }

    /** Returns the attribute URI by index. */
    public String getURI(int index) {

        ListNode node = getListNodeAt(index);
        return node != null ? node.uri : null;

    } // getURI(int):String

    /** Returns the attribute local name by index. */
    public String getLocalName(int index) {

        ListNode node = getListNodeAt(index);
        return node != null ? node.local : null;

    } // getLocalName(int):String

    /** Returns the attribute raw name by index. */
    public String getQName(int index) {

        ListNode node = getListNodeAt(index);
        return node != null ? node.raw : null;

    } // getQName(int):String

    /** Returns the attribute type by index. */
    public String getType(int index) {

        ListNode node = getListNodeAt(index);
        return (node != null) ? node.type : null;

    } // getType(int):String

    /** Returns the attribute type by uri and local. */
    public String getType(String uri, String local) {

        ListNode node = getListNode(uri, local);
        return (node != null) ? node.type : null;

    } // getType(String,String):String

    /** Returns the attribute type by raw name. */
    public String getType(String raw) {

        ListNode node = getListNode(raw);
        return (node != null) ? node.type : null;

    } // getType(String):String

    /** Returns the attribute value by index. */
    public String getValue(int index) {

        ListNode node = getListNodeAt(index);
        return (node != null) ? node.value : null;

    } // getType(int):String

    /** Returns the attribute value by uri and local. */
    public String getValue(String uri, String local) {

        ListNode node = getListNode(uri, local);
        return (node != null) ? node.value : null;

    } // getType(String):String

    /** Returns the attribute value by raw name. */
    public String getValue(String raw) {

        ListNode node = getListNode(raw);
        return (node != null) ? node.value : null;

    } // getType(String):String

    //
    // Public methods
    //

    /** Adds an attribute. */
    public void addAttribute(String raw, String type, String value) {
        addAttribute(null, null, raw, type, value);
    }

    /** Adds an attribute. */
    public void addAttribute(String uri, String local, String raw, 
                             String type, String value) {

        ListNode node = new ListNode(uri, local, raw, type, value);
        if (length == 0) {
            head = node;
        }
        else {
            tail.next = node;
        }
        tail = node;
        length++;

    } // addAttribute(String,StringString,String,String)

    /** Inserts an attribute. */
    public void insertAttributeAt(int index, 
                                  String raw, String type, String value) {
        insertAttributeAt(index, null, null, raw, type, value);
    }

    /** Inserts an attribute. */
    public void insertAttributeAt(int index, 
                                  String uri, String local, String raw, 
                                  String type, String value) {

        // if list is empty, add attribute
        if (length == 0 || index >= length) {
            addAttribute(uri, local, raw, type, value);
            return;
        }

        // insert at beginning of list
        ListNode node = new ListNode(uri, local, raw, type, value);
        if (index < 1) {
            node.next = head;
            head = node;
        }
        else {
            ListNode prev = getListNodeAt(index - 1);
            node.next = prev.next;
            prev.next = node;
        }
        length++;

    } // insertAttributeAt(int,String,String,String,String,String)

    /** Removes an attribute. */
    public void removeAttributeAt(int index) {

        if (length == 0) {
            return;
        }

        if (index == 0) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
            length--;
        }
        else {
            ListNode prev = getListNodeAt(index - 1);
            ListNode node = getListNodeAt(index);
            if (node != null) {
                prev.next = node.next;
                if (node == tail) {
                    tail = prev;
                }
                length--;
            }
        }

    } // removeAttributeAt(int)

    /** Removes the specified attribute. */
    public void removeAttribute(String raw) {
        removeAttributeAt(getIndex(raw));
    }

    /** Removes the specified attribute. */
    public void removeAttribute(String uri, String local) {
        removeAttributeAt(getIndex(uri, local));
    }

    //
    // Private methods
    //

    /** Returns the node at the specified index. */
    private ListNode getListNodeAt(int i) {

        for (ListNode place = head; place != null; place = place.next) {
            if (--i == -1) {
                return place;
            }
        }

        return null;

    } // getListNodeAt(int):ListNode

    /** Returns the first node with the specified uri and local. */
    public ListNode getListNode(String uri, String local) {

        if (uri != null && local != null) {
            ListNode place = head;
            while (place != null) {
                if (place.uri != null && place.local != null &&
                    place.uri.equals(uri) && place.local.equals(local)) {
                    return place;
                }
                place = place.next;
            }
        }
        return null;

    } // getListNode(String,String):ListNode
    
    /** Returns the first node with the specified raw name. */
    private ListNode getListNode(String raw) {

        if (raw != null) {
            for (ListNode place = head; place != null; place = place.next) {
                if (place.raw != null && place.raw.equals(raw)) {
                    return place;
                }
            }
        }

        return null;

    } // getListNode(String):ListNode

    //
    // Object methods
    //

    /** Returns a string representation of this object. */
    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append('[');
        str.append("len=");
        str.append(length);
        str.append(", {");
        for (ListNode place = head; place != null; place = place.next) {
            str.append(place.toString());
            if (place.next != null) {
                str.append(", ");
            }
        }
        str.append("}]");

        return str.toString();

    } // toString():String

    //
    // Classes
    //

    /**
     * An attribute node.
     */
    static class ListNode {

        //
        // Data
        //

        /** Attribute uri. */
        public String uri;

        /** Attribute local. */
        public String local;

        /** Attribute raw. */
        public String raw;

        /** Attribute type. */
        public String type;

        /** Attribute value. */
        public String value;

        /** Next node. */
        public ListNode next;

        //
        // Constructors
        //

        /** Constructs a list node. */
        public ListNode(String uri, String local, String raw, 
                        String type, String value) {

            this.uri   = uri;
            this.local = local;
            this.raw   = raw;
            this.type  = type;
            this.value = value;

        } // <init>(String,String,String,String,String)

        //
        // Object methods
        //

        /** Returns string representation of this object. */
        public String toString() {
            return raw != null ? raw : local;
        }

    } // class ListNode

} // class AttributesImpl
