/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.validation.models;

import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.v2.SchemaGrammar;
import org.apache.xerces.impl.validation.XSCMValidator;
import org.apache.xerces.impl.v2.XSParticleDecl;
import org.apache.xerces.impl.v2.SchemaSymbols;

/**This class constructs content models for a given grammar.
 * 
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class CMBuilder {


    private int fLeafCount = 0;
    private final QName fQName1 = new QName();
    private final QName fQName2 = new QName();
    private XSParticleDecl fParticleLeft = new XSParticleDecl();
    private XSParticleDecl fParticleRight = new XSParticleDecl();

    /**
     * When the element has a 'CONTENTTYPE_ELEMENT' model, this method is called to
     * create the content model object. It looks for some special case simple
     * models and creates SimpleContentModel objects for those. For the rest
     * it creates the standard DFA style model.
     * 
     * @param grammar
     * @param particle
     * @param particleIndex
     * @return 
     */
    public XSCMValidator createChildModel(SchemaGrammar grammar, 
                                          XSParticleDecl particle, 
                                          int particleIndex) {

        //
        //  Get the content spec node for the element we are working on.
        //  This will tell us what kind of node it is, which tells us what
        //  kind of model we will try to create.
        //

        //XMLContentSpec particle = new XMLContentSpec();
        short particleType = particle.type;
        if ((particleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY ||
            (particleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY_OTHER ||
            (particleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY_LIST) {
            // let fall through to build a DFAContentModel
        }

        else if (particleType == XSParticleDecl.PARTICLE_ELEMENT) {
            //
            //  Check that the left value is not SchemaGrammar.I_EMPTY_DECL, since any content model
            //  with PCDATA should be MIXED, so we should not have gotten here.
            //
            if (particle.value == SchemaGrammar.I_EMPTY_DECL && 
                particle.otherValue == SchemaGrammar.I_EMPTY_DECL)
                throw new RuntimeException("ImplementationMessages.VAL_NPCD");

            //
            //  Its a single leaf, so its an 'a' type of content model, i.e.
            //  just one instance of one element. That one is definitely a
            //  simple content model.
            //
            String elementName = grammar.getElementName(particle.value);
            fQName1.setValues(null, elementName, null, particle.uri );
            return new XSSimpleCM(particleType, fQName1, particle.value, 
                                  null, SchemaGrammar.I_EMPTY_DECL);
        }
        else if ((particleType == XSParticleDecl.PARTICLE_CHOICE)
                 ||  (particleType == XSParticleDecl.PARTICLE_SEQUENCE)) {
            //
            //  Lets see if both of the children are leafs. If so, then it
            //  it has to be a simple content model
            //
            grammar.getParticleDecl(particle.value, fParticleLeft);
            grammar.getParticleDecl(particle.otherValue, fParticleRight);


            if ((fParticleLeft.type == XSParticleDecl.PARTICLE_ELEMENT)
                &&  (fParticleRight.type == XSParticleDecl.PARTICLE_ELEMENT)) {
                //
                //  Its a simple choice or sequence, so we can do a simple
                //  content model for it.
                //
                String elementName = grammar.getElementName(fParticleLeft.value);
                fQName1.setValues(null, elementName, null, fParticleLeft.uri );

                elementName = grammar.getElementName(fParticleRight.value);
                fQName1.setValues(null, elementName, null, fParticleRight.uri );

                return new XSSimpleCM(particleType, fQName1, fParticleLeft.value, 
                                      fQName2, fParticleRight.value);
            }

        }
        else if ((particleType == XSParticleDecl.PARTICLE_ZERO_OR_ONE)
                 ||  (particleType == XSParticleDecl.PARTICLE_ZERO_OR_MORE)
                 ||  (particleType == XSParticleDecl.PARTICLE_ONE_OR_MORE)) {
            //
            //  Its a repetition, so see if its one child is a leaf. If so
            //  its a repetition of a single element, so we can do a simple
            //  content model for that.
            //
        }
        else {
            throw new RuntimeException("ImplementationMessages.VAL_CST");
        }

        //
        //  Its not a simple content model, so here we have to create a DFA
        //  for this element. So we create a DFAContentModel object. He
        //  encapsulates all of the work to create the DFA.
        //

        fLeafCount = 0;

        //CMNode cmn    = buildSyntaxTree(particleIndex, particle);

        // REVISIT: has to be fLeafCount because we convert x+ to x,x*, one more leaf
        //return new DFAContentModel(  cmn, fLeafCount, isDTD(), );

        return null;
    } // createChildModel(int):ContentModelValidator


    public int expandParticleTree(SchemaGrammar grammar, int particleIndex, XSParticleDecl particle) {

        // We may want to consider trying to combine this with buildSyntaxTree at some
        // point (if possible)


        int maxOccurs = particle.maxOccurs;
        int minOccurs = particle.minOccurs;
        short particleType = particle.type;

        // 
        // REVISIT: implement this function!

        return particleIndex;
    }



    private int expandContentModel(SchemaGrammar grammar, int index, XSParticleDecl particle) {

        int leafIndex = index;
        int maxOccurs = particle.maxOccurs;
        int minOccurs = particle.minOccurs;
        
        //REVISIT: add uri/other uri to particle creation methods
        //String uri = particle.uri;
        //String otherUri = particle.otherUri;

        if (minOccurs==1 && maxOccurs==1) {
            return index;
        }
        else if (minOccurs==0 && maxOccurs==1) {
            //zero or one
            index = grammar.addParticleDecl( XSParticleDecl.PARTICLE_ZERO_OR_ONE,
                                             index,
                                             SchemaGrammar.I_EMPTY_DECL);
            //index = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ZERO_OR_ONE, 
            //                                index, uri, SchemaGrammar.I_EMPTY_DECL, SchemaGrammar.I_EMPTY_DECL);
        }
        else if (minOccurs == 0 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            //zero or more
            index = grammar.addParticleDecl( XSParticleDecl.PARTICLE_ZERO_OR_MORE,
                                             index,
                                             SchemaGrammar.I_EMPTY_DECL);
        }
        else if (minOccurs == 1 && maxOccurs==SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            //one or more
            index = grammar.addParticleDecl( XSParticleDecl.PARTICLE_ONE_OR_MORE,
                                             index,
                                             SchemaGrammar.I_EMPTY_DECL);
        }
        else if (maxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            if (minOccurs<2) {
                //REVISIT
            }

            // => a,a,..,a+
            index = grammar.addParticleDecl( XSParticleDecl.PARTICLE_ONE_OR_MORE,
                                             index,
                                             SchemaGrammar.I_EMPTY_DECL);

            for (int i=0; i < (minOccurs-1); i++) {
                index = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE,
                                                leafIndex,
                                                index);
            }

        }
        else {
            // {n,m} => a,a,a,...(a),(a),...


            if (minOccurs==0) {
                int optional = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ZERO_OR_ONE,
                                                       leafIndex,
                                                       SchemaGrammar.I_EMPTY_DECL);
                index = optional;
                for (int i=0; i < (maxOccurs-minOccurs-1); i++) {
                    index = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE,
                                                    index,
                                                    optional);
                }
            }
            else {
                for (int i=0; i<(minOccurs-1); i++) {
                    index = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE,
                                                    index,
                                                    leafIndex);
                }

                int optional = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ZERO_OR_ONE,
                                                       leafIndex,
                                                       SchemaGrammar.I_EMPTY_DECL);
                for (int i=0; i < (maxOccurs-minOccurs); i++) {
                    index = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE,
                                                    index,
                                                    optional);
                }
            }
        }

        return index;
    }



}
