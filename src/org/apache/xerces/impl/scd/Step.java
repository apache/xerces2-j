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

import org.apache.xerces.xni.QName;

/**
 * This class represents a step in an SCP/SCD string.
 * @author Ishan Jayawardena udeshike@gmail.com
 * @version $Id$
 */
class Step {
    private final short axis;
    private final QName nametest;
    private final int predicate;

    public Step(short axis, QName nametest, int predicate) {
        this.axis = axis;
        this.nametest = nametest;
        this.predicate = predicate;
    }

    /**
     * returns the axis type of the axis contained in the step
     * @return the axis type
     */
    public short getAxisType() {
        return this.axis;
    }

    /**
     * the string representation of the axis of the step
     * @return the axis name
     */
    public String getAxisName() {
        return Axis.axisToString(axis);
    }

    /**
     * returns the name test of the step
     * @return the nametest
     */
    public QName getNametest() {
        return this.nametest;
    }

    /**
     * returns the predicate value of the step
     * @return the predicate
     */
    public int getPredicate() {
        return this.predicate;
    }

    /**
     * prints the content of the axis
     */
    public String toString() {
        return "(" +
        "axis=" + Axis.axisToString(axis) +
        ", nametest=" + ((nametest != null) ? ("{\"" + nametest.uri + "\"" + " \"" + nametest.rawname + "\"}") : null) +
        ", predicate= " + predicate +
        ")";
    }
} // class Step
