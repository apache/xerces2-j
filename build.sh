#!/bin/sh

echo
echo "Xerces-Java Build System"
echo "------------------------"

if [ "$JAVA_HOME" = "" ] ; then
   echo "ERROR: JAVA_HOME not found in your environment."
   echo 
   echo "Please, set the JAVA_HOME variable in your environment to match the"
   echo "location of the Java Virtual Machine you want to use."
   exit 1
fi

# UNIX
CLPATHSEP=:
# if we're on a Windows box make it ;
uname | grep WIN && CLPATHSEP=\;

LOCALCLASSPATH="$JAVA_HOME/lib/tools.jar${CLPATHSEP}${JAVA_HOME}/lib/classes.zip${CLPATHSEP}./tools/ant.jar${CLPATHSEP}./tools/xerces-1.0.1.jar${CLPATHSEP}./tools/xalan-0.19.2.jar${CLPATHSEP}./tools/stylebook-1.0-b2.jar${CLPATHSEP}./tools/style-apachexml.jar${CLPATHSEP}./tools/xml.jar"
ANT_HOME=./tools

echo Building with classpath \"$LOCALCLASSPATH\"
echo Starting Ant...
echo
"$JAVA_HOME"/bin/java -Dant.home="$ANT_HOME" -classpath "$LOCALCLASSPATH" org.apache.tools.ant.Main $@
