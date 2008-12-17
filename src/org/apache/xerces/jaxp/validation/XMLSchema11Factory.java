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

package org.apache.xerces.jaxp.validation;

import javax.xml.validation.SchemaFactory;

import org.apache.xerces.impl.Constants;

/**
 * {@link SchemaFactory} for XML Schema.
 *
 * @author Khaled Noaman, IBM
 * @version $Id$
 */
public final class XMLSchema11Factory extends BaseSchemaFactory {

    public XMLSchema11Factory() {
        super(Constants.W3C_XML_SCHEMA11_NS_URI);
    }

    public boolean isSchemaLanguageSupported(String schemaLanguage) {
        if (schemaLanguage == null) {
            throw new NullPointerException(JAXPValidationMessageFormatter.formatMessage(getLocale(), 
                    "SchemaLanguageNull", null));
        }
        if (schemaLanguage.length() == 0) {
            throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(getLocale(), 
                    "SchemaLanguageLengthZero", null));
        }
        // only W3C XML Schema 1.1 is supported
        return schemaLanguage.equals(Constants.W3C_XML_SCHEMA11_NS_URI);
    }

} // XMLSchema11Factory
