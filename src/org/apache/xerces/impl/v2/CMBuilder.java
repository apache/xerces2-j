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

package org.apache.xerces.impl.v2;

import org.apache.xerces.xni.QName;

/**
 * This class constructs content models for a given grammar.
 * 
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class CMBuilder {


    private final QName fQName1 = new QName();
    private final QName fQName2 = new QName();
    private XSParticleDecl fParticle = new XSParticleDecl();
    private XSParticleDecl fTempParticle = new XSParticleDecl();
    private XSParticleDecl fParticleRight = new XSParticleDecl();
    private XSElementDecl fTempElement = new XSElementDecl();

    //
    // REVISIT: need to add grammar resolver if current impl of grammar remains
    //          the same.
    //
    
    

    /**
     * When the element has a 'CONTENTTYPE_ELEMENT' model, this method is called to
     * create the content model object. It looks for some special case simple
     * models and creates SimpleContentModel objects for those. For the rest
     * it creates the standard DFA style model.
     * 
     * @param grammar
     * @param fParticleIndex
     * @return 
     */
    public XSCMValidator createChildModel(SchemaGrammar grammar, 
                                          int fParticleIndex) {

        //
        //  Get the content spec node for the element we are working on.
        //  This will tell us what kind of node it is, which tells us what
        //  kind of model we will try to create.
        //
        fParticle = grammar.getParticleDecl(fParticleIndex, fParticle);
        fTempParticle.clear();
        fParticleRight.clear();
        //XMLContentSpec fParticle = new XMLContentSpec();
        short fParticleType = fParticle.type;
        if ((fParticleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY ||
            (fParticleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY_OTHER ||
            (fParticleType & 0x0f ) == XSParticleDecl.PARTICLE_ANY_LIST) {
            // let fall through to build a DFAContentModel
        }

        else if (fParticleType == XSParticleDecl.PARTICLE_ELEMENT) {
            //
            //  Check that the left value is not SchemaGrammar.I_EMPTY_DECL, since any content model
            //  with PCDATA should be MIXED, so we should not have gotten here.
            //
            if (fParticle.value == SchemaGrammar.I_EMPTY_DECL && 
                fParticle.otherValue == SchemaGrammar.I_EMPTY_DECL)
                throw new RuntimeException("ImplementationMessages.VAL_NPCD");

            //
            //  Its a single leaf, so its an 'a' type of content model, i.e.
            //  just one instance of one element. That one is definitely a
            //  simple content model.
            //
            // fParticle otherUri holds local name of the element.
            fQName1.setValues(null, fParticle.otherUri, null, fParticle.uri );
            return new XSSimpleCM(fParticleType, fQName1, fParticle.value, 
                                  null, SchemaGrammar.I_EMPTY_DECL);
        }
        else if ((fParticleType == XSParticleDecl.PARTICLE_CHOICE)
                 ||  (fParticleType == XSParticleDecl.PARTICLE_SEQUENCE)) {
            //
            //  Lets see if both of the children are leafs. If so, then it
            //  it has to be a simple content model
            //
            
            fTempParticle = grammar.getParticleDecl(fParticle.value, fTempParticle);
            fParticleRight = grammar.getParticleDecl(fParticle.otherValue, fParticleRight);


            if ((fTempParticle.type == XSParticleDecl.PARTICLE_ELEMENT)
                &&  (fParticleRight.type == XSParticleDecl.PARTICLE_ELEMENT)) {
                //
                //  Its a simple choice or sequence, so we can do a simple
                //  content model for it.
                //
                // otherUri - stores local name of the element.
                fQName1.setValues(null, fTempParticle.otherUri, null, fTempParticle.uri );

                fQName1.setValues(null, fParticleRight.otherUri, null, fParticleRight.uri );

                return new XSSimpleCM(fParticleType, fQName1, fTempParticle.value, 
                                      fQName2, fParticleRight.value);
            }

        }
        else if ((fParticleType == XSParticleDecl.PARTICLE_ZERO_OR_ONE)
                 ||  (fParticleType == XSParticleDecl.PARTICLE_ZERO_OR_MORE)
                 ||  (fParticleType == XSParticleDecl.PARTICLE_ONE_OR_MORE)) {
            //
            //  Its a repetition, so see if its one child is a leaf. If so
            //  its a repetition of a single element, so we can do a simple
            //  content model for that.
            fTempParticle = grammar.getParticleDecl(fParticle.value, fTempParticle);

            if (fTempParticle.type == XSParticleDecl.PARTICLE_ELEMENT) {
                //
                //  It is, so we can create a simple content model here that
                //  will check for this repetition. We pass -1 for the unused
                //  right node.
                //
                fQName1.setValues(null, fTempParticle.otherUri, null, fTempParticle.uri );
                return new XSSimpleCM(fParticleType, fQName1, fParticle.value, 
                                      null, SchemaGrammar.I_EMPTY_DECL);
            }
            else if (fTempParticle.type==XSParticleDecl.PARTICLE_ALL) {
                // REVISIT: add ALL content model
            }
        
            
        }
        else {
            throw new RuntimeException("ImplementationMessages.VAL_CST");
        }

        //
        //  Its not a simple content model, so here we have to create a DFA
        //  for this element. So we create a DFAContentModel object. He
        //  encapsulates all of the work to create the DFA.
        //

        //REVISIT: add DFA Content Model
        return null;
    } 


    public int expandParticleTree(SchemaGrammar grammar, int fParticleIndex) {

        // We may want to consider trying to combine this with buildSyntaxTree at some
        // point (if possible)
        if (!grammar.fDeferParticleExpantion) {
            return fParticleIndex;
        }
        fTempParticle = grammar.getParticleDecl(fParticleIndex, fTempParticle);
        int maxOccurs = fTempParticle.maxOccurs;
        int minOccurs = fTempParticle.minOccurs;
        short fParticleType = fTempParticle.type;
        
        if (((fParticleType & 0x0f) == XSParticleDecl.PARTICLE_ANY) ||
            ((fParticleType & 0x0f) == XSParticleDecl.PARTICLE_ANY_OTHER) ||
            ((fParticleType & 0x0f) == XSParticleDecl.PARTICLE_ANY_LIST) ||
            (fParticleType == XSParticleDecl.PARTICLE_ELEMENT)) {

          // When checking Unique Particle Attribution, rename leaf elements
          if (grammar.fUPAChecking) {
            // REVISIT: implement
          }

          return expandContentModel(grammar, fParticleIndex, minOccurs, maxOccurs);
        }
        else if (fParticleType == XSParticleDecl.PARTICLE_CHOICE ||
                 fParticleType == XSParticleDecl.PARTICLE_ALL ||
                 fParticleType == XSParticleDecl.PARTICLE_SEQUENCE) {

          int left = fTempParticle.value;
          int right = fTempParticle.otherValue;
          
          //REVISIT: look at uri and switch grammar if necessary
          left =  expandParticleTree(grammar, left);

          if (right == SchemaGrammar.I_EMPTY_DECL)
             return expandContentModel(grammar, left, minOccurs, maxOccurs);

          right =  expandParticleTree(grammar, right);

          // When checking Unique Particle Attribution, we always create new
          // new node to store different name for different groups
          if (grammar.fUPAChecking) {
              //REVISIT:
              //contentSpecIndex = addContentSpecNode (type, left, right, false);
          } else {
            fTempParticle.value = left;
            fTempParticle.otherValue = right;
            grammar.setParticleDecl(fParticleIndex, fTempParticle);
            
          }
          return expandContentModel(grammar, fParticleIndex, minOccurs, maxOccurs);
        }
        else {
          // When checking Unique Particle Attribution, we have to rename
          // uri even on zero_or_one, zero_or_more and one_or_more
          if (grammar.fUPAChecking) {
              //REVISIT:
              //return addContentSpecNode (fParticleType,
              //                           convertContentSpecTree(fTempParticle.value),
              //                           convertContentSpecTree(fTempParticle.otherValue),
              //                           false);
          } else {
              return fParticleIndex;
          }
        }
        return fParticleIndex;
    }



    private int expandContentModel(SchemaGrammar grammar, int index, 
                                   int minOccurs, int maxOccurs) {

        int leafIndex = index;
        
        //REVISIT: add uri/other uri to fParticle creation methods
        //String uri = fParticle.uri;
        //String otherUri = fParticle.otherUri;

        // REVISIT: should we handle (maxOccurs - minOccurs) = {1,2} as
        //          separate case?

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
