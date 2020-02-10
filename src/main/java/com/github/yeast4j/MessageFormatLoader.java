/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageFormatLoader {
	public static final int LOADING_STRATEGY_LAZY = 0;
	public static final int LOADING_STRATEGY_EAGER = 1;
	
	private static Map<String, MessageFormat> cache = new Hashtable<String, MessageFormat>();
	private static String fmtBaseDir;
	private static int loadingStrategy;
	private static Logger logger = LoggerFactory.getLogger(MessageFormatLoader.class);
	
	public static void setBaseDir(String baseDir) {
		fmtBaseDir = baseDir;
	}
	
	public static void prepareLoad() {
		logger.info("prepareLoad called");
		
		// 필요한 모든 포맷을 로드한다.
		if(!cache.isEmpty()) return;
		// TODO: fmtBaseDir에 있는 모든 파일을 로드하게 처리
		
		if(fmtBaseDir == null) return;
		
		File path = new File(fmtBaseDir);
		File[] fileList = path.listFiles();
		logger.debug("포맷 파일 개수: "+fileList.length);
		
		for(File file : fileList) {
			String fileName = file.getName();
			try {
				if(fileName.indexOf(".fmt") <= 0) continue;
				
				fileName = fileName.replaceAll(".fmt", "");	// 확장자 제거
				MessageFormat fmt = load(fileName);
				cache.put(fileName, fmt);	// MessageFormat을 caching 한다.
			} catch(Exception e) {
				logger.error(fileName+" 포맷 로드 실패: "+e.getMessage());
			}
		}
	}
	
	public static MessageFormat load(String name) throws FileNotFoundException, IOException {
		
		// 캐슁된 것이 있는지 체크
		if(cache.containsKey(name)) {
			return cache.get(name);
		}
		
		// 파일을 파서에게 준다.
		String fileName = fmtBaseDir+"/"+name+Constants.FILE_EXT_FMT;	// C:/fmt
		MessageFormatParser mfp = new MessageFormatParser();
		BufferedReader reader = null;
		
		try {
//			System.out.println("file path="+fileName);
			logger.debug("format file path="+fileName);
		//	reader = new BufferedReader(new FileReader(fileName));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			MessageFormat format = mfp.parse(reader);
			
			if(!format.getRoot().getNodeName().equals(name)) {
//				System.out.println("format name과 format root명이 동일하지 않습니다. "+format.getRoot().getNodeName()+", "+name);
				logger.error("format name과 format root명이 동일하지 않습니다. "+format.getRoot().getNodeName()+", "+name);
			}
			
//			if(format != null) {
//				format.showLayout();
//			}
			
			cache.put(name, format);	// MessageFormat을 caching 한다.
			
			return format;
		} catch(FileNotFoundException e) {
//			System.out.println("FileNotFoundException: "+e.getMessage());
			logger.error("FileNotFoundException: "+e.getMessage());
			throw e;
		} catch(IOException e) {
//			System.out.println("IOException: "+e.getMessage());
			logger.error("IOException: "+e.getMessage());
			throw e;
		} finally {
			if(reader != null) {
				try { reader.close(); } catch(IOException e) {}
			}
		}
	}
	
	public int getLoadingStrategy() {
		return loadingStrategy;
	}
	
	public void setLoadingStrategy(int strategy) {
		loadingStrategy = strategy;
	}

}
