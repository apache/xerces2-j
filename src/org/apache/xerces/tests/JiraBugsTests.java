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

import org.xml.sax.SAXException;

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id$
 */
public class JiraBugsTests extends XercesAbstractTestCase {

	public JiraBugsTests(String name) {
		super(name);
	}
	
	public void testJira_1578_1() {
		String schemapath = fDataDir+"/jira_bugs/1578_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1578_2() {
		String schemapath = fDataDir+"/jira_bugs/1578_2.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1578_3() {
		String schemapath = fDataDir+"/jira_bugs/1578_3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1578_4() {
		String schemapath = fDataDir+"/jira_bugs/1578_4.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1577_1() {
		String schemapath = fDataDir+"/jira_bugs/1577_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1577_2() {
		String schemapath = fDataDir+"/jira_bugs/1577_2.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1585_1() {
		String xmlfile = fDataDir+"/jira_bugs/1585_1.xml";	
		String schemapath = fDataDir+"/jira_bugs/1585_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Identity Constraint error:");
            mesgFragments.setMessageFragment("the keyref identity constraint \"newKeyref\" refers to a key or unique that is out of scope");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1584_1() {
		String xmlfile = fDataDir+"/jira_bugs/3.xml";	
		String schemapath = fDataDir+"/jira_bugs/3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Attribute 'attr' must appear on element 'el'");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1584_2() {
		String xmlfile = fDataDir+"/jira_bugs/3_1.xml";	
		String schemapath = fDataDir+"/jira_bugs/3_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-maxInclusive-valid: Value '60' is not facet-valid with respect to maxInclusive '50' for type 'st'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-type.3.1.3: The value '60' of element 'el' is not valid");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1584_3() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
		String xmlfile = fDataDir+"/jira_bugs/3.xml";	
		String schemapath = fDataDir+"/jira_bugs/3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Attribute 'attr' must appear on element 'el'");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1584_4() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
		String xmlfile = fDataDir+"/jira_bugs/3_1.xml";	
		String schemapath = fDataDir+"/jira_bugs/3_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-maxInclusive-valid: Value '60' is not facet-valid with respect to maxInclusive '50' for type 'st'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-type.3.1.3: The value '60' of element 'el' is not valid");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1591_1() {
		String xmlfile = fDataDir+"/jira_bugs/ag.xml";	
		String schemapath = fDataDir+"/jira_bugs/ag3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Attribute 'id' must appear on element 'el'");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1591_2() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
		String xmlfile = fDataDir+"/jira_bugs/ag.xml";	
		String schemapath = fDataDir+"/jira_bugs/ag3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("Attribute 'id' must appear on element 'el'");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1591_3() {
		String xmlfile = fDataDir+"/jira_bugs/gr.xml";	
		String schemapath = fDataDir+"/jira_bugs/gr3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("The content of element 'el' is not complete. One of '{\"tns\":x}' is expected");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1591_4() {
		// run validation in XSD 1.0 mode
		fSchemaFactory = SchemaFactory.newInstance(DEFAULT_SCHEMA_LANGUAGE);
		String xmlfile = fDataDir+"/jira_bugs/gr.xml";	
		String schemapath = fDataDir+"/jira_bugs/gr3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 1);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("The content of element 'el' is not complete. One of '{\"tns\":x}' is expected");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1599_1() {
		String schemapath = fDataDir+"/jira_bugs/1599_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1599_2() {
		String schemapath = fDataDir+"/jira_bugs/1599_2.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1599_3() {
		String schemapath = fDataDir+"/jira_bugs/1599_3.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testJira_1603() {
		String schemapath = fDataDir+"/jira_bugs/1603_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
		} catch(SAXException ex) {
			// test expected error messages
			assertEquals("src-ct.5: Complex Type Definition Representation Error for type 'bCT'. If <openContent> is present and the actual value of its mode [attribute] is not none, then there must be an <any> among the [children] of <openContent>.", ex.getMessage());
		}
	}
	
	public void testJira_1605() {
		String schemapath = fDataDir+"/jira_bugs/1605_1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
		} catch(SAXException ex) {
			// test expected error messages
            assertEquals("cos-nonambig: Element1 and Element1 (or elements from their substitution group) violate \"Unique Particle Attribution\". During validation against this schema, ambiguity would be created for those two particles.", ex.getMessage());
		}
	}
	
	public void testJira_1608_1() {
		String xmlfile = fDataDir+"/jira_bugs/1608_1.xml";
		String schemapath = fDataDir+"/jira_bugs/1608_1.xsd";		
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
	
	public void testJira_1608_2() {
		String xmlfile = fDataDir+"/jira_bugs/1608_2.xml";
		String schemapath = fDataDir+"/jira_bugs/1608_1.xsd";		
		try {
			Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
		
}
