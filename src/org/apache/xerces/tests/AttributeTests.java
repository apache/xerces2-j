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

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id
 */
public class AttributeTests extends XercesAbstractTestCase {

	public AttributeTests(String name) {
		super(name);
	}
	
	public void testDefaultAttributes1() {
		String xmlfile = fDataDir+"/roger/defaultAttributes/bookStore/BookStore.xml";
		String schemapath = fDataDir+"/roger/defaultAttributes/bookStore/BookStore.xsd";	
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
	
	public void testDefaultAttributes2() {
		String xmlfile = fDataDir+"/roger/defaultAttributes/bookStore/BookStore_v2.xml";
		String schemapath = fDataDir+"/roger/defaultAttributes/bookStore/BookStore_v2.xsd";	
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
	
	public void testDefaultAttributes3() {
		String xmlfile = fDataDir+"/roger/defaultAttributes/bookStore/BookStore_v3.xml";
		String schemapath = fDataDir+"/roger/defaultAttributes/bookStore/BookStore_v3.xsd";	
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
	
	// inheritable attribute with type alternatives
	public void testAttributes4() {
		String xmlfile = fDataDir+"/attributes/test1_1.xml";
		String schemapath = fDataDir+"/attributes/test1.xsd";	
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
	
	// inheritable attribute with type alternatives
	public void testAttributes5() {
		String xmlfile = fDataDir+"/attributes/test1_2.xml";
		String schemapath = fDataDir+"/attributes/test1.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-enumeration-valid:");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-type.3.1.3: The value 'Prentice Hall' of element");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}

}
