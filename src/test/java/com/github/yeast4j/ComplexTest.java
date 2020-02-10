package com.github.yeast4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.yeast4j.Message;
import com.github.yeast4j.MessageEncoder;
import com.github.yeast4j.MessageFormat;
import com.github.yeast4j.MessageFormatLoader;

public class ComplexTest {
	
	public ComplexTest() {
		MessageFormatLoader.setBaseDir("C:/eGovFrameDev-3.7.0-64bit/workspace/yeast4j/src/main/resources/fmt");
	}
	
	public List getBooks() {
		List books = new ArrayList();
		
		Book book1 = new Book();
		book1.setTitle("XML Bible");
		book1.setAuthor("Gwyneth Paltrow");
		book1.setPrice(40000);
		books.add(book1);
		
		Book book2 = new Book();
		book2.setTitle("XML By Example");
		book2.setAuthor("홍길동");
		book2.setPrice(30000);
		books.add(book2);
		
		return books;
	}
	
	public void testBookList() {
		try {
			MessageFormat format = MessageFormatLoader.load("Amazon_BookList_Res");
			Message resp = new Message(format);
			
			List books = getBooks();
			resp.setAsString("keyword", "XML");
			resp.setAsInt("bookCount1", books.size());
			resp.setAsBeanList("books", books);
			
			MessageEncoder encoder = new MessageEncoder();
			byte[] resultBytes = encoder.encode(resp, "UTF-8");
			
			System.out.println("["+new String(resultBytes)+"], length="+resultBytes.length);
			
			List<Book> bookList = resp.getAsBeanList("books", Book.class);
			for(Book book : bookList) {
				System.out.println("title="+book.getTitle()+", author="+book.getAuthor()+", price="+book.getPrice());
			}
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void testClone() {
		try {
			MessageFormat format = MessageFormatLoader.load("Amazon_BookList_Res");
			Message resp = new Message(format);
			
			List books = getBooks();
			resp.setAsString("keyword", "XML");
			resp.setAsInt("bookCount1", books.size());
			
			Message cloneMsg = resp.clone();
			cloneMsg.setAsInt("bookCount1", 9);
			
			System.out.println("resp bookCount1="+resp.getAsInt("bookCount1"));
			System.out.println("clone bookCount1="+cloneMsg.getAsInt("bookCount1"));
			
			Map map = cloneMsg.getAsMap("keyword", "bookCount1");
			System.out.println("map size="+map.size());
			
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	//	ComplexTest test = new ComplexTest();
	//	test.testBookList();
	//	test.testClone();
		
		String value = "123.456";
		String val = String.valueOf(value);
		System.out.println("val["+val+"]");
		
	}

}
