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

package org.apache.xerces.impl.xpath;

/**
 * (Parser generated using JavaCC)
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class XPath20ParserTokenManager {

    /** Debug output. */
    public java.io.PrintStream debugStream = System.out;

    /** Set debug output. */
    public void setDebugStream(java.io.PrintStream ds) {
        debugStream = ds;
    }

    private final int jjStopStringLiteralDfa_0(int pos, long active0) {
        switch (pos) {
        case 0:
            if ((active0 & 0x800000L) != 0L)
                return 23;
            if ((active0 & 0x400000L) != 0L)
                return 24;
            if ((active0 & 0x3cL) != 0L) {
                jjmatchedKind = 19;
                return 22;
            }
            return -1;
        case 1:
            if ((active0 & 0x28L) != 0L)
                return 22;
            if ((active0 & 0x400000L) != 0L)
                return 4;
            if ((active0 & 0x800000L) != 0L)
                return 9;
            if ((active0 & 0x14L) != 0L) {
                jjmatchedKind = 19;
                jjmatchedPos = 1;
                return 22;
            }
            return -1;
        case 2:
            if ((active0 & 0x4L) != 0L)
                return 22;
            if ((active0 & 0x10L) != 0L) {
                jjmatchedKind = 19;
                jjmatchedPos = 2;
                return 22;
            }
            return -1;
        default:
            return -1;
        }
    }

    private final int jjStartNfa_0(int pos, long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private int jjStopAtPos(int pos, int kind) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_0() {
        switch (curChar) {
        case 10:
            return jjStopAtPos(0, 25);
        case 33:
            return jjMoveStringLiteralDfa1_0(0x1000L);
        case 34:
            return jjMoveStringLiteralDfa1_0(0x400000L);
        case 39:
            return jjMoveStringLiteralDfa1_0(0x800000L);
        case 40:
            return jjStopAtPos(0, 9);
        case 41:
            return jjStopAtPos(0, 10);
        case 58:
            return jjStopAtPos(0, 6);
        case 60:
            jjmatchedKind = 13;
            return jjMoveStringLiteralDfa1_0(0x8000L);
        case 61:
            return jjStopAtPos(0, 11);
        case 62:
            jjmatchedKind = 14;
            return jjMoveStringLiteralDfa1_0(0x10000L);
        case 63:
            return jjStopAtPos(0, 8);
        case 64:
            return jjStopAtPos(0, 7);
        case 97:
            return jjMoveStringLiteralDfa1_0(0x24L);
        case 99:
            return jjMoveStringLiteralDfa1_0(0x10L);
        case 111:
            return jjMoveStringLiteralDfa1_0(0x8L);
        default:
            return jjMoveNfa_0(0, 0);
        }
    }

    private int jjMoveStringLiteralDfa1_0(long active0) {
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (curChar) {
        case 34:
            if ((active0 & 0x400000L) != 0L)
                return jjStartNfaWithStates_0(1, 22, 4);
            break;
        case 39:
            if ((active0 & 0x800000L) != 0L)
                return jjStartNfaWithStates_0(1, 23, 9);
            break;
        case 61:
            if ((active0 & 0x1000L) != 0L)
                return jjStopAtPos(1, 12);
            else if ((active0 & 0x8000L) != 0L)
                return jjStopAtPos(1, 15);
            else if ((active0 & 0x10000L) != 0L)
                return jjStopAtPos(1, 16);
            break;
        case 97:
            return jjMoveStringLiteralDfa2_0(active0, 0x10L);
        case 110:
            return jjMoveStringLiteralDfa2_0(active0, 0x4L);
        case 114:
            if ((active0 & 0x8L) != 0L)
                return jjStartNfaWithStates_0(1, 3, 22);
            break;
        case 115:
            if ((active0 & 0x20L) != 0L)
                return jjStartNfaWithStates_0(1, 5, 22);
            break;
        default:
            break;
        }
        return jjStartNfa_0(0, active0);
    }

    private int jjMoveStringLiteralDfa2_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(0, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (curChar) {
        case 100:
            if ((active0 & 0x4L) != 0L)
                return jjStartNfaWithStates_0(2, 2, 22);
            break;
        case 115:
            return jjMoveStringLiteralDfa3_0(active0, 0x10L);
        default:
            break;
        }
        return jjStartNfa_0(1, active0);
    }

    private int jjMoveStringLiteralDfa3_0(long old0, long active0) {
        if (((active0 &= old0)) == 0L)
            return jjStartNfa_0(1, old0);
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
        switch (curChar) {
        case 116:
            if ((active0 & 0x10L) != 0L)
                return jjStartNfaWithStates_0(3, 4, 22);
            break;
        default:
            break;
        }
        return jjStartNfa_0(2, active0);
    }

    private int jjStartNfaWithStates_0(int pos, int kind, int state) {
        jjmatchedKind = kind;
        jjmatchedPos = pos;
        try {
            curChar = input_stream.readChar();
        } catch (java.io.IOException e) {
            return pos + 1;
        }
        return jjMoveNfa_0(state, pos + 1);
    }

    static final long[] jjbitVec0 = { 0x0L, 0x0L, 0xffffffffffffffffL,
            0xffffffffffffffffL };

    private int jjMoveNfa_0(int startState, int curPos) {
        int startsAt = 0;
        jjnewStateCnt = 23;
        int i = 1;
        jjstateSet[0] = startState;
        int kind = 0x7fffffff;
        for (;;) {
            if (++jjround == 0x7fffffff)
                ReInitRounds();
            if (curChar < 64) {
                long l = 1L << curChar;
                do {
                    switch (jjstateSet[--i]) {
                    case 23:
                        if ((0xffffff7fffffffffL & l) != 0L)
                            jjCheckNAddStates(0, 2);
                        else if (curChar == 39) {
                            if (kind > 24)
                                kind = 24;
                        }
                        if (curChar == 39)
                            jjstateSet[jjnewStateCnt++] = 9;
                        break;
                    case 24:
                        if ((0xfffffffbffffffffL & l) != 0L)
                            jjCheckNAddStates(3, 5);
                        else if (curChar == 34) {
                            if (kind > 24)
                                kind = 24;
                        }
                        if (curChar == 34)
                            jjstateSet[jjnewStateCnt++] = 4;
                        break;
                    case 0:
                        if ((0x3ff600000000000L & l) != 0L) {
                            if (kind > 21)
                                kind = 21;
                        } else if (curChar == 39)
                            jjCheckNAddStates(0, 2);
                        else if (curChar == 34)
                            jjCheckNAddStates(3, 5);
                        if ((0x3ff000000000000L & l) != 0L) {
                            if (kind > 17)
                                kind = 17;
                            jjCheckNAddStates(6, 9);
                        } else if (curChar == 46)
                            jjCheckNAdd(1);
                        break;
                    case 1:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAdd(1);
                        break;
                    case 2:
                        if ((0x3ff600000000000L & l) != 0L && kind > 21)
                            kind = 21;
                        break;
                    case 3:
                    case 4:
                        if (curChar == 34)
                            jjCheckNAddStates(3, 5);
                        break;
                    case 5:
                        if (curChar == 34)
                            jjstateSet[jjnewStateCnt++] = 4;
                        break;
                    case 6:
                        if ((0xfffffffbffffffffL & l) != 0L)
                            jjCheckNAddStates(3, 5);
                        break;
                    case 7:
                        if (curChar == 34 && kind > 24)
                            kind = 24;
                        break;
                    case 8:
                    case 9:
                        if (curChar == 39)
                            jjCheckNAddStates(0, 2);
                        break;
                    case 10:
                        if (curChar == 39)
                            jjstateSet[jjnewStateCnt++] = 9;
                        break;
                    case 11:
                        if ((0xffffff7fffffffffL & l) != 0L)
                            jjCheckNAddStates(0, 2);
                        break;
                    case 12:
                        if (curChar == 39 && kind > 24)
                            kind = 24;
                        break;
                    case 13:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAddStates(6, 9);
                        break;
                    case 14:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAddStates(10, 12);
                        break;
                    case 15:
                        if (curChar != 46)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAddTwoStates(16, 17);
                        break;
                    case 16:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAddTwoStates(16, 17);
                        break;
                    case 18:
                        if ((0x280000000000L & l) != 0L)
                            jjCheckNAdd(19);
                        break;
                    case 19:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 17)
                            kind = 17;
                        jjCheckNAdd(19);
                        break;
                    case 20:
                        if ((0x3ff000000000000L & l) == 0L)
                            break;
                        if (kind > 18)
                            kind = 18;
                        jjCheckNAdd(20);
                        break;
                    case 22:
                        if ((0x3ff600000000000L & l) == 0L)
                            break;
                        if (kind > 19)
                            kind = 19;
                        jjstateSet[jjnewStateCnt++] = 22;
                        break;
                    default:
                        break;
                    }
                } while (i != startsAt);
            } else if (curChar < 128) {
                long l = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                    case 23:
                    case 11:
                        jjCheckNAddStates(0, 2);
                        break;
                    case 24:
                    case 6:
                        jjCheckNAddStates(3, 5);
                        break;
                    case 0:
                        if ((0x7fffffe87fffffeL & l) != 0L) {
                            if (kind > 19)
                                kind = 19;
                            jjCheckNAdd(22);
                        }
                        if ((0x7fffffe87fffffeL & l) != 0L) {
                            if (kind > 21)
                                kind = 21;
                        }
                        break;
                    case 2:
                        if ((0x7fffffe87fffffeL & l) != 0L && kind > 21)
                            kind = 21;
                        break;
                    case 17:
                        if ((0x2000000020L & l) != 0L)
                            jjAddStates(13, 14);
                        break;
                    case 21:
                    case 22:
                        if ((0x7fffffe87fffffeL & l) == 0L)
                            break;
                        if (kind > 19)
                            kind = 19;
                        jjCheckNAdd(22);
                        break;
                    default:
                        break;
                    }
                } while (i != startsAt);
            } else {
                int i2 = (curChar & 0xff) >> 6;
                long l2 = 1L << (curChar & 077);
                do {
                    switch (jjstateSet[--i]) {
                    case 23:
                    case 11:
                        if ((jjbitVec0[i2] & l2) != 0L)
                            jjCheckNAddStates(0, 2);
                        break;
                    case 24:
                    case 6:
                        if ((jjbitVec0[i2] & l2) != 0L)
                            jjCheckNAddStates(3, 5);
                        break;
                    default:
                        break;
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                jjmatchedKind = kind;
                jjmatchedPos = curPos;
                kind = 0x7fffffff;
            }
            ++curPos;
            if ((i = jjnewStateCnt) == (startsAt = 23 - (jjnewStateCnt = startsAt)))
                return curPos;
            try {
                curChar = input_stream.readChar();
            } catch (java.io.IOException e) {
                return curPos;
            }
        }
    }

    static final int[] jjnextStates = { 10, 11, 12, 5, 6, 7, 14, 15, 17, 20,
            14, 15, 17, 18, 19, };

    /** Token literal values. */
    public static final String[] jjstrLiteralImages = { "", null,
            "\141\156\144", "\157\162", "\143\141\163\164", "\141\163", "\72",
            "\100", "\77", "\50", "\51", "\75", "\41\75", "\74", "\76",
            "\74\75", "\76\75", null, null, null, null, null, "\42\42",
            "\47\47", null, "\12", };

    /** Lexer state names. */
    public static final String[] lexStateNames = { "DEFAULT", };
    static final long[] jjtoToken = { 0x3fffffdL, };
    static final long[] jjtoSkip = { 0x2L, };
    protected SimpleCharStream input_stream;
    private final int[] jjrounds = new int[23];
    private final int[] jjstateSet = new int[46];
    protected char curChar;

    /** Constructor. */
    public XPath20ParserTokenManager(SimpleCharStream stream) {
        if (SimpleCharStream.staticFlag)
            throw new Error(
                    "ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
        input_stream = stream;
    }

    /** Constructor. */
    public XPath20ParserTokenManager(SimpleCharStream stream, int lexState) throws XPathException {
        this(stream);
        SwitchTo(lexState);
    }

    /** Reinitialise parser. */
    public void ReInit(SimpleCharStream stream) {
        jjmatchedPos = jjnewStateCnt = 0;
        curLexState = defaultLexState;
        input_stream = stream;
        ReInitRounds();
    }

    private void ReInitRounds() {
        int i;
        jjround = 0x80000001;
        for (i = 23; i-- > 0;)
            jjrounds[i] = 0x80000000;
    }

    /** Reinitialise parser. */
    public void ReInit(SimpleCharStream stream, int lexState) throws XPathException {
        ReInit(stream);
        SwitchTo(lexState);
    }

    /** Switch to specified lex state. */
    public void SwitchTo(int lexState) throws XPathException {
        if (lexState >= 1 || lexState < 0)
            throw new XPathException("c-general-xpath");
        else
            curLexState = lexState;
    }

    protected Token jjFillToken() {
        final Token t;
        final String curTokenImage;
        final int beginLine;
        final int endLine;
        final int beginColumn;
        final int endColumn;
        String im = jjstrLiteralImages[jjmatchedKind];
        curTokenImage = (im == null) ? input_stream.GetImage() : im;
        beginLine = input_stream.getBeginLine();
        beginColumn = input_stream.getBeginColumn();
        endLine = input_stream.getEndLine();
        endColumn = input_stream.getEndColumn();
        t = Token.newToken(jjmatchedKind, curTokenImage);

        t.beginLine = beginLine;
        t.endLine = endLine;
        t.beginColumn = beginColumn;
        t.endColumn = endColumn;

        return t;
    }

    int curLexState = 0;
    int defaultLexState = 0;
    int jjnewStateCnt;
    int jjround;
    int jjmatchedPos;
    int jjmatchedKind;

    /** Get the next Token. */
    public Token getNextToken() throws XPathException {
        Token matchedToken;
        int curPos = 0;

        EOFLoop: for (;;) {
            try {
                curChar = input_stream.BeginToken();
            } catch (java.io.IOException e) {
                jjmatchedKind = 0;
                matchedToken = jjFillToken();
                return matchedToken;
            }

            try {
                input_stream.backup(0);
                while (curChar <= 32 && (0x100000000L & (1L << curChar)) != 0L)
                    curChar = input_stream.BeginToken();
            } catch (java.io.IOException e1) {
                continue EOFLoop;
            }
            jjmatchedKind = 0x7fffffff;
            jjmatchedPos = 0;
            curPos = jjMoveStringLiteralDfa0_0();
            if (jjmatchedKind != 0x7fffffff) {
                if (jjmatchedPos + 1 < curPos)
                    input_stream.backup(curPos - jjmatchedPos - 1);
                if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
                    matchedToken = jjFillToken();
                    return matchedToken;
                } else {
                    continue EOFLoop;
                }
            }
            int error_line = input_stream.getEndLine();
            int error_column = input_stream.getEndColumn();
            boolean EOFSeen = false;
            try {
                input_stream.readChar();
                input_stream.backup(1);
            } catch (java.io.IOException e1) {
                EOFSeen = true;
                if (curChar == '\n' || curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else
                    error_column++;
            }
            if (!EOFSeen) {
                input_stream.backup(1);
            }
            throw new XPathException("c-general-xpath");
        }
    }

    private void jjCheckNAdd(int state) {
        if (jjrounds[state] != jjround) {
            jjstateSet[jjnewStateCnt++] = state;
            jjrounds[state] = jjround;
        }
    }

    private void jjAddStates(int start, int end) {
        do {
            jjstateSet[jjnewStateCnt++] = jjnextStates[start];
        } while (start++ != end);
    }

    private void jjCheckNAddTwoStates(int state1, int state2) {
        jjCheckNAdd(state1);
        jjCheckNAdd(state2);
    }

    private void jjCheckNAddStates(int start, int end) {
        do {
            jjCheckNAdd(jjnextStates[start]);
        } while (start++ != end);
    }

}
