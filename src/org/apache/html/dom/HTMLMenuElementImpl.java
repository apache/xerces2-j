package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMenuElement
 * @see ElementImpl
 */
public final class HTMLMenuElementImpl
    extends HTMLElementImpl
    implements HTMLMenuElement
{

    
    public boolean getCompact()
    {
        return getBinary( "compact" );
    }
    
    
    public void setCompact( boolean compact )
    {
        setAttribute( "compact", compact );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLMenuElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

