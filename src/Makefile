# Makefile for directory .

TARGETS =
DIRS = org javax

TOP = ..
include $(TOP)/src/Makefile.incl

apidocs: ../docs/apiDocs/index.html
../docs/apiDocs/index.html: classfiles_updated

	$(JAVADOC12) -classpath "$(CLPATH)" \
		-windowtitle "Xerces ${PRODUCTVERSION_DOT} API" \
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

#-src/org/apache/xerces/dom/events
	${MKDIR} ../class/org/apache/xerces/dom/events       
	${CP}  ../src/org/apache/xerces/dom/events/*.class  		../class/org/apache/xerces/dom/events

#-src/org/apache/xerces/impl
	${MKDIR} ../class/org/apache/xerces/impl
	${CP}  ../src/org/apache/xerces/impl/*.class			../class/org/apache/xerces/impl

#-src/org/apache/xerces/impl/io
	${MKDIR} ../class/org/apache/xerces/impl/io
	${CP}  ../src/org/apache/xerces/impl/io/*.class			../class/org/apache/xerces/impl/io

#-src/org/apache/xerces/impl/msg
	${MKDIR} ../class/org/apache/xerces/impl/msg
	${CP}  ../src/org/apache/xerces/impl/msg/*.class                      ../class/org/apache/xerces/impl/msg
	${CP}  ../src/org/apache/xerces/impl/msg/*.properties                      ../class/org/apache/xerces/impl/msg

#-src/org/apache/xerces/impl/validation
	${MKDIR} ../class/org/apache/xerces/impl/validation
	${CP}  ../src/org/apache/xerces/impl/validation/*.class                      ../class/org/apache/xerces/impl/validation

#-src/org/apache/xerces/impl/validation/datatypes
	${MKDIR} ../class/org/apache/xerces/impl/validation/datatypes
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/*.class                      ../class/org/apache/xerces/impl/validation/datatypes

#-src/org/apache/xerces/impl/validation/datatypes/regex
	${MKDIR} ../class/org/apache/xerces/impl/validation/datatypes/regex
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/regex/*.class                      ../class/org/apache/xerces/impl/validation/datatypes/regex

#-src/org/apache/xerces/impl/validation/grammars
	${MKDIR} ../class/org/apache/xerces/impl/validation/grammars
	${CP}  ../src/org/apache/xerces/impl/validation/grammars/*.class                      ../class/org/apache/xerces/impl/validation/grammars

#-src/org/apache/xerces/impl/validation/models
	${MKDIR} ../class/org/apache/xerces/impl/validation/models
	${CP}  ../src/org/apache/xerces/impl/validation/models/*.class                      ../class/org/apache/xerces/impl/validation/models

#-src/org/apache/xerces/impl/validation/validators
	${MKDIR} ../class/org/apache/xerces/impl/validation/validators
	${CP}  ../src/org/apache/xerces/impl/validation/validators/*.class                      ../class/org/apache/xerces/impl/validation/validators

#-src/org/apache/xerces/jaxp
	${MKDIR} ../class/org/apache/xerces/jaxp
	${CP}  ../src/org/apache/xerces/jaxp/*.class			../class/org/apache/xerces/jaxp

#-src/org/apache/xerces/parsers
	${MKDIR} ../class/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/*.class			../class/org/apache/xerces/parsers

#-src/org/apache/xerces/util
	${MKDIR} ../class/org/apache/xerces/util
	${CP}  ../src/org/apache/xerces/util/*.class                    ../class/org/apache/xerces/util

#-src/org/apache/xerces/xni
	${MKDIR} ../class/org/apache/xerces/xni
	${CP}  ../src/org/apache/xerces/xni/*.class		../class/org/apache/xerces/xni

#-src/org/apache/xml/serialize
#	${MKDIR} ../class/org/apache/xml/serialize
#	${CP}  ../src/org/apache/xml/serialize/*.class                  ../class/org/apache/xml/serialize
#	${CP}  ../src/org/apache/xml/serialize/*.res                    ../class/org/apache/xml/serialize 

#-src/org/apache/html/dom
#	${MKDIR} ../class/org/apache/html
#	${MKDIR} ../class/org/apache/html/dom
#	${CP}  ../src/org/apache/html/dom/*.class                  ../class/org/apache/html/dom

#-src/org/w3c/dom
	${MKDIR} ../class/org/w3c/dom	
	${CP}  ../src/org/w3c/dom/*.class				../class/org/w3c/dom

#-src/org/w3c/dom/events
	${MKDIR} ../class/org/w3c/dom/events	
	${CP}  ../src/org/w3c/dom/events/*.class			../class/org/w3c/dom/events

#-src/org/w3c/dom/ranges
	${MKDIR} ../class/org/w3c/dom/ranges	
	${CP}  ../src/org/w3c/dom/ranges/*.class			../class/org/w3c/dom/ranges

#-src/org/w3c/dom/html
#	${MKDIR} ../class/org/w3c/dom/html	
#	${CP}  ../src/org/w3c/dom/html/*.class				../class/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${MKDIR} ../class/org/w3c/dom/traversal	
	${CP}  ../src/org/w3c/dom/traversal/*.class			../class/org/w3c/dom/traversal

#-src/org/xml/sax
	${MKDIR} ../class/org/xml/sax
	${CP}  ../src/org/xml/sax/*.class				../class/org/xml/sax

#-src/org/xml/sax/helpers
	${MKDIR} ../class/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/*.class			../class/org/xml/sax/helpers

#-src/org/xml/sax/ext
	${MKDIR} ../class/org/xml/sax/ext
	${CP}  ../src/org/xml/sax/ext/*.class			../class/org/xml/sax/ext

#-src/org/apache/wml
#	${MKDIR} ../class/org/apache/wml
#	${CP}  ../src/org/apache/wml/*.class                  ../class/org/apache/wml
#	${MKDIR} ../class/org/apache/wml/dom
#	${CP}  ../src/org/apache/wml/dom/*.class                  ../class/org/apache/wml/dom

#-src/javax/xml/parsers
	${MKDIR} ../class/javax/xml/parsers
	${CP}  ../src/javax/xml/parsers/*.class                  ../class/javax/xml/parsers

#-src/org/apache/xerces/jaxp
	${MKDIR} ../class/org/apache/xerces/jaxp
	${CP}  ../src/org/apache/xerces/jaxp/*.class                  ../class/org/apache/xerces/jaxp


#make the first jar file
	echo ${PRODUCTVERSION}> ../class/Xerces_Ver_${PRODUCTVERSION}.info
	cd ../class;$(JAR) c0vf ../bin/${JARFILE} *.info org javax; cd ../src

###################################
#copy the .class files under samples
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

#-samples/socket

	${MKDIR} ../class/socket
	${CP}  ../samples/socket/*.class  ../class/socket

#-samples/socket/io

	${MKDIR}  ../class/socket/io
	${CP}  ../samples/socket/io/*.class                      ../class/socket/io

#-samples/ui

#	${MKDIR}  ../class/ui
#	${CP}  ../samples/ui/*.class  ../class/ui

#-samples/xni

	${MKDIR}  ../class/xni
	${CP}  ../samples/xni/*.class  ../class/xni

#-samples/util

	${MKDIR}  ../class/util
	${CP}  ../samples/util/*.class  ../class/util



#Make the second jar file

	cd ../class;$(JAR) c0vf ../bin/${SAMPLESJARFILE} Xerces_Ver_${PRODUCTVERSION}.info dom sax ui util xni;cd ../src

# copy the license, ANT build file, Status file, and READMEs
	${CP}  ../Readme.html ../bin


package_src:

##################################################
#copy the .java .dtd files under src

#-src/org/apache/wml
#	${MKDIR} ../source/src/org/apache/wml
#	${CP}  ../src/org/apache/wml/*.java  			../source/src/org/apache/wml

#-src/org/apache/wml/dom
#	${MKDIR} ../source/src/org/apache/wml/dom
#	${CP}  ../src/org/apache/wml/dom/*.java  			../source/src/org/apache/wml/dom

#-src/org/apache/html/dom
#	${MKDIR} ../source/src/org/apache/html/dom
#	${CP}  ../src/org/apache/html/dom/*.java  			../source/src/org/apache/html/dom

#-src/org/apache/xerces/dom
	${MKDIR} ../source/src/org/apache/xerces/dom
	${CP}  ../src/org/apache/xerces/dom/*.java  			../source/src/org/apache/xerces/dom

#-src/org/apache/xerces/dom/events
	${MKDIR} ../source/src/org/apache/xerces/dom/events       
	${CP}  ../src/org/apache/xerces/dom/events/*.java  		../source/src/org/apache/xerces/dom/events

#-src/org/apache/xerces/impl
	${MKDIR} ../source/src/org/apache/xerces/impl
	${CP}  ../src/org/apache/xerces/impl/*.java			../source/src/org/apache/xerces/impl

#-src/org/apache/xerces/impl/io
	${MKDIR} ../source/src/org/apache/xerces/impl/io
	${CP}  ../src/org/apache/xerces/impl/io/*.java			../source/src/org/apache/xerces/impl/io

#-src/org/apache/xerces/impl/msg
	${MKDIR} ../source/src/org/apache/xerces/impl/msg
	${CP}  ../src/org/apache/xerces/impl/msg/*.java                      ../source/src/org/apache/xerces/impl/msg

#-src/org/apache/xerces/impl/validation
	${MKDIR} ../source/src/org/apache/xerces/impl/validation
	${CP}  ../src/org/apache/xerces/impl/validation/*.java                      ../source/src/org/apache/xerces/impl/validation

#-src/org/apache/xerces/impl/validation/datatypes
	${MKDIR} ../source/src/org/apache/xerces/impl/validation/datatypes
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/*.java                      ../source/src/org/apache/xerces/impl/validation/datatypes

#-src/org/apache/xerces/impl/validation/datatypes/regex
	${MKDIR} ../source/src/org/apache/xerces/impl/validation/datatypes/regex
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/regex/*.java                      ../source/src/org/apache/xerces/impl/validation/datatypes/regex

#-src/org/apache/xerces/impl/validation/grammars
	${MKDIR} ../source/src/org/apache/xerces/impl/validation/grammars
	${CP}  ../src/org/apache/xerces/impl/validation/grammars/*.java                      ../source/src/org/apache/xerces/impl/validation/grammars

#-src/org/apache/xerces/impl/validation/models
	${MKDIR} ../source/src/org/apache/xerces/impl/validation/models
	${CP}  ../src/org/apache/xerces/impl/validation/models/*.java                      ../source/src/org/apache/xerces/impl/validation/models

#-src/org/apache/xerces/impl/validation/validators
	${MKDIR} ../source/src/org/apache/xerces/impl/validation/validators
	${CP}  ../src/org/apache/xerces/impl/validation/validators/*.java                      ../source/src/org/apache/xerces/impl/validation/validators

#-src/org/apache/xerces/jaxp
	${MKDIR} ../source/src/org/apache/xerces/jaxp
	${CP}  ../src/org/apache/xerces/jaxp/*.java			../source/src/org/apache/xerces/jaxp

#-src/org/apache/xerces/parsers
	${MKDIR} ../source/src/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/*.java			../source/src/org/apache/xerces/parsers

#-src/org/apache/xerces/util
	${MKDIR} ../source/src/org/apache/xerces/util
	${CP}  ../src/org/apache/xerces/util/*.java                    ../source/src/org/apache/xerces/util

#-src/org/apache/xerces/xni
	${MKDIR} ../source/src/org/apache/xerces/xni
	${CP}  ../src/org/apache/xerces/xni/*.java		../source/src/org/apache/xerces/xni

#-src/org/apache/xml/serialize
#	${MKDIR} ../source/src/org/apache/xml/serialize
#	${CP}  ../src/org/apache/xml/serialize/*.java                   ../source/src/org/apache/xml/serialize
#	${CP}  ../src/org/apache/xml/serialize/*.res                    ../source/src/org/apache/xml/serialize

#-src/org/w3c/dom
	${MKDIR} ../source/src/org/w3c/dom	
	${CP}  ../src/org/w3c/dom/*.java				../source/src/org/w3c/dom

#-src/org/w3c/dom/events
	${MKDIR} ../source/src/org/w3c/dom/events	
	${CP}  ../src/org/w3c/dom/events/*.java				../source/src/org/w3c/dom/events

#-src/org/w3c/dom/ranges
	${MKDIR} ../source/src/org/w3c/dom/ranges	
	${CP}  ../src/org/w3c/dom/ranges/*.java				../source/src/org/w3c/dom/ranges

#-src/org/w3c/dom/html
#	${MKDIR} ../source/src/org/w3c/dom/html	
#	${CP}  ../src/org/w3c/dom/html/*.java				../source/src/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${MKDIR} ../source/src/org/w3c/dom/traversal	
	${CP}  ../src/org/w3c/dom/traversal/*.java			../source/src/org/w3c/dom/traversal

#-src/org/xml/sax
	${MKDIR} ../source/src/org/xml/sax
	${CP}  ../src/org/xml/sax/*.java				../source/src/org/xml/sax

#-src/org/xml/sax/helpers
	${MKDIR} ../source/src/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/*.java			../source/src/org/xml/sax/helpers

#-src/org/xml/sax/ext
	${MKDIR} ../source/src/org/xml/sax/ext
	${CP}  ../src/org/xml/sax/ext/*.java			../source/src/org/xml/sax/ext

#-src/javax/xml/parsers
	${MKDIR} ../source/src/javax/xml/parsers
	${CP}  ../src/javax/xml/parsers/*.java                  ../source/src/javax/xml/parsers

#-src/org/apache/xerces/jaxp
	${MKDIR} ../source/src/org/apache/xerces/jaxp
	${CP}  ../src/org/apache/xerces/jaxp/*.java                  ../source/src/org/apache/xerces/jaxp

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

#-samples/socket

	${MKDIR} ../source/samples/socket
	${CP}  ../samples/socket/*.java  ../source/samples/socket

#-samples/socket/io

	${MKDIR}  ../source/samples/socket/io
	${CP}  ../samples/socket/io/*.java                      ../source/samples/socket/io

#-samples/ui

#	${MKDIR}  ../source/samples/ui
#	${CP}  ../samples/ui/*.java  ../source/samples/ui

#-samples/xni

	${MKDIR}  ../source/samples/xni
	${CP}  ../samples/xni/*.java  ../source/samples/xni

#-samples/util

	${MKDIR}  ../source/samples/util
	${CP}  ../samples/util/*.java  ../source/samples/util



#########################################
#copy the Makefiles under src

#-src/org/apache/xerces/dom
	${CP}  ../src/org/apache/xerces/dom/Makefile  			../source/src/org/apache/xerces/dom

#-src/org/apache/xerces/dom/events
	${CP}  ../src/org/apache/xerces/dom/events/Makefile  		../source/src/org/apache/xerces/dom/events

#-src/org/apache/xerces/impl
	${CP}  ../src/org/apache/xerces/impl/Makefile		../source/src/org/apache/xerces/impl

#-src/org/apache/xerces/impl/io
	${MKDIR} ../source/src/org/apache/xerces/impl/io
	${CP}  ../src/org/apache/xerces/impl/io/Makefile			../source/src/org/apache/xerces/impl/io

#-src/org/apache/xerces/impl/msg
	${CP}  ../src/org/apache/xerces/impl/msg/Makefile		../source/src/org/apache/xerces/impl/msg

#-src/org/apache/xerces/impl/validation
	${CP}  ../src/org/apache/xerces/impl/validation/Makefile                      ../source/src/org/apache/xerces/impl/validation

#-src/org/apache/xerces/impl/validation/datatypes
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/Makefile                  ../source/src/org/apache/xerces/impl/validation/datatypes

#-src/org/apache/xerces/impl/validation/datatypes/regex
	${CP}  ../src/org/apache/xerces/impl/validation/datatypes/regex/Makefile                  ../source/src/org/apache/xerces/impl/validation/regex

#-src/org/apache/xerces/impl/validation/grammars
	${CP}  ../src/org/apache/xerces/impl/validation/grammars/Makefile                  ../source/src/org/apache/xerces/impl/validation/grammars

#-src/org/apache/xerces/impl/validation/models
	${CP}  ../src/org/apache/xerces/impl/validation/models/Makefile                  ../source/src/org/apache/xerces/impl/validation/models

#-src/org/apache/xerces/impl/validation/validators
	${CP}  ../src/org/apache/xerces/impl/validation/validators/Makefile                  ../source/src/org/apache/xerces/impl/validation/validators

#-src/org/apache/xerces/jaxp
	${CP}  ../src/org/apache/xerces/jaxp/Makefile    ../source/src/org/apache/xerces/jaxp

#-src/org/apache/xerces/parsers
	${CP}  ../src/org/apache/xerces/parsers/Makefile			../source/src/org/apache/xerces/parsers

#-src/org/apache/xerces/util
	${CP}  ../src/org/apache/xerces/util/Makefile                    ../source/src/org/apache/xerces/util

#-src/org/apache/xerces/xni
	${CP}  ../src/org/apache/xerces/xni/Makefile                    ../source/src/org/apache/xerces/xni

#-src/org/apache/xml/serialize
#	${CP}  ../src/org/apache/xml/serialize/Makefile                    ../source/src/org/apache/xml/serialize

#-src/org/w3c/dom
	${CP}  ../src/org/w3c/dom/Makefile				../source/src/org/w3c/dom

#-src/org/w3c/dom/events
	${CP}  ../src/org/w3c/dom/events/Makefile 			../source/src/org/w3c/dom/events

#-src/org/w3c/dom/ranges
	${CP}  ../src/org/w3c/dom/ranges/Makefile			../source/src/org/w3c/dom/ranges

#-src/org/w3c/dom/html
#	${CP}  ../src/org/w3c/dom/html/Makefile				../source/src/org/w3c/dom/html

#-src/org/w3c/dom/traversal
	${CP}  ../src/org/w3c/dom/traversal/Makefile			../source/src/org/w3c/dom/traversal

#-src/org/xml/sax
	${CP}  ../src/org/xml/sax/Makefile				../source/src/org/xml/sax

#-src/org/xml/sax/helpers
	${CP}  ../src/org/xml/sax/helpers/Makefile			../source/src/org/xml/sax/helpers

#-src/org/xml/sax/ext
	${CP}  ../src/org/xml/sax/ext/Makefile			../source/src/org/xml/sax/ext

#-src/org/apache/wml
#	${CP}  ../src/org/apache/wml/Makefile                  ../source/src/org/apache/wml
#	${CP}  ../src/org/apache/wml/dom/Makefile                  ../source/src/org/apache/wml/dom



###
#copy the Makefiles and the batch files at higher levels
#-source
	${CP}  ../Makefile    ../source

#-src
	${CP}  ../src/Makefile    ../source/src

#-src
	${CP}  ../src/Makefile.incl    ../source/src

#-src/org
	${CP}  ../src/org/Makefile    ../source/src/org

#-src/org/apache
	${CP}  ../src/org/apache/Makefile    ../source/src/org/apache

#-src/org/apache/html
#	${CP}  ../src/org/apache/html/Makefile    ../source/src/org/apache/html

#-src/org/apache/html/dom
#	${CP}  ../src/org/apache/html/dom/Makefile    ../source/src/org/apache/html/dom

#-src/org/apache/xerces
	${CP}  ../src/org/apache/xerces/Makefile    ../source/src/org/apache/xerces

#-src/org/apache/xml
	${CP}  ../src/org/apache/xml/Makefile    ../source/src/org/apache/xml

#-src/org
	${CP}  ../src/org/Makefile    ../source/src/org

#-src/org/w3c
	${CP}  ../src/org/w3c/Makefile    ../source/src/org/w3c

#-src/org/xml
	${CP}  ../src/org/xml/Makefile    ../source/src/org/xml


#-src/javax/xml/parsers
	${CP}  ../src/javax/xml/parsers/Makefile    ../source/src/javax/xml/parsers

#-src/javax/xml
	${CP}  ../src/javax/xml/Makefile    ../source/src/javax/xml

#-src/javax
	${CP}  ../src/javax/Makefile    ../source/src/javax


###################################
#copy the Makefiles under samples

#-samples/dom

	${CP}  ../samples/dom/Makefile  ../source/samples/dom

#-samples/dom/traversal

	${CP}  ../samples/dom/traversal/Makefile                    ../source/samples/dom/traversal

#-samples/dom/wrappers

	${CP}  ../samples/dom/wrappers/Makefile                     ../source/samples/dom/wrappers

#-samples/sax

	${CP}  ../samples/socket/Makefile  ../source/samples/socket

#-samples/socket/io

	${CP}  ../samples/socket/io/Makefile                      ../source/samples/socket/io

#-samples/socket

	${CP}  ../samples/sax/Makefile  ../source/samples/sax

#-samples/sax/helpers

	${CP}  ../samples/sax/helpers/Makefile                      ../source/samples/sax/helpers

#-samples/ui

#	${MKDIR}  ../source/samples/ui
#	${CP}  ../samples/ui/Makefile  ../source/samples/ui

#-samples/xni

	${MKDIR}  ../source/samples/xni
	${CP}  ../samples/xni/Makefile  ../source/samples/xni

#-samples/util

	${MKDIR}  ../source/samples/util
	${CP}  ../samples/util/Makefile  ../source/samples/util


###
#copy higher levels Makefiles

#-samples
	${CP}  ../samples/Makefile  ../source/samples

# copy the license, ANT build file, Status file, and READMEs
	${CP}  ../README ../Readme.html ../STATUS ../source
