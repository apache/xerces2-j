/*****************************************************************************
 * Copyright (C) 2000 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.xerces.jaxp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.SAXException;

/**
 * The <code>DocumentBuilderFactory</code> implementation for the Apache
 * Xerces XML parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version $Revision$ $Date$
 */
public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory{

    /**
     * Create a new <code>DocumentBuilderFactoryImpl</code> instance.
     */
    public DocumentBuilderFactoryImpl() {
        super();
    }

    /**
     * Returns a new configured instance of type <code>DocumentBuilder</code>.
     */
    public DocumentBuilder newDocumentBuilder()
    throws ParserConfigurationException {
        return(new DocumentBuilderImpl(this.isNamespaceAware(),
                                       this.isValidating()));
    }
}
