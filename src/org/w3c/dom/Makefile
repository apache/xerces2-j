# Makefile for directory ./org/w3c/dom
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
	Attr.class\
	CDATASection.class\
	CharacterData.class\
	Comment.class\
	Document.class\
	DocumentFragment.class\
	DocumentType.class\
	DOMException.class\
	DOMImplementation.class\
	Element.class\
	Entity.class\
	EntityReference.class\
	NamedNodeMap.class\
	Node.class\
	NodeList.class\
	Notation.class\
	ProcessingInstruction.class\
	Text.class

all: dirs compile

dirs:
	${MAKE} -C events
	${MAKE} -C html
	${MAKE} -C traversal

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<
	touch ../../../../src/classfiles_updated

clean:
	${RM} *.class
	${MAKE} -C events clean
	${MAKE} -C html clean
	${MAKE} -C traversal clean

