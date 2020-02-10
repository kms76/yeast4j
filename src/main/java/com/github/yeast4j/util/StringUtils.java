/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j.util;

import java.io.UnsupportedEncodingException;


public class StringUtils {
	
	/**
	 *
	 * <pre>
	 * StringUtils.leftPad("123", 5, '-') = "--123"
	 * </pre> 
	 *
	 * @param originalString  the String we want to append to
	 * @param length  target length of the String
	 * @param padChar  the character to padd to the left side of the String
	 * @return String padded
	 */
	public static String leftPad(String originalString, int length, char padChar) {
		if(originalString == null) originalString = "";

		// 한글은 2byte로 계산한다.
		int padLen = length - originalString.getBytes().length;
	      
		if(padLen <= 0) return originalString;
	      
		return padding(padLen, padChar)+originalString; 
	}
	
	public static String rightPad(String originalString, int length, char padChar) {
		if(originalString == null) originalString = "";
		
		// 한글은 EUC-KR은 2byte, UTF-8은 3byte로 계산한다.
		int padLen = length - originalString.getBytes().length;
	      
		if(padLen <= 0) return originalString;
	      
		return originalString+padding(padLen, padChar); 
	}
	
	/**
	 * Returns padding using the specified delimiter repeated
	 * to a given length.
	 *
	 * <pre>
	 * StringUtils.padding(0, 'e')  = ""
	 * StringUtils.padding(3, 'e')  = "eee"
	 * StringUtils.padding(-2, 'e') = IndexOutOfBoundsException
	 * </pre>
	 *
	 * Note: this method doesn't not support padding with
	 * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
	 * as they require a pair of <code>char</code>s to be represented.
	 * If you are needing to support full I18N of your applications
	 * consider using {@link #repeat(String, int)} instead. 
	 * 
	 *
	 * @param repeat  number of times to repeat delim
	 * @param padChar  character to repeat
	 * @return String with repeated character
	 * @throws IndexOutOfBoundsException if <code>repeat &lt; 0</code>
	 * @see #repeat(String, int)
	 */
	private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
		if (repeat < 0) {
			throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
		}
		
		final char[] buf = new char[repeat];
		
		for (int i=0; i<buf.length; i++) {
			buf[i] = padChar;
		}
		
		return new String(buf);
	}

	/**
	 * Checks if a String is empty ("") or null.
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * NOTE: This method changed in Lang version 2.0.
	 * It no longer trims the String.
	 * That functionality is available in isBlank().
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
	
	public static byte[] getBytes(String src, String charsetName) {
		try {
			return src.getBytes(charsetName);
		} catch(UnsupportedEncodingException  e) {
			return src.getBytes();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(StringUtils.leftPad("5555", 10, '0'));
		System.out.println(StringUtils.rightPad("5555", 10, '*'));
		
		char mark = '\"';
		String str = "field=\"aaaa\" ";
		System.out.println("result="+str.indexOf(mark));
	}

}
