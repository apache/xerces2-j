TOP = ../../../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./org/apache/xerces
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
	${MAKE} -C dom
	${MAKE} -C domx
	${MAKE} -C framework
	${MAKE} -C msg
	${MAKE} -C parsers
	${MAKE} -C readers
	${MAKE} -C utils
	${MAKE} -C validators

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../src/classfiles_updated

clean:
	${MAKE} -C dom clean
	${MAKE} -C domx clean
	${MAKE} -C framework clean
	${MAKE} -C msg clean
	${MAKE} -C parsers clean
	${MAKE} -C readers clean
	${MAKE} -C utils clean
	${MAKE} -C validators clean

