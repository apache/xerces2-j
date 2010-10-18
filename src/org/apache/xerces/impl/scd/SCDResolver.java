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

package org.apache.xerces.impl.scd;

import java.util.ArrayList;
import java.util.List;

import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeGroupDefinition;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSIDCDefinition;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSModelGroupDefinition;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNotationDeclaration;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;

/**
 * Implements XML Schema: Component Designators (SCD)
 * Currently, this implementation has following limitations<br>
 * 1. the Schema Step is not supported<br>
 * 2. the axis types; Extension axis, Assertions axis, Alternative axis, Context axis<br>
 *    are not supported<br>
 * 3. Extension accessors are not supported<br>
 * 4. the top level Identity Constraint Definitions components are not supported.<br>
 * 5. all the other Schema 1.1 constructs that are not listed here, are not supported.<br>
 * 6. the schemaAttribute axis does not work as it is expected in the specification.<br>
 * 7. in some situations, the SCPs that have been reduced by the elided-componet axis do not<br>
 *    produce expected results<br>
 * 8. the fundamental facets are not supported (but the constraining facets are supported).<br>
 * @author Ishan Jayawardena <udeshike@gmail.com>
 * @version $Id$
 */
public class SCDResolver {
    private XSModel xsModel;
    private List result;
    private List currentComponents;
    private SCDParser parser;
    private static final short NO_FILTER = -1;
    /*
     *  Please note that the spec has some flaws regarding some of the facts mentioned in it 
     *  and therefore we could not interpret the correct meaning of them. 
     *  But we assumed the intended behavior, i.e. the behavior the spec might have expected and 
     *  implemented according to it.
     *  By setting the following variable's (i.e.IS_SPEC_COMPLIANT) value to false, 
     *  we can test the behavior of the resolver as we assumed it, 
     *  but it's not compliant with the spec. By setting its to true, 
     *  we can test the resolver's behavior in the spec compliant manner 
     *  (but under this behavior, the parser will not produce any output).
     */
    private static final boolean IS_SPEC_COMPLIANT = false;
    private static final short LIST_SIZE = 30;

    /**
     * Constructor
     * @param xsModel the schema description schema component
     */
    public SCDResolver(XSModel xsModel) {
        this.xsModel = xsModel;
        result = new ArrayList(LIST_SIZE);
        currentComponents = new ArrayList(LIST_SIZE);
        parser = new SCDParser();
    }

    /**
     * Resolves a relative SCD against the schema description schema component (i.e. the XSModel).
     * @param relativeSCD the input relative SCD string in the form of,<br>
     * <code>[5] RelativeSchemaComponentDesignator ::=  XmlnsPointerPart* XscdPointerPart</code><br>
     * e.g. <code>xmlns(p=http://www.example.com/schema/po)xscd(/type::p:SKU/facet::pattern)</code>
     * @return a list of XML schema components that are designated by the SCD, otherwise and empty
     * <code>XSObjectList</code>
     */
    public XSObjectList resolve(String relativeSCD) throws SCDException {
        List steps = parser.parseRelativeSCD(relativeSCD, false);
        if (steps.size() == 1
                && ((Step) steps.get(0)).getAxisType() == Axis.NO_AXIS
                && ((Step) steps.get(0)).getNametest() == null
                && ((Step) steps.get(0)).getPredicate() == 0) {
            // this is the schema step. i.e the SCP '/'
            // return xsModel; this is the ideal case
            // we return an exception instead since XSModel does not implement
            // XSObject interface yet
            throw new SCDException("Error in SCD: Schema step is not supported");
        }
        // apply the first step out from out side
        // TODO: this is strange but this what the spec says and this can be
        // changed in a better way if the spec changes
        result.clear();
        applyFirstStep((Step) steps.get(0));
        return evaluate(steps, 1);
    }

    /**
     * Resolves an SCP against the schema description schema component (i.e. the XSModel).
     * @param scp the input SCP to designate the components.<br>
     * e.g. <code>/type::p:SKU/facet::pattern</code>
     * @param nsContext namespace context details for the component names used in the SCP string
     * @return a list of XML schema components that are designated by the SCP, otherwise and empty
     * <code>XSObjectList</code>
     */
    public XSObjectList resolve(String scp, NamespaceContext nsContext)
    throws SCDException {
        List steps = parser.parseSCP(scp, nsContext, false);
        if (steps.size() == 1
                && ((Step) steps.get(0)).getAxisType() == Axis.NO_AXIS
                && ((Step) steps.get(0)).getNametest() == null
                && ((Step) steps.get(0)).getPredicate() == 0) {
            // this is the schema step. i.e the SCP '/'
            // return xsModel; this is the ideal case.
            // we return an exception instead since XSModel does not implement
            // XSObject interface yet
            throw new SCDException("Error in SCD: Schema step is not supported");
        }
        // apply the first step out from out side
        // TODO: this is strange but this what the spec says and this can be
        // changed in a better way if the spec changes
        result.clear();
        applyFirstStep((Step) steps.get(0));
        return evaluate(steps, 1);
    }

    /**
     * Resolves an incomplete SCP against a given schema component
     * @param incompleteSCP the incomplete SCP.
     * To emphasize the incompleteness of such paths, the current component step syntax may be used 
     * (.) for the head step. For example, if the initial source component is a complex type, 
     * the following paths are equivalent and designate the element declaration with the QName 
     * my:section within the sequence model group of that type:<br>
     * <code>model::sequence/schemaElement::my:section</code><br>
     * <code>./model::sequence/schemaElement::my:section</code>
     * @param nsContext namespace context details for the component names used in the 
     * incomplete SCP string 
     * @param currentComponent the initial source component
     * @return the list of schema components that are designated by the incomplete SCP,
     * otherwise an empty <code>XSObjectList</code>.
     */
    public XSObjectList resolve(String incompleteSCP,
            NamespaceContext nsContext, XSObject currentComponent)
    throws SCDException {
        List steps = parser.parseSCP(incompleteSCP, nsContext, true);
        result.clear();
        result.add(currentComponent);
        return evaluate(steps, 0);
    }

    /**
     * Resolves an incomplete SCD against a given schema component
     * @param incompleteSCD the incomplete SCD string. which is in the form of <br>
     * <code>[5] RelativeSchemaComponentDesignator ::=  XmlnsPointerPart* XscdPointerPart</code><br>
     * but <code>XscdPointerPart</code> contains an incomplete SCP instead of a complete SCP. <br>
     * e.g. <code>xmlns(p=http://www.example.com/schema/po)xscd(./type::p:SKU/facet::pattern)</code>
     * or <code>xmlns(p=http://www.example.com/schema/po)xscd(type::p:SKU/facet::pattern)</code><br>
     * i.e. an incomplete SCP must not start with a '/'.
     * @param currentComponent the initial source component
     * @return the list of schema components that are designated by the incomplete SCP,
     * otherwise an empty <code>XSObjectList</code>.
     */
    public XSObjectList resolve(String incompleteSCD, XSObject currentComponent)
    throws SCDException {
        List steps = parser.parseRelativeSCD(incompleteSCD, true);
        result.clear();
        result.add(currentComponent);
        return evaluate(steps, 0);
    }

    private XSObjectList evaluate(List steps, int startingStep) throws SCDException {
        for (int i = startingStep, nSteps = steps.size(); i < nSteps; ++i) {
            currentComponents.clear();
            Step step = (Step)steps.get(i);
            short axisType = step.getAxisType();
            for (int j = 0, n = result.size(); j < n; ++j) {
                currentComponents.add(result.get(j));
            }
            if (axisType != Axis.SPECIAL_COMPONENT) {
                for (int j = 0, n = currentComponents.size(); j < n; ++j) {
                    addElidedComponents((XSObject)currentComponents.get(j));
                }
            }
            result.clear();
            applyStep(step);
            if (axisType == Axis.SPECIAL_COMPONENT) {
                step = (Step) steps.get(++i); // TODO: is this correct?
                // copy result => currentComps
                List tmp = currentComponents;
                currentComponents = result;
                result = tmp;
                result.clear();
                applyStep(step);
            }
        }
        XSObjectListImpl resultComps = new XSObjectListImpl();
        for (int i = 0, n = result.size(); i < n; ++i) {
            resultComps.addXSObject((XSObject)result.get(i));
        }
        return resultComps;
    }

    private void addElidedComponents(XSObject sourceComponent) {
        // these are the components returned from the term() accessor whose component-kind() is equal to xscd:model-group
        // currentComponents.size() gets changed in each iteration
        for (int i = currentComponents.size() - 1; i < currentComponents.size(); ++i) {
            term((XSObject)currentComponents.get(i), XSConstants.MODEL_GROUP, SCDParser.WILDCARD, currentComponents);
        }

        switch (sourceComponent.getType()) {
        case XSConstants.ELEMENT_DECLARATION: {
            XSObject typeDef = ((XSElementDeclaration)sourceComponent).getTypeDefinition();
            if (typeDef != null && !currentComponents.contains(typeDef)) {
                currentComponents.add(typeDef);
            }
        }
        break;
        case XSConstants.ATTRIBUTE_DECLARATION: {
            XSObject typeDef = ((XSAttributeDeclaration)sourceComponent).getTypeDefinition();
            if (typeDef != null && !currentComponents.contains(typeDef)) {
                currentComponents.add(typeDef);
            }
        }
        break;
        }
        // TODO: we dont have type alternative for now.
    } // getElidedComponents()

    // apply the first step for the components of result
    private void applyFirstStep(Step step) throws SCDException {
        XSNamedMap map = null;

        switch (step.getAxisType()) {
        case Axis.ANNOTATION:
            XSObjectList annotations = xsModel.getAnnotations();
            for (int i = 0, n = annotations.size(); i < n; ++i) {
                addComponent(annotations.item(i), step.getNametest(), result);
            }
            break;
        case Axis.SCHEMA_ELEMENT:
            map = xsModel.getComponents(XSConstants.ELEMENT_DECLARATION);
            break;
        case Axis.TYPE:
            map = xsModel.getComponents(XSConstants.TYPE_DEFINITION);
            break;
        case Axis.SCHEMA_ATTRIBUTE:
            map = xsModel.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
            break;
        case Axis.ATTRIBUTE_GROUP:
            map = xsModel.getComponents(XSConstants.ATTRIBUTE_GROUP);
            break;
        case Axis.GROUP:
            map = xsModel.getComponents(XSConstants.MODEL_GROUP_DEFINITION); // TODO: correct?
            break;
        case Axis.NOTATION:
            map = xsModel.getComponents(XSConstants.NOTATION_DECLARATION);
            break;
        case Axis.COMPONENT: 
        case Axis.SPECIAL_COMPONENT: {
            currentComponents.clear();
            addTopLevelComponents(step.getNametest());
            int size = currentComponents.size();
            for (int i = 0; i < currentComponents.size(); ++i) {
                componentChildren((XSObject)currentComponents.get(i), NO_FILTER, SCDParser.WILDCARD, currentComponents);
            }
            int start = step.getAxisType() == Axis.SPECIAL_COMPONENT ? 0 : size;
            for (int i = start; i < currentComponents.size(); ++i) {
                addComponent((XSObject)currentComponents.get(i), step.getNametest(), result);
            }
        }
        break;
        default:
            throw new SCDException("Error in SCD: Unsupported top level component type "
                    + step.getAxisName());
        }
        if (map != null && !map.isEmpty()) {
            for (int i = 0, n = map.size(); i < n; ++i) {
                addComponent(map.item(i), step.getNametest(), result);
            }
        }
        applyPredicate(step.getPredicate());
    } // applyFirstStep()

    // apply the step for the components of result.
    // starting from the second step of the step list of a given SCP
    private void applyStep(Step step) throws SCDException {
        switch (step.getAxisType()) {
        case Axis.SCHEMA_ELEMENT:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                term((XSObject)currentComponents.get(i), XSConstants.ELEMENT_DECLARATION, step.getNametest(), result);
            }
            break;
        case Axis.SCHEMA_ATTRIBUTE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.ATTRIBUTE_DECLARATION, step.getNametest(), result);
            }
            break;
        case Axis.TYPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentChildren((XSObject)currentComponents.get(i), XSConstants.TYPE_DEFINITION, step.getNametest(), result);
            }
            break;
        case Axis.CURRENT_COMPONENT:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                result.add(currentComponents.get(i));
            }
            return;
        case Axis.COMPONENT: // TODO: correct?
        case Axis.SPECIAL_COMPONENT: {
            int size = currentComponents.size();
            for (int i = 0; i < currentComponents.size(); ++i) {
                componentChildren((XSObject)currentComponents.get(i), NO_FILTER, SCDParser.WILDCARD, currentComponents);
            }
            int start = step.getAxisType() == Axis.SPECIAL_COMPONENT ? 0 : size;
            for (int i = start; i < currentComponents.size(); ++i) {
                addComponent((XSObject)currentComponents.get(i), step.getNametest(), result);
            }
        }
        break;
        case Axis.ATTRIBUTE_GROUP:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.ATTRIBUTE_GROUP, step.getNametest(), result);
            }
            break;
        case Axis.GROUP:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.MODEL_GROUP_DEFINITION, step.getNametest(), result);
            }
            break;
        case Axis.IDENTITY_CONSTRAINT:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.IDENTITY_CONSTRAINT, step.getNametest(), result);
            }
            break;
        case Axis.ASSERTION:
            // TODO: we do not support this yet. Schema 1.1
            throw new SCDException("Error in SCD: Assertion axis is not supported");
            //break;
        case Axis.ALTERNATIVE:
            // TODO: we do not support this yet. is Schema 1.1
            throw new SCDException("Error in SCD: Alternative axis is not supported");
            //break;
        case Axis.NOTATION:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.NOTATION_DECLARATION, step.getNametest(), result);
            }
            break;
        case Axis.MODEL:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                term((XSObject)currentComponents.get(i), XSConstants.MODEL_GROUP, step.getNametest(), result);
            }
            break;
        case Axis.ANY_ATTRIBUTE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                XSObject comp = ((XSObject)currentComponents.get(i));
                short type = comp.getType();
                if (type == XSConstants.TYPE_DEFINITION) {
                    type = ((XSTypeDefinition)comp).getTypeCategory();
                    if (type == XSTypeDefinition.COMPLEX_TYPE) {
                        addComponent(((XSComplexTypeDefinition)comp).getAttributeWildcard(),
                                step.getNametest(), result);
                    }

                } else if (type == XSConstants.ATTRIBUTE_GROUP) {
                    addComponent(((XSAttributeGroupDefinition)comp).getAttributeWildcard(),
                            step.getNametest(), result);
                }
            }
            break;
        case Axis.ANY:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                term((XSObject)currentComponents.get(i), XSConstants.WILDCARD, step.getNametest(), result);
            }
            break;
        case Axis.FACET: // we are returning the part of thefacets. a limitation
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentLinked((XSObject)currentComponents.get(i), XSConstants.FACET, step.getNametest(), result);
            }
            break;
        case Axis.SCOPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                componentScope((XSObject)currentComponents.get(i), result);
            }
            break;
        case Axis.CONTEXT:
            // TODO: we do not support this yet. is Schema 1.1
            throw new SCDException("Error in SCD: Context axis is not supported");
            //break;
        case Axis.SUBSTITUTION_GROUP:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                XSObject comp = (XSObject)currentComponents.get(i);
                if (comp.getType() == XSConstants.ELEMENT_DECLARATION) {
                    addComponent(((XSElementDeclaration)comp).getSubstitutionGroupAffiliation(), 
                            step.getNametest(), result);
                }
            }
            break;
        case Axis.BASE_TYPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                if (((XSObject)currentComponents.get(i)).getType() == XSConstants.TYPE_DEFINITION) {
                    addComponent((((XSTypeDefinition)currentComponents.get(i))).getBaseType(),
                            step.getNametest(), result);
                }
            }
            break;
        case Axis.ITEM_TYPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                if (((XSObject)currentComponents.get(i)).getType() == XSConstants.TYPE_DEFINITION) {
                    XSObject comp = (XSObject)currentComponents.get(i);
                    if (((XSTypeDefinition)comp).getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                        addComponent(((XSSimpleTypeDefinition)comp).getItemType(),
                                step.getNametest(), result);
                    }
                }
            }
            break;
        case Axis.MEMBER_TYPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                if (((XSObject)currentComponents.get(i)).getType() == XSConstants.TYPE_DEFINITION) {
                    XSObject comp = (XSObject)currentComponents.get(i);
                    if (((XSTypeDefinition)comp).getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                        XSObjectList memberTypes = ((XSSimpleTypeDefinition)comp).getMemberTypes();
                        for (int j = 0, nt = memberTypes.size(); j < nt; ++j) {
                            addComponent((XSObject)memberTypes.get(j), step.getNametest(), result);
                        }
                    }
                }
            }
            break;
        case Axis.PRIMITIVE_TYPE:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                if (((XSObject)currentComponents.get(i)).getType() == XSConstants.TYPE_DEFINITION) {
                    XSObject comp = (XSObject)currentComponents.get(i);
                    if (((XSTypeDefinition)comp).getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                        addComponent(((XSSimpleTypeDefinition)comp).getPrimitiveType(),
                                step.getNametest(), result);
                    }
                }
            }
            break;
        case Axis.KEY:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                if (((XSObject)currentComponents.get(i)).getType() == XSConstants.IDENTITY_CONSTRAINT) {
                    addComponent(((XSIDCDefinition)currentComponents.get(i)).getRefKey(),
                            step.getNametest(), result);
                }
            }
            break;
        case Axis.ANNOTATION:
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                annotations((XSObject)currentComponents.get(i), result);
            }
            break;
        case Axis.ATTRIBUTE_USE: {
            XSObjectList attribUses = null;
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                XSObject comp = (XSObject)currentComponents.get(i);
                if (comp.getType() == XSConstants.TYPE_DEFINITION) {
                    if (((XSTypeDefinition)comp).getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                        attribUses = ((XSComplexTypeDefinition)comp).getAttributeUses();
                    }
                } else if (comp.getType() == XSConstants.ATTRIBUTE_GROUP) {
                    attribUses = ((XSAttributeGroupDefinition)comp).getAttributeUses();
                }
                if (attribUses != null) {
                    for (int j = 0, na = attribUses.size(); j < na; ++j) {
                        addComponent((XSObject)attribUses.get(j),
                                step.getNametest(), result);
                    }
                }
            }
        }
        break;
        case Axis.PARTICLE: {
            for (int i = 0, n = currentComponents.size(); i < n; ++i) {
                XSObject comp = (XSObject)currentComponents.get(i);
                if (comp.getType() == XSConstants.MODEL_GROUP) {
                    XSObjectList particles = ((XSModelGroup)comp).getParticles();
                    for (int j = 0, np = particles.size(); j < np; ++j) {
                        addComponent((XSObject) particles.get(j),
                                step.getNametest(), result);
                    }
                }
            }
        }
        break;
        case Axis.EXTENSION_AXIS:
            throw new SCDException("Error in SCD: Extension axis is not supported");
            //break;
        default:
            throw new SCDException("Error in SCD: Unsupported axis type " + step.getAxisName());
        }
        applyPredicate(step.getPredicate());
    } // applyStep()

    private void addTopLevelComponents(QName nameTest) {
        // get the annotations() components
        XSObjectList annotations = xsModel.getAnnotations();
        for (int i = 0, n = annotations.size(); i < n; ++i) {
            addComponent(annotations.item(i), nameTest, result);
        }
        final short[] SCHEMA_COMPONENTS = new short[] {
                XSConstants.ELEMENT_DECLARATION, XSConstants.TYPE_DEFINITION, XSConstants.ATTRIBUTE_DECLARATION,
                XSConstants.ATTRIBUTE_GROUP, XSConstants.MODEL_GROUP_DEFINITION, XSConstants.NOTATION_DECLARATION,
                // XSConstants.IDENTITY_CONSTRAINT // TODO: return empty. we don't support IDC at top level
        };
        XSNamedMap map;
        for (int i = 0; i < SCHEMA_COMPONENTS.length; ++i) {
            map = xsModel.getComponents(SCHEMA_COMPONENTS[i]);
            if (!map.isEmpty()) {
                for (int j = 0, n =map.size(); j < n; ++j) {
                    addComponent(map.item(i), nameTest, result);
                }
            }
        }
    } // getTopLevelComponents()

    private void applyPredicate(int predicate) throws SCDException {
        if (predicate == 0) {
            return;
        } else if (predicate > 0 && predicate <= result.size()) {
            XSObject component = (XSObject)result.get(predicate - 1);
            result.clear();
            result.add(component);
        } else {
            throw new SCDException("Error in SCD: Invalid predicate value (" 
                    + predicate + ") detected");
        }
    } // processPredicate()

    // Described in section 4.5.6; term Accessor
    private void term(XSObject sourceComponent, short filter, QName nameTest, List outputComponents) {
        switch (sourceComponent.getType()) {
        case XSConstants.MODEL_GROUP_DEFINITION:       
            if (NO_FILTER == filter || XSConstants.MODEL_GROUP == filter) {
                addComponent(((XSModelGroupDefinition) sourceComponent).getModelGroup()
                        , nameTest, outputComponents);
            }
            break;
        case XSConstants.TYPE_DEFINITION:
            if (((XSTypeDefinition) sourceComponent).getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                XSParticle particle = ((XSComplexTypeDefinition) sourceComponent).getParticle();
                if (particle != null) {
                    XSTerm term = particle.getTerm();
                    if (NO_FILTER == filter || (term != null && term.getType() == filter)) {
                        addComponent(term, nameTest, outputComponents);
                    }
                }
            }
            break;
        case XSConstants.PARTICLE: {
            XSTerm term = ((XSParticle) sourceComponent).getTerm();
            if (NO_FILTER == filter || (term != null && term.getType() == filter)) {
                addComponent(term, nameTest, outputComponents);
            }
        }
        break;
        case XSConstants.MODEL_GROUP:
            if (IS_SPEC_COMPLIANT == false) {
                XSObjectList particles = ((XSModelGroup) sourceComponent).getParticles();
                for (int i = 0, n = particles.size(); i < n; ++i) {
                    XSObject term = ((XSParticle) particles.item(i)).getTerm();
                    if (NO_FILTER == filter
                            || (term != null && term.getType() == filter)) {
                        addComponent(term, nameTest, outputComponents); // nameTest
                    }
                }

            }
            break;
        }    
    } // term()

    // Described in section 4.5.3; component-variety Accessor
    private String componentVariety(XSObject component) {
        // TODO: we only care about these three types of components
        short type = component.getType();
        if (type == XSConstants.MODEL_GROUP) {
            switch (((XSModelGroup)component).getCompositor()) {
            case XSModelGroup.COMPOSITOR_SEQUENCE:
                return "sequence";
            case XSModelGroup.COMPOSITOR_ALL:
                return "all";
            case XSModelGroup.COMPOSITOR_CHOICE:
                return "choice";
            }
        } else if (type == XSConstants.FACET
                || type == XSConstants.MULTIVALUE_FACET) {
            short kind = NO_FILTER;
            if (type == XSConstants.FACET) {
                kind = ((XSFacet)component).getFacetKind();
            } else {
                kind = ((XSMultiValueFacet)component).getFacetKind();
            }
            switch (kind) { // TODO: which ones are the most frequently used ones?
            case XSSimpleTypeDefinition.FACET_ENUMERATION:
                return "enumeration";
            case XSSimpleTypeDefinition.FACET_FRACTIONDIGITS:
                return "fractionDigits";
            case XSSimpleTypeDefinition.FACET_LENGTH:
                return "length";
            case XSSimpleTypeDefinition.FACET_MAXEXCLUSIVE:
                return "maxExclusive";
            case XSSimpleTypeDefinition.FACET_MAXINCLUSIVE:
                return "maxInclusive";
            case XSSimpleTypeDefinition.FACET_MAXLENGTH:
                return "maxLength";
            case XSSimpleTypeDefinition.FACET_MINEXCLUSIVE:
                return "minExclusive";
            case XSSimpleTypeDefinition.FACET_MININCLUSIVE:
                return "minInclusive";
            case XSSimpleTypeDefinition.FACET_MINLENGTH:
                return "minLength";
            case XSSimpleTypeDefinition.FACET_PATTERN:
                return "pattern";
            case XSSimpleTypeDefinition.FACET_TOTALDIGITS:
                return "totalDigits";
            case XSSimpleTypeDefinition.FACET_WHITESPACE:
                return "whiteSpace";
            }
        }
        return null;
    } // componentVariety()

    // Described in section 4.5.5; component-linked Accessor
    private void componentLinked(XSObject sourceComponent, short filter,
            QName nameTest, List targetComponents) throws SCDException {
        switch (sourceComponent.getType()) {
        case XSConstants.ATTRIBUTE_DECLARATION: {
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) { // TODO: correct? check with the caller
                if (((XSAttributeDeclaration)sourceComponent).getScope() == XSConstants.SCOPE_LOCAL) {
                    addComponent(((XSAttributeDeclaration)sourceComponent).getEnclosingCTDefinition(),
                            nameTest, targetComponents);
                }
            }
        }
        break;
        case XSConstants.ELEMENT_DECLARATION: {
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) { // TODO: correct? check again
                if (((XSElementDeclaration)sourceComponent).getScope() == XSConstants.SCOPE_LOCAL) {
                    addComponent(((XSElementDeclaration)sourceComponent).getEnclosingCTDefinition(),
                            nameTest, targetComponents);
                }
            }
            if (NO_FILTER == filter || XSConstants.IDENTITY_CONSTRAINT == filter) {
                XSNamedMap idcs = ((XSElementDeclaration)sourceComponent).getIdentityConstraints();
                for (int i = 0, n = idcs.size(); i < n; ++i) {
                    addComponent(idcs.item(i),
                            nameTest, targetComponents);
                }
            }
            if (NO_FILTER == filter || XSConstants.ELEMENT_DECLARATION == filter) {
                addComponent(((XSElementDeclaration)sourceComponent).getSubstitutionGroupAffiliation(),
                        nameTest, targetComponents);
            }
        }
        break;
        case XSConstants.TYPE_DEFINITION:
            if (((XSTypeDefinition)sourceComponent).getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                componentChildren(sourceComponent, filter, nameTest, targetComponents);
                if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                    annotations(sourceComponent, targetComponents);
                }
                if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) {
                    addComponent(((XSSimpleTypeDefinition)sourceComponent).getBaseType(),
                            nameTest, targetComponents);
                    addComponent(((XSSimpleTypeDefinition)sourceComponent).getPrimitiveType(),
                            nameTest, targetComponents);
                    addComponent(((XSSimpleTypeDefinition)sourceComponent).getItemType(),
                            nameTest, targetComponents);
                    XSObjectList list = ((XSSimpleTypeDefinition)sourceComponent).getMemberTypes();
                    for (int i = 0, n = list.size(); i < n; ++i) {
                        addComponent(list.item(i), nameTest, targetComponents);
                    }
                }
                // TODO: {context} not supported since it's defined in Schema 1.1
            } else { // a complex type
                componentChildren(sourceComponent, filter, nameTest, targetComponents);
                if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                    annotations(sourceComponent, targetComponents);
                }
                if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) {
                    addComponent(((XSComplexTypeDefinition)sourceComponent).getBaseType(),
                            nameTest, targetComponents);
                }
                if (NO_FILTER == filter || XSConstants.WILDCARD == filter) {
                    addComponent(((XSComplexTypeDefinition)sourceComponent).getAttributeWildcard(),
                            nameTest, targetComponents);
                }
                // TODO: how to get these other components?
                // {context} this is Schema 1.1 stuff
                // {assertions} this is Schema 1.1 stuff
                // {wildcard} of {open content} of {content type} this is also schema 1.1 stuff
            }
            break;
        case XSConstants.ATTRIBUTE_USE:
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.ATTRIBUTE_GROUP:
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            if (NO_FILTER == filter || XSConstants.WILDCARD == filter) {
                addComponent(((XSAttributeGroupDefinition)sourceComponent).getAttributeWildcard(),
                        nameTest, targetComponents);
            }
            break;
        case XSConstants.MODEL_GROUP_DEFINITION:
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.MODEL_GROUP:
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.PARTICLE:
            componentChildren(sourceComponent, filter, nameTest, targetComponents);
            break;
        case XSConstants.WILDCARD:
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.IDENTITY_CONSTRAINT:
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            if (NO_FILTER == filter || XSConstants.IDENTITY_CONSTRAINT == filter) {
                addComponent(((XSIDCDefinition)sourceComponent).getRefKey(),
                        nameTest, targetComponents);
            }
            break; // 
        case XSConstants.NOTATION_DECLARATION:
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.FACET:
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        case XSConstants.MULTIVALUE_FACET:
            if (NO_FILTER == filter || XSConstants.ANNOTATION == filter) {
                annotations(sourceComponent, targetComponents);
            }
            break;
        default: // this includes the case XSConstants.ANNOTATION
            // TODO: we have to add support for, type alternative, assertion, schema, constraining facet and fundamental facet
            break;
        }
    } // componentLinked()

    // Described in section 4.5.4; component-children Accessor
    private void componentChildren(XSObject sourceComponent, short filter, QName nameTest, List targetComponents) {
        switch (sourceComponent.getType()) {
        case XSConstants.ATTRIBUTE_DECLARATION: {
            if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) {
                addComponent(((XSAttributeDeclaration)sourceComponent).getTypeDefinition(),
                        nameTest, targetComponents);
            }
        }
        break;
        case XSConstants.ELEMENT_DECLARATION: {
            if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) {
                addComponent(((XSElementDeclaration)sourceComponent).getTypeDefinition(),
                        nameTest, targetComponents);
            }
        }
        break;
        case XSConstants.TYPE_DEFINITION:
            if (((XSTypeDefinition)sourceComponent).getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                if (NO_FILTER == filter || XSConstants.FACET == filter) {
                    XSObjectList facets = ((XSSimpleTypeDefinition)sourceComponent).getFacets();
                    for (int i = 0, n = facets.size(); i < n; ++i) {
                        addComponent(facets.item(i), nameTest, targetComponents);
                    }
                    facets = ((XSSimpleTypeDefinition)sourceComponent).getMultiValueFacets();
                    for (int i = 0, n = facets.size(); i < n; ++i) {
                        addComponent(facets.item(i), nameTest, targetComponents);
                    }
                }
            } else { // complex type
                XSComplexTypeDefinition cmplxType = ((XSComplexTypeDefinition)sourceComponent);
                if (NO_FILTER == filter || XSConstants.ATTRIBUTE_USE == filter) {
                    XSObjectList attributeUses = cmplxType.getAttributeUses();
                    for (int i = 0, n = attributeUses.size(); i < n; ++i) {
                        addComponent(attributeUses.item(i), nameTest, targetComponents);
                    }
                }
                int componentVariety = cmplxType.getContentType();
                switch (componentVariety) {
                case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
                    break;
                case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
                    if (NO_FILTER == filter || XSConstants.TYPE_DEFINITION == filter) {
                        addComponent(cmplxType.getSimpleType(), nameTest, targetComponents);
                    }
                    break;
                default:
                    term(cmplxType, filter, nameTest, targetComponents);
                break;
                }
            }
            break;
        case XSConstants.ATTRIBUTE_USE:
            if (NO_FILTER == filter || XSConstants.ATTRIBUTE_DECLARATION == filter) {
                addComponent(((XSAttributeUse)sourceComponent).getAttrDeclaration(),
                        nameTest, targetComponents);
            }
            break;
        case XSConstants.ATTRIBUTE_GROUP:
            if (NO_FILTER == filter || XSConstants.ATTRIBUTE_DECLARATION == filter) {
                XSObjectList attrbuses = ((XSAttributeGroupDefinition) sourceComponent).getAttributeUses();
                for (int i = 0, n = attrbuses.size(); i < n; ++i) {
                    addComponent(((XSAttributeUse) attrbuses.item(i)).getAttrDeclaration(),
                            nameTest, targetComponents);
                }
            }
            break;
        case XSConstants.MODEL_GROUP_DEFINITION:
            if (NO_FILTER == filter || XSConstants.MODEL_GROUP == filter) {
                addComponent(((XSModelGroupDefinition)sourceComponent).getModelGroup(),
                        nameTest, targetComponents);
            }
            break;
        case XSConstants.MODEL_GROUP: {
            XSObjectList particles = ((XSModelGroup)sourceComponent).getParticles();
            for (int i = 0, n = particles.size(); i < n; ++i) {
                XSTerm term = ((XSParticle)particles.item(i)).getTerm();
                if (NO_FILTER == filter || term.getType() == filter) {
                    addComponent(term, nameTest, targetComponents);
                }
            }
        }
        break;
        case XSConstants.PARTICLE: {
            XSTerm term = ((XSParticle)sourceComponent).getTerm();
            if (NO_FILTER == filter || term.getType() == filter) {
                addComponent(term, nameTest, targetComponents);
            }
        }
        break;
        default:
            break; // TODO: cases Schema and Type Alternative yet to be implemented
        }   
    } // componentChildren()

    // Described in section 4.5.9; annotations Accessor
    private void annotations(XSObject sourceComponent, List targetComponents) 
    throws SCDException {
        XSObjectList annotations;
        switch (sourceComponent.getType()) {
        case XSConstants.ATTRIBUTE_DECLARATION:
            annotations = ((XSAttributeDeclaration)sourceComponent).getAnnotations();
            break;
        case XSConstants.ELEMENT_DECLARATION:
            annotations = ((XSElementDeclaration)sourceComponent).getAnnotations();
            break;
        case XSConstants.TYPE_DEFINITION:
            if (((XSTypeDefinition)sourceComponent).getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                annotations = ((XSComplexTypeDefinition)sourceComponent).getAnnotations();
            } else { // simple type def
                annotations = ((XSSimpleTypeDefinition)sourceComponent).getAnnotations();
            }
            break;
        case XSConstants.ATTRIBUTE_USE:
            annotations = ((XSAttributeUse)sourceComponent).getAnnotations();
            break;
        case XSConstants.ATTRIBUTE_GROUP:
            annotations = ((XSAttributeGroupDefinition)sourceComponent).getAnnotations();
            break;
        case XSConstants.MODEL_GROUP:
            annotations = ((XSModelGroup)sourceComponent).getAnnotations();
            break;
        case XSConstants.MODEL_GROUP_DEFINITION:
            annotations = ((XSModelGroupDefinition)sourceComponent).getAnnotations();
            break;
        case XSConstants.PARTICLE:
            annotations = ((XSParticle)sourceComponent).getAnnotations();
            break;
        case XSConstants.WILDCARD:
            annotations = ((XSWildcard)sourceComponent).getAnnotations();
            break;
        case XSConstants.IDENTITY_CONSTRAINT:
            annotations = ((XSIDCDefinition)sourceComponent).getAnnotations();
            break;
        case XSConstants.NOTATION_DECLARATION:
            annotations = ((XSNotationDeclaration)sourceComponent).getAnnotations();
            break;
        case XSConstants.FACET:
            annotations = ((XSFacet)sourceComponent).getAnnotations();
            break;
        case XSConstants.MULTIVALUE_FACET:
            annotations = ((XSMultiValueFacet)sourceComponent).getAnnotations();
            break;
        default: // TODO: no type alternative, assertion, schema, fundamental facet
            throw new SCDException(
                    "Error in SCD: annotations accessor is not supported for the component type "
                    + sourceComponent.getType());
        }
        if (annotations != null) {
            XSObject annotation;
            for (int i = 0, n = annotations.size(); i < n; ++i) {
                annotation = annotations.item(i);
                if (annotation != null && !targetComponents.contains(annotation)) {
                    targetComponents.add(annotation);
                }
            }
        }
    } // annotations()

    // Described in section 4.5.7; component-scope Accessor
    private void componentScope(XSObject sourceComponent, List targetComponents) {
        switch (sourceComponent.getType()) {
        case XSConstants.ATTRIBUTE_DECLARATION:
            if (((XSAttributeDeclaration)sourceComponent).getScope() != XSConstants.SCOPE_GLOBAL) {
                XSObject type = ((XSAttributeDeclaration)sourceComponent).getEnclosingCTDefinition();
                if (type != null && !targetComponents.contains(type)) {
                    targetComponents.add(type);
                }
            }
            break;
        case XSConstants.ELEMENT_DECLARATION:
            if (((XSElementDeclaration)sourceComponent).getScope() != XSConstants.SCOPE_GLOBAL) {
                XSObject type = ((XSElementDeclaration)sourceComponent).getEnclosingCTDefinition();
                if (type != null && !targetComponents.contains(type)) {
                    targetComponents.add(type);
                }            
            }
            break;
        }
    } // componentScope()

    private void addComponent(XSObject component, QName nameTest,
            List resultComponents) {
        if (component == null || resultComponents.contains(component)) {
            return;
        }
        if (nameTest == SCDParser.ZERO) { // only for type defs without names
            if (component.getType() == XSConstants.TYPE_DEFINITION
                    && component.getName() == null) {
                resultComponents.add(component);
            }
        } else if (nameTest == SCDParser.WILDCARD) {
            resultComponents.add(component);
        } else {
            String localPart = component.getName();
            String uri = component.getNamespace();
            if (uri != null && localPart != null) { // .../schemaElement::p:item
                if (uri.equals(nameTest.uri)
                        && localPart.equals(nameTest.localpart)) {
                    resultComponents.add(component);
                }
            } else if (uri == null && localPart != null) { // .../schemaElement::item
                if (nameTest.uri == null
                        && localPart.equals(nameTest.localpart)) {
                    resultComponents.add(component);
                }
            } else if (uri == null && localPart == null) { // .../model::sequence
                String variety = null;
                short type = component.getType();
                if (type == XSConstants.MODEL_GROUP
                        || type == XSConstants.FACET
                        || type == XSConstants.MULTIVALUE_FACET) {
                    variety = componentVariety(component);
                    if (nameTest.uri == null && nameTest.localpart.equals(variety)) {
                        resultComponents.add(component);
                    }
                }
            }
        }
    }

    public String toString() {
        return "(current components="+currentComponents.toString()+", result="+result.toString()+")";
    }
} // SCDResolver
