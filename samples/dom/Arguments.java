/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

package dom;

import java.util.Stack;
import java.util.EmptyStackException;
import java.lang.Integer;
import java.util.Vector;




/**
 * Utility like Unix getopt.
 * 
 * Usage:
 * 
 * int c;
 * 
 *         parseArgumentTokens(argv);
 *         while ( (c =  getArguments()) != -1 ){
 * 
 *          switch (c) {
 *             case 'v':
 *                 System.out.println( "v" );
 *                 break;
 *             case 'V':
 *                 System.out.println( "V" );
 *                 break;
 * 
 * @author Jeffrey Rodriguez
 */

public class Arguments {
    private  Stack        stackOfOptions = new Stack();
    private  Stack        argumentList   = new Stack();
    private  String[]     messageArray   = null; 
    private  int          lastPopArgument = 0;

    public Arguments() {
      stackOfOptions.push( new Integer( -1 ) );// First Element to push in Stack

    }

    public void    parseArgumentTokens(  String[] arguments ){
        int  theDash         = 0;
        int  lengthOfToken   = 0;
        char []bufferOfToken = null;
        for ( int i = 0; i<arguments.length; i++ ){
            bufferOfToken = arguments[i].toCharArray();
            lengthOfToken = bufferOfToken.length;
            if ( bufferOfToken[0] == '-' ){
                for ( int j = 1; j<lengthOfToken; j++ ){
                    stackOfOptions.push( (Object ) new Integer(bufferOfToken[j] ));
                }
            } else{
                argumentList.addElement( arguments[i] ); 
            }
        }
    }

    /**
     * 
     *              Returns the list of argument switches 
     *              used by Arguments class.
     * 
     * @return 
     */
    public  String[] getArgumentTokens( ) {
        //return ((Integer ) stackOfOptions.push());
        return null;
    }


    /**
     * 
     * @return 
     */
    public  int getArguments(){
        Integer i;
        try {
            i = (Integer ) stackOfOptions.pop();
            lastPopArgument = i.intValue();
        } catch ( EmptyStackException ex ) {
            return -1;
        }
        return lastPopArgument;
    }

    

    /**
     * 
     * @return 
     */
    public String getStringParameter(){
        String s = null;
        try {
            s = (String) argumentList.pop();
        } catch ( EmptyStackException ex ) {
            System.out.println("missing parameter for argument -" +  (char )lastPopArgument );
        }
        return s;
    }

    public int stringParameterLeft( ){
        return argumentList.size();
    }


    public void setUsage( String[] message ){ 
        messageArray = message;
    }

    public void printUsage() {
        for( int i = 0; i< messageArray.length; i++ ){
            System.err.println( messageArray[i] );
        }
    }




    public static void main( String[] argv){
        int c;

        Arguments tst = new Arguments();

        tst.setUsage( new String[] {  "usage: java dom.DOMCount (options) uri ...",
                  "",
                  "options:",
                  "  -p name  Specify DOM parser wrapper by name.",
                  "           Default parser: ",
                  "  -h       This help screen." } );
 
        tst.parseArgumentTokens(argv);
        while ( (c =  tst.getArguments()) != -1 ){
            switch (c) {
            case 'v':
                System.out.println( "v" );
                break;
            case 'V':
                System.out.println( "V" );
                break;
            case 'N':
                System.out.println( "N" );
                break;
            case 'n':
                System.out.println( "n" );
                break;
            case 'p':
                System.out.println( "p  = " + tst.getStringParameter() );
                break;
            case 'd':
                System.out.println( "d" );
                break;
            case 'D':
                System.out.println( "D" );
                break;
            case 's':
                System.out.println( "s" );
                break;
            case 'S':
                System.out.println( "S" );
                break;
            case '?':
            case 'h':
            case '-':
                tst.printUsage();
                break;
            case 'e':
                System.out.println( "e  = " + tst.getStringParameter() );
                break;
            default:
                break;
            }
        }
    }



}
