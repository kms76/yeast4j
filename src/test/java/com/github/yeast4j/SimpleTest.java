package com.github.yeast4j;

import java.io.IOException;
import java.io.InputStream;

import com.github.yeast4j.MessageFormat;
import com.github.yeast4j.MessageFormatLoader;

public class SimpleTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MessageFormatLoader.setBaseDir("C:/eGovFrameDev-3.7.0-64bit/workspace/yeastj/src/main/resources/fmt");

//		InputStream in = SimpleTest.class.getResourceAsStream("/fmt/Lpay_1001.fmt");
		try {
			MessageFormat format = MessageFormatLoader.load("Lpay_1001.fmt");
			
		} catch(IOException e) {
			
		}
	}

}
