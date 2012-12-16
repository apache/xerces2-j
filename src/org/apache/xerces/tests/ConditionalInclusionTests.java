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
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXParseException;

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id:
 */
public class ConditionalInclusionTests extends XercesAbstractTestCase {

	public ConditionalInclusionTests(String name) {
		super(name);
	}
	
	public void testVersionControl1() {
		String xmlfile = fDataDir+"/version-control/test36.xml";
		String schemapath = fDataDir+"/version-control/test36.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // validation failure indicates the 'test' success
            assertNotNull(fErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl2() {
		String xmlfile = fDataDir+"/version-control/test37.xml";
		String schemapath = fDataDir+"/version-control/test37.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // validation failure indicates the 'test' success
            assertNotNull(fErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl3() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);		
		String xmlfile = fDataDir+"/version-control/test37.xml";
		String schemapath = fDataDir+"/version-control/test37.xsd";		
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
	
	public void testVersionControl4() {
		String xmlfile = fDataDir+"/version-control/test38.xml";
		String schemapath = fDataDir+"/version-control/test38.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // validation failure indicates the 'test' success
            assertNotNull(fErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl5() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);		
		String xmlfile = fDataDir+"/version-control/test38.xml";
		String schemapath = fDataDir+"/version-control/test38.xsd";		
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
	
	public void testVersionControl6() {
		String xmlfile = fDataDir+"/version-control/test39.xml";
		String schemapath = fDataDir+"/version-control/test39.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // validation failure indicates the 'test' success
            assertNotNull(fErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl7() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);		
		String xmlfile = fDataDir+"/version-control/test40.xml";
		String schemapath = fDataDir+"/version-control/test39.xsd";		
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
	
	public void testVersionControl8() {
		String xmlfile = fDataDir+"/roger/version-control/BookStore.xml";
		String schemapath = fDataDir+"/roger/version-control/BookStore.xsd";		
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
	
	public void testVersionControl9() {		
		String xmlfile = fDataDir+"/version-control/test44.xml";
		String schemapath = fDataDir+"/version-control/test44.xsd";		
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
	
	public void testVersionControl10() {		
		String xmlfile = fDataDir+"/version-control/test44.xml";
		String schemapath = fDataDir+"/version-control/test45.xsd";		
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
	
	public void testVersionControl11() {		
		String xmlfile = fDataDir+"/version-control/test44.xml";
		String schemapath = fDataDir+"/version-control/test46.xsd";		
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
	
	public void testVersionControl12() {		
		String xmlfile = fDataDir+"/version-control/test44.xml";
		String schemapath = fDataDir+"/version-control/test47.xsd";		
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
	
	public void testVersionControl13() {		
		String xmlfile = fDataDir+"/version-control/test45.xml";
		String schemapath = fDataDir+"/version-control/test48.xsd";		
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
	
	public void testVersionControl14() {		
		String xmlfile = fDataDir+"/version-control/test49.xml";
		String schemapath = fDataDir+"/version-control/test49.xsd";
		final String expectedErrMesgFragment10 = "The value 'a' of element 'Y' is not valid";
		final String expectedErrMesgFragment11 = "An simple schema type defines the content to be EMPTY";
		try {
			// test a) run validation in XSD 1.0 mode
			// instance validation fails.
			fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertEquals(true, fErrorMessage.indexOf(expectedErrMesgFragment10) != -1);            
            // test b) run validation in XSD 1.1 mode
            // instance validation fails.
            fSchemaFactory = SchemaFactory.newInstance(SCHEMA_11_LANGUAGE);
            s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertEquals(true, fErrorMessage.indexOf(expectedErrMesgFragment11) != -1);            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl15() {		
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test1.xsd";		
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
	
	public void testVersionControl16() {
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-complex-type.2.4.a: Invalid content was found starting with element 'x'. One of '{y}' is expected");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl17() {
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test3.xsd";
		List expectedMsgList = new ArrayList();
        FailureMesgFragments mesgFragments = null;
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-complex-type.2.4.a: Invalid content was found starting with element 'x'. One of '{y}' is expected");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		}
		catch(SAXParseException ex) {
			mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("UndeclaredPrefix: Cannot resolve 'a:t1' as a QName: the prefix 'a' is not declared");
            expectedMsgList.add(mesgFragments);
	    }
		catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl18() {		
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test4.xsd";		
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
	
	public void testVersionControl19() {
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test5.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-complex-type.2.4.a: Invalid content was found starting with element 'x'. One of '{y}' is expected");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testVersionControl20() {		
		String xmlfile = fDataDir+"/version-control/test2.xml";
		String schemapath = fDataDir+"/version-control/test6.xsd";		
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
	
	public void testVersionControl21() {		
		String xmlfile = fDataDir+"/version-control/test1.xml";
		String schemapath = fDataDir+"/version-control/test7.xsd";		
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
	
}
