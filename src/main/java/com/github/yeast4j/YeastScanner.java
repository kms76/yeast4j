/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class YeastScanner {
	private static final char SINGLE_QUOTE = '\'';
	private static final char DOUBLE_QUOTE = '\"';
	private static final char SLASH = '/';
	private static final char BACKSLASH = '\\';
	private static final char ASTERISK  = '*';
	private static final char LEFT_BRACE = '{';
	private static final char RIGHT_BRACE = '}';
	private static final char WHITE_SPACE  = ' ';
	private static final char RESERVED_SYMBOL_AT = '@';
	
	private Reader source;
	private char[] metaSymbols;		// Meta Symbol
	private char[] ignoringChars;	// 무시할 기호
	private StringBuilder buffer;
	private char nextMetaSymbol;
	private Logger logger = LoggerFactory.getLogger(YeastScanner.class);
	
	public YeastScanner(Reader source) {
		if (source == null) {
			throw new NullPointerException("source");
		}
		
		this.source = source;
	}
	
	public void setMetaSymbols(char[] symbols) {
		metaSymbols = symbols;
	}
	
	public void setIgnoringChars(char[] chars) {
		ignoringChars = chars;
	}
	
	public boolean hasNextMetaSymbol() {
		buffer = new StringBuilder();
		
		int r = 0;
		
		try {
			while((r = source.read()) != -1) {
				char ch = (char)r;
				
				if(isDoubleQuote(ch)) {
					readDoubleQuote();
					continue;
				} 
				else if(ch == SLASH) {	// skip comment
					source.mark(1);
					char nextCh = (char)source.read();
					source.reset();
						
					if(nextCh == ASTERISK) {
						source.skip(1);	// '*' skip
						skipComment();
						continue;
					}
				} else if(isMetaSymbol(ch)) {
					nextMetaSymbol = ch;
					return true;
				} else if(ch == RESERVED_SYMBOL_AT) {
					return readReservedSymbolAt();
				} else if(isIgnoringChar(ch)) {
					continue;
				}
				
				buffer.append(ch);
			}
			
		} catch(IOException e) {
//			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
		
		nextMetaSymbol = 0;	// null
		return false;
	}
	
	public char nextMetaSymbol() {
		return nextMetaSymbol;
	}
	
	public String getTokenInBuffer() {
		String token = null;
		
		if(buffer != null) {
			token = buffer.toString();
		}
		
		return token;
	}
	
	private boolean isMetaSymbol(char symbol) {
		for(int i=0; i<metaSymbols.length; i++) {
			if(metaSymbols[i] == symbol) return true;
		}
		return false;
	}
	
	private boolean isIgnoringChar(char ch) {
		for(int i=0; i<ignoringChars.length; i++) {
			if(ignoringChars[i] == ch) return true;
		}
		return false;
	}
	
	private boolean isDoubleQuote(char ch) {
		if(ch == DOUBLE_QUOTE) return true;
		return false;
	}

	private void readDoubleQuote() {
		int r = 0;
		
		try {
			while((r = source.read()) != -1) {
				char ch = (char)r;
				
				if(ch == DOUBLE_QUOTE) break;
				
				buffer.append(ch);
			}
			
		} catch(IOException e) {
//			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
	}

	private boolean readReservedSymbolAt() {
		int r = 0;
		
		try {
			while((r = source.read()) != -1) {
				char ch = (char)r;
				
				if(ch == WHITE_SPACE) break;
				
				buffer.append(ch);
			}
			nextMetaSymbol = RESERVED_SYMBOL_AT;
			return true;
		} catch(IOException e) {
//			System.out.println(e.getMessage());
			logger.error(e.getMessage());
			return false;
		}
	}
	
	private void skipComment() {
		StringBuilder comment = new StringBuilder();
		int r = 0;
		
		try {
			while((r = source.read()) != -1) {
				char ch = (char)r;
				
				// end of comment
				if(ch == ASTERISK) {
					source.mark(1);
					char nextCh = (char)source.read();
					source.reset();
					
					if(nextCh == SLASH) {
						source.skip(1);
						break;
					}
				}
				
				comment.append(ch);
			}
			
//			System.out.println("comment["+comment.toString()+"]");
			logger.debug("comment["+comment.toString()+"]");
			
		} catch(IOException e) {
//			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
	}

	public void close() {
		if(source != null) {
			try { source.close(); } catch(IOException e) {}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BufferedReader reader = null;
		
		try {
	//		reader = new BufferedReader(new InputStreamReader(new FileInputStream("E:/test.txt")));
			
			reader = new BufferedReader(new FileReader("C:/Users/kms/Downloads/yeast/LPayHeader.fmt"));
			
			char metaSymbols[] = new char[] {'{', '}', ',', ':', '(', ')'};
			char ignoringChars[] = new char[] {' ', '\n', '\r', '\t'};
			
			YeastScanner scanner = new YeastScanner(reader);
			scanner.setMetaSymbols(metaSymbols);
			scanner.setIgnoringChars(ignoringChars);
			
			while(scanner.hasNextMetaSymbol()) {
				System.out.println("Meta Symbol="+scanner.nextMetaSymbol()+", token="+scanner.getTokenInBuffer());
			}

		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}

}
