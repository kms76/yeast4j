/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

final class Constants {
	
	public static final char SINGLE_QUOTE = '\'';
	public static final char DOUBLE_QUOTE = '\"';
	public static final char SLASH = '/';
	public static final char BACKSLASH = '\\';
	public static final char ASTERISK  = '*';
	public static final char DOLLAR = '$';
	public static final char AMPERSAND = '&';
	public static final char PERCENT = '%';
	public static final char HYPHEN = '-';
	public static final char COLON = ':';
	public static final char SEMI_COLON = ';';
	public static final char COMMA = ',';
	public static final char EQUAL = '=';
	public static final char PIPE = '|';
	public static final char SPACE = ' ';
	public static final String EMPTY = "";
	
	public static final String LEFT_BRACE = "{";
	public static final String RIGHT_BRACE = "}";
	public static final String LEFT_BRACKET = "[";
	public static final String RIGHT_BRACKET = "]";
	public static final String LEFT_PARENTHESIS = "(";
	public static final String RIGHT_PARENTHESIS = ")";
	public static final String LESS_THAN = "<";
	public static final String GREATER_THAN = ">";
	
	public static final char TAB = '\t';
	public static final char CR = '\r';
	public static final char FF = '\f';
	public static final char LF = '\n';
    /** RFC 4180 defines line breaks as CRLF */
	public static final String CRLF = "\r\n";

    /** The end of stream symbol */
	public static final int END_OF_STREAM = -1;
	public static final String ARRAY_SUFFIX = "[]";	// 배열 접미사
	public static final String EXPR_PREFIX = "#{";	// Expression 접두사
    public static final char REF_OP = '&';	// reference operator
    public static final String EVAL_REGEX = ".*[\\{\\}\\*\\+\\-/#(),].*";
    public static final String FILE_EXT_FMT = ".fmt";	// '.fmt' 파일 확장자
}
