/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;


public interface Detector {
	
	public String detect(byte[] src);
	
	public String detect(byte[] src, String formatIdPattern);

}
