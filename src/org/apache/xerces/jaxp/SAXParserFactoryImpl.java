/*****************************************************************************
 * Copyright (C) 2000 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.xerces.jaxp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

/**
 * The <code>SAXParserFactory</code> implementation for the Apache Xerces
 * XML parser.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version $Revision$ $Date$
 */
public class SAXParserFactoryImpl extends SAXParserFactory {

    /**
     * Create a new <code>SAXParserFactoryImpl</code> instance.
     */
    public SAXParserFactoryImpl() {
        super();
    }

    /**
     * Returns a new configured instance of type <code>SAXParser</code>.
     */
    public SAXParser newSAXParser()
    throws ParserConfigurationException {
        return(new SAXParserImpl(this.isNamespaceAware(),this.isValidating()));
    }
}
