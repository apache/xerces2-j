include ../../../../../src/Makefile.incl
# Makefile for directory ./org/apache/xerces/readers
#
# This makefile depends on the following environment variables
# already being defined:
#
#   JAVAC   Java compiler (with options)
#   RM      Host delete file command (with options)
#
# In addition, the CLASSPATH environment variable must
# include the absolute path of the base source directory.

TARGETS=\
	AbstractCharReader.class\
	CharReader.class\
	EBCDICRecognizer.class\
	MIME2Java.class\
	StringReader.class\
	UCSReader.class\
	UCSRecognizer.class\
	UTF8CharReader.class\
	UTF8Reader.class\
	UTF8Recognizer.class\
	XCatalog.class\
	XMLCatalogHandler.class\
	XMLDeclRecognizer.class\
	XMLEntityHandler.class\
	XMLEntityReader.class\
	XMLEntityReaderFactory.class

all: dirs compile

dirs:

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../../src/classfiles_updated

clean:
	${RM} *.class

