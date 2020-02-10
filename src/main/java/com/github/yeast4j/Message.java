/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;


public class Message {
	
	private MessageFormat format;
	
	// key=path, value=Integer, String, byte[] 3종류가 있다.
	private Map<String, Object> dataMap;
	
	private Logger logger = LoggerFactory.getLogger(Message.class);
	
	/*
	 * encoder에서 사용
	 */
	public Message(MessageFormat format) {
		this.format = format;
		dataMap = new HashMap<String, Object>();
	}
	
	/*
	 * decoder에서 사용
	 */
	public Message(MessageFormat format, Map<String, Object> dataMap) {
		this.format = format;
		this.dataMap = dataMap;
	}
	
	public MessageFormat getMessageFormat() {
		return format;
	}
	
	public Map<String, Object> getDataMap() {
		return dataMap;
	}
	
	public Object getValue(String fieldName) {
		if(!validateField(fieldName)) return 0;
		
		Object value = null;
		// path 정보가 있으면 처리
		if(fieldName.indexOf('/') >= 0) {
			value = dataMap.get(fieldName);
		// path 정보가 없으면 가져온다.
		} else {
			value = dataMap.get(format.getNodePath(fieldName));
		}
		
		return value;
	}
	
	/*
	 * Returns the value of the field as a int.
	 */
	public int getAsInt(String fieldName) {
		if(!validateField(fieldName)) return 0;
		
		Object value = null;
		// path 정보가 있으면 처리
		if(fieldName.indexOf('/') >= 0) {
			value = dataMap.get(fieldName);
		// path 정보가 없으면 가져온다.
		} else {
			value = dataMap.get(format.getNodePath(fieldName));
		}
		
		if(value != null) return (int)value;
		else return 0;
	}
	
	/*
	 * Returns the value of the field as a string.
	 */
	public String getAsString(String fieldName) {
		if(!validateField(fieldName)) return null;

		Object value = null;
		// path 정보가 있으면 처리
		if(fieldName.indexOf('/') >= 0) {
			value = dataMap.get(fieldName);
		// path 정보가 없으면 가져온다.
		} else {
			value = dataMap.get(format.getNodePath(fieldName));
		}
		
		if(value != null) return (String)value;
		else return null;
	}
	
	/*
	 * Returns the value of the field as a byte array.
	 */
	public byte[] getAsBytes(String fieldName) {
		if(!validateField(fieldName)) return null;
		
		Object value = null;
		// path 정보가 있으면 처리
		if(fieldName.indexOf('/') >= 0) {
			value = dataMap.get(fieldName);
		// path 정보가 없으면 가져온다.
		} else {
			value = dataMap.get(format.getNodePath(fieldName));
		}
		
		if(value != null) return (byte[])value;
		else return null;
	}
	
	/*
	 * Bean 속성명과 동일한 필드명의 값을 Bean에 담아서 리턴한다.
	 */
	public <T> T getAsBean(final Class<T> cls) {
		T bean = null;
		try {
			// 주어진 cls에 대한 객체 생성을 한다.
			bean = ConstructorUtils.invokeConstructor(cls);
		} catch(Exception e) {
//			System.out.println(cls.getName()+" 객체 생성 실패: "+e.getMessage());
			logger.error(cls.getName()+" 객체 생성 실패: "+e.getMessage());
			return null;
		}
		
		// cls의 모든 멤버 변수를 가져온다.
		java.lang.reflect.Field[] fields = FieldUtils.getAllFields(cls);
		
		for (java.lang.reflect.Field field : fields) {
			String fieldName = field.getName();
			String fieldTypeName = field.getGenericType().getTypeName();
//			System.out.println("mapping fieldName="+fieldName+", fieldTypeName="+fieldTypeName);
			logger.debug(cls.getName()+"."+fieldName+", type="+fieldTypeName);
			
			try {
				Object value = null;
				if(fieldTypeName.indexOf("String") >= 0) {
					value = getAsString(fieldName);
				} else if(fieldTypeName.toLowerCase().indexOf("int") >= 0) {
					value = getAsInt(fieldName);
				} else if(fieldTypeName.indexOf("byte[]") >= 0) {
					value = getAsBytes(fieldName);
				} else {
//					System.out.println(fieldTypeName+"는 지원하지 않는 타입입니다.");
					logger.error(fieldTypeName+"는 지원하지 않는 타입입니다.");
				}

				if(value != null) {
					FieldUtils.writeField(bean, fieldName, value, true);
				} else {
//					System.out.println(fieldName+" of value is null");
					logger.debug(fieldName+" of value is null");
				}
				
			} catch(Exception e) {
//				System.out.println(fieldName+": "+e.getMessage());
				logger.error(cls.getName()+"."+fieldName+": "+e.getMessage());
			}
		}

		return bean;
	}
	
	/*
	 * 배열 요소 만큼 Bean을 생성해서 리턴한다.
	 * fieldName은 반드시 배열명 이어야한다.
	 */
	public <T> List<T> getAsBeanList(String fieldName, java.lang.Class<T> cls) {
		Node node = format.getNode(fieldName);
		if(node == null) return null;
		if(node.getUserObject() == null) return null;
		
		Field field = (Field)node.getUserObject();
		if(!field.isArray()) return null;
		
		// 배열 길이를 구한다.
		String arrayLength = field.getArrayLength();	// 배열 길이값 정보
		int length = 0;
		
		if(Character.isDigit(arrayLength.charAt(0))) {
			length = Integer.parseInt(arrayLength);
			
		// 다른 필더를 참조하는 경우
		} else if(arrayLength.matches(Constants.EVAL_REGEX)) {	// 필드참조
			String refFieldName = arrayLength.replaceAll("[^a-zA-Z0-9]", "");	// 영문자만 남기고 제거
			length = getAsInt(refFieldName);
//			System.out.println("array length="+refFieldName+", "+length);
			logger.debug(fieldName+" array length="+refFieldName+", "+length);
		}
		
		if(length <= 0) return null;
		
		String nodePath = node.getPath();
		nodePath = nodePath.replace("[]", "[%d]/%s");
		
		List<T> beanList = new ArrayList<>(length);
		T bean = null;
		
		for(int i=0; i<length; i++) {
			try {
				bean = ConstructorUtils.invokeConstructor(cls);
				java.lang.reflect.Field[] fields = FieldUtils.getAllFields(cls);
				
				for (java.lang.reflect.Field fieldOfBean : fields) {
					String fieldNameOfBean = fieldOfBean.getName();
					String fieldTypeName = fieldOfBean.getGenericType().getTypeName();
//					System.out.println("mapping fieldName="+fieldNameOfBean+", fieldTypeName="+fieldTypeName);
					logger.debug(cls.getName()+"."+fieldNameOfBean+", type="+fieldTypeName);
					
					String key = String.format(nodePath, i, fieldNameOfBean);
//					System.out.println("key="+key);
					logger.debug("field path="+key);
					
					try {
						Object value = null;
						if(fieldTypeName.indexOf("String") >= 0) {
							value = getAsString(key);
						} else if(fieldTypeName.toLowerCase().indexOf("int") >= 0) {
							value = getAsInt(key);
						} else if(fieldTypeName.indexOf("byte[]") >= 0) {
							value = getAsBytes(key);
						} else {
//							System.out.println(fieldTypeName+"는 지원하지 않는 타입입니다.");
							logger.error(fieldTypeName+"는 지원하지 않는 타입입니다.");
						}

						if(value != null) {
							FieldUtils.writeField(bean, fieldNameOfBean, value, true);
						} else {
//							System.out.println(fieldNameOfBean+" of value is null");
							logger.debug(fieldNameOfBean+" of value is null");
						}
						
					} catch(Exception e) {
//						System.out.println(fieldNameOfBean+": "+e.getMessage());
						logger.error(cls.getName()+"."+fieldNameOfBean+": "+e.getMessage());
					}
				}	// end for
				
				beanList.add(bean);
				
			} catch(Exception e) {
//				System.out.println(cls.getName()+" 객체 생성 실패: "+e.getMessage());
				logger.error(cls.getName()+" 객체 생성 실패: "+e.getMessage());
			}
		}
		
		return beanList;
	}
	
	/*
	 * 인자로 주어진 필드명들에 해당 하는 필드들의 값을 맵에 담아서 리턴
	 */
	public Map<String, Object> getAsMap(String... fieldNames) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Object value = null;
		
		for(String fieldName : fieldNames) {
			// path 정보가 있으면 처리
			if(fieldName.indexOf('/') >= 0) {
				value = dataMap.get(fieldName);
			// path 정보가 없으면 가져온다.
			} else {
				value = dataMap.get(format.getNodePath(fieldName));
			}
			
			if(value != null) {
				resultMap.put(fieldName, value);
			}
		}
		
		return resultMap;
	}
	
	/*
	 * 인자로 주어진 map의 key와 매핑되는 값을 map에 채운다.
	 */
	public void populate(Map<String, Object> map) {
		if(map == null) return;
		
		for(String key : map.keySet()) {
			map.put(key, getValue(key));
//			System.out.println(String.format("키 : %s, 값 : %s", key, map.get(key)) );
			logger.debug(String.format("key: %s, value: %s", key, map.get(key)));
		}
	}
	
	/*
	 * 인자로 주어진 bean의 속성과 매핑 되는 값을 bean에 채운다.
	 */
	public void populate(Object bean) {
		// cls의 모든 멤버 변수를 가져온다.
		java.lang.reflect.Field[] fields = FieldUtils.getAllFields(bean.getClass());
		String clsName = bean.getClass().getName();
		
		for (java.lang.reflect.Field field : fields) {
			String fieldName = field.getName();
			String fieldTypeName = field.getGenericType().getTypeName();
//			System.out.println("mapping fieldName="+fieldName+", fieldTypeName="+fieldTypeName);
			logger.debug(clsName+"."+fieldName+", type="+fieldTypeName);
			
			try {
				Object value = null;
				if(fieldTypeName.indexOf("String") >= 0) {
					value = getAsString(fieldName);
				} else if(fieldTypeName.toLowerCase().indexOf("int") >= 0) {
					value = getAsInt(fieldName);
				} else if(fieldTypeName.indexOf("byte[]") >= 0) {
					value = getAsBytes(fieldName);
				} else {
//					System.out.println(fieldTypeName+"는 지원하지 않는 타입입니다.");
					logger.error(fieldTypeName+"는 지원하지 않는 타입입니다.");
				}

				if(value != null) {
					FieldUtils.writeField(bean, fieldName, value, true);
				} else {
//					System.out.println(fieldName+" of value is null");
					logger.debug(fieldName+" of value is null");
				}
				
			} catch(Exception e) {
//				System.out.println(fieldName+": "+e.getMessage());
				logger.error(clsName+"."+fieldName+": "+e.getMessage());
			}
		}
	}
	
	/*
	 * Sets the value of the field with the given int value.
	 */
	public void setAsInt(String fieldName, int value) {
		setValue(fieldName, new Integer(value));
	}
	
	/*
	 * Sets the value of the field with the given String value.
	 */
	public void setAsString(String fieldName, String value) {
		setValue(fieldName, value);
	}
	
	/*
	 * Sets the value of the field with the given byte array value.
	 */
	public void setAsBytes(String fieldName, byte[] bytes) {
		setValue(fieldName, bytes);
	}
	
	private void setValue(String fieldName, Object value) {
		if(!validateField(fieldName)) return;
		
		// path 정보가 있으면 처리
		if(fieldName.indexOf('/') >= 0) {
			dataMap.put(fieldName, value);
		// path 정보가 없으면 가져온다.
		} else {
			dataMap.put(format.getNodePath(fieldName), value);
		}
	}
	
	/*
	 * 메시지 필더 = 필더명과 동일한 map의 key값
	 */
	public void setAsMap(Map<String, Object> map) {
		if(map == null) return;
		
		for(String key : map.keySet()) {
			setValue(key, map.get(key));
//			System.out.println(String.format("키 : %s, 값 : %s", key, map.get(key)));
			logger.debug(String.format("key: %s, value: %s", key, map.get(key)));
		}
	}
	
	/*
	 * Bean 속성명과 동일한 필드명의 값에 값을 넣는다.
	 */
	public void setAsBean(Object bean) {
		// bean class의 모든 멤버변수를 가져온다.
		java.lang.reflect.Field[] fields = FieldUtils.getAllFields(bean.getClass());
		String clsName = bean.getClass().getName();
		
		for (java.lang.reflect.Field field : fields) {
			String fieldName = field.getName();
			String fieldTypeName = field.getGenericType().getTypeName();
//			System.out.println("mapping fieldName="+fieldName+", fieldTypeName="+fieldTypeName);
			logger.debug(clsName+"."+fieldName+", type="+fieldTypeName);
			
			try {
				Object value = FieldUtils.readField(bean, fieldName, true);
				
				if(value != null) {
					if(fieldTypeName.indexOf("String") >= 0) {
						setAsString(fieldName, (String)value);
					} else if(fieldTypeName.toLowerCase().indexOf("int") >= 0) {
						setAsInt(fieldName, (Integer)value);
					} else if(fieldTypeName.indexOf("byte[]") >= 0) {
						setAsBytes(fieldName, (byte[])value);
					} else {
//						System.out.println(fieldTypeName+" is not supported type!!");
						logger.error(fieldTypeName+" is not supported type!!");
					}
				} else {
//					System.out.println(fieldName+" of value is null");
					logger.debug(fieldName+" of value is null");
				}
				
			} catch(Exception e) {
//				System.out.println(fieldName+": "+e.getMessage());
				logger.error(clsName+"."+fieldName+": "+e.getMessage());
			}
		}
	}
	
	/*
	 * 메시지의 배열 필더에 list에 담긴 요소들의 값을 넣는다.
	 * fieldName은 반드시 배열명 이어야한다.
	 */
	public void setAsBeanList(String fieldName, List list) {
		String nodePath = format.getNodePath(fieldName);
		// 해당 필더가 배열인지 확인 한다.
		if(nodePath == null || nodePath.indexOf("[]") < 0) {
//			System.out.println(fieldName+" 는 배열이 아니라서 해당 메소드를 사용할 수 없습니다. "+nodePath);
			logger.error(fieldName+" 는 배열이 아니라서 해당 메소드를 사용할 수 없습니다. "+nodePath);
			return;
		}
		nodePath = nodePath.replace("[]", "[%d]/%s");
		
		for(int i=0; i<list.size(); i++) {
			Object bean = list.get(i);
			java.lang.reflect.Field[] fields = FieldUtils.getAllFields(bean.getClass());
			String clsName = bean.getClass().getName();
//			System.out.println("**필더 개수="+fields.length);
			logger.debug(clsName+" 필드 개수="+fields.length);
			
			for (java.lang.reflect.Field field : fields) {
				String fieldNameOfBean = field.getName();
				String fieldTypeName = field.getGenericType().getTypeName();
//				System.out.println("mapping fieldName="+fieldNameOfBean+", fieldTypeName="+fieldTypeName);
				logger.debug(clsName+"."+fieldNameOfBean+", type="+fieldTypeName);
				
				String key = String.format(nodePath, i, fieldNameOfBean);
//				System.out.println("key="+key);
				logger.debug("path="+key);
				
				try {
					Object value = FieldUtils.readField(bean, fieldNameOfBean, true);
					
					if(value != null) {
						if(fieldTypeName.indexOf("String") >= 0) {
							setAsString(key, (String)value);
						} else if(fieldTypeName.toLowerCase().indexOf("int") >= 0) {
							setAsInt(key, (Integer)value);
						} else if(fieldTypeName.indexOf("byte[]") >= 0) {
							setAsBytes(key, (byte[])value);
						} else {
//							System.out.println(fieldTypeName+" is not supported type!!");
							logger.error(fieldTypeName+" is not supported type!!");
						}
					} else {
//						System.out.println(key+" of value is null");
						logger.debug(key+" of value is null");
					}
					
				} catch(Exception e) {
//					System.out.println(fieldNameOfBean+": "+e.getMessage());
					logger.error(clsName+"."+fieldNameOfBean+": "+e.getMessage());
				}
			}
		}
	}
	
	/*
	 * 인자값 필더명이 유효한지 검사한다.
	 */
	private boolean validateField(String fieldName) {
		if(!format.hasNode(fieldName)) {
//			System.out.println(fieldName+" 필드가 존재하지 않습니다.");
			logger.info(fieldName+" 는 format에 존재하는 않는 필드입니다.");
			return false;
		}
		
		// 배열은 path로만 가능
		if(fieldName.indexOf('[') >= 0) {
			if(fieldName.indexOf('/') < 0) {
//				System.out.println(fieldName+" 배열접근은 path로만 가능합니다.");
				logger.error(fieldName+" 배열접근은 path로만 가능합니다.");
				return false;
			}
		}
		
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> elem : dataMap.entrySet()) {
			sb.append(String.format("key:%s, value:[%s]", elem.getKey(), elem.getValue())).append("\n");
		}
		return sb.toString();
	}
	
	/*
	 * 복사본 리턴 shallow copy
	 */
	public Message clone() {
		// shallow copy
		Map<String, Object> cloneMap = new HashMap<String, Object>();
		cloneMap.putAll(dataMap);
		Message cloneMessage = new Message(format, cloneMap);
		return cloneMessage;
	}

}
