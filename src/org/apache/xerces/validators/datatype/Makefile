TOP = ../../../../../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./org/apache/xerces/validators/datatype
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
	BooleanValidator.class\
	DatatypeMessageProvider.class\
	DatatypeValidator.class\
	DecimalValidator.class\
	IllegalFacetException.class\
	IllegalFacetValueException.class\
	IntegerValidator.class\
	InternalDatatypeValidator.class\
	InvalidDatatypeValueException.class\
	RealValidator.class\
	StringValidator.class\
        TimeDurationValidator.java \
        TimeInstantValidator.java \
	UnknownFacetException.class

all: dirs compile

dirs:

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../../../src/classfiles_updated

clean:
	${RM} *.class

