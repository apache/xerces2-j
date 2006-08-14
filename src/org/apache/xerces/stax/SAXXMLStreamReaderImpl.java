/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

/**
 * <p>An XMLStreamReader created from a SAXSource.</p>
 * 
 * @author Hua Lei
 * 
 * @version $Id$
 */
public class SAXXMLStreamReaderImpl implements XMLStreamReader {
    
    //	The XMLInputFactory instance which creates the SAXXMLStreamReader
    private XMLInputFactory xif;
    
    // The Asynchronized SAX parser 
    private AsyncSAXParser asp;
    
    // The SAX event handler which deals with parsed event
    private StAXSAXHandler handler;
    
    // The current event type
    private int curType;
    
    // Record the location of SAX parser
    private Locator loc;
    
    private SAXLocation sl;
    
    // The attribute of element event
    private Attributes attrs;
    
    private String xmlVersion = "1.0" ;
    
    private boolean xmlStandalone = false;
    
    private String xmlEncoding = "UTF-8";
    
    private String inputEncoding = "UTF-8";
    
    // Record the current attribute and namespace for StartElement event
    private ArrayList curAttrs;
    
    class Attribute {
        public String prefix;
        public String local;
        public String value;
        public String type;
    }
    
    // Record the current namespace context
    private NamespaceContextImpl dc;
    
    /**
     * The construction method of SAXXMLStreamReader
     * 
     * @param is
     * @param xif
     */
    public SAXXMLStreamReaderImpl(XMLReader xr, InputSource is, XMLInputFactory xif)
    throws XMLStreamException {
        
        this.xif = xif;
        this.curAttrs = new ArrayList();
        
        try {
            asp = new AsyncSAXParser(xr, is);
            loc = new LocatorImpl();
            sl = new SAXLocation(loc);		
            
            handler = new StAXSAXHandler(asp, this, loc);
            
            xr.setContentHandler(handler);
            xr.setDTDHandler(handler);
            xr.setEntityResolver(handler);
            xr.setErrorHandler(handler);
            
            dc = new NamespaceContextImpl();
            
            asp.start();
            
            synchronized (asp) {
                while (asp.getRunningFlag() == true)
                    asp.wait();
            }
        }
        catch(Exception e) {
            throw new XMLStreamException("Error occurs during the SAX parsing process", e);  
        }
    }
    
    protected void setCurType(int curType) {
        this.curType = curType;
    }
    
    /**
     * Get the value of a feature/property from the underlying implementation
     *
     * @param name The name of the property, may not be null
     * @return The value of the property
     * @throws IllegalArgumentException if name is null
     */
    public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException {
        if (name == null)
            throw new IllegalArgumentException("The feature name should not be null");
        return xif.getProperty(name);
    }
    
    /**
     * Returns true if there are more parsing events and false
     * if there are no more events.  This method will return
     * false if the current state of the XMLStreamReader is
     * END_DOCUMENT
     * 
     * @return true if there are more events, false otherwise
     * @throws XMLStreamException if there is a fatal error detecting the next state
     */
    public boolean hasNext() throws XMLStreamException {
        
        if (curType == XMLStreamConstants.END_DOCUMENT)
            return false;
        return true;
    }
    
    /**
     * This method will throw an IllegalStateException if it is called after hasNext() returns false.
     *
     * @see javax.xml.stream.events.XMLEvent
     * @return the integer code corresponding to the current parse event
     * @throws NoSuchElementException if this is called when hasNext() returns false
     * @throws XMLStreamException  if there is an error processing the underlying XML source
     */
    public  int next() throws XMLStreamException {	 
        if (hasNext() == false) {
            throw new XMLStreamException("No such element!");
        }
        synchronized(asp) {
            asp.setRunningFlag(true);
            asp.notify();
            
            try {
                while (asp.getRunningFlag() == true)
                    asp.wait();
                
                return curType;
            } 
            catch (Exception e) {
                throw new XMLStreamException(
                        "Error occurs when processing SAXSource", e);
            }
        }
    }
    
    /**
     * This method is to record attribute(not namespace) and namespace of
     * current element
     * 
     */
    protected void initialElementAttrs(Attributes attrs) {
        if (curType == XMLStreamConstants.START_ELEMENT) {
            curAttrs.clear();
            
            this.attrs = attrs;
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    
                    String name = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    String type = attrs.getType(i);
                    
                    if (!name.startsWith("xmlns")) {
                        Attribute attr = new Attribute();
                        String prefix = null;
                        String local = name;
                        int indexPre = name.indexOf(":");
                        if (indexPre != -1){
                            local = name.substring(indexPre + 1);
                            prefix = name.substring(0, indexPre);
                        }
                        attr.value = value;
                        attr.prefix = prefix;
                        attr.local = local;
                        attr.type = type;
                        curAttrs.add(attr);
                    }
                    else {
                        
                        String prefix = "";
                        if (!name.equals("xmlns")) {
                            prefix += name.substring(6);
                        }
                        dc.addNamespace(prefix, value);  
                    }
                }
            }
        }
        
    }
    
    /**
     * Test if the current event is of the given type and if the namespace and name match the current
     * namespace and name of the current event.  If the namespaceURI is null it is not checked for equality,
     * if the localName is null it is not checked for equality.
     * @param type the event type
     * @param namespaceURI the uri of the event, may be null
     * @param localName the localName of the event, may be null
     * @throws XMLStreamException if the required values are not matched.
     */
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        // Need to be realized
    }
    
    
    /**
     * Reads the content of a text-only element, an exception is thrown if this is
     * not a text-only element.
     * Regardless of value of javax.xml.stream.isCoalescing this method always returns coalesced content.
     
     * <br /> Precondition: the current event is START_ELEMENT.
     * <br /> Postcondition: the current event is the corresponding END_ELEMENT.
     *
     * @throws XMLStreamException if the current event is not a START_ELEMENT 
     * or if a non text element is encountered
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
            } 
            else if (eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT) {
                // skipping
            } 
            else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException(
                        "unexpected end of document when reading element text content", getLocation());
            } 
            else if (eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException(
                        "element text content may not contain START_ELEMENT",
                        getLocation());
            } 
            else {
                throw new XMLStreamException("Unexpected event type "
                        + eventType, getLocation());
            }
            eventType = next();
        }
        return buf.toString();
    }
    
    /**
     * Skips any white space (isWhiteSpace() returns true), COMMENT,
     * or PROCESSING_INSTRUCTION,
     * until a START_ELEMENT or END_ELEMENT is reached.
     * If other than white space characters, COMMENT, PROCESSING_INSTRUCTION, START_ELEMENT, END_ELEMENT
     * are encountered, an exception is thrown. 
     * return eventType;
     * </pre>
     *
     * @return the event type of the element read (START_ELEMENT or END_ELEMENT)
     * @throws XMLStreamException if the current event is not white space, PROCESSING_INSTRUCTION,
     * START_ELEMENT or END_ELEMENT
     * @throws NoSuchElementException if this is called when hasNext() returns false
     */
    public int nextTag() throws XMLStreamException {
        int eventType = next();
        while ((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip																		// //																			// whitespace
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
     * Frees any resources associated with this Reader.  This method does not close the
     * underlying input source.
     * 
     * @throws XMLStreamException if there are errors freeing associated resources
     */
    public void close() throws XMLStreamException {
        // As for SAXSource, this method needs to close the underlying bytestream
    }
    
    /**
     * Return the uri for the given prefix.
     * The uri returned depends on the current state of the processor.
     *
     *
     * @param prefix The prefix to lookup, may not be null
     * @return the uri bound to the given prefix or null if it is not bound
     * @throws IllegalArgumentException if the prefix is null
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new IllegalStateException("The prefix should not be null");
        String uri = dc.getNamespaceURI(prefix);
        return uri;
    }
    
    /**
     * Returns true if the cursor points to a start tag (otherwise false)
     * @return true if the cursor points to a start tag, false otherwise
     */
    public boolean isStartElement() {  
        return curType == XMLStreamConstants.START_ELEMENT;
    }
    
    /**
     * Returns true if the cursor points to an end tag (otherwise false)
     * @return true if the cursor points to an end tag, false otherwise
     */
    public boolean isEndElement() {
        return curType == XMLStreamConstants.END_ELEMENT;
    }
    
    /**
     * Returns true if the cursor points to a character data event
     * @return true if the cursor points to character data, false otherwise
     */
    public boolean isCharacters() {
        return curType == XMLStreamConstants.CHARACTERS;
    }
    
    /**
     * Returns true if the cursor points to a character data event
     * that consists of all whitespace
     * 
     * @return true if the cursor points to all whitespace, false otherwise
     */
    public boolean isWhiteSpace() {
        if (curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.SPACE) {
            char[] data = asp.getCharacters();
            if(data != null){
                for(int i = 0; i < data.length; i++){
                    char c = data[i];
                    // The white space is tab, enter, and blank
                    // by http://www.w3.org/TR/2004/REC-xml-20040204/#sec-white-space
                    if(c == '\t' || c == '\n' || c == ' ' || c == '\r') continue;
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Returns the normalized attribute value of the
     * attribute with the namespace and localName
     * If the namespaceURI is null the namespace
     * is not checked for equality
     * 
     * @param namespaceURI the namespace of the attribute
     * @param localName the local name of the attribute, cannot be null
     * @return returns the value of the attribute , returns null if not found
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeValue(String namespaceURI,
            String localName) {
        if(curType == XMLStreamConstants.START_ELEMENT){
            String value = null;
            for(int i = 0; i < curAttrs.size(); i++){
                Attribute attr = (Attribute)curAttrs.get(i);
                if(attr.local.equals(localName)) return attr.value;
            }
            return value;
        }
        
        throw new IllegalStateException("Current event is not START_ELEMENT or ATTRIBUTE");
    }
    
    /**
     * Returns the count of attributes on this START_ELEMENT,
     * this method is only valid on a START_ELEMENT or ATTRIBUTE. 
     * For SAXSource, there is no explict Attribute event
     * 
     * @return returns the number of attributes
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public int getAttributeCount() {
        if (curType == XMLStreamConstants.START_ELEMENT) {
            return curAttrs.size();
        }
        throw new IllegalStateException(
        "Current event is not START_ELEMENT or ATTRIBUTE");
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
        if (curType == XMLStreamConstants.START_ELEMENT) {
            int leng = getAttributeCount();
            if (index + 1 > leng || index < 0)
                throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
            
            String name =  this.getAttributeLocalName(index);
            String uri = this.getAttributeNamespace(index);
            return new QName(uri, name);
        }
        throw new IllegalStateException(
        "Current event is not START_ELEMENT or ATTRIBUTE");
    }
    
    /**
     * Returns the namespace of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the namespace URI (can be null)
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeNamespace(int index) {
        String pre = this.getAttributePrefix(index);
        return dc.getNamespaceURI(pre);		
    }
    
    /**
     * Returns the localName of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the localName of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeLocalName(int index) {
        
        String attrName;
        int leng = getAttributeCount();
        
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if (curType == XMLStreamConstants.START_ELEMENT) {
            Attribute indexAttr = (Attribute) curAttrs.get(index);
            attrName = indexAttr.local;
        }
        else {
            throw new IllegalStateException(
            "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return attrName;
    }
    
    /**
     * Returns the prefix of this attribute at the
     * provided index
     * @param index the position of the attribute
     * @return the prefix of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributePrefix(int index) {
        String attrName;
        int leng = getAttributeCount();
        
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if (curType == XMLStreamConstants.START_ELEMENT) {
            Attribute indexAttr = (Attribute) curAttrs.get(index);
            attrName = indexAttr.prefix;
        }
        else {
            throw new IllegalStateException(
            "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return attrName;
    }
    
    /**
     * Returns the XML type of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the XML type of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeType(int index) {
        String attrType;
        int leng = getAttributeCount();
        
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if (curType == XMLStreamConstants.START_ELEMENT) {
            Attribute indexAttr = (Attribute) curAttrs.get(index);
            attrType = indexAttr.type;
        }
        else {
            throw new IllegalStateException(
            "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return attrType;
    }
    
    /**
     * Returns the value of the attribute at the
     * index
     * @param index the position of the attribute
     * @return the attribute value
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeValue(int index) {
        String attrValue;
        int leng = getAttributeCount();
        
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if (curType == XMLStreamConstants.START_ELEMENT) {
            Attribute indexAttr = (Attribute) curAttrs.get(index);
            attrValue = indexAttr.value;
        }
        else {
            throw new IllegalStateException(
            "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return attrValue;
    }
    
    
    /**
     * Returns a boolean which indicates if this
     * attribute was created by default
     * @param index the position of the attribute
     * @return true if this is a default attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public boolean isAttributeSpecified(int index) {
        return false;
    }
    
    /**
     * Returns the count of namespaces declared on this START_ELEMENT or END_ELEMENT.
     * This method is only valid on a START_ELEMENT, END_ELEMENT or NAMESPACE.
     * On an END_ELEMENT the count is of the namespaces that are about to go
     * out of scope.  This is the equivalent of the information reported
     * by SAX callback for an end element event.
     * @return returns the number of namespace declarations on this specific element
     * @throws IllegalStateException if this is not a START_ELEMEN or, END_ELEMENT
     */
    public int getNamespaceCount() { 
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT){
            ArrayList al = dc.getNamespaces();
            
            return al.size();
        }
        if(curType == XMLStreamConstants.NAMESPACE)
            return 1;
        return 0;
    }
    
    /**
     * Returns the prefix for the namespace declared at the
     * index.  Returns null if this is the default namespace
     * declaration
     *
     * @param index the position of the namespace declaration
     * @return returns the namespace prefix
     * @throws IllegalStateException if this is not a START_ELEMENT,
     *    END_ELEMENT or NAMESPACE
     */
    public String getNamespacePrefix(int index) {
        int leng = getNamespaceCount();
        
        if(index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.START_ELEMENT) { 
            return dc.getNamespacePrefix(index);
        }
        else
            throw new IllegalStateException(
            "Current event is not START_ELEMENT, END_ELEMENT or ATTRIBUTE");	
    }
    
    /**
     * Returns the uri for the namespace declared at the
     * index.
     *
     * @param index the position of the namespace declaration
     * @return returns the namespace uri
     * @throws IllegalStateException if this is not a START_ELEMENT,
     *   END_ELEMENT or NAMESPACE
     */
    public String getNamespaceURI(int index) {
        int leng = getNamespaceCount();
        
        if(index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.START_ELEMENT) { 
            return dc.getNamespaceURI(index);
        }
        else
            throw new IllegalStateException(
            "Current event is not START_ELEMENT, END_ELEMENT or ATTRIBUTE");		
    }
    
    /**
     * Returns a read only namespace context for the current
     * position.  The context is transient and only valid until
     * a call to next() changes the state of the reader.
     *
     * @return return a namespace context
     */
    public NamespaceContext getNamespaceContext() {
        return dc;
    }
    
    /**
     * Returns an integer code that indicates the type
     * of the event the cursor is pointing to.
     */
    public int getEventType() {
        return curType;
    }
    
    /**
     * Returns the current value of the parse event as a string,
     * this returns the string value of a CHARACTERS event,
     * returns the value of a COMMENT, the replacement value
     * for an ENTITY_REFERENCE, the string value of a CDATA section,
     * the string value for a SPACE event,
     * or the String value of the internal subset of the DTD.
     * If an ENTITY_REFERENCE has been resolved, any character data
     * will be reported as CHARACTERS events.
     * 
     * The DOM nodes Text, Comment, and CDATASection extends CharacterData, which
     * will map to StAX character events. 
     * 
     * @return the current text or null
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public String getText() {
        if(curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.SPACE){
            char[] chars = asp.getCharacters();
            
            return  String.valueOf(chars);
        }
        
        throw new IllegalStateException(
        "The current event is not a valid text state.");	
    }
    
    /**
     * Returns an array which contains the characters from this event.
     * This array should be treated as read-only and transient. I.e. the array will
     * contain the text characters until the XMLStreamReader moves on to the next event.
     * Attempts to hold onto the character array beyond that time or modify the
     * contents of the array are breaches of the contract for this interface.
     * 
     * @return the current text or an empty array
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public char[] getTextCharacters() {
        String text = getText();
        return text.toCharArray();
    }
    
    /**
     * Gets the the text associated with a CHARACTERS, SPACE or CDATA event.  
     * Text starting a "sourceStart" is copied into "target" starting at "targetStart".  
     * Up to "length" characters are copied.  The number of characters actually copied is returned.
     *
     * XMLStreamException may be thrown if there are any XML errors in the underlying source. 
     * The "targetStart" argument must be greater than or equal to 0 and less than the length of "target",  
     * Length must be greater than 0 and "targetStart + length" must be less than or equal to length of "target".  
     *
     * @param sourceStart the index of the first character in the source array to copy
     * @param target the destination array
     * @param targetStart the start offset in the target array
     * @param length the number of characters to copy
     * @return the number of characters actually copied
     * @throws XMLStreamException if the underlying XML source is not well-formed
     * @throws IndexOutOfBoundsException if targetStart < 0 or > than the length of target
     * @throws IndexOutOfBoundsException if length < 0 or targetStart + length > length of target
     * @throws UnsupportedOperationException if this method is not supported 
     * @throws NullPointerException is if target is null
     */
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) 
    throws XMLStreamException {
        
        if (target == null)
            throw new NullPointerException();
        
        int targetLen = target.length;
        
        if (targetStart < 0 || targetStart > targetLen)
            throw new ArrayIndexOutOfBoundsException("The start position of target is out of index");
        
        
        if (length < 0)
            throw new ArrayIndexOutOfBoundsException("The length is out of index");
        
        int len = getTextLength();
        if (sourceStart < 0 || sourceStart > len) {
            throw new ArrayIndexOutOfBoundsException("The start position of source is out of index");
        }
        
        
        int avail = len - sourceStart;
        
        if (avail < length) {
            length = avail;
        }
        
        
        char[] intBuf = getTextCharacters();
        int intStart = getTextStart();
        System.arraycopy(intBuf, intStart + sourceStart,
                target, targetStart, length);
        return length;
    }
    
    /**
     * Returns the offset into the text character array where the first
     * character (of this text event) is stored.
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public int getTextStart() {
        return 0;
    }
    
    /**
     * Returns the length of the sequence of characters for this
     * Text event within the text character array.
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public int getTextLength() {
        String text = getText();
        return text.length();
    }
    
    /**
     * Return input encoding if known or null if unknown.
     * @return the encoding of this instance or null
     */
    public String getEncoding() {
        return inputEncoding;
    }
    
    /**
     * Return true if the current event has text, false otherwise
     * The following events have text:
     * CHARACTERS,DTD ,ENTITY_REFERENCE, COMMENT, SPACE
     */
    public boolean hasText() {
        if (curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.ENTITY_REFERENCE ||
                curType == XMLStreamConstants.COMMENT || curType == XMLStreamConstants.SPACE || curType == XMLStreamConstants.DTD)
            return true;
        else
            return false;
    }
    
    /**
     * Return the current location of the processor.
     * If the Location is unknown the processor should return
     * an implementation of Location that returns -1 for the
     * location and null for the publicId and systemId.
     * The location information is only valid until next() is
     * called.
     */
    public Location getLocation() {
        return sl;
    }
    
    /**
     * Returns a QName for the current START_ELEMENT or END_ELEMENT event
     *
     * 
     * @return the QName for the current START_ELEMENT or END_ELEMENT event
     * @throws IllegalStateException if this is not a START_ELEMENT or
     * END_ELEMENT
     */
    public QName getName() {
        if (curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT) { 
            return new QName(getLocalName(), getNamespaceURI());
        }
        return null;
    }
    
    /**
     * Returns the (local) name of the current event.
     * For START_ELEMENT or END_ELEMENT returns the (local) name of the current element.
     * For ENTITY_REFERENCE it returns entity name.
     * The current event must be START_ELEMENT or END_ELEMENT, 
     * or ENTITY_REFERENCE
     * @return the localName
     * @throws IllegalStateException if this not a START_ELEMENT,
     * END_ELEMENT or ENTITY_REFERENCE
     */
    public String getLocalName() {
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT) { 
            String local = asp.getElementName();
            int indexPre = local.indexOf(":");
            if (indexPre != -1){
                local = local.substring(indexPre + 1);		
            }
            return local;
        }
        else
            throw new IllegalStateException("Current event is not START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
    }
    
    /**
     * returns true if the current event has a name (is a START_ELEMENT or END_ELEMENT)
     * returns false otherwise
     */
    public boolean hasName() {
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT)
            return true;
        else 
            return false;
    }
    
    /**
     * If the current event is a START_ELEMENT or END_ELEMENT  this method
     * returns the URI of the current element (URI mapping to the prefix
     * element/attribute has, if any; or if no prefix, null for attribute,
     * and the default namespace URI for the element).
     *
     * @return the URI bound to this elements prefix, the default namespace, or null
     * @throws IllegalStateException if this is not a START_ELEMENT, END_ELEMENT
     *    or ATTRIBUTE
     */
    public String getNamespaceURI() {
        String prefix = getPrefix();  
        
        if(prefix == null) prefix = ""; 
        return dc.getNamespaceURI(prefix);
    }
    
    /**
     * Returns the prefix of the current event or null if the event does not
     * have a prefix
     * @return the prefix or null
     * @throws IllegalStateException if this is not a START_ELEMENT or END_ELEMENT
     */
    public String getPrefix() {
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT) { 
            String pre = null;
            String name = asp.getElementName();
            int indexPre = name.indexOf(":");
            if(indexPre != -1){
                pre = name.substring(0, indexPre);		
            }
            return pre;
        }
        else
            throw new IllegalStateException("Current event is not START_ELEMENT, END_ELEMENT");
    }
    
    /**
     * Get the xml version declared on the xml declaration
     * Returns null if none was declared
     * @return the XML version or null
     */
    public String getVersion() {
        return xmlVersion;
    }
    
    /**
     * Get the standalone declaration from the xml declaration, if one found
     * ({@link #standaloneSet} returns true if one was specified).
     *
     * @return true if this is standalone, or false otherwise
     */
    public boolean isStandalone() {
        return xmlStandalone;
    }
    
    /**
     * Checks if standalone was set in the document
     * Since we only have DOMSource, the standaloneSet information is hard to 
     * get. Because the default standalone is false, if the standalone is true
     * then it was set.
     * 
     * @return true if standalone was set in the document, or false otherwise
     */
    public boolean standaloneSet() {
        if (xmlStandalone == false) return false;
        return true;
    }
    
    /**
     * Returns the character encoding declared on the xml declaration
     * Returns null if none was declared
     * @return the encoding declared in the document or null
     */
    public String getCharacterEncodingScheme() {
        return xmlEncoding;
    }
    
    /**
     * Get the target of a processing instruction
     * @return the target
     * @throws IllegalStateException if the current event is not a
     *   {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public String getPITarget() {
        if (curType == XMLStreamConstants.PROCESSING_INSTRUCTION) {
            String value = asp.getPITarget();
            return value;
        }
        throw new IllegalStateException("Current event is not PROCESSING_INSTRUCTION");
    }
    
    /**
     * Get the data section of a processing instruction
     * @return the data (if processing instruction has any), or null
     *    if the processing instruction only has target.
     * @throws IllegalStateException if the current event is not a
     *   {@link XMLStreamConstants#PROCESSING_INSTRUCTION}
     */
    public String getPIData(){
        if (curType == XMLStreamConstants.PROCESSING_INSTRUCTION) {
            String value = asp.getPIData();
            return value;
        }
        throw new IllegalStateException("Current event is not PROCESSING_INSTRUCTION");
    }
    
}
