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

	echo Building a jar file for binary release.
	${MKDIR} bin
	${CP} -r docs bin
	${RM} -r bin/docs/CVS
	${RM} -r bin/docs/*.xml bin/docs/*.ent
	${CP} -r data bin
	${RM} -r bin/data/CVS
	${CP} LICENSE bin
	mv bin xerces-${PRODUCTVERSION}
	jar cvfM ${BINJARFILE} xerces-${PRODUCTVERSION} 
	mv xerces-${PRODUCTVERSION} bin

package_src: ./source/src/Makefile
./source/src/Makefile: ./src/classfiles_updated

	echo Building a jar file for source release.
	${MAKE} -C src package_src
	${CP} -r data source
	${RM} -r source/data/CVS
	${MKDIR} source/docs
	${CP} docs/*.xml docs/*.ent source/docs
	${CP} LICENSE source
	mv source xerces-${PRODUCTVERSION}
	jar cvfM ${SRCJARFILE} xerces-${PRODUCTVERSION} 
	mv xerces-${PRODUCTVERSION} source

clean:
	${MAKE} -C src clean
	${MAKE} -C samples clean
	${RM} -rf bin class source docs/apiDocs docs/html
	${RM} ${BINJARFILE} ${SRCJARFILE}


