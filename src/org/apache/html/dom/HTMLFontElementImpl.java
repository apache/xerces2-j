package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLFontElement
 * @see ElementImpl
 */
public final class HTMLFontElementImpl
    extends HTMLElementImpl
    implements HTMLFontElement
{

    
    public String getColor()
    {
        return capitalize( getAttribute( "color" ) );
    }
    
    
    public void setColor( String color )
    {
        setAttribute( "color", color );
    }
    
    
    public String getFace()
    {
        return capitalize( getAttribute( "face" ) );
    }
    
    
    public void setFace( String face )
    {
        setAttribute( "face", face );
    }
    
    
    public String getSize()
    {
        return getAttribute( "size" );
    }
    
    
    public void setSize( String size )
    {
        setAttribute( "size", size );
    }

    
    public HTMLFontElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
  

}
