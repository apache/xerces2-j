TOP = ../../../../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./org/apache/html/dom
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
	HTMLAnchorElementImpl.class\
	HTMLAppletElementImpl.class\
	HTMLAreaElementImpl.class\
	HTMLBRElementImpl.class\
	HTMLBaseElementImpl.class\
	HTMLBaseFontElementImpl.class\
	HTMLBodyElementImpl.class\
	HTMLBuilder.class\
	HTMLButtonElementImpl.class\
	HTMLCollectionImpl.class\
	HTMLDListElementImpl.class\
	HTMLDOMImplementationImpl.class\
	HTMLDirectoryElementImpl.class\
	HTMLDivElementImpl.class\
	HTMLDocumentImpl.class\
	HTMLElementImpl.class\
	HTMLFieldSetElementImpl.class\
	HTMLFontElementImpl.class\
	HTMLFormControl.class\
	HTMLFormElementImpl.class\
	HTMLFrameElementImpl.class\
	HTMLFrameSetElementImpl.class\
	HTMLHRElementImpl.class\
	HTMLHeadElementImpl.class\
	HTMLHeadingElementImpl.class\
	HTMLHtmlElementImpl.class\
	HTMLIFrameElementImpl.class\
	HTMLImageElementImpl.class\
	HTMLInputElementImpl.class\
	HTMLIsIndexElementImpl.class\
	HTMLLIElementImpl.class\
	HTMLLabelElementImpl.class\
	HTMLLegendElementImpl.class\
	HTMLLinkElementImpl.class\
	HTMLMapElementImpl.class\
	HTMLMenuElementImpl.class\
	HTMLMetaElementImpl.class\
	HTMLModElementImpl.class\
	HTMLOListElementImpl.class\
	HTMLObjectElementImpl.class\
	HTMLOptGroupElementImpl.class\
	HTMLOptionElementImpl.class\
	HTMLParagraphElementImpl.class\
	HTMLParamElementImpl.class\
	HTMLPreElementImpl.class\
	HTMLQuoteElementImpl.class\
	HTMLScriptElementImpl.class\
	HTMLSelectElementImpl.class\
	HTMLStyleElementImpl.class\
	HTMLTableCaptionElementImpl.class\
	HTMLTableCellElementImpl.class\
	HTMLTableColElementImpl.class\
	HTMLTableElementImpl.class\
	HTMLTableRowElementImpl.class\
	HTMLTableSectionElementImpl.class\
	HTMLTextAreaElementImpl.class\
	HTMLTitleElementImpl.class\
	HTMLUListElementImpl.class\
	NameNodeListImpl.class

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

