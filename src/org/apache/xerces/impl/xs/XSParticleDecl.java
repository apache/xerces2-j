/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.xs;

/**
 * Store schema particle declaration.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class XSParticleDecl {

    // types of particles
    public static final short PARTICLE_EMPTY        = 0;
    public static final short PARTICLE_ELEMENT      = 1;
    public static final short PARTICLE_WILDCARD     = 2;
    public static final short PARTICLE_CHOICE       = 3;
    public static final short PARTICLE_SEQUENCE     = 4;
    public static final short PARTICLE_ALL          = 5;
    public static final short PARTICLE_ZERO_OR_ONE  = 6;
    public static final short PARTICLE_ZERO_OR_MORE = 7;
    public static final short PARTICLE_ONE_OR_MORE  = 8;

    // type of the particle
    public short fType = PARTICLE_EMPTY;
    // left-hand value of the particle
    // for PARTICLE_ELEMENT : the element decl
    // for PARTICLE_WILDCARD: the wildcard decl
    // for PARTICLE_CHOICE/SEQUENCE/ALL: the particle of the first child
    // for PARTICLE_?*+: the child particle
    public Object fValue = null;
    // for PARTICLE_CHOICE/SEQUENCE/ALL: the particle of the other child
    public Object fOtherValue = null;
    // minimum occurrence of this particle
    public int fMinOccurs = 1;
    // maximum occurrence of this particle
    public int fMaxOccurs = 1;

    // clone this decl
    public XSParticleDecl clone(boolean deep) {
        XSParticleDecl particle = new XSParticleDecl();
        particle.fType = fType;
        particle.fMinOccurs = fMinOccurs;
        particle.fMaxOccurs = fMaxOccurs;
        particle.fDescription = fDescription;
        // if it's not a deep clone, or it's a leaf particle
        // just copy value and other value
        if (!deep ||
            fType == PARTICLE_EMPTY ||
            fType == PARTICLE_ELEMENT ||
            fType == PARTICLE_WILDCARD) {
            particle.fValue = fValue;
            particle.fOtherValue = fOtherValue;
        }
        // otherwise, we have to make clones of value and other value
        else {
            if (fValue != null) {
                particle.fValue = ((XSParticleDecl)fValue).clone(deep);
            }
            else {
                particle.fValue = null;
            }
            if (fOtherValue != null) {
                particle.fOtherValue = ((XSParticleDecl)fOtherValue).clone(deep);
            }
            else {
                particle.fOtherValue = null;
            }
        }
        return particle;
    }
    
    /**
     * 3.9.6 Schema Component Constraint: Particle Emptiable
     * whether this particle is emptible
     */
    public boolean emptiable() {
        return minEffectiveTotalRange() == 0;
    }

    public boolean isEmpty() {
        if (fType==PARTICLE_ELEMENT || fType==PARTICLE_WILDCARD) return false; 

        if (fType==PARTICLE_EMPTY) return true; 
        
        boolean leftIsEmpty  = (fValue==null || ((XSParticleDecl)fValue).isEmpty());
        boolean rightIsEmpty  = (fOtherValue==null || 
                                 ((XSParticleDecl)fOtherValue).isEmpty());

        return (leftIsEmpty && rightIsEmpty) ;
    }

    /**
     * 3.8.6 Effective Total Range (all and sequence) and
     *       Effective Total Range (choice)
     * The following methods are used to return min/max range for a particle.
     * They are not exactly the same as it's described in the spec, but all the
     * values from the spec are retrievable by these methods.
     */
    public int minEffectiveTotalRange() {
        switch (fType) {
        case PARTICLE_ALL:
        case PARTICLE_SEQUENCE:
            return minEffectiveTotalRangeAllSeq();
        case PARTICLE_CHOICE:
            return minEffectiveTotalRangeChoice();
        default:
            return fMinOccurs;
        }
    }

    private int minEffectiveTotalRangeAllSeq() {
        int fromLeft = ((XSParticleDecl)fValue).minEffectiveTotalRange();
        if (fOtherValue != null)
            fromLeft += ((XSParticleDecl)fOtherValue).minEffectiveTotalRange();
        return fMinOccurs * fromLeft;
    }

    private int minEffectiveTotalRangeChoice() {
        int fromLeft = ((XSParticleDecl)fValue).minEffectiveTotalRange();
        if (fOtherValue != null) {
            int fromRight = ((XSParticleDecl)fOtherValue).minEffectiveTotalRange();
            if (fromRight < fromLeft)
                fromLeft = fromRight;
        }
        return fMinOccurs * fromLeft;
    }

    public int maxEffectiveTotalRange() {
        switch (fType) {
        case PARTICLE_ALL:
        case PARTICLE_SEQUENCE:
            return maxEffectiveTotalRangeAllSeq();
        case PARTICLE_CHOICE:
            return maxEffectiveTotalRangeChoice();
        default:
            return fMaxOccurs;
        }
    }

    private int maxEffectiveTotalRangeAllSeq() {
        int fromLeft = ((XSParticleDecl)fValue).maxEffectiveTotalRange();
        if (fromLeft == SchemaSymbols.OCCURRENCE_UNBOUNDED)
            return SchemaSymbols.OCCURRENCE_UNBOUNDED;
        if (fOtherValue != null) {
            int fromRight = ((XSParticleDecl)fValue).maxEffectiveTotalRange();
            if (fromRight == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            fromLeft += fromRight;
        }

        if (fromLeft != 0 && fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
            return SchemaSymbols.OCCURRENCE_UNBOUNDED;

        return fMaxOccurs * fromLeft;
    }

    private int maxEffectiveTotalRangeChoice() {
        int fromLeft = ((XSParticleDecl)fValue).maxEffectiveTotalRange();
        if (fromLeft == SchemaSymbols.OCCURRENCE_UNBOUNDED)
            return SchemaSymbols.OCCURRENCE_UNBOUNDED;
        if (fOtherValue != null) {
            int fromRight = ((XSParticleDecl)fValue).maxEffectiveTotalRange();
            if (fromRight == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                return SchemaSymbols.OCCURRENCE_UNBOUNDED;
            if (fromRight < fromLeft)
                fromLeft = fromRight;
        }

        if (fromLeft != 0 && fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
            return SchemaSymbols.OCCURRENCE_UNBOUNDED;

        return fMaxOccurs * fromLeft;
    }

    /**
     * get the string description of this particle
     */
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            appendParticle(buffer);
            if (!(fMinOccurs == 0 && fMaxOccurs == 0 ||
                  fMinOccurs == 1 && fMaxOccurs == 1)) {
                buffer.append("{" + fMinOccurs);
                if (fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                    buffer.append("-UNBOUNDED");
                else if (fMinOccurs != fMaxOccurs)
                    buffer.append("-" + fMaxOccurs);
                buffer.append("}");
            }
            fDescription = buffer.toString();
        }
        return fDescription;
    }

    /**
     * append the string description of this particle to the string buffer
     * this is for error message.
     */
    void appendParticle(StringBuffer fBuffer) {
        switch (fType) {
        case PARTICLE_EMPTY:
            fBuffer.append("EMPTY");
            break;
        case PARTICLE_ELEMENT:
        case PARTICLE_WILDCARD:
            fBuffer.append('(');
            fBuffer.append(fValue.toString());
            fBuffer.append(')');
            break;
        case PARTICLE_CHOICE:
        case PARTICLE_SEQUENCE:
        case PARTICLE_ALL:
            if (fOtherValue == null) {
                fBuffer.append(fValue.toString());
            } else {
                if (fType == PARTICLE_ALL)
                    fBuffer.append("all(");
                else
                    fBuffer.append('(');
                fBuffer.append(fValue.toString());
                if (fType == PARTICLE_CHOICE)
                    fBuffer.append('|');
                else
                    fBuffer.append(',');
                fBuffer.append(fOtherValue.toString());
                fBuffer.append(')');
            }
            break;
        }
    }
} // class XSParticle
