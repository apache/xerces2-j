TOP = .
include $(TOP)/src/Makefile.incl

all: compile jars docs apidocs package

compile: compile_src compile_samples
package: package_bin package_src 

compile_src:
	echo Building Source
	${MAKE} -C src

compile_samples: compile_src
	echo Building Samples
	${MAKE} -C samples

jars: compile
	echo Building Jar files in bin directory
	${MKDIR} class
	${MKDIR} bin
	${MAKE} -C src jars

docs: src/classfiles_updated
	echo Building Stylebook docs in docs directory
	$(JAVA12) -classpath "$(CLASSPATH)" org.apache.stylebook.StyleBook "targetDirectory=docs/html" docs/docs-book.xml ../../xml-stylebook/styles/apachexml

apidocs:
	echo Building apiDocs in docs directory.
	${MKDIR} docs/apiDocs
	${MAKE} -C src apidocs

package_bin: jars apidocs ${BINZIPFILE}
${BINZIPFILE}: ./src/classfiles_updated

	echo Building a jar file for binary release.
	${MKDIR} bin
	${CP} -r docs bin
	${RM} -r bin/docs/CVS
	${RM} -r bin/docs/*.xml bin/docs/*.ent
	${CP} -r data bin
	${RM} -r bin/data/CVS
	${CP} LICENSE bin
	${RM} bin/build.xml
	$(MV) bin xerces-${PRODUCTVERSION}
	$(JAR) cvfM ${BINZIPFILE} xerces-${PRODUCTVERSION} 
	$(MV) xerces-${PRODUCTVERSION} bin

package_src: ./source/src/Makefile
./source/src/Makefile: ./src/classfiles_updated

	echo Building a jar file for source release.
	${MAKE} -C src package_src
	${CP} -r data source
	${RM} -r source/data/CVS
	${MKDIR} source/docs
	${CP} docs/*.xml source/docs
	${CP} docs/*.ent source/docs
	${CP} LICENSE source
	${RM} bin/build.xml
	$(MV) source xerces-${PRODUCTVERSION}
	$(JAR) cvfM ${SRCZIPFILE} xerces-${PRODUCTVERSION} 
	$(MV) xerces-${PRODUCTVERSION} source

clean:
	${MAKE} -C src clean
	${MAKE} -C samples clean
	${RM} -rf bin class source docs/apiDocs docs/html
	${RM} ${BINZIPFILE} ${SRCZIPFILE}
