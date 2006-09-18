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

package org.apache.xerces.stax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * <p>NamespaceContext for the SAX and DOM XMLStreamReaders.</p>
 * 
 * @author Hua Lei
 * 
 * @version $Id$
 */
final class NamespaceContextImpl implements NamespaceContext {
    
    // Record the avaliable namespaces
    private Stack namespaceStack;
    
    // Record the namespaces of START_ELEMENT and END_ELEMENT for SAXSource
    private ArrayList eleNamespaces;
    
    private Stack enStack;
    
    /**
     * The initialization method of DOMNamespaceContext, 
     * the DOMNamespaceContext is a singleton
     */
    public NamespaceContextImpl() {
        namespaceStack = new Stack();
        enStack = new Stack();
    }
    
    /**
     * When encounters a StartElement event, the namespace level in the namespace
     * stack is increased.
     */
    public void onStartElement() {
        if(eleNamespaces != null) {
            enStack.push(eleNamespaces);
        }
        
        eleNamespaces = new ArrayList();
        if (!namespaceStack.empty()) {
            Iterator iter = namespaceStack.iterator();
            while(iter.hasNext()){
                Namespace dc = (Namespace)iter.next();
                if(dc.level == 0) eleNamespaces.add(dc);
                dc.increaseLevel();
            }
        }
    }
    
    /**
     * When encounters a StartElement event, the namespace level in the namespace
     * stack is decreased. If the level is less than 0, the namespace is out of scope
     * and removed from avaliable stack.
     */
    public void onEndElement() {
        if(!enStack.isEmpty())
            eleNamespaces = (ArrayList)enStack.pop();
        
        if (!namespaceStack.empty()) {
            
            Iterator iter = namespaceStack.iterator();
            
            while (iter.hasNext()) {
                Namespace dc = (Namespace)iter.next();
                int level = dc.decreaseLevel();
                
                // When encounter endElement event, the namespace whose level equals zero will not
                // removed from namespace stack util next end element
                if(level <= 0){
                    int index = namespaceStack.indexOf(dc);
                    int deleteNum = namespaceStack.size() - index;
                    while(deleteNum-- > 0)
                        namespaceStack.pop();
                    return;
                }
            }
        }
    }
    
    /**
     * Get the ArrayList which records the namespaces of element.
     * This method is for SAXSource. 
     * 
     * @return
     */
    protected ArrayList getNamespaces() {
        return eleNamespaces;
    }
    
    /**
     * Get the prefix of element namespace at specified index
     * This method is for SAXSource
     * 
     * @param index
     * @return
     */
    protected String getNamespacePrefix(int index) {
        Namespace dm = (Namespace)eleNamespaces.get(index);
        
        return dm.getPrefix();
    }
    
    /**
     * Get the uri of element namespace at specified index.
     * This method is for SAXSource
     * 
     * @param index
     * @return
     */
    protected String getNamespaceURI(int index) {
        Namespace dm = (Namespace)eleNamespaces.get(index);
        
        return dm.getNamespaceURI();
    }
    
    /**
     * Add the prefix and namespaceURI to the namespace stack
     * 
     * @param prefix
     * @param namespaceURI
     */
    public void addNamespace(String prefix, String namespaceURI) {
        Namespace dn = new Namespace(prefix, namespaceURI);
        namespaceStack.push(dn);
    }
    
    /**
     * Implement the interface of NamespaceContext
     */
    public String getNamespaceURI(String prefix) {
        if(prefix == null)
            throw new IllegalArgumentException("The input prefix should not be null");
        
        if (prefix.equals("xml")) return XMLConstants.XML_NS_URI;
        if (prefix.equals("xmlns")) return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        
        int size = namespaceStack.size();
        
        while (size > 0) {
            Namespace dc = (Namespace) namespaceStack.elementAt(--size);
            String pre = dc.getPrefix();
            if (prefix.equals(pre))
                return dc.getNamespaceURI();
        }
        
        return null;
    }
    
    /**
     * Implement the interface of NamespaceContext
     */
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null)
            throw new IllegalArgumentException("The input namespaceURI should not be null");
        
        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) return "xml";
        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) return "xmlns";
        
        int size = namespaceStack.size();
        
        HashSet prefixes = new HashSet();
        while (size > 0) {
            Namespace dc = (Namespace) namespaceStack.elementAt(--size);
            if (namespaceURI.equals(dc.namespaceURI)) {
                if(!prefixes.contains(dc.prefix))
                    return dc.prefix;
                else
                    return null;
            }
            else {
                prefixes.add(dc.prefix);
            }
        }
        
        return null;
    }
    
    /**
     * Implement the interface of NamespaceContext
     */
    public Iterator getPrefixes(String namespaceURI) {
        if(namespaceURI == null)
            throw new IllegalArgumentException("The input namespaceURI should not be null");
        ArrayList prefixes = new ArrayList();
        
        if (!namespaceStack.empty()) {
            Iterator iter = namespaceStack.iterator();
            while(iter.hasNext()){
                Namespace dc = (Namespace)iter.next();
                String namespace = dc.getNamespaceURI();
                if(namespaceURI.equals(namespace))
                    prefixes.add(dc.getPrefix());
            }
        }
        
        return prefixes.iterator();
    }
    
    /**
     * Class to represent the namespace in DOMSource and SAXSource
     * 
     * @author Hua Lei
     */
    final class Namespace {
        
        private String prefix;
        private String namespaceURI;
        private int level;
        
        Namespace(String prefix, String namespaceURI) {
            this.prefix = prefix;
            this.namespaceURI = namespaceURI;
            level = 0;
        }
        
        int decreaseLevel() {
            level--;
            return level;
        }
        
        void increaseLevel() {
            level++;
        }
        
        String getPrefix() {
            return prefix;
        }
        
        String getNamespaceURI() {
            return namespaceURI;
        }
        
        int getLevel() {
            return level;
        }
    }
}
