package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.apache.xerces.dom.ElementImpl;


/**
 * Implements an HTML-specific element, an {@link org.w3c.dom.Element} that
 * will only appear inside HTML documents. This element extends {@link
 * org.apache.xerces.dom.ElementImpl} by adding methods for directly
 * manipulating HTML-specific attributes. All HTML elements gain access to
 * the <code>id</code>, <code>title</code>, <code>lang</code>,
 * <code>dir</code> and <code>class</code> attributes. Other elements
 * add their own specific attributes.
 * 
 * 
 * @version $Revision$ $Date$
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLElement
 */
public class HTMLElementImpl
    extends ElementImpl
    implements HTMLElement
{


    /**
     * Constructor required owner document and element tag name. Will be called
     * by the constructor of specific element types but with a known tag name.
     * Assures that the owner document is an HTML element.
     * 
     * @param owner The owner HTML document
     * @param tagName The element's tag name
     */
    HTMLElementImpl( HTMLDocumentImpl owner, String tagName )
    {
        super( owner, tagName.toUpperCase() );
    }
    
    
    public String getId()
    {
        return getAttribute( "id" );
    }
    
    
    public void setId( String id )
    {
        setAttribute( "id", id );
    }
    
    
    public String getTitle()
    {
        return getAttribute( "title" );
    }
    
    
    public void setTitle( String title )
    {
        setAttribute( "title", title );
    }
    
    
    public String getLang()
    {
        return getAttribute( "lang" );
    }
    
    
    public void setLang( String lang )
    {
        setAttribute( "lang", lang );
    }
    
    
    public String getDir()
    {
        return getAttribute( "dir" );
    }
    
    
    public void setDir( String dir )
    {
        setAttribute( "dir", dir );
    }

    
    public String getClassName()
    {
        return getAttribute( "class" );
    }

    
    public void setClassName( String className )
    {
        setAttribute( "class", className );
    }
    
    
    /**
     * Convenience method used to translate an attribute value into an integer
     * value. Returns the integer value or zero if the attribute is not a
     * valid numeric string.
     * 
     * @param value The value of the attribute
     * @return The integer value, or zero if not a valid numeric string
     */
    int getInteger( String value )
    {
        try
        {
            return Integer.parseInt( value );
        }
        catch ( NumberFormatException except )
        {
            return 0;
        }
    }
    
    
    /**
     * Convenience method used to translate an attribute value into a boolean
     * value. If the attribute has an associated value (even an empty string),
     * it is set and true is returned. If the attribute does not exist, false
     * is returend.
     * 
     * @param value The value of the attribute
     * @return True or false depending on whether the attribute has been set
     */
    boolean getBinary( String name )
    {
        return ( getAttributeNode( name ) != null );
    }
    
    
    /**
     * Convenience method used to set a boolean attribute. If the value is true,
     * the attribute is set to an empty string. If the value is false, the attribute
     * is removed. HTML 4.0 understands empty strings as set attributes.
     * 
     * @param name The name of the attribute
     * @param value The value of the attribute
     */
    void setAttribute( String name, boolean value )
    {
        if ( value )
            setAttribute( name, name );
        else
            removeAttribute( name );
    }
    
    
    /**
     * Convenience method used to capitalize a one-off attribute value before it
     * is returned. For example, the align values "LEFT" and "left" will both
     * return as "Left".
     * 
     * @param value The value of the attribute
     * @return The capitalized value
     */
    String capitalize( String value )
    {
        char[]    chars;
        int        i;
        
        // Convert string to charactares. Convert the first one to upper case,
        // the other characters to lower case, and return the converted string.
        chars = value.toCharArray();
        if ( chars.length > 0 )
        {
            chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
            for ( i = 1 ; i < chars.length ; ++i )
                chars[ i ] = Character.toLowerCase( chars[ i ] );
            return String.valueOf( chars );
        }
        return value;
    }
    

    /**
     * Convenience method used to capitalize a one-off attribute value before it
     * is returned. For example, the align values "LEFT" and "left" will both
     * return as "Left".
     * 
     * @param name The name of the attribute
     * @return The capitalized value
     */
    String getCapitalized( String name )
    {
        String    value;
        char[]    chars;
        int        i;
        
        value = getAttribute( name );
        if ( value != null )
        {
            // Convert string to charactares. Convert the first one to upper case,
            // the other characters to lower case, and return the converted string.
            chars = value.toCharArray();
            if ( chars.length > 0 )
            {
                chars[ 0 ] = Character.toUpperCase( chars[ 0 ] );
                for ( i = 1 ; i < chars.length ; ++i )
                    chars[ i ] = Character.toLowerCase( chars[ i ] );
                return String.valueOf( chars );
            }
        }
        return value;
    }

    
    /**
     * Convenience method returns the form in which this form element is contained.
     * This method is exposed for form elements through the DOM API, but other
     * elements have no access to it through the API.
     */
    public HTMLFormElement getForm()
    {
        Node    parent;
        
        parent = getParentNode(); 
        while ( parent != null )
        {
            if ( parent instanceof HTMLFormElement )
                return (HTMLFormElement) parent;
            parent = parent.getParentNode();
        }
        return null;
    }


}

