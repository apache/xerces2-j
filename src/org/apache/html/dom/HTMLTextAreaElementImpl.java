package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLTextAreaElement
 * @see ElementImpl
 */
public final class HTMLTextAreaElementImpl
    extends HTMLElementImpl
    implements HTMLTextAreaElement, HTMLFormControl
{
    
    
    public String getDefaultValue()
    {
        // ! NOT FULLY IMPLEMENTED !
        return getAttribute( "default-value" );
    }
    
    
    public void setDefaultValue( String defaultValue )
    {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute( "default-value", defaultValue );
    }
  
  

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

    
    public int getCols()
    {
        return getInteger( getAttribute( "cols" ) );
    }
    
    
    public void setCols( int cols )
    {
        setAttribute( "cols", String.valueOf( cols ) );
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

    
    public boolean getReadOnly()
    {
        return getBinary( "readonly" );
    }
    
    
    public void setReadOnly( boolean readOnly )
    {
        setAttribute( "readonly", readOnly );
    }

    
       public int getRows()
    {
        return getInteger( getAttribute( "rows" ) );
    }
    
    
    public void setRows( int rows )
    {
        setAttribute( "rows", String.valueOf( rows ) );
    }

  
       public int getTabIndex()
    {
        return getInteger( getAttribute( "tabindex" ) );
    }
    
    
    public void setTabIndex( int tabIndex )
    {
        setAttribute( "tabindex", String.valueOf( tabIndex ) );
    }

  
    public String getType()
    {
        return getAttribute( "type" );
    }

    
      public String getValue()
    {
        return getAttribute( "value" );
    }
    
    
    public void setValue( String value )
    {
        setAttribute( "value", value );
    }

    
    public void blur()
    {
        // No scripting in server-side DOM. This method is moot.
    }
    
    
    public void focus()
    {
        // No scripting in server-side DOM. This method is moot.
    }
    
    
    public void select()
    {
        // No scripting in server-side DOM. This method is moot.
    }
    
      
      /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLTextAreaElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

