/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Iterator;
import java.util.Stack;
import java.util.ArrayList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.XMLConstants;

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
    
    /**
     * The initialization method of DOMNamespaceContext, 
     * the DOMNamespaceContext is a singleton
     */
    public NamespaceContextImpl() {
        namespaceStack = new Stack();
    }
    
    /**
     * When encounters a StartElement event, the namespace level in the namespace
     * stack is increased.
     *
     */
    public void onStartElement() {
        if (!namespaceStack.empty()) {
            Iterator iter = namespaceStack.iterator();
            while(iter.hasNext()){
                DOMNamespace dc = (DOMNamespace)iter.next();
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
        if (!namespaceStack.empty()) {
            
            Iterator iter = namespaceStack.iterator();
            
            while (iter.hasNext()) {
                DOMNamespace dc = (DOMNamespace)iter.next();
                int level = dc.decreaseLevel();
                
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
     * Add the prefix and namespaceURI to the namespace stack
     * 
     * @param prefix
     * @param namespaceURI
     */
    public void addNamespace(String prefix, String namespaceURI) {
        DOMNamespace dn = new DOMNamespace(prefix, namespaceURI);
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
            DOMNamespace dc = (DOMNamespace) namespaceStack.elementAt(--size);
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
        if (namespaceURI.equals(XMLConstants.XML_NS_URI)) return "xmlns";
        
        int size = namespaceStack.size();
        
        while (size > 0) {
            DOMNamespace dc = (DOMNamespace) namespaceStack.elementAt(--size);
            String namespace = dc.getNamespaceURI();
            if (namespaceURI.equals(namespace))
                return dc.getPrefix();
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
                DOMNamespace dc = (DOMNamespace)iter.next();
                String namespace = dc.getNamespaceURI();
                if(namespaceURI.equals(namespace))
                    prefixes.add(dc.getPrefix());
            }
        }
        
        return prefixes.iterator();
    }
    
    /**
     * Class to represent the namespace in DOMSource
     * 
     * @author Hua Lei
     */
    final class DOMNamespace {
        
        private String prefix;
        private String namespaceURI;
        private int level;
        
        DOMNamespace(String prefix, String namespaceURI) {
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
