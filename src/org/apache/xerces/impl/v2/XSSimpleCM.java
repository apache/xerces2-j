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

package org.apache.xerces.impl.v2;

import org.apache.xerces.xni.QName;

/**
 * XSSimpleCM is a derivative of the abstract content model base
 * class that handles a small set of simple content models that are just
 * way overkill to give the DFA treatment.
 * <p>
 * This class handles the following scenarios:
 * <ul>
 * <li> a
 * <li> a?
 * <li> a
 * <li> a+
 * <li> a,b
 * <li> a|b
 * </ul>
 * <p>
 * These all involve a unary operation with one element type, or a binary
 * operation with two elements. These are very simple and can be checked
 * in a simple way without a DFA and without the overhead of setting up a
 * DFA for such a simple check.
 * This model validated on the way in.
 *
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class XSSimpleCM
implements XSCMValidator {

    //
    // Constants
    //

    // start the content model: did not see any children
    private static final short STATE_START = 0;
    // seen first child
    private static final short STATE_FIRST = 1;
    private static final short STATE_VALID = 2;

    //
    // Data
    //


    // element declaration in the XML Schema grammar.
    private XSElementDecl fFirstElement = null;
    private XSElementDecl fSecondElement = null;

    /**
     * The operation that this object represents. Since this class only
     * does simple contents, there is only ever a single operation
     * involved (i.e. the children of the operation are always one or
     * two leafs.)
     */
    private short fOperator;


    //
    // Constructors
    //

     /**
     * Constructs a simple content model.
     *
     *
     */
    public XSSimpleCM(short operator, XSElementDecl elem) {
        fFirstElement = elem;
        fOperator = operator;

    }

    /**
     * Constructs a simple content model.
     *
     *
     */
    public XSSimpleCM(short operator, XSElementDecl elem1, XSElementDecl elem2) {
        fFirstElement = elem1;
        fSecondElement = elem2;
        fOperator = operator;

    }



    //
    // XSCMValidator methods
    //

    /**
     * This methods to be called on entering a first element whose type
     * has this content model. It will return the initial state of the content model
     *
     * @return Start state of the content model
     */
    public int[] startContentModel(){
        return (new int[] {STATE_START}); 
    }


    /**
     * The method corresponds to one transaction in the content model.
     *
     * @param elementName
     * @param state  Current state
     * @return element index corresponding to the element from the Schema grammar
     */
    public Object oneTransition (QName elementName, int[] currentState, SubstitutionGroupHandler subGroupHandler){

        // error state
        if (currentState[0] == XSCMValidator.FIRST_ERROR) {
            currentState[0] = XSCMValidator.SUBSEQUENT_ERROR;
            return null;
        }

        int state = currentState[0];

        switch (fOperator) {
        case XSParticleDecl.PARTICLE_ELEMENT :
        case XSParticleDecl.PARTICLE_ZERO_OR_ONE :
            if (state == STATE_START) {
                if (fFirstElement.fTargetNamespace == elementName.uri &&
                    fFirstElement.fName == elementName.localpart ||
                    subGroupHandler.substitutionGroupOK(elementName, fFirstElement)) {
                    currentState[0] = STATE_VALID;
                    return fFirstElement;
                }
                //error
            }
            break;

        case XSParticleDecl.PARTICLE_ZERO_OR_MORE :
        case XSParticleDecl.PARTICLE_ONE_OR_MORE :
            if (fFirstElement.fTargetNamespace == elementName.uri &&
                fFirstElement.fName == elementName.localpart ||
                subGroupHandler.substitutionGroupOK(elementName, fFirstElement)) {
                currentState[0] = STATE_VALID;
                return fFirstElement;
            }
            break;

        case XSParticleDecl.PARTICLE_CHOICE :
            if (state == STATE_START) {
                if (fFirstElement.fTargetNamespace == elementName.uri &&
                    fFirstElement.fName == elementName.localpart ||
                    subGroupHandler.substitutionGroupOK(elementName, fFirstElement)) {
                    currentState[0] = STATE_VALID;
                    return fFirstElement;

                }
                else if (fSecondElement.fTargetNamespace == elementName.uri &&
                         fSecondElement.fName == elementName.localpart ||
                         subGroupHandler.substitutionGroupOK(elementName, fSecondElement)) {
                    currentState[0] = STATE_VALID;
                    return fSecondElement;
                }
                //error
            }

            break;

        case XSParticleDecl.PARTICLE_SEQUENCE :
            //
            //  There must be two children and they must be the two values
            //  we stored, in the stored order.
            //
            if (state == STATE_START) {
                if (fFirstElement.fTargetNamespace == elementName.uri &&
                    fFirstElement.fName == elementName.localpart ||
                    subGroupHandler.substitutionGroupOK(elementName, fFirstElement)) {
                    currentState[0] = STATE_FIRST;
                    return fFirstElement;
                }
                //error
            }
            else if (state == STATE_FIRST) {
                if (fSecondElement.fTargetNamespace == elementName.uri &&
                    fSecondElement.fName == elementName.localpart ||
                    subGroupHandler.substitutionGroupOK(elementName, fSecondElement)) {
                    currentState[0] = STATE_VALID;
                    return fSecondElement;
                }
                //error
            }

            break;
        default :
            throw new RuntimeException("ImplementationMessages.VAL_CST");
        }

        //if we reach here there was an error
        currentState[0] = XSCMValidator.FIRST_ERROR;
        return null;
    }


    /**
     * The method indicates the end of list of children
     *
     * @param state  Current state of the content model
     * @return true if the last state was a valid final state
     */
    public boolean endContentModel (int[] currentState){
        boolean isFinal =  false;
        int state = currentState[0];

        // error
        if (state < 0) {
            return false;
        }

        switch (fOperator) {
        case XSParticleDecl.PARTICLE_ELEMENT :
        case XSParticleDecl.PARTICLE_ONE_OR_MORE :
        case XSParticleDecl.PARTICLE_CHOICE :
        case XSParticleDecl.PARTICLE_SEQUENCE :
            isFinal = (state == STATE_VALID)? true: false;
        break;
        case XSParticleDecl.PARTICLE_ZERO_OR_ONE :
        case XSParticleDecl.PARTICLE_ZERO_OR_MORE :
            isFinal = (state == STATE_VALID || state == STATE_START)? true:false;
        break;
        default :
            throw new RuntimeException("ImplementationMessages.VAL_CST");
        }


        return isFinal;
    }


} // class XSSimpleCM
