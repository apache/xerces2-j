/*
 * Copyright 2004 The Apache Software Foundation.
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

package org.apache.xerces.impl.xs;

import org.apache.xerces.dom3.DOMConfiguration;
import org.apache.xerces.dom3.DOMStringList;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.util.XSGrammarPool;
import org.apache.xerces.xni.grammars.XSGrammar;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.LSInputList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.DOMException;
import org.w3c.dom.ls.LSInput;

/**
 * <p>An implementation of XSLoader which wraps XMLSchemaLoader.</p>
 * 
 * @author Michael Glavassevich, IBM
 */
public final class XSLoaderImpl implements XSLoader, DOMConfiguration {
    
    /**
     * Grammar pool. Need this to prevent us from
     * getting two grammars from the same namespace.
     */
    private final XSGrammarPool fGrammarPool = new XSGrammarPool();
    
    /** Schema loader. **/
    private final XMLSchemaLoader fSchemaLoader = new XMLSchemaLoader();
    
    /**
     * No-args constructor.
     */
    public XSLoaderImpl() {
        fSchemaLoader.setProperty(XMLSchemaLoader.XMLGRAMMAR_POOL, fGrammarPool);
    }

    /**
     *  The configuration of a document. It maintains a table of recognized 
     * parameters. Using the configuration, it is possible to change the 
     * behavior of the load methods. The configuration may support the 
     * setting of and the retrieval of the following non-boolean parameters 
     * defined on the <code>DOMConfiguration</code> interface: 
     * <code>error-handler</code> (<code>DOMErrorHandler</code>) and 
     * <code>resource-resolver</code> (<code>LSResourceResolver</code>). 
     * <br> The following list of boolean parameters is defined: 
     * <dl>
     * <dt>
     * <code>"validate"</code></dt>
     * <dd>
     * <dl>
     * <dt><code>true</code></dt>
     * <dd>[required] (default) Validate an XML 
     * Schema during loading. If validation errors are found, the error 
     * handler is notified. </dd>
     * <dt><code>false</code></dt>
     * <dd>[optional] Do not 
     * report errors during the loading of an XML Schema document. </dd>
     * </dl></dd>
     * </dl>
     */
    public DOMConfiguration getConfig() {
        return this;
    }

    /**
     * Parses the content of XML Schema documents specified as the list of URI 
     * references. If the URI contains a fragment identifier, the behavior 
     * is not defined by this specification. 
     * @param uri The list of URI locations.
     * @return An XSModel representing the schema documents.
     */
    public XSModel loadURIList(StringList uriList) {
        int length = uriList.getLength();
        if (length == 0) {
            return null;
        }
        try {
            fGrammarPool.clear();
            for (int i = 0; i < length; ++i) {
                fSchemaLoader.loadGrammar(new XMLInputSource(null, uriList.item(i), null));
            }
            return fGrammarPool.toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    /**
     *  Parses the content of XML Schema documents specified as a list of 
     * <code>LSInput</code>s. 
     * @param is  The list of <code>LSInput</code>s from which the XML 
     *   Schema documents are to be read. 
     * @return An XSModel representing the schema documents.
     */
    public XSModel loadInputList(LSInputList is) {
        final int length = is.getLength();
        if (length == 0) {
            return null;
        }
        try {
            fGrammarPool.clear();
            for (int i = 0; i < length; ++i) {
                fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is.item(i)));
            }
            return fGrammarPool.toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    /**
     * Parse an XML Schema document from a location identified by a URI 
     * reference. If the URI contains a fragment identifier, the behavior is 
     * not defined by this specification. 
     * @param uri The location of the XML Schema document to be read.
     * @return An XSModel representing this schema.
     */
    public XSModel loadURI(String uri) {
        try {
            fGrammarPool.clear();
            return ((XSGrammar) fSchemaLoader.loadGrammar(new XMLInputSource(null, uri, null))).toXSModel();
        }
        catch (Exception e){
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    /**
     *  Parse an XML Schema document from a resource identified by a 
     * <code>LSInput</code> . 
     * @param is  The <code>DOMInputSource</code> from which the source 
     *   document is to be read. 
     * @return An XSModel representing this schema.
     */
    public XSModel load(LSInput is) {
        try {
            fGrammarPool.clear();
            return ((XSGrammar) fSchemaLoader.loadGrammar(fSchemaLoader.dom2xmlInputSource(is))).toXSModel();
        } 
        catch (Exception e) {
            fSchemaLoader.reportDOMFatalError(e);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.dom3.DOMConfiguration#setParameter(java.lang.String, java.lang.Object)
     */
    public void setParameter(String name, Object value) throws DOMException {
        fSchemaLoader.setParameter(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.dom3.DOMConfiguration#getParameter(java.lang.String)
     */
    public Object getParameter(String name) throws DOMException {
        return fSchemaLoader.getParameter(name);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.dom3.DOMConfiguration#canSetParameter(java.lang.String, java.lang.Object)
     */
    public boolean canSetParameter(String name, Object value) {
        return fSchemaLoader.canSetParameter(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.dom3.DOMConfiguration#getParameterNames()
     */
    public DOMStringList getParameterNames() {
        return fSchemaLoader.getParameterNames();
    }
}
