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

package org.apache.xerces.impl.xs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.EqualityHelper;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.impl.xs.identity.Field;
import org.apache.xerces.impl.xs.identity.FieldActivator;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.KeyRef;
import org.apache.xerces.impl.xs.identity.Selector;
import org.apache.xerces.impl.xs.identity.UniqueOrKey;
import org.apache.xerces.impl.xs.identity.ValueStore;
import org.apache.xerces.impl.xs.identity.XPathMatcher;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.CMNodeFactory;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.IntStack;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeAlternative;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.datatypes.ObjectList;

/**
 * 
 * A class providing certain functionalities and has validation session data for the 
 * XML Schema validator. Important functionalities provided by this class are IDC constraint 
 * implementation, schema error handling routines and CTA/assertion interfaces with the 
 * XML Schema validator.
 * 
 * @xerces.internal
 * 
 * @author Sandy Gao IBM
 * @author Elena Litani IBM
 * @author Andy Clark IBM
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Mukul Gandhi IBM
 * @version $Id$
 */
public class XMLSchemaValidatorBase implements XSElementDeclHelper, FieldActivator {
    
    protected XMLSchemaValidatorBase() {
       // NO OP  
    }
    
    //
    // Constants
    //
    protected static final boolean DEBUG = false;

    // feature identifiers

    /** Feature identifier: validation. */
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;

    /** Feature identifier: validation. */
    protected static final String SCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;

    /** Feature identifier: schema full checking*/
    protected static final String SCHEMA_FULL_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;

    /** Feature identifier: dynamic validation. */
    protected static final String DYNAMIC_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.DYNAMIC_VALIDATION_FEATURE;

    /** Feature identifier: expose schema normalized value */
    protected static final String NORMALIZE_DATA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;

    /** Feature identifier: send element default value via characters() */
    protected static final String SCHEMA_ELEMENT_DEFAULT =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;

    /** Feature identifier: augment PSVI */
    protected static final String SCHEMA_AUGMENT_PSVI =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;

    /** Feature identifier: whether to recognize java encoding names */
    protected static final String ALLOW_JAVA_ENCODINGS =
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;

    /** Feature identifier: standard uri conformant feature. */
    protected static final String STANDARD_URI_CONFORMANT_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.STANDARD_URI_CONFORMANT_FEATURE;
    
    /** Feature: generate synthetic annotations */
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    
    /** Feature identifier: validate annotations. */
    protected static final String VALIDATE_ANNOTATIONS =
        Constants.XERCES_FEATURE_PREFIX + Constants.VALIDATE_ANNOTATIONS_FEATURE;
    
    /** Feature identifier: honour all schemaLocations */
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;

    /** Feature identifier: use grammar pool only */
    protected static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;

    /** Feature identifier: whether to continue parsing a schema after a fatal error is encountered */
    protected static final String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;

    protected static final String PARSER_SETTINGS =
            Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    
    /** Feature identifier: namespace growth */
    protected static final String NAMESPACE_GROWTH = 
        Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_GROWTH_FEATURE;

    /** Feature identifier: tolerate duplicates */
    protected static final String TOLERATE_DUPLICATES = 
        Constants.XERCES_FEATURE_PREFIX + Constants.TOLERATE_DUPLICATES_FEATURE;

    /** Feature identifier: whether to ignore xsi:type attributes until a global element declaration is encountered */
    protected static final String IGNORE_XSI_TYPE =
        Constants.XERCES_FEATURE_PREFIX + Constants.IGNORE_XSI_TYPE_FEATURE;
    
    /** Feature identifier: whether to ignore ID/IDREF errors */
    protected static final String ID_IDREF_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.ID_IDREF_CHECKING_FEATURE;
    
    /** Feature identifier: whether to ignore unparsed entity errors */
    protected static final String UNPARSED_ENTITY_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.UNPARSED_ENTITY_CHECKING_FEATURE;
    
    /** Feature identifier: whether to ignore identity constraint errors */
    protected static final String IDENTITY_CONSTRAINT_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.IDC_CHECKING_FEATURE;

    /** Feature identifier: whether to ignore type alternatives */
    protected static final String TYPE_ALTERNATIVES_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.TYPE_ALTERNATIVES_CHEKING_FEATURE;
    
    /** Feature identifier: whether to allow full XPath 2.0 checking for CTA processing */
    protected static final String CTA_FULL_XPATH_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.CTA_FULL_XPATH_CHECKING_FEATURE;
    
    /** Feature identifier: whether to allow comment and PI nodes to be visible during <assert> processing */
    protected static final String ASSERT_COMMENT_PI_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.ASSERT_COMMENT_PI_CHECKING_FEATURE;

    // property identifiers

    /** Property identifier: symbol table. */
    public static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: error reporter. */
    public static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: entity resolver. */
    public static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    /** Property identifier: grammar pool. */
    public static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;

    protected static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;

    /** Property identifier: schema location. */
    protected static final String SCHEMA_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION;

    /** Property identifier: no namespace schema location. */
    protected static final String SCHEMA_NONS_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_NONS_LOCATION;

    /** Property identifier: JAXP schema source. */
    protected static final String JAXP_SCHEMA_SOURCE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE;

    /** Property identifier: JAXP schema language. */
    protected static final String JAXP_SCHEMA_LANGUAGE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE;

    /** Property identifier: root type definition. */
    protected static final String ROOT_TYPE_DEF =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_TYPE_DEFINITION_PROPERTY;
    
    /** Property identifier: root element declaration. */
    protected static final String ROOT_ELEMENT_DECL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_ELEMENT_DECLARATION_PROPERTY;

    /** Property identifier: Schema DV Factory */
    protected static final String SCHEMA_DV_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_DV_FACTORY_PROPERTY;

    /** Property identifier: xml schema version. */
    protected static final String XML_SCHEMA_VERSION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XML_SCHEMA_VERSION_PROPERTY;

    /** Property identifier: datatype xml version. */
    protected static final String DATATYPE_XML_VERSION = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_XML_VERSION_PROPERTY;

    // recognized features and properties

    /** Recognized features. */
    protected static final String[] RECOGNIZED_FEATURES =
        {
            VALIDATION,
            SCHEMA_VALIDATION,
            DYNAMIC_VALIDATION,
            SCHEMA_FULL_CHECKING,
            ALLOW_JAVA_ENCODINGS,
            CONTINUE_AFTER_FATAL_ERROR,
            STANDARD_URI_CONFORMANT_FEATURE,
            GENERATE_SYNTHETIC_ANNOTATIONS,
            VALIDATE_ANNOTATIONS,
            HONOUR_ALL_SCHEMALOCATIONS,
            USE_GRAMMAR_POOL_ONLY,
            IGNORE_XSI_TYPE,
            ID_IDREF_CHECKING,
            IDENTITY_CONSTRAINT_CHECKING,
            UNPARSED_ENTITY_CHECKING,
            NAMESPACE_GROWTH,
            TOLERATE_DUPLICATES,
            TYPE_ALTERNATIVES_CHECKING,
            CTA_FULL_XPATH_CHECKING,
            ASSERT_COMMENT_PI_CHECKING
        };


    /** Feature defaults. */
    protected static final Boolean[] FEATURE_DEFAULTS = { null,
        // NOTE: The following defaults are nulled out on purpose.
        //       If they are set, then when the XML Schema validator
        //       is constructed dynamically, these values may override
        //       those set by the application. This goes against the
        //       whole purpose of XMLComponent#getFeatureDefault but
        //       it can't be helped in this case. -Ac
        // NOTE: Instead of adding default values here, add them (and
        //       the corresponding recognized features) to the objects
        //       that have an XMLSchemaValidator instance as a member,
        //       such as the parser configurations. -PM
        null, //Boolean.FALSE,
        null, //Boolean.FALSE,
        null, //Boolean.FALSE,
        null, //Boolean.FALSE,
        null, //Boolean.FALSE,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    };

    /** Recognized properties. */
    protected static final String[] RECOGNIZED_PROPERTIES =
        {
            SYMBOL_TABLE,
            ERROR_REPORTER,
            ENTITY_RESOLVER,
            VALIDATION_MANAGER,
            SCHEMA_LOCATION,
            SCHEMA_NONS_LOCATION,
            JAXP_SCHEMA_SOURCE,
            JAXP_SCHEMA_LANGUAGE,
            ROOT_TYPE_DEF,
            ROOT_ELEMENT_DECL,
            SCHEMA_DV_FACTORY,
            XML_SCHEMA_VERSION,
            DATATYPE_XML_VERSION
        };

    /** Property defaults. */
    protected static final Object[] PROPERTY_DEFAULTS =
        { null, null, null, null, null, null, null, null, null, null, null, null, null};

    // this is the number of valuestores of each kind
    // we expect an element to have.  It's almost
    // never > 1; so leave it at that.
    protected static final int ID_CONSTRAINT_NUM = 1;
    
    // xsi:* attribute declarations
    static final XSAttributeDecl XSI_TYPE = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_TYPE);
    static final XSAttributeDecl XSI_NIL = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_NIL);
    static final XSAttributeDecl XSI_SCHEMALOCATION = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_SCHEMALOCATION);
    static final XSAttributeDecl XSI_NONAMESPACESCHEMALOCATION = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION);

    //
    protected static final Hashtable EMPTY_TABLE = new Hashtable();

    //
    // Data
    //

    /** current PSVI element info */
    protected ElementPSVImpl fCurrentPSVI = new ElementPSVImpl();

    // since it is the responsibility of each component to an
    // Augmentations parameter if one is null, to save ourselves from
    // having to create this object continually, it is created here.
    // If it is not present in calls that we're passing on, we *must*
    // clear this before we introduce it into the pipeline.
    protected final AugmentationsImpl fAugmentations = new AugmentationsImpl();

    // this is included for the convenience of handleEndElement
    protected XMLString fDefaultValue;

    // Validation features
    protected boolean fDynamicValidation = false;
    protected boolean fSchemaDynamicValidation = false;
    protected boolean fDoValidation = false;
    protected boolean fFullChecking = false;
    protected boolean fNormalizeData = true;
    protected boolean fSchemaElementDefault = true;
    protected boolean fAugPSVI = true;
    protected boolean fIdConstraint = false;
    protected boolean fUseGrammarPoolOnly = false;

    // Namespace growth feature
    protected boolean fNamespaceGrowth = false;
    
    /** Schema type: None, DTD, Schema */
    protected String fSchemaType = null;

    // to indicate whether we are in the scope of entity reference or CData
    protected boolean fEntityRef = false;
    protected boolean fInCDATA = false;

    // properties

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    /**
     * While parsing a document, keep the location of the document.
     */
    protected XMLLocator fLocator;

    protected ArrayList fXSITypeErrors = new ArrayList(4);

    protected IDContext fIDContext = null;
    
    protected String fDatatypeXMLVersion = null;
    
    protected NamespaceContext fNamespaceContext = null;
    
    /** Error reporter. */
    protected final XSIErrorReporter fXSIErrorReporter = new XSIErrorReporter();

    /** Entity resolver */
    protected XMLEntityResolver fEntityResolver;

    // updated during reset
    protected ValidationManager fValidationManager = null;
    protected XSValidationState fValidationState = new XSValidationState();
    protected XMLGrammarPool fGrammarPool;

    // schema location property values
    protected String fExternalSchemas = null;
    protected String fExternalNoNamespaceSchema = null;

    //JAXP Schema Source property
    protected Object fJaxpSchemaSource = null;

    /** Schema Grammar Description passed,  to give a chance to application to supply the Grammar */
    protected final XSDDescription fXSDDescription = new XSDDescription();
    protected final Hashtable fLocationPairs = new Hashtable();
    protected final Hashtable fExpandedLocationPairs = new Hashtable();
    protected final ArrayList fUnparsedLocations = new ArrayList();

    /** XML Schema 1.1 Support */
    short fSchemaVersion;

    /** XML Schema Constraints */
    XSConstraints fXSConstraints;

    // handlers

    /** Document handler. */
    protected XMLDocumentHandler fDocumentHandler;

    protected XMLDocumentSource fDocumentSource;
    
    // constants

    static final int INITIAL_STACK_SIZE = 8;
    static final int INC_STACK_SIZE = 8;

    //
    // Data
    //

    // Schema Normalization

    protected static final boolean DEBUG_NORMALIZATION = false;
    // temporary empty string buffer.
    protected final XMLString fEmptyXMLStr = new XMLString(null, 0, -1);
    // temporary character buffer, and empty string buffer.
    protected static final int BUFFER_SIZE = 20;
    protected final XMLString fNormalizedStr = new XMLString();
    protected boolean fFirstChunk = true;
    // got first chunk in characters() (SAX)
    protected boolean fTrailing = false; // Previous chunk had a trailing space
    protected short fWhiteSpace = -1; //whiteSpace: preserve/replace/collapse
    protected boolean fUnionType = false;

    /** Schema grammar resolver. */
    protected final XSGrammarBucket fGrammarBucket = new XSGrammarBucket();
    protected final SubstitutionGroupHandler fSubGroupHandler = new SubstitutionGroupHandler(this);

    /** the DV usd to convert xsi:type to a QName */
    // REVISIT: in new simple type design, make things in DVs static,
    //          so that we can QNameDV.getCompiledForm()
    //          using 1.0 xs:QName
    protected final XSSimpleType fQNameDV =
        (XSSimpleType) SchemaGrammar.getS4SGrammar(Constants.SCHEMA_VERSION_1_0).getGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME);

    protected final CMNodeFactory nodeFactory = new CMNodeFactory();
    /** used to build content models */
    // REVISIT: create decl pool, and pass it to each traversers
    protected final CMBuilder fCMBuilder = new CMBuilder(nodeFactory);
    
    // Schema grammar loader
    protected final XMLSchemaLoader fSchemaLoader =
        new XMLSchemaLoader(
                fXSIErrorReporter.fErrorReporter,
                fGrammarBucket,
                fSubGroupHandler,
                fCMBuilder);

    // state

    /** String representation of the validation root. */
    // REVISIT: what do we store here? QName, XPATH, some ID? use rawname now.
    protected String fValidationRoot;

    /** Skip validation: anything below this level should be skipped */
    protected int fSkipValidationDepth;

    /** anything above this level has validation_attempted != full */
    protected int fNFullValidationDepth;

    /** anything above this level has validation_attempted != none */
    protected int fNNoneValidationDepth;

    /** Element depth: -2: validator not in pipeline; >= -1 current depth. */
    protected int fElementDepth;

    /** Seen sub elements. */
    protected boolean fSubElement;

    /** Seen sub elements stack. */
    protected boolean[] fSubElementStack = new boolean[INITIAL_STACK_SIZE];

    /** Current element declaration. */
    protected XSElementDecl fCurrentElemDecl;

    /** Element decl stack. */
    protected XSElementDecl[] fElemDeclStack = new XSElementDecl[INITIAL_STACK_SIZE];

    /** nil value of the current element */
    protected boolean fNil;

    /** nil value stack */
    protected boolean[] fNilStack = new boolean[INITIAL_STACK_SIZE];

    /** notation value of the current element */
    protected XSNotationDecl fNotation;

    /** notation stack */
    protected XSNotationDecl[] fNotationStack = new XSNotationDecl[INITIAL_STACK_SIZE];

    /** Current type. */
    protected XSTypeDefinition fCurrentType;
    
    /** Failed assertions. */
    protected ObjectList fFailedAssertions;
    
    /** Type Alternative augmentation information. */
    protected XSTypeAlternative fTypeAlternative;

    /** type stack. */
    protected XSTypeDefinition[] fTypeStack = new XSTypeDefinition[INITIAL_STACK_SIZE];

    /** Current content model. */
    protected XSCMValidator fCurrentCM;

    /** Content model stack. */
    protected XSCMValidator[] fCMStack = new XSCMValidator[INITIAL_STACK_SIZE];

    /** the current state of the current content model */
    protected int[] fCurrCMState;

    /** stack to hold content model states */
    protected int[][] fCMStateStack = new int[INITIAL_STACK_SIZE][];

    /** whether the curret element is strictly assessed */
    protected boolean fStrictAssess = true;

    /** strict assess stack */
    protected boolean[] fStrictAssessStack = new boolean[INITIAL_STACK_SIZE];

    /** Temporary string buffers. */
    protected final StringBuffer fBuffer = new StringBuffer();

    /** Whether need to append characters to fBuffer */
    protected boolean fAppendBuffer = true;

    /** Did we see any character data? */
    protected boolean fSawText = false;

    /** stack to record if we saw character data */
    protected boolean[] fSawTextStack = new boolean[INITIAL_STACK_SIZE];

    /** Did we see non-whitespace character data? */
    protected boolean fSawCharacters = false;

    /** Stack to record if we saw character data outside of element content*/
    protected boolean[] fStringContent = new boolean[INITIAL_STACK_SIZE];

    /** temporary qname */
    protected final QName fTempQName = new QName();
    
    /** value of the "root-type-definition" property. */
    protected javax.xml.namespace.QName fRootTypeQName = null;
    protected XSTypeDefinition fRootTypeDefinition = null;
    
    /** value of the "root-element-declaration" property. */
    protected javax.xml.namespace.QName fRootElementDeclQName = null;
    protected XSElementDecl fRootElementDeclaration = null;
    
    protected int fIgnoreXSITypeDepth;
    
    protected boolean fIDCChecking;

    protected boolean fTypeAlternativesChecking;
    
    protected boolean fCommentsAndPIsForAssert;

    /** temporary validated info */
    protected ValidatedInfo fValidatedInfo = new ValidatedInfo();

    // used to validate default/fixed values against xsi:type
    // only need to check facets, so we set extraChecking to false (in reset)
    protected ValidationState fState4XsiType = new ValidationState();

    // used to apply default/fixed values
    // only need to check id/idref/entity, so we set checkFacets to false
    protected ValidationState fState4ApplyDefault = new ValidationState();

    // identity constraint information

    /**
     * Stack of active XPath matchers for identity constraints. All
     * active XPath matchers are notified of startElement
     * and endElement callbacks in order to perform their matches.
     * <p>
     * For each element with identity constraints, the selector of
     * each identity constraint is activated. When the selector matches
     * its XPath, then all the fields of the identity constraint are
     * activated.
     * <p>
     * <strong>Note:</strong> Once the activation scope is left, the
     * XPath matchers are automatically removed from the stack of
     * active matchers and no longer receive callbacks.
     */
    protected XPathMatcherStack fMatcherStack = new XPathMatcherStack();

    /** Cache of value stores for identity constraint fields. */
    protected ValueStoreCache fValueStoreCache = new ValueStoreCache();
    
    // assertion validator subcomponent
    protected XSDAssertionValidator fAssertionValidator = null;

    // variable to track validity of element content for simpleType->union. if a member type of union in XSD namespace can
    // successfully validate an element instance value, we don't process assertions for such union types in downstream checks. i.e.,
    // an element instance known to be valid doesn't require assertion evaluations.
    boolean fIsAssertProcessingNeededForSTUnionElem = true;

    // variable with similar semantics as fIsAssertProcessingNeededForSTUnionElem, but for attributes of an element instance (i.e.,
    // all attributes of one element instance).
    List fIsAssertProcessingNeededForSTUnionAttrs = new ArrayList();
    
    // 'type alternative' validator subcomponent
    protected XSDTypeAlternativeValidator fTypeAlternativeValidator = null;
    
    // a Vector list storing inheritable attributes
    Vector fInheritableAttrList = new Vector();
    
    // a Stack storing inheritable attribute count for the elements
    protected IntStack fInhrAttrCountStack = new IntStack();
    
    // Implements XSElementDeclHelper interface
    public XSElementDecl getGlobalElementDecl(QName element) {
        final SchemaGrammar sGrammar =
            findSchemaGrammar(
                XSDDescription.CONTEXT_ELEMENT,
                element.uri,
                null,
                element,
                null);
        if (sGrammar != null) {
            return sGrammar.getGlobalElementDecl(element.localpart);
        }
        return null;
    }
    
    /**
     * A wrapper of the standard error reporter. We'll store all schema errors
     * in this wrapper object, so that we can get all errors (error codes) of
     * a specific element. This is useful for PSVI.
     */
    protected final class XSIErrorReporter {

        // the error reporter property
        XMLErrorReporter fErrorReporter;

        // store error codes; starting position of the errors for each element;
        // number of element (depth); and whether to record error
        Vector fErrors = new Vector();
        int[] fContext = new int[INITIAL_STACK_SIZE];
        int fContextCount;

        // set the external error reporter, clear errors
        public void reset(XMLErrorReporter errorReporter) {
            fErrorReporter = errorReporter;
            fErrors.removeAllElements();
            fContextCount = 0;
        }

        // should be called when starting process an element or an attribute.
        // store the starting position for the current context
        public void pushContext() {
            if (!fAugPSVI) {
                return;
            }
            // resize array if necessary
            if (fContextCount == fContext.length) {
                int newSize = fContextCount + INC_STACK_SIZE;
                int[] newArray = new int[newSize];
                System.arraycopy(fContext, 0, newArray, 0, fContextCount);
                fContext = newArray;
            }

            fContext[fContextCount++] = fErrors.size();
        }

        // should be called on endElement: get all errors of the current element
        public String[] popContext() {
            if (!fAugPSVI) {
                return null;
            }
            // get starting position of the current element
            int contextPos = fContext[--fContextCount];
            // number of errors of the current element
            int size = fErrors.size() - contextPos;
            // if no errors, return null
            if (size == 0)
                return null;
            // copy errors from the list to an string array
            String[] errors = new String[size];
            for (int i = 0; i < size; i++) {
                errors[i] = (String) fErrors.elementAt(contextPos + i);
            }
            // remove errors of the current element
            fErrors.setSize(contextPos);
            return errors;
        }

        // should be called when an attribute is done: get all errors of
        // this attribute, but leave the errors to the containing element
        // also called after an element was strictly assessed.
        public String[] mergeContext() {
            if (!fAugPSVI) {
                return null;
            }
            // get starting position of the current element
            int contextPos = fContext[--fContextCount];
            // number of errors of the current element
            int size = fErrors.size() - contextPos;
            // if no errors, return null
            if (size == 0)
                return null;
            // copy errors from the list to an string array
            String[] errors = new String[size];
            for (int i = 0; i < size; i++) {
                errors[i] = (String) fErrors.elementAt(contextPos + i);
            }
            // don't resize the vector: leave the errors for this attribute
            // to the containing element
            return errors;
        }

        public void reportError(String domain, String key, Object[] arguments, short severity)
            throws XNIException {
            String message = fErrorReporter.reportError(domain, key, arguments, severity);
            if (fAugPSVI) {
                fErrors.addElement(key);
                fErrors.addElement(message);
            }
        } // reportError(String,String,Object[],short)

        public void reportError(
            XMLLocator location,
            String domain,
            String key,
            Object[] arguments,
            short severity)
            throws XNIException {
            String message = fErrorReporter.reportError(location, domain, key, arguments, severity);
            if (fAugPSVI) {
                fErrors.addElement(key);
                fErrors.addElement(message);
            }
        } // reportError(XMLLocator,String,String,Object[],short)
    }
    
    //this is the function where logic of retrieving grammar is written , parser first tries to get the grammar from
    //the local pool, if not in local pool, it gives chance to application to be able to retrieve the grammar, then it
    //tries to parse the grammar using location hints from the give namespace.
    SchemaGrammar findSchemaGrammar(
        short contextType,
        String namespace,
        QName enclosingElement,
        QName triggeringComponent,
        XMLAttributes attributes) {
        SchemaGrammar grammar = null;
        //get the grammar from local pool...
        grammar = fGrammarBucket.getGrammar(namespace);
        if (grammar == null) {
            fXSDDescription.setNamespace(namespace);
            if (fGrammarPool != null) {
                grammar = (SchemaGrammar) fGrammarPool.retrieveGrammar(fXSDDescription);
                if (grammar != null) {
                    // put this grammar into the bucket, along with grammars
                    // imported by it (directly or indirectly)
                    if (!fGrammarBucket.putGrammar(grammar, true, fNamespaceGrowth)) {
                        // REVISIT: a conflict between new grammar(s) and grammars
                        // in the bucket. What to do? A warning? An exception?
                        fXSIErrorReporter.fErrorReporter.reportError(
                            XSMessageFormatter.SCHEMA_DOMAIN,
                            "GrammarConflict",
                            null,
                            XMLErrorReporter.SEVERITY_WARNING);
                        grammar = null;
                    }
                }
            }
        }

        if (!fUseGrammarPoolOnly && (grammar == null || 
            (fNamespaceGrowth && !hasSchemaComponent(grammar, contextType, triggeringComponent)))) {
            fXSDDescription.reset();
            fXSDDescription.fContextType = contextType;
            fXSDDescription.setNamespace(namespace);
            fXSDDescription.fEnclosedElementName = enclosingElement;
            fXSDDescription.fTriggeringComponent = triggeringComponent;
            fXSDDescription.fAttributes = attributes;
            if (fLocator != null) {
                fXSDDescription.setBaseSystemId(fLocator.getExpandedSystemId());
            }

            Hashtable locationPairs = fLocationPairs;
            Object locationArray =
                locationPairs.get(namespace == null ? XMLSymbols.EMPTY_STRING : namespace);
            if (locationArray != null) {
                String[] temp = ((XMLSchemaLoader.LocationArray) locationArray).getLocationArray();
                if (temp.length != 0) {
                    setLocationHints(fXSDDescription, temp, grammar);
                }
            }

            if (grammar == null || fXSDDescription.fLocationHints != null) {
                boolean toParseSchema = true;
                if (grammar != null) {
                     // use location hints instead
                    locationPairs = EMPTY_TABLE;
                }

                // try to parse the grammar using location hints from that namespace..
                try {
                    XMLInputSource xis =
                        XMLSchemaLoader.resolveDocument(
                            fXSDDescription,
                            locationPairs,
                            fEntityResolver);
                    if (grammar != null && fNamespaceGrowth) {
                        try {
                            // if we are dealing with a different schema location, then include the new schema
                            // into the existing grammar
                            if (grammar.getDocumentLocations().contains(XMLEntityManager.expandSystemId(xis.getSystemId(), xis.getBaseSystemId(), false))) {
                                toParseSchema = false; 
                            }
                        }
                        catch (MalformedURIException e) {
                        }
                    }
                    if (toParseSchema) {
                        grammar = fSchemaLoader.loadSchema(fXSDDescription, xis, fLocationPairs);
                    }
                } 
                catch (IOException ex) {
                    final String [] locationHints = fXSDDescription.getLocationHints();
                    fXSIErrorReporter.fErrorReporter.reportError(
                        XSMessageFormatter.SCHEMA_DOMAIN,
                        "schema_reference.4",
                        new Object[] { locationHints != null ? locationHints[0] : XMLSymbols.EMPTY_STRING },
                        XMLErrorReporter.SEVERITY_WARNING, ex);
                }
            }
        }

        return grammar;

    } //findSchemaGrammar
    
    private boolean hasSchemaComponent(SchemaGrammar grammar, short contextType, QName triggeringComponent) {
        if (grammar != null && triggeringComponent != null) {
            String localName = triggeringComponent.localpart;
            if (localName != null && localName.length() > 0) {
                switch (contextType) {
                    case XSDDescription.CONTEXT_ELEMENT:
                        return grammar.getElementDeclaration(localName) != null;
                    case XSDDescription.CONTEXT_ATTRIBUTE:
                        return grammar.getAttributeDeclaration(localName) != null;
                    case XSDDescription.CONTEXT_XSITYPE:
                        return grammar.getTypeDefinition(localName) != null;
                }
            }
        }
        return false;
    }

    private void setLocationHints(XSDDescription desc, String[] locations, SchemaGrammar grammar) {
        int length = locations.length;
        if (grammar == null) {
            fXSDDescription.fLocationHints = new String[length];
            System.arraycopy(locations, 0, fXSDDescription.fLocationHints, 0, length);
        }
        else {
            setLocationHints(desc, locations, grammar.getDocumentLocations());
        }
    }
   
    private void setLocationHints(XSDDescription desc, String[] locations, StringList docLocations) {
        int length = locations.length;
        String[] hints = new String[length];
        int counter = 0;

        for (int i=0; i<length; i++) {
            if (!docLocations.contains(locations[i])) {
                hints[counter++] = locations[i];
            }
        }

        if (counter > 0) {
            if (counter == length) {
                fXSDDescription.fLocationHints = hints;
            }
            else {
                fXSDDescription.fLocationHints = new String[counter];
                System.arraycopy(hints, 0, fXSDDescription.fLocationHints, 0, counter);
            }
        }
    }
    
    void reportSchemaError(String key, Object[] arguments) {
        if (fDoValidation)
            fXSIErrorReporter.reportError(
                XSMessageFormatter.SCHEMA_DOMAIN,
                key,
                arguments,
                XMLErrorReporter.SEVERITY_ERROR);
    }
    
    // xpath matcher information

    /**
     * Stack of XPath matchers for identity constraints.
     */
    protected static class XPathMatcherStack {

        //
        // Data
        //

        /** Active matchers. */
        protected XPathMatcher[] fMatchers = new XPathMatcher[4];

        /** Count of active matchers. */
        protected int fMatchersCount;

        /** Offset stack for contexts. */
        protected IntStack fContextStack = new IntStack();

        //
        // Constructors
        //

        public XPathMatcherStack() {
        } // <init>()

        //
        // Public methods
        //

        /** Resets the XPath matcher stack. */
        public void clear() {
            for (int i = 0; i < fMatchersCount; i++) {
                fMatchers[i] = null;
            }
            fMatchersCount = 0;
            fContextStack.clear();
        } // clear()

        /** Returns the size of the stack. */
        public int size() {
            return fContextStack.size();
        } // size():int

        /** Returns the count of XPath matchers. */
        public int getMatcherCount() {
            return fMatchersCount;
        } // getMatcherCount():int

        /** Adds a matcher. */
        public void addMatcher(XPathMatcher matcher) {
            ensureMatcherCapacity();
            fMatchers[fMatchersCount++] = matcher;
        } // addMatcher(XPathMatcher)

        /** Returns the XPath matcher at the specified index. */
        public XPathMatcher getMatcherAt(int index) {
            return fMatchers[index];
        } // getMatcherAt(index):XPathMatcher

        /** Pushes a new context onto the stack. */
        public void pushContext() {
            fContextStack.push(fMatchersCount);
        } // pushContext()

        /** Pops a context off of the stack. */
        public void popContext() {
            fMatchersCount = fContextStack.pop();
        } // popContext()

        //
        // Private methods
        //

        /** Ensures the size of the matchers array. */
        private void ensureMatcherCapacity() {
            if (fMatchersCount == fMatchers.length) {
                XPathMatcher[] array = new XPathMatcher[fMatchers.length * 2];
                System.arraycopy(fMatchers, 0, array, 0, fMatchers.length);
                fMatchers = array;
            }
        } // ensureMatcherCapacity()

    } // class XPathMatcherStack


    // the purpose of this class is to enable IdentityConstraint,int
    // pairs to be used easily as keys in Hashtables.
    protected static final class LocalIDKey {

        public IdentityConstraint fId;
        public int fDepth;

        public LocalIDKey() {
        }

        public LocalIDKey(IdentityConstraint id, int depth) {
            fId = id;
            fDepth = depth;
        } // init(IdentityConstraint, int)

        // object method
        public int hashCode() {
            return fId.hashCode() + fDepth;
        }

        public boolean equals(Object localIDKey) {
            if (localIDKey instanceof LocalIDKey) {
                LocalIDKey lIDKey = (LocalIDKey) localIDKey;
                return (lIDKey.fId == fId && lIDKey.fDepth == fDepth);
            }
            return false;
        }
    } // class LocalIDKey
    
    // value store implementations
    
    /**
     * Value store implementation base class. There are specific subclasses
     * for handling unique, key, and keyref.
     */
    protected abstract class ValueStoreBase implements ValueStore {

        //
        // Data
        //

        /** Identity constraint. */
        protected IdentityConstraint fIdentityConstraint;
        protected int fFieldCount = 0;
        protected Field[] fFields = null;
        protected String fElementName;
        /** current data */
        protected Object[] fLocalValues = null;
        protected short[] fLocalValueTypes = null;
        protected ShortList[] fLocalItemValueTypes = null;

        /** Current data value count. */
        protected int fValuesCount;

        /** global data */
        public final Vector fValues = new Vector();
        public ShortVector fValueTypes = null;
        public Vector fItemValueTypes = null;
        
        private boolean fUseValueTypeVector = false;
        private int fValueTypesLength = 0; 
        private short fValueType = 0;
        
        private boolean fUseItemValueTypeVector = false;
        private int fItemValueTypesLength = 0;
        private ShortList fItemValueType = null;

        /** buffer for error messages */
        final StringBuffer fTempBuffer = new StringBuffer();

        //
        // Constructors
        //

        /** Constructs a value store for the specified identity constraint. */
        protected ValueStoreBase(IdentityConstraint identityConstraint, String elementName) {
            fElementName = elementName;
            fIdentityConstraint = identityConstraint;
            fFieldCount = fIdentityConstraint.getFieldCount();
            fFields = new Field[fFieldCount];
            fLocalValues = new Object[fFieldCount];
            fLocalValueTypes = new short[fFieldCount];
            fLocalItemValueTypes = new ShortList[fFieldCount];
            for (int i = 0; i < fFieldCount; i++) {
                fFields[i] = fIdentityConstraint.getFieldAt(i);
            }
        } // <init>(IdentityConstraint)

        //
        // Public methods
        //

        // destroys this ValueStore; useful when, for instance, a
        // locally-scoped ID constraint is involved.
        public void clear() {
            fValuesCount = 0;
            fUseValueTypeVector = false;
            fValueTypesLength = 0; 
            fValueType = 0;
            fUseItemValueTypeVector = false;
            fItemValueTypesLength = 0;
            fItemValueType = null;
            fValues.setSize(0);
            if (fValueTypes != null) {
                fValueTypes.clear();
            }
            if (fItemValueTypes != null) {
                fItemValueTypes.setSize(0);
            }
        } // end clear():void

        // appends the contents of one ValueStore to those of us.
        public void append(ValueStoreBase newVal) {
            for (int i = 0; i < newVal.fValues.size(); i++) {
                fValues.addElement(newVal.fValues.elementAt(i));

                // REVISIT:
                addValueType(newVal.getValueTypeAt(i));
                addItemValueType(newVal.getItemValueTypeAt(i));
            }
        } // append(ValueStoreBase)

        /** Start scope for value store. */
        public void startValueScope() {
            fValuesCount = 0;
            for (int i = 0; i < fFieldCount; i++) {
                fLocalValues[i] = null;
                fLocalValueTypes[i] = 0;
                fLocalItemValueTypes[i] = null;
            }
        } // startValueScope()

        /** Ends scope for value store. */
        public void endValueScope() {

            if (fValuesCount == 0) {
                if (fIdentityConstraint.getCategory() == IdentityConstraint.IC_KEY) {
                    String code = "AbsentKeyValue";
                    String cName = fIdentityConstraint.getIdentityConstraintName();
                    reportSchemaError(code, new Object[] { fElementName, cName });
                }
                return;
            }

            // Validation Rule: Identity-constraint Satisfied
            // 4.2 If the {identity-constraint category} is key, then all of the following must be true:
            // 4.2.1 The target node set and the qualified node set are equal, that is, every member of the 
            // target node set is also a member of the qualified node set and vice versa.
            //
            // If the IDC is a key check whether we have all the fields.
            if (fValuesCount != fFieldCount) {
                if (fIdentityConstraint.getCategory() == IdentityConstraint.IC_KEY) {
                    String code = "KeyNotEnoughValues";
                    UniqueOrKey key = (UniqueOrKey) fIdentityConstraint;
                    String cName = key.getIdentityConstraintName();
                    reportSchemaError(code, new Object[] { fElementName, cName });
                }
                return;
            }

        } // endValueScope()

        // This is needed to allow keyref's to look for matched keys
        // in the correct scope.  Unique and Key may also need to
        // override this method for purposes of their own.
        // This method is called whenever the DocumentFragment
        // of an ID Constraint goes out of scope.
        public void endDocumentFragment() {
        } // endDocumentFragment():void

        /**
         * Signals the end of the document. This is where the specific
         * instances of value stores can verify the integrity of the
         * identity constraints.
         */
        public void endDocument() {
        } // endDocument()

        //
        // ValueStore methods
        //

        /* reports an error if an element is matched
         * has nillable true and is matched by a key.
         */

        public void reportError(String key, Object[] args) {
            reportSchemaError(key, args);
        } // reportError(String,Object[])

        /**
         * Adds the specified value to the value store.
         *
         * @param field The field associated to the value. This reference
         *              is used to ensure that each field only adds a value
         *              once within a selection scope.
         * @param mayMatch a flag indiciating whether the field may be matched.
         * @param actualValue The value to add.
         * @param valueType Type of the value to add.
         * @param itemValueType If the value is a list, a list of types for each of the values in the list.
         */
        public void addValue(Field field, boolean mayMatch, Object actualValue, short valueType, ShortList itemValueType) {
            int i;
            for (i = fFieldCount - 1; i > -1; i--) {
                if (fFields[i] == field) {
                    break;
                }
            }
            // do we even know this field?
            if (i == -1) {
                String code = "UnknownField";
                String cName = fIdentityConstraint.getIdentityConstraintName();
                reportSchemaError(code, new Object[] { field.toString(), fElementName, cName });
                return;
            }
            if (!mayMatch) {
                String code = "FieldMultipleMatch";
                String cName = fIdentityConstraint.getIdentityConstraintName();
                reportSchemaError(code, new Object[] { field.toString(), cName });
            } 
            else {
                fValuesCount++;
            }
            fLocalValues[i] = actualValue;
            fLocalValueTypes[i] = valueType;
            fLocalItemValueTypes[i] = itemValueType;
            if (fValuesCount == fFieldCount) {
                checkDuplicateValues();
                // store values
                for (i = 0; i < fFieldCount; i++) {
                    fValues.addElement(fLocalValues[i]);
                    addValueType(fLocalValueTypes[i]);
                    addItemValueType(fLocalItemValueTypes[i]);
                }
            }
        } // addValue(String,Field)

        /**
         * Sets the name of the element which holds the identity constraint
         * that is stored in this value store
         */
        public void setElementName(String elementName) {
            fElementName = elementName;
        }

        /**
         * Returns the name of the element which holds the identity constraint 
         * that is stored in this value store
         */
        public String getElementName() {
            return fElementName;
        } // getElementName():String

        /**
         * Returns true if this value store contains the locally scoped value stores
         */
        public boolean contains() {
            // REVISIT: we can improve performance by using hash codes, instead of
            // traversing global vector that could be quite large.
            int next = 0;
            final int size = fValues.size();
            LOOP : for (int i = 0; i < size; i = next) {
                next = i + fFieldCount;
                for (int j = 0; j < fFieldCount; j++) {
                    final Object value1 = fLocalValues[j];
                    final Object value2 = fValues.elementAt(i);
                    final short valueType1 = fLocalValueTypes[j];
                    final short valueType2 = getValueTypeAt(i);
                    final ShortList typeList1 = isListType(valueType1) ? fLocalItemValueTypes[j] : null;
                    final ShortList typeList2 = isListType(valueType2) ? getItemValueTypeAt(i) : null;

                    if (!EqualityHelper.isEqual(value1, value2, valueType1, valueType2, typeList1, typeList2, fSchemaVersion)) {
                        continue LOOP;
                    }
                    i++;
                }
                // found it
                return true;
            }
            // didn't find it
            return false;
        } // contains():boolean

        /**
         * Returns -1 if this value store contains the specified
         * values, otherwise the index of the first field in the
         * key sequence.
         */
        public int contains(ValueStoreBase vsb) {
            
            final Vector values = vsb.fValues;         
            final int size1 = values.size();
            if (fFieldCount <= 1) {
                LOOP: for (int i = 0; i < size1; ++i) {
                    final Object value1 = values.elementAt(i);
                    final short valueType1 = vsb.getValueTypeAt(i);
                    final ShortList typeList1 = isListType(valueType1) ? vsb.getItemValueTypeAt(i) : null;
                    for (int j=0; j < fValues.size(); ++j) {
                        final Object value2 = fValues.elementAt(j);
                        final short valueType2 = getValueTypeAt(j);
                        final ShortList typeList2 = isListType(valueType2) ? getItemValueTypeAt(j) : null;
                        if (EqualityHelper.isEqual(value1, value2, valueType1, valueType2, typeList1, typeList2, fSchemaVersion)) {
                            continue LOOP;
                        }
                    }
                    return i;
                }
            }
            /** Handle n-tuples. **/
            else {
                final int size2 = fValues.size();
                /** Iterate over each set of fields. **/
                OUTER: for (int i = 0; i < size1; i += fFieldCount) {
                    /** Check whether this set is contained in the value store. **/
                    INNER: for (int j = 0; j < size2; j += fFieldCount) {
                        for (int k = 0; k < fFieldCount; ++k) {
                            final Object value1 = values.elementAt(i+k);
                            final Object value2 = fValues.elementAt(j+k);
                            final short valueType1 = vsb.getValueTypeAt(i+k);
                            final short valueType2 = getValueTypeAt(j+k);
                            final ShortList typeList1 = isListType(valueType1) ? vsb.getItemValueTypeAt(i+k) : null;
                            final ShortList typeList2 = isListType(valueType2) ? getItemValueTypeAt(j+k) : null;
                            
                            if (!EqualityHelper.isEqual(value1, value2, valueType1, valueType2, typeList1, typeList2, fSchemaVersion)) {
                                continue INNER;
                            }
                        }
                        continue OUTER;
                    }
                    return i;
                }
            }
            return -1;
            
        } // contains(Vector):Object

        //
        // Protected methods
        //

        protected void checkDuplicateValues() {
            // no-op
        } // duplicateValue(Hashtable)

        /** Returns a string of the specified values. */
        protected String toString(Object[] values) {

            // no values
            int size = values.length;
            if (size == 0) {
                return "";
            }

            fTempBuffer.setLength(0);

            // construct value string
            for (int i = 0; i < size; i++) {
                if (i > 0) {
                    fTempBuffer.append(',');
                }
                fTempBuffer.append(values[i]);
            }
            return fTempBuffer.toString();

        } // toString(Object[]):String
        
        /** Returns a string of the specified values. */
        protected String toString(Vector values, int start, int length) {

            // no values
            if (length == 0) {
                return "";
            }
            
            // one value
            if (length == 1) {
                return String.valueOf(values.elementAt(start));
            }

            // construct value string
            StringBuffer str = new StringBuffer();
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    str.append(',');
                }
                str.append(values.elementAt(start + i));
            }
            return str.toString();

        } // toString(Vector,int,int):String

        //
        // Object methods
        //

        /** Returns a string representation of this object. */
        public String toString() {
            String s = super.toString();
            int index1 = s.lastIndexOf('$');
            if (index1 != -1) {
                s = s.substring(index1 + 1);
            }
            int index2 = s.lastIndexOf('.');
            if (index2 != -1) {
                s = s.substring(index2 + 1);
            }
            return s + '[' + fIdentityConstraint + ']';
        } // toString():String
        
        //
        // Private methods
        //
        private boolean isListType(short type) {
            return type == XSConstants.LIST_DT || type == XSConstants.LISTOFUNION_DT;
        }
        
        private void addValueType(short type) {
            if (fUseValueTypeVector) {
                fValueTypes.add(type);
            }
            else if (fValueTypesLength++ == 0) {
                fValueType = type;
            }
            else if (fValueType != type) {
                fUseValueTypeVector = true;
                if (fValueTypes == null) {
                    fValueTypes = new ShortVector(fValueTypesLength * 2);
                }
                for (int i = 1; i < fValueTypesLength; ++i) {
                    fValueTypes.add(fValueType);
                }
                fValueTypes.add(type);
            }
        }
        
        private short getValueTypeAt(int index) {
            if (fUseValueTypeVector) {
                return fValueTypes.valueAt(index);
            }
            return fValueType;
        }
        
        private void addItemValueType(ShortList itemValueType) {
            if (fUseItemValueTypeVector) {
                fItemValueTypes.add(itemValueType);
            }
            else if (fItemValueTypesLength++ == 0) {
                fItemValueType = itemValueType;
            }
            else if (!(fItemValueType == itemValueType ||
                    (fItemValueType != null && fItemValueType.equals(itemValueType)))) {
                fUseItemValueTypeVector = true;
                if (fItemValueTypes == null) {
                    fItemValueTypes = new Vector(fItemValueTypesLength * 2);
                }
                for (int i = 1; i < fItemValueTypesLength; ++i) {
                    fItemValueTypes.add(fItemValueType);
                }
                fItemValueTypes.add(itemValueType);
            }
        }
        
        private ShortList getItemValueTypeAt(int index) {
            if (fUseItemValueTypeVector) {
                return (ShortList) fItemValueTypes.elementAt(index);
            }
            return fItemValueType;
        }
        
    } // class ValueStoreBase
    
    /**
     * Key value store.
     */
    protected class KeyValueStore extends ValueStoreBase {

        // REVISIT: Implement a more efficient storage mechanism. -Ac

        //
        // Constructors
        //

        /** Constructs a key value store. */
        public KeyValueStore(UniqueOrKey key, String elementName) {
            super(key, elementName);
        } // <init>(Key)

        //
        // ValueStoreBase protected methods
        //

        /**
         * Called when a duplicate value is added.
         */
        protected void checkDuplicateValues() {
            if (contains()) {
                String code = "DuplicateKey";
                String value = toString(fLocalValues);
                String cName = fIdentityConstraint.getIdentityConstraintName();
                reportSchemaError(code, new Object[] { value, fElementName, cName });
            }
        } // duplicateValue(Hashtable)

    } // class KeyValueStore

    /**
     * Key reference value store.
     */
    protected class KeyRefValueStore extends ValueStoreBase {

        //
        // Data
        //

        /** Key value store. */
        protected ValueStoreBase fKeyValueStore;

        //
        // Constructors
        //

        /** Constructs a key value store. */
        public KeyRefValueStore(KeyRef keyRef, KeyValueStore keyValueStore, String elementName) { 
            super(keyRef, elementName);
            fKeyValueStore = keyValueStore;
        } // <init>(KeyRef)

        //
        // ValueStoreBase methods
        //

        // end the value Scope; here's where we have to tie
        // up keyRef loose ends.
        public void endDocumentFragment() {

            // do all the necessary management...
            super.endDocumentFragment();

            // verify references
            // get the key store corresponding (if it exists):
            fKeyValueStore =
                (ValueStoreBase) fValueStoreCache.fGlobalIDConstraintMap.get(
                    ((KeyRef) fIdentityConstraint).getKey());

            if (fKeyValueStore == null) {
                // report error
                String code = "KeyRefOutOfScope";
                String value = fIdentityConstraint.getName();
                reportSchemaError(code, new Object[] { value });
                return;
            }
            int errorIndex = fKeyValueStore.contains(this);
            if (errorIndex != -1) {
                String code = "KeyNotFound";
                String values = toString(fValues, errorIndex, fFieldCount);
                String name = fIdentityConstraint.getName();
                reportSchemaError(code, new Object[] { name, values, fElementName });
            }

        } // endDocumentFragment()

        /** End document. */
        public void endDocument() {
            super.endDocument();

        } // endDocument()

    } // class KeyRefValueStore
    
    // a utility method for Identity constraints
    private void activateSelectorFor(IdentityConstraint ic) {
        Selector selector = ic.getSelector();
        FieldActivator activator = this;
        if (selector == null)
            return;
        XPathMatcher matcher = selector.createMatcher(activator, fElementDepth);
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            matcher.setXPathDefaultNamespace(selector.getXPathDefaultNamespace());
        }
        fMatcherStack.addMatcher(matcher);
        matcher.startDocumentFragment();
    }
    
    //
    // FieldActivator methods
    //
    
    /**
     * Request to activate the specified field. This method returns the
     * matcher for the field.
     *
     * @param field The field to activate.
     */
    public XPathMatcher activateField(Field field, int initialDepth) {
        ValueStore valueStore =
            fValueStoreCache.getValueStoreFor(field.getIdentityConstraint(), initialDepth);
        XPathMatcher matcher = field.createMatcher(valueStore);
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            matcher.setXPathDefaultNamespace(field.getXPathDefaultNamespace());
        }
        fMatcherStack.addMatcher(matcher);
        matcher.startDocumentFragment();
        return matcher;
    } // activateField(Field):XPathMatcher
    
    /**
     * Start the value scope for the specified identity constraint. This
     * method is called when the selector matches in order to initialize
     * the value store.
     *
     * @param identityConstraint The identity constraint.
     */
    public void startValueScopeFor(IdentityConstraint identityConstraint, int initialDepth) {

        ValueStoreBase valueStore =
            fValueStoreCache.getValueStoreFor(identityConstraint, initialDepth);
        valueStore.startValueScope();

    } // startValueScopeFor(IdentityConstraint identityConstraint)
    
    /**
     * Ends the value scope for the specified identity constraint.
     *
     * @param identityConstraint The identity constraint.
     */
    public void endValueScopeFor(IdentityConstraint identityConstraint, int initialDepth) {

        ValueStoreBase valueStore =
            fValueStoreCache.getValueStoreFor(identityConstraint, initialDepth);
        valueStore.endValueScope();

    } // endValueScopeFor(IdentityConstraint)
    
    /**
     * Unique value store.
     */
    protected class UniqueValueStore extends ValueStoreBase {

        //
        // Constructors
        //

        /** Constructs a unique value store. */
        public UniqueValueStore(UniqueOrKey unique, String elementName) {
            super(unique, elementName);
        } // <init>(Unique)

        //
        // ValueStoreBase protected methods
        //

        /**
         * Called when a duplicate value is added.
         */
        protected void checkDuplicateValues() {
            // is this value as a group duplicated?
            if (contains()) {
                String code = "DuplicateUnique";
                String value = toString(fLocalValues);
                String cName = fIdentityConstraint.getIdentityConstraintName();
                reportSchemaError(code, new Object[] { value, fElementName, cName });
            }
        } // duplicateValue(Hashtable)

    } // class UniqueValueStore
    
    // value store management

    /**
     * Value store cache. This class is used to store the values for
     * identity constraints.
     */
    protected class ValueStoreCache {

        //
        // Data
        //
        final LocalIDKey fLocalId = new LocalIDKey();
        // values stores

        /** stores all global Values stores. */
        protected final ArrayList fValueStores = new ArrayList();

        /**
         * Values stores associated to specific identity constraints.
         * This hashtable maps IdentityConstraints and
         * the 0-based element on which their selectors first matched to
         * a corresponding ValueStore.  This should take care
         * of all cases, including where ID constraints with
         * descendant-or-self axes occur on recursively-defined
         * elements.
         */
        protected final HashMap fIdentityConstraint2ValueStoreMap = new HashMap();

        // sketch of algorithm:
        // - when a constraint is first encountered, its
        //   values are stored in the (local) fIdentityConstraint2ValueStoreMap;
        // - Once it is validated (i.e., when it goes out of scope),
        //   its values are merged into the fGlobalIDConstraintMap;
        // - as we encounter keyref's, we look at the global table to
        //    validate them.
        //
        // The fGlobalIDMapStack has the following structure:
        // - validation always occurs against the fGlobalIDConstraintMap
        // (which comprises all the "eligible" id constraints);
        // When an endElement is found, this Hashtable is merged with the one
        // below in the stack.
        // When a start tag is encountered, we create a new
        // fGlobalIDConstraintMap.
        // i.e., the top of the fGlobalIDMapStack always contains
        // the preceding siblings' eligible id constraints;
        // the fGlobalIDConstraintMap contains descendants+self.
        // keyrefs can only match descendants+self.
        protected final Stack fGlobalMapStack = new Stack();
        protected final HashMap fGlobalIDConstraintMap = new HashMap();

        //
        // Constructors
        //

        /** Default constructor. */
        public ValueStoreCache() {
        } // <init>()

        //
        // Public methods
        //

        /** Resets the identity constraint cache. */
        public void startDocument() {
            fValueStores.clear();
            fIdentityConstraint2ValueStoreMap.clear();
            fGlobalIDConstraintMap.clear();
            fGlobalMapStack.removeAllElements();
        } // startDocument()

        // startElement:  pushes the current fGlobalIDConstraintMap
        // onto fGlobalMapStack and clears fGlobalIDConstraint map.
        public void startElement() {
            // only clone the map when there are elements
            if (fGlobalIDConstraintMap.size() > 0)
                fGlobalMapStack.push(fGlobalIDConstraintMap.clone());
            else
                fGlobalMapStack.push(null);
            fGlobalIDConstraintMap.clear();
        } // startElement(void)

        /** endElement():  merges contents of fGlobalIDConstraintMap with the
         * top of fGlobalMapStack into fGlobalIDConstraintMap.
         */
        public void endElement() {
            if (fGlobalMapStack.isEmpty()) {
                return; // must be an invalid doc!
            }
            HashMap oldMap = (HashMap) fGlobalMapStack.pop();
            // return if there is no element
            if (oldMap == null) {
                return;
            }

            Iterator entries = oldMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                IdentityConstraint id = (IdentityConstraint) entry.getKey();
                ValueStoreBase oldVal = (ValueStoreBase) entry.getValue();
                if (oldVal != null) {
                    ValueStoreBase currVal = (ValueStoreBase) fGlobalIDConstraintMap.get(id);
                    if (currVal == null) {
                        fGlobalIDConstraintMap.put(id, oldVal);
                    }
                    else if (currVal != oldVal) {
                        currVal.append(oldVal);
                    }
                }
            }
        } // endElement()

        /**
         * Initializes the value stores for the specified element
         * declaration.
         */
        public void initValueStoresFor(XSElementDecl eDecl, FieldActivator activator) {
            // initialize value stores for unique fields
            IdentityConstraint[] icArray = eDecl.fIDConstraints;
            int icCount = eDecl.fIDCPos;
            for (int i = 0; i < icCount; i++) {
                switch (icArray[i].getCategory()) {
                    case (IdentityConstraint.IC_UNIQUE) :
                        // initialize value stores for unique fields
                        UniqueOrKey unique = (UniqueOrKey) icArray[i];
                        LocalIDKey toHash = new LocalIDKey(unique, fElementDepth);
                        UniqueValueStore uniqueValueStore =
                            (UniqueValueStore) fIdentityConstraint2ValueStoreMap.get(toHash);
                        if (uniqueValueStore == null) {
                            uniqueValueStore = new UniqueValueStore(unique, eDecl.getName());
                            fIdentityConstraint2ValueStoreMap.put(toHash, uniqueValueStore);
                        } else {
                            uniqueValueStore.clear();
                            uniqueValueStore.setElementName(eDecl.getName());
                        }
                        fValueStores.add(uniqueValueStore);
                        activateSelectorFor(icArray[i]);
                        break;
                    case (IdentityConstraint.IC_KEY) :
                        // initialize value stores for key fields
                        UniqueOrKey key = (UniqueOrKey) icArray[i];
                        toHash = new LocalIDKey(key, fElementDepth);
                        KeyValueStore keyValueStore =
                            (KeyValueStore) fIdentityConstraint2ValueStoreMap.get(toHash);
                        if (keyValueStore == null) {
                            keyValueStore = new KeyValueStore(key, eDecl.getName());
                            fIdentityConstraint2ValueStoreMap.put(toHash, keyValueStore);
                        } else {
                            keyValueStore.clear();
                            keyValueStore.setElementName(eDecl.getName());
                        }
                        fValueStores.add(keyValueStore);
                        activateSelectorFor(icArray[i]);
                        break;
                    case (IdentityConstraint.IC_KEYREF) :
                        // initialize value stores for keyRef fields
                        KeyRef keyRef = (KeyRef) icArray[i];
                        toHash = new LocalIDKey(keyRef, fElementDepth);
                        KeyRefValueStore keyRefValueStore =
                            (KeyRefValueStore) fIdentityConstraint2ValueStoreMap.get(toHash);
                        if (keyRefValueStore == null) {
                            keyRefValueStore = new KeyRefValueStore(keyRef, null, eDecl.getName());
                            fIdentityConstraint2ValueStoreMap.put(toHash, keyRefValueStore);
                        } else {
                            keyRefValueStore.clear();
                            keyRefValueStore.setElementName(eDecl.getName());
                        }
                        fValueStores.add(keyRefValueStore);
                        activateSelectorFor(icArray[i]);
                        break;
                }
            }
        } // initValueStoresFor(XSElementDecl)

        /** Returns the value store associated to the specified IdentityConstraint. */
        public ValueStoreBase getValueStoreFor(IdentityConstraint id, int initialDepth) {
            fLocalId.fDepth = initialDepth;
            fLocalId.fId = id;
            return (ValueStoreBase) fIdentityConstraint2ValueStoreMap.get(fLocalId);
        } // getValueStoreFor(IdentityConstraint, int):ValueStoreBase

        /** Returns the global value store associated to the specified IdentityConstraint. */
        public ValueStoreBase getGlobalValueStoreFor(IdentityConstraint id) {
            return (ValueStoreBase) fGlobalIDConstraintMap.get(id);
        } // getValueStoreFor(IdentityConstraint):ValueStoreBase

        // This method takes the contents of the (local) ValueStore
        // associated with id and moves them into the global
        // hashtable, if id is a <unique> or a <key>.
        // If it's a <keyRef>, then we leave it for later.
        public void transplant(IdentityConstraint id, int initialDepth) {
            fLocalId.fDepth = initialDepth;
            fLocalId.fId = id;
            ValueStoreBase newVals =
                (ValueStoreBase) fIdentityConstraint2ValueStoreMap.get(fLocalId);
            if (id.getCategory() == IdentityConstraint.IC_KEYREF)
                return;
            ValueStoreBase currVals = (ValueStoreBase) fGlobalIDConstraintMap.get(id);
            if (currVals != null) {
                currVals.append(newVals);
                fGlobalIDConstraintMap.put(id, currVals);
            } else
                fGlobalIDConstraintMap.put(id, newVals);

        } // transplant(id)

        /** Check identity constraints. */
        public void endDocument() {

            int count = fValueStores.size();
            for (int i = 0; i < count; i++) {
                ValueStoreBase valueStore = (ValueStoreBase) fValueStores.get(i);
                valueStore.endDocument();
            }

        } // endDocument()

        //
        // Object methods
        //

        /** Returns a string representation of this object. */
        public String toString() {
            String s = super.toString();
            int index1 = s.lastIndexOf('$');
            if (index1 != -1) {
                return s.substring(index1 + 1);
            }
            int index2 = s.lastIndexOf('.');
            if (index2 != -1) {
                return s.substring(index2 + 1);
            }
            return s;
        } // toString():String

    } // class ValueStoreCache
    
    /**
     * A simple vector for <code>short</code>s.
     */
    protected static final class ShortVector {
        
        //
        // Data
        //

        /** Current length. */
        private int fLength;

        /** Data. */
        private short[] fData;
        
        //
        // Constructors
        //
        
        public ShortVector() {}
        
        public ShortVector(int initialCapacity) {
            fData = new short[initialCapacity];
        }

        //
        // Public methods
        //

        /** Returns the length of the vector. */
        public int length() {
            return fLength;
        }

        /** Adds the value to the vector. */
        public void add(short value) {
            ensureCapacity(fLength + 1);
            fData[fLength++] = value;
        }

        /** Returns the short value at the specified position in the vector. */
        public short valueAt(int position) {
            return fData[position];
        }

        /** Clears the vector. */
        public void clear() {
            fLength = 0;
        }
        
        /** Returns whether the short is contained in the vector. */
        public boolean contains(short value) {
            for (int i = 0; i < fLength; ++i) {
                if (fData[i] == value) {
                    return true;
                }
            }
            return false;
        }

        //
        // Private methods
        //

        /** Ensures capacity. */
        private void ensureCapacity(int size) {
            if (fData == null) {
                fData = new short[8];
            }
            else if (fData.length <= size) {
                short[] newdata = new short[fData.length * 2];
                System.arraycopy(fData, 0, newdata, 0, fData.length);
                fData = newdata;
            }
        }
        
    } // class ShortVector
    
    XSDAssertionValidator getAssertionValidator() {
        return fAssertionValidator;
    }

    void setIsAssertProcessingNeededForSTUnionElem(boolean isAssertProcessingNeededForSTUnionElem) {
        this.fIsAssertProcessingNeededForSTUnionElem = isAssertProcessingNeededForSTUnionElem;
    }

    List getIsAssertProcessingNeededForSTUnionAttrs() {
        return fIsAssertProcessingNeededForSTUnionAttrs;
    }
    
    Vector getInheritableAttrList() {
        return fInheritableAttrList;
    }

} // XMLSchemaValidatorBase