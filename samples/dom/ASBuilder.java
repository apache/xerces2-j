/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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

package dom;

import org.apache.xerces.dom3.DOMError;
import org.apache.xerces.dom3.DOMErrorHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.xerces.dom.ASDOMImplementationImpl;
import org.apache.xerces.dom3.as.DOMImplementationAS;
import org.apache.xerces.dom3.as.ASModel;
import org.apache.xerces.dom3.as.DOMASBuilder;

import org.w3c.dom.DOMImplementation;
import java.util.Vector;

import org.apache.xerces.dom.ASModelImpl;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.impl.xs.psvi.*;
import org.apache.xerces.impl.xs.SchemaGrammar;
import java.util.Enumeration;

/**
 * This sample program illustrates how to use DOM3 DOMASBuilder interface to
 * preparse ASModels and associate ASModels with an instance document to be
 * validated.
 * <p>
 * Xerces only support preparsing XML Schema grammars, so all ASModel
 * appears in this sample program are assumed to be XML Schema grammars. When
 * Xerces provide more complete DOM AS support, the sample should be extended
 * for other types of grammars.
 * <p>
 * <p>
 * Since XML Schema document might import other schemas: it is better to set each 
 * parsed schema (ASModel)  on the parser before parsing another schema document. 
 * The schema on the parser will be used in the case it is referenced from a 
 * schema document the parser is parsing.
 * If a schema document imports other schemas, the parser returns a container ASModel that
 * includes the list of all schemas that are referenced plus the schema that was set
 * on the parser.
 * NOTE: this behavior might be changed
 * 
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class ASBuilder implements DOMErrorHandler {

    //
    // Constants
    //

    // feature ids

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /** Validation feature id (http://xml.org/sax/features/validation). */
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";

    /** Schema validation feature id (http://apache.org/xml/features/validation/schema). */
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id (http://apache.org/xml/features/validation/schema-full-checking). */
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";

    // default settings

    /** Default Schema full checking support (false). */
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;

    //
    // MAIN
    //

    /** Main program entry point. */
    public static void main(String argv[]) {

        // too fee parameters
        if (argv.length < 2) {
            printUsage();
            System.exit(1);
        }
        // get DOM implementation
        DOMImplementationAS domImpl = (DOMImplementationAS)ASDOMImplementationImpl.getDOMImplementation();
        // create a new parser, and set the error handler
        DOMASBuilder parser = domImpl.createDOMASBuilder();
        parser.setErrorHandler(new ASBuilder());

        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;

        String arg = null;
        int i = 0;

        // process -f/F
        arg = argv[i];
        if (arg.equals("-f")) {
            schemaFullChecking = true;
            arg = argv[++i];
        } else if (arg.equals("-F")) {
            schemaFullChecking = false;
            arg = argv[++i];
        }

        // set the features. since we only deal with schema, some features have
        // to be true
        parser.setFeature(NAMESPACES_FEATURE_ID, true);
        parser.setFeature(VALIDATION_FEATURE_ID, true);
        parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);

        // process -a: as model files
        if (!arg.equals("-a")) {
            printUsage();
            System.exit(1);
        }

        i++;
        Vector asfiles = new Vector();
        while (i < argv.length && !(arg = argv[i]).startsWith("-")) {
            asfiles.addElement(arg);
            i++;
        }

        // has to be at least one as file, and there has to be other parameters
        if (asfiles.size() == 0) {
            printUsage();
            System.exit(1);
        }

        // process -i: instance files, if any
        Vector ifiles = null;
        if (i < argv.length) {
            if (!arg.equals("-i")) {
                printUsage();
                System.exit(1);
            }

            i++;
            ifiles = new Vector();
            while (i < argv.length && !(arg = argv[i]).startsWith("-")) {
                ifiles.addElement(arg);
                i++;
            }

            // has to be at least one instance file, and there has to be no more
            // parameters
            if (ifiles.size() == 0 || i != argv.length) {
                printUsage();
                System.exit(1);
            }
        }

        //
        // PARSING XML SCHEMAS
        //

        ASModel asmodel = null;
        try {
            for (i = 0; i < asfiles.size(); i++) {
                asmodel = parser.parseASURI((String)asfiles.elementAt(i));
                parser.setAbstractSchema(asmodel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // then for each instance file, try to validate it
        if (ifiles != null) {
            try {
                for (i = 0; i < ifiles.size(); i++) {
                    parser.parseURI((String)ifiles.elementAt(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        testXSModel(asmodel);
        
    } // main(String[])

    //
    // Private static methods
    //

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java dom.ASBuilder [-f|-F] -a uri ... [-i uri ...]");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -f  | -F    Turn on/off Schema full checking.");
        System.err.println("  -a uri ...  Provide a list of schema documents.");
        System.err.println("  -i uri ...  Provide a list of instalce documents to validate.");
        System.err.println();

        System.err.println("default:");
        System.err.print("  Schema full checking:     ");
        System.err.println(DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off");
        System.err.println();
        System.err.println("notes:");
        System.err.println("DOM Level 3 APIs might change in the future.");


    } // printUsage()

    public boolean handleError(DOMError error) {

        System.err.print("[");
        switch (error.getSeverity()) {
            case DOMError.SEVERITY_WARNING:
                System.err.print("Warning");
                break;
            case DOMError.SEVERITY_ERROR:
                System.err.print("Error");
                break;
            case DOMError.SEVERITY_FATAL_ERROR:
                System.err.print("Fatal Error");
                break;
        }
        System.err.print("] ");

        String systemId = error.getLocation().getUri();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            System.err.print(systemId);
        }
        System.err.print(':');
        System.err.print(error.getLocation().getLineNumber());
        System.err.print(':');
        System.err.print(error.getLocation().getColumnNumber());
        System.err.print(": ");
        System.err.print(error.getMessage());
        System.err.println();
        System.err.flush();

        return error.getSeverity() != DOMError.SEVERITY_FATAL_ERROR;
    }

    static private XSModel as2xsModel(ASModel asmodel) {
        ASModelImpl model = (ASModelImpl)asmodel;
        Vector models = model.getInternalASModels();
        SchemaGrammar[] grammars = new SchemaGrammar[models.size()];
        for (int i = 0; i < models.size(); i++)
            grammars[i] = ((ASModelImpl)models.elementAt(i)).getGrammar();
        return new XSModelImpl(grammars);
    }

    static void testXSModel(ASModel asmodel) {

        XSModel model = as2xsModel(asmodel);
        
        System.out.println("Namespaces:");
        Enumeration namespaces = model.getNamespaces();
        while (namespaces.hasMoreElements())
            System.out.println(namespaces.nextElement());
            
        XSNamedMap components;

        System.out.println();
        System.out.println("Type Definitions:");
        components = model.getComponents(XSConstants.TYPE_DEFINITION);
        int typecount = components.getMapLength();
        XSNamedMap ccomponents = model.getComponents(XSTypeDefinition.COMPLEX_TYPE);
        int complexcount = ccomponents.getMapLength();
        XSNamedMap scomponents = model.getComponents(XSTypeDefinition.SIMPLE_TYPE);
        int simplecount = scomponents.getMapLength();
        System.out.println("total = " + typecount + "; complex = " + complexcount + "; simple = " + simplecount);
        
        System.out.println();
        System.out.println("Complex Type Definitions:");
        XSComplexTypeDefinition complexType = (XSComplexTypeDefinition)ccomponents.getItem(0);
        System.out.println("namespace: " + complexType.getNamespace());
        System.out.println("name: " + complexType.getName());
        System.out.println("base: " + complexType.getBaseType().getName());
        System.out.println("final: " + complexType.getFinal());
        System.out.println("anonymous: " + complexType.getIsAnonymous());
        System.out.println("derivation: " + complexType.getDerivationMethod());
        System.out.println("abstract: " + complexType.getIsAbstract());
        System.out.println("content: " + complexType.getContentType());
        XSParticle particle = complexType.getParticle();
        if (particle != null)
            System.out.println("particle: " + particle.getType());

        System.out.println();
        System.out.println("Simple Type Definitions:");
        XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition)scomponents.getItem(0);
        System.out.println("namespace: " + simpleType.getNamespace());
        System.out.println("name: " + simpleType.getName());
        System.out.println("base: " + simpleType.getBaseType().getName());
        System.out.println("final: " + simpleType.getFinal());
        System.out.println("anonymous: " + simpleType.getIsAnonymous());
        System.out.println("defined facets: " + simpleType.getDefinedFacets());
        System.out.println("fixed facets: " + simpleType.getFixedFacets());
        System.out.println("variety: " + simpleType.getVariety());
        
        components = model.getComponentsByNamespace(XSTypeDefinition.SIMPLE_TYPE, simpleType.getNamespace());
        simpleType = (XSSimpleTypeDefinition)components.getItem(0);
        System.out.println("namespace: " + simpleType.getNamespace());
        System.out.println("name: " + simpleType.getName());

        simpleType = (XSSimpleTypeDefinition)model.getTypeDefinition(simpleType.getName(), simpleType.getNamespace());
        System.out.println("namespace: " + simpleType.getNamespace());
        System.out.println("name: " + simpleType.getName());
        
        System.out.println();
        System.out.println("Elements:");
        components = model.getComponents(XSConstants.ELEMENT_DECLARATION);
        XSElementDeclaration element = (XSElementDeclaration)components.getItem(0);
        System.out.println("namespace: " + element.getNamespace());
        System.out.println("name: " + element.getName());
        System.out.println("type: " + element.getTypeDefinition().getName());
        System.out.println("scope: " + element.getScope());
        System.out.println("constraint: " + element.getConstraintType() + ", " + element.getConstraintValue());
        System.out.println("nillable: " + element.getIsNillable());
        System.out.println("abstract: " + element.getIsAbstract());
        
        components = model.getComponentsByNamespace(XSConstants.ELEMENT_DECLARATION, element.getNamespace());
        element = (XSElementDeclaration)components.getItem(0);
        System.out.println("namespace: " + element.getNamespace());
        System.out.println("name: " + element.getName());

        element = model.getElementDecl(element.getName(), element.getNamespace());
        System.out.println("namespace: " + element.getNamespace());
        System.out.println("name: " + element.getName());
        
        System.out.println();
        System.out.println("Attribute Group:");
        components = model.getComponents(XSConstants.ATTRIBUTE_GROUP);
        XSAttributeGroupDefinition attrgrp = (XSAttributeGroupDefinition)components.getItem(0);
        System.out.println("namespace: " + attrgrp.getNamespace());
        System.out.println("name: " + attrgrp.getName());
        if (attrgrp.getAttributeWildcard() != null)
        System.out.println("wildcard: " + attrgrp.getAttributeWildcard().getConstraintType() + ", " + attrgrp.getAttributeWildcard().getProcessContents());
        System.out.println("uses: " + attrgrp.getAttributeUses().getListLength());
        System.out.println("uses: " + ((XSAttributeUse)attrgrp.getAttributeUses().getItem(0)).getAttrDeclaration().getName());
        
        components = model.getComponentsByNamespace(XSConstants.ATTRIBUTE_GROUP, attrgrp.getNamespace());
        attrgrp = (XSAttributeGroupDefinition)components.getItem(0);
        System.out.println("namespace: " + attrgrp.getNamespace());
        System.out.println("name: " + attrgrp.getName());

        attrgrp = model.getAttributeGroup(attrgrp.getName(), attrgrp.getNamespace());
        System.out.println("namespace: " + attrgrp.getNamespace());
        System.out.println("name: " + attrgrp.getName());
        
        System.out.println();
        System.out.println("Group:");
        components = model.getComponents(XSConstants.MODEL_GROUP_DEFINITION);
        XSModelGroupDefinition group = (XSModelGroupDefinition)components.getItem(0);
        System.out.println("namespace: " + group.getNamespace());
        System.out.println("name: " + group.getName());
        XSModelGroup modelgroup = group.getModelGroup();
        System.out.println("model group: " + modelgroup.getCompositor());
        System.out.println("model group: " + modelgroup.getParticles().getListLength());
        particle = (XSParticle)modelgroup.getParticles().getItem(0);
        System.out.println("particle: " + particle.getTerm().getType());
        System.out.println("particle: " + particle.getMinOccurs());
        System.out.println("particle: " + particle.getMaxOccurs());
        
        components = model.getComponentsByNamespace(XSConstants.MODEL_GROUP_DEFINITION, group.getNamespace());
        group = (XSModelGroupDefinition)components.getItem(0);
        System.out.println("namespace: " + group.getNamespace());
        System.out.println("name: " + group.getName());

        group = model.getModelGroupDefinition(group.getName(), group.getNamespace());
        System.out.println("namespace: " + group.getNamespace());
        System.out.println("name: " + group.getName());
        
        System.out.println();
        System.out.println("notation:");
        components = model.getComponents(XSConstants.NOTATION_DECLARATION);
        XSNotationDeclaration notation = (XSNotationDeclaration)components.getItem(0);
        if (notation == null) return;
        System.out.println("namespace: " + notation.getNamespace());
        System.out.println("name: " + notation.getName());
        System.out.println("system id: " + notation.getSystemId());
        System.out.println("public id: " + notation.getPublicId());
        
        components = model.getComponentsByNamespace(XSConstants.NOTATION_DECLARATION, notation.getNamespace());
        notation = (XSNotationDeclaration)components.getItem(0);
        System.out.println("namespace: " + notation.getNamespace());
        System.out.println("name: " + notation.getName());

        notation = model.getNotationDecl(notation.getName(), notation.getNamespace());
        System.out.println("namespace: " + notation.getNamespace());
        System.out.println("name: " + notation.getName());
        
    }
} // class DOMCount
