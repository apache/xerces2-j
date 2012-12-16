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

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class SimpleTypeTests extends XercesAbstractTestCase {

	public SimpleTypeTests(String name) {
		super(name);
	}
	
	public void testAnyAtomicType1() {
		String xmlfile = fDataDir+"/roger/anyAtomicType/Value/Example.xml";
		String schemapath = fDataDir+"/roger/anyAtomicType/Value/Example.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testSimpleType1() {
		String schemapath = fDataDir+"/simpletypes/test1.xsd";			
		try {
			fSchemaFactory.setErrorHandler(this);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("st-props-correct.1: 'itemType' of xs:list and 'memberTypes' of xs:union cannot refer to special types, xs:anyAtomicType or xs:anySimpleType. The 'xs:list' component of simpleType '#AnonType_X' violates this constraint.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("st-props-correct.1: 'itemType' of xs:list and 'memberTypes' of xs:union cannot refer to special types, xs:anyAtomicType or xs:anySimpleType. The 'xs:list' component of simpleType '#AnonType_Y' violates this constraint.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(SAXException ex) {
		   ex.printStackTrace();
		}
	}
	
	public void testSimpleType2() {
		String schemapath = fDataDir+"/simpletypes/test2.xsd";			
		try {
			fSchemaFactory.setErrorHandler(this);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("st-props-correct.1: 'itemType' of xs:list and 'memberTypes' of xs:union cannot refer to special types, xs:anyAtomicType or xs:anySimpleType. The 'xs:union' component of simpleType '#AnonType_X' violates this constraint.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("st-props-correct.1: 'itemType' of xs:list and 'memberTypes' of xs:union cannot refer to special types, xs:anyAtomicType or xs:anySimpleType. The 'xs:union' component of simpleType '#AnonType_Y' violates this constraint.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(SAXException ex) {
		   ex.printStackTrace();
		}
	}
	
}
