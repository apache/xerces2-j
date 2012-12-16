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
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeAlternative;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XercesAbstractTestCase extends TestCase implements ErrorHandler {
	
	protected SchemaFactory fSchemaFactory = null;
	protected String fDataDir = null;
	protected String fErrSysId = null;
	protected String fFatErrSysId = null;
	protected String fWarningSysId = null;
	protected String fErrorMessage = null;
	// to maintain a collection of errors and/or warnings for ONE test case execution
	protected List failureList = null;
	protected List warningList = null; 
	
	protected boolean checkOnlyWarnings = false; 
	
	protected static final String DEFAULT_SCHEMA_LANGUAGE = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	protected static final String SCHEMA_11_LANGUAGE = "http://www.w3.org/2001/XMLSchema/v1.1";
	protected static final String SCHEMA_11_FACTORY = "org.apache.xerces.jaxp.validation.XMLSchema11Factory";
	protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
	protected static final String CTA_FULL_XPATH = Constants.XERCES_FEATURE_PREFIX + Constants.CTA_FULL_XPATH_CHECKING_FEATURE;
	
	public XercesAbstractTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		System.setProperty("javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/v1.1", SCHEMA_11_FACTORY);
		fDataDir = System.getProperty("org.apache.xerces.tests.dataDir");
		fSchemaFactory = SchemaFactory.newInstance(SCHEMA_11_LANGUAGE);
		fSchemaFactory.setFeature(CTA_FULL_XPATH, true);
		failureList = new ArrayList();
		warningList = new ArrayList(); 
	}

	protected void tearDown() throws Exception {
		fErrSysId = null;
		fFatErrSysId = null;
		failureList = null;
		warningList = null;
		checkOnlyWarnings = false;
		fSchemaFactory.setFeature(CTA_FULL_XPATH, false);
	}
	
	public void error(SAXParseException exception) throws SAXException {
		fErrSysId = exception.getSystemId();
		fErrorMessage = exception.getMessage();
		failureList.add(new Error(fErrSysId, fErrorMessage));
    }

    public void fatalError(SAXParseException exception) throws SAXException {
    	fFatErrSysId = exception.getSystemId();
    	fErrorMessage = exception.getMessage();
    	failureList.add(new FatalError(fErrSysId, fErrorMessage));
    }

	public void warning(SAXParseException exception) throws SAXException {
		fWarningSysId = exception.getSystemId();
		fErrorMessage = exception.getMessage();
		warningList.add(new Warning(fWarningSysId, fErrorMessage));
	}
	
	
	/*
	 * Are error messages generated during the validation episode, as per specified in the test case.
	 */
	public boolean areErrorMessagesConsistent(List expectedMsgList) {		
		boolean isErrorMesgsOk = true;		
		for (int mesgIdx = 0; mesgIdx < expectedMsgList.size(); mesgIdx++) {
			FailureMesgFragments mesgFragments = (FailureMesgFragments) expectedMsgList.get(mesgIdx);
			if (!areMesgFragmentsOk(mesgFragments)) {
				isErrorMesgsOk = false;
				break;
			}
		}		
		return isErrorMesgsOk;		
	} // areErrorMessagesConsistent
	
	
	/*
	 * Checks fragments of one error/failure message.
	 */
	private boolean areMesgFragmentsOk(FailureMesgFragments mesgFragments) {
		
		boolean areMesgFragsOk = false;
		
		List mesgFragmentItems = mesgFragments.getMessageFragments();
		Iterator iter = null;
		if (checkOnlyWarnings) {
			iter = warningList.iterator(); 
		}
		else {
			iter = failureList.iterator(); 
		}
        for ( ; iter.hasNext(); ) {
        	Object failureInstance = iter.next();
        	String failureMesg = "";
        	if (failureInstance instanceof Error) {
        		failureMesg = ((Error) failureInstance).getFailureMessage(); 
        	}
        	else if (failureInstance instanceof FatalError) {
        		failureMesg = ((FatalError) failureInstance).getFailureMessage();
        	}
        	else if (failureInstance instanceof Warning) {
        		failureMesg = ((Warning) failureInstance).getFailureMessage();
        	}
        	int matchCount = 0;
        	for (Iterator mesg_iter = mesgFragmentItems.iterator(); mesg_iter.hasNext(); ) {
        		String mesgFrag = (String) mesg_iter.next();
        		if (failureMesg.indexOf(mesgFrag) != -1) {
        			matchCount++;
        		}
        	}
        	if (matchCount == mesgFragmentItems.size()) {
        		areMesgFragsOk = true;
        		break;
        	}
        }


		return areMesgFragsOk;
		
	} // areMesgFragmentsOk
	
	
	protected String getMemberTypePsviInfo(ElementPSVI elmPsviInfo) {		
		String memTypeStr = null;
		XSSimpleTypeDefinition memType = elmPsviInfo.getMemberTypeDefinition();
    	if (memType != null) {
    		memTypeStr = memType.getName();
    	}    	
    	return memTypeStr;    	
	} // getMemberTypePsviInfo
	
	
	class XercesFailure {
	   String systemId;
	   String failureMessage;
	   
	   public XercesFailure(String systemId, String failureMessage) {
		   this.systemId = systemId;
		   this.failureMessage = failureMessage;
	   }
	   
	   public String getFailureMessage() {
		  return failureMessage; 
	   }
	}
	
	class Error extends XercesFailure {		
		public Error(String systemId, String failureMessage) {
			super(systemId, failureMessage);
		}		
	}
	
	class FatalError extends XercesFailure {		
		public FatalError(String systemId, String failureMessage) {
			super(systemId, failureMessage);
		}		
	}
	
	class Warning extends XercesFailure {		
		public Warning(String systemId, String failureMessage) {
			super(systemId, failureMessage);
		}		
	}
	
	protected Document getDomDocument(String docUri) throws Exception {
		DocumentBuilderFactory dBf = DocumentBuilderFactory.newInstance();
		dBf.setNamespaceAware(true);
		DocumentBuilder dBuilder = dBf.newDocumentBuilder();
		return dBuilder.parse(docUri);
	}
	
	protected ObjectList getInheritedAttributeList(PSVIDocumentImpl document, String elemName) {
		PSVIElementNSImpl psviElement = (PSVIElementNSImpl)(document.getElementsByTagName(elemName)).item(0);
		return psviElement.getInheritedAttributes();
	}
	
	protected String getInhrAttributeName(PSVIDocumentImpl document, String elemName, int inhrAttrIdx) {
		return ((AttributePSVI)(getInheritedAttributeList(document, elemName)).item(inhrAttrIdx)).getAttributeDeclaration().getName();
	}
	
	protected String getInhrAttrValue(PSVIDocumentImpl resultDoc, String elemName, int inhrAttrIdx) {
		return ((AttributePSVI)(getInheritedAttributeList(resultDoc, elemName)).item(inhrAttrIdx)).getSchemaValue().getNormalizedValue();
	}
	
	protected ObjectList getFailedAssertions(PSVIDocumentImpl document, String elemName) {
		PSVIElementNSImpl psviElement = (PSVIElementNSImpl)(document.getElementsByTagName(elemName)).item(0);
		return psviElement.getFailedAssertions();
	}
	
	protected XSTypeAlternative getTypeAlternative(PSVIDocumentImpl document, String elemName) {
		PSVIElementNSImpl psviElement = (PSVIElementNSImpl)(document.getElementsByTagName(elemName)).item(0);
		return psviElement.getTypeAlternative();
	}
	
}
