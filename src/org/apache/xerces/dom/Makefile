include ../../../../../src/Makefile.incl
# Makefile for directory ./org/apache/xerces/dom
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
	AttrImpl.class\
	AttrNSImpl.class\
	CDATASectionImpl.class\
	CharacterDataImpl.class\
	CommentImpl.class\
	DeepNodeListImpl.class\
	DeferredAttrImpl.class\
	DeferredAttrNSImpl.class\
	DeferredCDATASectionImpl.class\
	DeferredCommentImpl.class\
	DeferredDocumentImpl.class\
	DeferredDocumentTypeImpl.class\
	DeferredElementDefinitionImpl.class\
	DeferredElementImpl.class\
	DeferredElementNSImpl.class\
	DeferredEntityImpl.class\
	DeferredEntityReferenceImpl.class\
	DeferredNode.class\
	DeferredNotationImpl.class\
	DeferredProcessingInstructionImpl.class\
	DeferredTextImpl.class\
	DocumentFragmentImpl.class\
	DocumentImpl.class\
	DocumentTypeImpl.class\
	DOMExceptionImpl.class\
	DOMImplementationImpl.class\
	ElementDefinitionImpl.class\
	ElementImpl.class\
	ElementNSImpl.class\
	EntityImpl.class\
	EntityReferenceImpl.class\
	LCount.class\
	NamedNodeMapImpl.class\
	NodeContainer.class\
	NodeImpl.class\
	NotationImpl.class\
	ProcessingInstructionImpl.class\
	TextImpl.class

all: dirs compile

dirs:
	${MAKE} -C events
	${MAKE} -C traversal

compile: ${TARGETS}

.SUFFIXES:

.SUFFIXES: .class .java

.java.class:
	${JAVAC} $<

	touch ../../../../../src/classfiles_updated

clean:
	${RM} *.class
	${MAKE} -C events clean
	${MAKE} -C traversal clean

