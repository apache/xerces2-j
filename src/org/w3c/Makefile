include ../../../src/Makefile.incl
# Makefile for directory ./org/w3c
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
	${MAKE} -C dom/html

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../src/classfiles_updated

clean:
	${MAKE} -C dom clean
	${MAKE} -C dom/html clean


