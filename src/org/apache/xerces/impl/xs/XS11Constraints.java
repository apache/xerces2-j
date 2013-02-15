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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.alternative.XSTypeAlternativeImpl;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.XS11CMRestriction;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.impl.xs.util.XS11TypeHelper;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * XML Schema 1.1 constraints
 * 
 * @xerces.internal
 *
 * @author Khaled Noaman, IBM
 *
 * @version $Id$
 *
 */
class XS11Constraints extends XSConstraints {

    public XS11Constraints() {
        super(SchemaGrammar.getXSAnyType(Constants.SCHEMA_VERSION_1_1), Constants.SCHEMA_VERSION_1_1);
    }

    public boolean overlapUPA(XSElementDecl element,
            XSWildcardDecl wildcard,
            SubstitutionGroupHandler sgHandler) {
        return false;
    }

    /**
     * Check that a given particle is a valid restriction of a base particle.
     */
    final protected void checkElementDeclsConsistent(XSComplexTypeDecl type,
            XSParticleDecl particle,
            SymbolHash elemDeclHash,
            SubstitutionGroupHandler sgHandler,
            XSGrammarBucket grammarBucket,
            ArrayList wcList,
            Stack elemDeclStack) throws XMLSchemaException {

        // check for elements in the tree with the same name and namespace
        if (particle.fType == XSParticleDecl.PARTICLE_WILDCARD) {
            return;
        }

        // clear wildcard decl list and element decl stack
        if (wcList.size() > 0) {
            wcList.clear();
        }
        
        if (elemDeclStack.size() > 0) {
            elemDeclStack.clear();
        }

        // check for open content
        if (type.fOpenContent != null) {
            final XSWildcardDecl wc = type.fOpenContent.fWildcard;
            if (wc != null && wc.fProcessContents != XSWildcardDecl.PC_SKIP) {
                wcList.add(wc);
            }
        }

        if (particle.fType == XSParticleDecl.PARTICLE_ELEMENT) {
            elemDeclStack.push((XSElementDecl)(particle.fValue));
        }
        else  {
            preprocessModelGroupParticle((XSModelGroupImpl)particle.fValue, wcList, elemDeclStack);
        }

        // process element declarations
        while (!elemDeclStack.empty()) {
            final XSElementDecl elem = (XSElementDecl)elemDeclStack.pop();

            findElemInTable(type, elem, elemDeclHash);

            if (elem.fScope == XSConstants.SCOPE_GLOBAL) {
                // Check for substitution groups.
                XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(elem, fSchemaVersion);
                for (int i = 0; i < subGroup.length; i++) {
                    findElemInTable(type, subGroup[i], elemDeclHash);
                }
            }
            else {
                checkExtraEDCRules(type, elem, grammarBucket, wcList);
            }
        }
    }

    final public void findElemInTable(XSComplexTypeDecl type,
            XSElementDecl elem,
            SymbolHash elemDeclHash)
        throws XMLSchemaException {

        final XSElementDecl existingElem = findExistingElement(elem, elemDeclHash);

        // First time or is same element
        if (existingElem == null || existingElem == elem) {
            return;
        }

        if (elem.fType != existingElem.fType) {
            // Types are not the same
            throw new XMLSchemaException("cos-element-consistent", new Object[] {type.fName, elem.fName});
        }
        
        if (XS11TypeHelper.isTypeTablesComparable(elem.getTypeAlternatives(), existingElem.getTypeAlternatives()) && !isTypeTablesEquivalent(elem, existingElem)) {
            // Type tables are not equivalent
            throw new XMLSchemaException("cos-element-consistent.4.b", new Object[] {type.fName, elem.fName});  
        }
    }

    protected void preprocessModelGroupParticle(XSModelGroupImpl group, ArrayList wcList, Stack elemDeclStack) {
        for (int i = group.fParticleCount - 1; i >=0 ; i--) {
            final XSParticleDecl particle = group.fParticles[i];
            final int pType = particle.fType;
            if (pType == XSParticleDecl.PARTICLE_WILDCARD) {
                final XSWildcardDecl wc = (XSWildcardDecl) particle.fValue;
                if (wc.fProcessContents != XSWildcardDecl.PC_SKIP) {
                    wcList.add(wc);
                }
            }
            else if (pType == XSParticleDecl.PARTICLE_ELEMENT) {
                elemDeclStack.push((XSElementDecl)particle.fValue);
            }
            else {
                preprocessModelGroupParticle((XSModelGroupImpl)particle.fValue, wcList, elemDeclStack);
            }
        }
    }

    private void checkExtraEDCRules(XSComplexTypeDecl type,
            XSElementDecl elem,
            XSGrammarBucket grammarBucket,
            ArrayList wcList) throws XMLSchemaException {
        
        // If all of the following are true:
        //  1 The {particles} property contains (either directly, indirectly,
        //    or implicitly) one or more element declarations with the same
        //    expanded name Q; call these element declarations EDS.
        //  2 At least one of the following is true
        //    2.1 The {particles} property contains one or more strict or lax
        //        wildcard particles which match Q.
        //    2.2 The Model Group is the {term} of the content model of some
        //        Complex Type Definition CTD and CTD.{content type} has an
        //        {open content} with a strict or lax Wildcard which matches Q.
        //  3 There exists a top-level element declaration G with the expanded
        //    name Q.
        //  then the {type table}s of EDS and the {type table} of G must either
        //  all be absent or else all be present and equivalent.
        for (int i=0; i<wcList.size(); i++) {
            final XSWildcardDecl wc = (XSWildcardDecl) wcList.get(i);
            if (wc.allowName(elem.fTargetNamespace, elem.fName)) {
                final SchemaGrammar grammar = grammarBucket.getGrammar(elem.fTargetNamespace);
                if (grammar != null) {
                    final XSElementDecl gElem = grammar.getGlobalElementDecl(elem.fName);
                    if (gElem != null) {
                        if (gElem != elem && XS11TypeHelper.isTypeTablesComparable(elem.getTypeAlternatives(), gElem.getTypeAlternatives()) && !isTypeTablesEquivalent(elem, gElem)) {
                            // Type tables are not equivalent
                            throw new XMLSchemaException("cos-element-consistent.4.b", new Object[] {type.fName, elem.fName});
                        }
                    }
                } 
            }
        }
    }

    /*
     * Check if two type tables are equivalent.
     */
    final public boolean isTypeTablesEquivalent(XSElementDecl elementDecl1, XSElementDecl elementDecl2) {
        
        boolean typeTablesEquivalent = true;
        
        final XSTypeAlternativeImpl[] typeTable1 = elementDecl1.getTypeAlternatives();
        final XSTypeAlternativeImpl[] typeTable2 = elementDecl2.getTypeAlternatives();
        
        // if two type tables have different length
        if (typeTable1.length != typeTable2.length) {
            typeTablesEquivalent = false;
        }

        if (typeTablesEquivalent) {
            for (int typeAltIdx = 0; typeAltIdx < typeTable1.length; typeAltIdx++) {
                final XSTypeAlternativeImpl typeAlt1 = typeTable1[typeAltIdx];
                final XSTypeAlternativeImpl typeAlt2 = typeTable2[typeAltIdx];
                if (!isTypeAlternativesEquivalent(typeAlt1, typeAlt2)) {
                    typeTablesEquivalent = false;
                    break;
                }
            }
        }

        if (typeTablesEquivalent && !elementDecl1.isTypeTableOK()) {
            typeTablesEquivalent = isTypeAlternativesEquivalent(elementDecl1.getDefaultTypeDefinition(), elementDecl2.getDefaultTypeDefinition()); 
        }
        
        return typeTablesEquivalent;
        
    } // isTypeTablesEquivalent

    /*
     * Check if two type alternative components are equivalent.
     */
    private boolean isTypeAlternativesEquivalent(XSTypeAlternativeImpl typeAlt1, XSTypeAlternativeImpl typeAlt2) {
        
        final String defNamespace1 = typeAlt1.getXPathDefaultNamespace();
        final String defNamespace2 = typeAlt2.getXPathDefaultNamespace();
        final String testStr1 = (typeAlt1.getTest() == null) ? null : typeAlt1.getTest().toString();
        final String testStr2 = (typeAlt2.getTest() == null) ? null : typeAlt2.getTest().toString();
        final XSTypeDefinition typeDefn1 = typeAlt1.getTypeDefinition();
        final XSTypeDefinition typeDefn2 = typeAlt2.getTypeDefinition();
        final String baseURI1 = typeAlt1.getBaseURI();
        final String baseURI2 = typeAlt2.getBaseURI();

        // 2 T1.{test}.{default namespace}  and T2.{test}.{default namespace}
        //   either are both absent or have the same value.
        // 3 T1.{test}.{base URI} and T2.{test}.{base URI} either are both
        //   absent or have the same value.
        // 4 T1.{test}.{expression} and T2.{test}.{expression} have the same
        //   value.
        // 5 T1.{type definition} and T2.{type definition} are the same type
        //   definition. 
        if (defNamespace1 != defNamespace2 || typeDefn1 != typeDefn2
                || (testStr1 == null && testStr2 != null)
                || (testStr1 != null && !testStr1.equals(testStr2))
                || (baseURI1 == null && baseURI2 != null)
                || (baseURI1 != null && !baseURI1.equals(baseURI2))) {
            return false;
        }
        
        // 1 T1.{test}.{namespace bindings} and T2.{test}.{namespace bindings}
        //   have the same number of Namespace Bindings, and for each entry in
        //   T1.{test}.{namespace bindings}  there is a corresponding entry in
        //   T2.{test}.{namespace bindings}  with the same {prefix} and
        //   {namespace}. 
        final NamespaceSupport nsContext1 = typeAlt1.getNamespaceContext();
        final NamespaceSupport nsContext2 = typeAlt2.getNamespaceContext();
        final Enumeration prefixes1 =  nsContext1.getAllPrefixes();
        final Enumeration prefixes2 =  nsContext2.getAllPrefixes();

        // REVISIT: optimize (same number + prefix/uri mapping)
        while (prefixes1.hasMoreElements()) {
            if (!prefixes2.hasMoreElements()) {
                return false;
            }
            
            final String prefix1 = (String) prefixes1.nextElement();
            final String prefix2 = (String) prefixes2.nextElement();

            if (nsContext1.getURI(prefix1) != nsContext2.getURI(prefix1)
                    || nsContext1.getURI(prefix2) != nsContext2.getURI(prefix2)) {
                return false;
            }
        }

        return !prefixes2.hasMoreElements();        
    } // isTypeAlternativesEquivalent

    /**
     *  Schema Component Constraint: Wildcard Subset
     */
    public boolean isSubsetOf(XSWildcardDecl wildcard, XSWildcardDecl superWildcard) {
        // if the super is null (not expressible), return false
        if (superWildcard == null) {
            return false;
        }

        // sub is a wildcard subset of super if and only if one of the following is true
        //  1 super has {variety} = any.
        if (superWildcard.fType != XSWildcardDecl.NSCONSTRAINT_ANY) {

        	// sub has a variety of enumeration
            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
                //  2 Both sub and super have {variety} = enumeration, and super's {namespaces} is a superset of sub's {namespaces}.
                if (superWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
                    if (!subset2sets(wildcard.fNamespaceList, superWildcard.fNamespaceList)) {
                        return false;
                    }
                }
                //  3 The {variety} of sub is enumeration, the {variety} of super is not, and the {namespaces} of the two are disjoint.
                else if (!disjoint2sets(wildcard.fNamespaceList, superWildcard.fNamespaceList)){
                    return false;
                }
            }

            // 4 Both sub and super have {variety} = not, and super's {namespaces} is a subset of sub's {namespaces}.
            else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                if (superWildcard.fType != XSWildcardDecl.NSCONSTRAINT_NOT || !subset2sets(superWildcard.fNamespaceList, wildcard.fNamespaceList)) {
                    return false;
                }
            }
            else {
                // Sub is any super is not. Not a subset.
                return false;
            }
        }

        // And all of the following must be true:
        //  1 Each QName member of super.{disallowed names} is not allowed by sub, as defined in Wildcard allows Expanded Name (3.10.4.2).
        //  2 If super.{disallowed names} contains defined, then sub.{disallowed names} also contains defined.
        //  3 If super.{disallowed names} contains sibling, then sub.{disallowed names} also contains sibling.
        if ((superWildcard.fDisallowedDefined && !wildcard.fDisallowedDefined)
        		|| (superWildcard.fDisallowedSibling && wildcard.fDisallowedSibling)
        		|| (superWildcard.fDisallowedNamesList != null && allowedNames(wildcard, superWildcard))) {
        	return false;
        }

        return true;
    }

    public XSWildcardDecl performUnionWith(XSWildcardDecl wildcard,
                                           XSWildcardDecl otherWildcard,
                                           short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (otherWildcard == null) {
            return null;
        }

        // The {variety} and {namespaces} of O are consistent with O being the wildcard
        // union of O1 and O2 if and only if one of the following is true
        XSWildcardDecl unionWildcard = new XSWildcardDecl();
        unionWildcard.fProcessContents = processContents;

        // 1 O, O1, and O2 all have the same {variety} and {namespaces}.
        if (areSame(wildcard, otherWildcard)) {
            unionWildcard.fType = wildcard.fType;
            unionWildcard.fNamespaceList = wildcard.fNamespaceList;
        }
        // 2 Either O1 or O2 has {variety} any, and O has {variety} any.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) || (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) ) {
            unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
        }
        // 3 O, O1, and O2 all have {variety} enumeration, and O's {namespaces} is the union
        //   of O1's {namespaces} and O2's {namespaces}.
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) ) {
            unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            unionWildcard.fNamespaceList = union2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }
        // 4 O1 and O2 both have {variety} not, and one of the following is true
        else if ( (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) && (otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) ) {
            //   4.1 The intersection of the {namespaces} of O1 and O2 is the empty set, and O has {variety} = any.        	
            String[] nsList = intersect2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
            if (nsList.length == 0) {
                unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
            }
            //   4.2 O also has {variety} not, and the {namespaces} of O is the non-empty intersection of the {namespaces} of O1 and O2.            
            else {
                unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
                unionWildcard.fNamespaceList = nsList;
            }
        }
        // 5 Either O1 or O2 has {variety} not and {namespaces} S1, and the other has {variety} enumeration and {namespaces} S2, and one of the following is true
        //   5.1 The set difference S1 minus S2 is the empty set, and O has {variety} = any.
        //   5.2 O has {variety} = not and the {namespaces} of O is the non-empty set difference S1 minus S2.
        else {
            String[] nsList = (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT)
                ? difference2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList) : difference2sets(otherWildcard.fNamespaceList, wildcard.fNamespaceList);
            if (nsList.length == 0) {
                unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_ANY;
            }
            else {
                unionWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
                unionWildcard.fNamespaceList = nsList;
            }
        }

        // The {disallowed names} property of O is consistent with O being the wildcard union of O1 and O2
        // if and only if O's {disallowed names} includes all and only the following:
        // 1 QName members of O1's {disallowed names} that are not allowed by O2, as defined in Wildcard allows Expanded Name (3.10.4.2).
        // 2 QName members of O2's {disallowed names} that are not allowed by O1.
        unionWildcard.fDisallowedNamesList = disallowedNamesUnion(wildcard, otherWildcard);

        // 3 The keyword defined if it is contained in both O1's and O2's {disallowed names}.;
        unionWildcard.fDisallowedDefined = wildcard.fDisallowedDefined && otherWildcard.fDisallowedDefined;

        return unionWildcard;
    }

    public XSWildcardDecl performIntersectionWith(XSWildcardDecl wildcard,
                                                  XSWildcardDecl otherWildcard,
                                                  short processContents) {
        // if the other wildcard is not expressible, the result is still not expressible
        if (otherWildcard == null) {
            return null;
        }

        // The {variety} and {namespaces} of O are consistent with O being the wildcard intersection of O1 and O2 if and only if
        XSWildcardDecl intersectWildcard = new XSWildcardDecl();
        intersectWildcard.fProcessContents = processContents;

        // 1 O, O1, and O2 have the same {variety} and {namespaces}.
        if (areSame(wildcard, otherWildcard)) {
            intersectWildcard.fType = wildcard.fType;
            intersectWildcard.fNamespaceList = wildcard.fNamespaceList;
        }
        // 2 Either O1 or O2 has {variety} = any and O has {variety} and {namespaces} identical to those of the other.
        else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY || otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
            // both cannot be ANY, if we have reached here.
            XSWildcardDecl localWildcard = wildcard;

            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
                localWildcard = otherWildcard;
            }

            intersectWildcard.fType = localWildcard.fType;
            intersectWildcard.fNamespaceList = localWildcard.fNamespaceList;
        }
        // 3 O, O1, and O2 all have {variety} = enumeration, and the {namespaces} of O is the intersection of the {namespaces} of O1 and O2.
        else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST && otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
            intersectWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = intersect2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }
        // 4 O, O1, and O2 all have {variety} not, and the {namespaces} of O is the union of the {namespaces} of O1 and O2.
        else if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT && otherWildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            intersectWildcard.fType = XSWildcardDecl.NSCONSTRAINT_NOT;
            intersectWildcard.fNamespaceList = union2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }
        // 5 Either O1 or O2 has {variety} = not and {namespaces} = S1 and the other has {variety} = enumeration and {namespaces} = S2, and O has {variety} = enumeration and {namespaces} = the set difference S2 minus S1.
        else {
            intersectWildcard.fType = XSWildcardDecl.NSCONSTRAINT_LIST;
            intersectWildcard.fNamespaceList = (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT)
                ? difference2sets(otherWildcard.fNamespaceList, wildcard.fNamespaceList) : difference2sets(wildcard.fNamespaceList, otherWildcard.fNamespaceList);
        }

        // The {disallowed names} property of O is consistent with O being the wildcard intersection of O1 and O2
        // if and only if O's {disallowed names} includes all and only the following:
        // 1 QName members of O1's {disallowed names} that are allowed by O2, as defined in Wildcard allows Expanded Name (3.10.4.2).
        // 2 QName members of O2's {disallowed names} that are allowed by O1.
        // 3 The intersection of O1's {disallowed names} and O2's {disallowed names}.
        // 4 The keyword defined if it is a member of either {disallowed names}.
        intersectWildcard.fDisallowedNamesList = disallowedNamesIntersection(wildcard, otherWildcard);
        intersectWildcard.fDisallowedDefined = wildcard.fDisallowedDefined || otherWildcard.fDisallowedDefined;

        return intersectWildcard;
    }

    /**
     * Wildcard constraints - helper methods
     */
    // REVISIT: update the method in XSConstraints (remove the check for not(list))
    boolean areSame(XSWildcardDecl wildcard, XSWildcardDecl otherWildcard) {
        if (wildcard.fType == otherWildcard.fType) {
            // ##any, true
            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
                return true;
            }

            // ## list [enumeration] or [not], must have the same length, 
            // and each item in one list must appear in the other one
            // (we are assuming that there are no duplicate items in a list)
            if (wildcard.fNamespaceList.length == otherWildcard.fNamespaceList.length) {
                for (int i=0; i<wildcard.fNamespaceList.length; i++) {
                    if (!elementInSet(wildcard.fNamespaceList[i], otherWildcard.fNamespaceList)) {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    } // areSame
    
    private boolean allowedNames(XSWildcardDecl wildcard, XSWildcardDecl superWildcard) {
        for (int i = 0; i < superWildcard.fDisallowedNamesList.length; i++) {
            if (wildcard.allowQName(superWildcard.fDisallowedNamesList[i])) {
                return true;
            }
        }
        return false;
    }

    // A namespace name or absent that is allowed by o1, as defined in Wildcard allows Namespace
    // Name (3.10.4.3), but not by o2
    private boolean disallowedNamespaces(XSWildcardDecl o1, XSWildcardDecl o2) {
    	// o2 has variety of {any} so it allows any namesapce
    	if (o2.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
    		return false;
    	}

    	// o1 allows any namespace, but o2 is restrictive
        if (o1.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
        	return true;
        }

        // o1 allows a list of namespace, so check to see if any of the
        // namespaces in the list is disallowed by o2
        if (o1.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
            for (int i = 0; i < o1.fNamespaceList.length; i++) {
                if (!o2.allowNamespace(o1.fNamespaceList[i])) {
                    return true;
                }
            }
            return false;
        }

        // o2 disallows a list of namespaces, so check to see if any
        // of the namespaces in the list is allowed by o1
        if (o2.fType == XSWildcardDecl.NSCONSTRAINT_NOT){
            for (int i=0; i<o2.fNamespaceList.length; i++) {
                if (o1.allowNamespace(o2.fNamespaceList[i])) {
                    return true;
                }
            }
            return false;
        }

        // o1 is not, and o2 is enumaration, so o2 is more restrictive
        return true;
    }

    // returns the set difference set1 minus set2
    private String[] difference2sets(String[] set1, String[] set2) {
        String[] result = new String[set1.length];

        // simple implemention,
        int count = 0;
        for (int i=0; i<set1.length; i++) {
            if (!elementInSet(set1[i], set2))
                result[count++] = set1[i];
        }

        String[] result2 = new String[count];
        System.arraycopy(result, 0, result2, 0, count);

        return result2;
    }

    private QName[] disallowedNamesUnion(XSWildcardDecl one, XSWildcardDecl theOther) {
    	final int len1 = (one.fDisallowedNamesList == null) ? 0 : one.fDisallowedNamesList.length;
    	final int len2 = (theOther.fDisallowedNamesList == null) ? 0 : theOther.fDisallowedNamesList.length;
        final QName[] result = new QName[len1 + len2];

        // simple implementation
        int count = 0;
        for (int i=0; i<len1; i++) {
            if (!theOther.allowQName(one.fDisallowedNamesList[i])) {
                result[count++] = one.fDisallowedNamesList[i];
            }
        }
        for (int i=0; i<len2; i++) {
            if (!one.allowQName(theOther.fDisallowedNamesList[i])) {
                result[count++] = theOther.fDisallowedNamesList[i];
            }
        }
        QName[] result2 = new QName[count];
        System.arraycopy(result, 0, result2, 0, count);
        return result2;
    }

    // 1 QName members of O1's {disallowed names} that are allowed by O2, as defined in Wildcard allows Expanded Name (3.10.4).
    // 2 QName members of O2's {disallowed names} that are allowed by O1.
    // 3 The intersection of O1's {disallowed names} and O2's {disallowed names}.
    private QName[] disallowedNamesIntersection(XSWildcardDecl one, XSWildcardDecl theOther) {
    	final int len1 = (one.fDisallowedNamesList == null) ? 0 : one.fDisallowedNamesList.length;
    	final int len2 = (theOther.fDisallowedNamesList == null) ? 0 : theOther.fDisallowedNamesList.length;
        final QName[] result = new QName[len1 + len2];

        // simple implementation
        int count = 0;
        for (int i=0; i<len1; i++) {
        	final QName qname = one.fDisallowedNamesList[i];
            if (theOther.allowQName(qname)) {
                result[count++] = qname;
            }
            // intersection of O1.disallowed names and O2.disallowed names
            else if (elementInSet(qname, theOther.fDisallowedNamesList)) {
                result[count++] = qname;
            }
        }
        for (int i=0; i<len2; i++) {
            if (one.allowQName(theOther.fDisallowedNamesList[i])) {
                result[count++] = theOther.fDisallowedNamesList[i];
            }
        }

        QName[] result2 = new QName[count];
        System.arraycopy(result, 0, result2, 0, count);
        return result2;
    }

    private boolean elementInSet(QName ele, QName[] eleSet){
        boolean found = false;
        final int length = (eleSet == null) ? 0 : eleSet.length;
        for (int i=0; i<length && !found; i++) {
            if (ele.equals(eleSet[i])) {
                found = true;
            }
        }

        return found;
    }

    protected void groupSubsumption(XSParticleDecl dParticle, XSParticleDecl bParticle,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, String dName,
            SimpleLocator locator) {
        // When we get here, particles are not null. Neither are content models.
        XSCMValidator cmd = cmBuilder.getContentModel(dParticle);
        XSCMValidator cmb = cmBuilder.getContentModel(bParticle);
        if (!new XS11CMRestriction(cmb, cmd, SGHandler, grammarBucket, cmBuilder, this).check()) {
            reportSchemaError(errorReporter, locator,
                    "src-redefine.6.2.2",
                    new Object[]{dName, ""});
        }
    }
    
    protected void typeSubsumption(XSComplexTypeDecl dType, XSComplexTypeDecl bType,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, SimpleLocator locator) {
        // When we get here, particles are not null. Neither are content models.
        XSCMValidator cmd = dType.getContentModel(cmBuilder);
        XSCMValidator cmb = bType.getContentModel(cmBuilder);
        if (!new XS11CMRestriction(cmb, cmd, SGHandler, grammarBucket, cmBuilder, this).check()) {
            reportSchemaError(errorReporter, locator,
                    "derivation-ok-restriction.5.4.2",
                    new Object[]{dType.fName});
        }
    }

    final protected boolean checkEmptyFacets(XSSimpleType baseType) {
        return baseType.getMultiValueFacets() == XSObjectListImpl.EMPTY_LIST;
    }
}
