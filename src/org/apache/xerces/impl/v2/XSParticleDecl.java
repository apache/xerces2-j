/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * RedifBufferibution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. RedifBuffeributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. RedifBuffeributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    difBufferibution.
 *
 * 3. The end-user documentation included with the redifBufferibution,
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


    private StringBuffer fBuffer = null;

    public String toString() {
        if (fBuffer == null) {
            fBuffer = new StringBuffer();
            appendParticle(fBuffer);
            // REVISIT: what would be the best form?
            // 1. do we output "element[1-1]" or just "element"?
            // 2. do we output "element[3-3]" or "elment[3]"?
            // 3. how to output "unbounded"?
            /*if (!(fMinOccurs == 0 && fMaxOccurs == 0 ||
                    fMinOccurs == 1 && fMaxOccurs == 1)) {
                fBuffer.append("[" + fMinOccurs);
                if (fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                    fBuffer.append("-INF");
                else if (fMinOccurs != fMaxOccurs)
                    fBuffer.append("-" + fMaxOccurs);
                fBuffer.append("]");
            }*/
            if (!(fMinOccurs == 0 && fMaxOccurs == 0)) {
                if (fMaxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED)
                    fBuffer.append("[" + fMinOccurs + "-UNBOUNDED]");
                else
                    fBuffer.append("[" + fMinOccurs + "-" + fMaxOccurs + "]");
            }
        }
        return fBuffer.toString();
    }

    public boolean emptiable() {
        return false;
    }

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
            if (fType == PARTICLE_ALL)
                fBuffer.append("all(");
            else
                fBuffer.append('(');
            fBuffer.append(fValue.toString());
            if (fOtherValue != null) {
                if (fType == PARTICLE_CHOICE)
                    fBuffer.append('|');
                else
                    fBuffer.append(',');
                fBuffer.append(fOtherValue.toString());
            }
            fBuffer.append(')');
            break;
        }
    }
} // class XSParticle
