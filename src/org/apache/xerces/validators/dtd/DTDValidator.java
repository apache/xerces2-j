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

package org.apache.xerces.validators.dtd;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLContentSpecNode;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.framework.XMLValidator;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.ChunkyCharArray;
import org.apache.xerces.utils.NamespacesScope;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.utils.ImplementationMessages;

import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.util.StringTokenizer;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Hashtable;

// for scanning DTD
import org.apache.xerces.framework.XMLDTDScanner;
import org.xml.sax.misc.NamespaceHandler;

public final class DTDValidator
    implements XMLValidator,
               XMLDTDScanner.EventHandler,
               NamespacesScope.NamespacesHandler
{
    //
    // Debugging options
    //
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    private static final boolean DEBUG_PRINT_ATTRIBUTES = false;
    private static final boolean DEBUG_PRINT_CONTENT = false;
    //
    //
    //
    private XMLDTDScanner fDTDScanner = null;
    protected StringPool fStringPool = null; // protected for use by AttributeValidator classes.
    private XMLErrorReporter fErrorReporter = null;
    private XMLEntityHandler fEntityHandler = null;
    //
    //
    //
    protected boolean fValidating = false; // protected for use by AttributeValidator classes.
    private boolean fValidationEnabled = false;
    private boolean fDynamicValidation = false;
    private boolean fValidationEnabledByDynamic = false;
    private boolean fDynamicDisabledByValidation = false;
    private boolean fWarningOnDuplicateAttDef = false;
    private boolean fWarningOnUndeclaredElements = false;
    private EntityPool fEntityPool = null;
    private int fStandaloneReader = -1;
    private int[] fElementTypeStack = new int[8];
    private int[] fElementIndexStack = new int[8];
    private int[] fContentSpecTypeStack = new int[8];
    private int[] fElementChildCount = new int[8];
    private int[][] fElementChildren = new int[8][];
    private int fElementDepth = -1;
    private boolean fNamespacesEnabled = false;
    private NamespacesScope fNamespacesScope = null;
    private int fNamespacesPrefix = -1;
    private int fRootElementType = -1;
    private int fAttrIndex = -1;
    private int fElementDeclCount = 0;
    private int fAttlistDeclCount = 0;
    private int fCurrentElementType = -1;
    private int fCurrentElementIndex = -1;
    private int fCurrentContentSpecType = -1;
    //
    private boolean fSeenDoctypeDecl = false;
    private EventHandler fEventHandler = null;
    private EntityPool fParameterEntityPool = null;
    private int fEMPTYSymbol = -1;
    private int fANYSymbol = -1;
    private int fMIXEDSymbol = -1;
    private int fCHILDRENSymbol = -1;
    private int fCDATASymbol = -1;
    private int fIDSymbol = -1;
    private int fIDREFSymbol = -1;
    private int fIDREFSSymbol = -1;
    private int fENTITYSymbol = -1;
    private int fENTITIESSymbol = -1;
    private int fNMTOKENSymbol = -1;
    private int fNMTOKENSSymbol = -1;
    private int fNOTATIONSymbol = -1;
    private int fENUMERATIONSymbol = -1;
    private int fREQUIREDSymbol = -1;
    private int fFIXEDSymbol = -1;
    private int fEpsilonIndex = -1;
    //
    //
    //
    public interface EventHandler {
        /**
         * Tell the handler that validation is happening
         *
         * @param flag true if validation is occuring
         */
        public void setValidating(boolean flag) throws Exception;
        /**
         * callback for the start of the DTD
         * This function will be called when a &lt;!DOCTYPE...&gt; declaration is
         * encountered.
         *
         * @param rootElementType element handle for the root element of the document
         * @param publicId string pool index of the DTD's public ID
         * @param systemId string pool index of the DTD's system ID
         * @exception java.lang.Exception
         */
        public void startDTD(int rootElementType, int publicId, int systemId) throws Exception;
        /** 
         * callback for the end of the DTD
         * This function will be called at the end of the DTD. 
         */
        public void endDTD() throws Exception;
        /**
         * callback for an element declaration. 
         *
         * @param elementType element handle of the element being declared
         * @param contentSpec contentSpec for the element being declared
         * @see org.apache.xerces.framework.XMLValidator.ContentSpec
         * @exception java.lang.Exception
         */
        public void elementDecl(int elementType, XMLValidator.ContentSpec contentSpec) throws Exception;
        /** 
         * callback for an attribute list declaration. 
         *
         * @param elementType element handle for the attribute's element
         * @param attrName string pool index of the attribute name
         * @param attType type of attribute
         * @param enumString String representing the values of the enumeration,
         *        if the attribute is of enumerated type, or null if it is not.
         * @param attDefaultType an integer value denoting the DefaultDecl value
         * @param attDefaultValue string pool index of this attribute's default value 
         *        or -1 if there is no defaultvalue 
         * @exception java.lang.Exception
         */
        public void attlistDecl(int elementType,
                                int attrName,
                                int attType,
                                String enumString,
                                int attDefaultType,
                                int attDefaultValue) throws Exception;
        /** 
         * callback for an internal parameter entity declaration.
         *
         * @param entityName string pool index of the entity name
         * @param entityValue string pool index of the entity replacement text
         * @exception java.lang.Exception
         */
        public void internalPEDecl(int entityName, int entityValue) throws Exception;
        /**
         * callback for an external parameter entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @exception java.lang.Exception
         */
        public void externalPEDecl(int entityName, int publicId, int systemId) throws Exception;
        /** 
         * callback for internal general entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param entityValue string pool index of the entity replacement text
         * @exception java.lang.Exception
         */
        public void internalEntityDecl(int entityName, int entityValue) throws Exception;
        /**
         * callback for external general entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @exception java.lang.Exception
         */
        public void externalEntityDecl(int entityName, int publicId, int systemId) throws Exception;
        /** 
         * callback for an unparsed entity declaration. 
         *
         * @param entityName string pool index of the entity name
         * @param publicId string pool index of the entity's public id.
         * @param systemId string pool index of the entity's system id.
         * @param notationName string pool index of the notation name.
         * @exception java.lang.Exception
         */
        public void unparsedEntityDecl(int entityName, int publicId, int systemId, int notationName) throws Exception;
        /**
         * callback for a notation declaration.
         *
         * @param notationName string pool index of the notation name
         * @param publicId string pool index of the notation's public id.
         * @param systemId string pool index of the notation's system id.
         * @exception java.lang.Exception
         */
        public void notationDecl(int notationName, int publicId, int systemId) throws Exception;
        /**
         * Callback for comment in DTD.
         * 
         * @param comment the string pool index of the comment text
         * @exception java.lang.Exception
         */
        public void commentInDTD(int dataIndex) throws Exception;
        /** 
         * Callback for processing instruction in DTD.  
         *
         * @param target the string pool index of the PI's target
         * @param data the string pool index of the PI's data
         * @exception java.lang.Exception
         */
        public void processingInstructionInDTD(int targetIndex, int dataIndex) throws Exception;
        /** 
         * callback for the start of a namespace declaration scope. 
         *
         * @param prefix string pool index of the namespace prefix being declared
         * @param uri string pool index of the namespace uri begin bound
         * @param java.lang.Exception
         */
        public void startNamespaceDeclScope(int prefix, int uri) throws Exception;
        /**
         * callback for the end a namespace declaration scope. 
         * 
         * @param prefix string pool index of the namespace prefix being declared
         * @exception java.lang.Exception
         */
        public void endNamespaceDeclScope(int prefix) throws Exception;
        
        /**
         * Supports DOM Level 2 internalSubset addition.
         * Called when the internal subset is completely scanned.
         */
        public void internalSubset(int internalSubset);
        
    }
    //
    //
    //
    public DTDValidator(EventHandler eventHandler,
                        StringPool stringPool,
                        XMLErrorReporter errorReporter,
                        XMLEntityHandler entityHandler)
    {
        fEventHandler = eventHandler;
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fDTDScanner = new XMLDTDScanner(this, fStringPool, fErrorReporter, fEntityHandler, new ChunkyCharArray(fStringPool));
        fEntityPool = new EntityPool(fStringPool, fErrorReporter, true);
        init();
    }
    //
    //
    //
    public void reset(StringPool stringPool) throws Exception
    {
        fStringPool = stringPool;
        fDTDScanner.reset(stringPool, new ChunkyCharArray(fStringPool));
        setValidating(fValidationEnabled);
        fValidationEnabledByDynamic = false;
        fDynamicDisabledByValidation = false;
        fEntityPool.reset(fStringPool);
        poolReset();
        fStandaloneReader = -1;
        fElementDepth = -1;
        fSeenDoctypeDecl = false;
        fParameterEntityPool = null;
        fNamespacesScope = null;
        fNamespacesPrefix = -1;
        fRootElementType = -1;
        fAttrIndex = -1;
        fElementDeclCount = 0;
        fAttlistDeclCount = 0;
        init();
    }
    //
    //
    //
    private void init() {
        fEMPTYSymbol = fStringPool.addSymbol("EMPTY");
        fANYSymbol = fStringPool.addSymbol("ANY");
        fMIXEDSymbol = fStringPool.addSymbol("MIXED");
        fCHILDRENSymbol = fStringPool.addSymbol("CHILDREN");
        fCDATASymbol = fStringPool.addSymbol("CDATA");
        fIDSymbol = fStringPool.addSymbol("ID");
        fIDREFSymbol = fStringPool.addSymbol("IDREF");
        fIDREFSSymbol = fStringPool.addSymbol("IDREFS");
        fENTITYSymbol = fStringPool.addSymbol("ENTITY");
        fENTITIESSymbol = fStringPool.addSymbol("ENTITIES");
        fNMTOKENSymbol = fStringPool.addSymbol("NMTOKEN");
        fNMTOKENSSymbol = fStringPool.addSymbol("NMTOKENS");
        fNOTATIONSymbol = fStringPool.addSymbol("NOTATION");
        fENUMERATIONSymbol = fStringPool.addSymbol("ENUMERATION");
        fREQUIREDSymbol = fStringPool.addSymbol("#REQUIRED");
        fFIXEDSymbol = fStringPool.addSymbol("#FIXED");
        fEpsilonIndex = fStringPool.addSymbol("<<CMNODE_EPSILON>>");
    }
    //
    // Turning on validation/dynamic turns on validation if it is off, and this
    // is remembered.  Turning off validation DISABLES validation/dynamic if it
    // is on.  Turning off validation/dynamic DOES NOT turn off validation if it
    // was explicitly turned on, only if it was turned on BECAUSE OF the call to
    // turn validation/dynamic on.  Turning on validation will REENABLE and turn
    // validation/dynamic back on if it was disabled by a call that turned off
    // validation while validation/dynamic was enabled.
    //
    public void setValidationEnabled(boolean flag) throws Exception {
        fValidationEnabled = flag;
        fValidationEnabledByDynamic = false;
        if (fValidationEnabled) {
            if (fDynamicDisabledByValidation) {
                fDynamicValidation = true;
                fDynamicDisabledByValidation = false;
            }
        } else if (fDynamicValidation) {
            fDynamicValidation = false;
            fDynamicDisabledByValidation = true;
        }
        setValidating(fValidationEnabled);
    }
    public boolean getValidationEnabled() {
        return fValidationEnabled;
    }
    public void setDynamicValidationEnabled(boolean flag) throws Exception {
        fDynamicValidation = flag;
        fDynamicDisabledByValidation = false;
        if (!fDynamicValidation) {
            if (fValidationEnabledByDynamic) {
                fValidationEnabled = false;
                fValidationEnabledByDynamic = false;
            }
        } else if (!fValidationEnabled) {
            fValidationEnabled = true;
            fValidationEnabledByDynamic = true;
        }
        setValidating(fValidationEnabled);
    }
    public boolean getDynamicValidationEnabled() {
        return fDynamicValidation;
    }
    private void setValidating(boolean flag) throws Exception {
        fValidating = flag;
        fEventHandler.setValidating(flag);
    }
    public void setNamespacesEnabled(boolean flag) {
        fNamespacesEnabled = flag;
    }
    public boolean getNamespacesEnabled() {
        return fNamespacesEnabled;
    }
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception {
        fEventHandler.startNamespaceDeclScope(prefix, uri);
    }
    public void endNamespaceDeclScope(int prefix) throws Exception {
        fEventHandler.endNamespaceDeclScope(prefix);
    }
    public void internalSubset(int internalSubset) {
        fEventHandler.internalSubset(internalSubset);
    }
    
    public void setWarningOnDuplicateAttDef(boolean flag) {
        fWarningOnDuplicateAttDef = flag;
    }
    public boolean getWarningOnDuplicateAttDef() {
        return fWarningOnDuplicateAttDef;
    }
    public void setWarningOnUndeclaredElements(boolean flag) {
        fWarningOnUndeclaredElements = flag;
    }
    public boolean getWarningOnUndeclaredElements() {
        return fWarningOnUndeclaredElements;
    }
    private boolean usingStandaloneReader() {
        return fStandaloneReader == -1 || fEntityHandler.getReaderId() == fStandaloneReader;
    }
    protected boolean invalidStandaloneAttDef(int elementType, int attrName) {
        if (fStandaloneReader == -1)
            return false;
        if (elementType == -1) // we are normalizing a default att value...  this ok?
            return false;
        int attDefIndex = getAttDef(elementType, attrName);
        return getAttDefIsExternal(attDefIndex);
    }
    //
    // Validator interface
    //
    public boolean scanDoctypeDecl(boolean standalone) throws Exception {
        fSeenDoctypeDecl = true;
        fStandaloneReader = standalone ? fEntityHandler.getReaderId() : -1;
        if (!fDTDScanner.scanDoctypeDecl()) {
            return false;
        }
        if (fDTDScanner.getReadingExternalEntity()) {
            fDTDScanner.scanDecls(true);
        }
        if (fValidating) {
            // check declared elements
            if (fWarningOnUndeclaredElements)
                checkDeclaredElements();

            // check required notations
            fEntityPool.checkRequiredNotations();
        }
        fEventHandler.endDTD();
        return true;
    }
    public void characters(char[] chars, int offset, int length) throws Exception {
        if (fValidating) {
            charDataInContent();
        }
    }
    public void characters(int stringIndex) throws Exception {
        if (fValidating) {
            charDataInContent();
        }
    }
    public void ignorableWhitespace(char[] chars, int offset, int length) throws Exception {
        if (fStandaloneReader != -1 && fValidating) {
            int elementIndex = fCurrentElementIndex;
            if (getElementDeclIsExternal(elementIndex)) {
                reportRecoverableXMLError(XMLMessages.MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE,
                                            XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION);
            }
        }
    }
    public void ignorableWhitespace(int stringIndex) throws Exception {
        if (fStandaloneReader != -1 && fValidating) {
            int elementIndex = fCurrentElementIndex;
            if (getElementDeclIsExternal(elementIndex)) {
                reportRecoverableXMLError(XMLMessages.MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE,
                                            XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION);
            }
        }
    }
    public int lookupEntity(int entityNameIndex) {
        int entityIndex = fEntityPool.lookupEntity(entityNameIndex);
        return entityIndex;
    }
    public boolean externalReferenceInContent(int entityIndex) throws Exception {
        boolean external = fEntityPool.isExternalEntity(entityIndex);
        if (fStandaloneReader != -1 && fValidating) {
            if (external) {
                reportRecoverableXMLError(XMLMessages.MSG_EXTERNAL_ENTITY_NOT_PERMITTED,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                          fEntityPool.getEntityName(entityIndex));
            } else if (fEntityPool.getEntityDeclIsExternal(entityIndex)) {
                reportRecoverableXMLError(XMLMessages.MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                          fEntityPool.getEntityName(entityIndex));
            }
        }
        return external;
    }
    public int valueOfReferenceInAttValue(int entityIndex) throws Exception {
        if (fStandaloneReader != -1 && fValidating && fEntityPool.getEntityDeclIsExternal(entityIndex)) {
            reportRecoverableXMLError(XMLMessages.MSG_REFERENCE_TO_EXTERNALLY_DECLARED_ENTITY_WHEN_STANDALONE,
                                      XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                      fEntityPool.getEntityName(entityIndex));
        }
        int entityValue = fEntityPool.getEntityValue(entityIndex);
        return entityValue;
    }
    public boolean isExternalEntity(int entityIndex) {
        boolean external = fEntityPool.isExternalEntity(entityIndex);
        return external;
    }
    public boolean isUnparsedEntity(int entityIndex) {
        boolean external = fEntityPool.isUnparsedEntity(entityIndex);
        return external;
    }
    public int getEntityValue(int entityIndex) {
        int value = fEntityPool.getEntityValue(entityIndex);
        return value;
    }
    public String getPublicIdOfEntity(int entityIndex) {
        int publicId = fEntityPool.getPublicId(entityIndex);
        return fStringPool.toString(publicId);
    }
    public String getSystemIdOfEntity(int entityIndex) {
        int systemId = fEntityPool.getSystemId(entityIndex);
        return fStringPool.toString(systemId);
    }
    public int lookupParameterEntity(int peNameIndex) throws Exception {
        int peIndex = -1;
        if (fParameterEntityPool != null)
            peIndex = fParameterEntityPool.lookupEntity(peNameIndex);
        if (peIndex == -1 && fValidating) {
            reportRecoverableXMLError(XMLMessages.MSG_ENTITY_NOT_DECLARED,
                                      XMLMessages.VC_ENTITY_DECLARED,
                                      peNameIndex);
        }
        return peIndex;
    }
    public boolean isExternalParameterEntity(int peIndex) {
        boolean external = fParameterEntityPool.isExternalEntity(peIndex);
        return external;
    }
    public int getParameterEntityValue(int peIndex) {
        int value = fParameterEntityPool.getEntityValue(peIndex);
        return value;
    }
    public String getPublicIdOfParameterEntity(int peIndex) {
        int publicId = fParameterEntityPool.getPublicId(peIndex);
        return fStringPool.toString(publicId);
    }
    public String getSystemIdOfParameterEntity(int peIndex) {
        int systemId = fParameterEntityPool.getSystemId(peIndex);
        return fStringPool.toString(systemId);
    }
    public void rootElementSpecified(int rootElementType) throws Exception {
        if (fDynamicValidation && !fSeenDoctypeDecl) {
            setValidating(false);
        }
        if (fValidating) {
            if (fRootElementType != -1 && rootElementType != fRootElementType) {
                          reportRecoverableXMLError(XMLMessages.MSG_ROOT_ELEMENT_TYPE,
                                            XMLMessages.VC_ROOT_ELEMENT_TYPE,
                                            fRootElementType, rootElementType);
            }
        }
        if (fNamespacesEnabled) {
            if (fNamespacesScope == null) {
                fNamespacesScope = new NamespacesScope(this);
                fNamespacesPrefix = fStringPool.addSymbol("xmlns");
                fNamespacesScope.setNamespaceForPrefix(fNamespacesPrefix, -1);
                int xmlSymbol = fStringPool.addSymbol("xml");
                int xmlNamespace = fStringPool.addSymbol("http://www.w3.org/XML/1998/namespace");
                fNamespacesScope.setNamespaceForPrefix(xmlSymbol, xmlNamespace);
            }
        }
    }
    public boolean attributeSpecified(int elementType, XMLAttrList attrList, int attrName, Locator attrNameLocator, int attValue) throws Exception {
        if (!fValidating && fAttlistDeclCount == 0) {
            int attType = fCDATASymbol;
            if (fAttrIndex == -1)
                fAttrIndex = attrList.startAttrList();
            return attrList.addAttr(attrName, attValue, attType, true, true) != -1;
        }
        int attDefIndex = getAttDef(elementType, attrName);
        if (attDefIndex == -1) {
            if (fValidating) {
                // REVISIT - cache the elem/attr tuple so that we only give
                //  this error once for each unique occurrence
                Object[] args = { fStringPool.toString(elementType),
                                  fStringPool.toString(attrName) };
                fErrorReporter.reportError(attrNameLocator,
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ATTRIBUTE_NOT_DECLARED,
                                           XMLMessages.VC_ATTRIBUTE_VALUE_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
            int attType = fCDATASymbol;
            if (fAttrIndex == -1)
                fAttrIndex = attrList.startAttrList();
            return attrList.addAttr(attrName, attValue, attType, true, true) != -1;
        }
        int attType = getAttType(attDefIndex);
        if (attType != fCDATASymbol) {
            AttributeValidator av = getAttributeValidator(attDefIndex);
            int enumHandle = (attType == fNOTATIONSymbol || attType == fENUMERATIONSymbol) ?
                                getEnumeration(attDefIndex) : -1;
            attValue = av.normalize(elementType, attrName, attValue, attType, enumHandle);
        }
        if (fAttrIndex == -1)
            fAttrIndex = attrList.startAttrList();
        return attrList.addAttr(attrName, attValue, attType, true, true) != -1;
    }
    public boolean startElement(int elementType, XMLAttrList attrList) throws Exception {
        int attrIndex = fAttrIndex;
        fAttrIndex = -1;
        if (fElementDeclCount == 0 && fAttlistDeclCount == 0 && !fValidating && !fNamespacesEnabled) {
            return false;
        }
        int elementIndex = fStringPool.getDeclaration(elementType);
        int contentSpecType = (elementIndex == -1) ? -1 : getContentSpecType(elementIndex);
        if (contentSpecType == -1 && fValidating) {
            reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_NOT_DECLARED,
                                      XMLMessages.VC_ELEMENT_VALID,
                                      elementType);
        }
        if (fAttlistDeclCount != 0 && elementIndex != -1) {
            attrIndex = addDefaultAttributes(elementIndex, attrList, attrIndex, fValidating, fStandaloneReader != -1);
        }
        if (DEBUG_PRINT_ATTRIBUTES) {
            String element = fStringPool.toString(elementType);
            System.out.print("startElement: <" + element);
            if (attrIndex != -1) {
                int index = attrList.getFirstAttr(attrIndex);
                while (index != -1) {
                    System.out.print(" " + fStringPool.toString(attrList.getAttrName(index)) + "=\"" +
                            fStringPool.toString(attrList.getAttValue(index)) + "\"");
                    index = attrList.getNextAttr(index);
                }
            }
            System.out.println(">");
        }
        //
        // Namespace support
        //
        if (fNamespacesEnabled) {
            fNamespacesScope.increaseDepth();
            if (attrIndex != -1) {
                int index = attrList.getFirstAttr(attrIndex);
                while (index != -1) {
                    int attName = attrList.getAttrName(index);
                    if (fStringPool.equalNames(attName, fNamespacesPrefix)) {
                        int uri = fStringPool.addSymbol(attrList.getAttValue(index));
                        fNamespacesScope.setNamespaceForPrefix(StringPool.EMPTY_STRING, uri);
                    } else {
                        int attPrefix = fStringPool.getPrefixForQName(attName);
                        if (attPrefix == fNamespacesPrefix) {
                            attPrefix = fStringPool.getLocalPartForQName(attName);
                            int uri = fStringPool.addSymbol(attrList.getAttValue(index));
                            fNamespacesScope.setNamespaceForPrefix(attPrefix, uri);
                        }
                    }
                    index = attrList.getNextAttr(index);
                }
            }
            int prefix = fStringPool.getPrefixForQName(elementType);
            int elementURI;
            if (prefix == -1) {
                elementURI = fNamespacesScope.getNamespaceForPrefix(StringPool.EMPTY_STRING);
                if (elementURI != -1) {
                    fStringPool.setURIForQName(elementType, elementURI);
                }
            } else {
                elementURI = fNamespacesScope.getNamespaceForPrefix(prefix);
                if (elementURI == -1) {
                    Object[] args = { fStringPool.toString(prefix) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XMLNS_DOMAIN,
                                               XMLMessages.MSG_PREFIX_DECLARED,
                                               XMLMessages.NC_PREFIX_DECLARED,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                }
                fStringPool.setURIForQName(elementType, elementURI);
            }
            if (attrIndex != -1) {
                int index = attrList.getFirstAttr(attrIndex);
                while (index != -1) {
                    int attName = attrList.getAttrName(index);
                    if (!fStringPool.equalNames(attName, fNamespacesPrefix)) {
                        int attPrefix = fStringPool.getPrefixForQName(attName);
                        if (attPrefix != fNamespacesPrefix) {
                            if (attPrefix == -1) {
                                fStringPool.setURIForQName(attName, elementURI);
                            } else {
                                int uri = fNamespacesScope.getNamespaceForPrefix(attPrefix);
                                if (uri == -1) {
                                    Object[] args = { fStringPool.toString(attPrefix) };
                                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                               XMLMessages.XMLNS_DOMAIN,
                                                               XMLMessages.MSG_PREFIX_DECLARED,
                                                               XMLMessages.NC_PREFIX_DECLARED,
                                                               args,
                                                               XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                                }
                                fStringPool.setURIForQName(attName, uri);
                            }
                        }
                    }
                    index = attrList.getNextAttr(index);
                }
            }
        }
        if (fElementDepth >= 0) {
            int[] children = fElementChildren[fElementDepth];
            int childCount = fElementChildCount[fElementDepth];
            try {
                children[childCount] = elementType;
            } catch (NullPointerException ex) {
                children = fElementChildren[fElementDepth] = new int[256];
                childCount = 0; // should really assert this...
                children[childCount] = elementType;
            } catch (ArrayIndexOutOfBoundsException ex) {
                int[] newChildren = new int[childCount * 2];
                System.arraycopy(children, 0, newChildren, 0, childCount);
                children = fElementChildren[fElementDepth] = newChildren;
                children[childCount] = elementType;
            }
            fElementChildCount[fElementDepth] = ++childCount;
        }
        fElementDepth++;
        if (fElementDepth == fElementTypeStack.length) {
            int[] newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
            fElementTypeStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementIndexStack, 0, newStack, 0, fElementDepth);
            fElementIndexStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fContentSpecTypeStack, 0, newStack, 0, fElementDepth);
            fContentSpecTypeStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementChildCount, 0, newStack, 0, fElementDepth);
            fElementChildCount = newStack;
            int[][] newContentStack = new int[fElementDepth * 2][];
            System.arraycopy(fElementChildren, 0, newContentStack, 0, fElementDepth);
            fElementChildren = newContentStack;
        }
        fCurrentElementType = elementType;
        fCurrentElementIndex = elementIndex;
        fCurrentContentSpecType = contentSpecType;
        fElementTypeStack[fElementDepth] = elementType;
        fElementIndexStack[fElementDepth] = elementIndex;
        fContentSpecTypeStack[fElementDepth] = contentSpecType;
        fElementChildCount[fElementDepth] = 0;
        return contentSpecType == fCHILDRENSymbol;
    }
    public boolean endElement(int elementType) throws Exception {
        if (!fValidating && !fNamespacesEnabled && fElementDeclCount == 0 && fAttlistDeclCount == 0) {
            return false;
        }
        if (fValidating) {
            int elementIndex = fCurrentElementIndex;
            if (elementIndex != -1 && fCurrentContentSpecType != -1) {
                int childCount = peekChildCount();
                int result = checkContent(elementIndex, childCount, peekChildren());
                if (result != -1) {
                    int majorCode = result != childCount ? XMLMessages.MSG_CONTENT_INVALID : XMLMessages.MSG_CONTENT_INCOMPLETE;
                    reportRecoverableXMLError(majorCode,
                                              0,
                                              fStringPool.toString(elementType),
                                              getContentSpecAsString(elementIndex));
                }
            }
        }
        if (fElementDepth-- < 0)
            throw new RuntimeException("VAL001 Element stack underflow");
        if (fElementDepth < 0) {
            fCurrentElementType = -1;
            fCurrentElementIndex = -1;
            fCurrentContentSpecType = -1;
            //
            // Check after document is fully parsed
            // (1) check that there was an element with a matching id for every
            //   IDREF and IDREFS attr (V_IDREF0)
            //
            if (fValidating && fIdRefs != null)
                checkIdRefs();
        } else {
            fCurrentElementType = fElementTypeStack[fElementDepth];
            fCurrentElementIndex = fElementIndexStack[fElementDepth];
            fCurrentContentSpecType = fContentSpecTypeStack[fElementDepth];
        }
        if (fNamespacesEnabled)
            fNamespacesScope.decreaseDepth();
        return fCurrentContentSpecType == fCHILDRENSymbol;
    }
    //
    // Attribute Normalization/Validation
    //
    interface AttributeValidator {
        int normalize(int elementType, int attrName, int attValue, int attType, int enumHandle) throws Exception;
    }
    final class AttValidatorCDATA implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            return attValueHandle;
        }
    }
    final class AttValidatorID implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validName(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ID_INVALID,
                                              XMLMessages.VC_ID,
                                              fStringPool.toString(attrName), newAttValue);
                }
                //
                // ID - check that the id value is unique within the document (V_TAG8)
                //
                if (elementType != -1 && !addId(attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ID_NOT_UNIQUE,
                                              XMLMessages.VC_ID,
                                              fStringPool.toString(attrName), newAttValue);
                }
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    final class AttValidatorIDREF implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validName(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_IDREF_INVALID,
                                              XMLMessages.VC_IDREF,
                                              fStringPool.toString(attrName), newAttValue);
                }
                //
                // IDREF - remember the id value
                //
                if (elementType != -1)
                    addIdRef(attValueHandle);
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    final class AttValidatorIDREFS implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String idName = tokenizer.nextToken();
                    if (fValidating) {
                        if (!XMLCharacterProperties.validName(idName)) {
                            ok = false;
                        }
                        //
                        // IDREFS - remember the id values
                        //
                        if (elementType != -1)
                            addIdRef(fStringPool.addSymbol(idName));
                    }
                    sb.append(idName);
                    if (!tokenizer.hasMoreTokens())
                        break;
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_IDREFS_INVALID,
                                          XMLMessages.VC_IDREF,
                                          fStringPool.toString(attrName), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(elementType, attrName)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attrName), attValue, newAttValue);
                }
            }
            return attValueHandle;
        }
    }
    final class AttValidatorENTITY implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // ENTITY - check that the value is an unparsed entity name (V_TAGa)
                //
                int entity = fEntityPool.lookupEntity(attValueHandle);
                if (entity == -1 || !fEntityPool.isUnparsedEntity(entity)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ENTITY_INVALID,
                                              XMLMessages.VC_ENTITY_NAME,
                                              fStringPool.toString(attrName), newAttValue);
                }
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    final class AttValidatorENTITIES implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String entityName = tokenizer.nextToken();
                    //
                    // ENTITIES - check that each value is an unparsed entity name (V_TAGa)
                    //
                    if (fValidating) {
                        int entity = fEntityPool.lookupEntity(fStringPool.addSymbol(entityName));
                        if (entity == -1 || !fEntityPool.isUnparsedEntity(entity)) {
                            ok = false;
                        }
                    }
                    sb.append(entityName);
                    if (!tokenizer.hasMoreTokens())
                        break;
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_ENTITIES_INVALID,
                                          XMLMessages.VC_ENTITY_NAME,
                                          fStringPool.toString(attrName), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(elementType, attrName)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attrName), attValue, newAttValue);
                }
            }
            return attValueHandle;
        }
    }
    final class AttValidatorNMTOKEN implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validNmtoken(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_NMTOKEN_INVALID,
                                              XMLMessages.VC_NAME_TOKEN,
                                              fStringPool.toString(attrName), newAttValue);
                }
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    final class AttValidatorNMTOKENS implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String nmtoken = tokenizer.nextToken();
                    if (fValidating && !XMLCharacterProperties.validNmtoken(nmtoken)) {
                        ok = false;
                    }
                    sb.append(nmtoken);
                    if (!tokenizer.hasMoreTokens())
                        break;
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_NMTOKENS_INVALID,
                                          XMLMessages.VC_NAME_TOKEN,
                                          fStringPool.toString(attrName), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(elementType, attrName)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attrName), attValue, newAttValue);
                }
            }
            return attValueHandle;
        }
    }
    final class AttValidatorNOTATION implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // NOTATION - check that the value is in the AttDef enumeration (V_TAGo)
                //
                if (!fStringPool.stringInList(enumHandle, attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_NOT_IN_LIST,
                                              XMLMessages.VC_NOTATION_ATTRIBUTES,
                                              fStringPool.toString(attrName),
                                              newAttValue, fStringPool.stringListAsString(enumHandle));
                }
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    final class AttValidatorENUMERATION implements AttributeValidator {
        public int normalize(int elementType, int attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(elementType, attrName)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attrName), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // ENUMERATION - check that value is in the AttDef enumeration (V_TAG9)
                //
                if (!fStringPool.stringInList(enumHandle, attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_NOT_IN_LIST,
                                              XMLMessages.VC_ENUMERATION,
                                              fStringPool.toString(attrName),
                                              newAttValue, fStringPool.stringListAsString(enumHandle));
                }
            } else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;
        }
    }
    //
    // element stack
    //
    private void charDataInContent() {
        int[] children = fElementChildren[fElementDepth];
        int childCount = fElementChildCount[fElementDepth];
        try {
            children[childCount] = -1;
        } catch (NullPointerException ex) {
            children = fElementChildren[fElementDepth] = new int[256];
            childCount = 0; // should really assert this...
            children[childCount] = -1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            int[] newChildren = new int[childCount * 2];
            System.arraycopy(children, 0, newChildren, 0, childCount);
            children = fElementChildren[fElementDepth] = newChildren;
            children[childCount] = -1;
        }
        fElementChildCount[fElementDepth] = ++childCount;
    }
    private int peekChildCount() {
        return fElementChildCount[fElementDepth];
    }
    private int[] peekChildren() {
        return fElementChildren[fElementDepth];
    }

    /**
     * Check that the content of an element is valid.
     * <p>
     * This is the method of primary concern to the validator. This method is called
     * upon the scanner reaching the end tag of an element. At that time, the
     * element's children must be structurally validated, so it calls this method.
     * The index of the element being checked (in the decl pool), is provided as
     * well as an array of element name indexes of the children. The validator must
     * confirm that this element can have these children in this order.
     * <p>
     * This can also be called to do 'what if' testing of content models just to see
     * if they would be valid.
     * <p>
     * Note that the element index is an index into the element decl pool, whereas
     * the children indexes are name indexes, i.e. into the string pool.
     * <p>
     * A value of -1 in the children array indicates a PCDATA node. All other
     * indexes will be positive and represent child elements. The count can be
     * zero, since some elements have the EMPTY content model and that must be
     * confirmed.
     *
     * @param elementIndex The index within the <code>ElementDeclPool</code> of this
     *                     element.
     * @param childCount The number of entries in the <code>children</code> array.
     * @param children The children of this element.  Each integer is an index within
     *                 the <code>StringPool</code> of the child element name.  An index
     *                 of -1 is used to indicate an occurrence of non-whitespace character
     *                 data.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then additional content is required to reach a valid
     *         ending state.
     *
     * @exception Exception Thrown on error.
     */
    public int checkContent(int elementIndex, int childCount, int[] children) throws Exception
    {
        // Get the element name index from the element
        final int elementType = fCurrentElementType;

        if (DEBUG_PRINT_CONTENT)
        {
            String strTmp = fStringPool.toString(elementType);
            System.out.println
            (
                "Name: "
                + strTmp
                + ", Count: "
                + childCount
                + ", ContentSpec: "
                + getContentSpecAsString(elementIndex)
            );
            for (int index = 0; index < childCount && index < 10; index++) {
                if (index == 0) System.out.print("  (");
                String childName = (children[index] == -1) ? "#PCDATA" : fStringPool.toString(children[index]);
                if (index + 1 == childCount)
                    System.out.println(childName + ")");
                else if (index + 1 == 10)
                    System.out.println(childName + ",...)");
                else
                    System.out.print(childName + ",");
            }
        }

        // Get out the content spec for this element
        final int contentSpec = fCurrentContentSpecType;

        //
        //  Deal with the possible types of content. We try to optimized here
        //  by dealing specially with content models that don't require the
        //  full DFA treatment.
        //
        if (contentSpec == fEMPTYSymbol)
        {
            //
            //  If the child count is greater than zero, then this is
            //  an error right off the bat at index 0.
            //
            if (childCount != 0)
                return 0;
        }
         else if (contentSpec == fANYSymbol)
        {
            //
            //  This one is open game so we don't pass any judgement on it
            //  at all. Its assumed to fine since it can hold anything.
            //
        }
         else if (contentSpec == fMIXEDSymbol ||  contentSpec == fCHILDRENSymbol)
        {
            // Get the content model for this element, faulting it in if needed
            XMLContentModel cmElem = null;
            try
            {
                cmElem = getContentModel(elementIndex);
                return cmElem.validateContent(childCount, children);
            }

            catch(CMException excToCatch)
            {
                // REVISIT - Translate the caught exception to the public error API
                int majorCode = excToCatch.getErrorCode();
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                           majorCode,
                                           0,
                                           null,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            }
        }
         else if (contentSpec == -1)
        {
            reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_NOT_DECLARED,
                                      XMLMessages.VC_ELEMENT_VALID,
                                      elementType);
        }
         else
        {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       ImplementationMessages.VAL_CST,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }

        // We succeeded
        return -1;
    }

    /**
     * Returns information about which elements can be placed at a particular point
     * in the passed element's content model.
     * <p>
     * Note that the incoming content model to test must be valid at least up to
     * the insertion point. If not, then -1 will be returned and the info object
     * will not have been filled in.
     * <p>
     * If, on return, the info.isValidEOC flag is set, then the 'insert after'
     * elemement is a valid end of content, i.e. nothing needs to be inserted
     * after it to make the parent element's content model valid.
     *
     * @param elementIndex The index within the <code>ElementDeclPool</code> of the
     *                     element which is being querying.
     * @param fullyValid Only return elements that can be inserted and still
     *                   maintain the validity of subsequent elements past the
     *                   insertion point (if any).  If the insertion point is at
     *                   the end, and this is true, then only elements that can
     *                   be legal final states will be returned.
     * @param info An object that contains the required input data for the method,
     *             and which will contain the output information if successful.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed before the insertion point. If the value
     *         returned is equal to the number of children, then the specified
     *         children are valid but additional content is required to reach a
     *         valid ending state.
     *
     * @exception Exception Thrown on error.
     *
     * @see InsertableElementsInfo
     */
    public int whatCanGoHere(int                        elementIndex
                            , boolean                   fullyValid
                            , InsertableElementsInfo    info) throws Exception
    {
        //
        //  Do some basic sanity checking on the info packet. First, make sure
        //  that insertAt is not greater than the child count. It can be equal,
        //  which means to get appendable elements, but not greater. Or, if
        //  the current children array is null, that's bad too.
        //
        //  Since the current children array must have a blank spot for where
        //  the insert is going to be, the child count must always be at least
        //  one.
        //
        //  Make sure that the child count is not larger than the current children
        //  array. It can be equal, which means get appendable elements, but not
        //  greater.
        //
        if ((info.insertAt > info.childCount)
        ||  (info.curChildren == null)
        ||  (info.childCount < 1)
        ||  (info.childCount > info.curChildren.length))
        {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       ImplementationMessages.VAL_WCGHI,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }

        int retVal = 0;
        try
        {
            // Get the content model for this element
            final XMLContentModel cmElem = getContentModel(elementIndex);

            // And delegate this call to it
            retVal = cmElem.whatCanGoHere(fullyValid, info);
        }

        catch(CMException excToCatch)
        {
            // REVISIT - Translate caught error to the public error handler interface
            int majorCode = excToCatch.getErrorCode();
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       majorCode,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }
        return retVal;
    }


    // -----------------------------------------------------------------------
    //  Private methods
    // -----------------------------------------------------------------------

    //
    //  When the element has a 'CHILDREN' model, this method is called to
    //  create the content model object. It looks for some special case simple
    //  models and creates SimpleContentModel objects for those. For the rest
    //  it creates the standard DFA style model.
    //
    private final XMLContentModel createChildModel(int elementIndex) throws CMException
    {
        //
        //  Get the content spec node for the element we are working on.
        //  This will tell us what kind of node it is, which tells us what
        //  kind of model we will try to create.
        //
        XMLContentSpecNode specNode = new XMLContentSpecNode();
        int contentSpecIndex = getContentSpecHandle(elementIndex);
        getContentSpecNode(contentSpecIndex, specNode);

        //
        //  Check that the left value is not -1, since any content model
        //  with PCDATA should be MIXED, so we should not have gotten here.
        //
        if (specNode.value == -1)
            throw new CMException(ImplementationMessages.VAL_NPCD);

        if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF)
        {
            //
            //  Its a single leaf, so its an 'a' type of content model, i.e.
            //  just one instance of one element. That one is definitely a
            //  simple content model.
            //
            return new SimpleContentModel(specNode.value, -1, specNode.type);
        }
         else if ((specNode.type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE)
              ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_SEQ))
        {
            //
            //  Lets see if both of the children are leafs. If so, then it
            //  it has to be a simple content model
            //
            XMLContentSpecNode specLeft = new XMLContentSpecNode();
            XMLContentSpecNode specRight = new XMLContentSpecNode();
            getContentSpecNode(specNode.value, specLeft);
            getContentSpecNode(specNode.otherValue, specRight);

            if ((specLeft.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF)
            &&  (specRight.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF))
            {
                //
                //  Its a simple choice or sequence, so we can do a simple
                //  content model for it.
                //
                return new SimpleContentModel
                (
                    specLeft.value
                    , specRight.value
                    , specNode.type
                );
            }
        }
         else if ((specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE)
              ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE)
              ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE))
        {
            //
            //  Its a repetition, so see if its one child is a leaf. If so
            //  its a repetition of a single element, so we can do a simple
            //  content model for that.
            //
            XMLContentSpecNode specLeft = new XMLContentSpecNode();
            getContentSpecNode(specNode.value, specLeft);

            if (specLeft.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF)
            {
                //
                //  It is, so we can create a simple content model here that
                //  will check for this repetition. We pass -1 for the unused
                //  right node.
                //
                return new SimpleContentModel(specLeft.value, -1, specNode.type);
            }
        }
         else
        {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        //
        //  Its not a simple content model, so here we have to create a DFA
        //  for this element. So we create a DFAContentModel object. He
        //  encapsulates all of the work to create the DFA.
        //
        fLeafCount = 0;
        CMNode cmn = buildSyntaxTree(contentSpecIndex, specNode);
        return new DFAContentModel
        (
            fStringPool
            , cmn
            , fLeafCount
        );
    }


    //
    //  This method will handle the querying of the content model for a
    //  particular element. If the element does not have a content model, then
    //  it will be created.
    //
    private XMLContentModel getContentModel(int elementIndex) throws CMException
    {
        // See if a content model already exists first
        XMLContentModel cmRet = getElementContentModel(elementIndex);

        // If we have one, just return that. Otherwise, gotta create one
        if (cmRet != null)
            return cmRet;

        // Get the type of content this element has
        final int contentSpec = getContentSpecType(elementIndex);

        // And create the content model according to the spec type
        if (contentSpec == fMIXEDSymbol)
        {
            //
            //  Just create a mixel content model object. This type of
            //  content model is optimized for mixed content validation.
            //
            XMLContentSpecNode specNode = new XMLContentSpecNode();
            int contentSpecIndex = getContentSpecHandle(elementIndex);
            makeContentList(contentSpecIndex, specNode);
            cmRet = new MixedContentModel(fCount, fContentList);
        }
         else if (contentSpec == fCHILDRENSymbol)
        {
            //
            //  This method will create an optimal model for the complexity
            //  of the element's defined model. If its simple, it will create
            //  a SimpleContentModel object. If its a simple list, it will
            //  create a SimpleListContentModel object. If its complex, it
            //  will create a DFAContentModel object.
            //
            cmRet = createChildModel(elementIndex);
        }
         else
        {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        // Add the new model to the content model for this element
        setContentModel(elementIndex, cmRet);

        return cmRet;
    }
    //
    //
    //
    protected void reportRecoverableXMLError(int majorCode, int minorCode) throws Exception {
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   null,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, int stringIndex1) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1) throws Exception {
        Object[] args = { string1 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1), fStringPool.toString(stringIndex2) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1, String string2) throws Exception {
        Object[] args = { string1, string2 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }
    protected void reportRecoverableXMLError(int majorCode, int minorCode, String string1, String string2, String string3) throws Exception {
        Object[] args = { string1, string2, string3 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }

    //
    // XMLEntityHandler interface
    //
    public void readerChange(XMLEntityHandler.EntityReader nextReader, int nextReaderId) throws Exception {
        fDTDScanner.readerChange(nextReader, nextReaderId);
    }
    public void endOfInput(int entityNameIndex, boolean moreToFollow) throws Exception {
        if (fValidating) {
            int readerDepth = fEntityHandler.getReaderDepth();
            if (fDTDScanner.getReadingContentSpec()) {
                int parenDepth = fDTDScanner.parenDepth();
                if (readerDepth != parenDepth) {
                    reportRecoverableXMLError(XMLMessages.MSG_IMPROPER_GROUP_NESTING,
                                              XMLMessages.VC_PROPER_GROUP_PE_NESTING,
                                              entityNameIndex);
                }
            } else {
                int markupDepth = fDTDScanner.markupDepth();
                if (readerDepth != markupDepth) {
                    reportRecoverableXMLError(XMLMessages.MSG_IMPROPER_DECLARATION_NESTING,
                                              XMLMessages.VC_PROPER_DECLARATION_PE_NESTING,
                                              entityNameIndex);
                }
            }
        }
        fDTDScanner.endOfInput(entityNameIndex, moreToFollow);
    }
    public int saveCurrentLocation() {
        return -1;
    }
    public boolean validVersionNum(String version) {
        return XMLCharacterProperties.validVersionNum(version);
    }
    public boolean validEncName(String encoding) {
        return XMLCharacterProperties.validEncName(encoding);
    }
    public int validPublicId(String publicId) {
        return XMLCharacterProperties.validPublicId(publicId);
    }
    public void doctypeDecl(int rootElementType, int publicId, int systemId) throws Exception {
        fRootElementType = rootElementType;
        fEventHandler.startDTD(rootElementType, publicId, systemId);
    }
    public void startReadingFromExternalSubset(int publicId, int systemId) throws Exception {
        fEntityHandler.startReadingFromExternalSubset(fStringPool.toString(publicId),
                                                      fStringPool.toString(systemId),
                                                      fDTDScanner.markupDepth());
    }
    public void stopReadingFromExternalSubset() throws Exception {
        fEntityHandler.stopReadingFromExternalSubset();
    }
    public int addElementDecl(int elementType) throws Exception {
        int elementIndex = addElement(elementType);
        return elementIndex;
    }
    public int addElementDecl(int elementType, int contentSpecType, int contentSpec) throws Exception {
        int elementIndex = addElementDecl(elementType, contentSpecType, contentSpec, !usingStandaloneReader());
        if (elementIndex == -1) {
            if (fValidating) {
                reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_ALREADY_DECLARED,
                                          XMLMessages.VC_UNIQUE_ELEMENT_TYPE_DECLARATION,
                                          elementType);
            }
        } else {
            fEventHandler.elementDecl(elementType, getContentSpec(elementIndex));
            fElementDeclCount++;
        }
        return elementIndex;
    }
    public int addAttDef(int elementIndex, int attName, int attType, int enumeration, int attDefaultType, int attDefaultValue) throws Exception {
        int attDefIndex = addAttDef(elementIndex, attName, attType, enumeration, attDefaultType, attDefaultValue, !usingStandaloneReader(), fValidating, fWarningOnDuplicateAttDef);
        if (attDefIndex != -1) {
            String enumString = (enumeration == -1) ? null : fStringPool.stringListAsString(enumeration);
            fEventHandler.attlistDecl(getElementType(elementIndex), attName, attType, enumString, attDefaultType, attDefaultValue);
            fAttlistDeclCount++;
        }
        return attDefIndex;
    }
    public int addUniqueLeafNode(int nodeValue) throws Exception {
        int csn = addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_LEAF, nodeValue, -1, true);
        if (csn == -1 && fValidating) {
            reportRecoverableXMLError(XMLMessages.MSG_DUPLICATE_TYPE_IN_MIXED_CONTENT,
                                      XMLMessages.VC_NO_DUPLICATE_TYPES,
                                      nodeValue);
        }
        return csn;
    }
    public int addContentSpecNode(int nodeType, int nodeValue) throws Exception {
        return addContentSpecNode(nodeType, nodeValue, -1, false);
    }
    public int addContentSpecNode(int nodeType, int nodeValue, int otherNodeValue) throws Exception {
        return addContentSpecNode(nodeType, nodeValue, otherNodeValue, false);
    }
    public int addInternalPEDecl(int name, int value, int location) throws Exception {
        if (fParameterEntityPool == null)
            fParameterEntityPool = new EntityPool(fStringPool, fErrorReporter, false);
        int entityIndex = fParameterEntityPool.addEntityDecl(name, value, location, -1, -1, -1, !usingStandaloneReader());
        fEventHandler.internalPEDecl(name, value);
        return entityIndex;
    }
    public int addExternalPEDecl(int name, int publicId, int systemId) throws Exception {
        if (fParameterEntityPool == null)
            fParameterEntityPool = new EntityPool(fStringPool, fErrorReporter, false);
        int entityIndex = fParameterEntityPool.addEntityDecl(name, -1, -1, publicId, systemId, -1, !usingStandaloneReader());
        fEventHandler.externalPEDecl(name, publicId, systemId);
        return entityIndex;
    }
    public int addInternalEntityDecl(int name, int value, int location) throws Exception {
        int entityIndex = fEntityPool.addEntityDecl(name, value, location, -1, -1, -1, !usingStandaloneReader());
        fEventHandler.internalEntityDecl(name, value);
        return entityIndex;
    }
    public int addExternalEntityDecl(int name, int publicId, int systemId) throws Exception {
        int entityIndex = fEntityPool.addEntityDecl(name, -1, -1, publicId, systemId, -1, !usingStandaloneReader());
        fEventHandler.externalEntityDecl(name, publicId, systemId);
        return entityIndex;
    }
    public int addUnparsedEntityDecl(int name, int publicId, int systemId, int notationName) throws Exception {
        int entityIndex = fEntityPool.addEntityDecl(name, -1, -1, publicId, systemId, notationName, !usingStandaloneReader());
        fEventHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        if (fEntityPool.lookupNotation(notationName) == -1) {
            Object[] args = { fStringPool.toString(name),
                              fStringPool.toString(notationName) };
            fEntityPool.addRequiredNotation(notationName,
                                            fErrorReporter.getLocator(),
                                            XMLMessages.MSG_NOTATION_NOT_DECLARED_FOR_UNPARSED_ENTITYDECL,
                                            XMLMessages.VC_NOTATION_DECLARED,
                                            args);
        }
        return entityIndex;
    }
    public int startEnumeration() {
        return fStringPool.startStringList();
    }
    public void addNameToEnumeration(int enumHandle, int elementType, int attrName, int nameIndex, boolean isNotationType) {
        fStringPool.addStringToList(enumHandle, nameIndex);
        if (isNotationType && fEntityPool.lookupNotation(nameIndex) == -1) {
            Object[] args = { fStringPool.toString(elementType),
                              fStringPool.toString(attrName),
                              fStringPool.toString(nameIndex) };
            fEntityPool.addRequiredNotation(nameIndex,
                                            fErrorReporter.getLocator(),
                                            XMLMessages.MSG_NOTATION_NOT_DECLARED_FOR_NOTATIONTYPE_ATTRIBUTE,
                                            XMLMessages.VC_NOTATION_DECLARED,
                                            args);
        }
    }
    public void endEnumeration(int enumHandle) {
        fStringPool.finishStringList(enumHandle);
    }
    public int addNotationDecl(int notationName, int publicId, int systemId) throws Exception {
        //
        // REVISIT - I am making an arbitrary decision that is not covered by the
        //  spec (or I missed it).  If we see a second NotationDecl for the same name,
        //  we will ignore the latter declaration.  This seems consistent with the
        //  usual behavior of such things in the spec, but it might also be good to
        //  generate a warning that this has occurred.  The addNotation() method will
        //  return -1 if we already have a declaration for this name.  This will slow
        //  down the parser in direct relation to the number of NotationDecl names.
        //
        int notationIndex = fEntityPool.addNotationDecl(notationName, publicId, systemId, !usingStandaloneReader());
        if (notationIndex != -1)
            fEventHandler.notationDecl(notationName, publicId, systemId);
        return notationIndex;
    }
    public void callProcessingInstruction(int target, int data) throws Exception {
        fEventHandler.processingInstructionInDTD(target, data);
    }
    public void callComment(int comment) throws Exception {
        fEventHandler.commentInDTD(comment);
    }
    public int scanElementType(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception {
        if (!fNamespacesEnabled) {
            return entityReader.scanName(fastchar);
        }
        int elementType = entityReader.scanQName(fastchar);
        if (entityReader.lookingAtChar(':', false)) {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_TWO_COLONS_IN_QNAME,
                                       XMLMessages.P5_INVALID_CHARACTER,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            entityReader.skipPastNmtoken(' ');
        }
        return elementType;
    }
    public int checkForElementTypeWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception {
        if (!fNamespacesEnabled) {
            return entityReader.scanName(fastchar);
        }
        int elementType = entityReader.scanQName(fastchar);
        if (entityReader.lookingAtChar(':', false)) {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_TWO_COLONS_IN_QNAME,
                                       XMLMessages.P5_INVALID_CHARACTER,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            entityReader.skipPastNmtoken(' ');
        }
        return elementType;
    }
    public int checkForAttributeNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception {
        if (!fNamespacesEnabled) {
            return entityReader.scanName(fastchar);
        }
        int attrName = entityReader.scanQName(fastchar);
        if (entityReader.lookingAtChar(':', false)) {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_TWO_COLONS_IN_QNAME,
                                       XMLMessages.P5_INVALID_CHARACTER,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            entityReader.skipPastNmtoken(' ');
        }
        return attrName;
    }
    public int checkForNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception {
        //
        // REVISIT - what does this have to do with PE references?
        //
        int valueIndex = entityReader.scanName(fastcheck);
        return valueIndex;
    }
    public int checkForNmtokenWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception {
        //
        // REVISIT - what does this have to do with PE references?
        //
        int nameOffset = entityReader.currentOffset();
        entityReader.skipPastNmtoken(fastcheck);
        int nameLength = entityReader.currentOffset() - nameOffset;
        if (nameLength == 0)
            return -1;
        int valueIndex = entityReader.addSymbol(nameOffset, nameLength);
        return valueIndex;
    }
    public int scanDefaultAttValue(int elementType, int attrName, int attType, int enumeration) throws Exception {
        if (fValidating && attType == fIDSymbol) {
            reportRecoverableXMLError(XMLMessages.MSG_ID_DEFAULT_TYPE_INVALID,
                                      XMLMessages.VC_ID_ATTRIBUTE_DEFAULT,
                                      attrName);
        }
        int defaultAttValue = fDTDScanner.scanDefaultAttValue(elementType, attrName);
        if (defaultAttValue == -1)
            return -1;
        if (attType != fCDATASymbol) {
            AttributeValidator av = getValidatorForAttType(attType);
            defaultAttValue = av.normalize(-1, attrName, defaultAttValue, attType, enumeration);
        }
        return defaultAttValue;
    }

    //
    //  This method will build our syntax tree by recursively going though
    //  the element's content model and creating new CMNode type node for
    //  the model, and rewriting '?' and '+' nodes along the way.
    //
    //  On final return, the head node of the syntax tree will be returned.
    //  This top node will be a sequence node with the left side being the
    //  rewritten content, and the right side being a special end of content
    //  node.
    //
    //  We also count the non-epsilon leaf nodes, which is an important value
    //  that is used in a number of places later.
    //
    private int fLeafCount = 0;
    private final CMNode
    buildSyntaxTree(int startNode, XMLContentSpecNode specNode) throws CMException
    {

        // We will build a node at this level for the new tree
        CMNode nodeRet = null;

        getContentSpecNode(startNode, specNode);

        //
        //  If this node is a leaf, then its an easy one. We just add it
        //  to the tree.
        //
        if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF)
        {
            //
            //  Create a new leaf node, and pass it the current leaf count,
            //  which is its DFA state position. Bump the leaf count after
            //  storing it. This makes the positions zero based since we
            //  store first and then increment.
            //
            nodeRet = new CMLeaf(specNode.type, specNode.value, fLeafCount++);
        }
         else
        {
            //
            //  Its not a leaf, so we have to recurse its left and maybe right
            //  nodes. Save both values before we recurse and trash the node.
            //
            final int leftNode = specNode.value;
            final int rightNode = specNode.otherValue;

            if ((specNode.type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE)
            ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_SEQ))
            {
                //
                //  Recurse on both children, and return a binary op node
                //  with the two created sub nodes as its children. The node
                //  type is the same type as the source.
                //

                nodeRet = new CMBinOp
                (
                    specNode.type
                    , buildSyntaxTree(leftNode, specNode)
                    , buildSyntaxTree(rightNode, specNode)
                );
            }
             else if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE)
            {
                // This one is fine as is, just change to our form
                nodeRet = new CMUniOp
                (
                    specNode.type
                    , buildSyntaxTree(leftNode, specNode)
                );
            }
             else if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE)
            {
                // Convert to (x|epsilon)
                nodeRet = new CMBinOp
                (
                    XMLContentSpecNode.CONTENTSPECNODE_CHOICE
                    , buildSyntaxTree(leftNode, specNode)
                    , new CMLeaf(XMLContentSpecNode.CONTENTSPECNODE_LEAF, fEpsilonIndex)
                );
            }
             else if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE)
            {
                // Convert to (x,x*)
                nodeRet = new CMBinOp
                (
                    XMLContentSpecNode.CONTENTSPECNODE_SEQ
                    , buildSyntaxTree(leftNode, specNode)
                    , new CMUniOp
                      (
                        XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE
                        , buildSyntaxTree(leftNode, specNode)
                      )
                );
            }
             else
            {
                throw new CMException(ImplementationMessages.VAL_CST);
            }
        }

        // And return our new node for this level
        return nodeRet;
    }

    private int fCount = 0;
    private int[] fContentList = new int[64];
    private final void makeContentList(int startNode, XMLContentSpecNode specNode) throws CMException {
        //
        //  Ok, we need to build up an array of the possible children
        //  under this element. The mixed content model can only be a
        //  repeated series of alternations with no numeration or ordering.
        //  So we call a local recursive method to iterate the tree and
        //  build up the array.
        //
        //  So we get the content spec of the element, which gives us the
        //  starting node. Everything else kicks off from there. We pass
        //  along a content node for each iteration to use so that it does
        //  not have to create and trash lots of objects.
        //
        while (true)
        {
            fCount = 0;

            try
            {
                fCount = buildContentList
                (
                    startNode
                    , 0
                    , specNode
                );
            }

            catch(IndexOutOfBoundsException excToCatch)
            {
                //
                //  Expand the array and try it again. Yes, this is
                //  piggy, but the odds of it ever actually happening
                //  are slim to none.
                //
                fContentList = new int[fContentList.length * 2];
                fCount = 0;
                continue;
            }

            // We survived, so break out
            break;
        }
    }
    private final int buildContentList( int                     startNode
                                        , int                   count
                                        , XMLContentSpecNode    specNode) throws CMException
    {
        // Get the content spec for the passed start node
        getContentSpecNode(startNode, specNode);

        // If this node is a leaf, then add it to our list and return.
        if (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_LEAF)
        {
            fContentList[count++] = specNode.value;
            return count;
        }

        //
        //  Its not a leaf, so we have to recurse its left and maybe right
        //  nodes. Save both values before we recurse and trash the node.
        //
        final int leftNode = specNode.value;
        final int rightNode = specNode.otherValue;

        if ((specNode.type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE)
        ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_SEQ))
        {
            //
            //  Recurse on the left and right nodes of this guy, making sure
            //  to keep the count correct.
            //
            count = buildContentList(leftNode, count, specNode);
            count = buildContentList(rightNode, count, specNode);
        }
         else if ((specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE)
              ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE)
              ||  (specNode.type == XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE))
        {
            // Just do the left node on this one
            count = buildContentList(leftNode, count, specNode);
        }
         else
        {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        // And return our accumlated new count
        return count;
    }

    //
    // Chunk size constants
    //
    private static final int CHUNK_SHIFT = 8;           // 2^8 = 256
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));   // 2^10 = 1k
    //
    // Instance variables
    //
    //
    // Element list
    //
    private int fElementCount = 0;
    private int[][] fElementType = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fElementDeclIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpecType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpec = new int[INITIAL_CHUNK_COUNT][];
    private XMLContentModel[][] fContentModel = new XMLContentModel[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistHead = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistTail = new int[INITIAL_CHUNK_COUNT][];
    //
    // ContentSpecNode list
    //
    private int fNodeCount = 0;
    private byte[][] fNodeType = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNodeValue = new int[INITIAL_CHUNK_COUNT][];
    //
    // AttDef list
    //
    private int fAttDefCount = 0;
    private int[][] fAttName = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttType = new int[INITIAL_CHUNK_COUNT][];
    private AttributeValidator[][] fAttValidator = new AttributeValidator[INITIAL_CHUNK_COUNT][];
    private int[][] fEnumeration = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttDefaultType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttValue = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fAttDefIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNextAttDef = new int[INITIAL_CHUNK_COUNT][];
    //
    //
    //
    private Hashtable fIdDefs = null;
    private Hashtable fIdRefs = null;
    private Object fNullValue = null;
    private AttributeValidator fAttValidatorCDATA = null;
    private AttributeValidator fAttValidatorID = null;
    private AttributeValidator fAttValidatorIDREF = null;
    private AttributeValidator fAttValidatorIDREFS = null;
    private AttributeValidator fAttValidatorENTITY = null;
    private AttributeValidator fAttValidatorENTITIES = null;
    private AttributeValidator fAttValidatorNMTOKEN = null;
    private AttributeValidator fAttValidatorNMTOKENS = null;
    private AttributeValidator fAttValidatorNOTATION = null;
    private AttributeValidator fAttValidatorENUMERATION = null;
    //
    //
    //
    private void poolReset() {
        int chunk = 0;
        int index = 0;
        for (int i = 0; i < fElementCount; i++) {
            fContentModel[chunk][index] = null;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        fElementCount = 0;
        fNodeCount = 0;
        fAttDefCount = 0;
        if (fIdDefs != null)
            fIdDefs.clear();
        if (fIdRefs != null)
            fIdRefs.clear();
    }
    //
    // Element entries
    //
    private boolean ensureElementCapacity(int chunk) {
        try {
            return fElementType[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fElementDeclIsExternal, 0, newByteArray, 0, chunk);
            fElementDeclIsExternal = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fElementType, 0, newIntArray, 0, chunk);
            fElementType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fContentSpecType, 0, newIntArray, 0, chunk);
            fContentSpecType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fContentSpec, 0, newIntArray, 0, chunk);
            fContentSpec = newIntArray;
            XMLContentModel[][] newContentModel = new XMLContentModel[chunk * 2][];
            System.arraycopy(fContentModel, 0, newContentModel, 0, chunk);
            fContentModel = newContentModel;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttlistHead, 0, newIntArray, 0, chunk);
            fAttlistHead = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttlistTail, 0, newIntArray, 0, chunk);
            fAttlistTail = newIntArray;
        } catch (NullPointerException ex) {
        }
        fElementType[chunk] = new int[CHUNK_SIZE];
        fElementDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
        fContentSpecType[chunk] = new int[CHUNK_SIZE];
        fContentSpec[chunk] = new int[CHUNK_SIZE];
        fContentModel[chunk] = new XMLContentModel[CHUNK_SIZE];
        fAttlistHead[chunk] = new int[CHUNK_SIZE];
        fAttlistTail[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    private int lookupElement(int elementType) {
        return fStringPool.getDeclaration(elementType);
    }
    private int addElement(int elementType) {
        int elementIndex = fStringPool.getDeclaration(elementType);
        if (elementIndex != -1)
            return elementIndex;
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        fElementType[chunk][index] = elementType;
        fElementDeclIsExternal[chunk][index] = 0;
        fContentSpecType[chunk][index] = -1;
        fContentSpec[chunk][index] = -1;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        fStringPool.setDeclaration(elementType, fElementCount);
        return fElementCount++;
    }
    private int addElementDecl(int elementType, int contentSpecType, int contentSpec, boolean isExternal) {
//System.out.println("Pool " + this + " add " + decl.elementType + " (" + fStringPool.toString(decl.elementType) + ")");
        int elementIndex = fStringPool.getDeclaration(elementType);
        if (elementIndex != -1) {
            int chunk = elementIndex >> CHUNK_SHIFT;
            int index = elementIndex & CHUNK_MASK;
            if (fContentSpecType[chunk][index] != -1)
                return -1;
            fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
            fContentSpecType[chunk][index] = contentSpecType;
            fContentSpec[chunk][index] = contentSpec;
            fContentModel[chunk][index] = null;
            return elementIndex;
        }
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        fElementType[chunk][index] = elementType;
        fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fContentSpecType[chunk][index] = contentSpecType;
        fContentSpec[chunk][index] = contentSpec;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        fStringPool.setDeclaration(elementType, fElementCount);
        return fElementCount++;
    }
    private int getElementType(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fElementType[chunk][index];
    }
    private boolean getElementDeclIsExternal(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return false;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return (fElementDeclIsExternal[chunk][index] != 0);
    }
    private int getContentSpecType(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpecType[chunk][index];
    }
    class ContentSpecImpl implements XMLValidator.ContentSpec {
        public StringPool fStringPool;
        public int fHandle;
        public int fType;
        public String toString() {
            if (fType == fMIXEDSymbol || fType == fCHILDRENSymbol)
                return getContentSpecNodeAsString(fHandle);
            else
                return fStringPool.toString(fType);
        }
        public int getType() {
            return fType;
        }
        public int getHandle() {
            return fHandle;
        }
        public void getNode(int handle, XMLContentSpecNode node) {
            getContentSpecNode(handle, node);
        }
    }
    ContentSpecImpl fContentSpecImpl = null;
    private XMLValidator.ContentSpec getContentSpec(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return null;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        if (fContentSpecImpl == null)
            fContentSpecImpl = new ContentSpecImpl();
        fContentSpecImpl.fStringPool = fStringPool;
        fContentSpecImpl.fHandle = fContentSpec[chunk][index];
        fContentSpecImpl.fType = fContentSpecType[chunk][index];
        return fContentSpecImpl;
    }
    private int getContentSpecHandle(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpec[chunk][index];
    }
    public String getContentSpecAsString(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return null;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        int contentSpecType = fContentSpecType[chunk][index];
        if (contentSpecType == fMIXEDSymbol || contentSpecType == fCHILDRENSymbol)
            return getContentSpecNodeAsString(fContentSpec[chunk][index]);
        else
            return fStringPool.toString(contentSpecType);
    }
    private XMLContentModel getElementContentModel(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return null;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentModel[chunk][index];
    }
    private void setContentModel(int elementIndex, XMLContentModel cm) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        fContentModel[chunk][index] = cm;
    }
    //
    // contentspec entries
    //
    private boolean ensureNodeCapacity(int chunk) {
        try {
            return fNodeType[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fNodeType, 0, newByteArray, 0, chunk);
            fNodeType = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fNodeValue, 0, newIntArray, 0, chunk);
            fNodeValue = newIntArray;
        } catch (NullPointerException ex) {
        }
        fNodeType[chunk] = new byte[CHUNK_SIZE];
        fNodeValue[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    //
    //
    //
    private int addContentSpecLeafNode(int nodeValue) throws Exception {
        //
        // Check that we have not seen this value before...
        //
        if (nodeValue != -1) {
            int nodeCount = fNodeCount;
            int chunk = fNodeCount >> CHUNK_SHIFT;
            int index = fNodeCount & CHUNK_MASK;
            while (true) {
                if (index-- == 0) {
                    index = CHUNK_SIZE - 1;
                    chunk--;
                }
                int nodeType = fNodeType[chunk][index];
                if (nodeType == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                    int otherNodeValue = fNodeValue[chunk][index];
                    if (otherNodeValue == -1)
                        break;
                    if (otherNodeValue == nodeValue) {
                        return -1;
                    }
                }
            }
        }
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        fNodeType[chunk][index] = (byte)XMLContentSpecNode.CONTENTSPECNODE_LEAF;
        fNodeValue[chunk][index] = nodeValue;
        return fNodeCount++;
    }
    //
    //
    //
    private int addContentSpecNode(int nodeType, int nodeValue, int otherNodeValue, boolean mustBeUnique) throws Exception {
        if (mustBeUnique) // REVISIT - merge these methods...
            return addContentSpecLeafNode(nodeValue);
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        switch (nodeType) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            fNodeType[chunk][index] = (byte)nodeType;
            fNodeValue[chunk][index] = nodeValue;
            return fNodeCount++;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            fNodeType[chunk][index] = (byte)nodeType;
            fNodeValue[chunk][index] = nodeValue;
            int nodeIndex = fNodeCount++;
            if (++index == CHUNK_SIZE) {
                chunk++;
                ensureNodeCapacity(chunk);
                index = 0;
            }
            fNodeType[chunk][index] = (byte)(nodeType | 64); // flag second entry for consistancy checking
            fNodeValue[chunk][index] = otherNodeValue;
            fNodeCount++;
            return nodeIndex;
        default:
            return -1;
        }
    }
    protected void getContentSpecNode(int contentSpecIndex, XMLContentSpecNode csn) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        csn.type = fNodeType[chunk][index];
        csn.value = fNodeValue[chunk][index];
        if (csn.type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE || csn.type == XMLContentSpecNode.CONTENTSPECNODE_SEQ) {
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
            csn.otherValue = fNodeValue[chunk][index];
        } else
            csn.otherValue = -1;
    }
    private void appendContentSpecNode(int contentSpecIndex, StringBuffer sb, boolean noParen) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        switch (type) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
            sb.append(value == -1 ? "#PCDATA" : fStringPool.toString(value));
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            appendContentSpecNode(value, sb, false);
            sb.append('?');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
            appendContentSpecNode(value, sb, false);
            sb.append('*');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            appendContentSpecNode(value, sb, false);
            sb.append('+');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            if (!noParen)
                sb.append('(');
            int leftChunk = value >> CHUNK_SHIFT;
            int leftIndex = value & CHUNK_MASK;
            int leftType = fNodeType[leftChunk][leftIndex];
            appendContentSpecNode(value, sb, leftType == type);
            sb.append(type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE ? '|' : ',');
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
            appendContentSpecNode(fNodeValue[chunk][index], sb, false);
            if (!noParen)
                sb.append(')');
            return;
        default:
            return;
        }
    }
    public String getContentSpecNodeAsString(int contentSpecIndex) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        StringBuffer sb = new StringBuffer();
        switch (type) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
            sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")");
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")?");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")*");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")+");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        default:
            return null;
        }
        return sb.toString();
    }
    //
    // attribute list interfaces
    //
    private boolean ensureAttrCapacity(int chunk) {
        try {
            return fAttName[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fAttDefIsExternal, 0, newByteArray, 0, chunk);
            fAttDefIsExternal = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttName, 0, newIntArray, 0, chunk);
            fAttName = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttType, 0, newIntArray, 0, chunk);
            fAttType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fEnumeration, 0, newIntArray, 0, chunk);
            fEnumeration = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttDefaultType, 0, newIntArray, 0, chunk);
            fAttDefaultType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttValue, 0, newIntArray, 0, chunk);
            fAttValue = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fNextAttDef, 0, newIntArray, 0, chunk);
            fNextAttDef = newIntArray;
            AttributeValidator[][] newValidatorArray = new AttributeValidator[chunk * 2][];
            System.arraycopy(fAttValidator, 0, newValidatorArray, 0, chunk);
            fAttValidator = newValidatorArray;
        } catch (NullPointerException ex) {
        }
        fAttDefIsExternal[chunk] = new byte[CHUNK_SIZE];
        fAttName[chunk] = new int[CHUNK_SIZE];
        fAttType[chunk] = new int[CHUNK_SIZE];
        fAttValidator[chunk] = new AttributeValidator[CHUNK_SIZE];
        fEnumeration[chunk] = new int[CHUNK_SIZE];
        fAttDefaultType[chunk] = new int[CHUNK_SIZE];
        fAttValue[chunk] = new int[CHUNK_SIZE];
        fNextAttDef[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    //
    private int addAttDef(int elementIndex, int attName, int attType, int enumeration, int attDefaultType, int attDefaultValue, boolean isExternal, boolean validationEnabled, boolean warnOnDuplicate) throws Exception {
        //
        // check fields
        //
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int dupID = -1;
        int dupNotation = -1;
        while (attlistIndex != -1) {
            int attrChunk = attlistIndex >> CHUNK_SHIFT;
            int attrIndex = attlistIndex & CHUNK_MASK;
            if (fStringPool.equalNames(fAttName[attrChunk][attrIndex], attName)) {
                if (warnOnDuplicate) {
                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                      fStringPool.toString(attName) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_DUPLICATE_ATTDEF,
                                               XMLMessages.P53_DUPLICATE,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                return -1;
            }
            if (validationEnabled) {
                if (attType == fIDSymbol && fAttType[attrChunk][attrIndex] == fIDSymbol)
                    dupID = fAttName[attrChunk][attrIndex];
                if (attType == fNOTATIONSymbol && fAttType[attrChunk][attrIndex] == fNOTATIONSymbol)
                    dupNotation = fAttName[attrChunk][attrIndex];
            }
            attlistIndex = fNextAttDef[attrChunk][attrIndex];
        }
        if (validationEnabled) {
            if (dupID != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupID),
                                  fStringPool.toString(attName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_ID_ATTRIBUTE,
                                           XMLMessages.VC_ONE_ID_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
            if (dupNotation != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupNotation),
                                  fStringPool.toString(attName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE,
                                           XMLMessages.VC_ONE_NOTATION_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
        }
        //
        // save the fields
        //
        int chunk = fAttDefCount >> CHUNK_SHIFT;
        int index = fAttDefCount & CHUNK_MASK;
        ensureAttrCapacity(chunk);
        fAttName[chunk][index] = attName;
        fAttType[chunk][index] = attType;
        fAttValidator[chunk][index] = getValidatorForAttType(attType);
        fEnumeration[chunk][index] = enumeration;
        fAttDefaultType[chunk][index] = attDefaultType;
        fAttDefIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fAttValue[chunk][index] = attDefaultValue;
        //
        // add to the attr list for this element
        //
        int nextIndex = -1;
        if (attDefaultValue != -1) {
            nextIndex = fAttlistHead[elemChunk][elemIndex];
            fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1)
                fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
        } else {
            nextIndex = fAttlistTail[elemChunk][elemIndex];
            fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1)
                fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            else {
                fNextAttDef[nextIndex >> CHUNK_SHIFT][nextIndex & CHUNK_MASK] = fAttDefCount;
                nextIndex = -1;
            }
        }
        fNextAttDef[chunk][index] = nextIndex;
        return fAttDefCount++;
    }
    private AttributeValidator getValidatorForAttType(int attType) {
        if (attType == fCDATASymbol) {
            if (fAttValidatorCDATA == null)
                fAttValidatorCDATA = new AttValidatorCDATA();
            return fAttValidatorCDATA;
        }
        if (attType == fIDSymbol) {
            if (fAttValidatorID == null)
                fAttValidatorID = new AttValidatorID();
            return fAttValidatorID;
        }
        if (attType == fIDREFSymbol) {
            if (fAttValidatorIDREF == null)
                fAttValidatorIDREF = new AttValidatorIDREF();
            return fAttValidatorIDREF;
        }
        if (attType == fIDREFSSymbol) {
            if (fAttValidatorIDREFS == null)
                fAttValidatorIDREFS = new AttValidatorIDREFS();
            return fAttValidatorIDREFS;
        }
        if (attType == fENTITYSymbol) {
            if (fAttValidatorENTITY == null)
                fAttValidatorENTITY = new AttValidatorENTITY();
            return fAttValidatorENTITY;
        }
        if (attType == fENTITIESSymbol) {
            if (fAttValidatorENTITIES == null)
                fAttValidatorENTITIES = new AttValidatorENTITIES();
            return fAttValidatorENTITIES;
        }
        if (attType == fNMTOKENSymbol) {
            if (fAttValidatorNMTOKEN == null)
                fAttValidatorNMTOKEN = new AttValidatorNMTOKEN();
            return fAttValidatorNMTOKEN;
        }
        if (attType == fNMTOKENSSymbol) {
            if (fAttValidatorNMTOKENS == null)
                fAttValidatorNMTOKENS = new AttValidatorNMTOKENS();
            return fAttValidatorNMTOKENS;
        }
        if (attType == fNOTATIONSymbol) {
            if (fAttValidatorNOTATION == null)
                fAttValidatorNOTATION = new AttValidatorNOTATION();
            return fAttValidatorNOTATION;
        }
        if (attType == fENUMERATIONSymbol) {
            if (fAttValidatorENUMERATION == null)
                fAttValidatorENUMERATION = new AttValidatorENUMERATION();
            return fAttValidatorENUMERATION;
        }
        throw new RuntimeException("VAL002 getValidatorForAttType(" + fStringPool.toString(attType) + ")");
    }
    private int getAttDef(int elementType, int attrName) {
        int elementIndex = fStringPool.getDeclaration(elementType);
        if (elementIndex == -1)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        int attDefIndex = fAttlistHead[chunk][index];
        while (attDefIndex != -1) {
            chunk = attDefIndex >> CHUNK_SHIFT;
            index = attDefIndex & CHUNK_MASK;
            if (fAttName[chunk][index] == attrName || fStringPool.equalNames(fAttName[chunk][index], attrName))
                return attDefIndex;
            attDefIndex = fNextAttDef[chunk][index];
        }
        return -1;
    }
    private boolean getAttDefIsExternal(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return (fAttDefIsExternal[chunk][index] != 0);
    }
    private int getAttName(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttName[chunk][index];
    }
    private int getAttValue(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttValue[chunk][index];
    }
    private AttributeValidator getAttributeValidator(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttValidator[chunk][index];
    }
    private int getAttType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttType[chunk][index];
    }
    private int getAttDefaultType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttDefaultType[chunk][index];
    }
    private int getEnumeration(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fEnumeration[chunk][index];
    }
    private int addDefaultAttributes(int elementIndex, XMLAttrList attrList, int attrIndex, boolean validationEnabled, boolean standalone) throws Exception {
        //
        // Check after all specified attrs are scanned
        // (1) report error for REQUIRED attrs that are missing (V_TAGc)
        // (2) check that FIXED attrs have matching value (V_TAGd)
        // (3) add default attrs (FIXED and NOT_FIXED)
        //
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int firstCheck = attrIndex;
        int lastCheck = -1;
//System.err.println("specified attributes for element type " + fStringPool.toString(fElementType[elemChunk][elemIndex]));
        while (attlistIndex != -1) {
            int adChunk = attlistIndex >> CHUNK_SHIFT;
            int adIndex = attlistIndex & CHUNK_MASK;
            int attName = fAttName[adChunk][adIndex];
            int attType = fAttType[adChunk][adIndex];
            int attDefType = fAttDefaultType[adChunk][adIndex];
            int attValue = fAttValue[adChunk][adIndex];
            boolean specified = false;
            boolean required = attDefType == fREQUIREDSymbol;
            if (firstCheck != -1) {
                boolean cdata = attType == fCDATASymbol;
                if (!cdata || required || attValue != -1) {
                    int i = attrList.getFirstAttr(firstCheck);
                    while (i != -1 && (lastCheck == -1 || i <= lastCheck)) {
                        if (fStringPool.equalNames(attrList.getAttrName(i), attName)) {
//System.err.println(fStringPool.toString(attrList.getAttrName(i)) + " == " + fStringPool.toString(attName));
                            if (validationEnabled && attDefType == fFIXEDSymbol) {
                                int alistValue = attrList.getAttValue(i);
                                if (alistValue != attValue &&
                                    !fStringPool.toString(alistValue).equals(fStringPool.toString(attValue))) {
                                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                                    fStringPool.toString(attName),
                                                    fStringPool.toString(alistValue),
                                                    fStringPool.toString(attValue) };
                                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                            XMLMessages.XML_DOMAIN,
                                                            XMLMessages.MSG_FIXED_ATTVALUE_INVALID,
                                                            XMLMessages.VC_FIXED_ATTRIBUTE_DEFAULT,
                                                            args,
                                                            XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                                }
                            }
                            specified = true;
                            break;
                        }
//System.err.println(fStringPool.toString(attrList.getAttrName(i)) + " != " + fStringPool.toString(attName));
                        i = attrList.getNextAttr(i);
                    }
                }
            }
            if (!specified) {
                if (required) {
                    if (validationEnabled) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_REQUIRED_ATTRIBUTE,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                } else if (attValue != -1) {
                    if (validationEnabled && standalone && fAttDefIsExternal[adChunk][adIndex] != 0) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                    if (attType == fIDREFSymbol) {
                        addIdRef(attValue);
                    } else if (attType == fIDREFSSymbol) {
                        StringTokenizer tokenizer = new StringTokenizer(fStringPool.toString(attValue));
                        while (tokenizer.hasMoreTokens()) {
                            String idName = tokenizer.nextToken();
                            addIdRef(fStringPool.addSymbol(idName));
                        }
                    }
                    if (attrIndex == -1)
                        attrIndex = attrList.startAttrList();
                    int newAttr = attrList.addAttr(attName, attValue, attType, false, false);
                    if (lastCheck == -1)
                        lastCheck = newAttr;
                }
            }
            attlistIndex = fNextAttDef[adChunk][adIndex];
        }
        return attrIndex;
    }
    //
    //
    //
    protected boolean addId(int idIndex) {
//System.err.println("addId(" + fStringPool.toString(idIndex) + ") " + idIndex);
        Integer key = new Integer(idIndex);
        if (fIdDefs == null)
            fIdDefs = new Hashtable();
        else if (fIdDefs.containsKey(key))
            return false;
        if (fNullValue == null)
            fNullValue = new Object();
        fIdDefs.put(key, fNullValue/*new Integer(elementType)*/);
        return true;
    }
    protected void addIdRef(int idIndex) {
//System.err.println("addIdRef(" + fStringPool.toString(idIndex) + ") " + idIndex);
        Integer key = new Integer(idIndex);
        if (fIdDefs != null && fIdDefs.containsKey(key))
            return;
        if (fIdRefs == null)
            fIdRefs = new Hashtable();
        else if (fIdRefs.containsKey(key))
            return;
        if (fNullValue == null)
            fNullValue = new Object();
        fIdRefs.put(key, fNullValue/*new Integer(elementType)*/);
    }
    /**
     * Check that all ID references were to ID attributes present in the document.
     * <p>
     * This method is a convenience call that allows the validator to do any id ref
     * checks above and beyond those done by the scanner. The scanner does the checks
     * specificied in the XML spec, i.e. that ID refs refer to ids which were
     * eventually defined somewhere in the document.
     * <p>
     * If the validator is for a Schema perhaps, which defines id semantics beyond
     * those of the XML specificiation, this is where that extra checking would be
     * done. For most validators, this is a no-op.
     *
     * @exception Exception Thrown on error.
     */
    private void checkIdRefs() throws Exception {
        if (fIdRefs == null)
            return;
        Enumeration en = fIdRefs.keys();
        while (en.hasMoreElements()) {
            Integer key = (Integer)en.nextElement();
            if (fIdDefs == null || !fIdDefs.containsKey(key)) {
                Object[] args = { fStringPool.toString(key.intValue()) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ELEMENT_WITH_ID_REQUIRED,
                                           XMLMessages.VC_IDREF,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }
    }

    /** 
     * Checks that all declared elements refer to declared elements
     * in their content models. This method calls out to the error
     * handler to indicate warnings.
     */
    private void checkDeclaredElements() throws Exception {
        for (int i = 0; i < fElementCount; i++) {
            int type = getContentSpecType(i);
            if (type == fMIXEDSymbol || type == fCHILDRENSymbol) {
                int chunk = i >> CHUNK_SHIFT;
                int index = i &  CHUNK_MASK;
                int contentSpecIndex = fContentSpec[chunk][index];
                checkDeclaredElements(i, contentSpecIndex);
            }
        }
    }

    /** 
     * Does a recursive (if necessary) check on the specified element's
     * content spec to make sure that all children refer to declared
     * elements.
     * <p>
     * This method assumes that it will only be called when there is
     * a validation handler.
     */
    private void checkDeclaredElements(int elementIndex, int contentSpecIndex) throws Exception {
        
        // get spec type and value
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex &  CHUNK_MASK;
        int type  = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];

        // handle type
        switch (type) {
        
            // #PCDATA | element
            case XMLContentSpecNode.CONTENTSPECNODE_LEAF: {
                // perform check for declared element
                if (value != -1 && fStringPool.getDeclaration(value) == -1) {
                    int elemChunk = elementIndex >> CHUNK_SHIFT;
                    int elemIndex = elementIndex &  CHUNK_MASK;
                    int elementType = fElementType[elemChunk][elemIndex];
                    Object[] args = { fStringPool.toString(elementType),
                                      fStringPool.toString(value) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               XMLMessages.P45_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                break;
            }

            // (...)? | (...)* | (...)+
            case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE: 
            case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE: {
                checkDeclaredElements(elementIndex, value);
                break;
            }

            // (... , ...) | (... | ...)
            case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
            case XMLContentSpecNode.CONTENTSPECNODE_SEQ: {
                checkDeclaredElements(elementIndex, value);
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    index = 0;
                }
                checkDeclaredElements(elementIndex, fNodeValue[chunk][index]);
                break;
            }
        }
    }
}
