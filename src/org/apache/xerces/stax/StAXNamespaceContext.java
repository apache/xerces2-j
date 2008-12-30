/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.stax;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

/**
 * @xerces.internal
 * 
 * @author Wei Duan
 * 
 * @version $Id$
 */
public class StAXNamespaceContext implements NamespaceContext {
	private org.apache.xerces.xni.NamespaceContext namespaceContext;

	public StAXNamespaceContext(
			org.apache.xerces.xni.NamespaceContext xniContext) {
		this.namespaceContext = xniContext;
	}

	/**
	 * Get Namespace URI bound to a prefix in the current scope.
	 * 
	 * <p>
	 * When requesting a Namespace URI by prefix, the following table describes
	 * the returned Namespace URI value for all possible prefix values:
	 * </p>
	 * 
	 * <table border="2" rules="all" cellpadding="4"> <thead>
	 * <tr>
	 * <th align="center" colspan="2"> <code>getNamespaceURI(prefix)</code>
	 * return value for specified prefixes </th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <th>prefix parameter</th>
	 * <th>Namespace URI return value</th>
	 * </tr>
	 * <tr>
	 * <td><code>DEFAULT_NS_PREFIX</code> ("")</td>
	 * <td>default Namespace URI in the current scope or <code>null</code>
	 * when there is no default Namespace URI in the current scope</td>
	 * </tr>
	 * <tr>
	 * <td>bound prefix</td>
	 * <td>Namespace URI bound to prefix in current scope</td>
	 * </tr>
	 * <tr>
	 * <td>unbound prefix</td>
	 * <td><code>null</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
	 * <td><code>XMLConstants.XML_NS_URI</code>
	 * ("http://www.w3.org/XML/1998/namespace")</td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
	 * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
	 * ("http://www.w3.org/2000/xmlns/")</td>
	 * </tr>
	 * <tr>
	 * <td><code>null</code></td>
	 * <td><code>IllegalArgumentException</code> is thrown</td>
	 * </tr>
	 * </tbody> </table>
	 * 
	 * @param prefix
	 *            prefix to look up
	 * @return Namespace URI bound to prefix in the current scope
	 */
	public String getNamespaceURI(String prefix) {
		return namespaceContext.getURI(prefix);
	}

	/**
	 * Get prefix bound to Namespace URI in the current scope.
	 * 
	 * <p>
	 * To get all prefixes bound to a Namespace URI in the current scope, use
	 * {@link #getPrefixes(String namespaceURI)}.
	 * </p>
	 * 
	 * <p>
	 * When requesting a prefix by Namespace URI, the following table describes
	 * the returned prefix value for all Namespace URI values:
	 * </p>
	 * 
	 * <table border="2" rules="all" cellpadding="4"> <thead>
	 * <tr>
	 * <th align="center" colspan="2"> <code>getPrefix(namespaceURI)</code>
	 * return value for specified Namespace URIs </th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <th>Namespace URI parameter</th>
	 * <th>prefix value returned</th>
	 * </tr>
	 * <tr>
	 * <td>&lt;default Namespace URI&gt;</td>
	 * <td><code>XMLConstants.DEFAULT_NS_PREFIX</code> ("") </td>
	 * </tr>
	 * <tr>
	 * <td>bound Namespace URI</td>
	 * <td>prefix bound to Namespace URI in the current scope, if multiple
	 * prefixes are bound to the Namespace URI in the current scope, a single
	 * arbitrary prefix, whose choice is implementation dependent, is returned</td>
	 * </tr>
	 * <tr>
	 * <td>unbound Namespace URI</td>
	 * <td><code>null</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XML_NS_URI</code>
	 * ("http://www.w3.org/XML/1998/namespace")</td>
	 * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
	 * ("http://www.w3.org/2000/xmlns/")</td>
	 * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
	 * </tr>
	 * <tr>
	 * <td><code>""</code> or <code>null</code></td>
	 * <td><code>IllegalArgumentException</code> is thrown</td>
	 * </tr>
	 * </tbody> </table>
	 * 
	 * @param namespaceURI
	 *            URI of Namespace to lookup
	 * @return prefix bound to Namespace URI in current context
	 */
	public String getPrefix(String namespaceURI) {
		return namespaceContext.getPrefix(namespaceURI);
	}

	/**
	 * Get all prefixes bound to a Namespace URI in the current scope.
	 * 
	 * <p>
	 * <strong>The returned <code>Iterator</code> is <em>not</em>
	 * modifiable. e.g. the <code>remove()</code> method will throw
	 * <code>NoSuchMethodException</code>.</strong>
	 * </p>
	 * 
	 * <p>
	 * Prefixes are returned in an arbitrary, implementation dependent, order.
	 * </p>
	 * 
	 * <p>
	 * When requesting prefixes by Namespace URI, the following table describes
	 * the returned prefixes value for all Namespace URI values:
	 * </p>
	 * 
	 * <table border="2" rules="all" cellpadding="4"> <thead>
	 * <tr>
	 * <th align="center" colspan="2"><code>
	 *         getPrefixes(namespaceURI)</code>
	 * return value for specified Namespace URIs</th>
	 * </tr>
	 * </thead> <tbody>
	 * <tr>
	 * <th>Namespace URI parameter</th>
	 * <th>prefixes value returned</th>
	 * </tr>
	 * <tr>
	 * <td>bound Namespace URI, including the &lt;default Namespace URI&gt;</td>
	 * <td><code>Iterator</code> over prefixes bound to Namespace URI in the
	 * current scope in an arbitrary, implementation dependent, order</td>
	 * </tr>
	 * <tr>
	 * <td>unbound Namespace URI</td>
	 * <td>empty <code>Iterator</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XML_NS_URI</code>
	 * ("http://www.w3.org/XML/1998/namespace")</td>
	 * <td><code>Iterator</code> with one element set to
	 * <code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
	 * </tr>
	 * <tr>
	 * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
	 * ("http://www.w3.org/2000/xmlns/")</td>
	 * <td><code>Iterator</code> with one element set to
	 * <code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
	 * </tr>
	 * <tr>
	 * <td><code>""</code> or <code>null</code></td>
	 * <td><code>IllegalArgumentException</code> is thrown</td>
	 * </tr>
	 * </tbody> </table>
	 * 
	 * @param namespaceURI
	 *            URI of Namespace to lookup
	 * @return <code>Iterator</code> for all prefixes bound to the Namespace
	 *         URI in the current scope
	 */
	public Iterator getPrefixes(String namespaceURI) {
		// TODO : Need to be done
		return null;
	}
}
