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
                    
import org.xml.sax.AttributeList;

/**
 * An AttributeList implementation that can perform more operations
 * than the attribute list helper supplied with the standard SAX
 * distribution.
 */
public class AttributeListImpl
    implements AttributeList {

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
    // AttributeList methods
    //

    /** Returns the number of attributes. */
    public int getLength() {
        return length;
    }

    /** Returns the attribute name by index. */
    public String getName(int index) {

        ListNode node = getNodeAt(index);
        return (node != null) ? node.name : null;

    } // getName(int):String

    /** Returns the attribute type by index. */
    public String getType(int index) {

        ListNode node = getNodeAt(index);
        return (node != null) ? node.type : null;

    } // getType(int):String

    /** Returns the attribute value by index. */
    public String getValue(int index) {

        ListNode node = getNodeAt(index);
        return (node != null) ? node.value : null;

    } // getType(int):String

    /** Returns the attribute type by name. */
    public String getType(String name) {

        ListNode node = getNodeAt(name);
        return (node != null) ? node.type : null;

    } // getType(int):String

    /** Returns the attribute value by name. */
    public String getValue(String name) {

        ListNode node = getNodeAt(name);
        return (node != null) ? node.value : null;

    } // getType(int):String

    //
    // Public methods
    //

    /** Adds an attribute. */
    public void addAttribute(String name, String type, String value) {

        ListNode node = new ListNode(name, type, value);
        if (length == 0) {
            head = node;
        }
        else {
            tail.next = node;
        }
        tail = node;
        length++;

    } // addAttribute(String,String,String)

    /** Inserts an attribute. */
    public void insertAttributeAt(int index, 
                                  String name, String type, String value) {

        // if list is empty, add attribute
        if (length == 0 || index >= length) {
            addAttribute(name, type, value);
            return;
        }

        // insert at beginning of list
        ListNode node = new ListNode(name, type, value);
        if (index < 1) {
            node.next = head;
            head = node;
        }
        else {
            ListNode prev = getNodeAt(index - 1);
            node.next = prev.next;
            prev.next = node;
        }
        length++;

    } // addAttribute(String,String,String)

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
            ListNode prev = getNodeAt(index - 1);
            ListNode node = getNodeAt(index);
            if (node != null) {
                prev.next = node.next;
                if (node == tail) {
                    tail = prev;
                }
                length--;
            }
        }

    } // removeAttributeAt(int)

    //
    // Private methods
    //

    /** Returns the node at the specified index. */
    private ListNode getNodeAt(int i) {

        for (ListNode place = head; place != null; place = place.next) {
            if (--i == -1) {
                return place;
            }
        }

        return null;

    } // getNodeAt(int):ListNode

    /** Returns the first node with the specified name. */
    private ListNode getNodeAt(String name) {

        if (name != null) {
            for (ListNode place = head; place != null; place = place.next) {
                if (place.name.equals(name)) {
                    return place;
                }
            }
        }

        return null;

    } // getNodeAt(int):ListNode

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
            str.append(place.name);
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

        /** Attribute name. */
        public String name;

        /** Attribute type. */
        public String type;

        /** Attribute value. */
        public String value;

        /** Next node. */
        public ListNode next;

        //
        // Constructors
        //

        /** Default constructor. */
        public ListNode(String name, String type, String value) {
            this.name  = name;
            this.type  = type;
            this.value = value;
        }

    } // class ListNode

} // class AttributeListImpl
