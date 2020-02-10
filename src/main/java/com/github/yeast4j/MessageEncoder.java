/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.yeast4j.util.ByteBufferUtils;
import com.github.yeast4j.util.StringUtils;


public class MessageEncoder {
	
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	private int offset;
	private ByteBuffer resultBuffer;
	private Map arrayIndexCache;
	private ExpressionEvaluator evaluator;
	private String charsetName;
	private Logger logger = LoggerFactory.getLogger(MessageEncoder.class);
	
	
	public byte[] encode(Message message) {
		return encode(message, DEFAULT_CHARSET);
	}
	
	/*
	 * 메시지를 바이트 스트림으로 변환(message to byte stream)
	 * charsetName: 결과 바이트 스트림의 캐릭터셋
	 */
	public byte[] encode(Message message, String charsetName) {
		logger.debug("=========== Message Encoding ===========");
		this.charsetName = charsetName;
		offset = 0;
		
		arrayIndexCache = new HashMap();
		resultBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		
		MessageFormat messageFormat = message.getMessageFormat();
		Map<String, Object> dataMap = message.getDataMap();
		evaluator = new ExpressionEvaluator(messageFormat, dataMap);
		Node root = messageFormat.getRoot();
		
		traverseTree(dataMap, root);
		
		// 버퍼에서 데이터 만큼만 배열로 리턴한다.
		resultBuffer.flip();
		byte[] resultBytes = new byte[resultBuffer.remaining()];
		resultBuffer.get(resultBytes);
		
		arrayIndexCache.clear();	// 캐쉬 정리
		resultBuffer.clear();
		
		return resultBytes;
	}
	
	private void traverseTree(Map<String, Object> dataMap, Node node) {
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
//				System.out.println(" 배열 크기값 오류!! "+arrayLength);
				logger.error(arrayLength+" 배열 크기값 오류!! "+arrayLength);
			}
			
			repeatArray(dataMap, node, length);
			
		} else {
			if(userObject != null) {	// 일반 필더일 경우 처리
				Field field = (Field)userObject;
				offset = offset + field.getLength();
				String key = node.getPath();
				
				if(key.indexOf('[') > 0) {	// 배열이 있으면 배열인덱스를 해결한다.
					key = resolveArrayIndex(node);
				}
				
				Object value = dataMap.get(key);
				
				// default 속성이 있으면 처리
				String defaultValue = field.getDefaultValue();
				if(defaultValue != null)  {
					if(defaultValue.matches(Constants.EVAL_REGEX)) {	// 값이 수식일 경우 처리
						defaultValue = evaluator.evaluate(defaultValue);
//						System.out.println(key+": default value Regex 결과:"+defaultValue);
						logger.debug(key+": default value Regex 결과:"+defaultValue);
					}
				}
				
				// 문자이면
				if(field.getDataType() == 's') {
					String strValue = null;
					if(value != null) strValue = (String)value;
					else strValue = defaultValue;
					
					if(field.getPadSign() == '-') {
						strValue = StringUtils.rightPad(strValue, field.getLength(), field.getPadChar());
					} else {
						strValue = StringUtils.leftPad(strValue, field.getLength(), field.getPadChar());
					}
					resultBuffer = ByteBufferUtils.put(resultBuffer, stringToBytes(strValue), field.getLength());
				
				// 숫자이면
				} else if(field.getDataType() == 'd') {
					String strValue = null;
					if(value != null) strValue = String.valueOf(value);	// string으로 변환
					else strValue = defaultValue;
					
					if(field.getPadSign() == '-') {
						strValue = StringUtils.rightPad(strValue, field.getLength(), field.getPadChar());
					} else {
						strValue = StringUtils.leftPad(strValue, field.getLength(), field.getPadChar());
					}
					
					resultBuffer = ByteBufferUtils.put(resultBuffer, strValue.getBytes(), field.getLength());
				
				// binary일 경우
				} else if(field.getDataType() == 'b') {
					byte[] bytes = null;
					if(value != null) {
						bytes = ByteBufferUtils.padding((byte[])value, field.getLength());
					} else {
						bytes = new byte[field.getLength()];
					}	
					resultBuffer = ByteBufferUtils.put(resultBuffer, bytes, field.getLength());
					
				} else {
//					System.out.println(field.getDataType()+" 지원하지 않는 dataType 입니다.");
					logger.error(field.getDataType()+" 지원하지 않는 dataType 입니다.");
				}
				
//				System.out.println("==========>"+key+"("+field.getLength()+") ["+value+"]");
				logger.debug("==========>"+key+"("+field.getLength()+") ["+value+"]");
			}
		
			for(Node each : node.getChildren()) {
				traverseTree(dataMap, each);
			}
		}
	}
	
	private void repeatArray(Map<String, Object> dataMap, Node node, int arrayLength) {	
		for(int i=0; i<arrayLength; i++) {
			arrayIndexCache.put(node.getPath(), i);
//			System.out.println(node.getPath()+" =>"+i);
			logger.debug(node.getPath()+" =>"+i);
			for(Node each : node.getChildren()) {
				traverseTree(dataMap, each);
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
	
	private byte[] stringToBytes(String src) {
		try {
			return src.getBytes(charsetName);
		} catch(UnsupportedEncodingException  e) {
			return src.getBytes();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MessageFormatParser mfp = new MessageFormatParser();
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("C:/Users/kms/Downloads/yeast/ArrayTest.fmt"));
			MessageFormat format = mfp.parse(reader);
			format.showLayout();
/*			
			ArrayList list = new ArrayList(2);
			User user = new User();
			user.setName("김민식");
			user.setAge(44);
			list.add(user);
			
			user = new User();
			user.setName("김미선");
			user.setAge(43);
			list.add(user);
			
			user = new User();
			user.setName("홍길동");
			user.setAge(23);
			list.add(user);
			
			Message message = new Message(format);
			message.setValue("ccc", list);
		
			MessageEncoder encoder = new MessageEncoder();
			byte[] result = encoder.encode(message);
			System.out.println("["+new String(result)+"], length="+result.length);

			User user = new User();
			user.setName("홍길동");
			user.setAge(22);
			user.setAddr("경북 봉화군 소천면 구마동길 513-127");
			
			Message message = new Message(format);
			
			message.setBytes("msgLen", "abcd".getBytes());
			message.setString("schoolName", "abcdefghijkl");
			message.setValue(user);
			message.setInt("classNo", 2);
			message.setString("/ArrayTest/students[0]/학생명", "김민식");
			message.setString("/ArrayTest/students[1]/학생명", "김미선");
			
			MessageEncoder encoder = new MessageEncoder();
			byte[] result = encoder.encode(message);
			
			System.out.println("["+new String(result)+"], length="+result.length);
			
			MessageDecoder decoder = new MessageDecoder(format);
			Message msg = decoder.decode(result);
			
			System.out.println("getAsBytes="+new String(msg.getAsBytes("msgLen")));
			System.out.println("classNo="+msg.getAsInt("classNo"));
*/
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
