/*
 * Copyright 2000-2002,2004 The Apache Software Foundation.
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



package dom;
import  org.w3c.dom.*;
import  org.apache.xerces.dom.DocumentImpl;
import  org.apache.xerces.dom.DOMImplementationImpl;
import  org.w3c.dom.Document;
import  org.apache.xml.serialize.OutputFormat;
import  org.apache.xml.serialize.Serializer;
import  org.apache.xml.serialize.SerializerFactory;
import  org.apache.xml.serialize.XMLSerializer;
import  java.io.*;

/**
 * Simple Sample that:
 * - Generate a DOM from Scratch.
 * - Output DOM to a String using Serializer
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class DOMGenerate {
    public static void main( String[] argv ) {
        try {
            Document doc= new DocumentImpl();
            Element root = doc.createElement("person");     // Create Root Element
            Element item = doc.createElement("name");       // Create element
            item.appendChild( doc.createTextNode("Jeff") );
            root.appendChild( item );                       // atach element to Root element
            item = doc.createElement("age");                // Create another Element
            item.appendChild( doc.createTextNode("28" ) );       
            root.appendChild( item );                       // Attach Element to previous element down tree
            item = doc.createElement("height");            
            item.appendChild( doc.createTextNode("1.80" ) );
            root.appendChild( item );                       // Attach another Element - grandaugther
            doc.appendChild( root );                        // Add Root to Document


            OutputFormat    format  = new OutputFormat( doc );   //Serialize DOM
            StringWriter  stringOut = new StringWriter();        //Writer will be a String
            XMLSerializer    serial = new XMLSerializer( stringOut, format );
            serial.asDOMSerializer();                            // As a DOM Serializer

            serial.serialize( doc.getDocumentElement() );

            System.out.println( "STRXML = " + stringOut.toString() ); //Spit out DOM as a String
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
}

