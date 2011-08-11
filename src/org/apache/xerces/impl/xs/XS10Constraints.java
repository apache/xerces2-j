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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;

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

    public XS10Constraints(short schemaVersion) {
        super(SchemaGrammar.getXSAnyType(schemaVersion), schemaVersion);
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

    protected void groupSubsumption(XSParticleDecl dParticle, XSParticleDecl bParticle,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, String dName,
            SimpleLocator locator) {
        try {
            particleValidRestriction(dParticle, SGHandler, bParticle, SGHandler);
        } catch (XMLSchemaException e) {
            String key = e.getKey();
            reportSchemaError(errorReporter, locator,
                    key,
                    e.getArgs());
            reportSchemaError(errorReporter, locator,
                    "src-redefine.6.2.2",
                    new Object[]{dName, key});
        }
    }
    
    protected void typeSubsumption(XSComplexTypeDecl dType, XSComplexTypeDecl bType,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, SimpleLocator locator) {
        try {
            particleValidRestriction(dType.fParticle, SGHandler, bType.fParticle, SGHandler);
        } catch (XMLSchemaException e) {
            reportSchemaError(errorReporter, locator,
                    e.getKey(),
                    e.getArgs());
            reportSchemaError(errorReporter, locator,
                    "derivation-ok-restriction.5.4.2",
                    new Object[]{dType.fName});
        }
    }
    
    private static final Comparator ELEMENT_PARTICLE_COMPARATOR = new Comparator() {

        public int compare(Object o1, Object o2) {
            XSParticleDecl pDecl1 = (XSParticleDecl) o1;
            XSParticleDecl pDecl2 = (XSParticleDecl) o2;
            XSElementDecl decl1 = (XSElementDecl) pDecl1.fValue;
            XSElementDecl decl2 = (XSElementDecl) pDecl2.fValue;

            String namespace1 = decl1.getNamespace();
            String namespace2 = decl2.getNamespace();
            String name1 = decl1.getName();
            String name2 = decl2.getName();

            boolean sameNamespace = (namespace1 == namespace2);
            int namespaceComparison = 0;

            if (!sameNamespace) {
                if (namespace1 != null) {
                    if (namespace2 != null){
                        namespaceComparison = namespace1.compareTo(namespace2);
                    }
                    else {
                        namespaceComparison = 1;
                    }
                }
                else {
                    namespaceComparison = -1;
                }
            }
            //This assumes that the names are never null.
            return namespaceComparison != 0 ? namespaceComparison : name1.compareTo(name2);
        }
    };

    // Check that a given particle is a valid restriction of a base particle.
    // 
    // IHR: 2006/11/17
    // Returns a boolean indicating if there has been expansion of substitution group
    // in the bParticle.
    // With this information the checkRecurseLax function knows when is
    // to keep the order and when to ignore it.
    private boolean particleValidRestriction(XSParticleDecl dParticle,
            SubstitutionGroupHandler dSGHandler,
            XSParticleDecl bParticle,
            SubstitutionGroupHandler bSGHandler)
        throws XMLSchemaException {
        return particleValidRestriction(dParticle, dSGHandler, bParticle, bSGHandler, true);
    }

    private boolean particleValidRestriction(XSParticleDecl dParticle,
            SubstitutionGroupHandler dSGHandler,
            XSParticleDecl bParticle,
            SubstitutionGroupHandler bSGHandler,
            boolean checkWCOccurrence)
        throws XMLSchemaException {

        Vector dChildren = null;
        Vector bChildren = null;
        int dMinEffectiveTotalRange=OCCURRENCE_UNKNOWN;
        int dMaxEffectiveTotalRange=OCCURRENCE_UNKNOWN;

        // By default there has been no expansion
        boolean bExpansionHappened = false;

        // Check for empty particles.   If either base or derived particle is empty,
        // (and the other isn't) it's an error.
        if (dParticle.isEmpty() && !bParticle.emptiable()) {
            throw new XMLSchemaException("cos-particle-restrict.a", null);
        }
        else if (!dParticle.isEmpty() && bParticle.isEmpty()) {
            throw new XMLSchemaException("cos-particle-restrict.b", null);
        }

        //
        // Do setup prior to invoking the Particle (Restriction) cases.
        // This involves:
        //   - removing pointless occurrences for groups, and retrieving a vector of
        //     non-pointless children
        //   - turning top-level elements with substitution groups into CHOICE groups.
        //

        short dType = dParticle.fType;
        //
        // Handle pointless groups for the derived particle
        //
        if (dType == XSParticleDecl.PARTICLE_MODELGROUP) {
            dType = ((XSModelGroupImpl)dParticle.fValue).fCompositor;

            // Find a group, starting with this particle, with more than 1 child.   There
            // may be none, and the particle of interest trivially becomes an element or
            // wildcard.
            XSParticleDecl dtmp = getNonUnaryGroup(dParticle);
            if (dtmp != dParticle) {
                // Particle has been replaced.   Retrieve new type info.
                dParticle = dtmp;
                dType = dParticle.fType;
                if (dType == XSParticleDecl.PARTICLE_MODELGROUP)
                    dType = ((XSModelGroupImpl)dParticle.fValue).fCompositor;
            }

            // Fill in a vector with the children of the particle, removing any
            // pointless model groups in the process.
            dChildren = removePointlessChildren(dParticle);
        }

        int dMinOccurs = dParticle.fMinOccurs;
        int dMaxOccurs = dParticle.fMaxOccurs;

        //
        // For elements which are the heads of substitution groups, treat as CHOICE
        //
        if (dSGHandler != null && dType == XSParticleDecl.PARTICLE_ELEMENT) {
            XSElementDecl dElement = (XSElementDecl)dParticle.fValue;

            if (dElement.fScope == XSConstants.SCOPE_GLOBAL) {
                // Check for subsitution groups.   Treat any element that has a
                // subsitution group as a choice.   Fill in the children vector with the
                // members of the substitution group
                XSElementDecl[] subGroup = dSGHandler.getSubstitutionGroup(dElement, fSchemaVersion);
                if (subGroup.length >0 ) {
                    // Now, set the type to be CHOICE.  The "group" will have the same
                    // occurrence information as the original particle.
                    dType = XSModelGroupImpl.MODELGROUP_CHOICE;
                    dMinEffectiveTotalRange = dMinOccurs;
                    dMaxEffectiveTotalRange = dMaxOccurs;

                    // Fill in the vector of children
                    dChildren = new Vector(subGroup.length+1);
                    for (int i = 0; i < subGroup.length; i++) {
                        addElementToParticleVector(dChildren, subGroup[i]);
                    }
                    addElementToParticleVector(dChildren, dElement);
                    Collections.sort(dChildren, ELEMENT_PARTICLE_COMPARATOR);

                    // Set the handler to null, to indicate that we've finished handling
                    // substitution groups for this particle.
                    dSGHandler = null;
                }
            }
        }

        short bType = bParticle.fType;
        //
        // Handle pointless groups for the base particle
        //
        if (bType == XSParticleDecl.PARTICLE_MODELGROUP) {
            bType = ((XSModelGroupImpl)bParticle.fValue).fCompositor;

            // Find a group, starting with this particle, with more than 1 child.   There
            // may be none, and the particle of interest trivially becomes an element or
            // wildcard.
            XSParticleDecl btmp = getNonUnaryGroup(bParticle);
            if (btmp != bParticle) {
                // Particle has been replaced.   Retrieve new type info.
                bParticle = btmp;
                bType = bParticle.fType;
                if (bType == XSParticleDecl.PARTICLE_MODELGROUP)
                    bType = ((XSModelGroupImpl)bParticle.fValue).fCompositor;
            }

            // Fill in a vector with the children of the particle, removing any
            // pointless model groups in the process.
            bChildren = removePointlessChildren(bParticle);
        }

        int bMinOccurs = bParticle.fMinOccurs;
        int bMaxOccurs = bParticle.fMaxOccurs;

        if (bSGHandler != null && bType == XSParticleDecl.PARTICLE_ELEMENT) {
            XSElementDecl bElement = (XSElementDecl)bParticle.fValue;

            if (bElement.fScope == XSConstants.SCOPE_GLOBAL) {
                // Check for subsitution groups.   Treat any element that has a
                // subsitution group as a choice.   Fill in the children vector with the
                // members of the substitution group
                XSElementDecl[] bsubGroup = bSGHandler.getSubstitutionGroup(bElement, fSchemaVersion);
                if (bsubGroup.length >0 ) {
                    // Now, set the type to be CHOICE
                    bType = XSModelGroupImpl.MODELGROUP_CHOICE;

                    bChildren = new Vector(bsubGroup.length+1);
                    for (int i = 0; i < bsubGroup.length; i++) {
                        addElementToParticleVector(bChildren, bsubGroup[i]);
                    }
                    addElementToParticleVector(bChildren, bElement);
                    Collections.sort(bChildren, ELEMENT_PARTICLE_COMPARATOR);
                    // Set the handler to null, to indicate that we've finished handling
                    // substitution groups for this particle.
                    bSGHandler = null;

                    // if we are here expansion of bParticle happened
                    bExpansionHappened = true;
                }
            }
        }

        //
        // O.K. - Figure out which particle derivation rule applies and call it
        //
        switch (dType) {
            case XSParticleDecl.PARTICLE_ELEMENT:
            {
                switch (bType) {

                    // Elt:Elt NameAndTypeOK
                    case XSParticleDecl.PARTICLE_ELEMENT:
                    {
                        checkNameAndTypeOK((XSElementDecl)dParticle.fValue,dMinOccurs,dMaxOccurs,
                                (XSElementDecl)bParticle.fValue,bMinOccurs,bMaxOccurs);
                        return bExpansionHappened;
                    }

                    // Elt:Any NSCompat
                    case XSParticleDecl.PARTICLE_WILDCARD:
                    {
                        checkNSCompat((XSElementDecl)dParticle.fValue,dMinOccurs,dMaxOccurs,
                                (XSWildcardDecl)bParticle.fValue,bMinOccurs,bMaxOccurs,
                                checkWCOccurrence);
                        return bExpansionHappened;
                    }

                    // Elt:All RecurseAsIfGroup
                    case XSModelGroupImpl.MODELGROUP_CHOICE:
                    {
                        // Treat the element as if it were in a group of the same type
                        // as the base Particle
                        dChildren = new Vector();
                        dChildren.addElement(dParticle);

                        checkRecurseLax(dChildren, 1, 1, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }
                    case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                    case XSModelGroupImpl.MODELGROUP_ALL:
                    {
                        // Treat the element as if it were in a group of the same type
                        // as the base Particle
                        dChildren = new Vector();
                        dChildren.addElement(dParticle);

                        checkRecurse(dChildren, 1, 1, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    default:
                    {
                        throw new XMLSchemaException("Internal-Error",
                                new Object[]{"in particleValidRestriction"});
                    }
                }
            }

            case XSParticleDecl.PARTICLE_WILDCARD:
            {
                switch (bType) {

                    // Any:Any NSSubset
                    case XSParticleDecl.PARTICLE_WILDCARD:
                    {
                        checkNSSubset((XSWildcardDecl)dParticle.fValue, dMinOccurs, dMaxOccurs,
                                (XSWildcardDecl)bParticle.fValue, bMinOccurs, bMaxOccurs);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_CHOICE:
                    case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                    case XSModelGroupImpl.MODELGROUP_ALL:
                    case XSParticleDecl.PARTICLE_ELEMENT:
                    {
                        throw new XMLSchemaException("cos-particle-restrict.2",
                                new Object[]{"any:choice,sequence,all,elt"});
                    }

                    default:
                    {
                        throw new XMLSchemaException("Internal-Error",
                                new Object[]{"in particleValidRestriction"});
                    }
                }
            }

            case XSModelGroupImpl.MODELGROUP_ALL:
            {
                switch (bType) {

                    // All:Any NSRecurseCheckCardinality
                    case XSParticleDecl.PARTICLE_WILDCARD:
                    {
                        if (dMinEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMinEffectiveTotalRange = dParticle.minEffectiveTotalRange();
                        if (dMaxEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMaxEffectiveTotalRange = dParticle.maxEffectiveTotalRange();

                        checkNSRecurseCheckCardinality(dChildren, dMinEffectiveTotalRange,
                                dMaxEffectiveTotalRange,
                                dSGHandler,
                                bParticle,bMinOccurs,bMaxOccurs,
                                checkWCOccurrence);

                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_ALL:
                    {
                        checkRecurse(dChildren, dMinOccurs, dMaxOccurs, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_CHOICE:
                    case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                    case XSParticleDecl.PARTICLE_ELEMENT:
                    {
                        throw new XMLSchemaException("cos-particle-restrict.2",
                                new Object[]{"all:choice,sequence,elt"});
                    }

                    default:
                    {
                        throw new XMLSchemaException("Internal-Error",
                                new Object[]{"in particleValidRestriction"});
                    }
                }
            }

            case XSModelGroupImpl.MODELGROUP_CHOICE:
            {
                switch (bType) {

                    // Choice:Any NSRecurseCheckCardinality
                    case XSParticleDecl.PARTICLE_WILDCARD:
                    {
                        if (dMinEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMinEffectiveTotalRange = dParticle.minEffectiveTotalRange();
                        if (dMaxEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMaxEffectiveTotalRange = dParticle.maxEffectiveTotalRange();

                        checkNSRecurseCheckCardinality(dChildren, dMinEffectiveTotalRange,
                                dMaxEffectiveTotalRange,
                                dSGHandler,
                                bParticle,bMinOccurs,bMaxOccurs,
                                checkWCOccurrence);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_CHOICE:
                    {
                        checkRecurseLax(dChildren, dMinOccurs, dMaxOccurs, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_ALL:
                    case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                    case XSParticleDecl.PARTICLE_ELEMENT:
                    {
                        throw new XMLSchemaException("cos-particle-restrict.2",
                                new Object[]{"choice:all,sequence,elt"});
                    }

                    default:
                    {
                        throw new XMLSchemaException("Internal-Error",
                                new Object[]{"in particleValidRestriction"});
                    }
                }
            }


            case XSModelGroupImpl.MODELGROUP_SEQUENCE:
            {
                switch (bType) {

                    // Choice:Any NSRecurseCheckCardinality
                    case XSParticleDecl.PARTICLE_WILDCARD:
                    {
                        if (dMinEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMinEffectiveTotalRange = dParticle.minEffectiveTotalRange();
                        if (dMaxEffectiveTotalRange == OCCURRENCE_UNKNOWN)
                            dMaxEffectiveTotalRange = dParticle.maxEffectiveTotalRange();

                        checkNSRecurseCheckCardinality(dChildren, dMinEffectiveTotalRange,
                                dMaxEffectiveTotalRange,
                                dSGHandler,
                                bParticle,bMinOccurs,bMaxOccurs,
                                checkWCOccurrence);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_ALL:
                    {
                        checkRecurseUnordered(dChildren, dMinOccurs, dMaxOccurs, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_SEQUENCE:
                    {
                        checkRecurse(dChildren, dMinOccurs, dMaxOccurs, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    case XSModelGroupImpl.MODELGROUP_CHOICE:
                    {
                        int min1 = dMinOccurs * dChildren.size();
                        int max1 = (dMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)?
                                dMaxOccurs : dMaxOccurs * dChildren.size();
                        checkMapAndSum(dChildren, min1, max1, dSGHandler,
                                bChildren, bMinOccurs, bMaxOccurs, bSGHandler);
                        return bExpansionHappened;
                    }

                    case XSParticleDecl.PARTICLE_ELEMENT:
                    {
                        throw new XMLSchemaException("cos-particle-restrict.2",
                                new Object[]{"seq:elt"});
                    }

                    default:
                    {
                        throw new XMLSchemaException("Internal-Error",
                                new Object[]{"in particleValidRestriction"});
                    }
                }
            }

        }

        return bExpansionHappened;
    }

    private void addElementToParticleVector (Vector v, XSElementDecl d)  {

        XSParticleDecl p = new XSParticleDecl();
        p.fValue = d;
        p.fType = XSParticleDecl.PARTICLE_ELEMENT;
        v.addElement(p);

    }

    private XSParticleDecl getNonUnaryGroup(XSParticleDecl p) {

        if (p.fType == XSParticleDecl.PARTICLE_ELEMENT ||
                p.fType == XSParticleDecl.PARTICLE_WILDCARD)
            return p;

        if (p.fMinOccurs==1 && p.fMaxOccurs==1 &&
                p.fValue!=null && ((XSModelGroupImpl)p.fValue).fParticleCount == 1)
            return getNonUnaryGroup(((XSModelGroupImpl)p.fValue).fParticles[0]);
        else
            return p;
    }

    private static Vector removePointlessChildren(XSParticleDecl p)  {

        if (p.fType == XSParticleDecl.PARTICLE_ELEMENT ||
                p.fType == XSParticleDecl.PARTICLE_WILDCARD)
            return null;

        Vector children = new Vector();

        XSModelGroupImpl group = (XSModelGroupImpl)p.fValue;
        for (int i = 0; i < group.fParticleCount; i++)
            gatherChildren(group.fCompositor, group.fParticles[i], children);

        return children;
    }


    private static void gatherChildren(int parentType, XSParticleDecl p, Vector children) {

        int min = p.fMinOccurs;
        int max = p.fMaxOccurs;
        int type = p.fType;
        if (type == XSParticleDecl.PARTICLE_MODELGROUP)
            type = ((XSModelGroupImpl)p.fValue).fCompositor;

        if (type == XSParticleDecl.PARTICLE_ELEMENT ||
                type== XSParticleDecl.PARTICLE_WILDCARD) {
            children.addElement(p);
            return;
        }

        if (! (min==1 && max==1)) {
            children.addElement(p);
        }
        else if (parentType == type) {
            XSModelGroupImpl group = (XSModelGroupImpl)p.fValue;
            for (int i = 0; i < group.fParticleCount; i++)
                gatherChildren(type, group.fParticles[i], children);
        }
        else if (!p.isEmpty()) {
            children.addElement(p);
        }

    }

    private void checkNameAndTypeOK(XSElementDecl dElement, int dMin, int dMax,
            XSElementDecl bElement, int bMin, int bMax)
        throws XMLSchemaException {


        //
        // Check that the names are the same
        //
        if (dElement.fName != bElement.fName ||
                dElement.fTargetNamespace != bElement.fTargetNamespace) {
            throw new XMLSchemaException(
                    "rcase-NameAndTypeOK.1",new Object[]{dElement.fName,
                            dElement.fTargetNamespace, bElement.fName, bElement.fTargetNamespace});
        }

        //
        // Check nillable
        //
        if (!bElement.getNillable() && dElement.getNillable()) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.2",
                    new Object[]{dElement.fName});
        }

        //
        // Check occurrence range
        //
        if (!checkOccurrenceRange(dMin, dMax, bMin, bMax)) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.3",
                    new Object[]{
                    dElement.fName,
                    Integer.toString(dMin),
                    dMax==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(dMax),
                            Integer.toString(bMin),
                            bMax==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(bMax)});
        }

        //
        // Check for consistent fixed values
        //
        if (bElement.getConstraintType() == XSConstants.VC_FIXED) {
            // derived one has to have a fixed value
            if (dElement.getConstraintType() != XSConstants.VC_FIXED) {
                throw new XMLSchemaException("rcase-NameAndTypeOK.4.a",
                        new Object[]{dElement.fName, bElement.fDefault.stringValue()});
            }

            // get simple type
            boolean isSimple = false;
            if (dElement.fType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE ||
                    ((XSComplexTypeDecl)dElement.fType).fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                isSimple = true;
            }

            // if there is no simple type, then compare based on string
            if (!isSimple && !bElement.fDefault.normalizedValue.equals(dElement.fDefault.normalizedValue) ||
                    isSimple && !bElement.fDefault.actualValue.equals(dElement.fDefault.actualValue)) {
                throw new XMLSchemaException("rcase-NameAndTypeOK.4.b",
                        new Object[]{dElement.fName,
                        dElement.fDefault.stringValue(),
                        bElement.fDefault.stringValue()});
            }
        }

        //
        // Check identity constraints
        //
        checkIDConstraintRestriction(dElement, bElement);

        //
        // Check for disallowed substitutions
        //
        int blockSet1 = dElement.fBlock;
        int blockSet2 = bElement.fBlock;
        if (((blockSet1 & blockSet2)!=blockSet2) ||
                (blockSet1==XSConstants.DERIVATION_NONE && blockSet2!=XSConstants.DERIVATION_NONE))
            throw new XMLSchemaException("rcase-NameAndTypeOK.6",
                    new Object[]{dElement.fName});


        //
        // Check that the derived element's type is derived from the base's.
        //
        if (!checkTypeDerivationOk(dElement.fType, bElement.fType,
                (short)(XSConstants.DERIVATION_EXTENSION|XSConstants.DERIVATION_LIST|XSConstants.DERIVATION_UNION))) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.7",
                    new Object[]{dElement.fName, dElement.fType.getName(), bElement.fType.getName()});
        }

    }


    private void checkIDConstraintRestriction(XSElementDecl derivedElemDecl,
            XSElementDecl baseElemDecl)
        throws XMLSchemaException {
        // TODO
    } // checkIDConstraintRestriction


    private boolean checkOccurrenceRange(int min1, int max1, int min2, int max2) {

        if ((min1 >= min2) &&
                ((max2==SchemaSymbols.OCCURRENCE_UNBOUNDED) ||
                        (max1!=SchemaSymbols.OCCURRENCE_UNBOUNDED && max1<=max2)))
            return true;
        else
            return false;
    }

    private void checkNSCompat(XSElementDecl elem, int min1, int max1,
            XSWildcardDecl wildcard, int min2, int max2,
            boolean checkWCOccurrence)
        throws XMLSchemaException {

        // check Occurrence ranges
        if (checkWCOccurrence && !checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-NSCompat.2",
                    new Object[]{
                    elem.fName,
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                            Integer.toString(min2),
                            max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        // check wildcard allows namespace of element
        if (!wildcard.allowNamespace(elem.fTargetNamespace))  {
            throw new XMLSchemaException("rcase-NSCompat.1",
                    new Object[]{elem.fName,elem.fTargetNamespace});
        }

    }

    private void checkNSSubset(XSWildcardDecl dWildcard, int min1, int max1,
            XSWildcardDecl bWildcard, int min2, int max2)
        throws XMLSchemaException {

        // check Occurrence ranges
        if (!checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-NSSubset.2", new Object[]{
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                            Integer.toString(min2),
                            max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        // check wildcard subset
        if (!isSubsetOf(dWildcard, bWildcard)) {
            throw new XMLSchemaException("rcase-NSSubset.1", null);
        }

        if (dWildcard.weakerProcessContents(bWildcard)) {
            throw new XMLSchemaException("rcase-NSSubset.3",
                    new Object[]{dWildcard.getProcessContentsAsString(),
                    bWildcard.getProcessContentsAsString()});
        }

    }


    private void checkNSRecurseCheckCardinality(Vector children, int min1, int max1,
            SubstitutionGroupHandler dSGHandler,
            XSParticleDecl wildcard, int min2, int max2,
            boolean checkWCOccurrence)
        throws XMLSchemaException {


        // check Occurrence ranges
        if (checkWCOccurrence && !checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.2", new Object[]{
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                            Integer.toString(min2),
                            max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        // Check that each member of the group is a valid restriction of the wildcard
        int count = children.size();
        try {
            for (int i = 0; i < count; i++) {
                XSParticleDecl particle1 = (XSParticleDecl)children.elementAt(i);
                particleValidRestriction(particle1, dSGHandler, wildcard, null, false);

            }
        }
        // REVISIT: should we really just ignore original cause of this error?
        //          how can we report it?
        catch (XMLSchemaException e) {
            throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.1", null);
        }

    }

    private void checkRecurse(Vector dChildren, int min1, int max1,
            SubstitutionGroupHandler dSGHandler,
            Vector bChildren, int min2, int max2,
            SubstitutionGroupHandler bSGHandler)
        throws XMLSchemaException {

        // check Occurrence ranges
        if (!checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-Recurse.1", new Object[]{
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                    Integer.toString(min2),
                    max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        int count1= dChildren.size();
        int count2= bChildren.size();

        int current = 0;
        label: for (int i = 0; i<count1; i++) {

            XSParticleDecl particle1 = (XSParticleDecl)dChildren.elementAt(i);
            for (int j = current; j<count2; j++) {
                XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
                current +=1;
                try {
                    particleValidRestriction(particle1, dSGHandler, particle2, bSGHandler);
                    continue label;
                }
                catch (XMLSchemaException e) {
                    if (!particle2.emptiable())
                        throw new XMLSchemaException("rcase-Recurse.2", null);
                }
            }
            throw new XMLSchemaException("rcase-Recurse.2", null);
        }

        // Now, see if there are some elements in the base we didn't match up
        for (int j=current; j < count2; j++) {
            XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
            if (!particle2.emptiable()) {
                throw new XMLSchemaException("rcase-Recurse.2", null);
            }
        }

    }

    private void checkRecurseUnordered(Vector dChildren, int min1, int max1,
            SubstitutionGroupHandler dSGHandler,
            Vector bChildren, int min2, int max2,
            SubstitutionGroupHandler bSGHandler)
        throws XMLSchemaException {


        // check Occurrence ranges
        if (!checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-RecurseUnordered.1", new Object[]{
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                    Integer.toString(min2),
                    max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        int count1= dChildren.size();
        int count2 = bChildren.size();

        boolean foundIt[] = new boolean[count2];

        label: for (int i = 0; i<count1; i++) {
            XSParticleDecl particle1 = (XSParticleDecl)dChildren.elementAt(i);

            for (int j = 0; j<count2; j++) {
                XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
                try {
                    particleValidRestriction(particle1, dSGHandler, particle2, bSGHandler);
                    if (foundIt[j])
                        throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
                    else
                        foundIt[j]=true;

                    continue label;
                }
                catch (XMLSchemaException e) {
                }
            }
            // didn't find a match.  Detect an error
            throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
        }

        // Now, see if there are some elements in the base we didn't match up
        for (int j=0; j < count2; j++) {
            XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
            if (!foundIt[j] && !particle2.emptiable()) {
                throw new XMLSchemaException("rcase-RecurseUnordered.2", null);
            }
        }

    }

    private void checkRecurseLax(Vector dChildren, int min1, int max1,
            SubstitutionGroupHandler dSGHandler,
            Vector bChildren, int min2, int max2,
            SubstitutionGroupHandler  bSGHandler)
        throws XMLSchemaException {

        // check Occurrence ranges
        if (!checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-RecurseLax.1", new Object[]{
                    Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                            Integer.toString(min2),
                            max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        int count1= dChildren.size();
        int count2 = bChildren.size();

        int current = 0;
        label: for (int i = 0; i<count1; i++) {

            XSParticleDecl particle1 = (XSParticleDecl)dChildren.elementAt(i);
            for (int j = current; j<count2; j++) {
                XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
                current +=1;
                try {
                    // IHR: go back one element on b list because the next element may match
                    // this as well.
                    if (particleValidRestriction(particle1, dSGHandler, particle2, bSGHandler))
                        current--;
                    continue label;
                }
                catch (XMLSchemaException e) {
                }
            }
            // didn't find a match.  Detect an error
            throw new XMLSchemaException("rcase-RecurseLax.2", null);

        }

    }

    private void checkMapAndSum(Vector dChildren, int min1, int max1,
            SubstitutionGroupHandler dSGHandler,
            Vector bChildren, int min2, int max2,
            SubstitutionGroupHandler bSGHandler)
        throws XMLSchemaException {

        // See if the sequence group is a valid restriction of the choice

        // Here is an example of a valid restriction:
        //   <choice minOccurs="2">
        //       <a/>
        //       <b/>
        //       <c/>
        //   </choice>
        //
        //   <sequence>
        //        <b/>
        //        <a/>
        //   </sequence>

        // check Occurrence ranges
        if (!checkOccurrenceRange(min1,max1,min2,max2)) {
            throw new XMLSchemaException("rcase-MapAndSum.2",
                    new Object[]{Integer.toString(min1),
                    max1==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max1),
                            Integer.toString(min2),
                            max2==SchemaSymbols.OCCURRENCE_UNBOUNDED?"unbounded":Integer.toString(max2)});
        }

        int count1 = dChildren.size();
        int count2 = bChildren.size();

        label: for (int i = 0; i<count1; i++) {

            XSParticleDecl particle1 = (XSParticleDecl)dChildren.elementAt(i);
            for (int j = 0; j<count2; j++) {
                XSParticleDecl particle2 = (XSParticleDecl)bChildren.elementAt(j);
                try {
                    particleValidRestriction(particle1, dSGHandler, particle2, bSGHandler);
                    continue label;
                }
                catch (XMLSchemaException e) {
                }
            }
            // didn't find a match.  Detect an error
            throw new XMLSchemaException("rcase-MapAndSum.1", null);
        }
    }
    
    final protected boolean checkEmptyFacets(XSSimpleType baseType) {
        return true;
    }
}
