package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLHRElement
 * @see ElementImpl
 */
public final class HTMLHRElementImpl
    extends HTMLElementImpl
    implements HTMLHRElement
{

    
    public String getAlign()
    {
        return capitalize( getAttribute( "align" ) );
    }
    
    
    public void setAlign( String align )
    {
        setAttribute( "align", align );
    }
  
    
    public boolean getNoShade()
    {
        return getBinary( "noshade" );
    }
    
    
    public void setNoShade( boolean noShade )
    {
        setAttribute( "noshade", noShade );
    }

    
    public String getSize()
    {
        return getAttribute( "size" );
    }
    
    
    public void setSize( String size )
    {
        setAttribute( "size", size );
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
    public HTMLHRElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

