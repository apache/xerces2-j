include ./src/Makefile.incl

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
	${JDK12BIN}/java org.apache.stylebook.StyleBook "targetDirectory=docs/html" docs/docs-book.xml ../../xml-stylebook/styles/apachexml

apidocs:
	echo Building apiDocs in docs directory.
	${MKDIR} docs/apiDocs
	${MAKE} -C src apidocs

package_bin: jars apidocs ${BINZIPFILE}
${BINZIPFILE}: ./src/classfiles_updated

	echo Building a jar file.
	${MKDIR} bin
	${CP} -r docs bin
	${CP} -r data bin
	${RM} -r bin/data/CVS
	mv bin xerces-${PRODUCTVERSION}
	jar cvf ${BINJARFILE} xerces-${PRODUCTVERSION} 
	mv xerces-${PRODUCTVERSION} bin

package_src: ./source/src/Makefile
./source/src/Makefile: ./src/classfiles_updated

	${MAKE} -C src package_src
	${CP} -r data source
	${RM} -r source/data/CVS
	mv source xerces-${PRODUCTVERSION}
	jar cvf ${SRCJARFILE} xerces-${PRODUCTVERSION} 
	mv xerces-${PRODUCTVERSION} source

clean:
	${MAKE} -C src clean
	${MAKE} -C samples clean
	${RM} -rf bin class source docs/apiDocs docs/html
	${RM} ${BINJARFILE} ${SRCJARFILE}


