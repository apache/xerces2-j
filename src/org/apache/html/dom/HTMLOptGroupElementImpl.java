package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLOptGroupElement
 * @see ElementImpl
 */
public final class HTMLOptGroupElementImpl
    extends HTMLElementImpl
    implements HTMLOptGroupElement
{

        
    public boolean getDisabled()
    {
        return getBinary( "disabled" );
    }
    
    
    public void setDisabled( boolean disabled )
    {
        setAttribute( "disabled", disabled );
    }

    
      public String getLabel()
    {
        return capitalize( getAttribute( "label" ) );
    }
    
    
    public void setLabel( String label )
    {
        setAttribute( "label", label );
    }

    
      /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLOptGroupElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

