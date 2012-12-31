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
 * @version $Id$
 */
public class AssertionTests extends XercesAbstractTestCase {

	public AssertionTests(String name) {
		super(name);
	}
	
	public void testAssert1() {
		String xmlfile = fDataDir+"/assertions/test1.xml";
		String schemapath = fDataDir+"/assertions/test1.xsd";		
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
	
	public void testAssert2() {
		String xmlfile = fDataDir+"/assertions/test2.xml";
		String schemapath = fDataDir+"/assertions/test2.xsd";		
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
	
	public void testAssert3() {
		String xmlfile = fDataDir+"/assertions/test3.xml";
		String schemapath = fDataDir+"/assertions/test3.xsd";		
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
	
	public void testAssert4() {
		String xmlfile = fDataDir+"/assertions/test4.xml";
		String schemapath = fDataDir+"/assertions/test4.xsd";		
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
	
	public void testAssert5() {
		String xmlfile = fDataDir+"/assertions/test5.xml";
		String schemapath = fDataDir+"/assertions/test5.xsd";		
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
	
	public void testAssert6() {
		String xmlfile = fDataDir+"/assertions/test6.xml";
		String schemapath = fDataDir+"/assertions/test6.xsd";		
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
	
	public void testAssert7() {
		String xmlfile = fDataDir+"/assertions/test7.xml";
		String schemapath = fDataDir+"/assertions/test7.xsd";		
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
	
	public void testAssert8() {
		String xmlfile = fDataDir+"/assertions/test8.xml";
		String schemapath = fDataDir+"/assertions/test8.xsd";		
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
	
	public void testAssert9() {
		String xmlfile = fDataDir+"/assertions/test9.xml";
		String schemapath = fDataDir+"/assertions/test9.xsd";		
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
	
	public void testAssert10() {
		String xmlfile = fDataDir+"/assertions/test10.xml";
		String schemapath = fDataDir+"/assertions/test10.xsd";		
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
	
	public void testAssert11() {
		String xmlfile = fDataDir+"/assertions/test11.xml";
		String schemapath = fDataDir+"/assertions/test11.xsd";		
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
	
	public void testAssert12() {
		String xmlfile = fDataDir+"/assertions/test12.xml";
		String schemapath = fDataDir+"/assertions/test12.xsd";		
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
	
	public void testAssert13() {
		String xmlfile = fDataDir+"/assertions/test13.xml";
		String schemapath = fDataDir+"/assertions/test13.xsd";		
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
	
	public void testAssert14() {
		String xmlfile = fDataDir+"/assertions/test14.xml";
		String schemapath = fDataDir+"/assertions/test14.xsd";		
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
	
	public void testAssert15() {
		String xmlfile = fDataDir+"/assertions/test19.xml";
		String schemapath = fDataDir+"/assertions/test19.xsd";		
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
	
	public void testAssert16() {
		String xmlfile = fDataDir+"/roger/assertions/classification/classification.xml";
		String schemapath = fDataDir+"/roger/assertions/classification/classification.xsd";		
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
	
	public void testAssert18() {
		String xmlfile = fDataDir+"/roger/assertions/conditional-presence/conditional-mode-of-transportation.xml";
		String schemapath = fDataDir+"/roger/assertions/conditional-presence/conditional-mode-of-transportation.xsd";		
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
	
	public void testAssert19() {
		String xmlfile = fDataDir+"/roger/assertions/even-integers/even-integers.xml";
		String schemapath = fDataDir+"/roger/assertions/even-integers/even-integers_v1.xsd";		
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
	
	public void testAssert20() {
		String xmlfile = fDataDir+"/roger/assertions/even-integers/even-integers.xml";
		String schemapath = fDataDir+"/roger/assertions/even-integers/even-integers_v2.xsd";		
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
	
	public void testAssert21() {
		String xmlfile = fDataDir+"/roger/assertions/meeting-time/meeting-time_v1.xml";
		String schemapath = fDataDir+"/roger/assertions/meeting-time/meeting-time_v1.xsd";		
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
	
	public void testAssert22() {
		String xmlfile = fDataDir+"/roger/assertions/meeting-time/meeting-time_v2.xml";
		String schemapath = fDataDir+"/roger/assertions/meeting-time/meeting-time_v2.xsd";		
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
	
	public void testAssert23() {
		String xmlfile = fDataDir+"/roger/assertions/meeting-time/meeting-time_v3.xml";
		String schemapath = fDataDir+"/roger/assertions/meeting-time/meeting-time_v3.xsd";		
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
	
	public void testAssert24() {
		String xmlfile = fDataDir+"/roger/assertions/paragraph/paragraph.xml";
		String schemapath = fDataDir+"/roger/assertions/paragraph/paragraph.xsd";		
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
	
	public void testAssert25() {
		String xmlfile = fDataDir+"/roger/assertions/whats-the-value-space/BarnesAndNoble.xml";
		String schemapath = fDataDir+"/roger/assertions/whats-the-value-space/BookStore.xsd";		
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
	
	public void testAssert26() {
		String xmlfile = fDataDir+"/roger/assertions/whats-the-value-space/BarnesAndNoble_v2.xml";
		String schemapath = fDataDir+"/roger/assertions/whats-the-value-space/BookStore_v2.xsd";		
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
	
	public void testAssert27() {
		String xmlfile = fDataDir+"/roger/assertions/whats-the-value-space/Borders.xml";
		String schemapath = fDataDir+"/roger/assertions/whats-the-value-space/BookStore.xsd";		
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
	
	public void testAssert28() {
		String xmlfile = fDataDir+"/roger/assertions/auto-loan-application.xml";
		String schemapath = fDataDir+"/roger/assertions/auto-loan-application.xsd";		
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
	
	public void testAssert29() {
		String xmlfile = fDataDir+"/roger/assertions/auto-loan-application-ns.xml";
		String schemapath = fDataDir+"/roger/assertions/auto-loan-application-ns.xsd";		
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
	
	public void testAssert30() {
		String xmlfile = fDataDir+"/roger/assertions/election-results.xml";
		String schemapath = fDataDir+"/roger/assertions/election-results.xsd";		
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
	
	public void testAssert31() {
		String xmlfile = fDataDir+"/roger/assertions/todays-activity.xml";
		String schemapath = fDataDir+"/roger/assertions/todays-activity.xsd";		
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
	
	public void testAssert32() {
		String xmlfile = fDataDir+"/roger/assertions/classification.xml";
		String schemapath = fDataDir+"/roger/assertions/classification.xsd";		
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
	
	public void testAssert33() {
		String xmlfile = fDataDir+"/assertions/derivation1.xml";
		String schemapath = fDataDir+"/assertions/derivation1.xsd";		
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
	
	public void testAssert34() {
		String xmlfile = fDataDir+"/assertions/derivation2.xml";
		String schemapath = fDataDir+"/assertions/derivation2.xsd";		
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
	
	public void testAssert35() {
		String xmlfile = fDataDir+"/assertions/test23.xml";
		String schemapath = fDataDir+"/assertions/test23.xsd";		
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
	
	public void testAssert36() {
		String xmlfile = fDataDir+"/assertions/test25.xml";
		String schemapath = fDataDir+"/assertions/test25.xsd";		
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
	
	public void testAssert37() {
		String xmlfile = fDataDir+"/assertions/test26.xml";
		String schemapath = fDataDir+"/assertions/test26.xsd";		
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
	
	public void testAssert38() {
		String xmlfile = fDataDir+"/assertions/test27.xml";
		String schemapath = fDataDir+"/assertions/test27.xsd";		
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
	
	public void testAssert39() {
		String xmlfile = fDataDir+"/assertions/test28.xml";
		String schemapath = fDataDir+"/assertions/test28.xsd";		
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
	
	public void testAssert40() {
		String xmlfile = fDataDir+"/assertions/test29.xml";
		String schemapath = fDataDir+"/assertions/test29.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
              errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
              errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert41() {
		String xmlfile = fDataDir+"/assertions/test30.xml";
		String schemapath = fDataDir+"/assertions/test30.xsd";		
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
	
	public void testAssert42() {
		String xmlfile = fDataDir+"/assertions/test31.xml";
		String schemapath = fDataDir+"/assertions/test31.xsd";		
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
	
	public void testAssert43() {
		String xmlfile = fDataDir+"/assertions/test32.xml";
		String schemapath = fDataDir+"/assertions/test32.xsd";		
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
	
	public void testAssert44() {
		String xmlfile = fDataDir+"/assertions/test33.xml";
		String schemapath = fDataDir+"/assertions/test33.xsd";		
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
	
	public void testAssert45() {
		String xmlfile = fDataDir+"/assertions/test34.xml";
		String schemapath = fDataDir+"/assertions/test34.xsd";		
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
	
	public void testAssert46() {
		String xmlfile = fDataDir+"/assertions/test35.xml";
		String schemapath = fDataDir+"/assertions/test35.xsd";		
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
	
	public void testAssert47() {
		String xmlfile = fDataDir+"/assertions/test40.xml";
		String schemapath = fDataDir+"/assertions/test40.xsd";		
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
	
	public void testAssert48() {
		String xmlfile = fDataDir+"/assertions/test41.xml";
		String schemapath = fDataDir+"/assertions/test41.xsd";		
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
	
	public void testAssert49() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion1.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}

	public void testAssert50() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion2.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion2.xsd";		
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
	
	public void testAssert51() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion3.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion3.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert52() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion3.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion4.xsd";		
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
	
	public void testAssert53() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion4.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion5.xsd";		
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
	
	public void testAssert54() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion5.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion5.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert55() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion6.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion6.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert56() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion7.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion6.xsd";		
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
	
	public void testAssert57() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion8.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion7.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert58() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion8.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion8.xsd";		
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
	
	public void testAssert59() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion9.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion9.xsd";		
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
	
	public void testAssert60() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion10.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion10.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert61() {
		String xmlfile = fDataDir+"/assertions/test42.xml";
		String schemapath = fDataDir+"/assertions/test42.xsd";		
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
	
	public void testAssert62() {
		String xmlfile = fDataDir+"/assertions/test43.xml";
		String schemapath = fDataDir+"/assertions/test43.xsd";		
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
	
	public void testAssert63() {
		String xmlfile = fDataDir+"/assertions/test44.xml";
		String schemapath = fDataDir+"/assertions/test44.xsd";		
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
	
	public void testAssert64() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion11.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion11.xsd";		
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
	
	public void testAssert65() {
		String xmlfile = fDataDir+"/assertions/whitespace/test1.xml";
		String schemapath = fDataDir+"/assertions/whitespace/test1.xsd";		
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
	
	public void testAssert66() {
		String xmlfile = fDataDir+"/assertions/whitespace/test1.xml";
		String schemapath = fDataDir+"/assertions/whitespace/test2.xsd";			
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert67() {
		String xmlfile = fDataDir+"/assertions/whitespace/test3.xml";
		String schemapath = fDataDir+"/assertions/whitespace/test3.xsd";		
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
	
	public void testAssert68() {
		String xmlfile = fDataDir+"/assertions/whitespace/test3.xml";
		String schemapath = fDataDir+"/assertions/whitespace/test4.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert69() {
		String xmlfile = fDataDir+"/assertions/whitespace/test3.xml";
		String schemapath = fDataDir+"/assertions/whitespace/test5.xsd";		
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
	
	public void testAssert70() {
		String xmlfile = fDataDir+"/assertions/po_sample/po.xml";
		String schemapath = fDataDir+"/assertions/po_sample/po.xsd";		
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
	
	public void testAssert71() {
		String xmlfile = fDataDir+"/assertions/namespace/ns1.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns1.xsd";		
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
	
	public void testAssert72() {
		String xmlfile = fDataDir+"/assertions/namespace/ns2.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns2.xsd";		
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
	
	public void testAssert73() {
		String xmlfile = fDataDir+"/assertions/namespace/ns3.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns3.xsd";		
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
	
	public void testAssert74() {
		String xmlfile = fDataDir+"/assertions/namespace/ns4.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns4.xsd";		
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
	
	public void testAssert75() {
		String xmlfile = fDataDir+"/assertions/namespace/ns5.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns5.xsd";		
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
	
	public void testAssert76() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion12.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion12.xsd";		
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
	
	public void testAssert77() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion13.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion13.xsd";		
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
	
	public void testAssert78() {
		String xmlfile = fDataDir+"/assertions/modelgroup/test1.xml";
		String schemapath = fDataDir+"/assertions/modelgroup/test1.xsd";		
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
	
	public void testAssert79() {
		String xmlfile = fDataDir+"/assertions/modelgroup/test2.xml";
		String schemapath = fDataDir+"/assertions/modelgroup/test1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert80() {
		String xmlfile = fDataDir+"/assertions/wildcard/assert_wc1.xml";
		String schemapath = fDataDir+"/assertions/wildcard/assert_wc1.xsd";		
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
	
	public void testAssert81() {
		String xmlfile = fDataDir+"/assertions/modelgroup/test3.xml";
		String schemapath = fDataDir+"/assertions/modelgroup/test3.xsd";		
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
	
	public void testAssert82() {
		String xmlfile = fDataDir+"/assertions/wildcard/assert_wc2.xml";
		String schemapath = fDataDir+"/assertions/wildcard/assert_wc2_1.xsd";		
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
	
	public void testAssert83() {
		String xmlfile = fDataDir+"/assertions/wildcard/assert_wc2.xml";
		String schemapath = fDataDir+"/assertions/wildcard/assert_wc2_2.xsd";		
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
	
	public void testAssert84() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_atomic1.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_atomic1.xsd";		
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
	
	public void testAssert85() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_atomic2.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_atomic1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert86() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_list1.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_list1.xsd";		
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
	
	public void testAssert87() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_list2.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_list1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert88() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_union1.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_union1.xsd";		
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
	
	public void testAssert89() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_union2.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_union1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert90() {
		String xmlfile = fDataDir+"/assertions/attributes/test_attr_union3.xml";
		String schemapath = fDataDir+"/assertions/attributes/test_attr_union1.xsd";		
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
	
	public void testAssert91() {
		String xmlfile = fDataDir+"/assertions/list_union/list1.xml";
		String schemapath = fDataDir+"/assertions/list_union/list1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert92() {
		String xmlfile = fDataDir+"/assertions/list_union/list1.xml";
		String schemapath = fDataDir+"/assertions/list_union/list2.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert93() {
		String xmlfile = fDataDir+"/assertions/defaultValues/test1.xml";
		String schemapath = fDataDir+"/assertions/defaultValues/test1.xsd";		
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
	
	public void testAssert94() {
		String xmlfile = fDataDir+"/assertions/defaultValues/test2.xml";
		String schemapath = fDataDir+"/assertions/defaultValues/test2.xsd";		
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
	
	public void testAssert95() {
		String xmlfile = fDataDir+"/assertions/defaultValues/test1.xml";
		String schemapath = fDataDir+"/assertions/defaultValues/test3.xsd";		
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
	
	public void testAssert96() {
		String xmlfile = fDataDir+"/assertions/defaultValues/test1.xml";
		String schemapath = fDataDir+"/assertions/defaultValues/test4.xsd";		
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
	
	public void testAssert97() {
		String xmlfile = fDataDir+"/assertions/defaultValues/test3.xml";
		String schemapath = fDataDir+"/assertions/defaultValues/test5.xsd";		
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
	
	public void testAssert98() {
		String xmlfile = fDataDir+"/assertions/list_union/list3.xml";
		String schemapath = fDataDir+"/assertions/list_union/list3.xsd";		
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
	
	public void testAssert99() {
		String xmlfile = fDataDir+"/assertions/list_union/list2.xml";
		String schemapath = fDataDir+"/assertions/list_union/list3.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // assertion must fail for test case to succeed
            String errStr = null;
            if (fErrSysId != null) {
               errStr = fErrSysId; 	
            }
            else if (fFatErrSysId != null) {
               errStr = fFatErrSysId; 	
            }
            assertNotNull(errStr);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert100() {
		String xmlfile = fDataDir+"/assertions/ST/list/test1.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // test a
            assertTrue(failureList.size() == 6);
            
            // test b
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("for element 'list' on schema type 'MY_INT'");
            mesgFragments.setMessageFragment("Assertion failed for an xs:list member value '1'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("for element 'list' on schema type 'MY_INT'");
            mesgFragments.setMessageFragment("Assertion failed for an xs:list member value '3'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("for element 'a' on schema type 'MY_INT'");
            expectedMsgList.add(mesgFragments);            
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert101() {
		String xmlfile = fDataDir+"/assertions/ST/list/test2.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test2.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 6);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert102() {
		String xmlfile = fDataDir+"/assertions/ST/list/test3.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test3.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 6);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert103() {
		String xmlfile = fDataDir+"/assertions/ST/list/test4.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test4.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 4);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert104() {
		String xmlfile = fDataDir+"/assertions/ST/list/test5.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test5.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 8);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert105() {
		String xmlfile = fDataDir+"/assertions/ST/list/test6.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test6.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 6);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert106() {
		String xmlfile = fDataDir+"/assertions/ST/list/test7.xml";
		String schemapath = fDataDir+"/assertions/ST/list/test7.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 20);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert107() {
		String xmlfile = fDataDir+"/assertions/test46_1.xml";
		String schemapath = fDataDir+"/assertions/test46.xsd";		
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
	
	public void testAssert108() {
		String xmlfile = fDataDir+"/assertions/test46_2.xml";
		String schemapath = fDataDir+"/assertions/test46.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('z') for element 'X' on schema type 'X_TYPE' did not succeed");
            expectedMsgList.add(mesgFragments);            
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert109() {
		String xmlfile = fDataDir+"/assertions/test46_3.xml";
		String schemapath = fDataDir+"/assertions/test46.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('y') for element 'X' on schema type 'X_TYPE' did not succeed");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('z') for element 'X' on schema type 'X_TYPE' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert110() {
		String xmlfile = fDataDir+"/assertions/test46_2.xml";
		String schemapath = fDataDir+"/assertions/test47.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('z') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);            
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert111() {
		String xmlfile = fDataDir+"/assertions/test46_3.xml";
		String schemapath = fDataDir+"/assertions/test47.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('y') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('z') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert112() {
		String xmlfile = fDataDir+"/assertions/test48_1.xml";
		String schemapath = fDataDir+"/assertions/test48.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 5);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('@a') for element 'X' on schema type 'X_TYPE' did not succeed");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('$value mod 2 = 0') for element 'X' on schema type '#AnonType_INTLIST' did not succeed");
            mesgFragments.setMessageFragment("failed for an xs:list member value '1'");
            expectedMsgList.add(mesgFragments);            
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('$value mod 2 = 0') for element 'X' on schema type '#AnonType_INTLIST' did not succeed");
            mesgFragments.setMessageFragment("failed for an xs:list member value '3'");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert113() {
		String xmlfile = fDataDir+"/assertions/test48_2.xml";
		String schemapath = fDataDir+"/assertions/test48.xsd";		
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
	
	public void testAssert114() {
		String xmlfile = fDataDir+"/assertions/wildcard/test1_1.xml";
		String schemapath = fDataDir+"/assertions/wildcard/test1.xsd";	
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
	
	public void testAssert115() {
		String xmlfile = fDataDir+"/assertions/wildcard/test1_2.xml";
		String schemapath = fDataDir+"/assertions/wildcard/test1.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('a or b') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert116() {
		String xmlfile = fDataDir+"/assertions/wildcard/test2_1.xml";
		String schemapath = fDataDir+"/assertions/wildcard/test2.xsd";	
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
	
	public void testAssert117() {
		String xmlfile = fDataDir+"/assertions/wildcard/test2_2.xml";
		String schemapath = fDataDir+"/assertions/wildcard/test2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("evaluation ('not(notallowed)') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert118() {
		String xmlfile = fDataDir+"/assertions/errors/test1.xml";
		String schemapath = fDataDir+"/assertions/errors/test1.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('countxx(a) gt 1') for element 'X' on schema type '#AnonType_X' did not succeed");
            mesgFragments.setMessageFragment("XPST0017 - Function does not exist: countxx arity: 1");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert119() {
		String xmlfile = fDataDir+"/assertions/errors/test2.xml";
		String schemapath = fDataDir+"/assertions/errors/test2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertions-valid: Value '100' is not facet-valid with respect to assertion '. = 100'");
            mesgFragments.setMessageFragment("XPDY0002 - Context is undefined");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('. = 100') for element 'X' on schema type '#AnonType_X' did not succeed");
            mesgFragments.setMessageFragment("XPDY0002 - Context is undefined");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert120() {
		String xmlfile = fDataDir+"/assertions/errors/test1.xml";
		String schemapath = fDataDir+"/assertions/errors/test3.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('xx:my = 'http://xx'') for element 'X' on schema type '#AnonType_X' did not succeed");
            mesgFragments.setMessageFragment("XPST0081 - Unknown prefix: xx");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert121() {
		String xmlfile = fDataDir+"/assertions/errors/test1.xml";
		String schemapath = fDataDir+"/assertions/errors/test4.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('xx:my = 'hello'') for element 'X' on schema type '#AnonType_X' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert122() {
		String xmlfile = fDataDir+"/assertions/test49.xml";
		String schemapath = fDataDir+"/assertions/test49.xsd";		
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
	
	public void testAssert123() {
		String xmlfile = fDataDir+"/assertions/test49.xml";
		String schemapath = fDataDir+"/assertions/test50.xsd";		
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
	
	public void testAssert124() {
		String xmlfile = fDataDir+"/assertions/namespace/ns6.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns6_1.xsd";		
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
	
	public void testAssert125() {
		String xmlfile = fDataDir+"/assertions/namespace/ns6.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns6_2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('namespace-uri-from-QName(node-name(ns0:Y)) = 'http://x.y1'') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('namespace-uri-from-QName(node-name(Y)) = 'http://x.y1'') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert126() {
		String xmlfile = fDataDir+"/assertions/misc_xpath/instOf1.xml";
		String schemapath = fDataDir+"/assertions/misc_xpath/instOf_1.xsd";		
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
	
	public void testAssert127() {
		String xmlfile = fDataDir+"/assertions/misc_xpath/instOf1.xml";
		String schemapath = fDataDir+"/assertions/misc_xpath/instOf_2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('Y instance of xs:integer') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('@att1 instance of xs:string') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert128() {
		String xmlfile = fDataDir+"/assertions/namespace/ns7.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns7_1.xsd";		
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
	
	public void testAssert129() {
		String xmlfile = fDataDir+"/assertions/namespace/ns7.xml";
		String schemapath = fDataDir+"/assertions/namespace/ns7_2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('namespace-uri-from-QName(node-name(*[1])) = 'http://x.y1'') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('namespace-uri-from-QName(node-name(*[2])) = 'http://x.y2'') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert130() {
		String xmlfile = fDataDir+"/assertions/x_1.xml";
		String schemapath = fDataDir+"/assertions/x_1.xsd";		
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
	
	public void testAssert131() {
		String xmlfile = fDataDir+"/assertions/x_1.xml";
		String schemapath = fDataDir+"/assertions/x_2.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('. instance of element(X11, xs:anyType)') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert132() {
		String xmlfile = fDataDir+"/assertions/x_1.xml";
		String schemapath = fDataDir+"/assertions/x_3.xsd";			
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('data(.) instance of xs:string') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('data(.) instance of xs:anyAtomicType') for element 'X' on schema type '#AnonType_X' did not succeed.");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert133() {
		// the test only tries to compile the schema
		String schemapath = fDataDir+"/xpath/xp1.xsd";			
		try {
			checkOnlyWarnings = true;
			fSchemaFactory.setErrorHandler(this);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
		    fSchemaFactory.setErrorHandler(null);
            assertTrue(warningList.size() == 2);
            // test expected warning messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-xpath.3.13.4.2b: An assert XPath expression such as ('/X') containing / or //, on the schema type 'TYP1', cannot yield a valid result (since an assert tree is rooted at a parentless element).");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-xpath.3.13.4.2b: An assert XPath expression such as ('X[//Y]') containing / or //, on the schema type 'TYP1', cannot yield a valid result (since an assert tree is rooted at a parentless element).");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert134() {
		String xmlfile = fDataDir+"/assertions/misc_xpath/x1_1.xml";
		String schemapath = fDataDir+"/assertions/misc_xpath/x1.xsd";		
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
	
	public void testAssert135() {
		String xmlfile = fDataDir+"/assertions/misc_xpath/x1_2.xml";
		String schemapath = fDataDir+"/assertions/misc_xpath/x1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-assertion: Assertion evaluation ('(@end - @start) = xs:dayTimeDuration('P1D')') for element 'temp' on schema type '#AnonType_temp' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert136() {
		String xmlfile = fDataDir+"/assertions/misc_xpath/x1_3.xml";
		String schemapath = fDataDir+"/assertions/misc_xpath/x1.xsd";		
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
	
	public void testAssert137() {
		String xmlfile = fDataDir+"/assertions/valuecomparison/x1.xml";
		String schemapath = fDataDir+"/assertions/valuecomparison/x1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation ('deep-equal(item/substring-after(., 'a'), data(@attr))') for element 'list' on schema type '#AnonType_list' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert138() {
		String xmlfile = fDataDir+"/assertions/valuecomparison/x2.xml";
		String schemapath = fDataDir+"/assertions/valuecomparison/x2.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 4);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Value '' is not facet-valid with respect to assertion '1 eq '1''. XPTY0004 - Value does not match a required type");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation ('1 eq '1'') for element 'emptydoc' on schema type '#AnonType_emptydoc' did not succeed. XPTY0004 - Value does not match a required type");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Value '' is not facet-valid with respect to assertion ''1' eq 1'. XPTY0004 - Value does not match a required type");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation (''1' eq 1') for element 'emptydoc' on schema type '#AnonType_emptydoc' did not succeed. XPTY0004 - Value does not match a required type");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert139() {
		String xmlfile = fDataDir+"/assertions/valuecomparison/x2.xml";
		String schemapath = fDataDir+"/assertions/valuecomparison/x3.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Value '' is not facet-valid with respect to assertion 'deep-equal((1, 2), ('1', '2'))'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation ('deep-equal((1, 2), ('1', '2'))') for element 'emptydoc' on schema type '#AnonType_emptydoc' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testAssert140() {
		String xmlfile = fDataDir+"/xpath/fnindexof.xml";
		String schemapath = fDataDir+"/xpath/fnindexof.xsd";		
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
	
	public void testAssert141() {
		String xmlfile = fDataDir+"/xpath/fnindexof.xml";
		String schemapath = fDataDir+"/xpath/fnindexof.xsd";		
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
	
	public void testAssert142() {
		String xmlfile = fDataDir+"/xpath/typedvalue_1.xml";
		String schemapath = fDataDir+"/xpath/typedvalue_1.xsd";		
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
	
	public void testAssert143() {
		String xmlfile = fDataDir+"/assertions/castableas.xml";
		String schemapath = fDataDir+"/assertions/castableas.xsd";		
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
	
	public void testAssert144() {
		String xmlfile = fDataDir+"/xpath/fnindexof_1.xml";
		String schemapath = fDataDir+"/xpath/fnindexof_1.xsd";		
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
	
	public void testAssert145() {
		String xmlfile = fDataDir+"/xpath/fndistinctvalues.xml";
		String schemapath = fDataDir+"/xpath/fndistinctvalues.xsd";		
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
	
	public void testAssert146() {
		String xmlfile = fDataDir+"/xpath/floating_point.xml";
		String schemapath = fDataDir+"/xpath/floating_point.xsd";		
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
	
	public void testAssert147() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion14_1.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion14_1.xsd";		
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
	
	public void testAssert148() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion14_1.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion14_3.xsd";		
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
	
	public void testAssert149() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion14_2.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion14_2.xsd";		
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
	
	public void testAssert150() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion14_2.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion14_4.xsd";		
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
	
	public void testAssert151() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion15_1.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion15_1.xsd";		
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

	public void testAssert152() {
		String xmlfile = fDataDir+"/assertions/list_union/listunion15_2.xml";
		String schemapath = fDataDir+"/assertions/list_union/listunion15_1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation ('data(X[1]/@val) instance of xs:date+') for element 'Z' on schema type '#AnonType_Z' did not succeed");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Assertion evaluation ('count(data(X[1]/@val)) eq 3') for element 'Z' on schema type '#AnonType_Z' did not succeed");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
}
