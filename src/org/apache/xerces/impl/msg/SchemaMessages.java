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

package org.apache.xerces.impl.msg;

import java.util.ListResourceBundle;

/**
 * This file contains error and warning messages for the Schema validator
 * The messages are arranged in key and value tuples in a ListResourceBundle.
 *
 * @version $Id$
 */
public class SchemaMessages extends ListResourceBundle {
    /** The list resource bundle contents. */
    public static final Object CONTENTS[][] = {

        // Internal message formatter messages
        {"BadMessageKey", "The error message corresponding to the message key can not be found."},
        {"FormatFailed", "An internal error occurred while formatting the following message:\n"},

        //old stuff
        { "NoValidatorFor", "No validator for datatype {0}" },
        { "IncorrectDatatype", "Incorrect datatype: {0}" },
        { "NotADatatype", "{0} is not a datatype." },
        { "TextOnlyContentWithType", "The content attribute must be 'textOnly' if you specify a type attribute." },
        { "FeatureUnsupported", "{0} is unsupported" },
        { "NestedOnlyInElemOnly", "Nested Element decls only allowed in elementOnly content" },
        { "EltRefOnlyInMixedElemOnly", "Element references only allowed in mixed or elementOnly content"},
        { "OnlyInEltContent", "{0} only allowed in elementOnly content."},
        { "OrderIsAll", "{0} not allowed if the order is all."},
        { "DatatypeWithType", "Datatype qualifiers can only be used if you specify a type attribute."},
        { "DatatypeQualUnsupported", "The datatype qualifier {0} is not supported."},
        { "GroupContentRestricted", "Error: {0} content must be one of choice, all or sequence.  Saw {1}."},
        { "UnknownBaseDatatype", "Unknown base type {0} for type {1}." },
        { "BadAttWithRef", "cannot use ref with any of type, block, final, abstract, nillable, default or fixed."},
        { "NoContentForRef", "Cannot have child content for an element declaration that has a ref attribute" },
        { "IncorrectDefaultType", "Incorrect type for {0}'s default value: {1}" },
        { "IllegalAttContent", "Illegal content {0} in attribute group" },
        { "ValueNotInteger", "Value of {0} is not an integer." },
        { "DatatypeError", "Datatype error: {0}." },
        { "TypeAlreadySet", "The type of the element has already been declared." },
        { "GenericError", "Schema error: {0}." },
        { "UnexpectedError", "UnexpectedError" },
        {"ContentError", "Content (annotation?,..) is incorrect for type {0}"},
        {"AnnotationError", "Annotation can only appear once: type {0}"},
        {"ListUnionRestrictionError","List | Union | Restriction content is invalid for type {0}"},
        { "ProhibitedAttributePresent", "An attribute declared \"prohibited\" is present in this element definition." },
        // identity constraints
        { "UniqueNotEnoughValues", "Not enough values specified for <unique> identity constraint specified for element \"{0}\"." },
        { "KeyNotEnoughValues", "Not enough values specified for <key name=\"{1}\"> identity constraint specified for element \"{0}\"." },
        { "KeyRefNotEnoughValues", "Not enough values specified for <keyref name=\"{1}\"> identity constraint specified for element \"{0}\"." },
        { "DuplicateField", "Duplicate match in scope for field \"{0}\"." },
        { "DuplicateUnique", "Duplicate unique value [{0}] declared for identity constraint of element \"{1}\"." },
        { "DuplicateKey", "Duplicate key value [{0}] declared for identity constraint of element \"{1}\"." },
        { "KeyNotFound", "Key with value [{0}] not found for identity constraint of element \"{1}\"." },
        { "UnknownField", "Internal identity constraint error; unknown field \"{0}\"." },
        { "KeyRefReferNotFound", "Key reference declaration \"{0}\" refers to unknown key with name \"{1}\"." },
        { "FixedDiffersFromActual", "The content of this element is not equivalent to the value of the \"fixed\" attribute in the element's declaration in the schema."},
        // simpleType
        {"InvalidBaseType", "itemType \"{0}\" must have a variety of atomic or union"},
        {"FieldMultipleMatch", "Identity constraint error:  field \"{0}\" matches more than one value within the scope of its selector; fields must match unique values"},
        {"KeyRefOutOfScope", "Identity Constraint error:  identity constraint \"{0}\" has a keyref which refers to a key or unique that is out of scope."},
        {"AbsentKeyValue", "Identity Constraint error (cvc-identity-constraint.4.2.1):  element \"{0}\" has a key with no value."},
        {"KeyMatchesNillable", "Identity Constraint error (cvc-identity-constraint.4.2.3):  element \"{0}\" has a key which matches an element which has nillable set to true."},
        {"BadMinMaxForAllElem", "cos-all-limited.2:  The {0} attribute of an element in an all schema component must have the value zero or one.  The value \"{1}\" is incorrect."},
        {"BadMinMaxForGroupWithAll", "Error:  cos-all-limited.1.2:  The {0} attribute in a reference to a named model group whose content model is \"all\" must have the value one.  The value \"{1}\" is incorrect."},
        {"SeqChoiceContentRestricted", "Error:  {0} content must be zero or more of element, group, choice, sequence or any.  Saw \"{1}\"."},
        {"AllContentRestricted", "Error:  The content of all is restricted to zero or more elements.  Saw \"{0}\"."},
        {"AllContentLimited", "Error:  cos-all-limited.1.2: A group whose content is \"all\" must only appear as the content type of a complex type definition.  Saw group in \"{0}\"."},
        {"MinMaxOnGroupChild", "Error:  The child ''{1}'' of the named group definition ''{0}'' must not specify either the minOccurs or maxOccurs attribute."},
        {"BadMinMaxForAllGp", "Error:  cos-all-limited.1.2:  The {0} attribute of a model group with \"all\" compositor that is part of a pair that is the content type of a complex type definition must have the value one.  The value \"{1}\" is incorrect."},
        {"SchemaLocation", "Value \"{0}\" is not valid 'schemaLocation' syntax. anyURI must be followed by schema file"},
        {"UniqueParticleAttribution", "Error: cos-nonambig: ({0}) and ({1}) violate the \"Unique Particle Attribution\" rule."},
        {"Con3X3ElementAppearance", "Error: constraint 3.x.3: Element ''{0}'' cannot appear here."},
        {"Con3X3AttributeAppearance", "Error: constraint 3.x.3: Attribute ''{1}'' cannot appear in element ''{0}''."},
        {"Con3X3AttributeMustAppear", "Error: constraint 3.x.3: Attribute ''{1}'' must appear in element ''{0}''."},
        {"Con3X3AttributeInvalidValue", "Error: constraint 3.x.3: Invalid attribute value for ''{1}'' in element ''{0}'': {2}."},

        // ideally, we should only use the following error keys, not the ones
        // under "old stuff". and we should cover all of the following errors.
        {"General", "schema error: {0}."},

        //validation (3.X.4)
        {"cvc-assess-attr", "cvc-assess-attr: error."},
        {"cvc-assess-elt", "cvc-assess-elt: error."},
        {"cvc-attribute.1", "cvc-attribute.1: error."},
        {"cvc-attribute.2", "cvc-attribute.2: error."},
        {"cvc-attribute.3", "cvc-attribute.3: error."},
        {"cvc-attribute.4", "cvc-attribute.4: error."},
        {"cvc-au", "cvc-au: error."},
        {"cvc-complex-type.1", "cvc-complex-type: error."},
        {"cvc-complex-type.2.1", "cvc-complex-type: error."},
        {"cvc-complex-type.2.2", "cvc-complex-type: error."},
        {"cvc-complex-type.2.3", "cvc-complex-type: error."},
        {"cvc-complex-type.2.4", "cvc-complex-type: error."},
        {"cvc-complex-type.3.1", "cvc-complex-type: error."},
        {"cvc-complex-type.3.2.1", "cvc-complex-type: error."},
        {"cvc-complex-type.3.2.2", "cvc-complex-type: error."},
        {"cvc-complex-type.4", "cvc-complex-type: error."},
        {"cvc-complex-type.5.1", "cvc-complex-type: error."},
        {"cvc-complex-type.5.2", "cvc-complex-type: error."},
        {"cvc-datatype-valid.1.1", "cvc-datatype-valid: error."},
        {"cvc-datatype-valid.1.2.1", "cvc-datatype-valid: error."},
        {"cvc-datatype-valid.1.2.2", "cvc-datatype-valid: error."},
        {"cvc-datatype-valid.1.2.3", "cvc-datatype-valid: error."},
        {"cvc-datatype-valid.2", "cvc-datatype-valid: error."},
        {"cvc-elt", "cvc-elt: error."},
        {"cvc-enumeration-valid", "cvc-enumeration-valid: error."},
        {"cvc-facet-valid", "cvc-facet-valid: error."},
        {"cvc-fractionDigits-valid", "cvc-fractionDigits-valid: error."},
        {"cvc-id", "cvc-id: error."},
        {"cvc-identity-constraint", "cvc-identity-constraint: error."},
        {"cvc-length-valid", "cvc-length-valid: error."},
        {"cvc-maxExclusive-valid", "cvc-maxExclusive-valid: error."},
        {"cvc-maxInclusive-valid", "cvc-maxInclusive-valid: error."},
        {"cvc-maxLength-valid", "cvc-maxLength-valid: error."},
        {"cvc-minExclusive-valid", "cvc-minExclusive-valid: error."},
        {"cvc-minInclusive-valid", "cvc-minInclusive-valid: error."},
        {"cvc-minLength-valid", "cvc-minLength-valid: error."},
        {"cvc-model-group", "cvc-model-group: error."},
        {"cvc-particle", "cvc-particle: error."},
        {"cvc-pattern-valid", "cvc-pattern-valid: error."},
        {"cvc-resolve-instance", "cvc-resolve-instance: error."},
        {"cvc-simple-type", "cvc-simple-type: error."},
        {"cvc-totalDigits-valid", "cvc-totalDigits-valid: error."},
        {"cvc-type", "cvc-type: error."},
        {"cvc-wildcard", "cvc-wildcard: error."},
        {"cvc-wildcard-namespace", "cvc-wildcard-namespace: error."},

        //schema for Schemas
        {"s4s-att-not-allowed", "s4s-att-not-allowed: error."},
        {"s4s-att-must-appear", "s4s-att-must-appear: error."},
        {"s4s-att-invalid-value", "s4s-att-invalid-value: error."},
        {"s4s-elt-must-match", "s4s-elt-must-match: error."},

        //schema valid (3.X.3)
        {"schema_reference", "schema_reference: error."},
        {"src-annotation", "src-annotation: error."},
        {"src-attribute", "src-attribute: error."},
        {"src-attribute_group", "src-attribute_group: error."},
        {"src-ct", "src-ct: error."},
        {"src-element", "src-element: error."},
        {"src-expredef", "src-expredef: error."},
        {"src-identity-constraint", "src-identity-constraint: error."},
        {"src-import", "src-import: error."},
        {"src-include", "src-include: error."},
        {"src-list-itemType-or-simpleType", "src-list-itemType-or-simpleType: error."},
        {"src-model_group", "src-model_group: error."},
        {"src-model_group_defn", "src-model_group_defn: error."},
        {"src-multiple-enumerations", "src-multiple-enumerations: error."},
        {"src-multiple-patterns", "src-multiple-patterns: error."},
        {"src-notation", "src-notation: error."},
        {"src-qname", "src-qname: error."},
        {"src-redefine", "src-redefine: error."},
        {"src-resolve", "src-resolve: error."},
        {"src-restriction-base-or-simpleType", "src-restriction-base-or-simpleType: error."},
        {"src-simple-type", "src-simple-type: error."},
        {"src-single-facet-value", "src-single-facet-value: error."},
        {"src-union-memberTypes-or-simpleTypes", "src-union-memberTypes-or-simpleTypes: error."},
        {"src-wildcard", "src-wildcard: error."},
        {"st-restrict-facets", "st-restrict-facets: error."},

        //constraint valid (3.X.6)
        {"ag-props-correct", "ag-props-correct: error."},
        {"an-props-correct", "an-props-correct: error."},
        {"a-props-correct", "a-props-correct: error."},
        {"au-props-correct", "au-props-correct: error."},
        {"c-fields-xpaths", "c-fields-xpaths: error."},
        {"cos-all-limited", "cos-all-limited: error."},
        {"cos-applicable-facets", "cos-applicable-facets: error."},
        {"cos-aw-intersect", "cos-aw-intersect: error."},
        {"cos-aw-union", "cos-aw-union: error."},
        {"cos-choice-range", "cos-choice-range: error."},
        {"cos-ct-derived-ok", "cos-ct-derived-ok: error."},
        {"cos-ct-extends", "cos-ct-extends: error."},
        {"cos-element-consistent", "cos-element-consistent: error."},
        {"cos-equiv-class", "cos-equiv-class: error."},
        {"cos-equiv-derived-ok-rec", "cos-equiv-derived-ok-rec: error."},
        {"cos-group-emptiable", "cos-group-emptiable: error."},
        {"cos-list-of-atomic", "cos-list-of-atomic: error."},
        {"cos-no-circular-unions", "cos-no-circular-unions: error."},
        {"cos-nonambig", "cos-nonambig: error."},
        {"cos-ns-subset", "cos-ns-subset: error."},
        {"cos-particle-extend", "cos-particle-extend: error."},
        {"cos-particle-restrict", "cos-particle-restrict: error."},
        {"cos-seq-range", "cos-seq-range: error."},
        {"cos-st-derived-ok", "cos-st-derived-ok: error."},
        {"cos-st-restricts", "cos-st-restricts: error."},
        {"cos-valid-default", "cos-valid-default: error."},
        {"c-props-correct", "c-props-correct: error."},
        {"c-selector-xpath", "c-selector-xpath: error."},
        {"ct-props-correct", "ct-props-correct: error."},
        {"derivation-ok-restriction", "derivation-ok-restriction: error."},
        {"enumeration-required-notation", "enumeration-required-notation: error."},
        {"enumeration-valid-restriction", "enumeration-valid-restriction: error."},
        {"e-props-correct", "e-props-correct: error."},
        {"fractionDigits-totalDigits", "fractionDigits-totalDigits: error."},
        {"length-minLength-maxLength", "length-minLength-maxLength: error."},
        {"length-valid-restriction", "length-valid-restriction: error."},
        {"maxExclusive-valid-restriction", "maxExclusive-valid-restriction: error."},
        {"maxInclusive-maxExclusive", "maxInclusive-maxExclusive: error."},
        {"maxInclusive-valid-restriction", "maxInclusive-valid-restriction: error."},
        {"maxLength-valid-restriction", "maxLength-valid-restriction: error."},
        {"mgd-props-correct", "mgd-props-correct: error."},
        {"mg-props-correct", "mg-props-correct: error."},
        {"minExclusive-less-than-equal-to-maxExclusive", "minExclusive-less-than-equal-to-maxExclusive: error."},
        {"minExclusive-less-than-maxInclusive", "minExclusive-less-than-maxInclusive: error."},
        {"minExclusive-valid-restriction", "minExclusive-valid-restriction: error."},
        {"minInclusive-less-than-equal-to-maxInclusive", "minInclusive-less-than-equal-to-maxInclusive: error."},
        {"minInclusive-less-than-maxExclusive", "minInclusive-less-than-maxExclusive: error."},
        {"minInclusive-minExclusive", "minInclusive-minExclusive: error."},
        {"minInclusive-valid-restriction", "minInclusive-valid-restriction: error."},
        {"minLength-less-than-equal-to-maxLength", "minLength-less-than-equal-to-maxLength: error."},
        {"minLength-valid-restriction", "minLength-valid-restriction: error."},
        {"no-xmlns", "no-xmlns: error."},
        {"no-xsi", "no-xsi: error."},
        {"n-props-correct", "n-props-correct: error."},
        {"p-props-correct", "p-props-correct: error."},
        {"range-ok", "range-ok: error."},
        {"rcase-MapAndSum", "rcase-MapAndSum: error."},
        {"rcase-NameAndTypeOK", "rcase-NameAndTypeOK: error."},
        {"rcase-NSCompat", "rcase-NSCompat: error."},
        {"rcase-NSRecurseCheckCardinality", "rcase-NSRecurseCheckCardinality: error."},
        {"rcase-NSSubset", "rcase-NSSubset: error."},
        {"rcase-Recurse", "rcase-Recurse: error."},
        {"rcase-RecurseAsIfGroup", "rcase-RecurseAsIfGroup: error."},
        {"rcase-RecurseLax", "rcase-RecurseLax: error."},
        {"rcase-RecurseUnordered", "rcase-RecurseUnordered: error."},
        {"sch-props-correct", "sch-props-correct: error."},
        {"st-props-correct", "st-props-correct: error."},
        {"totalDigits-valid-restriction", "totalDigits-valid-restriction: error."},
        {"whiteSpace-valid-restriction", "whiteSpace-valid-restriction: error."},
        {"w-props-correct", "w-props-correct: error."},
    };

    /** Returns the list resource bundle contents. */
    public Object[][] getContents() {
        return CONTENTS;
    }

}
