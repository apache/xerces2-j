/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.xs;

import org.apache.xerces.xni.QName;
import java.util.Vector;


/**
 * The XML representation for a wildcard declaration
 * schema component is an <any> or <anyAttribute> element information item
 *
 * @author Sandy Gao, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class XSWildcardDecl  {

    //REVISIT: Should this be here. Is this required at all.
    public static final String ABSENT = null;

    // types of wildcard
    // namespace="##any"
    public static final short WILDCARD_ANY   = 0;
    // namespace="##other"
    public static final short WILDCARD_OTHER = 1;
    // namespace= (list of (anyURI | ##targetNamespace | ##local))
    public static final short WILDCARD_LIST  = 2;

    // types of process contents
    // processContents="strict"
    public static final short WILDCARD_STRICT = 0;
    // processContents="lax"
    public static final short WILDCARD_LAX    = 1;
    // processContents="skip"
    public static final short WILDCARD_SKIP   = 2;

    // the type of wildcard: any, other, or list
    public short fType = WILDCARD_ANY;
    // the type of process contents: strict, lax, or skip
    public short fProcessContents = WILDCARD_STRICT;
    // the namespace list:
    // for WILDCARD_LIST, it means one of the namespaces in the list
    // for WILDCARD_OTHER, it means not any of the namespaces in the list
    public String[] fNamespaceList;

    // I'm trying to implement the following constraint exactly as what the
    // spec describes. Sometimes it seems redundant, and sometimes there seems
    // to be much easier solutions. But it makes it easy to understand,
    // easy to maintain, and easy to find a bug (either in the code, or in the
    // spec). -SG
    //
    // NOTE: Schema spec only requires that ##other not(tNS,absent).
    //       The way we store ##other is not(NS1,NS2,...,NSN), which covers
    //       what's required by Schema, and allows future enhanced features.
    //
    // In the following in-line comments:
    // - Bullet removed from w3c specification.
    // + Bullet added as proposed by Sandy Gao, IBM.
    // / Since we store ##other as not(NS1,NS2,...,NSN), we need to put some
    //   comments on where we didn't follow the spec exactly.
    // * When we really support not(NS1,NS2,...,NSN), we need to revisit these items.

    /**
     * Validation Rule: Wildcard allows Namespace Name
     */
    public boolean allowNamespace(String namespace) {
        // For a value which is either a namespace name or absent to be valid with respect to a wildcard constraint (the value of a {namespace constraint}) one of the following must be true:

        // 1 The constraint must be any.
        if (fType == WILDCARD_ANY)
            return true;

        // 2 All of the following must be true:
        // 2.1 The constraint is a pair of not and a namespace name or absent ([Definition:]  call this the namespace test).
        // 2.2 The value must not be identical to the namespace test.
        // 2.3 The value must not be absent.
        // / we store ##other as not(list), so our actual rule is
        // / 2 The constraint is a pair of not and a set, and the value is not in such set.
        if (fType == WILDCARD_OTHER) {
            boolean found = false;
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum && !found; i++) {
                if (namespace == fNamespaceList[i])
                    found = true;
            }

            if (!found)
                return true;
        }

        // 3 The constraint is a set, and the value is identical to one of the members of the set.
        if (fType == WILDCARD_LIST) {
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum; i++) {
                if (namespace == fNamespaceList[i])
                    return true;
            }
        }

        // none of the above conditions applied, so return false.
        return false;
    }

    /**
     *  Schema Component Constraint: Wildcard Subset
     */
    public boolean isSubsetOf(XSWildcardDecl superWildcard) {
        // if the super is null (not expressible), return false
        if (superWildcard == null)
            return false;

        // For a namespace constraint (call it sub) to be an intensional subset of another
        // namespace constraint (call it super) one of the following must be true:

        // 1 super must be any.
        if (superWildcard.fType == WILDCARD_ANY) {
            return true;
        }

        // 2 All of the following must be true:
        //   2.1 sub must be a pair of not and a namespace name or absent.
        //   2.2 super must be a pair of not and the same value.
        //   * we can't just compare whether the namespace are the same value
        //     since we store other as not(list)
        if (fType == WILDCARD_OTHER) {
            if (superWildcard.fType == WILDCARD_OTHER &&
                fNamespaceList[0] == superWildcard.fNamespaceList[0]) {
                return true;
            }
        }

        // 3 All of the following must be true:
        //   3.1 sub must be a set whose members are either namespace names or absent.
        //   3.2 One of the following must be true:
        //       3.2.1 super must be the same set or a superset thereof.
        //       -3.2.2 super must be a pair of not and a namespace name or absent and
        //              that value must not be in sub's set.
        //       +3.2.2 super must be a pair of not and a namespace name or absent and
        //              either that value or absent must not be in sub's set.
        //       * since we store ##other as not(list), we acturally need to make sure
        //         that none of the namespaces in super.list is in sub.list.
        if (fType == WILDCARD_LIST) {
            if (superWildcard.fType == WILDCARD_LIST &&
                subset2sets(fNamespaceList, superWildcard.fNamespaceList)) {
                return true;
            }

            if (superWildcard.fType == WILDCARD_OTHER &&
                !elementInSet(superWildcard.fNamespaceList[0], fNamespaceList) &&
                !elementInSet(ABSENT, fNamespaceList)) {
                return true;
            }
        }

        // none of the above conditions applied, so return false.
        return false;

    } // isSubsetOf

    /**
     * Schema Component Constraint: Attribute Wildcard Union
     */
    public XSWildcardDecl performUnionWith(XSWildcardDecl wildcard,
                                           short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (wildcard == null)
            return null;

        // For a wildcard's {namespace constraint} value to be the intensional union of two
        // other such values (call them O1 and O2): the appropriate case among the following
        // must be true:

        XSWildcardDecl unionWildcard = new XSWildcardDecl();
        unionWildcard.fProcessContents = processContents;

        // 1 If O1 and O2 are the same value, then that value must be the value.
        if (areSame(wildcard)) {
            unionWildcard.fType = fType;
            unionWildcard.fNamespaceList = fNamespaceList;
        }

        // 2 If either O1 or O2 is any, then any must be the value.
        else if ( (fType == WILDCARD_ANY) || (wildcard.fType == WILDCARD_ANY) ) {
            unionWildcard.fType = WILDCARD_ANY;
        }

        // 3 If both O1 and O2 are sets of (namespace names or absent), then the union of
        //   those sets must be the value.
        else if ( (fType == WILDCARD_LIST) && (wildcard.fType == WILDCARD_LIST) ) {
            unionWildcard.fType = WILDCARD_LIST;
            unionWildcard.fNamespaceList = union2sets(fNamespaceList, wildcard.fNamespaceList);
        }

        // -4 If the two are negations of different namespace names, then the intersection
        //    is not expressible.
        // +4 If the two are negations of different namespace names or absent, then
        //    a pair of not and absent must be the value.
        // * now we store ##other as not(list), the result should be
        //   not(intersection of two lists).
        else if (fType == WILDCARD_OTHER && wildcard.fType == WILDCARD_OTHER) {
            unionWildcard.fType = WILDCARD_OTHER;
            unionWildcard.fNamespaceList = new String[2];
            unionWildcard.fNamespaceList[0] = ABSENT;
            unionWildcard.fNamespaceList[1] = ABSENT;
        }

        // 5 If either O1 or O2 is a pair of not and a namespace name and the other is a set of
        //   (namespace names or absent), then The appropriate case among the following must be true:
        //      -5.1 If the set includes the negated namespace name, then any must be the value.
        //      -5.2 If the set does not include the negated namespace name, then whichever of O1 or O2
        //           is a pair of not and a namespace name must be the value.
        //    +5.1 If the negated value is a namespace name, then The appropriate case
        //         among the following must be true:
        //        +5.1.1 If the set includes both the namespace name and absent, then any
        //               must be the value.
        //        +5.1.2 If the set includes the namespace name but does not include
        //               absent, then a pair of not and absent must be the value.
        //        +5.1.3 If the set does not include the namespace name but includes
        //               absent, then the union is not expressible.
        //        +5.1.4 If the set does not include either the namespace name or absent,
        //               then whichever of O1 or O2 is a pair of not and a namespace name must be
        //               the value.
        //    +5.2 If the negated value is absent, then The appropriate case among the
        //         following must be true:
        //        +5.2.1 If the set includes absent, then any must be the value.
        //        +5.2.2 If the set does not include absent, then whichever of O1 or O2 is
        //               a pair of not and a namespace name must be the value.
        // * when we have not(list), the operation is just not(otherlist-list)
        else if ( ((fType == WILDCARD_OTHER) && (wildcard.fType == WILDCARD_LIST)) ||
                  ((fType == WILDCARD_LIST) && (wildcard.fType == WILDCARD_OTHER)) ) {
            String[] other = null;
            String[] list = null;

            if (fType == WILDCARD_OTHER) {
                other = fNamespaceList;
                list = wildcard.fNamespaceList;
            }
            else {
                other = wildcard.fNamespaceList;
                list = fNamespaceList;
            }

            boolean foundAbsent = elementInSet(ABSENT, list);

            if (other[0] != ABSENT) {
                boolean foundNS = elementInSet(other[0], list);
                if (foundNS && foundAbsent) {
                    unionWildcard.fType = WILDCARD_ANY;
                } else if (foundNS && !foundAbsent) {
                    unionWildcard.fType = WILDCARD_OTHER;
                    unionWildcard.fNamespaceList = new String[2];
                    unionWildcard.fNamespaceList[0] = ABSENT;
                    unionWildcard.fNamespaceList[1] = ABSENT;
                } else if (!foundNS && foundAbsent) {
                    return null;
                } else { // !foundNS && !foundAbsent
                    unionWildcard.fType = WILDCARD_OTHER;
                    unionWildcard.fNamespaceList = other;
                }
            } else { // other[0] == ABSENT
                if (foundAbsent) {
                    unionWildcard.fType = WILDCARD_ANY;
                } else { // !foundAbsent
                    unionWildcard.fType = WILDCARD_OTHER;
                    unionWildcard.fNamespaceList = other;
                }
            }
        }

        return unionWildcard;

    } // performUnionWith

    /**
     * Schema Component Constraint: Attribute Wildcard Intersection
     */
    public XSWildcardDecl performIntersectionWith(XSWildcardDecl wildcard,
                                                  short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (wildcard == null)
            return null;

        // For a wildcard's {namespace constraint} value to be the intensional intersection of
        // two other such values (call them O1 and O2): the appropriate case among the following
        // must be true:

        XSWildcardDecl intersectWildcard = new XSWildcardDecl();
        intersectWildcard.fProcessContents = processContents;

        // 1 If O1 and O2 are the same value, then that value must be the value.
        if (areSame(wildcard)) {
            intersectWildcard.fType = fType;
            intersectWildcard.fNamespaceList = fNamespaceList;
        }

        // 2 If either O1 or O2 is any, then the other must be the value.
        else if ( (fType == WILDCARD_ANY) || (wildcard.fType == WILDCARD_ANY) ) {
            // both cannot be ANY, if we have reached here.
            XSWildcardDecl other = this;

            if (fType == WILDCARD_ANY)
                other = wildcard;

            intersectWildcard.fType = other.fType;
            intersectWildcard.fNamespaceList = other.fNamespaceList;
        }

        // -3 If either O1 or O2 is a pair of not and a namespace name and the other is a set of
        //    (namespace names or absent), then that set, minus the negated namespace name if
        //    it was in the set, must be the value.
        // +3 If either O1 or O2 is a pair of not and a namespace name and the other
        //    is a set of (namespace names or absent), then that set, minus the negated
        //    namespace name if it was in the set, then minus absent if it was in the
        //    set, must be the value.
        // * when we have not(list), the operation is just list-otherlist
        else if ( ((fType == WILDCARD_OTHER) && (wildcard.fType == WILDCARD_LIST)) ||
                  ((fType == WILDCARD_LIST) && (wildcard.fType == WILDCARD_OTHER)) ) {
            String[] list = null;
            String[] other = null;

            if (fType == WILDCARD_OTHER) {
                other = fNamespaceList;
                list = wildcard.fNamespaceList;
            }
            else {
                other = wildcard.fNamespaceList;
                list = fNamespaceList;
            }

            int listSize = list.length;
            String[] intersect = new String[listSize];
            int newSize = 0;
            for (int i = 0; i < listSize; i++) {
                if (list[i] != other[0] && list[i] != ABSENT)
                    intersect[newSize++] = list[i];
            }

            intersectWildcard.fType = WILDCARD_LIST;
            intersectWildcard.fNamespaceList = new String[newSize];
            System.arraycopy(intersect, 0, intersectWildcard.fNamespaceList, 0, newSize);
        }

        // 4 If both O1 and O2 are sets of (namespace names or absent), then the intersection of those
        //   sets must be the value.
        else if ( (fType == WILDCARD_LIST) && (wildcard.fType == WILDCARD_LIST) ) {
            intersectWildcard.fType = WILDCARD_LIST;
            intersectWildcard.fNamespaceList = intersect2sets(fNamespaceList, wildcard.fNamespaceList);
        }

        // -5 If the two are negations of different namespace names, then the intersection is not expressible.
        // +5 If the two are negations of namespace names or absent, then The
        //    appropriate case among the following must be true:
        //    +5.1 If the two are negations of different namespace names, then the
        //         intersection is not expressible.
        //    +5.2 If one of the two is a pair of not and absent, the other must be
        //         the value.
        // * when we have not(list), the operation is just not(onelist+otherlist)
        else if (fType == WILDCARD_OTHER && wildcard.fType == WILDCARD_OTHER) {
            if (fNamespaceList[0] != ABSENT && wildcard.fNamespaceList[0] != ABSENT)
                return null;

            XSWildcardDecl other = this;
            if (fNamespaceList[0] == ABSENT)
                other = wildcard;

            intersectWildcard.fType = other.fType;
            intersectWildcard.fNamespaceList = other.fNamespaceList;
        }

        return intersectWildcard;

    } // performIntersectionWith

    private boolean areSame(XSWildcardDecl wildcard) {
        if (fType == wildcard.fType) {
            // ##any, true
            if (fType == WILDCARD_ANY)
                return true;

            // ##other, only check the negated value
            // * when we support not(list), we need to check in the same way
            //   as for WILDCARD_LIST.
            if (fType == WILDCARD_OTHER)
                return fNamespaceList[0] == wildcard.fNamespaceList[0];

            // ## list, must have the same length,
            // and each item in one list must appear in the other one
            // (we are assuming that there are no duplicate items in a list)
            if (fNamespaceList.length == wildcard.fNamespaceList.length) {
                for (int i=0; i<fNamespaceList.length; i++) {
                    if (!elementInSet(fNamespaceList[i], wildcard.fNamespaceList))
                        return false;
                }
                return true;
            }
        }

        return false;
    } // areSame

    String[] intersect2sets(String[] one, String[] theOther){
        String[] result = new String[Math.min(one.length,theOther.length)];

        // simple implemention,
        int count = 0;
        for (int i=0; i<one.length; i++) {
            if (elementInSet(one[i], theOther))
                result[count++] = one[i];
        }

        String[] result2 = new String[count];
        System.arraycopy(result, 0, result2, 0, count);

        return result2;
    }

    String[] union2sets(String[] one, String[] theOther){
        String[] result1 = new String[one.length];

        // simple implemention,
        int count = 0;
        for (int i=0; i<one.length; i++) {
            if (!elementInSet(one[i], theOther))
                result1[count++] = one[i];
        }

        String[] result2 = new String[count+theOther.length];
        System.arraycopy(result1, 0, result2, 0, count);
        System.arraycopy(theOther, 0, result2, count, theOther.length);

        return result2;
    }

    boolean subset2sets(String[] subSet, String[] superSet){
        for (int i=0; i<subSet.length; i++) {
            if (!elementInSet(subSet[i], superSet))
                return false;
        }

        return true;
    }

    boolean elementInSet(String ele, String[] set){
        boolean found = false;
        for (int i=0; i<set.length && !found; i++) {
            if (ele==set[i])
                found = true;
        }

        return found;
    }

    /**
     * get the string description of this wildcard
     */
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("WC[");
            switch (fType) {
            case WILDCARD_ANY:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                break;
            case WILDCARD_OTHER:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDOTHER);
                buffer.append(":\"");
                if (fNamespaceList[0] != null)
                    buffer.append(fNamespaceList[0]);
                buffer.append("\"");
                break;
            case WILDCARD_LIST:
                buffer.append("\"");
                if (fNamespaceList[0] != null)
                    buffer.append(fNamespaceList[0]);
                buffer.append("\"");
                for (int i = 1; i < fNamespaceList.length; i++) {
                    buffer.append(",\"");
                    if (fNamespaceList[i] != null)
                        buffer.append(fNamespaceList[i]);
                    buffer.append("\"");
                }
                break;
            }
            buffer.append("]");
            fDescription = buffer.toString();
        }

        return fDescription;
    }
    
} // class XSWildcardDecl
