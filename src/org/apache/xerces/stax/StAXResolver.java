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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * @xerces.internal
 * 
 * @author Wei Duan
 */
public class StAXResolver implements XMLEntityResolver {

    private XMLResolver resolve;

    public StAXResolver(XMLResolver resolve) {
        this.resolve = resolve;
    }

    /**
     * Resolves an external parsed entity. If the entity cannot be
     * resolved, this method should return null.
     *
     * @param resourceIdentifier location of the XML resource to resolve
     *
     * @throws XNIException Thrown on general error.
     * @throws IOException  Thrown if resolved entity stream cannot be
     *                      opened or some other i/o error occurs.
     * @see org.apache.xerces.xni.XMLResourceIdentifier
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
            throws XNIException, IOException {
        if (resolve != null && resourceIdentifier != null) {
            try {
                Object obj = resolve.resolveEntity(resourceIdentifier
                        .getPublicId(), resourceIdentifier
                        .getExpandedSystemId(), null, resourceIdentifier
                        .getNamespace());

                // The STAX resolve will also return XMLStreamReader or XMLEventReader
                // How to solve these two types
                if (obj != null && obj instanceof InputStream) {
                    XMLInputSource inputSource = new XMLInputSource(
                            resourceIdentifier);
                    inputSource.setByteStream((InputStream) obj);
                    return inputSource;
                }
            } catch (XMLStreamException e) {
                throw new XNIException(e.getMessage(), e);
            }
        }
        return null;
    }
}
