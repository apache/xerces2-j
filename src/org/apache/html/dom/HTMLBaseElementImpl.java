package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLBaseElement
 * @see ElementImpl
 */
public final class HTMLBaseElementImpl
    extends HTMLElementImpl
    implements HTMLBaseElement
{

    
    public String getHref()
    {
        return getAttribute( "href" );
    }
    
    
    public void setHref( String href )
    {
        setAttribute( "href", href );
    }
    
    public String getTarget()
    {
        return getAttribute( "target" );
    }
    
    
    public void setTarget( String target )
    {
        setAttribute( "target", target );
    }


    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLBaseElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

