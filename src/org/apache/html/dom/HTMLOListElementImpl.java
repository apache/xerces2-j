package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLOListElement
 * @see ElementImpl
 */
public final class HTMLOListElementImpl
    extends HTMLElementImpl
    implements HTMLOListElement
{

    
    public boolean getCompact()
    {
        return getBinary( "compact" );
    }
    
    
    public void setCompact( boolean compact )
    {
        setAttribute( "compact", compact );
    }
    
    
      public int getStart()
    {
        return getInteger( getAttribute( "start" ) );
    }
    
    
    public void setStart( int start )
    {
        setAttribute( "start", String.valueOf( start ) );
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
    public HTMLOListElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

