/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  
 * All rights reserved.
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
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.util;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;

import org.apache.xerces.dom3.DOMError;
import org.apache.xerces.dom3.DOMLocator;
import org.apache.xerces.dom3.DOMErrorHandler;

import org.apache.xerces.dom.DOMErrorImpl;

import java.io.PrintWriter;

/**
 * This class handles DOM errors .
 *
 * @see DOMErrorHandler
 *
 * @author Gopal Sharma, SUN Microsystems Inc.
 */

// REVISIT: current implementations wraps error several times:
//          XMLErrorReport.reportError creates XMLParserException (by wrapping all info)
//          and goes via switch to send errors.
//          DOMErrorHandlerWrapper catches calls, copies info from XMLParserException and
//          sends one call back to the application
//          I think we can avoid this indirection if we modify XMLErrorReporter. --el

public class DOMErrorHandlerWrapper
    implements XMLErrorHandler, DOMErrorHandler {


  // It keeps the reference of DOMErrorHandler of application
     protected DOMErrorHandler fDomErrorHandler;
     
  // Error Status
     boolean eStatus = true ;
     
  // Print writer
     protected PrintWriter fOut;

 	
  //
  // Constructors
  //
   
  // Default constructor /
  
   public DOMErrorHandlerWrapper() {
        fOut = new PrintWriter(System.err);
   }

   
   public DOMErrorHandlerWrapper(DOMErrorHandler domErrorHandler) {
	fDomErrorHandler = domErrorHandler;		
   } // DOMErrorHandlerWrapper(DOMErrorHandler domErrorHandler)

    
   //
   // Public methods
   //

   /** Sets the DOM error handler. */
   public void setErrorHandler(DOMErrorHandler errorHandler) {
       fDomErrorHandler = errorHandler;
   } // setErrorHandler(ErrorHandler)
    

   public DOMErrorHandler getErrorHandler(){
	return fDomErrorHandler;	
   } //getErrorHandler()
	
   //
   // XMLErrorHandler methods
   //

   /**
    * Reports a warning. Warnings are non-fatal and can be safely ignored
    * by most applications.
    *
    * @param domain    The domain of the warning. The domain can be any
    *                  string but is suggested to be a valid URI. The
    *                  domain can be used to conveniently specify a web
    *                  site location of the relevent specification or
    *                  document pertaining to this warning.
    * @param key       The warning key. This key can be any string and
    *                  is implementation dependent.
    * @param exception Exception.
    *
    * @throws XNIException Thrown to signal that the parser should stop
    *                      parsing the document.
    */

   public void warning(String domain, String key, 
			XMLParseException exception) throws XNIException {
	DOMError error = new DOMErrorImpl(DOMError.SEVERITY_WARNING, exception);
	fDomErrorHandler.handleError(error); 
   } // warning(String,String,XMLParseException)

   /**
    * Reports an error. Errors are non-fatal and usually signify that the
    * document is invalid with respect to its grammar(s).
    *
    * @param domain    The domain of the error. The domain can be any
    *                  string but is suggested to be a valid URI. The
    *                  domain can be used to conveniently specify a web
    *                  site location of the relevent specification or
    *                  document pertaining to this error.
    * @param key       The error key. This key can be any string and
    *                  is implementation dependent.
    * @param exception Exception.
    *
    * @throws XNIException Thrown to signal that the parser should stop
    *                      parsing the document.
    */
   public void error(String domain, String key, 
			XMLParseException exception) throws XNIException {
	DOMError error = new DOMErrorImpl(DOMError.SEVERITY_ERROR, exception);
	fDomErrorHandler.handleError(error);                        
   } // error(String,String,XMLParseException)

   /**
    * Report a fatal error. Fatal errors usually occur when the document
    * is not well-formed and signifies that the parser cannot continue
    * normal operation.
    * <p>
    * <strong>Note:</strong> The error handler should <em>always</em>
    * throw an <code>XNIException</code> from this method. This exception
    * can either be the same exception that is passed as a parameter to
    * the method or a new XNI exception object. If the registered error
    * handler fails to throw an exception, the continuing operation of
    * the parser is undetermined.
    *
    * @param domain    The domain of the fatal error. The domain can be 
    *                  any string but is suggested to be a valid URI. The
    *                  domain can be used to conveniently specify a web
    *                  site location of the relevent specification or
    *                  document pertaining to this fatal error.
    * @param key       The fatal error key. This key can be any string 
    *                  and is implementation dependent.
    * @param exception Exception.
    *
    * @throws XNIException Thrown to signal that the parser should stop
    *                      parsing the document.
    */
   public void fatalError(String domain, String key, 
			XMLParseException exception) throws XNIException {
	DOMError error = new DOMErrorImpl(DOMError.SEVERITY_FATAL_ERROR, exception);
	fDomErrorHandler.handleError(error);                             
   } // fatalError(String,String,XMLParseException)
    
    
   public boolean handleError(DOMError error) {
       printError(error);
       return eStatus;
   }
    
   /** Prints the error message. */
    
   private void printError(DOMError error) {
	int severity = -1;
	fOut.print("[");
	if ( severity == DOMError.SEVERITY_WARNING){
		fOut.print("Warning");
	}else if ( severity == DOMError.SEVERITY_ERROR){
		fOut.print("Error");
	}else{
		fOut.print("Fatal Error");
		eStatus = false ; //REVISIT: Abort processing if fatal error, do we need to??
	}
        fOut.print("] ");
	fOut.print(": ");
	fOut.print(error.getMessage());
	fOut.print(':');
        fOut.print(error.getException());		
	DOMLocator locator = error.getLocation();
	if (locator != null){
		fOut.print(":L ");
	        fOut.print(locator.getLineNumber());
	        fOut.print(":C ");
	        fOut.print(locator.getColumnNumber());
		fOut.print(": ");
	        fOut.print(locator.getOffset());
	        fOut.print(": ");
	        fOut.print(locator.getErrorNode().getNodeName());
		String systemId = locator.getUri();
		if (systemId != null) {
		    int index = systemId.lastIndexOf('/');
			if (index != -1)
				systemId = systemId.substring(index + 1);
		    fOut.print(": ");
	            fOut.print(systemId);
		}
			
	}
	fOut.println();
	fOut.flush();

    } // printError(DOMError)
    
} // class DOMErrorHandlerWrapper
