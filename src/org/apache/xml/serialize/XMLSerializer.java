/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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



// Sep 14, 2000:
//  Fixed problem with namespace handling. Contributed by
//  David Blondeau <blondeau@intalio.com>
// Sep 14, 2000:
//  Fixed serializer to report IO exception directly, instead at
//  the end of document processing.
//  Reported by Patrick Higgins <phiggins@transzap.com>
// Aug 21, 2000:
//  Fixed bug in startDocument not calling prepare.
//  Reported by Mikael Staldal <d96-mst-ingen-reklam@d.kth.se>
// Aug 21, 2000:
//  Added ability to omit DOCTYPE declaration.


package org.apache.xml.serialize;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.dom.*;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xerces.dom3.DOMErrorHandler;
import org.apache.xerces.dom3.DOMError;
import org.apache.xerces.dom3.ls.DOMWriter;
import org.apache.xerces.dom3.DOMErrorHandler;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.NamespaceContext;

/**
 * Implements an XML serializer supporting both DOM and SAX pretty
 * serializing. For usage instructions see {@link Serializer}.
 * <p>
 * If an output stream is used, the encoding is taken from the
 * output format (defaults to <tt>UTF-8</tt>). If a writer is
 * used, make sure the writer uses the same encoding (if applies)
 * as specified in the output format.
 * <p>
 * The serializer supports both DOM and SAX. SAX serializing is done by firing
 * SAX events and using the serializer as a document handler. DOM serializing is done
 * by calling {@link #serialize(Document)} or by using DOM Level 3  
 * {@link org.apache.xerces.dom3.ls.DOMWriter} and
 * serializing with {@link org.apache.xerces.dom3.ls.DOMWriter#writeNode}, 
 * {@link org.apache.xerces.dom3.ls.DOMWriter#writeToString}.
 * <p>
 * If an I/O exception occurs while serializing, the serializer
 * will not throw an exception directly, but only throw it
 * at the end of serializing (either DOM or SAX's {@link
 * org.xml.sax.DocumentHandler#endDocument}.
 * <p>
 * For elements that are not specified as whitespace preserving,
 * the serializer will potentially break long text lines at space
 * boundaries, indent lines, and serialize elements on separate
 * lines. Line terminators will be regarded as spaces, and
 * spaces at beginning of line will be stripped.
 * @author <a href="mailto:arkin@intalio.com">Assaf Arkin</a>
 * @author <a href="mailto:rahul.srivastava@sun.com">Rahul Srivastava</a>
 * @author Elena Litani IBM
 * @version $Revision$ $Date$
 * @see Serializer
 */
public class XMLSerializer
extends BaseMarkupSerializer
implements DOMWriter {

    //
    // constants
    //

    protected static final boolean DEBUG = false;

    // 
    // data
    //

    protected final NamespaceSupport fNamespaceSupport = new NamespaceSupport();
    protected final SymbolTable fSymbolTable = new SymbolTable();    
    protected String fEmptySymbol;
    protected String fXmlSymbol;
    protected String fXmlnsSymbol;
    // is node dom level 1 node?
    protected boolean fDOML1 = false;
    // counter for new prefix names
    protected int fNamespaceCounter = 1;


    private boolean fPreserveSpace;
    private String fEncoding;
    private String fLastEncoding;


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public XMLSerializer() {
        super( new OutputFormat( Method.XML, null, false ) );
        fFeatures = new Hashtable();
        initFeatures();
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public XMLSerializer( OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
        fFeatures = new Hashtable();
        initFeatures();
    }


    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public XMLSerializer( Writer writer, OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
        setOutputCharStream( writer );
        fFeatures = new Hashtable();
        initFeatures();
    }


    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public XMLSerializer( OutputStream output, OutputFormat format ) {
        super( format != null ? format : new OutputFormat( Method.XML, null, false ) );
        _format.setMethod( Method.XML );
        setOutputByteStream( output );
        fFeatures = new Hashtable();
        initFeatures();
    }


    public void setOutputFormat( OutputFormat format ) {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XML, null, false ) );
    }


    //-----------------------------------------//
    // SAX content handler serializing methods //
    //-----------------------------------------//


    public void startElement( String namespaceURI, String localName,
                              String rawName, Attributes attrs )
    throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;
        boolean      addNSAttr = false;

        try {
            if (_printer == null)
                throw new IllegalStateException( "SER002 No writer supplied for serializer" );

            state = getElementState();
            if (isDocumentState()) {
                // If this is the root element handle it differently.
                // If the first root element in the document, serialize
                // the document's DOCTYPE. Space preserving defaults
                // to that of the output format.
                if (! _started)
                    startDocument( ( localName == null || localName.length() == 0 ) ? rawName : localName );
            }
            else {
                // For any other element, if first in parent, then
                // close parent's opening tag and use the parnet's
                // space preserving.
                if (state.empty)
                    _printer.printText( '>' );
                // Must leave CData section first
                if (state.inCData) {
                    _printer.printText( "]]>" );
                    state.inCData = false;
                }
                // Indent this element on a new line if the first
                // content of the parent element or immediately
                // following an element or a comment
                if (_indenting && ! state.preserveSpace &&
                    ( state.empty || state.afterElement || state.afterComment))
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;

            //We remove the namespaces from the attributes list so that they will
            //be in _prefixes
            attrs = extractNamespaces(attrs);

            // Do not change the current element state yet.
            // This only happens in endElement().
            if (rawName == null || rawName.length() == 0) {
                if (localName == null)
                    throw new SAXException( "No rawName and localName is null" );
                if (namespaceURI != null && ! namespaceURI.equals( "" )) {
                    String prefix;
                    prefix = getPrefix( namespaceURI );
                    if (prefix != null && prefix.length() > 0)
                        rawName = prefix + ":" + localName;
                    else
                        rawName = localName;
                }
                else
                    rawName = localName;
                addNSAttr = true;
            }

            _printer.printText( '<' );
            _printer.printText( rawName );
            _printer.indent();

            // For each attribute print it's name and value as one part,
            // separated with a space so the element can be broken on
            // multiple lines.
            if (attrs != null) {
                for (i = 0 ; i < attrs.getLength() ; ++i) {
                    _printer.printSpace();

                    name = attrs.getQName( i );
                    if (name != null && name.length() == 0) {
                        String prefix;
                        String attrURI;

                        name = attrs.getLocalName( i );
                        attrURI = attrs.getURI( i );
                        if (( attrURI != null && attrURI.length() != 0 ) &&
                            ( namespaceURI == null || namespaceURI.length() == 0 ||
                              ! attrURI.equals( namespaceURI ) )) {
                            prefix = getPrefix( attrURI );
                            if (prefix != null && prefix.length() > 0)
                                name = prefix + ":" + name;
                        }
                    }

                    value = attrs.getValue( i );
                    if (value == null)
                        value = "";
                    _printer.printText( name );
                    _printer.printText( "=\"" );
                    printEscaped( value );
                    _printer.printText( '"' );

                    // If the attribute xml:space exists, determine whether
                    // to preserve spaces in this and child nodes based on
                    // its value.
                    if (name.equals( "xml:space" )) {
                        if (value.equals( "preserve" ))
                            preserveSpace = true;
                        else
                            preserveSpace = _format.getPreserveSpace();
                    }
                }
            }

            if (_prefixes != null) {
                Enumeration enum;

                enum = _prefixes.keys();
                while (enum.hasMoreElements()) {
                    _printer.printSpace();
                    value = (String) enum.nextElement();
                    name = (String) _prefixes.get( value );
                    if (name.length() == 0) {
                        _printer.printText( "xmlns=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                    else {
                        _printer.printText( "xmlns:" );
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }
                }
            }

            // Now it's time to enter a new element state
            // with the tag name and space preserving.
            // We still do not change the curent element state.
            state = enterElementState( namespaceURI, localName, rawName, preserveSpace );
            name = ( localName == null || localName.length() == 0 ) ? rawName : namespaceURI + "^" + localName;
            state.doCData = _format.isCDataElement( name );
            state.unescaped = _format.isNonEscapingElement( name );
        }
        catch (IOException except) {
            throw new SAXException( except );
        }
    }


    public void endElement( String namespaceURI, String localName,
                            String rawName )
    throws SAXException
    {
        try {
            endElementIO( namespaceURI, localName, rawName );
        }
        catch (IOException except) {
            throw new SAXException( except );
        }
    }


    public void endElementIO( String namespaceURI, String localName,
                              String rawName )
    throws IOException
    {
        ElementState state;
        fNamespaceSupport.popContext();
        if (DEBUG) {
            System.out.println("==>endElement: " +rawName);
        }
        // Works much like content() with additions for closing
        // an element. Note the different checks for the closed
        // element's state and the parent element's state.
        _printer.unindent();
        state = getElementState();
        if (state.empty) {
            _printer.printText( "/>" );
        }
        else {
            // Must leave CData section first
            if (state.inCData)
                _printer.printText( "]]>" );
            // This element is not empty and that last content was
            // another element, so print a line break before that
            // last element and this element's closing tag.
            if (_indenting && ! state.preserveSpace && (state.afterElement || state.afterComment))
                _printer.breakLine();
            _printer.printText( "</" );
            _printer.printText( state.rawName );
            _printer.printText( '>' );
        }
        // Leave the element state and update that of the parent
        // (if we're not root) to not empty and after element.
        state = leaveElementState();
        state.afterElement = true;
        state.afterComment = false;
        state.empty = false;
        if (isDocumentState())
            _printer.flush();
    }


    //------------------------------------------//
    // SAX document handler serializing methods //
    //------------------------------------------//


    public void startElement( String tagName, AttributeList attrs )
    throws SAXException
    {
        int          i;
        boolean      preserveSpace;
        ElementState state;
        String       name;
        String       value;

        try {
            if (_printer == null)
                throw new IllegalStateException( "SER002 No writer supplied for serializer" );

            state = getElementState();
            if (isDocumentState()) {
                // If this is the root element handle it differently.
                // If the first root element in the document, serialize
                // the document's DOCTYPE. Space preserving defaults
                // to that of the output format.
                if (! _started)
                    startDocument( tagName );
            }
            else {
                // For any other element, if first in parent, then
                // close parent's opening tag and use the parnet's
                // space preserving.
                if (state.empty)
                    _printer.printText( '>' );
                // Must leave CData section first
                if (state.inCData) {
                    _printer.printText( "]]>" );
                    state.inCData = false;
                }
                // Indent this element on a new line if the first
                // content of the parent element or immediately
                // following an element.
                if (_indenting && ! state.preserveSpace &&
                    ( state.empty || state.afterElement || state.afterComment))
                    _printer.breakLine();
            }
            preserveSpace = state.preserveSpace;

            // Do not change the current element state yet.
            // This only happens in endElement().

            _printer.printText( '<' );
            _printer.printText( tagName );
            _printer.indent();

            // For each attribute print it's name and value as one part,
            // separated with a space so the element can be broken on
            // multiple lines.
            if (attrs != null) {
                for (i = 0 ; i < attrs.getLength() ; ++i) {
                    _printer.printSpace();
                    name = attrs.getName( i );
                    value = attrs.getValue( i );
                    if (value != null) {
                        _printer.printText( name );
                        _printer.printText( "=\"" );
                        printEscaped( value );
                        _printer.printText( '"' );
                    }

                    // If the attribute xml:space exists, determine whether
                    // to preserve spaces in this and child nodes based on
                    // its value.
                    if (name.equals( "xml:space" )) {
                        if (value.equals( "preserve" ))
                            preserveSpace = true;
                        else
                            preserveSpace = _format.getPreserveSpace();
                    }
                }
            }
            // Now it's time to enter a new element state
            // with the tag name and space preserving.
            // We still do not change the curent element state.
            state = enterElementState( null, null, tagName, preserveSpace );
            state.doCData = _format.isCDataElement( tagName );
            state.unescaped = _format.isNonEscapingElement( tagName );
        }
        catch (IOException except) {
            throw new SAXException( except );
        }

    }


    public void endElement( String tagName )
    throws SAXException
    {
        endElement( null, null, tagName );
    }



    //------------------------------------------//
    // Generic node serializing methods methods //
    //------------------------------------------//


    /**
     * Called to serialize the document's DOCTYPE by the root element.
     * The document type declaration must name the root element,
     * but the root element is only known when that element is serialized,
     * and not at the start of the document.
     * <p>
     * This method will check if it has not been called before ({@link #_started}),
     * will serialize the document type declaration, and will serialize all
     * pre-root comments and PIs that were accumulated in the document
     * (see {@link #serializePreRoot}). Pre-root will be serialized even if
     * this is not the first root element of the document.
     */
    protected void startDocument( String rootTagName )
    throws IOException
    {
        int    i;
        String dtd;

        dtd = _printer.leaveDTD();
        if (! _started) {

            if (! _format.getOmitXMLDeclaration()) {
                StringBuffer    buffer;

                // Serialize the document declaration appreaing at the head
                // of very XML document (unless asked not to).
                buffer = new StringBuffer( "<?xml version=\"" );
                if (_format.getVersion() != null)
                    buffer.append( _format.getVersion() );
                else
                    buffer.append( "1.0" );
                buffer.append( '"' );
                if (_format.getEncoding() != null) {
                    buffer.append( " encoding=\"" );
                    buffer.append( _format.getEncoding() );
                    buffer.append( '"' );
                }
                if (_format.getStandalone() && _docTypeSystemId == null &&
                    _docTypePublicId == null)
                    buffer.append( " standalone=\"yes\"" );
                buffer.append( "?>" );
                _printer.printText( buffer );
                _printer.breakLine();
            }

            if (! _format.getOmitDocumentType()) {
                if (_docTypeSystemId != null) {
                    // System identifier must be specified to print DOCTYPE.
                    // If public identifier is specified print 'PUBLIC
                    // <public> <system>', if not, print 'SYSTEM <system>'.
                    _printer.printText( "<!DOCTYPE " );
                    _printer.printText( rootTagName );
                    if (_docTypePublicId != null) {
                        _printer.printText( " PUBLIC " );
                        printDoctypeURL( _docTypePublicId );
                        if (_indenting) {
                            _printer.breakLine();
                            for (i = 0 ; i < 18 + rootTagName.length() ; ++i)
                                _printer.printText( " " );
                        }
                        else
                            _printer.printText( " " );
                        printDoctypeURL( _docTypeSystemId );
                    }
                    else {
                        _printer.printText( " SYSTEM " );
                        printDoctypeURL( _docTypeSystemId );
                    }

                    // If we accumulated any DTD contents while printing.
                    // this would be the place to print it.
                    if (dtd != null && dtd.length() > 0) {
                        _printer.printText( " [" );
                        printText( dtd, true, true );
                        _printer.printText( ']' );
                    }

                    _printer.printText( ">" );
                    _printer.breakLine();
                }
                else if (dtd != null && dtd.length() > 0) {
                    _printer.printText( "<!DOCTYPE " );
                    _printer.printText( rootTagName );
                    _printer.printText( " [" );
                    printText( dtd, true, true );
                    _printer.printText( "]>" );
                    _printer.breakLine();
                }
            }
        }
        _started = true;
        // Always serialize these, even if not te first root element.
        serializePreRoot();
    }


    /**
     * Called to serialize a DOM element. Equivalent to calling {@link
     * #startElement}, {@link #endElement} and serializing everything
     * inbetween, but better optimized.
     */
    protected void serializeElement( Element elem )
    throws IOException
    {
        Attr         attr;
        NamedNodeMap attrMap;
        int          i;
        Node         child;
        ElementState state;
        String       name;
        String       value;
        String       tagName;

        String prefix;
        String uri;


        // add new namespace context
        fNamespaceSupport.pushContext();
        if (DEBUG) {
            System.out.println("==>startElement: " +elem.getNodeName());
        }
        tagName = elem.getTagName();
        state = getElementState();
        if (isDocumentState()) {
            // If this is the root element handle it differently.
            // If the first root element in the document, serialize
            // the document's DOCTYPE. Space preserving defaults
            // to that of the output format.

            // check if document is DOM L1 document
            fDOML1 = (elem.getLocalName() == null)? true: false;

            if (! _started)
                startDocument( tagName );
        }
        else {
            // For any other element, if first in parent, then
            // close parent's opening tag and use the parent's
            // space preserving.
            if (state.empty)
                _printer.printText( '>' );
            // Must leave CData section first
            if (state.inCData) {
                _printer.printText( "]]>" );
                state.inCData = false;
            }
            // Indent this element on a new line if the first
            // content of the parent element or immediately
            // following an element.
            if (_indenting && ! state.preserveSpace &&
                ( state.empty || state.afterElement || state.afterComment))
                _printer.breakLine();
        }

        // Do not change the current element state yet.
        // This only happens in endElement().
        fPreserveSpace = state.preserveSpace;


        // -----------------------------------------
        // seach for new namespace binding attributes 
        // -----------------------------------------
        // 
        attrMap = elem.getAttributes();
        int length = 0;

        //-----------------------
        // get element uri/prefix
        //-----------------------
        uri = elem.getNamespaceURI();            
        prefix = elem.getPrefix();


        //----------------------
        // output element name
        //----------------------
        if ((uri !=null && prefix !=null ) && uri.length() == 0 && prefix.length()!=0) {
            // uri is an empty string and element has some prefix
            // the namespace alg later will fix up the namespace attributes
            // remove element prefix
            _printer.printText( '<' );
            _printer.printText( elem.getLocalName() );
            _printer.indent();
        }
        else {
            _printer.printText( '<' );
            _printer.printText( tagName );
            _printer.indent();
        }

        // REVISIT: should we report error/warning if DOM 1 nodes mix with DOM 2 nodes?

        // -----------------------------------------
        // Fix up namespaces for element: per DOM L3 
        // check if prefix/namespace is correct for current element
        // -----------------------------------------
        if (uri != null) {  // Element has a namespace

            uri = fSymbolTable.addSymbol(uri);
            prefix = prefix == null ? fEmptySymbol :fSymbolTable.addSymbol(prefix);
            if (fNamespaceSupport.getURI(prefix) == uri) {
                // this namespace was declared at parent level
                // do nothing

            }
            else {
                // the prefix is either undeclared 
                // or
                // conflict: the prefix is bound to another URI

                // check if uri is an empty string && element has prefix
                if (uri == fEmptySymbol && prefix != fEmptySymbol) {

                    // the element prefix is already removed above
                    prefix = fEmptySymbol;
                    if (fNamespaceSupport.getURI(prefix) !=null) {
                        // xmlns default namespace is bound to something
                        // undeclare it

                        // print default namespace declaration
                        printNamespaceAttr(prefix, uri);
                        fNamespaceSupport.declarePrefix(prefix, uri);
                    }
                }
                else {
                    // add or modify local declaration
                    printNamespaceAttr(prefix, uri);
                    fNamespaceSupport.declarePrefix(prefix, uri);
                }

            }
        }
        else { // Element has no namespace

            int colon = tagName.indexOf(':');
            if (colon > -1) {
                //  DOM Level 1 node!
                int colon2 = tagName.lastIndexOf(':');
                if (colon != colon2) {
                    //not a QName: report an error
                    if (fDOMErrorHandler != null) {
                        modifyDOMError("Element's name is not a QName: "+tagName, DOMError.SEVERITY_ERROR);
                        boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                        // REVISIT: should we terminate upon request?                        
                    }
                }
                else {
                    // if we got here no namespace processing was performed
                    // report warnings
                    if (fDOMErrorHandler != null) {
                        modifyDOMError("Element <"+tagName+"> does not belong to any namespace: prefix could be undeclared or bound to some namespace", DOMError.SEVERITY_WARNING);
                        boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                    }
                }
            }
            else { // uri=null and no colon

                if (fNamespaceSupport.getURI(fEmptySymbol) !=null) {
                    // there is a default namespace decl that is bound to something else
                    printNamespaceAttr(fEmptySymbol, fEmptySymbol);
                    fNamespaceSupport.declarePrefix(fEmptySymbol, uri);

                }
            }
        }

        // -----------------------------------------
        // Fix up namespaces for attributes: per DOM L3 
        // check if prefix/namespace is correct the attributes
        // -----------------------------------------

        if (attrMap !=null) {

            for (i = 0; i < attrMap.getLength(); i++) {

                attr = (Attr) attrMap.item( i );
                value = attr.getValue();
                name = attr.getNodeName();
                if (DEBUG) {
                    System.out.println("==>process attribute: "+attr.getNodeName());
                }
                uri = attr.getNamespaceURI();



                if (uri != null) {  // attribute has namespace !=null
                    prefix = attr.getPrefix();
                    prefix = prefix == null ? fEmptySymbol :fSymbolTable.addSymbol(prefix);
                    String localpart = fSymbolTable.addSymbol( attr.getLocalName());

                    uri = fSymbolTable.addSymbol(uri);

                    // find if for this prefix a URI was already declared
                    String declaredURI =  fNamespaceSupport.getURI(prefix);

                    // check if attribute is a namespace decl 
                    if (prefix == fXmlnsSymbol) { //xmlns:prefix

                        // REVISIT: should we skip invalid xmlns declaration
                        //          xmlns:foo = "" ??
                        if (fNamespaceSupport.getURI(localpart) == null) {
                            // this declaration is not in the scope
                            printNamespaceAttr(localpart, value);
                            fNamespaceSupport.declarePrefix(localpart, value);
                        }
                        continue;
                    }
                    else if (localpart == fXmlnsSymbol && prefix == fEmptySymbol) { // xmlns
                        if (fNamespaceSupport.getURI(fEmptySymbol) == null) {
                            // this declaration is not in the scope
                            printNamespaceAttr(fEmptySymbol, value);
                            fNamespaceSupport.declarePrefix(fEmptySymbol, value);
                        }
                        continue;

                    }
                    
                    if (prefix == fEmptySymbol || declaredURI != uri) {
                        // attribute has no prefix (default namespace decl does not apply to attributes) 
                        // OR
                        // attribute prefix is not declared
                        // OR
                        // conflict: attr URI does not match the prefix in scope

                        name  = attr.getNodeName();
                        // Find if any prefix for attributes namespace URI is available
                        // in the scope
                        String declaredPrefix = fNamespaceSupport.getPrefix(uri);
                        if (declaredPrefix == null || declaredPrefix == fEmptySymbol) {
                            // could not find a prefix/prefix is empty string
                            if (DEBUG) {
                                System.out.println("==> cound not find prefix for the attribute: " +prefix);
                            }
                            if (prefix != fEmptySymbol) {
                                // no need to create a new prefix:
                                // use the one on the attribute
                            } else {                            
                                // create new prefix
                                prefix = "NS" +fNamespaceCounter++; 
                            }
                            // add declaration for the new prefix
                            printNamespaceAttr(prefix, uri);
                            fNamespaceSupport.declarePrefix(prefix, uri);
                        }  else {
                            // use the prefix that was found (declared previously for this URI
                            prefix = declaredPrefix;
                        }
                        name=prefix+":"+localpart;
                        // change prefix for this attribute
                    }

                    printAttribute (name, (value==null)?fEmptySymbol:value, attr.getSpecified());
                }
                else { // attribute uri == null

                    // data
                    name = attr.getNodeName();
                    int colon = name.indexOf(':');
                    int colon2 = name.lastIndexOf(':');
                    value = attr.getValue();
                    // check if attribute is a namespace decl 
                    if (name.startsWith(fXmlnsSymbol)) {
                        //
                        //  DOM Level 1 node!
                        // 
                        if (colon < 0) {  // xmlns decl
                            if (fNamespaceSupport.getURI(fEmptySymbol) == null) {
                                // this declaration is not in the scope
                                fNamespaceSupport.declarePrefix(fEmptySymbol, (value==null)?fEmptySymbol:value);
                            }
                            else {
                                // duplicate declaration
                                // skip
                                continue;
                            }

                        }
                        else if (colon == colon2) { // xmlns:prefix decl
                            // get prefix
                            prefix = name.substring(6);
                            if (prefix.length() == 0) {
                                // report an error - invalid namespace declaration
                                if (fDOMErrorHandler != null) {
                                    modifyDOMError("Namespace declaration syntax is incorrect "+name, DOMError.SEVERITY_ERROR);
                                    boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                                }
                                // REVISIT: skip invalid declaration?
                                // continue;

                            }
                            else if (value != null && value.length() == 0) {
                                if (fDOMErrorHandler != null) {
                                    modifyDOMError("Namespace declaration syntax is incorrect "+name, DOMError.SEVERITY_ERROR);
                                    boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                                }
                                // REVISIT: skip invalid declaration?
                                // continue;
                            }
                            else if (fNamespaceSupport.getURI(prefix) == null) {
                                // this declaration is not in the scope
                                fNamespaceSupport.declarePrefix(prefix, value);

                            }
                        } else { // colon !=colon
                            // will report error for this case later in the code
                        }

                    }

                    if (colon > -1) {
                        //
                        //  DOM Level 1 node!
                        // 
                        if (colon != colon2) {
                            //not a QName: report an error
                            if (fDOMErrorHandler != null) {
                                modifyDOMError("Attribute's name is not a QName: "+name, DOMError.SEVERITY_ERROR);
                                boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);                                                        
                            }

                        }
                        else {
                            // if we got here no namespace processing was performed
                            // report warnings
                            if (fDOMErrorHandler != null) {
                                modifyDOMError("Attribute '"+name+"' does not belong to any namespace: prefix could be undeclared or bound to some namespace", DOMError.SEVERITY_WARNING);
                                boolean continueProcess = fDOMErrorHandler.handleError(fDOMError);
                            }
                        }

                        // REVISIT: should we skip invalid declarations?
                        printAttribute (name, (value==null)?fEmptySymbol:value, attr.getSpecified());
                    }
                    else { // uri=null and no colon
                        // no fix up is needed: default namespace decl does not 
                        // apply to attributes

                        printAttribute (name, (value==null)?fEmptySymbol:value, attr.getSpecified());
                    }

                }
            }
        }



        // If element has children, then serialize them, otherwise
        // serialize en empty tag.        
        if (elem.hasChildNodes()) {
            // Enter an element state, and serialize the children
            // one by one. Finally, end the element.
            state = enterElementState( null, null, tagName, fPreserveSpace );
            state.doCData = _format.isCDataElement( tagName );
            state.unescaped = _format.isNonEscapingElement( tagName );
            child = elem.getFirstChild();
            while (child != null) {
                serializeNode( child );
                child = child.getNextSibling();
            }
            endElementIO( null, null, tagName );
        }
        else {
            if (DEBUG) {
                System.out.println("==>endElement: " +elem.getNodeName());
            }
            fNamespaceSupport.popContext();
            _printer.unindent();
            _printer.printText( "/>" );
            // After element but parent element is no longer empty.
            state.afterElement = true;
            state.afterComment = false;
            state.empty = false;
            if (isDocumentState())
                _printer.flush();
        }
    }



    /**
     * Serializes a namespace attribute with the given prefix and value for URI.
     * In case prefix is empty will serialize default namespace declaration.
     * 
     * @param prefix
     * @param uri
     * @exception IOException
     */

    private void printNamespaceAttr(String prefix, String uri) throws IOException{
        _printer.printSpace();
        if (prefix == fEmptySymbol) {
            _printer.printText( fXmlnsSymbol );
        }
        else {
            _printer.printText( fXmlnsSymbol+ ":"+prefix );
        }
        _printer.printText( "=\"" );
        printEscaped( uri );
        _printer.printText( '"' );
    }

    /**
     * Prints attribute. 
     * NOTE: xml:space attribute modifies output format
     * 
     * @param name
     * @param value
     * @param isSpecified
     * @exception IOException
     */
    private void printAttribute (String name, String value, boolean isSpecified) throws IOException{

        if (isSpecified || !getFeature("discard-default-content")) {
            _printer.printSpace();
            _printer.printText( name );
            _printer.printText( "=\"" );
            printEscaped( value );
            _printer.printText( '"' );
        }

        // If the attribute xml:space exists, determine whether
        // to preserve spaces in this and child nodes based on
        // its value.
        if (name.equals( "xml:space" )) {
            if (value.equals( "preserve" ))
                fPreserveSpace = true;
            else
                fPreserveSpace = _format.getPreserveSpace();
        }
    }

    protected String getEntityRef( int ch ) {
        // Encode special XML characters into the equivalent character references.
        // These five are defined by default for all XML documents.
        switch (ch) {
        case '<':
            return "lt";
        case '>':
            return "gt";
        case '"':
            return "quot";
        case '\'':
            return "apos";
        case '&':
            return "amp";
        }
        return null;
    }


    /** Retrieve and remove the namespaces declarations from the list of attributes.
     *
     */
    private Attributes extractNamespaces( Attributes attrs )
    throws SAXException
    {
        AttributesImpl attrsOnly;
        String         rawName;
        int            i;
        int            indexColon;
        String         prefix;
        int            length;

        length = attrs.getLength();
        attrsOnly = new AttributesImpl( attrs );

        for (i = length - 1 ; i >= 0 ; --i) {
            rawName = attrsOnly.getQName( i );

            //We have to exclude the namespaces declarations from the attributes
            //Append only when the feature http://xml.org/sax/features/namespace-prefixes"
            //is TRUE
            if (rawName.startsWith( "xmlns" )) {
                if (rawName.length() == 5) {
                    startPrefixMapping( "", attrs.getValue( i ) );
                    attrsOnly.removeAttribute( i );
                }
                else if (rawName.charAt(5) == ':') {
                    startPrefixMapping(rawName.substring(6), attrs.getValue(i));
                    attrsOnly.removeAttribute( i );
                }
            }
        }
        return attrsOnly;
    }


    // 
    // DOM Level 3 implementation
    //

    private void initFeatures() {
        fFeatures.put("normalize-characters",new Boolean(false));
        fFeatures.put("split-cdata-sections",new Boolean(true));
        fFeatures.put("validation",new Boolean(false));
        fFeatures.put("expand-entity-references",new Boolean(false));
        fFeatures.put("whitespace-in-element-content",new Boolean(true));
        fFeatures.put("discard-default-content",new Boolean(true));
        fFeatures.put("format-canonical",new Boolean(false));
        fFeatures.put("format-pretty-print",new Boolean(false));
    }

    private void checkAllFeatures() {
        if (getFeature("whitespace-in-element-content"))
            _format.setPreserveSpace(true);
        else
            _format.setPreserveSpace(false);
    }

    /**
     * Set the state of a feature.
     * <br>The feature name has the same form as a DOM hasFeature string.
     * <br>It is possible for a <code>DOMWriter</code> to recognize a feature 
     * name but to be unable to set its value.
     * @param name The feature name.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_SUPPORTED_ERR exception when the <code>DOMWriter</code> 
     *   recognizes the feature name but cannot set the requested value. 
     *   <br>Raise a NOT_FOUND_ERR When the <code>DOMWriter</code> does not 
     *   recognize the feature name.
     */
    public void setFeature(String name, 
                           boolean state)
    throws DOMException {
        if (name != null && fFeatures.containsKey(name))
            if (canSetFeature(name,state))
                fFeatures.put(name,new Boolean(state));
            else
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,"Feature "+name+" cannot be set as "+state);
        else
            throw new DOMException(DOMException.NOT_FOUND_ERR,"Feature "+name+" not found");
    }

    /**
     * Query whether setting a feature to a specific value is supported.
     * <br>The feature name has the same form as a DOM hasFeature string.
     * @param name The feature name, which is a DOM has-feature style string.
     * @param state The requested state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @return <code>true</code> if the feature could be successfully set to 
     *   the specified value, or <code>false</code> if the feature is not 
     *   recognized or the requested value is not supported. The value of 
     *   the feature itself is not changed.
     */
    public boolean canSetFeature(String name, boolean state) {
        if (name.equals("normalize-characters") && state)
            return false;
        else if (name.equals("validation") && state)
            return false;
        else if (name.equals("whitespace-in-element-content") && !state)
            return false;
        else if (name.equals("format-canonical") && state)
            return false;
        else if (name.equals("format-pretty-print") && state)
            return false;
        else
            return true;
    }   

    /**
     * Look up the value of a feature.
     * <br>The feature name has the same form as a DOM hasFeature string
     * @param name The feature name, which is a string with DOM has-feature 
     *   syntax.
     * @return The current state of the feature (<code>true</code> or 
     *   <code>false</code>).
     * @exception DOMException
     *   Raise a NOT_FOUND_ERR When the <code>DOMWriter</code> does not 
     *   recognize the feature name.
     */
    public boolean getFeature(String name)
    throws DOMException {
        Boolean state = (Boolean)fFeatures.get(name);
        if (state == null)
            throw new DOMException(DOMException.NOT_FOUND_ERR,"Feature "+name+" not found");
        return state.booleanValue();
    }

    /**
     *  The character encoding in which the output will be written. 
     * <br> The encoding to use when writing is determined as follows: If the 
     * encoding attribute has been set, that value will be used.If the 
     * encoding attribute is <code>null</code> or empty, but the item to be 
     * written includes an encoding declaration, that value will be used.If 
     * neither of the above provides an encoding name, a default encoding of 
     * "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public String getEncoding() {
        return fEncoding;
    }

    /**
     *  The character encoding in which the output will be written. 
     * <br> The encoding to use when writing is determined as follows: If the 
     * encoding attribute has been set, that value will be used.If the 
     * encoding attribute is <code>null</code> or empty, but the item to be 
     * written includes an encoding declaration, that value will be used.If 
     * neither of the above provides an encoding name, a default encoding of 
     * "UTF-8" will be used.
     * <br>The default value is <code>null</code>.
     */
    public void setEncoding(String encoding) {
        _format.setEncoding(encoding);
        fEncoding = _format.getEncoding();
    }

    /**
     *  The actual character encoding that was last used by this formatter. 
     * This convenience method allows the encoding that was used when 
     * serializing a document to be directly obtained. 
     */
    public String getLastEncoding() {
        return fLastEncoding;
    }

    /**
     *  The end-of-line sequence of characters to be used in the XML being 
     * written out. The only permitted values are these: 
     * <dl>
     * <dt><code>null</code></dt>
     * <dd> 
     * Use a default end-of-line sequence. DOM implementations should choose 
     * the default to match the usual convention for text files in the 
     * environment being used. Implementations must choose a default 
     * sequence that matches one of those allowed by  2.11 "End-of-Line 
     * Handling". </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The 
     * carriage-return and line-feed characters (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed 
     * character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public String getNewLine() {
        return _format.getLineSeparator();
    }

    /**
     *  The end-of-line sequence of characters to be used in the XML being 
     * written out. The only permitted values are these: 
     * <dl>
     * <dt><code>null</code></dt>
     * <dd> 
     * Use a default end-of-line sequence. DOM implementations should choose 
     * the default to match the usual convention for text files in the 
     * environment being used. Implementations must choose a default 
     * sequence that matches one of those allowed by  2.11 "End-of-Line 
     * Handling". </dd>
     * <dt>CR</dt>
     * <dd>The carriage-return character (#xD).</dd>
     * <dt>CR-LF</dt>
     * <dd> The 
     * carriage-return and line-feed characters (#xD #xA). </dd>
     * <dt>LF</dt>
     * <dd> The line-feed 
     * character (#xA). </dd>
     * </dl>
     * <br>The default value for this attribute is <code>null</code>.
     */
    public void setNewLine(String newLine) {
        _format.setLineSeparator(newLine);
    }

    /**
     *  The error handler that will receive error notifications during 
     * serialization. The node where the error occured is passed to this 
     * error handler, any modification to nodes from within an error 
     * callback should be avoided since this will result in undefined, 
     * implementation dependent behavior. 
     */
    public DOMErrorHandler getErrorHandler() {
        return fDOMErrorHandler;
    }

    /**
     *  The error handler that will receive error notifications during 
     * serialization. The node where the error occured is passed to this 
     * error handler, any modification to nodes from within an error 
     * callback should be avoided since this will result in undefined, 
     * implementation dependent behavior. 
     */
    public void setErrorHandler(DOMErrorHandler errorHandler) {
        fDOMErrorHandler = errorHandler;
    }

    /**
     * Write out the specified node as described above in the description of 
     * <code>DOMWriter</code>. Writing a Document or Entity node produces a 
     * serialized form that is well formed XML. Writing other node types 
     * produces a fragment of text in a form that is not fully defined by 
     * this document, but that should be useful to a human for debugging or 
     * diagnostic purposes. 
     * @param destination The destination for the data to be written.
     * @param wnode The <code>Document</code> or <code>Entity</code> node to 
     *   be written. For other node types, something sensible should be 
     *   written, but the exact serialized form is not specified.
     * @return  Returns <code>true</code> if <code>node</code> was 
     *   successfully serialized and <code>false</code> in case a failure 
     *   occured and the failure wasn't canceled by the error handler. 
     * @exception DOMSystemException
     *   This exception will be raised in response to any sort of IO or system 
     *   error that occurs while writing to the destination. It may wrap an 
     *   underlying system exception.
     */
    public boolean writeNode(java.io.OutputStream destination, 
                             Node wnode)
    throws Exception {
        checkAllFeatures();
        try {
            setOutputByteStream(destination);
            if (wnode == null)
                return false;
            else if (wnode.getNodeType() == Node.DOCUMENT_NODE)
                serialize((Document)wnode);
            else if (wnode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
                serialize((DocumentFragment)wnode);
            else if (wnode.getNodeType() == Node.ELEMENT_NODE)
                serialize((Element)wnode);
            else
                return false;
        }
        catch (NullPointerException npe) {
            throw npe;
        }
        catch (IOException ioe) {
            throw ioe;
        }
        fLastEncoding = getEncoding();
        return true;
    }

    /**
     *  Serialize the specified node as described above in the description of 
     * <code>DOMWriter</code>. The result of serializing the node is 
     * returned as a string. Writing a Document or Entity node produces a 
     * serialized form that is well formed XML. Writing other node types 
     * produces a fragment of text in a form that is not fully defined by 
     * this document, but that should be useful to a human for debugging or 
     * diagnostic purposes. 
     * @param wnode  The node to be written. 
     * @return  Returns the serialized data, or <code>null</code> in case a 
     *   failure occured and the failure wasn't canceled by the error 
     *   handler. 
     * @exception DOMException
     *    DOMSTRING_SIZE_ERR: The resulting string is too long to fit in a 
     *   <code>DOMString</code>. 
     */
    public String writeToString(Node wnode)
    throws DOMException {
        checkAllFeatures();
        StringWriter destination = new StringWriter();
        try {
            setOutputCharStream(destination);
            if (wnode == null)
                return null;
            else if (wnode.getNodeType() == Node.DOCUMENT_NODE)
                serialize((Document)wnode);
            else if (wnode.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)
                serialize((DocumentFragment)wnode);
            else if (wnode.getNodeType() == Node.ELEMENT_NODE)
                serialize((Element)wnode);
            else
                return null;
        }
        catch (IOException ioe) {
            throw new DOMException(DOMException.DOMSTRING_SIZE_ERR,"The resulting string is too long to fit in a DOMString: "+ioe.getMessage());
        }
        fLastEncoding = getEncoding();
        return destination.toString();
    }

    public boolean reset() {
        super.reset();
        fNamespaceSupport.reset(fSymbolTable);
        fNamespaceCounter = 1;
        fXmlSymbol = fSymbolTable.addSymbol("xml");
        fXmlnsSymbol = fSymbolTable.addSymbol("xmlns");
        fEmptySymbol=fSymbolTable.addSymbol("");
        return true;

    }

}



