include ../../../../../../src/Makefile.incl
# Makefile for directory ./org/apache/xerces/validators/dtd
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
	CMBinOp.class\
	CMException.class\
	CMLeaf.class\
	CMNode.class\
	CMStateSet.class\
	CMUniOp.class\
	DFAContentModel.class\
	DTDValidator.class\
	ElementDeclPool.class\
	EntityPool.class\
	InsertableElementsInfo.class\
	MixedContentModel.class\
	SimpleContentModel.class\
	XMLContentModel.class

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

