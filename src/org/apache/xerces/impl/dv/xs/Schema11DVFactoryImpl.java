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

package org.apache.xerces.impl.dv.xs;

import org.apache.xerces.impl.dv.XSFacets;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.util.SymbolHash;

/**
 * A special factory to create/return built-in schema DVs and create user-defined DVs
 * that includes anyAtomicType, yearMonthDuration and dayTimeDuration
 * 
 * @xerces.internal 
 *
 * @author Khaled Noaman, IBM
 *
 * @version $Id$
 */
public class Schema11DVFactoryImpl extends BaseSchemaDVFactory {

    static SymbolHash fBuiltInTypes = new SymbolHash();
    static {
        createBuiltInTypes();
    }
    
    // create all built-in types
    static void createBuiltInTypes() {
        final String ANYATOMICTYPE     = "anyAtomicType";
        final String DURATION          = "duration";
        final String YEARMONTHDURATION = "yearMonthDuration";
        final String DAYTIMEDURATION   = "dayTimeDuration";
        final String PRECISIONDECIMAL  = "precisionDecimal";
        final String ERROR             = "error";
        final String DATETIMESTAMP     = "dateTimeStamp"; 
        final String DATETIME          = "dateTime";
        final String ENTITIES          = "ENTITIES";
        final String ENTITY            = "ENTITY";
        final String NMTOKENS          = "NMTOKENS";
        final String NMTOKEN           = "NMTOKEN";
        final String IDREFS            = "IDREFS";
        final String IDREF             = "IDREF";
        
    	createBuiltInTypes(fBuiltInTypes, XSSimpleTypeDecl.fAnyAtomicType);

        // add anyAtomicType
        fBuiltInTypes.put(ANYATOMICTYPE, XSSimpleTypeDecl.fAnyAtomicType);
        
        // add error
        fBuiltInTypes.put(ERROR, XSSimpleTypeDecl.fError);

        // In XML Schema 1.1, ENTITIES, NMTOKENS, IDREFS have anySimpleType
        // as the base type
        final XSFacets facets = new XSFacets();
        facets.minLength = 1;
        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;

        // add ENTITIES
        final XSSimpleTypeDecl entityDV = (XSSimpleTypeDecl)fBuiltInTypes.get(ENTITY);
        final XSSimpleTypeDecl entitiesDV = new XSSimpleTypeDecl(ENTITIES, URI_SCHEMAFORSCHEMA, (short)0, entityDV, false, null);
        entitiesDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH | XSSimpleType.FACET_WHITESPACE, (short)0);
        fBuiltInTypes.put(ENTITIES, entitiesDV);

        // add NMTOKENS
        final XSSimpleTypeDecl nmtokenDV = (XSSimpleTypeDecl)fBuiltInTypes.get(NMTOKEN);
        final XSSimpleTypeDecl nmtokensDV = new XSSimpleTypeDecl(NMTOKENS, URI_SCHEMAFORSCHEMA, (short)0, nmtokenDV, false, null);
        nmtokensDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH  | XSSimpleType.FACET_WHITESPACE, (short)0);
        fBuiltInTypes.put(NMTOKENS, nmtokensDV);

        // add IDREFS
        final XSSimpleTypeDecl idrefDV = (XSSimpleTypeDecl)fBuiltInTypes.get(IDREF);
        final XSSimpleTypeDecl idrefsDV = new XSSimpleTypeDecl(IDREFS, URI_SCHEMAFORSCHEMA, (short)0, idrefDV, false, null);
        idrefsDV.applyFacets1(facets, XSSimpleType.FACET_MINLENGTH  | XSSimpleType.FACET_WHITESPACE, (short)0);
        fBuiltInTypes.put(IDREFS, idrefsDV);

        // add 2 duration types
        XSSimpleTypeDecl durationDV = (XSSimpleTypeDecl)fBuiltInTypes.get(DURATION);
        fBuiltInTypes.put(YEARMONTHDURATION, new XSSimpleTypeDecl(durationDV, YEARMONTHDURATION, XSSimpleTypeDecl.DV_YEARMONTHDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.YEARMONTHDURATION_DT));
        fBuiltInTypes.put(DAYTIMEDURATION, new XSSimpleTypeDecl(durationDV, DAYTIMEDURATION, XSSimpleTypeDecl.DV_DAYTIMEDURATION, XSSimpleType.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.DAYTIMEDURATION_DT));
        
        //add dateTimeStamp
        XSSimpleTypeDecl dateTimeDV = (XSSimpleTypeDecl)fBuiltInTypes.get(DATETIME);
        fBuiltInTypes.put(DATETIMESTAMP, new XSSimpleTypeDecl(dateTimeDV, DATETIMESTAMP, XSSimpleTypeDecl.DV_DATETIMESTAMP, XSSimpleTypeDecl.ORDERED_PARTIAL, false, false, false, true, XSSimpleTypeDecl.DATETIMESTAMP_DT));
                
        // add precision decimal
        fBuiltInTypes.put(PRECISIONDECIMAL, new XSSimpleTypeDecl(XSSimpleTypeDecl.fAnyAtomicType, PRECISIONDECIMAL, XSSimpleTypeDecl.DV_PRECISIONDECIMAL, XSSimpleType.ORDERED_PARTIAL, false, false, true, true, XSSimpleTypeDecl.PRECISIONDECIMAL_DT));
    } //createBuiltInTypes()

    /**
     * Get a built-in simple type of the given name
     * REVISIT: its still not decided within the Schema WG how to define the
     *          ur-types and if all simple types should be derived from a
     *          complex type, so as of now we ignore the fact that anySimpleType
     *          is derived from anyType, and pass 'null' as the base of
     *          anySimpleType. It needs to be changed as per the decision taken.
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public XSSimpleType getBuiltInType(String name) {
        return (XSSimpleType)fBuiltInTypes.get(name);
    }

    /**
     * get all built-in simple types, which are stored in a hashtable keyed by
     * the name
     *
     * @return      a hashtable which contains all built-in simple types
     */
    public SymbolHash getBuiltInTypes() {
        return (SymbolHash)fBuiltInTypes.makeClone();
    }
}
