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

package org.apache.xerces.impl.dv.dtd;

import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.dv.DatatypeValidator;
import java.util.Hashtable;

/**
 * the factory to create/return built-in schema DVs and create user-defined DVs
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class DTDDVFactoryImpl extends DTDDVFactory {

    static Hashtable fBuiltInTypes = null;

    /**
     * return a dtd type of the given name
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public DatatypeValidator getBuiltInDV(String name) {
        prepareBuiltInTypes();
        return (DatatypeValidator)fBuiltInTypes.get(name);
    }

    /**
     * get all built-in DVs, which are stored in a hashtable keyed by the name
     *
     * @return      a hashtable which contains all datatypes
     */
    public Hashtable getBuiltInTypes() {
        prepareBuiltInTypes();
        return (Hashtable)fBuiltInTypes.clone();
    }

    // make sure the built-in types are created
    // the types are supposed to be reused for all factory objects,
    // so we synchorinize on the class object.
    static void prepareBuiltInTypes() {
        if (fBuiltInTypes == null) {
            createBuiltInTypes();
        }
    }

    // create all built-in types
    // we are assumeing that fBuiltInTypes == null
    static synchronized void createBuiltInTypes() {

        if (fBuiltInTypes != null)
            return;

        Hashtable types = new Hashtable();
        DatatypeValidator dvTemp;

        types.put("string", new StringDatatypeValidator());
        types.put("ID", new IDDatatypeValidator());
        dvTemp = new IDREFDatatypeValidator();
        types.put("IDREF", dvTemp);
        types.put("IDREFS", new ListDatatypeValidator(dvTemp));
        dvTemp = new ENTITYDatatypeValidator();
        types.put("ENTITY", new ENTITYDatatypeValidator());
        types.put("ENTITIES", new ListDatatypeValidator(dvTemp));
        types.put("NOTATION", new NOTATIONDatatypeValidator());
        dvTemp = new NMTOKENDatatypeValidator();
        types.put("NMTOKEN", dvTemp);
        types.put("NMTOKENS", new ListDatatypeValidator(dvTemp));

        fBuiltInTypes = types;
    }//createBuiltInTypes()

}//SchemaDVFactory
