/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.*;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
;

/**
 * The XML representation for a complexType
 * schema component is a <complexType> element information item
 *
 * @author Elena Litani, IBM
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSComplexTypeDecl implements XSComplexTypeDefinition {

    // name of the complexType
    String fName = null;

    // target namespace of the complexType
    String fTargetNamespace = null;

    // base type of the complexType
    XSTypeDefinition fBaseType = null;

    // derivation method of the complexType
    short fDerivedBy = XSConstants.DERIVATION_RESTRICTION;

    // final set of the complexType
    short fFinal = XSConstants.DERIVATION_NONE;

    // block set (prohibited substitution) of the complexType
    short fBlock = XSConstants.DERIVATION_NONE;

    // flags: whether is abstract; whether contains ID type;
    //        whether it's an anonymous tpye
    short fMiscFlags = 0;

    // the attribute group that holds the attribute uses and attribute wildcard
    XSAttributeGroupDecl fAttrGrp = null;

    // the content type of the complexType
    short fContentType = CONTENTTYPE_EMPTY;

    // if the content type is simple, then the corresponding simpleType
    XSSimpleType fXSSimpleType = null;

    // if the content type is element or mixed, the particle
    XSParticleDecl fParticle = null;

    // if there is a particle, the content model corresponding to that particle
    XSCMValidator fCMValidator = null;

    // list of annotations affiliated with this type
    XSObjectListImpl fAnnotations = null;

    public XSComplexTypeDecl() {
        // do-nothing constructor for now.
    }

    public void setValues(String name, String targetNamespace,
            XSTypeDefinition baseType, short derivedBy, short schemaFinal, 
            short block, short contentType,
            boolean isAbstract, XSAttributeGroupDecl attrGrp, 
            XSSimpleType simpleType, XSParticleDecl particle,
            XSObjectListImpl annotations) {
        fTargetNamespace = targetNamespace;
        fBaseType = baseType;
        fDerivedBy = derivedBy;
        fFinal = schemaFinal;
        fBlock = block;
        fContentType = contentType;
        if(isAbstract)
            fMiscFlags |= CT_IS_ABSTRACT;
        fAttrGrp = attrGrp;
        fXSSimpleType = simpleType;
        fParticle = particle;
        fAnnotations = annotations;
   }

   public void setName(String name) {
        fName = name;
   }

    public short getTypeCategory() {
        return COMPLEX_TYPE;
    }

    public String getTypeName() {
        return fName;
    }

    public short getFinalSet(){
        return fFinal;
    }

    public String getTargetNamespace(){
        return fTargetNamespace;
    }

    // flags for the misc flag
    private static final short CT_IS_ABSTRACT = 1;
    private static final short CT_HAS_TYPE_ID = 2;
    private static final short CT_IS_ANONYMOUS = 4;

    // methods to get/set misc flag

    public boolean containsTypeID () {
        return((fMiscFlags & CT_HAS_TYPE_ID) != 0);
    }

    public void setIsAbstractType() {
        fMiscFlags |= CT_IS_ABSTRACT;
    }
    public void setContainsTypeID() {
        fMiscFlags |= CT_HAS_TYPE_ID;
    }
    public void setIsAnonymous() {
        fMiscFlags |= CT_IS_ANONYMOUS;
    }

    public synchronized XSCMValidator getContentModel(CMBuilder cmBuilder) {
        if (fCMValidator == null)
            fCMValidator = cmBuilder.getContentModel(this);

        return fCMValidator;
    }

    // some utility methods:

    // return the attribute group for this complex type
    public XSAttributeGroupDecl getAttrGrp() {
        return fAttrGrp;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        appendTypeInfo(str);
        return str.toString();
    }

    void appendTypeInfo(StringBuffer str) {
        String contentType[] = {"EMPTY", "SIMPLE", "ELEMENT", "MIXED"};
        String derivedBy[] = {"EMPTY", "EXTENSION", "RESTRICTION"};

        str.append("Complex type name='" + fTargetNamespace + "," + getTypeName() + "', ");
        if (fBaseType != null)
            str.append(" base type name='" + fBaseType.getName() + "', ");

        str.append(" content type='" + contentType[fContentType] + "', ");
        str.append(" isAbstract='" + getAbstract() + "', ");
        str.append(" hasTypeId='" + containsTypeID() + "', ");
        str.append(" final='" + fFinal + "', ");
        str.append(" block='" + fBlock + "', ");
        if (fParticle != null)
            str.append(" particle='" + fParticle.toString() + "', ");
        str.append(" derivedBy='" + derivedBy[fDerivedBy] + "'. ");

    }

    public boolean derivedFromType(XSTypeDefinition ancestor, short derivationMethod) {
        // ancestor is null, retur false
        if (ancestor == null)
            return false;
        // ancestor is anyType, return true
        if (ancestor == SchemaGrammar.fAnyType)
            return true;
        // recursively get base, and compare it with ancestor
        XSTypeDefinition type = this;
        while (type != ancestor &&                     // compare with ancestor
               type != SchemaGrammar.fAnySimpleType &&  // reached anySimpleType
               type != SchemaGrammar.fAnyType) {        // reached anyType
            type = type.getBaseType();
        }

        return type == ancestor;
    }

    public boolean derivedFrom(String ancestorNS, String ancestorName, short derivationMethod) {
        // ancestor is null, retur false
        if (ancestorName == null)
            return false;
        // ancestor is anyType, return true
        if (ancestorNS != null &&
            ancestorNS.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA) &&
            ancestorName.equals(SchemaSymbols.ATTVAL_ANYTYPE)) {
            return true;
        }

        // recursively get base, and compare it with ancestor
        XSTypeDefinition type = this;
        while (!(ancestorName.equals(type.getName()) &&
                 ((ancestorNS == null && type.getNamespace() == null) ||
                  (ancestorNS != null && ancestorNS.equals(type.getNamespace())))) &&   // compare with ancestor
               type != SchemaGrammar.fAnySimpleType &&  // reached anySimpleType
               type != SchemaGrammar.fAnyType) {        // reached anyType
            type = (XSTypeDefinition)type.getBaseType();
        }

        return type != SchemaGrammar.fAnySimpleType &&
        type != SchemaGrammar.fAnyType;
    }

    public void reset(){
        fName = null;
        fTargetNamespace = null;
        fBaseType = null;
        fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
        fFinal = XSConstants.DERIVATION_NONE;
        fBlock = XSConstants.DERIVATION_NONE;

        fMiscFlags = 0;

        // reset attribute group
        fAttrGrp.reset();
        fContentType = CONTENTTYPE_EMPTY;
        fXSSimpleType = null;
        fParticle = null;
        fCMValidator = null;
        if(fAnnotations != null) {
            // help out the garbage collector
            fAnnotations.clear();
        }
        fAnnotations = null;
    }

    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.TYPE_DEFINITION;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return getAnonymous() ? null : fName;
    }

    /**
     * A boolean that specifies if the type definition is anonymous.
     * Convenience attribute. This is a field is not part of
     * XML Schema component model.
     */
    public boolean getAnonymous() {
        return((fMiscFlags & CT_IS_ANONYMOUS) != 0);
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return fTargetNamespace;
    }

    /**
     * {base type definition} Either a simple type definition or a complex
     * type definition.
     */
    public XSTypeDefinition getBaseType() {
        return fBaseType;
    }

    /**
     * {derivation method} Either extension or restriction. The valid constant
     * value for this <code>XSConstants</code> EXTENTION, RESTRICTION.
     */
    public short getDerivationMethod() {
        return fDerivedBy;
    }

    /**
     * {final} For complex type definition it is a subset of {extension,
     * restriction}. For simple type definition it is a subset of
     * {extension, list, restriction, union}.
     * @param derivation  Extension, restriction, list, union constants
     *   (defined in <code>XSConstants</code>).
     * @return True if derivation is in the final set, otherwise false.
     */
    public boolean isFinal(short derivation) {
        return (fFinal & derivation) != 0;
    }

    /**
     * {final} For complex type definition it is a subset of {extension, restriction}.
     *
     * @return A bit flag that represents:
     *         {extension, restriction) or none for complexTypes;
     *         {extension, list, restriction, union} or none for simpleTypes;
     */
    public short getFinal() {
        return fFinal;
    }

    /**
     * {abstract} A boolean. Complex types for which {abstract} is true must
     * not be used as the {type definition} for the validation of element
     * information items.
     */
    public boolean getAbstract() {
        return((fMiscFlags & CT_IS_ABSTRACT) != 0);
    }

    /**
     *  {attribute uses} A set of attribute uses.
     */
    public XSObjectList getAttributeUses() {
        return fAttrGrp.getAttributeUses();
    }

    /**
     * {attribute wildcard} Optional. A wildcard.
     */
    public XSWildcard getAttributeWildcard() {
        return fAttrGrp.getAttributeWildcard();
    }

    /**
     * {content type} One of empty, a simple type definition (see
     * <code>simpleType</code>, or mixed, element-only (see
     * <code>cmParticle</code>).
     */
    public short getContentType() {
        return fContentType;
    }

    /**
     * A simple type definition corresponding to simple content model,
     * otherwise <code>null</code>
     */
    public XSSimpleTypeDefinition getSimpleType() {
        return fXSSimpleType;
    }

    /**
     * A particle for mixed or element-only content model, otherwise
     * <code>null</code>
     */
    public XSParticle getParticle() {
        return fParticle;
    }

    /**
     * {prohibited substitutions} A subset of {extension, restriction}.
     * @param prohibited  extention or restriction constants (defined in
     *   <code>XSConstants</code>).
     * @return True if prohibited is a prohibited substitution, otherwise
     *   false.
     */
    public boolean isProhibitedSubstitution(short prohibited) {
        return (fBlock & prohibited) != 0;
    }

    /**
     * {prohibited substitutions}
     *
     * @return A bit flag corresponding to prohibited substitutions
     */
    public short getProhibitedSubstitutions() {
        return fBlock;
    }

    /**
     * Optional. Annotation.
     */
    public XSObjectList getAnnotations() {
        return fAnnotations;
    }
    
	/**
	 * @see org.apache.xerces.xs.XSObject#getNamespaceItem()
	 */
	public XSNamespaceItem getNamespaceItem() {
        // REVISIT: implement
		return null;
	}

    /* (non-Javadoc)
     * @see org.apache.xerces.xs.XSComplexTypeDefinition#getAttributeUse(java.lang.String, java.lang.String)
     */
    public XSAttributeUse getAttributeUse(String namespace, String name) {
         return fAttrGrp.getAttributeUse(namespace, name);
    }

} // class XSComplexTypeDecl
