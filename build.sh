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

LOCALCLASSPATH=$JAVA_HOME/lib/tools.jar:./tools/ant.jar:./tools/xerces-1.0.1.jar:./tools/xalan-0.19.2.jar:./tools/stylebook-1.0-b2.jar:./tools/style-apachexml.jar:./tools/xml.jar:$CLASSPATH
ANT_HOME=./tools

echo Building with classpath $LOCALCLASSPATH
echo Starting Ant...
echo
$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath $LOCALCLASSPATH org.apache.tools.ant.Main $*
unset LOCALCLASSPATH