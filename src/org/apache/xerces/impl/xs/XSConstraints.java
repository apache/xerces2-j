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

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.xs.*;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import java.util.Vector;

/**
 * Constaints shared by traversers and validator
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class XSConstraints {

    /**
     * check whether derived is valid derived from base, given a subset
     * of {restriction, extension}.
     */
    public static boolean checkTypeDerivationOk(XSTypeDecl derived, XSTypeDecl base, int block) {
        // if derived is anyType, then it's valid only if base is anyType too
        if (derived == SchemaGrammar.fAnyType)
            return derived == base;
        // if derived is anySimpleType, then it's valid only if the base
        // is ur-type
        if (derived == SchemaGrammar.fAnySimpleType) {
            return (base == SchemaGrammar.fAnyType ||
                    base == SchemaGrammar.fAnySimpleType);
        }

        // if derived is simple type
        if (derived.getXSType() == XSTypeDecl.SIMPLE_TYPE) {
            // if base is complex type
            if (base.getXSType() == XSTypeDecl.COMPLEX_TYPE) {
                // if base is anyType, change base to anySimpleType,
                // otherwise, not valid
                if (base == SchemaGrammar.fAnyType)
                    base = SchemaGrammar.fAnySimpleType;
                else
                    return false;
            }
            return checkSimpleDerivation((DatatypeValidator)derived,
                                         (DatatypeValidator)base, block);
        } else {
            return checkComplexDerivation((XSComplexTypeDecl)derived, base, block);
        }
    }

    /**
     * check whether simple type derived is valid derived from base,
     * given a subset of {restriction, extension}.
     */
    public static boolean checkSimpleDerivationOk(DatatypeValidator derived, XSTypeDecl base, int block) {
        // if derived is anySimpleType, then it's valid only if the base
        // is ur-type
        if (derived == SchemaGrammar.fAnySimpleType) {
            return (base == SchemaGrammar.fAnyType ||
                    base == SchemaGrammar.fAnySimpleType);
        }

        // if base is complex type
        if (base.getXSType() == XSTypeDecl.COMPLEX_TYPE) {
            // if base is anyType, change base to anySimpleType,
            // otherwise, not valid
            if (base == SchemaGrammar.fAnyType)
                base = SchemaGrammar.fAnySimpleType;
            else
                return false;
        }
        return checkSimpleDerivation((DatatypeValidator)derived,
                                     (DatatypeValidator)base, block);
    }

    /**
     * check whether complex type derived is valid derived from base,
     * given a subset of {restriction, extension}.
     */
    public static boolean checkComplexDerivationOk(XSComplexTypeDecl derived, XSTypeDecl base, int block) {
        // if derived is anyType, then it's valid only if base is anyType too
        if (derived == SchemaGrammar.fAnyType)
            return derived == base;
        return checkComplexDerivation((XSComplexTypeDecl)derived, base, block);
    }

    /**
     * Note: this will be a private method, and it assumes that derived is not
     *       anySimpleType, and base is not anyType. Another method will be
     *       introduced for public use, which will call this method.
     */
    private static boolean checkSimpleDerivation(DatatypeValidator derived, DatatypeValidator base, int block) {
        // 1 They are the same type definition.
        if (derived == base)
            return true;

        // 2 All of the following must be true:
        // 2.1 restriction is not in the subset, or in the {final} of its own {base type definition};
        if ((block & SchemaSymbols.RESTRICTION) != 0 ||
            (derived.getBaseValidator().getFinalSet() & SchemaSymbols.RESTRICTION) != 0) {
            return false;
        }

        // 2.2 One of the following must be true:
        // 2.2.1 D's ·base type definition· is B.
        DatatypeValidator directBase = derived.getBaseValidator();
        if (directBase == base)
            return true;

        // 2.2.2 D's ·base type definition· is not the ·simple ur-type definition· and is validly derived from B given the subset, as defined by this constraint.
        if (directBase != SchemaGrammar.fAnySimpleType &&
            checkSimpleDerivation(directBase, base, block)) {
            return true;
        }

        // 2.2.3 D's {variety} is list or union and B is the ·simple ur-type definition·.
        if ((derived instanceof ListDatatypeValidator ||
             derived instanceof UnionDatatypeValidator) &&
            base == SchemaGrammar.fAnySimpleType) {
            return true;
        }

        // 2.2.4 B's {variety} is union and D is validly derived from a type definition in B's {member type definitions} given the subset, as defined by this constraint.
        if (base instanceof UnionDatatypeValidator) {
            Vector subUnionMemberDV = ((UnionDatatypeValidator)base).getBaseValidators();
            int subUnionSize = subUnionMemberDV.size();
            for (int i=0; i<subUnionSize; i++) {
                base = (DatatypeValidator)subUnionMemberDV.elementAt(i);
                if (checkSimpleDerivation(derived, base, block))
                    return true;
            }
        }

        return false;
    }

    /**
     * Note: this will be a private method, and it assumes that derived is not
     *       anyType. Another method will be introduced for public use,
     *       which will call this method.
     */
    private static boolean checkComplexDerivation(XSComplexTypeDecl derived, XSTypeDecl base, int block) {
        // 2.1 B and D must be the same type definition.
        if (derived == base)
            return true;

        // 1 If B and D are not the same type definition, then the {derivation method} of D must not be in the subset.
        if ((derived.fDerivedBy & block) != 0)
            return false;

        // 2 One of the following must be true:
        XSTypeDecl directBase = derived.fBaseType;
        // 2.2 B must be D's {base type definition}.
        if (directBase == base)
            return true;

        // 2.3 All of the following must be true:
        // 2.3.1 D's {base type definition} must not be the ·ur-type definition·.
        if (directBase == SchemaGrammar.fAnyType ||
            directBase == SchemaGrammar.fAnySimpleType) {
            return false;
        }

        // 2.3.2 The appropriate case among the following must be true:
        // 2.3.2.1 If D's {base type definition} is complex, then it must be validly derived from B given the subset as defined by this constraint.
        if (directBase.getXSType() == XSTypeDecl.COMPLEX_TYPE)
            return checkComplexDerivation((XSComplexTypeDecl)directBase, base, block);

        // 2.3.2.2 If D's {base type definition} is simple, then it must be validly derived from B given the subset as defined in Type Derivation OK (Simple) (§3.14.6).
        if (directBase.getXSType() == XSTypeDecl.SIMPLE_TYPE) {
            // if base is complex type
            if (base.getXSType() == XSTypeDecl.COMPLEX_TYPE) {
                // if base is anyType, change base to anySimpleType,
                // otherwise, not valid
                if (base == SchemaGrammar.fAnyType)
                    base = SchemaGrammar.fAnySimpleType;
                else
                    return false;
            }
            return checkSimpleDerivation((DatatypeValidator)directBase,
                                         (DatatypeValidator)base, block);
        }

        return false;
    }

    /**
     * check whether a value is a valid default for some type
     * returns the compiled form of the value
     */
    public static Object ElementDefaultValidImmediate(XSTypeDecl type, String value) {

        DatatypeValidator dv = null;

        // e-props-correct
        // For a string to be a valid default with respect to a type definition the appropriate case among the following must be true:
        // 1 If the type definition is a simple type definition, then the string must be ·valid· with respect to that definition as defined by String Valid (§3.14.4).
        if (type.getXSType() == XSTypeDecl.SIMPLE_TYPE) {
            dv = (DatatypeValidator)type;
        }

        // 2 If the type definition is a complex type definition, then all of the following must be true:
        else {
            // 2.1 its {content type} must be a simple type definition or mixed.
            XSComplexTypeDecl ctype = (XSComplexTypeDecl)type;
            // 2.2 The appropriate case among the following must be true:
            // 2.2.1 If the {content type} is a simple type definition, then the string must be ·valid· with respect to that simple type definition as defined by String Valid (§3.14.4).
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                dv = ctype.fDatatypeValidator;
            }
            // 2.2.2 If the {content type} is mixed, then the {content type}'s particle must be ·emptiable· as defined by Particle Emptiable (§3.9.6).
            else if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                if (!ctype.fParticle.emptiable())
                    return null;
            }
            else {
                return null;
            }
        }

        // get the simple type declaration, and validate
        Object actualValue = null;
        if (dv != null) {
            try {
                // REVISIT:  we'll be able to do this once he datatype redesign is implemented
                //actualValue = dv.validate(value, null);
                dv.validate(value, null);
                actualValue = value;
            } catch (InvalidDatatypeValueException ide) {
            }
        }

        return actualValue;
    }

    /**
     * used to check the 3 constraints against each complex type
     * (should be each model group):
     * Unique Particle Attribution, Particle Derivation (Restriction),
     * Element Declrations Consistent.
     */
    public static void fullSchemaChecking(XSGrammarResolver grammarResolver,
                                          SubstitutionGroupHandler SGHandler,
                                          CMBuilder cmBuilder,
                                          XMLErrorReporter errorReporter) {
        // get all grammars, and put all substitution group information
        // in the substitution group handler
        SchemaGrammar[] grammars = grammarResolver.getGrammars();
        for (int i = grammars.length-1; i >= 0; i--) {
            SGHandler.addSubstitutionGroup(grammars[i].getSubstitutionGroups());
        }

        // for each complex type, check the 3 constraints.
        // types need to be checked
        XSComplexTypeDecl[] types;
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
        for (int i = grammars.length-1, j, k; i >= 0; i--) {
            // get whether only check UPA, and types need to be checked
            keepType = 0;
            fullChecked = grammars[i].fFullChecked;
            types = grammars[i].getUncheckedComplexTypeDecls();
            // for each type
            for (j = types.length-1; j >= 0; j--) {
                // if only do UPA checking, skip the other two constraints
                if (!fullChecked) {
                // 1. Element Decl Consistent
                }

                // 2. Particle Derivation

                // 3. UPA
                // get the content model and check UPA
                XSCMValidator cm = types[j].getContentModel(cmBuilder);
                further = false;
                if (cm != null) {
                    try {
                        further = cm.checkUniqueParticleAttribution(SGHandler);
                    } catch (XMLSchemaException e) {
                        errorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                                  e.getKey(),
                                                  e.getArgs(),
                                                  XMLErrorReporter.SEVERITY_ERROR);
                    }
                }
                // now report all errors
                // REVISIT: do we want to report all errors? or just one?
                /*for (k = errors.getErrorCodeNum()-1; k >= 0; k--) {
                    errorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                              errors.getErrorCode(k),
                                              errors.getArgs(k),
                                              XMLErrorReporter.SEVERITY_ERROR);
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

    // to check whether two element overlap, as defined in constraint UPA
    public static boolean overlapUPA(XSElementDecl element1,
                                     XSElementDecl element2,
                                     SubstitutionGroupHandler sgHandler) {
        // if the two element have the same name and namespace,
        if (element1.fName == element2.fName &&
            element1.fTargetNamespace == element2.fTargetNamespace) {
            return true;
        }

        // or if there is an element decl in element1's substitution group,
        // who has the same name/namespace with element2
        XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(element1);
        for (int i = subGroup.length-1; i >= 0; i--) {
            if (subGroup[i].fName == element2.fName &&
                subGroup[i].fTargetNamespace == element2.fTargetNamespace) {
                return true;
            }
        }

        // or if there is an element decl in element2's substitution group,
        // who has the same name/namespace with element1
        subGroup = sgHandler.getSubstitutionGroup(element2);
        for (int i = subGroup.length-1; i >= 0; i--) {
            if (subGroup[i].fName == element1.fName &&
                subGroup[i].fTargetNamespace == element1.fTargetNamespace) {
                return true;
            }
        }

        return false;
    }

    // to check whether an element overlaps with a wildcard,
    // as defined in constraint UPA
    public static boolean overlapUPA(XSElementDecl element,
                                     XSWildcardDecl wildcard,
                                     SubstitutionGroupHandler sgHandler) {
        // if the wildcard allows the element
        if (wildcard.allowNamespace(element.fTargetNamespace))
            return true;

        // or if the wildcard allows any element in the substitution group
        XSElementDecl[] subGroup = sgHandler.getSubstitutionGroup(element);
        for (int i = subGroup.length-1; i >= 0; i--) {
            if (wildcard.allowNamespace(subGroup[i].fTargetNamespace))
                return true;
        }

        return false;
    }

    public static boolean overlapUPA(XSWildcardDecl wildcard1,
                                     XSWildcardDecl wildcard2) {
        // if the intersection of the two wildcard is not empty list
        XSWildcardDecl intersect = wildcard1.performIntersectionWith(wildcard2, wildcard1.fProcessContents);
        if (intersect == null ||
            intersect.fType != XSWildcardDecl.WILDCARD_LIST ||
            intersect.fNamespaceList.length != 0) {
            return true;
        }

        return false;
    }

    // call one of the above methods according to the type of decls
    public static boolean overlapUPA(Object decl1, Object decl2,
                                     SubstitutionGroupHandler sgHandler) {
        if (decl1 instanceof XSElementDecl) {
            if (decl2 instanceof XSElementDecl) {
                return overlapUPA((XSElementDecl)decl1,
                                  (XSElementDecl)decl2,
                                  sgHandler);
            } else {
                return overlapUPA((XSElementDecl)decl1,
                                  (XSWildcardDecl)decl2,
                                  sgHandler);
            }
        } else {
            if (decl2 instanceof XSElementDecl) {
                return overlapUPA((XSElementDecl)decl2,
                                  (XSWildcardDecl)decl1,
                                  sgHandler);
            } else {
                return overlapUPA((XSWildcardDecl)decl1,
                                  (XSWildcardDecl)decl2);
            }
        }
    }
} // class XSContraints
