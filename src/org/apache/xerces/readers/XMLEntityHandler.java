/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
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

import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.StringPool;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import java.io.InputStream;

/**
 * This is the interface used for entity management.  This interface
 * is typically implemented by the "parser" class to provide entity
 * management services for the scanner classes.
 *
 * @version $Id$
 */
public interface XMLEntityHandler extends Locator {

    /**
     * Special return values for scanCharRef method.  The normal return
     * value is a unicode character.  These error conditions are defined
     * using invalid XML unicode code points.
     */
    public static final int
        CHARREF_RESULT_SEMICOLON_REQUIRED  = -1,
        CHARREF_RESULT_INVALID_CHAR        = -2,
        CHARREF_RESULT_OUT_OF_RANGE        = -3;

    /**
     * Special return values for scanStringLiteral method.  The normal
     * return value is a StringPool handle.  These error conditions are
     * defined using invalid indices.
     */
    public static final int
        STRINGLIT_RESULT_QUOTE_REQUIRED = -1,
        STRINGLIT_RESULT_INVALID_CHAR   = -2;

    /**
     * Special return values for scanAttValue method.  The normal return
     * value is a StringPool handle for a simple AttValue that was already
     * correctly normalized for CDATA in the original document.  These
     * other return values either indicate an error or that the AttValue
     * needs further processing.
     */
    public static final int
        ATTVALUE_RESULT_COMPLEX         = -1,
        ATTVALUE_RESULT_LESSTHAN        = -2,
        ATTVALUE_RESULT_INVALID_CHAR    = -3;

    /**
     * Special return values for scanEntityValue method.  The normal return
     * value is a StringPool handle for a simple EntityValue that was entirely
     * contained within the original document.  These other return values can
     * either indicate an error or that the EntityValue needs further processing.
     */
    public static final int
        ENTITYVALUE_RESULT_FINISHED     = -1,
        ENTITYVALUE_RESULT_REFERENCE    = -2,
        ENTITYVALUE_RESULT_PEREF        = -3,
        ENTITYVALUE_RESULT_INVALID_CHAR = -4,
        ENTITYVALUE_RESULT_END_OF_INPUT = -5;

    /**
     * Return values for the scanContent method.
     */
    public static final int
        CONTENT_RESULT_START_OF_PI              =  0,
        CONTENT_RESULT_START_OF_COMMENT         =  1,
        CONTENT_RESULT_START_OF_CDSECT          =  2,
        CONTENT_RESULT_END_OF_CDSECT            =  3,
        CONTENT_RESULT_START_OF_ETAG            =  4,
        CONTENT_RESULT_MATCHING_ETAG            =  5,
        CONTENT_RESULT_START_OF_ELEMENT         =  6,
        CONTENT_RESULT_START_OF_CHARREF         =  7,
        CONTENT_RESULT_START_OF_ENTITYREF       =  8,
        CONTENT_RESULT_INVALID_CHAR             =  9,
        CONTENT_RESULT_MARKUP_NOT_RECOGNIZED    = 10,
        CONTENT_RESULT_MARKUP_END_OF_INPUT      = 11,
        CONTENT_RESULT_REFERENCE_END_OF_INPUT   = 12;

    /**
     * This is an enumeration of all the defined entity types.
     * These are provided to communicate state information to
     * the clients of the parser.
     */
    public static final int
        ENTITYTYPE_INTERNAL_PE      = 0,
        ENTITYTYPE_EXTERNAL_PE      = 1,
        ENTITYTYPE_INTERNAL         = 2,
        ENTITYTYPE_EXTERNAL         = 3,
        ENTITYTYPE_UNPARSED         = 4,
        ENTITYTYPE_DOCUMENT         = 5,
        ENTITYTYPE_EXTERNAL_SUBSET  = 6;

    /**
     * This is an enumeration of all the defined contexts in which
     * an entity reference may appear.  The order is important, as
     * all explicit general entity references must appear first and
     * the last of these must be ENTITYREF_IN_CONTENT.  This permits
     * the test "(context <= ENTITYREF_IN_CONTENT)" to be used as a
     * quick check for a general entity reference.
     *
     * @see #startReadingFromEntity
     */
    public static final int
        ENTITYREF_IN_ATTVALUE = 0,
        ENTITYREF_IN_DEFAULTATTVALUE = 1,
        ENTITYREF_IN_CONTENT = 2,
        ENTITYREF_IN_DTD_AS_MARKUP = 3,
        ENTITYREF_IN_ENTITYVALUE = 4,
        ENTITYREF_IN_DTD_WITHIN_MARKUP = 5,
        ENTITYREF_DOCUMENT = 6,
        ENTITYREF_EXTERNAL_SUBSET = 7;

    /**
     * Start reading document from an InputSource.
     *
     * @param source The input source for the document to process.
     * @return <code>true</code> if we were able to open the document source;
     *         <code>false</code> otherwise.
     * @exception java.lang.Exception
     */
    public boolean startReadingFromDocument(InputSource source) throws Exception;

    /**
     * Start reading from this entity.
     *
     * Note that the reader depth is not used by the reader, but is made
     * available so that it may be retrieved at end of input to test that
     * gramatical structures are properly nested within entities.
     *
     * @param entityName The entity name handle in the string pool.
     * @param readerDepth The depth to associate with the reader for this entity.
     * @param context The context of the entity reference; see ENTITYREF_IN_*.
     * @return <code>true</code> if the entity might start with a TextDecl;
     *         <code>false</code> otherwise.
     * @exception java.lang.Exception
     */
    public boolean startReadingFromEntity(int entityName, int readerDepth, int entityContext) throws Exception;

    /**
     * Expand the system identifier relative to the entity that we are processing.
     *
     * @return The expanded system identifier.
     */
    public String expandSystemId(String systemId);

    /**
     * DTD specific entity handler
     */
    public interface DTDHandler {
        /**
         * Start reading from the external subset of the DTD.
         *
         * @param publicId The public identifier for the external subset.
         * @param systemId The system identifier for the external subset.
         * @param readerDepth The depth to associate with the reader for the external subset.
         * @exception java.lang.Exception
         */
        public void startReadingFromExternalSubset(String publicId, String systemId, int readerDepth) throws Exception;

        /**
         * Finished reading from the external subset of the DTD.
         * @exception java.lang.Exception
         */
        public void stopReadingFromExternalSubset() throws Exception;

        /**
         * Start the scope of an entity declaration.
         *
         * @return <code>true</code> on success; otherwise
         *         <code>false</code> if the entity declaration is recursive.
         * @exception java.lang.Exception
         */
        public boolean startEntityDecl(boolean isPE, int entityName) throws Exception;

        /**
         * End the scope of an entity declaration.
         * @exception java.lang.Exception
         */
        public void endEntityDecl() throws Exception;

        /**
         * Declare entities and notations.
         */
        public int addInternalPEDecl(int entityName, int value, boolean isExternal) throws Exception;
        public int addExternalPEDecl(int entityName, int publicId, int systemId, boolean isExternal) throws Exception;
        public int addInternalEntityDecl(int entityName, int value, boolean isExternal) throws Exception;
        public int addExternalEntityDecl(int entityName, int publicId, int systemId, boolean isExternal) throws Exception;
        public int addUnparsedEntityDecl(int entityName, int publicId, int systemId, int notationName, boolean isExternal) throws Exception;
        public int addNotationDecl(int notationName, int publicId, int systemId, boolean isExternal) throws Exception;

        /**
         * Check for unparsed entity.
         *
         * @param entityName The string handle for the entity name.
         * @return <code>true</code> if entityName is an unparsed entity; otherwise
         *         <code>false</code> if entityName is not declared or not an unparsed entity.
         */
        public boolean isUnparsedEntity(int entityName);

        /**
         * Check for declared notation.
         *
         * @param notationName The string handle for the notation name.
         * @return <code>true</code> if notationName is a declared notation; otherwise
         *         <code>false</code> if notationName is not declared.
         */
        public boolean isNotationDeclared(int entityName);

        /**
         * Remember a required but undeclared notation.
         */
        public void addRequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args);

        /**
         * Check required but undeclared notations.
         */
        public void checkRequiredNotations() throws Exception;
    }

    /**
     * Return a unique identifier for the current reader.
     */
    public int getReaderId();

    /**
     * Set the depth for the current reader.
     */
    public void setReaderDepth(int depth);

    /**
     * Return the depth set for the current reader.
     */
    public int getReaderDepth();

    /**
     * Return the current reader.
     */
    public EntityReader getEntityReader();

    /**
     * This method is called by the reader subclasses at the
     * end of input.
     *
     * @return The reader to use next.
     * @exception java.lang.Exception
     */
    public EntityReader changeReaders() throws Exception;

    /**
     * This interface is used to store and retrieve character
     * sequences.  The primary use is for a literal data buffer
     * where we can construct the values for literal entity
     * replacement text.  When all of the characters for the
     * replacement text have been added to the buffer, the
     * contents are added to the string pool for later use
     * in constructing a StringReader if the entity is referenced.
     */
    public interface CharBuffer {
        /**
         * Append a character to this buffer.
         *
         * @param ch The character.
         */
        public void append(char ch);

        /**
         * Append characters to this buffer.
         *
         * @param chars The char array containing the characters.
         * @param offset The offset within the char array of the first character to append.
         * @param length The number of characters to append.
         */
        public void append(char[] chars, int offset, int length);

        /**
         * Get the current length of the buffer.  This is also the
         * offset of the next character that is added to the buffer.
         *
         * @return The length of the buffer.
         */
        public int length();

        /**
         * Add a region of this buffer to the string pool.
         *
         * @param offset The offset within this buffer of the first character of the string.
         * @param length The number of characters in the string.
         * @return The <code>StringPool</code> handle of the string.
         */
        public int addString(int offset, int length);
    }

    /**
     * Set the character data handler.
     */
    public void setCharDataHandler(XMLEntityHandler.CharDataHandler charDataHandler);

    /**
     * Get the character data handler.
     */
    public XMLEntityHandler.CharDataHandler getCharDataHandler();

    /**
     * Interface for passing character data.
     */
    public interface CharDataHandler {
        /**
         * Process character data, character array version
         * 
         * @param chars character buffer to be processed
         * @param offset offset in buffer where the data starts
         * @param length length of characters to be processed
         * @exception java.lang.Exception
         */
        public void processCharacters(char[] chars, int offset, int length) throws Exception;

        /**
         * Process character data, <code>StringPool</code> handle version
         *
         * @param stringHandle <code>StringPool</code> handle to the character data
         * @exception java.lang.Exception
         */
        public void processCharacters(int stringHandle) throws Exception;

        /**
         * Process white space data, character array version
         *
         * @param chars character buffer to be processed
         * @param offset offset in buffer where the data starts
         * @param length length of whitespace to be processed
         * @exception java.lang.Exception
         */
        public void processWhitespace(char[] chars, int offset, int length) throws Exception;

        /**
         * Process white space data, <code>StringPool</code> handle version
         *
         * @param stringHandle <code>StringPool</code> handle to the whitespace
         * @exception java.lang.Exception
         */
        public void processWhitespace(int stringHandle) throws Exception;
    }

    /**
     * This is the interface for scanners to process input data
     * from entities without needing to know the details of the
     * underlying storage of those entities, or their encodings.
     *
     * The methods in this interface have been refined over time
     * to a rough balance between keeping the XML grammar dependent
     * code within the scanner classes, and allowing high performance
     * processing of XML documents.
     */
    public interface EntityReader {
        /**
         * Return the current offset within this reader.
         *
         * @return The offset.
         */
        public int currentOffset();

        /**
         * Return the line number of the current position within the document that we are processing.
         *
         * @return The current line number.
         */
        public int getLineNumber();

        /**
         * Return the column number of the current position within the document that we are processing.
         *
         * @return The current column number.
         */
        public int getColumnNumber();

        /**
         * This method is provided for scanner implementations.
         */
        public void setInCDSect(boolean inCDSect);

        /**
         * This method is provided for scanner implementations.
         */
        public boolean getInCDSect();

        /**
         * Append the characters processed by this reader associated with <code>offset</code> and
         * <code>length</code> to the <code>CharBuffer</code>.
         *
         * @param charBuffer The <code>CharBuffer</code> to append the characters to.
         * @param offset The offset within this reader where the copy should start.
         * @param length The length within this reader where the copy should stop.
         */
        public void append(XMLEntityHandler.CharBuffer charBuffer, int offset, int length);

        /**
         * Add a string to the <code>StringPool</code> from the characters scanned using this
         * reader as described by <code>offset</code> and <code>length</code>.
         *
         * @param offset The offset within this reader where the characters start.
         * @param length The length within this reader where the characters end.
         * @return The <code>StringPool</code> handle for the string.
         */
        public int addString(int offset, int length);

        /**
         * Add a symbol to the <code>StringPool</code> from the characters scanned using this
         * reader as described by <code>offset</code> and <code>length</code>.
         *
         * @param offset The offset within this reader where the characters start.
         * @param length The length within this reader where the characters end.
         * @return The <code>StringPool</code> handle for the symbol.
         */
        public int addSymbol(int offset, int length);

        /**
         * Test that the current character is a <code>ch</code> character.
         *
         * @param ch The character to match against.
         * @param skipPastChar If <code>true</code>, we advance past the matched character.
         * @return <code>true</code> if the current character is a <code>ch</code> character;
         *         <code>false</code> otherwise.
         * @exception java.lang.Exception
         */
        public boolean lookingAtChar(char ch, boolean skipPastChar) throws Exception;

        /**
         * Test that the current character is valid.
         *
         * @param skipPastChar If <code>true</code>, we advance past the valid character.
         * @return <code>true</code> if the current character is valid;
         *         <code>false</code> otherwise.
         * @exception java.lang.Exception
         */
        public boolean lookingAtValidChar(boolean skipPastChar) throws Exception;

        /**
         * Test that the current character is a whitespace character.
         *
         * @param skipPastChar If <code>true</code>, we advance past the whitespace character.
         * @return <code>true</code> if the current character is whitespace;
         *         <code>false</code> otherwise.
         * @exception java.lang.Exception
         */
        public boolean lookingAtSpace(boolean skipPastChar) throws Exception;

        /**
         * Advance through the input data up to the next <code>ch</code> character.
         *
         * @param ch The character to search for.
         * @exception java.lang.Exception
         */
        public void skipToChar(char ch) throws Exception;

        /**
         * Skip past whitespace characters starting at the current position.
         * @exception java.lang.Exception
         */
        public void skipPastSpaces() throws Exception;

        /**
         * Skip past a sequence of characters that match the XML definition of a Name.
         * @exception java.lang.Exception
         */
        public void skipPastName(char fastcheck) throws Exception;

        /**
         * Skip past a sequence of characters that match the XML definition of an Nmtoken.
         * @exception java.lang.Exception
         */
        public void skipPastNmtoken(char fastcheck) throws Exception;

        /**
         * Skip past a sequence of characters that matches the specified character array.
         *
         * @param s The characters to match.
         * @return <code>true</code> if the current character is valid;
         *         <code>false</code> otherwise.
         * @exception java.lang.Exception
         */
        public boolean skippedString(char[] s) throws Exception;

        /**
         * Scan an invalid character.
         *
         * @return The invalid character as an integer, or -1 if there was a bad encoding.
         * @exception java.lang.Exception
         */
        public int scanInvalidChar() throws Exception;

        /**
         * Scan a character reference.
         *
         * @return The value of the character, or one of the following error codes:
         *
         *   CHARREF_RESULT_SEMICOLON_REQUIRED
         *   CHARREF_RESULT_INVALID_CHAR
         *   CHARREF_RESULT_OUT_OF_RANGE
         * @exception java.lang.Exception
         */
        public int scanCharRef(boolean isHexadecimal) throws Exception;

        /**
         * Scan a string literal.
         *
         * @return The <code>StringPool</code> handle for the string that
         *         was scanned, or one of the following error codes:
         *
         *   STRINGLIT_RESULT_QUOTE_REQUIRED
         *   STRINGLIT_RESULT_INVALID_CHAR
         * @exception java.lang.Exception
         */
        public int scanStringLiteral() throws Exception;

        /**
         * Scan an attribute value.
         *
         * @param qchar The initial quote character, either a single or double quote.
         * @return The <code>StringPool</code> handle for the string that
         *         was scanned, or one of the following error codes:
         *
         *   ATTVALUE_RESULT_COMPLEX
         *   ATTVALUE_RESULT_LESSTHAN
         *   ATTVALUE_RESULT_INVALID_CHAR
         * @exception java.lang.Exception
         */
        public int scanAttValue(char qchar, boolean asSymbol) throws Exception;

        /**
         * Scan an entity value.
         *
         * @param qchar The initial quote character, either a single or double quote.
         * @return The <code>StringPool</code> handle for the string that
         *         was scanned, or one of the following error codes:
         *
         *   ENTITYVALUE_RESULT_FINISHED
         *   ENTITYVALUE_RESULT_REFERENCE
         *   ENTITYVALUE_RESULT_PEREF
         *   ENTITYVALUE_RESULT_INVALID_CHAR
         *   ENTITYVALUE_RESULT_END_OF_INPUT
         * @exception java.lang.Exception
         */
        public int scanEntityValue(int qchar, boolean createString) throws Exception;

        /**
         * Add a sequence of characters that match the XML definition of a Name to the <code>StringPool</code>.
         *
         * If we find a name at the current position we will add it to the <code>StringPool</code>
         * as a symbol and will return the string pool handle for that symbol to the caller.
         *
         * @param fastcheck A character that is not a legal name character that is provided as a
         *                  hint to the reader of a character likely to terminate the Name.
         * @return The <code>StringPool</code> handle for the name that was scanned,
         *         or -1 if a name was not found at the current position within the input data.
         * @exception java.lang.Exception
         */
        public int scanName(char fastcheck) throws Exception;

        /**
         * Scan the name that is expected at the current position in the document.
         *
         * This method is invoked when we are scanning the element type in an end tag
         * that must match the element type in the corresponding start tag.
         *
         * @param fastcheck A character that is not a legal name character that is provided as a
         *                  hint to the reader of a character likely to terminate the Name.
         * @param expectedName The characters of the name we expect.
         * @return <code>true</code> if we scanned the name we expected to find; otherwise
         *         <code>false</code> if we did not.
         * @exception java.lang.Exception
         */
        public boolean scanExpectedName(char fastcheck, StringPool.CharArrayRange expectedName) throws Exception;

        /**
         * Add a sequence of characters that match the XML Namespaces definition of a QName
         * to the <code>StringPool</code>.
         *
         * If we find a QName at the current position we will add it to the <code>StringPool</code>
         * and will return the string pool handle of that QName to the caller.
         *
         * @param fastcheck A character that is not a legal name character that is provided as a
         *                  hint to the reader of a character likely to terminate the Name.
         * @return The <code>StringPool</code> handle for the QName that was scanned,
         *         or -1 if a name was not found at the current position within the input data.
         * @exception java.lang.Exception
         */
        public int scanQName(char fastcheck) throws Exception;

        /**
         * Skip through the input while we are looking at character data.
         *
         * @param elementType The element type handle in the StringPool.
         * @return One of the following result codes:
         *
         *   CONTENT_RESULT_START_OF_PI
         *   CONTENT_RESULT_START_OF_COMMENT
         *   CONTENT_RESULT_START_OF_CDSECT
         *   CONTENT_RESULT_END_OF_CDSECT
         *   CONTENT_RESULT_START_OF_ETAG
         *   CONTENT_RESULT_MATCHING_ETAG
         *   CONTENT_RESULT_START_OF_ELEMENT
         *   CONTENT_RESULT_START_OF_CHARREF
         *   CONTENT_RESULT_START_OF_ENTITYREF
         *   CONTENT_RESULT_INVALID_CHAR
         *   CONTENT_RESULT_MARKUP_NOT_RECOGNIZED
         *   CONTENT_RESULT_MARKUP_END_OF_INPUT
         *   CONTENT_RESULT_REFERENCE_END_OF_INPUT
         * @exception java.lang.Exception
         */
        public int scanContent(int elementType) throws Exception;
    }
}
