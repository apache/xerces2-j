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

package org.apache.xerces.impl.xpath;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;

import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentFragmentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;

/**
 * XPath matcher.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class XPathMatcher
    implements XMLComponent, XMLDocumentFragmentHandler {

    //
    // Constants
    //

    // private

    private static final String SYMBOL_TABLE = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    // debugging

    /** Compile to true to debug everything. */
    private static final boolean DEBUG_ALL = false;

    /** Compile to true to debug method callbacks. */
    private static final boolean DEBUG_METHODS = DEBUG_ALL || false;

    /** Compile to true to debug important method callbacks. */
    private static final boolean DEBUG_METHODS2 = DEBUG_ALL || DEBUG_METHODS || true;

    /** Compile to true to debug match. */
    private static final boolean DEBUG_MATCH = DEBUG_ALL || true;

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

    /** Namespace context. */
    private NamespaceSupport fNamespaceContext = new NamespaceSupport();

    /** 
     * No match depth. The value of this field will be zero while
     * matching is successful.
     */
    private int fNoMatchDepth;

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
    protected void matched(String content) throws XNIException {
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: matched \""+content+'"');
        }
    } // matched(String content)

    //
    // XMLComponent methods
    //

    /**
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     * 
     * @param componentManager The component manager.
     *
     * @throws SAXException Thrown by component on initialization error.
     *                      For example, if a feature or property is
     *                      required for the operation of the component, the
     *                      component manager may throw a 
     *                      SAXNotRecognizedException or a
     *                      SAXNotSupportedException.
     */
    public void reset(XMLComponentManager componentManager) 
        throws XNIException {
        
        // get symbol table
        SymbolTable symbols = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        
        // symbolizes the strings
        XPath.Step[] steps = fLocationPath.steps;
        for (int i = 0; i < steps.length; i++) {
            QName name = steps[i].nodeTest.name;
            if (name.prefix != null) {
                name.prefix = symbols.addSymbol(name.prefix);
            }
            if (name.localpart != null) {
                name.localpart = symbols.addSymbol(name.localpart);
            }
            if (name.rawname != null) {
                name.rawname = symbols.addSymbol(name.rawname);
            }
            if (name.uri != null) {
                name.uri = symbols.addSymbol(name.uri);
            }
        }

        // reset namespace support
        fNamespaceContext.reset(symbols);

    } // reset(XMLComponentManager)

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return null;
    }

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state. 
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     * 
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return null;
    }

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value. 
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     * 
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
    } // setProperty(String,Object)

    //
    // XMLDocumentFragmentHandler methods
    //

    /**
     * The start of the document fragment.
     *
     * @param baseSystemId     The base system identifier for this
     *                         fragment.
     * @param namespaceContext The namespace context in effect at the
     *                         start of this document fragment. This
     *                         object only represents the current context.
     *                         Implementors of this class are responsible
     *                         for copying the namespace bindings from the
     *                         the current context (and its parent contexts)
     *                         if that information is important.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startDocumentFragment(XMLLocator locator,
                                      NamespaceContext namespaceContext) 
        throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: startDocumentFragment("+
                               "namespaceContext="+namespaceContext+
                               ")");
        }

        // reset state
        clear();
        fMatchedBuffer.setLength(0);
        fStepIndexes.clear();
        fCurrentStep = 0;
        fNoMatchDepth = 0;

        // setup namespace context
        while (namespaceContext != null) {
            int count = namespaceContext.getDeclaredPrefixCount();
            for (int i = 0; i < count; i++) {
                String prefix = namespaceContext.getDeclaredPrefixAt(i);
                if (fNamespaceContext.getURI(prefix) == null) {
                    String uri = namespaceContext.getURI(prefix);
                    if (DEBUG_METHODS) {
                        System.out.println("XPATH["+toString()+"]: declaring prefix \""+prefix+"\" -> \""+uri+'"');
                    }
                    fNamespaceContext.declarePrefix(prefix, uri);
                }
            }
            namespaceContext = namespaceContext.getParentContext();
        }

    } // startDocumentFragment(XMLLocator,NamespaceContext)

    /**
     * This method notifies the start of an entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param baseSystemId The base system identifier of the entity if
     *                     the entity is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startEntity(String name, 
                            String publicId, String systemId,
                            String baseSystemId,
                            String encoding) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: startEntity("+
                               "name="+name+','+
                               "publicId="+publicId+','+
                               "systemId="+systemId+','+
                               "encoding="+encoding+
                               ")");
        }
    } // startEntity(String,String,String,String)

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: textDecl("+
                               "version="+version+','+
                               "encoding="+encoding+
                               ")");
        }
    } // textDecl(String,String)

    /**
     * This method notifies the end of an entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name The name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: endEntity("+
                               "name="+name+
                               ")");
        }
    } // endEntity(String)

    /**
     * A comment.
     * 
     * @param text The text in the comment.
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    public void comment(XMLString text) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: comment("+
                               "text="+text+
                               ")");
        }
    } // comment(XMLString)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     * 
     * @param target The target.
     * @param data   The data or null if none specified.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data)
        throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: processingInstruction("+
                               "target="+target+','+
                               "data="+data+
                               ")");
        }
    } // processingInstruction(String,XMLString)

    /**
     * The start of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     * @param uri    The URI bound to the prefix.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startPrefixMapping(String prefix, String uri)
        throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: startPrefixMapping("+
                               "prefix="+prefix+','+
                               "uri="+uri+
                               ")");
        }

        // keep track of prefix mapping
        fNamespaceContext.declarePrefix(prefix, uri);

    } // startPrefixMappin(String,String)

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes)
        throws XNIException {
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: startElement("+
                               "element="+element+','+
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
                case XPath.Axis.SELF: {
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
                case XPath.Axis.CHILD: {
                    int elementStep = fCurrentStep + 1;
                    if (DEBUG_MATCH) {
                        System.out.println("XPATH["+toString()+"]: "+
                                           "axis: CHILD");
                    }
                    // check element match
                    XPath.NodeTest nodeTest = step.nodeTest;
                    if (nodeTest.type == XPath.NodeTest.QNAME) {
                        if (DEBUG_MATCH) {
                            System.out.println("XPATH["+toString()+"]: "+
                                               "nodeTest: QNAME");
                        }
                        boolean matched = true;
                        QName name = nodeTest.name;
                        if (name.uri == null) {
                            if (element.rawname != name.rawname) {
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
                    else if (XPath.Axis.type == XPath.Axis.SELF) {
                        // let pass
                    }
                    /***/
                    else {
                        throw new XNIException("axis \""+axis+"\" not allowed");
                    }

                    // check for attribute match
                    if (fCurrentStep < fLocationPath.steps.length) {
                        step = fLocationPath.steps[fCurrentStep];
                        axis = step.axis;
                        if (axis.type == XPath.Axis.ATTRIBUTE) {
                            fCurrentStep++;
                            nodeTest = step.nodeTest;
                            if (nodeTest.type == XPath.NodeTest.QNAME) {
                                boolean matched = true;
                                QName name = nodeTest.name;
                                if (name.uri == null) {
                                    fMatchedString = attributes.getValue(name.rawname);
                                    if (fMatchedString == null) {
                                        matched = false;
                                    }
                                }
                                else {
                                    fMatchedString = attributes.getValue(name.uri, name.localpart);
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
                                throw new XNIException("node test \""+nodeTest+"\" not allowed");
                            }
                        }
                    }
                    break;
                }
                default: {
                    throw new XNIException("step \""+step+"\" not allowed");
                }
            }
            break;
        }

        // push context
        fNamespaceContext.pushContext();
        fStepIndexes.push(startStepIndex);

    } // startElement(QName,XMLAttributes)

    /**
     * Character content.
     * 
     * @param text The content.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void characters(XMLString text) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: characters("+
                               "text="+text+
                               ")");
        }

        // collect match content
        if (fBufferContent && fNoMatchDepth == 0) {
            if (!DEBUG_METHODS && DEBUG_METHODS2) {
                System.out.println("XPATH["+toString()+"]: characters("+
                                   "text="+text+
                                   ")");
            }
            fMatchedBuffer.append(text.ch, text.offset, text.length);
        }
        
    } // characters(XMLString)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     * 
     * @param text The ignorable whitespace.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: ignorableWhitespace("+
                               "text="+text+
                               ")");
        }
    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws XNIException {
        if (DEBUG_METHODS || DEBUG_METHODS2) {
            System.out.println("XPATH["+toString()+"]: endElement("+
                               "element="+element+
                               ")");
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
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix) throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: endPrefixMapping("+
                               "prefix="+prefix+
                               ")");
        }
    } // endPrefixMapping(String)

    /** 
     * The start of a CDATA section. 
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startCDATA() throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: startCDATA()");
        }
        clear();
    } // startCDATA()

    /**
     * The end of a CDATA section. 
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endCDATA() throws XNIException {
        if (DEBUG_METHODS) {
            System.out.println("XPATH["+toString()+"]: endCDATA()");
        }
    } // endCDATA()

    /**
     * The end of the document fragment.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocumentFragment() throws XNIException {
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
                final SymbolTable symbols = new SymbolTable();
                final String expr = argv[i];
                final XPath xpath = new XPath(expr, null);
                final XPathMatcher matcher = new XPathMatcher(xpath);
                matcher.reset(new XMLComponentManager() {
                    public boolean getFeature(String featureId) throws XMLConfigurationException {
                        short type = XMLConfigurationException.NOT_SUPPORTED;
                        throw new XMLConfigurationException(type, featureId);
                    }
                    public Object getProperty(String propertyId) throws XMLConfigurationException {
                        if (propertyId.equals(SYMBOL_TABLE)) {
                            return symbols;
                        }
                        short type = XMLConfigurationException.NOT_SUPPORTED;
                        throw new XMLConfigurationException(type, propertyId);
                    }
                });
                System.out.println("#### argv["+i+"]: \""+expr+"\" -> \""+xpath.toString()+'"');
                org.apache.xerces.parsers.XMLDocumentParser parser =
                    new org.apache.xerces.parsers.XMLDocumentParser(symbols) {
                    public void startDocument(XMLLocator locator, String encoding) 
                        throws XNIException {
                        matcher.startDocumentFragment(locator, null);
                    }
                    public void startElement(QName element, XMLAttributes attributes) throws XNIException {
                        matcher.startElement(element, attributes);
                    }
                    public void characters(XMLString text) throws XNIException {
                        matcher.characters(text);
                    }
                    public void endElement(QName element) throws XNIException {
                        matcher.endElement(element);
                    }
                    public void endDocument() throws XNIException {
                        matcher.endDocumentFragment();
                    }
                };
                final String uri = argv[++i];
                System.out.println("#### argv["+i+"]: "+uri);
                parser.parse(new org.apache.xerces.xni.parser.XMLInputSource(null, uri, null));
            }
        }

    } // main(String[])

} // class XPathMatcher
