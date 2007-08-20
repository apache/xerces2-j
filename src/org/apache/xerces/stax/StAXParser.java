/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.parsers.XML11Configuration;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.util.XMLSymbols;

/**
 * @author Wei Duan
 * 
 * @version $Id$
 */
public class StAXParser implements XMLStreamReader {

    // The XMLInputFactory instance which creates the StAXParser
    private XMLInputFactory inputFactory;

    // XML configuration for StAXParser
    private XML11Configuration configuration;
    
    // Input XMLInputSource
    private XMLInputSource inputSource;

    // XNI-based internal parser for StAXParser
    private AbstractStAXParser staxParser;

    // The current stax event type
    private int curStAXEventType;

    // The stax namespace context
    private StAXNamespaceContext namespaceContext = null;

    // The stax location
    private StAXLocation location = null;
    
    // Define property string 
    private final String notationProperty = "javax.xml.stream.notations";
    private final String entityProperty = "javax.xml.stream.entities";

    /**
     * The constructor for StAXParser
     * 
     * @param inputSource
     * @param inputFactory
     */
    public StAXParser(XMLInputSource inputSource, XMLInputFactory inputFactory)
            throws XMLStreamException {
        this.inputFactory = inputFactory;
        this.inputSource = inputSource;        
    }
    
    /**
     * Get the XMLConfiguration of current StAXParser.  
     * So the XMLConfiguration can be reused by other parser instance
     * 
     * @return XML11Configuration
     */
    public XML11Configuration GetXMLConfiguration()
    {
        return this.configuration;
    }
    
    /**
     * Set the XMLConfiguration to current StAXParser
     * 
     * @param XML11Configuration
     * @throws XMLStreamException if fail to set XMLConfiguration
     */
    public void InitStAXParser(XML11Configuration config)throws XMLStreamException
    {
        this.configuration = config;
        try {
            this.configuration.setInputSource(this.inputSource);
            this.staxParser = new AbstractStAXParser(configuration);           
            if (inputFactory.getXMLReporter() != null)
            {
                staxParser.setErrorHandler(new StAXErrorHandler(inputFactory.getXMLReporter()));
            }
            
            if (inputFactory.getXMLResolver() != null)
            {
                staxParser.setEntityResolver(new StAXResolver(inputFactory.getXMLResolver()));
            }
        }catch(Exception e)
        {
            throw new XMLStreamException("Fail to create StAXParser instance!", e);
        }
    }

    
    /**
     * Get the value of a feature/property from the underlying implementation
     * 
     * @param name
     *            The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException
     *             if name is null
     */
    public Object getProperty(java.lang.String name)
            throws java.lang.IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException(
                    "The feature name should not be null");
        }

        if (name == notationProperty)
        {
            // TODO : Add notation property support when current event is DTD
        }
        else if (name == entityProperty)
        {
            // TODO : Add notation property support when current event is DTD
        }
        
        return null;
    }

    /**
     * Returns true if there are more parsing events and false if there are no
     * more events. This method will return false if the current state of the
     * XMLStreamReader is END_DOCUMENT
     * 
     * @return true if there are more events, false otherwise
     * @throws XMLStreamException
     *             if there is a fatal error detecting the next state
     */
    public boolean hasNext() throws XMLStreamException {
        boolean hasNext = true;
        if (curStAXEventType == XMLStreamConstants.END_DOCUMENT) {
            hasNext = false;
        }

        return hasNext;
    }

    /**
     * Get next parsing event - a processor may return all contiguous character
     * data in a single chunk, or it may split it into several chunks. If the
     * property javax.xml.stream.isCoalescing is set to true element content
     * must be coalesced and only one CHARACTERS event must be returned for
     * contiguous element content or CDATA Sections. By default entity
     * references must be expanded and reported transparently to the
     * application. An exception will be thrown if an entity reference cannot be
     * expanded. If element content is empty (i.e. content is "") then no
     * CHARACTERS event will be reported.
     * 
     * @return the integer code corresponding to the current parse event
     * @throws java.lang.IllegalStateException -
     *             if this is called when hasNext() returns false
     * @throws XMLStreamException
     *             if there is an error processing the underlying XML source
     */
    public int next() throws XMLStreamException {
        try {
            // Initialize the StAXParser event type to zero
            staxParser.eventType = 0;
            configuration.parse(false);
           
        } catch (Exception e) {
            throw new XMLStreamException(
                    "Error occurs when processing the underlying XML source", e);
        }
        if (!hasNext()) {
            throw new IllegalStateException("No more document to parse");
        }

        curStAXEventType = staxParser.eventType;

        // Initialize the location information
        if (curStAXEventType == XMLStreamConstants.START_DOCUMENT) {
            this.namespaceContext = new StAXNamespaceContext(
                    staxParser.namespaceContext);
            this.location = new StAXLocation(staxParser.locator);
        }
        
        return curStAXEventType;
    }

    /**
     * Test if the current event is of the given type and if the namespace and
     * name match the current namespace and name of the current event. If the
     * namespaceURI is null it is not checked for equality, if the localName is
     * null it is not checked for equality.
     * 
     * @param type
     *            the event type
     * @param namespaceURI
     *            the uri of the event, may be null
     * @param localName
     *            the localName of the event, may be null
     * @throws XMLStreamException
     *             if the required values are not matched.
     */
    public void require(int type, String namespaceURI, String localName)
            throws XMLStreamException {
        if (type == curStAXEventType)
        {
           try{
               if (namespaceURI != null)
               {
                   String curNamespace = this.getNamespaceURI();
                   if (!namespaceURI.equals(curNamespace))
                   {
                       throw new XMLStreamException(
                               "Namespace " + curNamespace + " doesn't match with input "+ namespaceURI);  
                   }
               }
               if (localName != null)
               {
                   String curLocalName = this.getLocalName();
                   if (!localName.equals(curLocalName))
                   {
                       throw new XMLStreamException(
                               "Local name " + curLocalName + " doesn't match with input "+ localName);  
                   }
               }    
           }catch(IllegalStateException e)
           {
               throw new XMLStreamException(e);
           }       
        }
        else{
            throw new XMLStreamException(
               "Event type " + curStAXEventType + " doesn't match with input type "+ type);   
        }
    }

    /**
     * Reads the content of a text-only element, an exception is thrown if this
     * is not a text-only element. Regardless of value of
     * javax.xml.stream.isCoalescing this method always returns coalesced
     * content.
     * 
     * <br />
     * Precondition: the current event is START_ELEMENT. <br />
     * Postcondition: the current event is the corresponding END_ELEMENT.
     * 
     * @throws XMLStreamException
     *             if the current event is not a START_ELEMENT or if a non text
     *             element is encountered
     */
    public String getElementText() throws XMLStreamException {
        if (getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException(
                    "parser must be on START_ELEMENT to read next text",
                    getLocation());
        }
        int eventType = next();
        StringBuffer buf = new StringBuffer();
        while (eventType != XMLStreamConstants.END_ELEMENT) {
            if (eventType == XMLStreamConstants.CHARACTERS
                    || eventType == XMLStreamConstants.CDATA
                    || eventType == XMLStreamConstants.SPACE
                    || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                buf.append(getText());
            } else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT) {
                // skipping
            } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException(
                        "unexpected end of document when reading element text content",
                        getLocation());
            } else if (eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "element text content may not contain START_ELEMENT",
                        getLocation());
            } else {
                throw new XMLStreamException("Unexpected event type "
                        + eventType, getLocation());
            }
            eventType = next();
        }
        return buf.toString();
    }

    /**
     * Skips any white space (isWhiteSpace() returns true), COMMENT, or
     * PROCESSING_INSTRUCTION, until a START_ELEMENT or END_ELEMENT is reached.
     * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION,
     * START_ELEMENT, END_ELEMENT are encountered, an exception is thrown.
     * return eventType;
     * 
     * </pre>
     * 
     * @return the event type of the element read (START_ELEMENT or END_ELEMENT)
     * @throws XMLStreamException
     *             if the current event is not white space,
     *             PROCESSING_INSTRUCTION, START_ELEMENT or END_ELEMENT
     * @throws NoSuchElementException
     *             if this is called when hasNext() returns false
     */
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip
                // //
                // //
                // //
                // whitespace
                || (eventType == XMLStreamConstants.CDATA && isWhiteSpace())
                // skip whitespace
                || eventType == XMLStreamConstants.SPACE
                || eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                || eventType == XMLStreamConstants.COMMENT) {
            eventType = next();
        }
        if (eventType != XMLStreamConstants.START_ELEMENT
                && eventType != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("expected start or end tag",
                    getLocation());
        }
        return eventType;
    }

    /**
     * Frees any resources associated with this Reader. This method does not
     * close the underlying input source.
     * 
     * @throws XMLStreamException
     *             if there are errors freeing associated resources
     */
    public void close() throws XMLStreamException {
        if (curStAXEventType == XMLStreamConstants.END_DOCUMENT) {
            try
            {
                this.inputSource.getByteStream().close();
            }catch(java.io.IOException e)
            {
                throw new XMLStreamException(
                   "There are errors freeing associated resources!", e);                
            }
        }
    }

    /**
     * Return the uri for the given prefix. The uri returned depends on the
     * current state of the processor.
     * 
     * 
     * @param prefix
     *            The prefix to lookup, may not be null
     * @return the uri bound to the given prefix or null if it is not bound
     * @throws IllegalArgumentException
     *             if the prefix is null
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalStateException("The prefix can't be null");
        }
        return namespaceContext.getNamespaceURI(prefix);
    }

    /**
     * Returns true if the cursor points to a start tag (otherwise false)
     * 
     * @return true if the cursor points to a start tag, false otherwise
     */
    public boolean isStartElement() {
        return curStAXEventType == XMLStreamConstants.START_ELEMENT;
    }

    /**
     * Returns true if the cursor points to an end tag (otherwise false)
     * 
     * @return true if the cursor points to an end tag, false otherwise
     */
    public boolean isEndElement() {
        return curStAXEventType == XMLStreamConstants.END_ELEMENT;
    }

    /**
     * Returns true if the cursor points to a character data event
     * 
     * @return true if the cursor points to character data, false otherwise
     */
    public boolean isCharacters() {
        // TODO: Confirm the whitespace belongs to characters
        return curStAXEventType == XMLStreamConstants.CHARACTERS;
    }

    /**
     * Returns true if the cursor points to a character data event that consists
     * of all whitespace
     * 
     * @return true if the cursor points to all whitespace, false otherwise
     */
    public boolean isWhiteSpace() {
        return curStAXEventType == XMLStreamConstants.SPACE;
    }

    /**
     * Returns the normalized attribute value of the attribute with the
     * namespace and localName If the namespaceURI is null the namespace is not
     * checked for equality
     * 
     * @param namespaceURI
     *            the namespace of the attribute
     * @param localName
     *            the local name of the attribute, cannot be null
     * @return returns the value of the attribute , returns null if not found
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeValue(String namespaceURI, String localName) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            if (localName == null) {
                throw new IllegalStateException("Local name can't be null");
            }
            XMLAttributes attrs = staxParser.curElementAttr;
            return attrs.getValue(namespaceURI, localName);
        }

        throw new IllegalStateException(
                "Current stax event type is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the count of attributes on this START_ELEMENT, this method is
     * only valid on a START_ELEMENT or ATTRIBUTE. For SAXSource, there is no
     * explict Attribute event
     * 
     * @return returns the number of attributes
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public int getAttributeCount() {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getLength();
        }

        throw new IllegalStateException(
                "Current event type is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the qname of the attribute at the provided index This method
     * excludes namespace definitions
     * 
     * @param index
     *            the position of the attribute
     * @return the QName of the attribute
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public QName getAttributeName(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            String uri = this.getAttributeNamespace(index);
            String local = this.getAttributeLocalName(index);

            return new QName(uri, local);
        }
        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the namespace of the attribute at the provided index
     * 
     * @param index
     *            the position of the attribute
     * @return the namespace URI (can be null)
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeNamespace(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getURI(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the localName of the attribute at the provided index
     * 
     * @param index
     *            the position of the attribute
     * @return the localName of the attribute
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeLocalName(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getLocalName(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the prefix of this attribute at the provided index
     * 
     * @param index
     *            the position of the attribute
     * @return the prefix of the attribute
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributePrefix(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getPrefix(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the XML type of the attribute at the provided index
     * 
     * @param index
     *            the position of the attribute
     * @return the XML type of the attribute
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeType(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getType(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the value of the attribute at the index
     * 
     * @param index
     *            the position of the attribute
     * @return the attribute value
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeValue(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.getValue(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns a boolean which indicates if this attribute was created by
     * default
     * 
     * @param index
     *            the position of the attribute
     * @return true if this is a default attribute
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or ATTRIBUTE
     */
    public boolean isAttributeSpecified(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT) {
            return staxParser.curElementAttr.isSpecified(index);
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or ATTRIBUTE");
    }

    /**
     * Returns the count of namespaces declared on this START_ELEMENT or
     * END_ELEMENT.
     * 
     * @return returns the number of namespace declarations on this specific
     *         element
     * @throws IllegalStateException
     *             if this is not a START_ELEMEN or, END_ELEMENT
     */
    public int getNamespaceCount() {
        int countNamespace = 0;
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            XMLAttributes attrs = staxParser.curElementAttr;

            for (int i = 0; i < attrs.getLength(); i++) {
                if (attrs.getLocalName(i) == XMLSymbols.PREFIX_XMLNS) {
                    // e.g. xmlns="http://www.w3.org/2001/XMLSchema"
                    countNamespace++;
                } else if (attrs.getPrefix(i) == XMLSymbols.PREFIX_XMLNS) {
                    // e.g. xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/wsdl"
                    countNamespace++;
                }
            }
            return countNamespace;
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or END_ELEMENT");
    }

    /**
     * Returns the prefix for the namespace declared at the index. Returns null
     * if this is the default namespace declaration
     * 
     * @param index
     *            the position of the namespace declaration
     * @return returns the namespace prefix
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE
     */
    public String getNamespacePrefix(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            
            int namespaceCount = getNamespaceCount();      
            if (index + 1 > namespaceCount || index < 0)
            {
                throw new IndexOutOfBoundsException("Illegle of namespace index");
            }
     
            XMLAttributes attrs = staxParser.curElementAttr;
            
            String prefix = null;
            int count = 0;
            for (int i = 0; i < attrs.getLength() && count <= index ; i++) {
                if (attrs.getLocalName(i) == XMLSymbols.PREFIX_XMLNS) {
                    // default namesapce 
                    count++;
                    prefix = null;
                } else if (attrs.getPrefix(i) == XMLSymbols.PREFIX_XMLNS) {
                    count++;
                    prefix = attrs.getLocalName(i);
                }
            }
            
            return prefix;
        }
        throw new IllegalStateException(
                "Current state is not START_ELEMENT or END_ELEMENT");
    }

    /**
     * Returns the uri for the namespace declared at the index.
     * 
     * @param index
     *            the position of the namespace declaration
     * @return returns the namespace uri
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT, END_ELEMENT or NAMESPACE
     */
    public String getNamespaceURI(int index) {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            int namespaceCount = getNamespaceCount();      
            if (index + 1 > namespaceCount || index < 0)
            {
                throw new IndexOutOfBoundsException("Illegle of namespace index");
            }
     
            XMLAttributes attrs = staxParser.curElementAttr;
            
            String uri = null;
            int count = 0;
            for (int i = 0; i < attrs.getLength() && count <= index ; i++) {
                if (attrs.getLocalName(i) == XMLSymbols.PREFIX_XMLNS) {
                    // default namesapce 
                    count++;
                    uri = null;
                } else if (attrs.getPrefix(i) == XMLSymbols.PREFIX_XMLNS) {
                    count++;
                    uri = attrs.getValue(i);
                }
            }
            
            return uri;
        }

        throw new IllegalStateException(
                "Current state is not START_ELEMENT or END_ELEMENT");
    }

    /**
     * Returns a read only namespace context for the current position. The
     * context is transient and only valid until a call to next() changes the
     * state of the reader.
     * 
     * @return return a namespace context
     */
    public NamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    /**
     * Returns an integer code that indicates the type of the event the cursor
     * is pointing to.
     * 
     * @return the current event type
     */
    public int getEventType() {
        return curStAXEventType;
    }

    /**
     * Returns the current value of the parse event as a string, this returns
     * the string value of a CHARACTERS event, returns the value of a COMMENT,
     * the replacement value for an ENTITY_REFERENCE, the string value of a
     * CDATA section, the string value for a SPACE event, or the String value of
     * the internal subset of the DTD. If an ENTITY_REFERENCE has been resolved,
     * any character data will be reported as CHARACTERS events.
     * 
     * 
     * @return the current text or null
     * @throws java.lang.IllegalStateException
     *             if this state is not a valid text state.
     */
    public String getText() {
        String text = null;
        if (curStAXEventType == XMLStreamConstants.CHARACTERS
                || curStAXEventType == XMLStreamConstants.SPACE) {
            text = staxParser.characters.toString();
        } else if (curStAXEventType == XMLStreamConstants.COMMENT) {
            text = staxParser.comment.toString();
        } else {
            throw new IllegalStateException(
                    "The current state is not a valid text state.");
        }
        return text;
    }

    /**
     * Returns an array which contains the characters from this event. This
     * array should be treated as read-only and transient. I.e. the array will
     * contain the text characters until the XMLStreamReader moves on to the
     * next event. Attempts to hold onto the character array beyond that time or
     * modify the contents of the array are breaches of the contract for this
     * interface.
     * 
     * @return the current text or an empty array
     * @throws java.lang.IllegalStateException
     *             if this state is not a valid text state.
     */
    public char[] getTextCharacters() {
        String text = getText();

        return text == null ? new char[] {} : text.toCharArray();
    }

    /**
     * Gets the the text associated with a CHARACTERS, SPACE or CDATA event.
     * Text starting a "sourceStart" is copied into "target" starting at
     * "targetStart". Up to "length" characters are copied. The number of
     * characters actually copied is returned.
     * 
     * XMLStreamException may be thrown if there are any XML errors in the
     * underlying source. The "targetStart" argument must be greater than or
     * equal to 0 and less than the length of "target", Length must be greater
     * than 0 and "targetStart + length" must be less than or equal to length of
     * "target".
     * 
     * @param sourceStart
     *            the index of the first character in the source array to copy
     * @param target
     *            the destination array
     * @param targetStart
     *            the start offset in the target array
     * @param length
     *            the number of characters to copy
     * @return the number of characters actually copied
     * @throws XMLStreamException
     *             if the underlying XML source is not well-formed
     * @throws IndexOutOfBoundsException
     *             if targetStart < 0 or > than the length of target
     * @throws IndexOutOfBoundsException
     *             if length < 0 or targetStart + length > length of target
     * @throws UnsupportedOperationException
     *             if this method is not supported
     * @throws NullPointerException
     *             is if target is null
     */
    public int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {

        if (target == null)
            throw new NullPointerException();

        int targetLen = target.length;

        if (targetStart < 0 || targetStart > targetLen)
            throw new ArrayIndexOutOfBoundsException(
                    "The start position of target is out of index");

        if (length < 0)
            throw new ArrayIndexOutOfBoundsException(
                    "The length is out of index");

        int len = getTextLength();
        if (sourceStart < 0 || sourceStart > len) {
            throw new ArrayIndexOutOfBoundsException(
                    "The start position of source is out of index");
        }

        int avail = len - sourceStart;

        if (avail < length) {
            length = avail;
        }

        char[] intBuf = getTextCharacters();
        int intStart = getTextStart();
        System.arraycopy(intBuf, intStart + sourceStart, target, targetStart,
                length);
        return length;
    }

    /**
     * Returns the offset into the text character array where the first
     * character (of this text event) is stored.
     * 
     * @throws java.lang.IllegalStateException
     *             if this state is not a valid text state.
     */
    public int getTextStart() {
        if (!hasText()) {
            throw new IllegalStateException(
                    "The current state is not a valid text state.");
        }

        // TODO: need further investigation
        return 0;
    }

    /**
     * Returns the length of the sequence of characters for this Text event
     * within the text character array.
     * 
     * @throws java.lang.IllegalStateException
     *             if this state is not a valid text state.
     */
    public int getTextLength() {
        if (!hasText()) {
            throw new IllegalStateException(
                    "The current state is not a valid text state.");
        }

        // TODO: need further investigation with getTextStart
        int textLen = getText().length();
        return textLen;
    }

    /**
     * Return input encoding if known or null if unknown.
     * 
     * @return the encoding of this instance or null
     */
    public String getEncoding() {
        return staxParser.encodingDoc;
    }

    /**
     * Return true if the current event has text, false otherwise The following
     * events have text: CHARACTERS,DTD ,ENTITY_REFERENCE, COMMENT, SPACE
     * 
     */
    public boolean hasText() {
        boolean hasText = false;
        if (curStAXEventType == XMLStreamConstants.CHARACTERS
                || curStAXEventType == XMLStreamConstants.ENTITY_REFERENCE
                || curStAXEventType == XMLStreamConstants.COMMENT
                || curStAXEventType == XMLStreamConstants.SPACE
                || curStAXEventType == XMLStreamConstants.DTD) {
            hasText = true;
        }

        return hasText;
    }

    /**
     * Return the current location of the processor. If the Location is unknown
     * the processor should return an implementation of Location that returns -1
     * for the location and null for the publicId and systemId. The location
     * information is only valid until next() is called.
     */
    public Location getLocation() {
        return this.location;
    }

    /**
     * Returns a QName for the current START_ELEMENT or END_ELEMENT event
     * 
     * 
     * @return the QName for the current START_ELEMENT or END_ELEMENT event
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or END_ELEMENT
     */
    public QName getName() {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            return new QName(staxParser.elementName.uri,
                    staxParser.elementName.localpart);
        }
        throw new IllegalStateException(
                "The current state is not a valid START_ELEMENT or END_ELEMENT state.");
    }

    /**
     * Returns the (local) name of the current event. For START_ELEMENT or
     * END_ELEMENT returns the (local) name of the current element. For
     * ENTITY_REFERENCE it returns entity name.
     * 
     * The current event must be START_ELEMENT or END_ELEMENT, or
     * ENTITY_REFERENCE
     * 
     * @return the localName
     * @throws IllegalStateException
     *             if this not a START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE
     */
    public String getLocalName() {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            return staxParser.elementName.localpart;
        } else if (curStAXEventType == XMLStreamConstants.ENTITY_REFERENCE) {
            return staxParser.entityReferrenceName;
        } else {
            throw new IllegalStateException(
                    "Current state is not START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
        }
    }

    /**
     * returns true if the current event has a name (is a START_ELEMENT or
     * END_ELEMENT) returns false otherwise
     */
    public boolean hasName() {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * If the current event is a START_ELEMENT or END_ELEMENT this method
     * returns the URI of the current element (URI mapping to the prefix
     * element/attribute has, if any; or if no prefix, null for attribute, and
     * the default namespace URI for the element).
     * 
     * @return the URI bound to this elements prefix, the default namespace, or
     *         null
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT, END_ELEMENT or ATTRIBUTE
     */
    public String getNamespaceURI() {
        // TODO: Investigate whether ATTRIBUTE is existed?

        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            return staxParser.elementName.uri;
        } else {
            throw new IllegalStateException(
                    "Current state is not START_ELEMENT, END_ELEMENT");
        }
    }

    /**
     * Returns the prefix of the current event or null if the event does not
     * have a prefix
     * 
     * @return the prefix or null
     * @throws IllegalStateException
     *             if this is not a START_ELEMENT or END_ELEMENT
     */
    public String getPrefix() {
        if (curStAXEventType == XMLStreamConstants.START_ELEMENT
                || curStAXEventType == XMLStreamConstants.END_ELEMENT) {
            return staxParser.elementName.prefix;
        } else {
            throw new IllegalStateException(
                    "Current state is not START_ELEMENT, END_ELEMENT");
        }
    }

    /**
     * Get the xml version declared on the xml declaration Returns null if none
     * was declared
     * 
     * @return the XML version or null
     */
    public String getVersion() {
        return staxParser.versionXML;
    }

    /**
     * Get the standalone declaration from the xml declaration.
     * 
     * @return true if this is standalone, or false otherwise
     */
    public boolean isStandalone() {
        if (standaloneSet()) {
            if (staxParser.standaloneXML.equalsIgnoreCase("yes")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if standalone was set in the document
     * 
     * @return true if standalone was set in the document, or false otherwise
     */
    public boolean standaloneSet() {
        return staxParser.standaloneXML == null ? false : true;
    }

    /**
     * Returns the character encoding declared on the xml declaration Returns
     * null if none was declared
     * 
     * @return the encoding declared in the document or null
     */
    public String getCharacterEncodingScheme() {
        return staxParser.encodingXML;
    }

    /**
     * Get the target of a processing instruction
     * 
     * @return the target
     * @throws IllegalStateException
     *             if the current event is not a
     *             {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public String getPITarget() {
        if (curStAXEventType == XMLStreamConstants.PROCESSING_INSTRUCTION) {
            return staxParser.piTarget;
        }

        throw new IllegalStateException(
                "Current state is not PROCESSING_INSTRUCTION");
    }

    /**
     * Get the data section of a processing instruction
     * 
     * @return the data (if processing instruction has any), or null if the
     *         processing instruction only has target.
     * @throws IllegalStateException
     *             if the current event is not a
     *             {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public String getPIData() {
        if (curStAXEventType == XMLStreamConstants.PROCESSING_INSTRUCTION) {
            return staxParser.piData.toString();
        }

        throw new IllegalStateException(
                "Current state is not PROCESSING_INSTRUCTION");
    }

}
