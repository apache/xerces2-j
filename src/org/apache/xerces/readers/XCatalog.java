/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.readers;

import org.apache.xerces.framework.XMLParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.utils.StringPool;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This catalog supports the XCatalog proposal draft 0.2 posted
 * to the xml-dev mailing list by 
 * <a href="mailto:cowan@locke.ccil.org">John Cowan</a>. XCatalog
 * is an XML representation of the SGML Open TR9401:1997 catalog
 * format. The current proposal supports public identifier maps,
 * system identifier aliases, and public identifier prefix 
 * delegates. Refer to the XCatalog DTD for the full specification 
 * of this catalog format at 
 * <a href="http://www.ccil.org/~cowan/XML/XCatalog.html">http://www.ccil.org/~cowan/XML/XCatalog.html</a>.
 * <p>
 * In order to use XCatalogs, you must write the catalog files
 * with the following restrictions:
 * <ul>
 * <li>You must follow the XCatalog grammar.
 * <li>You must specify the <tt>&lt;!DOCTYPE&gt;</tt> line with
 *   the <tt>PUBLIC</tt> specified as "-//DTD XCatalog//EN" or
 *   make sure that the system identifier is able to locate the
 *   XCatalog 0.2 DTD (which is included in the Jar file containing
 *   the org.apache.xerces.readers.XCatalog class).
 *   For example:
 *   <pre>
 *   &lt;!DOCTYPE XCatalog PUBLIC "-//DTD XCatalog//EN" "org/apache/xerces/readers/xcatalog.dtd"&gt;
 *   </pre>
 * <li>The enclosing <tt>&lt;XCatalog&gt;</tt> document root 
 *   element is <b>not</b> optional -- it <b>must</b> be specified.
 * <li>The <tt>Version</tt> attribute of the <tt>&lt;XCatalog&gt;</tt>
 *   has been modified from '<tt><i>#FIXED "1.0"</i></tt>' to 
 *   '<tt><i>(0.1|0.2) "0.2"</i></tt>'.
 * </ul>
 * <p>
 * To use this catalog in a parser, set an XCatalog instance as the
 * parser's <tt>EntityResolver</tt>. For example:
 * <pre>
 *   XMLParser parser  = new AnyParser();
 *   Catalog   catalog = <font color="blue">new XCatalog()</font>;
 *   <font color="blue">parser.getEntityHandler().setEntityResolver(catalog);</font>
 * </pre>
 * <p>
 * Once installed, catalog files that conform to the XCatalog grammar
 * can be appended to the catalog by calling the <tt>loadCatalog</tt>
 * method on the parser or the catalog instance. The following example
 * loads the contents of two catalog files:
 * <pre>
 *   parser.loadCatalog(new InputSource("catalogs/cat1.xml"));
 *   parser.loadCatalog(new InputSource("http://host/catalogs/cat2.xml"));
 * </pre>
 * <p>
 * <b>Limitations:</b> The following are the current limitations
 * of this XCatalog implementation:
 * <ul>
 * <li>No error checking is done to avoid circular <tt>Delegate</tt>
 *   or <tt>Extend</tt> references. Do not specify a combination of
 *   catalog files that reference each other.
 * </ul>
 *
 * @author  Andy Clark, IBM
 * @version
 */
public class XCatalog
    extends XMLCatalogHandler
    {

    //
    // Constants
    //

    // public

    /** XCatalog public identifier string ("-//DTD XCatalog//EN"). */
    public static final String XCATALOG_DTD_PUBLICID = "-//DTD XCatalog//EN";

    // "default"

    /** XCatalog DTD resource name ("xcatalog.dtd"). */
    static final String DTD = "xcatalog.dtd";

    /** XCatalog element name ("XCatalog"). */
    static final String XCATALOG = "XCatalog";

    /** Map element name ("Map"). */
    static final String MAP = "Map";

    /** PublicID attribute name ("PublicID"). */
    static final String PUBLICID = "PublicID";

    /** HRef attribute name ("HRef"). */
    static final String HREF = "HRef";

    /** Delegate element name ("Delegate"). */
    static final String DELEGATE = "Delegate";

    /** Extend element name ("Extend"). */
    static final String EXTEND = "Extend";

    /** Base element name ("Base"). */
    static final String BASE = "Base";

    /** Remap element name ("Remap"). */
    static final String REMAP = "Remap";

    /** SystemID attribute name ("SystemID"). */
    static final String SYSTEMID = "SystemID";

    // private

    /** Set to true and recompile to include debugging code in class. */
    private static final boolean DEBUG = false;

    //
    // Data
    //

    /** Delegates. */
    private Hashtable delegate = new Hashtable();

    /** Delegates ordering. */
    private Vector delegateOrder = new Vector();

    //
    // Constructors
    //

    /** 
     * Constructs an XCatalog instance.
     */
    public XCatalog() {
    }

    //
    // Catalog methods
    //

    /**
     * Loads the catalog stream specified by the given input source and
     * appends the contents to the catalog.
     *
     * @param source The catalog source.
     *
     * @exception org.xml.sax.SAXException Throws exception on SAX error.
     * @exception java.io.IOException Throws exception on i/o error.
     */
    public void loadCatalog(InputSource source)
        throws SAXException, IOException
        {
        
        new Parser(source);

        /***
        if (DEBUG) {
            print("");
            }
        /***/

        } // loadCatalog(InputSource)

    /***
    void print(String indent) {
        System.out.println(indent+"# "+this);
        Enumeration maps = getMapKeys();
        while (maps.hasMoreElements()) {
            String key   = (String)maps.nextElement();
            String value = getMapValue(key);
            System.out.println(indent+"MAP \""+key+"\" -> \""+value+"\"");
            }
        Enumeration delegates = getDelegateKeys();
        while (delegates.hasMoreElements()) {
            String   key   = (String)delegates.nextElement();
            XCatalog value = getDelegateValue(key);
            System.out.println(indent+"DELEGATE \""+key+"\" -> "+value);
            value.print(indent+"  ");
            }
        Enumeration remaps = getRemapKeys();
        while (remaps.hasMoreElements()) {
            String key   = (String)remaps.nextElement();
            String value = getRemapValue(key);
            System.out.println(indent+"REMAP \""+key+"\" -> \""+value+"\"");
            }
        }
    /***/

    //
    // EntityResolver methods
    //

    /**
     * Resolves external entities.
     *
     * @param publicId The public identifier used for entity resolution.
     * @param systemId If the publicId is not null, this systemId is
     *                 to be considered the default system identifier;
     *                 else a system identifier alias mapping is
     *                 requested.
     *
     * @return Returns the input source of the resolved entity or null
     *         if no resolution is possible.
     *
     * @exception org.xml.sax.SAXException Exception thrown on SAX error.
     * @exception java.io.IOException Exception thrown on i/o error. 
     */
    public InputSource resolveEntity(String publicId, String systemId) 
        throws SAXException, IOException
        {

        if (DEBUG) {
            System.out.println("resolveEntity(\""+publicId+"\", \""+systemId+"\")");
            }

        // public identifier resolution
        if (publicId != null) {
            // direct public id mappings
            String value = getPublicMapping(publicId);
            if (DEBUG) {
                System.out.println("  map: \""+publicId+"\" -> \""+value+"\"");
                }
            if (value != null) {
                InputSource source = resolveEntity(null, value);
                if (source == null) {
                    source = new InputSource(value);
                    }
                source.setPublicId(publicId);
                return source;
                }

            // delegates
            Enumeration delegates = getDelegateCatalogKeys();
            while (delegates.hasMoreElements()) {
                String key = (String)delegates.nextElement();
                if (DEBUG) {
                    System.out.println("  delegate: \""+key+"\"");
                    }
                if (publicId.startsWith(key)) {
                    XMLCatalogHandler catalog = getDelegateCatalog(key);
                    InputSource source = catalog.resolveEntity(publicId, systemId);
                    if (source != null) {
                        return source;
                        }
                    }
                }
            }

        // system identifier resolution
        String value = getSystemMapping(systemId);
        if (value != null) {
            if (DEBUG) {
                System.out.println("  remap: \""+systemId+"\" -> \""+value+"\"");
                }
            InputSource source = new InputSource(value);
            source.setPublicId(publicId);
            return source;
            }

        // use default behavior
        if (DEBUG) {
            System.out.println("  returning null!");
            }
        return null;

        } // resolveEntity(String,String):InputSource

    //
    // Public methods
    //

    /** 
     * Adds a delegate mapping. If the prefix of a public identifier
     * matches a delegate prefix, then the delegate catalog is
     * searched in order to resolve the identifier.
     * <p>
     * This method makes sure that prefixes that match each other
     * are inserted into the delegate list in order of longest prefix
     * length first.
     *
     * @param prefix  The delegate prefix.
     * @param catalog The delegate catalog.
     */
    public void addDelegateCatalog(String prefix, XCatalog catalog) {

        synchronized (delegate) {
            // insert prefix in proper order
            if (!delegate.containsKey(prefix)) {
                int size = delegateOrder.size();
                boolean found = false;
                for (int i = 0; i < size; i++) {
                    String element = (String)delegateOrder.elementAt(i);
                    if (prefix.startsWith(element) || prefix.compareTo(element) < 0) {
                        delegateOrder.insertElementAt(prefix, i);
                        found = true;
                        break;
                        }
                    }
                if (!found) {
                    delegateOrder.addElement(prefix);
                    }
                }

            // replace (or add new) prefix mapping
            delegate.put(prefix, catalog);
            }

        } // addDelegateCatalog(String,XCatalog)

    /** 
     * Removes a delegate. 
     *
     * @param prefix The delegate prefix to remove.
     */
    public void removeDelegateCatalog(String prefix) {

        synchronized (delegate) {
            delegate.remove(prefix);
            delegateOrder.removeElement(prefix);
            }

        } // removeDelegateCatalog(String)

    /** Returns an enumeration of delegate prefixes. */
    public Enumeration getDelegateCatalogKeys() {
        return delegateOrder.elements();
        }

    /** Returns the catalog for the given delegate prefix. */
    public XCatalog getDelegateCatalog(String prefix) {
        return (XCatalog)delegate.get(prefix);
        }

    //
    // "default" methods
    //

    /** Returns true if the string is a valid URL. */
    boolean isURL(String str) {
        try {
            new java.net.URL(str);
            return true;
            }
        catch (java.net.MalformedURLException e) {
            // assume the worst
            }
        return false;
        }

    //
    // Classes
    //

    /** Parser for XCatalog document instances. */
    class Parser
        extends SAXParser
        implements DocumentHandler
        {

        //
        // Data
        //

        /** The base. */
        private String base;

        //
        // Constructors
        //

        /** Parses the specified input source. */
        public Parser(InputSource source) 
            throws SAXException, IOException
            {

            // setup parser
            setEntityResolver(new Resolver());
            setDocumentHandler((DocumentHandler)this);

            // set base and parse
            setBase(source.getSystemId());
            parse(source);

            } // <init>(InputSource)

        //
        // Protected methods
        //

        /** 
         * Sets the base from the given system identifier. The base is
         * the same as the system identifier with the least significant
         * part (the filename) removed.
         */
        protected void setBase(String systemId) throws SAXException {

            // normalize system id
            if (systemId == null) { 
                systemId = ""; 
                }

            // expand system id
            systemId = expandSystemId(systemId);

            // cut off the least significant part
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(0, index + 1);
                }

            // save base
            base = systemId;

            } // setBase(String)

        //
        // DocumentHandler methods
        //

        /** Not implemented. */
        public void processingInstruction(String target, String data) {}

        /** Not implemented. */
        public void setDocumentLocator(org.xml.sax.Locator locator) {}

        /** Not implemented. */
        public void startDocument() {}

        /** Not implemented. */
        public void endElement(String elementName) {}

        /** Not implemented. */
        public void endDocument() {}

        /** Not implemented. */
        public void characters(char ch[], int start, int length) {}

        /** Not implemented. */
        public void ignorableWhitespace(char ch[], int start, int length) {}

        /** The start of an element. */
        public void startElement(String elementName, AttributeList attrList) 
            throws SAXException
            {

            try {
                // <XCatalog Version="...">
                if (elementName.equals(XCATALOG)) {
                    return;
                    }
    
                // <Map PublicID="..." HRef="..."/>
                if (elementName.equals(MAP)) {
                    // get attributes
                    String publicId = attrList.getValue(PUBLICID);
                    String href     = attrList.getValue(HREF);
                    if (DEBUG) {
                        System.out.println("MAP \""+publicId+"\" \""+href+"\"");
                        }
    
                    // create mapping
                    if (!isURL(href)) {
                        href = base + href;
                        }
                    addPublicMapping(publicId, href);
                    }
    
                // <Delegate PublicId="..." HRef="..."/>
                else if (elementName.equals(DELEGATE)) {
                    // get attributes
                    String publicId = attrList.getValue(PUBLICID);
                    String href     = attrList.getValue(HREF);
                    if (DEBUG) {
                        System.out.println("DELEGATE \""+publicId+"\" \""+href+"\"");
                        }
    
                    // expand system id
                    if (!isURL(href)) {
                        href = base + href;
                        }
                    String systemId = expandSystemId(href);
    
                    // create delegate
                    XCatalog catalog = new XCatalog();
                    catalog.loadCatalog(new InputSource(systemId));
                    addDelegateCatalog(publicId, catalog);
                    }
    
                // <Extend HRef="..."/>
                else if (elementName.equals(EXTEND)) {
                    // get attributes
                    String href = attrList.getValue(HREF);
                    if (DEBUG) {
                        System.out.println("EXTEND \""+href+"\"");
                        }
    
                    // expand system id
                    if (!isURL(href)) {
                        href = base + href;
                        }
                    String systemId = expandSystemId(href);
    
                    // create catalog
                    XCatalog.this.loadCatalog(new InputSource(systemId));
                    }
    
                // <Base HRef="..."/>
                else if (elementName.equals(BASE)) {
                    // get attributes
                    String href = attrList.getValue(HREF);
    
                    // set new base
                    setBase(href);
                    if (DEBUG) {
                        System.out.println("BASE \""+href+"\" -> \""+base+"\"");
                        }
                    }
                
                // <Remap SystemID="..." HRef="..."/>
                else if (elementName.equals(REMAP)) {
                    // get attributes
                    String systemId = attrList.getValue(SYSTEMID);
                    String href     = attrList.getValue(HREF);
                    if (DEBUG) {
                        System.out.println("REMAP \""+systemId+"\" \""+href+"\"");
                        }
    
                    // create mapping
                    if (!isURL(href)) {
                        href = base + href;
                        }
                    addSystemMapping(systemId, href);
                    }
                }
            catch (Exception e) {
                throw new SAXException(e);
                }

            } // startElement(String,AttributeList)

        //
        // Classes
        //

        /** Resolver for locating the XCatalog DTD resource. */
        class Resolver
            implements EntityResolver
            {

            /** Resolves the XCatalog DTD entity. */
            public InputSource resolveEntity(String publicId, String systemId) 
                throws SAXException, IOException
                {

                // resolve the XCatalog DTD?
                if (publicId != null && publicId.equals(XCATALOG_DTD_PUBLICID)) {
                    InputSource src = new InputSource();
                    src.setPublicId(publicId);
                    InputStream is = getClass().getResourceAsStream(DTD);
                    src.setByteStream(is);
                    src.setCharacterStream(new InputStreamReader(is));
                    return src;
                    }

                // no resolution possible
                return null;

                } // resolveEntity(String,String):InputSource

            } // class Resolver

        } // class Parser

    } // class XCatalog
