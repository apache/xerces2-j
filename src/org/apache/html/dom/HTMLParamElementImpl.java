package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLParamElement
 * @see ElementImpl
 */
public final class HTMLParamElementImpl
    extends HTMLElementImpl
    implements HTMLParamElement
{
    
    
    public String getName()
    {
        return getAttribute( "name" );
    }
    
    
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
  
  
    public String getType()
    {
        return getAttribute( "type" );
    }
    
    
    public void setType( String type )
    {
        setAttribute( "type", type );
    }
    
    
      public String getValue()
    {
        return getAttribute( "value" );
    }
    
    
    public void setValue( String value )
    {
        setAttribute( "value", value );
    }

    
      public String getValueType()
    {
        return capitalize( getAttribute( "valuetype" ) );
    }
    
    
    public void setValueType( String valueType )
    {
        setAttribute( "valuetype", valueType );
    }


    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLParamElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }

  
}

