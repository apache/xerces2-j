/*
 * Copyright 1999,2000,2004 The Apache Software Foundation.
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
package org.apache.html.dom;


import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLTitleElement;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTitleElement
 * @see org.apache.xerces.dom.ElementImpl
 */
public class HTMLTitleElementImpl
    extends HTMLElementImpl
    implements HTMLTitleElement
{
 
    
    public String getText()
    {
        Node    child;
        String    text;
        
        // Find the Text nodes contained within this element and return their
        // concatenated value. Required to go around comments, entities, etc.
        child = getFirstChild();
        text = "";
        while ( child != null )
        {
            if ( child instanceof Text )
                text = text + ( (Text) child ).getData();
            child = child.getNextSibling();
        }
        return text;
    }
    
    
    public void setText( String text )
    {
        Node    child;
        Node    next;
        
        // Delete all the nodes and replace them with a single Text node.
        // This is the only approach that can handle comments and other nodes.
        child = getFirstChild();
        while ( child != null )
        {
            next = child.getNextSibling();
            removeChild( child );
            child = next;
        }
        insertBefore( getOwnerDocument().createTextNode( text ), getFirstChild() );
    }

        
      /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLTitleElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

