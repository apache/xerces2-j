/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000  The Apache Software Foundation.  All rights 
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



package util;

import java.lang.Integer;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;



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
    private  boolean      fDbug          = false;
    private  Queue        stackOfOptions = new Queue();

    private  Queue        argumentList   = new Queue();
    private  Queue        listOfFiles    = new Queue();
    private  String[]     messageArray   = null; 
    private  int          lastPopArgument = 0;


    public Arguments() {
        stackOfOptions.push( new Integer( -1 ) );// First Element to push in Stack

    }

    public void    parseArgumentTokens(  String[] arguments , char[] argsWithOptions ){
        int  theDash         = 0;
        int  lengthOfToken   = 0;
        char []bufferOfToken = null;
        Object[] temp;

        outer:
        for ( int i = 0; i<arguments.length; i++ ){
            bufferOfToken = arguments[i].toCharArray();
            lengthOfToken = bufferOfToken.length;
            if ( bufferOfToken[0] == '-' ){
                int   token;
                for ( int j = 1; j<lengthOfToken; j++ ){
                    token = bufferOfToken[j];
                    stackOfOptions.push( (Object ) new Integer( token ));
                    for ( int k = 0; k< argsWithOptions.length; k++) {
                        if ( token == argsWithOptions[k] ){
                            if ( this.fDbug  ) {
                                System.out.println( "token = " + token );
                            }
                            //stackOfOptions.push( (Object ) new Integer( -1 ));
                            argumentList.push( arguments[++i] );
                            continue outer;
                        }
                    }

                }
                stackOfOptions.push( (Object ) new Integer( -1 ));

            } else{
                //for ( int j = 0; j< argsWithOptions.length; j++) {
                //  if( bufferOfToken[i] == argsWithOptions[j] ){
                //    argumentList.push( arguments[i] );
                //}
                // }
                listOfFiles.push( arguments[i] );
            }
        }


        if ( this.fDbug ) {
            stackOfOptions.print();
            argumentList.print();
            listOfFiles.print();
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
        if ( stackOfOptions.empty() ){
            i = (Integer ) stackOfOptions.pop();
            lastPopArgument = i.intValue();
        } else
            lastPopArgument = -1;
        return lastPopArgument;
    }



    /**
     * 
     * @return 
     */
    public String getStringParameter(){
        String    s = (String) argumentList.pop();
        if ( this.fDbug )  {
                System.out.println( "string par = " + s );
            }
        return s;
    }


    public String getlistFiles(){
        String    s = null;
        try {
          s = (String) listOfFiles.pop(); 
        } catch( NoSuchElementException ex ){
            ;
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
        for ( int i = 0; i< messageArray.length; i++ ){
            System.err.println( messageArray[i] );
        }
    }

/*


    public static void main( String[] argv){
        int c;

        Arguments tst = new Arguments();

        tst.setUsage( new String[] {  "usage: java dom.DOMCount (options) uri ...",
                          "",
                          "options:",
                          "  -p name  Specify DOM parser wrapper by name.",
                          "           Default parser: ",
                          "  -h       This help screen."} );

        tst.parseArgumentTokens(argv , new char[] { 'e'});
        while ( (c =  tst.getArguments()) != -1 ){
            switch (c) {
            case 'e':
                System.out.println( "e  = " + tst.getStringParameter() );
                break;

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
            default:
                break;
            }
        }
    }
*/
    // Private methods

    // Private inner classes

    public  class Queue   {
        private LinkedList queue;
        public Queue() {
            queue = new LinkedList();
        }
        public void push( Object token ) {
            queue.addLast( token );
        }
        public Object pop() {
            Object token = queue.removeFirst(); 
            return token; 
        }
        public boolean empty(){
            return queue.isEmpty();
        }

        public int size(){
            return queue.size();
        }

        public void clear(){
            queue.clear();
        }


        public void print(){
            System.out.println("we are here " );
            ListIterator it          =  queue.listIterator();
            int      tokenNumber = 0;
            while ( it.hasNext()  ){
                System.out.println( "token[ " +  tokenNumber++
                                    + "] = " +   ( it.next().toString()));
            }
        }

    }
}
