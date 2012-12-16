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

public class TypeAlternativeTests extends XercesAbstractTestCase {

	public TypeAlternativeTests(String name) {
		super(name);		
	}
	
	public void testCTA1() {
		String xmlfile = fDataDir+"/type-alternatives/test15.xml";
		String schemapath = fDataDir+"/type-alternatives/test15.xsd";		
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
	
	public void testCTA2() {
		String xmlfile = fDataDir+"/type-alternatives/test16.xml";
		String schemapath = fDataDir+"/type-alternatives/test16.xsd";		
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
	
	public void testCTA3() {
		String xmlfile = fDataDir+"/type-alternatives/test17.xml";
		String schemapath = fDataDir+"/type-alternatives/test17.xsd";		
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
	
	public void testCTA4() {
		String xmlfile = fDataDir+"/type-alternatives/test18.xml";
		String schemapath = fDataDir+"/type-alternatives/test18.xsd";		
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
	
	public void testCTA5() {
		String xmlfile = fDataDir+"/type-alternatives/test19.xml";
		String schemapath = fDataDir+"/type-alternatives/test19.xsd";		
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
	
	public void testCTA6() {
		String xmlfile = fDataDir+"/type-alternatives/test20.xml";
		String schemapath = fDataDir+"/type-alternatives/test20.xsd";		
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
	
	public void testCTA7() {
		String xmlfile = fDataDir+"/roger/type-alternatives/beverage/beverage.xml";
		String schemapath = fDataDir+"/roger/type-alternatives/beverage/beverage.xsd";		
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
	
	public void testCTA8() {
		String xmlfile = fDataDir+"/roger/type-alternatives/meeting/meeting.xml";
		String schemapath = fDataDir+"/roger/type-alternatives/meeting/meeting.xsd";		
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
	
	public void testCTA9() {
		String xmlfile = fDataDir+"/roger/type-alternatives/publication/publication.xml";
		String schemapath = fDataDir+"/roger/type-alternatives/publication/publication_v1.xsd";		
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
		
	public void testCTA10() {
		String xmlfile = fDataDir+"/roger/type-alternatives/publication/publication.xml";
		String schemapath = fDataDir+"/roger/type-alternatives/publication/publication_v5.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(fErrSysId != null || fFatErrSysId != null);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testCTA11() {
		String xmlfile = fDataDir+"/roger/type-alternatives/holiday.xml";
		String schemapath = fDataDir+"/roger/type-alternatives/holiday.xsd";		
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
	
	public void testCTA12() {
		String xmlfile = fDataDir+"/type-alternatives/test24.xml";
		String schemapath = fDataDir+"/type-alternatives/test24.xsd";		
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
	
	public void testCTA13() {
		String xmlfile = fDataDir+"/type-alternatives/test25.xml";
		String schemapath = fDataDir+"/type-alternatives/test25.xsd";		
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
	
	public void testCTA14() {
		// the test only tries to compile the schema
		String schemapath = fDataDir+"/xpath/xp2.xsd";			
		try {
			checkOnlyWarnings = true;
			fSchemaFactory.setErrorHandler(this);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
		    fSchemaFactory.setErrorHandler(null);
            assertTrue(warningList.size() == 1);
            // test expected warning messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("c-cta-xpath-b: The CTA XPath expression such as ('/E1') containing / or //, while in 'cta-full' mode, cannot yield a valid result (since a CTA tree is rooted at a parentless element).");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}

}
