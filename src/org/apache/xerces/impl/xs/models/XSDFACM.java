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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.impl.dtd.models.CMStateSet;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.SubstitutionGroupHandler;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSElementDeclHelper;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSOpenContentDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.xni.QName;

/**
 * DFAContentModel is the implementation of XSCMValidator that does
 * all of the non-trivial element content validation. This class does
 * the conversion from the regular expression to the DFA that
 * it then uses in its validation algorithm.
 *
 * @xerces.internal 
 *
 * @author Neil Graham, IBM
 * @version $Id$
 */
public class XSDFACM
    implements XSCMValidator, XS11CMRestriction.XS11CM {

    //
    // Constants
    //
    private static final boolean DEBUG = false;

    // special strings

    // debugging

    /** Set to true to debug content model validation. */
    private static final boolean DEBUG_VALIDATE_CONTENT = false;

    //
    // Data
    //

    /**
     * This is the map of unique input symbol elements to indices into
     * each state's per-input symbol transition table entry. This is part
     * of the built DFA information that must be kept around to do the
     * actual validation.  Note tat since either XSElementDecl or XSParticleDecl object
     * can live here, we've got to use an Object.
     */
    private XSElementDecl fElements[];
    private XSWildcardDecl fWildcards[];
    private int fNumElements, fNumTotal;

    /**
     * This is an array of booleans, one per state (there are
     * fTransTableSize states in the DFA) that indicates whether that
     * state is a final state.
     */
    private boolean fFinalStateFlags[] = null;

    /**
     * The list of follow positions for each NFA position (i.e. for each
     * non-epsilon leaf node.) This is only used during the building of
     * the DFA, and is let go afterwards.
     */
    private CMStateSet fFollowList[] = null;

    /**
     * This is the head node of our intermediate representation. It is
     * only non-null during the building of the DFA (just so that it
     * does not have to be passed all around.) Once the DFA is built,
     * this is no longer required so its nulled out.
     */
    private CMNode fHeadNode = null;

    /**
     * The count of leaf nodes. This is an important number that set some
     * limits on the sizes of data structures in the DFA process.
     */
    private int fLeafCount = 0;

    /**
     * An array of non-epsilon leaf nodes, which is used during the DFA
     * build operation, then dropped.
     */
    private XSCMLeaf fLeafList[] = null;

    /** Array mapping ANY types to the leaf list. */
    private int fLeafListType[] = null;

    /**
     * This is the transition table that is the main by product of all
     * of the effort here. It is an array of arrays of ints. The first
     * dimension is the number of states we end up with in the DFA. The
     * second dimensions is the number of unique elements in the content
     * model (fElemMapSize). Each entry in the second dimension indicates
     * the new state given that input for the first dimension's start
     * state.
     * <p>
     * The fElemMap array handles mapping from element indexes to
     * positions in the second dimension of the transition table.
     */
    private int fTransTable[][] = null;
    
    /**
     * The open content model
     */
    private final XSOpenContentDecl fOpenContent;
    
    /**
     * The XML Schema version
     */
    private final short fSchemaVersion;

    /**
     * Array containing occurrence information for looping states 
     * which use counters to check minOccurs/maxOccurs.
     */
    private Occurence [] fCountingStates = null;
    static final class Occurence {
        final int minOccurs;
        final int maxOccurs;
        int elemIndex;
        public Occurence (XSCMRepeatingLeaf leaf, int elemIndex) {
            minOccurs = leaf.getMinOccurs();
            maxOccurs = leaf.getMaxOccurs();
            this.elemIndex = elemIndex;
        }
        public String toString() {
            return "minOccurs=" + minOccurs 
                + ";maxOccurs=" + 
                ((maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) 
                        ? Integer.toString(maxOccurs) : "unbounded");
        }
    }

    /**
     * The number of valid entries in the transition table, and in the other
     * related tables such as fFinalStateFlags.
     */
    private int fTransTableSize = 0;

    private boolean fIsCompactedForUPA;
    
    // temp variables

    //
    // Constructors
    //

    /**
     * Constructs a DFA content model.
     *
     * @param syntaxTree    The syntax tree of the content model.
     * @param leafCount     The number of leaves.
     *
     * @exception RuntimeException Thrown if DFA can't be built.
     */

   public XSDFACM(CMNode syntaxTree, int leafCount, short schemaVersion, XSOpenContentDecl openContent) {
   
        // Store away our index and pools in members
        fLeafCount = leafCount;
        fIsCompactedForUPA = syntaxTree.isCompactedForUPA();

        // Store the XML Schema version
        fSchemaVersion = schemaVersion;

        // Store open content
        fOpenContent = openContent;

        //
        //  Create some string pool indexes that represent the names of some
        //  magical nodes in the syntax tree.
        //  (already done in static initialization...
        //

        //
        //  Ok, so lets grind through the building of the DFA. This method
        //  handles the high level logic of the algorithm, but it uses a
        //  number of helper classes to do its thing.
        //
        //  In order to avoid having hundreds of references to the error and
        //  string handlers around, this guy and all of his helper classes
        //  just throw a simple exception and we then pass it along.
        //

        if(DEBUG_VALIDATE_CONTENT) {
            XSDFACM.time -= System.currentTimeMillis();
        }

        buildDFA(syntaxTree);

        if(DEBUG_VALIDATE_CONTENT) {
            XSDFACM.time += System.currentTimeMillis();
            System.out.println("DFA build: " + XSDFACM.time + "ms");
        }
    }

    private static long time = 0;

    //
    // XSCMValidator methods
    //

    /**
     * check whether the given state is one of the final states
     *
     * @param state       the state to check
     *
     * @return whether it's a final state
     */
    public boolean isFinalState (int state) {
        return (state < 0)? false :
            fFinalStateFlags[state];
    }

    /**
     * one transition only
     *
     * @param curElem The current element's QName
     * @param state stack to store the previous state
     * @param subGroupHandler the substitution group handler
     *
     * @return  null if transition is invalid; otherwise the Object corresponding to the
     *      XSElementDecl or XSWildcardDecl identified.  Also, the
     *      state array will be modified to include the new state; this so that the validator can
     *      store it away.
     *
     * @exception RuntimeException thrown on error
     */
    public Object oneTransition(QName curElem, int[] state, SubstitutionGroupHandler subGroupHandler, XSElementDeclHelper eDeclHelper) {
        Object ret = oneTransition1(curElem, state, subGroupHandler, eDeclHelper);
        if (fOpenContent != null && ret == fOpenContent.fWildcard) {
            ret = fOpenContent;
        }
        return ret;            
    }
    private Object oneTransition1(QName curElem, int[] state, SubstitutionGroupHandler subGroupHandler, XSElementDeclHelper eDeclHelper) {
        int curState = state[0];

        if(curState == XSCMValidator.FIRST_ERROR || curState == XSCMValidator.SUBSEQUENT_ERROR) {
            // there was an error last time; so just go find correct Object in fElemmMap.
            // ... after resetting state[0].
            if (curState == XSCMValidator.FIRST_ERROR) {
                state[0] = XSCMValidator.SUBSEQUENT_ERROR;
            }

            return findMatchingDecl(curElem, subGroupHandler);
        }

        int nextState = 0;
        int elemIndex = 0;
        Object matchingDecl = null;

        for (; elemIndex < fNumElements; elemIndex++) {
            nextState = fTransTable[curState][elemIndex];
            if (nextState == -1)
                continue;
            matchingDecl = subGroupHandler.getMatchingElemDecl(curElem, fElements[elemIndex], fSchemaVersion);
            if (matchingDecl != null) {
                break;
            }
        }
        if (matchingDecl == null) {
            for (; elemIndex < fNumTotal; elemIndex++) {
                nextState = fTransTable[curState][elemIndex];
                if (nextState == -1)
                    continue;

                // XML Schema 1.0
                if (fSchemaVersion < Constants.SCHEMA_VERSION_1_1) {
                    if (fWildcards[elemIndex].allowNamespace(curElem.uri)) {
                        matchingDecl = fWildcards[elemIndex];
                        break;
                    }
                }
                // XML Schema 1.1
                else if (allowExpandedName(fWildcards[elemIndex], curElem, subGroupHandler, eDeclHelper)) {
                    matchingDecl = fWildcards[elemIndex];
                    break;
                }
            }
        }

        // if we still can't find a match, set the state to first_error
        // and return null
        if (matchingDecl == null) {
            state[1] = state[0];
            state[0] = XSCMValidator.FIRST_ERROR;
            return findMatchingDecl(curElem, subGroupHandler);
        }

        state[0] = nextState;
        if (fCountingStates == null) {
            return matchingDecl;
        }
        // Interleave open content can appear anywhere, even while counting
        if (fOpenContent != null && fOpenContent.fWildcard == matchingDecl &&
                fOpenContent.fMode == XSOpenContentDecl.MODE_INTERLEAVE) {
            return matchingDecl;
        }
        
        Occurence o = fCountingStates[curState];
        if (o != null) {
            if (curState == nextState) {
                if (++state[2] > o.maxOccurs && 
                        o.maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                    // It's likely that we looped too many times on the current state
                    // however it's possible that we actually matched another particle
                    // which allows the same name.
                    //
                    // Consider:
                    //
                    // <xs:sequence>
                    //  <xs:element name="foo" type="xs:string" minOccurs="3" maxOccurs="3"/>
                    //  <xs:element name="foo" type="xs:string" fixed="bar"/>
                    // </xs:sequence>
                    //
                    // and
                    //
                    // <xs:sequence>
                    //  <xs:element name="foo" type="xs:string" minOccurs="3" maxOccurs="3"/>
                    //  <xs:any namespace="##any" processContents="skip"/>
                    // </xs:sequence>
                    //
                    // In the DFA there will be two transitions from the current state which 
                    // allow "foo". Note that this is not a UPA violation. The ambiguity of which
                    // transition to take is resolved by the current value of the counter. Since 
                    // we've already seen enough instances of the first "foo" perhaps there is
                    // another element declaration or wildcard deeper in the element map which
                    // matches.
                    return findMatchingDecl(curElem, state, subGroupHandler, ++elemIndex, eDeclHelper);
                }  
            }
            else if (state[2] < o.minOccurs) {
                // not enough loops on the current state.
                state[1] = state[0];
                state[0] = XSCMValidator.FIRST_ERROR;
                return findMatchingDecl(curElem, subGroupHandler);
            }
            else {
                // Exiting a counting state. If we're entering a new
                // counting state, reset the counter.
                o = fCountingStates[nextState];
                if (o != null) {
                    state[2] = (elemIndex == o.elemIndex) ? 1 : 0;
                }
            }
        }
        else {
            o = fCountingStates[nextState];
            if (o != null) {
                // Entering a new counting state. Reset the counter.
                // If we've already seen one instance of the looping
                // particle set the counter to 1, otherwise set it 
                // to 0.
                state[2] = (elemIndex == o.elemIndex) ? 1 : 0;
            }
        }

        return matchingDecl;
    } // oneTransition(QName, int[], SubstitutionGroupHandler):  Object

    Object findMatchingDecl(QName curElem, SubstitutionGroupHandler subGroupHandler) {
        Object matchingDecl = null;

        for (int elemIndex = 0; elemIndex < fNumElements; elemIndex++) {
            matchingDecl = subGroupHandler.getMatchingElemDecl(curElem, fElements[elemIndex], fSchemaVersion);
            if (matchingDecl != null) {
                return matchingDecl;
            }
        }
        for (int elemIndex = fNumElements; elemIndex < fNumTotal; elemIndex++) {
            if (fWildcards[elemIndex].allowQName(curElem)) {
                return fWildcards[elemIndex];
            }
        }

        return null;
    } // findMatchingDecl(QName, SubstitutionGroupHandler): Object
    
    Object findMatchingDecl(QName curElem, int[] state, SubstitutionGroupHandler subGroupHandler, int elemIndex, XSElementDeclHelper eDeclHelper) {    
        
        int curState = state[0];
        int nextState = 0;
        Object matchingDecl = null;
        
        for (; elemIndex < fNumElements; elemIndex++) {
            nextState = fTransTable[curState][elemIndex];
            if (nextState == -1)
                continue;
            matchingDecl = subGroupHandler.getMatchingElemDecl(curElem, fElements[elemIndex], fSchemaVersion);
            if (matchingDecl != null) {
                break;
            }
        }
        if (matchingDecl == null) {
            for (; elemIndex < fNumTotal; elemIndex++) {
                // XML Schema 1.0
                if (fSchemaVersion < Constants.SCHEMA_VERSION_1_1) {
                    if (fWildcards[elemIndex].allowNamespace(curElem.uri)) {
                        matchingDecl = fWildcards[elemIndex];
                        break;
                    }
                }
                // XML Schema 1.1
                else if (allowExpandedName(fWildcards[elemIndex], curElem, subGroupHandler, eDeclHelper)) {
                    matchingDecl = fWildcards[elemIndex];
                    break;
                }
            }
        }
        
        // if we still can't find a match, set the state to first_error
        // and return null
        if (matchingDecl == null) {
            state[1] = state[0];
            state[0] = XSCMValidator.FIRST_ERROR;
            return findMatchingDecl(curElem, subGroupHandler);
        }

        // if we found a match, set the next state and reset the 
        // counter if the next state is a counting state.
        state[0] = nextState;
        final Occurence o = fCountingStates[nextState];
        if (o != null) {
            state[2] = (elemIndex == o.elemIndex) ? 1 : 0;
        } 
        return matchingDecl;
    } // findMatchingDecl(QName, int[], SubstitutionGroupHandler, int): Object

    public XSElementDecl findMatchingElemDecl(QName curElem, SubstitutionGroupHandler subGroupHandler) {
        XSElementDecl matchingDecl = null;

        for (int elemIndex = 0; elemIndex < fNumElements; elemIndex++) {
            matchingDecl = subGroupHandler.getMatchingElemDecl(curElem, fElements[elemIndex], fSchemaVersion);
            if (matchingDecl != null) {
                return matchingDecl;
            }
        }

        return null;
    } // findMatchingDecl(QName, SubstitutionGroupHandler): Object
    
    public boolean allowExpandedName(XSWildcardDecl wildcard, QName curElem, SubstitutionGroupHandler subGroupHandler, XSElementDeclHelper eDeclHelper) {
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
    
    // This method returns the start states of the content model.
    public int[] startContentModel() {
        // [0] : the current state
        // [1] : if [0] is an error state then the 
        //       last valid state before the error
        // [2] : occurence counter for counting states
        return new int [3];
    } // startContentModel():int[]

    // this method returns whether the last state was a valid final state
    public boolean endContentModel(int[] state) {
        final int curState = state[0];
        if (fFinalStateFlags[curState]) {
            if (fCountingStates != null) {
                Occurence o = fCountingStates[curState];
                if (o != null && state[2] < o.minOccurs) {
                    // not enough loops on the current state to be considered final.
                    return false;
                }
            }
            return true;
        }
        return false;
    } // endContentModel(int[]):  boolean

    // Killed off whatCanGoHere; we may need it for DOM canInsert(...) etc.,
    // but we can put it back later.

    //
    // Private methods
    //

    /**
     * Builds the internal DFA transition table from the given syntax tree.
     *
     * @param syntaxTree The syntax tree.
     *
     * @exception RuntimeException Thrown if DFA cannot be built.
     */
    private void buildDFA(CMNode syntaxTree) {
        //
        //  The first step we need to take is to rewrite the content model
        //  using our CMNode objects, and in the process get rid of any
        //  repetition short cuts, converting them into '*' style repetitions
        //  or getting rid of repetitions altogether.
        //
        //  The conversions done are:
        //
        //  x+ -> (x|x*)
        //  x? -> (x|epsilon)
        //
        //  This is a relatively complex scenario. What is happening is that
        //  we create a top level binary node of which the special EOC value
        //  is set as the right side node. The the left side is set to the
        //  rewritten syntax tree. The source is the original content model
        //  info from the decl pool. The rewrite is done by buildSyntaxTree()
        //  which recurses the decl pool's content of the element and builds
        //  a new tree in the process.
        //
        //  Note that, during this operation, we set each non-epsilon leaf
        //  node's DFA state position and count the number of such leafs, which
        //  is left in the fLeafCount member.
        //
        //  The nodeTmp object is passed in just as a temp node to use during
        //  the recursion. Otherwise, we'd have to create a new node on every
        //  level of recursion, which would be piggy in Java (as is everything
        //  for that matter.)
        //

        /* MODIFIED (Jan, 2001)
         *
         * Use following rules.
         *   nullable(x+) := nullable(x), first(x+) := first(x),  last(x+) := last(x)
         *   nullable(x?) := true, first(x?) := first(x),  last(x?) := last(x)
         *
         * The same computation of follow as x* is applied to x+
         *
         * The modification drastically reduces computation time of
         * "(a, (b, a+, (c, (b, a+)+, a+, (d,  (c, (b, a+)+, a+)+, (b, a+)+, a+)+)+)+)+"
         */

        //
        //  And handle specially the EOC node, which also must be numbered
        //  and counted as a non-epsilon leaf node. It could not be handled
        //  in the above tree build because it was created before all that
        //  started. We save the EOC position since its used during the DFA
        //  building loop.
        //
        int EOCPos = fLeafCount;
        XSCMLeaf nodeEOC = new XSCMLeaf(XSParticleDecl.PARTICLE_ELEMENT, null, -1, fLeafCount++);
        fHeadNode = new XSCMBinOp(
            XSModelGroupImpl.MODELGROUP_SEQUENCE,
            syntaxTree,
            nodeEOC
        );

        //
        //  Ok, so now we have to iterate the new tree and do a little more
        //  work now that we know the leaf count. One thing we need to do is
        //  to calculate the first and last position sets of each node. This
        //  is cached away in each of the nodes.
        //
        //  Along the way we also set the leaf count in each node as the
        //  maximum state count. They must know this in order to create their
        //  first/last pos sets.
        //
        //  We also need to build an array of references to the non-epsilon
        //  leaf nodes. Since we iterate it in the same way as before, this
        //  will put them in the array according to their position values.
        //
        fLeafList = new XSCMLeaf[fLeafCount];
        fLeafListType = new int[fLeafCount];
        postTreeBuildInit(fHeadNode);

        //
        //  And, moving onward... We now need to build the follow position
        //  sets for all the nodes. So we allocate an array of state sets,
        //  one for each leaf node (i.e. each DFA position.)
        //
        fFollowList = new CMStateSet[fLeafCount];
        for (int index = 0; index < fLeafCount; index++)
            fFollowList[index] = new CMStateSet(fLeafCount);
        calcFollowList(fHeadNode);
        //
        //  And finally the big push... Now we build the DFA using all the
        //  states and the tree we've built up. First we set up the various
        //  data structures we are going to use while we do this.
        //
        //  First of all we need an array of unique element names in our
        //  content model. For each transition table entry, we need a set of
        //  contiguous indices to represent the transitions for a particular
        //  input element. So we need to a zero based range of indexes that
        //  map to element types. This element map provides that mapping.
        //
        Object[] fElemMap = new Object[fLeafCount];
        int[] fElemMapType = new int[fLeafCount];
        int[] fElemMapId = new int[fLeafCount];
        int fElemMapSize = 0;
        Occurence [] elemOccurenceMap = null;
        int numElem = 0;
        for (int outIndex = 0; outIndex < fLeafCount; outIndex++) {
            // optimization from Henry Zongaro:
            //fElemMap[outIndex] = new Object ();
            fElemMap[outIndex] = null;

            int inIndex = 0;
            final int id = fLeafList[outIndex].getParticleId();
            for (; inIndex < fElemMapSize; inIndex++) {
                if (id == fElemMapId[inIndex])
                    break;
            }

            // If it was not in the list, then add it, if not the EOC node
            if (inIndex == fElemMapSize) {
                XSCMLeaf leaf = fLeafList[outIndex];
                fElemMap[fElemMapSize] = leaf.getLeaf();
                if (leaf instanceof XSCMRepeatingLeaf) {
                    if (elemOccurenceMap == null) {
                        elemOccurenceMap = new Occurence[fLeafCount];
                    }
                    elemOccurenceMap[fElemMapSize] = new Occurence((XSCMRepeatingLeaf) leaf, fElemMapSize);
                }
                fElemMapType[fElemMapSize] = fLeafListType[outIndex];
                fElemMapId[fElemMapSize] = id;
                if (fElemMapType[fElemMapSize] == XSParticleDecl.PARTICLE_ELEMENT) {
                    numElem++;
                }
                fElemMapSize++;
            }
        }

        // the last entry in the element map must be the EOC element.
        // remove it from the map.
        if (DEBUG) {
            if (fElemMapId[fElemMapSize-1] != -1)
                System.err.println("interal error in DFA: last element is not EOC.");
        }
        fElemMapSize--;
        numElem--;

        fNumTotal = fElemMapSize;
        if (fOpenContent != null) {
            fNumTotal++;
        }
        
        // Sort the element map so that elements appear before wildcards?
        // this will simplify oneTransition(), because of weakened wc.
        for (int ep=0, wp=fElemMapSize-1;;) {
            while (ep <= wp && fElemMapType[ep] == XSParticleDecl.PARTICLE_ELEMENT) {
                ep++;
            }
            while (wp >= ep && fElemMapType[wp] == XSParticleDecl.PARTICLE_WILDCARD) {
                wp--;
            }
            if (ep < wp) {
                Object t1 = fElemMap[ep];
                fElemMap[ep] = fElemMap[wp];
                fElemMap[wp] = t1;
                int t2 = fElemMapId[ep];
                fElemMapId[ep] = fElemMapId[wp];
                fElemMapId[wp] = t2;
                if (elemOccurenceMap != null) {
                    Occurence t3 = elemOccurenceMap[ep];
                    elemOccurenceMap[ep] = elemOccurenceMap[wp];
                    elemOccurenceMap[wp] = t3;
                    if (elemOccurenceMap[ep] != null) {
                        elemOccurenceMap[ep].elemIndex = ep;
                    }
                    if (elemOccurenceMap[wp] != null) {
                        elemOccurenceMap[wp].elemIndex = wp;
                    }
                }
                ep++;
                wp--;
            }
            else {
                break;
            }
        }
        
        /***
         * Optimization(Jan, 2001); We sort fLeafList according to
         * elemIndex which is *uniquely* associated to each leaf.
         * We are *assuming* that each element appears in at least one leaf.
         **/

        int[] fLeafSorter = new int[fLeafCount + fElemMapSize];
        int fSortCount = 0;

        for (int elemIndex = 0; elemIndex < fElemMapSize; elemIndex++) {
            final int id = fElemMapId[elemIndex];
            for (int leafIndex = 0; leafIndex < fLeafCount; leafIndex++) {
                if (id == fLeafList[leafIndex].getParticleId())
                    fLeafSorter[fSortCount++] = leafIndex;
            }
            fLeafSorter[fSortCount++] = -1;
        }

        /* Optimization(Jan, 2001) */

        //
        //  Next lets create some arrays, some that hold transient
        //  information during the DFA build and some that are permament.
        //  These are kind of sticky since we cannot know how big they will
        //  get, but we don't want to use any Java collections because of
        //  performance.
        //
        //  Basically they will probably be about fLeafCount*2 on average,
        //  but can be as large as 2^(fLeafCount*2), worst case. So we start
        //  with fLeafCount*4 as a middle ground. This will be very unlikely
        //  to ever have to expand, though it if does, the overhead will be
        //  somewhat ugly.
        //
        int curArraySize = fLeafCount * 4;
        CMStateSet[] statesToDo = new CMStateSet[curArraySize];
        fFinalStateFlags = new boolean[curArraySize];
        fTransTable = new int[curArraySize][];

        //
        //  Ok we start with the initial set as the first pos set of the
        //  head node (which is the seq node that holds the content model
        //  and the EOC node.)
        //
        CMStateSet setT = fHeadNode.firstPos();

        //
        //  Init our two state flags. Basically the unmarked state counter
        //  is always chasing the current state counter. When it catches up,
        //  that means we made a pass through that did not add any new states
        //  to the lists, at which time we are done. We could have used a
        //  expanding array of flags which we used to mark off states as we
        //  complete them, but this is easier though less readable maybe.
        //
        int unmarkedState = 0;
        int curState = 0;

        //
        //  Init the first transition table entry, and put the initial state
        //  into the states to do list, then bump the current state.
        //
        fTransTable[curState] = makeDefStateList();
        statesToDo[curState] = setT;
        curState++;

        /* Optimization(Jan, 2001); This is faster for
         * a large content model such as, "(t001+|t002+|.... |t500+)".
         */

        HashMap stateTable = new HashMap();

        /* Optimization(Jan, 2001) */

        //
        //  Ok, almost done with the algorithm... We now enter the
        //  loop where we go until the states done counter catches up with
        //  the states to do counter.
        //
        while (unmarkedState < curState) {
            //
            //  Get the first unmarked state out of the list of states to do.
            //  And get the associated transition table entry.
            //
            setT = statesToDo[unmarkedState];
            int[] transEntry = fTransTable[unmarkedState];

            // Mark this one final if it contains the EOC state
            fFinalStateFlags[unmarkedState] = setT.getBit(EOCPos);

            // Bump up the unmarked state count, marking this state done
            unmarkedState++;

            // Loop through each possible input symbol in the element map
            CMStateSet newSet = null;
            /* Optimization(Jan, 2001) */
            int sorterIndex = 0;
            /* Optimization(Jan, 2001) */
            for (int elemIndex = 0; elemIndex < fElemMapSize; elemIndex++) {
                //
                //  Build up a set of states which is the union of all of
                //  the follow sets of DFA positions that are in the current
                //  state. If we gave away the new set last time through then
                //  create a new one. Otherwise, zero out the existing one.
                //
                if (newSet == null)
                    newSet = new CMStateSet(fLeafCount);
                else
                    newSet.zeroBits();

                /* Optimization(Jan, 2001) */
                int leafIndex = fLeafSorter[sorterIndex++];

                while (leafIndex != -1) {
                    // If this leaf index (DFA position) is in the current set...
                    if (setT.getBit(leafIndex)) {
                        //
                        //  If this leaf is the current input symbol, then we
                        //  want to add its follow list to the set of states to
                        //  transition to from the current state.
                        //
                        newSet.union(fFollowList[leafIndex]);
                    }

                   leafIndex = fLeafSorter[sorterIndex++];
                }
                /* Optimization(Jan, 2001) */

                //
                //  If this new set is not empty, then see if its in the list
                //  of states to do. If not, then add it.
                //
                if (!newSet.isEmpty()) {
                    //
                    //  Search the 'states to do' list to see if this new
                    //  state set is already in there.
                    //

                    /* Optimization(Jan, 2001) */
                    Integer stateObj = (Integer)stateTable.get(newSet);
                    int stateIndex = (stateObj == null ? curState : stateObj.intValue());
                    /* Optimization(Jan, 2001) */

                    // If we did not find it, then add it
                    if (stateIndex == curState) {
                        //
                        //  Put this new state into the states to do and init
                        //  a new entry at the same index in the transition
                        //  table.
                        //
                        statesToDo[curState] = newSet;
                        fTransTable[curState] = makeDefStateList();

                        /* Optimization(Jan, 2001) */
                        stateTable.put(newSet, new Integer(curState));
                        /* Optimization(Jan, 2001) */

                        // We now have a new state to do so bump the count
                        curState++;

                        //
                        //  Null out the new set to indicate we adopted it.
                        //  This will cause the creation of a new set on the
                        //  next time around the loop.
                        //
                        newSet = null;
                    }

                    //
                    //  Now set this state in the transition table's entry
                    //  for this element (using its index), with the DFA
                    //  state we will move to from the current state when we
                    //  see this input element.
                    //
                    transEntry[elemIndex] = stateIndex;

                    // Expand the arrays if we're full
                    if (curState == curArraySize) {
                        //
                        //  Yikes, we overflowed the initial array size, so
                        //  we've got to expand all of these arrays. So adjust
                        //  up the size by 50% and allocate new arrays.
                        //
                        final int newSize = (int)(curArraySize * 1.5);
                        CMStateSet[] newToDo = new CMStateSet[newSize];
                        boolean[] newFinalFlags = new boolean[newSize];
                        int[][] newTransTable = new int[newSize][];

                        // Copy over all of the existing content
                        System.arraycopy(statesToDo, 0, newToDo, 0, curArraySize);
                        System.arraycopy(fFinalStateFlags, 0, newFinalFlags, 0, curArraySize);
                        System.arraycopy(fTransTable, 0, newTransTable, 0, curArraySize);

                        // Store the new array size
                        curArraySize = newSize;
                        statesToDo = newToDo;
                        fFinalStateFlags = newFinalFlags;
                        fTransTable = newTransTable;
                    }
                }
            }
        }
        
        //
        // Fill in the occurence information for each looping state 
        // if we're using counters.
        //
        if (elemOccurenceMap != null) {
            fCountingStates = new Occurence[curState];
            for (int i = 0; i < curState; ++i) {
                int [] transitions = fTransTable[i];
                for (int j = 0; j < transitions.length; ++j) {
                    if (i == transitions[j]) {
                        fCountingStates[i] = elemOccurenceMap[j];
                        break;
                    }
                }
            }
        }

        fTransTableSize = curState;

        //
        //  And now we can say bye bye to the temp representation since we've
        //  built the DFA.
        //
        if (DEBUG_VALIDATE_CONTENT)
            dumpTree(fHeadNode, 0);
        fHeadNode = null;
        fLeafList = null;
        fFollowList = null;
        fLeafListType = null;
        fElemMapId = null;
        
        // If there is open content, massage the transition table
        if (fOpenContent != null) {
            fElemMap[fElemMapSize] = fOpenContent.fWildcard;
            if (fOpenContent.fMode == XSOpenContentDecl.MODE_INTERLEAVE) {
                for (int i = 0; i < fTransTableSize; i++) {
                    fTransTable[i][fElemMapSize] = i;
                }
            }
            else {
                for (int i = 0; i < fTransTableSize; i++) {
                    if (fFinalStateFlags[i]) {
                        fTransTable[i][fElemMapSize] = fTransTableSize;
                    }
                }
                // Seems that there is at least one empty spot.
                fTransTable[fTransTableSize] = makeDefStateList();
                fTransTable[fTransTableSize][fElemMapSize] = fTransTableSize;
                fFinalStateFlags[fTransTableSize] = true;
                fTransTableSize++;
            }
            fElemMapSize++;
        }

        fNumElements = numElem;
        if (numElem > 0) {
            fElements = new XSElementDecl[numElem];
        }
        if (fNumTotal > numElem) {
            fWildcards = new XSWildcardDecl[fNumTotal];
        }
        for (int i = 0; i < numElem; i++) {
            fElements[i] = (XSElementDecl)fElemMap[i];
        }
        for (int i = numElem; i < fNumTotal; i++) {
            fWildcards[i] = (XSWildcardDecl)fElemMap[i];
        }
    }

    /**
     * Calculates the follow list of the current node.
     *
     * @param nodeCur The curent node.
     *
     * @exception RuntimeException Thrown if follow list cannot be calculated.
     */
    private void calcFollowList(CMNode nodeCur) {
        // Recurse as required
        if (nodeCur.type() == XSModelGroupImpl.MODELGROUP_CHOICE) {
            // Recurse only
            calcFollowList(((XSCMBinOp)nodeCur).getLeft());
            calcFollowList(((XSCMBinOp)nodeCur).getRight());
        }
         else if (nodeCur.type() == XSModelGroupImpl.MODELGROUP_SEQUENCE) {
            // Recurse first
            calcFollowList(((XSCMBinOp)nodeCur).getLeft());
            calcFollowList(((XSCMBinOp)nodeCur).getRight());

            //
            //  Now handle our level. We use our left child's last pos
            //  set and our right child's first pos set, so go ahead and
            //  get them ahead of time.
            //
            final CMStateSet last  = ((XSCMBinOp)nodeCur).getLeft().lastPos();
            final CMStateSet first = ((XSCMBinOp)nodeCur).getRight().firstPos();

            //
            //  Now, for every position which is in our left child's last set
            //  add all of the states in our right child's first set to the
            //  follow set for that position.
            //
            for (int index = 0; index < fLeafCount; index++) {
                if (last.getBit(index))
                    fFollowList[index].union(first);
            }
        }
         else if (nodeCur.type() == XSParticleDecl.PARTICLE_ZERO_OR_MORE
        || nodeCur.type() == XSParticleDecl.PARTICLE_ONE_OR_MORE) {
            // Recurse first
            calcFollowList(((XSCMUniOp)nodeCur).getChild());

            //
            //  Now handle our level. We use our own first and last position
            //  sets, so get them up front.
            //
            final CMStateSet first = nodeCur.firstPos();
            final CMStateSet last  = nodeCur.lastPos();

            //
            //  For every position which is in our last position set, add all
            //  of our first position states to the follow set for that
            //  position.
            //
            for (int index = 0; index < fLeafCount; index++) {
                if (last.getBit(index))
                    fFollowList[index].union(first);
            }
        }

        else if (nodeCur.type() == XSParticleDecl.PARTICLE_ZERO_OR_ONE) {
            // Recurse only
            calcFollowList(((XSCMUniOp)nodeCur).getChild());
        }

    }

    /**
     * Dumps the tree of the current node to standard output.
     *
     * @param nodeCur The current node.
     * @param level   The maximum levels to output.
     *
     * @exception RuntimeException Thrown on error.
     */
    private void dumpTree(CMNode nodeCur, int level) {
        for (int index = 0; index < level; index++)
            System.out.print("   ");

        int type = nodeCur.type();

        switch(type ) {

        case XSModelGroupImpl.MODELGROUP_CHOICE:
        case XSModelGroupImpl.MODELGROUP_SEQUENCE: {
            if (type == XSModelGroupImpl.MODELGROUP_CHOICE)
                System.out.print("Choice Node ");
            else
                System.out.print("Seq Node ");

            if (nodeCur.isNullable())
                System.out.print("Nullable ");

            System.out.print("firstPos=");
            System.out.print(nodeCur.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(nodeCur.lastPos().toString());

            dumpTree(((XSCMBinOp)nodeCur).getLeft(), level+1);
            dumpTree(((XSCMBinOp)nodeCur).getRight(), level+1);
            break;
        }
        case XSParticleDecl.PARTICLE_ZERO_OR_MORE:
        case XSParticleDecl.PARTICLE_ONE_OR_MORE:
        case XSParticleDecl.PARTICLE_ZERO_OR_ONE: {
            System.out.print("Rep Node ");

            if (nodeCur.isNullable())
                System.out.print("Nullable ");

            System.out.print("firstPos=");
            System.out.print(nodeCur.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(nodeCur.lastPos().toString());

            dumpTree(((XSCMUniOp)nodeCur).getChild(), level+1);
            break;
        }
        case XSParticleDecl.PARTICLE_ELEMENT: {
            System.out.print
            (
                "Leaf: (pos="
                + ((XSCMLeaf)nodeCur).getPosition()
                + "), "
                + "(elemIndex="
                + ((XSCMLeaf)nodeCur).getLeaf()
                + ") "
            );

            if (nodeCur.isNullable())
                System.out.print(" Nullable ");

            System.out.print("firstPos=");
            System.out.print(nodeCur.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(nodeCur.lastPos().toString());
            break;
        }
        case XSParticleDecl.PARTICLE_WILDCARD:
              System.out.print("Any Node: ");

            System.out.print("firstPos=");
            System.out.print(nodeCur.firstPos().toString());
            System.out.print(" lastPos=");
            System.out.println(nodeCur.lastPos().toString());
            break;
        default: {
            throw new RuntimeException("ImplementationMessages.VAL_NIICM");
        }
        }

    }


    /**
     * -1 is used to represent bad transitions in the transition table
     * entry for each state. So each entry is initialized to an all -1
     * array. This method creates a new entry and initializes it.
     */
    private int[] makeDefStateList()
    {
        int[] retArray = new int[fNumTotal];
        for (int index = 0; index < fNumTotal; index++)
            retArray[index] = -1;
        return retArray;
    }

    /** Post tree build initialization. */
    private void postTreeBuildInit(CMNode nodeCur) throws RuntimeException {
        // Set the maximum states on this node
        nodeCur.setMaxStates(fLeafCount);

        XSCMLeaf leaf = null;
        int pos = 0;
        // Recurse as required
        if (nodeCur.type() == XSParticleDecl.PARTICLE_WILDCARD) {
            leaf = (XSCMLeaf)nodeCur;
            pos = leaf.getPosition();
            fLeafList[pos] = leaf;
            fLeafListType[pos] = XSParticleDecl.PARTICLE_WILDCARD;
        }
        else if ((nodeCur.type() == XSModelGroupImpl.MODELGROUP_CHOICE) ||
                 (nodeCur.type() == XSModelGroupImpl.MODELGROUP_SEQUENCE)) {
            postTreeBuildInit(((XSCMBinOp)nodeCur).getLeft());
            postTreeBuildInit(((XSCMBinOp)nodeCur).getRight());
        }
        else if (nodeCur.type() == XSParticleDecl.PARTICLE_ZERO_OR_MORE ||
                 nodeCur.type() == XSParticleDecl.PARTICLE_ONE_OR_MORE ||
                 nodeCur.type() == XSParticleDecl.PARTICLE_ZERO_OR_ONE) {
            postTreeBuildInit(((XSCMUniOp)nodeCur).getChild());
        }
        else if (nodeCur.type() == XSParticleDecl.PARTICLE_ELEMENT) {
            //  Put this node in the leaf list at the current index if its
            //  a non-epsilon leaf.
            leaf = (XSCMLeaf)nodeCur;
            pos = leaf.getPosition();
            fLeafList[pos] = leaf;
            fLeafListType[pos] = XSParticleDecl.PARTICLE_ELEMENT;
        }
        else {
            throw new RuntimeException("ImplementationMessages.VAL_NIICM");
        }
    }

    /**
     * check whether this content violates UPA constraint.
     *
     * @param subGroupHandler the substitution group handler
     * @param xsConstraints the XML Schema Constraint checker
     * @return true if this content model contains other or list wildcard
     */
    public boolean checkUniqueParticleAttribution(SubstitutionGroupHandler subGroupHandler, XSConstraints xsConstraints) throws XMLSchemaException {
        int elemSize = fOpenContent != null ? fNumTotal - 1 : fNumTotal;
        // Unique Particle Attribution
        // store the conflict results between any two elements in fElemMap
        // 0: not compared; -1: no conflict; 1: conflict
        // initialize the conflict table (all 0 initially)
        byte conflictTable[][] = new byte[elemSize][elemSize];

        // for each state, check whether it has overlap transitions
        for (int i = 0; i < fTransTableSize; i++) {
            for (int j = 0; j < elemSize; j++) {
                for (int k = j+1; k < elemSize; k++) {
                    if (fTransTable[i][j] != -1 &&
                        fTransTable[i][k] != -1) {
                        if (conflictTable[j][k] == 0) {
                            if (xsConstraints.overlapUPA
                                    (j < fNumElements ? (Object)fElements[j] : (Object)fWildcards[j],
                                     k < fNumElements ? (Object)fElements[k] : (Object)fWildcards[k],
                                     subGroupHandler)) {
                                if (fCountingStates != null) {
                                    Occurence o = fCountingStates[i];
                                    // If "i" is a counting state and exactly one of the transitions
                                    // loops back to "i" then the two particles do not overlap if
                                    // minOccurs == maxOccurs.
                                    if (o != null && 
                                        fTransTable[i][j] == i ^ fTransTable[i][k] == i && 
                                        o.minOccurs == o.maxOccurs) {
                                        conflictTable[j][k] = (byte) -1;
                                        continue;
                                    }
                                }
                                conflictTable[j][k] = (byte) 1;
                            }
                            else {
                                conflictTable[j][k] = (byte) -1;
                            }
                        }
                    }
                }
            }
        }

        // report all errors
        for (int i = 0; i < elemSize; i++) {
            for (int j = 0; j < elemSize; j++) {
                if (conflictTable[i][j] == 1) {
                    //errors.newError("cos-nonambig", new Object[]{fElemMap[i].toString(),
                    //                                             fElemMap[j].toString()});
                    // REVISIT: do we want to report all errors? or just one?
                    throw new XMLSchemaException("cos-nonambig", new Object[]{
                            i < fNumElements ? (Object)fElements[i] : (Object)fWildcards[i],
                            j < fNumElements ? (Object)fElements[j] : (Object)fWildcards[j]});
                }
            }
        }

        // if there is a other or list wildcard, we need to check this CM
        // again, if this grammar is cached.
        for (int i = fNumElements; i < elemSize; i++) {
            XSWildcardDecl wildcard = fWildcards[i];
            if (wildcard.fType == XSWildcardDecl.NSCONSTRAINT_LIST ||
                wildcard.fType == XSWildcardDecl.NSCONSTRAINT_NOT) {
                return true;
            }
        }

        return false;
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
        int elemSize = fOpenContent != null ? fNumTotal - 1 : fNumTotal;

        int curState = state[0];
        if (curState < 0)
            curState = state[1];
        Occurence o = (fCountingStates != null) ? 
                fCountingStates[curState] : null;
        int count = state[2];

        Vector ret = new Vector();
        for (int elemIndex = 0; elemIndex < elemSize; elemIndex++) {
            int nextState = fTransTable[curState][elemIndex];
            if (nextState != -1) {
                if (o != null) {
                    if (curState == nextState) {
                        // Do not include transitions which loop back to the
                        // current state if we've looped the maximum number
                        // of times or greater.
                        if (count >= o.maxOccurs &&
                            o.maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                            continue;
                        }
                    }
                    // Do not include transitions which advance past the
                    // current state if we have not looped enough times.
                    else if (count < o.minOccurs) {
                        continue;
                    }
                }
                ret.addElement(elemIndex < fNumElements ? (Object)fElements[elemIndex] : (Object)fWildcards[elemIndex]);
            }  
        }
        return ret;
    }
    
    public int [] occurenceInfo(int[] state) {
        if (fCountingStates != null) {
            int curState = state[0];
            if (curState < 0) {
                curState = state[1];
            }
            Occurence o = fCountingStates[curState];
            if (o != null) {
                int [] occurenceInfo = new int[4];
                occurenceInfo[0] = o.minOccurs;
                occurenceInfo[1] = o.maxOccurs;
                occurenceInfo[2] = state[2];
                occurenceInfo[3] = o.elemIndex;
                return occurenceInfo;
            }
        }
        return null;
    }
    
    public String getTermName(int termId) {
        Object term = termId < fNumElements ? (Object)fElements[termId] : (Object)fWildcards[termId];
        return (term != null) ? term.toString() : null;
    }
        
    public boolean isCompactedForUPA() {
        return fIsCompactedForUPA;
    }
    
    public XSElementDecl nextElementTransition(int[] s, int[] sn, int[] index) {
        for (int idx = index[0] + 1; idx < fNumElements; idx++) {
            if (isAllowedTransition(s, sn, idx)) {
                index[0] = idx;
                return fElements[idx];
            }
        }
        index[0] = -1;
        return null;
    }
    public XSWildcardDecl nextWildcardTransition(int[] s, int[] sn, int[] index) {
        for (int idx = index[0] == -1 ? fNumElements : index[0] + 1; idx < fNumTotal; idx++) {
            if (isAllowedTransition(s, sn, idx)) {
                index[0] = idx;
                return fWildcards[idx];
            }
        }
        index[0] = -1;
        return null;
    }
    private boolean isAllowedTransition(int[] s, int[] sn, int index) {
        int n = fTransTable[s[0]][index];
        if (n == -1) {
            // Transition not allowed
            return false;
        }
        // Record the next state
        if (sn != null) {
            sn[0] = n;
        }

        // If there are no counting state, the transition is allowed.
        if (fCountingStates == null) {
            return true;
        }
        
        // If the transition is for an interleave open content, then it's
        // always allowed, even while counting.
        if (index == fNumTotal-1 && fOpenContent != null &&
                fOpenContent.fMode == XSOpenContentDecl.MODE_INTERLEAVE) {
            return true;
        }
        
        // Handling of counting states
        Occurence o = fCountingStates[s[0]];
        if (o != null) {
            // If the previous state is a counting state
            if (s[0] == n) {
                // And the transition loops back to the same state
                if (s[2] == o.maxOccurs) {
                    // Too many when trying to stay in this loop
                    return false;
                }
                // Allowed transition. Increase the count. Don't need to do so
                // when we've satisfied the minOccurs requirement and maxOccurs
                // is unbounded.
                if (sn != null) {
                    sn[2] = s[2];
                    if (sn[2] == 0 || sn[2] < o.minOccurs || o.maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                        sn[2]++;
                    }
                }
            }
            else if (s[2] < o.minOccurs) {
                // Exiting a counting state, but minOccurs is not satisfied.
                return false;
            }
            else {
                // Exiting a counting state. If we're entering a new
                // counting state, reset the counter.
                o = fCountingStates[n];
                if (o != null && sn != null) {
                    sn[2] = (index == o.elemIndex) ? 1 : 0;
                }
            }
        }
        else {
            // Previous state was not counting. Check the new state.
            o = fCountingStates[n];
            if (o != null && sn != null) {
                // Entering a new counting state. Reset the counter.
                sn[2] = (index == o.elemIndex) ? 1 : 0;
            }
        }

        return true;
    }
    public boolean isOpenContent(XSWildcardDecl w) {
        return fOpenContent != null && fOpenContent.fWildcard == w;
    }
    public List getDefinedNames(SubstitutionGroupHandler subGroupHandler) {
        // Add names of all known elements and their sub-group members
        List ret = new ArrayList();
        for (int i = 0; i < fNumElements; i++) {
            XSElementDecl e = fElements[i];
            ret.add(e.fTargetNamespace);
            ret.add(e.fName);
            if (e.fScope == XSElementDecl.SCOPE_GLOBAL) {
                XSElementDecl[] es = subGroupHandler.getSubstitutionGroup(e, fSchemaVersion);
                for (int j = 0; j < es.length; j++) {
                    ret.add(es[j].fTargetNamespace);
                    ret.add(es[j].fName);
                }
            }
        }
        return ret;
    }
    public void optimizeStates(XS11CMRestriction.XS11CM base, int[] b, int[] d, int indexb) {
        // Can only optimize if the current state is a counting state.
        if (fCountingStates == null || fCountingStates[d[0]] == null) {
            return;
        }
        // Not sure whether we need this. Better to optimize only when we've
        // seen at least one element, to be sure we are "locked in" this state.
        if (d[2] <= 0) {
            return;
        }
        // When the count is less than minOccurs, or greater than minOccurs
        // but less than maxOccurs, then we can try to bring the count up to
        // min/maxOccurs, if base has sufficient space.
        int need = 0;
        if (d[2] < fCountingStates[d[0]].minOccurs) {
            need = fCountingStates[d[0]].minOccurs - d[2];
        }
        else if (d[2] > fCountingStates[d[0]].minOccurs && d[2] < fCountingStates[d[0]].maxOccurs) {
            need = fCountingStates[d[0]].maxOccurs - d[2];
        }
        // Derived has no space.
        if (need == 0) {
            return;
        }
        // Different strategy for different base kind.
        if (base instanceof XSDFACM) {
            optimizeForDFABase((XSDFACM)base, b, d, need);
        }
        else if (base instanceof XS11AllCM) {
            optimizeForAllBase((XS11AllCM)base, b, d, need, indexb);
        }
    }
    private void optimizeForDFABase(XSDFACM base, int[] b, int[] d, int need) {
        // Base must also be on a counting state
        if (base.fCountingStates == null || base.fCountingStates[b[0]] == null) {
            return;
        }
        // And have seen at least one element
        if (b[2] <= 0) {
            return;
        }
        // If base has max=unbounded, then there's enough space for "need"
        if (base.fCountingStates[d[0]] != null) {
            if (base.fCountingStates[d[0]].maxOccurs == SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                d[2] += need;
                if (b[2] + need > base.fCountingStates[d[0]].minOccurs) {
                    // Avoid setting count bigger than min if max is unbounded.
                    // It makes no difference. Not exceeding min means fewer
                    // distinct states, so shorter state-pairs in the list.
                    b[2] = base.fCountingStates[d[0]].minOccurs;
                }
                else {
                    b[2] += need;
                }
            }
            else {
                // If base doesn't have sufficient space, lower "need"
                if (need > base.fCountingStates[d[0]].maxOccurs - b[2]) {
                    need = base.fCountingStates[d[0]].maxOccurs - b[2];
                }
                b[2] += need;
                d[2] += need;
            }
        }
    }
    private void optimizeForAllBase(XS11AllCM base, int[] b, int[] d, int need, int indexb) {
        // Need to have seen an element, to be safe.
        if (b[indexb] <= 0) {
            return;
        }
        // Similar to the DFA case.
        if (base.maxOccurs(indexb) == SchemaSymbols.OCCURRENCE_UNBOUNDED) {
            d[2] += need;
            if (b[indexb] + need > base.minOccurs(indexb)) {
                b[indexb] = base.minOccurs(indexb);
            }
            else {
                b[indexb] += need;
            }
        }
        else {
            if (need > base.maxOccurs(indexb) - b[indexb]) {
                need = base.maxOccurs(indexb) - b[indexb];
            }
            b[indexb] += need;
            d[2] += need;
        }
    }

} // class DFAContentModel
