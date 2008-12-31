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

package org.apache.xerces.stax.events;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public final class StartElementImpl extends ElementImpl implements StartElement {

    private final Map fAttributes = new TreeMap(new Comparator(){
        public int compare(Object o1, Object o2) {
            if(o1.equals(o2)) {
                return 0;
            }
            QName name1 = (QName)o1;
            QName name2 = (QName)o2;
            return name1.toString().compareTo(name2.toString());
        }});
    private final NamespaceContext fNamespaceContext;

    /**
     * @param location
     * @param schemaType
     */
    public StartElementImpl(final QName name, final NamespaceContext namespaceContext, final Location location) {
        super(name, true, location);
        fNamespaceContext = namespaceContext;
    }

    /**
     * @see javax.xml.stream.events.StartElement#getAttributes()
     */
    public Iterator getAttributes() {
        return createImmutableIterator(fAttributes.values().iterator());
    }

    /**
     * @see javax.xml.stream.events.StartElement#getAttributeByName(javax.xml.namespace.QName)
     */
    public Attribute getAttributeByName(final QName name) {
        return (Attribute) fAttributes.get(name);
    }

    /**
     * @see javax.xml.stream.events.StartElement#getNamespaceContext()
     */
    public NamespaceContext getNamespaceContext() {
        return fNamespaceContext;
    }

    /**
     * @see javax.xml.stream.events.StartElement#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(final String prefix) {
        return fNamespaceContext.getNamespaceURI(prefix);
    }

    public void addAttribute(final Attribute attribute) {
        fAttributes.put(attribute.getName(), attribute);
    }
        
}
