package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMapElement
 * @see ElementImpl
 */
public final class HTMLMapElementImpl
    extends HTMLElementImpl
    implements HTMLMapElement
{

    
    
    public HTMLCollection getAreas()
    {
        if ( _areas == null )
            _areas = new HTMLCollectionImpl( this, HTMLCollectionImpl.AREA );
        return _areas;
    }
    
  
      public String getName()
    {
        return getAttribute( "name" );
    }
    
    
    public void setName( String name )
    {
        setAttribute( "name", name );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLMapElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
    
    
    private HTMLCollection    _areas;


}

