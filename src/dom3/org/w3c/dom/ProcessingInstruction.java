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

package org.w3c.dom;

/** 
 * DOM Level 3 WD Experimental:
 * The DOM Level 3 specification is at the stage 
 * of Working Draft, which represents work in 
 * progress and thus may be updated, replaced, 
 * or obsoleted by other documents at any time. 
 * 
 * The <code>ProcessingInstruction</code> interface represents a "processing 
 * instruction", used in XML as a way to keep processor-specific information 
 * in the text of the document.
 * <p ><b>Note:</b>  The property [notation] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
 *  is not accessible from DOM Level 3 Core. 
 * <p>See also the <a href='http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226'>Document Object Model (DOM) Level 3 Core Specification</a>.
 */
public interface ProcessingInstruction extends Node {
    /**
     * The target of this processing instruction. XML defines this as being 
     * the first token following the markup that begins the processing 
     * instruction.
     * <br> This attribute represents the property [target] defined in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     */
    public String getTarget();

    /**
     * The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code>.
     * <br> This attribute represents the property [content] defined by the 
     * Processing Instruction Information Item in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     */
    public String getData();
    /**
     * The content of this processing instruction. This is from the first non 
     * white space character after the target to the character immediately 
     * preceding the <code>?&gt;</code>.
     * <br> This attribute represents the property [content] defined by the 
     * Processing Instruction Information Item in [<a href='http://www.w3.org/TR/2001/REC-xml-infoset-20011024/'>XML Information set</a>]
     * . 
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     */
    public void setData(String data)
                                   throws DOMException;

}
