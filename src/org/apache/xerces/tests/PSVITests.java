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

import java.util.Map;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.assertion.XSAssert;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.PSVIProvider;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeAlternative;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.xml.sax.InputSource;

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id$
 */
public class PSVITests extends XercesAbstractTestCase {
	
	public PSVITests(String name) {
		super(name);
	}
	
	public void testPsvi1() {
		String xmlfile = fDataDir+"/psvi/test1.xml";
		String schemapath = fDataDir+"/psvi/test1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
            PSVIProvider psviProvider = (PSVIProvider) v;
            PSVIHandler psviHandler = new PSVIHandler(psviProvider);
		    v.setErrorHandler(this);
            v.validate(new SAXSource(new InputSource(xmlfile)), new SAXResult(psviHandler));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            // check memberType PSVI property
            XSSimpleTypeDefinition memType = ((ElementPSVI)(psviHandler.getElementPsviInfo()).get("X")).getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType).getName(), "date");            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testPsvi2() {
		String xmlfile = fDataDir+"/psvi/test1.xml";
		String schemapath = fDataDir+"/psvi/test2.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
            PSVIProvider psviProvider = (PSVIProvider) v;
            PSVIHandler psviHandler = new PSVIHandler(psviProvider);
		    v.setErrorHandler(this);
            v.validate(new SAXSource(new InputSource(xmlfile)), new SAXResult(psviHandler));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            // check memberType PSVI property
            XSSimpleTypeDefinition memType = ((ElementPSVI)(psviHandler.getElementPsviInfo()).get("X")).getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType).getName(), "MYDATE2");            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testPsvi3() {
		String xmlfile = fDataDir+"/psvi/test2.xml";
		String schemapath = fDataDir+"/psvi/test2.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
            PSVIProvider psviProvider = (PSVIProvider) v;
            PSVIHandler psviHandler = new PSVIHandler(psviProvider);
		    v.setErrorHandler(this);
            v.validate(new SAXSource(new InputSource(xmlfile)), new SAXResult(psviHandler));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            // check memberType PSVI property
            XSSimpleTypeDefinition memType = ((ElementPSVI)(psviHandler.getElementPsviInfo()).get("X")).getMemberTypeDefinition();         
            assertEquals(((XSSimpleTypeDecl) memType).getName(), "MYDATE2");            
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testPsvi4() {
		String xmlfile = fDataDir+"/psvi/test3.xml";
		String schemapath = fDataDir+"/psvi/test3.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
            PSVIProvider psviProvider = (PSVIProvider) v;
            PSVIHandler psviHandler = new PSVIHandler(psviProvider);
		    v.setErrorHandler(this);
            v.validate(new SAXSource(new InputSource(xmlfile)), new SAXResult(psviHandler));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            // check memberType PSVI property
            Map attrPsviInfo = psviHandler.getAttributePsviInfo();
            AttributePSVI attrPsvi1 = (AttributePSVI) attrPsviInfo.get("dt1");
            XSSimpleTypeDefinition memType1 = attrPsvi1.getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType1).getName(), "date");
            AttributePSVI attrPsvi2 = (AttributePSVI) attrPsviInfo.get("dt2");
            XSSimpleTypeDefinition memType2 = attrPsvi2.getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType2).getName(), "MYDATE2");                       
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testPsvi5() {
		String xmlfile = fDataDir+"/psvi/test3.xml";
		String schemapath = fDataDir+"/psvi/test4.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
            PSVIProvider psviProvider = (PSVIProvider) v;
            PSVIHandler psviHandler = new PSVIHandler(psviProvider);
		    v.setErrorHandler(this);
            v.validate(new SAXSource(new InputSource(xmlfile)), new SAXResult(psviHandler));
            assertNull(fErrSysId);
            assertNull(fFatErrSysId);
            // check memberType PSVI property
            Map attrPsviInfo = psviHandler.getAttributePsviInfo();
            AttributePSVI attrPsvi1 = (AttributePSVI) attrPsviInfo.get("dt1");
            XSSimpleTypeDefinition memType1 = attrPsvi1.getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType1).getName(), "MYDATE2");
            AttributePSVI attrPsvi2 = (AttributePSVI) attrPsviInfo.get("dt2");
            XSSimpleTypeDefinition memType2 = attrPsvi2.getMemberTypeDefinition();
            assertEquals(((XSSimpleTypeDecl) memType2).getName(), "MYDATE2");                       
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}
	
	public void testPsvi6() {
		String xmlfile = fDataDir+"/attributes/test1_1.xml";
		String schemapath = fDataDir+"/attributes/test1.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            assertTrue(getInheritedAttributeList(resultDoc, "BookStore").getLength() == 0);
            assertTrue(getInheritedAttributeList(resultDoc, "Book").getLength() == 1);
           	assertEquals("storename", getInhrAttributeName(resultDoc, "Book", 0));
           	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "Book", 0));
           	assertEquals("storename", getInhrAttributeName(resultDoc, "Title", 0));
           	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "Title", 0)); 
           	assertEquals("paperback", getInhrAttributeName(resultDoc, "Title", 1));
           	assertEquals("yes", getInhrAttrValue(resultDoc, "Title", 1));
           	assertEquals("storename", getInhrAttributeName(resultDoc, "Author", 0));
           	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "Author", 0));
           	assertEquals("storename", getInhrAttributeName(resultDoc, "Date", 0));
           	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "Date", 0));
           	assertEquals("storename", getInhrAttributeName(resultDoc, "ISBN", 0));
           	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "ISBN", 0));
           	assertEquals("paperback", getInhrAttributeName(resultDoc, "ISBN", 1));
           	assertEquals("yes", getInhrAttrValue(resultDoc, "ISBN", 1));
        	assertEquals("storename", getInhrAttributeName(resultDoc, "Publisher", 0));
        	assertEquals("Barnes and Noble", getInhrAttrValue(resultDoc, "Publisher", 0));
        	assertEquals("paperback", getInhrAttributeName(resultDoc, "Publisher", 1));
        	assertEquals("yes", getInhrAttrValue(resultDoc, "Publisher", 1));
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi7() {
		String xmlfile = fDataDir+"/psvi/test5.xml";
		String schemapath = fDataDir+"/psvi/test5.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            ObjectList failedAssertions = getFailedAssertions(resultDoc, "x");
            assertTrue(failedAssertions.getLength() == 1);
            XSAssert failedAssert = (XSAssert) failedAssertions.item(0);            
            assertEquals("#AnonType_x", XSTypeHelper.getSchemaTypeName(failedAssert.getTypeDefinition()));
            assertEquals("a = 'hello'", failedAssert.getTest().getXPathStr());
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi8() {
		String xmlfile = fDataDir+"/psvi/test6.xml";
		String schemapath = fDataDir+"/psvi/test6.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            ObjectList failedAssertions = getFailedAssertions(resultDoc, "x");
            assertTrue(failedAssertions.getLength() == 1);
            XSAssert failedAssert = (XSAssert) failedAssertions.item(0);
            assertEquals("MY_INT", XSTypeHelper.getSchemaTypeName(failedAssert.getTypeDefinition()));
            assertEquals("$value mod 2 = 0", failedAssert.getTest().getXPathStr());
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi9() {
		String xmlfile = fDataDir+"/psvi/test7.xml";
		String schemapath = fDataDir+"/psvi/test7.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            ObjectList failedAssertions = getFailedAssertions(resultDoc, "x");
            assertTrue(failedAssertions.getLength() == 2);
            XSAssert failedAssert1 = (XSAssert) failedAssertions.item(0);
            assertEquals("MY_INT", XSTypeHelper.getSchemaTypeName(failedAssert1.getTypeDefinition()));
            assertEquals("($value + 1) mod 2 = 0", failedAssert1.getTest().getXPathStr());
            XSAssert failedAssert2 = (XSAssert) failedAssertions.item(1);
            assertEquals("MY_INT", XSTypeHelper.getSchemaTypeName(failedAssert2.getTypeDefinition()));
            assertEquals("($value + 3) mod 2 = 0", failedAssert2.getTest().getXPathStr());
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi10() {
		String xmlfile = fDataDir+"/psvi/test8.xml";
		String schemapath = fDataDir+"/psvi/test8.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            ObjectList failedAssertions = getFailedAssertions(resultDoc, "x");
            assertTrue(failedAssertions.getLength() == 1);
            XSAssert failedAssert = (XSAssert) failedAssertions.item(0);
            assertEquals("X_TYPE", XSTypeHelper.getSchemaTypeName(failedAssert.getTypeDefinition()));
            assertEquals("a", failedAssert.getTest().getXPathStr());
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi11() {
		String xmlfile = fDataDir+"/psvi/test9_1.xml";
		String schemapath = fDataDir+"/psvi/test9.xsd";	
		try {
			fSchemaFactory.setFeature(CTA_FULL_XPATH, false);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            XSTypeAlternative typeAlternative = getTypeAlternative(resultDoc, "x");
            assertEquals("@a = 1", typeAlternative.getTestStr());
            assertEquals("A_TYPE", XSTypeHelper.getSchemaTypeName(typeAlternative.getTypeDefinition()));
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi12() {
		String xmlfile = fDataDir+"/psvi/test9_2.xml";
		String schemapath = fDataDir+"/psvi/test9.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            XSTypeAlternative typeAlternative = getTypeAlternative(resultDoc, "x");
            assertNull(typeAlternative.getTestStr());
            assertEquals("anyType", XSTypeHelper.getSchemaTypeName(typeAlternative.getTypeDefinition()));
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi13() {
		String xmlfile = fDataDir+"/psvi/test9_1.xml";
		String schemapath = fDataDir+"/psvi/test10.xsd";	
		try {
			fSchemaFactory.setFeature(CTA_FULL_XPATH, false);
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            XSTypeAlternative typeAlternative = getTypeAlternative(resultDoc, "x");
            assertEquals("@a = 1", typeAlternative.getTestStr());
            assertEquals("A_TYPE", XSTypeHelper.getSchemaTypeName(typeAlternative.getTypeDefinition()));
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
	
	public void testPsvi14() {
		String xmlfile = fDataDir+"/psvi/test9_2.xml";
		String schemapath = fDataDir+"/psvi/test10.xsd";	
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
		    PSVIDocumentImpl resultDoc = new PSVIDocumentImpl();
            v.validate(new DOMSource(getDomDocument(xmlfile)), new DOMResult(resultDoc));
            XSTypeAlternative typeAlternative = getTypeAlternative(resultDoc, "x");
            assertNull(typeAlternative.getTestStr());
            assertEquals("error", XSTypeHelper.getSchemaTypeName(typeAlternative.getTypeDefinition()));
		} catch(Exception ex) {		   
		   assertTrue(false);
		   ex.printStackTrace();
		}
	}
}
