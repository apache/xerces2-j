package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLMetaElement
 * @see ElementImpl
 */
public final class HTMLMetaElementImpl
    extends HTMLElementImpl
    implements HTMLMetaElement
{

    
    public String getContent()
    {
        return getAttribute( "content" );
    }
    
    
    public void setContent( String content )
    {
        setAttribute( "content", content );
    }

    
    
      public String getHttpEquiv()
    {
        return getAttribute( "http-equiv" );
    }
    
    
    public void setHttpEquiv( String httpEquiv )
    {
        setAttribute( "http-equiv", httpEquiv );
    }

  
      public String getName()
    {
        return getAttribute( "name" );
    }
    
    
    public void setName( String name )
    {
        setAttribute( "name", name );
    }

    
      public String getScheme()
    {
        return getAttribute( "scheme" );
    }
    
    
    public void setScheme( String scheme )
    {
        setAttribute( "scheme", scheme );
    }
    
    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLMetaElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

