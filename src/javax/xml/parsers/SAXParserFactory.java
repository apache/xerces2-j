/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2000, Pierpaolo
 * Fumagalli <mailto:pier@betaversion.org>, http://www.apache.org.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package javax.xml.parsers;

import org.xml.sax.SAXException;

/**
 * The <code>SAXParserFactory</code> defines a factory API that enables
 * applications to configure and obtain a SAX based parser to parse XML
 * documents.
 * <br>
 * <br>
 * <b>ATTENTION:</b> THIS IMPLEMENTATION OF THE "JAVAX.XML.PARSER" CLASSES
 *   IS NOT THE OFFICIAL REFERENCE IMPLEMENTATION OF THE JAVA SPECIFICATION
 *   REQUEST 5 FOUND AT
 *   <a href="http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html">
 *   http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html
 *   </a><br>
 *   THIS IMPLEMENTATION IS CONFORMANT TO THE "JAVA API FOR XML PARSING"
 *   SPECIFICATION VERSION 1.0 PUBLIC RELEASE 1 BY JAMES DUNCAN DAVIDSON
 *   PUBLISHED BY SUN MICROSYSTEMS ON FEB. 18, 2000 AND FOUND AT
 *   <a href="http://java.sun.com/xml">http://java.sun.com/xml</a>
 * <br>
 * <br>
 * <b>THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author <a href="pier@betaversion.org">Pierpaolo Fumagalli</a>
 * @author Copyright &copy; 2000 The Apache Software Foundation.
 * @version 1.0 CVS $Revision$ $Date$
 */
public abstract class SAXParserFactory {

    /** Wether the SAXParser to be generated must support namespaces. */
    private boolean namespaces=false;
    /** Wether the SAXParser to be generated must support validataion. */
    private boolean validation=false;
    /** The system property to check for the SAXParserFactory class name. */
    private static String property="javax.xml.parsers.SAXParserFactory";
    /** The default SAXParserFactory implementation class name. */
    private static String factory="org.apache.xerces.jaxp.SAXParserFactoryImpl";

    /**
     * Implementors of this abstract class <b>must</b> provide their own
     * public no-argument constructor in order for the static
     * <code>newInstance()</code> method to work correctly.
     * <br>
     * Application programmers should be able to instantiate an implementation
     * of this abstract class directly if they want to use a specfic
     * implementation of this API without using the static newInstance method
     * to obtain the configured or platform default implementation.
     */
    protected SAXParserFactory() {
        super();
    }

    /**
     * Returns a new instance of a <code>SAXParserFactory</code>.
     * <br>
     * The implementation of the SAX-ParserFactory returned depends on the
     * setting of the <code>javax.xml.parsers.SAXParserFactory</code>
     * system property or, if the property is not set, a platform specific
     * default.
     *
     * @exception FactoryConfigurationError If the class implementing the
     *                factory cannot be found or instantiated.
     *                An <code>Error</code> is thrown instead of an exception
     *                because the application is not expected to handle or
     *                recover from such events.
     */
    public static SAXParserFactory newInstance() {

        // Retrieve the javax.xml.parsers.SAXParserFactory system property
        String n=factory;
        try {
            n=System.getProperty(property, factory);
        } catch (SecurityException e) {
        	// In applets System.getProperty throws a SecurityException.
        	// Thanks to Aaron Buchanan <abuchanan@inovacorp.com> and to
        	// James Duncan Davidson <james.davidson@eng.sun.com> for this
        	n=factory;
        }

        try {
            // Attempt to load, instantiate and return the factory class
            return (SAXParserFactory)Class.forName(n).newInstance();

        } catch (ClassNotFoundException e) {
            // The factory class was not found
            throw new FactoryConfigurationError("Cannot load class "+
                "SAXParserFactory class \""+n+"\"");

        } catch (InstantiationException e) {
            // The factory class wasn't instantiated
            throw new FactoryConfigurationError("Cannot instantiate the "+
                "specified SAXParserFactory class \""+n+"\"");

        } catch (IllegalAccessException e) {
            // The factory class couldn't have been accessed
            throw new FactoryConfigurationError("Cannot access the specified "+
                "SAXParserFactory class \""+n+"\"");

        } catch (ClassCastException e) {
            // The factory class was not a SAXParserFactory
            throw new FactoryConfigurationError("The specified class \""+n+
                "\" is not instance of \"javax.xml.parsers.SAXParserFactory\"");
        }
    }

    /**
     * Returns a new configured instance of type <code>SAXParser</code>.
     *
     * @exception ParserConfigurationException If the <code>SAXParser</code>
     *                instance cannot be created with the requested
     *                configuration.
     * @exception SAXException If the initialization of the underlying parser
     *                fails.
     */
    public abstract SAXParser newSAXParser()
    throws ParserConfigurationException, SAXException;

    /**
     * Configuration method that specifies whether the parsers created by this
     * factory are required to provide XML namespace support or not.
     * <br>
     * <b>NOTE:</b> if a parser cannot be created by this factory that
     *     satisfies the requested namespace awareness value, a
     *     <code>ParserConfigurationException</code> will be thrown when the
     *     program attempts to aquire the parser calling the
     *     <code>newSaxParser()</code> method.
     */
    public void setNamespaceAware(boolean aware) {
        this.namespaces=aware;
    }

    /**
     * Configuration method whether specifies if the parsers created by this
     * factory are required to validate the XML documents that they parse.
     * <br>
     * <b>NOTE:</b> if a parser cannot be created by this factory that
     *     satisfies the requested validation capacity, a
     *     <code>ParserConfigurationException</code> will be thrown when
     *     the application attempts to aquire the parser via the
     *     <code>newSaxParser()</code> method.
     */
    public void setValidating(boolean validating) {
        this.validation=validating;
    }

    /**
     * Indicates if this <code>SAXParserFactory</code> is configured to
     * produce parsers that are namespace aware or not.
     */
    public boolean isNamespaceAware() {
        return(this.namespaces);
    }

    /**
     * Indicates if this <code>SAXParserFactory</code> is configured to
     * produce parsers that validate XML documents as they are parsed.
     */
    public boolean isValidating() {
        return(this.validation);
    }
}
