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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.datatype;

import java.util.Hashtable;


/* $Id$ */

public class DurationDatatypeValidator extends DateTimeValidator {

    //1903-03-01T00:00:00Z 
    private final int[] constTime= {1903, 3, 1, 0, 0, 0, 0, 'Z'};    

    public  DurationDatatypeValidator() throws InvalidDatatypeFacetException{
        super();
    }
    public  DurationDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                                        boolean derivedByList ) throws InvalidDatatypeFacetException {

        super(base, facets, derivedByList);
    }


    /**
     * Parses, validates and computes normalized version of duration object
     * 
     * @param str    The lexical representation of duration object PnYn MnDTnH nMnS 
     * @param date   uninitialized date object
     * @return normalized date representation
     * @exception Exception Invalid lexical representation
     */
    protected int[] parse(String str, int[] date) throws Exception{

        //PnYn MnDTnH nMnS: -P1Y2M3DT10H30M        
        resetBuffer(str);

        //create structure to hold an object
        if ( date== null ) {
            date=new int[TOTAL_SIZE];
        }
        date = resetDateObj(date);


        char c=fBuffer.charAt(fStart++);
        if ( c!='P' && c!='-' ) {
            throw new Exception();
        }
        else {
            date[utc]=(c=='-')?'-':0;
            if ( c=='-' && fBuffer.charAt(fStart++)!='P' ) {
                throw new Exception();
            }
        }

        //at least one number and designator must be seen after P
        boolean designator = false;

        //find 'Y'        
        int end = indexOf (fStart, fEnd, 'Y');
        if ( end!=-1 ) {
            //scan year
            date[CY]=parseInt(fStart,end);
            fStart = end+1;
            designator = true;
        }

        end = indexOf (fStart, fEnd, 'M');
        if ( end!=-1 ) {
            //scan month
            date[M]=parseInt(fStart,end);
            fStart = end+1;
            designator = true;
        }

        end = indexOf (fStart, fEnd, 'D');
        if ( end!=-1 ) {
            //scan day
            date[D]=parseInt(fStart,end);
            designator = true;
        }

        end = indexOf (fStart, fEnd, 'T'); 
        if ( end != -1 ) {

            //scan hours, minutes, seconds
            //REVISIT: can any item include a decimal fraction or only seconds?
            //

            if ( (end+1) == fEnd ) {
                //P01Y01M01DT
                throw new Exception();
            }



            fStart = end+1;
            end = indexOf (fStart, fEnd, 'H');
            if ( end!=-1 ) {
                //scan hours
                date[h]=parseInt(fStart,end);
                fStart=end+1;
                designator = true;
            }


            end = indexOf (fStart, fEnd, 'M');
            if ( end!=-1 ) {
                //scan min
                date[m]=parseInt(fStart,end);
                fStart=end+1;
                designator = true;
            }


            end = indexOf (fStart, fEnd, 'S');
            if ( end!=-1 ) {
                //scan seconds
                date[s]=parseInt(fStart,end);
                designator = true;
            }
        }
        if ( !designator ) {
            throw new Exception();
        }
        //REVISIT:  add error checking for some digits/chars in the end..?
        //          should pattern take care of that?

        return date;
    }


    //REVISIT: implement addDuration algorithm
    private int[] addDuration(int[] duration) {
        return duration;
    }
}


