package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLHeadElement
 * @see ElementImpl
 */
public final class HTMLHeadElementImpl
    extends HTMLElementImpl
    implements HTMLHeadElement
{

    
    public String getProfile()
    {
        return getAttribute( "profile" );
    }
    
    
    public void setProfile( String profile )
    {
        setAttribute( "profile", profile );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLHeadElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

