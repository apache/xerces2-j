/*
 * $Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
 * 4. The names "Crimson" and "Apache Software Foundation" must
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
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */

package org.apache.xerces.tree;


import java.io.CharArrayWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.*;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;

// Should move to use the xerces sax2 Attribute impl
import org.apache.xerces.tree.AttributeListEx;


/**
 * Class representing an XML attribute list.
 *
 * <P> This couples slightly with the Sun XML parser, in that it optionally
 * uses an extended SAX 1.0 API to see if an attribute was specified in the
 * document or was instead defaulted by attribute processing.
 *
 * @author David Brownell
 * @version $Revision$
 */
final
class AttributeSet implements NamedNodeMap, XmlWritable
{
    private boolean	readonly;
    private Vector	list;
    private ElementNode	nameScope;
        
    /* Constructs an attribute list, with associated name scope. */
    // package private
    AttributeSet (ElementNode nameScope)
    {
	list = new Vector (5);
	this.nameScope = nameScope;
    }

    /*
     * Constructs a copy of an attribute list, for use in cloning.
     * name scopes are set separately.
     */
    // package private
    AttributeSet (AttributeSet original, boolean deep)
    {
	int		size = original.getLength ();

	list = new Vector (size);
	for (int i = 0; i < size; i++) {
	    Node	node = original.item (i);

	    if (!(node instanceof AttributeNode))
		throw new IllegalArgumentException (((NodeBase)node).
						getMessage ("A-003"));
	    node = node.cloneNode (deep);

	    // temporarily undo binding to element ... it's rebound
	    // by the caller
	    ((AttributeNode)node).setNameScope (null);
	    list.addElement (node);
	}
    }

    // package private
    AttributeSet (AttributeList source)
    throws DOMException
    {
	int			len = source.getLength ();
	AttributeListEx		ex = null;

	list = new Vector (len);
	if (source instanceof AttributeListEx)
	    ex = (AttributeListEx) source;

	for (int i = 0; i < len; i++) {
	    list.addElement (new AttributeNode (
		    source.getName (i),
		    source.getValue (i),
		    ex == null	// remember if it was specified
			? true
			: ex.isSpecified (i),
		    ex == null	// remember any default value
			? null
			: ex.getDefault (i)
		    ));
	}
	list.trimToSize ();
    }

    /**
     * <b>DOM2:</b> Create DOM NamedNodeMap from SAX2 Attributes object
     */
    AttributeSet(Attributes source) throws DOMException {
	int			len = source.getLength();
	AttributeListEx		ex = null;

	list = new Vector(len);
	if (source instanceof AttributeListEx) { // XXX fix this
	    ex = (AttributeListEx) source;
        }

	for (int i = 0; i < len; i++) {
            // Translate "" of SAX2 to null.  See DOM2 spec under Node
            // namespaceURI
            String uri = source.getURI(i);
            if (uri.equals("")) {
                uri = null;
            }

            AttributeNode attrNode =
                new AttributeNode(uri,
                                  source.getRawName(i),
                                  source.getValue(i),
                                  ex == null	// remember if it was specified
                                  ? true
                                  : ex.isSpecified(i),
                                  ex == null	// remember any default value
                                  ? null
                                  : ex.getDefault(i));
	    list.addElement(attrNode);
	}
	list.trimToSize();
    }

    // package private
    void trimToSize () { list.trimToSize (); }

    // package private
    public void setReadonly ()
    {
	readonly = true;
	for (int i = 0; i < list.size (); i++)
	    ((AttributeNode)list.elementAt (i)).setReadonly (true);
    }

    public boolean isReadonly () {
    	if (readonly)
	    return true;
	for (int i = 0; i < list.size (); i++) {
	    if (((AttributeNode)list.elementAt (i)).isReadonly ()) {
	   	return true; 
	    }
	}
	return false;
    }

    // package private
    void setNameScope (ElementNode e)
    {
	if (e != null && nameScope != null)
	    throw new IllegalStateException (e.getMessage ("A-004"));
	nameScope = e;

	// need to bind the attributes to this element
	int	length = list.size ();

	for (int i = 0; i < length; i++) {
	    AttributeNode	node;

	    node = (AttributeNode) list.elementAt (i);
	    node.setNameScope (null);
	    node.setNameScope (e);
	}
    }

    // package private
    ElementNode getNameScope ()
    {
	return nameScope;
    }


    // package private
    String getValue (String name)
    {
	Attr	attr = (Attr) getNamedItem (name);

	if (attr == null)
	    return "";
	else
	    return attr.getValue ();
    }

    public Node getNamedItem (String name)
    {
	int	length = list.size ();
	Node	value;

	for (int i = 0; i < length; i++) {
	    value = item (i);
	    if (value.getNodeName ().equals (name))
		return value;
	}
	return null;
    }

    /**
     * <b>DOM2:</b>
     */
    public Node getNamedItemNS(String namespaceURI, String localName) {
	for (int i = 0; i < list.size(); i++) {
	    Node value = item(i);
            String iLocalName = value.getLocalName();
	    if (iLocalName != null && iLocalName.equals(localName)) {
                String iNamespaceURI = value.getNamespaceURI();
                if (iNamespaceURI != null
                    && iNamespaceURI.equals(namespaceURI)) {
                    return value;
                }
            }
	}
	return null;
    }

    public int getLength ()
    {
	return list.size ();
    }

    public Node item (int index)
    {
	if (index < 0 || index >= list.size ())
	    return null;
	return (Node) list.elementAt (index);
    }

    public Node removeNamedItem (String name)
    throws DOMException
    {
	int		length = list.size ();
	Node		value;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	for (int i = 0; i < length; i++) {
	    value = item (i);
	    if (value.getNodeName ().equals (name)) {
		AttributeNode	att = (AttributeNode) value;

		if (att.getDefaultValue () != null) {
		    att = new AttributeNode (att);
		    att.setOwnerDocument ((XmlDocument)
			    nameScope.getOwnerDocument ());
		    list.setElementAt (att, i);
		} else
		    list.removeElementAt (i);

		att.setNameScope (null);
		return value;
	    }
	}
	throw new DomEx (DomEx.NOT_FOUND_ERR);
    }

    /**
     * <b>DOM2:</b>
     */
    public Node removeNamedItemNS(String namespaceURI, String localName)
        throws DOMException
    {
	if (readonly) {
	    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

	for (int i = 0; i < list.size(); i++) {
	    Node value = item(i);
            String iLocalName = value.getLocalName();
	    if (iLocalName != null && iLocalName.equals(localName)) {
                String iNamespaceURI = value.getNamespaceURI();
                if (iNamespaceURI != null
                    && iNamespaceURI.equals(namespaceURI)) {
                    AttributeNode attr = (AttributeNode) value;
                    if (attr.getDefaultValue() != null) {
                        attr = new AttributeNode(attr);
                        list.setElementAt(attr, i);
                    } else {
                        list.removeElementAt(i);
                    }
                    return value;
                }
            }
	}
	throw new DomEx(DomEx.NOT_FOUND_ERR);
    }

    public Node setNamedItem (Node value)
    throws DOMException
    {
	AttributeNode	node;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (!(value instanceof AttributeNode)
		|| value.getOwnerDocument ()
			!= nameScope.getOwnerDocument ())
	    throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);
	node = (AttributeNode)value;
	if (node.getNameScope () != null)
	    throw new DomEx (DomEx.INUSE_ATTRIBUTE_ERR);

	int		length = list.size ();
	AttributeNode	oldValue;

	for (int i = 0; i < length; i++) {
	    oldValue = (AttributeNode) item (i);
	    if (oldValue.getNodeName ().equals (value.getNodeName ())) {
	    	if (oldValue.isReadonly ())
	    	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
		node.setNameScope (nameScope);
		list.setElementAt (value, i);
		oldValue.setNameScope (null);
		return oldValue;
	    }
	}
	node.setNameScope (nameScope);
	list.addElement (value);
	return null;
    }
    
    /**
     * <b>DOM2:</b>
     * XXX spec allows Element nodes also, but this code assumes Attr nodes
     * only
     */
    public Node setNamedItemNS(Node arg) throws DOMException {
	if (readonly) {
	    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

	AttributeNode attr = (AttributeNode) arg;
        if (attr.getOwnerElement() != null) {
	    throw new DomEx(DomEx.INUSE_ATTRIBUTE_ERR);
        }

	for (int i = 0; i < list.size(); i++) {
	    AttributeNode oldNode = (AttributeNode) item(i);
            String localName = oldNode.getLocalName();
            String namespaceURI = oldNode.getNamespaceURI();
            if (arg.getLocalName().equals(localName)
                && arg.getNamespaceURI().equals(namespaceURI)) {
                // Found a matching node so replace it
                list.setElementAt(arg, i);
                return oldNode;
            }
	}

        // Append instead of replace
	list.addElement(arg);
	return null;
    }

    /**
     * Writes out the attribute list.  Attributes known to have been
     * derived from the DTD are not (at this time) written out.  Part
     * of writing standalone XML is first ensuring that all attributes
     * are flagged as being specified in the "printed" form (or else
     * are defaulted only in the internal DTD subset).
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer		out = context.getWriter ();
	int		length = list.size ();
	AttributeNode	tmp;

	for (int i = 0; i < length; i++) {
	    tmp = (AttributeNode) list.elementAt (i);
	    if (tmp.getSpecified ()) {
		out.write (' ');
		tmp.writeXml (context);
	    }
	}
    }

    /**
     * Does nothing; this type of node has no children.
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
    }

    public String toString ()
    {
	try {
	    CharArrayWriter w = new CharArrayWriter ();
	    XmlWriteContext x = new XmlWriteContext (w);
	    writeXml (x);
	    return w.toString ();

	} catch (IOException e) {
	    return super.toString ();
	}
    }
}
