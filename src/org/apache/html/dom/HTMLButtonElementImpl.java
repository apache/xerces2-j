package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLButtonElement
 * @see ElementImpl
 */
public final class HTMLButtonElementImpl
    extends HTMLElementImpl
    implements HTMLButtonElement, HTMLFormControl
{

    
      public String getAccessKey()
    {
        String    accessKey;
        
        // Make sure that the access key is a single character.
        accessKey = getAttribute( "accesskey" );
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        return accessKey;
    }
    
    
    public void setAccessKey( String accessKey )
    {
        // Make sure that the access key is a single character.
        if ( accessKey != null && accessKey.length() > 1 )
            accessKey = accessKey.substring( 0, 1 );
        setAttribute( "accesskey", accessKey );
    }

    
    public boolean getDisabled()
    {
        return getBinary( "disabled" );
    }
    
    
    public void setDisabled( boolean disabled )
    {
        setAttribute( "disabled", disabled );
    }


    public String getName()
    {
        return getAttribute( "name" );
    }
    
    
    public void setName( String name )
    {
        setAttribute( "name", name );
    }

    
    public int getTabIndex()
    {
        try
        {
            return Integer.parseInt( getAttribute( "tabindex" ) );
        }
        catch ( NumberFormatException except )
        {
            return 0;
        }
    }
    
    
    public void setTabIndex( int tabIndex )
    {
        setAttribute( "tabindex", String.valueOf( tabIndex ) );
    }

    
    public String getType()
    {
        return capitalize( getAttribute( "type" ) );
    }

    
      public String getValue()
    {
        return getAttribute( "value" );
    }
    
    
    public void setValue( String value )
    {
        setAttribute( "value", value );
    }
    
    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLButtonElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
  
  
}

