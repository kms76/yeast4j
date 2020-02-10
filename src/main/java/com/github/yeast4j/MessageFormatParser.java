/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 중보노드를 허용하지 않음
 * 		중복노드를 허용할 경우 노드테이블 value에 구분자를 넣어서 삽입 path + '구분자' + path
 * 
 */

public class MessageFormatParser {
	private static String ARRAY_SUFFIX = "[]";	// 배열 접미사
	private YeastScanner scanner;
	private Node root;
	private String formatId;
	private Logger logger = LoggerFactory.getLogger(MessageFormatParser.class);
	
	public MessageFormat parse(Reader source) {
		return parse(source, " ");
	}
	
	public MessageFormat parse(Reader source, String formatId) {
		char metaSymbols[] = new char[] {'{', '}', ',', ':', '(', ')', '[', ']'};
		char ignoringChars[] = new char[] {' ', '\n', '\r', '\t'};
		
		this.formatId = formatId;
		scanner = new YeastScanner(source);
		scanner.setMetaSymbols(metaSymbols);
		scanner.setIgnoringChars(ignoringChars);
		
		buildParseTree(null);
		
		HashMap<String, Node> nodeTable = new HashMap<String, Node>();
		List<Node> duplicatedNodes = new ArrayList<Node>();	// 중복노드
		traverseTree(root, nodeTable, duplicatedNodes);
		
		if(duplicatedNodes.size() > 0) {
//			System.out.println("중복노드로 인하여 MessageFormat 생성 실패");
			logger.error("중복노드로 인하여 MessageFormat 생성 실패");
			return null;
		}
		
		MessageFormat messageFormat = new MessageFormat(root, nodeTable);
		root = null;
		return messageFormat;
	}
	
	private void setOptionalAttr(Field targetField, String attrName, String attrValue) {
		switch(attrName.toLowerCase()) {
		case "comment":
			targetField.setComment(attrValue);
			break;
		case "default":
			targetField.setDefaultValue(attrValue);
			break;
		case "pad":	// padChar
			targetField.setPadChar(attrValue.charAt(0));
			break;
		default:
//			System.out.println("Undefined Attribute");
			logger.error("Undefined Attribute");
		}
	}
	
	// 필수 속성
	private void setRequiredAttr(Field targetField, String attr) {
		// 필수속성(길이, 데이터타입, 패딩신호(sign))
		if(attr.charAt(0) != '%') {
//			System.out.println("Not Required Attribute");
			logger.error("Not Required Attribute");
		}
		
		if(attr.charAt(1) == '-') {
			targetField.setPadSign('-');
		}
		
		char typeChar = attr.charAt(attr.length()-1);
		
		if(validateDataType(typeChar)) {
			targetField.setDataType(typeChar);
		} else {
			// 지원하지 않는 dataType은 string으로 설정한다.
			targetField.setDataType('s');
		}
		
		targetField.setLength(Integer.parseInt(attr.replaceAll("[^0-9]", "")));
	}
	
	// construct
	private void buildParseTree(Node node) {
		if(node != null) {
//			System.out.println(node.getNodeName()+" start");
			logger.debug(node.getNodeName()+" start");
		}
		
		try {
			while(scanner.hasNextMetaSymbol()) {
				char metaSymbol = scanner.nextMetaSymbol();
				
				if(metaSymbol == '{') {
					if(node == null) {
						String token = scanner.getTokenInBuffer();
						if(token == null || token.length() < 1) {
							root = new Node(formatId);
						} else {
							root = new Node(scanner.getTokenInBuffer());
						}
						root.setPath("/"+root.getNodeName());
						buildParseTree(root);
					} else {
						Node child = new Node(scanner.getTokenInBuffer());
						child.setPath(node.getPath()+"/"+child.getNodeName());
						node.addChild(child);
						buildParseTree(child);
					}
				}
				else if(metaSymbol == '}') {
					return;
				}
				else if(metaSymbol == '[') {
					Node child = new Node(scanner.getTokenInBuffer());
					child.setPath(node.getPath()+"/"+child.getNodeName()+ARRAY_SUFFIX);
					node.addChild(child);
					
					Field field = new Field();
					field.setName(child.getNodeName());
					child.setUserObject(field);
					
					scanner.hasNextMetaSymbol();
					
					// 배열 길이값 
					field.setArray(true);
					field.setArrayLength(scanner.getTokenInBuffer());
					
					scanner.hasNextMetaSymbol();
					buildParseTree(child);
				}
				else if(metaSymbol == '@') {
					String reservedWord = scanner.getTokenInBuffer();
//					System.out.println("reservedSymbol="+reservedWord);
//					System.out.println("node="+node.getNodeName());
					logger.debug("reservedSymbol="+reservedWord);
					logger.debug("node="+node.getNodeName());
					
					scanner.hasNextMetaSymbol();
					
					String ident = scanner.getTokenInBuffer();
//					System.out.println("ident="+ident);
					logger.debug("ident="+ident);
					
					// import 처리 수행
					MessageFormat format = MessageFormatLoader.load(ident);
					if(format != null) {
						node.addChild(format.getRoot());
					} else {
//						System.out.println("import fail");
						logger.error("import fail");
					}
	
				}
				else if(metaSymbol == ':') {
					Node child = new Node(scanner.getTokenInBuffer());
					child.setPath(node.getPath()+"/"+child.getNodeName());
					node.addChild(child);
					
					Field field = new Field();
					field.setName(child.getNodeName());
					child.setUserObject(field);
					
					scanner.hasNextMetaSymbol();
					char symb = scanner.nextMetaSymbol();
//					System.out.println("symb="+symb);
					logger.debug("symb="+symb);
					setRequiredAttr(field, scanner.getTokenInBuffer());
					
					// 옵션 속성 처리
					if(symb == '(') {
						while(scanner.hasNextMetaSymbol()) {
							symb = scanner.nextMetaSymbol();
							if(symb == ',') {
								String fieldAttr = scanner.getTokenInBuffer();
								String str[] = fieldAttr.split("=");
								setOptionalAttr(field, str[0], str[1]);
							} else if(symb == ')') {
								String fieldAttr = scanner.getTokenInBuffer();
								String str[] = fieldAttr.split("=");
								setOptionalAttr(field, str[0], str[1]);
								child.setUserObject(field);
								break;
							}
						}
					} else if(symb == '}') {
						return;
					}
					
				}
				else if(metaSymbol == ',') {
					String str = scanner.getTokenInBuffer();
					if(str.indexOf("%") > 0) {
//						System.out.println("syntax error:"+str);
						logger.error("syntax error:"+str);
					}
					continue;
				}
				else {
//					System.out.println("Undefined Meta Symbol: "+metaSymbol);
					logger.error("Undefined Meta Symbol: "+metaSymbol);
				}
			}
			
		} catch(Exception e) {
//			 System.out.println(e.getMessage());
			 logger.error(e.getMessage());
		}
	}
	
	private void traverseTree(Node node, HashMap<String, Node> nodeTable, List<Node> duplicatedNodes) {
//		System.out.println("traverseTree="+node.getNodeName());
		logger.debug("traverseTree="+node.getNodeName());
		
		if(nodeTable.containsKey(node.getNodeName())) {
//			System.out.println("중복노드:"+node.getNodeName());
			logger.error("중복노드:"+node.getNodeName());
			duplicatedNodes.add(node);
		} else {
			nodeTable.put(node.getNodeName(), node);
		}
		
		for(Node each : node.getChildren()) {
			traverseTree(each, nodeTable, duplicatedNodes);
		}
	}
	
	private boolean validateDataType(char typeChar) {
		if(typeChar == 's' || typeChar == 'd' || typeChar == 'b') return true;
		else {
//			System.out.println(typeChar+" dataType은 지원 하지 않습니다.");
			logger.error(typeChar+" dataType은 지원 하지 않습니다.");
			return false;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MessageFormatLoader.setBaseDir("C:/Users/kms/Downloads/yeast");
		
		MessageFormatParser mfp = new MessageFormatParser();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("C:/Users/kms/Downloads/yeast/ArrayTest.fmt"));
	//		reader = new BufferedReader(new FileReader("C:/Users/kms/Downloads/yeast/LPay_1006_REQ.fmt"));
			MessageFormat format = mfp.parse(reader);
			format.showLayout();
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
