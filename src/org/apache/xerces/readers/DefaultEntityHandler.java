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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Stack;
import java.util.Vector;

import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.ImplementationMessages;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.URI;
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * Default entity handler implementation.
 *
 * @version $Id$
 */
public class DefaultEntityHandler 
    implements XMLEntityHandler, XMLEntityHandler.DTDHandler {

    public interface EventHandler {
        public void startEntityReference(int entityName, int entityType, int entityContext) throws Exception;
        public void endEntityReference(int entityName, int entityType, int entityContext) throws Exception;
        public void sendEndOfInputNotifications(int entityName, boolean moreToFollow) throws Exception;
        public void sendReaderChangeNotifications(XMLEntityHandler.EntityReader reader, int readerId) throws Exception;
        public boolean externalEntityStandaloneCheck();
        public boolean getValidating();
    }

    //
    // Data
    //
    private class ReaderState {
        XMLEntityHandler.EntityReader reader;
        InputSource source;
        int entityName;
        int entityType;
        int entityContext;
        String publicId;
        String systemId;
        int readerId;
        int depth;
        ReaderState nextReaderState;
    }
    private ReaderState fReaderStateFreeList = null;
    private StringPool fStringPool = null;
    private EventHandler fEventHandler = null;
    private XMLEntityHandler.CharDataHandler fCharDataHandler = null;
    private XMLErrorReporter fErrorReporter = null;
    private EntityResolver fResolver = null;
    private EntityPool fEntityPool = null;
    private EntityPool fParameterEntityPool = null;
    private byte[] fEntityTypeStack = null;
    private int[] fEntityNameStack = null;
    private int fEntityStackDepth = 0;
    private Stack fReaderStack = new Stack();
    private XMLEntityHandler.EntityReader fReader = null;
    private InputSource fSource = null;
    private int fEntityName = -1;
    private int fEntityType = -1;
    private int fEntityContext = -1;
    private String fPublicId = null;
    private String fSystemId = null;
    private int fReaderId = -1;
    private int fReaderDepth = -1;
    private int fNextReaderId = 0;
    private NullReader fNullReader = null;
    protected XMLEntityReaderFactory fReaderFactory = null;
    private boolean fSendCharDataAsCharArray = false;

    public DefaultEntityHandler(StringPool stringPool, XMLErrorReporter errorReporter) {
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fReaderFactory = new DefaultReaderFactory();
        fEntityPool = new EntityPool(fStringPool, fErrorReporter, true);
    }

    public void setEventHandler(EventHandler eventHandler) {
        fEventHandler = eventHandler;
    }

    public void setCharDataHandler(XMLEntityHandler.CharDataHandler charDataHandler) {
        fCharDataHandler = charDataHandler;
    }

    public XMLEntityHandler.CharDataHandler getCharDataHandler() {
        return fCharDataHandler;
    }

    /**
     * Set char data processing preference.
     */
    public void setSendCharDataAsCharArray(boolean flag) {
        fSendCharDataAsCharArray = flag;
        fReaderFactory.setSendCharDataAsCharArray(fSendCharDataAsCharArray);
    }

    /**
     * Set the reader factory.
     */
    public void setReaderFactory(XMLEntityReaderFactory readerFactory) {
        fReaderFactory = readerFactory;
        fReaderFactory.setSendCharDataAsCharArray(fSendCharDataAsCharArray);
    }

    /**
     * Reset the entity handler.
     */
    public void reset(StringPool stringPool) {
        fStringPool = stringPool;
        fEntityPool.reset(fStringPool);
        fParameterEntityPool = null;
        fReaderStack.removeAllElements();
        fEntityStackDepth = 0;
        fReader = null;
        fSource = null;
        fEntityName = -1;
        fEntityType = -1;
        fEntityContext = -1;
        fPublicId = null;
        fSystemId = null;
        fReaderId = -1;
        fReaderDepth = -1;
        fNextReaderId = 0;
    }

    /**
     *
     */
    public void setAllowJavaEncodings(boolean flag) {
        fReaderFactory.setAllowJavaEncodingName(flag);
    }
    /**
     *
     */
    public boolean getAllowJavaEncodings() {
        return fReaderFactory.getAllowJavaEncodingName();
    }

    //
    //
    //
    public int addInternalPEDecl(int name, int value, boolean isExternal) throws Exception {
        if (fParameterEntityPool == null)
            fParameterEntityPool = new EntityPool(fStringPool, fErrorReporter, false);
        int entityHandle = fParameterEntityPool.addEntityDecl(name, value, -1, -1, -1, -1, isExternal);
        return entityHandle;
    }
    public int addExternalPEDecl(int name, int publicId, int systemId, boolean isExternal) throws Exception {
        if (fParameterEntityPool == null)
            fParameterEntityPool = new EntityPool(fStringPool, fErrorReporter, false);
        int entityHandle = fParameterEntityPool.addEntityDecl(name, -1, publicId, systemId, fStringPool.addSymbol(fSystemId), -1, isExternal);
        return entityHandle;
    }
    public int addInternalEntityDecl(int name, int value, boolean isExternal) throws Exception {
        int entityHandle = fEntityPool.addEntityDecl(name, value, -1, -1, -1, -1, isExternal);
        return entityHandle;
    }
    public int addExternalEntityDecl(int name, int publicId, int systemId, boolean isExternal) throws Exception {
        int entityHandle = fEntityPool.addEntityDecl(name, -1, publicId, systemId, fStringPool.addSymbol(fSystemId), -1, isExternal);
        return entityHandle;
    }
    public int addUnparsedEntityDecl(int name, int publicId, int systemId, int notationName, boolean isExternal) throws Exception {
        int entityHandle = fEntityPool.addEntityDecl(name, -1, publicId, systemId, fStringPool.addSymbol(fSystemId), notationName, isExternal);
        if (!fEntityPool.isNotationDeclared(notationName)) {
            Object[] args = { fStringPool.toString(name),
                              fStringPool.toString(notationName) };
            fEntityPool.addRequiredNotation(notationName,
                                            fErrorReporter.getLocator(),
                                            XMLMessages.MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL,
                                            XMLMessages.VC_NOTATION_DECLARED,
                                            args);
        }
        return entityHandle;
    }
    public int addNotationDecl(int notationName, int publicId, int systemId, boolean isExternal) throws Exception {
        int notationHandle = fEntityPool.addNotationDecl(notationName, publicId, systemId, fStringPool.addSymbol(fSystemId), isExternal);
        return notationHandle;
    }
    public boolean isUnparsedEntity(int entityName) {
        int entityHandle = fEntityPool.lookupEntity(entityName);
        return (entityHandle != -1 && fEntityPool.isUnparsedEntity(entityHandle));
    }
    public boolean isNotationDeclared(int notationName) {
        return fEntityPool.isNotationDeclared(notationName);
    }
    public void addRequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args) {
        fEntityPool.addRequiredNotation(notationName, locator, majorCode, minorCode, args);
    }
    public void checkRequiredNotations() throws Exception {
        fEntityPool.checkRequiredNotations();
    }

    protected int lookupEntity(int entityNameIndex) {
        int entityIndex = fEntityPool.lookupEntity(entityNameIndex);
        return entityIndex;
    }
    private void reportRecoverableXMLError(int majorCode, int minorCode, int stringIndex1) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    public boolean externalReferenceInContent(int entityHandle) throws Exception {
        boolean external = fEntityPool.isExternalEntity(entityHandle);
        if (fEventHandler.externalEntityStandaloneCheck()) {
            if (external) {
                reportRecoverableXMLError(XMLMessages.MSG_EXTERNAL_ENTITY_NOT_PERMITTED,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                          fEntityName);
            } else if (fEntityPool.getEntityDeclIsExternal(entityHandle)) {
                reportRecoverableXMLError(XMLMessages.MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                          fEntityName);
            }
        }
        return external;
    }
    protected int valueOfReferenceInAttValue(int entityHandle) throws Exception {
        if (fEventHandler.externalEntityStandaloneCheck() && fEntityPool.getEntityDeclIsExternal(entityHandle)) {
            reportRecoverableXMLError(XMLMessages.MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE,
                                      XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                      fEntityName);
        }
        int entityValue = fEntityPool.getEntityValue(entityHandle);
        return entityValue;
    }
    protected boolean isExternalEntity(int entityHandle) {
        boolean external = fEntityPool.isExternalEntity(entityHandle);
        return external;
    }
    protected int getEntityValue(int entityHandle) {
        int value = fEntityPool.getEntityValue(entityHandle);
        return value;
    }
    protected String getPublicIdOfEntity(int entityHandle) {
        int publicId = fEntityPool.getPublicId(entityHandle);
        return fStringPool.toString(publicId);
    }
    protected String getSystemIdOfEntity(int entityHandle) {
        int systemId = fEntityPool.getSystemId(entityHandle);
        return fStringPool.toString(systemId);
    }
    protected int lookupParameterEntity(int peName) throws Exception {
        int entityHandle = -1;
        if (fParameterEntityPool != null)
            entityHandle = fParameterEntityPool.lookupEntity(peName);
        return entityHandle;
    }
    protected boolean isExternalParameterEntity(int peIndex) {
        boolean external = fParameterEntityPool.isExternalEntity(peIndex);
        return external;
    }
    protected int getParameterEntityValue(int peIndex) {
        int value = fParameterEntityPool.getEntityValue(peIndex);
        return value;
    }
    protected String getPublicIdOfParameterEntity(int peIndex) {
        int publicId = fParameterEntityPool.getPublicId(peIndex);
        return fStringPool.toString(publicId);
    }
    protected String getSystemIdOfParameterEntity(int peIndex) {
        int systemId = fParameterEntityPool.getSystemId(peIndex);
        return fStringPool.toString(systemId);
    }

    /**
     * get the Entity reader.
     */
    public XMLEntityHandler.EntityReader getEntityReader() {
        return fReader;
    }

    /**
     * Adds a recognizer.
     *
     * @param recognizer The XML recognizer to add.
     */
    public void addRecognizer(XMLDeclRecognizer recognizer) {
        fReaderFactory.addRecognizer(recognizer);
    }

    /**
     * Sets the resolver used to resolve external entities. The EntityResolver
     * interface supports resolution of public and system identifiers.
     *
     * @param resolver The new entity resolver. Passing a null value will
     *                 uninstall the currently installed resolver.
     */
    public void setEntityResolver(EntityResolver resolver) {
        fResolver = resolver;
    }

    /**
     * Gets the resolver used to resolve external entities. The EntityResolver
     * interface supports resolution of public and system identifiers.
     *
     * @return The current entity resolver.
     */
    public EntityResolver getEntityResolver() {
        return fResolver;
    }

    /**
     * Expands a system id and returns the system id as a URI, if
     * it can be expanded. A return value of null means that the
     * identifier is already expanded. An exception thrown
     * indicates a failure to expand the id.
     *
     * @param systemId The systemId to be expanded.
     *
     * @return Returns the URI string representing the expanded system
     *         identifier. A null value indicates that the given
     *         system identifier is already expanded.
     *
     */
    public String expandSystemId(String systemId) {
        return expandSystemId(systemId, fSystemId);
    }
    private String expandSystemId(String systemId, String currentSystemId) {
        String id = systemId;

        // check for bad parameters id
        if (id == null || id.length() == 0) {
            return systemId;
        }

        // if id already expanded, return
        try {
            URI uri = new URI(id);
            if (uri != null) {
                return systemId;
            }
        }
        catch (URI.MalformedURIException e) {
            // continue on...
        }

        // normalize id
        id = fixURI(id);

        // normalize base
        URI base = null;
        URI uri = null;
        try {
            if (currentSystemId == null) {
                String dir;
                try {
                    dir = fixURI(System.getProperty("user.dir"));
                }
                catch (SecurityException se) {
                    dir = "";
                }
                if (!dir.endsWith("/")) {
                    dir = dir + "/";
                }
                base = new URI("file", "", dir, null, null);
            }
            else {
                base = new URI(currentSystemId);
            }

            // expand id
            uri = new URI(base, id);
        }
        catch (Exception e) {
            // let it go through
        }
        if (uri == null) {
            return systemId;
        }
        return uri.toString();
    }

    //
    // Private methods
    //

    /**
     * Fixes a platform dependent filename to standard URI form.
     *
     * @param str The string to fix.
     *
     * @return Returns the fixed URI string.
     */
    private static String fixURI(String str) {

        // handle platform dependent strings
        str = str.replace(java.io.File.separatorChar, '/');

        // Windows fix
        if (str.length() >= 2) {
            char ch1 = str.charAt(1);
            if (ch1 == ':') {
                char ch0 = Character.toUpperCase(str.charAt(0));
                if (ch0 >= 'A' && ch0 <= 'Z') {
                    str = "/" + str;
                }
            }
        }

        // done
        return str;
    }

    public boolean startReadingFromDocument(InputSource source) throws Exception {
        pushEntity(false, -2); // Document Entity
        fSystemId = null;
        pushNullReader();
        fEntityName = -2; // Document Entity
        fEntityType = ENTITYTYPE_DOCUMENT;
        fEntityContext = ENTITYREF_DOCUMENT;
        fReaderDepth = 0;
        fReaderId = fNextReaderId++;
        fPublicId = source.getPublicId();
        fSystemId = source.getSystemId();
        fEventHandler.startEntityReference(fEntityName, fEntityType, fEntityContext);
        fSystemId = expandSystemId(fSystemId, null);
        fSource = source;
        boolean xmlDecl = true; // xmlDecl if true, textDecl if false
        try {
            fReader = fReaderFactory.createReader(this, fErrorReporter, source, fSystemId, xmlDecl, fStringPool);
        } catch (MalformedURLException mu) {
            String errorSystemId = fSystemId;
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            Object[] args = { errorSystemId };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                        ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                        ImplementationMessages.IO0,
                                        0,
                                        args,
                                        XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        } catch (FileNotFoundException fnf) {
            String errorSystemId = fSystemId;
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            Object[] args = { errorSystemId };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                        ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                        ImplementationMessages.IO0,
                                        0,
                                        args,
                                        XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        } catch (UnsupportedEncodingException uee) {
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            String encoding = uee.getMessage();
            if (encoding == null) {
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODING_REQUIRED,
                                           XMLMessages.P81_REQUIRED,
                                           null,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            } else if (!XMLCharacterProperties.validEncName(encoding)) {
                Object[] args = { encoding };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODINGDECL_INVALID,
                                           XMLMessages.P81_INVALID_VALUE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            } else {
                Object[] args = { encoding };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODING_NOT_SUPPORTED,
                                           XMLMessages.P81_NOT_SUPPORTED,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            }
        }
        fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
        return fReader != null;
    }
    /**
     * start reading from an external DTD subset
     */
    public void startReadingFromExternalSubset(String publicId, String systemId, int readerDepth) throws Exception {
        pushEntity(true, -1);
        pushReader();
        pushNullReader();
        fEntityName = -1; // External Subset
        fEntityType = ENTITYTYPE_EXTERNAL_SUBSET;
        fEntityContext = ENTITYREF_EXTERNAL_SUBSET;
        fReaderDepth = readerDepth;
        fReaderId = fNextReaderId++;
        fPublicId = publicId;
        fSystemId = systemId;
        startReadingFromExternalEntity(false, -1);
    }
    /**
     * stop reading from an external DTD subset
     */
    public void stopReadingFromExternalSubset() throws Exception {
        if (!(fReader instanceof NullReader))
            throw new RuntimeException("FWK004 cannot happen 18"+"\n18");
        popReader();
        fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
    }

    /**
     * start reading from an external entity
     */
    public boolean startReadingFromEntity(int entityName, int readerDepth, int context) throws Exception {
        if (context > XMLEntityHandler.ENTITYREF_IN_CONTENT)
            return startReadingFromParameterEntity(entityName, readerDepth, context);
        int entityHandle = lookupEntity(entityName);
        if (entityHandle < 0) {
            int minorCode = XMLMessages.VC_ENTITY_DECLARED;
            int errorType = XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR;
            // REVISIT - the following test in insufficient...
            if (fEntityContext == ENTITYREF_DOCUMENT || fEntityContext == ENTITYREF_IN_ATTVALUE) {
                minorCode = XMLMessages.WFC_ENTITY_DECLARED;
                errorType = XMLErrorReporter.ERRORTYPE_FATAL_ERROR;
            } else if (!fEventHandler.getValidating()) {
                return false;
            }
            Object[] args = { fStringPool.toString(entityName) };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_ENTITY_NOT_DECLARED,
                                       minorCode,
                                       args,
                                       errorType);
            return false;
        }
        if (context == ENTITYREF_IN_CONTENT) {
            if (fEntityPool.isUnparsedEntity(entityHandle)) {
                Object[] args = { fStringPool.toString(entityName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_REFERENCE_TO_UNPARSED_ENTITY,
                                           XMLMessages.WFC_PARSED_ENTITY,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
                return false;
            }
        } else {
            if (isExternalEntity(entityHandle)) {
                Object[] args = { fStringPool.toString(entityName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_REFERENCE_TO_EXTERNAL_ENTITY,
                                           XMLMessages.WFC_NO_EXTERNAL_ENTITY_REFERENCES,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
                return false;
            }
        }
        if (!pushEntity(false, entityName)) {
            Object[] args = { fStringPool.toString(entityName),
                              entityReferencePath(false, entityName) };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_RECURSIVE_REFERENCE,
                                       XMLMessages.WFC_NO_RECURSION,
                                       args,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            return false;
        }
        pushReader();
        fEntityName = entityName;
        fEntityContext = context;
        fReaderDepth = readerDepth;
        fReaderId = fNextReaderId++;
        if (context != ENTITYREF_IN_CONTENT || !externalReferenceInContent(entityHandle)) {
            fEntityType = ENTITYTYPE_INTERNAL;
            fPublicId = null/*"Internal Entity: " + fStringPool.toString(entityName)*/;
            fSystemId = fSystemId; // keep expandSystemId happy
            int value = -1;
            if (context == ENTITYREF_IN_CONTENT || context == ENTITYREF_IN_DEFAULTATTVALUE)
                value = getEntityValue(entityHandle);
            else
                value = valueOfReferenceInAttValue(entityHandle);
            startReadingFromInternalEntity(value, false);
            return false;
        }
        fEntityType = ENTITYTYPE_EXTERNAL;
        fPublicId = getPublicIdOfEntity(entityHandle);
        fSystemId = getSystemIdOfEntity(entityHandle);
        return startReadingFromExternalEntity(true, entityHandle);
    }
    private boolean startReadingFromParameterEntity(int peName, int readerDepth, int context) throws Exception {
        int entityHandle = lookupParameterEntity(peName);
        if (entityHandle == -1) {
            // strange... this is a VC, not a WFC...
            if (fEventHandler.getValidating()) {
                reportRecoverableXMLError(XMLMessages.MSG_ENTITY_NOT_DECLARED,
                                          XMLMessages.VC_ENTITY_DECLARED,
                                          peName);
            }
            return false;
        }
        if (!pushEntity(true, peName)) {
            Object[] args = { fStringPool.toString(peName),
                              entityReferencePath(true, peName) };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_RECURSIVE_PEREFERENCE,
                                       XMLMessages.WFC_NO_RECURSION,
                                       args,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            return false;
        }
        pushReader();
        fEntityName = peName;
        fEntityContext = context;
        fReaderDepth = readerDepth;
        fReaderId = fNextReaderId++;
        if (!isExternalParameterEntity(entityHandle)) {
            fEntityType = ENTITYTYPE_INTERNAL_PE;
            fPublicId = null/*"Internal Entity: %" + fStringPool.toString(peName)*/;
            fSystemId = fSystemId; // keep expandSystemId happy
            int value = getParameterEntityValue(entityHandle);
            startReadingFromInternalEntity(value, fEntityContext == ENTITYREF_IN_ENTITYVALUE ? false : true);
            return false;
        }
        fEntityType = ENTITYTYPE_EXTERNAL_PE;
        fPublicId = getPublicIdOfParameterEntity(entityHandle);
        fSystemId = getSystemIdOfParameterEntity(entityHandle);
        return startReadingFromExternalEntity(true, entityHandle);
    }
    private void startReadingFromInternalEntity(int value, boolean addSpaces) throws Exception {
        if (fEntityContext == ENTITYREF_IN_ENTITYVALUE) {
            //
            // REVISIT - consider optimizing the case where the entire entity value
            // consists of a single reference to a parameter entity and do not append
            // the value to fLiteralData again, but re-use the offset/length of the
            // referenced entity for the value of this entity.
            //
        }
        fSource = null;
        fEventHandler.startEntityReference(fEntityName, fEntityType, fEntityContext);
        fReader = fReaderFactory.createStringReader(this, fErrorReporter, fSendCharDataAsCharArray, getLineNumber(), getColumnNumber(), value, fStringPool, addSpaces); // REVISIT - string reader needs better location support
        fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
    }
    private boolean startReadingFromExternalEntity(boolean checkForTextDecl, int entityHandle) throws Exception {
        if (fEntityContext == ENTITYREF_IN_ENTITYVALUE) {
            //
            // REVISIT - Can we get the spec changed ?
            // There is a perverse edge case to handle here...  We have a reference
            // to an external PE within a literal EntityValue.  For the PE to be
            // well-formed, it must match the extPE production, but the code that
            // appends the replacement text to the entity value is in no position
            // to do a complete well-formedness check !!
            //
        }
        if (fEntityContext == ENTITYREF_IN_DTD_WITHIN_MARKUP) {
            //
            // REVISIT - Can we get the spec changed ?
            // There is a perverse edge case to handle here...  We have a reference
            // to an external PE within markup.  For the PE to be well-formed, it
            // must match the extPE production, which is probably not going to be
            // very useful expanded in the middle of a markup declaration.  The
            // problem is that an empty file, a file containing just whitespace or
            // another PE that is just empty or whitespace, matches extPE !!
            //
        }
        fEventHandler.startEntityReference(fEntityName, fEntityType, fEntityContext);
        String baseSystemId = null;
        if (entityHandle != -1) {
            if (fEntityType == ENTITYTYPE_EXTERNAL_PE)
                baseSystemId =
                    fParameterEntityPool.getBaseSystemId(entityHandle);
            else
                baseSystemId = fEntityPool.getBaseSystemId(entityHandle);
        }
        if (baseSystemId == null) {
            ReaderState rs = (ReaderState) fReaderStack.peek();
            baseSystemId = rs.systemId;
        }
        fSystemId = expandSystemId(fSystemId, baseSystemId);
        fSource = fResolver == null ? null : fResolver.resolveEntity(fPublicId, fSystemId);
        if (fSource == null) {
            fSource = new InputSource(fSystemId);
            if (fPublicId != null)
                fSource.setPublicId(fPublicId);
        } else {
            if (fSource.getSystemId() != null) {
                fSystemId =
                    expandSystemId(fSource.getSystemId(), baseSystemId);
            }
            if (fSource.getPublicId() != null) {
                fPublicId = fSource.getPublicId();
            }
        }

        boolean textDecl = false; // xmlDecl if true, textDecl if false
        try {
            fReader = fReaderFactory.createReader(this, fErrorReporter, fSource, fSystemId, textDecl, fStringPool);
        } catch (MalformedURLException mu) {
            String errorSystemId = fSystemId;
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            Object[] args = { errorSystemId };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                        ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                        ImplementationMessages.IO0,
                                        0,
                                        args,
                                        XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        } catch (FileNotFoundException fnf) {
            String errorSystemId = fSystemId;
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            Object[] args = { errorSystemId };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                        ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                        ImplementationMessages.IO0,
                                        0,
                                        args,
                                        XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        } catch (UnsupportedEncodingException uee) {
            fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
            popReader();
            popEntity();
            fReader = null;
            String encoding = uee.getMessage();
            if (encoding == null) {
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODING_REQUIRED,
                                           XMLMessages.P81_REQUIRED,
                                           null,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            } else if (!XMLCharacterProperties.validEncName(encoding)) {
                Object[] args = { encoding };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODINGDECL_INVALID,
                                           XMLMessages.P81_INVALID_VALUE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            } else {
                Object[] args = { encoding };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ENCODING_NOT_SUPPORTED,
                                           XMLMessages.P81_NOT_SUPPORTED,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            }
        }
        if (fReader == null || !checkForTextDecl) {
            fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
            return false;
        }
        int readerId = fReaderId;
        fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
        boolean parseTextDecl = fReader.lookingAtChar('<', false);
        if (readerId != fReaderId)
            parseTextDecl = false;
        return parseTextDecl;
    }

    //
    // reader stack
    //
    private void pushNullReader() {
        ReaderState rs = fReaderStateFreeList;
        if (rs == null)
            rs = new ReaderState();
        else
            fReaderStateFreeList = rs.nextReaderState;
        if (fNullReader == null)
            fNullReader = new NullReader();
        rs.reader = fNullReader;
        rs.source = null;
        rs.entityName = -1; // Null Entity
        rs.entityType = -1; // Null Entity
        rs.entityContext = -1; // Null Entity
        rs.publicId = "Null Entity";
        rs.systemId = fSystemId;
        rs.readerId = fNextReaderId++;
        rs.depth = -1;
        rs.nextReaderState = null;
        fReaderStack.push(rs);
    }
    private void pushReader() {
        ReaderState rs = fReaderStateFreeList;
        if (rs == null)
            rs = new ReaderState();
        else
            fReaderStateFreeList = rs.nextReaderState;
        rs.reader = fReader;
        rs.source = fSource;
        rs.entityName = fEntityName;
        rs.entityType = fEntityType;
        rs.entityContext = fEntityContext;
        rs.publicId = fPublicId;
        rs.systemId = fSystemId;
        rs.readerId = fReaderId;
        rs.depth = fReaderDepth;
        rs.nextReaderState = null;
        fReaderStack.push(rs);
    }
    private void popReader() {
        if (fReaderStack.empty())
            throw new RuntimeException("FWK004 cannot happen 19"+"\n19");
        ReaderState rs = (ReaderState) fReaderStack.pop();
        fReader = rs.reader;
        fSource = rs.source;
        fEntityName = rs.entityName;
        fEntityType = rs.entityType;
        fEntityContext = rs.entityContext;
        fPublicId = rs.publicId;
        fSystemId = rs.systemId;
        fReaderId = rs.readerId;
        fReaderDepth = rs.depth;
        rs.nextReaderState = fReaderStateFreeList;
        fReaderStateFreeList = rs;
    }

    /**
     * start an entity declaration
     */
    public boolean startEntityDecl(boolean isPE, int entityName) throws Exception {
        if (!pushEntity(isPE, entityName)) {
            int majorCode = isPE ? XMLMessages.MSG_RECURSIVE_PEREFERENCE : XMLMessages.MSG_RECURSIVE_REFERENCE;
            Object[] args = { fStringPool.toString(entityName),
                              entityReferencePath(isPE, entityName) };
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       majorCode,
                                       XMLMessages.WFC_NO_RECURSION,
                                       args,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            return false;
        }
        return true;
    }
    /**
     * end an entity declaration
     */
    public void endEntityDecl() throws Exception {
        popEntity();
    }
    //
    // entity stack
    //
    private boolean pushEntity(boolean isPE, int entityName) throws Exception {
        if (entityName >= 0) {
            for (int i = 0; i < fEntityStackDepth; i++) {
                if (fEntityNameStack[i] == entityName && fEntityTypeStack[i] == (isPE ? 1 : 0)) {
                    return false;
                }
            }
        }
        if (fEntityTypeStack == null) {
            fEntityTypeStack = new byte[8];
            fEntityNameStack = new int[8];
        } else if (fEntityStackDepth == fEntityTypeStack.length) {
            byte[] newTypeStack = new byte[fEntityStackDepth * 2];
            System.arraycopy(fEntityTypeStack, 0, newTypeStack, 0, fEntityStackDepth);
            fEntityTypeStack = newTypeStack;
            int[] newNameStack = new int[fEntityStackDepth * 2];
            System.arraycopy(fEntityNameStack, 0, newNameStack, 0, fEntityStackDepth);
            fEntityNameStack = newNameStack;
        }
        fEntityTypeStack[fEntityStackDepth] = (byte)(isPE ? 1 : 0);
        fEntityNameStack[fEntityStackDepth] = entityName;
        fEntityStackDepth++;
        return true;
    }
    private String entityReferencePath(boolean isPE, int entityName) {
        StringBuffer sb = new StringBuffer();
        sb.append("(top-level)");
        for (int i = 0; i < fEntityStackDepth; i++) {
            if (fEntityNameStack[i] >= 0) {
                sb.append('-');
                sb.append(fEntityTypeStack[i] == 1 ? '%' : '&');
                sb.append(fStringPool.toString(fEntityNameStack[i]));
                sb.append(';');
            }
        }
        sb.append('-');
        sb.append(isPE ? '%' : '&');
        sb.append(fStringPool.toString(entityName));
        sb.append(';');
        return sb.toString();
    }
    private void popEntity() throws Exception {
        fEntityStackDepth--;
    }

    //
    //
    //
    /**
     * This method is provided for scanner implementations.
     */
    public int getReaderId() {
        return fReaderId;
    }
    /**
     * This method is provided for scanner implementations.
     */
    public void setReaderDepth(int depth) {
        fReaderDepth = depth;
    }
    /**
     * This method is provided for scanner implementations.
     */
    public int getReaderDepth() {
        return fReaderDepth;
    }
    /**
     * Return the public identifier of the <code>InputSource</code> that we are processing.
     *
     * @return The public identifier, or null if not provided.
     */
    public String getPublicId() {
        return fPublicId;
    }
    /**
     * Return the system identifier of the <code>InputSource</code> that we are processing.
     *
     * @return The system identifier, or null if not provided.
     */
    public String getSystemId() {
        return fSystemId;
    }
    /**
     * Return the line number of the current position within the document that we are processing.
     *
     * @return The current line number.
     */
    public int getLineNumber() {
        return fReader == null ? 0 : fReader.getLineNumber();
    }
    /**
     * Return the column number of the current position within the document that we are processing.
     *
     * @return The current column number.
     */
    public int getColumnNumber() {
        return fReader == null ? 0 : fReader.getColumnNumber();
    }
    /**
     * This method is called by the reader subclasses at the
     * end of input, and also by the scanner directly to force
     * a reader change during error recovery.
     */
    public XMLEntityHandler.EntityReader changeReaders() throws Exception {
        fEventHandler.sendEndOfInputNotifications(fEntityName, fReaderStack.size() > 1);
        fEventHandler.endEntityReference(fEntityName, fEntityType, fEntityContext);
        popReader();
        fEventHandler.sendReaderChangeNotifications(fReader, fReaderId);
        popEntity();
        return fReader;
    }

    //
    // We use the null reader after we have reached the
    // end of input for the document or external subset.
    //
    private final class NullReader implements XMLEntityHandler.EntityReader {
        //
        //
        //
        public NullReader() {
        }
        public int currentOffset() {
            return -1;
        }
        public int getLineNumber() {
            return -1;
        }
        public int getColumnNumber() {
            return -1;
        }
        public void setInCDSect(boolean inCDSect) {
        }
        public boolean getInCDSect() {
            return false;
        }
        public void append(XMLEntityHandler.CharBuffer charBuffer, int offset, int length) {
        }
        public int addString(int offset, int length) {
            return -1;
        }
        public int addSymbol(int offset, int length) {
            return -1;
        }
        public boolean lookingAtChar(char ch, boolean skipPastChar) {
            return false;
        }
        public boolean lookingAtValidChar(boolean skipPastChar) {
            return false;
        }
        public boolean lookingAtSpace(boolean skipPastChar) {
            return false;
        }
        public void skipToChar(char ch) {
        }
        public void skipPastSpaces() {
        }
        public void skipPastName(char fastcheck) {
        }
        public void skipPastNmtoken(char fastcheck) {
        }
        public boolean skippedString(char[] s) {
            return false;
        }
        public int scanInvalidChar() {
            return -1;
        }
        public int scanCharRef(boolean hex) {
            return XMLEntityHandler.CHARREF_RESULT_INVALID_CHAR;
        }
        public int scanStringLiteral() {
            return XMLEntityHandler.STRINGLIT_RESULT_QUOTE_REQUIRED;
        }
        public int scanAttValue(char qchar, boolean asSymbol) {
            return XMLEntityHandler.ATTVALUE_RESULT_INVALID_CHAR;
        }
        public int scanEntityValue(int qchar, boolean createString) {
            return XMLEntityHandler.ENTITYVALUE_RESULT_INVALID_CHAR;
        }
        public boolean scanExpectedName(char fastcheck, StringPool.CharArrayRange expectedName) {
            return false;
        }
        public void scanQName(char fastcheck, QName qname) {
            qname.clear();
        }
        public int scanName(char fastcheck) {
            return -1;
        }
        public int scanContent(QName element) throws Exception {
            return XMLEntityHandler.CONTENT_RESULT_INVALID_CHAR;
        }
    }

    //
    // Entity Pool
    //

    //
    // Chunk size constants
    //
    static final int CHUNK_SHIFT = 5;           // 2^5 = 32
    static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    static final int CHUNK_MASK = CHUNK_SIZE - 1;
    static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));   // 2^10 = 1k

public final class EntityPool {
    //
    // Constants
    //

    //
    // Instance variables
    //
    private StringPool fStringPool = null;
    private XMLErrorReporter fErrorReporter = null;
    //
    // We store both EntityDecl and NotationDecl instances in this pool.
    // A NotationDecl has an fName field of -1.  The fNotationDeclHead
    // index is -1 if the NotationDecl list is empty, otherwise it contains
    // the index of the the last NotationDecl in the list and the fValue
    // field contains the index of the previous NotationDecl, or -1 when at
    // the end of the list.
    //
    private int fEntityCount = 0;
    private int[][] fName = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fValue = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fPublicId = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fSystemId = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fBaseSystemId = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fNotationName = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fDeclIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int fNotationListHead = -1;
    private boolean fCreateStandardEntities = false;
    private Vector fRequiredNotations = null;
    //
    // Constructor
    //
    public EntityPool(StringPool stringPool, XMLErrorReporter errorReporter, boolean createStandardEntities) {
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fCreateStandardEntities = createStandardEntities;
        if (fCreateStandardEntities) {
            createInternalEntity("lt", "&#60;");
            createInternalEntity("gt", ">");
            createInternalEntity("amp", "&#38;");
            createInternalEntity("apos", "\'");
            createInternalEntity("quot", "\"");
        }
    }
    //
    //
    //
    public void reset(StringPool stringPool) {
        fStringPool = stringPool;
        fEntityCount = 0;
        fNotationListHead = -1;
        if (fRequiredNotations != null)
            fRequiredNotations.removeAllElements();
        if (fCreateStandardEntities) {
            createInternalEntity("lt", "&#60;");
            createInternalEntity("gt", ">");
            createInternalEntity("amp", "&#38;");
            createInternalEntity("apos", "\'");
            createInternalEntity("quot", "\"");
        }
    }
    //
    //
    //
    private void createInternalEntity(String name, String value) {
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = fStringPool.addSymbol(name);
        fValue[chunk][index] = fStringPool.addString(value);
        fPublicId[chunk][index] = -1;
        fSystemId[chunk][index] = -1;
        fBaseSystemId[chunk][index] = -1;
        fNotationName[chunk][index] = -1;
        fEntityCount++;
    }
    //
    //
    //
    private boolean ensureCapacity(int chunk) {
        try {
            return fName[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fName, 0, newIntArray, 0, chunk);
            fName = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fValue, 0, newIntArray, 0, chunk);
            fValue = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fPublicId, 0, newIntArray, 0, chunk);
            fPublicId = newIntArray;

            newIntArray = new int[chunk * 2][];
            System.arraycopy(fSystemId, 0, newIntArray, 0, chunk);
            fSystemId = newIntArray;

            newIntArray = new int[chunk * 2][];
            System.arraycopy(fBaseSystemId, 0, newIntArray, 0, chunk);
            fBaseSystemId = newIntArray;


            newIntArray = new int[chunk * 2][];
            System.arraycopy(fNotationName, 0, newIntArray, 0, chunk);
            fNotationName = newIntArray;
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fDeclIsExternal, 0, newByteArray, 0, chunk);
            fDeclIsExternal = newByteArray;
        } catch (NullPointerException ex) {
        }
        fName[chunk] = new int[CHUNK_SIZE];
        fValue[chunk] = new int[CHUNK_SIZE];
        fPublicId[chunk] = new int[CHUNK_SIZE];
        fSystemId[chunk] = new int[CHUNK_SIZE];
        fBaseSystemId[chunk] = new int[CHUNK_SIZE];
        fNotationName[chunk] = new int[CHUNK_SIZE];
        fDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
        return true;
    }
    public int addEntityDecl(int name, int value, int publicId, int systemId, int baseSystemId, int notationName, boolean isExternal) {
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = name;
        fValue[chunk][index] = value;
        fPublicId[chunk][index] = publicId;
        fSystemId[chunk][index] = systemId;
        fBaseSystemId[chunk][index] = baseSystemId;
        fNotationName[chunk][index] = notationName;
        fDeclIsExternal[chunk][index] = isExternal ? (byte)0x80 : (byte)0;
        int entityIndex = fEntityCount++;
        return entityIndex;
    }
    public int addNotationDecl(int notationName, int publicId, int systemId, int baseSystemId, boolean isExternal) {
        int nIndex = fNotationListHead;
        while (nIndex != -1) {
            int chunk = nIndex >> CHUNK_SHIFT;
            int index = nIndex & CHUNK_MASK;
            if (fNotationName[chunk][index] == notationName)
                return -1;
            nIndex = fValue[chunk][index];
        }
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = -1;
        fValue[chunk][index] = fNotationListHead;
        fPublicId[chunk][index] = publicId;
        fSystemId[chunk][index] = systemId;
        fBaseSystemId[chunk][index] = baseSystemId;
        fNotationName[chunk][index] = notationName;
        fDeclIsExternal[chunk][index] = isExternal ? (byte)0x80 : (byte)0;
        fNotationListHead = fEntityCount++;
        return fNotationListHead;
    }
    public int lookupEntity(int nameIndex) {
        if (nameIndex == -1)
            return -1;
        int chunk = 0;
        int index = 0;
        for (int entityIndex = 0; entityIndex < fEntityCount; entityIndex++) {
            if (fName[chunk][index] == nameIndex)
                return entityIndex;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        return -1;
    }
    public boolean isExternalEntity(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fValue[chunk][index] == -1);
    }
    public boolean isUnparsedEntity(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fNotationName[chunk][index] != -1);
    }
    public boolean getEntityDeclIsExternal(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fDeclIsExternal[chunk][index] < 0);
    }
    public int getEntityName(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fName[chunk][index];
    }
    public int getEntityValue(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fValue[chunk][index];
    }
    public int getPublicId(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fPublicId[chunk][index];
    }
    public int getSystemId(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fSystemId[chunk][index];
    }
    public String getBaseSystemId(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        int baseIndex = fBaseSystemId[chunk][index];
        if (baseIndex == -1) {
            return null;
        } else {
            return fStringPool.toString(baseIndex);
        }
    }
    public boolean isNotationDeclared(int nameIndex) {
        int nIndex = fNotationListHead;
        while (nIndex != -1) {
            int chunk = nIndex >> CHUNK_SHIFT;
            int index = nIndex & CHUNK_MASK;
            if (fNotationName[chunk][index] == nameIndex)
                return true;
            nIndex = fValue[chunk][index];
        }
        return false;
    }
    public boolean getNotationDeclIsExternal(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fDeclIsExternal[chunk][index] < 0);
    }
    public int getNotationName(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fNotationName[chunk][index];
    }
    class RequiredNotation {
        RequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args) {
            fNotationName = notationName;
            fLocator = new LocatorImpl(locator); // snapshot of the current location
            fMajorCode = majorCode;
            fMinorCode = minorCode;
            fArgs = args;
        }
        int fNotationName;
        LocatorImpl fLocator;
        int fMajorCode;
        int fMinorCode;
        Object[] fArgs;
    };
    public void addRequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args) {
        if (fRequiredNotations == null)
            fRequiredNotations = new Vector();
        for (int index = 0; index < fRequiredNotations.size(); index++) {
            RequiredNotation rn = (RequiredNotation)fRequiredNotations.elementAt(index);
            if (rn.fNotationName == notationName)
                return; // REVISIT - do we want to keep just the first, or all of them?
        }
        fRequiredNotations.addElement(new RequiredNotation(notationName, locator, majorCode, minorCode, args));
    }
    public void checkRequiredNotations() throws Exception {
        if (fRequiredNotations == null)
            return;
        for (int index = 0; index < fRequiredNotations.size(); index++) {
            RequiredNotation rn = (RequiredNotation)fRequiredNotations.elementAt(index);
            if (!isNotationDeclared(rn.fNotationName)) {
                fErrorReporter.reportError(rn.fLocator,
                                           XMLMessages.XML_DOMAIN,
                                           rn.fMajorCode,
                                           rn.fMinorCode,
                                           rn.fArgs,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }
    }
}
}
