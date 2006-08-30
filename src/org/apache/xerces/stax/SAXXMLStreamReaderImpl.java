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
import org.xml.sax.XMLReader;

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
    int curType;
    
    private SAXLocation sl;
    
    // The attribute of element event
    //  private Attributes attrs;
    
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
        public String uri;
    }
    
    // Record the current namespace context
    private NamespaceContextImpl dc;
    
    boolean isCoalescing = false;
    
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
        
        Object obj = xif.getProperty(XMLInputFactory.IS_COALESCING);
        if(obj != null)
            isCoalescing = ((Boolean)obj).booleanValue();
        
        try {
            asp = new AsyncSAXParser(xr, is);
            asp.setDaemon(true);
            sl = new SAXLocation();		
            
            handler = new StAXSAXHandler(asp, this, sl);
            
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
            asp.interrupt();
            throw new XMLStreamException("No such element!");
        }
        
        if(asp.ex != null) {
            asp.interrupt();
            throw new XMLStreamException(asp.ex.getMessage(), asp.ex);
        }
        
        synchronized(asp) {
            asp.setRunningFlag(true);
            asp.notify();
            
            try {
                while (asp.getRunningFlag() == true) {
                    if(asp.ex != null) {
                        asp.interrupt();
                        throw new XMLStreamException(asp.ex.getMessage(), asp.ex);
                    }
                    asp.wait();
                }
                
                return curType;
            } 
            catch (Exception e) {
                asp.interrupt();
                throw new XMLStreamException(
                        "Error occurs when processing SAXSource", e);
            }
        }
    }
    
    /**
     * This method is to record attribute(not namespace) and namespace of
     * current element
     * @throws XMLStreamException 
     * 
     */
    protected void initialElementAttrs(Attributes attrs) throws XMLStreamException {
        if (curType == XMLStreamConstants.START_ELEMENT) {
            curAttrs.clear();
            
//          this.attrs = attrs;
            if (attrs != null) {
                for (int i = 0; i < attrs.getLength(); i++) {
                    
                    String qname = attrs.getQName(i);
                    String value = attrs.getValue(i);
                    String type = attrs.getType(i);
                    String uri = attrs.getURI(i);
                    String local = attrs.getLocalName(i);
                    
                    //namespaces
                    
                    String prefix = null;
                    if(qname.equals("xmlns")) {
                        prefix = "";
                        if("".equals(value))
                            value = null;
                        dc.addNamespace(prefix, value);
                    }
                    else if(qname.startsWith("xmlns:")){
                        prefix = qname.substring(6);
                        dc.addNamespace(prefix, value);
                    }
                    else { //attributes
                        Attribute attr = new Attribute();
                        if(qname != null && !"".equals(qname)) {
                            int indexPre = qname.indexOf(":");
                            if (indexPre != -1){
                                local = qname.substring(indexPre + 1);
                                prefix = qname.substring(0, indexPre);
                            }
                            else {
                                local = qname;
                            }
                        }
                        
                        attr.value = value;
                        attr.prefix = prefix;
                        attr.local = local;
                        attr.type = type;
                        attr.uri = uri;
                        curAttrs.add(attr);
                    }
                }
                checkDupAttrs();
            }
        }
        
    }
    
    private void checkDupAttrs() throws XMLStreamException {
        Attribute attr1, attr2;
        String uri1, uri2;
        
        int size = curAttrs.size();
        for(int i = 0; i < size - 1; ++i) {
            attr1 = (Attribute)curAttrs.get(i);
            
            //get attr1's namespace uri
            uri1 = attr1.uri;
            if(uri1 == null || "".equals(uri1)) {
                if(attr1.prefix != null)
                    uri1 = dc.getNamespaceURI(attr1.prefix);
                else
                    uri1 = null;
            }
            
            for(int j = i + 1; j < size; ++j) {
                attr2 = (Attribute)curAttrs.get(j);
                uri2 = attr2.uri;
                
                //get a's namespace uri
                if(uri2 == null || "".equals(uri2)) {
                    if(attr2.prefix != null)
                        uri2 = dc.getNamespaceURI(attr2.prefix);
                    else
                        uri2 = null;
                }
                
                //compare a with attr
                if(uri1 == null && uri2 == null || uri1 != null && uri1.equals(uri2)) {
                    if(attr1.local.equals(attr2.local))
                        throw new XMLStreamException("Duplicate attributes");
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
        boolean ok = (type == this.curType);
        
        if (ok && localName != null) {
            if (curType == START_ELEMENT || curType == END_ELEMENT
                    || curType == ENTITY_REFERENCE) {
                ok = localName.equals(getLocalName());
            } else {
                throw new XMLStreamException("Not Match.");
            }
        }
        
        if (ok && namespaceURI != null) {
            if (curType == START_ELEMENT || curType == START_ELEMENT) {
                String currNsUri = getNamespaceURI();
                if (namespaceURI.length() == 0) {
                    ok = (currNsUri == null);
                } else {
                    ok = namespaceURI.equals(currNsUri);
                }
            }
        }
        
        if (!ok)
            throw new XMLStreamException ("Not Match.");
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
        //if the asp thread is still alive, interrupt it
        if(asp.isAlive() && !asp.isInterrupted())
            asp.interrupt();
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
    
    private Attribute getAttribute(int index) {
        int leng = getAttributeCount();//current state being checked in this method 
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        return (Attribute)curAttrs.get(index);
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
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.ATTRIBUTE){
            Attribute attr;
            String uri;
            for(int i = 0; i < curAttrs.size(); i++){
                attr = (Attribute)curAttrs.get(i);
                uri = getAttributeNamespace(i);
                if(attr.local.equals(localName)) {
                    if(namespaceURI == null && uri == null ||
                            namespaceURI != null && namespaceURI.equals(uri)) {
                        return attr.value;
                    }
                }
            }
            return null;
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
        if (curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.ATTRIBUTE) {
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
        Attribute attr = getAttribute(index);
        return new QName(getAttributeNamespace(index), attr.local, attr.prefix);
    }
    
    /**
     * Returns the namespace of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the namespace URI (can be null)
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeNamespace(int index) {
        Attribute attr = getAttribute(index);
        if(attr.uri == null || "".equals(attr.uri)) {
            if(attr.prefix != null)
                return dc.getNamespaceURI(attr.prefix);
            else
                return null;
        }
        
        return attr.uri;
    }
    
    /**
     * Returns the localName of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the localName of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeLocalName(int index) {
        Attribute attr = getAttribute(index);
        return attr.local;
    }
    
    /**
     * Returns the prefix of this attribute at the
     * provided index
     * @param index the position of the attribute
     * @return the prefix of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributePrefix(int index) {
        Attribute attr = getAttribute(index);
        return attr.prefix;
    }
    
    /**
     * Returns the XML type of the attribute at the provided
     * index
     * @param index the position of the attribute
     * @return the XML type of the attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeType(int index) {
        Attribute attr = getAttribute(index);
        return attr.type;
    }
    
    /**
     * Returns the value of the attribute at the
     * index
     * @param index the position of the attribute
     * @return the attribute value
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public String getAttributeValue(int index) {
        Attribute attr = getAttribute(index);
        return attr.value;
    }
    
    
    /**
     * Returns a boolean which indicates if this
     * attribute was created by default
     * @param index the position of the attribute
     * @return true if this is a default attribute
     * @throws IllegalStateException if this is not a START_ELEMENT or ATTRIBUTE
     */
    public boolean isAttributeSpecified(int index) {
        if(curType != XMLStreamConstants.START_ELEMENT && curType != XMLStreamConstants.ATTRIBUTE) {
            throw new IllegalStateException("Current event is not START_ELEMENT or ATTRIBUTE");
        }
        
        int leng = getAttributeCount();        
        if (index + 1 > leng || index < 0)
            throw new IndexOutOfBoundsException("The index "+ index+ " should be between 0..."+ (leng-1));
        
        return true;
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
            
            if(al == null)
                return 0;
            else
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
        
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT) { 
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
        
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT) { 
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
        if(curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.ENTITY_REFERENCE ||
                curType == XMLStreamConstants.COMMENT || curType == XMLStreamConstants.SPACE || 
                curType == XMLStreamConstants.DTD || curType == XMLStreamConstants.CDATA){
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
        if(curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.CDATA ||
                curType == XMLStreamConstants.COMMENT || curType == XMLStreamConstants.SPACE){
            String text = getText();
            return text.toCharArray();
        }
        
        throw new IllegalStateException(
        "The current event is not a valid text state.");	
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
        if(curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.CDATA ||
                curType == XMLStreamConstants.COMMENT || curType == XMLStreamConstants.SPACE){
//          return asp.getCharacterStart();
            return 0;
        }
        
        throw new IllegalStateException(
        "The current event is not a valid text state.");	
    }
    
    /**
     * Returns the length of the sequence of characters for this
     * Text event within the text character array.
     * @throws java.lang.IllegalStateException if this state is not
     * a valid text state.
     */
    public int getTextLength() {
        if(curType == XMLStreamConstants.CHARACTERS || curType == XMLStreamConstants.CDATA ||
                curType == XMLStreamConstants.COMMENT || curType == XMLStreamConstants.SPACE){
//          return asp.getCharacterLength();
            return asp.getCharacters().length;
        }
        
        throw new IllegalStateException(
        "The current event is not a valid text state.");	
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
            String prefix = getPrefix();
            
            QName qname;
            if(prefix == null) {
                qname = new QName(getNamespaceURI(), getLocalName());
            }
            else {
                qname = new QName(getNamespaceURI(), getLocalName(), getPrefix()); 
            }
            return qname;
        }
        throw new IllegalStateException(
        "The current event is not START_ELEMENT or END_ELEMENT.");
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
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT
                || curType == XMLStreamConstants.ENTITY_REFERENCE) { 
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
        if(curType == XMLStreamConstants.START_ELEMENT || curType == XMLStreamConstants.END_ELEMENT || curType == XMLStreamConstants.ATTRIBUTE) { 
            String uri = asp.getUri();
            if(uri == null || "".equals(uri)) {
                String prefix = getPrefix();
                if(prefix == null) 
                    prefix = "";
                uri = dc.getNamespaceURI(prefix);
            }
            
            return uri;
        }
        else
            throw new IllegalStateException("Current event is not START_ELEMENT, END_ELEMENT");
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

	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
    
}
