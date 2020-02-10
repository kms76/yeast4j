/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j.util;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class ByteBufferUtils {
	
	/**
     * @return a new buffer with the contents of the original buffer and extended by the size
     */
    public static ByteBuffer extendBuffer(ByteBuffer buffer, int size) {
        final ByteBuffer localBuffer = ByteBuffer.allocate(buffer.capacity() + size);
        System.arraycopy(buffer.array(), 0, localBuffer.array(), 0, buffer.position());
        localBuffer.position(buffer.position());
        return localBuffer;
    }
    
    public static ByteBuffer putInt(ByteBuffer dest, int value) {
    	try {
    		dest.putInt(value);
    		return dest;
    	} catch(BufferOverflowException e) {
    		System.out.println("ByteBuffer BufferOverflowException");
    		dest = extendBuffer(dest, dest.capacity());
    		dest.putInt(value);
    		return dest;
    	}
    }
    
    public static ByteBuffer put(ByteBuffer dest, byte[] bytes) {
    	try {
    		dest.put(bytes);
    		return dest;
    	} catch(BufferOverflowException e) {
    		System.out.println("ByteBuffer BufferOverflowException");
    		dest = extendBuffer(dest, dest.capacity());
    		dest.put(bytes);
    		return dest;
    	}
    }
    
    public static ByteBuffer put(ByteBuffer dest, byte[] bytes, int length) {
    	try {
    		dest.put(bytes, 0, length);
    		return dest;
    	} catch(BufferOverflowException e) {
    		System.out.println("ByteBuffer BufferOverflowException");
    		dest = extendBuffer(dest, dest.capacity());
    		dest.put(bytes);
    		return dest;
    	}
    }
    
    /*
     * src의 바이트 길이가 length 보다 작을경우 src를 length 크기만큼 확장시킨다.
     */
    public static byte[] padding(byte[] src, int length) throws IndexOutOfBoundsException {
    	if(src.length >= length) return src;
    	
    	byte[] temp = new byte[length];
        System.arraycopy(src, 0, temp, 0, src.length);
        return temp;
	}
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
