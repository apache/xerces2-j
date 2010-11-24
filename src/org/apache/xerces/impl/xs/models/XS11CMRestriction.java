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

package org.apache.xerces.impl.xs.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSElementDeclHelper;
import org.apache.xerces.impl.xs.XSGrammarBucket;
import org.apache.xerces.impl.xs.XSOpenContentDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;


// TODO: tests:
// * base all: (e, w?)?; derived all: (e)? w. Should be invalid <e1/>
// * base all: (e/f, w); derived all: (e, w)
// * base: e|w(lax) + w(skip), derived: w(lax) + w(skip)
// Example test:
// Bad: base: ((a|b)&) derived: (a & b).
// derive allow a,b, base doesn't.
// Bad: base: ((ns1:a|ns2:b) & ns1:*) derived: (ns1:a & ns2:b).
// derive allow a,b, base doesn't.
// Good: base: ((ns1:a|ns2:b) & ns1:*? & ns2:*?) derived: (ns1:a & ns2:b).
// base: (a{2} & *), derived: (a & *)
/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public final class XS11CMRestriction implements XSElementDeclHelper {
    private final SubstitutionGroupHandler sgh;
    private final XSGrammarBucket gb;
    private final CMBuilder cmb;
    private final XSConstraints xsc;
    
    // Content models to be checked
    private XS11CM base, derived;
    // Used when both base and derived are "all" groups
    private XS11AllCM allb, alld;
    // Used when base is "dfa" and derived is "all"
    private XSDFACM dfab;

    // Temporary QName object
    private final QName qname = new QName();

    // Pairs of matching states from base and derived.
    private final List states = new ArrayList();
    // A pair of matching states.
    private StatePair pair = null;

    // The following field represents a wildcard. It's typically a wildcad
    // from the derived type. We keep subtract matching components in the base
    // from this wildcard.
    private short wType;
    private final List wNSList = new ArrayList();
    private final List wNSListTemp = new ArrayList();
    // Disallowed name list
    private final List wDNList = new ArrayList();
    // ##sibling and ##defined
    private boolean wDD, wDS;
    // Allowed name list, used after subtracting a disallowed list.
    private final List wANList = new ArrayList();

    // List of QNames used for ##sibling and ##defined checking
    private List globals;
    private List siblingsB;
    private List siblingsD;

    // Element/wildcard decls in base and derived
    private XSElementDecl eb, ed;
    private XSWildcardDecl wb, wd;
    // Current/next states in base and derived.
    private int[] b, bn, d, dn;
    // Index of the previously matched decl in base and derived, so that we
    // know where to continue.
    private final int[] indexb = new int[1], indexd = new int[1];
    // Optimization. If an element declaration in derived is the same as an
    // element declaration in base without using any substitution group, then
    // we don't need to dig into the sub-groups. They will be the same.
    private boolean matchedHead;

    // Interface implemented by the content model classes
    interface XS11CM extends XSCMValidator {
        // Find the next possible element transition.
        // "s" is the current state, "sn" is the next state after the transition,
        // "index" has info about where to start looking, and stores the index
        // of the new declaration if one is found, or -1.
        // Returns the matching decl, or null.
        public XSElementDecl nextElementTransition(int[] s, int[] sn, int[] index);
        // Find the next possible wildcard transition, including open content.
        public XSWildcardDecl nextWildcardTransition(int[] s, int[] sn, int[] index);
        // Check whether a matched wildcard is an open content.
        public boolean isOpenContent(XSWildcardDecl w);
        // Check whether a QName is allowed by a wildcard in the content model.
        // Need to check namespace, local name, ##sibling, and ##defined.
        public boolean allowExpandedName(XSWildcardDecl wildcard,
                QName name, SubstitutionGroupHandler sgh,
                XSElementDeclHelper edh);
        // Get the list of names known to the content model, for ##sibling.
        public List getDefinedNames(SubstitutionGroupHandler sgh);
        // Before putting a pair of states into the list, give the content
        // model a chance to optimize. If a set of states can be folded into
        // a single one, the content model can change the state int arrays
        // to represent the one state. e.g. if both base and derived have
        // the same element declaration with the same min/maxOccurs information,
        // then we don't have to have many different states.
        // Currently this is only implemented by DFA.
        public void optimizeStates(XS11CM base, int[] b, int[] d, int indexb);
    }
    
    public XS11CMRestriction(XSCMValidator base, XSCMValidator derived,
            SubstitutionGroupHandler sgh, XSGrammarBucket gb,
            CMBuilder cmb, XSConstraints xsc) {
        this.base = (XS11CM)base;
        this.derived = (XS11CM)derived;
        this.sgh = sgh;
        this.gb = gb;
        this.cmb = cmb;
        this.xsc = xsc;
    }

    // Helper: add a pair of states to the list, if such pair is not already
    // in the list.
    private void addState() {
        // Optimize the states when possible.
        derived.optimizeStates(base, bn, dn, indexb[0]);
        // Reuse the "pair" object
        if (pair == null) {
            pair = new StatePair(bn, dn);
        }
        else {
            pair.set(bn, dn);
        }
        // Only add if the pair isn't already in the list
        if (!states.contains(pair)) {
            states.add(pair);
            pair = null;
        }
    }

    // Helper: copy a derived wildcard to fields of this class.
    private void copyDerivedWildcard() {
        wType = wd.fType;
        wDD = wd.fDisallowedDefined;
        wDS = wd.fDisallowedSibling;
        wNSList.clear();
        if (wType == XSWildcardDecl.NSCONSTRAINT_ANY) {
            // "any" is the same as "not" + empty namespace list.
            wType = XSWildcardDecl.NSCONSTRAINT_NOT;
        }
        else {
            int size = wd.fNamespaceList == null ? 0 : wd.fNamespaceList.length;
            for (int i = 0; i < size; i++) {
                wNSList.add(wd.fNamespaceList[i]);
            }
        }
        // Copy disallowed name list
        // Don't clear, because the list is pre-populated with element names
        int size = wd.fDisallowedNamesList == null ? 0 : wd.fDisallowedNamesList.length;
        for (int i = 0; i < size; i++) {
            wDNList.add(wd.fDisallowedNamesList[i].uri);
            wDNList.add(wd.fDisallowedNamesList[i].localpart);
        }
        // Allowed name list starts as empty
        wANList.clear();
    }

    // Helper: add a name to the "allowed" list
    private void addAN(String ns, String name) {
        // Only add if it's allowed by the current wildcard
        if (!allowNS(ns)) {
            return;
        }
        // Only add if the name is not in the disallowed name list.
        for (int i = 0; i < wDNList.size();) {
            if (wDNList.get(i++) == ns && wDNList.get(i++) == name) {
                return;
            }
        }
        // Only add if the name is allowed by the wildcard in terms of
        // ##sibling and ##defined.
        qname.uri = ns;
        qname.localpart = name;
        if (!derived.allowExpandedName(wd, qname, sgh, this)) {
            return;
        }
        // The name is allowed, add it.
        wANList.add(ns);
        wANList.add(name);
    }

    // Helper: check whether a namespace name is allowed by the wildcard
    private boolean allowNS(String ns) {
        return wType == XSWildcardDecl.NSCONSTRAINT_ANY ||
               wType == XSWildcardDecl.NSCONSTRAINT_NOT && !wNSList.contains(ns) ||
               wType == XSWildcardDecl.NSCONSTRAINT_LIST && wNSList.contains(ns);
    }

    // Helper: check whether a name is allowed by the current wildcard
    private boolean allowName(String ns, String name) {
        // Check namespace first
        if (!allowNS(ns)) {
            return false;
        }
        // Whether the name is in the disallowed list
        for (int i = 0; i < wDNList.size(); i+=2) {
            if (ns == wDNList.get(i) && name == wDNList.get(i+1)) {
                return false;
            }
        }
        // Whether the name is in the allowed list
        for (int i = 0; i < wANList.size(); i+=2) {
            if (ns == wANList.get(i) && name == wANList.get(i+1)) {
                return true;
            }
        }
        // Additional ##sibling and ##defined checks
        qname.uri = ns;
        qname.localpart = name;
        return derived.allowExpandedName(wd, qname, sgh, this);
    }

    // Helper: whether the current wildcard is empty: allows nothing
    private boolean emptyWildcard() {
        // Only when the list of namespace and the list of allowed names are
        // both empty.
        return wType == XSWildcardDecl.NSCONSTRAINT_LIST &&
        wNSList.size() == 0 && wANList.size() == 0;
    }

    // Helper: get all defined elements. For ##defined.
    private void getGlobalElements() {
        globals = new ArrayList();
        SchemaGrammar[] sgs = gb.getGrammars();
        for (int i = 0; i < sgs.length; i++) {
            addGlobals(sgs[i]);
        }
    }

    // Helper: get all defined elements in a given namespace. For ##defined.
    private void addGlobals(SchemaGrammar g) {
        XSNamedMap map = g.getComponents(XSConstants.ELEMENT_DECLARATION);
        for (int i = 0; i < map.getLength(); i++) {
            XSElementDecl e = (XSElementDecl)map.item(i);
            globals.add(e.fTargetNamespace);
            globals.add(e.fName);
        }
    }

    // Helper: get all known names in the base content model. For ##sibling.
    private void getBaseSiblings() {
        siblingsB = base.getDefinedNames(sgh);
    }

    // Helper: get all known names in the derived content model. For ##sibling.
    private void getDerivedSiblings() {
        siblingsD = derived.getDefinedNames(sgh);
    }
    
    // Helper: get a global element declaration with the specified name.
    public XSElementDecl getGlobalElementDecl(QName name) {
        SchemaGrammar sg = gb.getGrammar(name.uri);
        return sg != null ? sg.getGlobalElementDecl(name.localpart) : null;
    }

    // Entry point: check the content models passed in to the constructor.
    public boolean check() {
        // Initialize the states. Needs this before the "derived is all" check,
        // because these state arrays are always needed.
        b = base.startContentModel();
        bn = base.startContentModel();
        d = derived.startContentModel();
        dn = derived.startContentModel();

        // If derived is all, then we may be able to do better than treating
        // it as a DFA.
        if (derived instanceof XS11AllCM) {
            alld = (XS11AllCM)derived;
            // True: valid; False: invalid; null: need further checking
            Boolean res = checkAllDerived();
            if (res != null) {
                return res.booleanValue();
            }
        }

        // Helper list to gather all names that can't match the wildcard
        // in derived, because the wildcard is weaker than the elements.
        List excludedElementsCopy = new ArrayList();

        // For every state in derived, find the corresponding states in base.
        // For each pair of corresponding states, make sure all transitions
        // allowed by derived are also allowed by base. The target states
        // are also treated as "corresponding states". Continue for all such
        // pairs. When derived is at a final state, the corresponding state
        // for base must also be a final state.

        // Handling the list of state pairs as a list, not a stack.
        // Otherwise we may pop a pair off the stack, but latter find the same
        // pair, but there is no way to know we've already seen that pair.
        
        // the number of processed state pairs.
        int pos = 0;
        // initialize the unprocessed set: put initial states in it
        StatePair p = new StatePair(b, d);
        states.add(p);
        // while there is unprocessed pair
        while (pos < states.size()) {
            StatePair sp = (StatePair)states.get(pos++);
            sp.getStates(b, d);

            // for all possible element edges
            excludedElementsCopy.clear();
            indexd[0] = -1;
            while ((ed = derived.nextElementTransition(d, dn, indexd)) != null) {
                // Add them to the "can't match wildcard name" list
                excludedElementsCopy.add(ed.fTargetNamespace);
                excludedElementsCopy.add(ed.fName);
                if (ed.getScope() == XSConstants.SCOPE_GLOBAL) {
                    XSElementDecl[] eds = sgh.getSubstitutionGroup(ed, Constants.SCHEMA_VERSION_1_1);
                    for (int j = 0; j < eds.length; j++) {
                        excludedElementsCopy.add(eds[j].fTargetNamespace);
                        excludedElementsCopy.add(eds[j].fName);
                    }
                }
                // Make sure this element is allowed by the base
                if (!matchElementInBase()) {
                    return false;
                }
            }

            // for all possible wildcard edges
            indexd[0] = -1;
            while ((wd = derived.nextWildcardTransition(d, dn, indexd)) != null) {
                // Put all the stronger element names in the disallowed list
                wDNList.clear();
                wDNList.addAll(excludedElementsCopy);
                // And make sure the wildcard is allowed by the base
                if (!matchWildcardInBase()) {
                    return false;
                }
            }
        }
        
        // We processed all state pairs. Now check final states.
        return checkFinalStates();
    }

    // for an element and all elements in its sub group, check whether
    // there are matching edges in the base.
    private boolean matchElementInBase() {
        matchedHead = false;
        // Look for an element decl in the base
        if (!findElementInBase()) {
            // There is a route in the derived, but no matching
            // route in the base. not a valid restriction.
            return false;
        }
        // put this pair in the unprocessed states, if it's not there
        addState();

        // Same element decl as derived, no need to look at sub-group.
        if (matchedHead) {
            return true;
        }

        // get all sub group elements
        XSElementDecl[] eds = sgh.getSubstitutionGroup(ed, Constants.SCHEMA_VERSION_1_1);
        for (int i = 0; i < eds.length; i++) {
            ed = eds[i];
            // check whether there is a matching edge
            if (!findElementInBase()) {
                // if not, there is a route in the derived, but no matching
                // route in the base. not a valid restriction.
                return false;
            }
            // put this pair in the unprocessed states, if it's not there
            addState();
        }

        return true;
    }

    // find the matching next state in the base
    private boolean findElementInBase() {
        // for all edges in the base that leaves the base state
        // check whether it's an edge matching the base edge.
        // if it is, return the next state.
        indexb[0] = -1;
        while ((eb = base.nextElementTransition(b, bn, indexb)) != null) {
            // Base element edge, check whether it matches the derived element
            if (matchElementWithBaseElement()) {
                // If name matches, check type, nil, fixed etc.
                return checkEERestriction();
            }
        }
        // No matching element edge, try wildcards.
        indexb[0] = -1;
        while ((wb = base.nextWildcardTransition(b, bn, indexb)) != null) {
            // Base wildcard edge, check whether it matches the derived element
            qname.uri = ed.fTargetNamespace;
            qname.localpart = ed.fName;
            if (base.allowExpandedName(wb, qname, sgh, this)) {
                // If name matches, check whether there is a matching global.
                return checkEWRestriction();
            }
        }
        return false;
    }

    // whether 2 element edges match
    private boolean matchElementWithBaseElement() {
        if (eb.getName() == ed.getName() && eb.getNamespace() == ed.getNamespace()) {
            // The elements match without using sub-groups. Remember this fact
            // so that we know we don't need to check sub-groups.
            matchedHead = eb == ed;
            return true;
        }

        // if the name doesn't match, try its sub group elements
        XSElementDecl[] ebs = sgh.getSubstitutionGroup(eb, Constants.SCHEMA_VERSION_1_1);
        for (int i = 0; i < ebs.length; i++) {
            if (ebs[i].getName() == ed.getName() &&
                ebs[i].getNamespace() == ed.getNamespace()) {
                eb = ebs[i];
                return true;
            }
        }
        return false;
    }

    private boolean checkEERestriction() {
        if (eb == ed) {
            return true;
        }
        
        // 4.1 Either G.{nillable} = true  or S.{nillable} = false.
        if (!eb.getNillable() && ed.getNillable()) {
            return false;
        }

        // 4.2 Either G has no {value constraint}, or it is not fixed,
        // or S has a fixed {value constraint} with an equal value.
        if (eb.getConstraintType() == XSConstants.VC_FIXED) {
            // derived one has to have a fixed value
            if (ed.getConstraintType() != XSConstants.VC_FIXED) {
                return false;
            }

            // Get primitive kind for the fixed values. Use "string" for
            // complex type with mixed content.
            short btype, dtype;
            if (eb.fType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                btype = ((XSSimpleTypeDefinition)eb.fType).getPrimitiveType().getBuiltInKind();
            }
            else {
                XSComplexTypeDefinition complex = (XSComplexTypeDefinition)eb.fType;
                if (complex.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {
                    btype = complex.getSimpleType().getPrimitiveType().getBuiltInKind();
                }
                else {
                    btype = XSConstants.STRING_DT;
                }
            }
            if (ed.fType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                dtype = ((XSSimpleTypeDefinition)ed.fType).getPrimitiveType().getBuiltInKind();
            }
            else {
                XSComplexTypeDefinition complex = (XSComplexTypeDefinition)ed.fType;
                if (complex.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE) {
                    dtype = complex.getSimpleType().getPrimitiveType().getBuiltInKind();
                }
                else {
                    dtype = XSConstants.STRING_DT;
                }
            }
            // The 2 values must have the same primitive kind and actual value.
            // equals() checks both equality and identity.
            if (btype != dtype || !eb.fDefault.actualValue.equals(ed.fDefault.actualValue)) {
                return false;
            }
        }

        // 4.3 S.{identity-constraint definitions} is a superset of G.{identity-constraint definitions}.
        if (!checkIDConstraintRestriction(ed, eb)) {
            return false;
        }

        // 4.4 S disallows a superset of the substitutions that G does.
        if ((ed.fBlock & eb.fBlock) != eb.fBlock) {
            return false;
        }

        // 4.5 S's declared {type definition} is ·validly substitutable as
        // a restriction· for G's declared {type definition}.
        if (!xsc.checkTypeDerivationOk(ed.fType, eb.fType,
                (short)(XSConstants.DERIVATION_EXTENSION|XSConstants.DERIVATION_LIST|XSConstants.DERIVATION_UNION))) {
            return false;
        }
        
        return true;
    }

    private boolean checkIDConstraintRestriction(XSElementDecl derivedElemDecl,
            XSElementDecl baseElemDecl) {
        IdentityConstraint[] idcd = derivedElemDecl.getIDConstraints();
        IdentityConstraint[] idcb = baseElemDecl.getIDConstraints();
        int sizeb = idcb == null ? 0 : idcb.length;
        int sized = idcd == null ? 0 : idcd.length;
        // For each IDC in the base, look for the same IDC in derived
        for (int b = 0; b < sizeb; b++) {
            int d = 0;
            for (; d < sized; d++) {
                if (idcb[b] == idcd[d]) {
                    break;
                }
            }
            // Didn't find a match, error.
            if (d == idcd.length) {
                return false;
            }
        }
        return true;
    }

    // whether 2 edges match
    private boolean checkEWRestriction() {
        // 1 G is skip.
        if (wb.getProcessContents() == XSWildcardDecl.PC_SKIP) {
            return true;
        }
        qname.uri = ed.fTargetNamespace;
        qname.localpart = ed.fName;
        eb = getGlobalElementDecl(qname);
        if (eb == null) {
            // 2 G is lax and S is not skip.
            return wb.getProcessContents() == XSWildcardDecl.PC_LAX;
        }
        else {
            // 4 G and S are both Element Declarations and all of the following are true:
            return checkEERestriction();
        }
    }

    private boolean matchWildcardInBase() {
        // For each wildcard in derived, subtract stronger elements first,
        // then look for matches in base, in the order of element, wildcard,
        // then open content.
        
        // Copy the derived wildcard into fields of this class, so that we can
        // start subtract parts that have matches in the base.
        // The strong elements are already stored in the "disallowed list"
        // before this method is called.
        copyDerivedWildcard();

        // If this wildcard is the open content, also need to subtract all
        // wildcard edges, because open content is weaker than the wildcards.
        if (derived.isOpenContent(wd)) {
            int[] idx = new int[1];
            idx[0] = -1;
            XSWildcardDecl wd1;
            // Subtract all wildcard edges. Stop if the wildcard becomes empty
            while (!emptyWildcard() && (wd1 = derived.nextWildcardTransition(d, dn, idx)) != null) {
                if (wd1 != wd) {
                    subtractWildcard(wd1, true);
                }
            }
        }
        
        // Go through all base element transitions
        indexb[0] = -1;
        while ((eb = base.nextElementTransition(b, bn, indexb)) != null) {
            // True: good match; False: bad match; null: no match
            Boolean res = checkWERestriction();
            if (res != null) {
                if (!res.booleanValue()) {
                    return false;
                }
                else {
                    // The base element name is added to the disallow list
                    // in checkWERestriction();
                    // Add good match to the state pair list
                    addState();
                }
            }
            // Also attempt to match sub-group members in base. Same process.
            if (eb.getScope() == XSConstants.SCOPE_GLOBAL) {
                XSElementDecl[] ebs = sgh.getSubstitutionGroup(eb, Constants.SCHEMA_VERSION_1_1);
                for (int j = 0; j < ebs.length; j++) {
                    this.eb = ebs[j];
                    res = checkWERestriction();
                    if (res != null) {
                        if (!res.booleanValue()) {
                            return false;
                        }
                        else {
                            addState();
                        }
                    }
                }
            }
        }

        // Now go through all base wildcard transitions
        indexb[0] = -1;
        while (!emptyWildcard() && (wb = base.nextWildcardTransition(b, bn, indexb)) != null) {
            // Returns whether there was any overlap that got subtracted
            if (subtractWildcard(wb, false)) {
                // If there was overlap (match), check "process contents"
                if (wd.weakerProcessContents(wb)) {
                    return false;
                }
                // Add good match to the state pair list
                addState();
            }
        }
        
        // The emtire wildcard must have been matched. If there's anything left,
        // it's allowed by derived but not base. Error.
        if (!emptyWildcard()) {
            return false;
        }
        
        return true;
    }
    
    // Base element and derived wildcard.
    // True: good match; False: bad match; null: no match
    private Boolean checkWERestriction() {
        // Whether element name is allowed by the wildcard
        if (!allowName(eb.fTargetNamespace, eb.fName)) {
            return null;
        }
        // Skip wildcard in derived can't restrict element in base.
        if (wd.fProcessContents == XSWildcardDecl.PC_SKIP) {
            return Boolean.FALSE;
        }
        // Must be able to find a global decl. Otherwise wildcard in derived
        // can't restrict element in base.
        ed = getGlobalElementDecl(qname);
        if (ed == null) {
            return Boolean.FALSE;
        }
        // The global element decl must match the base element
        if (ed != eb && !checkEERestriction()) {
            return Boolean.FALSE;
        }
        // Good match. Subtract the name from the wildcard.
        wDNList.add(eb.fTargetNamespace);
        wDNList.add(eb.fName);
        return Boolean.TRUE;
    }

    // Subtract the specified wildcard from the wildcard in this class.
    // Returns whether the 2 wildcards had any overlap.
    private boolean subtractWildcard(XSWildcardDecl wc, boolean isDerived) {
        boolean changed = false;
        
        // Because of UPA, it's not possible to add a name to the allowed list
        // when subtracting a wildcard, then remove that same name when
        // subtracting another wildcard, except for when the second wildcard
        // is the open content. So only need to clear the allowed list
        // when subtracting the open content.
        // Also because open content is the last wildcard to subtract, all
        // allowed names must be allowed by the base open content. If there is
        // a name that's not allowed, we can return quickly, leaving this
        // wildcard as non-empty, which will cause an error later.
        if (!isDerived && base.isOpenContent(wc) && wANList.size() > 0) {
            for (int i = 0; i < wANList.size();) {
                qname.uri = (String)wANList.get(i++);
                qname.localpart = (String)wANList.get(i++);
                // A name allowed by derived wildcard but not by base open
                // content. Quick return and detect the error later.
                if (!base.allowExpandedName(wc, qname, sgh, this)) {
                    return false;
                }
            }
            // All allowed names have base matches. Clear them.
            wANList.clear();
            changed = true;
        }

        // First add "disallowedNames" in "sub" to the allowed list.
        
        // All disallowed names in the "sub" wildcard are added as allowed names
        // if they are allowed by this wildcard.
        int count = wc.fDisallowedNamesList == null ? 0 : wc.fDisallowedNamesList.length;
        for (int i = 0; i < count; i++) {
            addAN(wc.fDisallowedNamesList[i].uri, wc.fDisallowedNamesList[i].localpart);
        }
        // If "sub" wildcard has ##defined, and this doesn't, add all global
        // element names to the allowed list, if this wildcard allows them.
        if (wc.fDisallowedDefined && !wDD) {
            if (globals == null) {
                getGlobalElements();
            }
            for (int i = 0; i < globals.size();) {
                String ns = (String)globals.get(i++);
                String name = (String)globals.get(i++);
                if (wc.allowNamespace(ns)) {
                    addAN(ns, name);
                }
            }
        }
        // If "sub" wildcard has ##sibling
        if (wc.fDisallowedSibling) {
            if (!isDerived) {
                // Add all names known to the base to the allowed list
                if (siblingsB == null) {
                    getBaseSiblings();
                }
                for (int i = 0; i < siblingsB.size();) {
                    String ns = (String)siblingsB.get(i++);
                    String name = (String)siblingsB.get(i++);
                    if (wc.allowNamespace(ns)) {
                        addAN(ns, name);
                    }
                }
            }
            else if (!wDS){
                // If "sub" is also from derived, don't need to subtract
                // if this wildcard also has ##sibling. Otherwise add all
                // known names in derived to the allowed list
                if (siblingsD == null) {
                    getDerivedSiblings();
                }
                for (int i = 0; i < siblingsD.size();) {
                    String ns = (String)siblingsD.get(i++);
                    String name = (String)siblingsD.get(i++);
                    if (wc.allowNamespace(ns)) {
                        addAN(ns, name);
                    }
                }
            }
        }
        
        // Now deal with namespace. "sub" is any, remove all namespaces.
        if (wc.getConstraintType() == XSWildcardDecl.NSCONSTRAINT_ANY) {
            wType = XSWildcardDecl.NSCONSTRAINT_LIST;
            wNSList.clear();
            return true;
        }

        if (wc.getConstraintType() == XSWildcardDecl.NSCONSTRAINT_NOT) {
            // both "sub" and this are "not". Only keep namespaces in "sub"'s
            // "not" list but not in my "not" list.
            if (wType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                wType = XSWildcardDecl.NSCONSTRAINT_LIST;
                wNSListTemp.clear();
                for (int i = 0; i < wc.fNamespaceList.length; i++) {
                    if (!wNSList.contains(wc.fNamespaceList[i])) {
                        wNSListTemp.add(wc.fNamespaceList[i]);
                    }
                }
                wNSList.clear();
                wNSList.addAll(wNSListTemp);
                return true;
            }

            // This is "list" and "sub" is "not". Only namespaces in "sub"'s
            // "not" list and also in my "yes" list.
            wNSListTemp.clear();
            for (int i = 0; i < wc.fNamespaceList.length; i++) {
                if (wNSList.contains(wc.fNamespaceList[i])) {
                    wNSListTemp.add(wc.fNamespaceList[i]);
                }
            }

            // No namespace was removed.
            if (wNSList.size() == wNSListTemp.size()) {
                return changed;
            }
            
            // Some namespaces were removed.
            wNSList.clear();
            wNSList.addAll(wNSListTemp);
            
            return true;
        }
        
        if (wType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            // "sub" is list, this is "not". Add "sub"'s list to my "not" list.
            for (int i = 0; i < wc.fNamespaceList.length; i++) {
                if (!wNSList.contains(wc.fNamespaceList[i])) {
                    wNSList.add(wc.fNamespaceList[i]);
                    changed = true;
                }
            }
        }
        else {
            // Both are lists. Remove "sub"'s list from my list.
            for (int i = 0; i < wc.fNamespaceList.length; i++) {
                if (wNSList.remove(wc.fNamespaceList[i])) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    // For each pair of matching states, if derived is final, base must be final.
    private boolean checkFinalStates() {
        for (int i = 0 ; i < states.size(); i++) {
            StatePair sp = (StatePair)states.get(i);
            sp.getStates(b, d);
            if (derived.endContentModel(d) && !base.endContentModel(b)) {
                return false;
            }
        }
        return true;
    }

    // A pair of base/derived states.
    private static class StatePair {
        private final int[] states;
        public StatePair(int[] b, int[] d) {
            this.states = new int[b.length + d.length];
            System.arraycopy(b, 0, states, 0, b.length);
            System.arraycopy(d, 0, states, states.length - d.length, d.length);
        }
        private void set(int[] b, int[] d) {
            System.arraycopy(b, 0, states, 0, b.length);
            System.arraycopy(d, 0, states, states.length - d.length, d.length);
        }
        private void getStates(int[] b, int[] d) {
            System.arraycopy(states, 0, b, 0, b.length);
            System.arraycopy(states, states.length - d.length, d, 0, d.length);
        }
        // Equal objects must have same hash code
        public int hashCode() {
            int res = 0;
            for (int i = 0; i < states.length; i++) {
                res = res * 7 + states[i];
            }
            return res;
        }
        // it equals to another object if and only if they both have the same
        // base and derived states
        public boolean equals(Object o) {
            if (!(o instanceof StatePair))
                return false;
            StatePair sp = (StatePair)o;
            return Arrays.equals(states, sp.states);
        }
    }

    // Optimization for when derived is "all", to avoid turning it into DFA
    private Boolean checkAllDerived() {
        // Derived is "all"; base is empty
        if (base instanceof XSEmptyCM) {
            Boolean ret = checkAllEmpty();
            if (ret != null) {
                return ret;
            }
            // Base must be an empty with open content. It's turned into an
            // all with open content. Fall through.
        }

        if (base instanceof XS11AllCM) {
            // Both are "all"
            allb = (XS11AllCM)base;
            // True: valid; False; invalid: null: check further
            Boolean ret = checkAllAll();
            // Even thought checkAllAll() didn't give a definitive answer, it
            // may have simplified the content models. Use the new ones.
            if (ret == null) {
                base = allb;
                derived = alld;
                // Treating the all derived as DFA. Check whether that'll
                // consume too much memory.
                cmb.testOccurrences(alld.calOccurs());
            }
            return ret;
        }
        else { // if (base instanceof XSDFACM)
            // Derived is "all"; base is DFA
            dfab = (XSDFACM)base;
            // True: valid; False; invalid: null: check further
            Boolean ret = checkAllDFA();
            // Even thought checkAllDFA() didn't give a definitive answer, it
            // may have simplified the content models. Use the new ones.
            if (ret == null) {
                base = dfab;
                derived = alld;
                // Treating the all derived as DFA. Check whether that'll
                // consume too much memory.
                cmb.testOccurrences(alld.calOccurs());
            }
            return ret;
        }
    }

    private Boolean checkAllEmpty() {
        // If base (empty) has an open content, turn it into an "all".
        int[] idx = new int[]{-1};
        if (base.nextWildcardTransition(b, bn, idx) != null) {
            base = new XS11AllCM(false, 0, null, ((XSEmptyCM)base).getOpenContent());
            return null;
        }
        // Base allows nothing. Derived must be empty too.
        return Boolean.valueOf(derived.nextElementTransition(d, dn, idx) == null &&
                derived.nextWildcardTransition(d, dn, idx) == null);
    }    

    // Optimize for the common case where base has elements and derived has
    // same-named elements. Try to reduce occurrence from derive, or remove
    // the elements entirely. Don't try too hard for wildcards.
    private Boolean checkAllAll() {
        if (allb.getOpenContent() != null &&
                allb.getOpenContent().fMode == XSOpenContentDecl.MODE_SUFFIX &&
                alld.getOpenContent() != null &&
                alld.getOpenContent().fMode == XSOpenContentDecl.MODE_INTERLEAVE) {
            // Base (e)+suffix, derived (e)+interleave. If we remove "e", we
            // may think the derivation is OK. Don't optimize. Fall back.
            return null;
        }

        if (siblingsD == null) {
            getDerivedSiblings();
        }

        // TODO
//        List excludedUnboundedElementsCopy = new ArrayList();
//        indexd[0] = -1;
//        while ((ed = derived.nextElementTransition(d, dn, indexd)) != null) {
//            if (alld.isUnbounded(indexd[0])) {
//                excludedUnboundedElementsCopy.add(ed.fTargetNamespace);
//                excludedUnboundedElementsCopy.add(ed.fName);
//            }
//            if (ed.getScope() == XSConstants.SCOPE_GLOBAL) {
//                XSElementDecl[] eds = sgh.getSubstitutionGroup(ed, Constants.SCHEMA_VERSION_1_1);
//                for (int i = 0; i < eds.length; i++) {
//                    if (alld.isUnbounded(indexd[0])) {
//                        excludedUnboundedElementsCopy.add(ed.fTargetNamespace);
//                        excludedUnboundedElementsCopy.add(ed.fName);
//                    }
//                }
//            }
//        }
        
        if (alld.hasOptionalContent()) {
            if (!checkOptionalContent()) {
                return Boolean.FALSE;
            }
        }
        else if (allb.hasOptionalContent()) {
            // base: (a&)? derived: (a?&) should be OK.
            // Not easy to detect; fall back
            return null;
        }

        // We've dealt with open content and optional content mismatches.
        // Now we can start reducing element occurrences. Ideally to 0.
        
        // The following scenario is safe to be reduced. Not sure about others.
        // 1. A base element B matches one or more derived elements
        // 2. B doesn't match any of the derived wildcards
        // 3. B matches the entirety of the derived elements. As a consequence,
        //    the derived elements don't match any other base element.
        // 4.1 B matches exactly one derived element, or
        // 4.2 B has big enough maxOccurs for all derived elements.
        // Then we can remove the base element and all the derived ones.
        // #2 is so that we don't need to worry about
        //    base: ((ns1:a|ns2:b) & ns2:*) derived (ns1:a & ns2:*)
        // #3 is so that we don't need to worry about
        //    base: ((a|b) & c) derived (a & (b|c))
        // #4 is so that we don't need to worry about
        //    base: ((a|b) & *) derived (a & b & *)
        //    but this is OK: (a & *) derived (a{2} & *)
        // We don't need to worry about if derived elements also match base
        // wildcard, because the wildcard is weaker and the derived element
        // always matches the base element (which has sufficient maxOccurs).
        
        // TODO: we may be able to do more, but element-element should be the
        // common case. May want to consider wildcard-element and
        // wildcard-wildcard, but need to take into account elements that may
        // have made the wildcards weaker.
        // e.g. reduce wildcards? open content?
        // e.g. base (wc*&) derived (e1&e2...) optimize
        
        // For each derived element, -1: initial; -2: no matching base element;
        // >= 0 index of matching base element
        int[] matchD = derived.startContentModel();
        // For each base element: 0: not used; < 0: can't be used;
        // > 0: number of derived elements that match this base element
        int[] usedB = base.startContentModel();
        indexd[0] = -1;
        while ((ed = derived.nextElementTransition(d, dn, indexd)) != null) {
            // For each derived element, try to find matching base elements.
            // Checking #1 and #3 above.
            matchD[indexd[0]] = -1;
            matchedHead = false;
            if (!matchEE(matchD, usedB)) {
                // Bad match, return the error immediately
                return Boolean.FALSE;
            }
            if (!matchedHead) {
                if (ed.getScope() == XSConstants.SCOPE_GLOBAL) {
                    XSElementDecl[] eds = sgh.getSubstitutionGroup(ed, Constants.SCHEMA_VERSION_1_1);
                    for (int i = 0; i < eds.length; i++) {
                        ed = eds[i];
                        if (!matchEE(matchD, usedB)) {
                            // Bad match, return the error immediately
                            return Boolean.FALSE;
                        }
                    }
                }
            }
        }
        
        // Match derived wildcards with base elements, to make sure #2 above
        // is satisfied.
        // The wildcards are weaker than elements. Remove element names.
        wDNList.clear();
        wDNList.addAll(siblingsD);
        indexd[0] = -1;
        while ((wd = derived.nextWildcardTransition(d, dn, indexd)) != null) {
            if (!matchWE(usedB)) {
                // Bad match, return the error immediately
                return Boolean.FALSE;
            }
        }
        
        // Now gather maxOccurs from derived to check for #4
        // For each base element, take the sum of all matching derived elements.
        int[] min = base.startContentModel(), max = base.startContentModel();
        indexd[0] = -1;
        while ((ed = derived.nextElementTransition(d, dn, indexd)) != null) {
            indexb[0] = matchD[indexd[0]];
            if (indexb[0] >= 0) {
                alld.collectOccurs(min, max, indexb[0], indexd[0]);
            }
        }
        
        // About to reduce min/maxOccurs. Make a copy of the content models.
        allb = allb.copy();
        alld = alld.copy();
        // Check whether the base can accommodate the total of the derived.
        if (!allb.removeAsBase(min, max, usedB)) {
            // If the base has more minOccurs than sum of derived, then
            // bad derivation. The minOccurs can't be satisfied by other
            // components (i.e wildcards), because the wildcards can take
            // names other than that of the base element.
            return Boolean.FALSE;
        }
        // For those that pass #4, remove corresponding derived elements.
        alld.removeAsDerived(max, usedB, matchD);

        // Whatever is left will be handled by the default DFA path.
        return null;
    }
    private boolean checkOptionalContent() {
        // Called when derived has optional content (a & b)?, so allows empty.
        // Need to make sure that
        // 1. Base also allowed the empty sequence
        // 2. If derived allows the first element to match the open content,
        //    leaving the "all" content as empty, then base doesn't match that
        //    element using an entry in the "all" content and require other
        //    things in the "all" content to also be present.
        
        // Check #1: Make sure base also allows empty.
        int totalMin = allb.totalMin();
        if (!allb.hasOptionalContent() && totalMin != 0) {
            return false;
        }

        // If derived doesn't have an open content, don't need to check #2.
        XSOpenContentDecl oc = alld.getOpenContent();
        wd = oc == null ? null : oc.fWildcard;
        if (wd == null) {
            return true;
        }
        
        // Calculate the effective open content for first element, by
        // subtracting all elements/wildcards from the "all" content.
        wDNList.clear();
        wDNList.addAll(siblingsD);
        copyDerivedWildcard();
        int[] idx = new int[1];
        idx[0] = -1;
        XSWildcardDecl wd1;
        while (!emptyWildcard() && (wd1 = derived.nextWildcardTransition(d, dn, idx)) != null) {
            if (wd1 != wd) {
                subtractWildcard(wd1, true);
            }
        }
        // If the "first element" open content is empty, don't need #2.
        if (emptyWildcard()) {
            return true;
        }

        // Now "first element" open content is not empty, need to check whether
        // it matches any element/wildcard in base "all" content. If it does,
        // then the matching component must not have min>1 and all the other
        // components must have min=0, to not impose more "required" elements.
        indexb[0] = -1;
        while ((eb = base.nextElementTransition(b, bn, indexb)) != null) {
            int min = allb.min(indexb[0]);
            if (allowName(eb.fTargetNamespace, eb.fName)) {
                // This element matches the derived open content. Make sure
                // no additional elements are required.
                if (min > 1 || min < totalMin) {
                    return false;
                }
                // No need to check sub-group, as they have same "min".
                continue;
            }
            if (eb.getScope() == XSConstants.SCOPE_GLOBAL) {
                XSElementDecl[] ebs = sgh.getSubstitutionGroup(eb, Constants.SCHEMA_VERSION_1_1);
                for (int j = 0; j < ebs.length; j++) {
                    this.eb = ebs[j];
                    if (allowName(eb.fTargetNamespace, eb.fName)) {
                        // This element matches the derived open content. Make
                        // sure no additional elements are required.
                        if (min > 1 || min < totalMin) {
                            return false;
                        }
                        break;
                    }
                }
            }
        }

        // Now match "first element" open content with base wildcards.
        indexb[0] = -1;
        while ((wb = base.nextWildcardTransition(b, bn, indexb)) != null) {
            if (!base.isOpenContent(wb) && overlap()) {
                int min = allb.min(indexb[0]);
                // This wildcards matches the derived open content. Make
                // sure no additional elements are required.
                if (min > 1 || min < totalMin) {
                    return false;
                }
            }
        }
        
        // Now we are certain that when the derived optional "all" content
        // is not used, the base do not have any required element.
        return true;
    }
    private boolean overlap() {
        // Check whether the derived open content wildcard overlaps with the
        // base wildcard "wb".
        
        // Overlap if names in the allowed list are allowed by "wb".
        for (int i = 0; i < wANList.size();) {
            qname.uri = (String)wANList.get(i++);
            qname.localpart = (String)wANList.get(i++);
            if (base.allowExpandedName(wb, qname, sgh, this)) {
                return true;
            }
        }
        // Overlap if namespaces overlap.
        // Both are any/not. Always overlap.
        if (wType == XSWildcardDecl.NSCONSTRAINT_ANY ||
                wb.fType == XSWildcardDecl.NSCONSTRAINT_ANY ||
                wType == XSWildcardDecl.NSCONSTRAINT_NOT &&
                wb.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            return true;
        }
        // Both are lists. Overlap if the namespace lists overlap.
        if (wType == XSWildcardDecl.NSCONSTRAINT_LIST &&
                wb.fType == XSWildcardDecl.NSCONSTRAINT_LIST) {
            for (int i = 0; i < wb.fNamespaceList.length; i++) {
                if (wNSList.contains(wb.fNamespaceList[i])) {
                    return true;
                }
            }
            return false;
        }
        // Derived is "not" and "wb" is list. Overlap if a namespace in "wb"
        // list is not excluded from derived.
        if (wType == XSWildcardDecl.NSCONSTRAINT_NOT) {
            for (int i = 0; i < wb.fNamespaceList.length; i++) {
                if (!wNSList.contains(wb.fNamespaceList[i])) {
                    return true;
                }
            }
            return false;
        }
        // Derived is list and "wb" is "not". Overlap if a namespace in derived
        // list is not excluded from "wb".
        for (int i = 0; i < wNSList.size(); i++) {
            String ns = (String)wNSList.get(i);
            int j = 0;
            for (; j < wb.fNamespaceList.length; j++) {
                if (ns == null && wb.fNamespaceList[j] == null ||
                    ns != null && ns.equals(wb.fNamespaceList[j])) {
                    break;
                }
            }
            // A namespace in "derived" is not excluded from "wb". Overlap.
            if (j == wb.fNamespaceList.length) {
                return true;
            }
        }
        return false;
    }
    // Similar to findElementInBase, but need to handle the match/used arrays.
    private boolean matchEE(int[] matchD, int[] usedB) {
        // Look for a base element to match "ed"
        int id = indexd[0];
        indexb[0] = -1;
        while ((eb = base.nextElementTransition(b, bn, indexb)) != null) {
            int ib = indexb[0];
            if (eb.getName() == ed.getName() && eb.getNamespace() == ed.getNamespace()) {
                // If derived had a match before, e.g. its sub-group member
                // matched an element before, and the head now also matches
                // an element. Then make sure the 2 base element are the same,
                // otherwise we break #3: same derived different base.
                if (matchD[id] != -1 && matchD[id] != ib) {
                    // Mark both base elements as "not usable".
                    usedB[matchD[id]] = -1;
                    usedB[ib] = -1;
                    // This derived element isn't usable either. Return.
                    // But this is not an error, so return true.
                    return true;
                }
                // Need to make sure the base is usable.
                if (usedB[ib] < 0) {
                    // Remember that the derived element had a match.
                    // In case this element matches a different base,
                    // then we know we need to mark that other base
                    // as not usable.
                    matchD[id] = ib;
                    return true;
                }
                // Record the match.
                if (matchD[id] == -1) {
                    matchD[id] = ib;
                    // Remember how many matches the base element has.
                    // Only do so if this is the first match, so that we only
                    // count each base element once.
                    usedB[ib]++;
                }
                // Head match. No need to check sub-group members.
                matchedHead = eb == ed;
                // Must satisfy other element-element restriction requirements.
                return checkEERestriction();
            }
            else {
                // if the name doesn't match, try its sub group elements
                XSElementDecl[] ebs = sgh.getSubstitutionGroup(eb, Constants.SCHEMA_VERSION_1_1);
                for (int i = 0; i < ebs.length; i++) {
                    // found a name match, check properties of the decls
                    if (ebs[i].getName() == ed.getName() &&
                            ebs[i].getNamespace() == ed.getNamespace()) {
                        if (matchD[id] != -1 && matchD[id] != ib) {
                            // #3: Same derived matches multiple base.
                            // Mark both base elements as "not usable".
                            if (matchD[id] >= 0) {
                                usedB[matchD[id]] = -1;
                            }
                            usedB[ib] = -1;
                            // This derived element isn't usable either. Return.
                            // But this is not an error, so return true.
                            return true;
                        }
                        // Need to make sure the base is usable.
                        if (usedB[ib] < 0) {
                            // Remember that the derived element had a match.
                            // In case this element matches a different base,
                            // then we know we need to mark that other base
                            // as not usable.
                            matchD[id] = ib;
                            return true;
                        }
                        // Record the match. Also need to make sure the base is usable.
                        if (matchD[id] == -1) {
                            matchD[id] = ib;
                            // Remember how many matches the base element has.
                            // Only do so if this is the first match, so that we only
                            // count each base element once.
                            usedB[ib]++;
                        }
                        eb = ebs[i];
                        return checkEERestriction();
                    }
                }
            }
        }
        // Derived element has no match in base.
        if (matchD[id] >= 0) {
            // If this element had a match, then #3 is not satisfied.
            // Mark the base as not usable.
            usedB[matchD[id]] = -1;
        }
        // Remember the fact that this element had a no-match. If we find a
        // match for it (its sub-group member) later, need to mark the matching
        // base as not usable.
        matchD[id] = -2;
        
        return true;
    }
    // Similar to matchWildcardInBase, but only deal with base element,
    // and need to handle the used array.
    private boolean matchWE(int[] usedB) {
        indexb[0] = -1;
        while ((eb = base.nextElementTransition(b, bn, indexb)) != null) {
            int ib = indexb[0];
            // Only interested in base elements that had derived matches.
            // Other elements will be handled when we fall back to DFA.
            if (usedB[ib] <= 0) {
                continue;
            }
            // True: good match; False: bad match; null: no match
            Boolean res = checkWERestriction();
            if (res != null) {
                if (!res.booleanValue()) {
                    return false;
                }
                else {
                    // Same base element matches both element and wildcard
                    // in derived. Fallback to DFA.
                    usedB[ib] = -1;
                    return true;
                }
            }

            // No match. Look at sub-group
            if (eb.getScope() == XSConstants.SCOPE_GLOBAL) {
                XSElementDecl[] ebs = sgh.getSubstitutionGroup(eb, Constants.SCHEMA_VERSION_1_1);
                for (int i = 0; i < ebs.length; i++) {
                    this.eb = ebs[i];
                    res = checkWERestriction();
                    if (res != null) {
                        if (!res.booleanValue()) {
                            return false;
                        }
                        else {
                            // Same base element matches both element and wildcard
                            // in derived. Fallback to DFA.
                            usedB[ib] = -1;
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private Boolean checkAllDFA() {
        // Don't optimize for this case yet. It's a strange derivation to
        // restrict a sequence/choice content model to an "all".
        return null;
    }    

}
