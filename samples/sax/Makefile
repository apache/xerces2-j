TOP = ../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./sax
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
	SAXCount.class\
	SAX2Count.class\
	SAXWriter.class\
	SAX2Writer.class

all: dirs compile

dirs:
	${MAKE} -C helpers

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../src/classfiles_updated

clean:
	${RM} *.class
	${MAKE} -C helpers clean

