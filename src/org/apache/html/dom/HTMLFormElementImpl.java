package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLFormElement
 * @see ElementImpl
 */
public final class HTMLFormElementImpl
    extends HTMLElementImpl
    implements HTMLFormElement
{

    
    public HTMLCollection getElements()
    {
        if ( _elements == null )
            _elements = new HTMLCollectionImpl( this, HTMLCollectionImpl.ELEMENT );
        return _elements;
    }
  

    public int getLength()
    {
        return getElements().getLength();
    }
  
  
    public String getName()
    {
        return getAttribute( "name" );
    }
    
    
    public void setName( String name )
    {
        setAttribute( "name", name );
    }

    
    public String getAcceptCharset()
    {
        return getAttribute( "accept-charset" );
    }
    
    
    public void setAcceptCharset( String acceptCharset )
    {
        setAttribute( "accept-charset", acceptCharset );
    }

  
      public String getAction()
    {
        return getAttribute( "action" );
    }
    
    
    public void setAction( String action )
    {
        setAttribute( "action", action );
    }
  
  
      public String getEnctype()
    {
        return getAttribute( "enctype" );
    }
    
    
    public void setEnctype( String enctype )
    {
        setAttribute( "enctype", enctype );
    }

    
      public String getMethod()
    {
        return capitalize( getAttribute( "method" ) );
    }
    
    
    public void setMethod( String method )
    {
        setAttribute( "method", method );
    }
  
  
    public String getTarget()
    {
        return getAttribute( "target" );
    }
    
    
    public void setTarget( String target )
    {
        setAttribute( "target", target );
    }

    
    public void submit()
    {
        // No scripting in server-side DOM. This method is moot.
    }

    
    public void reset()
    {
        // No scripting in server-side DOM. This method is moot.
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLFormElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
  
    
    /**
     * Collection of all elements contained in this FORM.
     */
    private HTMLCollectionImpl    _elements;
    
}

