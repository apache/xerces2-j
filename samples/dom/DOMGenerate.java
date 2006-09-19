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

package dom;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
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

