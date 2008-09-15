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

package org.apache.xerces.xs;

/**
 * This interface represents an openContent.
 * 
 * @author Khaled Noaman, IBM
 * @version $Id$
 */
public interface XSOpenContent extends XSObject {

	/**
     * Mode type
     */
    public static final short MODE_NONE       = 0;
    public static final short MODE_INTERLEAVE = 1;
    public static final short MODE_SUFFIX     = 2;

    /**
     * A mode type: none, interleave, suffix. 
     */
    public short getModeType();

    /**
     * A wildcard declaration
     */
    public XSWildcard getWildcard();
    
    /**
     * A flag that indicates whether a default open content is applied
     * when the content type of a complex type declaration is empty
     */
    // TODO: do we have two different implementations (i.e. XSOenContentDecl and XSDefaultOpenContentDecl)
    //       and add that method only to XSDefaultOpenContentDecl
    public boolean appliesToEmpty();

}
