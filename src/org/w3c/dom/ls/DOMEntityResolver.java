/*
 * Copyright (c) 2003 World Wide Web Consortium,
 *
 * (Massachusetts Institute of Technology, European Research Consortium for
 * Informatics and Mathematics, Keio University). All Rights Reserved. This
 * work is distributed under the W3C(r) Software License [1] in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * [1] http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231
 */

package org.w3c.dom.ls;

/**
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 *
 * <code>DOMEntityResolver</code> Provides a way for applications to redirect 
 * references to external entities.
 * <p>Applications needing to implement customized handling for external 
 * entities must implement this interface and register their implementation 
 * by setting the <code>entityResolver</code> attribute of the 
 * <code>DOMBuilder</code>. 
 * <p>The <code>DOMBuilder</code> will then allow the application to intercept 
 * any external entities (including the external DTD subset and external 
 * parameter entities) before including them.
 * <p>Many DOM applications will not need to implement this interface, but it 
 * will be especially useful for applications that build XML documents from 
 * databases or other specialized input sources, or for applications that 
 * use URNs.
 * <p ><b>Note:</b>  <code>DOMEntityResolver</code> is based on the SAX2 [<a href='http://www.saxproject.org/'>SAX</a>] <code>EntityResolver</code> 
 * interface. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030226'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMEntityResolver {
    /**
     * Allow the application to resolve external entities. 
     * <br>The <code>DOMBuilder</code> will call this method before opening 
     * any external entity except the top-level document entity (including 
     * the external DTD subset, external entities referenced within the DTD, 
     * and external entities referenced within the document element); the 
     * application may request that the <code>DOMBuilder</code> resolve the 
     * entity itself, that it use an alternative URI, or that it use an 
     * entirely different input source.
     * <br>Application writers can use this method to redirect external system 
     * identifiers to secure and/or local URIs, to look up public 
     * identifiers in a catalogue, or to read an entity from a database or 
     * other input source (including, for example, a dialog box).
     * <br>If the system identifier is a URI, the <code>DOMBuilder</code> must 
     * resolve it fully before reporting it to the application through this 
     * interface.
     * <p class="editorial"><b>Note:</b>  See issue #4. An alternative would 
     * be to pass the URI out without resolving it, and to provide a base as 
     * an additional parameter. SAX resolves URIs first, and does not 
     * provide a base. 
     * @param publicId The public identifier of the external entity being 
     *   referenced, or <code>null</code> if none was supplied.
     * @param systemId The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], of the 
     *   external entity being referenced exactly as written in the source. 
     * @param baseURI The absolute base URI of the resource being parsed, or 
     *   <code>null</code> if there is no base URI. 
     * @return A <code>DOMInputSource</code> object describing the new input 
     *   source, or <code>null</code> to request that the parser open a 
     *   regular URI connection to the system identifier. 
     */
    public DOMInputSource resolveEntity(String publicId, 
                                        String systemId, 
                                        String baseURI);

}
