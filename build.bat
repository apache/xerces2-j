@echo off
echo Xerces-Java Build System
echo ------------------------

if "%JAVA_HOME%" == "" goto error

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;.\tools\ant.jar;.\tools\xerces.jar;.\tools\xalan.jar;.\tools\stylebook-1.0-b2.jar;.\tools\style-apachexml.jar;

echo Building with classpath %LOCALCLASSPATH%
echo Starting Ant...
%JAVA_HOME%\bin\java.exe -Dant.home="./tools" -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5
goto end

:error
echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

:end
set LOCALCLASSPATH=
@echo on
