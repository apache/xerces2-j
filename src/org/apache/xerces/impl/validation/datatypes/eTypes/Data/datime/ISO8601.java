/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights
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


package org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime;


import org.apache.xerces.impl.validation.datatypes.regex.*;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Implements ISO8601 data standard.
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class ISO8601 extends ISODateTime {

   final static int START = 0, STOP = 1;
   ISOPeriod period = new ISOPeriod();
   ISODateTime[] ss = new ISODateTime[2];
   ISODateTime otherDateTime = new ISODateTime();
   Match splitMatch;

   public ISO8601(){ 
      super();
      setName("ISO8601"); 
   }
   public static void main(String[] args) throws FileNotFoundException, IOException {
      // Insert code to start the application here.
      if (args == null || args.length == 0) {
         args = new String[] {"/home/berman/perl/XML/date/ex.period", 
            "/home/berman/perl/XML/date/ex.dt"};
      }
      int i;
      ISO8601 iso = new ISO8601();
      /* TODO replace FileStringRW.
FileStringRW fsrw = new FileStringRW();
for (i = 0; i < args.length; i++) {
fsrw.clear();
fsrw.setFile(new String[] {args[i]});
fsrw.read(0);
String contents = fsrw.getContents();
StringTokenizer tok = new StringTokenizer(contents);
while (tok.hasMoreElements()) {
String str = (String) tok.nextElement();
if (!iso.validate(str)) {
    System.err.println(">>>   " + str + " not valid ISO8601\n");
}
}
}
      */
   }
   public boolean validate(Object obj) {
      String str = ( String ) obj;        
      int i;
      splitMatch = new Match();
      if (ISO8601Pattern.splitRE.matches(str, splitMatch)) {
         // the string is a pair of dates
         String[] s = new String[] {TimeJRep.getField(splitMatch,1), TimeJRep.getField(splitMatch,2)};
         for (i = 0; i < 2; i++) {
            if (s[i] == null || s[i].length() == 0) {
               continue;
            }
            if (s[i].indexOf('P') >= 0) {
               if (!period.validate(s[i])) {
                  return false;
               }
            } else {
               ss[i] = ( i == 0 ) ? this : otherDateTime ;
               if (! ss[i] . validate(s[i])) {
                  return false;
                  //											throw new TypeException("Argument to ISO8601.validate("+str+" contains invalid datetime ");
               }
            }
         }
         return true;
      }
      return super.validate(str);
   }
}
