/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.util.*;


public class MessageFormatDetector implements Detector {
	
	private MessageFormat headerFormat;
	
	public MessageFormatDetector() {
		
	}
	
	public MessageFormatDetector(MessageFormat headerFormat) {
		this.headerFormat = headerFormat;
	}
	
	public String detect(byte[] src) {
		return null;
	}

	public String detect(byte[] inputSource, String formatIdPattern) {
		MessageDecoder decoder = new MessageDecoder(headerFormat);
		Message message = decoder.decode(inputSource);
		
		Map<String, Object> dataMap = message.getDataMap();
		
		ExpressionEvaluator evaluator = new ExpressionEvaluator(headerFormat, dataMap);
		String formatId = evaluator.evaluate(formatIdPattern);
		return formatId;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String inputSource = "0100LMP0100100120170314110079100201001001000600013410110079201703141129220900                       ";
		
		try {
			MessageFormatLoader.setBaseDir("C:/Users/kms/Downloads/yeast");
			
			MessageFormat format = MessageFormatLoader.load("LPayHeader");
			
			MessageFormatDetector detector = new MessageFormatDetector(format);
			String id = detector.detect(inputSource.getBytes(), "'LPay_'+'#{전문구분코드}'+'_REQ'");
	//		String id = detector.detect(inputSource, "'LPay_'+substring('#{전문추적번호}', 0, 8)+'_REQ'");
			
			System.out.println("id="+id);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		
//		final String regex = "\\$\\{ *([\\w -]+) *\\}";
//		final String string = "hello ${ person } welcome to ${ university name}. you are enrolled in ${class}";
//		String string = "Lpay_${field1}_${field2}";
//		
//
//		final Pattern pattern = Pattern.compile(regex);
//		final Matcher matcher = pattern.matcher(string);
//
//		while (matcher.find()) {
//		    System.out.println("Full match: " + matcher.group(0));
//		    System.out.println("start="+matcher.start()+", end="+matcher.end());
//		    System.out.println("Group " + 1 + ": " + matcher.group(1));
//		    string = string.replace(matcher.group(0), matcher.group(1));
//		}
//		System.out.println("result="+string);
	}

}
