/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.v1;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.v1.XMLContentSpec;
import org.apache.xerces.impl.v1.schema.SubstitutionGroupComparator;
import org.apache.xerces.impl.v1.schema.SchemaMessageProvider;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.QName;

/**
 * ElementWildcard is used to check whether two element declarations conflict
 */
public class ElementWildcard {

    //
    // Data
    //

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;
    
    //
    // Constructors
    //

    public ElementWildcard() {}

    //
    // Public methods
    //

    public void setErrorReporter(XMLErrorReporter errorReporter) {
        fErrorReporter = errorReporter;
    }

    public boolean conflict(int type1, String local1, String uri1,
                            int type2, String local2, String uri2,
                            SubstitutionGroupComparator comparator) {
        boolean ret = conflic(type1, local1, uri1, type2, local2, uri2, comparator);

        try {
        if (ret && fErrorReporter != null) {
            String elements = getString (type1, local1, uri1, type2, local2, uri2);
            fErrorReporter.reportError(SchemaMessageProvider.SCHEMA_DOMAIN,
                                       SchemaMessageProvider.fgMessageKeys[SchemaMessageProvider.GenericError],
                                       new Object[]{ elements },
                                       XMLErrorReporter.SEVERITY_ERROR);
        }
        } catch (Exception e) {
            // REVISIT: Why is this swallowed w/o reporting an error?
        }

        return ret;
    }

    //
    // Private methods
    //

    private boolean uriInWildcard(QName qname, 
                                  String wildcard, int wtype,
                                  SubstitutionGroupComparator comparator) {
        int type = wtype & 0x0f;

        if (type == XMLContentSpec.CONTENTSPECNODE_ANY) {
            return true;
        }
        else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_NS) {
            // substitution of "uri" satisfies "wtype:wildcard"
            if (comparator != null) {
                try {
                    if (comparator.isAllowedByWildcard(qname, wildcard, false))
                        return true;
                } catch (Exception e) {
                    // error occurs in comparator, do nothing here.
                }
            } else {
                if (qname.uri == wildcard)
                    return true;
            }
        }
        else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {
            // substitution of "uri" satisfies "wtype:wildcard"
            if (comparator != null) {
                try {
                    if (comparator.isAllowedByWildcard(qname, wildcard, true))
                        return true;
                } catch (Exception e) {
                    // error occurs in comparator, do nothing here.
                }
            } else {
                if (wildcard != qname.uri)
                    return true;
            }
        }

        return false;
    }

    private boolean wildcardIntersect(String w1, int t1, String w2, int t2) {
        int type1 = t1 & 0x0f, type2 = t2 & 0x0f;

        // if either one is "##any", then intersects
        if (type1 == XMLContentSpec.CONTENTSPECNODE_ANY ||
            type2 == XMLContentSpec.CONTENTSPECNODE_ANY) {
            return true;
        }

        // if both are "some_namespace" and equal, then intersects
        if (type1 == XMLContentSpec.CONTENTSPECNODE_ANY_NS &&
            type2 == XMLContentSpec.CONTENTSPECNODE_ANY_NS &&
            w1 == w2) {
            return true;
        }

        // if both are "##other", and equal, then intersects
        if (type1 == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER &&
            type2 == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {
            return true;
        }

        // if one "##other" and one namespace, if not equal, then intersects
        if ((type1 == XMLContentSpec.CONTENTSPECNODE_ANY_NS &&
             type2 == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER ||
             type1 == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER &&
             type2 == XMLContentSpec.CONTENTSPECNODE_ANY_NS) &&
            w1 != w2) {
            return true;
        }

        return false;
    }

    // check whether two elements conflict
    private boolean conflic(int type1, String local1, String uri1,
                            int type2, String local2, String uri2,
                            SubstitutionGroupComparator comparator) {
        // REVISIT: cache these.
        QName q1 = new QName(), q2 = new QName();
        q1.localpart = local1;
        q1.uri = uri1;
        q2.localpart = local2;
        q2.uri = uri2;

        if (type1 == XMLContentSpec.CONTENTSPECNODE_LEAF &&
            type2 == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            if (comparator != null) {
                try {
                    if (comparator.isEquivalentTo(q1, q2) ||
                        comparator.isEquivalentTo(q2, q1))
                        return true;
                } catch (Exception e) {
                    // error occurs in comparator, do nothing here.
                }
            } else {
                if (q1.localpart == q2.localpart &&
                    q1.uri == q2.uri)
                    return true;
            }
        } else if (type1 == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            if (uriInWildcard(q1, uri2, type2, comparator))
                return true;
        } else if (type2 == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            if (uriInWildcard(q2, uri1, type1, comparator))
                return true;
        } else {
            if (wildcardIntersect(uri1, type1, uri2, type2))
                return true;
        }

        return false;
    }

    private String eleString(int type, String local, String uri) {
        switch (type & 0x0f) {
        case XMLContentSpec.CONTENTSPECNODE_LEAF:
            return uri+":"+local;
        case XMLContentSpec.CONTENTSPECNODE_ANY:
            return "##any:*";
        case XMLContentSpec.CONTENTSPECNODE_ANY_NS:
            return  uri+":*";
        case XMLContentSpec.CONTENTSPECNODE_ANY_OTHER:
            return "##other("+uri+"):*";
        }

        return "";
    }

    private String getString(int type1, String local1, String uri1,
                             int type2, String local2, String uri2) {
        return "cos-nonambig: (" + eleString(type1, local1, uri1) +
               ") and (" + eleString(type2, local2, uri2) +
               ") violate the \"Unique Particle Attribution\" rule";
    }

} // class ElementWildcard
