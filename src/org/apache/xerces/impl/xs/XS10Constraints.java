/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.util.SymbolHash;

/**
 * XML Schema 1.0 constraints
 *
 * @xerces.internal
 *
 * @author Sandy Gao, IBM
 * @author Khaled Noaman, IBM
 *
 * @version $Id$
 *
 */
class XS10Constraints extends XSConstraints {

    // accomodate 1.0 extended
    private short fSchemaVersion;

    public XS10Constraints(short schemaVersion) {
        super(SchemaGrammar.getXSAnyType(schemaVersion));
        fSchemaVersion = schemaVersion;
    }

    public boolean overlapUPA(XSElementDecl element,
            XSWildcardDecl wildcard,
            SubstitutionGroupHandler sgHandler) {
        // if the wildcard allows the element
        if (wildcard.allowNamespace(element.fTargetNamespace))
            return true;

        // or if the wildcard allows any element in the substitution group
        XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(element, fSchemaVersion);
        for (int i = subGroup.length-1; i >= 0; i--) {
            if (wildcard.allowNamespace(subGroup[i].fTargetNamespace))
                return true;
        }

        return false;
    }

    public boolean overlapUPA(XSWildcardDecl wildcard1,
            XSWildcardDecl wildcard2) {
        // if the intersection of the two wildcard is not empty list
        XSWildcardDecl intersect = performIntersectionWith(wildcard1, wildcard2, wildcard1.fProcessContents);
        if (intersect == null ||
                intersect.fType != XSWildcardDecl.NSCONSTRAINT_LIST ||
                intersect.fNamespaceList.length != 0) {
            return true;
        }

        return false;
    }
    
    public boolean overlapUPA(XSElementDecl element1,
            XSElementDecl element2,
            SubstitutionGroupHandler sgHandler) {
        return overlapUPA(element1, element2, sgHandler, fSchemaVersion);
    }
    
    public void checkElementDeclsConsistent(XSComplexTypeDecl type,
            XSParticleDecl particle,
            SymbolHash elemDeclHash,
            SubstitutionGroupHandler sgHandler) throws XMLSchemaException {
        checkElementDeclsConsistent(type, particle, elemDeclHash, sgHandler, fSchemaVersion);
    }
    
    public void fullSchemaChecking(XSGrammarBucket grammarBucket,
            SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder,
            XMLErrorReporter errorReporter) {
        fullSchemaChecking(grammarBucket, SGHandler, cmBuilder, errorReporter, fSchemaVersion);
    }

    /**
     *  Schema Component Constraint: Wildcard Subset
     *  
     *  wildcard.isSubsetOf(superWildcard)
     */
    public boolean isSubsetOf(XSWildcardDecl wildcard, XSWildcardDecl superWildcard) {
        // if the super is null (not expressible), return false
        if (superWildcard == null) {
            return false;
        }

        // For a namespace constraint (call it sub) to be an intensional subset of another
        // namespace constraint (call it super) one of the following must be true:

        // 1 super must be any.
        if (superWildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
            return true;
        }

        // 2 All of the following must be true:
        //   2.1 sub must be a pair of not and a namespace name or absent.
        //   2.2 super must be a pair of not and the same value.
        //   * we can't just compare whether the namespace are the same value
        //     since we store other as not(list)
        if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            if (superWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT &&
                wildcard.fNamespaceList[0] == superWildcard.fNamespaceList[0]) {
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
        if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
            if (superWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST &&
                subset2sets(wildcard.fNamespaceList, superWildcard.fNamespaceList)) {
                return true;
            }

            if (superWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT &&
                !elementInSet(superWildcard.fNamespaceList[0], wildcard.fNamespaceList) &&
                !elementInSet(XSWildcardDecl.ABSENT, wildcard.fNamespaceList)) {
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
                                           XSWildcardDecl otherWildcard,
                                           short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (otherWildcard == null)
            return null;

        // For a wildcard's {namespace constraint} value to be the intensional union of two
        // other such values (call them O1 and O2): the appropriate case among the following
        // must be true:

        XSWildcardDecl unionWildcard = new XSWildcardDecl();
        unionWildcard.fProcessContents = processContents;

        // 1 If O1 and O2 are the same value, then that value must be the value.
        if (areSame(wildcard, otherWildcard)) {
            unionWildcard.fType = wildcard.fType;
            unionWildcard.fNamespaceList = wildcard.fNamespaceList;
        }

        // 2 If either O1 or O2 is any, then any must be the value.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) || (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) ) {
            unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
        }

        // 3 If both O1 and O2 are sets of (namespace names or absent), then the union of
        //   those sets must be the value.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) ) {
            unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            unionWildcard.fNamespaceList = union2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }

        // -4 If the two are negations of different namespace names, then the intersection
        //    is not expressible.
        // +4 If the two are negations of different namespace names or absent, then
        //    a pair of not and absent must be the value.
        // * now we store ##other as not(list), the result should be
        //   not(intersection of two lists).
        else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT && otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
            unionWildcard.fNamespaceList = new String[2];
            unionWildcard.fNamespaceList[0] = XSWildcardDecl.ABSENT;
            unionWildcard.fNamespaceList[1] = XSWildcardDecl.ABSENT;
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
        else if ( ((wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST)) ||
                  ((wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT)) ) {
            String[] local = null;
            String[] list = null;

            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                local = wildcard.fNamespaceList;
                list = otherWildcard.fNamespaceList;
            }
            else {
                local = otherWildcard.fNamespaceList;
                list = wildcard.fNamespaceList;
            }

            boolean foundAbsent = elementInSet(XSWildcardDecl.ABSENT, list);

            if (local[0] != XSWildcardDecl.ABSENT) {
                boolean foundNS = elementInSet(local[0], list);
                if (foundNS && foundAbsent) {
                    unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
                } else if (foundNS && !foundAbsent) {
                    unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = new String[2];
                    unionWildcard.fNamespaceList[0] = XSWildcardDecl.ABSENT;
                    unionWildcard.fNamespaceList[1] = XSWildcardDecl.ABSENT;
                } else if (!foundNS && foundAbsent) {
                    return null;
                } else { // !foundNS && !foundAbsent
                    unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = local;
                }
            } else { // other[0] == ABSENT
                if (foundAbsent) {
                    unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
                } else { // !foundAbsent
                    unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
                    unionWildcard.fNamespaceList = local;
                }
            }
        }

        return unionWildcard;

    } // performUnionWith

    /**
     * Schema Component Constraint: Attribute Wildcard Intersection
     */
    public XSWildcardDecl performIntersectionWith(XSWildcardDecl wildcard,
                                                  XSWildcardDecl otherWildcard,
                                                  short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (otherWildcard == null)
            return null;

        // For a wildcard's {namespace constraint} value to be the intensional intersection of
        // two other such values (call them O1 and O2): the appropriate case among the following
        // must be true:

        XSWildcardDecl intersectWildcard = new XSWildcardDecl();
        intersectWildcard.fProcessContents = processContents;

        // 1 If O1 and O2 are the same value, then that value must be the value.
        if (areSame(wildcard, otherWildcard)) {
            intersectWildcard.fType = wildcard.fType;
            intersectWildcard.fNamespaceList = wildcard.fNamespaceList;
        }

        // 2 If either O1 or O2 is any, then the other must be the value.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) || (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) ) {
            // both cannot be ANY, if we have reached here.
            XSWildcardDecl other = wildcard;

            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY)
                other = otherWildcard;

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
        else if ( ((wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST)) ||
                  ((wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT)) ) {
            String[] list = null;
            String[] other = null;

            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                other = wildcard.fNamespaceList;
                list = otherWildcard.fNamespaceList;
            }
            else {
                other = otherWildcard.fNamespaceList;
                list = wildcard.fNamespaceList;
            }

            int listSize = list.length;
            String[] intersect = new String[listSize];
            int newSize = 0;
            for (int i = 0; i < listSize; i++) {
                if (list[i] != other[0] && list[i] != XSWildcardDecl.ABSENT)
                    intersect[newSize++] = list[i];
            }

            intersectWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = new String[newSize];
            System.arraycopy(intersect, 0, intersectWildcard.fNamespaceList, 0, newSize);
        }

        // 4 If both O1 and O2 are sets of (namespace names or absent), then the intersection of those
        //   sets must be the value.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) ) {
            intersectWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = intersect2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }

        // -5 If the two are negations of different namespace names, then the intersection is not expressible.
        // +5 If the two are negations of namespace names or absent, then The
        //    appropriate case among the following must be true:
        //    +5.1 If the two are negations of different namespace names, then the
        //         intersection is not expressible.
        //    +5.2 If one of the two is a pair of not and absent, the other must be
        //         the value.
        // * when we have not(list), the operation is just not(onelist+otherlist)
        else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT && otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            if (wildcard.fNamespaceList[0] != XSWildcardDecl.ABSENT && otherWildcard.fNamespaceList[0] != XSWildcardDecl.ABSENT)
                return null;

            XSWildcardDecl local = wildcard;
            if (wildcard.fNamespaceList[0] == XSWildcardDecl.ABSENT)
                local = otherWildcard;

            intersectWildcard.fType = local.fType;
            intersectWildcard.fNamespaceList = local.fNamespaceList;
        }

        return intersectWildcard;

    } // performIntersectionWith
}
