/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

package dom.serialize;
import java.io.ObjectOutputStream;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DeferredDocumentImpl;
import org.w3c.dom.NodeList;
import org.apache.xerces.dom.NodeImpl;
import org.w3c.dom.Document;
import java.io.FileOutputStream;
import org.w3c.dom.Node;
import dom.DOMWriter;


/**
 * This testcase tests the Java Serialization
 * of the DOM.
 * I wrote this to test this capability for
 * regresion
 * 
 * @author Jeffrey Rodriguez
 * @version $id$
 * @see                      TestSerializeDOMOut
 */


public class TestSerializeDOMOut
{          

    public TestSerializeDOMOut(){
    }

      /**
     * Deserializes Java DOM Object 
     * 
     * @param nameSerializeFile
     * @return 
     */
    public void serializeDOM( Document doc, String nameSerializedFile ){
        try {
            ObjectOutputStream out               =
                              new ObjectOutputStream( new FileOutputStream( nameSerializedFile ) );
            out.writeObject(doc);
            out.close();

        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    public static void main (String[] argv) 
    { 

        if ( argv.length != 1 ) {
            System.out.println("Error - Usage: java TestOut yourFile.xml" );
            System.exit(1);
        }

        String    xmlFilename = argv[0];


        try {
            DOMParser parser     = new DOMParser();

            parser.parse( xmlFilename ); 

            DocumentImpl doc     = (DocumentImpl) parser.getDocument();

            int indexOfextension = xmlFilename.indexOf("." );



            String nameOfSerializedFile = null;

            if ( indexOfextension == -1 ) {
                nameOfSerializedFile = xmlFilename +".ser" ;
            } else {
                nameOfSerializedFile = 
                xmlFilename.substring(0,indexOfextension) + ".ser";
            }

            System.out.println( "Writing Serialize DOM  to file = " + nameOfSerializedFile ); 


            FileOutputStream fileOut =  new FileOutputStream( nameOfSerializedFile );


            TestSerializeDOMOut  tstOut = new TestSerializeDOMOut();

            tstOut.serializeDOM( doc, nameOfSerializedFile );


            System.out.println( "Reading Serialize DOM from " + nameOfSerializedFile );


            TestSerializeDOMIn    tstIn  = new TestSerializeDOMIn();
            doc           = tstIn.deserializeDOM( nameOfSerializedFile );

            DOMWriter prettyWriter = new DOMWriter( false );
            System.out.println( "Here is the whole Document" );
            prettyWriter.print(  doc.getDocumentElement() );
        } catch ( Exception ex ){
            ex.printStackTrace();
        }
    } 
}

