package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLFrameSetElement
 * @see ElementImpl
 */
public final class HTMLFrameSetElementImpl
    extends HTMLElementImpl
    implements HTMLFrameSetElement
{

    
    public String getCols()
    {
        return getAttribute( "cols" );
    }
    
    
    public void setCols( String cols )
    {
        setAttribute( "cols", cols );
    }

    
    public String getRows()
    {
        return getAttribute( "rows" );
    }
    
    
    public void setRows( String rows )
    {
        setAttribute( "rows", rows );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLFrameSetElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
  

}

