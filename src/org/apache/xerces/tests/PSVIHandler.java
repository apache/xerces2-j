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

package org.apache.xerces.tests;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.xs.PSVIProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class consuming SAX events, augmented by PSVI information.
 * 
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id
 */
public class PSVIHandler extends DefaultHandler {

    private PSVIProvider fPsviProvider = null;
    private Map fAttributePsviInfo = null;
    private Map fElementPsviInfo = null;
    
	public PSVIHandler(PSVIProvider psviProvider) {
		fPsviProvider = psviProvider;
		fAttributePsviInfo = new HashMap();
		fElementPsviInfo = new HashMap();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		for (int attxIdx = 0; attxIdx < attributes.getLength(); attxIdx++) {
			fAttributePsviInfo.put(attributes.getLocalName(attxIdx), fPsviProvider.getAttributePSVIByName(null, attributes.getLocalName(attxIdx)));		
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
        fElementPsviInfo.put(localName, fPsviProvider.getElementPSVI());
	}
	
	public Map getElementPsviInfo() {
		return fElementPsviInfo; 
	}
	
	public Map getAttributePsviInfo() {
		return fAttributePsviInfo; 
	}

} // class PSVIHandler
