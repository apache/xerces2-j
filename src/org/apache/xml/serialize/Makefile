include ../../../../../src/Makefile.incl
# Makefile for directory ./org/apache/xml/serialize
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
	OutputFormat.class\
	Serializer.class\
	BaseSerializer.class\
	XMLSerializer.class\
	HTMLSerializer.class\
	XHTMLSerializer.class\
	ElementState.class\
	HTMLdtd.class

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

