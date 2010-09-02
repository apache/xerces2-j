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

import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSElementDeclHelper;
import org.apache.xerces.impl.xs.XSOpenContentDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.xni.QName;

/**
 * XSAll11CM implements XSCMValidator and handles &lt;all&gt;.
 *
 * @xerces.internal
 *
 * @author Khaled Noaman, IBM
 * @version $Id$
 */
public class XS11AllCM implements XSCMValidator {

    //
    // Constants
    //

    // start the content model: did not see any children
    private static final short STATE_START = 0;
    private static final short STATE_CHILD = 1;
    private static final short STATE_SUFFIX = 2;

    //
    // Data
    //
    private final boolean fHasOptionalContent;

    private final XSElementDecl fElements[];
    private final XSWildcardDecl fWildcards[];
    private final int fMinOccurs[], fMaxOccurs[];
    private final int fNumElements, fNumTotal;

    private XSOpenContentDecl fOpenContent = null;

    //
    // Constructors
    //

    public XS11AllCM (boolean hasOptionalContent, int size, XSParticleDecl[] particles,
            XSOpenContentDecl openContent) {
        fHasOptionalContent = hasOptionalContent;

        // Index 0 is not used.
        int numE = 1;
        for (int i = 0; i < size; i++) {
            if (particles[i].fType == XSParticleDecl.PARTICLE_ELEMENT) {
                numE++;
            }
        }
        fNumElements = numE;
        fNumTotal = size+1;
        
        if (numE > 1) {
            fElements = new XSElementDecl[numE];
        }
        else {
            fElements = null;
        }
        if (fNumTotal > numE) {
            fWildcards = new XSWildcardDecl[fNumTotal];
        }
        else {
            fWildcards = null;
        }
        if (fNumTotal > 1) {
            fMinOccurs = new int[fNumTotal];
            fMaxOccurs = new int[fNumTotal];
        }
        else {
            fMinOccurs = null;
            fMaxOccurs = null;
        }

        int numW = numE;
        numE = 1;
        for (int i = 0; i < size; i++) {
            XSParticleDecl particle = particles[i];
            if (particle.fType == XSParticleDecl.PARTICLE_ELEMENT) {
                fElements[numE] = (XSElementDecl)particle.fValue;
                fMinOccurs[numE] = particle.fMinOccurs;
                fMaxOccurs[numE] = particle.fMaxOccurs;
                numE++;
            }
            else {
                fWildcards[numW] = (XSWildcardDecl)particle.fValue;
                fMinOccurs[numW] = particle.fMinOccurs;
                fMaxOccurs[numW] = particle.fMaxOccurs;
                numW++;
            }
        }

        fOpenContent = openContent;
    }

    //
    // XSCMValidator methods
    //

    /**
     * This methods to be called on entering a first element whose type
     * has this content model. It will return the initial state of the
     * content model
     *
     * @return Start state of the content model
     */
    public int[] startContentModel() {
        int[] state = new int[fNumTotal];
        for (int i = 0; i < fNumTotal; i++) {
            state[i] = STATE_START;
        }
        return state;
    }

    // convinient method: when error occurs, to find a matching decl
    // from the candidate elements.
    Object findMatchingDecl(QName elementName, SubstitutionGroupHandler subGroupHandler) {
        Object matchingDecl = findMatchingElemDecl(elementName, subGroupHandler);

        if (matchingDecl != null) {
            return matchingDecl;
        }

        for (int i = fNumElements; i < fNumTotal; i++) {
            if (fWildcards[i].allowQName(elementName)) {
                return fWildcards[i];
            }
        }

        return null;
    }

    // convinient method: to find a matching element decl 
    XSElementDecl findMatchingElemDecl(QName elementName, SubstitutionGroupHandler subGroupHandler) {
        for (int i = 1; i < fNumElements; i++) {
            final XSElementDecl matchingDecl = subGroupHandler.getMatchingElemDecl(elementName, fElements[i], Constants.SCHEMA_VERSION_1_1);
            if (matchingDecl != null) {
                return matchingDecl;
            }
        }

        return null;
    }

    private boolean allowExpandedName(XSWildcardDecl wildcard,
                                      QName curElem,
                                      SubstitutionGroupHandler subGroupHandler,
                                      XSElementDeclHelper eDeclHelper) {
        if (wildcard.allowQName(curElem)) {
            if (wildcard.fDisallowedSibling && findMatchingElemDecl(curElem, subGroupHandler) != null) {
                return false;
            }
            if (wildcard.fDisallowedDefined && eDeclHelper.getGlobalElementDecl(curElem) != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * The method corresponds to one transition in the content model.
     *
     * @param elementName
     * @param currentState  Current state
     * @return an element decl object
     */
    public Object oneTransition (QName elementName, int[] currentState,
            SubstitutionGroupHandler subGroupHandler, XSElementDeclHelper eDeclHelper) {
        // error state
        if (currentState[0] < 0) {
            currentState[0] = XSCMValidator.SUBSEQUENT_ERROR;
            return findMatchingDecl(elementName, subGroupHandler);
        }

        // open content - suffix mode
        if (currentState[0] == STATE_SUFFIX) {
            if (allowExpandedName(fOpenContent.fWildcard, elementName, subGroupHandler, eDeclHelper)) {
                return fOpenContent;
            }
            else { // error
                currentState[0] = XSCMValidator.FIRST_ERROR;
                return findMatchingDecl(elementName, subGroupHandler);
            }
        }

        // seen child
        currentState[0] = STATE_CHILD;

        for (int i = 1; i < fNumElements; i++) {
            if (currentState[i] == fMaxOccurs[i]) {
                continue;
            }
            Object matchingDecl = subGroupHandler.getMatchingElemDecl(elementName, fElements[i], Constants.SCHEMA_VERSION_1_1);
            if (matchingDecl != null) {
                // found the decl, mark this element as "seen".
                ++currentState[i];
                return matchingDecl;
            }
        }

        for (int i = fNumElements; i < fNumTotal; i++) {
            if (currentState[i] == fMaxOccurs[i]) {
                continue;
            }
            if (allowExpandedName(fWildcards[i], elementName, subGroupHandler, eDeclHelper)) {
                // found the decl, mark this element as "seen".
                ++currentState[i];
                return fWildcards[i];
            }
        }

        // apply open content, if present
        if (fOpenContent != null) {
            if (fOpenContent.fMode == XSOpenContentDecl.MODE_SUFFIX) {
                if (isFinal(currentState)) {
                    currentState[0] = STATE_SUFFIX;
                }
                else {
                    currentState[0] = XSCMValidator.FIRST_ERROR;
                    return findMatchingDecl(elementName, subGroupHandler);
                }
            }
            if (allowExpandedName(fOpenContent.fWildcard, elementName, subGroupHandler, eDeclHelper)) {
            //if (fOpenContent.fWildcard.allowQName(elementName)) {
                return fOpenContent;
            }
        }

        // couldn't find the decl, change to error state.
        currentState[0] = XSCMValidator.FIRST_ERROR;
        return findMatchingDecl(elementName, subGroupHandler);
    }

    /**
     * The method indicates the end of list of children
     *
     * @param currentState  Current state of the content model
     * @return true if the last state was a valid final state
     */
    public boolean endContentModel (int[] currentState) {
        int state = currentState[0];

        if (state == XSCMValidator.FIRST_ERROR || state == XSCMValidator.SUBSEQUENT_ERROR) {
            return false;
        }

        // If <all> has minOccurs of zero and there are
        // no children to validate, it is trivially valid
        //
        // XML Schema 1.1
        // reached final state and doing suffix validation against open content
        if ((fHasOptionalContent && state == STATE_START) || state == STATE_SUFFIX) {
            return true;
        }

        for (int i = 1; i < fNumTotal; i++) {
            // if one element is required, but not present, then error
            if (currentState[i] < fMinOccurs[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * check whether this content violates UPA constraint.
     *
     * @param subGroupHandler the substitution group handler
     * @param xsConstraints the XML Schema Constraint checker
     * @return true if this content model contains other or list wildcard
     */
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler, XSConstraints xsConstraints) throws XMLSchemaException {
        // check whether there is conflict between any two leaves
        for (int i = 1; i < fNumElements; i++) {
            for (int j = i+1; j < fNumElements; j++) {
                if (xsConstraints.overlapUPA(fElements[i], fElements[j], subGroupHandler)) {
                    // REVISIT: do we want to report all errors? or just one?
                    throw new XMLSchemaException("cos-nonambig", new Object[]{fElements[i].toString(),
                                                                              fElements[j].toString()});
                }
            }
        }
        for (int i = fNumElements; i < fNumTotal; i++) {
            for (int j = i+1; j < fNumTotal; j++) {
                if (xsConstraints.overlapUPA(fWildcards[i], fWildcards[j])) {
                    // REVISIT: do we want to report all errors? or just one?
                    throw new XMLSchemaException("cos-nonambig", new Object[]{fWildcards[i].toString(),
                                                                              fWildcards[j].toString()});
                }
            }
        }
        return false;
    }

    /**
     * Check whether this content model is a valid restriction of the other one.
     *
     * @param base  the base content model
     * @return      true if this content model is a valid restriction.
     */
    public boolean isValidRestriction(XSCMValidator base,
            SubstitutionGroupHandler subGroupHandler,
            XSElementDeclHelper eDeclHelper) {
        return true;
    }

    /**
     * Check which elements are valid to appear at this point. This method also
     * works if the state is in error, in which case it returns what should
     * have been seen.
     *
     * @param state  the current state
     * @return       a Vector whose entries are instances of
     *               either XSWildcardDecl or XSElementDecl.
     */
    public Vector whatCanGoHere(int[] state) {
        Vector ret = new Vector();

        // handle element declarations
        for (int i = 1; i < fNumElements; i++) {
            // we only try to look for a matching decl if we have not seen
            // this element yet or we have seen it less times than its maxOccurs.
            if (state[i] == STATE_START || state[i] < fMaxOccurs[i]) {
                ret.addElement(fElements[i]);
            }
        }

        // only add wildcards if no element can be matched
        if (ret.size() == 0) {
            for (int i = fNumElements; i < fNumTotal; i++) {
                if (state[i] == STATE_START || state[i] < fMaxOccurs[i]) {
                    ret.addElement(fWildcards[i]);
                }
            }
        }

        // if 'ret' is empty and we have an open content, add the open content to 'ret'
        if (ret.size() == 0 && fOpenContent != null) {
            ret.add(fOpenContent);
        }
        return ret;
    }
    
    public int [] occurenceInfo(int[] state) {
        // REVISIT: maxOccurs > 1 is allowed by <xs:all> in XML Schema 1.1
        return null;
    }
    
    public String getTermName(int termId) {
        // REVISIT: maxOccurs > 1 is allowed by <xs:all> in XML Schema 1.1
        return null;
    }

    public boolean isCompactedForUPA() {
        return false;
    }

    private boolean isFinal(int[] currentState) {
        for (int i = 1; i < fNumTotal; i++) {
            // if one element is required, but not present, then error
            if (currentState[i] < fMinOccurs[i]) {
                return false;
            }
        }

        return true;
    }

} // class XSAll11CM
