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

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id:
 */
public class OpenContentTests extends XercesAbstractTestCase {

	public OpenContentTests(String name) {
		super(name);
	}
	
	public void testOpenContent1() {
		String xmlfile = fDataDir+"/open-content/test21.xml";
		String schemapath = fDataDir+"/open-content/test21.xsd";		
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
	
	public void testOpenContent2() {
		String xmlfile = fDataDir+"/open-content/test22.xml";
		String schemapath = fDataDir+"/open-content/test22.xsd";		
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
	
	public void testOpenContent3() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v1.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v1.xsd";	
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
	
	public void testOpenContent4() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v2.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v2.xsd";	
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
	
	public void testOpenContent5() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v3.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v3.xsd";	
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
	
	public void testOpenContent6() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v4.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v4.xsd";	
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
	
	public void testOpenContent7() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v5.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v5.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   // schema is incorrect. an exception indicates, that this test 
		   // has 'passed'.
		   assertTrue(true);
		}
	}
	
	public void testOpenContent8() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v6.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v6.xsd";	
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
	
	public void testOpenContent9() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v7.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v7.xsd";	
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
	
	public void testOpenContent10() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v8.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v8.xsd";	
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
	
	public void testOpenContent11() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v9.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v9.xsd";	
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
	
	public void testOpenContent12() {
		String xmlfile = fDataDir+"/roger/open-content/"+"BookStore_v10.xml";
		String schemapath = fDataDir+"/roger/open-content/"+"BookStore_v10.xsd";	
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
	
	public void testOpenContent13() {
		String xmlfile = fDataDir+"/open-content/test42.xml";
		String schemapath = fDataDir+"/open-content/test42.xsd";		
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
	
	public void testOpenContent14() {
		String xmlfile = fDataDir+"/open-content/test43.xml";
		String schemapath = fDataDir+"/open-content/test43.xsd";		
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
