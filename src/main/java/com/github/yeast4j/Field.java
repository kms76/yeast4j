/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;


public class Field {
	private static final char SPACE = ' ';
	private static final char ZERO = '0';
	
	private String name;
	private char dataType;
	private int length;
	private boolean isArray;
	private String arrayLength;
	private String defaultValue;
	private String comment;
	private char padSign;	// '-' 이면 오른쪽을 공백처리한다. default는 왼쪽 공백처리
	private char padChar;	// default 값은 space
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public char getDataType() {
		return dataType;
	}
	
	public void setDataType(char dataType) {
		this.dataType = dataType;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}
	
	public String getArrayLength() {
		return arrayLength;
	}
	
	public void setArrayLength(String arrayLength) {
		this.arrayLength = arrayLength;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public char getPadSign() {
		return padSign;
	}

	public void setPadSign(char padSign) {
		this.padSign = padSign;
	}

	public char getPadChar() {
		if(padChar > 0) {
			return padChar;
		} else {
			if(dataType == 'd') return ZERO;
			else return SPACE;
		}
	}

	public void setPadChar(char padChar) {
		this.padChar = padChar;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(name).append("]");
		if(comment != null) {
			sb.append("[").append(comment).append("]");
		}
		sb.append("[").append(length).append("] ");
		sb.append("[dataType=").append(dataType).append("] ");
		sb.append("[isArray=").append(isArray).append("] ");
		sb.append("[arrayLength=").append(arrayLength).append("] ");
		sb.append("[padSign=").append(padSign).append("] ");
		sb.append("[defaultValue=").append(defaultValue).append("]");
		return sb.toString();
	}

}
