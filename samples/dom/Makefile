TOP = ../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./dom
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
	DOMAddLines.class\
	DOMCount.class\
	DOMFilter.class\
	DOMParserWrapper.class\
	DOMWriter.class

all: dirs compile

dirs:
	${MAKE} -C traversal
	${MAKE} -C wrappers

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../src/classfiles_updated

clean:
	${RM} *.class
	${MAKE} -C traversal clean
	${MAKE} -C wrappers clean

