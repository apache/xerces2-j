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

package org.apache.xerces.validators.schema;

import java.io.IOException;
import java.util.Hashtable; 
import java.util.Vector; 

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import java.io.IOException;


import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.msg.SchemaMessages;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.common.XMLValidator;
import org.apache.xerces.validators.common.XMLContentModel;
import org.apache.xerces.validators.datatype.DatatypeValidator;
import org.apache.xerces.validators.datatype.InvalidDatatypeValueException;
import org.apache.xerces.validators.datatype.IllegalFacetException;
import org.apache.xerces.validators.datatype.IllegalFacetValueException;
import org.apache.xerces.validators.datatype.UnknownFacetException;
import org.apache.xerces.validators.datatype.BooleanValidator;
import org.apache.xerces.validators.datatype.IntegerValidator;
import org.apache.xerces.validators.datatype.StringValidator;
import org.apache.xerces.validators.datatype.FloatValidator;
import org.apache.xerces.validators.datatype.DoubleValidator;
import org.apache.xerces.validators.datatype.DecimalValidator;
import org.apache.xerces.validators.datatype.TimeDurationValidator;
import org.apache.xerces.validators.datatype.TimeInstantValidator;
import org.apache.xerces.validators.datatype.BinaryValidator;
import org.apache.xerces.validators.datatype.URIValidator;
import org.apache.xerces.utils.QName;


/**
 * SchemaImporter is an <font color="red"><b>experimental</b></font> implementation
 * of a validator for the W3C Schema Language.  All of its implementation is subject
 * to change.
 * 
 * This class is being factored out to the Grammar class
 */
public class SchemaImporter {
    private Schema                    fSchema           = null;
    private XMLValidator              fValidator        = null;
    private XMLErrorReporter          fErrorReporter    = null;
    private DefaultEntityHandler      fEntityHandler    = null;
    private StringPool                fStringPool       = null;
    private DatatypeValidatorRegistry fDatatypeRegistry = new DatatypeValidatorRegistry();
    private int                       fTypeCount        = 0;
    private int                       fGroupCount       = 0;
    private int                       fModelGroupCount  = 0;
    private int                       fAttributeGroupCount = 0;
    private int                       fDatatypeCount    = 0;
    private Hashtable                 fForwardRefs      = new Hashtable(); // REVISIT w/ more efficient structure later
    private Hashtable                 fAttrGroupUses    = new Hashtable();


    private Document fSchemaDocument                    = null;//The Schema Document

    //
    //
    //
    public SchemaImporter(StringPool stringPool,
                          XMLErrorReporter errorReporter,
                          DefaultEntityHandler entityHandler,
                          XMLValidator validator) {
        fErrorReporter  = errorReporter;
        fEntityHandler  = entityHandler;
        fStringPool     = stringPool;
        fValidator      = validator;
        fDatatypeRegistry.initializeRegistry();
        fSchema = new Schema (fErrorReporter, fValidator);
    }


    /**
     * Resets SchemaImporter- This function will move
     * to the Grammar class
     * 
     * @param stringPool StringPool entry
     * @exception Exception
     */
    public void reset(StringPool stringPool) throws Exception {
        fStringPool = stringPool;
        // need to reset the datatype registry !!
        fDatatypeRegistry = new DatatypeValidatorRegistry();
        fDatatypeRegistry.initializeRegistry();
        fSchema = new Schema (fErrorReporter, fValidator);
    }

    /**
     * Get a Schema document - This method will
     * move to Grammar
     * 
     * @return        Returns a DOM Document. Future implementation
     *         will return a DOM Schema.
     */
    public Document getSchemaDocument() {
        return fSchemaDocument;
    }

    /**
     * Revisit- A dummy now
     * 
     * @param elementType
     *               An index to a pool
     * @return 
     */
    private int getContentSpecHandleForElementType(int elementType) {
        // REVISIT: ???
        //return fValidator.getContentSpecHandle(fStringPool.getDeclaration(elementType));
        return -1;
    }
    /**
     * A Dummy now
     * 
     * @param elementType
     *               Index to pool
     * @return 
     */
    private int getContentSpecTypeForElementType(int elementType) {
        // REVISIT: ???
        //return fValidator.getContentSpecType(fStringPool.getDeclaration(elementType));
        return -1;
    }

    // content spec node types

    /** Occurrence count: [0, n]. */
    private static final int CONTENTSPECNODE_ZERO_TO_N = XMLContentSpec.CONTENTSPECNODE_SEQ + 1;

    /** Occurrence count: [m, n]. */
    private static final int CONTENTSPECNODE_M_TO_N = CONTENTSPECNODE_ZERO_TO_N + 1;

    /** Occurrence count: [m, *). */
    private static final int CONTENTSPECNODE_M_OR_MORE = CONTENTSPECNODE_M_TO_N + 1;

    private DOMParser fSchemaParser = null;

    /**
     * Builds a DOM Schema representation of a Grammar.
     * 
     * @param is     Input source which represents the Schema grammar( an .xsd file)
     */
    public void loadSchema(InputSource is) {


        DOMParser parser = new DOMParser (); //WORK
        try {
            parser.setEntityResolver(new Resolver());
            parser.setErrorHandler(new ErrorHandler());
            //parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.parse (is);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit (1);
        }


        // create parser for schema
        if (fSchemaParser == null) {
            fSchemaParser = new DOMParser() {
                public void ignorableWhitespace(char ch[], int start, int length) {}
                public void ignorableWhitespace(int dataIdx) {}
            };
            fSchemaParser.setEntityResolver(new Resolver());
            fSchemaParser.setErrorHandler(new ErrorHandler());
        }


        try {
        fSchemaParser.setFeature("http://xml.org/sax/features/validation", true);
        fSchemaParser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        fSchemaParser.parse(is);
        } catch( SAXNotSupportedException e ){
            e.printStackTrace();  
        } catch( SAXNotRecognizedException e ){
            e.printStackTrace();
        } catch( IOException e ){
            e.printStackTrace();
        } catch( SAXException e ){
            e.printStackTrace();
        }

        fSchemaDocument = fSchemaParser.getDocument();
        if (fSchemaDocument == null) {
            System.err.println("error: couldn't load schema file!");
            return;
        }


        try {
            Element root = fSchemaDocument.getDocumentElement(); // Get root of grammar tree representation
            traverseSchema(root); //start traversing and filling Grammar structures                                                        
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * We traverse recursively through Document and build 
     * Grammar as we go along.
     * 
     * @param root   Element
     * @exception Exception
     */
    private void traverseSchema(Element root) throws Exception {
        // is there anything to do?
        if (root == null) {
            return;
        }

        // run through children
        for (Element child = XUtil.getFirstChildElement(root);
            child != null;
            child = XUtil.getNextSiblingElement(child)) {
            //System.out.println("child: "+child.getNodeName()+' '+child.getAttribute(SchemaSymbols.ATT_NAME));

            //
            // Element type
            //

            String name = child.getNodeName();
            if (name.equals(SchemaSymbols.ELT_ANNOTATION) ) {
                traverseComment(child);
            } else if (name.equals(SchemaSymbols.ELT_SIMPLETYPE )) {
                traverseDatatypeDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_COMPLEXTYPE )) {
                traverseTypeDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_ELEMENT )) { // && child.getAttribute(SchemaSymbols.ATT_REF).equals("")) {
                traverseElementDecl(child);
            } else if (name.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                traverseAttrGroup(child);
            } else if (name.equals(SchemaSymbols.ELT_GROUP) && child.getAttribute(SchemaSymbols.ATT_REF).equals("")) {
                traverseGroup(child);
            } else if (name.equals(SchemaSymbols.ELT_NOTATION)) {

                int notationName = fStringPool.addSymbol(child.getAttribute(SchemaSymbols.ATT_NAME));
                int publicId = fStringPool.addString(child.getAttribute("public"));
                int systemId = fStringPool.addString(child.getAttribute("system"));
                fEntityHandler.addNotationDecl(notationName, publicId, systemId, true);
            }

        } // for each child node

    } // traverseSchema(Element)


    private void traverseComment(Element comment) {
        return; // do nothing
    }

    private int traverseTypeDecl(Element typeDecl) throws Exception {
        return 0;
    }

    private int traverseGroup(Element groupDecl) throws Exception {
        return 0;
    }

    private int traverseGroupRef(Element groupRef) {
        return 0;
    }

    public int traverseDatatypeDecl(Element datatypeDecl) throws Exception {
        return 0;
    }

    private int traverseElementDecl(Element elementDecl) throws Exception {
        return 0;
    }

    private int traverseElementRef(Element elementRef) {
        return 0;
    }

    private void traverseAttributeDecl(Element attrDecl, int elementIndex) throws Exception {
        return ;
    }

    private int traverseAttrGroup(Element attrGroupDecl) throws Exception {
        return 0;
    }

    private int traverseAttrGroupRef(Element attrGroupRef) {
        return 0;
    }

    private void addUse(int def, int use) {
        return ;
    }


    /** builds the all content model */
    private int buildAllModel(int children[], int count) throws Exception {
        // build all model
        if (count > 1) {

            // create and initialize singletons
            XMLContentSpec.Node choice = new XMLContentSpec.Node();

            choice.type = XMLContentSpec.CONTENTSPECNODE_CHOICE;
            choice.value = -1;
            choice.otherValue = -1;

            // build all model
            sort(children, 0, count);
            int index = buildAllModel(children, 0, choice);

            return index;
        }

        if (count > 0) {
            return children[0];
        }

        return -1;
    }

    /** Builds the all model. */
    private int buildAllModel(int src[], int offset,
                              XMLContentSpec.Node choice) throws Exception {

        // swap last two places
        if (src.length - offset == 2) {
            int seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            } else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            swap(src, offset, offset + 1);
            seqIndex = createSeq(src);
            if (choice.value == -1) {
                choice.value = seqIndex;
            } else {
                if (choice.otherValue != -1) {
                    choice.value = fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
                }
                choice.otherValue = seqIndex;
            }
            return fValidator.addContentSpecNode(choice.type, choice.value, choice.otherValue, false);
        }

        // recurse
        for (int i = offset; i < src.length - 1; i++) {
            choice.value = buildAllModel(src, offset + 1, choice);
            choice.otherValue = -1;
            sort(src, offset, src.length - offset);
            shift(src, offset, i + 1);
        }

        int choiceIndex = buildAllModel(src, offset + 1, choice);
        sort(src, offset, src.length - offset);

        return choiceIndex;

    } // buildAllModel(int[],int,ContentSpecNode,ContentSpecNode):int

    /** Creates a sequence. */
    private int createSeq(int src[]) throws Exception {

        int left = src[0];
        int right = src[1];

        for (int i = 2; i < src.length; i++) {
            left = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                 left, right, false);
            right = src[i];
        }

        return fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                             left, right, false);

    } // createSeq(int[]):int

    /** Shifts a value into position. */
    private void shift(int src[], int pos, int offset) {

        int temp = src[offset];
        for (int i = offset; i > pos; i--) {
            src[i] = src[i - 1];
        }
        src[pos] = temp;

    } // shift(int[],int,int)

    /** Simple sort. */
    private void sort(int src[], final int offset, final int length) {

        for (int i = offset; i < offset + length - 1; i++) {
            int lowest = i;
            for (int j = i + 1; j < offset + length; j++) {
                if (src[j] < src[lowest]) {
                    lowest = j;
                }
            }
            if (lowest != i) {
                int temp = src[i];
                src[i] = src[lowest];
                src[lowest] = temp;
            }
        }

    } // sort(int[],int,int)

    /** Swaps two values. */
    private void swap(int src[], int i, int j) {

        int temp = src[i];
        src[i] = src[j];
        src[j] = temp;

    } // swap(int[],int,int)

    private int expandContentModel(int contentSpecNodeIndex, Element element) throws Exception {

        // set occurrence count
        int occurs = getOccurrenceCount(element);
        int m = 1, n = 1;
        if (!isSimpleOccurrenceCount(occurs)) {
            try {
                m = Integer.parseInt(element.getAttribute(SchemaSymbols.ATT_MINOCCURS));
            } catch (NumberFormatException e) {
                reportSchemaError(SchemaMessageProvider.ValueNotInteger,
                                  new Object [] { SchemaSymbols.ATT_MINOCCURS });
            }
            try {
                n = Integer.parseInt(element.getAttribute(SchemaSymbols.ATT_MAXOCCURS));
            } catch (NumberFormatException e) {
                reportSchemaError(SchemaMessageProvider.ValueNotInteger,
                                  new Object [] { SchemaSymbols.ATT_MAXOCCURS});
            }
        }

        switch (occurs) {

        
        case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                //System.out.println("occurs = +");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE,
                                                                     contentSpecNodeIndex, -1, false);
                break;
            }

        case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: {
                //System.out.println("occurs = *");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                                     contentSpecNodeIndex, -1, false);
                break;
            }

        case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE: {
                //System.out.println("occurs = ?");
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                                     contentSpecNodeIndex, -1, false);
                break;
            }

        case CONTENTSPECNODE_M_OR_MORE: {
                //System.out.println("occurs = "+m+" -> *");

                // create sequence node
                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add required number
                for (int i = 1; i < m; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                              value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE,
                                                                     contentSpecNodeIndex, -1, false);

                // add optional part
                if (otherValue != -1) {
                    value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                          value, otherValue, false);
                }
                otherValue = contentSpecNodeIndex;

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                     value, otherValue, false);
                break;
            }

            //
            // M -> N
            //

        case CONTENTSPECNODE_M_TO_N: {
                //System.out.println("occurs = "+m+" -> "+n);

                // create sequence node
                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add required number
                for (int i = 1; i < m; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                              value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                                     contentSpecNodeIndex, -1, false);

                // add optional number
                for (int i = n; i > m; i--) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                              value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                     value, otherValue, false);
                break;
            }

            //
            // 0 -> N
            //

        case CONTENTSPECNODE_ZERO_TO_N: {
                //System.out.println("occurs = 0 -> "+n);

                // create optional content model node
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE,
                                                                     contentSpecNodeIndex, -1, false);

                int value = contentSpecNodeIndex;
                int otherValue = -1;

                // add optional number
                for (int i = 1; i < n; i++) {
                    if (otherValue != -1) {
                        value = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                              value, otherValue, false);
                    }
                    otherValue = contentSpecNodeIndex;
                }

                // set expanded content model index
                contentSpecNodeIndex = fValidator.addContentSpecNode(XMLContentSpec.CONTENTSPECNODE_SEQ,
                                                                     value, otherValue, false);
                break;
            }

        } // switch

        //System.out.println("content = "+getContentSpecNodeAsString(contentSpecNodeIndex));
        return contentSpecNodeIndex;

    } // expandContentModel(int,int,int,int):int

    private boolean isSimpleOccurrenceCount(int occurs) {
        return occurs == -1 ||
        occurs == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE ||
        occurs == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE ||
        occurs == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE;
    }

    private int getOccurrenceCount(Element element) {

        String minOccur = element.getAttribute(SchemaSymbols.ATT_MINOCCURS);
        String maxOccur = element.getAttribute(SchemaSymbols.ATT_MAXOCCURS);

        if (minOccur.equals("0")) {
            if (maxOccur.equals("1") || maxOccur.length() == 0) {
                return XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE;
            } else if (maxOccur.equals("*")) {
                return XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE;
            } else {
                return CONTENTSPECNODE_ZERO_TO_N;
            }
        } else if (minOccur.equals("1") || minOccur.length() == 0) {
            if (maxOccur.equals("*")) {
                return XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE;
            } else if (!maxOccur.equals("1") && maxOccur.length() > 0) {
                return CONTENTSPECNODE_M_TO_N;
            }
        } else {
            if (maxOccur.equals("*")) {
                return CONTENTSPECNODE_M_OR_MORE;
            } else {
                return CONTENTSPECNODE_M_TO_N;
            }
        }

        // exactly one
        return -1;
    }

    private void reportSchemaError(int major, Object args[]) throws Exception {
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   SchemaMessageProvider.SCHEMA_DOMAIN,
                                   major,
                                   SchemaMessageProvider.MSG_NONE,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
    }

    //
    // Classes
    //

    static class Resolver implements EntityResolver {

        private static final String SYSTEM[] = {
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000225/structures.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-2-20000225/datatypes.dtd",
            "http://www.w3.org/TR/2000/WD-xmlschema-1-20000225/versionInfo.ent",
        };
        private static final String PATH[] = {
            "structures.dtd",
            "datatypes.dtd",
            "versionInfo.ent",
        };

        public InputSource resolveEntity(String publicId, String systemId)
        throws IOException {

            // looking for the schema DTDs?
            for (int i = 0; i < SYSTEM.length; i++) {
                if (systemId.equals(SYSTEM[i])) {
                    InputSource source = new InputSource(getClass().getResourceAsStream(PATH[i]));
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
            }

            // use default resolution
            return null;

        } // resolveEntity(String,String):InputSource

    } // class Resolver

    static class ErrorHandler implements org.xml.sax.ErrorHandler {
        /** Warning. */
        public void warning(SAXParseException ex) {
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }
        /** Error. */
        public void error(SAXParseException ex) {
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        }
        /** Fatal error. */
        public void fatalError(SAXParseException ex) throws SAXException {
            System.err.println("[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
            throw ex;
        }
        //
        // Private methods
        //

        /** Returns a string of the location. */
        private String getLocationString(SAXParseException ex) {
            StringBuffer str = new StringBuffer();

            String systemId_ = ex.getSystemId();
            if (systemId_ != null) {
                int index = systemId_.lastIndexOf('/');
                if (index != -1)
                    systemId_ = systemId_.substring(index + 1);
                str.append(systemId_);
            }
            str.append(':');
            str.append(ex.getLineNumber());
            str.append(':');
            str.append(ex.getColumnNumber());

            return str.toString();

        } // getLocationString(SAXParseException):String
    }

    class DatatypeValidatorRegistry {
        Hashtable fRegistry = new Hashtable();

        String integerSubtypeTable[][] = {
            { "non-negative-integer", DatatypeValidator.MININCLUSIVE , "0"},
            { "positive-integer", DatatypeValidator.MININCLUSIVE, "1"},
            { "non-positive-integer", DatatypeValidator.MAXINCLUSIVE, "0"},
            { "negative-integer", DatatypeValidator.MAXINCLUSIVE, "-1"}
        };

        void initializeRegistry() {
            Hashtable facets = null;
            fRegistry.put("boolean", new BooleanValidator());
            DatatypeValidator integerValidator = new IntegerValidator();
            fRegistry.put("integer", integerValidator);
            fRegistry.put("string", new StringValidator());
            fRegistry.put("decimal", new DecimalValidator());
            fRegistry.put("float", new FloatValidator());
            fRegistry.put("double", new DoubleValidator());
            fRegistry.put("timeDuration", new TimeDurationValidator());
            fRegistry.put("timeInstant", new TimeInstantValidator());
            fRegistry.put("binary", new BinaryValidator());
            fRegistry.put("uri", new URIValidator());
            //REVISIT - enable the below
            //fRegistry.put("date", new DateValidator());
            //fRegistry.put("timePeriod", new TimePeriodValidator());
            //fRegistry.put("time", new TimeValidator());


            DatatypeValidator v = null;
            for (int i = 0; i < integerSubtypeTable.length; i++) {
                v = new IntegerValidator();
                facets = new Hashtable();
                facets.put(integerSubtypeTable[i][1],integerSubtypeTable[i][2]);
                v.setBasetype(integerValidator);
                try {
                    v.setFacets(facets);
                } catch (IllegalFacetException ife) {
                    System.out.println("Internal error initializing registry - Illegal facet: "+integerSubtypeTable[i][0]);
                } catch (IllegalFacetValueException ifve) {
                    System.out.println("Internal error initializing registry - Illegal facet value: "+integerSubtypeTable[i][0]);
                } catch (UnknownFacetException ufe) {
                    System.out.println("Internal error initializing registry - Unknown facet: "+integerSubtypeTable[i][0]);
                }
                fRegistry.put(integerSubtypeTable[i][0], v);
            }
        }

        DatatypeValidator getValidatorFor(String type) {
            return(DatatypeValidator) fRegistry.get(type);
        }

        void addValidator(String name, DatatypeValidator v) {
            fRegistry.put(name,v);
        }
    }

    //
    //
    //
    final class AttValidatorDATATYPE implements XMLValidator.AttributeValidator {
        public int normalize(QName elementType1, QName attrName, int attValueHandle, int attType, int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            try { // REVISIT - integrate w/ error handling
                String type = fStringPool.toString(enumHandle);
                DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
                if (v != null)
                    v.validate(fStringPool.toString(attValueHandle));
                else
                    reportSchemaError(SchemaMessageProvider.NoValidatorFor,
                                      new Object [] { type});
            } catch (InvalidDatatypeValueException idve) {
                reportSchemaError(SchemaMessageProvider.IncorrectDatatype,
                                  new Object [] { idve.getMessage()});
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Internal error in attribute datatype validation");
            }
            return attValueHandle;
        }
    }
    //
    //
    //
    public XMLValidator.AttributeValidator createDatatypeAttributeValidator() {
        return new AttValidatorDATATYPE();
    }
    public XMLContentModel createDatatypeContentModel(int elementIndex) {
        return new DatatypeContentModel(fDatatypeRegistry, fValidator, fStringPool, elementIndex);
    }
}
