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

import org.w3c.dom.Node;


/**
 * This interface is supported by elements and attributes whose names
 * are scoped according to the XML Namespaces specification.
 *
 * @author David Brownell
 * @version $Revision$
 */
public interface NamespaceScoped extends NodeEx
{
    /**
     * Returns the "local part" of the object's scoped name, without
     * any namespace prefix.
     */
    public String	getLocalName ();

    /**
     * Return the XML namespace name (a URI) associated with this object,
     * or null for the case of the default document namespace.  This is
     * computed from this node and its ancestors.
     *
     * @exception IllegalStateException Thrown when the namespace
     *	prefix for this element is not known.
     */
    public String	getNamespace ();

    /**
     * Returns any prefix of the object's name.  This is only a
     * context-sensitive alias for the namespace URI.  If the name
     * is unqualified (e.g. <em>template</em> vs <em>xsl:template</em>),
     * the null string is returned.
     *
     * <P> The URI corresponding to that prefix may be retrieved by
     * getting the inherited value of the attribute named
     * <em>"xmlns:" + getPrefix ()</em> if the prefix is not null;
     * if there is no value for this URI, that indicates an error.
     * If the prefix is null, the URI is the inherited value of the
     * attribute named <em>"xmlns"</em>; if there is no value for
     * that URI, the default namespace has not been defined.
     *
     * @see #setPrefix
     */
    public String	getPrefix ();

    /**
     * Assigns a prefix to be used for the object's name.  This method
     * should be used with care, primarily to "patch up" elements after
     * they have been moved to a context where the correct namespace may
     * call for a different prefix.  This method does not check whether
     * the prefix is declared.  The return value of <em>getNodeName</em>
     * may change, if this prefix was not the one being used.
     *
     * <P> To assign the URI associated with this prefix, declare the
     * prefix by defining a value for the <em>"xmlns" + prefix</em>
     * attribute for this node's element or an ancestor element.  For
     * the null prefix, give a value for the <em>"xmlns"</em> attribute
     * instead.  All non-null prefixes must be declared.
     *
     * @see #getPrefix
     *
     * @param prefix null to remove any prefix, otherwise the unqualified
     *	name prefix to be be used.
     */
//XXX modify this for DOM2
    public void		setPrefix (String prefix);

}
