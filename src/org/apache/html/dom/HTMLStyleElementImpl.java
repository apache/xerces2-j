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


import org.w3c.dom.html.HTMLStyleElement;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLStyleElement
 * @see org.apache.xerces.dom.ElementImpl
 */
public class HTMLStyleElementImpl
    extends HTMLElementImpl
    implements HTMLStyleElement
{
    
    
    public boolean getDisabled()
    {
        return getBinary( "disabled" );
    }
    
    
    public void setDisabled( boolean disabled )
    {
        setAttribute( "disabled", disabled );
    }

    
    public String getMedia()
    {
        return getAttribute( "media" );
    }
    
    
    public void setMedia( String media )
    {
        setAttribute( "media", media );
    }
  
  
    public String getType()
    {
        return getAttribute( "type" );
    }
    
    
    public void setType( String type )
    {
        setAttribute( "type", type );
    }
    
    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLStyleElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

