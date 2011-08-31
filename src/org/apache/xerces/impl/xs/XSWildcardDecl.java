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

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSWildcard;

/**
 * The XML representation for a wildcard declaration
 * schema component is an &lt;any&gt; or &lt;anyAttribute&gt; element information item
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
public class XSWildcardDecl implements XSWildcard {

    public static final String ABSENT = null;

    // the type of wildcard: any, other, or list
    public short fType = NSCONSTRAINT_ANY;
    // the type of process contents: strict, lax, or skip
    public short fProcessContents = PC_STRICT;
    // the namespace list:
    // for NSCONSTRAINT_LIST, it means one of the namespaces in the list
    // for NSCONSTRAINT_NOT, it means not any of the namespaces in the list
    public String[] fNamespaceList;

    // optional annotation
    public XSObjectList fAnnotations = null;

    /*
     * XML Schema 1.1
     */
    // disallowed names list
    public QName[] fDisallowedNamesList = null;
    // ##defined
    public boolean fDisallowedDefined = false;
    // ##definedSibling
    public boolean fDisallowedSibling = false;

    // I'm trying to implement the following constraint exactly as what the
    // spec describes. Sometimes it seems redundant, and sometimes there seems
    // to be much easier solutions. But it makes it easy to understand,
    // easy to maintain, and easy to find a bug (either in the code, or in the
    // spec). -SG
    //
    // NOTE: Schema spec only requires that ##other not(tNS,absent).
    //       The way we store ##other is not(NS1,NS2,...,NSN), which covers
    //       what's required by Schema, and allows future enhanced features.
    //
    // In the following in-line comments:
    // - Bullet removed from w3c specification.
    // + Bullet added as proposed by Sandy Gao, IBM.
    // / Since we store ##other as not(NS1,NS2,...,NSN), we need to put some
    //   comments on where we didn't follow the spec exactly.
    // * When we really support not(NS1,NS2,...,NSN), we need to revisit these items.

    /**
     * Validation Rule: Wildcard allows Namespace Name
     */
    public boolean allowNamespace(String namespace) {
        // For a value which is either a namespace name or absent to be valid with respect to a wildcard constraint (the value of a {namespace constraint}) one of the following must be true:

        // 1 The constraint must be any.
        if (fType == NSCONSTRAINT_ANY)
            return true;

        // 2 All of the following must be true:
        // 2.1 The constraint is a pair of not and a namespace name or absent ([Definition:]  call this the namespace test).
        // 2.2 The value must not be identical to the namespace test.
        // 2.3 The value must not be absent.
        // / we store ##other as not(list), so our actual rule is
        // / 2 The constraint is a pair of not and a set, and the value is not in such set.
        if (fType == NSCONSTRAINT_NOT) {
            boolean found = false;
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum && !found; i++) {
                if (namespace == fNamespaceList[i])
                    found = true;
            }

            if (!found)
                return true;
        }

        // 3 The constraint is a set, and the value is identical to one of the members of the set.
        if (fType == NSCONSTRAINT_LIST) {
            int listNum = fNamespaceList.length;
            for (int i = 0; i < listNum; i++) {
                if (namespace == fNamespaceList[i])
                    return true;
            }
        }

        // none of the above conditions applied, so return false.
        return false;
    }

    /**
     * XML Schema 1.1
     * 
     * Validation Rule: Wildcard allows Name
     */
    final public boolean allowQName(QName name) {
        return allowName(name.uri, name.localpart);
    }
    
    final public boolean allowName(String uri, String localPart) {
        // 1 The namespace name is valid with respect to C, as defined in Wildcard allows Namespace Name (3.10.4.3);
        // 2 C.{disallowed names} does not contain E.
        if (allowNamespace(uri)) {
            if (fDisallowedNamesList == null || fDisallowedNamesList.length == 0) {
                return true;
            }
            return isNameAllowed(uri, localPart);
        }

        // failed
        return false;
    }

    private boolean isNameAllowed(String namespace, String localpart) {
        for (int i=0; i < fDisallowedNamesList.length; i++) {
            if (fDisallowedNamesList[i].uri == namespace && fDisallowedNamesList[i].localpart == localpart) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether this wildcard has a weaker process contents than the super.
     */
    public boolean weakerProcessContents(XSWildcardDecl superWildcard) {
        return fProcessContents == XSWildcardDecl.PC_LAX &&
               superWildcard.fProcessContents == XSWildcardDecl.PC_STRICT ||
               fProcessContents == XSWildcardDecl.PC_SKIP &&
               superWildcard.fProcessContents != XSWildcardDecl.PC_SKIP;
    }

    /**
     * get the string description of this wildcard
     */
    private String fDescription = null;
    public String toString() {
        if (fDescription == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("WC[");
            switch (fType) {
            case NSCONSTRAINT_ANY:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDANY);
                break;
            case NSCONSTRAINT_NOT:
                buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDOTHER);
                buffer.append(':');
                // fall through
            case NSCONSTRAINT_LIST:
                if (fNamespaceList.length == 0)
                    break;
                buffer.append("\"");
                if (fNamespaceList[0] != null)
                    buffer.append(fNamespaceList[0]);
                buffer.append("\"");
                for (int i = 1; i < fNamespaceList.length; i++) {
                    buffer.append(",\"");
                    if (fNamespaceList[i] != null)
                        buffer.append(fNamespaceList[i]);
                    buffer.append("\"");
                }
                break;
            }
            if (fDisallowedNamesList != null) {
                buffer.append(", notQName(");
                if (fDisallowedNamesList.length > 0) {
                    buffer.append(fDisallowedNamesList[0]);
                    for (int i = 1; i < fDisallowedNamesList.length; i++) {
                        buffer.append(", ");
                        buffer.append(fDisallowedNamesList[i]);
                    }
                }
                if (fDisallowedDefined) {
                    buffer.append(", ");
                    buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDDEFINED);
                }
                if (fDisallowedSibling) {
                    buffer.append(", ");
                    buffer.append(SchemaSymbols.ATTVAL_TWOPOUNDDEFINEDSIBLING);
                }
                buffer.append(')');
            }

            buffer.append(']');
            fDescription = buffer.toString();
        }

        return fDescription;
    }
    
    /**
     * Get the type of the object, i.e ELEMENT_DECLARATION.
     */
    public short getType() {
        return XSConstants.WILDCARD;
    }

    /**
     * The <code>name</code> of this <code>XSObject</code> depending on the
     * <code>XSObject</code> type.
     */
    public String getName() {
        return null;
    }

    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.  defines how a namespace URI is attached to schema
     * components.
     */
    public String getNamespace() {
        return null;
    }

    /**
     * Namespace constraint: A constraint type: any, not, list.
     */
    public short getConstraintType() {
        return fType;
    }

    /**
     * Namespace constraint. For <code>constraintType</code>
     * LIST_NSCONSTRAINT, the list contains allowed namespaces. For
     * <code>constraintType</code> NOT_NSCONSTRAINT, the list contains
     * disallowed namespaces.
     */
    public StringList getNsConstraintList() {
        return new StringListImpl(fNamespaceList, fNamespaceList == null ? 0 : fNamespaceList.length);
    }

    /**
     * {process contents} One of skip, lax or strict. Valid constants values
     * are: PC_SKIP, PC_LAX, PC_STRICT.
     */
    public short getProcessContents() {
        return fProcessContents;
    }

    /**
     * String valid of {process contents}. One of "skip", "lax" or "strict".
     */
    public String getProcessContentsAsString() {
        switch (fProcessContents) {
            case XSWildcardDecl.PC_SKIP: return "skip";
            case XSWildcardDecl.PC_LAX: return "lax";
            case XSWildcardDecl.PC_STRICT: return "strict";
            default: return "invalid value";
        }
    }

    /**
     * Optional. Annotation.
     */
    public XSAnnotation getAnnotation() {
        return (fAnnotations != null) ? (XSAnnotation) fAnnotations.item(0) : null;
    }

    /**
     * Optional. Annotations.
     */
    public XSObjectList getAnnotations() {
        return (fAnnotations != null) ? fAnnotations : XSObjectListImpl.EMPTY_LIST;
    }

    /**
     * @see org.apache.xerces.xs.XSObject#getNamespaceItem()
     */
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

} // class XSWildcardDecl
