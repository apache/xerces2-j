/******************************************************************************
 * Copyright (C) 1999-2000, Pierpaolo Fumagalli <mailto:pier@betaversion.org> *
 *                                                                            *
 * Redistribution and use in source  and/or in binary forms,  with or without *
 * modification are hereby permitted provided that the above copyright notice *
 * the following disclaimers, and this paragraph are not altered.             *
 *                                                                            *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING,  BUT NOT LIMITED TO,  THE IMPLIED WARRANTIES OF MERCHANTABILITY *
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE *
 * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, *
 * OR CONSEQUENTIAL DAMAGES  (INCLUDING,  BUT NOT LIMITED TO,  PROCUREMENT OF *
 * SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS;  OR BUSINESS *
 * INTERRUPTION)  HOWEVER CAUSED AND ON  ANY THEORY OF LIABILITY,  WHETHER IN *
 * CONTRACT,  STRICT LIABILITY,  OR TORT  (INCLUDING NEGLIGENCE OR OTHERWISE) *
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE *
 * POSSIBILITY OF SUCH DAMAGE.                                                *
 *                                                                            *
 * ATTENTION:                                                                 *
 *     THIS  IMPLEMENTATION  OF THE  "JAVAX.XML.PARSER"  CLASSES IS  PROVIDED *
 *     FOR  EXPERIMENTAL PURPOSES  ONLY  AND IS  NOT THE  OFFICIAL  REFERENCE *
 *     IMPLEMENTATION OF THE JAVA SPECIFICATION REQUEST 5 FOUND AT:           *
 *     <http://java.sun.com/aboutJava/communityprocess/jsr/jsr_005_xml.html>  *
 *                                                                            *
 *     THIS IMPLEMENTATION  IS CONFORMANT  TO THE  "JAVA API FOR XML PARSING" *
 *     SPECIFICATION  VERSION 1.0  PUBLIC RELEASE 1  BY JAMES DUNCAN DAVIDSON *
 *     ET AL. PUBLISHED BY SUN MICROSYSTEMS ON FEB. 18, 2000 AND FOUND AT:    *
 *     <http://java.sun.com/xml>                                              *
 ******************************************************************************/
package javax.xml.parsers;

/**
 * The <code>DocumentBuilderFactory</code> defines a factory API that enables
 * applications to configure and obtain a parser to parse XML documents into
 * a DOM Document tree.
 * <br>
 * <br>
 * <b>ATTENTION:</b> THIS IMPLEMENTATION OF THE "JAVAX.XML.PARSER" CLASSES
 *   IS PROVIDED FOR EXPERIMENTAL PURPOSES ONLY AND IS NOT THE OFFICIAL
 *   REFERENCE IMPLEMENTATION OF THE JAVA SPECIFICATION REQUEST 5 FOUND AT
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
 * @author Copyright &copy; 1999-2000, Pierpaolo Fumagalli
 *         <a href="mailto:pier@betaversion.org">pier@betaversion.org</a>
 * @version 1.0 CVS $Revision$ $Date$
 */
public abstract class DocumentBuilderFactory {

    /** Wether the DocumentBuilder to be generated must support namespaces. */
    private boolean namespaces=false;
    /** Wether the DocumentBuilder to be generated must support validataion. */
    private boolean validation=false;
    /** The system property to check for DocumentBuilderFactory class name. */
    private static String property="javax.xml.parsers.DocumentBuilderFactory";
    /** The default DocumentBuilderFactory implementation class name. */
    private static String factory=
                          "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl";

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
    protected DocumentBuilderFactory() {
        super();
    }

    /**
     *
     * Returns a new instance of a <code>DocumentBuilderFactory</code>.
     * <br>
     * The implementation of the <code>DocumentBuilderFactory</code>
     * returned depends on the setting of the
     * <code>javax.xml.parsers.DocumentBuilderFactory</code> property or,
     * if the property is not set, a platform specific default.
     *
     * @exception FactoryConfigurationError If the class implementing the
     *                factory cannot be found or instantiated.
     *                An <code>Error</code> is thrown instead of an exception
     *                because the application is not expected to handle or
     *                recover from such events.
     */
    public static DocumentBuilderFactory newInstance() {

        // Evaluate wether we have to use or Class.forName() for JDK 1.1 or
        // Thread.currentThread().getContextClassLoader().loadClass() for
        // the new JDK 1.2
        boolean newPlatform=true;
        try {
            new ThreadLocal();
        } catch (NoClassDefFoundError t) {
            newPlatform=false;
        }

        // Retrieve the javax.xml.parsers.DocumentBuilderFactory system property
        String n=System.getProperty(property, factory);

        try {
            // Attempt to load the factory class.
            Class c=null;
            if (!newPlatform) c=Class.forName(n);
            else c=Thread.currentThread().getContextClassLoader().loadClass(n);

            // Attempt to instantiate and return the factory class
            return (DocumentBuilderFactory)c.newInstance();

        } catch (ClassNotFoundException e) {
            // The factory class was not found
            throw new FactoryConfigurationError("Cannot load class "+
                "DocumentBuilderFactory class \""+n+"\"");

        } catch (InstantiationException e) {
            // The factory class wasn't instantiated
            throw new FactoryConfigurationError("Cannot instantiate the "+
                "specified DocumentBuilderFactory class \""+n+"\"");

        } catch (IllegalAccessException e) {
            // The factory class couldn't have been accessed
            throw new FactoryConfigurationError("Cannot access the specified "+
                "DocumentBuilderFactory class \""+n+"\"");

        } catch (ClassCastException e) {
            // The factory class was not a DocumentBuilderFactory
            throw new FactoryConfigurationError("The specified class \""+n+
                "\" is not instance of \""+
                "javax.xml.parsers.DocumentBuilderFactory\"");
        }
    }

    /**
     * Returns a new configured instance of type <code>DocumentBuilder</code>.
     *
     * @exception ParserConfigurationException If the
     *                <code>DocumentBuilder</code> instance cannot be created
     *                with the requested configuration.
     */
    public abstract DocumentBuilder newDocumentBuilder()
    throws ParserConfigurationException;

    /**
     * Configuration method that specifies whether the parsers created by this
     * factory are required to provide XML namespace support or not.
     * <br>
     * <b>NOTE:</b> if a parser cannot be created by this factory that
     *     satisfies the requested namespace awareness value, a
     *     <code>ParserConfigurationException</code> will be thrown when the
     *     program attempts to aquire the parser calling the
     *     <code>newDocumentBuilder()</code> method.
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
     *     <code>newDocumentBuilder()</code> method.
     */
    public void setValidating(boolean validating) {
        this.validation=validating;
    }

    /**
     * Indicates if this <code>DocumentBuilderFactory</code> is configured to
     * produce parsers that are namespace aware or not.
     */
    public boolean isNamespaceAware() {
        return(this.namespaces);
    }

    /**
     * Indicates if this <code>DocumentBuilderFactory</code> is configured to
     * produce parsers that validate XML documents as they are parsed.
     */
    public boolean isValidating() {
        return(this.validation);
    }
}
