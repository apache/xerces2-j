/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.domx.events;

import org.w3c.dom.Node;

/**
 * The <code>UIEvent</code> interface provides specific contextual  
 * information associated with User Interface events.  The values for the 
 * keyCode constants are yet to be determined. 
 */
public interface UIEvent extends Event {
  public static final int             CHAR_UNDEFINED       = 1;
  public static final int             KEY_FIRST            = 1;
  public static final int             KEY_LAST             = 1;
  public static final int             VK_0                 = 1;
  public static final int             VK_1                 = 1;
  public static final int             VK_2                 = 1;
  public static final int             VK_3                 = 1;
  public static final int             VK_4                 = 1;
  public static final int             VK_5                 = 1;
  public static final int             VK_6                 = 1;
  public static final int             VK_7                 = 1;
  public static final int             VK_8                 = 1;
  public static final int             VK_9                 = 1;
  public static final int             VK_A                 = 1;
  public static final int             VK_ACCEPT            = 1;
  public static final int             VK_ADD               = 1;
  public static final int             VK_AGAIN             = 1;
  public static final int             VK_ALL_CANDIDATES    = 1;
  public static final int             VK_ALPHANUMERIC      = 1;
  public static final int             VK_ALT               = 1;
  public static final int             VK_ALT_GRAPH         = 1;
  public static final int             VK_AMPERSAND         = 1;
  public static final int             VK_ASTERISK          = 1;
  public static final int             VK_AT                = 1;
  public static final int             VK_B                 = 1;
  public static final int             VK_BACK_QUOTE        = 1;
  public static final int             VK_BACK_SLASH        = 1;
  public static final int             VK_BACK_SPACE        = 1;
  public static final int             VK_BRACELEFT         = 1;
  public static final int             VK_BRACERIGHT        = 1;
  public static final int             VK_C                 = 1;
  public static final int             VK_CANCEL            = 1;
  public static final int             VK_CAPS_LOCK         = 1;
  public static final int             VK_CIRCUMFLEX        = 1;
  public static final int             VK_CLEAR             = 1;
  public static final int             VK_CLOSE_BRACKET     = 1;
  public static final int             VK_CODE_INPUT        = 1;
  public static final int             VK_COLON             = 1;
  public static final int             VK_COMMA             = 1;
  public static final int             VK_COMPOSE           = 1;
  public static final int             VK_CONTROL           = 1;
  public static final int             VK_CONVERT           = 1;
  public static final int             VK_COPY              = 1;
  public static final int             VK_CUT               = 1;
  public static final int             VK_D                 = 1;
  public static final int             VK_DEAD_ABOVEDOT     = 1;
  public static final int             VK_DEAD_ABOVERING    = 1;
  public static final int             VK_DEAD_ACUTE        = 1;
  public static final int             VK_DEAD_BREVE        = 1;
  public static final int             VK_DEAD_CARON        = 1;
  public static final int             VK_DEAD_CEDILLA      = 1;
  public static final int             VK_DEAD_CIRCUMFLEX   = 1;
  public static final int             VK_DEAD_DIAERESIS    = 1;
  public static final int             VK_DEAD_DOUBLEACUTE  = 1;
  public static final int             VK_DEAD_GRAVE        = 1;
  public static final int             VK_DEAD_IOTA         = 1;
  public static final int             VK_DEAD_MACRON       = 1;
  public static final int             VK_DEAD_OGONEK       = 1;
  public static final int             VK_DEAD_SEMIVOICED_SOUND = 1;
  public static final int             VK_DEAD_TILDE        = 1;
  public static final int             VK_DEAD_VOICED_SOUND = 1;
  public static final int             VK_DECIMAL           = 1;
  public static final int             VK_DELETE            = 1;
  public static final int             VK_DIVIDE            = 1;
  public static final int             VK_DOLLAR            = 1;
  public static final int             VK_DOWN              = 1;
  public static final int             VK_E                 = 1;
  public static final int             VK_END               = 1;
  public static final int             VK_ENTER             = 1;
  public static final int             VK_EQUALS            = 1;
  public static final int             VK_ESCAPE            = 1;
  public static final int             VK_EURO_SIGN         = 1;
  public static final int             VK_EXCLAMATION_MARK  = 1;
  public static final int             VK_F                 = 1;
  public static final int             VK_F1                = 1;
  public static final int             VK_F10               = 1;
  public static final int             VK_F11               = 1;
  public static final int             VK_F12               = 1;
  public static final int             VK_F13               = 1;
  public static final int             VK_F14               = 1;
  public static final int             VK_F15               = 1;
  public static final int             VK_F16               = 1;
  public static final int             VK_F17               = 1;
  public static final int             VK_F18               = 1;
  public static final int             VK_F19               = 1;
  public static final int             VK_F2                = 1;
  public static final int             VK_F20               = 1;
  public static final int             VK_F21               = 1;
  public static final int             VK_F22               = 1;
  public static final int             VK_F23               = 1;
  public static final int             VK_F24               = 1;
  public static final int             VK_F3                = 1;
  public static final int             VK_F4                = 1;
  public static final int             VK_F5                = 1;
  public static final int             VK_F6                = 1;
  public static final int             VK_F7                = 1;
  public static final int             VK_F8                = 1;
  public static final int             VK_F9                = 1;
  public static final int             VK_FINAL             = 1;
  public static final int             VK_FIND              = 1;
  public static final int             VK_FULL_WIDTH        = 1;
  public static final int             VK_G                 = 1;
  public static final int             VK_GREATER           = 1;
  public static final int             VK_H                 = 1;
  public static final int             VK_HALF_WIDTH        = 1;
  public static final int             VK_HELP              = 1;
  public static final int             VK_HIRAGANA          = 1;
  public static final int             VK_HOME              = 1;
  public static final int             VK_I                 = 1;
  public static final int             VK_INSERT            = 1;
  public static final int             VK_INVERTED_EXCLAMATION_MARK = 1;
  public static final int             VK_J                 = 1;
  public static final int             VK_JAPANESE_HIRAGANA = 1;
  public static final int             VK_JAPANESE_KATAKANA = 1;
  public static final int             VK_JAPANESE_ROMAN    = 1;
  public static final int             VK_K                 = 1;
  public static final int             VK_KANA              = 1;
  public static final int             VK_KANJI             = 1;
  public static final int             VK_KATAKANA          = 1;
  public static final int             VK_KP_DOWN           = 1;
  public static final int             VK_KP_LEFT           = 1;
  public static final int             VK_KP_RIGHT          = 1;
  public static final int             VK_KP_UP             = 1;
  public static final int             VK_L                 = 1;
  public static final int             VK_LEFT              = 1;
  public static final int             VK_LEFT_PARENTHESIS  = 1;
  public static final int             VK_LESS              = 1;
  public static final int             VK_M                 = 1;
  public static final int             VK_META              = 1;
  public static final int             VK_MINUS             = 1;
  public static final int             VK_MODECHANGE        = 1;
  public static final int             VK_MULTIPLY          = 1;
  public static final int             VK_N                 = 1;
  public static final int             VK_NONCONVERT        = 1;
  public static final int             VK_NUM_LOCK          = 1;
  public static final int             VK_NUMBER_SIGN       = 1;
  public static final int             VK_NUMPAD0           = 1;
  public static final int             VK_NUMPAD1           = 1;
  public static final int             VK_NUMPAD2           = 1;
  public static final int             VK_NUMPAD3           = 1;
  public static final int             VK_NUMPAD4           = 1;
  public static final int             VK_NUMPAD5           = 1;
  public static final int             VK_NUMPAD6           = 1;
  public static final int             VK_NUMPAD7           = 1;
  public static final int             VK_NUMPAD8           = 1;
  public static final int             VK_NUMPAD9           = 1;
  public static final int             VK_O                 = 1;
  public static final int             VK_OPEN_BRACKET      = 1;
  public static final int             VK_P                 = 1;
  public static final int             VK_PAGE_DOWN         = 1;
  public static final int             VK_PAGE_UP           = 1;
  public static final int             VK_PASTE             = 1;
  public static final int             VK_PAUSE             = 1;
  public static final int             VK_PERIOD            = 1;
  public static final int             VK_PLUS              = 1;
  public static final int             VK_PREVIOUS_CANDIDATE = 1;
  public static final int             VK_PRINTSCREEN       = 1;
  public static final int             VK_PROPS             = 1;
  public static final int             VK_Q                 = 1;
  public static final int             VK_QUOTE             = 1;
  public static final int             VK_QUOTEDBL          = 1;
  public static final int             VK_R                 = 1;
  public static final int             VK_RIGHT             = 1;
  public static final int             VK_RIGHT_PARENTHESIS = 1;
  public static final int             VK_ROMAN_CHARACTERS  = 1;
  public static final int             VK_S                 = 1;
  public static final int             VK_SCROLL_LOCK       = 1;
  public static final int             VK_SEMICOLON         = 1;
  public static final int             VK_SEPARATER         = 1;
  public static final int             VK_SHIFT             = 1;
  public static final int             VK_SLASH             = 1;
  public static final int             VK_SPACE             = 1;
  public static final int             VK_STOP              = 1;
  public static final int             VK_SUBTRACT          = 1;
  public static final int             VK_T                 = 1;
  public static final int             VK_TAB               = 1;
  public static final int             VK_U                 = 1;
  public static final int             VK_UNDEFINED         = 1;
  public static final int             VK_UNDERSCORE        = 1;
  public static final int             VK_UNDO              = 1;
  public static final int             VK_UP                = 1;
  public static final int             VK_V                 = 1;
  public static final int             VK_W                 = 1;
  public static final int             VK_X                 = 1;
  public static final int             VK_Y                 = 1;
  public static final int             VK_Z                 = 1;
  /**
   *  <code>altKey</code> indicates whether the 'alt' key was depressed during 
   * the firing of the event.  On some platforms this key may map to an 
   * alternative key name. 
   */
  public boolean            getAltKey();  
  /**
   *  During mouse events caused by the depression or release of a mouse 
   * button, <code>button</code>  is used to indicate which mouse button 
   * changed state. 
   */
  public short              getButton();  
  /**
   *  <code>charCode</code> holds the value of the Unicode character 
   * associated with the depressed key if the event is a key event.  
   * Otherwise, the value is zero. 
   */
  public int                getCharCode();  
  /**
   *  The <code>clickCount</code> property indicates the number of times a 
   * mouse button has been pressed and released over the same screen location 
   * during a user action.  The property value is 1 when the user begins this 
   * action and increments by 1 for each full sequence of pressing and 
   * releasing. If the user moves the mouse between the mousedown and mouseup 
   * the value will be set to 0,  indicating that no click is occurring. 
   */
  public short              getClickCount();  
  /**
   *  <code>clientX</code> indicates the horizontal coordinate at which the 
   * event occurred  relative to the DOM implementation's client area. 
   */
  public int                getClientX();  
  /**
   *  <code>clientY</code> indicates the vertical coordinate at which the 
   * event occurred  relative to the DOM implementation's client area. 
   */
  public int                getClientY();  
  /**
   *  <code>ctrlKey</code> indicates whether the 'ctrl' key was depressed 
   * during the firing of the event. 
   */
  public boolean            getCtrlKey();  
  /**
   *  The value of <code>keyCode</code> holds the virtual key code value of 
   * the key which was depressed if the event is a key event.  Otherwise, the 
   * value is zero. 
   */
  public int                getKeyCode();  
  /**
   *  <code>metaKey</code> indicates whether the 'meta' key was depressed 
   * during the firing of the event.  On some platforms this key may map to 
   * an alternative key name. 
   */
  public boolean            getMetaKey();  
  /**
   *  <code>relatedNode</code> is used to identify a secondary node related to 
   * a UI event. 
   */
  public Node               getRelatedNode();  
  /**
   *  <code>screenX</code> indicates the horizontal coordinate at which the 
   * event occurred in relative to the origin of the screen coordinate system.
   *  
   */
  public int                getScreenX();  
  /**
   *  <code>screenY</code> indicates the vertical coordinate at which the 
   * event occurred  relative to the origin of the screen coordinate system. 
   */
  public int                getScreenY();  
  /**
   *  <code>shiftKey</code> indicates whether the 'shift' key was depressed 
   * during the firing of the event. 
   */
  public boolean            getShiftKey();  
  /**
   * 
   * @param typeArg Specifies the event type.
   * @param canBubbleArg Specifies whether or not the event can bubble.
   * @param cancelableArg Specifies whether or not the event's default  action 
   *   can be prevent.
   * @param screenXArg Specifies the <code>Event</code>'s screen x coordinate
   * @param screenYArg Specifies the <code>Event</code>'s screen y coordinate
   * @param clientXArg Specifies the <code>Event</code>'s client x coordinate
   * @param clientYArg Specifies the <code>Event</code>'s client y coordinate
   * @param ctrlKeyArg Specifies whether or not control key was depressed 
   *   during the  <code>Event</code>.
   * @param altKeyArg Specifies whether or not alt key was depressed during 
   *   the  <code>Event</code>.
   * @param shiftKeyArg Specifies whether or not shift key was depressed 
   *   during the  <code>Event</code>.
   * @param metaKeyArg Specifies whether or not meta key was depressed during 
   *   the  <code>Event</code>.
   * @param keyCodeArg Specifies the <code>Event</code>'s <code>keyCode</code>
   * @param charCodeArg Specifies the <code>Event</code>'s 
   *   <code>charCode</code>
   * @param buttonArg Specifies the <code>Event</code>'s mouse button.
   * @param clickCountArg Specifies the <code>Event</code>'s mouse click count.
   * @param relatedNodeArg Specifies the <code>Event</code>'s related Node.
   */
  public void               initUIEvent(String typeArg, 
										boolean canBubbleArg, 
										boolean cancelableArg, 
										int screenXArg, 
										int screenYArg, 
										int clientXArg, 
										int clientYArg, 
										boolean ctrlKeyArg, 
										boolean altKeyArg, 
										boolean shiftKeyArg, 
										boolean metaKeyArg, 
										int keyCodeArg, 
										int charCodeArg, 
										short buttonArg, 
										short clickCountArg, 
										Node relatedNodeArg);
  public void               setAltKey(boolean altKey);  
  public void               setButton(short button);  
  public void               setCharCode(int charCode);  
  public void               setClickCount(short clickCount);  
  public void               setClientX(int clientX);  
  public void               setClientY(int clientY);  
  public void               setCtrlKey(boolean ctrlKey);  
  public void               setKeyCode(int keyCode);  
  public void               setMetaKey(boolean metaKey);  
  public void               setRelatedNode(Node relatedNode);  
  public void               setScreenX(int screenX);  
  public void               setScreenY(int screenY);  
  public void               setShiftKey(boolean shiftKey);  
}
