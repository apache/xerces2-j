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

package org.apache.xerces.validators.schema.identity;

import org.apache.xerces.framework.XMLAttrList;

import org.apache.xerces.utils.IntStack;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.NamespacesScope;
import org.apache.xerces.utils.StringPool;

/***
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
/***/

/**
 * XPath matcher.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class XPathMatcher {

    //
    // Constants
    //

    // debugging

    /** Compile to true to debug everything. */
    protected static final boolean DEBUG_ALL = false;

    /** Compile to true to debug method callbacks. */
    protected static final boolean DEBUG_METHODS = false || DEBUG_ALL;

    /** Compile to true to debug important method callbacks. */
    protected static final boolean DEBUG_METHODS2 = false || DEBUG_METHODS || DEBUG_ALL;

    /** Compile to true to debug the <em>really</em> important methods. */
    protected static final boolean DEBUG_METHODS3 = false || DEBUG_METHODS || DEBUG_ALL;
                                                      
    /** Compile to true to debug match. */
    protected static final boolean DEBUG_MATCH = false || DEBUG_ALL;

    /** Compile to true to debug step index stack. */
    protected static final boolean DEBUG_STACK = false || DEBUG_ALL;

    /** Don't touch this value unless you add more debug constants. */
    protected static final boolean DEBUG_ANY = DEBUG_METHODS || 
                                               DEBUG_METHODS2 ||
                                               DEBUG_METHODS3 ||
                                               DEBUG_MATCH ||
                                               DEBUG_STACK;

    //
    // Data
    //

    /** XPath location path. */
    private XPath.LocationPath fLocationPath;

    /** Application preference to buffer content or not. */
    private boolean fShouldBufferContent;

    /** True if should buffer character content <em>at this time</em>. */
    private boolean fBufferContent;

    /** Buffer to hold match text. */
    private StringBuffer fMatchedBuffer = new StringBuffer();

    /** True if XPath has been matched. */
    private boolean fMatched;

    /** The matching string. */
    private String fMatchedString;

    /** Integer stack of step indexes. */
    private IntStack fStepIndexes = new IntStack();

    /** Current step. */
    private int fCurrentStep;

    /** 
     * No match depth. The value of this field will be zero while
     * matching is successful.
     */
    private int fNoMatchDepth;

    // Xerces 1.x framework

    /** String pool. */
    protected StringPool fStringPool;

    /** Namespace scope. */
    protected NamespacesScope fNamespacesScope;

    //
    // Constructors
    //

    /** 
     * Constructs an XPath matcher that implements a document fragment 
     * handler. 
     *
     * @param xpath   The xpath.
     * @param symbols The symbol table.
     */
    public XPathMatcher(XPath xpath) {
        this(xpath, false);
    } // <init>(Stringm,SymbolTable,NamespaceContext)

    /** 
     * Constructs an XPath matcher that implements a document fragment 
     * handler. 
     *
     * @param xpath   The xpath.
     * @param symbols The symbol table.
     * @param shouldBufferContent True if the matcher should buffer the
     *                            matched content.
     */
    public XPathMatcher(XPath xpath, boolean shouldBufferContent) {
        fLocationPath = xpath.getLocationPath();
        fShouldBufferContent = shouldBufferContent;
        if (DEBUG_METHODS) {
            System.out.println(toString()+"#<init>()");
        }
    } // <init>(String,SymbolTable,NamespaceContext,boolean)

    //
    // Public methods
    //

    /** Returns true if XPath has been matched. */
    public boolean isMatched() {
        return fMatched;
    } // isMatched():boolean

    /** Returns the matched string. */
    public String getMatchedString() {
        return fMatchedString;
    } // getMatchedString():String

    //
    // Protected methods
    //

    /**
     * This method is called when the XPath handler matches the
     * XPath expression. Subclasses can override this method to
     * provide default handling upon a match.
     */
    protected void matched(String content) throws Exception {
        if (DEBUG_METHODS3) {
            System.out.println(toString()+"#matched(\""+normalize(content)+"\")");
        }
    } // matched(String content)

    //
    // XMLDocumentFragmentHandler methods
    //

    /**
     * The start of the document fragment.
     *
     * @param namespaceScope The namespace scope in effect at the
     *                       start of this document fragment.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDocumentFragment(StringPool stringPool,
                                      NamespacesScope namespacesScope) 
        throws Exception {
        if (DEBUG_METHODS) {
            System.out.println(toString()+"#startDocumentFragment("+
                               "stringPool="+stringPool+','+
                               "namespacesScope="+namespacesScope+
                               ")");
        }

        // reset state
        clear();
        fMatchedBuffer.setLength(0);
        fStepIndexes.clear();
        fCurrentStep = 0;
        fNoMatchDepth = 0;

        // keep values
        fStringPool = stringPool;
        fNamespacesScope = namespacesScope;
        if (namespacesScope == null) {
            fNamespacesScope = new NamespacesScope();
        }

    } // startDocumentFragment(StringPool,NamespacesScope)

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttrList attributes, int handle)
        throws Exception {
        if (DEBUG_METHODS2) {
            System.out.println(toString()+"#startElement("+
                               "element={"+
                               "prefix="+fStringPool.toString(element.prefix)+','+
                               "localpart="+fStringPool.toString(element.localpart)+','+
                               "rawname="+fStringPool.toString(element.rawname)+','+
                               "uri="+fStringPool.toString(element.uri)+
                               "},"+
                               "attributes=..."+//attributes+
                               ")");
        }

        // push context
        int startStep = fCurrentStep;
        fStepIndexes.push(startStep);
        if (DEBUG_STACK) {
            System.out.println(toString()+": "+fStepIndexes);
        }
        //fNamespacesScope.increaseDepth();

        // return, if not matching
        if (fMatched || fNoMatchDepth > 0) {
            fNoMatchDepth++;
            return;
        }

        // consume self::node() steps
        XPath.Step[] steps = fLocationPath.steps;
        while (fCurrentStep < steps.length && 
               steps[fCurrentStep].axis.type == XPath.Axis.SELF) {
            if (DEBUG_MATCH) {
                XPath.Step step = steps[fCurrentStep];
                System.out.println(toString()+" [SELF] MATCHED!");
            }
            fCurrentStep++;
        }
        if (fCurrentStep == steps.length) {
            if (DEBUG_MATCH) {
                System.out.println(toString()+" XPath MATCHED!");
            }
            fMatched = true;
            fBufferContent = true && fShouldBufferContent;
        }
        
        // match child::... step, if haven't consumed any self::node()
        if (fCurrentStep == startStep &&
            steps[fCurrentStep].axis.type == XPath.Axis.CHILD) {
            XPath.Step step = steps[fCurrentStep];
            XPath.NodeTest nodeTest = step.nodeTest;
            if (DEBUG_MATCH) {
                System.out.println(toString()+" [CHILD] before");
            }
            if (nodeTest.type == XPath.NodeTest.QNAME) {
                if (!nodeTest.name.equals(element)) {
                    fNoMatchDepth++;
                    if (DEBUG_MATCH) {
                        System.out.println(toString()+" [CHILD] after NO MATCH");
                    }
                    return;
                }
            }
            fCurrentStep++;
            if (DEBUG_MATCH) {
                System.out.println(toString()+" [CHILD] after MATCHED!");
            }
        }
        if (fCurrentStep == steps.length) {
            fMatched = true;
            fBufferContent = true && fShouldBufferContent;
        }

        // match attribute::... step
        if (fCurrentStep < steps.length &&
            steps[fCurrentStep].axis.type == XPath.Axis.ATTRIBUTE) {
            if (DEBUG_MATCH) {
                System.out.println(toString()+" [ATTRIBUTE] before");
            }
            int aindex = attributes.getFirstAttr(handle);
            if (aindex != -1) {
                XPath.NodeTest nodeTest = steps[fCurrentStep].nodeTest;
                QName aname = new QName(); // REVISIT: cache this
                while (aindex != -1) {
                    int aprefix = attributes.getAttrPrefix(aindex);
                    int alocalpart = attributes.getAttrLocalpart(aindex);
                    int arawname = attributes.getAttrName(aindex);
                    int auri = attributes.getAttrURI(aindex);
                    aname.setValues(aprefix, alocalpart, arawname, auri);
                    if (nodeTest.type != XPath.NodeTest.QNAME ||
                        nodeTest.name.equals(aname)) {
                        fCurrentStep++;
                        if (fCurrentStep == steps.length) {
                            fMatched = true;
                            int avalue = attributes.getAttValue(aindex);
                            fMatchedString = fStringPool.toString(avalue);
                            matched(fMatchedString);
                        }
                        break;
                    }
                    aindex = attributes.getNextAttr(aindex);
                }
            }
            if (!fMatched) {
                fNoMatchDepth++;
                if (DEBUG_MATCH) {
                    System.out.println(toString()+" [ATTRIBUTE] after");
                }
                return;
            }
            if (DEBUG_MATCH) {
                System.out.println(toString()+" [ATTRIBUTE] after MATCHED!");
            }
        }

    } // startElement(QName,XMLAttrList,int)

    /** Character content. */
    public void characters(char[] ch, int offset, int length) 
        throws Exception {
        if (DEBUG_METHODS) {
            System.out.println(toString()+"#characters("+
                               "text="+normalize(new String(ch, offset, length))+
                               ")");
        }

        // collect match content
        if (fBufferContent && fNoMatchDepth == 0) {
            if (!DEBUG_METHODS && DEBUG_METHODS2) {
                System.out.println(toString()+"#characters("+
                                   "text="+normalize(new String(ch, offset, length))+
                                   ")");
            }
            fMatchedBuffer.append(ch, offset, length);
        }
        
    } // characters(char[],int,int)

    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws Exception {
        if (DEBUG_METHODS2) {
            System.out.println(toString()+"#endElement("+
                               "element={"+
                               "prefix="+fStringPool.toString(element.prefix)+','+
                               "localpart="+fStringPool.toString(element.localpart)+','+
                               "rawname="+fStringPool.toString(element.rawname)+','+
                               "uri="+fStringPool.toString(element.uri)+
                               "})");
        }
        
        // go back a step
        fCurrentStep = fStepIndexes.pop();
        
        // return, if not matching
        if (fNoMatchDepth > 0) {
            fNoMatchDepth--;
            return;
        }

        // signal match, if appropriate
        if (fBufferContent) {
            fBufferContent = false;
            fMatchedString = fMatchedBuffer.toString();
            matched(fMatchedString);
        }
        clear();

        if (DEBUG_STACK) {
            System.out.println(toString()+": "+fStepIndexes);
        }

    } // endElement(QName)

    /**
     * The end of the document fragment.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDocumentFragment() throws Exception {
        if (DEBUG_METHODS) {
            System.out.println(toString()+"#endDocumentFragment()");
        }
        clear();
    } // endDocumentFragment()

    //
    // Object methods
    //
    
    /** Returns a string representation of this object. */
    public String toString() {
        /***
        return fLocationPath.toString();
        /***/
        StringBuffer str = new StringBuffer();
        String s = super.toString();
        int index2 = s.lastIndexOf('.');
        if (index2 != -1) {
            s = s.substring(index2 + 1);
        }
        str.append(s);
        str.append('[');
        XPath.Step[] steps = fLocationPath.steps;
        for (int i = 0; i < steps.length; i++) {
            if (i == fCurrentStep) {
                str.append('^');
            }
            str.append(steps[i].toString());
            if (i < steps.length - 1) {
                str.append('/');
            }
        }
        if (fCurrentStep == steps.length) {
            str.append('^');
        }
        str.append(']');
        return str.toString();
        /***/
    } // toString():String

    //
    // Private methods
    //

    /** Clears the match values. */
    private void clear() {
        fBufferContent = false;
        fMatchedBuffer.setLength(0);
        fMatched = false;
        fMatchedString = null;
    } // clear()

    /** Normalizes text. */
    private String normalize(String s) {
        StringBuffer str = new StringBuffer();
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': {
                    str.append("\\n");
                    break;
                }
                default: {
                    str.append(c);
                }
            }
        }
        return str.toString();
    } // normalize(String):String

    //
    // MAIN
    //

    // NOTE: The main of this class is here for debugging purposes.
    //       However, javac (JDK 1.1.8) has an internal compiler
    //       error when compiling. Jikes has no problem, though.
    //
    //       If you want to use this main, use Jikes to compile but
    //       *never* check in this code to CVS without commenting it
    //       out. -Ac
    
    /** Main program. */
    /***
    public static void main(String[] argv) throws Exception {

        if (DEBUG_ANY) {
            for (int i = 0; i < argv.length; i++) {
                final String expr = argv[i];
                final StringPool symbols = new StringPool();
                final XPath xpath = new XPath(expr, symbols, null);
                final XPathMatcher matcher = new XPathMatcher(xpath, true);
                org.apache.xerces.parsers.SAXParser parser = 
                    new org.apache.xerces.parsers.SAXParser(symbols) {
                    public void startDocument() throws Exception {
                        matcher.startDocumentFragment(symbols, null);
                    }
                    public void startElement(QName element, XMLAttrList attributes, int handle) throws Exception {
                        matcher.startElement(element, attributes, handle);
                    }
                    public void characters(char[] ch, int offset, int length) throws Exception {
                        matcher.characters(ch, offset, length);
                    }
                    public void endElement(QName element) throws Exception {
                        matcher.endElement(element);
                    }
                    public void endDocument() throws Exception {
                        matcher.endDocumentFragment();
                    }
                };
                System.out.println("#### argv["+i+"]: \""+expr+"\" -> \""+xpath.toString()+'"');
                final String uri = argv[++i];
                System.out.println("#### argv["+i+"]: "+uri);
                parser.parse(uri);
            }
        }

    } // main(String[])
    /***/

} // class XPathMatcher
