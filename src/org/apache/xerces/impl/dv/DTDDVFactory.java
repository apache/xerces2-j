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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.dv;

import java.util.Hashtable;

/**
 * The factory to create and return DTD types. The implementation should
 * store the created datatypes in static data, so that they can be shared by
 * multiple parser instance, and multiple threads.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public abstract class DTDDVFactory {

    private static final String DEFAULT_FACTORY_CLASS = "org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl";

    private static String       fFactoryClass    = null;
    private static DTDDVFactory fFactoryInstance = null;

    /**
     * Set the class name of the dtd factory implementation. This method
     * can only be called before the first time the method <code>getInstance</code>
     * successfully returns, otherwise a DVFactoryException will be thrown.
     *
     * @param className  the class name of the DTDDVFactory implementation
     * @exception DVFactoryException  the method cannot be called at this time
     */
    public static final void setFactoryClass(String factoryClass) throws DVFactoryException {
        // if the factory instance has been created, it's an error.
        if (fFactoryInstance != null)
            throw new DVFactoryException("Cannot set the class name now. The class name '"+fFactoryClass+"' is already used.");

        // synchronize on the string value
        synchronized (DEFAULT_FACTORY_CLASS) {
            // in case this thread was waiting for another thread
            if (fFactoryInstance != null)
                throw new DVFactoryException("Cannot set the class name now. The class name '"+fFactoryClass+"' is already used.");

            fFactoryClass = factoryClass;
        }
    }

    /**
     * Get an instance of DTDDVFactory implementation.
     *
     * If <code>setFactoryClass</code> is called before this method,
     * the passed-in class name will be used to create the factory instance.
     * Otherwise, a default implementation is used.
     *
     * After the first time this method successfully returns, any subsequent
     * invocation to this method returns the same instance.
     *
     * @return  an instance of DTDDVFactory implementation
     * @exception DVFactoryException  cannot create an instance of the specified
     *                                class name or the default class name
     */
    public static final DTDDVFactory getInstance() throws DVFactoryException {
        // if the factory instance has been created, just return it.
        if (fFactoryInstance != null)
            return fFactoryInstance;

        // synchronize on the string value, to make sure that we don't create
        // two instance of the dv factory class
        synchronized (DEFAULT_FACTORY_CLASS) {
            // in case this thread was waiting for another thread to create
            // the factory instance, just return the instance created by the
            // other thread.
            if (fFactoryInstance != null)
                return fFactoryInstance;

            try {
                // if the class name is not specified, use the default one
                if (fFactoryClass == null)
                    fFactoryClass = DEFAULT_FACTORY_CLASS;
                fFactoryInstance = (DTDDVFactory)(Class.forName(fFactoryClass).newInstance());
            } catch (ClassNotFoundException e1) {
                throw new DVFactoryException("DTD factory class " + fFactoryClass + " not found.");
            } catch (IllegalAccessException e2) {
                throw new DVFactoryException("DTD factory class " + fFactoryClass + " found but cannot be loaded.");
            } catch (InstantiationException e3) {
                throw new DVFactoryException("DTD factory class " + fFactoryClass + " loaded but cannot be instantiated (no empty public constructor?).");
            } catch (ClassCastException e4) {
                throw new DVFactoryException("DTD factory class " + fFactoryClass + " does not extend from DTDDVFactory.");
            }
        }

        // return the newly created dv factory instance
        return fFactoryInstance;
    }

    // can't create a new object of this class
    protected DTDDVFactory(){}

    /**
     * return a dtd type of the given name
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public abstract DatatypeValidator getBuiltInDV(String name);

    /**
     * get all built-in DVs, which are stored in a hashtable keyed by the name
     *
     * @return      a hashtable which contains all datatypes
     */
    public abstract Hashtable getBuiltInTypes();

}
