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

package org.apache.xerces.framework;

/**
 * ContentSpec really exists to aid the parser classes in implementing
 * access to the grammar.
 *
 * This class is used by the DTD scanner and the validator classes,
 * allowing them to be used separately or together.  This "struct"
 * class is used to build content models for validation, where it
 * is more efficient to fetch all of the information for each of
 * these content model "fragments" than to fetch each field one at
 * a time.  Since configurations are allowed to have validators
 * without a DTD scanner (i.e. a schema validator) and a DTD scanner
 * without a validator (non-validating processor), this class can be
 * used by each without requiring the presence of the other.
 *
 * When processing element declarations, the DTD scanner will build
 * up a representation of the content model using the node types that
 * are defined here.  Since a non-validating processor only needs to
 * remember the type of content model declared (i.e. ANY, EMPTY, MIXED,
 * or CHILDREN), it is free to discard the specific details of the
 * MIXED and CHILDREN content models described using this class.
 *
 * In the typical case of a validating processor reading the grammar
 * of the document from a DTD, the information about the content model
 * declared will be preserved and later "compiled" into an efficient
 * form for use during element validation.  Each content spec node
 * that is saved is assigned a unique index that is used as a handle
 * for the "value" or "otherValue" fields of other content spec nodes.
 * A leaf node has a "value" that is either an index in the string
 * pool of the element type of that leaf, or a value of -1 to indicate
 * the special "#PCDATA" leaf type used in a mixed content model.
 *
 * For a mixed content model, the content spec will be made up of
 * leaf and choice content spec nodes, with an optional "zero or more"
 * node.  For example, the mixed content declaration "(#PCDATA)" would
 * contain a single leaf node with a node value of -1.  A mixed content
 * declaration of "(#PCDATA|foo)*" would have a content spec consisting
 * of two leaf nodes, for the "#PCDATA" and "foo" choices, a choice node
 * with the "value" set to the index of the "#PCDATA" leaf node and the
 * "otherValue" set to the index of the "foo" leaf node, and a "zero or
 * more" node with the "value" set to the index of the choice node.  If
 * the content model has more choices, for example "(#PCDATA|a|b)*", then
 * there will be more corresponding choice and leaf nodes, the choice
 * nodes will be chained together through the "value" field with each
 * leaf node referenced by the "otherValue" field.
 *
 * For element content models, there are sequence nodes and also "zero or
 * one" and "one or more" nodes.  The leaf nodes would always have a valid
 * string pool index, as the "#PCDATA" leaf is not used in the declarations
 * for element content models.
 *
 * @version
 */

public interface XMLContentSpec {
    /**
     * return this ContentSpec as a string
     *
     * @return the content spec as a string
     */
    public String toString();
    /**
     * return the type of this ContentSpec
     *
     * @return The string pool handle for the type of this ContentSpec
     */
    public int getType();
    /**
     * get this ContentSpec's handle
     *
     * @return handle for this ContentSpec
     */
    public int getHandle();
    /**
     * fill in XMLContentSpec.Node node with the information for
     * ContentSpec in handle
     * 
     * @param handle ContentSpec handle
     * @param node result XMLContentSpec.Node
     */
    public void getNode(int handle, XMLContentSpec.Node node);

    //
    // node type
    //
    // Note: The #PCDATA leaf in a MIXED content spec is indicated by a type
    //       of CONTENTSPECNODE_LEAF and an value of -1.
    //
    public static final int
        CONTENTSPECNODE_LEAF           = 0,            // Name or #PCDATA (-1)
        CONTENTSPECNODE_ZERO_OR_ONE    = 1,            // '?'
        CONTENTSPECNODE_ZERO_OR_MORE   = 2,            // '*'
        CONTENTSPECNODE_ONE_OR_MORE    = 3,            // '+'
        CONTENTSPECNODE_CHOICE         = 4,            // '|'
        CONTENTSPECNODE_SEQ            = 5;            // ','

    //
    // Node record
    //
    public final class Node {
        //
        // Public instance variables - filled in on each call to getNode().
        //
        public int type;                    // node type (see above)
        public int value;                   // leaf index, single child for unary ops, left child for binary ops.
        public int otherValue;              // right child for binary ops
    }
}
