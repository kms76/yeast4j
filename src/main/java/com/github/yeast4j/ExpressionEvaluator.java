/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.util.*;
import java.util.regex.*;

import net.sourceforge.jeval.Evaluator;
import net.sourceforge.jeval.function.FunctionHelper;


public class ExpressionEvaluator {
	
	private MessageFormat format;
	private Map<String, Object> dataMap;
	private Evaluator jeval = null;
	
	public ExpressionEvaluator(MessageFormat format, Map<String, Object> dataMap) {
		this.format = format;
		this.dataMap = dataMap;
	}
	
	public String evaluate(String expr) {
		
		// 표현식에 '#' 변수 기호가 있으면 처리한다.
		if(isComplexExpr(expr)) {
			jeval = new Evaluator();
			String regex = "\\#\\{ *([\\w|ㄱ-ㅎ|ㅏ-ㅣ|가-힣 -]+) *\\}";
			final Pattern pattern = Pattern.compile(regex);
			final Matcher matcher = pattern.matcher(expr);
			
			while(matcher.find()) {
				String var = matcher.group(1);
				System.out.println("var="+var);
				// 배열같은 경우 배열 크기값을 다른 필드를 참조 할 경우 필요하다.
				String nodePath = format.getNodePath(var);
				
				if(nodePath == null) continue;

				Object value = dataMap.get(nodePath);
				if(value != null) {
					String typeName = value.getClass().getTypeName();
					
					if(typeName.indexOf("String") >= 0) {
						jeval.putVariable(var, (String)value);
					} else if(typeName.toLowerCase().indexOf("int") >= 0) {
						jeval.putVariable(var, String.valueOf(value));
					} else {
						System.out.println(typeName+" 은 지원하지 않습니다.");
					}
				}
			}	// end while
			
			try {
				String result = jeval.evaluate(expr);
				if(result.charAt(0) == '\'') {
					result = FunctionHelper.trimAndRemoveQuoteChars(result, '\'');
				}
				return result;
			} catch(Exception e) {
				System.out.println(e.getMessage());
				return null;
			}
			
		} else {	// 단순 필드 참조일 경우
			String fieldName = expr.replaceAll("[^a-zA-Z0-9]", "");
			String nodePath = format.getNodePath(fieldName);
			
			if(nodePath != null) {
				Object value = dataMap.get(nodePath);
				if(value != null) {
					String typeName = value.getClass().getTypeName();
					
					if(typeName.indexOf("String") >= 0) {
						return (String)value;
					} else if(typeName.toLowerCase().indexOf("int") >= 0) {
						return String.valueOf(value);
					} else {
						System.out.println(typeName+" 은 지원하지 않습니다.");
						return null;
					}
				}
			}
			
			return null;
		}

	}
	
	private boolean isComplexExpr(String expr) {
		int repeatCount = 0;
		
		for(int i=0; i<expr.length(); i++) {
			if(expr.charAt(i) == '#') repeatCount++;
		}
		
		// function이 있거나, 변수가 여러개일 경우 복잡하다고 판단한다.
		if(expr.indexOf('(') >= 0 || repeatCount > 1) return true;
		
		return false;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String expr = "date(yyyyMMddHHmmss)";	// tree 형식으로 변환
	//	String expr = "${substring(1, 2)}";	// tree 형식으로 변환
		try {
		//	String expr = "substring('#{aaa}', 0, #{len}) + '_REQ'";
		//	String expr = "substring('&aaa', 0, &len) + '_REQ'";
		//	String expr = "#{aaa}";
			
		//	Map<String, String> dataMap = new HashMap<String, String>();
		//	dataMap.put("aaa", "1234");
		//	dataMap.put("len", "6");
			Evaluator jeval = new Evaluator();
		//	jeval.setVariables(dataMap);
			System.out.println("result="+jeval.evaluate(expr));
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
