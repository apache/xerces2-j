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

package org.apache.xerces.readers;

import java.util.*;

/**
 * MIME2Java is a convenience class which handles conversions between MIME charset names
 * and Java encoding names.
 * <p>The supported XML encodings are the intersection of XML-supported code sets and those 
 * supported in JDK 1.1.
 * <p>MIME charset names are used on <var>xmlEncoding</var> parameters to methods such
 * as <code>TXDocument#setEncoding</code> and <code>DTD#setEncoding</code>.
 * <p>Java encoding names are used on <var>encoding</var> parameters to
 * methods such as <code>TXDocument#printWithFormat</code> and <code>DTD#printExternal</code>. 
 * <P>
 * <TABLE BORDER="0" WIDTH="100%">
 *  <TR>
 *      <TD WIDTH="33%">
 *          <P ALIGN="CENTER"><B>Common Name</B>
 *      </TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER"><B>Use this name in XML files</B>
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER"><B>Name Type</B>
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER"><B>Xerces converts to this Java Encoder Name</B>
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">8 bit Unicode</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">UTF-8
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">UTF8
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin 1</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-1
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-1
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin 2</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-2
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-2
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin 3</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-3
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-3
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin 4</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-4
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-4
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin Cyrillic</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-5
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-5
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin Arabic</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-6
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-6
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin Greek</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-7
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-7
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin Hebrew</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-8
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-8
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">ISO Latin 5</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-8859-9
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">ISO-8859-9
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: US</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-us
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp037
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Canada</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-ca
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp037
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Netherlands</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-nl
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp037
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Denmark</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-dk
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp277
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Norway</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-no
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp277
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Finland</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-fi
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp278
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Sweden</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-se
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp278
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Italy</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-it
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp280
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Spain, Latin America</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-es
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp284
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Great Britain</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-gb
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp285
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: France</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-fr
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp297
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Arabic</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-ar1
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp420
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Hebrew</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-he
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp424
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Switzerland</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-ch
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp500
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Roece</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-roece
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp870
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Yogoslavia</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-yu
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp870
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Iceland</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-is
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp871
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">EBCDIC: Urdu</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ebcdic-cp-ar2
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">IANA
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">cp918
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Chinese for PRC, mixed 1/2 byte</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">gb2312
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">GB2312
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Extended Unix Code, packed for Japanese</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">euc-jp
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">eucjis
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Japanese: ISO-2022-jp</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">ISO-2020-jp
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">JIS
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Japanese: Shift JIS</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">Shift_JIS
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">SJIS
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Japanese Windows: An extension of Shift JIS</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">Windows-31J
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">MS932 (since JDK 1.2)
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Chinese: Big5</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">Big5
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">Big5
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Extended Unix Code, packed for Korean</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">euc-kr
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">iso2022kr
 *      </TD>
 *  </TR>
 *  <TR>
 *      <TD WIDTH="33%">Cyrillic</TD>
 *      <TD WIDTH="15%">
 *          <P ALIGN="CENTER">koi8-r
 *      </TD>
 *      <TD WIDTH="12%">
 *          <P ALIGN="CENTER">MIME
 *      </TD>
 *      <TD WIDTH="31%">
 *          <P ALIGN="CENTER">koi8-r
 *      </TD>
 *  </TR>
 * </TABLE>
 * 
 * @version $Id$
 * @author TAMURA Kent &lt;kent@trl.ibm.co.jp&gt;
 */
public class MIME2Java {
    
    static private Hashtable s_enchash;
    static private Hashtable s_revhash;
    
    static {
        s_enchash = new Hashtable();
        //    <preferred MIME name (uppercase)>, <Java encoding name>
        s_enchash.put("UTF-8", "UTF8");
        s_enchash.put("US-ASCII",        "ASCII");
        s_enchash.put("ISO-IR-6",        "ASCII");
        s_enchash.put("ANSI_X3.4-1986",        "ASCII");
        s_enchash.put("ISO_646.IRV:1991",        "ASCII");
        s_enchash.put("ASCII",        "ASCII");
        s_enchash.put("ISO646-US",        "ASCII");
        s_enchash.put("US",        "ASCII");
        s_enchash.put("IBM367",        "ASCII");
        s_enchash.put("CP367",        "ASCII");
        s_enchash.put("ISO-8859-1",      "ISO8859_1");
        s_enchash.put("ISO-IR-100",      "ISO8859_1");
        s_enchash.put("ISO_8859-1",      "ISO8859_1");
        s_enchash.put("LATIN1",      "ISO8859_1");
        s_enchash.put("L1",      "ISO8859_1");
        s_enchash.put("IBM819",      "ISO8859_1");
        s_enchash.put("CP819",      "ISO8859_1");
        s_enchash.put("ISO-8859-2",      "ISO8859_2");
        s_enchash.put("ISO-IR-101",      "ISO8859_2");
        s_enchash.put("ISO_8859-2",      "ISO8859_2");
        s_enchash.put("LATIN2",      "ISO8859_2");
        s_enchash.put("L2",      "ISO8859_2");
        s_enchash.put("ISO-8859-3",      "ISO8859_3");
        s_enchash.put("ISO-IR-109",      "ISO8859_3");
        s_enchash.put("ISO_8859-3",      "ISO8859_3");
        s_enchash.put("LATIN3",      "ISO8859_3");
        s_enchash.put("L3",      "ISO8859_3");
        s_enchash.put("ISO-8859-4",      "ISO8859_4");
        s_enchash.put("ISO-IR-110",      "ISO8859_4");
        s_enchash.put("ISO_8859-4",      "ISO8859_4");
        s_enchash.put("LATIN4",      "ISO8859_4");
        s_enchash.put("L4",      "ISO8859_4");
        s_enchash.put("ISO-8859-5",      "ISO8859_5");
        s_enchash.put("ISO-IR-144",      "ISO8859_5");
        s_enchash.put("ISO_8859-5",      "ISO8859_5");
        s_enchash.put("CYRILLIC",      "ISO8859_5");
        s_enchash.put("ISO-8859-6",      "ISO8859_6");
        s_enchash.put("ISO-IR-127",      "ISO8859_6");
        s_enchash.put("ISO_8859-6",      "ISO8859_6");
        s_enchash.put("ECMA-114",      "ISO8859_6");
        s_enchash.put("ASMO-708",      "ISO8859_6");
        s_enchash.put("ARABIC",      "ISO8859_6");
        s_enchash.put("ISO-8859-7",      "ISO8859_7");
        s_enchash.put("ISO-IR-126",      "ISO8859_7");
        s_enchash.put("ISO_8859-7",      "ISO8859_7");
        s_enchash.put("ELOT_928",      "ISO8859_7");
        s_enchash.put("ECMA-118",      "ISO8859_7");
        s_enchash.put("GREEK",      "ISO8859_7");
        s_enchash.put("GREEK8",      "ISO8859_7");
        s_enchash.put("ISO-8859-8",      "ISO8859_8");
        s_enchash.put("ISO-IR-138",      "ISO8859_8");
        s_enchash.put("ISO_8859-8",      "ISO8859_8");
        s_enchash.put("HEBREW",      "ISO8859_8");
        s_enchash.put("ISO-8859-9",      "ISO8859_9");
        s_enchash.put("ISO-IR-148",      "ISO8859_9");
        s_enchash.put("ISO_8859-9",      "ISO8859_9");
        s_enchash.put("LATIN5",      "ISO8859_9");
        s_enchash.put("L5",      "ISO8859_9");
        s_enchash.put("ISO-2022-JP",     "ISO2022JP");
        s_enchash.put("SHIFT_JIS",       "SJIS");
        s_enchash.put("MS_Kanji",       "SJIS");
        /**
         * MS932 is suitable for Windows-31J,
         * but JDK 1.1.x does not support MS932.
         */
        String version = System.getProperty("java.version");
        if (version.equals("1.1") || version.startsWith("1.1.")) {
            s_enchash.put("WINDOWS-31J",      "SJIS");
        } else {
            s_enchash.put("WINDOWS-31J",      "MS932");
        }
        s_enchash.put("EUC-JP",          "EUC_JP");
        s_enchash.put("GB2312",          "GB2312");
        s_enchash.put("BIG5",            "Big5");
        s_enchash.put("EUC-KR",          "EUC_KR");
        s_enchash.put("ISO-2022-KR",     "ISO2022KR");
        s_enchash.put("KOI8-R",          "KOI8_R");

        s_enchash.put("EBCDIC-CP-US",    "CP037");
        s_enchash.put("EBCDIC-CP-CA",    "CP037");
        s_enchash.put("EBCDIC-CP-NL",    "CP037");
        s_enchash.put("EBCDIC-CP-WT",    "CP037");
        s_enchash.put("EBCDIC-CP-DK",    "CP277");
        s_enchash.put("EBCDIC-CP-NO",    "CP277");
        s_enchash.put("EBCDIC-CP-FI",    "CP278");
        s_enchash.put("EBCDIC-CP-SE",    "CP278");
        s_enchash.put("EBCDIC-CP-IT",    "CP280");
        s_enchash.put("EBCDIC-CP-ES",    "CP284");
        s_enchash.put("EBCDIC-CP-GB",    "CP285");
        s_enchash.put("EBCDIC-CP-FR",    "CP297");
        s_enchash.put("EBCDIC-CP-AR1",   "CP420");
        s_enchash.put("EBCDIC-CP-HE",    "CP424");
        s_enchash.put("EBCDIC-CP-CH",    "CP500");
        s_enchash.put("EBCDIC-CP-BE",    "CP500");
        s_enchash.put("CP-AR",        "CP868");
        s_enchash.put("CP-GR",        "CP869");
        s_enchash.put("EBCDIC-CP-ROECE", "CP870");
        s_enchash.put("EBCDIC-CP-YU",    "CP870");
        s_enchash.put("EBCDIC-CP-IS",    "CP871");
        s_enchash.put("EBCDIC-CP-AR2",   "CP918");

	    // Add support for Cp1252 and its friends
        s_enchash.put("WINDOWS-1250",   "Cp1250");
        s_enchash.put("WINDOWS-1251",   "Cp1251");
        s_enchash.put("WINDOWS-1252",   "Cp1252");
        s_enchash.put("WINDOWS-1253",   "Cp1253");
        s_enchash.put("WINDOWS-1254",   "Cp1254");
        s_enchash.put("WINDOWS-1255",   "Cp1255");
        s_enchash.put("WINDOWS-1256",   "Cp1256");
        s_enchash.put("WINDOWS-1257",   "Cp1257");
        s_enchash.put("WINDOWS-1258",   "Cp1258");
        s_enchash.put("TIS-620",   "TIS620");
                                                // j:CNS11643 -> EUC-TW?
        s_enchash.put("ISO-2022-CN",    "ISO2022CN");
        s_enchash.put("X0201",  "JIS0201");
        s_enchash.put("X0208",  "JIS0208");
        s_enchash.put("X0212",  "JIS0212");
        s_enchash.put("ISO-IR-159",  "JIS0212");
                                                
        s_revhash = new Hashtable();
        //    <Java encoding name (uppercase)>, <preferred MIME name>
        s_revhash.put("UTF8", "UTF-8");
        s_revhash.put("ASCII", "US-ASCII");
        s_revhash.put("ASCII", "ISO-IR-6");
        s_revhash.put("ASCII", "ANSI_X3.4-1986");
        s_revhash.put("ASCII", "ISO_646.IRV:1991");
        s_revhash.put("ASCII", "ASCII");
        s_revhash.put("ASCII", "ISO646-US");
        s_revhash.put("ASCII", "US");
        s_revhash.put("ASCII", "IBM367");
        s_revhash.put("ASCII", "CP367");
        s_revhash.put("ISO8859_1", "ISO-8859-1");
        s_revhash.put("ISO8859_1", "ISO-IR-100");
        s_revhash.put("ISO8859_1", "ISO_8859-1");
        s_revhash.put("ISO8859_1", "LATIN1");
        s_revhash.put("ISO8859_1", "L1");
        s_revhash.put("ISO8859_1", "IBM819");
        s_revhash.put("ISO8859_1", "CP819");
        s_revhash.put("ISO8859_2", "ISO-8859-2");
        s_revhash.put("ISO8859_2", "ISO-IR-101");
        s_revhash.put("ISO8859_2", "ISO_8859-2");
        s_revhash.put("ISO8859_2", "LATIN2");
        s_revhash.put("ISO8859_2", "L2");
        s_revhash.put("ISO8859_3", "ISO-8859-3");
        s_revhash.put("ISO8859_3", "ISO-IR-109");
        s_revhash.put("ISO8859_3", "ISO_8859-3");
        s_revhash.put("ISO8859_3", "LATIN3");
        s_revhash.put("ISO8859_3", "L3");
        s_revhash.put("ISO8859_4", "ISO-8859-4");
        s_revhash.put("ISO8859_4", "ISO-IR-110");
        s_revhash.put("ISO8859_4", "ISO_8859-4");
        s_revhash.put("ISO8859_4", "LATIN4");
        s_revhash.put("ISO8859_4", "L4");
        s_revhash.put("ISO8859_5", "ISO-8859-5");
        s_revhash.put("ISO8859_5", "ISO-IR-144");
        s_revhash.put("ISO8859_5", "ISO_8859-5");
        s_revhash.put("ISO8859_5", "CYRILLIC");
        s_revhash.put("ISO8859_6", "ISO-8859-6");
        s_revhash.put("ISO8859_6", "ISO-IR-127");
        s_revhash.put("ISO8859_6", "ISO_8859-6");
        s_revhash.put("ISO8859_6", "ECMA-114");
        s_revhash.put("ISO8859_6", "ASMO-708");
        s_revhash.put("ISO8859_6", "ARABIC");
        s_revhash.put("ISO8859_7", "ISO-8859-7");
        s_revhash.put("ISO8859_7", "ISO-IR-126");
        s_revhash.put("ISO8859_7", "ISO_8859-7");
        s_revhash.put("ISO8859_7", "ELOT_928");
        s_revhash.put("ISO8859_7", "ECMA-118");
        s_revhash.put("ISO8859_7", "GREEK");
        s_revhash.put("ISO8859_7", "GREEK8");
        s_revhash.put("ISO8859_8", "ISO-8859-8");
        s_revhash.put("ISO8859_8", "ISO-IR-138");
        s_revhash.put("ISO8859_8", "ISO_8859-8");
        s_revhash.put("ISO8859_8", "HEBREW");
        s_revhash.put("ISO8859_9", "ISO-8859-9");
        s_revhash.put("ISO8859_9", "ISO-IR-148");
        s_revhash.put("ISO8859_9", "ISO_8859-9");
        s_revhash.put("ISO8859_9", "LATIN5");
        s_revhash.put("ISO8859_9", "L5");
        s_revhash.put("ISO2022JP", "ISO-2022-JP");
        s_revhash.put("SJIS", "Shift_JIS");
        s_revhash.put("SJIS", "MS_Kanji");
        s_revhash.put("MS932", "WINDOWS-31J");
        s_revhash.put("EUC_JP", "EUC-JP");
        s_revhash.put("GB2312", "GB2312");
        s_revhash.put("BIG5", "Big5");
        s_revhash.put("EUC_KR", "EUC-KR");
        s_revhash.put("ISO2022KR", "ISO-2022-KR");
        s_revhash.put("KOI8_R", "KOI8-R");

        s_revhash.put("CP037", "EBCDIC-CP-US");
        s_revhash.put("CP037", "EBCDIC-CP-CA");
        s_revhash.put("CP037", "EBCDIC-CP-NL");
        s_revhash.put("CP037", "EBCDIC-CP-WT");
        s_revhash.put("CP277", "EBCDIC-CP-DK");
        s_revhash.put("CP277", "EBCDIC-CP-NO");
        s_revhash.put("CP278", "EBCDIC-CP-FI");
        s_revhash.put("CP278", "EBCDIC-CP-SE");
        s_revhash.put("CP280", "EBCDIC-CP-IT");
        s_revhash.put("CP284", "EBCDIC-CP-ES");
        s_revhash.put("CP285", "EBCDIC-CP-GB");
        s_revhash.put("CP297", "EBCDIC-CP-FR");
        s_revhash.put("CP420", "EBCDIC-CP-AR1");
        s_revhash.put("CP424", "EBCDIC-CP-HE");
        s_revhash.put("CP500", "EBCDIC-CP-CH");
        s_revhash.put("CP500", "EBCDIC-CP-BE");
        s_revhash.put("CP868",      "CP-AR");
        s_revhash.put("CP869",      "CP-GR");
        s_revhash.put("CP870", "EBCDIC-CP-ROECE");
        s_revhash.put("CP870", "EBCDIC-CP-YU");
        s_revhash.put("CP871", "EBCDIC-CP-IS");
        s_revhash.put("CP918", "EBCDIC-CP-AR2");

	// Add support for Cp1252 and friends
	// Since this code page should be written out in mixed case,
	// there is no need to reverse the function.
        s_revhash.put("CP1250", "WINDOWS-1250");
        s_revhash.put("CP1251", "WINDOWS-1251");
        s_revhash.put("CP1252", "WINDOWS-1252");
        s_revhash.put("CP1253", "WINDOWS-1253");
        s_revhash.put("CP1254", "WINDOWS-1254");
        s_revhash.put("CP1255", "WINDOWS-1255");
        s_revhash.put("CP1256", "WINDOWS-1256");
        s_revhash.put("CP1257", "WINDOWS-1257");
        s_revhash.put("CP1258", "WINDOWS-1258");
        s_revhash.put("TIS620", "TIS-620");
        s_revhash.put("ISO2022CN",  "ISO-2022-CN");
        s_revhash.put("JIS0201",    "X0201");
        s_revhash.put("JIS0208",    "X0208");
        s_revhash.put("JIS0212",    "X0212");
        s_revhash.put("JIS0212",    "ISO-IR-159");
    }

    private MIME2Java() {
    }

    /**
     * Convert a MIME charset name, also known as an XML encoding name, to a Java encoding name.
     * @param   mimeCharsetName Case insensitive MIME charset name: <code>UTF-8, US-ASCII, ISO-8859-1,
     *                          ISO-8859-2, ISO-8859-3, ISO-8859-4, ISO-8859-5, ISO-8859-6,
     *                          ISO-8859-7, ISO-8859-8, ISO-8859-9, ISO-2022-JP, Shift_JIS, Windows-31J
     *                          EUC-JP, GB2312, Big5, EUC-KR, ISO-2022-KR, KOI8-R,
     *                          EBCDIC-CP-US, EBCDIC-CP-CA, EBCDIC-CP-NL, EBCDIC-CP-DK,
     *                          EBCDIC-CP-NO, EBCDIC-CP-FI, EBCDIC-CP-SE, EBCDIC-CP-IT,
     *                          EBCDIC-CP-ES, EBCDIC-CP-GB, EBCDIC-CP-FR, EBCDIC-CP-AR1,
     *                          EBCDIC-CP-HE, EBCDIC-CP-CH, EBCDIC-CP-ROECE, EBCDIC-CP-YU,
     *                          EBCDIC-CP-IS and EBCDIC-CP-AR2</code>.
     * @return                  Java encoding name, or <var>null</var> if <var>mimeCharsetName</var>
     *                          is unknown.
     * @see #reverse
     */
    public static String convert(String mimeCharsetName) {
        return (String)s_enchash.get(mimeCharsetName.toUpperCase());
    }

    /**
     * Convert a Java encoding name to MIME charset name.
     * Available values of <i>encoding</i> are "UTF8", "8859_1", "8859_2", "8859_3", "8859_4",
     * "8859_5", "8859_6", "8859_7", "8859_8", "8859_9", "JIS", "SJIS", "MS932", "EUCJIS",
     * "GB2312", "BIG5", "KSC5601", "ISO2022KR",  "KOI8_R", "CP037", "CP277", "CP278",
     * "CP280", "CP284", "CP285", "CP297", "CP420", "CP424", "CP500", "CP870", "CP871" and "CP918".
     * @param   encoding    Case insensitive Java encoding name: <code>UTF8, 8859_1, 8859_2, 8859_3,
     *                      8859_4, 8859_5, 8859_6, 8859_7, 8859_8, 8859_9, JIS, SJIS, MS932, EUCJIS,
     *                      GB2312, BIG5, KSC5601, ISO2022KR, KOI8_R, CP037, CP277, CP278,
     *                      CP280, CP284, CP285, CP297, CP420, CP424, CP500, CP870, CP871 
     *                      and CP918</code>.
     * @return              MIME charset name, or <var>null</var> if <var>encoding</var> is unknown.
     * @see #convert
     */
    public static String reverse(String encoding) {
        return (String)s_revhash.get(encoding.toUpperCase());
    }
}
