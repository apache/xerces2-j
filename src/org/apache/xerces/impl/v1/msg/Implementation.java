/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights 
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
package org.apache.xerces.impl.v1.msg;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ListResourceBundle;

/**
 * ImplementationMessages provides messages internal to the parser implementation
 *
 * @version
 */
public class Implementation implements XMLMessageProvider {
    /**
     * The domain of messages concerning the Xerces implementation.
     */
    public static final String XERCES_IMPLEMENTATION_DOMAIN = "http://www.apache.org/xml/xerces.html";

    /**
     *
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
    /**
     *
     */
    public Locale getLocale() {
        return fLocale;
    }

    /**
     * Creates a message from the specified key and replacement
     * arguments, localized to the given locale.
     *
     * @param locale    The requested locale of the message to be
     *                  created.
     * @param key       The key for the message text.
     * @param args      The arguments to be used as replacement text
     *                  in the message created.
     */
    public String createMessage(Locale locale, int majorCode, int minorCode, Object args[]) {
        boolean throwex = false;
        if (fResourceBundle == null || locale != fLocale) {
            if (locale != null)
                fResourceBundle = ListResourceBundle.getBundle("org.apache.xerces.msg.ImplementationMessages", locale);
            if (fResourceBundle == null)
                fResourceBundle = ListResourceBundle.getBundle("org.apache.xerces.msg.ImplementationMessages");
        }
        if (majorCode < 0 || majorCode >= fgMessageKeys.length - 1) {
            majorCode = BAD_MAJORCODE;
            throwex = true;
        }
        String msgKey = fgMessageKeys[majorCode];
        String msg = fResourceBundle.getString(msgKey);
        if (args != null) {
            try {
                msg = java.text.MessageFormat.format(msg, args);
            } catch (Exception e) {
                msg = fResourceBundle.getString(fgMessageKeys[FORMAT_FAILED]);
                msg += " " + fResourceBundle.getString(msgKey);
            }
        }
        if (throwex) {
            throw new RuntimeException(msg);
        }
        return msg;
    }
    //
    //
    //
    private Locale fLocale = null;
    private ResourceBundle fResourceBundle = null;
    //
    //
    //
    public static final int
        BAD_MAJORCODE = 0,  //  majorCode parameter to createMessage was out of bounds
        ENC4 = 1,           // "Invalid UTF-8 code. (byte: 0x{0})"
        ENC5 = 2,           // "Invalid UTF-8 code. (bytes: 0x{0} 0x{1})"
        ENC6 = 3,           // "Invalid UTF-8 code. (bytes: 0x{0} 0x{1} 0x{2})"
        ENC7 = 4,           // "Invalid UTF-8 code. (bytes: 0x{0} 0x{1} 0x{2} 0x{3})"
        IO0 = 5,            // "File, \"{0}\", not found."
        VAL_BST = 6,        // "Invalid ContentSpecNode.NODE_XXX value for binary op CMNode."
        VAL_CMSI = 7,       // "Invalid CMStateSet bit index."
        VAL_CST = 8,        // "Unknown ContentSpecNode.NODE_XXX value."
        VAL_LST = 9,        // "Invalid ContentSpecNode.NODE_XXX value for leaf CMNode."
        VAL_NIICM = 10,     // "Only * unary ops should be in the internal content model tree."
        VAL_NPCD = 11,      // "PCData found in non-mixed model content."
        VAL_UST = 12,       // "Invalid ContentSpecNode.NODE_XXX value for unary op CMNode."
        VAL_WCGHI = 13,     // "Input to whatCanGoHere() is inconsistent."
        INT_DCN = 14,       // "Internal Error: dataChunk == NULL"
        INT_PCN = 15,       // "Internal Error: fPreviousChunk == NULL"
        FATAL_ERROR = 16,   // "Stopping after fatal error: {0}"

        //
        // KEEP THIS AT THE END
        //
        FORMAT_FAILED = 17; // "Internal Error while formatting error message "

    //
    // Table of error code to error key strings.
    //
    private static final String[] fgMessageKeys = {
        "BadMajorCode",         //  0, "The majorCode parameter to createMessage was out of bounds."
        "ENC4",                 //  1, "Invalid UTF-8 code. (byte: 0x{0})"
        "ENC5",                 //  2, "Invalid UTF-8 code. (bytes: 0x{0} 0x{1})"
        "ENC6",                 //  3, "Invalid UTF-8 code. (bytes: 0x{0} 0x{1} 0x{2})"
        "ENC7",                 //  4, "Invalid UTF-8 code. (bytes: 0x{0} 0x{1} 0x{2} 0x{3})"
        "FileNotFound",         //  5, "File, \"{0}\", not found."
        "VAL_BST",              //  6, "Invalid ContentSpecNode.NODE_XXX value for binary op CMNode."
        "VAL_CMSI",             //  7, "Invalid CMStateSet bit index."
        "VAL_CST",              //  8, "Unknown ContentSpecNode.NODE_XXX value."
        "VAL_LST",              //  9, "Invalid ContentSpecNode.NODE_XXX value for leaf CMNode."
        "VAL_NIICM",            // 10, "Only * unary ops should be in the internal content model tree."
        "VAL_NPCD",             // 11, "PCData found in non-mixed model content."
        "VAL_UST",              // 12, "Invalid ContentSpecNode.NODE_XXX value for unary op CMNode."
        "VAL_WCGHI",            // 13, "Input to whatCanGoHere() is inconsistent."
        "INT_DCN",              // 14, "Internal Error: dataChunk == NULL"
        "INT_PCN",              // 15, "Internal Error: fPreviousChunk == NULL"
        "FatalError",           // 16, "Stopping after fatal error: {0}"

        //
        // KEEP THIS AT THE END
        //
        "FormatFailed",         // 17, "An internal error occurred while formatting the following message:"
        null
    };
}
