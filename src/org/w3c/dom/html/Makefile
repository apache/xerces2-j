TOP = ../../../../..
include $(TOP)/src/Makefile.incl
# Makefile for directory ./org/w3c/dom/html
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
	HTMLAnchorElement.class\
	HTMLAppletElement.class\
	HTMLAreaElement.class\
	HTMLBRElement.class\
	HTMLBaseElement.class\
	HTMLBaseFontElement.class\
	HTMLBodyElement.class\
	HTMLButtonElement.class\
	HTMLCollection.class\
	HTMLDListElement.class\
	HTMLDOMImplementation.class\
	HTMLDirectoryElement.class\
	HTMLDivElement.class\
	HTMLDocument.class\
	HTMLElement.class\
	HTMLFieldSetElement.class\
	HTMLFontElement.class\
	HTMLFormElement.class\
	HTMLFrameElement.class\
	HTMLFrameSetElement.class\
	HTMLHRElement.class\
	HTMLHeadElement.class\
	HTMLHeadingElement.class\
	HTMLHtmlElement.class\
	HTMLIFrameElement.class\
	HTMLImageElement.class\
	HTMLInputElement.class\
	HTMLIsIndexElement.class\
	HTMLLIElement.class\
	HTMLLabelElement.class\
	HTMLLegendElement.class\
	HTMLLinkElement.class\
	HTMLMapElement.class\
	HTMLMenuElement.class\
	HTMLMetaElement.class\
	HTMLModElement.class\
	HTMLOListElement.class\
	HTMLObjectElement.class\
	HTMLOptGroupElement.class\
	HTMLOptionElement.class\
	HTMLParagraphElement.class\
	HTMLParamElement.class\
	HTMLPreElement.class\
	HTMLQuoteElement.class\
	HTMLScriptElement.class\
	HTMLSelectElement.class\
	HTMLStyleElement.class\
	HTMLTableCaptionElement.class\
	HTMLTableCellElement.class\
	HTMLTableColElement.class\
	HTMLTableElement.class\
	HTMLTableRowElement.class\
	HTMLTableSectionElement.class\
	HTMLTextAreaElement.class\
	HTMLTitleElement.class\
	HTMLUListElement.class


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

