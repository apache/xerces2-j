/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2003-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 * 
 */


package org.apache.xerces.impl.xs.models;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.util.SecurityManager ;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.Constants;

/**
 *
 * @author  Neeraj Bajaj
 *
 */
public class CMNodeFactory {
    

    /** Property identifier: error reporter. */
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    
    /** property identifier: security manager. */
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;

    private static final boolean DEBUG = false ;
    
    //
    private static final int MULTIPLICITY = 1 ;

    //count of number of nodes created
    private int nodeCount = 0;
    
    //No. of nodes allowed.
    private int maxNodeLimit ;

    
    /**
     * Error reporter. This property identifier is:
     * http://apache.org/xml/properties/internal/error-reporter
     */
    private XMLErrorReporter fErrorReporter;

    // stores defaults for different security holes (maxOccurLimit in current context) if it has
    // been set on the configuration.
    private SecurityManager fSecurityManager = null;
    
    /** default constructor */
    public CMNodeFactory() {
    }
    
    public void reset(XMLComponentManager componentManager){
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        try {
            fSecurityManager = (SecurityManager)componentManager.getProperty(SECURITY_MANAGER);
            //we are setting the limit of number of nodes to 3times the maxOccur value..
            if(fSecurityManager != null){
                maxNodeLimit = fSecurityManager.getMaxOccurNodeLimit() * MULTIPLICITY ;
            }
        }
        catch (XMLConfigurationException e) {
            fSecurityManager = null;
        }
        
    }//reset()
    
    public CMNode getCMLeafNode(int type, Object leaf, int id, int position) {
        nodeCountCheck() ;
        return new XSCMLeaf(type, leaf, id, position) ;
    }
    
    public CMNode getCMUniOpNode(int type, CMNode childNode) {
        nodeCountCheck();
        return new XSCMUniOp(type, childNode) ;
    }
    
    public CMNode getCMBinOpNode(int type, CMNode leftNode, CMNode rightNode) {
        nodeCountCheck() ;
        return new XSCMBinOp(type, leftNode, rightNode) ;
    }
    
    public void nodeCountCheck(){
        if( fSecurityManager != null && nodeCount++ > maxNodeLimit){
            if(DEBUG){
                System.out.println("nodeCount = " + nodeCount ) ;
                System.out.println("nodeLimit = " + maxNodeLimit ) ;
            }
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "maxOccurLimit", new Object[]{ new Integer(maxNodeLimit) }, XMLErrorReporter.SEVERITY_FATAL_ERROR);
            // similarly to entity manager behaviour, take into accont
            // behaviour if continue-after-fatal-error is set.
            nodeCount = 0;
        }
        
    }//nodeCountCheck()

    //reset the node count
    public void resetNodeCount(){
        nodeCount = 0 ;
    }
        /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {

        // Xerces properties
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
        	final int prefixLength = Constants.XERCES_PROPERTY_PREFIX.length();
        	
            if (propertyId.regionMatches(prefixLength, Constants.SECURITY_MANAGER_PROPERTY,
                0, Constants.SECURITY_MANAGER_PROPERTY.length())) {
                fSecurityManager = (SecurityManager)value;                
                maxNodeLimit = (fSecurityManager != null) ? fSecurityManager.getMaxOccurNodeLimit() * MULTIPLICITY : 0 ;
                return;
            }
            if (propertyId.regionMatches(prefixLength, Constants.ERROR_REPORTER_PROPERTY,
                0, Constants.ERROR_REPORTER_PROPERTY.length())) {
                fErrorReporter = (XMLErrorReporter)value;
                return;
            }
        }

    } // setProperty(String,Object)

}//CMNodeFactory()
