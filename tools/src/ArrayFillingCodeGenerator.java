/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 
package org.apache.xerces.util;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This class can be used to generate the code for 
 * an efficient initialization for a given array.
 * 
 * @author Michael Glavassevich, IBM
 * 
 * @version $Id$
 */
public class ArrayFillingCodeGenerator {

    /**
     * <p>Generates code for an efficient initialization for
     * a given byte array.</p>
     * 
     * @param arrayName the name of the array to be generated
     * @param array the array for which code will be generated
     * @param out the stream where the code will be written
     */
    public static void generateByteArray(String arrayName, 
                                         byte[] array, 
                                         OutputStream out) {

        PrintWriter writer = new PrintWriter(out);
        int cursor = 0;
        int i;
        byte last = 0;
        for (i = 0; i < array.length; ++i) {
            if (last == array[i]) {
                continue;
            }
            if (i - cursor > 1 && last != (byte) 0) {
                writer.print("Arrays.fill(" + arrayName + ", " + cursor + ", " + i + ", (byte) " + last + " );");
                writer.println(" // Fill " + (i - cursor) + " of value (byte) " + last);
                writer.flush();
            }
            else if (i - cursor == 1 && array[cursor] != (byte) 0) {
                writer.println(arrayName + "[" + cursor + "] = " + array[cursor] + ";");
                writer.flush();
            }
            last = array[i];
            cursor = i;
        }
        if (i - cursor > 1 && last != (byte) 0) {
            writer.print("Arrays.fill(" + arrayName + ", " + cursor + ", " + i + ", (byte) " + last + " );");
            writer.println(" // Fill " + (i - cursor) + " of value (byte) " + last);
            writer.flush();
        }
        else if (i - cursor == 1 && array[cursor] != (byte) 0) {
            writer.println(arrayName + "[" + cursor + "] = " + array[cursor] + ";");
            writer.flush();
        }
        writer.flush();
        writer.close();
    }
}
