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

package org.apache.xerces.impl.validation.datatypes.eTypes.Data;

import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.datatypes.regex.Match;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Vector;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Pieces of a uri are available using getComponent
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class uri extends BasicStringProperty {
   /** names holds names of all the non-terminals occurring in the BNF
    */
   static final String[] names = new String[] {"alpha", "alphanum", "hex", "escaped", "mark", "unreserved", "reserved", "uric", "fragment", "query", "pchar", "param", "segment", "path_segments", "abs_path", "uric_no_slash", "opaque_part", "path", "port", "IPv4address", "toplabel", "domainlabel", "hostname", "host", "hostport", "userinfo", "server", "reg_name", "authority", "scheme", "rel_segment", "rel_path", "net_path", "hier_part", "relativeURI", "absoluteURI", "URIreference"};
   static String $alpha = "[A-z]";
   static String $alphanum = "[A-z0-9]";
   static String $hex = "(?:[0-9A-Fa-f])";
   static String $escaped = "(?:%" + $hex + "" + $hex +")";
   static String $mark = "(?:[-_.!~*')(])";
   static String $unreserved = "(?:(?:\\w|" + $mark +"))";
   static String $reserved = "(?:[;/\\?:@&=\\+$\\,}])";
   static String $uric = "(?:" + $reserved + "|" + $unreserved + "|" + $escaped +")";
   static String $fragment = "(?:" + $uric + "*)";
   static String $query = "(?:" + $uric + "*)";
   static String $pchar = "(?:(?:" + $unreserved + "|" + $escaped + "|:|@|&|=|\\+|$|,))";
   static String $param = "(?:" + $pchar + "*)";
   static String $segment = "(?:" + $pchar + "*(?:;" + $param +")*)";
   static String $path_segments = "(?:" + $segment + "(?:/" + $segment +")*)";
   static String $abs_path = "/" + $path_segments + "";
   static String $uric_no_slash = "(?:" + $unreserved + "|" + $escaped + "|;|\\?|:|@|&|=|\\+|$|,)";
   static String $opaque_part = "(?:" + $uric_no_slash + "" + $uric + "*)";
   static String $path = "(?:" + $abs_path + "|" + $opaque_part +")?";
   static String $port = "(?:\\d*)";
   static String $IPv4address = "(?:\\d+\\.\\d+\\.\\d+\\.\\d+)";
   static String $toplabel = "(?:" + $alpha + "(?:" + $alphanum + "|-)*" + $alphanum +")";
   static String $domainlabel = "(?:" + $alphanum + "(?:" + $alphanum + "|-)*" + $alphanum +")";
   static String $hostname = "(?:(?:" + $domainlabel + "\\.)*" + $toplabel + "\\.?)";
   static String $host = "(?:(?:" + $hostname + "|" + $IPv4address +"))";
   static String $hostport = "(?:" + $host + "(?::" + $port +")?)";
   static String $userinfo = "(?:(?:" + $unreserved + "|" + $escaped + "|;|:|&|=|\\+|$|,)*)";
   static String $server = "(?:(?:(?:" + $userinfo + "@)?" + $hostport +"))";
   static String $reg_name = "(?:(?:" + $unreserved + "|" + $escaped + "|$|,|;|:|@|&|=|\\+)+)";
   static String $authority = "(?:(?:" + $server + "|" + $reg_name +"))";
   static String $scheme = "(?:" + $alpha + "(?:" + $alphanum + "|\\+|-|\\.)*)";
   static String $rel_segment = "(?:(?:" + $unreserved + "|" + $escaped + "|;|@|&|=|\\+|$|,)+)";
   static String $rel_path = "(?:" + $rel_segment + "" + $abs_path + "?)";
   static String $net_path = "(?://" + $authority + "" + $abs_path + "?)";
   static String $hier_part = "(?:(?:" + $net_path + "|" + $abs_path +")(?:\\?" + $query +")?)";
   static String $relativeURI = "(?:(?:" + $net_path + "|" + $abs_path + "|" + $rel_path +")(?:\\?" + $query +")?)";
   static String $absoluteURI = "(?:" + $scheme + ":(?:" + $hier_part + "|" + $opaque_part +"))";
   static String $URIreference = "(?:(?:" + $absoluteURI + "|" + $relativeURI +")?(?:#" + $fragment +")?)";
   static String[] arrayOfPatternStrings = new String[] {$alpha, $alphanum, $hex, $escaped, $mark, $unreserved, $reserved, $uric, $fragment, $query, $pchar, $param, $segment, $path_segments, $abs_path, $uric_no_slash, $opaque_part, $path, $port, $IPv4address, $toplabel, $domainlabel, $hostname, $host, $hostport, $userinfo, $server, $reg_name, $authority, $scheme, $rel_segment, $rel_path, $net_path, $hier_part, $relativeURI, $absoluteURI, $URIreference};
   static RegularExpression uriRE = new RegularExpression($URIreference);
   static Hashtable ht = new Hashtable();

   // For each non-terminal, we construct a string which extracts (using RegularExpression and Match ) the named groups which make up the NT.
   static String $escapedG = "(?:%" + "(" + $hex +")" + "(" + $hex +")" +")";
   static String $unreservedG = "(?:(?:\\w|" + "(" + $mark +")" +"))";
   static String $URIreferenceG = "(?:(?:" + "(" + $absoluteURI +")" + "|" + "(" + $relativeURI +")" +")?(?:#" + "(" + $fragment +")" +")?)";
   static String $absoluteURIG = "(?:" + "(" + $scheme +")" + ":(?:" + "(" + $hier_part +")" + "|" + "(" + $opaque_part +")" +"))";
   static String $relativeURIG = "(?:(?:" + "(" + $net_path +")" + "|" + "(" + $abs_path +")" + "|" + "(" + $rel_path +")" +")(?:\\?" + "(" + $query +")" +")?)";
   static String $hier_partG = "(?:(?:" + "(" + $net_path +")" + "|" + "(" + $abs_path +")" +")(?:\\?" + "(" + $query +")" +")?)";
   static String $net_pathG = "(?://" + "(" + $authority +")" + "(" + $abs_path +")" + "?)";
   static String $rel_pathG = "(?:" + "(" + $rel_segment +")" + "(" + $abs_path +")" + "?)";
   static String $rel_segmentG = "(?:(?:" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" + "|;|@|&|=|\\+|$|,)+)";
   static String $schemeG = "(?:" + "(" + $alpha +")" + "(?:" + "(" + $alphanum +")" + "|\\+|-|\\.)*)";
   static String $authorityG = "(?:(?:" + "(" + $server +")" + "|" + "(" + $reg_name +")" +"))";
   static String $reg_nameG = "(?:(?:" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" + "|$|,|;|:|@|&|=|\\+)+)";
   static String $serverG = "(?:(?:(?:" + "(" + $userinfo +")" + "@)?" + "(" + $hostport +")" +"))";
   static String $userinfoG = "(?:(?:" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" + "|;|:|&|=|\\+|$|,)*)";
   static String $hostportG = "(?:" + "(" + $host +")" + "(?::" + "(" + $port +")" +")?)";
   static String $hostG = "(?:(?:" + "(" + $hostname +")" + "|" + "(" + $IPv4address +")" +"))";
   static String $hostnameG = "(?:(?:" + "(" + $domainlabel +")" + "\\.)*" + "(" + $toplabel +")" + "\\.?)";
   static String $domainlabelG = "(?:" + "(" + $alphanum +")" + "(?:" + "(" + $alphanum +")" + "|-)*" + "(" + $alphanum +")" +")";
   static String $toplabelG = "(?:" + "(" + $alpha +")" + "(?:" + "(" + $alphanum +")" + "|-)*" + "(" + $alphanum +")" +")";
   static String $pathG = "(?:" + "(" + $abs_path +")" + "|" + "(" + $opaque_part +")" +")?";
   static String $opaque_partG = "(?:" + "(" + $uric_no_slash +")" + "(" + $uric +")" + "*)";
   static String $uric_no_slashG = "(?:" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" + "|;|\\?|:|@|&|=|\\+|$|,)";
   static String $abs_pathG = "/" + "(" + $path_segments +")";
   static String $path_segmentsG = "(?:" + "(" + $segment +")" + "(?:/" + "(" + $segment +")" +")*)";
   static String $segmentG = "(?:" + "(" + $pchar +")" + "*(?:;" + "(" + $param +")" +")*)";
   static String $paramG = "(?:" + "(" + $pchar +")" + "*)";
   static String $pcharG = "(?:(?:" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" + "|:|@|&|=|\\+|$|,))";
   static String $queryG = "(?:" + "(" + $uric +")" + "*)";
   static String $fragmentG = "(?:" + "(" + $uric +")" + "*)";
   static String $uricG = "(?:" + "(" + $reserved +")" + "|" + "(" + $unreserved +")" + "|" + "(" + $escaped +")" +")";
   static String $reservedG = "(?:[;/\\?:@&=\\+$\\,}])";

   // Now construct the regular expressions which extract matches.
   static RegularExpression $escapedRE = new RegularExpression("^" + $escapedG + "$");
   static RegularExpression $unreservedRE = new RegularExpression("^" + $unreservedG + "$");
   static RegularExpression $reservedRE = new RegularExpression("^" + $reservedG + "$");
   static RegularExpression $uricRE = new RegularExpression("^" + $uricG + "$");
   static RegularExpression $fragmentRE = new RegularExpression("^" + $fragmentG + "$");
   static RegularExpression $queryRE = new RegularExpression("^" + $queryG + "$");
   static RegularExpression $pcharRE = new RegularExpression("^" + $pcharG + "$");
   static RegularExpression $paramRE = new RegularExpression("^" + $paramG + "$");
   static RegularExpression $segmentRE = new RegularExpression("^" + $segmentG + "$");
   static RegularExpression $path_segmentsRE = new RegularExpression("^" + $path_segmentsG + "$");
   static RegularExpression $abs_pathRE = new RegularExpression("^" + $abs_pathG + "$");
   static RegularExpression $uric_no_slashRE = new RegularExpression("^" + $uric_no_slashG + "$");
   static RegularExpression $opaque_partRE = new RegularExpression("^" + $opaque_partG + "$");
   static RegularExpression $pathRE = new RegularExpression("^" + $pathG + "$");
   static RegularExpression $toplabelRE = new RegularExpression("^" + $toplabelG + "$");
   static RegularExpression $domainlabelRE = new RegularExpression("^" + $domainlabelG + "$");
   static RegularExpression $hostnameRE = new RegularExpression("^" + $hostnameG + "$");
   static RegularExpression $hostRE = new RegularExpression("^" + $hostG + "$");
   static RegularExpression $hostportRE = new RegularExpression("^" + $hostportG + "$");
   static RegularExpression $userinfoRE = new RegularExpression("^" + $userinfoG + "$");
   static RegularExpression $serverRE = new RegularExpression("^" + $serverG + "$");
   static RegularExpression $reg_nameRE = new RegularExpression("^" + $reg_nameG + "$");
   static RegularExpression $authorityRE = new RegularExpression("^" + $authorityG + "$");
   static RegularExpression $schemeRE = new RegularExpression("^" + $schemeG + "$");
   static RegularExpression $rel_segmentRE = new RegularExpression("^" + $rel_segmentG + "$");
   static RegularExpression $rel_pathRE = new RegularExpression("^" + $rel_pathG + "$");
   static RegularExpression $net_pathRE = new RegularExpression("^" + $net_pathG + "$");
   static RegularExpression $hier_partRE = new RegularExpression("^" + $hier_partG + "$");
   static RegularExpression $relativeURIRE = new RegularExpression("^" + $relativeURIG + "$");
   static RegularExpression $absoluteURIRE = new RegularExpression("^" + $absoluteURIG + "$");
   static RegularExpression $URIreferenceRE = new RegularExpression("^"+$URIreferenceG + "$");
   /* Now static initialization */
   static {
      String[] parts = new String[] {"$opaque_part", "$net_path", "$abs_path", "$rel_segment", "$reg_name", "$fragment", "$query", "$port", "$scheme", "$userinfo", "$hostname", "$IPv4address"};
      // now make the data extracting REs findable (didn't really need to make them static)
      ht.put("$escapedRE", $escapedRE);
      ht.put("$unreservedRE", $unreservedRE);
      ht.put("$reservedRE", $reservedRE);
      ht.put("$uricRE", $uricRE);
      ht.put("$fragmentRE", $fragmentRE);
      ht.put("$queryRE", $queryRE);
      ht.put("$pcharRE", $pcharRE);
      ht.put("$paramRE", $paramRE);
      ht.put("$segmentRE", $segmentRE);
      ht.put("$path_segmentsRE", $path_segmentsRE);
      ht.put("$abs_pathRE", $abs_pathRE);
      ht.put("$uric_no_slashRE", $uric_no_slashRE);
      ht.put("$opaque_partRE", $opaque_partRE);
      ht.put("$pathRE", $pathRE);
      ht.put("$toplabelRE", $toplabelRE);
      ht.put("$domainlabelRE", $domainlabelRE);
      ht.put("$hostnameRE", $hostnameRE);
      ht.put("$hostRE", $hostRE);
      ht.put("$hostportRE", $hostportRE);
      ht.put("$userinfoRE", $userinfoRE);
      ht.put("$serverRE", $serverRE);
      ht.put("$reg_nameRE", $reg_nameRE);
      ht.put("$authorityRE", $authorityRE);
      ht.put("$schemeRE", $schemeRE);
      ht.put("$rel_segmentRE", $rel_segmentRE);
      ht.put("$rel_pathRE", $rel_pathRE);
      ht.put("$net_pathRE", $net_pathRE);
      ht.put("$hier_partRE", $hier_partRE);
      ht.put("$relativeURIRE", $relativeURIRE);
      ht.put("$absoluteURIRE", $absoluteURIRE);
      ht.put("$URIreferenceRE", $URIreferenceRE);

      // Now for each non-terminal, create a vector of the names of the non-terms on its RHS
      Vector v = new Vector();
      ht.put("$escaped", v);
      v.addElement("$hex");
      v.addElement("$hex");
      v = new Vector();
      ht.put("$unreserved", v);
      v.addElement("$mark");
      v = new Vector();
      ht.put("$uric", v);
      v.addElement("$reserved");
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$fragment", v);
      v.addElement("$uric");
      v = new Vector();
      ht.put("$query", v);
      v.addElement("$uric");
      v = new Vector();
      ht.put("$pchar", v);
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$param", v);
      v.addElement("$pchar");
      v = new Vector();
      ht.put("$segment", v);
      v.addElement("$pchar");
      v.addElement("$param");
      v = new Vector();
      ht.put("$path_segments", v);
      v.addElement("$segment");
      v.addElement("$segment");
      v = new Vector();
      ht.put("$abs_path", v);
      v.addElement("$path_segments");
      v = new Vector();
      ht.put("$uric_no_slash", v);
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$opaque_part", v);
      v.addElement("$uric_no_slash");
      v.addElement("$uric");
      v = new Vector();
      ht.put("$path", v);
      v.addElement("$abs_path");
      v.addElement("$opaque_part");
      v = new Vector();
      ht.put("$toplabel", v);
      v.addElement("$alpha");
      v.addElement("$alphanum");
      v.addElement("$alphanum");
      v = new Vector();
      ht.put("$domainlabel", v);
      v.addElement("$alphanum");
      v.addElement("$alphanum");
      v.addElement("$alphanum");
      v = new Vector();
      ht.put("$hostname", v);
      v.addElement("$domainlabel");
      v.addElement("$toplabel");
      v = new Vector();
      ht.put("$host", v);
      v.addElement("$hostname");
      v.addElement("$IPv4address");
      //
      v = new Vector();
      ht.put("$hostport", v);
      v.addElement("$host");
      v.addElement("$port");
      v = new Vector();
      ht.put("$userinfo", v);
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$server", v);
      v.addElement("$userinfo");
      v.addElement("$hostport");
      v = new Vector();
      ht.put("$reg_name", v);
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$authority", v);
      v.addElement("$server");
      v.addElement("$reg_name");
      v = new Vector();
      ht.put("$scheme", v);
      v.addElement("$alpha");
      v.addElement("$alphanum");
      v = new Vector();
      ht.put("$rel_segment", v);
      v.addElement("$unreserved");
      v.addElement("$escaped");
      v = new Vector();
      ht.put("$rel_path", v);
      v.addElement("$rel_segment");
      v.addElement("$abs_path");
      v = new Vector();
      ht.put("$net_path", v);
      v.addElement("$authority");
      v.addElement("$abs_path");
      v = new Vector();
      ht.put("$hier_part", v);
      v.addElement("$net_path");
      v.addElement("$abs_path");
      v.addElement("$query");
      v = new Vector();
      ht.put("$relativeURI", v);
      v.addElement("$net_path");
      v.addElement("$abs_path");
      v.addElement("$rel_path");
      v.addElement("$query");
      v = new Vector();
      ht.put("$absoluteURI", v);
      v.addElement("$scheme");
      v.addElement("$hier_part");
      v.addElement("$opaque_part");
      v = new Vector();
      ht.put("$URIreference", v);
      v.addElement("$absoluteURI");
      v.addElement("$relativeURI");
      v.addElement("$fragment");
   }
   public Vector stack = new Vector();
   private boolean valid = false;
   /** 
    * Returns the substring of the uri, which is matched by the non-terminal specified by request.
    <p>
    The search for the appropriate string traverses the parse tree of the uri.  Only 
    values, which occur on the right hand side of a production beginning with one of the
    following 31 rfc 2396 non-terminals, can return non-null:
    <p>
    "$IPv4address", "$URIreference", "$abs_path", "$absoluteURI", "$alpha", "$alphanum",
    "$authority", "$domainlabel", "$escaped", "$fragment", "$hex", "$hier_part", "$host",
    "$hostname", "$hostport", "$mark", "$net_path", "$opaque_part", "$param", "$path",
    "$path_segments", "$pchar", "$port", "$query", "$reg_name", "$rel_path", "$rel_segment",
    "$relativeURI", "$reserved", "$scheme", "$segment", "$server", "$toplabel",
    "$unreserved", "$uric", "$uric_no_slash", "$userinfo", 


    * @return the value of the component or null if none exists.
    * @param request java.lang.String.  request should be a non-terminal from the grammar contained in rfc2396
    */
   public uri(){
      super( "uri" );
   }
   public String getComponent(String request) {
      NamedMatch nm = (NamedMatch) stack.elementAt(0);
      // If this is what we're looking for return text.
      String currentMatchName = nm.getName();
      Vector currentMatchSubParts = (Vector) ht.get(currentMatchName);
      Match match = nm.getMatch();
      if (!request.startsWith("$")) {
         request = "$" + request;
      }
      if (currentMatchName.equals(request)) {
         return match.getCapturedText(0);
      }
//  				if ( basicParts.indexOf(currentMatchName) >= 0 ){
//  						return null;
//  				}
      //Search inside
      int i;
      for (i = 0; i < match.getNumberOfGroups()-1; i++) {
         String str = match.getCapturedText(i+1);
         if (str == null) {
            continue;
         }
         // next is $name
         String ithSubMatchName = (String) currentMatchSubParts.elementAt(i);
         Match m = new Match();
         RegularExpression re = (RegularExpression) ht.get(ithSubMatchName + "RE");
         if (re == null) {
            continue;
         }
         if (!re.matches(str, m)) {
            continue;
         }
         stack.insertElementAt(new NamedMatch(ithSubMatchName, m), 0);
         String result = getComponent(request);
         if (result != null) {
            stack.removeElementAt(0);
            return result;
         }
      }
      stack.removeElementAt(0);
      return null;
   }
   public static void main(String[] args) throws IOException, FileNotFoundException {
      if (args.length == 0) {
         System.err.println("Must specify file holding uri examples\n");
      }
      int i, j;
      String success = "";
      String failure = "";
      /* TODO replace FileStrinRW in main driver
      FileStringRW fsrw = new FileStringRW();
      for (i = 0; i < args.length; i++) {
         fsrw.clear();
         fsrw.setFile(new String[] {args[i]});
         fsrw.read(0);
         String contents = fsrw.getContents();
         StringTokenizer tok = new StringTokenizer(contents);
         while (tok.hasMoreElements()) {
            uri uri = new uri();
            String str = (String) tok.nextElement();
            String requestedComponent = "hier_part";
            contents = "Processing " + str + "\n";
            if (uri.validate(str)) {
               contents += "uri.validate succeeds\n";
               String xxx = uri.getComponent(requestedComponent);
               contents += "\tgetComponent(" + requestedComponent + ") " +
                           (( xxx == null ) ? "fails\n\n" : (" returns " + xxx));
            } else {
               contents += " uri.validate fails on:" + str + "\n\n";
               contents += "subpattern matches\n";
               for (j = 0; j < arrayOfPatternStrings.length; j++) {
                  Match m = new Match();
                  RegularExpression r = new RegularExpression("(" + arrayOfPatternStrings[j] + ")");
                  if (r.matches(str, m)) {
                     contents += "\t" + names[j] + " matches: " + m.getCapturedText(1) + "\n";
                  } else {
                     contents += "\t" + names[j] + " does not match.\n";
                  }
               }
            }
            System.out.println( contents);
         }
      }
      */
   }
   /**
    * Determines whethr a string matches uriReference from rfc2396
    * @return boolean
    * @param str java.lang.String
    */
   public boolean validate(String str) {
      super.validate( str );
      return validateAs(str, "URIreference");
   }
   /**
       Determines whether the first parameter, is an instance of the rfc 2396
       non-terminal contained in the second parameter.  Valid second parameters are:
       <p>
       "$IPv4address", "$URIreference", "$abs_path", "$absoluteURI", "$alpha", "$alphanum",
       "$authority", "$domainlabel", "$escaped", "$fragment", "$hex", "$hier_part", "$host",
       "$hostname", "$hostport", "$mark", "$net_path", "$opaque_part", "$param", "$path",
       "$path_segments", "$pchar", "$port", "$query", "$reg_name", "$rel_path", "$rel_segment",
       "$relativeURI", "$reserved", "$scheme", "$segment", "$server", "$toplabel",
       "$unreserved", "$uric", "$uric_no_slash", "$userinfo", 
       
       * @return boolean
       * @param str java.lang.String
       * @param type java.lang.String - rfc 2396 non-terminal
       */
   public boolean validateAs(String str, String type) {
      if ( str.length() == 0 ) {
         return false;
      }
      stack.removeAllElements();
      Match m = new Match();
      stack.insertElementAt(new NamedMatch("$" + type, m), 0);
      RegularExpression re = (RegularExpression )ht.get("$" + type + "RE");
      return valid = re.matches(str, m);
   }
}
