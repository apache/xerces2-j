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

import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.NamespacesScope;
import org.apache.xerces.utils.StringPool;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
    private static final boolean DEBUG_ALL = false;

    /** Compile to true to debug method callbacks. */
    private static final boolean DEBUG_METHODS = DEBUG_ALL || false;

    /** Compile to true to debug important method callbacks. */
    private static final boolean DEBUG_METHODS2 = DEBUG_ALL || DEBUG_METHODS || 
                                                  false;

    /** Compile to true to debug match. */
    private static final boolean DEBUG_MATCH = DEBUG_ALL || false;

    /** Don't touch this value unless you add more debug constants. */
    private static final boolean DEBUG_ANY = DEBUG_METHODS || 
                                             DEBUG_METHODS2 ||
                                             DEBUG_MATCH;

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
    private IntegerStack fStepIndexes = new IntegerStack();

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
            System.out.println("XPATH["+toString()+"]: <init>()");
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
    protected void matched(String content) throws SAXException {
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: matched \""+content+'"');
        }
    } // matched(String content)

    //
    // XMLDocumentFragmentHandler methods
    //

    /**
     * The start of the document fragment.
     *
     * @param namespaceContext The namespace context in effect at the
     *                         start of this document fragment. This
     *                         object only represents the current context.
     *                         Implementors of this class are responsible
     *                         for copying the namespace bindings from the
     *                         the current context (and its parent contexts)
     *                         if that information is important.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDocumentFragment(StringPool stringPool,
                                      NamespacesScope namespacesScope) 
        throws Exception {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: startDocumentFragment("+
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
            NamespacesScope.NamespacesHandler handler =
                new NamespacesScope.NamespacesHandler() {
                public void startNamespaceDeclScope(int prefix, int uri) throws Exception {
                }
                public void endNamespaceDeclScope(int prefix) throws Exception {
                }
            };
            fNamespacesScope = new NamespacesScope(handler);
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
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: startElement("+
                               "element={"+
                               "prefix="+fStringPool.toString(element.prefix)+','+
                               "localpart="+fStringPool.toString(element.localpart)+','+
                               "rawname="+fStringPool.toString(element.rawname)+','+
                               "uri="+fStringPool.toString(element.uri)+
                               "},"+
                               "attributes="+attributes+
                               ")");
        }

        // return, if not matching
        if (fNoMatchDepth > 0) {
            fNoMatchDepth++;
            return;
        }

        // check match
        int startStepIndex = fCurrentStep;
        while (fCurrentStep < fLocationPath.steps.length) {
            XPath.Step step = fLocationPath.steps[fCurrentStep++];
            if (DEBUG_MATCH) {
                System.out.println("XPATH["+toString()+"]: "+
                                   "attempting match at step["+(fCurrentStep - 1)+"]: \""+step+'"');
            }
            XPath.Axis axis = step.axis;
            switch (axis.type) {
                case axis.SELF: {
                    if (DEBUG_MATCH) {
                        System.out.println("XPATH["+toString()+"]: "+
                                           "axis: SELF");
                    }
                    if (DEBUG_MATCH) {
                        System.out.println("XPATH["+toString()+"]: "+
                                           "STEP MATCHED");
                    }
                    if (fCurrentStep == fLocationPath.steps.length) {
                        if (DEBUG_MATCH) {
                            System.out.println("XPATH["+toString()+"]: "+
                                               "PATH MATCHED");
                        }
                        fMatched = true;
                        fBufferContent = true && fShouldBufferContent;
                        break;
                    }
                    continue;
                }
                case axis.CHILD: {
                    int elementStep = fCurrentStep + 1;
                    if (DEBUG_MATCH) {
                        System.out.println("XPATH["+toString()+"]: "+
                                           "axis: CHILD");
                    }
                    // check element match
                    XPath.NodeTest nodeTest = step.nodeTest;
                    if (nodeTest.type == nodeTest.QNAME) {
                        if (DEBUG_MATCH) {
                            System.out.println("XPATH["+toString()+"]: "+
                                               "nodeTest: QNAME");
                        }
                        boolean matched = true;
                        QName name = nodeTest.name;
                        if (name.uri == -1) {
                            if (element.rawname != name.rawname) {
                                //System.out.println(">>> fStringPool:     "+fStringPool);
                                //System.out.println(">>> element.rawname: "+fStringPool.toString(element.rawname));
                                //System.out.println(">>> name.rawname:    "+fStringPool.toString(name.rawname));
                                matched = false;
                            }
                        }
                        else {
                            if (element.uri != name.uri ||
                                element.localpart != name.localpart) {
                                matched = false;
                            }
                        }
                        if (!matched) {
                            if (DEBUG_MATCH) {
                                System.out.println("XPATH["+toString()+"]: "+
                                                   "STEP *NOT* MATCHED");
                            }
                            fNoMatchDepth++;
                            return;
                        }
                        if (DEBUG_MATCH) {
                            System.out.println("XPATH["+toString()+"]: "+
                                               "STEP MATCHED");
                        }
                        if (fCurrentStep == fLocationPath.steps.length) {
                            if (DEBUG_MATCH) {
                                System.out.println("XPATH["+toString()+"]: "+
                                                   "PATH MATCHED");
                            }
                            fMatched = true;
                            fBufferContent = true && fShouldBufferContent;
                        }
                    }
                    /***
                    // REVISIT: [Q] Is self:: axis needed? -Ac
                    else if (axis.type == XPath.Axis.SELF) {
                        // let pass
                    }
                    /***/
                    else {
                        throw new SAXException("axis \""+axis+"\" not allowed");
                    }

                    // check for attribute match
                    if (fCurrentStep < fLocationPath.steps.length) {
                        step = fLocationPath.steps[fCurrentStep];
                        axis = step.axis;
                        if (axis.type == axis.ATTRIBUTE) {
                            fCurrentStep++;
                            nodeTest = step.nodeTest;
                            if (nodeTest.type == nodeTest.QNAME) {
                                boolean matched = true;
                                QName name = nodeTest.name;
                                if (name.uri == -1) {
                                    fMatchedString = attributes.getValue(name.rawname);
                                    if (fMatchedString == null) {
                                        matched = false;
                                    }
                                }
                                else {
                                    int aindex = attributes.getFirstAttr(handle);
                                    while (aindex != -1) {
                                        int auri = attributes.getAttrURI(aindex);
                                        int alocalpart = attributes.getAttrLocalpart(aindex);
                                        if (auri != -1 && alocalpart != -1 &&
                                            auri == name.uri && alocalpart == name.localpart) {
                                            fMatchedString = fStringPool.toString(attributes.getAttValue(aindex));
                                            break;
                                        }
                                        aindex = attributes.getNextAttr(aindex);
                                    }
                                    if (fMatchedString == null) {
                                        matched = false;
                                    }
                                }
                                if (!matched) {
                                    if (DEBUG_MATCH) {
                                        System.out.println("XPATH["+toString()+"]: "+
                                                           "ATTRIBUTE *NOT* MATCHED");
                                    }
                                    fNoMatchDepth++;
                                    return;
                                }
                                if (DEBUG_MATCH) {
                                    System.out.println("XPATH["+toString()+"]: "+
                                                       "STEP MATCHED");
                                }
                                if (fCurrentStep == fLocationPath.steps.length) {
                                    if (DEBUG_MATCH) {
                                        System.out.println("XPATH["+toString()+"]: "+
                                                           "PATH MATCHED");
                                    }
                                }
                                fBufferContent = false;
                                fCurrentStep++;
                                fMatched = fMatchedString != null;
                                matched(fMatchedString);
                            }
                            else {
                                throw new SAXException("node test \""+nodeTest+"\" not allowed");
                            }
                        }
                    }
                    break;
                }
                default: {
                    throw new SAXException("step \""+step+"\" not allowed");
                }
            }
            break;
        }

        // push context
        fNamespacesScope.increaseDepth();
        fStepIndexes.push(startStepIndex);

    } // startElement(QName,XMLAttrList,int)

    /** Character content. */
    public void characters(char[] ch, int offset, int length) 
        throws Exception {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: characters("+
                               "text="+new String(ch, offset, length)+
                               ")");
        }

        // collect match content
        if (fBufferContent && fNoMatchDepth == 0) {
            if (!DEBUG_METHODS && DEBUG_METHODS2) {
                System.out.println("XPATH["+toString()+"]: characters("+
                                   "text="+new String(ch, offset, length)+
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
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: endElement("+
                               "element={"+
                               "prefix="+fStringPool.toString(element.prefix)+','+
                               "localpart="+fStringPool.toString(element.localpart)+','+
                               "rawname="+fStringPool.toString(element.rawname)+','+
                               "uri="+fStringPool.toString(element.uri)+
                               "})");
        }
        
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

        // go back a step
        fCurrentStep = fStepIndexes.pop();
        clear();

    } // endElement(QName)

    /**
     * The end of the document fragment.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDocumentFragment() throws Exception {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: endDocumentFragment()");
        }
        clear();
    } // endDocumentFragment()

    //
    // Object methods
    //
    
    /** Returns a string representation of this object. */
    public String toString() {
        return fLocationPath.toString();
    } // toString():String

    //
    // Private methods
    //

    /** Clears the match values. */
    private void clear() {
        fBufferContent = false;
        fMatched = false;
        fMatchedString = null;
    } // clear()

    //
    // Classes
    //

    /**
     * A simple integer stack. 
     *
     * @author Andy Clark, IBM
     */
    protected final static class IntegerStack {

        //
        // Data
        //

        /** Stack top. */
        private int fTop = -1;

        /** Stack data. */
        private int[] fData = new int[4];

        //
        // Constructors
        //

        /** Default constructor. */
        public IntegerStack() {
        } // <init>()

        //
        // Public methods
        //

        /** Clears the stack. */
        public void clear() {
            fTop = -1;
        } // clear()

        /** Pushes an integer onto the stack. */
        public void push(int value) {
            ensureCapacity(++fTop);
            fData[fTop] = value;
        } // push(int)

        /** Pops an integer off of the stack. */
        public int pop() {
            return fData[fTop--];
        } // pop():int

        //
        // Private methods
        //

        /** Ensures data structure can hold data. */
        private void ensureCapacity(int size) {
            if (size >= fData.length) {
                int[] array = new int[fData.length * 2];
                System.arraycopy(fData, 0, array, 0, fData.length);
                fData = array;
            }
        } // ensureCapacity(int)

    } // class IntegerStack

    //
    // MAIN
    //

    /** Main program. */
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
                        matcher.startDocumentFragment(matcher.fStringPool, null);
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

} // class XPathMatcher
