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
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class TargetNamespaceTests extends XercesAbstractTestCase {

	public TargetNamespaceTests(String name) {
		super(name);
	}
	
	public void testTargetNamespace1() {
		String xmlfile = fDataDir+"/roger/targetNamespace/BookStore.xml";
		String schemapath = fDataDir+"/roger/targetNamespace/BookStore.xsd";	
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
	
	public void testTargetNamespace2() {
		String xmlfile = fDataDir+"/targetNamespace/tns1.xml";
		String schemapath = fDataDir+"/targetNamespace/tns1.xsd";	
		try {
			fSchemaFactory.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, false);
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
	
	public void testTargetNamespace3() {
		String xmlfile = fDataDir+"/targetNamespace/tns2.xml";
		String schemapath = fDataDir+"/targetNamespace/tns2.xsd";	
		try {
			fSchemaFactory.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, false);
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
	
	public void testTargetNamespace4() {
		String xmlfile = fDataDir+"/targetNamespace/tns3.xml";
		String schemapath = fDataDir+"/targetNamespace/tns3.xsd";	
		try {
			fSchemaFactory.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, false);
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
	
	public void testTargetNamespace5() {
		String xmlfile = fDataDir+"/targetNamespace/tns4.xml";
		String schemapath = fDataDir+"/targetNamespace/tns4.xsd";	
		try {
			// test 'a'
			// run validation in XSD 1.1 mode
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            
            // test 'b'
            // run validation in XSD 1.0 mode
    		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
    		s = fSchemaFactory.newSchema(new StreamSource(schemapath));
    		v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testTargetNamespace6() {
		String xmlfile = fDataDir+"/targetNamespace/tns5.xml";
		String schemapath = fDataDir+"/targetNamespace/tns5.xsd";	
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
	
	public void testTargetNamespace7() {
		String xmlfile = fDataDir+"/targetNamespace/tns6.xml";
		String schemapath = fDataDir+"/targetNamespace/tns6.xsd";	
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
	
	public void testTargetNamespace8() {
		String xmlfile = fDataDir+"/targetNamespace/tns7.xml";
		String schemapath = fDataDir+"/targetNamespace/tns7.xsd";	
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
	
	public void testTargetNamespace9() {
		String xmlfile = fDataDir+"/targetNamespace/tns7.xml";
		String schemapath = fDataDir+"/targetNamespace/tns8.xsd";	
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
	
	public void testTargetNamespace10() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns9.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns9.xsd";	
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
	
	public void testTargetNamespace11() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns9.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns10.xsd";	
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
	
	public void testTargetNamespace12() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns10.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns11.xsd";	
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
	
	public void testTargetNamespace13() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns10.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns12.xsd";	
		try {
			// run validation in 1.0 mode
			fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
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
	
	public void testTargetNamespace14() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns11.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns13.xsd";	
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
	
	public void testTargetNamespace15() {
		String xmlfile = fDataDir+"/targetNamespace/example1/tns12.xml";
		String schemapath = fDataDir+"/targetNamespace/example1/tns14.xsd";	
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
