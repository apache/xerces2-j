TOP = ../../../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./org/xml/sax
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
	Attributes.class\
	AttributeList.class\
	ContentHandler.class\
	DocumentHandler.class\
	DTDHandler.class\
	EntityResolver.class\
	ErrorHandler.class\
	HandlerBase.class\
	InputSource.class\
	Locator.class\
	Parser.class\
	SAXException.class\
	SAXNotRecognizedException.class\
	SAXNotSupportedException.class\
	SAXParseException.class\
	XMLFilter.class\
	XMLReader.class

all: dirs compile

dirs:
	${MAKE} -C helpers
	${MAKE} -C ext

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../src/classfiles_updated

clean:
	${RM} *.class
	${MAKE} -C helpers clean
	${MAKE} -C ext clean

