/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights
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

package org.apache.xerces.validators.common;

import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.validators.schema.SubstitutionGroupComparator;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.validators.schema.SchemaMessageProvider;

/**
 * ElementWildcard is used to check whether two element declarations conflict
 */
public class ElementWildcard {
    private ElementWildcard(){}

    private static boolean uriInWildcard(QName qname, int wildcard, int wtype,
                                         SubstitutionGroupComparator comparator) throws Exception {
        int type = wtype & 0x0f;

        if (type == XMLContentSpec.CONTENTSPECNODE_ANY) {
            return true;
        }
        else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_NS) {
            // substitution of "uri" satisfies "wtype:wildcard"
            if (comparator != null) {
                if (comparator.isAllowedByWildcard(qname, wildcard, false))
                    return true;
            } else {
                if (qname.uri == wildcard)
                    return true;
            }
        }
        else if (type == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {
            // substitution of "uri" satisfies "wtype:wildcard"
            if (comparator != null) {
                if (comparator.isAllowedByWildcard(qname, wildcard, true))
                    return true;
            } else {
                //if (wildcard != uri && uri != StringPool.EMPTY_STRING) // ???
                if (wildcard != qname.uri)
                    return true;
            }
        }

        return false;
    }

    private static boolean wildcardIntersect(int w1, int t1, int w2, int t2) {
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
    private static boolean conflic(int type1, int local1, int uri1,
                                   int type2, int local2, int uri2,
                                   SubstitutionGroupComparator comparator) throws Exception {
        QName q1 = new QName(), q2 = new QName();
        q1.localpart = local1;
        q1.uri = uri1;
        q2.localpart = local2;
        q2.uri = uri2;

        if (type1 == XMLContentSpec.CONTENTSPECNODE_LEAF &&
            type2 == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            if (comparator != null) {
                if (comparator.isEquivalentTo(q1, q2) ||
                    comparator.isEquivalentTo(q2, q1))
                    return true;
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

    public static boolean conflict(int type1, int local1, int uri1,
                                   int type2, int local2, int uri2,
                                   SubstitutionGroupComparator comparator) throws Exception {
        boolean ret = conflic(type1, local1, uri1, type2, local2, uri2, comparator);

        if (ret && comparator != null) {
            String elements = getString (type1, local1, uri1,
                                         type2, local2, uri2,
                                         comparator.getStringPool());
            XMLErrorReporter err = comparator.getErrorReporter();
            err.reportError(err.getLocator(),
                            SchemaMessageProvider.SCHEMA_DOMAIN,
                            SchemaMessageProvider.GenericError,
                            SchemaMessageProvider.MSG_NONE,
                            new Object[]{elements},
                            XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
        }

        return ret;
    }
    private static String eleString(int type, int local, int uri, StringPool stringPool) {
        switch (type & 0x0f) {
        case XMLContentSpec.CONTENTSPECNODE_LEAF:
            return stringPool.toString(uri) + ":" + stringPool.toString(local);
        case XMLContentSpec.CONTENTSPECNODE_ANY:
            return "##any:*";
        case XMLContentSpec.CONTENTSPECNODE_ANY_NS:
            return  stringPool.toString(uri) + ":*";
        case XMLContentSpec.CONTENTSPECNODE_ANY_OTHER:
            return "##other(" + stringPool.toString(uri) + "):*";
        }

        return "";
    }

    private static String getString(int type1, int local1, int uri1,
                                    int type2, int local2, int uri2,
                                    StringPool stringPool) {
        return "cos-nonambig: (" + eleString(type1, local1, uri1, stringPool) +
               ") and (" + eleString(type2, local2, uri2, stringPool) +
               ") violate the \"Unique Particle Attribution\" rule";
    }
} // class ElementWildcard
