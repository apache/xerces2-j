/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.taskdefs.Javac;

import java.lang.StringBuffer;
import java.util.Properties;
import java.util.Locale;

/**
 * The implementation of the javac compiler for IBM JDK 1.4
 *
 * The purpose of this task is to diagnose whether we're
 * running on an IBM 1.4 JVM; if we are, to
 * set up the bootclasspath such that the build will
 * succeed; if we aren't, then invoke the Javac12
 * task.
 *
 * @author Neil Graham, IBM
 */

public class XJavac extends Javac {

    /**
     * Run the compilation.
     *
     * @exception BuildException if the compilation has problems.
     */
    public void execute() throws BuildException {
        if(JavaEnvUtils.getJavaVersion().equals(JavaEnvUtils.JAVA_1_4)) {
            // maybe the right one; check vendor:
            // by checking system properties:
            Properties props = null;
            try {
                props = System.getProperties();
            } catch (Exception e) {
                throw new BuildException("unable to determine java vendor because could not access system properties!");
            }
            // this is supposed to be provided by all JVM's from time immemorial
            String vendor = ((String)props.get("java.vendor")).toUpperCase(Locale.ENGLISH);
            if(vendor.indexOf("IBM") >= 0){
                // we're on an IBM 1.4; fiddle with the bootclasspath.
                Path bcp = createBootclasspath();
                String javaHome = System.getProperty("java.home");
                StringBuffer bcpMember = new StringBuffer();
                bcpMember.append(javaHome).append("/lib/charsets.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(), "/lib/core.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/graphics.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/javaws.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/jaws.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/security.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/server.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/JawBridge.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/gskikm.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/ibmjceprovider.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/indicim.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/jaccess.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/ldapsec.jar:");
                bcp.createPathElement().setPath(bcpMember.toString());
                bcpMember.replace(javaHome.length(), bcpMember.length(),  "/lib/ext/oldcertpath.jar");
                bcp.createPathElement().setPath(bcpMember.toString());
                setBootclasspath(bcp);
            }
            // need to do special things for Sun too and also
            // for Apple and Blackdown: a Linux port of Sun Java
            else if( (vendor.indexOf("SUN") >= 0) || 
                     (vendor.indexOf("BLACKDOWN") >= 0) || 
                     (vendor.indexOf("APPLE") >= 0) ) {
                // we're on an SUN 1.4; fiddle with the bootclasspath.
                // since we can't eviscerate XML-related info here,
                // we must use the classpath
                Path bcp = createBootclasspath();
                Path clPath = getClasspath();
                bcp.append(clPath);
                String currBCP = (String)props.get("sun.boot.class.path");
                Path currBCPath = new Path(null); 
                currBCPath.createPathElement().setPath(currBCP);
                bcp.append(currBCPath);
                setBootclasspath(bcp);
            }
        }
        // now just do the normal thing:
        super.execute();
    }
}
