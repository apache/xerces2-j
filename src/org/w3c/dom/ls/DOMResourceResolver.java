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
 *  <code>DOMResourceResolver</code> provides a way for applications to 
 * redirect references to external resources. 
 * <p> Applications needing to implement custom handling for external 
 * resources can implement this interface and register their implementation 
 * by setting the <code>resourceResolver</code> attribute of the 
 * <code>DOMParser</code>. 
 * <p> The <code>DOMParser</code> will then allow the application to intercept 
 * any external entities (including the external DTD subset and external 
 * parameter entities) before including them. 
 * <p> Many DOM applications will not need to implement this interface, but it 
 * will be especially useful for applications that build XML documents from 
 * databases or other specialized input sources, or for applications that 
 * use URN's. 
 * <p ><b>Note:</b>  <code>DOMResourceResolver</code> is based on the SAX2 [<a href='http://www.saxproject.org/'>SAX</a>] <code>EntityResolver</code> 
 * interface. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-LS-20030619'>Document Object Model (DOM) Level 3 Load
and Save Specification</a>.
 */
public interface DOMResourceResolver {
    /**
     *  Allow the application to resolve external resources. 
     * <br> The <code>DOMParser</code> will call this method before opening 
     * any external resource except the top-level document entity (including 
     * the external DTD subset, external entities referenced within the DTD, 
     * and external entities referenced within the document element); the 
     * application may request that the <code>DOMParser</code> resolve the 
     * resource itself, that it use an alternative URI, or that it use an 
     * entirely different input source. 
     * <br> Application writers can use this method to redirect external 
     * system identifiers to secure and/or local URI's, to look up public 
     * identifiers in a catalogue, or to read an entity from a database or 
     * other input source (including, for example, a dialog box). 
     * <br> If the system identifier is a URI, the <code>DOMParser</code> must 
     * resolve it fully before calling this method. 
     * @param publicId  The public identifier of the external entity being 
     *   referenced, or <code>null</code> if no public identifier was 
     *   supplied or if the resource is not an entity. 
     * @param systemId  The system identifier, a URI reference [<a href='http://www.ietf.org/rfc/rfc2396.txt'>IETF RFC 2396</a>], of the 
     *   external resource being referenced. 
     * @param baseURI  The absolute base URI of the resource being parsed, or 
     *   <code>null</code> if there is no base URI. 
     * @return  A <code>DOMInput</code> object describing the new input 
     *   source, or <code>null</code> to request that the parser open a 
     *   regular URI connection to the system identifier. 
     */
    public DOMInput resolveResource(String publicId, 
                                    String systemId, 
                                    String baseURI);

}
