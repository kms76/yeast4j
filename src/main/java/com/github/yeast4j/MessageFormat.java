/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageFormat {
	
	private Node root;
	
	// Key:노드명, Value: Node객체
	private HashMap<String, Node> nodeTable;
	
	private Logger logger = LoggerFactory.getLogger(MessageFormat.class);
	
	
	public MessageFormat(Node root, HashMap<String, Node> nodeTable) {
		this.root = root;
		this.nodeTable = nodeTable;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public void showLayout() {
//		System.out.println("MessageFormat nodeTable size="+nodeTable.size());
		logger.debug("MessageFormat nodeTable size="+nodeTable.size());
		traverseTree(root, "    ");
	}
	
	private void traverseTree(Node node, String appender) {
		if(node.hasUserObject()) {
			Field field = (Field)node.getUserObject();
//			System.out.println(appender+field.toString()+", path="+node.getPath());
			logger.debug(appender+field.toString()+", path="+node.getPath());
		} else {
//			System.out.println(appender+node.getNodeName()+", path="+node.getPath());
			logger.debug(appender+node.getNodeName()+", path="+node.getPath());
		}
		
		for(Node each : node.getChildren()) {
			traverseTree(each, appender+appender);
		}
	}
	
	public String getNodePath(String nodeName) {
		Node node = nodeTable.get(nodeName);
		if(node != null) return node.getPath();
		return null;
	}
	
	public Node getNode(String nodeName) {
		if(nodeName.indexOf('/') >= 0) {	// 경로정보를 포함한 노드명일 경우
			nodeName = extractNodeName(nodeName);
		}
		return nodeTable.get(nodeName);
	}
	
	public boolean hasNode(String nodeName) {
		if(nodeName.indexOf('/') >= 0) {	// 경로정보를 포함한 노드명일 경우
			nodeName = extractNodeName(nodeName);
		}
		return nodeTable.containsKey(nodeName);
	}
	
	private String extractNodeName(String path) {
		String[] tokens = path.split("/");
		if(tokens[tokens.length-1].equals("/")) return tokens[tokens.length-2];
		else return tokens[tokens.length-1];
	}
	
	private Node searchNode(String nodeName) {
		return searchNode(nodeName, getRoot());
	}
	
	private Node searchNode(String nodeName, Node node) {
		if(node.getNodeName().equals(nodeName)) return node;
		
		for(Node each : node.getChildren()) {
			Node foundNode = searchNode(nodeName, each);
			if(foundNode != null) return foundNode;
		}
		
		return null;
	}
	
}
