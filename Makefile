include ./src/Makefile.incl

all: compile jars apidocs package

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

apidocs:
	echo Building apiDocs in docs directory
	${MKDIR} docs/apiDocs
	${MAKE} -C src apidocs

package_bin: jars apidocs ${BINZIPFILE}
${BINZIPFILE}: ./src/classfiles_updated

	echo Building a zip file and a tar.gz file 
	${MKDIR} bin
	${CP} -r docs bin
	${CP} -r data bin
	mv bin xerces-${PRODUCTVERSION}
	zip -r ${BINZIPFILE} xerces-${PRODUCTVERSION}
	tar cvf ${BINTARFILE} xerces-${PRODUCTVERSION} ; gzip -f ${BINTARFILE}
	mv xerces-${PRODUCTVERSION} bin

package_src: ./source/src/Makefile
./source/src/Makefile: ./src/classfiles_updated

	${MAKE} -C src package_src
	${CP} -r data source
	mv source xerces-${PRODUCTVERSION}
	zip -r ${SRCZIPFILE} xerces-${PRODUCTVERSION}
	tar cvf ${SRCTARFILE} xerces-${PRODUCTVERSION} ; gzip -f ${SRCTARFILE}
	mv xerces-${PRODUCTVERSION} source

clean:
	${MAKE} -C src clean
	${MAKE} -C samples clean


