/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.xs.opti;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.w3c.dom.DOMException;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.Augmentations;

import org.apache.xerces.xni.XNIException;


/**
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class SchemaDOM extends DefaultDocument {

    static final int relationsRowResizeFactor = 15;
    static final int relationsColResizeFactor = 10;

    ElementImpl[][] relations;
    ElementImpl parent;
    int currLoc;
    int nextFreeLoc;
    boolean hidden;


    public SchemaDOM() {
        reset();
    }
    

    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        ElementImpl node = new ElementImpl();
        processElement(element, attributes, augs, node);
        // now the current node added, becomes the parent
        parent = node;
    }
    
    
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException {
        ElementImpl node = new ElementImpl();
        processElement(element, attributes, augs, node);
    }
    
    
    private void processElement(QName element, XMLAttributes attributes, Augmentations augs, ElementImpl node) throws XNIException {

        // populate node
        node.prefix = element.prefix;
        node.localpart = element.localpart;
        node.rawname = element.rawname;
        node.uri = element.uri;
        node.schemaDOM = this;
        
        // set the attributes
        Attr[] attrs = new Attr[attributes.getLength()];
        for (int i=0; i<attributes.getLength(); i++) {
            attrs[i] = new AttrImpl(null, 
                                    attributes.getPrefix(i), 
                                    attributes.getLocalName(i), 
                                    attributes.getQName(i), 
                                    attributes.getURI(i), 
                                    attributes.getValue(i));
        }
        node.attrs = attrs;
        
        // check if array needs to be resized
        if (nextFreeLoc == relations.length) {
            resizeRelations();
        }
        
        // store the current parent
        //if (relations[currLoc][0] == null || relations[currLoc][0] != parent) {
        if (relations[currLoc][0] != parent) {
            relations[nextFreeLoc][0] = parent;
            currLoc = nextFreeLoc++;
        }
        
        // add the current node as child of parent
        boolean foundPlace = false;
        int i = 1;
        for (i = 1; i<relations[currLoc].length; i++) {
            if (relations[currLoc][i] == null) {
                foundPlace = true;
                break;
            }
        }
        
        if (!foundPlace) {
            resizeRelations(currLoc);
        }
        relations[currLoc][i] = node;
        
        parent.parentRow = currLoc;
        node.row = currLoc;
        node.col = i;
    }
    
    
    public void endElement(QName element, Augmentations augs) throws XNIException {
        // the parent of current parent node becomes the parent
        // for the next node.
        currLoc = parent.row;
        parent = relations[currLoc][0];
    }
    
    
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        // REVISIT: Make a text node.
    }
    
    
    private void resizeRelations() {
        ElementImpl[][] temp = new ElementImpl[relations.length+relationsRowResizeFactor][];
        System.arraycopy(relations, 0, temp, 0, relations.length);
        for (int i = relations.length ; i < temp.length ; i++) {
            temp[i] = new ElementImpl[relationsColResizeFactor];
        }
        relations = temp;
    }
    
    private void resizeRelations(int i) {
        ElementImpl[] temp = new ElementImpl[relations[i].length+relationsColResizeFactor];
        System.arraycopy(relations[i], 0, temp, 0, relations[i].length);
        relations[i] = temp;
    }
    
    
    public void reset() {
        
        relations = new ElementImpl[relationsRowResizeFactor][];
        parent = new ElementImpl();
        parent.rawname = "DOCUMENT_NODE";
        currLoc = 0;
        nextFreeLoc = 1;
        for (int i=0; i<relationsRowResizeFactor; i++) {
            relations[i] = new ElementImpl[relationsColResizeFactor];
        }
        relations[currLoc][0] = parent;
    }
    
    
    public void printDOM() {
        /*
        for (int i=0; i<relations.length; i++) {
            if (relations[i][0] != null) {
            for (int j=0; j<relations[i].length; j++) {
                if (relations[i][j] != null) {
                    System.out.print(relations[i][j].nodeType+"-"+relations[i][j].parentRow+"  ");
                }
            }
            System.out.println("");
            }
        }
        */
        //traverse(getDocumentElement(), 0);
    }
    
    
    // debug methods
    
    public static void traverse(Node node, int depth) {
        indent(depth);
        System.out.print("<"+node.getNodeName());
        
        if (node.hasAttributes()) {
            NamedNodeMap attrs = node.getAttributes();
            for (int i=0; i<attrs.getLength(); i++) {
                System.out.print("  "+((Attr)attrs.item(i)).getName()+"=\""+((Attr)attrs.item(i)).getValue()+"\"");
            }
        }
        
        if (node.hasChildNodes()) {
            System.out.println(">");
            depth+=4;
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                traverse(child, depth);
            }
            depth-=4;
            indent(depth);
            System.out.println("</"+node.getNodeName()+">");
        }
        else {
            System.out.println("/>");
        }
    }
    
    public static void indent(int amount) {
        for (int i = 0; i < amount; i++) {
            System.out.print(' ');
        }
    }
    
    
    
    // org.w3c.dom methods
    public Element getDocumentElement() {
        return relations[0][1];
    }
    
    
    // other methods
    public void setReadOnly(boolean hide, boolean x) {
        hidden = hide;
    }
    
    public boolean getReadOnly() {
        return hidden;
    }
    
}