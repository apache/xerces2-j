/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.xerces.impl.xs.opti;


import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.w3c.dom.Document;

/**
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class SchemaDOMParser extends DefaultXMLDocumentHandler {

    //
    // Data
    //
    
    /** Property identifier: error reporter. */
    public static final String ERROR_REPORTER =
    Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    // the locator containing line/column information
    protected XMLLocator   fLocator;

    // namespace context, needed for producing
    // representations of annotations
    protected NamespaceContext fNamespaceContext = null;

    SchemaDOM schemaDOM;
   
    XMLParserConfiguration config;

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaDOMParser(XMLParserConfiguration config) {
        this.config = config;
    }

    // where an annotation element itself begins
    // -1 means not in an annotation's scope
    private int fAnnotationDepth = -1;
    // Where xs:appinfo or xs:documentation starts;
    // -1 means not in the scope of either of the two elements.
    private int fInnerAnnotationDepth = -1;
    // The current element depth
    private int fDepth = -1;
    // Use to report the error when characters are not allowed.
    XMLErrorReporter fErrorReporter;


    //
    // XMLDocumentHandler methods
    //

    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext namespaceContext, Augmentations augs)
        throws XNIException {
		fErrorReporter = (XMLErrorReporter)config.getProperty(ERROR_REPORTER);
		schemaDOM = new SchemaDOM(); 
		fAnnotationDepth = -1;
        fInnerAnnotationDepth = -1;
        fDepth = -1;
        fLocator = locator;
        fNamespaceContext = namespaceContext;
    } // startDocument(XMLLocator,String,NamespaceContext, Augmentations)

    /**
     * The end of the document.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocument(Augmentations augs) throws XNIException {
	    // To debug the DOM created uncomment the line below
	    // schemaDOM.printDOM();
    } // endDocument()


    /**
     * A comment.
     * 
     * @param text   The text in the comment.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by application to signal an error.
     */
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        if(fAnnotationDepth > -1) {
            schemaDOM.comment(text);
        }
    }

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
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException {
        if(fAnnotationDepth > -1) {
            schemaDOM.processingInstruction(target, data.toString());
        }
    }

    /**
     * Character content.
     * 
     * @param text   The content.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        // when it's not within xs:appinfo or xs:documentation
        if (fInnerAnnotationDepth == -1 ) {
            for (int i=text.offset; i<text.offset+text.length; i++) {
                // and there is a non-whitespace character
                if (!XMLChar.isSpace(text.ch[i])) {
                    // the string we saw: starting from the first non-whitespace character.
                    String txt = new String(text.ch, i, text.length+text.offset-i);
                    // report an error
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "s4s-elt-character",
                                               new Object[]{txt},
                                               XMLErrorReporter.SEVERITY_ERROR);
                    break;
                }
            }
            // don't call super.characters() when it's not within one of the 2
            // annotation elements: the traversers ignore them anyway. We can
            // save time/memory creating the text nodes.
        }
        // when it's within either of the 2 elements, characters are allowed
        // and we need to store them.
        else {
            schemaDOM.characters(text);
        }

    }


    /**
     * The start of an element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {

        fDepth++;
        // while it is true that non-whitespace character data
        // may only occur in appInfo or documentation
        // elements, it's certainly legal for comments and PI's to
        // occur as children of annotation; we need
        // to account for these here.
        if (fAnnotationDepth == -1) {
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                fAnnotationDepth = fDepth;
                schemaDOM.startAnnotation(element, attributes, fNamespaceContext);
            } 
        } else if(fDepth == fAnnotationDepth+1) {
            fInnerAnnotationDepth = fDepth;
            schemaDOM.startAnnotationElement(element, attributes);
        } else {
            schemaDOM.startAnnotationElement(element, attributes);
            // avoid falling through; don't call startElement in this case
            return;
        }
        schemaDOM.startElement(element, attributes, 
                               fLocator.getLineNumber(),
                               fLocator.getColumnNumber());

    }


    /**
     * An empty element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {
        // the order of events that occurs here is:
        //   schemaDOM.startAnnotation/startAnnotationElement (if applicable)
        //   schemaDOM.emptyElement  (basically the same as startElement then endElement)
        //   schemaDOM.endAnnotationElement (if applicable)
        // the order of events that would occur if this was <element></element>:
        //   schemaDOM.startAnnotation/startAnnotationElement (if applicable)
        //   schemaDOM.startElement
        //   schemaDOM.endAnnotationElement (if applicable)
        //   schemaDOM.endElementElement
        // Thus, we can see that the order of events isn't the same.  However, it doesn't
        // seem to matter.  -- PJM
        if (fAnnotationDepth == -1) {
            // this is messed up, but a case to consider:
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                schemaDOM.startAnnotation(element, attributes, fNamespaceContext);
            }
        } else {
            schemaDOM.startAnnotationElement(element, attributes);
        }
        
        schemaDOM.emptyElement(element, attributes, 
                               fLocator.getLineNumber(),
                               fLocator.getColumnNumber());
        
        if (fAnnotationDepth == -1) {
            // this is messed up, but a case to consider:
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                    element.localpart == SchemaSymbols.ELT_ANNOTATION) {
                schemaDOM.endAnnotationElement(element, true);
            }
        } else {
            schemaDOM.endAnnotationElement(element, false);
        } 
    }


    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     * @param augs    Additional information that may include infoset augmentations
     *                
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endElement(QName element, Augmentations augs) throws XNIException {

        // when we reach the endElement of xs:appinfo or xs:documentation,
        // change fInnerAnnotationDepth to -1
        if(fAnnotationDepth > -1) {
            if (fInnerAnnotationDepth == fDepth) {
                fInnerAnnotationDepth = -1;
                schemaDOM.endAnnotationElement(element, false);
	            schemaDOM.endElement();
            } else if (fAnnotationDepth == fDepth) {
                fAnnotationDepth = -1;
                schemaDOM.endAnnotationElement(element, true);
	            schemaDOM.endElement();
            } else { // inside a child of annotation
                schemaDOM.endAnnotationElement(element, false);
            }
        } else { // not in an annotation at all
	        schemaDOM.endElement();
        }
        fDepth--;

    }
    
    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     * 
     * @param text   The ignorable whitespace.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {
        // unlikely to be called, but you never know...
        if (fAnnotationDepth != -1 ) {
            schemaDOM.characters(text);
        }
    }

    /**
     * The start of a CDATA section.
     * 
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startCDATA(Augmentations augs) throws XNIException {
        schemaDOM.startAnnotationCDATA();
    }

    /**
     * The end of a CDATA section.
     * 
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endCDATA(Augmentations augs) throws XNIException {
        schemaDOM.endAnnotationCDATA();
    }

    
    //
    // other methods
    //
    
    /**
     * Returns the DOM document object.
     */
    public Document getDocument() {
        return schemaDOM;
    }

}
