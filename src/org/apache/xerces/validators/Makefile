include ../../../../../src/Makefile.incl
# Makefile for directory ./org/apache/xerces/validators
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
	${MAKE} -C datatype
	${MAKE} -C dtd
	${MAKE} -C schema

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../../src/classfiles_updated

clean:
	${MAKE} -C datatype clean
	${MAKE} -C dtd clean
	${MAKE} -C schema clean

