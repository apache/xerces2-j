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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * Instances of <code>DocumentBuilder</code> provide a mechansim for
 * parsing XML documents into a DOM document tree represented by an
 * <code>org.w3c.dom.Document</code> object.
 * <br>
 * A <code>DocumentBuilder</code> instance is obtained from a
 * <code>DocumentBuilderFactory</code> by invoking its
 * <code>newDocumentBuilder()</code> method.
 * <br>
 * <b>NOTE:</b> <code>DocumentBuilder</code> uses several classes from the
 *     SAX API. This does not require that the implementor of the
 *     underlying DOM implementation use a SAX parser to parse XML content
 *     into a <code>org.w3c.dom.Document</code>. It merely requires that
 *     the implementation communicate with the application using these
 *     existing APIs.
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
public abstract class DocumentBuilder {

    /**
     * Implementors of this abstract class are not required to provide a
     * public no-argument constructor, since instantiation is taken care
     * by <code>DocumentBuilderFactory</code> implementations.
     */
    protected DocumentBuilder() {
        super();
    }

    /**
     * Parses the contents of the given <code>InputStream</code> and returns
     * a <code>Document</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur reading the given
     *                <code>InputStream</code>.
     * @exception  IllegalArgumentException If the given
     *                <code>InputStream</code> is <b>null</b>.
     */
    public Document parse(InputStream stream)
    throws SAXException, IOException, IllegalArgumentException {
        if (stream==null) throw new IllegalArgumentException();
        return(this.parse(new InputSource(stream)));
    }

    /**
     * Parses the content of the given URI and returns a <code>Document</code>
     * object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur while reading content
     *                located by the given URI.
     * @exception IllegalArgumentException If the given URI is <b>null</b>.
     */
    public Document parse(String uri)
    throws SAXException, IOException, IllegalArgumentException {
        if (uri==null) throw new IllegalArgumentException();
        return(this.parse(new InputSource(uri)));
    }

    /**
     * Parses the content of the given <code>File</code> and returns a
     * <code>Document</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO errors occur while reading content
     *                from the given <code>File</code>.
     * @exception IllegalArgumentException if the given <code>File</code> is
     *                <b>null</b>.
     */
    public Document parse(File file)
    throws SAXException, IOException, IllegalArgumentException {
        if (file==null) throw new IllegalArgumentException();
        return(this.parse(new InputSource(file.getName())));
    }

    /**
     * Parses the content of the given <code>InputSource</code> and returns
     * a <code>Document</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO Errors occur while reading content
     *                from the given <code>InputSource</code>.
     * @exception IllegalArgumentException if the given
     *                <code>InputSource</code> is <b>null</b>.
     */
    public abstract Document parse(InputSource source)
    throws SAXException, IOException, IllegalArgumentException;

    /**
     * Creates an new <code>Document</code> instance from the underlying DOM
     * implementation.
     */
    public abstract Document newDocument();

    /**
     * Returns whether or not this parser supports XML namespaces.
     */
    public abstract boolean isNamespaceAware();

    /**
     * Returns whether or not this parser supports validating XML content.
     */
    public abstract boolean isValidating();

    /**
     * Specifies the <code>EntityResolver</code> to be used by this
     * <code>DocumentBuilder</code>.
     * <br>
     * Setting the <code>EntityResolver</code> to <b>null</b>, or not
     * calling this method, will cause the underlying implementation to
     * use its own default implementation and behavior.
     */
    public abstract void setEntityResolver(EntityResolver er);

    /**
     * Specifies the <code>ErrorHandler</code> to be used by this
     * <code>DocumentBuilder</code>.
     *
     * Setting the <code>ErrorHandler</code> to <b>null</b>, or not
     * calling this method, will cause the underlying implementation to
     * use its own default implementation and behavior.
     */
    public abstract void setErrorHandler(ErrorHandler eh);
}

