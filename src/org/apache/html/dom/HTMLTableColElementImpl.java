package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTableColElement
 * @see ElementImpl
 */
public final class HTMLTableColElementImpl
    extends HTMLElementImpl
    implements HTMLTableColElement
{
    
    
    public String getAlign()
    {
        return capitalize( getAttribute( "align" ) );
    }
    
    
    public void setAlign( String align )
    {
        setAttribute( "align", align );
    }
  
    
    public String getCh()
    {
        String    ch;
        
        // Make sure that the access key is a single character.
        ch = getAttribute( "char" );
        if ( ch != null && ch.length() > 1 )
            ch = ch.substring( 0, 1 );
        return ch;
    }
    
    
    public void setCh( String ch )
    {
        // Make sure that the access key is a single character.
        if ( ch != null && ch.length() > 1 )
            ch = ch.substring( 0, 1 );
        setAttribute( "char", ch );
    }


    public String getChOff()
    {
        return getAttribute( "charoff" );
    }
    
    
    public void setChOff( String chOff )
    {
        setAttribute( "charoff", chOff );
    }
  
  
    public int getSpan()
    {
        return getInteger( getAttribute( "span" ) );
    }
    
    
    public void setSpan( int span )
    {
        setAttribute( "span", String.valueOf( span ) );
    }

    
    public String getVAlign()
    {
        return capitalize( getAttribute( "valign" ) );
    }
    
    
    public void setVAlign( String vAlign )
    {
        setAttribute( "valign", vAlign );
    }
    
    public String getWidth()
    {
        return getAttribute( "width" );
    }
    
    
    public void setWidth( String width )
    {
        setAttribute( "width", width );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLTableColElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

