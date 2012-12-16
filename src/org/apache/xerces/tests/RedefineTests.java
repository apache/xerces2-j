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
public class RedefineTests extends XercesAbstractTestCase {

	public RedefineTests(String name) {
		super(name);
	}
	
	public void testXSRedefine1() {
		String xmlfile = fDataDir+"/redefine/v1_1.xml";
		String schemapath = fDataDir+"/redefine/v1.xsd";		
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
	
	public void testXSRedefine2() {
		String xmlfile = fDataDir+"/redefine/v1_2.xml";
		String schemapath = fDataDir+"/redefine/v1.xsd";		
		try {
		    Schema s = fSchemaFactory.newSchema(new StreamSource(schemapath));
            Validator v = s.newValidator();
		    v.setErrorHandler(this);
            v.validate(new StreamSource(xmlfile));
            assertTrue(failureList.size() == 2);
            // test expected error messages
            List expectedMsgList = new ArrayList();
            FailureMesgFragments mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-maxInclusive-valid: Value '7' is not facet-valid with respect to maxInclusive '6' for type 'T1'");
            expectedMsgList.add(mesgFragments);
            mesgFragments = new FailureMesgFragments();
            mesgFragments.setMessageFragment("cvc-type.3.1.3: The value '7' of element 'a' is not valid");
            expectedMsgList.add(mesgFragments);
            assertTrue(areErrorMessagesConsistent(expectedMsgList));
		} catch(Exception ex) {
		   ex.printStackTrace();
		   assertTrue(false);
		}
	}

}
