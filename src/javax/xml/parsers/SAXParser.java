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
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;

/**
 * Implementation instances of the <code>SAXParser</code> abstract class
 * contain an implementation of the <code>org.xml.sax.Parser</code> interface
 * and enables content from a variety of sources to be parsed using the
 * contained parser.
 * <br>
 * Instances of <code>SAXParser</code> are obtained from a
 * <code>SAXParserFactory</code> by invoking its <code>newSAXParser()</code>
 * method.
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
public abstract class SAXParser {

    /**
     * Implementations should provide a protected constructor so that 
     * their factory implementation can instantiate instances of the 
     * implementation class.
     * <br>
     * Application programmers should not be able to directly construct 
     * implementation subclasses of this abstract subclass. The only way a 
     * application should be able to obtain a reference to a SAXParser 
     * implementation instance is by using the appropriate methods of the 
     * <code>SAXParserFactory</code>.
     */
    protected SAXParser() {
        super();
    }

    /**
     * Parses the contents of the given <code>InputStream</code> as an XML
     * document using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur reading the given
     *                <code>InputStream</code>.
     * @exception  IllegalArgumentException If the given
     *                <code>InputStream</code> is <b>null</b>.
     */
    public void parse(InputStream stream, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (stream==null) throw new IllegalArgumentException();
        this.parse(new InputSource(stream),base);
    }

    /**
     * Parses the content of the given URI as an XML document using the
     * specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException If any IO errors occur while reading content
     *                located by the given URI.
     * @exception IllegalArgumentException If the given URI is <b>null</b>.
     */
    public void parse(String uri, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (uri==null) throw new IllegalArgumentException();
        this.parse(new InputSource(uri),base);
    }

    /**
     * Parses the content of the given <code>File</code> as an XML document
     * using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO errors occur while reading content
     *                from the given <code>File</code>.
     * @exception IllegalArgumentException if the given <code>File</code> is
     *                <b>null</b>.
     */
    public void parse(File file, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (file==null) throw new IllegalArgumentException();
        this.parse(new InputSource(file.getName()),base);
    }

    /**
     * Parses the content of the given <code>InputSource</code> as an XML
     * document using the specified <code>HandlerBase</code> object.
     *
     * @exception SAXException If there is a problem parsing the given XML
     *                content.
     * @exception IOException if any IO Errors occur while reading content
     *                from the given <code>InputSource</code>.
     * @exception IllegalArgumentException if the given
     *                <code>InputSource</code> is <b>null</b>.
     */
    public void parse(InputSource source, HandlerBase base)
    throws SAXException, IOException, IllegalArgumentException {
        if (source==null) throw new IllegalArgumentException();

        // Get the SAX parser instance
        Parser p=this.getParser();

        // Set the various handler instances
        if (base!=null) {
            p.setDocumentHandler(base);
            p.setDTDHandler(base);
            p.setEntityResolver(base);
            p.setErrorHandler(base);
        }

        // Parse the specified source
        p.parse(source);
    }

    /**
     * Returns the underlying <code>Parser</code> object which is wrapped by
     * this <code>SAXParser</code> implementation.
     *
     * @exception SAXException If the initialization of the underlying parser
     *                fails. <b>NOTE:</b> This Exception is specified on page
     *                21 of the specification, but later on omissed in this
     *                method documentation on page 23. Wich one is correct?
     */
    public abstract Parser getParser()
    throws SAXException;

    /**
     * Returns whether or not this parser supports XML namespaces.
     */
    public abstract boolean isNamespaceAware();

    /**
     * Returns whether or not this parser supports validating XML content.
     */
    public abstract boolean isValidating();
}