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
import java.util.Stack;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * Constraints shared by traversers and validator
 * 
 * @xerces.internal
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public abstract class XSConstraints {

    // IHR: Visited on 2006-11-17
    // Added a boolean return value to particleValidRestriction (it was a void function)
    // to help the checkRecurseLax to know when expansion has happened and no order is required
    // (IHR@xbrl.org) (Ignacio@Hernandez-Ros.com)

    static final int OCCURRENCE_UNKNOWN = SchemaSymbols.OCCURRENCE_UNBOUNDED-1;
    // TODO: using 1.0 xs:string
    static final XSSimpleType STRING_TYPE = (XSSimpleType)SchemaGrammar.getS4SGrammar(Constants.SCHEMA_VERSION_1_0).getGlobalTypeDecl(SchemaSymbols.ATTVAL_STRING);

    private static XSParticleDecl fEmptyParticle = null;
    public static XSParticleDecl getEmptySequence() {
        if (fEmptyParticle == null) {
            XSModelGroupImpl group = new XSModelGroupImpl();
            group.fCompositor = XSModelGroupImpl.MODELGROUP_SEQUENCE;
            group.fParticleCount = 0;
            group.fParticles = null;
            group.fAnnotations = XSObjectListImpl.EMPTY_LIST;
            XSParticleDecl particle = new XSParticleDecl();
            particle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
            particle.fValue = group;
            particle.fAnnotations = XSObjectListImpl.EMPTY_LIST;
            fEmptyParticle = particle;
       }
        return fEmptyParticle;
    }

    static final XSConstraints XS_1_0_CONSTRAINTS = new XS10Constraints(Constants.SCHEMA_VERSION_1_0);
    static final XSConstraints XS_1_0_CONSTRAINTS_EXTENDED = new XS10Constraints(Constants.SCHEMA_VERSION_1_0_EXTENDED);
    static final XSConstraints XS_1_1_CONSTRAINTS = new XS11Constraints();

    private final XSComplexTypeDecl fAnyType;
    protected final short fSchemaVersion;

    protected XSConstraints(XSComplexTypeDecl anyType, short schemaVersion) {
        fAnyType = anyType;
        fSchemaVersion = schemaVersion;
    }
    
    final public short getSchemaVersion() {
        return fSchemaVersion;
    }
    
    public boolean isTypeTablesEquivalent(XSElementDecl elementDecl1, XSElementDecl elementDecl2) {
        return true;
    }

    /**
     * check whether derived is valid derived from base, given a subset
     * of {restriction, extension}.B
     */
    public boolean checkTypeDerivationOk(XSTypeDefinition derived, XSTypeDefinition base, short block) {
        // if derived is anyType, then it's valid only if base is anyType too
        if (derived == fAnyType)
            return derived == base;
        // if derived is anySimpleType, then it's valid only if the base
        // is ur-type
        if (derived == SchemaGrammar.fAnySimpleType) {
            return (base == fAnyType ||
                    base == SchemaGrammar.fAnySimpleType);
        }

        // if derived is simple type
        if (derived.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            // if base is complex type
            if (base.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                // if base is anyType, change base to anySimpleType,
                // otherwise, not valid
                if (base == fAnyType)
                    base = SchemaGrammar.fAnySimpleType;
                else
                    return false;
            }
            return checkSimpleDerivation((XSSimpleType)derived,
                    (XSSimpleType)base, block);
        } 
        else {
            return checkComplexDerivation((XSComplexTypeDecl)derived, base, block);
        }
    }

    /**
     * check whether simple type derived is valid derived from base,
     * given a subset of {restriction, extension}.
     */
    public boolean checkSimpleDerivationOk(XSSimpleType derived, XSTypeDefinition base, short block) {
        // if derived is anySimpleType, then it's valid only if the base
        // is ur-type
        if (derived == SchemaGrammar.fAnySimpleType) {
            return (base == fAnyType ||
                    base == SchemaGrammar.fAnySimpleType);
        }

        // if base is complex type
        if (base.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            // if base is anyType, change base to anySimpleType,
            // otherwise, not valid
            if (base == fAnyType)
                base = SchemaGrammar.fAnySimpleType;
            else
                return false;
        }
        return checkSimpleDerivation((XSSimpleType)derived,
                (XSSimpleType)base, block);
    }

    /**
     * check whether complex type derived is valid derived from base,
     * given a subset of {restriction, extension}.
     */
    public boolean checkComplexDerivationOk(XSComplexTypeDecl derived, XSTypeDefinition base, short block) {
        // if derived is anyType, then it's valid only if base is anyType too
        if (derived == fAnyType)
            return derived == base;
        return checkComplexDerivation((XSComplexTypeDecl)derived, base, block);
    }

    /**
     * Note: this will be a private method, and it assumes that derived is not
     *       anySimpleType, and base is not anyType. Another method will be
     *       introduced for public use, which will call this method.
     */
    private boolean checkSimpleDerivation(XSSimpleType derived, XSSimpleType base, short block) {
        // 1 They are the same type definition.
        if (derived == base)
            return true;

        // 2 All of the following must be true:
        // 2.1 restriction is not in the subset, or in the {final} of its own {base type definition};
        if ((block & XSConstants.DERIVATION_RESTRICTION) != 0 ||
                (derived.getBaseType().getFinal() & XSConstants.DERIVATION_RESTRICTION) != 0) {
            return false;
        }

        // 2.2 One of the following must be true:
        // 2.2.1 D's base type definition is B.
        XSSimpleType directBase = (XSSimpleType)derived.getBaseType();
        if (directBase == base)
            return true;

        // 2.2.2 D's base type definition is not the simple ur-type definition and is validly derived from B given the subset, as defined by this constraint.
        if (directBase != SchemaGrammar.fAnySimpleType &&
                checkSimpleDerivation(directBase, base, block)) {
            return true;
        }

        // 2.2.3 D's {variety} is list or union and B is the simple ur-type definition.
        if ((derived.getVariety() == XSSimpleType.VARIETY_LIST ||
                derived.getVariety() == XSSimpleType.VARIETY_UNION) &&
                base == SchemaGrammar.fAnySimpleType) {
            return true;
        }

        // 2.2.4 B's {variety} is union and D is validly derived from a type definition in B's {member type definitions} given the subset, as defined by this constraint.
        if (base.getVariety() == XSSimpleType.VARIETY_UNION) {
            if (checkEmptyFacets(base)) {
                XSObjectList subUnionMemberDV = base.getMemberTypes();
                int subUnionSize = subUnionMemberDV.getLength();
                for (int i=0; i<subUnionSize; i++) {
                    base = (XSSimpleType)subUnionMemberDV.item(i);
                    if (checkSimpleDerivation(derived, base, block)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Note: this will be a private method, and it assumes that derived is not
     *       anyType. Another method will be introduced for public use,
     *       which will call this method.
     */
    private boolean checkComplexDerivation(XSComplexTypeDecl derived, XSTypeDefinition base, short block) {
        // 2.1 B and D must be the same type definition.
        if (derived == base)
            return true;

        // 1 If B and D are not the same type definition, then the {derivation method} of D must not be in the subset.
        if ((derived.fDerivedBy & block) != 0)
            return false;

        // 2 One of the following must be true:
        XSTypeDefinition directBase = derived.fBaseType;
        // 2.2 B must be D's {base type definition}.
        if (directBase == base)
            return true;

        // 2.3 All of the following must be true:
        // 2.3.1 D's {base type definition} must not be the ur-type definition.
        if (directBase == fAnyType ||
                directBase == SchemaGrammar.fAnySimpleType) {
            return false;
        }

        // 2.3.2 The appropriate case among the following must be true:
        // 2.3.2.1 If D's {base type definition} is complex, then it must be validly derived from B given the subset as defined by this constraint.
        if (directBase.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
            return checkComplexDerivation((XSComplexTypeDecl)directBase, base, block);

        // 2.3.2.2 If D's {base type definition} is simple, then it must be validly derived from B given the subset as defined in Type Derivation OK (Simple) (3.14.6).
        if (directBase.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            // if base is complex type
            if (base.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                // if base is anyType, change base to anySimpleType,
                // otherwise, not valid
                if (base == fAnyType)
                    base = SchemaGrammar.fAnySimpleType;
                else
                    return false;
            }
            return checkSimpleDerivation((XSSimpleType)directBase,
                    (XSSimpleType)base, block);
        }

        return false;
    }

    /**
     * check whether a value is a valid default for some type
     * returns the compiled form of the value
     * The parameter value could be either a String or a ValidatedInfo object
     */
    public Object ElementDefaultValidImmediate(XSTypeDefinition type, String value, ValidationContext context, ValidatedInfo vinfo) {

        XSSimpleType dv = null;

        // e-props-correct
        // For a string to be a valid default with respect to a type definition the appropriate case among the following must be true:
        // 1 If the type definition is a simple type definition, then the string must be valid with respect to that definition as defined by String Valid (3.14.4).
        if (type.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            dv = (XSSimpleType)type;
        }

        // 2 If the type definition is a complex type definition, then all of the following must be true:
        else {
            // 2.1 its {content type} must be a simple type definition or mixed.
            XSComplexTypeDecl ctype = (XSComplexTypeDecl)type;
            // 2.2 The appropriate case among the following must be true:
            // 2.2.1 If the {content type} is a simple type definition, then the string must be valid with respect to that simple type definition as defined by String Valid (3.14.4).
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                dv = ctype.fXSSimpleType;
            }
            // 2.2.2 If the {content type} is mixed, then the {content type}'s particle must be emptiable as defined by Particle Emptiable (3.9.6).
            else if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                if (!((XSParticleDecl)ctype.getParticle()).emptiable())
                    return null;
            }
            else {
                return null;
            }
        }

        // get the simple type declaration, and validate
        Object actualValue = null;
        if (dv == null) {
            // complex type with mixed. to make sure that we store correct
            // information in vinfo and return the correct value, we use
            // "string" type for validation
            dv = STRING_TYPE;
        }
        try {
            // validate the original lexical rep, and set the actual value
            actualValue = dv.validate(value, context, vinfo);
            // validate the canonical lexical rep
            if (vinfo != null)
                actualValue = dv.validate(vinfo.stringValue(), context, vinfo);
        } catch (InvalidDatatypeValueException ide) {
            return null;
        }

        return actualValue;
    }

    void reportSchemaError(XMLErrorReporter errorReporter,
            SimpleLocator loc,
            String key, Object[] args) {
        if (loc != null) {
            errorReporter.reportError(loc, XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_ERROR);
        }
        else {
            errorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_ERROR);
        }
    }

    /**
     * used to check the 3 constraints against each complex type
     * (should be each model group):
     * Unique Particle Attribution, Particle Derivation (Restriction),
     * Element Declrations Consistent.
     */
    public void fullSchemaChecking(XSGrammarBucket grammarBucket,
            SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder,
            XMLErrorReporter errorReporter) {
        // get all grammars, and put all substitution group information
        // in the substitution group handler
        SchemaGrammar[] grammars = grammarBucket.getGrammars();
        for (int i = grammars.length-1; i >= 0; i--) {
            SGHandler.addSubstitutionGroup(grammars[i].getSubstitutionGroups());
        }

        XSParticleDecl fakeDerived = new XSParticleDecl();
        XSParticleDecl fakeBase = new XSParticleDecl();
        fakeDerived.fType = XSParticleDecl.PARTICLE_MODELGROUP;
        fakeBase.fType = XSParticleDecl.PARTICLE_MODELGROUP;
        // before worrying about complexTypes, let's get
        // groups redefined by restriction out of the way.
        for (int g = grammars.length-1; g >= 0; g--) {
            XSGroupDecl [] redefinedGroups = grammars[g].getRedefinedGroupDecls();
            SimpleLocator [] rgLocators = grammars[g].getRGLocators();
            for(int i=0; i<redefinedGroups.length; ) {
                XSGroupDecl derivedGrp = redefinedGroups[i++];
                XSModelGroupImpl derivedMG = derivedGrp.fModelGroup;
                XSGroupDecl baseGrp = redefinedGroups[i++];
                XSModelGroupImpl baseMG = baseGrp.fModelGroup;
                fakeDerived.fValue = derivedMG;
                fakeBase.fValue = baseMG;
                if(baseMG == null) {
                    if(derivedMG != null) { // can't be a restriction!
                        reportSchemaError(errorReporter, rgLocators[i/2-1],
                                "src-redefine.6.2.2",
                                new Object[]{derivedGrp.fName, "rcase-Recurse.2"});
                    }
                } else if (derivedMG == null) {
                    if (!fakeBase.emptiable()) {
                        reportSchemaError(errorReporter, rgLocators[i/2-1],
                                "src-redefine.6.2.2",
                                new Object[]{derivedGrp.fName, "rcase-Recurse.2"});
                    }
                } else {
                    groupSubsumption(fakeDerived, fakeBase, grammarBucket,
                            SGHandler, cmBuilder, errorReporter, derivedGrp.fName,
                            rgLocators[i/2-1]);
                }
            }
        }

        // for each complex type, check the 3 constraints.
        // types need to be checked
        XSComplexTypeDecl[] types;
        SimpleLocator [] ctLocators;
        // to hold the errors
        // REVISIT: do we want to report all errors? or just one?
        //XMLSchemaError1D errors = new XMLSchemaError1D();
        // whether need to check this type again;
        // whether only do UPA checking
        boolean further, fullChecked;
        // if do all checkings, how many need to be checked again.
        int keepType;
        // i: grammar; j: type; k: error
        // for all grammars
        SymbolHash elemTable = new SymbolHash();
        Stack stack = new Stack();
        ArrayList wcList = (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) ? new ArrayList() : null;
        for (int i = grammars.length-1, j; i >= 0; i--) {
            // get whether to skip EDC, and types need to be checked
            keepType = 0;
            fullChecked = grammars[i].fFullChecked;
            types = grammars[i].getUncheckedComplexTypeDecls();
            ctLocators = grammars[i].getUncheckedCTLocators();
            // for each type
            for (j = 0; j < types.length; j++) {
                // if we've already full-checked this grammar, then
                // skip the EDC constraint
                if (!fullChecked) {
                    // 1. Element Decl Consistent
                    if (types[j].fParticle!=null) {
                        elemTable.clear();
                        try {
                            checkElementDeclsConsistent(types[j], types[j].fParticle,
                                    elemTable, SGHandler, grammarBucket,
                                    wcList, stack);
                        }
                        catch (XMLSchemaException e) {
                            reportSchemaError(errorReporter, ctLocators[j],
                                    e.getKey(),
                                    e.getArgs());
                        }
                    }
                }

                // 2. Particle Derivation

                if (types[j].fBaseType != null &&
                        types[j].fBaseType != fAnyType &&
                        types[j].fDerivedBy == XSConstants.DERIVATION_RESTRICTION &&
                        (types[j].fBaseType instanceof XSComplexTypeDecl)) {

                    XSParticleDecl derivedParticle=types[j].fParticle;
                    XSComplexTypeDecl bType = (XSComplexTypeDecl)(types[j].fBaseType);
                    XSParticleDecl baseParticle= bType.fParticle;
                    // When there is open content, particle is never null.
                    // Open contents are handled in typeSubsumption().
                    if (derivedParticle==null) {
                        if (baseParticle!=null && !baseParticle.emptiable()) {
                            reportSchemaError(errorReporter,ctLocators[j],
                                    "derivation-ok-restriction.5.3.2",
                                    new Object[]{types[j].fName, types[j].fBaseType.getName()});
                        }
                    }
                    else if (baseParticle!=null) {
                        typeSubsumption(types[j], bType, grammarBucket,
                                SGHandler, cmBuilder, errorReporter, ctLocators[j]);
                    }
                    else {
                        reportSchemaError(errorReporter, ctLocators[j],
                                "derivation-ok-restriction.5.4.2",
                                new Object[]{types[j].fName});
                    }
                }
                // 3. UPA
                // get the content model and check UPA
                XSCMValidator cm = types[j].getContentModel(cmBuilder, true);
                further = false;
                if (cm != null) {
                    try {
                        further = cm.checkUniqueParticleAttribution(SGHandler, this);
                    } catch (XMLSchemaException e) {
                        reportSchemaError(errorReporter, ctLocators[j],
                                e.getKey(),
                                e.getArgs());
                    }
                }
                // now report all errors
                // REVISIT: do we want to report all errors? or just one?
                /*for (k = errors.getErrorCodeNum()-1; k >= 0; k--) {
                    reportSchemaError(errorReporter, ctLocators[j],
                                      errors.getErrorCode(k),
                                      errors.getArgs(k));
                }*/

                // if we are doing all checkings, and this one needs further
                // checking, store it in the type array.
                if (!fullChecked && further)
                    types[keepType++] = types[j];

                // clear errors for the next type.
                // REVISIT: do we want to report all errors? or just one?
                //errors.clear();
            }
            // we've done with the types in this grammar. if we are checking
            // all constraints, need to trim type array to a proper size:
            // only contain those need further checking.
            // and mark this grammar that it only needs UPA checking.
            if (!fullChecked) {
                grammars[i].setUncheckedTypeNum(keepType);
                grammars[i].fFullChecked = true;
            }
        }
    }

    /*
     * Check that a given particle is a valid restriction of a base particle.
     * NOTE: deprecated
     */
    public void checkElementDeclsConsistent(XSComplexTypeDecl type,
            XSParticleDecl particle,
            SymbolHash elemDeclHash,
            SubstitutionGroupHandler sgHandler) throws XMLSchemaException {

        // check for elements in the tree with the same name and namespace

        int pType = particle.fType;

        if (pType == XSParticleDecl.PARTICLE_WILDCARD)
            return;

        if (pType == XSParticleDecl.PARTICLE_ELEMENT) {
            XSElementDecl elem = (XSElementDecl)(particle.fValue);
            findElemInTable(type, elem, elemDeclHash);

            if (elem.fScope == XSConstants.SCOPE_GLOBAL) {
                // Check for subsitution groups.
                XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(elem, fSchemaVersion);
                for (int i = 0; i < subGroup.length; i++) {
                    findElemInTable(type, subGroup[i], elemDeclHash);
                }
            }
            return;
        }

        XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
        for (int i = 0; i < group.fParticleCount; i++)
            checkElementDeclsConsistent(type, group.fParticles[i], elemDeclHash, sgHandler);
    }

    protected void checkElementDeclsConsistent(XSComplexTypeDecl type,
            XSParticleDecl particle,
            SymbolHash elemDeclHash,
            SubstitutionGroupHandler sgHandler,
            XSGrammarBucket grammarBucket,
            ArrayList wcList,
            Stack stack) throws XMLSchemaException {

        // check for elements in the tree with the same name and namespace

        if (stack.size() > 0) {
            stack.clear();
        }

        for (;;) {
            final int pType = particle.fType;

            if (pType == XSParticleDecl.PARTICLE_WILDCARD) {
                // no op
            }
            else  if (pType == XSParticleDecl.PARTICLE_ELEMENT) {
                XSElementDecl elem = (XSElementDecl)(particle.fValue);
                findElemInTable(type, elem, elemDeclHash);

                if (elem.fScope == XSConstants.SCOPE_GLOBAL) {
                    // Check for subsitution groups.
                    XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(elem, fSchemaVersion);
                    for (int i = 0; i < subGroup.length; i++) {
                        findElemInTable(type, subGroup[i], elemDeclHash);
                    }
                }
            }
            else {
                XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                for (int i = group.fParticleCount - 1; i >= 0 ; i--)
                    stack.push(group.fParticles[i]);
            }
            
            if (stack.isEmpty()) {
                break;
            }
            particle = (XSParticleDecl) stack.pop();
        }
    }

    public void findElemInTable(XSComplexTypeDecl type, XSElementDecl elem,
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
    }

    protected XSElementDecl findExistingElement(XSElementDecl elem, SymbolHash elemDeclHash) {
        // How can we avoid this concat?  LM.
        String name = elem.fName + "," + elem.fTargetNamespace;
        XSElementDecl existingElem = (XSElementDecl)(elemDeclHash.get(name));

        if (existingElem == null) {
            // just add it in
            elemDeclHash.put(name, elem);
        }
        
        return existingElem;
    }

    // to check whether two element overlap, as defined in constraint UPA
    protected boolean overlapUPA(XSElementDecl element1,
            XSElementDecl element2,
            SubstitutionGroupHandler sgHandler) {
        // if the two element have the same name and namespace,
        if (element1.fName == element2.fName &&
                element1.fTargetNamespace == element2.fTargetNamespace) {
            return true;
        }

        // or if there is an element decl in element1's substitution group,
        // who has the same name/namespace with element2
        XSElementDecl[] subGroup1 = sgHandler.getSubstitutionGroup(element1, fSchemaVersion);
        for (int i = subGroup1.length-1; i >= 0; i--) {
            if (subGroup1[i].fName == element2.fName &&
                    subGroup1[i].fTargetNamespace == element2.fTargetNamespace) {
                return true;
            }
        }

        // or if there is an element decl in element2's substitution group,
        // who has the same name/namespace with element1
        XSElementDecl[] subGroup2 = sgHandler.getSubstitutionGroup(element2, fSchemaVersion);
        for (int i = subGroup2.length-1; i >= 0; i--) {
            if (subGroup2[i].fName == element1.fName &&
                    subGroup2[i].fTargetNamespace == element1.fTargetNamespace) {
                return true;
            }
        }

        // or if the 2 substitution groups overlap.
        for (int i = subGroup1.length-1; i >= 0; i--) {
            for (int j = subGroup2.length-1; j >= 0; j--) {
                if (subGroup1[i].fName == subGroup2[j].fName &&
                        subGroup1[i].fTargetNamespace == subGroup2[j].fTargetNamespace) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean overlapUPA(XSWildcardDecl wildcard1,
            XSWildcardDecl wildcard2) {
        // if the intersection of the two wildcards is not any and
        // and the {namespaces} of such intersection is not the empty set
        XSWildcardDecl intersect = performIntersectionWith(wildcard1, wildcard2, wildcard1.fProcessContents);
        if (intersect == null ||
                intersect.fType != XSWildcardDecl.NSCONSTRAINT_LIST ||
                intersect.fNamespaceList.length != 0) {
            return true;
        }

        return false;
    }

    // call one of the above methods according to the type of decls
    public boolean overlapUPA(Object decl1, Object decl2,
            SubstitutionGroupHandler sgHandler) {
        if (decl1 instanceof XSElementDecl) {
            if (decl2 instanceof XSElementDecl) {
                return overlapUPA((XSElementDecl)decl1,
                        (XSElementDecl)decl2,
                        sgHandler);
            } 
            else {
                return overlapUPA((XSElementDecl)decl1,
                        (XSWildcardDecl)decl2,
                        sgHandler);
            }
        } 
        else {
            if (decl2 instanceof XSElementDecl) {
                return overlapUPA((XSElementDecl)decl2,
                        (XSWildcardDecl)decl1,
                        sgHandler);
            } 
            else {
                return overlapUPA((XSWildcardDecl)decl1,
                        (XSWildcardDecl)decl2);
            }
        }
    }
    
    /**
     * Wildcard constraints - helper methods
     */
    boolean areSame(XSWildcardDecl wildcard, XSWildcardDecl otherWildcard) {
        if (wildcard.fType == otherWildcard.fType) {
            // ##any, true
            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_ANY) {
                return true;
            }

            // ##other, only check the negated value
            // * when we support not(list), we need to check in the same way
            //   as for NSCONSTRAINT_LIST.
            // not(list) is supported - no need for that check
            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                return wildcard.fNamespaceList[0] == otherWildcard.fNamespaceList[0];
            }

            // ## list, must have the same length,
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

    boolean disjoint2sets(String[] one, String[] theOther) {
        for (int i=0; i<one.length; i++) {
            if (elementInSet(one[i], theOther))
                return false;
        }
        return true;
    }
    
    // End wildcard constraints - helper methods
    
    // Wildcard constraint checking - abstract methods
    public abstract boolean isSubsetOf(XSWildcardDecl wildcard, XSWildcardDecl superWildcard);
    public abstract XSWildcardDecl performUnionWith(XSWildcardDecl wildcard, XSWildcardDecl otherWildcard, short processContents);
    public abstract XSWildcardDecl performIntersectionWith(XSWildcardDecl wildcard, XSWildcardDecl otherWildcard, short processContents);
    protected abstract boolean checkEmptyFacets(XSSimpleType baseType);

    // to check whether an element overlaps with a wildcard,
    // as defined in constraint UPA
    public abstract boolean overlapUPA(XSElementDecl element,
            XSWildcardDecl wildcard,
            SubstitutionGroupHandler sgHandler);

    protected abstract void groupSubsumption(XSParticleDecl dParticle, XSParticleDecl bParticle,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, String dName,
            SimpleLocator locator);
    
    protected abstract void typeSubsumption(XSComplexTypeDecl dType, XSComplexTypeDecl bType,
            XSGrammarBucket grammarBucket, SubstitutionGroupHandler SGHandler,
            CMBuilder cmBuilder, XMLErrorReporter errorReporter, SimpleLocator locator);
    
} // class XSContraints
