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

package org.apache.xerces.impl.xs.alternative;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.impl.xpath.XPath20;
import org.apache.xerces.impl.xs.AbstractPsychoPathImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Representation of XML Schema 1.1 'type alternatives', "test" attribute.
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class Test extends AbstractPsychoPathImpl {

	/** The type alternative to which the test belongs */
    protected final XSTypeAlternativeImpl fTypeAlternative;

    /** XPath 2.0 expression. Xerces-J native XPath 2.0 subset. */
    protected XPath20 fXPath = null;
    
    /** XPath 2.0 expression. PsychoPath XPath 2.0 expression. */
    protected XPath fXPathPsychoPath = null;

    /** Constructs a "test" for type alternatives */
    public Test(XPath20 xpath, XSTypeAlternativeImpl typeAlternative) {
        fXPath = xpath;
        fTypeAlternative = typeAlternative;
    }
    
    /*
     * Constructs a "test" for type alternatives. An overloaded constructor,
     * for PsychoPath XPath processor.
     */
    public Test(XPath xpath, XSTypeAlternativeImpl typeAlternative) {
       fXPathPsychoPath = xpath;
       fTypeAlternative = typeAlternative;    
    }

    public XSTypeAlternativeImpl getTypeAlternative() {
        return fTypeAlternative;
    }
	
     /*
      * Returns the test XPath. Return the native XPath expression,
      * or PsychoPath, whichever is available. 
      */
    public Object getXPath() {
        Object xpath = null;
        
        if (fXPath != null) {
            xpath = fXPath;    
        } else if (fXPathPsychoPath != null) {
            xpath = fXPathPsychoPath;    
        }
        
        return xpath;
    }
    
    /** Evaluate the test expression with respect to the specified element and its attributes */
    public boolean evaluateTest(QName element, XMLAttributes attributes) {
        if (fXPath != null) {
            return fXPath.evaluateTest(element, attributes);
        } else if (fXPathPsychoPath != null) {
            return evaluateTestWithPsychoPath(element, attributes);  
        }
        else {
            return false;
        }
    }

    public String toString() {
        return fXPath.toString();
    }
    
    /*
     * Evaluate the XPath "test" expression on an XDM instance, containing the specified
     * element and its attributes. Using PsychoPath XPath 2.0 engine for the evaluation. 
     */
    private boolean evaluateTestWithPsychoPath(QName element, XMLAttributes attributes) {
       boolean result = false;
       
       try {
         // construct a DOM document (an XPath XDM instance), for XPath evaluation
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = dbf.newDocumentBuilder();
         Document document = docBuilder.newDocument();
       
         Element elem = document.createElementNS(element.uri, element.rawname);
         for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {
            elem.setAttributeNS(attributes.getURI(attrIndx),
                            attributes.getQName(attrIndx),
                            attributes.getValue(attrIndx));
         }
       
         document.appendChild(elem);
         
         initDynamicContext(null, document);       
         result = evaluatePsychoPathExpr(fXPathPsychoPath, elem);
       } catch(ParserConfigurationException ex) {
           result = false;  
       } catch (StaticError ex) {
           result = false; 
       } catch(DynamicError ex) {
           result = false;
       }
       
       return result;
       
    } //evaluateTestWithPsychoPath
    
}
