/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.xs.models;

import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.impl.xs.XSConstraints;

/**
 * XSAllCM implements XSCMValidator and handles <all>
 *
 * @author Pavani Mukthipudi, Sun Microsystems Inc.
 * @version $Id$
 */
public class XSAllCM implements XSCMValidator {

    //
    // Constants
    //

    // start the content model: did not see any children
    private static final short STATE_START = 0;
    private static final short STATE_VALID = 1;


    //
    // Data
    //

    private XSElementDecl fAllElements[] = new XSElementDecl[10];
    private boolean fIsOptionalElement[] = new boolean[10];
    private boolean fHasOptionalContent = false;
    // private boolean fIsMixed = false;

    private int fNumElements = 0;
    private int fNumRequired = 0;

    //
    // Constructors
    //

    public XSAllCM (boolean hasOptionalContent) {
        fHasOptionalContent = hasOptionalContent;
    }

    public void addElement (XSElementDecl element, boolean isOptional) {

        // resize arrays if necessary
        if (fNumElements >= fAllElements.length) {
            XSElementDecl newAllElements[] = new XSElementDecl [2*fAllElements.length];
            boolean newIsOptionalElements[] =
                                      new boolean[2*fIsOptionalElement.length];
            System.arraycopy(fAllElements, 0, newAllElements, 0,
                             fAllElements.length);
            System.arraycopy(fIsOptionalElement, 0, newIsOptionalElements, 0,
                             fIsOptionalElement.length);

            fAllElements = newAllElements;
            fIsOptionalElement = newIsOptionalElements;
        }

        fAllElements[fNumElements] = element;
        fIsOptionalElement[fNumElements] = isOptional;

        fNumElements++;

        // keeping track of the number of elements that are required
        if (!isOptional) {
            fNumRequired++;
        }

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

        int[] state = new int[fNumElements + 1];

        for (int i = 0; i <= fNumElements; i++) {
            state[i] = STATE_START;
        }
        return state;
    }

    // convinient method: when error occurs, to find a matching decl
    // from the candidate elements.
    Object findMatchingDecl(QName elementName, SubstitutionGroupHandler subGroupHandler) {
        Object matchingDecl = null;
        for (int i = 0; i < fNumElements; i++) {
            matchingDecl = subGroupHandler.getMatchingElemDecl(elementName, fAllElements[i]);
            if (matchingDecl != null)
                break;
        }
        return matchingDecl;
    }

    /**
     * The method corresponds to one transition in the content model.
     *
     * @param elementName
     * @param state  Current state
     * @return an element decl object
     */
    public Object oneTransition (QName elementName, int[] currentState, SubstitutionGroupHandler subGroupHandler) {

        // error state
        if (currentState[0] < 0) {
            currentState[0] = XSCMValidator.SUBSEQUENT_ERROR;
            return findMatchingDecl(elementName, subGroupHandler);
        }

        Object matchingDecl = null;

        for (int i = 0; i < fNumElements; i++) {
            // we only try to look for a matching decl if we have not seen
            // this element yet.
            if (currentState[i+1] != STATE_START)
                continue;
            matchingDecl = subGroupHandler.getMatchingElemDecl(elementName, fAllElements[i]);
            if (matchingDecl != null) {
                // found the decl, mark this element as "seen".
                currentState[i+1] = STATE_VALID;
                return matchingDecl;
            }
        }

        // couldn't find the decl, change to error state.
        currentState[0] = XSCMValidator.FIRST_ERROR;
        return findMatchingDecl(elementName, subGroupHandler);
    }


    /**
     * The method indicates the end of list of children
     *
     * @param state  Current state of the content model
     * @return true if the last state was a valid final state
     */
    public boolean endContentModel (int[] currentState) {

        int state = currentState[0];

        if (state == XSCMValidator.FIRST_ERROR || state == XSCMValidator.SUBSEQUENT_ERROR) {
            return false;
        }

        // If <all> has minOccurs of zero and there are
        // no children to validate, it is trivially valid

        if (fHasOptionalContent && fNumElements == 0) {
            return true;
        }

        int numRequiredSeen = 0;

        for (int i = 0; i < fNumElements; i++) {
            if (!fIsOptionalElement[i] && currentState[i+1] != STATE_START)
                numRequiredSeen++;
        }

        if (fNumRequired == numRequiredSeen ) {
            return true;
        }

        return false;
    }

    /**
     * check whether this content violates UPA constraint.
     *
     * @param errors to hold the UPA errors
     * @return true if this content model contains other or list wildcard
     */
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler) throws XMLSchemaException {
        // check whether there is conflict between any two leaves
        for (int i = 0; i < fNumElements; i++) {
            for (int j = i+1; j < fNumElements; j++) {
                if (XSConstraints.overlapUPA(fAllElements[i], fAllElements[j], subGroupHandler)) {
                    // REVISIT: do we want to report all errors? or just one?
                    throw new XMLSchemaException("cos-nonambig", new Object[]{fAllElements[i].toString(),
                                                                              fAllElements[j].toString()});
                }
            }
        }

        return false;
    }

} // class XSAllCM

