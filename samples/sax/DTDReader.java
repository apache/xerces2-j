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


package sax;

import org.xml.sax.ext.DeclHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;




/**
 *      Provides DTD Grammar information
 *      This is a very simple program that shows DTD access
 *      through the usage of the DeclHandler interface.
 * 
 * @author Jeffrey Rodriguez
 * @version $Id$
 * @see       org.xml.sax.ext.DeclHandler
 */
public class DTDReader implements DeclHandler {

    /**
     * AttributeDecl
     * 
     * @param eName
     * @param aName
     * @param type
     * @param valueDefault
     * @param value
     * @exception SAXException
     */
    public void attributeDecl( String eName, String aName,String 
                               type, String valueDefault, String value ) throws SAXException {
        System.out.println( "\nAttributeDecl:" );
        System.out.println( "  eName = "  + eName );
        System.out.println( "  aName = "  + aName );
        System.out.println( "  type  = "  + type );
        System.out.println( "  valueDefault = " + valueDefault );
        System.out.println( "  value     = " + value );

    }

    /**
     * ElementDecl
     * 
     * @param name
     * @param model
     * @exception SAXException
     */
    public void elementDecl( String name, String model ) throws SAXException {
        System.out.println( "\nElementDecl = " + name );
        System.out.println( "  ContentModel =" + model +"\n" );
    }

    /**
     * Internal Entity Decl
     * 
     * @param name
     * @param value
     * @exception SAXException
     */
    public void internalEntityDecl (String name, String value)
    throws SAXException {
        System.out.println( "\nInternalEntityDecl" );
        System.out.println( "  Name = " + name+ ",value = " + value );

    }

    /**
     * External Entity Decl
     * 
     * @param name
     * @param publicId
     * @param systemId
     * @exception SAXException
     */
    public  void externalEntityDecl (String name, String publicId,
                                     String systemId)
    throws SAXException {
        System.out.println( "\nExternalEntityDecl" );
        System.out.println( "  Name = " + name + "publicId = " + publicId + "systemId = " + systemId );
    }




    /**
     * driver
     * 
     * @param argv
     */
    public static void main( String[] argv ) {

        if ( argv.length != 1 ) {

            System.out.println( "Error: Usage: java -cp ... DTDReader myxmlfile" ); 
            System.exit(0);
        }

        DTDReader   handler = new DTDReader();

        try {
            SAXParser  parser = new SAXParser( );
            parser.setProperty( "http://xml.org/sax/properties/declaration-handler", handler );
            System.out.println( "argv = " + argv[0] );
            parser.parse( argv[0] );

        } catch ( Exception ex ){
            ex.printStackTrace();
        }

    }

}
