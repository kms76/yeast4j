/* yeast4j is simple library for convert byte stream to message object
 *
 * Copyright (c) 2017 김민식(kim min sik), kaisakim@gmail.com
 */

package com.github.yeast4j;

import java.util.LinkedList;
import java.util.List;

public class Node {
	
	private String nodeName;
	private Node parent;
	private List<Node> children;
	private String path;
	private Object userObject;	// Field 객체
	
	public Node(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public Node(String nodeName, Object userObject) {
		this.nodeName = nodeName;
		this.userObject = userObject;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public List<Node> getChildren() {
		if(children == null) {
			children = new LinkedList<Node>();
		}
		return children;
	}
	
	public void addChild(Node child) {
		if(children == null) {
			children = new LinkedList<Node>();
		}
		
		child.setParent(this);
		children.add(child);
	}
	
	public boolean hasUserObject() {
		if(userObject == null) return false;
		return true;
	}
}
