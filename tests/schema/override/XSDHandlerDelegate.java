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

package schema.override;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
class XSHandlerDelegate extends XSDHandler {
    
    private XMLErrorReporter fErrorReporter = new XMLErrorReporter();

    XSHandlerDelegate(short schemaVersion, XSConstraints xsConstraints) {
        super(schemaVersion, xsConstraints);
    }

    public void reportSchemaError(String key, Object[] args, Element ele) {
        fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                key, args, XMLErrorReporter.SEVERITY_ERROR, null);
    }
}
