package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLQuoteElement
 * @see ElementImpl
 */
public final class HTMLQuoteElementImpl
    extends HTMLElementImpl
    implements HTMLQuoteElement
{
    
    
    public String getCite()
    {
        return getAttribute( "cite" );
    }
    
    
    public void setCite( String cite )
    {
        setAttribute( "cite", cite );
    }
    
    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLQuoteElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

