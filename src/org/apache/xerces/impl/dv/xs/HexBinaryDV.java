/*
 * Copyright 2001, 2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.util.HexBin;

/**
 * Represent the schema type "hexBinary"
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class HexBinaryDV extends TypeValidator {

    public short getAllowedFacets(){
        return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE );
    }

    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        byte[] decoded = HexBin.decode(content);
        if (decoded == null)
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "hexBinary"});

        return new XHex(decoded);
    }

    // length of a binary type is the number of bytes
    public int getDataLength(Object value) {
        return ((XHex)value).length();
    }

    private static final class XHex {
        // actually data stored in a byte array
        final byte[] data;
        // canonical representation of the data
        private String canonical;
        public XHex(byte[] data) {
            this.data = data;
        }
        public synchronized String toString() {
            if (canonical == null) {
                canonical = HexBin.encode(data);
            }
            return canonical;
        }
        public int length() {
            return data.length;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof XHex))
                return false;
            byte[] odata = ((XHex)obj).data;
            int len = data.length;
            if (len != odata.length)
                return false;
            for (int i = 0; i < len; i++) {
                if (data[i] != odata[i])
                    return false;
            }
            return true;
        }
    }
}
