/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;


public class MessageDecoder {
	
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	private MessageFormat messageFormat;
	private Map<String, Object> dataMap;
	private int offset;
	private Map arrayIndexCache;
	private ExpressionEvaluator evaluator;
	private String charsetName;
	private Logger logger = LoggerFactory.getLogger(MessageDecoder.class);
	
	/*
	 * 바이트 스트림을 메시지로 변환
	 */
	public MessageDecoder(MessageFormat messageFormat) {
		this.messageFormat = messageFormat;
	}
	
	public void setMessageFormat(MessageFormat messageFormat) {
		this.messageFormat = messageFormat;
	}

	public Message decode(byte[] src) {
		return decode(src, 0, DEFAULT_CHARSET);
	}
	
	public Message decode(byte[] src, int srcPos) {
		return decode(src, srcPos, DEFAULT_CHARSET);
	}
	
	/*
	 * 바이트 스트림을 메시지로 변환
	 * srcPos: 입력 바이트에서 시작위치
	 * charsetName: 입력 바이트 스트림의 캐릭터셋
	 */
	public Message decode(byte[] src, int srcPos, String charsetName) {
		logger.debug("=========== Message Decoding ===========");
		this.charsetName = charsetName;
		offset = srcPos;
		
		dataMap = new HashMap<String, Object>();
		arrayIndexCache = new HashMap();
		evaluator = new ExpressionEvaluator(messageFormat, dataMap);
		
		Node root = messageFormat.getRoot();
		
		traverseTree(src, root);

		Message message = new Message(messageFormat, dataMap);
		
		dataMap = null;
		arrayIndexCache.clear();	// 캐쉬 정리
		
		return message;
	}
	
	private void traverseTree(byte[] inputSource, Node node) {
		Object userObject = node.getUserObject();
		
		if(userObject != null && ((Field)userObject).isArray()) {	// 배열 필더일 경우 처리
			Field field = (Field)userObject;
			
			String arrayLength = field.getArrayLength();	// 배열 길이값
			int length = 0;
			
			if(Character.isDigit(arrayLength.charAt(0))) {
				length = Integer.parseInt(arrayLength);
			} else if(arrayLength.matches(Constants.EVAL_REGEX)) {	// 필드참조
				// 수식 처리
				String value = evaluator.evaluate(arrayLength);
//				System.out.println("배열크기 Regex 결과:"+value);
				logger.debug(arrayLength+" Regex 결과:"+value);
				
				if(value != null) {
					length = (int)Float.parseFloat(value);
				}
			} else {
//				System.out.println("배열 크기값 오류!!");
				logger.error(arrayLength+" 배열 크기값 오류!!");
			}
			
			repeatArray(inputSource, node, length);
			
		} else {
			if(userObject != null) {
				Field field = (Field)userObject;
				if(offset+field.getLength() > inputSource.length) {
//					System.out.println("Not Enough Input Data");
					logger.error("Not Enough Input Data");
					return;
				}
				
				Object value = null;
				
				// 문자이면
				if(field.getDataType() == 's') {
					value = byteToString(inputSource, offset, field.getLength());
					
				// 숫자이면
				} else if(field.getDataType() == 'd') {
					String strVal = byteToString(inputSource, offset, field.getLength());
					// 값이 숫자로만 되어 있는지 체크
					if(strVal != null && StringUtils.isNumeric(strVal)) {
						value = Integer.parseInt(strVal);
					} else {
						value = new Integer(0);	// 문자가 들어 있는 경우 0으로 셋팅
					}
				
				// binary이면
				} else if(field.getDataType() == 'b') {
					value = new byte[field.getLength()];
					System.arraycopy(inputSource, offset, value, 0, field.getLength());
				} else {
//					System.out.println(field.getDataType()+" 은 지원하지 않는 dataType 입니다.");
					logger.error(field.getDataType()+" 은 지원하지 않는 dataType 입니다.");
				}
				
				offset = offset + field.getLength();
				String key = node.getPath();
				if(key.indexOf('[') > 0) {	// 배열이 있으면 배열인덱스를 해결한다.
					key = resolveArrayIndex(node);
				}
				
//				System.out.println("==========>"+key+"("+field.getLength()+") - ["+value.toString()+"]");
				logger.debug("==========>"+key+"("+field.getLength()+") - ["+value.toString()+"]");
				if(value != null) {
					dataMap.put(key, value);
				}
			}
			
			for(Node each : node.getChildren()) {
				traverseTree(inputSource, each);
			}
		}
	}
	
	private void repeatArray(byte[] inputSource, Node node, int arrayLength) {
		for(int i=0; i<arrayLength; i++) {
			arrayIndexCache.put(node.getPath(), i);
//			System.out.println(node.getPath()+" =>"+i);
			logger.debug(node.getPath()+" =>"+i);
			for(Node each : node.getChildren()) {
				traverseTree(inputSource, each);
			}
		}
	}
	
	private String resolveArrayIndex(Node node) {
		String path = node.getPath();
		StringBuilder result = new StringBuilder();
		String[] tokens = path.substring(1).split("/");
		
		for(int i=0; i<tokens.length; i++) {
			if(tokens[i].indexOf('[') > 0) {
				StringBuilder sb = new StringBuilder();
				for(int j=0; j<=i; j++) {
					sb.append("/").append(tokens[j]);
				}
				String key = sb.toString();
				if(arrayIndexCache.containsKey(key)) {
					int arrayIndex = (int)arrayIndexCache.get(key);
					String arrayName = tokens[i].substring(0, tokens[i].indexOf('['));
					result.append("/").append(arrayName).append("[").append(arrayIndex).append("]");
				}
			} else {
				result.append("/").append(tokens[i]);
			}
		}
		
		return result.toString();
	}
	
	/*
	 *  byte[] 중에서 srcPos 부터 length길이 만큼 String 으로 반환
	 */
	private String byteToString(byte[] src, int srcPos, int length) {
		byte[] buffer = new byte[length];	
		System.arraycopy(src, srcPos, buffer, 0, length);
		
		try {
			// UTF-8 이면 처리
			if(charsetName.toUpperCase().indexOf("UTF") == 0) {
				return new String(buffer, charsetName);
			} else {
				// UTF-8로 변환
				String temp = new String(buffer, charsetName);
				return new String(temp.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET);
			}
			
		} catch(Exception e) {
//			System.out.println(e.getMessage());
			logger.error(e.getMessage());
			return new String(buffer);
		}
	}
	
	
	/*
	 * byte[] 중에서 srcPos 부터 4byte를 int 값으로 반환
	 */
	public int byteToInt(byte[] src, int srcPos) {
		return (src[srcPos] & 0xff)<<24
				| (src[srcPos+1] & 0xff)<<16
				| (src[srcPos+2] & 0xff)<<8
				| (src[srcPos+3] & 0xff);
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MessageFormatParser mfp = new MessageFormatParser();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("C:/Users/kms/Downloads/yeast/ArrayTest.fmt"));
//			reader = new BufferedReader(new FileReader("C:/java/LPay_1006_RES.txt"));
//			reader = new BufferedReader(new FileReader("C:/java/LPay_1001_RES.txt"));
//			reader = new BufferedReader(new FileReader("C:/java/LPayHeader"));
			MessageFormat format = mfp.parse(reader);
			format.showLayout();
			
//			String inputSource = "003kbcard    1111111111shcard    2222222222nhcard    3333333333                    ";
//			String inputSource = "[1492LMP0110100120170321110139000101001001069700028427110139201703212301020911                       0000정상                                                        8710550000409951CC08PM201607281436458328롯데w                         롯데카드(589*)                                              00000000533872******589*5338720837216478000008911                              1Y신용카드            롯데카드              00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                         000795532017032123003090299502                                                                                                                           ]
			
//			String inputSource = "1490LMP0110100120170322110079100101001001002000013410110079201703221744091701                       0000정상                                                        8710409994781893CC08PM201703211347225746롯데카드                      롯데카드(123*)                                              00000000513791******123*5137910034177507000008911                              1Y신용카드            롯데카드              00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                           00                                                                                                              00000000                                                                       0                                         0015487120170322174338039014  8710409994781893320354                                                                                             ";
			String inputSource = "휘문고    02김민식    국어090영어08010김미선    국어100영어10001";
			
			MessageDecoder decoder = new MessageDecoder(format);
			Message message = decoder.decode(inputSource.getBytes());
//			System.out.println(message.toString());
			
//			MessageEncoder encoder = new MessageEncoder();
//			encoder.encode(message);
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
