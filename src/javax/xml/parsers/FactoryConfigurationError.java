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
 * This error is thrown if there is a configuration problem when creating
 * new factory instances.
 * <br>
 * This error will also be thrown when the class of a Factory specified by
 * a system property, or the class of the default system parser factory,
 * cannot be loaded or instantiated.
 * <br>
 * Implementation or Application developers should never need to directly
 * construct or catch errors of this type.
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
public class FactoryConfigurationError extends Error {

    /** The root cause of this <code>FactoryConfigurationError</code>. */
    private Exception exception=null;

    /**
     * Constructs a new <code>FactoryConfigurationError</code> with no
     * detail message.
     */
    public FactoryConfigurationError() {
        this(null,null);
    }

    /**
     * Constructs a new <code>FactoryConfigurationError</code> with the
     * given detail message.
     */
    public FactoryConfigurationError(String msg) {
        this(null,msg);
    }

    /**
     * Constructs a new <code>FactoryConfigurationError</code> with the
     * given <code>Exception</code> as a root cause.
     */
    public FactoryConfigurationError(Exception e) {
        this(e,null);
    }

    /**
     * Constructs a new <code>FactoryConfigurationError</code> with the
     * given <code>Exception</code> as a root cause and the given detail
     * message.
     */
    public FactoryConfigurationError(Exception e, String msg) {
        super(msg);
        this.exception=e;
    }

    /**
     * Returns the root cause of this <code>FactoryConfigurationError</code>
     * or <b>null</b> if there is none.
     */
    public Exception getException() {
        return(this.exception);
    }
}
