include Makefile.incl
# Makefile for directory .
#
# This makefile depends on the following environment variables
# already being defined:
#
#   JAVAC   Java compiler (with options)
#   RM      Host delete file command (with options)
#
# In addition, the CLASSPATH environment variable must
# include the absolute path of the base source directory.

TARGETS=

all: dirs compile 

dirs:
	${MAKE} -C org

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

clean:
	${MAKE} -C org clean

apidocs: ../docs/apiDocs/index.html
../docs/apiDocs/index.html: classfiles_updated

	${JDK12BIN}/javadoc -windowtitle "Xerces ${PRODUCVERSION_DOT} API" \
		-splitindex -header "Xerces ${PRODUCTVERSION_DOT}" \
		-version  \
		-d ../docs/apiDocs ${PACKAGES}

jars: source samples

source: ../bin/${JARFILE}
../bin/${JARFILE}: classfiles_updated

#########################################
#copy the .class and .dtd files under src
#-src/org/apache/xerces/dom
	${MKDIR} ../class/org/apache/xerces/dom
	${CP}  ../src/org/apache/xerces/dom/*.class  			../class/org/apache/xerces/dom

#-src/org/apache/xerces/dom/traversal
	${MKDIR} ../class/org/apache/xerces/dom/traversal       
	${CP}  ../src/org/apache/xerces/dom/traversal/*.class  		../class/org/apache/xerces/dom/traversal

#-src/org/apache/xerces/dom/events
	${MKDIR} ../class/org/apache/xerces/dom/events       
	${CP}  ../src/org/apache/xerces/dom/events/*.class  		../class/org/apache/xerces/dom/events

#-src/org/apache/xerces/domx
	${MKDIR} ../class/org/apache/xerces/domx
	${CP}  ../src/org/apache/xerces/domx/*.class  		../class/org/apache/xerces/domx

#-src/org/apache/xerces/framework
	${MKDIR} ../class/org/apache/xerces/framework
	${CP}  ../src/org/apache/xerces/framework/*.class		../class/org/apache/xerces/framework

#-src/org/apache/xerces/infoset
#	${MKDIR} ../class/org/apache/xerces/infoset
#	${CP}  ../src/org/apache/xerces/infoset/*.class			../class/org/apache/xerces/infoset

#-src/org/apache/xerces/internal/msg
#	${MKDIR} ../class/org/apache/xerces/internal/msg
#	${CP}  ../src/org/apache/xerces/internal/msg/*.class		../class/org/apache/xerces/internal/msg

#-src/org/apache/xerces/msg
	${MKDIR} ../class/org/apache/xerces/msg
	${CP}  ../src/org/apache/xerces/msg/*.class                      ../class/org/apache/xerces/msg

#-src/org/apache/xerces/parsers
	${MKDIR} ../class/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/*.class			../class/org/apache/xerces/parsers

#-src/org/apache/xerces/readers
	${MKDIR} ../class/org/apache/xerces/readers
	${CP}  ../src/org/apache/xerces/readers/*.class                  ../class/org/apache/xerces/readers

#-src/org/apache/xerces/readers
	${MKDIR} ../class/org/apache/xerces/readers
	${CP}  ../src/org/apache/xerces/readers/*.dtd                    ../class/org/apache/xerces/readers

#-src/org/apache/xerces/utils
	${MKDIR} ../class/org/apache/xerces/utils
	${CP}  ../src/org/apache/xerces/utils/*.class                    ../class/org/apache/xerces/utils

#-src/org/apache/xerces/validaters/datatype
	${MKDIR} ../class/org/apache/xerces/validators/datatype
	${CP}  ../src/org/apache/xerces/validators/datatype/*.class      ../class/org/apache/xerces/validators/datatype

#-src/org/apache/xerces/validaters/dtd
	${MKDIR} ../class/org/apache/xerces/validators/dtd
	${CP}  ../src/org/apache/xerces/validators/dtd/*.class           ../class/org/apache/xerces/validators/dtd

#-src/org/apache/xerces/validaters/schema
	${MKDIR} ../class/org/apache/xerces/validators/schema
	${CP}  ../src/org/apache/xerces/validators/schema/*.class        ../class/org/apache/xerces/validators/schema

#-src/org/apache/xerces/validaters/schema
	${CP}  ../src/org/apache/xerces/validators/schema/*.dtd          ../class/org/apache/xerces/validators/schema

#-src/org/apache/xml/serialize
	${MKDIR} ../class/org/apache/xml/serialize
	${CP}  ../src/org/apache/xml/serialize/*.class                  ../class/org/apache/xml/serialize
	${CP}  ../src/org/apache/xml/serialize/*.res                    ../class/org/apache/xml/serialize 

#-src/org/apache/html/dom
	${MKDIR} ../class/org/apache/html
	${MKDIR} ../class/org/apache/html/dom
	${CP}  ../src/org/apache/html/dom/*.class                  ../class/org/apache/html/dom

#-src/org/w3c/dom
	${MKDIR} ../class/org/w3c/dom	
	${CP}  ../src/org/w3c/dom/*.class				../class/org/w3c/dom

#-src/org/w3c/dom/events
	${MKDIR} ../class/org/w3c/dom/events	
	${CP}  ../src/org/w3c/dom/events/*.class			../class/org/w3c/dom/events

#-src/org/w3c/dom/html
	${MKDIR} ../class/org/w3c/dom/html	
	${CP}  ../src/org/w3c/dom/html/*.class				../class/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${MKDIR} ../class/org/w3c/dom/traversal	
	${CP}  ../src/org/w3c/dom/traversal/*.class			../class/org/w3c/dom/traversal

#-src/org/xml/sax
	${MKDIR} ../class/org/xml/sax
	${CP}  ../src/org/xml/sax/*.class				../class/org/xml/sax

#-src/org/xml/sax/helpers
	${MKDIR} ../class/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/*.class			../class/org/xml/sax/helpers

#-src/org/xml/sax/misc
	${MKDIR} ../class/org/xml/sax/misc
	${CP}  ../src/org/xml/sax/misc/*.class			../class/org/xml/sax/misc

#make the first jar file
	echo ${PRODUCTVERSION}> ../class/Xerces_Ver_${PRODUCTVERSION}.info
	cd ../class;${JAR} ../bin/${JARFILE} *.info org; cd ../src        
###################################
#copy the .class files unser samples
samples: ../bin/${SAMPLESJARFILE}
../bin/${SAMPLESJARFILE}: classfiles_updated

#-samples/dom

	${MKDIR} ../class/dom
	${CP}  ../samples/dom/*.class  ../class/dom

#-samples/dom/traversal

	${MKDIR} ../class/dom/traversal
	${CP}  ../samples/dom/traversal/*.class                    ../class/dom/traversal

#-samples/dom/wrappers

	${MKDIR} ../class/dom/wrappers
	${CP}  ../samples/dom/wrappers/*.class                     ../class/dom/wrappers

#-samples/sax

	${MKDIR} ../class/sax
	${CP}  ../samples/sax/*.class  ../class/sax

#-samples/sax/helpers

	${MKDIR}  ../class/sax/helpers
	${CP}  ../samples/sax/helpers/*.class                      ../class/sax/helpers

#-samples/ui

	${MKDIR}  ../class/ui
	${CP}  ../samples/ui/*.class  ../class/ui

#Make the second jar file

	cd ../class;${JAR} ../bin/${SAMPLESJARFILE} Xerces_Ver_${PRODUCTVERSION}.info dom sax ui;cd ../src

# copy the license, ANT build file, Status file, and READMEs
	${CP}  ../build.xml ../Readme ../Readme.html ../STATUS ../bin


package_src:

##################################################
#copy the .java .dtd files under src

#-src/org/apache/xerces/dom
	${MKDIR} ../source/src/org/apache/xerces/dom
	${CP}  ../src/org/apache/xerces/dom/*.java  			../source/src/org/apache/xerces/dom

#-src/org/apache/xerces/dom/traversal
	${MKDIR} ../source/src/org/apache/xerces/dom/traversal       
	${CP}  ../src/org/apache/xerces/dom/traversal/*.java  		../source/src/org/apache/xerces/dom/traversal

#-src/org/apache/xerces/dom/events
	${MKDIR} ../source/src/org/apache/xerces/dom/events       
	${CP}  ../src/org/apache/xerces/dom/events/*.java  		../source/src/org/apache/xerces/dom/events

#-src/org/apache/xerces/domx
	${MKDIR} ../source/src/org/apache/xerces/domx
	${CP}  ../src/org/apache/xerces/domx/*.java  		../source/src/org/apache/xerces/domx

#-src/org/apache/xerces/framework
	${MKDIR} ../source/src/org/apache/xerces/framework
	${CP}  ../src/org/apache/xerces/framework/*.java		../source/src/org/apache/xerces/framework

#-src/org/apache/xerces/infoset
#	${MKDIR} ../source/src/org/apache/xerces/infoset
#	${CP}  ../src/org/apache/xerces/infoset/*.java			../source/src/org/apache/xerces/infoset

#-src/org/apache/xerces/internal/msg
#	${MKDIR} ../source/src/org/apache/xerces/internal/msg
#	${CP}  ../src/org/apache/xerces/internal/msg/*.java		../source/src/org/apache/xerces/internal/msg

#-src/org/apache/xerces/msg
	${MKDIR} ../source/src/org/apache/xerces/msg
	${CP}  ../src/org/apache/xerces/msg/*.java                      ../source/src/org/apache/xerces/msg

#-src/org/apache/xerces/parsers
	${MKDIR} ../source/src/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/*.java			../source/src/org/apache/xerces/parsers

#-src/org/apache/xerces/readers
	${MKDIR} ../source/src/org/apache/xerces/readers
	${CP}  ../src/org/apache/xerces/readers/*.java                  ../source/src/org/apache/xerces/readers

#-src/org/apache/xerces/readers
	${MKDIR} ../source/src/org/apache/xerces/readers
	${CP}  ../src/org/apache/xerces/readers/*.dtd                    ../source/src/org/apache/xerces/readers

#-src/org/apache/xerces/utils
	${MKDIR} ../source/src/org/apache/xerces/utils
	${CP}  ../src/org/apache/xerces/utils/*.java                    ../source/src/org/apache/xerces/utils

#-src/org/apache/xerces/validaters/datatype
	${MKDIR} ../source/src/org/apache/xerces/validators/datatype
	${CP}  ../src/org/apache/xerces/validators/datatype/*.java      ../source/src/org/apache/xerces/validators/datatype

#-src/org/apache/xerces/validaters/dtd
	${MKDIR} ../source/src/org/apache/xerces/validators/dtd
	${CP}  ../src/org/apache/xerces/validators/dtd/*.java           ../source/src/org/apache/xerces/validators/dtd

#-src/org/apache/xerces/validaters/schema
	${MKDIR} ../source/src/org/apache/xerces/validators/schema
	${CP}  ../src/org/apache/xerces/validators/schema/*.java        ../source/src/org/apache/xerces/validators/schema

#-src/org/apache/xerces/validaters/schema
	${CP}  ../src/org/apache/xerces/validators/schema/*.dtd          ../source/src/org/apache/xerces/validators/schema

#-src/org/apache/xml/serialize
	${MKDIR} ../source/src/org/apache/xml/serialize
	${CP}  ../src/org/apache/xml/serialize/*.java                   ../source/src/org/apache/xml/serialize
	${CP}  ../src/org/apache/xml/serialize/*.res                    ../source/src/org/apache/xml/serialize

#-src/org/w3c/dom
	${MKDIR} ../source/src/org/w3c/dom	
	${CP}  ../src/org/w3c/dom/*.java				../source/src/org/w3c/dom

#-src/org/w3c/dom/events
	${MKDIR} ../source/src/org/w3c/dom/events	
	${CP}  ../src/org/w3c/dom/events/*.java				../source/src/org/w3c/dom/events

#-src/org/w3c/dom/html
	${MKDIR} ../source/src/org/w3c/dom/html	
	${CP}  ../src/org/w3c/dom/html/*.java				../source/src/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${MKDIR} ../source/src/org/w3c/dom/traversal	
	${CP}  ../src/org/w3c/dom/traversal/*.java			../source/src/org/w3c/dom/traversal

#-src/org/xml/sax
	${MKDIR} ../source/src/org/xml/sax
	${CP}  ../src/org/xml/sax/*.java				../source/src/org/xml/sax

#-src/org/xml/sax/helpers
	${MKDIR} ../source/src/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/*.java			../source/src/org/xml/sax/helpers

#-src/org/xml/sax/misc
	${MKDIR} ../source/src/org/xml/sax/misc
	${CP}  ../src/org/xml/sax/misc/*.java			../source/src/org/xml/sax/misc

###################################
#copy the .java and .dtd files unser samples

#-samples/dom

	${MKDIR} ../source/samples/dom
	${CP}  ../samples/dom/*.java  ../source/samples/dom

#-samples/dom/traversal

	${MKDIR} ../source/samples/dom/traversal
	${CP}  ../samples/dom/traversal/*.java                    ../source/samples/dom/traversal

#-samples/dom/wrappers

	${MKDIR} ../source/samples/dom/wrappers
	${CP}  ../samples/dom/wrappers/*.java                     ../source/samples/dom/wrappers

#-samples/sax

	${MKDIR} ../source/samples/sax
	${CP}  ../samples/sax/*.java  ../source/samples/sax

#-samples/sax/helpers

	${MKDIR}  ../source/samples/sax/helpers
	${CP}  ../samples/sax/helpers/*.java                      ../source/samples/sax/helpers

#-samples/ui

	${MKDIR}  ../source/samples/ui
	${CP}  ../samples/ui/*.java  ../source/samples/ui

#########################################
#copy the Makefiles under src

#-src/org/apache/xerces/dom
	${CP}  ../src/org/apache/xerces/dom/Makefile  			../source/src/org/apache/xerces/dom

#-src/org/apache/xerces/dom/traversal
	${CP}  ../src/org/apache/xerces/dom/traversal/Makefile  		../source/src/org/apache/xerces/dom/traversal

#-src/org/apache/xerces/dom/events
	${CP}  ../src/org/apache/xerces/dom/events/Makefile  		../source/src/org/apache/xerces/dom/events

#-src/org/apache/xerces/framework
	${CP}  ../src/org/apache/xerces/framework/Makefile		../source/src/org/apache/xerces/framework

#-src/org/apache/xerces/infoset
#	${MKDIR} ../source/src/org/apache/xerces/infoset
#	${CP}  ../src/org/apache/xerces/infoset/Makefile			../source/src/org/apache/xerces/infoset

#-src/org/apache/xerces/internal/msg
#	${CP}  ../src/org/apache/xerces/internal/msg/Makefile		../source/src/org/apache/xerces/internal/msg

#-src/org/apache/xerces/msg
	${CP}  ../src/org/apache/xerces/msg/Makefile                      ../source/src/org/apache/xerces/msg

#-src/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/Makefile			../source/src/org/apache/xerces/parsers

#-src/org/apache/xerces/readers
	${CP}  ../src/org/apache/xerces/readers/Makefile                  ../source/src/org/apache/xerces/readers

#-src/org/apache/xerces/utils
	${CP}  ../src/org/apache/xerces/utils/Makefile                    ../source/src/org/apache/xerces/utils

#-src/org/apache/xerces/validaters/datatype
	${CP}  ../src/org/apache/xerces/validators/datatype/Makefile      ../source/src/org/apache/xerces/validators/datatype

#-src/org/apache/xerces/validaters/dtd
	${CP}  ../src/org/apache/xerces/validators/dtd/Makefile           ../source/src/org/apache/xerces/validators/dtd

#-src/org/apache/xerces/validaters/schema
	${CP}  ../src/org/apache/xerces/validators/schema/Makefile        ../source/src/org/apache/xerces/validators/schema

#-src/org/apache/xml/serialize
	${CP}  ../src/org/apache/xml/serialize/Makefile                    ../source/src/org/apache/xml/serialize

#-src/org/w3c/dom
	${CP}  ../src/org/w3c/dom/Makefile				../source/src/org/w3c/dom

#-src/org/w3c/dom/events
	${CP}  ../src/org/w3c/dom/events/Makefile			../source/src/org/w3c/dom/events

#-src/org/w3c/dom/html
	${CP}  ../src/org/w3c/dom/html/Makefile				../source/src/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${CP}  ../src/org/w3c/dom/traversal/Makefile			../source/src/org/w3c/dom/traversal

#-src/org/xml/sax
	${CP}  ../src/org/xml/sax/Makefile				../source/src/org/xml/sax

#-src/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/Makefile			../source/src/org/xml/sax/helpers

#-src/org/xml/sax/misc
	${CP}  ../src/org/xml/sax/misc/Makefile			../source/src/org/xml/sax/misc

###
#copy the Makefiles and the batch files at higher levels
#-source
	${CP}  ../Makefile    ../source

#-source
	${CP}  ../BuildAll.bat    ../source

#-source
	${CP}  ../BuildAll    ../source

#-src
	${CP}  ../src/Makefile    ../source/src

#-src
	${CP}  ../src/Makefile.incl    ../source/src

#-src/org
	${CP}  ../src/org/Makefile    ../source/src/org

#-src/org/apache
	${CP}  ../src/org/apache/Makefile    ../source/src/org/apache

#-src/org/apache/xerces
	${CP}  ../src/org/apache/xerces/Makefile    ../source/src/org/apache/xerces

#-src/org/apache/xerces/domx
	${CP}  ../src/org/apache/xerces/domx/Makefile    ../source/src/org/apache/xerces/domx

#-src/org/apache/xerces/internal
#	${CP}  ../src/org/apache/xerces/internal/Makefile    ../source/src/org/apache/xerces/internal

#-src/org/apache/xerces/validators
	${CP}  ../src/org/apache/xerces/validators/Makefile    ../source/src/org/apache/xerces/validators

#-src/org/apache/xml
	${CP}  ../src/org/apache/xml/Makefile    ../source/src/org/apache/xml

#-src/org
	${CP}  ../src/org/Makefile    ../source/src/org

#-src/org/w3c
	${CP}  ../src/org/w3c/Makefile    ../source/src/org/w3c

#-src/org/xml
	${CP}  ../src/org/xml/Makefile    ../source/src/org/xml

###################################
#copy the Makefiles under samples

#-samples/dom

	${CP}  ../samples/dom/Makefile  ../source/samples/dom

#-samples/dom/traversal

	${CP}  ../samples/dom/traversal/Makefile                    ../source/samples/dom/traversal

#-samples/dom/wrappers

	${CP}  ../samples/dom/wrappers/Makefile                     ../source/samples/dom/wrappers

#-samples/sax

	${CP}  ../samples/sax/Makefile  ../source/samples/sax

#-samples/sax/helpers

	${CP}  ../samples/sax/helpers/Makefile                      ../source/samples/sax/helpers

#-samples/ui

	${MKDIR}  ../source/samples/ui
	${CP}  ../samples/ui/Makefile  ../source/samples/ui

###
#copy higher levels Makefiles

#-samples
	${CP}  ../samples/Makefile  ../source/samples

# copy the license, ANT build file, Status file, and READMEs
	${CP}  ../build.xml ../Readme ../Readme.html ../STATUS ../source















