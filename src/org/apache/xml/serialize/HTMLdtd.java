/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.xml.serialize;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Hashtable;


/**
 * Utility class for accessing information specific to HTML documents.
 * The HTML DTD is expressed as three utility function groups. Two methods
 * allow for checking whether an element requires an open tag on printing
 * ({@link #isEmptyTag}) or on parsing ({@link #isOptionalClosing}).
 * <P>
 * Two other methods translate character references from name to value and
 * from value to name. A small entities resource is loaded into memory the
 * first time any of these methods is called for fast and efficient access.
 *
 *
 * @version
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 */
final class HTMLdtd
{


    /**
     * Table of reverse character reference mapping. Character codes are held
     * as single-character strings, mapped to their reference name.
     */
    private static Hashtable        _byChar;


    /**
     * Table of entity name to value mapping. Entities are held as strings,
     * character references as <TT>Character</TT> objects.
     */
    private static Hashtable        _byName;


    /**
     * Locates the HTML entities file that is loaded upon initialization.
     * This file is a resource loaded with the default class loader.
     */
    private static final String     ENTITIES_RESOURCE = "HTMLEntities.res";


    /**
     * Holds element definitions.
     */
    private static Hashtable        _elemDefs;


    /**
     * Only opening tag should be printed.
     */
    private static final int ONLY_OPENING = 0x0001;

    /**
     * Element contains element content only.
     */
    private static final int ELEM_CONTENT = 0x0002;

    /**
     * Element preserve spaces.
     */
    private static final int PRESERVE     = 0x0004;

    /**
     * Optional closing tag.
     */
    private static final int OPT_CLOSING  = 0x0008;

    /**
     * Element is empty (also means only opening tag)
     */
    private static final int EMPTY        = 0x0010 | ONLY_OPENING;

    /**
     * Allowed to appear in head.
     */
    private static final int ALLOWED_HEAD = 0x0020;

    /**
     * When opened, closes P.
     */
    private static final int CLOSE_P      = 0x0040;

    /**
     * When opened, closes DD or DT.
     */
    private static final int CLOSE_DD_DT  = 0x0080;

    /**
     * When opened, closes itself.
     */
    private static final int CLOSE_SELF   = 0x0100;


    /**
     * When opened, closes another table section.
     */
    private static final int CLOSE_TABLE  = 0x0200;

    /**
     * When opened, closes TH or TD.
     */
    private static final int CLOSE_TH_TD  = 0x04000;




    /**
     * Returns true if element is declared to be empty. HTML elements are
     * defines as empty in the DTD, not by the document syntax.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element is empty
     */
    public static boolean isEmptyTag( String tagName )
    {
        // BR AREA LINK IMG PARAM HR INPUT COL BASE META BASEFONT ISINDEX FRAME
	/*
        return ( tagName.equals( "BR" ) || tagName.equals( "AREA" ) ||
                 tagName.equals( "LINK" ) || tagName.equals( "IMG" ) ||
                 tagName.equals( "PARAM" ) || tagName.equals( "HR" ) ||
                 tagName.equals( "INPUT" ) || tagName.equals( "COL" ) ||
                 tagName.equals( "BASE" ) || tagName.equals( "META" ) ||
                 tagName.equals( "BASEFONT" ) || tagName.equals( "ISINDEX" ) );
	*/
	return isElement( tagName, EMPTY );
    }


    /**
     * Returns true if element is declared to have element content.
     * Whitespaces appearing inside element content will be ignored,
     * other text will simply report an error.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element content
     */
    public static boolean isElementContent( String tagName )
    {
        // DL OL UL SELECT OPTGROUP TABLE THEAD TFOOT TBODY COLGROUP TR HEAD HTML
	/*
        return ( tagName.equals( "DL" ) || tagName.equals( "OL" ) ||
                 tagName.equals( "UL" ) || tagName.equals( "SELECT" ) ||
                 tagName.equals( "OPTGROUP" ) || tagName.equals( "TABLE" ) ||
                 tagName.equals( "THEAD" ) || tagName.equals( "TFOOT" ) ||
                 tagName.equals( "TBODY" ) || tagName.equals( "COLGROUP" ) ||
                 tagName.equals( "TR" ) || tagName.equals( "HEAD" ) ||
                 tagName.equals( "HTML" ) );
	*/
	return isElement( tagName, ELEM_CONTENT );
    }

    
    /**
     * Returns true if element's textual contents preserves spaces.
     * This only applies to PRE and TEXTAREA, all other HTML elements
     * do not preserve space.
     * 
     * @param tagName The element tag name (upper case)
     * @return True if element's text content preserves spaces
     */
    public static boolean isPreserveSpace( String tagName )
    {
        // PRE TEXTAREA
	/*
        return ( tagName.equals( "PRE" ) || tagName.equals( "TEXTAREA" ) );
	*/
	return isElement( tagName, PRESERVE );
    }


    /**
     * Returns true if element's closing tag is optional and need not
     * exist. An error will not be reported for such elements if they
     * are not closed. For example, <tt>LI</tt> is most often not closed.
     *
     * @param tagName The element tag name (upper case)
     * @return True if closing tag implied
     */
    public static boolean isOptionalClosing( String tagName )
    {
        // BODY HEAD HTML P DT DD LI OPTION THEAD TFOOT TBODY TR COLGROUP TH TD FRAME
	/*
        return ( tagName.equals( "BODY" ) || tagName.equals( "HEAD" ) ||
                 tagName.equals( "HTML" ) || tagName.equals( "P" ) ||
                 tagName.equals( "DT" ) || tagName.equals( "DD" ) ||
                 tagName.equals( "LI" ) || tagName.equals( "OPTION" ) ||
                 tagName.equals( "THEAD" ) || tagName.equals( "TFOOT" ) ||
                 tagName.equals( "TBODY" ) || tagName.equals( "TR" ) ||
                 tagName.equals( "COLGROUP" ) || tagName.equals( "TH" ) ||
                 tagName.equals( "TD" ) || tagName.equals( "FRAME" ) );
	*/
	return isElement( tagName, OPT_CLOSING );
    }


    /**
     * Returns true if element's closing tag is generally not printed.
     * For example, <tt>LI</tt> should not print the closing tag.
     *
     * @param tagName The element tag name (upper case)
     * @return True if only opening tag should be printed
     */
    public static boolean isOnlyOpening( String tagName )
    {
        //DT DD LI OPTION
	/*
        return ( tagName.equals( "DT" ) || tagName.equals( "DD" ) ||
		 tagName.equals( "LI" ) || tagName.equals( "OPTION" ) );
	*/
	return isElement( tagName, ONLY_OPENING );
    }


    /**
     * Returns true if the opening of one element (<tt>tagName</tt>) implies
     * the closing of another open element (<tt>openTag</tt>). For example,
     * every opening <tt>LI</tt> will close the previously open <tt>LI</tt>,
     * and every opening <tt>BODY</tt> will close the previously open <tt>HEAD</tt>.
     *
     * @param tagName The newly opened element
     * @param openTag The already opened element
     * @return True if closing tag closes opening tag
     */    
    public static boolean isClosing( String tagName, String openTag )
    {
        // BODY (closing HTML, end of document)
        // HEAD (BODY, closing HTML, end of document)
        if ( openTag.equalsIgnoreCase( "HEAD" ) )
	    /*
            return ! ( tagName.equals( "ISINDEX" ) || tagName.equals( "TITLE" ) ||
		       tagName.equals( "META" ) || tagName.equals( "SCRIPT" ) ||
		       tagName.equals( "STYLE" ) || tagName.equals( "LINK" ) );
	    */
	    return ! isElement( tagName, ALLOWED_HEAD );
        // P (P, H1-H6, UL, OL, DL, PRE, DIV, BLOCKQUOTE, FORM, HR, TABLE, ADDRESS, FIELDSET, closing BODY, closing HTML, end of document)
        if ( openTag.equalsIgnoreCase( "P" ) )
	    /*
            return ( tagName.endsWith( "P" ) || tagName.endsWith( "H1" ) ||
                     tagName.endsWith( "H2" ) || tagName.endsWith( "H3" ) ||
                     tagName.endsWith( "H4" ) || tagName.endsWith( "H5" ) ||
                     tagName.endsWith( "H6" ) || tagName.endsWith( "UL" ) ||
                     tagName.endsWith( "OL" ) || tagName.endsWith( "DL" ) ||
                     tagName.endsWith( "PRE" ) || tagName.endsWith( "DIV" ) ||
                     tagName.endsWith( "BLOCKQUOTE" ) || tagName.endsWith( "FORM" ) ||
                     tagName.endsWith( "HR" ) || tagName.endsWith( "TABLE" ) ||
                     tagName.endsWith( "ADDRESS" ) || tagName.endsWith( "FIELDSET" ) );
	    */
	    return isElement( tagName, CLOSE_P );
        if ( openTag.equalsIgnoreCase( "DT" ) || openTag.equalsIgnoreCase( "DD" ) )
	    return isElement( tagName, CLOSE_DD_DT );
        // DT (DD)
	/*
        if ( openTag.equals( "DT" ) )
            return tagName.endsWith( "DD" );
	*/
        // DD (DT, closing DL)
	/*
	if ( openTag.equals( "DD" ) )
            return tagName.endsWith( "DT" );
	*/
        if ( openTag.equalsIgnoreCase( "LI" ) || openTag.equalsIgnoreCase( "OPTION" ) )
	    return isElement( tagName, CLOSE_SELF );
        // LI (LI, closing UL/OL)
	/*
        if ( openTag.equals( "LI" ) )
            return tagName.endsWith( "LI" );
	*/
        // OPTION (OPTION, OPTGROUP closing or opening, closing SELECT)
	/*
        if ( openTag.equals( "OPTION" ) )
            return tagName.endsWith( "OPTION" );
	*/
        // THEAD (TFOOT, TBODY, TR, closing TABLE
        // TFOOT (TBODY, TR, closing TABLE)
        // TBODY (TBODY, closing TABLE)
        // COLGROUP (THEAD, TBODY, TR, closing TABLE)
        // TR (TR, closing THEAD, TFOOT, TBODY, TABLE)
        if ( openTag.equalsIgnoreCase( "THEAD" ) || openTag.equalsIgnoreCase( "TFOOT" ) ||
             openTag.equalsIgnoreCase( "TBODY" ) || openTag.equalsIgnoreCase( "TR" ) || 
             openTag.equalsIgnoreCase( "COLGROUP" ) )
	    /*
            return ( tagName.endsWith( "THEAD" ) || tagName.endsWith( "TFOOT" ) ||
                     tagName.endsWith( "TBODY" ) || tagName.endsWith( "TR" ) ||
                     tagName.endsWith( "COLGROUP" ) );
	    */
	    return isElement( tagName, CLOSE_TABLE );
        // TH (TD, TH, closing TR)
        // TD (TD, TH, closing TR)
        if ( openTag.equalsIgnoreCase( "TH" ) || openTag.equalsIgnoreCase( "TD" ) )
	    /*
            return ( tagName.endsWith( "TD" ) || tagName.endsWith( "TH" ) );
	    */
	    return isElement( tagName, CLOSE_TH_TD );
        return false;
    }


    /**
     * Returns true if the specified attribute it a URI and should be
     * escaped appropriately. In HTML URIs are escaped differently
     * than normal attributes.
     *
     * @param tagName The element's tag name
     * @param attrName The attribute's name
     */
    public static boolean isURI( String tagName, String attrName )
    {
	// Stupid checks.
	return ( attrName.equalsIgnoreCase( "href" ) || attrName.equalsIgnoreCase( "src" ) );
    }

        
    /**
     * Returns the value of an HTML character reference by its name. If the
     * reference is not found or was not defined as a character reference,
     * returns EOF (-1).
     *
     * @param name Name of character reference
     * @return Character code or EOF (-1)
     */
    public static int charFromName( String name )
    {
        Object    value;

        initialize();
        value = _byName.get( name );
        if ( value != null && value instanceof Character )
            return ( (Character) value ).charValue();
        else
            return -1;
    }


    /**
     * Returns the name of an HTML character reference based on its character
     * value. Only valid for entities defined from character references. If no
     * such character value was defined, return null.
     *
     * @param value Character value of entity
     * @return Entity's name or null
     */
    public static String fromChar( char value )
    {
        String    name;

        initialize();
        name = (String) _byChar.get( String.valueOf( value ) );
        if ( name == null )
            return null;
        else
            return name;
    }


    /**
     * Initialize upon first access. Will load all the HTML character references
     * into a list that is accessible by name or character value and is optimized
     * for character substitution. This method may be called any number of times
     * but will execute only once.
     */
    private static void initialize()
    {
        InputStream     is = null;
        BufferedReader  reader = null;
        int             index;
        String          name;
        String          value;
        int             code;
        String          line;

        // Make sure not to initialize twice.
        if ( _byName != null )
            return;
        try
        {
            _byName = new Hashtable();
            _byChar = new Hashtable();
            is = HTMLdtd.class.getResourceAsStream( ENTITIES_RESOURCE );
            if ( is == null )
                throw new RuntimeException( "The resource [" + ENTITIES_RESOURCE + "] could not be found." );
            reader = new BufferedReader( new InputStreamReader( is ) );
            line = reader.readLine();
            while ( line != null )
            {
                if ( line.length() == 0 || line.charAt( 0 ) == '#' )
                {
                    line = reader.readLine();
                    continue;
                }
                index = line.indexOf( ' ' );
                if ( index > 1 )
                {
                    name = line.substring( 0, index );
                    ++index;
                    if ( index < line.length() )
                    {
                        value = line.substring( index );
                        index = value.indexOf( ' ' );
                        if ( index > 0 )
                            value = value.substring( 0, index );
                        code = Integer.parseInt( value );
                        defineEntity( name, (char) code );
                    }
                }
                line = reader.readLine();
            }
            is.close();
        }
        catch ( Exception except )
        {
            throw new RuntimeException( "The resource [" + ENTITIES_RESOURCE + "] could not load: " +
					except.toString() );
        }
        finally
        {
            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( Exception except )
                {
                }
            }
        }
    }


    /**
     * Defines a new character reference. The reference's name and value are
     * supplied. Nothing happens if the character reference is already defined.
     * <P>
     * Unlike internal entities, character references are a string to single
     * character mapping. They are used to map non-ASCII characters both on
     * parsing and printing, primarily for HTML documents. '&lt;amp;' is an
     * example of a character reference.
     *
     * @param name The entity's name
     * @param value The entity's value
     */
    private static void defineEntity( String name, char value )
    {
        if ( _byName.get( name ) == null )
        {
            _byName.put( name, new Character( value ) );
            _byChar.put( String.valueOf( value ), name );
        }
    }


    private static void defineElement( String name, int flags )
    {
	_elemDefs.put( name, new Integer( flags ) );
    }


    private static boolean isElement( String name, int flag )
    {
	Integer flags;

	flags = (Integer) _elemDefs.get( name.toUpperCase() );
	if ( flags == null )
	    return false;
	else
	    return ( ( flags.intValue() & flag ) != 0 );
    }


    static
    {
	_elemDefs = new Hashtable();
	defineElement( "ADDRESS", CLOSE_P );
	defineElement( "AREA", EMPTY );
	defineElement( "BASE", EMPTY );
	defineElement( "BASEFONT", EMPTY );
	defineElement( "BLOCKQUOTE", CLOSE_P );
	defineElement( "BODY", OPT_CLOSING );
	defineElement( "BR", EMPTY );
	defineElement( "COL", EMPTY );
	defineElement( "COLGROUP", ELEM_CONTENT | OPT_CLOSING | CLOSE_TABLE );
	defineElement( "DD", OPT_CLOSING | ONLY_OPENING | CLOSE_DD_DT );
	defineElement( "DIV", CLOSE_P );
	defineElement( "DL", ELEM_CONTENT | CLOSE_P );
	defineElement( "DT", OPT_CLOSING | ONLY_OPENING | CLOSE_DD_DT );
	defineElement( "FIELDSET", CLOSE_P );
	defineElement( "FORM", CLOSE_P );
	defineElement( "FRAME", EMPTY | OPT_CLOSING );
	defineElement( "H1", CLOSE_P );
	defineElement( "H2", CLOSE_P );
	defineElement( "H3", CLOSE_P );
	defineElement( "H4", CLOSE_P );
	defineElement( "H5", CLOSE_P );
	defineElement( "H6", CLOSE_P );
	defineElement( "HEAD", ELEM_CONTENT | OPT_CLOSING );
	defineElement( "HR", EMPTY | CLOSE_P );
	defineElement( "HTML", ELEM_CONTENT | OPT_CLOSING );
	defineElement( "IMG", EMPTY );
	defineElement( "INPUT", EMPTY );
	defineElement( "ISINDEX", EMPTY | ALLOWED_HEAD );
	defineElement( "LI", OPT_CLOSING | ONLY_OPENING | CLOSE_SELF );
	defineElement( "LINK", EMPTY | ALLOWED_HEAD );
	defineElement( "META", EMPTY | ALLOWED_HEAD );
	defineElement( "OL", ELEM_CONTENT | CLOSE_P );
	defineElement( "OPTGROUP", ELEM_CONTENT );
	defineElement( "OPTION", OPT_CLOSING | ONLY_OPENING | CLOSE_SELF );
	defineElement( "P", OPT_CLOSING | CLOSE_P | CLOSE_SELF );
	defineElement( "PARAM", EMPTY );
	defineElement( "PRE", PRESERVE | CLOSE_P );
	defineElement( "SCRIPT", ALLOWED_HEAD | PRESERVE );
	defineElement( "SELECT", ELEM_CONTENT );
	defineElement( "STYLE", ALLOWED_HEAD | PRESERVE );
	defineElement( "TABLE", ELEM_CONTENT | CLOSE_P );
	defineElement( "TBODY", ELEM_CONTENT | OPT_CLOSING | CLOSE_TABLE );
	defineElement( "TD", OPT_CLOSING | CLOSE_TH_TD );
	defineElement( "TEXTAREA", PRESERVE );
	defineElement( "TFOOT", ELEM_CONTENT | OPT_CLOSING | CLOSE_TABLE );
	defineElement( "TH", OPT_CLOSING | CLOSE_TH_TD );
	defineElement( "THEAD", ELEM_CONTENT | OPT_CLOSING | CLOSE_TABLE );
	defineElement( "TITLE", ALLOWED_HEAD );
	defineElement( "TR", ELEM_CONTENT | OPT_CLOSING | CLOSE_TABLE );
	defineElement( "UL", ELEM_CONTENT | CLOSE_P );
    }



}

